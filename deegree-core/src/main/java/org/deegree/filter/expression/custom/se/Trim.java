//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.filter.expression.custom.se;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.SENS;
import static org.deegree.rendering.r2d.se.unevaluated.Continuation.SBUPDATER;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.Feature;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.custom.AbstractCustomExpression;
import org.deegree.rendering.r2d.se.parser.SymbologyParser;
import org.deegree.rendering.r2d.se.unevaluated.Continuation;

/**
 * <code>Trim</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Trim extends AbstractCustomExpression {

    private static final QName ELEMENT_NAME = new QName( SENS, "Trim" );

    private StringBuffer value;

    private Continuation<StringBuffer> contn;

    private boolean leading = true, trailing;

    private String substr;

    /**
     * 
     */
    public Trim() {
        // just used for SPI
    }

    private Trim( StringBuffer value, Continuation<StringBuffer> contn, boolean leading, boolean trailing, String substr ) {
        this.value = value;
        this.contn = contn;
        this.leading = leading;
        this.trailing = trailing;
        this.substr = substr;
    }

    @Override
    public QName getElementName() {
        return ELEMENT_NAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypedObjectNode[] evaluate( T obj, XPathEvaluator<T> xpathEvaluator )
                            throws FilterEvaluationException {
        StringBuffer sb = new StringBuffer( value.toString().trim() );
        if ( contn != null ) {
            contn.evaluate( sb, (Feature) obj, (XPathEvaluator<Feature>) xpathEvaluator );
        }

        String res = sb.toString();

        final int subLen = substr.length();
        if ( leading ) {
            while ( res.startsWith( substr ) ) {
                res = res.substring( subLen );
            }
        }
        if ( trailing ) {
            while ( res.endsWith( substr ) ) {
                res = res.substring( 0, res.length() - subLen );
            }
        }
        return new TypedObjectNode[] { new PrimitiveValue( res ) };
    }

    @Override
    public Trim parse( XMLStreamReader in )
                            throws XMLStreamException {

        StringBuffer value = null;
        Continuation<StringBuffer> contn = null;
        boolean leading = true, trailing = false;

        in.require( START_ELEMENT, null, "Trim" );

        String pos = in.getAttributeValue( null, "stripOffPosition" );
        if ( pos != null ) {
            if ( pos.equals( "trailing" ) ) {
                leading = false;
                trailing = true;
            }
            if ( pos.equals( "both" ) ) {
                trailing = true;
            }
        }
        String ch = in.getAttributeValue( null, "stripOffChar" );
        String substr = ch == null ? " " : ch;

        while ( !( in.isEndElement() && in.getLocalName().equals( "Trim" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "StringValue" ) ) {
                value = new StringBuffer();
                contn = SymbologyParser.INSTANCE.updateOrContinue( in, "StringValue", value, SBUPDATER, null ).second;
            }

        }
        in.require( END_ELEMENT, null, "Trim" );
        return new Trim( value, contn, leading, trailing, substr );
    }
}