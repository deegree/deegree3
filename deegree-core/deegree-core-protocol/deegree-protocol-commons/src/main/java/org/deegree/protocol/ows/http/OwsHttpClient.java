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
package org.deegree.protocol.ows.http;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.deegree.commons.utils.io.StreamBufferStore;

/**
 * Performs HTTP requests against remote OWS.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public interface OwsHttpClient {

	/**
	 * Performs an HTTP-GET request to the specified service endpoint.
	 * <p>
	 * NOTE: The caller <b>must</b> call {@link OwsHttpResponseImpl#close()} on the
	 * returned object eventually, otherwise the HTTP connection will not be freed.
	 * </p>
	 * @param endPoint service endpoint to send to request to, must not be
	 * <code>null</code>
	 * @param params KVP parameters, may be <code>null</code>
	 * @param headers HTTP headers, may be <code>null</code>
	 * @return service response, never <code>null</code>
	 * @throws IOException
	 */
	OwsHttpResponse doGet(URL endPoint, Map<String, String> params, Map<String, String> headers) throws IOException;

	/**
	 * Performs an HTTP-POST request to the specified service endpoint.
	 * <p>
	 * NOTE: The caller <b>must</b> call {@link OwsHttpResponseImpl#close()} on the
	 * returned object eventually, otherwise the HTTP connection will not be freed.
	 * </p>
	 * @param endPoint service endpoint to send to request to, must not be
	 * <code>null</code>
	 * @param contentType content type, may be <code>null</code>
	 * @param body POST body, may be <code>null</code>
	 * @param headers HTTP headers, may be <code>null</code>
	 * @return service response, never <code>null</code>
	 * @throws IOException
	 */
	OwsHttpResponse doPost(URL endPoint, String contentType, StreamBufferStore body, Map<String, String> headers)
			throws IOException;

}
