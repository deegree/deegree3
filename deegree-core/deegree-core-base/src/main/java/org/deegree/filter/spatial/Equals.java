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
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Geometry;

/**
 * If a geometry is spatially equal to an other geometry.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class Equals extends SpatialOperator {

	/**
	 * @param param1 geometry to compare to, can be <code>null</code> (use default
	 * geometry)
	 * @param param2 geometry argument for testing, never <code>null</code>
	 */
	public Equals(Expression param1, Geometry param2) {
		super(param1, param2);
	}

	/**
	 * @param param1 geometry to compare to, can be <code>null</code> (use default
	 * geometry)
	 * @param param2 value reference argument for testing, never <code>null</code>
	 */
	public Equals(Expression param1, ValueReference param2) {
		super(param1, param2);
	}

	@Override
	public <T> boolean evaluate(T obj, XPathEvaluator<T> xpathEvaluator) throws FilterEvaluationException {
		for (TypedObjectNode paramValue : param1.evaluate(obj, xpathEvaluator)) {
			Geometry geom = checkGeometryOrNull(paramValue);
			if (geom != null) {
				Geometry transformedLiteral = getCompatibleGeometry(geom, param2AsGeometry);
				return geom.equals(transformedLiteral);
			}
		}
		return false;
	}

	@Override
	public String toString(String indent) {
		String s = indent + "-Equals\n";
		s += indent + param1 + "\n";
		if (param2AsGeometry != null)
			s += indent + param2AsGeometry;
		if (param2AsValueReference != null)
			s += indent + param2AsValueReference;
		return s;
	}

	@Override
	public Object[] getParams() {
		if (param2AsValueReference != null)
			return new Object[] { param1, param2AsValueReference };
		return new Object[] { param1, param2AsGeometry };
	}

}
