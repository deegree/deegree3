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
 53177 Bonn
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
package org.deegree.model.geometry.jtswrapper;

import static org.junit.Assert.*;

import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.primitive.Envelope;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class JTSWrapperPointTest {

    JTSWrapperPoint p1, p2, p3;

    static double delta = 0.001;
    static double testDelta = 0.00001;

    static CoordinateSystem crs = CRSFactory.createDummyCRS( "dummy" );

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
                            throws Exception {
        p1 = new JTSWrapperPoint( null, delta, crs, new double[] { 2.0, 3.0 } );
        p2 = new JTSWrapperPoint( null, delta, crs, new double[] { -1.0, 5.0 } );
        p3 = new JTSWrapperPoint( null, delta, crs, new double[] { 2.00001, 3.00001 } );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperPoint#getEnvelope()}.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetEnvelope() {
        p1.getEnvelope();
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperPoint#JTSWrapperPoint(String, double, org.deegree.model.crs.coordinatesystems.CoordinateSystem, double[])}.
     */
    // @Test
    public void testJTSWrapperPoint() {
        // constructor is used in setUp, that should be enough testing for now
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperPoint#get(int)}.
     */
    @Test
    public void testGet() {
        assertEquals( 2.0, p1.get( 0 ), testDelta );
        assertEquals( 3.0, p1.get( 1 ), testDelta );
        assertEquals( Double.NaN, p1.get( 2 ) );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperPoint#getAsArray()}.
     */
    @Test
    public void testGetAsArray() {
        double[] arr = p1.getAsArray();
        assertEquals( 2, arr.length );
        assertEquals( 2.0, arr[0], testDelta );
        assertEquals( 3.0, arr[1], testDelta );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperPoint#getX()}.
     */
    @Test
    public void testGetX() {
        assertEquals( 2.0, p1.getX(), testDelta );
        assertEquals( p1.getX(), p3.getX(), testDelta );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperPoint#getY()}.
     */
    @Test
    public void testGetY() {
        assertEquals( 3.0, p1.getY(), testDelta );
        assertEquals( p1.getY(), p3.getY(), testDelta );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperPoint#getZ()}.
     */
    @Test
    public void testGetZ() {
        assertEquals( Double.NaN, p1.getZ() );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#export(org.deegree.model.geometry.Geometry)}.
     */
    // @Test
    public void testExport() {
        // this is more or less a static method, so no tests here
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#wrap(com.vividsolutions.jts.geom.Geometry)}.
     */
    // @Test
    public void testWrap() {
        // this is more or less a static method, so no tests here
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#toPoint(com.vividsolutions.jts.geom.Coordinate)}.
     */
    // @Test
    public void testToPoint() {
        // this is more or less a static method, so no tests here
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#toPoints(com.vividsolutions.jts.geom.Coordinate[])}.
     */
    // @Test
    public void testToPoints() {
        // this is more or less a static method, so no tests here
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#contains(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testContains() {
        assertTrue( p1.contains( p3 ) );
        assertTrue( p3.contains( p1 ) );
        assertFalse( p1.contains( p2 ) );
        assertFalse( p2.contains( p1 ) );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#difference(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testDifference() {
        assertTrue( p1.difference( p2 ).equals( p1 ) );
        assertNull( p1.difference( p3 ) );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#distance(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testDistance() {
        assertEquals( 0.0, p1.distance( p3 ), testDelta );
        assertEquals( 0.0, p1.distance( p3 ), testDelta );
        double dist = Math.sqrt( 2 * 2 + 3 * 3 );
        assertEquals( dist, p1.distance( p2 ), testDelta );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#getPrecision()}.
     */
    @Test
    public void testGetPrecision() {
        assertEquals( delta, p1.getPrecision(), testDelta );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#getBuffer(double)}.
     */
    @Test
    public void testGetBuffer() {
        assertTrue( p2.isWithin( p1.getBuffer( 5.0 ) ) );
        assertTrue( p3.isWithin( p1.getBuffer( 0.01 ) ) );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#getConvexHull()}.
     */
    @Test
    public void testGetConvexHull() {
        assertTrue( p1.getConvexHull().equals( p1 ) );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#getCoordinateDimension()}.
     */
    @Test
    public void testGetCoordinateDimension() {
        assertEquals( 2, p1.getCoordinateDimension() );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#getCoordinateSystem()}.
     */
    @Test
    public void testGetCoordinateSystem() {
        assertEquals( crs, p1.getCoordinateSystem() );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#intersection(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testIntersection() {
        assertNull( p1.intersection( p2 ) );
        assertTrue( p1.equals( p1.intersection( p3 ) ) );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#intersects(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testIntersects() {
        assertTrue( p1.intersects( p1 ) );
        assertTrue( p1.intersects( p3 ) );
        assertFalse( p1.intersects( p2 ) );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#isWithin(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testIsWithin() {
        assertTrue( p1.isWithin( p3 ) );
        assertTrue( p3.isWithin( p1 ) );
        assertFalse( p1.isWithin( p2 ) );
        assertFalse( p2.isWithin( p1 ) );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#isWithinDistance(org.deegree.model.geometry.Geometry, double)}.
     */
    @Test
    public void testIsWithinDistance() {
        assertTrue( p1.isWithinDistance( p3, 0.0 ) );

        double dist = Math.sqrt( 2 * 2 + 3 * 3 );
        assertTrue( p1.isWithinDistance( p2, dist ) );
        assertTrue( p1.isWithinDistance( p2, dist + delta ) );
        assertFalse( p1.isWithinDistance( p2, dist - delta ) );

        // doesn't work as expected, see JTS precision model description
        // http://www.vividsolutions.com/jts/javadoc/com/vividsolutions/jts/geom/PrecisionModel.html
        // assertTrue( p1.isWithinDistance( p2, dist + delta / 2 ) );
        // assertTrue( p1.isWithinDistance( p2, dist - delta / 2 ) );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#isBeyond(org.deegree.model.geometry.Geometry, double)}.
     */
    @Test
    public void testIsBeyond() {
        assertFalse( p1.isBeyond( p3, 0.0 ) );

        double dist = Math.sqrt( 2 * 2 + 3 * 3 );
        assertFalse( p1.isBeyond( p2, dist ) );
        assertFalse( p1.isBeyond( p2, dist + delta ) );
        assertTrue( p1.isBeyond( p2, dist - delta ) );

        // see testIsWithinDistance
        // assertFalse( p1.isBeyond( p2, dist + delta / 2 ) );
        // assertFalse( p1.isBeyond( p2, dist - delta / 2 ) );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#union(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testUnion() {
        Geometry union = p1.union( p2 );
        Envelope env = union.getEnvelope();
        assertEquals( 3.0, env.getWidth(), testDelta );
        assertEquals( 2.0, env.getHeight(), testDelta );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#equals(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testEquals() {
        assertTrue( p1.equals( p1 ) );
        assertTrue( p1.equals( p3 ) );
        assertFalse( p1.equals( p2 ) );

        JTSWrapperPoint p1_1 = new JTSWrapperPoint( null, delta, crs, new double[] { 2.0 + ( delta / 5 ), 3.0 } );
        JTSWrapperPoint p1_2 = new JTSWrapperPoint( null, delta, crs, new double[] { 2.0 + delta, 3.0 } );
        assertTrue( p1.equals( p1_1 ) );
        assertFalse( p1.equals( p1_2 ) );

    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#getJTSGeometry()}.
     */
    @Test
    public void testGetJTSGeometry() {
        assertTrue( p1.getJTSGeometry().isSimple() );
    }

}
