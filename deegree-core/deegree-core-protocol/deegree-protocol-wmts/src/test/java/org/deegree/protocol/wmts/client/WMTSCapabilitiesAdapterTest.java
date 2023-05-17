/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static junit.framework.Assert.assertEquals;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipStartDocument;
import static org.deegree.protocol.wmts.WMTSConstants.WMTS_100_NS;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link WMTSCapabilitiesAdapter}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class WMTSCapabilitiesAdapterTest {

	private WMTSCapabilitiesAdapter adapter;

	@Before
	public void setup() throws OWSExceptionReport, XMLStreamException, IOException {
		adapter = new WMTSCapabilitiesAdapter();
		URL capaUrl = WMTSClientTest.class.getResource("wmts100_capabilities_example.xml");
		adapter.load(capaUrl);
	}

	/**
	 * Test method for
	 * {@link org.deegree.protocol.wmts.client.WMTSCapabilitiesAdapter#parseLayers()}.
	 */
	@Test
	public void testParseLayers() throws XMLStreamException {
		List<Layer> layers = adapter.parseLayers();
		assertEquals(1, layers.size());
	}

	@Test
	public void testParseLayer() throws Exception {

		XMLStreamReader xmlStream = getResourceXmlStreamReader("wmts100_layer.xml");

		assertEquals(START_ELEMENT, xmlStream.getEventType());
		assertEquals(new QName(WMTS_100_NS, "Layer"), xmlStream.getName());
		Layer layer = adapter.parseLayer(xmlStream);
		assertEquals(END_ELEMENT, xmlStream.getEventType());
		assertEquals(new QName(WMTS_100_NS, "Layer"), xmlStream.getName());

		assertEquals("utah_ortho", layer.getIdentifier());

		List<Style> styles = layer.getStyles();
		assertEquals(1, styles.size());
		Style style = styles.get(0);
		assertEquals("default", style.getIdentifier());
		assertEquals(true, style.isDefault());

		List<String> formats = layer.getFormats();
		assertEquals(1, formats.size());
		assertEquals("image/png", formats.get(0));

		List<String> tileMatrixSets = layer.getTileMatrixSets();
		assertEquals(1, tileMatrixSets.size());
		assertEquals("Satellite_Provo", tileMatrixSets.get(0));
	}

	@Test
	public void testParseTileMatrixSets() throws XMLStreamException {
		List<TileMatrixSet> tileMatrixSets = adapter.parseTileMatrixSets();
		assertEquals(1, tileMatrixSets.size());
	}

	@Test
	public void testParseTileMatrixSetWithoutTopLevelEnvelope() throws Exception {

		XMLStreamReader xmlStream = getResourceXmlStreamReader("wmts100_tilematrixset.xml");

		assertEquals(START_ELEMENT, xmlStream.getEventType());
		assertEquals(new QName(WMTS_100_NS, "TileMatrixSet"), xmlStream.getName());
		TileMatrixSet tileMatrixSet = adapter.parseTileMatrixSet(xmlStream);
		assertEquals(END_ELEMENT, xmlStream.getEventType());
		assertEquals(new QName(WMTS_100_NS, "TileMatrixSet"), xmlStream.getName());

		assertEquals("Satellite_Provo", tileMatrixSet.getIdentifier());
		SpatialMetadata spatialMetadata = tileMatrixSet.getSpatialMetadata();
		assertEquals("EPSG:26912", spatialMetadata.getCoordinateSystems().get(0).getAlias());
		assertEquals(441174.0, spatialMetadata.getEnvelope().getMin().get0(), 0.0001);
		assertEquals(4448359.0, spatialMetadata.getEnvelope().getMin().get1(), 0.0001);
		assertEquals(447830.0, spatialMetadata.getEnvelope().getMax().get0(), 0.0001);
		assertEquals(4456039.0, spatialMetadata.getEnvelope().getMax().get1(), 0.0001);
		assertEquals(4, tileMatrixSet.getTileMatrices().size());
	}

	@Test
	public void testParseTileMatrix() throws Exception {

		XMLStreamReader xmlStream = getResourceXmlStreamReader("wmts100_tilematrix.xml");
		ICRS crs = CRSManager.getCRSRef("EPSG:26912");

		assertEquals(START_ELEMENT, xmlStream.getEventType());
		assertEquals(new QName(WMTS_100_NS, "TileMatrix"), xmlStream.getName());
		TileMatrix tileMatrix = adapter.parseTileMatrix(xmlStream, crs);
		assertEquals(END_ELEMENT, xmlStream.getEventType());
		assertEquals(new QName(WMTS_100_NS, "TileMatrix"), xmlStream.getName());

		assertEquals("tilematrix1", tileMatrix.getIdentifier());
		assertEquals(2.0, tileMatrix.getResolution(), 0.000000001);
		assertEquals(512, tileMatrix.getTilePixelsX());
		assertEquals(256, tileMatrix.getTilePixelsY());
		assertEquals(3, tileMatrix.getNumTilesX());
		assertEquals(4, tileMatrix.getNumTilesY());

		SpatialMetadata spatialMetadata = tileMatrix.getSpatialMetadata();
		assertEquals(crs, spatialMetadata.getCoordinateSystems().get(0));
		assertEquals(441174.0, spatialMetadata.getEnvelope().getMin().get0(), 0.0001);
		assertEquals(4453991.0, spatialMetadata.getEnvelope().getMin().get1(), 0.0001);
		assertEquals(444246.0, spatialMetadata.getEnvelope().getMax().get0(), 0.0001);
		assertEquals(4456039.0, spatialMetadata.getEnvelope().getMax().get1(), 0.0001);
	}

	private XMLStreamReader getResourceXmlStreamReader(String resourcePath)
			throws XMLStreamException, FactoryConfigurationError, IOException {
		URL docUrl = WMTSClientTest.class.getResource(resourcePath);
		XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(docUrl.openStream());
		skipStartDocument(xmlStream);
		return xmlStream;
	}

}
