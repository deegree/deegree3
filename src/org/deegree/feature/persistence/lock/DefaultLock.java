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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.ResultSetIterator;
import org.deegree.commons.utils.CloseableIterator;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @see DefaultLockManager
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultLock implements Lock {

    private static final Logger LOG = LoggerFactory.getLogger( DefaultLock.class );

    private String jdbcConnId;

    private String id;

    private Date acquired;

    private Date expires;

    /**
     * @param jdbcConnId
     * @param id
     * @param acquired
     * @param expires
     */
    public DefaultLock( String jdbcConnId, String id, Date acquired, Date expires ) {
        this.jdbcConnId = jdbcConnId;
        this.id = id;
        this.acquired = acquired;
        this.expires = expires;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public CloseableIterator<String> getLockedFeatures()
                            throws FeatureStoreException {
        CloseableIterator<String> fidIter = null;
        try {
            Connection conn = ConnectionManager.getConnection( jdbcConnId );
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT FID FROM LOCKED_FIDS WHERE LOCK_ID=" + id + "" );

            fidIter = new ResultSetIterator<String>( rs, conn, stmt ) {
                @Override
                protected String createElement( ResultSet rs )
                                        throws SQLException {
                    return rs.getString( 1 );
                }
            };
        } catch ( SQLException e ) {
            String msg = "Could not retrieve ids of locked features: " + e.getMessage();
            LOG.debug( msg, e );
            throw new FeatureStoreException( msg, e );
        }
        return fidIter;
    }

    @Override
    public String toString() {
        return "{id=" + id + ",acquired=" + acquired + ",expires=" + expires + "}";
    }

    @Override
    public boolean isLocked( String fid )
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void release()
                            throws FeatureStoreException {
        try {
            // delete entries from LOCK_FIDS table
            Connection conn = ConnectionManager.getConnection( jdbcConnId );
            PreparedStatement stmt = conn.prepareStatement( "DELETE FROM LOCKED_FIDS WHERE LOCK_ID=?" );
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

    @Override
    public void release( String fid )
                            throws FeatureStoreException {
        // TODO Auto-generated method stub

    }

    @Override
    public void release( QName ftName, Filter filter )
                            throws FeatureStoreException {
        // TODO Auto-generated method stub

    }
}
