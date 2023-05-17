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

package org.deegree.protocol.wfs.lockfeature.xml;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.junit.Assert.assertFalse;

import org.deegree.protocol.wfs.lockfeature.LockFeature;
import org.junit.Test;

/**
 * Tests for {@link LockFeatureXMLAdapter}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class LockFeatureXMLAdapterTest {

	@Test
	public void testWfs200SpecExample1() throws Exception {
		LockFeatureXMLAdapter parser = new LockFeatureXMLAdapter();
		parser.load(LockFeatureXMLAdapterTest.class.getResource("wfs200/example1.xml"));
		LockFeature request = parser.parse();
		assertEquals(VERSION_200, request.getVersion());

		assertFalse(request.getLockAll());
		assertEquals(1, request.getQueries().size());
		assertNull(request.getExistingLockId());
		assertNull(request.getExpiryInSeconds());
		assertNull(request.getHandle());
	}

}
