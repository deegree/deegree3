//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/src/org/deegree/framework/util/StringTools.java $
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de

 
 ---------------------------------------------------------------------------*/
package org.deegree.model.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * this is a collection of some methods that extends the functionallity of the sun-java string
 * class.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: apoth $
 * 
 * @version $Revision: 10660 $, $Date: 2008-03-24 22:39:54 +0100 (Mo, 24 Mrz 2008) $
 */
public class StringTools {

    /**
     * concatenates an array of strings using a
     * 
     * @see StringBuffer
     * 
     * @param size
     *            estimated size of the target string
     * @param objects
     *            toString() will be called for each object to append it to the result string
     * @return concatinated string
     */
    public static String concat( int size, Object... objects ) {
        StringBuilder sbb = new StringBuilder( size );
        for ( int i = 0; i < objects.length; i++ ) {
            sbb.append( objects[i] );
        }
        return sbb.toString();
    }

    /**
     * replaces occurences of a string fragment within a string by a new string.
     * 
     * @param target
     *            is the original string
     * @param from
     *            is the string to be replaced
     * @param to
     *            is the string which will used to replace
     * @param all
     *            if it's true all occurences of the string to be replaced will be replaced. else
     *            only the first occurence will be replaced.
     * @return the changed target string
     * @deprecated
     */
    public static String replace( String target, String from, String to, boolean all ) {

        StringBuffer buffer = new StringBuffer( target.length() );
        int copyFrom = 0;
        char[] targetChars = null;
        int lf = from.length();
        int start = -1;
        do {
            start = target.indexOf( from );
            copyFrom = 0;
            if ( start == -1 ) {
                return target;
            }

            targetChars = target.toCharArray();
            while ( start != -1 ) {
                buffer.append( targetChars, copyFrom, start - copyFrom );
                buffer.append( to );
                copyFrom = start + lf;
                start = target.indexOf( from, copyFrom );
                if ( !all ) {
                    start = -1;
                }
            }
            buffer.append( targetChars, copyFrom, targetChars.length - copyFrom );
            target = buffer.toString();
            buffer.delete( 0, buffer.length() );
        } while ( target.indexOf( from ) > -1 && to.indexOf( from ) < 0 );

        return target;
    }

    /**
     * parse a string and return its tokens as array
     * 
     * @param s
     *            string to parse
     * @param delimiter
     *            delimiter that marks the end of a token
     * @param deleteDoubles
     *            if it's true all string that are already within the resulting array will be
     *            deleted, so that there will only be one copy of them.
     * @return array of strings 
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
     * parse a string and return its tokens as typed List. empty fields will be removed from the
     * list.
     * 
     * @param s
     *            string to parse
     * @param delimiter
     *            delimiter that marks the end of a token
     * @param deleteDoubles
     *            if it's true all string that are already within the resulting array will be
     *            deleted, so that there will only be one copy of them.
     * @return list of strings
     * @deprecated
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
     * transforms a string array to one string. the array fields are seperated by the submitted
     * delimiter:
     * 
     * @param s
     *            stringarray to transform
     * @param delimiter
     * @return string created as concatination of passed string array
     */
    public static String arrayToString( String[] s, char delimiter ) {
        StringBuilder res = new StringBuilder( s.length * 20 );

        for ( int i = 0; i < s.length; i++ ) {
            res.append( s[i] );

            if ( i < ( s.length - 1 ) ) {
                res.append( delimiter );
            }
        }

        return res.toString();
    }

    /**
     * transforms a list to one string. the array fields are seperated by the submitted delimiter:
     * 
     * @param s
     *            stringarray to transform
     * @param delimiter
     * @return string created as a concatination of passed list
     */
    public static String listToString( List s, char delimiter ) {
        StringBuilder res = new StringBuilder( s.size() * 20 );

        for ( int i = 0; i < s.size(); i++ ) {
            res.append( s.get( i ) );

            if ( i < ( s.size() - 1 ) ) {
                res.append( delimiter );
            }
        }

        return res.toString();
    }

