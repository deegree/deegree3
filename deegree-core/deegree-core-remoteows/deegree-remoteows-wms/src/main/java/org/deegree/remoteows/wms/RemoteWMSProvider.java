//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-base/src/main/java/org/deegree/remoteows/wms/RemoteWMSProvider.java $
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
package org.deegree.remoteows.wms;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.remoteows.RemoteOWSProvider;
import org.deegree.remoteows.RemoteOWSStore;
import org.deegree.remoteows.wms.jaxb.AuthenticationType;
import org.deegree.remoteows.wms.jaxb.HTTPBasicAuthenticationType;
import org.deegree.remoteows.wms.jaxb.RemoteWMSStore;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: aschmitz $
 * 
 * @version $Revision: 31451 $, $Date: 2011-08-08 08:13:46 +0200 (Mon, 08 Aug 2011) $
 */
public class RemoteWMSProvider implements RemoteOWSProvider {

    private static final Logger LOG = getLogger( RemoteWMSProvider.class );

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.remoteows.wms.jaxb";

    private static final URL CONFIG_SCHEMA = RemoteWMSProvider.class.getResource( "/META-INF/schemas/datasource/remoteows/wms/3.1.0/remotewms.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public List<String> getCapabilitiesNamespaces() {
        return null;
    }

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/datasource/remoteows/wms";
    }

    @Override
    public String getServiceType() {
        return "WMS";
    }

    @Override
    public RemoteOWSStore create( URL config ) {
        try {
            RemoteWMSStore cfg = (RemoteWMSStore) unmarshall( CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA, config, workspace );
            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( config.toString() );
            URL capas = resolver.resolve( cfg.getCapabilitiesDocumentLocation().getLocation() );

            int connTimeout = cfg.getConnectionTimeout() == null ? 5 : cfg.getConnectionTimeout();
            int reqTimeout = cfg.getRequestTimeout() == null ? 60 : cfg.getRequestTimeout();

            WMSClient111 client;

            AuthenticationType type = cfg.getAuthentication() == null ? null : cfg.getAuthentication().getValue();
            String user = null;
            String pass = null;
            if ( type instanceof HTTPBasicAuthenticationType ) {
                HTTPBasicAuthenticationType basic = (HTTPBasicAuthenticationType) type;
                user = basic.getUsername();
                pass = basic.getPassword();
            }
            client = new WMSClient111( capas, connTimeout, reqTimeout, user, pass );

            return new org.deegree.remoteows.wms.RemoteWMSStore( client );
        } catch ( JAXBException e ) {
            e.printStackTrace();
            LOG.warn( "Remote WMS store config at '{}' could not be parsed: {}", config, e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( ClassCastException e ) {
            e.printStackTrace();
            LOG.warn( "Remote WMS store config at '{}' could not be parsed: {}", config, e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
            LOG.warn( "Remote WMS store config at '{}' could not be parsed: {}", config, e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
        return null;
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { ProxyUtils.class };
    }

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

}