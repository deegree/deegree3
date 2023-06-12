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
package org.deegree.filter.expression;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.filter.Expression;
import org.deegree.filter.XPathEvaluator;

/**
 * {@link Expression} that has a constant value.
 *
 * @param <V> type of the contained value, in most cases {@link PrimitiveValue}
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class Literal<V extends TypedObjectNode> implements Expression {

	private final V value;

	private final QName type;

	@SuppressWarnings("unchecked")
	public Literal(String value) {
		this.value = (V) new PrimitiveValue(value);
		this.type = null;
	}

	/**
	 * Creates a new {@link Literal} instance.
	 * @param value value of the literal
	 * @param type name of the type, can be <code>null</code>
	 */
	public Literal(V value, QName type) {
		this.value = value;
		this.type = type;
	}

	/**
	 * Returns the literal's value.
	 * @return the literal's value
	 */
	public V getValue() {
		return value;
	}

	/**
	 * Returns the name of the value type.
	 * @return the name of the value type, can be <code>null</code> (no explicit type
	 * information available)
	 */
	public QName getTypeName() {
		return type;
	}

	@Override
	public Type getType() {
		return Type.LITERAL;
	}

	@Override
	public <T> TypedObjectNode[] evaluate(T obj, XPathEvaluator<T> xpathEvaluator) {
		return new TypedObjectNode[] { value };
	}

	@Override
	public String toString(String indent) {
		String s = indent + "-Literal ('" + value + "')\n";
		return s;
	}

	@Override
	public Expression[] getParams() {
		return new Expression[0];
	}

}
