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
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.segments.Circle;

/**
 * Default implementation of {@link Circle} segments.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class DefaultCircle extends DefaultArc implements Circle {

	/**
	 * Creates a new <code>DefaultCircle</code> instance from the given parameters.
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	public DefaultCircle(Point p1, Point p2, Point p3) {
		super(p1, p2, p3);
	}

	@Override
	public Point getMidPoint() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Measure getRadius(Unit requestedUnits) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Point getEndPoint() {
		return points.get(0);
	}

	@Override
	public CurveSegmentType getSegmentType() {
		return CurveSegmentType.CIRCLE;
	}

}
