/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.metadata.iso.persistence;

import static org.slf4j.LoggerFactory.getLogger;

import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.cs.CRSUtils;
import org.deegree.filter.Filter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.And;
import org.deegree.filter.spatial.BBOX;
import org.deegree.geometry.GeometryFactory;
import org.deegree.metadata.persistence.MetadataQuery;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 */
public class ISOMetadataStoreTest extends AbstractISOTest {

	private static final Logger LOG = getLogger(ISOMetadataStoreTest.class);

	@Test
	public void testBBoxFilter() throws Exception {
		LOG.info("START Test: testInsert");
		initStore(TstConstants.configURL);
		Assume.assumeNotNull(store);

		TstUtils.insertMetadata(store, TstConstants.tst_9, TstConstants.tst_10, TstConstants.tst_1);
		GeometryFactory gf = new GeometryFactory();
		ValueReference pn = new ValueReference("ows:BoundingBox", nsContext);
		Operator op = new BBOX(pn, gf.createEnvelope(7.30, 49.30, 10.70, 51.70, CRSUtils.EPSG_4326));
		Filter filter = new OperatorFilter(op);
		MetadataQuery query = new MetadataQuery(null, null, filter, null, 1, 10);
		store.getRecordCount(query);
	}

	@Test
	public void testKeywordFilter() throws Exception {
		LOG.info("START Test: testInsert");
		initStore(TstConstants.configURL);
		Assume.assumeNotNull(store);

		TstUtils.insertMetadata(store, TstConstants.tst_9, TstConstants.tst_10, TstConstants.tst_1);
		Literal<PrimitiveValue> lit1 = new Literal<PrimitiveValue>("Hessen Wasser Analyser");
		Operator op1 = new PropertyIsEqualTo(new ValueReference("Title", nsContext), lit1, true, null);

		Literal<PrimitiveValue> lit2 = new Literal<PrimitiveValue>("%Karte%");
		Operator op2 = new PropertyIsLike(new ValueReference("Subject", nsContext), lit2, "%", "_", "?", true, null);

		Operator op = new And(op1, op2);
		Filter filter = new OperatorFilter(op);
		MetadataQuery query = new MetadataQuery(null, null, filter, null, 1, 10);
		store.getRecordCount(query);
	}

	@Test
	public void testEqualKeywordFilter() throws Exception {
		LOG.info("START Test: testInsert");
		initStore(TstConstants.configURL);
		Assume.assumeNotNull(store);

		TstUtils.insertMetadata(store, TstConstants.tst_9, TstConstants.tst_10);
		Literal<PrimitiveValue> lit2 = new Literal<PrimitiveValue>("SPOT 5");
		Operator op = new PropertyIsEqualTo(new ValueReference("Subject", nsContext), lit2, true, null);

		Filter filter = new OperatorFilter(op);
		MetadataQuery query = new MetadataQuery(null, null, filter, null, 1, 10);
		int recordCount = store.getRecordCount(query);

		Assert.assertEquals(1, recordCount);

	}

}
