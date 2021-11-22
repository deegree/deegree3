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

import java.lang.reflect.Field;

import org.deegree.spring.annotation.InjectMetadata;
import org.deegree.spring.jaxb.SingleBeanRef;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.Workspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A GenericSpringResourceBuilder can be used to provide a single bean as deegree workspace resource.
 * 
 * @author <a href="mailto:reijer.copier@idgis.nl">Reijer Copier</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GenericSpringResourceBuilder<T extends Resource> extends AbstractSpringResourceBuilder<T> {

    private static final Logger LOG = LoggerFactory.getLogger( GenericSpringResourceBuilder.class );

    private final Class<T> clazz;

    private final String beanName;

    private final GenericSpringResourceMetadata<T> metadata;

    /**
     * Creates a GenericSpringResourceBuilder for a given workspace bean reference.
     * 
     * @param workspace
     *            A reference to the current workspace.
     * @param singleBeanRef
     *            A configuration snippet containing the reference to the bean.
     * @param clazz
     *            The type of the bean.
     * @param metadata
     *            The metadata to be associated with the bean.
     */
    public GenericSpringResourceBuilder( final Workspace workspace, final SingleBeanRef singleBeanRef,
                                         final Class<T> clazz, final GenericSpringResourceMetadata<T> metadata ) {
        super( workspace, singleBeanRef.getApplicationContextHolder() );

        this.clazz = clazz;
        this.beanName = singleBeanRef.getBeanName();
        this.metadata = metadata;
    }

    private void wireMetadata( Class<?> clazz, Object o )
                            throws IllegalArgumentException, IllegalAccessException {

        for ( Field f : clazz.getDeclaredFields() ) {
            if ( f.getAnnotation( InjectMetadata.class ) != null ) {
                f.setAccessible( true );
                f.set( o, metadata );
            }
        }

        if ( !clazz.equals( Object.class ) ) {
            wireMetadata( clazz.getSuperclass(), o );
        }
    }

    @Override
    public T build() {
        final T t = getBean( clazz, beanName );

        try {
            wireMetadata( t.getClass(), t );
        } catch ( Exception e ) {
            LOG.debug( "Couldn't inject metadata.", e );
            throw new ResourceInitException( "Couldn't inject metadata.", e );
        }

        return t;
    }
}
