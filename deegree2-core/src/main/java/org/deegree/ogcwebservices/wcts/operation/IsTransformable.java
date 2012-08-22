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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deegree.framework.util.Pair;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wcts.WCTSExceptionCode;
import org.deegree.ogcwebservices.wcts.WCTService;

/**
 * <code>IsTransformable</code> encapsulates the bean representation of the xml-dom or kvp encoded IsTransformable
 * request of the wcts 0.4.0
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class IsTransformable extends WCTSRequestBase {

    private static final long serialVersionUID = 5834206406155257853L;

    private final CoordinateSystem sourceCRS;

    private final CoordinateSystem targetCRS;

    private final String transformation;

    private final String method;

    private final List<Pair<String, String>> geometryTypes;

    private final List<String> simpleGeometryTypes;

    private final List<Pair<String, String>> coverageTypes;

    private final List<Pair<String, String>> interpolationTypes;

    /**
     * @param version
     * @param id
     * @param sourceCRS
     *            if <code>null</code> it is assumed that the sourceCRS is not supported.
     * @param targetCRS
     *            if <code>null</code> it is assumed that the targetCRS is not supported.
     * @param transformation
     * @param method
     * @param geometryTypes
     * @param coverageTypes
     * @param interpolationTypes
     */
    public IsTransformable( String version, String id, CoordinateSystem sourceCRS, CoordinateSystem targetCRS,
                            String transformation, String method, List<Pair<String, String>> geometryTypes,
                            List<Pair<String, String>> coverageTypes, List<Pair<String, String>> interpolationTypes ) {

        super( version, id, null );
        this.sourceCRS = sourceCRS;
        this.targetCRS = targetCRS;
        this.transformation = transformation;
        this.method = method;
        if ( coverageTypes != null ) {
            throw new IllegalArgumentException( Messages.getMessage( "WCTS_ISTRANSFORMABLE_COVERAGE_NOT_SUPPORTED" ) );
        }
        this.coverageTypes = coverageTypes;
        if ( interpolationTypes != null ) {
            throw new IllegalArgumentException(
                                                Messages.getMessage( "WCTS_ISTRANSFORMABLE_INTERPOLATION_NOT_SUPPORTED" ) );
        }
        this.interpolationTypes = interpolationTypes;
        this.geometryTypes = geometryTypes;
        this.simpleGeometryTypes = null;
    }

    /**
     * @param version
     * @param requestID
     * @param sourceCRS
     *            if <code>null</code> it is assumed that the sourceCRS is not supported.
     * @param targetCRS
     *            if <code>null</code> it is assumed that the targetCRS is not supported.
     * @param geometryTypes
     *            a list of simple geometry representations.
     */
    public IsTransformable( String version, String requestID, CoordinateSystem sourceCRS, CoordinateSystem targetCRS,
                            List<String> geometryTypes ) {
        super( version, requestID, null );
        this.sourceCRS = sourceCRS;
        this.targetCRS = targetCRS;
        this.transformation = null;
        this.method = null;
        this.coverageTypes = null;
        this.interpolationTypes = null;

        this.geometryTypes = null;
        this.simpleGeometryTypes = geometryTypes;
    }

    /**
     * @return the sourceCRS, if <code>null</code> it can be assumed that the sourceCRS is not supported.
     */
    public final CoordinateSystem getSourceCRS() {
        return sourceCRS;
    }

    /**
     * @return the targetCRS, if <code>null</code> it can be assumed that the targetCRS is not supported.
     */
    public final CoordinateSystem getTargetCRS() {
        return targetCRS;
    }

    /**
     * @return the transformation currently <code>null</code>
     */
    public final String getTransformation() {
        return transformation;
    }

    /**
     * @return the method currently <code>null</code>
     */
    public final String getMethod() {
        return method;
    }

    /**
     * @return the geometryTypes as a list of &lt value, codeSpace &gt; pairs. May be <code>null</code>, if so, check
     *         {@link #getSimpleGeometryTypes()} to see if the request defined geometryTypes by KVP.
     */
    public final List<Pair<String, String>> getGeometryTypes() {
        return geometryTypes;
    }

    /**
     * @return the simpleGeometryTypes. Maybe <code>null</code>, if so check {@link #getGeometryTypes()} to see if
     *         xml encoded geometryTypes were supplied.
     */
    public final List<String> getSimpleGeometryTypes() {
        return simpleGeometryTypes;
    }

    /**
     * @return the coverageTypes currently <code>null</code>.
     */
    public final List<Pair<String, String>> getCoverageTypes() {
        return coverageTypes;
    }

    /**
     * @return the interpolationTypes currently <code>null</code>.
     */
    public final List<Pair<String, String>> getInterpolationTypes() {
        return interpolationTypes;
    }

    /**
     * Create a {@link IsTransformable}-request by extracting the values from the map, and calling the constructor with
     * these values.
     *
     * @param requestID
     *            service internal id for this request.
     * @param map
     *            to extract requested values from.
     * @return the bean representation
     * @throws OGCWebServiceException
     *             if the map is <code>null</code> or has size==0, or the service,request parameters have none
     *             accepted values.
     */
    public static IsTransformable create( String requestID, Map<String, String> map )
                            throws OGCWebServiceException {
        if ( map == null || map.size() == 0 ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_REQUESTMAP_NULL" ),
                                              ExceptionCode.MISSINGPARAMETERVALUE );
        }
        String service = map.get( "SERVICE" );
        if ( service == null || !"WCTS".equals( service ) ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_NO_VERSION_KVP", service ),
                                              ExceptionCode.MISSINGPARAMETERVALUE );
        }
        String request = map.get( "REQUEST" );
        if ( request == null || !"IsTransformable".equalsIgnoreCase( request ) ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_NO_REQUEST_KVP", "IsTransformable" ),
                                              ( request == null ? ExceptionCode.MISSINGPARAMETERVALUE
                                                               : ExceptionCode.OPERATIONNOTSUPPORTED ) );
        }
        String version = map.get( "VERSION" );
        if ( version == null || !WCTService.version.equalsIgnoreCase( version ) ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_NO_VERSION_KVP", version ),
                                              ExceptionCode.MISSINGPARAMETERVALUE );
        }

        String sCRS = map.get( "SOURCECRS" );
        String tCRS = map.get( "TARGETCRS" );
        if ( ( ( sCRS != null && !"".equals( sCRS.trim() ) ) && ( tCRS == null || "".equals( tCRS.trim() ) ) )
             || ( ( tCRS != null && !"".equals( tCRS.trim() ) ) && ( sCRS == null || "".equals( sCRS.trim() ) ) ) ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_MISSING_MUTUAL_KEY_KVP",
                                                                   ( ( sCRS == null ) ? "TargetCRS" : "SourceCRS" ),
                                                                   ( ( sCRS == null ) ? "SourceCRS" : "TargetCRS" ) ),
                                              ExceptionCode.INVALIDPARAMETERVALUE );
        }
        if ( ( sCRS == null || "".equals( sCRS.trim() ) ) && ( tCRS == null || "".equals( tCRS.trim() ) ) ) {
            String transformation = map.get( "TRANSFORMATION" );
            if ( !( transformation == null || "".equals( transformation.trim() ) ) ) {
                throw new OGCWebServiceException(
                                                  Messages.getMessage( "WCTS_OPERATION_NOT_SUPPORTED",
                                                                       "defining of transformations (Transformation key)" ),
                                                  ExceptionCode.OPERATIONNOTSUPPORTED );
            }

            String method = map.get( "METHOD" );
            if ( !( method == null || "".equals( method.trim() ) ) ) {
                throw new OGCWebServiceException( Messages.getMessage( "WCTS_OPERATION_NOT_SUPPORTED",
                                                                       "transformation method (Method key)" ),
                                                  ExceptionCode.OPERATIONNOTSUPPORTED );
            }
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_TRANSFORMATION_NO_CRS_OR_TRANSFORM_KVP" ),
                                              ExceptionCode.MISSINGPARAMETERVALUE );

        }
        CoordinateSystem sourceCRS = null;
        CoordinateSystem targetCRS = null;
        try {
            sourceCRS = CRSFactory.create( WCTService.CRS_PROVIDER, sCRS );
            targetCRS = CRSFactory.create( WCTService.CRS_PROVIDER, tCRS );
        } catch ( UnknownCRSException e ) {
            throw new OGCWebServiceException( e.getMessage(), WCTSExceptionCode.UNSUPPORTED_COMBINATION );
        }

        String tmp = map.get( "GEOMETRYTYPES" );
        List<String> geometryTypes = new ArrayList<String>( 10 );
        if ( tmp != null && !"".equals( tmp.trim() ) ) {
            String[] splitter = tmp.split( "," );
            for ( String split : splitter ) {
                if ( split != null && !"".equals( split.trim() ) ) {
                    geometryTypes.add( split.trim() );
                }
            }
        }

        String coverageTypes = map.get( "COVERAGETYPES" );
        if ( !( coverageTypes == null || "".equals( coverageTypes.trim() ) ) ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_OPERATION_NOT_SUPPORTED",
                                                                   "CoverageTypes (key)" ),
                                              ExceptionCode.OPERATIONNOTSUPPORTED );
        }

        String interPolationType = map.get( "INTERPOLATIONTYPES" );
        if ( !( interPolationType == null || "".equals( interPolationType.trim() ) ) ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_OPERATION_NOT_SUPPORTED",
                                                                   "InterpolationTypes (key)" ),
                                              ExceptionCode.OPERATIONNOTSUPPORTED );
        }

        return new IsTransformable( version, requestID, sourceCRS, targetCRS, geometryTypes );
    }

}
