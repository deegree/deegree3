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
package org.deegree.filter.temporal;

import java.util.List;

import org.deegree.commons.tom.ElementNode;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.Operator;
import org.deegree.filter.XPathEvaluator;
import org.deegree.time.primitive.TimeGeometricPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a predicate based on temporal relationships between two time-valued arguments.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public abstract class TemporalOperator implements Operator {

	private static final Logger LOG = LoggerFactory.getLogger(TemporalOperator.class);

	protected final Expression param1;

	protected final Expression param2;

	protected TemporalOperator(Expression param1, Expression param2) {
		this.param1 = param1;
		this.param2 = param2;
	}

	/**
	 * Convenience enum type for discriminating the different {@link TemporalOperator}
	 * types.
	 */
	public enum SubType {

		AFTER, ANYINTERACTS, BEFORE, BEGINS, BEGUNBY, DURING, ENDS, ENDEDBY, MEETS, METBY, OVERLAPPEDBY, TEQUALS,
		TCONTAINS, TOVERLAPS

	}

	/**
	 * Always returns {@link Operator.Type#TEMPORAL} (for {@link TemporalOperator}
	 * instances).
	 * @return {@link Operator.Type#TEMPORAL}
	 */
	@Override
	public Type getType() {
		return Type.TEMPORAL;
	}

	/**
	 * Returns the type of spatial operator. Use this to safely determine the subtype of
	 * {@link TemporalOperator}.
	 * @return type of spatial operator
	 */
	public SubType getSubType() {
		return SubType.valueOf(getClass().getSimpleName().toUpperCase());
	}

	public Expression getParameter1() {
		return param1;
	}

	public Expression getParameter2() {
		return param2;
	}

	@Override
	public String toString(String indent) {
		String s = indent + "-" + getSubType() + "\n";
		s += param1.toString(indent + "  ");
		s += param2.toString(indent + "  ");
		return s;
	}

	@Override
	public <T> boolean evaluate(T obj, XPathEvaluator<T> xpathEvaluator) throws FilterEvaluationException {
		final TypedObjectNode[] param1Values = param1.evaluate(obj, xpathEvaluator);
		final TypedObjectNode[] param2Values = param2.evaluate(obj, xpathEvaluator);
		if (param1Values.length == 1 && param2Values.length == 1) {
			final TimeGeometricPrimitive t1 = getTimePrimitiveValue(param1Values[0]);
			final TimeGeometricPrimitive t2 = getTimePrimitiveValue(param2Values[0]);
			return evaluate(t1, t2);
		}
		return false;
	}

	protected boolean evaluate(TimeGeometricPrimitive t1, TimeGeometricPrimitive t2) {
		throw new UnsupportedOperationException("Evaluation of operator " + getSubType() + " is not implemented yet.");
	}

	private TimeGeometricPrimitive getTimePrimitiveValue(final TypedObjectNode node) {
		if (node == null) {
			return null;
		}
		if (node instanceof TimeGeometricPrimitive) {
			return (TimeGeometricPrimitive) node;
		}
		if (node instanceof Property) {
			return getTimePrimitiveValue(((Property) node).getValue());
		}
		if (node instanceof ElementNode) {
			final ElementNode elNode = (ElementNode) node;
			final List<TypedObjectNode> children = elNode.getChildren();
			if (children == null || children.isEmpty()) {
				return null;
			}
			return getTimePrimitiveValue(children.get(0));
		}
		return null;
	}

}
