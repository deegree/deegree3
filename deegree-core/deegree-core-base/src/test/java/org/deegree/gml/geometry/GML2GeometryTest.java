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

import static org.deegree.gml.GMLVersion.GML_2;

import java.io.IOException;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.deegree.commons.xml.stax.SchemaLocationXMLStreamWriter;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.junit.XMLAssert;
import org.deegree.junit.XMLMemoryStreamWriter;
import org.junit.Assert;
import org.junit.Test;

/**
 * The class tests both the {@link GML2GeometryWriter} and the {@link GML2GeometryReader}.
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class GML2GeometryTest extends TestCase {

	private static final String BASE_DIR = "../misc/geometry/gml21/";

	private static final String GML21NS = "http://www.opengis.net/gml";

	private static final String BOX_FILE = "Box.gml";

	private static final String POINT_FILE = "Point.gml";

	private static final String POINT2_FILE = "Point2.gml";

	private static final String POLYGON_FILE = "Polygon.gml";

	private static final String LINESTRING_FILE = "LineString.gml";

	private static final String MULTIGEOMETRY_FILE = "MultiGeometry.gml";

	private static final String MULTILINESTRING_FILE = "MultiLineString.gml";

	private static final String MULTIPOINT_FILE = "MultiPoint.gml";

	private static final String MULTIPOLYGON_FILE = "MultiPolygon.gml";

	private static double DELTA = 0.00000001;

	private final String SCHEMA_LOCATION = "http://schemas.opengis.net/gml/2.1.2/geometry.xsd";

	private final String SCHEMA_LOCATION_ATTRIBUTE = "http://www.opengis.net/gml " + SCHEMA_LOCATION;

	/**
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws UnknownCRSException
	 * @throws TransformationException
	 */
	@Test
	public void testBox() throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException,
			TransformationException {
		XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(this.getClass().getResource(BASE_DIR + BOX_FILE));
		xmlReader.nextTag();

		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName(GML21NS, "Box"), xmlReader.getName());

		Envelope envelope = getGML2GeometryReader(xmlReader).parseEnvelope(xmlReader, null);
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName(GML21NS, "Box"), xmlReader.getName());
		Assert.assertEquals(0.0, envelope.getMin().get0(), DELTA);
		Assert.assertEquals(0.0, envelope.getMin().get1(), DELTA);
		Assert.assertEquals(100.0, envelope.getMax().get0(), DELTA);
		Assert.assertEquals(100.0, envelope.getMax().get1(), DELTA);
		Assert.assertEquals(CRSManager.lookup("http://www.opengis.net/gml/srs/epsg.xml#4326"),
				envelope.getCoordinateSystem());

		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", new Boolean(true));
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();

		SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
				SCHEMA_LOCATION_ATTRIBUTE);
		GMLStreamWriter exporter = GMLOutputFactory.createGMLStreamWriter(GML_2, writer);

		// writer.setPrefix( "app", "http://www.deegree.org" );
		// writer.setPrefix( "app", "http://www.deegree.org/app" );
		writer.setPrefix("gml", "http://www.opengis.net/gml");
		// writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
		// writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
		// writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
		// writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );

		exporter.write(envelope);
		writer.flush();

		XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
	}

	/**
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws UnknownCRSException
	 * @throws TransformationException
	 */
	@Test
	public void testPoint() throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException,
			TransformationException {
		XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
				this.getClass().getResource(BASE_DIR + POINT_FILE));
		xmlReader.nextTag();

		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName(GML21NS, "Point"), xmlReader.getName());

		Point point = getGML2GeometryReader(xmlReader).parsePoint(xmlReader, null);
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName(GML21NS, "Point"), xmlReader.getName());
		Assert.assertEquals(5.0, point.get0(), DELTA);
		Assert.assertEquals(40.0, point.get1(), DELTA);
		Assert.assertEquals(CRSManager.lookup("http://www.opengis.net/gml/srs/epsg.xml#4326"),
				point.getCoordinateSystem());

		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", new Boolean(true));
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();

		SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
				SCHEMA_LOCATION_ATTRIBUTE);
		GMLStreamWriter exporter = GMLOutputFactory.createGMLStreamWriter(GML_2, writer);
		writer.setPrefix("gml", "http://www.opengis.net/gml");
		exporter.write(point);
		writer.flush();

		XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
	}

	/**
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws UnknownCRSException
	 * @throws TransformationException
	 */
	@Test
	public void testPoint2() throws XMLStreamException, FactoryConfigurationError, IOException, TransformationException,
			UnknownCRSException {
		XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
				this.getClass().getResource(BASE_DIR + POINT2_FILE));
		xmlReader.nextTag();
		Point point = getGML2GeometryReader(xmlReader).parsePoint(xmlReader, null);
		Assert.assertEquals(5.0, point.get0(), DELTA);
		Assert.assertEquals(30.0, point.get1(), DELTA);

		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", new Boolean(true));
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();

		SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
				SCHEMA_LOCATION_ATTRIBUTE);
		GMLStreamWriter exporter = GMLOutputFactory.createGMLStreamWriter(GML_2, writer);
		writer.setPrefix("gml", "http://www.opengis.net/gml");
		exporter.write(point);
		writer.flush();

		XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
	}

	/**
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws UnknownCRSException
	 * @throws TransformationException
	 */
	@Test
	public void testPolygon() throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException,
			TransformationException {
		XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
				this.getClass().getResource(BASE_DIR + POLYGON_FILE));
		xmlReader.nextTag();

		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName(GML21NS, "Polygon"), xmlReader.getName());

		Polygon polygon = getGML2GeometryReader(xmlReader).parsePolygon(xmlReader, null);
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName(GML21NS, "Polygon"), xmlReader.getName());

		Points points = polygon.getExteriorRing().getControlPoints();
		comparePoint(0.0, 0.0, points.get(0));
		comparePoint(100.0, 0.0, points.get(1));
		comparePoint(100.0, 100.0, points.get(2));
		comparePoint(0.0, 100.0, points.get(3));
		comparePoint(0.0, 0.0, points.get(4));

		List<Points> innerPoints = polygon.getInteriorRingsCoordinates();
		Points points1 = innerPoints.get(0);
		comparePoint(10.0, 10.0, points1.get(0));
		comparePoint(10.0, 40.0, points1.get(1));
		comparePoint(40.0, 40.0, points1.get(2));
		comparePoint(40.0, 10.0, points1.get(3));
		comparePoint(10.0, 10.0, points1.get(4));

		Points points2 = innerPoints.get(1);
		comparePoint(60.0, 60.0, points2.get(0));
		comparePoint(60.0, 90.0, points2.get(1));
		comparePoint(90.0, 90.0, points2.get(2));
		comparePoint(90.0, 60.0, points2.get(3));
		comparePoint(60.0, 60.0, points2.get(4));

		Assert.assertEquals(CRSManager.lookup("http://www.opengis.net/gml/srs/epsg.xml#4326"),
				polygon.getCoordinateSystem());

		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", new Boolean(true));
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();

		SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
				SCHEMA_LOCATION_ATTRIBUTE);
		GMLStreamWriter exporter = GMLOutputFactory.createGMLStreamWriter(GML_2, writer);
		writer.setPrefix("gml", "http://www.opengis.net/gml");
		writer.setPrefix("xlink", "http://www.w3.org/1999/xlink");
		exporter.write(polygon);
		writer.flush();

		XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
	}

	/**
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws UnknownCRSException
	 * @throws TransformationException
	 */
	@Test
	public void testLineString() throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException,
			TransformationException {
		XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
				this.getClass().getResource(BASE_DIR + LINESTRING_FILE));
		xmlReader.nextTag();

		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName(GML21NS, "LineString"), xmlReader.getName());

		LineString lineString = getGML2GeometryReader(xmlReader).parseLineString(xmlReader, null);
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName(GML21NS, "LineString"), xmlReader.getName());

		Points controlPoints = lineString.getControlPoints();
		comparePoint(0.0, 0.0, controlPoints.get(0));
		comparePoint(20.0, 35.0, controlPoints.get(1));
		comparePoint(100.0, 100.0, controlPoints.get(2));

		Assert.assertEquals(CRSManager.lookup("http://www.opengis.net/gml/srs/epsg.xml#4326"),
				lineString.getCoordinateSystem());

		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", new Boolean(true));
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();

		SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
				SCHEMA_LOCATION_ATTRIBUTE);
		GMLStreamWriter exporter = GMLOutputFactory.createGMLStreamWriter(GML_2, writer);
		writer.setPrefix("gml", "http://www.opengis.net/gml");
		writer.setPrefix("xlink", "http://www.w3.org/1999/xlink");
		exporter.write(lineString);
		writer.flush();

		XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
	}

	/**
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws UnknownCRSException
	 * @throws TransformationException
	 */
	@Test
	public void testMultiGeometry() throws XMLStreamException, FactoryConfigurationError, IOException,
			TransformationException, UnknownCRSException {
		XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
				this.getClass().getResource(BASE_DIR + MULTIGEOMETRY_FILE));
		xmlReader.nextTag();

		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName(GML21NS, "MultiGeometry"), xmlReader.getName());

		MultiGeometry<?> multiGeometry = getGML2GeometryReader(xmlReader).parseMultiGeometry(xmlReader, null);
		assertEquals("c731", multiGeometry.getId());

		Point firstMember = (Point) multiGeometry.get(0);
		assertEquals("P6776", firstMember.getId());
		comparePoint(50.0, 50.0, firstMember);

		LineString secondMember = (LineString) multiGeometry.get(1);
		assertEquals("L21216", secondMember.getId());

		Points controlPoints = secondMember.getControlPoints();
		comparePoint(0.0, 0.0, controlPoints.get(0));
		comparePoint(0.0, 50.0, controlPoints.get(1));
		comparePoint(100.0, 50.0, controlPoints.get(2));

		Polygon thirdMember = (Polygon) multiGeometry.get(2);
		assertEquals("_877789", thirdMember.getId());

		Points points = thirdMember.getExteriorRing().getControlPoints();
		comparePoint(0.0, 0.0, points.get(0));
		comparePoint(100.0, 0.0, points.get(1));
		comparePoint(50.0, 100.0, points.get(2));
		comparePoint(0.0, 0.0, points.get(3));

		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", new Boolean(true));
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();

		SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
				SCHEMA_LOCATION_ATTRIBUTE);
		GMLStreamWriter exporter = GMLOutputFactory.createGMLStreamWriter(GML_2, writer);
		writer.setPrefix("gml", "http://www.opengis.net/gml");
		writer.setPrefix("xlink", "http://www.w3.org/1999/xlink");
		exporter.write(multiGeometry);
		writer.flush();

		XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
	}

	/**
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws UnknownCRSException
	 * @throws TransformationException
	 */
	@Test
	public void testMultiLineString() throws XMLStreamException, FactoryConfigurationError, IOException,
			TransformationException, UnknownCRSException {
		XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
				this.getClass().getResource(BASE_DIR + MULTILINESTRING_FILE));
		xmlReader.nextTag();

		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName(GML21NS, "MultiLineString"), xmlReader.getName());

		MultiLineString multiLineString = getGML2GeometryReader(xmlReader).parseMultiLineString(xmlReader, null);
		LineString firstMember = multiLineString.get(0);

		Points controlPoints = firstMember.getControlPoints();
		comparePoint(56.1, 0.45, controlPoints.get(0));
		comparePoint(67.23, 0.98, controlPoints.get(1));

		LineString secondMember = multiLineString.get(1);

		controlPoints = secondMember.getControlPoints();
		comparePoint(46.71, 9.25, controlPoints.get(0));
		comparePoint(56.88, 10.44, controlPoints.get(1));

		LineString thirdMember = multiLineString.get(2);

		controlPoints = thirdMember.getControlPoints();
		comparePoint(324.1, 219.7, controlPoints.get(0));
		comparePoint(0.45, 4.56, controlPoints.get(1));

		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", new Boolean(true));
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();

		SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
				SCHEMA_LOCATION_ATTRIBUTE);
		GMLStreamWriter exporter = GMLOutputFactory.createGMLStreamWriter(GML_2, writer);
		writer.setPrefix("gml", "http://www.opengis.net/gml");
		writer.setPrefix("xlink", "http://www.w3.org/1999/xlink");
		exporter.write(multiLineString);
		writer.flush();

		XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
	}

	/**
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws UnknownCRSException
	 * @throws TransformationException
	 */
	@Test
	public void testMultiPoint() throws XMLStreamException, FactoryConfigurationError, IOException,
			TransformationException, UnknownCRSException {
		XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
				this.getClass().getResource(BASE_DIR + MULTIPOINT_FILE));
		xmlReader.nextTag();

		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName(GML21NS, "MultiPoint"), xmlReader.getName());

		MultiPoint multiPoint = getGML2GeometryReader(xmlReader).parseMultiPoint(xmlReader, null);

		Point firstMember = multiPoint.get(0);
		comparePoint(5.0, 40.0, firstMember);

		Point secondMember = multiPoint.get(1);
		comparePoint(0.0, 0.0, secondMember);

		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", new Boolean(true));
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();

		SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
				SCHEMA_LOCATION_ATTRIBUTE);
		GMLStreamWriter exporter = GMLOutputFactory.createGMLStreamWriter(GML_2, writer);
		writer.setPrefix("gml", "http://www.opengis.net/gml");
		writer.setPrefix("xlink", "http://www.w3.org/1999/xlink");
		exporter.write(multiPoint);
		writer.flush();

		XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
	}

	/**
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws UnknownCRSException
	 * @throws TransformationException
	 */
	@Test
	public void testMultiPolygon() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
				this.getClass().getResource(BASE_DIR + MULTIPOLYGON_FILE));
		xmlReader.nextTag();

		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName(GML21NS, "MultiPolygon"), xmlReader.getName());

		MultiPolygon multiPolygon = getGML2GeometryReader(xmlReader).parseMultiPolygon(xmlReader, null);

		Polygon firstMember = multiPolygon.get(0);
		Points points = firstMember.getExteriorRing().getControlPoints();
		comparePoint(0.0, 0.0, points.get(0));
		comparePoint(100.0, 0.0, points.get(1));
		comparePoint(100.0, 100.0, points.get(2));
		comparePoint(0.0, 100.0, points.get(3));
		comparePoint(0.0, 0.0, points.get(4));

		List<Points> innerPoints = firstMember.getInteriorRingsCoordinates();
		Points points1 = innerPoints.get(0);
		comparePoint(10.0, 10.0, points1.get(0));
		comparePoint(10.0, 40.0, points1.get(1));
		comparePoint(40.0, 40.0, points1.get(2));
		comparePoint(40.0, 10.0, points1.get(3));
		comparePoint(10.0, 10.0, points1.get(4));

		Points points2 = innerPoints.get(1);
		comparePoint(60.0, 60.0, points2.get(0));
		comparePoint(60.0, 90.0, points2.get(1));
		comparePoint(90.0, 90.0, points2.get(2));
		comparePoint(90.0, 60.0, points2.get(3));
		comparePoint(60.0, 60.0, points2.get(4));

		Polygon secondMember = multiPolygon.get(1);
		points = secondMember.getExteriorRing().getControlPoints();
		comparePoint(0.0, 0.0, points.get(0));
		comparePoint(100.0, 0.0, points.get(1));
		comparePoint(100.0, 100.0, points.get(2));
		comparePoint(0.0, 100.0, points.get(3));
		comparePoint(0.0, 0.0, points.get(4));

		Assert.assertEquals(CRSManager.lookup("http://www.opengis.net/gml/srs/epsg.xml#4326"),
				multiPolygon.getCoordinateSystem());

		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", new Boolean(true));
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();

		SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
				SCHEMA_LOCATION_ATTRIBUTE);
		GMLStreamWriter exporter = GMLOutputFactory.createGMLStreamWriter(GML_2, writer);
		writer.setPrefix("gml", "http://www.opengis.net/gml");
		writer.setPrefix("xlink", "http://www.w3.org/1999/xlink");
		exporter.write(multiPolygon);
		writer.flush();

		XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
	}

	private GML2GeometryReader getGML2GeometryReader(XMLStreamReaderWrapper xmlReader) throws XMLStreamException {
		GMLStreamReader gmlStream = GMLInputFactory.createGMLStreamReader(GML_2, xmlReader);
		return (GML2GeometryReader) gmlStream.getGeometryReader();
	}

	private void comparePoint(double x, double y, Point point) {
		Assert.assertEquals(x, point.get0(), DELTA);
		Assert.assertEquals(y, point.get1(), DELTA);
	}

}
