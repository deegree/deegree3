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

package org.deegree.protocol.wfs.describefeaturetype.kvp;

import static javax.xml.namespace.QName.valueOf;
import static org.deegree.commons.utils.kvp.KVPUtils.readFileIntoMap;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.describefeaturetype.kvp.DescribeFeatureTypeKVPAdapter.parse;

import java.io.IOException;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.deegree.protocol.wfs.WFSConstants;
import org.deegree.protocol.wfs.describefeaturetype.DescribeFeatureType;
import org.junit.Test;

/**
 * Tests for {@link DescribeFeatureTypeKVPAdapter}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class DescribeFeatureTypeKVPAdapterTest extends TestCase {

	@Test
	public void test110Example1() throws IOException {
		Map<String, String> kvpParams = readFileIntoMap(this.getClass().getResource("wfs110/example1.kvp"));
		DescribeFeatureType dft = parse(kvpParams);
		assertEquals(dft.getHandle(), null);
		assertEquals(dft.getOutputFormat(), null);
		assertEquals(dft.getTypeNames().length, 1);
		assertEquals(dft.getTypeNames()[0], new QName("TreesA_1M"));
		assertEquals(dft.getVersion(), WFSConstants.VERSION_110);
	}

	@Test
	public void test110Example2() throws IOException {
		Map<String, String> kvpParams = readFileIntoMap(this.getClass().getResource("wfs110/example2.kvp"));
		DescribeFeatureType dft = parse(kvpParams);
		assertEquals(dft.getHandle(), null);
		assertEquals(dft.getOutputFormat(), null);
		assertEquals(dft.getTypeNames().length, 2);
		assertEquals(dft.getTypeNames()[0], new QName("TreesA_1M"));
		assertEquals(dft.getTypeNames()[1], new QName("BuiltUpA_1M"));
		assertEquals(dft.getVersion(), WFSConstants.VERSION_110);
	}

	@Test
	public void test200Example1() throws IOException {
		Map<String, String> kvpParams = readFileIntoMap(this.getClass().getResource("wfs200/example1.kvp"));
		DescribeFeatureType dft = parse(kvpParams);
		assertEquals(VERSION_200, dft.getVersion());
		assertEquals(null, dft.getHandle());
		assertEquals(null, dft.getOutputFormat());
		assertEquals(1, dft.getTypeNames().length);
		assertEquals(valueOf("{http://www.myserver.com/myns}TreesA_1M"), dft.getTypeNames()[0]);
	}

	@Test
	public void test200Example2() throws IOException {
		Map<String, String> kvpParams = readFileIntoMap(this.getClass().getResource("wfs200/example2.kvp"));
		DescribeFeatureType dft = parse(kvpParams);
		assertEquals(VERSION_200, dft.getVersion());
		assertEquals(null, dft.getHandle());
		assertEquals(null, dft.getOutputFormat());
		assertEquals(2, dft.getTypeNames().length);
		assertEquals(valueOf("{http://www.someserver.com/ns1}TreesA_1M"), dft.getTypeNames()[0]);
		assertEquals(valueOf("{http://someserver.com/ns2}BuiltUpA_1M"), dft.getTypeNames()[1]);
	}

}
