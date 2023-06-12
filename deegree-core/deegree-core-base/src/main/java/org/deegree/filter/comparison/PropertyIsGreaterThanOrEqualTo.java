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

import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.filter.Expression;
import org.deegree.filter.MatchAction;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class PropertyIsGreaterThanOrEqualTo extends BinaryComparisonOperator {

	public PropertyIsGreaterThanOrEqualTo(Expression param1, Expression param2, Boolean matchCase,
			MatchAction matchAction) {
		super(param1, param2, matchCase, matchAction);
	}

	@Override
	public SubType getSubType() {
		return SubType.PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO;
	}

	@Override
	protected boolean compare(PrimitiveValue param1, PrimitiveValue param2) {
		if ((param1).compareTo(param2) >= 0) {
			return true;
		}
		return false;
	}

	@Override
	public String toString(String indent) {
		String s = indent + "-PropertyIsGreaterThanOrEqualTo\n";
		s += param1.toString(indent + "  ");
		s += param2.toString(indent + "  ");
		return s;
	}

}
