//$HeadURL$
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
package org.deegree.record.persistence;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.protocol.csw.CSWConstants.SetOfReturnableElements;
import org.deegree.record.publication.TransactionOperation;

/**
 * Base interface of the {@link RecordStore} persistence layer, provides access to stored {@link RecordStore} instances
 * and their schemas.
 * <p>
 * NOTE: One {@link RecordStore} instance corresponds to one metadata format (e.g. DublinCore, MD_Metadata (ISO TC211),
 * SV_Service (ISO TC211)).
 * </p>
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public interface RecordStore {

    /**
     * Called by the container to indicate that this {@link RecordStore} instance is being placed into service.
     * 
     * @throws RecordStoreException
     *             if the initialization fails
     */
    public void init()
                            throws RecordStoreException;

    /**
     * Called by the container to indicate that this {@link RecordStore} instance is being taken out of service.
     */
    public void destroy();

    /**
     * Exports the XML schema for the associated metadata format.
     * 
     * @param writer
     *            writer to export to, must not be <code>null</code>
     * @param typeName
     *            specifies which record profile should be returned in the response.
     */
    public void describeRecord( XMLStreamWriter writer, QName typeName );

    /**
     * 
     * Exports the XML for the requested records.
     * 
     * @param writer
     *            writer to export to, must not be <code>null</code>
     * @param typeName
     *            of a specific requested record profile
     * @param outputSchema
     *            that should present in the response. If there is a DC recordStore requested and the outputSchema is a
     *            ISO schema then there should be presented the ISO representation of the record.
     * @param recordStoreOptions
     *            {@link RecordStoreOptions}
     * @throws SQLException
     * @throws XMLStreamException
     * @throws IOException
     */
    public void getRecords( XMLStreamWriter writer, QName typeName, URI outputSchema,
                            RecordStoreOptions recordStoreOptions )
                            throws SQLException, XMLStreamException, IOException;

    /**
     * Exports the records by the requested identifier.
     * 
     * @param writer
     *            writer to export to, must not be <code>null</code>
     * @param idList
     *            list of the requested identifiers
     * @param outputSchema
     *            that should be presented in the response
     * @param elementSetName
     *            {@link SetOfReturnableElements}
     * @throws SQLException
     */
    public void getRecordById( XMLStreamWriter writer, List<String> idList, URI outputSchema,
                               SetOfReturnableElements elementSetName )
                            throws SQLException;

    /**
     * Exports the XML fragment to the recordstore-backend.
     * <p>
     * INSERT-action: inserts one or more records to the backend. <br>
     * UPDATE-action: updates one or more complete records OR individual properties. <br>
     * DELETE-action: deletes one or more records by one filter expression.
     * <p>
     * 
     * 
     * @param writer
     *            writer to export to, must not be <code>null</code>
     * @param operations
     *            that are hold by this container
     * @return the number of successful transactions
     * @throws SQLException
     * @throws XMLStreamException
     */
    public List<Integer> transaction( XMLStreamWriter writer, TransactionOperation operations )
                            throws SQLException, XMLStreamException;

    /**
     * Gets the records in dublin core representation for the insert action of the transaction operation. If there is an
     * INSERT statement in the transaction operation there has to be a brief representation (because of the validity) of
     * this inserted record presented in the response.
     * 
     * @param writer
     *            to be updated with a brief representation of the inserted records
     * @param transactionIds
     *            that are affected by the transaction
     * @throws SQLException
     * @throws IOException
     */
    public void getRecordsForTransactionInsertStatement( XMLStreamWriter writer, List<Integer> transactionIds )
                            throws SQLException, IOException;

    /**
     * Returns the typeNames that are known in the backend. <br>
     * i.e. the ISORecordStore holds two profiles, the DUBLIN CORE and the ISO profile.
     * 
     * @return a map from a QName to an int value
     * 
     */
    public Map<QName, Integer> getTypeNames();

}
