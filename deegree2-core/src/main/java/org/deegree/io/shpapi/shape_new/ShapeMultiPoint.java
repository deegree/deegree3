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

import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.ByteUtils;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.MultiPoint;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.WKTAdapter;

/**
 * <code>ShapeMultiPoint</code> encapsulates shapefile MultiPoint/M/Z structures.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ShapeMultiPoint implements Shape {

    private CoordinateSystem crs; // optional crs for geometry

    private boolean isM, isZ;

    private ShapeEnvelope envelope;

    protected ShapePoint[] points;

    /**
     * @param isZ
     * @param isM
     */
    public ShapeMultiPoint( boolean isZ, boolean isM ) {
        this.isZ = isZ;
        this.isM = isM;
    }

    /**
     * @param isZ
     * @param isM
     * @param crs
     *            CoordinateSystem for shape
     */
    public ShapeMultiPoint( boolean isZ, boolean isM, CoordinateSystem crs ) {
        this.isZ = isZ;
        this.isM = isM;
        this.crs = crs;
    }

    /**
     * Creates MultiPointZ from deegree MultiPoint
     *
     * @param p
     */
    public ShapeMultiPoint( MultiPoint p ) {
        if ( p.getDimension() == 3 ) {
            isZ = true;
        }
        envelope = new ShapeEnvelope( p.getEnvelope() );

        Point[] ps = p.getAllPoints();
        points = new ShapePoint[ps.length];

        for ( int i = 0; i < ps.length; ++i ) {
            points[i] = new ShapePoint( ps[i] );
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.io.shpapi.Shape#getByteLength()
     */
    public int getByteLength() {
        int len = 44 + points.length * 16;
        if ( isZ ) {
            len += 32 + points.length * 16;
        }
        if ( isM ) {
            len += 16 + points.length * 8;
        }
        return len;
    }

    private int readMultiPoint( byte[] bytes, int offset ) {
        int off = offset;
        envelope = new ShapeEnvelope( false, false );
        off = envelope.read( bytes, off );

        int numPoints = ByteUtils.readLEInt( bytes, off );
        off += 4;

        points = new ShapePoint[numPoints];

        for ( int i = 0; i < numPoints; ++i ) {
            points[i] = new ShapePoint( bytes, off );
            off += 16;
        }

        return off;
    }

    private int readMultiPointZ( byte[] bytes, int offset ) {
        int off = readMultiPoint( bytes, offset );

        double zmin, zmax;

        zmin = ByteUtils.readLEDouble( bytes, off );
        off += 8;
        zmax = ByteUtils.readLEDouble( bytes, off );
        off += 8;

        double[] zVals = new double[points.length];
        for ( int i = 0; i < points.length; ++i ) {
            zVals[i] = ByteUtils.readLEDouble( bytes, off );
            off += 8;
        }

        double mmin, mmax;

        mmin = ByteUtils.readLEDouble( bytes, off );
        off += 8;
        mmax = ByteUtils.readLEDouble( bytes, off );
        off += 8;

        double[] mVals = new double[points.length];
        for ( int i = 0; i < points.length; ++i ) {
            mVals[i] = ByteUtils.readLEDouble( bytes, off );
            off += 8;
        }

        envelope.extend( zmin, zmax, mmin, mmax );

        for ( int i = 0; i < points.length; ++i ) {
            points[i].extend( zVals[i], mVals[i] );
        }

        return off;
    }

    private int readMultiPointM( byte[] bytes, int offset ) {
        int off = readMultiPoint( bytes, offset );

        double mmin, mmax;

        mmin = ByteUtils.readLEDouble( bytes, off );
        off += 8;
        mmax = ByteUtils.readLEDouble( bytes, off );
        off += 8;

        envelope.extend( mmin, mmax );

        double m;
        for ( int i = 0; i < points.length; ++i ) {
            m = ByteUtils.readLEDouble( bytes, off );
            off += 8;
            points[i].extend( m );
        }

        return off;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.io.shpapi.Shape#read(byte[], int)
     */
    public int read( byte[] bytes, int offset ) {
        int t = ByteUtils.readLEInt( bytes, offset );
        int off = offset + 4;

        if ( t == ShapeFile.NULL ) {
            return off;
        }

        if ( t == ShapeFile.MULTIPOINTZ ) {
            return readMultiPointZ( bytes, off );
        }

        if ( t == ShapeFile.MULTIPOINTM ) {
            return readMultiPointM( bytes, off );
        }

        if ( t == ShapeFile.MULTIPOINT ) {
            return readMultiPoint( bytes, off );
        }

        return -1;
    }

    private int writeMultiPoint( byte[] bytes, int offset ) {
        int off = envelope.write( bytes, offset );

        ByteUtils.writeLEInt( bytes, off, points.length );
        off += 4;

        for ( int i = 0; i < points.length; ++i ) {
            ByteUtils.writeLEDouble( bytes, off, points[i].x );
            off += 8;
            ByteUtils.writeLEDouble( bytes, off, points[i].y );
            off += 8;
        }

        return off;
    }

    private int writeMultiPointZ( byte[] bytes, int offset ) {
        int off = writeMultiPoint( bytes, offset );

        ByteUtils.writeLEDouble( bytes, off, envelope.zmin );
        off += 8;
        ByteUtils.writeLEDouble( bytes, off, envelope.zmax );
        off += 8;

        for ( int i = 0; i < points.length; ++i ) {
            ByteUtils.writeLEDouble( bytes, off, points[i].z );
            off += 8;
        }

        ByteUtils.writeLEDouble( bytes, off, envelope.mmin );
        off += 8;
        ByteUtils.writeLEDouble( bytes, off, envelope.mmax );
        off += 8;

        for ( int i = 0; i < points.length; ++i ) {
            ByteUtils.writeLEDouble( bytes, off, points[i].m );
            off += 8;
        }

        return off;
    }

    private int writeMultiPointM( byte[] bytes, int offset ) {
        int off = writeMultiPoint( bytes, offset );

        ByteUtils.writeLEDouble( bytes, off, envelope.mmin );
        off += 8;
        ByteUtils.writeLEDouble( bytes, off, envelope.mmax );
        off += 8;

        for ( int i = 0; i < points.length; ++i ) {
            ByteUtils.writeLEDouble( bytes, off, points[i].m );
            off += 8;
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
            ByteUtils.writeLEInt( bytes, offset, ShapeFile.MULTIPOINTZ );
            return writeMultiPointZ( bytes, offset + 4 );
        }
        if ( isM ) {
            ByteUtils.writeLEInt( bytes, offset, ShapeFile.MULTIPOINTM );
            return writeMultiPointM( bytes, offset + 4 );
        }
        ByteUtils.writeLEInt( bytes, offset, ShapeFile.MULTIPOINT );
        return writeMultiPoint( bytes, offset + 4 );
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.io.shpapi.shape_new.Shape#getType()
     */
    public int getType() {
        if ( isZ ) {
            return ShapeFile.MULTIPOINTZ;
        }
        if ( isM ) {
            return ShapeFile.MULTIPOINTM;
        }
        return ShapeFile.MULTIPOINT;
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
     * This creates a MultiPoint object.
     *
     * @see org.deegree.io.shpapi.shape_new.Shape#getGeometry()
     */
    public Geometry getGeometry()
                            throws ShapeGeometryException {
        if ( points == null ) {
            return null;
        }
        Point[] ps = new Point[points.length];

        for ( int i = 0; i < ps.length; ++i ) {
            ps[i] = (Point) points[i].getGeometry();
        }

        return GeometryFactory.createMultiPoint( ps, crs );
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
