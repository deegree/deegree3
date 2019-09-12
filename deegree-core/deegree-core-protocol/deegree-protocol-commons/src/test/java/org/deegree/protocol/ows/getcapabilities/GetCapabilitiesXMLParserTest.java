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
package org.deegree.protocol.ows.getcapabilities;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.deegree.commons.tom.ows.Version;
import org.junit.Test;

/**
 * Test cases for {@link GetCapabilitiesXMLParser}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GetCapabilitiesXMLParserTest {

    /**
     * Tests the parsing of an OWS 1.0.0 GetCapabilities document.
     */
    @Test
    public void testParsing100() {
        URL docURL = GetCapabilitiesXMLParserTest.class.getResource( "../capabilities/GetCapabilitiesOWS100.xml" );
        GetCapabilitiesXMLParser parser = new GetCapabilitiesXMLParser();
        parser.load( docURL );
        GetCapabilities request = parser.parse100();
        assertNotNull( request );

        // check accept versions
        assertNotNull( request.getAcceptVersions() );
        assertEquals( 2, request.getAcceptVersions().size() );
        assertEquals( Version.parseVersion( "1.0.0" ), request.getAcceptVersionsAsVersions().get( 1 ) );
        assertEquals( Version.parseVersion( "1.1.0" ), request.getAcceptVersionsAsVersions().get( 0 ) );

        // check sections
        assertNotNull( request.getSections() );
        assertEquals( 3, request.getSections().size() );
        assertTrue( request.getSections().contains( "ServiceIdentification" ) );
        assertTrue( request.getSections().contains( "ServiceProvider" ) );
        assertTrue( request.getSections().contains( "OperationsMetadata" ) );

        // check accept formats
        assertNotNull( request.getAcceptFormats() );
        assertEquals( 1, request.getAcceptFormats().size() );
        assertEquals( "text/xml", request.getAcceptFormats().iterator().next() );

        assertNull( request.getUpdateSequence() );
    }

    /**
     * Tests the parsing of an OWS 1.1.0 GetCapabilities document.
     */
    @Test
    public void testParsing110() {
        URL docURL = GetCapabilitiesXMLParserTest.class.getResource( "../capabilities/GetCapabilitiesOWS110.xml" );
        GetCapabilitiesXMLParser parser = new GetCapabilitiesXMLParser();
        parser.load( docURL );
        GetCapabilities request = parser.parse110();
        assertNotNull( request );

        // check accept versions
        assertNotNull( request.getAcceptVersions() );
        assertEquals( 3, request.getAcceptVersions().size() );
        assertEquals( Version.parseVersion( "1.0.0" ), request.getAcceptVersionsAsVersions().get( 0 ) );
        assertEquals( Version.parseVersion( "2.0.0" ), request.getAcceptVersionsAsVersions().get( 1 ) );

        // check sections
        assertNotNull( request.getSections() );
        assertEquals( 3, request.getSections().size() );
        assertTrue( request.getSections().contains( "ServiceIdentification" ) );
        assertTrue( request.getSections().contains( "ServiceProvider" ) );
        assertTrue( request.getSections().contains( "OperationsMetadata" ) );

        // check accept formats
        assertNotNull( request.getAcceptFormats() );
        assertEquals( 1, request.getAcceptFormats().size() );
        assertEquals( "text/xml", request.getAcceptFormats().iterator().next() );

        assertNull( request.getUpdateSequence() );
    }

    /**
     * Tests the parsing of an OWS 2.0.0 GetCapabilities document.
     */
    @Test
    public void testParsing200() {
        URL docURL = GetCapabilitiesXMLParserTest.class.getResource( "../capabilities/GetCapabilitiesOWS200.xml" );
        GetCapabilitiesXMLParser parser = new GetCapabilitiesXMLParser();
        parser.load( docURL );
        GetCapabilities request = parser.parse200();

        // check accept versions
        List<String> acceptVersions = request.getAcceptVersions();
        assertThat( acceptVersions.size(), is( 3 ) );
        assertThat( acceptVersions, hasItems( "1.0.0", "2.0.0", "1.1.0" ) );

        // check sections
        Set<String> sections = request.getSections();
        assertThat( sections.size(), is( 3 ) );
        assertThat( sections, hasItems( "ServiceIdentification", "ServiceProvider", "OperationsMetadata" ) );

        // check accept formats
        Set<String> acceptFormats = request.getAcceptFormats();
        assertThat( acceptFormats.size(), is( 1 ) );
        assertThat( acceptFormats, hasItems( "text/xml" ) );

        // check accept formats
        List<String> acceptLanguages = request.getAcceptLanguages();
        assertThat( acceptLanguages.size(), is( 2 ) );
        assertThat( acceptLanguages, hasItems( "en", "de" ) );

        assertThat( request.getUpdateSequence(), is( "2" ) );
    }

}