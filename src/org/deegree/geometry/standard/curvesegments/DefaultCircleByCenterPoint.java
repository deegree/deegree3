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
import org.deegree.commons.uom.Length;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.segments.CircleByCenterPoint;
import org.deegree.geometry.primitive.segments.CurveSegment.CurveSegmentType;

/**
 * Default implementation of {@link CircleByCenterPoint} segments.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class DefaultCircleByCenterPoint extends DefaultArcByCenterPoint implements CircleByCenterPoint {

    /**
     * Creates a new <code>DefaultCircleByCenterPoint</code> instance from the given parameters.
     *
     * @param midPoint
     * @param radius
     * @param startAngle
     */
    public DefaultCircleByCenterPoint( Point midPoint, Length radius, Angle startAngle ) {
        super( midPoint, radius, startAngle, startAngle );
    }

    @Override
    public CurveSegmentType getSegmentType() {
        return CurveSegmentType.CIRCLE_BY_CENTER_POINT;
    }
}
