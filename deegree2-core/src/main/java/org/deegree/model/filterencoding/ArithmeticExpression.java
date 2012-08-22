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

import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.feature.Feature;
import org.w3c.dom.Element;

/**
 * Encapsulates the information of a &lt;Add&gt; / &lt;Sub&gt;/ &lt;Mul&gt; or &lt;DIV&gt; element
 * as defined in the Expression DTD.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ArithmeticExpression extends Expression {

    /**
     * The first operand.
     *
     */
    Expression expr1;

    /**
     * The second operand.
     *
     */
    Expression expr2;

    /**
     * Constructs a new ArithmeticExpression.
     *
     * @param id
     * @param expr1
     * @param expr2
     */
    public ArithmeticExpression( int id, Expression expr1, Expression expr2 ) {
        this.id = id;
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    /**
     * Given a DOM-fragment, a corresponding Expression-object is built. This method recursively
     * calls other buildFromDOM () - methods to validate the structure of the DOM-fragment.
     *
     * @param element
     * @return the new instance
     *
     * @throws FilterConstructionException
     *             if the structure of the DOM-fragment is invalid
     */
    public static Expression buildFromDOM( Element element )
                            throws FilterConstructionException {

        // check if root element's name is 'Add' / 'Sub' / 'Mul' or 'Div'
        String name = element.getLocalName();
        int id = ExpressionDefines.getIdByName( name );
        switch ( id ) {
        case ExpressionDefines.ADD:
        case ExpressionDefines.SUB:
        case ExpressionDefines.MUL:
        case ExpressionDefines.DIV: {
            break;
        }
        default: {
            throw new FilterConstructionException( "Element's name does not match 'Add' / 'Sub' / 'Mul' or 'Div'!" );
        }
        }

        // determine the arguments
        ElementList children = XMLTools.getChildElements( element );
        if ( children.getLength() != 2 )
            throw new FilterConstructionException( "'" + name + "' requires exactly 2 elements!" );

        Expression expr1 = Expression.buildFromDOM( children.item( 0 ) );
        Expression expr2 = Expression.buildFromDOM( children.item( 1 ) );

        return new ArithmeticExpression( id, expr1, expr2 );
    }

    /**
     * Produces an indented XML representation of this object.
     *
     * @return XML representation of this object.
     */
    @Override
    public StringBuffer toXML() {
        StringBuffer sb = new StringBuffer();

        sb.append( "<ogc:" ).append( getExpressionName() ).append( ">" );
        sb.append( expr1.toXML() );
        sb.append( expr2.toXML() );
        sb.append( "</ogc:" ).append( getExpressionName() ).append( ">" );
        return sb;
    }

    /**
     * Returns this <tt>ArithmeticExpression/tt>'s value (to be used in the
     * evaluation of complex <tt>Expression</tt>s).
     * TODO: Improve datatype handling.
     * @param feature that determines the concrete values of
     *                <tt>PropertyNames</tt> in the expression
     * @return the resulting value (as <tt>Double</tt>)
     * @throws FilterEvaluationException if the expressions are not numerical
     */
    @Override
    public Object evaluate( Feature feature )
                            throws FilterEvaluationException {

        Object o1 = expr1.evaluate( feature );
        Object o2 = expr2.evaluate( feature );

        if ( !( o1 instanceof Number && o2 instanceof Number ) ) {
            throw new FilterEvaluationException( "ADD/SUB/DIV/MUL may only be applied to numerical expressions." );
        }
        double d1 = ( (Number) o1 ).doubleValue();
        double d2 = ( (Number) o2 ).doubleValue();
        switch ( id ) {
        case ExpressionDefines.ADD:
            return new Double( d1 + d2 );
        case ExpressionDefines.SUB:
            return new Double( d1 - d2 );
        case ExpressionDefines.MUL:
            return new Double( d1 * d2 );
        case ExpressionDefines.DIV:
            return new Double( d1 / d2 );
        default: {
            throw new FilterEvaluationException( "Unknown ArithmeticExpression: '" + getExpressionName() + "'!" );
        }
        }
    }

    /**
     * returns the first expression
     *
     * @return the first expression
     */
    public Expression getFirstExpression() {
        return expr1;
    }

    /**
     * returns the second expression
     *
     * @return the second expression
     */
    public Expression getSecondExpression() {
        return expr2;
    }
}
