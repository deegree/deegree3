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
package org.deegree.geometry.standard.curvesegments;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.segments.OffsetCurve;

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

    private Measure distance;

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
    public DefaultOffsetCurve( Curve baseCurve, Point direction, Measure distance ) {
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
    public Measure getDistance( Unit requestedUnits ) {
        return distance;
    }

    @Override
    public int getCoordinateDimension() {
        return baseCurve.getCoordinateDimension();
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
