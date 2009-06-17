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
package org.deegree.crs.utilities;

import static org.deegree.crs.projections.ProjectionUtils.EPS11;

import java.awt.geom.AffineTransform;

import javax.vecmath.GMatrix;
import javax.vecmath.Matrix3d;

import org.deegree.crs.components.Axis;

/**
 * The <code>Matrix</code> class TODO add documentation here
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */

public class Matrix extends GMatrix {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3778102551617232269L;

    /**
     * Construct a square identity matrix of size <code>size</code>&nbsp;&times;&nbsp;<code>size</code>.
     *
     * @param size
     */
    public Matrix( final int size ) {
        super( size, size );
    }

    /**
     * Construct a matrix of size <code>numRow</code>&nbsp;&times;&nbsp;<code>numCol</code>. Elements on the
     * diagonal <var>j==i</var> are set to 1.
     *
     * @param numRow
     * @param numCol
     */
    public Matrix( final int numRow, final int numCol ) {
        super( numRow, numCol );
    }

    /**
     * Constructs a <code>numRow</code>&nbsp;&times;&nbsp;<code>numCol</code> matrix initialized to the values in
     * the <code>matrix</code> array. The array values are copied in one row at a time in row major fashion. The array
     * should be exactly <code>numRow*numCol</code> in length. Note that because row and column numbering begins with
     * zero, <code>row</code> and <code>numCol</code> will be one larger than the maximum possible matrix index
     * values.
     *
     * @param numRow
     * @param numCol
     * @param matrix
     */
    public Matrix( final int numRow, final int numCol, final double[] matrix ) {
        super( numRow, numCol, matrix );
        if ( numRow * numCol != matrix.length ) {
            throw new IllegalArgumentException( String.valueOf( matrix.length ) );
        }
    }

    /**
     * Constructs a new matrix from a two-dimensional array of doubles.
     *
     * @param matrix
     *            Array of rows. Each row must have the same length.
     * @throws IllegalArgumentException
     *             if the specified matrix is not regular (i.e. if all rows doesn't have the same length).
     */
    public Matrix( final double[][] matrix ) throws IllegalArgumentException {
        super( matrix.length, ( matrix.length != 0 ) ? matrix[0].length : 0 );
        final int numRow = getNumRow();
        final int numCol = getNumCol();
        for ( int j = 0; j < numRow; j++ ) {
            if ( matrix[j].length != numCol ) {
                throw new IllegalArgumentException( "Not a regular Matrix (given rows have different lengths)" );
            }
            setRow( j, matrix[j] );
        }
    }

    /**
     * Constructs a new matrix and copies the initial values from the parameter matrix.
     *
     * @param matrix
     */
    public Matrix( final GMatrix matrix ) {
        super( matrix );
    }

    /**
     * Construct a 3&times;3 matrix from the specified affine transform.
     *
     * @param transform
     */
    public Matrix( final AffineTransform transform ) {
        super( 3, 3, new double[] { transform.getScaleX(), transform.getShearX(), transform.getTranslateX(),
                                   transform.getShearY(), transform.getScaleY(), transform.getTranslateY(), 0, 0, 1 } );
    }

