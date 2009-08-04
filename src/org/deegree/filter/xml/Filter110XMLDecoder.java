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

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.utils.ArrayUtils;
import org.deegree.commons.xml.FixedChildIterator;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.exceptions.UnknownCRSException;
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
import org.deegree.filter.expression.Add;
import org.deegree.filter.expression.Div;
import org.deegree.filter.expression.Function;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.Mul;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.expression.Sub;
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
import org.deegree.geometry.gml.GML311GeometryDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decodes XML documents that comply to the OGC Filter Encoding Specification 1.1.0 as {@link Filter} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class Filter110XMLDecoder extends XMLAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( Filter110XMLDecoder.class );

    private static final String OGC_NS = "http://www.opengis.net/ogc";

    private static final String GML_NS = "http://www.opengis.net/gml";

    private static final QName FEATURE_ID_ELEMENT = new QName( OGC_NS, "FeatureId" );

    private static final QName FID_ATTR_NAME = new QName( "fid" );

    private static final QName GML_OBJECT_ID_ELEMENT = new QName( OGC_NS, "GmlObjectId" );

    private static final QName GML_ID_ATTR_NAME = new QName( GML_NS, "id" );

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
     * Parses the encapsulated root element as a {@link Filter}.
     * <p>
     * The element must be a {http://www.opengis.net/ogc}Filter element.
     * 
     * @return <code>Filter</code> object
     * @throws XMLParsingException
     */
    public Filter parse()
                            throws XMLParsingException {

        Filter filter = null;

        Iterator<?> childIterator = rootElement.getChildElements();
        if ( !childIterator.hasNext() ) {
            throw new XMLParsingException( this, rootElement, Messages.getMessage( "FILTER_PARSER_FILTER_EMPTY",
                                                                                   rootElement.getQName() ) );
        }

        OMElement element = (OMElement) childIterator.next();
        QName elementName = element.getQName();
        if ( GML_OBJECT_ID_ELEMENT.equals( elementName ) || FEATURE_ID_ELEMENT.equals( elementName ) ) {
            LOG.debug( "Building id filter" );
            filter = parseIdFilter( rootElement );
        } else {
            LOG.debug( "Building operator filter" );
            Operator rootOperator = parseOperator( element );
            filter = new OperatorFilter( rootOperator );
        }
        return filter;
    }

    /**
     * Parses the given element as an {@link IdFilter}.
     * <p>
     * The element must be a {http://www.opengis.net/ogc}Filter element.
     * 
     * @return <code>Filter</code> object
     * @throws XMLProcessingException
     */
    private IdFilter parseIdFilter( OMElement element ) {

        Set<String> matchedIds = new HashSet<String>();

        Iterator<?> childElementIter = element.getChildElements();
        while ( childElementIter.hasNext() ) {
            OMElement childElement = (OMElement) childElementIter.next();
            QName childElementName = childElement.getQName();
            if ( GML_OBJECT_ID_ELEMENT.equals( childElementName ) ) {
                String id = childElement.getAttributeValue( GML_ID_ATTR_NAME );
                if ( id == null || id.length() == 0 ) {
                    String msg = Messages.getMessage( "FILTER_PARSER_ID_FILTER_NO_ID", GML_OBJECT_ID_ELEMENT,
                                                      GML_ID_ATTR_NAME );
                    throw new XMLParsingException( this, childElement, msg );
                }
                matchedIds.add( id );
            } else if ( FEATURE_ID_ELEMENT.equals( childElementName ) ) {
                String id = childElement.getAttributeValue( FID_ATTR_NAME );
                if ( id == null || id.length() == 0 ) {
                    String msg = Messages.getMessage( "FILTER_PARSER_ID_FILTER_NO_ID", FEATURE_ID_ELEMENT,
                                                      FID_ATTR_NAME );
                    throw new XMLParsingException( this, childElement, msg );
                }
                matchedIds.add( id );
            } else {
                String msg = Messages.getMessage( "FILTER_PARSER_ID_FILTER_UNEXPECTED_ELEMENT", childElementName,
                                                  GML_OBJECT_ID_ELEMENT, FEATURE_ID_ELEMENT );
                throw new XMLParsingException( this, childElement, msg );
            }
        }
        return new IdFilter( matchedIds );
    }

    /**
     * Parses the given element as an {@link Expression}.
     * <p>
     * The element must be one of the following:
     * <ul>
     * <li>{http://www.opengis.net/ogc}Add</li>
     * <li>{http://www.opengis.net/ogc}Sub</li>
     * <li>{http://www.opengis.net/ogc}Div</li>
     * <li>{http://www.opengis.net/ogc}Mul</li>
     * <li>{http://www.opengis.net/ogc}PropertyName</li>
     * <li>{http://www.opengis.net/ogc}Literal</li>
     * <li>{http://www.opengis.net/ogc}Function</li>
     * </ul>
     * 
     * @param element
     *            element to be parsed
     * @return expression object
     */
    public Expression parseExpression( OMElement element ) {

        Expression expression = null;

        // check if element name is a valid expression element
        Expression.Type type = elementNameToExpressionType.get( element.getQName() );
        if ( type == null ) {
            String msg = Messages.getMessage( "FILTER_PARSER_UNEXPECTED_ELEMENT", element.getQName(),
                                              elemNames( Expression.Type.class, expressionTypeToElementName ) );
            throw new XMLParsingException( this, element, msg );
        }

        switch ( type ) {
        case ADD: {
            FixedChildIterator childElementIter = new FixedChildIterator( element, 2 );
            Expression param1 = parseExpression( childElementIter.next() );
            Expression param2 = parseExpression( childElementIter.next() );
            expression = new Add( param1, param2 );
            break;
        }
        case SUB: {
            FixedChildIterator childElementIter = new FixedChildIterator( element, 2 );
            Expression param1 = parseExpression( childElementIter.next() );
            Expression param2 = parseExpression( childElementIter.next() );
            expression = new Sub( param1, param2 );
            break;
        }
        case MUL: {
            FixedChildIterator childElementIter = new FixedChildIterator( element, 2 );
            Expression param1 = parseExpression( childElementIter.next() );
            Expression param2 = parseExpression( childElementIter.next() );
            expression = new Mul( param1, param2 );
            break;
        }
        case DIV: {
            FixedChildIterator childElementIter = new FixedChildIterator( element, 2 );
            Expression param1 = parseExpression( childElementIter.next() );
            Expression param2 = parseExpression( childElementIter.next() );
            expression = new Div( param1, param2 );
            break;
        }
        case PROPERTY_NAME: {
            expression = parsePropertyName( element );
            break;
        }
        case LITERAL: {
            expression = new Literal( element.getText() );
            break;
        }
        case FUNCTION: {
            String name = getRequiredNodeAsString( element, new XPath( "@name", nsContext ) );
            List<Expression> params = new ArrayList<Expression>();
            FixedChildIterator childElementIter = new FixedChildIterator( element, 1 );
            Expression param = parseExpression( childElementIter.next() );
            params.add( param );
            expression = new Function( name, params );
            break;
        }
        }
        return expression;
    }

    /**
     * Parses the given {http://www.opengis.net/ogc}PropertyName element as a {@link PropertyName}.
     * 
     * @param element
     *            element to be parsed
     * @return propertyName object
     */
    private PropertyName parsePropertyName( OMElement element ) {
        String propName = element.getText().trim();
        if ( propName.isEmpty() ) {
            // TODO filter encoding guy: use whatever exception shall be used here. But make sure that the
            // GetObservation100XMLAdapter gets an exception from here as the compliance of the SOS hangs on it's thread
            throw new XMLParsingException( this, element, Messages.getMessage( "FILTER_PARSER_PROPERTY_NAME_EMPTY",
                                                                               element.getQName() ) );
        }
        return new PropertyName( propName, getNamespaceContext( element ) );
    }

    /**
     * Parses the given element as an {@link Operator}.
     * <p>
     * The element must be one of the following types:
     * <ul>
     * <li>{@link LogicalOperator}</li>
     * <li>{@link SpatialOperator}</li>
     * <li>{@link ComparisonOperator}</li>
     * </ul>
     * 
     * @param element
     *            element to be parsed
     * @return expression object
     */
    private Operator parseOperator( OMElement element ) {

        Operator operator = null;

        // check if element name is a valid operator element
        Operator.Type type = elementNameToOperatorType.get( element.getQName() );
        if ( type == null ) {
            String expectedList = elemNames( Operator.Type.class, logicalOperatorTypeToElementName ) + ", "
                                  + elemNames( Operator.Type.class, spatialOperatorTypeToElementName ) + ", "
                                  + elemNames( Operator.Type.class, comparisonOperatorTypeToElementName );
            String msg = Messages.getMessage( "FILTER_PARSER_UNEXPECTED_ELEMENT", element.getQName(), expectedList );
            throw new XMLParsingException( this, element, msg );
        }

        switch ( type ) {
        case COMPARISON:
            LOG.debug( "Building comparison operator" );
            operator = parseComparisonOperator( element );
            break;
        case LOGICAL:
            LOG.debug( "Building logical operator" );
            operator = parseLogicalOperator( element );
            break;
        case SPATIAL:
            LOG.debug( "Building spatial operator" );
            try {
                operator = parseSpatialOperator( element );
            } catch ( XMLStreamException e ) {
                throw new XMLParsingException( this, element, e.getMessage() );
            } catch ( UnknownCRSException e ) {
                throw new XMLParsingException( this, element, e.getMessage() );
            }
            break;
        }
        return operator;
    }

    /**
     * Parses the given element as a {@link SpatialOperator}.
     * <p>
     * The element must be one of the following:
     * <ul>
     * <li>{http://www.opengis.net/ogc}BBOX</li>
     * <li>{http://www.opengis.net/ogc}Beyond</li>
     * <li>{http://www.opengis.net/ogc}Contains</li>
     * <li>{http://www.opengis.net/ogc}Crosses</li>
     * <li>{http://www.opengis.net/ogc}Disjoint</li>
     * <li>{http://www.opengis.net/ogc}DWithin</li>
     * <li>{http://www.opengis.net/ogc}Equals</li>
     * <li>{http://www.opengis.net/ogc}Intersects</li>
     * <li>{http://www.opengis.net/ogc}Overlaps</li>
     * <li>{http://www.opengis.net/ogc}Touches</li>
     * <li>{http://www.opengis.net/ogc}Within</li>
     * </ul>
     * 
     * @param element
     *            element to be parsed
     * @return logical operator object
     * @throws UnknownCRSException
     * @throws XMLStreamException
     * @throws XMLParsingException
     */
    private SpatialOperator parseSpatialOperator( OMElement element )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        SpatialOperator spatialOperator = null;

        // check if element name is a valid spatial operator element name
        SpatialOperator.SubType type = elementNameToSpatialOperatorType.get( element.getQName() );

        if ( type == null ) {
            String msg = Messages.getMessage( "FILTER_PARSER_UNEXPECTED_ELEMENT", element.getQName(),
                                              elemNames( SpatialOperator.SubType.class,
                                                         spatialOperatorTypeToElementName ) );
            throw new XMLParsingException( this, element, msg );
        }

        switch ( type ) {
        case BBOX: {
            FixedChildIterator childElementIter = new FixedChildIterator( element, 2 );

            // first parameter: 'ogc:PropertyName'
            PropertyName param1 = parsePropertyName( childElementIter.next() );

            // second parameter: 'gml:Envelope'
            GML311GeometryDecoder geomParser = new GML311GeometryDecoder();
            OMElement geometryElement = childElementIter.next();
            XMLStreamReader reader = geometryElement.getXMLStreamReaderWithoutCaching();
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( reader, getSystemId() );
            xmlReader.nextTag();
            xmlReader.require( START_ELEMENT, GML_NS, "Envelope" );
            Envelope param2 = geomParser.parseEnvelope( xmlReader, null );

            spatialOperator = new BBOX( param1, param2 );
            break;
        }
        case BEYOND: {
            FixedChildIterator childElementIter = new FixedChildIterator( element, 3 );

            // first parameter: 'ogc:PropertyName'
            PropertyName param1 = parsePropertyName( childElementIter.next() );

            // second parameter: 'gml:_Geometry'
            GML311GeometryDecoder geomParser = new GML311GeometryDecoder();
            OMElement geometryElement = childElementIter.next();
            XMLStreamReader reader = geometryElement.getXMLStreamReaderWithoutCaching();
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( reader, getSystemId() );
            xmlReader.nextTag();
            Geometry param2 = geomParser.parseAbstractGeometry( xmlReader, null );

            // third parameter: 'ogc:Distance'
            OMElement distanceElement = childElementIter.next();
            String distanceUnits = getRequiredNodeAsString( distanceElement, new XPath( "@units", nsContext ) );
            Measure distance = new Measure( distanceElement.getText(), distanceUnits );

            spatialOperator = new Beyond( param1, param2, distance );
            break;
        }
        case INTERSECTS: {
            FixedChildIterator childElementIter = new FixedChildIterator( element, 2 );

            // first parameter: 'ogc:PropertyName'
            PropertyName param1 = parsePropertyName( childElementIter.next() );

            // second parameter: 'gml:_Geometry'
            GML311GeometryDecoder geomParser = new GML311GeometryDecoder();
            OMElement geometryElement = childElementIter.next();
            XMLStreamReader reader = geometryElement.getXMLStreamReaderWithoutCaching();
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( reader, getSystemId() );
            xmlReader.nextTag();
            Geometry param2 = geomParser.parseAbstractGeometry( xmlReader, null );

            spatialOperator = new Intersects( param1, param2 );
            break;
        }
        case CONTAINS: {
            FixedChildIterator childElementIter = new FixedChildIterator( element, 2 );

            // first parameter: 'ogc:PropertyName'
            PropertyName param1 = parsePropertyName( childElementIter.next() );

            // second parameter: 'gml:_Geometry'
            GML311GeometryDecoder geomParser = new GML311GeometryDecoder();
            OMElement geometryElement = childElementIter.next();
            XMLStreamReader reader = geometryElement.getXMLStreamReaderWithoutCaching();
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( reader, getSystemId() );
            xmlReader.nextTag();
            Geometry param2 = geomParser.parseAbstractGeometry( xmlReader, null );

            spatialOperator = new Contains( param1, param2 );
            break;
        }
        case CROSSES: {
            FixedChildIterator childElementIter = new FixedChildIterator( element, 2 );

            // first parameter: 'ogc:PropertyName'
            PropertyName param1 = parsePropertyName( childElementIter.next() );

            // second parameter: 'gml:_Geometry'
            GML311GeometryDecoder geomParser = new GML311GeometryDecoder();
            OMElement geometryElement = childElementIter.next();
            XMLStreamReader reader = geometryElement.getXMLStreamReaderWithoutCaching();
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( reader, getSystemId() );
            xmlReader.nextTag();
            Geometry param2 = geomParser.parseAbstractGeometry( xmlReader, null );

            spatialOperator = new Crosses( param1, param2 );
            break;
        }
        case DISJOINT: {
            FixedChildIterator childElementIter = new FixedChildIterator( element, 2 );

            // first parameter: 'ogc:PropertyName'
            PropertyName param1 = parsePropertyName( childElementIter.next() );

            // second parameter: 'gml:_Geometry'
            GML311GeometryDecoder geomParser = new GML311GeometryDecoder();
            OMElement geometryElement = childElementIter.next();
            XMLStreamReader reader = geometryElement.getXMLStreamReaderWithoutCaching();
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( reader, getSystemId() );
            xmlReader.nextTag();
            Geometry param2 = geomParser.parseAbstractGeometry( xmlReader, null );

            spatialOperator = new Disjoint( param1, param2 );
            break;
        }
        case DWITHIN: {
            FixedChildIterator childElementIter = new FixedChildIterator( element, 3 );

            // first parameter: 'ogc:PropertyName'
            PropertyName param1 = parsePropertyName( childElementIter.next() );

            // second parameter: 'gml:_Geometry'
            GML311GeometryDecoder geomParser = new GML311GeometryDecoder();
            OMElement geometryElement = childElementIter.next();
            XMLStreamReader reader = geometryElement.getXMLStreamReaderWithoutCaching();
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( reader, getSystemId() );
            xmlReader.nextTag();
            Geometry param2 = geomParser.parseAbstractGeometry( xmlReader, null );

            // third parameter: 'ogc:Distance'
            OMElement distanceElement = childElementIter.next();
            String distanceUnits = getRequiredNodeAsString( distanceElement, new XPath( "@units", nsContext ) );
            Measure distance = new Measure( distanceElement.getText(), distanceUnits );

            spatialOperator = new DWithin( param1, param2, distance );
            break;
        }
        case EQUALS: {
            FixedChildIterator childElementIter = new FixedChildIterator( element, 2 );

            // first parameter: 'ogc:PropertyName'
            PropertyName param1 = parsePropertyName( childElementIter.next() );

            // second parameter: 'gml:_Geometry'
            GML311GeometryDecoder geomParser = new GML311GeometryDecoder();
            OMElement geometryElement = childElementIter.next();
            XMLStreamReader reader = geometryElement.getXMLStreamReaderWithoutCaching();
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( reader, getSystemId() );
            xmlReader.nextTag();
            Geometry param2 = geomParser.parseAbstractGeometry( xmlReader, null );

            spatialOperator = new Equals( param1, param2 );
            break;
        }
        case OVERLAPS: {
            FixedChildIterator childElementIter = new FixedChildIterator( element, 2 );

            // first parameter: 'ogc:PropertyName'
            PropertyName param1 = parsePropertyName( childElementIter.next() );

            // second parameter: 'gml:_Geometry'
            GML311GeometryDecoder geomParser = new GML311GeometryDecoder();
            OMElement geometryElement = childElementIter.next();
            XMLStreamReader reader = geometryElement.getXMLStreamReaderWithoutCaching();
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( reader, getSystemId() );
            xmlReader.nextTag();
            Geometry param2 = geomParser.parseAbstractGeometry( xmlReader, null );

            spatialOperator = new Overlaps( param1, param2 );
            break;
        }
        case TOUCHES: {
            FixedChildIterator childElementIter = new FixedChildIterator( element, 2 );

            // first parameter: 'ogc:PropertyName'
            PropertyName param1 = parsePropertyName( childElementIter.next() );

            // second parameter: 'gml:_Geometry'
            GML311GeometryDecoder geomParser = new GML311GeometryDecoder();
            OMElement geometryElement = childElementIter.next();
            XMLStreamReader reader = geometryElement.getXMLStreamReaderWithoutCaching();
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( reader, getSystemId() );
            xmlReader.nextTag();
            Geometry param2 = geomParser.parseAbstractGeometry( xmlReader, null );

            spatialOperator = new Touches( param1, param2 );
            break;
        }
        case WITHIN: {
            FixedChildIterator childElementIter = new FixedChildIterator( element, 2 );

            // first parameter: 'ogc:PropertyName'
            PropertyName param1 = parsePropertyName( childElementIter.next() );

            // second parameter: 'gml:_Geometry'
            GML311GeometryDecoder geomParser = new GML311GeometryDecoder();
            OMElement geometryElement = childElementIter.next();
            XMLStreamReader reader = geometryElement.getXMLStreamReaderWithoutCaching();
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( reader, getSystemId() );
            xmlReader.nextTag();
            Geometry param2 = geomParser.parseAbstractGeometry( xmlReader, null );

            spatialOperator = new Within( param1, param2 );
        }
        }
        return spatialOperator;
    }

    /**
     * Parses the given element as a {@link ComparisonOperator}.
     * <p>
     * The element must be one of the following:
     * <ul>
     * <li>{http://www.opengis.net/ogc}PropertyIsEqualTo</li>
     * <li>{http://www.opengis.net/ogc}PropertyIsGreaterThan</li>
     * <li>{http://www.opengis.net/ogc}PropertyIsGreaterThanOrEqualTo</li>
     * <li>{http://www.opengis.net/ogc}PropertyIsLessThan</li>
     * <li>{http://www.opengis.net/ogc}PropertyIsLessThanOrEqualTo</li>
     * <li>{http://www.opengis.net/ogc}PropertyIsNotEqualTo</li>
     * <li>{http://www.opengis.net/ogc}PropertyIsBetween</li>
     * <li>{http://www.opengis.net/ogc}PropertyIsLike</li>
     * <li>{http://www.opengis.net/ogc}PropertyIsNull</li>
     * </ul>
     * 
     * @param element
     *            element to be parsed
     * @return logical operator object
     */
    public ComparisonOperator parseComparisonOperator( OMElement element ) {

        ComparisonOperator comparisonOperator = null;

        // check if element name is a valid comparison operator element
        ComparisonOperator.SubType type = elementNameToComparisonOperatorType.get( element.getQName() );

        if ( type == null ) {
            String msg = Messages.getMessage( "FILTER_PARSER_UNEXPECTED_ELEMENT", element.getQName(),
                                              elemNames( ComparisonOperator.SubType.class,
                                                         comparisonOperatorTypeToElementName ) );
            throw new XMLParsingException( this, element, msg );
        }

        switch ( type ) {
        case PROPERTY_IS_EQUAL_TO:
        case PROPERTY_IS_GREATER_THAN:
        case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
        case PROPERTY_IS_LESS_THAN:
        case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
        case PROPERTY_IS_NOT_EQUAL_TO:
            comparisonOperator = parseBinaryComparisonOperator( element, type );
            break;
        case PROPERTY_IS_BETWEEN:
            comparisonOperator = parsePropertyIsBetweenOperator( element );
            break;
        case PROPERTY_IS_LIKE:
            comparisonOperator = parsePropertyIsLikeOperator( element );
            break;
        case PROPERTY_IS_NULL:
            comparisonOperator = parsePropertyIsNullOperator( element );
            break;
        }
        return comparisonOperator;
    }

    private Expression parseBoundaryExpression( QName boundary, OMElement element ) {
        if ( !element.getQName().equals( boundary ) ) {
            throw new XMLParsingException( this, element, "Error while parsing filter. Expected " + boundary );
        }
        Expression expression;
        FixedChildIterator childElementIter = new FixedChildIterator( element, 1 );
        OMElement parameterElement = childElementIter.next();
        expression = parseExpression( parameterElement );

        return expression;
    }

    private ComparisonOperator parsePropertyIsNullOperator( OMElement element ) {
        FixedChildIterator childElementIter = new FixedChildIterator( element, 1 );
        OMElement parameterElement = childElementIter.next();
        Expression.Type expType = elementNameToExpressionType.get( parameterElement.getQName() );
        if ( expType == null && expType != Expression.Type.PROPERTY_NAME ) {
            String msg = "Error while parsing ogc:PropertyIsNull. Expected "
                         + expressionTypeToElementName.get( expType );
            throw new XMLParsingException( this, element, msg );
        }
        // TODO build nsContext
        return new PropertyIsNull( new PropertyName( parameterElement.getText(), null ) );
    }

    private ComparisonOperator parsePropertyIsBetweenOperator( OMElement element ) {
        FixedChildIterator childElementIter = new FixedChildIterator( element, 3 );
        OMElement parameterElement = childElementIter.next();
        Expression expression = parseExpression( parameterElement );
        Expression lowerBoundary = parseBoundaryExpression( new QName( OGC_NS, "lowerBoundary" ),
                                                            childElementIter.next() );
        Expression upperBoundary = parseBoundaryExpression( new QName( OGC_NS, "upperBoundary" ),
                                                            childElementIter.next() );
        return new PropertyIsBetween( expression, lowerBoundary, upperBoundary );
    }

    private ComparisonOperator parsePropertyIsLikeOperator( OMElement element ) {
        FixedChildIterator childElementIter = new FixedChildIterator( element, 2 );
        PropertyName propName = parseTypedExpression( PropertyName.class, childElementIter.next() );
        Literal literal = parseTypedExpression( Literal.class, childElementIter.next() );
        String wildCard = getRequiredNodeAsString( element, new XPath( "@wildCard", nsContext ) );
        String singleChar = getRequiredNodeAsString( element, new XPath( "@singleChar", nsContext ) );
        String escapeChar = getRequiredNodeAsString( element, new XPath( "@escapeChar", nsContext ) );
        return new PropertyIsLike( propName, literal, wildCard, singleChar, escapeChar );
    }

    @SuppressWarnings("unchecked")
    private <T extends Expression> T parseTypedExpression( Class<T> type, OMElement element ) {
        Expression expression = parseExpression( element );
        if ( !expression.getClass().equals( type ) ) {
            throw new XMLParsingException( this, element, "Expected element ogc:" + type.getSimpleName() );
        }
        return (T) expression;
    }

    /**
     * Parses the given element as a {@link BinaryComparisonOperator}.
     * <p>
     * The element must be one of the following:
     * <ul>
     * <li>{http://www.opengis.net/ogc}PropertyIsEqualTo</li>
     * <li>{http://www.opengis.net/ogc}PropertyIsGreaterThan</li>
     * <li>{http://www.opengis.net/ogc}PropertyIsGreaterThanOrEqualTo</li>
     * <li>{http://www.opengis.net/ogc}PropertyIsLessThan</li>
     * <li>{http://www.opengis.net/ogc}PropertyIsLessThanOrEqualTo</li>
     * <li>{http://www.opengis.net/ogc}PropertyIsNotEqualTo</li>
     * </ul>
     * 
     * @param element
     *            element to be parsed
     * @return binary comparison operator object
     */
    private BinaryComparisonOperator parseBinaryComparisonOperator( OMElement element, ComparisonOperator.SubType type ) {

        BinaryComparisonOperator comparisonOperator = null;
        FixedChildIterator childElementIter = new FixedChildIterator( element, 2 );

        Expression parameter1 = parseExpression( childElementIter.next() );
        Expression parameter2 = parseExpression( childElementIter.next() );

        boolean matchCase = getNodeAsBoolean( element, new XPath( "@matchCase", nsContext ), false );

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

    /**
     * Parses the given element as a {@link LogicalOperator}.
     * <p>
     * The element must be one of the following:
     * <ul>
     * <li>{http://www.opengis.net/ogc}And</li>
     * <li>{http://www.opengis.net/ogc}Or</li>
     * <li>{http://www.opengis.net/ogc}Not</li>
     * </ul>
     * 
     * @param element
     *            element to be parsed
     * @return logical operator object
     */
    @SuppressWarnings("unchecked")
    private Operator parseLogicalOperator( OMElement element ) {

        Operator logicalOperator = null;

        // check if element name is a valid logical operator element
        LogicalOperator.SubType type = elementNameToLogicalOperatorType.get( element.getQName() );
        if ( type == null ) {
            String msg = Messages.getMessage( "FILTER_PARSER_UNEXPECTED_ELEMENT", element.getQName(),
                                              elemNames( LogicalOperator.SubType.class,
                                                         logicalOperatorTypeToElementName ) );
            throw new XMLParsingException( this, element, msg );
        }
        switch ( type ) {
        case AND: {

            List<Operator> listOperators = new ArrayList<Operator>();
            Iterator<OMElement> iterator = element.getChildElements();
            while ( iterator.hasNext() ) {
                listOperators.add( parseOperator( iterator.next() ) );
            }
            Operator operators[] = new Operator[listOperators.size()];
            listOperators.toArray( operators );

            try {
                logicalOperator = new And( operators );
            } catch ( Exception e ) {
                String msg = "Error while parsing the And operator. It must have at least two arguments.";
                throw new XMLParsingException( this, element, msg );
            }
            break;
        }
        case OR: {
            List<Operator> listOperators = new ArrayList<Operator>();
            Iterator<OMElement> iterator = element.getChildElements();
            while ( iterator.hasNext() ) {
                listOperators.add( parseOperator( iterator.next() ) );
            }
            Operator operators[] = new Operator[listOperators.size()];
            listOperators.toArray( operators );

            try {
                logicalOperator = new Or( operators );
            } catch ( Exception e ) {
                String msg = "Error while parsing the Or operator. It must have at least two arguments.";
                throw new XMLParsingException( this, element, msg );
            }

            break;
        }
        case NOT: {
            FixedChildIterator childElementIter = new FixedChildIterator( element, 1 );
            Operator parameter = parseOperator( childElementIter.next() );
            logicalOperator = new Not( parameter );
            break;
        }
        }
        return logicalOperator;
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
            names.add( qname.toString() );
        }
        return ArrayUtils.join( ", ", names );
    }
}
