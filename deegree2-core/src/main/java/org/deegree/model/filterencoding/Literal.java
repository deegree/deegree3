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

import org.deegree.framework.xml.XMLTools;
import org.deegree.model.feature.Feature;
import org.w3c.dom.Element;

/**
 * Encapsulates the information of a <Literal>element as defined in the FeatureId DTD.
 *
 * @author Markus Schneider
 * @version 07.08.2002
 */
public class Literal extends Expression {

    /** The literal's value. */
    private String value;

    /**
     * Constructs a new Literal.
     *
     * @param value
     */
    public Literal( String value ) {
        id = ExpressionDefines.LITERAL;
        this.value = value;
    }

    /**
     * Given a DOM-fragment, a corresponding Expression-object is built. This method recursively
     * calls other buildFromDOM () - methods to validate the structure of the DOM-fragment.
     *
     * @param element
     *
     * @throws FilterConstructionException
     *             if the structure of the DOM-fragment is invalid
     */
    public static Expression buildFromDOM( Element element )
                            throws FilterConstructionException {

        // check if root element's name equals 'Literal'
        if ( !element.getLocalName().equals( "Literal" ) ) {
            throw new FilterConstructionException( "Name of element does not equal 'Literal'!" );
        }

        return new Literal( XMLTools.getStringValue( element ) );
    }

    /**
     * Returns the literal's value (as String).
     *
     * @return the literal's value (as String).
     */
    public String getValue() {
        return value;
    }

    /**
     * @see org.deegree.model.filterencoding.Literal#getValue()
     * @param value
     */
    public void setValue( String value ) {
        this.value = value;
    }

    /**
     * Produces an indented XML representation of this object.
     *
     * @return XML representation of this object.
     */
    @Override
    public StringBuffer toXML() {
        StringBuffer sb = new StringBuffer( 200 );
        // use CDATA section because content may not allowed within
        // simple XML elements
        sb.append( "<ogc:Literal><![CDATA[" ).append( value ).append( "]]></ogc:Literal>" );
        return sb;
    }

    /**
     * Returns the <code>Literal</code>'s value (to be used in the evaluation of a complexer
     * <code>Expression</code>). If the value appears to be numerical, a <code>Double</code> is
     * returned, else a <code>String</code>. TODO: Improve datatype handling.
     *
     * @param feature
     *            that determines the values of <code>PropertyNames</code> in the expression (no
     *            use here)
     * @return the resulting value
     */
    @Override
    public Object evaluate( Feature feature ) {
        try {
            return new Double( value );
        } catch ( NumberFormatException e ) {
            // and then eat it
        }
        return value;
    }
}
