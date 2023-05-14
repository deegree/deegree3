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
package org.deegree.commons.jdbc;

import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map.Entry;

import org.deegree.commons.tom.sql.ParticleConversion;
import org.slf4j.Logger;

/**
 * Encapsulates columns and values for updating one row in a database table.
 *
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 */
public class UpdateRow extends TransactionRow {

	private static final Logger LOG = getLogger(UpdateRow.class);

	private String whereClause;

	public UpdateRow(TableName table) {
		super(table);
	}

	/**
	 * @param whereClause the whereClause to set, without 'WHERE' in the beginning
	 */
	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}

	/**
	 * @return the whereClause
	 */
	public String getWhereClause() {
		return whereClause;
	}

	@Override
	public String getSql() {
		StringBuilder sql = new StringBuilder("UPDATE " + table + " SET ");
		boolean first = true;
		for (SQLIdentifier column : columnToLiteral.keySet()) {
			if (!first) {
				sql.append(',');
			}
			else {
				first = false;
			}
			sql.append(column);
			sql.append(" = ");
			sql.append(columnToLiteral.get(column));
		}
		sql.append(" WHERE ").append(whereClause);
		return sql.toString();
	}

	@Override
	public String toString() {
		return getSql();
	}

	public void performUpdate(Connection conn) throws SQLException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Updating: " + this);
		}

		String sql = getSql();
		PreparedStatement stmt = null;
		stmt = conn.prepareStatement(sql);

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
		stmt.close();
	}

}
