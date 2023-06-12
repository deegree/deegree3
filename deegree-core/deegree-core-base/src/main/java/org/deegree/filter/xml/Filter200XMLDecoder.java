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

import static java.util.Collections.singleton;
import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.tom.datetime.ISO8601Converter.parseDateTime;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getAttributeValueAsBoolean;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getAttributeValueAsQName;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getRequiredAttributeValue;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.require;
import static org.deegree.commons.xml.stax.XMLStreamUtils.requireNextTag;
import static org.deegree.commons.xml.stax.XMLStreamUtils.requireStartElement;
import static org.deegree.filter.MatchAction.ALL;
import static org.deegree.gml.GMLVersion.GML_32;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.utils.ArrayUtils;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPathUtils;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.MatchAction;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.ResourceId;
import org.deegree.filter.comparison.BinaryComparisonOperator;
import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.comparison.ComparisonOperator.SubType;
import org.deegree.filter.comparison.PropertyIsBetween;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.comparison.PropertyIsGreaterThan;
import org.deegree.filter.comparison.PropertyIsGreaterThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLessThan;
import org.deegree.filter.comparison.PropertyIsLessThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.comparison.PropertyIsNil;
import org.deegree.filter.comparison.PropertyIsNotEqualTo;
import org.deegree.filter.comparison.PropertyIsNull;
import org.deegree.filter.expression.Function;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.expression.custom.CustomExpression;
import org.deegree.filter.expression.custom.CustomExpressionManager;
import org.deegree.filter.function.FunctionManager;
import org.deegree.filter.function.FunctionProvider;
import org.deegree.filter.i18n.Messages;
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
import org.deegree.filter.temporal.After;
import org.deegree.filter.temporal.AnyInteracts;
import org.deegree.filter.temporal.Before;
import org.deegree.filter.temporal.Begins;
import org.deegree.filter.temporal.BegunBy;
import org.deegree.filter.temporal.During;
import org.deegree.filter.temporal.EndedBy;
import org.deegree.filter.temporal.Ends;
import org.deegree.filter.temporal.Meets;
import org.deegree.filter.temporal.MetBy;
import org.deegree.filter.temporal.OverlappedBy;
import org.deegree.filter.temporal.TContains;
import org.deegree.filter.temporal.TEquals;
import org.deegree.filter.temporal.TOverlaps;
import org.deegree.filter.temporal.TemporalOperator;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.geometry.GMLGeometryReader;
import org.deegree.gml.geometry.GMLGeometryVersionHelper;
import org.deegree.time.TimeObject;
import org.deegree.time.gml.reader.GmlTimeGeometricPrimitiveReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decodes XML fragments that comply to the
 * <a href="http://www.opengeospatial.org/standards/filter">OGC Filter Encoding
 * Specification</a> 2.0.0.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class Filter200XMLDecoder {

	private static final Logger LOG = LoggerFactory.getLogger(Filter200XMLDecoder.class);

	private static final String FES_NS = "http://www.opengis.net/fes/2.0";

	private static final QName RESOURCE_ID_ELEMENT = new QName(FES_NS, "ResourceId");

	private static final Map<Expression.Type, QName> expressionTypeToElementName = new HashMap<Expression.Type, QName>();

	private static final Map<QName, Expression.Type> elementNameToExpressionType = new HashMap<QName, Expression.Type>();

	private static final Map<QName, Operator.Type> elementNameToOperatorType = new HashMap<QName, Operator.Type>();

	private static final Map<QName, SpatialOperator.SubType> elementNameToSpatialOperatorType = new HashMap<QName, SpatialOperator.SubType>();

	private static final Map<SpatialOperator.SubType, QName> spatialOperatorTypeToElementName = new HashMap<SpatialOperator.SubType, QName>();

	private static final Map<QName, ComparisonOperator.SubType> elementNameToComparisonOperatorType = new HashMap<QName, ComparisonOperator.SubType>();

	private static final Map<ComparisonOperator.SubType, QName> comparisonOperatorTypeToElementName = new HashMap<ComparisonOperator.SubType, QName>();

	private static final Map<QName, LogicalOperator.SubType> elementNameToLogicalOperatorType = new HashMap<QName, LogicalOperator.SubType>();

	private static final Map<LogicalOperator.SubType, QName> logicalOperatorTypeToElementName = new HashMap<LogicalOperator.SubType, QName>();

	private static final Map<QName, TemporalOperator.SubType> elementNameToTemporalOperatorType = new HashMap<QName, TemporalOperator.SubType>();

	private static final Map<TemporalOperator.SubType, QName> temporalOperatorTypeToElementName = new HashMap<TemporalOperator.SubType, QName>();

	static {

		// element name <-> expression type
		addElementToExpressionMapping(new QName(FES_NS, "ValueReference"), Expression.Type.VALUE_REFERENCE);
		addElementToExpressionMapping(new QName(FES_NS, "Function"), Expression.Type.FUNCTION);
		addElementToExpressionMapping(new QName(FES_NS, "Literal"), Expression.Type.LITERAL);

		// element name <-> expression type (custom expressions)
		// TODO cope with workspace re-initialization
		for (CustomExpression ce : CustomExpressionManager.getCustomExpressions().values()) {
			addElementToExpressionMapping(ce.getElementName(), Expression.Type.CUSTOM);
		}

		// element name <-> spatial operator type
		addElementToSpatialOperatorMapping(new QName(FES_NS, "BBOX"), SpatialOperator.SubType.BBOX);
		addElementToSpatialOperatorMapping(new QName(FES_NS, "Beyond"), SpatialOperator.SubType.BEYOND);
		addElementToSpatialOperatorMapping(new QName(FES_NS, "Contains"), SpatialOperator.SubType.CONTAINS);
		addElementToSpatialOperatorMapping(new QName(FES_NS, "Crosses"), SpatialOperator.SubType.CROSSES);
		addElementToSpatialOperatorMapping(new QName(FES_NS, "Equals"), SpatialOperator.SubType.EQUALS);
		addElementToSpatialOperatorMapping(new QName(FES_NS, "Disjoint"), SpatialOperator.SubType.DISJOINT);
		addElementToSpatialOperatorMapping(new QName(FES_NS, "DWithin"), SpatialOperator.SubType.DWITHIN);
		addElementToSpatialOperatorMapping(new QName(FES_NS, "Intersects"), SpatialOperator.SubType.INTERSECTS);
		addElementToSpatialOperatorMapping(new QName(FES_NS, "Overlaps"), SpatialOperator.SubType.OVERLAPS);
		addElementToSpatialOperatorMapping(new QName(FES_NS, "Touches"), SpatialOperator.SubType.TOUCHES);
		addElementToSpatialOperatorMapping(new QName(FES_NS, "Within"), SpatialOperator.SubType.WITHIN);

		// element name <-> logical operator type
		addElementToLogicalOperatorMapping(new QName(FES_NS, "And"), LogicalOperator.SubType.AND);
		addElementToLogicalOperatorMapping(new QName(FES_NS, "Or"), LogicalOperator.SubType.OR);
		addElementToLogicalOperatorMapping(new QName(FES_NS, "Not"), LogicalOperator.SubType.NOT);

		// element name <-> comparison operator type
		addElementToComparisonOperatorMapping(new QName(FES_NS, "PropertyIsBetween"),
				ComparisonOperator.SubType.PROPERTY_IS_BETWEEN);
		addElementToComparisonOperatorMapping(new QName(FES_NS, "PropertyIsEqualTo"),
				ComparisonOperator.SubType.PROPERTY_IS_EQUAL_TO);
		addElementToComparisonOperatorMapping(new QName(FES_NS, "PropertyIsGreaterThan"),
				ComparisonOperator.SubType.PROPERTY_IS_GREATER_THAN);
		addElementToComparisonOperatorMapping(new QName(FES_NS, "PropertyIsGreaterThanOrEqualTo"),
				ComparisonOperator.SubType.PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO);
		addElementToComparisonOperatorMapping(new QName(FES_NS, "PropertyIsLessThan"),
				ComparisonOperator.SubType.PROPERTY_IS_LESS_THAN);
		addElementToComparisonOperatorMapping(new QName(FES_NS, "PropertyIsLessThanOrEqualTo"),
				ComparisonOperator.SubType.PROPERTY_IS_LESS_THAN_OR_EQUAL_TO);
		addElementToComparisonOperatorMapping(new QName(FES_NS, "PropertyIsLike"),
				ComparisonOperator.SubType.PROPERTY_IS_LIKE);
		addElementToComparisonOperatorMapping(new QName(FES_NS, "PropertyIsNotEqualTo"),
				ComparisonOperator.SubType.PROPERTY_IS_NOT_EQUAL_TO);
		addElementToComparisonOperatorMapping(new QName(FES_NS, "PropertyIsNull"),
				ComparisonOperator.SubType.PROPERTY_IS_NULL);
		addElementToComparisonOperatorMapping(new QName(FES_NS, "PropertyIsNil"),
				ComparisonOperator.SubType.PROPERTY_IS_NIL);

		// element name <-> temporal operator type
		addElementToTemporalOperatorMapping("After", TemporalOperator.SubType.AFTER);
		addElementToTemporalOperatorMapping("AnyInteracts", TemporalOperator.SubType.ANYINTERACTS);
		addElementToTemporalOperatorMapping("Before", TemporalOperator.SubType.BEFORE);
		addElementToTemporalOperatorMapping("Begins", TemporalOperator.SubType.BEGINS);
		addElementToTemporalOperatorMapping("BegunBy", TemporalOperator.SubType.BEGUNBY);
		addElementToTemporalOperatorMapping("During", TemporalOperator.SubType.DURING);
		addElementToTemporalOperatorMapping("EndedBy", TemporalOperator.SubType.ENDEDBY);
		addElementToTemporalOperatorMapping("Meets", TemporalOperator.SubType.MEETS);
		addElementToTemporalOperatorMapping("MetBy", TemporalOperator.SubType.METBY);
		addElementToTemporalOperatorMapping("OverlappedBy", TemporalOperator.SubType.OVERLAPPEDBY);
		addElementToTemporalOperatorMapping("TContains", TemporalOperator.SubType.TCONTAINS);
		addElementToTemporalOperatorMapping("TEquals", TemporalOperator.SubType.TEQUALS);
		addElementToTemporalOperatorMapping("TOverlaps", TemporalOperator.SubType.TOVERLAPS);
	}

	private static void addElementToExpressionMapping(QName elementName, Expression.Type type) {
		elementNameToExpressionType.put(elementName, type);
		expressionTypeToElementName.put(type, elementName);
	}

	private static void addElementToSpatialOperatorMapping(QName elementName, SpatialOperator.SubType type) {
		elementNameToOperatorType.put(elementName, Operator.Type.SPATIAL);
		elementNameToSpatialOperatorType.put(elementName, type);
		spatialOperatorTypeToElementName.put(type, elementName);
	}

	private static void addElementToLogicalOperatorMapping(QName elementName, LogicalOperator.SubType type) {
		elementNameToOperatorType.put(elementName, Operator.Type.LOGICAL);
		elementNameToLogicalOperatorType.put(elementName, type);
		logicalOperatorTypeToElementName.put(type, elementName);
	}

	private static void addElementToComparisonOperatorMapping(QName elementName, ComparisonOperator.SubType type) {
		elementNameToOperatorType.put(elementName, Operator.Type.COMPARISON);
		elementNameToComparisonOperatorType.put(elementName, type);
		comparisonOperatorTypeToElementName.put(type, elementName);
	}

	private static void addElementToTemporalOperatorMapping(String localName, TemporalOperator.SubType type) {
		QName elName = new QName(FES_NS, localName);
		elementNameToOperatorType.put(elName, Operator.Type.TEMPORAL);
		elementNameToTemporalOperatorType.put(elName, type);
		temporalOperatorTypeToElementName.put(type, elName);
	}

	/**
	 * Returns the object representation for the given <code>fes:Filter</code> element
	 * event that the cursor of the given <code>XMLStreamReader</code> points at.
	 * <ul>
	 * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;fes:Filter&gt;)</li>
	 * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code>
	 * event (&lt;/fes:Filter&gt;)</li>
	 * </ul>
	 * @param xmlStream must not be <code>null</code> and cursor must point at the
	 * <code>START_ELEMENT</code> event (&lt;fes:Filter&gt;), points at the corresponding
	 * <code>END_ELEMENT</code> event (&lt;/fes:Filter&gt;) afterwards
	 * @return corresponding {@link Filter} object, never <code>null</code>
	 * @throws XMLParsingException if the element is not a valid "fes:Filter" element
	 * @throws XMLStreamException
	 */
	public static Filter parse(XMLStreamReader xmlStream) throws XMLParsingException, XMLStreamException {

		Filter filter = null;
		xmlStream.require(START_ELEMENT, FES_NS, "Filter");
		nextElement(xmlStream);
		if (xmlStream.getEventType() != START_ELEMENT) {
			throw new XMLParsingException(xmlStream,
					Messages.getMessage("FILTER_PARSER_FILTER_EMPTY", new QName(FES_NS, "Filter")));
		}
		QName elementName = xmlStream.getName();
		if (RESOURCE_ID_ELEMENT.equals(elementName)) {
			LOG.debug("Building id filter");
			filter = parseIdFilter(xmlStream);
		}
		else {
			LOG.debug("Building operator filter");
			Operator rootOperator = parseOperator(xmlStream);
			filter = new OperatorFilter(rootOperator);
			nextElement(xmlStream);
		}

		xmlStream.require(END_ELEMENT, FES_NS, "Filter");
		return filter;
	}

	/**
	 * Returns the object representation for the given <code>fes:expression</code> element
	 * event that the cursor of the associated <code>XMLStreamReader</code> points at.
	 * <p>
	 * The element must be one of the following:
	 * <ul>
	 * <li>fes:ValueReference</li>
	 * <li>fes:Literal</li>
	 * <li>fes:Function</li>
	 * <li>substitution for fes:expression (handled by {@link CustomExpression}
	 * instance)</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <ul>
	 * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;fes:expression&gt;)</li>
	 * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code>
	 * event (&lt;/fes:expression&gt;)</li>
	 * </ul>
	 * </p>
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;fes:expression&gt;), points at the corresponding <code>END_ELEMENT</code>
	 * event (&lt;/fes:expression&gt;) afterwards
	 * @return corresponding {@link Expression} object, never <code>null</code>
	 * @throws XMLParsingException if the element is not a valid "fes:expression" element
	 * @throws XMLStreamException
	 */
	public static Expression parseExpression(XMLStreamReader xmlStream) throws XMLStreamException {

		Expression expression = null;

		// check if element name is a valid expression element
		require(xmlStream, START_ELEMENT);
		Expression.Type type = elementNameToExpressionType.get(xmlStream.getName());
		if (type == null) {
			String msg = Messages.getMessage("FILTER_PARSER_UNEXPECTED_ELEMENT", xmlStream.getName(),
					elemNames(Expression.Type.class, expressionTypeToElementName));
			throw new XMLParsingException(xmlStream, msg);
		}
		switch (type) {
			case VALUE_REFERENCE: {
				expression = parseValueReference(xmlStream, false);
				break;
			}
			case LITERAL: {
				expression = parseLiteral(xmlStream);
				break;
			}
			case FUNCTION: {
				expression = parseFunction(xmlStream);
				break;
			}
			case CUSTOM: {
				expression = parseCustomExpression(xmlStream);
				break;
			}
		}
		return expression;
	}

	/**
	 * Returns the object representation for the given <code>fes:Function</code> element
	 * event that the cursor of the associated <code>XMLStreamReader</code> points at.
	 * <p>
	 * <ul>
	 * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;fes:Function&gt;)</li>
	 * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code>
	 * event (&lt;/fes:Function&gt;)</li>
	 * </ul>
	 * </p>
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;fes:Function&gt;), points at the corresponding <code>END_ELEMENT</code> event
	 * (&lt;/fes:Function&gt;) afterwards
	 * @return corresponding {@link Function} object, never <code>null</code>
	 * @throws XMLParsingException if the element is not a valid "ogc:Function" element
	 * @throws XMLStreamException
	 */
	public static Function parseFunction(XMLStreamReader xmlStream) throws XMLStreamException {

		xmlStream.require(START_ELEMENT, FES_NS, "Function");
		String name = getRequiredAttributeValue(xmlStream, "name");
		nextElement(xmlStream);
		List<Expression> params = new ArrayList<Expression>();
		while (xmlStream.getEventType() == START_ELEMENT) {
			params.add(parseExpression(xmlStream));
			nextElement(xmlStream);
		}
		xmlStream.require(END_ELEMENT, FES_NS, "Function");

		Function function = null;
		FunctionProvider cf = FunctionManager.getFunctionProvider(name);
		if (cf != null) {
			function = cf.create(params);
		}
		else {
			function = new Function(name, params);
		}
		return function;
	}

	/**
	 * Returns the object representation for the custom <code>fes:expression</code>
	 * substitution element event that the cursor of the given
	 * <code>XMLStreamReader</code> points at.
	 * <p>
	 * <ul>
	 * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;fes:expression&gt;)</li>
	 * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code>
	 * event (&lt;/fes:expression&gt;)</li>
	 * </ul>
	 * </p>
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;fes:expression&gt;), points at the corresponding <code>END_ELEMENT</code>
	 * event (&lt;/fes:expression&gt;) afterwards
	 * @return corresponding {@link CustomExpression} object
	 * @throws XMLParsingException if the element is not a known or valid custom
	 * "fes:expression" element
	 * @throws XMLStreamException
	 */
	public static CustomExpression parseCustomExpression(XMLStreamReader xmlStream) throws XMLStreamException {

		CustomExpression expr = CustomExpressionManager.getExpression(xmlStream.getName());
		if (expr == null) {
			String msg = Messages.getMessage("FILTER_PARSER_UNKNOWN_CUSTOM_EXPRESSION", xmlStream.getName());
			throw new XMLParsingException(xmlStream, msg);
		}
		return expr.parse200(xmlStream);
	}

	/**
	 * Returns the object representation for the given <code>fes:comparisonOps</code>
	 * element event that the cursor of the given <code>XMLStreamReader</code> points at.
	 * <p>
	 * The element must be one of the following:
	 * <ul>
	 * <li>fes:PropertyIsEqualTo</li>
	 * <li>fes:PropertyIsGreaterThan</li>
	 * <li>fes:PropertyIsGreaterThanOrEqualTo</li>
	 * <li>fes:PropertyIsLessThan</li>
	 * <li>fes:PropertyIsLessThanOrEqualTo</li>
	 * <li>fes:PropertyIsNotEqualTo</li>
	 * <li>fes:PropertyIsBetween</li>
	 * <li>fes:PropertyIsLike</li>
	 * <li>fes:PropertyIsNull</li>
	 * <li>fes:PropertyIsNil</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <ul>
	 * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;ogc:comparisonOps&gt;)</li>
	 * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code>
	 * event (&lt;/ogc:comparisonOps&gt;)</li>
	 * </ul>
	 * </p>
	 * @param xmlStream must not be <code>null</code> and cursor must point at the
	 * <code>START_ELEMENT</code> event (&lt;fes:comparisonOps&gt;), points at the
	 * corresponding <code>END_ELEMENT</code> event (&lt;/fes:comparisonOps&gt;)
	 * afterwards
	 * @return corresponding {@link ComparisonOperator} object, never <code>null</code>
	 * @throws XMLParsingException if the element is not a valid "ogc:comparisonOps"
	 * element
	 * @throws XMLStreamException
	 */
	public static ComparisonOperator parseComparisonOperator(XMLStreamReader xmlStream) throws XMLStreamException {

		ComparisonOperator comparisonOperator = null;

		// check if element name is a valid comparison operator element
		ComparisonOperator.SubType type = elementNameToComparisonOperatorType.get(xmlStream.getName());
		if (type == null) {
			String msg = Messages.getMessage("FILTER_PARSER_UNEXPECTED_ELEMENT", xmlStream.getName(),
					elemNames(ComparisonOperator.SubType.class, comparisonOperatorTypeToElementName));
			throw new XMLParsingException(xmlStream, msg);
		}

		switch (type) {
			case PROPERTY_IS_EQUAL_TO:
			case PROPERTY_IS_GREATER_THAN:
			case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
			case PROPERTY_IS_LESS_THAN:
			case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
			case PROPERTY_IS_NOT_EQUAL_TO:
				comparisonOperator = parseBinaryComparisonOperator(xmlStream, type);
				break;
			case PROPERTY_IS_BETWEEN:
				comparisonOperator = parsePropertyIsBetweenOperator(xmlStream);
				break;
			case PROPERTY_IS_LIKE:
				comparisonOperator = parsePropertyIsLikeOperator(xmlStream);
				break;
			case PROPERTY_IS_NULL:
				comparisonOperator = parsePropertyIsNullOperator(xmlStream);
				break;
			case PROPERTY_IS_NIL:
				comparisonOperator = parsePropertyIsNilOperator(xmlStream);
				break;
		}
		return comparisonOperator;
	}

	/**
	 * Returns the object representation for the given <code>fes:temporalOps</code>
	 * element event that the cursor of the given <code>XMLStreamReader</code> points at.
	 * <p>
	 * The element must be one of the following:
	 * <ul>
	 * <li>fes:After</li>
	 * <li>fes:AnyInteracts</li>
	 * <li>fes:Before</li>
	 * <li>fes:Begins</li>
	 * <li>fes:BegunBy</li>
	 * <li>fes:During</li>
	 * <li>fes:Ends</li>
	 * <li>fes:EndedBy</li>
	 * <li>fes:Meets</li>
	 * <li>fes:MetBy</li>
	 * <li>fes:Overlapped</li>
	 * <li>fes:TContains</li>
	 * <li>fes:TEquals</li>
	 * <li>fes:TOverlaps</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <ul>
	 * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;fes:temporalOps&gt;)</li>
	 * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code>
	 * event (&lt;/fes:temporalOps&gt;)</li>
	 * </ul>
	 * </p>
	 * @param xmlStream must not be <code>null</code> and cursor must point at the
	 * <code>START_ELEMENT</code> event (&lt;fes:temporalOps&gt;), points at the
	 * corresponding <code>END_ELEMENT</code> event (&lt;/fes:temporalOps&gt;) afterwards
	 * @return corresponding {@link TemporalOperator} object, never <code>null</code>
	 * @throws XMLParsingException if the element is not a valid "ogc:temporalOps" element
	 * @throws XMLStreamException
	 */
	public static TemporalOperator parseTemporalOperator(final XMLStreamReader xmlStream) throws XMLStreamException {
		final TemporalOperator.SubType type = checkTemporalOperatorName(xmlStream);
		requireNextTag(xmlStream, START_ELEMENT);
		final Expression param1 = parseExpression(xmlStream);
		requireNextTag(xmlStream, START_ELEMENT);
		Expression param2 = null;
		final GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_32, xmlStream);
		final GmlTimeGeometricPrimitiveReader timeReader = new GmlTimeGeometricPrimitiveReader(gmlReader);
		final QName elName = xmlStream.getName();
		if (elName.getLocalPart().equals("TimeInstant") || elName.getLocalPart().equals("TimePeriod")) {
			final TimeObject to = timeReader.read(xmlStream);
			param2 = new Literal<TimeObject>(to, elName);
		}
		else {
			param2 = parseExpression(xmlStream);
		}
		requireNextTag(xmlStream, END_ELEMENT);
		TemporalOperator temporalOperator = null;
		switch (type) {
			case AFTER:
				return new After(param1, param2);
			case ANYINTERACTS:
				return new AnyInteracts(param1, param2);
			case BEFORE:
				return new Before(param1, param2);
			case BEGINS:
				return new Begins(param1, param2);
			case BEGUNBY:
				return new BegunBy(param1, param2);
			case DURING:
				return new During(param1, param2);
			case ENDEDBY:
				return new EndedBy(param1, param2);
			case ENDS:
				return new Ends(param1, param2);
			case MEETS:
				return new Meets(param1, param2);
			case METBY:
				return new MetBy(param1, param2);
			case OVERLAPPEDBY:
				return new OverlappedBy(param1, param2);
			case TCONTAINS:
				return new TContains(param1, param2);
			case TEQUALS:
				return new TEquals(param1, param2);
			case TOVERLAPS:
				return new TOverlaps(param1, param2);
		}
		throw new RuntimeException();
	}

	private static TemporalOperator.SubType checkTemporalOperatorName(final XMLStreamReader xmlStream) {
		TemporalOperator.SubType type = elementNameToTemporalOperatorType.get(xmlStream.getName());
		if (type == null) {
			String msg = Messages.getMessage("FILTER_PARSER_UNEXPECTED_ELEMENT", xmlStream.getName(),
					elemNames(TemporalOperator.SubType.class, temporalOperatorTypeToElementName));
			throw new XMLParsingException(xmlStream, msg);
		}
		return type;
	}

	private static Operator parseOperator(XMLStreamReader xmlStream) throws XMLStreamException {
		Operator operator = null;

		// check if element name is a valid operator element
		Operator.Type type = elementNameToOperatorType.get(xmlStream.getName());
		if (type == null) {
			String expectedList = elemNames(LogicalOperator.SubType.class, logicalOperatorTypeToElementName) + ", "
					+ elemNames(SpatialOperator.SubType.class, spatialOperatorTypeToElementName) + ", "
					+ elemNames(ComparisonOperator.SubType.class, comparisonOperatorTypeToElementName) + ","
					+ elemNames(TemporalOperator.SubType.class, temporalOperatorTypeToElementName);
			String msg = Messages.getMessage("FILTER_PARSER_UNEXPECTED_ELEMENT", xmlStream.getName(), expectedList);
			throw new XMLParsingException(xmlStream, msg);
		}

		switch (type) {
			case COMPARISON:
				LOG.debug("Building comparison operator");
				operator = parseComparisonOperator(xmlStream);
				break;
			case LOGICAL:
				LOG.debug("Building logical operator");
				operator = parseLogicalOperator(xmlStream);
				break;
			case SPATIAL:
				LOG.debug("Building spatial operator");
				operator = parseSpatialOperator(xmlStream);
				break;
			case TEMPORAL: {
				LOG.debug("Building temporal operator");
				operator = parseTemporalOperator(xmlStream);
				break;
			}
		}
		return operator;
	}

	private static IdFilter parseIdFilter(XMLStreamReader xmlStream) throws XMLStreamException {

		List<ResourceId> selectedIds = new ArrayList<ResourceId>();
		while (xmlStream.getEventType() == START_ELEMENT) {
			selectedIds.add(parseAbstractId(xmlStream));
			nextElement(xmlStream);
		}
		return new IdFilter(selectedIds);
	}

	private static ResourceId parseAbstractId(XMLStreamReader xmlStream)
			throws NoSuchElementException, XMLStreamException {
		if (!RESOURCE_ID_ELEMENT.equals(xmlStream.getName())) {
			String msg = Messages.getMessage("FILTER_PARSER_ID_FILTER_UNEXPECTED_ELEMENT", xmlStream.getName(),
					RESOURCE_ID_ELEMENT, RESOURCE_ID_ELEMENT);
			throw new XMLParsingException(xmlStream, msg);
		}
		String rid = getRequiredAttributeValue(xmlStream, "rid");
		String previousRid = xmlStream.getAttributeValue(null, "previousRid");
		String version = xmlStream.getAttributeValue(null, "version");
		DateTime startDate = null;
		String startDateString = xmlStream.getAttributeValue(null, "startDate");
		if (startDateString != null) {
			try {
				startDate = parseDateTime(startDateString);
			}
			catch (Exception e) {
				throw new XMLParsingException(xmlStream, e.getMessage());
			}
		}
		DateTime endDate = null;
		String endDateString = xmlStream.getAttributeValue(null, "endDate");
		if (endDateString != null) {
			try {
				endDate = parseDateTime(endDateString);
			}
			catch (Exception e) {
				throw new XMLParsingException(xmlStream, e.getMessage());
			}
		}
		nextElement(xmlStream);
		return new ResourceId(rid, previousRid, version, startDate, endDate);
	}

	private static ComparisonOperator parseBinaryComparisonOperator(XMLStreamReader xmlStream, SubType type)
			throws XMLStreamException {

		BinaryComparisonOperator comparisonOperator = null;

		// TODO should this be null, if not present?
		Boolean matchCase = getAttributeValueAsBoolean(xmlStream, null, "matchCase", true);

		MatchAction matchAction = null;
		String s = XMLStreamUtils.getAttributeValue(xmlStream, "matchAction");
		if (s != null) {
			matchAction = parseMatchAction(xmlStream, s);
		}

		XMLStreamUtils.requireNextTag(xmlStream, START_ELEMENT);
		Expression parameter1 = parseExpression(xmlStream);
		XMLStreamUtils.requireNextTag(xmlStream, START_ELEMENT);
		Expression parameter2 = parseExpression(xmlStream);
		XMLStreamUtils.requireNextTag(xmlStream, END_ELEMENT);

		switch (type) {
			case PROPERTY_IS_EQUAL_TO:
				comparisonOperator = new PropertyIsEqualTo(parameter1, parameter2, matchCase, matchAction);
				break;
			case PROPERTY_IS_NOT_EQUAL_TO:
				comparisonOperator = new PropertyIsNotEqualTo(parameter1, parameter2, matchCase, matchAction);
				break;
			case PROPERTY_IS_LESS_THAN:
				comparisonOperator = new PropertyIsLessThan(parameter1, parameter2, matchCase, matchAction);
				break;
			case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
				comparisonOperator = new PropertyIsLessThanOrEqualTo(parameter1, parameter2, matchCase, matchAction);
				break;
			case PROPERTY_IS_GREATER_THAN:
				comparisonOperator = new PropertyIsGreaterThan(parameter1, parameter2, matchCase, matchAction);
				break;
			case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
				comparisonOperator = new PropertyIsGreaterThanOrEqualTo(parameter1, parameter2, matchCase, matchAction);
				break;
			default:
				assert false;
		}
		return comparisonOperator;
	}

	private static MatchAction parseMatchAction(XMLStreamReader xmlStream, String matchAction) {
		if ("All".equals(matchAction))
			return ALL;
		if ("Any".equals(matchAction))
			return MatchAction.ANY;
		if ("One".equals(matchAction))
			return MatchAction.ONE;
		String msg = Messages.getMessage("FILTER_PARSER_UNEXPECTED_VALUE", matchAction,
				ArrayUtils.join(",", "All", "Any", "None"));
		throw new XMLParsingException(xmlStream, msg);
	}

	private static Literal<?> parseLiteral(XMLStreamReader xmlStream) throws XMLStreamException {

		QName type = getAttributeValueAsQName(xmlStream, null, "type", null);
		if (type != null) {
			LOG.warn("Literal with type attribute. Not respecting type hint (needs implementation).");
		}

		Map<QName, PrimitiveValue> attrs = parseAttrs(xmlStream);
		List<TypedObjectNode> children = new ArrayList<TypedObjectNode>();
		while (xmlStream.next() != END_ELEMENT) {
			int eventType = xmlStream.getEventType();
			if (eventType == START_ELEMENT) {
				checkRequiredGmlGeometry(xmlStream);
				children.add(parseElement(xmlStream));
			}
			else if (eventType == CHARACTERS || eventType == CDATA) {
				children.add(new PrimitiveValue(xmlStream.getText()));
			}
		}

		// TODO what about well-known complex elements (e.g. geometries)?

		TypedObjectNode value = null;
		if (attrs.isEmpty() && children.size() == 1) {
			value = children.get(0);
		}
		else if (attrs.isEmpty() && children.isEmpty()) {
			value = new PrimitiveValue("");
		}
		else {
			value = new GenericXMLElement(null, attrs, children);
		}
		return new Literal<TypedObjectNode>(value, type);
	}

	private static GenericXMLElement parseElement(XMLStreamReader xmlStream)
			throws IllegalArgumentException, XMLStreamException {
		Map<QName, PrimitiveValue> attrs = parseAttrs(xmlStream);
		List<TypedObjectNode> children = new ArrayList<TypedObjectNode>();
		while (xmlStream.next() != END_ELEMENT) {
			int eventType = xmlStream.getEventType();
			if (eventType == START_ELEMENT) {
				children.add(parseElement(xmlStream));
			}
			else if (eventType == CHARACTERS || eventType == CDATA) {
				children.add(new PrimitiveValue(xmlStream.getText()));
			}
		}
		return new GenericXMLElement(xmlStream.getName(), attrs, children);
	}

	private static Map<QName, PrimitiveValue> parseAttrs(XMLStreamReader xmlStream) {
		Map<QName, PrimitiveValue> attrs = new LinkedHashMap<QName, PrimitiveValue>();
		for (int i = 0; i < xmlStream.getAttributeCount(); i++) {
			QName name = xmlStream.getAttributeName(i);
			String value = xmlStream.getAttributeValue(i);
			PrimitiveValue xmlValue = new PrimitiveValue(value);
			attrs.put(name, xmlValue);
		}
		return attrs;
	}

	private static ValueReference parseValueReference(XMLStreamReader xmlStream, boolean permitEmpty)
			throws XMLStreamException {
		requireStartElement(xmlStream, singleton(new QName(FES_NS, "ValueReference")));
		String xpath = xmlStream.getElementText().trim();
		if (!permitEmpty && xpath.isEmpty()) {
			throw new XMLParsingException(xmlStream,
					Messages.getMessage("FILTER_PARSER_PROPERTY_NAME_EMPTY", new QName(FES_NS, "ValueReference")));
		}
		if (xpath.isEmpty()) {
			return null;
		}
		Set<String> prefixes = XPathUtils.extractPrefixes(xpath);
		return new ValueReference(xpath, new NamespaceBindings(xmlStream.getNamespaceContext(), prefixes));
	}

	private static PropertyIsBetween parsePropertyIsBetweenOperator(XMLStreamReader xmlStream)
			throws XMLStreamException {

		// this is a deegree extension over Filter 2.0.0 spec. (TODO should this be null,
		// if not present?)
		Boolean matchCase = getAttributeValueAsBoolean(xmlStream, null, "matchCase", true);

		// this is a deegree extension over Filter 2.0.0 spec. (TODO should this be null,
		// if not present?)
		MatchAction matchAction = null;
		String s = XMLStreamUtils.getAttributeValue(xmlStream, "matchAction");
		if (s != null) {
			matchAction = parseMatchAction(xmlStream, s);
		}

		nextElement(xmlStream);
		Expression expression = parseExpression(xmlStream);

		nextElement(xmlStream);
		xmlStream.require(START_ELEMENT, FES_NS, "LowerBoundary");
		nextElement(xmlStream);
		Expression lowerBoundary = parseExpression(xmlStream);
		nextElement(xmlStream);

		nextElement(xmlStream);
		xmlStream.require(START_ELEMENT, FES_NS, "UpperBoundary");
		nextElement(xmlStream);
		Expression upperBoundary = parseExpression(xmlStream);
		nextElement(xmlStream);
		nextElement(xmlStream);

		return new PropertyIsBetween(expression, lowerBoundary, upperBoundary, matchCase, matchAction);
	}

	private static PropertyIsLike parsePropertyIsLikeOperator(XMLStreamReader xmlStream) throws XMLStreamException {

		// this is a deegree extension over Filter 2.0.0 spec. (TODO should this be null,
		// if not present?)
		Boolean matchCase = getAttributeValueAsBoolean(xmlStream, null, "matchCase", true);

		// this is a deegree extension over Filter 2.0.0 spec. (TODO should this be null,
		// if not present?)
		MatchAction matchAction = null;
		String s = XMLStreamUtils.getAttributeValue(xmlStream, "matchAction");
		if (s != null) {
			matchAction = parseMatchAction(xmlStream, s);
		}

		String wildCard = getRequiredAttributeValue(xmlStream, "wildCard");
		String singleChar = getRequiredAttributeValue(xmlStream, "singleChar");
		String escapeChar = getRequiredAttributeValue(xmlStream, "escapeChar");

		nextElement(xmlStream);
		Expression value = parseExpression(xmlStream);

		nextElement(xmlStream);
		Expression pattern = parseExpression(xmlStream);
		nextElement(xmlStream);
		return new PropertyIsLike(value, pattern, wildCard, singleChar, escapeChar, matchCase, matchAction);
	}

	private static PropertyIsNull parsePropertyIsNullOperator(XMLStreamReader xmlStream) throws XMLStreamException {

		// this is a deegree extension over Filter 2.0.0 spec. (TODO should this be null,
		// if not present?)
		MatchAction matchAction = null;
		String s = XMLStreamUtils.getAttributeValue(xmlStream, "matchAction");
		if (s != null) {
			matchAction = parseMatchAction(xmlStream, s);
		}

		nextElement(xmlStream);
		Expression value = parseExpression(xmlStream);
		nextElement(xmlStream);
		return new PropertyIsNull(value, matchAction);
	}

	private static PropertyIsNil parsePropertyIsNilOperator(XMLStreamReader xmlStream) throws XMLStreamException {

		// this is a deegree extension over Filter 2.0.0 spec. (TODO should this be null,
		// if not present?)
		MatchAction matchAction = null;
		String s = XMLStreamUtils.getAttributeValue(xmlStream, "matchAction");
		if (s != null) {
			matchAction = parseMatchAction(xmlStream, s);
		}

		String nilReason = XMLStreamUtils.getAttributeValue(xmlStream, "nilReason");

		nextElement(xmlStream);
		Expression param = parseExpression(xmlStream);
		nextElement(xmlStream);
		return new PropertyIsNil(param, nilReason, matchAction);
	}

	private static LogicalOperator parseLogicalOperator(XMLStreamReader xmlStream) throws XMLStreamException {

		LogicalOperator logicalOperator = null;

		// check if element name is a valid logical operator element
		LogicalOperator.SubType type = elementNameToLogicalOperatorType.get(xmlStream.getName());
		if (type == null) {
			String msg = Messages.getMessage("FILTER_PARSER_UNEXPECTED_ELEMENT", xmlStream.getName(),
					elemNames(LogicalOperator.SubType.class, logicalOperatorTypeToElementName));
			throw new XMLParsingException(xmlStream, msg);
		}

		switch (type) {
			case AND: {
				List<Operator> innerOperators = new ArrayList<Operator>();
				while (nextElement(xmlStream) == START_ELEMENT) {
					innerOperators.add(parseOperator(xmlStream));
				}
				if (innerOperators.size() < 2) {
					String msg = "Error while parsing And operator. Must have at least two arguments.";
					throw new XMLParsingException(xmlStream, msg);
				}
				logicalOperator = new And(innerOperators.toArray(new Operator[innerOperators.size()]));
				break;
			}
			case OR: {
				List<Operator> innerOperators = new ArrayList<Operator>();
				while (nextElement(xmlStream) == START_ELEMENT) {
					innerOperators.add(parseOperator(xmlStream));
				}
				if (innerOperators.size() < 2) {
					String msg = "Error while parsing Or operator. Must have at least two arguments.";
					throw new XMLParsingException(xmlStream, msg);
				}
				logicalOperator = new Or(innerOperators.toArray(new Operator[innerOperators.size()]));
				break;
			}
			case NOT: {
				nextElement(xmlStream);
				Operator parameter = parseOperator(xmlStream);
				logicalOperator = new Not(parameter);
				nextElement(xmlStream);
				break;
			}
		}
		return logicalOperator;
	}

	private static SpatialOperator parseSpatialOperator(XMLStreamReader xmlStream) throws XMLStreamException {

		SpatialOperator spatialOperator = null;

		require(xmlStream, START_ELEMENT);
		// check if element name is a valid spatial operator element name
		SpatialOperator.SubType type = elementNameToSpatialOperatorType.get(xmlStream.getName());
		if (type == null) {
			String msg = Messages.getMessage("FILTER_PARSER_UNEXPECTED_ELEMENT", xmlStream.getName(),
					elemNames(SpatialOperator.SubType.class, spatialOperatorTypeToElementName));
			throw new XMLParsingException(xmlStream, msg);
		}

		nextElement(xmlStream);

		switch (type) {
			case BBOX: {
				// <xsd:element ref="fes:expression" minOccurs="0"/>
				Expression param1 = null;
				if (elementNameToExpressionType.containsKey(xmlStream.getName())) {
					param1 = parseExpression(xmlStream);
					nextElement(xmlStream);
				}
				if (isCurrentStartElementIsGmlGeometry(xmlStream)) {
					// <xsd:any namespace="##other"/> (is 'gml:Envelope'/'gml:Box')
					Geometry param2 = parseGeomOrEnvelope(xmlStream);
					spatialOperator = new BBOX(param1, (Envelope) param2);
				}
				else {
					// <xsd:any namespace="##other"/> (is 'fes:ValueReference')
					ValueReference param2 = parseValueReference(xmlStream, false);
					spatialOperator = new BBOX(param1, param2);
				}
				break;
			}
			case BEYOND: {
				// <xsd:element ref="fes:expression" minOccurs="0"/>
				Expression param1 = null;
				if (elementNameToExpressionType.containsKey(xmlStream.getName())) {
					param1 = parseExpression(xmlStream);
					nextElement(xmlStream);
				}
				if (isCurrentStartElementIsGmlGeometry(xmlStream)) {
					// <xsd:any namespace="##other"/> (is 'gml:Geometry')
					Geometry param2 = parseGeomOrEnvelope(xmlStream);
					// third parameter: 'fes:Distance'
					nextElement(xmlStream);
					Measure distance = parseDistance(xmlStream);
					spatialOperator = new Beyond(param1, param2, distance);
				}
				else {
					// <xsd:any namespace="##other"/> (is 'fes:ValueReference')
					ValueReference param2 = parseValueReference(xmlStream, false);
					// third parameter: 'fes:Distance'
					nextElement(xmlStream);
					Measure distance = parseDistance(xmlStream);
					spatialOperator = new Beyond(param1, param2, distance);
				}
				break;
			}
			case INTERSECTS: {
				// <xsd:element ref="fes:expression"/> (NOTE: we accept minOccurs="1" as
				// well)
				Expression param1 = null;
				if (elementNameToExpressionType.containsKey(xmlStream.getName())) {
					param1 = parseExpression(xmlStream);
					nextElement(xmlStream);
				}
				if (isCurrentStartElementIsGmlGeometry(xmlStream)) {
					// <xsd:any namespace="##other"/> (is 'gml:Geometry')
					Geometry param2 = parseGeomOrEnvelope(xmlStream);
					spatialOperator = new Intersects(param1, param2);
				}
				else {
					// <xsd:any namespace="##other"/> (is 'fes:ValueReference')
					ValueReference param2 = parseValueReference(xmlStream, false);
					spatialOperator = new Intersects(param1, param2);
				}
				break;
			}
			case CONTAINS: {
				// <xsd:element ref="fes:expression"/> (NOTE: we accept minOccurs="1" as
				// well)
				Expression param1 = null;
				if (elementNameToExpressionType.containsKey(xmlStream.getName())) {
					param1 = parseExpression(xmlStream);
					nextElement(xmlStream);
				}
				if (isCurrentStartElementIsGmlGeometry(xmlStream)) {
					// <xsd:any namespace="##other"/> (is 'gml:Geometry')
					Geometry param2 = parseGeomOrEnvelope(xmlStream);
					spatialOperator = new Contains(param1, param2);
				}
				else {
					// <xsd:any namespace="##other"/> (is 'fes:ValueReference')
					ValueReference param2 = parseValueReference(xmlStream, false);
					spatialOperator = new Contains(param1, param2);
				}
				break;
			}
			case CROSSES: {
				// <xsd:element ref="fes:expression"/> (NOTE: we accept minOccurs="1" as
				// well)
				Expression param1 = null;
				if (elementNameToExpressionType.containsKey(xmlStream.getName())) {
					param1 = parseExpression(xmlStream);
					nextElement(xmlStream);
				}
				if (isCurrentStartElementIsGmlGeometry(xmlStream)) {
					// <xsd:any namespace="##other"/> (is 'gml:Geometry')
					Geometry param2 = parseGeomOrEnvelope(xmlStream);
					spatialOperator = new Crosses(param1, param2);
				}
				else {
					// <xsd:any namespace="##other"/> (is 'fes:ValueReference')
					ValueReference param2 = parseValueReference(xmlStream, false);
					spatialOperator = new Crosses(param1, param2);
				}
				break;
			}
			case DISJOINT: {
				// <xsd:element ref="fes:expression"/> (NOTE: we accept minOccurs="1" as
				// well)
				Expression param1 = null;
				if (elementNameToExpressionType.containsKey(xmlStream.getName())) {
					param1 = parseExpression(xmlStream);
					nextElement(xmlStream);
				}
				if (isCurrentStartElementIsGmlGeometry(xmlStream)) {
					// <xsd:any namespace="##other"/> (is 'gml:Geometry')
					Geometry param2 = parseGeomOrEnvelope(xmlStream);
					spatialOperator = new Disjoint(param1, param2);
				}
				else {
					// <xsd:any namespace="##other"/> (is 'fes:ValueReference')
					ValueReference param2 = parseValueReference(xmlStream, false);
					spatialOperator = new Disjoint(param1, param2);
				}
				break;
			}
			case DWITHIN: {
				// <xsd:element ref="fes:expression" minOccurs="0"/>
				Expression param1 = null;
				if (elementNameToExpressionType.containsKey(xmlStream.getName())) {
					param1 = parseExpression(xmlStream);
					nextElement(xmlStream);
				}
				if (isCurrentStartElementIsGmlGeometry(xmlStream)) {
					// <xsd:any namespace="##other"/> (is 'gml:Geometry')
					Geometry param2 = parseGeomOrEnvelope(xmlStream);
					// third parameter: 'fes:Distance'
					nextElement(xmlStream);
					Measure distance = parseDistance(xmlStream);
					spatialOperator = new DWithin(param1, param2, distance);
				}
				else {
					// <xsd:any namespace="##other"/> (is 'fes:ValueReference')
					ValueReference param2 = parseValueReference(xmlStream, false);
					// third parameter: 'fes:Distance'
					nextElement(xmlStream);
					Measure distance = parseDistance(xmlStream);
					spatialOperator = new DWithin(param1, param2, distance);
				}
				break;
			}
			case EQUALS: {
				// <xsd:element ref="fes:expression"/> (NOTE: we accept minOccurs="1" as
				// well)
				Expression param1 = null;
				if (elementNameToExpressionType.containsKey(xmlStream.getName())) {
					param1 = parseExpression(xmlStream);
					nextElement(xmlStream);
				}
				if (isCurrentStartElementIsGmlGeometry(xmlStream)) {
					// <xsd:any namespace="##other"/> (is 'gml:Geometry')
					Geometry param2 = parseGeomOrEnvelope(xmlStream);
					spatialOperator = new Equals(param1, param2);
				}
				else {
					// <xsd:any namespace="##other"/> (is 'fes:ValueReference')
					ValueReference param2 = parseValueReference(xmlStream, false);
					spatialOperator = new Equals(param1, param2);
				}
				break;
			}
			case OVERLAPS: {
				// <xsd:element ref="fes:expression"/> (NOTE: we accept minOccurs="1" as
				// well)
				Expression param1 = null;
				if (elementNameToExpressionType.containsKey(xmlStream.getName())) {
					param1 = parseExpression(xmlStream);
					nextElement(xmlStream);
				}
				if (isCurrentStartElementIsGmlGeometry(xmlStream)) {
					// <xsd:any namespace="##other"/> (is 'gml:Geometry')
					Geometry param2 = parseGeomOrEnvelope(xmlStream);
					spatialOperator = new Overlaps(param1, param2);
				}
				else {
					// <xsd:any namespace="##other"/> (is 'fes:ValueReference')
					ValueReference param2 = parseValueReference(xmlStream, false);
					spatialOperator = new Overlaps(param1, param2);
				}
				break;
			}
			case TOUCHES: {
				// <xsd:element re="fes:expression"/> (NOTE: we accept minOccurs="1" as
				// well)
				Expression param1 = null;
				if (elementNameToExpressionType.containsKey(xmlStream.getName())) {
					param1 = parseExpression(xmlStream);
					nextElement(xmlStream);
				}
				if (isCurrentStartElementIsGmlGeometry(xmlStream)) {
					// <xsd:any namespace="##other"/> (is 'gml:Geometry')
					Geometry param2 = parseGeomOrEnvelope(xmlStream);
					spatialOperator = new Touches(param1, param2);
				}
				else {
					// <xsd:any namespace="##other"/> (is 'fes:ValueReference')
					ValueReference param2 = parseValueReference(xmlStream, false);
					spatialOperator = new Touches(param1, param2);
				}
				break;
			}
			case WITHIN: {
				// <xsd:element ref="fes:expression"/> (NOTE: we accept minOccurs="1" as
				// well)
				Expression param1 = null;
				if (elementNameToExpressionType.containsKey(xmlStream.getName())) {
					param1 = parseExpression(xmlStream);
					nextElement(xmlStream);
				}
				if (isCurrentStartElementIsGmlGeometry(xmlStream)) {
					// <xsd:any namespace="##other"/> (is 'gml:Geometry')
					Geometry param2 = parseGeomOrEnvelope(xmlStream);
					spatialOperator = new Within(param1, param2);
				}
				else {
					// <xsd:any namespace="##other"/> (is 'fes:ValueReference')
					ValueReference param2 = parseValueReference(xmlStream, false);
					spatialOperator = new Within(param1, param2);
				}
				break;
			}
		}

		nextElement(xmlStream);
		return spatialOperator;
	}

	private static Geometry parseGeomOrEnvelope(XMLStreamReader xmlStream) throws XMLStreamException {
		GMLGeometryReader gmlReader = GMLGeometryVersionHelper.getGeometryReader(xmlStream.getName(), xmlStream);
		try {
			return gmlReader.parseGeometryOrEnvelope(new XMLStreamReaderWrapper(xmlStream, null), null);
		}
		catch (XMLParsingException e) {
			throw e;
		}
		catch (XMLStreamException e) {
			throw e;
		}
		catch (Throwable t) {
			t.printStackTrace();
			throw new XMLParsingException(xmlStream, t.getMessage());
		}
	}

	private static Measure parseDistance(XMLStreamReader xmlStream) throws XMLStreamException {
		xmlStream.require(START_ELEMENT, FES_NS, "Distance");
		String distanceUnits = getRequiredAttributeValue(xmlStream, "uom");
		String distanceValue = xmlStream.getElementText();
		return new Measure(distanceValue, distanceUnits);
	}

	private static void checkRequiredGmlGeometry(XMLStreamReader xmlStream) throws XMLStreamException {
		if (isCurrentStartElementIsGmlGeometry(xmlStream))
			throw new XMLParsingException(xmlStream, "Geometry elements as Literal child are not supported!");
	}

	private static boolean isCurrentStartElementIsGmlGeometry(XMLStreamReader xmlStream) throws XMLStreamException {
		String ns = xmlStream.getNamespaceURI();
		if (CommonNamespaces.GMLNS.equals(ns) || CommonNamespaces.GML3_2_NS.equals(ns)) {
			GMLGeometryReader gmlReader = GMLGeometryVersionHelper.getGeometryReader(xmlStream.getName(), xmlStream);
			return gmlReader.isGeometryOrEnvelopeElement(xmlStream);
		}
		return false;
	}

	/**
	 * Return a String with all element names of the given enum class.
	 * @param enumClass
	 * @param map the operator type -> element name map
	 * @return a coma separated list of element names
	 */
	private static String elemNames(Class<? extends Enum<?>> enumClass, Map<? extends Enum<?>, QName> map) {
		List<String> names = new LinkedList<String>();
		for (Enum<?> e : enumClass.getEnumConstants()) {
			QName qname = map.get(e);
			if (qname != null) {
				LOG.debug(qname.toString());
				names.add(qname.toString());
			}

		}
		return ArrayUtils.join(", ", names);
	}

}
