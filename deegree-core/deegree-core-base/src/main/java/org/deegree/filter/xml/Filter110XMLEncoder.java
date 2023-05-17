/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.filter.xml;

import static org.deegree.gml.GMLVersion.GML_31;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.comparison.PropertyIsBetween;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.comparison.PropertyIsGreaterThan;
import org.deegree.filter.comparison.PropertyIsGreaterThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLessThan;
import org.deegree.filter.comparison.PropertyIsLessThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.comparison.PropertyIsNotEqualTo;
import org.deegree.filter.comparison.PropertyIsNull;
import org.deegree.filter.expression.Add;
import org.deegree.filter.expression.Div;
import org.deegree.filter.expression.Function;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.Mul;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.expression.Sub;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.logical.Not;
import org.deegree.filter.logical.Or;
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
import org.deegree.geometry.io.DecimalCoordinateFormatter;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;

/**
 * Encodes {@link Filter} objects according to the Filter Encoding Specification 1.1.0.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class Filter110XMLEncoder {

	private static final Map<Expression.Type, QName> expressionTypeToElementName = new HashMap<Expression.Type, QName>();

	private static final Map<SpatialOperator.SubType, QName> spatialOperatorTypeToElementName = new HashMap<SpatialOperator.SubType, QName>();

	private static final Map<ComparisonOperator.SubType, QName> comparisonOperatorTypeToElementName = new HashMap<ComparisonOperator.SubType, QName>();

	private static final Map<LogicalOperator.SubType, QName> logicalOperatorTypeToElementName = new HashMap<LogicalOperator.SubType, QName>();

	static {
		// element name <-> expression type
		addElementToExpressionMapping(new QName(CommonNamespaces.OGCNS, "Add"), Expression.Type.ADD);
		addElementToExpressionMapping(new QName(CommonNamespaces.OGCNS, "Sub"), Expression.Type.SUB);
		addElementToExpressionMapping(new QName(CommonNamespaces.OGCNS, "Mul"), Expression.Type.MUL);
		addElementToExpressionMapping(new QName(CommonNamespaces.OGCNS, "Div"), Expression.Type.DIV);
		addElementToExpressionMapping(new QName(CommonNamespaces.OGCNS, "PropertyName"),
				Expression.Type.VALUE_REFERENCE);
		addElementToExpressionMapping(new QName(CommonNamespaces.OGCNS, "Function"), Expression.Type.FUNCTION);
		addElementToExpressionMapping(new QName(CommonNamespaces.OGCNS, "Literal"), Expression.Type.LITERAL);

		// element name <-> spatial operator type
		addElementToSpatialOperatorMapping(new QName(CommonNamespaces.OGCNS, "BBOX"), SpatialOperator.SubType.BBOX);
		addElementToSpatialOperatorMapping(new QName(CommonNamespaces.OGCNS, "Beyond"), SpatialOperator.SubType.BEYOND);
		addElementToSpatialOperatorMapping(new QName(CommonNamespaces.OGCNS, "Contains"),
				SpatialOperator.SubType.CONTAINS);
		addElementToSpatialOperatorMapping(new QName(CommonNamespaces.OGCNS, "Crosses"),
				SpatialOperator.SubType.CROSSES);
		addElementToSpatialOperatorMapping(new QName(CommonNamespaces.OGCNS, "Equals"), SpatialOperator.SubType.EQUALS);
		addElementToSpatialOperatorMapping(new QName(CommonNamespaces.OGCNS, "Disjoint"),
				SpatialOperator.SubType.DISJOINT);
		addElementToSpatialOperatorMapping(new QName(CommonNamespaces.OGCNS, "DWithin"),
				SpatialOperator.SubType.DWITHIN);
		addElementToSpatialOperatorMapping(new QName(CommonNamespaces.OGCNS, "Intersects"),
				SpatialOperator.SubType.INTERSECTS);
		addElementToSpatialOperatorMapping(new QName(CommonNamespaces.OGCNS, "Overlaps"),
				SpatialOperator.SubType.OVERLAPS);
		addElementToSpatialOperatorMapping(new QName(CommonNamespaces.OGCNS, "Touches"),
				SpatialOperator.SubType.TOUCHES);
		addElementToSpatialOperatorMapping(new QName(CommonNamespaces.OGCNS, "Within"), SpatialOperator.SubType.WITHIN);

		// element name <-> logical operator type
		addElementToLogicalOperatorMapping(new QName(CommonNamespaces.OGCNS, "And"), LogicalOperator.SubType.AND);
		addElementToLogicalOperatorMapping(new QName(CommonNamespaces.OGCNS, "Or"), LogicalOperator.SubType.OR);
		addElementToLogicalOperatorMapping(new QName(CommonNamespaces.OGCNS, "Not"), LogicalOperator.SubType.NOT);

		// element name <-> comparison operator type
		addElementToComparisonOperatorMapping(new QName(CommonNamespaces.OGCNS, "PropertyIsBetween"),
				ComparisonOperator.SubType.PROPERTY_IS_BETWEEN);
		addElementToComparisonOperatorMapping(new QName(CommonNamespaces.OGCNS, "PropertyIsEqualTo"),
				ComparisonOperator.SubType.PROPERTY_IS_EQUAL_TO);
		addElementToComparisonOperatorMapping(new QName(CommonNamespaces.OGCNS, "PropertyIsGreaterThan"),
				ComparisonOperator.SubType.PROPERTY_IS_GREATER_THAN);
		addElementToComparisonOperatorMapping(new QName(CommonNamespaces.OGCNS, "PropertyIsGreaterThanOrEqualTo"),
				ComparisonOperator.SubType.PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO);
		addElementToComparisonOperatorMapping(new QName(CommonNamespaces.OGCNS, "PropertyIsLessThan"),
				ComparisonOperator.SubType.PROPERTY_IS_LESS_THAN);
		addElementToComparisonOperatorMapping(new QName(CommonNamespaces.OGCNS, "PropertyIsLessThanOrEqualTo"),
				ComparisonOperator.SubType.PROPERTY_IS_LESS_THAN_OR_EQUAL_TO);
		addElementToComparisonOperatorMapping(new QName(CommonNamespaces.OGCNS, "PropertyIsLike"),
				ComparisonOperator.SubType.PROPERTY_IS_LIKE);
		addElementToComparisonOperatorMapping(new QName(CommonNamespaces.OGCNS, "PropertyIsNotEqualTo"),
				ComparisonOperator.SubType.PROPERTY_IS_NOT_EQUAL_TO);
		addElementToComparisonOperatorMapping(new QName(CommonNamespaces.OGCNS, "PropertyIsNull"),
				ComparisonOperator.SubType.PROPERTY_IS_NULL);

	}

	private static void addElementToExpressionMapping(QName elementName, Expression.Type type) {
		expressionTypeToElementName.put(type, elementName);
	}

	private static void addElementToSpatialOperatorMapping(QName elementName, SpatialOperator.SubType type) {
		spatialOperatorTypeToElementName.put(type, elementName);
	}

	private static void addElementToLogicalOperatorMapping(QName elementName, LogicalOperator.SubType type) {
		logicalOperatorTypeToElementName.put(type, elementName);
	}

	private static void addElementToComparisonOperatorMapping(QName elementName, ComparisonOperator.SubType type) {
		comparisonOperatorTypeToElementName.put(type, elementName);
	}

	/**
	 * Serializes the given {@link Filter} object to XML.
	 * @param filter <code>Filter</code> object to be serialized
	 * @param writer target of the xml stream
	 * @throws XMLStreamException
	 * @throws TransformationException
	 * @throws UnknownCRSException
	 */
	public static void export(Filter filter, XMLStreamWriter writer)
			throws XMLStreamException, UnknownCRSException, TransformationException {

		writer.setPrefix("ogc", "http://www.opengis.net/ogc");
		writer.writeStartElement(CommonNamespaces.OGCNS, "Filter");
		writer.writeNamespace("ogc", "http://www.opengis.net/ogc");
		writer.writeNamespace("gml", "http://www.opengis.net/gml");

		switch (filter.getType()) {
			case ID_FILTER:
				Collection<String> ids = ((IdFilter) filter).getMatchingIds();
				for (String id : ids) {
					writer.writeStartElement(CommonNamespaces.OGCNS, "GmlObjectId");
					writer.writeAttribute("http://www.opengis.net/gml", "id", id);
					writer.writeEndElement();
				}
				break;
			case OPERATOR_FILTER:
				export(((OperatorFilter) filter).getOperator(), writer);
				break;
		}
		writer.writeEndElement();
	}

	/**
	 * Serializes the given {@link Operator} object to XML.
	 * @param operator <code>BooleanOperator</code> object to be serialized
	 * @param writer target of the xml stream
	 * @throws XMLStreamException
	 * @throws TransformationException
	 * @throws UnknownCRSException
	 */
	private static void export(Operator operator, XMLStreamWriter writer)
			throws XMLStreamException, UnknownCRSException, TransformationException {
		switch (operator.getType()) {
			case COMPARISON:
				export((ComparisonOperator) operator, writer);
				break;
			case LOGICAL:
				export((LogicalOperator) operator, writer);
				break;
			case SPATIAL:
				export((SpatialOperator) operator, writer);
				break;
		}
	}

	/**
	 * Serializes the given {@link LogicalOperator} object to XML.
	 * @param operator <code>LogicalOperator</code> object to be serialized
	 * @param writer target of the xml stream
	 * @throws XMLStreamException
	 * @throws TransformationException
	 * @throws UnknownCRSException
	 */
	private static void export(LogicalOperator operator, XMLStreamWriter writer)
			throws XMLStreamException, UnknownCRSException, TransformationException {

		QName elementName = logicalOperatorTypeToElementName.get(operator.getSubType());
		writer.writeStartElement(elementName.getNamespaceURI(), elementName.getLocalPart());

		switch (operator.getSubType()) {
			case AND:
				And andOp = (And) operator;
				for (int i = 0; i < andOp.getSize(); i++) {
					export(andOp.getParameter(i), writer);
				}
				break;
			case OR:
				Or orOp = (Or) operator;
				for (int i = 0; i < orOp.getSize(); i++) {
					export(orOp.getParameter(i), writer);
				}
				break;
			case NOT:
				export(((Not) operator).getParameter(), writer);
				break;
		}

		writer.writeEndElement();
	}

	/**
	 * Serializes the given {@link ComparisonOperator} object to XML.
	 * @param operator <code>ComparisonOperator</code> object to be serialized
	 * @param writer target of the xml stream
	 * @throws XMLStreamException
	 */
	private static void export(ComparisonOperator operator, XMLStreamWriter writer) throws XMLStreamException {

		QName elementName = comparisonOperatorTypeToElementName.get(operator.getSubType());
		writer.writeStartElement(elementName.getNamespaceURI(), elementName.getLocalPart());

		switch (operator.getSubType()) {
			case PROPERTY_IS_BETWEEN:
				PropertyIsBetween isBetween = (PropertyIsBetween) operator;
				export(isBetween.getExpression(), writer);
				writer.writeStartElement(CommonNamespaces.OGCNS, "LowerBoundary");
				export(isBetween.getLowerBoundary(), writer);
				writer.writeEndElement();
				writer.writeStartElement(CommonNamespaces.OGCNS, "UpperBoundary");
				export(isBetween.getUpperBoundary(), writer);
				writer.writeEndElement();
				break;
			case PROPERTY_IS_EQUAL_TO:
				export(((PropertyIsEqualTo) operator).getParameter1(), writer);
				export(((PropertyIsEqualTo) operator).getParameter2(), writer);
				break;
			case PROPERTY_IS_GREATER_THAN:
				export(((PropertyIsGreaterThan) operator).getParameter1(), writer);
				export(((PropertyIsGreaterThan) operator).getParameter2(), writer);
				break;
			case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
				export(((PropertyIsGreaterThanOrEqualTo) operator).getParameter1(), writer);
				export(((PropertyIsGreaterThanOrEqualTo) operator).getParameter2(), writer);
				break;
			case PROPERTY_IS_LESS_THAN:
				export(((PropertyIsLessThan) operator).getParameter1(), writer);
				export(((PropertyIsLessThan) operator).getParameter2(), writer);
				break;
			case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
				export(((PropertyIsLessThanOrEqualTo) operator).getParameter1(), writer);
				export(((PropertyIsLessThanOrEqualTo) operator).getParameter2(), writer);
				break;
			case PROPERTY_IS_LIKE:
				PropertyIsLike isLikeOperator = (PropertyIsLike) operator;
				writer.writeAttribute("wildCard", isLikeOperator.getWildCard());
				writer.writeAttribute("singleChar", isLikeOperator.getSingleChar());
				writer.writeAttribute("escapeChar", isLikeOperator.getEscapeChar());
				if (isLikeOperator.isMatchCase() != null) {
					writer.writeAttribute("matchCase", "" + isLikeOperator.isMatchCase());
				}
				export(isLikeOperator.getExpression(), writer);
				export(isLikeOperator.getPattern(), writer);
				break;
			case PROPERTY_IS_NOT_EQUAL_TO:
				export(((PropertyIsNotEqualTo) operator).getParameter1(), writer);
				export(((PropertyIsNotEqualTo) operator).getParameter2(), writer);
				break;
			case PROPERTY_IS_NULL:
				export(((PropertyIsNull) operator).getPropertyName(), writer);
				break;
		}

		writer.writeEndElement();
	}

	/**
	 * Serializes the given {@link SpatialOperator} object to XML.
	 * @param operator <code>SpatialOperator</code> object to be serialized
	 * @param writer target of the xml stream
	 * @throws XMLStreamException
	 * @throws TransformationException
	 * @throws UnknownCRSException
	 */
	private static void export(SpatialOperator operator, XMLStreamWriter writer)
			throws XMLStreamException, UnknownCRSException, TransformationException {

		QName elementName = spatialOperatorTypeToElementName.get(operator.getSubType());
		writer.writeStartElement(elementName.getNamespaceURI(), elementName.getLocalPart());

		GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter(GMLVersion.GML_31, writer);
		gmlWriter.setCoordinateFormatter(new DecimalCoordinateFormatter(3));
		Map<String, String> bindings = new HashMap<String, String>();
		bindings.put("gml", GML_31.getNamespace());
		writer.writeNamespace("gml", GML_31.getNamespace());
		gmlWriter.setNamespaceBindings(bindings);
		// gmlWriter.setLocalXLinkTemplate( "#{}" );
		// gmlWriter.setXLinkDepth( 0 );

		ValueReference propertyName = (ValueReference) operator.getParam1();
		Geometry geometry = operator.getGeometry();
		ValueReference secondParam = operator.getValueReference();
		Measure distance = null;

		switch (operator.getSubType()) {
			case BEYOND:
				distance = ((Beyond) operator).getDistance();
				break;
			case DWITHIN:
				distance = ((DWithin) operator).getDistance();
				break;
		}

		// exporting the comparable geometry property
		export(propertyName, writer);

		// serializing the geometry
		if (geometry != null) {
			gmlWriter.setOutputCrs(geometry.getCoordinateSystem());
			gmlWriter.write(geometry);
		}

		// or value reference
		if (secondParam != null)
			export(secondParam, writer);

		if (distance != null) { // in case of Beyond- and DWithin-operators export their
								// distance variable
			QName distanceElementName = new QName(CommonNamespaces.OGCNS, "Distance");
			writer.writeStartElement(distanceElementName.getNamespaceURI(), distanceElementName.getLocalPart());
			writer.writeAttribute("units", distance.getUomUri());
			writer.writeCharacters(distance.getValue().toString());
			writer.writeEndElement();
		}
		writer.writeEndElement();
	}

	/**
	 * Serializes the given {@link Expression} object to XML.
	 * @param expression <code>Expression</code> object to be serialized
	 * @param writer target of the xml stream
	 * @throws XMLStreamException
	 */
	public static void export(Expression expression, XMLStreamWriter writer) throws XMLStreamException {

		QName elementName = expressionTypeToElementName.get(expression.getType());
		boolean prefixBound = (writer.getPrefix(elementName.getNamespaceURI()) != null) ? true : false;
		if (prefixBound) {
			writer.writeStartElement(elementName.getNamespaceURI(), elementName.getLocalPart());
		}
		else {
			writer.writeStartElement(elementName.getPrefix(), elementName.getLocalPart(),
					elementName.getNamespaceURI());
			writer.writeNamespace(elementName.getPrefix(), elementName.getNamespaceURI());
		}

		switch (expression.getType()) {
			case VALUE_REFERENCE:
				ValueReference propertyName = (ValueReference) expression;
				NamespaceBindings nsBindings = propertyName.getNsContext();
				Iterator<String> prefixIter = nsBindings.getPrefixes();
				while (prefixIter.hasNext()) {
					String prefix = prefixIter.next();
					String ns = nsBindings.getNamespaceURI(prefix);
					writer.writeNamespace(prefix, ns);
				}
				writer.writeCharacters(propertyName.getAsText());
				break;
			case LITERAL:
				// TODO handle complex literals
				writer.writeCharacters(((Literal<?>) expression).getValue().toString());
				break;
			case FUNCTION:
				Function function = (Function) expression;
				writer.writeAttribute("name", function.getName());
				for (Expression param : function.getParameters()) {
					export(param, writer);
				}
				break;
			case CUSTOM:
				throw new UnsupportedOperationException("Exporting of custom expressions is not implemented yet.");
			case ADD:
				export(((Add) expression).getParameter1(), writer);
				export(((Add) expression).getParameter2(), writer);
				break;
			case SUB:
				export(((Sub) expression).getParameter1(), writer);
				export(((Sub) expression).getParameter2(), writer);
				break;
			case MUL:
				export(((Mul) expression).getParameter1(), writer);
				export(((Mul) expression).getParameter2(), writer);
				break;
			case DIV:
				export(((Div) expression).getParameter1(), writer);
				export(((Div) expression).getParameter2(), writer);
				break;
		}
		writer.writeEndElement();
	}

}