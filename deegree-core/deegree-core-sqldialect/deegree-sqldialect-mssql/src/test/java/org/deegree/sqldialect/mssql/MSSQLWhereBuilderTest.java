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
package org.deegree.sqldialect.mssql;

import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchAction;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.sort.SortProperty;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.filter.PropertyNameMapper;
import org.deegree.sqldialect.filter.PropertyNameMapping;
import org.deegree.sqldialect.filter.TableAliasManager;
import org.deegree.sqldialect.filter.UnmappableException;
import org.deegree.sqldialect.filter.expression.SQLOperation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link MSSQLWhereBuilder}.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class MSSQLWhereBuilderTest {

	private MSSQLWhereBuilder whereBuilder;

	@Before
	public void setup() throws FilterEvaluationException, UnmappableException {
		SQLDialect sqldialect = null;
		PropertyNameMapper mapper = new PropertyNameMapper() {

			@Override
			public PropertyNameMapping getSpatialMapping(ValueReference propName, TableAliasManager aliasManager)
					throws FilterEvaluationException, UnmappableException {
				return new PropertyNameMapping(null, null, propName.getAsText(), "table");
			}

			@Override
			public PropertyNameMapping getMapping(ValueReference propName, TableAliasManager aliasManager)
					throws FilterEvaluationException, UnmappableException {
				return new PropertyNameMapping(null, null, propName.getAsText(), "table");
			}
		};
		OperatorFilter filter = null;
		SortProperty[] sortCrit = null;
		boolean allowPartialMappings = false;
		whereBuilder = new MSSQLWhereBuilder(sqldialect, mapper, filter, sortCrit, null, allowPartialMappings);
	}

	@Test
	public void testToProtoSQLPropertyIsLikeRequiredEscapeClausePresent()
			throws UnmappableException, FilterEvaluationException {

		Expression testValue = new ValueReference("shortdesc", CommonNamespaces.getNamespaceContext());
		Expression pattern = new Literal<PrimitiveValue>("HOWELLCITY");
		String wildCard = "*";
		String singleChar = "#";
		String escapeChar = "!";
		Boolean matchCase = true;
		MatchAction matchAction = MatchAction.ALL;
		PropertyIsLike op = new PropertyIsLike(testValue, pattern, wildCard, singleChar, escapeChar, matchCase,
				matchAction);
		SQLOperation protoSQL = whereBuilder.toProtoSQL(op);

		StringBuilder sql = protoSQL.getSQL();
		Assert.assertEquals("table.shortdesc LIKE 'HOWELLCITY' ESCAPE '\\'", sql.toString());
	}

	@Test
	public void testToProtoSQLPropertyIsLikeEscapedBrackets() throws UnmappableException, FilterEvaluationException {

		Expression testValue = new ValueReference("shortdesc", CommonNamespaces.getNamespaceContext());
		Expression pattern = new Literal<PrimitiveValue>("HOWELL [CITY]");
		String wildCard = "*";
		String singleChar = "#";
		String escapeChar = "!";
		Boolean matchCase = true;
		MatchAction matchAction = MatchAction.ALL;
		PropertyIsLike op = new PropertyIsLike(testValue, pattern, wildCard, singleChar, escapeChar, matchCase,
				matchAction);
		SQLOperation protoSQL = whereBuilder.toProtoSQL(op);

		StringBuilder sql = protoSQL.getSQL();
		Assert.assertEquals("table.shortdesc LIKE 'HOWELL \\[CITY\\]' ESCAPE '\\'", sql.toString());
	}

}
