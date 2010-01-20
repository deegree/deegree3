//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.filter.sql.islike;

import java.util.ArrayList;
import java.util.List;

import org.deegree.filter.comparison.PropertyIsLike;

/**
 * Used for an escape-free representation of a literal from a {@link PropertyIsLike} operation.
 * <p>
 * May contain special symbols (wildCard, singleChar, escape) as a list of its parts ({@link IsLikeStringPart}).
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class IsLikeString {

    private List<IsLikeStringPart> parts;

    private String wildCard;

    private String singleChar;

    private String escape;

    /**
     * Constructs a new <code>SpecialCharString</code> instance from the given parameters.
     * 
     * @param encodedString
     * @param wildCard
     * @param singleChar
     * @param escape
     */
    public IsLikeString( String encodedString, String wildCard, String singleChar, String escape ) {
        this.wildCard = wildCard;
        this.singleChar = singleChar;
        this.escape = escape;
        this.parts = decode( encodedString );
    }

    /**
     * Decodes the given <code>String</code> to a representation that contains explicit objects to represent wildCard
     * and singleChar symbols and has no escape symbols.
     * 
     * @param encodedString
     *            encoded <code>String</code>, may contain wildCard, singleChar and escape symbols
     * @return decoded representation that contains special objects for special characters
     */
    private List<IsLikeStringPart> decode( String encodedString ) {

        List<IsLikeStringPart> parts = new ArrayList<IsLikeStringPart>( encodedString.length() );

        boolean escapeMode = false;
        String decodedString = encodedString;

        StringBuffer sb = null;
        while ( decodedString.length() > 0 ) {
            if ( escapeMode ) {
                if ( sb == null ) {
                    sb = new StringBuffer();
                }
                sb.append( decodedString.charAt( 0 ) );
                decodedString = decodedString.substring( 1 );
                escapeMode = false;
            } else {
                if ( decodedString.startsWith( wildCard ) ) {
                    if ( sb != null ) {
                        parts.add( new PlainText( sb.toString() ) );
                        sb = null;
                    }
                    parts.add( new WildCard() );
                    decodedString = decodedString.substring( wildCard.length() );
                } else if ( decodedString.startsWith( singleChar ) ) {
                    if ( sb != null ) {
                        parts.add( new PlainText( sb.toString() ) );
                        sb = null;
                    }
                    parts.add( new SingleChar() );
                    decodedString = decodedString.substring( singleChar.length() );
                } else if ( decodedString.startsWith( escape ) ) {
                    decodedString = decodedString.substring( escape.length() );
                    escapeMode = true;
                } else {
                    if ( sb == null ) {
                        sb = new StringBuffer();
                    }
                    sb.append( decodedString.charAt( 0 ) );
                    decodedString = decodedString.substring( 1 );
                }
            }
        }

        if ( sb != null ) {
            parts.add( new PlainText( sb.toString() ) );
        }
        return parts;
    }

    /**
     * Returns an encoding that is suitable for arguments of "IS LIKE"-clauses in SQL.
     * <p>
     * This means:
     * <ul>
     * <li>wildCard: encoded as the '%'-character</li>
     * <li>singleChar: encoded as the '_'-character</li>
     * <li>escape: encoded as the '\'-character</li>
     * </ul>
     * 
     * @return encoded string
     */
    public String toSQL() {
        StringBuffer sb = new StringBuffer();
        for ( IsLikeStringPart part : parts ) {
            sb.append( part.toSQL() );
        }
        return sb.toString();
    }

    /**
     * Returns an encoding that is suitable for arguments of "IS LIKE"-clauses in SQL.
     * <p>
     * This means:
     * <ul>
     * <li>wildCard: encoded as the '%'-character</li>
     * <li>singleChar: encoded as the '_'-character</li>
     * <li>escape: encoded as the '\'-character</li>
     * </ul>
     * 
     * @param toLowerCase
     *            true means: convert to lowercase letters
     * @return encoded string
     */
    public String toSQL( boolean toLowerCase ) {
        StringBuffer sb = new StringBuffer();
        for ( IsLikeStringPart part : parts ) {
            sb.append( part.toSQL( toLowerCase ) );
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for ( IsLikeStringPart part : parts ) {
            sb.append( part.toString() );
            sb.append( '\n' );
        }
        return sb.toString();
    }
}
