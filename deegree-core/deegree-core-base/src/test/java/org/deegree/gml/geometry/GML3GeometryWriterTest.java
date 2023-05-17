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

package org.deegree.gml.geometry;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.gml.GMLInputFactory.createGMLStreamReader;
import static org.deegree.gml.GMLOutputFactory.createGMLStreamWriter;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.commons.xml.stax.SchemaLocationXMLStreamWriter;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.types.AppSchema;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.deegree.junit.XMLAssert;
import org.deegree.junit.XMLMemoryStreamWriter;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exporting all types of geometries and validating them.
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class GML3GeometryWriterTest {

	private static final Logger LOG = LoggerFactory.getLogger(GML3GeometryWriterTest.class);

	final static String DIR = "../misc/geometry/";

	final static String PATCH_DIR = "../misc/geometry/patches/";

	final static String SEGMENT_DIR = "../misc/geometry/segments/";

	private static List<String> sources = new ArrayList<String>();

	private static List<String> patchSources = new ArrayList<String>();

	private static List<String> segmentSources = new ArrayList<String>();

	private static List<String> envelopeSources = new ArrayList<String>();

	final String SCHEMA_LOCATION_ATTRIBUTE = "http://www.opengis.net/gml http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";

	final String SCHEMA_LOCATION = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";

	static {
		sources.add("CompositeCurve.gml");
		sources.add("CompositeSolid.gml");
		sources.add("CompositeSurface.gml");
		sources.add("Curve.gml");
		sources.add("GeometricComplex.gml");
		sources.add("LinearRing.gml");
		sources.add("LineString_coord.gml");
		sources.add("LineString_coordinates.gml");
		sources.add("LineString_pointProperty.gml");
		sources.add("LineString_pointRep.gml");
		sources.add("LineString_pos.gml");
		sources.add("LineString_posList.gml");
		sources.add("MultiCurve.gml");
		sources.add("MultiGeometry.gml");
		sources.add("MultiLineString.gml");
		sources.add("MultiPoint_members.gml");
		sources.add("MultiPolygon.gml");
		sources.add("MultiSolid.gml");
		sources.add("MultiSurface.gml");
		sources.add("OrientableCurve.gml");
		sources.add("OrientableSurface.gml");
		sources.add("Point_coord.gml");
		sources.add("Point_coordinates.gml");
		sources.add("Point_pos.gml");
		sources.add("Polygon.gml");
		sources.add("PolyhedralSurface.gml");
		sources.add("Ring.gml");
		sources.add("Solid.gml");
		sources.add("Surface.gml");
		sources.add("Tin.gml");
		sources.add("TriangulatedSurface.gml");

		patchSources.add("Cone.gml");
		patchSources.add("Cylinder.gml");
		patchSources.add("PolygonPatch.gml");
		patchSources.add("Rectangle.gml");
		patchSources.add("Sphere.gml");
		patchSources.add("Triangle.gml");

		segmentSources.add("Arc.gml");
		segmentSources.add("ArcByBulge.gml");
		segmentSources.add("ArcByCenterPoint.gml");
		segmentSources.add("ArcString.gml");
		segmentSources.add("ArcStringByBulge.gml");
		segmentSources.add("Bezier.gml");
		segmentSources.add("BSpline.gml");
		segmentSources.add("Circle.gml");
		segmentSources.add("CircleByCenterPoint.gml");
		segmentSources.add("Clothoid.gml");
		segmentSources.add("CubicSpline.gml");
		segmentSources.add("Geodesic.gml");
		segmentSources.add("GeodesicString.gml");
		segmentSources.add("LineStringSegment.gml");

		envelopeSources.add("Envelope_coord.gml");
		envelopeSources.add("Envelope_coordinates.gml");
		envelopeSources.add("Envelope_pos.gml");
		envelopeSources.add("Envelope.gml");
	}

	/**
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 * @throws UnknownCRSException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws TransformationException
	 */
	@Test
	public void testValidatingExportedAbstractGeometryTypes() throws XMLStreamException, XMLParsingException,
			UnknownCRSException, FactoryConfigurationError, IOException, TransformationException {

		for (String source : sources) {
			URL docURL = GML3GeometryWriterTest.class.getResource(DIR + source);
			GMLStreamReader parser = getGML31StreamReader(docURL);
			Geometry geom = parser.readGeometry();

			XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();

			SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
					SCHEMA_LOCATION_ATTRIBUTE);
			GMLStreamWriter exporter = GMLOutputFactory.createGMLStreamWriter(GML_31, writer);
			exporter.write(geom);
			writer.flush();

			XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
		}
	}

	/**
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 * @throws UnknownCRSException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws TransformationException
	 */
	@Test
	public void testValidatingExportedSurfacePatches() throws XMLStreamException, XMLParsingException,
			UnknownCRSException, FactoryConfigurationError, IOException, TransformationException {
		for (String patchSource : patchSources) {
			URL docURL = GML3GeometryWriterTest.class.getResource(PATCH_DIR + patchSource);
			if (docURL == null)
				LOG.debug("patch dir: " + GML3GeometryWriterTest.class.getResource(PATCH_DIR + patchSource));
			GMLStreamReader parser = getGML31StreamReader(docURL);
			XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(parser.getXMLReader(), docURL.toString());
			SurfacePatch surfPatch = ((GML3GeometryReader) parser.getGeometryReader()).getSurfacePatchReader()
				.parseSurfacePatch(xmlReader, null);
			XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
			SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
					SCHEMA_LOCATION_ATTRIBUTE);
			GMLStreamWriter exporter = GMLOutputFactory.createGMLStreamWriter(GML_31, writer);
			((GML3GeometryWriter) exporter.getGeometryWriter()).exportSurfacePatch(surfPatch);
			writer.flush();

			XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
		}
	}

	/**
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 * @throws UnknownCRSException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws TransformationException
	 */
	@Test
	public void testValidatingExportedCurveSegments() throws XMLStreamException, XMLParsingException,
			UnknownCRSException, FactoryConfigurationError, IOException, TransformationException {
		for (String segmentSource : segmentSources) {
			URL docURL = GML3GeometryWriterTest.class.getResource(SEGMENT_DIR + segmentSource);
			GMLStreamReader parser = getGML31StreamReader(docURL);
			XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(parser.getXMLReader(), docURL.toString());
			CurveSegment curveSegment = ((GML3GeometryReader) parser.getGeometryReader()).getCurveSegmentReader()
				.parseCurveSegment(xmlReader, null);
			XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
			SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
					SCHEMA_LOCATION_ATTRIBUTE);
			GMLStreamWriter exporter = GMLOutputFactory.createGMLStreamWriter(GML_31, writer);
			((GML3GeometryWriter) exporter.getGeometryWriter()).exportCurveSegment(curveSegment);
			writer.flush();

			XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
		}
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
	public void testValidatingExportedEnvelope() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException, TransformationException {

		for (String envelopeSource : envelopeSources) {
			URL docURL = GML3GeometryWriterTest.class.getResource(DIR + envelopeSource);
			GMLStreamReader parser = getGML31StreamReader(docURL);
			Geometry geom = parser.readGeometryOrEnvelope();

			XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
			SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
					SCHEMA_LOCATION_ATTRIBUTE);
			GMLStreamWriter exporter = GMLOutputFactory.createGMLStreamWriter(GML_31, writer);
			exporter.write(geom);
			writer.flush();

			XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
		}
	}

	/**
	 * @throws XMLParsingException
	 * @throws XMLStreamException
	 * @throws UnknownCRSException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws TransformationException
	 * @throws ReferenceResolvingException
	 */
	@Test
	public void testValidatingExportedXLinkMultiGeometry1()
			throws XMLParsingException, XMLStreamException, UnknownCRSException, FactoryConfigurationError, IOException,
			TransformationException, ReferenceResolvingException {

		String source = "XLinkMultiGeometry1.gml";
		URL docURL = GML3GeometryWriterTest.class.getResource(DIR + source);
		GMLStreamReader parser = getGML31StreamReader(docURL);
		Geometry geom = parser.readGeometry();
		parser.getIdContext().resolveLocalRefs();

		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
		SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
				SCHEMA_LOCATION_ATTRIBUTE);
		GMLStreamWriter exporter = GMLOutputFactory.createGMLStreamWriter(GML_31, writer);
		exporter.write(geom);
		writer.flush();

		XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
	}

	@Test
	public void testValidatingExportedXLinkMultiGeometry2()
			throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException, UnknownCRSException,
			TransformationException, ReferenceResolvingException {

		String source = "XLinkMultiGeometry2.gml";
		URL docURL = GML3GeometryWriterTest.class.getResource(DIR + source);
		GMLStreamReader parser = getGML31StreamReader(docURL);
		Geometry geom = parser.readGeometry();
		parser.getIdContext().resolveLocalRefs();

		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
		SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
				SCHEMA_LOCATION_ATTRIBUTE);
		GMLStreamWriter exporter = createGMLStreamWriter(GML_31, writer);
		exporter.write(geom);
		writer.flush();

		XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
	}

	@Test
	public void testValidatingExportedXLinkMultiLineString()
			throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException, UnknownCRSException,
			TransformationException, ReferenceResolvingException {

		String source = "XLinkMultiLineString.gml";
		URL docURL = GML3GeometryWriterTest.class.getResource(DIR + source);
		GMLStreamReader parser = getGML31StreamReader(docURL);
		Geometry geom = parser.readGeometry();
		parser.getIdContext().resolveLocalRefs();

		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
		SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
				SCHEMA_LOCATION_ATTRIBUTE);
		GMLStreamWriter exporter = createGMLStreamWriter(GML_31, writer);
		exporter.write(geom);
		writer.flush();

		XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
	}

	@Test
	public void testAIXMPoint() throws XMLParsingException, ClassCastException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		assertAIXMReexport("AIXMPoint.gml", "AIXMPoint-reexport.gml");
	}

	@Test
	public void testAIXMCurve() throws XMLParsingException, ClassCastException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		assertAIXMReexport("AIXMCurve.gml", "AIXMCurve-reexport.gml");
	}

	@Test
	public void testAIXMSurface() throws XMLParsingException, ClassCastException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		assertAIXMReexport("AIXMSurface.gml", "AIXMSurface-reexport.gml");
	}

	@Test
	public void testAIXMElevatedPoint() throws XMLParsingException, ClassCastException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		assertAIXMReexport("AIXMElevatedPoint.gml", "AIXMElevatedPoint-reexport.gml");
	}

	@Test
	public void testAIXMElevatedCurve() throws XMLParsingException, ClassCastException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		assertAIXMReexport("AIXMElevatedCurve.gml", "AIXMElevatedCurve-reexport.gml");
	}

	@Test
	public void testAIXMElevatedSurface() throws XMLParsingException, ClassCastException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		assertAIXMReexport("AIXMElevatedSurface.gml", "AIXMElevatedSurface-reexport.gml");
	}

	private void assertAIXMReexport(String srcFileName, String expectedFileName) throws XMLParsingException,
			ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException, TransformationException {

		String aixmNs = "http://www.aixm.aero/schema/5.1";
		Geometry geom = readAIXMGeometry(srcFileName);

		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
		XMLStreamWriter xmlWriter = new IndentingXMLStreamWriter(memoryWriter.getXMLStreamWriter());
		GMLStreamWriter exporter = createGMLStreamWriter(GML_32, xmlWriter);
		Map<String, String> nsBindings = new HashMap<String, String>();
		nsBindings.put("aixm", aixmNs);
		exporter.setNamespaceBindings(nsBindings);
		exporter.write(geom);
		xmlWriter.flush();

		String expected = IOUtils
			.toString(GML3GeometryWriterTest.class.getResourceAsStream("../aixm/geometry/" + expectedFileName), UTF_8);
		String actual = memoryWriter.toString();
		assertThat(actual, isSimilarTo(expected).ignoreWhitespace());
	}

	private Geometry readAIXMGeometry(String fileName)
			throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException, UnknownCRSException {

		String schemaUrl = this.getClass().getResource("../aixm/schema/message/AIXM_BasicMessage.xsd").toString();
		GMLAppSchemaReader adapter = new GMLAppSchemaReader(null, null, schemaUrl);
		AppSchema schema = adapter.extractAppSchema();

		GMLStreamReader gmlStream = createGMLStreamReader(GML_32,
				this.getClass().getResource("../aixm/geometry/" + fileName));
		gmlStream.setApplicationSchema(schema);

		XMLStreamReader xmlReader = gmlStream.getXMLReader();
		Assert.assertEquals(START_ELEMENT, xmlReader.getEventType());
		QName elName = xmlReader.getName();
		Assert.assertTrue(gmlStream.isGeometryElement());
		Geometry geom = gmlStream.readGeometry();
		Assert.assertEquals(END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(elName, xmlReader.getName());
		return geom;
	}

	private GMLStreamReader getGML31StreamReader(URL docURL)
			throws XMLStreamException, FactoryConfigurationError, IOException {
		return GMLInputFactory.createGMLStreamReader(GML_31, docURL);
	}

}
