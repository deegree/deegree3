// $HeadURL:
// /deegreerepository/deegree/src/org/deegree/model/filterencoding/capabilities/FilterCapabilities100Factory.java,v
// 1.3 2005/03/09 11:55:46 mschneider Exp $
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
package org.deegree.model.filterencoding.capabilities;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.getcapabilities.UnknownOperatorNameException;
import org.w3c.dom.Element;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class FilterCapabilities100Fragment extends XMLFragment {

    private static final long serialVersionUID = 2430362135205814360L;

    private static final URI OGCNS = CommonNamespaces.OGCNS;

    private static final ILogger LOG = LoggerFactory.getLogger( FilterCapabilities100Fragment.class );

    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    /**
     * Creates a new <code>FilterCapabilities100Fragment</code> from the given parameters.
     *
     * @param element
     * @param systemId
     */
    public FilterCapabilities100Fragment( Element element, URL systemId ) {
        super( element );
        setSystemId( systemId );
    }

    /**
     * Returns the object representation for the <code>ogc:Filter_Capabilities</code> root element.
     *
     * @return object representation for the given <code>ogc:Filter_Capabilities</code> element
     * @throws XMLParsingException
     */
    public FilterCapabilities parseFilterCapabilities()
                            throws XMLParsingException {
        Element e1 = (Element) XMLTools.getRequiredNode( getRootElement(), "ogc:Scalar_Capabilities", nsContext );
        Element e2 = (Element) XMLTools.getRequiredNode( getRootElement(), "ogc:Spatial_Capabilities", nsContext );
        return new FilterCapabilities( parseScalarCapabilities( e1 ), parseSpatialCapabilities( e2 ) );
    }

    /**
     * Returns the object representation for an <code>ogc:Spatial_Capabilities</code> element.
     *
     * @return object representation for the given <code>ogc:Spatial_Capabilities</code> element
     * @throws XMLParsingException
     */
    private SpatialCapabilities parseSpatialCapabilities( Element spatialElement )
                            throws XMLParsingException {
        Map<String, Element> operatorMap = parseOperators( (Element) XMLTools.getRequiredNode( spatialElement,
                                                                                               "ogc:Spatial_Operators",
                                                                                               nsContext ) );
        ArrayList<SpatialOperator> operators = new ArrayList<SpatialOperator>();
        Iterator<String> it = operatorMap.keySet().iterator();
        while ( it.hasNext() ) {
            String next = it.next();
            try {
                operators.add( OperatorFactory100.createSpatialOperator( next ) );
            } catch ( UnknownOperatorNameException e ) {
                LOG.logWarning( "Operator name not found. Trying again with filter encoding 1.1.0 names..." );
                try {
                    operators.add( OperatorFactory110.createSpatialOperator( next ) );
                } catch ( UnknownOperatorNameException e2 ) {
                    LOG.logError( "Still not found. Here's two stack traces:" );
                    LOG.logError( e.getMessage(), e );
                    LOG.logError( e2.getMessage(), e2 );
                }
            }
        }
        return new SpatialCapabilities( operators.toArray( new SpatialOperator[operators.size()] ) );
    }

    /**
     * Returns the object representation for an <code>ogc:Scalar_Capabilities</code> element.
     *
     * @return object representation for the given <code>ogc:Scalar_Capabilities</code> element
     * @throws XMLParsingException
     */
    private ScalarCapabilities parseScalarCapabilities( Element scalarElement )
                            throws XMLParsingException {

        // "Logical_Operators"-element
        boolean supportsLogicalOperators = false;
        if ( XMLTools.getChildElement( "Logical_Operators", OGCNS, scalarElement ) != null ) {
            supportsLogicalOperators = true;
        }

        // "Comparison_Operators"-element
        Element elem = XMLTools.getChildElement( "Comparison_Operators", OGCNS, scalarElement );
        ArrayList<Operator> operators = new ArrayList<Operator>();
        Map<String, Element> operatorMap = null;
        if ( elem != null ) {
            operatorMap = parseOperators( elem );
            Iterator<String> it = operatorMap.keySet().iterator();
            while ( it.hasNext() ) {
                String next = it.next();
                try {
                    operators.add( OperatorFactory100.createComparisonOperator( next ) );
                } catch ( UnknownOperatorNameException e ) {
                    LOG.logWarning( "Operator name not found. Trying again with filter encoding 1.1.0 names..." );
                    try {
                        operators.add( OperatorFactory110.createComparisonOperator( next ) );
                    } catch ( UnknownOperatorNameException e2 ) {
                        LOG.logError( "Still not found. Here's two stack traces:" );
                        LOG.logError( e.getMessage(), e );
                        LOG.logError( e2.getMessage(), e2 );
                    }
                }
            }
        }
        Operator[] comparionsOperators = operators.toArray( new Operator[operators.size()] );
        operators = null;

        // "Arithmetic_Operators"-element
        elem = XMLTools.getChildElement( "Arithmetic_Operators", OGCNS, scalarElement );
        if ( elem != null ) {
            operatorMap = parseOperators( elem );
            operators = new ArrayList<Operator>();
            Iterator<String> it = operatorMap.keySet().iterator();
            while ( it.hasNext() ) {
                String operatorName = it.next();
                try {
                    if ( operatorName.equals( OperatorFactory100.OPERATOR_FUNCTIONS ) ) {
                        // functions definition
                        Element functionsElement = operatorMap.get( operatorName );
                        Element functionNamesElement = XMLTools.getRequiredChildElement( "Function_Names", OGCNS,
                                                                                         functionsElement );
                        List<Element> functionNameList = XMLTools.getRequiredElements( functionNamesElement,
                                                                                       "ogc:Function_Name", nsContext );
                        for ( int i = 0; i < functionNameList.size(); i++ ) {
                            Element functionNameElement = functionNameList.get( i );
                            String name = XMLTools.getStringValue( functionNameElement );
                            String argumentCount = XMLTools.getRequiredAttrValue( "nArgs", null, functionNameElement );
                            if ( name == null || name.length() == 0 ) {
                                throw new XMLParsingException( "Error parsing a 'Function_Name' (namespace: '" + OGCNS
                                                               + "') element: text node is empty." );
                            }
                            try {
                                operators.add( OperatorFactory100.createArithmeticFunction(
                                                                                            name,
                                                                                            Integer.parseInt( argumentCount ) ) );
                            } catch ( NumberFormatException e ) {
                                throw new XMLParsingException( "Error parsing 'Function_Name' (namespace: '" + OGCNS
                                                               + "') element: attribute 'nArgs'"
                                                               + " does not contain a valid integer value." );
                            }
                        }

                    } else {
                        // simple operator
                        operators.add( OperatorFactory100.createArithmeticOperator( operatorName ) );
                    }
                } catch ( UnknownOperatorNameException e ) {
                    LOG.logWarning( "Operator name not found. Trying again with filter encoding 1.1.0 names..." );
                    try {
                        operators.add( OperatorFactory110.createComparisonOperator( operatorName ) );
                    } catch ( UnknownOperatorNameException e2 ) {
                        LOG.logError( "Still not found. Here's two stack traces:" );
                        LOG.logError( e.getMessage(), e );
                        LOG.logError( e2.getMessage(), e2 );
                    }
                }
            }
        }
        Operator[] arithmeticOperators = operators != null ? operators.toArray( new Operator[operators.size()] )
                                                          : new Operator[0];
        return new ScalarCapabilities( supportsLogicalOperators, comparionsOperators, arithmeticOperators );
    }

    private Map<String, Element> parseOperators( Element operatorsElement ) {
        HashMap<String, Element> operators = new HashMap<String, Element>();
        ElementList operatorList = XMLTools.getChildElements( operatorsElement );
        for ( int i = 0; i < operatorList.getLength(); i++ ) {
            String namespaceURI = operatorList.item( i ).getNamespaceURI();
            if ( namespaceURI != null && namespaceURI.equals( OGCNS.toASCIIString() ) ) {
                operators.put( operatorList.item( i ).getLocalName(), operatorList.item( i ) );
            }
        }
        return operators;
    }
}
