//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.geometry.validation;

/**
 * Discriminates geometry validation events.
 * 
 * @see GeometryValidator
 * @see GeometryValidationEventHandler
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public enum ValidationEvent {

    /** Segment contains identical successive points. */
    SEGMENT_DUPLICATE_POINTS,
    /** End point of a segment <code>n</code> does not coincide with the start point of segment <code>n+1</code>. */
    CURVE_DISCONTINUITY,
    /** Curve intersects itself. */
    CURVE_SELF_INTERSECTION,
    /** Curve course contains an angle that is considered to be too acute. */
    CURVE_ACUTE_ANGLE,    
    /** End point does not coincide with the start point. */
    RING_NOT_CLOSED,
    /** Ring intersects itself. */
    RING_SELF_INTERSECTION,
    /** Orientation of an exterior polygon ring (i.e. the shell) does not follow counter-clockwise order. */
    EXTERIOR_RING_CCW,
    /** Orientation of an interior polygon ring (i.e. a hole) does not follow clockwise order. */
    INTERIOR_RING_CW,
    /** Two interior rings touch each other. */
    INTERIOR_RINGS_TOUCH,
    /** Two interior rings intersect each other. */
    INTERIOR_RINGS_INTERSECTS,
    /** An interior ring lies inside another. */
    INTERIOR_RINGS_WITHIN,
    /** An interior ring touches the shell. */
    EXTERIOR_RING_TOUCHES_INTERIOR,
    /** An interior ring intersects the shell. */
    EXTERIOR_RING_INTERSECTS_INTERIOR,
    /** The exterior ring lies inside an interior ring. */
    EXTERIOR_RING_WITHIN_INTERIOR
}
