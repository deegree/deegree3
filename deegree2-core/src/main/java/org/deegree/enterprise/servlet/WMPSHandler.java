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

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.NetWorker;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.DOMPrinter;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.OGCWebServiceResponse;
import org.deegree.ogcwebservices.wmps.WMPService;
import org.deegree.ogcwebservices.wmps.WMPServiceFactory;
import org.deegree.ogcwebservices.wmps.XMLFactory;
import org.deegree.ogcwebservices.wmps.capabilities.WMPSCapabilities;
import org.deegree.ogcwebservices.wmps.capabilities.WMPSCapabilitiesDocument;
import org.deegree.ogcwebservices.wmps.configuration.WMPSConfiguration;
import org.deegree.ogcwebservices.wmps.operation.DescribeTemplateResponseDocument;
import org.deegree.ogcwebservices.wmps.operation.GetAvailableTemplatesResponseDocument;
import org.deegree.ogcwebservices.wmps.operation.PrintMapResponseDocument;
import org.deegree.ogcwebservices.wmps.operation.WMPSGetCapabilitiesResult;
import org.deegree.ogcwebservices.wms.InvalidFormatException;

/**
 * 
 * Web servlet client for WMPS.
 * 
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version 2.0, $Revision$, $Date$
 * 
 * @since 2.0
 */

public class WMPSHandler extends AbstractOWServiceHandler {

    private static ILogger LOG = LoggerFactory.getLogger( WMPSHandler.class );

    private HttpServletResponse resp = null;

    private WMPSConfiguration configuration = null;

    /**
     * Create an empty WMPSHandler
     */
    WMPSHandler() {
        LOG.logDebug( "New WMPSHandler instance created: " + this.getClass().getName() );
    }

    /**
     * performs the passed OGCWebServiceRequest by accessing service from the pool and passing the request to it
     */
    public void perform( OGCWebServiceRequest request, HttpServletResponse response )
                            throws OGCWebServiceException {

        resp = response;
        OGCWebService service;
        try {
            service = WMPServiceFactory.getService();
        } catch ( Exception e ) {
            throw new OGCWebServiceException( "Error performing the WMPService(s)." );
        }
        configuration = (WMPSConfiguration) ( (WMPService) service ).getCapabilities();
        // if ( service == null ) {
        // OGCWebServiceException exce = new OGCWebServiceException( "WMPS:WMPS",
        // "could not access a WMPService instance" );
        // sendException( response, exce );
        // return;
        // }
        Object o = service.doService( request );
        handleResponse( o );
    }

    /**
     * 
     * 
     * @param result
     */
    private void handleResponse( Object result ) {

        try {
            OGCWebServiceResponse response = (OGCWebServiceResponse) result;
            if ( response.getException() != null ) {
                // handle the case that an exception occured during the
                // request performance
                OGCWebServiceException exce = response.getException();
                sendException( resp, exce );
            } else {
                if ( response instanceof WMPSGetCapabilitiesResult ) {
                    handleGetCapabilitiesResponse( (WMPSGetCapabilitiesResult) response );
                } else if ( response instanceof PrintMapResponseDocument ) {
                    handlePrintMapResponse( (PrintMapResponseDocument) response );
                } else if ( response instanceof GetAvailableTemplatesResponseDocument ) {
                    handleGetAvailableTemplatesResponse( (GetAvailableTemplatesResponseDocument) response );
                } else if ( response instanceof DescribeTemplateResponseDocument ) {
                    handleDescribeTemplateResponse( (DescribeTemplateResponseDocument) response );
                }
            }
        } catch ( InvalidFormatException ife ) {
            LOG.logError( ife.getMessage(), ife );
            OGCWebServiceException exce = new OGCWebServiceException( "InvalidFormat", ife.getMessage() );
            sendException( resp, exce );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            OGCWebServiceException exce = new OGCWebServiceException( "WMPS:write", e.getMessage() );
            sendException( resp, exce );
        }

    }

    /**
     * @param response
     */
    private void handleDescribeTemplateResponse( DescribeTemplateResponseDocument response ) {
        resp.setContentType( "application/vnd.ogc.wms_xml" );

        XMLFragment xml = new XMLFragment( response.getRootElement() );
        try {
            PrintWriter pw = resp.getWriter();
            xml.prettyPrint( pw );
            pw.close();
        } catch ( Exception e ) {
            LOG.logError( "-", e );
        }        
    }

    /**
     * @param response
     */
    private void handleGetAvailableTemplatesResponse( GetAvailableTemplatesResponseDocument response ) {
        resp.setContentType( "application/vnd.ogc.wms_xml" );

        XMLFragment xml = new XMLFragment( response.getRootElement() );
        try {
            PrintWriter pw = resp.getWriter();
            xml.prettyPrint( pw );
            pw.close();
        } catch ( Exception e ) {
            LOG.logError( "-", e );
        }
    }

    /**
     * handles the response to a get capabilities request
     * 
     * @param response
     */
    private void handleGetCapabilitiesResponse( WMPSGetCapabilitiesResult response )
                            throws Exception {

        resp.setContentType( "application/vnd.ogc.wms_xml" );
        WMPSCapabilities capa = response.getCapabilities();
        WMPSCapabilitiesDocument doc = XMLFactory.export( capa );

        // XMLFragment frag = doc.transform( url.openStream() , XMLFragment.DEFAULT_URL );
        String xml = DOMPrinter.nodeToString( doc.getRootElement(), "" );

        String dtd = NetWorker.url2String( configuration.getDeegreeParams().getDTDLocation() );
        StringBuffer sb = new StringBuffer();
        sb.append( "<!DOCTYPE WMT_PS_Capabilities SYSTEM " );
        sb.append( "'" + dtd + "' \n" );
        sb.append( "[\n<!ELEMENT VendorSpecificCapabilities EMPTY>\n]>" );

        xml = StringTools.replace( xml, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                                   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n" + sb.toString(), false );
        xml = StringTools.replace( xml, "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", "", false );

        try {
            PrintWriter pw = resp.getWriter();
            pw.print( xml );
            pw.close();
        } catch ( Exception e ) {
            LOG.logError( "-", e );
        }

    }

    /**
     * handles the response to a print map request
     * 
     * @param response
     */
    private void handlePrintMapResponse( PrintMapResponseDocument response ) {

        resp.setContentType( "application/vnd.ogc.wms_xml" );

        XMLFragment xml = new XMLFragment( response.getRootElement() );
        try {
            PrintWriter pw = resp.getWriter();
            xml.prettyPrint( pw );
            pw.close();
        } catch ( Exception e ) {
            LOG.logError( "-", e );
        }

    }

}
