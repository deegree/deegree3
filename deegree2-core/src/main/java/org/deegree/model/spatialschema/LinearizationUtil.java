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

import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 * Utility class for the linearization of arcs and circles.
 * 
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author: elmasri$
 * 
 * @version $Revision$, $Date: 9 May 2008 13:09:29$
 */
public class LinearizationUtil {

    private static ILogger LOG = LoggerFactory.getLogger( LinearizationUtil.class );

    private static double PI2 = Math.PI * 2;

    private static double EPSILON = 1E-11;

    /**
     * Takes in a three Position of an arc and computes a linearized Arc (linestring) using 150 positions/vertices
     * 
     * @param p0
     * @param p1
     * @param p2
     * @return An array of linearized Positions
     */
    public static Position[] linearizeArc( Position p0, Position p1, Position p2 ) {
        return linearizeArc( p0, p1, p2, 150 );
    }

    /**
     * Takes in a three Positions of an Arc and computes a linearized Arc (linestring).
     * 
     * @param p0
     * @param p1
     * @param p2
     * 
     * @param numberOfOpPositions
     *            the number of linearized Positions to produce
     * @return An array of linearized Positions
     */
    public static Position[] linearizeArc( Position p0, Position p1, Position p2, int numberOfOpPositions ) {

        // shift the points down (to reduce the occurrence of floating point errors), independently on the x and y axes
        double shiftx = findShiftX( p0, p1, p2 );
        double shifty = findShiftY( p0, p1, p2 );
        // if the points are already shifted, this does no harm!
        Position p0Shifted = new PositionImpl( p0.getX() - shiftx, p0.getY() - shifty );
        Position p1Shifted = new PositionImpl( p1.getX() - shiftx, p1.getY() - shifty );
        Position p2Shifted = new PositionImpl( p2.getX() - shiftx, p2.getY() - shifty );

        if ( areCollinear( p0Shifted, p1Shifted, p2Shifted ) ) {
            Position[] Positions = new Position[3];
            Positions[0] = p0;
            Positions[1] = p1;
            Positions[2] = p2;
            return Positions;
        }

        int j = numberOfOpPositions - 1;
        if ( j < 1 ) {
            return null;
        }

        double[] positionsDouble = computeArc( p0Shifted.getX(), p0Shifted.getY(), p1Shifted.getX(), p1Shifted.getY(),
                                               p2Shifted.getX(), p2Shifted.getY() );
        double d11 = computeArcOrientation( p0Shifted.getX(), p0Shifted.getY(), p1Shifted.getX(), p1Shifted.getY(),
                                            p2Shifted.getX(), p2Shifted.getY() );
        double d12 = positionsDouble[3];
        double d13 = positionsDouble[5];
        if ( d11 > 0 && d13 < d12 ) {
            d13 += PI2;
        } else if ( d11 < 0 && d13 > d12 ) {
            d13 -= PI2;
        }
        Position outPositions[] = new Position[numberOfOpPositions];
        for ( int k = 0; k <= j; k++ ) {
            double d15 = d12 + ( ( d13 - d12 ) * k ) / j;
            double xCoord = positionsDouble[0] + positionsDouble[2] * Math.cos( d15 );
            double yCoord = positionsDouble[1] + positionsDouble[2] * Math.sin( d15 );
            outPositions[k] = GeometryFactory.createPosition( xCoord, yCoord );
        }

        // ensure numerical stability for start and end points
        outPositions[0] = GeometryFactory.createPosition( p0Shifted.getX(), p0Shifted.getY() );
        outPositions[outPositions.length - 1] = GeometryFactory.createPosition( p2Shifted.getX(), p2Shifted.getY() );

        // shift the points back up
        for ( int i = 0; i < outPositions.length; i++ ) {
            outPositions[i] = new PositionImpl( outPositions[i].getX() + shiftx, outPositions[i].getY() + shifty );
        }

        return outPositions;
    }

    private static double findShiftY( Position p0, Position p1, Position p2 ) {
        double miny = p0.getY();
        if ( p1.getY() < miny ) {
            miny = p1.getY();
        }
        if ( p2.getY() < miny ) {
            miny = p2.getY();
        }

        double maxy = p0.getY();
        if ( p1.getX() > maxy ) {
            maxy = p1.getY();
        }
        if ( p2.getY() > maxy ) {
            maxy = p2.getY();
        }
        return ( miny + maxy ) / 2;
    }

