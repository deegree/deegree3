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

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.grid.GridMetaInfoFile;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@link TileContainer}s based on a rectangular grid of disjunct, equal-sized cells (raster
 * tiles).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class GriddedTileContainer implements TileContainer {

    private static Logger LOG = LoggerFactory.getLogger( GriddedTileContainer.class );

    private final Envelope envelope;

    private final double envelopeWidth;

    private final double envelopeHeight;

    private final RasterGeoReference rasterReference;

    private final int rows;

    private final int columns;

    private final double tileWidth;

    private final double tileHeight;

    private final static GeometryFactory geomFac = new GeometryFactory();

    /** Number of samples of each raster tile in x-direction. */
    protected final int tileSamplesX;

    /** Number of samples of each raster tile in y-direction. */
    protected final int tileSamplesY;

    /**
     * Creates a new {@link GriddedTileContainer} instances.
     * 
     * @param infoFile
     *            containing the relevant information.
     * 
     */
    protected GriddedTileContainer( GridMetaInfoFile infoFile ) {
        this.envelope = infoFile.getEnvelope( OriginLocation.OUTER );
        this.envelopeWidth = envelope.getMax().get0() - envelope.getMin().get0();
        this.envelopeHeight = envelope.getMax().get1() - envelope.getMin().get1();
        // this.envelopeWidth = envelope.getSpan0();
        // this.envelopeHeight = envelope.getSpan1();
        this.rows = infoFile.rows();
        this.columns = infoFile.columns();
        this.tileSamplesX = infoFile.getTileRasterWidth();
        this.tileSamplesY = infoFile.getTileRasterHeight();
        this.tileWidth = envelopeWidth / columns;
        this.tileHeight = envelopeHeight / rows;

        this.rasterReference = infoFile.getGeoReference();
        LOG.debug( "envelope: " + envelope );
        LOG.debug( "raster reference: " + rasterReference );
    }

    /**
     * Creates a new {@link GriddedTileContainer} instances.
     * 
     * @param location
     * 
     * @param envelope
     *            area of the samples of the contained tiles
     * @param rows
     *            number of rows in the cell grid
     * @param columns
     *            number of columns in the cell grid
     * @param tileSamplesX
     *            number of samples of each raster tile in x-direction
     * @param tileSamplesY
     *            number of samples of each raster tile in y-direction
     */
    protected GriddedTileContainer( OriginLocation location, Envelope envelope, int rows, int columns,
                                    int tileSamplesX, int tileSamplesY ) {
        this.envelope = envelope;
        this.envelopeWidth = envelope.getMax().get0() - envelope.getMin().get0();
        this.envelopeHeight = envelope.getMax().get1() - envelope.getMin().get1();
        this.rows = rows;
        this.columns = columns;
        this.tileSamplesX = tileSamplesX;
        this.tileSamplesY = tileSamplesY;
        this.tileWidth = envelopeWidth / columns;
        this.tileHeight = envelopeHeight / rows;
        this.rasterReference = RasterGeoReference.create( location, envelope, tileSamplesX * columns, tileSamplesY
                                                                                                      * rows );
        LOG.debug( "envelope: " + envelope );
        LOG.debug( "raster reference: " + rasterReference );
    }

    /**
     * Returns the raster tile at the given grid position.
     * 
     * @param rowId
     *            row id, must in the range [0 ... #rows - 1]
     * @param columnId
     *            column id, must be in the range [0 ... #columns - 1]
     * @return the raster tile at the given grid position
     */
    public abstract AbstractRaster getTile( int rowId, int columnId );

    @Override
    public List<AbstractRaster> getTiles( Envelope env ) {

        List<AbstractRaster> tiles = new ArrayList<AbstractRaster>();

        int minColumn = getColumnIdx( env.getMin().get0() );
        int minRow = getRowIdx( env.getMax().get1() );
        int maxColumn = getColumnIdx( env.getMax().get0() );
        int maxRow = getRowIdx( env.getMin().get1() );

        int[] max = rasterReference.getRasterCoordinate( env.getMax().get0(), env.getMin().get1() );
        double[] maxReal = rasterReference.getRasterCoordinateUnrounded( env.getMax().get0(), env.getMin().get1() );
        if ( ( Math.abs( maxReal[0] - max[0] ) < 1E-6 ) && max[0] % tileSamplesX == 0 ) {
            // found an edge, don't use the last tile.
            maxColumn--;
        }
        if ( ( Math.abs( maxReal[1] - max[1] ) < 1E-6 ) && max[1] % tileSamplesY == 0 ) {
            // found an edge, don't use the last tile.
            maxRow--;
        }

        // the requested envelope is outside the boundaries of the data
        if ( ( maxColumn <= -1 ) || ( maxRow <= -1 ) || ( minColumn == columns ) || ( minRow == rows ) ) {
            return tiles;
        }
        // reset values to maximal/minimal allowed
        minColumn = max( minColumn, 0 );
        minRow = max( minRow, 0 );
        maxColumn = min( maxColumn, columns - 1 );
        maxRow = min( maxRow, rows - 1 );

        for ( int rowId = minRow; rowId <= maxRow; rowId++ ) {
            for ( int columnId = minColumn; columnId <= maxColumn; columnId++ ) {
                AbstractRaster rasterTile = getTile( rowId, columnId );
                tiles.add( rasterTile );
            }
        }

        return tiles;
    }

    @Override
    public Envelope getEnvelope() {
        return envelope;
    }

    @Override
    public RasterGeoReference getRasterReference() {
        return rasterReference;
    }

    /**
     * Returns the number of rows of the grid.
     * 
     * @return the number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns the number of columns of the grid.
     * 
     * @return the number of columns
     */
    public int getColumns() {
        return columns;
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
        int idx = rowId * columns + columnId;
        return idx;
    }

    /**
     * Calculates the envelope for a tile at a given position in the grid.
     * 
     * @param columnId
     *            column id, must be in the range [0 ... #columns - 1]
     * @param rowId
     *            row id, must be in the range [0 ... #rows - 1]
     * @return the tile's envelope
     */
    protected Envelope getTileEnvelope( int rowId, int columnId ) {
        double xOffset = columnId * tileWidth;
        double yOffset = ( rows - rowId - 1 ) * tileHeight;

        double minX = envelope.getMin().get0() + xOffset;
        double minY = envelope.getMin().get1() + yOffset;
        double maxX = minX + tileWidth;
        double maxY = minY + tileHeight;

        return geomFac.createEnvelope( minX, minY, maxX, maxY, envelope.getCoordinateSystem() );
    }

    private int getColumnIdx( double x ) {
        int[] rasterCoordinate = rasterReference.getRasterCoordinate( x, 0 );
        if ( rasterCoordinate[0] < 0 ) {
            return -1;
        }
        return Math.min( columns, Math.max( -1, ( rasterCoordinate[0] / tileSamplesX ) ) );

        // if ( x < envelope.getMin().get0() || x > envelope.getMax().get0() ) {
        // String msg = "Specified x coordinate (=" + x + ") is out of range [" + envelope.getMin().get0() + ";"
        // + envelope.getMax().get0() + "]";
        // LOG.debug( msg );
        // throw new IllegalArgumentException( msg );
        // }

        // double dx = x - envelope.getMin().get0();
        // int columnIdx = (int) Math.floor( ( columns * dx ) / envelopeWidth );
        // if ( columnIdx < 0 ) {
        // // signal outside
        // return -1;
        // }
        // if ( columnIdx > columns - 1 ) {
        // // signal outside
        // return columns;
        // }
        // return columnIdx;
    }

    private int getRowIdx( double y ) {
        int[] rasterCoordinate = rasterReference.getRasterCoordinate( 0, y );
        if ( rasterCoordinate[1] < 0 ) {
            return -1;
        }
        return Math.min( rows, Math.max( -1, ( rasterCoordinate[1] / tileSamplesY ) ) );

        // if ( y < envelope.getMin().get1() || y > envelope.getMax().get1() ) {
        // String msg = "Specified y coordinate (=" + y + ") is out of range [" + envelope.getMin().get1() + ";"
        // + envelope.getMax().get1() + "]";
        // LOG.debug( msg );
        // throw new IllegalArgumentException( msg );
        // }

        // double dy = y - envelope.getMin().get1();
        // int rowIdx = (int) Math.floor( ( ( rows * ( envelopeHeight - dy ) ) / envelopeHeight ) );
        // if ( rowIdx < 0 ) {
        // // signal outside
        // return -1;
        // }
        // if ( rowIdx > rows - 1 ) {
        // // signal outside
        // return rows;
        // }
        // return rowIdx;
    }
}
