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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.primitive.Envelope;
import org.deegree.model.geometry.primitive.Point;
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
public class JTSWrapperEnvelopeTest {

    JTSWrapperEnvelope env1, env2, env3;

    Point min1, max1, min2, max2, min3, max3, between;

    static CoordinateSystem crs = CRSFactory.createDummyCRS( "dummy" );

    static double delta = 0.0001;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
                            throws Exception {

        min1 = new JTSWrapperPoint( null, delta, crs, new double[] { 2.0, 3.0 } );
        max1 = new JTSWrapperPoint( null, delta, crs, new double[] { 5.0, 7.0 } );

        min2 = new JTSWrapperPoint( null, delta, crs, new double[] { -1.0, 5.0 } );
        max2 = new JTSWrapperPoint( null, delta, crs, new double[] { 1.0, 11.0 } );

        min3 = new JTSWrapperPoint( null, delta, crs, new double[] { 2.5, 3.5 } );
        max3 = new JTSWrapperPoint( null, delta, crs, new double[] { 4.5, 6.5 } );

        env1 = new JTSWrapperEnvelope( delta, crs, 2, min1, max1 );
        env2 = new JTSWrapperEnvelope( delta, crs, 2, min2, max2 );
        env3 = new JTSWrapperEnvelope( delta, crs, 2, min3, max3 );

        // between both envelopes
        between = new JTSWrapperPoint( null, delta, crs, new double[] { 1.5, 6.0 } );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperEnvelope#JTSWrapperEnvelope(double, org.deegree.model.crs.coordinatesystems.CoordinateSystem, int, org.deegree.model.geometry.primitive.Point, org.deegree.model.geometry.primitive.Point)}.
     */
    // @Test
    public void testJTSWrapperEnvelope() {
        // constructor is used in setUp, that should be enough testing for now
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperEnvelope#getMax()}.
     */
    @Test
    public void testGetMax() {
        assertTrue( env1.getMax().intersects( max1 ) );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperEnvelope#getMin()}.
     */
    @Test
    public void testGetMin() {
        // point intersects point
        // Geometry p1 = new WKTReader().read("POINT (2.0 3.0)");
        // Geometry p2 = new WKTReader().read("POINT (2.0 3.0)");
        // assertTrue(p1.intersects( p2 ));
        assertTrue( env1.getMin().intersects( min1 ) );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperEnvelope#merger(org.deegree.model.geometry.primitive.Envelope)}.
     */
    @Test
    public void testMerger() {
        Envelope merge = env1.merger( env2 );
        assertEquals( 6.0, merge.getWidth() );
        assertEquals( 8.0, merge.getHeight() );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperEnvelope#getHeight()}.
     */
    @Test
    public void testGetHeight() {
        assertEquals( 4.0, env1.getHeight(), 0.0001 );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperEnvelope#getWidth()}.
     */
    @Test
    public void testGetWidth() {
        assertEquals( 3.0, env1.getWidth(), 0.0001 );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#distance(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testDistance() {
        assertEquals( 1.0, env1.distance( env2 ), 0.0001 );
        assertEquals( 3.0, env1.distance( min2 ), 0.0001 );
        assertEquals( 0.0, env1.distance( min1 ), 0.0001 );
        assertEquals( 0.0, env1.distance( env1 ), 0.0001 );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#getPrecision()}.
     */
    @Test
    public void testGetPrecision() {
        assertEquals( delta, env1.getPrecision() );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#getBuffer(double)}.
     */
    @Test
    public void testGetBuffer() {
        assertTrue( env1.getBuffer( 0.01 ).contains( env1 ) );
        assertFalse( env1.getBuffer( 0.01 ).contains( env2 ) );

        assertTrue( env2.getBuffer( 0.01 ).contains( env2 ) );
        assertFalse( env2.getBuffer( 0.01 ).contains( env1 ) );

        assertTrue( env1.getBuffer( 10.0 ).contains( env2 ) );
        assertTrue( env2.getBuffer( 10.0 ).contains( env1 ) );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#getConvexHull()}.
     */
    @Test
    public void testGetConvexHull() {
        assertTrue( env1.getConvexHull().contains( env1 ) );
        assertTrue( env2.getConvexHull().contains( env2 ) );
        JTSWrapperPoint p = new JTSWrapperPoint(null, delta, null, new double[] { 6.0, 10.0 } );
        assertFalse( env1.union( env2 ).getConvexHull().contains( p ) );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#getCoordinateDimension()}.
     */
    @Test
    public void testGetCoordinateDimension() {
        assertEquals( 2, env1.getCoordinateDimension() );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#getCoordinateSystem()}.
     */
    @Test
    public void testGetCoordinateSystem() {
        assertEquals( crs, env1.getCoordinateSystem() );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#getEnvelope()}.
     */
    @Test
    public void testGetEnvelope() {
        assertTrue( env1.equals( env1.getEnvelope() ) );
        assertFalse( env1.equals( env2.getEnvelope() ) );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#difference(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testDifference() {
        assertTrue( env1.difference( env2 ).equals( env1 ) );
        assertFalse( env1.difference( env3 ).equals( env1 ) );
        assertNull( env1.difference( env1 ) );
        
    }
    
    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#intersection(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testIntersection() {
        assertNull( env1.intersection( env2 ) );
        assertNotNull( env1.intersection( env3 ) );
        assertNotNull( env1.intersection( min1 ) );
        assertNull( env1.intersection( min2 ) );
        assertNotNull( env1.intersection( min3 ) );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#intersects(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testIntersects() {
        assertTrue( env1.intersects( env3 ) );
        assertTrue( env3.intersects( env1 ) );
        assertFalse( env1.intersects( env2 ) );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#contains(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testContains() {
        assertTrue( env1.contains( env3 ) );
        assertFalse( env3.contains( env1 ) );
        assertFalse( env2.contains( env1 ) );
        assertFalse( env1.contains( env2 ) );
    }
    
    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#isWithin(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testIsWithin() {
        assertTrue( env3.isWithin( env1 ) );
        assertFalse( env1.isWithin( env3 ) );
        assertFalse( env1.isWithin( env2 ) );
        assertFalse( env2.isWithin( env1 ) );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#isWithinDistance(org.deegree.model.geometry.Geometry, double)}.
     */
    @Test
    public void testIsWithinDistance() {
        assertFalse( env1.isWithinDistance( env2, 0.999 ) );
        assertTrue( env1.isWithinDistance( env2, 1.001 ) );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#isBeyond(org.deegree.model.geometry.Geometry, double)}.
     */
    @Test
    public void testIsBeyond() {
        assertTrue( env1.isBeyond( env2, 0.999 ) );
        assertFalse( env1.isBeyond( env2, 1.001 ) );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#union(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testUnion() {
        Geometry union = env1.union( env2 );
        assertTrue( union.contains( env1 ) );
        assertTrue( union.contains( env2 ) );
        assertFalse( union.contains( between ) );

        union = env1.union( env3 );
        assertTrue( union.equals( env1 ) );
    }

    /**
     * Test method for
     * {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#equals(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testEquals() {
        JTSWrapperEnvelope env1_ = new JTSWrapperEnvelope( delta, crs, 2, max1, min1 );
        assertTrue( env1.equals( env1 ) );
        assertTrue( env1.equals( env1_ ) );
        assertFalse( env2.equals( env1 ) );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#getJTSGeometry()}.
     */
    @Test
    public void testGetJTSGeometry() {
        assertTrue( env1.getJTSGeometry().isSimple() );
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

}
