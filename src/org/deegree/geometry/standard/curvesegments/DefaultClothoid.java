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

import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.segments.Clothoid;

/**
 * Default implementation of {@link Clothoid} segments.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultClothoid implements Clothoid {

    private AffinePlacement referenceLocation;

    private double scaleFactor;

    private double startParameter;

    private double endParameter;

    /**
     * Creates a new <code>DefaultClothoid</code> instance from the given parameters.
     * 
     * @param referenceLocation
     *            the affine mapping that places the curve defined by the Fresnel Integrals into the coordinate
     *            reference system of this object
     * @param scaleFactor
     *            the value for the constant in the Fresnel's integrals
     * @param startParameter
     *            the arc length distance from the inflection point that will be the start point for this curve segment
     * @param endParameter
     *            the arc length distance from the inflection point that will be the end point for this curve segment
     */
    public DefaultClothoid( AffinePlacement referenceLocation, double scaleFactor, double startParameter,
                            double endParameter ) {
        this.referenceLocation = referenceLocation;
        this.scaleFactor = scaleFactor;
        this.startParameter = startParameter;
        this.endParameter = endParameter;
    }

    @Override
    public AffinePlacement getReferenceLocation() {
        return referenceLocation;
    }

    @Override
    public double getScaleFactor() {
        return scaleFactor;
    }

    @Override
    public double getStartParameter() {
        return startParameter;
    }

    @Override
    public double getEndParameter() {
        return endParameter;
    }

    @Override
    public int getCoordinateDimension() {
        return referenceLocation.getOutDimension();
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
        return CurveSegmentType.CLOTHOID;
    }
}
