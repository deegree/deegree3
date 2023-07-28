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
package org.deegree.tools.rendering.r2d.se;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.db.ConnectionProviderUtils;
import org.deegree.style.se.parser.PostgreSQLReader;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.tools.i18n.Messages;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.slf4j.Logger;

/**
 * <code>StyleChecker</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
@Tool(value = "This tool can be used to check and remove faulty styles in a WMS style database (PostgreSQL).")
public class StyleChecker {

	private static final Logger LOG = getLogger(StyleChecker.class);

	private static HashSet<Integer> faultyStyles = new HashSet<Integer>();

	private static ConnectionProvider connProvider;

	private static Options initOptions() {
		Options opts = new Options();

		Option opt = new Option("d", "dburl", true, "database url, like jdbc:postgresql://localhost/dbname");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option("u", "dbuser", true, "database user, like postgres");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option("p", "dbpassword", true, "database password, if left off, will be set as empty");
		opt.setRequired(false);
		opts.addOption(opt);

		opt = new Option("s", "schema", true, "table schema, if left off, public will be used");
		opt.setRequired(false);
		opts.addOption(opt);

		opt = new Option("c", "clean", false,
				"if set, faulty styles will be deleted (currently only in the styles table)");
		opt.setRequired(false);
		opts.addOption(opt);

		CommandUtils.addDefaultOptions(opts);

		return opts;

	}

	private static void check(String schema) {
		PostgreSQLReader reader = new PostgreSQLReader(connProvider, schema, null);

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean bad = false;
		try {
			conn = connProvider.getConnection();
			stmt = conn.prepareStatement("select id from " + schema + ".styles");
			rs = stmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				Style s = reader.getStyle(id);
				if (s == null) {
					bad = true;
					LOG.info("Style with id {} could not be loaded.", id);
					faultyStyles.add(id);
				}
			}
			if (bad) {
				LOG.info("Some styles could not be loaded.");
			}
			else {
				LOG.info("Style DB is fine.");
			}
		}
		catch (SQLException e) {
			LOG.info("DB connection error: {}", e);
			LOG.trace("Stack trace:", e);
		}
		finally {
			if (rs != null) {
				try {
					rs.close();
				}
				catch (SQLException e) {
					LOG.info("DB connection error: {}", e);
					LOG.trace("Stack trace:", e);
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					LOG.info("DB connection error: {}", e);
					LOG.trace("Stack trace:", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				}
				catch (SQLException e) {
					LOG.info("DB connection error: {}", e);
					LOG.trace("Stack trace:", e);
				}
			}
		}
	}

	/**
	 *
	 */
	public static void clean(String schema) {
		if (faultyStyles.isEmpty()) {
			return;
		}

		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = connProvider.getConnection();
			StringBuilder sql = new StringBuilder("delete from " + schema + ".styles where id in (");
			for (int i = 0; i < faultyStyles.size() - 1; ++i) {
				sql.append("?, ");
			}
			sql.append("?)");
			stmt = conn.prepareStatement(sql.toString());

			int i = 1;
			for (Integer id : faultyStyles) {
				stmt.setInt(i++, id);
			}

			int res = stmt.executeUpdate();
			LOG.info("{} styles have been deleted.", res);
		}
		catch (SQLException e) {
			LOG.info("DB connection error: {}", e);
			LOG.trace("Stack trace:", e);
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					LOG.info("DB connection error: {}", e);
					LOG.trace("Stack trace:", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				}
				catch (SQLException e) {
					LOG.info("DB connection error: {}", e);
					LOG.trace("Stack trace:", e);
				}
			}
		}
	}

	/**
	 * @param args
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 */
	public static void main(String[] args) throws XMLStreamException, FactoryConfigurationError, IOException {
		Options options = initOptions();

		// for the moment, using the CLI API there is no way to respond to a help
		// argument; see
		// https://issues.apache.org/jira/browse/CLI-179
		if (args.length == 0 || (args.length > 0 && (args[0].contains("help") || args[0].contains("?")))) {
			CommandUtils.printHelp(options, StyleChecker.class.getSimpleName(), null, null);
		}

		try {
			CommandLine line = new PosixParser().parse(options, args);

			String url = line.getOptionValue("dburl");
			String user = line.getOptionValue("dbuser");
			String pass = line.getOptionValue("dbpassword");
			if (pass == null) {
				pass = "";
			}
			String schema = options.getOption("schema").getValue();
			if (schema == null) {
				schema = "public";
			}

			File file = File.createTempFile("deegree", "workspace");
			file.delete();
			file.mkdir();
			Workspace workspace = new DefaultWorkspace(file);
			workspace.getLocationHandler()
				.addExtraResource(ConnectionProviderUtils.getSyntheticProvider("style", url, user, pass));
			workspace.initAll();
			connProvider = workspace.getResource(ConnectionProviderProvider.class, "style");

			check(schema);
			if (line.hasOption("clean")) {
				clean(schema);
			}

			file.delete();
			workspace.destroy();
		}
		catch (ParseException exp) {
			System.err.println(Messages.getMessage("TOOL_COMMANDLINE_ERROR", exp.getMessage()));
			CommandUtils.printHelp(options, StyleChecker.class.getSimpleName(), null, null);
		}

	}

}
