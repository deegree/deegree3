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
package org.deegree.metadata.iso.persistence.sql;

import java.util.List;

import org.deegree.metadata.iso.persistence.ISOPropertyNameMapper;
import org.deegree.metadata.iso.persistence.queryable.Queryable;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.filter.AbstractWhereBuilder;
import org.deegree.sqldialect.filter.Join;
import org.deegree.sqldialect.filter.PropertyNameMapping;

/**
 * Encapsulates backend informations (table names, column names...) and creation of common
 * sql snippets.
 *
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 */
abstract class AbstractSqlHelper {

	protected String idColumn;

	protected String fileIdColumn;

	protected String recordColumn;

	protected String fk_main;

	protected String mainTable;

	protected String crsTable;

	protected String keywordTable;

	protected String constraintTable;

	protected String opOnTable;

	protected final SQLDialect dialect;

	protected final List<Queryable> queryables;

	AbstractSqlHelper(SQLDialect dialect, List<Queryable> queryables) {
		this.dialect = dialect;
		this.queryables = queryables;
		idColumn = ISOPropertyNameMapper.CommonColumnNames.id.name();
		fk_main = ISOPropertyNameMapper.CommonColumnNames.fk_main.name();
		recordColumn = ISOPropertyNameMapper.CommonColumnNames.recordfull.name();
		fileIdColumn = ISOPropertyNameMapper.CommonColumnNames.fileidentifier.name();
		mainTable = ISOPropertyNameMapper.DatabaseTables.idxtb_main.name();
		crsTable = ISOPropertyNameMapper.DatabaseTables.idxtb_crs.name();
		keywordTable = ISOPropertyNameMapper.DatabaseTables.idxtb_keyword.name();
		opOnTable = ISOPropertyNameMapper.DatabaseTables.idxtb_operatesondata.name();
		constraintTable = ISOPropertyNameMapper.DatabaseTables.idxtb_constraint.name();
	}

	protected StringBuilder getPreparedStatementDatasetIDs(AbstractWhereBuilder builder) {

		StringBuilder getDatasetIDs = new StringBuilder(300);
		String rootTableAlias = builder.getAliasManager().getRootTableAlias();
		getDatasetIDs.append("SELECT DISTINCT ");
		getDatasetIDs.append(rootTableAlias);
		getDatasetIDs.append('.');
		getDatasetIDs.append(idColumn);

		// for SELECT DISTINCT, all ORDER BY columns have to be SELECTed as well
		if (builder.getOrderBy() != null) {
			// hack to transform the ORDER BY column list in select list
			String orderColList = builder.getOrderBy().getSQL().toString();
			int i = 1;
			while (orderColList.contains(" ASC") || orderColList.contains("DESC")) {
				orderColList = orderColList.replaceFirst(" ASC| DESC", " AS crit" + (i++));
			}
			getDatasetIDs.append(',');
			getDatasetIDs.append(orderColList);
		}

		return getDatasetIDs;
	}

	protected void getPSBody(AbstractWhereBuilder builder, StringBuilder getDatasetIDs) {

		String rootTableAlias = builder.getAliasManager().getRootTableAlias();
		getDatasetIDs.append(" FROM ");
		getDatasetIDs.append(mainTable);
		getDatasetIDs.append(" ");
		getDatasetIDs.append(rootTableAlias);

		for (PropertyNameMapping mappedPropName : builder.getMappedPropertyNames()) {
			for (Join join : mappedPropName.getJoins()) {
				getDatasetIDs.append(" LEFT OUTER JOIN ");
				getDatasetIDs.append(join.getToTable());
				getDatasetIDs.append(' ');
				getDatasetIDs.append(join.getToTableAlias());
				getDatasetIDs.append(" ON ");
				getDatasetIDs.append(join.getSQLJoinCondition());
			}
		}

		if (builder.getWhere() != null) {
			getDatasetIDs.append(" WHERE ");
			getDatasetIDs.append(builder.getWhere().getSQL());
		}

	}

}