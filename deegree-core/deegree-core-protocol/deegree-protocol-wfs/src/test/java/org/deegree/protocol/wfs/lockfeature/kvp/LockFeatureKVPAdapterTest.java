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
package org.deegree.protocol.wfs.lockfeature.kvp;

import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.spatial.Within;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;
import org.deegree.protocol.wfs.lockfeature.LockFeature;
import org.deegree.protocol.wfs.query.FeatureIdQuery;
import org.deegree.protocol.wfs.query.FilterQuery;
import org.deegree.protocol.wfs.query.Query;
import org.junit.Test;

/**
 * The <code>LockFeatureKVPAdapterTest</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class LockFeatureKVPAdapterTest extends TestCase {

	private final static String EXAMPLE1_WFS110 = "wfs110/example1.kvp";

	private final static String EXAMPLE2_WFS110 = "wfs110/example2.kvp";

	private final static String EXAMPLE3_WFS110 = "wfs110/example3.kvp";

	private final static String EXAMPLE4_WFS110 = "wfs110/example4.kvp";

	private final static String EXAMPLE1_WFS200 = "wfs200/example1.kvp";

	private final static String EXAMPLE2_WFS200 = "wfs200/example2.kvp";

	@Test
	public void testExample1Wfs110() throws Exception {
		URL example = this.getClass().getResource(EXAMPLE1_WFS110);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(example);

		LockFeature lockFeature = LockFeatureKVPAdapter.parse(kvpMap);
		FilterQuery filterLock = (FilterQuery) lockFeature.getQueries().get(0);
		assertEquals(new QName("InWaterA_1M"), filterLock.getTypeNames()[0].getFeatureTypeName());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testExample2Wfs110() throws Exception {
		URL example = this.getClass().getResource(EXAMPLE2_WFS110);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(example);

		LockFeature lockFeature = LockFeatureKVPAdapter.parse(kvpMap);
		FeatureIdQuery featureLock = (FeatureIdQuery) lockFeature.getQueries().get(0);
		assertEquals("RoadL_1M.1013", featureLock.getFeatureIds()[0]);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testExample3Wfs110() throws Exception {
		URL example = this.getClass().getResource(EXAMPLE3_WFS110);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(example);

		LockFeature lockFeature = LockFeatureKVPAdapter.parse(kvpMap);
		FilterQuery featureLock0 = (FilterQuery) lockFeature.getQueries().get(0);
		assertEquals(new QName("InWaterA_1M"), featureLock0.getTypeNames()[0].getFeatureTypeName());
		FilterQuery featureLock1 = (FilterQuery) lockFeature.getQueries().get(1);
		assertEquals(new QName("BuiltUpA_1M"), featureLock1.getTypeNames()[0].getFeatureTypeName());
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testExample4Wfs110() throws Exception {
		URL example = this.getClass().getResource(EXAMPLE4_WFS110);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(example);
		LockFeature lockFeature = LockFeatureKVPAdapter.parse(kvpMap);
		assertTrue(lockFeature.getLockAll());
		assertEquals(new BigInteger("300"), lockFeature.getExpiryInSeconds());

		List<Query> queries = lockFeature.getQueries();
		assertEquals(2, queries.size());

		FilterQuery filterQuery1 = (FilterQuery) queries.get(0);

		OperatorFilter filter1 = (OperatorFilter) filterQuery1.getFilter();
		assertTrue(filter1.getOperator() instanceof Within);

		Within within = (Within) filter1.getOperator();
		assertEquals("wkbGeom", within.getPropName().getAsText());
		Envelope env = (Envelope) within.getGeometry();
		verifyEnvelope(env, 10, 10, 20, 20);
	}

	@Test
	public void testExample1Wfs200() throws Exception {
		URL example = this.getClass().getResource(EXAMPLE1_WFS200);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(example);
		LockFeature lockFeature = LockFeatureKVPAdapter.parse(kvpMap);
		assertNull(lockFeature.getHandle());
		assertNull(lockFeature.getExistingLockId());
		assertNull(lockFeature.getExpiryInSeconds());
		assertNull(lockFeature.getLockAll());
		assertEquals(1, lockFeature.getQueries().size());
	}

	@Test
	public void testExample2Wfs200() throws Exception {
		URL example = this.getClass().getResource(EXAMPLE2_WFS200);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(example);
		LockFeature lockFeature = LockFeatureKVPAdapter.parse(kvpMap);
		assertNull(lockFeature.getHandle());
		assertEquals("LOCK_1", lockFeature.getExistingLockId());
		assertEquals(new BigInteger("38348348884895485485485623783487548745587548754"),
				lockFeature.getExpiryInSeconds());
		assertFalse(lockFeature.getLockAll());
		assertEquals(1, lockFeature.getQueries().size());
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

}