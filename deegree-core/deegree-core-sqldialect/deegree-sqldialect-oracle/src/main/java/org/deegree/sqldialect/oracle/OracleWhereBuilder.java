/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

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
package org.deegree.sqldialect.oracle;

import static java.sql.Types.BOOLEAN;
import static org.deegree.commons.tom.primitive.BaseType.DECIMAL;
import static org.deegree.commons.tom.primitive.BaseType.STRING;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.filter.comparison.ComparisonOperator.SubType.PROPERTY_IS_NIL;

import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.DefaultPrimitiveConverter;
import org.deegree.commons.tom.sql.PrimitiveParticleConverter;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.comparison.PropertyIsNil;
import org.deegree.filter.expression.Function;
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
 * {@link AbstractWhereBuilder} implementation for Oracle Spatial databases.
 *
 * Oracle Database Version 10g or 11g are recommended (Oracle Version 9i and 8.1.7 may
 * also work)
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
class OracleWhereBuilder extends AbstractWhereBuilder {

	private int databaseMajorVersion;

	/**
	 * Creates a new {@link OracleWhereBuilder} instance.
	 * @param dialect SQL dialect, must not be <code>null</code>
	 * @param mapper provides the mapping from {@link ValueReference}s to DB columns, must
	 * not be <code>null</code>
	 * @param filter Filter to use for generating the WHERE clause, can be
	 * <code>null</code>
	 * @param sortCrit criteria to use generating the ORDER BY clause, can be
	 * <code>null</code>
	 * @param defaultSortCriteria criteria to use for generating the ORDER-BY clause if
	 * the sort order is not specified by the query, may be <code>null</code>
	 * @param allowPartialMappings if false, any unmappable expression will cause an
	 * {@link UnmappableException} to be thrown
	 * @throws FilterEvaluationException
	 * @throws UnmappableException if allowPartialMappings is false and an expression
	 * could not be mapped to the db
	 */
	OracleWhereBuilder(OracleDialect dialect, PropertyNameMapper mapper, OperatorFilter filter, SortProperty[] sortCrit,
			List<SortCriterion> defaultSortCriteria, boolean allowPartialMappings, int databaseMajorVersion)
			throws FilterEvaluationException, UnmappableException {
		super(dialect, mapper, filter, sortCrit, defaultSortCriteria);
		this.databaseMajorVersion = databaseMajorVersion;
		build(allowPartialMappings);
	}

	@Override
	protected SQLOperation toProtoSQL(SpatialOperator op) throws UnmappableException, FilterEvaluationException {

		SQLOperationBuilder builder = new SQLOperationBuilder(BOOLEAN);
		SQLExpression propNameExpr = toProtoSQLSpatial(op.getPropName());

		switch (op.getSubType()) {
			case BBOX:
				BBOX bbox = (BBOX) op;
				if (bbox.getAllowFalsePositives()) {
					appendFilterOperation(builder, propNameExpr, ((BBOX) op).getBoundingBox());
				}
				else {
					appendRelateOperation(builder, propNameExpr, ((BBOX) op).getBoundingBox(), "ANYINTERACT");
				}
				break;
			case INTERSECTS:
				appendRelateOperation(builder, propNameExpr, ((Intersects) op).getGeometry(), "ANYINTERACT");
				break;
			case EQUALS:
				appendRelateOperation(builder, propNameExpr, ((Equals) op).getGeometry(), "EQUAL");
				break;
			case DISJOINT:
				builder.add("NOT ");
				appendRelateOperation(builder, propNameExpr, ((Disjoint) op).getGeometry(), "ANYINTERACT");
				break;
			case TOUCHES:
				appendRelateOperation(builder, propNameExpr, ((Touches) op).getGeometry(), "TOUCH");
				break;
			case WITHIN:
				appendRelateOperation(builder, propNameExpr, ((Within) op).getGeometry(), "INSIDE+COVEREDBY");
				break;
			case OVERLAPS:
				appendRelateOperation(builder, propNameExpr, ((Overlaps) op).getGeometry(), "OVERLAPBDYINTERSECT");
				break;
			case CROSSES:
				appendRelateOperation(builder, propNameExpr, ((Crosses) op).getGeometry(), "OVERLAPBDYDISJOINT");
				break;
			case CONTAINS:
				appendRelateOperation(builder, propNameExpr, ((Contains) op).getGeometry(), "CONTAINS+COVERS");
				break;
			case DWITHIN:
				appendDWithinOperation(builder, propNameExpr, ((DWithin) op).getGeometry(),
						((DWithin) op).getDistance());
				break;
			case BEYOND:
				builder.add("NOT ");
				appendDWithinOperation(builder, propNameExpr, ((Beyond) op).getGeometry(), ((Beyond) op).getDistance());
				break;
		}

		return builder.toOperation();
	}

