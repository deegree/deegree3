/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.style.utils;

import static org.deegree.style.se.parser.SymbologyParser.ELSEFILTER;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.utils.Pair;
import org.deegree.filter.Expression;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.logical.Or;
import org.deegree.style.se.parser.SymbologyParser.FilterContinuation;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.style.se.unevaluated.Symbolizer;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class Styles {

	public static OperatorFilter getStyleFilters(Style style, double scale) {
		OperatorFilter sldFilter = null;
		outer: if (style != null) {
			// the full use of generics here will defeat the compiler
			LinkedList<Pair> rules = (LinkedList) style.filter(scale).getRules();
			for (Pair p : rules) {
				if (p.first == null) {
					sldFilter = null;
					break outer;
				}
				if (p.first instanceof FilterContinuation) {
					FilterContinuation contn = (FilterContinuation) p.first;
					if (contn.filter == ELSEFILTER) {
						sldFilter = null;
						break outer;
					}
					if (contn.filter == null) {
						sldFilter = null;
						break outer;
					}
					if (sldFilter == null) {
						sldFilter = (OperatorFilter) contn.filter;
					}
					else {
						Operator op1 = sldFilter.getOperator();
						Operator op2 = ((OperatorFilter) contn.filter).getOperator();
						sldFilter = new OperatorFilter(new Or(op1, op2));
					}
				}
			}
		}

		return sldFilter;
	}

	public static List<Expression> getGeometryExpressions(Style style) {
		List<Expression> list = new ArrayList<Expression>();

		if (style == null) {
			return list;
		}

		// do not use full generics here, else compilation will fail
		// it's always fun to see how easy the compiler can be defeated...
		LinkedList<Pair> rules = (LinkedList) style.getRules();
		for (Pair rule : rules) {
			if (rule.first instanceof FilterContinuation) {
				for (Symbolizer<?> s : ((FilterContinuation) rule.first).getSymbolizers()) {
					Expression expr = s.getGeometryExpression();
					if (expr != null && !list.contains(expr)) {
						list.add(expr);
					}
				}
			}
		}

		return list;
	}

}
