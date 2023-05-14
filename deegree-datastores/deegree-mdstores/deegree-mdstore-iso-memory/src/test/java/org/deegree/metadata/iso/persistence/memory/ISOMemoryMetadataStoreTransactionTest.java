/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.metadata.iso.persistence.memory;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.transaction.DeleteOperation;
import org.deegree.metadata.persistence.transaction.InsertOperation;
import org.deegree.metadata.persistence.transaction.UpdateOperation;
import org.deegree.protocol.csw.MetadataStoreException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class ISOMemoryMetadataStoreTransactionTest {

	private File directory;

	@Before
	public void setupTestDirectory() throws Exception {
		try {
			directory = File.createTempFile("deegreecswtest", "");
			directory.delete();
			directory.mkdir();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		URL record = ISOMemoryMetadataStore.class.getResource("1.xml");
		File dataDirectory = new File(new File(record.toURI()).getParent());
		File[] listFiles = dataDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".xml") && arg1.length() < 6;
			}
		});
		for (File file : listFiles) {
			FileUtils.copyFile(file, new File(directory, file.getName()));
		}
	}

	@Test
	public void testInsert() throws Exception {
		StoredISORecords storedRecords = new StoredISORecords(Collections.singletonList(directory));
		ISOMemoryMetadataStore metadataStore = Mockito.mock(ISOMemoryMetadataStore.class);
		ISOMemoryMetadataStoreTransaction transaction = new ISOMemoryMetadataStoreTransaction(metadataStore,
				storedRecords, directory);
		int beforeInsert = storedRecords.getNumberOfStoredRecords();
		ISORecord record = GetTestRecordsUtils.getRecord("toInsert.xml");
		InsertOperation insert = new InsertOperation(Collections.singletonList(record), null, null);
		transaction.performInsert(insert);
		transaction.commit();
		assertEquals(beforeInsert + 1, storedRecords.getNumberOfStoredRecords());
		assertSynchronizedStore(storedRecords);
	}

	@Test
	public void testDelete() throws Exception {
		StoredISORecords storedRecords = new StoredISORecords(Collections.singletonList(directory));
		ISOMemoryMetadataStore metadataStore = Mockito.mock(ISOMemoryMetadataStore.class);
		ISOMemoryMetadataStoreTransaction transaction = new ISOMemoryMetadataStoreTransaction(metadataStore,
				storedRecords, directory);
		int beforeInsert = storedRecords.getNumberOfStoredRecords();
		ISORecord record = GetTestRecordsUtils.getRecord("1.xml");
		Filter filter = getIdFilter(record.getIdentifier());
		DeleteOperation delete = new DeleteOperation(null, null, filter);
		transaction.performDelete(delete);
		transaction.commit();
		assertEquals(beforeInsert - 1, storedRecords.getNumberOfStoredRecords());
		assertSynchronizedStore(storedRecords);
	}

	@Test
	public void testUpdate() throws Exception {
		StoredISORecords storedRecords = new StoredISORecords(Collections.singletonList(directory));
		ISOMemoryMetadataStore metadataStore = Mockito.mock(ISOMemoryMetadataStore.class);
		ISOMemoryMetadataStoreTransaction transaction = new ISOMemoryMetadataStoreTransaction(metadataStore,
				storedRecords, directory);

		ISORecord updateRecord = GetTestRecordsUtils.getRecord("toUpdate.xml");
		String expectedKeyword = getKeyword(updateRecord);
		String identifierOfRecordToUpdate = updateRecord.getIdentifier();

		File fileBeforeUpdate = storedRecords.getFile(identifierOfRecordToUpdate);
		assertTrue(fileBeforeUpdate.exists());

		int beforeInsert = storedRecords.getNumberOfStoredRecords();

		update(transaction, updateRecord);

		assertEquals(beforeInsert, storedRecords.getNumberOfStoredRecords());
		assertSynchronizedStore(storedRecords);

		List<ISORecord> records = storedRecords.getRecords(getIdFilter(updateRecord.getIdentifier()));
		assertEquals(1, records.size());
		assertEquals(expectedKeyword, getKeyword(records.get(0)));

		File fileAfterUpdate = storedRecords.getFile(identifierOfRecordToUpdate);
		assertTrue(fileAfterUpdate.exists());
		assertEquals(fileBeforeUpdate, fileAfterUpdate);
	}

	private String getKeyword(ISORecord updateRecord) {
		return updateRecord.getParsedElement().getQueryableProperties().getKeywords().get(0).getKeywords().get(0);
	}

	private void update(ISOMemoryMetadataStoreTransaction transaction, ISORecord updateRecord)
			throws MetadataStoreException, MetadataInspectorException {
		Filter filter = getIdFilter(updateRecord.getIdentifier());
		UpdateOperation update = new UpdateOperation(null, updateRecord, null, filter, null);
		transaction.performUpdate(update);
		transaction.commit();
	}

	private IdFilter getIdFilter(String id) {
		return new IdFilter(id);
	}

	private void assertSynchronizedStore(StoredISORecords storedRecords) {
		assertEquals(storedRecords.getNumberOfStoredRecords(), getNumberOfRecordsInStoreDirectory());
	}

	private int getNumberOfRecordsInStoreDirectory() {
		return directory.listFiles().length;
	}

}
