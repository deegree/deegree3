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
package org.deegree.model.spatialschema;

import junit.framework.TestCase;

/**
 * Unit tests for class {@link LinearizationUtil}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class LinearizationUtilTest extends TestCase {

    private double getDistance( Position p0, Position p1 ) {
        double dx = p1.getX() - p0.getX();
        double dy = p1.getY() - p0.getY();
        return Math.sqrt( dx * dx + dy * dy );
    }

    private void arePointsOnCircle( Position center, Position p0, Position p1, Position p2 ) {
        double dp0 = getDistance( center, p0 );
        double dp1 = getDistance( center, p1 );
        double dp2 = getDistance( center, p2 );
        assertEquals( dp0, dp1, 1E-6f );
        assertEquals( dp1, dp2, 1E-6f );
    }

    private void testArcLinearization( Position p0, Position p1, Position p2, int numPositions ) {

        Position[] positions = LinearizationUtil.linearizeArc( p0, p1, p2, numPositions );
        if ( positions.length == 3 ) {
            // no interpolation performed, points are collinear
            assertEquals( p0, positions[0] );
            assertEquals( p1, positions[1] );
            assertEquals( p2, positions[2] );
        } else {
            Position center = LinearizationUtil.findCircleCenter( p0, p1, p2 );
            double radius = getDistance( center, p0 );
            for ( int i = 0; i < positions.length; i++ ) {
                double dist = getDistance( center, positions[0] );
                assertEquals( radius, dist, 1E-9 );
            }
        }
    }

    private void testCircleLinearization( Position p0, Position p1, Position p2, int numPositions ) {

        Position[] positions = LinearizationUtil.linearizeCircle( p0, p1, p2, numPositions );

        Position center = LinearizationUtil.findCircleCenter( p0, p1, p2 );
        double radius = getDistance( center, p0 );

        for ( int i = 0; i < positions.length; i++ ) {
            double dist = getDistance( center, positions[i] );
            assertEquals( radius, dist, 1E-4 );
        }
    }

    /**
     * Tests if {@link LinearizationUtil#isClockwise(Position, Position, Position)} determines the correct point order.
     */
    public void testIsClockwise() {
        Position p0 = GeometryFactory.createPosition( -2, 0 );
        Position p1 = GeometryFactory.createPosition( 0, 2 );
        Position p2 = GeometryFactory.createPosition( 2, 0 );
        assertTrue( LinearizationUtil.isClockwise( p0, p1, p2 ) );
        assertFalse( LinearizationUtil.isClockwise( p0, p2, p1 ) );

        p0 = GeometryFactory.createPosition( -2, 0 );
        p1 = GeometryFactory.createPosition( 0, -2 );
        p2 = GeometryFactory.createPosition( 2, 0 );
        assertFalse( LinearizationUtil.isClockwise( p0, p1, p2 ) );
    }

    /**
     * Tests if {@link LinearizationUtil#findCircleCenter(Position, Position, Position)} finds the correct midpoint.
     */
    public void testFindCircleCenter() {
        Position p0 = GeometryFactory.createPosition( 8f, -1f );
        Position p1 = GeometryFactory.createPosition( 3f, 1.6f );
        Position p2 = GeometryFactory.createPosition( -110f, 16.77777f );
        Position center = LinearizationUtil.findCircleCenter( p0, p1, p2 );
        arePointsOnCircle( center, p0, p1, p2 );
    }

    /**
     * Tests if {@link LinearizationUtil#linearizeCircle(Position, Position, Position, int)} produces sequences of
     * positions that coincide with the circle arc.
     */
    public void testLinearizeCircle() {
        testCircleLinearization( GeometryFactory.createPosition( 0f, 2f ), GeometryFactory.createPosition( 2f, 0f ),
                                 GeometryFactory.createPosition( -2f, 0f ), 10 );
        testCircleLinearization( GeometryFactory.createPosition( 0f, 2f ), GeometryFactory.createPosition( 2f, 0f ),
                                 GeometryFactory.createPosition( -2f, 0f ), 1000 );
        testCircleLinearization( GeometryFactory.createPosition( 8f, -1f ), GeometryFactory.createPosition( 3f, 1.6f ),
                                 GeometryFactory.createPosition( -110f, 16.77777f ), 1000 );
        testCircleLinearization( GeometryFactory.createPosition( 8f, -1f ), GeometryFactory.createPosition( 3f, 1.6f ),
                                 GeometryFactory.createPosition( -110f, 16.77777f ), 10 );
    }

    /**
     * Tests if start- and end-points of the linearized line strings produced by
     * {@link LinearizationUtil#linearizeArc(Position, Position, Position)} are identical with the input positions.
     */
    public void testLinearizeArc() {
        Position p0 = GeometryFactory.createPosition( 118075.83, 407620.65 );
        Position p1 = GeometryFactory.createPosition( 118082.289, 407621.62 );
        Position p2 = GeometryFactory.createPosition( 118087.099, 407626.038 );
        testArcLinearization( p0, p1, p2, 150 );

        p0 = GeometryFactory.createPosition( 230481.753, 486923.349 );
        p1 = GeometryFactory.createPosition( 230481.806, 486923.35 );
        p2 = GeometryFactory.createPosition( 230481.859, 486923.351 );
        testArcLinearization( p0, p1, p2, 150 );

        p0 = GeometryFactory.createPosition( -230481.753, -486923.349 );
        p1 = GeometryFactory.createPosition( -230481.806, -486923.35 );
        p2 = GeometryFactory.createPosition( -230481.859, -486923.351 );
        testArcLinearization( p0, p1, p2, 150 );

        p0 = GeometryFactory.createPosition( 0.0, 0.0 );
        p1 = GeometryFactory.createPosition( 1.0, 1.0 );
        p2 = GeometryFactory.createPosition( 2.0, 2.0 );
        testArcLinearization( p0, p1, p2, 150 );

        p0 = GeometryFactory.createPosition( 10, 20 );
        p1 = GeometryFactory.createPosition( -5, 15 );
        p2 = GeometryFactory.createPosition( -10, -10 );
        testArcLinearization( p0, p1, p2, 10 );
    }
}
