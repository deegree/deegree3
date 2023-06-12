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
package org.deegree.metadata.persistence;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.metadata.MetadataRecord;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.workspace.Resource;

/**
 * Base interface of the {@link MetadataRecord} persistence layer, provides access to
 * stored {@link MetadataRecord} instances.
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public interface MetadataStore<T extends MetadataRecord> extends Resource {

	/**
	 * Performs the given {@link MetadataQuery} and provides access to the
	 * {@link MetadataRecord}s that match it.
	 * <p>
	 * NOTE: A caller <b>must</b> ensure to call {@link MetadataResultSet#close()} after
	 * working with the result set. Otherwise, DB resources may be left open.
	 * </p>
	 * @param query query that selects the records, must not be <code>null</code>
	 * @return result set for accessing the matching records, never <code>null</code>
	 * @throws MetadataStoreException
	 */
	public MetadataResultSet<T> getRecords(MetadataQuery query) throws MetadataStoreException;

	/**
	 * Returns the number of {@link MetadataRecord}s that match the given
	 * {@link MetadataQuery}.
	 * @param query query that selects the records, must not be <code>null</code>
	 * @return number of matching records
	 * @throws MetadataStoreException
	 */
	public int getRecordCount(MetadataQuery query) throws MetadataStoreException;

	/**
	 * Looks up the given {@link MetadataRecord} identifiers and provides access to
	 * matching {@link MetadataRecord}s.
	 * <p>
	 * NOTE: A caller <b>must</b> ensure to call {@link MetadataResultSet#close()} after
	 * working with the result set. Otherwise, DB resources may be left open.
	 * </p>
	 * @param idList list of the requested record identifiers, can be empty, but must not
	 * be <code>null</code>
	 * @param recordTypeNames requested record type names, can be empty or
	 * <code>null</code>
	 * @throws MetadataStoreException
	 */
	public MetadataResultSet<T> getRecordById(List<String> idList, QName[] recordTypeNames)
			throws MetadataStoreException;

	/**
	 * Acquires transactional access to this {@link MetadataStore}.
	 * @return transaction object that allows to perform transaction operations on this
	 * store, never <code>null</code>
	 * @throws MetadataStoreException if the transactional access could not be acquired or
	 * is not available for this implementation
	 */
	public MetadataStoreTransaction acquireTransaction() throws MetadataStoreException;

	/**
	 * Returns the JDBC connection id.
	 * @return the JDBC connection id, never <code>null</code>
	 */
	public String getConnId();

	/**
	 * @return the type of the store
	 */
	public String getType();

}