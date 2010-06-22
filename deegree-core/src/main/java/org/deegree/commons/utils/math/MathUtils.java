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

package org.deegree.commons.utils.math;

import static java.lang.Math.abs;

/**
 * <code>MathUtils</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MathUtils {

    /**
     *
     */
    public static final double EPSILON = 1E-10;

    /**
     * The value of sqrt(2)
     */
    public static final double SQRT2 = Math.sqrt( 2 );

    /**
     * Helper to round a double value to an int without the need to cast.
     *
     * @param v
     * @return a rounded int
     */
    public static int round( double v ) {
        return (int) Math.round( v );
    }

    /**
     * @param v
     * @return true, if abs(v) &lt; EPSILON.
     */
    public static boolean isZero( double v ) {
        return abs( v ) < EPSILON;
    }

    /**
     * This method will return the next power, two must be raised too to give the next power of two, e.g. if value is
     * 511 it will return 9, if value is 513 it will return 10.
     *
     * @param value
     *            to use
     * @return the next power to the base of two the given value has
     */
    public static int previousPowerOfTwo( double value ) {
        int result = 0;
        int power = 1;
        while ( power <= value && Math.abs( value - power ) > EPSILON ) {
            power = power << 1;
            result++;
        }
        return result;
    }

    /**
     * This method will return the next power of two for the given value, e.g. if value is 511 it will return 512, if
     * value is 513 it will return 1024.
     *
     * @param value
     *            to use
     * @return the next power of two of the given value
     */
    public static int nextPowerOfTwoValue( double value ) {
        int power = previousPowerOfTwo( value );
        return 1 << power;
    }

}
