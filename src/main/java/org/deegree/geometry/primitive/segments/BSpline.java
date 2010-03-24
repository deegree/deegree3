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
package org.deegree.geometry.primitive.segments;

import java.util.List;

import org.deegree.geometry.points.Points;

/**
 * {@link CurveSegment} that uses either polynomial or rational interpolation.
 * <p>
 * Description from the GML 3.1.1 schema:
 * <p>
 * A B-Spline is a piecewise parametric polynomial or rational curve described in terms of control points and basis
 * functions. Knots are breakpoints on the curve that connect its pieces. They are given as a non-decreasing sequence of
 * real numbers. If the weights in the knots are equal then it is a polynomial spline. The degree is the algebraic
 * degree of the basis functions.
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public interface BSpline extends CurveSegment {

    /**
     * Returns the control points of the segment.
     *
     * @return the control points of the segment
     */
    public Points getControlPoints();

    /**
     * Returns the degree of the polynomial used for interpolation in this spline.
     *
     * @return the degree of the polynomial
     */
    public int getPolynomialDegree();

    /**
     * Returns whether the interpolation is polynomial or rational.
     *
     * @return true, if the interpolation is polynomial, false if it's rational 
     */
    public boolean isPolynomial();

    /**
     * Returns the knots that define the spline basis functions.
     *
     * @return list of distinctive knots
     */
    public List<Knot> getKnots();
}
