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

package org.deegree.ogcwebservices.wcts.operation;

import static org.deegree.framework.xml.XMLTools.getElements;
import static org.deegree.framework.xml.XMLTools.getNodeAsString;
import static org.deegree.framework.xml.XMLTools.getStringValue;

import java.util.ArrayList;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Pair;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wcts.WCTService;
import org.w3c.dom.Element;

/**
 * <code>IsTransformableDocument</code> is a helper class which is able to parse wcts isTransformable requests version
 * 0.4.0
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class IsTransformableDocument extends WCTSRequestBaseDocument {

    private static ILogger LOG = LoggerFactory.getLogger( IsTransformableDocument.class );

    private static final long serialVersionUID = 241071726326354269L;

    private IsTransformable isTransformable;

    /**
     * @param id
     * @param rootElement
     * @throws OGCWebServiceException
     *             if an {@link XMLParsingException} occurred or a mandatory element/attribute is missing.
     */
    public IsTransformableDocument( String id, Element rootElement ) throws OGCWebServiceException {
        super( rootElement );
        String version = parseVersion();

        // check for valid request.
        parseService();
        try {
            String sCRS = getNodeAsString( getRootElement(), PRE + "SourceCRS", nsContext, null );
            String tCRS = getNodeAsString( getRootElement(), PRE + "TargetCRS", nsContext, null );
            CoordinateSystem sourceCRS = null;
            CoordinateSystem targetCRS = null;
            String transformation = null;
            String method = null;
            if ( ( sCRS != null && tCRS == null ) || ( sCRS == null && tCRS != null ) ) {
                throw new OGCWebServiceException(
                                                  Messages.getMessage(
                                                                       "WCTS_ISTRANSFORMABLE_MISSING_CRS",
                                                                       ( ( sCRS == null ) ? "TargetCRS" : "SourceCRS" ),
                                                                       ( ( sCRS == null ) ? "SourceCRS" : "TargetCRS" ) ),
                                                  ExceptionCode.INVALIDPARAMETERVALUE );
            }
            if ( sCRS == null && tCRS == null ) {
                transformation = getNodeAsString( getRootElement(), PRE + "Transformation", nsContext, null );

                method = getNodeAsString( getRootElement(), PRE + "Method", nsContext, null );
            } else {
                // handle the creation of the crs's separately because if one fails the other may still be valid.
                try {
                    targetCRS = CRSFactory.create( WCTService.CRS_PROVIDER, tCRS );
                } catch ( UnknownCRSException e ) {
                    // LOG.logError( e.getMessage(), e );
                    // here an ogc webservice exception should (could) be thrown, but since the 'spec' defines an other
                    // response, we have to ignore it and handle it later.
                    // throw new OGCWebServiceException( e.getMessage(), ExceptionCode.NOAPPLICABLECODE );
                }

                try {
                    sourceCRS = CRSFactory.create( WCTService.CRS_PROVIDER, sCRS );
                } catch ( UnknownCRSException e ) {
                    // LOG.logError( e.getMessage(), e );
                }
            }

            // Check for not supported values.
            if ( transformation != null ) {
                throw new OGCWebServiceException( Messages.getMessage( "WCTS_ISTRANSFORMABLE_NOT_KNOWN",
                                                                       "transformation" ),
                                                  ExceptionCode.INVALIDPARAMETERVALUE );
            }
            if ( method != null ) {
                throw new OGCWebServiceException( Messages.getMessage( "WCTS_ISTRANSFORMABLE_NOT_KNOWN", "Method" ),
                                                  ExceptionCode.INVALIDPARAMETERVALUE );
            }

            List<Pair<String, String>> geometryTypes = parseGeometryTypes();
            // just do a check if the coverageTypes were requested.
            List<Pair<String, String>> coverageTypes = parseCoverageTypes();
            // just do a check if the interpolationTypes were requested.
            List<Pair<String, String>> interpolationTypes = parseInterpolationTypes();
            this.isTransformable = new IsTransformable( version, id, sourceCRS, targetCRS, transformation, method,
                                                        geometryTypes, coverageTypes, interpolationTypes );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage(), ExceptionCode.NOAPPLICABLECODE );
        }

    }

    /**
     * @return a list containing &lt;value, codeSpace &gt; pairs. If codeSpace, was not set it will contain the default
     *         value: <code>http://schemas.opengis.net/wcts/0.0.0/geometryType.xml</code>. The result can be empty
     *         but never <code>null</code>.
     * @throws XMLParsingException
     *             if an error occurs while getting all elements of type geometryType.
     */
    protected List<Pair<String, String>> parseGeometryTypes()
                            throws XMLParsingException {
        List<Element> geometryTypes = getElements( getRootElement(), PRE + "GeometryType", nsContext );
        List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
        if ( geometryTypes != null && geometryTypes.size() > 0 ) {
            for ( Element geomType : geometryTypes ) {
                if ( geomType != null ) {
                    String geom = getStringValue( geomType );
                    String codeSpace = geomType.getAttribute( "codeSpace" );
                    if ( "".equals( codeSpace ) ) {
                        codeSpace = "http://schemas.opengis.net/wcts/0.0.0/geometryType.xml";
                    }
                    result.add( new Pair<String, String>( geom, codeSpace ) );
                }
            }
        }
        return result;
    }

    /**
     * @return always <code>null</code>, because this operation is not supported by the wcts.
     * @throws XMLParsingException
     *             if an error occurs while getting all elements of type coverageType.
     * @throws OGCWebServiceException
     *             if coverageType elements were found.
     */
    protected List<Pair<String, String>> parseCoverageTypes()
                            throws XMLParsingException, OGCWebServiceException {
        List<Element> coverageTypes = getElements( getRootElement(), PRE + "CoverageType", nsContext );
        if ( coverageTypes != null && coverageTypes.size() > 0 ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_OPERATION_NOT_SUPPORTED",
                                                                   "transformation of coverages" ),
                                              ExceptionCode.OPERATIONNOTSUPPORTED );
        }
        return null;
    }

    /**
     * @return always <code>null</code>, because this operation is not supported by the wcts.
     * @throws XMLParsingException
     *             if an error occurs while getting all elements of type interpolationType.
     * @throws OGCWebServiceException
     *             if interpolationType elements were found.
     */
    protected List<Pair<String, String>> parseInterpolationTypes()
                            throws XMLParsingException, OGCWebServiceException {
        List<Element> interpolationTypes = getElements( getRootElement(), CommonNamespaces.WCS_1_2_0_PREFIX
                                                                          + ":InterpolationType", nsContext );
        if ( interpolationTypes != null && interpolationTypes.size() > 0 ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_OPERATION_NOT_SUPPORTED",
                                                                   "interpolation of coverages" ),
                                              ExceptionCode.OPERATIONNOTSUPPORTED );
        }
        return null;
    }

    /**
     * @return the isTransformable.
     */
    public final IsTransformable getIsTransformable() {
        return isTransformable;
    }

}
