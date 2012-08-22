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
package org.deegree.io.shpapi;

import java.util.ArrayList;

import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.CurveSegment;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.SurfaceInterpolation;
import org.deegree.model.spatialschema.SurfaceInterpolationImpl;

/**
 * the class SHP2WKS transforms a polygon structure read from a shape-file<BR>
 * into a WKSLinearPolygon specified by the sf-specifications<BR>
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class SHP2WKS {

    /**
     * method: Point transformPoint(CS_CoordinateSystem srs,<BR>
     * SHPPoint shppoint))<BR>
     * transforms a SHPPoint to a WKSGeometry<BR>
     * gets a point that should be transformed<BR>
     *
     * @param crs
     * @param shppoint
     * @return the transformed point
     */
    public Point transformPoint( CoordinateSystem crs, SHPPoint shppoint ) {
        return GeometryFactory.createPoint( shppoint.x, shppoint.y, crs );
    }

    /**
     * method: Point[] transformMultiPoint(CS_CoordinateSystem srs,<BR>
     * SHPMultiPoint shpmultipoint))<BR>
     * transforms a SHPMultiPoint to a WKSGeometry<BR>
     * gets a multipoint that should be transformed<BR>
     *
     * @param srs
     * @param shpmultipoint
     * @return the transformed points
     */
    public Point[] transformMultiPoint( CoordinateSystem srs, SHPMultiPoint shpmultipoint ) {
        Point[] gm_points = new Point[shpmultipoint.numPoints];

        for ( int i = 0; i < shpmultipoint.numPoints; i++ )
            gm_points[i] = GeometryFactory.createPoint( shpmultipoint.points[i].x, shpmultipoint.points[i].y, srs );

        return gm_points;
    }

    /**
     * method: Point[][] transformPolyLine(CS_CoordinateSystem srs,<BR>
     * SHPPolyLine shppolyline))<BR>
     * transforms a SHPPolyLine to a WKSGeometry<BR>
     * gets a polyline that should be transformed<BR>
     *
     * @param crs
     * @param shppolyline
     * @return the transformed line
     */
    public Curve[] transformPolyLine( CoordinateSystem crs, SHPPolyLine shppolyline ) {
        Curve[] curve = new Curve[shppolyline.numParts];

        try {
            for ( int j = 0; j < shppolyline.numParts; j++ ) {
                Position[] gm_points = new Position[shppolyline.points[j].length];

                for ( int i = 0; i < shppolyline.points[j].length; i++ ) {
                    gm_points[i] = GeometryFactory.createPosition( shppolyline.points[j][i].x,
                                                                   shppolyline.points[j][i].y );
                }

                CurveSegment cs = GeometryFactory.createCurveSegment( gm_points, crs );
                curve[j] = GeometryFactory.createCurve( cs );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return curve;
    }

    /**
     * method: private boolean isInsideRing(Point[] ring, Point point)<BR>
     * checks if a point is inside a polygon. the algorithm is taken from:<BR>
     * http://www.ics.uci.edu/~eppstein/161/960307.html#intest<BR>
     */
    private boolean isInsideRing( Position[] ring, Position point ) {
        int crossings = 0;

        for ( int i = 0; i < ring.length; i++ ) {
            int z = i + 1;

            if ( ( i + 1 ) >= ring.length ) {
                z = 0;
            }

            // check if point.x is between x of vertex i and z of ring
            if ( ( ring[i].getX() < point.getX() && point.getX() < ring[z].getX() )
                 || ( ring[i].getX() > point.getX() && point.getX() > ring[z].getX() ) ) {
                double t = ( point.getX() - ring[z].getX() ) / ( ring[i].getX() - ring[z].getX() );

                double cy = ( t * ring[i].getY() ) + ( ( 1 - t ) * ring[z].getY() );

                if ( point.getY() == cy ) { // point is on border of ring
                    return false;
                } else if ( point.getY() > cy ) { // downwards vertical line through point crosses
                    // ring
                    crossings++;
                }
            }

            // check if point.x equals x of vertex i of ring while point.y > ring[i].y
            if ( ( ring[i].getX() == point.getX() ) && ( ring[i].getY() <= point.getY() ) ) {

                if ( ring[i].getY() == point.getY() ) { // point is on border of ring
                    return false;
                }

                // find next point on ring with different x
                // (adjacent points in shapefile can have equal x&y)
                while ( ring[z].getX() == point.getX() ) {
                    if ( z == i ) {
                        return false;
                    }
                    z += 1;
                    if ( z == ring.length ) {
                        z = 0;
                    }
                }

                // find previous point on ring with different x
                int zz = i - 1;
                if ( zz < 0 ) {
                    zz = ring.length - 1;
                }
                while ( ring[zz].getX() == point.getX() ) {
                    if ( zz == i ) {
                        return false;
                    }
                    zz -= 1;
                    if ( zz < 0 ) {
                        zz = ring.length - 1;
                    }
                }

                // if point.x between previous and next x then crossing
                if ( ring[z].getX() < point.getX() && point.getX() < ring[zz].getX() || ring[z].getX() > point.getX()
                     && point.getX() > ring[zz].getX() ) {
                    crossings++;
                }

            }
        }

        if ( ( crossings % 2 ) != 0 ) {
            return true;
        }
        return false;
    }

    /**
     * transforms the SHPPolygon to a WKSGeometry<BR>
     * gets the polygon that should be transformed<BR>
     *
     * @param crs
     * @param shppolygon
     * @return the transformed polygon
     */
    public Surface[] transformPolygon( CoordinateSystem crs, SHPPolygon shppolygon ) {
        ArrayList<Position[]> all_rings = new ArrayList<Position[]>( shppolygon.numRings );
        ArrayList<Position[]> outer_rings = new ArrayList<Position[]>( shppolygon.numRings );
        ArrayList<Position[]> inner_rings = new ArrayList<Position[]>( shppolygon.numRings );

        for ( int i = 0; i < shppolygon.numRings; i++ ) {

            Position[] ring = new Position[shppolygon.rings.points[i].length];

            for ( int k = 0; k < shppolygon.rings.points[i].length; k++ ) {
                ring[k] = GeometryFactory.createPosition( shppolygon.rings.points[i][k].x,
                                                          shppolygon.rings.points[i][k].y );
            }
            all_rings.add( ring );
        }

        // for every outer ring
        for ( int i = 0; i < all_rings.size(); i++ ) {
            Position[] out_ring = all_rings.get( i );

            boolean inn = false;
            for ( int j = 0; j < all_rings.size(); j++ ) {
                if ( i == j )
                    continue;
                Position[] inring = all_rings.get( j );

                // check if one or more points of a inner ring are
                // within the actual outer ring
                try {
                    if ( isInsideRing( inring, out_ring[0] ) ) {
                        inn = true;
                        inner_rings.add( out_ring );
                        break;
                    }
                } catch ( Exception e ) {
                    e.printStackTrace();
                }

            }
            if ( !inn ) {
                outer_rings.add( out_ring );
            }

        }

        ArrayList<Surface> wkslp = new ArrayList<Surface>( outer_rings.size() );
        SurfaceInterpolation si = new SurfaceInterpolationImpl();
        for ( int i = 0; i < outer_rings.size(); i++ ) {
            Position[] out_ring = outer_rings.get( i );

            int count = inner_rings.size() - 1;
            ArrayList<Position[]> list = new ArrayList<Position[]>( count + 2 );
            // find inner rings of the current outter ring
            for ( int k = count; k >= 0; k-- ) {
                Position[] in_ring = inner_rings.get( k );
                if ( isInsideRing( out_ring, in_ring[0] ) ) {
                    list.add( inner_rings.remove( k ) );
                }
            }
            Position[][] inrings = list.toArray( new Position[list.size()][] );

            try {
                Surface sur = GeometryFactory.createSurface( out_ring, inrings, si, crs );
                wkslp.add( sur );
            } catch ( Exception e ) {
                e.printStackTrace();
            }

        }

        return wkslp.toArray( new Surface[wkslp.size()] );
    }
}
