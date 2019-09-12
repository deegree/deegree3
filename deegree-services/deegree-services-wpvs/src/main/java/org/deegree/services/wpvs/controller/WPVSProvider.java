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
package org.deegree.services.wpvs.controller;

import static org.deegree.protocol.wpvs.WPVSConstants.VERSION_040;
import static org.deegree.protocol.wpvs.WPVSConstants.WPVS_NS;

import java.net.URL;

import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.wpvs.WPVSConstants.WPVSRequestType;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPVSProvider extends OWSProvider {

    protected static final ImplementationMetadata<WPVSRequestType> IMPLEMENTATION_METADATA = new ImplementationMetadata<WPVSRequestType>() {
        {
            supportedVersions = new Version[] { VERSION_040 };
            handledNamespaces = new String[] { WPVS_NS };
            handledRequests = WPVSRequestType.class;
            supportedConfigVersions = new Version[] { Version.parseVersion( "3.0.0" ) };
            serviceName = new String[] { "WPVS" };
        }
    };

    @Override
    public String getNamespace() {
        return "http://www.deegree.org/services/wpvs";
    }

    @Override
    public URL getSchema() {
        return WPVSProvider.class.getResource( "/META-INF/schemas/services/wpvs/3.4.0/wpvs_configuration.xsd" );
    }

    @Override
    public ImplementationMetadata<WPVSRequestType> getImplementationMetadata() {
        return IMPLEMENTATION_METADATA;
    }

    @Override
    public ResourceMetadata<OWS> createFromLocation( Workspace workspace, ResourceLocation<OWS> location ) {
        return new WpvsMetadata( workspace, location, this );
    }

}
