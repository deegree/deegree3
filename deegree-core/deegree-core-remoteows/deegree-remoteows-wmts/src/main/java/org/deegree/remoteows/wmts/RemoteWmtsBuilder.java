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
package org.deegree.remoteows.wmts;

import java.io.IOException;
import java.net.URL;

import javax.xml.stream.XMLStreamException;

import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.ows.http.OwsHttpClient;
import org.deegree.protocol.ows.http.OwsHttpClientImpl;
import org.deegree.protocol.wmts.client.WMTSClient;
import org.deegree.remoteows.RemoteOWS;
import org.deegree.remoteows.wmts.jaxb.AuthenticationType;
import org.deegree.remoteows.wmts.jaxb.HTTPBasicAuthenticationType;
import org.deegree.remoteows.wmts.jaxb.RemoteWMTSConfig;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;

/**
 * This class is responsible for building remote WMTS resources.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class RemoteWmtsBuilder implements ResourceBuilder<RemoteOWS> {

	private static final int DEFAULT_CONNECTION_TIMEOUT_SECS = 5;

	private static final int DEFAULT_REQUEST_TIMEOUT_SECS = 60;

	private RemoteWMTSConfig config;

	private ResourceMetadata<RemoteOWS> metadata;

	public RemoteWmtsBuilder(RemoteWMTSConfig config, ResourceMetadata<RemoteOWS> metadata) {
		this.config = config;
		this.metadata = metadata;
	}

	@Override
	public RemoteOWS build() {
		WMTSClient client = null;
		try {
			client = createClient();
		}
		catch (Exception e) {
			String msg = "Could not create WMTS client for Remote WMTS store config at '" + metadata.getIdentifier()
					+ "': " + e.getLocalizedMessage();
			throw new ResourceInitException(msg, e);
		}
		return new RemoteWMTS(client, metadata);
	}

	private WMTSClient createClient() throws OWSExceptionReport, XMLStreamException, IOException {
		URL capas = metadata.getLocation().resolveToUrl(config.getCapabilitiesDocumentLocation().getLocation());
		OwsHttpClient httpClient = createOwsHttpClient(config);
		return new WMTSClient(capas, httpClient);
	}

	private OwsHttpClient createOwsHttpClient(RemoteWMTSConfig config) {
		int connTimeout = DEFAULT_CONNECTION_TIMEOUT_SECS;
		if (config.getConnectionTimeout() != null) {
			connTimeout = config.getConnectionTimeout();
		}
		int reqTimeout = DEFAULT_REQUEST_TIMEOUT_SECS;
		if (config.getRequestTimeout() != null) {
			reqTimeout = config.getRequestTimeout();
		}

		AuthenticationType type = config.getAuthentication() == null ? null : config.getAuthentication().getValue();
		String user = null;
		String pass = null;
		if (type instanceof HTTPBasicAuthenticationType) {
			HTTPBasicAuthenticationType basic = (HTTPBasicAuthenticationType) type;
			user = basic.getUsername();
			pass = basic.getPassword();
		}
		return new OwsHttpClientImpl(connTimeout * 1000, reqTimeout * 1000, user, pass);
	}

}
