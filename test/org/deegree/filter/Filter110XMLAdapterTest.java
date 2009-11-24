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

import java.io.IOException;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.LogicalOperator;
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
                            throws XMLStreamException, FactoryConfigurationError, IOException {
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

    @Test (expected=XMLParsingException.class)
    public void parseMixedIdFilter()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        parse( "testfilter_110_id_mixed.xml" );
    }

    @Test
    public void parseOperatorFilter()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter filter = parse( "testfilter_110_operator.xml" );
        Assert.assertNotNull( filter );
        Assert.assertEquals( Filter.Type.OPERATOR_FILTER, filter.getType() );
        OperatorFilter operatorFilter = (OperatorFilter) filter;
        Assert.assertEquals( Operator.Type.LOGICAL, operatorFilter.getOperator().getType() );
        LogicalOperator logicalOperator = (LogicalOperator) operatorFilter.getOperator();
        Assert.assertEquals( LogicalOperator.SubType.AND, logicalOperator.getSubType() );
        And and = (And) logicalOperator;
        Assert.assertEquals( 2, and.getSize() );
        Assert.assertEquals( Operator.Type.COMPARISON, and.getParameter( 0 ).getType() );
        ComparisonOperator param1Oper = (ComparisonOperator) and.getParameter( 0 );
        Assert.assertEquals( ComparisonOperator.SubType.PROPERTY_IS_GREATER_THAN, param1Oper.getSubType() );
        Assert.assertEquals( Operator.Type.COMPARISON, and.getParameter( 1 ).getType() );
        ComparisonOperator param2Oper = (ComparisonOperator) and.getParameter( 1 );
        Assert.assertEquals( ComparisonOperator.SubType.PROPERTY_IS_EQUAL_TO, param2Oper.getSubType() );
    
    }

    @Test(expected = XMLParsingException.class)
    public void parseBrokenIdFilterDocument()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        parse( "testfilter_110_id.invalid_xml" );
    }

    @Test(expected = XMLParsingException.class)
    public void parseBrokenIdFilterDocument2()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        parse( "testfilter_110_id2.invalid_xml" );
    }

    @Test
    public void parseAndExportFilterDocument()
                            throws XMLStreamException, FactoryConfigurationError, IOException {

        Filter filter = parse( "testfilter_110_operator.xml" );

        XMLMemoryStreamWriter writer = new XMLMemoryStreamWriter();
        Filter110XMLEncoder.export( filter, writer.getXMLStreamWriter() );

        String schemaLocation = "http://schemas.opengis.net/filter/1.1.0/filter.xsd";
        XMLAssert.assertValidity( writer.getReader(), schemaLocation );
    }

    private Filter parse( String resourceName )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        URL url = Filter110XMLAdapterTest.class.getResource( "testdata/v110/" + resourceName );
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( url.toString(),
                                                                                         url.openStream() );
        xmlStream.nextTag();
        Location loc = xmlStream.getLocation();
        System.out.println( loc.getLineNumber() );
        System.out.println( loc.getSystemId() );
        System.out.println( loc.getColumnNumber() );
        return Filter110XMLDecoder.parse( xmlStream );
    }
    
    @Test
    public void parseBeyondFilter()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter filter = parse( "testfilter15.xml" );
        Assert.assertNotNull( filter );
        
    }
    
    @Test
    public void parseDisjointFilter()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter filter = parse( "testfilter16.xml" );
        Assert.assertNotNull( filter );
        
    }
    
    @Test
    public void parseContainsFilter()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter filter = parse( "testfilter17.xml" );
        Assert.assertNotNull( filter );
        
    }
    
    @Test
    public void parseCrossesFilter()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter filter = parse( "testfilter18.xml" );
        Assert.assertNotNull( filter );
        
    }
    
    @Test
    public void parseDWithinFilter()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter filter = parse( "testfilter19.xml" );
        Assert.assertNotNull( filter );
        
    }
    
    @Test
    public void parseIntersectsFilter()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter filter = parse( "testfilter20.xml" );
        Assert.assertNotNull( filter );
        
    }
    
    @Test
    public void parseEqualsFilter()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter filter = parse( "testfilter21.xml" );
        Assert.assertNotNull( filter );
        
    }
    
    @Test
    public void parseOverlapsFilter()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter filter = parse( "testfilter22.xml" );
        Assert.assertNotNull( filter );
        
    }
    
    @Test
    public void parseTouchesFilter()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter filter = parse( "testfilter23.xml" );
        Assert.assertNotNull( filter );
        
    }
    
    @Test
    public void parseWithinFilter()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter filter = parse( "testfilter24.xml" );
        Assert.assertNotNull( filter );
        
    }
}
