//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.ArrayUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.commons.xml.XPath;
import org.deegree.model.filter.comparison.BinaryComparisonOperator;
import org.deegree.model.filter.comparison.ComparisonOperator;
import org.deegree.model.filter.comparison.PropertyIsBetween;
import org.deegree.model.filter.comparison.PropertyIsEqualTo;
import org.deegree.model.filter.comparison.PropertyIsGreaterThan;
import org.deegree.model.filter.comparison.PropertyIsGreaterThanOrEqualTo;
import org.deegree.model.filter.comparison.PropertyIsLessThan;
import org.deegree.model.filter.comparison.PropertyIsLessThanOrEqualTo;
import org.deegree.model.filter.comparison.PropertyIsLike;
import org.deegree.model.filter.comparison.PropertyIsNotEqualTo;
import org.deegree.model.filter.comparison.PropertyIsNull;
import org.deegree.model.filter.expression.Add;
import org.deegree.model.filter.expression.Div;
import org.deegree.model.filter.expression.Function;
import org.deegree.model.filter.expression.Literal;
import org.deegree.model.filter.expression.Mul;
import org.deegree.model.filter.expression.PropertyName;
import org.deegree.model.filter.expression.Sub;
import org.deegree.model.filter.logical.And;
import org.deegree.model.filter.logical.LogicalOperator;
import org.deegree.model.filter.logical.Not;
import org.deegree.model.filter.logical.Or;
import org.deegree.model.filter.spatial.SpatialOperator;
import org.deegree.model.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter between XML documents that comply to the Filter Encoding Specification 1.1.0 and {@link Filter} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class Filter110XMLAdapter extends XMLAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( Filter110XMLAdapter.class );

    private static final String OGC_NS = "http://www.opengis.net/ogc";

    private static final QName NAME_ATTR = new QName( "name" );

    private static final QName FEATURE_ID_ELEMENT = new QName( OGC_NS, "FeatureId" );

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
     * @throws XMLProcessingException
     */
    public Filter parse()
                            throws XMLProcessingException {

        Filter filter = null;

        Iterator<?> childIterator = rootElement.getChildElements();
        if ( !childIterator.hasNext() ) {
            String msg = "ogc:Filter elements must have at least one child.";
            throw new XMLProcessingException( msg );
        }

        OMElement element = (OMElement) rootElement.getChildElements().next();
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
            if ( GML_OBJECT_ID_ELEMENT.equals( childElementName ) || FEATURE_ID_ELEMENT.equals( childElementName ) ) {
                String id = childElement.getText();
                matchedIds.add( id );
            } else {
                String msg = Messages.getMessage( "FILTER_PARSING_ID_FILTER", childElementName, GML_OBJECT_ID_ELEMENT,
                                                  FEATURE_ID_ELEMENT );
                throw new XMLParsingException (this, childElement, msg);
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
    private Expression parseExpression( OMElement element ) {

        Expression expression = null;

        // check if element name is a valid expression element
        Expression.Type type = elementNameToExpressionType.get( element.getQName() );
        if ( type == null ) {
            String msg = "Error while parsing ogc:expression. Expected one of "
                         + elemNames( Expression.Type.class, expressionTypeToElementName ) + ". ";
            throw new XMLParsingException( this, element, msg );
        }

        switch ( type ) {
        case ADD: {
            try {
                Iterator<?> childElementIter = element.getChildElements();
                Expression param1 = parseExpression( (OMElement) childElementIter.next() );
                Expression param2 = parseExpression( (OMElement) childElementIter.next() );
                expression = new Add( param1, param2 );
                if ( childElementIter.hasNext() ) {
                    String msg = Messages.getMessage( "FILTER_PARSING_WRONG_CHILD_COUNT", element.getQName(), 2 );
                    throw new XMLProcessingException( msg );
                }
            } catch ( NoSuchElementException e ) {
                String msg = Messages.getMessage( "FILTER_PARSING_WRONG_CHILD_COUNT", element.getQName(), 2 );
                throw new XMLProcessingException( msg );
            }
            break;
        }
        case SUB: {
            try {
                Iterator<?> childElementIter = element.getChildElements();
                Expression param1 = parseExpression( (OMElement) childElementIter.next() );
                Expression param2 = parseExpression( (OMElement) childElementIter.next() );
                expression = new Sub( param1, param2 );
                if ( childElementIter.hasNext() ) {
                    String msg = Messages.getMessage( "FILTER_PARSING_WRONG_CHILD_COUNT", element.getQName(), 2 );
                    throw new XMLProcessingException( msg );
                }
            } catch ( NoSuchElementException e ) {
                String msg = Messages.getMessage( "FILTER_PARSING_WRONG_CHILD_COUNT", element.getQName(), 2 );
                throw new XMLProcessingException( msg );
            }
        }
        case MUL: {
            try {
                Iterator<?> childElementIter = element.getChildElements();
                Expression param1 = parseExpression( (OMElement) childElementIter.next() );
                Expression param2 = parseExpression( (OMElement) childElementIter.next() );
                expression = new Mul( param1, param2 );
                if ( childElementIter.hasNext() ) {
                    String msg = Messages.getMessage( "FILTER_PARSING_WRONG_CHILD_COUNT", element.getQName(), 2 );
                    throw new XMLProcessingException( msg );
                }
            } catch ( NoSuchElementException e ) {
                String msg = Messages.getMessage( "FILTER_PARSING_WRONG_CHILD_COUNT", element.getQName(), 2 );
                throw new XMLProcessingException( msg );
            }
        }
        case DIV: {
            try {
                Iterator<?> childElementIter = element.getChildElements();
                Expression param1 = parseExpression( (OMElement) childElementIter.next() );
                Expression param2 = parseExpression( (OMElement) childElementIter.next() );
                expression = new Div( param1, param2 );
                if ( childElementIter.hasNext() ) {
                    String msg = Messages.getMessage( "FILTER_PARSING_WRONG_CHILD_COUNT", element.getQName(), 2 );
                    throw new XMLProcessingException( msg );
                }
            } catch ( NoSuchElementException e ) {
                String msg = Messages.getMessage( "FILTER_PARSING_WRONG_CHILD_COUNT", element.getQName(), 2 );
                throw new XMLProcessingException( msg );
            }
        }
        case PROPERTY_NAME: {
            expression = new PropertyName( element.getText() );
            break;
        }
        case LITERAL: {
            expression = new Literal( element.getText() );
            break;
        }
        case FUNCTION: {
            String name = element.getAttributeValue( new QName( "name" ) );
            if ( name == null ) {
                String msg = Messages.getMessage( "FILTER_PARSING_FUNCTION_NAME_ATTR_MISSING" );
                throw new XMLProcessingException( msg );
            }
            List<Expression> params = new ArrayList<Expression>();
            Iterator<?> childElementIter = element.getChildElements();
            Expression param = parseExpression( (OMElement) childElementIter.next() );
            params.add( param );
            expression = new Function( name, params );
            break;
        }
        }
        return expression;
    }

    /**
     * Parses the given element as an {@link Operator}.
     * <p>
     * The element must be one of the following types:
     * <ul>
     * <li> {@link LogicalOperator}</li>
     * <li> {@link SpatialOperator}</li>
     * <li> {@link ComparisonOperator}</li>
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
            String msg = "Error while parsing ogc:Filter. Expected one of "
                         + elemNames( Operator.Type.class, logicalOperatorTypeToElementName ) + ", "
                         + elemNames( Operator.Type.class, spatialOperatorTypeToElementName ) + ", "
                         + elemNames( Operator.Type.class, comparisonOperatorTypeToElementName );
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
            operator = parseSpatialOperator( element );
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
     */
    private SpatialOperator parseSpatialOperator( OMElement element ) {
        // TODO Auto-generated method stub
        return null;
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
            String msg = "Error while parsing ogc:comparsionOps. Expected one of "
                         + elemNames( ComparisonOperator.SubType.class, comparisonOperatorTypeToElementName ) + ".";
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
            // TODO implement me
            break;
        case PROPERTY_IS_LIKE:
            // TODO implement me
            break;
        case PROPERTY_IS_NULL:
            // TODO implement me
            break;
        }
        return comparisonOperator;
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
        Iterator<?> childElementIter = element.getChildElements();
        Expression parameter1 = null;
        Expression parameter2 = null;
        boolean matchCase = true;
        try {
            OMElement parameterElement = (OMElement) childElementIter.next();
            parameter1 = parseExpression( parameterElement );
            parameterElement = (OMElement) childElementIter.next();
            parameter2 = parseExpression( parameterElement );
            if ( childElementIter.hasNext() ) {
                throw new NoSuchElementException();
            }
            matchCase = getNodeAsBoolean( parameterElement, new XPath( "@matchCase", nsContext ), false );
        } catch ( NoSuchElementException e ) {
            String msg = Messages.getMessage( "FILTER_PARSING_WRONG_CHILD_COUNT", element.getQName(), 2 );
            throw new XMLProcessingException( msg );
        }

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
    private Operator parseLogicalOperator( OMElement element ) {

        Operator logicalOperator = null;

        // check if element name is a valid logical operator element
        LogicalOperator.SubType type = elementNameToLogicalOperatorType.get( element.getQName() );
        if ( type == null ) {
            String msg = "Error while parsing ogc:logicOps. Expected one of "
                         + elemNames( LogicalOperator.SubType.class, logicalOperatorTypeToElementName ) + ".";
            throw new XMLParsingException( this, element, msg );
        }
        try {
            switch ( type ) {
            case AND: {
                Iterator<?> childElementIter = element.getChildElements();
                Operator parameter1 = parseOperator( (OMElement) childElementIter.next() );
                Operator parameter2 = parseOperator( (OMElement) childElementIter.next() );
                if ( childElementIter.hasNext() ) {
                    throw new NoSuchElementException();
                }
                logicalOperator = new And( parameter1, parameter2 );
                break;
            }
            case OR: {
                Iterator<?> childElementIter = element.getChildElements();
                Operator parameter1 = parseOperator( (OMElement) childElementIter.next() );
                Operator parameter2 = parseOperator( (OMElement) childElementIter.next() );
                if ( childElementIter.hasNext() ) {
                    throw new NoSuchElementException();
                }
                logicalOperator = new Or( parameter1, parameter2 );
            }
            case NOT: {
                Iterator<?> childElementIter = element.getChildElements();
                Operator parameter = parseOperator( (OMElement) childElementIter.next() );
                if ( childElementIter.hasNext() ) {
                    throw new NoSuchElementException();
                }
                logicalOperator = new Not( parameter );
                break;
            }
            }
        } catch ( NoSuchElementException e ) {
            String msg = Messages.getMessage( "FILTER_PARSING_WRONG_CHILD_COUNT", element.getQName(), 2 );
            throw new XMLProcessingException( msg );
        }

        return logicalOperator;
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
            Collection<String> ids = ( (IdFilter) filter ).getIds();
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
            export( ( (And) operator ).getParameter1(), writer );
            export( ( (And) operator ).getParameter2(), writer );
            break;
        case OR:
            export( ( (Or) operator ).getParameter1(), writer );
            export( ( (Or) operator ).getParameter2(), writer );
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
    private static void export( Expression expression, XMLStreamWriter writer )
                            throws XMLStreamException {

        QName elementName = expressionTypeToElementName.get( expression.getType() );
        writer.writeStartElement( elementName.getNamespaceURI(), elementName.getLocalPart() );

        switch ( expression.getType() ) {
        case PROPERTY_NAME:
            writer.writeCharacters( ( (PropertyName) expression ).getPropertyName() );
            break;
        case LITERAL:
            writer.writeCharacters( ( (Literal) expression ).getValue() );
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
            names.add( qname.getLocalPart() );
        }
        return ArrayUtils.join( ", ", names );
    }
}
