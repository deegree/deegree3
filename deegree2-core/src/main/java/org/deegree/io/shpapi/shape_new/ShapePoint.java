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
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.WKTAdapter;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <code>ShapePoint</code> corresponds to Point, PointZ and PointM in shapefile terminology.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ShapePoint implements Shape {

    private CoordinateSystem crs; // optional crs for geometry

    /**
     *
     */
    public boolean isZ;

    /**
     *
     */
    public boolean isM;

    /**
     * The x value.
     */
    public double x;

    /**
     * The y value.
     */
    public double y;

    /**
     * The z value.
     */
    public double z;

    /**
     * The m value.
     */
    public double m;

    private boolean isNull;

    /**
     * Constructs one without values, use read to fill it.
     *
     * @param z
     *            if PointZ
     * @param m
     *            if PointM
     */
    public ShapePoint( boolean z, boolean m ) {
        isZ = z;
        isM = m;
    }

    /**
     * Constructs one without values, use read to fill it.
     *
     * @param z
     *            if PointZ
     * @param m
     *            if PointM
     * @param crs
     *            CoordinateSystem of the shape
     */
    public ShapePoint( boolean z, boolean m, CoordinateSystem crs ) {
        isZ = z;
        isM = m;
        this.crs = crs;
    }

    /**
     * Creates a new PointZ from deegree Point.
     *
     * @param p
     */
    public ShapePoint( Point p ) {
        x = p.getX();
        y = p.getY();
        z = p.getZ();
        if ( p.getCoordinateDimension() == 3 ) {
            isZ = true;
        }
    }

    /**
     * Creates a new PointZ from deegree Position.
     *
     * @param p
     */
    public ShapePoint( Position p ) {
        x = p.getX();
        y = p.getY();
        z = p.getZ();
        if ( p.getCoordinateDimension() == 3 ) {
            isZ = true;
        }
    }

    /**
     * Just reads x and y from the byte array.
     *
     * @param bytes
     * @param offset
     */
    public ShapePoint( byte[] bytes, int offset ) {
        x = ByteUtils.readLEDouble( bytes, offset );
        y = ByteUtils.readLEDouble( bytes, offset + 8 );
    }

    /**
     * @return the Point as (x, y, NaN), the PointZ as (x, y, z) Coordinate
     */
    public Coordinate export() {
        if ( isZ ) {
            return new Coordinate( x, y, z );
        }
        return new Coordinate( x, y );
    }

    /**
     * Extends this point with z and m values, so it is a PointZ
     *
     * @param zVal
     * @param mVal
     */
    public void extend( double zVal, double mVal ) {
        z = zVal;
        m = mVal;
        isZ = true;
    }

    /**
     * Extends this point with m values, so it is a PointM
     *
     * @param mVal
     */
    public void extend( double mVal ) {
        m = mVal;
        isM = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.io.shpapi.Shape#getByteLength()
     */
    public int getByteLength() {
        if ( isZ ) {
            return 36;
        }
        if ( isM ) {
            return 28;
        }
        return 20;
    }

    private int readPoint( byte[] bytes, int offset ) {
        int off = offset;
        isM = false;
        isZ = false;
        x = ByteUtils.readLEDouble( bytes, off );

        off += 8;

        y = ByteUtils.readLEDouble( bytes, off );

        off += 8;
        return off;
    }

    private int readPointZ( byte[] bytes, int offset ) {
        int off = offset;
        isM = false;
        isZ = true;
        x = ByteUtils.readLEDouble( bytes, off );

        off += 8;

        y = ByteUtils.readLEDouble( bytes, off );

        off += 8;

        z = ByteUtils.readLEDouble( bytes, off );

        off += 8;

        m = ByteUtils.readLEDouble( bytes, off );

        off += 8;

        return off;
    }

    private int readPointM( byte[] bytes, int offset ) {
        int off = offset;
        isM = true;
        isZ = false;
        x = ByteUtils.readLEDouble( bytes, off );

        off += 8;

        y = ByteUtils.readLEDouble( bytes, off );

        off += 8;

        m = ByteUtils.readLEDouble( bytes, off );

        off += 8;
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
            isNull = true;
            return off;
        }

        if ( type == ShapeFile.POINT ) {
            return readPoint( bytes, off );
        }

        if ( type == ShapeFile.POINTZ ) {
            return readPointZ( bytes, off );
        }

        if ( type == ShapeFile.POINTM ) {
            return readPointM( bytes, off );
        }

        return -1;
    }

    private int writePoint( byte[] bytes, int offset ) {
        int off = offset;

        ByteUtils.writeLEDouble( bytes, off, x );
        off += 8;

        ByteUtils.writeLEDouble( bytes, off, y );
        off += 8;

        return off;
    }

    private int writePointZ( byte[] bytes, int offset ) {
        int off = writePoint( bytes, offset );

        ByteUtils.writeLEDouble( bytes, off, z );
        off += 8;

        ByteUtils.writeLEDouble( bytes, off, m );
        off += 8;

        return off;
    }

    private int writePointM( byte[] bytes, int offset ) {
        int off = writePoint( bytes, offset );

        ByteUtils.writeLEDouble( bytes, off, m );
        off += 8;

        return off;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.io.shpapi.Shape#write(byte[], int)
     */
    public int write( byte[] bytes, int offset ) {
        if ( isZ ) {
            ByteUtils.writeLEInt( bytes, offset, ShapeFile.POINTZ );
            return writePointZ( bytes, offset + 4 );
        }
        if ( isM ) {
            ByteUtils.writeLEInt( bytes, offset, ShapeFile.POINTM );
            return writePointM( bytes, offset + 4 );
        }

        ByteUtils.writeLEInt( bytes, offset, ShapeFile.POINT );
        return writePoint( bytes, offset + 4 );
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.io.shpapi.shape_new.Shape#getType()
     */
    public int getType() {
        if ( isZ ) {
            return ShapeFile.POINTZ;
        }
        if ( isM ) {
            return ShapeFile.POINTM;
        }
        return ShapeFile.POINT;
    }

    /**
     * @return null, points do not have an envelope
     * @see org.deegree.io.shpapi.shape_new.Shape#getEnvelope()
     */
    public ShapeEnvelope getEnvelope() {
        return null;
    }

    /**
     * This creates a Point object.
     *
     * @see org.deegree.io.shpapi.shape_new.Shape#getGeometry()
     */
    public Geometry getGeometry()
                            throws ShapeGeometryException {
        if ( isNull ) {
            return null;
        }
        if ( isZ ) {
            return GeometryFactory.createPoint( x, y, z, crs );
        }
        return GeometryFactory.createPoint( x, y, crs );
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
