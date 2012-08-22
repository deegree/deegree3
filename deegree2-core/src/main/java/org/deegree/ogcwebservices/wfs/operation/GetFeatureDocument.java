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
package org.deegree.ogcwebservices.wfs.operation;

import static java.lang.Integer.parseInt;
import static java.lang.Math.toDegrees;
import static org.deegree.crs.coordinatesystems.GeographicCRS.WGS84;
import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.deegree.framework.xml.XMLTools.getElements;
import static org.deegree.framework.xml.XMLTools.getNodeAsString;
import static org.deegree.framework.xml.XMLTools.getNodes;
import static org.deegree.framework.xml.XMLTools.getRequiredNodeAsString;
import static org.deegree.i18n.Messages.get;
import static org.deegree.i18n.Messages.getMessage;
import static org.deegree.model.crs.CRSFactory.create;
import static org.deegree.model.filterencoding.OperationDefines.BBOX;
import static org.deegree.model.filterencoding.OperationDefines.TYPE_LOGICAL;
import static org.deegree.model.filterencoding.OperationDefines.getTypeById;
import static org.deegree.ogcwebservices.wfs.configuration.WFSDeegreeParams.getSwitchAxes;
import static org.deegree.ogcwebservices.wfs.operation.AbstractWFSRequest.FORMAT_GML2_WFS100;
import static org.deegree.ogcwebservices.wfs.operation.AbstractWFSRequest.FORMAT_GML3;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Point2d;

