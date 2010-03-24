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
package org.deegree.filter;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.filter.expression.Add;
import org.deegree.filter.expression.Div;
import org.deegree.filter.expression.Function;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.Mul;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.expression.Sub;

/**
 * An <code>Expression</code> describes a rule to obtain a value that may be derived from the properties of a
 * {@link MatchableObject}.
 * <p>
 * Note that the objects returned by {@link #evaluate(MatchableObject)} is an <code>TypedObjectNode[]</code>, as an
 * expression may evaluate to multiple values, e.g. a {@link PropertyName} that targets a multi property of a feature.
 * Values in the returned array may also be <code>null</code>.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface Expression {

    /**
     * Convenience enum type for discriminating the different expression types.
     */
    public enum Type {
        /** Value is computed by adding two values. The {@link Expression} is an instance of {@link Add}. */
        ADD,
        /** Value is computed by subtracting two values. The {@link Expression} is an instance of {@link Sub}. */
        SUB,
        /** Value is computed by multipliying two values. The {@link Expression} is an instance of {@link Mul}. */
        MUL,
        /** Value is computed by dividing two values. The {@link Expression} is an instance of {@link Div}. */
        DIV,
        /**
         * Expression references a property of a {@link MatchableObject}. The {@link Expression} is an instance of
         * {@link PropertyName}.
         */
        PROPERTY_NAME,
        /**
         * Value is given as a literal. The {@link Expression} is an instance of {@link Literal}.
         */
        LITERAL,
        /**
         * Value is given as a function. The {@link Expression} is an instance of {@link Function}.
         */
        FUNCTION;
    }

    /**
     * Returns the type of expression. Use this to safely determine the subtype of {@link Expression}.
     * 
     * @return type of expression
     */
    public Type getType();

    /**
     * Determines the values of the expression for the given {@link MatchableObject}.
     * <p>
     * Note that this returns an <code>TypedObjectNode[]</code>, as an expression may evaluate to multiple values, e.g.
     * a {@link PropertyName} that targets a multi property of a feature.
     * </p>
     * 
     * @param obj
     *            object that the expression is evaluated upon
     * @return the values of the expression, may be empty (and even contain <code>null</code> values), but never
     *         <code>null</code>
     * @throws FilterEvaluationException
     */
    public TypedObjectNode[] evaluate( MatchableObject obj )
                            throws FilterEvaluationException;

    /**
     * Returns the expression's paramters.
     * 
     * @return the parameters of the expression
     */
    public abstract Expression[] getParams();

    public String toString( String indent );
}
