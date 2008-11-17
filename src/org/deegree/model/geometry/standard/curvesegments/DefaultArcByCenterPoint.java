//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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
package org.deegree.model.geometry.standard.curvesegments;

import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.curvesegments.ArcByCenterPoint;
import org.deegree.model.geometry.primitive.curvesegments.CurveSegment.CurveSegmentType;
import org.deegree.model.gml.Angle;
import org.deegree.model.gml.Length;

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

    private Length radius;

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
    public DefaultArcByCenterPoint( Point midPoint, Length radius, Angle startAngle, Angle endAngle ) {
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
    public Length getRadius() {
        return radius;
    }

    @Override
    public Angle getStartAngle() {
        return startAngle;
    }

    @Override
    public int getCoordinateDimension() {
        throw new UnsupportedOperationException();
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
        return CurveSegmentType.ARC_BY_CENTER_POINT;
    }
}
