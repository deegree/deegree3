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
package org.deegree.filter.function.se;

import static java.lang.Double.parseDouble;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.rendering.r2d.se.parser.SymbologyParser.updateOrContinue;
import static org.deegree.rendering.r2d.se.unevaluated.Continuation.SBUPDATER;

import java.text.DecimalFormat;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.filter.MatchableObject;
import org.deegree.filter.expression.Function;
import org.deegree.rendering.r2d.se.unevaluated.Continuation;

/**
 * <code>FormatNumber</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FormatNumber extends Function {

    private String decimalPoint, groupingSeparator;

    private StringBuffer numericValue;

    private Continuation<StringBuffer> numericValueContn;

    private DecimalFormat pattern, negativePattern;

    /***/
    public FormatNumber() {
        super( "FormatNumber", null );
    }

    @Override
    public Object[] evaluate( MatchableObject f ) {
        double nr;
        if ( numericValueContn != null ) {
            StringBuffer sb = new StringBuffer();
            sb.append( numericValue );
            numericValueContn.evaluate( sb, f );
            nr = parseDouble( sb.toString() );
        } else {
            nr = parseDouble( numericValue.toString() );
        }
        if ( nr < 0 && negativePattern != null ) {
            return new Object[] { negativePattern.format( nr ) };
        }
        return new Object[] { pattern.format( nr ) };
    }

    /**
     * @param in
     * @throws XMLStreamException
     */
    public void parse( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "FormatNumber" );

        decimalPoint = in.getAttributeValue( null, "decimalPoint" );
        decimalPoint = decimalPoint == null ? "." : decimalPoint;
        groupingSeparator = in.getAttributeValue( null, "groupingSeparator" );
        groupingSeparator = groupingSeparator == null ? "," : groupingSeparator;

        String pat = "", neg = null;

        while ( !( in.isEndElement() && in.getLocalName().equals( "FormatNumber" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "NumericValue" ) ) {
                numericValue = new StringBuffer();
                numericValueContn = updateOrContinue( in, "NumericValue", numericValue, SBUPDATER, null );
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
    }

}
