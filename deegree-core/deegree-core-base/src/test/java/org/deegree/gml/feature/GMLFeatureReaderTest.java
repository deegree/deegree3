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
package org.deegree.gml.feature;

import static org.deegree.gml.GMLInputFactory.createGMLStreamReader;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.xerces.xs.XSElementDeclaration;
import org.deegree.commons.tom.ElementNode;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.timeslice.TimeSlice;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.deegree.time.primitive.TimePeriod;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * Tests that check the correct reading of {@link Feature} / {@link FeatureCollection}
 * objects from GML instance documents.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class GMLFeatureReaderTest {

	private static final String AIXM_NS = "http://www.aixm.aero/schema/5.1";

	private static final Logger LOG = getLogger(GMLFeatureReaderTest.class);

	@Test
	public void testParsingPhilosopherFeatureCollection() throws XMLStreamException, FactoryConfigurationError,
			IOException, XMLParsingException, UnknownCRSException, ReferenceResolvingException, ClassCastException,
			ClassNotFoundException, InstantiationException, IllegalAccessException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../misc/feature/Philosopher_FeatureCollection.xml");
		XMLStreamReader xmlReader = XMLInputFactory.newInstance()
			.createXMLStreamReader(docURL.toString(), docURL.openStream());
		xmlReader.next();
		GMLStreamReader gmlReader = createGMLStreamReader(GML_31, docURL);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();
		Assert.assertEquals(7, fc.size());
	}

	@Test
	public void testParsingPhilosopherFeatureCollectionStream() throws XMLStreamException, FactoryConfigurationError,
			IOException, XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../misc/feature/Philosopher_FeatureCollection.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_31, docURL);
		StreamFeatureCollection fc = gmlReader.readFeatureCollectionStream();

		int i = 0;
		while (fc.read() != null) {
			i++;
		}
		Assert.assertEquals(7, i);
	}

	@Test
	public void testParsingPhilosopherFeatureCollectionDynamicNoSchema()
			throws XMLStreamException, FactoryConfigurationError, IOException, ReferenceResolvingException,
			XMLParsingException, UnknownCRSException {

		URL docURL = GMLFeatureReaderTest.class
			.getResource("../misc/feature/Philosopher_FeatureCollection_no_schema.xml");
		XMLStreamReader xmlReader = XMLInputFactory.newInstance()
			.createXMLStreamReader(docURL.toString(), docURL.openStream());
		xmlReader.next();
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_31, docURL);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();
		Assert.assertEquals(7, fc.size());
		for (FeatureType ft : gmlReader.getAppSchema().getFeatureTypes()) {
			System.out.println(ft.getName());
		}
		// Assert.assertEquals( 4, gmlReader.getAppSchema().getFeatureTypes().length );
	}

	@Test
	public void testParsingCiteSF0() throws XMLStreamException, FactoryConfigurationError, IOException,
			ClassCastException, XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../cite/feature/dataset-sf0.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_31, docURL);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();
		for (Property prop : fc.getProperties()) {
			System.out.println(prop.getValue().getClass());
		}
		// Assert.assertEquals( fc.getGMLProperties().getDescription().getString(),
		// "Test data for assessing compliance with the GMLSF profile at level SF-0." );
		// Assert.assertEquals( fc.getGMLProperties().getNames().length, 1 );
		// Assert.assertEquals( fc.getGMLProperties().getNames()[0].getCode(),
		// "CITE/WFS-1.1" );
		// Assert.assertEquals( fc.getGMLProperties().getNames()[0].getCodeSpace(), null
		// );
		// Assert.assertEquals( 16, fc.size() );
	}

	@Test
	public void testParsingCiteSF0Stream() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../cite/feature/dataset-sf0.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31, docURL);
		StreamFeatureCollection fc = gmlReader.readFeatureCollectionStream();
		int i = 0;
		while (fc.read() != null) {
			i++;
		}
		Assert.assertEquals(16, i);
	}

	/**
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws XMLParsingException
	 * @throws UnknownCRSException
	 * @throws TransformationException
	 */
	@Test
	public void testParsingCiteSF1() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException, TransformationException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../cite/feature/dataset-sf1.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31, docURL);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		Assert.assertEquals(3, fc.size());
	}

	@Test
	public void testParsingCiteSF1Stream() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../cite/feature/dataset-sf1.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31, docURL);
		StreamFeatureCollection fc = gmlReader.readFeatureCollectionStream();
		int i = 0;
		while (fc.read() != null) {
			i++;
		}
		Assert.assertEquals(3, i);
	}

	@Test
	public void testParsingCiteSF2() throws XMLStreamException, FactoryConfigurationError, IOException,
			ClassCastException, XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../cite/feature/dataset-sf2.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31, docURL);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();
		Assert.assertEquals(29, fc.size());
	}

	@Test
	public void testParsingCiteSF2Stream() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../cite/feature/dataset-sf2.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31, docURL);
		StreamFeatureCollection fc = gmlReader.readFeatureCollectionStream();
		int i = 0;
		while (fc.read() != null) {
			i++;
		}
		Assert.assertEquals(29, i);
	}

	@Test
	public void testParsingCite100() throws FactoryConfigurationError, Exception {

		URL docURL = GMLFeatureReaderTest.class.getResource("../cite/feature/dataset.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_2, docURL);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();
		Assert.assertEquals(106, fc.size());
	}

	@Test
	public void testParsingCite100Stream() throws FactoryConfigurationError, Exception {

		URL docURL = GMLFeatureReaderTest.class.getResource("../cite/feature/dataset.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_2, docURL);
		StreamFeatureCollection fc = gmlReader.readFeatureCollectionStream();
		int i = 0;
		while (fc.read() != null) {
			i++;
		}
		Assert.assertEquals(106, i);
	}

	// @Test
	public void testParsingCityGML() throws XMLStreamException, FactoryConfigurationError, IOException,
			ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		String schemaURL = "http://schemas.opengis.net/citygml/profiles/base/1.0/CityGML.xsd";
		GMLAppSchemaReader adapter = new GMLAppSchemaReader(GMLVersion.GML_31, null, schemaURL);
		AppSchema schema = adapter.extractAppSchema();

		URL docURL = new URL("file:/home/schneider/Desktop/waldbruecke_v1.0.0.gml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31, docURL);
		gmlReader.setApplicationSchema(schema);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();

		// work with the fc
		for (Feature feature : fc) {
			LOG.debug("member fid: " + feature.getId());
		}
		LOG.debug("member features: " + fc.size());
	}

	// @Test
	public void testParsingXPlan20() throws XMLStreamException, FactoryConfigurationError, IOException,
			ClassCastException, XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		// BP2070
		URL docURL = new URL(
				"file:/home/schneider/workspace/lkee_xplanung2/resources/testdata/XPlanGML_2_0/BP2070.gml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31, docURL);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();

		// BP2135
		docURL = new URL("file:/home/schneider/workspace/lkee_xplanung2/resources/testdata/XPlanGML_2_0/BP2135.gml");
		gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31, docURL);
		fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();

		// PlanA
		docURL = new URL("file:/home/schneider/workspace/lkee_xplanung2/resources/testdata/XPlanGML_2_0/FPlan_2.0.gml");
		gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31, docURL);
		fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();

		// LA22
		docURL = new URL("file:/home/schneider/workspace/lkee_xplanung2/resources/testdata/XPlanGML_2_0/LA 22.gml");
		gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31, docURL);
		fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();

		// LA67
		docURL = new URL("file:/home/schneider/workspace/lkee_xplanung2/resources/testdata/XPlanGML_2_0/LA67_2_0.gml");
		gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31, docURL);
		fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();
		fc.size();
	}

	@Test
	public void testParsingCustomProps() throws XMLStreamException, FactoryConfigurationError, IOException,
			ClassCastException, XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../misc/feature/CustomProperties.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31, docURL);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();
		Feature feature = fc.iterator().next();

		Property custom1Prop = feature.getProperties(new QName("http://www.deegree.org/app", "custom1")).get(0);
		assertTrue(custom1Prop != null);
		assertNotNull(custom1Prop.getXSType());
		assertTrue(custom1Prop.getXSType() instanceof XSElementDeclaration);
		Assert.assertEquals(2, custom1Prop.getAttributes().size());
		PrimitiveValue mimeTypeAttr = custom1Prop.getAttributes().get(new QName("mimeType"));
		Assert.assertEquals("img/gif", mimeTypeAttr.getAsText());
		Assert.assertEquals("string", mimeTypeAttr.getType().getXSType().getName());
		PrimitiveValue lengthAttr = custom1Prop.getAttributes().get(new QName("length"));
		Assert.assertEquals("5657", lengthAttr.getAsText());
		Assert.assertEquals("positiveInteger", lengthAttr.getType().getXSType().getName());
		// assertNull (custom1PropValue.getChildren());

		// System.out.println( "type: " + custom1Prop.getType() );
		// System.out.println( "value: " + custom1Prop.getValue() );
		// Property custom2Prop = feature.getProperty( new QName(
		// "http://www.deegree.org/app", "custom2" ) );
		// System.out.println( "type: " + custom2Prop.getType() );
		// System.out.println( "value: " + custom2Prop.getValue() );
		// Property custom3Prop = feature.getProperty( new QName(
		// "http://www.deegree.org/app", "custom3" ) );
		// System.out.println( "type: " + custom3Prop.getType() );
		// System.out.println( "value: " + custom3Prop.getValue() );
	}

	@Test
	public void testINSPIREAddresses1() throws XMLStreamException, FactoryConfigurationError, IOException,
			ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL schemaURL = GMLFeatureReaderTest.class.getResource("../inspire/schema/Addresses.xsd");
		GMLAppSchemaReader adapter = new GMLAppSchemaReader(GML_32, null, schemaURL.toString());
		AppSchema schema = adapter.extractAppSchema();

		URL docURL = GMLFeatureReaderTest.class.getResource("../inspire/feature/inspire_addresses1.gml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_32, docURL);
		gmlReader.setApplicationSchema(schema);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();

		Assert.assertEquals(4, fc.size());
	}

	@Test
	public void testINSPIREAddresses1Stream() throws XMLStreamException, FactoryConfigurationError, IOException,
			ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL schemaURL = GMLFeatureReaderTest.class.getResource("../inspire/schema/Addresses.xsd");
		GMLAppSchemaReader adapter = new GMLAppSchemaReader(GML_32, null, schemaURL.toString());
		AppSchema schema = adapter.extractAppSchema();

		URL docURL = GMLFeatureReaderTest.class.getResource("../inspire/feature/inspire_addresses1.gml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_32, docURL);
		gmlReader.setApplicationSchema(schema);
		StreamFeatureCollection fc = gmlReader.readFeatureCollectionStream();
		int i = 0;
		while (fc.read() != null) {
			i++;
		}
		Assert.assertEquals(4, i);
	}

	@Test
	public void testINSPIREAddresses2() throws XMLStreamException, FactoryConfigurationError, IOException,
			ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL schemaURL = GMLFeatureReaderTest.class.getResource("../inspire/schema/Addresses.xsd");
		GMLAppSchemaReader adapter = new GMLAppSchemaReader(GML_32, null, schemaURL.toString());
		AppSchema schema = adapter.extractAppSchema();

		URL docURL = GMLFeatureReaderTest.class.getResource("../inspire/feature/inspire_addresses2.gml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_32, docURL);
		gmlReader.setApplicationSchema(schema);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();

		Assert.assertEquals(4, fc.size());
	}

	@Test
	public void testINSPIREAddresses2Stream() throws XMLStreamException, FactoryConfigurationError, IOException,
			ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL schemaURL = GMLFeatureReaderTest.class.getResource("../inspire/schema/Addresses.xsd");
		GMLAppSchemaReader adapter = new GMLAppSchemaReader(GML_32, null, schemaURL.toString());
		AppSchema schema = adapter.extractAppSchema();

		URL docURL = GMLFeatureReaderTest.class.getResource("../inspire/feature/inspire_addresses2.gml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_32, docURL);
		gmlReader.setApplicationSchema(schema);
		StreamFeatureCollection fc = gmlReader.readFeatureCollectionStream();
		int i = 0;
		while (fc.read() != null) {
			i++;
		}
		Assert.assertEquals(4, i);
	}

	@Test
	public void testGeoServerWFS100FC() throws XMLStreamException, FactoryConfigurationError, IOException,
			ClassCastException, XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../geoserver/feature/GeoServer_FC_WFS100.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_2, docURL);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();
		Assert.assertEquals(4, fc.size());
	}

	@Test
	public void testGeoServerWFS100FCStream() throws XMLStreamException, FactoryConfigurationError, IOException,
			ClassCastException, XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../geoserver/feature/GeoServer_FC_WFS100.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_2, docURL);
		StreamFeatureCollection fc = gmlReader.readFeatureCollectionStream();
		int i = 0;
		while (fc.read() != null) {
			i++;
		}
		Assert.assertEquals(4, i);
	}

	@Test
	public void testGeoServerWFS110FC() throws XMLStreamException, FactoryConfigurationError, IOException,
			ClassCastException, XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../geoserver/feature/GeoServer_FC_WFS110.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_31, docURL);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();
		Assert.assertEquals(4, fc.size());
	}

	@Test
	public void testGeoServerWFS110FCStream() throws XMLStreamException, FactoryConfigurationError, IOException,
			ClassCastException, XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../geoserver/feature/GeoServer_FC_WFS110.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_31, docURL);
		StreamFeatureCollection fc = gmlReader.readFeatureCollectionStream();
		int i = 0;
		while (fc.read() != null) {
			i++;
		}
		Assert.assertEquals(4, i);
	}

	@Test
	public void testGeoServerWFS100DynamicNoSchema() throws XMLStreamException, FactoryConfigurationError, IOException,
			ClassCastException, XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../geoserver/feature/GeoServer_FC_WFS100_no_schema.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_2, docURL);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();
		Assert.assertEquals(4, fc.size());
		Assert.assertEquals(1, gmlReader.getAppSchema().getFeatureTypes().length);
		Assert.assertEquals(23, gmlReader.getAppSchema().getFeatureTypes()[0].getPropertyDeclarations().size());
	}

	@Test
	public void testGeoServerWFS100DynamicNoSchemaFCStream() throws XMLStreamException, FactoryConfigurationError,
			IOException, ClassCastException, XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../geoserver/feature/GeoServer_FC_WFS100_no_schema.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_2, docURL);
		StreamFeatureCollection fc = gmlReader.readFeatureCollectionStream();
		int i = 0;
		while (fc.read() != null) {
			i++;
		}
		Assert.assertEquals(4, i);
		Assert.assertEquals(1, gmlReader.getAppSchema().getFeatureTypes().length);
		Assert.assertEquals(23, gmlReader.getAppSchema().getFeatureTypes()[0].getPropertyDeclarations().size());
	}

	@Test
	public void testGeoServerWFS110DynamicNoSchema() throws XMLStreamException, FactoryConfigurationError, IOException,
			ClassCastException, XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../geoserver/feature/GeoServer_FC_WFS110_no_schema.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_31, docURL);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();
		Assert.assertEquals(4, fc.size());
		Assert.assertEquals(1, gmlReader.getAppSchema().getFeatureTypes().length);
		Assert.assertEquals(23, gmlReader.getAppSchema().getFeatureTypes()[0].getPropertyDeclarations().size());
	}

	@Test
	public void testGeoServerWFS110DynamicNoSchemaFCStream() throws XMLStreamException, FactoryConfigurationError,
			IOException, ClassCastException, XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../geoserver/feature/GeoServer_FC_WFS110_no_schema.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_31, docURL);
		StreamFeatureCollection fc = gmlReader.readFeatureCollectionStream();
		int i = 0;
		while (fc.read() != null) {
			i++;
		}
		gmlReader.getIdContext().resolveLocalRefs();
		Assert.assertEquals(4, i);
		Assert.assertEquals(1, gmlReader.getAppSchema().getFeatureTypes().length);
		Assert.assertEquals(23, gmlReader.getAppSchema().getFeatureTypes()[0].getPropertyDeclarations().size());
	}

	@Test
	public void testDeegree2GetFeatureInfoResponse() throws XMLStreamException, FactoryConfigurationError, IOException,
			ClassCastException, XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		URL docURL = GMLFeatureReaderTest.class.getResource("../misc/feature/deegree2_getfeatureinfo.xml");
		GMLStreamReader gmlReader = createGMLStreamReader(GML_2, docURL);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		Assert.assertEquals(1, fc.size());
		Assert.assertEquals(1, gmlReader.getAppSchema().getFeatureTypes().length);
	}

	@Test
	public void testAIXM51BasicMessage() throws XMLStreamException, FactoryConfigurationError, IOException,
			ClassCastException, XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		final URL docURL = GMLFeatureReaderTest.class.getResource("../aixm/feature/AIXM51_BasicMessage.gml");
		final GMLStreamReader gmlReader = createGMLStreamReader(GML_32, docURL);
		final FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		assertEquals(157, gmlReader.getAppSchema().getFeatureTypes().length);
		assertEquals(35, gmlReader.getAppSchema().getGeometryTypes().size());
		assertEquals(182, fc.size());

		final Feature organisationAuthority = fc.iterator().next();
		assertEquals("ORGCIVIL_AVIATION", organisationAuthority.getId());
		final List<Property> timeSliceProps = organisationAuthority.getProperties(new QName(AIXM_NS, "timeSlice"));
		assertEquals(1, timeSliceProps.size());
		final Property timeSliceProp = timeSliceProps.get(0);
		final TimeSlice timeSlice = (TimeSlice) timeSliceProp.getValue();
		final List<Property> props = timeSlice.getProperties();
		assertEquals(6, props.size());
		final ElementNode validTime = props.get(0);
		assertEquals(new QName(GML_32.getNamespace(), "validTime"), validTime.getName());
		final TimePeriod timePeriod = (TimePeriod) validTime.getChildren().get(0);
		assertNotNull(timePeriod);
		assertEquals("rtvtOXS0", timePeriod.getId());

		final Feature f = getFeature(fc, "EADD");
		assertNotNull(f.getEnvelope());
	}

	private Feature getFeature(final FeatureCollection fc, final String gmlId) {
		final Iterator<Feature> iter = fc.iterator();
		while (iter.hasNext()) {
			final Feature f = iter.next();
			if (gmlId.equals(f.getId())) {
				return f;
			}
		}
		return null;
	}

}
