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
import org.deegree.feature.property.Property;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.Operator;
import org.deegree.filter.i18n.Messages;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public abstract class ComparisonOperator implements Operator {

    protected boolean matchCase;

    protected ComparisonOperator( boolean matchCase ) {
        this.matchCase = matchCase;
    }

    public enum SubType {
        PROPERTY_IS_EQUAL_TO, PROPERTY_IS_NOT_EQUAL_TO, PROPERTY_IS_LESS_THAN, PROPERTY_IS_GREATER_THAN, PROPERTY_IS_LESS_THAN_OR_EQUAL_TO, PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO, PROPERTY_IS_LIKE, PROPERTY_IS_NULL, PROPERTY_IS_BETWEEN;
    }

    public Type getType() {
        return Type.COMPARISON;
    }

    public boolean getMatchCase() {
        return matchCase;
    }

    public abstract SubType getSubType();

    /**
     * Performs a checked cast to {@link Comparable}. If the given value is neither null nor a {@link Comparable}
     * instance, a corresponding {@link FilterEvaluationException} is thrown.
     * 
     * @param value
     * @return the very same value (if it is a {@link Comparable} or <code>null</code>)
     * @throws FilterEvaluationException
     *             if the value is neither <code>null</code> nor a {@link Comparable}
     */
    protected Comparable<Object> checkComparableOrNull( Object value )
                            throws FilterEvaluationException {
        if ( value != null && !( value instanceof Comparable<?> ) ) {
            String msg = Messages.getMessage( "FILTER_EVALUATION_NOT_COMPARABLE", this.getType().name(), value );
            throw new FilterEvaluationException( msg );
        }
        return (Comparable<Object>) value;
    }

    /**
     * Creates a pair of {@link PrimitiveValue} instances from the given {@link TypedObjectNode} while trying to
     * preserve primitive type information.
     * 
     * @param value1
     * @param value2
     * @return
     * @throws FilterEvaluationException
     */
    protected Pair<PrimitiveValue, PrimitiveValue> getPrimitives( TypedObjectNode value1, TypedObjectNode value2 )
                            throws FilterEvaluationException {

        if ( value1 instanceof Property ) {
            value1 = ( (Property) value1 ).getValue();
        }
        if ( value2 instanceof Property ) {
            value2 = ( (Property) value2 ).getValue();
        }

        Pair<PrimitiveValue, PrimitiveValue> result = null;
        if ( value1 instanceof PrimitiveValue ) {
            result = getPrimitivePair( (PrimitiveValue) value1, value2 );
        } else if ( value2 instanceof PrimitiveValue ) {
            Pair<PrimitiveValue, PrimitiveValue> switched = getPrimitivePair( (PrimitiveValue) value2, value1 );
            result = new Pair<PrimitiveValue, PrimitiveValue>( switched.second, switched.first );
        } else {
            PrimitiveValue primitive1 = new PrimitiveValue( value1.toString() );
            PrimitiveValue primitive2 = new PrimitiveValue( value2.toString() );
            result = new Pair<PrimitiveValue, PrimitiveValue>( primitive1, primitive2 );
        }
        return result;
    }

    private Pair<PrimitiveValue, PrimitiveValue> getPrimitivePair( PrimitiveValue value1, TypedObjectNode value2 ) {
        PrimitiveValue pValue2 = null;
        if ( value2 instanceof PrimitiveValue ) {
            pValue2 = (PrimitiveValue) value2;
        } else {
            pValue2 = new PrimitiveValue( value2.toString() );
        }
        return new Pair<PrimitiveValue, PrimitiveValue>( value1, pValue2 );
    }

    public abstract Expression[] getParams();
}
