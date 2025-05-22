package org.deegree.layer.persistence.feature;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.deegree.commons.utils.Pair;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.logical.Or;
import org.deegree.filter.temporal.After;
import org.deegree.geometry.Envelope;
import org.deegree.layer.LayerQuery;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FilterBuilderTest {

	@Test
	public void testBuildRequestFilter() {
		String filterProperty = "type";
		List<String> filterValues = Collections.singletonList("one");

		LayerQuery layerQuery = mockLayerQuery(filterProperty, filterValues);
		Set<QName> propertyNames = createPropertyNames(filterProperty);
		OperatorFilter operatorFilter = FilterBuilder.buildRequestFilter(layerQuery, propertyNames);

		assertThat(operatorFilter.getOperator(), instanceOf(PropertyIsEqualTo.class));
	}

	@Test
	public void testBuildRequestFilter_MultipleValues() {
		String filterProperty = "type";
		List<String> filterValues = Arrays.asList(new String[] { "one", "two" });
		LayerQuery layerQuery = mockLayerQuery(filterProperty, filterValues);
		Set<QName> propertyNames = createPropertyNames(filterProperty);
		OperatorFilter operatorFilter = FilterBuilder.buildRequestFilter(layerQuery, propertyNames);

		assertThat(operatorFilter.getOperator(), instanceOf(Or.class));
	}

	@Test
	public void testBuildRequestFilter_EmptyValues() {
		String filterProperty = "type";
		List<String> filterValues = Collections.emptyList();
		LayerQuery layerQuery = mockLayerQuery(filterProperty, filterValues);
		Set<QName> propertyNames = createPropertyNames(filterProperty);
		OperatorFilter operatorFilter = FilterBuilder.buildRequestFilter(layerQuery, propertyNames);

		assertThat(operatorFilter, is(nullValue()));
	}

	@Test
	public void testBuildRequestFilter_NullRequestFilter() {
		LayerQuery layerQuery = mock(LayerQuery.class);

		Set<QName> propertyNames = createPropertyNames("abc");
		OperatorFilter operatorFilter = FilterBuilder.buildRequestFilter(layerQuery, propertyNames);

		assertThat(operatorFilter, is(nullValue()));
	}

	@Test
	public void testBuildRequestFilter_PropertyNotKnown() {
		String filterProperty = "type";
		List<String> filterValues = Collections.singletonList("one");

		LayerQuery layerQuery = mockLayerQuery(filterProperty, filterValues);
		Set<QName> propertyNames = createPropertyNames("anothertype");
		OperatorFilter operatorFilter = FilterBuilder.buildRequestFilter(layerQuery, propertyNames);

		assertThat(operatorFilter, is(nullValue()));
	}

	@Test
	public void testBuildCql2Filter() {
		String cql2Filter = "T_AFTER(testDate,TIMESTAMP('2025-04-14T08:59:30Z'))";
		LayerQuery layerQuery = mockLayerQuery(cql2Filter);
		Set<QName> propertyNames = createPropertyNames("testDate");
		OperatorFilter operatorFilter = FilterBuilder.buildCql2Filter(layerQuery, propertyNames);

		assertThat(operatorFilter.getOperator(), instanceOf(After.class));
	}

	private LayerQuery mockLayerQuery(String filterProperty, List<String> filterValues) {
		Pair<String, List<String>> requestFilter = new Pair<>(filterProperty, filterValues);
		LayerQuery layerQueryMock = mock(LayerQuery.class);
		when(layerQueryMock.requestFilter()).thenReturn(requestFilter);
		return layerQueryMock;
	}

	private LayerQuery mockLayerQuery(String cql2Filter) {
		LayerQuery layerQueryMock = mock(LayerQuery.class);
		when(layerQueryMock.cql2Filter()).thenReturn(cql2Filter);
		Envelope envelope = mock(Envelope.class);
		when(envelope.getCoordinateSystem()).thenReturn(CRSManager.getCRSRef("EPSG:4326"));
		when(layerQueryMock.getQueryBox()).thenReturn(envelope);
		return layerQueryMock;
	}

	private Set<QName> createPropertyNames(String filterProperty) {
		return Collections.singleton(new QName(filterProperty));
	}

}