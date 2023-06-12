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
package org.deegree.sqldialect.table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;

/**
 * Metadata for a table in an SQL database (columns/types).
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class TableDefinition {

	private TableName name;

	private Map<SQLIdentifier, ColumnDefinition> columnNameToColumn = new LinkedHashMap<SQLIdentifier, ColumnDefinition>();

	/**
	 * Creates a new <code>TableDetails</code> instance.
	 * @param name name of the table, must not be <code>null</code>
	 * @param columns column of the table, must not be <code>null</code>
	 */
	public TableDefinition(TableName name, List<ColumnDefinition> columns) {
		this.name = name;
		for (ColumnDefinition column : columns) {
			columnNameToColumn.put(column.getName(), column);
		}
	}

	/**
	 * Returns the name of table.
	 * @return name of table, never <code>null</code>
	 */
	public SQLIdentifier getName() {
		return name;
	}

	/**
	 * Returns the columns of the table.
	 * @return columns of the table, never <code>null</code>
	 */
	public List<ColumnDefinition> getColumns() {
		return new ArrayList<ColumnDefinition>(columnNameToColumn.values());
	}

	/**
	 * Returns the specified column.
	 * @param name name of the column, must not be <code>null</code>
	 * @return specified column, may be <code>null</code> (no such column)
	 */
	public ColumnDefinition getColumn(SQLIdentifier name) {
		return columnNameToColumn.get(name);
	}

}
