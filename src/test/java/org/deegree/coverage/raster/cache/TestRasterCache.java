//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.coverage.raster.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.TiledRaster;
import org.deegree.coverage.raster.container.MemoryTileContainer;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.junit.Test;

/**
 * Test the caching of rasters.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TestRasterCache {

    private static final long TILED_SINGLE_RASTER_SIZE = 750000;

    // 4tiles * (w*h*(rgb-size) = 4 * (500*500*3) = 3000000
    private static final long TILED_RASTER_SIZE = 4 * TILED_SINGLE_RASTER_SIZE;

    /** 1000x1000 pixel, cached are 3x3 tiles of 334 pixel size */
    private static final long OVERVIEW_RASTER_TILE_SIZE = 334668;

    /** 1000x1000 pixel, cached are 3x3 tiles of 334 pixel size = 3012012 */
    private static final long OVERVIEW_RASTER_SIZE = 9 * OVERVIEW_RASTER_TILE_SIZE;

    private static final String[] TILE_INFO_0_0 = new String[] { "16.0", "0.0", "0.0", "-16.0", "420000.0",
                                                                "4519999.0", "1", "1", "500", "500", "3", "0", "500",
                                                                "500", "1" };

    private static final FileInfo TILE_0_0 = new FileInfo( "saltlakecity_0_0", TILED_SINGLE_RASTER_SIZE, TILE_INFO_0_0 );

    private static final String[] TILE_INFO_0_1 = new String[] { "16.0", "0.0", "0.0", "-16.0", "428000.0",
                                                                "4519999.0", "1", "1", "500", "500", "3", "0", "500",
                                                                "500", "1" };

    private static final FileInfo TILE_0_1 = new FileInfo( "saltlakecity_0_1", TILED_SINGLE_RASTER_SIZE, TILE_INFO_0_1 );

    private static final String[] TILE_INFO_1_0 = new String[] { "16.0", "0.0", "0.0", "-16.0", "420000.0",
                                                                "4511999.0", "1", "1", "500", "500", "3", "0", "500",
                                                                "500", "1" };

    private static final FileInfo TILE_1_0 = new FileInfo( "saltlakecity_1_0", TILED_SINGLE_RASTER_SIZE, TILE_INFO_1_0 );

    private static final String[] TILE_INFO_1_1 = new String[] { "16.0", "0.0", "0.0", "-16.0", "428000.0",
                                                                "4511999.0", "1", "1", "500", "500", "3", "0", "500",
                                                                "500", "1" };

    private static final FileInfo TILE_1_1 = new FileInfo( "saltlakecity_1_1", TILED_SINGLE_RASTER_SIZE, TILE_INFO_1_1 );

    private static final String[] OVERVIEW_INFO = new String[] { "0.1", "0.0", "0.0", "-0.1", "1000.0", "2100.0", "3",
                                                                "3", "334", "334", "3", "0", "1002", "1002", "111",
                                                                "111", "111" };

    private static final FileInfo OVERVIEW = new FileInfo( "overview", OVERVIEW_RASTER_SIZE, OVERVIEW_INFO );

    static final GeometryFactory geomFac = new GeometryFactory();

    private static final File CACHE_DIR;
    static {
        CACHE_DIR = new File( RasterCache.DEFAULT_CACHE_DIR + "/test" );
        if ( CACHE_DIR.exists() && CACHE_DIR.isDirectory() ) {
            File[] files = CACHE_DIR.listFiles();
            for ( File fi : files ) {
                System.out.println( "deleting file: " + fi.getAbsolutePath() );
                fi.delete();
            }
        }
    }

    private static void setRasterCache() {
        System.setProperty( RasterCache.DEF_RASTER_CACHE_MEM_SIZE, "4m" );
        System.setProperty( RasterCache.DEF_RASTER_CACHE_DISK_SIZE, "5m" );
        RasterCache.reset( true );

    }

    private static void resetCache() {
        System.out.println( "class is going down" );
        System.setProperty( RasterCache.DEF_RASTER_CACHE_MEM_SIZE, "" );
        System.setProperty( RasterCache.DEF_RASTER_CACHE_DISK_SIZE, "" );
        RasterCache.reset( true );
    }

    private RasterIOOptions getOptionsForFile( OriginLocation loc, String fileType, String crs ) {
        RasterIOOptions opts = new RasterIOOptions( loc );
        opts.add( RasterIOOptions.CREATE_RASTER_MISSING_CACHE_DIR, "true" );
        opts.add( RasterIOOptions.RASTER_CACHE_DIR, CACHE_DIR.getAbsolutePath() );
        opts.add( RasterIOOptions.OPT_FORMAT, fileType );
        if ( crs != null ) {
            opts.add( RasterIOOptions.CRS, crs );
        }
        return opts;
    }

    private AbstractRaster buildRaster( OriginLocation orig, String fileName, String type, String crs )
                            throws IOException, URISyntaxException {
        RasterIOOptions opts = getOptionsForFile( orig, type, crs );
        URL resource = TestRasterCache.class.getResource( fileName );
        File rFile = new File( resource.toURI() );
        return RasterFactory.loadRasterFromFile( rFile, opts );
    }

    private TiledRaster buildTiledRaster( OriginLocation type )
                            throws IOException, URISyntaxException {
        RasterIOOptions opts = getOptionsForFile( type, "jpg", "epsg:26912" );
        MemoryTileContainer mtc = new MemoryTileContainer();
        for ( int y = 0; y < 2; ++y ) {
            for ( int x = 0; x < 2; ++x ) {
                String f = y + "_" + x;

                URL resource = TestRasterCache.class.getResource( "utah/saltlakecity_" + f + ".jpg" );
                File rFile = new File( resource.toURI() );
                AbstractRaster raster = RasterFactory.loadRasterFromFile( rFile, opts );
                mtc.addTile( raster );
            }
        }
        return new TiledRaster( mtc );
    }

    /**
     * Test the requesting of tiles from different rasters.
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testTileCache()
                            throws IOException, URISyntaxException {
        setRasterCache();
        TiledRaster tR = buildTiledRaster( OriginLocation.CENTER );
        checkDiskSize( 0 );
        checkMemSize( 0 );

        // get a sub raster
        tR.getSubRaster( tR.getEnvelope() ).getAsSimpleRaster();
        checkDiskSize( 0 );
        checkMemSize( TILED_RASTER_SIZE );

        // once more
        tR.getSubRaster( tR.getEnvelope() ).getAsSimpleRaster();
        checkDiskSize( 0 );
        checkMemSize( TILED_RASTER_SIZE );

        // load the overview
        AbstractRaster raster = buildRaster( OriginLocation.CENTER, "overview.png", "png", "epsg:26912" );
        checkDiskSize( 0 );
        checkMemSize( TILED_RASTER_SIZE );

        // get a sub raster, which will result in to much memory, ( 3000000 + 3012012 > 4Mb), hence three tiles of the
        // tiled raster (least recently used) should be written to file.
        ( (ByteBufferRasterData) raster.getSubRaster( raster.getEnvelope() ).getAsSimpleRaster().getRasterData() ).getByteBuffer();
        // first three tiles are to be written to cache
        checkDiskSize( TILED_SINGLE_RASTER_SIZE * 3 );
        checkFiles( new FileInfo[] { TILE_0_0, TILE_0_1, TILE_1_0 } );
        checkMemSize( ( TILED_SINGLE_RASTER_SIZE ) + OVERVIEW_RASTER_SIZE );

        /**
         * get the first tile again, this call needs 750000 bytes of raster cache, currently there is 750000 + 3012012 =
         * 3762012 bytes. 3762012 + 750000 = 4512012 > 4194304 (4Mb), start freeing memory: <code>
         * - write TILE_1_1 to disk 3762012 - 750000 = 3012012 ( > 0.5 * 4Mb)
         * - write OVERVIEW to disk 3012012 - 3012012 = 0 Mb
         * => on disk are all files = 3012012 + 3000000 = 6012012
         * - add the last tile in memory = 750000 bytes
         * </code>
         */
        Envelope env = geomFac.createEnvelope( 420000.0, 4511999.0, 428000.0, 4519999.0, new CRS( "epsg:26912" ) );
        tR.getSubRaster( env ).getAsSimpleRaster();
        checkDiskSize( TILED_RASTER_SIZE + OVERVIEW_RASTER_SIZE );
        checkFiles( new FileInfo[] { TILE_0_0, TILE_0_1, TILE_1_0, TILE_1_1, OVERVIEW } );
        checkMemSize( TILED_SINGLE_RASTER_SIZE );

        /**
         * get the overview again, this call needs 3012012 bytes of extra raster cache memory, currently there is 750000
         * bytes of memory result will be 3762012 (<4Mb) and 6012012 bytes on disk.
         */
        ( (ByteBufferRasterData) raster.getSubRaster( raster.getEnvelope() ).getAsSimpleRaster().getRasterData() ).getByteBuffer();
        checkDiskSize( TILED_RASTER_SIZE + OVERVIEW_RASTER_SIZE );
        checkFiles( new FileInfo[] { TILE_0_0, TILE_0_1, TILE_1_0, TILE_1_1, OVERVIEW } );
        checkMemSize( TILED_SINGLE_RASTER_SIZE + OVERVIEW_RASTER_SIZE );

        /**
         * get the third tile again, this call needs 750000 bytes of raster cache, currently there is (first tile)
         * 750000 + (overview)3012012 = 3762012 bytes. 3762012 + 750000 = 4512012 > 4194304 (4Mb), start freeing memory:
         * <code>
         * - on disk is currently 6012012 > (5242880)5Mb
         * ==> delete TILE_0_1 (last recently used ) -> on disk == 6012012 - 750000 = 5262012 (3Tiles, overview)(>5Mb)
         * ==> delete TILE_1_1 (last recently used ) -> on disk == 5262012 - 750000 = 4512012 (2Tiles, overview)(<5Mb)
         * -- Mem will still be = 4512012
         * - free mem of TILE_0_0 => 3762012 - 750000 = 3012012 ( > 0.5 * 4Mb)
         * - free mem of OVERVIEW => 3012012 - 3012012 = 0 Mb
         * => on disk are 2Tile and Overview = 4512012
         * - add the last tile in memory = 750000 bytes
         * </code>
         */
        env = geomFac.createEnvelope( 420000.0, 4503999.0, 428000.0, 4511999.0, new CRS( "epsg:26912" ) );
        tR.getSubRaster( env ).getAsSimpleRaster();
        checkDiskSize( TILED_SINGLE_RASTER_SIZE * 2 + OVERVIEW_RASTER_SIZE );
        checkFiles( new FileInfo[] { TILE_0_0, TILE_1_0, OVERVIEW } );
        checkMemSize( TILED_SINGLE_RASTER_SIZE );

        /**
         * get the overview again, this call needs 2250000 bytes of extra raster cache memory, currently there is 750000
         * bytes of memory result will be 300000 (<4Mb) and 4512012 (<5Mb) bytes on disk.
         */
        tR.getSubRaster( tR.getEnvelope() ).getAsSimpleRaster();
        checkDiskSize( TILED_SINGLE_RASTER_SIZE * 2 + OVERVIEW_RASTER_SIZE );
        checkFiles( new FileInfo[] { TILE_0_0, TILE_1_0, OVERVIEW } );
        checkMemSize( TILED_RASTER_SIZE );

        /**
         * Get upper left tile from the overview raster, this call needs 334668 bytes of memory, currently there is
         * 300000 bytes -> result = 3334668 bytes in MEM
         */
        env = geomFac.createEnvelope( 1000, 2071, 1032, 2100, new CRS( "epsg:26912" ) );
        ( (ByteBufferRasterData) raster.getSubRaster( env ).getAsSimpleRaster().getRasterData() ).getByteBuffer();
        checkDiskSize( TILED_SINGLE_RASTER_SIZE * 2 + OVERVIEW_RASTER_SIZE );
        checkFiles( new FileInfo[] { TILE_0_0, TILE_1_0, OVERVIEW } );
        checkMemSize( TILED_RASTER_SIZE + OVERVIEW_RASTER_TILE_SIZE );

        /**
         * Get most right tiles from the overview raster, this call needs 3*334668 bytes of memory, currently there is
         * 3334668 bytes -> result 1st = 3669336, 2nd = 4004004, third call needs 4338672 bytes in MEM (>4Mb) clean up
         * is needed:<code>
         * -> tile_0_0 will be written to disk (still is) -> 4004004-750000=3254004 (>4M/2)
         * -> tile_0_1 will be written to disk (new) ->  3254004-750000=2504004 Mem (>4Mb/2), 
         *    on disk will be 5262012 (=>5Mb), next call must delete last recently used, which is tile_1_0 
         * -> tile_1_0 will be written to disk (still is but will be removed)-> 2504004-750000=1754004 Mem (<4Mb/2),
         *    Disk 5262012-750000 = 4512012 (<5mb=5242880)
         * </code> Disk=4512012 (2Tiles, Overview), Mem=2088672 ( TILE_1_1 + 4*Overview_Tiles)
         * 
         */
        env = geomFac.createEnvelope( 1070, 2001, 1100, 2100, new CRS( "epsg:26912" ) );
        ( (ByteBufferRasterData) raster.getSubRaster( env ).getAsSimpleRaster().getRasterData() ).getByteBuffer();
        checkDiskSize( TILED_SINGLE_RASTER_SIZE * 2 + OVERVIEW_RASTER_SIZE );
        checkFiles( new FileInfo[] { TILE_0_0, TILE_0_1, OVERVIEW } );
        checkMemSize( TILED_SINGLE_RASTER_SIZE + OVERVIEW_RASTER_TILE_SIZE * 4 );
        clearCache();
    }

    @Test
    public void testMultiThreaded()
                            throws IOException, URISyntaxException {
        setRasterCache();
        final TiledRaster tR = buildTiledRaster( OriginLocation.CENTER );
        checkDiskSize( 0 );
        checkMemSize( 0 );

        // load the overview
        final AbstractRaster raster = buildRaster( OriginLocation.CENTER, "overview.png", "png", "epsg:26912" );
        checkDiskSize( 0 );
        checkMemSize( 0 );
        Thread a = new Thread( new Runnable() {

            @Override
            public void run() {
                // get a sub raster
                tR.getSubRaster( tR.getEnvelope() ).getAsSimpleRaster();
                // once more
                tR.getSubRaster( tR.getEnvelope() ).getAsSimpleRaster();
                ( (ByteBufferRasterData) raster.getSubRaster( raster.getEnvelope() ).getAsSimpleRaster().getRasterData() ).getByteBuffer();
                Envelope env = geomFac.createEnvelope( 420000.0, 4511999.0, 428000.0, 4519999.0, new CRS( "epsg:26912" ) );
                tR.getSubRaster( env ).getAsSimpleRaster();
                ( (ByteBufferRasterData) raster.getSubRaster( raster.getEnvelope() ).getAsSimpleRaster().getRasterData() ).getByteBuffer();
                env = geomFac.createEnvelope( 420000.0, 4503999.0, 428000.0, 4511999.0, new CRS( "epsg:26912" ) );
                tR.getSubRaster( env ).getAsSimpleRaster();
                tR.getSubRaster( tR.getEnvelope() ).getAsSimpleRaster();
                env = geomFac.createEnvelope( 1000, 2071, 1032, 2100, new CRS( "epsg:26912" ) );
                ( (ByteBufferRasterData) raster.getSubRaster( env ).getAsSimpleRaster().getRasterData() ).getByteBuffer();
                env = geomFac.createEnvelope( 1070, 2001, 1100, 2100, new CRS( "epsg:26912" ) );
                ( (ByteBufferRasterData) raster.getSubRaster( env ).getAsSimpleRaster().getRasterData() ).getByteBuffer();
            }
        }, "a" );

        Thread b = new Thread( new Runnable() {

            @Override
            public void run() {
                Envelope env = geomFac.createEnvelope( 420000.0, 4511999.0, 428000.0, 4519999.0, new CRS( "epsg:26912" ) );
                tR.getSubRaster( env ).getAsSimpleRaster();

                tR.getSubRaster( tR.getEnvelope() ).getAsSimpleRaster();
                ( (ByteBufferRasterData) raster.getSubRaster( raster.getEnvelope() ).getAsSimpleRaster().getRasterData() ).getByteBuffer();

                env = geomFac.createEnvelope( 420000.0, 4503999.0, 428000.0, 4511999.0, new CRS( "epsg:26912" ) );
                tR.getSubRaster( env ).getAsSimpleRaster();

                tR.getSubRaster( tR.getEnvelope() ).getAsSimpleRaster();

                ( (ByteBufferRasterData) raster.getSubRaster( raster.getEnvelope() ).getAsSimpleRaster().getRasterData() ).getByteBuffer();

                // get a sub raster
                tR.getSubRaster( tR.getEnvelope() ).getAsSimpleRaster();

                env = geomFac.createEnvelope( 1070, 2001, 1100, 2100, new CRS( "epsg:26912" ) );
                ( (ByteBufferRasterData) raster.getSubRaster( env ).getAsSimpleRaster().getRasterData() ).getByteBuffer();

            }
        }, "b" );

        Thread c = new Thread( new Runnable() {

            @Override
            public void run() {
                ( (ByteBufferRasterData) raster.getSubRaster( raster.getEnvelope() ).getAsSimpleRaster().getRasterData() ).getByteBuffer();
                // once more
                tR.getSubRaster( tR.getEnvelope() ).getAsSimpleRaster();

                Envelope env = geomFac.createEnvelope( 420000.0, 4511999.0, 428000.0, 4519999.0, new CRS( "epsg:26912" ) );
                tR.getSubRaster( env ).getAsSimpleRaster();

                ( (ByteBufferRasterData) raster.getSubRaster( raster.getEnvelope() ).getAsSimpleRaster().getRasterData() ).getByteBuffer();
                tR.getSubRaster( tR.getEnvelope() ).getAsSimpleRaster();
                // get a sub raster
                tR.getSubRaster( tR.getEnvelope() ).getAsSimpleRaster();
                env = geomFac.createEnvelope( 1070, 2001, 1100, 2100, new CRS( "epsg:26912" ) );
                ( (ByteBufferRasterData) raster.getSubRaster( env ).getAsSimpleRaster().getRasterData() ).getByteBuffer();

                env = geomFac.createEnvelope( 420000.0, 4503999.0, 428000.0, 4511999.0, new CRS( "epsg:26912" ) );
                tR.getSubRaster( env ).getAsSimpleRaster();

            }
        }, "c" );
        a.start();
        b.start();
        c.start();
        try {
            a.join( 1000 );
            b.join( 1000 );
            c.join( 1000 );
        } catch ( Exception e ) {
            Assert.fail( "Waiting for a thread took to long, this might be a raster cache deadlock." );
        }
        clearCache();

    }

    private void clearCache() {
        // clear cache
        RasterCache.clear( true );
        Assert.assertEquals( 0, RasterCache.size() );
        Assert.assertEquals( 0, RasterCache.getCurrentlyUsedDisk() );
        Assert.assertEquals( 0, RasterCache.getCurrentlyUsedMemory() );
        Assert.assertFalse( CACHE_DIR.exists() );
        resetCache();
    }

    /**
     * @param strings
     */
    private void checkFiles( FileInfo[] expected ) {
        File[] curFiles = CACHE_DIR.listFiles();
        Assert.assertNotNull( "The files of the raster cache could not be listed, this may not be.", curFiles );
        if ( expected == null ) {
            Assert.assertEquals( 0, curFiles.length );
        } else {
            Assert.assertEquals( expected.length * 2, curFiles.length );
            Map<String, FileInfo> exp = new HashMap<String, FileInfo>();
            for ( FileInfo fi : expected ) {
                exp.put( fi.name, fi );
            }

            for ( File f : curFiles ) {
                String name = FileUtils.getFilename( f );
                Assert.assertTrue( exp.containsKey( name ) );
                FileInfo fi = exp.get( name );
                Assert.assertNotNull( fi );
                fi.testFile( f );

            }
        }

    }

    private void checkDiskSize( long expected ) {
        Assert.assertEquals( expected, RasterCache.getCurrentlyUsedDisk() );
    }

    private void checkMemSize( long expected ) {
        Assert.assertEquals( expected, RasterCache.getCurrentlyUsedMemory() );
    }

    private static final class FileInfo {

        final long expectedSize;

        final String name;

        final String[] infoFile;

        FileInfo( String baseName, long expectedSize, String[] infoFile ) {
            this.name = baseName;
            this.expectedSize = expectedSize;
            this.infoFile = Arrays.copyOf( infoFile, infoFile.length );
        }

        /**
         * @param f
         */
        void testFile( File f ) {
            if ( isCacheFile( f ) ) {
                Assert.assertEquals( expectedSize, f.length() );
            } else if ( isCacheInfoFile( f ) ) {
                testInfoFile( f );
            } else {
                Assert.fail( "The given file: " + f.getAbsolutePath()
                             + " is neither a raster cache file, nor a raster cache info file." );
            }

        }

        boolean isCacheFile( File f ) {
            if ( f != null ) {
                String ext = FileUtils.getFileExtension( f );
                return "d3rcache".equals( ext );
            }
            return false;
        }

        void testInfoFile( File f ) {
            if ( isCacheInfoFile( f ) ) {
                BufferedReader b = null;
                try {
                    b = new BufferedReader( new FileReader( f ) );
                    int lnr = 1;
                    for ( String exp : infoFile ) {
                        String line = b.readLine();
                        Assert.assertEquals( "Mismatch in line number: " + ( lnr++ ) + " of file: "
                                             + f.getAbsolutePath() + ".", exp, line );
                    }
                    b.close();
                } catch ( IOException e ) {
                    Assert.fail( "An io exception occurred while inspecting the info file for cache file: " + name
                                 + " message: " + e.getLocalizedMessage() );
                } finally {
                    if ( b != null ) {
                        try {
                            b.close();
                        } catch ( IOException e ) {
                            // nothing
                        }
                    }
                }
            }
        }

        boolean isCacheInfoFile( File f ) {
            if ( f != null ) {
                String ext = FileUtils.getFileExtension( f );
                return "info".equals( ext );
            }
            return false;
        }

        boolean matchExtension( File f ) {
            if ( f != null ) {
                String ext = FileUtils.getFileExtension( f );
                return ".d3rcache".equals( ext ) || ".info".equals( ext );
            }
            return false;
        }
    }

}
