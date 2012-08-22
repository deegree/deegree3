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

import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wcts.WCTSExceptionCode;
import org.deegree.ogcwebservices.wcts.WCTService;
import org.deegree.ogcwebservices.wcts.data.FeatureCollectionData;
import org.deegree.ogcwebservices.wcts.data.GeometryData;
import org.deegree.ogcwebservices.wcts.data.SimpleData;
import org.deegree.ogcwebservices.wcts.data.TransformableData;
import org.deegree.owscommon_1_1_0.Manifest;

/**
 * <code>Transform</code> is an encapsulation of a Transform request defined in the wcts 0.4.0 specification.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class Transform extends WCTSRequestBase {

    /**
     * Signals that the data was presented inline.
     */
    public final static int INLINE = 0;

    /**
     * Signals that the data was presented in multiparts.
     */
    public final static int MULTIPART = 1;

    private static ILogger LOG = LoggerFactory.getLogger( Transform.class );

    private static final long serialVersionUID = -7695383750627152056L;

    private final boolean store;

    private final CoordinateSystem sourceCRS;

    private final CoordinateSystem targetCRS;

    private final Manifest inputData;

    private final String inputDataString;

    private final String outputFormat;

    private final TransformableData<?> transformableData;

    private String requestIP = "Unknown IP";

    private final boolean extensiveLogging = false;

    private final int dataPresentation;

    private final TransformationReference transformationReference;

    /**
     * @param version
     *            of the request (0.4.0)
     * @param requestID
     *            internal id of the request.
     * @param store
     * @param sourceCRS
     * @param targetCRS
     * @param transformationReference
     *            a bean holding the reference to a configured (or user defined) reference or
     * @param inputData
     * @param transformableData
     *            an encapsulation of all kinds of possible transformable data.
     * @param outputFormat
     * @param dataPresentation
     *            a flag signaling the way the data was presented to the wcts. Valid values are {@link #INLINE} and
     *            {@link #MULTIPART}. If another value is given, {@link #MULTIPART} is assumed.
     * @throws IllegalArgumentException
     *             if the transformableData was <code>null</code>.
     */
    public Transform( String version, String requestID, boolean store, CoordinateSystem sourceCRS,
                      CoordinateSystem targetCRS, TransformationReference transformationReference, Manifest inputData,
                      TransformableData<?> transformableData, String outputFormat, int dataPresentation )
                            throws IllegalArgumentException {
        super( version, requestID, null );
        this.store = store;
        this.sourceCRS = sourceCRS;
        this.targetCRS = targetCRS;
        this.transformationReference = transformationReference;
        if ( sourceCRS == null && targetCRS == null && transformationReference == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "WCTS_INVALID_CONSTRUCT",
                                                                     "Source/Target, TransformationReference" ) );
        }

        this.inputData = inputData;
        this.inputDataString = null;
        this.outputFormat = outputFormat;
        if ( transformableData == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "WCTS_MISSING_ARGUMENT", transformableData ) );
        }
        this.transformableData = transformableData;
        if ( dataPresentation != INLINE && dataPresentation != MULTIPART ) {
            dataPresentation = MULTIPART;
        }
        this.dataPresentation = dataPresentation;
    }

    /**
     * @return true if the transformed data should be stored.
     */
    public final boolean mustStore() {
        return store;
    }

    /**
     * @return the sourceCRS.
     */
    public final CoordinateSystem getSourceCRS() {
        return sourceCRS;
    }

    /**
     * @return the targetCRS.
     */
    public final CoordinateSystem getTargetCRS() {
        return targetCRS;
    }

    /**
     * @return the inputData, may be <code>null</code>, if so, check {@link #getInputDataString()} to see if the
     *         request defined a location by kvp.
     */
    public final Manifest getInputData() {
        return inputData;
    }

    /**
     * @return the inputDataString, supplied by kvp. Maybe <code>null</code>, if so check {@link #getInputData()} to
     *         see if an xml encoded inputdata object was supplied.
     */
    public final String getInputDataString() {
        return inputDataString;
    }

    /**
     * @return the outputFormat.
     */
    public final String getOutputFormat() {
        return outputFormat;
    }

    /**
     * Create a {@link Transform}-request by extracting the values from the map, and calling the constructor with these
     * values.
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
    public static Transform create( String requestID, Map<String, String> map )
                            throws OGCWebServiceException {
        if ( map == null || map.size() == 0 ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_REQUESTMAP_NULL" ),
                                              ExceptionCode.MISSINGPARAMETERVALUE );
        }
        String service = map.get( "SERVICE" );
        if ( service == null || !"WCTS".equals( service ) ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_NO_SERVICE_KVP", service ),
                                              ExceptionCode.MISSINGPARAMETERVALUE );
        }
        String request = map.get( "REQUEST" );
        if ( request == null || !"Transform".equalsIgnoreCase( request ) ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_NO_REQUEST_KVP", "Transform" ),
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
        TransformationReference transformationReference = null;
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
                transformationReference = new TransformationReference( transformation );
                //
                // throw new OGCWebServiceException(
                // Messages.getMessage( "WCTS_OPERATION_NOT_SUPPORTED",
                // "defining of transformations (Transformation key)" ),
                // ExceptionCode.OPERATIONNOTSUPPORTED );
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

        int dataRepresentation = MULTIPART;
        String inputData = map.get( "INPUTDATA" );
        if ( inputData == null || "".equals( inputData ) ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_MISSING_MANDATORY_KEY_KVP", "inputData" ),
                                              ExceptionCode.MISSINGPARAMETERVALUE );
        }
        TransformableData<?> theData = FeatureCollectionData.parseFeatureCollection( inputData );
        if ( theData == null ) {
            dataRepresentation = INLINE;
            LOG.logDebug( "Could not generate a feature collection, trying GeometryData" );
            theData = GeometryData.parseGeometryData( sourceCRS, inputData );
            if ( theData == null ) {
                LOG.logDebug( "Could not generate a feature collection nor a geometry, trying SimpleData" );
                String cs = map.get( "CS" );
                if ( cs == null || "".equals( cs ) ) {
                    LOG.logDebug( "CS is set to a space ' '." );
                    cs = " ";
                }
                String ts = map.get( "TS" );
                if ( ts == null || "".equals( ts ) ) {
                    LOG.logDebug( "TS is set to comma ','." );
                    ts = ",";
                }
                String ds = map.get( "DS" );
                if ( ds == null || "".equals( ds ) ) {
                    LOG.logDebug( "DS is set to point '.'." );
                    ds = ".";
                }
                List<Point3d> points = SimpleData.parseData( inputData, sourceCRS.getDimension(), cs, ts, ds );
                if ( points.size() > 0 ) {
                    theData = new SimpleData( points, cs, ts );
                } else {
                    LOG.logError( "Didn't find any parsable data, this transform can not be handled." );
                    throw new OGCWebServiceException( Messages.getMessage( "WCTS_TRANSFORM_MISSING_DATA" ),
                                                      WCTSExceptionCode.NO_INPUT_DATA );
                }
            }
        }

        String interPolationType = map.get( "INTERPOLATIONTYPE" );
        if ( !( interPolationType == null || "".equals( interPolationType.trim() ) ) ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_OPERATION_NOT_SUPPORTED",
                                                                   "InterpolationType (key)" ),
                                              ExceptionCode.OPERATIONNOTSUPPORTED );
        }

        String outputFormat = map.get( "OUTPUTFORMAT" );

        String st = map.get( "STORE" );
        boolean store = false;

        if ( st != null && !"".equals( st.trim() ) ) {
            store = "true".equalsIgnoreCase( st ) || "yes".equalsIgnoreCase( st ) || "1".equalsIgnoreCase( st );
        }
        return new Transform( version, requestID, store, sourceCRS, targetCRS, transformationReference, null, theData,
                              outputFormat, dataRepresentation );
    }

    /**
     * @return the transformableData, as an encapsulation of a
     */
    public final TransformableData<?> getTransformableData() {
        return transformableData;
    }

    /**
     * @param requestIP
     *            or <code>null</code> if not known.
     */
    public void setRequestIP( String requestIP ) {
        if ( requestIP != null && !"".equals( requestIP.trim() ) ) {
            this.requestIP = requestIP;
        }

    }

    /**
     * @return the requestIP or 'Unknown IP' if the ip-address of the request was not set/known.
     */
    public final String getRequestIP() {
        return requestIP;
    }

    /**
     * @return true if the logging should be extensive.
     */
    public final boolean extensiveLogging() {
        return extensiveLogging;
    }

    /**
     * @return a flag signaling the way the data was presented to the wcts. Valid values are {@link #INLINE} and
     *         {@link #MULTIPART}.
     */
    public final int getDataPresentation() {
        return dataPresentation;
    }

    /**
     * @return the transformationReference which references a transformation, may be <code>null</code>
     */
    public final TransformationReference getTransformationReference() {
        return transformationReference;
    }

}
