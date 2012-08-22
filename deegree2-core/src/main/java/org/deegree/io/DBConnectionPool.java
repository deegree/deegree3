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
package org.deegree.io;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * class to manage a database connection pool. this is part of the combination of the object pool pattern an the
 * singelton pattern.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @version 07.02.2001
 */
public class DBConnectionPool {

    private static DBConnectionPool instance = null;

    private Map<String, DBPool> pools = null;

    /**
     * Creates a new DBConnectionPool object.
     */
    private DBConnectionPool() {
        pools = new HashMap<String, DBPool>();
    }

    /**
     * realize singelton pattern using double checked locking pattern.
     * 
     * @return an instance of the data base pool. it is gauranteed that there exists only one instance of pool for each
     *         submitted class name.
     * 
     */
    public static DBConnectionPool getInstance() {
        if ( instance == null ) {
            synchronized ( DBConnectionPool.class ) {
                if ( instance == null ) {
                    instance = new DBConnectionPool();
                }
            }
        }

        return instance;
    }

    /**
     * @param driver
     *            driver to look for
     * @param database
     *            the database to open
     * @param user
     *            the username
     * @param password
     *            the password.
     * @return get a Connection from the Connection pool
     * @throws DBPoolException
     */
    public synchronized Connection acquireConnection( final String driver, final String database, final String user,
                                                      final String password )
                            throws DBPoolException {
        String q = driver + database + user + password;
        DBPool pool = null;
        if ( pools.get( q ) == null ) {
            pool = new DBPool( driver, database, user, password );
            pools.put( q, pool );
        } else {
            pool = pools.get( q );
        }
        return (Connection) pool.acquireObject();
    }

    /**
     * @param driver
     *            driver to look for
     * @param database
     *            the database to open
     * @param properties
     *            the properties of the database
     * @return get a Connection from the Connection pool
     * @throws DBPoolException
     */
    public synchronized Connection acquireConnection( final String driver, final String database,
                                                      final Properties properties )
                            throws DBPoolException {
        String q = driver + database + properties.toString();

        if ( pools.get( q ) == null ) {
            DBPool pool = new DBPool( driver, database, properties );
            pools.put( q, pool );
            return (Connection) pool.acquireObject();
        }
        DBPool pool = pools.get( q );
        return (Connection) pool.acquireObject();
    }

    /**
     * releases a connection back to the pool
     * 
     * @param con
     *            connections to be released
     * @param driver
     *            driver to look for
     * @param database
     *            the database to open
     * @param user
     *            the username
     * @param password
     *            the password.
     * @throws DBPoolException
     */
    public void releaseConnection( final Connection con, final String driver, final String database, final String user,
                                   final String password )
                            throws DBPoolException {
        // prevent dead locks because of stupid code trying to release null connections
        if ( con == null ) {
            return;
        }
        String q = driver + database + user + password;
        DBPool pool = pools.get( q );
        try {
            con.setAutoCommit( true );
            pool.releaseObject( con );
        } catch ( Exception e ) {
            throw new DBPoolException( "could not release connection", e );
        }
    }

    /**
     * releases a connection back to the pool
     * 
     * @param con
     *            connections to be released
     * @param driver
     *            driver to look for
     * @param database
     *            the database to open
     * @param properties
     *            containing username and password.
     * @throws Exception
     */
    public void releaseConnection( final Connection con, final String driver, final String database,
                                   final Properties properties )
                            throws Exception {
        String q = driver + database + properties.toString();
        DBPool pool = pools.get( q );
        con.setAutoCommit( true );
        pool.releaseObject( con );
    }

}
