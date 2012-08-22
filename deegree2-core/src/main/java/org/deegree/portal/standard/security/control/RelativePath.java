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
package org.deegree.portal.standard.security.control;

import java.io.File;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author: elmasri$
 *
 * @version $Revision$, $Date: 08-Mar-2007 16:46:12$
 */
public class RelativePath {

    /**
     * get the first match from the many delimiters give as input
     *
     * @param source
     * @param delimiters
     * @return String
     */
    public static String getFirstMatch( String source, String[] delimiters ) {

        Vector<Delimiter> indices = new Vector<Delimiter>();

        for ( int i = 0; i < delimiters.length; i++ ) {
            // we added the indices of all matches to the vector
            indices.add( new Delimiter( i, source.indexOf( delimiters[i] ), delimiters[i] ) );
        }

        Delimiter delimiter = getFirstIndex( indices );
        if ( delimiter.getValue() == null ) {
            return null;
        }

        return source.substring( 0, delimiter.getFoundAt() + 1 );
    }

    /**
     * Gets the first index of a match from the many delimiters given as input Takes many delimiters
     * returns the delimiter that occured first
     *
     * @param collection
     * @return instance of Delimiter class
     */
    private static Delimiter getFirstIndex( Collection<Delimiter> collection ) {

        Delimiter delimiter = new Delimiter( -1, 999, null );
        Iterator it = collection.iterator();
        // comparing the matches to see which match occured first
        while ( it.hasNext() ) {
            Delimiter temp = (Delimiter) it.next();
            int indexOf = temp.foundAt;
            if ( indexOf < delimiter.getFoundAt() && indexOf > -1 ) {
                delimiter = temp;
            }
        }

        if ( delimiter == null ) {
            return null;
        }
        return delimiter;

    }

    /**
     * Split a string based on the given delimiters and return an array of strings(tokens)
     *
     * @param source
     * @param delimiters
     * @return tokens from a given string
     */
    public static String[] splitString( String source, String[] delimiters ) {

        if ( source == null || delimiters == null )
            return null;

        Vector<String> returnedStrings = new Vector<String>();
        String tempSource = source;

        while ( tempSource.length() != 0 ) {

            int delimiterLength = 0;
            String match = getFirstMatch( tempSource, delimiters );
            // if this is the last token in the String
            if ( match == null ) {
                returnedStrings.add( tempSource );
                break;
            } else {

                // removing any delimiters that could exist
                for ( int i = 0; i < delimiters.length; i++ ) {
                    if ( match.contains( delimiters[i] ) ) {
                        match = match.replace( delimiters[i], "" );
                        delimiterLength = delimiters[i].length();
                        break;
                    }

                }
                // Ignore the ./ and don't add it to the array
                if ( match.compareTo( "./" ) != 0 ) {
                    returnedStrings.add( match );
                }
                tempSource = tempSource.substring( match.length() + delimiterLength, tempSource.length() );
            }

        }

        String[] strings = new String[returnedStrings.size()];
        for ( int i = 0; i < returnedStrings.size(); i++ ) {
            strings[i] = returnedStrings.elementAt( i );
        }
        return strings;
    }

    /**
     * Maps from a source sTring to a target String, based on the delimiters given the delimiters
     * are basically "\\" or "/", but it also could be anything else Two absolute pathes should be
     * given here Don't give relative or non existing pathes
     *
     * @param source
     * @param target
     * @param delimiters
     * @return the mapped path
     * @throws ParseException
     */
    public static String mapRelativePath( String source, String target, String[] delimiters )
                            throws ParseException {

        if ( !new File( source ).isAbsolute() ) {
            throw new ParseException( "The source path is not absolute", 0 );
        }
        if ( !new File( target ).isAbsolute() ) {
            throw new ParseException( "The target path is not absolute", 0 );
        }

        String[] sourceTokens = splitString( source, delimiters );
        String[] targetTokens = splitString( target, delimiters );
        if ( sourceTokens == null || targetTokens == null )
            return null;
        if ( sourceTokens.length == 0 || targetTokens.length == 0 )
            return null;

        int lessTokens = 0;
        if ( sourceTokens.length < targetTokens.length ) {
            lessTokens = sourceTokens.length;
        } else {
            lessTokens = targetTokens.length;
        }

        int counter = 0;
        for ( counter = 0; counter < lessTokens; counter++ ) {
            if ( !sourceTokens[counter].equals( targetTokens[counter] ) )
                break;
        }

        StringBuffer buffer = new StringBuffer();
        for ( int i = counter; i < sourceTokens.length; i++ ) {
            if ( i != sourceTokens.length - 1 ) {
                buffer.append( "../" );
            } else {
                // We are checking if the last token in the source String is a file or a directory
                // if its a file we don'tn write it
                File sourceFile = new File( source );
                if ( sourceFile.isDirectory() ) {
                    buffer.append( "../" );
                }
            }

        }

        // This is used when the target is only one token different/larger than the source, so we
        // just take
        // the last token in the target
        if ( ( counter == sourceTokens.length ) && ( sourceTokens.length == targetTokens.length + 1 )
             && ( sourceTokens[counter - 1].equals( targetTokens[counter - 1] ) ) ) {
            return targetTokens[targetTokens.length - 1];
        }
        for ( int i = counter; i < targetTokens.length; i++ ) {
            if ( buffer.length() == 0 ) {
                // This is the first token in the path
                buffer.append( "./" );
            }
            buffer.append( targetTokens[i] );
            if ( i != targetTokens.length - 1 ) {
                buffer.append( "/" );
            }

        }

        return buffer.toString();
    }

    static class Delimiter {
        int index;

        String value;

        int foundAt;

        /**
         * @param index
         * @param foundAt
         * @param value
         */
        public Delimiter( int index, int foundAt, String value ) {
            this.index = index;
            this.value = value;
            this.foundAt = foundAt;
        }

        /**
         * @return int
         */
        public int getIndex() {
            return index;
        }

        /**
         * @param index
         */
        public void setIndex( int index ) {
            this.index = index;
        }

        /**
         * @return String
         */
        public String getValue() {
            return value;
        }

        /**
         * @param value
         */
        public void setValue( String value ) {
            this.value = value;
        }

        /**
         * @return int
         */
        public int getFoundAt() {
            return foundAt;
        }

        /**
         * @param foundAt
         */
        public void setFoundAt( int foundAt ) {
            this.foundAt = foundAt;
        }
    }

}