    private static double findShiftX( Position p0, Position p1, Position p2 ) {
        double minx = p0.getX();
        if ( p1.getX() < minx ) {
            minx = p1.getX();
        }
        if ( p2.getX() < minx ) {
            minx = p2.getX();
        }

        double maxx = p0.getX();
        if ( p1.getX() > maxx ) {
            maxx = p1.getX();
        }
        if ( p2.getX() > maxx ) {
            maxx = p2.getX();
        }
        return ( minx + maxx ) / 2;
    }

    /**
     * Calculates a sequence of {@link Position}s on the arc of a circle.
     * <p>
     * The circle is constructed from the input points: All three points belong to the arc of the circle. They must be
     * distinct and non-colinear. To form a complete circle, the arc is extended past the third control point until the
     * first control point is encountered.
     * 
     * @param p0
     *            start point on the arc of the circle
     * @param p1
     *            second point on the arc of the circle
     * @param p2
     *            third point on the arc of the circle
     * @param numberOfPositions
     *            number of interpolation points, the returned array contains numberOfPositions+1 entries, to ensure
     *            that the first point is identical to the last
     * @return Position array with numberOfPositions+1 entries, first entry and last entry are identical to p0
     * @throws IllegalArgumentException
     *             if no order can be determined, because the points are identical or co-linear
     */
    public static Position[] linearizeCircle( Position p0, Position p1, Position p2, int numberOfPositions ) {

        // shift the points down (to reduce the occurrence of floating point errors), independently on the x and y axes
        double shiftx = findShiftX( p0, p1, p2 );
        double shifty = findShiftY( p0, p1, p2 );
        // if the points are already shifted, this does no harm!
        Position p0Shifted = new PositionImpl( p0.getX() - shiftx, p0.getY() - shifty );
        Position p1Shifted = new PositionImpl( p1.getX() - shiftx, p1.getY() - shifty );
        Position p2Shifted = new PositionImpl( p2.getX() - shiftx, p2.getY() - shifty );

        Position[] positions = new Position[numberOfPositions + 1];

        Position center = findCircleCenter( p0Shifted, p1Shifted, p2Shifted );

        double centerX = center.getX();
        double centerY = center.getY();
        double dx = p0Shifted.getX() - centerX;
        double dy = p0Shifted.getY() - centerY;
        double radius = Math.sqrt( dx * dx + dy * dy );

        double angleStep = 0.0;
        if ( isClockwise( p0Shifted, p1Shifted, p2Shifted ) ) {
            angleStep = -Math.PI * 2 / numberOfPositions;
        } else {
            angleStep = Math.PI * 2 / numberOfPositions;
        }
        double angle = Math.atan2( dy, dx );

        for ( int i = 0; i < numberOfPositions; i++ ) {
            double x = centerX + Math.cos( angle ) * radius;
            double y = centerY + Math.sin( angle ) * radius;
            positions[i] = GeometryFactory.createPosition( x, y );
            angle += angleStep;
        }
        positions[numberOfPositions] = positions[0];

        // shift the points back up
        for ( int i = 0; i <= numberOfPositions; i++ ) {
            positions[i] = new PositionImpl( positions[i].getX() + shiftx, positions[i].getY() + shifty );
        }

        return positions;
    }

    /**
     * Returns whether the order of the given three points is clockwise or counterclockwise. Uses the (signed) area of a
     * planar triangle to get to know about the order of the points.
     * 
     * @param p0
     *            first point
     * @param p1
     *            second point
     * @param p2
     *            third point
     * @return true, if order is clockwise, otherwise false
     * @throws IllegalArgumentException
     *             if no order can be determined, because the points are identical or co-linear
     */
    static final boolean isClockwise( Position p0, Position p1, Position p2 )
                            throws IllegalArgumentException {

        if ( areCollinear( p0, p1, p2 ) ) {
            throw new IllegalArgumentException( "Cannot evaluate isClockwise(). The three points are colinear." );
        }
        double res = ( p2.getX() - p0.getX() ) * ( ( p2.getY() + p0.getY() ) / 2 ) + ( p1.getX() - p2.getX() )
                     * ( ( p1.getY() + p2.getY() ) / 2 ) + ( p0.getX() - p1.getX() ) * ( ( p0.getY() + p1.getY() ) / 2 );
        return res < 0.0 ? true : false;
    }

