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

package org.deegree.feature.persistence.lock;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.ResultSetIterator;
import org.deegree.commons.utils.CloseableIterator;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.filter.Filter;
import org.deegree.protocol.wfs.getfeature.FilterQuery;
import org.deegree.protocol.wfs.getfeature.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link LockManager} implementation that is based on an SQL database.
 * <p>
 * TODO Currently this class is only tested with Derby 10, but it should be easy to make it work with PostGIS, Oracle
 * and other SQL DBs.
 * <p/>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultLockManager implements LockManager {

    private static final Logger LOG = LoggerFactory.getLogger( DefaultLockManager.class );

    private FeatureStore store;

    private String jdbcConnId;

    /**
     * Creates a new {@link DefaultLockManager} for the given {@link FeatureStore}.
     * 
     * @param store
     * @param jdbcConnId
     * @throws FeatureStoreException
     *             if the initialization of the locking backend fails
     */
    public DefaultLockManager( FeatureStore store, String jdbcConnId ) throws FeatureStoreException {
        this.store = store;
        this.jdbcConnId = jdbcConnId;
        initDatabase();
    }

    private void initDatabase()
                            throws FeatureStoreException {

        LOG.info( "Initializing lock database." );
        Connection conn = null;
        ResultSet rs = null;
        Statement stmt = null;
        try {
            conn = ConnectionManager.getConnection( jdbcConnId );
            DatabaseMetaData dbMetaData = conn.getMetaData();
            rs = dbMetaData.getTables( null, null, "LOCKS", new String[] { "TABLE" } );
            if ( !rs.next() ) {
                LOG.debug( "Creating table 'LOCKS'." );
                stmt = conn.createStatement();
                String sql = "CREATE TABLE LOCKS (";
                sql += "ID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,";
                sql += "ACQUIRED TIMESTAMP NOT NULL,";
                sql += "EXPIRES TIMESTAMP NOT NULL";
                sql += ")";
                stmt.execute( sql );
            } else {
                LOG.debug( "Table 'LOCKS' already exists." );
            }
            rs.close();

            rs = dbMetaData.getTables( null, null, "LOCKED_FIDS", new String[] { "TABLE" } );
            if ( !rs.next() ) {
                LOG.debug( "Creating table 'LOCKED_FIDS'." );
                String sql = "CREATE TABLE LOCKED_FIDS (";
                sql += "LOCK_ID INT REFERENCES LOCKS,";
                sql += "FID VARCHAR(255) NOT NULL UNIQUE";
                sql += ")";
                stmt.execute( sql );
            } else {
                LOG.debug( "Table 'LOCKED_FIDS' already exists." );
            }
        } catch ( SQLException e ) {
            String msg = Messages.getMessage( "LOCK_DB_CREATE_ERROR", e.getMessage() );
            LOG.debug( msg, e );
            throw new FeatureStoreException( msg, e );
        } finally {
            close( rs, stmt, conn );
        }
    }

    @Override
    public Lock acquireLock( QName ftName, Filter filter, boolean mustLockAll, long expireTimeout )
                            throws FeatureStoreException {

        Query query = new FilterQuery( ftName, null, null, filter );
        // TODO don't actually fetch the feature collection, but only the fids
        FeatureCollection fc = store.performQuery( query );

        Lock lock = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection( jdbcConnId );

            // create entry in LOCKS table
            stmt = conn.prepareStatement( "INSERT INTO LOCKS (ACQUIRED, EXPIRES) VALUES (?,?)",
                                          PreparedStatement.RETURN_GENERATED_KEYS );
            Date acquired = new Date();
            Date expires = new Date( acquired.getTime() + expireTimeout );
            stmt.setTimestamp( 1, new Timestamp( acquired.getTime() ) );
            stmt.setTimestamp( 2, new Timestamp( expires.getTime() ) );
            stmt.execute();

            rs = stmt.getGeneratedKeys();
            if ( rs == null || !rs.next() ) {
                throw new FeatureStoreException( "No autogenerated key!?" );
            }
            int lockId = rs.getInt( 1 );
            rs.close();
            rs = null;
            lock = new DefaultLock( jdbcConnId, "" + lockId, acquired, expires );

            // create entries in LOCKED_FIDS table
            stmt = conn.prepareStatement( "INSERT INTO LOCKED_FIDS (LOCK_ID, FID) VALUES (?,?)" );
            for ( Feature feature : fc ) {
                String fid = feature.getId();
                stmt.setInt( 1, lockId );
                stmt.setString( 2, fid );
                stmt.execute();
            }
            conn.commit();
        } catch ( SQLException e ) {
            throw new FeatureStoreException( e.getMessage(), e );
        } finally {
            close( rs, stmt, conn );
        }
        return lock;
    }

    @Override
    public CloseableIterator<Lock> getActiveLocks() {
        CloseableIterator<Lock> lockIter = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection( jdbcConnId );
            stmt = conn.createStatement();
            rs = stmt.executeQuery( "SELECT ID,ACQUIRED,EXPIRES FROM LOCKS" );
            lockIter = new ResultSetIterator<Lock>( rs, conn, stmt ) {
                @Override
                protected Lock createElement( ResultSet rs )
                                        throws SQLException {
                    return new DefaultLock( jdbcConnId, rs.getString( 1 ), rs.getTimestamp( 2 ), rs.getTimestamp( 3 ) );
                }
            };
        } catch ( SQLException e ) {
            String msg = "Could not retrieve active locks: " + e.getMessage();
            LOG.debug( msg, e );
            throw new RuntimeException( msg, e );
        } finally {
            close( rs, stmt, conn );
        }
        return lockIter;
    }

    @Override
    public Lock getLock( String lockId ) throws FeatureStoreException {

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Lock lock = null;
        try {
            conn = ConnectionManager.getConnection( jdbcConnId );
            stmt = conn.createStatement();
            rs = stmt.executeQuery( "SELECT ACQUIRED,EXPIRES FROM LOCKS WHERE ID='" + lockId + "'" );
            if ( !rs.next() ) {
                String msg = Messages.getMessage( "LOCK_NO_SUCH_ID", lockId );
                throw new FeatureStoreException();
            }
            lock = new DefaultLock( jdbcConnId, lockId, rs.getTimestamp( 1 ), rs.getTimestamp( 2 ) );
        } catch ( SQLException e ) {
            String msg = "Could not retrieve lock with id '" + lockId + "':" + e.getMessage();
            LOG.debug( msg, e );
            throw new RuntimeException( msg, e );
        } finally {
            close( rs, stmt, conn );
        }
        return lock;
    }

    // public static void main( String[] args )
    // throws FeatureStoreException {
    // ConnectionManager.addConnection( "LOCK_DB", DatabaseType.UNDEFINED, "jdbc:derby:/tmp/lockdb;create=true", null,
    // null, 0, 10 );
    //
    // LockManager manager = new DefaultLockManager( "LOCK_DB" );
    // Lock lock = manager.acquireLock( new QName( "http://www.deegree.org/app", "Philosopher" ), null, false );
    // System.out.println( lock );
    //
    // CloseableIterator<String> fidIter = lock.getLockedFeatures();
    // try {
    // while ( fidIter.hasNext() ) {
    // String fid = fidIter.next();
    // System.out.println( fid );
    // }
    // } finally {
    // fidIter.close();
    // }
    // }

    private void close( ResultSet rs, Statement stmt, Connection conn ) {
        if ( rs != null ) {
            try {
                rs.close();
            } catch ( SQLException e ) {
                LOG.error( "Unable to close ResultSet: " + e.getMessage() );
            }
        }
        if ( stmt != null ) {
            try {
                stmt.close();
            } catch ( SQLException e ) {
                LOG.error( "Unable to close Statement: " + e.getMessage() );
            }
        }
        if ( conn != null ) {
            try {
                conn.close();
            } catch ( SQLException e ) {
                LOG.error( "Unable to close Connection: " + e.getMessage() );
            }
        }
    }

    @Override
    public boolean isFeatureLocked( String fid )
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isFeatureModifiable( String fid, String lockId )
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void releaseLock( Lock lock )
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        
    }    
}
