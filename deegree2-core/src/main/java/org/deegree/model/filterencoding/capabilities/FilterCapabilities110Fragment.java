// $HeadURL:
// /deegreerepository/deegree/src/org/deegree/model/filterencoding/capabilities/FilterCapabilities110Factory.java,v
// 1.1 2005/03/04 16:33:28 mschneider Exp $
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

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcwebservices.getcapabilities.UnknownOperatorNameException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class FilterCapabilities110Fragment extends XMLFragment {

    private static final long serialVersionUID = -5371910986551767458L;

    /**
     * Creates a new <code>FilterCapabilities110Fragment</code> from the given parameters.
     *
     * @param url
     * @throws SAXException
     * @throws IOException
     */
    public FilterCapabilities110Fragment( URL url ) throws IOException, SAXException {
        super( url );
    }

    /**
     * Creates a new <code>FilterCapabilities110Fragment</code> from the given parameters.
     *
     * @param element
     * @param systemId
     */
    public FilterCapabilities110Fragment( Element element, URL systemId ) {
        super( element );
        setSystemId( systemId );
    }

    /**
     * Returns the object representation for the <code>ogc:Filter_Capabilities</code> root
     * element.
     *
     * @return object representation for the given <code>ogc:Filter_Capabilities</code> element
     * @throws XMLParsingException
     */
    public FilterCapabilities parseFilterCapabilities()
                            throws XMLParsingException {
        return new FilterCapabilities(
                                       parseScalarCapabilities( (Element) XMLTools.getRequiredNode(
                                                                                                    getRootElement(),
                                                                                                    "ogc:Scalar_Capabilities",
                                                                                                    nsContext ) ),
                                       parseSpatialCapabilities( (Element) XMLTools.getRequiredNode(
                                                                                                     getRootElement(),
                                                                                                     "ogc:Spatial_Capabilities",
                                                                                                     nsContext ) ),
                                       parseIdCapabilities( (Element) XMLTools.getRequiredNode( getRootElement(),
                                                                                                "ogc:Id_Capabilities",
                                                                                                nsContext ) ) );
    }

    /**
     * Returns the object representation for an <code>ogc:Spatial_Capabilities</code> element that
     * is compliant to the <code>OpenGIS Filter Encoding Specification 1.1.0</code>.
     *
     * @return object representation for the given <code>ogc:Spatial_Capabilities</code> element
     * @throws XMLParsingException
     */
    private SpatialCapabilities parseSpatialCapabilities( Element spatialElement )
                            throws XMLParsingException {

        List<Node> geometryOperandList = XMLTools.getNodes( spatialElement,
                                                            "ogc:GeometryOperands/ogc:GeometryOperand/text()",
                                                            nsContext );
        QualifiedName[] geometryOperands = new QualifiedName[geometryOperandList.size()];
        for ( int i = 0; i < geometryOperands.length; i++ ) {
            geometryOperands[i] = XMLFragment.parseQualifiedName( geometryOperandList.get( i ) );
        }
        String[] operatorNames = XMLTools.getNodesAsStrings( spatialElement,
                                                             "ogc:SpatialOperators/ogc:SpatialOperator/@name",
                                                             nsContext );
        SpatialOperator[] spatialOperators = new SpatialOperator[operatorNames.length];
        for ( int i = 0; i < operatorNames.length; i++ ) {
            try {
                spatialOperators[i] = OperatorFactory110.createSpatialOperator( operatorNames[i] );
            } catch ( UnknownOperatorNameException e ) {
                throw new XMLParsingException( "Invalid operator name '" + operatorNames[i]
                                               + "' for spatial operator: " + e.getMessage(), e );
            }
        }
        return new SpatialCapabilities( spatialOperators, geometryOperands );
    }

    /**
     * Returns the object representation for an <code>ogc:Scalar_Capabilities</code> element that
     * is compliant to the <code>OpenGIS Filter Encoding Specification 1.1.0</code>.
     *
     * @return object representation for the given <code>ogc:Scalar_Capabilities</code> element
     * @throws XMLParsingException
     */
    private ScalarCapabilities parseScalarCapabilities( Element element )
                            throws XMLParsingException {

        boolean supportsLogicalOperators = XMLTools.getNode( element, "ogc:LogicalOperators", nsContext ) != null ? true
                                                                                                                 : false;
        Operator[] comparisonOperators = null;
        Operator[] arithmeticOperators = null;

        Element comparisonOperatorsElement = (Element) XMLTools.getNode( element, "ogc:ComparisonOperators", nsContext );
        if ( comparisonOperatorsElement != null ) {
            String[] operatorNames = XMLTools.getNodesAsStrings( comparisonOperatorsElement,
                                                                 "ogc:ComparisonOperator/text()", nsContext );
            comparisonOperators = new Operator[operatorNames.length];
            for ( int i = 0; i < operatorNames.length; i++ ) {
                try {
                    comparisonOperators[i] = OperatorFactory110.createComparisonOperator( operatorNames[i] );
                } catch ( UnknownOperatorNameException e ) {
                    throw new XMLParsingException( "Invalid operator name '" + operatorNames[i]
                                                   + "' for comparison operator: " + e.getMessage(), e );
                }
            }
        }

        Element arithmeticOperatorsElement = (Element) XMLTools.getNode( element, "ogc:ArithmeticOperators", nsContext );
        if ( arithmeticOperatorsElement != null ) {
            XMLTools.getRequiredNode( arithmeticOperatorsElement, "ogc:SimpleArithmetic", nsContext );
            List<Element> functionNameElementList = XMLTools.getElements(
                                                                          arithmeticOperatorsElement,
                                                                          "ogc:Functions/ogc:FunctionNames/ogc:FunctionName",
                                                                          nsContext );
            arithmeticOperators = new Function[functionNameElementList.size()];
            for ( int i = 0; i < arithmeticOperators.length; i++ ) {
                arithmeticOperators[i] = parseFunction( functionNameElementList.get( i ) );
            }
        }
        return new ScalarCapabilities( supportsLogicalOperators, comparisonOperators, arithmeticOperators );
    }

    /**
     * Returns the object representation for an <code>ogc:Id_Capabilities</code> element that is
     * compliant to the <code>OpenGIS Filter Encoding Specification 1.1.0</code>.
     *
     * @return object representation for the given <code>ogc:Id_Capabilities</code> element
     * @throws XMLParsingException
     */
    private IdCapabilities parseIdCapabilities( Element element )
                            throws XMLParsingException {
        List<Element> eidList = XMLTools.getElements( element, "ogc:EID", nsContext );
        Element[] eidElements = new Element[eidList.size()];
        for ( int i = 0; i < eidElements.length; i++ ) {
            eidElements[i] = eidList.get( i );
        }
        List<Element> fidList = XMLTools.getElements( element, "ogc:FID", nsContext );
        Element[] fidElements = new Element[fidList.size()];
        for ( int i = 0; i < fidElements.length; i++ ) {
            fidElements[i] = fidList.get( i );
        }
        return new IdCapabilities( eidElements, fidElements );
    }

    private Function parseFunction( Element element )
                            throws XMLParsingException {
        String name = XMLTools.getRequiredNodeAsString( element, "text()", nsContext );
        int nArgs = XMLTools.getRequiredNodeAsInt( element, "@nArgs", nsContext );
        return new Function( name, nArgs );
    }
    /*
     * private Operator parseSpatialOperator( Element element ) throws XMLParsingException { String
     * name = XMLTools.getRequiredNodeAsString( element, "@name", nsContext ); List
     * geometryOperandList = XMLTools.getNodes( element, "GeometryOperand/text()", nsContext );
     * QualifiedName[] operands = new QualifiedName[geometryOperandList.size()]; for ( int i = 0; i <
     * operands.length; i++ ) { operands[i] = XMLFragment.parseQualifiedName( (Node)
     * geometryOperandList.get( i ) ); } return new SpatialOperator( name, operands ); }
     */
}
