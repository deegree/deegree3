//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.remoteows.wmts;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.ows.http.OwsHttpClient;
import org.deegree.protocol.ows.http.OwsHttpClientImpl;
import org.deegree.protocol.wmts.client.WMTSClient;
import org.deegree.remoteows.RemoteOWSProvider;
import org.deegree.remoteows.wmts.jaxb.AuthenticationType;
import org.deegree.remoteows.wmts.jaxb.HTTPBasicAuthenticationType;
import org.deegree.remoteows.wmts.jaxb.RemoteWMTSConfig;
import org.slf4j.Logger;

/**
 * {@link RemoteOWSProvider} for {@link RemoteWMTS}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RemoteWMTSProvider implements RemoteOWSProvider {

    private static final Logger LOG = getLogger( RemoteWMTSProvider.class );

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.remoteows.wmts.jaxb";

    private static final URL CONFIG_SCHEMA = RemoteWMTSProvider.class.getResource( "/META-INF/schemas/remoteows/wmts/3.2.0/remotewmts.xsd" );

    private static final String CONFIG_NAMESPACE = "http://www.deegree.org/remoteows/wmts";

    private static final int DEFAULT_CONNECTION_TIMEOUT_SECS = 5;

    private static final int DEFAULT_REQUEST_TIMEOUT_SECS = 60;

    private DeegreeWorkspace workspace;

    @Override
    public String getConfigNamespace() {
        return CONFIG_NAMESPACE;
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

    @Override
    public RemoteWMTS create( URL configUrl )
                            throws ResourceInitException {
        RemoteWMTSConfig config = unmarshall( configUrl );
        XMLAdapter urlResolver = new XMLAdapter();
        urlResolver.setSystemId( configUrl.toString() );
        WMTSClient client = null;
        try {
            client = createClient( config, urlResolver );
        } catch ( Exception e ) {
            LOG.trace( "Stack trace:", e );
            String msg = "Could not create WMTS client for Remote WMTS store config at '" + configUrl + "': "
                         + e.getLocalizedMessage();
            throw new ResourceInitException( msg );
        }
        return new RemoteWMTS( client );
    }

    private WMTSClient createClient( RemoteWMTSConfig config, XMLAdapter urlResolver )
                            throws OWSExceptionReport, XMLStreamException, IOException {
        URL capas = urlResolver.resolve( config.getCapabilitiesDocumentLocation().getLocation() );
        OwsHttpClient httpClient = createOwsHttpClient( config );
        return new WMTSClient( capas, httpClient );
    }

    private OwsHttpClient createOwsHttpClient( RemoteWMTSConfig config ) {
        int connTimeout = DEFAULT_CONNECTION_TIMEOUT_SECS;
        if ( config.getConnectionTimeout() != null ) {
            connTimeout = config.getConnectionTimeout();
        }
        int reqTimeout = DEFAULT_REQUEST_TIMEOUT_SECS;
        if ( config.getRequestTimeout() != null ) {
            reqTimeout = config.getRequestTimeout();
        }

        AuthenticationType type = config.getAuthentication() == null ? null : config.getAuthentication().getValue();
        String user = null;
        String pass = null;
        if ( type instanceof HTTPBasicAuthenticationType ) {
            HTTPBasicAuthenticationType basic = (HTTPBasicAuthenticationType) type;
            user = basic.getUsername();
            pass = basic.getPassword();
        }
        return new OwsHttpClientImpl( connTimeout * 1000, reqTimeout * 1000, user, pass );
    }

    private RemoteWMTSConfig unmarshall( URL configUrl )
                            throws ResourceInitException {
        try {
            return (RemoteWMTSConfig) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA, configUrl, workspace );
        } catch ( Exception e ) {
            LOG.trace( "Stack trace:", e );
            String msg = "Remote WMTS store config at '" + configUrl + "' could not be parsed: "
                         + e.getLocalizedMessage();
            throw new ResourceInitException( msg );
        }
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
