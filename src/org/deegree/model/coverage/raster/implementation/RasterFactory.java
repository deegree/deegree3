//$HeadURL: svn+ssh://otonnhofer@svn.wald.intevation.org/deegree/deegree3/model/trunk/src/org/deegree/model/coverage/raster/RasterFactory.java $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/
package org.deegree.model.coverage.raster.implementation;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.model.geometry.primitive.Envelope;

import org.deegree.model.coverage.raster.AbstractRaster;
import org.deegree.model.coverage.raster.LazyRasterDataContainer;
import org.deegree.model.coverage.raster.MemoryRasterDataContainer;
import org.deegree.model.coverage.raster.MemoryTileContainer;
import org.deegree.model.coverage.raster.MultiResolutionRaster;
import org.deegree.model.coverage.raster.RasterDataContainer;
import org.deegree.model.coverage.raster.RasterEnvelope;
import org.deegree.model.coverage.raster.RasterReader;
import org.deegree.model.coverage.raster.SimpleRaster;
import org.deegree.model.coverage.raster.TiledRaster;
import org.deegree.model.coverage.raster.implementation.io.JAIRasterReader;
import org.deegree.model.coverage.raster.implementation.io.JAIRasterWriter;
import org.deegree.model.coverage.raster.implementation.io.TileIndex;
import org.deegree.model.coverage.raster.implementation.io.TileIndexReader;
import org.deegree.model.coverage.raster.implementation.io.WorldFileReader;
import org.deegree.model.coverage.raster.implementation.io.WorldFileReader.TYPE;

/**
 * This class creates complex raster from raster files.
 * 
 * This class is the link between the io.raster packages and the AbstractRaster-based raster classes.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: otonnhofer $
 *
 * @version $Revision: 10847 $, $Date: 2008-03-31 15:54:40 +0200 (Mon, 31 Mar 2008) $
 */
public class RasterFactory {

    private final static String SHAPEFILE_SUFFIX = ".shp";

    private static Log log = LogFactory.getLog( RasterFactory.class );

    /**
     * Defines how raster should be loaded/stored.
     */
    public enum CachePolicy {
        /** Load raster right away and keep in memory */
        NONE,
        /** Load raster on first access and keep in memory */
        LAZY,
        /** Use caching. Load raster on fist access and cache in memory */
        CACHED
    }

    private static CachePolicy defaultCachePolicy = CachePolicy.CACHED;

    /**
     * Creates a new SimpleRaster from file.
     * 
     * Tries to load a proper world file and sets the envelope.
     * 
     * @param filename
     *            filename for raster
     * @return new SimpleRaster
     */
    public static SimpleRaster createRasterFromFile( String filename ) {
        return createRasterFromFile( filename, defaultCachePolicy );
    }

    /**
     * Creates a new SimpleRaster from file.
     * 
     * Tries to load a proper world file and sets the envelope.
     * 
     * @param filename
     *            filename for raster
     * @param policy
     *            CachePolicy for the SimpleRaster
     * @return new SimpleRaster
     */
    public static SimpleRaster createRasterFromFile( String filename, CachePolicy policy ) {
        JAIRasterReader reader = new JAIRasterReader( filename );

        int width = reader.getWidth();
        int height = reader.getHeight();

        reader.close();

        RasterEnvelope rasterEnvelope;

        try {
            rasterEnvelope = WorldFileReader.readWorldFile( filename, WorldFileReader.TYPE.CENTER, width, height );
        } catch ( IOException e ) {
            rasterEnvelope = new RasterEnvelope( 0.5, height - 0.5, 1.0, -1.0 );
        }

        Envelope envelope = rasterEnvelope.getEnvelope( width, height );
        RasterDataContainer source = containerWithCachePolicy( reader, policy );
        return new SimpleRaster( source, envelope, rasterEnvelope );
    }

    /**
     * Creates a new SimpleRaster from file with given Envelope and size.
     * 
     * @param filename
     *            filname for raster
     * @param envelope
     *            Envelope of the raster
     * @param width
     *            width of the raster in pixel
     * @param height
     *            height of the raster in pixel
     * @return new SimpleRaster
     */
    public static SimpleRaster createRasterFromFile( String filename, Envelope envelope, int width, int height ) {
        return createRasterFromFile( filename, envelope, width, height, defaultCachePolicy );
    }

    /**
     * Creates a new SimpleRaster from file with given Envelope and size.
     * 
     * @param filename
     *            filname for raster
     * @param envelope
     *            Envelope of the raster
     * @param width
     *            width of the raster in pixel
     * @param height
     *            height of the raster in pixel
     * @param policy
     *            CachePolicy for the SimpleRaster
     * @return new SimpleRaster
     */
    public static SimpleRaster createRasterFromFile( String filename, Envelope envelope, int width, int height,
                                                     CachePolicy policy ) {

        JAIRasterReader reader = new JAIRasterReader( filename );

        RasterEnvelope rasterEnvelope = new RasterEnvelope( envelope, width, height );

        RasterDataContainer source = containerWithCachePolicy( reader, policy );
        return new SimpleRaster( source, envelope, rasterEnvelope );
    }

    /*
    // TODO: sane exception handling
    public static DatastoreRaster createRasterFromSchema( String schemaFileName, String baseDir )
                            throws Exception {
        MappedGMLSchemaDocument schemaDoc = new MappedGMLSchemaDocument();
        schemaDoc.load( new File( schemaFileName ).toURL() );
        MappedGMLSchema gmlSchema = schemaDoc.parseMappedGMLSchema();
        Datastore datastore = gmlSchema.getDatastore();
        RasterDatastore rasterDS = new RasterDatastore( datastore, baseDir );
        return new DatastoreRaster( rasterDS );
    }
    */

