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

import org.deegree.model.crs.CoordinateSystem;

/**
 * default implementation of the Point interface.
 *
 * <p>
 * ------------------------------------------------------------
 * </p>
 *
 * @version 5.6.2001
 * @author Andreas Poth
 *         <p>
 */

public class PointImpl extends PrimitiveImpl implements Point, Serializable {
    /** Use serialVersionUID for interoperability. */
    private final static long serialVersionUID = 6106017748940535740L;

    private Position position = null;

    /**
     * constructor. initializes a point to the coordinate 0/0
     *
     * @param crs
     *            spatial reference system of the point
     */
    protected PointImpl( CoordinateSystem crs ) {
        super( crs );
        position = new PositionImpl();
        empty = true;
        centroid = position;
    }

    /**
     * constructor for initializing a point within a two-dimensional coordinate system
     *
     * @param x
     *            x-value of the point
     * @param y
     *            y-value of the point
     * @param crs
     *            spatial reference system of the point
     */
    protected PointImpl( double x, double y, CoordinateSystem crs ) {
        super( crs );
        position = new PositionImpl( x, y );
        empty = false;
        centroid = position;
    }

    /**
     * constructor for initializing a point within a three-dimensional coordinate system
     *
     * @param x
     *            x-value of the point
     * @param y
     *            y-value of the point
     * @param z
     *            z-value of the point
     * @param crs
     *            spatial reference system of the point
     */
    protected PointImpl( double x, double y, double z, CoordinateSystem crs ) {
        super( crs );
        position = new PositionImpl( x, y, z );
        empty = false;
        centroid = position;
    }

    /**
     * constructor
     *
     * @param otherPoint
     *            existing GM_Point
     */
    protected PointImpl( Point otherPoint ) {
        super( otherPoint.getCoordinateSystem() );
        position = new PositionImpl( otherPoint.getAsArray() );
        empty = false;
        centroid = position;
    }

    /**
     * constructor
     *
     * @param position
     *            existing position
     * @param crs
     *            spatial reference system of the point
     */
    protected PointImpl( Position position, CoordinateSystem crs ) {
        super( crs );
        this.position = position;
        empty = false;
        centroid = position;
    }

    /**
     * checks if this point is completely equal to the submitted geometry
     */
    @Override
    public boolean equals( Object other ) {
        if ( super.equals( other ) && ( other instanceof Point ) ) {
            Point p = (Point) other;
            boolean flagEq = Math.abs( getX() - p.getX() ) < mute && Math.abs( getY() - p.getY() ) < mute;
            if ( getCoordinateDimension() == 3 ) {
                flagEq = flagEq && Math.abs( getZ() - p.getZ() ) < mute;
            }
            return flagEq;
        }

        return false;
    }

    /**
     * @return 0
     */
    public int getDimension() {
        return 0;
    }

    /**
     * @return the number of ordinates contained in this point.
     */
    public int getCoordinateDimension() {
        return getPosition().getCoordinateDimension();
    }

    @Override
    public Object clone() {
        return new PointImpl( position.getX(), position.getY(), position.getZ(), getCoordinateSystem() );
    }

    public double getX() {
        return position.getX();
    }

    public double getY() {
        return position.getY();
    }

    public double getZ() {
        return position.getZ();
    }

    /**
     * @return the x- and y-value of the point as a two dimensional array the first field contains the x- the second
     *         field the y-value.
     */
    public double[] getAsArray() {
        return position.getAsArray();
    }

    /**
     * translate the point by the submitted values. the <code>dz</code>- value will be ignored.
     */
    @Override
    public void translate( double[] d ) {
        setValid( false );
        position.translate( d );
    }

    /**
     * @return position
     *
     */
    public Position getPosition() {
        return position;
    }

    /**
     * The Boolean valued operation "intersects" shall return TRUE if this Geometry intersects another Geometry. Within
     * a Complex, the Primitives do not intersect one another. In general, topologically structured data uses shared
     * geometric objects to capture intersection information.
     * <p>
     * </p>
     * dummy implementation
     */
    @Override
    public boolean intersects( Geometry gmo ) {
        boolean inter = false;

        try {
            if ( gmo instanceof Point ) {
                inter = LinearIntersects.intersects( (Point) gmo, this );
            } else if ( gmo instanceof Curve ) {
                inter = LinearIntersects.intersects( this, (Curve) gmo );
            } else if ( gmo instanceof Surface ) {
                inter = LinearIntersects.intersects( this, (Surface) gmo );
            } else if ( gmo instanceof Aggregate ) {
                inter = intersectsAggregate( (Aggregate) gmo );
            }
        } catch ( Exception e ) {
            // ignore
        }

        return inter;
    }

    /**
     * the operations returns true if the submitted multi primitive intersects with the curve segment
     */
    private boolean intersectsAggregate( Aggregate mprim )
                            throws Exception {
        boolean inter = false;

        int cnt = mprim.getSize();

        for ( int i = 0; i < cnt; i++ ) {
            if ( intersects( mprim.getObjectAt( i ) ) ) {
                inter = true;
                break;
            }
        }

        return inter;
    }

    /**
     * The Boolean valued operation "contains" shall return TRUE if this Geometry contains another Geometry.
     * <p>
     * </p>
     */
    @Override
    public boolean contains( Geometry gmo ) {
        throw new UnsupportedOperationException( "the contains operation for points "
                                                 + "isn't supported at the moment." );
    }

    /**
     * recalculates internal parameters
     */
    @Override
    protected void calculateParam() {
        setValid( true );
        envelope = GeometryFactory.createEnvelope( position, position, crs );
    }

    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer( 30 );
        ret.append( this.getClass().getName() ).append( ": " );

        for ( int i = 0; i < getAsArray().length; i++ ) {
            ret.append( getAsArray()[i] ).append( ' ' );
        }

        return ret.toString();
    }

}
