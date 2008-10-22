//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.gml;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Ring;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.CurveSegment.Interpolation;
import org.deegree.model.geometry.primitive.curvesegments.Arc;
import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;
import org.junit.Assert;
import org.junit.Test;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GML311GeometryParserTest {

    private static GeometryFactory geomFac = GeometryFactoryCreator.getInstance().getGeometryFactory("Standard");

    @Test
    public void parsePointPos()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryParserTest.class.getResource( "Point1_pos.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Point point = new GML311GeometryParser( geomFac, xmlReader ).parsePoint( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Assert.assertEquals( 7.12, point.getX() );
        Assert.assertEquals( 50.72, point.getY() );
        Assert.assertEquals( 2, point.getCoordinateDimension() );
        Assert.assertEquals( "EPSG:4326", point.getCoordinateSystem().getIdentifier() );
    }

    @Test
    public void parsePointCoordinates()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryParserTest.class.getResource( "Point1_coordinates.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Point point = new GML311GeometryParser( geomFac, xmlReader ).parsePoint( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Assert.assertEquals( 7.12, point.getX() );
        Assert.assertEquals( 50.72, point.getY() );
        Assert.assertEquals( 2, point.getCoordinateDimension() );
        Assert.assertEquals( "EPSG:4326", point.getCoordinateSystem().getIdentifier() );
    }

    @Test
    public void parsePointCoord()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryParserTest.class.getResource( "Point1_coord.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Point point = new GML311GeometryParser( geomFac, xmlReader ).parsePoint( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Assert.assertEquals( 7.12, point.getX() );
        Assert.assertEquals( 50.72, point.getY() );
        Assert.assertEquals( 2, point.getCoordinateDimension() );
        Assert.assertEquals( "EPSG:4326", point.getCoordinateSystem().getIdentifier() );
    }

    @Test
    public void parseLineStringPos()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryParserTest.class.getResource( "LineString1_pos.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = new GML311GeometryParser( geomFac, xmlReader ).parseLineString( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
    }

    @Test
    public void parseLineStringPosList()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryParserTest.class.getResource( "LineString1_posList.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = new GML311GeometryParser( geomFac, xmlReader ).parseLineString( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( 1, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( 3, curve.getAsLineString().getPoints().size() );
        Assert.assertEquals( 7.12, curve.getAsLineString().getPoints().get( 0 ).getX() );
        Assert.assertEquals( 50.72, curve.getAsLineString().getPoints().get( 0 ).getY() );
        Assert.assertEquals( 9.98, curve.getAsLineString().getPoints().get( 1 ).getX() );
        Assert.assertEquals( 53.55, curve.getAsLineString().getPoints().get( 1 ).getY() );
        Assert.assertEquals( 13.42, curve.getAsLineString().getPoints().get( 2 ).getX() );
        Assert.assertEquals( 52.52, curve.getAsLineString().getPoints().get( 2 ).getY() );
    }

    @Test
    public void parseLineStringCoordinates()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryParserTest.class.getResource( "LineString1_coordinates.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = new GML311GeometryParser( geomFac, xmlReader ).parseLineString( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( 1, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( 3, curve.getAsLineString().getPoints().size() );
        Assert.assertEquals( 7.12, curve.getAsLineString().getPoints().get( 0 ).getX() );
        Assert.assertEquals( 50.72, curve.getAsLineString().getPoints().get( 0 ).getY() );
        Assert.assertEquals( 9.98, curve.getAsLineString().getPoints().get( 1 ).getX() );
        Assert.assertEquals( 53.55, curve.getAsLineString().getPoints().get( 1 ).getY() );
        Assert.assertEquals( 13.42, curve.getAsLineString().getPoints().get( 2 ).getX() );
        Assert.assertEquals( 52.52, curve.getAsLineString().getPoints().get( 2 ).getY() );
    }

    @Test
    public void parseLineStringPointProperty()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryParserTest.class.getResource( "LineString1_pointProperty.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = new GML311GeometryParser( geomFac, xmlReader ).parseLineString( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( 1, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( 3, curve.getAsLineString().getPoints().size() );
        Assert.assertEquals( 7.12, curve.getAsLineString().getPoints().get( 0 ).getX() );
        Assert.assertEquals( 50.72, curve.getAsLineString().getPoints().get( 0 ).getY() );
        Assert.assertEquals( 9.98, curve.getAsLineString().getPoints().get( 1 ).getX() );
        Assert.assertEquals( 53.55, curve.getAsLineString().getPoints().get( 1 ).getY() );
        Assert.assertEquals( 13.42, curve.getAsLineString().getPoints().get( 2 ).getX() );
        Assert.assertEquals( 52.52, curve.getAsLineString().getPoints().get( 2 ).getY() );
    }

    @Test
    public void parseLineStringPointRep()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryParserTest.class.getResource( "LineString1_pointRep.gml" ) );

        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = new GML311GeometryParser( geomFac, xmlReader ).parseLineString( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( 1, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( 3, curve.getAsLineString().getPoints().size() );
        Assert.assertEquals( 7.12, curve.getAsLineString().getPoints().get( 0 ).getX() );
        Assert.assertEquals( 50.72, curve.getAsLineString().getPoints().get( 0 ).getY() );
        Assert.assertEquals( 9.98, curve.getAsLineString().getPoints().get( 1 ).getX() );
        Assert.assertEquals( 53.55, curve.getAsLineString().getPoints().get( 1 ).getY() );
        Assert.assertEquals( 13.42, curve.getAsLineString().getPoints().get( 2 ).getX() );
        Assert.assertEquals( 52.52, curve.getAsLineString().getPoints().get( 2 ).getY() );
    }

    @Test
    public void parseCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryParserTest.class.getResource( "Curve.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Curve" ), xmlReader.getName() );
        Curve curve = new GML311GeometryParser( geomFac, xmlReader ).parseCurve( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Curve" ), xmlReader.getName() );
        Assert.assertEquals( 2, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.circularArc3Points, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 1 ).getInterpolation() );
    }

    @Test
    public void parseOrientableCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryParserTest.class.getResource( "OrientableCurve.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "OrientableCurve" ), xmlReader.getName() );
        Curve curve = new GML311GeometryParser( geomFac, xmlReader ).parseOrientableCurve( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "OrientableCurve" ), xmlReader.getName() );
        Assert.assertEquals( 2, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.circularArc3Points, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 1 ).getInterpolation() );
    }    
    
    @Test
    public void parseLinearRing()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryParserTest.class.getResource( "LinearRing.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LinearRing" ), xmlReader.getName() );
        Ring ring = new GML311GeometryParser( geomFac, xmlReader ).parseLinearRing( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LinearRing" ), xmlReader.getName() );
        Assert.assertEquals( 1, ring.getMembers().size() );
        Assert.assertEquals( 1, ring.getMembers().get( 0 ).getCurveSegments().size() );
        Assert.assertTrue( ring.getMembers().get( 0 ).getCurveSegments().get( 0 ) instanceof LineStringSegment );
        Assert.assertEquals( 7, ring.getMembers().get( 0 ).getAsLineString().getPoints().size() );
    }

    @Test
    public void parseRing()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryParserTest.class.getResource( "Ring.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Ring" ), xmlReader.getName() );
        Ring ring = new GML311GeometryParser( geomFac, xmlReader ).parseRing( null );
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
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryParserTest.class.getResource( "Ring.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Polygon" ), xmlReader.getName() );
        Surface polygon = new GML311GeometryParser( geomFac, xmlReader ).parsePolygon( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Polygon" ), xmlReader.getName() );
    }
}
