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
package org.deegree.protocol.wfs.getfeature.kvp;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.filter.Filter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.filter.projection.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.Within;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.query.AdHocQuery;
import org.deegree.protocol.wfs.query.BBoxQuery;
import org.deegree.protocol.wfs.query.FeatureIdQuery;
import org.deegree.protocol.wfs.query.FilterQuery;
import org.deegree.protocol.wfs.query.Query;
import org.junit.Test;

/**
 * The <code>GetFeatureKVPAdapterTest</code> class tests the GetFeature KVP adapter.
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class GetFeatureKVPAdapterTest extends TestCase {

	// ----------------------V 1.0.0 ---------------------------------
	private final String V100_EXAMPLE_1 = "wfs100/example1.kvp";

	private final String V100_EXAMPLE_2 = "wfs100/example2.kvp";

	private final String V100_EXAMPLE_3 = "wfs100/example3.kvp";

	private final String V100_EXAMPLE_4 = "wfs100/example4.kvp";

	private final String V100_EXAMPLE_5 = "wfs100/example5.kvp";

	private final String V100_EXAMPLE_6 = "wfs100/example6.kvp";

	private final String V100_EXAMPLE_7 = "wfs100/example7.kvp";

	private final String V100_EXAMPLE_9 = "wfs100/example9.kvp";

	private final String V100_EXAMPLE_10 = "wfs100/example10.kvp";

	private final String V100_EXAMPLE_11 = "wfs100/example11.kvp";

	private final String V100_EXAMPLE_12 = "wfs100/example12.kvp";

	// ----------------------V 1.1.0 ---------------------------------
	private final String V110_EXAMPLE_1 = "wfs110/example1.kvp";

	private final String V110_EXAMPLE_2 = "wfs110/example2.kvp";

	private final String V110_EXAMPLE_3 = "wfs110/example3.kvp";

	private final String V110_EXAMPLE_4 = "wfs110/example4.kvp";

	private final String V110_EXAMPLE_5 = "wfs110/example5.kvp";

	private final String V110_EXAMPLE_6 = "wfs110/example6.kvp";

	private final String V110_EXAMPLE_7 = "wfs110/example7.kvp";

	private final String V110_EXAMPLE_8 = "wfs110/example8.kvp";

	private final String V110_EXAMPLE_9 = "wfs110/example9.kvp";

	private final String V110_EXAMPLE_10 = "wfs110/example10.kvp";

	private final String V110_EXAMPLE_11 = "wfs110/example11.kvp";

	private final String V110_EXAMPLE_12 = "wfs110/example12.kvp";

	private final String V110_EXAMPLE_13 = "wfs110/example13.kvp";

	private final String V110_EXAMPLE_14 = "wfs110/example14.kvp";

	private final String V110_EXAMPLE_15 = "wfs110/example15.kvp";

	private final String V110_EXAMPLE_16 = "wfs110/example16.kvp";

	private final String V110_EXAMPLE_17 = "wfs110/example17.kvp";

	private final String V110_EXAMPLE_sortby = "wfs110/example_sortby.kvp";

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V100_EXAMPLE_1() throws Exception {
		URL exampleURL = this.getClass().getResource(V100_EXAMPLE_1);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		assertEquals(new QName("INWATERA_1M"),
				((AdHocQuery) getFeature.getQueries().get(0)).getTypeNames()[0].getFeatureTypeName());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_1() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_1);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);

		List<Query> queries = getFeature.getQueries();
		assertEquals(((FilterQuery) queries.get(0)).getTypeNames()[0].getFeatureTypeName(), new QName("InWaterA_1M"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V100_EXAMPLE_2() throws Exception {
		URL exampleURL = this.getClass().getResource(V100_EXAMPLE_2);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FilterQuery filterQuery = (FilterQuery) getFeature.getQueries().get(0);
		assertEquals(new QName("INWATERA_1M"), filterQuery.getTypeNames()[0].getFeatureTypeName());

		assertEquals("INWATERA_1M/WKB_GEOM",
				((PropertyName) filterQuery.getProjectionClauses()[0]).getPropertyName().getAsText());
		assertEquals("INWATERA_1M/TILE_ID",
				((PropertyName) filterQuery.getProjectionClauses()[1]).getPropertyName().getAsText());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_2() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_2);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);

		List<Query> queries = getFeature.getQueries();
		assertEquals(
				((PropertyName) ((FilterQuery) queries.get(0)).getProjectionClauses()[0]).getPropertyName().getAsText(),
				"InWaterA_1M/wkbGeom");
		assertEquals(
				((PropertyName) ((FilterQuery) queries.get(0)).getProjectionClauses()[1]).getPropertyName().getAsText(),
				"InWaterA_1M/tileId");
		assertEquals(((FilterQuery) queries.get(0)).getTypeNames()[0].getFeatureTypeName(), new QName("InWaterA_1M"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V100_EXAMPLE_3() throws Exception {
		URL exampleURL = this.getClass().getResource(V100_EXAMPLE_3);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FeatureIdQuery featureQuery = (FeatureIdQuery) getFeature.getQueries().get(0);
		assertEquals("INWATERA_1M.1013", featureQuery.getFeatureIds()[0]);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_3() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_3);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);

		List<Query> queries = getFeature.getQueries();
		FeatureIdQuery featureId = (FeatureIdQuery) queries.get(0);
		assertEquals("InWaterA_1M.1013", featureId.getFeatureIds()[0]);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V100_EXAMPLE_4() throws Exception {
		URL exampleURL = this.getClass().getResource(V100_EXAMPLE_4);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);

		List<Query> queries = getFeature.getQueries();
		FeatureIdQuery featureId = (FeatureIdQuery) queries.get(0);
		assertEquals("INWATERA_1M", featureId.getFeatureIds()[0]);

		assertEquals("INWATERA_1M/WKB_GEOM",
				((PropertyName) featureId.getProjectionClauses()[0]).getPropertyName().getAsText());
		assertEquals("INWATERA_1M/TILE_ID",
				((PropertyName) featureId.getProjectionClauses()[1]).getPropertyName().getAsText());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_4() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_4);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		assertEquals(((PropertyName) ((FeatureIdQuery) getFeature.getQueries().get(0)).getProjectionClauses()[0])
			.getPropertyName()
			.getAsText(), "InWaterA_1M/wkbGeom");
		assertEquals(((PropertyName) ((FeatureIdQuery) getFeature.getQueries().get(0)).getProjectionClauses()[1])
			.getPropertyName()
			.getAsText(), "InWaterA_1M/tileId");
		FeatureIdQuery featureId = (FeatureIdQuery) getFeature.getQueries().get(0);
		assertEquals("InWaterA_1M.1013", featureId.getFeatureIds()[0]);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V100_EXAMPLE_5() throws Exception {
		URL exampleURL = this.getClass().getResource(V100_EXAMPLE_5);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);

		List<Query> queries = getFeature.getQueries();
		FeatureIdQuery query = (FeatureIdQuery) queries.get(0);
		assertEquals("INWATERA_1M.1013", query.getFeatureIds()[0]);
		assertEquals("INWATERA_1M.1014", query.getFeatureIds()[1]);
		assertEquals("INWATERA_1M.1015", query.getFeatureIds()[2]);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_5() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_5);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);

		List<Query> queries = getFeature.getQueries();
		FeatureIdQuery query = (FeatureIdQuery) queries.get(0);
		assertEquals("InWaterA_1M.1013", query.getFeatureIds()[0]);
		assertEquals("InWaterA_1M.1014", query.getFeatureIds()[1]);
		assertEquals("InWaterA_1M.1015", query.getFeatureIds()[2]);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V100_EXAMPLE_6() throws Exception {
		URL exampleURL = this.getClass().getResource(V100_EXAMPLE_6);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);

		List<Query> queries = getFeature.getQueries();
		FeatureIdQuery query0 = (FeatureIdQuery) queries.get(0);
		FeatureIdQuery query1 = (FeatureIdQuery) queries.get(1);
		FeatureIdQuery query2 = (FeatureIdQuery) queries.get(2);
		assertEquals("INWATERA_1M.1013", query0.getFeatureIds()[0]);
		assertEquals("INWATERA_1M.1014", query1.getFeatureIds()[0]);
		assertEquals("INWATERA_1M.1015", query2.getFeatureIds()[0]);
		assertEquals("INWATERA_1M/WKB_GEOM",
				((PropertyName) query0.getProjectionClauses()[0]).getPropertyName().getAsText());
		assertEquals("INWATERA_1M/TILE_ID",
				((PropertyName) query0.getProjectionClauses()[1]).getPropertyName().getAsText());
		assertEquals("INWATERA_1M/WKB_GEOM",
				((PropertyName) query1.getProjectionClauses()[0]).getPropertyName().getAsText());
		assertEquals("INWATERA_1M/TILE_ID",
				((PropertyName) query1.getProjectionClauses()[1]).getPropertyName().getAsText());
		assertEquals("INWATERA_1M/WKB_GEOM",
				((PropertyName) query2.getProjectionClauses()[0]).getPropertyName().getAsText());
		assertEquals("INWATERA_1M/TILE_ID",
				((PropertyName) query2.getProjectionClauses()[1]).getPropertyName().getAsText());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_6() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_6);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FeatureIdQuery query0 = (FeatureIdQuery) getFeature.getQueries().get(0);
		FeatureIdQuery query1 = (FeatureIdQuery) getFeature.getQueries().get(1);
		FeatureIdQuery query2 = (FeatureIdQuery) getFeature.getQueries().get(2);
		assertEquals("InWaterA_1M.1013", query0.getFeatureIds()[0]);
		assertEquals("InWaterA_1M.1014", query1.getFeatureIds()[0]);
		assertEquals("InWaterA_1M.1015", query2.getFeatureIds()[0]);
		assertEquals("InWaterA_1M/wkbGeom",
				((PropertyName) query0.getProjectionClauses()[0]).getPropertyName().getAsText());
		assertEquals("InWaterA_1M/tileId",
				((PropertyName) query0.getProjectionClauses()[1]).getPropertyName().getAsText());
		assertEquals("InWaterA_1M/wkbGeom",
				((PropertyName) query1.getProjectionClauses()[0]).getPropertyName().getAsText());
		assertEquals("InWaterA_1M/tileId",
				((PropertyName) query1.getProjectionClauses()[1]).getPropertyName().getAsText());
		assertEquals("InWaterA_1M/wkbGeom",
				((PropertyName) query2.getProjectionClauses()[0]).getPropertyName().getAsText());
		assertEquals("InWaterA_1M/tileId",
				((PropertyName) query2.getProjectionClauses()[1]).getPropertyName().getAsText());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V100_EXAMPLE_7() throws Exception {
		URL exampleURL = this.getClass().getResource(V100_EXAMPLE_7);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FilterQuery filterQuery = (FilterQuery) getFeature.getQueries().get(0);
		OperatorFilter opFilter = (OperatorFilter) filterQuery.getFilter();
		Within within = (Within) opFilter.getOperator();
		assertEquals("INWATERA_1M/WKB_GEOM", within.getPropName().getAsText());
		Envelope env = (Envelope) within.getGeometry();
		verifyEnvelope(env, 10.0, 10.0, 20.0, 20.0);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_7() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_7);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FilterQuery filterq = (FilterQuery) getFeature.getQueries().get(0);
		assertEquals("InWaterA_1M", filterq.getTypeNames()[0].getFeatureTypeName().getLocalPart());
		OperatorFilter filter = (OperatorFilter) filterq.getFilter();
		assertTrue(filter.getOperator() instanceof Within);
		Within within = (Within) filter.getOperator();
		assertEquals("InWaterA_1M/wkbGeom", within.getPropName().getAsText());
		assertTrue(within.getGeometry() instanceof Envelope);
		Envelope env = (Envelope) within.getGeometry();
		verifyEnvelope(env, 10.0, 10.0, 20.0, 20.0);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_8() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_8);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FilterQuery filterq = (FilterQuery) getFeature.getQueries().get(0);
		assertEquals("InWaterA_1M/wkbGeom",
				((PropertyName) filterq.getProjectionClauses()[0]).getPropertyName().getAsText());
		assertEquals("InWaterA_1M/tileId",
				((PropertyName) filterq.getProjectionClauses()[1]).getPropertyName().getAsText());
		OperatorFilter filter = (OperatorFilter) filterq.getFilter();
		assertTrue(filter.getOperator() instanceof Within);
		Within within = (Within) filter.getOperator();
		assertEquals("InWaterA_1M/wkbGeom", within.getPropName().getAsText());
		assertTrue(within.getGeometry() instanceof Envelope);
		Envelope env = (Envelope) within.getGeometry();
		verifyEnvelope(env, 10.0, 10.0, 20.0, 20.0);
	}

	@SuppressWarnings("boxing")
	private void verifyEnvelope(Envelope env, double d, double e, double f, double g) {
		Point p1 = env.getMin();
		assertEquals(p1.get0(), d);
		assertEquals(p1.get1(), e);
		Point p2 = env.getMax();
		assertEquals(p2.get0(), f);
		assertEquals(p2.get1(), g);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V100_EXAMPLE_9() throws Exception {
		URL exampleURL = this.getClass().getResource(V100_EXAMPLE_9);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FilterQuery filterQuery0 = (FilterQuery) getFeature.getQueries().get(0);
		assertEquals(new QName("INWATERA_1M"), filterQuery0.getTypeNames()[0].getFeatureTypeName());
		FilterQuery filterQuery1 = (FilterQuery) getFeature.getQueries().get(1);
		assertEquals(new QName("BUILTUPA_1M"), filterQuery1.getTypeNames()[0].getFeatureTypeName());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_9() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_9);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FilterQuery query0 = (FilterQuery) getFeature.getQueries().get(0);
		assertEquals(new QName("http://www.someserver.com", "InWaterA_1M"),
				query0.getTypeNames()[0].getFeatureTypeName());
		FilterQuery query1 = (FilterQuery) getFeature.getQueries().get(1);
		assertEquals(new QName("http://www.someotherserver.com", "BuiltUpA_1M"),
				query1.getTypeNames()[0].getFeatureTypeName());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V100_EXAMPLE_10() throws Exception {
		URL exampleURL = this.getClass().getResource(V100_EXAMPLE_10);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);

		FilterQuery filterQuery = (FilterQuery) getFeature.getQueries().get(0);
		assertEquals(new QName("INWATERA_1M"), filterQuery.getTypeNames()[0].getFeatureTypeName());
		assertEquals("INWATERA_1M/WKB_GEOM",
				((PropertyName) filterQuery.getProjectionClauses()[0]).getPropertyName().getAsText());
		assertEquals("INWATERA_1M/TILE_ID",
				((PropertyName) filterQuery.getProjectionClauses()[1]).getPropertyName().getAsText());

		filterQuery = (FilterQuery) getFeature.getQueries().get(1);
		assertEquals(new QName("BUILTUPA_1M"), filterQuery.getTypeNames()[0].getFeatureTypeName());
		assertEquals("BUILTUPA_1M/*",
				((PropertyName) filterQuery.getProjectionClauses()[0]).getPropertyName().getAsText());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_10() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_10);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FilterQuery query0 = (FilterQuery) getFeature.getQueries().get(0);
		ProjectionClause[] propNames0 = query0.getProjectionClauses();
		assertEquals("InWaterA_1M/wkbGeom", ((PropertyName) propNames0[0]).getPropertyName().getAsText());
		assertEquals("InWaterA_1M/tileId", ((PropertyName) propNames0[1]).getPropertyName().getAsText());
		assertEquals(new QName("InWaterA_1M"), query0.getTypeNames()[0].getFeatureTypeName());

		FilterQuery query1 = (FilterQuery) getFeature.getQueries().get(1);
		ProjectionClause[] propNames1 = query1.getProjectionClauses();
		assertEquals("BuiltUpA_1M/*", ((PropertyName) propNames1[0]).getPropertyName().getAsText());
		assertEquals(new QName("BuiltUpA_1M"), query1.getTypeNames()[0].getFeatureTypeName());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V100_EXAMPLE_11() throws Exception {
		URL exampleURL = this.getClass().getResource(V100_EXAMPLE_11);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FeatureIdQuery query = (FeatureIdQuery) getFeature.getQueries().get(0);
		assertEquals("INWATERA_1M.1013", query.getFeatureIds()[0]);
		assertEquals("BUILTUP_1M.3456", query.getFeatureIds()[1]);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_11() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_11);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FeatureIdQuery query = (FeatureIdQuery) getFeature.getQueries().get(0);
		assertEquals("InWaterA_1M.1013", query.getFeatureIds()[0]);
		assertEquals("BUILTUP_1M.3456", query.getFeatureIds()[1]);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V100_EXAMPLE_12() throws Exception {
		URL exampleURL = this.getClass().getResource(V100_EXAMPLE_12);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FeatureIdQuery query0 = (FeatureIdQuery) getFeature.getQueries().get(0);
		FeatureIdQuery query1 = (FeatureIdQuery) getFeature.getQueries().get(1);
		assertEquals("INWATERA_1M.1013", query0.getFeatureIds()[0]);
		assertEquals("BUILTUPA_1M.3456", query1.getFeatureIds()[0]);
		assertEquals("INWATERA_1M/WKB_GEOM",
				((PropertyName) query0.getProjectionClauses()[0]).getPropertyName().getAsText());
		assertEquals("INWATERA_1M/TILE_ID",
				((PropertyName) query0.getProjectionClauses()[1]).getPropertyName().getAsText());
		// TODO example query appears to be invalid
		assertEquals("BUILTUPA_1M/WKB_GEOM",
				((PropertyName) query0.getProjectionClauses()[2]).getPropertyName().getAsText());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_12() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_12);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);
		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FeatureIdQuery query0 = (FeatureIdQuery) getFeature.getQueries().get(0);
		FeatureIdQuery query1 = (FeatureIdQuery) getFeature.getQueries().get(1);
		assertEquals("InWaterA_1M.1013", query0.getFeatureIds()[0]);
		assertEquals("BuiltUpA_1M.3456", query1.getFeatureIds()[0]);
		assertEquals("InWaterA_1M/wkbGeom",
				((PropertyName) query0.getProjectionClauses()[0]).getPropertyName().getAsText());
		assertEquals("InWaterA_1M/tileId",
				((PropertyName) query0.getProjectionClauses()[1]).getPropertyName().getAsText());
		assertEquals("BuiltUpA_1M/wkbGeom",
				((PropertyName) query1.getProjectionClauses()[0]).getPropertyName().getAsText());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_13() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_13);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FilterQuery query0 = (FilterQuery) getFeature.getQueries().get(0);
		OperatorFilter filter = (OperatorFilter) query0.getFilter();
		assertTrue(filter.getOperator() instanceof Within);
		Within within = (Within) filter.getOperator();
		assertEquals("InWaterA_1M/wkbGeom", within.getPropName().getAsText());

		Envelope env = (Envelope) within.getGeometry();
		verifyEnvelope(env, 10.0, 10.0, 20.0, 20.0);

		FilterQuery query1 = (FilterQuery) getFeature.getQueries().get(1);
		filter = (OperatorFilter) query1.getFilter();
		assertTrue(filter.getOperator() instanceof Within);
		within = (Within) filter.getOperator();
		assertEquals("BuiltUpA_1M/wkbGeom", within.getPropName().getAsText());

		env = (Envelope) within.getGeometry();
		verifyEnvelope(env, 10.0, 10.0, 20.0, 20.0);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_14() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_14);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FilterQuery filter0 = (FilterQuery) getFeature.getQueries().get(0);
		ProjectionClause[] propNames = filter0.getProjectionClauses();
		assertEquals("InWaterA_1M/wkbGeom", ((PropertyName) propNames[0]).getPropertyName().getAsText());
		assertEquals("InWaterA_1M/tileId", ((PropertyName) propNames[1]).getPropertyName().getAsText());
		assertEquals(new QName("InWaterA_1M"), filter0.getTypeNames()[0].getFeatureTypeName());

		FilterQuery filter1 = (FilterQuery) getFeature.getQueries().get(1);
		propNames = filter1.getProjectionClauses();
		assertEquals("BuiltUpA_1M/wkbGeom", ((PropertyName) propNames[0]).getPropertyName().getAsText());
		assertEquals(new QName("BuiltUpA_1M"), filter1.getTypeNames()[0].getFeatureTypeName());

		Operator op = ((OperatorFilter) filter0.getFilter()).getOperator();
		assertTrue(op instanceof Within);
		Within within = (Within) op;
		assertEquals("InWaterA_1M/wkbGeom|InWaterA_1M/wkbGeom", within.getPropName().getAsText());
		Envelope env = (Envelope) within.getGeometry();
		verifyEnvelope(env, 10.0, 10.0, 20.0, 20.0);

		op = ((OperatorFilter) filter1.getFilter()).getOperator();
		assertTrue(op instanceof Within);
		within = (Within) op;
		assertEquals("InWaterA_1M/wkbGeom", within.getPropName().getAsText());
		env = (Envelope) within.getGeometry();
		verifyEnvelope(env, 10.0, 10.0, 20.0, 20.0);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_15() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_15);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FeatureIdQuery featureQuery = (FeatureIdQuery) getFeature.getQueries().get(0);
		ProjectionClause[] propNames = featureQuery.getProjectionClauses();
		assertEquals("uk:Town/gml:name", ((PropertyName) propNames[0]).getPropertyName().getAsText());
		assertEquals("uk:Town/gml:directedNode", ((PropertyName) propNames[1]).getPropertyName().getAsText());
		String[] featureId = featureQuery.getFeatureIds();
		assertEquals("t1", featureId[0]);
		TypeName[] typeName = featureQuery.getTypeNames();
		assertEquals(new QName("http://www.theuknamespace.uk", "Town"), typeName[0].getFeatureTypeName());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_16() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_16);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		assertEquals("1", getFeature.getResolveParams().getDepth());
		assertEquals(BigInteger.valueOf(60), getFeature.getResolveParams().getTimeout());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_17() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_17);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FeatureIdQuery featureQuery = (FeatureIdQuery) getFeature.getQueries().get(0);
		ProjectionClause[] xlinkProps = featureQuery.getProjectionClauses();
		assertEquals("uk:Town/gml:name", ((PropertyName) xlinkProps[0]).getPropertyName().getAsText());
		assertEquals("0", ((PropertyName) xlinkProps[0]).getResolveParams().getDepth());
		assertEquals(BigInteger.valueOf(0), ((PropertyName) xlinkProps[0]).getResolveParams().getTimeout());

		assertEquals("uk:Town/gml:directedNode", ((PropertyName) xlinkProps[1]).getPropertyName().getAsText());
		assertEquals("2", ((PropertyName) xlinkProps[1]).getResolveParams().getDepth());
		assertEquals(BigInteger.valueOf(120), ((PropertyName) xlinkProps[1]).getResolveParams().getTimeout());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_V110_EXAMPLE_sortby() throws Exception {
		URL exampleURL = this.getClass().getResource(V110_EXAMPLE_sortby);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);

		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMap, null);
		FilterQuery filterQuery = (FilterQuery) getFeature.getQueries().get(0);
		SortProperty[] sortby = filterQuery.getSortBy();
		assertEquals(4, sortby.length);
		assertEquals(true, sortby[0].getSortOrder());
		assertEquals("ands/dsalw", sortby[0].getSortProperty().getAsText());
		assertEquals(true, sortby[1].getSortOrder());
		assertEquals("dsad/assdsa", sortby[1].getSortProperty().getAsText());
		assertEquals(true, sortby[2].getSortOrder());
		assertEquals("dsda/asdasda", sortby[2].getSortProperty().getAsText());
		assertEquals(true, sortby[3].getSortOrder());
		assertEquals("erewr/sdasd/dasda", sortby[3].getSortProperty().getAsText());

		assertEquals("ALL", filterQuery.getFeatureVersion());
		assertEquals(BigInteger.valueOf(1000000), getFeature.getPresentationParams().getCount());
		assertEquals(CRSManager.getCRSRef("EPSG:4326"), filterQuery.getSrsName());
	}

	@Test
	public void test200Example1() throws Exception {
		GetFeature request = parse("wfs200/example1.kvp");
		assertEquals(VERSION_200, request.getVersion());
		assertNull(request.getPresentationParams().getOutputFormat());
		assertNull(request.getPresentationParams().getCount());
		assertNull(request.getPresentationParams().getResultType());
		assertNull(request.getPresentationParams().getStartIndex());
		assertNull(request.getResolveParams().getDepth());
		assertNull(request.getResolveParams().getMode());
		assertNull(request.getResolveParams().getTimeout());
		assertEquals(1, request.getQueries().size());
		FilterQuery query = (FilterQuery) request.getQueries().get(0);
		assertNull(query.getFeatureVersion());
		assertNull(query.getHandle());
		assertNull(query.getFilter());
		assertEquals(0, query.getProjectionClauses().length);
		assertEquals(0, query.getSortBy().length);
		assertNull(query.getSrsName());
		assertEquals(1, query.getTypeNames().length);
		assertEquals(QName.valueOf("InWaterA_1M"), query.getTypeNames()[0].getFeatureTypeName());
		assertNull(query.getTypeNames()[0].getAlias());
	}

	@Test
	public void test200Example2() throws Exception {
		GetFeature request = parse("wfs200/example2.kvp");
		assertEquals(VERSION_200, request.getVersion());
		assertNull(request.getPresentationParams().getOutputFormat());
		assertNull(request.getPresentationParams().getCount());
		assertNull(request.getPresentationParams().getResultType());
		assertNull(request.getPresentationParams().getStartIndex());
		assertNull(request.getResolveParams().getDepth());
		assertNull(request.getResolveParams().getMode());
		assertNull(request.getResolveParams().getTimeout());
		assertEquals(1, request.getQueries().size());
		FilterQuery query = (FilterQuery) request.getQueries().get(0);
		assertNull(query.getFeatureVersion());
		assertNull(query.getHandle());
		assertNull(query.getFilter());
		assertEquals(2, query.getProjectionClauses().length);
		assertEquals("InWaterA_1M/wkbGeom",
				((PropertyName) query.getProjectionClauses()[0]).getPropertyName().getAsText());
		assertNull(((PropertyName) query.getProjectionClauses()[0]).getResolveParams().getDepth());
		assertNull(((PropertyName) query.getProjectionClauses()[0]).getResolveParams().getMode());
		assertNull(((PropertyName) query.getProjectionClauses()[0]).getResolveParams().getTimeout());
		assertNull(((PropertyName) query.getProjectionClauses()[0]).getResolvePath());
		assertEquals("InWaterA_1M/tileId",
				((PropertyName) query.getProjectionClauses()[1]).getPropertyName().getAsText());
		assertNull(((PropertyName) query.getProjectionClauses()[1]).getResolveParams().getDepth());
		assertNull(((PropertyName) query.getProjectionClauses()[1]).getResolveParams().getMode());
		assertNull(((PropertyName) query.getProjectionClauses()[1]).getResolveParams().getTimeout());
		assertNull(((PropertyName) query.getProjectionClauses()[1]).getResolvePath());
		assertEquals(0, query.getSortBy().length);
		assertNull(query.getSrsName());
		assertEquals(1, query.getTypeNames().length);
		assertEquals(QName.valueOf("InWaterA_1M"), query.getTypeNames()[0].getFeatureTypeName());
		assertNull(query.getTypeNames()[0].getAlias());
	}

	@Test
	public void test200Example3() throws Exception {
		GetFeature request = parse("wfs200/example3.kvp");
		assertEquals(VERSION_200, request.getVersion());
		assertNull(request.getPresentationParams().getOutputFormat());
		assertNull(request.getPresentationParams().getCount());
		assertNull(request.getPresentationParams().getResultType());
		assertNull(request.getPresentationParams().getStartIndex());
		assertNull(request.getResolveParams().getDepth());
		assertNull(request.getResolveParams().getMode());
		assertNull(request.getResolveParams().getTimeout());
		assertEquals(1, request.getQueries().size());
		FeatureIdQuery query = (FeatureIdQuery) request.getQueries().get(0);
		assertNull(query.getFeatureVersion());
		assertNull(query.getHandle());
		assertEquals(0, query.getProjectionClauses().length);
		assertEquals(0, query.getSortBy().length);
		assertNull(query.getSrsName());
		assertEquals(0, query.getTypeNames().length);
		assertEquals(1, query.getFeatureIds().length);
		assertEquals("InWaterA_1M.1013", query.getFeatureIds()[0]);
	}

	@Test
	public void test200Example4() throws Exception {
		GetFeature request = parse("wfs200/example4.kvp");
		assertEquals(VERSION_200, request.getVersion());
		assertNull(request.getPresentationParams().getOutputFormat());
		assertNull(request.getPresentationParams().getCount());
		assertNull(request.getPresentationParams().getResultType());
		assertNull(request.getPresentationParams().getStartIndex());
		assertNull(request.getResolveParams().getDepth());
		assertNull(request.getResolveParams().getMode());
		assertNull(request.getResolveParams().getTimeout());
		assertEquals(1, request.getQueries().size());
		FeatureIdQuery query = (FeatureIdQuery) request.getQueries().get(0);
		assertNull(query.getFeatureVersion());
		assertNull(query.getHandle());
		assertEquals(0, query.getProjectionClauses().length);
		assertEquals(0, query.getSortBy().length);
		assertNull(query.getSrsName());
		assertEquals(0, query.getTypeNames().length);
		assertEquals(3, query.getFeatureIds().length);
		assertEquals("InWaterA_1M.1013", query.getFeatureIds()[0]);
		assertEquals("InWaterA_1M.1014", query.getFeatureIds()[1]);
		assertEquals("InWaterA_1M.1015", query.getFeatureIds()[2]);
	}

	@Test
	public void test200Example5() throws Exception {
		GetFeature request = parse("wfs200/example5.kvp");
		assertEquals(VERSION_200, request.getVersion());
		assertNull(request.getPresentationParams().getOutputFormat());
		assertNull(request.getPresentationParams().getCount());
		assertNull(request.getPresentationParams().getResultType());
		assertNull(request.getPresentationParams().getStartIndex());
		assertNull(request.getResolveParams().getDepth());
		assertNull(request.getResolveParams().getMode());
		assertNull(request.getResolveParams().getTimeout());
		assertEquals(1, request.getQueries().size());
		BBoxQuery query = (BBoxQuery) request.getQueries().get(0);
		assertNull(query.getFeatureVersion());
		assertNull(query.getHandle());
		assertEquals(0, query.getProjectionClauses().length);
		assertEquals(0, query.getSortBy().length);
		assertNull(query.getSrsName());
		assertEquals(1, query.getTypeNames().length);
		assertEquals(QName.valueOf("InWaterA_1M"), query.getTypeNames()[0].getFeatureTypeName());
		assertNull(query.getTypeNames()[0].getAlias());
		assertEquals(null, query.getBBox().getCoordinateSystem());
		assertEquals(18.54, query.getBBox().getMin().get0(), 0.0000001);
		assertEquals(-72.3544, query.getBBox().getMin().get1(), 0.0000001);
		assertEquals(18.62, query.getBBox().getMax().get0(), 0.0000001);
		assertEquals(-72.2564, query.getBBox().getMax().get1(), 0.0000001);
	}

	@Test
	public void test200Example6() throws Exception {
		GetFeature request = parse("wfs200/example6.kvp");
		assertEquals(VERSION_200, request.getVersion());
		assertNull(request.getPresentationParams().getOutputFormat());
		assertNull(request.getPresentationParams().getCount());
		assertNull(request.getPresentationParams().getResultType());
		assertNull(request.getPresentationParams().getStartIndex());
		assertNull(request.getResolveParams().getDepth());
		assertNull(request.getResolveParams().getMode());
		assertNull(request.getResolveParams().getTimeout());
		assertEquals(1, request.getQueries().size());
		FilterQuery query = (FilterQuery) request.getQueries().get(0);
		assertNull(query.getFeatureVersion());
		assertNull(query.getHandle());
		assertEquals(2, query.getProjectionClauses().length);
		assertEquals("InWaterA_1M/wkbGeom",
				((PropertyName) query.getProjectionClauses()[0]).getPropertyName().getAsText());
		assertNull(((PropertyName) query.getProjectionClauses()[0]).getResolveParams().getDepth());
		assertNull(((PropertyName) query.getProjectionClauses()[0]).getResolveParams().getMode());
		assertNull(((PropertyName) query.getProjectionClauses()[0]).getResolveParams().getTimeout());
		assertNull(((PropertyName) query.getProjectionClauses()[0]).getResolvePath());
		assertEquals("InWaterA_1M/tileId",
				((PropertyName) query.getProjectionClauses()[1]).getPropertyName().getAsText());
		assertNull(((PropertyName) query.getProjectionClauses()[1]).getResolveParams().getDepth());
		assertNull(((PropertyName) query.getProjectionClauses()[1]).getResolveParams().getMode());
		assertNull(((PropertyName) query.getProjectionClauses()[1]).getResolveParams().getTimeout());
		assertNull(((PropertyName) query.getProjectionClauses()[1]).getResolvePath());
		assertEquals(0, query.getSortBy().length);
		assertNull(query.getSrsName());
		assertEquals(1, query.getTypeNames().length);
		assertEquals(QName.valueOf("InWaterA_1M"), query.getTypeNames()[0].getFeatureTypeName());
		assertNull(query.getTypeNames()[0].getAlias());
		Filter filter = query.getFilter();
	}

	@Test
	public void test200Example7() throws Exception {
		GetFeature request = parse("wfs200/example7.kvp");
	}

	@Test
	public void test200Example8() throws Exception {
		GetFeature request = parse("wfs200/example8.kvp");
	}

	@Test
	public void test200Example9() throws Exception {
		GetFeature request = parse("wfs200/example9.kvp");
	}

	@Test
	public void test200Example10() throws Exception {
		GetFeature request = parse("wfs200/example10.kvp");
	}

	@Test
	public void test200Example11() throws Exception {
		GetFeature request = parse("wfs200/example11.kvp");
	}

	@Test
	public void test200Example12() throws Exception {
		GetFeature request = parse("wfs200/example12.kvp");
	}

	@Test
	public void test200Example13() throws Exception {
		GetFeature request = parse("wfs200/example13.kvp");
	}

	@Test
	public void test200Example14() throws Exception {
		GetFeature request = parse("wfs200/example14.kvp");
	}

	@Test
	public void test200Example15() throws Exception {
		GetFeature request = parse("wfs200/example15.kvp");
	}

	@Test
	public void test200Example16() throws Exception {
		GetFeature request = parse("wfs200/example16.kvp");
	}

	@Test
	public void test200Example17() throws Exception {
		GetFeature request = parse("wfs200/example17.kvp");
	}

	@Test
	public void test200ExampleBboxExplicitCrs() throws Exception {
		final GetFeature request = parse("wfs200/example_bbox_explicit_crs.kvp");
		final BBoxQuery query = (BBoxQuery) request.getQueries().get(0);
		final ICRS crs = query.getBBox().getCoordinateSystem();
		assertEquals("EPSG:4326", crs.getAlias());
	}

	private GetFeature parse(String resource) throws Exception {
		URL exampleURL = GetFeatureKVPAdapterTest.class.getResource(resource);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(exampleURL);
		return GetFeatureKVPAdapter.parse(kvpMap, null);
	}

}
