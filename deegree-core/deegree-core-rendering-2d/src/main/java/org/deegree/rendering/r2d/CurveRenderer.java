/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.rendering.r2d;

import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.style.styling.PointStyling;

/**
 * <code>CurveRenderer</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class CurveRenderer {

	private Java2DRenderer renderer;

	public CurveRenderer(Java2DRenderer renderer) {
		this.renderer = renderer;
	}

	void render(PointStyling styling, Curve curve) {
		if (curve.getCurveSegments().size() != 1 || !(curve.getCurveSegments().get(0) instanceof LineStringSegment)) {
			// TODO handle non-linear and multiple curve segments
			throw new IllegalArgumentException();
		}
		LineStringSegment segment = ((LineStringSegment) curve.getCurveSegments().get(0));
		// coordinate representation is still subject to change...
		for (Point point : segment.getControlPoints()) {
			point.setCoordinateSystem(curve.getCoordinateSystem());
			renderer.render(styling, point);
		}
	}

}
