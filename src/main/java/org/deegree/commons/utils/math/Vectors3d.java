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

/**
 * The <code>Vectors3f</code> class supplies convenience methods for 3 dimensional vector (represented as a double
 * array with length 3 ) calculations. No checking what so ever is done, so the callee has to make sure the arrays are
 * initialized and have a length of 3.
 * <p>
 * Two different methods of almost all functions exist, one which creates a new allocated array and one which puts the
 * result in a supplied array. Again, the callee has to make sure the supplied array is initialized and has a length of
 * at least 3.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class Vectors3d {

    /**
     * Subtract b from a and store the result in result.
     *
     * @param a
     *            array with length 3
     * @param b
     *            array with length 3
     * @param result
     *            array with length 3
     */
    public static void sub( double[] a, double[] b, double[] result ) {
        result[0] = a[0] - b[0];
        result[1] = a[1] - b[1];
        result[2] = a[2] - b[2];
    }

    /**
     * Subtract b from a and store the result in a new allocated double[3] array.
     *
     * @param a
     *            array with length 3
     * @param b
     *            array with length 3
     * @return an array with length 3 containing a-b.
     */
    public static double[] sub( double[] a, double[] b ) {
        double[] result = new double[3];
        sub( a, b, result );
        return result;
    }

    /**
     * @param a
     *            an array containing the ordinates of the vectors
     * @param ia
     *            index of the first vector
     * @param ib
     *            index of the second vector
     * @param result
     *            an array with length 3
     */
    public static void sub( double[] a, int ia, int ib, double[] result ) {
        result[0] = a[ia] - a[ib];
        result[1] = a[ia + 1] - a[ib + 1];
        result[2] = a[ia + 2] - a[ib + 2];
    }

    /**
     * @param a
     *            an array containing the ordinates of the vectors
     * @param ia
     *            index of the first vector
     * @param ib
     *            index of the second vector
     * @return a new allocated array with length 3 containing (a[ia])-(a[ib]).
     */
    public static double[] sub( double[] a, int ia, int ib ) {
        double[] result = new double[3];
        sub( a, ia, ib, result );
        return result;
    }

    /**
     * @param a
     *            array with length 3
     * @return the String representation of the scalars of given array separated by a comma (',')
     */
    public final static String asString( double[] a ) {
        return a[0] + "," + a[1] + "," + a[2];
    }

    /**
     * Calculate the normal vector for vectors starting at index by using the vectors a=(index, index+1, index+2),
     * b=(index+3, index+4, index+5) and c=(index+6, index+7, index+8) by calculating the cross product from ab x ac and
     * store the result in a new allocated array with length 3.
     *
     * @param a
     *            array with length > index + 9
     * @param index
     *            index of the first vector
     * @return a newly allocated array with length 3 containing the normal vector.
     */
    public final static double[] normal( double[] a, int index ) {
        return normal( a, index, index + 3, index + 6 );
    }

    /**
     * Calculate the normal vector for vectors starting at index by using the vectors a=(ia, ia+1, ia+2), b=(ib, ib+1,
     * ib+3) and c=(ic, ic+1, ic+2) by calculating the cross product from ab x ac and store the result in a new
     * allocated array with length 3.
     *
     * @param a
     *            array containing the ordinates of the vectors
     * @param ia
     *            index of the first vector
     * @param ib
     *            index of the second vector
     * @param ic
     *            index of the third vector
     * @return a newly allocated array with length 3 containing the normal vector.
     */
    public final static double[] normal( double[] a, int ia, int ib, int ic ) {
        double[] result = new double[3];
        normal( a, ia, ib, ic, result );
        return result;
    }

    /**
     * Calculate the normal vector for vectors starting at given indizes a=(ia, ia+1, ia+2), b=(ib, ib+1, ib+2) and
     * c=(ic, ic+1, ic+2) by calculating the cross product from ab x ac and store the result in given array with length
     * 3.
     *
     * @param a
     *            array containing the ordinates of the vectors
     * @param ia
     *            index of the first vector
     * @param ib
     *            index of the second vector
     * @param ic
     *            index of the third vector
     * @param result
     *            array with length 3
     * @throws IndexOutOfBoundsException
     *             if one of the indizes doesn't fit
     */
    public final static void normal( double[] a, int ia, int ib, int ic, double[] result ) {

        // v[0] = a[ia] - a[ib];
        // v[1] = a[ia + 1] - a[ib + 1];
        // v[2] = a[ia + 2] - a[ib + 2];
        //
        // w[0] = a[ia] - a[ic];
        // w[1] = a[ia + 1] - a[ic + 1];
        // w[2] = a[ia + 2] - a[ic + 2];

        // result[0] = v[1] * w[2] - w[1] * v[2];
        // result[1] = v[2] * w[0] - w[2] * v[0];
        // result[2] = v[0] * w[1] - w[0] * v[1];

        result[0] = ( ( a[ia + 1] - a[ib + 1] ) * ( a[ia + 2] - a[ic + 2] ) )
                    - ( ( a[ia + 1] - a[ic + 1] ) * ( a[ia + 2] - a[ib + 2] ) );
        result[1] = ( ( a[ia + 2] - a[ib + 2] ) * ( a[ia] - a[ic] ) )
                    - ( ( a[ia + 2] - a[ic + 2] ) * ( a[ia] - a[ib] ) );
        result[2] = ( ( a[ia] - a[ib] ) * ( a[ia + 1] - a[ic + 1] ) )
                    - ( ( a[ia] - a[ic] ) * ( a[ia + 1] - a[ib + 1] ) );

    }

    /**
     * Calculate the normal vector for vectors starting at index by using the vectors a=(index, index+1, index+2),
     * b=(index+3, index+4, index+5) and c=(index+6, index+7, index+8) by calculating the cross product from ab x ac and
     * store the result in given array with length 3.
     *
     * @param a
     *            an array containing the ordinates of the vectors with length > index + 9
     * @param index
     *            index of the first vector
     * @param result
     *            array with length 3
     * @throws IndexOutOfBoundsException
     *             if a.length < index +9
     */
    public final static void normal( double[] a, int index, double[] result ) {
        normal( a, index, index + 3, index + 6, result );
    }

    /**
     * Calculate the normal vector for vectors starting at index by using the vectors a=(index, index+1, index+2),
     * b=(index+3, index+4, index+5) and c=(index+6, index+7, index+8) by calculating the cross product from ab x ac and
     * store the result in a new allocated array with length 3. If the resulting normal has length 0, the (unnormalized)
     * vector will be returned .
     *
     * @param a
     *            array containing the ordinates of the vectors with length > index + 9
     * @param index
     *            index of the first vector
     * @return a new allocated array with length 3 containing the normalized normal vector.
     */
    public final static double[] normalizedNormal( double[] a, int index ) {
        return normalizedNormal( a, index, index + 3, index + 6 );
    }

    /**
     * Calculate the normal vector for vectors starting at index by using the vectors a=(ia, ia+1, ia+2), b=(ib, ib+1,
     * ib+2) and c=(ic, ic+1, ic+2) by calculating the cross product from ab x ac and store the result in a new
     * allocated array with length 3. If the resulting normal has length 0, the (unnormalized) vector will be returned .
     *
     * @param a
     *            array containing the ordinates of the vectors
     * @param ia
     *            index of the first vector
     * @param ib
     *            index of the second vector
     * @param ic
     *            index of the third vector
     * @return a new allocated array with length 3 containing the normalized normal vector.
     */
    public final static double[] normalizedNormal( double[] a, int ia, int ib, int ic ) {
        double[] result = new double[3];
        normalizedNormal( a, ia, ib, ic, result );
        return result;
    }

    /**
     * Calculate the normalized normal vector for given triangle with vertices a=(index, index+1, index+2), b=(index+3,
     * index+4, index+5) and c=(index+6, index+7, index+8) by calculating the cross product from ab x ac and normalize
     * the result which will be stored in the given result array with length 3. If the resulting normal has length 0,
     * the (unnormalized) vector will be returned .
     *
     * @param a
     *            array containing the ordinates of the vectors with length > index + 9
     * @param index
     *            index of the first vector
     * @param result
     *            array with length 3
     */
    public final static void normalizedNormal( double[] a, int index, double[] result ) {
        normalizedNormal( a, index, index + 3, index + 6, result );
    }

    /**
     * Calculate the normalized normal vector for given triangle with vertices a=(ia, ia+1, ia+2), b=(ib, ib+1, ib+2)
     * and c=(ic, ic+1, ic+2) by calculating the cross product from ab x ac and normalize the result which will be
     * stored in the given result array with length 3. If the resulting normal has length 0, the (unnormalized) vector
     * will be returned .
     *
     * @param a
     *            array containing the ordinates of the vectors
     * @param ia
     *            index of the first vector
     * @param ib
     *            index of the second vector
     * @param ic
     *            index of the third vector
     * @param result
     *            array with length 3
     */
    public final static void normalizedNormal( double[] a, int ia, int ib, int ic, double[] result ) {
        normal( a, ia, ib, ic, result );
        normalizeInPlace( result );
    }

    /**
     * Calculate the normal vector for given vectors a, b, c by calculating the cross product from ab x ac and store the
     * result in a new allocated array with length 3.
     *
     * @param a
     *            array with length 3
     * @param b
     *            array with length 3
     * @param c
     *            array with length 3
     * @return a newly allocated array with length 3 containing the normalized normal vector.
     */
    public final static double[] normal( double[] a, double[] b, double[] c ) {
        double[] result = new double[3];
        normal( a, b, c, result );
        return result;
    }

    /**
     * Calculate the normal vector for given vectors a, b, c by calculating the cross product from ab x ac and store the
     * result in given result vector.
     *
     * @param a
     *            array with length 3
     * @param b
     *            array with length 3
     * @param c
     *            array with length 3
     * @param result
     *            array with length 3
     */
    public final static void normal( double[] a, double[] b, double[] c, double[] result ) {
        double[] v = sub( a, b );
        double[] w = sub( a, c );
        cross( v, w, result );
    }

    /**
     * Calculate the normalized normal vector for given triangle with vertices a, b, c by calculating the cross product
     * from ab x ac and normalize the result which will be stored in a new allocated array of length 3. If the resulting
     * normal has length 0, the (unnormalized) vector will be returned .
     *
     * @param a
     *            array with length 3
     * @param b
     *            array with length 3
     * @param c
     *            array with length 3
     * @return a new allocated array with length 3 containing the normalized normal vector.
     */
    public final static double[] normalizedNormal( double[] a, double[] b, double[] c ) {
        double[] result = new double[3];
        normalizedNormal( a, b, c, result );
        return result;
    }

    /**
     * Calculate the normalized normal vector for given triangle with vertices a, b, c by calculating the cross product
     * from ab x ac and normalize the result which will be stored in the given result array with length 3. If the
     * resulting normal has length 0, the (unnormalized) vector will be returned .
     *
     * @param a
     *            array with length 3
     * @param b
     *            array with length 3
     * @param c
     *            array with length 3
     * @param result
     *            array with length 3
     */
    public final static void normalizedNormal( double[] a, double[] b, double[] c, double[] result ) {
        normal( a, b, c, result );
        normalizeInPlace( result );
    }

    /**
     * Calculate the cross product of given vectors and store the result in a new allocated array of length 3.
     *
     * @param a
     *            array with length 3
     * @param b
     *            array with length 3
     * @return a new allocated array with length 3.
     */
    public final static double[] cross( double[] a, double[] b ) {
        double[] result = new double[3];
        cross( a, b, result );
        return result;
    }

    /**
     * Calculate the cross product of given vectors and store the result in given result vector with length 3
     *
     * @param a
     *            array with length 3
     * @param b
     *            array with length 3
     * @param result
     *            array with length 3
     *
     */
    public final static void cross( double[] a, double[] b, double[] result ) {
        result[0] = a[1] * b[2] - b[1] * a[2];
        result[1] = a[2] * b[0] - b[2] * a[0];
        result[2] = a[0] * b[1] - b[0] * a[1];
    }

    /**
     * Calculate the dot product of given vectors (with length 3).
     *
     * @param a
     *            array with length 3
     * @param b
     *            array with length 3
     * @return the dot product of given vectors.
     *
     */
    public final static double dot( double[] a, double[] b ) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    /**
     * @param a
     *            array with length 3
     * @return the euclidean length of the given vector
     */
    public final static double length( double[] a ) {
        return Math.sqrt( a[0] * a[0] + a[1] * a[1] + a[2] * a[2] );
    }

    /**
     * Normalize the given vector (of length 3) in place. If the length of the vector is 0, the vector will not be
     * modified.
     *
     * @param a
     *            array with length 3
     */
    public final static void normalizeInPlace( double[] a ) {
        double length = length( a );
        if ( length > 1E-11 ) {
            a[0] /= length;
            a[1] /= length;
            a[2] /= length;
        }
    }

    /**
     * Normalize the given vector (of length 3) and store the result in a new allocated array of length 3. If the length
     * of the vector is 0, the result vector will contain the same values as the given vector.
     *
     * @param a
     *            array with length 3
     * @return the normalized vector
     */
    public final static double[] normalize( double[] a ) {
        double[] result = new double[] { a[0], a[1], a[2] };
        normalizeInPlace( result );
        return result;
    }
}
