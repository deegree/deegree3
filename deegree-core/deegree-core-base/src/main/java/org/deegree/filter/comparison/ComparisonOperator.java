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

import java.util.List;

import org.deegree.commons.tom.ElementNode;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchAction;
import org.deegree.filter.Operator;
import org.deegree.filter.i18n.Messages;

/**
 * Abstract base class for all comparison operators.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public abstract class ComparisonOperator implements Operator {

	protected final Boolean matchCase;

	protected final MatchAction matchAction;

	protected ComparisonOperator(Boolean matchCase, MatchAction matchAction) {
		this.matchCase = matchCase;
		this.matchAction = matchAction;
	}

	public enum SubType {

		PROPERTY_IS_EQUAL_TO, PROPERTY_IS_NOT_EQUAL_TO, PROPERTY_IS_LESS_THAN, PROPERTY_IS_GREATER_THAN,
		PROPERTY_IS_LESS_THAN_OR_EQUAL_TO, PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO, PROPERTY_IS_LIKE, PROPERTY_IS_NULL,
		PROPERTY_IS_NIL, PROPERTY_IS_BETWEEN

	}

	public Type getType() {
		return Type.COMPARISON;
	}

	public Boolean isMatchCase() {
		return matchCase;
	}

	public MatchAction getMatchAction() {
		return matchAction;
	}

	public abstract SubType getSubType();

	/**
	 * Performs a checked cast to {@link Comparable}. If the given value is neither null
	 * nor a {@link Comparable} instance, a corresponding
	 * {@link FilterEvaluationException} is thrown.
	 * @param value
	 * @return the very same value (if it is a {@link Comparable} or <code>null</code>)
	 * @throws FilterEvaluationException if the value is neither <code>null</code> nor a
	 * {@link Comparable}
	 */
	protected Comparable<?> checkComparableOrNull(Object value) throws FilterEvaluationException {
		if (value != null && !(value instanceof Comparable<?>)) {
			String msg = Messages.getMessage("FILTER_EVALUATION_NOT_COMPARABLE", this.getType().name(), value);
			throw new FilterEvaluationException(msg);
		}
		return (Comparable<?>) value;
	}

	/**
	 * Creates a pair of {@link PrimitiveValue} instances from the given
	 * {@link TypedObjectNode}s.
	 * @param node1 first node, can be <code>null</code>
	 * @param node2 second node, can be <code>null</code>
	 * @return pair of primitive values, never <code>null</code> (and values not null)
	 * @throws FilterEvaluationException
	 */
	protected Pair<PrimitiveValue, PrimitiveValue> getPrimitiveValues(TypedObjectNode node1, TypedObjectNode node2)
			throws FilterEvaluationException {
		PrimitiveValue primitive1 = getPrimitiveValue(node1);
		PrimitiveValue primitive2 = getPrimitiveValue(node2);
		return new Pair<PrimitiveValue, PrimitiveValue>(primitive1, primitive2);
	}

	private PrimitiveValue getPrimitiveValue(TypedObjectNode node) {
		if (node == null) {
			return new PrimitiveValue("null");
		}
		if (node instanceof PrimitiveValue) {
			return (PrimitiveValue) node;
		}
		if (node instanceof Property) {
			return getPrimitiveValue(((Property) node).getValue());
		}
		if (node instanceof ElementNode) {
			ElementNode elNode = (ElementNode) node;
			List<TypedObjectNode> children = elNode.getChildren();
			if (children == null || children.isEmpty()) {
				return new PrimitiveValue("null");
			}
			return getPrimitiveValue(children.get(0));
		}
		return new PrimitiveValue(node.toString());
	}

	public abstract Expression[] getParams();

}
