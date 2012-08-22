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
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.deegree.framework.util.ObjectPool;

/**
 * class to manage a pool of database connections.
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class DBPool extends ObjectPool {

    private String driver = null;

    private String database = null;

    private Properties properties = new Properties();

    /**
     * initialize pool for defined connection parameters
     * 
     * @param driver
     * @param database
     * @param user
     * @param password
     */
    public DBPool( final String driver, final String database, final String user, final String password ) {

        this.driver = driver;
        this.database = database;
        properties.put( "user", user );
        properties.put( "password", password );
    }

    /**
     * initialize pool for defined connection parameters
     * 
     * @param driver
     * @param database
     * @param properties
     */
    public DBPool( final String driver, final String database, final Properties properties ) {

        this.driver = driver;
        this.database = database;
        this.properties = properties;
    }

    /**
     * get an object from the object pool
     * 
     * @return the object
     * 
     * @throws DBPoolException
     */
    public synchronized Object acquireObject()
                            throws DBPoolException {
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
                if ( ( (Connection) o ).isClosed() ) {
                    startLifeTime.remove( o );
                    o = acquireObject();
                }

                // add it to 'in use' container
                if ( !in_use.contains( o ) )
                    in_use.add( o );

                // reset its start life time
                startLifeTime.put( o, new Long( System.currentTimeMillis() ) );
                // set the start of its usage
                startUsageTime.put( o, new Long( System.currentTimeMillis() ) );

                // return the object
                return o;

            }
            // else instatiate a new object
            // create a new class instance
            DriverManager.registerDriver( (Driver) Class.forName( driver ).newInstance() );

            Properties prop = (Properties) properties.clone();
            Object connection = DriverManager.getConnection( database, prop );

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
            throw new DBPoolException( "Error while acquiring connection: " + e.getMessage(), e );
        }
    }

    /**
     * will be called when the object is removed from the pool
     * 
     * @param o
     */
    @Override
    public void onObjectKill( Object o ) {
        try {
            ( (Connection) o ).close();
        } catch ( SQLException e ) {
            // which is a bad thing
        }
    }

}