    /**
     * Tests if the given three points are collinear.
     * <p>
     * NOTE: Only this method should be used throughout the whole linearization process for testing collinearity to
     * avoid inconsistent results (the necessary EPSILON would differ).
     * 
     * @param p0
     * @param p1
     * @param p2
     * @return true if the points are collinear, false otherwise
     */
    public static boolean areCollinear( Position p0, Position p1, Position p2 ) {
        double res = ( p2.getX() - p0.getX() ) * ( ( p2.getY() + p0.getY() ) / 2 ) + ( p1.getX() - p2.getX() )
                     * ( ( p1.getY() + p2.getY() ) / 2 ) + ( p0.getX() - p1.getX() ) * ( ( p0.getY() + p1.getY() ) / 2 );
        return Math.abs( res ) < EPSILON;
    }

    /**
     * Computes an Arc from of three points (wich must not be collinear).
     * 
     * @return array of points representing the computed arc
     */
    private static final double[] computeArc( double d1, double d2, double d3, double d4, double d5, double d6 ) {

        double d13 = d1 - d3;
        double d14 = d3 - d5;
        double d15 = d4 - d6;
        double d16 = d2 - d4;
        double d17 = d1 + d3;
        double d18 = d3 + d5;
        double d19 = d2 + d4;
        double d20 = d4 + d6;
        double d21 = d13 * d15 - d14 * d16;

        double d7 = ( ( d15 * d17 * d13 - d16 * d18 * d14 ) + d16 * d15 * ( d16 + d15 ) ) / d21;
        double d8;
        if ( Math.abs( d4 - d6 ) < 5E-008D ) {
            d8 = ( d19 * d16 + ( d17 - d7 ) * d13 ) / d16;
        } else {
            d8 = ( d20 * d15 + ( d18 - d7 ) * d14 ) / d15;
        }
        d7 *= 0.5;
        d8 *= 0.5;
        double d9 = Math.sqrt( ( d7 - d1 ) * ( d7 - d1 ) + ( d8 - d2 ) * ( d8 - d2 ) );
        double d10 = Math.atan2( d2 - d8, d1 - d7 );
        if ( d10 < 0 ) {
            d10 += PI2;
        }
        double d11 = Math.atan2( d4 - d8, d3 - d7 );
        if ( d11 < 0 ) {
            d11 += PI2;
        }
        double d12 = Math.atan2( d6 - d8, d5 - d7 );
        if ( d12 < 0 ) {
            d12 += PI2;
        }

        double ad[] = new double[6];
        ad[0] = d7;
        ad[1] = d8;
        ad[2] = d9;
        ad[3] = d10;
        ad[4] = d11;
        ad[5] = d12;
        return ad;

    }

    private static final double computeArcOrientation( double d, double d1, double d2, double d3, double d4, double d5 ) {
        return ( d * d3 + d2 * d5 + d4 * d1 ) - ( d4 * d3 + d2 * d1 + d * d5 );
    }

    /**
     * Transforms an array of Position to an array of double
     * 
     * @param positions
     * @return array of double
     */
    protected static double[] positionToDouble( Position[] positions ) {
        double[] ad = new double[positions.length * 2];
        for ( int i = 0; i < positions.length; i++ ) {
            ad[i] = positions[i].getX();
            ad[i + 1] = positions[i].getY();
        }
        return ad;
    }

    /**
     * Transforms an array of double to an array of Position
     * 
     * @param doubles
     * @return generated array of Position or null if doubles%2 != 0
     */
    protected static Position[] doubleToPosition( double[] doubles ) {
        // The number of doubles has to be even
        if ( doubles.length % 2 != 0 ) {
            return null;
        }
        Position[] positions = new Position[doubles.length / 2];
        for ( int i = 0; i < positions.length; i++ ) {
            positions[i] = GeometryFactory.createPosition( doubles[i], doubles[i + 1] );
        }
        return positions;
    }

