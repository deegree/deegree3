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
 * Easy mocking for {@link OwsHttpClient}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class OwsHttpClientMock implements OwsHttpClient {

	private OwsHttpResponse response;

	@Override
	public OwsHttpResponse doGet(URL endPoint, Map<String, String> params, Map<String, String> headers)
			throws IOException {
		return response;
	}

	@Override
	public OwsHttpResponse doPost(URL endPoint, String contentType, StreamBufferStore body, Map<String, String> headers)
			throws IOException {
		return response;
	}

	/**
	 * Sets the {@link OwsHttpResponse} that will be returned by the next call to
	 * {@link #doGet(URL, Map, Map)} or
	 * {@link #doPost(URL, String, StreamBufferStore, Map)}.
	 * @param responseBody source of the response body, must not be <code>null</code>
	 * @param contentType content-type header, may be <code>null</code>
	 * @param httpStatus HTTP response status, usually 200 (OK)
	 */
	public void setResponse(URL responseBody, String contentType, int httpStatus) {
		this.response = new OwsHttpResponseMock(responseBody, contentType, httpStatus);
	}

}
