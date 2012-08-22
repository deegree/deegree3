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

import java.util.Arrays;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.ByteUtils;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.LineString;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.WKTAdapter;

/**
 * <code>ShapePolyline</code> corresponds to the Polyline, PolylineM and PolylineZ shapes of the shapefile spec.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ShapePolyline implements Shape {

    private static final ILogger LOG = LoggerFactory.getLogger( ShapePolyline.class );

    protected CoordinateSystem crs; // optional crs for geometry

    protected boolean isM, isZ;

    private ShapeEnvelope envelope;

    protected ShapePoint[][] points;

    /**
     * Empty constructor. Should be used in concert with read().
     */
    public ShapePolyline() {
        // all default
    }

    /**
     * Creates a new Polyline/M/Z.
     *
     * @param z
     * @param m
     */
    public ShapePolyline( boolean z, boolean m ) {
        isM = m;
        isZ = z;
    }

    /**
     * Creates a new Polyline/M/Z.
     *
     * @param z
     * @param m
     * @param crs
     *            CoordinateSystem for shape
     */
    public ShapePolyline( boolean z, boolean m, CoordinateSystem crs ) {
        isM = m;
        isZ = z;
        this.crs = crs;
    }

    /**
     * Creates a new PolylineZ from deegree Curves.
     *
     * @param cs
     */
    public ShapePolyline( List<Curve> cs ) {
        try {
            points = new ShapePoint[cs.size()][];
            int partNum = 0;
            for ( Curve c : cs ) {

                if ( envelope == null ) {
                    envelope = new ShapeEnvelope( c.getEnvelope() );
                }
                LineString ls = c.getAsLineString();

                points[partNum] = new ShapePoint[ls.getNumberOfPoints()];

                ShapePoint p;
                for ( int i = 0; i < ls.getNumberOfPoints(); i++ ) {
                    p = new ShapePoint( ls.getPositionAt( i ) );
                    points[partNum][i] = p;
                    envelope.fit( p.x, p.y, p.z );
                }
                ++partNum;

                // uses only the last one, but whatever will happen if they're not consistent anyway...
                if ( c.getDimension() == 3 ) {
                    isZ = true;
                }
            }
        } catch ( GeometryException e ) {
            LOG.logError( "Something was wrong with a Curve object. " + "This will probably lead to followup errors, "
                          + "better check your input data. Stack Trace:", e );
        }
    }

    /**
     * Creates a new PolylineZ from deegree Curve.
     *
     * @param c
     */
    public ShapePolyline( Curve c ) {
        this( Arrays.asList( new Curve[] { c } ) );
    }

    private int numPoints() {
        int num = 0;

        for ( ShapePoint[] ps : points ) {
            num += ps.length;
        }

        return num;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.io.shpapi.Shape#getByteLength()
     */
    public int getByteLength() {
        int numPoints = numPoints();

        int len = 44 + 4 * points.length + 16 * numPoints;

        if ( isM ) {
            len += 8 * numPoints + 16;
        }

        if ( isZ ) {
            len += 16 * numPoints + 32;
        }

        return len;
    }

    protected int readPolyline( byte[] bytes, int offset ) {
        int off = offset;
        envelope = new ShapeEnvelope( false, false );
        off = envelope.read( bytes, off );

        int numParts = ByteUtils.readLEInt( bytes, off );
        off += 4;

        int numPoints = ByteUtils.readLEInt( bytes, off );
        off += 4;

        points = new ShapePoint[numParts][];
        int[] partStart = new int[numParts];

        for ( int i = 0; i < numParts; ++i ) {
            partStart[i] = ByteUtils.readLEInt( bytes, off );
            off += 4;
        }

        for ( int i = 0; i < numParts; ++i ) {

            // calculate number of points for current part
            int len;
            if ( i == numParts - 1 ) {
                len = numPoints - partStart[i];
            } else {
                len = partStart[i + 1] - partStart[i];
            }

            points[i] = new ShapePoint[len];
            for ( int j = 0; j < len; ++j ) {
                points[i][j] = new ShapePoint( bytes, off );
                off += 16;
            }
        }

        return off;
    }

    protected int readPolylineZ( byte[] bytes, int offset ) {
        isZ = true;

        int off = readPolyline( bytes, offset );

        int numPoints = numPoints();

        double zmin, zmax, mmin, mmax;
        zmin = ByteUtils.readLEDouble( bytes, off );
        off += 8;
        zmax = ByteUtils.readLEDouble( bytes, off );
        off += 8;

        double[] zVals = new double[numPoints];
        for ( int i = 0; i < numPoints; ++i ) {
            zVals[i] = ByteUtils.readLEDouble( bytes, off );
            off += 8;
        }

        mmin = ByteUtils.readLEDouble( bytes, off );
        off += 8;
        mmax = ByteUtils.readLEDouble( bytes, off );
        off += 8;

        double[] mVals = new double[numPoints];
        for ( int i = 0; i < numPoints; ++i ) {
            mVals[i] = ByteUtils.readLEDouble( bytes, off );
            off += 8;
        }

        int i = 0;
        for ( ShapePoint[] ps : points ) {
            for ( ShapePoint p : ps ) {
                p.extend( zVals[i], mVals[i] );
                ++i;
            }
        }

        envelope.extend( zmin, zmax, mmin, mmax );

        return off;
    }

    protected int readPolylineM( byte[] bytes, int offset ) {
        isM = true;

        int off = readPolyline( bytes, offset );

        int numPoints = numPoints();

        double mmin, mmax;
        mmin = ByteUtils.readLEDouble( bytes, off );
        off += 8;
        mmax = ByteUtils.readLEDouble( bytes, off );
        off += 8;

        envelope.extend( mmin, mmax );

        double[] mVals = new double[numPoints];
        for ( int i = 0; i < numPoints; ++i ) {
            mVals[i] = ByteUtils.readLEDouble( bytes, off );
            off += 8;
        }

        int i = 0;
        for ( ShapePoint[] ps : points ) {
            for ( ShapePoint p : ps ) {
                p.extend( mVals[i] );
                ++i;
            }
        }

        return off;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.io.shpapi.Shape#read(byte[], int)
     */
    public int read( byte[] bytes, int offset ) {
        int off = offset;

        int type = ByteUtils.readLEInt( bytes, off );
        off += 4;

        if ( type == ShapeFile.NULL ) {
            return off;
        }

        if ( type == ShapeFile.POLYLINE ) {
            isZ = false;
            isM = false;
            return readPolyline( bytes, off );
        }

        if ( type == ShapeFile.POLYLINEZ ) {
            isZ = true;
            isM = false;
            return readPolylineZ( bytes, off );
        }

        if ( type == ShapeFile.POLYLINEM ) {
            isZ = false;
            isM = true;
            return readPolylineM( bytes, off );
        }

        return -1;
    }

    protected int writePolyline( byte[] bytes, int offset ) {
        int off = envelope.write( bytes, offset );

        ByteUtils.writeLEInt( bytes, off, points.length );
        off += 4;

        int numPoints = numPoints();
        ByteUtils.writeLEInt( bytes, off, numPoints );
        off += 4;

        int pos = 0;
        for ( ShapePoint[] ps : points ) {
            ByteUtils.writeLEInt( bytes, off, pos );
            off += 4;
            pos += ps.length;
        }

        for ( ShapePoint[] ps : points ) {
            for ( ShapePoint p : ps ) {
                ByteUtils.writeLEDouble( bytes, off, p.x );
                off += 8;
                ByteUtils.writeLEDouble( bytes, off, p.y );
                off += 8;
            }
        }

        return off;
    }

    protected int writePolylineZ( byte[] bytes, int offset ) {
        int off = writePolyline( bytes, offset );

        ByteUtils.writeLEDouble( bytes, off, envelope.zmin );
        off += 8;
        ByteUtils.writeLEDouble( bytes, off, envelope.zmax );
        off += 8;

        for ( ShapePoint[] ps : points ) {
            for ( ShapePoint p : ps ) {
                ByteUtils.writeLEDouble( bytes, off, p.z );
                off += 8;
            }
        }

        ByteUtils.writeLEDouble( bytes, off, envelope.mmin );
        off += 8;
        ByteUtils.writeLEDouble( bytes, off, envelope.mmax );
        off += 8;

        for ( ShapePoint[] ps : points ) {
            for ( ShapePoint p : ps ) {
                ByteUtils.writeLEDouble( bytes, off, p.m );
                off += 8;
            }
        }

        return off;
    }

    protected int writePolylineM( byte[] bytes, int offset ) {
        int off = writePolyline( bytes, offset );

        ByteUtils.writeLEDouble( bytes, off, envelope.mmin );
        off += 8;
        ByteUtils.writeLEDouble( bytes, off, envelope.mmax );
        off += 8;

        for ( ShapePoint[] ps : points ) {
            for ( ShapePoint p : ps ) {
                ByteUtils.writeLEDouble( bytes, off, p.m );
                off += 8;
            }
        }

        return off;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.io.shpapi.Shape#write(byte[], int)
     */
    public int write( byte[] bytes, int offset ) {
        if ( isZ ) {
            ByteUtils.writeLEInt( bytes, offset, ShapeFile.POLYLINEZ );
            return writePolylineZ( bytes, offset + 4 );
        }
        if ( isM ) {
            ByteUtils.writeLEInt( bytes, offset, ShapeFile.POLYLINEM );
            return writePolylineM( bytes, offset + 4 );
        }
        ByteUtils.writeLEInt( bytes, offset, ShapeFile.POLYLINE );
        return writePolyline( bytes, offset + 4 );
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.io.shpapi.shape_new.Shape#getType()
     */
    public int getType() {
        if ( isZ ) {
            return ShapeFile.POLYLINEZ;
        }
        if ( isM ) {
            return ShapeFile.POLYLINEM;
        }
        return ShapeFile.POLYLINE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.io.shpapi.shape_new.Shape#getEnvelope()
     */
    public ShapeEnvelope getEnvelope() {
        return envelope;
    }

    /**
     * This creates a MultiCurve object.
     *
     * @see org.deegree.io.shpapi.shape_new.Shape#getGeometry()
     */
    public Geometry getGeometry()
                            throws ShapeGeometryException {
        if ( points == null ) {
            return null;
        }
        try {
            Curve[] cs = new Curve[points.length];

            for ( int i = 0; i < points.length; ++i ) {
                Position[] ps = new Position[points[i].length];
                for ( int k = 0; k < points[i].length; ++k ) {
                    if ( isZ ) {
                        ps[k] = GeometryFactory.createPosition( points[i][k].x, points[i][k].y, points[i][k].z );
                    } else {
                        ps[k] = GeometryFactory.createPosition( points[i][k].x, points[i][k].y );
                    }
                }
                cs[i] = GeometryFactory.createCurve( ps, crs );
            }

            return GeometryFactory.createMultiCurve( cs, crs );
        } catch ( GeometryException e ) {
            throw new ShapeGeometryException( "MultiCurve could not be constructed" + " from ShapePolyline.", e );
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
