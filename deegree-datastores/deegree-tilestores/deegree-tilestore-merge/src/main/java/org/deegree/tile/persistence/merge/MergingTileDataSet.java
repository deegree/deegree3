package org.deegree.tile.persistence.merge;

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

public class MergingTileDataSet implements TileDataSet {

    private final List<TileDataSet> tileDataSets;

    private final TileMatrixSet tileMatrixSet;

    private final Map<String, TileMatrix> tileMatrices;

    public MergingTileDataSet( List<TileDataSet> tileDataSets, TileMatrixSet tileMatrixSet ) {
        this.tileDataSets = tileDataSets;
        this.tileMatrixSet = tileMatrixSet;

        tileMatrices = new HashMap<String, TileMatrix>();
        for ( TileMatrix tileMatrix : tileMatrixSet.getTileMatrices() ) {
            tileMatrices.put( tileMatrix.getIdentifier(), tileMatrix );
        }
    }

    @Override
    public String getNativeImageFormat() {
        return "image/jpeg";
    }

    @Override
    public TileDataLevel getTileDataLevel( String identifier ) {

        ArrayList<TileDataLevel> tileDataLevels = new ArrayList<TileDataLevel>();

        for ( TileDataSet tileDataSet : tileDataSets ) {
            tileDataLevels.add( tileDataSet.getTileDataLevel( identifier ) );
        }

        return new MergingTileDataLevel( tileDataLevels, tileMatrices.get( identifier ) );
    }

    @Override
    public List<TileDataLevel> getTileDataLevels() {
        List<TileDataLevel> tileDataLevels = new ArrayList<TileDataLevel>();

        for ( TileMatrix tileMatrix : tileMatrixSet.getTileMatrices() ) {
            List<TileDataLevel> levelTileDataLevels = new ArrayList<TileDataLevel>();

            for ( TileDataSet tileDataSet : tileDataSets ) {
                tileDataLevels.add( tileDataSet.getTileDataLevel( tileMatrix.getIdentifier() ) );
            }

            tileDataLevels.add( new MergingTileDataLevel( levelTileDataLevels, tileMatrix ) );
        }

        return tileDataLevels;
    }

    @Override
    public TileMatrixSet getTileMatrixSet() {
        return tileMatrixSet;
    }

    @Override
    public Iterator<Tile> getTiles( Envelope envelope, double resolution ) {
        final ArrayList<Iterator<Tile>> iterators = new ArrayList<Iterator<Tile>>();
        for ( TileDataSet tileDataSet : tileDataSets ) {
            iterators.add( tileDataSet.getTiles( envelope, resolution ) );
        }

        return new Iterator<Tile>() {

            @Override
            public boolean hasNext() {
                for ( Iterator<Tile> iterator : iterators ) {
                    return iterator.hasNext();
                }

                return false;
            }

            @Override
            public Tile next() {
                ArrayList<Tile> tiles = new ArrayList<Tile>();

                for ( Iterator<Tile> iterator : iterators ) {
                    tiles.add( iterator.next() );
                }

                return new MergingTile( tiles );
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
