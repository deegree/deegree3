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
package org.deegree.filter;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.xerces.xni.parser.XMLInputSource;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.filter.xml.Filter110XMLEncoder;
import org.deegree.junit.XMLAssert;
import org.deegree.junit.XMLMemoryStreamWriter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the correct parsing and exporting of Filter Encoding 1.1.0 documents.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class Filter110XMLAdapterTest {

    @Test
    public void parseIdFilter()
                            throws XMLStreamException, FactoryConfigurationError {
        Filter filter = parse( "testfilter_110_id.xml" );
        assertNotNull( filter );
        Assert.assertEquals( Filter.Type.ID_FILTER, filter.getType() );
        IdFilter idFilter = (IdFilter) filter;
        Assert.assertEquals( 4, idFilter.getMatchingIds().size() );
        Assert.assertTrue( idFilter.getMatchingIds().contains( "PHILOSOPHER_966" ) );
        Assert.assertTrue( idFilter.getMatchingIds().contains( "PHILOSOPHER_967" ) );
        Assert.assertTrue( idFilter.getMatchingIds().contains( "PHILOSOPHER_968" ) );
        Assert.assertTrue( idFilter.getMatchingIds().contains( "PHILOSOPHER_969" ) );
    }

    @Test
    public void parseMixedIdFilter()
                            throws XMLStreamException, FactoryConfigurationError {
        Filter filter = parse( "testfilter_110_id_mixed.xml" );
        assertNotNull( filter );
        Assert.assertEquals( Filter.Type.ID_FILTER, filter.getType() );
        IdFilter idFilter = (IdFilter) filter;
        Assert.assertEquals( 4, idFilter.getMatchingIds().size() );
        Assert.assertTrue( idFilter.getMatchingIds().contains( "PHILOSOPHER_966" ) );
        Assert.assertTrue( idFilter.getMatchingIds().contains( "PHILOSOPHER_967" ) );
        Assert.assertTrue( idFilter.getMatchingIds().contains( "PHILOSOPHER_968" ) );
        Assert.assertTrue( idFilter.getMatchingIds().contains( "PHILOSOPHER_969" ) );
    }

//    @Test
//    public void parseFilterDocument()
//                            throws XMLStreamException, FactoryConfigurationError {
//        Filter110XMLDecoder adapter = new Filter110XMLDecoder();
//        Filter filter = parse( "testfilter_110.xml" );
//        Assert.assertNotNull( filter );
//    }

    @Test(expected = XMLParsingException.class)
    public void parseBrokenIdFilterDocument() {
        Filter110XMLDecoder adapter = new Filter110XMLDecoder();
        adapter.load( Filter110XMLAdapterTest.class.getResourceAsStream( "testfilter_110_id2.invalid_xml" ) );
        adapter.parse();
    }

    @Test(expected = XMLParsingException.class)
    public void parseBrokenIdFilterDocument2() {
        Filter110XMLDecoder adapter = new Filter110XMLDecoder();
        adapter.load( Filter110XMLAdapterTest.class.getResourceAsStream( "testfilter_110_id2.invalid_xml" ) );
        adapter.parse();
    }

    @Test
    public void parseAndExportFilterDocument()
                            throws XMLStreamException {

        Filter110XMLDecoder adapter = new Filter110XMLDecoder();
        adapter.load( Filter110XMLAdapterTest.class.getResourceAsStream( "testfilter_110.xml" ) );
        Filter filter = adapter.parse();

        XMLMemoryStreamWriter writer = new XMLMemoryStreamWriter();
        Filter110XMLEncoder.export( filter, writer.getXMLStreamWriter() );

        String schemaLocation = "http://schemas.opengis.net/filter/1.1.0/filter.xsd";
        XMLAssert.assertValidity( writer.getReader(), schemaLocation );
    }

    private Filter parse( String resourceName )
                            throws XMLStreamException, FactoryConfigurationError {
        InputStream is = Filter110XMLAdapterTest.class.getResourceAsStream( resourceName );
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( is );
        xmlStream.nextTag();
        return Filter110XMLDecoder.parse( xmlStream );
    }
}
