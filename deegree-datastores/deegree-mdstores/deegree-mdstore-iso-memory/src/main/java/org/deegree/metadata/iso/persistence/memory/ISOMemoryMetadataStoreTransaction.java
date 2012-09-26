//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-metadata/src/main/java/org/deegree/metadata/iso/persistence/ISOMetadataStoreProvider.java $
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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.filter.FilterEvaluationException;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.persistence.transaction.DeleteOperation;
import org.deegree.metadata.persistence.transaction.InsertOperation;
import org.deegree.metadata.persistence.transaction.UpdateOperation;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.deegree.protocol.csw.MetadataStoreException;
import org.slf4j.Logger;

/**
 * Implementation of the {@link MetadataStoreTransaction} to handle transactions.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: 30800 $, $Date: 2011-05-12 16:49:44 +0200 (Do, 12. Mai 2011) $
 */
public class ISOMemoryMetadataStoreTransaction implements MetadataStoreTransaction {

    private static final Logger LOG = getLogger( ISOMemoryMetadataStoreTransaction.class );

    private enum TransactionStatus {
        UPDATE, DELETE, INSERT
    }

    private ISOMemoryMetadataStore metadataStore;

    private StoredISORecords storedRecords;

    private URL transactionalDirectory;

    private List<TransactionCandidate> transactionCandidates = new ArrayList<TransactionCandidate>();

    public ISOMemoryMetadataStoreTransaction( ISOMemoryMetadataStore metadataStore, StoredISORecords storedRecords,
                                              URL transactionalDirectory ) {
        this.metadataStore = metadataStore;
        this.storedRecords = storedRecords;
        this.transactionalDirectory = transactionalDirectory;
    }

    @Override
    public void commit()
                            throws MetadataStoreException {
        try {
            for ( TransactionCandidate transactionCandidate : transactionCandidates ) {
                switch ( transactionCandidate.status ) {
                case INSERT:
                    commitInsert( transactionCandidate );
                    break;
                case UPDATE:
                    commitUpdate( transactionCandidate );
                    break;
                case DELETE:
                    commitDelete( transactionCandidate );
                    break;
                }
            }
        } catch ( XMLStreamException e ) {
            LOG.error( "Commit failed: ", e );
            throw new MetadataStoreException( e );
        } catch ( FileNotFoundException e ) {
            LOG.error( "Commit failed: ", e );
            throw new MetadataStoreException( e );
        } catch ( URISyntaxException e ) {
            LOG.error( "Commit failed: ", e );
            throw new MetadataStoreException( e );
        }
        metadataStore.releaseTransaction();
    }

    private void commitInsert( TransactionCandidate transactionCandidate )
                            throws FileNotFoundException, URISyntaxException, XMLStreamException,
                            FactoryConfigurationError {
        File recordFile = null;
        if ( transactionalDirectory != null ) {
            recordFile = writeFile( transactionCandidate, null );
        }
        storedRecords.insertRecord( transactionCandidate.record, recordFile );
    }

    private void commitUpdate( TransactionCandidate transactionCandidate )
                            throws FileNotFoundException, URISyntaxException, XMLStreamException,
                            FactoryConfigurationError, MetadataStoreException {
        File recordFile = null;
        if ( transactionalDirectory != null ) {
            File fileToUpdate = storedRecords.getFile( transactionCandidate.identifier );
            deleteFile( TransactionStatus.UPDATE, fileToUpdate );
            recordFile = writeFile( transactionCandidate, fileToUpdate );
        }
        storedRecords.deleteRecord( transactionCandidate.identifier );
        storedRecords.insertRecord( transactionCandidate.record, recordFile );
    }

    private void commitDelete( TransactionCandidate transactionCandidate )
                            throws MetadataStoreException {
        File fileToDelete = storedRecords.getFile( transactionCandidate.identifier );
        deleteFile( TransactionStatus.DELETE, fileToDelete );
        storedRecords.deleteRecord( transactionCandidate.identifier );
    }

