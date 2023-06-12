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

import org.deegree.protocol.wmts.client.WMTSClient;
import org.deegree.remoteows.RemoteOWS;
import org.deegree.workspace.ResourceMetadata;

/**
 * {@link RemoteOWS} implementation for remote
 * <a href="http://www.opengeospatial.org/standards/wmts">Web Map Tile Services</a>.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class RemoteWMTS implements RemoteOWS {

	private final WMTSClient client;

	private ResourceMetadata<RemoteOWS> metadata;

	/**
	 * Creates a new {@link RemoteWMTS} instance.
	 * @param client client to use for accessing the remote WMTS, must not be
	 * <code>null</code>
	 * @param metadata resource metadata, must not be <code>null</code> if resource is
	 * managed
	 */
	public RemoteWMTS(WMTSClient client, ResourceMetadata<RemoteOWS> metadata) {
		this.client = client;
		this.metadata = metadata;
	}

	/**
	 * Returns the client for accessing the remote WMTS.
	 * @return client for accessing the remote WMTS, never <code>null</code>
	 */
	public WMTSClient getClient() {
		return client;
	}

	@Override
	public void destroy() {
		// nothing to do
	}

	@Override
	public void init() {
		// nothing to do
	}

	@Override
	public ResourceMetadata<RemoteOWS> getMetadata() {
		return metadata;
	}

}
