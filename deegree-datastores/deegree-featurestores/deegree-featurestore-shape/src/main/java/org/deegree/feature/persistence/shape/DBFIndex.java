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
package org.deegree.feature.persistence.shape;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.db.ConnectionProvider;
import org.deegree.feature.persistence.shape.ShapeFeatureStoreProvider.Mapping;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.ResourceId;
import org.deegree.filter.sort.SortProperty;
import org.deegree.sqldialect.filter.UnmappableException;
import org.deegree.sqldialect.filter.expression.SQLArgument;
import org.deegree.sqldialect.filter.expression.SQLExpression;

/**
 * This class converts the dbf file into a H2 database, to enable proper filtering.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class DBFIndex {

	private ConnectionProvider connProvider;

	/**
	 * @param dbf
	 * @param file
	 * @param envelopes
	 * @throws IOException
	 */
	public DBFIndex(DBFReader dbf, File file, Pair<ArrayList<Pair<float[], Long>>, Boolean> envelopes,
			List<Mapping> mappings) throws IOException {
		connProvider = new DbfIndexImporter(dbf, file, envelopes, mappings).createIndex();
	}

	/**
	 * @param available is modified in place to contain only matches!
	 * @param filter
	 * @param sort
	 * @return null, if there was an error, else a pair of left overs (with possibly null
	 * values if everything could be mapped)
	 * @throws FilterEvaluationException
	 * @throws UnmappableException
	 */
	public Pair<Filter, SortProperty[]> query(List<Pair<Integer, Long>> available, Filter filter, SortProperty[] sort)
			throws FilterEvaluationException {

		if (filter == null && (sort == null || sort.length == 0)) {
			return new Pair<Filter, SortProperty[]>();
		}

		if (filter == null) {
			return null;
		}

		H2WhereBuilder where = null;
		SQLExpression generated = null;
		if (filter instanceof OperatorFilter) {
			where = new H2WhereBuilder(null, (OperatorFilter) filter, sort);
			generated = where.getWhere();
			if (generated == null) {
				return null;
			}
		}

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet set = null;
		try {
			conn = connProvider.getConnection();
			if (generated == null) {
				StringBuilder sb = new StringBuilder();
				for (ResourceId rid : ((IdFilter) filter).getSelectedIds()) {
					String id = rid.getRid();
					sb.append(id.substring(id.lastIndexOf("_") + 1));
					sb.append(",");
				}
				sb.deleteCharAt(sb.length() - 1);
				stmt = conn.prepareStatement(
						"select record_number,file_index from dbf_index where record_number in (" + sb + ")");
			}
			else {
				String clause = generated.getSQL().toString();

				stmt = conn.prepareStatement("select record_number,file_index from dbf_index where " + clause);

				int i = 1;
				for (SQLArgument lit : generated.getArguments()) {
					lit.setArgument(stmt, i++);
					// TODO what about ElementNode?
					// Object o = lit.getValue();
					// if ( o instanceof PrimitiveValue ) {
					// o = ( (PrimitiveValue) o ).getValue();
					// }
					// if ( o instanceof ElementNode ) {
					// stmt.setString( i++, o.toString() );
					// } else {
					// stmt.setObject( i++, o );
					// }
				}
			}

			set = stmt.executeQuery();

			while (set.next()) {
				available.add(new Pair<Integer, Long>(set.getInt("record_number"), set.getLong("file_index")));
			}

			if (where == null) {
				return new Pair<Filter, SortProperty[]>(null, sort);
			}
			return new Pair<Filter, SortProperty[]>(where.getPostFilter(), where.getPostSortCriteria());
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			JDBCUtils.close(set);
			JDBCUtils.close(stmt);
			JDBCUtils.close(conn);
		}

		return null;

	}

	public void destroy() {
		connProvider.destroy();
	}

}
