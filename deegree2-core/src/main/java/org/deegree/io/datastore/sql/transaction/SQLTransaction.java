//$HeadURL$
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
package org.deegree.io.datastore.sql.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.DatastoreTransaction;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.sql.AbstractRequestHandler;
import org.deegree.io.datastore.sql.AbstractSQLDatastore;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.datastore.sql.transaction.delete.DeleteHandler;
import org.deegree.io.datastore.sql.transaction.insert.InsertHandler;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcwebservices.wfs.operation.transaction.Native;
import org.deegree.ogcwebservices.wfs.operation.transaction.Transaction;

/**
 * Handles {@link Transaction} requests to SQL based datastores.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SQLTransaction extends AbstractRequestHandler implements DatastoreTransaction {

    private static final ILogger LOG = LoggerFactory.getLogger( SQLTransaction.class );

    /**
     * Creates a new instance of <code>SQLTransaction</code> from the given parameters.
     *
     * @param datastore
     * @param aliasGenerator
     * @param conn
     * @throws DatastoreException
     */
    public SQLTransaction( AbstractSQLDatastore datastore, TableAliasGenerator aliasGenerator, Connection conn )
                            throws DatastoreException {
        super( datastore, aliasGenerator, conn );
        try {
            conn.setAutoCommit( false );
        } catch ( SQLException e ) {
            String msg = "Unable to disable auto commit: " + e.getMessage();
            LOG.logError( msg );
            throw new DatastoreException( msg, e );
        }
    }

    /**
     * Returns the underlying <code>AbstractSQLDatastore</code>.
     *
     * @return the underlying <code>AbstractSQLDatastore</code>
     */
    public AbstractSQLDatastore getDatastore() {
        return this.datastore;
    }

    /**
     * Returns the underlying JDBC connection.
     *
     * @return the underlying JDBC connection
     */
    public Connection getConnection() {
        return this.conn;
    }

    /**
     * Makes the changes persistent that have been performed in this transaction.
     *
     * @throws DatastoreException
     */
    public void commit()
                            throws DatastoreException {
        try {
            conn.commit();
        } catch ( SQLException e ) {
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
            conn.rollback();
        } catch ( SQLException e ) {
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

        InsertHandler handler = new InsertHandler( this, this.aliasGenerator, this.conn );
        List<FeatureId> fids = handler.performInsert( features );
        return fids;
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

        UpdateHandler handler = new UpdateHandler( this, this.aliasGenerator, this.conn, lockId );
        int updatedFeatures = handler.performUpdate( mappedFeatureType, replacementProps, filter );
        return updatedFeatures;
    }

    /**
     * Performs a update (replace-style) operation against the datastore.
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
    public int performUpdate( MappedFeatureType mappedFeatureType, Feature replacementFeature, Filter filter,
                              String lockId )
                            throws DatastoreException {

        UpdateHandler handler = new UpdateHandler( this, this.aliasGenerator, this.conn, lockId );
        int updatedFeatures = handler.performUpdate( mappedFeatureType, replacementFeature, filter );
        return updatedFeatures;
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

        DeleteHandler handler = new DeleteHandler( this, this.aliasGenerator, this.conn, lockId );
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

        throw new UnsupportedOperationException( "Native operations are not supported." );
    }
}