	/**
	 * Append a primary (index) filter to the query
	 */
	private void appendFilterOperation(SQLOperationBuilder builder, SQLExpression propNameExpr, Geometry geom) {
		ICRS storageCRS = propNameExpr.getCRS();
		int srid = propNameExpr.getSRID() != null ? Integer.parseInt(propNameExpr.getSRID()) : -1;

		builder.add("MDSYS.SDO_FILTER(");
		builder.add(propNameExpr);
		builder.add(",");
		builder.add(toProtoSQL(geom, storageCRS, srid));

		if (databaseMajorVersion < 10)
			builder.add(",'querytype=WINDOW')='TRUE'");
		else
			builder.add(")='TRUE'");
	}

	/**
	 * Append a primary (index) and secondary (geometry) filter to the query
	 */
	private void appendRelateOperation(SQLOperationBuilder builder, SQLExpression propNameExpr, Geometry geom,
			String mask) {
		ICRS storageCRS = propNameExpr.getCRS();
		int srid = propNameExpr.getSRID() != null ? Integer.parseInt(propNameExpr.getSRID()) : -1;

		builder.add("MDSYS.SDO_RELATE(");
		builder.add(propNameExpr);
		builder.add(",");
		builder.add(toProtoSQL(geom, storageCRS, srid));
		if (databaseMajorVersion < 10)
			builder.add(",'MASK=" + mask + " querytype=WINDOW')='TRUE'");
		else
			builder.add(",'MASK=" + mask + "')='TRUE'");
	}

	/**
	 * Append a primary (index) and secondary (geometry) distance based filter to the
	 * query
	 */
	private void appendDWithinOperation(SQLOperationBuilder builder, SQLExpression propNameExpr, Geometry geom,
			Measure distance) {
		ICRS storageCRS = propNameExpr.getCRS();
		int srid = propNameExpr.getSRID() != null ? Integer.parseInt(propNameExpr.getSRID()) : -1;

		builder.add("MDSYS.SDO_WITHIN_DISTANCE(");
		builder.add(propNameExpr);
		builder.add(",");
		builder.add(toProtoSQL(geom, storageCRS, srid));
		// TODO handle uom correctly

		PrimitiveType pt = new PrimitiveType(DECIMAL);
		PrimitiveValue value = new PrimitiveValue(distance.getValue(), pt);
		PrimitiveParticleConverter converter = new DefaultPrimitiveConverter(pt, null, false);
		SQLArgument argument = new SQLArgument(value, converter);

		// TRICKY do not use "QUERYTYPE=FILTER" as parameter to skip the secondary filter
		// operation, which will lead to
		// imprecise results

		builder.add(",'DISTANCE=");
		builder.add(argument);
		builder.add("')='TRUE'");
	}

	private SQLExpression toProtoSQL(Geometry geom, ICRS storageCRS, int srid) {
		return new SQLArgument(geom, new OracleGeometryConverter(null, storageCRS, "" + srid));
	}

