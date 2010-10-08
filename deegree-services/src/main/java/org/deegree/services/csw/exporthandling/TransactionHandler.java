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
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.metadata.DCRecord;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.publication.DeleteTransaction;
import org.deegree.metadata.publication.InsertTransaction;
import org.deegree.metadata.publication.TransactionOperation;
import org.deegree.metadata.publication.UpdateTransaction;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.deegree.services.controller.ows.OWSException;
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

    /**
     * Creates a new {@link TransactionHandler} instance that uses the given service to lookup the {@link MetadataStore}
     * s.
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
     * @throws OWSException
     * @throws IOException
     */
    public void doTransaction( Transaction trans, HttpResponseBuffer response )
                            throws XMLStreamException, OWSException, IOException {

        LOG.debug( "doTransaction: " + trans );

        Version version = trans.getVersion();

        // response.setContentType( trans.getOutputFormat() );
        response.setContentType( "application/xml; charset=UTF-8" );

        // to be sure of a valid response
        String schemaLocation = "";
        if ( version.equals( VERSION_202 ) ) {
            schemaLocation = CSW_202_NS + " " + CSW_202_PUBLICATION_SCHEMA;
        }

        XMLStreamWriter xmlWriter = getXMLResponseWriter( response, schemaLocation );
        try {
            doTransaction( xmlWriter, trans, version );
        } catch ( MetadataStoreException e ) {
            throw new OWSException( e.getMessage(), OWSException.NO_APPLICABLE_CODE );
        }
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
     * @throws OWSException
     * @throws MetadataStoreException
     */
    private void doTransaction( XMLStreamWriter xmlWriter, Transaction transaction, Version version )
                            throws XMLStreamException, OWSException, MetadataStoreException {

        if ( VERSION_202.equals( version ) ) {
            export202( xmlWriter, transaction );
        } else {
            throw new OWSException( "Version '" + version + "' is not supported.", OWSException.INVALID_PARAMETER_VALUE );
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
     * @throws OWSException
     * @throws MetadataStoreException
     */
    private void export202( XMLStreamWriter writer, Transaction transaction )
                            throws OWSException, XMLStreamException, MetadataStoreException {
        Version version = new Version( 2, 0, 2 );

        int insertCount = 0;
        int updateCount = 0;
        int deleteCount = 0;

        writer.setDefaultNamespace( CSW_202_NS );
        writer.setPrefix( CSW_PREFIX, CSW_202_NS );

        writer.writeStartElement( CSW_202_NS, "TransactionResponse" );
        writer.writeAttribute( "version", version.toString() );

        writer.writeStartElement( CSW_202_NS, "TransactionSummary" );
        if ( transaction.getRequestId() != null ) {
            writer.writeAttribute( "requestId", transaction.getRequestId() );
        }

        MetadataStoreTransaction ta = service.getStore().acquireTransaction();

        List<String> insertHandles = new ArrayList<String>();
        List<List<String>> insertIds = new ArrayList<List<String>>();

        String currentHandle = null;

        try {
            for ( TransactionOperation transact : transaction.getOperations() ) {
                currentHandle = transact.getHandle();
                switch ( transact.getType() ) {

                case INSERT:

                    List<String> ids = doInsert( ta, (InsertTransaction) transact );
                    insertHandles.add( transact.getHandle() );
                    insertIds.add( ids );
                    insertCount += ids.size();

                    break;
                case UPDATE:

                    updateCount = doUpdate( ta, (UpdateTransaction) transact );

                    break;
                case DELETE:

                    deleteCount = doDelete( ta, (DeleteTransaction) transact );

                    break;

                }

            }

            ta.commit();
        } catch ( Throwable e ) {
            ta.rollback();
            if ( currentHandle != null ) {
                String msg = "Transaction operation '" + currentHandle + "' failed: " + e.getMessage();
                throw new OWSException( msg, OWSException.NO_APPLICABLE_CODE );
            }
            throw new OWSException( e.getMessage(), OWSException.NO_APPLICABLE_CODE );
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
            for ( int i = 0; i < insertHandles.size(); i++ ) {
                String handle = insertHandles.get( i );
                List<String> ids = insertIds.get( i );
                writer.writeStartElement( CSW_202_NS, "InsertResult" );
                if ( handle != null ) {
                    writer.writeAttribute( "handleRef", handle );
                }
                MetadataResultSet rs = service.getStore().getRecordsById( ids );
                for ( MetadataRecord m : rs.getMembers() ) {
                    DCRecord dc = m.toDublinCore();
                    dc.serialize( writer, ReturnableElement.brief );
                }
                writer.writeEndElement();// InsertResult
            }
        }

        writer.writeEndElement();// TransactionResponse
        writer.writeEndDocument();

    }

    private int doDelete( MetadataStoreTransaction ta, DeleteTransaction delete )
                            throws MetadataStoreException {
        int i = ta.performDelete( delete );
        LOG.info( "Delete done!" );
        return i;
    }

    private int doUpdate( MetadataStoreTransaction ta, UpdateTransaction update )
                            throws MetadataStoreException {
        int i = ta.performUpdate( update );
        LOG.info( "Update done!" );
        return i;
    }

    private List<String> doInsert( MetadataStoreTransaction ta, InsertTransaction insert )
                            throws MetadataStoreException, OWSException {
        // TODO the first element determines the metadataStore
        String uri = insert.getElements().get( 0 ).getNamespace().getNamespaceURI();
        String localName = insert.getElements().get( 0 ).getLocalName();
        String prefix = insert.getElements().get( 0 ).getNamespace().getPrefix();

        List<String> ids = ta.performInsert( insert );
        LOG.debug( "inserted metadata: " + ids );
        LOG.info( "Insert done!" );
        return ids;
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
