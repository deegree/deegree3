package org.deegree.spring.db;

import static org.deegree.spring.db.SpringConnectionProviderProvider.CONFIG_SCHEMA;

import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.db.ConnectionProvider;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.deegree.workspace.standard.DefaultResourceIdentifier;

import org.deegree.spring.ApplicationContextHolder;
import org.deegree.spring.ApplicationContextHolderProvider;
import org.deegree.spring.db.jaxb.SpringConnectionProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpringConnectionProviderMetadata extends AbstractResourceMetadata<ConnectionProvider> {

    private static final Logger LOG = LoggerFactory.getLogger( SpringConnectionProviderMetadata.class );

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.spring.db.jaxb";

    public SpringConnectionProviderMetadata( Workspace workspace, ResourceLocation<ConnectionProvider> location,
                                             AbstractResourceProvider<ConnectionProvider> provider ) {
        super( workspace, location, provider );
    }

    @Override
    public SpringConnectionProviderBuilder prepare() {

        final SpringConnectionProviderConfig config;
        try {
            config = (SpringConnectionProviderConfig) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA,
                                                                            location.getAsStream(), workspace );

            final String applicationContextHolder = config.getApplicationContextHolder();
            dependencies.add( new DefaultResourceIdentifier<ApplicationContextHolder>(
                                                                                       ApplicationContextHolderProvider.class,
                                                                                       applicationContextHolder ) );
        } catch ( Exception e ) {
            LOG.trace( "Stack trace:", e );
            throw new ResourceInitException( e.getLocalizedMessage(), e );
        }

        return new SpringConnectionProviderBuilder( this, workspace, config );
    }
}
