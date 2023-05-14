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
package org.deegree.services.wps.provider.jrxml.contentprovider.map;

import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.nsContext;
import static org.deegree.services.wps.provider.jrxml.contentprovider.map.MapContentProvider.MIME_TYPE;
import static org.deegree.services.wps.provider.jrxml.contentprovider.map.MapContentProvider.SCHEMA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.provider.jrxml.ParameterDescription;
import org.deegree.services.wps.provider.jrxml.contentprovider.Utils;
import org.deegree.services.wps.provider.jrxml.jaxb.map.Layer;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class TestMapContentProviderTest {

    /**
     * Test method for
     * {@link org.deegree.services.wps.provider.jrxml.contentprovider.map.MapContentProvider#inspectInputParametersFromJrxml(java.util.List, java.util.List, java.util.List)}
     * .
     */
    @Test
    public void testInspectInputParametersFromJrxml() {
        MapContentProvider wmsContentProvider = getProvider();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put( "mapMAP_img", "java.lang.String" );
        parameters.put( "mapMAP_legend", "java.lang.String" );
        parameters.put( "LEGEND", "java.lang.String" );
        List<JAXBElement<? extends ProcessletInputDefinition>> inputs = new ArrayList<JAXBElement<? extends ProcessletInputDefinition>>();
        XMLAdapter adapter = new XMLAdapter(
                                             TestMapContentProviderTest.class.getResourceAsStream( "../../testWPSreportTemplate.jrxml" ) );
        List<String> handledParams = new ArrayList<String>();
        wmsContentProvider.inspectInputParametersFromJrxml( new HashMap<String, ParameterDescription>(), inputs,
                                                            adapter, parameters, handledParams );

        assertEquals( 3, parameters.size() );

        // handled
        assertEquals( 2, handledParams.size() );
        assertEquals( 1, inputs.size() );
        assertEquals( "MAP", inputs.get( 0 ).getValue().getIdentifier().getValue() );
    }

    /**
     * Test method for
     * {@link org.deegree.services.wps.provider.jrxml.contentprovider.map.MapContentProvider#prepareJrxmlAndReadInputParameters(java.io.InputStream, java.util.Map, org.deegree.services.wps.ProcessletInputs, java.util.List)}
     * .
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws FactoryConfigurationError
     * @throws XMLStreamException
     * @throws ProcessletException
     */
    @Ignore
    @Test
    public void testPrepareJrxmlAndReadInputParameters()
                            throws URISyntaxException, IOException, XMLStreamException, FactoryConfigurationError {
        MapContentProvider wmsContentProvider = getProvider();

        List<CodeType> processedIds = new ArrayList<CodeType>();
        Pair<InputStream, Boolean> jrxml = new Pair<InputStream, Boolean>(
                                                                           TestMapContentProviderTest.class.getResourceAsStream( "../../testWPSreportTemplate.jrxml" ),
                                                                           false );
        Map<String, Object> params = new HashMap<String, Object>();
        ProcessletInputs in = Utils.getInputs( "MAP", MIME_TYPE, SCHEMA,
                                               TestMapContentProviderTest.class.getResourceAsStream( "complexInput" ) );
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put( "mapMAP_img", "java.lang.String" );
        parameters.put( "mapMAP_legend", "java.lang.String" );
        parameters.put( "LEGEND", "java.lang.String" );
        try {
            jrxml = wmsContentProvider.prepareJrxmlAndReadInputParameters( jrxml.first, params, in, processedIds,
                                                                           parameters );
        } catch ( ProcessletException e ) {
            // maybe connection to demo.deegree.org failed!
            Assume.assumeNoException( e );
        }
        assertEquals( 2, params.size() );
        assertEquals( 1, processedIds.size() );
        XMLAdapter a = new XMLAdapter( jrxml.first );
        String[] elements = a.getNodesAsStrings( a.getRootElement(),
                                                 new XPath(
                                                            "/jasper:jasperReport/jasper:detail/jasper:band/jasper:frame/jasper:staticText/jasper:text/text()",
                                                            nsContext ) );
        assertEquals( 3, elements.length );
        boolean containsLake = false;
        boolean containsOverview = false;
        for ( int i = 0; i < elements.length; i++ ) {
            if ( "Lake".equals( elements[i] ) )
                containsLake = true;
            if ( "StateOverview".equals( elements[i] ) )
                containsOverview = true;
        }
        assertTrue( containsOverview );
        assertTrue( containsLake );
    }

    /**
     * Test method for
     * {@link org.deegree.services.wps.provider.jrxml.contentprovider.map.MapContentProvider#prepareJrxmlAndReadInputParameters(java.io.InputStream, java.util.Map, org.deegree.services.wps.ProcessletInputs, java.util.List)}
     * .
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws FactoryConfigurationError
     * @throws XMLStreamException
     * @throws ProcessletException
     */
    @Ignore
    @Test
    public void testPrepareJrxmlAndReadInputParametersWFS()
                            throws URISyntaxException, IOException, XMLStreamException, FactoryConfigurationError,
                            ProcessletException {
        MapContentProvider wmsContentProvider = getProvider();

        List<CodeType> processedIds = new ArrayList<CodeType>();
        Pair<InputStream, Boolean> jrxml = new Pair<InputStream, Boolean>(
                                                                           TestMapContentProviderTest.class.getResourceAsStream( "../../testWPSreportTemplate.jrxml" ),
                                                                           false );
        Map<String, Object> params = new HashMap<String, Object>();
        ProcessletInputs in = Utils.getInputs( "MAP", MIME_TYPE, SCHEMA,
                                               TestMapContentProviderTest.class.getResourceAsStream( "complexInputWFS" ) );
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put( "mapMAP_img", "java.lang.String" );
        parameters.put( "mapMAP_legend", "java.lang.String" );
        parameters.put( "LEGEND", "java.lang.String" );
        try {
            jrxml = wmsContentProvider.prepareJrxmlAndReadInputParameters( jrxml.first, params, in, processedIds,
                                                                           parameters );
        } catch ( ProcessletException e ) {
            // maybe connection to demo.deegree.org failed!
            Assume.assumeNoException( e );
        }
        assertEquals( 2, params.size() );
        assertEquals( 1, processedIds.size() );

        assertTrue( params.containsKey( "mapMAP_img" ) );
        Object value = params.get( "mapMAP_img" );
        assertTrue( value instanceof String );

        BufferedImage img = ImageIO.read( new File( (String) value ) );
        assertNotNull( img );
        assertEquals( 438, img.getWidth() );
        assertEquals( 479, img.getHeight() );

    }

    @Test
    public void testAnaylizeRequestOrder()
                            throws JAXBException, IOException {
        MapContentProvider wmsContentProvider = getProvider();

        JAXBContext jc = JAXBContext.newInstance( org.deegree.services.wps.provider.jrxml.jaxb.map.Map.class );
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        InputStream is = TestMapContentProviderTest.class.getResourceAsStream( "mapDescription.xml" );
        org.deegree.services.wps.provider.jrxml.jaxb.map.Map map = (org.deegree.services.wps.provider.jrxml.jaxb.map.Map) unmarshaller.unmarshal( is );

        List<OrderedDatasource<?>> anaylizeRequestOrder = wmsContentProvider.anaylizeRequestOrder( map.getDatasources().getWMSDatasourceOrWFSDatasource() );

        List<Pair<String, String>> expectedParts = new ArrayList<Pair<String, String>>();
        expectedParts.add( new Pair<String, String>( "http://demo.deegree.org:80/deegree-wms/services", "StateOverview" ) );
        expectedParts.add( new Pair<String, String>( "http://testing.deegree.org:80/deegree-wms/services", "River" ) );
        expectedParts.add( new Pair<String, String>( "http://demo.deegree.org:80/deegree-wms/services", "Lake" ) );
        expectedParts.add( new Pair<String, String>( "http://testing.printer.org:80/deegree-wms/services",
                                                     "Vegetation,Wood" ) );
        expectedParts.add( new Pair<String, String>( "http://localhost:8080/deegree-wms/services", "Town,SmallTown" ) );

        int index = 0;
        assertEquals( expectedParts.size(), anaylizeRequestOrder.size() );
        for ( OrderedDatasource<?> ds : anaylizeRequestOrder ) {
            assertTrue( ds instanceof WMSOrderedDatasource );
            WMSOrderedDatasource wds = (WMSOrderedDatasource) ds;

            Pair<String, String> expected = expectedParts.get( index++ );

            assertTrue( wds.datasource.getUrl().startsWith( expected.first ) );

            String layersAsString = "";
            boolean isFirst = true;
            for ( Layer l : wds.layers ) {
                if ( !isFirst ) {
                    layersAsString += ",";
                }
                layersAsString += l.getName();
                isFirst = false;
            }
            assertEquals( expected.second, layersAsString );
        }

        is.close();
    }

    private MapContentProvider getProvider() {
        return new MapContentProvider( null );
    }

    @Test
    public void testAnaylizeRequestOrderSimple()
                            throws JAXBException, IOException {
        MapContentProvider wmsContentProvider = getProvider();

        JAXBContext jc = JAXBContext.newInstance( org.deegree.services.wps.provider.jrxml.jaxb.map.Map.class );
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        InputStream is = TestMapContentProviderTest.class.getResourceAsStream( "mapDescription_simple.xml" );
        org.deegree.services.wps.provider.jrxml.jaxb.map.Map map = (org.deegree.services.wps.provider.jrxml.jaxb.map.Map) unmarshaller.unmarshal( is );

        List<OrderedDatasource<?>> anaylizeRequestOrder = wmsContentProvider.anaylizeRequestOrder( map.getDatasources().getWMSDatasourceOrWFSDatasource() );

        assertEquals( 1, anaylizeRequestOrder.size() );
        OrderedDatasource<?> ds = anaylizeRequestOrder.get( 0 );
        assertTrue( ds instanceof WMSOrderedDatasource );
        WMSOrderedDatasource wds = (WMSOrderedDatasource) ds;
        assertTrue( wds.datasource.getUrl().startsWith( "http://demo.deegree.org:80/deegree-wms/services" ) );

        assertEquals( 2, wds.layers.size() );

        assertEquals( wds.layers.get( 0 ).getName(), "StateOverview" );
        assertEquals( wds.layers.get( 1 ).getName(), "Lake" );

        is.close();
    }

}
