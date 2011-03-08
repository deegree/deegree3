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

import static java.sql.DriverManager.registerDriver;
import static org.deegree.commons.config.ResourceState.StateType.init_ok;
import static org.deegree.commons.jdbc.ConnectionManager.Type.H2;
import static org.deegree.commons.jdbc.ConnectionManager.Type.MSSQL;
import static org.deegree.commons.jdbc.ConnectionManager.Type.Oracle;
import static org.deegree.commons.jdbc.ConnectionManager.Type.PostgreSQL;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.deegree.commons.annotations.ConsoleManaged;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceManagerMetadata;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.i18n.Messages;
import org.deegree.commons.jdbc.jaxb.JDBCConnection;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.commons.xml.jaxb.JAXBUtils;
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
public class ConnectionManager implements ResourceManager, ResourceProvider {

    private static Logger LOG = LoggerFactory.getLogger( ConnectionManager.class );

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.commons.jdbc.jaxb";

    private static final String CONFIG_SCHEMA = "/META-INF/schemas/jdbc/3.0.0/jdbc.xsd";

    private static Map<String, ConnectionPool> idToPools = new HashMap<String, ConnectionPool>();

    private static Map<String, Type> idToType = new HashMap<String, Type>();

    public static enum Type {
        PostgreSQL, MSSQL, Oracle, H2
    }

    /**
     * Initializes the {@link ConnectionManager} by loading all JDBC pool configurations from the given directory.
     * 
     * @param jdbcDir
     */
    public void init( File jdbcDir, DeegreeWorkspace workspace ) {
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
                addConnection( fsConfigFile.toURI().toURL(), fsId, workspace );
            } catch ( Exception e ) {
                LOG.error( "Error initializing JDBC connection pool: " + e.getMessage(), e );
            }
        }
        LOG.info( "" );
    }

    public static void destroy( String connid ) {
        try {
            idToPools.remove( connid ).destroy();
        } catch ( Exception e ) {
            LOG.debug( "Exception caught shutting down connection pool: " + e.getMessage(), e );
        }
    }

    /**
     * @param id
     * @return the type of the connection, null if the connection is unknown or the connection type could not be
     *         determined
     */
    public static Type getType( String id ) {
        return idToType.get( id );
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
     * @param workspace
     *            can be <code>null</code>
     * @throws JAXBException
     */
    public static void addConnection( URL jdbcConfigUrl, String connId, DeegreeWorkspace workspace )
                            throws JAXBException {
        synchronized ( ConnectionManager.class ) {
            JDBCConnection pc = (JDBCConnection) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA,
                                                                       jdbcConfigUrl, workspace );
            addConnection( pc, connId );
        }
    }

    private static void checkType( String url, String connId ) {
        if ( url.startsWith( "jdbc:postgresql:" ) ) {
            idToType.put( connId, PostgreSQL );
        }
        if ( url.startsWith( "jdbc:h2:" ) ) {
            idToType.put( connId, H2 );
        }
        if ( url.startsWith( "jdbc:oracle:" ) ) {
            idToType.put( connId, Oracle );
        }
        if ( url.startsWith( "jdbc:sqlserver:" ) ) {
            idToType.put( connId, MSSQL );
        }
    }

    /**
     * Adds a connection pool from the given pool definition.
     * 
     * @param jaxbConn
     * @param connId
     */
    public static void addConnection( JDBCConnection jaxbConn, String connId ) {
        synchronized ( ConnectionManager.class ) {
            String url = jaxbConn.getUrl();

            checkType( url, connId );

            String user = jaxbConn.getUser();
            String password = jaxbConn.getPassword();
            // TODO move this params
            int poolMinSize = 5;
            int poolMaxSize = 25;
            boolean readOnly = jaxbConn.isReadOnly() != null ? jaxbConn.isReadOnly() : false;

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
            checkType( url, connId );
            idToPools.put( connId, pool );
        }
    }

    /**
     * @return all currently available connection ids
     */
    public static Set<String> getConnectionIds() {
        return idToPools.keySet();
    }

    public void shutdown() {
        for ( String id : idToPools.keySet() ) {
            try {
                idToPools.get( id ).destroy();
            } catch ( Exception e ) {
                LOG.debug( "Exception caught shutting down connection pool: " + e.getMessage(), e );
            }
        }
        idToPools.clear();
    }

    public void startup( DeegreeWorkspace workspace ) {
        try {
            for ( Driver d : ServiceLoader.load( Driver.class, workspace.getModuleClassLoader() ) ) {
                registerDriver( new DriverWrapper( d ) );
                LOG.info( "Found and loaded {}", d.getClass().getName() );
            }
        } catch ( SQLException e ) {
            LOG.debug( "Unable to load MSSQL driver: {}", e.getLocalizedMessage() );
        }
        init( new File( workspace.getLocation(), "jdbc" ), workspace );
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { ProxyUtils.class };
    }

    @ConsoleManaged(startPage = "/console/jdbc/buttons")
    class ConnectionManagerMetadata implements ResourceManagerMetadata {
        public String getName() {
            return "jdbc";
        }

        public String getPath() {
            return "jdbc";
        }

        public List<ResourceProvider> getResourceProviders() {
            return Collections.singletonList( (ResourceProvider) ConnectionManager.this );
        }
    }

    public ResourceManagerMetadata getMetadata() {
        return new ConnectionManagerMetadata();
    }

    public String getConfigNamespace() {
        return "http://www.deegree.org/jdbc";
    }

    public URL getConfigSchema() {
        return ConnectionManager.class.getResource( CONFIG_SCHEMA );
    }

    @Override
    public ResourceState getState( String id ) {
        if ( idToPools.get( id ) != null ) {
            return new ResourceState( init_ok, null );
        }
        // TODO
        return null;
    }
}