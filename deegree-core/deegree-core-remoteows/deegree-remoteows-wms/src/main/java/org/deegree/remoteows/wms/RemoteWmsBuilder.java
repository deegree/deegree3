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
package org.deegree.remoteows.wms;

import java.net.URL;

import org.deegree.protocol.wms.client.WMSClient;
import org.deegree.remoteows.RemoteOWS;
import org.deegree.remoteows.wms_new.jaxb.AuthenticationType;
import org.deegree.remoteows.wms_new.jaxb.HTTPBasicAuthenticationType;
import org.deegree.remoteows.wms_new.jaxb.RemoteWMS;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;

/**
 * This class is responsible for building remote WMS.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class RemoteWmsBuilder implements ResourceBuilder<RemoteOWS> {

	private RemoteWMS cfg;

	private ResourceMetadata<RemoteOWS> metadata;

	public RemoteWmsBuilder(org.deegree.remoteows.wms_new.jaxb.RemoteWMS cfg, ResourceMetadata<RemoteOWS> metadata) {
		this.cfg = cfg;
		this.metadata = metadata;
	}

	@Override
	public RemoteOWS build() {
		try {
			URL capas = metadata.getLocation().resolveToUrl(cfg.getCapabilitiesDocumentLocation().getLocation());

			int connTimeout = cfg.getConnectionTimeout() == null ? 5 : cfg.getConnectionTimeout();
			int reqTimeout = cfg.getRequestTimeout() == null ? 60 : cfg.getRequestTimeout();

			WMSClient client;

			AuthenticationType type = cfg.getAuthentication() == null ? null : cfg.getAuthentication().getValue();
			String user = null;
			String pass = null;
			if (type instanceof HTTPBasicAuthenticationType) {
				HTTPBasicAuthenticationType basic = (HTTPBasicAuthenticationType) type;
				user = basic.getUsername();
				pass = basic.getPassword();
			}
			client = new WMSClient(capas, connTimeout, reqTimeout, user, pass);

			return new org.deegree.remoteows.wms.RemoteWMS(client, metadata);
		}
		catch (Exception e) {
			throw new ResourceInitException("Unable to build remote WMS: " + e.getLocalizedMessage(), e);
		}
	}

}
