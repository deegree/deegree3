package org.deegree.tile.persistence.merge;

import java.net.URL;

import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

public class MergingTileStoreProvider extends TileStoreProvider {

    private static final URL SCHEMA = MergingTileStoreProvider.class.getResource( "/META-INF/schemas/datasource/tile/merge/3.4.0/merge.xsd" );

    @Override
    public String getNamespace() {
        return "http://www.deegree.org/datasource/tile/merge";
    }

    @Override
    public ResourceMetadata<TileStore> createFromLocation( Workspace workspace, ResourceLocation<TileStore> location ) {
        return new MergingTileStoreMetadata( workspace, location, this );
    }

    @Override
    public URL getSchema() {
        return SCHEMA;
    }
}
