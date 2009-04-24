//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
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
     * 
     * @return whether the curve segment has 3 or 2 coordinates (2 for flat surfaces; 3 for surfaces in a 3D space)
     */
    public boolean is3D();

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