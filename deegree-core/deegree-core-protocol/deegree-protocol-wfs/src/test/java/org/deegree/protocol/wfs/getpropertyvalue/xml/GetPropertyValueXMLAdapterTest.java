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

package org.deegree.protocol.wfs.getpropertyvalue.xml;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import junit.framework.Assert;

import org.deegree.commons.tom.ResolveMode;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.logical.And;
import org.deegree.protocol.wfs.getpropertyvalue.GetPropertyValue;
import org.deegree.protocol.wfs.getpropertyvalue.xml.GetPropertyValueXMLAdapter;
import org.deegree.protocol.wfs.query.FilterQuery;
import org.junit.Test;

/**
 * Tests for {@link GetPropertyValueXMLAdapter}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class GetPropertyValueXMLAdapterTest {

	@Test
	public void test200Example2() throws Exception {
		GetPropertyValueXMLAdapter parser = new GetPropertyValueXMLAdapter();
		parser.load(GetPropertyValueXMLAdapterTest.class.getResource("wfs200/example2.xml"));
		GetPropertyValue request = parser.parse();
		assertEquals(VERSION_200, request.getVersion());
		assertEquals("myns:location", request.getValueReference().getAsText());
		assertNull(request.getPresentationParams().getStartIndex());
		assertNull(request.getPresentationParams().getCount());
		assertEquals("application/xml; subtype=gml/3.2", request.getPresentationParams().getOutputFormat());
		assertEquals(ResolveMode.LOCAL, request.getResolveParams().getMode());
		assertEquals("*", request.getResolveParams().getDepth());
		assertEquals(null, request.getResolveParams().getTimeout());
		FilterQuery query = (FilterQuery) request.getQuery();
		OperatorFilter filter = (OperatorFilter) query.getFilter();
		Assert.assertTrue(filter.getOperator() instanceof And);
	}

	@Test
	public void test200Example4() throws Exception {
		GetPropertyValueXMLAdapter parser = new GetPropertyValueXMLAdapter();
		parser.load(GetPropertyValueXMLAdapterTest.class.getResource("wfs200/example4.xml"));
		GetPropertyValue request = parser.parse();
		assertEquals(VERSION_200, request.getVersion());
		assertEquals("valueOf(myns:livesIn)/valueOf(myns:frontsOn)/abc:numLanes",
				request.getValueReference().getAsText());
		assertNull(request.getPresentationParams().getStartIndex());
		assertNull(request.getPresentationParams().getCount());
		assertEquals("application/xml; subtype=gml/3.2", request.getPresentationParams().getOutputFormat());
		assertNull(request.getResolveParams().getMode());
		assertNull(request.getResolveParams().getDepth());
		assertNull(request.getResolveParams().getTimeout());
		FilterQuery query = (FilterQuery) request.getQuery();
		OperatorFilter filter = (OperatorFilter) query.getFilter();
		Assert.assertTrue(filter.getOperator() instanceof And);
	}

	@Test
	public void test200Example6() throws Exception {
		GetPropertyValueXMLAdapter parser = new GetPropertyValueXMLAdapter();
		parser.load(GetPropertyValueXMLAdapterTest.class.getResource("wfs200/example6.xml"));
		GetPropertyValue request = parser.parse();
		assertEquals(VERSION_200, request.getVersion());
		assertEquals("valueOf(myns:livesIn)/valueof(myns:mailAddress)/myns:postalCode",
				request.getValueReference().getAsText());
		assertNull(request.getPresentationParams().getStartIndex());
		assertNull(request.getPresentationParams().getCount());
		assertEquals("application/xml; subtype=gml/3.2", request.getPresentationParams().getOutputFormat());
		assertNull(request.getResolveParams().getMode());
		assertNull(request.getResolveParams().getDepth());
		assertNull(request.getResolveParams().getTimeout());
		FilterQuery query = (FilterQuery) request.getQuery();
		OperatorFilter filter = (OperatorFilter) query.getFilter();
		Assert.assertTrue(filter.getOperator() instanceof And);
	}

	@Test
	public void test200Example7() throws Exception {
		GetPropertyValueXMLAdapter parser = new GetPropertyValueXMLAdapter();
		parser.load(GetPropertyValueXMLAdapterTest.class.getResource("wfs200/example7.xml"));
		GetPropertyValue request = parser.parse();
		assertEquals(VERSION_200, request.getVersion());
		assertEquals("myns:age", request.getValueReference().getAsText());
		assertNull(request.getPresentationParams().getStartIndex());
		assertNull(request.getPresentationParams().getCount());
		assertEquals("application/xml; subtype=gml/3.2", request.getPresentationParams().getOutputFormat());
		assertNull(request.getResolveParams().getMode());
		assertNull(request.getResolveParams().getDepth());
		assertNull(request.getResolveParams().getTimeout());
		FilterQuery query = (FilterQuery) request.getQuery();
		OperatorFilter filter = (OperatorFilter) query.getFilter();
		Assert.assertTrue(filter.getOperator() instanceof And);
	}

	@Test
	public void test200Example8() throws Exception {
		GetPropertyValueXMLAdapter parser = new GetPropertyValueXMLAdapter();
		parser.load(GetPropertyValueXMLAdapterTest.class.getResource("wfs200/example8.xml"));
		GetPropertyValue request = parser.parse();
		assertEquals(VERSION_200, request.getVersion());
		assertEquals("myns:phone", request.getValueReference().getAsText());
		assertNull(request.getPresentationParams().getStartIndex());
		assertNull(request.getPresentationParams().getCount());
		assertEquals("application/xml; subtype=gml/3.2", request.getPresentationParams().getOutputFormat());
		assertNull(request.getResolveParams().getMode());
		assertNull(request.getResolveParams().getDepth());
		assertNull(request.getResolveParams().getTimeout());
		FilterQuery query = (FilterQuery) request.getQuery();
		OperatorFilter filter = (OperatorFilter) query.getFilter();
		Assert.assertTrue(filter.getOperator() instanceof And);
	}

	@Test
	public void test200Example9() throws Exception {
		GetPropertyValueXMLAdapter parser = new GetPropertyValueXMLAdapter();
		parser.load(GetPropertyValueXMLAdapterTest.class.getResource("wfs200/example9.xml"));
		GetPropertyValue request = parser.parse();
		assertEquals(VERSION_200, request.getVersion());
		assertEquals("myns:phone[2]", request.getValueReference().getAsText());
		assertNull(request.getPresentationParams().getStartIndex());
		assertNull(request.getPresentationParams().getCount());
		assertEquals("application/xml; subtype=gml/3.2", request.getPresentationParams().getOutputFormat());
		assertNull(request.getResolveParams().getMode());
		assertNull(request.getResolveParams().getDepth());
		assertNull(request.getResolveParams().getTimeout());
		FilterQuery query = (FilterQuery) request.getQuery();
		OperatorFilter filter = (OperatorFilter) query.getFilter();
		Assert.assertTrue(filter.getOperator() instanceof And);
	}

}
