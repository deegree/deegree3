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
package org.deegree.filter.comparison;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
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
public class PropertyIsNotEqualTo extends BinaryComparisonOperator {

    public PropertyIsNotEqualTo( Expression param1, Expression param2, boolean matchCase ) {
        super( param1, param2, matchCase );
    }

    @Override
    public SubType getSubType() {
        return SubType.PROPERTY_IS_NOT_EQUAL_TO;
    }

    @Override
    public boolean evaluate( MatchableObject object )
                            throws FilterEvaluationException {

        TypedObjectNode[] param1Values = param1.evaluate( object );
        TypedObjectNode[] param2Values = param2.evaluate( object );

        // evaluate to true if at least one pair of values matches the condition
        for ( TypedObjectNode value1 : param1Values ) {
            for ( TypedObjectNode value2 : param2Values ) {
                if ( value1 == null && value2 == null ) {
                    return true;
                }
                if ( value1 != null && value2 != null ) {
                    Pair<PrimitiveValue, PrimitiveValue> comparablePair = getPrimitives( value1, value2 );
                    if ( !matchCase ) {
                        if ( !comparablePair.first.toString().equalsIgnoreCase( comparablePair.second.toString() ) ) {
                            return true;
                        }
                    } else {
                        if ( !comparablePair.first.equals( comparablePair.second ) ) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String toString( String indent ) {
        String s = indent + "-PropertyIsNotEqualTo\n";
        s += param1.toString( indent + "  " );
        s += param2.toString( indent + "  " );
        return s;
    }
}
