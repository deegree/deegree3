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

package org.deegree.commons.dataaccess.shape;

import static org.deegree.commons.utils.ByteUtils.readLEDouble;
import static org.deegree.commons.utils.ByteUtils.readLEInt;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.index.SpatialIndex;
import org.deegree.commons.utils.Pair;
import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.standard.points.PackedPoints;
import org.deegree.geometry.standard.points.PointsList;
import org.slf4j.Logger;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * <code>SHPReader</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SHPReader {

    /**
     * The file type number.
     */
    public static final int FILETYPE = 9994;

    /**
     * The shape file version.
     */
    public static final int VERSION = 1000;

    /**
     * The NULL shape.
     */
    public static final int NULL = 0;

    /**
     * The normal point.
     */
    public static final int POINT = 1;

    /**
     * The normal polyline.
     */
    public static final int POLYLINE = 3;

    /**
     * The normal polygon.
     */
    public static final int POLYGON = 5;

    /**
     * The normal multipoint.
     */
    public static final int MULTIPOINT = 8;

    /**
     * The point with z coordinates.
     */
    public static final int POINTZ = 11;

    /**
     * The polyline with z coordinates.
     */
    public static final int POLYLINEZ = 13;

    /**
     * The polygon with z coordinates.
     */
    public static final int POLYGONZ = 15;

    /**
     * The multipoint with z coordinates.
     */
    public static final int MULTIPOINTZ = 18;

    /**
     * The point with measure.
     */
    public static final int POINTM = 21;

    /**
     * The polyline with measures.
     */
    public static final int POLYLINEM = 23;

    /**
     * The polygon with measures.
     */
    public static final int POLYGONM = 25;

    /**
     * The multipoint with measures.
     */
    public static final int MULTIPOINTM = 28;

    /**
     * The multipatch shape.
     */
    public static final int MULTIPATCH = 31;

    /**
     * Triangle strip part type.
     */
    public static final int TRIANGLE_STRIP = 0;

    /**
     * Triangle fan part type.
     */
    public static final int TRIANGLE_FAN = 1;

    /**
     * Outer polygon ring part type.
     */
    public static final int OUTER_RING = 2;

    /**
     * Inner polygon ring part type.
     */
    public static final int INNER_RING = 3;

    /**
     * First ring of a polygon part type.
     */
    public static final int FIRST_RING = 4;

    /**
     * Polygon ring part type.
     */
    public static final int RING = 5;

    private static final Logger LOG = getLogger( SHPReader.class );

    private static final GeometryFactory fac = new GeometryFactory();

    private double[] envelope;

    private int type;

    private final RandomAccessFile in;

    private final CRS crs;

    private Envelope bbox;

    private SpatialIndex<Long> rtree;

    /**
     * @param in
     * @param crs
     * @param rtree
     * @throws IOException
     */
    public SHPReader( RandomAccessFile in, CRS crs, SpatialIndex<Long> rtree ) throws IOException {
        this.in = in;
        this.crs = crs;
        this.rtree = rtree;
        if ( in.readInt() != FILETYPE ) {
            LOG.warn( "File type is wrong, unexpected things might happen, continuing anyway..." );
        }

        in.seek( 24 );
        int length = in.readInt() * 2; // 16 bit words...

        LOG.trace( "Length {}", length );

        // whyever they mix byte orders?
        int version = readLEInt( in );

        if ( version != VERSION ) {
            LOG.warn( "File version is wrong, continuing in the hope of compatibility..." );
        }

        type = readLEInt( in );
        if ( LOG.isTraceEnabled() ) {
            switch ( type ) {
            case NULL:
                LOG.trace( "NULL" );
                break;
            case POINT:
                LOG.trace( "POINT" );
                break;
            case POLYLINE:
                LOG.trace( "POLYLINE" );
                break;
            case POLYGON:
                LOG.trace( "POLYGON" );
                break;
            case MULTIPOINT:
                LOG.trace( "MULTIPOINT" );
                break;
            case POINTM:
                LOG.trace( "POINTM" );
                break;
            case POLYLINEM:
                LOG.trace( "POLYLINEM" );
                break;
            case POLYGONM:
                LOG.trace( "POLYGONM" );
                break;
            case MULTIPOINTM:
                LOG.trace( "MULTIPOINTM" );
                break;
            case POINTZ:
                LOG.trace( "POINTZ" );
                break;
            case POLYLINEZ:
                LOG.trace( "POLYLINEZ" );
                break;
            case POLYGONZ:
                LOG.trace( "POLYGONZ" );
                break;
            case MULTIPOINTZ:
                LOG.trace( "MULTIPOINTZ" );
                break;
            case MULTIPATCH:
                LOG.trace( "MULTIPATCH" );
                break;

            }
        }

        envelope = new double[8];
        envelope[0] = readLEDouble( in );
        envelope[2] = readLEDouble( in );
        envelope[1] = readLEDouble( in );
        envelope[3] = readLEDouble( in );
        envelope[4] = readLEDouble( in );
        envelope[5] = readLEDouble( in );
        envelope[6] = readLEDouble( in );
        envelope[7] = readLEDouble( in );

        // TODO do this for 3D as well
        bbox = fac.createEnvelope( envelope[0], envelope[2], envelope[1], envelope[3], crs );

        if ( LOG.isTraceEnabled() ) {
            LOG.trace( "Envelope: " + envelope[0] + "," + envelope[1] + " " + envelope[2] + "," + envelope[3] + " "
                       + envelope[4] + "," + envelope[5] + " " + envelope[6] + "," + envelope[7] );
        }

    }

    private static final void maybeAddPair( int num, Geometry g, boolean withGeometry, boolean exact,
                                            List<Pair<Integer, Geometry>> list, Envelope bbox ) {
        if ( exact ) {
            if ( bbox.intersects( g ) ) {
                list.add( new Pair<Integer, Geometry>( num, withGeometry ? g : null ) );
            }
        } else {
            list.add( new Pair<Integer, Geometry>( num, g ) );
        }
    }

    /**
     * @param bbox
     * @param withGeometry
     * @param exact
     * @return the list of contained geometries
     * @throws IOException
     */
    public LinkedList<Pair<Integer, Geometry>> query( Envelope bbox, boolean withGeometry, boolean exact )
                            throws IOException {

        LOG.debug( "Querying shp with bbox {}", bbox );

        LinkedList<Pair<Integer, Geometry>> list = new LinkedList<Pair<Integer, Geometry>>();

        List<Long> pointers = (List<Long>) rtree.query( bbox );
        for ( Long ptr : pointers ) {
            in.seek( ptr - 8 );

            int num = in.readInt() - 1;

            if ( !withGeometry && !exact ) {
                list.add( new Pair<Integer, Geometry>( num, null ) );
                continue;
            }

            int length = in.readInt() * 2; // bah, 16 bit length units here as well!

            int type = readLEInt( in );
            switch ( type ) {
            case NULL:
                continue;
            case POINT: {
                Point p = readPoint();
                maybeAddPair( num, p, withGeometry, exact, list, bbox );
                break;
            }
            case POLYLINE: {
                in.skipBytes( 32 );
                maybeAddPair( num, readPolyline( false, false, length ), withGeometry, exact, list, bbox );
                break;
            }
            case POLYGON: {
                in.skipBytes( 32 );
                maybeAddPair( num, readPolygon( false, false, length ), withGeometry, exact, list, bbox );
                break;
            }
            case MULTIPOINT: {
                in.skipBytes( 32 );
                maybeAddPair( num, readMultipoint(), withGeometry, exact, list, bbox );
                break;
            }
            case POINTM: {
                in.skipBytes( 32 );
                maybeAddPair( num, readPointM(), withGeometry, exact, list, bbox );
                break;
            }
            case POLYLINEM: {
                in.skipBytes( 32 );
                maybeAddPair( num, readPolyline( false, true, length ), withGeometry, exact, list, bbox );
                break;
            }
            case POLYGONM: {
                in.skipBytes( 32 );
                maybeAddPair( num, readPolygon( false, true, length ), withGeometry, exact, list, bbox );
                break;
            }
            case MULTIPOINTM: {
                in.skipBytes( 32 );
                maybeAddPair( num, readMultipointM( length ), withGeometry, exact, list, bbox );
                break;
            }
            case POINTZ: {
                in.skipBytes( 32 );
                maybeAddPair( num, readPointZ(), withGeometry, exact, list, bbox );
                break;
            }
            case POLYLINEZ: {
                in.skipBytes( 32 );
                maybeAddPair( num, readPolyline( true, false, length ), withGeometry, exact, list, bbox );
                break;
            }
            case POLYGONZ: {
                in.skipBytes( 32 );
                maybeAddPair( num, readPolygon( true, false, length ), withGeometry, exact, list, bbox );
                break;
            }
            case MULTIPOINTZ: {
                in.skipBytes( 32 );
                maybeAddPair( num, readMultipointZ( length ), withGeometry, exact, list, bbox );
                break;
            }
            case MULTIPATCH: {
                in.skipBytes( 32 );
                maybeAddPair( num, readMultipatch( length ), withGeometry, exact, list, bbox );
                break;
            }
            }
        }

        return list;
    }

    /**
     * He Who Needs It As Double, is welcome to implement/copy it.
     * 
     * @return a list of all envelopes (minx, miny, maxx, maxy)
     * @throws IOException
     */
    public ArrayList<Pair<Envelope, Long>> readEnvelopes()
                            throws IOException {
        ArrayList<Pair<Envelope, Long>> list = new ArrayList<Pair<Envelope, Long>>();

        in.seek( 100 );

        while ( in.getFilePointer() + 1 < in.length() ) {
            in.skipBytes( 4 );
            int length = in.readInt() * 2; // bah, 16 bit length units here as well!
            long pos = in.getFilePointer();
            int type = readLEInt( in );
            switch ( type ) {
            case NULL:
                continue;
            case POINT: {
                double x = readLEDouble( in );
                double y = readLEDouble( in );
                list.add( new Pair<Envelope, Long>( fac.createEnvelope( x, y, x, y, null ), pos ) );
                break;
            }
            default: {
                list.add( new Pair<Envelope, Long>( fac.createEnvelope( readLEDouble( in ), readLEDouble( in ),
                                                                        readLEDouble( in ), readLEDouble( in ), null ),
                                                    pos ) );
                break;
            }
            }

            in.seek( pos + length );
        }

        return list;
    }

    /**
     * @return the overall bbox of the shape file
     */
    public Envelope getEnvelope() {
        return bbox;
    }

    private Point readPoint()
                            throws IOException {
        return fac.createPoint( null, readLEDouble( in ), readLEDouble( in ), crs );
    }

    private Geometry readPolygon( boolean z, boolean m, int length )
                            throws IOException {

        Points[] ps = readLines( m, z, length );

        Ring outer = null;
        LinkedList<Ring> inners = new LinkedList<Ring>();
        LinkedList<Polygon> polys = new LinkedList<Polygon>();

        for ( Points p : ps ) {
            if ( outer == null ) {
                outer = fac.createLinearRing( null, crs, p );
            } else {
                Ring ring = fac.createLinearRing( null, crs, p );
                if ( outer.contains( ring ) ) {
                    inners.add( ring );
                } else {
                    polys.add( fac.createPolygon( null, crs, ring, null ) );
                    polys.add( fac.createPolygon( null, crs, outer, null ) );
                }
            }
        }

        if ( polys.size() > 0 ) {
            return fac.createMultiPolygon( null, crs, polys );
        }
        return fac.createPolygon( null, crs, outer, inners );
    }

    private MultiPoint readMultipoint()
                            throws IOException {
        int num = readLEInt( in );

        LinkedList<Point> list = new LinkedList<Point>();
        for ( int i = 0; i < num; ++i ) {
            list.add( fac.createPoint( null, readLEInt( in ), readLEInt( in ), crs ) );
        }

        return fac.createMultiPoint( null, crs, list );
    }

    private Point readPointM()
                            throws IOException {
        return fac.createPoint( null, readLEInt( in ), readLEInt( in ), readLEInt( in ), crs );
    }

    private MultiPoint readMultipointM( int length )
                            throws IOException {
        int num = readLEInt( in );

        int len = 40 + num * 16;
        if ( length == len ) {
            LinkedList<Point> list = new LinkedList<Point>();

            for ( int i = 0; i < num; ++i ) {
                list.add( fac.createPoint( null, new double[] { readLEInt( in ), readLEInt( in ), 0, 0 }, crs ) );
            }

            return fac.createMultiPoint( null, crs, list );
        }

        LinkedList<double[]> xy = new LinkedList<double[]>();
        for ( int i = 0; i < num; ++i ) {
            xy.add( new double[] { readLEInt( in ), readLEInt( in ), 0, 0 } );
        }

        LinkedList<Point> list = new LinkedList<Point>();
        in.skipBytes( 16 ); // skip measure bounds
        for ( int i = 0; i < num; ++i ) {
            double[] p = xy.poll();
            p[3] = readLEInt( in );
            list.add( fac.createPoint( null, p, crs ) );
        }

        return fac.createMultiPoint( null, crs, list );
    }

    private Point readPointZ()
                            throws IOException {
        return fac.createPoint( null,
                                new double[] { readLEInt( in ), readLEInt( in ), readLEInt( in ), readLEInt( in ) },
                                crs );
    }

    private Curve readPolyline( boolean z, boolean m, int length )
                            throws IOException {

        Points[] ps = readLines( m, z, length );
        Curve curve = null;
        if ( ps.length == 1 ) {
            curve = fac.createLineString( null, crs, ps[0] );
        } else {
            LineStringSegment[] segs = new LineStringSegment[ps.length];
            for ( int i = 0; i < segs.length; ++i ) {
                segs[i] = fac.createLineStringSegment( ps[i] );
            }
            curve = fac.createCurve( null, segs, crs );
        }
        return curve;
    }

    private MultiPoint readMultipointZ( int length )
                            throws IOException {
        int num = readLEInt( in );
        int len = 40 + ( 16 * num ) + 16 + 8 * num;

        if ( len == length ) {
            LinkedList<double[]> xy = new LinkedList<double[]>();
            for ( int i = 0; i < num; ++i ) {
                xy.add( new double[] { readLEInt( in ), readLEInt( in ), 0, 0 } );
            }

            LinkedList<Point> list = new LinkedList<Point>();
            in.skipBytes( 16 ); // skip Z bounds
            for ( int i = 0; i < num; ++i ) {
                double[] p = xy.poll();
                p[2] = readLEInt( in );
                list.add( fac.createPoint( null, p, crs ) );
            }

            return fac.createMultiPoint( null, crs, list );
        }

        LinkedList<double[]> xy = new LinkedList<double[]>();
        for ( int i = 0; i < num; ++i ) {
            xy.add( new double[] { readLEInt( in ), readLEInt( in ), 0, 0 } );
        }

        in.skipBytes( 16 ); // skip Z bounds
        for ( double[] ps : xy ) {
            ps[2] = readLEInt( in );
        }

        LinkedList<Point> list = new LinkedList<Point>();
        in.skipBytes( 16 ); // skip measure bounds
        for ( int i = 0; i < num; ++i ) {
            double[] p = xy.poll();
            p[3] = readLEInt( in );
            list.add( fac.createPoint( null, p, crs ) );
        }

        return fac.createMultiPoint( null, crs, list );
    }

    private MultiSurface readMultipatch( int length )
                            throws IOException {
        int numParts = readLEInt( in );
        int numPoints = readLEInt( in );

        if ( LOG.isTraceEnabled() ) {
            LOG.trace( "Reading multipatch with " + numParts + " parts and " + numPoints + " points." );
        }

        int[] partTypes = new int[numParts];
        int[] parts = new int[numParts];

        // read part info
        for ( int i = 0; i < numParts; ++i ) {
            parts[i] = readLEInt( in );
        }
        for ( int i = 0; i < numParts; ++i ) {
            partTypes[i] = readLEInt( in );
        }

        // read points
        double[][][] points = new double[numParts][][];
        for ( int i = 0; i < numParts; ++i ) {
            // get length of current part
            int max;
            if ( i == numParts - 1 ) {
                max = numPoints;
            } else {
                max = parts[i + 1];
            }
            points[i] = new double[max - parts[i]][];

            // read points for part
            for ( int k = 0; k < points[i].length; ++k ) {
                points[i][k] = new double[] { readLEInt( in ), readLEInt( in ), 0, 0 };
            }
        }

        in.skipBytes( 16 ); // z boundary

        for ( int i = 0; i < numParts; ++i ) {
            // read points for part
            for ( int k = 0; k < points[i].length; ++k ) {
                points[i][k][2] = readLEInt( in );
            }
        }

        int len = 60 + 8 * numParts + 24 * numPoints;

        if ( length != len ) {
            in.skipBytes( 16 );
            for ( int i = 0; i < numParts; ++i ) {
                // read points for part
                for ( int k = 0; k < points[i].length; ++k ) {
                    points[i][k][3] = readLEInt( in );
                }
            }
        }

        return parseMultiPatch( points, partTypes );
    }

    // converts every triangle to a surface with an outer ring
    private LinkedList<Surface> fromTriangleStrip( double[][] points ) {
        LinkedList<Point> ps = new LinkedList<Point>();

        for ( double[] p : points ) {
            ps.add( fac.createPoint( null, p, crs ) );
        }

        LinkedList<Surface> ss = new LinkedList<Surface>();

        while ( ps.size() > 2 ) {
            LinkedList<Point> ring = new LinkedList<Point>();
            ring.add( ps.get( 0 ) );
            ring.add( ps.get( 1 ) );
            ring.add( ps.get( 2 ) );
            ring.add( ring.getFirst() );
            ps.poll();
            Ring r = fac.createLinearRing( null, crs, new PointsList( ring ) );
            ss.add( fac.createPolygon( null, crs, r, null ) );
        }

        return ss;
    }

    // just uses an outer ring, all vertices and the first one again
    private LinkedList<Surface> fromTriangleFan( double[][] points ) {
        LinkedList<Point> ps = new LinkedList<Point>();

        for ( double[] p : points ) {
            ps.add( fac.createPoint( null, p, crs ) );
        }

        LinkedList<Surface> ss = new LinkedList<Surface>();

        Point center = ps.poll();

        while ( ps.size() > 1 ) {
            LinkedList<Point> ringps = new LinkedList<Point>();
            ringps.add( center );
            ringps.add( ps.get( 0 ) );
            ringps.add( ps.get( 1 ) );
            ringps.add( center );
            ps.poll();
            LinearRing ring = fac.createLinearRing( null, crs, new PointsList( ringps ) );
            ss.add( fac.createPolygon( null, crs, ring, null ) );
        }

        return ss;
    }

    private LinearRing fromRing( double[][] points ) {
        LinkedList<Point> ps = new LinkedList<Point>();

        for ( double[] p : points ) {
            ps.add( fac.createPoint( null, p, crs ) );
        }

        // may be expensive
        if ( ps.getFirst() != ps.getLast() ) {
            LOG.debug( "Ring was not closed as required by the shape file spec!" );
            LOG.debug( "Trying to recover anyway." );

            ps.add( ps.getFirst() );
        }

        return fac.createLinearRing( null, crs, new PointsList( ps ) );
    }

    // bad: do it by hand using JTS
    private boolean isCCW( Curve c ) {
        Points ps = c.getControlPoints();
        Coordinate[] cs = new Coordinate[ps.size()];
        int i = 0;
        for ( Point p : ps ) {
            cs[i++] = new Coordinate( p.get0(), p.get1() );
        }

        return CGAlgorithms.isCCW( cs );
    }

    private MultiSurface parseMultiPatch( double[][][] ps, int[] partTypes ) {
        LinkedList<Surface> ss = new LinkedList<Surface>();

        boolean outerRingMode = false;
        Ring outerRing = null;
        LinkedList<Ring> innerRings = new LinkedList<Ring>();

        boolean unknownRingMode = false;
        Ring unknownOuterRing = null;
        LinkedList<Ring> unknownInnerRings = new LinkedList<Ring>();

        for ( int i = 0; i < partTypes.length; ++i ) {
            switch ( partTypes[i] ) {
            case TRIANGLE_STRIP:
            case TRIANGLE_FAN:
            case OUTER_RING:
            case FIRST_RING:
            case RING:
                if ( outerRingMode ) {
                    LOG.trace( "Finishing with outer ring mode." );
                    ss.add( fac.createPolygon( null, crs, outerRing, innerRings ) );
                    innerRings.clear();
                    outerRing = null;
                    outerRingMode = false;
                }
                break;
            }

            switch ( partTypes[i] ) {
            case TRIANGLE_STRIP:
                ss.addAll( fromTriangleStrip( ps[i] ) );
                LOG.trace( "Read triangle strip." );
                break;
            case TRIANGLE_FAN:
                ss.addAll( fromTriangleFan( ps[i] ) );
                LOG.trace( "Read triangle fan." );
                break;
            case OUTER_RING:
                outerRingMode = true;
                outerRing = fromRing( ps[i] );
                LOG.trace( "Read outer ring." );
                break;
            case INNER_RING:
                if ( !outerRingMode ) {
                    LOG.debug( "Found inner ring without preceding outer ring." );
                    break;
                }
                innerRings.add( fromRing( ps[i] ) );
                LOG.trace( "Read inner ring." );
                break;
            case FIRST_RING:
                LOG.trace( "Read first ring." );
                unknownRingMode = true;
                Ring ring = fromRing( ps[i] );
                if ( isCCW( ring ) ) {
                    unknownOuterRing = ring;
                } else {
                    unknownInnerRings.add( ring );
                }
                break;
            case RING:
                LOG.trace( "Read ring." );
                ring = fromRing( ps[i] );
                if ( !unknownRingMode ) {
                    ss.add( fac.createPolygon( null, crs, ring, null ) );
                } else {
                    if ( isCCW( ring ) ) {
                        unknownOuterRing = ring;
                    } else {
                        unknownInnerRings.add( ring );
                    }
                }

                break;

            }

            if ( unknownRingMode ) {
                if ( partTypes[i] != RING ) {
                    if ( unknownOuterRing == null ) {
                        while ( unknownInnerRings.size() != 0 ) {
                            ss.add( fac.createPolygon( null, crs, unknownInnerRings.poll(), null ) );
                        }
                    } else {
                        ss.add( fac.createPolygon( null, crs, unknownOuterRing, unknownInnerRings ) );
                    }
                    unknownInnerRings.clear();
                    unknownOuterRing = null;
                    unknownRingMode = false;
                }
            }

        }

        if ( ss.size() == 0 ) {
            return null;
        }
        return fac.createMultiSurface( null, crs, ss );
    }

    private Points[] readLines( boolean m, boolean z, int length )
                            throws IOException {

        int coordDim = m ? 4 : ( z ? 3 : 2 );

        int numParts = readLEInt( in );
        int numPoints = readLEInt( in );

        PackedPoints[] res = new PackedPoints[numParts];
        int[] parts = new int[numParts];

        for ( int i = 0; i < numParts; ++i ) {
            parts[i] = readLEInt( in );
        }

        for ( int i = 0; i < numParts; ++i ) {
            // calculate number of points for current part
            int num;
            if ( i == numParts - 1 ) {
                num = numPoints - parts[i];
            } else {
                num = parts[i + 1] - parts[i];
            }

            double[] coords = new double[num * coordDim];
            int idx = 0;
            for ( int j = 0; j < num; ++j ) {
                coords[idx++] = readLEDouble( in );
                coords[idx++] = readLEDouble( in );
                if ( coordDim == 3 ) {
                    idx += 1;
                } else if ( coordDim == 4 ) {
                    idx += 2;
                }
            }
            res[i] = new PackedPoints( coords, coordDim );
        }

        if ( !z && !m ) {
            return res;
        }

        int mlen = 44 + 4 * numParts + 16 * numPoints;
        int zlen = mlen + 16 + 8 * numPoints;

        if ( z ) {
            in.skipBytes( 16 );
            for ( int i = 0; i < numParts; ++i ) {
                double[] coords = res[i].getAsArray();
                for ( int j = 0; j < res[i].size(); ++j ) {
                    coords[2 + j * coordDim] = readLEDouble( in );
                }
            }
        }

        if ( ( m && mlen != length ) || ( z && zlen != length ) ) {
            in.skipBytes( 16 );
            for ( int i = 0; i < numParts; ++i ) {
                double[] coords = res[i].getAsArray();
                for ( int j = 0; j < res[i].size(); ++j ) {
                    coords[3 + j * coordDim] = readLEDouble( in );
                }
            }
        }

        return res;
    }

    /**
     * Closes the underlying input stream.
     * 
     * @throws IOException
     */
    public void close()
                            throws IOException {
        in.close();
    }

}
