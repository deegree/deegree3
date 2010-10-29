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

import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getAttributeValueAsBoolean;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getRequiredAttributeValue;
import static org.deegree.commons.xml.stax.StAXParsingHelper.nextElement;
import static org.deegree.commons.xml.stax.StAXParsingHelper.require;
import static org.deegree.commons.xml.stax.StAXParsingHelper.requireNextTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.genericxml.GenericXMLElementContent;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.utils.ArrayUtils;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.BinaryComparisonOperator;
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
import org.deegree.filter.comparison.ComparisonOperator.SubType;
import org.deegree.filter.expression.Add;
import org.deegree.filter.expression.Div;
import org.deegree.filter.expression.Function;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.Mul;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.expression.Sub;
import org.deegree.filter.expression.custom.CustomExpressionManager;
import org.deegree.filter.expression.custom.CustomExpressionProvider;
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
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.gml.geometry.GML2GeometryReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>Filter100XMLDecoder</code> class is identical to {@link Filter110XMLDecoder} except for:
 * 
 * <ul>
 * <li>the v1.1.0 element 'Envelope' was in v1.0.0 called 'Box'</li>
 * <li>TODO add here more differences</li>
 * </ul>
 * 
 * All the v1.0.0 elements are parsed with {@link GML2GeometryReader}.
 * 
 * It was decided not to extend Filter110XMLDecoder as both filters should have static parse methods that make accessing
 * easier (than creating a superfluous instance beforehand)
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class Filter100XMLDecoder {

    private static final Logger LOG = LoggerFactory.getLogger( Filter110XMLDecoder.class );

    private static final String OGC_NS = "http://www.opengis.net/ogc";

    private static final String GML_NS = "http://www.opengis.net/gml";

    private static final QName FEATURE_ID_ELEMENT = new QName( OGC_NS, "FeatureId" );

    private static final QName FID_ATTR_NAME = new QName( "fid" );

    private static final QName GML_OBJECT_ID_ELEMENT = new QName( OGC_NS, "GmlObjectId" );

    private static final Map<Expression.Type, QName> expressionTypeToElementName = new HashMap<Expression.Type, QName>();

    private static final Map<QName, Expression.Type> elementNameToExpressionType = new HashMap<QName, Expression.Type>();

    private static final Map<QName, Operator.Type> elementNameToOperatorType = new HashMap<QName, Operator.Type>();

    private static final Map<QName, SpatialOperator.SubType> elementNameToSpatialOperatorType = new HashMap<QName, SpatialOperator.SubType>();

    private static final Map<SpatialOperator.SubType, QName> spatialOperatorTypeToElementName = new HashMap<SpatialOperator.SubType, QName>();

    private static final Map<QName, ComparisonOperator.SubType> elementNameToComparisonOperatorType = new HashMap<QName, ComparisonOperator.SubType>();

    private static final Map<ComparisonOperator.SubType, QName> comparisonOperatorTypeToElementName = new HashMap<ComparisonOperator.SubType, QName>();

    private static final Map<QName, LogicalOperator.SubType> elementNameToLogicalOperatorType = new HashMap<QName, LogicalOperator.SubType>();

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

        // element name <-> expression type (custom expressions)
        for ( CustomExpressionProvider ce : CustomExpressionManager.getCustomExpressions().values() ) {
            addElementToExpressionMapping( ce.getElementName(), Expression.Type.CUSTOM );
        }

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
        elementNameToExpressionType.put( elementName, type );
        expressionTypeToElementName.put( type, elementName );
    }

    private static void addElementToSpatialOperatorMapping( QName elementName, SpatialOperator.SubType type ) {
        elementNameToOperatorType.put( elementName, Operator.Type.SPATIAL );
        elementNameToSpatialOperatorType.put( elementName, type );
        spatialOperatorTypeToElementName.put( type, elementName );
    }

    private static void addElementToLogicalOperatorMapping( QName elementName, LogicalOperator.SubType type ) {
        elementNameToOperatorType.put( elementName, Operator.Type.LOGICAL );
        elementNameToLogicalOperatorType.put( elementName, type );
        logicalOperatorTypeToElementName.put( type, elementName );
    }

    private static void addElementToComparisonOperatorMapping( QName elementName, ComparisonOperator.SubType type ) {
        elementNameToOperatorType.put( elementName, Operator.Type.COMPARISON );
        elementNameToComparisonOperatorType.put( elementName, type );
        comparisonOperatorTypeToElementName.put( type, elementName );
    }

    /**
     * Returns the object representation for the given <code>wfs:Filter</code> element event that the cursor of the
     * associated <code>XMLStreamReader</code> points at.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;wfs:Filter&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/wfs:Filter&gt;)</li>
     * </ul>
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;wfs:Filter&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/wfs:Filter&gt;) afterwards
     * @return corresponding {@link Filter} object
     * @throws XMLParsingException
     *             if the element is not a valid "wfs:Filter" element
     * @throws XMLStreamException
     */
    public static Filter parse( XMLStreamReader xmlStream )
                            throws XMLParsingException, XMLStreamException {

        Filter filter = null;
        xmlStream.require( START_ELEMENT, OGC_NS, "Filter" );
        nextElement( xmlStream );
        if ( xmlStream.getEventType() != START_ELEMENT ) {
            throw new XMLParsingException( xmlStream, Messages.getMessage( "FILTER_PARSER_FILTER_EMPTY",
                                                                           new QName( OGC_NS, "Filter" ) ) );
        }
        QName elementName = xmlStream.getName();
        if ( FEATURE_ID_ELEMENT.equals( elementName ) ) {
            LOG.debug( "Building id filter" );
            filter = parseIdFilter( xmlStream );
        } else {
            LOG.debug( "Building operator filter" );
            Operator rootOperator = parseOperator( xmlStream );
            filter = new OperatorFilter( rootOperator );
            nextElement( xmlStream );
            LOG.debug( "Local name: " + xmlStream.getLocalName() );
        }

        LOG.debug( "Local name: " + xmlStream.getLocalName() );
        xmlStream.require( XMLStreamConstants.END_ELEMENT, OGC_NS, "Filter" );
        return filter;
    }

    /**
     * Returns the object representation for the given <code>ogc:expression</code> element event that the cursor of the
     * associated <code>XMLStreamReader</code> points at.
     * <p>
     * The element must be one of the following:
     * <ul>
     * <li>ogc:Add</li>
     * <li>ogc:Sub</li>
     * <li>ogc:Div</li>
     * <li>ogc:Mul</li>
     * <li>ogc:PropertyName</li>
     * <li>ogc:Literal</li>
     * <li>ogc:Function</li>
     * <li>substitution for ogc:expression (handled by {@link CustomExpressionProvider} instance)</li>
     * </ul>
     * </p>
     * <p>
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:expression&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/ogc:expression&gt;)</li>
     * </ul>
     * </p>
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:expression&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/ogc:expression&gt;) afterwards
     * @return corresponding {@link Expression} object
     * @throws XMLParsingException
     *             if the element is not a valid "ogc:expression" element
     * @throws XMLStreamException
     */
    public static Expression parseExpression( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        Expression expression = null;

        // check if element name is a valid expression element
        require( xmlStream, START_ELEMENT );
        Expression.Type type = elementNameToExpressionType.get( xmlStream.getName() );
        if ( type == null ) {
            String msg = Messages.getMessage( "FILTER_PARSER_UNEXPECTED_ELEMENT", xmlStream.getName(),
                                              elemNames( Expression.Type.class, expressionTypeToElementName ) );
            throw new XMLParsingException( xmlStream, msg );
        }

        switch ( type ) {
        case ADD: {
            nextElement( xmlStream );
            Expression param1 = parseExpression( xmlStream );
            nextElement( xmlStream );
            Expression param2 = parseExpression( xmlStream );
            expression = new Add( param1, param2 );
            nextElement( xmlStream );
            break;
        }
        case SUB: {
            nextElement( xmlStream );
            Expression param1 = parseExpression( xmlStream );
            nextElement( xmlStream );
            Expression param2 = parseExpression( xmlStream );
            expression = new Sub( param1, param2 );
            nextElement( xmlStream );
            break;
        }
        case MUL: {
            nextElement( xmlStream );
            Expression param1 = parseExpression( xmlStream );
            nextElement( xmlStream );
            Expression param2 = parseExpression( xmlStream );
            expression = new Mul( param1, param2 );
            nextElement( xmlStream );
            break;
        }
        case DIV: {
            nextElement( xmlStream );
            Expression param1 = parseExpression( xmlStream );
            nextElement( xmlStream );
            Expression param2 = parseExpression( xmlStream );
            expression = new Div( param1, param2 );
            nextElement( xmlStream );
            break;
        }
        case PROPERTY_NAME: {
            expression = parsePropertyName( xmlStream );
            break;
        }
        case LITERAL: {
            expression = parseLiteral( xmlStream );
            break;
        }
        case FUNCTION: {
            expression = parseFunction( xmlStream );
            break;
        }
        case CUSTOM: {
            expression = parseCustomExpression( xmlStream );
            break;
        }
        }
        return expression;
    }

    /**
     * Returns the object representation for the given <code>ogc:Function</code> element event that the cursor of the
     * associated <code>XMLStreamReader</code> points at.
     * <p>
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:Function&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/ogc:Function&gt;)</li>
     * </ul>
     * </p>
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:Function&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/ogc:Function&gt;) afterwards
     * @return corresponding {@link Function} object
     * @throws XMLParsingException
     *             if the element is not a valid "ogc:Function" element
     * @throws XMLStreamException
     */
    public static Function parseFunction( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        xmlStream.require( START_ELEMENT, OGC_NS, "Function" );
        String name = getRequiredAttributeValue( xmlStream, "name" );
        nextElement( xmlStream );
        List<Expression> params = new ArrayList<Expression>();
        while ( xmlStream.getEventType() == START_ELEMENT ) {
            params.add( parseExpression( xmlStream ) );
            nextElement( xmlStream );
        }
        xmlStream.require( END_ELEMENT, OGC_NS, "Function" );

        Function function = null;
        FunctionProvider provider = FunctionManager.getFunctionProvider( name );
        if ( provider != null ) {
            function = provider.create( params );
        } else {
            function = new Function( name, params );
        }
        return function;
    }

    /**
     * Returns the object representation for the given custom <code>ogc:expression</code> element event that the cursor
     * of the associated <code>XMLStreamReader</code> points at.
     * <p>
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:expression&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/ogc:expression&gt;)</li>
     * </ul>
     * </p>
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:expression&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/ogc:expression&gt;) afterwards
     * @return corresponding {@link CustomExpressionProvider} object
     * @throws XMLParsingException
     *             if the element is not a known or valid custom "ogc:expression" element
     * @throws XMLStreamException
     */
    public static CustomExpressionProvider parseCustomExpression( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        CustomExpressionProvider expr = CustomExpressionManager.getExpression( xmlStream.getName() );
        if ( expr == null ) {
            String msg = Messages.getMessage( "FILTER_PARSER_UNKNOWN_CUSTOM_EXPRESSION", xmlStream.getName() );
            throw new XMLParsingException( xmlStream, msg );
        }
        return expr.parse100( xmlStream );
    }

    /**
     * Returns the object representation for the given <code>ogc:comparisonOps</code> element event that the cursor of
     * the associated <code>XMLStreamReader</code> points at.
     * <p>
     * The element must be one of the following:
     * <ul>
     * <li>ogc:PropertyIsEqualTo</li>
     * <li>ogc:PropertyIsGreaterThan</li>
     * <li>ogc:PropertyIsGreaterThanOrEqualTo</li>
     * <li>ogc:PropertyIsLessThan</li>
     * <li>ogc:PropertyIsLessThanOrEqualTo</li>
     * <li>ogc:PropertyIsNotEqualTo</li>
     * <li>ogc:PropertyIsBetween</li>
     * <li>ogc:PropertyIsLike</li>
     * <li>ogc:PropertyIsNull</li>
     * </ul>
     * </p>
     * <p>
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:comparisonOps&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/ogc:comparisonOps&gt;)
     * </li>
     * </ul>
     * </p>
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:comparisonOps&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/ogc:comparisonOps&gt;) afterwards
     * @return corresponding {@link Expression} object
     * @throws XMLParsingException
     *             if the element is not a valid "ogc:comparisonOps" element
     * @throws XMLStreamException
     */
    public static ComparisonOperator parseComparisonOperator( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        ComparisonOperator comparisonOperator = null;

        // check if element name is a valid comparison operator element
        ComparisonOperator.SubType type = elementNameToComparisonOperatorType.get( xmlStream.getName() );
        if ( type == null ) {
            String msg = Messages.getMessage( "FILTER_PARSER_UNEXPECTED_ELEMENT", xmlStream.getName(),
                                              elemNames( ComparisonOperator.SubType.class,
                                                         comparisonOperatorTypeToElementName ) );
            throw new XMLParsingException( xmlStream, msg );
        }

        switch ( type ) {
        case PROPERTY_IS_EQUAL_TO:
        case PROPERTY_IS_GREATER_THAN:
        case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
        case PROPERTY_IS_LESS_THAN:
        case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
        case PROPERTY_IS_NOT_EQUAL_TO:
            comparisonOperator = parseBinaryComparisonOperator( xmlStream, type );
            break;
        case PROPERTY_IS_BETWEEN:
            comparisonOperator = parsePropertyIsBetweenOperator( xmlStream );
            break;
        case PROPERTY_IS_LIKE:
            comparisonOperator = parsePropertyIsLikeOperator( xmlStream );
            break;
        case PROPERTY_IS_NULL:
            comparisonOperator = parsePropertyIsNullOperator( xmlStream );
            break;
        }
        return comparisonOperator;
    }

    private static Operator parseOperator( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        Operator operator = null;

        // check if element name is a valid operator element
        Operator.Type type = elementNameToOperatorType.get( xmlStream.getName() );
        if ( type == null ) {
            String expectedList = elemNames( Operator.Type.class, logicalOperatorTypeToElementName ) + ", "
                                  + elemNames( Operator.Type.class, spatialOperatorTypeToElementName ) + ", "
                                  + elemNames( Operator.Type.class, comparisonOperatorTypeToElementName );
            String msg = Messages.getMessage( "FILTER_PARSER_UNEXPECTED_ELEMENT", xmlStream.getName(), expectedList );
            throw new XMLParsingException( xmlStream, msg );
        }

        switch ( type ) {
        case COMPARISON:
            LOG.debug( "Building comparison operator" );
            operator = parseComparisonOperator( xmlStream );
            break;
        case LOGICAL:
            LOG.debug( "Building logical operator" );
            operator = parseLogicalOperator( xmlStream );
            break;
        case SPATIAL:
            LOG.debug( "Building spatial operator" );
            operator = parseSpatialOperator( xmlStream );
            break;
        }
        return operator;
    }

    private static IdFilter parseIdFilter( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        Set<String> matchingIds = new HashSet<String>();

        while ( xmlStream.getEventType() == START_ELEMENT ) {
            QName childElementName = xmlStream.getName();
            if ( FEATURE_ID_ELEMENT.equals( childElementName ) ) {
                String id = xmlStream.getAttributeValue( null, "fid" );
                if ( id == null || id.length() == 0 ) {
                    String msg = Messages.getMessage( "FILTER_PARSER_ID_FILTER_NO_ID", FEATURE_ID_ELEMENT,
                                                      FID_ATTR_NAME );
                    throw new XMLParsingException( xmlStream, msg );
                }
                matchingIds.add( id );
                nextElement( xmlStream );
                xmlStream.require( XMLStreamConstants.END_ELEMENT, OGC_NS, "FeatureId" );
            } else {
                String msg = Messages.getMessage( "FILTER_PARSER_ID_FILTER_UNEXPECTED_ELEMENT", childElementName,
                                                  GML_OBJECT_ID_ELEMENT, FEATURE_ID_ELEMENT );
                throw new XMLParsingException( xmlStream, msg );
            }
            nextElement( xmlStream );
        }
        return new IdFilter( matchingIds );
    }

    private static ComparisonOperator parseBinaryComparisonOperator( XMLStreamReader xmlStream, SubType type )
                            throws XMLStreamException {

        BinaryComparisonOperator comparisonOperator = null;

        // this is a deegree extension over Filter 1.0.0 spec.
        boolean matchCase = getAttributeValueAsBoolean( xmlStream, null, "matchCase", true );

        requireNextTag( xmlStream, START_ELEMENT );
        Expression parameter1 = parseExpression( xmlStream );
        requireNextTag( xmlStream, START_ELEMENT );
        Expression parameter2 = parseExpression( xmlStream );
        requireNextTag( xmlStream, END_ELEMENT );

        switch ( type ) {
        case PROPERTY_IS_EQUAL_TO:
            comparisonOperator = new PropertyIsEqualTo( parameter1, parameter2, matchCase );
            break;
        case PROPERTY_IS_NOT_EQUAL_TO:
            comparisonOperator = new PropertyIsNotEqualTo( parameter1, parameter2, matchCase );
            break;
        case PROPERTY_IS_LESS_THAN:
            comparisonOperator = new PropertyIsLessThan( parameter1, parameter2, matchCase );
            break;
        case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
            comparisonOperator = new PropertyIsLessThanOrEqualTo( parameter1, parameter2, matchCase );
            break;
        case PROPERTY_IS_GREATER_THAN:
            comparisonOperator = new PropertyIsGreaterThan( parameter1, parameter2, matchCase );
            break;
        case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
            comparisonOperator = new PropertyIsGreaterThanOrEqualTo( parameter1, parameter2, matchCase );
            break;
        default:
            assert false;
        }
        return comparisonOperator;
    }

    private static Literal<?> parseLiteral( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        Map<QName, PrimitiveValue> attrs = parseAttrs( xmlStream );
        List<TypedObjectNode> children = new ArrayList<TypedObjectNode>();
        while ( xmlStream.next() != END_ELEMENT ) {
            int eventType = xmlStream.getEventType();
            if ( eventType == START_ELEMENT ) {
                children.add( parseElement( xmlStream ) );
            } else if ( eventType == CHARACTERS ) {
                children.add( new PrimitiveValue( xmlStream.getText() ) );
            }
        }
        TypedObjectNode value = null;
        if ( attrs == null || children.size() == 1 ) {
            value = children.get( 0 );
        } else {
            value = new GenericXMLElementContent( null, attrs, children );
        }
        return new Literal<TypedObjectNode>( value );
    }

    private static GenericXMLElement parseElement( XMLStreamReader xmlStream )
                            throws IllegalArgumentException, XMLStreamException {
        Map<QName, PrimitiveValue> attrs = parseAttrs( xmlStream );
        List<TypedObjectNode> children = new ArrayList<TypedObjectNode>();
        while ( xmlStream.next() != END_ELEMENT ) {
            int eventType = xmlStream.getEventType();
            if ( eventType == START_ELEMENT ) {
                children.add( parseElement( xmlStream ) );
            } else if ( eventType == CHARACTERS ) {
                children.add( new PrimitiveValue( xmlStream.getText() ) );
            }
        }
        return new GenericXMLElement( xmlStream.getName(), null, attrs, children );
    }

    private static Map<QName, PrimitiveValue> parseAttrs( XMLStreamReader xmlStream ) {
        Map<QName, PrimitiveValue> attrs = new LinkedHashMap<QName, PrimitiveValue>();
        for ( int i = 0; i < xmlStream.getAttributeCount(); i++ ) {
            QName name = xmlStream.getAttributeName( i );
            String value = xmlStream.getAttributeValue( i );
            PrimitiveValue xmlValue = new PrimitiveValue( value );
            attrs.put( name, xmlValue );
        }
        return attrs;
    }

    private static PropertyName parsePropertyName( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        NamespaceContext nsc = StAXParsingHelper.getDeegreeNamespaceContext( xmlStream );
        String propName = xmlStream.getElementText().trim();
        if ( propName.isEmpty() ) {
            // TODO filter encoding guy: use whatever exception shall be used here. But make sure that the
            // GetObservation100XMLAdapter gets an exception from here as the compliance of the SOS hangs on it's thread
            throw new XMLParsingException( xmlStream, Messages.getMessage( "FILTER_PARSER_PROPERTY_NAME_EMPTY",
                                                                           new QName( OGC_NS, "PropertyName" ) ) );
        }
        return new PropertyName( propName, nsc );
    }

    private static PropertyIsBetween parsePropertyIsBetweenOperator( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        // this is a deegree extension over Filter 1.0.0 spec.
        boolean matchCase = getAttributeValueAsBoolean( xmlStream, null, "matchCase", true );

        nextElement( xmlStream );
        Expression expression = parseExpression( xmlStream );

        nextElement( xmlStream );
        xmlStream.require( START_ELEMENT, OGC_NS, "LowerBoundary" );
        nextElement( xmlStream );
        Expression lowerBoundary = parseExpression( xmlStream );

        nextElement( xmlStream ); // </ expression >
        nextElement( xmlStream ); // </LowerBoundary>

        xmlStream.require( START_ELEMENT, OGC_NS, "UpperBoundary" );
        nextElement( xmlStream );
        Expression upperBoundary = parseExpression( xmlStream );

        nextElement( xmlStream ); // </ expression >
        nextElement( xmlStream ); // </UowerBoundary>
        return new PropertyIsBetween( expression, lowerBoundary, upperBoundary, matchCase );
    }

    private static PropertyIsLike parsePropertyIsLikeOperator( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        // this is a deegree extension over Filter 1.0.0
        boolean matchCase = getAttributeValueAsBoolean( xmlStream, null, "matchCase", true );

        String wildCard = getRequiredAttributeValue( xmlStream, "wildCard" );
        String singleChar = getRequiredAttributeValue( xmlStream, "singleChar" );
        String escapeChar = getRequiredAttributeValue( xmlStream, "escape" );

        nextElement( xmlStream );
        PropertyName propName = parsePropertyName( xmlStream );

        nextElement( xmlStream );
        Literal<?> literal = parseLiteral( xmlStream );
        nextElement( xmlStream );
        return new PropertyIsLike( propName, literal, wildCard, singleChar, escapeChar, matchCase );
    }

    private static PropertyIsNull parsePropertyIsNullOperator( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        nextElement( xmlStream );
        PropertyName propName = parsePropertyName( xmlStream );
        nextElement( xmlStream );
        return new PropertyIsNull( propName );
    }

    private static LogicalOperator parseLogicalOperator( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        LogicalOperator logicalOperator = null;

        // check if element name is a valid logical operator element
        LogicalOperator.SubType type = elementNameToLogicalOperatorType.get( xmlStream.getName() );
        if ( type == null ) {
            String msg = Messages.getMessage( "FILTER_PARSER_UNEXPECTED_ELEMENT", xmlStream.getName(),
                                              elemNames( LogicalOperator.SubType.class,
                                                         logicalOperatorTypeToElementName ) );
            throw new XMLParsingException( xmlStream, msg );
        }

        switch ( type ) {
        case AND: {
            List<Operator> innerOperators = new ArrayList<Operator>();
            while ( nextElement( xmlStream ) == START_ELEMENT ) {
                innerOperators.add( parseOperator( xmlStream ) );
            }
            if ( innerOperators.size() < 2 ) {
                String msg = "Error while parsing And operator. Must have at least two arguments.";
                throw new XMLParsingException( xmlStream, msg );
            }
            logicalOperator = new And( innerOperators.toArray( new Operator[innerOperators.size()] ) );
            break;
        }
        case OR: {
            List<Operator> innerOperators = new ArrayList<Operator>();
            while ( nextElement( xmlStream ) == START_ELEMENT ) {
                innerOperators.add( parseOperator( xmlStream ) );
            }
            if ( innerOperators.size() < 2 ) {
                String msg = "Error while parsing Or operator. Must have at least two arguments.";
                throw new XMLParsingException( xmlStream, msg );
            }
            logicalOperator = new Or( innerOperators.toArray( new Operator[innerOperators.size()] ) );
            break;
        }
        case NOT: {
            nextElement( xmlStream );
            Operator parameter = parseOperator( xmlStream );
            logicalOperator = new Not( parameter );
            nextElement( xmlStream );
            break;
        }
        }
        return logicalOperator;
    }

    private static SpatialOperator parseSpatialOperator( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        SpatialOperator spatialOperator = null;

        require( xmlStream, START_ELEMENT );
        // check if element name is a valid spatial operator element name
        SpatialOperator.SubType type = elementNameToSpatialOperatorType.get( xmlStream.getName() );
        if ( type == null ) {
            String msg = Messages.getMessage( "FILTER_PARSER_UNEXPECTED_ELEMENT", xmlStream.getName(),
                                              elemNames( SpatialOperator.SubType.class,
                                                         spatialOperatorTypeToElementName ) );
            throw new XMLParsingException( xmlStream, msg );
        }

        nextElement( xmlStream );

        // TODO remove this after GML parser is adapted
        XMLStreamReaderWrapper wrapper = new XMLStreamReaderWrapper( xmlStream, null );
        GML2GeometryReader geomParser = new GML2GeometryReader();

        // always first parameter: 'ogc:PropertyName'
        PropertyName param1 = parsePropertyName( xmlStream );
        nextElement( xmlStream );

        try {
            switch ( type ) {
            case BBOX: {
                // second parameter: 'gml:Box'
                xmlStream.require( START_ELEMENT, GML_NS, "Box" );
                Envelope param2 = geomParser.parseEnvelope( wrapper );
                spatialOperator = new BBOX( param1, param2 );
                break;
            }
            case BEYOND: {
                // second parameter: 'gml:_Geometry'
                Geometry param2 = geomParser.parse( wrapper );
                // third parameter: 'ogc:Distance'
                nextElement( xmlStream );
                xmlStream.require( START_ELEMENT, OGC_NS, "Distance" );
                String distanceUnits = getRequiredAttributeValue( xmlStream, "units" );
                String distanceValue = xmlStream.getElementText();
                // In Filter 1.0.0 (with distinction to 1.1.0) the <Distance> element IS supposed to contain text, e.g.
                // a value. The unit of measure is in the "units" attribute. This differs with Filter 1.1.0.
                Measure distance = new Measure( distanceValue, distanceUnits );
                spatialOperator = new Beyond( param1, param2, distance );
                break;
            }
            case INTERSECTS: {
                // second parameter: 'gml:_Geometry' or 'gml:Box'
                Geometry param2 = geomParser.parseGeometryOrBox( wrapper );
                spatialOperator = new Intersects( param1, param2 );
                break;
            }
            case CONTAINS: {
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrBox( wrapper );
                spatialOperator = new Contains( param1, param2 );
                break;
            }
            case CROSSES: {
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrBox( wrapper );
                spatialOperator = new Crosses( param1, param2 );
                break;
            }
            case DISJOINT: {
                // second parameter: 'gml:_Geometry'
                Geometry param2 = geomParser.parse( wrapper );
                spatialOperator = new Disjoint( param1, param2 );
                break;
            }
            case DWITHIN: {
                // second parameter: 'gml:_Geometry'
                Geometry param2 = geomParser.parse( wrapper );
                // third parameter: 'ogc:Distance'
                nextElement( xmlStream );
                xmlStream.require( START_ELEMENT, OGC_NS, "Distance" );
                String distanceUnits = getRequiredAttributeValue( xmlStream, "units" );
                String distanceValue = xmlStream.getElementText();
                // In Filter 1.0.0 (with distinction to 1.1.0) the <Distance> element IS supposed to contain text, e.g.
                // a value. The unit of measure is in the "units" attribute. This differs with Filter 1.1.0.
                Measure distance = new Measure( distanceValue, distanceUnits );
                spatialOperator = new DWithin( param1, param2, distance );
                break;
            }
            case EQUALS: {
                // second parameter: 'gml:_Geometry'
                Geometry param2 = geomParser.parse( wrapper, null );
                spatialOperator = new Equals( param1, param2 );
                break;
            }
            case OVERLAPS: {
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrBox( wrapper );
                spatialOperator = new Overlaps( param1, param2 );
                break;
            }
            case TOUCHES: {
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrBox( wrapper );
                spatialOperator = new Touches( param1, param2 );
                break;
            }
            case WITHIN: {
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrBox( wrapper );
                spatialOperator = new Within( param1, param2 );
            }
            }
        } catch ( UnknownCRSException e ) {
            throw new XMLParsingException( xmlStream, e.getMessage() );
        }
        nextElement( xmlStream );
        return spatialOperator;
    }

    /**
     * Return a String with all element names of the given enum class.
     * 
     * @param enumClass
     * @param map
     *            the operator type -> element name map
     * @return a coma separated list of element names
     */
    private static String elemNames( Class<? extends Enum<?>> enumClass, Map<? extends Enum<?>, QName> map ) {
        List<String> names = new LinkedList<String>();
        for ( Enum<?> e : enumClass.getEnumConstants() ) {
            QName qname = map.get( e );
            if ( qname == null ) {
                LOG.warn( "No value for " + e );
            } else {
                names.add( qname.toString() );
            }
        }
        return ArrayUtils.join( ", ", names );
    }
}