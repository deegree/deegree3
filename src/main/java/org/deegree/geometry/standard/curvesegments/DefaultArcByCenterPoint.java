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

import org.deegree.commons.uom.Angle;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.segments.ArcByCenterPoint;

/**
 * Default implementation of {@link ArcByCenterPoint} segments.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class DefaultArcByCenterPoint implements ArcByCenterPoint {

    private Point midPoint;

    private Measure radius;

    private Angle startAngle;

    private Angle endAngle;

    /**
     * Creates a new <code>DefaultArcByCenterPoint</code> instance from the given parameters.
     *
     * @param midPoint
     * @param radius
     * @param startAngle
     * @param endAngle
     */
    public DefaultArcByCenterPoint( Point midPoint, Measure radius, Angle startAngle, Angle endAngle ) {
        this.midPoint = midPoint;
        this.radius = radius;
        this.startAngle = startAngle;
        this.endAngle = endAngle;
    }

    @Override
    public Angle getEndAngle() {
        return endAngle;
    }

    @Override
    public Point getMidPoint() {
        return midPoint;
    }

    @Override
    public Measure getRadius(Unit requestedUnits) {
        return radius;
    }

    @Override
    public Angle getStartAngle() {
        return startAngle;
    }

    @Override
    public int getCoordinateDimension() {
        return midPoint.getCoordinateDimension();
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
        return CurveSegmentType.ARC_BY_CENTER_POINT;
    }
}
