package org.deegree.spring.services;

import java.net.URL;

import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.metadata.provider.OWSMetadataProviderProvider;
import org.deegree.spring.GenericSpringResourceMetadata;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

public class SpringOWSMetadataProviderProvider extends OWSMetadataProviderProvider {

    private static final String CONFIG_NS = "http://www.deegree.org/spring/metadata";

    private static final URL CONFIG_SCHEMA = SpringOWSMetadataProviderProvider.class.getResource( "/META-INF/schemas/spring/3.4.0/metadata.xsd" );

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.spring.metadata.jaxb";

    @Override
    public String getNamespace() {
        return CONFIG_NS;
    }

    @Override
    public ResourceMetadata<OWSMetadataProvider> createFromLocation( Workspace workspace,
                                                                     ResourceLocation<OWSMetadataProvider> location ) {

        return new GenericSpringResourceMetadata<OWSMetadataProvider>( workspace, location, this, CONFIG_JAXB_PACKAGE,
                                                                       CONFIG_SCHEMA, OWSMetadataProvider.class );
    }

    @Override
    public URL getSchema() {
        return CONFIG_SCHEMA;
    }
}
