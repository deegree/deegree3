//$HeadURL$
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
package org.deegree.rendering.r2d.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.filter.Expression;
import org.deegree.rendering.r2d.se.parser.SymbologyParser.FilterContinuation;
import org.deegree.rendering.r2d.se.unevaluated.Continuation;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.rendering.r2d.se.unevaluated.Symbolizer;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Styles {

    public static List<Expression> getGeometryExpressions( Style style ) {
        List<Expression> list = new ArrayList<Expression>();

        LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>> rules = style.getRules();
        for ( Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair> rule : rules ) {
            if ( rule.first instanceof FilterContinuation ) {
                for ( Symbolizer<?> s : ( (FilterContinuation) rule.first ).getSymbolizers() ) {
                    Expression expr = s.getGeometryExpression();
                    if ( expr != null && !list.contains( expr ) ) {
                        list.add( expr );
                    }
                }
            }
        }

        return list;
    }

}
