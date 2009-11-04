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
 * <code>Concatenate</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Concatenate extends Function {

    private LinkedList<StringBuffer> values = new LinkedList<StringBuffer>();

    private LinkedList<Continuation<StringBuffer>> valueContns = new LinkedList<Continuation<StringBuffer>>();

    /***/
    public Concatenate() {
        super( "Concatenate", null );
    }

    @Override
    public Object[] evaluate( MatchableObject f ) {
        StringBuffer res = new StringBuffer();
        Iterator<StringBuffer> sbs = values.iterator();
        Iterator<Continuation<StringBuffer>> contns = valueContns.iterator();
        while ( sbs.hasNext() && contns.hasNext() ) {
            StringBuffer sb = new StringBuffer( sbs.next().toString().trim() );
            Continuation<StringBuffer> contn = contns.next();
            if ( contn != null ) {
                contn.evaluate( sb, f );
            }
            res.append( sb.toString() );
        }
        return new Object[] { res.toString().trim() };
    }

    /**
     * @param in
     * @throws XMLStreamException
     */
    public void parse( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "Concatenate" );

        while ( !( in.isEndElement() && in.getLocalName().equals( "Concatenate" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "StringValue" ) ) {
                StringBuffer sb = new StringBuffer();
                valueContns.add( updateOrContinue( in, "StringValue", sb, SBUPDATER, null ) );
                values.add( sb );
            }

        }

        in.require( END_ELEMENT, null, "Concatenate" );
    }

}
