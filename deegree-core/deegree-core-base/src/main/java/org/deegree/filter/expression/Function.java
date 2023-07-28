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
package org.deegree.filter.expression;

import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;

/**
 * Generic {@link Function} implementation that can be used to represent an arbitrary
 * function, but that doesn't offer any evaluation capabilities (this is added by
 * subclassing).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class Function implements Expression {

	private String name;

	private List<Expression> params;

	/**
	 * Creates a new {@link Function} instance.
	 * @param name name of the function, must not be <code>null</code>
	 * @param params parameters of the function, may be empty, but never <code>null</code>
	 */
	public Function(String name, List<Expression> params) {
		this.name = name;
		this.params = params;
	}

	/**
	 * Returns the name of the function.
	 * @return the name of the function, never <code>null</code>
	 */
	public String getName() {
		return name;
	}

	/**
	 * Always returns {@link Expression.Type#FUNCTION}.
	 * @return {@link Expression.Type#FUNCTION}
	 */
	public Type getType() {
		return Type.FUNCTION;
	}

	/**
	 * Returns the parameters of the function.
	 * @return the parameters of the function, may be empty, but never <code>null</code>
	 */
	public List<Expression> getParameters() {
		return params;
	}

	/**
	 * Evaluates the function for the given arguments (without a context object).
	 * <p>
	 * The double-dimension of input arguments is due to the fact that input arguments
	 * usually stem from XPath-evaluation performed on a {@link TypedObjectNode}.
	 * Therefore, multiple values per input may occur. The outer list contains one entry
	 * per parameter, the inner array contain the values for each parameter. Depending on
	 * the type of function it may return multiple outputs as well.
	 * </p>
	 * @param args input arguments, must match the parameter signature, can be empty, but
	 * never <code>null</code>
	 * @return output values, can be empty, but never <code>null</code>
	 * @throws FilterEvaluationException
	 */
	public TypedObjectNode[] evaluate(List<TypedObjectNode[]> args) throws FilterEvaluationException {
		throw new FilterEvaluationException("Evaluation of function '" + getName()
				+ "' is not implemented (or not supported without a context object.");
	}

	/**
	 * Evaluates the function for the given arguments and context.
	 * <p>
	 * If the function requires information from the context object for performing the
	 * computation, this method must be overridden.
	 * </p>
	 * @param obj context object, must not be <code>null</code>
	 * @param args input arguments, must match the parameter signature, can be empty, but
	 * never <code>null</code>
	 * @return output values, can be empty, but never <code>null</code>
	 * @throws FilterEvaluationException
	 */
	protected <T> TypedObjectNode[] evaluate(T obj, List<TypedObjectNode[]> args) throws FilterEvaluationException {
		return evaluate(args);
	}

	@Override
	public <T> TypedObjectNode[] evaluate(T obj, XPathEvaluator<T> xpathEvaluator) throws FilterEvaluationException {

		List<TypedObjectNode[]> args = new ArrayList<TypedObjectNode[]>(params.size());
		for (Expression param : params) {
			TypedObjectNode[] inputs = param.evaluate(obj, xpathEvaluator);
			args.add(inputs);
		}
		return evaluate(obj, args);
	}

	@Override
	public String toString(String indent) {
		String s = indent + "-Function (" + name + ")\n";
		if (params != null) {
			for (Expression param : params) {
				s += param.toString(indent + "  ");
			}
		}
		return s;
	}

	@Override
	public Expression[] getParams() {
		return params.toArray(new Expression[params.size()]);
	}

}