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
package org.deegree.services.wcs;

import static org.deegree.protocol.wcs.WCSConstants.VERSION_110;
import static org.deegree.protocol.wcs.WCSConstants.WCS_100_NS;
import static org.deegree.protocol.wps.WPSConstants.VERSION_100;

import java.net.URL;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.tom.ows.Version;
import org.deegree.coverage.persistence.CoverageBuilderManager;
import org.deegree.protocol.wcs.WCSConstants.WCSRequestType;
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
public class WCSProvider implements OWSProvider {

    protected static final ImplementationMetadata<WCSRequestType> IMPLEMENTATION_METADATA = new ImplementationMetadata<WCSRequestType>() {
        {
            supportedVersions = new Version[] { VERSION_100, VERSION_110 };
            handledNamespaces = new String[] { WCS_100_NS };
            handledRequests = WCSRequestType.class;
            supportedConfigVersions = new Version[] { Version.parseVersion( "3.0.0" ) };
            serviceName = new String[] { "WCS" };
        }
    };

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/services/wcs";
    }

    @Override
    public URL getConfigSchema() {
        return WCSProvider.class.getResource( "/META-INF/schemas/services/wcs/3.0.0/wcs_configuration.xsd" );
    }

    @Override
    public ImplementationMetadata<WCSRequestType> getImplementationMetadata() {
        return IMPLEMENTATION_METADATA;
    }

    @Override
    public OWS create( URL configURL ) {
        return new WCSController( configURL, getImplementationMetadata() );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { CoverageBuilderManager.class };
    }

    @Override
    public void init( DeegreeWorkspace workspace ) {
        // TODO Auto-generated method stub

    }
}