//$Header: /deegreerepository/deegree/src/org/deegree/ogcwebservices/wfs/TransactionHandler.java,v 1.69 2007/03/14 14:41:43 mschneider Exp $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.ogcwebservices.wfs;

import static java.util.Collections.singletonList;
import static org.deegree.ogcwebservices.wfs.operation.transaction.Transaction.RELEASE_ACTION.ALL;
import static org.deegree.ogcwebservices.wfs.operation.transaction.Transaction.RELEASE_ACTION.SOME;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.Datastore;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.DatastoreTransaction;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.LockManager;
import org.deegree.io.datastore.MissingLockIdException;
import org.deegree.io.datastore.PropertyPathResolver;
import org.deegree.io.datastore.idgenerator.FeatureIdAssigner;
import org.deegree.io.datastore.schema.MappedFeaturePropertyType;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGMLSchema;
import org.deegree.io.datastore.sql.transaction.SQLTransaction;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureAdapter;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.feature.Validator;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathStep;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.operation.transaction.Delete;
import org.deegree.ogcwebservices.wfs.operation.transaction.Insert;
import org.deegree.ogcwebservices.wfs.operation.transaction.InsertResults;
import org.deegree.ogcwebservices.wfs.operation.transaction.Native;
import org.deegree.ogcwebservices.wfs.operation.transaction.Transaction;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionOperation;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionResponse;
import org.deegree.ogcwebservices.wfs.operation.transaction.Update;
import org.deegree.ogcwebservices.wfs.operation.transaction.Insert.ID_GEN;

