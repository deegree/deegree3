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
package org.deegree.filter.comparison;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchAction;
import org.deegree.filter.XPathEvaluator;

/**
 * Abstract base class for all binary comparison operators.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public abstract class BinaryComparisonOperator extends ComparisonOperator {

	protected final Expression param1;

	protected final Expression param2;

	protected BinaryComparisonOperator(Expression param1, Expression param2, Boolean matchCase,
			MatchAction matchAction) {
		super(matchCase, matchAction);
		this.param1 = param1;
		this.param2 = param2;
	}

	public Expression getParameter1() {
		return param1;
	}

	public Expression getParameter2() {
		return param2;
	}

	@Override
	public Expression[] getParams() {
		return new Expression[] { param1, param2 };
	}

	@Override
	public <T> boolean evaluate(T obj, XPathEvaluator<T> xpathEvaluator) throws FilterEvaluationException {

		TypedObjectNode[] param1Values = param1.evaluate(obj, xpathEvaluator);
		TypedObjectNode[] param2Values = param2.evaluate(obj, xpathEvaluator);

		MatchAction ma = matchAction != null ? matchAction : MatchAction.ANY;
		switch (ma) {
			case ANY: {
				// evaluate to true if at least one pair of values matches the condition
				for (TypedObjectNode value1 : param1Values) {
					if (value1 != null) {
						for (TypedObjectNode value2 : param2Values) {
							if (value2 != null) {
								Pair<PrimitiveValue, PrimitiveValue> primitivePair = getPrimitiveValues(value1, value2);
								if (compare(primitivePair.first, primitivePair.second)) {
									return true;
								}
							}
						}
					}
				}
				break;
			}
			case ALL: {
				// evaluate to true if every value from A has a counterpart in B
				if (param1Values.length == 0) {
					return false;
				}
				for (TypedObjectNode value1 : param1Values) {
					if (value1 != null) {
						boolean foundMatch = false;
						for (TypedObjectNode value2 : param2Values) {
							if (value2 != null) {
								Pair<PrimitiveValue, PrimitiveValue> primitivePair = getPrimitiveValues(value1, value2);
								if (compare(primitivePair.first, primitivePair.second)) {
									foundMatch = true;
								}
							}
							if (foundMatch) {
								break;
							}
						}
						if (!foundMatch) {
							return false;
						}
					}
				}
				return true;
			}
			case ONE: {
				// evaluate to true if exactly one value from A has exactly one
				// counterpart in B
				boolean foundMatch = false;
				for (TypedObjectNode value1 : param1Values) {
					if (value1 != null) {
						for (TypedObjectNode value2 : param2Values) {
							if (value2 != null) {
								Pair<PrimitiveValue, PrimitiveValue> primitivePair = getPrimitiveValues(value1, value2);
								if (compare(primitivePair.first, primitivePair.second)) {
									if (foundMatch) {
										// found a second match
										return false;
									}
									foundMatch = true;
								}
							}
						}
					}
				}
				if (!foundMatch) {
					return false;
				}
				return true;
			}
		}

		return false;
	}

	protected abstract boolean compare(PrimitiveValue param1, PrimitiveValue param2);

}
