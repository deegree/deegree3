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

import static java.sql.DriverManager.deregisterDriver;
import static java.sql.DriverManager.getDrivers;
import static org.deegree.commons.config.ResourceState.StateType.init_error;
import static org.deegree.commons.config.ResourceState.StateType.init_ok;
import static org.deegree.commons.jdbc.ConnectionManager.Type.H2;
import static org.deegree.commons.jdbc.ConnectionManager.Type.MSSQL;
import static org.deegree.commons.jdbc.ConnectionManager.Type.Oracle;
import static org.deegree.commons.jdbc.ConnectionManager.Type.PostgreSQL;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.dbcp.DelegatingConnection;
import org.deegree.commons.config.AbstractBasicResourceManager;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceManagerMetadata;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.config.ResourceState.StateType;
import org.deegree.commons.i18n.Messages;
import org.deegree.commons.jdbc.param.JDBCParams;
import org.deegree.commons.jdbc.param.JDBCParamsManager;
import org.deegree.commons.utils.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the {@link ConnectionPools} of a {@link DeegreeWorkspace}.
 * <p>
 * TODO complete separation of JDBC parameter definition ({@link JDBCParams}) and connection pooling
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class ConnectionManager extends AbstractBasicResourceManager implements ResourceProvider {

    private static Logger LOG = LoggerFactory.getLogger( ConnectionManager.class );

    private static final URL CONFIG_SCHEMA = ConnectionManager.class.getResource( "/META-INF/schemas/jdbc/3.0.0/jdbc.xsd" );

    private static Map<String, Type> idToType = new HashMap<String, Type>();

    private static Map<String, ConnectionPool> idToPools = new HashMap<String, ConnectionPool>();

    public static enum Type {
        PostgreSQL, MSSQL, Oracle, H2
    }

    /**
     * Initializes the {@link ConnectionManager} by loading all JDBC pool configurations from the given directory.
     * 
     * @param jdbcDir
     */
    @SuppressWarnings("unchecked")
    public void init( File jdbcDir, DeegreeWorkspace workspace ) {

        JDBCParamsManager paramsMgr = workspace.getSubsystemManager( JDBCParamsManager.class );
        if ( paramsMgr.getStates().length == 0 ) {
            LOG.info( "No 'jdbc' connections defined -- skipping initialization of JDBC connection pools." );
            return;
        }
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Setting up JDBC connection pools." );
        LOG.info( "--------------------------------------------------------------------------------" );
        for ( ResourceState<JDBCParams> state : paramsMgr.getStates() ) {
            if ( state.getType() == init_ok ) {
                String connId = state.getId();
                LOG.info( "Setting up JDBC connection pool for connection id '" + connId + "'..." + "" );
                try {
                    addPool( connId, state.getResource(), workspace );
                    Connection conn = get( connId );
                    if ( conn != null ) {
                        conn.close();
                    }
                    idToState.put( connId, new ResourceState( connId, state.getConfigLocation(), this, init_ok, null,
                                                              null ) );
                } catch ( Throwable t ) {
                    ResourceInitException e = new ResourceInitException( t.getMessage(), t );
                    idToState.put( connId, new ResourceState( connId, state.getConfigLocation(), this, init_error,
                                                              null, e ) );
                    LOG.error( "Error initializing JDBC connection pool: " + t.getMessage(), t );
                }
            }
        }
        LOG.info( "" );
    }

    /**
     * @param id
     * @return the type of the connection, null if the connection is unknown or the connection type could not be
     *         determined
     */
    public Type getType( String id ) {
        return idToType.get( id );
    }

    /**
     * Returns a connection from the connection pool with the given id.
     * 
     * @param id
     *            id of the connection pool
     * @return connection from the corresponding connection pool, null, if not available
     */
    public Connection get( String id ) {
        ConnectionPool pool = idToPools.get( id );
        if ( pool == null ) {
            throw new RuntimeException( "Connection not configured." );
        }
        Connection conn = null;
        try {
            conn = pool.getConnection();
            return conn;
        } catch ( SQLException e ) {
            LOG.warn( "JDBC connection {} is not available.", id );
            throw new RuntimeException( e.getLocalizedMessage(), e );
        }
    }

    /**
     * Invalidates a broken {@link Connection} to avoid its re-use.
     * 
     * @param id
     *            connection pool id, must not be <code>null</code>
     * @param conn
     *            connection, must not be <code>null</code>
     * @throws Exception
     */
    public static void invalidate( String id, Connection conn )
                            throws Exception {
        ConnectionPool pool = idToPools.get( id );
        if ( pool != null ) {
            pool.invalidate( (DelegatingConnection) conn );
        }
    }

    /**
     * Adds the connection pool defined in the given file.
     * 
     * @param connId
     * @param params
     * @param workspace
     *            can be <code>null</code>
     * @throws JAXBException
     */
    public void addPool( String connId, JDBCParams params, DeegreeWorkspace workspace ) {

        synchronized ( ConnectionManager.class ) {
            String url = params.getUrl();
            checkType( url, connId );

            String user = params.getUser();
            String password = params.getPassword();
            boolean readOnly = params.isReadOnly();

            // TODO move this params
            int poolMinSize = 5;
            int poolMaxSize = 25;

            LOG.debug( Messages.getMessage( "JDBC_SETTING_UP_CONNECTION_POOL", connId, url, user, poolMinSize,
                                            poolMaxSize ) );
            if ( idToPools.containsKey( connId ) ) {
                throw new IllegalArgumentException( Messages.getMessage( "JDBC_DUPLICATE_ID", connId ) );
            }

            ConnectionPool pool = new ConnectionPool( connId, url, user, password, readOnly, poolMinSize, poolMaxSize );
            idToPools.put( connId, pool );
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

    @Override
    public void shutdown() {
        for ( String id : idToPools.keySet() ) {
            try {
                idToPools.get( id ).destroy();
            } catch ( Exception e ) {
                LOG.debug( "Exception caught shutting down connection pool: " + e.getMessage(), e );
            }
        }
        idToPools.clear();
        try {
            Enumeration<Driver> enumer = getDrivers();
            while ( enumer.hasMoreElements() ) {
                Driver d = enumer.nextElement();
                if ( d instanceof DriverWrapper ) {
                    deregisterDriver( d );
                }
            }
        } catch ( SQLException e ) {
            LOG.debug( "Unable to deregister driver: {}", e.getLocalizedMessage() );
        }
    }

    @Override
    public void startup( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
        dir = new File( workspace.getLocation(), "jdbc" );
        init( dir, workspace );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { ProxyUtils.class, JDBCParamsManager.class };
    }

    class ConnectionManagerMetadata implements ResourceManagerMetadata {
        @Override
        public String getName() {
            return "jdbc";
        }

        @Override
        public String getPath() {
            return "jdbc";
        }

        @Override
        public List<ResourceProvider> getResourceProviders() {
            return Collections.singletonList( (ResourceProvider) ConnectionManager.this );
        }
    }

    public ResourceManagerMetadata getMetadata() {
        return new ConnectionManagerMetadata();
    }

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/jdbc";
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

    @Override
    public ResourceState deleteResource( String id ) {
        throw new UnsupportedOperationException(
                                                 "Deleting of connection pools not supported. Deleted JDBCParams resource instead." );
    }

    @Override
    protected void remove( String id ) {
        idToPools.remove( id );
        idToState.remove( id );
        idToType.remove( id );
    }

    @Override
    public ResourceState activate( String id ) {
        throw new UnsupportedOperationException(
                                                 "Activating of connection pools not supported. Activate JDBCParams resource instead." );
    }

    @Override
    public ResourceState deactivate( String id ) {
        try {
            ConnectionPool pool = idToPools.remove( id );
            if ( pool != null ) {
                pool.destroy();
            }

            ResourceState state = getState( id );
            if ( state == null ) {
                return null;
            }
            idToState.put( id, new ResourceState( id, state.getConfigLocation(), state.getProvider(),
                                                  StateType.deactivated, null, null ) );
            idToType.remove( id );
            return getState( id );
        } catch ( Exception e ) {
            LOG.error( "Error when deactivating pool: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
            return null;
        }
    }

    @Override
    protected ResourceProvider getProvider( URL file ) {
        return this;
    }
}