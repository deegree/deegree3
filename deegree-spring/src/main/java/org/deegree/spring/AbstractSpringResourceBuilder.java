//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2013 by:

 IDgis bv

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 IDgis bv
 Boomkamp 16
 7461 AX Rijssen
 The Netherlands
 http://idgis.nl/ 

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
----------------------------------------------------------------------------*/
package org.deegree.spring;

import java.util.Map;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.Workspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationContext;

/**
 * The AbstractSpringResourceBuilder can be extended in order to create a 
 * {@link org.deegree.workspace.ResourceBuilder} that fetches beans from 
 * the {@link org.springframework.context.ApplicationContext} contained in 
 * the specified {@link org.deegree.spring.ApplicationContextHolder}. 
 * 
 * @author <a href="mailto:reijer.copier@idgis.nl">Reijer Copier</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractSpringResourceBuilder<T extends Resource> implements ResourceBuilder<T> {

    private static final Logger LOG = LoggerFactory.getLogger( AbstractSpringResourceBuilder.class );

    private final Workspace workspace;

    private final String applicationContextHolderId;

    /**
     * Creates an AbstractSpringResourceBuilder for a given workspace and application context holder.
     * 
     * @param workspace A reference to the current workspace.
     * @param applicationContextHolderId The resource identifier of the application context holder.
     */
    public AbstractSpringResourceBuilder( final Workspace workspace, final String applicationContextHolderId ) {
        this.workspace = workspace;
        this.applicationContextHolderId = applicationContextHolderId;
    }

    /**
     * Get a bean for a given type.
     * 
     * @param clazz The type of bean to fetch.
     * @throws org.deegree.workspace.ResourceInitException if there is not 
     * exactly one single bean of given type. 
     * @return A bean.
     */
    protected <B> B getBean( final Class<B> clazz ) {
        return getBean( clazz, null );
    }

    /**
     * Get a bean for a given name and type.
     * 
     * @param clazz The type of bean to fetch.
     * @param beanName The name of the bean. Allowed to be null.
     * @throws org.deegree.workspace.ResourceInitException if the requested
     * bean does not exist.
     * @return A bean.
     */
    protected <B> B getBean( final Class<B> clazz, final String beanName ) {
        return getBean( clazz, beanName, null );
    }

    /**
     * Get a bean for a given name and type. 
     * 
     * @param clazz The type of bean to fetch.
     * @param beanName The name of the bean. Allowed to be null.
     * @param conventionalBeanName The conventional Spring name of this type of bean. 
     * Allowed to be null. Only used in case beanName is null.
     * @throws org.deegree.workspace.ResourceInitException if the requested
     * bean does not exist.
     * @return A bean.
     */
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
            switch ( beans.size() ) {
            case 0:
                throw new ResourceInitException( "No beans of type " + className
                                                 + " found in ApplicationContext." );
            case 1:
                bean = beans.values().iterator().next();

                LOG.info( "Single {} bean fetched from ApplicationContext.", className );
                break;
            default:
                if ( conventionalBeanName != null ) {
                    if ( beans.containsKey( conventionalBeanName ) ) {
                        bean = beans.get( conventionalBeanName );

                        LOG.info( "Multiple {} beans found in ApplicationContext, bean named '{}' selected by convention.",
                                  className, conventionalBeanName );
                    } else {
                        throw new ResourceInitException( "Multiple beans of type " + className
                                                         + " are found in ApplicationContext, none of them bares the conventional name '"
                                                         + conventionalBeanName
                                                         + "'. Suggestion: add bean name to configuration." );
                    }
                } else {
                    throw new ResourceInitException( "Multiple beans of type " + className
                                                     + " are found in ApplicationContext." );
                }
            }
        }

        return bean;
    }
}
