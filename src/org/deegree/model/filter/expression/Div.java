//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.filter.expression;

import org.deegree.model.filter.Expression;
import org.deegree.model.filter.FilterEvaluationException;
import org.deegree.model.filter.MatchableObject;
import org.deegree.model.generic.DeegreeObject;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class Div implements Expression {

    private Expression param1;

    private Expression param2;

    public Div( Expression param1, Expression param2 ) {
        this.param1 = param1;
        this.param2 = param2;
    }

    public Type getType() {
        return Type.DIV;
    }

    public Expression getParameter1() {
        return param1;
    }

    public Expression getParameter2() {
        return param2;
    }

    public Double evaluate( MatchableObject obj )
                            throws FilterEvaluationException {
        Object value1 = param1.evaluate( obj );
        Object value2 = param2.evaluate( obj );
        if ( !( value1 instanceof Number ) ) {
            String msg = "Cannot evaluate '" + getType().name() + "' expression on '" + value1
                         + "'. This is only possible for numerical values.";
            throw new FilterEvaluationException( msg );
        }
        if ( !( value2 instanceof Number ) ) {
            String msg = "Cannot evaluate '" + getType().name() + "' expression on '" + value2
                         + "'. This is only possible for numerical values.";
            throw new FilterEvaluationException( msg );
        }
        return ( (Number) value1 ).doubleValue() / ( (Number) value2 ).doubleValue();
    }

    public String toString( String indent ) {
        String s = indent + "-Div\n";
        s += param1.toString( indent + "  " );
        s += param2.toString( indent + "  " );
        return s;
    }
}
