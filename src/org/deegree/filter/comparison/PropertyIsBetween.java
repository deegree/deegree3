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
public class PropertyIsBetween extends ComparisonOperator {

    private final Expression upperBoundary;

    private final Expression lowerBoundary;

    private final Expression expression;

    public PropertyIsBetween( Expression expression, Expression lowerBoundary, Expression upperBoundary ) {
        this.expression = expression;
        this.lowerBoundary = lowerBoundary;
        this.upperBoundary = upperBoundary;
    }

    public Expression getExpression() {
        return expression;
    }

    /**
     * @return the upperBoundary
     */
    public Expression getUpperBoundary() {
        return upperBoundary;
    }

    /**
     * @return the lowerBoundary
     */
    public Expression getLowerBoundary() {
        return lowerBoundary;
    }

    @Override
    public SubType getSubType() {
        return SubType.PROPERTY_IS_BETWEEN;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean evaluate( MatchableObject obj )
                            throws FilterEvaluationException {

        Object[] propertyValues = expression.evaluate( obj );
        Object[] upperBoundaryValues = upperBoundary.evaluate( obj );
        Object[] lowerBoundaryValues = lowerBoundary.evaluate( obj );

        for ( Object propertyValue : propertyValues ) {
            // check for one upper value that is larger than the propertyValue
            if ( propertyValue != null ) {
                for ( Object upperValue : upperBoundaryValues ) {
                    if ( upperValue != null ) {
                        Pair<Object, Object> propUpper = makeComparable( propertyValue, upperValue );
                        if ( ( (Comparable<Object>) propUpper.first ).compareTo( propUpper.second ) <= 0 ) {
                            // now check for one lower value that is smaller than the propertyValue
                            for ( Object lowerValue : lowerBoundaryValues ) {
                                if ( lowerValue != null ) {
                                    Pair<Object, Object> propLower = makeComparable( propertyValue, lowerValue );
                                    if ( ( (Comparable<Object>) propLower.first ).compareTo( propLower.second ) >= 0 ) {
                                        return true;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String toString( String indent ) {
        String s = indent + "-PropertyIsBetween\n";
        s += lowerBoundary.toString( indent + "  " );
        s += expression.toString( indent + "  " );
        s += upperBoundary.toString( indent + "  " );
        return s;
    }
    
    public Expression[] getParams () {
        return new Expression [] {lowerBoundary, expression, upperBoundary};
    }
}
