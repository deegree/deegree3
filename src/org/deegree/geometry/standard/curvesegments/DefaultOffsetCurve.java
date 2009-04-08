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

import org.deegree.commons.types.Length;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.curvesegments.OffsetCurve;
import org.deegree.geometry.primitive.curvesegments.CurveSegment.CurveSegmentType;

/**
 * Default implementation of {@link OffsetCurve} segments.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultOffsetCurve implements OffsetCurve {

    private Curve baseCurve;

    private Point direction;

    private Length distance;

    /**
     * Creates a new <code>DefaultOffsetCurve</code> instance from the given parameters.
     * 
     * @param baseCurve
     *            the base geometry
     * @param direction
     *            the direction of the offset
     * @param distance
     *            the distance from the base curve
     */
    public DefaultOffsetCurve( Curve baseCurve, Point direction, Length distance ) {
        this.baseCurve = baseCurve;
        this.direction = direction;
        this.distance = distance;
    }

    @Override
    public Curve getBaseCurve() {
        return baseCurve;
    }

    @Override
    public Point getDirection() {
        return direction;
    }

    @Override
    public Length getDistance() {
        return distance;
    }

    @Override
    public int getCoordinateDimension() {
        return baseCurve.getCoordinateDimension();
    }

    @Override
    public Interpolation getInterpolation() {
        throw new UnsupportedOperationException();
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
        return CurveSegmentType.OFFSET_CURVE;
    }    
}
