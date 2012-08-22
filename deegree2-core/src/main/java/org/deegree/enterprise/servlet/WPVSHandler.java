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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.deegree.enterprise.ServiceException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.MimeTypeMapper;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.DOMPrinter;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wpvs.WPVService;
import org.deegree.ogcwebservices.wpvs.WPVServiceFactory;
import org.deegree.ogcwebservices.wpvs.XMLFactory;
import org.deegree.ogcwebservices.wpvs.capabilities.WPVSCapabilities;
import org.deegree.ogcwebservices.wpvs.capabilities.WPVSCapabilitiesDocument;
import org.deegree.ogcwebservices.wpvs.configuration.WPVSConfiguration;
import org.deegree.ogcwebservices.wpvs.operation.Get3DFeatureInfoResponse;
import org.deegree.ogcwebservices.wpvs.operation.GetView;
import org.deegree.ogcwebservices.wpvs.operation.GetViewResponse;
import org.w3c.dom.Node;

/**
 * Handler for the Web Perspective View Service (WPVS).
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 * 
 * @version 2.0, $Revision$, $Date$
 * 
 * @since 2.0
 */
public class WPVSHandler extends AbstractOWServiceHandler {

    private static ILogger LOG = LoggerFactory.getLogger( WPVSHandler.class );

    /**
     * Performs the passed OGCWebServiceRequest by accessing service from the pool and passing the request to it
     * 
     * @param request
     *            the incoming web service request
     * @param httpResponse
     *            the outgoing web serivce response
     * @throws ServiceException
     * @throws OGCWebServiceException
     */
    public void perform( OGCWebServiceRequest request, HttpServletResponse httpResponse )
                            throws ServiceException, OGCWebServiceException {

        LOG.logDebug( StringTools.concat( 200, "Performing request: ", request.toString() ) );

        OGCWebService service = WPVServiceFactory.createInstance();

        try {
            Object response = service.doService( request );
            if ( response instanceof WPVSCapabilities ) {
                sendGetCapabilitiesResponse( httpResponse, (WPVSCapabilities) response );
            } else if ( response instanceof GetViewResponse ) {
                sendGetViewResponse( httpResponse, (GetViewResponse) response );
            } else if ( response instanceof Get3DFeatureInfoResponse ) {
                sendGet3DFeatureInfoResponse( httpResponse, (Get3DFeatureInfoResponse) response );
            } else {
                String s = ( response == null ? "null response object" : response.getClass().getName() );
                // this is not really nice...because excepts get cought later on below
                throw new OGCWebServiceException( getClass().getName(),
                                                  StringTools.concat( 200, "Unknown response class: '", s, "'." ) );
            }
        } catch ( OGCWebServiceException e ) {

            LOG.logError( "Error performing WPVFS request.", e );
            if ( request instanceof GetView
                 && ( "INIMAGE".equalsIgnoreCase( ( (GetView) request ).getExceptionFormat() ) ) ) {
                sendExceptionImage( httpResponse, e, (GetView) request );

            } else {
                sendException( httpResponse, e );
            }
        }

    }

    // TODO common to WMS
    private void sendExceptionImage( HttpServletResponse httpResponse, OGCWebServiceException e, GetView request ) {

        Dimension d = request.getImageDimension();

        BufferedImage bi = new BufferedImage( d.width, d.height, BufferedImage.TYPE_INT_RGB );
        Graphics g = bi.getGraphics();
        g.setColor( Color.WHITE );
        g.fillRect( 0, 0, d.width, d.height );
        g.setColor( Color.BLUE );

        String s = e.getLocator();
        String location = s != null ? s : "Unknown";
        s = e.getMessage();
        String message = s != null ? s : "Unknown reason!";

        g.drawString( location, 5, 20 );
        g.drawString( message, 15, 50 );
        String mime = MimeTypeMapper.toMimeType( request.getOutputFormat() );
        g.dispose();
        writeImage( bi, mime, httpResponse );
    }

    // TODO common to WMS
    private void writeImage( Object output, String mime, HttpServletResponse resp ) {
        try {
            OutputStream os = null;
            resp.setContentType( mime );

            if ( mime.equalsIgnoreCase( "image/gif" ) ) {
                os = resp.getOutputStream();
                ImageUtils.saveImage( (BufferedImage) output, os, "gif", 1 );
            } else if ( mime.equalsIgnoreCase( "image/jpg" ) || mime.equalsIgnoreCase( "image/jpeg" ) ) {
                os = resp.getOutputStream();
                ImageUtils.saveImage( (BufferedImage) output, os, "jpeg", 1 );
            } else if ( mime.equalsIgnoreCase( "image/png" ) ) {
                os = resp.getOutputStream();
                ImageUtils.saveImage( (BufferedImage) output, os, "png", 1 );
            } else if ( mime.equalsIgnoreCase( "image/tif" ) || mime.equalsIgnoreCase( "image/tiff" ) ) {
                os = resp.getOutputStream();
                ImageUtils.saveImage( (BufferedImage) output, os, "tif", 1 );
            } else if ( mime.equalsIgnoreCase( "image/bmp" ) ) {
                os = resp.getOutputStream();
                ImageUtils.saveImage( (BufferedImage) output, os, "bmp", 1 );
            } else if ( mime.equalsIgnoreCase( "image/svg+xml" ) ) {
                os = resp.getOutputStream();
                PrintWriter pw = new PrintWriter( os );
                DOMPrinter.printNode( pw, (Node) output );
                pw.close();
            } else {
                resp.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
                os = resp.getOutputStream();
                OGCWebServiceException exce = new OGCWebServiceException( "WMS:writeImage",
                                                                          "unsupported image format: " + mime );
                sendExceptionReport( resp, exce );
            }

            os.close();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }
    }

