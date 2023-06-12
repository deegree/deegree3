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
package org.deegree.filter;

/**
 * {@link Filter} that selects objects that test positively against {@link Operator}-based
 * expressions.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class OperatorFilter implements Filter {

	private Operator rootOperator;

	/**
	 * Creates a new {@link OperatorFilter}.
	 * @param rootOperator root operator that defines the test expression, must not be
	 * <code>null</code>
	 * @throws NullPointerException
	 */
	public OperatorFilter(Operator rootOperator) {
		this.rootOperator = rootOperator;
		if (rootOperator == null) {
			throw new NullPointerException();
		}
	}

	/**
	 * Always returns {@link Filter.Type#OPERATOR_FILTER} (for {@link OperatorFilter}
	 * instances).
	 * @return {@link Filter.Type#OPERATOR_FILTER}
	 */
	@Override
	public Type getType() {
		return Type.OPERATOR_FILTER;
	}

	/**
	 * Returns the root {@link Operator} of the test expression.
	 * @return the root operator
	 */
	public Operator getOperator() {
		return rootOperator;
	}

	@Override
	public <T> boolean evaluate(T obj, XPathEvaluator<T> xpathEvaluator) throws FilterEvaluationException {
		return rootOperator.evaluate(obj, xpathEvaluator);
	}

	@Override
	public String toString() {
		return rootOperator.toString("");
	}

}
