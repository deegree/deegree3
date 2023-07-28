/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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
package org.deegree.test.services.util;

import static org.junit.Assert.assertEquals;

/**
 * This is a simple class to validate a HTTP response.
 *
 * <p>
 * It checks if the result code is 200 and the Content-type is 'text/xml'. You should
 * overwrite the <code>responseCode</code> an <code>contentType</code> or the
 * {@link #validateStatus(int)} and {@link #validateHeaders()} methods to alter the
 * validation process.
 *
 * <p>
 * If the checks fail, the methods will throw assert exceptions. Therefore this class
 * should be used within junit tests.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 *
 */
public class HTTPResponseValidator {

	/**
	 * The expected HTTP response code
	 */
	public int responseCode = 200;

	/**
	 * The expected HTTP Content-type
	 */
	public String contentType = "text/xml";

	/**
	 * Don't validate the response.
	 */
	public static final HTTPResponseValidator NONE = new HTTPResponseValidator() {
		@Override
		public void validate(HTTPTempFile http) {
			// no validation
		}
	};

	private HTTPTempFile http;

	/**
	 * @param http
	 */
	public void validate(HTTPTempFile http) {
		this.http = http;
		validateStatus(http.getStatus());
		validateHeaders();
	}

	/**
	 * Validates the headers.
	 * <p>
	 * Overwrite this method and check single headers with
	 * {@link #validateHeader(String, String)}.
	 */
	public void validateHeaders() {
		validateHeader("Content-type", contentType);
	}

	/**
	 * @param key the header name
	 * @return the value of the header or <code>null</code>
	 */
	public String getHeader(String key) {
		return http.getHeader(key);
	}

	/**
	 * Validate the HTTP response code.
	 * @param responseStatus
	 */
	public void validateStatus(int responseStatus) {
		assertEquals("expected another http response code", responseCode, responseStatus);
	}

	/**
	 * @param key the header name to check
	 * @param expectedValue the expected value
	 */
	public final void validateHeader(String key, String expectedValue) {
		assertEquals("expected another value for header " + key, expectedValue, http.getHeader(key));
	}

}
