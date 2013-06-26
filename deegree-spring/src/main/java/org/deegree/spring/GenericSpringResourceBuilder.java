package org.deegree.spring;

import java.lang.reflect.Field;

import org.deegree.spring.annotation.InjectMetadata;
import org.deegree.spring.jaxb.SingleBeanRef;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.Workspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericSpringResourceBuilder<T extends Resource> extends AbstractSpringResourceBuilder<T> {

    private static final Logger LOG = LoggerFactory.getLogger( GenericSpringResourceBuilder.class );

    private final Class<T> clazz;

    private final String beanName;

    private final GenericSpringResourceMetadata<T> metadata;

    public GenericSpringResourceBuilder( final Workspace workspace, final SingleBeanRef singleBeanRef,
                                         final Class<T> clazz, final GenericSpringResourceMetadata<T> metadata ) {
        super( workspace, singleBeanRef.getApplicationContextHolder() );

        this.clazz = clazz;
        this.beanName = singleBeanRef.getBeanName();
        this.metadata = metadata;
    }

    @Override
    public T build() {
        final T t = getBean( clazz, beanName );

        try {
            for ( Field f : t.getClass().getDeclaredFields() ) {
                if ( f.getAnnotation( InjectMetadata.class ) != null ) {
                    f.setAccessible( true );
                    f.set( t, metadata );
                }
            }
        } catch ( Exception e ) {
            LOG.debug( "Couldn't inject metadata.", e );
            throw new ResourceInitException( "Couldn't inject metadata.", e );
        }

        return t;
    }
}
