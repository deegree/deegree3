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
package org.deegree.geometry.primitive.patches;

import java.util.List;

import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;

/**
 * A {@link Rectangle} is a {@link PolygonPatch} defined by four planar points.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public interface Rectangle extends PolygonPatch {

    /**
     * Returns the first of the four control points.
     *
     * @return the first control point
     */
    public Point getPoint1();

    /**
     * Returns the second of the four control points.
     *
     * @return the second control point
     */
    public Point getPoint2();

    /**
     * Returns the third of the four control points.
     *
     * @return the third control point
     */
    public Point getPoint3();

    /**
     * Returns the last of the four control points.
     *
     * @return the last control point
     */
    public Point getPoint4();

    /**
     * Returns the sequence of control points as a {@link LinearRing}.
     *
     * @return the exterior ring
     */
    @Override
    public LinearRing getExteriorRing();

    @Override
    public List<LinearRing> getBoundaryRings();
}
