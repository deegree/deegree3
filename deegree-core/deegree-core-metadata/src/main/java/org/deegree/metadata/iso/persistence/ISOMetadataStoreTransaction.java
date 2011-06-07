package org.deegree.metadata.iso.persistence;

import static org.deegree.commons.jdbc.ConnectionManager.Type.PostgreSQL;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.AbstractWhereBuilder;
import org.deegree.filter.sql.UnmappableException;
import org.deegree.filter.sql.mssql.MSSQLWhereBuilder;
import org.deegree.filter.sql.postgis.PostGISWhereBuilder;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.persistence.inspectors.RecordInspector;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.AnyText;
import org.deegree.metadata.persistence.transaction.DeleteOperation;
import org.deegree.metadata.persistence.transaction.InsertOperation;
import org.deegree.metadata.persistence.transaction.MetadataProperty;
import org.deegree.metadata.persistence.transaction.UpdateOperation;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.MetadataStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MetadataStoreTransaction} implementation for the {@link ISOMetadataStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ISOMetadataStoreTransaction implements MetadataStoreTransaction {

    private static final Logger LOG = LoggerFactory.getLogger( ISOMetadataStoreTransaction.class );

    private final Connection conn;

    private final List<RecordInspector<ISORecord>> inspectors;

    private final AnyText anyTextConfig;

    private final boolean useLegacyPredicates;

    private final Type connectionType;

    ISOMetadataStoreTransaction( Connection conn, List<RecordInspector<ISORecord>> inspectors, AnyText anyText,
                                 boolean useLegacyPredicates, Type connectionType ) throws SQLException {
        this.conn = conn;
        this.anyTextConfig = anyText;
        this.inspectors = inspectors;
        this.useLegacyPredicates = useLegacyPredicates;
        this.connectionType = connectionType;
    }

    @Override
    public void commit()
                            throws MetadataStoreException {
        LOG.debug( Messages.getMessage( "INFO_TA_COMMIT" ) );
        try {
            conn.commit();
        } catch ( SQLException e ) {
            String msg = Messages.getMessage( "ERROR_TA_COMMIT", e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        } finally {
            JDBCUtils.close( conn );
        }
    }

    private AbstractWhereBuilder getWhereBuilder( OperatorFilter filter )
                            throws FilterEvaluationException, UnmappableException {
        ISOPropertyNameMapper mapping = new ISOPropertyNameMapper( connectionType, useLegacyPredicates );
        if ( connectionType == PostgreSQL ) {
            return new PostGISWhereBuilder( mapping, filter, null, false, useLegacyPredicates );
        }
        if ( connectionType == Type.MSSQL ) {
            return new MSSQLWhereBuilder( mapping, filter, null, false );
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public int performDelete( DeleteOperation delete )
                            throws MetadataStoreException {

        try {
            AbstractWhereBuilder builder = getWhereBuilder( (OperatorFilter) delete.getConstraint() );

            TransactionHelper transactionHelper = new TransactionHelper( connectionType, anyTextConfig );
            return transactionHelper.executeDelete( conn, builder );

        } catch ( Exception e ) {
            throw new MetadataStoreException( e.getMessage() );
        }
    }

    @Override
    public List<String> performInsert( InsertOperation insert )
                            throws MetadataStoreException, MetadataInspectorException {

        List<String> identifierList = new ArrayList<String>();
        for ( MetadataRecord record : insert.getRecords() ) {
            try {
                for ( RecordInspector<ISORecord> r : inspectors ) {
                    record = r.inspect( (ISORecord) record, conn, connectionType );
                }
                if ( record != null ) {
                    ISORecord rec = new ISORecord( record.getAsOMElement() );
                    TransactionHelper transactionHelper = new TransactionHelper( connectionType, anyTextConfig );
                    transactionHelper.executeInsert( conn, rec );
                    identifierList.add( rec.getIdentifier() );
                }
            } catch ( XMLStreamException e ) {
                throw new MetadataStoreException( "Error on insert: " + e.getMessage(), e );
            }
        }
        return identifierList;
    }

    @Override
    public int performUpdate( UpdateOperation update )
                            throws MetadataStoreException, MetadataInspectorException {
        TransactionHelper generateQP = new TransactionHelper( connectionType, anyTextConfig );
        int result = 0;

        if ( update.getRecord() != null && update.getConstraint() == null ) {
            LOG.warn( "Update with complete metadatset and without constraint is deprecated. Updating is forwarded, the fileIdentifer is used to find the record to update." );
            ISORecord record = (ISORecord) update.getRecord();
            for ( RecordInspector<ISORecord> r : inspectors ) {
                record = r.inspect( record, conn, connectionType );
            }
            ISORecord rec = new ISORecord( record.getAsOMElement() );
            generateQP.executeUpdate( conn, rec, null );
            return 1;
        }

        QueryHelper qh = new QueryHelper( connectionType );
        try {
            MetadataQuery query = new MetadataQuery( null, null, (OperatorFilter) update.getConstraint(), null, 1,
                                                     Integer.MIN_VALUE );
            // get all metadatasets to update
            ISOMetadataResultSet isoRs = qh.execute( query, conn );
            while ( isoRs.next() ) {
                ISORecord rec = isoRs.getRecord();
                LOG.debug( "record to update" + rec );
                boolean updated = false;

                if ( update.getRecord() != null ) {
                    ISORecord record = (ISORecord) update.getRecord();
                    for ( RecordInspector<ISORecord> r : inspectors ) {
                        record = r.inspect( record, conn, connectionType );
                    }
                    rec = new ISORecord( record.getAsOMElement() );
                    updated = true;
                } else if ( update.getConstraint() != null
                            && ( update.getRecordProperty() != null && update.getRecordProperty().size() > 0 ) ) {
                    List<MetadataProperty> recordProperty = update.getRecordProperty();
                    for ( MetadataProperty metadataProperty : recordProperty ) {
                        PropertyName name = metadataProperty.getPropertyName();
                        Object value = metadataProperty.getReplacementValue();

                        if ( value == null ) {
                            LOG.debug( "    Remove: " + name );
                            rec.removeNode( name );
                            updated = true;
                        } else if ( value instanceof String ) {
                            LOG.debug( "    Update: " + name + " with: " + value );
                            try {
                                rec.update( name, (String) value );
                                updated = true;
                            } catch ( Exception e ) {
                                LOG.info( "Update or record " + rec + " failed: " + e.getMessage() );
                            }
                        } else if ( value instanceof OMElement ) {
                            LOG.debug( "    Update: " + name + " with xml: " + value );
                            rec.update( name, (OMElement) value );
                            updated = true;
                        } else {
                            LOG.warn( "Could not update propertyName: " + name
                                      + ": must be a string, an OMELement or null!" );
                        }
                    }
                    // inspect element if it is still valid
                    for ( RecordInspector<ISORecord> inspector : inspectors ) {
                        rec = inspector.inspect( rec, conn, connectionType );
                    }
                }
                if ( rec != null ) {
                    generateQP.executeUpdate( conn, rec, rec.getIdentifier() );
                    if ( updated )
                        result++;
                }
            }
        } catch ( Throwable t ) {
            String msg = Messages.getMessage( "ERROR_REQUEST_TYPE", ResultType.results.name(), t.getMessage() );
            LOG.info( msg );
            throw new MetadataStoreException( msg );
        }
        return result;
    }

    @Override
    public void rollback()
                            throws MetadataStoreException {
        LOG.debug( Messages.getMessage( "INFO_TA_ROLLBACK" ) );
        try {
            conn.rollback();
        } catch ( SQLException e ) {
            String msg = Messages.getMessage( "ERROR_TA_ROLLBACK", e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        } finally {
            JDBCUtils.close( conn );
        }
    }
}