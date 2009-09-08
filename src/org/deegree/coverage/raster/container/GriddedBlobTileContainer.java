//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.coverage.raster.container;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterDataFactory;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.InterleaveType;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.geom.RasterReference;
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of {@link GriddedTileContainer} that extracts the tiles from a blob with a custom format.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class GriddedBlobTileContainer extends GriddedTileContainer {

    private static Logger LOG = LoggerFactory.getLogger( GriddedBlobTileContainer.class );

    public static final String METAINFO_FILE_NAME = "gridded_raster.info";

    private final long bytesPerTile;

    private final int tilesPerBlob;

    private final FileChannel[] blobChannels;

    private final TileCache cache;

    public GriddedBlobTileContainer( File blobDir, Envelope envelope, int rows, int columns, int tileSamplesX,
                                     int tileSamplesY ) throws IOException {

        super( envelope, rows, columns, tileSamplesX, tileSamplesY );

        bytesPerTile = tileSamplesX * tileSamplesY * 3;
        LOG.debug( "Bytes per tile: " + bytesPerTile );

        long expectedBlobSize = (long) rows * columns * bytesPerTile;
        LOG.debug( "Expected blob size: " + expectedBlobSize );

        List<File> blobFiles = new ArrayList<File>();
        long totalSize = 0;
        int blobNo = 0;
        while ( true ) {
            File blobFile = new File( blobDir, "blob_" + blobNo + ".bin" );
            if ( !blobFile.exists() ) {
                break;
            }
            blobFiles.add( blobFile );
            blobNo++;
            totalSize += blobFile.length();
        }
        LOG.debug( "Real blob size: " + totalSize );

        if ( expectedBlobSize != totalSize ) {
            String msg = "Size of blobs  (=" + totalSize + ") does not match the expected size (=" + expectedBlobSize
                         + ").";
            throw new IllegalArgumentException( msg );
        }

        blobChannels = new FileChannel[blobFiles.size()];
        for ( int i = 0; i < blobChannels.length; i++ ) {
            FileInputStream fis = new FileInputStream( blobFiles.get( i ) );
            blobChannels[i] = fis.getChannel();
        }

        tilesPerBlob = (int) ( blobFiles.get( 0 ).length() / bytesPerTile );
        LOG.debug( "Tiles per (full) blob: " + tilesPerBlob );

        cache = new TileCache( 50 );
    }

    public static GriddedBlobTileContainer create( File dir )
                            throws IOException {

        File metaInfoFile = new File( dir, METAINFO_FILE_NAME );
        BufferedReader br = new BufferedReader( new FileReader( metaInfoFile ) );

        // read world file entries
        double[] worldFileValues = new double[6];
        try {
            for ( int i = 0; i < 6; i++ ) {
                String line = br.readLine();
                if ( line == null ) {
                    throw new IOException( "invalid metainfo file (" + metaInfoFile.getAbsolutePath() + ")" );
                }
                line = line.trim();
                double val = Double.parseDouble( line.replace( ',', '.' ) );
                worldFileValues[i] = val;
            }
        } catch ( NumberFormatException e ) {
            throw new IOException( "invalid metainfo file (" + metaInfoFile.getAbsolutePath() + ")" );
        }
        double resx = worldFileValues[0];
        double resy = worldFileValues[3];
        double xmin = worldFileValues[4];
        double ymax = worldFileValues[5];
        RasterReference renv = new RasterReference( xmin, ymax, resx, resy );

        // read grid info
        int rows = Integer.parseInt( br.readLine() );
        int columns = Integer.parseInt( br.readLine() );
        int tileSamplesX = Integer.parseInt( br.readLine() );
        int tileSamplesY = Integer.parseInt( br.readLine() );

        Envelope env = renv.getEnvelope( tileSamplesX * columns, tileSamplesY * rows );
        br.close();
        return new GriddedBlobTileContainer( dir, env, rows, columns, tileSamplesX, tileSamplesY );
    }

    @Override
    public AbstractRaster getTile( int rowId, int columnId ) {

        int tileId = getTileId( columnId, rowId );
        SimpleRaster tile = cache.get( tileId );

        if ( tile == null ) {
            long begin = System.currentTimeMillis();
            Envelope tileEnvelope = getTileEnvelope( rowId, columnId );
            RasterReference tileRasterReference = new RasterReference( tileEnvelope, tileSamplesX, tileSamplesY );

            ByteBufferRasterData tileData = RasterDataFactory.createRasterData( tileSamplesX, tileSamplesY,
                                                                                new BandType[] { BandType.RED,
                                                                                                BandType.GREEN,
                                                                                                BandType.BLUE },
                                                                                DataType.BYTE, InterleaveType.PIXEL );
            ByteBuffer buffer = tileData.getByteBuffer();
            buffer.rewind();

            // transfer the data from the blob
            try {
                int blobNo = tileId / tilesPerBlob;
                int tileInBlob = tileId % tilesPerBlob;

                LOG.debug( "Tile id: " + tileId + " -> blob no " + blobNo + ", pos in blob: " + tileInBlob );

                FileChannel channel = blobChannels[blobNo];
                channel.position( tileInBlob * bytesPerTile );
                channel.read( buffer );
                buffer.rewind();

            } catch ( IOException e ) {
                LOG.error( "Error reading tile data from blob: " + e.getMessage(), e );
            }

            long elapsed = System.currentTimeMillis() - begin;
            LOG.debug( "Loading of tile (" + tileSamplesX + "x" + tileSamplesY + ") in " + elapsed + " ms." );

            tile = new SimpleRaster( tileData, tileEnvelope, tileRasterReference );
            // cache.put( tileId, tile );
        }
        return tile;
    }

    /**
     * Inner class that provides a simple in-memory cache of tiles with an LRU strategy.
     */
    private class TileCache extends LinkedHashMap<Integer, SimpleRaster> {

        private int maxEntries;

        private TileCache( int maxEntries ) {
            super( 100, 0.1f, true );
            this.maxEntries = maxEntries;
        }

        @Override
        protected boolean removeEldestEntry( Map.Entry<Integer, SimpleRaster> eldest ) {
            return size() > maxEntries;
        }
    }

}
