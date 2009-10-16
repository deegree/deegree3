//$HeadURL$
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.expression.Sub;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.logical.Not;
import org.deegree.filter.logical.Or;
import org.deegree.filter.spatial.SpatialOperator;

/**
 * Encodes {@link Filter} objects according to the Filter Encoding Specification 1.1.0.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class Filter110XMLEncoder {

    private static final String OGC_NS = "http://www.opengis.net/ogc";

    private static final QName NAME_ATTR = new QName( "name" );

    private static final Map<Expression.Type, QName> expressionTypeToElementName = new HashMap<Expression.Type, QName>();

    private static final Map<SpatialOperator.SubType, QName> spatialOperatorTypeToElementName = new HashMap<SpatialOperator.SubType, QName>();

    private static final Map<ComparisonOperator.SubType, QName> comparisonOperatorTypeToElementName = new HashMap<ComparisonOperator.SubType, QName>();

    private static final Map<LogicalOperator.SubType, QName> logicalOperatorTypeToElementName = new HashMap<LogicalOperator.SubType, QName>();

    static {
        // element name <-> expression type
        addElementToExpressionMapping( new QName( OGC_NS, "Add" ), Expression.Type.ADD );
        addElementToExpressionMapping( new QName( OGC_NS, "Sub" ), Expression.Type.SUB );
        addElementToExpressionMapping( new QName( OGC_NS, "Mul" ), Expression.Type.MUL );
        addElementToExpressionMapping( new QName( OGC_NS, "Div" ), Expression.Type.DIV );
        addElementToExpressionMapping( new QName( OGC_NS, "PropertyName" ), Expression.Type.PROPERTY_NAME );
        addElementToExpressionMapping( new QName( OGC_NS, "Function" ), Expression.Type.FUNCTION );
        addElementToExpressionMapping( new QName( OGC_NS, "Literal" ), Expression.Type.LITERAL );

        // element name <-> spatial operator type
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "BBOX" ), SpatialOperator.SubType.BBOX );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Beyond" ), SpatialOperator.SubType.BEYOND );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Contains" ), SpatialOperator.SubType.CONTAINS );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Crosses" ), SpatialOperator.SubType.CROSSES );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Equals" ), SpatialOperator.SubType.EQUALS );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Disjoint" ), SpatialOperator.SubType.DISJOINT );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "DWithin" ), SpatialOperator.SubType.DWITHIN );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Intersects" ), SpatialOperator.SubType.INTERSECTS );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Overlaps" ), SpatialOperator.SubType.OVERLAPS );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Touches" ), SpatialOperator.SubType.TOUCHES );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Within" ), SpatialOperator.SubType.WITHIN );

        // element name <-> logical operator type
        addElementToLogicalOperatorMapping( new QName( OGC_NS, "And" ), LogicalOperator.SubType.AND );
        addElementToLogicalOperatorMapping( new QName( OGC_NS, "Or" ), LogicalOperator.SubType.OR );
        addElementToLogicalOperatorMapping( new QName( OGC_NS, "Not" ), LogicalOperator.SubType.NOT );

        // element name <-> comparison operator type
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsBetween" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_BETWEEN );
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsEqualTo" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_EQUAL_TO );
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsGreaterThan" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_GREATER_THAN );
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsGreaterThanOrEqualTo" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO );
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsLessThan" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_LESS_THAN );
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsLessThanOrEqualTo" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_LESS_THAN_OR_EQUAL_TO );
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsLike" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_LIKE );
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsNotEqualTo" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_NOT_EQUAL_TO );
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsNull" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_NULL );
    }

    private static void addElementToExpressionMapping( QName elementName, Expression.Type type ) {
        expressionTypeToElementName.put( type, elementName );
    }

    private static void addElementToSpatialOperatorMapping( QName elementName, SpatialOperator.SubType type ) {
        spatialOperatorTypeToElementName.put( type, elementName );
    }

    private static void addElementToLogicalOperatorMapping( QName elementName, LogicalOperator.SubType type ) {
        logicalOperatorTypeToElementName.put( type, elementName );
    }

    private static void addElementToComparisonOperatorMapping( QName elementName, ComparisonOperator.SubType type ) {
        comparisonOperatorTypeToElementName.put( type, elementName );
    }

    /**
     * Serializes the given {@link Filter} object to XML.
     * 
     * @param filter
     *            <code>Filter</code> object to be serialized
     * @param writer
     *            target of the xml stream
     * @throws XMLStreamException
     */
    public static void export( Filter filter, XMLStreamWriter writer )
                            throws XMLStreamException {

        writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );

        writer.writeStartElement( OGC_NS, "Filter" );
        switch ( filter.getType() ) {
        case ID_FILTER:
            Collection<String> ids = ( (IdFilter) filter ).getMatchingIds();
            for ( String id : ids ) {
                writer.writeStartElement( OGC_NS, "GmlObjectId" );
                writer.writeCharacters( id );
                writer.writeEndElement();
            }
            break;
        case OPERATOR_FILTER:
            export( ( (OperatorFilter) filter ).getOperator(), writer );
            break;
        }
        writer.writeEndElement();
    }

    /**
     * Serializes the given {@link Operator} object to XML.
     * 
     * @param operator
     *            <code>BooleanOperator</code> object to be serialized
     * @param writer
     *            target of the xml stream
     * @throws XMLStreamException
     */
    private static void export( Operator operator, XMLStreamWriter writer )
                            throws XMLStreamException {
        switch ( operator.getType() ) {
        case COMPARISON:
            export( (ComparisonOperator) operator, writer );
            break;
        case LOGICAL:
            export( (LogicalOperator) operator, writer );
            break;
        case SPATIAL:
            export( (SpatialOperator) operator, writer );
            break;
        }
    }

    /**
     * Serializes the given {@link LogicalOperator} object to XML.
     * 
     * @param operator
     *            <code>LogicalOperator</code> object to be serialized
     * @param writer
     *            target of the xml stream
     * @throws XMLStreamException
     */
    private static void export( LogicalOperator operator, XMLStreamWriter writer )
                            throws XMLStreamException {

        QName elementName = logicalOperatorTypeToElementName.get( operator.getSubType() );
        writer.writeStartElement( elementName.getNamespaceURI(), elementName.getLocalPart() );

        switch ( operator.getSubType() ) {
        case AND:
            And andOp = (And) operator;
            for ( int i = 0; i < andOp.getSize(); i++ ) {
                export( andOp.getParameter( i ), writer );
            }
            break;
        case OR:
            Or orOp = (Or) operator;
            for ( int i = 0; i < orOp.getSize(); i++ ) {
                export( orOp.getParameter( i ), writer );
            }
            break;
        case NOT:
            export( ( (Not) operator ).getParameter(), writer );
            break;
        }

        writer.writeEndElement();
    }

    /**
     * Serializes the given {@link ComparisonOperator} object to XML.
     * 
     * @param operator
     *            <code>ComparisonOperator</code> object to be serialized
     * @param writer
     *            target of the xml stream
     * @throws XMLStreamException
     */
    private static void export( ComparisonOperator operator, XMLStreamWriter writer )
                            throws XMLStreamException {

        QName elementName = comparisonOperatorTypeToElementName.get( operator.getSubType() );
        writer.writeStartElement( elementName.getNamespaceURI(), elementName.getLocalPart() );

        switch ( operator.getSubType() ) {
        case PROPERTY_IS_BETWEEN:
            PropertyIsBetween isBetween = (PropertyIsBetween) operator;
            export( isBetween.getExpression(), writer );
            writer.writeStartElement( OGC_NS, "LowerBoundary" );
            writer.writeEndElement();
            writer.writeStartElement( OGC_NS, "UpperBoundary" );
            writer.writeEndElement();
            break;
        case PROPERTY_IS_EQUAL_TO:
            export( ( (PropertyIsEqualTo) operator ).getParameter1(), writer );
            export( ( (PropertyIsEqualTo) operator ).getParameter2(), writer );
            break;
        case PROPERTY_IS_GREATER_THAN:
            export( ( (PropertyIsGreaterThan) operator ).getParameter1(), writer );
            export( ( (PropertyIsGreaterThan) operator ).getParameter2(), writer );
            break;
        case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
            export( ( (PropertyIsGreaterThanOrEqualTo) operator ).getParameter1(), writer );
            export( ( (PropertyIsGreaterThanOrEqualTo) operator ).getParameter2(), writer );
            break;
        case PROPERTY_IS_LESS_THAN:
            export( ( (PropertyIsLessThan) operator ).getParameter1(), writer );
            export( ( (PropertyIsLessThan) operator ).getParameter2(), writer );
            break;
        case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
            export( ( (PropertyIsLessThanOrEqualTo) operator ).getParameter1(), writer );
            export( ( (PropertyIsLessThanOrEqualTo) operator ).getParameter2(), writer );
            break;
        case PROPERTY_IS_LIKE:
            PropertyIsLike isLikeOperator = (PropertyIsLike) operator;
            writer.writeAttribute( "wildCard", isLikeOperator.getWildCard() );
            writer.writeAttribute( "singleChar", isLikeOperator.getSingleChar() );
            writer.writeAttribute( "escapeChar", isLikeOperator.getEscapeChar() );
            export( isLikeOperator.getPropertyName(), writer );
            export( isLikeOperator.getLiteral(), writer );
            break;
        case PROPERTY_IS_NOT_EQUAL_TO:
            export( ( (PropertyIsNotEqualTo) operator ).getParameter1(), writer );
            export( ( (PropertyIsNotEqualTo) operator ).getParameter2(), writer );
            break;
        case PROPERTY_IS_NULL:
            export( ( (PropertyIsNull) operator ).getPropertyName(), writer );
            break;
        }

        writer.writeEndElement();
    }

    /**
     * Serializes the given {@link SpatialOperator} object to XML.
     * 
     * @param operator
     *            <code>SpatialOperator</code> object to be serialized
     * @param writer
     *            target of the xml stream
     * @throws XMLStreamException
     */
    private static void export( SpatialOperator operator, XMLStreamWriter writer )
                            throws XMLStreamException {

        QName elementName = spatialOperatorTypeToElementName.get( operator.getSubType() );
        writer.writeStartElement( elementName.getNamespaceURI(), elementName.getLocalPart() );

        switch ( operator.getSubType() ) {
        // TODO implement me
        case BBOX:
        case BEYOND:
        case CONTAINS:
        case CROSSES:
        case DISJOINT:
        case DWITHIN:
        case EQUALS:
        case INTERSECTS:
        case OVERLAPS:
        case TOUCHES:
        case WITHIN:
        }

        writer.writeEndElement();
    }

    /**
     * Serializes the given {@link Expression} object to XML.
     * 
     * @param expression
     *            <code>Expression</code> object to be serialized
     * @param writer
     *            target of the xml stream
     * @throws XMLStreamException
     */
    public static void export( Expression expression, XMLStreamWriter writer )
                            throws XMLStreamException {

        QName elementName = expressionTypeToElementName.get( expression.getType() );
        writer.writeStartElement( elementName.getNamespaceURI(), elementName.getLocalPart() );

        switch ( expression.getType() ) {
        case PROPERTY_NAME:
            writer.writeCharacters( ( (PropertyName) expression ).getPropertyName() );
            break;
        case LITERAL:
            // TODO handle complex literals
            writer.writeCharacters( ( (Literal<?>) expression ).getValue().toString() );
            break;
        case FUNCTION:
            Function function = (Function) expression;
            writer.writeAttribute( NAME_ATTR.getLocalPart(), function.getName() );
            for ( Expression param : function.getParameters() ) {
                export( param, writer );
            }
            break;
        case ADD:
            export( ( (Add) expression ).getParameter1(), writer );
            export( ( (Add) expression ).getParameter2(), writer );
            break;
        case SUB:
            export( ( (Sub) expression ).getParameter1(), writer );
            export( ( (Sub) expression ).getParameter2(), writer );
            break;
        case MUL:
            export( ( (Mul) expression ).getParameter1(), writer );
            export( ( (Mul) expression ).getParameter2(), writer );
            break;
        case DIV:
            export( ( (Div) expression ).getParameter1(), writer );
            export( ( (Div) expression ).getParameter2(), writer );
            break;
        }

        writer.writeEndElement();
    }
}
