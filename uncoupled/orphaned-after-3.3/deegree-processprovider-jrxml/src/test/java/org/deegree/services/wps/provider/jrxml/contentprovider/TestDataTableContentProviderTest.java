/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.services.wps.provider.jrxml.contentprovider;

import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.nsContext;
import static org.deegree.services.wps.provider.jrxml.contentprovider.DataTableContentProvider.DETAIL_SUFFIX;
import static org.deegree.services.wps.provider.jrxml.contentprovider.DataTableContentProvider.HEADER_SUFFIX;
import static org.deegree.services.wps.provider.jrxml.contentprovider.DataTableContentProvider.TABLE_PREFIX;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.provider.jrxml.ParameterDescription;
import org.junit.Test;

/**
 * 
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class TestDataTableContentProviderTest {

    /**
     * Test method for
     * {@link org.deegree.services.wps.provider.jrxml.contentprovider.TestDataTableContentProviderTest#inspectInputParametersFromJrxml(java.util.List, java.util.List, java.util.List)}
     * .
     */
    @Test
    public void testInspectInputParametersFromJrxml() {
        DataTableContentProvider dataTableContentProvider = getProvider();
        Map<String, String> parameters = new HashMap<String, String>();
        List<JAXBElement<? extends ProcessletInputDefinition>> inputs = new ArrayList<JAXBElement<? extends ProcessletInputDefinition>>();
        XMLAdapter adapter = new XMLAdapter(
                                             TestDataTableContentProviderTest.class.getResourceAsStream( "../templateWithTable.jrxml" ) );
        List<String> handledParams = new ArrayList<String>();
        dataTableContentProvider.inspectInputParametersFromJrxml( new HashMap<String, ParameterDescription>(), inputs,
                                                                  adapter, parameters, handledParams );

        assertEquals( 0, parameters.size() );

        // handled
        assertEquals( 0, handledParams.size() );
        assertEquals( 1, inputs.size() );
        assertEquals( "REPORT", inputs.get( 0 ).getValue().getIdentifier().getValue() );
    }

    private DataTableContentProvider getProvider() {
        return new DataTableContentProvider( null );
    }

    /**
     * Test method for
     * {@link org.deegree.services.wps.provider.jrxml.contentprovider.DataTableContentProvider#prepareJrxmlAndReadInputParameters(java.io.InputStream, java.util.Map, org.deegree.services.wps.ProcessletInputs, java.util.List)}
     * .
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws FactoryConfigurationError
     * @throws XMLStreamException
     * @throws ProcessletException
     */
    @Test
    public void testPrepareJrxmlAndReadInputParameters()
                            throws URISyntaxException, IOException, XMLStreamException, FactoryConfigurationError,
                            ProcessletException {
        DataTableContentProvider tableContentProvider = getProvider();

        List<CodeType> processedIds = new ArrayList<CodeType>();
        Pair<InputStream, Boolean> jrxml = new Pair<InputStream, Boolean>(
                                                                           TestDataTableContentProviderTest.class.getResourceAsStream( "../templateWithTable.jrxml" ),
                                                                           false );
        Map<String, Object> params = new HashMap<String, Object>();
        ProcessletInputs in = Utils.getInputs( "REPORT",
                                               DataTableContentProvider.MIME_TYPE,
                                               DataTableContentProvider.SCHEMA,
                                               TestDataTableContentProviderTest.class.getResourceAsStream( "complexInputTABLE" ) );
        tableContentProvider.setTableId( "REPORT" );
        jrxml = tableContentProvider.prepareJrxmlAndReadInputParameters( jrxml.first, params, in, processedIds,
                                                                         new HashMap<String, String>() );
        assertEquals( 1, params.size() );
        assertEquals( 1, processedIds.size() );
        XMLAdapter a = new XMLAdapter( jrxml.first );
        String[] fieldNames = a.getNodesAsStrings( a.getRootElement(),
                                                   new XPath( "/jasper:jasperReport/jasper:field/@name", nsContext ) );
        List<String> detailFields = new ArrayList<String>();
        List<String> headerFields = new ArrayList<String>();
        for ( String field : fieldNames ) {
            if ( field.matches( TABLE_PREFIX + "[a-zA-Z0-9]*_(" + DETAIL_SUFFIX + ")[0-9]*" ) )
                detailFields.add( field );
            else if ( field.matches( TABLE_PREFIX + "[a-zA-Z0-9]*_(" + HEADER_SUFFIX + ")[0-9]*" ) )
                headerFields.add( field );
        }

        String[] textFieldNames = a.getNodesAsStrings( a.getRootElement(),
                                                       new XPath( ".//jasper:textField/jasper:textFieldExpression",
                                                                  nsContext ) );

        List<String> detailTextFields = new ArrayList<String>();
        List<String> headerTextFields = new ArrayList<String>();
        for ( String field : textFieldNames ) {
            if ( field.matches( "\\$F\\{" + TABLE_PREFIX + "[a-zA-Z0-9]*_(" + DETAIL_SUFFIX + ")[0-9]*\\}" ) )
                detailTextFields.add( field );
            else if ( field.matches( "\\$F\\{" + TABLE_PREFIX + "[a-zA-Z0-9]*_(" + HEADER_SUFFIX + ")[0-9]*\\}" ) )
                headerTextFields.add( field );
        }

        assertEquals( 4, detailFields.size() );
        assertEquals( 4, headerFields.size() );

        assertEquals( 4, detailTextFields.size() );
        assertEquals( 4, headerTextFields.size() );
    }

}
