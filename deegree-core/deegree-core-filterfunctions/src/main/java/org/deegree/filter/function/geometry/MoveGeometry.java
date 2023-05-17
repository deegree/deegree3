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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.filter.function.geometry;

import static org.deegree.filter.function.ParameterType.DOUBLE;
import static org.deegree.filter.function.ParameterType.GEOMETRY;
import static org.deegree.filter.utils.FilterUtils.getGeometryValue;
import static org.deegree.geometry.utils.GeometryUtils.move;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.Function;
import org.deegree.filter.function.FunctionProvider;
import org.deegree.filter.function.ParameterType;
import org.deegree.geometry.Geometry;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class MoveGeometry implements FunctionProvider {

	static final Logger LOG = getLogger(MoveGeometry.class);

	private static final String NAME = "MoveGeometry";

	private static final List<ParameterType> INPUTS = new ArrayList<ParameterType>(3);

	static {
		INPUTS.add(GEOMETRY);
		INPUTS.add(DOUBLE);
		INPUTS.add(DOUBLE);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public List<ParameterType> getArgs() {
		return Collections.singletonList(GEOMETRY);
	}

	@Override
	public ParameterType getReturnType() {
		return DOUBLE;
	}

	@Override
	public Function create(List<Expression> params) {
		return new Function(NAME, params) {

			@Override
			public <T> TypedObjectNode[] evaluate(T obj, XPathEvaluator<T> xpathEvaluator)
					throws FilterEvaluationException {
				TypedObjectNode[] geometry = getParams()[0].evaluate(obj, xpathEvaluator);
				TypedObjectNode[] offx = getParams()[1].evaluate(obj, xpathEvaluator);
				TypedObjectNode[] offy = getParams()[2].evaluate(obj, xpathEvaluator);
				if (geometry.length != 1) {
					throw new FilterEvaluationException(
							"The MoveGeometry function's first argument must " + "evaluate to exactly one value.");
				}
				if (offx.length != 1) {
					throw new FilterEvaluationException(
							"The MoveGeometry function's second argument must " + "evaluate to exactly one value.");
				}
				if (offy.length != 1) {
					throw new FilterEvaluationException(
							"The MoveGeometry function's third argument must " + "evaluate to exactly one value.");
				}
				try {
					double movex = Double.parseDouble(offx[0].toString());
					double movey = Double.parseDouble(offy[0].toString());
					Geometry geom = getGeometryValue(geometry[0]);
					return new TypedObjectNode[] { move(geom, movex, movey) };
				}
				catch (NumberFormatException e) {
					throw new FilterEvaluationException("The MoveGeometry function's second and third argument must "
							+ "evaluate to numeric values.");
				}
			}
		};
	}

	@Override
	public void init(Workspace ws) {
		// nothing to do
	}

	@Override
	public void destroy() {
		// nothing to do
	}

}
