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

package org.deegree.enterprise.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.deegree.enterprise.ServiceException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wcts.WCTService;
import org.deegree.ogcwebservices.wcts.WCTServiceFactory;
import org.deegree.ogcwebservices.wcts.XMLFactory;
import org.deegree.ogcwebservices.wcts.operation.GetResourceByID;
import org.deegree.ogcwebservices.wcts.operation.GetTransformation;
import org.deegree.ogcwebservices.wcts.operation.IsTransformable;
import org.deegree.ogcwebservices.wcts.operation.Transform;
import org.deegree.ogcwebservices.wcts.operation.TransformResponse;
import org.deegree.ogcwebservices.wcts.operation.WCTSGetCapabilities;

/**
 * The <code>WCTSHandler</code> is the interface between the {@link WCTService} and the {@link OGCServletController}.
 * <p>
 * Currently it is able to understand following operations:
 * <ul>
 * <li>WCTSGetCapabilities</li>
 * <li>GetResourceByID --limited</li>
 * <li>IsTransformable</li>
 * <li>Transform</li>
 * </ul>
 * </p>
 * <p>
 * Not supported is the the {@link GetTransformation} - Operation which --in our humble opinion-- is just another
 * GetResoureceByID.
 * </p>
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class WCTSHandler extends AbstractOWServiceHandler {

    private static ILogger LOG = LoggerFactory.getLogger( WCTSHandler.class );

    private String ip = "UnKnown IP";

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.enterprise.servlet.ServiceDispatcher#perform(org.deegree.ogcwebservices.OGCWebServiceRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public void perform( OGCWebServiceRequest request, HttpServletResponse response )
                            throws ServiceException, OGCWebServiceException {
        LOG.logDebug( "Performing request: " + request.toString() );

        try {
            WCTService service = WCTServiceFactory.createServiceInstance();
            if ( request instanceof WCTSGetCapabilities ) {
                performGetCapabilities( service, (WCTSGetCapabilities) request, response );
            } else if ( request instanceof GetResourceByID ) {
                performGetResourceByID( service, (GetResourceByID) request, response );
            } else if ( request instanceof IsTransformable ) {
                performIsTransformable( service, (IsTransformable) request, response );
            } else if ( request instanceof Transform ) {
                performTransform( service, (Transform) request, response );
            } else if ( request instanceof GetTransformation ) {
                performGetTransformation( service, (GetTransformation) request, response );
            } else {
                LOG.logInfo( "Incoming request is not a WCTS request." );
                sendException( response, new OGCWebServiceException( "Given request is not known to the WCTS",
                                                                     ExceptionCode.OPERATIONNOTSUPPORTED ) );
            }
        } catch ( OGCWebServiceException e ) {
            LOG.logInfo( "Following error occurred while performing WCTS request (from ip=" + ip + "): "
                         + e.getMessage(), e );
            sendException( response, e );
        } catch ( Exception e ) {
            LOG.logError( "Following fatal error occurred while performing WCTS request: " + e.getMessage(), e );
            sendException( response, new OGCWebServiceException( getClass().getName(), e.getMessage() ) );
        }
    }

    /**
     * @param requestIP
     */
    public void setIP( String requestIP ) {
        if ( requestIP != null && !"".equals( requestIP ) ) {
            ip = requestIP;
        }
    }

    /**
     * @param service
     *            to do the handling.
     * @param request
     *            to handle.
     * @param response
     *            to write to.
     * @throws OGCWebServiceException
     *             if for some reason the request can not be dealt with.
     */
    private void performGetTransformation( WCTService service, GetTransformation request, HttpServletResponse response )
                            throws OGCWebServiceException {
        XMLFragment serviceResponse = (XMLFragment) service.doService( request );
        outputResponse( response, serviceResponse, null );
    }

    /**
     * @param service
     *            to do the handling.
     * @param request
     *            to handle.
     * @param response
     *            to write to.
     * @throws OGCWebServiceException
     *             if for some reason the request can not be dealt with.
     */
    private void performTransform( WCTService service, Transform request, HttpServletResponse response )
                            throws OGCWebServiceException {
        request.setRequestIP( ip );
        TransformResponse serviceResponse = (TransformResponse) service.doService( request );
        LOG.logDebug( "Transform response: " + serviceResponse );
        XMLFragment resultDocument = XMLFactory.createResponse( serviceResponse,
                                                                service.getDeegreeParams().useDeegreeTransformType() );
        if ( LOG.isDebug() ) {
            LOG.logDebugXMLFile( "transform_response", resultDocument );
        }
        outputResponse( response, resultDocument, request.getOutputFormat() );

    }

    /**
     * @param service
     *            to do the handling.
     * @param request
     *            to handle.
     * @param response
     *            to write to.
     * @throws OGCWebServiceException
     *             if for some reason the request can not be dealt with.
     */
    private void performIsTransformable( WCTService service, IsTransformable request, HttpServletResponse response )
                            throws OGCWebServiceException {
        XMLFragment serviceResponse = (XMLFragment) service.doService( request );
        outputResponse( response, serviceResponse, null );
    }

    /**
     * @param service
     *            to do the handling.
     * @param request
     *            to handle.
     * @param response
     *            to write to.
     * @throws OGCWebServiceException
     *             if for some reason the request can not be dealt with.
     */
    private void performGetResourceByID( WCTService service, GetResourceByID request, HttpServletResponse response )
                            throws OGCWebServiceException {
        XMLFragment serviceResponse = (XMLFragment) service.doService( request );
        outputResponse( response, serviceResponse, request.getOutputFormat() );
    }

    /**
     * @param service
     *            to do the handling.
     * @param request
     *            to handle.
     * @param response
     *            to write to.
     * @throws OGCWebServiceException
     *             if for some reason the request can not be dealt with.
     */
    private void performGetCapabilities( WCTService service, WCTSGetCapabilities request, HttpServletResponse response )
                            throws OGCWebServiceException {
        XMLFragment serviceResponse = (XMLFragment) service.doService( request );
        List<String> acceptedFormats = request.getAcceptedFormats();
        String contentType = "text/xml";
        for ( String format : acceptedFormats ) {
            if ( format != null ) {
                String tmp = format.trim().toLowerCase();
                if ( tmp.contains( "xml" ) || tmp.contains( "gml" ) ) {
                    contentType = tmp;
                    break;
                }
            }
        }
        outputResponse( response, serviceResponse, contentType );

    }

    /**
     * Outputs the given result to the servlet response object and sets the contentType to the given contentType.
     *
     * @param response
     *            to output to.
     * @param result
     *            to output.
     * @param contentType
     *            to set.
     */
    private void outputResponse( HttpServletResponse response, XMLFragment result, String contentType ) {
        if ( result != null ) {
            if ( contentType == null || "".equals( contentType.trim() ) ) {
                contentType = "text/xml;";
            }
            response.setContentType( contentType );
            try {
                result.prettyPrint( response.getOutputStream() );
            } catch ( TransformerException e ) {
                LOG.logError( e.getMessage(), e );
            } catch ( IOException e ) {
                LOG.logError( e.getMessage(), e );
            }
        } else {
            sendException( response, new OGCWebServiceException( Messages.getMessage( "WCTS_ILLEGAL_STATE" ),
                                                                 ExceptionCode.NOAPPLICABLECODE ) );
        }

    }

}
