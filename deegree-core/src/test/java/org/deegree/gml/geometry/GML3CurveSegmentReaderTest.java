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

import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.Assert;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.segments.Arc;
import org.deegree.geometry.primitive.segments.ArcByBulge;
import org.deegree.geometry.primitive.segments.ArcByCenterPoint;
import org.deegree.geometry.primitive.segments.ArcString;
import org.deegree.geometry.primitive.segments.ArcStringByBulge;
import org.deegree.geometry.primitive.segments.BSpline;
import org.deegree.geometry.primitive.segments.Bezier;
import org.deegree.geometry.primitive.segments.Circle;
import org.deegree.geometry.primitive.segments.Clothoid;
import org.deegree.geometry.primitive.segments.CubicSpline;
import org.deegree.geometry.primitive.segments.Geodesic;
import org.deegree.geometry.primitive.segments.GeodesicString;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.primitive.segments.OffsetCurve;
import org.deegree.gml.GMLDocumentIdContext;
import org.deegree.gml.GMLVersion;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that check the correct parsing of GML 3.1.1 curve segments, i.e. of elements that are substitutable for
 * <code>gml:_CurveSegment</code>.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GML3CurveSegmentReaderTest {

    private GeometryFactory geomFac;

    @Before
    public void setUp()
                            throws Exception {
        geomFac = new GeometryFactory();
    }

    @Test
    public void parseArc()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        Arc arc = (Arc) getParser().parseCurveSegment( getReader( "Arc.gml" ), new CRS( "EPSG:4326" ) );
        Assert.assertEquals( 1, arc.getNumArcs() );
        Assert.assertEquals( 3, arc.getControlPoints().size() );
        Assert.assertEquals( 2.0, arc.getPoint1().get0() );
        Assert.assertEquals( 0.0, arc.getPoint1().get1() );
        Assert.assertEquals( 0.0, arc.getPoint2().get0() );
        Assert.assertEquals( 2.0, arc.getPoint2().get1() );
        Assert.assertEquals( -2.0, arc.getPoint3().get0() );
        Assert.assertEquals( 0.0, arc.getPoint3().get1() );
    }

    @Test
    public void parseArcByBulge()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        ArcByBulge arc = (ArcByBulge) getParser().parseCurveSegment( getReader( "ArcByBulge.gml" ),
                                                                     new CRS( "EPSG:4326" ) );
        Assert.assertEquals( 1, arc.getNumArcs() );
        Assert.assertEquals( 2, arc.getControlPoints().size() );
        Assert.assertEquals( 2.0, arc.getPoint1().get0() );
        Assert.assertEquals( 0.0, arc.getPoint1().get1() );
        Assert.assertEquals( -2.0, arc.getPoint2().get0() );
        Assert.assertEquals( 0.0, arc.getPoint2().get1() );
        Assert.assertEquals( 1, arc.getBulges().length );
        Assert.assertEquals( 2.0, arc.getBulge() );
        Assert.assertEquals( 1, arc.getNormals().size() );
        // Assert.assertEquals( 1, arc.getNormal().getCoordinateDimension() ); TODO Since every point we're working with
        // has 2 or 3 coords, find a way around this gml:vectortype
        Assert.assertEquals( -1.0, arc.getNormal().get0() );
    }

    @Test
    public void parseArcByCenterPoint()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        ArcByCenterPoint arc = (ArcByCenterPoint) getParser().parseCurveSegment( getReader( "ArcByCenterPoint.gml" ),
                                                                                 new CRS( "EPSG:4326" ) );
        Assert.assertEquals( 2, arc.getMidPoint().getCoordinateDimension() );
        Assert.assertEquals( 47.0, arc.getMidPoint().get0() );
        Assert.assertEquals( 11.0, arc.getMidPoint().get1() );
        Assert.assertEquals( 1.0, arc.getRadius( null ).getValueAsDouble() );
        Assert.assertEquals( "whatever#metres", arc.getRadius( null ).getUomUri() );
        Assert.assertEquals( 180.0, arc.getStartAngle().getValueAsDouble() );
        Assert.assertEquals( "whatever#degrees", arc.getStartAngle().getUomUri() );
        Assert.assertEquals( 360.0, arc.getEndAngle().getValueAsDouble() );
        Assert.assertEquals( "whatever#degrees", arc.getEndAngle().getUomUri() );
    }

    @Test
    public void parseArcString()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        ArcString arc = (ArcString) getParser().parseCurveSegment( getReader( "ArcString.gml" ), null );
        Assert.assertEquals( 3, arc.getNumArcs() );
        Assert.assertEquals( 7, arc.getControlPoints().size() );
        Assert.assertEquals( 2.0, arc.getControlPoints().get( 0 ).get0() );
        Assert.assertEquals( 0.0, arc.getControlPoints().get( 0 ).get1() );
        Assert.assertEquals( 0.0, arc.getControlPoints().get( 1 ).get0() );
        Assert.assertEquals( 2.0, arc.getControlPoints().get( 1 ).get1() );
        Assert.assertEquals( -2.0, arc.getControlPoints().get( 2 ).get0() );
        Assert.assertEquals( 0.0, arc.getControlPoints().get( 2 ).get1() );
        Assert.assertEquals( -4.0, arc.getControlPoints().get( 3 ).get0() );
        Assert.assertEquals( -2.0, arc.getControlPoints().get( 3 ).get1() );
        Assert.assertEquals( -6.0, arc.getControlPoints().get( 4 ).get0() );
        Assert.assertEquals( 0.0, arc.getControlPoints().get( 4 ).get1() );
        Assert.assertEquals( -8.0, arc.getControlPoints().get( 5 ).get0() );
        Assert.assertEquals( 2.0, arc.getControlPoints().get( 5 ).get1() );
        Assert.assertEquals( -10.0, arc.getControlPoints().get( 6 ).get0() );
        Assert.assertEquals( 0.0, arc.getControlPoints().get( 6 ).get1() );
    }

    @Test
    public void parseArcStringByBulge()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        ArcStringByBulge arc = (ArcStringByBulge) getParser().parseCurveSegment( getReader( "ArcStringByBulge.gml" ),
                                                                                 new CRS( "EPSG:4326" ) );
        Assert.assertEquals( 3, arc.getNumArcs() );
        Assert.assertEquals( 4, arc.getControlPoints().size() );
        Assert.assertEquals( 2.0, arc.getControlPoints().get( 0 ).get0() );
        Assert.assertEquals( 0.0, arc.getControlPoints().get( 0 ).get1() );
        Assert.assertEquals( -2.0, arc.getControlPoints().get( 1 ).get0() );
        Assert.assertEquals( 0.0, arc.getControlPoints().get( 1 ).get1() );
        Assert.assertEquals( -4.0, arc.getControlPoints().get( 2 ).get0() );
        Assert.assertEquals( 0.0, arc.getControlPoints().get( 2 ).get1() );
        Assert.assertEquals( -6.0, arc.getControlPoints().get( 3 ).get0() );
        Assert.assertEquals( 0.0, arc.getControlPoints().get( 3 ).get1() );
    }

    @Test
    public void parseBezier()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        Bezier arc = (Bezier) getParser().parseCurveSegment( getReader( "Bezier.gml" ), new CRS( "EPSG:4326" ) );
        Assert.assertEquals( 4, arc.getControlPoints().size() );
        Assert.assertEquals( 2.0, arc.getControlPoints().get( 0 ).get0() );
        Assert.assertEquals( 1.0, arc.getControlPoints().get( 0 ).get1() );
        Assert.assertEquals( -2.0, arc.getControlPoints().get( 1 ).get0() );
        Assert.assertEquals( 2.0, arc.getControlPoints().get( 1 ).get1() );
        Assert.assertEquals( -4.0, arc.getControlPoints().get( 2 ).get0() );
        Assert.assertEquals( 3.0, arc.getControlPoints().get( 2 ).get1() );
        Assert.assertEquals( -6.0, arc.getControlPoints().get( 3 ).get0() );
        Assert.assertEquals( 4.0, arc.getControlPoints().get( 3 ).get1() );
        Assert.assertEquals( 4, arc.getPolynomialDegree() );
        Assert.assertEquals( 2, arc.getKnots().size() );
        Assert.assertEquals( 1.0, arc.getKnot1().getValue() );
        Assert.assertEquals( 4, arc.getKnot1().getMultiplicity() );
        Assert.assertEquals( 5.0, arc.getKnot1().getWeight() );
        Assert.assertEquals( 2.0, arc.getKnot2().getValue() );
        Assert.assertEquals( 5, arc.getKnot2().getMultiplicity() );
        Assert.assertEquals( 1.0, arc.getKnot2().getWeight() );
    }

    @Test
    public void parseBSpline()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        BSpline arc = (BSpline) getParser().parseCurveSegment( getReader( "BSpline.gml" ), new CRS( "EPSG:4326" ) );
        Assert.assertEquals( 4, arc.getControlPoints().size() );
        Assert.assertEquals( 2.0, arc.getControlPoints().get( 0 ).get0() );
        Assert.assertEquals( 1.0, arc.getControlPoints().get( 0 ).get1() );
        Assert.assertEquals( -2.0, arc.getControlPoints().get( 1 ).get0() );
        Assert.assertEquals( 2.0, arc.getControlPoints().get( 1 ).get1() );
        Assert.assertEquals( -4.0, arc.getControlPoints().get( 2 ).get0() );
        Assert.assertEquals( 3.0, arc.getControlPoints().get( 2 ).get1() );
        Assert.assertEquals( -6.0, arc.getControlPoints().get( 3 ).get0() );
        Assert.assertEquals( 4.0, arc.getControlPoints().get( 3 ).get1() );
        Assert.assertEquals( 4, arc.getPolynomialDegree() );
        Assert.assertEquals( 2, arc.getKnots().size() );
        Assert.assertEquals( 1.0, arc.getKnots().get( 0 ).getValue() );
        Assert.assertEquals( 4, arc.getKnots().get( 0 ).getMultiplicity() );
        Assert.assertEquals( 5.0, arc.getKnots().get( 0 ).getWeight() );
        Assert.assertEquals( 2.0, arc.getKnots().get( 1 ).getValue() );
        Assert.assertEquals( 5, arc.getKnots().get( 1 ).getMultiplicity() );
        Assert.assertEquals( 1.0, arc.getKnots().get( 1 ).getWeight() );
    }

    @Test
    public void parseCircle()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        Circle circle = (Circle) getParser().parseCurveSegment( getReader( "Circle.gml" ), null );
        Assert.assertEquals( 1, circle.getNumArcs() );
        Assert.assertEquals( 3, circle.getControlPoints().size() );
        Assert.assertEquals( 2.0, circle.getPoint1().get0() );
        Assert.assertEquals( 0.0, circle.getPoint1().get1() );
        Assert.assertEquals( 0.0, circle.getPoint2().get0() );
        Assert.assertEquals( 2.0, circle.getPoint2().get1() );
        Assert.assertEquals( -2.0, circle.getPoint3().get0() );
        Assert.assertEquals( 0.0, circle.getPoint3().get1() );
    }

    @Test
    public void parseCircleByCenterPoint()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        ArcByCenterPoint arc = (ArcByCenterPoint) getParser().parseCurveSegment(
                                                                                 getReader( "CircleByCenterPoint.gml" ),
                                                                                 new CRS( "EPSG:4326" ) );
        Assert.assertEquals( 2, arc.getMidPoint().getCoordinateDimension() );
        Assert.assertEquals( 47.0, arc.getMidPoint().get0() );
        Assert.assertEquals( 11.0, arc.getMidPoint().get1() );
        Assert.assertEquals( 1.0, arc.getRadius( null ).getValueAsDouble() );
        Assert.assertEquals( "whatever#metres", arc.getRadius( null ).getUomUri() );
        Assert.assertEquals( 0.0, arc.getStartAngle().getValueAsDouble() );
        Assert.assertEquals( "whatever#degrees", arc.getStartAngle().getUomUri() );
        Assert.assertEquals( 0.0, arc.getEndAngle().getValueAsDouble() );
        Assert.assertEquals( "whatever#degrees", arc.getEndAngle().getUomUri() );
    }

    @Test
    public void parseClothoid()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        Clothoid segment = (Clothoid) getParser().parseCurveSegment( getReader( "Clothoid.gml" ), new CRS( "EPSG:4326" ) );
        Assert.assertEquals( 47.0, segment.getReferenceLocation().getLocation().get0() );
        Assert.assertEquals( 11.0, segment.getReferenceLocation().getLocation().get1() );
        Assert.assertEquals( 13.0, segment.getReferenceLocation().getLocation().get2() );
        Assert.assertEquals( 2, segment.getReferenceLocation().getRefDirections().size() );
        Assert.assertEquals( 3.0, segment.getReferenceLocation().getRefDirections().get( 0 ).get0() );
        Assert.assertEquals( 4.0, segment.getReferenceLocation().getRefDirections().get( 0 ).get1() );
        Assert.assertEquals( 8.0, segment.getReferenceLocation().getRefDirections().get( 0 ).get2() );
        Assert.assertEquals( 5.0, segment.getReferenceLocation().getRefDirections().get( 1 ).get0() );
        Assert.assertEquals( 6.0, segment.getReferenceLocation().getRefDirections().get( 1 ).get1() );
        Assert.assertEquals( 9.0, segment.getReferenceLocation().getRefDirections().get( 1 ).get2() );
        Assert.assertEquals( 2, segment.getReferenceLocation().getInDimension() );
        Assert.assertEquals( 3, segment.getReferenceLocation().getOutDimension() );
        Assert.assertEquals( 0.9, segment.getScaleFactor() );
        Assert.assertEquals( -2.5, segment.getStartParameter() );
        Assert.assertEquals( 3.0, segment.getEndParameter() );
    }

    @Test
    public void parseCubicSpline()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        CubicSpline segment = (CubicSpline) getParser().parseCurveSegment( getReader( "CubicSpline.gml" ),
                                                                           new CRS( "EPSG:4326" ) );
        Assert.assertEquals( 3, segment.getControlPoints().size() );
        Assert.assertEquals( -2.0, segment.getControlPoints().get( 0 ).get0() );
        Assert.assertEquals( 0.0, segment.getControlPoints().get( 0 ).get1() );
        Assert.assertEquals( -4.0, segment.getControlPoints().get( 1 ).get0() );
        Assert.assertEquals( 0.0, segment.getControlPoints().get( 1 ).get1() );
        Assert.assertEquals( -6.0, segment.getControlPoints().get( 2 ).get0() );
        Assert.assertEquals( 1.0, segment.getControlPoints().get( 2 ).get1() );
        Assert.assertEquals( 0.0, segment.getVectorAtStart().get0() );
        Assert.assertEquals( -1.0, segment.getVectorAtStart().get1() );
        Assert.assertEquals( -1.0, segment.getVectorAtEnd().get0() );
        Assert.assertEquals( 1.0, segment.getVectorAtEnd().get1() );
    }

    @Test
    public void parseGeodesic()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        Geodesic segment = (Geodesic) getParser().parseCurveSegment( getReader( "Geodesic.gml" ), null );
        Assert.assertEquals( 2, segment.getControlPoints().size() );
        Assert.assertEquals( 2.0, segment.getControlPoints().get( 0 ).get0() );
        Assert.assertEquals( 0.0, segment.getControlPoints().get( 0 ).get1() );
        Assert.assertEquals( 0.0, segment.getControlPoints().get( 1 ).get0() );
        Assert.assertEquals( 2.0, segment.getControlPoints().get( 1 ).get1() );
    }

    @Test
    public void parseGeodesicString()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        GeodesicString segment = (GeodesicString) getParser().parseCurveSegment( getReader( "GeodesicString.gml" ),
                                                                                 null );
        Assert.assertEquals( 3, segment.getControlPoints().size() );
        Assert.assertEquals( 2.0, segment.getControlPoints().get( 0 ).get0() );
        Assert.assertEquals( 0.0, segment.getControlPoints().get( 0 ).get1() );
        Assert.assertEquals( 0.0, segment.getControlPoints().get( 1 ).get0() );
        Assert.assertEquals( 2.0, segment.getControlPoints().get( 1 ).get1() );
        Assert.assertEquals( -2.0, segment.getControlPoints().get( 2 ).get0() );
        Assert.assertEquals( 0.0, segment.getControlPoints().get( 2 ).get1() );
    }

    @Test
    public void parseLineStringSegment()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        LineStringSegment arc = (LineStringSegment) getParser().parseCurveSegment(
                                                                                   getReader( "LineStringSegment.gml" ),
                                                                                   null );
        Assert.assertEquals( 3, arc.getControlPoints().size() );
        Assert.assertEquals( 2.0, arc.getControlPoints().get( 0 ).get0() );
        Assert.assertEquals( 0.0, arc.getControlPoints().get( 0 ).get1() );
        Assert.assertEquals( 0.0, arc.getControlPoints().get( 1 ).get0() );
        Assert.assertEquals( 2.0, arc.getControlPoints().get( 1 ).get1() );
        Assert.assertEquals( -2.0, arc.getControlPoints().get( 2 ).get0() );
        Assert.assertEquals( 0.0, arc.getControlPoints().get( 2 ).get1() );
    }

    @Test
    public void parseOffsetCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        OffsetCurve segment = (OffsetCurve) getParser().parseCurveSegment( getReader( "OffsetCurve.gml" ),
                                                                           new CRS( "EPSG:4326" ) );
        Assert.assertEquals( 1.0, segment.getDistance( null ).getValueAsDouble() );
        Assert.assertEquals( 0.0, segment.getDirection().get0() );
        Assert.assertEquals( 1.0, segment.getDirection().get1() );
    }

    private XMLStreamReaderWrapper getReader( String fileName )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML3CurveSegmentReaderTest.class.getResource( "../../geometry/gml/testdata/segments/"
                                                                                                                      + fileName ) );
        xmlReader.nextTag();
        return xmlReader;
    }

    private GML3CurveSegmentReader getParser()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        GMLDocumentIdContext idContext = new GMLDocumentIdContext( GMLVersion.GML_31 );
        return new GML3CurveSegmentReader( new GML3GeometryReader( GMLVersion.GML_31, null, null, 2 ), geomFac, 2 );
    }
}
