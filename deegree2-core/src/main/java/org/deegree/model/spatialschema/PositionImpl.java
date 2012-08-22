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
package org.deegree.model.spatialschema;

import java.io.Serializable;
import java.util.Arrays;

import javax.vecmath.Point3d;

/**
 * A sequence of decimals numbers which when written on a width are a sequence of coordinate positions. The width is
 * derived from the CRS or coordinate dimension of the container.
 *
 * <p>
 * -----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * @version $Revision$ $Date$
 */
public class PositionImpl implements Position, Serializable {
    /** Use serialVersionUID for interoperability. */
    private final static long serialVersionUID = -3780255674921824356L;

    private final Point3d point;

    private static float accuracy = 0.000001f;

    private final byte dimension;

    /**
     * constructor. initializes a point to the coordinate 0/0
     */
    protected PositionImpl() {
        point = new Point3d();
        dimension = 3;
    }

    /**
     * constructor
     *
     * @param x
     *            x-value of the point
     * @param y
     *            y-value of the point
     */
    protected PositionImpl( double x, double y ) {
        point = new Point3d( (double)x, (double)y, Double.NaN );
        dimension = 2;
    }

    /**
     * constructor
     *
     * @param x
     *            x-value of the point
     * @param y
     *            y-value of the point
     * @param z
     *            z-value of the point
     */
    protected PositionImpl( double x, double y, double z ) {
        point = new Point3d( (double)x, (double)y, (double)z );
        if ( Double.isNaN( z ) ) {
            dimension = 2;
        } else {
            dimension = 3;
        }
    }

    /**
     * constructor.
     *
     * @param coords
     *            the Coordinates from which the position is build.
     */
    protected PositionImpl( double[] coords ) {
        if ( coords == null || coords.length < 2 || coords.length > 3 ) {
            if ( coords == null ) {
                throw new NullPointerException( "The given coordinate array does not denote a valid Position." );
            }
            throw new IllegalArgumentException( "The given coordinate array does not denote a valid Position: "
                                                + Arrays.toString( coords ) );

        }
        if ( coords.length == 3 && !Double.isNaN( coords[2] ) ) {
            dimension = 3;
        } else {
            if ( coords.length == 2 ) {
                coords = new double[] { coords[0], coords[1], Double.NaN };
            }
            dimension = 2;
        }
        point = new Point3d( new Point3d(coords) );
    }

    /**
     * Constructor from another Point3d
     *
     * @param other
     *            the Coordinates from which the position is build if <code>null</code> the default values 0,0,0 with
     *            a dim of 3 is assumed.
     */
    protected PositionImpl( final Point3d other ) {
        if ( other != null ) {
            dimension = (byte)( Double.isNaN( other.z ) ? 2 : 3);
            point = new Point3d( other );
        } else {
            dimension = 3;
            point = new Point3d();
        }
    }
    
    /**
     * @return the coordinate dimension of the position
     */
    public int getCoordinateDimension() {
        return dimension;
    }

    /**
     * @return a shallow copy of the geometry.
     */
    @Override
    public Object clone() {
        return new PositionImpl( (Point3d) point.clone() );
    }

    /**
     * @return the x-value of this point
     */
    public double getX() {
        return point.x;
    }

    /**
     * @return the y-value of this point
     */
    public double getY() {
        return point.y;
    }

    /**
     * @return the z-value of this point, if dimension is 2, this value will be Double.NaN
     */
    public double getZ() {
        return point.z;
    }

    /**
     * @return the position as a array the first field contains the x- the second field the y-value etc.
     *
     * NOTE: The returned array always has a length of 3, regardless of the dimension. This is due to a limitation in
     * the coordinate transformation package (proj4), which expects coordinates to have 3 dimensions.
     */
    public double[] getAsArray() {
        return new double[] { point.x, point.y, point.z };
    }

    /**
     * translate the point by the submitted values.
     *
     * @param d
     */
    public void translate( double[] d ) {
        if ( d != null && d.length >= 2 ) {
            point.x += d[0];
            point.y += d[1];
            if ( dimension == 3 ) {
                if ( d.length == 3 ) {
                    point.z += d[2];
                }
            }
        }
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof Position ) {
            final Position that = (Position) other;
            return dimension == that.getCoordinateDimension() && Math.abs( point.x - that.getX() ) <= accuracy
                   && Math.abs( point.y - that.getY() ) <= accuracy
                   && ( ( dimension == 3 ) ? Math.abs( point.z - that.getZ() ) <= accuracy : true );
        }
        return false;
    }

    /**
     * @return the accuracy the position is defined. The accuracy is measured in values of the CRS the positions
     *         coordinates are stored
     */
    public double getAccuracy() {
        return accuracy;
    }

    /**
     * @param accuracy
     */
    public void setAccuracy( double accuracy ) {
        PositionImpl.accuracy = (float)accuracy;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder( "Position: " );
        ret.append( point.x ).append( " " );
        ret.append( point.y );
        if ( dimension == 3 ) {
            ret.append( " " );
            ret.append( point.z );
        }
        return ret.toString();
    }

    public final Point3d getAsPoint3d() {
        return new Point3d( point );
    }
}