import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.filterencoding.AbstractFilter;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.Function;
import org.deegree.model.filterencoding.LogicalOperation;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.wfs.operation.GetFeature.RESULT_TYPE;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Parser for "wfs:GetFeature" requests.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GetFeatureDocument extends AbstractWFSRequestDocument {

    private static final ILogger LOG = getLogger( GetFeatureDocument.class );

    private static final long serialVersionUID = -3411186861123355322L;

    /**
     * Parses the underlying document into a <code>GetFeature</code> request object.
     *
     * @param id
     * @return corresponding <code>GetFeature</code> object
     * @throws XMLParsingException
     * @throws InvalidParameterValueException
     */
    public GetFeature parse( String id )
                            throws InvalidParameterValueException, XMLParsingException {

        checkServiceAttribute();
        String version = checkVersionAttribute();
        boolean useVersion_1_0_0 = "1.0.0".equals( version );

        Element root = getRootElement();
        String handle = XMLTools.getNodeAsString( root, "@handle", nsContext, null );
        String outputFormat = XMLTools.getNodeAsString( root, "@outputFormat", nsContext,
                                                        useVersion_1_0_0 ? FORMAT_GML2_WFS100 : FORMAT_GML3 );

        int maxFeatures = XMLTools.getNodeAsInt( root, "@maxFeatures", nsContext, -1 );
        int startPosition = XMLTools.getNodeAsInt( root, "@startPosition", nsContext, 1 );
        if ( startPosition < 1 ) {
            String msg = Messages.getMessage( "WFS_INVALID_STARTPOSITION", Integer.toString( startPosition ) );
            throw new XMLParsingException( msg );
        }

        String depth = XMLTools.getNodeAsString( root, "@traverseXlinkDepth", nsContext, "*" );
        int traverseXLinkDepth = depth.equals( "*" ) ? -1 : parseInt( depth );
        int traverseXLinkExpiry = XMLTools.getNodeAsInt( root, "@traverseXlinkExpiry", nsContext, -1 );

        String resultTypeString = XMLTools.getNodeAsString( root, "@resultType", nsContext, "results" );
        RESULT_TYPE resultType;
        if ( "results".equals( resultTypeString ) ) {
            resultType = RESULT_TYPE.RESULTS;
        } else if ( "hits".equals( resultTypeString ) ) {
            resultType = RESULT_TYPE.HITS;
        } else {
            String msg = Messages.getMessage( "WFS_INVALID_RESULT_TYPE", resultTypeString );
            throw new XMLParsingException( msg );
        }

        // next time, perhaps we'll use real validation instead of ad hoc "validation" like this
        List<Element> nl = getElements( root, "wfs:Query", nsContext );
        try {
            XPath xpath = new DOMXPath( "count(*)" );
            xpath.numberValueOf( root );

            int cnt = xpath.numberValueOf( root ).intValue();

            if ( cnt != nl.size() ) {
                throw new InvalidParameterValueException( getMessage( "WFS_ONLY_QUERY_ELEMENTS_PERMITTED" ) );
            }

        } catch ( JaxenException e ) {
            // the xpath was tested...
        }

        if ( nl.size() == 0 ) {
            throw new InvalidParameterValueException( getMessage( "WFS_QUERY_ELEMENT_MISSING" ) );
        }
        Query[] queries = new Query[nl.size()];
        for ( int i = 0; i < queries.length; i++ ) {
            queries[i] = parseQuery( nl.get( i ), useVersion_1_0_0 );
        }

        // vendorspecific attributes; required by deegree rights management
        Map<String, String> vendorSpecificParams = parseDRMParams( root );

        GetFeature req = new GetFeature( version, id, handle, resultType, outputFormat, maxFeatures, startPosition,
                                         traverseXLinkDepth, traverseXLinkExpiry, queries, vendorSpecificParams );
        return req;
    }

    /**
     * Parses the given query element into a {@link Query} object with filter encoding 1.1.0.
     * <p>
     * Note that the following attributes from the surrounding element are also considered (if it is present):
     * <ul>
     * <li>resultType</li>
     * <li>maxFeatures</li>
     * <li>startPosition</li>
     * </ul>
     *
     * @param element
     *            query element
     * @return corresponding <code>Query</code> object
     * @throws XMLParsingException
     */
    Query parseQuery( Element element )
                            throws XMLParsingException {
        return parseQuery( element, false );

    }

    /**
     * Parses the given query element into a {@link Query} object.
     * <p>
     * Note that the following attributes from the surrounding element are also considered (if it is present):
     * <ul>
     * <li>resultType</li>
     * <li>maxFeatures</li>
     * <li>startPosition</li>
     * </ul>
     *
     * @param element
     *            query element
     * @param useVersion_1_0_0
     *            true, if the query is part of a 1.0.0 GetFeature request, otherwise false
     * @return corresponding <code>Query</code> object
     * @throws XMLParsingException
     */
    Query parseQuery( Element element, boolean useVersion_1_0_0 )
                            throws XMLParsingException {

        String handle = getNodeAsString( element, "@handle", nsContext, null );
        String typeNameList = getRequiredNodeAsString( element, "@typeName", nsContext );
        // handle both 1.1.0 and 1.2.0 delimiters
        String[] values = typeNameList.split( "[,\\s]" );
        QualifiedName[] typeNames = transformToQualifiedNames( values, element );
        String[] aliases = null;
        String aliasesList = getNodeAsString( element, "@aliases", nsContext, null );
        if ( aliasesList != null ) {
            aliases = aliasesList.split( "\\s" );
            if ( LOG.isDebug() ) {
                LOG.logDebug( "Found following aliases:" + Arrays.toString( aliases ) );
            }

            if ( aliases.length != typeNames.length ) {
                String msg = getMessage( "WFS_QUERY_ALIAS_WRONG_COUNT", Integer.toString( typeNames.length ),
                                         Integer.toString( aliases.length ) );
                throw new XMLParsingException( msg );
            }
            Set<String> tempSet = new HashSet<String>();
            for ( String alias : aliases ) {
                if ( tempSet.contains( alias ) ) {
                    String msg = getMessage( "WFS_QUERY_ALIAS_NOT_UNIQUE", alias );
                    throw new XMLParsingException( msg );
                }
                tempSet.add( alias );
            }
        }

        String featureVersion = getNodeAsString( element, "@featureVersion", nsContext, null );
        String srsName = getNodeAsString( element, "@srsName", nsContext, null );

        List<Node> nl = null;
        if ( useVersion_1_0_0 ) {
            nl = getNodes( element, "ogc:PropertyName", nsContext );
        } else {
            nl = getNodes( element, "wfs:PropertyName | wfs:XlinkPropertyName", nsContext );
        }
        PropertyPath[] propertyNames = new PropertyPath[nl.size()];
        for ( int i = 0; i < propertyNames.length; i++ ) {
            propertyNames[i] = parseExtendedPropertyPath( (Element) nl.get( i ) );
        }

        nl = XMLTools.getNodes( element, "ogc:Function", nsContext );
        Function[] functions = new Function[nl.size()];
        for ( int i = 0; i < functions.length; i++ ) {
            functions[i] = (Function) Function.buildFromDOM( (Element) nl.get( i ) );
        }

        Filter filter = null;
        Element filterElement = (Element) XMLTools.getNode( element, "ogc:Filter", nsContext );
        if ( filterElement != null ) {
            filter = AbstractFilter.buildFromDOM( filterElement, useVersion_1_0_0 );
        }

        SortProperty[] sortProps = null;
        Element sortByElement = (Element) XMLTools.getNode( element, "ogc:SortBy", nsContext );
        if ( sortByElement != null ) {
            sortProps = parseSortBy( sortByElement );
        }

        // ----------------------------------------------------------------------------------------
        // parse "inherited" attributes from GetFeature element (but very kindly)
        // ----------------------------------------------------------------------------------------

        String resultTypeString = "results";
        RESULT_TYPE resultType;
        try {
            resultTypeString = XMLTools.getNodeAsString( element, "../@resultType", nsContext, "results" );
        } catch ( XMLParsingException doNothing ) {
            // it's o.k. - let's be really kind here
        }

        if ( "results".equals( resultTypeString ) ) {
            resultType = RESULT_TYPE.RESULTS;
        } else if ( "hits".equals( resultTypeString ) ) {
            resultType = RESULT_TYPE.HITS;
        } else {
            String msg = Messages.getMessage( "WFS_INVALID_RESULT_TYPE", resultTypeString );
            throw new XMLParsingException( msg );
        }

        int maxFeatures = -1;
        try {
            maxFeatures = XMLTools.getNodeAsInt( element, "../@maxFeatures", nsContext, -1 );
        } catch ( XMLParsingException doNothing ) {
            // it's o.k. - let's be really kind here
        }

        int startPosition = -1;
        try {
            startPosition = XMLTools.getNodeAsInt( element, "../@startPosition", nsContext, 0 );
        } catch ( XMLParsingException doNothing ) {
            // it's o.k. - let's be really kind here
        }

        BBoxTest test = new BBoxTest( srsName, filter );

        return new Query( propertyNames, functions, sortProps, handle, featureVersion, typeNames, aliases, srsName,
                          filter, resultType, maxFeatures, startPosition, test );
    }

    /**
     * <code>BBoxTest</code> is a helper class that encapsulates the check for bounding boxes.
     *
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     *
     * @version $Revision$, $Date$
     */
    public static class BBoxTest {
        private String srsName;

        private Filter filter;

        /**
         * @param srsName
         * @param filter
         */
        public BBoxTest( String srsName, Filter filter ) {
            this.srsName = srsName;
            this.filter = filter;
        }

        /**
         * @throws InvalidParameterValueException
         */
        public void performTest()
                                throws InvalidParameterValueException {
            if ( srsName == null ) {
                return;
            }
            isBoundingBoxValid( srsName, filter );
        }
    }

    private static SpatialOperation extractFirstBBOX( Operation operation ) {
        if ( operation.getOperatorId() == BBOX ) {
            return (SpatialOperation) operation;
        }
        if ( getTypeById( operation.getOperatorId() ) == TYPE_LOGICAL ) {
            for ( Operation op : ( (LogicalOperation) operation ).getArguments() ) {
                SpatialOperation bbox = extractFirstBBOX( op );
                if ( bbox != null ) {
                    return bbox;
                }
            }
        }

        return null;
    }

    static void isBoundingBoxValid( String srsName, Filter filter )
                            throws InvalidParameterValueException {
        SpatialOperation bbox = null;

        if ( filter instanceof ComplexFilter ) {
            ComplexFilter cf = (ComplexFilter) filter;
            bbox = extractFirstBBOX( cf.getOperation() );
        }

        if ( bbox == null ) {
            return;
        }

        try {
            CoordinateSystem crs = create( srsName );

            if ( !( crs.getCRS() instanceof ProjectedCRS ) ) {
                return;
            }

            String code = crs.getCRS().getIdentifier().split( ":" )[1];

            if ( !( code.compareTo( "26901" ) > 0 && code.compareTo( "26929" ) < 0 )
                 && !( code.compareTo( "32601" ) > 0 && code.compareTo( "32660" ) < 0 )
                 && !( code.compareTo( "32701" ) > 0 && code.compareTo( "32760" ) < 0 ) ) {
                return;
            }

            Envelope bb = bbox.getGeometry().getEnvelope();
            bb = GeometryFactory.createEnvelope( bb.getMin(), bb.getMax(), bb.getCoordinateSystem() );
            if ( !bb.getCoordinateSystem().getCRS().equals( WGS84 ) ) {
                bb = new GeoTransformer( create( WGS84 ) ).transform( bb, bb.getCoordinateSystem() );
            }

            Point2d naturalOrigin = ( (ProjectedCRS) crs.getCRS() ).getProjection().getNaturalOrigin();

            boolean swap = getSwitchAxes();

            double degx = toDegrees( naturalOrigin.x );
            double left = swap ? bb.getMin().getX() : bb.getMin().getY();
            double right = swap ? bb.getMax().getX() : bb.getMin().getY();
            if ( !( degx > left && degx < right ) ) {
                throw new InvalidParameterValueException( get( "WFS_WRONG_UTM_STRIPE", left, degx, right ) );
            }

        } catch ( UnknownCRSException e ) {
            LOG.logError( "A problem occurred while parsing the request. Please report the stack trace.", e );
        } catch ( ClassCastException e ) {
            LOG.logError( "A problem occurred while parsing the request. Please report the stack trace.", e );
        } catch ( InvalidParameterException e ) {
            LOG.logError( "A problem occurred while parsing the request. Please report the stack trace.", e );
        } catch ( CRSTransformationException e ) {
            LOG.logError( "A problem occurred while parsing the request. Please report the stack trace.", e );
        }
    }

    /**
     * Parses the given "ogc:SortBy" element.
     *
     * @param root
     *            "ogc:SortBy" element
     * @return corresponding <code>SortProperty</code> instances (in original order)
     */
    private SortProperty[] parseSortBy( Element root )
                            throws XMLParsingException {

        List<Node> nl = XMLTools.getRequiredNodes( root, "ogc:SortProperty", nsContext );
        SortProperty[] sortProps = new SortProperty[nl.size()];
        for ( int i = 0; i < nl.size(); i++ ) {
            sortProps[i] = SortProperty.create( (Element) nl.get( i ) );
        }
        return sortProps;
    }
}
