package org.deegree.tile.persistence.merge;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

public class MergingTileStoreBuilder implements ResourceBuilder<TileStore> {

    private static final Logger LOG = getLogger( MergingTileStoreMetadata.class );

    private final org.deegree.tile.persistence.merge.jaxb.MergingTileStore cfg;

    private final ResourceMetadata<TileStore> metadata;

    private final Workspace workspace;

    public MergingTileStoreBuilder( org.deegree.tile.persistence.merge.jaxb.MergingTileStore cfg,
                                    ResourceMetadata<TileStore> metadata, Workspace workspace ) {

        this.cfg = cfg;
        this.metadata = metadata;
        this.workspace = workspace;
    }

    @Override
    public TileStore build() {

        Map<String, TileMatrixSet> matrixSets = new HashMap<String, TileMatrixSet>();
        Map<String, List<TileStore>> tileStores = new HashMap<String, List<TileStore>>();

        for ( String tsid : cfg.getTileStoreId() ) {
            LOG.debug( "Processing TileStore: " + tsid );

            TileStore tileStore = workspace.getResource( TileStoreProvider.class, tsid );
            for ( String dsid : tileStore.getTileDataSetIds() ) {
                LOG.debug( "Processing TileDataSet: " + dsid );

                TileDataSet tds = tileStore.getTileDataSet( dsid );
                TileMatrixSet tms = tds.getTileMatrixSet();

                if ( matrixSets.containsKey( dsid ) ) {
                    if ( !matrixSets.get( dsid ).equals( tms ) ) {
                        throw new ResourceInitException( "TileDataSet uses different TileMatrixSet: " + dsid );
                    }
                } else {
                    matrixSets.put( dsid, tms );
                }

                if ( tileStores.containsKey( dsid ) ) {
                    tileStores.get( dsid ).add( tileStore );
                } else {
                    ArrayList<TileStore> tileStoreList = new ArrayList<TileStore>();
                    tileStoreList.add( tileStore );

                    tileStores.put( dsid, tileStoreList );
                }
            }
        }

        return new MergingTileStore( matrixSets, tileStores, metadata );
    }
}
