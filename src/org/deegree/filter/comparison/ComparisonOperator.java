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

import java.math.BigDecimal;
import java.text.ParseException;

import org.deegree.commons.types.datetime.Date;
import org.deegree.commons.types.datetime.DateTime;
import org.deegree.commons.types.datetime.Time;
import org.deegree.commons.utils.Pair;
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

    public enum SubType {
        PROPERTY_IS_EQUAL_TO, PROPERTY_IS_NOT_EQUAL_TO, PROPERTY_IS_LESS_THAN, PROPERTY_IS_GREATER_THAN, PROPERTY_IS_LESS_THAN_OR_EQUAL_TO, PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO, PROPERTY_IS_LIKE, PROPERTY_IS_NULL, PROPERTY_IS_BETWEEN;
    }

    public Type getType() {
        return Type.COMPARISON;
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

    protected Pair<Object, Object> makeComparable( Object value1, Object value2 )
                            throws FilterEvaluationException {
        Pair<Object, Object> result = new Pair<Object, Object>( value1, value2 );
        if ( !( value1 instanceof String ) ) {
            if ( value1 instanceof Number ) {
                result = new Pair<Object, Object>( value1, new BigDecimal( value2.toString() ) );
            } else if ( value1 instanceof Date ) {
                try {
                    result = new Pair<Object, Object>( value1, new Date( value2.toString() ) );
                } catch ( ParseException e ) {
                    throw new FilterEvaluationException( e.getMessage() );
                }
            } else if ( value1 instanceof DateTime ) {
                try {
                    result = new Pair<Object, Object>( value1, new DateTime( value2.toString() ) );
                } catch ( ParseException e ) {
                    throw new FilterEvaluationException( e.getMessage() );
                }
            } else if ( value1 instanceof Time ) {
                try {
                    result = new Pair<Object, Object>( value1, new Time( value2.toString() ) );
                } catch ( ParseException e ) {
                    throw new FilterEvaluationException( e.getMessage() );
                }
            }
        } else if ( !( value2 instanceof String ) ) {
            if ( value2 instanceof Number ) {
                result = new Pair<Object, Object>( new BigDecimal( value1.toString() ), value2 );
            } else if ( value2 instanceof Date ) {
                try {
                    result = new Pair<Object, Object>( new Date( value1.toString() ), value2 );
                } catch ( ParseException e ) {
                    throw new FilterEvaluationException( e.getMessage() );
                }
            } else if ( value2 instanceof DateTime ) {
                try {
                    result = new Pair<Object, Object>( new DateTime( value1.toString() ), value2 );
                } catch ( ParseException e ) {
                    throw new FilterEvaluationException( e.getMessage() );
                }
            } else if ( value2 instanceof Time ) {
                try {
                    result = new Pair<Object, Object>( new Time( value1.toString() ), value2 );
                } catch ( ParseException e ) {
                    throw new FilterEvaluationException( e.getMessage() );
                }
            }
        }
        return result;
    }
}
