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

import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This is a collection of some methods that work with arrays and lists, like join or removeAll. It is complementary to
 * the StringTools.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class ArrayUtils {
    /**
     * Removes all occurrences of token in array
     * 
     * @param array
     * @param token
     *            the obj to remove
     * @return the array without all occurrences of obj
     */
    public static String[] removeAll( String[] array, String token ) {
        ArrayList<String> vec = new ArrayList<String>();

        for ( String part : array ) {
            if ( !part.equals( token ) ) {
                vec.add( part );
            }
        }
        return vec.toArray( new String[] {} );
    }

    /**
     * Joins a list of strings with given delimiter.
     * 
     * @param delimiter
     *            the delimiter to put between every string.
     * @param strings
     * @return the joined string
     */
    public static String join( String delimiter, String... strings ) {
        // call join method for list
        // Arrays.asList is cheap and only creates a wrapper
        return join( delimiter, Arrays.asList( strings ) );
    }

    /**
     * Joins a list of objects with given delimiter.
     * 
     * @param delimiter
     *            the delimiter to put between every string.
     * @param objects
     * @return the joined string
     */
    public static String join( String delimiter, Object... objects ) {
        // call join method for list
        // Arrays.asList is cheap and only creates a wrapper
        return join( delimiter, Arrays.asList( objects ) );
    }

    /**
     * Joins a collection of objects with given delimiter.
     * 
     * @param delimiter
     *            the delimiter to put between every string.
     * @param objects
     * @return the joined string
     */
    public static String join( String delimiter, Collection<?> objects ) {
        StringBuilder sb = new StringBuilder();
        for ( Object part : objects ) {
            sb.append( part.toString() ).append( delimiter );
        }
        if ( sb.length() > delimiter.length() ) {
            sb.delete( sb.length() - delimiter.length(), sb.length() );
        }
        return sb.toString();
    }

    /**
     * Joins a list of <code>int</code>s.
     * 
     * @param delimiter
     * @param values
     * @return the joined string
     */
    public static String join( String delimiter, int[] values ) {
        StringBuilder sb = new StringBuilder();
        for ( int value : values ) {
            sb.append( Integer.toString( value ) ).append( delimiter );
        }
        if ( sb.length() > delimiter.length() ) { // remove last delimiter
            sb.delete( sb.length() - delimiter.length(), sb.length() );
        }
        return sb.toString();
    }

    /**
     * Joins a list of <code>double</code>s.
     * 
     * @param delimiter
     * @param values
     * @return the joined string
     */
    public static String join( String delimiter, double[] values ) {
        StringBuilder sb = new StringBuilder();
        for ( double value : values ) {
            sb.append( Double.toString( value ) ).append( delimiter );
        }
        if ( sb.length() > delimiter.length() ) { // remove last delimiter
            sb.delete( sb.length() - delimiter.length(), sb.length() );
        }
        return sb.toString();
    }

    /**
     * Checks if the array contains the string <code>value</code>.
     * 
     * @param target
     *            array to check if it contains <code>value</code>
     * @param value
     *            string to check if it within the array
     * @param caseSensitive
     *            true if the search should be case sensitive
     * @param exact
     *            if the equals test should return true if one of the target strings contains a part of the value.
     * @return true if passed array contains value
     */
    public static boolean contains( String[] target, String value, boolean caseSensitive, boolean exact ) {
        if ( target != null && target.length > 0 && value != null ) {
            String testVal = caseSensitive ? value : value.toLowerCase();
            for ( String id : target ) {
                if ( id != null ) {
                    String idVal = caseSensitive ? id : id.toLowerCase();
                    if ( exact ? idVal.equals( testVal ) : idVal.contains( testVal ) ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if the array contains the string <code>value</code>. This method is case insensitive.
     * 
     * @param target
     *            array to check if it contains <code>value</code>
     * @param value
     *            string to check if it within the array
     * @return true if passed array contains value
     */
    public static boolean contains( String[] target, String value ) {
        return contains( target, value, false, true );
    }

    /**
     * parse a string and return its tokens as array
     * 
     * @param s
     *            string to parse
     * @param delimiter
     *            delimiter that marks the end of a token
     * @param deleteDoubles
     *            if it's true all string that are already within the resulting array will be deleted, so that there
     *            will only be one copy of them.
     * @return an Array of Strings
     */
    public static String[] toArray( String s, String delimiter, boolean deleteDoubles ) {
        if ( s == null || s.equals( "" ) ) {
            return new String[0];
        }

        StringTokenizer st = new StringTokenizer( s, delimiter );
        ArrayList<String> vec = new ArrayList<String>( st.countTokens() );

        if ( st.countTokens() > 0 ) {
            for ( int i = 0; st.hasMoreTokens(); i++ ) {
                String t = st.nextToken();
                if ( ( t != null ) && ( t.length() > 0 ) ) {
                    vec.add( t.trim() );
                }
            }
        } else {
            vec.add( s );
        }

        String[] kw = vec.toArray( new String[vec.size()] );
        if ( deleteDoubles ) {
            kw = deleteDoubles( kw );
        }

        return kw;
    }

    /**
     * parse a string and return its tokens as typed List. empty fields will be removed from the list.
     * 
     * @param s
     *            string to parse
     * @param delimiter
     *            delimiter that marks the end of a token
     * @param deleteDoubles
     *            if it's true all string that are already within the resulting array will be deleted, so that there
     *            will only be one copy of them.
     * @return a list of Strings
     */
    public static List<String> toList( String s, String delimiter, boolean deleteDoubles ) {
        if ( s == null || s.equals( "" ) ) {
            return new ArrayList<String>();
        }

        StringTokenizer st = new StringTokenizer( s, delimiter );
        ArrayList<String> vec = new ArrayList<String>( st.countTokens() );
        for ( int i = 0; st.hasMoreTokens(); i++ ) {
            String t = st.nextToken();
            if ( ( t != null ) && ( t.length() > 0 ) ) {
                if ( deleteDoubles ) {
                    if ( !vec.contains( t.trim() ) ) {
                        vec.add( t.trim() );
                    }
                } else {
                    vec.add( t.trim() );
                }
            }
        }

        return vec;
    }

    /**
     * deletes all double entries from the submitted array
     * 
     * @param s
     *            to remove the doubles from
     * @return The string array without all doubled values
     */
    public static String[] deleteDoubles( String[] s ) {
        ArrayList<String> vec = new ArrayList<String>( s.length );

        for ( int i = 0; i < s.length; i++ ) {
            if ( !vec.contains( s[i] ) ) {
                vec.add( s[i] );
            }
        }

        return vec.toArray( new String[vec.size()] );
    }

    /**
     * @param str
     * @param delim
     * @return str.split(delim) values parsed as ints
     */
    public static ArrayList<Integer> splitAsIntList( String str, String delim ) {
        String[] ss = str.split( delim );
        ArrayList<Integer> ds = new ArrayList<Integer>( ss.length );
        for ( int i = 0; i < ss.length; ++i ) {
            ds.add( Integer.valueOf( ss[i] ) );
        }
        return ds;
    }

    /**
     * @param str
     * @param delim
     * @return str.split(delim) values parsed as doubles
     */
    public static double[] splitAsDoubles( String str, String delim ) {
        String[] ss = str.split( delim );
        double[] ds = new double[ss.length];
        for ( int i = 0; i < ss.length; ++i ) {
            ds[i] = parseDouble( ss[i] );
        }
        return ds;
    }

    /**
     * @param str
     * @param delim
     * @return str.split(delim) values parsed as floats
     * @throws NumberFormatException
     *             if one of the values could not be read.
     */
    public static float[] splitAsFloats( String str, String delim )
                            throws NumberFormatException {
        String[] ss = str.split( delim );
        float[] fs = new float[ss.length];
        for ( int i = 0; i < ss.length; ++i ) {
            fs[i] = parseFloat( ss[i] );
        }
        return fs;
    }

}
