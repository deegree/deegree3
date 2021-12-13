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

package org.deegree.feature.persistence.shape;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.GEOMETRY;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.MULTI_LINE_STRING;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.MULTI_POINT;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.MULTI_POLYGON;
import static org.deegree.geometry.utils.GeometryUtils.createEnvelope;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.index.SpatialIndex;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.standard.points.PackedPoints;
import org.deegree.geometry.standard.points.PointsList;
import org.slf4j.Logger;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.Coordinate;

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

    private final ICRS crs;

    private Envelope bbox;

    private SpatialIndex<Long> rtree;

    private boolean recordNumStartsWith0 = false;

    private RandomAccessFile file;

    private FileChannel channel;

    // buffer is not thread-safe (needs to be duplicated for every thread)
    private final ByteBuffer sharedBuffer;

    /**
     * @param inFile
     * @param crs
     * @param rtree
     * @param startsWithZero
     * @throws IOException
     */
    public SHPReader( RandomAccessFile inFile, ICRS crs, SpatialIndex<Long> rtree, boolean startsWithZero )
                            throws IOException {
        file = inFile;
        channel = file.getChannel();
        sharedBuffer = channel.map( MapMode.READ_ONLY, 0, file.length() );
        ByteBuffer buffer = sharedBuffer.asReadOnlyBuffer();
        buffer.order( ByteOrder.BIG_ENDIAN );
        this.crs = crs;
        this.rtree = rtree;
        this.recordNumStartsWith0 = startsWithZero;
        if ( buffer.getInt() != FILETYPE ) {
            LOG.warn( "File type is wrong, unexpected things might happen, continuing anyway..." );
        }

        buffer.position( 24 );
        int length = buffer.getInt() * 2; // 16 bit words...

        LOG.trace( "Length {}", length );

        // whyever they mix byte orders?
        buffer.order( ByteOrder.LITTLE_ENDIAN );
        int version = buffer.getInt();

        if ( version != VERSION ) {
            LOG.warn( "File version is wrong, continuing in the hope of compatibility..." );
        }

        type = buffer.getInt();
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
        envelope[0] = buffer.getDouble();
        envelope[2] = buffer.getDouble();
        envelope[1] = buffer.getDouble();
        envelope[3] = buffer.getDouble();
        envelope[4] = buffer.getDouble();
        envelope[5] = buffer.getDouble();
        envelope[6] = buffer.getDouble();
        envelope[7] = buffer.getDouble();

        // TODO do this for 3D as well
        bbox = fac.createEnvelope( envelope[0], envelope[2], envelope[1], envelope[3], crs );

        if ( LOG.isTraceEnabled() ) {
            LOG.trace( "Envelope: " + envelope[0] + "," + envelope[1] + " " + envelope[2] + "," + envelope[3] + " "
                       + envelope[4] + "," + envelope[5] + " " + envelope[6] + "," + envelope[7] );
        }

    }

    public GeometryType getGeometryType() {
        switch ( type ) {
        case POINT:
        case POINTM:
        case POINTZ:
            return GeometryType.POINT;
        case POLYLINE:
        case POLYLINEM:
        case POLYLINEZ:
            return MULTI_LINE_STRING;
        case MULTIPOINT:
        case MULTIPOINTM:
        case MULTIPOINTZ:
            return MULTI_POINT;
        case POLYGON:
        case POLYGONM:
        case POLYGONZ:
            return MULTI_POLYGON;
        case NULL:
        case MULTIPATCH:
        }
        return GEOMETRY;
    }

    /**
     * @param bbox
     * @param ids
     *            if not null, the resulting list will only contain record numbers which are also contained in this set.
     *            If null, all matching records are returned.
     * @return the list of matching record ids
     */
    public List<Pair<Integer, Long>> query( Envelope bbox, HashSet<Integer> ids ) {

        LOG.debug( "Querying shp with bbox {}", bbox );

        ByteBuffer buffer = sharedBuffer.asReadOnlyBuffer();
        buffer.order( ByteOrder.LITTLE_ENDIAN );
        List<Long> pointers = (List<Long>) rtree.query( createEnvelope( bbox ) );
        List<Pair<Integer, Long>> recNums = new ArrayList<Pair<Integer, Long>>( pointers.size() );
        Collections.sort( pointers );
        for ( Long ptr : pointers ) {
            buffer.position( (int) ( ptr - 8 ) );
            int num = getBEInt( buffer );
            if ( num == 0 && !recordNumStartsWith0 && rtree != null ) {
                LOG.error( "PLEASE NOTE THIS: Detected that the shape file starts counting record numbers at 0 and not at 1 as specified!" );
                LOG.error( "PLEASE NOTE THIS: This should not happen any more, and is a bug! Please report this along with the data!" );
                recordNumStartsWith0 = true;
            }

            if ( !recordNumStartsWith0 ) {
                num -= 1;
            }

            if ( ids != null && !ids.contains( num ) ) {
                continue;
            }

            recNums.add( new Pair<Integer, Long>( num, ptr ) );
        }

        return recNums;
    }

    /**
     * @param bbox
     * @param withGeometry
     * @param exact
     * @return the list of contained geometries
     */
    public LinkedList<Pair<Integer, Geometry>> query( Envelope bbox, boolean withGeometry, boolean exact ) {

        LOG.debug( "Querying shp with bbox {}", bbox );
        ByteBuffer buffer = sharedBuffer.asReadOnlyBuffer();
        buffer.order( ByteOrder.LITTLE_ENDIAN );

        LinkedList<Pair<Integer, Geometry>> list = new LinkedList<Pair<Integer, Geometry>>();

        List<Long> pointers = (List<Long>) rtree.query( createEnvelope( bbox ) );
        Collections.sort( pointers );
        for ( Long ptr : pointers ) {
            buffer.position( (int) ( ptr - 8 ) );

            int num = getBEInt( buffer );
            if ( num == 0 && !recordNumStartsWith0 ) {
                LOG.error( "PLEASE NOTE THIS: Detected that the shape file starts counting record numbers at 0 and not at 1 as specified!" );
                LOG.error( "PLEASE NOTE THIS: This should not happen any more, and is a bug! Please report this along with the data!" );
                recordNumStartsWith0 = true;
            }
            if ( !recordNumStartsWith0 ) {
                num -= 1;
            }

            if ( !withGeometry && !exact ) {
                list.add( new Pair<Integer, Geometry>( num, null ) );
                continue;
            }

            int length = getBEInt( buffer ) * 2; // bah, 16 bit length units here as well!

            int type = buffer.getInt();
            switch ( type ) {
            case NULL:
                continue;
            case POINT: {
                Point p = readPoint( buffer );
                maybeAddPair( num, p, withGeometry, exact, list, bbox );
                break;
            }
            case POLYLINE: {
                skipBytes( buffer, 32 );
                maybeAddPair( num, readPolyline( buffer, false, false, length ), withGeometry, exact, list, bbox );
                break;
            }
            case POLYGON: {
                skipBytes( buffer, 32 );
                maybeAddPair( num, readPolygon( buffer, false, false, length ), withGeometry, exact, list, bbox );
                break;
            }
            case MULTIPOINT: {
                skipBytes( buffer, 32 );
                maybeAddPair( num, readMultipoint( buffer ), withGeometry, exact, list, bbox );
                break;
            }
            case POINTM: {
                skipBytes( buffer, 32 );
                maybeAddPair( num, readPointM( buffer ), withGeometry, exact, list, bbox );
                break;
            }
            case POLYLINEM: {
                skipBytes( buffer, 32 );
                maybeAddPair( num, readPolyline( buffer, false, true, length ), withGeometry, exact, list, bbox );
                break;
            }
            case POLYGONM: {
                skipBytes( buffer, 32 );
                maybeAddPair( num, readPolygon( buffer, false, true, length ), withGeometry, exact, list, bbox );
                break;
            }
            case MULTIPOINTM: {
                skipBytes( buffer, 32 );
                maybeAddPair( num, readMultipointM( buffer, length ), withGeometry, exact, list, bbox );
                break;
            }
            case POINTZ: {
                skipBytes( buffer, 32 );
                maybeAddPair( num, readPointZ( buffer ), withGeometry, exact, list, bbox );
                break;
            }
            case POLYLINEZ: {
                skipBytes( buffer, 32 );
                maybeAddPair( num, readPolyline( buffer, true, false, length ), withGeometry, exact, list, bbox );
                break;
            }
            case POLYGONZ: {
                skipBytes( buffer, 32 );
                maybeAddPair( num, readPolygon( buffer, true, false, length ), withGeometry, exact, list, bbox );
                break;
            }
            case MULTIPOINTZ: {
                skipBytes( buffer, 32 );
                maybeAddPair( num, readMultipointZ( buffer, length ), withGeometry, exact, list, bbox );
                break;
            }
            case MULTIPATCH: {
                skipBytes( buffer, 32 );
                maybeAddPair( num, readMultipatch( buffer, length ), withGeometry, exact, list, bbox );
                break;
            }
            }
        }

        return list;
    }

    /**
     * @return the overall bbox of the shape file
     */
    public Envelope getEnvelope() {
        return bbox;
    }

    /**
     * He Who Needs It As Double, is welcome to implement/copy it.
     * 
     * @return a list of all envelopes (minx, miny, maxx, maxy)
     */
    public Pair<ArrayList<Pair<float[], Long>>, Boolean> readEnvelopes() {
        ByteBuffer buffer = sharedBuffer.asReadOnlyBuffer();
        buffer.order( ByteOrder.LITTLE_ENDIAN );
        ArrayList<Pair<float[], Long>> list = new ArrayList<Pair<float[], Long>>();
        boolean startsFromZero = false;

        buffer.position( 100 );

        while ( buffer.position() + 1 < buffer.capacity() ) {
            int recNum = getBEInt( buffer );
            if ( !startsFromZero ) {
                startsFromZero = recNum == 0;
            }
            int length = getBEInt( buffer ) * 2; // bah, 16 bit length units here as well!
            long pos = buffer.position();
            int type = buffer.getInt();
            switch ( type ) {
            case NULL:
                list.add( new Pair<float[], Long>( null, pos ) );
                break;
            case POINT: {
                double x = buffer.getDouble();
                double y = buffer.getDouble();
                Pair<float[], Long> p = new Pair<float[], Long>( new float[] { (float) x, (float) y, (float) x,
                                                                              (float) y }, pos );
                list.add( p );
                break;
            }
            default: {
                Pair<float[], Long> p = new Pair<float[], Long>( new float[] { (float) buffer.getDouble(),
                                                                              (float) buffer.getDouble(),
                                                                              (float) buffer.getDouble(),
                                                                              (float) buffer.getDouble() }, pos );
                list.add( p );
                break;
            }
            }

            try {
                buffer.position( (int) ( pos + length ) );
            } catch ( IllegalArgumentException e ) {
                // ignore it, this seems to happen with some broken shape files
                return new Pair<ArrayList<Pair<float[], Long>>, Boolean>( list, startsFromZero );
            }
        }

        return new Pair<ArrayList<Pair<float[], Long>>, Boolean>( list, startsFromZero );
    }

    /**
     * Returns the geometry entry stored at the given position.
     * 
     * @param ptr
     *            position of the entry
     * @return geometry object, may be <code>null</code>
     */
    public Geometry readGeometry( long ptr ) {

        LOG.trace( "Retrieving geometry at position {}", ptr );
        ByteBuffer buffer = sharedBuffer.asReadOnlyBuffer();
        buffer.order( ByteOrder.LITTLE_ENDIAN );

        buffer.position( (int) ( ptr - 4 ) );

        int length = getBEInt( buffer ) * 2; // bah, 16 bit length units here as well!
        int type = buffer.getInt();

        Geometry g = null;
        switch ( type ) {
        case NULL: {
            // nothing to do
        }
        case POINT: {
            g = readPoint( buffer );
            break;
        }
        case POLYLINE: {
            skipBytes( buffer, 32 );
            g = readPolyline( buffer, false, false, length );
            break;
        }
        case POLYGON: {
            skipBytes( buffer, 32 );
            g = readPolygon( buffer, false, false, length );
            break;
        }
        case MULTIPOINT: {
            skipBytes( buffer, 32 );
            g = readMultipoint( buffer );
            break;
        }
        case POINTM: {
            skipBytes( buffer, 32 );
            g = readPointM( buffer );
            break;
        }
        case POLYLINEM: {
            skipBytes( buffer, 32 );
            g = readPolyline( buffer, false, true, length );
            break;
        }
        case POLYGONM: {
            skipBytes( buffer, 32 );
            g = readPolygon( buffer, false, true, length );
            break;
        }
        case MULTIPOINTM: {
            skipBytes( buffer, 32 );
            g = readMultipointM( buffer, length );
            break;
        }
        case POINTZ: {
            skipBytes( buffer, 32 );
            g = readPointZ( buffer );
            break;
        }
        case POLYLINEZ: {
            skipBytes( buffer, 32 );
            g = readPolyline( buffer, true, true, length );
            break;
        }
        case POLYGONZ: {
            skipBytes( buffer, 32 );
            g = readPolygon( buffer, true, false, length );
            break;
        }
        case MULTIPOINTZ: {
            skipBytes( buffer, 32 );
            g = readMultipointZ( buffer, length );
            break;
        }
        case MULTIPATCH: {
            skipBytes( buffer, 32 );
            g = readMultipatch( buffer, length );
            break;
        }
        default: {
            throw new IllegalArgumentException( "Invalid geometry type " + type );
        }
        }
        return g;
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

    private final static int getBEInt( ByteBuffer buffer ) {
        buffer.order( BIG_ENDIAN );
        int result = buffer.getInt();
        buffer.order( LITTLE_ENDIAN );
        return result;
    }

    private final static void skipBytes( ByteBuffer buffer, int bytes ) {
        buffer.position( buffer.position() + bytes );
    }

    private Point readPoint( ByteBuffer buffer ) {
        return fac.createPoint( null, buffer.getDouble(), buffer.getDouble(), crs );
    }

    private Geometry readPolygon( ByteBuffer buffer, boolean z, boolean m, int length ) {

        Points[] ps = readLines( buffer, m, z, length );

        LinearRing outer = null;
        LinkedList<Ring> inners = new LinkedList<Ring>();
        LinkedList<Polygon> polys = new LinkedList<Polygon>();

        for ( Points p : ps ) {
            if ( p.size() < 4 ) {
                LOG.warn( "Ignoring ring with only {} points!", p.size() );
                continue;
            }

            if ( !p.getStartPoint().equals( p.getEndPoint() ) ) {
                LOG.warn( "Found ring that is not closed. Repairing it." );
                double[] coords = new double[( p.size() + 1 ) * p.getDimension()];
                int i = 0;
                for ( Point pt : p ) {
                    for ( int dim = 0; dim < pt.getCoordinateDimension(); dim++ ) {
                        coords[i++] = pt.get( dim );
                    }
                }
                for ( int dim = 0; dim < p.getDimension(); dim++ ) {
                    coords[i++] = p.get( 0 ).get( dim );
                }
                p = new PackedPoints( crs, coords, p.getDimension() );
            }

            if ( outer == null ) {
                outer = fac.createLinearRing( null, crs, p );
            } else {
                LinearRing ring = fac.createLinearRing( null, crs, p );
                Polygon outerP = fac.createPolygon( null, crs, outer, null );
                Polygon innerP = fac.createPolygon( null, crs, ring, null );
                if ( outerP.contains( innerP ) ) {
                    inners.add( ring );
                } else {
                    if ( inners.isEmpty() && innerP.contains( outerP ) ) {
                        LOG.warn( "Reordering rings of polygon..." );
                        inners.add( outer );
                        outer = ring;
                        continue;
                    }
                    if ( !inners.isEmpty() ) {
                        polys.add( fac.createPolygon( null, crs, outer, inners ) );
                        inners = new LinkedList<Ring>();
                    } else {
                        polys.add( fac.createPolygon( null, crs, outer, null ) );
                    }
                    outer = ring;
                }
            }
        }

        if ( outer != null ) {
            polys.add( fac.createPolygon( null, crs, outer, inners ) );
        }

        return fac.createMultiPolygon( null, crs, polys );
    }

    private MultiPoint readMultipoint( ByteBuffer buffer ) {
        int num = buffer.getInt();

        LinkedList<Point> list = new LinkedList<Point>();
        for ( int i = 0; i < num; ++i ) {
            list.add( fac.createPoint( null, buffer.getDouble(), buffer.getDouble(), crs ) );
        }

        return fac.createMultiPoint( null, crs, list );
    }

    private Point readPointM( ByteBuffer buffer ) {
        return fac.createPoint( null, buffer.getDouble(), buffer.getDouble(), buffer.getDouble(), crs );
    }

    private MultiPoint readMultipointM( ByteBuffer buffer, int length ) {
        int num = buffer.getInt();

        int len = 40 + num * 16;
        if ( length == len ) {
            LinkedList<Point> list = new LinkedList<Point>();

            for ( int i = 0; i < num; ++i ) {
                list.add( fac.createPoint( null, new double[] { buffer.getDouble(), buffer.getDouble(), 0, 0 }, crs ) );
            }

            return fac.createMultiPoint( null, crs, list );
        }

        LinkedList<double[]> xy = new LinkedList<double[]>();
        for ( int i = 0; i < num; ++i ) {
            xy.add( new double[] { buffer.getDouble(), buffer.getDouble(), 0, 0 } );
        }

        LinkedList<Point> list = new LinkedList<Point>();
        skipBytes( buffer, 16 ); // skip measure bounds
        for ( int i = 0; i < num; ++i ) {
            double[] p = xy.poll();
            p[3] = buffer.getDouble();
            list.add( fac.createPoint( null, p, crs ) );
        }

        return fac.createMultiPoint( null, crs, list );
    }

    private Point readPointZ( ByteBuffer buffer ) {
        return fac.createPoint( null,
                                new double[] { buffer.getDouble(), buffer.getDouble(), buffer.getDouble(),
                                              buffer.getDouble() }, crs );
    }

    private Geometry readPolyline( ByteBuffer buffer, boolean z, boolean m, int length ) {
        Points[] ps = readLines( buffer, m, z, length );
        List<LineString> cs = new ArrayList<LineString>( ps.length );
        for ( int i = 0; i < ps.length; ++i ) {
            cs.add( fac.createLineString( null, crs, ps[i] ) );
        }
        return fac.createMultiLineString( null, crs, cs );
    }

    private MultiPoint readMultipointZ( ByteBuffer buffer, int length ) {
        int num = buffer.getInt();
        int len = 40 + ( 16 * num ) + 16 + 8 * num;

        if ( len == length ) {
            LinkedList<double[]> xy = new LinkedList<double[]>();
            for ( int i = 0; i < num; ++i ) {
                xy.add( new double[] { buffer.getDouble(), buffer.getDouble(), 0, 0 } );
            }

            LinkedList<Point> list = new LinkedList<Point>();
            skipBytes( buffer, 16 ); // skip Z bounds
            for ( int i = 0; i < num; ++i ) {
                double[] p = xy.poll();
                p[2] = buffer.getDouble();
                list.add( fac.createPoint( null, p, crs ) );
            }

            return fac.createMultiPoint( null, crs, list );
        }

        LinkedList<double[]> xy = new LinkedList<double[]>();
        for ( int i = 0; i < num; ++i ) {
            xy.add( new double[] { buffer.getInt(), buffer.getInt(), 0, 0 } );
        }

        skipBytes( buffer, 16 ); // skip Z bounds
        for ( double[] ps : xy ) {
            ps[2] = buffer.getDouble();
        }

        LinkedList<Point> list = new LinkedList<Point>();
        skipBytes( buffer, 16 ); // skip measure bounds
        for ( int i = 0; i < num; ++i ) {
            double[] p = xy.poll();
            p[3] = buffer.getDouble();
            list.add( fac.createPoint( null, p, crs ) );
        }

        return fac.createMultiPoint( null, crs, list );
    }

    private MultiPolygon readMultipatch( ByteBuffer buffer, int length ) {
        int numParts = buffer.getInt();
        int numPoints = buffer.getInt();

        if ( LOG.isTraceEnabled() ) {
            LOG.trace( "Reading multipatch with " + numParts + " parts and " + numPoints + " points." );
        }

        int[] partTypes = new int[numParts];
        int[] parts = new int[numParts];

        // read part info
        for ( int i = 0; i < numParts; ++i ) {
            parts[i] = buffer.getInt();
        }
        for ( int i = 0; i < numParts; ++i ) {
            partTypes[i] = buffer.getInt();
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
                points[i][k] = new double[] { buffer.getInt(), buffer.getInt(), 0, 0 };
            }
        }

        skipBytes( buffer, 16 ); // z boundary

        for ( int i = 0; i < numParts; ++i ) {
            // read points for part
            for ( int k = 0; k < points[i].length; ++k ) {
                points[i][k][2] = buffer.getInt();
            }
        }

        int len = 60 + 8 * numParts + 24 * numPoints;

        if ( length != len ) {
            skipBytes( buffer, 16 );
            for ( int i = 0; i < numParts; ++i ) {
                // read points for part
                for ( int k = 0; k < points[i].length; ++k ) {
                    points[i][k][3] = buffer.getInt();
                }
            }
        }

        return parseMultiPatch( points, partTypes );
    }

    // converts every triangle to a surface with an outer ring
    private LinkedList<Polygon> fromTriangleStrip( double[][] points ) {
        LinkedList<Point> ps = new LinkedList<Point>();

        for ( double[] p : points ) {
            ps.add( fac.createPoint( null, p, crs ) );
        }

        LinkedList<Polygon> ss = new LinkedList<Polygon>();

        while ( ps.size() > 2 ) {
            LinkedList<Point> ring = new LinkedList<Point>();
            ring.add( ps.get( 0 ) );
            ring.add( ps.get( 1 ) );
            ring.add( ps.get( 2 ) );
            ring.add( ring.getFirst() );
            ps.poll();
            LinearRing r = fac.createLinearRing( null, crs, new PointsList( ring ) );
            ss.add( fac.createPolygon( null, crs, r, null ) );
        }

        return ss;
    }

    // just uses an outer ring, all vertices and the first one again
    private LinkedList<Polygon> fromTriangleFan( double[][] points ) {
        LinkedList<Point> ps = new LinkedList<Point>();

        for ( double[] p : points ) {
            ps.add( fac.createPoint( null, p, crs ) );
        }

        LinkedList<Polygon> ss = new LinkedList<Polygon>();

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
    private static boolean isCCW( LinearRing c ) {
        Points ps = c.getControlPoints();
        Coordinate[] cs = new Coordinate[ps.size()];
        int i = 0;
        for ( Point p : ps ) {
            cs[i++] = new Coordinate( p.get0(), p.get1() );
        }

        return CGAlgorithms.isCCW( cs );
    }

    private MultiPolygon parseMultiPatch( double[][][] ps, int[] partTypes ) {
        LinkedList<Polygon> ss = new LinkedList<Polygon>();

        boolean outerRingMode = false;
        LinearRing outerRing = null;
        LinkedList<Ring> innerRings = new LinkedList<Ring>();

        boolean unknownRingMode = false;
        LinearRing unknownOuterRing = null;
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
                LinearRing ring = fromRing( ps[i] );
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
        return fac.createMultiPolygon( null, crs, ss );
    }

    private Points[] readLines( ByteBuffer buffer, boolean m, boolean z, int length ) {

        int coordDim = m ? 4 : ( z ? 3 : 2 );

        int numParts = buffer.getInt();
        int numPoints = buffer.getInt();

        PackedPoints[] res = new PackedPoints[numParts];
        int[] parts = new int[numParts];

        for ( int i = 0; i < numParts; ++i ) {
            parts[i] = buffer.getInt();
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
                coords[idx++] = buffer.getDouble();
                coords[idx++] = buffer.getDouble();
                if ( coordDim == 3 ) {
                    idx += 1;
                } else if ( coordDim == 4 ) {
                    idx += 2;
                }
            }
            res[i] = new PackedPoints( crs, coords, coordDim );
        }

        if ( !z && !m ) {
            return res;
        }

        int mlen = 44 + 4 * numParts + 16 * numPoints;
        int zlen = mlen + 16 + 8 * numPoints;

        if ( z ) {
            skipBytes( buffer, 16 );
            for ( int i = 0; i < numParts; ++i ) {
                double[] coords = res[i].getAsArray();
                for ( int j = 0; j < res[i].size(); ++j ) {
                    coords[2 + j * coordDim] = buffer.getDouble();
                }
            }
        }

        // untested...
        if ( m && mlen == length ) {
            for ( int i = 0; i < numParts; ++i ) {
                double[] coords = res[i].getAsArray();
                int num = coords.length / coordDim;
                int newDim = coordDim - 1;
                double[] newCoords = new double[num * newDim];
                for ( int j = 0; j < num; ++j ) {
                    int baseidx = j * newDim;
                    int basesrcidx = j * coordDim;
                    for ( int k = 0; k < newDim; ++k ) {
                        newCoords[k + baseidx] = coords[k + basesrcidx];
                    }
                }
                res[i] = new PackedPoints( crs, newCoords, newDim );
            }
            return res;
        }

        if ( ( m && mlen != length ) || ( z && zlen != length ) ) {
            skipBytes( buffer, 16 );
            for ( int i = 0; i < numParts; ++i ) {
                boolean allNodata = true;
                double[] coords = res[i].getAsArray();
                double val;
                for ( int j = 0; j < res[i].size(); ++j ) {
                    val = buffer.getDouble();
                    if ( val > -10E38 ) {
                        allNodata = false;
                    }
                    coords[3 + j * coordDim] = val;
                }
                if ( allNodata ) {
                    int newDim = coordDim - 1;
                    double[] newCoords = new double[newDim * res[i].size()];
                    for ( int j = 0; j < res[i].size(); ++j ) {
                        int baseidx = j * newDim;
                        int basesrcidx = j * coordDim;
                        for ( int k = 0; k < newDim; ++k ) {
                            newCoords[k + baseidx] = coords[k + basesrcidx];
                        }
                    }
                    res[i] = new PackedPoints( crs, newCoords, newDim );
                }
            }
        }

        return res;
    }

    /**
     * Closes the underlying file channel and random access file.
     * 
     * @throws IOException
     */
    public void close()
                            throws IOException {
        channel.close();
        file.close();
    }
}
