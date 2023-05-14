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

import org.deegree.metadata.persistence.transaction.DeleteOperation;
import org.deegree.metadata.persistence.transaction.InsertOperation;
import org.deegree.metadata.persistence.transaction.UpdateOperation;
import org.deegree.protocol.csw.MetadataStoreException;

/**
 * Provides transactional access to a {@link MetadataStore}.
 * <p>
 * NOTE: a transaction must always be ended by calling either {@link #commit()} or
 * {@link #rollback()}
 * </p>
 *
 * @see MetadataStore#acquireTransaction()
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public interface MetadataStoreTransaction {

	/**
	 * Makes the changes persistent that have been performed in this transaction and
	 * releases the transaction instance so other clients may acquire a transaction on the
	 * {@link MetadataStore}.
	 * @throws MetadataStoreException if the committing fails
	 */
	public void commit() throws MetadataStoreException;

	/**
	 * Aborts the changes that have been performed in this transaction and releases the
	 * transaction instance so other clients may acquire a transaction on the
	 * {@link MetadataStore}.
	 * @throws MetadataStoreException if the rollback fails
	 */
	public void rollback() throws MetadataStoreException;

	/**
	 * Performs the given {@link InsertOperation}.
	 *
	 * TODO for scalabilitiy reasons and simplicitly, consider changing this to
	 * #performInsert (MetadataRecord)
	 * @param insert operation to be performed, must not be <code>null</code>
	 * @return identifier of the inserted records, can be empty, but never
	 * <code>null</code>
	 * @throws MetadataStoreException if the insertion failed
	 */
	public List<String> performInsert(InsertOperation insert) throws MetadataStoreException, MetadataInspectorException;

	/**
	 * Performs the given {@link DeleteOperation}.
	 * @param delete operation to be performed, must not be <code>null</code>
	 * @return number of deleted records
	 * @throws MetadataStoreException if the deletion failed
	 */
	public int performDelete(DeleteOperation delete) throws MetadataStoreException;

	/**
	 * Performs the given {@link UpdateOperation}.
	 * @param update operation to be performed, must not be <code>null</code>
	 * @return number of updated records
	 * @throws MetadataStoreException if the update failed
	 */
	public int performUpdate(UpdateOperation update) throws MetadataStoreException, MetadataInspectorException;

}