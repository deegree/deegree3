/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.services.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(OGCFrontController.class)
@PowerMockIgnore({ "jdk.internal.reflect.*", "jdk.internal.misc.*", "javax.management.*", "javax.xml.*",
		"javax.activation.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "org.xml.*",
		"org.w3c.dom.*" })
public class OGCFrontControllerTest {

	@Test
	public void testGetHttpPostURLWithServiceUrl() throws Exception {
		String serviceUrl = "http://myservice.de/deegree-webservices/test";
		RequestContext mockedContext = mockContext(serviceUrl);

		prepareOGCFrontController(mockedContext);

		String httpPostURL = OGCFrontController.getHttpPostURL();

		assertThat(httpPostURL, is(serviceUrl));
	}

	@Test
	public void testGetHttpPostURLWithXForwardedHost() throws Exception {
		String serviceUrl = "http://myservice.de/deegree-webservices/test";
		String xForwardedHost = "xForwardedHost.de";
		RequestContext mockedContext = mockContext(serviceUrl, xForwardedHost);

		prepareOGCFrontController(mockedContext);

		String httpPostURL = OGCFrontController.getHttpPostURL();

		assertThat(httpPostURL, is("http://xForwardedHost.de/deegree-webservices/test"));
	}

	@Test
	public void testGetHttpPostURLWithXForwardedHostAndPortAddPort() throws Exception {
		String serviceUrl = "http://myservice.de/deegree-webservices/test";
		String xForwardedHost = "xForwardedHost.de";
		String xForwardedPort = "8089";
		RequestContext mockedContext = mockContext(serviceUrl, xForwardedHost, xForwardedPort);

		prepareOGCFrontController(mockedContext);

		String httpPostURL = OGCFrontController.getHttpPostURL();

		assertThat(httpPostURL, is("http://xForwardedHost.de:8089/deegree-webservices/test"));
	}

	@Test
	public void testGetHttpPostURLWithXForwardedHostAndPortReplacePort() throws Exception {
		String serviceUrl = "http://myservice.de:9090/deegree-webservices/test";
		String xForwardedHost = "xForwardedHost.de";
		String xForwardedPort = "8089";
		RequestContext mockedContext = mockContext(serviceUrl, xForwardedHost, xForwardedPort);

		prepareOGCFrontController(mockedContext);

		String httpPostURL = OGCFrontController.getHttpPostURL();

		assertThat(httpPostURL, is("http://xForwardedHost.de:8089/deegree-webservices/test"));
	}

	@Test
	public void testGetHttpPostURLWithXForwardedHostAndPortAndProtocolReplaceProto() throws Exception {
		String serviceUrl = "http://myservice.de:9090/deegree-webservices/test";
		String xForwardedHost = "xForwardedHost.de";
		String xForwardedPort = "8089";
		String xForwardedProto = "https";
		RequestContext mockedContext = mockContext(serviceUrl, xForwardedHost, xForwardedPort, xForwardedProto);

		prepareOGCFrontController(mockedContext);

		String httpPostURL = OGCFrontController.getHttpPostURL();

		assertThat(httpPostURL, is("https://xForwardedHost.de:8089/deegree-webservices/test"));
	}

	private void prepareOGCFrontController(RequestContext mockedContext) throws Exception {
		mockStatic(OGCFrontController.class);
		PowerMockito.when(OGCFrontController.getHttpPostURL()).thenCallRealMethod();
		PowerMockito.when(OGCFrontController.class, "getHttpURL").thenCallRealMethod();
		PowerMockito.when(OGCFrontController.class, "buildUrlFromForwardedHeader", eq(mockedContext), any(URL.class))
			.thenCallRealMethod();
		PowerMockito.when(OGCFrontController.class, "parseProtocol", anyString(), any(URL.class)).thenCallRealMethod();
		PowerMockito.when(OGCFrontController.class, "parsePort", anyString(), any(URL.class)).thenCallRealMethod();
		PowerMockito.when(OGCFrontController.getContext()).thenReturn(mockedContext);
	}

	private RequestContext mockContext(String serviceUrl) {
		return mockContext(serviceUrl, null);
	}

	private RequestContext mockContext(String serviceUrl, String xForwardedHost) {
		return mockContext(serviceUrl, xForwardedHost, null);
	}

	private RequestContext mockContext(String serviceUrl, String xForwardedHost, String xForwardedPort) {
		return mockContext(serviceUrl, xForwardedHost, xForwardedPort, "http");
	}

	private RequestContext mockContext(String serviceUrl, String xForwardedHost, String xForwardedPort,
			String xForwardedProto) {
		RequestContext context = mock(RequestContext.class);
		when(context.getServiceUrl()).thenReturn(serviceUrl);
		when(context.getXForwardedHost()).thenReturn(xForwardedHost);
		when(context.getXForwardedPort()).thenReturn(xForwardedPort);
		when(context.getXForwardedProto()).thenReturn(xForwardedProto);
		return context;
	}

}