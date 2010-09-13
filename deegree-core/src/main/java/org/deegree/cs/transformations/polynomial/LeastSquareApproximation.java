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

package org.deegree.cs.transformations.polynomial;

import static org.deegree.cs.utilities.ProjectionUtils.EPS11;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.WarpCubic;
import javax.media.jai.WarpGeneralPolynomial;
import javax.media.jai.WarpPolynomial;
import javax.media.jai.WarpQuadratic;
import javax.vecmath.Point3d;

import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.EPSGCode;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>LeastSquareApproximation</code> is a polynomial transformation which uses the least square method to
 * approximate a function given by some measured values.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class LeastSquareApproximation extends PolynomialTransformation {
    private static Logger LOG = LoggerFactory.getLogger( LeastSquareApproximation.class );

    private WarpPolynomial leastSquarePolynomial;

    private final int order;

    private final float scaleX;

    private final float scaleY;

    /**
     * @param firstParameters
     *            of the polynomial
     * @param secondParameters
     *            of the polynomial
     * @param sourceCRS
     *            of this transformation
     * @param targetCRS
     *            of this transformation
     * @param scaleX
     *            to apply to incoming data's x value, if 1 (or 0) no scale will be applied.
     * @param scaleY
     *            to apply to incoming data's y value, if 1 (or 0) no scale will be applied.
     * @param id
     *            an identifiable instance containing information about this transformation
     */
    public LeastSquareApproximation( List<Double> firstParameters, List<Double> secondParameters,
                                     CoordinateSystem sourceCRS, CoordinateSystem targetCRS, float scaleX,
                                     float scaleY, CRSIdentifiable id ) {
        super( firstParameters, secondParameters, sourceCRS, targetCRS, id );
        if ( getSecondParams().size() != getFirstParams().size() ) {
            throw new IllegalArgumentException( "The given parameter lists do not have equal length" );
        }
        // calc from (n+1)*(n+2) = 2*size;
        order = (int) Math.floor( ( -3 + Math.sqrt( 9 + ( 4 * ( getFirstParams().size() * 2 ) - 2 ) ) ) * 0.5 );
        float[] aParams = new float[getFirstParams().size()];
        for ( int i = 0; i < firstParameters.size(); ++i ) {
            aParams[i] = firstParameters.get( i ).floatValue();
        }
        float[] bParams = new float[getSecondParams().size()];
        for ( int i = 0; i < secondParameters.size(); ++i ) {
            bParams[i] = secondParameters.get( i ).floatValue();
        }
        if ( Float.isNaN( scaleX ) || Math.abs( scaleX ) < EPS11 ) {
            scaleX = 1;
        }
        this.scaleX = scaleX;

        if ( Float.isNaN( scaleY ) || Math.abs( scaleY ) < EPS11 ) {
            scaleY = 1;
        }
        this.scaleY = scaleY;
        switch ( order ) {
        case 2:
            leastSquarePolynomial = new WarpQuadratic( aParams, bParams, this.scaleX, this.scaleY, 1f / this.scaleX,
                                                       1f / this.scaleY );
            break;
        case 3:

            leastSquarePolynomial = new WarpCubic( aParams, bParams, this.scaleX, this.scaleY, 1f / this.scaleX,
                                                   1f / this.scaleY );
            break;
        default:
            leastSquarePolynomial = new WarpGeneralPolynomial( aParams, bParams, this.scaleX, this.scaleY,
                                                               1f / this.scaleX, 1f / this.scaleY );
            break;
        }
    }

    /**
     * Sets the id to EPSG::9645 ( General polynomial of degree 2 ).
     * 
     * @param firstParameters
     *            of the polynomial
     * @param secondParameters
     *            of the polynomial
     * @param sourceCRS
     *            of this transformation
     * @param targetCRS
     *            of this transformation
     * @param scaleX
     *            to apply to incoming data's x value, if 1 (or 0) no scale will be applied.
     * @param scaleY
     *            to apply to incoming data's y value, if 1 (or 0) no scale will be applied.
     */
    public LeastSquareApproximation( List<Double> firstParameters, List<Double> secondParameters,
                                     CoordinateSystem sourceCRS, CoordinateSystem targetCRS, float scaleX, float scaleY ) {
        this( firstParameters, secondParameters, sourceCRS, targetCRS, scaleX, scaleY,
              new CRSIdentifiable( new EPSGCode( 9645 ) ) );
    }

    @Override
    public List<Point3d> applyPolynomial( List<Point3d> srcPts )
                            throws TransformationException {
        if ( srcPts == null || srcPts.size() == 0 ) {
            return srcPts;
        }
        List<Point3d> result = new ArrayList<Point3d>( srcPts.size() );
        for ( Point3d p : srcPts ) {
            Point2D r = leastSquarePolynomial.mapDestPoint( new Point2D.Double( p.x, p.y ) );
            if ( r != null ) {
                result.add( new Point3d( r.getX(), r.getY(), p.z ) );
            } else {
                throw new TransformationException( Messages.getMessage( "CRS_POLYNOMIAL_TRANSFORM_ERROR", p.toString() ) );
            }
        }
        return result;
    }

    @Override
    public String getImplementationName() {
        return "leastsquare";
    }

    @Override
    public float[][] createVariables( List<Point3d> originalPoints, List<Point3d> projectedPoints, int polynomalOrder ) {
        float[] sourceCoords = new float[originalPoints.size() * 2];
        int count = 0;
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;

        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for ( Point3d coord : originalPoints ) {
            if ( minX > coord.x ) {
                minX = coord.x;
            }
            if ( maxX < coord.x ) {
                maxX = coord.x;
            }
            if ( minY > coord.y ) {
                minY = coord.y;
            }
            if ( maxY < coord.y ) {
                maxY = coord.y;
            }
            sourceCoords[count++] = (float) coord.x;
            sourceCoords[count++] = (float) coord.y;
        }

        float sX = Math.abs( ( maxX - minX ) ) > EPS11 ? (float) ( 1. / ( maxX - minX ) ) : 1;
        float sY = Math.abs( ( maxY - minY ) ) > EPS11 ? (float) ( 1. / ( maxY - minY ) ) : 1;
        float[] targetCoords = new float[projectedPoints.size() * 2];

        count = 0;
        for ( Point3d coord : projectedPoints ) {
            targetCoords[count++] = (float) coord.x;
            targetCoords[count++] = (float) coord.y;
        }

        StringBuilder sb = new StringBuilder( "\nCalculated scales are:\n" );
        sb.append( "<crs:scaleX>" ).append( sX ).append( "</crs:scaleX>\n" );
        sb.append( "<crs:scaleY>" ).append( sY ).append( "</crs:scaleY>\n" );
        LOG.info( sb.toString() );

        /**
         * create warp object from reference points and desired interpolation, Because jai only implements the
         * mapDestPoint function, we must calculate the polynomial variables by using the target coordinates as the
         * source.
         */
        WarpPolynomial warp = WarpPolynomial.createWarp( targetCoords, 0, sourceCoords, 0, sourceCoords.length, sX, sY,
                                                         1f / sX, 1f / sY, polynomalOrder );
        return warp.getCoeffs();
    }

    @Override
    public int getOrder() {
        return order;
    }

    /**
     * @return the scale which will be applied to the x value of all incoming data
     */
    public final float getScaleX() {
        return scaleX;
    }

    /**
     * @return the scale which will be applied to the y value of all incoming data
     */
    public final float getScaleY() {
        return scaleY;
    }

}
