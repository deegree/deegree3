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

/**
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class LinearIntersects {

    /**
     * 
     * @param geom1
     * @param geom2
     * @return maximum tolerance of geom1 and geom2
     */
    public static double getTolerance( Geometry geom1, Geometry geom2 ) {
        double d = geom1.getTolerance();
        if ( geom2.getTolerance() > d ) {
            d = geom2.getTolerance();
        }
        return d;
    }

    /**
     * the operations returns true if two the submitted points intersects
     * 
     * @param point1
     * @param point2
     * @param tolerance
     * @return true if two the submitted points intersects
     */
    public static boolean intersects( Position point1, Position point2, double tolerance ) {

        double d = 0;
        double[] p1 = point1.getAsArray();
        double[] p2 = point2.getAsArray();

        for ( int i = 0; i < p1.length; i++ ) {
            d += ( ( p1[i] - p2[i] ) * ( p1[i] - p2[i] ) );
        }

        return Math.sqrt( d ) < tolerance;
    }

    /**
     * the operations returns true if the submitted point intersects the passed curve segment
     * 
     * @param point
     * @param curve
     * @param tolerance
     * @return true if the submitted point intersects the passed curve segment
     */
    public static boolean intersects( Position point, CurveSegment curve, double tolerance ) {
        boolean inter = false;

        Position[] points = curve.getPositions();

        for ( int i = 0; i < ( points.length - 1 ); i++ ) {
            if ( linesIntersect( points[i].getX(), points[i].getY(), points[i + 1].getX(), points[i + 1].getY(),
                                 point.getX() - tolerance, point.getY() - tolerance, point.getX() + tolerance,
                                 point.getY() - tolerance )
                 || linesIntersect( points[i].getX(), points[i].getY(), points[i + 1].getX(), points[i + 1].getY(),
                                    point.getX() + tolerance, point.getY() - tolerance, point.getX() + tolerance,
                                    point.getY() + tolerance )
                 || linesIntersect( points[i].getX(), points[i].getY(), points[i + 1].getX(), points[i + 1].getY(),
                                    point.getX() + tolerance, point.getY() + tolerance, point.getX() - tolerance,
                                    point.getY() + tolerance )
                 || linesIntersect( points[i].getX(), points[i].getY(), points[i + 1].getX(), points[i + 1].getY(),
                                    point.getX() - tolerance, point.getY() + tolerance, point.getX() - tolerance,
                                    point.getY() - tolerance ) ) {
                inter = true;
                break;
            }
        }

        return inter;
    }

    /**
     * the operation returns true if the submitted point intersects the submitted surface patch
     * 
     * @param point
     * @param surface
     * @param tolerance
     * @return true if the submitted point intersects the submitted surface patch
     */
    public static boolean intersects( Position point, SurfacePatch surface, double tolerance ) {
        return LinearContains.contains( surface, point, tolerance );
    }

    /**
     * the operation returns true if the two submitted curves segments intersects
     * 
     * @param curve1
     * @param curve2
     * @return returns true if the two submitted curves segments intersects
     */
    public static boolean intersects( CurveSegment curve1, CurveSegment curve2 ) {
        Position[] points = curve1.getPositions();
        Position[] other = curve2.getPositions();
        boolean inter = false;

        for ( int i = 0; i < ( points.length - 1 ); i++ ) {
            for ( int j = 0; j < ( other.length - 1 ); j++ ) {
                if ( linesIntersect( points[i].getX(), points[i].getY(), points[i + 1].getX(), points[i + 1].getY(),
                                     other[j].getX(), other[j].getY(), other[j + 1].getX(), other[j + 1].getY() ) ) {
                    inter = true;
                    break;
                }
            }
        }

        return inter;
    }

    /**
     * the operation returns true if the passed curve segment intersects the submitted surface patch
     * 
     * @param curve
     * @param surface
     * @param tolerance
     * @return true if the submitted curve segment intersects the passed surface patch
     * @throws GeometryException
     */
    public static boolean intersects( CurveSegment curve, SurfacePatch surface, double tolerance )
                            throws GeometryException {
        boolean inter = false;
        // is the curve completly embedded within the surface patch

        if ( LinearContains.contains( surface, curve, tolerance ) ) {
            inter = true;
        }

        // intersects the curve the exterior ring of the surface patch
        if ( !inter ) {
            Position[] ex = surface.getExteriorRing();
            CurveSegment cs = new LineStringImpl( ex, surface.getCoordinateSystem() );

            if ( intersects( curve, cs ) ) {
                inter = true;
            }
        }

        // intersects the curve one of the interior rings of the surface patch
        if ( !inter ) {
            Position[][] interior = surface.getInteriorRings();

            if ( interior != null ) {
                for ( int i = 0; i < interior.length; i++ ) {
                    CurveSegment cs = new LineStringImpl( interior[i], surface.getCoordinateSystem() );

                    if ( intersects( curve, cs ) ) {
                        inter = true;
                        break;
                    }
                }
            }
        }

        return inter;
    }

    /**
     * the operation returns true if the two passed surface patches intersects
     * 
     * @param surface1
     * @param surface2
     * @param tolerance
     *            ignored by the current implementation
     * @return true if the two passed surface patches intersects
     * @throws GeometryException
     *             if the line strings could not be created.
     */
    public static boolean intersects( SurfacePatch surface1, SurfacePatch surface2, double tolerance )
                            throws GeometryException {
        com.vividsolutions.jts.geom.Polygon jtsPolygon1 = JTSAdapter.export( surface1 );
        com.vividsolutions.jts.geom.Polygon jtsPolygon2 = JTSAdapter.export( surface2 );
        return jtsPolygon1.intersects( jtsPolygon2 );
    }

    /**
     * the operations returns true if two the submitted points intersects
     * 
     * @param point1
     * @param point2
     * @return true if two the submitted points intersects
     */
    public static boolean intersects( Point point1, Point point2 ) {
        double tolerance = getTolerance( point1, point2 );
        return intersects( point1.getPosition(), point2.getPosition(), tolerance );
    }

    /**
     * the operations returns true if the submitted point intersects the submitted curve
     * 
     * @param point
     * @param curve
     * @return true if the submitted point intersects the submitted curve
     * @throws GeometryException
     */
    public static boolean intersects( Point point, Curve curve )
                            throws GeometryException {
        boolean inter = false;

        int cnt = curve.getNumberOfCurveSegments();

        double tolerance = getTolerance( point, curve );
        for ( int i = 0; i < cnt; i++ ) {
            if ( intersects( point.getPosition(), curve.getCurveSegmentAt( i ), tolerance ) ) {
                inter = true;
                break;
            }
        }

        return inter;
    }

    /**
     * the operation returns true if the submitted point intersects the submitted surface
     * 
     * @param point
     * @param surface
     * @return true if the submitted point intersects the submitted surface
     * @throws GeometryException
     */
    public static boolean intersects( Point point, Surface surface )
                            throws GeometryException {
        boolean inter = false;

        int cnt = surface.getNumberOfSurfacePatches();

        double tolerance = getTolerance( point, surface );
        for ( int i = 0; i < cnt; i++ ) {
            if ( intersects( point.getPosition(), surface.getSurfacePatchAt( i ), tolerance ) ) {
                inter = true;
                break;
            }
        }

        return inter;
    }

    /**
     * the operation returns true if the two submitted curves intersects
     * 
     * @param curve1
     * @param curve2
     * @return true if the two submitted curves intersects
     * @throws GeometryException
     */
    public static boolean intersects( Curve curve1, Curve curve2 )
                            throws GeometryException {
        boolean inter = false;
        int cnt1 = curve1.getNumberOfCurveSegments();
        int cnt2 = curve2.getNumberOfCurveSegments();

        for ( int i = 0; ( i < cnt1 ) && !inter; i++ ) {
            for ( int j = 0; j < cnt2; j++ ) {
                if ( intersects( curve1.getCurveSegmentAt( i ), curve2.getCurveSegmentAt( j ) ) ) {
                    inter = true;
                    break;
                }
            }
        }

        return inter;
    }

    /**
     * the operation returns true if the submitted curve intersects the submitted surface
     * 
     * @param curve
     * @param surface
     * @return true if the submitted curve intersects the submitted surface
     * @throws GeometryException
     */
    public static boolean intersects( Curve curve, Surface surface )
                            throws GeometryException {
        boolean inter = false;
        int cnt1 = curve.getNumberOfCurveSegments();
        int cnt2 = surface.getNumberOfSurfacePatches();

        double tolerance = getTolerance( curve, surface );
        for ( int i = 0; i < cnt1; i++ ) {
            for ( int j = 0; j < cnt2; j++ ) {
                if ( intersects( curve.getCurveSegmentAt( i ), surface.getSurfacePatchAt( j ), tolerance ) ) {
                    inter = true;
                    break;
                }
            }

            if ( inter ) {
                break;
            }
        }

        return inter;
    }

    /**
     * the operation returns true if the two passed surfaces intersects
     * 
     * @param surface1
     * @param surface2
     * @return true if the two passed surfaces intersects
     * @throws GeometryException
     */
    public static boolean intersects( Surface surface1, Surface surface2 )
                            throws GeometryException {
        boolean inter = false;

        int cnt1 = surface1.getNumberOfSurfacePatches();
        int cnt2 = surface2.getNumberOfSurfacePatches();

        double tolerance = getTolerance( surface1, surface2 );
        for ( int i = 0; i < cnt1; i++ ) {
            for ( int j = 0; j < cnt2; j++ ) {
                if ( intersects( surface1.getSurfacePatchAt( i ), surface2.getSurfacePatchAt( j ), tolerance ) ) {
                    inter = true;
                    break;
                }
            }

            if ( inter ) {
                break;
            }
        }

        return inter;
    }

    /**
     * 
     * 
     * @param X1
     * @param Y1
     * @param X2
     * @param Y2
     * @param PX
     * @param PY
     * 
     * @return -1 if the points are counter clock wise, 0 if the points have a direction and 1 if they are clockwise.
     */
    protected static int relativeCCW( double X1, double Y1, double X2, double Y2, double PX, double PY ) {
        X2 -= X1;
        Y2 -= Y1;
        PX -= X1;
        PY -= Y1;

        double ccw = ( PX * Y2 ) - ( PY * X2 );

        if ( ccw == 0.0 ) {
            ccw = ( PX * X2 ) + ( PY * Y2 );

            if ( ccw > 0.0 ) {
                PX -= X2;
                PY -= Y2;
                ccw = ( PX * X2 ) + ( PY * Y2 );

                if ( ccw < 0.0 ) {
                    ccw = 0.0;
                }
            }
        }

        return ( ccw < 0.0 ) ? ( -1 ) : ( ( ccw > 0.0 ) ? 1 : 0 );
    }

    /**
     * Tests if the line segment from (x1,&nbsp;y1) to (x2,&nbsp;y2) intersects the line segment from (x3,&nbsp;y3) to
     * (x4,&nbsp;y4).
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     * @param x4
     * @param y4
     * 
     * @return <code>true</code> if the first specified line segment and the second specified line segment intersect
     *         each other; <code>false</code> otherwise.
     */
    protected static boolean linesIntersect( double x1, double y1, double x2, double y2, double x3, double y3,
                                             double x4, double y4 ) {
        return ( ( relativeCCW( x1, y1, x2, y2, x3, y3 ) * relativeCCW( x1, y1, x2, y2, x4, y4 ) <= 0 ) && ( relativeCCW(
                                                                                                                          x3,
                                                                                                                          y3,
                                                                                                                          x4,
                                                                                                                          y4,
                                                                                                                          x1,
                                                                                                                          y1 )
                                                                                                             * relativeCCW(
                                                                                                                            x3,
                                                                                                                            y3,
                                                                                                                            x4,
                                                                                                                            y4,
                                                                                                                            x2,
                                                                                                                            y2 ) <= 0 ) );
    }
}
