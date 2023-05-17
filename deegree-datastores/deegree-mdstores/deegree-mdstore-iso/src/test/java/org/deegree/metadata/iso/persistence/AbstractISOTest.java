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
import static org.deegree.db.ConnectionProviderUtils.getSyntheticProvider;
import static org.deegree.workspace.WorkspaceUtils.activateFromUrl;
import static org.junit.Assume.assumeNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.deegree.commons.utils.test.TestProperties;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.metadata.persistence.MetadataStoreProvider;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public abstract class AbstractISOTest {

	private static final Logger LOG = getLogger(AbstractISOTest.class);

	protected static final NamespaceBindings nsContext = CommonNamespaces.getNamespaceContext();

	protected ISOMetadataStore store;

	protected String jdbcURL;

	protected String jdbcUser;

	protected String jdbcPass;

	protected MetadataResultSet<?> resultSet;

	protected Workspace workspace;

	static {
		nsContext.addNamespace("ows", OWS_NS);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		jdbcURL = TestProperties.getProperty("iso_store_url");
		jdbcUser = TestProperties.getProperty("iso_store_user");
		jdbcPass = TestProperties.getProperty("iso_store_pass");

		workspace = new DefaultWorkspace(new File("/tmp/"));
		workspace.startup();
		workspace.getLocationHandler()
			.addExtraResource(getSyntheticProvider("iso_pg_set_up_tables", jdbcURL, jdbcUser, jdbcPass));
		workspace.initAll();
		ConnectionProvider prov = workspace.getResource(ConnectionProviderProvider.class, "iso_pg_set_up_tables");

		assumeNotNull(prov);

		Connection conn = prov.getConnection();
		try {
			setUpTables(conn);
		}
		catch (Exception e) {
			// ignore
		}
		try {
			deleteFromTables(conn);
		}
		catch (Exception e) {
			// ignore
		}

		conn.close();
	}

	private void setUpTables(Connection conn)
			throws SQLException, UnsupportedEncodingException, IOException, MetadataStoreException {

		ConnectionProvider prov = workspace.getResource(ConnectionProviderProvider.class, "iso_pg_set_up_tables");

		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			for (String sql : new ISOMetadataStoreProvider().getDropStatements(prov.getDialect())) {
				try {
					stmt.executeUpdate(sql);
				}
				catch (Exception e) {
					// TODO: handle exception
					System.out.println(e.getMessage());
				}
			}

			for (String sql : new ISOMetadataStoreProvider().getCreateStatements(prov.getDialect())) {
				stmt.execute(sql);
			}

			conn.commit();
		}
		finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	@After
	public void tearDown() throws SQLException, UnsupportedEncodingException, IOException, MetadataStoreException {
		if (resultSet != null) {
			LOG.info("------------------");
			LOG.info("Tear down the test");
			LOG.info("------------------");
			resultSet.close();
		}
		workspace.destroy();
	}

	private void deleteFromTables(Connection conn) throws SQLException, UnsupportedEncodingException, IOException {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String sql = "DELETE from idxtb_main;";
			stmt.executeUpdate(sql);

		}
		finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	protected void initStore(URL url) {
		if (workspace.getResource(ConnectionProviderProvider.class, "iso_pg_set_up_tables") != null) {
			store = (ISOMetadataStore) activateFromUrl(workspace, MetadataStoreProvider.class, "id", url);
		}
	}

}
