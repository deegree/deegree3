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
package org.deegree.sqldialect.mssql;

import static java.sql.Types.BOOLEAN;
import static org.deegree.commons.tom.primitive.BaseType.DECIMAL;

import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.DefaultPrimitiveConverter;
import org.deegree.commons.tom.sql.PrimitiveParticleConverter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.BBOX;
import org.deegree.filter.spatial.Beyond;
import org.deegree.filter.spatial.Contains;
import org.deegree.filter.spatial.Crosses;
import org.deegree.filter.spatial.DWithin;
import org.deegree.filter.spatial.Disjoint;
import org.deegree.filter.spatial.Equals;
import org.deegree.filter.spatial.Intersects;
import org.deegree.filter.spatial.Overlaps;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.filter.spatial.Touches;
import org.deegree.filter.spatial.Within;
import org.deegree.geometry.Geometry;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.SortCriterion;
import org.deegree.sqldialect.filter.AbstractWhereBuilder;
import org.deegree.sqldialect.filter.PropertyNameMapper;
import org.deegree.sqldialect.filter.UnmappableException;
import org.deegree.sqldialect.filter.expression.SQLArgument;
import org.deegree.sqldialect.filter.expression.SQLExpression;
import org.deegree.sqldialect.filter.expression.SQLOperation;
import org.deegree.sqldialect.filter.expression.SQLOperationBuilder;
import org.deegree.sqldialect.filter.islike.IsLikeString;

import java.util.List;

