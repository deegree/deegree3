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

/**
 * SpringConnectionProviderMetadata is used as the 
 * {@link org.deegree.workspace.ResourceMetadata} by the
 * {@link org.deegree.spring.db.SpringConnectionProviderProvider}
 * It registers the configured 
 * {@link org.deegree.spring.ApplicationContextHolder} as dependency.
 * 
 * @author <a href="mailto:reijer.copier@idgis.nl">Reijer Copier</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
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

        return new SpringConnectionProviderBuilder( workspace, this, config );
    }
}
