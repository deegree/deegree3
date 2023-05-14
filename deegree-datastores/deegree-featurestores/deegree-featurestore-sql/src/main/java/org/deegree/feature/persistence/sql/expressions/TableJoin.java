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
package org.deegree.feature.persistence.sql.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.feature.persistence.sql.id.IDGenerator;
import org.deegree.sqldialect.filter.MappingExpression;

/**
 * Defines a join between two tables with optional ordering and key generation / delete
 * propagation information.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class TableJoin implements MappingExpression {

	private final TableName fromTable;

	private final TableName toTable;

	private final List<SQLIdentifier> fromColumns;

	private final List<SQLIdentifier> toColumns;

	private final List<SQLIdentifier> orderColumns;

	private final boolean numberedOrder;

	private final Map<SQLIdentifier, IDGenerator> keyColumnToGenerator;

	public TableJoin(TableName fromTable, TableName toTable, List<String> fromColumns, List<String> toColumns,
			List<String> orderColumns, boolean numberedOrder, Map<SQLIdentifier, IDGenerator> keyColumnToGenerator) {
		this.fromTable = fromTable;
		this.toTable = toTable;
		if (fromColumns != null) {
			this.fromColumns = new ArrayList<SQLIdentifier>(fromColumns.size());
			for (String fromColumn : fromColumns) {
				this.fromColumns.add(new SQLIdentifier(fromColumn));
			}
		}
		else {
			this.fromColumns = null;
		}
		if (toColumns != null) {
			this.toColumns = new ArrayList<SQLIdentifier>(toColumns.size());
			for (String toColumn : toColumns) {
				this.toColumns.add(new SQLIdentifier(toColumn));
			}
		}
		else {
			this.toColumns = null;
		}
		if (orderColumns != null) {
			this.orderColumns = new ArrayList<SQLIdentifier>(orderColumns.size());
			for (String orderColumn : orderColumns) {
				this.orderColumns.add(new SQLIdentifier(orderColumn));
			}
		}
		else {
			this.orderColumns = null;
		}
		this.numberedOrder = numberedOrder;
		this.keyColumnToGenerator = keyColumnToGenerator;
	}

	public TableName getFromTable() {
		return fromTable;
	}

	public TableName getToTable() {
		return toTable;
	}

	public List<SQLIdentifier> getFromColumns() {
		return fromColumns;
	}

	public List<SQLIdentifier> getToColumns() {
		return toColumns;
	}

	public List<SQLIdentifier> getOrderColumns() {
		return orderColumns;
	}

	public boolean isNumberedOrder() {
		return numberedOrder;
	}

	public Map<SQLIdentifier, IDGenerator> getKeyColumnToGenerator() {
		return keyColumnToGenerator;
	}

	@Override
	public String toString() {
		return fromTable + "." + fromColumns + " <-> " + toTable + "." + toColumns;
	}

}
