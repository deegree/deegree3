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
package org.deegree.geometry.primitive.curvesegments;

import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;

/**
 * A <code>CurveSegment</code> is a portion of a {@link Curve} which uses a single interpolation method.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version. $Revision$, $Date$
 */
public interface CurveSegment {

    /**
     * Convenience enum type for discriminating the different curve segment variants.
     */
    public enum CurveSegmentType {
        ARC, ARC_BY_BULGE, ARC_BY_CENTER_POINT, ARC_STRING, ARC_STRING_BY_BULGE, BEZIER, BSPLINE, CIRCLE, CIRCLE_BY_CENTER_POINT, CLOTHOID, CUBIC_SPLINE, GEODESIC, GEODESIC_STRING, LINE_STRING_SEGMENT, OFFSET_CURVE
    }

    /**
     * All known curve segment interpolations.
     */
    public enum Interpolation {
        /**
         * A linear interpolation.
         */
        linear,
        /**
         * A geodesic interpolation.
         */
        geodesic,
        /**
         * A circularArcCenterPointWithRadius interpolation.
         */
        circularArcCenterPointWithRadius,
        /**
         * A circularArc3Points interpolation.
         */
        circularArc3Points,
        /**
         * A circularArc2PointWithBulge interpolation.
         */
        circularArc2PointWithBulge,
        /**
         * A elliptical interpolation.
         */
        elliptical,
        /**
         * A clothoid interpolation.
         */
        clothoid,
        /**
         * A conic interpolation.
         */
        conic,
        /**
         * A cubicSpline interpolation.
         */
        cubicSpline,
        /**
         * A polynomialSpline interpolation.
         */
        polynomialSpline,
        /**
         * A rationalSpline interpolation.
         */
        rationalSpline
    }

    /**
     * Returns the coordinate dimension, i.e. the dimension of the space that the curve is embedded in.
     * 
     * @return the coordinate dimension
     */
    public int getCoordinateDimension();

    /**
     *
     * @return interpolation method used by this curve segment
     */
    public Interpolation getInterpolation();

    /**
     * Returns the type of curve segment.
     *
     * @return the type of curve segment
     */
    public CurveSegmentType getSegmentType();

    /**
     * Returns the start point of the segment.
     *
     * @return the start point of the segment
     */
    public Point getStartPoint();

    /**
     * Returns the end point of the segment.
     *
     * @return the end point of the segment
     */
    public Point getEndPoint();
}