    /**
     * Sends the result of a someWPVService.doService( request ) bacn to the client
     * 
     * @param httpResponse
     *            the response object used to pipe the result
     * @param getViewResponse
     *            the actua result to be sent
     */
    private void sendGetViewResponse( HttpServletResponse httpResponse, GetViewResponse getViewResponse ) {

        String mime = MimeTypeMapper.toMimeType( getViewResponse.getOutputFormat() );
        httpResponse.setContentType( mime );

        // GetView response is, for the time being, always an image
        writeImage( getViewResponse.getOutput(), httpResponse, mime );

        System.gc();
    }

    /**
     * Sends the response to a GetCapabilities request to the client.
     * 
     * @param httpResponse
     * @param capabilities
     * @throws OGCWebServiceException
     *             if an exception occurs which can be propagated to the client
     */
    private void sendGetCapabilitiesResponse( HttpServletResponse httpResponse, WPVSCapabilities capabilities )
                            throws OGCWebServiceException {
        try {
            httpResponse.setContentType( "text/xml" );
            WPVSCapabilitiesDocument document = XMLFactory.export( capabilities );
            document.write( httpResponse.getOutputStream() );
        } catch ( IOException e ) {
            LOG.logError( "Error sending GetCapabilities response.", e );

            throw new OGCWebServiceException( getClass().getName(),
                                              "Error exporting capabilities for GetCapabilities response." );
        }
    }

    /**
     * Sends the response to a Get3DFeatureInfo request to the client.
     * 
     * @param httpResponse
     * @param response
     * @throws OGCWebServiceException
     *             if an exception occurs which can be propagated to the client
     */
    private void sendGet3DFeatureInfoResponse( HttpServletResponse httpResponse, Get3DFeatureInfoResponse response )
                            throws OGCWebServiceException {
        try {
            httpResponse.setContentType( "text/xml" );

            PrintWriter pw = httpResponse.getWriter();
            String s = response.get3DFeatureInfo();
            pw.print( s );
        } catch ( IOException e ) {
            LOG.logError( "Error sendingGet3DFeatureInfo response.", e );

            throw new OGCWebServiceException( getClass().getName(),
                                              "Error exporting Info for Get3DFeatureInfo response." );
        }
    }

    /**
     * Writes an output of a GetView request to the <code>httpResponse</code> using the <code>mime</code> type.
     * 
     * @param output
     *            the image to be sent back
     * @param httpResponse
     *            the response to pipe the image
     * @param mime
     *            the type of image
     */
    private void writeImage( Image output, HttpServletResponse httpResponse, String mime ) {
        try {

            OutputStream os = httpResponse.getOutputStream();
            httpResponse.setContentType( mime );

            if ( mime.equalsIgnoreCase( "image/jpg" ) || mime.equalsIgnoreCase( "image/jpeg" ) ) {

                OGCWebService service = WPVServiceFactory.createInstance();
                WPVSConfiguration config = (WPVSConfiguration) ( (WPVService) service ).getCapabilities();
                float quality = config.getDeegreeParams().getViewQuality();
                ImageUtils.saveImage( (BufferedImage) output, os, "jpeg", quality );
            } else if ( mime.equalsIgnoreCase( "image/png" ) ) {
                ImageUtils.saveImage( (BufferedImage) output, os, "png", 1 );
            } else if ( mime.equalsIgnoreCase( "image/tif" ) || mime.equalsIgnoreCase( "image/tiff" ) ) {
                ImageUtils.saveImage( (BufferedImage) output, os, "tiff", 1 );
            } else if ( mime.equalsIgnoreCase( "image/bmp" ) ) {
                ImageUtils.saveImage( (BufferedImage) output, os, "bmp", 1 );
            } else {
                httpResponse.setContentType( "text/xml" );
                os = httpResponse.getOutputStream();
                OGCWebServiceException exce = new OGCWebServiceException( "WMS:writeImage",
                                                                          "unsupported image format: " + mime );
                sendExceptionReport( httpResponse, exce );
            }

            os.close();
        } catch ( Exception e ) {
            LOG.logError( "-", e );
        }
    }
}
