/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.feature.persistence.simplesql;

import static java.sql.Types.BIGINT;
import static java.sql.Types.BINARY;
import static java.sql.Types.BIT;
import static java.sql.Types.BLOB;
import static java.sql.Types.CHAR;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.INTEGER;
import static java.sql.Types.LONGVARBINARY;
import static java.sql.Types.NUMERIC;
import static java.sql.Types.OTHER;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.VARCHAR;
import static org.deegree.commons.tom.primitive.BaseType.BOOLEAN;
import static org.deegree.commons.tom.primitive.BaseType.DECIMAL;
import static org.deegree.commons.tom.primitive.BaseType.STRING;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2_OR_3;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.GEOMETRY;
import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.db.ConnectionProvider;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.io.WKTWriter;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class DbFeatureUtils {

	private static final Logger LOG = getLogger(DbFeatureUtils.class);

	private static final GeometryFactory fac = new GeometryFactory();

	/**
	 * @param ftName
	 * @param prov
	 * @param sql
	 * @return null, if an SQL error occurred
	 */
	public static GenericFeatureType determineFeatureType(QName ftName, ConnectionProvider prov, String sql) {
		Connection conn = null;
		ResultSet set = null;
		PreparedStatement stmt = null;
		try {
			conn = prov.getConnection();
			boolean isOracle = conn.getMetaData().getDriverName().contains("Oracle");
			stmt = conn.prepareStatement(sql + (isOracle ? "" : " limit 0"));
			stmt.setString(1, WKTWriter.write(fac.createEnvelope(0, 0, 1, 1, null)));
			LOG.debug("Determining feature type using query '{}'.", isOracle ? sql : stmt);
			stmt.execute();
			set = stmt.getResultSet();
			ResultSetMetaData md = set.getMetaData();
			LinkedList<PropertyType> ps = new LinkedList<PropertyType>();
			for (int i = 1; i <= md.getColumnCount(); ++i) {

				String name = md.getColumnLabel(i);
				PropertyType pt;
				int colType = md.getColumnType(i);

				QName ptName = new QName(ftName.getNamespaceURI(), name, ftName.getPrefix());
				switch (colType) {
					case VARCHAR:
					case CHAR:
						pt = new SimplePropertyType(ptName, 0, 1, STRING, null, null);
						break;
					case INTEGER:
					case SMALLINT:
						pt = new SimplePropertyType(ptName, 0, 1, BaseType.INTEGER, null, null);
						break;
					case BIT:
						pt = new SimplePropertyType(ptName, 0, 1, BOOLEAN, null, null);
						break;
					case NUMERIC:
					case DOUBLE:
					case BIGINT:
					case Types.DECIMAL:
						pt = new SimplePropertyType(ptName, 0, 1, DECIMAL, null, null);
						break;
					case OTHER:
					case BINARY:
					case BLOB:
					case LONGVARBINARY:
						pt = new GeometryPropertyType(ptName, 0, 1, null, null, GEOMETRY, DIM_2_OR_3, null);
						break;
					default:
						LOG.error("Unsupported data type of column '{}': '{}'", name, colType);
						continue;
				}

				ps.add(pt);
			}

			return new GenericFeatureType(ftName, ps, false);
		}
		catch (SQLException e) {
			LOG.info("A DB error occurred: '{}'.", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
			return null;
		}
		finally {
			JDBCUtils.close(set);
			JDBCUtils.close(stmt);
			JDBCUtils.close(conn);
		}
	}

	/**
	 * @param prov
	 * @return a list of all schemas with geometry tables
	 */
	public static LinkedList<String> fetchGeometrySchemas(ConnectionProvider prov, Workspace workspace) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet set = null;
		LinkedList<String> result = new LinkedList<String>();
		try {
			conn = prov.getConnection();
			try {
				// make this configurable via argument?
				stmt = conn.prepareStatement("select probe_geometry_columns()");
				stmt.executeQuery();
			}
			catch (SQLException e) {
				LOG.debug("Could not update the geometry_columns table: '{}'", e.getLocalizedMessage());
				LOG.trace("Stack trace:", e);
			}
			finally {
				JDBCUtils.close(stmt);
			}
			stmt = conn.prepareStatement("select distinct(f_table_schema) from geometry_columns");
			set = stmt.executeQuery();
			LOG.debug("Getting all schemas with geometry tables.");

			while (set.next()) {
				result.add(set.getString("f_table_schema"));
			}

			return result;
		}
		catch (SQLException e) {
			LOG.info("A DB error occurred: '{}'.", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
			return null;
		}
		finally {
			JDBCUtils.close(set);
			JDBCUtils.close(stmt);
			JDBCUtils.close(conn);
		}
	}

	/**
	 * @param prov
	 * @param schema
	 * @return all tables (currently only PostGIS)
	 */
	public static LinkedList<String> fetchGeometryTables(ConnectionProvider prov, String schema) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet set = null;
		LinkedList<String> result = new LinkedList<String>();
		try {
			conn = prov.getConnection();
			try {
				// make this configurable via argument?
				stmt = conn.prepareStatement("select probe_geometry_columns()");
				stmt.executeQuery();
			}
			catch (SQLException e) {
				LOG.debug("Could not update the geometry_columns table: '{}'", e.getLocalizedMessage());
				LOG.trace("Stack trace:", e);
			}
			finally {
				JDBCUtils.close(stmt);
			}
			StringBuilder query = new StringBuilder("select f_table_name from geometry_columns");
			if (schema != null) {
				query.append(" where f_table_schema = ?");
			}
			stmt = conn.prepareStatement(query.toString());
			if (schema != null) {
				stmt.setString(1, schema);
			}
			set = stmt.executeQuery();
			if (schema != null) {
				LOG.debug("Getting all geometry tables for schema '{}'.", schema);
			}
			else {
				LOG.debug("Getting all geometry tables.");
			}

			while (set.next()) {
				result.add(set.getString("f_table_name"));
			}

			return result;
		}
		catch (SQLException e) {
			LOG.info("A DB error occurred: '{}'.", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
			return null;
		}
		finally {
			JDBCUtils.close(set);
			JDBCUtils.close(stmt);
			JDBCUtils.close(conn);
		}
	}

	/**
	 * @param prov
	 * @param tableName
	 * @param tableSchema
	 * @return -2, if an error occurred (can be -1 if srid is -1 in the db, or if not
	 * found in geometry_columns metadata)
	 */
	public static int findSrid(ConnectionProvider prov, String tableName, String tableSchema) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = prov.getConnection();
			stmt = conn
				.prepareStatement("select srid from geometry_columns where f_table_name = ? and f_table_schema = ?");
			stmt.setString(1, tableName);
			stmt.setString(2, tableSchema);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
			return -1;
		}
		catch (SQLException e) {
			LOG.info("A DB error occurred: '{}'.", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
			return -2;
		}
		finally {
			JDBCUtils.close(rs);
			JDBCUtils.close(stmt);
			JDBCUtils.close(conn);
		}
	}

}
