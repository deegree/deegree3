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

package org.deegree.commons.utils;

import static java.awt.geom.PathIterator.SEG_CLOSE;
import static java.awt.geom.PathIterator.SEG_CUBICTO;
import static java.awt.geom.PathIterator.SEG_LINETO;
import static java.awt.geom.PathIterator.SEG_MOVETO;
import static java.awt.geom.PathIterator.SEG_QUADTO;
import static java.lang.Math.sqrt;

import java.awt.Shape;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.standard.points.PointsList;
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
        GeometryFactory fac = new GeometryFactory();
        if ( geom instanceof Point ) {
            Point p = (Point) geom;
            return fac.createPoint( geom.getId(), new double[] { p.get0() + offx, p.get1() + offy },
                                    p.getCoordinateSystem() );
        }
        if ( geom instanceof Curve ) {
            Curve c = (Curve) geom;
            LinkedList<Point> ps = new LinkedList<Point>();
            for ( Point p : c.getAsLineString().getControlPoints() ) {
                ps.add( (Point) move( p, offx, offy ) );
            }
            return fac.createLineString( geom.getId(), c.getCoordinateSystem(), new PointsList( ps ) );
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

    private static Points move( Points points, double offx, double offy ) {
        List<Point> movedPoints = new ArrayList<Point>( points.size() );
        GeometryFactory fac = new GeometryFactory();
        for ( Point point : points ) {
            double[] movedCoordinates = new double[] { point.get0() + offx, point.get1() + offy };
            movedPoints.add( fac.createPoint( point.getId(), movedCoordinates, point.getCoordinateSystem() ) );
        }
        return new PointsList( movedPoints );
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
                sb.append( ", close]" );
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

    /**
     * Converts the given Envelope into an envelope with the given coordinate system. Basically this is a delegate call
     * to the {@link GeometryTransformer}.
     * 
     * @param sourceEnvelope
     *            to convert
     * @param targetCRS
     * @return the target Envelope
     * @throws TransformationException
     * @throws TransformationException
     *             if the transformation between the source and target crs cannot be created.
     */
    public static Envelope createConvertedEnvelope( Envelope sourceEnvelope, CRS targetCRS )
                            throws TransformationException {
        Envelope result = sourceEnvelope;
        if ( !sourceEnvelope.getCoordinateSystem().equals( targetCRS ) ) {
            try {
                result = (Envelope) new GeometryTransformer( targetCRS.getWrappedCRS() ).transform( sourceEnvelope );
            } catch ( IllegalArgumentException e ) {
                throw new TransformationException( "Could not transform to given envelope because: "
                                                   + e.getLocalizedMessage(), e );
            } catch ( UnknownCRSException e ) {
                throw new TransformationException( "Could not transform to given envelope because: "
                                                   + e.getLocalizedMessage(), e );
            }
        }
        return result;
    }

}
