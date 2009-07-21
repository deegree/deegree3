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
package org.deegree.filter.logical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchableObject;
import org.deegree.filter.Operator;

/**
 * The API for the Or logical operator. For the schema model, see 
 * http://schemas.opengis.net/filter/1.1.0/filter.xsd
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class Or extends LogicalOperator {

    private List<Operator> params;

    /**
     * Creates an Or operator by providing an arbitrary number of parameters
     * @param paramsArray    an arbitrary number of parameters
     * @throws Exception when less than 2 parameters are provided
     */
    public Or( Operator... paramsArray ) throws Exception {
        if ( paramsArray.length < 2 ) {
            throw new Exception( "Or operator must have at least 2 arguments" );
        }
            
        params = new ArrayList<Operator>( paramsArray.length );
        params = Arrays.asList( paramsArray );
    }

    /**  
     * @see org.deegree.filter.logical.LogicalOperator#getSubType() 
     */
    @Override
    public SubType getSubType() {
        return SubType.OR;
    }

    /**
     * Return the number of parameters in the Or operator
     * @return  the number of parameters
     */
    public int getSize() {
        return params.size();
    }

    /**
     * @param n the index of the wanted argument. Starting from 0.
     * @return  returns the nth parameter of the Or operator. In order to prevent the 
     * {@link IndexOutOfBoundsException} from occurring, one can call getSize() first and check... 
     */
    public Operator getParameter( int n ) {
        return params.get( n ); 
    }

    /**
     * @param object    the object that will be evaluated
     * @return a boolean value representing the result of the Or expression evaluation
     * @throws FilterEvaluationException    if the evaluation of the object fails
     */
    public boolean evaluate( MatchableObject object )
                            throws FilterEvaluationException {
        boolean partialRes = true;
        for ( int i = 0; i < getSize(); i++ ) {
            partialRes = partialRes && params.get( i ).evaluate( object );
        }
        return partialRes;
    }


    /**
     * @param indent    used to indent the output String
     * @return       an indented String representation of the expression 
     * (think of XML representation but without tags)
     */
    public String toString( String indent ) {
        String s = indent + "-Or\n";
        for ( int i = 0; i < getSize(); i++ ) {
            s += params.get( i ).toString( indent + "  " );
        }
        return s;
    }
}
