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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.geometry.Envelope;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreTransaction;

/**
 * {@link TileStore} implementation that overlays two or more {@link TileStore} instances.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
class OverlayTileStore implements TileStore {

    private final Map<String, TileMatrixSet> idToMatrixSet;

    private final Map<String, List<TileStore>> idToTileStore;

    private final OverlayTileStoreMetadata metadata;

    /**
     * Creates a new {@link OverlayTileStore} instance.
     * 
     * @param idToMatrixSet
     *            tile matrix set id to matrix set mapping, must not be <code>null</code>
     * @param idToTileStore
     *            tile store id to tile store mapping, must not be <code>null</code>
     * @param metadata
     *            resource metadata, must not be <code>null</code>
     */
    OverlayTileStore( final Map<String, TileMatrixSet> idToMatrixSet, final Map<String, List<TileStore>> idToTileStore,
                      final OverlayTileStoreMetadata metadata ) {
        this.idToMatrixSet = idToMatrixSet;
        this.idToTileStore = idToTileStore;
        this.metadata = metadata;
    }

    @Override
    public void destroy() {
    }

    @Override
    public OverlayTileStoreMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void init() {
    }

    @Override
    public TileStoreTransaction acquireTransaction( final String id ) {
        throw new UnsupportedOperationException( "OverlayTileStore does not support transactions." );
    }

    @Override
    public Tile getTile( final String tileDataSet, final String tileDataLevel, final int x, final int y ) {
        final List<Tile> tiles = new ArrayList<Tile>();
        for ( final TileStore tileStore : idToTileStore.get( tileDataSet ) ) {
            tiles.add( tileStore.getTile( tileDataSet, tileDataLevel, x, y ) );
        }
        return new OverlayTile( tiles );
    }

    @Override
    public TileDataSet getTileDataSet( final String tileDataSet ) {
        final List<TileDataSet> tileDataSets = new ArrayList<TileDataSet>();
        for ( final TileStore tileStore : idToTileStore.get( tileDataSet ) ) {
            tileDataSets.add( tileStore.getTileDataSet( tileDataSet ) );
        }
        return new OverlayTileDataSet( tileDataSets, idToMatrixSet.get( tileDataSet ) );
    }

    @Override
    public Collection<String> getTileDataSetIds() {
        return idToTileStore.keySet();
    }

    @Override
    public Iterator<Tile> getTiles( final String tileDataSet, final Envelope envelope, final double resolution ) {
        return getTileDataSet( tileDataSet ).getTiles( envelope, resolution );
    }
}
