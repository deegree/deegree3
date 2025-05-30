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
 * {@link CurveSegment} that uses three-point circular arc interpolation.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public interface ArcString extends CurveSegment {

	/**
	 * Returns the number of arcs of the string.
	 * @return the number <code>n</code> of arcs (n >= 1)
	 */
	public int getNumArcs();

	/**
	 * Returns the control points of the interpolation.
	 * <p>
	 * The number of points is always <code>2 * k + 1</code> points (k >= 1).
	 * </p>
	 * @return the control points of the interpolation, never <code>null</code>
	 */
	public Points getControlPoints();

}
