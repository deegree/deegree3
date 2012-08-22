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

package org.deegree.framework.util;

import static org.deegree.framework.log.LoggerFactory.getLogger;

import static org.deegree.framework.util.CollectionUtils.map;
import static org.deegree.model.spatialschema.GeometryFactory.createCurve;
import static org.deegree.model.spatialschema.GeometryFactory.createCurveSegment;
import static org.deegree.model.spatialschema.GeometryFactory.createEnvelope;
import static org.deegree.model.spatialschema.GeometryFactory.createPoint;
import static org.deegree.model.spatialschema.GeometryFactory.createPosition;
import static org.deegree.model.spatialschema.GeometryFactory.createSurface;
import static org.deegree.model.spatialschema.GeometryFactory.createSurfacePatch;

import java.util.ArrayList;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.util.CollectionUtils.Mapper;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Aggregate;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.CurveSegment;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.JTSAdapter;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Ring;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.SurfaceInterpolationImpl;
import org.deegree.model.spatialschema.SurfacePatch;

import com.vividsolutions.jts.algorithm.CGAlgorithms;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeometryUtils {

    private static final ILogger LOG = getLogger( GeometryUtils.class );

    /**
     * 
     * @param p1
     * @param p2
     * @return distance between two 2D positions
     */
    public static double distance( Position p1, Position p2 ) {
        return distance( p1.getX(), p1.getY(), p2.getX(), p2.getY() );
    }

    /**
     * 
     * @param xx
     * @param yy
     * @param xx_
     * @param yy_
     * @return distance between two points
     */
    public static double distance( double xx, double yy, double xx_, double yy_ ) {
        double dx = xx - xx_;
        double dy = yy - yy_;
        return Math.sqrt( dx * dx + dy * dy );
    }

    /**
     * @param <T>
     * @param geom
     * @param x
     * @param y
     * @return moves 2D geometries only (will discard any z values
     */
    @SuppressWarnings("unchecked")
    public static <T> T move( T geom, double x, double y ) {
        final Mapper<Position, Position> mover = getMover( x, y );
        if ( geom instanceof Envelope ) {
            Envelope e = (Envelope) geom;
            // dammit, where's the proper typecase?
            return (T) createEnvelope( move( e.getMin(), x, y ), move( e.getMax(), x, y ), e.getCoordinateSystem() );
        }
        if ( geom instanceof Point ) {
            Point p = (Point) geom;
            return (T) createPoint( p.getX() + x, p.getY() + y, p.getCoordinateSystem() );
        }
        if ( geom instanceof Curve ) {
            Curve c = (Curve) geom;
            try {
                return (T) createCurve( map( c.getCurveSegments(), GeometryUtils.<CurveSegment> getMover( x, y ) ),
                                        c.getCoordinateSystem() );
            } catch ( GeometryException e ) {
                LOG.logError( "Unknown error", e );
                return null;
            }
        }
        if ( geom instanceof CurveSegment ) {
            CurveSegment c = (CurveSegment) geom;
            try {
                return (T) createCurveSegment( map( c.getPositions(), mover ), c.getCoordinateSystem() );
            } catch ( GeometryException e ) {
                LOG.logError( "Unknown error", e );
                return null;
            }
        }
        if ( geom instanceof Position ) {
            Position p = (Position) geom;
            return (T) createPosition( p.getX() + x, p.getY() + y );
        }
        if ( geom instanceof Surface ) {
            Surface s = (Surface) geom;
            SurfacePatch[] patches = new SurfacePatch[s.getNumberOfSurfacePatches()];
            for ( int i = 0; i < patches.length; ++i ) {
                try {
                    patches[i] = move( s.getSurfacePatchAt( 0 ), x, y );
                } catch ( GeometryException e ) {
                    LOG.logError( "Unknown error", e );
                    return null;
                }
            }
            try {
                return (T) createSurface( patches, s.getCoordinateSystem() );
            } catch ( GeometryException e ) {
                LOG.logError( "Unknown error", e );
                return null;
            }
        }
        if ( geom instanceof SurfacePatch ) {
            SurfacePatch p = (SurfacePatch) geom;
            Position[] ring = p.getExteriorRing();
            Position[] outer = map( ring, mover ).toArray( new Position[ring.length] );
            Position[][] inners = p.getInteriorRings();
            if ( inners != null ) {
                for ( int i = 0; i < inners.length; ++i ) {
                    inners[i] = map( inners[i], mover ).toArray( new Position[inners[i].length] );
                }
            }
            try {
                return (T) createSurfacePatch( outer, inners, p.getInterpolation(), p.getCoordinateSystem() );
            } catch ( GeometryException e ) {
                LOG.logError( "Unknown error", e );
                return null;
            }
        }
        if ( geom instanceof Aggregate ) {
            Aggregate m = (Aggregate) geom;
            for ( int i = 0; i < m.getSize(); ++i ) {
                try {
                    m.setObjectAt( move( m.getObjectAt( i ), x, y ), i );
                } catch ( GeometryException e ) {
                    LOG.logError( "Unknown error", e );
                    return null;
                }
            }
            return (T) m;
        }

        throw new UnsupportedOperationException( "Moving geometries of class " + geom.getClass() + " is not supported." );
    }

    /**
     * @param <T>
     * @param x
     * @param y
     * @return a move mapper wrapper
     */
    public static <T> Mapper<T, T> getMover( final double x, final double y ) {
        return new Mapper<T, T>() {
            public T apply( T u ) {
                return move( u, x, y );
            }
        };
    }

    /**
     * 
     * @param surface
     * @return surface with inverted order of vertices
     * @throws GeometryException
     */
    public static Surface invertOrder( Surface surface )
                            throws GeometryException {
        Position[] exring = surface.getSurfaceBoundary().getExteriorRing().getPositions();
        // invert exterior ring vertices order
        for ( int i = 0; i < exring.length / 2; i++ ) {
            Position p = exring[i];
            exring[i] = exring[exring.length - 1 - i];
            exring[exring.length - 1 - i] = p;
        }
        Ring[] inner = surface.getSurfaceBoundary().getInteriorRings();
        Position[][] inPos = new Position[inner.length][];
        // invert interior ring vertices order
        for ( int i = 0; i < inner.length; i++ ) {
            Position[] tmp = inner[i].getPositions();
            for ( int j = 0; j < tmp.length; j++ ) {
                Position p = tmp[j];
                tmp[j] = tmp[tmp.length - 1 - j];
                tmp[tmp.length - 1 - j] = p;
            }
            inPos[i] = tmp;
        }
        return GeometryFactory.createSurface( exring, inPos, new SurfaceInterpolationImpl(),
                                              surface.getCoordinateSystem() );
    }

    /**
     * 
     * @param curve
     * @return curve with inverted order of vertices for each segment
     * @throws GeometryException
     */
    public static Curve invertOrder( Curve curve )
                            throws GeometryException {
        CurveSegment[] segments = curve.getCurveSegments();
        for ( int i = 0; i < segments.length; i++ ) {
            Position[] tmp = segments[i].getPositions();
            for ( int j = 0; j < tmp.length; j++ ) {
                Position p = tmp[j];
                tmp[j] = tmp[tmp.length - 1 - j];
                tmp[tmp.length - 1 - j] = p;
            }
            segments[i] = GeometryFactory.createCurveSegment( tmp, curve.getCoordinateSystem() );
        }
        return GeometryFactory.createCurve( segments );
    }

    /**
     * 
     * @param surface
     * @return true if an array of passed {@link Position} forms a clockwise orientated ring
     */
    public static boolean isClockwise( Surface surface ) {
        Position[] ring = surface.getSurfaceBoundary().getExteriorRing().getPositions();
        return !CGAlgorithms.isCCW( JTSAdapter.export( ring ).getCoordinates() );
    }

    /**
     * 
     * @param geom
     * @return surface or multi surface with guaranteed clockwise vertices orientation
     * @throws GeometryException
     */
    public static Geometry ensureClockwise( Geometry geom )
                            throws GeometryException {
        if ( geom instanceof Surface ) {
            if ( !isClockwise( (Surface) geom ) ) {
                geom = invertOrder( (Surface) geom );
            }
        } else if ( geom instanceof MultiSurface ) {
            Surface[] surfaces = ( (MultiSurface) geom ).getAllSurfaces();
            for ( int i = 0; i < surfaces.length; i++ ) {
                surfaces[i] = invertOrder( surfaces[i] );
            }
            geom = GeometryFactory.createMultiSurface( surfaces );
        }
        return geom;
    }

    /**
     * 
     * @param distance
     *            if distance is < 0 left parallel will be created
     * @param curve
     * @return parallel curve with distance.
     * @throws GeometryException
     */
    // TODO
    // remove arte facts occuring if distance between at least two vertices is less passed distance
    public static Curve createCurveParallel( double distance, Curve curve )
                            throws GeometryException {
        Position[] pos = curve.getAsLineString().getPositions();

        List<Position[]> posList = new ArrayList<Position[]>( pos.length );
        // create parallel for each segment
        for ( int j = 0; j < pos.length - 1; j++ ) {
            // calculate normal vector
            // swap x and y and change sign
            double nx = -( pos[j].getY() - pos[j + 1].getY() );
            double ny = pos[j].getX() - pos[j + 1].getX();
            double nl = Math.sqrt( nx * nx + ny * ny );
            nx = nx / nl * distance;
            ny = ny / nl * distance;
            Position[] p = new Position[2];
            p[0] = GeometryFactory.createPosition( pos[j].getX() + nx, pos[j].getY() + ny );
            p[1] = GeometryFactory.createPosition( pos[j + 1].getX() + nx, pos[j + 1].getY() + ny );
            posList.add( p );
        }
        List<Position> pList = new ArrayList<Position>( pos.length );
        // first point
        pList.add( posList.get( 0 )[0] );
        for ( int j = 0; j < posList.size() - 1; j++ ) {
            // find intersection point between j'th and j+1'th segment
            Position is = intersection( posList.get( j )[0], posList.get( j )[1], posList.get( j + 1 )[0],
                                        posList.get( j + 1 )[1] );
            if ( is != null ) {
                // is == null if to segments are parallel or part of the same line
                pList.add( is );
            }
        }
        // last point
        pList.add( posList.get( posList.size() - 1 )[1] );
        curve = GeometryFactory.createCurve( pList.toArray( new Position[pList.size()] ), curve.getCoordinateSystem() );
        return curve;
    }

    /**
     * 
     * @param startPoint1
     * @param endPoint1
     * @param startPoint2
     * @param endPoint2
     * @return intersection coordinates between to lines (not line segments!!!). This means the intersection point may
     *         not lies between passed start- and end-points
     */
    public static Position intersection( Position startPoint1, Position endPoint1, Position startPoint2,
                                         Position endPoint2 ) {
        double m1 = ( endPoint1.getY() - startPoint1.getY() ) / ( endPoint1.getX() - startPoint1.getX() );
        double m2 = ( endPoint2.getY() - startPoint2.getY() ) / ( endPoint2.getX() - startPoint2.getX() );

        // lines are parallels
        if ( m1 == m2 ) {
            return null;
        }

        double t1 = -( m1 * startPoint1.getX() - startPoint1.getY() );
        double t2 = -( m2 * startPoint2.getX() - startPoint2.getY() );
        double x = ( t2 - t1 ) / ( m1 - m2 );
        double y = m1 * x + t1;
        return GeometryFactory.createPosition( x, y );
    }

    /**
     * 
     * @param a1
     *            the first point
     * @param a2
     *            the second point
     * @param l
     *            the length of the segment starting from the second point
     * @param alpha
     *            the angle that the a1-a2 segment makes the following segment
     * @return the point found at length l from a2 and which (connected with a2) forms an angle of alpha to a1a2.
     */
    public static Point vectorByAngle( Point a1, Point a2, double l, double alpha, boolean useAbsoluteAngle ) {
        double beta = Math.atan2( a1.getY() - a2.getY(), a1.getX() - a2.getX() );

        double absoluteAngle;
        if ( useAbsoluteAngle ) {
            if ( alpha >= 0 ) {
                absoluteAngle = beta + alpha;
            } else {
                absoluteAngle = beta - alpha;
            }
        } else {
            absoluteAngle = beta - alpha;
        }
        return GeometryFactory.createPoint( a2.getX() + l * Math.cos( absoluteAngle ), a2.getY() + l
                                                                                       * Math.sin( absoluteAngle ),
                                            a1.getCoordinateSystem() );
    }

    /**
     * 
     * @param center
     * @param r
     * @param noOfPos
     * @param startPosition
     * @param endPosition
     * @return
     * @throws GeometryException
     */
    public static Curve calcCircleCoordinates( Position center, double r, int nSeg, Position startPosition,
                                               Position endPosition, CoordinateSystem crs )
                            throws GeometryException {
        Curve curve = null;
        if ( startPosition != null && endPosition != null ) {
            double startCos = ( startPosition.getX() - center.getX() ) / r;
            double startSin = ( startPosition.getY() - center.getY() ) / r;
            double startArc = getArc( startSin, startCos );
            double endCos = ( endPosition.getX() - center.getX() ) / r;
            double endSin = ( endPosition.getY() - center.getY() ) / r;
            double endArc = getArc( endSin, endCos );

            if ( startArc > endArc ) {
                double t = startArc;
                startArc = endArc;
                endArc = t;
            }
            if ( endArc - startArc > 180 ) {
                double t = startArc;
                startArc = endArc;
                endArc = 360 + t;
            }

            curve = GeometryFactory.createCurveAsArc( center.getX(), center.getY(), r, r, nSeg, startArc, endArc, crs );
            List<Position> l = null;
            // ensure that curve starts/ends at start/end point of lines
            if ( !curve.getEndPoint().getPosition().equals( startPosition ) ) {
                Position[] p = curve.getAsLineString().getPositions();
                l = new ArrayList<Position>( p.length + 1 );
                for ( Position position : p ) {
                    l.add( position );
                }
                if ( GeometryUtils.distance( p[p.length - 1], startPosition ) < GeometryUtils.distance(
                                                                                                        p[p.length - 1],
                                                                                                        endPosition ) ) {
                    l.add( startPosition );
                } else {
                    l.add( endPosition );
                }
                curve = GeometryFactory.createCurve( l.toArray( new Position[l.size()] ), curve.getCoordinateSystem() );
            }
            if ( !curve.getEndPoint().getPosition().equals( endPosition ) ) {
                Position[] p = curve.getAsLineString().getPositions();
                l = new ArrayList<Position>( p.length + 1 );
                for ( Position position : p ) {
                    l.add( position );
                }
                if ( GeometryUtils.distance( p[p.length - 1], startPosition ) < GeometryUtils.distance(
                                                                                                        p[p.length - 1],
                                                                                                        endPosition ) ) {
                    l.add( startPosition );
                } else {
                    l.add( endPosition );
                }
                curve = GeometryFactory.createCurve( l.toArray( new Position[l.size()] ), curve.getCoordinateSystem() );
            }
            if ( !curve.getStartPoint().getPosition().equals( startPosition )
                 && curve.getStartPoint().getPosition().equals( endPosition ) ) {
                Position[] p = curve.getAsLineString().getPositions();
                l = new ArrayList<Position>( p.length + 1 );
                for ( Position position : p ) {
                    l.add( position );
                }
                if ( GeometryUtils.distance( p[0], startPosition ) < GeometryUtils.distance( p[0], endPosition ) ) {
                    l.add( 0, startPosition );
                } else {
                    l.add( 0, endPosition );
                }
                curve = GeometryFactory.createCurve( l.toArray( new Position[l.size()] ), curve.getCoordinateSystem() );
            }

        }
        return curve;
    }

    private static double getArc( double sin, double cos ) {
        double atan = Math.atan2( sin, cos );
        return Math.toDegrees( atan ) + 90;
    }

    /**
     * 
     * @param p0x
     * @param p0y
     * @param p1x
     * @param p1y
     * @param p2x
     * @param p2y
     * @return arc between two line segments where p0x/p0y is common point of both segments
     */
    public static double getArc( double p0x, double p0y, double p1x, double p1y, double p2x, double p2y ) {
        double d1 = GeometryUtils.distance( p0x, p0y, p1x, p1y );
        double d2 = GeometryUtils.distance( p2x, p2y, p1x, p1y );
        double d3 = GeometryUtils.distance( p2x, p2y, p0x, p0y );
        double rad = 180 / Math.PI;
        double s = ( d1 + d2 + d3 ) / 2d;
        return rad * 2 * Math.asin( Math.sqrt( ( s - d1 ) * ( s - d3 ) / d1 / d3 ) );
    }

    /**
     * 
     * @param p0x
     * @param p0y
     * @param p1x
     * @param p1y
     * @param p2x
     * @param p2y
     * @return true if p2x/p2y is left of the line defined by p0x/p0y p1x/p1y
     */
    public static boolean isLeft( double p0x, double p0y, double p1x, double p1y, double p2x, double p2y ) {
        double p = ( p2x - p0x ) * ( p0y - p1y ) + ( p2y - p0y ) * ( p1x - p0x );
        return ( p > 0 );
    }

}
