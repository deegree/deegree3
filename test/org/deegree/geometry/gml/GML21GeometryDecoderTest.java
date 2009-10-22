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
package org.deegree.geometry.gml;

import java.io.IOException;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.CRSRegistry;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.junit.Assert;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GML21GeometryDecoderTest extends TestCase {

    private static final String BASE_DIR = "testdata/gml21/";

    private static final String GML21NS = "http://www.opengis.org/gml";

    private static final String BOX_FILE = "Box.gml";

    private static final String POINT_FILE = "Point.gml";

    private static final String POLYGON_FILE = "Polygon.gml";

    private static final String LINESTRING_FILE = "LineString.gml";

    private static final String MULTIGEOMETRY_FILE = "MultiGeometry.gml";

    private static final String MULTILINESTRING_FILE = "MultiLineString.gml";

    private static final String MULTIPOINT_FILE = "MultiPoint.gml";

    private static final String MULTIPOLYGON_FILE = "MultiPolygon.gml";

    private static double DELTA = 0.00000001;

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws UnknownCRSException
     */
    public void testBox()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       this.getClass().getResource( BASE_DIR + BOX_FILE ) );
        xmlReader.nextTag();

        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( GML21NS, "Box" ), xmlReader.getName() );

        Envelope envelope = new GML21GeometryDecoder().parseBox( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( GML21NS, "Box" ), xmlReader.getName() );
        Assert.assertEquals( 0.0, envelope.getMin().get0(), DELTA );
        Assert.assertEquals( 0.0, envelope.getMin().get1(), DELTA );
        Assert.assertEquals( 100.0, envelope.getMax().get0(), DELTA );
        Assert.assertEquals( 100.0, envelope.getMax().get1(), DELTA );
        Assert.assertEquals( CRSRegistry.lookup( "EPSG:4326" ), envelope.getCoordinateSystem().getWrappedCRS() );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws UnknownCRSException
     */
    public void testPoint()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       this.getClass().getResource(
                                                                                                    BASE_DIR
                                                                                                                            + POINT_FILE ) );
        xmlReader.nextTag();

        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( GML21NS, "Point" ), xmlReader.getName() );

        Point point = new GML21GeometryDecoder().parsePoint( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( GML21NS, "Point" ), xmlReader.getName() );
        Assert.assertEquals( 5.0, point.get0(), DELTA );
        Assert.assertEquals( 40.0, point.get1(), DELTA );
        Assert.assertEquals( CRSRegistry.lookup( "EPSG:4326" ), point.getCoordinateSystem().getWrappedCRS() );

    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws UnknownCRSException
     */
    public void testPolygon()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       this.getClass().getResource(
                                                                                                    BASE_DIR
                                                                                                                            + POLYGON_FILE ) );
        xmlReader.nextTag();

        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( GML21NS, "Polygon" ), xmlReader.getName() );

        Polygon polygon = new GML21GeometryDecoder().parsePolygon( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( GML21NS, "Polygon" ), xmlReader.getName() );

        Points points = polygon.getExteriorRing().getControlPoints();
        comparePoint( 0.0, 0.0, points.get( 0 ) );
        comparePoint( 100.0, 0.0, points.get( 1 ) );
        comparePoint( 100.0, 100.0, points.get( 2 ) );
        comparePoint( 0.0, 100.0, points.get( 3 ) );
        comparePoint( 0.0, 0.0, points.get( 4 ) );

        List<Points> innerPoints = polygon.getInteriorRingsCoordinates();
        Points points1 = innerPoints.get( 0 );
        comparePoint( 10.0, 10.0, points1.get( 0 ) );
        comparePoint( 10.0, 40.0, points1.get( 1 ) );
        comparePoint( 40.0, 40.0, points1.get( 2 ) );
        comparePoint( 40.0, 10.0, points1.get( 3 ) );
        comparePoint( 10.0, 10.0, points1.get( 4 ) );

        Points points2 = innerPoints.get( 1 );
        comparePoint( 60.0, 60.0, points2.get( 0 ) );
        comparePoint( 60.0, 90.0, points2.get( 1 ) );
        comparePoint( 90.0, 90.0, points2.get( 2 ) );
        comparePoint( 90.0, 60.0, points2.get( 3 ) );
        comparePoint( 60.0, 60.0, points2.get( 4 ) );

        Assert.assertEquals( CRSRegistry.lookup( "EPSG:4326" ), polygon.getCoordinateSystem().getWrappedCRS() );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws UnknownCRSException
     */
    public void testLineString()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       this.getClass().getResource(
                                                                                                    BASE_DIR
                                                                                                                            + LINESTRING_FILE ) );
        xmlReader.nextTag();

        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( GML21NS, "LineString" ), xmlReader.getName() );

        LineString lineString = new GML21GeometryDecoder().parseLineString( xmlReader, null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( GML21NS, "LineString" ), xmlReader.getName() );

        Points controlPoints = lineString.getControlPoints();
        comparePoint( 0.0, 0.0, controlPoints.get( 0 ) );
        comparePoint( 20.0, 35.0, controlPoints.get( 1 ) );
        comparePoint( 100.0, 100.0, controlPoints.get( 2 ) );

        Assert.assertEquals( CRSRegistry.lookup( "EPSG:4326" ), lineString.getCoordinateSystem().getWrappedCRS() );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    public void testMultiGeometry()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       this.getClass().getResource(
                                                                                                    BASE_DIR
                                                                                                                            + MULTIGEOMETRY_FILE ) );
        xmlReader.nextTag();

        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( GML21NS, "MultiGeometry" ), xmlReader.getName() );

        MultiGeometry<?> multiGeometry = new GML21GeometryDecoder().parseMultiGeometry( xmlReader, null );
        assertEquals( "c731", multiGeometry.getId() );

        Point firstMember = (Point) multiGeometry.get( 0 );
        assertEquals( "P6776", firstMember.getId() );
        comparePoint( 50.0, 50.0, firstMember );

        LineString secondMember = (LineString) multiGeometry.get( 1 );
        assertEquals( "L21216", secondMember.getId() );

        Points controlPoints = secondMember.getControlPoints();
        comparePoint( 0.0, 0.0, controlPoints.get( 0 ) );
        comparePoint( 0.0, 50.0, controlPoints.get( 1 ) );
        comparePoint( 100.0, 50.0, controlPoints.get( 2 ) );

        Polygon thirdMember = (Polygon) multiGeometry.get( 2 );
        assertEquals( "_877789", thirdMember.getId() );

        Points points = thirdMember.getExteriorRing().getControlPoints();
        comparePoint( 0.0, 0.0, points.get( 0 ) );
        comparePoint( 100.0, 0.0, points.get( 1 ) );
        comparePoint( 50.0, 100.0, points.get( 2 ) );
        comparePoint( 0.0, 0.0, points.get( 3 ) );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    public void testMultiLineString()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       this.getClass().getResource(
                                                                                                    BASE_DIR
                                                                                                                            + MULTILINESTRING_FILE ) );
        xmlReader.nextTag();

        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( GML21NS, "MultiLineString" ), xmlReader.getName() );

        MultiLineString multiLineString = new GML21GeometryDecoder().parseMultiLineString( xmlReader, null );
        LineString firstMember = multiLineString.get( 0 );

        Points controlPoints = firstMember.getControlPoints();
        comparePoint( 56.1, 0.45, controlPoints.get( 0 ) );
        comparePoint( 67.23, 0.98, controlPoints.get( 1 ) );

        LineString secondMember = multiLineString.get( 1 );

        controlPoints = secondMember.getControlPoints();
        comparePoint( 46.71, 9.25, controlPoints.get( 0 ) );
        comparePoint( 56.88, 10.44, controlPoints.get( 1 ) );

        LineString thirdMember = multiLineString.get( 2 );

        controlPoints = thirdMember.getControlPoints();
        comparePoint( 324.1, 219.7, controlPoints.get( 0 ) );
        comparePoint( 0.45, 4.56, controlPoints.get( 1 ) );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    public void testMultiPoint()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       this.getClass().getResource(
                                                                                                    BASE_DIR
                                                                                                                            + MULTIPOINT_FILE ) );
        xmlReader.nextTag();

        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( GML21NS, "MultiPoint" ), xmlReader.getName() );

        MultiPoint multiPoint = new GML21GeometryDecoder().parseMultiPoint( xmlReader, null );

        Point firstMember = multiPoint.get( 0 );
        comparePoint( 5.0, 40.0, firstMember );

        Point secondMember = multiPoint.get( 1 );
        comparePoint( 0.0, 0.0, secondMember );

    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws UnknownCRSException
     */
    public void testMultiPolygon()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       this.getClass().getResource(
                                                                                                    BASE_DIR
                                                                                                                            + MULTIPOLYGON_FILE ) );
        xmlReader.nextTag();

        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( GML21NS, "MultiPolygon" ), xmlReader.getName() );

        MultiPolygon multiPolygon = new GML21GeometryDecoder().parseMultiPolygon( xmlReader, null );

        Polygon firstMember = multiPolygon.get( 0 );
        Points points = firstMember.getExteriorRing().getControlPoints();
        comparePoint( 0.0, 0.0, points.get( 0 ) );
        comparePoint( 100.0, 0.0, points.get( 1 ) );
        comparePoint( 100.0, 100.0, points.get( 2 ) );
        comparePoint( 0.0, 100.0, points.get( 3 ) );
        comparePoint( 0.0, 0.0, points.get( 4 ) );

        List<Points> innerPoints = firstMember.getInteriorRingsCoordinates();
        Points points1 = innerPoints.get( 0 );
        comparePoint( 10.0, 10.0, points1.get( 0 ) );
        comparePoint( 10.0, 40.0, points1.get( 1 ) );
        comparePoint( 40.0, 40.0, points1.get( 2 ) );
        comparePoint( 40.0, 10.0, points1.get( 3 ) );
        comparePoint( 10.0, 10.0, points1.get( 4 ) );

        Points points2 = innerPoints.get( 1 );
        comparePoint( 60.0, 60.0, points2.get( 0 ) );
        comparePoint( 60.0, 90.0, points2.get( 1 ) );
        comparePoint( 90.0, 90.0, points2.get( 2 ) );
        comparePoint( 90.0, 60.0, points2.get( 3 ) );
        comparePoint( 60.0, 60.0, points2.get( 4 ) );

        Polygon secondMember = multiPolygon.get( 1 );
        points = secondMember.getExteriorRing().getControlPoints();
        comparePoint( 0.0, 0.0, points.get( 0 ) );
        comparePoint( 100.0, 0.0, points.get( 1 ) );
        comparePoint( 100.0, 100.0, points.get( 2 ) );
        comparePoint( 0.0, 100.0, points.get( 3 ) );
        comparePoint( 0.0, 0.0, points.get( 4 ) );

        Assert.assertEquals( CRSRegistry.lookup( "EPSG:4326" ), multiPolygon.getCoordinateSystem().getWrappedCRS() );

    }

    private void comparePoint( double x, double y, Point point ) {
        Assert.assertEquals( x, point.get0(), DELTA );
        Assert.assertEquals( y, point.get1(), DELTA );
    }
}
