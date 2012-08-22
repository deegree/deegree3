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

import org.deegree.framework.util.ObjectPool;

/**
 * class to manage a pool of sde connections.
 * 
 * 
 * @author <a href="mailto:cpollmann@moss.de">Christoph Pollmann</a>
 * @version 2.0
 */
public class SDEPool extends ObjectPool {

    private String server = null;

    private String database = null;

    private int instance = 5151;

    private String version = null;

    private String user = null;

    private String password = null;

    // private constructor to protect initializing
    /**
     * @param server
     * @param instance
     * @param database
     * @param version
     * @param user
     * @param password
     */
    public SDEPool( final String server, final int instance, final String database, final String version,
                    final String user, final String password ) {

        this.server = server;
        this.instance = instance;
        this.database = database;
        this.version = version;
        this.user = user;
        this.password = password;
    }

    /**
     * get an object from the object pool
     * 
     * @return an object from the object pool
     * @throws Exception
     */
    public synchronized Object acquireObject()
                            throws Exception {
        try {
            // if the maximum amount of instances are in use
            // wait until an instance has been released back
            // to the pool or 20 seconds has passed
            long timediff = 0;
            while ( in_use.size() == getMaxInstances() && timediff < 20000 ) {
                Thread.sleep( 100 );
                timediff += 100;
            }
            // if no instance has been released within 20 seconds
            // or can newly be instantiated return null
            if ( timediff >= 20000 )
                return null;

            // if a none used is available from the pool
            if ( available.size() > 0 ) {

                // get/remove ojebct from the pool
                Object o = available.remove( available.size() - 1 );
                if ( ( (SDEConnection) o ).isClosed() ) {
                    o = acquireObject();
                }

                // add it to 'in use' container
                in_use.add( o );

                // reset its start life time
                startLifeTime.put( o, new Long( System.currentTimeMillis() ) );
                // set the start of its usage
                startUsageTime.put( o, new Long( System.currentTimeMillis() ) );

                // return the object
                return o;

            }
            // else instatiate a new object
            Object connection = new SDEConnection( server, instance, database, version, user, password );

            existingInstances++;

            // add it to 'in use' container
            in_use.add( connection );
            // set the start of its life time
            startLifeTime.put( connection, new Long( System.currentTimeMillis() ) );
            // set the start of its usage
            startUsageTime.put( connection, new Long( System.currentTimeMillis() ) );
            // return the object
            return connection;
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new Exception( "Error while acquiring connection: " + e.getMessage(), e );
        }
    }

    /**
     * will be called when the object is removed from the pool
     */
    @Override
    public void onObjectKill( Object o ) {
        try {
            ( (SDEConnection) o ).close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

}