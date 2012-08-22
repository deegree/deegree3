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

import org.deegree.model.spatialschema.ByteUtils;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;

/**
 * <code>ShapeEnvelope</code> encapsulates a shapefile envelope.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ShapeEnvelope implements Shape {

    /**
     * The minimum x value.
     */
    public double xmin;

    /**
     * The maximum x value.
     */
    public double xmax;

    /**
     * The minimum y value.
     */
    public double ymin;

    /**
     * The maximum y value.
     */
    public double ymax;

    /**
     * The minimum z value.
     */
    public double zmin;

    /**
     * The maximum z value.
     */
    public double zmax;

    /**
     * The minimum m value.
     */
    public double mmin;

    /**
     * The maximum m value.
     */
    public double mmax;

    private boolean isZ, isM;

    /**
     * Copy constructor. Better to do with clone()?
     *
     * @param s
     */
    public ShapeEnvelope( ShapeEnvelope s ) {
        xmin = s.xmin;
        xmax = s.xmax;
        ymin = s.ymin;
        ymax = s.ymax;
        zmin = s.zmin;
        zmax = s.zmax;
        mmin = s.mmin;
        mmax = s.mmax;
        isZ = s.isZ;
        isM = s.isM;
    }

    /**
     * Creates a new envelope, with/out z and m dimensions as specified.
     *
     * @param z
     * @param m
     */
    public ShapeEnvelope( boolean z, boolean m ) {
        isZ = z;
        isM = m;
    }

    /**
     * Construct one from deegree Envelope.
     *
     * @param env
     */
    public ShapeEnvelope( Envelope env ) {
        xmin = env.getMin().getX();
        ymin = env.getMin().getY();
        zmin = env.getMin().getZ();
        xmax = env.getMax().getX();
        ymax = env.getMax().getY();
        zmax = env.getMax().getZ();
        if ( env.getMin().getCoordinateDimension() == 3 ) {
            isZ = true;
        }
    }

    /**
     * Extends this envelope to z and m direction.
     *
     * @param z_min
     * @param z_max
     * @param m_min
     * @param m_max
     */
    public void extend( double z_min, double z_max, double m_min, double m_max ) {
        this.zmin = z_min;
        this.zmax = z_max;
        this.mmin = m_min;
        this.mmax = m_max;

        isZ = true;
    }

    /**
     * Extends this envelope to m direction.
     *
     * @param m_min
     * @param m_max
     */
    public void extend( double m_min, double m_max ) {
        this.mmin = m_min;
        this.mmax = m_max;

        isM = true;
    }

    /**
     * Extends the envelope so the given point fits in.
     *
     * @param x
     * @param y
     */
    public void fit( double x, double y ) {
        xmin = Math.min( x, xmin );
        xmax = Math.max( x, xmax );
        ymin = Math.min( y, ymin );
        ymax = Math.max( y, ymax );
    }

    /**
     * Extends the envelope so the given point fits in.
     *
     * @param x
     * @param y
     * @param z
     */
    public void fit( double x, double y, double z ) {
        fit( x, y );
        zmin = Math.min( z, zmin );
        zmax = Math.max( z, zmax );
    }

    /**
     * Extends the envelope so the given envelope fits in.
     *
     * @param s
     */
    public void fit( ShapeEnvelope s ) {
        if ( s.isZ ) {
            fit( s.xmin, s.ymin, s.zmin );
            fit( s.xmax, s.ymax, s.zmax );
        } else {
            fit( s.xmin, s.ymin );
            fit( s.xmax, s.ymax );
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.io.shpapi.Shape#getByteLength()
     */
    public int getByteLength() {
        int len = 32;
        if ( isZ ) {
            len += 32;
        }
        if ( isM ) {
            len += 16;
        }
        return len;
    }

    /**
     * Reads only x and y values.
     *
     * @see org.deegree.io.shpapi.shape_new.Shape#read(byte[], int)
     */
    public int read( byte[] bytes, int offset ) {
        int off = offset;

        xmin = ByteUtils.readLEDouble( bytes, off );
        off += 8;

        ymin = ByteUtils.readLEDouble( bytes, off );
        off += 8;

        xmax = ByteUtils.readLEDouble( bytes, off );
        off += 8;

        ymax = ByteUtils.readLEDouble( bytes, off );
        off += 8;

        return off;
    }

    /**
     * Writes only x and y values.
     *
     * @see org.deegree.io.shpapi.shape_new.Shape#write(byte[], int)
     */
    public int write( byte[] bytes, int offset ) {
        int off = offset;

        ByteUtils.writeLEDouble( bytes, off, xmin );
        off += 8;

        ByteUtils.writeLEDouble( bytes, off, ymin );
        off += 8;

        ByteUtils.writeLEDouble( bytes, off, xmax );
        off += 8;

        ByteUtils.writeLEDouble( bytes, off, ymax );
        off += 8;

        return off;
    }

    /**
     * @return zero, because the envelope does not have a type
     * @see org.deegree.io.shpapi.shape_new.Shape#getType()
     */
    public int getType() {
        return 0;
    }

    /**
     * @return itself, of course
     * @see org.deegree.io.shpapi.shape_new.Shape#getEnvelope()
     */
    public ShapeEnvelope getEnvelope() {
        return this;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer( 200 );
        sb.append( "x: " ).append( xmin ).append( "/" ).append( xmax );
        sb.append( ", y: " ).append( ymin ).append( "/" ).append( ymax );
        if ( isZ ) {
            sb.append( ", z: " ).append( zmin ).append( "/" ).append( zmax );
        }
        if ( isM || isZ ) {
            sb.append( ", m: " ).append( mmin ).append( "/" ).append( mmax );
        }
        return sb.toString();
    }

    /**
     * @return null, because an envelope is not a geometry
     * @see org.deegree.io.shpapi.shape_new.Shape#getGeometry()
     */
    public Geometry getGeometry() {
        return null;
    }

}
