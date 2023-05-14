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
package org.deegree.filter.function.geometry;

import static org.deegree.filter.function.ParameterType.GEOMETRY;
import static org.deegree.filter.function.ParameterType.POINT;
import static org.deegree.filter.utils.FilterUtils.getGeometryValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.Function;
import org.deegree.filter.function.FunctionProvider;
import org.deegree.filter.function.ParameterType;
import org.deegree.geometry.Geometry;
import org.deegree.workspace.Workspace;

/**
 * Returns the centroid for each input geometry. Other inputs are ignored.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class Centroid implements FunctionProvider {

	private static final String NAME = "Centroid";

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
		return POINT;
	}

	@Override
	public Function create(List<Expression> params) {
		return new Function(NAME, params) {
			@Override
			public TypedObjectNode[] evaluate(List<TypedObjectNode[]> args) throws FilterEvaluationException {
				TypedObjectNode[] inputs = args.get(0);
				List<TypedObjectNode> outputs = new ArrayList<TypedObjectNode>(inputs.length);
				for (TypedObjectNode input : inputs) {
					Geometry geom = getGeometryValue(input);
					if (geom != null) {
						outputs.add(geom.getCentroid());
					}
				}
				return outputs.toArray(new TypedObjectNode[outputs.size()]);
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
