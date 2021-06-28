//$HeadURL$
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
package org.deegree.commons.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a collection of some methods that work with strings, like split or replace. It is complementary to the
 * ArrayTools.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class StringUtils {

    /**
     * Remove empty fields. Used as an option for some StringTools methods.
     */
    public static final int REMOVE_EMPTY_FIELDS = 1;

    /**
     * Remove all double ocurrences of a field. Used as an option for some StringTools methods.
     */
    public static final int REMOVE_DOUBLE_FIELDS = 2;

    /**
     * Do not trim whitespace on fields. Used as an option for some StringTools methods.
     */
    public static final int NO_TRIM_FIELDS = 4;

    /**
     * Extract all the strings that begin and end with the given tokens.
     * 
     * @param target
     * @param start
     *            start token
     * @param end
     *            ent token
     * 
     * @return a list with all extracted strings
     */
    public static List<String> extract( String target, String start, String end ) {
        List<String> result = new LinkedList<String>();
        Pattern p = Pattern.compile( Pattern.quote( start ) + "(.*?)" + Pattern.quote( end ) );
        Matcher m = p.matcher( target );
        while ( m.find() ) {
            result.add( m.group( 1 ) );
        }
        return result;
    }

    /**
     * Replaces the first substring of this string that matches the given from string with the given replacement. Works
     * like {@link String#replaceFirst(String, String)} but doesn't use regular expressions. All occurences of special
     * chars will be escaped.
     * 
     * @param target
     *            is the original string
     * @param from
     *            is the string to be replaced
     * @param to
     *            is the string which will used to replace
     * 
     * @return the changed target string
     */
    public static String replaceFirst( String target, String from, String to ) {
        return target.replaceFirst( Pattern.quote( from ), Matcher.quoteReplacement( to ) );
    }

    /**
     * Replaces the all substrings of this string that matches the given from string with the given replacement. Works
     * like {@link String#replaceAll(String, String)} but doesn't use regular expressions. All occurences of special
     * chars will be escaped.
     * 
     * @param target
     *            is the original string
     * @param from
     *            is the string to be replaced
     * @param to
     *            is the string which will used to replace
     * 
     * @return the changed target string
     */
    public static String replaceAll( String target, String from, String to ) {
        return target.replaceAll( Pattern.quote( from ), Matcher.quoteReplacement( to ) );
    }

    /**
     * Splits a string on all occurrences of delimiter and returns a list with all parts. Each part will be trimmed from
     * whitespace. See {@link StringUtils#split(String, String, int)} for further options. If you need regular
     * expressions, use {@link String#split(String)}.
     * 
     * @param string
     *            the string to split
     * @param delimiter
     * @return a list with all parts
     */
    public static String[] split( String string, String delimiter ) {
        return split( string, delimiter, 0 );
    }

    /**
     * Splits a string on all occurrences of delimiter and returns a list with all parts. If you need regular
     * expressions, use {@link String#split(String)}.
     * 
     * This methods offers some options to modify the behaviour of the splitting. You can combine the options with |
     * (eg. StringTools.split(string, delimiter, REMOVE_EMPTY_FIELDS | REMOVE_DOUBLE_FIELDS)
     * 
     * <ul>
     * <li> {@link StringUtils#REMOVE_DOUBLE_FIELDS} removes all double occurrences of a field.</li>
     * <li> {@link StringUtils#REMOVE_EMPTY_FIELDS} removes all empty fields.</li>
     * <li> {@link StringUtils#NO_TRIM_FIELDS} doesn't remove whitespace around each field</li>
     * </ul>
     * 
     * @param string
     * @param delimiter
     * @param options
     *            a combination (|) of options
     * @return a list with all parts
     */
    public static String[] split( String string, String delimiter, int options ) {
        String[] parts = string.split( Pattern.quote( delimiter ) );
        Set<String> parts_set = new HashSet<String>();
        int doublesRemoves = 0;

        ArrayList<String> result = new ArrayList<String>( parts.length );
        for ( String part : parts ) {
            if ( ( options & NO_TRIM_FIELDS ) != NO_TRIM_FIELDS ) {
                part = part.trim();
            }
            if ( ( options & REMOVE_EMPTY_FIELDS ) == REMOVE_EMPTY_FIELDS && part.length() == 0 ) {
                // skip
            } else {
                if ( ( options & REMOVE_DOUBLE_FIELDS ) == REMOVE_DOUBLE_FIELDS ) {
                    if ( parts_set.contains( part ) ) {
                        // skip
                    } else {
                        doublesRemoves += 1;
                        result.add( part );
                        parts_set.add( part );
                    }
                } else {
                    result.add( part );
                }
            }
        }

        // check if we got empty fields at the end and if we want to keep them, because String#split will remove the
        // last delimiter
        if ( string.endsWith( delimiter ) && ( options & REMOVE_EMPTY_FIELDS ) != REMOVE_EMPTY_FIELDS ) {
            int count = StringUtils.count( string, delimiter );
            count += 1; // n delimiters -> n+1 fields
            int missingFields = count - parts.length - doublesRemoves;
            for ( int i = 0; i < missingFields; i++ ) {
                result.add( "" );
            }
        }

        return result.toArray( new String[] {} );
    }

    /**
     * Removes all occurrences of a string from the start and end. If you only want to remove whitespaces use
     * {@link String#trim()}.
     * 
     * @param string
     * @param mark
     *            string to remove from begin and end of <code>string</code>
     * @return string where <code>mark</code> has been removed from start and end
     */
    public static String trim( String string, String mark ) {
        if ( string == null ) {
            return null;
        }
        if ( string.length() == 0 ) {
            return string;
        }

        string = string.trim();

        while ( string.startsWith( mark ) ) {
            string = string.substring( mark.length() ).trim();
        }

        while ( string.endsWith( mark ) ) {
            string = string.substring( 0, string.length() - mark.length() ).trim();
        }

        return string;
    }

    /**
     * Counts the occurrences of token in target.
     * 
     * @param target
     * @param token
     * 
     * @return number of tokens within a string
     */
    public static int count( String target, String token ) {
        if ( token == null || token.length() == 0 ) {
            return 0;
        }
        int count = 0;
        int i = 0;
        while ( ( i = target.indexOf( token, i ) ) != -1 ) {
            i += token.length();
            count += 1;
        }
        return count;
    }

    /**
     * Test if given string is not <code>null</code> and not is the empty string "".
     * 
     * @param s
     *            the string to test.
     * @return true iff s is not <code>null</code> and s not is the empty string "".
     */
    public final static boolean isSet( String s ) {
        return s != null && !"".equals( s );

    }

    /**
     * Convert the given String to the number of bytes they represent. Incoming values could be something like:
     * <ul>
     * <li>20k, result=20*1024</li>
     * <li>20M, result=20*(1024*1024)</li>
     * <li>20G, result=20*(1024*1024*1024)</li>
     * </ul>
     * If the String could not be parsed, 0 will be returned.
     * 
     * @param size
     *            the size representation
     * @return the size in bytes, or 0 if not parsable.
     */
    public final static long parseByteSize( String size ) {
        String s = size;
        long result = 0;
        if ( isSet( s ) ) {
            int byteConvert = 1;
            // split on no numbers.
            String[] split = s.split( "\\D" );
            // only the first split is of importance
            String bytes = split[0];
            if ( bytes.length() != s.length() ) {
                // some characters were used after the split.
                String unit = s.substring( bytes.length(), bytes.length() + 1 );
                if ( unit.equalsIgnoreCase( "k" ) ) {
                    byteConvert = 1024;
                } else if ( unit.equalsIgnoreCase( "m" ) ) {
                    byteConvert = 1024 * 1024;
                } else if ( unit.equalsIgnoreCase( "g" ) ) {
                    byteConvert = 1024 * 1024 * 1024;
                }
            }
            try {
                result = Long.parseLong( bytes ) * byteConvert;
            } catch ( NumberFormatException e ) {

                // nothing, just return 0

            }
        }
        return result;
    }

    public static String concat( List<? extends Object> parts, String separator ) {
        StringBuilder sb = new StringBuilder();
        if ( !parts.isEmpty() ) {
            sb.append( parts.get( 0 ) );
        }
        for ( int i = 1; i < parts.size(); i++ ) {
            sb.append( separator );
            sb.append( parts.get( i ) );
        }
        return sb.toString();
    }

    public static String repeat( int count, String str ) {
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < count; ++i ) {
            sb.append( str );
        }
        return sb.toString();
    }
    
    /**
     * Split string into multiple elements and alow the delimiter to be escaped with a backslash
     * 
     * @see OGC WFS 1.1.0 14.2.2
     * 
     * @param input
     * @param separator any character expect backslash
     * @param limit maximum number of elements to return, zero is unlimited
     * @return a list with all parts
     */
    public static List<String> splitEscaped( String input, char delimiter, int limit ) {
        List<String> res = new ArrayList<>();
        if ( input == null ) {
            return res;
        }
        if ( delimiter == '\\' ) {
            throw new IllegalArgumentException( "The delimiter cannot be the escape character" );
        }
        if ( limit < 1 ) {
            limit = Integer.MAX_VALUE;
        }

        StringBuilder sb = new StringBuilder();
        boolean escaped = false;

        for ( int i = 0; i < input.length(); i++ ) {
            char c = input.charAt( i );
            if ( c == delimiter && !escaped && res.size() < limit ) {
                res.add( sb.toString() );
                sb.setLength( 0 );
            } else {
                if ( escaped ) {
                    escaped = false;
                    sb.append( '\\' );
                    sb.append( c );
                } else if ( c == '\\' ) {
                    escaped = true;
                } else {
                    sb.append( c );
                }
            }
        }
        
        if ( escaped ) {
            throw new IllegalArgumentException( "The specified String contains a incomplete escape sequence." );
        }

        res.add( sb.toString() );
        return res;
    }
    
    /**
     * Resolve escape sequences in a String.
     *
     * @see OGC WFS 1.1.0 14.2.2
     *
     * @param input the String to unescape
     * @return resolved String
     */
    public static String unescape(String input) {
        Objects.requireNonNull( input, "The specified String cannot be null" );

        StringBuilder sb = new StringBuilder();
        boolean escaped = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (escaped) {
                escaped = false;
                sb.append(c);
            } else if (c == '\\') {
                escaped = true;
            } else {
                sb.append(c);
            }
        }
        
        if (escaped) {
            throw new IllegalArgumentException(
                    "The specified String contains a incomplete escape sequence.");
        }
        
        return sb.toString();
    }
}