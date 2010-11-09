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
package org.deegree.cs.transformations.coordinate;

import static org.deegree.cs.utilities.ProjectionUtils.EPS11;

import java.util.List;

import javax.media.jai.PerspectiveTransform;
import javax.vecmath.GMatrix;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.utilities.Matrix;

/**
 * 
 * The <code>MatrixTransform</code> class allows transformations using matrices. Although technically n &times; m
 * matrices are possible, at the moment only 2 &times; 2, 3 &times; 3 and 4 &times; 4 matrices are supported.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class MatrixTransform extends Transformation {

    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2104496465933824935L;

    /**
     * the number of rows.
     */
    private final int numRow;

    /**
     * the number of columns.
     */
    private final int numCol;

    private GMatrix matrix = null;

    private GMatrix invertMatrix = null;

    private Matrix3d matrix3D = null;

    private Matrix3d invertMatrix3D = null;

    private Matrix4d matrix4D = null;

    private Matrix4d invertMatrix4D = null;

    private String transformationName = "Matrix-Transform";

    /**
     * Set super values and numRow,numCol.
     * 
     * @param source
     * @param target
     * @param numRow
     * @param numCol
     */
    private MatrixTransform( CoordinateSystem source, CoordinateSystem target, int numRow, int numCol,
                             CRSIdentifiable id ) {
        super( source, target, id );
        this.numCol = numCol;
        this.numRow = numRow;
    }

    /**
     * Construct a transform.
     * 
     * @param source
     *            the source coordinate system
     * @param target
     *            the target coordinate system.
     * 
     * @param matrix
     * @param id
     *            an identifiable instance containing information about this transformation
     */
    public MatrixTransform( CoordinateSystem source, CoordinateSystem target, final GMatrix matrix, CRSIdentifiable id ) {
        this( source, target, matrix.getNumRow(), matrix.getNumCol(), id );
        if ( numCol == numRow ) {
            if ( numCol == 3 ) {
                this.matrix3D = new Matrix3d();
                matrix.get( this.matrix3D );
                invertMatrix3D = new Matrix3d();
                invertMatrix3D.invert( matrix3D );
            }
            if ( numCol == 4 ) {
                this.matrix4D = new Matrix4d();
                matrix.get( this.matrix4D );
                invertMatrix4D = new Matrix4d();
                invertMatrix4D.invert( matrix4D );
            }
        } else {
            this.matrix = new GMatrix( matrix );
            invertMatrix = new GMatrix( matrix );
            invertMatrix.invert();
        }

    }

    /**
     * Construct a transform.
     * 
     * @param source
     *            the source coordinate system
     * @param target
     *            the target coordinate system.
     * 
     * @param matrix
     */
    public MatrixTransform( CoordinateSystem source, CoordinateSystem target, final GMatrix matrix ) {
        this( source, target, matrix,
              new CRSIdentifiable( CRSCodeType.valueOf( createFromTo( source.getCode().toString(),
                                                                      target.getCode().toString() ) ) ) );
    }

    /**
     * Construct a 3d transform.
     * 
     * @param source
     *            the source coordinate system
     * @param target
     *            the target coordinate system.
     * 
     * @param matrix
     * @param id
     *            an identifiable instance containing information about this transformation
     */
    public MatrixTransform( CoordinateSystem source, CoordinateSystem target, final Matrix3d matrix, CRSIdentifiable id ) {
        this( source, target, 3, 3, id );
        this.matrix3D = new Matrix3d( matrix );
        invertMatrix3D = new Matrix3d();
        invertMatrix3D.invert( matrix3D );
    }

    /**
     * Construct a 3d transform.
     * 
     * @param source
     *            the source coordinate system
     * @param target
     *            the target coordinate system.
     * 
     * @param matrix
     */
    public MatrixTransform( CoordinateSystem source, CoordinateSystem target, final Matrix3d matrix ) {
        this( source, target, matrix,
              new CRSIdentifiable( CRSCodeType.valueOf( createFromTo( source.getCode().toString(),
                                                                      target.getCode().toString() ) ) ) );
    }

    /**
     * Construct a 4d transform.
     * 
     * @param source
     *            the source coordinate system
     * @param target
     *            the target coordinate system.
     * 
     * @param matrix
     * @param id
     *            an identifiable instance containing information about this transformation
     */
    public MatrixTransform( CoordinateSystem source, CoordinateSystem target, Matrix4d matrix, CRSIdentifiable id ) {
        this( source, target, 4, 4, id );
        matrix4D = new Matrix4d( matrix );
        invertMatrix4D = new Matrix4d();
        invertMatrix4D.invert( matrix4D );
    }

    /**
     * Construct a 4d transform.
     * 
     * @param source
     *            the source coordinate system
     * @param target
     *            the target coordinate system.
     * 
     * @param matrix
     */
    public MatrixTransform( CoordinateSystem source, CoordinateSystem target, Matrix4d matrix ) {
        this( source, target, matrix,
              new CRSIdentifiable( CRSCodeType.valueOf( createFromTo( source.getCode().toString(),
                                                                      target.getCode().toString() ) ) ) );
    }

    /**
     * Construct a 4d transform.
     * 
     * @param source
     *            the source coordinate system
     * @param target
     *            the target coordinate system.
     * 
     * @param matrix
     * @param transformationName
     *            the 'optional' name of the transformation, which is useful to specify the 'helmert' transformation.
     * @param id
     *            an identifiable instance containing information about this transformation
     */
    public MatrixTransform( CoordinateSystem source, CoordinateSystem target, Matrix4d matrix,
                            String transformationName, CRSIdentifiable id ) {
        this( source, target, matrix, id );
        if ( transformationName != null ) {
            this.transformationName = transformationName;
        }
    }

    /**
     * Construct a 4d transform.
     * 
     * @param source
     *            the source coordinate system
     * @param target
     *            the target coordinate system.
     * 
     * @param matrix
     * @param transformationName
     *            the 'optional' name of the transformation, which is useful to specify the 'helmert' transformation.
     */
    public MatrixTransform( CoordinateSystem source, CoordinateSystem target, Matrix4d matrix, String transformationName ) {
        this( source, target, matrix,
              new CRSIdentifiable( CRSCodeType.valueOf( createFromTo( source.getCode().toString(),
                                                                      target.getCode().toString() ) ) ) );
    }

    @Override
    public List<Point3d> doTransform( List<Point3d> srcPts ) {
        if ( isIdentity() ) {
            return srcPts;
        }
        // List<Point3d> results = new ArrayList<Point3d>( srcPts );
        if ( isInverseTransform() ) {
            if ( matrix3D != null ) {
                transform( invertMatrix3D, srcPts );
            } else if ( matrix4D != null ) {
                transform( invertMatrix4D, srcPts );
            } else {
                transform( invertMatrix, srcPts );
            }
        } else {
            if ( matrix3D != null ) {
                transform( matrix3D, srcPts );
            } else if ( matrix4D != null ) {
                transform( matrix4D, srcPts );
            } else {
                transform( matrix, srcPts );
            }
        }
        return srcPts;
    }

    /**
     * @return the dimension of input points.
     */
    public int getDimSource() {
        return numCol - 1;
    }

    /**
     * @return the dimension of output points.
     */
    public int getDimTarget() {
        return numRow - 1;
    }

    /**
     * @return true if this transformation holds an identity matrix (e.g. doesn't transform at all).
     */
    @Override
    public boolean isIdentity() {
        if ( numRow != numCol ) {
            return false;
        }
        for ( int row = 0; row < numRow; row++ ) {
            for ( int col = 0; col < numCol; col++ ) {
                double value = ( matrix3D != null ) ? matrix3D.getElement( row, col )
                                                   : ( ( matrix4D != null ) ? matrix4D.getElement( row, col )
                                                                           : matrix.getElement( row, col ) );
                if ( Math.abs( value - ( col == row ? 1 : 0 ) ) > EPS11 ) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean equals( final Object object ) {
        if ( object == this ) {
            return true; // Slight optimization
        }
        if ( object != null && super.equals( object ) ) {
            if ( matrix3D != null ) {
                return matrix3D.equals( ( (MatrixTransform) object ).matrix3D );
            } else if ( matrix4D != null ) {
                return matrix4D.equals( ( (MatrixTransform) object ).matrix4D );
            } else {
                return matrix.equals( ( (MatrixTransform) object ).matrix );
            }
        }
        return false;
    }

    /**
     * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
     * distribution and is relatively fast. It is created from field <b>f</b> as follows:
     * <ul>
     * <li>boolean -- code = (f ? 0 : 1)</li>
     * <li>byte, char, short, int -- code = (int)f</li>
     * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
     * <li>float -- code = Float.floatToIntBits(f);</li>
     * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt; 32))</li>
     * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code = f.hashCode(&nbsp;)</li>
     * <li>Array -- Apply above rules to each element</li>
     * </ul>
     * <p>
     * Combining the hash code(s) computed above: result = 37 * result + code;
     * </p>
     * 
     * @return (int) ( result >>> 32 ) ^ (int) result;
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // the 2nd millionth prime, :-)
        long result = 32452843;
        if ( matrix3D != null ) {
            result = result * 37 + matrix3D.hashCode();
        } else if ( matrix4D != null ) {
            result = result * 37 + matrix4D.hashCode();
        } else {
            result = result * 37 + matrix.hashCode();
        }
        return (int) ( result >>> 32 ) ^ (int) result;
    }

    /**
     * Transforms an array of floating point coordinates by this matrix. Because of the usage of a point3d some
     * assumptions are made,
     * <ol>
     * <li>The result point will have the dimension of the number of rows-1.</li>
     * <li>Only the first number of Columns -1 (=input dimension) are used from the incoming point to calculate the
     * result.</li>
     * <li>An output dimension > 3 (e.g. the number of rows > 4 ) results in an IllegalArgumentException.</li>
     * <li>An input dimension > 3 (e.g. the number of columns > 4 ) results in an IllegalArgumentException.</li>
     * <li>Only inputDimension values of the incoming point will be taken into account.</li>
     * <li>If the input dimension == 4 the fourth value is assumed to be 1.</li>
     * </ol>
     * 
     * <p>
     * For example, a square matrix of size 4&times;4. Is a three-dimensional transformation for incoming and outgoing
     * coordinates. The transformed points <code>(x',y',z')</code> are computed as below (note that this computation is similar to
     * {@link PerspectiveTransform}):
     * 
     * <blockquote> <code>
     * <pre>
     *    [ a ]     [ m&lt;sub&gt;00&lt;/sub&gt;  m&lt;sub&gt;01&lt;/sub&gt;  m&lt;sub&gt;02&lt;/sub&gt;  m&lt;sub&gt;03&lt;/sub&gt; ] [ x ]
     *    [ b ]  =  [ m&lt;sub&gt;10&lt;/sub&gt;  m&lt;sub&gt;11&lt;/sub&gt;  m&lt;sub&gt;12&lt;/sub&gt;  m&lt;sub&gt;13&lt;/sub&gt; ] [ y ]
     *    [ c ]     [ m&lt;sub&gt;20&lt;/sub&gt;  m&lt;sub&gt;21&lt;/sub&gt;  m&lt;sub&gt;22&lt;/sub&gt;  m&lt;sub&gt;23&lt;/sub&gt; ] [ z ]
     *    [ w ]     [ m&lt;sub&gt;30&lt;/sub&gt;  m&lt;sub&gt;31&lt;/sub&gt;  m&lt;sub&gt;32&lt;/sub&gt;  m&lt;sub&gt;33&lt;/sub&gt; ] [ 1 ]
     * 
     *     x' = a/w
     *     y' = b/w
     *     z' = c/w
     * </pre>
     * </code> </blockquote>
     * 
     * @param srcPts
     *            list containing the source point coordinates.
     */
    private void transform( GMatrix gm, List<Point3d> srcPts ) {
        final int inputDimension = numCol - 1;
        final int outputDimension = numRow - 1;
        if ( inputDimension > 3 ) {
            throw new IllegalArgumentException(
                                                "Number of collumns: "
                                                                        + numCol
                                                                        + " of the given matrix exceed the maximum dimension (3) supported by this Transformation" );
        }
        if ( outputDimension > 3 ) {
            throw new IllegalArgumentException(
                                                "Number of rows: "
                                                                        + numRow
                                                                        + " of the given matrix exceed the maximum dimension (3) supported by this Transformation" );
        }

        final double[] tmpPoint = new double[numRow];
        for ( Point3d p : srcPts ) {
            for ( int row = 0; row < numRow; ++row ) {
                tmpPoint[row] = gm.getElement( row, 0 ) * p.x;
                if ( numCol >= 2 ) {
                    tmpPoint[row] += gm.getElement( row, 1 ) * p.y;
                    if ( numCol >= 3 ) {
                        tmpPoint[row] += gm.getElement( row, 2 )
                                         * ( ( !Double.isNaN( p.z ) && !Double.isInfinite( p.z ) ) ? p.z : 1 );
                        if ( numCol == 4 ) { // assume 1
                            tmpPoint[row] += gm.getElement( row, 3 );
                        }
                    }

                }
            }
            final double w = tmpPoint[outputDimension];
            if ( outputDimension >= 1 ) {
                p.x = tmpPoint[0] / w;
                if ( outputDimension >= 2 ) {
                    p.y = tmpPoint[1] / w;
                    if ( outputDimension == 3 ) {
                        p.z = tmpPoint[2] / w;
                    }
                }
            }
        }

    }

    /**
     * Use the given GMatrix to transform the given points inplace.
     * 
     * @param m4d
     *            the matrix to use (e.g. the inverse matrix or the forward matrix.
     * @param srcPts
     *            The array containing the source point coordinates.
     */
    private void transform( Matrix4d m4d, List<Point3d> srcPts ) {
        for ( Point3d p : srcPts ) {
            m4d.transform( p );
        }
    }

    /**
     * Use the given GMatrix to transform the given points in-place.
     * 
     * @param m3d
     *            the matrix to use (e.g. the inverse matrix or the forward matrix).
     * @param srcPts
     *            The array containing the source point coordinates.
     */
    private void transform( Matrix3d m3d, List<Point3d> srcPts ) {
        for ( Point3d p : srcPts ) {

            boolean zIsNaN = Double.isNaN( p.z );
            if ( zIsNaN ) {
                p.z = 1;
            }
            m3d.transform( p );
            if ( zIsNaN ) {
                p.z = Double.NaN;
            }
        }
    }

    /**
     * @return the matrix.
     */
    public final GMatrix getMatrix() {
        if ( matrix != null ) {
            return isInverseTransform() ? invertMatrix : matrix;
        }
        GMatrix result = new GMatrix( numRow, numCol );
        if ( matrix3D != null ) {
            if ( isInverseTransform() ) {
                result.set( invertMatrix3D );
            } else {
                result.set( matrix3D );
            }
        }
        if ( matrix4D != null ) {
            if ( isInverseTransform() ) {
                result.set( invertMatrix4D );
            } else {
                result.set( matrix4D );
            }
        }
        return result;
    }

    @Override
    public String getImplementationName() {
        return transformationName;
    }

    /**
     * Creates a Matrix transform from a matrix.
     * 
     * @param sourceCRS
     *            used as the CRS for transforming ordinates on the matrix
     * @param targetCRS
     *            used as the result CRS after transforming ordinates on the matrix
     * @param matrix
     *            The matrix used to define the affine transform.
     * @return A {@link MatrixTransform} mapping incoming ordinates given in the sourceCRS to the targetCRS.
     * @throws TransformationException
     *             if the matrix is not affine.
     * 
     */
    public static MatrixTransform createMatrixTransform( CoordinateSystem sourceCRS, CoordinateSystem targetCRS,
                                                         final Matrix matrix )
                            throws TransformationException {
        if ( matrix == null ) {
            return null;
        }
        if ( matrix.isAffine() ) {// Affine transform are square.
            if ( matrix.getNumRow() == 3 && matrix.getNumCol() == 3 && !matrix.isIdentity() ) {
                Matrix3d tmp = matrix.toAffineTransform();
                return new MatrixTransform( sourceCRS, targetCRS, tmp );
            }
            return new MatrixTransform( sourceCRS, targetCRS, matrix );
        }
        throw new TransformationException( "Given matrix is not affine, cannot continue" );
    }

    /**
     * Create a swap matrix to align the axis of the given coordinate systems and create a tranformation for the result.
     * 
     * @param sourceCRS
     *            used as the CRS for transforming ordinates on the matrix
     * @param targetCRS
     *            used as the result CRS after transforming ordinates on the matrix
     * @return an axis aligned matrix transform, or <code>null</code> if the source and/or target are <code>null</code>
     *         or no transformation is needed.
     * @throws TransformationException
     *             if the matrix could not be created or the result is not affine.
     */
    public static Transformation createAllignMatrixTransform( CoordinateSystem sourceCRS, CoordinateSystem targetCRS )
                            throws TransformationException {
        if ( sourceCRS == null || targetCRS == null ) {
            return null;
        }
        Matrix allign = Matrix.swapAxis( sourceCRS, targetCRS );
        if ( allign == null ) {
            return null;
        }
        return createMatrixTransform( sourceCRS, targetCRS, allign );
    }
}