/**
 * Handler for transaction requests to the {@link WFService}.
 * <p>
 * If the used backend does not support atomic transactions, it is possible that one part fails while another works
 * well. Depending on definitions made in the OGC WFS 1.1.0 specification in this case it is possible that even if a sub
 * part of the request fails no exception will be thrown. In this case the result objects contains informations on the
 * parts of the request that worked and that did not.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class TransactionHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( TransactionHandler.class );

    private WFService service;

    private Transaction request;

    private Map<QualifiedName, MappedFeatureType> ftMap;

    // filled by #acquireDSTransactions()
    private Map<QualifiedName, DatastoreTransaction> taMap = new HashMap<QualifiedName, DatastoreTransaction>();

    // filled by #acquireDSTransactions()
    private Map<Datastore, DatastoreTransaction> dsToTaMap = new HashMap<Datastore, DatastoreTransaction>();

    private Set<FeatureId> changedFIDs = new TreeSet<FeatureId>();

    /**
     * Creates a new <code>TransactionHandler</code> instance.
     * 
     * @param service
     * @param request
     */
    TransactionHandler( WFService service, Transaction request ) {
        this.service = service;
        this.request = request;
        this.ftMap = service.getMappedFeatureTypes();
    }

    /**
     * Performs the associated transaction.
     * 
     * @return transaction response
     * @throws OGCWebServiceException
     *             if an error occurred
     */
    synchronized TransactionResponse handleRequest()
                            throws OGCWebServiceException {

        changedFIDs.clear();

        validate( this.request );

        TransactionResponse response = null;

        acquireDSTransactions();

        try {
            try {
                response = performOperations();
            } catch ( OGCWebServiceException e ) {
                abortDSTransactions();
                throw e;
            }
            commitDSTransactions();
            if ( request.getLockId() != null && request.getReleaseAction() == ALL ) {
                try {
                    LockManager.getInstance().releaseLock( request.getLockId() );
                } catch ( DatastoreException e ) {
                    throw new InvalidParameterValueException( e.getMessage() );
                }
            }
            if ( request.getLockId() != null && request.getReleaseAction() == SOME ) {
                try {
                    LockManager.getInstance().releaseLockPartly( request.getLockId(), changedFIDs );
                } catch ( DatastoreException e ) {
                    throw new InvalidParameterValueException( e.getMessage() );
                }
            }
        } finally {
            releaseDSTransactions();
        }

        return response;
    }

    /**
     * Validates the feature instances in the given transaction against the WFS' application schemas.
     * <p>
     * The feature instances are assigned the corresponding <code>MappedFeatureType</code> in the process.
     * 
     * @param request
     * @throws OGCWebServiceException
     */
    private void validate( Transaction request )
                            throws OGCWebServiceException {

        List<TransactionOperation> operations = request.getOperations();

        Iterator<TransactionOperation> iter = operations.iterator();
        while ( iter.hasNext() ) {
            TransactionOperation operation = iter.next();
            if ( operation instanceof Insert ) {
                try {
                    validateInsert( (Insert) operation );
                } catch ( DatastoreException e ) {
                    throw new OGCWebServiceException( this.getClass().getName(), e.getMessage() );
                }
            } else if ( operation instanceof Delete ) {
                validateDelete( (Delete) operation );
            } else if ( operation instanceof Update ) {
                validateUpdate( (Update) operation );
            } else if ( operation instanceof Native ) {
                // nothing to do
            } else {
                String msg = "Internal error. Unhandled transaction operation type '" + operation.getClass().getName()
                             + "'.";
                throw new OGCWebServiceException( this.getClass().getName(), msg );
            }
        }
    }

    /**
     * Validates all feature instances in the given insert operation against the WFS' application schemas.
     * <p>
     * The feature instances are assigned the corresponding <code>MappedFeatureType</code> in the process and are
     * disambiguated, so every feature has a unique feature id afterwards.
     * 
     * @param operation
     * @throws OGCWebServiceException
     * @throws DatastoreException
     */
    @SuppressWarnings("unchecked")
    private void validateInsert( Insert operation )
                            throws OGCWebServiceException, DatastoreException {
        FeatureCollection fc = operation.getFeatures();
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            try {
                GMLFeatureAdapter ada = new GMLFeatureAdapter( false );
                GMLFeatureCollectionDocument doc = ada.export( fc );
                LOG.logDebugXMLFile( "TransactionHandler_insert_incoming", doc );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
            }
        }
        Validator validator = new Validator( (Map) this.service.getMappedFeatureTypes() );
        for ( int i = 0; i < fc.size(); i++ ) {
            validator.validate( fc.getFeature( i ) );
        }

        // merge all equal and anonymous features (without fid)
        FeatureDisambiguator merger = new FeatureDisambiguator( fc );
        if ( operation.getIdGen() == ID_GEN.USE_EXISTING ) {
            if ( merger.checkForAnonymousFeatures() ) {
                String msg = Messages.getMessage( "WFS_INSERT_USE_EXISTING_AND_NO_FID" );
                throw new DatastoreException( msg );
            }
        }
        fc = merger.mergeFeatures();
    }

    /**
     * Validates all feature instances in the given insert operation against the WFS' application schemas.
     * <p>
     * The feature instances are assigned the corresponding <code>MappedFeatureType</code> in the process.
     * 
     * @param operation
     * @throws OGCWebServiceException
     */
    private void validateDelete( Delete operation )
                            throws OGCWebServiceException {
        QualifiedName ftName = operation.getTypeName();
        MappedFeatureType ft = this.ftMap.get( ftName );
        if ( ft == null ) {
            String msg = Messages.getMessage( "WFS_DELETE_FEATURE_TYPE_UNKNOWN", ftName );
            throw new OGCWebServiceException( this.getClass().getName(), msg );
        }
        if ( ft.isAbstract() ) {
            String msg = Messages.getMessage( "WFS_DELETE_FEATURE_TYPE_ABSTRACT", ftName );
            throw new OGCWebServiceException( this.getClass().getName(), msg );
        }
    }

    /**
     * Validates any feature instance in the given update operation against the WFS' application schemas.
     * <p>
     * Feature instances are assigned the corresponding <code>MappedFeatureType</code> in the process, property names
     * are normalized and their values are parsed into the respective objects.
     * 
     * @param operation
     *            update operation
     * @throws OGCWebServiceException
     */
    @SuppressWarnings("unchecked")
    private void validateUpdate( Update operation )
                            throws OGCWebServiceException {

        QualifiedName ftName = operation.getTypeName();
        MappedFeatureType ft = this.ftMap.get( ftName );
        if ( ft == null ) {
            String msg = Messages.getMessage( "WFS_UPDATE_FEATURE_TYPE_UNKNOWN", ftName );
            throw new OGCWebServiceException( this.getClass().getName(), msg );
        }

        Feature feature = operation.getFeature();
        if ( feature != null ) {
            Validator validator = new Validator( (Map) this.service.getMappedFeatureTypes() );
            validator.validate( feature );
        } else {
            validateProperties( ft, operation );
        }
    }

    /**
     * Validates the properties and their replacement values that are specified in the given <code>Update</code>
     * operation.
     * <p>
     * Property names are normalized and their values are parsed into the respective objects.
     * 
     * @param ft
     *            feature type
     * @param operation
     *            update operation
     * @throws OGCWebServiceException
     */
    private void validateProperties( MappedFeatureType ft, Update operation )
                            throws OGCWebServiceException {

        Map<PropertyPath, FeatureProperty> replacementProps = operation.getReplacementProperties();
        Map<PropertyPath, FeatureProperty> normalizedProps = new HashMap<PropertyPath, FeatureProperty>();

        for ( PropertyPath path : replacementProps.keySet() ) {
            FeatureProperty property = replacementProps.get( path );
            path = PropertyPathResolver.normalizePropertyPath( ft, null, path );
            validateProperty( ft, path, property );
            normalizedProps.put( path, property );
        }

        // remove all mappings and add normalized ones
        replacementProps.clear();
        for ( PropertyPath path : normalizedProps.keySet() ) {
            replacementProps.put( path, normalizedProps.get( path ) );
        }
    }

    /**
     * Validates the property name and it's replacement value.
     * <p>
     * Values are parsed into the respective objects.
     * 
     * @param ft
     *            feature type
     * @param path
     *            property name
     * @param replacementProperty
     *            replacement property value (as XML node)
     * @throws OGCWebServiceException
     */
    private void validateProperty( MappedFeatureType ft, PropertyPath path, FeatureProperty replacementProperty )
                            throws OGCWebServiceException {

        for ( int i = 0; i < path.getSteps(); i += 2 ) {
            // check if feature step is valid
            PropertyPathStep ftStep = path.getStep( i );
            FeatureType stepFt = this.ftMap.get( ftStep.getPropertyName() );
            if ( stepFt == null ) {
                String msg = Messages.getMessage( "WFS_UPDATE_FEATURE_STEP_UNKNOWN", path, "unknown" );
                throw new OGCWebServiceException( this.getClass().getName(), msg );
            }
            MappedGMLSchema schema = ft.getGMLSchema();
            if ( !schema.isValidSubstitution( ft, stepFt ) ) {
                String msg = Messages.getMessage( "WFS_UPDATE_FEATURE_STEP_INVALID", path, stepFt.getName(),
                                                  ft.getName() );
                throw new OGCWebServiceException( this.getClass().getName(), msg );
            }

            // check if property step is valid
            PropertyPathStep propertyStep = path.getStep( i + 1 );
            QualifiedName propertyName = propertyStep.getPropertyName();
            PropertyType pt = ft.getProperty( propertyName );
            if ( pt == null ) {
                String msg = Messages.getMessage( "WFS_UPDATE_PROPERTY_STEP_UNKNOWN", path, propertyName, ft.getName() );
                throw new OGCWebServiceException( this.getClass().getName(), msg );
            }
            if ( i + 2 == path.getSteps() ) {
                if ( replacementProperty.getValue() == null && pt.getMinOccurs() > 0 ) {
                    String msg = Messages.getMessage( "WFS_UPDATE_PROPERTY_NULL_INVALID", path, pt.getMinOccurs() );
                    throw new InvalidParameterValueException( this.getClass().getName(), msg );
                }
                if ( replacementProperty.getValue() instanceof Feature ) {
                    Validator validator = new Validator( (Map) this.service.getMappedFeatureTypes() );
                    validator.validate( (Feature) replacementProperty.getValue() );
                }
            } else {
                if ( !( pt instanceof MappedFeaturePropertyType ) ) {
                    String msg = Messages.getMessage( "WFS_UPDATE_NOT_FEATURE_PROPERTY", path, propertyName );
                    throw new OGCWebServiceException( this.getClass().getName(), msg );
                }
                MappedFeaturePropertyType fpt = (MappedFeaturePropertyType) pt;
                ft = fpt.getFeatureTypeReference().getFeatureType();
            }
        }
    }

    /**
     * Performs the operations contained in the transaction.
     * 
     * @throws OGCWebServiceException
     */
    private TransactionResponse performOperations()
                            throws OGCWebServiceException {

        int inserts = 0;
        int deletes = 0;
        int updates = 0;

        List<InsertResults> insertResults = new ArrayList<InsertResults>();
        List<TransactionOperation> operations = request.getOperations();
        List<Exception> exceptions = new ArrayList<Exception>();

        Iterator<TransactionOperation> iter = operations.iterator();
        while ( iter.hasNext() ) {
            TransactionOperation operation = iter.next();
            String handle = operation.getHandle();
            try {
                if ( operation instanceof Insert ) {
                    List<FeatureId> insertedFIDs = performInsert( (Insert) operation );
                    inserts += insertedFIDs.size();

                    for ( FeatureId id : insertedFIDs ) {
                        InsertResults results = new InsertResults( handle, singletonList( id ) );
                        insertResults.add( results );
                    }
                } else if ( operation instanceof Delete ) {
                    // the intended behavior is different between versions here, so in #performDelete the exception
                    // will be thrown now, and caught here if 1.1.0
                    if ( request.getVersion().equals( "1.0.0" ) ) {
                        deletes += performDelete( (Delete) operation );
                    } else {
                        try {
                            deletes += performDelete( (Delete) operation );
                        } catch ( MissingLockIdException e ) {
                            throw new MissingParameterValueException( "LockId", e.getMessage() );
                        }
                    }
                } else if ( operation instanceof Update ) {
                    if ( request.getVersion().equals( "1.0.0" ) ) {
                        try {
                            updates += performUpdate( (Update) operation );
                        } catch ( MissingLockIdException e ) {
                            exceptions.add( e );
                        }
                    } else {
                        updates += performUpdate( (Update) operation );
                    }
                } else if ( operation instanceof Native ) {
                    String msg = Messages.getMessage( "WFS_NATIVE_OPERATIONS_UNSUPPORTED" );
                    throw new OGCWebServiceException( this.getClass().getName(), msg );
                } else {
                    String opType = operation.getClass().getName();
                    String msg = Messages.getMessage( "WFS_UNHANDLED_OPERATION_TYPE", opType );
                    throw new OGCWebServiceException( this.getClass().getName(), msg );
                }
            } catch ( DatastoreException e ) {
                LOG.logError( e.getMessage(), e );
                String msg = "A datastore exception occured during the processing of operation with handle '" + handle
                             + "': " + e.getMessage();
                throw new InvalidParameterValueException( this.getClass().getName(), msg );
            }
        }
        TransactionResponse response = new TransactionResponse( request, inserts, updates, deletes, insertResults,
                                                                exceptions );
        return response;
    }

    /**
     * Performs the given insert operation.
     * 
     * @param insert
     *            insert operation to be performed
     * @throws DatastoreException
     */
    private List<FeatureId> performInsert( Insert insert )
                            throws DatastoreException {

        List<FeatureId> fids = new ArrayList<FeatureId>();
        FeatureCollection fc = insert.getFeatures();

        // clear reference to feature collection
        // insert.setFeatureCollection( null );

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            try {
                GMLFeatureAdapter ada = new GMLFeatureAdapter( false );
                GMLFeatureCollectionDocument doc = ada.export( fc );
                LOG.logDebugXMLFile( "TransactionHandler_insert_merged", doc );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
            }
        }

        Map<DatastoreTransaction, List<Feature>> taFeaturesMap = new HashMap<DatastoreTransaction, List<Feature>>();
        FeatureIdAssigner fidAssigner = new FeatureIdAssigner( insert.getIdGen() );

        // assign features to corresponding datastore transactions
        for ( int i = 0; i < fc.size(); i++ ) {
            Feature feature = fc.getFeature( i );
            QualifiedName ftName = feature.getName();
            DatastoreTransaction dsTa = this.taMap.get( ftName );
            // reassign feature ids (if necessary)
            fidAssigner.assignFID( feature, dsTa );
            List<Feature> features = taFeaturesMap.get( dsTa );
            if ( features == null ) {
                features = new ArrayList<Feature>();
                taFeaturesMap.put( dsTa, features );
            }
            features.add( feature );
        }

        // TODO remove this hack
        fidAssigner.markStoredFeatures();

        // clear reference to fidAssigner (implicitly to feature collection)
        fidAssigner = null;

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            try {
                GMLFeatureAdapter ada = new GMLFeatureAdapter( false );
                GMLFeatureCollectionDocument doc = ada.export( fc );
                LOG.logDebugXMLFile( "TransactionHandler_insert_marked", doc );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
            }
        }

        // clear reference to feature collection
        fc = null;

        for ( DatastoreTransaction ta : taFeaturesMap.keySet() ) {
            List<Feature> features = taFeaturesMap.get( ta );
            fids.addAll( ta.performInsert( features ) );
        }
        return fids;
    }

    /**
     * Performs the given delete operation.
     * 
     * @param delete
     *            delete operation to be performed
     * @throws DatastoreException
     */
    private int performDelete( Delete delete )
                            throws DatastoreException {
        QualifiedName ftName = delete.getTypeName();
        MappedFeatureType ft = this.ftMap.get( ftName );
        DatastoreTransaction dsTa = this.taMap.get( ftName );
        int deleted = dsTa.performDelete( ft, delete.getFilter(), this.request.getLockId() );
        return deleted;
    }

    /**
     * Performs the given update operation.
     * <p>
     * Assigning of FIDs to replacement features is performed in the {@link DatastoreTransaction}.
     * 
     * @param update
     *            update operation to be perform
     * @throws DatastoreException
     */
    private int performUpdate( Update update )
                            throws DatastoreException {

        QualifiedName ftName = update.getTypeName();
        MappedFeatureType ft = this.ftMap.get( ftName );
        DatastoreTransaction dsTa = this.taMap.get( ftName );
        int updated = 0;
        if ( update.getFeature() == null ) {
            updated = dsTa.performUpdate( ft, update.getReplacementProperties(), update.getFilter(),
                                          this.request.getLockId() );
        } else {
            updated = dsTa.performUpdate( ft, update.getFeature(), update.getFilter(), this.request.getLockId() );
        }

        // to work around API changes...
        if ( dsTa instanceof SQLTransaction ) {
            changedFIDs.addAll( ( (SQLTransaction) dsTa ).determineAffectedFIDs( ft, update.getFilter() ) );
        }

        return updated;
    }

    /**
     * Acquires the necessary <code>DatastoreTransaction</code>s. For each participating <code>Datastore</code>, one
     * transaction is needed.
     * <p>
     * Fills the <code>taMap</code> and <code>dsToTaMap</code> members of this class.
     * 
     * @throws OGCWebServiceException
     *             if a feature type is unknown or a DatastoreTransaction could not be acquired
     */
    private void acquireDSTransactions()
                            throws OGCWebServiceException {
        Set<QualifiedName> ftNames = this.request.getAffectedFeatureTypes();
        for ( QualifiedName ftName : ftNames ) {
            MappedFeatureType ft = this.ftMap.get( ftName );
            if ( ft == null ) {
                String msg = "FeatureType '" + ftName + "' is not known to the WFS.";
                throw new OGCWebServiceException( this.getClass().getName(), msg );
            }
            Datastore ds = ft.getGMLSchema().getDatastore();
            DatastoreTransaction dsTa = this.dsToTaMap.get( ds );
            if ( dsTa == null ) {
                try {
                    dsTa = ds.acquireTransaction();
                } catch ( DatastoreException e ) {
                    LOG.logError( e.getMessage(), e );
                    String msg = "Could not acquire transaction for FeatureType '" + ftName + "'.";
                    throw new OGCWebServiceException( this.getClass().getName(), msg );
                }
                this.dsToTaMap.put( ds, dsTa );
            }
            this.taMap.put( ftName, dsTa );
        }
    }

    /**
     * Releases all acquired <code>DatastoreTransaction</code>s.
     * 
     * @throws OGCWebServiceException
     *             if a DatastoreTransaction could not be released
     */
    private void releaseDSTransactions()
                            throws OGCWebServiceException {
        String msg = "";
        for ( DatastoreTransaction dsTa : this.dsToTaMap.values() ) {
            LOG.logDebug( "Releasing DatastoreTransaction " + dsTa );
            try {
                dsTa.release();
            } catch ( DatastoreException e ) {
                LOG.logError( "Error releasing DatastoreTransaction: " + e.getMessage(), e );
                msg += e.getMessage() + "\n";
            }
        }
        if ( msg.length() != 0 ) {
            msg = "Could not release one or more DatastoreTransactions: " + msg;
            throw new OGCWebServiceException( this.getClass().getName(), msg );
        }
    }

    /**
     * Commits all pending <code>DatastoreTransaction</code>s.
     * 
     * @throws OGCWebServiceException
     *             if a DatastoreException could not be committed
     */
    private void commitDSTransactions()
                            throws OGCWebServiceException {
        String msg = "";
        for ( DatastoreTransaction dsTa : this.dsToTaMap.values() ) {
            LOG.logDebug( "Committing DatastoreTransaction " + dsTa );
            try {
                dsTa.commit();
            } catch ( DatastoreException e ) {
                LOG.logError( "Error committing DatastoreTransaction: " + e.getMessage(), e );
                msg += e.getMessage() + "\n";
            }
        }
        if ( msg.length() != 0 ) {
            msg = "Could not commit one or more DatastoreTransactions: " + msg;
            throw new OGCWebServiceException( this.getClass().getName(), msg );
        }
    }

    /**
     * Aborts all pending <code>DatastoreTransaction</code>s.
     * 
     * @throws OGCWebServiceException
     *             if a DatastoreException could not be aborted
     */
    private void abortDSTransactions()
                            throws OGCWebServiceException {
        String msg = "";
        for ( DatastoreTransaction dsTa : this.dsToTaMap.values() ) {
            LOG.logDebug( "Aborting DatastoreTransaction " + dsTa );
            try {
                dsTa.rollback();
            } catch ( DatastoreException e ) {
                LOG.logError( "Error aborting DatastoreTransaction: " + e.getMessage(), e );
                msg += e.getMessage() + "\n";
            }
        }
        if ( msg.length() != 0 ) {
            msg = "Could not abort one or more DatastoreTransactions: " + msg;
            throw new OGCWebServiceException( this.getClass().getName(), msg );
        }
    }
}
