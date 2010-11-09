package org.deegree.metadata.persistence.iso;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sql.postgis.PostGISWhereBuilder;
import org.deegree.metadata.ISORecord;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.persistence.iso.generating.GenerateQueryableProperties;
import org.deegree.metadata.persistence.iso.parsing.IdUtils;
import org.deegree.metadata.persistence.iso.parsing.inspectation.RecordInspector;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.AnyText;
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

    private final List<RecordInspector> inspectors;

    private final AnyText anyText;

    private final boolean useLegacyPredicates;

    ISOMetadataStoreTransaction( Connection conn, List<RecordInspector> inspectors, AnyText anyText,
                                 boolean useLegacyPredicates ) throws SQLException {
        this.conn = conn;
        this.anyText = anyText;
        this.inspectors = inspectors;
        this.useLegacyPredicates = useLegacyPredicates;
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
        }
    }

    @Override
    public int performDelete( DeleteTransaction delete )
                            throws MetadataStoreException {
        PostGISWhereBuilder builder = null;

        try {
            builder = new PostGISWhereBuilder( new PostGISMappingsISODC(), (OperatorFilter) delete.getConstraint(),
                                               null, useLegacyPredicates );

            ExecuteStatements execStm = new ExecuteStatements();
            return execStm.executeDeleteStatement( conn, builder );

        } catch ( FilterEvaluationException e ) {
            throw new MetadataStoreException( "The Filterexpression has thrown an error! " + e.getMessage() );
        }
    }

    @Override
    public List<String> performInsert( InsertTransaction insert )
                            throws MetadataStoreException, MetadataInspectorException {

        List<String> identifierList = new ArrayList<String>();
        for ( OMElement element : insert.getElements() ) {

            try {
                for ( RecordInspector r : inspectors ) {
                    element = r.inspect( element, conn );
                }
                if ( element != null ) {
                    ISORecord rec = new ISORecord( element, anyText );

                    if ( IdUtils.newInstance( conn ).proveIdExistence( rec.getIdentifier() ) ) {
                        GenerateQueryableProperties generateQP = new GenerateQueryableProperties();
                        int operatesOnId = generateQP.generateMainDatabaseDataset( conn, rec );
                        generateQP.executeQueryableProperties( false, conn, operatesOnId, rec );
                        identifierList.addAll( Arrays.asList( rec.getIdentifier() ) );
                    }
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
        int result = 0;
        if ( update.getElement() != null ) {

            ISORecord rec = new ISORecord( update.getElement(), anyText );
            GenerateQueryableProperties generateQP = new GenerateQueryableProperties();
            int operatesOnId = generateQP.updateMainDatabaseTable( conn, rec );
            generateQP.executeQueryableProperties( true, conn, operatesOnId, rec );
            result++;

        }

        // PostGISMappingsISODC mapping = new PostGISMappingsISODC();
        // PostGISWhereBuilder builder = null;
        // int result = 0;
        // /*
        // * if there should a complete record be updated or some properties
        // */
        // if ( update.getElement() != null ) {
        //
        // ExecuteStatements executeStatements = new ExecuteStatements();
        //
        // result = executeStatements.executeUpdateStatement( conn,
        // new ISOQPParsing().parseAPISO( update.getElement() ) );
        //
        // } else {
        //
        // // try {
        // // RecordStoreOptions gdds = new RecordStoreOptions( update.getConstraint(),
        // // new URI( update.getTypeName().getNamespaceURI() ), null,
        // // ResultType.results, ReturnableElement.full, result,
        // // result );
        // // } catch ( URISyntaxException e1 ) {
        // // // TODO Auto-generated catch block
        // // e1.printStackTrace();
        // // }
        //
        // int formatNumber = 0;
        // Set<QName> qNameSet = new HashSet<QName>();
        //
        // // TODO sortProperty
        //
        // try {
        // builder = new PostGISWhereBuilder( mapping, (OperatorFilter) update.getConstraint(), null,
        // useLegacyPredicates );
        // } catch ( FilterEvaluationException e ) {
        // throw new MetadataStoreException( e.getMessage() );
        // }
        //
        // }
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
        }
    }
}