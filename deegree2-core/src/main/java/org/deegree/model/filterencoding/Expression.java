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
package org.deegree.model.filterencoding;

import org.deegree.model.feature.Feature;
import org.w3c.dom.Element;

/**
 * Abstract superclass representing expr-entities (as defined in the Expression DTD).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
abstract public class Expression {

    /**
     * The underlying expression's id.
     *
     * @see ExpressionDefines
     */
    protected int id;

    /**
     * Given a DOM-fragment, a corresponding Expression-object is built. This method recursively calls other
     * buildFromDOM () - methods to validate the structure of the DOM-fragment.
     *
     * @param element
     * @return the Bean of the DOM
     *
     * @throws FilterConstructionException
     *             if the structure of the DOM-fragment is invalid
     */
    public static Expression buildFromDOM( Element element )
                            throws FilterConstructionException {

        // check if root element's name is a known expression
        String name = element.getLocalName();
        int id = ExpressionDefines.getIdByName( name );
        Expression expression = null;

        switch ( id ) {
        case ExpressionDefines.EXPRESSION: {
            break;
        }
        case ExpressionDefines.PROPERTYNAME: {
            expression = PropertyName.buildFromDOM( element );
            break;
        }
        case ExpressionDefines.LITERAL: {
            expression = Literal.buildFromDOM( element );
            break;
        }
        case ExpressionDefines.FUNCTION: {
            expression = Function.buildFromDOM( element );
            break;
        }
        case ExpressionDefines.ADD:
        case ExpressionDefines.SUB:
        case ExpressionDefines.MUL:
        case ExpressionDefines.DIV: {
            expression = ArithmeticExpression.buildFromDOM( element );
            break;
        }
        default: {
            throw new FilterConstructionException( "Unknown expression '" + name + "'!" );
        }
        }
        return expression;
    }

    /** @return the name of the expression. */
    public String getExpressionName() {
        return ExpressionDefines.getNameById( id );
    }

    /**
     * @return the expression's id.
     *
     * @see ExpressionDefines
     */
    public int getExpressionId() {
        return this.id;
    }

    /**
     * Calculates the <tt>Expression</tt>'s value based on the certain property values of the given feature.
     *
     * @param feature
     *            that determines the values of <tt>PropertyNames</tt> in the expression
     * @return the resulting Object
     * @throws FilterEvaluationException
     *             if the evaluation fails
     */
    public abstract Object evaluate( Feature feature )
                            throws FilterEvaluationException;

    /**
     * Produces an XML representation of this object.
     *
     * @return an XML representation of this object
     */
    public abstract StringBuffer toXML();
}