    /**
     * Construct an affine transform changing axis order. The resulting affine transform will convert incoming
     * coordinates into the given destination Axis. For example if source axis are given with (NORTH,WEST) and
     * destination axis as (EAST,NORTH) assuming the axis use the same units, the resulted matrix will look like:<br/><code>
     *  &nbsp;0,&nbsp;1,&nbsp;0<br/>
     * -1,&nbsp;0,&nbsp;0<br/>
     *  &nbsp;0,&nbsp;0,&nbsp;1<br/>
     *  </code>
     * Axis orientation can be inverted only. Rotating axis (e.g. from NORTH,WEST, to NORTH,DOWN, ) is not supported.
     *
     * @param srcAxis
     *            The set of axis orientation for source coordinate system.
     * @param dstAxis
     *            The set of axis orientation for destination coordinate system.
     * @throws IllegalArgumentException
     *             if the affine transform can't be created for some other reason.
     */
    public Matrix( final Axis[] srcAxis, final Axis[] dstAxis ) {
        this( srcAxis.length + 1 );
        final int dimension = srcAxis.length;
        if ( dstAxis.length != dimension ) {
            throw new IllegalArgumentException( "Given dimensions are of differnt length." );
        }
        /*
         * Map source axis to destination axis. If no axis is moved (for example if the user want to transform
         * (NORTH,EAST) to (SOUTH,EAST)), then source and destination index will be equal. If some axis are moved (for
         * example if the user want to transform (NORTH,EAST) to (EAST,NORTH)), then ordinates at index <code>srcIndex</code>
         * will have to be moved at index <code>dstIndex</code>.
         */
        setZero();
        for ( int srcIndex = 0; srcIndex < dimension; srcIndex++ ) {
            boolean hasFound = false;
            final int srcAxe = srcAxis[srcIndex].getOrientation();
            final int sourceAxisDirection = Math.abs( srcAxe );
            for ( int dstIndex = 0; dstIndex < dimension; dstIndex++ ) {
                final int dstAxeDirection = dstAxis[dstIndex].getOrientation();
                if ( sourceAxisDirection == Math.abs( dstAxeDirection ) ) {
                    if ( hasFound ) {
                        throw new IllegalArgumentException( "Following axis are colinear: "
                                                            + srcAxis[srcIndex].getName() + " dstAxe: "
                                                            + dstAxis[dstIndex].getName() );
                    }
                    hasFound = true;
                    // row, column, value
                    setElement( dstIndex, srcIndex, ( srcAxe == dstAxeDirection ) ? 1 : -1 );
                }
            }
            if ( !hasFound ) {
                throw new IllegalArgumentException( "No appropriate transformation axis found for srcAxis: "
                                                    + srcAxis[srcIndex].getName() );
            }
        }
        setElement( dimension, dimension, 1 );

    }

    /**
     * Returns <code>true</code> if this matrix is an affine transform. A transform is affine if the matrix is square
     * and last row contains only zeros, except in the last column which contains 1.
     *
     * @return <code>true</code> if this matrix is an affine transform.
     */
    public final boolean isAffine() {
        int dimension = getNumRow();
        if ( dimension != getNumCol() ) {
            return false;
        }

        dimension--;
        for ( int i = 0; i <= dimension; i++ ) {
            if ( Math.abs( getElement( dimension, i ) - ( i == dimension ? 1 : 0 ) ) > EPS11 ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Copies the first 2x3 values into an affine transform object. If not enough values are available, an identity
     * transform is returned.
     *
     * @return an affine transform for this matrix. or an identity if this matrix has not sufficient values.
     *
     */
    public final Matrix3d toAffineTransform() {
        if ( getNumCol() < 3 || getNumRow() < 2 ) {
            return new Matrix3d();
        }
        return new Matrix3d( getElement( 0, 0 ), getElement( 0, 1 ), getElement( 0, 2 ), getElement( 1, 0 ),
                             getElement( 1, 1 ), getElement( 1, 2 ), 0, 0, 1 );
    }

    /**
     * Returns <code>true</code> if this matrix is an identity matrix.
     *
     * @return <code>true</code> if this matrix is an identity matrix.
     */
    public final boolean isIdentity() {
        final int numRow = getNumRow();
        final int numCol = getNumCol();
        if ( numRow != numCol ) {
            return false;
        }
        for ( int j = 0; j < numRow; j++ )
            for ( int i = 0; i < numCol; i++ ) {
                if ( Math.abs( getElement( j, i ) - ( i == j ? 1 : 0 ) ) > EPS11 ) {
                    return false;
                }
            }
        return true;
    }
}