	/**
	 * Translates the given {@link ComparisonOperator} into an {@link SQLExpression}.
	 * @param op comparison operator to be translated, must not be <code>null</code>
	 * @return corresponding SQL expression, never <code>null</code>
	 * @throws UnmappableException if translation is not possible (usually due to
	 * unmappable property names)
	 * @throws FilterEvaluationException if the filter contains invalid
	 * {@link ValueReference}s
	 */
	@Override
	protected SQLExpression toProtoSQL(ComparisonOperator op) throws UnmappableException, FilterEvaluationException {
		SQLOperation sqlOper = null;

		if (PROPERTY_IS_NIL == op.getSubType()) {

			PropertyIsNil propIsNil = (PropertyIsNil) op;
			SQLOperationBuilder builder = new SQLOperationBuilder(BOOLEAN);
			Expression expr = propIsNil.getPropertyName();
			if (!(expr instanceof ValueReference)) {
				final String msg = "Mapping of PropertyIsNil is only supported for ValueReference expressions.";
				throw new UnmappableException(msg);
			}
			ValueReference valRef = (ValueReference) expr;
			NamespaceBindings nsContext = valRef.getNsContext();
			nsContext.addNamespace("xsi", XSINS);
			valRef = new ValueReference(valRef.getAsText() + "/@xsi:nil", nsContext);
			// TODO what if the xsi:nil attribute is not explicitly mapped (fallback to
			// NULL mapping?)
			builder.add(toProtoSQL(valRef));
			builder.add(" = ");
			PrimitiveType pt = new PrimitiveType(BaseType.BOOLEAN);
			PrimitiveValue pv = new PrimitiveValue(Boolean.TRUE, pt);
			builder.add(new SQLArgument(pv, new OraclePrimitiveConverter(pt, null)));
			sqlOper = builder.toOperation();
		}

		if (sqlOper == null) {
			return super.toProtoSQL(op);
		}
		else {
			return sqlOper;
		}
	}

	/**
	 * Translates the given {@link PropertyIsLike} into an {@link SQLOperation}.
	 * @param op comparison operator to be translated, must not be <code>null</code>
	 * @return corresponding SQL expression, never <code>null</code>
	 * @throws UnmappableException if translation is not possible (usually due to
	 * unmappable property names)
	 * @throws FilterEvaluationException if the filter contains invalid
	 * {@link ValueReference}s
	 */
	protected SQLOperation toProtoSQL(PropertyIsLike op) throws UnmappableException, FilterEvaluationException {

		Expression pattern = op.getPattern();
		if (pattern instanceof Literal) {
			String literal = ((Literal<?>) pattern).getValue().toString();
			return toProtoSql(op, literal);
		}
		else if (pattern instanceof Function) {
			String valueAsString = getStringValueFromFunction(pattern);
			return toProtoSql(op, valueAsString);
		}
		String msg = "Mapping of PropertyIsLike with non-literal or non-function comparisons to SQL is not implemented yet.";
		throw new UnsupportedOperationException(msg);
	}

	private SQLOperation toProtoSql(PropertyIsLike op, String literal)
			throws UnmappableException, FilterEvaluationException {
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

		SQLOperationBuilder builder = new SQLOperationBuilder(BOOLEAN);
		if (!op.isMatchCase()) {
			builder.add("LOWER (" + propName + ")");
		}
		else {
			builder.add(propName);
		}
		builder.add(" LIKE ");
		PrimitiveType pt = new PrimitiveType(STRING);
		PrimitiveValue value = new PrimitiveValue(sqlEncoded, pt);
		PrimitiveParticleConverter converter = new DefaultPrimitiveConverter(pt, null, propName.isMultiValued());
		SQLArgument argument = new SQLArgument(value, converter);
		builder.add(argument);

		// set default ESCAPE Character to backslash
		// oracle (10.2 - 12.2) defaults to have escaping disabled by default
		builder.add(" ESCAPE '\\'");

		return builder.toOperation();
	}

}