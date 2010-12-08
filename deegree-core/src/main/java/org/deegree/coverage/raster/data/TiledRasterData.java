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

package org.deegree.coverage.raster.data;

import java.util.Arrays;

import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.grid.GridReader;
import org.deegree.coverage.raster.io.grid.TileOffsetReader;

/**
 * The <code>TiledRasterData</code> is a grid of raster data, wrapping all pixel operations on the tiles.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class TiledRasterData implements RasterData {

    private final RasterData[] tiles;

    private final int tileWidth;

    private final int tileHeight;

    private final int columns;

    private final int rows;

    private final RasterRect sampleDomain;

    private final RasterDataInfo dataInfo;

    /**
     * @param reader
     *            to be used for the tiles.
     * @param options
     * 
     */
    protected TiledRasterData( GridReader reader, RasterIOOptions options ) {
        if ( reader == null ) {
            throw new NullPointerException( "Grid reader may not be null." );
        }
        this.columns = reader.getTileColumns();
        this.rows = reader.getTileRows();
        this.tileWidth = reader.getTileRasterWidth();
        this.tileHeight = reader.getTileRasterHeight();
        tiles = new RasterData[this.columns * this.rows];
        for ( int row = 0; row < rows; ++row ) {
            for ( int col = 0; col < columns; ++col ) {
                TileOffsetReader r = new TileOffsetReader( reader, new RasterRect( col * tileWidth, row * tileHeight,
                                                                                   tileWidth, tileHeight ) );
                tiles[( row * columns ) + col] = RasterDataFactory.createRasterData( new RasterRect( 0, 0, tileWidth,
                                                                                                     tileHeight ),
                                                                                     reader.getRasterDataInfo(), r,
                                                                                     false, options );
            }
        }
        this.sampleDomain = new RasterRect( 0, 0, reader.getWidth(), reader.getHeight() );
        this.dataInfo = reader.getRasterDataInfo();
    }

    @Override
    public RasterData asReadOnly() {
        return this;
    }

    @Override
    public RasterData createCompatibleRasterData( RasterRect sampleDomain, BandType[] bands ) {
        throw new UnsupportedOperationException(
                                                 "Creating compatible raster data is not supported for the tiled raster data." );
    }

    @Override
    public ByteBufferRasterData createCompatibleRasterData( RasterRect sampleDomain ) {
        throw new UnsupportedOperationException(
                                                 "Creating compatible raster data is not supported for the tiled raster data." );
    }

    @Override
    public RasterData createCompatibleRasterData( int width, int height ) {
        throw new UnsupportedOperationException(
                                                 "Creating compatible raster data is not supported for the tiled raster data." );
    }

    @Override
    public RasterData createCompatibleRasterData( BandType[] bands ) {
        throw new UnsupportedOperationException(
                                                 "Creating compatible raster data is not supported for the tiled raster data." );
    }

    @Override
    public RasterData createCompatibleRasterData() {
        throw new UnsupportedOperationException(
                                                 "Creating compatible raster data is not supported for the tiled raster data." );
    }

    @Override
    public RasterData createCompatibleWritableRasterData( RasterRect sampleDomain, BandType[] bands ) {
        throw new UnsupportedOperationException(
                                                 "Creating writable compatible raster data is not supported for the tiled raster data." );
    }

    @Override
    public int getBands() {
        return dataInfo.bands;
    }

    /**
     * The rasterdata which is references by the given x, y value.
     * 
     * @param x
     * @param y
     * @return The rasterdata which is references by the given x, y value or <code>null</code> if not available.
     */
    private final MappedTile getTile( int x, int y ) {
        if ( y < 0 || y > this.sampleDomain.height || x < 0 || x > this.sampleDomain.width ) {
            return null;
        }
        final int tileY = ( y / tileHeight );
        final int tileX = ( x / tileWidth );
        final int index = ( tileY * columns ) + ( tileX );
        if ( index >= tiles.length ) {
            return null;
        }
        return new MappedTile( this.tiles[index], x - ( tileX * tileWidth ), y - ( tileY * tileHeight ) );
    }

    /**
     * Returns the min column, row and max column row of the given rect. The rectangle will be cut off to fit the data.
     * If the rect does not intersect the data, <code>null</code> will be returned.
     * 
     * @param rect
     * @return {min column, min row, max column, max row} or <code>null</code> if the given rect does not intersect the
     *         data.
     */
    private final int[] getIntersectingTiles( RasterRect rect ) {
        RasterRect fRect = RasterRect.intersection( sampleDomain, rect );
        if ( fRect != null ) {
            final int minCol = getColNumber( fRect.x );
            final int minRow = getRowNumber( fRect.y );
            final int maxCol = getColNumber( fRect.x + fRect.width );
            final int maxRow = getRowNumber( fRect.y + fRect.height );
            if ( ( maxCol != -1 ) && ( maxRow != -1 ) && ( minCol != columns ) && ( minRow != rows ) ) {
                return new int[] { minCol, minRow, maxCol, maxRow };
            }
        }
        return null;
    }

    /**
     * @param rasterCoord
     *            normally the y.
     * @return the row number of tile which holds the given raster coordinate.
     */
    private int getRowNumber( float rasterCoord ) {
        int row = (int) Math.floor( rasterCoord / tileHeight );
        // if < 0, then -1 and >= rows than rows.
        return Math.min( rows, Math.max( -1, row ) );
    }

    /**
     * @param rasterCoord
     *            in raster coordinates normally x.
     * @return the column number of tile which holds the given raster coordinate.
     */
    private int getColNumber( float rasterCoord ) {
        int column = (int) Math.floor( rasterCoord / tileWidth );
        return Math.min( columns, Math.max( -1, column ) );
    }

    @Override
    public byte[] getBytePixel( int x, int y, byte[] result ) {
        if ( result == null || result.length < dataInfo.bands ) {
            result = new byte[dataInfo.bands];
        }
        for ( int band = 0; band < dataInfo.bands; band++ ) {
            result[band] = getByteSample( x, y, band );
        }
        return result;
    }

    @Override
    public byte getByteSample( int x, int y, int band ) {
        MappedTile tile = getTile( x, y );
        if ( tile == null ) {
            return dataInfo.getByteNoDataForBand( band );
        }
        synchronized ( tiles ) {
            return tile.data.getByteSample( tile.mappedX, tile.mappedY, band );
        }
    }

    @Override
    public byte[] getBytes( int x, int y, int width, int height, int band, byte[] result ) {
        RasterRect requestedRect = new RasterRect( x, y, width, height );
        int[] interSectingTiles = getIntersectingTiles( requestedRect );
        byte[] resultValues = result;
        // one band.
        if ( result == null || result.length != requestedRect.width * requestedRect.height ) {
            resultValues = new byte[requestedRect.width * requestedRect.height];
        }
        // fill with no data
        byte noData = dataInfo.getByteNoDataForBand( band );
        Arrays.fill( resultValues, noData );

        if ( interSectingTiles != null ) {
            RasterRect tileRect = new RasterRect( 0, 0, tileWidth, tileHeight );
            for ( int row = interSectingTiles[1]; row < interSectingTiles[3]; ++row ) {
                for ( int col = interSectingTiles[0]; col < interSectingTiles[2]; ++col ) {
                    // rect of tile
                    tileRect.x = col * tileWidth;
                    tileRect.y = row * tileHeight;
                    // intersection of request with given tile
                    final RasterRect intersectionWithTile = RasterRect.intersection( tileRect, requestedRect );
                    if ( intersectionWithTile != null ) {
                        // an intersection, get the raster tile
                        final RasterData tile = tiles[row * columns + col];
                        if ( tile != null ) {

                            final int widthOffset = ( intersectionWithTile.x / tileWidth );
                            final int heightOffset = ( intersectionWithTile.y / tileHeight );
                            // map to the given tile coordinate system.
                            final RasterRect tReq = new RasterRect( ( intersectionWithTile.x - widthOffset ),
                                                                    ( intersectionWithTile.y - heightOffset ),
                                                                    intersectionWithTile.width,
                                                                    intersectionWithTile.height );
                            // get the result
                            byte[] tileResult = null;
                            synchronized ( tiles ) {
                                tileResult = tile.getBytes( tReq.x, tReq.y, tReq.width, tReq.height, band, null );
                            }

                            // calculate the offset in the result array.
                            final int offsetInResult = ( intersectionWithTile.y * requestedRect.width )
                                                       + intersectionWithTile.x;
                            // copy the result with the data from the tile.
                            for ( int yResult = 0; yResult < intersectionWithTile.height; ++yResult ) {
                                // one band, so row copying is available
                                System.arraycopy( tileResult, yResult * tReq.width, resultValues,
                                                  ( yResult * requestedRect.width ) + offsetInResult,
                                                  intersectionWithTile.width );
                            }
                        }
                    }
                }
            }
        }
        return resultValues;
    }

    @Override
    public RasterDataInfo getDataInfo() {
        return dataInfo;
    }

    @Override
    public DataType getDataType() {
        return dataInfo.dataType;
    }

    @Override
    public double[] getDoublePixel( int x, int y, double[] result ) {
        if ( result == null || result.length < dataInfo.bands ) {
            result = new double[dataInfo.bands];
        }
        for ( int band = 0; band < dataInfo.bands; band++ ) {
            result[band] = getDoubleSample( x, y, band );
        }
        return result;
    }

    @Override
    public double getDoubleSample( int x, int y, int band ) {
        MappedTile tile = getTile( x, y );
        if ( tile == null ) {
            return dataInfo.getDoubleNoDataForBand( band );
        }
        synchronized ( tiles ) {
            return tile.data.getDoubleSample( tile.mappedX, tile.mappedY, band );
        }
    }

    @Override
    public double[] getDoubles( int x, int y, int width, int height, int band, double[] result ) {
        RasterRect requestedRect = new RasterRect( x, y, width, height );
        int[] interSectingTiles = getIntersectingTiles( requestedRect );
        double[] resultValues = result;
        // one band.
        if ( result == null || result.length != requestedRect.width * requestedRect.height ) {
            resultValues = new double[requestedRect.width * requestedRect.height];
        }
        // fill with no data
        double noData = dataInfo.getDoubleNoDataForBand( band );
        Arrays.fill( resultValues, noData );

        if ( interSectingTiles != null ) {
            final RasterRect tileRect = new RasterRect( 0, 0, tileWidth, tileHeight );
            for ( int row = interSectingTiles[1]; row < interSectingTiles[3]; ++row ) {
                for ( int col = interSectingTiles[0]; col < interSectingTiles[2]; ++col ) {
                    // rect of tile
                    tileRect.x = col * tileWidth;
                    tileRect.y = row * tileHeight;
                    // intersection of request with given tile
                    final RasterRect intersectionWithTile = RasterRect.intersection( tileRect, requestedRect );
                    if ( intersectionWithTile != null ) {
                        // an intersection, get the raster tile
                        final RasterData tile = tiles[row * columns + col];
                        if ( tile != null ) {

                            final int widthOffset = ( intersectionWithTile.x / tileWidth );
                            final int heightOffset = ( intersectionWithTile.y / tileHeight );
                            // map to the given tile coordinate system.
                            final RasterRect tReq = new RasterRect( ( intersectionWithTile.x - widthOffset ),
                                                                    ( intersectionWithTile.y - heightOffset ),
                                                                    intersectionWithTile.width,
                                                                    intersectionWithTile.height );
                            // get the result
                            double[] tileResult = null;
                            synchronized ( tiles ) {
                                tileResult = tile.getDoubles( tReq.x, tReq.y, tReq.width, tReq.height, band, null );
                            }

                            // calculate the offset in the result array.
                            final int offsetInResult = ( intersectionWithTile.y * requestedRect.width )
                                                       + intersectionWithTile.x;
                            // copy the result with the data from the tile.
                            for ( int yResult = 0; yResult < intersectionWithTile.height; ++yResult ) {
                                // one band, so row copying is available
                                System.arraycopy( tileResult, yResult * tReq.width, resultValues,
                                                  ( yResult * requestedRect.width ) + offsetInResult,
                                                  intersectionWithTile.width );
                            }
                        }
                    }
                }
            }
        }
        return resultValues;
    }

    @Override
    public float[] getFloatPixel( int x, int y, float[] result ) {
        if ( result == null || result.length < dataInfo.bands ) {
            result = new float[dataInfo.bands];
        }
        for ( int band = 0; band < dataInfo.bands; band++ ) {
            result[band] = getFloatSample( x, y, band );
        }
        return result;
    }

    @Override
    public float getFloatSample( int x, int y, int band ) {
        MappedTile tile = getTile( x, y );
        if ( tile == null ) {
            return dataInfo.getFloatNoDataForBand( band );
        }
        synchronized ( tiles ) {
            return tile.data.getFloatSample( tile.mappedX, tile.mappedY, band );
        }

    }

    @Override
    public float[] getFloats( int x, int y, int width, int height, int band, float[] result ) {
        RasterRect requestedRect = new RasterRect( x, y, width, height );
        int[] interSectingTiles = getIntersectingTiles( requestedRect );
        float[] resultValues = result;
        // one band.
        if ( result == null || result.length != requestedRect.width * requestedRect.height ) {
            resultValues = new float[requestedRect.width * requestedRect.height];
        }
        // fill with no data
        float noData = dataInfo.getFloatNoDataForBand( band );
        Arrays.fill( resultValues, noData );

        if ( interSectingTiles != null ) {
            final RasterRect tileRect = new RasterRect( 0, 0, tileWidth, tileHeight );
            for ( int row = interSectingTiles[1]; row < interSectingTiles[3]; ++row ) {
                for ( int col = interSectingTiles[0]; col < interSectingTiles[2]; ++col ) {
                    // rect of tile
                    tileRect.x = col * tileWidth;
                    tileRect.y = row * tileHeight;
                    // intersection of request with given tile
                    final RasterRect intersectionWithTile = RasterRect.intersection( tileRect, requestedRect );
                    if ( intersectionWithTile != null ) {
                        // an intersection, get the raster tile
                        final RasterData tile = tiles[row * columns + col];
                        if ( tile != null ) {
                            final int widthOffset = ( intersectionWithTile.x / tileWidth );
                            final int heightOffset = ( intersectionWithTile.y / tileHeight );
                            // map to the given tile coordinate system.
                            final RasterRect tReq = new RasterRect( ( intersectionWithTile.x - widthOffset ),
                                                                    ( intersectionWithTile.y - heightOffset ),
                                                                    intersectionWithTile.width,
                                                                    intersectionWithTile.height );
                            // get the result
                            float[] tileResult = null;
                            synchronized ( tiles ) {
                                tileResult = tile.getFloats( tReq.x, tReq.y, tReq.width, tReq.height, band, null );
                            }

                            // calculate the offset in the result array.
                            final int offsetInResult = ( intersectionWithTile.y * requestedRect.width )
                                                       + intersectionWithTile.x;
                            // copy the result with the data from the tile.
                            for ( int yResult = 0; yResult < intersectionWithTile.height; ++yResult ) {
                                // one band, so row copying is available
                                System.arraycopy( tileResult, yResult * tReq.width, resultValues,
                                                  ( yResult * requestedRect.width ) + offsetInResult,
                                                  intersectionWithTile.width );
                            }
                        }
                    }
                }
            }
        }
        return resultValues;
    }

    @Override
    public int getRows() {
        return sampleDomain.height;
    }

    @Override
    public int[] getIntPixel( int x, int y, int[] result ) {
        if ( result == null || result.length < dataInfo.bands ) {
            result = new int[dataInfo.bands];
        }
        for ( int band = 0; band < dataInfo.bands; band++ ) {
            result[band] = getIntSample( x, y, band );
        }
        return result;
    }

    @Override
    public int getIntSample( int x, int y, int band ) {
        MappedTile tile = getTile( x, y );
        if ( tile == null ) {
            return dataInfo.getIntNoDataForBand( band );
        }
        synchronized ( tiles ) {
            return tile.data.getIntSample( tile.mappedX, tile.mappedY, band );
        }
    }

    @Override
    public int[] getInts( int x, int y, int width, int height, int band, int[] result ) {
        RasterRect requestedRect = new RasterRect( x, y, width, height );
        int[] interSectingTiles = getIntersectingTiles( requestedRect );
        int[] resultValues = result;
        // one band.
        if ( result == null || result.length != requestedRect.width * requestedRect.height ) {
            resultValues = new int[requestedRect.width * requestedRect.height];
        }
        // fill with no data
        int noData = dataInfo.getIntNoDataForBand( band );
        Arrays.fill( resultValues, noData );

        if ( interSectingTiles != null ) {
            final RasterRect tileRect = new RasterRect( 0, 0, tileWidth, tileHeight );
            for ( int row = interSectingTiles[1]; row < interSectingTiles[3]; ++row ) {
                for ( int col = interSectingTiles[0]; col < interSectingTiles[2]; ++col ) {
                    // rect of tile
                    tileRect.x = col * tileWidth;
                    tileRect.y = row * tileHeight;
                    // intersection of request with given tile
                    final RasterRect intersectionWithTile = RasterRect.intersection( tileRect, requestedRect );
                    if ( intersectionWithTile != null ) {
                        // an intersection, get the raster tile
                        final RasterData tile = tiles[row * columns + col];
                        if ( tile != null ) {

                            final int widthOffset = ( intersectionWithTile.x / tileWidth );
                            final int heightOffset = ( intersectionWithTile.y / tileHeight );
                            // map to the given tile coordinate system.
                            final RasterRect tReq = new RasterRect( ( intersectionWithTile.x - widthOffset ),
                                                                    ( intersectionWithTile.y - heightOffset ),
                                                                    intersectionWithTile.width,
                                                                    intersectionWithTile.height );
                            // get the result
                            int[] tileResult = null;
                            synchronized ( tiles ) {
                                tileResult = tile.getInts( tReq.x, tReq.y, tReq.width, tReq.height, band, null );
                            }
                            // calculate the offset in the result array.
                            final int offsetInResult = ( intersectionWithTile.y * requestedRect.width )
                                                       + intersectionWithTile.x;
                            // copy the result with the data from the tile.
                            for ( int yResult = 0; yResult < intersectionWithTile.height; ++yResult ) {
                                // one band, so row copying is available
                                System.arraycopy( tileResult, yResult * tReq.width, resultValues,
                                                  ( yResult * requestedRect.width ) + offsetInResult,
                                                  intersectionWithTile.width );
                            }
                        }
                    }
                }
            }
        }
        return resultValues;
    }

    @Override
    public byte[] getNullPixel( byte[] result ) {
        return dataInfo.getNoDataPixel( result );
    }

    @Override
    public byte[] getPixel( int x, int y, byte[] result ) {
        if ( result == null || result.length < dataInfo.bands ) {
            result = new byte[dataInfo.bands * dataInfo.dataSize];
        }
        byte bandResult[] = new byte[dataInfo.dataSize];
        for ( int band = 0; band < dataInfo.bands; band++ ) {
            getSample( x, y, band, bandResult );
            System.arraycopy( bandResult, 0, result, band * dataInfo.dataSize, dataInfo.dataSize );
        }
        return result;
    }

    @Override
    public byte[] getSample( int x, int y, int band, byte[] result ) {
        MappedTile tile = getTile( x, y );
        if ( tile == null ) {
            return dataInfo.getNoDataSample( band, result );
        }
        synchronized ( tiles ) {
            return tile.data.getSample( tile.mappedX, tile.mappedY, band, result );
        }
    }

    @Override
    public short[] getShortPixel( int x, int y, short[] result ) {
        if ( result == null || result.length < dataInfo.bands ) {
            result = new short[dataInfo.bands];
        }
        for ( int band = 0; band < dataInfo.bands; band++ ) {
            result[band] = getShortSample( x, y, band );
        }
        return result;
    }

    @Override
    public short getShortSample( int x, int y, int band ) {
        MappedTile tile = getTile( x, y );
        if ( tile == null ) {
            return dataInfo.getShortNoDataForBand( band );
        }
        synchronized ( tiles ) {
            return tile.data.getShortSample( tile.mappedX, tile.mappedY, band );
        }
    }

    @Override
    public short[] getShorts( int x, int y, int width, int height, int band, short[] result ) {
        RasterRect requestedRect = new RasterRect( x, y, width, height );
        int[] interSectingTiles = getIntersectingTiles( requestedRect );
        short[] resultValues = result;
        // one band.
        if ( result == null || result.length != requestedRect.width * requestedRect.height ) {
            resultValues = new short[requestedRect.width * requestedRect.height];
        }
        // fill with no data
        short noData = dataInfo.getShortNoDataForBand( band );
        Arrays.fill( resultValues, noData );

        if ( interSectingTiles != null ) {
            final RasterRect tileRect = new RasterRect( 0, 0, tileWidth, tileHeight );
            for ( int row = interSectingTiles[1]; row < interSectingTiles[3]; ++row ) {
                for ( int col = interSectingTiles[0]; col < interSectingTiles[2]; ++col ) {
                    // rect of tile
                    tileRect.x = col * tileWidth;
                    tileRect.y = row * tileHeight;
                    // intersection of request with given tile
                    final RasterRect intersectionWithTile = RasterRect.intersection( tileRect, requestedRect );
                    if ( intersectionWithTile != null ) {
                        // an intersection, get the raster tile
                        final RasterData tile = tiles[row * columns + col];
                        if ( tile != null ) {
                            final int widthOffset = ( intersectionWithTile.x / tileWidth );
                            final int heightOffset = ( intersectionWithTile.y / tileHeight );
                            // map to the given tile coordinate system.
                            final RasterRect tReq = new RasterRect( ( intersectionWithTile.x - widthOffset ),
                                                                    ( intersectionWithTile.y - heightOffset ),
                                                                    intersectionWithTile.width,
                                                                    intersectionWithTile.height );
                            // get the result
                            short[] tileResult = null;
                            synchronized ( tiles ) {
                                tileResult = tile.getShorts( tReq.x, tReq.y, tReq.width, tReq.height, band, null );
                            }

                            // calculate the offset in the result array.
                            final int offsetInResult = ( intersectionWithTile.y * requestedRect.width )
                                                       + intersectionWithTile.x;
                            // copy the result with the data from the tile.
                            for ( int yResult = 0; yResult < intersectionWithTile.height; ++yResult ) {
                                // one band, so row copying is available
                                System.arraycopy( tileResult, yResult * tReq.width, resultValues,
                                                  ( yResult * requestedRect.width ) + offsetInResult,
                                                  intersectionWithTile.width );
                            }
                        }
                    }
                }
            }
        }
        return resultValues;
    }

    @Override
    public int getColumns() {
        return sampleDomain.width;
    }

    @Override
    public void setBytePixel( int x, int y, byte[] pixel ) {
        for ( int band = 0; band < dataInfo.bands; band++ ) {
            setByteSample( x, y, band, pixel[band] );
        }
    }

    @Override
    public void setByteSample( int x, int y, int band, byte value ) {
        MappedTile tile = getTile( x, y );
        if ( tile != null ) {
            synchronized ( tiles ) {
                tile.data.setByteSample( tile.mappedX, tile.mappedY, band, value );
            }
        }
    }

    @Override
    public void setBytes( int x, int y, int width, int height, int band, byte[] values ) {
        RasterRect requestedRect = new RasterRect( x, y, width, height );
        int[] interSectingTiles = getIntersectingTiles( requestedRect );
        if ( interSectingTiles != null ) {
            final RasterRect tileRect = new RasterRect( 0, 0, tileWidth, tileHeight );
            for ( int row = interSectingTiles[1]; row < interSectingTiles[3]; ++row ) {
                for ( int col = interSectingTiles[0]; col < interSectingTiles[2]; ++col ) {
                    // rect of tile
                    tileRect.x = col * tileWidth;
                    tileRect.y = row * tileHeight;
                    // intersection of request with given tile
                    final RasterRect intersectionWithTile = RasterRect.intersection( tileRect, requestedRect );
                    if ( intersectionWithTile != null ) {
                        // get the raster tile
                        final RasterData tile = tiles[row * columns + col];
                        if ( tile != null ) {
                            // make sure it exists.
                            // get the result
                            final byte[] tileValues = new byte[intersectionWithTile.width * intersectionWithTile.height];

                            // calculate the offset in the given array.
                            final int offsetInResult = ( intersectionWithTile.y * requestedRect.width )
                                                       + intersectionWithTile.x;
                            // copy the result with the data from the tile.
                            for ( int yResult = 0; yResult < intersectionWithTile.height; ++yResult ) {
                                // one band, so row copying is available
                                System.arraycopy( values, ( yResult * requestedRect.width ) + offsetInResult,
                                                  tileValues, yResult * intersectionWithTile.width,
                                                  intersectionWithTile.width );
                            }

                            final int widthOffset = ( intersectionWithTile.x / tileWidth );
                            final int heightOffset = ( intersectionWithTile.y / tileHeight );
                            // map to the given tile coordinate system.
                            final RasterRect tReq = new RasterRect( ( intersectionWithTile.x - widthOffset ),
                                                                    ( intersectionWithTile.y - heightOffset ),
                                                                    intersectionWithTile.width,
                                                                    intersectionWithTile.height );
                            synchronized ( tiles ) {
                                tile.setBytes( tReq.x, tReq.y, tileRect.width, tileRect.height, band, tileValues );
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setDoublePixel( int x, int y, double[] pixel ) {
        for ( int band = 0; band < dataInfo.bands; band++ ) {
            setDoubleSample( x, y, band, pixel[band] );
        }
    }

    @Override
    public void setDoubleSample( int x, int y, int band, double value ) {
        MappedTile tile = getTile( x, y );
        if ( tile != null ) {
            synchronized ( tiles ) {
                tile.data.setDoubleSample( tile.mappedX, tile.mappedY, band, value );
            }
        }
    }

    @Override
    public void setDoubles( int x, int y, int width, int height, int band, double[] values ) {
        RasterRect requestedRect = new RasterRect( x, y, width, height );
        int[] interSectingTiles = getIntersectingTiles( requestedRect );
        if ( interSectingTiles != null ) {
            final RasterRect tileRect = new RasterRect( 0, 0, tileWidth, tileHeight );
            for ( int row = interSectingTiles[1]; row < interSectingTiles[3]; ++row ) {
                for ( int col = interSectingTiles[0]; col < interSectingTiles[2]; ++col ) {
                    // rect of tile
                    tileRect.x = col * tileWidth;
                    tileRect.y = row * tileHeight;
                    // intersection of request with given tile
                    final RasterRect intersectionWithTile = RasterRect.intersection( tileRect, requestedRect );
                    if ( intersectionWithTile != null ) {
                        // get the raster tile
                        final RasterData tile = tiles[row * columns + col];
                        if ( tile != null ) {
                            // make sure it exists.
                            // get the result
                            final double[] tileValues = new double[intersectionWithTile.width
                                                                   * intersectionWithTile.height];

                            // calculate the offset in the given array.
                            final int offsetInResult = ( intersectionWithTile.y * requestedRect.width )
                                                       + intersectionWithTile.x;
                            // copy the result with the data from the tile.
                            for ( int yResult = 0; yResult < intersectionWithTile.height; ++yResult ) {
                                // one band, so row copying is available
                                System.arraycopy( values, ( yResult * requestedRect.width ) + offsetInResult,
                                                  tileValues, yResult * intersectionWithTile.width,
                                                  intersectionWithTile.width );
                            }

                            final int widthOffset = ( intersectionWithTile.x / tileWidth );
                            final int heightOffset = ( intersectionWithTile.y / tileHeight );
                            // map to the given tile coordinate system.
                            final RasterRect tReq = new RasterRect( ( intersectionWithTile.x - widthOffset ),
                                                                    ( intersectionWithTile.y - heightOffset ),
                                                                    intersectionWithTile.width,
                                                                    intersectionWithTile.height );
                            synchronized ( tiles ) {
                                tile.setDoubles( tReq.x, tReq.y, tileRect.width, tileRect.height, band, tileValues );
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setFloatPixel( int x, int y, float[] pixel ) {
        for ( int band = 0; band < dataInfo.bands; band++ ) {
            setFloatSample( x, y, band, pixel[band] );
        }

    }

    @Override
    public void setFloatSample( int x, int y, int band, float value ) {
        MappedTile tile = getTile( x, y );
        if ( tile != null ) {
            synchronized ( tiles ) {
                tile.data.setFloatSample( tile.mappedX, tile.mappedY, band, value );
            }
        }
    }

    @Override
    public void setFloats( int x, int y, int width, int height, int band, float[] values ) {
        RasterRect requestedRect = new RasterRect( x, y, width, height );
        int[] interSectingTiles = getIntersectingTiles( requestedRect );
        if ( interSectingTiles != null ) {
            final RasterRect tileRect = new RasterRect( 0, 0, tileWidth, tileHeight );
            for ( int row = interSectingTiles[1]; row < interSectingTiles[3]; ++row ) {
                for ( int col = interSectingTiles[0]; col < interSectingTiles[2]; ++col ) {
                    // rect of tile
                    tileRect.x = col * tileWidth;
                    tileRect.y = row * tileHeight;
                    // intersection of request with given tile
                    final RasterRect intersectionWithTile = RasterRect.intersection( tileRect, requestedRect );
                    if ( intersectionWithTile != null ) {
                        // get the raster tile
                        final RasterData tile = tiles[row * columns + col];
                        if ( tile != null ) {
                            // make sure it exists.
                            // get the result
                            final float[] tileValues = new float[intersectionWithTile.width
                                                                 * intersectionWithTile.height];

                            // calculate the offset in the given array.
                            final int offsetInResult = ( intersectionWithTile.y * requestedRect.width )
                                                       + intersectionWithTile.x;
                            // copy the result with the data from the tile.
                            for ( int yResult = 0; yResult < intersectionWithTile.height; ++yResult ) {
                                // one band, so row copying is available
                                System.arraycopy( values, ( yResult * requestedRect.width ) + offsetInResult,
                                                  tileValues, yResult * intersectionWithTile.width,
                                                  intersectionWithTile.width );
                            }

                            final int widthOffset = ( intersectionWithTile.x / tileWidth );
                            final int heightOffset = ( intersectionWithTile.y / tileHeight );
                            // map to the given tile coordinate system.
                            final RasterRect tReq = new RasterRect( ( intersectionWithTile.x - widthOffset ),
                                                                    ( intersectionWithTile.y - heightOffset ),
                                                                    intersectionWithTile.width,
                                                                    intersectionWithTile.height );
                            synchronized ( tiles ) {
                                tile.setFloats( tReq.x, tReq.y, tileRect.width, tileRect.height, band, tileValues );
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setIntPixel( int x, int y, int[] pixel ) {
        for ( int band = 0; band < dataInfo.bands; band++ ) {
            setIntSample( x, y, band, pixel[band] );
        }
    }

    @Override
    public void setIntSample( int x, int y, int band, int value ) {
        MappedTile tile = getTile( x, y );
        if ( tile != null ) {
            synchronized ( tiles ) {
                tile.data.setIntSample( tile.mappedX, tile.mappedY, band, value );
            }
        }
    }

    @Override
    public void setInts( int x, int y, int width, int height, int band, int[] values ) {
        RasterRect requestedRect = new RasterRect( x, y, width, height );
        int[] interSectingTiles = getIntersectingTiles( requestedRect );
        if ( interSectingTiles != null ) {
            RasterRect tileRect = new RasterRect( 0, 0, tileWidth, tileHeight );
            for ( int row = interSectingTiles[1]; row < interSectingTiles[3]; ++row ) {
                for ( int col = interSectingTiles[0]; col < interSectingTiles[2]; ++col ) {
                    // rect of tile
                    tileRect.x = col * tileWidth;
                    tileRect.y = row * tileHeight;
                    // intersection of request with given tile
                    final RasterRect intersectionWithTile = RasterRect.intersection( tileRect, requestedRect );
                    if ( intersectionWithTile != null ) {
                        // get the raster tile
                        final RasterData tile = tiles[row * columns + col];
                        if ( tile != null ) {
                            // make sure it exists.
                            // get the result
                            final int[] tileValues = new int[intersectionWithTile.width * intersectionWithTile.height];

                            // calculate the offset in the given array.
                            final int offsetInResult = ( intersectionWithTile.y * requestedRect.width )
                                                       + intersectionWithTile.x;
                            // copy the result with the data from the tile.
                            for ( int yResult = 0; yResult < intersectionWithTile.height; ++yResult ) {
                                // one band, so row copying is available
                                System.arraycopy( values, ( yResult * requestedRect.width ) + offsetInResult,
                                                  tileValues, yResult * intersectionWithTile.width,
                                                  intersectionWithTile.width );
                            }

                            final int widthOffset = ( intersectionWithTile.x / tileWidth );
                            final int heightOffset = ( intersectionWithTile.y / tileHeight );
                            // map to the given tile coordinate system.
                            final RasterRect tReq = new RasterRect( ( intersectionWithTile.x - widthOffset ),
                                                                    ( intersectionWithTile.y - heightOffset ),
                                                                    intersectionWithTile.width,
                                                                    intersectionWithTile.height );
                            synchronized ( tiles ) {
                                tile.setInts( tReq.x, tReq.y, tileRect.width, tileRect.height, band, tileValues );
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setNoDataValue( byte[] values ) {
        dataInfo.setNoDataPixel( null );
    }

    @Override
    public void setPixel( int x, int y, byte[] pixel ) {
        if ( pixel == null || pixel.length < dataInfo.bands * dataInfo.dataSize ) {
            return;
        }
        byte bandResult[] = new byte[dataInfo.dataSize];
        for ( int band = 0; band < dataInfo.bands; band++ ) {
            System.arraycopy( pixel, band * dataInfo.dataSize, bandResult, 0, dataInfo.dataSize );
            setSample( x, y, band, bandResult );
        }
    }

    @Override
    public void setSample( int x, int y, int band, byte[] values ) {
        if ( values == null || values.length < ( dataInfo.bands * dataInfo.dataSize ) ) {
            return;
        }
        MappedTile tile = getTile( x, y );
        if ( tile != null ) {
            synchronized ( tiles ) {
                tile.data.setSample( tile.mappedX, tile.mappedY, band, values );
            }
        }
    }

    @Override
    public void setShortPixel( int x, int y, short[] pixel ) {
        for ( int band = 0; band < dataInfo.bands; band++ ) {
            setShortSample( x, y, band, pixel[band] );
        }
    }

    @Override
    public void setShortSample( int x, int y, int band, short value ) {
        MappedTile tile = getTile( x, y );
        if ( tile != null ) {
            synchronized ( tiles ) {
                tile.data.setShortSample( tile.mappedX, tile.mappedY, band, value );
            }
        }
    }

    @Override
    public void setShorts( int x, int y, int width, int height, int band, short[] values ) {
        RasterRect requestedRect = new RasterRect( x, y, width, height );
        int[] interSectingTiles = getIntersectingTiles( requestedRect );
        if ( interSectingTiles != null ) {
            RasterRect tileRect = new RasterRect( 0, 0, tileWidth, tileHeight );
            for ( int row = interSectingTiles[1]; row < interSectingTiles[3]; ++row ) {
                for ( int col = interSectingTiles[0]; col < interSectingTiles[2]; ++col ) {
                    // rect of tile
                    tileRect.x = col * tileWidth;
                    tileRect.y = row * tileHeight;
                    // intersection of request with given tile
                    final RasterRect intersectionWithTile = RasterRect.intersection( tileRect, requestedRect );
                    if ( intersectionWithTile != null ) {
                        // get the raster tile
                        final RasterData tile = tiles[row * columns + col];
                        if ( tile != null ) {
                            // make sure it exists.
                            // get the result
                            final short[] tileValues = new short[intersectionWithTile.width
                                                                 * intersectionWithTile.height];

                            // calculate the offset in the given array.
                            final int offsetInResult = ( intersectionWithTile.y * requestedRect.width )
                                                       + intersectionWithTile.x;
                            // copy the result with the data from the tile.
                            for ( int yResult = 0; yResult < intersectionWithTile.height; ++yResult ) {
                                // one band, so row copying is available
                                System.arraycopy( values, ( yResult * requestedRect.width ) + offsetInResult,
                                                  tileValues, yResult * intersectionWithTile.width,
                                                  intersectionWithTile.width );
                            }

                            final int widthOffset = ( intersectionWithTile.x / tileWidth );
                            final int heightOffset = ( intersectionWithTile.y / tileHeight );
                            // map to the given tile coordinate system.
                            final RasterRect tReq = new RasterRect( ( intersectionWithTile.x - widthOffset ),
                                                                    ( intersectionWithTile.y - heightOffset ),
                                                                    intersectionWithTile.width,
                                                                    intersectionWithTile.height );
                            synchronized ( tiles ) {
                                tile.setShorts( tReq.x, tReq.y, tileRect.width, tileRect.height, band, tileValues );
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public RasterData getSubset( RasterRect rasterRect ) {
        throw new UnsupportedOperationException(
                                                 "Getting subset is undefined for the TiledRaster data. Use pixel operations instead." );
    }

    @Override
    public RasterData getSubset( RasterRect rasterRect, BandType[] bands ) {
        throw new UnsupportedOperationException(
                                                 "Getting subset is undefined for the TiledRaster data. Use pixel operations instead." );
    }

    @Override
    public void setSubset( int x, int y, int width, int height, RasterData sourceRaster ) {
        throw new UnsupportedOperationException(
                                                 "Setting subset is undefined for the TiledRaster data. Use pixel operations instead." );
    }

    @Override
    public void setSubset( int x, int y, int width, int height, RasterData sourceRaster, int xOffset, int yOffset ) {
        throw new UnsupportedOperationException(
                                                 "Setting subset is undefined for the TiledRaster data. Use pixel operations instead." );
    }

    @Override
    public void setSubset( int x, int y, int width, int height, int dstBand, RasterData sourceRaster, int srcBand ) {
        throw new UnsupportedOperationException(
                                                 "Setting subset is undefined for the TiledRaster data. Use pixel operations instead." );
    }

    @Override
    public void setSubset( int x, int y, int width, int height, int dstBand, RasterData sourceRaster, int srcBand,
                           int xOffset, int yOffset ) {
        throw new UnsupportedOperationException(
                                                 "Setting subset is undefined for the TiledRaster data. Use pixel operations instead." );
    }

    /**
     * 
     * little result bean.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author$
     * @version $Revision$, $Date$
     * 
     */
    private static class MappedTile {
        final RasterData data;

        final int mappedX;

        final int mappedY;

        /**
         * @param rasterData
         * @param mappedX
         *            x coordinate in the given tile
         * @param mappedY
         *            y coordinate in the given tile
         */
        MappedTile( RasterData rasterData, int mappedX, int mappedY ) {
            this.data = rasterData;
            this.mappedX = mappedX;
            this.mappedY = mappedY;
        }

    }

    @Override
    public void dispose() {
        if ( tiles != null ) {
            synchronized ( tiles ) {
                for ( int i = 0; i < tiles.length; ++i ) {
                    if ( tiles[i] != null ) {
                        tiles[i].dispose();
                    }
                }
            }
        }
    }

}
