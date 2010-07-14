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
package org.deegree.filter.function.other;

import static org.deegree.commons.utils.math.MathUtils.round;

import java.math.BigInteger;
import java.util.List;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchableObject;
import org.deegree.filter.expression.Function;

/**
 * <code>IMod</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class IMod extends Function {

    /**
     * @param exprs
     */
    public IMod( List<Expression> exprs ) {
        super( "imod", exprs );
    }

    static Pair<Integer, Integer> extractValues( Expression first, Expression second, MatchableObject f )
                            throws FilterEvaluationException {
        TypedObjectNode[] vals1 = first.evaluate( f );
        TypedObjectNode[] vals2 = second.evaluate( f );

        PrimitiveValue pv1;
        PrimitiveValue pv2;
        if ( vals1[0] instanceof PrimitiveValue ) {
            pv1 = (PrimitiveValue) vals1[0];
        } else {
            pv1 = ( (SimpleProperty) vals1[0] ).getValue();
        }
        if ( vals2[0] instanceof PrimitiveValue ) {
            pv2 = (PrimitiveValue) vals2[0];
        } else {
            pv2 = ( (SimpleProperty) vals2[0] ).getValue();
        }

        return new Pair<Integer, Integer>( round( Double.valueOf( pv1.getValue().toString() ) ),
                                           round( Double.valueOf( pv2.getValue().toString() ) ) );
    }

    @Override
    public TypedObjectNode[] evaluate( MatchableObject f )
                            throws FilterEvaluationException {
        Pair<Integer, Integer> p = extractValues( getParams()[0], getParams()[1], f );

        return new TypedObjectNode[] { new PrimitiveValue( BigInteger.valueOf( p.first % p.second ) ) };
    }

}
