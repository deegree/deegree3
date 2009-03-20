//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.model.coverage.raster.container;

import java.util.ArrayList;
import java.util.List;

import org.deegree.model.coverage.raster.AbstractRaster;
import org.deegree.model.coverage.raster.geom.RasterEnvelope;
import org.deegree.model.geometry.Envelope;
import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.GeometryFactoryCreator;
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

    private final RasterEnvelope rasterEnvelope;

    private final int rows;

    private final int columns;

    private final double tileWidth;

    private final double tileHeight;

    private final GeometryFactory geomFac;

    /** Number of samples of each raster tile in x-direction. */
    protected final int tileSamplesX;

    /** Number of samples of each raster tile in y-direction. */
    protected final int tileSamplesY;

    /**
     * Creates a new {@link GriddedTileContainer} instances.
     * 
     * @param envelope
     *            area of the samples of the contained tiles (TODO: OUTER / INNER???)
     * @param rows
     *            number of rows in the cell grid
     * @param columns
     *            number of columns in the cell grid
     * @param tileSamplesX
     *            number of samples of each raster tile in x-direction
     * @param tileSamplesY
     *            number of samples of each raster tile in y-direction
     */
    protected GriddedTileContainer( Envelope envelope, int rows, int columns, int tileSamplesX, int tileSamplesY ) {
        this.envelope = envelope;
        this.envelopeWidth = envelope.getMax().getX() - envelope.getMin().getX();
        this.envelopeHeight = envelope.getMax().getY() - envelope.getMin().getY();
        this.rows = rows;
        this.columns = columns;
        this.tileSamplesX = tileSamplesX;
        this.tileSamplesY = tileSamplesY;
        this.tileWidth = envelopeWidth / columns;
        this.tileHeight = envelopeHeight / rows;
        this.rasterEnvelope = new RasterEnvelope( envelope, tileSamplesX * columns, tileSamplesY * rows );
        geomFac = GeometryFactoryCreator.getInstance().getGeometryFactory();
        LOG.debug( "envelope: " + envelope );
        LOG.debug( "raster envelope: " + rasterEnvelope );
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

        int minColumnId = getColumnIdx( env.getMin().getX() );
        int minRowId = getRowIdx( env.getMax().getY() );
        int maxColumnId = getColumnIdx( env.getMax().getX() );
        int maxRowId = getRowIdx( env.getMin().getY() );

        for ( int rowId = minRowId; rowId <= maxRowId; rowId++ ) {
            for ( int columnId = minColumnId; columnId <= maxColumnId; columnId++ ) {
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
    public RasterEnvelope getRasterEnvelope() {
        return rasterEnvelope;
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

        double minX = envelope.getMin().getX() + xOffset;
        double minY = envelope.getMin().getY() + yOffset;
        double maxX = minX + tileWidth;
        double maxY = minY + tileHeight;

        return geomFac.createEnvelope( minX, minY, maxX, maxY, envelope.getCoordinateSystem() );
    }

    private int getColumnIdx( double x ) {

        if ( x < envelope.getMin().getX() || x > envelope.getMax().getX() ) {
            String msg = "Specified x coordinate (=" + x + ") is out of range [" + envelope.getMin().getX() + ";"
                         + envelope.getMax().getX() + "]";
            throw new IllegalArgumentException( msg );
        }

        double dx = x - envelope.getMin().getX();
        int columnIdx = (int) ( columns * dx / envelopeWidth );
        if ( columnIdx < 0 ) {
            columnIdx = 0;
        }
        if ( columnIdx > columns - 1 ) {
            columnIdx = columns - 1;
        }
        return columnIdx;
    }

    private int getRowIdx( double y ) {

        if ( y < envelope.getMin().getY() || y > envelope.getMax().getY() ) {
            String msg = "Specified y coordinate (=" + y + ") is out of range [" + envelope.getMin().getY() + ";"
                         + envelope.getMax().getY() + "]";
            throw new IllegalArgumentException( msg );
        }

        double dy = y - envelope.getMin().getY();
        int rowIdx = (int) ( rows * ( envelopeHeight - dy ) / envelopeHeight );
        if ( rowIdx < 0 ) {
            rowIdx = 0;
        }
        if ( rowIdx > rows - 1 ) {
            rowIdx = rows - 1;
        }
        return rowIdx;
    }
}
