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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.ResultSetIterator;
import org.deegree.commons.utils.CloseableIterator;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.Query;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Lock} implementation that is based on an SQL database.
 * 
 * @see DefaultLockManager
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class DefaultLock implements Lock {

    private static final Logger LOG = LoggerFactory.getLogger( DefaultLock.class );

    private final DefaultLockManager manager;

    private final String jdbcConnId;

    private final String id;

    private final Date acquired;

    private final Date expires;

    private final int numFailed;

    private final int numLocked;

    /**
     * Creates a new {@link DefaultLock} instance.
     * 
     * @param manager
     *            corresponding {@link DefaultLockManager} instance, must not be null
     * @param jdbcConnId
     *            id of the JDBC connection, must not be null
     * @param id
     *            lock id, must not be null
     * @param acquired
     *            time that the lock has been acquired, never null
     * @param expires
     *            time that the lock will expire, never null
     * @param numLocked
     *            number of locked features
     * @param numFailed
     *            number of features that have been requested to be locked, but which couldn't
     */
    DefaultLock( DefaultLockManager manager, String jdbcConnId, String id, Date acquired, Date expires, int numLocked,
                 int numFailed ) {
        this.manager = manager;
        this.jdbcConnId = jdbcConnId;
        this.id = id;
        this.acquired = acquired;
        this.expires = expires;
        this.numLocked = numLocked;
        this.numFailed = numFailed;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getNumLocked() {
        return numLocked;
    }

    @Override
    public int getNumFailedToLock() {
        return numFailed;
    }

    @Override
    public CloseableIterator<String> getLockedFeatures()
                            throws FeatureStoreException {

        CloseableIterator<String> fidIter = null;
        synchronized ( manager ) {
            manager.releaseExpiredLocks();
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                conn = ConnectionManager.getConnection( jdbcConnId );
                stmt = conn.createStatement();
                rs = stmt.executeQuery( "SELECT FID FROM LOCKED_FIDS WHERE LOCK_ID=" + id );

                fidIter = new ResultSetIterator<String>( rs, conn, stmt ) {
                    @Override
                    protected String createElement( ResultSet rs )
                                            throws SQLException {
                        return rs.getString( 1 );
                    }
                };
            } catch ( SQLException e ) {
                close( rs, stmt, conn, LOG );
                String msg = "Could not retrieve ids of locked features: " + e.getMessage();
                LOG.debug( msg, e );
                throw new FeatureStoreException( msg, e );
            }
        }
        return fidIter;
    }

    @Override
    public CloseableIterator<String> getFailedToLockFeatures()
                            throws FeatureStoreException {
        CloseableIterator<String> fidIter = null;
        synchronized ( manager ) {
            manager.releaseExpiredLocks();
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                conn = ConnectionManager.getConnection( jdbcConnId );
                stmt = conn.createStatement();
                rs = stmt.executeQuery( "SELECT FID FROM LOCK_FAILED_FIDS WHERE LOCK_ID=" + id );

                fidIter = new ResultSetIterator<String>( rs, conn, stmt ) {
                    @Override
                    protected String createElement( ResultSet rs )
                                            throws SQLException {
                        return rs.getString( 1 );
                    }
                };
            } catch ( SQLException e ) {
                close( rs, stmt, conn, LOG );
                String msg = "Could not retrieve ids of failed to lock features: " + e.getMessage();
                LOG.debug( msg, e );
                throw new FeatureStoreException( msg, e );
            }
        }
        return fidIter;
    }

    @Override
    public boolean isLocked( String fid )
                            throws FeatureStoreException {
        boolean isLocked = false;
        synchronized ( manager ) {
            manager.releaseExpiredLocks();
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                conn = ConnectionManager.getConnection( jdbcConnId );
                stmt = conn.createStatement();
                rs = stmt.executeQuery( "SELECT COUNT(*) FROM LOCKED_FIDS WHERE FID='" + fid + "' AND LOCK_ID=" + id );
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
    public void release()
                            throws FeatureStoreException {
        synchronized ( manager ) {
            try {
                // delete entries from LOCKED_FIDS table
                Connection conn = ConnectionManager.getConnection( jdbcConnId );
                PreparedStatement stmt = conn.prepareStatement( "DELETE FROM LOCKED_FIDS WHERE LOCK_ID=?" );
                stmt.setString( 1, id );
                stmt.execute();
                stmt.close();

                // delete entries from LOCK_FAILED_FIDS table
                stmt = conn.prepareStatement( "DELETE FROM LOCK_FAILED_FIDS WHERE LOCK_ID=?" );
                stmt.setString( 1, id );
                stmt.execute();
                stmt.close();

                // delete entry from LOCK table
                stmt = conn.prepareStatement( "DELETE FROM LOCKS WHERE ID=?" );
                stmt.setString( 1, id );
                stmt.execute();
                stmt.close();

                conn.commit();
                conn.close();
            } catch ( SQLException e ) {
                throw new FeatureStoreException( e.getMessage(), e );
            }
        }
    }

    @Override
    public void release( String fid )
                            throws FeatureStoreException {

        synchronized ( manager ) {
            if ( isLocked( fid ) ) {
                Connection conn = null;
                Statement stmt = null;
                ResultSet rs = null;
                try {
                    conn = ConnectionManager.getConnection( jdbcConnId );
                    stmt = conn.createStatement();
                    stmt.executeUpdate( "DELETE FROM LOCKED_FIDS WHERE FID='" + fid + "'" );
                } catch ( SQLException e ) {
                    String msg = "Could not release locked feature: " + e.getMessage();
                    LOG.debug( msg, e );
                    throw new RuntimeException( msg, e );
                } finally {
                    close( rs, stmt, conn, LOG );
                }
            }
        }
    }

    @Override
    public void release( QName ftName, Filter filter )
                            throws FeatureStoreException {

        synchronized ( this ) {

            Query query = new Query( new TypeName[] { new TypeName( ftName, null ) }, filter, null, null, null );

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                // TODO don't actually fetch the feature collection, but only the fids of the features
                FeatureCollection fc = manager.getStore().query( query ).toCollection();

                conn = ConnectionManager.getConnection( jdbcConnId );
                conn.setAutoCommit( false );

                // delete entries in LOCKED_FIDS table
                stmt = conn.prepareStatement( "DELETE FROM LOCKED_FIDS WHERE FID=? AND LOCK_ID=?" );

                for ( Feature feature : fc ) {
                    String fid = feature.getId();
                    stmt.setString( 1, fid );
                    stmt.setString( 2, id );
                    int deleted = stmt.executeUpdate();
                    if ( deleted != 1 ) {
                        LOG.error( "Internal error. Locked fid entry has not actually been removed from LOCKED_FIDS." );
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
            } catch ( FilterEvaluationException e ) {
                LOG.debug( "Stack trace:", e );
                throw new FeatureStoreException( e );
            } finally {
                try {
                    conn.setAutoCommit( true );
                } catch ( SQLException e ) {
                    e.printStackTrace();
                }
                close( rs, stmt, conn, LOG );
            }
        }
    }

    @Override
    public String toString() {
        return "{id=" + id + ",acquired=" + acquired + ",expires=" + expires + "}";
    }
}
