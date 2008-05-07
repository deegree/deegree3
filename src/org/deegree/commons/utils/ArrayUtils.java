//$HeadURL$
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
 53177 Bonn
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
package org.deegree.commons.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a collection of some methods that work with arrays and lists, like join or removeAll. It is complementary to the
 * StringTools.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: $
 *
 * @version $Revision: $, $Date: $
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
        
        for ( String part: array ) {
            if ( !part.equals( token ) ) {
                vec.add( part );
            }
        }
        return vec.toArray( new String[]{} );
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
     * Joins a list of objects with given delimiter.
     * 
     * @param delimiter
     *            the delimiter to put between every string.
     * @param objects
     * @return the joined string
     */
    public static String join( String delimiter, List<?> objects ) {
        StringBuilder sb = new StringBuilder();
        for ( Object part: objects ) {
            sb.append( part.toString() ).append( delimiter );
        }
        if ( sb.length() > delimiter.length() ) {
            sb.delete( sb.length() - delimiter.length(), sb.length() );
        }
        return sb.toString();
    }
    
    /**
     * Joins a list of <code>int</code>s.
     * @param delimiter
     * @param values
     * @return the joined string
     */
    public static String join( String delimiter, int[] values ) {
        StringBuilder sb = new StringBuilder();
        for ( int value: values ) {
            sb.append( Integer.toString( value ) ).append( delimiter );
        }
        if ( sb.length() > delimiter.length() ) {
            sb.delete( sb.length() - delimiter.length(), sb.length() );
        }
        return sb.toString();
    }
    
    /**
     * Checks if the array contains the string <code>value</code>. This method is case insensitive.
     * 
     * @param target
     *            array to check if it contains <code>value</code>
     * @param value
     *            string to check if it within the array
     * @return true if passed arrya contains value
     */
    public static boolean contains( String[] target, String value ) {
        if ( target == null || value == null ) {
            return false;
        }
        for ( String part: target ) {
            if ( value.equalsIgnoreCase( part ) ) {
                return true;
            }
        }

        return false;
    }
}
