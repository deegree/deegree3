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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.geometry.Envelope;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;

/**
 * {@link TileDataSet} implementation for the {@link OverlayTileStore}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
class OverlayTileDataSet implements TileDataSet {

    private final List<TileDataSet> tileDataSets;

    private final TileMatrixSet tileMatrixSet;

    private final Map<String, TileMatrix> idToTileMatrix;

    /**
     * Creates a new {@link OverlayTileDataSet} instance.
     * 
     * @param tileDataSets
     *            must not be <code>null</code>
     * @param tileMatrixSet
     *            must not be <code>null</code>
     */
    OverlayTileDataSet( final List<TileDataSet> tileDataSets, final TileMatrixSet tileMatrixSet ) {
        this.tileDataSets = tileDataSets;
        this.tileMatrixSet = tileMatrixSet;
        idToTileMatrix = new HashMap<String, TileMatrix>();
        for ( final TileMatrix tileMatrix : tileMatrixSet.getTileMatrices() ) {
            idToTileMatrix.put( tileMatrix.getIdentifier(), tileMatrix );
        }
    }

    @Override
    public String getNativeImageFormat() {
        return "image/jpeg";
    }

    @Override
    public TileDataLevel getTileDataLevel( final String identifier ) {
        final ArrayList<TileDataLevel> tileDataLevels = new ArrayList<TileDataLevel>();
        for ( final TileDataSet tileDataSet : tileDataSets ) {
            tileDataLevels.add( tileDataSet.getTileDataLevel( identifier ) );
        }
        return new OverlayTileDataLevel( tileDataLevels, idToTileMatrix.get( identifier ) );
    }

    @Override
    public List<TileDataLevel> getTileDataLevels() {
        final List<TileDataLevel> tileDataLevels = new ArrayList<TileDataLevel>();
        for ( final TileMatrix tileMatrix : tileMatrixSet.getTileMatrices() ) {
            final List<TileDataLevel> levelTileDataLevels = new ArrayList<TileDataLevel>();
            for ( final TileDataSet tileDataSet : tileDataSets ) {
                tileDataLevels.add( tileDataSet.getTileDataLevel( tileMatrix.getIdentifier() ) );
            }
            tileDataLevels.add( new OverlayTileDataLevel( levelTileDataLevels, tileMatrix ) );
        }
        return tileDataLevels;
    }

    @Override
    public TileMatrixSet getTileMatrixSet() {
        return tileMatrixSet;
    }

    @Override
    public Iterator<Tile> getTiles( final Envelope envelope, final double resolution ) {
        final ArrayList<Iterator<Tile>> iterators = new ArrayList<Iterator<Tile>>();
        for ( final TileDataSet tileDataSet : tileDataSets ) {
            iterators.add( tileDataSet.getTiles( envelope, resolution ) );
        }
        return new Iterator<Tile>() {

            @Override
            public boolean hasNext() {
                for ( final Iterator<Tile> iterator : iterators ) {
                    return iterator.hasNext();
                }
                return false;
            }

            @Override
            public Tile next() {
                final ArrayList<Tile> tiles = new ArrayList<Tile>();
                for ( final Iterator<Tile> iterator : iterators ) {
                    tiles.add( iterator.next() );
                }
                return new OverlayTile( tiles );
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
