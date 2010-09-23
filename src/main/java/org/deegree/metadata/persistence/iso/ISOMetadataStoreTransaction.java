package org.deegree.metadata.persistence.iso;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sql.postgis.PostGISWhereBuilder;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.persistence.iso.parsing.CoupledDataInspector;
import org.deegree.metadata.persistence.iso.parsing.FileIdentifierInspector;
import org.deegree.metadata.persistence.iso.parsing.ISOQPParsing;
import org.deegree.metadata.persistence.iso.parsing.InspireCompliance;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig;
import org.deegree.metadata.publication.DeleteTransaction;
import org.deegree.metadata.publication.InsertTransaction;
import org.deegree.metadata.publication.UpdateTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MetadataStoreException} implementation for the {@link ISOMetadataStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ISOMetadataStoreTransaction implements MetadataStoreTransaction {

    private static final Logger LOG = LoggerFactory.getLogger( ISOMetadataStoreTransaction.class );

    private final Connection conn;

    private final FileIdentifierInspector fi;

    private final InspireCompliance ic;

    private final CoupledDataInspector ci;

    private final Map<QName, Integer> typeNames;

    private final boolean useLegacyPredicates;

    ISOMetadataStoreTransaction( Connection conn, ISOMetadataStoreConfig config, Map<QName, Integer> typeNames,
                                 boolean useLegacyPredicates ) throws SQLException {
        this.conn = conn;
        fi = FileIdentifierInspector.newInstance( config.getIdentifierInspector(), conn );
        ic = InspireCompliance.newInstance( config.getRequireInspireCompliance(), conn );
        ci = CoupledDataInspector.newInstance( config.getCoupledResourceInspector(), conn );
        this.typeNames = typeNames;
        this.useLegacyPredicates = useLegacyPredicates;
        conn.setAutoCommit( false );

    }

    @Override
    public void commit()
                            throws MetadataStoreException {
        LOG.debug( "Committing transaction." );
        try {
            conn.commit();
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            LOG.debug( e.getMessage(), e.getNextException() );
            throw new MetadataStoreException( "Unable to commit SQL transaction: " + e.getMessage() );
        }
    }

    @Override
    public int performDelete( DeleteTransaction delete )
                            throws MetadataStoreException {
        PostGISWhereBuilder builder = null;
        int formatNumber = 0;
        PostGISMappingsISODC mapping = new PostGISMappingsISODC();
        // if there is a typeName denoted, the record with this profile should be deleted.
        // if there is no typeName attribute denoted, every record matched should be deleted.
        if ( delete.getTypeName() != null ) {

            // for ( QName qName : typeNames.keySet() ) {
            // if ( qName.equals( delete.getTypeName() ) ) {
            // formatNumber = typeNames.get( qName );
            // }
            // }
            formatNumber = typeNames.get( delete.getTypeName() );
            if ( formatNumber == 0 ) {
                throw new InvalidParameterValueException( "The typeName could not be resolved! " );
            }
        } else {
            // TODO remove hack,
            // but: a csw record is available in every case, if not there is no iso, as well
            formatNumber = 1;
        }

        // TODO sortProperty
        try {
            builder = new PostGISWhereBuilder( mapping, (OperatorFilter) delete.getConstraint(), null,
                                               useLegacyPredicates );

            ExecuteStatements execStm = new ExecuteStatements();
            return execStm.executeDeleteStatement( conn, builder, formatNumber );

        } catch ( FilterEvaluationException e ) {

            throw new MetadataStoreException( "The Filterexpression has thrown an error! " );
        }
    }

    @Override
    public List<String> performInsert( InsertTransaction insert )
                            throws MetadataStoreException {
        ExecuteStatements execStm;
        List<String> identifierList = new ArrayList<String>();
        for ( OMElement element : insert.getElement() ) {
            QName localName = element.getQName();

            try {

                execStm = new ExecuteStatements();

                if ( localName.getLocalPart().equals( "Record" ) ) {

                    identifierList.add( execStm.executeInsertStatement( true, conn,
                                                                        new ISOQPParsing().parseAPDC( element ) ) );

                } else {

                    identifierList.add( execStm.executeInsertStatement( false, conn,
                                                                        new ISOQPParsing().parseAPISO( fi, ic, ci,
                                                                                                       element, false ) ) );

                }

            } catch ( IOException e ) {
                throw new MetadataStoreException( "Error on insert: " + e.getMessage(), e );
            }

        }
        return identifierList;
    }

    @Override
    public int performUpdate( UpdateTransaction update )
                            throws MetadataStoreException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void rollback()
                            throws MetadataStoreException {
        LOG.debug( "Performing rollback of transaction." );
        try {
            conn.rollback();
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new MetadataStoreException( "Unable to rollback SQL transaction: " + e.getMessage() );
        }
    }
}