    /**
     * Finds the center of a cirlce/arc using three points that lie on the circle. Thanks to wikipedia:
     * http://en.wikipedia.org/wiki/Circumradius#Coordinates_of_circumcenter.
     * 
     * @param p0
     * @param p1
     * @param p2
     * @return center of the circle
     * @throws IllegalArgumentException
     *             if the points are co linear, e.g. on a line.
     */
    static Position findCircleCenter( Position p0, Position p1, Position p2 )
                            throws IllegalArgumentException {

        // shift the points down (to reduce the occurrence of floating point errors), independently on the x and y axes
        double shiftx = findShiftX( p0, p1, p2 );
        double shifty = findShiftY( p0, p1, p2 );
        // if the points are already shifted, this does no harm!
        Position p0Shifted = new PositionImpl( p0.getX() - shiftx, p0.getY() - shifty );
        Position p1Shifted = new PositionImpl( p1.getX() - shiftx, p1.getY() - shifty );
        Position p2Shifted = new PositionImpl( p2.getX() - shiftx, p2.getY() - shifty );

        if ( areCollinear( p0Shifted, p1Shifted, p2Shifted ) ) {
            throw new IllegalArgumentException( "The given points are co linear, no circum center can be calculated." );
        }

        Vector3d a = new Vector3d( p0Shifted.getAsPoint3d() );
        Vector3d b = new Vector3d( p1Shifted.getAsPoint3d() );
        Vector3d c = new Vector3d( p2Shifted.getAsPoint3d() );

        if ( Double.isNaN( a.z ) ) {
            a.z = 0;
        }
        if ( Double.isNaN( b.z ) ) {
            b.z = 0;
        }
        if ( Double.isNaN( c.z ) ) {
            c.z = 0;
        }

        Vector3d ab = new Vector3d( a );
        Vector3d ac = new Vector3d( a );
        Vector3d bc = new Vector3d( b );
        Vector3d ba = new Vector3d( b );
        Vector3d ca = new Vector3d( c );
        Vector3d cb = new Vector3d( c );

        ab.sub( b );
        ac.sub( c );
        bc.sub( c );
        ba.sub( a );
        ca.sub( a );
        cb.sub( b );

        Vector3d cros = new Vector3d();
        cros.cross( ab, bc );
        double crosSquare = 2 * cros.length() * cros.length();

        if ( LOG.isDebug() ) {
            double radius = ( ab.length() * bc.length() * ca.length() ) / ( 2 * cros.length() );
            LOG.logDebug( "Radius: " + radius );
        }

        a.scale( ( ( bc.length() * bc.length() ) * ab.dot( ac ) ) / crosSquare );
        b.scale( ( ( ac.length() * ac.length() ) * ba.dot( bc ) ) / crosSquare );
        c.scale( ( ( ab.length() * ab.length() ) * ca.dot( cb ) ) / crosSquare );

        Point3d circle = new Point3d( a );
        circle.add( b );
        circle.add( c );

        // shift the center circle back up
        circle.x += shiftx;
        circle.y += shifty;

        return GeometryFactory.createPosition( circle );
    }

    /**
     * Finds the angle between three points, p0,p1 and a center, where the angle to be measured. In other words, its the
     * angle between the segments p0-center and p1-center.
     * 
     * @param p0
     * @param p1
     * @param centre
     * @return calculated angle in degree
     */
    private static double findAngle( Position p0, Position p1, Position centre ) {
        Vector2d vec1 = calcVector( centre, p0 );
        Vector2d vec2 = calcVector( centre, p1 );
        double cosTheta = ( vec1.dot( vec2 ) ) / ( vec1.length() * vec2.length() );
        int round = 1000;
        // Using the 1000th decimal to round the cosTheta.
        cosTheta = Math.ceil( cosTheta * round ) / round;
        // convert from radian to degree
        return Math.acos( cosTheta ) / Math.PI * 180;

    }

    /**
     * Finds the vector between two points. which is the difference between p1 and p0 and the direction is from p0 to
     * p1.
     * 
     * @param p0
     * @param p1
     * @return calculated vector
     */
    protected static Vector2d calcVector( Position p0, Position p1 ) {
        return new Vector2d( p1.getX() - p0.getX(), p1.getY() - p0.getY() );
    }
}
