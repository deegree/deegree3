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

import static org.deegree.commons.utils.ArrayUtils.join;
import static org.deegree.commons.utils.ArrayUtils.removeAll;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

/**
 *
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class ArrayToolsTest {

    /**
     * Test method for {@link org.deegree.commons.utils.ArrayUtils#join( String delimiter, String... strings )}.
     */
    @Test
    public void testJoin() {
        assertEquals( "foo|bar|baz", join( "|", "foo", "bar", "baz" ) );
        assertEquals( "foo|bar|baz", join( "|", new String[] { "foo", "bar", "baz" } ) );
        String[] test = new String[] { "foo", "bar", "baz" };
        assertEquals( "foo|bar|baz", join( "|", test ) );
    }

    /**
     * Test method for {@link org.deegree.commons.utils.ArrayUtils#join(String, List)}.
     */
    @Test
    public void testJoinList() {
        List<String> list1 = new LinkedList<String>();
        list1.add( "foo" );
        list1.add( "bar" );
        list1.add( "baz" );
        assertEquals( "foo|bar|baz", join( "|", list1 ) );
        List<String> list2 = new ArrayList<String>( 3 );
        list2.add( "foo" );
        list2.add( "bar" );
        list2.add( "baz" );
        assertEquals( "foo | bar | baz", join( " | ", list2 ) );
    }

    /**
     * Test method for {@link org.deegree.commons.utils.ArrayUtils#join( String delimiter, int[] values )}.
     */
    @Test
    public void testjoinInts() {
        int[] arr1 = new int[] { 1, 2, 3, 4, 5 };
        assertEquals( "1, 2, 3, 4, 5", ArrayUtils.join( ", ", arr1 ) );
        assertEquals( "1,2,3,4,5", ArrayUtils.join( ",", arr1 ) );
        assertEquals( "12345", ArrayUtils.join( "", arr1 ) );
        assertEquals( "", ArrayUtils.join( ",", new int[] {} ) );
    }

    /**
     * Test method for {@link org.deegree.commons.utils.ArrayUtils#removeAll(String[], String)}.
     */
    @Test
    public void testRemoveAll() {
        String[] arr1 = new String[] { "foo", "bar", "baz", "baz", "bazfoo" };
        arrayCompare( removeAll( arr1, "bar" ), "foo", "baz", "baz", "bazfoo" );
        arrayCompare( removeAll( arr1, "baz" ), "foo", "bar", "bazfoo" );
        arrayCompare( removeAll( arr1, "bazba" ), "foo", "bar", "baz", "baz", "bazfoo" );
    }

    // compare all list elements with the given arguments
    private <T> void arrayCompare( T[] actual, T... expected ) {
        assertEquals( "length differ", expected.length, actual.length );
        for ( int i = 0; i < expected.length; i++ ) {
            assertEquals( expected[i], actual[i] );
        }
    }
}
