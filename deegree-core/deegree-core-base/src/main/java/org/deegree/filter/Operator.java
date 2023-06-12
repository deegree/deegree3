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

import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.spatial.SpatialOperator;

/**
 * {@link Operator} instances are predicates and the building blocks of
 * {@link OperatorFilter}s. They may be nested recursively -- an argument of an
 * {@link Operator} can be another {@link Operator} so they form a tree structure that can
 * be evaluated by traversing it.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public interface Operator {

	/**
	 * Convenience enum type for discriminating the different operator types.
	 */
	public enum Type {

		/**
		 * Spatial operator. The {@link Operator} is an instance of
		 * {@link SpatialOperator}.
		 */
		SPATIAL,
		/**
		 * Logical operator. The {@link Operator} is an instance of
		 * {@link LogicalOperator}.
		 */
		LOGICAL,
		/**
		 * Comparison operator. The {@link Operator} is an instance of
		 * {@link ComparisonOperator}.
		 */
		COMPARISON,
		/**
		 * Temporal operator. The {@link Operator} is an instance of
		 * {@link TemporalOperator}.
		 */
		TEMPORAL;

	}

	/**
	 * Returns the type of operator. Use this to safely determine the subtype of
	 * {@link Operator}.
	 * @return type of operator
	 */
	public Type getType();

	/**
	 * Determines the value of the boolean operator.
	 * @param <T> type of the context object
	 * @param obj object that the operator is evaluated upon, must not be
	 * <code>null</code>
	 * @param xpathEvaluator used for evaluation of XPath expressions, must not be
	 * <code>null</code>
	 * @return true, if the operator evaluates to true, false otherwise
	 * @throws FilterEvaluationException if the evaluation fails
	 */
	public <T> boolean evaluate(T obj, XPathEvaluator<T> xpathEvaluator) throws FilterEvaluationException;

	public String toString(String indent);

}