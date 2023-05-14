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
 http://ICoordinateSystem.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.cs.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.transformations.TransformationFactory.DSTransform;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public class WorkspaceTest {

	public static final String STORE_DEEGREE = "deegree";

	public static final String STORE_GML1 = "gml-store1";

	public static final String CRS_FROM_GML1 = "urn:ogc:def:crs:EPSG::31467";

	public static final String CRS_FROM_DEEGREE = "epsg:4002";

	public static final String CRS_UNKNOWN = "unknown";

	private Workspace workspace;

	@Before
	public void beforeAll() throws URISyntaxException {
		URL resource = WorkspaceTest.class.getResource(".");
		workspace = new DefaultWorkspace(new File(resource.toURI()));
		workspace.initAll();
	}

	@After
	public void afterAll() {
		workspace.destroy();
	}

	@Test
	public void testInitWorkspace() {
		Collection<CRSStore> all = CRSManager.getAll();
		assertNotNull(all);
		assertEquals(3, all.size());
		assertNotNull(CRSManager.get(STORE_DEEGREE));
		assertNotNull(CRSManager.get(STORE_GML1));
		Collection<String> crsStoreIds = CRSManager.getCrsStoreIds();
		assertNotNull(crsStoreIds);
		assertEquals(3, crsStoreIds.size());
		assertTrue(crsStoreIds.contains(STORE_DEEGREE));
		assertTrue(crsStoreIds.contains(STORE_GML1));
	}

	@Test
	public void testNTV2Configured() {
		CRSStore crsStore = CRSManager.get(STORE_GML1);
		assertEquals(DSTransform.NTv2, crsStore.getPreferedTransformationType());
	}

	@Test
	public void testLookupKnownCRSFromGML() throws UnknownCRSException {
		ICRS crs = CRSManager.lookup(STORE_GML1, CRS_FROM_GML1);
		assertNotNull(crs);
	}

	@Test(expected = UnknownCRSException.class)
	public void testLookupUnknownCRSFromDeegree() throws UnknownCRSException {
		CRSManager.lookup(STORE_DEEGREE, CRS_FROM_GML1);
	}

	@Test
	public void testLookupKnownCRSFromDeegree() throws UnknownCRSException {
		ICRS crs = CRSManager.lookup(STORE_DEEGREE, CRS_FROM_DEEGREE);
		assertNotNull(crs);
	}

	@Test(expected = UnknownCRSException.class)
	public void testLookupUnknownCRSFromGML() throws UnknownCRSException {
		System.out.println(CRSManager.lookup(STORE_GML1, CRS_FROM_DEEGREE));
	}

	@Test
	public void testLookupCRSFromAll() throws UnknownCRSException {
		ICRS crsDeegree = CRSManager.lookup(CRS_FROM_DEEGREE);
		assertNotNull(crsDeegree);
		ICRS crsGML1 = CRSManager.lookup(CRS_FROM_GML1);
		assertNotNull(crsGML1);
	}

}
