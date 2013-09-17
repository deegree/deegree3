package org.deegree.tile.persistence.merge;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.slf4j.Logger;

public class MergingTileStoreMetadata extends AbstractResourceMetadata<TileStore> {
    
    private static final Logger LOG = getLogger(MergingTileStoreMetadata.class);

    public MergingTileStoreMetadata( Workspace workspace, ResourceLocation<TileStore> location,
                                     AbstractResourceProvider<TileStore> provider ) {
        super( workspace, location, provider );
    }

    @Override
    public ResourceBuilder<TileStore> prepare() {
        try {
            org.deegree.tile.persistence.merge.jaxb.MergingTileStore cfg;
            cfg = (org.deegree.tile.persistence.merge.jaxb.MergingTileStore) unmarshall( "org.deegree.tile.persistence.merge.jaxb",
                                                                                         provider.getSchema(),
                                                                                         location.getAsStream(),
                                                                                         workspace );
            List<String> tsids = cfg.getTileStoreId();
            for ( String tsid : tsids ) {
                LOG.debug("Adding dependency on TileStore: " + tsid);
                
                dependencies.add( new DefaultResourceIdentifier<TileStore>( TileStoreProvider.class, tsid ) );
            }

            return new MergingTileStoreBuilder( cfg, this, workspace );
        } catch ( Exception e ) {
            throw new ResourceInitException( "Could not prepare tile store: " + e.getLocalizedMessage(), e );
        }
    }
}
