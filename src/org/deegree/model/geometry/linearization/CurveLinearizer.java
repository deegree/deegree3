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

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Ring;
import org.deegree.model.geometry.primitive.curvesegments.Arc;
import org.deegree.model.geometry.primitive.curvesegments.Circle;
import org.deegree.model.geometry.primitive.curvesegments.CurveSegment;
import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;

/**
 * Provides methods for the linearization of {@link Curve}s and {@link CurveSegment}s.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: elmasri$
 * 
 * @version $Revision: $, $Date: 9 May 2008 13:09:29$
 */
public class CurveLinearizer {

    private GeometryFactory geomFac;

    /**
     * @param geomFac
     */
    public CurveLinearizer( GeometryFactory geomFac ) {
        this.geomFac = geomFac;
    }

    /**
     * Returns a linearized version of the given {@link Curve} geometry.
     * <p>
     * NOTE: This method respects the semantic difference between {@link Curve} and {@link Ring} geometries: if the
     * input is a {@link Ring}, a ring geometry will be returned.
     * 
     * @param curve
     * @param crit
     * @return linearized version of the input curve
     */
    public Curve linearize( Curve curve, LinearizationCriterion crit ) {
        Curve linearizedCurve = null;
        switch ( curve.getCurveType() ) {
        case LineString: {
            // both LineString and LinearRing are handled by this case
            linearizedCurve = curve;
            break;
        }
        default: {
            if ( curve instanceof Ring ) {
                Ring ring = (Ring) curve;
                List<Curve> curves = ring.getMembers();
                List<Curve> linearizedMembers = new ArrayList<Curve>( curves.size() );
                for ( Curve member : curves ) {
                    linearizedMembers.add( linearize( member, crit ) );
                }
                linearizedCurve = geomFac.createRing( ring.getId(), ring.getCoordinateSystem(), linearizedMembers );
            } else {
                List<CurveSegment> segments = curve.getCurveSegments();
                CurveSegment[] linearSegments = new CurveSegment[segments.size()];
                for ( int i = 0; i < linearSegments.length; i++ ) {
                    linearSegments[i] = linearize( segments.get( i ), crit );
                }
                linearizedCurve = geomFac.createCurve( curve.getId(), linearSegments, curve.getCoordinateSystem() );
            }
            break;
        }
        }
        return linearizedCurve;
    }

