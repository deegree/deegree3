package org.deegree.metadata.iso.persistence.sql;

/**
 * Encapsulates write access to the sql backend
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 *
 */
import java.sql.Connection;

import javax.xml.stream.XMLStreamException;

import org.deegree.metadata.iso.ISORecord;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.sqldialect.filter.AbstractWhereBuilder;

public interface TransactionService {

	/**
	 * Generates and inserts the maindatabasetable that is needed for the queryable
	 * properties databasetables to derive from.
	 * <p>
	 * BE AWARE: the "modified" attribute is get from the first position in the list. The
	 * backend has the possibility to add one such attribute. In the xsd-file there are
	 * more possible...
	 * @param conn the SQL connection
	 * @return the primarykey of the inserted dataset which is the foreignkey for the
	 * queryable properties databasetables
	 * @throws MetadataStoreException
	 * @throws XMLStreamException
	 */
	int executeInsert(Connection conn, ISORecord rec) throws MetadataStoreException, XMLStreamException;

	int executeDelete(Connection connection, AbstractWhereBuilder builder) throws MetadataStoreException;

	/**
	 * @param conn the database connection
	 * @param rec the record to update
	 * @param fileIdentifier the fileIdentifer of the record to update, can be
	 * <code>null</code> when the identifer of the record is the one to use for updating
	 * @return the database id of the updated record
	 * @throws MetadataStoreException if updating fails
	 */
	int executeUpdate(Connection conn, ISORecord rec, String fileIdentifier) throws MetadataStoreException;

}