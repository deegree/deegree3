/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.metadata.persistence.ebrim.eo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.utils.test.TestProperties;
import org.deegree.protocol.csw.MetadataStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 */
public class Helper {

	private static Logger LOG = LoggerFactory.getLogger(Helper.class);

	public static void setUpTables(Connection conn)
			throws SQLException, UnsupportedEncodingException, IOException, MetadataStoreException {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			URL url = EbrimEOTransactionTest.class.getResource("postgis/drop.sql");
			List<String> stms = new ArrayList<String>();
			stms.addAll(readStatements(new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))));
			for (String sql : stms) {
				try {
					stmt.executeUpdate(sql);
				}
				catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
			stms.clear();
			url = EbrimEOTransactionTest.class.getResource("postgis/create.sql");
			stms.addAll(readStatements(new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))));
			for (String sql : stms) {
				stmt.execute(sql);
			}
		}
		finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	private static List<String> readStatements(BufferedReader reader) throws IOException {
		List<String> stmts = new ArrayList<String>();
		String currentStmt = "";
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("--") || line.trim().isEmpty()) {
				// skip
			}
			else if (line.contains(";")) {
				currentStmt += line.substring(0, line.indexOf(';'));
				stmts.add(currentStmt);
				currentStmt = "";
			}
			else {
				currentStmt += line + "\n";
			}
		}
		return stmts;
	}

	// TODO: extrenalize connection configuration
	public static Connection getConnection() throws SQLException {
		String jdbcUrl = TestProperties.getProperty("deegree-mdstore-ebrim-eo.url");
		if (jdbcUrl == null) {
			LOG.info("Skipping test, property 'deegree-mdstore-ebrim-eo.url' not set.");
			return null;
		}
		String jdbcUser = TestProperties.getProperty("deegree-mdstore-ebrim-eo.user");
		String jdbcPass = TestProperties.getProperty("deegree-mdstore-ebrim-eo.pass");
		Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass);
		conn.setAutoCommit(false);
		return conn;
	}

}