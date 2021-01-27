//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.geometry.standard.points.PackedPoints;
import org.deegree.geometry.standard.points.PointsArray;
import org.deegree.geometry.standard.points.PointsList;

/**
 * Contains utility methods for common tasks on {@link Geometry} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Geometries {

    private final static GeometryFactory fac = new GeometryFactory();

    /**
     * Homogenizes the given generic {@link org.deegree.geometry.multi.MultiGeometry}, i.e. returns a
     * {@link org.deegree.geometry.multi.MultiPoint}, {@link org.deegree.geometry.multi.MultiCurve},
     * {@link org.deegree.geometry.multi.MultiLineString}, {@link org.deegree.geometry.multi.MultiSurface},
     * {@link org.deegree.geometry.multi.MultiPolygon} or {@link org.deegree.geometry.multi.MultiSolid} (depending on
     * the members).
     * 
     * @param geometry
     *            generic multi geometry to be homogenized, must not be <code>null</code>
     * @return the homogenized multi geometry
     */
    @SuppressWarnings("unchecked")
    public static MultiGeometry<?> homogenize( MultiGeometry<?> geometry ) {

        MultiGeometry<?> homogenized = null;

        List<?> deepMembers = new ArrayList<Object>( geometry.size() );
        for ( Geometry member : geometry ) {
            collectMembersDeep( member, (List<GeometricPrimitive>) deepMembers );
        }

        // check if all members have the same dimension
        int dim = getDimension( deepMembers );

        switch ( dim ) {
        case -1: {
            homogenized = new GeometryFactory().createMultiPoint( geometry.getId(), geometry.getCoordinateSystem(),
                                                                  Collections.EMPTY_LIST );
            break;
        }
        case 0: {
            homogenized = new GeometryFactory().createMultiPoint( geometry.getId(), geometry.getCoordinateSystem(),
                                                                  (List<Point>) deepMembers );
            break;
        }
        case 1: {
            homogenized = new GeometryFactory().createMultiLineString( geometry.getId(),
                                                                       geometry.getCoordinateSystem(),
                                                                       (List<LineString>) deepMembers );
            break;
        }
        case 2: {
            homogenized = new GeometryFactory().createMultiPolygon( geometry.getId(), geometry.getCoordinateSystem(),
                                                                    (List<Polygon>) deepMembers );
            break;
        }
        default: {
            String msg = "Cannot homogenize MultiGeometry: contains members with dimension " + dim + ".";
            throw new IllegalArgumentException( msg );
        }
        }
        return homogenized;
    }

    private static int getDimension( List<?> deepMembers ) {
        int dim = -1;
        for ( Object o : deepMembers ) {
            GeometricPrimitive member = (GeometricPrimitive) o;
            int memberDim = -1;
            switch ( member.getPrimitiveType() ) {
            case Point: {
                memberDim = 0;
                break;
            }
            case Curve: {
                memberDim = 1;
                break;
            }
            case Surface: {
                memberDim = 2;
                break;
            }
            case Solid: {
                memberDim = 3;
                break;
            }
            }
            if ( dim == -1 ) {
                dim = memberDim;
            } else if ( dim != memberDim ) {
                String msg = "Cannot homogenize MultiGeometry: contains members with dimension " + dim + " and "
                             + memberDim + ".";
                throw new IllegalArgumentException( msg );
            }
        }
        return dim;
    }

    private static void collectMembersDeep( Geometry geometry, List<GeometricPrimitive> deepMembers ) {
        switch ( geometry.getGeometryType() ) {
        case COMPOSITE_GEOMETRY: {
            // TODO
            break;
        }
        case ENVELOPE: {
            // TODO
            break;
        }
        case PRIMITIVE_GEOMETRY: {
            deepMembers.add( (GeometricPrimitive) geometry );
        }
        case MULTI_GEOMETRY: {
            MultiGeometry<?> multi = (MultiGeometry<?>) geometry;
            for ( Geometry member : multi ) {
                collectMembersDeep( member, deepMembers );
            }
        }
        }
    }

    /**
     * Returns a corresponding {@link GeometricPrimitive} object for the given {@link Envelope}.
     * <p>
     * Depending on the extent, this can either be:
     * <ul>
     * <li>a {@link Polygon}</li>
     * <li>a {@link LineString}</li>
     * <li>a {@link Point}</li>
     * </ul>
     * </p>
     * 
     * @param env
     *            envelope, must not be <code>null</code>
     * @return corresponding geometry object, never <code>null</code>
     */
    public static GeometricPrimitive getAsGeometry( Envelope env ) {
        Point min = env.getMin();
        Point max = env.getMax();
        ICRS crs = env.getCoordinateSystem();
        if ( min.equals( max ) ) {
            return fac.createPoint( null, min.getAsArray(), crs );
        }
        if ( env.getCoordinateDimension() == 2 ) {
            if ( min.get0() == max.get0() || min.get1() == max.get1() ) {
                Points points = new PointsArray( min, max );
                return fac.createLineString( null, crs, points );
            }
            double[] points = new double[] { min.get0(), min.get1(), max.get0(), min.get1(), max.get0(), max.get1(),
                                            min.get0(), max.get1(), min.get0(), min.get1() };
            Curve ls = fac.createLineString( null, crs, new PackedPoints( null, points, 2 ) );
            Ring exteriorRing = fac.createRing( null, crs, Collections.singletonList( ls ) );
            return fac.createPolygon( null, crs, exteriorRing, null );
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a deep copy of the given {@link Geometry} object.
     * 
     * @param geom
     * @return
     */
    public static Geometry copyDeep( Geometry geom ) {
        // TODO implement this without JTS
        org.locationtech.jts.geom.Geometry jtsGeom = ( (AbstractDefaultGeometry) geom ).getJTSGeometry();
        return ( (AbstractDefaultGeometry) geom ).createFromJTS( jtsGeom, geom.getCoordinateSystem() );
    }

    /**
     * Samples points on a line string using the specified distance.
     * 
     * @param linestring
     * @param distance
     * @return the sampled points
     */
    public static Points sampleLineString( LineString linestring, int distance ) {
        GeometryFactory fac = new GeometryFactory();
        ICRS crs = linestring.getCoordinateSystem();
        List<Point> list = new ArrayList<Point>();

        double lastx = Double.NaN, lasty = Double.NaN;
        for ( Point p : linestring.getControlPoints() ) {
            double x = p.get0();
            double y = p.get1();
            if ( Double.isNaN( lastx ) ) {
                lastx = x;
                lasty = y;
                list.add( fac.createPoint( null, x, y, crs ) );
                continue;
            }
            double dx = x - lastx;
            double dy = y - lasty;
            double len = Math.sqrt( dx * dx + dy * dy );
            dx /= len;
            dy /= len;
            double newx = lastx + dx * distance;
            double newy = lasty + dy * distance;

            int num = (int) Math.floor( len / distance );

            for ( int i = 0; i < num; ++i ) {
                list.add( fac.createPoint( null, newx, newy, crs ) );
                newx += dx * distance;
                newy += dy * distance;
            }
            list.add( fac.createPoint( null, x, y, crs ) );
            lastx = x;
            lasty = y;
        }

        return new PointsList( list );
    }

}
