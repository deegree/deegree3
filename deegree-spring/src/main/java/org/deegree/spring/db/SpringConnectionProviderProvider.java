package org.deegree.spring.db;

import java.net.URL;

import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;

public class SpringConnectionProviderProvider extends ConnectionProviderProvider {
    
    private static final String CONFIG_NS = "http://www.deegree.org/spring/db";
    static final URL CONFIG_SCHEMA = SpringConnectionProviderProvider.class.getResource( "/META-INF/schemas/spring/3.4.0/db.xsd" );

    @Override
    public String getNamespace() {
        return CONFIG_NS;
    }

    @Override
    public SpringConnectionProviderMetadata createFromLocation( Workspace workspace,
                                                                    ResourceLocation<ConnectionProvider> location ) {
        return new SpringConnectionProviderMetadata( workspace, location, this );
    }

    @Override
    public URL getSchema() {
        return CONFIG_SCHEMA;
    }
}
