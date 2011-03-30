package org.deegree.metadata.persistence.iso;

import static org.deegree.commons.jdbc.ConnectionManager.Type.PostgreSQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.AbstractWhereBuilder;
import org.deegree.filter.sql.mssql.MSSQLServerWhereBuilder;
import org.deegree.filter.sql.postgis.PostGISWhereBuilder;
import org.deegree.metadata.ISORecord;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.persistence.iso.generating.GenerateQueryableProperties;
import org.deegree.metadata.persistence.iso.parsing.IdUtils;
import org.deegree.metadata.persistence.iso.parsing.inspectation.RecordInspector;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.AnyText;
import org.deegree.metadata.publication.DeleteTransaction;
import org.deegree.metadata.publication.InsertTransaction;
import org.deegree.metadata.publication.MetadataProperty;
import org.deegree.metadata.publication.UpdateTransaction;
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

    private final List<RecordInspector> inspectors;

    private final AnyText anyText;

    private final boolean useLegacyPredicates;

    private final Type connectionType;

    ISOMetadataStoreTransaction( Connection conn, List<RecordInspector> inspectors, AnyText anyText,
                                 boolean useLegacyPredicates, Type connectionType ) throws SQLException {
        this.conn = conn;
        this.anyText = anyText;
        this.inspectors = inspectors;
        this.useLegacyPredicates = useLegacyPredicates;
        this.connectionType = connectionType;
        conn.setAutoCommit( false );
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
                            throws FilterEvaluationException {
        if ( connectionType == PostgreSQL ) {
            PostGISMappingsISODC mapping = new PostGISMappingsISODC();
            return new PostGISWhereBuilder( mapping, filter, null, useLegacyPredicates );
        }
        if ( connectionType == Type.MSSQL ) {
            MSSQLMappingsISODC mapping = new MSSQLMappingsISODC();
            return new MSSQLServerWhereBuilder( mapping, filter, null );
        }
        return null;
    }

    @Override
    public int performDelete( DeleteTransaction delete )
                            throws MetadataStoreException {

        try {
            AbstractWhereBuilder builder = getWhereBuilder( (OperatorFilter) delete.getConstraint() );

            ExecuteStatements execStm = new ExecuteStatements( connectionType );
            return execStm.executeDeleteStatement( conn, builder );

        } catch ( FilterEvaluationException e ) {
            throw new MetadataStoreException( e.getMessage() );
        }
    }

    @Override
    public List<String> performInsert( InsertTransaction insert )
                            throws MetadataStoreException, MetadataInspectorException {

        List<String> identifierList = new ArrayList<String>();
        for ( OMElement element : insert.getElements() ) {
            try {
                for ( RecordInspector r : inspectors ) {
                    element = r.inspect( element, conn, connectionType );
                }
                if ( element != null ) {
                    ISORecord rec = new ISORecord( element, anyText );
                    GenerateQueryableProperties generateQP = new GenerateQueryableProperties( connectionType );
                    int operatesOnId = generateQP.generateMainDatabaseDataset( conn, rec );
                    generateQP.executeQueryableProperties( false, conn, operatesOnId, rec );
                    identifierList.addAll( Arrays.asList( rec.getIdentifier() ) );
                }
            } catch ( XMLStreamException e ) {
                throw new MetadataStoreException( "Error on insert: " + e.getMessage(), e );
            }
        }
        return identifierList;
    }

    @Override
    public int performUpdate( UpdateTransaction update )
                            throws MetadataStoreException, MetadataInspectorException {
        GenerateQueryableProperties generateQP = new GenerateQueryableProperties( connectionType );
        int result = 0;
        if ( update.getElement() != null ) {
            OMElement element = update.getElement();
            for ( RecordInspector r : inspectors ) {
                element = r.inspect( element, conn, connectionType );
            }
            ISORecord rec = new ISORecord( element, anyText );
            int operatesOnId = generateQP.updateMainDatabaseTable( conn, rec, null );
            generateQP.executeQueryableProperties( true, conn, operatesOnId, rec );
            result++;
        } else if ( update.getConstraint() != null
                    && ( update.getRecordProperty() != null && update.getRecordProperty().size() > 0 ) ) {

            ResultSet rs = null;
            PreparedStatement preparedStatement = null;
            ExecuteStatements exe = new ExecuteStatements( connectionType );
            try {

                AbstractWhereBuilder builder = getWhereBuilder( (OperatorFilter) update.getConstraint() );
                ExecuteStatements execStm = new ExecuteStatements( connectionType );
                execStm.executeGetRecords( null, builder, conn );
                preparedStatement = exe.executeGetRecords( null, builder, conn );
                rs = preparedStatement.executeQuery();

                // get all metadatasets to update
                ISOMetadataResultSet isoRs = new ISOMetadataResultSet( rs, conn, preparedStatement, anyText );
                while ( isoRs.next() ) {
                    ISORecord rec = isoRs.getRecord();
                    LOG.debug( "Update record " + rec );
                    List<MetadataProperty> recordProperty = update.getRecordProperty();
                    boolean updated = false;
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
                    OMElement element = rec.getAsOMElement();
                    for ( RecordInspector r : inspectors ) {
                        element = r.inspect( element, conn, connectionType );
                    }
                    int operatesOnId = generateQP.updateMainDatabaseTable( conn, rec, rec.getIdentifier() );
                    generateQP.executeQueryableProperties( true, conn, operatesOnId, rec );
                    if ( updated )
                        result++;
                }
            } catch ( Throwable t ) {
                String msg = Messages.getMessage( "ERROR_REQUEST_TYPE", ResultType.results.name(), t.getMessage() );
                LOG.info( msg );
                throw new MetadataStoreException( msg );
            } finally {
                JDBCUtils.close( rs, preparedStatement, null, LOG );
            }
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