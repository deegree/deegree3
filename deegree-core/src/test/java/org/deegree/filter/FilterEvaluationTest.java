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

import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.gml.GMLVersion.GML_32;

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.xpath.FeatureXPathEvaluator;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the correct evaluation of filter expressions.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class FilterEvaluationTest {

    private FeatureCollection fc;

    private SimpleNamespaceContext nsContext;

    private static final String BASE_DIR = "../gml/feature/testdata/features/";

    @Before
    public void setUp()
                            throws Exception {

        String schemaURL = this.getClass().getResource( "../gml/feature/testdata/schema/Philosopher.xsd" ).toString();
        ApplicationSchemaXSDDecoder xsdAdapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, null, schemaURL );
        ApplicationSchema schema = xsdAdapter.extractFeatureTypeSchema();

        URL docURL = this.getClass().getResource( "../gml/feature/testdata/features/Philosopher_FeatureCollection.xml" );
        GMLStreamReader gmlStream = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, docURL );
        gmlStream.setApplicationSchema( schema );
        fc = (FeatureCollection) gmlStream.readFeature();
        gmlStream.getIdContext().resolveLocalRefs();

        nsContext = new SimpleNamespaceContext();
        nsContext.addNamespace( "gml", "http://www.opengis.net/gml" );
        nsContext.addNamespace( "app", "http://www.deegree.org/app" );
    }

    @Test
    public void filterCollection1()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
        Filter filter = parseFilter( "testfilter1.xml" );
        assertResultSet( fc.getMembers( filter, new FeatureXPathEvaluator( GML_31 ) ), "PHILOSOPHER_7" );
    }

    @Test
    public void filterCollection2()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
        Filter filter = parseFilter( "testfilter2.xml" );
        assertResultSet( fc.getMembers( filter, new FeatureXPathEvaluator( GML_31 ) ), "PHILOSOPHER_2" );
    }

    @Test
    public void filterCollection3()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
        Filter filter = parseFilter( "testfilter3.xml" );
        assertResultSet( fc.getMembers( filter, new FeatureXPathEvaluator( GML_31 ) ), "PHILOSOPHER_5", "PHILOSOPHER_6" );
    }

    @Test
    public void filterCollection4()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
        Filter filter = parseFilter( "testfilter4.xml" );
        assertResultSet( fc.getMembers( filter, new FeatureXPathEvaluator( GML_31 ) ), "PHILOSOPHER_1", "PHILOSOPHER_2" );
    }

    @Test
    public void filterCollection5()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
        Filter filter = parseFilter( "testfilter5.xml" );
        assertResultSet( fc.getMembers( filter, new FeatureXPathEvaluator( GML_31 ) ), "PHILOSOPHER_1", "PHILOSOPHER_2" );
    }

    @Test
    public void filterCollection6()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
        Filter filter = parseFilter( "testfilter6.xml" );
        assertResultSet( fc.getMembers( filter, new FeatureXPathEvaluator( GML_31 ) ), "PHILOSOPHER_1" );
    }

    @Test
    public void filterCollection7()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError,
                            JaxenException {
        Filter filter = parseFilter( "testfilter7.xml" );
        assertResultSet( fc.getMembers( filter, new FeatureXPathEvaluator( GML_31 ) ), "PHILOSOPHER_1" );
    }

    @Test
    public void filterCollection8()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError,
                            JaxenException {
        Filter filter = parseFilter( "testfilter8.xml" );
        assertResultSet( fc.getMembers( filter, new FeatureXPathEvaluator( GML_31 ) ), "PHILOSOPHER_1" );
    }

    @Test
    public void filterCollection9()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError,
                            JaxenException {
        Filter filter = parseFilter( "testfilter9.xml" );
        assertResultSet( fc.getMembers( filter, new FeatureXPathEvaluator( GML_31 ) ), "PHILOSOPHER_6" );
    }

    @Test
    public void filterCollection10()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError,
                            JaxenException {
        Filter filter = parseFilter( "testfilter10.invalid_xml" );
        assertResultSet( fc.getMembers( filter, new FeatureXPathEvaluator( GML_31 ) ) );
    }

    @Test
    public void filterCollection11()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError,
                            JaxenException {
        Filter filter = parseFilter( "testfilter11.xml" );
        assertResultSet( fc.getMembers( filter, new FeatureXPathEvaluator( GML_31 ) ), "PHILOSOPHER_1", "PHILOSOPHER_2" );
    }

    @Test
    public void filterCollection12()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError,
                            JaxenException {
        Filter filter = parseFilter( "testfilter12.xml" );
        assertResultSet( fc.getMembers( filter, new FeatureXPathEvaluator( GML_31 ) ), "PHILOSOPHER_1",
                         "PHILOSOPHER_2", "PHILOSOPHER_3" );
    }

    @Test
    public void filterCollection13()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
        Filter filter = parseFilter( "testfilter13.xml" );
        assertResultSet( fc.getMembers( filter, new FeatureXPathEvaluator( GML_31 ) ), "PHILOSOPHER_7" );
    }

    @Test
    public void filterCollection14()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
        Filter filter = parseFilter( "testfilter14.xml" );
        assertResultSet( fc.getMembers( filter, new FeatureXPathEvaluator( GML_31 ) ), "PHILOSOPHER_1" );
    }
    
    @Test
    public void filterCollection25()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
        Filter filter = parseFilter( "testfilter25.xml" );
        assertResultSet( fc.getMembers( filter, new FeatureXPathEvaluator( GML_31 ) ), "PHILOSOPHER_1" );
    }
    
    @Test
    public void filterCollection26()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
        Filter filter = parseFilter( "testfilter26.xml" );
        assertResultSet( fc.getMembers( filter, new FeatureXPathEvaluator( GML_31 ) ), "PHILOSOPHER_1" );
    }
    
    @Test
    public void filterCollection27()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
        Filter filter = parseFilter( "testfilter27.xml" );
        assertResultSet( fc.getMembers( filter, new FeatureXPathEvaluator( GML_32 ) ), "PHILOSOPHER_1" );
    }

    private void assertResultSet( FeatureCollection fc, String... expectedIds ) {
        Assert.assertEquals( expectedIds.length, fc.size() );
        Set<String> ids = new HashSet<String>();
        for ( Feature feature : fc ) {
            ids.add( feature.getId() );
        }
        for ( String string : expectedIds ) {
            Assert.assertTrue( ids.contains( string ) );
        }
    }

    private Filter parseFilter( String resourceName )
                            throws XMLStreamException, FactoryConfigurationError {
        InputStream is = FilterEvaluationTest.class.getResourceAsStream( "testdata/v110/" + resourceName );
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( is );
        xmlStream.nextTag();
        return Filter110XMLDecoder.parse( xmlStream );
    }
}
