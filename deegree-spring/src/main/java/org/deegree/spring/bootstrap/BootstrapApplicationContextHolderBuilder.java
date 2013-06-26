package org.deegree.spring.bootstrap;

import org.deegree.spring.ApplicationContextHolder;
import org.deegree.spring.bootstrap.jaxb.BootstrapApplicationContextHolderConfig;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class BootstrapApplicationContextHolderBuilder implements ResourceBuilder<ApplicationContextHolder> {
    
    private static final Logger LOG = LoggerFactory.getLogger( BootstrapApplicationContextHolderBuilder.class );

    private final BootstrapApplicationContextHolderMetadata metadata;

    private final BootstrapApplicationContextHolderConfig config;

    private final ClassLoader classLoader; 

    public BootstrapApplicationContextHolderBuilder( ClassLoader classLoader,
                                                     BootstrapApplicationContextHolderMetadata metadata,
                                                     BootstrapApplicationContextHolderConfig config ) {
        this.classLoader = classLoader;
        this.metadata = metadata;
        this.config = config;
    }

    @Override
    public ApplicationContextHolder build() {
        LOG.debug( "Building BootstrapApplicationContextHolder." );
        
        try {
            final String contextClass = config.getContextClass();

            if ( contextClass != null ) {
                LOG.debug( "Using ContextClass {}", contextClass );

                final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
                context.setClassLoader( classLoader );
                context.register( classLoader.loadClass( contextClass ) );
                context.refresh();

                return new ApplicationContextHolder( metadata, context );
            } else {
                final String contextConfigLocation = config.getContextConfigLocation();
                if ( contextConfigLocation == null ) {
                    throw new ResourceInitException(
                                                     "Both ContextClass and ContextConfigLocation are missing from BootstrapApplicationContextHolderConfig" );
                }

                LOG.debug( "Using ContextConfigLocation {}", contextConfigLocation );
                final GenericXmlApplicationContext context = new GenericXmlApplicationContext();
                context.setClassLoader( classLoader );
                context.load( contextConfigLocation );
                context.refresh();

                return new ApplicationContextHolder( metadata, context );
            }
        } catch ( Exception e ) {
            LOG.debug( "Couldn't build BootstrapApplicationContextHolder", e );
            throw new ResourceInitException( "Couldn't build BootstrapApplicationContextHolder", e );
        }
    }

}
