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
package org.deegree.services.sos;

import static org.deegree.protocol.sos.SOSConstants.SOS_100_NS;
import static org.deegree.protocol.wps.WPSConstants.VERSION_100;

import java.net.URL;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.tom.ows.Version;
import org.deegree.observation.persistence.ObservationStoreManager;
import org.deegree.protocol.sos.SOSConstants.SOSRequestType;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.controller.ImplementationMetadata;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * 
 */
public class SOSProvider implements OWSProvider {

    protected static final ImplementationMetadata<SOSRequestType> IMPLEMENTATION_METADATA = new ImplementationMetadata<SOSRequestType>() {
        {
            supportedVersions = new Version[] { VERSION_100 };
            handledNamespaces = new String[] { SOS_100_NS };
            handledRequests = SOSRequestType.class;
            supportedConfigVersions = new Version[] { Version.parseVersion( "3.0.0" ) };
            serviceName = new String[] { "SOS" };
        }
    };

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/services/sos";
    }

    @Override
    public URL getConfigSchema() {
        return SOSProvider.class.getResource( "/META-INF/schemas/services/sos/3.0.0/sos_configuration.xsd" );
    }

    @Override
    public ImplementationMetadata<SOSRequestType> getImplementationMetadata() {
        return IMPLEMENTATION_METADATA;
    }

    @Override
    public OWS create( URL configURL ) {
        return new SOSController( configURL, getImplementationMetadata() );
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { ObservationStoreManager.class };
    }

    @Override
    public void init( DeegreeWorkspace workspace ) {
        // TODO Auto-generated method stub

    }
}