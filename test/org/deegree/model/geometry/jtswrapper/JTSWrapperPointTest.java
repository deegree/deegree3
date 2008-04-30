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

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;

/**
 *
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: $
 *
 * @version $Revision: $, $Date: $
 *
 */
public class JTSWrapperPointTest {

    JTSWrapperPoint p1;
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
                            throws Exception {
        p1 = new JTSWrapperPoint(0.01, null, new double[] {2.0, 3.0} );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperPoint#getEnvelope()}.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetEnvelope() {
        p1.getEnvelope();
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperPoint#JTSWrapperPoint(double, org.deegree.model.crs.coordinatesystems.CoordinateSystem, double[])}.
     */
    @Test
    public void testJTSWrapperPoint() {
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperPoint#get(int)}.
     */
    @Test
    public void testGet() {
        assertEquals( 2.0, p1.get(0), 0.00001 );
        assertEquals( 3.0, p1.get(1), 0.00001 );
        assertEquals( Double.NaN, p1.get(2) );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperPoint#getAsArray()}.
     */
    @Test
    public void testGetAsArray() {
        double[] arr = p1.getAsArray();
        assertEquals( 2, arr.length );
        assertEquals( 2.0, arr[0], 0.00001 );
        assertEquals( 3.0, arr[1], 0.00001 );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperPoint#getX()}.
     */
    @Test
    public void testGetX() {
        assertEquals( 2.0, p1.getX(), 0.00001 );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperPoint#getY()}.
     */
    @Test
    public void testGetY() {
        assertEquals( 3.0, p1.getY(), 0.00001 );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperPoint#getZ()}.
     */
    @Test
    public void testGetZ() {
        assertEquals( Double.NaN, p1.getZ() );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#JTSWrapperGeometry(double, org.deegree.model.crs.coordinatesystems.CoordinateSystem, int)}.
     */
    @Test
    public void testJTSWrapperGeometry() {
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#export(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testExport() {
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#wrap(com.vividsolutions.jts.geom.Geometry)}.
     */
    @Test
    public void testWrap() {
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#toPoint(com.vividsolutions.jts.geom.Coordinate)}.
     */
    @Test
    public void testToPoint() {
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#toPoints(com.vividsolutions.jts.geom.Coordinate[])}.
     */
    @Test
    public void testToPoints() {
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#contains(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testContains() {
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#difference(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testDifference() {
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#distance(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testDistance() {
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#getPrecision()}.
     */
    @Test
    public void testGetPrecision() {
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#getBuffer(double)}.
     */
    @Test
    public void testGetBuffer() {
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#getConvexHull()}.
     */
    @Test
    public void testGetConvexHull() {
        fail( "Not yet implemented" );
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
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#intersection(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testIntersection() {
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#intersects(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testIntersects() {
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#isWithin(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testIsWithin() {
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#isWithinDistance(org.deegree.model.geometry.Geometry, double)}.
     */
    @Test
    public void testIsWithinDistance() {
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#isBeyond(org.deegree.model.geometry.Geometry, double)}.
     */
    @Test
    public void testIsBeyond() {
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#union(org.deegree.model.geometry.Geometry)}.
     */
    @Test
    public void testUnion() {
        fail( "Not yet implemented" );
    }

    /**
     * Test method for {@link org.deegree.model.geometry.jtswrapper.JTSWrapperGeometry#getJTSGeometry()}.
     */
    @Test
    public void testGetJTSGeometry() {
        assertTrue( p1.getJTSGeometry().isSimple() );
    }

}
