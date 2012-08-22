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

import java.util.ArrayList;

/**
 *
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class LinearContains {

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
     * Currently not supported
     *
     * @param point1
     * @param point2
     * @return true if the the two submitted points are the same......maybe.
     */
    public static boolean contains( Position point1, Position point2 ) {
        throw new UnsupportedOperationException( "contains(Position, Position)" + " not supported at the moment." );
    }

    /**
     * Currently not supported
     *
     * @param curve
     * @param point
     * @return true if the submitted point contains the submitted curve segment
     */
    public static boolean contains( CurveSegment curve, Position point ) {
        throw new UnsupportedOperationException( "contains(CurveSegment, Position)" + " not supported at the moment." );
    }

    /**
     * the operation returns true if the submitted point contains the submitted surface patch
     *
     * @param surface
     * @param point
     * @param tolerance
     * @return true if the passed point contains the submitted surface patch
     */
    public static boolean contains( SurfacePatch surface, Position point, double tolerance ) {
        boolean con = false;
        Position[] ex = surface.getExteriorRing();
        con = contains( ex, point, tolerance );

        if ( con ) {
            Position[][] inner = surface.getInteriorRings();

            if ( inner != null ) {
                for ( int i = 0; i < inner.length; i++ ) {
                    if ( contains( inner[i], point, tolerance ) ) {
                        con = false;
                        break;
                    }
                }
            }
        }

        return con;
    }

    /**
     * the operation is currently not supported
     *
     * @param curve1
     * @param curve2
     * @return true if the two submitted curves segments contain each other.
     */
    public static boolean contains( CurveSegment curve1, CurveSegment curve2 ) {
        throw new UnsupportedOperationException( "contains(CurveSegment, CurveSegment)"
                                                 + " not supported at the moment." );
    }

    /**
     * the operation returns true if the submitted curve segment contains the submitted surface patch
     *
     * @param surface
     * @param curve
     * @param tolerance
     * @return true if the submitted curve segment contains the submitted surface patch
     */
    public static boolean contains( SurfacePatch surface, CurveSegment curve, double tolerance ) {
        boolean con = true;
        Position[] ex = surface.getExteriorRing();
        Position[] cu = curve.getPositions();

        for ( int i = 0; i < cu.length; i++ ) {
            if ( !contains( ex, cu[i], tolerance ) ) {
                con = false;
                break;
            }
        }

        if ( con ) {
            Position[][] inner = surface.getInteriorRings();

            if ( inner != null ) {
                for ( int i = 0; i < inner.length; i++ ) {
                    for ( int j = 0; j < cu.length; j++ ) {
                        if ( contains( inner[i], cu[j], tolerance ) ) {
                            con = false;
                            break;
                        }
                    }

                    if ( !con ) {
                        break;
                    }
                }
            }
        }

        return con;
    }

    /**
     * the operation returns true if the first surface patches contains the second one
     *
     * @param surface1
     * @param surface2
     * @param tolerance
     * @return true if the first surface patches contains
     */
    public static boolean contains( SurfacePatch surface1, SurfacePatch surface2, double tolerance ) {
        boolean con = true;
        Position[] ex = surface1.getExteriorRing();
        Position[] ex_ = surface2.getExteriorRing();

        for ( int i = 0; i < ex_.length; i++ ) {
            if ( !contains( ex, ex_[i], tolerance ) ) {
                con = false;
                break;
            }
        }

        if ( con ) {
            Position[][] inner = surface1.getInteriorRings();
            Position[][] inner_ = surface2.getInteriorRings();

            if ( inner != null ) {
                for ( int i = 0; i < inner.length; i++ ) {
                    // a point of the second exterior is not allowed to be
                    // within a inner ring of the first
                    for ( int j = 0; j < ex_.length; j++ ) {
                        if ( contains( inner[i], ex_[j], tolerance ) ) {
                            con = false;
                            break;
                        }
                    }

                    if ( !con ) {
                        break;
                    }

                    // a point of the inner rings of the second is not allowed
                    // to be within a inner ring of the first
                    if ( inner_ != null ) {
                        for ( int k = 0; k < inner_.length; k++ ) {
                            for ( int j = 0; j < inner_[k].length; j++ ) {
                                if ( contains( inner[i], inner_[k][j], tolerance ) ) {
                                    con = false;
                                    break;
                                }
                            }

                            if ( !con ) {
                                break;
                            }
                        }
                    }

                    // a point of the inner rings of the first is not allowed
                    // to be within the second surface
                    for ( int j = 0; j < inner[i].length; j++ ) {
                        if ( contains( surface2, inner[i][j], tolerance ) ) {
                            con = false;
                            break;
                        }
                    }

                    if ( !con ) {
                        break;
                    }
                }
            }
        }

        // surface2 is not allowed to contain one point of surface1
        if ( con ) {
            for ( int i = 0; i < ex.length; i++ ) {
                if ( contains( surface2, ex[i], tolerance ) ) {
                    con = false;
                    break;
                }
            }
        }

        return con;
    }

    /**
     * the operations returns true if two the passed points contains
     *
     * @param point1
     * @param point2
     * @return true if two the passed points contains
     */
    public static boolean contains( Point point1, Point point2 ) {
        return point1.equals( point2 );
    }

    /**
     * the operation is currently not supported.
     *
     * @param curve
     * @param point
     * @return true if the submitted point contains the submitted curve
     */
    public static boolean contains( Curve curve, Point point ) {
        throw new UnsupportedOperationException( "contains(Curve, Point)" + " not supported at the moment." );
    }

    /**
     * the operation returns true if the submitted point contains the submitted surface
     *
     * @param surface
     * @param point
     * @return true if the submitted point contains the submitted surface
     * @throws Exception
     */
    public static boolean contains( Surface surface, Point point )
                            throws Exception {
        boolean contain = false;
        int cnt = surface.getNumberOfSurfacePatches();

        double tolerance = getTolerance( surface, point );
        for ( int i = 0; i < cnt; i++ ) {
            if ( contains( surface.getSurfacePatchAt( i ), point.getPosition(), tolerance ) ) {
                contain = true;
                break;
            }
        }

        return contain;
    }

    /**
     * the operation is currently not supported.
     *
     * @param curve1
     * @param curve2
     * @return true if the two submitted curves contains
     */
    public static boolean contains( Curve curve1, Curve curve2 ) {
        throw new UnsupportedOperationException( "contains(Curve, Curve)" + " not supported at the moment." );
    }

    /**
     * Convenience method to extract all <tt>Position</tt>s from a <tt>Curve</tt>.
     */
    private static Position[] getPositions( Curve curve )
                            throws GeometryException {
        ArrayList<Position> positions = new ArrayList<Position>( 1000 );

        for ( int i = 0; i < curve.getNumberOfCurveSegments(); i++ ) {
            CurveSegment segment = curve.getCurveSegmentAt( i );
            Position[] segmentPos = segment.getPositions();

            for ( int j = 0; j < segmentPos.length; j++ )
                positions.add( segmentPos[j] );
        }

        return positions.toArray( new Position[positions.size()] );
    }

    /**
     * the operation returns true if the submitted curve contains the submitted surface
     *
     * @param surface
     * @param curve
     * @return true if the submitted curve contains the submitted surface
     * @throws GeometryException
     */
    public static boolean contains( Surface surface, Curve curve )
                            throws GeometryException {
        // gather the positions of the crings (exterior and interior) and
        // the curve as arrays of Positions
        SurfaceBoundary boundary = (SurfaceBoundary) surface.getBoundary();
        Ring extRing = boundary.getExteriorRing();
        Ring[] intRings = boundary.getInteriorRings();

        Position[] curvePos = getPositions( curve );
        Position[] extRingPos = extRing.getPositions();
        Position[][] intRingsPos = new Position[intRings.length][];

        for ( int i = 0; i < intRings.length; i++ )
            intRingsPos[i] = intRings[i].getPositions();

        // necessary condition: all points of the curve have to be inside
        // of the surface's exterior ring and none must be inside of one
        // of the interior rings
        for ( int i = 0; i < curvePos.length; i++ ) {
            if ( !contains( extRingPos, curvePos[i], getTolerance( surface, curve ) ) ) {
                return false;
            }

            for ( int j = 0; j < intRings.length; j++ ) {
                if ( contains( intRingsPos[j], curvePos[i], getTolerance( surface, curve ) ) ) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * the operation returns true if the two submitted surfaces contains
     *
     * @param surface2
     * @param surface1
     * @return true if the two submitted surfaces contains
     * @throws Exception
     */
    public static boolean contains( Surface surface2, Surface surface1 )
                            throws Exception {
        double tolerance = getTolerance( surface1, surface2 );
        return contains( surface2.getSurfacePatchAt( 0 ), surface1.getSurfacePatchAt( 0 ), tolerance );
    }

    /**
     * the operation returns true if polygon defined by an array of Position contains the submitted point.
     *
     * @param positions
     * @param point
     * @param tolerance
     * @return true if polygon defined by an array of Position contains the submitted point.
     */
    protected static boolean contains( Position[] positions, Position point, double tolerance ) {

        // TODO
        // consider tolerance value

        if ( positions.length <= 2 ) {
            return false;
        }

        int hits = 0;

        double lastx = positions[positions.length - 1].getX();
        double lasty = positions[positions.length - 1].getY();
        double curx;
        double cury;

        // Walk the edges of the polygon
        for ( int i = 0; i < positions.length; lastx = curx, lasty = cury, i++ ) {
            curx = positions[i].getX();
            cury = positions[i].getY();

            if ( cury == lasty ) {
                continue;
            }

            double leftx;

            if ( curx < lastx ) {
                if ( point.getX() >= lastx ) {
                    continue;
                }

                leftx = curx;
            } else {
                if ( point.getX() >= curx ) {
                    continue;
                }

                leftx = lastx;
            }

            double test1;
            double test2;

            if ( cury < lasty ) {
                if ( ( point.getY() < cury ) || ( point.getY() >= lasty ) ) {
                    continue;
                }

                if ( point.getX() < leftx ) {
                    hits++;
                    continue;
                }

                test1 = point.getX() - curx;
                test2 = point.getY() - cury;
            } else {
                if ( ( point.getY() < lasty ) || ( point.getY() >= cury ) ) {
                    continue;
                }

                if ( point.getX() < leftx ) {
                    hits++;
                    continue;
                }

                test1 = point.getX() - lastx;
                test2 = point.getY() - lasty;
            }

            if ( test1 < ( test2 / ( lasty - cury ) * ( lastx - curx ) ) ) {
                hits++;
            }
        }

        return ( ( hits & 1 ) != 0 );
    }
}
