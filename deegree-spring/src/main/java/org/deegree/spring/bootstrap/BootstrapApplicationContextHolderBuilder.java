package org.deegree.spring.bootstrap;

import org.deegree.spring.ApplicationContextHolder;
import org.deegree.spring.bootstrap.jaxb.BootstrapApplicationContextHolderConfig;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class BootstrapApplicationContextHolderBuilder implements ResourceBuilder<ApplicationContextHolder> {
    
    private static final Logger LOG = LoggerFactory.getLogger( BootstrapApplicationContextHolderBuilder.class );

    private final BootstrapApplicationContextHolderMetadata metadata;

    private final BootstrapApplicationContextHolderConfig config;

    public BootstrapApplicationContextHolderBuilder( BootstrapApplicationContextHolderMetadata metadata,
                                                     BootstrapApplicationContextHolderConfig config ) {

        this.metadata = metadata;
        this.config = config;
    }

    @Override
    public ApplicationContextHolder build() {
        LOG.debug( "Building BootstrapApplicationContextHolder." );
        
        try {
            final ConfigurableApplicationContext context;
            final String contextClass = config.getContextClass();
            if ( contextClass != null ) {
                LOG.debug( "Using ContextClass {},", contextClass );                
                context = new AnnotationConfigApplicationContext( Class.forName( contextClass ) );
            } else {
                final String contextConfigLocation = config.getContextConfigLocation();
                if ( contextConfigLocation == null ) {
                    throw new ResourceInitException(
                                                     "Both ContextClass and ContextConfigLocation are missing from BootstrapApplicationContextHolderConfig" );
                }
                
                LOG.debug( "Using ContextConfigLocation {},", contextConfigLocation );
                context = new GenericXmlApplicationContext( contextConfigLocation );
            }

            return new ApplicationContextHolder( metadata, context );
        } catch ( Exception e ) {
            LOG.debug( "Couldn't build BootstrapApplicationContextHolder", e );
            throw new ResourceInitException( "Couldn't build BootstrapApplicationContextHolder", e );
        }
    }

}
