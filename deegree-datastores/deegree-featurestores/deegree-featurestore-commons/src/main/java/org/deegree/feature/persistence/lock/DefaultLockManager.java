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

import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.commons.tom.datetime.ISO8601Converter.formatDateTime;
import static org.deegree.commons.utils.JDBCUtils.close;
import static org.deegree.feature.i18n.Messages.getMessage;
import static org.h2.engine.Constants.SCHEMA_MAIN;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.deegree.commons.jdbc.ResultSetIterator;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.utils.CloseableIterator;
import org.deegree.db.ConnectionProvider;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.Query;
import org.deegree.filter.FilterEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link LockManager} implementation that is based on an SQL database.
 * <p>
 * TODO Currently this class is only tested with Derby 10, h2, but it should be easy to make it work with PostGIS,
 * Oracle and other SQL DBs.
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

    private ConnectionProvider connection;

    /**
     * Creates a new {@link DefaultLockManager} for the given {@link FeatureStore}.
     * 
     * @param store
     * @param jdbcConnId
     * @throws FeatureStoreException
     *             if the initialization of the locking backend fails
     */
    public DefaultLockManager( FeatureStore store, ConnectionProvider connection ) throws FeatureStoreException {
        this.store = store;
        this.connection = connection;
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

        LOG.debug( "Initializing lock database." );
        Connection conn = null;
        ResultSet rs = null;
        Statement stmt = null;
        try {
            conn = connection.getConnection();
            DatabaseMetaData dbMetaData = conn.getMetaData();
            rs = dbMetaData.getTables( null, SCHEMA_MAIN, "LOCKS", new String[] { "TABLE" } );
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

            rs = dbMetaData.getTables( null, SCHEMA_MAIN, "LOCKED_FIDS", new String[] { "TABLE" } );
            if ( !rs.next() ) {
                LOG.debug( "Creating table 'LOCKED_FIDS'." );
                if ( stmt == null ) {
                    stmt = conn.createStatement();
                }
                String sql = "CREATE TABLE LOCKED_FIDS (";
                sql += "LOCK_ID INT REFERENCES LOCKS,";
                sql += "FID VARCHAR(255) NOT NULL UNIQUE";
                sql += ")";
                stmt.execute( sql );
            } else {
                LOG.debug( "Table 'LOCKED_FIDS' already exists." );
            }
            rs.close();

            rs = dbMetaData.getTables( null, SCHEMA_MAIN, "LOCK_FAILED_FIDS", new String[] { "TABLE" } );
            if ( !rs.next() ) {
                LOG.debug( "Creating table 'LOCK_FAILED_FIDS'." );
                if ( stmt == null ) {
                    stmt = conn.createStatement();
                }
                String sql = "CREATE TABLE LOCK_FAILED_FIDS (";
                sql += "LOCK_ID INT REFERENCES LOCKS,";
                sql += "FID VARCHAR(255) NOT NULL";
                sql += ")";
                stmt.execute( sql );
            } else {
                LOG.debug( "Table 'LOCK_FAILED_FIDS' already exists." );
            }
            rs.close();
        } catch ( SQLException e ) {
            String msg = Messages.getMessage( "LOCK_DB_CREATE_ERROR", e.getMessage() );
            LOG.error( msg, e );
            throw new FeatureStoreException( msg, e );
        } finally {
            close( rs, stmt, conn, LOG );
        }
    }

    @Override
    public Lock acquireLock( List<Query> queries, boolean mustLockAll, long expireTimeout )
                            throws FeatureStoreException, OWSException {

        Lock lock = null;

        synchronized ( this ) {
            releaseExpiredLocks();

            Connection conn = null;
            PreparedStatement insertLockstmt = null;
            PreparedStatement checkStmt = null;
            PreparedStatement lockedStmt = null;
            PreparedStatement failedToLockStmt = null;
            ResultSet rs = null;

            try {
                conn = connection.getConnection();
                conn.setAutoCommit( false );

                // create entry in LOCKS table
                insertLockstmt = conn.prepareStatement( "INSERT INTO LOCKS (ACQUIRED,EXPIRES) VALUES (?,?)",
                                                        Statement.RETURN_GENERATED_KEYS );
                Date acquired = new Date();
                Date expires = new Date( acquired.getTime() + expireTimeout );
                insertLockstmt.setTimestamp( 1, new Timestamp( acquired.getTime() ) );
                insertLockstmt.setTimestamp( 2, new Timestamp( expires.getTime() ) );
                insertLockstmt.execute();

                rs = insertLockstmt.getGeneratedKeys();
                rs.next();
                int lockId = rs.getInt( 1 );
                rs.close();
                rs = null;

                int numLocked = 0;
                int numFailed = 0;

                for ( Query query : queries ) {
                    FeatureCollection fc = store.query( query ).toCollection();

                    // create entries in LOCKED_FIDS/LOCK_FAILED_FIDS tables
                    checkStmt = conn.prepareStatement( "SELECT LOCK_ID FROM LOCKED_FIDS WHERE FID=?" );
                    lockedStmt = conn.prepareStatement( "INSERT INTO LOCKED_FIDS (LOCK_ID, FID) VALUES (?,?)" );
                    failedToLockStmt = conn.prepareStatement( "INSERT INTO LOCK_FAILED_FIDS (LOCK_ID, FID) VALUES (?,?)" );

                    for ( Feature feature : fc ) {
                        String fid = feature.getId();
                        int currentLockId = -1;

                        // check if feature is locked already
                        checkStmt.setString( 1, fid );
                        rs = checkStmt.executeQuery();
                        if ( rs.next() ) {
                            currentLockId = rs.getInt( 1 );
                        }
                        rs.close();

                        rs = null;
                        if ( currentLockId != lockId ) {
                            if ( currentLockId != -1 ) {
                                if ( mustLockAll ) {
                                    conn.rollback();
                                    checkStmt = conn.prepareStatement( "SELECT A.ACQUIRED,A.EXPIRES FROM LOCKS A INNER JOIN LOCKED_FIDS B ON A.ID=B.LOCK_ID WHERE B.FID=?" );
                                    checkStmt.setString( 1, fid );
                                    rs = checkStmt.executeQuery();
                                    rs.next();
                                    Timestamp acquired2 = rs.getTimestamp( 1 );
                                    Timestamp expires2 = rs.getTimestamp( 2 );
                                    rs.close();
                                    String msg = getMessage( "LOCK_CANNOT_LOCK_ALL", fid,
                                                             formatDateTime( new DateTime( expires2, null ) ),
                                                             formatDateTime( new DateTime( acquired2, null ) ) );
                                    throw new OWSException( msg, NO_APPLICABLE_CODE );
                                }
                                failedToLockStmt.setInt( 1, lockId );
                                failedToLockStmt.setString( 2, fid );
                                failedToLockStmt.execute();
                                numFailed++;
                            } else {
                                lockedStmt.setInt( 1, lockId );
                                lockedStmt.setString( 2, fid );
                                lockedStmt.execute();
                                numLocked++;
                            }
                        }
                    }
                }
                conn.commit();
                lock = new DefaultLock( this, connection, "" + lockId, acquired, expires, numLocked, numFailed );
            } catch ( SQLException e ) {
                try {
                    if ( conn != null ) {
                        conn.rollback();
                    }
                } catch ( SQLException e1 ) {
                    LOG.warn( "Error performing rollback on lock db: " + e.getMessage(), e );
                }
                throw new FeatureStoreException( e.getMessage(), e );
            } catch ( FilterEvaluationException e ) {
                LOG.debug( "Stack trace:", e );
                throw new FeatureStoreException( e );
            } finally {
                try {
                    if ( conn != null ) {
                        conn.setAutoCommit( true );
                    }
                } catch ( SQLException e ) {
                    LOG.warn( "Error resetting auto commit on lock db connection: " + e.getMessage(), e );
                }
                close( rs, insertLockstmt, conn, LOG );
                if ( checkStmt != null ) {
                    try {
                        checkStmt.close();
                    } catch ( SQLException e ) {
                        LOG.error( "Unable to close Statement: " + e.getMessage() );
                    }
                }
                if ( lockedStmt != null ) {
                    try {
                        lockedStmt.close();
                    } catch ( SQLException e ) {
                        LOG.error( "Unable to close Statement: " + e.getMessage() );
                    }
                }
                if ( failedToLockStmt != null ) {
                    try {
                        failedToLockStmt.close();
                    } catch ( SQLException e ) {
                        LOG.error( "Unable to close Statement: " + e.getMessage() );
                    }
                }
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
            try {
                conn = connection.getConnection();
                LOG.debug( "Using connection: " + conn );
                stmt = conn.createStatement();
                rs = stmt.executeQuery( "SELECT ID,ACQUIRED,EXPIRES FROM LOCKS" );
                lockIter = new ResultSetIterator<Lock>( rs, conn, stmt) {
                    @Override
                    protected Lock createElement( ResultSet rs )
                                            throws SQLException {
                        String lockId = rs.getString( 1 );
                        Timestamp acquired = rs.getTimestamp( 2 );
                        Timestamp expires = rs.getTimestamp( 3 );

                        Statement stmt = rs.getStatement().getConnection().createStatement();
                        ResultSet rs2 = stmt.executeQuery( "SELECT COUNT(*) FROM LOCKED_FIDS WHERE LOCK_ID=" + lockId );
                        rs2.next();
                        int numLocked = rs2.getInt( 1 );
                        rs2 = stmt.executeQuery( "SELECT COUNT(*) FROM LOCK_FAILED_FIDS WHERE LOCK_ID=" + lockId );
                        rs2.next();
                        int numFailed = rs.getInt( 1 );
                        stmt.close();
                        return new DefaultLock( manager, connection, lockId, acquired, expires, numLocked, numFailed );
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

        int lockIdInt = -1;
        try {
            lockIdInt = Integer.parseInt( lockId );
        } catch ( NumberFormatException e ) {
            // not a number -> use -1 (which is never used)
        }

        synchronized ( this ) {
            releaseExpiredLocks();
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                conn = connection.getConnection();
                stmt = conn.createStatement();
                rs = stmt.executeQuery( "SELECT ACQUIRED,EXPIRES FROM LOCKS WHERE ID=" + lockIdInt + "" );
                if ( !rs.next() ) {
                    String msg = Messages.getMessage( "LOCK_NO_SUCH_ID", lockId );
                    throw new LockHasExpiredException( msg, "lockId" );
                }
                Timestamp acquired = rs.getTimestamp( 1 );
                Timestamp expires = rs.getTimestamp( 2 );
                rs = stmt.executeQuery( "SELECT COUNT(*) FROM LOCKED_FIDS WHERE LOCK_ID=" + lockIdInt );
                rs.next();
                int numLocked = rs.getInt( 1 );
                rs = stmt.executeQuery( "SELECT COUNT(*) FROM LOCK_FAILED_FIDS WHERE LOCK_ID=" + lockIdInt );
                rs.next();
                int numFailed = rs.getInt( 1 );
                lock = new DefaultLock( this, connection, lockId, acquired, expires, numLocked, numFailed );
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
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                conn = connection.getConnection();
                stmt = conn.prepareStatement( "SELECT COUNT(*) FROM LOCKED_FIDS WHERE FID=?" );
                stmt.setString( 1, fid );
                rs = stmt.executeQuery();
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

        int lockIdInt = -1;
        try {
            lockIdInt = Integer.parseInt( lockId );
        } catch ( NumberFormatException e ) {
            // not a number -> use -1 (which is never used)
        }

        boolean isModifiable = false;
        synchronized ( this ) {
            releaseExpiredLocks();
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                conn = connection.getConnection();
                stmt = conn.prepareStatement( "SELECT COUNT(*) FROM LOCKED_FIDS WHERE FID=? AND LOCK_ID<>?" );
                stmt.setString( 1, fid );
                stmt.setInt( 2, lockIdInt );
                rs = stmt.executeQuery();
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
                conn = connection.getConnection();

                stmt = conn.prepareStatement( "DELETE FROM LOCKED_FIDS WHERE LOCK_ID IN (SELECT ID FROM LOCKS WHERE EXPIRES <=?)" );
                stmt.setTimestamp( 1, now );
                int deleted = stmt.executeUpdate();
                LOG.debug( "Deleted " + deleted + " row(s) from table LOCKED_FIDS." );
                stmt.close();

                stmt = conn.prepareStatement( "DELETE FROM LOCK_FAILED_FIDS WHERE LOCK_ID IN (SELECT ID FROM LOCKS WHERE EXPIRES <=?)" );
                stmt.setTimestamp( 1, now );
                deleted = stmt.executeUpdate();
                LOG.debug( "Deleted " + deleted + " row(s) from table LOCK_FAILED_FIDS." );
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
