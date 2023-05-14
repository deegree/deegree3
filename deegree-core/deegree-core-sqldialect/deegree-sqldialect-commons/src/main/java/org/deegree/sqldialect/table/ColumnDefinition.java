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

import java.sql.Types;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.tom.primitive.BaseType;

/**
 * Metadata for a column of a table in an SQL database.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class ColumnDefinition {

	private SQLIdentifier name;

	private BaseType type;

	private Integer jdbcCode;

	private String nativeType;

	/**
	 * Creates a new <code>ColumnDefinition</code> instance.
	 * @param name name of the column, must not be <code>null</code>
	 * @param type type of the column, must not be <code>null</code>
	 * @param jdbcCode type code from {@link Types}, may be <code>null</code>
	 * @param nativeType database native type definition (e.g.
	 * <code>VARCHAR2(255)</code>), may be <code>null</code>
	 */
	public ColumnDefinition(SQLIdentifier name, BaseType type, Integer jdbcCode, String nativeType) {
		this.name = name;
		this.type = type;
		this.jdbcCode = jdbcCode;
		this.nativeType = nativeType;
	}

	/**
	 * Returns the name of the column.
	 * @return name of the column, never <code>null</code>
	 */
	public SQLIdentifier getName() {
		return name;
	}

	/**
	 * Returns the type of the column.
	 * @return name of the column, never <code>null</code>
	 */
	public BaseType getType() {
		return type;
	}

	/**
	 * Returns the type code from {@link Types}.
	 * @return type code or <code>null</code> (no JDBC type code available)
	 */
	public Integer getJdbcCode() {
		return jdbcCode;
	}

	/**
	 * Return the database native type definition.
	 * @return database native type definition (e.g. <code>VARCHAR2(255)</code>) or
	 * <code>null</code> (no native type definition available)
	 */
	public String getNativeType() {
		return nativeType;
	}

}
