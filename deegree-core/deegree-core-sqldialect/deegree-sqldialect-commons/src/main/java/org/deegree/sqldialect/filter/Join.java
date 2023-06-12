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

import static java.util.Collections.singletonList;

import java.util.List;

/**
 * The <code></code> class TODO add class documentation here.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class Join {

	private final String fromTable;

	private final String fromTableAlias;

	private final List<String> fromColumns;

	private final String toTable;

	private final String toTableAlias;

	private final List<String> toColumns;

	public Join(String fromTable, String fromTableAlias, String fromColumn, String toTable, String toTableAlias,
			String toColumn) {
		this.fromTable = fromTable;
		this.fromTableAlias = fromTableAlias;
		this.fromColumns = singletonList(fromColumn);
		this.toTable = toTable;
		this.toTableAlias = toTableAlias;
		this.toColumns = singletonList(toColumn);
	}

	public Join(String fromTable, String fromTableAlias, List<String> fromColumns, String toTable, String toTableAlias,
			List<String> toColumns) {
		this.fromTable = fromTable;
		this.fromTableAlias = fromTableAlias;
		this.fromColumns = fromColumns;
		this.toTable = toTable;
		this.toTableAlias = toTableAlias;
		this.toColumns = toColumns;
	}

	public String getFromTable() {
		return fromTable;
	}

	public String getFromTableAlias() {
		return fromTableAlias;
	}

	public List<String> getFromColumns() {
		return fromColumns;
	}

	public String getToTable() {
		return toTable;
	}

	public String getToTableAlias() {
		return toTableAlias;
	}

	public List<String> getToColumns() {
		return toColumns;
	}

	public String getSQLJoinCondition() {
		StringBuilder sb = new StringBuilder();

		if (fromColumns.size() > 1) {
			sb.append('(');
		}

		sb.append(fromTableAlias);
		sb.append('.');
		sb.append(fromColumns.get(0));
		sb.append('=');
		sb.append(toTableAlias);
		sb.append('.');
		sb.append(toColumns.get(0));

		for (int i = 1; i < fromColumns.size(); i++) {
			sb.append(" AND ");
			sb.append(fromTableAlias);
			sb.append('.');
			sb.append(fromColumns.get(i));
			sb.append('=');
			sb.append(toTableAlias);
			sb.append('.');
			sb.append(toColumns.get(i));
		}

		if (fromColumns.size() > 1) {
			sb.append(')');
		}
		return sb.toString();
	}

}
