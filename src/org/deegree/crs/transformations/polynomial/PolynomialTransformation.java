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

package org.deegree.crs.transformations.polynomial;

import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.transformations.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>PolynomialTransformation</code> is the base class for all polynomial transformations.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public abstract class PolynomialTransformation extends Transformation {

    private static Logger LOG = LoggerFactory.getLogger( PolynomialTransformation.class );

    private List<Double> firstParams;

    private List<Double> secondParams;

    /**
     * @param firstParameters
     *            the parameters for the
     * @param secondParameters
     * @param sourceCRS
     * @param targetCRS
     * @param id
     *            an identifiable instance containing information about this transformation
     */
    public PolynomialTransformation( List<Double> firstParameters, List<Double> secondParameters,
                                     CoordinateSystem sourceCRS, CoordinateSystem targetCRS, CRSIdentifiable id ) {
        super( sourceCRS, targetCRS, id );
        if ( firstParameters == null ) {
            throw new IllegalArgumentException( "The first parameter list my not be null" );
        }
        this.firstParams = firstParameters;

        if ( secondParameters == null ) {
            throw new IllegalArgumentException( "The second parameter list my not be null" );
        }
        this.secondParams = secondParameters;

    }

    /**
     * @return the order of the Polynomial used for this transformation.
     */
    public abstract int getOrder();

    /**
     * @param originalPoints
     *            of the projection
     * @param projectedPoints
     *            the 'function' values
     * @param polynomalOrder
     *            the order of the polynomial function to use.
     * @return the variables the polynomial used for this transformation.
     */
    public abstract float[][] createVariables( List<Point3d> originalPoints, List<Point3d> projectedPoints,
                                               int polynomalOrder );

    /**
     * The central method, which actually transforms the points by applying the implemented polynomial.
     *
     * @param srcPoints
     *            to transform
     * @return the transformed points.
     * @throws TransformationException
     *             if for some reason one of the incoming points could not be transformed.
     */
    public abstract List<Point3d> applyPolynomial( List<Point3d> srcPoints )
                            throws TransformationException;

    /**
     * @return the Parameters for the calculation of the first coordinate.
     */
    public List<Double> getFirstParams() {
        return firstParams;
    }

    /**
     * @return the Parameters for the calculation of the second coordinate.
     */
    public List<Double> getSecondParams() {
        return secondParams;
    }

    @Override
    public final List<Point3d> doTransform( List<Point3d> srcPts )
                            throws TransformationException {
        return applyPolynomial( srcPts );
    }

    @Override
    public void inverse() {
        LOG.warn( "A Polynomial Transformation cannot be inverse yet" );
    }

    @Override
    public boolean isIdentity() {
        return false;
    }

    @Override
    public boolean isInverseTransform() {
        return false;
    }

}
