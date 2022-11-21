//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

package org.deegree.geometry.linearization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.DecompositionSolver;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.SingularMatrixException;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.segments.Arc;
import org.deegree.geometry.primitive.segments.ArcString;
import org.deegree.geometry.primitive.segments.Circle;
import org.deegree.geometry.primitive.segments.CubicSpline;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.primitive.segments.GeodesicString;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.standard.curvesegments.DefaultLineStringSegment;
import org.deegree.geometry.standard.points.PointsArray;
import org.deegree.geometry.standard.points.PointsList;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for the linearization of {@link Curve}s and {@link CurveSegment}s.
 * <p>
 * Currently, the following {@link CurveSegment} variants are handled:
 * <ul>
 * <li>{@link Arc}</li>
 * <li>{@link ArcString}</li>
 * <li>{@link Circle}</li>
 * <li>{@link CubicSpline}</li>
 * <li>{@link GeodesicString}</li>
 * <li>{@link LineStringSegment}</li>
 * </ul>
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CurveLinearizer {

    private static Logger LOG = LoggerFactory.getLogger( CurveLinearizer.class );

    private static final double EPSILON = 1E-6;

    private final GeometryFactory geomFac;

    private final static double TWO_PI = Math.PI * 2;

    /**
     * Creates a new {@link CurveLinearizer} instance.
     * 
     * @param geomFac
     *            geometry factory to be used for creating {@link LineString}s and {@link LineStringSegments}, must not
     *            be <code>null</code>
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
     *            curve to be linearized, must not be <code>null</code>
     * @param crit
     *            linearization criterion, must not be <code>null</code>
     * @return linearized version of the input curve, never <code>null</code>
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
                linearizedCurve = geomFac.createCurve( curve.getId(), curve.getCoordinateSystem(), linearSegments );
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
     *            segment to be linearized, must not be <code>null</code>
     * @param crit
     *            determines the interpolation quality / number of interpolation points, must not be <code>null</code>
     * @return linearized version of the input segment, never <code>null</code>
     */
    public LineStringSegment linearize( CurveSegment segment, LinearizationCriterion crit ) {
        LineStringSegment lineSegment = null;
        switch ( segment.getSegmentType() ) {
        case ARC:
        case CIRCLE:
            lineSegment = linearizeArc( (Arc) segment, crit );
            break;
        case LINE_STRING_SEGMENT: {
            lineSegment = (LineStringSegment) segment;
            break;
        }
        case CUBIC_SPLINE: {
            lineSegment = linearizeCubicSpline( (CubicSpline) segment, crit );
            break;
        }
        case ARC_STRING: {
            lineSegment = linearizeArcString( (ArcString) segment, crit );
            break;
        }
        case GEODESIC_STRING: {
            lineSegment = linearizeGeodesicString( (GeodesicString) segment, crit );
            break;
        }
        case ARC_BY_BULGE:
        case ARC_BY_CENTER_POINT:
        case ARC_STRING_BY_BULGE:
        case BEZIER:
        case BSPLINE:
        case CIRCLE_BY_CENTER_POINT:
        case CLOTHOID:
        case GEODESIC:
        case OFFSET_CURVE: {
            String msg = "Linearization of curve segment type '" + segment.getSegmentType().name()
                         + "' is not implemented yet.";
            throw new IllegalArgumentException( msg );
        }
        }
        return lineSegment;
    }

    private LineStringSegment linearizeGeodesicString( GeodesicString segment, LinearizationCriterion crit ) {
        return new DefaultLineStringSegment( segment.getControlPoints() );
    }

    /**
     * Returns a linearized version (i.e. a {@link LineStringSegment}) of the given {@link Arc}.
     * <p>
     * If the three control points <code>p0</code>, <code>p1</code> and <code>p2</code> of the arc are collinear, i.e.
     * on a straight line, the behaviour depends on the type of {@link Arc}:
     * <ul>
     * <li>Generic {@link Arc}: returns the linear segment <code>(p0, p2)</code></li>
     * <li>{@link Circle}: returns the linear segment <code>(p0, p1, p0)</code></li>
     * </ul>
     * 
     * @param arc
     *            segment to be linearized, must not be <code>null</code>
     * @param crit
     *            determines the interpolation quality / number of interpolation points, must not be <code>null</code>
     * @return linearized version of the input segment, never <code>null</code>
     */
    public LineStringSegment linearizeArc( Arc arc, LinearizationCriterion crit ) {

        LineStringSegment lineSegment = null;

        if ( areCollinear( arc.getPoint1(), arc.getPoint2(), arc.getPoint3() ) ) {
            // if the points are already on a line we don't need to (and must not) apply any linearization algorithm
            Points points = null;
            if ( arc instanceof Circle ) {
                points = new PointsList(
                                         Arrays.asList( new Point[] { arc.getPoint1(), arc.getPoint2(), arc.getPoint1() } ) );
            } else {
                points = new PointsList( Arrays.asList( new Point[] { arc.getPoint1(), arc.getPoint3() } ) );
            }
            lineSegment = geomFac.createLineStringSegment( points );

        } else if ( crit instanceof NumPointsCriterion ) {
            int numPoints = ( (NumPointsCriterion) crit ).getNumberOfPoints();
            lineSegment = geomFac.createLineStringSegment( interpolate( arc.getPoint1(), arc.getPoint2(),
                                                                        arc.getPoint3(), numPoints,
                                                                        arc instanceof Circle ) );
        } else if ( crit instanceof MaxErrorCriterion ) {

            double error = ( (MaxErrorCriterion) crit ).getMaxError();
            int numPoints = calcNumPoints( arc.getPoint1(), arc.getPoint2(), arc.getPoint3(), arc instanceof Circle,
                                           error );
            int maxNumPoints = ( (MaxErrorCriterion) crit ).getMaxNumPoints();
            if ( maxNumPoints > 0 && maxNumPoints < numPoints ) {
                numPoints = maxNumPoints;
            }
            LOG.debug( "Using " + numPoints + " for segment linearization." );
            lineSegment = geomFac.createLineStringSegment( interpolate( arc.getPoint1(), arc.getPoint2(),
                                                                        arc.getPoint3(), numPoints,
                                                                        arc instanceof Circle ) );
        } else {
            String msg = "Handling of criterion '" + crit.getClass().getName() + "' is not implemented yet.";
            throw new IllegalArgumentException( msg );
        }
        return lineSegment;
    }

    /**
     * Returns a linearized version (i.e. a {@link LineStringSegment}) of the given {@link ArcString}.
     * <p>
     * If one of the arc elements is collinear, it will be added as a straight segment.
     * </p>
     * 
     * @param arcString
     *            curve segment to be linearized, must not be <code>null</code>
     * @param crit
     *            determines the interpolation quality / number of interpolation points, must not be <code>null</code>
     * @return linearized version of the input string, never <code>null</code>
     */
    public LineStringSegment linearizeArcString( ArcString arcString, LinearizationCriterion crit ) {

        List<Point> points = new ArrayList<Point>();

        Points srcpnts = arcString.getControlPoints();
        Point a = null;
        Point b = null;
        Point c = null;
        Points pnts = null;

        // insert first point
        if ( srcpnts.size() > 0 ) {
            points.add( srcpnts.get( 0 ) );
        }

        for ( int i = 0, j = ( srcpnts.size() - 2 ); i < j; i += 2 ) {
            a = srcpnts.get( i );
            b = srcpnts.get( i + 1 );
            c = srcpnts.get( i + 2 );
            if ( areCollinear( a, b, c ) ) {
                pnts = new PointsArray( a, b, c );
            } else if ( crit instanceof NumPointsCriterion ) {
                int numPoints = ( (NumPointsCriterion) crit ).getNumberOfPoints();
                pnts = interpolate( a, b, c, numPoints, false );
            } else if ( crit instanceof MaxErrorCriterion ) {
                double error = ( (MaxErrorCriterion) crit ).getMaxError();
                int numPoints = calcNumPoints( a, b, c, false, error );
                int maxNumPoints = ( (MaxErrorCriterion) crit ).getMaxNumPoints();
                if ( maxNumPoints > 0 && maxNumPoints < numPoints ) {
                    numPoints = maxNumPoints;
                }
                LOG.debug( "Using " + numPoints + " for segment linearization." );
                pnts = interpolate( a, b, c, numPoints, false );
            } else {
                String msg = "Handling of criterion '" + crit.getClass().getName() + "' is not implemented yet.";
                throw new IllegalArgumentException( msg );
            }

            // add point 2..n
            for ( int m = 1, n = pnts.size(); m < n; m++ ) {
                points.add( pnts.get( m ) );
            }
        }

        return geomFac.createLineStringSegment( new PointsArray( points.toArray( new Point[points.size()] ) ) );
    }

    /**
     * Returns a linearized version (i.e. a {@link LineStringSegment}) of the given {@link CubicSpline}.
     * <p>
     * A cubic spline consists of n polynomials of degree 3: S<sub>j</sub>(x) = a<sub>j</sub> +
     * b<sub>j</sub>*(x-x<sub>j</sub>) + c<sub>j</sub>*(x-x<sub>j</sub>)<sup>2</sup> +
     * d<sub>j</sub>*(x-x<sub>j</sub>)<sup>3</sup>; that acts upon the interval [x<sub>j</sub>,x<sub>j+1</sub>], 0 <=j<
     * n.
     * <p>
     * The algorithm for generating points on a spline defined with only control points and starting/ending tangents can
     * be found at <a
     * href="http://persson.berkeley.edu/128A/lec14-2x3.pdf">http://persson.berkeley.edu/128A/lec14-2x3.pdf</a> (last
     * visited 19/08/09)
     * 
     * @param spline
     *            curve segment to be linearized, must not be <code>null</code>
     * @param crit
     *            determines the interpolation quality / number of interpolation points, must not be <code>null</code>
     * @return linearized version of the input segment, never <code>null</code>
     */
    public LineStringSegment linearizeCubicSpline( CubicSpline spline, LinearizationCriterion crit ) {

        if ( spline.getCoordinateDimension() != 2 ) {
            throw new UnsupportedOperationException(
                                                     "Linearization of the cubic spline is only suported for a spline in 2D." );
        }

        Points controlPts = spline.getControlPoints();
        // build an array of Point in order to sort it in ascending order
        Point[] pts = new Point[controlPts.size()];
        // n denotes the # of polynomials, that is one less than the # of control pts
        int n = controlPts.size() - 1;
        for ( int i = 0; i <= n; i++ ) {
            pts[i] = controlPts.get( i );
        }

        double startTan = Math.atan2( spline.getVectorAtStart().get1(), spline.getVectorAtStart().get0() );
        double endTan = Math.atan2( spline.getVectorAtEnd().get1(), spline.getVectorAtEnd().get0() );

        boolean ascending = true;
        if ( pts[0].get0() > pts[1].get0() ) {
            ascending = false;
        }

        for ( int i = 0; i <= n - 1; i++ ) {
            if ( ascending ) {
                if ( pts[i].get0() > pts[i + 1].get0() ) {
                    throw new UnsupportedOperationException(
                                                             "It is expected that the control points are ordered on the X-axis either ascendingly or descendingly." );
                }
            } else {
                if ( pts[i].get0() < pts[i + 1].get0() ) {
                    throw new UnsupportedOperationException(
                                                             "It is expected that the control points are ordered on the X-axis either ascendingly or descendingly." );
                }
            }
        }

        if ( !ascending ) {
            // interchange the elements so that they are ordered ascendingly (on the X-axis)
            for ( int i = 0; i <= ( n / 2 ); i++ ) {
                Point aux = pts[i];
                pts[i] = pts[n - i];
                pts[n - i] = aux;
            }
            // also reverse the starting and ending tangents
            startTan = Math.atan2( -spline.getVectorAtEnd().get1(), -spline.getVectorAtEnd().get0() );
            endTan = Math.atan2( -spline.getVectorAtStart().get1(), -spline.getVectorAtStart().get0() );
        }

        // break-up the pts into xcoor in ycoor
        double xcoor[] = new double[n + 1];
        double ycoor[] = new double[n + 1];
        for ( int i = 0; i <= n; i++ ) {
            xcoor[i] = pts[i].get0();
            ycoor[i] = pts[i].get1();
        }

        double[] h = new double[n];
        for ( int i = 0; i <= n - 1; i++ ) {
            h[i] = xcoor[i + 1] - xcoor[i];
        }

        double[][] matrixA = constructMatrixA( h, n );

        double[] vectorb = constructVectorB( n, ycoor, h, startTan, endTan );

        double[] vectorx = solveLinearEquation( matrixA, vectorb );

        int numPoints = -1;
        if ( crit instanceof NumPointsCriterion ) {
            numPoints = ( (NumPointsCriterion) crit ).getNumberOfPoints();
        } else if ( crit instanceof MaxErrorCriterion ) {
            numPoints = ( (MaxErrorCriterion) crit ).getMaxNumPoints();
            if ( numPoints <= 0 ) {
                throw new UnsupportedOperationException(
                                                         "Linearization of the cubic spline with MaxErrorCriterion is currently not supported, unless the number of points is provided." );
                // TODO it is mathematically hard to get an expression of the numOfPoints with respect to the error;
                // there would be two work-arounds as I can see them: 1) through a trial-and-error procedure determine
                // how small should the sampling interval be, so that the difference in value is less than the
                // given error; 2) use the mathematical expression used for the arc/circle (something with Math.acos...)
                // - it needs a good approximation for the radius.
            }
        }

        double[] interpolated = interpolateSpline( n, h, xcoor, ycoor, vectorx, numPoints );

        // populate a list of points, so that later a LineStringSegment can be built from it
        List<Point> iPoints = new ArrayList<Point>();
        ICRS crs = spline.getControlPoints().get( 0 ).getCoordinateSystem();
        PrecisionModel pm = spline.getControlPoints().get( 0 ).getPrecision();
        for ( int i = 0; i < numPoints; i++ ) {
            iPoints.add( new DefaultPoint( null, crs, pm, new double[] { interpolated[2 * i], interpolated[2 * i + 1] } ) );
        }

        LineStringSegment lineSegment = geomFac.createLineStringSegment( new PointsList( iPoints ) );

        return lineSegment;
    }

    private double[] constructVectorB( int n, double[] ycoor, double[] h, double startTan, double endTan ) {
        double[] vectorb = new double[n + 1];
        vectorb[0] = 3 * ( ycoor[1] - ycoor[0] ) / h[0] - 3 * startTan;
        for ( int i = 1; i <= n - 1; i++ ) {
            vectorb[i] = 3 * ( ycoor[i + 1] - ycoor[i] ) / h[i] - 3 * ( ycoor[i] - ycoor[i - 1] ) / h[i - 1];
        }
        vectorb[n] = 3 * endTan - 3 * ( ycoor[n] - ycoor[n - 1] ) / h[n - 1];

        return vectorb;
    }

    private double[] solveLinearEquation( double[][] matrixA, double[] vectorb ) {

        RealMatrix coefficients = new Array2DRowRealMatrix( matrixA, false );

        // LU-decomposition
        DecompositionSolver solver = new LUDecompositionImpl( coefficients ).getSolver();

        RealVector constants = new ArrayRealVector( vectorb, false );
        RealVector solution = null;
        try {
            solution = solver.solve( constants );
        } catch ( SingularMatrixException e ) {
            LOG.error( e.getLocalizedMessage() );
            e.printStackTrace();
        }
        return solution.getData();
    }

    private double[] interpolateSpline( int n, double[] h, double[] xcoor, double[] ycoor, double[] vectorx,
                                        int numPoints ) {
        double[] interpolated = new double[2 * numPoints];

        // compute coefficients of spline
        double[] a = new double[n + 1];
        double[] c = new double[n + 1];
        for ( int i = 0; i <= n; i++ ) {
            a[i] = ycoor[i];
            c[i] = vectorx[i];
        }

        double[] b = new double[n];
        double[] d = new double[n];
        for ( int i = 0; i < n; i++ ) {
            b[i] = ( a[i + 1] - a[i] ) / h[i] - h[i] * ( 2 * c[i] + c[i + 1] ) / 3;
            d[i] = ( c[i + 1] - c[i] ) / ( 3 * h[i] );
        }

        // compute the spacing between points
        double spacing = ( xcoor[n] - xcoor[0] ) / ( numPoints - 1 );

        // current segment of polynomial
        int seg = 0;
        for ( int i = 0; i <= numPoints - 1; i++ ) {
            double x = xcoor[0] + i * spacing;
            if ( x > xcoor[seg + 1] ) {
                seg++;
            }

            double y = a[seg] + b[seg] * ( x - xcoor[seg] ) + c[seg] * Math.pow( x - xcoor[seg], 2 ) + d[seg]
                       * Math.pow( x - xcoor[seg], 3 );

            interpolated[2 * i] = x;
            interpolated[2 * i + 1] = y;
        }

        return interpolated;
    }

    private double[][] constructMatrixA( double[] h, int n ) {
        // first line
        double[][] matrixA = new double[n + 1][n + 1];
        Arrays.fill( matrixA[0], 0 );
        matrixA[0][0] = 2 * h[0];
        matrixA[0][1] = h[0];

        // middle lines
        for ( int i = 1; i <= n - 1; i++ ) {
            Arrays.fill( matrixA[i], 0 );
            matrixA[i][i - 1] = h[i - 1];
            matrixA[i][i] = 2 * ( h[i - 1] + h[i] );
            matrixA[i][i + 1] = h[i];
        }

        Arrays.fill( matrixA[n], 0 );
        matrixA[n][n - 1] = h[n - 1];
        matrixA[n][n] = 2 * h[n - 1];

        return matrixA;
    }

    private double createAngleStep( double startAngle, double endAngle, int numPoints, boolean isClockwise ) {
        boolean isCircle = ( Math.abs( startAngle - endAngle ) < 1E-10 );
        double sweepAngle = isCircle ? TWO_PI : ( startAngle - endAngle );
        double angleStep = 0;
        if ( isClockwise ) {
            if ( !isCircle ) {

                if ( sweepAngle < 0 ) {
                    /**
                     * Because the sweepAngle is negative and we are going cw the sweepAngle must be inverted by adding
                     * it to 2pi
                     */
                    sweepAngle = ( TWO_PI + sweepAngle );
                }
            }
            angleStep = -( sweepAngle / ( numPoints - 1 ) );
        } else {
            if ( !isCircle ) {
                if ( sweepAngle < 0 ) {
                    /**
                     * Because sweepangle is negative but we are going ccw the sweepangle must be mathematically
                     * inverted
                     */
                    sweepAngle = Math.abs( sweepAngle );
                } else {
                    sweepAngle = TWO_PI - sweepAngle;
                }

            }
            angleStep = sweepAngle / ( numPoints - 1 );
        }
        return angleStep;
    }

    private Points interpolate( Point p0, Point p1, Point p2, int numPoints, boolean isCircle ) {

        // shift the points down (to reduce the occurrence of floating point errors), independently on the x and y axes
        double minOrd0 = findShiftOrd0( p0, p1, p2 );
        double minOrd1 = findShiftOrd1( p0, p1, p2 );

        // if the points are already shifted, this does no harm!
        Point p0Shifted = new DefaultPoint( null, p0.getCoordinateSystem(), p0.getPrecision(),
                                            new double[] { p0.get0() - minOrd0, p0.get1() - minOrd1 } );
        Point p1Shifted = new DefaultPoint( null, p1.getCoordinateSystem(), p1.getPrecision(),
                                            new double[] { p1.get0() - minOrd0, p1.get1() - minOrd1 } );
        Point p2Shifted = new DefaultPoint( null, p2.getCoordinateSystem(), p2.getPrecision(),
                                            new double[] { p2.get0() - minOrd0, p2.get1() - minOrd1 } );

        List<Point> interpolationPoints = new ArrayList<Point>( numPoints );
        Point center = calcCircleCenter( p0Shifted, p1Shifted, p2Shifted );

        double centerX = center.get0();
        double centerY = center.get1();

        double dx = p0Shifted.get0() - centerX;
        double dy = p0Shifted.get1() - centerY;
        double ex = p2Shifted.get0() - centerX;
        double ey = p2Shifted.get1() - centerY;

        double startAngle = Math.atan2( dy, dx );
        double endAngle = isCircle ? startAngle : Math.atan2( ey, ex );
        double radius = Math.sqrt( dx * dx + dy * dy );

        double angleStep = createAngleStep( startAngle, endAngle, numPoints,
                                            isClockwise( p0Shifted, p1Shifted, p2Shifted ) );
        ICRS crs = p0Shifted.getCoordinateSystem();
        // ensure numerical stability for start point (= use original circle start point)
        interpolationPoints.add( p0Shifted );

        // calculate intermediate (=interpolated) points on arc
        for ( int i = 1; i < numPoints - 1; i++ ) {
            double angle = startAngle + i * angleStep;
            double x = centerX + Math.cos( angle ) * radius;
            double y = centerY + Math.sin( angle ) * radius;
            interpolationPoints.add( geomFac.createPoint( null, new double[] { x, y }, crs ) );
        }
        // ensure numerical stability for end point (= use original circle start point)
        interpolationPoints.add( isCircle ? p0Shifted : p2Shifted );

        // shift the points back up
        List<Point> realPoints = new ArrayList<Point>( interpolationPoints.size() );
        for ( Point p : interpolationPoints ) {
            realPoints.add( new DefaultPoint( null, p.getCoordinateSystem(), p.getPrecision(),
                                              new double[] { p.get0() + minOrd0, p.get1() + minOrd1 } ) );
        }

        return new PointsList( realPoints );
    }

    private int calcNumPoints( Point p0, Point p1, Point p2, boolean isCircle, double error ) {

        // shift the points down (to reduce the occurrence of floating point errors), independently on the x and y axes
        double minOrd0 = CurveLinearizer.findShiftOrd0( p0, p1, p2 );
        double minOrd1 = CurveLinearizer.findShiftOrd1( p0, p1, p2 );

        // if the points are already shifted, this does no harm!
        Point p0Shifted = new DefaultPoint( null, p0.getCoordinateSystem(), p0.getPrecision(),
                                            new double[] { p0.get0() - minOrd0, p0.get1() - minOrd1 } );
        Point p1Shifted = new DefaultPoint( null, p1.getCoordinateSystem(), p1.getPrecision(),
                                            new double[] { p1.get0() - minOrd0, p1.get1() - minOrd1 } );
        Point p2Shifted = new DefaultPoint( null, p2.getCoordinateSystem(), p2.getPrecision(),
                                            new double[] { p2.get0() - minOrd0, p2.get1() - minOrd1 } );

        Point center = calcCircleCenter( p0Shifted, p1Shifted, p2Shifted );

        double centerX = center.get0();
        double centerY = center.get1();

        double dx = p0Shifted.get0() - centerX;
        double dy = p0Shifted.get1() - centerY;
        double ex = p2Shifted.get0() - centerX;
        double ey = p2Shifted.get1() - centerY;

        double startAngle = Math.atan2( dy, dx );
        double endAngle = isCircle ? startAngle : Math.atan2( ey, ex );
        double radius = Math.sqrt( dx * dx + dy * dy );

        double angleStep = 2 * Math.acos( 1 - error / radius );
        int numPoints;
        if ( isCircle ) {
            numPoints = (int) Math.ceil( 2 * Math.PI / angleStep ) + 2;
        } else {
            if ( !isClockwise( p0Shifted, p1Shifted, p2Shifted ) ) {
                if ( endAngle < startAngle ) {
                    endAngle += 2 * Math.PI;
                }
                numPoints = (int) Math.ceil( ( endAngle - startAngle ) / angleStep ) + 2;
            } else {
                if ( startAngle < endAngle ) {
                    startAngle += 2 * Math.PI;
                }
                numPoints = (int) Math.ceil( ( startAngle - endAngle ) / angleStep ) + 2;
            }
        }
        return numPoints;
    }

    /**
     * Finds the center of a circle/arc that is specified by three points that lie on the circle's boundary.
     * <p>
     * Credits go to <a href="http://en.wikipedia.org/wiki/Circumradius#Coordinates_of_circumcenter">wikipedia</a>
     * (visited on 13/08/09).
     * </p>
     * 
     * @param p0
     *            first point
     * @param p1
     *            second point
     * @param p2
     *            third point
     * @return center of the circle
     * @throws IllegalArgumentException
     *             if the points are collinear, i.e. on a single line
     */
    Point calcCircleCenter( Point p0, Point p1, Point p2 )
                            throws IllegalArgumentException {

        // shift the points down (to reduce the occurrence of floating point errors), independently on the x and y axes
        double minOrd0 = CurveLinearizer.findShiftOrd0( p0, p1, p2 );
        double minOrd1 = CurveLinearizer.findShiftOrd1( p0, p1, p2 );

        // if the points are already shifted, this does no harm!
        Point p0Shifted = new DefaultPoint( null, p0.getCoordinateSystem(), p0.getPrecision(),
                                            new double[] { p0.get0() - minOrd0, p0.get1() - minOrd1 } );
        Point p1Shifted = new DefaultPoint( null, p1.getCoordinateSystem(), p1.getPrecision(),
                                            new double[] { p1.get0() - minOrd0, p1.get1() - minOrd1 } );
        Point p2Shifted = new DefaultPoint( null, p2.getCoordinateSystem(), p2.getPrecision(),
                                            new double[] { p2.get0() - minOrd0, p2.get1() - minOrd1 } );

        if ( areCollinear( p0Shifted, p1Shifted, p2Shifted ) ) {
            throw new IllegalArgumentException( "The given points are collinear, no circum center can be calculated." );
        }

        Vector3d a = new Vector3d( p0Shifted.get0(), p0Shifted.get1(), p0Shifted.get2() );
        Vector3d b = new Vector3d( p1Shifted.get0(), p1Shifted.get1(), p1Shifted.get2() );
        Vector3d c = new Vector3d( p2Shifted.get0(), p2Shifted.get1(), p2Shifted.get2() );

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

        a.scale( ( ( bc.length() * bc.length() ) * ab.dot( ac ) ) / crosSquare );
        b.scale( ( ( ac.length() * ac.length() ) * ba.dot( bc ) ) / crosSquare );
        c.scale( ( ( ab.length() * ab.length() ) * ca.dot( cb ) ) / crosSquare );

        Point3d circle = new Point3d( a );
        circle.add( b );
        circle.add( c );

        // shift the center circle back up
        circle.x += minOrd0;
        circle.y += minOrd1;

        return geomFac.createPoint( null, new double[] { circle.x, circle.y }, p0Shifted.getCoordinateSystem() );
    }

    /**
     * Find the midpoint between the highest and the lowest ordonate 1 axis among the 3 points
     * 
     * @param p0
     * @param p1
     * @param p2
     * @return
     */
    private static double findShiftOrd1( Point p0, Point p1, Point p2 ) {
        double minOrd1 = p0.get1();
        if ( p1.get1() < minOrd1 ) {
            minOrd1 = p1.get1();
        }
        if ( p2.get1() < minOrd1 ) {
            minOrd1 = p2.get1();
        }

        double maxOrd1 = p0.get1();
        if ( p1.get1() > maxOrd1 ) {
            maxOrd1 = p1.get1();
        }
        if ( p2.get1() > maxOrd1 ) {
            maxOrd1 = p2.get1();
        }

        return ( maxOrd1 + minOrd1 ) / 2;
    }

    /**
     * Find the midpoint between the highest and the lowest ordonate 0 axis among the 3 points
     * 
     * @param p0
     * @param p1
     * @param p2
     * @return
     */
    private static double findShiftOrd0( Point p0, Point p1, Point p2 ) {
        double minOrd0 = p0.get0();
        if ( p1.get0() < minOrd0 ) {
            minOrd0 = p1.get0();
        }
        if ( p2.get0() < minOrd0 ) {
            minOrd0 = p2.get0();
        }

        double maxOrd0 = p0.get0();
        if ( p1.get0() > minOrd0 ) {
            maxOrd0 = p1.get0();
        }
        if ( p2.get0() > minOrd0 ) {
            maxOrd0 = p2.get0();
        }

        return ( maxOrd0 + minOrd0 ) / 2;
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
        if ( areCollinear( p0, p1, p2 ) ) {
            throw new IllegalArgumentException( "Cannot evaluate isClockwise(). The three points are collinear." );
        }
        double res = ( p2.get0() - p0.get0() ) * ( ( p2.get1() + p0.get1() ) / 2 ) + ( p1.get0() - p2.get0() )
                     * ( ( p1.get1() + p2.get1() ) / 2 ) + ( p0.get0() - p1.get0() ) * ( ( p0.get1() + p1.get1() ) / 2 );

        return res < 0.0 ? true : false;
    }

    /**
     * Tests if the given three points are collinear.
     * <p>
     * NOTE: Only this method should be used throughout the whole linearization process for testing collinearity to
     * avoid inconsistent results (the necessary EPSILON would differ).
     * </p>
     * 
     * @param p0
     *            first point, must not be <code>null</code>
     * @param p1
     *            second point, must not be <code>null</code>
     * @param p2
     *            third point, must not be <code>null</code>
     * @return true if the points are collinear, false otherwise
     */
    public static boolean areCollinear( Point p0, Point p1, Point p2 ) {

        // shift the points down (to reduce the occurrence of floating point errors), independently on the x and y axes
        double minOrd0 = findShiftOrd0( p0, p1, p2 );
        double minOrd1 = findShiftOrd1( p0, p1, p2 );

        // if the points are already shifted, this does no harm!
        Point p0Shifted = new DefaultPoint( null, p0.getCoordinateSystem(), p0.getPrecision(),
                                            new double[] { p0.get0() - minOrd0, p0.get1() - minOrd1 } );
        Point p1Shifted = new DefaultPoint( null, p1.getCoordinateSystem(), p1.getPrecision(),
                                            new double[] { p1.get0() - minOrd0, p1.get1() - minOrd1 } );
        Point p2Shifted = new DefaultPoint( null, p2.getCoordinateSystem(), p2.getPrecision(),
                                            new double[] { p2.get0() - minOrd0, p2.get1() - minOrd1 } );

        double res = ( p2Shifted.get0() - p0Shifted.get0() ) * ( ( p2Shifted.get1() + p0Shifted.get1() ) / 2 )
                     + ( p1Shifted.get0() - p2Shifted.get0() ) * ( ( p1Shifted.get1() + p2Shifted.get1() ) / 2 )
                     + ( p0Shifted.get0() - p1Shifted.get0() ) * ( ( p0Shifted.get1() + p1Shifted.get1() ) / 2 );
        return Math.abs( res ) < EPSILON;
    }
}
