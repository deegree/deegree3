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

import java.text.DecimalFormat;

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
 * <code>FormatNumber</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FormatNumber extends AbstractCustomExpression {

    private static final QName ELEMENT_NAME = new QName( SENS, "FormatNumber" );

    private StringBuffer numericValue;

    private Continuation<StringBuffer> numericValueContn;

    private DecimalFormat pattern, negativePattern;

    /***/
    public FormatNumber() {
        // just used for SPI
    }

    private FormatNumber( StringBuffer numericValue, Continuation<StringBuffer> numericValueContn,
                          DecimalFormat pattern, DecimalFormat negativePattern ) {
        this.numericValue = numericValue;
        this.numericValueContn = numericValueContn;
        this.pattern = pattern;
        this.negativePattern = negativePattern;
    }

    @Override
    public QName getElementName() {
        return ELEMENT_NAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypedObjectNode[] evaluate( T obj, XPathEvaluator<T> xpathEvaluator )
                            throws FilterEvaluationException {
        double nr;
        if ( numericValueContn != null ) {
            StringBuffer sb = new StringBuffer();
            sb.append( numericValue );
            numericValueContn.evaluate( sb, (Feature) obj, (XPathEvaluator<Feature>) xpathEvaluator );
            nr = parseDouble( sb.toString() );
        } else {
            nr = parseDouble( numericValue.toString() );
        }
        if ( nr < 0 && negativePattern != null ) {
            return new TypedObjectNode[] { new PrimitiveValue( negativePattern.format( nr ) ) };
        }
        return new TypedObjectNode[] { new PrimitiveValue( pattern.format( nr ) ) };
    }

    @Override
    public FormatNumber parse( XMLStreamReader in )
                            throws XMLStreamException {

        StringBuffer numericValue = null;
        Continuation<StringBuffer> numericValueContn = null;
        DecimalFormat pattern, negativePattern;

        in.require( START_ELEMENT, null, "FormatNumber" );

        String decimalPoint = in.getAttributeValue( null, "decimalPoint" );
        decimalPoint = decimalPoint == null ? "." : decimalPoint;
        String groupingSeparator = in.getAttributeValue( null, "groupingSeparator" );
        groupingSeparator = groupingSeparator == null ? "," : groupingSeparator;

        String pat = "", neg = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "FormatNumber" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "NumericValue" ) ) {
                numericValue = new StringBuffer();
                numericValueContn = SymbologyParser.INSTANCE.updateOrContinue( in, "NumericValue", numericValue,
                                                                               SBUPDATER, null ).second;
            }

            if ( in.getLocalName().equals( "Pattern" ) ) {
                pat = in.getElementText();
            }

            if ( in.getLocalName().equals( "NegativePattern" ) ) {
                neg = in.getElementText();
            }
        }

        if ( neg == null ) {
            neg = "-" + pat;
        }

        pattern = new DecimalFormat( pat );
        negativePattern = new DecimalFormat( neg );

        in.require( END_ELEMENT, null, "FormatNumber" );
        return new FormatNumber( numericValue, numericValueContn, pattern, negativePattern );
    }
}