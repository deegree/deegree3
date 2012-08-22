//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2006 by: M.O.S.S. Computer Grafik Systeme GmbH
 Hohenbrunner Weg 13
 D-82024 Taufkirchen
 http://www.moss.de/

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 ---------------------------------------------------------------------------*/
package org.deegree.io.datastore.sde;

import java.util.List;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.DatastoreTransaction;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.sdeapi.SDEConnection;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcwebservices.wfs.operation.transaction.Native;

/**
 * Handles <code>Transaction</code> requests to SQL based datastores.
 * 
 * @author <a href="mailto:cpollmann@moss.de">Christoph Pollmann</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class SDETransaction extends AbstractSDERequestHandler implements DatastoreTransaction {

    private static final ILogger LOG = LoggerFactory.getLogger( SDETransaction.class );

    /**
     * Creates a new instance of <code>SQLTransaction</code> from the given parameters.
     * 
     * @param datastore
     * @param aliasGenerator
     * @param conn
     */
    public SDETransaction( SDEDatastore datastore, TableAliasGenerator aliasGenerator, SDEConnection conn ) {
        super( datastore, aliasGenerator, conn );
    }

    /**
     * Starts a new transaction.
     * 
     * @throws DatastoreException
     */
    public void begin()
                            throws DatastoreException {
        try {
            conn.getConnection().startTransaction();
        } catch ( Exception e ) {
            String msg = "Unable to commit transaction: " + e.getMessage();
            LOG.logError( msg );
            throw new DatastoreException( msg, e );
        }
    }

    /**
     * Makes the changes persistent that have been performed in this transaction.
     * 
     * @throws DatastoreException
     */
    public void commit()
                            throws DatastoreException {
        try {
            conn.getConnection().commitTransaction();
            conn.close();
        } catch ( Exception e ) {
            String msg = "Unable to commit transaction: " + e.getMessage();
            LOG.logError( msg );
            throw new DatastoreException( msg, e );
        }
    }

    /**
     * Aborts the changes that have been performed in this transaction.
     * 
     * @throws DatastoreException
     */
    public void rollback()
                            throws DatastoreException {
        try {
            conn.getConnection().rollbackTransaction();
            conn.close();
        } catch ( Exception e ) {
            String msg = "Unable to rollback transaction: " + e.getMessage();
            LOG.logError( msg );
            throw new DatastoreException( msg, e );
        }
    }

    /**
     * Returns the transaction instance so other clients may acquire a transaction (and underlying resources, such as
     * JDBCConnections can be freed).
     * 
     * @throws DatastoreException
     */
    public void release()
                            throws DatastoreException {
        this.datastore.releaseTransaction( this );
    }

    /**
     * Inserts the given feature instances into the datastore.
     * 
     * @param features
     * @return feature ids of the inserted (root) features
     * @throws DatastoreException
     */
    public List<FeatureId> performInsert( List<Feature> features )
                            throws DatastoreException {
        throw new UnsupportedOperationException( "Insert on a SDE-Datastore is not supported yet" );
        /*
         * SDEInsertHandler handler = new SDEInsertHandler( this ); List<FeatureId> fids = handler.performInsert(
         * features ); return fids;
         */
    }

    /**
     * Performs an update operation against the datastore.
     * 
     * @param mappedFeatureType
     *            feature type that is to be updated
     * @param replacementProps
     *            properties and their replacement values
     * @param filter
     *            selects the feature instances that are to be updated
     * @param lockId
     *            optional id of associated lock (may be null)
     * @return number of updated feature instances
     * @throws DatastoreException
     */
    public int performUpdate( MappedFeatureType mappedFeatureType, Map<PropertyPath, FeatureProperty> replacementProps,
                              Filter filter, String lockId )
                            throws DatastoreException {

        SDEUpdateHandler handler = new SDEUpdateHandler( this, this.aliasGenerator, this.conn );
        int updatedFeatures = handler.performUpdate( mappedFeatureType, replacementProps, filter );
        return updatedFeatures;
    }

    /**
     * Performs an update operation against the datastore.
     * <p>
     * The filter is expected to match exactly one feature which will be replaced by the specified replacement feature.
     * 
     * @param mappedFeatureType
     *            feature type that is to be updated
     * @param replacementFeature
     *            feature instance that will replace the selected feature
     * @param filter
     *            selects the single feature instances that is to be replaced
     * @param lockId
     *            optional id of associated lock (may be null)
     * @return number of updated feature instances (must be 0 or 1)
     * @throws DatastoreException
     */
    public int performUpdate( MappedFeatureType mappedFeatureType, Feature replacementFeature, Filter filter,
                              String lockId )
                            throws DatastoreException {

        // SDEUpdateHandler handler = new SDEUpdateHandler( this, this.aliasGenerator, this.conn );
        // int updatedFeatures = handler.performUpdate( mappedFeatureType, replacementFeature,
        // filter );
        return 0;// updatedFeatures;
    }

    /**
     * Performs a replace operation against the datastore.
     * 
     * @param mappedFeatureType
     *            feature type that is to be replaced
     * @param replacementFeature
     *            feature instance that will be used to replace the properties of the selected features
     * @param filter
     *            selects the feature instances that are to be replaced
     * @param lockId
     *            optional id of associated lock (may be null)
     * @return number of replaced feature instances
     * @throws DatastoreException
     */
    public int performReplace( MappedFeatureType mappedFeatureType, Feature replacementFeature, Filter filter,
                               String lockId )
                            throws DatastoreException {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes the features from the datastore that are matched by the given filter and type.
     * 
     * @param mappedFeatureType
     * @param filter
     * @param lockId
     *            optional id of associated lock (may be null)
     * @return number of deleted feature instances
     * @throws DatastoreException
     */
    public int performDelete( MappedFeatureType mappedFeatureType, Filter filter, String lockId )
                            throws DatastoreException {

        SDEDeleteHandler handler = new SDEDeleteHandler( this, this.aliasGenerator, this.conn );
        int deletedFeatures = handler.performDelete( mappedFeatureType, filter );
        return deletedFeatures;
    }

    /**
     * Performs a 'native' operation against the datastore.
     * 
     * @param operation
     * @return number of processed feature instances.
     * @throws DatastoreException
     */
    public int performNative( Native operation )
                            throws DatastoreException {
        throw new UnsupportedOperationException( "Native not implemented yet." );
    }
}