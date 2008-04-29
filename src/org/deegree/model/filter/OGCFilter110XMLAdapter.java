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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.model.filter.comparison.BinaryComparisonOperator;
import org.deegree.model.filter.comparison.PropertyIsBetweenOperator;
import org.deegree.model.filter.comparison.PropertyIsEqualToOperator;
import org.deegree.model.filter.comparison.PropertyIsGreaterThanOperator;
import org.deegree.model.filter.comparison.PropertyIsGreaterThanOrEqualToOperator;
import org.deegree.model.filter.comparison.PropertyIsLessThanOperator;
import org.deegree.model.filter.comparison.PropertyIsLessThanOrEqualToOperator;
import org.deegree.model.filter.comparison.PropertyIsLikeOperator;
import org.deegree.model.filter.comparison.PropertyIsNotEqualToOperator;
import org.deegree.model.filter.comparison.PropertyIsNullOperator;
import org.deegree.model.filter.expression.AddExpression;
import org.deegree.model.filter.expression.DivExpression;
import org.deegree.model.filter.expression.Expression;
import org.deegree.model.filter.expression.Function;
import org.deegree.model.filter.expression.Literal;
import org.deegree.model.filter.expression.MulExpression;
import org.deegree.model.filter.expression.PropertyName;
import org.deegree.model.filter.expression.SubExpression;
import org.deegree.model.filter.logical.AndOperator;
import org.deegree.model.filter.logical.NotOperator;
import org.deegree.model.filter.logical.OrOperator;
import org.deegree.model.i18n.Messages;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class OGCFilter110XMLAdapter extends XMLAdapter {

    private static Log LOG = LogFactory.getLog( OGCFilter110XMLAdapter.class );

    private static String OGC_NS = "http://www.opengis.net/ogc";

    // qualified element name of filter

    public static QName FILTER = new QName( OGC_NS, "Filter" );

    // qualified element names of comparison operators

    public static QName PROPERTY_IS_EQUAL_TO = new QName( OGC_NS, "PropertyIsEqualTo" );

    public static QName PROPERTY_IS_NOT_EQUAL_TO = new QName( OGC_NS, "PropertyIsNotEqualTo" );

    public static QName PROPERTY_IS_LESS_THAN = new QName( OGC_NS, "PropertyIsLessThan" );

    public static QName PROPERTY_IS_GREATER_THAN = new QName( OGC_NS, "PropertyIsGreaterThan" );

    public static QName PROPERTY_IS_LESS_THAN_OR_EQUAL_TO = new QName( OGC_NS, "PropertyIsLessThanOrEqualTo" );

    public static QName PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO = new QName( OGC_NS, "PropertyIsGreaterThanOrEqualTo" );

    public static QName PROPERTY_IS_LIKE = new QName( OGC_NS, "PropertyIsLike" );

    public static QName PROPERTY_IS_NULL = new QName( OGC_NS, "PropertyIsNull" );

    public static QName PROPERTY_IS_BETWEEN = new QName( OGC_NS, "PropertyIsBetween" );
    
    public static QName UPPER_BOUNDARY = new QName( OGC_NS, "UpperBoundary" );
    public static QName LOWER_BOUNDARY = new QName( OGC_NS, "UpperBoundary" );

    // attribute names

    public static QName MATCH_CASE = new QName( "matchCase" );

    // qualified element names of spatial operators

    public static QName EQUALS = new QName( OGC_NS, "Equals" );

    public static QName DISJOINT = new QName( OGC_NS, "Disjoint" );

    public static QName TOUCHES = new QName( OGC_NS, "Touches" );

    public static QName WITHIN = new QName( OGC_NS, "Within" );

    public static QName OVERLAPS = new QName( OGC_NS, "Overlaps" );

    public static QName CROSSES = new QName( OGC_NS, "Crosses" );

    public static QName INTERSECTS = new QName( OGC_NS, "Intersects" );

    public static QName CONTAINS = new QName( OGC_NS, "Contains" );

    public static QName DWITHIN = new QName( OGC_NS, "DWithin" );

    public static QName BEYOND = new QName( OGC_NS, "Beyond" );

    public static QName BBOX = new QName( OGC_NS, "BBOX" );

    // qualified element names of logical operators

    public static QName AND = new QName( OGC_NS, "And" );

    public static QName OR = new QName( OGC_NS, "Or" );

    public static QName NOT = new QName( OGC_NS, "Not" );

    // qualified element names of id specifiers

    public static QName FEATURE_ID = new QName( OGC_NS, "FeatureId" );

    public static QName GML_OBJECT_ID = new QName( OGC_NS, "GmlObjectId" );

    // qualified element names of expressions

    public static QName ADD = new QName( OGC_NS, "Add" );

    public static QName SUB = new QName( OGC_NS, "Sub" );

    public static QName MUL = new QName( OGC_NS, "Mul" );

    public static QName DIV = new QName( OGC_NS, "Div" );

    public static QName PROPERTY_NAME = new QName( OGC_NS, "PropertyName" );

    public static QName FUNCTION = new QName( OGC_NS, "Function" );

    public static QName LITERAL = new QName( OGC_NS, "Literal" );

    // attribute names

    public static QName NAME = new QName( "name" );

    // arrays for all element names of a certain type

    private static QName[] COMPARISON_OPS;

    private static QName[] SPATIAL_OPS;

    private static QName[] LOGICAL_OPS;

    private static QName[] ID_SPECIFIERS;

    static {
        COMPARISON_OPS = new QName[] { PROPERTY_IS_EQUAL_TO, PROPERTY_IS_NOT_EQUAL_TO, PROPERTY_IS_LESS_THAN,
                                      PROPERTY_IS_GREATER_THAN, PROPERTY_IS_LESS_THAN_OR_EQUAL_TO,
                                      PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO, PROPERTY_IS_LIKE, PROPERTY_IS_BETWEEN };
        SPATIAL_OPS = new QName[] { EQUALS, DISJOINT, TOUCHES, WITHIN, OVERLAPS, CROSSES, INTERSECTS, CONTAINS,
                                   DWITHIN, BEYOND, BBOX };
        LOGICAL_OPS = new QName[] { AND, OR, NOT };
        ID_SPECIFIERS = new QName[] { FEATURE_ID, GML_OBJECT_ID };

    }

    /**
     * Parses the encapsulated element as a {@link Filter} expression.
     * 
     * @return corresponding <code>Filter</code> object
     * @throws XMLParsingException
     */
    public Filter parse()
                            throws XMLParsingException {

        Filter filter = null;

        Iterator childIterator = rootElement.getChildElements();
        if ( !childIterator.hasNext() ) {
            String msg = "ogc:Filter elements must have at least one child.";
            throw new XMLParsingException( msg );
        }

        OMElement element = (OMElement) rootElement.getChildElements().next();
        QName elementName = element.getQName();
        if ( isIdSpecifier( elementName ) ) {
            LOG.debug( "Building id filter" );
        } else {
            LOG.debug( "Building complex (operator based) filter" );
            BooleanOperator rootOperator = parseOperator( element );
            filter = new ComplexFilter( rootOperator );
        }
        return filter;
    }

    /**
     * Parses the given element as an {@link Expression}.
     * <p>
     * The given element must be one of the following:
     * <ul>
     * {http://www.opengis.net/ogc}:Add
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:Sub
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:Div
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:Mul
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:PropertyName
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:Function
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:Literal
     * </ul>
     * <li>
     * 
     * @param element
     *            element to be parsed
     * @return expression object
     */
    private Expression parseExpression( OMElement element ) {
        Expression expression = null;
        QName elementName = element.getQName();
        if ( ADD.equals( elementName ) || SUB.equals( elementName ) || MUL.equals( elementName )
             || DIV.equals( elementName ) ) {
            Expression param1 = null;
            Expression param2 = null;
            try {
                Iterator childElementIter = element.getChildElements();
                OMElement paramElement = (OMElement) childElementIter.next();
                param1 = parseExpression( paramElement );
                paramElement = (OMElement) childElementIter.next();
                param2 = parseExpression( paramElement );
                if ( childElementIter.hasNext() ) {
                    throw new NoSuchElementException();
                }
            } catch ( NoSuchElementException e ) {
                String msg = Messages.getMessage( "FILTER_PARSING_WRONG_CHILD_COUNT", element.getQName(), 2 );
                throw new XMLParsingException( msg );
            }
            if ( ADD.equals( elementName ) ) {
                expression = new AddExpression( param1, param2 );
            } else if ( SUB.equals( elementName ) ) {
                expression = new SubExpression( param1, param2 );
            } else if ( MUL.equals( elementName ) ) {
                expression = new MulExpression( param1, param2 );
            } else {
                expression = new DivExpression( param1, param2 );
            }
        } else if ( PROPERTY_NAME.equals( elementName ) ) {
            expression = new PropertyName( element.getText() );
        } else if ( FUNCTION.equals( elementName ) ) {
            String name = element.getAttributeValue( NAME );
            if ( name == null ) {
                String msg = Messages.getMessage( "FILTER_PARSING_FUNCTION_NAME_ATTR_MISSING" );
                throw new XMLParsingException( msg );
            }
            List<Expression> params = new ArrayList<Expression>();
            Iterator childElementIter = element.getChildElements();
            OMElement paramElement = (OMElement) childElementIter.next();
            Expression param = parseExpression( paramElement );
            params.add( param );
            expression = new Function( name, params );
        } else if ( LITERAL.equals( elementName ) ) {
            expression = new Literal( element.getText() );
        }
        return expression;
    }

    private BooleanOperator parseOperator( OMElement element ) {
        BooleanOperator operator = null;
        QName elementName = element.getQName();
        if ( isSpatialOperator( elementName ) ) {
            LOG.debug( "Building spatial operator" );
            operator = parseSpatialOperator( element );
        } else if ( isComparisonOperator( elementName ) ) {
            LOG.debug( "Building comparison operator" );
            operator = parseComparisonOperator( element );
        } else if ( isLogicalOperator( elementName ) ) {
            LOG.debug( "Building logical operator" );
            operator = parseLogicalOperator( element );
        } else {
            String msg = "Unknown operator '" + elementName + "' in filter expression.";
            throw new XMLParsingException( msg );
        }
        return operator;
    }

    private BooleanOperator parseSpatialOperator( OMElement element ) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Parses the given element as a {@link BooleanOperator}.
     * <p>
     * The given element must be one of the following:
     * <ul>
     * {http://www.opengis.net/ogc}:PropertyIsEqualTo
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:PropertyIsNotEqualTo
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:PropertyIsLessThan
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:PropertyIsGreaterThan
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:PropertyIsLessThanOrEqualTo
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:PropertyIsGreaterThanOrEqualTo
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:PropertyIsLike
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:PropertyIsNull
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:PropertyIsBetween
     * </ul>
     * <li>
     * 
     * @param element
     *            element to be parsed
     * @return corresponding boolean operator object
     */
    private BooleanOperator parseComparisonOperator( OMElement element ) {
        BooleanOperator comparisonOperator = null;
        QName elementName = element.getQName();

        if ( elementName.equals( PROPERTY_IS_EQUAL_TO ) || elementName.equals( PROPERTY_IS_NOT_EQUAL_TO )
             || elementName.equals( PROPERTY_IS_LESS_THAN ) || elementName.equals( PROPERTY_IS_GREATER_THAN )
             || elementName.equals( PROPERTY_IS_LESS_THAN_OR_EQUAL_TO )
             || elementName.equals( PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO ) ) {
            comparisonOperator = parseBinaryComparisonOperator( element );
        } else if ( elementName.equals( PROPERTY_IS_LIKE ) ) {
        } else if ( elementName.equals( PROPERTY_IS_NULL ) ) {
        } else if ( elementName.equals( PROPERTY_IS_BETWEEN ) ) {
        } else {
            assert false;
        }
        return comparisonOperator;
    }

    /**
     * Parses the given element as a {@link BinaryComparisonOperator}.
     * <p>
     * The given element must be one of the following:
     * <ul>
     * {http://www.opengis.net/ogc}:PropertyIsEqualTo
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:PropertyIsNotEqualTo
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:PropertyIsLessThan
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:PropertyIsGreaterThan
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:PropertyIsLessThanOrEqualTo
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:PropertyIsGreaterThanOrEqualTo
     * </ul>
     * <li>
     * 
     * @param element
     *            element to be parsed
     * @return corresponding binary comparison operator object
     */
    private BinaryComparisonOperator parseBinaryComparisonOperator( OMElement element ) {

        BinaryComparisonOperator comparisonOperator = null;
        Iterator childElementIter = element.getChildElements();
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
            String matchCaseAttr = parameterElement.getAttributeValue( MATCH_CASE );
            matchCase = parseBoolean( matchCaseAttr );
        } catch ( NoSuchElementException e ) {
            String msg = Messages.getMessage( "FILTER_PARSING_WRONG_CHILD_COUNT", element.getQName(), 2 );
            throw new XMLParsingException( msg );
        }

        QName elementName = element.getQName();
        if ( PROPERTY_IS_EQUAL_TO.equals( elementName ) ) {
            comparisonOperator = new PropertyIsEqualToOperator( parameter1, parameter2, matchCase );
        } else if ( PROPERTY_IS_NOT_EQUAL_TO.equals( elementName ) ) {
            comparisonOperator = new PropertyIsNotEqualToOperator( parameter1, parameter2, matchCase );
        } else if ( PROPERTY_IS_LESS_THAN.equals( elementName ) ) {
            comparisonOperator = new PropertyIsLessThanOperator( parameter1, parameter2, matchCase );
        } else if ( PROPERTY_IS_GREATER_THAN.equals( elementName ) ) {
            comparisonOperator = new PropertyIsGreaterThanOperator( parameter1, parameter2, matchCase );
        } else if ( PROPERTY_IS_LESS_THAN_OR_EQUAL_TO.equals( elementName ) ) {
            comparisonOperator = new PropertyIsLessThanOrEqualToOperator( parameter1, parameter2, matchCase );
        } else if ( PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO.equals( elementName ) ) {
            comparisonOperator = new PropertyIsGreaterThanOrEqualToOperator( parameter1, parameter2, matchCase );
        } else {
            assert false;
        }
        return comparisonOperator;
    }

    /**
     * Parses the given element as a {@link BooleanOperator}.
     * <p>
     * The given element must be one of the following:
     * <ul>
     * {http://www.opengis.net/ogc}:And
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:Or
     * </ul>
     * <ul>
     * {http://www.opengis.net/ogc}:Not
     * </ul>
     * <li>
     * 
     * @param element
     *            element to be parsed
     * @return corresponding boolean operator object
     */
    private BooleanOperator parseLogicalOperator( OMElement element ) {

        BooleanOperator logicalOperator;
        QName elementName = element.getQName();

        if ( elementName.equals( AND ) ) {
            Iterator childElementIter = element.getChildElements();
            BooleanOperator parameter1 = null;
            BooleanOperator parameter2 = null;
            try {
                OMElement parameterElement = (OMElement) childElementIter.next();
                parameter1 = parseOperator( parameterElement );
                parameterElement = (OMElement) childElementIter.next();
                parameter2 = parseOperator( parameterElement );
                if ( childElementIter.hasNext() ) {
                    throw new NoSuchElementException();
                }
            } catch ( NoSuchElementException e ) {
                String msg = Messages.getMessage( "FILTER_PARSING_WRONG_CHILD_COUNT", AND, 2 );
                throw new XMLParsingException( msg );
            }
            logicalOperator = new AndOperator( parameter1, parameter2 );
        } else if ( elementName.equals( OR ) ) {
            Iterator childElementIter = element.getChildElements();
            BooleanOperator parameter1 = null;
            BooleanOperator parameter2 = null;
            try {
                OMElement parameterElement = (OMElement) childElementIter.next();
                parameter1 = parseOperator( parameterElement );
                parameterElement = (OMElement) childElementIter.next();
                parameter2 = parseOperator( parameterElement );
                if ( childElementIter.hasNext() ) {
                    throw new NoSuchElementException();
                }
            } catch ( NoSuchElementException e ) {
                String msg = Messages.getMessage( "FILTER_PARSING_WRONG_CHILD_COUNT", OR, 2 );
                throw new XMLParsingException( msg );
            }
            logicalOperator = new OrOperator( parameter1, parameter2 );
        } else {
            Iterator childElementIter = element.getChildElements();
            BooleanOperator parameter = null;
            try {
                OMElement parameterElement = (OMElement) childElementIter.next();
                parameter = parseOperator( parameterElement );
                parameterElement = (OMElement) childElementIter.next();
                if ( childElementIter.hasNext() ) {
                    throw new NoSuchElementException();
                }
            } catch ( NoSuchElementException e ) {
                String msg = Messages.getMessage( "FILTER_PARSING_WRONG_CHILD_COUNT", NOT, 1 );
                throw new XMLParsingException( msg );
            }
            logicalOperator = new NotOperator( parameter );
        }
        return logicalOperator;
    }

    private boolean isNameInArray( QName name, QName[] nameArray ) {
        for ( QName name2 : nameArray ) {
            if ( name2.equals( name ) ) {
                return true;
            }
        }
        return false;
    }

    private boolean isComparisonOperator( QName name ) {
        return isNameInArray( name, COMPARISON_OPS );
    }

    private boolean isSpatialOperator( QName name ) {
        return isNameInArray( name, SPATIAL_OPS );
    }

    private boolean isLogicalOperator( QName name ) {
        return isNameInArray( name, LOGICAL_OPS );
    }

    private boolean isIdSpecifier( QName name ) {
        return isNameInArray( name, ID_SPECIFIERS );
    }

    /**
     * Serializes the given {@link Filter} object.
     * 
     * @param filter
     *            <code>Filter</code> object to be serialized
     * @param writer
     *            where the xml is written
     * @throws XMLStreamException
     */
    public static void export( Filter filter, XMLStreamWriter writer )
                            throws XMLStreamException {

        writer.setPrefix( "ogc", "http://www.opengis.net/ogc"  );        
        
        writer.writeStartElement( FILTER.getNamespaceURI(), FILTER.getLocalPart() );
        if ( filter instanceof ComplexFilter ) {
            export (((ComplexFilter) filter).getOperator(), writer);
        } else if ( filter instanceof IdFilter ) {

        } else {
            assert false;
        }
        writer.writeEndElement();
    }

    /**
     * Serializes the given {@link BooleanOperator} object.
     * 
     * @param operator
     *            <code>BooleanOperator</code> object to be serialized
     * @param writer
     *            where the xml is written
     * @throws XMLStreamException
     */
    private static void export( BooleanOperator operator, XMLStreamWriter writer )
                            throws XMLStreamException {

        if ( operator instanceof AndOperator ) {
            writer.writeStartElement( AND.getNamespaceURI(), AND.getLocalPart() );
            export( ( (AndOperator) operator ).getParameter1(), writer );
            export( ( (AndOperator) operator ).getParameter2(), writer );
            writer.writeEndElement();            
        } else if ( operator instanceof OrOperator ) {
            writer.writeStartElement( OR.getNamespaceURI(), OR.getLocalPart() );
            export( ( (OrOperator) operator ).getParameter1(), writer );
            export( ( (OrOperator) operator ).getParameter2(), writer );
            writer.writeEndElement();            
        } else if ( operator instanceof NotOperator ) {
            writer.writeStartElement( NOT.getNamespaceURI(), NOT.getLocalPart() );
            export( ( (NotOperator) operator ).getParameter(), writer );
            writer.writeEndElement();            
        } else if ( operator instanceof PropertyIsEqualToOperator ) {
            writer.writeStartElement( PROPERTY_IS_EQUAL_TO.getNamespaceURI(), PROPERTY_IS_EQUAL_TO.getLocalPart() );
            export( ( (PropertyIsEqualToOperator) operator ).getParameter1(), writer );
            export( ( (PropertyIsEqualToOperator) operator ).getParameter2(), writer );
            writer.writeEndElement();            
        } else if ( operator instanceof PropertyIsNotEqualToOperator ) {
            writer.writeStartElement( PROPERTY_IS_NOT_EQUAL_TO.getNamespaceURI(),
                                      PROPERTY_IS_NOT_EQUAL_TO.getLocalPart() );
            export( ( (PropertyIsNotEqualToOperator) operator ).getParameter1(), writer );
            export( ( (PropertyIsNotEqualToOperator) operator ).getParameter2(), writer );
            writer.writeEndElement();            
        } else if ( operator instanceof PropertyIsLessThanOperator ) {
            writer.writeStartElement( PROPERTY_IS_LESS_THAN.getNamespaceURI(), PROPERTY_IS_LESS_THAN.getLocalPart() );
            export( ( (PropertyIsLessThanOperator) operator ).getParameter1(), writer );
            export( ( (PropertyIsLessThanOperator) operator ).getParameter2(), writer );
            writer.writeEndElement();            
        } else if ( operator instanceof PropertyIsGreaterThanOperator ) {
            writer.writeStartElement( PROPERTY_IS_GREATER_THAN.getNamespaceURI(),
                                      PROPERTY_IS_GREATER_THAN.getLocalPart() );
            export( ( (PropertyIsGreaterThanOperator) operator ).getParameter1(), writer );
            export( ( (PropertyIsGreaterThanOperator) operator ).getParameter2(), writer );
            writer.writeEndElement();            
        } else if ( operator instanceof PropertyIsLessThanOrEqualToOperator ) {
            writer.writeStartElement( PROPERTY_IS_LESS_THAN_OR_EQUAL_TO.getNamespaceURI(),
                                      PROPERTY_IS_LESS_THAN_OR_EQUAL_TO.getLocalPart() );
            export( ( (PropertyIsLessThanOrEqualToOperator) operator ).getParameter1(), writer );
            export( ( (PropertyIsLessThanOrEqualToOperator) operator ).getParameter2(), writer );
            writer.writeEndElement();            
        } else if ( operator instanceof PropertyIsGreaterThanOrEqualToOperator ) {
            writer.writeStartElement( PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO.getNamespaceURI(),
                                      PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO.getLocalPart() );
            export( ( (PropertyIsGreaterThanOrEqualToOperator) operator ).getParameter1(), writer );
            export( ( (PropertyIsGreaterThanOrEqualToOperator) operator ).getParameter2(), writer );
            writer.writeEndElement();            
        } else if ( operator instanceof PropertyIsLikeOperator ) {
            writer.writeStartElement( PROPERTY_IS_LIKE.getNamespaceURI(),
                                      PROPERTY_IS_LIKE.getLocalPart() );
            PropertyIsLikeOperator isLikeOperator = (PropertyIsLikeOperator) operator;
            writer.writeAttribute( "wildCard", isLikeOperator.getWildCard() );
            writer.writeAttribute( "singleChar", isLikeOperator.getSingleChar() );
            writer.writeAttribute( "escapeChar", isLikeOperator.getEscapeChar() );
            export( isLikeOperator.getPropertyName(), writer );
            export( isLikeOperator.getLiteral(), writer );
            writer.writeEndElement();            
        } else if ( operator instanceof PropertyIsNullOperator ) {
            writer.writeStartElement( PROPERTY_IS_NULL.getNamespaceURI(),
                                      PROPERTY_IS_NULL.getLocalPart() );
            export( ( (PropertyIsNullOperator) operator ).getPropertyName(), writer );
            writer.writeEndElement();            
        } else if ( operator instanceof PropertyIsBetweenOperator ) {
            PropertyIsBetweenOperator isBetween = (PropertyIsBetweenOperator) operator;            
            writer.writeStartElement( PROPERTY_IS_BETWEEN.getNamespaceURI(),
                                      PROPERTY_IS_BETWEEN.getLocalPart() );
            export( isBetween.getExpression(), writer );
            writer.writeStartElement( LOWER_BOUNDARY.getNamespaceURI(), LOWER_BOUNDARY.getLocalPart() );
            writer.writeEndElement();
            writer.writeStartElement( UPPER_BOUNDARY.getNamespaceURI(), UPPER_BOUNDARY.getLocalPart() );
            writer.writeEndElement();
            writer.writeEndElement();            
        } else {
            assert false;
        }
    }

    private static void export( Expression expression, XMLStreamWriter writer ) throws XMLStreamException {
        if (expression instanceof PropertyName) {
            writer.writeStartElement( PROPERTY_NAME.getNamespaceURI(), PROPERTY_NAME.getLocalPart() );
            writer.writeCharacters( ((PropertyName) expression).getPropertyName() );
            writer.writeEndElement();
        } else if (expression instanceof Literal) {
            writer.writeStartElement( LITERAL.getNamespaceURI(), LITERAL.getLocalPart() );
            writer.writeCharacters( ((Literal) expression).getValue() );
            writer.writeEndElement();            
        } else if (expression instanceof Function) {
            Function function = (Function) expression;
            writer.writeStartElement( FUNCTION.getNamespaceURI(), FUNCTION.getLocalPart() );
            writer.writeAttribute( NAME.getLocalPart(), function.getName() );
            for ( Expression param : function.getParameters() ) {
                export (param, writer);
            }
            writer.writeEndElement();                  
        } else if (expression instanceof AddExpression) {
            writer.writeStartElement( ADD.getNamespaceURI(), ADD.getLocalPart() );
            export (((AddExpression) expression).getParameter1(), writer);
            export (((AddExpression) expression).getParameter2(), writer);
            writer.writeEndElement();             
        } else if (expression instanceof SubExpression) {
            writer.writeStartElement( SUB.getNamespaceURI(), SUB.getLocalPart() );
            export (((SubExpression) expression).getParameter1(), writer);
            export (((SubExpression) expression).getParameter2(), writer);
            writer.writeEndElement();            
        } else if (expression instanceof MulExpression) {
            writer.writeStartElement( MUL.getNamespaceURI(), MUL.getLocalPart() );
            export (((MulExpression) expression).getParameter1(), writer);
            export (((MulExpression) expression).getParameter2(), writer);
            writer.writeEndElement();              
        } else if (expression instanceof DivExpression) {
            writer.writeStartElement( DIV.getNamespaceURI(), DIV.getLocalPart() );
            export (((DivExpression) expression).getParameter1(), writer);
            export (((DivExpression) expression).getParameter2(), writer);
            writer.writeEndElement();              
        } else {
            assert false;
        }
    }
}
