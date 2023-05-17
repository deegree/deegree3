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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.MetadataRecordFactory;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates the {@link ISORecord}s stored by a
 * {@link org.deegree.metadata.persistence.MetadataStore} instance.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class StoredISORecords {

	private static final Logger LOG = LoggerFactory.getLogger(StoredISORecords.class);

	private final LinkedHashMap<String, File> identifierToFile = new LinkedHashMap<String, File>();

	private final LinkedHashMap<String, ISORecord> identifierToRecord = new LinkedHashMap<String, ISORecord>();

	/**
	 * Creates an empty store.
	 */
	StoredISORecords() {
	}

	/**
	 * Creates a store and reads all records from the passed directories
	 * @param recordDirectories directories to read records from
	 * @throws IOException
	 */
	StoredISORecords(List<File> recordDirectories) throws IOException {
		addRecords(recordDirectories);
	}

	private void addRecords(List<File> recordDirectories) throws IOException {
		for (File dir : recordDirectories) {
			loadRecords(dir);
		}
	}

	private void loadRecords(File recordDirectory) throws IOException {
		File[] records = recordDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		});
		for (File record : records) {
			loadRecord(record);
		}
	}

	/**
	 * Add a new record to the list of stored records
	 * @param record record to add, never <code>null</code>
	 * @param recordFile file containing the record, can be <code>null</code>
	 */
	public String insertRecord(ISORecord record, File recordFile) {
		return addOrUpdateRecord(record, recordFile);
	}

	/**
	 * Removes the record with the given identifier from the store
	 * @param identifier never <code>null</code>
	 */
	public boolean deleteRecord(String identifier) {
		if (identifierToRecord.containsKey(identifier)) {
			identifierToRecord.remove(identifier);
			identifierToFile.remove(identifier);
			return true;
		}
		return false;
	}

	private void loadRecord(File recordFile) throws IOException {
		FileInputStream recordStream = new FileInputStream(recordFile);
		OMElement document = new XMLAdapter(recordStream).getRootElement();
		document.build();
		MetadataRecord record = MetadataRecordFactory.create(document);
		recordStream.close();
		if (!(record instanceof ISORecord)) {
			LOG.debug("Ignore record {}: is not a ISO19139 record.", recordFile.getName());
			return;
		}
		addOrUpdateRecord((ISORecord) record, recordFile);
	}

	private String addOrUpdateRecord(ISORecord record, File file) {
		try {
			String identifier = record.getIdentifier();
			LOG.debug("Add record number {} with fileIdentifier {}", getNumberOfStoredRecords() + 1, identifier);
			if (identifier == null) {
				LOG.debug("Ignore record {}, fileIdentifier is null.", file != null ? file.getName() : "");
				return null;
			}
			if (identifierToRecord.containsKey(identifier)) {
				LOG.debug("Overwrite record with fileIdentifier {}.", identifier);
			}
			identifierToRecord.put(identifier, record);
			identifierToFile.put(identifier, file);
			return identifier;
		}
		catch (Exception e) {
			LOG.debug("Ignore record {}, could not be parsed: {}.", file != null ? file.getName() : "", e.getMessage());
		}
		return null;
	}

	/**
	 * Requests all records with the passed ids.
	 * @param idList may be empty but never null never <code>null</code>
	 * @return the records with the passed ids,may be empty but never <code>null</code>
	 */
	public MetadataResultSet<ISORecord> getRecordById(List<String> idList) {
		if (idList == null) {
			throw new IllegalArgumentException("List with ids must not be null!");
		}
		List<ISORecord> result = new ArrayList<ISORecord>();
		for (String id : idList) {
			if (identifierToRecord.containsKey(id)) {
				result.add(identifierToRecord.get(id));
			}
		}
		return new ListMetadataResultSet(result);
	}

	/**
	 * Requests all records matching the query.
	 * @param query never <code>null</code>
	 * @return all records matching the query, may be empty but never <code>null</code>
	 * @throws FilterEvaluationException
	 */
	public MetadataResultSet<ISORecord> getRecords(MetadataQuery query) throws FilterEvaluationException {
		if (query == null) {
			throw new IllegalArgumentException("MetadataQuery must not be null!");
		}
		List<ISORecord> result = applyFilter(query.getFilter(), query.getStartPosition(), query.getMaxRecords());
		return new ListMetadataResultSet(result);
	}

	private List<ISORecord> applyFilter(Filter filter, int startPosition, int maxRecords)
			throws FilterEvaluationException {
		if (filter == null) {
			return applyNullFilter(startPosition, maxRecords);
		}
		List<ISORecord> result = new ArrayList<ISORecord>(maxRecords);
		int matched = 1;
		for (ISORecord record : identifierToRecord.values()) {
			if (record.eval(filter)) {
				if (matched >= startPosition) {
					result.add(record);
				}
				matched++;
			}
			if (result.size() >= maxRecords) {
				break;
			}
		}
		return result;
	}

	private List<ISORecord> applyNullFilter(int startPosition, int maxRecords) {
		List<ISORecord> result = new ArrayList<ISORecord>(maxRecords);
		int index = 1;
		for (String fileIdentifier : identifierToRecord.keySet()) {
			if (index >= startPosition) {
				result.add(identifierToRecord.get(fileIdentifier));
			}
			index++;
			if (result.size() >= maxRecords) {
				break;
			}
		}
		return result;
	}

	/**
	 * Requests the number of records kept in memory
	 * @return the number of records kept in memory
	 */
	public int getNumberOfStoredRecords() {
		return identifierToRecord.size();
	}

	/**
	 * Requests all records matching the filter
	 * @param filter if <code>null</code> all records are returned
	 * @return
	 * @throws FilterEvaluationException
	 */
	public List<ISORecord> getRecords(Filter filter) throws FilterEvaluationException {
		List<ISORecord> result = new ArrayList<ISORecord>();
		if (filter == null) {
			result.addAll(identifierToRecord.values());
		}
		else {
			for (ISORecord record : identifierToRecord.values()) {
				if (record.eval(filter)) {
					result.add(record);
				}
			}
		}
		return result;
	}

	/**
	 * @param record never <code>null</code>
	 * @return return true if a record with the same identifier is stored, false otherwise
	 */
	public boolean contains(ISORecord record) {
		return identifierToRecord.containsKey(record.getIdentifier());
	}

	/**
	 * @param identifier never <code>null</code>
	 * @return the file assigned to the identifier, <code>null</code> if no file is
	 * assigned
	 */
	File getFile(String identifier) {
		return identifierToFile.get(identifier);
	}

}
