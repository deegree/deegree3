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

import static org.deegree.filter.comparison.ComparisonOperator.SubType.PROPERTY_IS_NIL;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.ElementNode;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchAction;
import org.deegree.filter.XPathEvaluator;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class PropertyIsNil extends ComparisonOperator {

	private final static QName XSI_NIL = new QName(CommonNamespaces.XSINS, "nil");

	private final Expression propName;

	private final String nilReason;

	public PropertyIsNil(Expression propName, String nilReason, MatchAction matchAction) {
		super(true, matchAction);
		this.propName = propName;
		this.nilReason = nilReason;
	}

	public Expression getPropertyName() {
		return propName;
	}

	@Override
	public SubType getSubType() {
		return PROPERTY_IS_NIL;
	}

	@Override
	public <T> boolean evaluate(T obj, XPathEvaluator<T> xpathEvaluator) throws FilterEvaluationException {

		TypedObjectNode[] paramValues = propName.evaluate(obj, xpathEvaluator);
		if (paramValues.length == 0) {
			return true;
		}
		for (TypedObjectNode value : paramValues) {
			if (!(value instanceof ElementNode)) {
				return false;
			}
			PrimitiveValue nil = ((ElementNode) value).getAttributes().get(XSI_NIL);
			if (nil != null && nil.getValue() instanceof Boolean) {
				if ((Boolean) nil.getValue()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString(String indent) {
		String s = indent + "-PropertyIsNil\n";
		s += propName.toString(indent + "  ");
		return s;
	}

	@Override
	public Expression[] getParams() {
		return new Expression[] { propName };
	}

	public String getNilReason() {
		return nilReason;
	}

}
