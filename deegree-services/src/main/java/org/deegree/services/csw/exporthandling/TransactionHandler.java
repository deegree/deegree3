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
package org.deegree.services.csw.exporthandling;

import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_PUBLICATION_SCHEMA;
import static org.deegree.protocol.csw.CSWConstants.CSW_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.record.persistence.RecordStore;
import org.deegree.record.publication.DeleteTransaction;
import org.deegree.record.publication.InsertTransaction;
import org.deegree.record.publication.TransactionOperation;
import org.deegree.record.publication.UpdateTransaction;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.csw.CSWService;
import org.deegree.services.csw.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the export functionality for a {@link Transaction} request
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class TransactionHandler {

    private static final Logger LOG = LoggerFactory.getLogger( TransactionHandler.class );

    private CSWService service;

    private static Map<QName, RecordStore> requestedTypeNames;

    /**
     * Creates a new {@link TransactionHandler} instance that uses the given service to lookup the {@link RecordStore}s.
     * 
     * @param service
     */
    public TransactionHandler( CSWService service ) {
        this.service = service;

    }

    /**
     * 
     * Preprocessing for the export of a {@link Transaction} request
     * 
     * @param trans
     * @param response
     * @throws XMLStreamException
     * @throws IOException
     * @throws SQLException
     */
    public void doTransaction( Transaction trans, HttpResponseBuffer response, boolean isSoap )
                            throws XMLStreamException, IOException, SQLException {

        LOG.debug( "doTransaction: " + trans );

        Version version = trans.getVersion();

        response.setContentType( trans.getOutputFormat() );

        // to be sure of a valid response
        String schemaLocation = "";
        if ( version.equals( VERSION_202 ) ) {
            schemaLocation = CSW_202_NS + " " + CSW_202_PUBLICATION_SCHEMA;
        }

        XMLStreamWriter xmlWriter = getXMLResponseWriter( response, schemaLocation );
        export( xmlWriter, trans, version, isSoap );
        xmlWriter.flush();

    }

    /**
     * Exports the correct recognized request and determines to which version it should delegate the request.
     * 
     * @param xmlWriter
     * @param transaction
     *            request from the client
     * @param version
     * @throws XMLStreamException
     * @throws SQLException
     */
    private void export( XMLStreamWriter xmlWriter, Transaction transaction, Version version, boolean isSoap )
                            throws XMLStreamException, SQLException {

        if ( VERSION_202.equals( version ) ) {
            export202( xmlWriter, transaction, isSoap );
        } else {
            throw new IllegalArgumentException( "Version '" + version + "' is not supported." );
        }

    }

    /**
     * Exporthandling for the version 2.0.2. <br>
     * Here it is determined which of the transaction actions are handled. First there is the handling with the
     * database. After that in the case of the INSERT action the output should generate a brief representation of the
     * inserted records.
     * 
     * @param writer
     * @param transaction
     *            request
     * @throws XMLStreamException
     * @throws SQLException
     */
    private void export202( XMLStreamWriter writer, Transaction transaction, boolean isSoap )
                            throws XMLStreamException, SQLException {
        Version version = new Version( 2, 0, 2 );

        requestedTypeNames = new HashMap<QName, RecordStore>();
        InsertTransaction insert = null;
        UpdateTransaction update = null;
        DeleteTransaction delete = null;

        int insertCount = 0;
        int updateCount = 0;
        int deleteCount = 0;
        List<Integer> transactionIds = new ArrayList<Integer>();

        writer.setDefaultNamespace( CSW_202_NS );
        writer.setPrefix( CSW_PREFIX, CSW_202_NS );

        writer.writeStartElement( CSW_202_NS, "TransactionResponse" );
        writer.writeAttribute( "version", version.toString() );

        writer.writeStartElement( CSW_202_NS, "TransactionSummary" );
        if ( transaction.getRequestId() != null ) {
            writer.writeAttribute( "requestId", transaction.getRequestId() );
        }
        Iterator<Integer> it;

        for ( TransactionOperation transact : transaction.getOperations() ) {
            switch ( transact.getType() ) {

            case INSERT:

                insert = (InsertTransaction) transact;

                for ( OMElement element : insert.getElement() ) {

                    requestedTypeNames.put(
                                            new QName( element.getNamespace().getNamespaceURI(),
                                                       element.getLocalName(), element.getNamespace().getPrefix() ),
                                            service.getRecordStore( new QName(
                                                                               element.getNamespace().getNamespaceURI(),
                                                                               element.getLocalName(),
                                                                               element.getNamespace().getPrefix() ) ) );

                }

                for ( RecordStore rec : requestedTypeNames.values() ) {

                    transactionIds.addAll( rec.transaction( writer, insert ) );
                }

                it = transactionIds.listIterator();
                while ( it.hasNext() ) {
                    it.next();
                    insertCount++;
                }

                break;
            case UPDATE:

                update = (UpdateTransaction) transact;

                /*
                 * Either it is a hole recordStore to be updated or just some recordProperties.
                 */
                if ( update.getRecordProperty() == null ) {
                    requestedTypeNames.put(
                                            new QName( update.getElement().getNamespace().getNamespaceURI(),
                                                       update.getElement().getLocalName(),
                                                       update.getElement().getNamespace().getPrefix() ),
                                            service.getRecordStore( new QName(
                                                                               update.getElement().getNamespace().getNamespaceURI(),
                                                                               update.getElement().getLocalName(),
                                                                               update.getElement().getNamespace().getPrefix() ) ) );

                    for ( RecordStore rec : requestedTypeNames.values() ) {
                        transactionIds.addAll( rec.transaction( writer, update ) );

                    }
                } else {

                    /*
                     * here all the registered recordStores are queried
                     */
                    for ( RecordStore rec : service.getRecordStore() ) {

                        transactionIds.addAll( rec.transaction( writer, update ) );

                    }

                }

                it = transactionIds.listIterator();
                while ( it.hasNext() ) {
                    it.next();
                    updateCount++;
                }

                break;
            case DELETE:

                delete = (DeleteTransaction) transact;
                /*
                 * here all the registered recordStores are queried
                 */
                for ( RecordStore rec : service.getRecordStore() ) {
                    transactionIds.addAll( rec.transaction( writer, delete ) );

                }
                it = transactionIds.listIterator();
                while ( it.hasNext() ) {
                    it.next();
                    deleteCount++;
                }

                break;

            }
        }

        writer.writeStartElement( CSW_202_NS, "totalInserted" );
        writer.writeCharacters( Integer.toString( insertCount ) );
        writer.writeEndElement();// totalInserted

        writer.writeStartElement( CSW_202_NS, "totalUpdated" );
        writer.writeCharacters( Integer.toString( updateCount ) );
        writer.writeEndElement();// totalUpdated

        writer.writeStartElement( CSW_202_NS, "totalDeleted" );
        writer.writeCharacters( Integer.toString( deleteCount ) );
        writer.writeEndElement();// totalDeleted

        writer.writeEndElement();// TransactionSummary

        if ( insertCount > 0 ) {
            writer.writeStartElement( CSW_202_NS, "InsertResult" );
            // TODO handle?? where is it?? writer.writeAttribute( "handleRef", trans. );

            for ( RecordStore rec : requestedTypeNames.values() ) {
                try {
                    rec.getRecordsForTransactionInsertStatement( writer, transactionIds );
                } catch ( IOException e ) {
                    e.printStackTrace();
                }

            }

            writer.writeEndElement();// InsertResult
        }

        writer.writeEndElement();// TransactionResponse
        writer.writeEndDocument();

    }

    /**
     * Returns an <code>XMLStreamWriter</code> for writing an XML response document.
     * 
     * @param writer
     *            writer to write the XML to, must not be null
     * @param schemaLocation
     *            allows to specify a value for the 'xsi:schemaLocation' attribute in the root element, must not be null
     * @return {@link XMLStreamWriter}
     * @throws XMLStreamException
     * @throws IOException
     */
    static XMLStreamWriter getXMLResponseWriter( HttpResponseBuffer writer, String schemaLocation )
                            throws XMLStreamException, IOException {

        if ( schemaLocation.equals( "" ) ) {
            return writer.getXMLWriter();
        }
        return new XMLStreamWriterWrapper( writer.getXMLWriter(), schemaLocation );
    }
}
