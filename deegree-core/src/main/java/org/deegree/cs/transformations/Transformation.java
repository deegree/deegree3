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

package org.deegree.cs.transformations;

import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.i18n.Messages;
import org.deegree.cs.transformations.coordinate.ConcatenatedTransform;

/**
 * The <code>Transformation</code> class supplies the most basic method interface for any given transformation.
 * 
 * The change of coordinates from one CRS to another CRS based on different datum is 'currently' only possible via a
 * coordinate <code>Transformation</code>.
 * <p>
 * The derivation of transformation parameters can be done empirically or analytically.
 * <p>
 * The quality (accuracy) of an empirical derivation strongly depends on the chosen reference points, there allocation,
 * and their number. Therefore different realizations for transformations from one datum to another exist. *
 * </p>
 * <p>
 * An analytic derivation is precise but mostly too complex to evaluate.
 * </p>
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public abstract class Transformation extends CRSIdentifiable {

    private CoordinateSystem sourceCRS;

    private CoordinateSystem targetCRS;

    /**
     * Signaling this transformation as inverse
     */
    private boolean isInverse;

    /**
     * @param sourceCRS
     * @param targetCRS
     * @param id
     *            an identifiable instance containing information about this transformation
     */
    public Transformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS, CRSIdentifiable id ) {
        super( id );
        checkForNullObject( targetCRS, "Transformation", "targetCRS" );
        // checkForNullObject( sourceCRS, "Transformation", "sourceCRS" );
        this.sourceCRS = sourceCRS;
        this.targetCRS = targetCRS;
        isInverse = false;
    }

    /**
     * @return the name of the transformation.
     */
    public abstract String getImplementationName();

    /**
     * Do a transformation, e.g. the incoming data will be transformed into other coordinates.
     * 
     * @param srcPts
     *            the points which must be transformed, expected are following values either, long_1, lat_1, height_1,
     *            long_2, lat_2, height_2. or long_1, lat_1, long_2, lat_2
     * @return the transformed points
     * @throws TransformationException
     *             if a transform could not be calculated.
     */
    public abstract List<Point3d> doTransform( final List<Point3d> srcPts )
                            throws TransformationException;

    /**
     * @return true if this transformation doesn't transform the incoming points. (e.g. is the id. matrix)
     */
    public abstract boolean isIdentity();

    /**
     * Little helper function to create a temporary id or name.
     * 
     * @param source
     *            containing the value (id or name) of the 'src' coourdinateSystem
     * @param dest
     *            containing the value (id or name) of the 'dest' coourdinateSystem
     * @return a following string "_SRC_fromValue_DEST_toValue".
     */
    public static String createFromTo( String source, String dest ) {
        return new StringBuilder( "_SRC_" ).append( source ).append( "_DEST_" ).append( dest ).toString();
    }

    /**
     * Wraps the incoming coordinates into a List<Point3d> and calls the {@link #doTransform(List)}. The source array
     * will be read according to the dimension of the source CRS {@link #getSourceDimension()} and the target
     * coordinates will be put according to the dimension of the targetCRS {@link #getTargetDimension()}. If the
     * sourceDim &lt; 2 or &gt; 3 a transformation exception will be thrown.
     * 
     * @param srcOrdinates
     *            the array holding the source ('original') coordinates.
     * @param startPositionSrc
     *            the position to start reading the coordinates from the source array (0 is the first).
     * @param destOrdinates
     *            the array which will receive the transformed coordinates.
     * @param startPositionDest
     *            the index of the destCoords array to put the results, if the result will exceed the array.length, the
     *            array will be enlarged to hold the transformed coordinates.
     * @param length
     *            the number of source ordinates to transform
     * @throws TransformationException
     *             If the sourceDim &lt; 2 or soureDim &gt 3;
     * @throws IllegalArgumentException
     *             if
     *             <ul>
     *             <li>the srcCoords is null</li>
     *             <li>the startPositionSrc &gt; srcCoords.length</li>
     *             <li>the lastCoord &gt; startPositionSrc</li>
     *             <li>the number of source coordinates are not congruent with the source dimension</li>
     *             <li>the lastCoord &lt; startCoordSrc</li>
     *             <li>the source or target dimension &lt; 2 or &gt; 3</li>
     *             </ul>
     */
    public void doTransform( double[] srcOrdinates, int startPositionSrc, double[] destOrdinates,
                             int startPositionDest, int length )
                            throws TransformationException {
        if ( startPositionSrc < 0 ) {
            startPositionSrc = 0;
        }
        if ( srcOrdinates == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_PARAMETER_NOT_NULL",
                                                                     "doTransform(double[],int,double[],int,int)",
                                                                     "srcCoords" ) );
        }
        if ( startPositionSrc > srcOrdinates.length ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_TRANSFORM_START_GT_LENGTH" ) );
        }
        if ( length > srcOrdinates.length ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_TRANSFORM_END_GT_LENGTH" ) );
        }
        // if ( ( length - startPositionSrc ) % getSourceDimension() != 0 ) {
        // throw new IllegalArgumentException( Messages.getMessage( "CRS_TRANSFORM_SRC_WRONG_DIM" ) );
        // }

        int listSize = ( length - startPositionSrc ) / getSourceDimension();
        if ( listSize < 0 ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_TRANSFORM_LAST_LT_START" ) );
        }

        List<Point3d> sourceCoords = new LinkedList<Point3d>();
        final int dim = getSourceDimension();
        if ( dim > 3 || dim < 2 ) {
            throw new TransformationException( Messages.getMessage( "CRS_TRANSFORM_WRONG_CRS_DIM", "source" ) );
        }
        for ( int i = startPositionSrc; ( ( i + ( dim - 1 ) ) < ( startPositionSrc + length ) ); i += dim ) {
            sourceCoords.add( new Point3d( srcOrdinates[i], srcOrdinates[i + 1], ( dim == 3 ) ? srcOrdinates[i + 2] : 0 ) );
        }
        List<Point3d> result = doTransform( sourceCoords );
        if ( startPositionDest < 0 ) {
            startPositionDest = 0;
        }
        final int requiredSpace = result.size() * getTargetDimension();
        if ( destOrdinates == null ) {
            startPositionDest = 0;
            destOrdinates = new double[requiredSpace];
        }
        final int requiredSize = startPositionDest + requiredSpace;
        if ( requiredSize > destOrdinates.length ) {
            double[] tmp = new double[requiredSize];
            System.arraycopy( destOrdinates, 0, tmp, 0, startPositionDest );
            destOrdinates = tmp;
        }
        final int dimDest = getTargetDimension();
        if ( dimDest > 3 || dimDest < 2 ) {
            throw new TransformationException( Messages.getMessage( "CRS_TRANSFORM_WRONG_CRS_DIM", "target" ) );
        }
        int arrayPos = startPositionDest;
        for ( Point3d coord : result ) {
            destOrdinates[arrayPos++] = coord.x;
            destOrdinates[arrayPos++] = coord.y;
            if ( dimDest == 3 ) {
                destOrdinates[arrayPos++] = coord.z;
            }
        }
    }

    /**
     * Transforms a single point3d (by calling the doTransform( List<Point3d>).
     * 
     * @param coordinate
     *            to transform, if <code>null</code> null will be returned.
     * @return the transformed coordinate.
     * @throws TransformationException
     *             if the coordinate could not be transformed from the sourceCRS to the targetCRS.
     */
    public Point3d doTransform( Point3d coordinate )
                            throws TransformationException {
        if ( coordinate == null ) {
            return null;
        }
        List<Point3d> coord = new LinkedList<Point3d>();
        coord.add( coordinate );
        return doTransform( coord ).get( 0 );
    }

    /**
     * @return true if the doInverseTransform method should be called, false otherwise.
     */
    public boolean isInverseTransform() {
        return isInverse;
    }

    /**
     * This method flags the transformation about it's state. If this transformation was inverse calling this method
     * will result in a forward transformation and vice versa.
     */
    public void inverse() {
        isInverse = !isInverse;
    }

    /**
     * @return a representation of this transformations name, including the 'Forward' or 'Inverse' modifier.
     */
    public String getTransformationName() {
        StringBuilder result = new StringBuilder( isInverse ? "Inverse " : "Forward " );
        result.append( getImplementationName() );
        return result.toString();
    }

    /**
     * @return the sourceCRS.
     */
    public final CoordinateSystem getSourceCRS() {
        return isInverse ? targetCRS : sourceCRS;
    }

    /**
     * @return the targetCRS.
     */
    public final CoordinateSystem getTargetCRS() {
        return isInverse ? sourceCRS : targetCRS;
    }

    /**
     * @return the dimension of the source coordinateSystem.
     */
    public int getSourceDimension() {
        return getSourceCRS().getDimension();
    }

    /**
     * @return the dimension of the target coordinateSystem.
     */
    public int getTargetDimension() {
        return getTargetCRS().getDimension();
    }

    /**
     * Checks if this transformation is the inverse of the other transformation, which means, this.sourceCRS equals
     * other.targetCRS && this.targetCRS == other.sourceCRS. If Both transformations are identity this method also
     * returns true.
     * 
     * @param other
     *            the transformation to check
     * @return true if this and the other transformation are each others inverse.
     */
    public boolean areInverse( Transformation other ) {
        boolean result = ( other == null ) ? false : ( this.isIdentity() && other.isIdentity() );
        if ( result && other != null ) {
            result = getSourceCRS() == null ? other.getSourceCRS() == null
                                           : getSourceCRS().equals( other.getSourceCRS() );
            if ( result ) {
                result = getTargetCRS() == null ? other.getTargetCRS() == null
                                               : getTargetCRS().equals( other.getTargetCRS() );
            }
        }
        return result;

    }

    /**
     * @param sb
     *            to add the transformation chain to, if <code>null</code> a new StringBuilder will be created.
     * @return the given StringBuilder (or a new instance) with the appended transformation steps.
     */
    public final StringBuilder getTransformationPath( StringBuilder sb ) {
        if ( sb == null ) {
            sb = new StringBuilder();
        }
        outputTransform( 0, sb, this );
        return sb;
    }

    private int outputTransform( int level, StringBuilder sb, Transformation t ) {
        if ( t instanceof ConcatenatedTransform ) {
            level = outputTransform( level, sb, ( (ConcatenatedTransform) t ).getFirstTransform() );
            level = outputTransform( level, sb, ( (ConcatenatedTransform) t ).getSecondTransform() );
        } else {
            if ( level != 0 ) {
                sb.append( "->" );
            }
            sb.append( "(" ).append( level ).append( ")" ).append( t.getTransformationName() );
            return ++level;
        }
        return level;
    }

    /**
     * Returns true if the source and target of this transformation equal the source and target of the given
     * transformation.
     * 
     * @param other
     *            transformation to check against
     * @return true if the source and target coordinate systems of this transformation equal the source and target
     *         coordinate systems of the given transformation.
     */
    public boolean equalOnCRS( Transformation other ) {
        boolean result = false;
        if ( other != null ) {
            result = getSourceCRS() == null ? other.getSourceCRS() == null
                                           : getSourceCRS().equals( other.getSourceCRS() );
            if ( result ) {
                result = getTargetCRS() == null ? other.getTargetCRS() == null
                                               : getTargetCRS().equals( other.getTargetCRS() );
            }
        }
        return result;
    }

    /**
     * Returns true if this Transformation transforms over the given crs.
     * 
     * @param crs
     *            to check for
     * @return true if the given crs is used in this transformation.
     */
    public boolean contains( CoordinateSystem crs ) {
        boolean result = getSourceCRS() == null ? crs == null : getSourceCRS().equals( crs );
        if ( !result ) {
            result = getTargetCRS() == null ? crs == null : getTargetCRS().equals( crs );
        }

        return result;
    }

    /**
     * @param newSource
     *            to be used as the new source coordinate system.
     */
    public void setSourceCRS( CoordinateSystem newSource ) {
        this.sourceCRS = newSource;
    }

    /**
     * @param sourceCRS
     *            from which ordinates will be transformed
     * @param targetCRS
     *            to which ordinates will be transformed.
     * @return true if this transformation can transform from the given source CRS to the target CRS.
     */
    public boolean canTransform( CoordinateSystem sourceCRS, CoordinateSystem targetCRS ) {
        return getSourceCRS().equals( sourceCRS ) && getTargetCRS().equals( targetCRS );
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof Transformation ) {
            final Transformation that = (Transformation) other;
            return this.getImplementationName().equals( that.getImplementationName() ) && super.equals( that )
                   && isIdentity() == that.isIdentity() && isInverseTransform() == that.isInverseTransform();
        }
        return false;
    }

}
