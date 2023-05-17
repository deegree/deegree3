/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.protocol.wfs.getfeature.xml;

import static java.math.BigInteger.valueOf;
import static org.deegree.filter.Operator.Type.COMPARISON;
import static org.deegree.filter.Operator.Type.LOGICAL;
import static org.deegree.filter.logical.LogicalOperator.SubType.AND;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.BinaryComparisonOperator;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.comparison.PropertyIsGreaterThan;
import org.deegree.filter.comparison.PropertyIsGreaterThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLessThanOrEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.filter.projection.PropertyName;
import org.deegree.filter.projection.TimeSliceProjection;
import org.deegree.filter.spatial.Within;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.query.FilterQuery;
import org.deegree.protocol.wfs.query.Query;
import org.junit.Test;

/**
 * Test class for the GetFeatureXMLAdapter.
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class GetFeatureXMLAdapterTest extends TestCase {

	// ---------------------version 1.0.0------------------------------
	private final String V100_EXAMPLE1 = "wfs100/example1.xml";

	private final String V100_EXAMPLE2 = "wfs100/example2.xml";

	private final String V100_EXAMPLE3 = "wfs100/example3.xml";

	private final String V100_EXAMPLE4 = "wfs100/example4.xml";

	private final String V100_EXAMPLE5 = "wfs100/example5.xml";

	private final String V100_EXAMPLE6 = "wfs100/example6.xml";

	private final String V100_EXAMPLE7 = "wfs100/example7.xml";

	private final String V100_EXAMPLE8 = "wfs100/example8.xml";

	private final String V100_EXAMPLE9 = "wfs100/example9.xml";

	// ---------------------version 1.1.0------------------------------
	private final String V110_EXAMPLE01 = "wfs110/example01.xml";

	private final String V110_EXAMPLE02 = "wfs110/example02.xml";

	private final String V110_EXAMPLE03 = "wfs110/example03.xml";

	private final String V110_EXAMPLE04 = "wfs110/example04.xml";

	// private final String V110_EXAMPLE05 = "wfs110/example05.xml";

	// private final String V110_EXAMPLE06 = "wfs110/example06.xml";

	private final String V110_EXAMPLE09 = "wfs110/example09.xml";

	private final String V110_EXAMPLE10 = "wfs110/example10.xml";

	private final String V110_EXAMPLE11 = "wfs110/example11.xml";

	private final String V110_EXAMPLE12 = "wfs110/example12.xml";

	/**
	 * @throws Exception
	 */
	public void test_V100_EXAMPLE01() throws Exception {
		URL exampleURL = this.getClass().getResource(V100_EXAMPLE1);
		XMLAdapter xmlAdapter = new XMLAdapter(exampleURL);

		GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
		getFeatureAdapter.setRootElement(xmlAdapter.getRootElement());
		GetFeature getFeature = getFeatureAdapter.parse();

		List<Query> queries = getFeature.getQueries();
		FilterQuery filterQuery = (FilterQuery) queries.get(0);
		assertEquals(new QName("http://www.someserver.com/myns", "INWATERA_1M"),
				filterQuery.getTypeNames()[0].getFeatureTypeName());
		IdFilter idFilter = (IdFilter) filterQuery.getFilter();

		Set<String> matchingIds = idFilter.getMatchingIds();
		assertTrue(matchingIds.size() == 1 && matchingIds.contains("INWATERA_1M.1234"));
	}

	/**
	 * @throws Exception When the version of the GetFeature document is not supported for
	 * parsing (superfluous in this case, since we are testing 1.1.0 files and parsing is
	 * supported for this version)
	 */
	@Test
	public void test_V110_EXAMPLE01() throws Exception {

		URL exampleURL = this.getClass().getResource(V110_EXAMPLE01);
		XMLAdapter xmlAdapter = new XMLAdapter(exampleURL);

		GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
		getFeatureAdapter.setRootElement(xmlAdapter.getRootElement());
		GetFeature getFeature = getFeatureAdapter.parse();

		List<Query> queries = getFeature.getQueries();
		FilterQuery filterQuery = (FilterQuery) queries.get(0);

		TypeName[] typeNames = filterQuery.getTypeNames();

		assertEquals(typeNames.length, 1);
		assertEquals(typeNames[0].getFeatureTypeName(), new QName("http://www.someserver.com/myns", "InWaterA1M"));

		IdFilter filter = (IdFilter) filterQuery.getFilter();
		Set<String> ids = filter.getMatchingIds();

		assertEquals(ids.size(), 1);
		assertTrue(ids.contains("InWaterA_1M.1234"));
	}

	/**
	 * @throws Exception
	 */
	public void test_V100_EXAMPLE2() throws Exception {

		URL exampleURL = this.getClass().getResource(V100_EXAMPLE2);
		XMLAdapter xmlAdapter = new XMLAdapter(exampleURL);

		GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
		getFeatureAdapter.setRootElement(xmlAdapter.getRootElement());
		GetFeature getFeature = getFeatureAdapter.parse();

		FilterQuery filterQuery = (FilterQuery) getFeature.getQueries().get(0);
		ProjectionClause[] projections = filterQuery.getProjectionClauses();

		assertEquals("myns:WKB_GEOM", ((PropertyName) projections[0]).getPropertyName().getAsText());
		assertEquals("myns:TILE_ID", ((PropertyName) projections[1]).getPropertyName().getAsText());
		assertEquals("myns:FAC_ID", ((PropertyName) projections[2]).getPropertyName().getAsText());

		IdFilter idFilter = (IdFilter) filterQuery.getFilter();
		Set<String> matchingIds = idFilter.getMatchingIds();
		assertTrue(matchingIds.size() == 1 && matchingIds.contains("INWATERA_1M.1013"));
	}

	/**
	 * @throws Exception When the version of the GetFeature document is not supported for
	 * parsing (superfluous in this case, since we are testing 1.1.0 files and parsing is
	 * supported for this version)
	 */
	@Test
	public void test_V110_EXAMPLE02() throws Exception {

		URL exampleURL = this.getClass().getResource(V110_EXAMPLE02);
		XMLAdapter xmlAdapter = new XMLAdapter(exampleURL);

		GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
		getFeatureAdapter.setRootElement(xmlAdapter.getRootElement());
		GetFeature getFeature = getFeatureAdapter.parse();

		List<Query> queries = getFeature.getQueries();
		FilterQuery filterQuery = (FilterQuery) queries.get(0);

		ProjectionClause[] propNames = filterQuery.getProjectionClauses();

		assertEquals(propNames.length, 3);
		assertEquals(((PropertyName) propNames[0]).getPropertyName().getAsText(), "myns:wkbGeom");
		assertEquals(((PropertyName) propNames[1]).getPropertyName().getAsText(), "myns:tileId");
		assertEquals(((PropertyName) propNames[2]).getPropertyName().getAsText(), "myns:facId");

		IdFilter filter = (IdFilter) filterQuery.getFilter();
		Set<String> ids = filter.getMatchingIds();

		assertEquals(ids.size(), 1);
		assertTrue(ids.contains("InWaterA1M.1013"));
	}

	/**
	 * @throws Exception
	 */
	public void test_V100_EXAMPLE3() throws Exception {

		URL exampleURL = this.getClass().getResource(V100_EXAMPLE3);
		XMLAdapter xmlAdapter = new XMLAdapter(exampleURL);

		GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
		getFeatureAdapter.setRootElement(xmlAdapter.getRootElement());
		GetFeature getFeature = getFeatureAdapter.parse();

		FilterQuery filterQuery = (FilterQuery) getFeature.getQueries().get(0);
		assertEquals(new QName("http://www.someserver.com/myns", "INWATERA_1M"),
				filterQuery.getTypeNames()[0].getFeatureTypeName());
		IdFilter idFilter = (IdFilter) filterQuery.getFilter();
		Set<String> matchingIds = idFilter.getMatchingIds();
		assertTrue(matchingIds.size() == 3 && matchingIds.contains("INWATERA_1M.1013")
				&& matchingIds.contains("INWATERA_1M.1014") && matchingIds.contains("INWATERA_1M.1015"));
	}

	/**
	 * @throws Exception When the version of the GetFeature document is not supported for
	 * parsing (superfluous in this case, since we are testing 1.1.0 files and parsing is
	 * supported for this version)
	 */
	@Test
	public void test_V110_EXAMPLE03() throws Exception {

		URL exampleURL = this.getClass().getResource(V110_EXAMPLE03);
		XMLAdapter xmlAdapter = new XMLAdapter(exampleURL);

		GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
		getFeatureAdapter.setRootElement(xmlAdapter.getRootElement());
		GetFeature getFeature = getFeatureAdapter.parse();

		List<Query> queries = getFeature.getQueries();
		FilterQuery filterQuery = (FilterQuery) queries.get(0);

		IdFilter filter = (IdFilter) filterQuery.getFilter();
		Set<String> ids = filter.getMatchingIds();

		assertEquals(ids.size(), 3);
		assertTrue(ids.contains("InWaterA1M.1013"));
		assertTrue(ids.contains("InWaterA1M.1014"));
		assertTrue(ids.contains("InWaterA1M.1015"));
	}

	/**
	 * @throws Exception
	 */
	public void test_V100_EXAMPLE4() throws Exception {

		URL exampleURL = this.getClass().getResource(V100_EXAMPLE4);
		XMLAdapter xmlAdapter = new XMLAdapter(exampleURL);

		GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
		getFeatureAdapter.setRootElement(xmlAdapter.getRootElement());
		GetFeature getFeature = getFeatureAdapter.parse();

		FilterQuery filterQuery = (FilterQuery) getFeature.getQueries().get(0);
		assertEquals(new QName("http://www.someserver.com/myns", "INWATERA_1M"),
				filterQuery.getTypeNames()[0].getFeatureTypeName());

		assertEquals("myns:WKB_GEOM",
				((PropertyName) filterQuery.getProjectionClauses()[0]).getPropertyName().getAsText());
		assertEquals("myns:TILE_ID",
				((PropertyName) filterQuery.getProjectionClauses()[1]).getPropertyName().getAsText());
		IdFilter idFilter = (IdFilter) filterQuery.getFilter();
		Set<String> ids = idFilter.getMatchingIds();

		assertTrue(ids.size() == 3 && ids.contains("INWATERA_1M.1013") && ids.contains("INWATERA_1M.1014")
				&& ids.contains("INWATERA_1M.1015"));

	}

	/**
	 * @throws Exception When the version of the GetFeature document is not supported for
	 * parsing (superfluous in this case, since we are testing 1.1.0 files and parsing is
	 * supported for this version)
	 */
	@Test
	public void test_V110_EXAMPLE04() throws Exception {

		URL exampleURL = this.getClass().getResource(V110_EXAMPLE04);
		XMLAdapter xmlAdapter = new XMLAdapter(exampleURL);

		GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
		getFeatureAdapter.setRootElement(xmlAdapter.getRootElement());
		GetFeature getFeature = getFeatureAdapter.parse();

		List<Query> queries = getFeature.getQueries();
		FilterQuery filterQuery = (FilterQuery) queries.get(0);

		ProjectionClause[] propNames = filterQuery.getProjectionClauses();

		assertEquals(propNames.length, 2);
		assertEquals(((PropertyName) propNames[0]).getPropertyName().getAsText(), "myns:wkbGeom");
		assertEquals(((PropertyName) propNames[1]).getPropertyName().getAsText(), "myns:tileId");

		IdFilter filter = (IdFilter) filterQuery.getFilter();
		Set<String> ids = filter.getMatchingIds();

		assertEquals(ids.size(), 3);
		assertTrue(ids.contains("InWaterA1M.1013"));
		assertTrue(ids.contains("InWaterA1M.1014"));
		assertTrue(ids.contains("InWaterA1M.1015"));
	}

	/**
	 * @throws Exception
	 */
	public void test_V100_EXAMPLE5() throws Exception {

		URL exampleURL = this.getClass().getResource(V100_EXAMPLE5);
		XMLAdapter xmlAdapter = new XMLAdapter(exampleURL);

		GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
		getFeatureAdapter.setRootElement(xmlAdapter.getRootElement());
		GetFeature getFeature = getFeatureAdapter.parse();

		assertEquals(valueOf(10000), getFeature.getPresentationParams().getCount());

		FilterQuery filterQuery = (FilterQuery) getFeature.getQueries().get(0);
		assertEquals(new QName("http://www.someserver.com/myns", "INWATERA_1M"),
				filterQuery.getTypeNames()[0].getFeatureTypeName());
	}

	/**
	 * @throws Exception
	 */
	public void test_V100_EXAMPLE6() throws Exception {

		URL exampleURL = this.getClass().getResource(V100_EXAMPLE6);
		XMLAdapter xmlAdapter = new XMLAdapter(exampleURL);

		GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
		getFeatureAdapter.setRootElement(xmlAdapter.getRootElement());
		GetFeature getFeature = getFeatureAdapter.parse();

		FilterQuery filterQuery = (FilterQuery) getFeature.getQueries().get(0);
		assertEquals(new QName("http://www.someserver.com/myns", "INWATERA_1M"),
				filterQuery.getTypeNames()[0].getFeatureTypeName());

		filterQuery = (FilterQuery) getFeature.getQueries().get(1);
		assertEquals(new QName("http://www.someserver.com/myns", "BUILTUPA_1M"),
				filterQuery.getTypeNames()[0].getFeatureTypeName());

		filterQuery = (FilterQuery) getFeature.getQueries().get(2);
		assertEquals(new QName("http://demo.cubewerx.com/yourns", "ROADL_1M"),
				filterQuery.getTypeNames()[0].getFeatureTypeName());
	}

	/**
	 * @throws Exception
	 */
	public void test_V100_EXAMPLE7() throws Exception {

		URL exampleURL = this.getClass().getResource(V100_EXAMPLE7);
		XMLAdapter xmlAdapter = new XMLAdapter(exampleURL);

		GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
		getFeatureAdapter.setRootElement(xmlAdapter.getRootElement());
		GetFeature getFeature = getFeatureAdapter.parse();

		FilterQuery filterQuery = (FilterQuery) getFeature.getQueries().get(0);
		assertEquals(new QName("http://www.someserver.com/myns", "HYDROGRAPHY"),
				filterQuery.getTypeNames()[0].getFeatureTypeName());

		assertEquals("myns:GEOTEMP",
				((PropertyName) filterQuery.getProjectionClauses()[0]).getPropertyName().getAsText());
		assertEquals("myns:DEPTH",
				((PropertyName) filterQuery.getProjectionClauses()[1]).getPropertyName().getAsText());
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("boxing")
	public void test_V100_EXAMPLE8() throws Exception {

		URL exampleURL = this.getClass().getResource(V100_EXAMPLE8);
		XMLAdapter xmlAdapter = new XMLAdapter(exampleURL);

		GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
		getFeatureAdapter.setRootElement(xmlAdapter.getRootElement());
		GetFeature getFeature = getFeatureAdapter.parse();

		FilterQuery filterQuery = (FilterQuery) getFeature.getQueries().get(0);
		assertEquals(new QName("http://www.someserver.com/myns", "ROADS"),
				filterQuery.getTypeNames()[0].getFeatureTypeName());

		assertEquals("myns:PATH", ((PropertyName) filterQuery.getProjectionClauses()[0]).getPropertyName().getAsText());
		assertEquals("myns:LANES",
				((PropertyName) filterQuery.getProjectionClauses()[1]).getPropertyName().getAsText());
		assertEquals("myns:SURFACETYPE",
				((PropertyName) filterQuery.getProjectionClauses()[2]).getPropertyName().getAsText());

		OperatorFilter opFilter = (OperatorFilter) filterQuery.getFilter();
		Within within = (Within) opFilter.getOperator();
		assertEquals("myns:PATH", within.getPropName().getAsText());
		Envelope env = (Envelope) within.getGeometry();
		assertEquals(50.0, env.getMin().get0());
		assertEquals(40.0, env.getMin().get1());
		assertEquals(100.0, env.getMax().get0());
		assertEquals(60.0, env.getMax().get1());
	}

	// /**
	// * @throws Exception
	// * When the version of the GetFeature document is not supported for parsing
	// (superfluous in this case,
	// * since we are testing 1.1.0 files and parsing is supported for this version)
	// */
	// @Test
	// public void testEXAMPLE05()
	// throws Exception {
	//
	// URL exampleURL = this.getClass().getResource( V110_EXAMPLE05 );
	// XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );
	//
	// GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
	// getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
	// GetFeature getFeature = getFeatureAdapter.parse();
	//
	// List<Query> queries = getFeature.getQueries();
	// FilterQuery filterQuery = (FilterQuery) queries.get(0);
	//
	// TypeName[] typeNames = filterQuery.getTypeNames();
	//
	// assertEquals( typeNames.length, 1 );
	// assertEquals( typeNames[0].getFeatureTypeName(), new QName(
	// "http://www.someserver.com/myns", "InWaterA_1M" ) );
	// }

	// /**
	// * @throws Exception When the version of the GetFeature document is not supported
	// for
	// * parsing (superfluous in this case, since we are testing 1.1.0 files and parsing
	// is supported for this version)
	// */
	// @Test
	// public void testEXAMPLE06() throws Exception {
	//
	// URL exampleURL = this.getClass().getResource( V110_EXAMPLE06 );
	// XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );
	//
	// GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
	// getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
	// GetFeature getFeature = getFeatureAdapter.parse();
	//
	// List<Query> queries = getFeature.getQueries();
	//
	// assertEquals( queries.length, 3 );
	//
	// FilterQuery filterQuery = (FilterQuery) queries.get(0);
	// TypeName[] typeNames = filterQuery.getTypeNames();
	// assertEquals( typeNames.length, 1 );
	// assertEquals( typeNames[0].getFeatureTypeName(),
	// new QName( "http://www.someserver.com/myns", "InWaterA_1M" ) );
	//
	// filterQuery = (FilterQuery) queries[1];
	// typeNames = filterQuery.getTypeNames();
	// assertEquals( typeNames.length, 1 );
	// assertEquals( typeNames[0].getFeatureTypeName(),
	// new QName( "http://www.someserver.com/myns", "BuiltUpA_1M" ) );
	//
	// filterQuery = (FilterQuery) queries[2];
	// typeNames = filterQuery.getTypeNames();
	// assertEquals( typeNames.length, 1 );
	// assertEquals( typeNames[0].getFeatureTypeName(),
	// new QName( "http://demo.cubewerx.com/yourns", "RoadL_1M" ) );
	// }

	/**
	 * @throws Exception
	 */
	public void test_V100_EXAMPLE9() throws Exception {

		URL exampleURL = this.getClass().getResource(V100_EXAMPLE9);
		XMLAdapter xmlAdapter = new XMLAdapter(exampleURL);

		GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
		getFeatureAdapter.setRootElement(xmlAdapter.getRootElement());
		GetFeature getFeature = getFeatureAdapter.parse();

		FilterQuery filterQuery = (FilterQuery) getFeature.getQueries().get(0);
		assertEquals(new QName("Person"), filterQuery.getTypeNames()[0].getFeatureTypeName());

		assertEquals("myns:Person/myns:LastName",
				((PropertyName) filterQuery.getProjectionClauses()[0]).getPropertyName().getAsText());

		OperatorFilter opFilter = (OperatorFilter) filterQuery.getFilter();
		assertTrue(opFilter.getOperator() instanceof And);

		And rootOp = (And) opFilter.getOperator();
		assertTrue(rootOp.getParameter(0) instanceof And);
		And op0 = (And) rootOp.getParameter(0);

		assertTrue(op0.getParameter(0) instanceof PropertyIsGreaterThanOrEqualTo);
		PropertyIsGreaterThanOrEqualTo op00 = (PropertyIsGreaterThanOrEqualTo) op0.getParameter(0);
		assertEquals("myns:Person/myns:Address/myns:StreetNumber", ((ValueReference) op00.getParameter1()).getAsText());
		assertEquals("10000", ((Literal<?>) op00.getParameter2()).getValue().toString());

		assertTrue(rootOp.getParameter(1) instanceof And);
		And op1 = (And) rootOp.getParameter(1);

		assertTrue(op1.getParameter(0) instanceof PropertyIsEqualTo);
		PropertyIsEqualTo op10 = (PropertyIsEqualTo) op1.getParameter(0);
		assertEquals("myns:Person/myns:Address/myns:StreetName", ((ValueReference) op10.getParameter1()).getAsText());
		assertEquals("Main St.", ((Literal<?>) op10.getParameter2()).getValue().toString());

		PropertyIsEqualTo op11 = (PropertyIsEqualTo) op1.getParameter(1);
		assertEquals("myns:Person/myns:Address/myns:City", ((ValueReference) op11.getParameter1()).getAsText());
		assertEquals("SomeTown", ((Literal<?>) op11.getParameter2()).getValue().toString());

		PropertyIsEqualTo op12 = (PropertyIsEqualTo) op1.getParameter(2);
		assertEquals("myns:Person/myns:Sex", ((ValueReference) op12.getParameter1()).getAsText());
		assertEquals("Female", ((Literal<?>) op12.getParameter2()).getValue().toString());

		PropertyIsGreaterThan op13 = (PropertyIsGreaterThan) op1.getParameter(3);
		assertEquals("myns:Person/myns:Salary", ((ValueReference) op13.getParameter1()).getAsText());
		assertEquals("35000", ((Literal<?>) op13.getParameter2()).getValue().toString());

	}

	/**
	 * @throws Exception When the version of the GetFeature document is not supported for
	 * parsing (superfluous in this case, since we are testing 1.1.0 files and parsing is
	 * supported for this version)
	 */
	@Test
	public void test_V110_EXAMPLE09() throws Exception {

		URL exampleURL = this.getClass().getResource(V110_EXAMPLE09);
		XMLAdapter xmlAdapter = new XMLAdapter(exampleURL);

		GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
		getFeatureAdapter.setRootElement(xmlAdapter.getRootElement());
		GetFeature getFeature = getFeatureAdapter.parse();

		List<Query> queries = getFeature.getQueries();
		FilterQuery filterQuery = (FilterQuery) queries.get(0);
		TypeName[] typeNames = filterQuery.getTypeNames();

		assertEquals(typeNames.length, 1);
		assertEquals(new QName("Person"), typeNames[0].getFeatureTypeName());

		OperatorFilter opFilter = (OperatorFilter) filterQuery.getFilter();
		assertEquals(opFilter.getOperator().getType(), LOGICAL);

		LogicalOperator logOp = (LogicalOperator) opFilter.getOperator();

		assertEquals(logOp.getSubType(), AND);
		And andOp = (And) logOp;
		Operator op1 = andOp.getParameter(0);

		assertEquals(op1.getType(), LOGICAL);
		LogicalOperator logOp1 = (LogicalOperator) op1;
		assertEquals(logOp1.getSubType(), AND);
		And andOp1 = (And) logOp1;

		Operator op11 = andOp1.getParameter(0);
		assertEquals(op11.getType(), COMPARISON);
		BinaryComparisonOperator compOp11 = (BinaryComparisonOperator) op11;
		assertTrue(compOp11 instanceof PropertyIsGreaterThanOrEqualTo);
		assertTrue(((PropertyIsGreaterThanOrEqualTo) op11).getParameter1() instanceof ValueReference);
		assertEquals(((ValueReference) ((PropertyIsGreaterThanOrEqualTo) op11).getParameter1()).getAsText(),
				"myns:Person/myns:mailAddress/myns:Address/myns:streetNumber");
		assertTrue(((PropertyIsGreaterThanOrEqualTo) op11).getParameter2() instanceof Literal<?>);
		assertEquals(((Literal<?>) ((PropertyIsGreaterThanOrEqualTo) op11).getParameter2()).getValue().toString(),
				"10000");

		Operator op12 = andOp1.getParameter(1);
		assertEquals(op12.getType(), COMPARISON);
		BinaryComparisonOperator compOp12 = (BinaryComparisonOperator) op12;
		assertTrue(compOp12 instanceof PropertyIsLessThanOrEqualTo);
		assertTrue(((PropertyIsLessThanOrEqualTo) op12).getParameter1() instanceof ValueReference);
		assertEquals(((ValueReference) ((PropertyIsLessThanOrEqualTo) op12).getParameter1()).getAsText(),
				"myns:Person/myns:mailAddress/myns:Address/myns:streetNumber");
		assertTrue(((PropertyIsLessThanOrEqualTo) op12).getParameter2() instanceof Literal<?>);
		assertEquals(((Literal<?>) ((PropertyIsLessThanOrEqualTo) op12).getParameter2()).getValue().toString(),
				"10999");

		Operator op2 = andOp.getParameter(1);

		assertEquals(op2.getType(), LOGICAL);
		LogicalOperator logOp2 = (LogicalOperator) op2;
		assertEquals(logOp2.getSubType(), AND);
		And andOp2 = (And) logOp2;

		Operator op21 = andOp2.getParameter(0);
		assertEquals(op21.getType(), COMPARISON);
		BinaryComparisonOperator compOp21 = (BinaryComparisonOperator) op21;
		assertTrue(compOp21 instanceof PropertyIsEqualTo);
		assertTrue(((PropertyIsEqualTo) op21).getParameter1() instanceof ValueReference);
		assertEquals(((ValueReference) ((PropertyIsEqualTo) op21).getParameter1()).getAsText(),
				"myns:Person/myns:mailAddress/myns:Address/myns:streetName");
		assertTrue(((PropertyIsEqualTo) op21).getParameter2() instanceof Literal<?>);
		assertEquals(((Literal<?>) ((PropertyIsEqualTo) op21).getParameter2()).getValue().toString(), "Main St.");

		Operator op22 = andOp2.getParameter(1);
		assertEquals(op22.getType(), COMPARISON);
		BinaryComparisonOperator compOp22 = (BinaryComparisonOperator) op22;
		assertTrue(compOp22 instanceof PropertyIsEqualTo);
		assertTrue(((PropertyIsEqualTo) op22).getParameter1() instanceof ValueReference);
		assertEquals(((ValueReference) ((PropertyIsEqualTo) op22).getParameter1()).getAsText(),
				"myns:Person/myns:mailAddress/myns:Address/myns:city");
		assertTrue(((PropertyIsEqualTo) op22).getParameter2() instanceof Literal<?>);
		assertEquals(((Literal<?>) ((PropertyIsEqualTo) op22).getParameter2()).getValue().toString(), "SomeTown");

		Operator op23 = andOp2.getParameter(2);
		assertEquals(op23.getType(), COMPARISON);
		BinaryComparisonOperator compOp23 = (BinaryComparisonOperator) op23;
		assertTrue(compOp23 instanceof PropertyIsEqualTo);
		assertTrue(((PropertyIsEqualTo) op23).getParameter1() instanceof ValueReference);
		assertEquals(((ValueReference) ((PropertyIsEqualTo) op23).getParameter1()).getAsText(), "myns:Person/myns:sex");
		assertTrue(((PropertyIsEqualTo) op23).getParameter2() instanceof Literal<?>);
		assertEquals(((Literal<?>) ((PropertyIsEqualTo) op23).getParameter2()).getValue().toString(), "Female");

		Operator op24 = andOp2.getParameter(3);
		assertEquals(op24.getType(), COMPARISON);
		BinaryComparisonOperator compOp24 = (BinaryComparisonOperator) op24;
		assertTrue(compOp24 instanceof PropertyIsGreaterThan);
		assertTrue(((PropertyIsGreaterThan) op24).getParameter1() instanceof ValueReference);
		assertEquals(((ValueReference) ((PropertyIsGreaterThan) op24).getParameter1()).getAsText(),
				"myns:Person/myns:salary");
		assertTrue(((PropertyIsGreaterThan) op24).getParameter2() instanceof Literal<?>);
		assertEquals(((Literal<?>) ((PropertyIsGreaterThan) op24).getParameter2()).getValue().toString(), "35000");
	}

	/**
	 * @throws Exception When the version of the GetFeature document is not supported for
	 * parsing (superfluous in this case, since we are testing 1.1.0 files and parsing is
	 * supported for this version)
	 */
	@Test
	public void test_V110_EXAMPLE10() throws Exception {

		URL exampleURL = this.getClass().getResource(V110_EXAMPLE10);
		XMLAdapter xmlAdapter = new XMLAdapter(exampleURL);

		GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
		getFeatureAdapter.setRootElement(xmlAdapter.getRootElement());
		GetFeature getFeature = getFeatureAdapter.parse();

		List<Query> queries = getFeature.getQueries();
		FilterQuery filterQuery = (FilterQuery) queries.get(0);

		ProjectionClause[] propNames = filterQuery.getProjectionClauses();

		assertEquals(propNames.length, 2);
		assertEquals(((PropertyName) propNames[0]).getPropertyName().getAsText(), "gml:name");
		assertEquals(((PropertyName) propNames[1]).getPropertyName().getAsText(), "gml:directedNode");

		IdFilter filter = (IdFilter) filterQuery.getFilter();
		Set<String> ids = filter.getMatchingIds();

		assertEquals(ids.size(), 1);
		assertTrue(ids.contains("t1"));
	}

	/**
	 * @throws Exception When the version of the GetFeature document is not supported for
	 * parsing (superfluous in this case, since we are testing 1.1.0 files and parsing is
	 * supported for this version)
	 */
	@Test
	public void test_V110_EXAMPLE11() throws Exception {

		URL exampleURL = this.getClass().getResource(V110_EXAMPLE11);
		XMLAdapter xmlAdapter = new XMLAdapter(exampleURL);

		GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
		getFeatureAdapter.setRootElement(xmlAdapter.getRootElement());
		GetFeature getFeature = getFeatureAdapter.parse();

		List<Query> queries = getFeature.getQueries();
		FilterQuery filterQuery = (FilterQuery) queries.get(0);

		ProjectionClause[] propNames = filterQuery.getProjectionClauses();

		assertEquals(propNames.length, 2);
		assertEquals(((PropertyName) propNames[0]).getPropertyName().getAsText(), "gml:name");
		assertEquals(((PropertyName) propNames[1]).getPropertyName().getAsText(), "gml:directedNode");

		IdFilter filter = (IdFilter) filterQuery.getFilter();
		Set<String> ids = filter.getMatchingIds();

		assertEquals(ids.size(), 1);
		assertTrue(ids.contains("t1"));
	}

	/**
	 * @throws Exception When the version of the GetFeature document is not supported for
	 * parsing (superfluous in this case, since we are testing 1.1.0 files and parsing is
	 * supported for this version)
	 */
	public void test_V110_EXAMPLE12() throws Exception {

		URL exampleURL = this.getClass().getResource(V110_EXAMPLE12);
		XMLAdapter xmlAdapter = new XMLAdapter(exampleURL);

		GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
		getFeatureAdapter.setRootElement(xmlAdapter.getRootElement());
		GetFeature getFeature = getFeatureAdapter.parse();

		List<Query> queries = getFeature.getQueries();
		FilterQuery filterQuery = (FilterQuery) queries.get(0);

		ProjectionClause[] propNames = filterQuery.getProjectionClauses();
		assertEquals(2, propNames.length);
		assertEquals("gml:name", ((PropertyName) propNames[0]).getPropertyName().getAsText());
		assertEquals("2", ((PropertyName) propNames[1]).getResolveParams().getDepth());
		assertEquals(BigInteger.valueOf(120), ((PropertyName) propNames[1]).getResolveParams().getTimeout());
		assertEquals("gml:directedNode", ((PropertyName) propNames[1]).getPropertyName().getAsText());

		IdFilter filter = (IdFilter) filterQuery.getFilter();
		Set<String> ids = filter.getMatchingIds();

		assertEquals(ids.size(), 1);
		assertTrue(ids.contains("t1"));
	}

	@Test
	public void test200Example1() throws Exception {

		GetFeature request = parseExample("wfs200/example1.xml");

		// global request params
		assertEquals(VERSION_200, request.getVersion());
		assertNull(request.getHandle());

		// presentation params
		assertNull(request.getPresentationParams().getStartIndex());
		assertNull(request.getPresentationParams().getCount());
		assertEquals("application/gml+xml; version=3.2", request.getPresentationParams().getOutputFormat());

		// resolve params
		assertNull(request.getResolveParams().getMode());
		assertNull(request.getResolveParams().getDepth());
		assertEquals(null, request.getResolveParams().getTimeout());

		// queries
		assertEquals(1, request.getQueries().size());
		FilterQuery query = (FilterQuery) request.getQueries().get(0);
		assertNull(query.getHandle());
		assertEquals(1, query.getTypeNames().length);
		assertEquals(QName.valueOf("{http://www.someserver.com/myns}InWaterA_1M"),
				query.getTypeNames()[0].getFeatureTypeName());

		// filter
		IdFilter filter = (IdFilter) query.getFilter();
		assertEquals(1, filter.getSelectedIds().size());
		assertEquals("InWaterA_1M.1234", filter.getSelectedIds().get(0).getRid());
	}

	public void test200Example2() throws Exception {
		GetFeature request = parseExample("wfs200/example2.xml");
	}

	public void test200Example3() throws Exception {
		GetFeature request = parseExample("wfs200/example3.xml");
	}

	public void test200Example4() throws Exception {
		GetFeature request = parseExample("wfs200/example4.xml");
	}

	public void test200Example5() throws Exception {
		GetFeature request = parseExample("wfs200/example5.xml");
	}

	public void test200Example6() throws Exception {
		GetFeature request = parseExample("wfs200/example6.xml");
	}

	public void test200Example7() throws Exception {
		GetFeature request = parseExample("wfs200/example7.xml");
	}

	public void test200Example8() throws Exception {
		// GetFeature request = parseExample( "wfs200/example8.xml" );
	}

	public void test200Example9() throws Exception {
		GetFeature request = parseExample("wfs200/example9.xml");
	}

	public void test200Example10() throws Exception {
		GetFeature request = parseExample("wfs200/example10.xml");
	}

	public void test200Example11() throws Exception {
		GetFeature request = parseExample("wfs200/example11.xml");
	}

	public void test200Example12() throws Exception {
		GetFeature request = parseExample("wfs200/example12.xml");
	}

	public void test200Example13() throws Exception {
		// GetFeature request = parseExample( "wfs200/example13.xml" );
	}

	public void test200Example14() throws Exception {
		GetFeature request = parseExample("wfs200/example14.xml");
	}

	public void test200Example15() throws Exception {
		// GetFeature request = parseExample( "wfs200/example15.xml" );
	}

	public void test200Example16() throws Exception {
		GetFeature request = parseExample("wfs200/example16.xml");
	}

	public void test200Example17() throws Exception {
		// GetFeature request = parseExample( "wfs200/example17.xml" );
	}

	public void test200Example18() throws Exception {
		GetFeature request = parseExample("wfs200/example18.xml");
	}

	public void test200Example19() throws Exception {
		GetFeature request = parseExample("wfs200/example19.xml");
	}

	public void testTemporalityExtension100Example4() throws Exception {
		GetFeature request = parseExample("te100/example4.xml");
		List<Query> queries = request.getQueries();
		assertEquals(1, queries.size());
		FilterQuery query = (FilterQuery) queries.get(0);
		assertEquals(1, query.getProjectionClauses().length);
		TimeSliceProjection projectionClause = (TimeSliceProjection) query.getProjectionClauses()[0];
		Filter timeSliceFilter = projectionClause.getTimeSliceFilter();
		assertNotNull(timeSliceFilter);
		Filter filter = query.getFilter();
		assertNotNull(filter);
	}

	private GetFeature parseExample(String resourceName) throws Exception {
		GetFeatureXMLAdapter parser = new GetFeatureXMLAdapter();
		parser.load(GetFeatureXMLAdapterTest.class.getResource(resourceName));
		return parser.parse();
	}

}
