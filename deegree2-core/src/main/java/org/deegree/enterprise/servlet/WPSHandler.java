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

import javax.servlet.http.HttpServletResponse;

import org.deegree.enterprise.ServiceException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.GMLFeatureAdapter;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wps.WPService;
import org.deegree.ogcwebservices.wps.WPServiceFactory;
import org.deegree.ogcwebservices.wps.XMLFactory;
import org.deegree.ogcwebservices.wps.capabilities.WPSCapabilities;
import org.deegree.ogcwebservices.wps.capabilities.WPSCapabilitiesDocument;
import org.deegree.ogcwebservices.wps.configuration.WPSConfiguration;
import org.deegree.ogcwebservices.wps.describeprocess.ProcessDescriptions;
import org.deegree.ogcwebservices.wps.describeprocess.ProcessDescriptionsDocument;
import org.deegree.ogcwebservices.wps.execute.ComplexValue;
import org.deegree.ogcwebservices.wps.execute.ExecuteResponse;
import org.deegree.ogcwebservices.wps.execute.ExecuteResponseDocument;

/**
 * WPSHandler.java
 *
 * Created on 08.03.2006. 17:01:31h
 *
 * @author <a href="mailto:kiehle@giub.uni-bonn.de">Christian Kiehle</a>
 * @author <a href="mailto:che@wupperverband.de">Christian Heier</a>
 *
 * @version 1.0.
 *
 * @since 2.0
 */
public class WPSHandler extends AbstractOWServiceHandler implements ServiceDispatcher {

    private static final ILogger LOG = LoggerFactory.getLogger( WPSHandler.class );

    /**
     *
     */
    public void perform( OGCWebServiceRequest request, HttpServletResponse httpServletResponse )
                            throws ServiceException, OGCWebServiceException {

        WPService service = WPServiceFactory.getInstance();
        @SuppressWarnings("unused")
        WPSConfiguration config = (WPSConfiguration) service.getCapabilities();
        Object response = service.doService( request );
        if ( response instanceof WPSCapabilities ) {
            sendGetCapabilitiesResponse( httpServletResponse, (WPSCapabilities) response );
        } else if ( response instanceof ProcessDescriptions ) {
            sendDescribeProcessResponse( httpServletResponse, (ProcessDescriptions) response );
        } else if ( response instanceof ExecuteResponse ) {
            sendExecuteResponse( httpServletResponse, (ExecuteResponse) response );
        }

    }

    /**
     * Sends the response to a GetCapabilities request to the client.
     *
     * @param httpResponse
     * @param capabilities
     */
    private void sendGetCapabilitiesResponse( HttpServletResponse httpResponse, WPSCapabilities capabilities ) {
        try {
            httpResponse.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
            WPSCapabilitiesDocument document = XMLFactory.export( capabilities );
            document.write( httpResponse.getOutputStream() );
        } catch ( IOException e ) {
            LOG.logError( "Error sending GetCapabilities response.", e );
        }
    }

    /**
     * Sends the response to a DescribeProcess request to the client.
     *
     * @param httpResponse
     * @param processDescriptions
     */
    private void sendDescribeProcessResponse( HttpServletResponse httpResponse, ProcessDescriptions processDescriptions ) {
        try {
            httpResponse.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
            ProcessDescriptionsDocument document = XMLFactory.export( processDescriptions );
            document.write( httpResponse.getOutputStream() );
        } catch ( IOException e ) {
            LOG.logError( "Error sending DescribeProcess response.", e );
        }
    }

    /**
     * Sends the response to an Execute request to the client.
     *
     * @param httpResponse
     * @param executeResponse
     * @throws OGCWebServiceException
     *             if an exception occurs which can be propagated to the client
     */
    private void sendExecuteResponse( HttpServletResponse httpResponse, ExecuteResponse executeResponse )
                            throws OGCWebServiceException {

        /*
         * @see OGC 05-007r4 Subclauses 10.3.1 and 10.3.2
         * @see OGC 05-007r4 Tables 43, 44
         * @see OGC 05-007r4 Table 27: If the storeparameter is false, process execution was
         *      successful, there is only one output, and that output has a ComplexValue, then this
         *      ComplexValue shall be returned to the client outside of any ExecuteResponse
         *      document.
         */
        String processSucceeded = executeResponse.getStatus().getProcessSucceeded();

        if ( null != processSucceeded && executeResponse.isDirectResponse() ) {

            ComplexValue complexValue = executeResponse.getProcessOutputs().getOutputs().get( 0 ).getComplexValue();

            if ( null != complexValue ) {
                sendDirectResponse( httpResponse, complexValue );
            }

        } else {
            try {
                httpResponse.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
                ExecuteResponseDocument document = XMLFactory.export( executeResponse );
                document.write( httpResponse.getOutputStream() );
            } catch ( IOException e ) {
                LOG.logError( "error sending execute response.", e );
            }
        }
    }

    /**
     * Writes the passed <code>ComplexValue</code> to the <code>HTTPServletResponse</code>
     *
     * @param httpResponse
     * @param complexValue
     */
    private static void sendDirectResponse( HttpServletResponse httpResponse, ComplexValue complexValue )
                            throws OGCWebServiceException {

        Object content = complexValue.getContent();

        if ( content instanceof FeatureCollection ) {

            LOG.logInfo( "content is instance of featurecollection" );

            FeatureCollection fc = (FeatureCollection) content;

            GMLFeatureAdapter gmlFeatureAdapter = new GMLFeatureAdapter();

            try {
                gmlFeatureAdapter.export( fc, httpResponse.getOutputStream() );
            } catch ( Exception e ) {
                String msg = "Error sending direct execute response.";
                LOG.logError( msg, e );
                throw new OGCWebServiceException( "", msg, ExceptionCode.NOAPPLICABLECODE );
            }
        } else {
            // TODO implement direct output methods for complexvalue types other
            // than
            // featurecollection
        }
    }
}
