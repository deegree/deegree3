package org.deegree.metadata.persistence.iso;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sql.postgis.PostGISWhereBuilder;
import org.deegree.metadata.ISORecord;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.persistence.iso.generating.GenerateQueryableProperties;
import org.deegree.metadata.persistence.iso.parsing.inspectation.CoupledDataInspector;
import org.deegree.metadata.persistence.iso.parsing.inspectation.FileIdentifierInspector;
import org.deegree.metadata.persistence.iso.parsing.inspectation.InspireCompliance;
import org.deegree.metadata.persistence.iso.parsing.inspectation.MetadataValidation;
import org.deegree.metadata.persistence.iso.parsing.inspectation.ResourceIdentifier;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig;
import org.deegree.metadata.persistence.types.ConfigurationAccess;
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

    private final ConfigurationAccess ca;

    private final boolean useLegacyPredicates;

    ISOMetadataStoreTransaction( Connection conn, ISOMetadataStoreConfig config, boolean useLegacyPredicates )
                            throws SQLException {
        this.conn = conn;

        FileIdentifierInspector fi = FileIdentifierInspector.newInstance( config.getIdentifierInspector(), conn );
        InspireCompliance ic = InspireCompliance.newInstance( config.getRequireInspireCompliance(), conn );
        CoupledDataInspector ci = CoupledDataInspector.newInstance( config.getCoupledResourceInspector(), conn );
        MetadataValidation mv = MetadataValidation.newInstance( config.isValidate() );
        this.ca = ConfigurationAccess.newInstance( fi, ic, ci, mv );
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
                            throws MetadataStoreException {
        List<String> identifierList = new ArrayList<String>();
        for ( OMElement element : insert.getElements() ) {

            try {
                OMElement elemFI = ca.getFi().inspect( element );
                if ( elemFI != null ) {
                    OMElement elemRI = ResourceIdentifier.newInstance( ca.getIc().getRic(), conn ).inspect( elemFI );

                    ISORecord rec = new ISORecord( elemRI );
                    GenerateQueryableProperties generateQP = new GenerateQueryableProperties();
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
                            throws MetadataStoreException {

        PostGISMappingsISODC mapping = new PostGISMappingsISODC();
        PostGISWhereBuilder builder = null;
        int result = 0;
        /*
         * if there should a complete record be updated or some properties
         */
        if ( update.getElement() != null ) {
            QName localName = update.getElement().getQName();

            ExecuteStatements executeStatements = new ExecuteStatements();

            // if ( localName.getLocalPart().equals( "Record" ) ) {
            //
            // result = executeStatements.executeUpdateStatement(
            // conn,
            // new ISOQPParsing( ca ).parseAPDC( update.getElement() ) );
            //
            // } else {
            // result = executeStatements.executeUpdateStatement(
            // conn,
            // new ISOQPParsing( ca ).parseAPISO(
            // update.getElement(),
            // true ) );
            //
            // }

        } else {

            // try {
            // RecordStoreOptions gdds = new RecordStoreOptions( update.getConstraint(),
            // new URI( update.getTypeName().getNamespaceURI() ), null,
            // ResultType.results, ReturnableElement.full, result,
            // result );
            // } catch ( URISyntaxException e1 ) {
            // // TODO Auto-generated catch block
            // e1.printStackTrace();
            // }

            int formatNumber = 0;
            Set<QName> qNameSet = new HashSet<QName>();

            // TODO sortProperty

            try {
                builder = new PostGISWhereBuilder( mapping, (OperatorFilter) update.getConstraint(), null,
                                                   useLegacyPredicates );
            } catch ( FilterEvaluationException e ) {
                throw new MetadataStoreException( e.getMessage() );
            }

        }
        return result;
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