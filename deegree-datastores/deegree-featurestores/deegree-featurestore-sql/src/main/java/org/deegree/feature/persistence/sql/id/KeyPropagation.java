/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.feature.persistence.sql.id;

import java.util.List;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;

/**
 * Defines the propagation of values from key columns in a source table to foreign key
 * columns in a target table.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class KeyPropagation {

	private final TableName source;

	private final TableName target;

	private final List<SQLIdentifier> pkColumns;

	private final List<SQLIdentifier> fkColumns;

	/**
	 * Creates a new {@link KeyPropagation} instance.
	 * @param source source table, must not be <code>null</code>
	 * @param pkColumn primary key columns (in source table), must not be
	 * <code>null</code> and contain at least one entry
	 * @param target target table, must not be <code>null</code>
	 * @param fkColumn foreign key columns (in target table), must not be
	 * <code>null</code> and contain at least one entry
	 */
	public KeyPropagation(TableName source, List<SQLIdentifier> pkColumns, TableName target,
			List<SQLIdentifier> fkColumns) {
		this.source = source;
		this.pkColumns = pkColumns;
		this.target = target;
		this.fkColumns = fkColumns;
		if (source == null) {
			throw new NullPointerException("Source table must not be null.");
		}
		if (target == null) {
			throw new NullPointerException("Target table must not be null.");
		}
		if (pkColumns == null) {
			throw new NullPointerException("Primary key columns must not be null.");
		}
		if (fkColumns == null) {
			throw new NullPointerException("Foreign key columns must not be null.");
		}
		if (pkColumns.isEmpty()) {
			throw new NullPointerException("At least one primary/foreign key column must be used.");
		}
		if (pkColumns.size() != fkColumns.size()) {
			throw new IllegalArgumentException("Number of primary and foreign key columns must match.");
		}
	}

	/**
	 * Returns the source table.
	 * @return source table, never <code>null</code>
	 */
	public TableName getSourceTable() {
		return source;
	}

	/**
	 * Returns the primary key columns (in source table).
	 * @return primary key columns, never <code>null</code> and contain at least one entry
	 */
	public List<SQLIdentifier> getPrimaryKeyColumns() {
		return pkColumns;
	}

	/**
	 * Returns the target table.
	 * @return target table, never <code>null</code>
	 */
	public TableName getTargetTable() {
		return target;
	}

	/**
	 * Returns the foreign key columns (in target table).
	 * @return foreign key columns, never <code>null</code> and contain at least one entry
	 */
	public List<SQLIdentifier> getForeignKeyColumns() {
		return fkColumns;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof KeyPropagation)) {
			return false;
		}
		KeyPropagation that = (KeyPropagation) obj;
		if (!this.source.equals(that.source) || !this.target.equals(that.target)) {
			return false;
		}
		for (int i = 0; i < pkColumns.size(); i++) {
			if (!this.pkColumns.get(i).equals(that.pkColumns.get(i))) {
				return false;
			}
			if (!this.fkColumns.get(i).equals(that.fkColumns.get(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(source);
		sb.append(".[");
		sb.append(pkColumns.get(0));
		for (int i = 1; i < pkColumns.size(); i++) {
			sb.append(",");
			sb.append(pkColumns.get(i));
		}
		sb.append("] -> ");
		sb.append(target);
		sb.append(".[");
		sb.append(fkColumns.get(0));
		for (int i = 1; i < fkColumns.size(); i++) {
			sb.append(",");
			sb.append(fkColumns.get(i));
		}
		sb.append("]");
		return sb.toString();
	}

}
