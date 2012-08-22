//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/model/spatialschema/GMLTest.java $
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
package org.deegree.model.spatialschema;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.crs.CoordinateSystem;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import alltests.Configuration;

/**
 * Test case for the {@link GMLGeometryAdapter} class.
 * <p>
 * TODO
 * <ul>
 * <li>remove obsessive output (JUnit tests should not print to System.out)</li>
 * <li>add check for exported geometries (schema validation or XMLUnit)</li>
 * </ul>
 * 
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author last edited by: $Author: rbezema $
 * 
 * @version $Revision: 10877 $, $Date: 2008-04-01 15:05:11 +0000 (Di, 01 Apr 2008) $
 */
public strictfp class GMLGeometryAdapterTest extends TestCase {

    private static ILogger LOG = LoggerFactory.getLogger( GMLGeometryAdapterTest.class );

    public static Test suite() {
        return new TestSuite( GMLGeometryAdapterTest.class );
    }

    /**
     * Constructor for WFSServiceTest.
     * 
     * @param arg0
     */

    public GMLGeometryAdapterTest( String arg0 ) {

        super( arg0 );

    }

    /*
     * @see TestCase#setUp()
     */

    protected void setUp()
                            throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */

    protected void tearDown()
                            throws Exception {
        super.tearDown();
    }

    /*
     * Tests wether the conversion <gml:Point> into a Point instance is correctly made
     */
    public void testWrapPoint() {

        LOG.logInfo( " --- \t Point \t --- " );
        // point to test: pt=(2,-1)
        // double[][] point = { { 2, -1 } };
        String gmlCoordinates = "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">"
                                + "<gml:coordinates>2,-1</gml:coordinates></gml:Point>";

        String gmlPos = "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">"
                        + "<gml:pos dimension=\"2\">2 -1</gml:pos></gml:Point>";

        String gmlCoord = "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">"
                          + "<gml:coord><gml:X>2</gml:X><gml:Y>-1</gml:Y>" + "</gml:coord></gml:Point>";

        int i = 0;
        while ( i <= 2 ) {

            String identifier = "";
            String gml = "";

            if ( i == 0 ) {
                // Test the <gml:coordinates> Element
                identifier = "<gml:Point><gml:coordinates>";
                gml = gmlCoordinates;
            }

            if ( i == 1 ) {
                // Test the <gml:pos> Element
                identifier = "<gml:Point><gml:pos>";
                gml = gmlPos;
            }

            if ( i == 2 ) {
                // Test the <gml:coord> Element
                identifier = "<gml:Point><gml:coord>";
                gml = gmlCoord;
            }

            try {

                Geometry geom = wrapGmlString( identifier, gml );

                // wrap methode shoud have returned a Point object
                if ( geom instanceof Point ) {
                    Point pt = (Point) geom;
                    LOG.logInfo( "\t************" );
                    LOG.logInfo( "\t\t X=" + pt.getX() + "\t Y=" + pt.getY() );
                    LOG.logInfo( "\tExpected X=" + 2 + "\t Y=" + -1 );
                    LOG.logInfo( "\t************" );

                    assertTrue( "X coordinates expected to be equal", 2 == pt.getX() );
                    assertTrue( "Y coordinates expected to be equal", -1 == pt.getY() );
                } else {
                    String s = identifier + "\nExpected Point object" + "\nGot " + geom.getClass() + " object";
                    throw new Exception( s );
                }
            } catch ( Exception e ) {
                LOG.logError( "\t Unit test failed \n" + e.getMessage(), e );
                fail( e.getMessage() );
            }
            i++;
            LOG.logInfo( "\n" );
        }
        LOG.logInfo( " ----------------------------------" );
    }

    /*
     * Tests wether the conversion <gml:MultiPoint> into a MultiPoint instance is correctly made
     */
    public void testWrapMultiPoint() {

        LOG.logInfo( " --- \t MultiPoint \t --- " );

        // MultiPoints to test (1,1) (2,2) (3,3) (4,4)
        double[][] multipoint = { { 1, 1 }, { 2, 2 }, { 3, 3 }, { 4, 4 } };
        String pos = "<gml:MultiPoint xmlns:gml=\"http://www.opengis.net/gml\">"
                     + "<gml:pointMember><gml:Point><gml:pos>1 1</gml:pos>" + "</gml:Point></gml:pointMember>"
                     + "<gml:pointMember><gml:Point><gml:pos>2 2</gml:pos>" + "</gml:Point></gml:pointMember>"
                     + "<gml:pointMember><gml:Point><gml:pos>3 3</gml:pos>" + "</gml:Point></gml:pointMember>"
                     + "<gml:pointMember><gml:Point><gml:pos>4 4</gml:pos>"
                     + "</gml:Point></gml:pointMember></gml:MultiPoint>";

        String coordinates = "<gml:MultiPoint xmlns:gml=\"http://www.opengis.net/gml\">"
                             + "<gml:pointMember><gml:Point>" + "<gml:coordinates>1,1</gml:coordinates>"
                             + "</gml:Point></gml:pointMember>" + "<gml:pointMember><gml:Point>"
                             + "<gml:coordinates>2,2</gml:coordinates>" + "</gml:Point></gml:pointMember>"
                             + "<gml:pointMember><gml:Point>" + "<gml:coordinates>3,3</gml:coordinates>"
                             + "</gml:Point></gml:pointMember>" + "<gml:pointMember><gml:Point>"
                             + "<gml:coordinates>4,4</gml:coordinates>"
                             + "</gml:Point></gml:pointMember></gml:MultiPoint>";

        int i = 0;
        while ( i < 2 ) {

            String identifier = "";
            String gml = "";

            if ( i == 0 ) {
                // Test the <gml:coordinates> Element
                identifier = "<gml:MultiPoint><gml:coordinates>";
                gml = coordinates;
            }
            if ( i == 1 ) {
                // Test the <gml:pos> Element
                identifier = "<gml:MultiPoint><gml:pos>";
                gml = pos;
            }

            try {

                Geometry geom = wrapGmlString( identifier, gml );

                // wrap methode should have returned MultiPoint object
                if ( geom instanceof MultiPoint ) {
                    MultiPoint mpt = (MultiPoint) geom;
                    multiPointTesting( mpt, multipoint );
                } else {
                    String s = identifier + "\nExpected MultiPoint object" + "\nGot " + geom.getClass() + " object";
                    throw new Exception( s );
                }
            } catch ( Exception e ) {
                LOG.logError( "Unit failed \n" + e.getMessage(), e );
                fail( e.getMessage() );
            }
            i++;
        }
        LOG.logInfo( " ----------------------------------" );
    }

    /*
     * Tests wether the conversion <gml:LineString> into a Curve instance is correctly made
     */
    public void testWrapLineString() {

        LOG.logInfo( " --- \t LineString \t --- " );
        // points to test (10,20) (22, 33) (-1, -1)
        double[][] lineStringPoints = { { 10, 20 }, { 22, 33 }, { -1, -1 } };

        String pos = "<gml:LineString xmlns:gml=\"http://www.opengis.net/gml\">"
                     + "<gml:pos>10 20</gml:pos><gml:pos>22 33</gml:pos>" + "<gml:pos>-1 -1</gml:pos></gml:LineString>";

        String coordinates = "<gml:LineString xmlns:gml=\"http://www.opengis.net/gml\""
                             + " srsName=\"http://www.opengis.net/gml/srs/epsg.xml#4326\">"
                             + "<gml:coordinates>10,20 22,33 -1,-1</gml:coordinates>" + "</gml:LineString>";

        int i = 0;
        while ( i < 2 ) {

            String identifier = "";
            String gml = "";

            if ( i == 0 ) {
                // test <gml:coordinates> element
                identifier = "<gml:LineString><gml:coordinates>";
                gml = coordinates;
            }

            if ( i == 1 ) {
                // test <gml:pos> element
                identifier = "<gml:LineString><gml:pos>";
                gml = pos;
            }

            try {

                Geometry geom = wrapGmlString( identifier, gml );

                if ( geom instanceof Curve ) {
                    // wrap methode should have returned Curve object
                    Curve cv = (Curve) geom;
                    curveTesting( cv, lineStringPoints );
                } else {
                    String s = identifier + "\nExpected Curve object" + "\nGot " + geom.getClass() + " object";
                    throw new Exception( s );
                }
            } catch ( Exception e ) {
                LOG.logError( "\t Unit failed \n" + e.getMessage(), e );
                fail( e.getMessage() );
            }
            i++;
        }
        LOG.logInfo( " ----------------------------------" );
    }

    /*
     * Tests wether the conversion <gml:MultiLineString> into a MultiCurve instance is correctly made
     */
    public strictfp void testWrapMultiLineString() {

        LOG.logInfo( " --- \t MultiLineString \t --- " );
        double[][][] points = {
                               { { 4586.790988700565f, 2604.7224576271187f },
                                { 393.268333333333f, 2315.6079378531076f }, { 5301.969011299436f, 1509.130593220339f },
                                { 4647.657203389831f, 2011.2768644067799f },
                                { 5591.0835310734465f, 2011.2768644067799f } },
                               { { 4525.9247740113f, 3426.4163559322037f }, { 5317.185564971752f, 3167.734943502825f },
                                { 4632.440649717514f, 2924.2700847457627f },
                                { 5332.402118644068f, 2756.8879943502825f } } };

        String coords = "<gml:MultiLineString " + "xmlns:gml=\"http://www.opengis.net/gml\">"
                        + "<gml:lineStringMember><gml:LineString>" + "<gml:coordinates>"
                        + "4586.790988700565,2604.7224576271187 " + "393.268333333333,2315.6079378531076 "
                        + "5301.969011299436,1509.130593220339 " + "4647.657203389831,2011.2768644067799 "
                        + "5591.0835310734465,2011.2768644067799" + "</gml:coordinates>"
                        + "</gml:LineString></gml:lineStringMember>" + "<gml:lineStringMember><gml:LineString>"
                        + "<gml:coordinates>" + "4525.9247740113,3426.4163559322037 "
                        + "5317.185564971752,3167.734943502825 " + "4632.440649717514,2924.2700847457627 "
                        + "5332.402118644068,2756.8879943502825" + "</gml:coordinates>"
                        + "</gml:LineString></gml:lineStringMember>" + "</gml:MultiLineString>";

        // 2 LineString Members
        double[][][] multiLineStringPoints = {
                                              { { 1.1f, 2.2f }, { 3.3f, 4.4f }, { 5.5f, 6.6f }, { 7.7f, 8.8f },
                                               { 9.9f, 10.10f }, { 11.11f, 12.12f } },
                                              { { 13.13f, 14.14f }, { 15.15f, 16.16f }, { 17.17f, 18.18f } } };

        String coordinates = "<gml:MultiLineString" + " xmlns:gml=\"http://www.opengis.net/gml\">"
                             + "<gml:lineStringMember><gml:LineString>" + "<gml:coordinates>"
                             + "1.1,2.2 3.3,4.4 5.5,6.6 7.7,8.8 9.9,10.10 11.11,12.12" + "</gml:coordinates>"
                             + "</gml:LineString></gml:lineStringMember>"
                             + "<gml:lineStringMember><gml:LineString><gml:coordinates>"
                             + "13.13,14.14 15.15,16.16 17.17,18.18" + "</gml:coordinates>"
                             + "</gml:LineString></gml:lineStringMember>" + "</gml:MultiLineString>";

        String pos = "<gml:MultiLineString " + "xmlns:gml=\"http://www.opengis.net/gml\">"
                     + "<gml:lineStringMember><gml:LineString>" + "<gml:pos>1.1 2.2 </gml:pos>"
                     + "<gml:pos>3.3 4.4 </gml:pos><gml:pos>5.5 6.6</gml:pos>"
                     + "<gml:pos>7.7 8.8 </gml:pos><gml:pos>9.9 10.10</gml:pos>"
                     + "<gml:pos>11.11 12.12</gml:pos></gml:LineString>" + "</gml:lineStringMember>"
                     + "<gml:lineStringMember><gml:LineString>"
                     + "<gml:pos>13.13 14.14</gml:pos><gml:pos>15.15 16.16</gml:pos>"
                     + "<gml:pos>17.17 18.18</gml:pos></gml:LineString>"
                     + "</gml:lineStringMember></gml:MultiLineString>";

        int i = 0;
        while ( i < 3 ) {

            String identifier = "";
            String gml = "";

            if ( i == 0 ) {
                // test <gml:coordinates> elemnt
                identifier = "<gml:MultiLineString><gml:coordinates>";
                gml = coordinates;
            }
            if ( i == 1 ) {
                // test <gml:pos> elememt
                identifier = "<gml:MultiLineString><gml:pos>";
                gml = pos;
            }
            if ( i == 2 ) {
                identifier = "<gml:MultiLineString><gml:coordinates>";
                gml = coords;
                multiLineStringPoints = points;
            }
            try {

                Geometry geom = wrapGmlString( identifier, gml );

                if ( geom instanceof MultiCurve ) {
                    // wrap methode should have returned MultiCurve object

                    MultiCurve mcv = (MultiCurve) geom;
                    multiCurveTesting( mcv, multiLineStringPoints );
                } else {
                    String s = identifier + "\nExpected MultiCurve object" + "\nGot " + geom.getClass() + " object";
                    throw new Exception( s );
                }
            } catch ( Exception e ) {
                LOG.logError( "\t Unit failed \n" + e.getMessage(), e );
                fail( e.getMessage() );
            }
            i++;
        }
        LOG.logInfo( " ----------------------------------" );
    }

    /*
     * Tests wether the conversion <gml:Polygon> into a Surface instance is correctly made
     */
    public void testWrapPolygon() {

        LOG.logInfo( " --- \t Polygon \t --- " );
        // Polygon1: One Exterior, no interior
        double[][] exteriorPoints1 = { { 100, 100 }, { 99, -99 }, { -88, -88 }, { -77, 77 }, { 100, 100 } };
        double[][][] interiorPoints1 = null;

        String outerboundaryis = "<gml:Polygon " + "xmlns:gml=\"http://www.opengis.net/gml\">"
                                 + "<gml:outerBoundaryIs><gml:LinearRing>" + "<gml:coordinates>"
                                 + "100,100 99,-99 -88,-88 -77,77 100,100" + "</gml:coordinates>"
                                 + "</gml:LinearRing></gml:outerBoundaryIs>" + "</gml:Polygon>";

        // Polygon 2: One Exterior and one interior
        double[][] exteriorPoints2 = exteriorPoints1;
        double[][][] interiorsPoints2 = { { { 1, 1 }, { 9, -9 }, { -8, -8 }, { -7, 7 }, { 1, 1 } } };

        String outerInner = "<gml:Polygon " + "xmlns:gml=\"http://www.opengis.net/gml\">"
                            + "<gml:outerBoundaryIs><gml:LinearRing>" + "<gml:coordinates>"
                            + "100,100 99,-99 -88,-88 -77,77 100,100" + "</gml:coordinates>"
                            + "</gml:LinearRing></gml:outerBoundaryIs>" + "<gml:innerBoundaryIs><gml:LinearRing>"
                            + "<gml:coordinates>" + "1,1 9,-9 -8,-8 -7,7 1,1" + "</gml:coordinates>"
                            + "</gml:LinearRing></gml:innerBoundaryIs>" + "</gml:Polygon>";

        int i = 0;
        while ( i < 2 ) {

            String identifier = "";
            String gml = "";

            double[][] exterior = null;
            double[][][] interiors = null;

            if ( i == 0 ) {
                // set Values for Polygon1
                // test <gml:outerboundaryis> element
                identifier = "<gml:Polygon><gml:outerboundaryis>";
                gml = outerboundaryis;
                exterior = exteriorPoints1;
                interiors = interiorPoints1;
            }

            if ( i == 1 ) {
                // set Values for Polygon2
                // test <gml:innerBoundaryIs>
                identifier = "<gml:Polygon><gml:innerBoundaryIs>";
                gml = outerInner;
                exterior = exteriorPoints2;
                interiors = interiorsPoints2;
            }

            try {

                Geometry geom = wrapGmlString( identifier, gml );

                if ( geom instanceof Surface ) {
                    // wrap methode shoud have returned a Surface
                    surfaceTesting( (Surface) geom, exterior, interiors );
                } else {
                    String s = identifier + "\nExpected Surface object" + "\nGot " + geom.getClass() + " object";
                    throw new Exception( s );
                }
            } catch ( Exception e ) {
                LOG.logError( "\t Unit failed \n" + e.getMessage(), e );
                fail( e.getMessage() );
            }
            i++;
        }
        LOG.logInfo( " ----------------------------------" );
    }

    /*
     * Tests wether the conversion <gml:MultiPolygon> into a MultiSurface instance is correctly made
     */
    public void testWrapMultiPolygon() {

        LOG.logInfo( " --- \t MultiPolygon \t --- " );
        // different Polygons are determined by exteriors
        double[][][] polygons = {// exterior1
        { { 111, 111 }, { 111, 1000 }, { 999, 1000 }, { 999, 111 }, { 111, 111 } },
                // exterior 2
                { { 100, 100 }, { 99, -99 }, { -88, -88 }, { -77, 77 }, { 100, 100 } } };
        /*
         * The Interiors of the Polygons defined above Polygons[0] has no Interior -> null Polygon[1] has 1 Interior
         */
        double[][][][] interiorsPoints = { null, { { { 1, 1 }, { 9, -9 }, { -8, -8 }, { -7, 7 }, { 1, 1 } } } };

        String multipolygon = "<gml:MultiPolygon xmlns:gml=\"http://www.opengis.net/gml\">"
                              + "<gml:polygonMember><gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>"
                              + "<gml:coordinates>111,111 111,1000 999,1000 999,111 111,111</gml:coordinates>"
                              + "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon></gml:polygonMember>"
                              + "<gml:polygonMember><gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>"
                              + "<gml:coordinates>100,100 99,-99 -88,-88 -77,77 100,100</gml:coordinates>"
                              + "</gml:LinearRing></gml:outerBoundaryIs><gml:innerBoundaryIs><gml:LinearRing>"
                              + "<gml:coordinates>1,1 9,-9 -8,-8 -7,7 1,1</gml:coordinates></gml:LinearRing>"
                              + "</gml:innerBoundaryIs></gml:Polygon></gml:polygonMember></gml:MultiPolygon>";

        String identifier = "<gml:MultiPolygon>";

        try {
            Geometry geom = wrapGmlString( identifier, multipolygon );

            if ( geom instanceof MultiSurface ) {
                // wrap methode shoud have returned a MultiSurface
                multiSurfaceTesting( (MultiSurface) geom, polygons, interiorsPoints );
            } else {
                String s = identifier + "\nExpected MutltiSurface object" + "\nGot " + geom.getClass() + " object";
                throw new Exception( s );
            }
        } catch ( Exception e ) {
            LOG.logError( " \t Unit failed \n" + e.getMessage(), e );
            fail( e.getMessage() );
        }
        LOG.logInfo( " ----------------------------------" );
    }

    /**
     * Tests whether the conversion of a <gml:MultiGeometry> instance into a {@link MultiGeometry} instance produces
     * member geometries of the expected type.
     * 
     * @throws SAXException
     * @throws IOException
     * @throws GeometryException
     */
    public MultiGeometry testWrapMultiGeometry()
                            throws IOException, SAXException, GeometryException {

        URL inputURL = GMLGeometryAdapterTest.class.getResource( "MultiGeometryNestedExample.xml" );
        XMLFragment xml = new XMLFragment();
        xml.load( inputURL );

        Geometry geometry = GMLGeometryAdapter.wrap( xml.getRootElement(), null );
        assertNotNull( geometry );
        assertTrue( geometry instanceof MultiGeometry );

        // check type of member geometries
        MultiGeometry multiGeometry = (MultiGeometry) geometry;
        assertEquals( 9, multiGeometry.getSize() );
        assertTrue( multiGeometry.getObjectAt( 0 ) instanceof Point );
        assertTrue( multiGeometry.getObjectAt( 1 ) instanceof MultiPoint );
        assertTrue( multiGeometry.getObjectAt( 2 ) instanceof Curve );
        assertTrue( multiGeometry.getObjectAt( 3 ) instanceof MultiCurve );
        assertTrue( multiGeometry.getObjectAt( 4 ) instanceof Surface );
        assertTrue( multiGeometry.getObjectAt( 5 ) instanceof Surface );
        assertTrue( multiGeometry.getObjectAt( 6 ) instanceof MultiSurface );
        assertTrue( multiGeometry.getObjectAt( 7 ) instanceof MultiSurface );
        assertTrue( multiGeometry.getObjectAt( 8 ) instanceof MultiGeometry );

        // check type of member geometries in nested MultiGeometry
        multiGeometry = (MultiGeometry) multiGeometry.getObjectAt( 8 );
        assertEquals( 2, multiGeometry.getSize() );
        assertTrue( multiGeometry.getObjectAt( 0 ) instanceof Point );
        assertTrue( multiGeometry.getObjectAt( 1 ) instanceof MultiGeometry );

        // check type of member geometries in nested MultiGeometry
        multiGeometry = (MultiGeometry) multiGeometry.getObjectAt( 1 );
        assertEquals( 8, multiGeometry.getSize() );
        assertTrue( multiGeometry.getObjectAt( 0 ) instanceof Point );
        assertTrue( multiGeometry.getObjectAt( 1 ) instanceof MultiPoint );
        assertTrue( multiGeometry.getObjectAt( 2 ) instanceof Curve );
        assertTrue( multiGeometry.getObjectAt( 3 ) instanceof MultiCurve );
        assertTrue( multiGeometry.getObjectAt( 4 ) instanceof Surface );
        assertTrue( multiGeometry.getObjectAt( 5 ) instanceof Surface );
        assertTrue( multiGeometry.getObjectAt( 6 ) instanceof MultiSurface );
        assertTrue( multiGeometry.getObjectAt( 7 ) instanceof MultiSurface );
        return multiGeometry;
    }

    /*
     * Test for void doService(OGCWebServiceEvent)
     */

    public void testWrapGMLBox() {

        LOG.logInfo( " --- \t GMLBox \t --- " );

        String coord = "<gml:Box xmlns:gml=\"http://www.opengis.net/gml\" >"
                       + "<gml:coord><gml:X>10</gml:X><gml:Y>50</gml:Y></gml:coord>"
                       + "<gml:coord><gml:X>12</gml:X><gml:Y>56</gml:Y></gml:coord></gml:Box>";

        String coordinates = "<gml:Box xmlns:gml=\"http://www.opengis.net/gml\" >"
                             + "<gml:coordinates cs=\",\" decimal=\".\" ts=\" \">10,50 12,56"
                             + "</gml:coordinates></gml:Box>";

        String pos = "<gml:Box xmlns:gml=\"http://www.opengis.net/gml\">" + "<gml:pos dimension=\"2\">10 50</gml:pos>"
                     + "<gml:pos dimension=\"2\">12 56</gml:pos></gml:Box>";

        try {
            StringReader sr = new StringReader( coord );
            Document doc = XMLTools.parse( sr );
            Envelope env = GMLGeometryAdapter.wrapBox( doc.getDocumentElement(), null );
            LOG.logInfo( env.toString() );

            if ( env.getMin().getX() != 10 || env.getMin().getY() != 50 ||

            env.getMax().getX() != 12 || env.getMax().getY() != 56 ) {
                throw new Exception( "gml:Box - coord hasn't been read correctly" );
            }

        } catch ( Exception e ) {
            LOG.logError( "Unit test failed" + e.getMessage(), e );
            fail( e.getMessage() );
        }

        try {
            StringReader sr = new StringReader( coordinates );
            Document doc = XMLTools.parse( sr );
            Envelope env = GMLGeometryAdapter.wrapBox( doc.getDocumentElement(), null );
            LOG.logInfo( env.toString() );

            if ( env.getMin().getX() != 10 || env.getMin().getY() != 50 || env.getMax().getX() != 12
                 || env.getMax().getY() != 56 ) {

                throw new Exception( "gml:Box - coordinates hasn't been read correctly" );
            }
        } catch ( Exception e ) {
            LOG.logError( "Unit test failed" + e.getMessage(), e );
            fail( e.getMessage() );
        }

        try {

            StringReader sr = new StringReader( pos );
            Document doc = XMLTools.parse( sr );
            Envelope env = GMLGeometryAdapter.wrapBox( doc.getDocumentElement(), null );
            LOG.logInfo( env.toString() );

            if ( env.getMin().getX() != 10 || env.getMin().getY() != 50 || env.getMax().getX() != 12
                 || env.getMax().getY() != 56 ) {

                throw new Exception( "gml:Box - pos hasn't been read correctly" );
            }
        } catch ( Exception e ) {
            LOG.logError( "Unit test failed" + e.getMessage(), e );
            fail( e.getMessage() );
        }
        LOG.logInfo( " ----------------------------------" );
    }

    /*
     * Test wether the <tt>MultiPoint</tt> object mpt contains all Points defined in 2-dimensional array points @param
     * mpt @param points
     */
    public void multiPointTesting( MultiPoint mpt, double[][] points ) {

        Point[] pts = mpt.getAllPoints();

        for ( int i = 0; i < pts.length; i++ ) {

            LOG.logInfo( "\t********************" );
            LOG.logInfo( "\t pos[" + i + "].getX()=" + pts[i].getX() );
            LOG.logInfo( "\t pos[" + i + "].getY()=" + pts[i].getY() );
            LOG.logInfo( "\t expected:" );
            LOG.logInfo( "\t points[" + i + "][0] =" + points[i][0] );
            LOG.logInfo( "\t points[" + i + "][1] =" + points[i][1] );
            LOG.logInfo( "\t********************" );
            LOG.logInfo( "" );

            assertTrue( "X coordinates expected to be equal", points[i][0] == pts[i].getX() );
            assertTrue( "Y coordinates expected to be equal", points[i][1] == pts[i].getY() );
        }
    }

    /*
     * Test wether the <tt>Curve</tt> object cv contains all Positions defined in 2-dimensional array points @param cv
     * 
     * @param points
     */
    public void curveTesting( Curve cv, double[][] points ) {

        Position[] curvePositons = null;

        try {
            curvePositons = cv.getAsLineString().getPositions();
        } catch ( Exception e ) {
            String s = "Unit failed: \n" + e.getMessage();
            LOG.logError( s, e );
            fail( e.getMessage() );
        }

        for ( int i = 0; i < curvePositons.length; i++ ) {

            LOG.logInfo( "\t*****************" );
            LOG.logInfo( "pos[" + i + "].getX()=" + curvePositons[i].getX() );
            LOG.logInfo( "pos[" + i + "].getY()=" + curvePositons[i].getY() );
            LOG.logInfo( "\tExpected" );
            LOG.logInfo( "points[" + i + "][0] =" + points[i][0] );
            LOG.logInfo( "points[" + i + "][1] =" + points[i][1] );
            LOG.logInfo( "\t*****************" );
            LOG.logInfo( "" );

            assertEquals( "X coordinates expected to be equal", points[i][0], curvePositons[i].getX(), 0.001 );
            assertEquals( "Y coordinates expected to be equal", points[i][1], curvePositons[i].getY(), 0.001 );
        }
    }

    /*
     * Test wether curve[i] of the <tt>MultiCurve</tt>[ object mcv contains all Points defined points[i] @param mpt
     * 
     * @param points
     */
    public void multiCurveTesting( MultiCurve mcv, double[][][] points ) {

        Curve[] cvs = mcv.getAllCurves();

        for ( int i = 0; i < cvs.length; i++ ) {
            LOG.logInfo( "\t Testing curve[" + i + "]: " );
            curveTesting( cvs[i], points[i] );
        }
    }

    /*
     * Test wether the exterior of the <tt>Surface</tt> object surface contains the Postions defined in ths
     * 2-dimensional array exterior and wether the interiors[i] contains the Position of interiors[i] @param mpt @param
     * exterior @param interiors
     */
    public void surfaceTesting( Surface surface, double[][] exterior, double[][][] interiors ) {

        SurfacePatch surfPatch = null;
        Position[] surfExtPosition = null;

        try {

            // Exterior Test
            surfPatch = surface.getSurfacePatchAt( 0 );
            surfExtPosition = surfPatch.getExteriorRing();

        } catch ( Exception e ) {

            LOG.logError( "surfaceTest failed" + e.getMessage(), e );
            fail( e.getMessage() );
        }

        int extLength1 = surfExtPosition.length;
        int extLength2 = exterior.length;

        LOG.logInfo( "surfExtPosition.length: " + extLength1 );
        LOG.logInfo( "Expected: " + extLength2 );
        assertTrue( "Exterior length expected to be equal", extLength1 == extLength2 );
        exteriorTesting( exterior, surfExtPosition );

        if ( interiors == null ) {
            return;
        }

        // Interiors Test
        Position[][] surfInteriors = surfPatch.getInteriorRings();

        int intLength1 = surfInteriors.length;
        int intLength2 = interiors.length;
        LOG.logInfo( "surfInteriors.length: " + intLength1 );
        LOG.logInfo( "Expected: " + intLength2 );
        assertTrue( "Interior lengths expected to be equal", intLength1 == intLength2 );

        interiorsTesting( interiors, surfInteriors );
    }

    /*
     * Test wether surfExtPosition contains alls Positions defined in exterior @param exterior @param surfExtPosition
     */
    public void exteriorTesting( double[][] exterior, Position[] surfExtPosition ) {

        for ( int i = 0; i < surfExtPosition.length; i++ ) {

            LOG.logInfo( "\t*********************" );
            LOG.logInfo( "\t surfExtPosition[" + i + "].getX() = " + surfExtPosition[i].getX() );
            LOG.logInfo( "\t surfExtPosition[" + i + "].getY() = " + surfExtPosition[i].getY() );
            LOG.logInfo( "\t Expected:" );
            LOG.logInfo( "\t exterior[" + i + "][0] = " + exterior[i][0] );
            LOG.logInfo( "\t exterior[" + i + "][1] = " + exterior[i][1] );
            LOG.logInfo( "\t*********************" );
            LOG.logInfo( "" );

            assertTrue( "X coordinate expected to be equal", surfExtPosition[i].getX() == exterior[i][0] );
            assertTrue( "Y coordinate expected to be equal", surfExtPosition[i].getY() == exterior[i][1] );
        }
    }

    /*
     * Test wether surfInteriors contains all Positions defined in ineterior @param interior @param surfInteriors
     */
    public void interiorsTesting( double[][][] interiors, Position[][] surfInteriors ) {

        for ( int i = 0; i < surfInteriors.length; i++ ) {

            Position[] surfIntPosition = surfInteriors[i];

            for ( int j = 0; j < surfIntPosition.length; j++ ) {

                LOG.logInfo( "\t*********************" );
                LOG.logInfo( "\t surfExtPosition[" + i + "].getX() = " + surfIntPosition[i].getX() );
                LOG.logInfo( "\t surfIntPosition[" + i + "].getY() = " + surfIntPosition[i].getY() );
                LOG.logInfo( "\t Expected:" );
                LOG.logInfo( "\t interiors[" + i + "][0] = " + interiors[i][0] );
                LOG.logInfo( "\t interiors[" + i + "][1] = " + interiors[i][1] );
                LOG.logInfo( "\t*********************" );
                LOG.logInfo( "" );

                assertTrue( "X coordinate expected to be equal", surfIntPosition[j].getX() == interiors[i][j][0] );
                assertTrue( "Y coordinate expected to be equal", surfIntPosition[j].getY() == interiors[i][j][1] );
            }
        }
    }

    /*
     * Test wether the exterior of suface[i] of the <tt>MultiSurface</tt> object msf contains all Positions in
     * exterior[i] and wether its interiors contains all Positions in interiors[i] @param msf @param exteriors @param
     * interiors
     */
    public void multiSurfaceTesting( MultiSurface msf, double[][][] exteriors, double[][][][] interiors ) {

        try {

            Surface[] surfaces = msf.getAllSurfaces();

            if ( surfaces.length != exteriors.length ) {
                String s = "surfaces.length != exteriors.length\n" + "surfaces.length == exteriors.length expected";
                throw new Exception( s );
            }

            for ( int i = 0; i < surfaces.length; i++ ) {
                surfaceTesting( surfaces[i], exteriors[i], interiors[i] );
            }
        } catch ( Exception e ) {
            LOG.logError( "\tmultiSurfaceTest failed \n" + e.getMessage(), e );
            fail( e.getMessage() );
        }
    }

    /*
     * Wraps a gmlGeometry into deegre Geometry object @param identifier @param gml
     */
    public Geometry wrapGmlString( String identifier, String gml ) {

        try {
            LOG.logInfo( identifier );
            StringReader sr = new StringReader( gml );
            Document doc = XMLTools.parse( sr );
            Geometry geom = GMLGeometryAdapter.wrap( doc.getDocumentElement(), null );
            return geom;
        } catch ( Exception e ) {
            LOG.logError( "\tmultiSurfaceTest failed \n" + e.getMessage(), e );
            fail( e.getMessage() );
            throw new RuntimeException( e );
        }
    }

    /*
     * Tests wether the conversion Point instance into a <gml:Point> is correctly made
     */
    public void testExportPoint() {

        CoordinateSystem crs = null;
        Geometry pt = GeometryFactory.createPoint( 1, 1, crs );
        try {
            if ( pt instanceof Point ) {
                StringBuffer sb = GMLGeometryAdapter.export( pt );
                LOG.logInfo( "\t--- Exporting Point (1,1,\"crs\") ---" );
                LOG.logInfo( sb.toString() );
                // -------------------------
                String resultFileName = new URL( Configuration.getWFSBaseDir(), Configuration.GENERATED_DIR
                                                                                + "/point_result.xml" ).getFile();
                LOG.logInfo( "--- ExportPoint --- " );
                FileOutputStream file = new FileOutputStream( resultFileName );
                PrintWriter pw = GMLGeometryAdapter.export( pt, file );
                pw.close();
                LOG.logInfo( "--- /ExportPoint --- " );

            } else {
                throw new Exception( "Point hasn't been correctly crested" );
            }
        } catch ( Exception e ) {
            LOG.logError( "\ttestExportPoint failed \n" + e.getMessage(), e );
            fail( e.getMessage() );
        }
        LOG.logInfo( " ----------------------------------" );
    }

    /*
     * Tests wether the conversion MultiPoint instance into a <gml:MultiPoint> is correctly made
     */
    public void testExportMultiPoint() {

        Point[] pts = new Point[4];

        for ( int i = 0; i < pts.length; i++ ) {
            pts[i] = GeometryFactory.createPoint( i, i, null );
        }

        try {

            Geometry mpt = GeometryFactory.createMultiPoint( pts );

            if ( mpt instanceof MultiPoint ) {
                StringBuffer sb = GMLGeometryAdapter.export( mpt );
                LOG.logInfo( " --- Exporting MultiPoints (0,0,\"crs\") (1,1,\"crs\") (2,2,\"crs\") (3,3,\"crs\") ---" );
                LOG.logInfo( sb.toString() );
                String resultFileName = new URL( Configuration.getWFSBaseDir(), Configuration.GENERATED_DIR
                                                                                + "/multipoint_result.xml" ).getFile();
                FileOutputStream file = new FileOutputStream( resultFileName );
                PrintWriter pw = GMLGeometryAdapter.export( mpt, file );
                pw.close();
                LOG.logInfo( "MutiPoint exported" );

            } else {
                throw new Exception( "MultiPoint hasn't been correctly created" );
            }
        } catch ( Exception e ) {
            LOG.logError( "\ttestExportMultiPoint failed \n" + e.getMessage(), e );
            fail( e.getMessage() );
        }
        LOG.logInfo( " ----------------------------------" );
    }

    /*
     * Tests wether the conversion Curve instance into a <gml:LineString> is correctly made
     */
    public void testExportLineString() {

        CoordinateSystem crs = null;
        Position[] pos = new Position[4];

        for ( int i = 0; i < pos.length; i++ ) {
            pos[i] = GeometryFactory.createPosition( i + 1, 1 - i );
        }

        try {

            Geometry geom = GeometryFactory.createCurve( pos, crs );

            if ( geom instanceof Curve ) {
                StringBuffer sb = GMLGeometryAdapter.export( geom );
                LOG.logInfo( " --- LineString Point (1,1,\"\") (2,0,\"\") (3,-1,\"\") (4,-2,\"\") ---" );
                LOG.logInfo( sb.toString() );
            } else {
                throw new Exception( "LineString hasn't been correctly created" );
            }
        } catch ( Exception e ) {
            LOG.logError( "\ttestExportLineString failed \n" + e.getMessage(), e );
            fail( e.getMessage() );
        }
        LOG.logInfo( " ----------------------------------" );
    }

    /*
     * Tests wether the conversion MultiCurve instance into a <gml:MultiLineString> is correctly made
     */
    public void testExportMultiLineString() {

        try {
            Position[][] pos = new Position[2][3];
            CoordinateSystem crs = null;

            for ( int i = 0; i < pos.length; i++ ) {
                for ( int j = 0; j < 3; j++ ) {
                    pos[i][j] = GeometryFactory.createPosition( i, j );
                }
            }

            Curve[] curves = new Curve[pos.length];
            for ( int i = 0; i < curves.length; i++ ) {
                curves[i] = GeometryFactory.createCurve( pos[i], crs );
            }

            Geometry geom = GeometryFactory.createMultiCurve( curves );

            if ( geom instanceof MultiCurve ) {
                StringBuffer sb = GMLGeometryAdapter.export( geom );
                LOG.logInfo( " --- Exporting MultiCurves -----" );
                LOG.logInfo( "Curve1: (0,0,\"\") (0,1,\"\") (0,2,\"\")" );
                LOG.logInfo( "Curve2: (1,0,\"\") (1,1,\"\") (1,2,\"\")" );
                LOG.logInfo( sb.toString() );

            } else {
                throw new Exception( "MultiLineString hasn't been correctly created" );
            }
        } catch ( Exception e ) {
            LOG.logError( "\ttestExportMultiLineString failed \n" + e.getMessage(), e );
            fail( e.getMessage() );
        }
        LOG.logInfo( " ----------------------------------" );
    }

    /*
     * Tests wether the conversion Polygon instance into a <gml:Polygon> is correctly made
     */
    public void testExportPolygon() {

        try {

            Position[] exteriorRing = new Position[5];
            exteriorRing[0] = GeometryFactory.createPosition( 100, 100 );
            exteriorRing[1] = GeometryFactory.createPosition( 99, -99 );
            exteriorRing[2] = GeometryFactory.createPosition( -88, -88 );
            exteriorRing[3] = GeometryFactory.createPosition( -77, 77 );
            exteriorRing[4] = GeometryFactory.createPosition( 100, 100 );
            Position[][] interiorRings = new Position[2][3];

            interiorRings[0][0] = GeometryFactory.createPosition( 1, 1 );
            interiorRings[0][1] = GeometryFactory.createPosition( 9, -9 );
            interiorRings[0][2] = GeometryFactory.createPosition( 1, 1 );
            interiorRings[1][0] = GeometryFactory.createPosition( 10, 10 );
            interiorRings[1][1] = GeometryFactory.createPosition( 5, -5 );
            interiorRings[1][2] = GeometryFactory.createPosition( 10, 10 );

            Geometry geom = GeometryFactory.createSurface( exteriorRing, interiorRings, null, null );

            if ( geom instanceof Surface ) {
                StringBuffer sb = GMLGeometryAdapter.export( geom );
                LOG.logInfo( " --- Exporting Surface -----" );
                LOG.logInfo( "Exterior: (100,100,\"\"),(99,-99,\"\"),(-88,-88,\"\"),(-77,77,\"\"),(100,100,\"\")" );
                LOG.logInfo( "Interior1: (1,1,\"\") (9,-9,\"\") (1,1,\"\")" );
                LOG.logInfo( "Interior1: (10,10,\"\") (5,-5,\"\") (10,10,\"\")" );
                LOG.logInfo( sb.toString() );
            } else {
                throw new Exception( "Surface hasn't been correctly created" );
            }
        } catch ( Exception e ) {
            LOG.logError( "\ttestExportPolygon failed \n" + e.getMessage(), e );
            fail( e.getMessage() );
        }
        LOG.logInfo( " ----------------------------------" );
    }

    /*
     * Tests wether the conversion MultiPolygon instance into a <gml:MultiPolygon> is correctly made
     */
    public void testExportMultiPolygon() {
        /*
         * double[][][][] interiors = { {}, { { { 1, 1 }, { 9, -9 }, { -8, -8 }, { -7, 7 }, { 1, 1 } } } };
         */
        try {

            Position[][] exteriorRing = new Position[2][5];
            exteriorRing[0][0] = GeometryFactory.createPosition( 100, 100 );
            exteriorRing[0][1] = GeometryFactory.createPosition( 99, -99 );
            exteriorRing[0][2] = GeometryFactory.createPosition( -88, -88 );
            exteriorRing[0][3] = GeometryFactory.createPosition( -77, 77 );
            exteriorRing[0][4] = GeometryFactory.createPosition( 100, 100 );
            exteriorRing[1][0] = GeometryFactory.createPosition( 111, 111 );
            exteriorRing[1][1] = GeometryFactory.createPosition( 111, 999 );
            exteriorRing[1][2] = GeometryFactory.createPosition( 1000, 999 );
            exteriorRing[1][3] = GeometryFactory.createPosition( 1000, 111 );
            exteriorRing[1][4] = GeometryFactory.createPosition( 111, 111 );

            Position[][] interiorRings = new Position[2][3];
            interiorRings[0][0] = GeometryFactory.createPosition( 1, 1 );
            interiorRings[0][1] = GeometryFactory.createPosition( 9, -9 );
            interiorRings[0][2] = GeometryFactory.createPosition( 1, 1 );
            interiorRings[1][0] = GeometryFactory.createPosition( 10, 10 );
            interiorRings[1][1] = GeometryFactory.createPosition( 5, -5 );
            interiorRings[1][2] = GeometryFactory.createPosition( 10, 10 );

            Surface[] surface = new Surface[2];
            surface[0] = GeometryFactory.createSurface( exteriorRing[0], interiorRings, null, null );
            surface[1] = GeometryFactory.createSurface( exteriorRing[1], null, null, null );
            Geometry geometry = null;
            geometry = GeometryFactory.createMultiSurface( surface );

            if ( geometry instanceof MultiSurface ) {

                StringBuffer sb = GMLGeometryAdapter.export( geometry );
                LOG.logInfo( " --- Exporting MultiSurface -----" );
                LOG.logInfo( "\tPolygon1: " );
                LOG.logInfo( "Exterior1 (100,100),\"\"),(99,-99,\"\"),(-88,-88,\"\"),(-77,77,\"\"),(100,100,\"\")" );
                LOG.logInfo( "Interior1_1: (1,1,\"\") (9,-9,\"\") (1,1,\"\")" );
                LOG.logInfo( "Interior1_2: (10,10,\"\") (5,-5,\"\") (10,10,\"\")" );
                LOG.logInfo( "\tPolygon2: " );
                LOG.logInfo( "Exterior2 (111,111),\"\"),(111,999,\"\"),(1000,999,\"\"),(1000,111,\"\"),(111,111,\"\")" );
                LOG.logInfo( "No Interiors" );
                LOG.logInfo( sb.toString() );

            } else {
                throw new Exception( "MultiSurface hasn't been correctly created" );
            }
        }

        catch ( Exception e ) {
            LOG.logError( "\ttestMultiExportPolygon failed \n" + e.getMessage(), e );
            fail( e.getMessage() );
        }
        LOG.logInfo( " ----------------------------------" );
    }

    /**
     * Tests the exporting of a {@link MultiGeometry} instance into a <gml:MultiGeometry> element.
     * 
     * @throws GeometryException
     * @throws IOException
     * @throws SAXException
     */
    public void testExportMultiGeometry()
                            throws GeometryException, IOException, SAXException {

        MultiGeometry multiGeometry = testWrapMultiGeometry();
        String resultFileName = new URL( Configuration.getWFSBaseDir(), Configuration.GENERATED_DIR
                                                                        + "/multigeometry_result.xml" ).getFile();
        FileOutputStream file = new FileOutputStream( resultFileName );
        PrintWriter pw = GMLGeometryAdapter.export( multiGeometry, file );
        pw.close();
    }

    /*
     * Tests wether the conversion Envelope instance into a <gml:Box> is correctly made and wether the conversion
     * Envelope instance into a <gml:Envelope> is correctly made
     */
    public void testExportEnvelope() {

        try {

            Position[] pos = new Position[5];
            pos[0] = GeometryFactory.createPosition( -100, -100 );
            pos[1] = GeometryFactory.createPosition( 100, 100 );
            Envelope env = GeometryFactory.createEnvelope( pos[0], pos[1], null );

            StringBuffer sb1 = GMLGeometryAdapter.exportAsBox( env );
            LOG.logInfo( " --- Exporting EnvolopeAsBox -----" );
            LOG.logInfo( "Min: (-100,-100,\"\"),(100,100,\"\")" );
            LOG.logInfo( sb1.toString() );

            StringBuffer sb2 = GMLGeometryAdapter.exportAsEnvelope( env );
            LOG.logInfo( " --- Exporting EnvolopeAsEnvelope -----" );
            LOG.logInfo( "Min: (-100,-100,\"\"),(100,100,\"\")" );
            LOG.logInfo( sb2.toString() );
        } catch ( Exception e ) {
            LOG.logError( "testEnvelope failed" + e.getMessage(), e );
            fail( e.getMessage() );
        }
        LOG.logInfo( " ----------------------------------" );
    }

    /**
     * Tests whether wrapCurveAsCurve correctly returns a curve instance.
     */
    public void testWrapCurveAsCurve()
                            throws Exception {

        URL inputURL = GMLGeometryAdapterTest.class.getResource( "curve.xml" );
        XMLFragment xml = new XMLFragment();
        xml.load( inputURL );
        Curve curve = GMLGeometryAdapter.wrapCurveAsCurve( xml.getRootElement(), null );
        assertNotNull( curve );
        String resultFileName = new URL( Configuration.getWFSBaseDir(), Configuration.GENERATED_DIR
                                                                        + "/curve_result.xml" ).getFile();
        FileOutputStream file = new FileOutputStream( resultFileName );
        PrintWriter pw = GMLGeometryAdapter.export( curve, file );
        pw.close();
        LOG.logInfo( "curve exported" );
    }

    public void testWrapSurfaceAsSurface()
                            throws Exception {

        URL inputURL = GMLGeometryAdapterTest.class.getResource( "surface.xml" );
        XMLFragment xml = new XMLFragment();
        xml.load( inputURL );
        Surface surface = GMLGeometryAdapter.wrapSurfaceAsSurface( xml.getRootElement(), null );
        assertNotNull( surface );
        String resultFileName = new URL( Configuration.getWFSBaseDir(), Configuration.GENERATED_DIR
                                                                        + "/surface_result.xml" ).getFile();
        FileOutputStream file = new FileOutputStream( resultFileName );
        PrintWriter pw = GMLGeometryAdapter.export( surface, file );
        pw.close();
        LOG.logInfo( "surface exported" );
    }

    public void testWrapSurfaceAsSurface2()
                            throws Exception {

        // surface made from segments
        URL inputURL = GMLGeometryAdapterTest.class.getResource( "surface2.xml" );
        XMLFragment xml = new XMLFragment();
        xml.load( inputURL );
        Surface surface = GMLGeometryAdapter.wrapSurfaceAsSurface( xml.getRootElement(), null );
        assertNotNull( surface );
        String resultFileName = new URL( Configuration.getWFSBaseDir(), Configuration.GENERATED_DIR
                                                                        + "/surface2_result.xml" ).getFile();
        FileOutputStream file = new FileOutputStream( resultFileName );
        PrintWriter pw = GMLGeometryAdapter.export( surface, file );
        pw.close();
        LOG.logInfo( "surface exported" );
    }

    public void testWrapSurfaceAsSurface3()
                            throws Exception {

        // surfaces containing Arcs
        URL inputURL = GMLGeometryAdapterTest.class.getResource( "surface3.xml" );
        XMLFragment xml = new XMLFragment();
        xml.load( inputURL );
        Surface surface = GMLGeometryAdapter.wrapSurfaceAsSurface( xml.getRootElement(), null );
        assertNotNull( surface );
        String resultFileName = new URL( Configuration.getWFSBaseDir(), Configuration.GENERATED_DIR
                                                                        + "/surface3_result.xml" ).getFile();
        FileOutputStream file = new FileOutputStream( resultFileName );
        PrintWriter pw = GMLGeometryAdapter.export( surface, file );
        pw.close();
        LOG.logInfo( "surface exported" );
    }

    public void testMultiCurveAsMultiCurve()
                            throws Exception {

        URL inputURL = GMLGeometryAdapterTest.class.getResource( "multiCurve.xml" );
        XMLFragment xml = new XMLFragment();
        xml.load( inputURL );
        MultiCurve multiCurve = GMLGeometryAdapter.wrapMultiCurveAsMultiCurve( xml.getRootElement(), null );
        assertNotNull( multiCurve );
        String resultFileName = new URL( Configuration.getWFSBaseDir(), Configuration.GENERATED_DIR
                                                                        + "/multicurve_result.xml" ).getFile();
        FileOutputStream file = new FileOutputStream( resultFileName );
        PrintWriter pw = GMLGeometryAdapter.export( multiCurve, file );
        pw.close();
        LOG.logInfo( " MultiCurve exported" );
    }

    public void testMultiSurfaceAsMultiSurface()
                            throws Exception {

        URL inputURL = GMLGeometryAdapterTest.class.getResource( "multiSurface.xml" );
        XMLFragment xml = new XMLFragment();
        xml.load( inputURL );
        MultiSurface multiSurface = GMLGeometryAdapter.wrapMultiSurfaceAsMultiSurface( xml.getRootElement(), null );
        assertNotNull( multiSurface );
        String resultFileName = new URL( Configuration.getWFSBaseDir(), Configuration.GENERATED_DIR
                                                                        + "/multisurface_result.xml" ).getFile();
        FileOutputStream file = new FileOutputStream( resultFileName );
        PrintWriter pw = GMLGeometryAdapter.export( multiSurface, file );
        pw.close();
        LOG.logInfo( "MultiSurfaceExported" );
    }
}
