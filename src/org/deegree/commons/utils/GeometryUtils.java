//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
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

package org.deegree.commons.utils;

import static java.awt.geom.PathIterator.SEG_CLOSE;
import static java.awt.geom.PathIterator.SEG_CUBICTO;
import static java.awt.geom.PathIterator.SEG_LINETO;
import static java.awt.geom.PathIterator.SEG_MOVETO;
import static java.awt.geom.PathIterator.SEG_QUADTO;
import static java.lang.Math.sqrt;
import static org.deegree.model.geometry.GeometryFactoryCreator.getInstance;

import java.awt.Shape;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.LinearRing;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Ring;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.surfacepatches.PolygonPatch;
import org.deegree.model.geometry.primitive.surfacepatches.SurfacePatch;
import org.deegree.rendering.r2d.strokes.TextStroke;

/**
 * <code>GeometryUtils</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeometryUtils {

    /**
     * Moves the coordinates of a geometry.
     * 
     * @param geom
     *            use only surfaces, line strings or points, and only with dim == 2
     * @param offx
     * @param offy
     * @return the moved geometry
     */
    public static Geometry move( Geometry geom, double offx, double offy ) {
        GeometryFactory fac = getInstance().getGeometryFactory();
        if ( geom instanceof Point ) {
            Point p = (Point) geom;
            return fac.createPoint( geom.getId(), new double[] { p.getX() + offx, p.getY() + offy },
                                    p.getCoordinateSystem() );
        }
        if ( geom instanceof Curve ) {
            Curve c = (Curve) geom;
            LinkedList<Point> ps = new LinkedList<Point>();
            for ( Point p : c.getAsLineString().getControlPoints() ) {
                ps.add( (Point) move( p, offx, offy ) );
            }
            return fac.createLineString( geom.getId(), c.getCoordinateSystem(), ps );
        }
        if ( geom instanceof Surface ) {
            Surface s = (Surface) geom;
            LinkedList<SurfacePatch> movedPatches = new LinkedList<SurfacePatch>();
            for ( SurfacePatch patch : s.getPatches() ) {
                if ( patch instanceof PolygonPatch ) {
                    Ring exterior = ( (PolygonPatch) patch ).getExteriorRing();
                    LinearRing movedExteriorRing = null;
                    if ( exterior != null ) {
                        movedExteriorRing = fac.createLinearRing( exterior.getId(), exterior.getCoordinateSystem(),
                                                                  move( exterior.getAsLineString().getControlPoints(),
                                                                        offx, offy ) );
                    }
                    List<Ring> interiorRings = ( (PolygonPatch) patch ).getInteriorRings();
                    List<Ring> movedInteriorRings = new ArrayList<Ring>( interiorRings.size() );
                    for ( Ring interior : interiorRings ) {
                        movedInteriorRings.add( fac.createLinearRing(
                                                                      interior.getId(),
                                                                      interior.getCoordinateSystem(),
                                                                      move(
                                                                            interior.getAsLineString().getControlPoints(),
                                                                            offx, offy ) ) );
                    }
                    movedPatches.add( fac.createPolygonPatch( movedExteriorRing, movedInteriorRings ) );
                } else {
                    throw new UnsupportedOperationException( "Cannot move non-planar surface patches." );
                }
            }
            return fac.createSurface( geom.getId(), movedPatches, geom.getCoordinateSystem() );
        }
        return geom;
    }

    private static List<Point> move( List<Point> points, double offx, double offy ) {
        List<Point> movedPoints = new ArrayList<Point>( points.size() );
        GeometryFactory fac = getInstance().getGeometryFactory();
        for ( Point point : points ) {
            double[] movedCoordinates = new double[] { point.getX() + offx, point.getY() + offy };
            movedPoints.add( fac.createPoint( point.getId(), movedCoordinates, point.getCoordinateSystem() ) );
        }
        return movedPoints;
    }

    /**
     * @param shape
     * @return a string representation of the shape
     */
    public static String prettyPrintShape( Shape shape ) {
        StringBuilder sb = new StringBuilder();
        PathIterator iter = shape.getPathIterator( null );
        double[] coords = new double[6];
        boolean closed = false;

        while ( !iter.isDone() ) {
            switch ( iter.currentSegment( coords ) ) {
            case SEG_CLOSE:
                sb.append( "]" );
                closed = true;
                break;
            case SEG_CUBICTO:
                sb.append( ", cubic to [" );
                sb.append( coords[0] + ", " );
                sb.append( coords[1] + ", " );
                sb.append( coords[2] + ", " );
                sb.append( coords[3] + ", " );
                sb.append( coords[4] + ", " );
                sb.append( coords[5] + "]" );
                break;
            case SEG_LINETO:
                sb.append( ", line to [" );
                sb.append( coords[0] + ", " );
                sb.append( coords[1] + "]" );
                break;
            case SEG_MOVETO:
                sb.append( "[move to [" );
                sb.append( coords[0] + ", " );
                sb.append( coords[1] + "]" );
                closed = false;
                break;
            case SEG_QUADTO:
                sb.append( ", quadratic to [" );
                sb.append( coords[0] + ", " );
                sb.append( coords[1] + ", " );
                sb.append( coords[2] + ", " );
                sb.append( coords[3] + "]" );
                break;
            }
            iter.next();
        }

        if ( !closed ) {
            sb.append( "]" );
        }
        return sb.toString();
    }

    /**
     * This method flattens the path with a flatness parameter of 1.
     * 
     * @author Jerry Huxtable
     * @see TextStroke
     * @param shape
     * @return the path segment lengths
     */
    public static LinkedList<Double> measurePathLengths( Shape shape ) {
        PathIterator it = new FlatteningPathIterator( shape.getPathIterator( null ), 1 );
        double points[] = new double[6];
        double moveX = 0, moveY = 0;
        double lastX = 0, lastY = 0;
        double thisX = 0, thisY = 0;
        int type = 0;
        LinkedList<Double> res = new LinkedList<Double>();

        while ( !it.isDone() ) {
            type = it.currentSegment( points );
            switch ( type ) {
            case PathIterator.SEG_MOVETO:
                moveX = lastX = points[0];
                moveY = lastY = points[1];
                break;

            case PathIterator.SEG_CLOSE:
                points[0] = moveX;
                points[1] = moveY;
                // Fall into....

            case PathIterator.SEG_LINETO:
                thisX = points[0];
                thisY = points[1];
                double dx = thisX - lastX;
                double dy = thisY - lastY;
                res.add( sqrt( dx * dx + dy * dy ) );
                lastX = thisX;
                lastY = thisY;
                break;
            }
            it.next();
        }

        return res;
    }

}
