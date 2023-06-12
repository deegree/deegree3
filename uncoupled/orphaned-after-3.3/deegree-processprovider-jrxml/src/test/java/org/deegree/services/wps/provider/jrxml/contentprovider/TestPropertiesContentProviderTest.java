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

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.input.LiteralInputImpl;
import org.deegree.services.wps.input.ProcessletInput;
import org.deegree.services.wps.provider.jrxml.ParameterDescription;
import org.deegree.services.wps.provider.jrxml.jaxb.process.ResourceBundle;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class TestPropertiesContentProviderTest {

    private PropertiesContentProvider propertiesContentProvider;

    private Map<String, String> parameters = new HashMap<String, String>();

    @Before
    public void createPropertiesContentProvider()
                            throws Exception {
        ResourceBundle resourceBundle = new ResourceBundle();
        resourceBundle.setName( "org.deegree.services.wps.provider.jrxml.contentprovider.testlabels" );
        resourceBundle.setPrefix( "PROP_" );
        resourceBundle.setDefaultLocale( "de" );
        resourceBundle.getSupportedLocale().add( "de" );
        resourceBundle.getSupportedLocale().add( "en" );
        propertiesContentProvider = new PropertiesContentProvider( null, resourceBundle );

        parameters.put( "DATE", "java.util.Date" );
        parameters.put( "DESCRIPTION", "java.lang.String" );
        parameters.put( "MAPSCALE", "java.lang.Integer" );
        parameters.put( "PROP_author", "java.lang.String" );
        parameters.put( "PROP_date", "java.lang.String" );
        parameters.put( "PROP_theme", "java.lang.String" );
        parameters.put( "PROP_description", "java.lang.String" );
    }

    /**
     * Test method for
     * {@link org.deegree.services.wps.provider.jrxml.contentprovider.PropertiesContentProvider#inspectInputParametersFromJrxml(java.util.List, org.deegree.commons.xml.XMLAdapter, java.util.Map, java.util.List)}
     * .
     */
    @Test
    public void testInspectInputParametersFromJrxml() {

        List<JAXBElement<? extends ProcessletInputDefinition>> inputs = new ArrayList<JAXBElement<? extends ProcessletInputDefinition>>();

        XMLAdapter jrxmlAdapter = new XMLAdapter(
                                                  TestPropertiesContentProviderTest.class.getResourceAsStream( "../templateWithPropsFromResourceBundle.jrxml" ) );
        List<String> handledParameters = new ArrayList<String>();
        propertiesContentProvider.inspectInputParametersFromJrxml( new HashMap<String, ParameterDescription>(), inputs,
                                                                   jrxmlAdapter, parameters, handledParameters );

        assertEquals( 4, handledParameters.size() );
        assertEquals( 1, inputs.size() );
    }

    /**
     * Test method for
     * {@link org.deegree.services.wps.provider.jrxml.contentprovider.PropertiesContentProvider#prepareJrxmlAndReadInputParameters(java.io.InputStream, java.util.Map, org.deegree.services.wps.ProcessletInputs, java.util.List, java.util.Map)}
     * .
     * 
     * @throws ProcessletException
     */
    @Test
    public void testPrepareJrxmlAndReadInputParameters()
                            throws ProcessletException {
        List<CodeType> processedIds = new ArrayList<CodeType>();
        InputStream jrxml = TestDataTableContentProviderTest.class.getResourceAsStream( "../templateWithPropsFromResourceBundle.jrxml" );
        Map<String, Object> params = new HashMap<String, Object>();
        List<ProcessletInput> inputs = new ArrayList<ProcessletInput>();
        inputs.add( new LiteralInputImpl(
                                          propertiesContentProvider.getInputDefinition( new HashMap<String, ParameterDescription>() ),
                                          null, null, "de", null ) );
        ProcessletInputs in = new ProcessletInputs( inputs );
        propertiesContentProvider.prepareJrxmlAndReadInputParameters( jrxml, params, in, processedIds, parameters );

        assertEquals( 1, processedIds.size() );
        assertEquals( 4, params.size() );
    }
}
