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
package org.deegree.io.shpapi.shape_new;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.ByteUtils;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.CurveSegment;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.JTSAdapter;
import org.deegree.model.spatialschema.LineString;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Ring;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.WKTAdapter;

import com.vividsolutions.jts.algorithm.CGAlgorithms;

/**
 * <code>ShapeMultiPatch</code> corresponds to a shapefile MultiPatch object.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ShapeMultiPatch implements Shape {

    private CoordinateSystem crs; // optional crs for geometry

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

    private static final ILogger LOG = LoggerFactory.getLogger( ShapeMultiPatch.class );

    private ShapeEnvelope envelope;

    private int numberParts, numberPoints;

    private int[] partTypes;

    private ShapePoint[][] points;

    private int expectedLength;

    /**
     * Empty constructor, used for reading.
     *
     * @param length
     *            expected length for reading
     */
    public ShapeMultiPatch( int length ) {
        expectedLength = length;
    }

    /**
     * Empty constructor, used for reading.
     *
     * @param length
     *            expected length for reading
     * @param crs
     *            CoordinateSystem for shape
     */
    public ShapeMultiPatch( int length, CoordinateSystem crs ) {
        expectedLength = length;
        this.crs = crs;
    }

    /**
     * Constructs one from deegree MultiSurface. Every Surface is stored as an OUTER_RING followed
     * by its INNER_RINGs (these are the only two part types used here).
     *
     * @param f
     */
    public ShapeMultiPatch( MultiSurface f ) {

        // just an estimation, could be more
        ArrayList<ShapePoint[]> parts = new ArrayList<ShapePoint[]>( f.getSize() * 2 );
        ArrayList<Integer> types = new ArrayList<Integer>( f.getSize() * 2 );
        envelope = new ShapeEnvelope( f.getEnvelope() );
        try {
            points = new ShapePoint[f.getSize()][];
            Surface[] surfaces = f.getAllSurfaces();
            for ( Surface s : surfaces ) {

                // add exterior ring first
                CurveSegment cs = s.getSurfaceBoundary().getExteriorRing().getAsCurveSegment();
                addCurve( parts, types, GeometryFactory.createCurve( cs ), true );

                // then, add inner rings
                Ring[] innerRings = s.getSurfaceBoundary().getInteriorRings();

                if ( innerRings != null ) {
                    for ( Ring r : innerRings ) {
                        cs = r.getAsCurveSegment();
                        addCurve( parts, types, GeometryFactory.createCurve( cs ), false );
                    }
                }

            }

            // this does not exist in Collections or Array?!? D'oh!
            partTypes = new int[types.size()];
            for ( int i = 0; i < types.size(); ++i ) {
                partTypes[i] = types.get( i ).intValue();
            }
            points = parts.toArray( new ShapePoint[parts.size()][] );

            numberParts = parts.size();

            numberPoints = 0;
            for ( ShapePoint[] ps : points ) {
                numberPoints += ps.length;
            }

        } catch ( GeometryException e ) {
            LOG.logError( "Something was wrong with a Curve object. " + "This will probably lead to followup errors, "
                          + "better check your input data. Stack Trace:", e );
        }
    }

    private void addCurve( List<ShapePoint[]> parts, List<Integer> types, Curve c, boolean outer )
                            throws GeometryException {
        if ( outer ) {
            types.add( new Integer( OUTER_RING ) );
        } else {
            types.add( new Integer( INNER_RING ) );
        }
        LineString ls = c.getAsLineString();

        ShapePoint[] ps = new ShapePoint[ls.getNumberOfPoints()];

        ShapePoint p;
        for ( int i = 0; i < ls.getNumberOfPoints(); i++ ) {
            p = new ShapePoint( ls.getPositionAt( i ) );
            ps[i] = p;
            envelope.fit( p.x, p.y, p.z );
        }
        parts.add( ps );
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.io.shpapi.shape_new.Shape#getByteLength()
     */
    public int getByteLength() {
        return 44 + 4 * numberParts + 4 * numberParts + 16 * numberPoints + 16 + 8 * numberPoints + 16 + 8
               * numberPoints;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.io.shpapi.shape_new.Shape#getEnvelope()
     */
    public ShapeEnvelope getEnvelope() {
        return envelope;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.io.shpapi.shape_new.Shape#getType()
     */
    public int getType() {
        return ShapeFile.MULTIPATCH;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.io.shpapi.shape_new.Shape#read(byte[], int)
     */
    public int read( byte[] bytes, int offset ) {
        int back = offset;
        int type = ByteUtils.readLEInt( bytes, offset );
        offset += 4;

        if ( type == ShapeFile.NULL ) {
            LOG.logInfo( "Read null shape." );
            return offset;
        }

        envelope = new ShapeEnvelope( true, false );

        if ( type == ShapeFile.MULTIPATCH ) {
            offset = envelope.read( bytes, offset );
            numberParts = ByteUtils.readLEInt( bytes, offset );
            offset += 4;
            numberPoints = ByteUtils.readLEInt( bytes, offset );
            offset += 4;

            LOG.logDebug( "Reading " + numberParts + " parts with a total of " + numberPoints + " points." );

            partTypes = new int[numberParts];
            int[] parts = new int[numberParts];

            // read part info
            for ( int i = 0; i < numberParts; ++i ) {
                parts[i] = ByteUtils.readLEInt( bytes, offset );
                offset += 4;
            }
            for ( int i = 0; i < numberParts; ++i ) {
                partTypes[i] = ByteUtils.readLEInt( bytes, offset );
                offset += 4;
            }

            // read points
            points = new ShapePoint[numberParts][];
            for ( int i = 0; i < numberParts; ++i ) {
                // get length of current part
                int max;
                if ( i == numberParts - 1 ) {
                    max = numberPoints;
                } else {
                    max = parts[i + 1];
                }
                points[i] = new ShapePoint[max - parts[i]];

                // read points for part
                for ( int k = 0; k < points[i].length; ++k ) {
                    points[i][k] = new ShapePoint( bytes, offset );
                    offset += 16;
                }
            }

            double zmin, zmax, mmin, mmax;
            zmin = ByteUtils.readLEDouble( bytes, offset );
            offset += 8;
            zmax = ByteUtils.readLEDouble( bytes, offset );
            offset += 8;

            double[] zVals = new double[numberPoints];
            for ( int i = 0; i < numberPoints; ++i ) {
                zVals[i] = ByteUtils.readLEDouble( bytes, offset );
                offset += 8;
            }

            // check for broken (?) files that omit the M-section completely:
            if ( expectedLength == ( ( offset - back ) + 8 ) ) {
                return offset;
            }

            mmin = ByteUtils.readLEDouble( bytes, offset );
            offset += 8;
            mmax = ByteUtils.readLEDouble( bytes, offset );
            offset += 8;

            double[] mVals = new double[numberPoints];
            for ( int i = 0; i < numberPoints; ++i ) {
                mVals[i] = ByteUtils.readLEDouble( bytes, offset );
                offset += 8;
            }

            int i = 0;
            for ( ShapePoint[] ps : points ) {
                for ( ShapePoint p : ps ) {
                    p.extend( zVals[i], mVals[i] );
                    ++i;
                }
            }

            envelope.extend( zmin, zmax, mmin, mmax );

        } else {
            LOG.logError( "Shape type was unexpectedly not Multipatch?" );
        }

        return offset;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.io.shpapi.shape_new.Shape#write(byte[], int)
     */
    public int write( byte[] bytes, int offset ) {
        ByteUtils.writeLEInt( bytes, offset, ShapeFile.MULTIPATCH );
        offset += 4;

        offset = envelope.write( bytes, offset );

        ByteUtils.writeLEInt( bytes, offset, points.length );
        offset += 4;

        ByteUtils.writeLEInt( bytes, offset, numberPoints );
        offset += 4;

        int pos = 0;
        for ( ShapePoint[] ps : points ) {
            ByteUtils.writeLEInt( bytes, offset, pos );
            offset += 4;
            pos += ps.length;
        }

        for ( int i = 0; i < partTypes.length; ++i ) {
            ByteUtils.writeLEInt( bytes, offset, partTypes[i] );
            offset += 4;
        }

        for ( ShapePoint[] ps : points ) {
            for ( ShapePoint p : ps ) {
                ByteUtils.writeLEDouble( bytes, offset, p.x );
                offset += 8;
                ByteUtils.writeLEDouble( bytes, offset, p.y );
                offset += 8;
            }
        }

        // write the m and z part
        ByteUtils.writeLEDouble( bytes, offset, envelope.zmin );
        offset += 8;
        ByteUtils.writeLEDouble( bytes, offset, envelope.zmax );
        offset += 8;

        for ( ShapePoint[] ps : points ) {
            for ( ShapePoint p : ps ) {
                ByteUtils.writeLEDouble( bytes, offset, p.z );
                offset += 8;
            }
        }

        ByteUtils.writeLEDouble( bytes, offset, envelope.mmin );
        offset += 8;
        ByteUtils.writeLEDouble( bytes, offset, envelope.mmax );
        offset += 8;

        for ( ShapePoint[] ps : points ) {
            for ( ShapePoint p : ps ) {
                ByteUtils.writeLEDouble( bytes, offset, p.m );
                offset += 8;
            }
        }

        return offset;
    }

    // converts every triangle to a surface with an outer ring
    private LinkedList<Surface> fromTriangleStrip( ShapePoint[] points )
                            throws GeometryException {
        LinkedList<Position> ps = new LinkedList<Position>();

        for ( ShapePoint p : points ) {
            if ( p.isZ ) {
                ps.add( GeometryFactory.createPosition( p.x, p.y, p.z ) );
            } else {
                ps.add( GeometryFactory.createPosition( p.x, p.y ) );
            }
        }

        LinkedList<Surface> ss = new LinkedList<Surface>();

        while ( ps.size() > 2 ) {
            Position[] ring = new Position[4];
            ring[0] = ps.get( 0 );
            ring[1] = ps.get( 1 );
            ring[2] = ps.get( 2 );
            ring[3] = ring[0];
            ps.poll();
            ss.add( GeometryFactory.createSurface( ring, null, null, crs ) );
        }

        return ss;
    }

    // just uses an outer ring, all vertices and the first one again
    private LinkedList<Surface> fromTriangleFan( ShapePoint[] points )
                            throws GeometryException {
        LinkedList<Position> ps = new LinkedList<Position>();

        for ( ShapePoint p : points ) {
            if ( p.isZ ) {
                ps.add( GeometryFactory.createPosition( p.x, p.y, p.z ) );
            } else {
                ps.add( GeometryFactory.createPosition( p.x, p.y ) );
            }
        }

        LinkedList<Surface> ss = new LinkedList<Surface>();

        Position center = ps.poll();

        while ( ps.size() > 1 ) {
            Position[] ring = new Position[4];
            ring[0] = center;
            ring[1] = ps.get( 0 );
            ring[2] = ps.get( 1 );
            ring[3] = center;
            ps.poll();
            ss.add( GeometryFactory.createSurface( ring, null, null, crs ) );
        }

        return ss;
    }

    private Position[] fromRing( ShapePoint[] points ) {
        Position[] ps = new Position[points.length];

        for ( int i = 0; i < points.length; ++i ) {
            if ( points[i].isZ ) {
                ps[i] = GeometryFactory.createPosition( points[i].x, points[i].y, points[i].z );
            } else {
                ps[i] = GeometryFactory.createPosition( points[i].x, points[i].y );
            }
        }

        if ( ps[0] != ps[ps.length - 1] ) {
            LOG.logDebug( "Ring was not closed as required by the shape file spec!" );
            LOG.logDebug( "Trying to recover anyway." );

            // append first point, have to copy arrays
            Position[] ps2 = new Position[points.length + 1];
            for ( int i = 0; i < ps.length; ++i ) {
                ps2[i] = ps[i];
            }
            ps2[ps.length] = ps[0];
            ps = ps2;
        }

        return ps;
    }

    private boolean isClockwise( Position[] ring ) {
        return !CGAlgorithms.isCCW( JTSAdapter.export( ring ).getCoordinates() );
    }

    /**
     * Creates a MultiSurface object.
     *
     * @see org.deegree.io.shpapi.shape_new.Shape#getGeometry()
     */
    public Geometry getGeometry()
                            throws ShapeGeometryException {
        if ( points == null ) {
            LOG.logWarning( "Trying to export null geometry." );
            return null;
        }
        try {
            LinkedList<Surface> ss = new LinkedList<Surface>();

            boolean outerRingMode = false;
            Position[] outerRing = null;
            LinkedList<Position[]> innerRings = new LinkedList<Position[]>();

            boolean unknownRingMode = false;
            Position[] unknownOuterRing = null;
            LinkedList<Position[]> unknownInnerRings = new LinkedList<Position[]>();

            for ( int i = 0; i < partTypes.length; ++i ) {
                switch ( partTypes[i] ) {
                case TRIANGLE_STRIP:
                    ss.addAll( fromTriangleStrip( points[i] ) );
                    LOG.logDebug( "Read triangle strip." );
                    break;
                case TRIANGLE_FAN:
                    ss.addAll( fromTriangleFan( points[i] ) );
                    LOG.logDebug( "Read triangle fan." );
                    break;
                case OUTER_RING:
                    outerRingMode = true;
                    outerRing = fromRing( points[i] );
                    LOG.logDebug( "Read outer ring." );
                    break;
                case INNER_RING:
                    if ( !outerRingMode ) {
                        LOG.logWarning( "Found inner ring without preceding outer ring." );
                        break;
                    }
                    innerRings.add( fromRing( points[i] ) );
                    LOG.logDebug( "Read inner ring." );
                    break;
                case FIRST_RING:
                    LOG.logDebug( "Read first ring." );
                    unknownRingMode = true;
                    Position[] ring = fromRing( points[i] );
                    if ( isClockwise( ring ) ) {
                        innerRings.add( ring );
                    } else {
                        outerRing = ring;
                    }
                    break;
                case RING:
                    LOG.logDebug( "Read ring." );
                    ring = fromRing( points[i] );
                    if ( !unknownRingMode ) {
                        ss.add( GeometryFactory.createSurface( ring, null, null, crs ) );
                    } else {
                        if ( isClockwise( ring ) ) {
                            innerRings.add( ring );
                        } else {
                            outerRing = ring;
                        }
                    }

                    break;

                }
                if ( outerRingMode ) {
                    if ( partTypes[i] != INNER_RING ) {
                        ss.add( GeometryFactory.createSurface( outerRing,
                                                               innerRings.toArray( new Position[innerRings.size()][] ),
                                                               null, crs ) );
                        innerRings.clear();
                        outerRing = null;
                        outerRingMode = false;
                    }
                }
                if ( unknownRingMode ) {
                    if ( partTypes[i] != RING ) {
                        if ( unknownOuterRing == null ) {
                            while ( unknownInnerRings.size() != 0 ) {
                                ss.add( GeometryFactory.createSurface( unknownInnerRings.poll(), null, null, crs ) );
                            }
                        } else {
                            ss.add( GeometryFactory.createSurface(
                                                                   unknownOuterRing,
                                                                   unknownInnerRings.toArray( new Position[unknownInnerRings.size()][] ),
                                                                   null, crs ) );
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
            return GeometryFactory.createMultiSurface( ss.toArray( new Surface[ss.size()] ), crs );
        } catch ( GeometryException e ) {
            throw new ShapeGeometryException( "", e );
        }
    }

    @Override
    public String toString() {
        try {
            return WKTAdapter.export( getGeometry() ).toString();
        } catch ( GeometryException e ) {
            return "(unknown)";
        }
    }

}
