/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.filter.xml;

import static org.deegree.commons.xml.CommonNamespaces.FES_20_NS;
import static org.deegree.commons.xml.CommonNamespaces.FES_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.CommonNamespaces.XS_PREFIX;
import static org.deegree.gml.GMLVersion.GML_32;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.deegree.filter.Expression.Type;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.MatchAction;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.ResourceId;
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
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.logical.Not;
import org.deegree.filter.logical.Or;
import org.deegree.filter.spatial.Beyond;
import org.deegree.filter.spatial.DWithin;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.filter.temporal.TemporalOperator;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.DecimalCoordinateFormatter;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.time.gml.writer.GmlTimeGeometricPrimitiveWriter;
import org.deegree.time.primitive.TimeGeometricPrimitive;

/**
 * Encodes {@link Filter} objects according to the Filter Encoding Specification 2.0.0.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class Filter200XMLEncoder {

	private static final String GML_PREFIX = "gml";

	private static final Map<SpatialOperator.SubType, QName> spatialOperatorTypeToElementName = new HashMap<SpatialOperator.SubType, QName>();

	private static final Map<TemporalOperator.SubType, QName> temporalOperatorTypeToElementName = new HashMap<TemporalOperator.SubType, QName>();

	static {
		spatialOperatorTypeToElementName.put(SpatialOperator.SubType.BBOX, new QName(FES_20_NS, "BBOX"));
		spatialOperatorTypeToElementName.put(SpatialOperator.SubType.BEYOND, new QName(FES_20_NS, "Beyond"));
		spatialOperatorTypeToElementName.put(SpatialOperator.SubType.CONTAINS, new QName(FES_20_NS, "Contains"));
		spatialOperatorTypeToElementName.put(SpatialOperator.SubType.CROSSES, new QName(FES_20_NS, "Crosses"));
		spatialOperatorTypeToElementName.put(SpatialOperator.SubType.EQUALS, new QName(FES_20_NS, "Equals"));
		spatialOperatorTypeToElementName.put(SpatialOperator.SubType.DISJOINT, new QName(FES_20_NS, "Disjoint"));
		spatialOperatorTypeToElementName.put(SpatialOperator.SubType.DWITHIN, new QName(FES_20_NS, "DWithin"));
		spatialOperatorTypeToElementName.put(SpatialOperator.SubType.INTERSECTS, new QName(FES_20_NS, "Intersects"));
		spatialOperatorTypeToElementName.put(SpatialOperator.SubType.OVERLAPS, new QName(FES_20_NS, "Overlaps"));
		spatialOperatorTypeToElementName.put(SpatialOperator.SubType.TOUCHES, new QName(FES_20_NS, "Touches"));
		spatialOperatorTypeToElementName.put(SpatialOperator.SubType.WITHIN, new QName(FES_20_NS, "Within"));

		temporalOperatorTypeToElementName.put(TemporalOperator.SubType.AFTER, new QName(FES_20_NS, "After"));
		temporalOperatorTypeToElementName.put(TemporalOperator.SubType.ANYINTERACTS,
				new QName(FES_20_NS, "AnyInteracts"));
		temporalOperatorTypeToElementName.put(TemporalOperator.SubType.BEFORE, new QName(FES_20_NS, "Before"));
		temporalOperatorTypeToElementName.put(TemporalOperator.SubType.BEGINS, new QName(FES_20_NS, "Begins"));
		temporalOperatorTypeToElementName.put(TemporalOperator.SubType.BEGUNBY, new QName(FES_20_NS, "BegunBy"));
		temporalOperatorTypeToElementName.put(TemporalOperator.SubType.DURING, new QName(FES_20_NS, "During"));
		temporalOperatorTypeToElementName.put(TemporalOperator.SubType.ENDEDBY, new QName(FES_20_NS, "EndedBy"));
		temporalOperatorTypeToElementName.put(TemporalOperator.SubType.MEETS, new QName(FES_20_NS, "Meets"));
		temporalOperatorTypeToElementName.put(TemporalOperator.SubType.METBY, new QName(FES_20_NS, "MetBy"));
		temporalOperatorTypeToElementName.put(TemporalOperator.SubType.OVERLAPPEDBY,
				new QName(FES_20_NS, "OverlappedBy"));
		temporalOperatorTypeToElementName.put(TemporalOperator.SubType.TCONTAINS, new QName(FES_20_NS, "TContains"));
		temporalOperatorTypeToElementName.put(TemporalOperator.SubType.TEQUALS, new QName(FES_20_NS, "TEquals"));
		temporalOperatorTypeToElementName.put(TemporalOperator.SubType.TOVERLAPS, new QName(FES_20_NS, "TOverlaps"));
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
		writer.setPrefix(FES_PREFIX, FES_20_NS);
		writer.writeStartElement(FES_20_NS, "Filter");
		writer.writeNamespace(FES_PREFIX, FES_20_NS);
		writer.writeNamespace(GML_PREFIX, GML3_2_NS);
		writer.writeNamespace(XS_PREFIX, XSINS);
		writer.writeAttribute(XSINS, "schemaLocation",
				"http://www.opengis.net/fes/2.0 http://schemas.opengis.net/filter/2.0/filter.xsd");

		switch (filter.getType()) {
			case ID_FILTER:
				export((IdFilter) filter, writer);
				break;
			case OPERATOR_FILTER:
				export(((OperatorFilter) filter).getOperator(), writer);
				break;
		}
		writer.writeEndElement();
	}

	private static void export(IdFilter idFilter, XMLStreamWriter writer) throws XMLStreamException {
		List<ResourceId> ids = idFilter.getSelectedIds();
		for (ResourceId id : ids) {
			writer.writeStartElement(FES_20_NS, "ResourceId");
			writer.writeAttribute("rid", id.getRid());
			// writer.writeAttribute( "previousRid", id.getPreviousId() );
			// writer.writeAttribute( "version", id.get() );
			// writer.writeAttribute( "startDate", id.get() );
			// writer.writeAttribute( "endDate", id.get() );
			writer.writeEndElement();
		}
	}

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
			case TEMPORAL:
				export((TemporalOperator) operator, writer);
				break;
			default:
				throw new IllegalArgumentException(
						"Encoding of operator type " + operator.getType() + " is not supported yet!");
		}
	}

	private static void export(ComparisonOperator operator, XMLStreamWriter writer)
			throws XMLStreamException, UnknownCRSException, TransformationException {
		SubType operatorSubType = operator.getSubType();
		switch (operatorSubType) {
			case PROPERTY_IS_BETWEEN:
				export((PropertyIsBetween) operator, writer);
				break;
			case PROPERTY_IS_EQUAL_TO:
				export((PropertyIsEqualTo) operator, writer);
				break;
			case PROPERTY_IS_GREATER_THAN:
				export((PropertyIsGreaterThan) operator, writer);
				break;
			case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
				export((PropertyIsGreaterThanOrEqualTo) operator, writer);
				break;
			case PROPERTY_IS_LESS_THAN:
				export((PropertyIsLessThan) operator, writer);
				break;
			case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
				export((PropertyIsLessThanOrEqualTo) operator, writer);
				break;
			case PROPERTY_IS_LIKE:
				export((PropertyIsLike) operator, writer);
				break;
			case PROPERTY_IS_NOT_EQUAL_TO:
				export((PropertyIsNotEqualTo) operator, writer);
				break;
			case PROPERTY_IS_NULL:
				export((PropertyIsNull) operator, writer);
				break;
			case PROPERTY_IS_NIL:
				export((PropertyIsNil) operator, writer);
				break;
			default:
				throw new IllegalArgumentException(
						"Encoding of operator subtype " + operator.getType() + " is not supported yet!");
		}
	}

	private static void export(LogicalOperator operator, XMLStreamWriter writer)
			throws XMLStreamException, UnknownCRSException, TransformationException {
		switch (operator.getSubType()) {
			case AND:
				export(writer, (And) operator);
				break;
			case OR:
				export(writer, (Or) operator);
				break;
			case NOT:
				export(writer, (Not) operator);
				break;
			default:
				throw new IllegalArgumentException(
						"Encoding of logical operator subtype " + operator.getSubType() + " is not supported yet!");
		}
	}

	private static void export(SpatialOperator operator, XMLStreamWriter writer)
			throws XMLStreamException, UnknownCRSException, TransformationException {
		QName elementName = spatialOperatorTypeToElementName.get(operator.getSubType());
		if (elementName == null)
			throw new IllegalArgumentException(
					"Encoding of spatial operator subtype " + operator.getSubType() + " is not supported yet!");
		writer.writeStartElement(elementName.getNamespaceURI(), elementName.getLocalPart());

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
			case BBOX:
			case CONTAINS:
			case CROSSES:
			case DISJOINT:
			case EQUALS:
			case INTERSECTS:
			case OVERLAPS:
			case TOUCHES:
			case WITHIN:
				break;
			default:
				throw new IllegalArgumentException(
						"Encoding of spatial operator subtype " + operator.getSubType() + " is not supported yet!");
		}
		GMLStreamWriter gmlWriter = createGml32StreamWriter(writer);

		export(operator.getParam1(), writer);
		if (secondParam != null)
			export(secondParam, writer);
		if (geometry != null)
			exportGeometry(geometry, gmlWriter);
		exportDistance(distance, writer);
		writer.writeEndElement();
	}

	private static void export(TemporalOperator operator, XMLStreamWriter writer)
			throws XMLStreamException, UnknownCRSException, TransformationException {
		QName elementName = temporalOperatorTypeToElementName.get(operator.getSubType());
		if (elementName == null)
			throw new IllegalArgumentException(
					"Encoding of temporal operator subtype " + operator.getSubType() + " is not supported yet!");
		writer.writeStartElement(elementName.getNamespaceURI(), elementName.getLocalPart());
		export(operator.getParameter1(), writer);
		export(operator.getParameter2(), writer);
		writer.writeEndElement();
	}

	private static void export(PropertyIsBetween operator, XMLStreamWriter writer)
			throws XMLStreamException, UnknownCRSException, TransformationException {
		writer.writeStartElement(FES_20_NS, "PropertyIsBetween");
		export(operator.getExpression(), writer);
		writer.writeStartElement(FES_20_NS, "LowerBoundary");
		export(operator.getLowerBoundary(), writer);
		writer.writeEndElement();
		writer.writeStartElement(FES_20_NS, "UpperBoundary");
		export(operator.getUpperBoundary(), writer);
		writer.writeEndElement();
		writer.writeEndElement();
	}

	private static void export(PropertyIsEqualTo operator, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(FES_20_NS, "PropertyIsEqualTo");
		String matchAction = retrieveMatchActionAsString(operator, writer);
		if (matchAction != null)
			writer.writeAttribute("matchAction", matchAction);
		writer.writeAttribute("matchCase", Boolean.toString(operator.isMatchCase()));
		export(operator.getParameter1(), writer);
		export(operator.getParameter2(), writer);
		writer.writeEndElement();
	}

	private static void export(PropertyIsGreaterThan operator, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(FES_20_NS, "PropertyIsGreaterThan");
		String matchAction = retrieveMatchActionAsString(operator, writer);
		if (matchAction != null)
			writer.writeAttribute("matchAction", matchAction);
		writer.writeAttribute("matchCase", Boolean.toString(operator.isMatchCase()));
		export(operator.getParameter1(), writer);
		export(operator.getParameter2(), writer);
		writer.writeEndElement();
	}

	private static void export(PropertyIsGreaterThanOrEqualTo operator, XMLStreamWriter writer)
			throws XMLStreamException {
		writer.writeStartElement(FES_20_NS, "PropertyIsGreaterThanOrEqualTo");
		String matchAction = retrieveMatchActionAsString(operator, writer);
		if (matchAction != null)
			writer.writeAttribute("matchAction", matchAction);
		writer.writeAttribute("matchCase", Boolean.toString(operator.isMatchCase()));
		export(operator.getParameter1(), writer);
		export(operator.getParameter2(), writer);
		writer.writeEndElement();
	}

	private static void export(PropertyIsLessThan operator, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(FES_20_NS, "PropertyIsLessThan");
		String matchAction = retrieveMatchActionAsString(operator, writer);
		if (matchAction != null)
			writer.writeAttribute("matchAction", matchAction);
		writer.writeAttribute("matchCase", Boolean.toString(operator.isMatchCase()));
		export(operator.getParameter1(), writer);
		export(operator.getParameter2(), writer);
		writer.writeEndElement();
	}

	private static void export(PropertyIsLessThanOrEqualTo operator, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(FES_20_NS, "PropertyIsLessThanOrEqualTo");
		String matchAction = retrieveMatchActionAsString(operator, writer);
		if (matchAction != null)
			writer.writeAttribute("matchAction", matchAction);
		writer.writeAttribute("matchCase", Boolean.toString(operator.isMatchCase()));
		export(operator.getParameter1(), writer);
		export(operator.getParameter2(), writer);
		writer.writeEndElement();
	}

	private static void export(PropertyIsLike operator, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(FES_20_NS, "PropertyIsLike");
		writer.writeAttribute("wildCard", operator.getWildCard());
		writer.writeAttribute("singleChar", operator.getSingleChar());
		writer.writeAttribute("escapeChar", operator.getEscapeChar());
		export(operator.getExpression(), writer);
		export(operator.getPattern(), writer);
		writer.writeEndElement();
	}

	private static void export(PropertyIsNotEqualTo operator, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(FES_20_NS, "PropertyIsNotEqualTo");
		String matchAction = retrieveMatchActionAsString(operator, writer);
		if (matchAction != null)
			writer.writeAttribute("matchAction", matchAction);
		writer.writeAttribute("matchCase", Boolean.toString(operator.isMatchCase()));
		export(operator.getParameter1(), writer);
		export(operator.getParameter2(), writer);
		writer.writeEndElement();
	}

	private static void export(PropertyIsNull operator, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(FES_20_NS, "PropertyIsNull");
		export(operator.getPropertyName(), writer);
		writer.writeEndElement();
	}

	private static void export(PropertyIsNil operator, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(FES_20_NS, "PropertyIsNil");
		export(operator.getPropertyName(), writer);
		writer.writeEndElement();
	}

	private static void export(XMLStreamWriter writer, And andOp)
			throws XMLStreamException, UnknownCRSException, TransformationException {
		writer.writeStartElement(FES_20_NS, "And");
		for (int i = 0; i < andOp.getSize(); i++) {
			export(andOp.getParameter(i), writer);
		}
		writer.writeEndElement();
	}

	private static void export(XMLStreamWriter writer, Or orOp)
			throws XMLStreamException, UnknownCRSException, TransformationException {
		writer.writeStartElement(FES_20_NS, "Or");
		for (int i = 0; i < orOp.getSize(); i++) {
			export(orOp.getParameter(i), writer);
		}
		writer.writeEndElement();
	}

	private static void export(XMLStreamWriter writer, Not not)
			throws XMLStreamException, UnknownCRSException, TransformationException {
		writer.writeStartElement(FES_20_NS, "Not");
		export(not.getParameter(), writer);
		writer.writeEndElement();
	}

	private static void export(Expression expression, XMLStreamWriter writer) throws XMLStreamException {
		if (expression != null) {
			Type expressionType = expression.getType();
			switch (expressionType) {
				case VALUE_REFERENCE:
					export((ValueReference) expression, writer);
					break;
				case LITERAL:
					export((Literal<?>) expression, writer);
					break;
				case FUNCTION:
					export((Function) expression, writer);
					break;
				case CUSTOM:
				default:
					throw new IllegalArgumentException(
							"Encoding of expression type " + expressionType + " is not supported yet!");
			}
		}
	}

	private static void export(ValueReference propertyName, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(FES_20_NS, "ValueReference");
		NamespaceBindings nsBindings = propertyName.getNsContext();
		Iterator<String> prefixIter = nsBindings.getPrefixes();
		while (prefixIter.hasNext()) {
			String prefix = prefixIter.next();
			String ns = nsBindings.getNamespaceURI(prefix);
			writer.writeNamespace(prefix, ns);
		}
		writer.writeCharacters(propertyName.getAsText());
		writer.writeEndElement();
	}

	private static void export(Literal<?> literal, XMLStreamWriter writer) throws XMLStreamException {
		Object value = literal.getValue();
		if (value instanceof TimeGeometricPrimitive) {
			new GmlTimeGeometricPrimitiveWriter().write(writer, (TimeGeometricPrimitive) value);
		}
		else {
			writer.writeStartElement(FES_20_NS, "Literal");
			writer.writeCharacters(value.toString());
			writer.writeEndElement();
		}
	}

	private static void export(Function function, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(FES_20_NS, "Function");
		writer.writeAttribute("name", function.getName());
		for (Expression param : function.getParameters()) {
			export(param, writer);
		}
		writer.writeEndElement();
	}

	private static void exportGeometry(Geometry geometry, GMLStreamWriter gmlWriter)
			throws XMLStreamException, UnknownCRSException, TransformationException {
		if (geometry != null) {
			gmlWriter.setOutputCrs(geometry.getCoordinateSystem());
			gmlWriter.write(geometry);
		}
	}

	private static void exportDistance(Measure distance, XMLStreamWriter writer) throws XMLStreamException {
		if (distance != null) { // in case of Beyond- and DWithin-operators export their
								// distance variable
			QName distanceElementName = new QName(CommonNamespaces.FES_20_NS, "Distance");
			writer.writeStartElement(distanceElementName.getNamespaceURI(), distanceElementName.getLocalPart());
			writer.writeAttribute("uom", distance.getUomUri());
			writer.writeCharacters(distance.getValue().toString());
			writer.writeEndElement();
		}
	}

	private static GMLStreamWriter createGml32StreamWriter(XMLStreamWriter writer) throws XMLStreamException {
		GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter(GML_32, writer);
		gmlWriter.setCoordinateFormatter(new DecimalCoordinateFormatter(3));
		Map<String, String> bindings = new HashMap<String, String>();
		bindings.put(GML_PREFIX, GML3_2_NS);
		writer.writeNamespace(GML_PREFIX, GML3_2_NS);
		gmlWriter.setNamespaceBindings(bindings);
		return gmlWriter;
	}

	private static String retrieveMatchActionAsString(ComparisonOperator operator, XMLStreamWriter writer)
			throws XMLStreamException {
		MatchAction matchAction = operator.getMatchAction();
		if (matchAction != null) {
			switch (matchAction) {
				case ALL:
					return "All";
				case ANY:
					return "Any";
				case ONE:
					return "One";
				default:
					return null;
			}
		}
		return null;
	}

}