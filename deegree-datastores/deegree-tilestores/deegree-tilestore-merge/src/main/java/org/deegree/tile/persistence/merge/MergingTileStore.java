package org.deegree.tile.persistence.merge;

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
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;

public class MergingTileStore implements TileStore {

    private final Map<String, TileMatrixSet> matrixSets;

    private final Map<String, List<TileStore>> tileStores;

    private final ResourceMetadata<TileStore> metadata;

    public MergingTileStore( Map<String, TileMatrixSet> matrixSets, Map<String, List<TileStore>> tileStores,
                             ResourceMetadata<TileStore> metadata ) {
        this.matrixSets = matrixSets;
        this.tileStores = tileStores;
        this.metadata = metadata;
    }

    @Override
    public void destroy() {

    }

    @Override
    public ResourceMetadata<? extends Resource> getMetadata() {
        return metadata;
    }

    @Override
    public void init() {

    }

    @Override
    public TileStoreTransaction acquireTransaction( String id ) {
        throw new UnsupportedOperationException( "MergingTileStore does not support transactions." );
    }

    @Override
    public Tile getTile( String tileDataSet, String tileDataLevel, int x, int y ) {
        List<Tile> tiles = new ArrayList<Tile>();

        for ( TileStore tileStore : tileStores.get( tileDataSet ) ) {
            tiles.add( tileStore.getTile( tileDataSet, tileDataLevel, x, y ) );
        }

        return new MergingTile( tiles );
    }

    @Override
    public TileDataSet getTileDataSet( String tileDataSet ) {
        List<TileDataSet> tileDataSets = new ArrayList<TileDataSet>();

        for ( TileStore tileStore : tileStores.get( tileDataSet ) ) {
            tileDataSets.add( tileStore.getTileDataSet( tileDataSet ) );
        }

        return new MergingTileDataSet( tileDataSets, matrixSets.get( tileDataSet ) );
    }

    @Override
    public Collection<String> getTileDataSetIds() {
        return tileStores.keySet();
    }

    @Override
    public Iterator<Tile> getTiles( String tileDataSet, Envelope envelope, double resolution ) {
        return getTileDataSet( tileDataSet ).getTiles( envelope, resolution );
    }
}
