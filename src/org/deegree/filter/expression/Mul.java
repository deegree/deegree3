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
public class Mul implements Expression {

    private Expression param1;

    private Expression param2;

    public Mul( Expression param1, Expression param2 ) {
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
        return Type.MUL;
    }    
    
    @Override    
    public Object[] evaluate( MatchableObject obj )
                            throws FilterEvaluationException {

        Object[] values1 = param1.evaluate( obj );
        Object[] values2 = param2.evaluate( obj );
        
        Object [] resultValues = new Object [values1.length * values2.length];
        int i = 0;
        for ( Object value1 : values1 ) {
            if ( !( value1 instanceof BigDecimal ) ) {
                value1 = new BigDecimal( value1.toString() );
            }
            for ( Object value2 : values2 ) {
                if ( !( value2 instanceof BigDecimal ) ) {
                    value2 = new BigDecimal( value2.toString() );
                }
                resultValues[i++] = ( (BigDecimal) value1 ).multiply( ( (BigDecimal) value2 ));
            }
        }
        return resultValues;
    }

    @Override    
    public String toString( String indent ) {
        String s = indent + "-Mul\n";
        s += param1.toString( indent + "  " );
        s += param2.toString( indent + "  " );
        return s;
    }

    @Override
    public Expression[] getParams() {
        return new Expression[] { param1, param2 };
    }    
}
