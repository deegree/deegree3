package org.deegree.tile.persistence.merge;

import java.util.ArrayList;
import java.util.List;

import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileMatrix;

public class MergingTileDataLevel implements TileDataLevel {

    private final List<TileDataLevel> tileDataLevels;
    private final TileMatrix tileMatrix;

    public MergingTileDataLevel( List<TileDataLevel> tileDataLevels, TileMatrix tileMatrix ) {
        this.tileDataLevels = tileDataLevels;
        this.tileMatrix = tileMatrix;
    }

    @Override
    public TileMatrix getMetadata() {
        return tileMatrix;
    }

    @Override
    public Tile getTile( long x, long y ) {
        List<Tile> tiles = new ArrayList<Tile>();

        for ( TileDataLevel tileDataLevel : tileDataLevels ) {
            tiles.add( tileDataLevel.getTile( x, y ) );
        }

        return new MergingTile( tiles );
    }
}
