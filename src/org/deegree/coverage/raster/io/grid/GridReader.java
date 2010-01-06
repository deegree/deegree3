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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.cache.ByteBufferPool;
import org.deegree.coverage.raster.data.RasterDataFactory;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.geometry.Envelope;

/**
 * The <code>GridReader</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public abstract class GridReader implements RasterReader {

    // private final static GeometryFactory geomFac = new GeometryFactory();

    /** Holds information on the grid. */
    protected GridMetaInfoFile infoFile;

    private Envelope envelope;

    /**
     * @return the envelope
     */
    public final Envelope getEnvelope() {
        return envelope;
    }

    private int bytesPerTile;

    private int tilesPerBlob;

    private RasterRect rasterRect;

    /** The size of one sample */
    protected int sampleSize;

    private RasterDataInfo rasterDataInfo;

    /**
     * Instantiates this grid reader with the given information.
     * 
     * @param infoFile
     */
    protected synchronized void instantiate( GridMetaInfoFile infoFile ) {
        this.infoFile = infoFile;
        this.envelope = infoFile.getEnvelope( OriginLocation.OUTER );
        this.rasterRect = new RasterRect( 0, 0, infoFile.columns() * infoFile.getTileRasterWidth(),
                                          infoFile.rows() * infoFile.getTileRasterHeight() );
        this.rasterDataInfo = infoFile.getDataInfo();
        // this.envelopeWidth = envelope.getMax().get0() - envelope.getMin().get0();
        // this.envelopeHeight = envelope.getMax().get1() - envelope.getMin().get1();
        // this.envelopeWidth = envelope.getSpan0();
        // this.envelopeHeight = envelope.getSpan1();
        // this.rows = infoFile.rows();
        // this.columns = infoFile.columns();
        // this.tileSamplesX = infoFile.getTileSamplesX();
        // this.tileSamplesY = infoFile.getTileSamplesY();
        // this.tileWidth = envelope.getSpan0() / infoFile.columns();
        // this.tileHeight = envelope.getSpan1() / infoFile.rows();
        this.tilesPerBlob = infoFile.columns() * infoFile.rows();
        this.sampleSize = ( rasterDataInfo.getDataType().getSize() * rasterDataInfo.bands() );
        this.bytesPerTile = infoFile.getTileRasterWidth() * infoFile.getTileRasterHeight() * sampleSize;
    }

    /**
     * Get intersection of the requested rectangle with the rectangle of the grid file.
     * 
     * @param original
     * @return the intersection with the grids raster rectangle and the given.
     */
    protected RasterRect snapToGrid( RasterRect original ) {
        return RasterRect.intersection( rasterRect, original );
    }

    /**
     * Calculates the id for a tile at a given position in the grid.
     * 
     * @param columnId
     *            column id, must be in the range [0 ... #columns - 1]
     * @param rowId
     *            row id, must be in the range [0 ... #rows - 1]
     * @return the tile's id
     */
    protected int getTileId( int columnId, int rowId ) {
        int idx = rowId * infoFile.columns() + columnId;
        return idx;
    }

    /**
     * Calculates the envelope for a tile at a given position in the grid.
     * 
     * @param column
     *            column id, must be in the range [0 ... #columns - 1]
     * @param row
     *            row id, must be in the range [0 ... #rows - 1]
     * @return the tile's envelope
     */
    protected Envelope getTileEnvelope( int column, int row ) {
        int xOffset = column * infoFile.getTileRasterWidth();
        int yOffset = row * infoFile.getTileRasterHeight();

        RasterRect rect = new RasterRect( xOffset, yOffset, infoFile.getTileRasterWidth(),
                                          infoFile.getTileRasterHeight() );
        return infoFile.getGeoReference().getEnvelope( rect, null );
        // double xOffset = columnId * tileWidth;
        // double yOffset = ( infoFile.rows() - rowId - 1 ) * tileHeight;
        //
        // double minX = envelope.getMin().get0() + xOffset;
        // double minY = envelope.getMin().get1() + yOffset;
        // double maxX = minX + tileWidth;
        // double maxY = minY + tileHeight;
        //
        // return geomFac.createEnvelope( minX, minY, maxX, maxY, envelope.getCoordinateSystem() );
    }

    /**
     * Read a raster from the grid file at location (row,column).
     * 
     * @param columnId
     * 
     * @param rowId
     * 
     * @return the read raster or null if it could not be read.
     * @throws IOException
     */
    public AbstractRaster getTile( int columnId, int rowId )
                            throws IOException {

        Envelope tileEnvelope = getTileEnvelope( columnId, rowId );

        RasterGeoReference tileRasterReference = RasterGeoReference.create( OriginLocation.OUTER, tileEnvelope,
                                                                            infoFile.getTileRasterWidth(),
                                                                            infoFile.getTileRasterHeight() );

        RasterRect tileRect = getGeoReference().createRelocatedReference( OriginLocation.OUTER ).convertEnvelopeToRasterCRS(
                                                                                                                             tileEnvelope );
        TileOffsetReader tReader = new TileOffsetReader( this, tileRect );
        ByteBufferRasterData tileData = RasterDataFactory.createRasterData( infoFile.getTileRasterWidth(),
                                                                            infoFile.getTileRasterHeight(),
                                                                            getRasterDataInfo(), tReader, false );
        SimpleRaster tile = new SimpleRaster( tileData, tileEnvelope, tileRasterReference );
        return tile;
    }

    /**
     * Reads the data from the grid.
     * 
     * @param columnId
     * @param rowId
     * @param buffer
     * @throws IOException
     */
    protected abstract void read( int columnId, int rowId, ByteBuffer buffer )
                            throws IOException;

    @Override
    public AbstractRaster load( InputStream stream, RasterIOOptions options )
                            throws IOException {
        throw new UnsupportedOperationException( "Reading from streams is currently not supported." );
    }

    /**
     * @return the number of tiles in this blob.
     */
    public int getNumberOfTiles() {
        return this.getTilesPerBlob();
    }

    /**
     * @return the number of bytes one tile in the given grid has.
     */
    public long getBytesPerTile() {
        return this.bytesPerTile;
    }

    @Override
    public RasterGeoReference getGeoReference() {
        return infoFile.getGeoReference();
    }

    @Override
    public int getHeight() {
        return this.rasterRect.height;
    }

    @Override
    public int getWidth() {
        return this.rasterRect.width;
    }

    /**
     * @return the width of a tile in raster coordinates.
     */
    public int getTileRasterWidth() {
        return this.infoFile.getTileRasterWidth();
    }

    /**
     * @return the height of a tile in raster coordinates.
     */
    public int getTileRasterHeight() {
        return this.infoFile.getTileRasterHeight();
    }

    @Override
    public boolean canReadTiles() {
        return true;
    }

    /**
     * Returns the min column, row and max column row of the given rect. The rectangle will be cut off to fit the data.
     * If the rect does not intersect the data, <code>null</code> will be returned.
     * 
     * @param rect
     * @return {min column, min row, max column, max row} or <code>null</code> if the given rect does not intersect the
     *         data.
     */
    protected int[] getIntersectingTiles( RasterRect rect ) {
        RasterRect fRect = snapToGrid( rect );
        if ( fRect != null ) {
            int minCol = getColNumber( fRect.x );
            int minRow = getRowNumber( fRect.y );
            int maxCol = getColNumber( fRect.x + fRect.width );
            int maxRow = getRowNumber( fRect.y + fRect.height );
            if ( ( maxCol != -1 ) && ( maxRow != -1 ) && ( minCol != infoFile.columns() )
                 && ( minRow != infoFile.rows() ) ) {
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
        int row = (int) Math.floor( rasterCoord / infoFile.getTileRasterHeight() );
        if ( row < 0 ) {
            row = -1;
        }
        if ( row >= infoFile.rows() ) {
            row = infoFile.rows();
        }
        return row;
    }

    /**
     * @param rasterCoord
     *            in raster coordinates normally x.
     * @return the column number of tile which holds the given raster coordinate.
     */
    private int getColNumber( float rasterCoord ) {
        int column = (int) Math.floor( rasterCoord / infoFile.getTileRasterWidth() );
        if ( column < 0 ) {
            column = -1;
        }
        if ( column >= infoFile.columns() ) {
            column = infoFile.columns();
        }
        return column;
    }

    @Override
    public boolean shouldCreateCacheFile() {
        return false;
    }

    /**
     * @param column
     * @param row
     * @param buffer
     * @return a newly allocated buffer if the given one was <code>null</code>
     * @throws IOException
     */
    public ByteBuffer getTileData( int column, int row, ByteBuffer buffer )
                            throws IOException {
        if ( buffer == null ) {
            buffer = allocateTileBuffer( false, false );
        }
        read( column, row, buffer );

        return buffer;
    }

    /**
     * Allocate a buffer which can hold a tile.
     * 
     * @param direct
     *            if the buffer should be direct
     * @param forCache
     *            if the buffer is used for caching mechanisms.
     * 
     * @return a ByteBuffer which can hold a tile.
     */
    protected ByteBuffer allocateTileBuffer( boolean direct, boolean forCache ) {
        return ByteBufferPool.allocate( sampleSize * infoFile.getTileRasterHeight() * infoFile.getTileRasterWidth(),
                                        direct, forCache );
    }

    /**
     * @return the number of grid rows
     */
    public int getTileRows() {
        return infoFile.rows();
    }

    /**
     * @return the number of grid columns
     */
    public int getTileColumns() {
        return infoFile.columns();
    }

    /**
     * @return the tilesPerBlob
     */
    public int getTilesPerBlob() {
        return tilesPerBlob;
    }

    /**
     * @param tilesPerBlob
     */
    public void setTilesPerBlob( int tilesPerBlob ) {
        this.tilesPerBlob = tilesPerBlob;
    }

    @Override
    public RasterDataInfo getRasterDataInfo() {
        return rasterDataInfo;
    }

}
