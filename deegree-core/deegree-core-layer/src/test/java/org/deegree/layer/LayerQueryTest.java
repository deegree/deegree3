package org.deegree.layer;

import org.deegree.commons.utils.Pair;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.SimpleGeometryFactory;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.deegree.layer.LayerQuery.FILTERPROPERTY;
import static org.deegree.layer.LayerQuery.FILTERVALUE;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class LayerQueryTest {

    @Test
    public void requestFilter()
                            throws Exception {
        String filterProperty = "type";
        String filterValue = "one";
        LayerQuery layerQuery = createLayerQuery( filterProperty, filterValue );

        Pair<String, List<String>> requestFilter = layerQuery.requestFilter();
        assertThat( requestFilter.getFirst(), is( filterProperty ) );
        assertThat( requestFilter.getSecond().size(), is( 1 ) );
        assertThat( requestFilter.getSecond(), hasItem( filterValue ) );
    }

    @Test
    public void requestFilter_MultipleValues()
                            throws Exception {
        String filterProperty = "type";
        String filterValue = "one, two";
        LayerQuery layerQuery = createLayerQuery( filterProperty, filterValue );

        Pair<String, List<String>> requestFilter = layerQuery.requestFilter();
        assertThat( requestFilter.getFirst(), is( filterProperty ) );
        assertThat( requestFilter.getSecond().size(), is( 2 ) );
        assertThat( requestFilter.getSecond(), hasItem( "one" ) );
        assertThat( requestFilter.getSecond(), hasItem( "two" ) );
    }

    @Test
    public void requestFilter_MultipleValuesWithMissing()
                            throws Exception {
        String filterProperty = "type";
        String filterValue = "one,,two";
        LayerQuery layerQuery = createLayerQuery( filterProperty, filterValue );

        Pair<String, List<String>> requestFilter = layerQuery.requestFilter();
        assertThat( requestFilter.getFirst(), is( filterProperty ) );
        assertThat( requestFilter.getSecond().size(), is( 2 ) );
        assertThat( requestFilter.getSecond(), hasItem( "one" ) );
        assertThat( requestFilter.getSecond(), hasItem( "two" ) );
    }

    @Test
    public void requestFilter_NullValue()
                            throws Exception {
        String filterProperty = null;
        String filterValue = "one, two";
        LayerQuery layerQuery = createLayerQuery( filterProperty, filterValue );

        Pair<String, List<String>> requestFilter = layerQuery.requestFilter();
        assertThat( requestFilter, is( nullValue() ) );
    }

    @Test
    public void requestFilter_NullProperty()
                            throws Exception {
        String filterProperty = "type";
        String filterValue = null;
        LayerQuery layerQuery = createLayerQuery( filterProperty, filterValue );

        Pair<String, List<String>> requestFilter = layerQuery.requestFilter();
        assertThat( requestFilter, is( nullValue() ) );
    }

    @Test
    public void requestFilter_EmptyValue()
                            throws Exception {
        String filterProperty = "";
        String filterValue = "one, two";
        LayerQuery layerQuery = createLayerQuery( filterProperty, filterValue );

        Pair<String, List<String>> requestFilter = layerQuery.requestFilter();
        assertThat( requestFilter, is( nullValue() ) );
    }

    @Test
    public void requestFilter_EMptyProperty()
                            throws Exception {
        String filterProperty = "type";
        String filterValue = "";
        LayerQuery layerQuery = createLayerQuery( filterProperty, filterValue );

        Pair<String, List<String>> requestFilter = layerQuery.requestFilter();
        assertThat( requestFilter, is( nullValue() ) );
    }

    private LayerQuery createLayerQuery( String filterProperty, String filterValue )
                            throws UnknownCRSException {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put( FILTERPROPERTY, filterProperty );
        parameters.put( FILTERVALUE, filterValue );
        Envelope envelope = new SimpleGeometryFactory().createEnvelope( 5, 12, 6, 11,
                                                                        CRSManager.lookup( "EPSG:4326" ) );
        return new LayerQuery( envelope, 300, 200, 1, 2, 3, null, null, parameters, null, null, null, 1 );
    }

}