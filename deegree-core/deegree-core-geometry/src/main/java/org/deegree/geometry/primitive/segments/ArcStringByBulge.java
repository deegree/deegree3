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

import org.deegree.geometry.points.Points;

/**
 * Circular {@link CurveSegment}.
 * <p>
 * From the GML 3.1.1 spec: This variant of the arc computes the mid points of the arcs
 * instead of storing the coordinates directly. The control point sequence consists of the
 * start and end points of each arc plus the bulge.
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public interface ArcStringByBulge extends CurveSegment {

	/**
	 * Returns the number of arcs of the string.
	 * @return the number of arcs
	 */
	public int getNumArcs();

	/**
	 * Returns the bulge values.
	 * @return the bulge values
	 */
	public double[] getBulges();

	/**
	 * Returns the normal vectors that define the arc string.
	 * @return the normal vectors
	 */
	public Points getNormals();

	/**
	 * Returns the control points of the segment.
	 * @return the control points of the segment
	 */
	public Points getControlPoints();

}
