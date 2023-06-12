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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.provider.jrxml.ParameterDescription;
import org.deegree.services.wps.provider.jrxml.jaxb.process.ResourceBundle;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class TestSubreportContentProviderTest {

    private SubreportContentProvider subreportContentProvider;

    @Before
    public void createSubreportContentProvider()
                            throws Exception {
        List<JrxmlContentProvider> contentProviders = new ArrayList<JrxmlContentProvider>();
        contentProviders.add( new DataTableContentProvider( null ) );

        ResourceBundle resourceBundle = new ResourceBundle();
        resourceBundle.setName( "org.deegree.services.wps.provider.jrxml.contentprovider.testlabels" );
        resourceBundle.setPrefix( "PROP_" );
        resourceBundle.setDefaultLocale( "de" );
        resourceBundle.getSupportedLocale().add( "de" );
        resourceBundle.getSupportedLocale().add( "en" );
        contentProviders.add( new PropertiesContentProvider( null, resourceBundle ) );
        contentProviders.add( new OtherContentProvider( null ) );

        subreportContentProvider = new SubreportContentProvider(
                                                                 null,
                                                                 "SUBREPORT",
                                                                 TestOtherContentProviderTest.class.getResource( "../templateWithInlineTable_subreport1.jrxml" ),
                                                                 null );
    }

    /**
     * Test method for
     * {@link org.deegree.services.wps.provider.jrxml.contentprovider.SubreportContentProvider#inspectInputParametersFromJrxml(java.util.List, org.deegree.commons.xml.XMLAdapter, java.util.Map, java.util.List)}
     * .
     */
    @Test
    public void testInspectInputParametersFromJrxml() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put( "SUBREPORT_dir", "java.lang.String" );
        parameters.put( "SUBREPORT_datasource", "net.sf.jasperreports.engine.JRDataSource" );
        List<JAXBElement<? extends ProcessletInputDefinition>> inputs = new ArrayList<JAXBElement<? extends ProcessletInputDefinition>>();

        XMLAdapter jrxmlAdapter = new XMLAdapter(
                                                  TestOtherContentProviderTest.class.getResourceAsStream( "../templateWithInlineTable.jrxml" ) );
        List<String> handledParameters = new ArrayList<String>();
        subreportContentProvider.inspectInputParametersFromJrxml( new HashMap<String, ParameterDescription>(), inputs,
                                                                  jrxmlAdapter, parameters, handledParameters );

        // includes parameters of the subreport
        assertEquals( 2, handledParameters.size() );
        // table, title parameter (from resource bundle), title
        assertEquals( 3, inputs.size() );
    }

}
