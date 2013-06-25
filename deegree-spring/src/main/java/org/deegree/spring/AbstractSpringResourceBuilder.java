package org.deegree.spring;

import java.util.Map;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.Workspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationContext;

public abstract class AbstractSpringResourceBuilder<T extends Resource> implements ResourceBuilder<T> {

    private static final Logger LOG = LoggerFactory.getLogger( AbstractSpringResourceBuilder.class );

    private final Workspace workspace;

    private final String applicationContextHolderId;

    public AbstractSpringResourceBuilder( final Workspace workspace, final String applicationContextHolderId ) {
        this.workspace = workspace;
        this.applicationContextHolderId = applicationContextHolderId;
    }

    protected <B> B getBean( final Class<B> clazz ) {
        return getBean( clazz, null );
    }

    protected <B> B getBean( final Class<B> clazz, final String beanName ) {
        return getBean( clazz, beanName, null );
    }

    protected <B> B getBean( final Class<B> clazz, final String beanName, final String conventionalBeanName ) {
        final String className = clazz.getCanonicalName();

        final ApplicationContextHolder applicationContextHolder = workspace.getResource( ApplicationContextHolderProvider.class,
                                                                                         applicationContextHolderId );
        final ApplicationContext applicationContext = applicationContextHolder.getApplicationContext();

        final B bean;
        if ( beanName != null ) {
            try {
                bean = applicationContext.getBean( beanName, clazz );

                LOG.info( "Bean with name '{}' fetched from ApplicationContext.", beanName );
            } catch ( Exception e ) {
                throw new ResourceInitException( "Couldn't fetch bean with type '" + className + "' and name '"
                                                 + beanName + "' from ApplicationContext.", e );
            }
        } else {
            final Map<String, B> beans = applicationContext.getBeansOfType( clazz );
            if ( beans.size() == 1 ) {
                bean = beans.values().iterator().next();

                LOG.info( "Single {} bean fetched from ApplicationContext.", className );
            } else {
                if ( conventionalBeanName != null ) {
                    if ( beans.containsKey( conventionalBeanName ) ) {
                        bean = beans.get( conventionalBeanName );

                        LOG.info( "Multiple {} beans found in ApplicationContext, bean named '{}' selected.",
                                  className, conventionalBeanName );
                    } else {
                        throw new ResourceInitException( "Multiple beans with type " + className
                                                         + " are found in ApplicationContext, none of them are named '"
                                                         + conventionalBeanName
                                                         + "'. Suggestion: add bean name to configuration." );
                    }
                } else {
                    throw new ResourceInitException( "Multiple beans with type " + className
                                                     + " are found in ApplicationContext." );
                }
            }
        }

        return bean;
    }
}
