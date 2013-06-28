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
package org.deegree.spring.bootstrap;

import org.deegree.spring.ApplicationContextHolder;
import org.deegree.spring.bootstrap.jaxb.BootstrapApplicationContextHolderConfig;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

/** 
 * The BootstrapApplicationContextHolderBuilder bootstraps a
 * Spring {@link org.springframework.context.ApplicationContext} and
 * wraps it in an {@link org.deegree.spring.ApplicationContextHolder}
 * 
 * This builder is the deegree workspace equivalent of Spring classes like
 * {@link org.springframework.web.servlet.FrameworkServlet}, 
 * {@link org.springframework.web.context.ContextLoaderListener} or
 * other Spring classes that construct a root
 * {@link org.springframework.context.ApplicationContext}.
 * 
 * @author <a href="mailto:reijer.copier@idgis.nl">Reijer Copier</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
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
