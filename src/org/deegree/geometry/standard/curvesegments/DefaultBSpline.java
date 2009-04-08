//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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
package org.deegree.geometry.standard.curvesegments;

import java.util.List;

import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.curvesegments.BSpline;
import org.deegree.geometry.primitive.curvesegments.Knot;
import org.deegree.geometry.primitive.curvesegments.CurveSegment.CurveSegmentType;

/**
 * Default implementation of {@link BSpline} segments.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultBSpline implements BSpline {

    private List<Point> controlPoints;

    private int polynomialDegree;

    private boolean isPolynomial;

    private List<Knot> knots;

    /**
     * Creates a new <code>DefaultBSpline</code> instance from the given parameters.
     * 
     * @param controlPoints
     * @param polynomialDegree
     * @param knots
     * @param isPolynomial
     */
    public DefaultBSpline( List<Point> controlPoints, int polynomialDegree, List<Knot> knots, boolean isPolynomial ) {
        this.controlPoints = controlPoints;
        this.polynomialDegree = polynomialDegree;
        this.knots = knots;
        this.isPolynomial = isPolynomial;        
    }

    @Override
    public int getCoordinateDimension() {
        return controlPoints.get( 0 ).getCoordinateDimension();
    }

    @Override
    public Interpolation getInterpolation() {
        if ( isPolynomial ) {
            return Interpolation.polynomialSpline;
        }
        return Interpolation.rationalSpline;
    }

    @Override
    public List<Point> getControlPoints() {
        return controlPoints;
    }

    @Override
    public int getPolynomialDegree() {
        return polynomialDegree;
    }

    @Override
    public List<Knot> getKnots() {
        return knots;
    }

    @Override
    public Point getStartPoint() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point getEndPoint() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public CurveSegmentType getSegmentType() {
        return CurveSegmentType.BSPLINE;
    }    
}
