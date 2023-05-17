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

/**
 * Table name with optional schema qualifier and optional quoting.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class TableName extends SQLIdentifier {

	/**
	 * Creates a new {@link TableName} instance.
	 * @param identifier table identifier (with optional schema and quoting), must not be
	 * <code>null</code>
	 */
	public TableName(String identifier) {
		super(identifier);
	}

	/**
	 * Creates a new {@link TableName} instance.
	 * @param table database table identifier, never <code>null</code>
	 * @param schema database schema identifier, can be <code>null</code>
	 */
	public TableName(String table, String schema) {
		super(table, schema);
	}

	/**
	 * Returns the name of the table (without schema).
	 * @return the name of the table, never <code>null</code>
	 */
	public String getTable() {
		return getName();
	}

	/**
	 * Returns the name of the schema.
	 * @return the name of the schema, can be <code>null</code> (default schema)
	 */
	public String getSchema() {
		return getQualifier();
	}

	public static TableName createFromQualifiedName(String tableName) {
		String table = tableName;
		String schema = null;
		if (table.contains(".")) {
			String[] splittedQualifiedTableName = table.split("[.]");
			schema = splittedQualifiedTableName[0];
			table = splittedQualifiedTableName[1];
		}
		return new TableName(table, schema);
	}

}
