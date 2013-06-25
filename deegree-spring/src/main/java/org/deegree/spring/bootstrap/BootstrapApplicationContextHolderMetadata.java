package org.deegree.spring.bootstrap;

import static org.deegree.spring.bootstrap.BootstrapApplicationContextHolderProvider.CONFIG_SCHEMA;

import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.spring.ApplicationContextHolder;
import org.deegree.spring.bootstrap.jaxb.BootstrapApplicationContextHolderConfig;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootstrapApplicationContextHolderMetadata extends AbstractResourceMetadata<ApplicationContextHolder> {

    private static final Logger LOG = LoggerFactory.getLogger( BootstrapApplicationContextHolderMetadata.class );

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.spring.bootstrap.jaxb";

    public BootstrapApplicationContextHolderMetadata( Workspace workspace,
                                                      ResourceLocation<ApplicationContextHolder> location,
                                                      AbstractResourceProvider<ApplicationContextHolder> provider ) {
        super( workspace, location, provider );
    }

    @Override
    public ResourceBuilder<ApplicationContextHolder> prepare() {
        BootstrapApplicationContextHolderConfig config;

        try {
            config = (BootstrapApplicationContextHolderConfig) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE,
                                                                                     CONFIG_SCHEMA,
                                                                                     location.getAsStream(), workspace );
        } catch ( Exception e ) {
            LOG.trace( "Stack trace:", e );
            throw new ResourceInitException( e.getLocalizedMessage(), e );
        }

        return new BootstrapApplicationContextHolderBuilder( this, config );
    }
}
