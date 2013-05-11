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

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceManager;
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

    private CSWController cswController;

    private DeegreeWorkspace ws;

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/services/csw";
    }

    @Override
    public URL getConfigSchema() {
        return CSWProvider.class.getResource( "/META-INF/schemas/services/csw/3.2.0/csw_configuration.xsd" );
    }

    @Override
    public ImplementationMetadata<CSWRequestType> getImplementationMetadata() {
        if ( cswController != null && cswController.getStore() != null ) {
            this.profile = ServiceProfileManager.createProfile( cswController.getStore() );
        }
        return profile.getImplementationMetadata();
    }

    @Override
    public OWS create( URL configURL ) {
        cswController = new CSWController( configURL, getImplementationMetadata() );
        return cswController;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
    }

    @Override
    public void init( DeegreeWorkspace workspace ) {
        LOG.info( "Init CSW Provider" );
        this.ws = workspace;
    }
}