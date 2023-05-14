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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.metadata.iso.persistence.memory;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.metadata.iso.persistence.memory.ISOMemoryMetadataStoreTransaction.TransactionCandidate;
import org.deegree.metadata.iso.persistence.memory.ISOMemoryMetadataStoreTransaction.TransactionStatus;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.slf4j.Logger;

/**
 * <code>TransactionHandler</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */

class TransactionCommitter {

	private static final Logger LOG = getLogger(TransactionCommitter.class);

	private StoredISORecords storedRecords;

	private File insertDirectory;

	TransactionCommitter(StoredISORecords storedRecords, File insertDirectory) {
		this.storedRecords = storedRecords;
		this.insertDirectory = insertDirectory;
	}

	void commitInsert(TransactionCandidate transactionCandidate) throws XMLStreamException, IOException {
		File recordFile = null;
		if (insertDirectory != null) {
			recordFile = writeFile(transactionCandidate, null);
		}
		storedRecords.insertRecord(transactionCandidate.record, recordFile);
	}

	void commitUpdate(TransactionCandidate transactionCandidate)
			throws XMLStreamException, MetadataStoreException, IOException {
		File recordFile = null;
		File fileToUpdate = storedRecords.getFile(transactionCandidate.identifier);
		deleteFile(TransactionStatus.UPDATE, fileToUpdate);
		recordFile = writeFile(transactionCandidate, fileToUpdate);
		storedRecords.deleteRecord(transactionCandidate.identifier);
		storedRecords.insertRecord(transactionCandidate.record, recordFile);
	}

	void commitDelete(TransactionCandidate transactionCandidate) throws MetadataStoreException {
		File fileToDelete = storedRecords.getFile(transactionCandidate.identifier);
		deleteFile(TransactionStatus.DELETE, fileToDelete);
		storedRecords.deleteRecord(transactionCandidate.identifier);
	}

	private File writeFile(TransactionCandidate transactionCandidate, File fileToWrite)
			throws XMLStreamException, IOException {
		if (fileToWrite == null) {
			fileToWrite = new File(insertDirectory, transactionCandidate.identifier + ".xml");
		}
		if (!fileToWrite.exists()) {
			boolean created = fileToWrite.createNewFile();
			LOG.debug("File {} was " + (created ? "successful" : "not") + " created.", fileToWrite);
		}
		OutputStream stream = null;
		XMLStreamWriter writer = null;
		try {
			stream = new FileOutputStream(fileToWrite);
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
			transactionCandidate.record.serialize(writer, ReturnableElement.full);
		}
		finally {
			try {
				if (writer != null)
					writer.close();
			}
			catch (XMLStreamException e) {
				// ignore when closing
			}
			try {
				if (stream != null)
					stream.close();
			}
			catch (IOException e) {
				// ignore when closing
			}
		}
		return fileToWrite;
	}

	private void deleteFile(TransactionStatus status, File file) throws MetadataStoreException {
		boolean deleted = file.delete();
		LOG.debug("File {} was " + (deleted ? "successful" : "not") + " deleted", file);
		if (!deleted)
			throw new MetadataStoreException("Commit failed: could not " + status + " record at " + file);
	}

}
