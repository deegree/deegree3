//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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

package org.deegree.coverage.raster.io.grid;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.deegree.coverage.raster.cache.ByteBufferPool;
import org.deegree.coverage.raster.data.container.BufferResult;
import org.deegree.coverage.raster.geom.RasterRect;
import org.slf4j.Logger;

/**
 * The <code>SplittedBlobReader</code> is a rasterreader for a grid file which is spread over multiple blobs. The blobs
 * are organised as follows: Each blob will hold the so much rows until a given file size is reached, the next blob will
 * hold the next tile in the same row and will then add rows etc. In other words the grid looks like this:
 * <p>
 * rows: 5 cols: 5 filesize: 7*tilesize.
 * <ul>
 * <li>file:blob_0.bin [(0,0)(0,1)(0,2)(0,3)(0,4)(1,0)(1,1)]</li>
 * <li>file:blob_1.bin [(1,2)(1,3)(1,4),(2,0)(2,1)(2,2)(2,3)]</li>
 * <li>...</li>
 * </ul>
 * the last file might be partially empty. Each tile will be saved flat row first (rb, please check this):
 * pixel(0,0)(0,1)(0,2)(0,3)(0,4)(0,5)(0,6)(0,7)(1,0)(1,1)(1,2)(1,3)(1,4)(1,5)(1,6)(1,7)
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class SplittedBlobReader extends GridFileReader {
    private static final Logger LOG = getLogger( SplittedBlobReader.class );

    private FileChannel[] blobChannels;

    /**
     * Creates a blob reader for a grid file which is spread over multiple blobs.
     * 
     * @param blobDir
     * @param blobFilename
     * @param blobFileext
     * @param infoFile
     */
    public SplittedBlobReader( File blobDir, String blobFilename, String blobFileext, GridMetaInfoFile infoFile ) {
        instantiate( infoFile );
        List<File> blobFiles = new ArrayList<File>();
        long totalSize = 0;
        int blobNo = 0;

        // find all blob files.
        while ( true ) {
            File blobFile = new File( blobDir, blobFilename + blobNo + blobFileext );
            if ( !blobFile.exists() ) {
                break;
            }
            blobFiles.add( blobFile );
            blobNo++;
            totalSize += blobFile.length();
        }
        LOG.debug( "Concatenated grid size (of all blob files): " + totalSize );

        blobChannels = new FileChannel[blobFiles.size()];
        for ( int i = 0; i < blobChannels.length; ++i ) {
            try {
                blobChannels[i] = new FileInputStream( blobFiles.get( i ) ).getChannel();
            } catch ( FileNotFoundException e ) {
                // this should not happen
                LOG.debug( "Could not find file: " + blobFiles.get( i ) + ": because: " + e.getLocalizedMessage(), e );
            }
        }
        setTilesPerBlob( (int) ( blobFiles.get( 0 ).length() / getBytesPerTile() ) );
        long expectedBlobSize = getTileRows() * getTileColumns() * getBytesPerTile();
        if ( expectedBlobSize != totalSize ) {
            String msg = "Size of grid (all blob file) (=" + totalSize + ") does not match the expected size (="
                         + expectedBlobSize + ").";
            throw new IllegalArgumentException( msg );
        }
    }

    @Override
    protected void read( int columnId, int rowId, ByteBuffer buffer )
                            throws IOException {
        long begin = System.currentTimeMillis();
        int tileId = getTileId( columnId, rowId );

        buffer.rewind();

        // transfer the data from the blob
        try {
            int blobNo = tileId / getTilesPerBlob();
            int tileInBlob = tileId % getTilesPerBlob();
            LOG.debug( "Tile id: {} -> pos in blob: {}", tileId, +tileInBlob );
            FileChannel channel = blobChannels[blobNo];
            channel.position( tileInBlob * getBytesPerTile() );
            channel.read( buffer );
            buffer.rewind();

        } catch ( IOException e ) {
            LOG.error( "Error reading tile data from blob: " + e.getMessage(), e );
        }

        if ( LOG.isDebugEnabled() ) {
            long elapsed = System.currentTimeMillis() - begin;
            LOG.debug( "Loading of tile ({}x{}) in {} ms.", new Object[] { infoFile.getTileRasterWidth(),
                                                                          infoFile.getTileRasterHeight(), elapsed } );
        }

    }

    @Override
    public boolean canLoad( File filename ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public BufferResult read( RasterRect rect, ByteBuffer resultBuffer )
                            throws IOException {
        BufferResult res = null;
        RasterRect fRect = snapToGrid( rect );
        if ( fRect != null ) {
            int[] minCRmaxCR = getIntersectingTiles( fRect );
            if ( minCRmaxCR == null ) {
                return null;
            }
            int size = fRect.width * fRect.height * sampleSize;
            if ( resultBuffer == null ) {
                resultBuffer = ByteBufferPool.allocate( size, false );
            }

            RasterRect tmpRect = new RasterRect( 0, 0, fRect.width, fRect.height );
            for ( int col = minCRmaxCR[0]; col <= minCRmaxCR[2]; ++col ) {
                for ( int row = minCRmaxCR[1]; row <= minCRmaxCR[3]; ++row ) {
                    int tileId = getTileId( col, row );
                    int blobNo = tileId / getTilesPerBlob();
                    FileChannel channel = blobChannels[blobNo];
                    readValuesFromTile( col, row, fRect, channel, resultBuffer );
                }
            }
            res = new BufferResult( tmpRect, resultBuffer );
        }
        return res;
    }

}
