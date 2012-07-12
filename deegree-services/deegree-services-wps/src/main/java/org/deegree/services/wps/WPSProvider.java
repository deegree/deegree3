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
package org.deegree.services.wps;

import static org.deegree.protocol.wps.WPSConstants.VERSION_100;
import static org.deegree.protocol.wps.WPSConstants.WPS_100_NS;

import java.net.URL;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.protocol.wps.WPSConstants.WPSRequestType;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.controller.ImplementationMetadata;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPSProvider implements OWSProvider {

    protected static final ImplementationMetadata<WPSRequestType> IMPLEMENTATION_METADATA = new ImplementationMetadata<WPSRequestType>() {
        {
            supportedVersions = new Version[] { VERSION_100 };
            handledNamespaces = new String[] { WPS_100_NS };
            handledRequests = WPSRequestType.class;
            supportedConfigVersions = new Version[] { Version.parseVersion( "3.0.0" ), Version.parseVersion( "3.1.0" ) };
            serviceName = new String[] { "WPS" };
        }
    };

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/services/wps";
    }

    @Override
    public URL getConfigSchema() {
        return WPSProvider.class.getResource( "/META-INF/schemas/services/wps/3.1.0/wps_configuration.xsd" );
    }

    @Override
    public ImplementationMetadata<WPSRequestType> getImplementationMetadata() {
        return IMPLEMENTATION_METADATA;
    }

    @Override
    public OWS create( URL configURL ) {
        return new WPService( configURL, getImplementationMetadata() );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { ProxyUtils.class, ProcessManager.class };
    }

    @Override
    public void init( DeegreeWorkspace workspace ) {
        // TODO Auto-generated method stub
    }
}