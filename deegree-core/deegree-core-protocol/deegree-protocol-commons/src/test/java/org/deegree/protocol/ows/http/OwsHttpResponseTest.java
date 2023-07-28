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

import static java.nio.charset.StandardCharsets.UTF_8;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.conn.ClientConnectionManager;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.h2.util.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link OwsHttpResponse}.
 * <p>
 * 4 server response scenarios:
 * <ul>
 * <li>Scenario 1: status 200 / XML payload</li>
 * <li>Scenario 2: status 200 / OWS Exception report payload</li>
 * <li>Scenario 3: status 200 / binary payload</li>
 * <li>Scenario 4: status 500 / empty payload</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class OwsHttpResponseTest {

	private static final String SCENARIO1_RESPONSE = "scenario1.xml";

	private static final String SCENARIO2_RESPONSE = "scenario2.xml";

	private static final String SCENARIO3_RESPONSE = "scenario3.gif";

	private OwsHttpResponse scenario1;

	private OwsHttpResponse scenario2;

	private OwsHttpResponse scenario3;

	private OwsHttpResponse scenario4;

	private ClientConnectionManager connManager;

	@Before
	public void setup() throws Exception {
		connManager = mock(ClientConnectionManager.class);
		scenario1 = createScenario1();
		scenario2 = createScenario2();
		scenario3 = createScenario3();
		scenario4 = createScenario4();
	}

	/**
	 * Test method for
	 * {@link org.deegree.protocol.ows.http.OwsHttpResponse#getAsHttpResponse()}.
	 */
	@Test
	public void testGetAsHttpResponse() {
		assertNotNull(scenario1.getAsHttpResponse());
	}

	/**
	 * Test method for
	 * {@link org.deegree.protocol.ows.http.OwsHttpResponse#getAsBinaryStream()}.
	 */
	@Test
	public void testGetAsBinaryStreamScenario1() throws IOException {
		InputStream is = scenario1.getAsBinaryStream();
		String actual = org.apache.commons.io.IOUtils.toString(is, UTF_8);
		String expected = org.apache.commons.io.IOUtils
			.toString(OwsHttpResponseTest.class.getResourceAsStream("scenario1.xml"), UTF_8);
		assertThat(actual, isSimilarTo(expected).ignoreWhitespace());
	}

	/**
	 * Test method for
	 * {@link org.deegree.protocol.ows.http.OwsHttpResponse#getAsBinaryStream()}.
	 */
	@Test
	public void testGetAsBinaryStreamScenario2() throws IOException {
		InputStream is = scenario2.getAsBinaryStream();
		String actual = org.apache.commons.io.IOUtils.toString(is, UTF_8);
		String expected = org.apache.commons.io.IOUtils
			.toString(OwsHttpResponseTest.class.getResourceAsStream("scenario2.xml"), UTF_8);
		assertThat(actual, isSimilarTo(expected).ignoreWhitespace());
	}

	/**
	 * Test method for
	 * {@link org.deegree.protocol.ows.http.OwsHttpResponse#getAsBinaryStream()}.
	 */
	@Test
	public void testGetAsBinaryStreamScenario3() throws IOException {
		InputStream is = scenario3.getAsBinaryStream();
		byte[] readBytesAndClose = IOUtils.readBytesAndClose(is, -1);
		assertEquals(2107, readBytesAndClose.length);
	}

	/**
	 * Test method for
	 * {@link org.deegree.protocol.ows.http.OwsHttpResponse#getAsBinaryStream()}.
	 */
	@Test
	public void testGetAsBinaryStreamScenario4() throws IOException {
		InputStream is = scenario4.getAsBinaryStream();
		byte[] readBytesAndClose = IOUtils.readBytesAndClose(is, -1);
		assertEquals(0, readBytesAndClose.length);
	}

	/**
	 * Test method for
	 * {@link org.deegree.protocol.ows.http.OwsHttpResponse#getAsXMLStream()}.
	 */
	@Test
	public void testGetAsXMLStreamScenario1() throws OWSExceptionReport, XMLStreamException {
		XMLStreamReader xmlStream = scenario1.getAsXMLStream();
		int i = 0;
		while (xmlStream.hasNext()) {
			xmlStream.next();
			i++;
		}
		xmlStream.close();
		assertEquals(8215, i);
	}

	/**
	 * Test method for
	 * {@link org.deegree.protocol.ows.http.OwsHttpResponse#getAsXMLStream()}.
	 */
	@Test(expected = OWSExceptionReport.class)
	public void testGetAsXMLStreamScenario2() throws OWSExceptionReport, XMLStreamException {
		scenario2.getAsXMLStream();
	}

	/**
	 * Test method for
	 * {@link org.deegree.protocol.ows.http.OwsHttpResponse#getAsXMLStream()}.
	 */
	@Test(expected = XMLStreamException.class)
	public void testGetAsXMLStreamScenario3() throws OWSExceptionReport, XMLStreamException {
		scenario3.getAsXMLStream();
	}

	/**
	 * Test method for
	 * {@link org.deegree.protocol.ows.http.OwsHttpResponse#getAsXMLStream()}.
	 */
	@Test(expected = XMLStreamException.class)
	public void testGetAsXMLStreamScenario4() throws OWSExceptionReport, XMLStreamException {
		scenario4.getAsXMLStream();
	}

	/**
	 * Test method for
	 * {@link org.deegree.protocol.ows.http.OwsHttpResponse#assertHttpStatus200()}.
	 */
	@Test
	public void testAssertHttpStatus200Scenario1() throws OWSExceptionReport {
		scenario1.assertHttpStatus200();
	}

	/**
	 * Test method for
	 * {@link org.deegree.protocol.ows.http.OwsHttpResponse#assertHttpStatus200()}.
	 */
	@Test
	public void testAssertHttpStatus200Scenario2() throws OWSExceptionReport {
		scenario2.assertHttpStatus200();
	}

	/**
	 * Test method for
	 * {@link org.deegree.protocol.ows.http.OwsHttpResponse#assertHttpStatus200()}.
	 */
	@Test
	public void testAssertHttpStatus200Scenario3() throws OWSExceptionReport {
		scenario3.assertHttpStatus200();
	}

	/**
	 * Test method for
	 * {@link org.deegree.protocol.ows.http.OwsHttpResponse#assertHttpStatus200()}.
	 */
	@Test(expected = OWSExceptionReport.class)
	public void testAssertHttpStatus200Scenario4() throws OWSExceptionReport {
		scenario4.assertHttpStatus200();
	}

	/**
	 * Test method for {@link org.deegree.protocol.ows.http.OwsHttpResponse#close()}.
	 */
	@Test
	public void testClose() throws IOException {
		Mockito.verify(connManager, times(0)).shutdown();
		scenario1.close();
		Mockito.verify(connManager, times(1)).shutdown();
	}

	private OwsHttpResponse createScenario1() throws Exception {
		InputStream payload = OwsHttpResponseTest.class.getResourceAsStream(SCENARIO1_RESPONSE);
		HttpResponse httpResponse = mockHttpResponse(payload, 200);
		return new OwsHttpResponseImpl(httpResponse, connManager, "");
	}

	private OwsHttpResponse createScenario2() throws Exception {
		InputStream payload = OwsHttpResponseTest.class.getResourceAsStream(SCENARIO2_RESPONSE);
		HttpResponse httpResponse = mockHttpResponse(payload, 200);
		return new OwsHttpResponseImpl(httpResponse, connManager, "");
	}

	private OwsHttpResponse createScenario3() throws Exception {
		InputStream payload = OwsHttpResponseTest.class.getResourceAsStream(SCENARIO3_RESPONSE);
		HttpResponse httpResponse = mockHttpResponse(payload, 200);
		return new OwsHttpResponseImpl(httpResponse, connManager, "");
	}

	private OwsHttpResponse createScenario4() throws Exception {
		InputStream payload = new ByteArrayInputStream(new byte[0]);
		HttpResponse httpResponse = mockHttpResponse(payload, 500);
		return new OwsHttpResponseImpl(httpResponse, connManager, "");
	}

	private HttpResponse mockHttpResponse(InputStream payload, int status) throws IllegalStateException, IOException {
		HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
		HttpEntity mockedEntity = mockHttpEntity(payload);
		Mockito.when(httpResponse.getEntity()).thenReturn(mockedEntity);
		StatusLine mockedStatus = mockStatusLine(status);
		Mockito.when(httpResponse.getStatusLine()).thenReturn(mockedStatus);
		return httpResponse;
	}

	private StatusLine mockStatusLine(int status) {
		StatusLine statusLine = Mockito.mock(StatusLine.class);
		Mockito.when(statusLine.getStatusCode()).thenReturn(status);
		return statusLine;
	}

	private HttpEntity mockHttpEntity(InputStream mockedPayload) throws IllegalStateException, IOException {
		HttpEntity mock = Mockito.mock(HttpEntity.class);
		Mockito.when(mock.getContent()).thenReturn(mockedPayload);
		return mock;
	}

}
