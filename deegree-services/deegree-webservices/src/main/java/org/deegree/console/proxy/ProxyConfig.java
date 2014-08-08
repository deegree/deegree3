/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.console.proxy;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.deegree.console.Config;

/**
 * Config implementation for the main.xml.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public class ProxyConfig extends Config {

    private static final URL PROXY_SCHEMA_URL = ProxyConfig.class.getResource( "/META-INF/schemas/proxy/3.0.0/proxy.xsd" );

    private static final URL PROXY_EXAMPLE_URL = ProxyConfig.class.getResource( "/META-INF/schemas/proxy/3.0.0/example.xml" );

    private String file;

    public ProxyConfig( String file ) {
        super( null, null, "/console/proxy/index", false );
        this.file = file;
    }

    @Override
    public String edit()
                            throws IOException {
        FileUtils.copyURLToFile( getTemplate(), new File( file ) );
        StringBuilder sb = new StringBuilder( "/console/generic/xmleditor?faces-redirect=true" );
        sb.append( "&id=" ).append( id );
        sb.append( "&schemaUrl=" ).append( PROXY_SCHEMA_URL.toString() );
        sb.append( "&fileName=" ).append( file );
        sb.append( "&nextView=" ).append( getResourceOutcome() );
        return sb.toString();
    }

    @Override
    public String getSchemaAsText() {
        try {
            return IOUtils.toString( PROXY_SCHEMA_URL );
        } catch ( IOException e ) {
            // ignore
        }
        return "";
    }

    @Override
    public URL getTemplate() {
        return PROXY_EXAMPLE_URL;
    }

    @Override
    public URL getSchemaURL() {
        return PROXY_SCHEMA_URL;
    }

}
