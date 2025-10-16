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
package org.deegree.sqldialect.filter;

/**
 * The <code></code> class TODO add class documentation here.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class DBField implements MappingExpression {

	private final String schema;

	private final String table;

	private final String column;

	public DBField(String schema, String table, String column) {
		this.schema = schema;
		this.table = table;
		this.column = column;
	}

	public DBField(String table, String column) {
		this(null, table, column);
	}

	public DBField(String column) {
		this(null, null, column);
	}

	public String getSchema() {
		return schema;
	}

	public String getTable() {
		return table;
	}

	public String getColumn() {
		return column;
	}

	@Override
	public String toString() {
		if (schema != null) {
			return schema + "." + table + "." + column;
		}
		if (table != null) {
			return table + "." + column;
		}
		return column;
	}

}
