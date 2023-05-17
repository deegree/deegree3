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
package org.deegree.filter.spatial;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.uom.Measure;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Geometry;

/**
 * {@link SpatialOperator} that evaluates to true, iff geometries are beyond the specified
 * distance of each other.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class Beyond extends SpatialOperator {

	private final Measure distance;

	/**
	 * @param param1 geometry to compare to, can be <code>null</code> (use default
	 * geometry)
	 * @param param2 geometry argument for testing, never <code>null</code>
	 * @param distance distance, never <code>null</code>
	 */
	public Beyond(Expression param1, Geometry param2, Measure distance) {
		super(param1, param2);
		this.distance = distance;
	}

	/**
	 * @param param1 geometry to compare to, can be <code>null</code> (use default
	 * geometry)
	 * @param param2 value reference argument for testing, never <code>null</code>
	 * @param distance distance, never <code>null</code>
	 */
	public Beyond(Expression param1, ValueReference param2, Measure distance) {
		super(param1, param2);
		this.distance = distance;
	}

	/**
	 * @return the distance
	 */
	public Measure getDistance() {
		return distance;
	}

	@Override
	public <T> boolean evaluate(T obj, XPathEvaluator<T> xpathEvaluator) throws FilterEvaluationException {
		for (TypedObjectNode param1Value : param1.evaluate(obj, xpathEvaluator)) {
			Geometry geom = checkGeometryOrNull(param1Value);
			if (geom != null) {
				Geometry transformedLiteral = getCompatibleGeometry(geom, param2AsGeometry);
				// TODO what about the units of the distance when transforming?
				return geom.isBeyond(transformedLiteral, distance);
			}
		}
		return false;
	}

	public String toString(String indent) {
		String s = indent + "-Beyond\n";
		s += indent + param1 + "\n";
		if (param2AsGeometry != null)
			s += indent + param2AsGeometry + "\n";
		if (param2AsValueReference != null)
			s += indent + param2AsValueReference + "\n";
		s += indent + distance;
		return s;
	}

	@Override
	public Object[] getParams() {
		if (param2AsValueReference != null)
			return new Object[] { param1, param2AsValueReference };
		return new Object[] { param1, param2AsGeometry };
	}

}
