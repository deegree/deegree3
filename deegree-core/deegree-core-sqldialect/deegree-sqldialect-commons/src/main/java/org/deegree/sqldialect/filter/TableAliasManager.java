/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.sqldialect.filter;

import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.jdbc.TableName;
import org.deegree.filter.expression.ValueReference;

/**
 * Creates and tracks table aliases that are needed for mapping {@link ValueReference}s to
 * a relational schema.
 *
 * @see AbstractWhereBuilder
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class TableAliasManager {

	private final Map<TableName, String> aliases = new HashMap<TableName, String>();

	private final String rootTableAlias;

	private int currentIdx = 1;

	/**
	 * Creates a new {@link TableAliasManager} instance.
	 */
	public TableAliasManager() {
		rootTableAlias = generateNew();
	}

	/**
	 * Deprecated: Use #getTableAlias(TableName) instead.
	 *
	 * Returns the table alias for the root table.
	 * @return the table alias for the root table, never <code>null</code>
	 */
	@Deprecated
	public String getRootTableAlias() {
		return rootTableAlias;
	}

	/**
	 * Returns the table alias for the passed {@link TableName}.
	 * @param tableName to retrieve the alias for, never <code>null</code>
	 * @return the table alias of the passed {@link TableName}, never <code>null</code>
	 */
	public String getTableAlias(TableName tableName) {
		if (!aliases.containsKey(tableName))
			aliases.put(tableName, generateNew());
		return aliases.get(tableName);
	}

	/**
	 * Returns a new unique table alias.
	 * @return a new unique table alias, never <code>null</code>
	 */
	public String generateNew() {
		return "X" + (currentIdx++);
	}

}
