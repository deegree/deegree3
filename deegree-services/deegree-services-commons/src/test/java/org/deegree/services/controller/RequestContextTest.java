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
package org.deegree.services.controller;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit tests for {@link RequestContext}
 *
 * @author <a href="mailto:name@company.com">Your Name</a>
 */
public class RequestContextTest {

	@Test
	public void testGetServiceUrlWithoutServiceIdPathInfo() {
		HttpServletRequest request = mockHttpRequest("http://localhost:8080/deegree-webservices/services", "/services",
				null);
		RequestContext requestContext = new RequestContext(request, null, null, null);
		Assert.assertEquals("http://localhost:8080/deegree-webservices/services", requestContext.getServiceUrl());
	}

	@Test
	public void testGetServiceUrlWithServiceIdPathInfo() {
		HttpServletRequest request = mockHttpRequest(
				"http://localhost:8080/deegree-webservices/services/inspire/ad/wfs", "/services", "/inspire/ad/wfs");
		RequestContext requestContext = new RequestContext(request, null, null, null);
		Assert.assertEquals("http://localhost:8080/deegree-webservices/services/inspire/ad/wfs",
				requestContext.getServiceUrl());
	}

	@Test
	public void testGetServiceUrlHardcodedWithoutServiceIdPathInfo() {
		HttpServletRequest request = mockHttpRequest("http://localhost:8080/deegree-webservices/services", "/services",
				null);
		RequestContext requestContext = new RequestContext(request, null, "http://mygeoportal.com/ows", null);
		Assert.assertEquals("http://mygeoportal.com/ows", requestContext.getServiceUrl());
	}

	@Test
	public void testGetServiceUrlHardcodedWithServiceIdPathInfo() {
		HttpServletRequest request = mockHttpRequest(
				"http://localhost:8080/deegree-webservices/services/inspire/ad/wfs", "/services", "/inspire/ad/wfs");
		RequestContext requestContext = new RequestContext(request, null, "http://mygeoportal.com/ows", null);
		Assert.assertEquals("http://mygeoportal.com/ows/inspire/ad/wfs", requestContext.getServiceUrl());
	}

	@Test
	public void testGetResourcesUrl() {
		HttpServletRequest request = mockHttpRequest("http://localhost:8080/deegree-webservices/services", "/services",
				null);
		RequestContext requestContext = new RequestContext(request, null, null, null);
		Assert.assertEquals("http://localhost:8080/deegree-webservices/resources", requestContext.getResourcesUrl());
	}

	@Test
	public void testGetResourcesUrlHardcoded() {
		HttpServletRequest request = mockHttpRequest("http://localhost:8080/deegree-webservices/services", "/services",
				null);
		RequestContext requestContext = new RequestContext(request, null, null, "http://mygeoportal.com/rest");
		Assert.assertEquals("http://mygeoportal.com/rest", requestContext.getResourcesUrl());
	}

	private HttpServletRequest mockHttpRequest(String requestUrl, String servletPath, String pathInfo) {
		HttpServletRequest mock = Mockito.mock(HttpServletRequest.class);
		Mockito.when(mock.getRequestURL()).thenReturn(new StringBuffer(requestUrl));
		Mockito.when(mock.getServletPath()).thenReturn(servletPath);
		Mockito.when(mock.getPathInfo()).thenReturn(pathInfo);
		return mock;
	}

}
