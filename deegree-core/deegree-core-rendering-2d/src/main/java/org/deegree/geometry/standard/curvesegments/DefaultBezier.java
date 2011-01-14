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
package org.deegree.geometry.standard.curvesegments;

import java.util.ArrayList;
import java.util.List;

import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.segments.Bezier;
import org.deegree.geometry.primitive.segments.Knot;

/**
 * Default implementation of {@link Bezier} segments.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class DefaultBezier implements Bezier {

    private Points controlPoints;

    private int polynomialDegree;

    private List<Knot> knots;

    /**
     * Creates a new <code>DefaultBezier</code> instance from the given parameters.
     *
     * @param controlPoints
     *            interpolation points
     * @param polynomialDegree
     * @param knot1
     * @param knot2
     */
    public DefaultBezier( Points controlPoints, int polynomialDegree, Knot knot1, Knot knot2 ) {
        this.controlPoints = controlPoints;
        this.polynomialDegree = polynomialDegree;
        knots = new ArrayList<Knot>( 2 );
        knots.add( knot1 );
        knots.add( knot2 );
    }

    @Override
    public Knot getKnot1() {
        return knots.get( 0 );
    }

    @Override
    public Knot getKnot2() {
        return knots.get( 1 );
    }

    @Override
    public int getCoordinateDimension() {
        return controlPoints.get( 0 ).getCoordinateDimension();
    }

    @Override
    public Points getControlPoints() {
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
        return CurveSegmentType.BEZIER;
    }

    @Override
    public boolean isPolynomial() {
        return true;
    }
}
