package org.deegree.layer.persistence.feature;

import org.deegree.commons.utils.Pair;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.logical.Or;
import org.deegree.layer.LayerQuery;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FilterBuilderTest {

    @Test
    public void testBuildRequestFilter() {
        String filterProperty = "type";
        List<String> filterValues = Collections.singletonList( "one" );

        LayerQuery layerQuery = mockLayerQuery( filterProperty, filterValues );
        OperatorFilter operatorFilter = FilterBuilder.buildRequestFilter( layerQuery );

        assertThat( operatorFilter.getOperator(), instanceOf( PropertyIsEqualTo.class ) );
    }

    @Test
    public void testBuildRequestFilter_MultipleValues() {
        String filterProperty = "type";
        List<String> filterValues = Arrays.asList( new String[] { "one", "two" } );
        LayerQuery layerQuery = mockLayerQuery( filterProperty, filterValues );
        OperatorFilter operatorFilter = FilterBuilder.buildRequestFilter( layerQuery );

        assertThat( operatorFilter.getOperator(), instanceOf( Or.class ) );
    }

    @Test
    public void testBuildRequestFilter_EmptyValues() {
        String filterProperty = "type";
        List<String> filterValues = Collections.emptyList();
        LayerQuery layerQuery = mockLayerQuery( filterProperty, filterValues );
        OperatorFilter operatorFilter = FilterBuilder.buildRequestFilter( layerQuery );

        assertThat( operatorFilter, is( nullValue() ) );
    }

    @Test
    public void testBuildRequestFilter_NullRequestFilter() {
        LayerQuery layerQuery = mockLayerQuery( null );
        OperatorFilter operatorFilter = FilterBuilder.buildRequestFilter( layerQuery );

        assertThat( operatorFilter, is( nullValue() ) );
    }

    private LayerQuery mockLayerQuery( String filterProperty, List<String> filterValues ) {
        Pair<String, List<String>> requestFilter = new Pair<String, List<String>>( filterProperty, filterValues );

        return mockLayerQuery( requestFilter );
    }

    private LayerQuery mockLayerQuery( Pair<String, List<String>> requestFilter ) {
        LayerQuery layerQueryMock = Mockito.mock( LayerQuery.class );
        Mockito.when( layerQueryMock.requestFilter() ).thenReturn( requestFilter );
        return layerQueryMock;
    }

}