package org.deegree.spring.bootstrap;

import java.net.URL;

import org.deegree.spring.ApplicationContextHolder;
import org.deegree.spring.ApplicationContextHolderProvider;

import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

public class BootstrapApplicationContextHolderProvider extends ApplicationContextHolderProvider {

    private static final String CONFIG_NS = "http://www.deegree.org/spring/bootstrap";

    static final URL CONFIG_SCHEMA = BootstrapApplicationContextHolderProvider.class.getResource( "/META-INF/schemas/spring/3.4.0/bootstrap.xsd" );

    @Override
    public String getNamespace() {
        return CONFIG_NS;
    }

    @Override
    public ResourceMetadata<ApplicationContextHolder> createFromLocation( Workspace workspace,
                                                                          ResourceLocation<ApplicationContextHolder> location ) {
        return new BootstrapApplicationContextHolderMetadata( workspace, location, this );
    }

    @Override
    public URL getSchema() {
        return CONFIG_SCHEMA;
    }

}
