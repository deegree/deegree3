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

import static org.deegree.commons.utils.StringTools.NO_TRIM_FIELDS;
import static org.deegree.commons.utils.StringTools.REMOVE_DOUBLE_FIELDS;
import static org.deegree.commons.utils.StringTools.REMOVE_EMPTY_FIELDS;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.deegree.commons.utils.StringTools;
import org.junit.Test;

/**
 * @version $Revision: $
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: $
 * 
 */
public class StringToolsTest {

    /**
     * Test method for
     * {@link org.deegree.model.util.StringTools#replaceAll(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testReplaceAll() {
        assertEquals( "foo|bar||baz", StringTools.replaceAll( "foo*bar**baz", "*", "|" ) );
        assertEquals( "foo$1bar$1baz", StringTools.replaceAll( "foo*bar*baz", "*", "$1" ) );
        assertEquals( "", StringTools.replaceAll( "", "*", "$1" ) );
    }

    /**
     * Test method for
     * {@link org.deegree.model.util.StringTools#replaceAll(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test(expected = NullPointerException.class)
    public void testReplaceAllNull() {
        // should that be the correct behaviour? or should it return an empty string?
        StringTools.replaceAll( null, "*", "$1" );
    }

    /**
     * Test method for
     * {@link org.deegree.model.util.StringTools#replaceFirst(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testReplaceFirst() {
        assertEquals( "foo|bar**baz", StringTools.replaceFirst( "foo*bar**baz", "*", "|" ) );
        assertEquals( "foo$1bar*baz", StringTools.replaceFirst( "foo*bar*baz", "*", "$1" ) );
    }

    /**
     * Test method for {@link org.deegree.model.util.StringTools#split(String, String, int)}.
     */
    @Test
    public void testSplit() {
        String[] result;
        // simple split
        result = StringTools.split( "foo ;bar;;bar;baz", ";" );
        arrayCompare( result, "foo", "bar", "", "bar", "baz" );

        result = StringTools.split( "foo ;bar;;bar;baz", ";", REMOVE_DOUBLE_FIELDS );
        arrayCompare( result, "foo", "bar", "", "baz" );

        result = StringTools.split( "foo ;bar;;bar;baz", ";", REMOVE_EMPTY_FIELDS );
        arrayCompare( result, "foo", "bar", "bar", "baz" );

        result = StringTools.split( "foo ;bar;; ;bar;baz", ";", NO_TRIM_FIELDS );
        arrayCompare( result, "foo ", "bar", "", " ", "bar", "baz" );

        result = StringTools.split( "foo ;bar; ;bar;baz;;", ";", REMOVE_EMPTY_FIELDS );
        arrayCompare( result, "foo", "bar", "bar", "baz" );

        result = StringTools.split( "foo ;bar; ;bar;baz;;", ";", REMOVE_EMPTY_FIELDS | NO_TRIM_FIELDS );
        arrayCompare( result, "foo ", "bar", " ", "bar", "baz" );

        result = StringTools.split( "foo ;bar;;bar;baz", ";", NO_TRIM_FIELDS | REMOVE_DOUBLE_FIELDS
                                                              | REMOVE_EMPTY_FIELDS );
        arrayCompare( result, "foo ", "bar", "baz" );

        // some tests with empty fields at the end
        result = StringTools.split( "foo ;bar;;bar;baz;;", ";" );
        arrayCompare( result, "foo", "bar", "", "bar", "baz", "", "" );

        result = StringTools.split( "foo ;bar;;bar;baz;;", ";", REMOVE_DOUBLE_FIELDS );
        arrayCompare( result, "foo", "bar", "", "baz" );

        result = StringTools.split( "foo ;bar;;bar;baz;;", ";", REMOVE_EMPTY_FIELDS );
        arrayCompare( result, "foo", "bar", "bar", "baz" );

        result = StringTools.split( "foo ;bar;;bar;baz;;", ";;" );
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
     * Test method for {@link org.deegree.model.util.StringTools#count(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCountString() {
        assertEquals( StringTools.count( "fod,sdmcsd,ssdcs,,", "," ), 4 );
        assertEquals( StringTools.count( "fod,sdmcsd,ssdcs,,", ",s" ), 2 );
        assertEquals( StringTools.count( "fod,sdmcsd,,,,ssdcs,,", ",," ), 3 );
        assertEquals( StringTools.count( "fod,sdmcsd,,,,ssdcs,,", "" ), 0 );
        assertEquals( StringTools.count( "", ",," ), 0 );
    }
    
    /**
     * Test method for {@link org.deegree.model.util.StringTools#extract(String, String, String)}.
     */
    @Test
    public void testExtract() {
        listCompare( StringTools.extract( "<foo><bar<bar><baz>", "<", ">" ), "foo", "bar<bar", "baz" );
        listCompare( StringTools.extract( "fhsld jfflkfs dlk $$ sdn $foo$", "$", "$" ), "", "foo" );
        listCompare( StringTools.extract( "fhsld jfflkfs dlk $$ sdn $foo$", "$", ">" ) );
        
    }

}
