//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.commons.jdbc;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.commons.i18n.Messages;
import org.deegree.commons.jdbc.jaxb.PooledConnection;
import org.deegree.commons.utils.TempFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for accessing JDBC connections in deegree that are defined in JDBC configuration files.
 * <p>
 * Configuration of JDBC connections used in deegree is based on simple string identifiers: each configured JDBC
 * connection has a unique identifier. This class allows the retrieval of connections based on their identifier.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class ConnectionManager {

    private static Logger LOG = LoggerFactory.getLogger( ConnectionManager.class );

    private static Map<String, ConnectionPool> idToPools = new HashMap<String, ConnectionPool>();

    // TODO find a better solution then this static connection
    private static ConnectionPool derbyConn;

    static {
        String lockDb = new File( TempFileManager.getBaseDir(), "lockdb" ).getAbsolutePath();
        LOG.info( "Using '" + lockDb + "' for derby lock database." );

        try {
            Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" ).newInstance();
        } catch ( Exception e ) {
            LOG.error( "Error loading derby JDBC driver: " + e.getMessage(), e );
        }
        derbyConn = getConnection( "LOCK_DB", "jdbc:derby:" + lockDb + ";create=true", null, null, 0, 10 );
        idToPools.put( "LOCK_DB", derbyConn );
    }

    /**
     * Initializes the {@link ConnectionManager} by loading all JDBC pool configurations from the given directory.
     * 
     * @param jdbcDir
     */
    public static void init( File jdbcDir ) {

        idToPools.put( "LOCK_DB", derbyConn );

        if ( !jdbcDir.exists() ) {
            LOG.info( "No 'jdbc' directory -- skipping initialization of JDBC connection pools." );
            return;
        }
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Setting up JDBC connection pools." );
        LOG.info( "--------------------------------------------------------------------------------" );
        File[] fsConfigFiles = jdbcDir.listFiles( new FilenameFilter() {
            @Override
            public boolean accept( File dir, String name ) {
                return name.toLowerCase().endsWith( ".xml" );
            }
        } );
        for ( File fsConfigFile : fsConfigFiles ) {
            String fileName = fsConfigFile.getName();
            // 4 is the length of ".xml"
            String fsId = fileName.substring( 0, fileName.length() - 4 );
            LOG.info( "Setting up JDBC connection '" + fsId + "' from file '" + fileName + "'..." + "" );
            try {
                addConnection( fsConfigFile.toURI().toURL(), fsId );
            } catch ( Exception e ) {
                LOG.error( "Error initializing JDBC connection pool: " + e.getMessage(), e );
            }
        }
        LOG.info( "" );
    }

    /**
     * 
     */
    public static void destroy() {
        // try {
        // DriverManager.getConnection( "jdbc:derby:;shutdown=true" );
        // } catch ( SQLException e ) {
        // LOG.debug( "Exception caught shutting down derby databases: " + e.getMessage(), e );
        // }
        // TODO remove the LOCK_DB
        for ( String id : idToPools.keySet() ) {
            if ( !id.equals( "LOCK_DB" ) ) {
                try {
                    idToPools.get( id ).destroy();
                } catch ( Exception e ) {
                    LOG.debug( "Exception caught shutting down connection pool: " + e.getMessage(), e );
                }
            }
        }
        idToPools.clear();
    }

    /**
     * Returns a connection from the connection pool with the given id.
     * 
     * @param id
     *            id of the connection pool
     * @return connection from the corresponding connection pool
     * @throws SQLException
     *             if the connection pool is unknown or a SQLException occurs creating the connection
     */
    public static Connection getConnection( String id )
                            throws SQLException {
        ConnectionPool pool = idToPools.get( id );
        if ( pool == null ) {
            throw new SQLException( Messages.getMessage( "JDBC_UNKNOWN_CONNECTION", id ) );
        }
        return pool.getConnection();
    }

    /**
     * Adds the connection pool defined in the given file.
     * 
     * @param jdbcConfigUrl
     * @param connId
     * @throws JAXBException
     */
    public static void addConnection( URL jdbcConfigUrl, String connId )
                            throws JAXBException {
        synchronized ( ConnectionManager.class ) {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.commons.jdbc.jaxb" );
            Unmarshaller u = jc.createUnmarshaller();
            addConnection( (PooledConnection) u.unmarshal( jdbcConfigUrl ), connId );
        }
    }

    /**
     * Adds a connection pool from the given pool definition.
     * 
     * @param jaxbConn
     * @param connId
     */
    public static void addConnection( PooledConnection jaxbConn, String connId ) {
        synchronized ( ConnectionManager.class ) {
            String url = jaxbConn.getUrl();

            String user = jaxbConn.getUser();
            String password = jaxbConn.getPassword();
            int poolMinSize = jaxbConn.getPoolMinSize().intValue();
            int poolMaxSize = jaxbConn.getPoolMaxSize().intValue();
            boolean readOnly = jaxbConn.isReadOnly() == null ? false : jaxbConn.isReadOnly();

            LOG.debug( Messages.getMessage( "JDBC_SETTING_UP_CONNECTION_POOL", connId, url, user, poolMinSize,
                                            poolMaxSize ) );
            if ( idToPools.containsKey( connId ) ) {
                throw new IllegalArgumentException( Messages.getMessage( "JDBC_DUPLICATE_ID", connId ) );
            }

            ConnectionPool pool = new ConnectionPool( connId, url, user, password, readOnly, poolMinSize, poolMaxSize );
            idToPools.put( connId, pool );
        }
    }

    /**
     * Adds a connection pool as specified in the parameters.
     * 
     * @param connId
     * @param url
     * @param user
     * @param password
     * @param poolMinSize
     * @param poolMaxSize
     */
    public static void addConnection( String connId, String url, String user, String password, int poolMinSize,
                                      int poolMaxSize ) {

        synchronized ( ConnectionManager.class ) {
            LOG.debug( Messages.getMessage( "JDBC_SETTING_UP_CONNECTION_POOL", connId, url, user, poolMinSize,
                                            poolMaxSize ) );
            if ( idToPools.containsKey( connId ) ) {
                throw new IllegalArgumentException( Messages.getMessage( "JDBC_DUPLICATE_ID", connId ) );
            }
            // TODO check callers for read only flag
            ConnectionPool pool = new ConnectionPool( connId, url, user, password, false, poolMinSize, poolMaxSize );
            idToPools.put( connId, pool );
        }
    }

    /**
     * @return all currently available connection ids
     */
    public static Set<String> getConnectionIds() {
        return idToPools.keySet();
    }

    /**
     * Adds a connection pool as specified in the parameters.
     * 
     * @param connId
     * @param url
     * @param user
     * @param password
     * @param poolMinSize
     * @param poolMaxSize
     */
    private static ConnectionPool getConnection( String connId, String url, String user, String password,
                                                 int poolMinSize, int poolMaxSize ) {

        ConnectionPool pool = null;
        synchronized ( ConnectionManager.class ) {
            LOG.debug( Messages.getMessage( "JDBC_SETTING_UP_CONNECTION_POOL", connId, url, user, poolMinSize,
                                            poolMaxSize ) );
            if ( idToPools.containsKey( connId ) ) {
                throw new IllegalArgumentException( Messages.getMessage( "JDBC_DUPLICATE_ID", connId ) );
            }
            // TODO check callers for read only flag
            pool = new ConnectionPool( connId, url, user, password, false, poolMinSize, poolMaxSize );

        }
        return pool;
    }
}
