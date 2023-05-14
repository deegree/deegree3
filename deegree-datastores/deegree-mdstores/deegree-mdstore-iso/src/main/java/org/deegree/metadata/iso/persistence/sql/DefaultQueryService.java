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
package org.deegree.metadata.iso.persistence.sql;

import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.StringUtils;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.iso.persistence.ISOMetadataResultSet;
import org.deegree.metadata.iso.persistence.ISOPropertyNameMapper;
import org.deegree.metadata.iso.persistence.queryable.Queryable;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.filter.AbstractWhereBuilder;
import org.deegree.sqldialect.filter.UnmappableException;
import org.deegree.sqldialect.filter.expression.SQLArgument;
import org.deegree.sqldialect.postgis.PostGISDialect;
import org.slf4j.Logger;

/**
 * Executes statements that does the interaction with the underlying database. This is a
 * PostGRES implementation.
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class DefaultQueryService extends AbstractSqlHelper implements QueryService {

	private static final Logger LOG = getLogger(DefaultQueryService.class);

	/**
	 * Used to limit the fetch size for SELECT statements that potentially return a lot of
	 * rows.
	 */
	private static final int DEFAULT_FETCH_SIZE = 100;

	private static final int QUERY_TIMEOUT_SECONDS = 300;

	public DefaultQueryService(SQLDialect dialect, List<Queryable> queryables) {
		super(dialect, queryables);
	}

	@Override
	public ISOMetadataResultSet execute(MetadataQuery query, Connection conn) throws MetadataStoreException {
		List<TypedObjectNode> arguments = new ArrayList<TypedObjectNode>();
		String sql = null;
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		try {
			AbstractWhereBuilder builder = getWhereBuilder(query, conn);

			StringBuilder idSelect = getPreparedStatementDatasetIDs(builder);

			// TODO: use SQLDialect
			if (query != null && query.getStartPosition() != 1
					&& dialect.getClass().getSimpleName().equals("MSSQLDialect")) {
				String oldHeader = idSelect.toString();
				idSelect = idSelect.append(" from (").append(oldHeader);
				idSelect.append(", ROW_NUMBER() OVER (ORDER BY X1.ID) as rownum");
			}
			if (query != null && (query.getStartPosition() != 1 || query.getMaxRecords() > -1)
					&& dialect.getClass().getSimpleName().equals("OracleDialect")) {
				String oldHeader = idSelect.toString();
				idSelect = new StringBuilder();
				idSelect.append("select * from ( ");
				if (query.getStartPosition() != 1) {
					idSelect.append("select a.*, ROWNUM rnum from (");
				}
				idSelect.append(oldHeader);
			}

			getPSBody(builder, idSelect);
			if (builder.getOrderBy() != null) {
				idSelect.append(" ORDER BY ");
				idSelect.append(builder.getOrderBy().getSQL());
			}

			if (query != null && query.getStartPosition() != 1 && dialect instanceof PostGISDialect) {
				idSelect.append(" OFFSET ").append(Integer.toString(query.getStartPosition() - 1));
			}
			if (query != null && query.getStartPosition() != 1
					&& dialect.getClass().getSimpleName().equals("MSSQLDialect")) {
				idSelect.append(") as X1 where X1.rownum > ");
				idSelect.append(query.getStartPosition() - 1);
			}
			// take a look in the wiki before changing this!
			if (dialect instanceof PostGISDialect && query != null && query.getMaxRecords() > -1) {
				idSelect.append(" LIMIT ").append(query.getMaxRecords());
			}
			if (query != null && (query.getStartPosition() != 1 || query.getMaxRecords() > -1)
					&& dialect.getClass().getSimpleName().equals("OracleDialect")) {
				idSelect.append(" ) ");
				if (query.getStartPosition() != 1) {
					idSelect.append(" a ");
				}
				if (query.getMaxRecords() > -1) {
					int max = query.getMaxRecords() - 1;
					if (query.getStartPosition() != -1) {
						max += query.getStartPosition();
					}
					idSelect.append(" WHERE ROWNUM <= ").append(max);
				}
				if (query.getStartPosition() != 1) {
					int min = query.getStartPosition();
					idSelect.append(" ) WHERE rnum >= ").append(min);
				}
			}

			StringBuilder outerSelect = new StringBuilder("SELECT ");
			outerSelect.append(recordColumn);
			outerSelect.append(" FROM ");
			outerSelect.append(ISOPropertyNameMapper.DatabaseTables.idxtb_main);
			outerSelect.append(" A INNER JOIN (");
			outerSelect.append(idSelect);
			outerSelect.append(") B ON A.id=B.id");

			// append sort criteria in the outer again, because IN statement looses
			// ordering from inner ORDER BY
			if (builder.getOrderBy() != null) {
				outerSelect.append(" ORDER BY ");

				// check that all sort columns belong to root table
				String sortCols = builder.getOrderBy().getSQL().toString();
				String rootTableQualifier = builder.getAliasManager().getRootTableAlias() + ".";
				int columnCount = StringUtils.count(sortCols, ",") + 1;
				int rootAliasCount = StringUtils.count(sortCols, rootTableQualifier);

				if (rootAliasCount < columnCount) {
					String msg = "Sorting based on properties not stored in the root table is currently not supported.";
					throw new MetadataStoreException(msg);
				}

				String colRegEx = builder.getAliasManager().getRootTableAlias() + ".\\S+";
				for (int i = 1; i <= columnCount; i++) {
					sortCols = sortCols.replaceFirst(colRegEx, "crit" + i);
				}
				outerSelect.append(sortCols);
			}

			sql = outerSelect.toString();
			preparedStatement = createPreparedStatement(conn, sql);

			int i = 1;
			if (builder.getWhere() != null) {
				for (SQLArgument o : builder.getWhere().getArguments()) {
					o.setArgument(preparedStatement, i++);
					arguments.add(o.getValue());
				}
			}

			if (builder.getOrderBy() != null) {
				for (SQLArgument o : builder.getOrderBy().getArguments()) {
					o.setArgument(preparedStatement, i++);
					arguments.add(o.getValue());
				}
			}
			logSqlAndArguments(preparedStatement, sql, arguments);

			preparedStatement.setFetchSize(DEFAULT_FETCH_SIZE);
			rs = preparedStatement.executeQuery();
			return new ISOMetadataResultSet(rs, conn, preparedStatement);
		}
		catch (SQLException e) {
			JDBCUtils.close(rs, preparedStatement, conn, LOG);
			logSqlExceptionWithSqlAndArguments(e, sql, arguments);
			String msg = Messages.getMessage("ERROR_SQL", preparedStatement.toString(), e.getMessage());
			throw new MetadataStoreException(msg);
		}
		catch (Throwable t) {
			JDBCUtils.close(rs, preparedStatement, conn, LOG);
			String msg = Messages.getMessage("ERROR_REQUEST_TYPE", ResultType.results.name(), t.getMessage());
			LOG.debug(msg);
			throw new MetadataStoreException(msg);
		}
		finally {
			// Don't close the ResultSet or PreparedStatement if no error occurs, the
			// ResultSet is needed in the
			// ISOMetadataResultSet and both will be closed by
			// org.deegree.metadata.persistence.XMLMetadataResultSet#close().
		}
	}

	@Override
	public int executeCounting(MetadataQuery query, Connection conn)
			throws MetadataStoreException, FilterEvaluationException, UnmappableException {
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		List<TypedObjectNode> arguments = new ArrayList<TypedObjectNode>();
		String sql = null;
		try {
			AbstractWhereBuilder builder = getWhereBuilder(query, conn);
			LOG.debug("new Counting");
			StringBuilder getDatasetIDs = new StringBuilder();
			getDatasetIDs.append("SELECT ");
			getDatasetIDs.append("COUNT( DISTINCT(");
			getDatasetIDs.append(builder.getAliasManager().getRootTableAlias());
			getDatasetIDs.append(".");
			getDatasetIDs.append(idColumn);
			getDatasetIDs.append("))");
			getPSBody(builder, getDatasetIDs);

			sql = getDatasetIDs.toString();

			preparedStatement = createPreparedStatement(conn, sql);
			int i = 1;
			if (builder.getWhere() != null) {
				for (SQLArgument o : builder.getWhere().getArguments()) {
					o.setArgument(preparedStatement, i++);
					arguments.add(o.getValue());
				}
			}
			logSqlAndArguments(preparedStatement, sql, arguments);
			rs = preparedStatement.executeQuery();
			rs.next();
			LOG.debug("rs for rowCount: " + rs.getInt(1));
			return rs.getInt(1);
		}
		catch (SQLException e) {
			String msg = Messages.getMessage("ERROR_SQL", preparedStatement.toString(), e.getMessage());
			logSqlExceptionWithSqlAndArguments(e, sql, arguments);
			throw new MetadataStoreException(msg);
		}
		finally {
			JDBCUtils.close(rs, preparedStatement, conn, LOG);
		}
	}

	@Override
	public ISOMetadataResultSet executeGetRecordById(List<String> idList, Connection conn)
			throws MetadataStoreException {
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			int size = idList.size();

			StringBuilder select = new StringBuilder();
			select.append("SELECT ").append(recordColumn);
			select.append(" FROM ").append(mainTable);
			select.append(" WHERE ");
			for (int iter = 0; iter < size; iter++) {
				select.append(fileIdColumn).append(" = ? ");
				if (iter < size - 1) {
					select.append(" OR ");
				}
			}

			String sql = select.toString();
			stmt = createPreparedStatement(conn, sql);
			stmt.setFetchSize(DEFAULT_FETCH_SIZE);
			LOG.debug("select RecordById statement: " + stmt);
			LOG.trace(sql);
			int i = 1;
			for (String identifier : idList) {
				stmt.setString(i, identifier);
				LOG.debug("identifier: " + identifier);
				LOG.debug("" + stmt);
				i++;
			}
			rs = stmt.executeQuery();
		}
		catch (Throwable t) {
			JDBCUtils.close(rs, stmt, conn, LOG);
			String msg = Messages.getMessage("ERROR_REQUEST_TYPE", ResultType.results.name(), t.getMessage());
			LOG.debug(msg);
			throw new MetadataStoreException(msg);
		}
		finally {
			// Don't close the ResultSet or PreparedStatement if no error occurs, the
			// ResultSet is needed in the
			// ISOMetadataResultSet and both will be closed by
			// org.deegree.metadata.persistence.XMLMetadataResultSet#close().
		}
		return new ISOMetadataResultSet(rs, conn, stmt);
	}

	protected AbstractWhereBuilder getWhereBuilder(MetadataQuery query, Connection conn)
			throws FilterEvaluationException, UnmappableException {
		return dialect.getWhereBuilder(new ISOPropertyNameMapper(dialect, queryables),
				(OperatorFilter) query.getFilter(), query.getSorting(), null, false);
	}

	protected PreparedStatement createPreparedStatement(Connection conn, String sql) throws SQLException {
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
		return preparedStatement;
	}

	protected void logSqlAndArguments(PreparedStatement preparedStatement, String sql,
			List<TypedObjectNode> arguments) {
		if (LOG.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Performing SQL statement: \n");
			sb.append("   ").append(sql).append("\n");
			sb.append("with argumens: \n");
			appendArguments(arguments, sb);
			LOG.debug(sb.toString());
		}
		if (LOG.isTraceEnabled())
			LOG.trace(preparedStatement.toString());
	}

	private void logSqlExceptionWithSqlAndArguments(SQLException e, String sql, List<TypedObjectNode> arguments) {
		if (LOG.isErrorEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Error while performing the SQL statement: \n");
			sb.append("   ").append(sql).append("\n");
			sb.append("with arguments \n");
			appendArguments(arguments, sb);
			sb.append("error message: ");
			sb.append(e.getMessage());
			LOG.error(sb.toString());
		}
	}

	private void appendArguments(List<TypedObjectNode> arguments, StringBuilder sb) {
		for (TypedObjectNode argument : arguments) {
			String argumentValue = argument != null ? argument.toString() : "NULL";
			sb.append("   - ").append(argumentValue).append("\n");
		}
	}

}