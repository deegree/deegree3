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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.tom.primitive.BaseType.STRING;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.geometry.primitive.segments.CurveSegment.CurveSegmentType.ARC;
import static org.deegree.geometry.primitive.segments.CurveSegment.CurveSegmentType.LINE_STRING_SEGMENT;
import static org.deegree.gml.GMLInputFactory.createGMLStreamReader;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.xpath.TypedObjectNodeXPathEvaluator;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.composite.CompositeSolid;
import org.deegree.geometry.composite.CompositeSurface;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSolid;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Curve.CurveType;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.OrientableSurface;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.PolyhedralSurface;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Ring.RingType;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Solid.SolidType;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.Surface.SurfaceType;
import org.deegree.geometry.primitive.Tin;
import org.deegree.geometry.primitive.TriangulatedSurface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.segments.Arc;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.standard.curvesegments.DefaultLineStringSegment;
import org.deegree.geometry.standard.primitive.DefaultCurve;
import org.deegree.geometry.standard.primitive.DefaultLinearRing;
import org.deegree.geometry.standard.primitive.DefaultRing;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.deegree.gml.schema.GMLAppSchemaReaderTest;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * Tests that check the correct parsing of GML 3 geometry elements.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class GML3GeometryReaderTest {

	private static final Logger LOG = getLogger(GML3GeometryReaderTest.class);

	private static final String BASE_DIR = "../misc/geometry/";

	private static double DELTA = 0.00000001;

	@Test
	public void parsePointPos() throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {

		GMLStreamReader gmlReader = getParser("Point_pos.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Point"), xmlReader.getName());
		Point point = (Point) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, gmlReader.getXMLReader().getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Point"), xmlReader.getName());
		Assert.assertEquals(7.12, point.get0(), DELTA);
		Assert.assertEquals(50.72, point.get1(), DELTA);
		Assert.assertEquals(2, point.getCoordinateDimension());
		Assert.assertEquals(CRSManager.lookup("EPSG:4326"), point.getCoordinateSystem());
	}

	@Test
	public void parsePointCoordinates()
			throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {

		GMLStreamReader gmlReader = getParser("Point_coordinates.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Point"), xmlReader.getName());
		Point point = (Point) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Point"), xmlReader.getName());
		Assert.assertEquals(7.12, point.get0(), DELTA);
		Assert.assertEquals(50.72, point.get1(), DELTA);
		Assert.assertEquals(2, point.getCoordinateDimension());
		Assert.assertEquals(CRSManager.lookup("EPSG:4326"), point.getCoordinateSystem());
	}

	@Test
	public void parsePointCoord()
			throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("Point_coord.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Point"), xmlReader.getName());
		Point point = (Point) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Point"), xmlReader.getName());
		Assert.assertEquals(7.12, point.get0(), DELTA);
		Assert.assertEquals(50.72, point.get1(), DELTA);
		Assert.assertEquals(2, point.getCoordinateDimension());
		Assert.assertEquals(CRSManager.lookup("EPSG:4326"), point.getCoordinateSystem());
	}

	@Test
	public void parseLineStringPos() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("LineString_pos.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "LineString"), xmlReader.getName());
		gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "LineString"), xmlReader.getName());
	}

	@Test
	public void parseLineStringPosList() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("LineString_posList.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "LineString"), xmlReader.getName());
		Curve curve = (Curve) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "LineString"), xmlReader.getName());
		Assert.assertEquals(1, curve.getCurveSegments().size());
		Assert.assertEquals(LINE_STRING_SEGMENT, curve.getCurveSegments().get(0).getSegmentType());
		Assert.assertEquals(3, curve.getAsLineString().getControlPoints().size());
		Assert.assertEquals(7.12, curve.getAsLineString().getControlPoints().get(0).get0(), DELTA);
		Assert.assertEquals(50.72, curve.getAsLineString().getControlPoints().get(0).get1(), DELTA);
		Assert.assertEquals(9.98, curve.getAsLineString().getControlPoints().get(1).get0(), DELTA);
		Assert.assertEquals(53.55, curve.getAsLineString().getControlPoints().get(1).get1(), DELTA);
		Assert.assertEquals(13.42, curve.getAsLineString().getControlPoints().get(2).get0(), DELTA);
		Assert.assertEquals(52.52, curve.getAsLineString().getControlPoints().get(2).get1(), DELTA);
	}

	@Test
	public void parseLineStringCoordinates() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("LineString_coordinates.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "LineString"), xmlReader.getName());
		Curve curve = (Curve) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "LineString"), xmlReader.getName());
		Assert.assertEquals(1, curve.getCurveSegments().size());
		Assert.assertEquals(LINE_STRING_SEGMENT, curve.getCurveSegments().get(0).getSegmentType());
		Assert.assertEquals(3, curve.getAsLineString().getControlPoints().size());
		Assert.assertEquals(7.12, curve.getAsLineString().getControlPoints().get(0).get0(), DELTA);
		Assert.assertEquals(50.72, curve.getAsLineString().getControlPoints().get(0).get1(), DELTA);
		Assert.assertEquals(9.98, curve.getAsLineString().getControlPoints().get(1).get0(), DELTA);
		Assert.assertEquals(53.55, curve.getAsLineString().getControlPoints().get(1).get1(), DELTA);
		Assert.assertEquals(13.42, curve.getAsLineString().getControlPoints().get(2).get0(), DELTA);
		Assert.assertEquals(52.52, curve.getAsLineString().getControlPoints().get(2).get1(), DELTA);
	}

	@Test
	public void parseLineStringPointProperty() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("LineString_pointProperty.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "LineString"), xmlReader.getName());
		Curve curve = (Curve) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "LineString"), xmlReader.getName());
		Assert.assertEquals(1, curve.getCurveSegments().size());
		Assert.assertEquals(LINE_STRING_SEGMENT, curve.getCurveSegments().get(0).getSegmentType());
		Assert.assertEquals(3, curve.getAsLineString().getControlPoints().size());
		Assert.assertEquals(7.12, curve.getAsLineString().getControlPoints().get(0).get0(), DELTA);
		Assert.assertEquals(50.72, curve.getAsLineString().getControlPoints().get(0).get1(), DELTA);
		Assert.assertEquals(9.98, curve.getAsLineString().getControlPoints().get(1).get0(), DELTA);
		Assert.assertEquals(53.55, curve.getAsLineString().getControlPoints().get(1).get1(), DELTA);
		Assert.assertEquals(13.42, curve.getAsLineString().getControlPoints().get(2).get0(), DELTA);
		Assert.assertEquals(52.52, curve.getAsLineString().getControlPoints().get(2).get1(), DELTA);
	}

	@Test
	public void parseLineStringPointRep() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("LineString_pointRep.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "LineString"), xmlReader.getName());
		Curve curve = (Curve) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "LineString"), xmlReader.getName());
		Assert.assertEquals(1, curve.getCurveSegments().size());
		Assert.assertEquals(LINE_STRING_SEGMENT, curve.getCurveSegments().get(0).getSegmentType());
		Assert.assertEquals(3, curve.getAsLineString().getControlPoints().size());
		Assert.assertEquals(7.12, curve.getAsLineString().getControlPoints().get(0).get0(), DELTA);
		Assert.assertEquals(50.72, curve.getAsLineString().getControlPoints().get(0).get1(), DELTA);
		Assert.assertEquals(9.98, curve.getAsLineString().getControlPoints().get(1).get0(), DELTA);
		Assert.assertEquals(53.55, curve.getAsLineString().getControlPoints().get(1).get1(), DELTA);
		Assert.assertEquals(13.42, curve.getAsLineString().getControlPoints().get(2).get0(), DELTA);
		Assert.assertEquals(52.52, curve.getAsLineString().getControlPoints().get(2).get1(), DELTA);
	}

	@Test
	public void parseLineStringCoord() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("LineString_coord.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "LineString"), xmlReader.getName());
		Curve curve = (Curve) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "LineString"), xmlReader.getName());
		Assert.assertEquals(1, curve.getCurveSegments().size());
		Assert.assertEquals(LINE_STRING_SEGMENT, curve.getCurveSegments().get(0).getSegmentType());
		Assert.assertEquals(3, curve.getAsLineString().getControlPoints().size());
		Assert.assertEquals(7.12, curve.getAsLineString().getControlPoints().get(0).get0(), DELTA);
		Assert.assertEquals(50.72, curve.getAsLineString().getControlPoints().get(0).get1(), DELTA);
		Assert.assertEquals(9.98, curve.getAsLineString().getControlPoints().get(1).get0(), DELTA);
		Assert.assertEquals(53.55, curve.getAsLineString().getControlPoints().get(1).get1(), DELTA);
		Assert.assertEquals(13.42, curve.getAsLineString().getControlPoints().get(2).get0(), DELTA);
		Assert.assertEquals(52.52, curve.getAsLineString().getControlPoints().get(2).get1(), DELTA);
	}

	@Test
	public void parseCurve() throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
			UnknownCRSException {
		GMLStreamReader gmlReader = getParser("Curve.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Curve"), xmlReader.getName());
		Curve curve = (Curve) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Curve"), xmlReader.getName());
		Assert.assertEquals(2, curve.getCurveSegments().size());
		Assert.assertEquals(ARC, curve.getCurveSegments().get(0).getSegmentType());
		Assert.assertEquals(LINE_STRING_SEGMENT, curve.getCurveSegments().get(1).getSegmentType());
	}

	@Test
	public void parseOrientableCurve() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("OrientableCurve.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "OrientableCurve"), xmlReader.getName());
		Curve curve = (Curve) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "OrientableCurve"), xmlReader.getName());
		Assert.assertEquals(2, curve.getCurveSegments().size());
		Assert.assertEquals(ARC, curve.getCurveSegments().get(0).getSegmentType());
		Assert.assertEquals(LINE_STRING_SEGMENT, curve.getCurveSegments().get(1).getSegmentType());
	}

	@Test
	public void parseLinearRing() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("LinearRing.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "LinearRing"), xmlReader.getName());
		Ring ring = (Ring) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "LinearRing"), xmlReader.getName());
		Assert.assertEquals(1, ring.getMembers().size());
		Assert.assertEquals(1, ring.getMembers().get(0).getCurveSegments().size());
		Assert.assertTrue(ring.getMembers().get(0).getCurveSegments().get(0) instanceof LineStringSegment);
		Assert.assertEquals(7, ring.getMembers().get(0).getAsLineString().getControlPoints().size());
	}

	@Test
	public void parseRing() throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
			UnknownCRSException {
		GMLStreamReader gmlReader = getParser("Ring.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Ring"), xmlReader.getName());
		Ring ring = (Ring) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Ring"), xmlReader.getName());
		Assert.assertEquals(2, ring.getMembers().size());
		Assert.assertEquals(2, ring.getMembers().get(0).getCurveSegments().size());
		Assert.assertTrue(ring.getMembers().get(0).getCurveSegments().get(0) instanceof Arc);
		Assert.assertTrue(ring.getMembers().get(0).getCurveSegments().get(1) instanceof Arc);
		Assert.assertEquals(1, ring.getMembers().get(1).getCurveSegments().size());
		Assert.assertTrue(ring.getMembers().get(1).getCurveSegments().get(0) instanceof LineStringSegment);
	}

	@Test
	public void parseRingWithLinearRing() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("RingWithLinearRing.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Ring"), xmlReader.getName());
		Ring ring = (Ring) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Ring"), xmlReader.getName());
		Assert.assertEquals(1, ring.getMembers().size());
		Assert.assertTrue(ring.getMembers().get(0) instanceof DefaultLinearRing);
		Assert.assertEquals(1, ring.getMembers().get(0).getCurveSegments().size());
		Assert.assertTrue(ring.getMembers().get(0).getCurveSegments().get(0) instanceof DefaultLineStringSegment);
	}

	@Test
	public void parseRingWithRing() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("RingWithRing.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Ring"), xmlReader.getName());
		Ring ring = (Ring) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Ring"), xmlReader.getName());
		Assert.assertEquals(1, ring.getMembers().size());
		Assert.assertTrue(ring.getMembers().get(0) instanceof DefaultRing);

		DefaultRing innerRing = (DefaultRing) ring.getMembers().get(0);
		Assert.assertEquals(2, innerRing.getMembers().size());
		Assert.assertTrue(innerRing.getMembers().get(0) instanceof DefaultCurve);
		Assert.assertEquals(2, innerRing.getMembers().get(0).getCurveSegments().size());
		Assert.assertTrue(innerRing.getMembers().get(0).getCurveSegments().get(0) instanceof Arc);
		Assert.assertTrue(innerRing.getMembers().get(0).getCurveSegments().get(1) instanceof Arc);
		Assert.assertTrue(innerRing.getMembers().get(1) instanceof DefaultCurve);
		Assert.assertEquals(1, innerRing.getMembers().get(1).getCurveSegments().size());
		Assert.assertTrue(innerRing.getMembers().get(1).getCurveSegments().get(0) instanceof LineStringSegment);
	}

	@Test
	public void parsePolygon() throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
			UnknownCRSException {
		GMLStreamReader gmlReader = getParser("Polygon.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Polygon"), xmlReader.getName());
		Polygon polygon = (Polygon) gmlReader.readGeometry();
		Assert.assertEquals(SurfaceType.Polygon, polygon.getSurfaceType());
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Polygon"), xmlReader.getName());
		Assert.assertEquals(RingType.LinearRing, polygon.getExteriorRing().getRingType());
		Assert.assertEquals(2, polygon.getInteriorRings().size());
		Assert.assertEquals(RingType.LinearRing, polygon.getInteriorRings().get(0).getRingType());
		Assert.assertEquals(RingType.LinearRing, polygon.getInteriorRings().get(1).getRingType());
	}

	@Test
	public void parseSurface() throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
			UnknownCRSException {
		GMLStreamReader gmlReader = getParser("Surface.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Surface"), xmlReader.getName());
		Surface surface = (Surface) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Surface"), xmlReader.getName());
		Assert.assertEquals(SurfaceType.Surface, surface.getSurfaceType());
		Assert.assertEquals(2, surface.getPatches().size());
	}

	@Test
	public void parsePolyhedralSurface() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("PolyhedralSurface.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "PolyhedralSurface"), xmlReader.getName());
		PolyhedralSurface surface = (PolyhedralSurface) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "PolyhedralSurface"), xmlReader.getName());
		Assert.assertEquals(SurfaceType.PolyhedralSurface, surface.getSurfaceType());
		Assert.assertEquals(2, surface.getPatches().size());
	}

	@Test
	public void parseTriangulatedSurface() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("TriangulatedSurface.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "TriangulatedSurface"), xmlReader.getName());
		TriangulatedSurface surface = (TriangulatedSurface) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "TriangulatedSurface"), xmlReader.getName());
		Assert.assertEquals(SurfaceType.TriangulatedSurface, surface.getSurfaceType());
		Assert.assertEquals(3, surface.getPatches().size());
	}

	@Test
	public void parseTin() throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
			UnknownCRSException {
		GMLStreamReader gmlReader = getParser("Tin.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Tin"), xmlReader.getName());
		Tin surface = (Tin) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Tin"), xmlReader.getName());
		Assert.assertEquals(SurfaceType.Tin, surface.getSurfaceType());
		Assert.assertEquals(2, surface.getStopLines().size());
		Assert.assertEquals(1, surface.getBreakLines().size());
		Assert.assertEquals(15.0, surface.getMaxLength(null).getValueAsDouble(), DELTA);
		Assert.assertEquals(3, surface.getControlPoints().size());
		Assert.assertEquals(3.0, surface.getControlPoints().get(2).get0(), DELTA);
		Assert.assertEquals(4.0, surface.getControlPoints().get(2).get1(), DELTA);
	}

	@Test
	public void parseOrientableSurface() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("OrientableSurface.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "OrientableSurface"), xmlReader.getName());
		OrientableSurface surface = (OrientableSurface) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "OrientableSurface"), xmlReader.getName());
		Assert.assertEquals(SurfaceType.OrientableSurface, surface.getSurfaceType());
		Assert.assertEquals(2, surface.getPatches().size());
	}

	@Test
	public void parseSolid() throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("Solid.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Solid"), xmlReader.getName());
		Solid solid = (Solid) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Solid"), xmlReader.getName());
		Assert.assertEquals(SolidType.Solid, solid.getSolidType());
		Assert.assertEquals(CRSManager.lookup("EPSG:31466"), solid.getCoordinateSystem());
		Assert.assertEquals(8, solid.getExteriorSurface().getPatches().size());
		Assert.assertEquals(2568786.096,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing()
					.getStartPoint()
					.get0(),
				DELTA);
		Assert.assertEquals(5662881.386,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing()
					.getStartPoint()
					.get1(),
				DELTA);
		Assert.assertEquals(60.3842642785516,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing()
					.getStartPoint()
					.get2(),
				DELTA);
		Assert.assertEquals(2568786.096,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing().getEndPoint().get0(),
				DELTA);
		Assert.assertEquals(5662881.386,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing().getEndPoint().get1(),
				DELTA);
		Assert.assertEquals(60.3842642785516,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing().getEndPoint().get2(),
				DELTA);
	}

	@Test
	public void parseCompositeCurve() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("CompositeCurve.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "CompositeCurve"), xmlReader.getName());
		Curve curve = (Curve) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "CompositeCurve"), xmlReader.getName());
		Assert.assertEquals(3, curve.getCurveSegments().size());
		Assert.assertEquals(ARC, curve.getCurveSegments().get(0).getSegmentType());
		Assert.assertEquals(LINE_STRING_SEGMENT, curve.getCurveSegments().get(1).getSegmentType());
		Assert.assertEquals(LINE_STRING_SEGMENT, curve.getCurveSegments().get(2).getSegmentType());
	}

	@Test
	public void parseCompositeSurface() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("CompositeSurface.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "CompositeSurface"), xmlReader.getName());
		CompositeSurface surface = (CompositeSurface) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "CompositeSurface"), xmlReader.getName());
		Assert.assertEquals(3, surface.getPatches().size());
	}

	@Test
	public void parseCompositeSolid()
			throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("CompositeSolid.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "CompositeSolid"), xmlReader.getName());
		CompositeSolid compositeSolid = (CompositeSolid) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "CompositeSolid"), xmlReader.getName());
		Assert.assertEquals(SolidType.CompositeSolid, compositeSolid.getSolidType());
		Solid solid = compositeSolid.get(0);
		Assert.assertEquals(SolidType.Solid, solid.getSolidType());
		Assert.assertEquals(CRSManager.lookup("EPSG:31466"), solid.getCoordinateSystem());
		Assert.assertEquals(8, solid.getExteriorSurface().getPatches().size());
		Assert.assertEquals(2568786.096,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing()
					.getStartPoint()
					.get0(),
				DELTA);
		Assert.assertEquals(5662881.386,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing()
					.getStartPoint()
					.get1(),
				DELTA);
		Assert.assertEquals(60.3842642785516,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing()
					.getStartPoint()
					.get2(),
				DELTA);
		Assert.assertEquals(2568786.096,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing().getEndPoint().get0(),
				DELTA);
		Assert.assertEquals(5662881.386,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing().getEndPoint().get1(),
				DELTA);
		Assert.assertEquals(60.3842642785516,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing().getEndPoint().get2(),
				DELTA);
	}

	@Test
	public void parseGeometricComplex()
			throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("GeometricComplex.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "GeometricComplex"), xmlReader.getName());
		CompositeGeometry<?> compositeGeometry = (CompositeGeometry<?>) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "GeometricComplex"), xmlReader.getName());
		Solid solid = (Solid) compositeGeometry.get(0);
		Assert.assertEquals(SolidType.Solid, solid.getSolidType());
		Assert.assertEquals(CRSManager.lookup("EPSG:31466"), solid.getCoordinateSystem());
		Assert.assertEquals(8, solid.getExteriorSurface().getPatches().size());
		Assert.assertEquals(2568786.096,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing()
					.getStartPoint()
					.get0(),
				DELTA);
		Assert.assertEquals(5662881.386,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing()
					.getStartPoint()
					.get1(),
				DELTA);
		Assert.assertEquals(60.3842642785516,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing()
					.getStartPoint()
					.get2(),
				DELTA);
		Assert.assertEquals(2568786.096,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing().getEndPoint().get0(),
				DELTA);
		Assert.assertEquals(5662881.386,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing().getEndPoint().get1(),
				DELTA);
		Assert.assertEquals(60.3842642785516,
				((PolygonPatch) solid.getExteriorSurface().getPatches().get(7)).getExteriorRing().getEndPoint().get2(),
				DELTA);
		Curve curve = (Curve) compositeGeometry.get(1);
		Assert.assertEquals(2, curve.getCurveSegments().size());
		Assert.assertEquals(ARC, curve.getCurveSegments().get(0).getSegmentType());
		Assert.assertEquals(LINE_STRING_SEGMENT, curve.getCurveSegments().get(1).getSegmentType());
	}

	@Test
	public void parseMultiPoint() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("MultiPoint.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiPoint"), xmlReader.getName());
		MultiPoint aggregate = (MultiPoint) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiPoint"), xmlReader.getName());
		Assert.assertEquals(3, aggregate.size());
	}

	@Test
	public void parseMultiPointMembers() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("MultiPoint_members.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiPoint"), xmlReader.getName());
		MultiPoint aggregate = (MultiPoint) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiPoint"), xmlReader.getName());
		Assert.assertEquals(3, aggregate.size());
	}

	@Test
	public void parseMultiPointMixed() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("MultiPoint_mixed.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiPoint"), xmlReader.getName());
		MultiPoint aggregate = (MultiPoint) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiPoint"), xmlReader.getName());
		Assert.assertEquals(6, aggregate.size());
	}

	@Test
	public void parseMultiCurve() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("MultiCurve.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiCurve"), xmlReader.getName());
		MultiCurve<?> aggregate = (MultiCurve<?>) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiCurve"), xmlReader.getName());
		Assert.assertEquals(2, aggregate.size());
		Assert.assertEquals(CurveType.Curve, aggregate.get(0).getCurveType());
		Assert.assertEquals(CurveType.CompositeCurve, aggregate.get(1).getCurveType());
	}

	@Test
	public void parseMultiSurface() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("MultiSurface.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiSurface"), xmlReader.getName());
		MultiSurface<?> aggregate = (MultiSurface<?>) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiSurface"), xmlReader.getName());
		Assert.assertEquals(2, aggregate.size());
		Assert.assertEquals(SurfaceType.Surface, aggregate.get(0).getSurfaceType());
		Assert.assertEquals(SurfaceType.TriangulatedSurface, aggregate.get(1).getSurfaceType());
	}

	@Test
	public void parseMultiPolygon() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("MultiPolygon.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiPolygon"), xmlReader.getName());
		MultiPolygon aggregate = (MultiPolygon) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiPolygon"), xmlReader.getName());
		Assert.assertEquals(2, aggregate.size());
		Assert.assertEquals(SurfaceType.Polygon, aggregate.get(0).getSurfaceType());
		Assert.assertEquals(SurfaceType.Polygon, aggregate.get(1).getSurfaceType());
	}

	@Test
	public void parseMultiSolid() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("MultiSolid.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiSolid"), xmlReader.getName());
		MultiSolid aggregate = (MultiSolid) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiSolid"), xmlReader.getName());
		Assert.assertEquals(2, aggregate.size());
		Assert.assertEquals(SolidType.Solid, aggregate.get(0).getSolidType());
		Assert.assertEquals(SolidType.CompositeSolid, aggregate.get(1).getSolidType());
	}

	@Test
	public void parseMultiGeometry() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("MultiGeometry.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiGeometry"), xmlReader.getName());
		MultiGeometry<?> aggregate = (MultiGeometry<?>) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiGeometry"), xmlReader.getName());
		Assert.assertEquals(5, aggregate.size());
		Assert.assertTrue(aggregate.get(0) instanceof Point);
		Assert.assertTrue(aggregate.get(1) instanceof Point);
		Assert.assertTrue(aggregate.get(2) instanceof Curve);
		Assert.assertTrue(aggregate.get(3) instanceof MultiSurface<?>);
		Assert.assertTrue(aggregate.get(4) instanceof CompositeSolid);
	}

	@Test
	public void parseMultiLineString() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("MultiLineString.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiLineString"), xmlReader.getName());
		MultiLineString aggregate = (MultiLineString) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiLineString"), xmlReader.getName());
		Assert.assertEquals(2, aggregate.size());
		Assert.assertEquals(CurveType.LineString, aggregate.get(0).getCurveType());
		Assert.assertEquals(CurveType.LineString, aggregate.get(1).getCurveType());
	}

	@Test
	public void parseEnvelope() throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("Envelope.gml");
		XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(gmlReader.getXMLReader(), null);
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Envelope"), xmlReader.getName());
		Envelope envelope = gmlReader.getGeometryReader().parseEnvelope(xmlReader, null);
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Envelope"), xmlReader.getName());
		Assert.assertEquals(11.0, envelope.getMin().get0(), DELTA);
		Assert.assertEquals(22.0, envelope.getMin().get1(), DELTA);
		Assert.assertEquals(44.0, envelope.getMax().get0(), DELTA);
		Assert.assertEquals(88.0, envelope.getMax().get1(), DELTA);
		Assert.assertEquals(CRSManager.lookup("EPSG:4326"), envelope.getCoordinateSystem());
	}

	@Test
	public void parseEnvelopeCoord()
			throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("Envelope_coord.gml");
		XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(gmlReader.getXMLReader(), null);
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Envelope"), xmlReader.getName());
		Envelope envelope = gmlReader.getGeometryReader().parseEnvelope(xmlReader, null);
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Envelope"), xmlReader.getName());
		Assert.assertEquals(11.0, envelope.getMin().get0(), DELTA);
		Assert.assertEquals(22.0, envelope.getMin().get1(), DELTA);
		Assert.assertEquals(44.0, envelope.getMax().get0(), DELTA);
		Assert.assertEquals(88.0, envelope.getMax().get1(), DELTA);
		Assert.assertEquals(CRSManager.lookup("EPSG:4326"), envelope.getCoordinateSystem());
	}

	@Test
	public void parseEnvelopePos()
			throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("Envelope_pos.gml");
		XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(gmlReader.getXMLReader(), null);
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Envelope"), xmlReader.getName());
		Envelope envelope = gmlReader.getGeometryReader().parseEnvelope(xmlReader, null);
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Envelope"), xmlReader.getName());
		Assert.assertEquals(11.0, envelope.getMin().get0(), DELTA);
		Assert.assertEquals(22.0, envelope.getMin().get1(), DELTA);
		Assert.assertEquals(44.0, envelope.getMax().get0(), DELTA);
		Assert.assertEquals(88.0, envelope.getMax().get1(), DELTA);
		Assert.assertEquals(CRSManager.lookup("EPSG:4326"), envelope.getCoordinateSystem());
	}

	@Test
	public void parseEnvelopeCoordinates()
			throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
		GMLStreamReader gmlReader = getParser("Envelope_coordinates.gml");
		XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(gmlReader.getXMLReader(), null);
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Envelope"), xmlReader.getName());
		Envelope envelope = gmlReader.getGeometryReader().parseEnvelope(xmlReader, null);
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "Envelope"), xmlReader.getName());
		Assert.assertEquals(11.0, envelope.getMin().get0(), DELTA);
		Assert.assertEquals(22.0, envelope.getMin().get1(), DELTA);
		Assert.assertEquals(44.0, envelope.getMax().get0(), DELTA);
		Assert.assertEquals(88.0, envelope.getMax().get1(), DELTA);
		Assert.assertEquals(CRSManager.lookup("EPSG:4326"), envelope.getCoordinateSystem());
	}

	@Test
	public void parseXLinkLineString()
			throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {

		GMLStreamReader gmlReader = getParser("XLinkLineString.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "LineString"), xmlReader.getName());
		LineString geom = (LineString) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "LineString"), xmlReader.getName());
		Assert.assertEquals(CRSManager.lookup("EPSG:4326"), geom.getCoordinateSystem());
		gmlReader.getIdContext().resolveLocalRefs();

		for (Point p : geom.getControlPoints()) {
			LOG.debug(p.getId() + ", " + p.getClass());
			LOG.debug("get0: " + p.get0());
		}
	}

	@Test
	public void parseXLinkMultiGeometry1() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, ReferenceResolvingException {
		GMLStreamReader gmlReader = getParser("XLinkMultiGeometry1.gml");
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		Assert.assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiGeometry"), xmlReader.getName());
		MultiGeometry<Geometry> geom = (MultiGeometry<Geometry>) gmlReader.readGeometry();
		Assert.assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		Assert.assertEquals(new QName("http://www.opengis.net/gml", "MultiGeometry"), xmlReader.getName());
		Assert.assertEquals(CRSManager.lookup("EPSG:4326"), geom.getCoordinateSystem());

		gmlReader.getIdContext().resolveLocalRefs();
		LineString ls = (LineString) geom.get(2);
		for (Point p : ls.getControlPoints()) {
			LOG.debug(p.getId() + ", " + p.getClass());
			LOG.debug("get0: " + p.get0());
		}
	}

	@Test
	public void parseStandardPropsGML311()
			throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException, ClassCastException,
			ClassNotFoundException, InstantiationException, IllegalAccessException {

		String schemaUrl = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
		GMLAppSchemaReader adapter = new GMLAppSchemaReader(GML_31, null, schemaUrl);
		AppSchema schema = adapter.extractAppSchema();

		GMLStreamReader gmlReader = getParser("StandardPropsGML311.gml");
		gmlReader.setApplicationSchema(schema);
		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		assertEquals(XMLStreamConstants.START_ELEMENT, xmlReader.getEventType());
		assertEquals(new QName("http://www.opengis.net/gml", "Point"), xmlReader.getName());
		Point point = (Point) gmlReader.readGeometry();
		assertEquals(XMLStreamConstants.END_ELEMENT, xmlReader.getEventType());
		assertEquals(new QName("http://www.opengis.net/gml", "Point"), xmlReader.getName());

		// geometry value
		assertEquals(7.12, point.get0(), DELTA);
		assertEquals(50.72, point.get1(), DELTA);
		assertEquals(2, point.getCoordinateDimension());
		assertEquals(CRSManager.lookup("EPSG:4326"), point.getCoordinateSystem());

		// standard properties
		assertEquals(6, point.getProperties().size());

		// gml:metaDataProperty
		assertEquals(QName.valueOf("{http://www.opengis.net/gml}metaDataProperty"),
				point.getProperties().get(0).getName());
		GenericXMLElement mdProp1 = (GenericXMLElement) point.getProperties().get(0).getValue();
		assertNotNull(mdProp1.getXSType());
		assertEquals(1, mdProp1.getChildren().size());
		GenericXMLElement genericMetadata = (GenericXMLElement) mdProp1.getChildren().get(0);
		Assert.assertNull(genericMetadata.getXSType());
		assertEquals(QName.valueOf("{http://www.opengis.net/gml}GenericMetaData"), genericMetadata.getName());
		assertEquals(1, genericMetadata.getChildren().size());
		PrimitiveValue pv = (PrimitiveValue) genericMetadata.getChildren().get(0);
		assertEquals(BaseType.STRING, pv.getType().getBaseType());
		assertEquals("Example for metadata: Ce point ne pas une point.", pv.getValue());

		// gml:metaDataProperty
		assertEquals(QName.valueOf("{http://www.opengis.net/gml}metaDataProperty"),
				point.getProperties().get(1).getName());
		GenericXMLElement mdProp2 = (GenericXMLElement) point.getProperties().get(1).getValue();
		assertNotNull(mdProp2.getXSType());
		assertEquals(1, mdProp2.getChildren().size());
		GenericXMLElement myMetadata = (GenericXMLElement) mdProp2.getChildren().get(0);
		Assert.assertNull(myMetadata.getXSType());
		assertEquals(QName.valueOf("{http://www.myns.com}MyMetaData"), myMetadata.getName());
		assertEquals(1, myMetadata.getChildren().size());
		pv = (PrimitiveValue) myMetadata.getChildren().get(0);
		assertEquals(STRING, pv.getType().getBaseType());
		assertEquals("Actually, in GML 3.1.1, anything may appear here, content is not constrained.", pv.getValue());

		// gml:description
		assertEquals(QName.valueOf("{http://www.opengis.net/gml}description"), point.getProperties().get(2).getName());
		GenericXMLElement descriptionProp = (GenericXMLElement) point.getProperties().get(2).getValue();
		assertNotNull(descriptionProp.getXSType());
		assertEquals(1, descriptionProp.getChildren().size());
		pv = (PrimitiveValue) descriptionProp.getChildren().get(0);
		assertEquals(STRING, pv.getType().getBaseType());
		assertEquals("This is just for testing the parsing of standard GML properties.", pv.getValue());

		// gml:name
		assertEquals(QName.valueOf("{http://www.opengis.net/gml}name"), point.getProperties().get(3).getName());
		GenericXMLElement nameProp1 = (GenericXMLElement) point.getProperties().get(3).getValue();
		assertNotNull(nameProp1.getXSType());
		assertEquals(1, nameProp1.getChildren().size());
		pv = (PrimitiveValue) nameProp1.getChildren().get(0);
		assertEquals(STRING, pv.getType().getBaseType());
		assertEquals("Point P1", pv.getValue());

		// gml:name
		assertEquals(QName.valueOf("{http://www.opengis.net/gml}name"), point.getProperties().get(4).getName());
		GenericXMLElement nameProp2 = (GenericXMLElement) point.getProperties().get(4).getValue();
		assertNotNull(nameProp2.getXSType());
		assertEquals(1, nameProp2.getChildren().size());
		pv = (PrimitiveValue) nameProp2.getChildren().get(0);
		assertEquals(STRING, pv.getType().getBaseType());
		assertEquals("P1", pv.getValue());

		// gml:name
		assertEquals(QName.valueOf("{http://www.opengis.net/gml}name"), point.getProperties().get(5).getName());
		GenericXMLElement nameProp3 = (GenericXMLElement) point.getProperties().get(5).getValue();
		assertNotNull(nameProp3.getXSType());
		assertEquals(1, nameProp3.getChildren().size());
		pv = (PrimitiveValue) nameProp3.getChildren().get(0);
		assertEquals(STRING, pv.getType().getBaseType());
		assertEquals("Point_P1", pv.getValue());
	}

	@Test
	public void parseAIXMPoint() throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
			UnknownCRSException, ClassCastException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, TransformationException, FilterEvaluationException {

		String aixmNs = "http://www.aixm.aero/schema/5.1";
		Point geom = (Point) readAIXMGeometry("AIXMPoint.gml");

		assertEquals(7.12, geom.get0(), DELTA);
		assertEquals(50.72, geom.get1(), DELTA);
		assertEquals(2, geom.getCoordinateDimension());
		assertEquals(CRSManager.lookup("EPSG:4326"), geom.getCoordinateSystem());

		List<Property> props = geom.getProperties();
		assertEquals(8, props.size());
		assertEquals(new QName(GML3_2_NS, "metaDataProperty"), props.get(0).getName());
		assertEquals(new QName(GML3_2_NS, "description"), props.get(1).getName());
		assertEquals(new QName(GML3_2_NS, "descriptionReference"), props.get(2).getName());
		assertEquals(new QName(GML3_2_NS, "identifier"), props.get(3).getName());
		assertEquals(new QName(GML3_2_NS, "name"), props.get(4).getName());
		assertEquals(new QName(GML3_2_NS, "name"), props.get(5).getName());
		assertEquals(new QName(aixmNs, "horizontalAccuracy"), props.get(6).getName());
		assertEquals(new QName(aixmNs, "annotation"), props.get(7).getName());

		assertEquals("Example for metadata: Ce point ne pas une GML point, c'est une AIXM point.",
				getPrimitive("gml:metaDataProperty/gml:GenericMetaData/text()", geom).getAsText());
		assertEquals("This is just for testing the parsing of standard GML properties.",
				getPrimitive("gml:description/text()", geom).getAsText());
		assertEquals("XYZ", getPrimitive("gml:identifier/text()", geom).getAsText());
		assertEquals("urn:blabla:bla", getPrimitive("gml:identifier/@codeSpace", geom).getAsText());
		assertEquals("Point P1", getPrimitive("gml:name[1]/text()", geom).getAsText());
		assertEquals("P1", getPrimitive("gml:name[2]/text()", geom).getAsText());
		assertEquals("1.0", getPrimitive("aixm:horizontalAccuracy/text()", geom).getAsText());
		assertEquals("M", getPrimitive("aixm:horizontalAccuracy/@uom", geom).getAsText());
	}

	@Test
	public void parseAIXMElevatedPoint() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException, ClassCastException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, TransformationException, FilterEvaluationException {

		String aixmNs = "http://www.aixm.aero/schema/5.1";
		Point geom = (Point) readAIXMGeometry("AIXMElevatedPoint.gml");

		assertEquals(7.12, geom.get0(), DELTA);
		assertEquals(50.72, geom.get1(), DELTA);
		assertEquals(2, geom.getCoordinateDimension());
		assertEquals(CRSManager.lookup("EPSG:4326"), geom.getCoordinateSystem());

		List<Property> props = geom.getProperties();
		assertEquals(12, props.size());
		assertEquals(new QName(GML3_2_NS, "metaDataProperty"), props.get(0).getName());
		assertEquals(new QName(GML3_2_NS, "description"), props.get(1).getName());
		assertEquals(new QName(GML3_2_NS, "descriptionReference"), props.get(2).getName());
		assertEquals(new QName(GML3_2_NS, "identifier"), props.get(3).getName());
		assertEquals(new QName(GML3_2_NS, "name"), props.get(4).getName());
		assertEquals(new QName(GML3_2_NS, "name"), props.get(5).getName());
		assertEquals(new QName(aixmNs, "horizontalAccuracy"), props.get(6).getName());
		assertEquals(new QName(aixmNs, "annotation"), props.get(7).getName());
		assertEquals(new QName(aixmNs, "elevation"), props.get(8).getName());
		assertEquals(new QName(aixmNs, "geoidUndulation"), props.get(9).getName());
		assertEquals(new QName(aixmNs, "verticalDatum"), props.get(10).getName());
		assertEquals(new QName(aixmNs, "verticalAccuracy"), props.get(11).getName());

		assertEquals("Example for metadata: Ce point ne pas une GML point, c'est une AIXM point.",
				getPrimitive("gml:metaDataProperty/gml:GenericMetaData/text()", geom).getAsText());
		assertEquals("This is just for testing the parsing of standard GML properties.",
				getPrimitive("gml:description/text()", geom).getAsText());
		assertEquals("XYZ", getPrimitive("gml:identifier/text()", geom).getAsText());
		assertEquals("urn:blabla:bla", getPrimitive("gml:identifier/@codeSpace", geom).getAsText());
		assertEquals("Point P1", getPrimitive("gml:name[1]/text()", geom).getAsText());
		assertEquals("P1", getPrimitive("gml:name[2]/text()", geom).getAsText());
		assertEquals("1.0", getPrimitive("aixm:horizontalAccuracy/text()", geom).getAsText());
		assertEquals("M", getPrimitive("aixm:horizontalAccuracy/@uom", geom).getAsText());
		assertEquals("47.11", getPrimitive("aixm:elevation/text()", geom).getAsText());
		assertEquals("M", getPrimitive("aixm:elevation/@uom", geom).getAsText());
		assertEquals("3.22", getPrimitive("aixm:geoidUndulation/text()", geom).getAsText());
		assertEquals("M", getPrimitive("aixm:geoidUndulation/@uom", geom).getAsText());
		assertEquals("NAVD88", getPrimitive("aixm:verticalDatum/text()", geom).getAsText());
		assertEquals("2.0", getPrimitive("aixm:verticalAccuracy/text()", geom).getAsText());
		assertEquals("M", getPrimitive("aixm:verticalAccuracy/@uom", geom).getAsText());
	}

	@Test
	public void parseAIXMCurve() throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
			UnknownCRSException, ClassCastException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, TransformationException, FilterEvaluationException {

		String aixmNs = "http://www.aixm.aero/schema/5.1";
		Curve geom = (Curve) readAIXMGeometry("AIXMCurve.gml");

		List<Property> props = geom.getProperties();
		assertEquals(8, props.size());
		assertEquals(new QName(GML3_2_NS, "metaDataProperty"), props.get(0).getName());
		assertEquals(new QName(GML3_2_NS, "description"), props.get(1).getName());
		assertEquals(new QName(GML3_2_NS, "descriptionReference"), props.get(2).getName());
		assertEquals(new QName(GML3_2_NS, "identifier"), props.get(3).getName());
		assertEquals(new QName(GML3_2_NS, "name"), props.get(4).getName());
		assertEquals(new QName(GML3_2_NS, "name"), props.get(5).getName());
		assertEquals(new QName(aixmNs, "horizontalAccuracy"), props.get(6).getName());
		assertEquals(new QName(aixmNs, "annotation"), props.get(7).getName());

		assertEquals("Example for metadata: Ce curve ne pas une GML curve, c'est une AIXM curve.",
				getPrimitive("gml:metaDataProperty/gml:GenericMetaData/text()", geom).getAsText());
		assertEquals("This is just for testing the parsing of standard GML properties.",
				getPrimitive("gml:description/text()", geom).getAsText());
		assertEquals("XYZ", getPrimitive("gml:identifier/text()", geom).getAsText());
		assertEquals("urn:blabla:bla", getPrimitive("gml:identifier/@codeSpace", geom).getAsText());
		assertEquals("Curve C1", getPrimitive("gml:name[1]/text()", geom).getAsText());
		assertEquals("C1", getPrimitive("gml:name[2]/text()", geom).getAsText());
		assertEquals("1.0", getPrimitive("aixm:horizontalAccuracy/text()", geom).getAsText());
		assertEquals("M", getPrimitive("aixm:horizontalAccuracy/@uom", geom).getAsText());
	}

	@Test
	public void parseAIXMElevatedCurve() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException, ClassCastException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, TransformationException, FilterEvaluationException {

		String aixmNs = "http://www.aixm.aero/schema/5.1";
		Curve geom = (Curve) readAIXMGeometry("AIXMElevatedCurve.gml");

		List<Property> props = geom.getProperties();
		assertEquals(12, props.size());
		assertEquals(new QName(GML3_2_NS, "metaDataProperty"), props.get(0).getName());
		assertEquals(new QName(GML3_2_NS, "description"), props.get(1).getName());
		assertEquals(new QName(GML3_2_NS, "descriptionReference"), props.get(2).getName());
		assertEquals(new QName(GML3_2_NS, "identifier"), props.get(3).getName());
		assertEquals(new QName(GML3_2_NS, "name"), props.get(4).getName());
		assertEquals(new QName(GML3_2_NS, "name"), props.get(5).getName());
		assertEquals(new QName(aixmNs, "horizontalAccuracy"), props.get(6).getName());
		assertEquals(new QName(aixmNs, "annotation"), props.get(7).getName());
		assertEquals(new QName(aixmNs, "elevation"), props.get(8).getName());
		assertEquals(new QName(aixmNs, "geoidUndulation"), props.get(9).getName());
		assertEquals(new QName(aixmNs, "verticalDatum"), props.get(10).getName());
		assertEquals(new QName(aixmNs, "verticalAccuracy"), props.get(11).getName());

		assertEquals("Example for metadata: Ce curve ne pas une GML curve, c'est une AIXM curve.",
				getPrimitive("gml:metaDataProperty/gml:GenericMetaData/text()", geom).getAsText());
		assertEquals("This is just for testing the parsing of standard GML properties.",
				getPrimitive("gml:description/text()", geom).getAsText());
		assertEquals("XYZ", getPrimitive("gml:identifier/text()", geom).getAsText());
		assertEquals("urn:blabla:bla", getPrimitive("gml:identifier/@codeSpace", geom).getAsText());
		assertEquals("Curve C1", getPrimitive("gml:name[1]/text()", geom).getAsText());
		assertEquals("C1", getPrimitive("gml:name[2]/text()", geom).getAsText());
		assertEquals("1.0", getPrimitive("aixm:horizontalAccuracy/text()", geom).getAsText());
		assertEquals("M", getPrimitive("aixm:horizontalAccuracy/@uom", geom).getAsText());
		assertEquals("47.11", getPrimitive("aixm:elevation/text()", geom).getAsText());
		assertEquals("M", getPrimitive("aixm:elevation/@uom", geom).getAsText());
		assertEquals("3.22", getPrimitive("aixm:geoidUndulation/text()", geom).getAsText());
		assertEquals("M", getPrimitive("aixm:geoidUndulation/@uom", geom).getAsText());
		assertEquals("NAVD88", getPrimitive("aixm:verticalDatum/text()", geom).getAsText());
		assertEquals("2.0", getPrimitive("aixm:verticalAccuracy/text()", geom).getAsText());
		assertEquals("M", getPrimitive("aixm:verticalAccuracy/@uom", geom).getAsText());
	}

	@Test
	public void parseAIXMSurface() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException, ClassCastException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, TransformationException, FilterEvaluationException {

		String aixmNs = "http://www.aixm.aero/schema/5.1";
		Surface geom = (Surface) readAIXMGeometry("AIXMSurface.gml");

		List<Property> props = geom.getProperties();
		assertEquals(8, props.size());
		assertEquals(new QName(GML3_2_NS, "metaDataProperty"), props.get(0).getName());
		assertEquals(new QName(GML3_2_NS, "description"), props.get(1).getName());
		assertEquals(new QName(GML3_2_NS, "descriptionReference"), props.get(2).getName());
		assertEquals(new QName(GML3_2_NS, "identifier"), props.get(3).getName());
		assertEquals(new QName(GML3_2_NS, "name"), props.get(4).getName());
		assertEquals(new QName(GML3_2_NS, "name"), props.get(5).getName());
		assertEquals(new QName(aixmNs, "horizontalAccuracy"), props.get(6).getName());
		assertEquals(new QName(aixmNs, "annotation"), props.get(7).getName());

		assertEquals("Example for metadata: Ce surface ne pas une GML surface, c'est une AIXM surface.",
				getPrimitive("gml:metaDataProperty/gml:GenericMetaData/text()", geom).getAsText());
		assertEquals("This is just for testing the parsing of standard GML properties.",
				getPrimitive("gml:description/text()", geom).getAsText());
		assertEquals("XYZ", getPrimitive("gml:identifier/text()", geom).getAsText());
		assertEquals("urn:blabla:bla", getPrimitive("gml:identifier/@codeSpace", geom).getAsText());
		assertEquals("Surface S1", getPrimitive("gml:name[1]/text()", geom).getAsText());
		assertEquals("S1", getPrimitive("gml:name[2]/text()", geom).getAsText());
		assertEquals("1.0", getPrimitive("aixm:horizontalAccuracy/text()", geom).getAsText());
		assertEquals("M", getPrimitive("aixm:horizontalAccuracy/@uom", geom).getAsText());
	}

	@Test
	public void parseAIXMElevatedSurface() throws XMLStreamException, FactoryConfigurationError, IOException,
			XMLParsingException, UnknownCRSException, ClassCastException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, TransformationException, FilterEvaluationException {

		String aixmNs = "http://www.aixm.aero/schema/5.1";
		Surface geom = (Surface) readAIXMGeometry("AIXMElevatedSurface.gml");

		List<Property> props = geom.getProperties();
		assertEquals(12, props.size());
		assertEquals(new QName(GML3_2_NS, "metaDataProperty"), props.get(0).getName());
		assertEquals(new QName(GML3_2_NS, "description"), props.get(1).getName());
		assertEquals(new QName(GML3_2_NS, "descriptionReference"), props.get(2).getName());
		assertEquals(new QName(GML3_2_NS, "identifier"), props.get(3).getName());
		assertEquals(new QName(GML3_2_NS, "name"), props.get(4).getName());
		assertEquals(new QName(GML3_2_NS, "name"), props.get(5).getName());
		assertEquals(new QName(aixmNs, "horizontalAccuracy"), props.get(6).getName());
		assertEquals(new QName(aixmNs, "annotation"), props.get(7).getName());
		assertEquals(new QName(aixmNs, "elevation"), props.get(8).getName());
		assertEquals(new QName(aixmNs, "geoidUndulation"), props.get(9).getName());
		assertEquals(new QName(aixmNs, "verticalDatum"), props.get(10).getName());
		assertEquals(new QName(aixmNs, "verticalAccuracy"), props.get(11).getName());

		assertEquals("Example for metadata: Ce surface ne pas une GML surface, c'est une AIXM surface.",
				getPrimitive("gml:metaDataProperty/gml:GenericMetaData/text()", geom).getAsText());
		assertEquals("This is just for testing the parsing of standard GML properties.",
				getPrimitive("gml:description/text()", geom).getAsText());
		assertEquals("XYZ", getPrimitive("gml:identifier/text()", geom).getAsText());
		assertEquals("urn:blabla:bla", getPrimitive("gml:identifier/@codeSpace", geom).getAsText());
		assertEquals("Surface S1", getPrimitive("gml:name[1]/text()", geom).getAsText());
		assertEquals("S1", getPrimitive("gml:name[2]/text()", geom).getAsText());
		assertEquals("1.0", getPrimitive("aixm:horizontalAccuracy/text()", geom).getAsText());
		assertEquals("M", getPrimitive("aixm:horizontalAccuracy/@uom", geom).getAsText());
		assertEquals("47.11", getPrimitive("aixm:elevation/text()", geom).getAsText());
		assertEquals("M", getPrimitive("aixm:elevation/@uom", geom).getAsText());
		assertEquals("3.22", getPrimitive("aixm:geoidUndulation/text()", geom).getAsText());
		assertEquals("M", getPrimitive("aixm:geoidUndulation/@uom", geom).getAsText());
		assertEquals("NAVD88", getPrimitive("aixm:verticalDatum/text()", geom).getAsText());
		assertEquals("2.0", getPrimitive("aixm:verticalAccuracy/text()", geom).getAsText());
		assertEquals("M", getPrimitive("aixm:verticalAccuracy/@uom", geom).getAsText());
	}

	private PrimitiveValue getPrimitive(String xpath, GMLObject object) throws FilterEvaluationException {
		TypedObjectNodeXPathEvaluator evaluator = new TypedObjectNodeXPathEvaluator();
		NamespaceBindings nsContext = new NamespaceBindings();
		nsContext.addNamespace("gml", GML3_2_NS);
		nsContext.addNamespace("aixm", "http://www.aixm.aero/schema/5.1");
		ValueReference path = new ValueReference(xpath, nsContext);
		TypedObjectNode[] eval = evaluator.eval(object, path);
		assertEquals(1, eval.length);
		assertTrue(eval[0] instanceof PrimitiveValue);
		return (PrimitiveValue) eval[0];
	}

	private Geometry readAIXMGeometry(String fileName)
			throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException, UnknownCRSException {

		String schemaUrl = GMLAppSchemaReaderTest.class.getResource("../aixm/schema/message/AIXM_BasicMessage.xsd")
			.toString();
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

	private GMLStreamReader getParser(String fileName)
			throws XMLStreamException, FactoryConfigurationError, IOException {
		return GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31,
				GML3SurfacePatchReaderTest.class.getResource(BASE_DIR + fileName));
	}

}
