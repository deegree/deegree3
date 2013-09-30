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
package org.deegree.commons.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import org.deegree.commons.concurrent.Executor;
import org.deegree.commons.jdbc.ConnectionManager;
import org.slf4j.Logger;

/**
 * This class contains static utility methods for working with JDBC.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public final class JDBCUtils {

    public static void log( SQLException e, Logger log ) {
        while ( e != null ) {
            log.debug( e.getMessage(), e );
            e = e.getNextException();
        }
    }

    public static String getMessage( SQLException e ) {
        StringBuffer sb = new StringBuffer();
        while ( e != null ) {
            sb.append( e.getMessage() );
            e = e.getNextException();
        }
        return sb.toString();
    }

    /**
     * Close the object, suppress all errors/exceptions. Useful for finally clauses.
     * 
     * @param conn
     */
    public static void close( Connection conn ) {
        try {
            conn.close();
        } catch ( Exception ex ) {
            //
        }
    }

    /**
     * Close the object, suppress all errors/exceptions. Useful for finally clauses.
     * 
     * @param resultSet
     */
    public static void close( ResultSet resultSet ) {
        try {
            resultSet.close();
        } catch ( Exception ex ) {
            //
        }
    }

    /**
     * Close the object, suppress all errors/exceptions. Useful for finally clauses.
     * 
     * @param stmt
     */
    public static void close( Statement stmt ) {
        if ( stmt != null ) {
            try {
                stmt.close();
            } catch ( Exception ex ) {
                //
            }
        }
    }

    /**
     * Tries to close each object from a <code>ResultSet</code>, <code>Statement</code>, <code>Connection</code> triple.
     * Useful for cleaning up in <code>finally</code> clauses.
     * 
     * @param rs
     *            <code>ResultSet</code> to be closed
     * @param stmt
     *            <code>Statement</code> to be closed
     * @param conn
     *            <code>Connection</code> to be closed
     * @param log
     *            used to log error messages, may be null
     */
    public static void close( ResultSet rs, Statement stmt, Connection conn, Logger log ) {
        if ( rs != null ) {
            try {
                rs.close();
            } catch ( SQLException e ) {
                if ( log != null ) {
                    log.error( "Unable to close ResultSet: " + e.getMessage() );
                }
            }
        }
        if ( stmt != null ) {
            try {
                stmt.close();
            } catch ( SQLException e ) {
                if ( log != null ) {
                    log.error( "Unable to close Statement: " + e.getMessage() );
                }
            }
        }
        if ( conn != null ) {
            try {
                conn.close();
            } catch ( SQLException e ) {
                if ( log != null ) {
                    log.error( "Unable to close Connection: " + e.getMessage() );
                }
            }
        }
    }

    public static String determinePostGISVersion( Connection conn, Logger log ) {
        String version = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery( "SELECT postgis_version()" );
            rs.next();
            String postGISVersion = rs.getString( 1 );
            version = postGISVersion.split( " " )[0];
            log.debug( "PostGIS version: {}", version );
        } catch ( Throwable t ) {
            log.warn( "Could not determine PostGIS version." );
        }
        return version;
    }

    public static boolean useLegayPostGISPredicates( Connection conn, Logger log ) {
        boolean useLegacyPredicates = false;
        String version = determinePostGISVersion( conn, log );
        if ( version.startsWith( "0." ) || version.startsWith( "1.0" ) || version.startsWith( "1.1" )
             || version.startsWith( "1.2" ) ) {
            log.debug( "PostGIS version is " + version + " -- using legacy (pre-SQL-MM) predicates." );
            useLegacyPredicates = true;
        } else {
            log.debug( "PostGIS version is " + version + " -- using modern (SQL-MM) predicates." );
        }
        return useLegacyPredicates;
    }

    /**
     * Executes the SQL query in the given {@link PreparedStatement} object and returns the ResultSet object generated
     * by the query.
     * 
     * @param stmt
     *            statement to be executed, must not be <code>null</code>
     * @param connManager
     *            {@link ConnectionManager} that has been used to retrieve the connection (needed for invalidating
     *            cancelled queries), must not be <code>null</code>
     * @param connId
     *            id of the connection pool that the connection belongs to (needed for invalidating cancelled queries),
     *            must not be <code>null</code>
     * @param executionTimeout
     *            timeout in milliseconds, if 0 or negative, timeout is disabled
     * @return a <code>ResultSet</code> object that contains the data produced by the query, never <code>null</code>
     * @throws SQLException
     *             if an execution timeout or a database access error occurs; this method is called on a closed
     *             <code>PreparedStatement</code> or the SQL statement does not return a <code>ResultSet</code> object
     */
    public static ResultSet executeQuery( final PreparedStatement stmt, ConnectionManager connManager, String connId,
                                          long executionTimeout )
                            throws SQLException {
        if ( executionTimeout <= 0 ) {
            return stmt.executeQuery();
        }
        try {
            return Executor.getInstance().performSynchronously( new Callable<ResultSet>() {
                @Override
                public ResultSet call()
                                        throws Exception {
                    return stmt.executeQuery();
                }
            }, executionTimeout );
        } catch ( CancellationException e ) {
            stmt.cancel();
            Connection conn = stmt.getConnection();
            String msg = "Database query has been cancelled, because query execution timeout (" + executionTimeout
                         + "[ms]) has been exceeded.";
            try {
                // This is necessary, because cancelled connections appear not to be reusable, so errors occur if a
                // cancelled connection is returned to the pool and re-used (at least on PostgreSQL)
                connManager.invalidate( connId, conn );
            } catch ( Throwable t ) {
                msg += "Invalidation of connection failed: " + t.getMessage();
            }
            throw new SQLException( msg );
        } catch ( InterruptedException e ) {
            String msg = "Interruption during database query: " + e.getMessage();
            throw new SQLException( msg, e );
        } catch ( SQLException e ) {
            throw e;
        } catch ( Throwable e ) {
            String msg = "Error executing query: " + e.getMessage();
            throw new SQLException( msg, e );
        }
    }

    public static void rollbackQuietly( Connection conn ) {
        if ( conn != null ) {
            try {
                conn.rollback();
            } catch ( SQLException e ) {
                // nothing to do
            }
        }
    }
}