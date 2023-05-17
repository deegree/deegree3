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
package org.deegree.commons.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;

/**
 * This class contains static utility methods for working with JDBC.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 */
public final class JDBCUtils {

	public static void log(SQLException e, Logger log) {
		while (e != null) {
			log.debug(e.getMessage(), e);
			e = e.getNextException();
		}
	}

	public static String getMessage(SQLException e) {
		StringBuffer sb = new StringBuffer();
		while (e != null) {
			sb.append(e.getMessage());
			e = e.getNextException();
		}
		return sb.toString();
	}

	/**
	 * Close the object, suppress all errors/exceptions. Useful for finally clauses.
	 * @param conn
	 */
	public static void close(Connection conn) {
		try {
			conn.close();
		}
		catch (Exception ex) {
			//
		}
	}

	/**
	 * Close the object, suppress all errors/exceptions. Useful for finally clauses.
	 * @param resultSet
	 */
	public static void close(ResultSet resultSet) {
		try {
			resultSet.close();
		}
		catch (Exception ex) {
			//
		}
	}

	/**
	 * Close the object, suppress all errors/exceptions. Useful for finally clauses.
	 * @param stmt
	 */
	public static void close(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			}
			catch (Exception ex) {
				//
			}
		}
	}

	/**
	 * Tries to close each object from a <code>ResultSet</code>, <code>Statement</code>,
	 * <code>Connection</code> triple. Useful for cleaning up in <code>finally</code>
	 * clauses.
	 * @param rs <code>ResultSet</code> to be closed
	 * @param stmt <code>Statement</code> to be closed
	 * @param conn <code>Connection</code> to be closed
	 * @param log used to log error messages, may be null
	 */
	public static void close(ResultSet rs, Statement stmt, Connection conn, Logger log) {
		if (rs != null) {
			try {
				rs.close();
			}
			catch (SQLException e) {
				if (log != null) {
					log.error("Unable to close ResultSet: " + e.getMessage());
				}
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			}
			catch (SQLException e) {
				if (log != null) {
					log.error("Unable to close Statement: " + e.getMessage());
				}
			}
		}
		if (conn != null) {
			try {
				conn.close();
			}
			catch (SQLException e) {
				if (log != null) {
					log.error("Unable to close Connection: " + e.getMessage());
				}
			}
		}
	}

	public static String determinePostGISVersion(Connection conn, Logger log) {
		String version = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT postgis_version()");
			rs.next();
			String postGISVersion = rs.getString(1);
			version = postGISVersion.split(" ")[0];
			log.debug("PostGIS version: {}", version);
		}
		catch (Throwable t) {
			log.warn("Could not determine PostGIS version.");
		}
		return version;
	}

	public static boolean useLegayPostGISPredicates(Connection conn, Logger log) {
		boolean useLegacyPredicates = false;
		String version = determinePostGISVersion(conn, log);
		if (version.startsWith("0.") || version.startsWith("1.0") || version.startsWith("1.1")
				|| version.startsWith("1.2")) {
			log.debug("PostGIS version is " + version + " -- using legacy (pre-SQL-MM) predicates.");
			useLegacyPredicates = true;
		}
		else {
			log.debug("PostGIS version is " + version + " -- using modern (SQL-MM) predicates.");
		}
		return useLegacyPredicates;
	}

	public static void rollbackQuietly(Connection conn) {
		if (conn != null) {
			try {
				conn.rollback();
			}
			catch (SQLException e) {
				// nothing to do
			}
		}
	}

}