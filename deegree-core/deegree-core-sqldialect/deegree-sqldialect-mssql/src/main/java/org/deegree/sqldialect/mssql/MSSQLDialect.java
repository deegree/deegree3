/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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
package org.deegree.sqldialect.mssql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.sql.DefaultPrimitiveConverter;
import org.deegree.commons.tom.sql.PrimitiveParticleConverter;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.cs.CRSUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.utils.GeometryParticleConverter;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.SortCriterion;
import org.deegree.sqldialect.filter.AbstractWhereBuilder;
import org.deegree.sqldialect.filter.PropertyNameMapper;
import org.deegree.sqldialect.filter.UnmappableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.deegree.sqldialect.AbstractSQLDialect;

/**
 * {@link SQLDialect} for Microsoft SQL Server databases.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class MSSQLDialect extends AbstractSQLDialect implements SQLDialect {

	private static Logger LOG = LoggerFactory.getLogger(MSSQLDialect.class);

	private final char escapeChar = 0;

	@Override
	public int getMaxColumnNameLength() {
		return 128;
	}

	@Override
	public int getMaxTableNameLength() {
		return 128;
	}

	public String getDefaultSchema() {
		return "dbo";
	}

	public String stringPlus() {
		return "+";
	}

	public String stringIndex(String pattern, String string) {
		return "CHARINDEX(" + pattern + "," + string + ")";
	}

	public String cast(String expr, String type) {
		return "CAST(" + expr + " AS " + type + ")";
	}

	@Override
	public String geometryMetadata(TableName qTable, String column, boolean isGeography) {
		// TODO no way to get more out of this "database"?
		return "SELECT 2,-1,'GEOMETRY'";
	}

	@Override
	public AbstractWhereBuilder getWhereBuilder(PropertyNameMapper mapper, OperatorFilter filter,
			SortProperty[] sortCrit, List<SortCriterion> defaultSortCriteria, boolean allowPartialMappings)
			throws UnmappableException, FilterEvaluationException {
		return new MSSQLWhereBuilder(this, mapper, filter, sortCrit, defaultSortCriteria, allowPartialMappings);
	}

	@Override
	public String getUndefinedSrid() {
		return "0";
	}

	@Override
	public String getBBoxAggregateSnippet(String colummn) {
		return "1";
	}

	@Override
	public Envelope getBBoxAggregateValue(ResultSet rs, int colIdx, ICRS crs) {
		return new GeometryFactory().createEnvelope(-180, -90, 180, 90, CRSUtils.EPSG_4326);
	}

	@Override
	public GeometryParticleConverter getGeometryConverter(String column, ICRS crs, String srid, boolean is2d) {
		return new MSSQLGeometryConverter(column, crs, srid, is2d);
	}

	@Override
	public PrimitiveParticleConverter getPrimitiveConverter(String column, PrimitiveType pt) {
		return new DefaultPrimitiveConverter(pt, column);
	}

	@Override
	public void createDB(Connection adminConn, String dbName) throws SQLException {

		String sql = "CREATE DATABASE " + dbName;

		Statement stmt = null;
		try {
			stmt = adminConn.createStatement();
			stmt.executeUpdate(sql);
		}
		finally {
			JDBCUtils.close(null, stmt, null, LOG);
		}
	}

	@Override
	public void dropDB(Connection adminConn, String dbName) throws SQLException {

		String sql = "DROP DATABASE " + dbName;
		Statement stmt = null;
		try {
			stmt = adminConn.createStatement();
			stmt.executeUpdate(sql);
		}
		finally {
			JDBCUtils.close(null, stmt, null, LOG);
		}
	}

	@Override
	public void createAutoColumn(StringBuffer currentStmt, List<StringBuffer> additionalSmts, SQLIdentifier column,
			SQLIdentifier table) {
		currentStmt.append(column);
		currentStmt.append(" integer IDENTITY(1,1)");
	}

	@Override
	public ResultSet getTableColumnMetadata(DatabaseMetaData md, TableName qTable) throws SQLException {
		String schema = qTable.getSchema() != null ? qTable.getSchema() : getDefaultSchema();
		String table = qTable.getTable();
		return md.getColumns(null, schema.toLowerCase(), table.toLowerCase(), null);
	}

	@Override
	public boolean requiresTransactionForCursorMode() {
		return false;
	}

	@Override
	public String getSelectSequenceNextVal(String sequence) {
		throw new UnsupportedOperationException(
				"Using DB sequences for FIDs is currently not supported on Microsoft SQL Server.");
	}

}
