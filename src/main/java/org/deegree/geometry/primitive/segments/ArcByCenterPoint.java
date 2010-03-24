//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/geometry/primitive/CurveSegment.java $
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

import org.deegree.commons.uom.Angle;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.geometry.primitive.Point;

/**
 * Circular {@link CurveSegment} that consists of a single arc only.
 * <p>
 * This variant of the arc requires that the points on the arc have to be computed instead of storing the coordinates
 * directly. The control point is the center point of the arc plus the radius and the bearing at start and end. This
 * representation can be used only in 2D.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface ArcByCenterPoint extends CurveSegment {

    /**
     * Returns the center point of the arc.
     * 
     * @return the center point of the arc
     */
    public Point getMidPoint();

    /**
     * Returns the radius of the arc.
     * 
     * @param requestedUnits
     *            units that the radius should be expressed as
     * @return the radius of the arc
     */
    public Measure getRadius( Unit requestedUnits );

    /**
     * Returns the bearing of the arc at the start.
     * 
     * @return the bearing of the arc at the start
     */
    public Angle getStartAngle();

    /**
     * Returns the bearing of the arc at the end.
     * 
     * @return the bearing of the arc at the end
     */
    public Angle getEndAngle();
}
