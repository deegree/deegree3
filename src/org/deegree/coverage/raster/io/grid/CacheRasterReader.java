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
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.data.container.BufferResult;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;

/**
 * The <code>CacheRasterFile</code> is a grid based caching mechanism for raster readers.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class CacheRasterReader extends GridFileReader {
    private static final Logger LOG = getLogger( CacheRasterReader.class );

    /**
     * Standard name for a deegree cache file.
     */
    public static final String FILE_EXTENSION = ".d3rcache";

    private File cacheFile;

    private int width;

    private int height;

    /** in raster coordinates */
    private int tileWidth;

    /** in raster coordinates */
    private int tileHeight;

    private RasterReader cachedReader;

    private GridWriter gridWriter;

    private static final int TILE_SIZE = 500;

    private final ByteBuffer[][] tiles;

    private RasterGeoReference geoRef;

    /**
     * @param width
     * @param height
     * @param cacheFile
     * @param dataInfo
     */
    private CacheRasterReader( int width, int height, File cacheFile, RasterDataInfo dataInfo,
                               RasterGeoReference geoRefence ) {
        this.width = width;
        this.height = height;
        int numberOfTiles = calcApproxTiles( width, height );
        this.tileWidth = calcTileSize( width, numberOfTiles );
        this.tileHeight = calcTileSize( height, numberOfTiles );
        int columns = tileWidth / width;
        int rows = tileHeight / height;
        tiles = new ByteBuffer[columns][rows];
        if ( cacheFile == null ) {
            // create new cacheFile
        } else {
            this.cacheFile = cacheFile;
        }
        this.geoRef = geoRefence;
        Envelope env = geoRef.getEnvelope( width, height, null );
        GridMetaInfoFile gmif = new GridMetaInfoFile( geoRef, numberOfTiles, numberOfTiles, tileWidth, tileHeight,
                                                      dataInfo );
        super.instantiate( gmif, cacheFile );
        this.gridWriter = new GridWriter( numberOfTiles, numberOfTiles, env, geoRef, cacheFile, dataInfo );

    }

    public CacheRasterReader( ByteBuffer filledBuffer, int width, int height, File cacheFile, RasterDataInfo dataInfo,
                              RasterGeoReference geoReference ) {
        this( width, height, cacheFile, dataInfo, geoReference );
        if ( tiles.length == 1 ) {
            tiles[0][0] = filledBuffer;
        } else {
            // create tiling
            RasterRect tileRect = new RasterRect( 0, 0, tileWidth, tileHeight );
            RasterRect dataRect = new RasterRect( 0, 0, width, height );
            for ( int col = 0; col < tiles.length; ++col ) {
                for ( int row = 0; row < tiles[col].length; ++row ) {
                    tileRect.x = col * tileWidth;
                    tileRect.y = row * tileHeight;
                    tiles[col][row] = ByteBuffer.allocate( sampleSize * tileWidth * tileHeight );
                    try {
                        copyValuesFromTile( dataRect, tileRect, filledBuffer, tiles[col][row] );
                    } catch ( IOException e ) {
                        LOG.debug( "Could not create tile from buffer because: " + e.getLocalizedMessage(), e );
                    }
                }
            }
            // remove the old buffer from memory
            filledBuffer = null;
        }

    }

    /**
     * 
     */
    public CacheRasterReader( RasterReader cachedReader, File cacheFile, RasterDataInfo dataInfo ) {
        this( cachedReader.getWidth(), cachedReader.getHeight(), cacheFile, dataInfo, cachedReader.getGeoReference() );
        this.cachedReader = cachedReader;
    }

    /**
     * @param width2
     * @param height2
     * @param numberOfTiles
     * @return
     */
    private final static int calcTileSize( int imageSide, int numberOfTiles ) {
        double size = imageSide / numberOfTiles;
        return (int) Math.round( size );
    }

    private final static int calcApproxTiles( int imageWidth, int imageHeight ) {
        int largest = Math.max( imageWidth, imageHeight );
        // smaller then
        if ( largest < ( 0.5 * TILE_SIZE ) + TILE_SIZE ) {
            return 1;
        }
        if ( largest < 2 * TILE_SIZE ) {
            return 2;
        }
        int result = 3;
        while ( largest > ( result * TILE_SIZE ) ) {
            result++;
        }
        return result;
    }

    @Override
    public BufferResult read( RasterRect rect, ByteBuffer resultBuffer )
                            throws IOException {
        BufferResult res = null;
        if ( rect != null ) {
            //
            cacheFileUpToDate( rect );
            RasterRect intersection = snapToGrid( rect );
            if ( intersection != null ) {
                int[] minCRmaxCR = getIntersectingTiles( intersection );
                if ( minCRmaxCR != null ) {

                    if ( resultBuffer == null ) {
                        resultBuffer = ByteBuffer.allocate( intersection.height * intersection.width * sampleSize );
                    }
                    for ( int col = minCRmaxCR[0]; col < tiles.length && col <= minCRmaxCR[2]; ++col ) {
                        for ( int row = minCRmaxCR[1]; row < tiles[col].length && row <= minCRmaxCR[3]; ++row ) {
                            if ( tiles[col][row] == null ) {
                                tiles[col][row] = super.getTileData( col, row, null );
                            }
                            ByteBuffer tileBuffer = tiles[col][row];
                            copyValuesFromTile( col, row, intersection, tileBuffer, resultBuffer );
                        }
                    }
                    res = new BufferResult( intersection, resultBuffer );
                }
            }
        }
        return res;
    }

    private void copyValuesFromTile( int tileColumn, int tileRow, RasterRect requestedRect, ByteBuffer tileBuffer,
                                     ByteBuffer resultBuffer )
                            throws IOException {
        RasterRect tileRect = new RasterRect( this.tileWidth * tileColumn, tileHeight * tileRow, tileWidth, tileHeight );
        copyValuesFromTile( tileRect, requestedRect, tileBuffer, resultBuffer );
    }

    /**
     * @param col
     * @param tileRow
     * @param requestedRect
     * @param channel
     * @param resultBuffer
     * @throws IOException
     */
    private void copyValuesFromTile( RasterRect tileRect, RasterRect requestedRect, ByteBuffer tileBuffer,
                                     ByteBuffer resultBuffer )
                            throws IOException {
        RasterRect inter = RasterRect.intersection( tileRect, requestedRect );
        if ( inter != null ) {
            // rewind the buffer, to be on the right side with the limit.
            tileBuffer.rewind();

            // the size of one line of the intersection.
            int lineSize = inter.width * sampleSize;

            // offset to the byte buffer.
            int bufferOffsetY = inter.y - requestedRect.y;
            int bufferOffsetX = inter.x - requestedRect.x;

            // offset in the tile channel
            int tileOffsetX = inter.x - tileRect.x;
            int tileOffsetY = inter.y - tileRect.y;

            // keep track of the number of rows in a tile.
            int currentIntersectRow = tileOffsetY;

            // position of the buffer.
            int resultPos = 0;
            // limit of the buffer.
            int limit = 0;
            // the current file position.
            int tilePosition = 0;
            // loop over the intersection rows and put them into the right place in the bytebuffer.
            // get the intersection inside the tile, then read row-wise into the buffer.
            for ( int row = bufferOffsetY; row < ( bufferOffsetY + inter.height ); ++row, ++currentIntersectRow ) {
                tilePosition = ( ( tileOffsetX + ( currentIntersectRow * tileRect.width ) ) * sampleSize );
                limit = tilePosition + lineSize;
                tileBuffer.limit( limit );
                tileBuffer.position( tilePosition );
                resultPos = ( bufferOffsetX + ( requestedRect.width * row ) ) * sampleSize;
                // then the position.
                resultBuffer.position( resultPos );
                resultBuffer.put( tileBuffer );
            }
        }

    }

    /**
     * Currently only entire file pushing is supported
     */
    private synchronized void cacheFileUpToDate( RasterRect requestedRect ) {
        if ( cachedReader != null && cacheFile.lastModified() < cachedReader.file().lastModified() ) {
            // update the cachefile.
            try {
                // RasterRect totalRect = new RasterRect( 0, 0, gridReader.getWidth(), gridReader.getHeight() );
                RasterRect tileRect = new RasterRect( 0, 0, tileWidth, tileHeight );
                int rows = getTileRows();
                int columns = getTileColumns();
                ByteBuffer tileBuffer = ByteBuffer.allocate( tileWidth * tileHeight * sampleSize );
                for ( int row = 0; row < rows; ++row ) {
                    for ( int column = 0; column < columns; ++column ) {
                        // gridReader.getTileData( row, column, tileBuffer );
                        cachedReader.read( tileRect, tileBuffer );
                        gridWriter.writeTile( column, row, tileBuffer );
                        tileBuffer.rewind();
                        tileRect.x += tileWidth;
                    }
                    tileRect.x = 0;
                    tileRect.y += tileHeight;
                }
                gridWriter.writeMetadataFile( null );
                tileBuffer.clear();
                tileBuffer = null;

            } catch ( IOException e ) {
                LOG.debug( e.getLocalizedMessage(), e );
            }

        }
    }

    @Override
    public boolean canLoad( File filename ) {
        return FILE_EXTENSION.equalsIgnoreCase( FileUtils.getFileExtension( filename ) );
    }

    @Override
    public File file() {
        return cacheFile;
    }

    @Override
    public Set<String> getSupportedFormats() {
        HashSet<String> result = new HashSet<String>();
        result.add( FILE_EXTENSION );
        return result;
    }

    @Override
    public AbstractRaster load( File filename, RasterIOOptions options )
                            throws IOException {
        return new GridFileReader( filename, options ).load( filename, options );
    }

    @Override
    public AbstractRaster load( InputStream stream, RasterIOOptions options )
                            throws IOException {
        throw new UnsupportedOperationException( "Loading from a stream is currently not supported" );
    }

    @Override
    public boolean shouldCreateCacheFile() {
        return false;
    }

    @Override
    public RasterGeoReference getGeoReference() {
        return geoRef;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

}
