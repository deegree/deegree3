//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.geometry.primitive.curvesegments;

import java.util.List;

import org.deegree.model.geometry.primitive.CurveSegment;
import org.deegree.model.geometry.primitive.Point;

/**
 * A <code>BSpline</code> is a {@link CurveSegment} that uses either polynomial or rational interpolation.
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
    public List<Point> getControlPoints();    
    
    /**
     * Returns the degree of the polynomial used for interpolation in this spline.
     * 
     * @return the degree of the polynomial
     */
    public int getPolynomialDegree();

    /**
     * Returns the curve interpolation mechanism used for this segment, this is either
     * {@link CurveSegment.Interpolation#polynomialSpline} or {@link CurveSegment.Interpolation#rationalSpline}.
     * 
     * @returns the curve interpolation mechanism
     */
    @Override
    public Interpolation getInterpolation();

    /**
     * Returns the knots that define the spline basis functions.
     * 
     * @return list of distinctive knots
     */
    public List<Knot> getKnots();
}
