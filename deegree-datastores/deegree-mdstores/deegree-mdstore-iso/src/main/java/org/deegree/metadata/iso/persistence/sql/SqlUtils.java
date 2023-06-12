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
package org.deegree.metadata.iso.persistence.sql;

import java.util.List;

import org.deegree.sqldialect.filter.AbstractWhereBuilder;
import org.deegree.sqldialect.filter.Join;

/**
 * Contains useful methods to create or adjust sql.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class SqlUtils {

	public static String repairAliasesInWhereClause(AbstractWhereBuilder builder, List<Join> usedJoins,
			List<Join> redundantJoins) {
		String whereClause = builder.getWhere().getSQL().toString();
		for (Join redundantJoin : redundantJoins) {
			Join usedJoin = getEquivalentJoin(redundantJoin, usedJoins);
			String usedAlias = usedJoin.getToTableAlias();
			String redundantAlias = redundantJoin.getToTableAlias();
			whereClause = whereClause.replace(redundantAlias, usedAlias);
		}
		return whereClause;
	}

	private static Join getEquivalentJoin(Join duplicatedJoin, List<Join> usedJoins) {
		for (Join join : usedJoins) {
			if (joinsAreEqual(duplicatedJoin, join)) {
				return join;
			}
		}
		return duplicatedJoin;
	}

	public static boolean joinIsWritten(Join join, List<Join> writtenJoins) {
		for (Join other : writtenJoins) {
			if (joinsAreEqual(join, other)) {
				return true;
			}
		}
		return false;
	}

	private static boolean joinsAreEqual(Join join, Join other) {
		List<String> fromColumns = join.getFromColumns();
		List<String> otherFromColumns = other.getFromColumns();
		if (fromColumns == null) {
			if (otherFromColumns != null)
				return false;
		}
		else if (!fromColumns.equals(otherFromColumns))
			return false;
		String fromTable = join.getFromTable();
		String otherFromTable = other.getFromTable();
		if (fromTable == null) {
			if (otherFromTable != null)
				return false;
		}
		else if (!fromTable.equals(otherFromTable))
			return false;
		List<String> toColumns = join.getToColumns();
		List<String> otherToColumns = other.getToColumns();
		if (toColumns == null) {
			if (otherToColumns != null)
				return false;
		}
		else if (!toColumns.equals(otherToColumns))
			return false;
		String toTable = join.getToTable();
		String otherToTable = other.getToTable();
		if (toTable == null) {
			if (otherToTable != null)
				return false;
		}
		else if (!toTable.equals(otherToTable))
			return false;
		return true;
	}

}
