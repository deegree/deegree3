/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.feature.persistence.sql.insert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TransactionRow;
import org.deegree.commons.tom.sql.ParticleConversion;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.sql.id.AutoIDGenerator;
import org.deegree.feature.persistence.sql.id.IDGenerator;
import org.deegree.feature.persistence.sql.id.SequenceIDGenerator;
import org.deegree.feature.persistence.sql.id.UUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TransactionRow} that can not be inserted until the values for the foreign keys
 * are known.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public abstract class InsertRow extends TransactionRow {

	private static Logger LOG = LoggerFactory.getLogger(InsertRow.class);

	protected final InsertRowManager mgr;

	// parent rows
	private final Map<InsertRow, ParentRowReference> parentToRef = new HashMap<InsertRow, ParentRowReference>();

	protected InsertRow(InsertRowManager mgr) {
		super(null);
		this.mgr = mgr;
	}

	/**
	 * Generates all keys except those that are created by the DB on INSERT.
	 * @param keyColumnToGenerator columns and their generators, may be <code>null</code>
	 * (no generators)
	 * @throws FeatureStoreException if an error occurs during generation
	 */
	protected void generateImmediateKeys(Map<SQLIdentifier, IDGenerator> keyColumnToGenerator)
			throws FeatureStoreException {
		if (keyColumnToGenerator != null) {
			for (SQLIdentifier autoKeyColumn : keyColumnToGenerator.keySet()) {
				IDGenerator idGenerator = keyColumnToGenerator.get(autoKeyColumn);
				if (idGenerator instanceof SequenceIDGenerator) {
					int seqVal = getSequenceNextVal(((SequenceIDGenerator) idGenerator).getSequence());
					LOG.debug("Got key value for column '" + autoKeyColumn.getName() + "' from sequence: " + seqVal);
					addPreparedArgument(autoKeyColumn, seqVal);
				}
				else if (idGenerator instanceof UUIDGenerator) {
					String uuid = UUID.randomUUID().toString();
					LOG.debug("Got key value for column '" + autoKeyColumn.getName() + "' from UUID: " + uuid);
					addPreparedArgument(autoKeyColumn, uuid);
				}
				else if (idGenerator instanceof AutoIDGenerator) {
					LOG.debug("Key for column '" + autoKeyColumn.getName() + "' will be generated on insert by DB.");
				}
				else {
					LOG.warn("Unhandled ID generator: " + idGenerator.getClass().getName());
				}
			}
		}
	}

	private int getSequenceNextVal(String sequenceName) throws FeatureStoreException {
		String sql = mgr.getDialect().getSelectSequenceNextVal(sequenceName);
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = mgr.getConnection().createStatement();
			LOG.debug("Determing feature ID from db sequence: " + sql);
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				return rs.getInt(1);
			}
			else {
				String msg = "Error determining ID from db sequence. No value returned for: " + sql;
				throw new FeatureStoreException(msg);
			}
		}
		catch (SQLException e) {
			String msg = "Error determining ID from db sequence. No value returned for: " + sql;
			throw new FeatureStoreException(msg, e);
		}
		finally {
			JDBCUtils.close(rs, stmt, null, LOG);
		}
	}

	void addParent(ParentRowReference ref) {
		parentToRef.put(ref.getTarget(), ref);
	}

	void removeParent(InsertRow parent) {

		ParentRowReference ref = parentToRef.get(parent);

		// propagate values from parent (foreign key values)
		List<SQLIdentifier> fromColumns = ref.getKeyPropagation().getPrimaryKeyColumns();
		List<SQLIdentifier> toColumns = ref.getKeyPropagation().getForeignKeyColumns();
		for (int i = 0; i < fromColumns.size(); i++) {
			SQLIdentifier fromColumn = fromColumns.get(i);
			SQLIdentifier toColumn = toColumns.get(i);
			Object key = parent.get(fromColumn);
			if (key == null) {
				String msg = "Unable to create foreign key relation. Encountered NULL value for foreign key column '"
						+ fromColumn + "'.";
				throw new IllegalArgumentException(msg);
			}
			addPreparedArgument(toColumn, key);
		}

		// if parent is a feature row, set value for href column
		if (ref.isHrefed(this) && parent instanceof FeatureRow) {
			addPreparedArgument(ref.getHrefColum(this), "#" + ((FeatureRow) parent).getNewId());
		}

		parentToRef.remove(parent);
	}

	/**
	 * Returns whether this {@link InsertRow} has uninserted parents, i.e. rows that
	 * provide keys which are foreign keys in this row.
	 * @return <code>true</code>, if this node has uninserted parents, <code>false</code>
	 * otherwise
	 */
	boolean hasParents() {
		return !parentToRef.isEmpty();
	}

	/**
	 * Performs the insertion and deals with propagating the values of auto-generated
	 * columns to child rows.
	 * @param conn JDBC connection to use for insertion, must not be <code>null</code>
	 * @param propagateNonFidAutoGenColumns <code>true</code>, if auto generated key
	 * columns need to be processed (and propagated), <code>false</code> otherwise
	 * @throws SQLException
	 * @throws FeatureStoreException
	 */
	void performInsert(Connection conn, boolean propagateNonFidAutoGenColumns)
			throws SQLException, FeatureStoreException {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Inserting row: " + this);
		}

		String sql = getSql();
		PreparedStatement stmt = null;

		Set<SQLIdentifier> autoGenColumns = getAutogenColumns(propagateNonFidAutoGenColumns);
		if (autoGenColumns.isEmpty()) {
			stmt = conn.prepareStatement(sql);
		}
		else {
			String[] cols = new String[autoGenColumns.size()];
			int i = 0;
			for (SQLIdentifier id : autoGenColumns) {
				if (!id.isEscaped()) {
					cols[i++] = id.getName().toLowerCase();
				}
				else {
					cols[i++] = id.getName();
				}
			}
			stmt = conn.prepareStatement(sql, cols);
		}
		int columnId = 1;
		for (Entry<SQLIdentifier, Object> entry : columnToObject.entrySet()) {
			if (entry.getValue() != null) {
				LOG.debug("- Argument " + entry.getKey() + " = " + entry.getValue() + " (" + entry.getValue().getClass()
						+ ")");
				if (entry.getValue() instanceof ParticleConversion<?>) {
					ParticleConversion<?> conversion = (ParticleConversion<?>) entry.getValue();
					conversion.setParticle(stmt, columnId++);
				}
				else {
					stmt.setObject(columnId++, entry.getValue());
				}
			}
			else {
				LOG.debug("- Argument " + entry.getKey() + " = NULL");
				stmt.setObject(columnId++, null);
			}
		}
		stmt.execute();

		if (!autoGenColumns.isEmpty()) {
			ResultSet rs = null;
			try {
				rs = stmt.getGeneratedKeys();
				if (rs.next()) {
					int i = 1;
					for (SQLIdentifier autoGenCol : autoGenColumns) {
						Object keyValue = rs.getObject(i++);
						columnToObject.put(autoGenCol, keyValue);
						LOG.debug("Retrieved auto generated key: " + autoGenCol + "=" + keyValue);
					}
				}
				else {
					throw new FeatureStoreException("DB didn't return auto-generated columns.");
				}
			}
			finally {
				if (rs != null) {
					rs.close();
				}
			}
		}
		stmt.close();
	}

	protected Set<SQLIdentifier> getAutogenColumns(boolean propagateNonFidAutoGenColumns) {
		Set<SQLIdentifier> cols = new LinkedHashSet<SQLIdentifier>();
		if (propagateNonFidAutoGenColumns) {
			if (mgr.getGenColumns(table) != null) {
				cols.addAll(mgr.getGenColumns(table));
			}
		}
		return cols;
	}

	@Override
	public String getSql() {
		StringBuilder sql = new StringBuilder("INSERT INTO " + table + "(");
		boolean first = true;
		for (SQLIdentifier column : columnToLiteral.keySet()) {
			if (!first) {
				sql.append(',');
			}
			else {
				first = false;
			}
			sql.append(column);
		}
		sql.append(") VALUES(");
		first = true;
		for (Entry<SQLIdentifier, String> entry : columnToLiteral.entrySet()) {
			if (!first) {
				sql.append(',');
			}
			else {
				first = false;
			}
			sql.append(entry.getValue());
		}
		sql.append(")");
		return sql.toString();
	}

	@Override
	public String toString() {
		return getSql();
	}

}
