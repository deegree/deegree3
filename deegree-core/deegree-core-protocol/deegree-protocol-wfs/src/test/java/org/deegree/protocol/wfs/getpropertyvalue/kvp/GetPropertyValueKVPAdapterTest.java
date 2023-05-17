/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.wfs.getpropertyvalue.kvp;

import static org.deegree.commons.utils.kvp.KVPUtils.readFileIntoMap;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.deegree.commons.tom.ResolveMode;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.logical.And;
import org.deegree.protocol.wfs.getpropertyvalue.GetPropertyValue;
import org.deegree.protocol.wfs.getpropertyvalue.kvp.GetPropertyValueKVPAdapter;
import org.deegree.protocol.wfs.query.FilterQuery;
import org.deegree.protocol.wfs.query.StoredQuery;
import org.junit.Test;

/**
 * Tests for {@link GetPropertyValueKVPAdapter}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class GetPropertyValueKVPAdapterTest extends TestCase {

	@Test
	public void test200Example1() throws Exception {
		Map<String, String> kvpParams = readFileIntoMap(
				GetPropertyValueKVPAdapterTest.class.getResource("wfs200/example1.kvp"));
		GetPropertyValue request = GetPropertyValueKVPAdapter.parse(kvpParams);
		assertEquals(VERSION_200, request.getVersion());
		assertEquals("myns:mailAddress/myns:Address/myns:city", request.getValueReference().getAsText());
		StoredQuery query = (StoredQuery) request.getQuery();
		assertEquals("urn:ogc:def:query:OGC-WFS::GetFeatureById", query.getId());
		assertEquals("p4456", query.getParams().get("ID").getText());
	}

	@Test
	public void test200Example2() throws Exception {
		Map<String, String> kvpParams = readFileIntoMap(
				GetPropertyValueKVPAdapterTest.class.getResource("wfs200/example2.kvp"));
		GetPropertyValue request = GetPropertyValueKVPAdapter.parse(kvpParams);
		assertEquals(VERSION_200, request.getVersion());
		assertEquals(ResolveMode.LOCAL, request.getResolveParams().getMode());
		assertEquals("*", request.getResolveParams().getDepth());
		assertEquals("myns:location", request.getValueReference().getAsText());
		FilterQuery query = (FilterQuery) request.getQuery();
		assertEquals(1, query.getTypeNames().length);
		assertEquals(QName.valueOf("Person"), query.getTypeNames()[0].getFeatureTypeName());
		OperatorFilter filter = (OperatorFilter) query.getFilter();
		Assert.assertTrue(filter.getOperator() instanceof And);
	}

	@Test
	public void test200Example3() throws Exception {
		Map<String, String> kvpParams = readFileIntoMap(
				GetPropertyValueKVPAdapterTest.class.getResource("wfs200/example3.kvp"));
		GetPropertyValue request = GetPropertyValueKVPAdapter.parse(kvpParams);
		assertEquals(VERSION_200, request.getVersion());
		assertEquals("valueOf(myns:livesIn)/valueOf(myns:frontsOn)/abc:numLanes",
				request.getValueReference().getAsText());
		FilterQuery query = (FilterQuery) request.getQuery();
		assertEquals(1, query.getTypeNames().length);
		assertEquals(QName.valueOf("Person"), query.getTypeNames()[0].getFeatureTypeName());
		OperatorFilter filter = (OperatorFilter) query.getFilter();
		Assert.assertTrue(filter.getOperator() instanceof And);
	}

	@Test
	public void test200Example4() throws Exception {
		Map<String, String> kvpParams = readFileIntoMap(
				GetPropertyValueKVPAdapterTest.class.getResource("wfs200/example4.kvp"));
		GetPropertyValue request = GetPropertyValueKVPAdapter.parse(kvpParams);
		assertEquals(VERSION_200, request.getVersion());
		assertEquals("valueOf(myns:livesIn)/valueof(myns:mailAddress)/myns:postalCode",
				request.getValueReference().getAsText());
		FilterQuery query = (FilterQuery) request.getQuery();
		assertEquals(1, query.getTypeNames().length);
		assertEquals(QName.valueOf("Person"), query.getTypeNames()[0].getFeatureTypeName());
		OperatorFilter filter = (OperatorFilter) query.getFilter();
		Assert.assertTrue(filter.getOperator() instanceof And);
	}

	@Test
	public void test200Example5() throws Exception {
		Map<String, String> kvpParams = readFileIntoMap(
				GetPropertyValueKVPAdapterTest.class.getResource("wfs200/example5.kvp"));
		GetPropertyValue request = GetPropertyValueKVPAdapter.parse(kvpParams);
		assertEquals(VERSION_200, request.getVersion());
		assertEquals("myns:age", request.getValueReference().getAsText());
		FilterQuery query = (FilterQuery) request.getQuery();
		assertEquals(1, query.getTypeNames().length);
		assertEquals(QName.valueOf("Person"), query.getTypeNames()[0].getFeatureTypeName());
		OperatorFilter filter = (OperatorFilter) query.getFilter();
		Assert.assertTrue(filter.getOperator() instanceof And);
	}

	@Test
	public void test200Example6() throws Exception {
		Map<String, String> kvpParams = readFileIntoMap(
				GetPropertyValueKVPAdapterTest.class.getResource("wfs200/example6.kvp"));
		GetPropertyValue request = GetPropertyValueKVPAdapter.parse(kvpParams);
		assertEquals(VERSION_200, request.getVersion());
		assertEquals("myns:phone", request.getValueReference().getAsText());
		FilterQuery query = (FilterQuery) request.getQuery();
		assertEquals(1, query.getTypeNames().length);
		assertEquals(QName.valueOf("Person"), query.getTypeNames()[0].getFeatureTypeName());
		OperatorFilter filter = (OperatorFilter) query.getFilter();
		Assert.assertTrue(filter.getOperator() instanceof And);
	}

	@Test
	public void test200Example7() throws Exception {
		Map<String, String> kvpParams = readFileIntoMap(
				GetPropertyValueKVPAdapterTest.class.getResource("wfs200/example7.kvp"));
		GetPropertyValue request = GetPropertyValueKVPAdapter.parse(kvpParams);
		assertEquals(VERSION_200, request.getVersion());
		assertEquals("myns:phone[2]", request.getValueReference().getAsText());
		FilterQuery query = (FilterQuery) request.getQuery();
		assertEquals(1, query.getTypeNames().length);
		assertEquals(QName.valueOf("Person"), query.getTypeNames()[0].getFeatureTypeName());
		OperatorFilter filter = (OperatorFilter) query.getFilter();
		Assert.assertTrue(filter.getOperator() instanceof And);
	}

	@Test
	public void test200Example8() throws Exception {
		Map<String, String> kvpParams = readFileIntoMap(
				GetPropertyValueKVPAdapterTest.class.getResource("wfs200/example8.kvp"));
		GetPropertyValue request = GetPropertyValueKVPAdapter.parse(kvpParams);
		assertEquals(VERSION_200, request.getVersion());
		assertEquals("valueOf(myns:livesIn)/myns:frontsOn", request.getValueReference().getAsText());
		FilterQuery query = (FilterQuery) request.getQuery();
		assertEquals(1, query.getTypeNames().length);
		assertEquals(QName.valueOf("Person"), query.getTypeNames()[0].getFeatureTypeName());
		OperatorFilter filter = (OperatorFilter) query.getFilter();
		Assert.assertTrue(filter.getOperator() instanceof And);
	}

}
