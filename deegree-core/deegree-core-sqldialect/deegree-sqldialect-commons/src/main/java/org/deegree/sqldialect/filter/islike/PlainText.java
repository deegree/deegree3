//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-base/src/main/java/org/deegree/filter/sql/islike/PlainText.java $
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
package org.deegree.sqldialect.filter.islike;

/**
 * Part of a {@link IsLikeString} that contains standard characters only.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author: mschneider $
 *
 * @version $Revision: 22060 $, $Date: 2010-01-20 17:59:52 +0100 (Mi, 20. Jan 2010) $
 */
final class PlainText implements IsLikeStringPart {

    private String text;

    /**
     * Creates a new instance of {@link PlainText}.
     *
     * @param text
     */
    PlainText( String text ) {
        this.text = text;
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
        return toSQL( false );
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
        StringBuffer sqlEscaped = new StringBuffer( text.length() );
        String rest = text;
        while ( rest.length() > 0 ) {
            char currentChar = rest.charAt( 0 );
            switch ( currentChar ) {
            case '%':
            case '_':
            case '"':
            case '\\': {
                sqlEscaped.append( '\\' );
                sqlEscaped.append( currentChar );
                break;
            }
            case '\'': {
                sqlEscaped.append( '\'' );
                sqlEscaped.append( currentChar );
                break;
            }
            default: {
                if ( toLowerCase ) {
                    sqlEscaped.append( Character.toLowerCase( currentChar ) );
                } else {
                    sqlEscaped.append( currentChar );
                }
                break;
            }
            }
            rest = rest.substring( 1 );
        }
        return sqlEscaped.toString();
    }

    @Override
    public String toString() {
        return "Text: " + text;
    }
}