    /**
     * Returns a linearized version (i.e. a {@link LineStringSegment}) of the given {@link CurveSegment}.
     * 
     * @param segment
     * @param crit
     *            determines the interpolation quality / number of interpolation points
     * @return linearized version of the input segment
     */
    public LineStringSegment linearize( CurveSegment segment, LinearizationCriterion crit ) {
        LineStringSegment lineSegment = null;
        switch ( segment.getSegmentType() ) {
        case ARC: {
            lineSegment = linearizeArc( (Arc) segment, crit );
            break;
        }
        case CIRCLE: {
            lineSegment = linearizeCircle( (Circle) segment, crit );
            break;
        }
        case LINE_STRING_SEGMENT: {
            lineSegment = (LineStringSegment) segment;
            break;
        }
        case ARC_STRING:
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
     * Returns a linearized version (i.e. a {@link LineStringSegment}) of the given {@link Arc}.
     * 
     * @param arc
     * @param crit
     * @return linearized version of the input segment
     */
    public LineStringSegment linearizeArc( Arc arc, LinearizationCriterion crit ) {

        if ( !( crit instanceof NumPointsCriterion ) ) {
            String msg = "Handling of criterion '" + crit.getClass().getName() + "' is not implemented.";
            throw new IllegalArgumentException( msg );
        }

        int numPoints = ( (NumPointsCriterion) crit ).getNumberOfPoints();
        List<Point> interpolationPoints = new ArrayList<Point>( numPoints );
        Point p0 = arc.getPoint1();
        Point p1 = arc.getPoint2();
        Point p2 = arc.getPoint3();

        Point center = calcCircleCenter( p0, p1, p2 );

        double centerX = center.getX();
        double centerY = center.getY();
        double dx = p0.getX() - centerX;
        double dy = p0.getY() - centerY;
        double ex = p2.getX() - centerX;
        double ey = p2.getY() - centerY;
        double startAngle = Math.atan2( dy, dx );
        double endAngle = Math.atan2( ey, ex );
        double radius = Math.sqrt( dx * dx + dy * dy );

        double sweepAngle = Math.abs( startAngle - endAngle );

        double angleStep = 0.0;
        if ( isClockwise( p0, p1, p2 ) ) {
            angleStep = -sweepAngle / ( numPoints - 1 );
        } else {
            angleStep = sweepAngle / ( numPoints - 1 );
        }

        // use original circle start point (better for numerical stability)
        interpolationPoints.add( geomFac.createPoint( null, new double[] { p0.getX(), p0.getY() },
                                                      p0.getCoordinateSystem() ) );
        // calculate intermediate (=interpolated) points on arc
        for ( int i = 1; i < numPoints - 1; i++ ) {
            double angle = startAngle + i * angleStep;
            double x = centerX + Math.cos( angle ) * radius;
            double y = centerY + Math.sin( angle ) * radius;
            interpolationPoints.add( geomFac.createPoint( null, new double[] { x, y }, p0.getCoordinateSystem() ) );
        }

        // use original circle end point (better for numerical stability)
        interpolationPoints.add( geomFac.createPoint( null, new double[] { p2.getX(), p2.getY() },
                                                      p0.getCoordinateSystem() ) );
        return geomFac.createLineStringSegment( interpolationPoints );
    }

    /**
     * Returns a linearized version (i.e. a {@link LineStringSegment}) of the given {@link Circle}.
     * <p>
     * The circle is constructed from the input points: All three points belong to the arc of the circle. They must be
     * distinct and non-collinear. To form a complete circle, the arc is extended past the third control point until the
     * first control point is encountered.
     * 
     * @param circle
     * @param crit
     * @return linearized version of the input segment
     * @throws IllegalArgumentException
     *             if no order can be determined, because the points are identical or collinear
     */
    public LineStringSegment linearizeCircle( Circle circle, LinearizationCriterion crit ) {

        if ( !( crit instanceof NumPointsCriterion ) ) {
            String msg = "Handling of criterion '" + crit.getClass().getName() + "' is not implemented.";
            throw new IllegalArgumentException( msg );
        }

        int numPoints = ( (NumPointsCriterion) crit ).getNumberOfPoints();
        List<Point> interpolationPoints = new ArrayList<Point>( numPoints );
        Point p0 = circle.getPoint1();
        Point p1 = circle.getPoint2();
        Point p2 = circle.getPoint3();

        Point center = calcCircleCenter( p0, p1, p2 );

        double centerX = center.getX();
        double centerY = center.getY();
        double dx = p0.getX() - centerX;
        double dy = p0.getY() - centerY;
        double radius = Math.sqrt( dx * dx + dy * dy );

        double angleStep = 0.0;
        if ( isClockwise( p0, p1, p2 ) ) {
            angleStep = -Math.PI * 2 / ( numPoints - 1 );
        } else {
            angleStep = Math.PI * 2 / ( numPoints - 1 );
        }
        double startAngle = Math.atan2( dy, dx );

        // ensure numerical stability for start point (= use original circle start point)
        interpolationPoints.add( geomFac.createPoint( null, new double[] { p0.getX(), p0.getY() },
                                                      p0.getCoordinateSystem() ) );
        // calculate intermediate (=interpolated) points on arc
        for ( int i = 1; i < numPoints - 1; i++ ) {
            double angle = startAngle + i * angleStep;
            double x = centerX + Math.cos( angle ) * radius;
            double y = centerY + Math.sin( angle ) * radius;
            interpolationPoints.add( geomFac.createPoint( null, new double[] { x, y }, p0.getCoordinateSystem() ) );
        }

        // ensure numerical stability for end point (= use original circle start point)
        interpolationPoints.add( geomFac.createPoint( null, new double[] { p0.getX(), p0.getY() },
                                                      p0.getCoordinateSystem() ) );
        return geomFac.createLineStringSegment( interpolationPoints );
    }

    /**
     * Finds the center of a circle/arc that is specified by three points that lie on the circle's boundary.
     * <p>
     * Credits go to <a href="http://en.wikipedia.org/wiki/Circumradius#Coordinates_of_circumcenter">wikipedia</a>.
     * 
     * @param p0
     *            first point
     * @param p1
     *            second point
     * @param p2
     *            third point
     * @return center of the circle
     * @throws IllegalArgumentException
     *             if the points are collinear, e.g. on a single line
     */
    public Point calcCircleCenter( Point p0, Point p1, Point p2 )
                            throws IllegalArgumentException {

        Vector3d a = new Vector3d( p0.getX(), p0.getY(), p0.getZ() );
        Vector3d b = new Vector3d( p1.getX(), p1.getY(), p1.getZ() );
        Vector3d c = new Vector3d( p2.getX(), p2.getY(), p2.getZ() );

        if ( Double.isNaN( a.z ) ) {
            a.z = 0.0;
        }
        if ( Double.isNaN( b.z ) ) {
            b.z = 0.0;
        }
        if ( Double.isNaN( c.z ) ) {
            c.z = 0.0;
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

        if ( Math.abs( crosSquare ) < 1E-11 ) {
            throw new IllegalArgumentException( "The given points are collinear, no circum center can be calculated." );
        }

        a.scale( ( ( bc.length() * bc.length() ) * ab.dot( ac ) ) / crosSquare );
        b.scale( ( ( ac.length() * ac.length() ) * ba.dot( bc ) ) / crosSquare );
        c.scale( ( ( ab.length() * ab.length() ) * ca.dot( cb ) ) / crosSquare );

        Point3d circle = new Point3d( a );
        circle.add( b );
        circle.add( c );
        return geomFac.createPoint( null, new double[] { circle.getX(), circle.getY() }, p0.getCoordinateSystem() );
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
     *             if no order can be determined, because the points are identical or collinear
     */
    boolean isClockwise( Point p0, Point p1, Point p2 )
                            throws IllegalArgumentException {

        double res = ( p2.getX() - p0.getX() ) * ( ( p2.getY() + p0.getY() ) / 2 ) + ( p1.getX() - p2.getX() )
                     * ( ( p1.getY() + p2.getY() ) / 2 ) + ( p0.getX() - p1.getX() ) * ( ( p0.getY() + p1.getY() ) / 2 );
        if ( Math.abs( res ) < 1E-12 ) {
            throw new IllegalArgumentException( "Cannot evaluate isClockwise(). The three points are collinear." );
        }
        return res < 0.0 ? true : false;
    }
}
