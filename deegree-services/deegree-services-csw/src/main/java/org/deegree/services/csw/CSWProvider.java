//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.services.csw;

import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceState;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreManager;
import org.deegree.protocol.csw.CSWConstants.CSWRequestType;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.csw.profile.CommonCSWProfile;
import org.deegree.services.csw.profile.ServiceProfile;
import org.deegree.services.csw.profile.ServiceProfileManager;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CSWProvider implements OWSProvider {

    private static final Logger LOG = getLogger( CSWProvider.class );

    // pre-initialized to avoid NPE in WebServicesConfiguration if no CSW is configured
    private ServiceProfile profile = new CommonCSWProfile();

    private DeegreeWorkspace ws;

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/services/csw";
    }

    @Override
    public URL getConfigSchema() {
        return CSWProvider.class.getResource( "/META-INF/schemas/csw/3.1.0/csw_configuration.xsd" );
    }

    @Override
    public ImplementationMetadata<CSWRequestType> getImplementationMetadata() {
        return profile.getImplementationMetadata();
    }

    @Override
    public OWS create( URL configURL ) {
        MetadataStoreManager mgr = ws.getSubsystemManager( MetadataStoreManager.class );
        if ( mgr == null )
            throw new IllegalArgumentException( "Could not find a MetadataStoreManager!" );
        List<MetadataStore<?>> availableStores = new ArrayList<MetadataStore<?>>();
        for ( ResourceState<MetadataStore<?>> state : mgr.getStates() ) {
            if ( state.getResource() != null ) {
                availableStores.add( state.getResource() );
            }
        }
        if ( availableStores.size() == 0 )
            throw new IllegalArgumentException(
                                                "There is no MetadataStore configured, ensure that exactly one store is available!" );
        if ( availableStores.size() > 1 )
            throw new IllegalArgumentException( "Number of MetadataStores must be one: configured are "
                                                + availableStores.size() + " stores!" );
        MetadataStore<?> store = availableStores.get( 0 );
        profile = ServiceProfileManager.createProfile( store );
        return new CSWController( configURL, getImplementationMetadata() );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { MetadataStoreManager.class };
    }

    @Override
    public void init( DeegreeWorkspace workspace ) {
        LOG.info( "Init CSW Provider" );
        this.ws = workspace;
    }
}