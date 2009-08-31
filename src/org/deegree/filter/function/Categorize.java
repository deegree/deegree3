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
package org.deegree.filter.function;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.rendering.r2d.se.parser.SymbologyParser.updateOrContinue;
import static org.deegree.rendering.r2d.se.unevaluated.Continuation.SBUPDATER;

import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.filter.MatchableObject;
import org.deegree.filter.expression.Function;
import org.deegree.rendering.r2d.se.unevaluated.Continuation;

/**
 * <code>Categorize</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Categorize extends Function {

    private StringBuffer value;

    private Continuation<StringBuffer> contn;

    private boolean precedingBelongs = false;

    private LinkedList<StringBuffer> values = new LinkedList<StringBuffer>();

    private LinkedList<StringBuffer> thresholds = new LinkedList<StringBuffer>();

    private LinkedList<Continuation<StringBuffer>> valueContns = new LinkedList<Continuation<StringBuffer>>();

    private LinkedList<Continuation<StringBuffer>> thresholdContns = new LinkedList<Continuation<StringBuffer>>();

    /***/
    public Categorize() {
        super( "Categorize", null );
    }

    private static final String eval( StringBuffer initial, Continuation<StringBuffer> contn, MatchableObject f ) {
        StringBuffer sb = new StringBuffer( initial.toString().trim() );
        if ( contn != null ) {
            contn.evaluate( sb, f );
        }
        return sb.toString();
    }

    @Override
    public Object[] evaluate( MatchableObject f ) {
        String val = eval( value, contn, f );

        Iterator<StringBuffer> vals = values.iterator();
        Iterator<StringBuffer> barriers = thresholds.iterator();
        Iterator<Continuation<StringBuffer>> valContns = valueContns.iterator();
        Iterator<Continuation<StringBuffer>> barrierContns = thresholdContns.iterator();

        String curVal = eval( vals.next(), valContns.next(), f );
        while ( barriers.hasNext() ) {
            String cur = eval( barriers.next(), barrierContns.next(), f );
            String nextVal = eval( vals.next(), valContns.next(), f );
            if ( cur.equals( val ) ) {
                return new Object[] { precedingBelongs ? curVal : nextVal };
            }
            if ( val.compareTo( cur ) == -1 ) {
                return new Object[] { curVal };
            }
            curVal = nextVal;
        }

        return new Object[] { curVal };
    }

    /**
     * @param in
     * @throws XMLStreamException
     */
    public void parse( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "Categorize" );

        String belong = in.getAttributeValue( null, "thresholdsBelongTo" );
        if ( belong == null ) {
            belong = in.getAttributeValue( null, "threshholdsBelongTo" );
        }
        if ( belong != null ) {
            precedingBelongs = belong.equals( "preceding" );
        }

        while ( !( in.isEndElement() && in.getLocalName().equals( "Categorize" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "LookupValue" ) ) {
                value = new StringBuffer();
                contn = updateOrContinue( in, "LookupValue", value, SBUPDATER, null );
            }

            if ( in.getLocalName().equals( "Threshold" ) ) {
                StringBuffer sb = new StringBuffer();
                thresholdContns.add( updateOrContinue( in, "Threshold", sb, SBUPDATER, null ) );
                thresholds.add( sb );
            }

            if ( in.getLocalName().equals( "Value" ) ) {
                StringBuffer sb = new StringBuffer();
                valueContns.add( updateOrContinue( in, "Value", sb, SBUPDATER, null ) );
                values.add( sb );
            }

        }

        in.require( END_ELEMENT, null, "Categorize" );
    }

}
