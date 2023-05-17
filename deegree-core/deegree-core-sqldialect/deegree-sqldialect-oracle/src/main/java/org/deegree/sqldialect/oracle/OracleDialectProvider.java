/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

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
package org.deegree.sqldialect.oracle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deegree.commons.utils.JDBCUtils;
import org.deegree.db.dialect.SqlDialectProvider;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.workspace.ResourceInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SqlDialectProvider} for Oracle spatial databases.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public class OracleDialectProvider implements SqlDialectProvider {

	private static Logger LOG = LoggerFactory.getLogger(OracleDialectProvider.class);

	@Override
	public boolean supportsConnection(Connection connection) {
		String url = null;
		try {
			url = connection.getMetaData().getURL();
		}
		catch (Exception e) {
			LOG.debug("Could not determine metadata/url of connection: {}", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
			return false;
		}
		return url.startsWith("jdbc:oracle:");
	}

	@Override
	public SQLDialect createDialect(Connection conn) {
		String schema = null;
		Statement stmt = null;
		ResultSet rs = null;

		// default to 10.0
		int major = 10;
		int minor = 0;

		try {
			stmt = conn.createStatement();

			// this function / parameters exists since oracle version 8
			rs = stmt.executeQuery("SELECT sys_context('USERENV', 'CURRENT_SCHEMA') FROM DUAL");

			if (rs.next())
				schema = rs.getString(1);

			JDBCUtils.close(rs);

			// tested with oracle 9, 10, 11
			rs = stmt.executeQuery("SELECT version FROM product_component_version WHERE product LIKE 'Oracle%'");

			if (rs.next()) {
				Pattern p = Pattern.compile("^(\\d+)\\.(\\d+)\\.");
				Matcher m = p.matcher(rs.getString(1));
				if (m.find()) {
					major = Integer.valueOf(m.group(1));
					minor = Integer.valueOf(m.group(2));
				}
			}

			LOG.info("Instantiating Oracle dialect for version {}.{}", major, minor);
		}
		catch (SQLException se) {
			LOG.warn("Failed loading default schema/database version for connection.");
			LOG.trace(se.getMessage(), se);
			throw new ResourceInitException(se.getMessage(), se);
		}
		finally {
			JDBCUtils.close(rs, stmt, conn, null);
		}

		return new OracleDialect(schema, major, minor);
	}

}
