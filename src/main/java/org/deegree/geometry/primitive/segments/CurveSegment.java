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

import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;

/**
 * A <code>CurveSegment</code> is a portion of a {@link Curve} which uses a single interpolation method.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version. $Revision$, $Date$
 */
public interface CurveSegment {

    /**
     * Convenience enum type for discriminating the different curve segment variants in switch statements.
     */
    public enum CurveSegmentType {
        /** Segment is an {@link Arc}. */
        ARC,
        /** Segment is an {@link ArcByBulge}. */
        ARC_BY_BULGE,
        /** Segment is an {@link ArcByCenterPoint}. */
        ARC_BY_CENTER_POINT,
        /** Segment is an {@link ArcString}. */
        ARC_STRING,
        /** Segment is an {@link ArcStringByBulge}. */
        ARC_STRING_BY_BULGE,
        /** Segment is a {@link Bezier}. */
        BEZIER,
        /** Segment is a {@link BSpline}. */
        BSPLINE,
        /** Segment is a {@link Circle}. */
        CIRCLE,
        /** Segment is a {@link CircleByCenterPoint}. */
        CIRCLE_BY_CENTER_POINT,
        /** Segment is a {@link Clothoid}. */
        CLOTHOID,
        /** Segment is a {@link CubicSpline}. */
        CUBIC_SPLINE,
        /** Segment is a {@link Geodesic}. */
        GEODESIC,
        /** Segment is a {@link GeodesicString}. */
        GEODESIC_STRING,
        /** Segment is a {@link LineStringSegment}. */
        LINE_STRING_SEGMENT,
        /** Segment is an {@link OffsetCurve}. */
        OFFSET_CURVE
    }

    /**
     * Returns the type of curve segment.
     *
     * @return the type of curve segment
     */
    public CurveSegmentType getSegmentType();    
    
    /**
     * Returns the coordinate dimension, i.e. the dimension of the space that the curve is embedded in.
     * 
     * @return the coordinate dimension
     */
    public int getCoordinateDimension();

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
