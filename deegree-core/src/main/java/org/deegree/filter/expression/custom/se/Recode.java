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

import static java.lang.Double.parseDouble;
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
 * <code>Recode</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Recode extends AbstractCustomExpression {

    private static final QName ELEMENT_NAME = new QName( SENS, "Recode" );

    private StringBuffer value;

    private Continuation<StringBuffer> contn;

    private LinkedList<Double> datas = new LinkedList<Double>();

    private LinkedList<StringBuffer> values = new LinkedList<StringBuffer>();

    private LinkedList<Continuation<StringBuffer>> valueContns = new LinkedList<Continuation<StringBuffer>>();

    private String fallbackValue;

    /***/
    public Recode() {
        // just used for SPI
    }

    private Recode( StringBuffer value, Continuation<StringBuffer> contn, LinkedList<Double> datas,
                    LinkedList<StringBuffer> values, LinkedList<Continuation<StringBuffer>> valueContns,
                    String fallbackValue ) {
        this.value = value;
        this.contn = contn;
        this.datas = datas;
        this.values = values;
        this.valueContns = valueContns;
        this.fallbackValue = fallbackValue;
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

        String s = sb.toString();
        if ( s.isEmpty() ) {
            return new TypedObjectNode[] { new PrimitiveValue( fallbackValue ) };
        }
        double val = parseDouble( s );

        Iterator<Double> data = datas.iterator();
        Iterator<StringBuffer> vals = values.iterator();
        Iterator<Continuation<StringBuffer>> contns = valueContns.iterator();
        while ( data.hasNext() ) {
            StringBuffer target = new StringBuffer( vals.next().toString().trim() );
            Continuation<StringBuffer> contn = contns.next();

            if ( data.next().doubleValue() == val ) {
                if ( contn == null ) {
                    return new TypedObjectNode[] { new PrimitiveValue( target.toString() ) };
                }
                contn.evaluate( target, (Feature) obj, (XPathEvaluator<Feature>) xpathEvaluator );
                return new TypedObjectNode[] { new PrimitiveValue( target.toString() ) };
            }
        }

        return new TypedObjectNode[] { new PrimitiveValue( fallbackValue ) };
    }

    @Override
    public Recode parse( XMLStreamReader in )
                            throws XMLStreamException {

        StringBuffer value = null;
        Continuation<StringBuffer> contn = null;
        LinkedList<Double> datas = new LinkedList<Double>();
        LinkedList<StringBuffer> values = new LinkedList<StringBuffer>();
        LinkedList<Continuation<StringBuffer>> valueContns = new LinkedList<Continuation<StringBuffer>>();
        String fallbackValue;

        in.require( START_ELEMENT, null, "Recode" );

        fallbackValue = in.getAttributeValue( null, "fallbackValue" );

        while ( !( in.isEndElement() && in.getLocalName().equals( "Recode" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "LookupValue" ) ) {
                value = new StringBuffer();
                contn = SymbologyParser.INSTANCE.updateOrContinue( in, "LookupValue", value, SBUPDATER, null ).second;
            }

            if ( in.getLocalName().equals( "MapItem" ) ) {
                while ( !( in.isEndElement() && in.getLocalName().equals( "MapItem" ) ) ) {
                    in.nextTag();

                    if ( in.getLocalName().equals( "Data" ) ) {
                        datas.add( Double.valueOf( in.getElementText() ) );
                    }

                    if ( in.getLocalName().equals( "Value" ) ) {
                        StringBuffer sb = new StringBuffer();
                        valueContns.add( SymbologyParser.INSTANCE.updateOrContinue( in, "Value", sb, SBUPDATER, null ).second );
                        values.add( sb );
                    }
                }
            }
        }
        in.require( END_ELEMENT, null, "Recode" );
        return new Recode( value, contn, datas, values, valueContns, fallbackValue );
    }
}