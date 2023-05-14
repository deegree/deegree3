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
package org.deegree.protocol.wfs.getfeaturewithlock.kvp;

import java.math.BigInteger;
import java.net.URL;
import java.util.Map;

import junit.framework.TestCase;

import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.protocol.wfs.getfeaturewithlock.GetFeatureWithLock;
import org.junit.Test;

/**
 * The <code>GetFeatureWithLockKVPAdapterTest</code> class tests the GetFeatureWithLock
 * kvp adapter.
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class GetFeatureWithLockKVPAdapterTest extends TestCase {

	private final String EXAMPLE1WFS110 = "wfs110/example1.kvp";

	private final String EXAMPLE1WFS200 = "wfs200/example1.kvp";

	private final String EXAMPLE2WFS200 = "wfs200/example2.kvp";

	@Test
	public void testExample1Wfs110() throws Exception {
		URL example1 = this.getClass().getResource(EXAMPLE1WFS110);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(example1);

		GetFeatureWithLock getFeatureWL = GetFeatureWithLockKVPAdapter.parse(kvpMap);
		assertEquals(new BigInteger("60"), getFeatureWL.getExpiryInSeconds());
	}

	@Test
	public void testExample1Wfs200() throws Exception {
		URL example1 = this.getClass().getResource(EXAMPLE1WFS200);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(example1);

		GetFeatureWithLock getFeatureWL = GetFeatureWithLockKVPAdapter.parse(kvpMap);
		assertNull(getFeatureWL.getExpiryInSeconds());
		assertNull(getFeatureWL.getLockAll());
	}

	@Test
	public void testExample2Wfs200() throws Exception {
		URL example2 = this.getClass().getResource(EXAMPLE2WFS200);
		Map<String, String> kvpMap = KVPUtils.readFileIntoMap(example2);

		GetFeatureWithLock getFeatureWL = GetFeatureWithLockKVPAdapter.parse(kvpMap);
		assertEquals(new BigInteger("300"), getFeatureWL.getExpiryInSeconds());
		assertFalse(getFeatureWL.getLockAll());
	}

}
