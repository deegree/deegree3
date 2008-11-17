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

package org.deegree.model.geometry.linearization;

import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.curvesegments.CurveSegment;
import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for the linearization of {@link CurveSegment}.
 * 
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author: elmasri$
 * 
 * @version $Revision: $, $Date: 9 May 2008 13:09:29$
 */
public class CurveLinearizer {

    private static final Logger LOG = LoggerFactory.getLogger( CurveLinearizer.class );

    private static double PI2 = Math.PI * 2;

    private GeometryFactory geomFac;

    /**
     * @param geomFac
     */
    public CurveLinearizer( GeometryFactory geomFac ) {
        this.geomFac = geomFac;
    }

    /**
     * Returns a linearized version (i.e. a {@link LineStringSegment}) of the given {@link CurveSegment}.
     * 
     * @param segment
     * @param crit
     *            determines the interpolation quality / number of interpolation points
     * @return
     */
    public LineStringSegment linearize( CurveSegment segment, LinearizationCriterion crit ) {
        LineStringSegment lineSegment = null;
        switch ( segment.getSegmentType() ) {
        case ARC: {
            break;
        }
        case ARC_STRING: {
            break;
        }
        case CIRCLE: {
            break;
        }
        case LINE_STRING_SEGMENT: {
            lineSegment = (LineStringSegment) segment;
            break;
        }
        case ARC_BY_BULGE:
        case ARC_BY_CENTER_POINT:
        case ARC_STRING_BY_BULGE:
        case BEZIER:
        case BSPLINE:
        case CIRCLE_BY_CENTER_POINT:
        case CLOTHOID:
        case CUBIC_SPLINE:
        case GEODESIC:
        case GEODESIC_STRING:
        case OFFSET_CURVE: {
            String msg = "Linearization of curve segment type '" + segment.getSegmentType().name()
                         + "' is not implemented yet.";
            throw new IllegalArgumentException( msg );
        }
        }
        return lineSegment;
    }

    /**
     * Takes in a three Points of an Arc and computes a linearized Arc (linestring).
     * 
     * @param p0
     * @param p1
     * @param p2
     * 
     * @param numberOfOpPoints
     *            the number of linearized Points to produce
     * @return An array of linearized Points
     */
    public static Point[] linearizeArc( Point p0, Point p1, Point p2, int numberOfOpPoints ) {

        int j = numberOfOpPoints - 1;
        if ( j < 1 ) {
            return null;
        }

        double[] positionsDouble = computeArc( p0.getX(), p0.getY(), p1.getX(), p1.getY(), p2.getX(), p2.getY() );
        if ( positionsDouble == null ) {

            Point[] Points = new Point[3];
            Points[0] = p0;
            Points[1] = p1;
            Points[2] = p2;
            return Points;
        }

        double d11 = computeArcOrientation( p0.getX(), p0.getY(), p1.getX(), p1.getY(), p2.getX(), p2.getY() );
        double d12 = positionsDouble[3];
        double d13 = positionsDouble[5];
        if ( d11 > 0 && d13 < d12 ) {
            d13 += PI2;
        } else if ( d11 < 0 && d13 > d12 ) {
            d13 -= PI2;
        }
        Point outPoints[] = new Point[numberOfOpPoints];
        for ( int k = 0; k <= j; k++ ) {
            double d15 = d12 + ( ( d13 - d12 ) * k ) / j;
            double xCoord = positionsDouble[0] + positionsDouble[2] * Math.cos( d15 );
            double yCoord = positionsDouble[1] + positionsDouble[2] * Math.sin( d15 );
            // outPoints[k] = GeometryFactory.createPoint( xCoord, yCoord );
        }

        // ensure numerical stability for start and end points
        // outPoints[0] = GeometryFactory.createPoint( p0.getX(), p0.getY() );
        // outPoints[outPoints.length - 1] = GeometryFactory.createPoint( p2.getX(), p2.getY() );

        return outPoints;
    }

