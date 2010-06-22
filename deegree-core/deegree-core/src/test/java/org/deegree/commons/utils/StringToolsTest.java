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

import static org.deegree.commons.utils.StringUtils.NO_TRIM_FIELDS;
import static org.deegree.commons.utils.StringUtils.REMOVE_DOUBLE_FIELDS;
import static org.deegree.commons.utils.StringUtils.REMOVE_EMPTY_FIELDS;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

/**
 * @version $Revision$
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 */
public class StringToolsTest {

    /**
     * Test method for
     * {@link org.deegree.commons.utils.StringUtils#replaceAll(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testReplaceAll() {
        assertEquals( "foo|bar||baz", StringUtils.replaceAll( "foo*bar**baz", "*", "|" ) );
        assertEquals( "foo$1bar$1baz", StringUtils.replaceAll( "foo*bar*baz", "*", "$1" ) );
        assertEquals( "", StringUtils.replaceAll( "", "*", "$1" ) );
    }

    /**
     * Test method for
     * {@link org.deegree.commons.utils.StringUtils#replaceAll(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test(expected = NullPointerException.class)
    public void testReplaceAllNull() {
        // should that be the correct behaviour? or should it return an empty string?
        StringUtils.replaceAll( null, "*", "$1" );
    }

    /**
     * Test method for
     * {@link org.deegree.commons.utils.StringUtils#replaceFirst(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testReplaceFirst() {
        assertEquals( "foo|bar**baz", StringUtils.replaceFirst( "foo*bar**baz", "*", "|" ) );
        assertEquals( "foo$1bar*baz", StringUtils.replaceFirst( "foo*bar*baz", "*", "$1" ) );
    }

    /**
     * Test method for {@link org.deegree.commons.utils.StringUtils#split(String, String, int)}.
     */
    @Test
    public void testSplit() {
        String[] result;
        // simple split
        result = StringUtils.split( "foo ;bar;;bar;baz", ";" );
        arrayCompare( result, "foo", "bar", "", "bar", "baz" );

        result = StringUtils.split( "foo ;bar;;bar;baz", ";", REMOVE_DOUBLE_FIELDS );
        arrayCompare( result, "foo", "bar", "", "baz" );

        result = StringUtils.split( "foo ;bar;;bar;baz", ";", REMOVE_EMPTY_FIELDS );
        arrayCompare( result, "foo", "bar", "bar", "baz" );

        result = StringUtils.split( "foo ;bar;; ;bar;baz", ";", NO_TRIM_FIELDS );
        arrayCompare( result, "foo ", "bar", "", " ", "bar", "baz" );

        result = StringUtils.split( "foo ;bar; ;bar;baz;;", ";", REMOVE_EMPTY_FIELDS );
        arrayCompare( result, "foo", "bar", "bar", "baz" );

        result = StringUtils.split( "foo ;bar; ;bar;baz;;", ";", REMOVE_EMPTY_FIELDS | NO_TRIM_FIELDS );
        arrayCompare( result, "foo ", "bar", " ", "bar", "baz" );

        result = StringUtils.split( "foo ;bar;;bar;baz", ";", NO_TRIM_FIELDS | REMOVE_DOUBLE_FIELDS
                                                              | REMOVE_EMPTY_FIELDS );
        arrayCompare( result, "foo ", "bar", "baz" );

        // some tests with empty fields at the end
        result = StringUtils.split( "foo ;bar;;bar;baz;;", ";" );
        arrayCompare( result, "foo", "bar", "", "bar", "baz", "", "" );

        result = StringUtils.split( "foo ;bar;;bar;baz;;", ";", REMOVE_DOUBLE_FIELDS );
        arrayCompare( result, "foo", "bar", "", "baz" );

        result = StringUtils.split( "foo ;bar;;bar;baz;;", ";", REMOVE_EMPTY_FIELDS );
        arrayCompare( result, "foo", "bar", "bar", "baz" );

        result = StringUtils.split( "foo ;bar;;bar;baz;;", ";;" );
        arrayCompare( result, "foo ;bar", "bar;baz", "" );

    }

    // compare all list elements with the given arguments
    private void arrayCompare( String[] actual, String... expected ) {
        assertEquals( "length differ", expected.length, actual.length );
        for ( int i = 0; i < expected.length; i++ ) {
            assertEquals( expected[i], actual[i] );
        }
    }

    // compare all list elements with the given arguments
    private void listCompare( List<String> actual, String... expected ) {
        assertEquals( "length differ", expected.length, actual.size() );
        for ( int i = 0; i < expected.length; i++ ) {
            assertEquals( expected[i], actual.get( i ) );
        }
    }

    /**
     * Test method for {@link org.deegree.commons.utils.StringUtils#count(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCountString() {
        assertEquals( StringUtils.count( "fod,sdmcsd,ssdcs,,", "," ), 4 );
        assertEquals( StringUtils.count( "fod,sdmcsd,ssdcs,,", ",s" ), 2 );
        assertEquals( StringUtils.count( "fod,sdmcsd,,,,ssdcs,,", ",," ), 3 );
        assertEquals( StringUtils.count( "fod,sdmcsd,,,,ssdcs,,", "" ), 0 );
        assertEquals( StringUtils.count( "", ",," ), 0 );
    }

    /**
     * Test method for {@link org.deegree.commons.utils.StringUtils#extract(String, String, String)}.
     */
    @Test
    public void testExtract() {
        listCompare( StringUtils.extract( "<foo><bar<bar><baz>", "<", ">" ), "foo", "bar<bar", "baz" );
        listCompare( StringUtils.extract( "fhsld jfflkfs dlk $$ sdn $foo$", "$", "$" ), "", "foo" );
        listCompare( StringUtils.extract( "fhsld jfflkfs dlk $$ sdn $foo$", "$", ">" ) );

    }

}
