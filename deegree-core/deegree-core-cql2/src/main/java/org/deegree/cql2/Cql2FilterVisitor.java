/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2025 by:
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
package org.deegree.cql2;

import static org.deegree.cql2.FilterPropertyType.DATE;
import static org.deegree.cql2.FilterPropertyType.DATE_TIME;
import static org.deegree.cql2.FilterPropertyType.TIME;
import static org.deegree.cql2.FilterPropertyType.UNKNOWN;
import static org.slf4j.LoggerFactory.getLogger;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.deegree.commons.tom.datetime.ISO8601Converter;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.Expression;
import org.deegree.filter.MatchAction;
import org.deegree.filter.Operator;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.Not;
import org.deegree.filter.logical.Or;
import org.deegree.filter.spatial.Intersects;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.filter.temporal.After;
import org.deegree.filter.temporal.TemporalOperator;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class Cql2FilterVisitor extends Cql2ParserBaseVisitor {

	private static final Logger LOG = getLogger(Cql2FilterVisitor.class);

	private final ICRS filterCrs;

	private final List<FilterProperty> filterProperties;

	/**
	 * @param filterCrs never <code>null</code>
	 * @param filterProperties used to identify properties with namespace bindings, may be
	 * empty or <code>null</code>
	 */
	public Cql2FilterVisitor(ICRS filterCrs, List<FilterProperty> filterProperties) {
		this.filterCrs = filterCrs;
		this.filterProperties = filterProperties != null ? filterProperties : Collections.emptyList();
	}

	@Override
	public Object visitBooleanExpression(Cql2Parser.BooleanExpressionContext ctx) {
		int terms = ctx.booleanTerm().size();
		if (terms == 1) {
			return ctx.booleanTerm(0).accept(this);
		}
		throw new Cql2UnsupportedExpressionException("More than one booleanTerm are currently not supported.");
	}

	@Override
	public Object visitBooleanTerm(Cql2Parser.BooleanTermContext ctx) {
		List<Operator> operators = ctx.booleanFactor().stream().map(factor -> (Operator) factor.accept(this)).toList();
		if (operators.isEmpty())
			return null;
		if (operators.size() == 1)
			return operators.get(0);
		return new And(operators.toArray(new Operator[0]));
	}

	@Override
	public Object visitBooleanFactor(Cql2Parser.BooleanFactorContext ctx) {
		return ctx.booleanPrimary().accept(this);
	}

	@Override
	public Object visitBooleanPrimary(Cql2Parser.BooleanPrimaryContext ctx) {
		if (ctx.booleanExpression() != null)
			throw new Cql2UnsupportedExpressionException("booleanExpressions are currently not supported.");
		if (ctx.function() != null)
			throw new Cql2UnsupportedExpressionException("functions are currently not supported.");
		if (ctx.BooleanLiteral() != null)
			throw new Cql2UnsupportedExpressionException("BooleanLiterals are currently not supported.");
		return ctx.predicate().accept(this);
	}

	@Override
	public Object visitPredicate(Cql2Parser.PredicateContext ctx) {
		if (ctx.comparisonPredicate() != null)
			return ctx.comparisonPredicate().accept(this);
		if (ctx.temporalPredicate() != null)
			return ctx.temporalPredicate().accept(this);
		if (ctx.arrayPredicate() != null)
			throw new Cql2UnsupportedExpressionException("arrayPredicate are currently not supported.");
		return ctx.spatialPredicate().accept(this);
	}

	@Override
	public Object visitComparisonPredicate(Cql2Parser.ComparisonPredicateContext ctx) {
		if (ctx.binaryComparisonPredicate() != null)
			return ctx.binaryComparisonPredicate().accept(this);
		if (ctx.isLikePredicate() != null)
			return ctx.isLikePredicate().accept(this);
		if (ctx.isBetweenPredicate() != null)
			throw new Cql2UnsupportedExpressionException("isBetweenPredicates are currently not supported.");
		if (ctx.isInListPredicate() != null)
			return ctx.isInListPredicate().accept(this);
		if (ctx.isNullPredicate() != null)
			throw new Cql2UnsupportedExpressionException("isNullPredicates are currently not supported.");
		throw new Cql2UnsupportedExpressionException("ComparisonPredicate is currently not supported.");
	}

	@Override
	public Object visitBinaryComparisonPredicate(Cql2Parser.BinaryComparisonPredicateContext ctx) {
		String comparisonOperator = ctx.ComparisonOperator().getText();
		switch (comparisonOperator) {
			case "=":
				boolean matchCase = ctx.getText().contains("CASEI");
				Expression param1 = (Expression) ctx.scalarExpression().get(0).accept(this);
				Expression param2 = (Expression) ctx.scalarExpression().get(1).accept(this);
				return new PropertyIsEqualTo(param1, param2, matchCase, MatchAction.ANY);
		}
		throw new Cql2UnsupportedExpressionException("Unsupported comparisonOperator " + comparisonOperator);
	}

	@Override
	public Object visitIsLikePredicate(Cql2Parser.IsLikePredicateContext ctx) {
		boolean matchCase = ctx.getText().contains("CASEI");
		Expression param1 = (Expression) ctx.characterExpression().accept(this);
		Expression param2 = (Expression) ctx.patternExpression().accept(this);
		return new PropertyIsLike(param1, param2, "%", "_", "\\", matchCase, MatchAction.ANY);
	}

	@Override
	public Object visitIsInListPredicate(Cql2Parser.IsInListPredicateContext ctx) {
		Expression param1 = (Expression) ctx.scalarExpression().accept(this);
		List<Expression> params = (List<Expression>) ctx.inList().accept(this);
		List<PropertyIsEqualTo> propertyIsEqualToFilters = params.stream()
			.map(param -> new PropertyIsEqualTo(param1, param, true, MatchAction.ANY))
			.toList();
		Operator isInOperator = createIsInOperator(propertyIsEqualToFilters);
		if (Objects.nonNull(ctx.NOT()))
			return new Not(isInOperator);
		return isInOperator;
	}

	private static Operator createIsInOperator(List<PropertyIsEqualTo> propertyIsEqualToFilters) {
		if (propertyIsEqualToFilters.size() == 1) {
			return propertyIsEqualToFilters.get(0);
		}
		return new Or(propertyIsEqualToFilters.toArray(PropertyIsEqualTo[]::new));
	}

	@Override
	public Object visitInList(Cql2Parser.InListContext ctx) {
		return ctx.scalarExpression()
			.stream()
			.map(scalarExpression -> (Expression) scalarExpression.accept(this))
			.toList();
	}

	@Override
	public Object visitScalarExpression(Cql2Parser.ScalarExpressionContext ctx) {
		if (ctx.characterClause() != null)
			return ctx.characterClause().accept(this);
		if (ctx.propertyName() != null)
			return ctx.propertyName().accept(this);
		if (ctx.NumericLiteral() != null) {
			String numericText = ctx.NumericLiteral().getText();
			try {
				int i = Integer.parseInt(numericText);
				return new Literal<>(new PrimitiveValue(i, new PrimitiveType(BaseType.INTEGER)), null);
			}
			catch (NumberFormatException e) {
				double d = Double.parseDouble(numericText);
				return new Literal<>(new PrimitiveValue(d, new PrimitiveType(BaseType.DOUBLE)), null);
			}
		}
		if (ctx.function() != null)
			throw new Cql2UnsupportedExpressionException("functions are currently not supported.");
		throw new Cql2UnsupportedExpressionException("ScalarExpression is currently not supported.");
	}

	@Override
	public Object visitCharacterExpression(Cql2Parser.CharacterExpressionContext ctx) {
		if (ctx.characterClause() != null)
			return ctx.characterClause().accept(this);
		if (ctx.propertyName() != null)
			return ctx.propertyName().accept(this);
		if (ctx.function() != null)
			throw new Cql2UnsupportedExpressionException("functions are currently not supported.");
		throw new Cql2UnsupportedExpressionException("CharacterExpression is currently not supported.");
	}

	@Override
	public Object visitCharacterClause(Cql2Parser.CharacterClauseContext ctx) {
		return parseLiteral(ctx.getText(), ctx.CASEI());
	}

	@Override
	public Object visitPatternExpression(Cql2Parser.PatternExpressionContext ctx) {
		return parseLiteral(ctx.getText(), ctx.CASEI());
	}

	private static Literal<?> parseLiteral(String text, TerminalNode casei) {
		if (casei != null) {
			return new Literal<>(text.substring(7, text.length() - 2));
		}
		return new Literal<>(text.substring(1, text.length() - 1));
	}

	@Override
	public SpatialOperator visitSpatialPredicate(Cql2Parser.SpatialPredicateContext ctx) {
		String spatialFunctionType = ctx.SpatialFunction().getText().toUpperCase().substring(2);
		SpatialOperator.SubType type = SpatialOperator.SubType.valueOf(spatialFunctionType);
		switch (type) {
			case INTERSECTS:
				Expression propName = checkExpressionType((Expression) ctx.geomExpression().get(0).accept(this),
						FilterPropertyType.GEOMETRY);
				Geometry geometry = (Geometry) ctx.geomExpression().get(1).accept(this);
				return new Intersects(propName, geometry);
		}
		throw new Cql2UnsupportedExpressionException("Unsupported geometry type " + type);
	}

	@Override
	public Object visitPropertyName(Cql2Parser.PropertyNameContext ctx) {
		String text = ctx.getText();
		List<QName> filterPropWithSameLocalName = filterProperties.stream()
			.map(FilterProperty::getName)
			.filter(name -> name.getLocalPart().equals(text))
			.toList();
		if (!filterPropWithSameLocalName.isEmpty()) {
			if (filterPropWithSameLocalName.size() > 1)
				LOG.warn("Found multiple filter properties with name {}: {}. Use {}", text,
						filterPropWithSameLocalName.stream().map(QName::toString).collect(Collectors.joining()),
						filterPropWithSameLocalName.get(0));
			return new ValueReference(filterPropWithSameLocalName.get(0));
		}
		throw new IllegalArgumentException("Property with name " + text + " is not supported.");
	}

	@Override
	public Object visitGeomExpression(Cql2Parser.GeomExpressionContext ctx) {
		if (ctx.function() != null) {
			throw new Cql2UnsupportedExpressionException("functions are currently not supported as geomExpressions.");
		}
		if (ctx.propertyName() != null) {
			return ctx.propertyName().accept(this);
		}
		return ctx.spatialInstance().accept(this);
	}

	@Override
	public Point visitPointText(Cql2Parser.PointTextContext ctx) {
		return (Point) ctx.point().accept(this);
	}

	@Override
	public LineString visitLineStringText(Cql2Parser.LineStringTextContext ctx) {
		List<Point> points = new ArrayList<>();
		for (Cql2Parser.PointContext p : ctx.point()) {
			points.add((Point) p.accept(this));
		}
		GeometryFactory geometryFactory = new GeometryFactory();
		return geometryFactory.createLineString("ls", filterCrs, geometryFactory.createPoints(points));
	}

	@Override
	public LinearRing visitLinearRingText(Cql2Parser.LinearRingTextContext ctx) {
		List<Point> points = new ArrayList<>();
		for (Cql2Parser.PointContext p : ctx.point()) {
			points.add((Point) p.accept(this));
		}
		GeometryFactory geometryFactory = new GeometryFactory();
		return geometryFactory.createLinearRing("lr", filterCrs, geometryFactory.createPoints(points));
	}

	@Override
	public Object visitPolygonText(Cql2Parser.PolygonTextContext ctx) {
		LinearRing exteriorRing = null;
		List<Ring> interiorRings = new ArrayList<>();
		for (Cql2Parser.LinearRingTextContext linearRing : ctx.linearRingText()) {
			if (exteriorRing == null) {
				exteriorRing = (LinearRing) linearRing.accept(this);
			}
			else {
				interiorRings.add((LinearRing) linearRing.accept(this));
			}
		}
		GeometryFactory geometryFactory = new GeometryFactory();
		return geometryFactory.createPolygon("po", filterCrs, exteriorRing, interiorRings);
	}

	@Override
	public Object visitMultiPointText(Cql2Parser.MultiPointTextContext ctx) {
		List<Point> points = new ArrayList<>();
		for (Cql2Parser.PointTextContext pointTextContext : ctx.pointText()) {
			points.add((Point) pointTextContext.accept(this));
		}
		GeometryFactory geometryFactory = new GeometryFactory();
		return geometryFactory.createMultiPoint("mp", filterCrs, points);
	}

	@Override
	public Object visitMultiLineStringText(Cql2Parser.MultiLineStringTextContext ctx) {
		List<LineString> lineStrings = new ArrayList<>();
		for (Cql2Parser.LineStringTextContext lineStringTextContext : ctx.lineStringText()) {
			lineStrings.add((LineString) lineStringTextContext.accept(this));
		}
		GeometryFactory geometryFactory = new GeometryFactory();
		return geometryFactory.createMultiLineString("ml", filterCrs, lineStrings);
	}

	@Override
	public Object visitMultiPolygonText(Cql2Parser.MultiPolygonTextContext ctx) {
		List<Polygon> polygons = new ArrayList<>();
		for (Cql2Parser.PolygonTextContext polygonTextContext : ctx.polygonText()) {
			polygons.add((Polygon) polygonTextContext.accept(this));
		}
		GeometryFactory geometryFactory = new GeometryFactory();
		return geometryFactory.createMultiPolygon("mpol", filterCrs, polygons);
	}

	@Override
	public Object visitGeometryCollectionText(Cql2Parser.GeometryCollectionTextContext ctx) {
		List<Geometry> geometries = new ArrayList<>();
		for (Cql2Parser.GeometryLiteralContext geometryLiteralContext : ctx.geometryLiteral()) {
			geometries.add((Geometry) geometryLiteralContext.accept(this));
		}
		return geometries;
	}

	@Override
	public Object visitGeometryCollectionTaggedText(Cql2Parser.GeometryCollectionTaggedTextContext ctx) {
		List<Geometry> geometries = (List<Geometry>) ctx.geometryCollectionText().accept(this);
		GeometryFactory geometryFactory = new GeometryFactory();
		return geometryFactory.createMultiGeometry("gc", filterCrs, geometries);
	}

	@Override
	public Object visitBboxTaggedText(Cql2Parser.BboxTaggedTextContext ctx) {
		return ctx.bboxText().accept(this);
	}

	@Override
	public Object visitBboxText(Cql2Parser.BboxTextContext ctx) {
		Double minX = Double.valueOf(ctx.westBoundLon().getText());
		Double minY = Double.valueOf(ctx.southBoundLat().getText());
		Double maxX = Double.valueOf(ctx.eastBoundLon().getText());
		Double maxY = Double.valueOf(ctx.northBoundLat().getText());
		GeometryFactory geometryFactory = new GeometryFactory();
		return geometryFactory.createEnvelope(minX, minY, maxX, maxY, filterCrs);
	}

	@Override
	public Point visitPoint(Cql2Parser.PointContext ctx) {
		GeometryFactory geometryFactory = new GeometryFactory();
		double x = (double) ctx.xCoord().accept(this);
		double y = (double) ctx.yCoord().accept(this);
		double z = ctx.zCoord() != null ? (double) ctx.zCoord().accept(this) : 0;
		return geometryFactory.createPoint("p", x, y, z, filterCrs);
	}

	@Override
	public Double visitXCoord(Cql2Parser.XCoordContext ctx) {
		return Double.valueOf(ctx.getText());
	}

	@Override
	public Double visitYCoord(Cql2Parser.YCoordContext ctx) {
		return Double.valueOf(ctx.getText());
	}

	@Override
	public Double visitZCoord(Cql2Parser.ZCoordContext ctx) {
		return Double.valueOf(ctx.getText());
	}

	@Override
	public Object visitTemporalPredicate(Cql2Parser.TemporalPredicateContext ctx) {
		String temporalFunctionType = ctx.TemporalFunction().getText().substring(2);
		TemporalOperator.SubType type = TemporalOperator.SubType.valueOf(temporalFunctionType);
		switch (type) {
			case AFTER:
				Expression propName = checkExpressionType(
						(Expression) ctx.temporalExpression(0).propertyName().accept(this), DATE, DATE_TIME, TIME);
				Expression dateValue = (Expression) ctx.temporalExpression(1).temporalInstance().accept(this);
				return new After(propName, dateValue);
		}
		throw new Cql2UnsupportedExpressionException("Unsupported geometry type " + type);
	}

	@Override
	public Object visitTemporalInstance(Cql2Parser.TemporalInstanceContext ctx) {
		if (ctx.intervalInstance() != null)
			throw new Cql2UnsupportedExpressionException("intervalInstance are currently not supported.");
		PrimitiveValue primitiveValue = (PrimitiveValue) ctx.instantInstance().accept(this);
		return new Literal<>(primitiveValue, null);
	}

	@Override
	public Object visitInstantInstance(Cql2Parser.InstantInstanceContext ctx) {
		if (ctx.dateInstant() != null) {
			Object date = ctx.dateInstant().accept(this);
			return new PrimitiveValue(date, new PrimitiveType(BaseType.DATE));
		}
		if (ctx.timestampInstant() != null) {
			Object dateTime = ctx.timestampInstant().accept(this);
			return new PrimitiveValue(dateTime, new PrimitiveType(BaseType.DATE_TIME));
		}
		return super.visitInstantInstance(ctx);
	}

	@Override
	public Object visitDateInstant(Cql2Parser.DateInstantContext ctx) {
		String substring = ctx.getText().substring(6, ctx.getText().length() - 2);
		return ISO8601Converter.parseDate(substring);
	}

	@Override
	public Object visitTimestampInstant(Cql2Parser.TimestampInstantContext ctx) {
		return ISO8601Converter.parseDateTime(ctx.getText().substring(11, ctx.getText().length() - 2));
	}

	private Expression checkExpressionType(Expression expression, FilterPropertyType... filterPropertyTypes)
			throws IllegalArgumentException {
		if (expression instanceof ValueReference reference) {
			Optional<FilterProperty> filterProperty = filterProperties.stream()
				.filter(fp -> fp.getName().equals(reference.getAsQName()))
				.findFirst();
			if (filterProperty.isPresent() && (!UNKNOWN.equals(filterProperty.get().getType())
					&& Arrays.stream(filterPropertyTypes).noneMatch(fp -> fp == filterProperty.get().getType())))
				throw new IllegalArgumentException(
						"Property " + filterProperty.get().getName() + " is not of one of the expected types "
								+ Arrays.stream(filterPropertyTypes)
									.map(FilterPropertyType::name)
									.collect(Collectors.joining(",")));
		}
		return expression;
	}

}
