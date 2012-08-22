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
package org.deegree.io.sdeapi;

import java.util.HashMap;
import java.util.Map;

import org.deegree.io.DBPoolException;

/**
 * class to manage a pool of sde connections.
 * 
 * @author <a href="mailto:cpollmann@moss.de">Christoph Pollmann</a>
 * @version 2.0
 */
public class SDEConnectionPool {

    private static volatile SDEConnectionPool instance = null;

    private Map<String, SDEPool> pools = null;

    /**
     * Creates a new SDEConnectionPool object.
     */
    private SDEConnectionPool() {
        pools = new HashMap<String, SDEPool>();
    }

    /**
     * realize singelton pattern using double checked locking pattern.
     * 
     * @return an instance of the data base pool. it is gauranteed that there exists only one
     *         instance of pool for each submitted class name.
     * 
     */
    public static SDEConnectionPool getInstance() {
        if ( instance == null ) {
            synchronized ( SDEConnectionPool.class ) {
                if ( instance == null ) {
                    instance = new SDEConnectionPool();
                }
            }
        }

        return instance;
    }

    /**
     * get an object from the object pool
     * 
     * @param server
     * @param instance
     * @param database
     * @param version
     * @param user
     * @param password
     * @return the connection
     * @throws Exception
     */
    public synchronized SDEConnection acquireConnection( final String server, final int instance,
                                                         final String database, final String version,
                                                         final String user, final String password )
                            throws Exception {
        String q = server + instance + database + version + user;
        SDEPool pool = pools.get( q );
        if ( pool == null ) {
            pool = new SDEPool( server, instance, database, version, user, password );
            pools.put( q, pool );
        }
        return (SDEConnection) pool.acquireObject();
    }

    /**
     * releases a connection back to the pool
     * 
     * @param con
     * @param server
     * @param instance
     * @param database
     * @param version
     * @param user
     * @throws DBPoolException
     */
    public synchronized void releaseConnection( final SDEConnection con, final String server, final int instance,
                                                final String database, final String version, final String user )
                            throws DBPoolException {
        String q = server + instance + database + version + user;
        SDEPool pool = pools.get( q );
        try {
            pool.releaseObject( con );
        } catch ( Exception e ) {
            throw new DBPoolException( "could not release connection", e );
        }
    }
}