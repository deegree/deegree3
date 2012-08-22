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
package org.deegree.processing.raster.interpolation;

/**
 *
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 */
public class DataTuple implements Comparable<DataTuple> {

    /**
     * X-Coordinate of the data tuple
     */
    public double x = 0;

    /**
     * Y-Coordinate of the data tuple
     */
    public double y = 0;

    /**
     * Value at location x,y
     */
    public double value = 0;

    /**
     * This may not be the best choice for epsilon.
     */
    // public static final double EPSILON = 0.00000000001;
    /**
     * Convenience constructor.
     *
     * @param x
     * @param y
     * @param value
     */
    public DataTuple( double x, double y, double value ) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    /**
     * Empty constructor. Data is pre-set to zero.
     */
    public DataTuple() {
        //
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(T)
     */
    public int compareTo( DataTuple o ) {

        // boolean xequal = ( ( ( x - EPSILON ) < o.x ) && ( ( x + EPSILON ) > o.x ) );
        // boolean yequal = ( ( ( y - EPSILON ) < o.y ) && ( ( y + EPSILON ) > o.y ) );

        boolean xequal = ( x == o.x );
        boolean yequal = ( y == o.y );

        if ( xequal && yequal ) {
            return 0;
        }

        if ( x < o.x ) {
            return -1;
        }

        if ( xequal && ( y < o.y ) ) {
            return -1;
        }

        return 1;
    }

}
