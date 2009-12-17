//$HeadURL$
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

import static org.deegree.geometry.primitive.segments.CurveSegment.CurveSegmentType.ARC;
import static org.deegree.geometry.primitive.segments.CurveSegment.CurveSegmentType.LINE_STRING_SEGMENT;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.CRSRegistry;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
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
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.OrientableSurface;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.PolyhedralSurface;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.Tin;
import org.deegree.geometry.primitive.TriangulatedSurface;
import org.deegree.geometry.primitive.Curve.CurveType;
import org.deegree.geometry.primitive.Ring.RingType;
import org.deegree.geometry.primitive.Solid.SolidType;
import org.deegree.geometry.primitive.Surface.SurfaceType;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.segments.Arc;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.gml.GMLDocumentIdContext;
import org.deegree.gml.ReferenceResolvingException;
import org.deegree.gml.GMLVersion;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests that check the correct decoding of GML 3.1.1 geometry elements (elements substitutable for
 * <code>gml:_Geometry</code> and <code>gml:Envelope</code>).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GML3GeometryReaderTest {

    private static GeometryFactory geomFac = new GeometryFactory();

    private static final String BASE_DIR = "../../geometry/gml/testdata/geometries/";

    private static double DELTA = 0.00000001;

    @Test
    public void parsePointPos()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {

        XMLStreamReaderWrapper xmlReader = getParser( "Point_pos.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Point point = (Point) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Assert.assertEquals( 7.12, point.get0(), DELTA );
        Assert.assertEquals( 50.72, point.get1(), DELTA );
        Assert.assertEquals( 2, point.getCoordinateDimension() );
        Assert.assertEquals( CRSRegistry.lookup( "EPSG:4326" ), point.getCoordinateSystem().getWrappedCRS() );
    }

    @Test
    public void parsePointCoordinates()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "Point_coordinates.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Point point = (Point) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Assert.assertEquals( 7.12, point.get0(), DELTA );
        Assert.assertEquals( 50.72, point.get1(), DELTA );
        Assert.assertEquals( 2, point.getCoordinateDimension() );
        Assert.assertEquals( CRSRegistry.lookup( "EPSG:4326" ), point.getCoordinateSystem().getWrappedCRS() );
    }

    @Test
    public void parsePointCoord()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "Point_coord.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Point point = (Point) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Assert.assertEquals( 7.12, point.get0(), DELTA );
        Assert.assertEquals( 50.72, point.get1(), DELTA );
        Assert.assertEquals( 2, point.getCoordinateDimension() );
        Assert.assertEquals( CRSRegistry.lookup( "EPSG:4326" ), point.getCoordinateSystem().getWrappedCRS() );
    }

    @Test
    public void parseLineStringPos()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "LineString_pos.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = (Curve) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
    }

    @Test
    public void parseLineStringPosList()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "LineString_posList.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = (Curve) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( 1, curve.getCurveSegments().size() );
        Assert.assertEquals( LINE_STRING_SEGMENT, curve.getCurveSegments().get( 0 ).getSegmentType() );
        Assert.assertEquals( 3, curve.getAsLineString().getControlPoints().size() );
        Assert.assertEquals( 7.12, curve.getAsLineString().getControlPoints().get( 0 ).get0(), DELTA );
        Assert.assertEquals( 50.72, curve.getAsLineString().getControlPoints().get( 0 ).get1(), DELTA );
        Assert.assertEquals( 9.98, curve.getAsLineString().getControlPoints().get( 1 ).get0(), DELTA );
        Assert.assertEquals( 53.55, curve.getAsLineString().getControlPoints().get( 1 ).get1(), DELTA );
        Assert.assertEquals( 13.42, curve.getAsLineString().getControlPoints().get( 2 ).get0(), DELTA );
        Assert.assertEquals( 52.52, curve.getAsLineString().getControlPoints().get( 2 ).get1(), DELTA );
    }

    @Test
    public void parseLineStringCoordinates()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "LineString_coordinates.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = (Curve) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( 1, curve.getCurveSegments().size() );
        Assert.assertEquals( LINE_STRING_SEGMENT, curve.getCurveSegments().get( 0 ).getSegmentType() );
        Assert.assertEquals( 3, curve.getAsLineString().getControlPoints().size() );
        Assert.assertEquals( 7.12, curve.getAsLineString().getControlPoints().get( 0 ).get0(), DELTA );
        Assert.assertEquals( 50.72, curve.getAsLineString().getControlPoints().get( 0 ).get1(), DELTA );
        Assert.assertEquals( 9.98, curve.getAsLineString().getControlPoints().get( 1 ).get0(), DELTA );
        Assert.assertEquals( 53.55, curve.getAsLineString().getControlPoints().get( 1 ).get1(), DELTA );
        Assert.assertEquals( 13.42, curve.getAsLineString().getControlPoints().get( 2 ).get0(), DELTA );
        Assert.assertEquals( 52.52, curve.getAsLineString().getControlPoints().get( 2 ).get1(), DELTA );
    }

    @Test
    public void parseLineStringPointProperty()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "LineString_pointProperty.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = (Curve) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( 1, curve.getCurveSegments().size() );
        Assert.assertEquals( LINE_STRING_SEGMENT, curve.getCurveSegments().get( 0 ).getSegmentType() );
        Assert.assertEquals( 3, curve.getAsLineString().getControlPoints().size() );
        Assert.assertEquals( 7.12, curve.getAsLineString().getControlPoints().get( 0 ).get0(), DELTA );
        Assert.assertEquals( 50.72, curve.getAsLineString().getControlPoints().get( 0 ).get1(), DELTA );
        Assert.assertEquals( 9.98, curve.getAsLineString().getControlPoints().get( 1 ).get0(), DELTA );
        Assert.assertEquals( 53.55, curve.getAsLineString().getControlPoints().get( 1 ).get1(), DELTA );
        Assert.assertEquals( 13.42, curve.getAsLineString().getControlPoints().get( 2 ).get0(), DELTA );
        Assert.assertEquals( 52.52, curve.getAsLineString().getControlPoints().get( 2 ).get1(), DELTA );
    }

    @Test
    public void parseLineStringPointRep()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "LineString_pointRep.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = (Curve) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( 1, curve.getCurveSegments().size() );
        Assert.assertEquals( LINE_STRING_SEGMENT, curve.getCurveSegments().get( 0 ).getSegmentType() );
        Assert.assertEquals( 3, curve.getAsLineString().getControlPoints().size() );
        Assert.assertEquals( 7.12, curve.getAsLineString().getControlPoints().get( 0 ).get0(), DELTA );
        Assert.assertEquals( 50.72, curve.getAsLineString().getControlPoints().get( 0 ).get1(), DELTA );
        Assert.assertEquals( 9.98, curve.getAsLineString().getControlPoints().get( 1 ).get0(), DELTA );
        Assert.assertEquals( 53.55, curve.getAsLineString().getControlPoints().get( 1 ).get1(), DELTA );
        Assert.assertEquals( 13.42, curve.getAsLineString().getControlPoints().get( 2 ).get0(), DELTA );
        Assert.assertEquals( 52.52, curve.getAsLineString().getControlPoints().get( 2 ).get1(), DELTA );
    }

    @Test
    public void parseLineStringCoord()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "LineString_coord.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = (Curve) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( 1, curve.getCurveSegments().size() );
        Assert.assertEquals( LINE_STRING_SEGMENT, curve.getCurveSegments().get( 0 ).getSegmentType() );
        Assert.assertEquals( 3, curve.getAsLineString().getControlPoints().size() );
        Assert.assertEquals( 7.12, curve.getAsLineString().getControlPoints().get( 0 ).get0(), DELTA );
        Assert.assertEquals( 50.72, curve.getAsLineString().getControlPoints().get( 0 ).get1(), DELTA );
        Assert.assertEquals( 9.98, curve.getAsLineString().getControlPoints().get( 1 ).get0(), DELTA );
        Assert.assertEquals( 53.55, curve.getAsLineString().getControlPoints().get( 1 ).get1(), DELTA );
        Assert.assertEquals( 13.42, curve.getAsLineString().getControlPoints().get( 2 ).get0(), DELTA );
        Assert.assertEquals( 52.52, curve.getAsLineString().getControlPoints().get( 2 ).get1(), DELTA );
    }

    @Test
    public void parseCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "Curve.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Curve" ), xmlReader.getName() );
        Curve curve = (Curve) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Curve" ), xmlReader.getName() );
        Assert.assertEquals( 2, curve.getCurveSegments().size() );
        Assert.assertEquals( ARC, curve.getCurveSegments().get( 0 ).getSegmentType() );
        Assert.assertEquals( LINE_STRING_SEGMENT, curve.getCurveSegments().get( 1 ).getSegmentType() );
    }

    @Test
    public void parseOrientableCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "OrientableCurve.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "OrientableCurve" ), xmlReader.getName() );
        Curve curve = (Curve) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "OrientableCurve" ), xmlReader.getName() );
        Assert.assertEquals( 2, curve.getCurveSegments().size() );
        Assert.assertEquals( ARC, curve.getCurveSegments().get( 0 ).getSegmentType() );
        Assert.assertEquals( LINE_STRING_SEGMENT, curve.getCurveSegments().get( 1 ).getSegmentType() );
    }

    @Test
    public void parseLinearRing()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "LinearRing.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LinearRing" ), xmlReader.getName() );
        Ring ring = (Ring) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LinearRing" ), xmlReader.getName() );
        Assert.assertEquals( 1, ring.getMembers().size() );
        Assert.assertEquals( 1, ring.getMembers().get( 0 ).getCurveSegments().size() );
        Assert.assertTrue( ring.getMembers().get( 0 ).getCurveSegments().get( 0 ) instanceof LineStringSegment );
        Assert.assertEquals( 7, ring.getMembers().get( 0 ).getAsLineString().getControlPoints().size() );
    }

    @Test
    public void parseRing()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "Ring.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Ring" ), xmlReader.getName() );
        Ring ring = (Ring) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Ring" ), xmlReader.getName() );
        Assert.assertEquals( 2, ring.getMembers().size() );
        Assert.assertEquals( 2, ring.getMembers().get( 0 ).getCurveSegments().size() );
        Assert.assertTrue( ring.getMembers().get( 0 ).getCurveSegments().get( 0 ) instanceof Arc );
        Assert.assertTrue( ring.getMembers().get( 0 ).getCurveSegments().get( 1 ) instanceof Arc );
        Assert.assertEquals( 1, ring.getMembers().get( 1 ).getCurveSegments().size() );
        Assert.assertTrue( ring.getMembers().get( 1 ).getCurveSegments().get( 0 ) instanceof LineStringSegment );
    }

    @Test
    public void parsePolygon()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "Polygon.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Polygon" ), xmlReader.getName() );
        Polygon polygon = (Polygon) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( SurfaceType.Polygon, polygon.getSurfaceType() );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Polygon" ), xmlReader.getName() );
        Assert.assertEquals( RingType.LinearRing, polygon.getExteriorRing().getRingType() );
        Assert.assertEquals( 2, polygon.getInteriorRings().size() );
        Assert.assertEquals( RingType.LinearRing, polygon.getInteriorRings().get( 0 ).getRingType() );
        Assert.assertEquals( RingType.LinearRing, polygon.getInteriorRings().get( 1 ).getRingType() );
    }

    @Test
    public void parseSurface()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "Surface.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Surface" ), xmlReader.getName() );
        Surface surface = (Surface) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Surface" ), xmlReader.getName() );
        Assert.assertEquals( SurfaceType.Surface, surface.getSurfaceType() );
        Assert.assertEquals( 2, surface.getPatches().size() );
    }

    @Test
    public void parsePolyhedralSurface()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "PolyhedralSurface.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "PolyhedralSurface" ), xmlReader.getName() );
        PolyhedralSurface surface = (PolyhedralSurface) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader,
                                                                                                           null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "PolyhedralSurface" ), xmlReader.getName() );
        Assert.assertEquals( SurfaceType.PolyhedralSurface, surface.getSurfaceType() );
        Assert.assertEquals( 2, surface.getPatches().size() );
    }

    @Test
    public void parseTriangulatedSurface()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "TriangulatedSurface.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "TriangulatedSurface" ), xmlReader.getName() );
        TriangulatedSurface surface = (TriangulatedSurface) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse(
                                                                                                               xmlReader,
                                                                                                               null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "TriangulatedSurface" ), xmlReader.getName() );
        Assert.assertEquals( SurfaceType.TriangulatedSurface, surface.getSurfaceType() );
        Assert.assertEquals( 3, surface.getPatches().size() );
    }

    @Test
    public void parseTin()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "Tin.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Tin" ), xmlReader.getName() );
        Tin surface = (Tin) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Tin" ), xmlReader.getName() );
        Assert.assertEquals( SurfaceType.Tin, surface.getSurfaceType() );
        Assert.assertEquals( 2, surface.getStopLines().size() );
        Assert.assertEquals( 1, surface.getBreakLines().size() );
        Assert.assertEquals( 15.0, surface.getMaxLength( null ).getValueAsDouble(), DELTA );
        Assert.assertEquals( 3, surface.getControlPoints().size() );
        Assert.assertEquals( 3.0, surface.getControlPoints().get( 2 ).get0(), DELTA );
        Assert.assertEquals( 4.0, surface.getControlPoints().get( 2 ).get1(), DELTA );
    }

    @Test
    public void parseOrientableSurface()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "OrientableSurface.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "OrientableSurface" ), xmlReader.getName() );
        OrientableSurface surface = (OrientableSurface) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader,
                                                                                                           null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "OrientableSurface" ), xmlReader.getName() );
        Assert.assertEquals( SurfaceType.OrientableSurface, surface.getSurfaceType() );
        Assert.assertEquals( 2, surface.getPatches().size() );
    }

    @Test
    public void parseSolid()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "Solid.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Solid" ), xmlReader.getName() );
        Solid solid = (Solid) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Solid" ), xmlReader.getName() );
        Assert.assertEquals( SolidType.Solid, solid.getSolidType() );
        Assert.assertEquals( CRSRegistry.lookup( "EPSG:31466" ), solid.getCoordinateSystem().getWrappedCRS() );
        Assert.assertEquals( 8, solid.getExteriorSurface().getPatches().size() );
        Assert.assertEquals(
                             2568786.096,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getStartPoint().get0(),
                             DELTA );
        Assert.assertEquals(
                             5662881.386,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getStartPoint().get1(),
                             DELTA );
        Assert.assertEquals(
                             60.3842642785516,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getStartPoint().get2(),
                             DELTA );
        Assert.assertEquals(
                             2568786.096,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getEndPoint().get0(),
                             DELTA );
        Assert.assertEquals(
                             5662881.386,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getEndPoint().get1(),
                             DELTA );
        Assert.assertEquals(
                             60.3842642785516,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getEndPoint().get2(),
                             DELTA );
    }

    @Test
    public void parseCompositeCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "CompositeCurve.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "CompositeCurve" ), xmlReader.getName() );
        Curve curve = (Curve) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "CompositeCurve" ), xmlReader.getName() );
        Assert.assertEquals( 3, curve.getCurveSegments().size() );
        Assert.assertEquals( ARC, curve.getCurveSegments().get( 0 ).getSegmentType() );
        Assert.assertEquals( LINE_STRING_SEGMENT, curve.getCurveSegments().get( 1 ).getSegmentType() );
        Assert.assertEquals( LINE_STRING_SEGMENT, curve.getCurveSegments().get( 2 ).getSegmentType() );
    }

    @Test
    public void parseCompositeSurface()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "CompositeSurface.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "CompositeSurface" ), xmlReader.getName() );
        CompositeSurface surface = (CompositeSurface) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader,
                                                                                                         null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "CompositeSurface" ), xmlReader.getName() );
        Assert.assertEquals( 3, surface.getPatches().size() );
    }

    @Test
    public void parseCompositeSolid()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "CompositeSolid.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "CompositeSolid" ), xmlReader.getName() );
        CompositeSolid compositeSolid = (CompositeSolid) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader,
                                                                                                            null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "CompositeSolid" ), xmlReader.getName() );
        Assert.assertEquals( SolidType.CompositeSolid, compositeSolid.getSolidType() );
        Solid solid = compositeSolid.get( 0 );
        Assert.assertEquals( SolidType.Solid, solid.getSolidType() );
        Assert.assertEquals( CRSRegistry.lookup( "EPSG:31466" ), solid.getCoordinateSystem().getWrappedCRS() );
        Assert.assertEquals( 8, solid.getExteriorSurface().getPatches().size() );
        Assert.assertEquals(
                             2568786.096,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getStartPoint().get0(),
                             DELTA );
        Assert.assertEquals(
                             5662881.386,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getStartPoint().get1(),
                             DELTA );
        Assert.assertEquals(
                             60.3842642785516,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getStartPoint().get2(),
                             DELTA );
        Assert.assertEquals(
                             2568786.096,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getEndPoint().get0(),
                             DELTA );
        Assert.assertEquals(
                             5662881.386,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getEndPoint().get1(),
                             DELTA );
        Assert.assertEquals(
                             60.3842642785516,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getEndPoint().get2(),
                             DELTA );
    }

    @Test
    public void parseGeometricComplex()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "GeometricComplex.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "GeometricComplex" ), xmlReader.getName() );
        CompositeGeometry compositeGeometry = (CompositeGeometry) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse(
                                                                                                                     xmlReader,
                                                                                                                     null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "GeometricComplex" ), xmlReader.getName() );
        Solid solid = (Solid) compositeGeometry.get( 0 );
        Assert.assertEquals( SolidType.Solid, solid.getSolidType() );
        Assert.assertEquals( CRSRegistry.lookup( "EPSG:31466" ), solid.getCoordinateSystem().getWrappedCRS() );
        Assert.assertEquals( 8, solid.getExteriorSurface().getPatches().size() );
        Assert.assertEquals(
                             2568786.096,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getStartPoint().get0(),
                             DELTA );
        Assert.assertEquals(
                             5662881.386,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getStartPoint().get1(),
                             DELTA );
        Assert.assertEquals(
                             60.3842642785516,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getStartPoint().get2(),
                             DELTA );
        Assert.assertEquals(
                             2568786.096,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getEndPoint().get0(),
                             DELTA );
        Assert.assertEquals(
                             5662881.386,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getEndPoint().get1(),
                             DELTA );
        Assert.assertEquals(
                             60.3842642785516,
                             ( (PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 ) ).getExteriorRing().getEndPoint().get2(),
                             DELTA );
        Curve curve = (Curve) compositeGeometry.get( 1 );
        Assert.assertEquals( 2, curve.getCurveSegments().size() );
        Assert.assertEquals( ARC, curve.getCurveSegments().get( 0 ).getSegmentType() );  
        Assert.assertEquals( LINE_STRING_SEGMENT, curve.getCurveSegments().get( 1 ).getSegmentType() );  
    }

    @Test
    public void parseMultiPoint()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "MultiPoint.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiPoint" ), xmlReader.getName() );
        MultiPoint aggregate = (MultiPoint) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiPoint" ), xmlReader.getName() );
        Assert.assertEquals( 3, aggregate.size() );
    }

    @Test
    public void parseMultiPointMembers()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "MultiPoint_members.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiPoint" ), xmlReader.getName() );
        MultiPoint aggregate = (MultiPoint) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiPoint" ), xmlReader.getName() );
        Assert.assertEquals( 3, aggregate.size() );
    }

    @Test
    public void parseMultiPointMixed()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "MultiPoint_mixed.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiPoint" ), xmlReader.getName() );
        MultiPoint aggregate = (MultiPoint) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiPoint" ), xmlReader.getName() );
        Assert.assertEquals( 6, aggregate.size() );
    }

    @Test
    public void parseMultiCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "MultiCurve.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiCurve" ), xmlReader.getName() );
        MultiCurve aggregate = (MultiCurve) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiCurve" ), xmlReader.getName() );
        Assert.assertEquals( 2, aggregate.size() );
        Assert.assertEquals( CurveType.Curve, aggregate.get( 0 ).getCurveType() );
        Assert.assertEquals( CurveType.CompositeCurve, aggregate.get( 1 ).getCurveType() );
    }

    @Test
    public void parseMultiSurface()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "MultiSurface.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiSurface" ), xmlReader.getName() );
        MultiSurface aggregate = (MultiSurface) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiSurface" ), xmlReader.getName() );
        Assert.assertEquals( 2, aggregate.size() );
        Assert.assertEquals( SurfaceType.Surface, aggregate.get( 0 ).getSurfaceType() );
        Assert.assertEquals( SurfaceType.TriangulatedSurface, aggregate.get( 1 ).getSurfaceType() );
    }

    @Test
    public void parseMultiPolygon()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "MultiPolygon.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiPolygon" ), xmlReader.getName() );
        MultiPolygon aggregate = (MultiPolygon) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiPolygon" ), xmlReader.getName() );
        Assert.assertEquals( 2, aggregate.size() );
        Assert.assertEquals( SurfaceType.Polygon, aggregate.get( 0 ).getSurfaceType() );
        Assert.assertEquals( SurfaceType.Polygon, aggregate.get( 1 ).getSurfaceType() );
    }

    @Test
    public void parseMultiSolid()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "MultiSolid.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiSolid" ), xmlReader.getName() );
        MultiSolid aggregate = (MultiSolid) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiSolid" ), xmlReader.getName() );
        Assert.assertEquals( 2, aggregate.size() );
        Assert.assertEquals( SolidType.Solid, aggregate.get( 0 ).getSolidType() );
        Assert.assertEquals( SolidType.CompositeSolid, aggregate.get( 1 ).getSolidType() );
    }

    @Test
    public void parseMultiGeometry()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "MultiGeometry.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiGeometry" ), xmlReader.getName() );
        MultiGeometry<Geometry> aggregate = (MultiGeometry<Geometry>) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse(
                                                                                                                         xmlReader,
                                                                                                                         null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiGeometry" ), xmlReader.getName() );
        Assert.assertEquals( 5, aggregate.size() );
        Assert.assertTrue( aggregate.get( 0 ) instanceof Point );
        Assert.assertTrue( aggregate.get( 1 ) instanceof Point );
        Assert.assertTrue( aggregate.get( 2 ) instanceof Curve );
        Assert.assertTrue( aggregate.get( 3 ) instanceof MultiSurface );
        Assert.assertTrue( aggregate.get( 4 ) instanceof CompositeSolid );
    }

    @Test
    public void parseMultiLineString()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "MultiLineString.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiLineString" ), xmlReader.getName() );
        MultiLineString aggregate = (MultiLineString) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader,
                                                                                                         null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiLineString" ), xmlReader.getName() );
        Assert.assertEquals( 2, aggregate.size() );
        Assert.assertEquals( CurveType.LineString, aggregate.get( 0 ).getCurveType() );
        Assert.assertEquals( CurveType.LineString, aggregate.get( 1 ).getCurveType() );
    }

    @Test
    public void parseEnvelope()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "Envelope.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Envelope" ), xmlReader.getName() );
        Envelope envelope = new GML3GeometryReader( GMLVersion.GML_31, null, null ).parseEnvelope( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Envelope" ), xmlReader.getName() );
        Assert.assertEquals( 11.0, envelope.getMin().get0(), DELTA );
        Assert.assertEquals( 22.0, envelope.getMin().get1(), DELTA );
        Assert.assertEquals( 44.0, envelope.getMax().get0(), DELTA );
        Assert.assertEquals( 88.0, envelope.getMax().get1(), DELTA );
        Assert.assertEquals( CRSRegistry.lookup( "EPSG:4326" ), envelope.getCoordinateSystem().getWrappedCRS() );
    }

    @Test
    public void parseEnvelopeCoord()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "Envelope_coord.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Envelope" ), xmlReader.getName() );
        Envelope envelope = new GML3GeometryReader( GMLVersion.GML_31, null, null ).parseEnvelope( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Envelope" ), xmlReader.getName() );
        Assert.assertEquals( 11.0, envelope.getMin().get0(), DELTA );
        Assert.assertEquals( 22.0, envelope.getMin().get1(), DELTA );
        Assert.assertEquals( 44.0, envelope.getMax().get0(), DELTA );
        Assert.assertEquals( 88.0, envelope.getMax().get1(), DELTA );
        Assert.assertEquals( CRSRegistry.lookup( "EPSG:4326" ), envelope.getCoordinateSystem().getWrappedCRS() );
    }

    @Test
    public void parseEnvelopePos()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "Envelope_pos.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Envelope" ), xmlReader.getName() );
        Envelope envelope = new GML3GeometryReader( GMLVersion.GML_31, null, null ).parseEnvelope( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Envelope" ), xmlReader.getName() );
        Assert.assertEquals( 11.0, envelope.getMin().get0(), DELTA );
        Assert.assertEquals( 22.0, envelope.getMin().get1(), DELTA );
        Assert.assertEquals( 44.0, envelope.getMax().get0(), DELTA );
        Assert.assertEquals( 88.0, envelope.getMax().get1(), DELTA );
        Assert.assertEquals( CRSRegistry.lookup( "EPSG:4326" ), envelope.getCoordinateSystem().getWrappedCRS() );
    }

    @Test
    public void parseEnvelopeCoordinates()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "Envelope_coordinates.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Envelope" ), xmlReader.getName() );
        Envelope envelope = new GML3GeometryReader( GMLVersion.GML_31, null, null ).parseEnvelope( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Envelope" ), xmlReader.getName() );
        Assert.assertEquals( 11.0, envelope.getMin().get0(), DELTA );
        Assert.assertEquals( 22.0, envelope.getMin().get1(), DELTA );
        Assert.assertEquals( 44.0, envelope.getMax().get0(), DELTA );
        Assert.assertEquals( 88.0, envelope.getMax().get1(), DELTA );
        Assert.assertEquals( CRSRegistry.lookup( "EPSG:4326" ), envelope.getCoordinateSystem().getWrappedCRS() );
    }

    @Test
    public void parseXLinkLineString()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {

        XMLStreamReaderWrapper xmlReader = getParser( "XLinkLineString.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        GML3GeometryReader decoder = new GML3GeometryReader( GMLVersion.GML_31, null, null );
        LineString geom = decoder.parseLineString( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( CRSRegistry.lookup( "EPSG:4326" ), geom.getCoordinateSystem().getWrappedCRS() );
        decoder.getDocumentIdContext();

        for ( Point p : geom.getControlPoints() ) {
            System.out.println( p.getId() + ", " + p.getClass() );
            System.out.println( p.get0() );
        }
    }

    @Test
    public void parseXLinkMultiGeometry1()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException, ReferenceResolvingException {
        XMLStreamReaderWrapper xmlReader = getParser( "XLinkMultiGeometry1.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiGeometry" ), xmlReader.getName() );
        GMLDocumentIdContext idContext = new GMLDocumentIdContext(GMLVersion.GML_31);
        MultiGeometry<Geometry> geom = new GML3GeometryReader( GMLVersion.GML_31, new GeometryFactory(), idContext ).parseMultiGeometry(
                                                                                                                         xmlReader,
                                                                                                                         null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "MultiGeometry" ), xmlReader.getName() );
        Assert.assertEquals( CRSRegistry.lookup( "EPSG:4326" ), geom.getCoordinateSystem().getWrappedCRS() );

        idContext.resolveLocalRefs();
        LineString ls = (LineString) geom.get( 2 );
        for ( Point p : ls.getControlPoints() ) {
            System.out.println( p.getId() + ", " + p.getClass() );
            System.out.println( p.get0() );
        }
    }

    @Test
    public void parseStandardProps()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = getParser( "StandardProps.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Point point = (Point) new GML3GeometryReader( GMLVersion.GML_31, null, null ).parse( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Assert.assertEquals( 7.12, point.get0(), DELTA );
        Assert.assertEquals( 50.72, point.get1(), DELTA );
        Assert.assertEquals( 2, point.getCoordinateDimension() );
        Assert.assertEquals( CRSRegistry.lookup( "EPSG:4326" ), point.getCoordinateSystem().getWrappedCRS() );
    }

    private XMLStreamReaderWrapper getParser( String fileName )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML3SurfacePatchReaderTest.class.getResource( BASE_DIR
                                                                                                                        + fileName ) );
        xmlReader.nextTag();
        return xmlReader;
    }
}
