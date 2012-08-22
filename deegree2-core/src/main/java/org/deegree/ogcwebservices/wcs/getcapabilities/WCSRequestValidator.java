// $HeadURL:  
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH
 and 
   grit GmbH
   http://www.grit.de

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
package org.deegree.ogcwebservices.wcs.getcapabilities;

import java.net.URI;
import java.net.URL;

import org.deegree.crs.exceptions.CRSException;
import org.deegree.datatypes.Code;
import org.deegree.datatypes.CodeList;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Surface;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.CurrentUpdateSequenceException;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.InvalidUpdateSequenceException;
import org.deegree.ogcwebservices.LonLatEnvelope;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.SupportedFormats;
import org.deegree.ogcwebservices.wcs.CoverageOfferingBrief;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageDescription;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;
import org.deegree.ogcwebservices.wcs.describecoverage.DescribeCoverage;
import org.deegree.ogcwebservices.wcs.getcoverage.GetCoverage;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class WCSRequestValidator {

    private static final ILogger LOG = LoggerFactory.getLogger( WCSRequestValidator.class );

    /**
     * validates the passed <tt>AbstractOGCWebServiceRequest</tt> which must be a request that is known by a WCS against
     * the passed <tt>WCSCapabilities</tt>
     * 
     * @param capabilities
     * @param request
     * @throws CurrentUpdateSequenceException
     * @throws InvalidUpdateSequenceException
     * @throws OGCWebServiceException
     */
    public static void validate( WCSCapabilities capabilities, OGCWebServiceRequest request )
                            throws CurrentUpdateSequenceException, InvalidUpdateSequenceException,
                            OGCWebServiceException {
        // schmitz: since we only support one version, we can actually just ignore
        // the attribute (at least for GetCapabilities requests). I do not think
        // it does any harm to remove it completely.

        // if ( !request.getVersion().equals(capabilities.getVersion() )) {
        // throw new InvalidParameterValueException(request.getVersion() + " is not " +
        // "a valid version for requesting this WCS");
        // }

        if ( request instanceof WCSGetCapabilities ) {
            validate( capabilities, (WCSGetCapabilities) request );
        } else if ( request instanceof GetCoverage ) {
            validate( capabilities, (GetCoverage) request );
        } else if ( request instanceof DescribeCoverage ) {
            validate( capabilities, (DescribeCoverage) request );
        } else {
            throw new OGCWebServiceException( "Invalid request type: " + request );
        }
    }

    /**
     * validates the passed <tt>WCSGetCapabilities</tt> against the passed <tt>WCSCapabilities</tt>
     * 
     * @param capabilities
     * @param request
     * @throws CurrentUpdateSequenceException
     * @throws InvalidUpdateSequenceException
     */
    private static void validate( WCSCapabilities capabilities, WCSGetCapabilities request )
                            throws CurrentUpdateSequenceException, InvalidUpdateSequenceException {
        String rUp = request.getUpdateSequence();
        String cUp = capabilities.getUpdateSequence();

        if ( ( rUp != null ) && ( cUp != null ) && ( rUp.compareTo( cUp ) == 0 ) ) {
            ExceptionCode code = ExceptionCode.CURRENT_UPDATE_SEQUENCE;
            throw new CurrentUpdateSequenceException( "WCS GetCapabilities", "request update sequence: " + rUp
                                                                             + "is equal to capabilities"
                                                                             + " update sequence " + cUp, code );
        }

        if ( ( rUp != null ) && ( cUp != null ) && ( rUp.compareTo( cUp ) > 0 ) ) {
            ExceptionCode code = ExceptionCode.INVALID_UPDATESEQUENCE;
            throw new InvalidUpdateSequenceException( "WCS GetCapabilities", "request update sequence: " + rUp
                                                                             + " is higher then the "
                                                                             + "capabilities update sequence " + cUp,
                                                      code );
        }
    }

    /**
     * validates the passed <tt>DescribeCoverage</tt> against the passed <tt>WCSCapabilities</tt>
     * 
     * @param capabilities
     * @param request
     * @throws InvalidParameterValueException
     */
    private static void validate( WCSCapabilities capabilities, DescribeCoverage request )
                            throws InvalidParameterValueException {
        String[] coverages = request.getCoverages();
        if ( coverages != null ) {
            ContentMetadata cm = capabilities.getContentMetadata();
            for ( int i = 0; i < coverages.length; i++ ) {
                if ( cm.getCoverageOfferingBrief( coverages[i] ) == null ) {
                    throw new InvalidParameterValueException( "Coverage: " + coverages[i] + "is not known by the WCS" );
                }
            }
        }
    }

    /**
     * validates the passed <tt>GetCoverage</tt> against the passed <tt>WCSCapabilities</tt>
     * 
     * @param capabilities
     * @param request
     * @throws InvalidParameterValueException
     */
    private static void validate( WCSCapabilities capabilities, GetCoverage request )
                            throws InvalidParameterValueException {
        String coverage = request.getSourceCoverage();
        ContentMetadata cm = capabilities.getContentMetadata();
        // is coverage known by the WCS?
        CoverageOfferingBrief cob = cm.getCoverageOfferingBrief( coverage );
        if ( cob == null ) {
            throw new InvalidParameterValueException( "Coverage: " + coverage + " is not known by the WCS" );
        }

        URL url = cob.getConfiguration();
        CoverageDescription cd = null;
        try {
            cd = CoverageDescription.createCoverageDescription( url );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage() );
        }
        CoverageOffering co = cd.getCoverageOffering( coverage );
        if ( co == null ) {
            throw new InvalidParameterValueException( "no coverage descrition " + "available for requested coverage: "
                                                      + coverage );
        }
        // validate requested format
        String format = request.getOutput().getFormat().getCode();
        SupportedFormats sf = co.getSupportedFormats();
        CodeList[] codeList = sf.getFormats();
        if ( !validate( codeList, null, format ) ) {
            throw new InvalidParameterValueException( "requested format: " + format
                                                      + " is not known by the WCS for coverage:" + coverage );
        }
        // validate requested response CRS
        String crs = request.getOutput().getCrs().getCode();
        URI codeSpace = request.getOutput().getCrs().getCodeSpace();
        String space = null;
        if ( codeSpace != null ) {
            space = codeSpace.toString();
        }

        CodeList[] rrcrs = co.getSupportedCRSs().getRequestResponseSRSs();
        CodeList[] rescrs = co.getSupportedCRSs().getResponseSRSs();
        if ( !validate( rrcrs, space, crs ) && !validate( rescrs, space, crs ) ) {
            throw new InvalidParameterValueException( "requested response CRS: " + crs + " is not known by the WCS "
                                                      + "for coverage:" + coverage );
        }
        // validate requested CRS
        crs = request.getDomainSubset().getRequestSRS().getCode();
        codeSpace = request.getDomainSubset().getRequestSRS().getCodeSpace();
        if ( codeSpace != null ) {
            space = codeSpace.toString();
        }
        CodeList[] reqcrs = co.getSupportedCRSs().getRequestSRSs();

        if ( !validate( rrcrs, space, crs ) && !validate( reqcrs, space, crs ) ) {
            throw new InvalidParameterValueException( "requested request CRS: " + crs
                                                      + " is not known by the WCS for coverage:" + coverage );
        }
        // validate requested envelope
        Envelope envelope = request.getDomainSubset().getSpatialSubset().getEnvelope();
        LonLatEnvelope llEnv = cob.getLonLatEnvelope();
        Envelope[] domEnvs = co.getDomainSet().getSpatialDomain().getEnvelops();

        try {
            if ( !intersects( envelope, request.getDomainSubset().getRequestSRS(), domEnvs, llEnv ) ) {
                throw new InvalidParameterValueException( "requested BBOX: doesn't intersect "
                                                          + " the area of the requested coverage: " + coverage );
            }
        } catch ( UnknownCRSException e ) {
            throw new InvalidParameterValueException( e );
        }

    }

    /**
     * @return true if the passed <tt>CodeList</tt> s contains the also passed codeSpace-value combination. Otherwise
     *         false will be returned
     * 
     * @param codeList
     * @param codeSpace
     * @param value
     */
    private static boolean validate( CodeList[] codeList, String codeSpace, String value ) {
        for ( int i = 0; i < codeList.length; i++ ) {
            if ( codeList[i].validate( codeSpace, value ) ) {
                return true;
            }
        }
        return false;
    }

    private static boolean intersects( Envelope envelope, Code reqCRS, Envelope[] envs, LonLatEnvelope llEnv )
                            throws UnknownCRSException {

        boolean res = false;
        String reqCRSCode = reqCRS.getCode();

        try {
            if ( envs == null || envs.length == 0 ) {
                Envelope latlonEnv = GeometryFactory.createEnvelope( llEnv.getMin().getX(), llEnv.getMin().getY(),
                                                                     llEnv.getMax().getX(), llEnv.getMax().getY(),
                                                                     CRSFactory.create( "EPSG:4326" ) );

                if ( !"EPSG:4326".equals( reqCRSCode ) ) {
                    res = intersects( envelope, reqCRSCode, latlonEnv, "EPSG:4326" );
                } else {
                    res = envelope.intersects( latlonEnv );
                }
            } else {
                for ( int i = 0; i < envs.length && !res; i++ ) {
                    if ( intersects( envelope, reqCRSCode, envs[i], envs[i].getCoordinateSystem().getPrefixedName() ) ) {
                        res = true;
                        break;
                    }
                }
            }
        } catch ( GeometryException ex ) {
            LOG.logWarning( "intersection test; translation into surface failed", ex );
        } catch ( CRSException ex ) {
            LOG.logWarning( "intersection test; transformation of reqeust envelope/valid area impossible", ex );
        } catch ( CRSTransformationException ex ) {
            LOG.logWarning( "intersection test; transformation of reqeust envelope/valid area failed", ex );
        }
        return res;
    }

    private static boolean intersects( Envelope requestEnv, String requestCrs, Envelope regionEnv, String regionCrs )
                            throws CRSException, GeometryException, CRSTransformationException, UnknownCRSException {
        Surface request = GeometryFactory.createSurface( requestEnv, CRSFactory.create( requestCrs ) );
        Surface region = GeometryFactory.createSurface( regionEnv, CRSFactory.create( regionCrs ) );

        if ( !requestCrs.equalsIgnoreCase( regionCrs ) ) {
            GeoTransformer gt = new GeoTransformer( requestCrs );
            region = (Surface) gt.transform( region );
        }

        return request.intersects( region );
    }

}