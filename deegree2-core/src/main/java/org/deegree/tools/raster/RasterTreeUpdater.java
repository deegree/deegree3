//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
----------------------------------------------------------------------------*/
package org.deegree.tools.raster;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.io.dbaseapi.DBaseException;
import org.deegree.io.shpapi.HasNoDBaseFileException;
import org.deegree.io.shpapi.ShapeFile;
import org.deegree.model.coverage.grid.WorldFile;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcwebservices.wcs.configuration.Resolution;
import org.deegree.ogcwebservices.wcs.configuration.ShapeResolution;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageDescriptionDocument;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;
import org.deegree.ogcwebservices.wcs.describecoverage.InvalidCoverageDescriptionExcpetion;
import org.xml.sax.SAXException;

import com.sun.media.jai.codec.FileSeekableStream;

/**
 * The <code>RasterTreeUpdater</code> is a command line utility that can be used in addition to the
 * <code>RasterTreeBuilder</code> to update a previously generated raster tree.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class RasterTreeUpdater {

    private static final ILogger LOG = LoggerFactory.getLogger( RasterTreeUpdater.class );

    private RTUConfiguration config;

    private SortedMap<Double, ShapeResolution> shapeFiles;

    private Cache imgCache;

    // is determined automatically off one of the output filenames
    private String format;

    /**
     * Creates a new <code>RasterTreeUpdater</code> configured through the options contained in the passed
     * configuration
     *
     * @param config
     * @throws IllegalStateException
     * @throws CacheException
     * @throws IOException
     */
    public RasterTreeUpdater( RTUConfiguration config ) throws IllegalStateException, CacheException, IOException {
        this.config = config;

        // a lot of lines just for a simple cache, but what the heck...
        CacheManager singletonManager = CacheManager.create();
        if ( singletonManager.getCache( "imgCache" ) == null ) {
            Cache cache = new Cache( "imgCache", 10, MemoryStoreEvictionPolicy.LFU, false, ".", false, 3600, 3600,
                                     false, 240, null );
            singletonManager.addCache( cache );
            imgCache = singletonManager.getCache( "imgCache" );
        } else {
            imgCache = singletonManager.getCache( "imgCache" );
            imgCache.removeAll();
        }
    }

    /**
     * loads an image
     *
     * @param imageSource
     * @return
     * @throws IOException
     */
    private TiledImage loadImage( String imageSource )
                            throws IOException {

        TiledImage ti = null;
        Element elem = imgCache.get( imageSource );
        if ( elem != null ) {
            ti = (TiledImage) elem.getObjectValue();
        }

        if ( ti == null ) {
            if ( config.verbose ) {
                LOG.logInfo( "Cache size: " + imgCache.getSize() );
                LOG.logInfo( "Reading image: " + imageSource );
            }

            FileSeekableStream fss = new FileSeekableStream( imageSource );
            RenderedOp rop = JAI.create( "stream", fss );
            BufferedImage bi = rop.getAsBufferedImage();
            fss.close();
            ti = new TiledImage( bi, 500, 500 );
            imgCache.put( new Element( imageSource, ti ) );
        }

        return ti;
    }

    /**
     * Initializes the instance.
     *
     * @throws IOException
     * @throws SAXException
     * @throws InvalidCoverageDescriptionExcpetion
     * @throws UnknownCRSException
     */
    public void init()
                            throws IOException, SAXException, InvalidCoverageDescriptionExcpetion, UnknownCRSException {
        CoverageDescriptionDocument doc = new CoverageDescriptionDocument();
        doc.load( config.wcsConfiguration );

        CoverageOffering offering = null;
        if ( config.coverageName == null ) {
            offering = doc.getCoverageOfferings()[0];
        } else {
            for ( CoverageOffering of : doc.getCoverageOfferings() ) {
                if ( of.getName().equals( config.coverageName ) ) {
                    offering = of;
                }
            }
        }

        Resolution[] rs = offering.getExtension().getResolutions();
        shapeFiles = new TreeMap<Double, ShapeResolution>();
        for ( Resolution r : rs ) {
            shapeFiles.put( new Double( r.getMinScale() ), (ShapeResolution) r );
        }

    }

    /**
     * extracts the envelopes that correspond to the filenames of getfilenames
     *
     * @param shapeName
     * @return
     * @throws IOException
     */
    private ArrayList<Envelope> getEnvelopes( String shapeName )
                            throws IOException {
        ShapeFile file = new ShapeFile( shapeName );
        ArrayList<Envelope> envs = new ArrayList<Envelope>( file.getRecordNum() );

        for ( int i = 0; i < file.getRecordNum(); ++i ) {
            Geometry geom = file.getGeometryByRecNo( i + 1 );
            envs.add( geom.getEnvelope() );
            if ( config.verbose ) {
                LOG.logInfo( StringTools.concat( 200, "Envelope of tile is ", geom.getEnvelope() ) );
            }
        }
        file.close();

        return envs;
    }

    /**
     * extracts the filenames of the tiles contained within the shape file dbf
     *
     * @param shapeName
     * @return
     * @throws IOException
     * @throws HasNoDBaseFileException
     * @throws DBaseException
     */
    private ArrayList<String> getTilenames( String shapeName )
                            throws IOException, HasNoDBaseFileException, DBaseException {
        ShapeFile file = new ShapeFile( shapeName );
        String dirName = new File( shapeName ).getParent();
        if ( dirName == null ) {
            dirName = "./";
        }

        ArrayList<String> tileNames = new ArrayList<String>( file.getRecordNum() );
        for ( int i = 0; i < file.getRecordNum(); ++i ) {
            Feature f = file.getFeatureByRecNo( i + 1 );
            FeatureProperty[] p = f.getProperties();
            StringBuffer name = new StringBuffer( 200 );
            name.append( dirName ).append( "/" );
            name.append( ( p[1].getValue() == null ) ? "" : p[1].getValue() );
            name.append( "/" ).append( p[0].getValue() );
            tileNames.add( name.toString() );
            if ( config.verbose ) {
                LOG.logInfo( StringTools.concat( 200, "Found tile ", name ) );
            }
        }
        file.close();

        return tileNames;
    }

    /**
     * returns the envelopes of the files to be updated
     *
     * @return returns the envelopes of the files to be updated
     * @throws IOException
     */
    private ArrayList<Envelope> getUpdatedEnvelopes()
                            throws IOException {
        ArrayList<Envelope> updatedEnvelopes = new ArrayList<Envelope>( config.updatedFiles.size() );

        for ( String filename : config.updatedFiles ) {
            WorldFile wf = WorldFile.readWorldFile( filename, config.worldfileType );
            updatedEnvelopes.add( wf.getEnvelope() );
            if ( config.verbose ) {
                LOG.logInfo( StringTools.concat( 200, "Updating from file ", filename, " with envelope ",
                                                 wf.getEnvelope() ) );
            }
            if ( format == null ) {
                format = filename.substring( filename.lastIndexOf( '.' ) + 1 );
            }
        }

        return updatedEnvelopes;
    }

    /**
     * updates the tiles with the image file
     *
     * @param filename
     * @param envelope
     * @param tileNames
     * @param tileEnvelopes
     * @param res
     * @throws IOException
     */
    private void updateFile( String filename, Envelope envelope, List<String> tileNames, List<Envelope> tileEnvelopes,
                             double res )
                            throws IOException {

        for ( int i = 0; i < tileNames.size(); ++i ) {
            Envelope env = tileEnvelopes.get( i );
            if ( !envelope.intersects( env ) ) {
                continue;
            }
            String tile = tileNames.get( i );

            // paint the new image on top of the existing one
            if ( config.verbose ) {
                LOG.logInfo( StringTools.concat( 200, "Updating tile ", tile, " with image ", filename ) );
            }

            TiledImage tileImage = loadImage( tile );
            WorldFile wf = WorldFile.readWorldFile( filename, config.worldfileType );
            TiledImage inputImage = loadImage( filename );
            Tile t = new Tile( WorldFile.readWorldFile( tile, config.worldfileType ).getEnvelope(), null );
            BufferedImage out = tileImage.getAsBufferedImage();
            float[][] data = null;
            if ( out.getColorModel().getPixelSize() == 16 ) {
                // do not use image api if target bitDepth = 16
                data = new float[out.getHeight()][out.getWidth()];
            }
            RasterTreeBuilder.drawImage( out, data, inputImage, t, wf, res, config.interpolation, null, format,
                                         config.bitDepth, 0, 1 );

            String frm = format;
            if ( "raw".equals( frm ) ) {
                frm = "tif";
            }

            File file = new File( tile ).getAbsoluteFile();

            ImageUtils.saveImage( out, file, config.quality );

        }

    }

    /**
     * a hack to determine the minimum resolution
     *
     * @param shapeName
     * @return
     * @throws IOException
     * @throws HasNoDBaseFileException
     * @throws DBaseException
     */
    private double getLevel( String shapeName )
                            throws IOException, HasNoDBaseFileException, DBaseException {
        ShapeFile file = new ShapeFile( shapeName );
        Feature f = file.getFeatureByRecNo( 1 );
        FeatureProperty[] p = f.getProperties();
        file.close();
        return Double.parseDouble( p[1].getValue().toString() );
    }

    /**
     * Updates the images.
     *
     * @throws IOException
     * @throws DBaseException
     * @throws HasNoDBaseFileException
     */
    public void update()
                            throws IOException, HasNoDBaseFileException, DBaseException {
        SortedMap<Double, ShapeResolution> shapes = new TreeMap<Double, ShapeResolution>();
        shapes.putAll( shapeFiles );

        // stores the envelopes of the files that are being updated
        ArrayList<Envelope> updatedEnvelopes = getUpdatedEnvelopes();

        while ( !shapes.isEmpty() ) {
            ShapeResolution shape = shapes.remove( shapes.firstKey() );
            String shapeName = shape.getShape().getRootFileName();
            double res = getLevel( shapeName );

            LOG.logInfo( StringTools.concat( 200, "Processing shape file ", shapeName, "..." ) );

            // these store the image filenames of the existing tiles and their envelopes
            ArrayList<String> tileNames = getTilenames( shapeName );
            ArrayList<Envelope> envelopes = getEnvelopes( shapeName );

            for ( int i = 0; i < config.updatedFiles.size(); ++i ) {
                String filename = config.updatedFiles.get( i );
                Envelope envelope = updatedEnvelopes.get( i );

                updateFile( filename, envelope, tileNames, envelopes, res );
            }
        }
    }

    /**
     * Prints out usage information and the message, then <code>System.exit</code>s.
     *
     * @param message
     *            can be null
     */
    private static void printUsage( String message ) {
        if ( message != null ) {
            System.out.println( message );
            System.out.println();
        }

        System.out.println( "Usage:" );
        System.out.println();
        System.out.println( "<classpath> <rtu> <options>" );
        System.out.println( "   where" );
        System.out.println( "  <rtu>:" );
        System.out.println( "           java <classpath> org.deegree.tools.raster.RasterTreeUpdater" );
        System.out.println( "  <classpath>:" );
        System.out.println( "           -cp <the classpath containing the deegree.jar and " );
        System.out.println( "                additional required libraries>" );
        System.out.println( "  <option>:" );
        System.out.println( "           as follows:" );
        System.out.println();
        System.out.println( "  -wcs <URL/filename>:" );
        System.out.println( "           The URL or a filename of the WCS configuration that was" );
        System.out.println( "           generated by the RasterTreeBuilder. Mandatory." );
        System.out.println( "  -name <name>:" );
        System.out.println( "           The name of the coverage to update. Optional." );
        System.out.println( "  -verbose:" );
        System.out.println( "           Print out more informational messages." );
        System.out.println( "  -interpolation <name>: " );
        System.out.println( "           The name of the interpolation to be used, as specified in the" );
        System.out.println( "           RasterTreeBuilder. Optional. Default is Nearest Neighbor." );
        System.out.println( "  -depth <n>:" );
        System.out.println( "           The bit depth of the output images. Optional. Default is 16." );
        System.out.println( "  -quality <n>:" );
        System.out.println( "           The desired output quality, between 0 and 1. Optional. Default is 0.95." );
        System.out.println( "  -mapFiles <file1,file2...fileN>:" );
        System.out.println( "           comma seperated list of image files to update. These files" );
        System.out.println( "           need to have a corresponding worldfile, as usual." );
        System.out.println( "  -worldFileType <type>:" );
        System.out.println( "           How to treat worldfiles that are read. Possible values are outer and" );
        System.out.println( "           center. Center is the default." );
    }

    /**
     * @param args
     */
    public static void main( String[] args ) {
        try {
            RTUConfiguration config = new RTUConfiguration( args );
            RasterTreeUpdater updater = new RasterTreeUpdater( config );
            updater.init();
            updater.update();
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
            printUsage( "An URL is malformed." );
        } catch ( ClassCastException e ) {
            e.printStackTrace();
            printUsage( "Data is not defined in shapefiles." );
        } catch ( IOException e ) {
            e.printStackTrace();
            printUsage( "The coverage offering document can not be read:" );
        } catch ( SAXException e ) {
            e.printStackTrace();
            printUsage( "The coverage offering document is not in XML format:" );
        } catch ( InvalidCoverageDescriptionExcpetion e ) {
            e.printStackTrace();
            printUsage( "The coverage offering document is not valid:" );
        } catch ( UnknownCRSException e ) {
            e.printStackTrace();
            printUsage( "The coverage offering document is not sound:" );
        } catch ( HasNoDBaseFileException e ) {
            e.printStackTrace();
            printUsage( "A shapefile has no associated .dbf." );
        } catch ( DBaseException e ) {
            e.printStackTrace();
            printUsage( "A shapefile database is in the wrong format or has errors." );
        }

    }

    /**
     * <code>RTUConfiguration</code> is a class containing configuration options for the
     * <code>RasterTreeUpdater</code>.
     *
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     *
     * @version 2.0, $Revision$, $Date$
     *
     * @since 2.0
     */
    public static class RTUConfiguration {

        /**
         * The location of the WCS configuration document.
         */
        URL wcsConfiguration;

        /**
         * The list of image files being updated.
         */
        List<String> updatedFiles;

        /**
         * The coverage name to update.
         */
        String coverageName;

        /**
         * Whether to be verbose in logging.
         */
        boolean verbose;

        /**
         * The interpolation method to be used.
         */
        Object interpolation;

        /**
         * The bit depth for the output images.
         */
        int bitDepth;

        /**
         * Desired output image quality.
         */
        float quality;

        /**
         * Worldfile type used for reading.
         */
        WorldFile.TYPE worldfileType;

        /**
         *
         * @param wcsConfiguration
         * @param updatedFiles
         * @param coverageName
         * @param verbose
         * @param interpolation
         * @param bitDepth
         * @param quality
         * @param worldfileType
         */
        public RTUConfiguration( URL wcsConfiguration, List<String> updatedFiles, String coverageName, boolean verbose,
                                 Object interpolation, int bitDepth, float quality, WorldFile.TYPE worldfileType ) {
            this.wcsConfiguration = wcsConfiguration;
            this.updatedFiles = updatedFiles;
            this.coverageName = coverageName;
            this.verbose = verbose;
            this.interpolation = interpolation;
            this.bitDepth = bitDepth;
            this.quality = quality;
            this.worldfileType = worldfileType;
        }

        /**
         * Constructs a new instance through command line arguments.
         *
         * @param args
         *            the command line arguments
         * @throws MalformedURLException
         */
        public RTUConfiguration( String[] args ) throws MalformedURLException {

            Properties map = new Properties();
            int i = 0;
            while ( i < args.length ) {
                if ( args[i].equals( "-verbose" ) ) {
                    map.put( args[i++], "-" );
                } else {
                    map.put( args[i++], args[i++] );
                }
            }

            try {
                wcsConfiguration = new URL( map.getProperty( "-wcs" ) );
            } catch ( MalformedURLException e ) {
                wcsConfiguration = new File( map.getProperty( "-wcs" ) ).toURI().toURL();
            }

            coverageName = map.getProperty( "-name" );

            verbose = map.getProperty( "-verbose" ) != null;

            if ( map.getProperty( "-interpolation" ) != null ) {
                String t = map.getProperty( "-interpolation" );
                interpolation = RasterTreeBuilder.createInterpolation( t );
            } else {
                interpolation = RasterTreeBuilder.createInterpolation( "Nearest Neighbor" );
            }

            bitDepth = 32;
            if ( map.getProperty( "-depth" ) != null ) {
                bitDepth = Integer.parseInt( map.getProperty( "-depth" ) );
            }

            quality = 0.95f;
            if ( map.getProperty( "-quality" ) != null ) {
                quality = Float.parseFloat( map.getProperty( "-quality" ) );
            }

            worldfileType = WorldFile.TYPE.CENTER;
            if ( map.getProperty( "-worldFileType" ) != null ) {
                if ( map.getProperty( "-worldFileType" ).equalsIgnoreCase( "outer" ) ) {
                    worldfileType = WorldFile.TYPE.OUTER;
                }
            }

            updatedFiles = StringTools.toList( map.getProperty( "-mapFiles" ), ",;", true );
        }

        /**
         * @return true, if the configuration values are sound
         */
        public boolean isValidConfiguration() {
            return updatedFiles.size() > 0 && wcsConfiguration != null;
        }

    }

}