/**
 * {@link AbstractWhereBuilder} implementation for Microsoft SQL Server databases.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class MSSQLWhereBuilder extends AbstractWhereBuilder {

	/**
	 * Creates a new {@link MSSQLWhereBuilder} instance.
	 * @param dialect SQL dialect, can be <code>null</code> (TODO refactor code, so not
	 * null is always used)
	 * @param mapper provides the mapping from {@link ValueReference}s to DB columns, must
	 * not be <code>null</code>
	 * @param filter filter to use for generating the WHERE clause, can be
	 * <code>null</code>
	 * @param sortCrit criteria to use for generating the ORDER BY clause, can be
	 * <code>null</code>
	 * @param defaultSortCriteria criteria to use for generating the ORDER-BY clause if
	 * the sort order is not specified by the query, may be <code>null</code>
	 * @param allowPartialMappings if false, any unmappable expression will cause an
	 * {@link UnmappableException} to be thrown
	 * @throws FilterEvaluationException if the expression contains invalid
	 * {@link ValueReference}s
	 * @throws UnmappableException if allowPartialMappings is false and an expression
	 * could not be mapped to the db
	 */
	public MSSQLWhereBuilder(SQLDialect dialect, PropertyNameMapper mapper, OperatorFilter filter,
			SortProperty[] sortCrit, List<SortCriterion> defaultSortCriteria, boolean allowPartialMappings)
			throws FilterEvaluationException, UnmappableException {
		super(dialect, mapper, filter, sortCrit, defaultSortCriteria);
		build(allowPartialMappings);
	}

	/**
	 * copied from postgis
	 * @param op comparison operator to be translated, must not be <code>null</code>
	 * @return corresponding SQL expression, never <code>null</code>
	 * @throws UnmappableException if translation is not possible (usually due to
	 * unmappable property names)
	 * @throws FilterEvaluationException if the expression contains invalid
	 * {@link ValueReference}s
	 */
	@Override
	protected SQLOperation toProtoSQL(PropertyIsLike op) throws UnmappableException, FilterEvaluationException {

		if (!(op.getPattern() instanceof Literal)) {
			String msg = "Mapping of PropertyIsLike with non-literal comparisons to SQL is not implemented yet.";
			throw new UnsupportedOperationException(msg);
		}

		String literal = ((Literal) op.getPattern()).getValue().toString();
		String escape = "" + op.getEscapeChar();
		String wildCard = "" + op.getWildCard();
		String singleChar = "" + op.getSingleChar();

		SQLExpression propName = toProtoSQL(op.getExpression());

		IsLikeString specialString = new IsLikeString(literal, wildCard, singleChar, escape);
		String sqlEncoded = specialString.toSQL(!op.isMatchCase());

		if (propName.isMultiValued()) {
			// TODO escaping of pipe symbols
			sqlEncoded = "%|" + sqlEncoded + "|%";
		}

		SQLOperationBuilder builder = new SQLOperationBuilder();
		if (!op.isMatchCase()) {
			builder.add("LOWER (" + propName + ")");
		}
		else {
			builder.add(propName);
		}

		sqlEncoded = replaceAdditionalMsSqlServerSpecialChars(sqlEncoded);

		builder.add(" LIKE '");
		builder.add(sqlEncoded);
		builder.add("' ESCAPE '\\'");
		return builder.toOperation();
	}

	private String replaceAdditionalMsSqlServerSpecialChars(String sqlEncoded) {
		sqlEncoded = sqlEncoded.replace("[", "\\[");
		sqlEncoded = sqlEncoded.replace("]", "\\]");
		return sqlEncoded;
	}

	@Override
	protected SQLOperation toProtoSQL(SpatialOperator op) throws UnmappableException, FilterEvaluationException {

		SQLOperationBuilder builder = new SQLOperationBuilder(BOOLEAN);

		SQLExpression propNameExpr = toProtoSQLSpatial(op.getPropName());
		if (!propNameExpr.isSpatial()) {
			String msg = "Cannot evaluate spatial operator on database. Targeted property name '" + op.getPropName()
					+ "' does not denote a spatial column.";
			throw new FilterEvaluationException(msg);
		}

		ICRS storageCRS = propNameExpr.getCRS();
		int srid = propNameExpr.getSRID() != null ? Integer.parseInt(propNameExpr.getSRID()) : 0;

		switch (op.getSubType()) {
			case BBOX: {
				BBOX bbox = (BBOX) op;
				builder.add(propNameExpr).add(".STIntersects(");
				builder.add(toProtoSQL(bbox.getBoundingBox(), storageCRS, srid));
				builder.add(")=1");
				break;
			}
			case BEYOND: {
				Beyond beyond = (Beyond) op;
				builder.add("NOT ").add(propNameExpr).add(".STDWithin(");
				builder.add(toProtoSQL(beyond.getGeometry(), storageCRS, srid));
				builder.add(",");
				// TODO uom handling
				PrimitiveType pt = new PrimitiveType(DECIMAL);
				PrimitiveValue value = new PrimitiveValue(beyond.getDistance().getValue(), pt);
				PrimitiveParticleConverter converter = new DefaultPrimitiveConverter(pt, null, false);
				SQLArgument argument = new SQLArgument(value, converter);
				builder.add(argument);
				builder.add(")=1");
				break;
			}
			case CONTAINS: {
				Contains contains = (Contains) op;
				builder.add(propNameExpr).add(".STContains(");
				builder.add(toProtoSQL(contains.getGeometry(), storageCRS, srid));
				builder.add(")=1");
				break;
			}
			case CROSSES: {
				Crosses crosses = (Crosses) op;
				builder.add(propNameExpr).add(".STCrosses(");
				builder.add(toProtoSQL(crosses.getGeometry(), storageCRS, srid));
				builder.add(")=1");
				break;
			}
			case DISJOINT: {
				Disjoint disjoint = (Disjoint) op;
				builder.add(propNameExpr).add(".STDisjoint(");
				builder.add(toProtoSQL(disjoint.getGeometry(), storageCRS, srid));
				builder.add(")=1");
				break;
			}
			case DWITHIN: {
				DWithin dWithin = (DWithin) op;
				builder.add(propNameExpr).add(".STDWithin(");
				builder.add(toProtoSQL(dWithin.getGeometry(), storageCRS, srid));
				builder.add(",");
				// TODO uom handling
				PrimitiveType pt = new PrimitiveType(DECIMAL);
				PrimitiveValue value = new PrimitiveValue(dWithin.getDistance().getValue(), pt);
				PrimitiveParticleConverter converter = new DefaultPrimitiveConverter(pt, null, false);
				SQLArgument argument = new SQLArgument(value, converter);
				builder.add(argument);
				builder.add(")=1");
				break;
			}
			case EQUALS: {
				Equals equals = (Equals) op;
				builder.add(propNameExpr).add(".STEquals(");
				builder.add(toProtoSQL(equals.getGeometry(), storageCRS, srid));
				builder.add(")=1");
				break;
			}
			case INTERSECTS: {
				Intersects intersects = (Intersects) op;
				builder.add(propNameExpr).add(".STIntersects(");
				builder.add(toProtoSQL(intersects.getGeometry(), storageCRS, srid));
				builder.add(")=1");
				break;
			}
			case OVERLAPS: {
				Overlaps overlaps = (Overlaps) op;
				builder.add(propNameExpr).add(".STOverlaps(");
				builder.add(toProtoSQL(overlaps.getGeometry(), storageCRS, srid));
				builder.add(")=1");
				break;
			}
			case TOUCHES: {
				Touches touches = (Touches) op;
				builder.add(propNameExpr).add(".STTouches(");
				builder.add(toProtoSQL(touches.getGeometry(), storageCRS, srid));
				builder.add(")=1");
				break;
			}
			case WITHIN: {
				Within within = (Within) op;
				builder.add(propNameExpr).add(".STWithin(");
				builder.add(toProtoSQL(within.getGeometry(), storageCRS, srid));
				builder.add(")=1");
				break;
			}
		}
		return builder.toOperation();
	}

	private SQLExpression toProtoSQL(Geometry geom, ICRS targetCRS, int srid) {
		// always use is2d = true (else srid handling will not function properly in mssql)
		return new SQLArgument(geom, new MSSQLGeometryConverter(null, targetCRS, "" + srid, true));
	}

}