    /**
     * transforms a double array to one string. the array fields are seperated by the submitted
     * delimiter:
     * 
     * @param s
     *            stringarray to transform
     * @param delimiter
     * @return string created as concatination of passed double array
     */
    public static String arrayToString( double[] s, char delimiter ) {
        StringBuilder res = new StringBuilder( s.length * 20 );

        for ( int i = 0; i < s.length; i++ ) {
            res.append( Double.toString( s[i] ) );

            if ( i < ( s.length - 1 ) ) {
                res.append( delimiter );
            }
        }

        return res.toString();
    }

    /**
     * transforms a int array to one string. the array fields are seperated by the submitted
     * delimiter:
     * 
     * @param s
     *            stringarray to transform
     * @param delimiter
     * @return string created as concatination of passed int array
     */
    public static String arrayToString( int[] s, char delimiter ) {
        StringBuilder res = new StringBuilder( s.length * 20 );

        for ( int i = 0; i < s.length; i++ ) {
            res.append( Integer.toString( s[i] ) );

            if ( i < ( s.length - 1 ) ) {
                res.append( delimiter );
            }
        }

        return res.toString();
    }

    /**
     * clears the begin and end of a string from the strings sumitted
     * 
     * @param s
     *            string to validate
     * @param mark
     *            string to remove from begin and end of <code>s</code>
     * @return string where <code>mark</code> has been removed from start and end
     * @deprecated
     */
    public static String validateString( String s, String mark ) {
        if ( s == null ) {
            return null;
        }

        if ( s.length() == 0 ) {
            return s;
        }

        s = s.trim();

        while ( s.startsWith( mark ) ) {
            s = s.substring( mark.length(), s.length() ).trim();
        }

        while ( s.endsWith( mark ) ) {
            s = s.substring( 0, s.length() - mark.length() ).trim();
        }

        return s;
    }

    /**
     * deletes all double entries from the submitted array
     * 
     * @param s
     * @return arry withou double entries
     */
    private static String[] deleteDoubles( String[] s ) {
        ArrayList<String> vec = new ArrayList<String>( s.length );

        for ( int i = 0; i < s.length; i++ ) {
            if ( !vec.contains( s[i] ) ) {
                vec.add( s[i] );
            }
        }

        return vec.toArray( new String[vec.size()] );
    }

    /**
     * removes all fields from the array that equals <code>s</code>
     * 
     * @param target
     *            array where to remove the submitted string
     * @param value
     *            string to remove
     * @return array from which the passed value has been removed
     */
    public static String[] removeFromArray( String[] target, String value ) {
        ArrayList<String> vec = new ArrayList<String>( target.length );

        for ( int i = 0; i < target.length; i++ ) {
            if ( !target[i].equals( value ) ) {
                vec.add( target[i] );
            }
        }

        return vec.toArray( new String[vec.size()] );
    }

    /**
     * checks if the submitted array contains the string <code>value</code>
     * 
     * @param target
     *            array to check if it contains <code>value</code>
     * @param value
     *            string to check if it within the array
     * @return true if passed arry contains value
     */
    public static boolean contains( String[] target, String value ) {
        if ( target == null || value == null ) {
            return false;
        }

        for ( int i = 0; i < target.length; i++ ) {
            if ( value.equalsIgnoreCase( target[i] ) ) {
                return true;
            }
        }

        return false;
    }

    /**
     * convert the array of string like [(x1,y1),(x2,y2)...] into an array of double
     * [x1,y1,x2,y2...]
     * 
     * @param s
     * @param delimiter
     * 
     * @return array of doubles
     * @deprecated
     */
    public static double[] toArrayDouble( String s, String delimiter ) {
        if ( s == null ) {
            return null;
        }

        if ( s.equals( "" ) ) {
            return null;
        }

        StringTokenizer st = new StringTokenizer( s, delimiter );

        ArrayList<String> vec = new ArrayList<String>( st.countTokens() );

        for ( int i = 0; st.hasMoreTokens(); i++ ) {
            String t = st.nextToken().replace( ' ', '+' );

            if ( ( t != null ) && ( t.length() > 0 ) ) {
                vec.add( t.trim().replace( ',', '.' ) );
            }
        }

        double[] array = new double[vec.size()];

        for ( int i = 0; i < vec.size(); i++ ) {
            array[i] = Double.parseDouble( vec.get( i ) );
        }

        return array;
    }

