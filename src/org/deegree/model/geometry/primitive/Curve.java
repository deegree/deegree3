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
package org.deegree.model.geometry.primitive;

import java.util.List;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.composite.CompositeCurve;
import org.deegree.model.geometry.primitive.curvesegments.CurveSegment;
import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;

/**
 * <code>Curve</code> instances are 1D-geometries that consist of an arbitrary number of curve segments.
 * 
 * @see CompositeCurve
 * @see LineString
 * @see OrientableCurve
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface Curve extends GeometricPrimitive {

    /**
     * Convenience enum type for discriminating the different curve variants.
     */
    public enum CurveType {
        /** Generic curve that consists of an arbitrary number of segments. */
        Curve,
        /** Curve that consists of a single segment with linear interpolation. */
        LineString,
        /** Curve that wraps a base curve with additional orientation flag. */
        OrientableCurve,
        /** Curve composited from multiple base curves. */
        CompositeCurve,
    }

    /**
     * Returns the type of curve.
     * 
     * @return the type of curve
     */
    public CurveType getCurveType();

    /**
     * Must always return {@link GeometricPrimitive.PrimitiveType#Curve}.
     * 
     * @return {@link GeometricPrimitive.PrimitiveType#Curve}
     */
    @Override
    public PrimitiveType getPrimitiveType();

    /**
     * Returns whether the curve forms a closed loop.
     * 
     * @return true, if the curve forms a closed loop, false otherwise
     */
    public boolean isClosed();

    /**
     * 
     * @return length of a curve measured in units of the assigend {@link CoordinateSystem}
     */
    public double getLength();

    /**
     * The boundary of a curve is the set of points at either end of the curve. If the curve is a cycle, the two ends
     * are identical, and the curve (if topologically closed) is considered to not have a boundary.
     * 
     * @return boundary of a curve. If a curve does not have a boundary because it is closed an empty {@link List} shall
     *         be retruned
     */
    public List<Point> getBoundary();

    /**
     * Returns the start point of the curve.
     * 
     * @return the start point of the curve
     */
    public Point getStartPoint();

    /**
     * Returns the end point of the curve.
     * 
     * @return the end point of the curve
     */
    public Point getEndPoint();

    /**
     * Returns the segments that constitute this curve.
     * 
     * @return the segments that constitute this curve
     */
    public List<CurveSegment> getCurveSegments();

    /**
     * Convenience method for accessing the control points of linear interpolated curves.
     * <p>
     * NOTE: This method is only safe to use when the curve is a {@link LineString} or {@link LinearRing} or it only
     * consists of {@link LineStringSegment}s. In any other case it will fail.
     * </p>
     * 
     * @return the control points
     * @throws IllegalArgumentException
     *             if the curve is not linear interpolated
     */
    public List<Point> getControlPoints();

    /**
     * Returns a linear interpolated representation of the curve.
     * <p>
     * Please note that this operation returns an approximated version if the curve uses non-linear curve segments.
     * 
     * @return a linear interpolated representation of the curve
     */
    public LineString getAsLineString();
}