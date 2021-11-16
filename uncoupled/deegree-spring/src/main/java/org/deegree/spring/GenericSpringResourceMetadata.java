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

import javax.xml.bind.JAXBElement;

import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.spring.jaxb.SingleBeanRef;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.deegree.workspace.standard.DefaultResourceIdentifier;

/**
 * A GenericSpringResourceMetadata is to be used as the 
 * {@link org.deegree.workspace.ResourceMetadata} for resources
 * provided by {@link org.deegree.spring.GenericSpringResourceBuilder}.
 * It registers the configured 
 * {@link org.deegree.spring.ApplicationContextHolder} as dependency.
 * 
 * @author <a href="mailto:reijer.copier@idgis.nl">Reijer Copier</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GenericSpringResourceMetadata<T extends Resource> extends AbstractResourceMetadata<T> {

    private final String configJaxbPackage;

    private final Class<T> clazz;

    public GenericSpringResourceMetadata( final Workspace workspace, final ResourceLocation<T> location,
                                          final AbstractResourceProvider<T> provider, final String configJaxbPackage,
                                          final Class<T> clazz ) {
        super( workspace, location, provider );

        this.configJaxbPackage = configJaxbPackage;        
        this.clazz = clazz;
    }

    @Override
    public ResourceBuilder<T> prepare() {
        final SingleBeanRef config;

        try {
            final JAXBElement<?> element = (JAXBElement<?>) JAXBUtils.unmarshall( configJaxbPackage, provider.getSchema(),
                                                                                  location.getAsStream(), workspace );
            if ( element.getDeclaredType().equals( SingleBeanRef.class ) ) {
                config = (SingleBeanRef) element.getValue();

                final String applicationContextHolder = config.getApplicationContextHolder();
                dependencies.add( new DefaultResourceIdentifier<ApplicationContextHolder>(
                                                                                           ApplicationContextHolderProvider.class,
                                                                                           applicationContextHolder ) );
            } else {
                throw new ResourceInitException( "Wrong configuration object passed to GenericSpringResourceMetadata." );
            }
        } catch ( Exception e ) {
            throw new ResourceInitException( "Couldn't construct GenericSpringResourceBuilder.", e );
        }

        return new GenericSpringResourceBuilder<T>( workspace, config, clazz, this );
    }
}
