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

import static java.io.File.separator;
import static java.util.Arrays.sort;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.media.jai.BorderExtender;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.graphics.transformation.WorldToScreenTransform;
import org.deegree.io.dbaseapi.DBaseFile;
import org.deegree.io.quadtree.IndexException;
import org.deegree.io.rtree.HyperBoundingBox;
import org.deegree.io.rtree.HyperPoint;
import org.deegree.io.rtree.RTree;
import org.deegree.io.shpapi.ShapeFile;
import org.deegree.model.coverage.grid.GridCoverageExchange;
import org.deegree.model.coverage.grid.WorldFile;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.processing.raster.converter.Image2RawData;
import org.deegree.processing.raster.converter.RawData2Image;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

/**
 * This class represents a <code>RasterTreeBuilder</code> object.<br>
 * It wcan be used to create a resolution pyramid from one or more already existing raster dataset (image). The
 * resulting pyramid will be described by a set of shapes (containing the image tiles bounding boxes) and a XML coverage
 * description document that can be used with the deegree WCS. The RTB supports real images like png, tif, jpeg, bmp and
 * gif as well as raw data image like 16Bit and 32Bit tif-images without color model. <br>
 * because of the large amount of data that may be process by the RTB it makes use of a caching mechnism. For this the
 * ehcache project is used. One can configure the cache behavior by placing a file named ehcache.xml defining a cache
 * named 'imgCache' within the class root when starting the RTB. (For details please see the ehcache documentation). If
 * no ehcache.xml is available default cache configuration will be used which is set to:
 * <ul>
 * <li>maxElementsInMemory = 10
 * <li>memoryStoreEvictionPolicy = LFU
 * <li>overflowToDisk = false (notice that overflow to disk is not supported because cached objects are not
 * serializable)
 * <li>eternal = false
 * <li>timeToLiveSeconds = 3600
 * <li>timeToIdleSeconds = 3600
 * </ul>
 * 
 * 
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 2.0, $Revision$, $Date$
 * 
 * @since 2.0
 */
public class RasterTreeBuilderIndexed {

    private static final ILogger LOG = LoggerFactory.getLogger( RasterTreeBuilderIndexed.class );

    private static final URI DEEGREEAPP = CommonNamespaces.buildNSURI( "http://www.deegree.org/app" );

    private static final String APP_PREFIX = "app";

    // templates and transformation scripts
    private URL configURL = RasterTreeBuilder.class.getResource( "template_wcs_configuration.xml" );

    private URL configXSL = RasterTreeBuilder.class.getResource( "updateConfig.xsl" );

    private URL inputXSL = RasterTreeBuilder.class.getResource( "updateCapabilities.xsl" );

    private int bitDepth = 16;

    // input for new MergeRaste object
    private List<String> imageFiles;

    private List<WorldFile> imageFilesEnvs;

    private Map<String, String> imageFilesErrors;

    private String outputDir;

    private String baseName;

    private String outputFormat;

    private double maxTileSize;

    private String srs = null;

    private Object interpolation = null;

    private WorldFile.TYPE worldFileType = null;

    private float quality = 0;

    private String bgColor = null;

    private float offset = 0;

    private float scaleFactor = 1;

    // minimum resolution of input images
    private double minimumRes;

    // combining image bounding box
    private Envelope combiningEnvelope;

    // size of virtual bounding box in px
    private double pxWidthVirtualBBox;

    private double pxHeightVirtualBBox;

    // size of every tile in virtual bounding box in px
    private long pxWidthTile;

    private long pxHeightTile;

    // number of tiles in virtual bounding box
    private int tileRows;

    private int tileCols;

    private FeatureType ftype = null;

    private FeatureCollection fc = null;

    private Cache imgCache;

    private boolean dummy;

    private RTree rTree;

    /**
     * @param imageFiles
     * @param outputDir
     * @param baseName
     * @param outputFormat
     * @param maxTileSize
     * @param srs
     * @param interpolation
     * @param worldFileType
     * @param quality
     * @param bgColor
     * @param depth
     * @param resolution
     * @param offset
     * @param scaleFactor
     * @param dummy
     */
    public RasterTreeBuilderIndexed( List<String> imageFiles, String outputDir, String baseName, String outputFormat,
                                     double maxTileSize, String srs, String interpolation,
                                     WorldFile.TYPE worldFileType, float quality, String bgColor, int depth,
                                     double resolution, float offset, float scaleFactor, boolean dummy ) {
        this( imageFiles, outputDir, baseName, outputFormat, maxTileSize, srs, interpolation, worldFileType, quality,
              bgColor, depth, resolution, offset, scaleFactor );
        this.dummy = dummy;
    }

