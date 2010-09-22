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

import java.util.Iterator;
import java.util.LinkedList;

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
 * <code>Concatenate</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Concatenate extends AbstractCustomExpression {

    private static final QName ELEMENT_NAME = new QName( SENS, "Concatenate" );

    private LinkedList<StringBuffer> values;

    private LinkedList<Continuation<StringBuffer>> valueContns;

    /***/
    public Concatenate() {
        // just used for SPI
    }

    private Concatenate( LinkedList<StringBuffer> values, LinkedList<Continuation<StringBuffer>> valueContns ) {
        this.values = values;
        this.valueContns = valueContns;
    }

    @Override
    public QName getElementName() {
        return ELEMENT_NAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypedObjectNode[] evaluate( T obj, XPathEvaluator<T> xpathEvaluator )
                            throws FilterEvaluationException {
        StringBuffer res = new StringBuffer();
        Iterator<StringBuffer> sbs = values.iterator();
        Iterator<Continuation<StringBuffer>> contns = valueContns.iterator();
        while ( sbs.hasNext() && contns.hasNext() ) {
            StringBuffer sb = new StringBuffer( sbs.next().toString().trim() );
            Continuation<StringBuffer> contn = contns.next();
            if ( contn != null ) {
                contn.evaluate( sb, (Feature) obj, (XPathEvaluator<Feature>) xpathEvaluator );
            }
            res.append( sb.toString() );
        }
        return new TypedObjectNode[] { new PrimitiveValue( res.toString().trim() ) };
    }

    @Override
    public Concatenate parse( XMLStreamReader in )
                            throws XMLStreamException {

        LinkedList<StringBuffer> values = new LinkedList<StringBuffer>();
        LinkedList<Continuation<StringBuffer>> valueContns = new LinkedList<Continuation<StringBuffer>>();

        in.require( START_ELEMENT, null, "Concatenate" );

        while ( !( in.isEndElement() && in.getLocalName().equals( "Concatenate" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "StringValue" ) ) {
                StringBuffer sb = new StringBuffer();
                valueContns.add( SymbologyParser.INSTANCE.updateOrContinue( in, "StringValue", sb, SBUPDATER, null ).second );
                values.add( sb );
            }
        }

        in.require( END_ELEMENT, null, "Concatenate" );
        return new Concatenate( values, valueContns );
    }
}