/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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
package org.deegree.console.webservices;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceState;
import org.deegree.console.Config;
import org.deegree.services.OWS;
import org.deegree.services.controller.WebServicesConfiguration;

public class ServiceConfig extends Config {

    private static final URL METADATA_EXAMPLE_URL = Config.class.getResource( "/META-INF/schemas/services/metadata/3.2.0/example.xml" );

    private static final URL METADATA_SCHEMA_URL = Config.class.getResource( "/META-INF/schemas/services/metadata/3.2.0/metadata.xsd" );

    public ServiceConfig( ResourceState<?> state, ResourceManager resourceManager ) {
        super( state, null, resourceManager, "/console/webservices/services", true );
    }

    public String editMetadata()
                            throws IOException {
        File metadataLocation = new File( location.getParent(), new File( id ).getName() + "_metadata.xml" );
        Config metadataConfig = new Config( metadataLocation, METADATA_SCHEMA_URL, METADATA_EXAMPLE_URL,
                                            "/console/webservices/services" );
        return metadataConfig.edit();
    }

    public String getMetadataSchemaUrl() {
        return METADATA_SCHEMA_URL.toString();
    }

    public String getMetadataLocation() {
        File metadataLocation = new File( location.getParent(), new File( id ).getName() + "_metadata.xml" );
        return metadataLocation.getAbsolutePath();
    }

    public String getCapabilitiesUrl() {
        OWS ows = ( (WebServicesConfiguration) resourceManager ).get( id );
        String type = ows.getImplementationMetadata().getImplementedServiceName()[0];

        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        StringBuffer sb = req.getRequestURL();

        // HACK HACK HACK
        int index = sb.indexOf( "/console" );
        return sb.substring( 0, index ) + "/services/" + id + "?service=" + type + "&request=GetCapabilities";
    }
}
