//$HeadURL$
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchableObject;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class Sub implements Expression {

    private Expression param1;

    private Expression param2;

    public Sub( Expression param1, Expression param2 ) {
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
    public Type getType() {
        return Type.SUB;
    }

    @Override
    public TypedObjectNode[] evaluate( MatchableObject obj )
                            throws FilterEvaluationException {
        TypedObjectNode[] values1 = param1.evaluate( obj );
        TypedObjectNode[] values2 = param2.evaluate( obj );

        List<TypedObjectNode> resultValues = new ArrayList<TypedObjectNode>( values1.length * values2.length );
        for ( TypedObjectNode value1 : values1 ) {
            if ( value1 != null ) {
                try {
                    BigDecimal bd1 = new BigDecimal( value1.toString() );
                    for ( TypedObjectNode value2 : values2 ) {
                        if ( value2 != null ) {
                            BigDecimal bd2 = new BigDecimal( value2.toString() );
                            resultValues.add( new PrimitiveValue( bd1.subtract( bd2 ) ) );
                        }
                    }
                } catch ( NumberFormatException e ) {
                    // nothing to do
                }
            }
        }
        return resultValues.toArray( new TypedObjectNode[resultValues.size()] );
    }

    @Override
    public String toString( String indent ) {
        String s = indent + "-Sub\n";
        s += param1.toString( indent + "  " );
        s += param2.toString( indent + "  " );
        return s;
    }

    @Override
    public Expression[] getParams() {
        return new Expression[] { param1, param2 };
    }
}
