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
package org.deegree.filter.function.styling;

import static org.deegree.commons.utils.math.MathUtils.isZero;
import static org.deegree.filter.function.ParameterType.DOUBLE;

import java.util.ArrayList;
import java.util.List;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.Function;
import org.deegree.filter.function.FunctionProvider;
import org.deegree.filter.function.ParameterType;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.Workspace;

public class HatchingDistance implements FunctionProvider {

	private static final String NAME = "HatchingDistance";

	private static final List<ParameterType> INPUTS = new ArrayList<ParameterType>(2);

	static {
		INPUTS.add(DOUBLE);
		INPUTS.add(DOUBLE);
	}

	static void checkTwoArguments(String name, TypedObjectNode[] vals1, TypedObjectNode[] vals2)
			throws FilterEvaluationException {
		if (vals1.length == 0 || vals2.length == 0) {
			String msg = "The " + name + " function expects two arguments, but ";
			if (vals1.length == 0 && vals2.length == 0) {
				msg += "both arguments were missing.";
			}
			else {
				msg += "the ";
				msg += vals1.length == 0 ? "first" : "second";
				msg += " argument was missing.";
			}
			throw new FilterEvaluationException(msg);
		}
	}

	private static String extractAsText(TypedObjectNode tom) {
		PrimitiveValue pv = null;
		if (tom instanceof PrimitiveValue) {
			pv = (PrimitiveValue) tom;
		}
		else if (tom instanceof SimpleProperty) {
			pv = ((SimpleProperty) tom).getValue();
		}
		if (pv != null) {
			return pv.getAsText();
		}
		return tom.toString();
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public List<ParameterType> getArgs() {
		return INPUTS;
	}

	@Override
	public ParameterType getReturnType() {
		return DOUBLE;
	}

	@Override
	public Function create(List<Expression> params) {
		return new Function(NAME, params) {

			private <T> Pair<Double, Double> extractValues(Expression first, Expression second, T f,
					XPathEvaluator<T> xpathEvaluator) throws FilterEvaluationException {
				TypedObjectNode[] vals1 = first.evaluate(f, xpathEvaluator);
				TypedObjectNode[] vals2 = second.evaluate(f, xpathEvaluator);

				checkTwoArguments(NAME, vals1, vals2);

				return new Pair<>(Double.valueOf(extractAsText(vals1[0])), Double.valueOf(extractAsText(vals2[0])));
			}

			@Override
			public <T> TypedObjectNode[] evaluate(T obj, XPathEvaluator<T> xpathEvaluator)
					throws FilterEvaluationException {
				Pair<Double, Double> p = extractValues(getParams()[0], getParams()[1], obj, xpathEvaluator);
				double angle = p.getFirst();
				double distance;
				while (angle < 0.0)
					angle += 90.0d;
				while (angle >= 90.0)
					angle -= 90.0d;

				if (isZero(angle)) {
					distance = p.getSecond();
				}
				else {
					double ak = p.getSecond() / Math.sin(Math.toRadians(angle));
					distance = ak / Math.cos(Math.toRadians(angle));
				}

				return new TypedObjectNode[] { new PrimitiveValue(Double.valueOf(distance)) };
			}
		};
	}

	@Override
	public void init(Workspace ws) throws ResourceInitException {
		// nothing to do
	}

	@Override
	public void destroy() {
		// nothing to do
	}

}