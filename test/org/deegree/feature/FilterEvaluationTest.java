package org.deegree.feature;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.filter.Filter;
import org.deegree.commons.filter.FilterEvaluationException;
import org.deegree.commons.filter.xml.Filter110XMLAdapter;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.feature.gml.GMLFeatureParser;
import org.deegree.feature.gml.GMLIdContext;
import org.deegree.feature.gml.schema.GMLApplicationSchemaXSDAdapter;
import org.deegree.feature.gml.schema.GMLVersion;
import org.deegree.feature.types.ApplicationSchema;
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
        String schemaURL = this.getClass().getResource( "gml/schema/Philosopher_typesafe.xsd" ).toString();
        GMLApplicationSchemaXSDAdapter xsdAdapter = new GMLApplicationSchemaXSDAdapter( schemaURL,
                                                                                        GMLVersion.VERSION_31 );
        ApplicationSchema schema = xsdAdapter.extractFeatureTypeSchema();
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureParser gmlAdapter = new GMLFeatureParser( schema, idContext );

        URL docURL = this.getClass().getResource( "gml/testdata/features/Philosopher_FeatureCollection.xml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        fc = (FeatureCollection) gmlAdapter.parseFeature( new XMLStreamReaderWrapper( xmlReader, docURL.toString() ),
                                                          null);
        idContext.resolveXLinks( schema );

        for ( Feature member : fc ) {
            System.out.println( member.getId() );
        }

        nsContext = new SimpleNamespaceContext();
        nsContext.addNamespace( "gml", "http://www.opengis.net/gml" );
        nsContext.addNamespace( "app", "http://www.deegree.org/app" );
    }

    @Test
    public void filterCollection1()
                            throws FilterEvaluationException {
        Filter110XMLAdapter adapter = new Filter110XMLAdapter();
        adapter.load( FilterEvaluationTest.class.getResourceAsStream( "testfilter1.xml" ) );
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
        adapter.load( FilterEvaluationTest.class.getResourceAsStream( "testfilter2.xml" ) );
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
        adapter.load( FilterEvaluationTest.class.getResourceAsStream( "testfilter3.xml" ) );
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
        adapter.load( FilterEvaluationTest.class.getResourceAsStream( "testfilter4.xml" ) );
        Filter filter = adapter.parse();
        Assert.assertNotNull( filter );

        FeatureCollection filteredCollection = fc.getMembers( filter );
        for ( Feature feature : filteredCollection ) {
            System.out.println (feature.getId());
        }
    }    
}
