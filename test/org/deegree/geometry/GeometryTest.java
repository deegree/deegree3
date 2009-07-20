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
package org.deegree.geometry;

import static org.junit.Assert.assertTrue;

import org.deegree.crs.CRS;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.points.PackedPoints;
import org.junit.Before;
import org.junit.Test;

/**
 * Some very basic tests. Just to make sure we can create a GeometryFactory and do some simple operations.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$ }
 */
public class GeometryTest {

    private static GeometryFactory geomFactory = new GeometryFactory();

    private Point p1, p2, p3, p4;

    private LineString l1, l2, l3;

    private Envelope env1, env2;

    /**
     * common envelopes as test geometry
     */
    @Before
    public void setUp() {

        CRS crs = new CRS( "EPSG:4326" );
        p1 = geomFactory.createPoint( "p1", 0.0, 0.0, crs );
        p2 = geomFactory.createPoint( "p2", 10.0, 10.0, crs );
        p3 = geomFactory.createPoint( "p3", 10.0, 10.0, crs );
        p4 = geomFactory.createPoint( "p4", 20.0, 20.0, crs );

        l1 = geomFactory.createLineString( "l1", crs,
                                           new PackedPoints( new double[] { 10.0, 5.0, 15.0, 9.0, 20.0, 20.0 }, 2 ) );
        l2 = geomFactory.createLineString( "l2", crs, new PackedPoints( new double[] { 15.0, 20.0, 15.0, 6.0 }, 2 ) );
        l3 = geomFactory.createLineString( "l3", crs, new PackedPoints( new double[] { 9.0, 9.0, 12.0, 5.0 }, 2 ) );

        env1 = geomFactory.createEnvelope( 13.0, 7.0, 21.0, 21.0, crs );
    }

    private Envelope createEnvelope( int x1, int y1, int x2, int y2 ) {
        return geomFactory.createEnvelope( new double[] { x1, y1 }, new double[] { x2, y2 }, null );
    }

    @Test
    public void testIntersects() {
        assertTrue(! p1.intersects( p2 ) );
        assertTrue( !p1.intersects( p3 ) );
        assertTrue( p2.intersects( p3 ) );

        assertTrue( l1.intersects( l2 ) );
        assertTrue( l1.intersects( l3 ) );
        assertTrue( !l2.intersects( l3 ) );
        
        assertTrue(!env1.intersects( p1 ));
        assertTrue(!env1.intersects( p2 ));
        assertTrue(!env1.intersects( p3 ));
        assertTrue (env1.intersects( p4 ));
    }

    //    
    //
    // /**
    // *
    // */
    // @Test
    // public void testContains() {
    // assertTrue( env1.contains( env1 ) );
    // assertTrue( env1.contains( env4 ) );
    // assertTrue( env1.contains( env3 ) );
    // assertFalse( env4.contains( env1 ) );
    // assertFalse( env3.contains( env1 ) );
    // assertFalse( env1.contains( env2 ) );
    // assertFalse( env1.contains( env5 ) );
    // }
    //
    // /**
    // *
    // */
    // @Test
    // public void testIntersects() {
    // assertTrue( env1.intersects( env1 ) );
    // assertFalse( env1.intersects( env2 ) );
    // assertFalse( env2.intersects( env1 ) );
    // assertTrue( env1.intersects( env3 ) );
    // assertTrue( env3.intersects( env1 ) );
    // assertTrue( env1.intersects( env4 ) );
    // assertTrue( env4.intersects( env1 ) );
    // assertTrue( env1.intersects( env5 ) );
    // assertTrue( env5.intersects( env1 ) );
    // assertTrue( env6.intersects( env1 ) );
    // assertTrue( env6.intersects( env3 ) );
    // assertTrue( env6.intersects( env4 ) );
    // assertTrue( env6.intersects( env5 ) );
    // assertTrue( env7.intersects( env2 ) );
    // assertTrue( env7.intersects( env9 ) );
    // assertTrue( env8.intersects( env2 ) );
    // assertTrue( env8.intersects( env7 ) );
    // assertTrue( env9.intersects( env2 ) );
    // assertTrue( env9.intersects( env7 ) );
    // assertTrue( env9.intersects( env8 ) );
    //
    // assertFalse( env9.intersects( env5 ) );
    // assertFalse( env5.intersects( env9 ) );
    // }
    //
    // /**
    // *
    // */
    // @Test
    // public void testIntersection() {
    // assertTrue( env1.intersection( env4 ).equals( env4 ) );
    // assertTrue( env4.intersection( env1 ).equals( env4 ) );
    // assertTrue( env1.intersection( env6 ).equals( createEnvelope( 15, 15, 20, 20 ) ) );
    // assertTrue( env2.intersection( env9 ).equals( createEnvelope( 34, 30, 40, 40 ) ) );
    // assertTrue( env1.intersection( env1 ).equals( env1 ) );
    // assertTrue( env2.intersection( env7 ).equals( createEnvelope( 30, 34, 40, 36 ) ) );
    // assertTrue( env5.intersection( env2 ).equals( createEnvelope( 30, 30, 30, 30 ) ) );
    // assertTrue( env10.intersection( env9 ).equals( createEnvelope( 45, 10, 45, 45 ) ) );
    // assertTrue( env9.intersection( env10 ).equals( createEnvelope( 45, 10, 45, 45 ) ) );
    // }
}
