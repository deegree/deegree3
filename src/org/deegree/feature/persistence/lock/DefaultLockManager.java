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

import static org.deegree.commons.utils.JDBCUtils.close;

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

    /**
     * Returns the associated {@link FeatureStore}.
     * 
     * @return the associated store, never null
     */
    FeatureStore getStore() {
        return store;
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
            close( rs, stmt, conn, LOG );
        }
    }

    @Override
    public Lock acquireLock( QName ftName, Filter filter, boolean mustLockAll, long expireTimeout )
                            throws FeatureStoreException {

        Lock lock = null;

        synchronized ( this ) {
            releaseExpiredLocks();
            Query query = new FilterQuery( ftName, null, null, filter );
            // TODO don't actually fetch the feature collection, but only the fids of the features
            FeatureCollection fc = store.performQuery( query );

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                conn = ConnectionManager.getConnection( jdbcConnId );
                conn.setAutoCommit( false );

                // create entry in LOCKS table
                stmt = conn.prepareStatement( "INSERT INTO LOCKS (ACQUIRED,EXPIRES) VALUES (?,?)",
                                              Statement.RETURN_GENERATED_KEYS );
                Date acquired = new Date();
                Date expires = new Date( acquired.getTime() + expireTimeout );
                stmt.setTimestamp( 1, new Timestamp( acquired.getTime() ) );
                stmt.setTimestamp( 2, new Timestamp( expires.getTime() ) );
                stmt.execute();

                rs = stmt.getGeneratedKeys();
                rs.next();
                int lockId = rs.getInt( 1 );
                rs.close();
                rs = null;
                lock = new DefaultLock( this, jdbcConnId, "" + lockId, acquired, expires );

                // create entries in LOCKED_FIDS table
                PreparedStatement checkStmt = conn.prepareStatement( "SELECT COUNT(LOCK_ID) FROM LOCKED_FIDS WHERE FID=?" );
                stmt = conn.prepareStatement( "INSERT INTO LOCKED_FIDS (LOCK_ID, FID) VALUES (?,?)" );

                for ( Feature feature : fc ) {
                    String fid = feature.getId();

                    // check if feature is locked already
                    checkStmt.setString( 1, fid );
                    rs = checkStmt.executeQuery();
                    rs.next();
                    int count = rs.getInt( 1 );
                    rs.close();
                    rs = null;
                    if ( count > 0 ) {
                        if ( mustLockAll ) {
                            conn.rollback();
                            checkStmt = conn.prepareStatement( "SELECT A.ACQUIRED,A.EXPIRES FROM LOCKS A INNER JOIN LOCKED_FIDS B ON A.ID=B.LOCK_ID WHERE B.FID=?" );
                            checkStmt.setString( 1, fid );
                            rs = checkStmt.executeQuery();
                            rs.next();
                            Timestamp acquired2 = rs.getTimestamp( 1 );
                            Timestamp expires2 = rs.getTimestamp( 2 );
                            rs.close();
                            String msg = Messages.getMessage( "LOCK_CANNOT_LOCK_ALL", fid, expires2, acquired2 );
                            throw new FeatureStoreException( msg );
                        }
                    } else {
                        stmt.setInt( 1, lockId );
                        stmt.setString( 2, fid );
                        stmt.execute();
                    }
                }
                conn.commit();
            } catch ( SQLException e ) {
                try {
                    conn.rollback();
                } catch ( SQLException e1 ) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                throw new FeatureStoreException( e.getMessage(), e );
            } finally {
                try {
                    conn.setAutoCommit( true );
                } catch ( SQLException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                close( rs, stmt, conn, LOG );
            }
        }
        return lock;
    }

    @Override
    public CloseableIterator<Lock> getActiveLocks() {

        CloseableIterator<Lock> lockIter = null;

        synchronized ( this ) {
            releaseExpiredLocks();
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            final DefaultLockManager manager = this;
            final String jdbcConnId = this.jdbcConnId;
            try {
                conn = ConnectionManager.getConnection( jdbcConnId );
                LOG.debug( "Using connection: " + conn );
                stmt = conn.createStatement();
                rs = stmt.executeQuery( "SELECT ID,ACQUIRED,EXPIRES FROM LOCKS" );
                lockIter = new ResultSetIterator<Lock>( rs, conn, stmt ) {
                    @Override
                    protected Lock createElement( ResultSet rs )
                                            throws SQLException {
                        return new DefaultLock( manager, jdbcConnId, rs.getString( 1 ), rs.getTimestamp( 2 ),
                                                rs.getTimestamp( 3 ) );
                    }
                };
            } catch ( SQLException e ) {
                close( rs, stmt, conn, LOG );
                String msg = "Could not retrieve active locks: " + e.getMessage();
                LOG.debug( msg, e );
                throw new RuntimeException( msg, e );
            }
        }
        return lockIter;
    }

    @Override
    public Lock getLock( String lockId )
                            throws FeatureStoreException {

        Lock lock = null;
        synchronized ( this ) {
            releaseExpiredLocks();
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                conn = ConnectionManager.getConnection( jdbcConnId );
                stmt = conn.createStatement();
                rs = stmt.executeQuery( "SELECT ACQUIRED,EXPIRES FROM LOCKS WHERE ID=" + lockId + "" );
                if ( !rs.next() ) {
                    String msg = Messages.getMessage( "LOCK_NO_SUCH_ID", lockId );
                    throw new FeatureStoreException( msg );
                }
                lock = new DefaultLock( this, jdbcConnId, lockId, rs.getTimestamp( 1 ), rs.getTimestamp( 2 ) );
            } catch ( SQLException e ) {
                String msg = "Could not retrieve lock with id '" + lockId + "':" + e.getMessage();
                LOG.debug( msg, e );
                throw new RuntimeException( msg, e );
            } finally {
                close( rs, stmt, conn, LOG );
            }
        }
        return lock;
    }

    @Override
    public boolean isFeatureLocked( String fid )
                            throws FeatureStoreException {

        boolean isLocked = false;
        synchronized ( this ) {
            releaseExpiredLocks();
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                conn = ConnectionManager.getConnection( jdbcConnId );
                stmt = conn.createStatement();
                rs = stmt.executeQuery( "SELECT COUNT(*) FROM LOCKED_FIDS WHERE FID='" + fid + "'" );
                rs.next();
                int count = rs.getInt( 1 );
                isLocked = count > 0;
            } catch ( SQLException e ) {
                String msg = "Could not retrieve active locks: " + e.getMessage();
                LOG.debug( msg, e );
                throw new RuntimeException( msg, e );
            } finally {
                close( rs, stmt, conn, LOG );
            }
        }
        return isLocked;
    }

    @Override
    public boolean isFeatureModifiable( String fid, String lockId )
                            throws FeatureStoreException {
        if ( lockId == null ) {
            return !isFeatureLocked( fid );
        }
        boolean isModifiable = false;
        synchronized ( this ) {
            releaseExpiredLocks();
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                conn = ConnectionManager.getConnection( jdbcConnId );
                stmt = conn.createStatement();
                rs = stmt.executeQuery( "SELECT COUNT(*) FROM LOCKED_FIDS WHERE FID='" + fid + "' AND LOCK_ID<>"
                                        + lockId );
                rs.next();
                int count = rs.getInt( 1 );
                isModifiable = count == 0;
            } catch ( SQLException e ) {
                String msg = "Could not retrieve active locks: " + e.getMessage();
                LOG.debug( msg, e );
                throw new RuntimeException( msg, e );
            } finally {
                close( rs, stmt, conn, LOG );
            }
        }
        return isModifiable;
    }

    /**
     * Checks for and releases all expired locks.
     */
    void releaseExpiredLocks() {

        Timestamp now = new Timestamp( new Date().getTime() );
        LOG.debug( "Checking for and removing all locks expired until '" + now + "'" );
        synchronized ( this ) {
            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                conn = ConnectionManager.getConnection( jdbcConnId );
                stmt = conn.prepareStatement( "DELETE FROM LOCKED_FIDS WHERE LOCK_ID IN (SELECT ID FROM LOCKS WHERE EXPIRES <=?)" );
                stmt.setTimestamp( 1, now );
                int deleted = stmt.executeUpdate();
                LOG.debug( "Deleted " + deleted + " row(s) from table LOCKED_FIDS." );

                stmt.close();
                stmt = conn.prepareStatement( "DELETE FROM LOCKS WHERE EXPIRES <=?" );
                stmt.setTimestamp( 1, now );
                deleted = stmt.executeUpdate();
                LOG.debug( "Deleted " + deleted + " row(s) from table LOCKS." );
            } catch ( SQLException e ) {
                String msg = "Could not determine expired locks: " + e.getMessage();
                LOG.debug( msg, e );
                throw new RuntimeException( msg, e );
            } finally {
                close( null, stmt, conn, LOG );
            }
        }
    }
}
