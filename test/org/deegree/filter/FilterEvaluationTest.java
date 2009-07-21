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

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.gml.GMLFeatureParser;
import org.deegree.feature.gml.GMLIdContext;
import org.deegree.feature.gml.schema.ApplicationSchemaXSDAdapter;
import org.deegree.feature.gml.schema.GMLVersion;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.xml.Filter110XMLAdapter;
import org.jaxen.SimpleNamespaceContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FilterEvaluationTest {

    private FeatureCollection fc;

    private SimpleNamespaceContext nsContext;

    private static final String BASE_DIR = "gml/testdata/features/";

    @Before
    public void setUp()
                            throws Exception {
        String schemaURL = this.getClass().getResource( "../feature/gml/schema/Philosopher_typesafe.xsd" ).toString();
        ApplicationSchemaXSDAdapter xsdAdapter = new ApplicationSchemaXSDAdapter( schemaURL,
                                                                                        GMLVersion.GML_31 );
        ApplicationSchema schema = xsdAdapter.extractFeatureTypeSchema();
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureParser gmlAdapter = new GMLFeatureParser( schema, idContext );

        URL docURL = this.getClass().getResource( "../feature/gml/testdata/features/Philosopher_FeatureCollection.xml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        fc = (FeatureCollection) gmlAdapter.parseFeature( new XMLStreamReaderWrapper( xmlReader, docURL.toString() ),
                                                          null);
        idContext.resolveXLinks( schema );

        nsContext = new SimpleNamespaceContext();
        nsContext.addNamespace( "gml", "http://www.opengis.net/gml" );
        nsContext.addNamespace( "app", "http://www.deegree.org/app" );
    }

    @Test
    public void filterCollection1()
                            throws FilterEvaluationException {
        Filter110XMLAdapter adapter = new Filter110XMLAdapter();
        adapter.load( FilterEvaluationTest.class.getResourceAsStream( "testdata/testfilter1.xml" ) );
        Filter filter = adapter.parse();
        Assert.assertNotNull( filter );

        FeatureCollection filteredCollection = fc.getMembers( filter );
        Assert.assertEquals (1, filteredCollection.size());
        Assert.assertEquals ("Albert Camus", filteredCollection.iterator().next().getPropertyValue( QName.valueOf( "{http://www.deegree.org/app}name" )));
    }

    @Test
    public void filterCollection2()
                            throws FilterEvaluationException {
        Filter110XMLAdapter adapter = new Filter110XMLAdapter();
        adapter.load( FilterEvaluationTest.class.getResourceAsStream( "testdata/testfilter2.xml" ) );
        Filter filter = adapter.parse();
        Assert.assertNotNull( filter );

        FeatureCollection filteredCollection = fc.getMembers( filter );
        for ( Feature feature : filteredCollection ) {
            System.out.println( "HUHU: " + feature.getId() );
        }
    }

    @Test
    public void filterCollection3()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
        Filter110XMLAdapter adapter = new Filter110XMLAdapter();
        adapter.load( FilterEvaluationTest.class.getResourceAsStream( "testdata/testfilter3.xml" ) );
        Filter filter = adapter.parse();
        Assert.assertNotNull( filter );

        FeatureCollection filteredCollection = fc.getMembers( filter );
        Assert.assertEquals (2, filteredCollection.size());
        Set<String> ids = new HashSet<String>();
        for ( Feature feature : filteredCollection ) {
            ids.add( feature.getId() );
        }
        Assert.assertTrue (ids.contains( "PHILOSOPHER_5" ));
        Assert.assertTrue (ids.contains( "PHILOSOPHER_6" ));
    }

    @Test
    public void filterCollection4()
                            throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
        Filter110XMLAdapter adapter = new Filter110XMLAdapter();
        adapter.load( FilterEvaluationTest.class.getResourceAsStream( "testdata/testfilter4.xml" ) );
        Filter filter = adapter.parse();
        Assert.assertNotNull( filter );

        FeatureCollection filteredCollection = fc.getMembers( filter );
        for ( Feature feature : filteredCollection ) {
            System.out.println (feature.getId());
        }
    }
}
