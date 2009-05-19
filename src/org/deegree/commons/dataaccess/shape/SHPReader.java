//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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

package org.deegree.commons.dataaccess.shape;

import static org.deegree.commons.utils.ByteUtils.readLEDouble;
import static org.deegree.commons.utils.ByteUtils.readLEInt;
import static org.deegree.crs.coordinatesystems.GeographicCRS.WGS84;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.utils.Pair;
import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryFactoryCreator;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.curvesegments.CurveSegment;
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

    private static final GeometryFactory fac = GeometryFactoryCreator.getInstance().getGeometryFactory();

    private double[] envelope;

    private int type;

    private final RandomAccessFile in;

    private final CRS crs;

    private Envelope bbox;

    /**
     * @param in
     * @param crs
     * @throws IOException
     */
    public SHPReader( RandomAccessFile in, CRS crs ) throws IOException {
        this.in = in;
        this.crs = crs;
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
        try {
            bbox = fac.createEnvelope( envelope[0], envelope[2], envelope[1], envelope[3], crs );
            GeometryTransformer t = new GeometryTransformer( WGS84 );
            bbox = (Envelope) t.transform( bbox );
        } catch ( Exception e ) {
            LOG.warn( "Could not properly transform envelope of shape file." );
            LOG.warn( "Using the envelope will likely yield problems." );
            LOG.debug( "Stack trace: ", e );
        }

        if ( LOG.isTraceEnabled() ) {
            LOG.trace( "Envelope: " + envelope[0] + "," + envelope[1] + " " + envelope[2] + "," + envelope[3] + " "
                       + envelope[4] + "," + envelope[5] + " " + envelope[6] + "," + envelope[7] );
        }

    }

    /**
     * @param bbox
     * @return the list of contained geometries
     * @throws IOException
     */
    public LinkedList<Pair<Integer, Geometry>> query( Envelope bbox )
                            throws IOException {
        LinkedList<Pair<Integer, Geometry>> list = new LinkedList<Pair<Integer, Geometry>>();

        in.seek( 100 );

        while ( in.getFilePointer() + 1 < in.length() ) {
            int num = in.readInt() - 1;
            int length = in.readInt() * 2; // bah, 16 bit length units here as well!
            LOG.trace( "Current record length: " + length );
            long last = in.getFilePointer();
            int type = readLEInt( in );
            switch ( type ) {
            case NULL:
                continue;
            case POINT: {
                Point p = readPoint();
                if ( bbox == null || bbox.intersects( p ) ) {
                    list.add( new Pair<Integer, Geometry>( num, p ) );
                }
                break;
            }
            case POLYLINE: {
                Envelope box = readEnvelope();
                if ( bbox == null || bbox.intersects( box ) ) {
                    list.add( new Pair<Integer, Geometry>( num, readPolyline( false, false, length ) ) );
                }
                break;
            }
            case POLYGON: {
                Envelope box = readEnvelope();
                if ( bbox == null || bbox.intersects( box ) ) {
                    list.add( new Pair<Integer, Geometry>( num, readPolygon( false, false, length ) ) );
                }
                break;
            }
            case MULTIPOINT: {
                Envelope box = readEnvelope();
                if ( bbox == null || bbox.intersects( box ) ) {
                    list.add( new Pair<Integer, Geometry>( num, readMultipoint() ) );
                }
                break;
            }
            case POINTM: {
                Envelope box = readEnvelope();
                if ( bbox == null || bbox.intersects( box ) ) {
                    list.add( new Pair<Integer, Geometry>( num, readPointM() ) );
                }
                break;
            }
            case POLYLINEM: {
                Envelope box = readEnvelope();
                if ( bbox == null || bbox.intersects( box ) ) {
                    list.add( new Pair<Integer, Geometry>( num, readPolyline( false, true, length ) ) );
                }
                break;
            }
            case POLYGONM: {
                Envelope box = readEnvelope();
                if ( bbox == null || bbox.intersects( box ) ) {
                    list.add( new Pair<Integer, Geometry>( num, readPolygon( false, true, length ) ) );
                }
                break;
            }
            case MULTIPOINTM: {
                Envelope box = readEnvelope();
                if ( bbox == null || bbox.intersects( box ) ) {
                    list.add( new Pair<Integer, Geometry>( num, readMultipointM( length ) ) );
                }
                break;
            }
            case POINTZ: {
                Envelope box = readEnvelope();
                if ( bbox == null || bbox.intersects( box ) ) {
                    list.add( new Pair<Integer, Geometry>( num, readPointZ() ) );
                }
                break;
            }
            case POLYLINEZ: {
                Envelope box = readEnvelope();
                if ( bbox == null || bbox.intersects( box ) ) {
                    list.add( new Pair<Integer, Geometry>( num, readPolyline( true, false, length ) ) );
                }
                break;
            }
            case POLYGONZ: {
                Envelope box = readEnvelope();
                if ( bbox == null || bbox.intersects( box ) ) {
                    list.add( new Pair<Integer, Geometry>( num, readPolygon( true, false, length ) ) );
                }
                break;
            }
            case MULTIPOINTZ: {
                Envelope box = readEnvelope();
                if ( bbox == null || bbox.intersects( box ) ) {
                    list.add( new Pair<Integer, Geometry>( num, readMultipointZ( length ) ) );
                }
                break;
            }
            case MULTIPATCH: {
                Envelope box = readEnvelope();
                if ( bbox == null || bbox.intersects( box ) ) {
                    list.add( new Pair<Integer, Geometry>( num, readMultipatch( length ) ) );
                }
                break;
            }
            }

            in.seek( last + length ); // in case the last one was skipped
        }

        return list;
    }

    /**
     * @return the overall bbox of the shape file
     */
    public Envelope getEnvelope() {
        return bbox;
    }

    private Envelope readEnvelope()
                            throws IOException {
        return fac.createEnvelope( readLEDouble( in ), readLEDouble( in ), readLEDouble( in ), readLEDouble( in ), crs );
    }

    private Point readPoint()
                            throws IOException {
        return fac.createPoint( null, readLEDouble( in ), readLEDouble( in ), crs );
    }

    private Polygon readPolygon( boolean z, boolean m, int length )
                            throws IOException {
        double[][][] ps = readLines( m, z, length );

        Ring outer = null;
        LinkedList<Ring> inners = new LinkedList<Ring>();

        for ( int i = 0; i < ps.length; ++i ) {
            LinkedList<Point> pos = new LinkedList<Point>();
            for ( int j = 0; j < ps[i].length; ++j ) {
                pos.add( fac.createPoint( null, ps[i][j], crs ) );
            }
            if ( outer == null ) {
                outer = fac.createLinearRing( null, crs, pos );
            } else {
                inners.add( fac.createLinearRing( null, crs, pos ) );
            }
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
        double[][][] ps = readLines( m, z, length );

        CurveSegment[] segs = new CurveSegment[ps.length];

        for ( int i = 0; i < segs.length; ++i ) {
            LinkedList<Point> points = new LinkedList<Point>();
            for ( int j = 0; j < ps[i].length; ++j ) {
                points.add( fac.createPoint( null, ps[i][j], crs ) );
            }
            segs[i] = fac.createLineStringSegment( points );
        }

        return fac.createCurve( null, segs, crs );
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
            Ring r = fac.createLinearRing( null, crs, ring );
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
            LinearRing ring = fac.createLinearRing( null, crs, ringps );
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

        return fac.createLinearRing( null, crs, ps );
    }

    // bad: do it by hand using JTS
    private boolean isCCW( Curve c ) {
        List<Point> ps = c.getControlPoints();
        Coordinate[] cs = new Coordinate[ps.size()];
        int i = 0;
        for ( Point p : ps ) {
            cs[i++] = new Coordinate( p.getX(), p.getY() );
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

    private double[][][] readLines( boolean m, boolean z, int length )
                            throws IOException {
        int numParts = readLEInt( in );
        int numPoints = readLEInt( in );

        double[][][] res = new double[numParts][][];
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

            res[i] = new double[num][];
            for ( int j = 0; j < num; ++j ) {
                if ( !z && !m ) {
                    res[i][j] = new double[] { readLEDouble( in ), readLEDouble( in ) };
                } else {
                    res[i][j] = new double[] { readLEDouble( in ), readLEDouble( in ), 0, 0 };
                }
            }
        }

        if ( !z && !m ) {
            return res;
        }

        int mlen = 44 + 4 * numParts + 16 * numPoints;
        int zlen = mlen + 16 + 8 * numPoints;

        if ( z ) {
            in.skipBytes( 16 );
            for ( int i = 0; i < numParts; ++i ) {
                for ( int j = 0; j < res[i].length; ++j ) {
                    res[i][j][2] = readLEDouble( in );
                }
            }
        }

        if ( ( m && mlen != length ) || ( z && zlen != length ) ) {
            in.skipBytes( 16 );
            for ( int i = 0; i < numParts; ++i ) {
                for ( int j = 0; j < res[i].length; ++j ) {
                    res[i][j][3] = readLEDouble( in );
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
