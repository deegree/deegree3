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

package org.deegree.coverage.raster.container;

import java.io.IOException;

import org.deegree.coverage.raster.AbstractRaster;
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

    public GriddedMemoryTileContainer( Envelope envelope, int rows, int columns, int tileSamplesX, int tileSamplesY,
                                       AbstractRaster[] cells ) {

        super( envelope, rows, columns, tileSamplesX, tileSamplesY );
        this.cells = cells;
    }

    @Override
    public AbstractRaster getTile( int rowId, int columnId ) {
        return cells[getTileId( columnId, rowId )];
    }    
    
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

        return new GriddedMemoryTileContainer( orig.getEnvelope(), rows, columns, orig.tileSamplesX, orig.tileSamplesY,
                                               cells );
    }
}
