/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.services.encoding;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.deegree.services.encoding.LimitedSupportedEncodingsTest.RequestType.CreateStoredQuery;
import static org.deegree.services.encoding.LimitedSupportedEncodingsTest.RequestType.DescribeFeatureType;
import static org.deegree.services.encoding.LimitedSupportedEncodingsTest.RequestType.GetCapabilities;
import static org.deegree.services.encoding.LimitedSupportedEncodingsTest.RequestType.GetFeatureWithLock;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class LimitedSupportedEncodingsTest {

	private LimitedSupportedEncodings limitedSupportedEncodings = prepareLimitedSupportedEncodings();

	@Test
	public void testIsEncodingSupportedUnsupportedRequestType() {
		assertFalse(limitedSupportedEncodings.isEncodingSupported(GetFeatureWithLock, "xml"));
		assertFalse(limitedSupportedEncodings.isEncodingSupported(GetFeatureWithLock, "kvp"));
		assertFalse(limitedSupportedEncodings.isEncodingSupported(GetFeatureWithLock, "soap"));
	}

	@Test
	public void testIsEncodingSupportedSupportedRequestType() {
		assertTrue(limitedSupportedEncodings.isEncodingSupported(GetCapabilities, "xml"));
		assertTrue(limitedSupportedEncodings.isEncodingSupported(GetCapabilities, "kvp"));
		assertTrue(limitedSupportedEncodings.isEncodingSupported(GetCapabilities, "soap"));
	}

	@Test
	public void testIsEncodingSupportedPartlySupportedRequestType() {
		assertFalse(limitedSupportedEncodings.isEncodingSupported(DescribeFeatureType, "xml"));
		assertTrue(limitedSupportedEncodings.isEncodingSupported(DescribeFeatureType, "kvp"));
		assertTrue(limitedSupportedEncodings.isEncodingSupported(DescribeFeatureType, "soap"));
	}

	private LimitedSupportedEncodings prepareLimitedSupportedEncodings() {
		LimitedSupportedEncodings limitedSupportedEncodings = new LimitedSupportedEncodings();
		limitedSupportedEncodings.addEnabledEncodings(CreateStoredQuery, new HashSet<String>(singletonList("kvp")));
		limitedSupportedEncodings.addEnabledEncodings(DescribeFeatureType, new HashSet<String>(asList("kvp", "soap")));
		limitedSupportedEncodings.addEnabledEncodings(GetCapabilities,
				new HashSet<String>(asList("kvp", "soap", "xml")));
		return limitedSupportedEncodings;
	}

	enum RequestType {

		GetCapabilities, GetFeatureWithLock, DescribeFeatureType, CreateStoredQuery

	}

}