    private File writeFile( TransactionCandidate transactionCandidate, File fileToWrite )
                            throws FileNotFoundException, URISyntaxException, XMLStreamException,
                            FactoryConfigurationError {
        if ( fileToWrite == null ) {
            fileToWrite = new File( new File( transactionalDirectory.toURI() ), transactionCandidate.identifier
                                                                                + ".xml" );
        }
        OutputStream stream = new FileOutputStream( fileToWrite );
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter( stream );
        transactionCandidate.record.serialize( writer, ReturnableElement.full );
        return fileToWrite;
    }

    private void deleteFile( TransactionStatus status, File fileToUpdate )
                            throws MetadataStoreException {
        boolean deleted = fileToUpdate.delete();
        LOG.debug( "File was " + ( deleted ? "successful" : "not" ) + " deleted" );
        if ( !deleted )
            throw new MetadataStoreException( "Commit failed: could not " + status + " record at " + fileToUpdate );
    }

    @Override
    public void rollback()
                            throws MetadataStoreException {
        transactionCandidates.clear();
        metadataStore.releaseTransaction();
    }

    @Override
    public List<String> performInsert( InsertOperation insert )
                            throws MetadataStoreException, MetadataInspectorException {
        List<? extends MetadataRecord> recordsToInsert = insert.getRecords();
        List<String> insertedIds = new ArrayList<String>( recordsToInsert.size() );
        for ( MetadataRecord record : recordsToInsert ) {
            ISORecord isoRecord = (ISORecord) record;
            if ( storedRecords.contains( isoRecord ) ) {
                throw new MetadataStoreException( "Insert failed: record with identifier " + record.getIdentifier()
                                                  + " exists." );
            }
            transactionCandidates.add( new TransactionCandidate( TransactionStatus.INSERT, isoRecord.getIdentifier(),
                                                                 isoRecord ) );
            insertedIds.add( isoRecord.getIdentifier() );
        }
        return insertedIds;
    }

    @Override
    public int performDelete( DeleteOperation delete )
                            throws MetadataStoreException {
        List<ISORecord> recordsToDelete;
        try {
            recordsToDelete = storedRecords.getRecords( delete.getConstraint() );
            for ( ISORecord record : recordsToDelete ) {
                transactionCandidates.add( new TransactionCandidate( TransactionStatus.DELETE, record.getIdentifier(),
                                                                     record ) );
            }
        } catch ( FilterEvaluationException e ) {
            LOG.error( "Could not evaluate filter!", e );
            throw new MetadataStoreException( e );
        }
        return recordsToDelete.size();
    }

    @Override
    public int performUpdate( UpdateOperation update )
                            throws MetadataStoreException, MetadataInspectorException {
        try {
            if ( update.getRecordProperty() != null || !update.getRecordProperty().isEmpty() ) {
                throw new MetadataStoreException( "Update failed: update of properties is not implemented yet!" );
            }
            List<ISORecord> records = storedRecords.getRecords( update.getConstraint() );
            if ( records.size() > 0 ) {
                throw new MetadataStoreException(
                                                  "Update failed: update with a filter matching more than one record is not implemented yet!" );
            }
            if ( records.size() == 0 ) {
                return 0;
            }
            ISORecord record = records.get( 0 );
            transactionCandidates.add( new TransactionCandidate( TransactionStatus.UPDATE, record.getIdentifier(),
                                                                 record ) );
            return 1;
        } catch ( FilterEvaluationException e ) {
            LOG.error( "Could not evaluate filter!", e );
            throw new MetadataStoreException( e );
        }
    }

    class TransactionCandidate {

        TransactionStatus status;

        String identifier;

        ISORecord record;

        public TransactionCandidate( TransactionStatus status, String identifier, ISORecord record ) {
            this.status = status;
            this.identifier = identifier;
            this.record = record;
        }

    }

}
