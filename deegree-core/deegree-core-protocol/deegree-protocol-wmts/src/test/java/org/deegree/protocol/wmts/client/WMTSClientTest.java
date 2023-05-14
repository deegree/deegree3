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
package org.deegree.protocol.wmts.client;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.ows.http.OwsHttpClientMock;
import org.deegree.protocol.wmts.ops.GetTile;
import org.deegree.tile.TileMatrixSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for {@link WMTSClient}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class WMTSClientTest {

	private OwsHttpClientMock httpClientMock;

	private WMTSClient client;

	@Before
	public void setup() throws OWSExceptionReport, XMLStreamException, IOException {
		URL capaUrl = WMTSClientTest.class.getResource("wmts100_capabilities_example.xml");
		httpClientMock = new OwsHttpClientMock();
		client = new WMTSClient(capaUrl, httpClientMock);
	}

	/**
	 * Test method for
	 * {@link org.deegree.protocol.wmts.client.WMTSClient#getTile(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, int)}
	 * .
	 */
	@Test
	public void testGetTileOK() throws IOException, OWSExceptionReport, XMLStreamException {

		URL responseUrl = WMTSClientTest.class.getResource("gettile_response1.png");
		httpClientMock.setResponse(responseUrl, "image/png", 200);
		GetTile request = buildExampleRequest();
		GetTileResponse response = client.getTile(request);
		assertNotNull(response);
	}

	/**
	 * Test method for
	 * {@link org.deegree.protocol.wmts.client.WMTSClient#getTile(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, int)}
	 * .
	 */
	@Test(expected = OWSExceptionReport.class)
	public void testGetTileHttpStatus500() throws IOException, OWSExceptionReport, XMLStreamException {

		URL responseUrl = WMTSClientTest.class.getResource("gettile_response1.png");
		httpClientMock.setResponse(responseUrl, "image/png", 500);
		GetTile request = buildExampleRequest();
		client.getTile(request);
	}

	/**
	 * Test method for
	 * {@link org.deegree.protocol.wmts.client.WMTSClient#getTile(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, int)}
	 * .
	 */
	@Test(expected = OWSExceptionReport.class)
	public void testGetTileExceptionReport() throws IOException, OWSExceptionReport, XMLStreamException {

		URL responseUrl = WMTSClientTest.class.getResource("wmts100_exception_report.xml");
		httpClientMock.setResponse(responseUrl, "text/xml", 200);
		GetTile request = buildExampleRequest();
		GetTileResponse response = client.getTile(request);
		response.getAsImage();
	}

	@Test
	public void testGetLayers() throws XMLStreamException {
		List<Layer> clientLayers = client.getLayers();
		Assert.assertNotNull(clientLayers);
	}

	@Test
	public void testGetTileMatrixSets() throws XMLStreamException {
		List<TileMatrixSet> matrixSets = client.getTileMatrixSets();
		Assert.assertNotNull(matrixSets);
	}

	@Test
	public void testGetTileMatrixSet() throws XMLStreamException {
		TileMatrixSet matrixSet = client.getTileMatrixSet("Satellite_Provo");
		Assert.assertNotNull(matrixSet);
	}

	@Test
	public void testGetTileMatrixSetNotExists() throws XMLStreamException {
		TileMatrixSet matrixSet = client.getTileMatrixSet("Satellite_PROVO");
		Assert.assertNull(matrixSet);
	}

	private GetTile buildExampleRequest() {
		return new GetTile("medford:hydro", "_null", "image/png", "EPSG:900913", "EPSG:900913:24", 6203400, 2660870,
				null);
	}

}