    /**
     * Returns a new MultiResolutionRaster with tiles from tile indices. Loads all given tile indices. Each tileindex
     * represents a resolution level.
     * 
     * @param shapefiles
     *            array with filenames of the tile indices
     * @return MultiResolutionRaster
     */
    public static MultiResolutionRaster createMultiResolutionRaster( String[] shapefiles ) {
        MultiResolutionRaster result = new MultiResolutionRaster();

        for ( String filename : shapefiles ) {
            try {
                log.info( "reading tileindex " + filename );
                if ( filename.toLowerCase().endsWith( ".shp" ) ) {
                    filename = filename.substring( 0, filename.lastIndexOf( '.' ) );
                }
                result.addRaster( createShapeFileTiledRaster( filename ) );
            } catch ( IOException e ) {
                log.warn( "couldn't create multi-resolution level for " + filename );
            }
        }
        return result;
    }

    /**
     * Returns a new MultiResolutionRaster with tiles from tile indices. Loads all tileindex that begin with
     * shapeBasename (e.g. is shapeBasename is /tmp/level, it will load /tmp/level*.shp). Each tileindex represents a
     * resolution level.
     * 
     * @param shapeBasename
     *            common basename for all shapefiles
     * @return MultiResolutionRaster
     * @throws IOException
     */
    public static MultiResolutionRaster createMultiResolutionRaster( String shapeBasename )
                            throws IOException {
        int sep = shapeBasename.lastIndexOf( File.separatorChar );
        String pathname = shapeBasename.substring( 0, sep );
        String basename = shapeBasename.substring( sep + 1 );

        File dir = new File( pathname );
        LinkedList<String> indices = new LinkedList<String>();

        for ( String filename : dir.list() ) {
            if ( filename.startsWith( basename ) && filename.endsWith( SHAPEFILE_SUFFIX ) ) {
                indices.add( dir + File.separator + filename );
            }
        }

        return createMultiResolutionRaster( indices.toArray( new String[] {} ) );
    }

    /**
     * Returns a new TiledRaster with tiles from tileindex.
     * 
     * All tiles are loaded into memory (not necessarily the data, though).
     * 
     * @param tileindex
     *            filename to tileindex shape-file
     * @return TiledRaster
     * @throws IOException
     */
    public static TiledRaster createTiledRaster( String tileindex )
                            throws IOException {
        TileIndex index = TileIndexReader.readTileIndex( tileindex );

        // read first tile to get tilesize
        String firstTile = index.iterator().next();
        RasterReader reader = new JAIRasterReader( firstTile );
        int width = reader.getWidth();
        int height = reader.getHeight();

        MemoryTileContainer tileContainer = new MemoryTileContainer();
        TiledRaster tr = new TiledRaster( tileContainer );
        for ( String filename : index ) {
            Envelope env = index.getEnvelope( filename );
            AbstractRaster tile = RasterFactory.createRasterFromFile( filename, env, width, height );
            tileContainer.addTile( tile );
        }

        return tr;
    }

    /**
     * Returns a new TiledRaster that maps to a tileindex.
     * 
     * Tiles are loaded on access. The information of the tiles are read from the shapefile.
     * 
     * @param tileindex
     *            filename to tileindex shape-file
     * @return TiledRaster
     * @throws IOException
     */
    public static TiledRaster createShapeFileTiledRaster( String tileindex )
                            throws IOException {
        ShapeFileTileContainer tileContainer = new ShapeFileTileContainer( tileindex );
        TiledRaster tr = new TiledRaster( tileContainer );
        return tr;
    }

    /**
     * Saves an AbstractRaster as TIFF image
     * 
     * @param raster
     *            SimpleRaster to save
     * @param filename
     *            filename for output
     */
    public static void saveRasterToFile( AbstractRaster raster, String filename ) {
        JAIRasterWriter.rasterDataToImage( raster.getAsSimpleRaster().getRasterData(), filename );
    }

    /**
     * Saves a AbstractRaster as TIFF image. If saveWorldfile true, it saves the raster envelope (origin and pixel
     * resolution) in a world file (.wld).
     * 
     * @param raster
     *            SimpleRaster to save
     * @param filename
     *            filename for output
     * @param saveWorldFile
     *            true if a world file should be created
     */
    public static void saveRasterToFile( AbstractRaster raster, String filename, boolean saveWorldFile ) {

        saveRasterToFile( raster, filename );

        if ( saveWorldFile ) {
            try {
                RasterEnvelope rasterEnv = raster.getRasterEnvelope();
                WorldFileReader.writeWorldFile( rasterEnv, filename );
            } catch ( IOException e ) {
                log.warn( "could not write world file for " + filename );
            }
        }
    }

    /**
     * Create a RasterDataContainer for given CachePolicy
     */
    private static RasterDataContainer containerWithCachePolicy( RasterReader reader, CachePolicy policy ) {
        RasterDataContainer result;

        switch ( policy ) {
        case NONE:
            result = new MemoryRasterDataContainer( reader );
            break;
        case LAZY:
            result = new LazyRasterDataContainer( reader );
            break;
        case CACHED:
            result = new CachedRasterDataContainer( reader );
            break;
        default:
            throw new UnsupportedOperationException( "Unsupported CachePolicy" );
        }

        return result;
    }
}