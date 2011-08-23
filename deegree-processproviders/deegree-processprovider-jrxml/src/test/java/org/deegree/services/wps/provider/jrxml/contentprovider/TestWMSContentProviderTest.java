//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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

import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.getAsCodeType;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.getAsLanguageStringType;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.nsContext;
import static org.deegree.services.wps.provider.jrxml.contentprovider.WMSContentProvider.MIME_TYPE;
import static org.deegree.services.wps.provider.jrxml.contentprovider.WMSContentProvider.SCHEMA;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.io.StreamBufferStore;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.process.jaxb.java.ComplexFormatType;
import org.deegree.process.jaxb.java.ComplexInputDefinition;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.input.ComplexInputImpl;
import org.deegree.services.wps.input.EmbeddedComplexInput;
import org.deegree.services.wps.input.ProcessletInput;
import org.junit.Test;

/**
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class TestWMSContentProviderTest {

    /**
     * Test method for
     * {@link org.deegree.services.wps.provider.jrxml.contentprovider.WMSContentProvider#inspectInputParametersFromJrxml(java.util.List, java.util.List, java.util.List)}
     * .
     */
    @Test
    public void testInspectInputParametersFromJrxml() {
        WMSContentProvider wmsContentProvider = new WMSContentProvider();
        List<String> textParameters = new ArrayList<String>();
        textParameters.add( "parameterXYZ" );
        List<String> imgParameters = new ArrayList<String>();
        imgParameters.add( "wmsMAP1_map" );
        imgParameters.add( "wmsMAP1_legend" );
        imgParameters.add( "wmsMAP1_usualParam" );
        List<JAXBElement<? extends ProcessletInputDefinition>> inputs = new ArrayList<JAXBElement<? extends ProcessletInputDefinition>>();
        wmsContentProvider.inspectInputParametersFromJrxml( inputs, textParameters, imgParameters );

        assertEquals( 1, textParameters.size() );
        assertEquals( 1, imgParameters.size() );

        assertEquals( 1, inputs.size() );
        assertEquals( "MAP1", inputs.get( 0 ).getValue().getIdentifier().getValue() );
    }

    /**
     * Test method for
     * {@link org.deegree.services.wps.provider.jrxml.contentprovider.WMSContentProvider#prepareJrxmlAndReadInputParameters(java.io.InputStream, java.util.Map, org.deegree.services.wps.ProcessletInputs, java.util.List)}
     * .
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws FactoryConfigurationError
     * @throws XMLStreamException
     */
    @Test
    public void testPrepareJrxmlAndReadInputParameters()
                            throws URISyntaxException, IOException, XMLStreamException, FactoryConfigurationError {
        WMSContentProvider wmsContentProvider = new WMSContentProvider();

        List<CodeType> processedIds = new ArrayList<CodeType>();
        InputStream jrxml = TestWMSContentProviderTest.class.getResourceAsStream( "../testWPSreportTemplate.jrxml" );
        Map<String, Object> params = new HashMap<String, Object>();
        List<ProcessletInput> inputs = new ArrayList<ProcessletInput>();
        ProcessletInputs in = new ProcessletInputs( inputs );

        ComplexInputDefinition definition = new ComplexInputDefinition();
        definition.setTitle( getAsLanguageStringType( "MAP" ) );
        definition.setIdentifier( getAsCodeType( "MAP" ) );
        ComplexFormatType format = new ComplexFormatType();
        // TODO
        format.setEncoding( "UTF-8" );
        format.setMimeType( MIME_TYPE );
        format.setSchema( SCHEMA );
        definition.setDefaultFormat( format );
        definition.setMaxOccurs( BigInteger.valueOf( 1 ) );
        definition.setMinOccurs( BigInteger.valueOf( 0 ) );

        // URL resource = TestWMSContentProviderTest.class.getResource( "store" );
        // File f = new File( resource.toExternalForm() );
        File f = File.createTempFile( "tmpStore", "" );
        StreamBufferStore store = new StreamBufferStore( 1024, f );

        // LOG.debug( "Storing embedded ComplexInput as XML" );
        InputStream complexInput = TestWMSContentProviderTest.class.getResourceAsStream( "complexInput" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( complexInput );
        XMLStreamWriter xmlWriter = null;
        try {
            xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( store );
            XMLStreamUtils.skipStartDocument( xmlReader );
            XMLAdapter.writeElement( xmlWriter, xmlReader );
        } finally {
            try {
                xmlReader.close();
            } catch ( XMLStreamException e ) {
                // nothing to do
            }
            try {
                xmlWriter.close();
            } catch ( XMLStreamException e ) {
                // nothing to do
            }
            IOUtils.closeQuietly( store );
        }

        ComplexInputImpl mapProcesslet = new EmbeddedComplexInput( definition, new LanguageString( "title", "ger" ),
                                                                   new LanguageString( "summary", "ger" ), format,
                                                                   store );

        inputs.add( mapProcesslet );
        jrxml = wmsContentProvider.prepareJrxmlAndReadInputParameters( jrxml, params, in, processedIds );

        assertEquals( 2, params.size() );
        assertEquals( 1, processedIds.size() );
        XMLAdapter a = new XMLAdapter( jrxml );
        String[] elements = a.getNodesAsStrings( a.getRootElement(),
                                                 new XPath(
                                                            "/jasper:jasperReport/jasper:detail/jasper:band/jasper:frame/jasper:staticText/jasper:text/text()",
                                                            nsContext ) );
        for ( int i = 0; i < elements.length; i++ ) {
            System.out.println( elements[i] );
        }
        System.out.println( a.toString() );
        assertEquals( 4, elements.length );
    }

}
