/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2022 by:
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

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
package org.deegree.style.styling.wkn.shape;

import static org.deegree.commons.utils.math.MathUtils.isZero;

import java.awt.geom.GeneralPath;
import java.util.Iterator;

import org.deegree.geometry.linearization.GeometryLinearizer;
import org.deegree.geometry.linearization.LinearizationCriterion;
import org.deegree.geometry.linearization.NumPointsCriterion;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;

public class ShapeConverterLinearize extends AbstractShapeConverter {

	private static final GeometryLinearizer linearizer = new GeometryLinearizer();

	private final LinearizationCriterion crit;

	private final boolean close;

	public ShapeConverterLinearize(boolean close, int pointsPerArc) {
		this.close = close;
		this.crit = new NumPointsCriterion(pointsPerArc);
	}

	@Override
	protected void toShape(GeneralPath path, Curve geometry) {
		geometry = linearizer.linearize(geometry, crit);

		Points points = geometry.getControlPoints();
		Iterator<Point> iter = points.iterator();
		Point p = iter.next();
		double x = p.get0(), y = p.get1();
		path.moveTo(x, y);
		while (iter.hasNext()) {
			p = iter.next();
			if (iter.hasNext()) {
				path.lineTo(p.get0(), p.get1());
			}
			else {
				if (close && isZero(x - p.get0()) && isZero(y - p.get1())) {
					path.closePath();
				}
				else {
					path.lineTo(p.get0(), p.get1());
				}
			}
		}
	}

}
