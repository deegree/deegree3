//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-metadata/src/test/java/org/deegree/metadata/iso/persistence/AbstractISOTest.java $
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
package org.deegree.metadata.iso.persistence;

import static org.deegree.commons.xml.CommonNamespaces.OWS_NS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.commons.jdbc.param.DefaultJDBCParams;
import org.deegree.commons.jdbc.param.JDBCParams;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.test.TestProperties;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.protocol.csw.MetadataStoreException;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31239 $, $Date: 2011-07-07 17:23:05 +0200 (Do, 07. Jul 2011) $
 */
public abstract class AbstractISOTest {

    private static final Logger LOG = getLogger( AbstractISOTest.class );

    protected static final NamespaceBindings nsContext = CommonNamespaces.getNamespaceContext();

    protected ISOMetadataStore store;

    protected String jdbcURL;

    protected String jdbcUser;

    protected String jdbcPass;

    protected MetadataResultSet<?> resultSet;

    protected Connection conn;

    static {
        nsContext.addNamespace( "ows", OWS_NS );
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
                            throws Exception {
        jdbcURL = TestProperties.getProperty( "iso_store_url" );
        jdbcUser = TestProperties.getProperty( "iso_store_user" );
        jdbcPass = TestProperties.getProperty( "iso_store_pass" );

        DeegreeWorkspace workspace = DeegreeWorkspace.getInstance();
        workspace.initManagers();

        ConnectionManager mgr = workspace.getSubsystemManager( ConnectionManager.class );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            if ( mgr.getState( "iso_pg_set_up_tables" ) != null ) {
                // skip new creation of the connection
                // Connection connDeleteTables = null;
                try {
                    conn = mgr.get( "iso_pg_set_up_tables" );

                    deleteFromTables( conn );
                } finally {
                    JDBCUtils.close( conn );
                }

            } else {
                JDBCParams params = new DefaultJDBCParams( jdbcURL, jdbcUser, jdbcPass, false );
                mgr.addPool( "iso_pg_set_up_tables", params, workspace );
                // Connection connSetUpTables = null;

                try {
                    conn = mgr.get( "iso_pg_set_up_tables" );

                    setUpTables( conn );

                } finally {
                    JDBCUtils.close( conn );

                }
            }
        }
        workspace.initAll();
    }

    private void setUpTables( Connection conn )
                            throws SQLException, UnsupportedEncodingException, IOException, MetadataStoreException {

        Statement stmt = null;
        try {
            stmt = conn.createStatement();

            for ( String sql : new ISOMetadataStoreProvider().getDropStatements( Type.PostgreSQL ) ) {
                try {
                    stmt.executeUpdate( sql );
                } catch ( Exception e ) {
                    // TODO: handle exception
                    System.out.println( e.getMessage() );
                }
            }

            for ( String sql : new ISOMetadataStoreProvider().getCreateStatements( Type.PostgreSQL ) ) {

                stmt.execute( sql );
            }

        } finally {
            if ( stmt != null ) {
                stmt.close();
            }
        }
    }

    @After
    public void tearDown()
                            throws SQLException, UnsupportedEncodingException, IOException, MetadataStoreException {
        if ( resultSet != null ) {
            LOG.info( "------------------" );
            LOG.info( "Tear down the test" );
            LOG.info( "------------------" );
            resultSet.close();
        } else {
            if ( conn != null && conn.isClosed() ) {
                LOG.info( "------------------" );
                LOG.info( "no closing of resultSet possible..." );
                LOG.info( "so close the jdbcConnection at least!" );
                LOG.info( "------------------" );
                JDBCUtils.close( conn );
            }

        }
    }

    private void deleteFromTables( Connection conn )
                            throws SQLException, UnsupportedEncodingException, IOException {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            String sql = "DELETE from idxtb_main;";
            stmt.executeUpdate( sql );

        } finally {
            if ( stmt != null ) {
                stmt.close();
            }
        }
    }

}
