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

import java.io.IOException;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.geometry.Envelope;

/**
 * Concrete implementation of {@link GriddedTileContainer} that keeps the tiles in memory.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GriddedMemoryTileContainer extends GriddedTileContainer {

    private AbstractRaster[] cells;

    /**
     * The memory based gridded tile container.
     * 
     * @param location
     * @param envelope
     * @param rows
     * @param columns
     * @param tileSamplesX
     * @param tileSamplesY
     * @param cells
     */
    public GriddedMemoryTileContainer( OriginLocation location, Envelope envelope, int rows, int columns,
                                       int tileSamplesX, int tileSamplesY, AbstractRaster[] cells ) {

        super( location, envelope, rows, columns, tileSamplesX, tileSamplesY );
        this.cells = cells == null ? null : new AbstractRaster[cells.length];
        // make copy is a better style...
        if ( cells != null ) {
            for ( int i = 0; i < cells.length; ++i ) {
                this.cells[i] = cells[i];
            }
        }
    }

    @Override
    public AbstractRaster getTile( int rowId, int columnId ) {
        return cells[getTileId( columnId, rowId )];
    }

    /**
     * Creates an in-memory gridded tile container from the given tile container.
     * 
     * @param orig
     * @return an in-memory gridded tile container.
     * @throws IOException
     */
    public static GriddedMemoryTileContainer create( GriddedTileContainer orig )
                            throws IOException {

        int rows = orig.getRows();
        int columns = orig.getColumns();
        AbstractRaster[] cells = new AbstractRaster[rows * columns];
        for ( int rowId = 0; rowId < rows; rowId++ ) {
            for ( int columnId = 0; columnId < columns; columnId++ ) {
                cells[orig.getTileId( columnId, rowId )] = orig.getTile( rowId, columnId );
            }
        }

        return new GriddedMemoryTileContainer( orig.getRasterReference().getOriginLocation(), orig.getEnvelope(), rows,
                                               columns, orig.tileSamplesX, orig.tileSamplesY, cells );
    }
}
