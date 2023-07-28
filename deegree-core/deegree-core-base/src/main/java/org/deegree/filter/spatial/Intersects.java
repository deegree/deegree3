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
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.feature.Feature;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for representing and evaluating the <code>Intersects</code> operator.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class Intersects extends SpatialOperator {

	private static final Logger LOG = LoggerFactory.getLogger(Intersects.class);

	/**
	 * Instantiates a {@link Intersects} operator with geometry as second parameter.
	 * @param propName may actually be <code>null</code> (deegree extension to cope with
	 * features that have only hidden geometry props)
	 * @param geometry never <code>null</code>
	 */
	public Intersects(Expression propName, Geometry geometry) {
		super(propName, geometry);
	}

	/**
	 * Instantiates a {@link Intersects} operator with value reference as second
	 * parameter.
	 * @param propName may actually be <code>null</code> (deegree extension to cope with
	 * features that have only hidden geometry props)
	 * @param valueReference never <code>null</code>
	 */
	public Intersects(Expression propName, ValueReference valueReference) {
		super(propName, valueReference);
	}

	@Override
	public <T> boolean evaluate(T obj, XPathEvaluator<T> xpathEvaluator) throws FilterEvaluationException {
		if (param2AsGeometry == null)
			return false;

		Expression param1 = getParam1();
		if (param1 != null) {
			for (TypedObjectNode paramValue : param1.evaluate(obj, xpathEvaluator)) {
				Geometry param1Value = checkGeometryOrNull(paramValue);
				if (param1Value != null) {
					Geometry transformedGeom = getCompatibleGeometry(param1Value, param2AsGeometry);
					return transformedGeom.intersects(param1Value);
				}
			}
		}
		else if (obj instanceof Feature) {
			// handle the case where the property name is empty
			Feature f = (Feature) obj;
			boolean foundGeom = false;
			for (Property prop : f.getProperties()) {
				if (prop.getValue() instanceof Geometry) {
					foundGeom = true;
					Geometry geom = (Geometry) prop.getValue();
					Geometry transformedGeom = getCompatibleGeometry(param2AsGeometry, geom);
					if (transformedGeom.intersects(param2AsGeometry)) {
						return true;
					}
				}
			}
			if (!foundGeom) {
				Envelope env = f.getEnvelope();
				if (env != null) {
					Geometry g = getCompatibleGeometry(param2AsGeometry, env);
					if (g.intersects(param2AsGeometry)) {
						return true;
					}
				}
			}
			if (f.getExtraProperties() != null) {
				for (Property prop : f.getExtraProperties().getProperties()) {
					if (prop.getValue() instanceof Geometry) {
						Geometry geom = (Geometry) prop.getValue();
						Geometry transformedGeom = getCompatibleGeometry(param2AsGeometry, geom);
						if (transformedGeom.intersects(param2AsGeometry)) {
							return true;
						}
					}
				}
			}
		}
		else {
			LOG.warn("Evaluating Intersects on non-Feature object and property name not specified.");
		}
		return false;
	}

	@Override
	public String toString(String indent) {
		String s = indent + "-Intersects\n";
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