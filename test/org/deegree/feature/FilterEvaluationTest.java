package org.deegree.feature;

import org.deegree.commons.filter.Filter;
import org.deegree.commons.filter.Filter110XMLAdapter;
import org.deegree.commons.filter.FilterEvaluationException;
import org.jaxen.SimpleNamespaceContext;
import org.junit.Assert;
import org.junit.Before;

public class FilterEvaluationTest {

    private FeatureCollection fc;

    private SimpleNamespaceContext nsContext;

    @Before
    public void setUp()
                            throws Exception {
        // fc = new GMLFeatureParserTest().testParsingPhilosopherFeatureCollection();
        nsContext = new SimpleNamespaceContext();
        nsContext.addNamespace( "app", "http://www.deegree.org/app" );
    }

    // @Test
    public void filterCollection1()
                            throws FilterEvaluationException {
        Filter110XMLAdapter adapter = new Filter110XMLAdapter();
        adapter.load( FilterEvaluationTest.class.getResourceAsStream( "testfilter1.xml" ) );
        Filter filter = adapter.parse();
        Assert.assertNotNull( filter );

        FeatureCollection filteredCollection = fc.getMembers( filter );
        for ( Feature feature : filteredCollection ) {
            System.out.println( feature.getId() );
        }
    }

    // @Test
    public void filterCollection2()
                            throws FilterEvaluationException {
        Filter110XMLAdapter adapter = new Filter110XMLAdapter();
        adapter.load( FilterEvaluationTest.class.getResourceAsStream( "testfilter2.xml" ) );
        Filter filter = adapter.parse();
        Assert.assertNotNull( filter );

        FeatureCollection filteredCollection = fc.getMembers( filter );
        for ( Feature feature : filteredCollection ) {
            System.out.println( feature.getId() );
        }
    }
}
