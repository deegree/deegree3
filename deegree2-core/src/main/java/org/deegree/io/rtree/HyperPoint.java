//$HeadURL$
//----------------------------------------
//RTree implementation.
//Copyright (C) 2002-2004 Wolfgang Baer - WBaer@gmx.de
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//----------------------------------------

package org.deegree.io.rtree;

import java.io.Serializable;

/**
 * <p>
 * Point in multidimensional space. Based on double coordinates.
 * </p>
 *
 * @author Wolfgang Baer - WBaer@gmx.de
 */
public class HyperPoint implements Serializable {

    private double[] coords;

    /**
     * Constructor.<br>
     * Length of given array sets the dimension.
     *
     * @param coords -
     *            array with double values of the coordinates.
     */
    public HyperPoint( double[] coords ) {
        // this.coords = coords;
        this.coords = new double[] { coords[0], coords[1] };
    }

    /**
     * Creates a null HyperPoint with coordinates Double.NaN. Mostly used internal.
     *
     * @param dimension -
     *            int for dimension of point
     * @return HyperPoint
     */
    protected static HyperPoint getNullHyperPoint( int dimension ) {
        double[] point = new double[dimension];
        for ( int i = 0; i < dimension; i++ )
            point[i] = Double.NaN;
        return new HyperPoint( point );
    }

    /**
     * Returns the coordinates as double array.
     *
     * @return double[]
     *
     */
    public double[] getCoords() {
        return coords;
    }

    /**
     * Returns the coordinate for given index.
     *
     * @param index -
     *            int
     * @return double - coordinate
     */
    public double getCoord( int index ) {
        return coords[index];
    }

    /**
     * Returns the dimension of the HyperPoint.
     *
     * @return int
     */
    public int getDimension() {
        return coords.length;
    }

    /**
     * Builds a String representation of the HyperPoint.
     *
     * @return String
     */
    public String toString() {
        String ret = "";

        for ( int i = 0; i < coords.length; i++ ) {
            ret += ( coords[i] + ", " );
        }

        return ret;
    }

    /**
     * Implements equals
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals( Object obj ) {
        HyperPoint point = (HyperPoint) obj;
        for ( int i = 0; i < coords.length; i++ ) {
            if ( this.coords[i] != point.coords[i] )
                return false;
        }
        return true;
    }

    /**
     * Implements clone.
     *
     * @see java.lang.Object#clone()
     */
    protected Object clone() {
        double[] point = new double[coords.length];

        for ( int i = 0; i < coords.length; i++ )
            point[i] = coords[i];

        return new HyperPoint( point );
    }
}