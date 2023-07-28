/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.metadata.iso.persistence.inspectors;

import java.util.List;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.metadata.iso.persistence.AbstractISOTest;
import org.deegree.metadata.iso.persistence.TstConstants;
import org.deegree.metadata.iso.persistence.TstUtils;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.protocol.csw.MetadataStoreException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class InspectorCouplingTest extends AbstractISOTest {

	private static Logger LOG = LoggerFactory.getLogger(InspectorCouplingTest.class);

	@Test
	public void testCouplingConsistencyErrorFALSE()
			throws MetadataStoreException, MetadataInspectorException, ResourceInitException {
		LOG.info(
				"START Test: test if the the coupling of data and service metadata is correct and no exception will be thrown. ");

		initStore(TstConstants.configURL_COUPLING_ACCEPT);
		Assume.assumeNotNull(store);

		List<String> ids = TstUtils.insertMetadata(store, TstConstants.tst_12, TstConstants.tst_12_2,
				TstConstants.tst_13);

		resultSet = store.getRecordById(ids, null);
		int size = 0;
		while (resultSet.next()) {
			size++;
		}

		Assert.assertEquals(3, size);

	}

	@Test
	public void testCouplingConsistencyErrorFALSE_NO_CONSISTENCY()
			throws MetadataStoreException, MetadataInspectorException, ResourceInitException {
		LOG.info(
				"START Test: test if the the coupled service metadata will be inserted without any coupling but no exception will be thrown. ");

		initStore(TstConstants.configURL_COUPLING_ACCEPT);
		Assume.assumeNotNull(store);

		List<String> ids = TstUtils.insertMetadata(store, TstConstants.tst_11, TstConstants.tst_13);

		resultSet = store.getRecordById(ids, null);
		int size = 0;
		while (resultSet.next()) {
			size++;
		}

		Assert.assertEquals(2, size);

	}

	@Test
	public void testCouplingConsistencyErrorTRUE_NO_Exception()
			throws MetadataStoreException, MetadataInspectorException, ResourceInitException {
		LOG.info(
				"START Test: test if the the coupling of data and service metadata is correct and no exception will be thrown. ");

		initStore(TstConstants.configURL_COUPLING_Ex_AWARE);
		Assume.assumeNotNull(store);

		List<String> ids = TstUtils.insertMetadata(store, TstConstants.tst_12, TstConstants.tst_12_2,
				TstConstants.tst_13);

		resultSet = store.getRecordById(ids, null);
		int size = 0;
		while (resultSet.next()) {
			size++;
		}

		Assert.assertEquals(3, size);

	}

	// strictness when testing for coupling was set more relaxed
	// @Test(expected = MetadataInspectorException.class)
	public void testCouplingConsistencyErrorTRUE_WITH_Exception()
			throws MetadataStoreException, MetadataInspectorException, ResourceInitException {
		LOG.info("START Test: test if an exception will be thrown if there is an insert of the service metadata. ");
		initStore(TstConstants.configURL_COUPLING_Ex_AWARE);
		Assume.assumeNotNull(store);
		TstUtils.insertMetadata(store, TstConstants.tst_11, TstConstants.tst_13);

	}

}
