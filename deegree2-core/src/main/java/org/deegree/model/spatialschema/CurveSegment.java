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

package org.deegree.model.spatialschema;

import org.deegree.model.crs.CoordinateSystem;

/**
 * The interface defines the root of each unit building Curves
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public interface CurveSegment extends GenericCurve {

    /**
     * @return the number of points building the curve segment
     */
    int getNumberOfPoints();

    /**
     * @return all positions of the segement as array of Point
     */
    Position[] getPositions();

    /**
     * @param index
     *            to get the curve
     * @return the curve position at the submitted index
     */
    Position getPositionAt( int index );

    /**
     * reverses the direction of the curvesegment
     */
    void reverse();

    /**
     * @return the coordinate system of the curve segment
     */
    CoordinateSystem getCoordinateSystem();

    /**
     * The Boolean valued operation "intersects" shall return TRUE if this Geometry intersects another Geometry. Within
     * a Complex, the Primitives do not intersect one another. In general, topologically structured data uses shared
     * geometric objects to capture intersection information.
     *
     * @param gmo
     * @return true if this geometry intersects with given geometry.
     */
    boolean intersects( Geometry gmo );

    /**
     * The Boolean valued operation "contains" shall return TRUE if this Geometry contains another Geometry.
     *
     * @param gmo
     * @return true if this geometry contains given geometry.
     */
    boolean contains( Geometry gmo );

}