    /**
     * 
     * @param imageFiles
     * @param outputDir
     * @param baseName
     * @param outputFormat
     * @param maxTileSize
     * @param srs
     * @param interpolation
     * @param worldFileType
     * @param quality
     * @param bgColor
     * @param depth
     * @param resolution
     * @param offset
     * @param scaleFactor
     */
    public RasterTreeBuilderIndexed( List<String> imageFiles, String outputDir, String baseName, String outputFormat,
                                     double maxTileSize, String srs, String interpolation,
                                     WorldFile.TYPE worldFileType, float quality, String bgColor, int depth,
                                     double resolution, float offset, float scaleFactor ) {

        this.imageFiles = imageFiles;
        this.imageFilesErrors = new HashMap<String, String>( imageFiles.size() );
        this.imageFilesEnvs = new ArrayList<WorldFile>( imageFiles.size() );
        for ( int i = 0; i < imageFiles.size(); i++ ) {
            this.imageFilesEnvs.add( null );
        }
        this.outputDir = outputDir;
        File dir = new File( outputDir ).getAbsoluteFile();
        if ( !dir.exists() ) {
            dir.mkdir();
        }
        this.baseName = baseName;
        this.outputFormat = outputFormat.toLowerCase();
        this.maxTileSize = maxTileSize;
        this.srs = srs;
        this.interpolation = createInterpolation( interpolation );
        this.worldFileType = worldFileType;
        this.quality = quality;
        this.bgColor = bgColor;
        if ( depth != 0 ) {
            this.bitDepth = depth;
        }
        this.minimumRes = resolution;
        this.offset = offset;
        this.scaleFactor = scaleFactor;

        CacheManager singletonManager = CacheManager.create();
        if ( singletonManager.getCache( "imgCache" ) == null ) {
            Cache cache = new Cache( "imgCache", 10, MemoryStoreEvictionPolicy.LFU, false, ".", false, 3600, 3600,
                                     false, 240, null );
            singletonManager.addCache( cache );
            imgCache = singletonManager.getCache( "imgCache" );
        } else {
            imgCache = singletonManager.getCache( "imgCache" );
            try {
                imgCache.removeAll();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        PropertyType[] ftp = new PropertyType[3];
        ftp[0] = FeatureFactory.createSimplePropertyType( new QualifiedName( "GEOM" ), Types.GEOMETRY, false );
        ftp[1] = FeatureFactory.createSimplePropertyType(
                                                          new QualifiedName( GridCoverageExchange.SHAPE_IMAGE_FILENAME ),
                                                          Types.VARCHAR, false );
        ftp[2] = FeatureFactory.createSimplePropertyType( new QualifiedName( GridCoverageExchange.SHAPE_DIR_NAME ),
                                                          Types.VARCHAR, false );
        ftype = FeatureFactory.createFeatureType( new QualifiedName( "tiles" ), false, ftp );
    }

    /**
     * @throws IOException
     */
    public void logCollectedErrors()
                            throws IOException {
        FileOutputStream fos = new FileOutputStream( "RasterTreeBuilder" + minimumRes + ".log" );
        PrintWriter pw = new PrintWriter( fos );
        pw.println( "processing the following files caused an error" );
        Iterator<String> iter = imageFilesErrors.keySet().iterator();
        while ( iter.hasNext() ) {
            String key = iter.next();
            String value = imageFilesErrors.get( key );
            pw.print( key );
            pw.print( ": " );
            pw.println( value );
        }
        pw.close();
        LOG.logInfo( "LOG file RasterTreeBuilder.log has been written" );
    }

    /**
     * starts creating of a raster tile level using the current bbox and resolution
     * 
     * @throws Exception
     */
    public void start()
                            throws Exception {
        System.gc();
        fc = FeatureFactory.createFeatureCollection( Double.toString( minimumRes ), tileRows * tileCols );
        createTiles( tileRows, tileCols );

        LOG.logInfo( "creating shape for georeferencing ... " );
        ShapeFile sf = new ShapeFile( outputDir + "/sh" + minimumRes, "rw" );
        sf.writeShape( fc );
        sf.close();

    }

    /**
     * @param env
     * @param resolution
     * @throws IndexException
     */
    public void init( Envelope env, double resolution )
                            throws Exception {

        rTree = new RTree( 2, imageFiles.size() );
        for ( int i = 0; i < imageFiles.size(); i++ ) {

            File file = new File( imageFiles.get( i ) );
            if ( file.exists() && !file.isDirectory() ) {
                FileSeekableStream fss = new FileSeekableStream( imageFiles.get( i ) );
                RenderedOp rop = JAI.create( "stream", fss );
                int iw = ( (Integer) rop.getProperty( "image_width" ) ).intValue();
                int ih = ( (Integer) rop.getProperty( "image_height" ) ).intValue();
                fss.close();

                WorldFile wf = null;
                try {
                    wf = WorldFile.readWorldFile( imageFiles.get( i ), worldFileType, iw, ih );
                } catch ( Exception e ) {
                    LOG.logError( e.getMessage() );
                    continue;
                }
                imageFilesEnvs.set( i, wf );
            }
        }

        for ( int i = 0; i < imageFiles.size(); i++ ) {
            if ( imageFilesEnvs.get( i ) != null ) {
                Envelope envelope = imageFilesEnvs.get( i ).getEnvelope();
                Position pp = envelope.getMin();
                HyperPoint min = new HyperPoint( new double[] { pp.getX(), pp.getY() } );
                pp = envelope.getMax();
                HyperPoint max = new HyperPoint( new double[] { pp.getX(), pp.getY() } );
                HyperBoundingBox hbb = new HyperBoundingBox( min, max );
                rTree.insert( i, hbb );
            }
        }

        // set target envelope
        setEnvelope( env );
        setResolution( resolution );
        determineVirtualBBox();
        determineTileSize();
    }

    /**
     * sets the resolution level to be used for tiling
     * 
     * @param resolution
     */
    public void setResolution( double resolution ) {
        minimumRes = resolution;
    }

    /**
     * sets the bounding box used for tiling
     * 
     * @param bbox
     */
    public void setEnvelope( Envelope bbox ) {
        combiningEnvelope = bbox;
    }

    /**
     * TODO this is a copy from org.deegree.tools.raster#AutoTiler
     * 
     * loads the base image
     * 
     * @throws IOException
     */
    private RenderedOp loadImage( String imageSource )
                            throws IOException {

        File f = new File( imageSource );
        InputStream is = f.toURL().openStream();
        SeekableStream fss = new MemoryCacheSeekableStream( is );

        return JAI.create( "stream", fss );
    }

    /**
     * Determins the necessary size of a bounding box, which is large enough to hold all input image files. The result
     * is stored in the combining <code>Envelope</code>.
     * 
     * @throws Exception
     */
    private WorldFile determineCombiningBBox()
                            throws Exception {

        System.out.println( "calculating overall bounding box ..." );

        if ( imageFiles == null || imageFiles.isEmpty() ) {
            throw new Exception( "No combining BoundingBox to be determined: "
                                 + "The list of image files is null or empty." );
        }

        WorldFile wf1 = null;
        if ( combiningEnvelope == null ) {

            // upper left corner of combining bounding box
            double minX = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;
            // lower right corner of combining bounding box
            double maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE;
            // minimum resolution within combining bounding box
            double minResX = Double.MAX_VALUE;
            double minResY = Double.MAX_VALUE;

            for ( int i = 0; i < imageFiles.size(); i++ ) {

                File file = new File( imageFiles.get( i ) );
                if ( file.exists() && !file.isDirectory() ) {
                    System.out.println( imageFiles.get( i ) );
                    FileSeekableStream fss = new FileSeekableStream( imageFiles.get( i ) );
                    RenderedOp rop = JAI.create( "stream", fss );
                    int iw = ( (Integer) rop.getProperty( "image_width" ) ).intValue();
                    int ih = ( (Integer) rop.getProperty( "image_height" ) ).intValue();
                    fss.close();

                    WorldFile wf = null;

                    try {
                        wf = WorldFile.readWorldFile( imageFiles.get( i ), worldFileType, iw, ih );
                    } catch ( Exception e ) {
                        LOG.logError( e.getMessage() );
                        continue;
                    }
                    imageFilesEnvs.set( i, wf );
                    // now the values of resx, resy, envelope of the current image
                    // (read from the world file) file are available

                    // find min for x and y
                    minX = Math.min( minX, wf.getEnvelope().getMin().getX() );
                    minY = Math.min( minY, wf.getEnvelope().getMin().getY() );
                    // find max for x and y
                    maxX = Math.max( maxX, wf.getEnvelope().getMax().getX() );
                    maxY = Math.max( maxY, wf.getEnvelope().getMax().getY() );

                    // find min for resolution of x and y
                    minResX = Math.min( minResX, wf.getResx() );
                    minResY = Math.min( minResY, wf.getResy() );
                } else {
                    System.out.println( "File: " + imageFiles.get( i ) + " does not exist!" );
                    System.out.println( "Image will be ignored" );
                }
                if ( i % 10 == 0 ) {
                    System.gc();
                }

            }
            // store minimum resolution
            if ( minimumRes <= 0 ) {
                minimumRes = Math.min( minResX, minResY );
            }
            combiningEnvelope = GeometryFactory.createEnvelope( minX, minY, maxX, maxY, null );
            LOG.logInfo( "determined envelope: ", combiningEnvelope );
        }
        wf1 = new WorldFile( minimumRes, minimumRes, 0, 0, combiningEnvelope );
        return wf1;
    }

    /**
     * Determins a usefull size for the virtual bounding box. It is somewhat larger than the combining bounding box. The
     * result is stored in the virtual <code>Envelope</code>.
     * 
     */
    private Envelope determineVirtualBBox() {

        double width = combiningEnvelope.getWidth();
        double height = combiningEnvelope.getHeight();

        // set width and height to next higher even-numbered thousand
        double pxWidth = ( width / minimumRes ) + 1;
        double pxHeight = ( height / minimumRes ) + 1;

        pxWidthVirtualBBox = pxWidth;
        pxHeightVirtualBBox = pxHeight;

        // lower right corner of virtual bounding box

        WorldFile wf = new WorldFile( minimumRes, minimumRes, 0, 0, combiningEnvelope );
        // upper left corner of virtual bounding box
        double minX = combiningEnvelope.getMin().getX();
        double maxY = combiningEnvelope.getMax().getY();

        double maxX = minX + ( ( pxWidth - 1 ) * wf.getResx() );
        double minY = maxY - ( ( pxHeight - 1 ) * wf.getResx() );

        return GeometryFactory.createEnvelope( minX, minY, maxX, maxY, null );

        // return combiningEnvelope;
    }

    /**
     * This method determins and sets the size of the tiles in pixel both horizontally (pxWidthTile) and vertically
     * (pxHeightTile). It also sets the necessary number of <code>tileCols</code> (depending on the tileWidth) and
     * <code>tileRows</code> (depending on the tileHeight).
     * 
     * By default, all tiles have a size of close to but less than 6000 pixel either way.
     */
    private void determineTileSize() {
        /*
         * The size of the virtual bbox gets divided by maxTileSize to find an approximat number of tiles (a).
         * 
         * If the virtual bbox is in any direction (horizontally or vertically) smaler than maxTileSize px, then it has
         * only 1 tile in that direction. In this case, the size of the tile equals the size of the virtual bbox.
         * 
         * Otherwise, divide the size of the pixel size of virtual bbox by the pixel tile size
         */
        // determin width of tile
        double a = ( pxWidthVirtualBBox / maxTileSize );
        int tileCols = (int) Math.ceil( a );
        if ( a <= 1.0 ) {
            pxWidthTile = Math.round( pxWidthVirtualBBox );
        } else {
            tileCols = (int) Math.round( ( pxWidthVirtualBBox / ( maxTileSize - 1 ) ) + 1 );
            pxWidthTile = (int) Math.round( maxTileSize );
        }

        // determin height of tile
        a = ( pxHeightVirtualBBox / maxTileSize );
        int tileRows = (int) Math.ceil( a );
        if ( a <= 1.0 ) {
            pxHeightTile = Math.round( pxHeightVirtualBBox );
        } else {
            tileRows = (int) Math.round( ( pxHeightVirtualBBox / ( maxTileSize - 1 ) ) + 1 );
            pxHeightTile = (int) Math.round( maxTileSize );
        }

        this.tileCols = tileCols;
        this.tileRows = tileRows;

        LOG.logInfo( "minimum resolution: " + minimumRes );
        LOG.logInfo( "width = " + pxWidthVirtualBBox + " *** height = " + pxHeightVirtualBBox );
        LOG.logInfo( "pxWidthTile = " + pxWidthTile + " *** pxHeightTile = " + pxHeightTile );
        LOG.logInfo( "number of tiles: horizontally = " + tileCols + ", vertically = " + tileRows );
    }

    /**
     * Creates one <code>Tile</code> object after the other, with the number of tiles being specified by the given
     * number of <code>rows</code> and <code>cols</code>.
     * 
     * Each Tile gets written to the FileOutputStream by the internal call to #paintImagesOnTile.
     * 
     * @param rows
     * @param cols
     * @throws Exception
     */
    private void createTiles( int rows, int cols )
                            throws Exception {

        System.out.println( "creating merged image ..." );

        Envelope virtualEnv = determineVirtualBBox();

        double tileWidth = minimumRes * ( pxWidthTile );
        double tileHeight = minimumRes * ( pxHeightTile );

        double upperY = virtualEnv.getMax().getY();

        File file = new File( outputDir + '/' + Double.toString( minimumRes ) ).getAbsoluteFile();
        file.mkdir();

        for ( int i = 0; i < rows; i++ ) {
            System.out.println( "processing row " + i );
            double leftX = virtualEnv.getMin().getX();
            double lowerY = upperY - tileHeight;
            for ( int j = 0; j < cols; j++ ) {
                System.out.println( "processing tile: " + i + " - " + j );
                double rightX = leftX + tileWidth;
                Envelope env = GeometryFactory.createEnvelope( leftX, lowerY, rightX, upperY, null );
                leftX = rightX;
                String postfix = "_" + i + "_" + j;
                Tile tile = new Tile( env, postfix );

                paintImagesOnTile( tile );
                System.gc();
            }
            upperY = lowerY;
        }
        System.gc();

    }

    /**
     * Paints all image files that intersect with the passed <code>tile</code> onto that tile and creates an output file
     * in the <code>outputDir</code>. If no image file intersects with the given tile, then an empty output file is
     * created. The name of the output file is defined by the <code>baseName</code> and the tile's index of row and
     * column.
     * 
     * @param tile
     *            The tile on which to paint the image.
     * @throws Exception
     */
    private void paintImagesOnTile( Tile tile )
                            throws Exception {

        Envelope tileEnv = tile.getTileEnvelope();
        String postfix = tile.getPostfix();

        BufferedImage out = createOutputImage();
        float[][] data = null;
        if ( bitDepth == 16 && "raw".equals( outputFormat ) ) {
            // do not use image api if target bitDepth = 16
            data = new float[(int) pxHeightTile][(int) pxWidthTile];
        }

        if ( bgColor != null ) {
            Graphics g = out.getGraphics();
            g.setColor( Color.decode( bgColor ) );
            g.fillRect( 0, 0, out.getWidth(), out.getHeight() );
            g.dispose();
        }
        boolean paint = false;
        int gcc = 0;

        if ( dummy ) {
            paint = true;
        } else {

            Position p = tileEnv.getMin();
            HyperPoint min = new HyperPoint( new double[] { p.getX(), p.getY() } );
            p = tileEnv.getMax();
            HyperPoint max = new HyperPoint( new double[] { p.getX(), p.getY() } );
            HyperBoundingBox hbb = new HyperBoundingBox( min, max );
            Object[] obj = rTree.intersects( hbb );
            for ( int i = 0; i < obj.length; i++ ) {
                File file = new File( imageFiles.get( ( (Integer) obj[i] ).intValue() ) );
                if ( imageFilesErrors.get( imageFiles.get( ( (Integer) obj[i] ).intValue() ) ) == null && file.exists()
                     && !file.isDirectory() ) {
                    // now the values of resx, resy, envelope of the current image file are available
                    RenderedOp result = loadImage( imageFiles.get( ( (Integer) obj[i] ).intValue() ) );

                    gcc++;
                    try {
                        System.out.println( "drawImage" );
                        drawImage( out, data, result, tile, imageFilesEnvs.get( ( (Integer) obj[i] ).intValue() ),
                                   minimumRes, interpolation, imageFilesErrors, outputFormat, bitDepth, offset,
                                   scaleFactor );
                        paint = true;
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        imageFilesErrors.put( imageFiles.get( i ), e.getMessage() );
                    }
                    if ( gcc % 5 == 0 ) {
                        System.out.println( "garbage collecting" );
                        System.gc();
                    }
                } else {
                    imageFilesErrors.put( imageFiles.get( ( (Integer) obj[i] ).intValue() ), "image does not exist!" );
                }
            }
        }
        if ( paint ) {
            if ( !isTransparent( out ) ) {
                // just write files if something has been painted
                if ( bitDepth == 16 && "raw".equals( outputFormat ) ) {
                    out = RawData2Image.rawData2Image( data, false, scaleFactor, offset );
                }
                storeTileImageToFileSystem( postfix, out );
                createWorldFile( tile );
                String frm = outputFormat;
                if ( "raw".equals( outputFormat ) ) {
                    frm = "tif";
                }
                storeEnvelope( Double.toString( minimumRes ), baseName + postfix + '.' + frm, tileEnv );
            }
        }
    }

    /**
     * creates an instance of a BufferedImage depending on requested target format
     * 
     * @return the new image
     */
    private BufferedImage createOutputImage() {

        BufferedImage out = null;
        if ( "jpg".equals( outputFormat ) || "jpeg".equals( outputFormat ) || "bmp".equals( outputFormat ) ) {
            // for bmp, jpg, jpeg use 3 byte:
            out = new BufferedImage( (int) pxWidthTile, (int) pxHeightTile, BufferedImage.TYPE_INT_RGB );
        } else if ( "tif".equals( outputFormat ) || "tiff".equals( outputFormat ) || "png".equals( outputFormat )
                    || "gif".equals( outputFormat ) ) {
            // for tif, tiff and png use 4 byte:
            out = new BufferedImage( (int) pxWidthTile, (int) pxHeightTile, BufferedImage.TYPE_INT_ARGB );
        } else {
            ColorModel ccm;

            if ( bitDepth == 16 ) {
                ccm = new ComponentColorModel( ColorSpace.getInstance( ColorSpace.CS_GRAY ), null, false, false,
                                               BufferedImage.OPAQUE, DataBuffer.TYPE_USHORT );
                WritableRaster wr = ccm.createCompatibleWritableRaster( (int) pxWidthTile, (int) pxHeightTile );

                out = new BufferedImage( ccm, wr, false, new Hashtable<Object, Object>() );
            } else {
                out = new BufferedImage( (int) pxWidthTile, (int) pxHeightTile, BufferedImage.TYPE_INT_ARGB );
            }
        }

        return out;

    }

    /**
     * 
     * @param postfix
     *            tile name postfix ( -> tile index $x_$y )
     * @param out
     *            tile image to save
     */
    private void storeTileImageToFileSystem( String postfix, BufferedImage out ) {
        try {
            String frm = outputFormat;
            if ( "raw".equals( frm ) ) {
                frm = "tif";
            }
            String imageFile = outputDir + '/' + Double.toString( minimumRes ) + '/' + baseName + postfix + '.' + frm;
            File file = new File( imageFile ).getAbsoluteFile();

            ImageUtils.saveImage( out, file, quality );

        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Draws an image map to the target tile considering defined interpolation method for rescaling. This method is
     * static so it can be used easily from the <code>RasterTreeUpdater</code>.
     * 
     * @param out
     *            target image tile
     * @param data
     * @param image
     *            source image map
     * @param tile
     *            tile description, must contain the envelope of the target image
     * @param wf
     *            must contain the envelope of the TiledImage of the source image
     * @param minimumRes
     *            the minimum resolution of input images
     * @param interpolation
     *            the interpolation method
     * @param imageFilesErrors
     *            a mapping between image files and errors
     * @param outputFormat
     *            the output format
     * @param bitDepth
     *            the output bit depth
     * @param offset
     *            offset used if bitDepth = 16 and outputFormat = raw
     * @param scaleFactor
     *            scale factor used if bitDepth = 16 and outputFormat = raw
     */
    public static void drawImage( BufferedImage out, float[][] data, final RenderedOp image, Tile tile, WorldFile wf,
                                  double minimumRes, Object interpolation, Map<String, String> imageFilesErrors,
                                  String outputFormat, int bitDepth, float offset, float scaleFactor ) {

        Envelope tileEnv = tile.getTileEnvelope();
        Envelope mapEnv = wf.getEnvelope();

        GeoTransform gt2 = new WorldToScreenTransform( mapEnv.getMin().getX(), mapEnv.getMin().getY(),
                                                       mapEnv.getMax().getX(), mapEnv.getMax().getY(), 0, 0,
                                                       image.getWidth() - 1, image.getHeight() - 1 );

        Envelope inter = mapEnv.createIntersection( tileEnv );
        if ( inter == null )
            return;
        int x1 = (int) Math.round( gt2.getDestX( inter.getMin().getX() ) );
        int y1 = (int) Math.round( gt2.getDestY( inter.getMax().getY() ) );
        int x2 = (int) Math.round( gt2.getDestX( inter.getMax().getX() ) );
        int y2 = (int) Math.round( gt2.getDestY( inter.getMin().getY() ) );

        if ( x2 - x1 >= 0 && y2 - y1 >= 0 && x1 + x2 - x1 < image.getWidth() && y1 + y2 - y1 < image.getHeight()
             && x1 >= 0 && y1 >= 0 ) {

            BufferedImage newImg = null;
            int w = x2 - x1 + 1;
            int h = y2 - y1 + 1;
            // System.out.println( x1 + " " + y1 + " " + w + " " + h + " " + image.getWidth() + " "
            // + image.getHeight() );
            WritableRaster jpgRaster = image.getColorModel().createCompatibleWritableRaster( w, h ).createWritableTranslatedChild(
                                                                                                                                   x1,
                                                                                                                                   y2 );
            image.copyExtendedData( jpgRaster, BorderExtender.createInstance( BorderExtender.BORDER_ZERO ) );
            BufferedImage img = new BufferedImage( image.getColorModel(), jpgRaster.getWritableParent(), false, null );

            if ( !isTransparent( img ) ) {

                // copy source image to a 4 Byte BufferedImage because there are
                // problems with handling 8 Bit palette images
                if ( img.getColorModel().getPixelSize() == 18 ) {
                    LOG.logInfo( "copy 8Bit image to 32Bit image" );
                    BufferedImage bi = new BufferedImage( img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB );

                    Graphics g = bi.getGraphics();
                    try {
                        g.drawImage( img, 0, 0, null );
                    } catch ( Exception e ) {
                        System.out.println( e.getMessage() );
                    }
                    g.dispose();
                    img = bi;
                }
                if ( ( wf.getResx() / minimumRes < 0.9999 ) || ( wf.getResx() / minimumRes > 1.0001 )
                     || ( wf.getResy() / minimumRes < 0.9999 ) || ( wf.getResy() / minimumRes > 1.0001 ) ) {
                    double dx = wf.getResx() / minimumRes;
                    double dy = wf.getResy() / minimumRes;
                    int destWidth = (int) Math.round( img.getWidth() * dx );
                    int destHeight = (int) Math.round( img.getHeight() * dy );
                    newImg = resize( img, destWidth, destHeight, interpolation );
                } else {
                    newImg = img;
                }
                GeoTransform gt = new WorldToScreenTransform( tileEnv.getMin().getX(), tileEnv.getMin().getY(),
                                                              tileEnv.getMax().getX(), tileEnv.getMax().getY(), 0, 0,
                                                              out.getWidth() - 1, out.getHeight() - 1 );

                x1 = (int) Math.round( gt.getDestX( inter.getMin().getX() ) );
                y1 = (int) Math.round( gt.getDestY( inter.getMax().getY() ) );
                x2 = (int) Math.round( gt.getDestX( inter.getMax().getX() ) );
                y2 = (int) Math.round( gt.getDestY( inter.getMin().getY() ) );

                if ( x2 - x1 > 0 && y2 - y1 > 0 ) {
                    // ensure that there is something to draw
                    try {
                        if ( "raw".equals( outputFormat ) ) {
                            DataBuffer outBuffer = out.getData().getDataBuffer();
                            DataBuffer newImgBuffer = newImg.getData().getDataBuffer();
                            int ps = newImg.getColorModel().getPixelSize();
                            float[][] newData = null;
                            if ( bitDepth == 16 && ps == 16 ) {
                                Image2RawData i2r = new Image2RawData( newImg, 1f / scaleFactor, -1 * offset );
                                // do not use image api if target bitDepth = 16
                                newData = i2r.parse();
                            }
                            for ( int i = 0; i < newImg.getWidth(); i++ ) {
                                for ( int j = 0; j < newImg.getHeight(); j++ ) {
                                    if ( x1 + i < out.getWidth() && y1 + j < out.getHeight() ) {
                                        int newImgPos = newImg.getWidth() * j + i;
                                        int outPos = out.getWidth() * ( y1 + j ) + ( x1 + i );
                                        if ( bitDepth == 16 && ps == 16 ) {
                                            // int v = newImgBuffer.getElem( newImgPos );
                                            // outBuffer.setElem( outPos, v );
                                            data[y1 + j][x1 + i] = newData[j][i];
                                        } else if ( bitDepth == 16 && ps == 32 ) {
                                            int v = newImg.getRGB( i, j );
                                            float f = Float.intBitsToFloat( v ) * 10f;
                                            outBuffer.setElem( outPos, Math.round( f ) );
                                            // TODO
                                            // data[y1 + j][x1 + i] = f;
                                        } else if ( bitDepth == 32 && ps == 16 ) {
                                            float f = newImgBuffer.getElem( newImgPos ) / 10f;
                                            outBuffer.setElem( outPos, Float.floatToIntBits( f ) );
                                        } else {
                                            out.setRGB( x1 + i, y1 + j, newImg.getRGB( i, j ) );
                                        }
                                    }
                                }
                            }
                            if ( ( bitDepth == 16 && ps == 16 ) || ( bitDepth == 16 && ps == 32 )
                                 || ( bitDepth == 32 && ps == 16 ) ) {
                                out.setData( Raster.createRaster( out.getSampleModel(), outBuffer, null ) );
                            }
                        } else {
                            Graphics g = out.getGraphics();
                            g.drawImage( newImg, x1, y1, newImg.getWidth(), newImg.getHeight(), null );
                            g.dispose();
                        }
                    } catch ( Exception e ) {
                        LOG.logError( "Could not draw upon the image: " );
                        LOG.logError( "New image is of size " + newImg.getWidth() + ", " + newImg.getHeight() );
                        LOG.logError( "Position/width tried is (" + x1 + ", " + y1 + ", " + newImg.getWidth() + ", "
                                      + newImg.getHeight() + ")" );
                        if ( imageFilesErrors != null ) {
                            imageFilesErrors.put( tile.getPostfix(), StringTools.stackTraceToString( e ) );
                        }
                    }
                }
            }
        }
    }

    public static BufferedImage resize( BufferedImage source, int destWidth, int destHeight, Object interpolation ) {

        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        double xScale = ( (double) destWidth ) / (double) sourceWidth;
        double yScale = ( (double) destHeight ) / (double) sourceHeight;
        if ( destWidth <= 0 ) {
            xScale = yScale;
            destWidth = (int) Math.rint( xScale * sourceWidth );
        }
        if ( destHeight <= 0 ) {
            yScale = xScale;
            destHeight = (int) Math.rint( yScale * sourceHeight );
        }
        WritableRaster ra = source.getRaster().createCompatibleWritableRaster( destWidth, destHeight );
        BufferedImage result = new BufferedImage( source.getColorModel(), ra, source.isAlphaPremultiplied(), null );
        Graphics2D g2d = null;
        try {
            g2d = result.createGraphics();
            g2d.setRenderingHint( RenderingHints.KEY_INTERPOLATION, interpolation );
            AffineTransform at = AffineTransform.getScaleInstance( xScale, yScale );
            g2d.drawRenderedImage( source, at );
        } finally {
            if ( g2d != null )
                g2d.dispose();
        }
        return result;
    }

    private static boolean isTransparent( BufferedImage bi ) {
        /*
         * TODO determine if the passed image is completly transparent for ( int i = 0; i < bi.getHeight(); i++ ) { for
         * ( int j = 0; j < bi.getWidth(); j++ ) { if ( bi.getRGB( i, j ) != 0 && bi.getRGB( i, j ) != -256 ) { return
         * false; } } } return true;
         */
        return false;
    }

    /**
     * @return an interpolation object from a well known name
     * @param interpolation
     */
    public static Object createInterpolation( String interpolation ) {
        Object interpol = null;

        if ( interpolation.equalsIgnoreCase( "Nearest Neighbor" ) ) {
            interpol = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        } else if ( interpolation.equalsIgnoreCase( "Bicubic" ) ) {
            interpol = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
        } else if ( interpolation.equalsIgnoreCase( "Bicubic2" ) ) {
            // for downward compability
            interpol = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
        } else if ( interpolation.equalsIgnoreCase( "Bilinear" ) ) {
            interpol = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
        } else {
            throw new RuntimeException( "invalid interpolation method: " + interpolation );
        }

        return interpol;
    }

    /**
     * Creates a world file for the corresponding tile in the <code>outputDir</code>. The name of the output file is
     * defined by the <code>baseName</code> and the tile's index of row and column.
     * 
     * @param tile
     *            The tile for which to create a world file.
     * @throws IOException
     */
    private void createWorldFile( Tile tile )
                            throws IOException {

        Envelope env = tile.getTileEnvelope();
        String postfix = tile.getPostfix();

        StringBuffer sb = new StringBuffer( 1000 );

        sb.append( minimumRes ).append( "\n" ).append( 0.0 ).append( "\n" ).append( 0.0 );
        sb.append( "\n" ).append( ( -1 ) * minimumRes ).append( "\n" );
        sb.append( env.getMin().getX() ).append( "\n" ).append( env.getMax().getY() );
        sb.append( "\n" );

        File f = new File( outputDir + '/' + Double.toString( minimumRes ) + '/' + baseName + postfix + ".wld" );

        FileWriter fw = new FileWriter( f );
        PrintWriter pw = new PrintWriter( fw );

        pw.print( sb.toString() );

        pw.close();
        fw.close();
    }

    /**
     * stores an envelope and the assigend image file information into a feature/featureCollection
     * 
     * @param dir
     *            directory where the image file is stored
     * @param file
     *            name of the image file
     * @param env
     *            bbox of the image file
     */
    private void storeEnvelope( String dir, String file, Envelope env ) {
        try {
            Geometry geom = GeometryFactory.createSurface( env, null );
            FeatureProperty[] props = new FeatureProperty[3];
            props[0] = FeatureFactory.createFeatureProperty( new QualifiedName( "GEOM" ), geom );
            props[1] = FeatureFactory.createFeatureProperty(
                                                             new QualifiedName(
                                                                                GridCoverageExchange.SHAPE_IMAGE_FILENAME ),
                                                             file );
            props[2] = FeatureFactory.createFeatureProperty( new QualifiedName( GridCoverageExchange.SHAPE_DIR_NAME ),
                                                             dir );
            Feature feat = FeatureFactory.createFeature( "file", ftype, props );
            fc.add( feat );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * creates a configuration file (extended CoverageDescriotion) for a WCS coverage considering the passed resolution
     * levels
     * 
     * @param targetResolutions
     */
    private void createConfigurationFile( double[] targetResolutions ) {

        // copy this file to the target directory
        String resolutions = "";
        sort( targetResolutions );
        int length = targetResolutions.length;

        for ( int i = 0; i < length; i++ ) {
            resolutions += String.valueOf( targetResolutions[length - 1 - i] );
            if ( i < ( length - 1 ) )
                resolutions += ',';
        }

        try {
            Map<String, String> param = new HashMap<String, String>( 20 );
            Envelope llEnv = getLatLonEnvelope( combiningEnvelope );
            param.put( "upperleftll", String.valueOf( llEnv.getMin().getX() ) + ','
                                      + String.valueOf( llEnv.getMin().getY() ) );
            param.put( "lowerrightll", String.valueOf( llEnv.getMax().getX() ) + ','
                                       + String.valueOf( llEnv.getMax().getY() ) );
            param.put( "upperleft", String.valueOf( combiningEnvelope.getMin().getX() ) + ','
                                    + String.valueOf( combiningEnvelope.getMin().getY() ) );
            param.put( "lowerright", String.valueOf( combiningEnvelope.getMax().getX() ) + ','
                                     + combiningEnvelope.getMax().getY() );
            File dir = new File( outputDir );
            if ( dir.isAbsolute() ) {
                // param.put( "dataDir", outputDir + '/' );
                param.put( "dataDir", "" );
            } else {
                param.put( "dataDir", "" );
            }
            param.put( "label", baseName );
            param.put( "name", baseName );
            param.put( "description", "" );
            param.put( "keywords", "" );
            param.put( "resolutions", resolutions );
            String frm = outputFormat;
            if ( "raw".equals( outputFormat ) && bitDepth == 32 ) {
                frm = "tif";
            } else if ( "raw".equals( outputFormat ) && bitDepth == 16 ) {
                frm = "GeoTiff";
            }
            param.put( "mimeType", frm );
            int p = srs.lastIndexOf( ':' );
            param.put( "srs", srs.substring( p + 1, srs.length() ) );
            param.put( "srsPre", srs.substring( 0, p + 1 ) );

            Reader reader = new InputStreamReader( configURL.openStream() );

            XSLTDocument xslt = new XSLTDocument();
            xslt.load( configXSL );
            XMLFragment xml = xslt.transform( reader, XMLFragment.DEFAULT_URL, null, param );
            reader.close();

            // write the result
            String dstFilename = "wcs_" + baseName + "_configuration.xml";
            File dstFile = new File( outputDir, dstFilename );
            String configurationFilename = dstFile.getAbsolutePath().toString();
            FileOutputStream fos = new FileOutputStream( configurationFilename );
            xml.write( fos );
            fos.close();

        } catch ( Exception e1 ) {
            e1.printStackTrace();
        }

    }

    private Envelope getLatLonEnvelope( Envelope env )
                            throws Exception {
        GeoTransformer gt = new GeoTransformer( "EPSG:4326" );
        return gt.transform( env, srs );
    }

    /**
     *
     */
    private void updateCapabilitiesFile( File capabilitiesFile ) {

        try {
            XSLTDocument xslt = new XSLTDocument();
            xslt.load( inputXSL );
            Map<String, String> param = new HashMap<String, String>();

            param.put( "dataDirectory", outputDir );
            String url = new File( "wcs_" + baseName + "_configuration.xml" ).toURL().toString();
            param.put( "configFile", url );
            Envelope llEnv = getLatLonEnvelope( combiningEnvelope );
            param.put( "upperleftll", String.valueOf( llEnv.getMin().getX() ) + ','
                                      + String.valueOf( llEnv.getMin().getY() ) );
            param.put( "lowerrightll", String.valueOf( llEnv.getMax().getX() ) + ','
                                       + String.valueOf( llEnv.getMax().getY() ) );

            param.put( "name", baseName );
            param.put( "label", baseName );

            param.put( "description", "" );
            param.put( "keywords", "" );

            XMLFragment xml = new XMLFragment();
            xml.load( capabilitiesFile.toURL() );

            xml = xslt.transform( xml, capabilitiesFile.toURL().toExternalForm(), null, param );

            // write the result
            FileOutputStream fos = new FileOutputStream( capabilitiesFile );
            xml.write( fos );
            fos.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Validates the content of <code>map</code>, to see, if necessary arguments were passed when calling this class.
     * 
     * @param map
     * @throws Exception
     */
    private static void validate( Properties map )
                            throws Exception {

        if ( map.get( "-outDir" ) == null ) {
            throw new Exception( "-outDir must be set" );
        }
        String s = (String) map.get( "-outDir" );
        if ( s.endsWith( "/" ) || s.endsWith( "\\" ) ) {
            s = s.substring( 0, s.length() - 1 );
        }

        if ( map.get( "-baseName" ) == null ) {
            throw new Exception( "-baseName must be set" );
        }
        if ( map.get( "-outputFormat" ) == null ) {
            map.put( "-outputFormat", "png" );
        } else {
            String format = ( (String) map.get( "-outputFormat" ) ).toLowerCase();
            if ( !"bmp".equals( format ) && !"png".equals( format ) && !"jpg".equals( format )
                 && !"jpeg".equals( format ) && !"tif".equals( format ) && !"tiff".equals( format )
                 && !"gif".equals( format ) && !( "raw" ).equals( format ) ) {

                throw new Exception( "-outputFormat must be one of the following: "
                                     + "'bmp', 'jpeg', 'jpg', 'png', 'tif', 'tiff', 'raw'." );
            }
        }
        if ( map.get( "-maxTileSize" ) == null ) {
            map.put( "-maxTileSize", "500" );
        }
        if ( map.get( "-srs" ) == null ) {
            map.put( "-srs", "EPSG:4326" );
        }
        if ( map.get( "-interpolation" ) == null ) {
            map.put( "-interpolation", "Nearest Neighbor" );
        }
        if ( map.get( "-noOfLevel" ) == null ) {
            map.put( "-noOfLevel", "1" );
        }
        if ( map.get( "-worldFileType" ) == null ) {
            map.put( "-worldFileType", "center" );
        }
        if ( map.get( "-quality" ) == null ) {
            map.put( "-quality", "0.95" );
        }
        if ( map.get( "-bbox" ) != null ) {
            double[] d = StringTools.toArrayDouble( (String) map.get( "-bbox" ), "," );
            Envelope env = GeometryFactory.createEnvelope( d[0], d[1], d[2], d[3], null );
            map.put( "-bbox", env );
            if ( map.get( "-resolution" ) == null ) {
                throw new Exception( "-resolution must be set if -bbox is set" );
            }
            map.put( "-resolution", new Double( (String) map.get( "-resolution" ) ) );
        } else {
            if ( map.get( "-resolution" ) == null ) {
                map.put( "-resolution", new Double( -1 ) );
            } else {
                map.put( "-resolution", new Double( (String) map.get( "-resolution" ) ) );
            }
        }
    }

    /**
     * @return the list of image map files to consider read from -mapFiles parameter
     * 
     * @param mapFiles
     */
    private static List<String> getFileList( String[] mapFiles ) {
        List<String> imageFiles = new ArrayList<String>();
        for ( int i = 0; i < mapFiles.length; i++ ) {
            imageFiles.add( mapFiles[i] );
        }
        return imageFiles;
    }

    /**
     * @return the list of image map files to consider read from a defined root directory.
     * 
     * @param rootDir
     *            root directory where to read image map files
     * @param subdirs
     *            true if subdirectories of the root directory shall be parsed for image maps too
     */
    private static List<String> getFileList( String rootDir, boolean subdirs ) {
        List<String> list = new ArrayList<String>( 10000 );
        File file = new File( rootDir );
        String[] entries = file.list( new DFileFilter() );
        if ( entries != null ) {
            for ( int i = 0; i < entries.length; i++ ) {
                File entry = new File( rootDir + '/' + entries[i] );
                if ( entry.isDirectory() && subdirs ) {
                    list = readSubDirs( entry, list );
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
     * @return the sub directories
     */
    private static List<String> readSubDirs( File file, List<String> list ) {

        String[] entries = file.list( new DFileFilter() );
        if ( entries != null ) {
            for ( int i = 0; i < entries.length; i++ ) {
                File entry = new File( file.getAbsolutePath() + '/' + entries[i] );
                if ( entry.isDirectory() ) {
                    list = readSubDirs( entry, list );
                } else {
                    list.add( file.getAbsolutePath() + '/' + entries[i] );
                }
            }
        }
        return list;
    }

    /**
     * @return the list of image map files to consider read from a dbase file defined by the dbase parameter
     * 
     * @param dbaseFile
     *            name of the dbase file
     * @param fileColumn
     *            name of the column containing the image map files names
     * @param baseDir
     *            name of the directory where the image map files are stored if this parameter is <code>null</code> it
     *            is assumed that the image map files are full referenced within the dbase
     * @param sort
     *            true if map image file names shall be sorted
     * @param sortColum
     *            name of the column that shall be used for sorting
     */
    private static List<String> getFileList( String dBaseFile, String fileColumn, String baseDir, boolean sort,
                                             String sortColum, String sortDirection )
                            throws Exception {

        // handle dbase file extension and file location/reading problems
        if ( dBaseFile.endsWith( ".dbf" ) ) {
            dBaseFile = dBaseFile.substring( 0, dBaseFile.lastIndexOf( "." ) );
        }
        DBaseFile dbf = new DBaseFile( dBaseFile );

        // sort dbase file contents chronologicaly (oldest first)
        int cnt = dbf.getRecordNum();

        Object[][] mapItems = new Object[cnt][2];
        QualifiedName fileC = new QualifiedName( APP_PREFIX, fileColumn.toUpperCase(), DEEGREEAPP );
        QualifiedName sortC = null;
        if ( sort ) {
            sortC = new QualifiedName( APP_PREFIX, sortColum.toUpperCase(), DEEGREEAPP );
        }
        for ( int i = 0; i < cnt; i++ ) {
            if ( sort ) {
                mapItems[i][0] = dbf.getFRow( i + 1 ).getDefaultProperty( sortC ).getValue();
            } else {
                mapItems[i][0] = new Integer( 1 );
            }
            // name of map file
            mapItems[i][1] = dbf.getFRow( i + 1 ).getDefaultProperty( fileC ).getValue();
        }
        Arrays.sort( mapItems, new MapAgeComparator( sortDirection ) );

        // extract names of image files from dBase file and attach them to rootDir
        if ( baseDir == null ) {
            baseDir = "";
        } else if ( !baseDir.endsWith( "/" ) && !baseDir.endsWith( "\\" ) ) {
            baseDir = baseDir + "/";
        }
        List<String> imageFiles = new ArrayList<String>( mapItems.length );
        for ( int i = 0; i < mapItems.length; i++ ) {
            if ( mapItems[i][0] != null ) {
                LOG.logDebug( "" + mapItems[i][0] );
                imageFiles.add( baseDir + mapItems[i][1] );
            }
        }

        return imageFiles;
    }

    private static void printHelp() {

        System.out.println( "-outDir directory where resulting tiles and describing shape(s) will be stored (mandatory)\r\n"
                            + "-redirect whether to redirect the standard output/error streams to a file rtb.log in the output directory. Default is false.\r\n"
                            + "-baseName base name used for creating names of the raster tile files. It also will be the name of the created coverage. (mandatory)\r\n"
                            + "-outputFormat name of the image format used for created tiles (png|jpg|jpeg|bmp|tif|tiff|gif|raw default png)\r\n"
                            + "-maxTileSize maximum size of created raster tiles in pixel (default 500)\r\n"
                            + "-srs name of the spatial reference system used for the coverage (default EPSG:4326)\r\n"
                            + "-interpolation interpolation method used for rescaling raster images (Nearest Neighbor|Bicubic|Bicubic2|Bilinear default Nearest Neighbor)\r\n"
                            + "               be careful using Bicubic and Bicubic2 interpolation; there seems to be a problem with JAI\r\n"
                            + "               If you use the proogram with images (tif) containing raw data like DEMs just use \r\n"
                            + "               Nearest Neighbor interpolation. All other interpolation methods will cause artefacts."
                            + "-bbox boundingbox of the the resulting coverage. If not set the bbox will be determined by analysing the input map files. (optional)\r\n"
                            + "-resolution spatial resolution of the resulting coverage. If not set the resolution will determined by analysing the input map files. This parameter is conditional; if -bbox is defined -resolution must be defined too.\r\n"
                            + "-noOfLevel number of tree levels created (optional default = 1)\r\n"
                            + "-capabilitiesFile name of a deegree WCS capabilities/configuration file. If defined the program will add the created rastertree as a new coverage to the WCS configuration.\r\n"
                            + "-h or -? print this help\r\n"
                            + "\r\n"
                            + "Input files\r\n"
                            + "there are three alternative ways/parameters to define which input files shall be used for creating a raster tree:\r\n"
                            + "1)\r\n"
                            + "-mapFiles defines a list of image file names (including full path information) seperated by \',\', \';\' or \'|\'\r\n"
                            + "\r\n"
                            + "2)\r\n"
                            + "-rootDir defines a directory that shall be parsed for files in a known image format. Each file found will be used as input.\r\n"
                            + "-subDirs conditional parameter used with -rootDir. It defines if all sub directories of -rootDir shall be parsed too (true|false default false)\r\n"
                            + "\r\n"
                            + "3)\r\n"
                            + "-dbaseFile name a dBase file that contains a column listing all files to be considered by the program\r\n"
                            + "-fileColumn name of the column containing the file names (mandatory if -dbaseFile is defined)\r\n"
                            + "-baseDir name of the directory where the files are stored. If this parameter will not be set the program assumes the -fileColumn contains completely referenced file names (optional)\r\n"
                            + "-sortColumn If -dbaseFile is defined one can define a column that shall be used for sorting the files referenced by the -fileColumn (optional)\r\n"
                            + "-sortDirection If -sortColumn is defined this parameter will be used for definition of sorting direction (UP|DOWN default UP)\r\n"
                            + "-worldFileType two types of are common: \r\n "
                            + "               a) the boundingbox is defined on the center of the corner pixels; \r\n "
                            + "               b) the boundingbox is defined on the outer corner of the corner pixels; \r\n "
                            + "               first is default and will be used if this parameter is not set; second will be use if '-worldFileType outer' is defined.\r\n"
                            + "-quality image quality if jpeg is used as output format; valid range is from 0..1 (default 0.95) \r\n"
                            + "-bitDepth image bit depth; valid values are 32 and 16, default is 16 \r\n"
                            + "-bgColor defines the background color of the created tiles for those region no data are available (e.g. -bgColor 0xFFFFF defines background as white) \r\n"
                            + "         If no -bgColor is defined, transparent background will be used for image formats that are transparency enabled (e.g. png) and black is used for all other formats (e.g. bmp) \r\n"
                            + "-offset defines the offset added to raster values if -outputFormat = raw and -bitDepth (default 0) \r\n"
                            + "-scaleFactor defines the factor by which raster values are multiplied if -outputFormat = raw and -bitDepth (default 1) \r\n"
                            + "\r\n"
                            + "Common to all option defining the input files is that each referenced file must be in a known image format (png, tif, jpeg, bmp, gif) and if must be geo-referenced by a world file or must be a GeoTIFF." );
        System.out.println();
        System.out.println( "caching:" );
        System.out.println( "To use default caching mechanism you just have to start RTB as before; to define " );
        System.out.println( "your own caching behavior you have to place a file named ehcache.xml within the " );
        System.out.println( "root of your classpath. The content of this file is described by the ehcache documentation " );
        System.out.println( "(http://ehcache.sourceforge.net/documentation); at least it must provide a cache named 'imgCache'." );
        System.out.println( " When defining your own cache please consider that just 'inMemory' caching is supported because " );
        System.out.println( "the objects cached by RTB are not serializable." );
        System.out.println();
        System.out.println( "Example invoking RTB (windows):" );
        System.out.println( "java -Xms300m -Xmx1000m -classpath .;.\\lib\\deegree2.jar;.\\lib\\acme.jar;"
                            + ".\\lib\\batik-awt-util.jar;.\\lib\\commons-beanutils-1.5.jar;"
                            + ".\\lib\\commons-codec-1.3.jar;.\\lib\\commons-collections-3.1.jar;"
                            + ".\\lib\\commons-digester-1.7.jar;.\\lib\\commons-discovery-0.2.jar;"
                            + ".\\lib\\commons-logging.jar;.\\lib\\jai_codec.jar;.\\lib\\jai_core.jar;"
                            + ".\\lib\\mlibwrapper_jai.jar;.\\lib\\j3dcore.jar;.\\lib\\j3dutils.jar;"
                            + ".\\lib\\vecmath.jar;.\\lib\\jts-1.6.jar;.\\lib\\log4j-1.2.9.jar;"
                            + ".\\lib\\axis.jar;.\\lib\\jaxen-1.1-beta-7.jar;.\\lib\\ehcache-1.2.0_03.jar "
                            + "org.deegree.tools.raster.RasterTreeBuilder "
                            + "-dbaseFile D:/lgv/resources/data/dbase/dip.dbf -outDir D:/lgv/output/ "
                            + "-baseName out -outputFormat jpg -maxTileSize 500 -noOfLevel 4 -interpolation "
                            + "Bilinear -bbox 3542428,5918168,3593354,5957043 -resolution 0.2 -sortColumn "
                            + "PLANJAHR -fileColumn NAME_PNG -sortDirection UP -quality 0.91 -baseDir "
                            + "D:/lgv/resources/data/images/ " );
    }

    /**
     * 
     * @param args
     *            Example arguments to pass when calling are:
     *            <ul>
     *            <li>-mapFiles D:/temp/europe_DK.jpg,D:/temp/europe_BeNeLux.jpg</li>
     *            <li>-outDir D:/temp/out/</li>
     *            <li>-baseName pretty</li>
     *            <li>-outputFormat png</li>
     *            <li>-maxTileSize 600</li>
     *            </ul>
     * 
     * @throws Exception
     */
    public static void main( String[] args )
                            throws Exception {
        long tsp = System.currentTimeMillis();
        Properties map = new Properties();
        for ( int i = 0; i < args.length; i += 2 ) {
            map.put( args[i], args[i + 1] );
        }

        if ( map.get( "-?" ) != null || map.get( "-h" ) != null ) {
            printHelp();
            return;
        }

        try {
            validate( map );
        } catch ( Exception e ) {
            LOG.logInfo( map.toString() );
            System.out.println( e.getMessage() );
            System.out.println();
            printHelp();
            return;
        }

        String outDir = map.getProperty( "-outDir" );

        // set up stderr/stdout redirection
        String redirect = map.getProperty( "-redirect" );
        if ( redirect != null && redirect.equals( "true" ) ) {
            File f = new File( outDir + separator + "rtb.log" );
            PrintStream out = new PrintStream( new FileOutputStream( f ) );
            System.setOut( out );
            System.setErr( out );
        }

        // read input parameters
        String baseName = map.getProperty( "-baseName" );
        String outputFormat = map.getProperty( "-outputFormat" );
        String srs = map.getProperty( "-srs" );
        if ( srs == null ) {
            srs = "EPSG:4326";
        }
        String interpolation = map.getProperty( "-interpolation" );
        Envelope env = (Envelope) map.get( "-bbox" );
        double resolution = ( (Double) map.get( "-resolution" ) ).doubleValue();
        int level = Integer.parseInt( map.getProperty( "-noOfLevel" ) );
        double maxTileSize = ( Double.valueOf( map.getProperty( "-maxTileSize" ) ) ).doubleValue();
        WorldFile.TYPE worldFileType = WorldFile.TYPE.CENTER;
        if ( "outer".equals( map.getProperty( "-worldFileType" ) ) ) {
            worldFileType = WorldFile.TYPE.OUTER;
        }
        float quality = Float.parseFloat( map.getProperty( "-quality" ) );
        String backgroundColor = map.getProperty( "-bgColor" );

        int depth = 0;

        if ( map.get( "-bitDepth" ) != null ) {
            depth = Integer.parseInt( map.getProperty( "-bitDepth" ) );
        }

        boolean dummy = false;
        if ( map.get( "-dummy" ) != null ) {
            dummy = true;
        }

        float offset = 0;
        if ( map.get( "-offset" ) != null ) {
            offset = Float.parseFloat( map.getProperty( "-offset" ) );
        }

        float scaleFactor = 1;
        if ( map.get( "-scaleFactor" ) != null ) {
            scaleFactor = Float.parseFloat( map.getProperty( "-scaleFactor" ) );
        }

        List<String> imageFiles = null;
        if ( map.get( "-mapFiles" ) != null ) {
            String[] mapFiles = StringTools.toArray( map.getProperty( "-mapFiles" ), ",;|", true );
            imageFiles = getFileList( mapFiles );
        } else if ( map.get( "-dbaseFile" ) != null ) {
            String dBaseFile = map.getProperty( "-dbaseFile" );
            String fileColum = map.getProperty( "-fileColumn" );
            String baseDir = map.getProperty( "-baseDir" );
            if ( baseDir == null ) {
                baseDir = map.getProperty( "-rootDir" );
            }
            boolean sort = map.get( "-sortColumn" ) != null;
            String sortColumn = map.getProperty( "-sortColumn" );
            if ( map.get( "-sortDirection" ) == null ) {
                map.put( "-sortDirection", "UP" );
            }
            String sortDirection = map.getProperty( "-sortDirection" );
            imageFiles = getFileList( dBaseFile, fileColum, baseDir, sort, sortColumn, sortDirection );
        } else if ( map.get( "-rootDir" ) != null ) {
            String rootDir = map.getProperty( "-rootDir" );
            boolean subDirs = "true".equals( map.get( "-subDirs" ) );
            imageFiles = getFileList( rootDir, subDirs );
        } else {
            LOG.logInfo( map.toString() );
            System.out.println( "-mapFiles, -rootDir or -dbaseFile parameter must be defined" );
            printHelp();
            return;
        }

        LOG.logDebug( imageFiles.toString() );
        LOG.logInfo( map.toString() );

        // initialize RasterTreeBuilder
        RasterTreeBuilderIndexed rtb = new RasterTreeBuilderIndexed( imageFiles, outDir, baseName, outputFormat,
                                                                     maxTileSize, srs, interpolation, worldFileType,
                                                                     quality, backgroundColor, depth, resolution,
                                                                     offset, scaleFactor, dummy );

        // calculate bbox and resolution from input images if parameters are not set
        if ( env == null ) {
            WorldFile wf = rtb.determineCombiningBBox();
            env = wf.getEnvelope();
            resolution = wf.getResx();
        }

        // Calculate necessary number of levels to get not more than 4
        // tiles in highest resolution
        if ( level == -1 ) {
            rtb.init( env, resolution );
            level = 0;
            int numTilesMax = Math.min( rtb.tileCols, rtb.tileRows );
            int numTiles = 4;
            while ( numTiles < numTilesMax ) {
                level += 1;
                numTiles *= 2;
            }
        }
        if ( level == 0 ) {
            level = 1;
        }
        System.out.println( "Number of Levels: " + level );

        // create tree where for each loop resolution will be halfed
        double[] re = new double[level];
        for ( int i = 0; i < level; i++ ) {
            rtb.init( env, resolution );
            rtb.start();
            rtb.logCollectedErrors();
            re[i] = resolution;
            if ( i < level - 1 ) {
                String dir = outDir + '/' + Double.toString( resolution );
                imageFiles = getFileList( dir, false );
                rtb = new RasterTreeBuilderIndexed( imageFiles, outDir, baseName, outputFormat, maxTileSize, srs,
                                                    interpolation, WorldFile.TYPE.CENTER, quality, backgroundColor,
                                                    depth, resolution, offset, scaleFactor );
            }
            resolution = resolution * 2;
        }

        LOG.logInfo( "create configuration files ..." );
        rtb.createConfigurationFile( re );

        if ( map.get( "-capabilitiesFile" ) != null ) {
            LOG.logInfo( "adjust capabilities ..." );
            File file = new File( map.getProperty( "-capabilitiesFile" ) );
            rtb.updateCapabilitiesFile( file );
        }

        rtb.logCollectedErrors();

        System.out.println( ( System.currentTimeMillis() - tsp ) / 1000 );
    }

    /**
     * class: official version of a FilenameFilter
     */
    static class DFileFilter implements FilenameFilter {

        private List<String> extensions = null;

        /**
         *
         */
        public DFileFilter() {
            extensions = new ArrayList<String>();
            extensions.add( "JPEG" );
            extensions.add( "JPG" );
            extensions.add( "BMP" );
            extensions.add( "PNG" );
            extensions.add( "GIF" );
            extensions.add( "TIF" );
            extensions.add( "TIFF" );
            extensions.add( "GEOTIFF" );
        }

        /**
         * @return "*.*"
         */
        public String getDescription() {
            return "*.*";
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
         */
        public boolean accept( java.io.File file, String name ) {
            int pos = name.lastIndexOf( "." );
            String ext = name.substring( pos + 1 ).toUpperCase();
            if ( file.isDirectory() ) {
                String s = file.getAbsolutePath() + '/' + name;
                File tmp = new File( s );
                if ( tmp.isDirectory() ) {
                    return true;
                }
            }
            return extensions.contains( ext );
        }
    }

    /**
     * 
     * This class enables sorting of dBaseFile objects in chronological order (lowest first, highest last).
     * 
     * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
     * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
     * @author last edited by: $Author$
     * 
     * @version 2.0, $Revision$, $Date$
     * 
     * @since 2.0
     */
    private static class MapAgeComparator implements Comparator<Object> {

        private String direction = null;

        /**
         * @param direction
         */
        public MapAgeComparator( String direction ) {
            this.direction = direction.toUpperCase();
        }

        public int compare( Object o1, Object o2 ) {
            Object[] o1a = (Object[]) o1;
            Object[] o2a = (Object[]) o2;

            if ( o1a[0] == null || o2a[0] == null ) {
                return 0;
            }
            if ( direction.equals( "UP" ) ) {
                return o1a[0].toString().compareTo( o2a[0].toString() );
            }
            return o2a[0].toString().compareTo( o1a[0].toString() );
        }
    }
}
