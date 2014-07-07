/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.overlay;

import java.util.ArrayList;
import java.util.List;

import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileMatrix;

/**
 * {@link TileDataLevel} implementation for the {@link OverlayTileStore}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
class OverlayTileDataLevel implements TileDataLevel {

    private final List<TileDataLevel> tileDataLevels;

    private final TileMatrix tileMatrix;

    /**
     * Creates a new {@link OverlayTileDataLevel} instance.
     * 
     * @param tileDataLevels
     *            matching tile data levels to be overlayed into a single data level, must not be <code>null</code>
     * @param tileMatrix
     *            corresponding tile matrix, must not be <code>null</code>
     */
    OverlayTileDataLevel( final List<TileDataLevel> tileDataLevels, final TileMatrix tileMatrix ) {
        this.tileDataLevels = tileDataLevels;
        this.tileMatrix = tileMatrix;
    }

    @Override
    public TileMatrix getMetadata() {
        return tileMatrix;
    }

    @Override
    public Tile getTile( final long x, final long y ) {
        final List<Tile> tiles = new ArrayList<Tile>();
        for ( final TileDataLevel tileDataLevel : tileDataLevels ) {
            tiles.add( tileDataLevel.getTile( x, y ) );
        }
        return new OverlayTile( tiles );
    }
}
