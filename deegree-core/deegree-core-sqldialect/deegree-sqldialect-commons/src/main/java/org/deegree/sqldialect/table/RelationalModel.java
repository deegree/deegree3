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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.deegree.commons.jdbc.SQLIdentifier;

/**
 * Metadata for a number of tables in an SQL database.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class RelationalModel {

	private Map<SQLIdentifier, TableDefinition> tableNameToTable = new TreeMap<SQLIdentifier, TableDefinition>();

	/**
	 * Creates a new <code>RelationalModel</code> instance.
	 * @param tables tables, must not be <code>null</code>
	 */
	public RelationalModel(List<TableDefinition> tables) {
		for (TableDefinition table : tables) {
			tableNameToTable.put(table.getName(), table);
		}
	}

	/**
	 * Returns the tables.
	 * @return tables, never <code>null</code>
	 */
	public List<TableDefinition> getTables() {
		return new ArrayList<TableDefinition>(tableNameToTable.values());
	}

	/**
	 * Returns the specified table.
	 * @param name name of the table, must not be <code>null</code>
	 * @return specified table, may be <code>null</code> (no such table)
	 */
	public TableDefinition getTable(SQLIdentifier name) {
		return tableNameToTable.get(name);
	}

}