    /**
     * Calculates a sequence of {@link Point}s on the arc of a circle.
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
     * @param numberOfPoints
     *            number of interpolation points, the returned array contains numberOfPoints+1 entries, to ensure that
     *            the first point is identical to the last
     * @return Point array with numberOfPoints+1 entries, first entry and last entry are identical to p0
     * @throws IllegalArgumentException
     *             if no order can be determined, because the points are identical or co-linear
     */
    public static Point[] linearizeCircle( Point p0, Point p1, Point p2, int numberOfPoints ) {

        Point[] positions = new Point[numberOfPoints + 1];

        Point center = findCircleCenter( p0, p1, p2 );
        double centerX = center.getX();
        double centerY = center.getY();
        double dx = p0.getX() - centerX;
        double dy = p0.getY() - centerY;
        double radius = Math.sqrt( dx * dx + dy * dy );

        double angleStep = 0.0;
        if ( isClockwise( p0, p1, p2 ) ) {
            angleStep = -Math.PI * 2 / numberOfPoints;
        } else {
            angleStep = Math.PI * 2 / numberOfPoints;
        }
        double angle = Math.atan2( dy, dx );

        for ( int i = 0; i < numberOfPoints; i++ ) {
            double x = centerX + Math.cos( angle ) * radius;
            double y = centerY + Math.sin( angle ) * radius;
            // positions[i] = GeometryFactory.createPoint( x, y );
            angle += angleStep;
        }
        positions[numberOfPoints] = positions[0];
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
    static final boolean isClockwise( Point p0, Point p1, Point p2 )
                            throws IllegalArgumentException {

        double res = ( p2.getX() - p0.getX() ) * ( ( p2.getY() + p0.getY() ) / 2 ) + ( p1.getX() - p2.getX() )
                     * ( ( p1.getY() + p2.getY() ) / 2 ) + ( p0.getX() - p1.getX() ) * ( ( p0.getY() + p1.getY() ) / 2 );
        if ( Math.abs( res ) < 1E-12 ) {
            throw new IllegalArgumentException( "Cannot evaluate isClockwise(). The three points are colinear." );
        }

        return res < 0.0 ? true : false;
    }

    /**
     * Computes an Arc from of three points.
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

        if ( d21 > -4.9E-323 && d21 < 4.9E-323 ) {
            return null;
        }

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
     * Transforms an array of Point to an array of double
     * 
     * @param positions
     * @return array of double
     */
    protected static double[] positionToDouble( Point[] positions ) {
        double[] ad = new double[positions.length * 2];
        for ( int i = 0; i < positions.length; i++ ) {
            ad[i] = positions[i].getX();
            ad[i + 1] = positions[i].getY();
        }
        return ad;
    }

    /**
     * Transforms an array of double to an array of Point
     * 
     * @param doubles
     * @return generated array of Point or null if doubles%2 != 0
     */
    protected static Point[] doubleToPoint( double[] doubles ) {
        // The number of doubles has to be even
        if ( doubles.length % 2 != 0 ) {
            return null;
        }
        Point[] positions = new Point[doubles.length / 2];
        for ( int i = 0; i < positions.length; i++ ) {
            // positions[i] = GeometryFactory.createPoint( doubles[i], doubles[i + 1] );
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
    public static Point findCircleCenter( Point p0, Point p1, Point p2 )
                            throws IllegalArgumentException {

        // Vector3d a = new Vector3d( p0.getAsPoint3d() );
        // Vector3d b = new Vector3d( p1.getAsPoint3d() );
        // Vector3d c = new Vector3d( p2.getAsPoint3d() );

        // if ( Double.isNaN( a.z ) ) {
        // a.z = 0;
        // }
        // if ( Double.isNaN( b.z ) ) {
        // b.z = 0;
        // }
        // if ( Double.isNaN( c.z ) ) {
        // c.z = 0;
        // }
        //
        // Vector3d ab = new Vector3d( a );
        // Vector3d ac = new Vector3d( a );
        // Vector3d bc = new Vector3d( b );
        // Vector3d ba = new Vector3d( b );
        // Vector3d ca = new Vector3d( c );
        // Vector3d cb = new Vector3d( c );
        //
        // ab.sub( b );
        // ac.sub( c );
        // bc.sub( c );
        // ba.sub( a );
        // ca.sub( a );
        // cb.sub( b );
        //
        // Vector3d cros = new Vector3d();
        //
        // cros.cross( ab, bc );
        // double crosSquare = 2 * cros.length() * cros.length();
        //
        // if ( LOG.isDebugEnabled() ) {
        // double radius = ( ab.length() * bc.length() * ca.length() ) / ( 2 * cros.length() );
        // LOG.debug( "Radius: " + radius );
        // }
        //
        // if ( Math.abs( crosSquare ) < 1E-11 ) {
        // throw new IllegalArgumentException( "The given points are co linear, no circum center can be calculated." );
        // }
        // a.scale( ( ( bc.length() * bc.length() ) * ab.dot( ac ) ) / crosSquare );
        // b.scale( ( ( ac.length() * ac.length() ) * ba.dot( bc ) ) / crosSquare );
        // c.scale( ( ( ab.length() * ab.length() ) * ca.dot( cb ) ) / crosSquare );
        //
        // Point3d circle = new Point3d( a );
        // circle.add( b );
        // circle.add( c );
        // return GeometryFactory.createPoint( circle );
        return null;
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
    public static double findAngle( Point p0, Point p1, Point centre ) {
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
    protected static Vector2d calcVector( Point p0, Point p1 ) {
        return new Vector2d( p1.getX() - p0.getX(), p1.getY() - p0.getY() );
    }
}