    /**
     * convert the array of string like [(x1,y1),(x2,y2)...] into an array of float values
     * [x1,y1,x2,y2...]
     * 
     * @param s
     * @param delimiter
     * 
     * @return array of floats
     * @deprecated
     */
    public static float[] toArrayFloat( String s, String delimiter ) {
        if ( s == null ) {
            return null;
        }

        if ( s.equals( "" ) ) {
            return null;
        }

        StringTokenizer st = new StringTokenizer( s, delimiter );

        ArrayList<String> vec = new ArrayList<String>( st.countTokens() );
        for ( int i = 0; st.hasMoreTokens(); i++ ) {
            String t = st.nextToken().replace( ' ', '+' );
            if ( ( t != null ) && ( t.length() > 0 ) ) {
                vec.add( t.trim().replace( ',', '.' ) );
            }
        }

        float[] array = new float[vec.size()];

        for ( int i = 0; i < vec.size(); i++ ) {
            array[i] = Float.parseFloat( vec.get( i ) );
        }

        return array;
    }

    /**
     * counts the occurrences of token into target
     * 
     * @param target
     * @param token
     * 
     * @return number of tokens within a string
     */
    public static int countString( String target, String token ) {
        int start = target.indexOf( token );
        int count = 0;

        while ( start != -1 ) {
            count++;
            start = target.indexOf( token, start + 1 );
        }

        return count;
    }

    /**
     * Extract all the strings that begin with "start" and end with "end" and store it into an array
     * of String
     * 
     * @param target
     * @param startString
     * @param endString
     * 
     * @return <code>null</code> if no strings were found!!
     */
    public static String[] extractStrings( String target, String startString, String endString ) {
        int start = target.indexOf( startString );

        if ( start == -1 ) {
            return null;
        }

        int count = countString( target, startString );
        String[] subString = null;
        if ( startString.equals( endString ) ) {
            count = count / 2;
            subString = new String[count];
            for ( int i = 0; i < count; i++ ) {
                int tmp = target.indexOf( endString, start + 1 );
                subString[i] = target.substring( start, tmp + 1 );
                start = target.indexOf( startString, tmp + 1 );
            }
        } else {
            subString = new String[count];
            for ( int i = 0; i < count; i++ ) {
                subString[i] = target.substring( start, target.indexOf( endString, start + 1 ) + 1 );
                subString[i] = extractString( subString[i], startString, endString, true, true );
                start = target.indexOf( startString, start + 1 );
            }
        }

        return subString;
    }

    /**
     * extract a string contained between startDel and endDel, you can remove the delimiters if set
     * true the parameters delStart and delEnd
     * 
     * @param target
     * @param startDel
     * @param endDel
     * @param delStart
     * @param delEnd
     * 
     * @return string contained between startDel and endDel
     */
    public static String extractString( String target, String startDel, String endDel, boolean delStart, boolean delEnd ) {
        int start = target.indexOf( startDel );

        if ( start == -1 ) {
            return null;
        }

        String s = target.substring( start, target.indexOf( endDel, start + 1 ) + 1 );

        s = s.trim();

        if ( delStart ) {
            while ( s.startsWith( startDel ) ) {
                s = s.substring( startDel.length(), s.length() ).trim();
            }
        }

        if ( delEnd ) {
            while ( s.endsWith( endDel ) ) {
                s = s.substring( 0, s.length() - endDel.length() ).trim();
            }
        }

        return s;
    }

}
