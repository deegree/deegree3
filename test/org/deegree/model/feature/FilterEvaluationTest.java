package org.deegree.model.feature;


import java.util.List;

import org.deegree.model.filter.Filter;
import org.deegree.model.filter.Filter110XMLAdapter;
import org.deegree.model.filter.FilterEvaluationException;
import org.deegree.model.gml.FeatureGMLAdapterTest;
import org.jaxen.SimpleNamespaceContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FilterEvaluationTest {

    private FeatureCollection fc; 
    
    private SimpleNamespaceContext nsContext;    
    
    @Before
    public void setUp()
                            throws Exception {
        fc = new FeatureGMLAdapterTest().testParsingPhilosopherFeatureCollection();
        nsContext = new SimpleNamespaceContext ();
        nsContext.addNamespace( "app", "http://www.deegree.org/app" );
    }

    @Test
    public void filterCollection1() throws FilterEvaluationException {
        Filter110XMLAdapter adapter = new Filter110XMLAdapter();
        adapter.load( FilterEvaluationTest.class.getResourceAsStream( "testfilter1.xml" ) );
        Filter filter = adapter.parse();
        Assert.assertNotNull( filter );

        List<Feature> features = fc.getMembers( filter );
        for ( Feature feature : features ) {
            System.out.println (feature.getId());
        }
    }
    
    @Test
    public void filterCollection2() throws FilterEvaluationException {
        Filter110XMLAdapter adapter = new Filter110XMLAdapter();
        adapter.load( FilterEvaluationTest.class.getResourceAsStream( "testfilter2.xml" ) );
        Filter filter = adapter.parse();
        Assert.assertNotNull( filter );

        List<Feature> features = fc.getMembers( filter );
        for ( Feature feature : features ) {
            System.out.println (feature.getId());
        }
    }    
}
