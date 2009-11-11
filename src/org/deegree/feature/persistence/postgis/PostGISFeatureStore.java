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
package org.deegree.feature.persistence.postgis;

import java.sql.Connection;
import java.sql.SQLException;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.StoredFeatureTypeMetadata;
import org.deegree.feature.persistence.lock.DefaultLockManager;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.wfs.getfeature.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStore} implementation that uses a PostGIS/PostgreSQL database as backend.
 * 
 * @see FeatureStore
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISFeatureStore implements FeatureStore {

    private static final Logger LOG = LoggerFactory.getLogger( PostGISFeatureStore.class );      
    
    private PostGISFeatureStoreTransaction activeTransaction;

    private Thread transactionHolder;

    private final ApplicationSchema schema;

    private final String jdbcConnId;
    
    private DefaultLockManager lockManager;        

    /**
     * Creates a new {@link PostGISFeatureStore} for the given {@link ApplicationSchema}.
     * 
     * @param schema
     *            schema information, must not be <code>null</code>
     * @param jdbcConnId
     */
    public PostGISFeatureStore( ApplicationSchema schema, String jdbcConnId ) {
        this.schema = schema;
        this.jdbcConnId = jdbcConnId;
    }

    @Override
    public FeatureStoreTransaction acquireTransaction()
                            throws FeatureStoreException {

        while ( this.activeTransaction != null ) {
            Thread holder = this.transactionHolder;
            // check if transaction holder variable has (just) been cleared or if the other thread
            // has been killed (avoid deadlocks)
            if ( holder == null || !holder.isAlive() ) {
                this.activeTransaction = null;
                this.transactionHolder = null;
                break;
            }

            try {
                // wait until the transaction holder wakes us, but not longer than 5000
                // milliseconds (as the transaction holder may very rarely get killed without
                // signalling us)
                wait( 5000 );
            } catch ( InterruptedException e ) {
                // nothing to do
            }
        }

        try {
            Connection conn = ConnectionManager.getConnection( jdbcConnId );
            conn.setAutoCommit( false );
            this.activeTransaction = new PostGISFeatureStoreTransaction( this, conn );
        } catch ( SQLException e ) {
            throw new FeatureStoreException( "Unable to acquire JDBC connection for transaction: " + e.getMessage(), e );
        }
        this.transactionHolder = Thread.currentThread();
        return this.activeTransaction;
    }

    @Override
    public void destroy() {
        LOG.debug( "destroy" );    }

    @Override
    public Envelope getEnvelope( QName ftName ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LockManager getLockManager()
                            throws FeatureStoreException {
        return lockManager;
    }

    @Override
    public StoredFeatureTypeMetadata getMetadata( QName ftName ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getObjectById( String id )
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationSchema getSchema() {
        return schema;
    }

    @Override
    public void init()
                            throws FeatureStoreException {
        LOG.debug( "init" );
        lockManager = new DefaultLockManager( this, "LOCK_DB" );
    }

    @Override
    public boolean isAvailable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int performHitsQuery( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public FeatureCollection performQuery( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FeatureCollection query( QName featureType, Filter filter, Envelope bbox, boolean withGeometries,
                                    boolean exact )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns the transaction to the datastore. This makes the transaction available to other clients again (via
     * {@link #acquireTransaction()}.
     * <p>
     * The transaction should be terminated, i.e. commit() or rollback() must have been called before.
     * 
     * @param ta
     *            the PostGISFeatureStoreTransaction to be returned
     * @throws FeatureStoreException
     */
    void releaseTransaction( PostGISFeatureStoreTransaction ta )
                            throws FeatureStoreException {
        if ( ta.getStore() != this ) {
            String msg = Messages.getMessage( "TA_NOT_OWNER" );
            throw new FeatureStoreException( msg );
        }
        if ( ta != this.activeTransaction ) {
            String msg = Messages.getMessage( "TA_NOT_ACTIVE" );
            throw new FeatureStoreException( msg );
        }
        this.activeTransaction = null;
        this.transactionHolder = null;
        // notifyAll();
    }
}
