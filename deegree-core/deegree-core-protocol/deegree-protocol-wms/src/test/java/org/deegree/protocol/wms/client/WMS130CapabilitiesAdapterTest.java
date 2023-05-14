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
package org.deegree.protocol.wms.client;

import static org.deegree.cs.coordinatesystems.GeographicCRS.WGS84;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.LinkedList;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.deegree.commons.tom.ows.Version;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.junit.Test;

/**
 * Test cases for {@link WMS130CapabilitiesAdapter}
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class WMS130CapabilitiesAdapterTest extends WMSCapabilitiesAdapterTest {

	private static final String GETMAP_URL = "http://demo.deegree.org/deegree-wms-ri-130/services?";

	@Test(expected = IllegalArgumentException.class)
	public void testNullWMS130Capabilities() {
		new WMS130CapabilitiesAdapter(null);
	}

	@Test
	public void testWMS130CapabilitiesCoordinateSystem() throws XMLStreamException {
		WMSCapabilitiesAdapter capabilities = createCapabilities();

		LinkedList<String> coordinateSystems = capabilities.getCoordinateSystems("cite:NamedPlaces");
		assertEquals(2, coordinateSystems.size());
	}

	@Test
	public void testWMS130CapabilitiesgetBoundingBox() throws XMLStreamException, UnknownCRSException {
		WMSCapabilitiesAdapter capabilities = createCapabilities();
		Envelope boundingBox = capabilities.getBoundingBox("EPSG:4326", "citelayers");
		assertNotNull(boundingBox);
		Envelope bbox = (new GeometryFactory()).createEnvelope(-90, -180, 90, 180, CRSManager.lookup("EPSG:4326"));
		assertTrue(boundingBox.equals(bbox));
	}

	@Test
	public void testWMS130CapabilitiesgetLatLonBoundingBox() throws XMLStreamException, UnknownCRSException {
		WMSCapabilitiesAdapter capabilities = createCapabilities();
		Envelope boundingBox = capabilities.getLatLonBoundingBox("citelayers");
		assertNotNull(boundingBox);
		Envelope bbox = (new GeometryFactory()).createEnvelope(-180, -90, 180, 90, CRSManager.getCRSRef(WGS84));
		assertTrue(boundingBox.equals(bbox));
	}

	@Override
	protected WMSCapabilitiesAdapter createCapabilities() throws XMLStreamException {
		return createCapabilities("wms130.xml");
	}

	@Override
	protected WMSCapabilitiesAdapter createInspireCapabilities() throws XMLStreamException {
		return createCapabilities("wms130-inspire-capabilities.xml");
	}

	private WMSCapabilitiesAdapter createCapabilities(String capabilitiesFile) throws XMLStreamException {
		InputStream is = WMS130CapabilitiesAdapterTest.class.getResourceAsStream(capabilitiesFile);
		StAXOMBuilder builder = new StAXOMBuilder(is);
		OMElement capabilities = builder.getDocumentElement();
		WMSCapabilitiesAdapter adapter = new WMS130CapabilitiesAdapter(capabilities);
		adapter.parseWMSSpecificCapabilities(adapter.parseOperationsMetadata());
		return adapter;
	}

	@Override
	protected String getGetGetMapUrl() {
		return GETMAP_URL;
	}

	@Override
	protected String getPostGetMapUrl() {
		return GETMAP_URL;
	}

	@Override
	protected int getNoOfChildrenOfRootLayer() {
		return 1;
	}

	@Override
	protected Version getServiceVersion() {
		return new Version(1, 3, 0);
	}

}
