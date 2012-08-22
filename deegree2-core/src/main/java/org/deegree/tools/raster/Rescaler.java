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
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ConvenienceFileFilter;
import org.deegree.framework.util.ImageUtils;
import org.deegree.io.dbaseapi.DBaseFile;
import org.deegree.model.coverage.grid.WorldFile;
import org.deegree.ogcbase.CommonNamespaces;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Rescaler {

    private static final ILogger LOG = LoggerFactory.getLogger( Rescaler.class );

    private static final URI DEEGREEAPP = CommonNamespaces.buildNSURI( "http://www.deegree.org/app" );

    private static final String APP_PREFIX = "app";

    private String outDir;

    private String format;

    private Interpolation interpolation = new InterpolationBilinear();

    private List<String> fileList;

    private double resolution = 0;

    /**
     *
     * @param resolution
     * @param rootDir
     * @param outDir
     * @param format
     * @param subDirs
     */
    Rescaler( double resolution, String rootDir, String outDir, String format, boolean subDirs ) {
        this.resolution = resolution;
        this.outDir = outDir;
        this.format = format;
        fileList = getFileList( rootDir, subDirs );
    }

    /**
     *
     * @param resolution
     * @param rootDir
     * @param outDir
     * @param dBase
     * @param format
     * @param fileColumn
     * @throws Exception
     */
    Rescaler( double resolution, String rootDir, String outDir, String dBase, String format, String fileColumn )
                            throws Exception {
        this.resolution = resolution;
        this.outDir = outDir;
        this.format = format;
        fileList = getFileList( dBase, fileColumn, rootDir );
    }

    /**
     * returns the list of image map files to consider read from a dbase file defined by the dbase parameter
     *
     * @param dbaseFile
     *            name of the dbase file
     * @param fileColumn
     *            name of the column containing the image map files names
     * @param baseDir
     *            name of the directory where the image map files are stored if this parameter is <code>null</code> it
     *            is assumed that the image map files are full referenced within the dbase
     * @return the list of image map files to consider read from a dbase file defined by the dbase parameter
     * @throws Exception
     */
    private static List<String> getFileList( String dBaseFile, String fileColumn, String baseDir )
                            throws Exception {

        // handle dbase file extension and file location/reading problems
        if ( dBaseFile.endsWith( ".dbf" ) ) {
            dBaseFile = dBaseFile.substring( 0, dBaseFile.lastIndexOf( "." ) );
        }
        DBaseFile dbf = new DBaseFile( dBaseFile );

        // sort dbase file contents chronologicaly (oldest first)
        int cnt = dbf.getRecordNum();

        Object[] mapItems = new Object[cnt];
        QualifiedName fileC = new QualifiedName( APP_PREFIX, fileColumn.toUpperCase(), DEEGREEAPP );

        for ( int i = 0; i < cnt; i++ ) {
            // name of map file
            mapItems[i] = dbf.getFRow( i + 1 ).getDefaultProperty( fileC ).getValue();
        }

        // extract names of image files from dBase file and attach them to rootDir
        if ( baseDir == null ) {
            baseDir = "";
        } else if ( !baseDir.endsWith( "/" ) && !baseDir.endsWith( "\\" ) ) {
            baseDir = baseDir + '/';
        }
        List<String> imageFiles = new ArrayList<String>( mapItems.length );
        for ( int i = 0; i < mapItems.length; i++ ) {
            if ( mapItems[i] != null ) {
                imageFiles.add( baseDir + mapItems[i] );
            }
        }

        return imageFiles;
    }

    /**
     * returns the list of image map files to consider read from a defined root directory.
     *
     * @param rootDir
     *            root directory where to read image map files
     * @param subdirs
     *            true if subdirectories of the root directory shall be parsed for image maps too
     * @return the list of image map files to consider read from a defined root directory.
     */
    private static List<String> getFileList( String rootDir, boolean subdirs ) {
        List<String> list = new ArrayList<String>( 10000 );
        File file = new File( rootDir );
        List<String> extensions = new ArrayList<String>();
        extensions.add( "JPEG" );
        extensions.add( "JPG" );
        extensions.add( "BMP" );
        extensions.add( "PNG" );
        extensions.add( "GIF" );
        extensions.add( "TIF" );
        extensions.add( "TIFF" );
        extensions.add( "GEOTIFF" );
        ConvenienceFileFilter cff = new ConvenienceFileFilter( extensions, true );
        String[] entries = file.list( cff );
        if ( entries != null ) {
            for ( int i = 0; i < entries.length; i++ ) {
                File entry = new File( rootDir + '/' + entries[i] );
                if ( entry.isDirectory() && subdirs ) {
                    list = readSubDirs( entry, list, cff );
                } else {
                    list.add( rootDir + '/' + entries[i] );
                }
            }
        }
        return list;
    }

    /**
     *
     * @param file
     * @param list
     * @return a list of strings
     */
    private static List<String> readSubDirs( File file, List<String> list, ConvenienceFileFilter cff ) {

        String[] entries = file.list( cff );
        if ( entries != null ) {
            for ( int i = 0; i < entries.length; i++ ) {
                File entry = new File( file.getAbsolutePath() + '/' + entries[i] );
                if ( entry.isDirectory() ) {
                    list = readSubDirs( entry, list, cff );
                } else {
                    list.add( file.getAbsolutePath() + '/' + entries[i] );
                }
            }
        }
        return list;
    }

    /**
     *
     * @throws Exception
     */
    public void process()
                            throws Exception {
        for ( int i = 0; i < fileList.size(); i++ ) {
            System.out.print( fileList.get( i ) + "\r" );
            File file = new File( fileList.get( i ) );
            BufferedImage image = ImageUtils.loadImage( file );
            WorldFile wf = WorldFile.readWorldFile( fileList.get( i ), WorldFile.TYPE.CENTER, image.getWidth(),
                                                    image.getHeight() );
            float qx = (float) ( wf.getResx() / resolution );
            float qy = (float) ( wf.getResy() / resolution );

            ParameterBlock pb = new ParameterBlock();
            pb.addSource( image );
            pb.add( qx ); // The xScale
            pb.add( qy ); // The yScale
            pb.add( 0.0F ); // The x translation
            pb.add( 0.0F ); // The y translation
            pb.add( interpolation ); // The interpolation
            // Create the scale operation
            RenderedOp ro = JAI.create( "scale", pb, null );
            try {
                image = ro.getAsBufferedImage();
            } catch ( Exception e ) {
                e.printStackTrace();
            }

            wf = new WorldFile( resolution, resolution, 0, 0, wf.getEnvelope() );
            int p = file.getName().lastIndexOf( '.' );
            String fileBaseName = file.getName().substring( 0, p );
            ImageUtils.saveImage( image, outDir + fileBaseName + '.' + format, 1 );
            WorldFile.writeWorldFile( wf, outDir + fileBaseName );
            System.gc();
        }
    }

    private static void printHelp() {
        System.out.println();
        System.out.println( "Parameter description for RasterSplitter:" );
        System.out.println( "-res : desired raster resolution" );
        System.out.println( "-format : desired image format of result images (mandatory)" );
        System.out.println( "-outDir : directory where result images shall be stored (mandatory)" );

        System.out.println( "-rootDir : directory from where images to split will be read (mandatory)" );
        System.out.println( "-subDirs : (true|false). If 'true' all sub directories of the 'rootDir' " );
        System.out.println( "            will be searched for images too (optional; default = false)" );
    }

    private static boolean validate( Properties map ) {
        if ( map.getProperty( "-res" ) == null ) {
            System.out.println( "-res must be set!" );
            return false;
        }
        if ( map.getProperty( "-format" ) == null ) {
            System.out.println( "-format must be set!" );
            return false;
        }
        if ( map.getProperty( "-outDir" ) == null ) {
            System.out.println( "-outDir must be set!" );
            return false;
        }
        if ( map.getProperty( "-rootDir" ) == null ) {
            System.out.println( "-rootDir must be set!" );
            return false;
        }
        if ( map.getProperty( "-subDirs" ) != null && !"true".equals( map.getProperty( "-subDirs" ) )
             && !"false".equals( map.getProperty( "-subDirs" ) ) ) {
            System.out.println( "if -subDirs is set it must be true or false!" );
            return false;
        }
        return true;
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main( String[] args )
                            throws Exception {
        Properties map = new Properties();
        for ( int i = 0; i < args.length; i += 2 ) {
            map.put( args[i], args[i + 1] );
        }
        if ( !validate( map ) ) {
            printHelp();
            return;
        }

        String format = map.getProperty( "-format" );
        String outDir = map.getProperty( "-outDir" );
        double res = Double.parseDouble( map.getProperty( "-res" ) );
        Rescaler rescaler = null;
        if ( map.get( "-dbaseFile" ) != null ) {
            String dBaseFile = map.getProperty( "-dbaseFile" );
            String fileColum = map.getProperty( "-fileColumn" );
            String baseDir = map.getProperty( "-baseDir" );
            if ( baseDir == null ) {
                baseDir = map.getProperty( "-rootDir" );
            }
            rescaler = new Rescaler( res, baseDir, outDir, dBaseFile, format, fileColum );
        } else if ( map.get( "-rootDir" ) != null ) {
            String rootDir = map.getProperty( "-rootDir" );
            boolean subDirs = "true".equals( map.get( "-subDirs" ) );
            rescaler = new Rescaler( res, rootDir, outDir, format, subDirs );
        } else {
            LOG.logInfo( map.toString() );
            System.out.println( "-rootDir or -dbaseFile parameter must be defined" );
            return;
        }
        rescaler.process();

    }

}
