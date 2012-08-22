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
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.deegree.datatypes.parameter.GeneralParameterValueIm;
import org.deegree.datatypes.parameter.OperationParameterIm;
import org.deegree.enterprise.ServiceException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.i18n.Messages;
import org.deegree.model.coverage.grid.Format;
import org.deegree.model.coverage.grid.GridCoverage;
import org.deegree.model.coverage.grid.GridCoverageExchange;
import org.deegree.model.coverage.grid.GridCoverageWriter;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.ExceptionReport;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wcs.CoverageOfferingBrief;
import org.deegree.ogcwebservices.wcs.WCService;
import org.deegree.ogcwebservices.wcs.WCServiceFactory;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageDescription;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;
import org.deegree.ogcwebservices.wcs.describecoverage.InvalidCoverageDescriptionExcpetion;
import org.deegree.ogcwebservices.wcs.getcapabilities.ContentMetadata;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSCapabilities;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSGetCapabilities;
import org.deegree.ogcwebservices.wcs.getcoverage.GetCoverage;
import org.deegree.ogcwebservices.wcs.getcoverage.ResultCoverage;
import org.deegree.ogcwebservices.wcs.getcoverage.SpatialSubset;
import org.deegree.owscommon.XMLFactory;
import org.xml.sax.SAXException;

/**
 * Dispatcher for WCService.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
class WCSHandler extends AbstractOWServiceHandler {

    private static ILogger LOG = LoggerFactory.getLogger( WCSHandler.class );

    /**
     *
     */
    WCSHandler() {
        LOG.logDebug( "New WCSHandler instance created: " + this.getClass().getName() );
    }

    /**
     *
     */
    public void perform( OGCWebServiceRequest request, HttpServletResponse response )
                            throws ServiceException, OGCWebServiceException {

        try {
            Object serviceRes = this.getService().doService( request );

            if ( serviceRes instanceof OGCWebServiceException ) {
                sendException( response, (OGCWebServiceException) serviceRes );
            } else if ( response instanceof Exception ) {
                sendException( response, (Exception) serviceRes );
            } else if ( serviceRes instanceof ResultCoverage ) {
                SpatialSubset spsu = ( (GetCoverage) request ).getDomainSubset().getSpatialSubset();
                Envelope size = (Envelope) spsu.getGrid();
                try {
                    sendCoverage( response, (ResultCoverage) serviceRes, size );
                } catch ( Exception e ) {
                    sendException( response, e );
                }
            } else if ( serviceRes instanceof WCSCapabilities ) {
                sendCapabilities( response, (WCSGetCapabilities) request, (WCSCapabilities) serviceRes );
            } else if ( serviceRes instanceof CoverageDescription ) {
                sendCoverageDescription( response, (CoverageDescription) serviceRes );
            } else {
                String s = Messages.getMessage( "WCS_UNKNOWN_RESPONSE_CLASS", serviceRes.getClass().getName() );
                throw new OGCWebServiceException( this.getClass().getName(), s );
            }
        } catch ( Exception e ) {
            sendException( response, e );
        }
    }

    /**
     * sends the passed <tt>WCSCapabilities</tt> to the calling client
     *
     * @param response
     *            <tt>HttpServletResponse</tt> for opening stream to the client
     * @param serviceRes
     *            object to send
     */
    private void sendCapabilities( HttpServletResponse response, WCSGetCapabilities owsr, WCSCapabilities serviceRes ) {
        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<?xml version=\"1.0\" encoding='" + CharsetUtils.getSystemCharset() + "'?>" );
        sb.append( "<xsl:stylesheet version=\"1.0\" " );
        sb.append( "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" " );
        sb.append( "xmlns:wcs=\"http://www.opengis.net/wcs\" " );
        sb.append( "xmlns:deegree=\"http://www.deegree.org/wcs\">" );
        sb.append( "<xsl:template match=\"wcs:WCS_Capabilities\">" + "<xsl:copy-of select=\"XXX\"/>" );
        sb.append( "</xsl:template>" + "</xsl:stylesheet>" );
        String xslt = sb.toString();
        try {
            XMLFragment doc = org.deegree.ogcwebservices.wcs.XMLFactory.export( serviceRes );
            String[] sections = owsr.getSections();

            if ( sections != null && sections.length > 0 ) {
                // filter out the requested section
                if ( sections[0].equals( "/WCS_Capabilities/Service" ) ) {
                    xslt = StringTools.replace( xslt, "XXX", "./wcs:Service", false );
                } else if ( sections[0].equals( "/WCS_Capabilities/Capability" ) ) {
                    xslt = StringTools.replace( xslt, "XXX", "./wcs:Capability", false );
                } else if ( sections[0].equals( "/WCS_Capabilities/ContentMetadata" ) ) {
                    xslt = StringTools.replace( xslt, "XXX", "./wcs:ContentMetadata", false );
                } else {
                    xslt = StringTools.replace( xslt, "XXX", ".", false );
                }
                XSLTDocument xslSheet = new XSLTDocument();
                xslSheet.load( new StringReader( xslt ), XMLFragment.DEFAULT_URL );
                doc = xslSheet.transform( doc );
            }

            response.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
            OutputStream os = response.getOutputStream();
            doc.write( os );
            os.close();
        } catch ( Exception e ) {
            LOG.logError( "ERROR: " + StringTools.stackTraceToString( e ), e );
        }
    }

    @Override
    public void sendException( HttpServletResponse response, Exception exc ) {
        OGCWebServiceException ogc;

        if ( exc instanceof OGCWebServiceException ) {
            ogc = (OGCWebServiceException) exc;
        } else {
            ogc = new OGCWebServiceException( exc.getMessage() );
        }

        ExceptionReport report = new ExceptionReport( new OGCWebServiceException[] { ogc } );

        try {
            response.setContentType( "application/vnd.ogc.se_xml" );
            XMLFragment reportDocument = XMLFactory.exportNS( report );
            OutputStream os = response.getOutputStream();
            reportDocument.write( os );
            os.close();
        } catch ( Exception e ) {
            LOG.logError( "Error sending exception report: ", e );
        }

    }

    /**
     * sends the passed <tt>CoverageDescription</tt> to the calling client
     *
     * @param response
     *            <tt>HttpServletResponse</tt> for opening stream to the client
     * @param serviceRes
     *            object to send
     */
    private void sendCoverageDescription( HttpServletResponse response, CoverageDescription serviceRes ) {
        try {
            XMLFragment doc = org.deegree.ogcwebservices.wcs.XMLFactory.export( serviceRes );
            response.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
            OutputStream os = response.getOutputStream();
            doc.write( os );
            os.close();
        } catch ( Exception e ) {
            LOG.logError( "ERROR: " + StringTools.stackTraceToString( e ), e );
        }
    }

    /**
     * writes the <tt>GridCoverage</tt> that is encapsulated within the <tt>ResultCoverage</tt>
     * into the <tt>OutputStream</tt> taken from the passed <tt>HttpServletResponse</tt>
     *
     * @param response
     *            destination for writing the result coverage
     * @param serviceRes
     *            response to a GetCoverage request
     * @param size
     *            desired size of the GridCoverage
     * @throws IOException
     * @throws ServiceException
     * @throws SAXException
     * @throws InvalidCoverageDescriptionExcpetion
     */
    private void sendCoverage( HttpServletResponse response, ResultCoverage serviceRes, Envelope size )
                            throws IOException, InvalidCoverageDescriptionExcpetion, SAXException, ServiceException {
        OutputStream os = response.getOutputStream();
        Format format = new Format( serviceRes.getDesiredOutputFormat() );
        String frmt = format.getName();
        if ( frmt.equalsIgnoreCase( "png" ) ) {
            frmt = "image/png";
        } else if ( frmt.equalsIgnoreCase( "bmp" ) ) {
            frmt = "image/bmp";
        } else if ( frmt.equalsIgnoreCase( "tif" ) || frmt.equalsIgnoreCase( "tiff" )
                    || frmt.equalsIgnoreCase( "geotiff" ) ) {
            frmt = "image/tiff";
        } else if ( frmt.equalsIgnoreCase( "gif" ) ) {
            frmt = "image/gif";
        } else if ( frmt.equalsIgnoreCase( "jpg" ) || frmt.equalsIgnoreCase( "jpeg" ) ) {
            frmt = "image/jpeg";
        } else if ( frmt.equalsIgnoreCase( "GML2" ) || frmt.equalsIgnoreCase( "GML3" ) || frmt.equalsIgnoreCase( "GML" ) ) {
            frmt = "application/vnd.ogc.gml";
        } else if ( frmt.equalsIgnoreCase( "XYZ" ) ) {
            frmt = "plain/text";
        } else {
            frmt = "application/octet-stream";
        }
        response.setContentType( frmt );

        GetCoverage req = serviceRes.getRequest();
        List<GeneralParameterValueIm> list = new ArrayList<GeneralParameterValueIm>( 20 );
        OperationParameterIm op = new OperationParameterIm( "addr", null, OGCServletController.address );
        list.add( new GeneralParameterValueIm( op ) );
        op = new OperationParameterIm( "width", null, new Integer( (int) size.getWidth() + 1 ) );
        list.add( new GeneralParameterValueIm( op ) );
        op = new OperationParameterIm( "height", null, new Integer( (int) size.getHeight() + 1 ) );
        list.add( new GeneralParameterValueIm( op ) );
        op = new OperationParameterIm( "service", null, "WCS" );
        list.add( new GeneralParameterValueIm( op ) );
        op = new OperationParameterIm( "version", null, req.getVersion() );
        list.add( new GeneralParameterValueIm( op ) );
        op = new OperationParameterIm( "coverage", null, req.getSourceCoverage() );
        list.add( new GeneralParameterValueIm( op ) );
        op = new OperationParameterIm( "crs", null, req.getDomainSubset().getRequestSRS().getCode() );
        list.add( new GeneralParameterValueIm( op ) );
        op = new OperationParameterIm( "response_crs", null, req.getOutput().getCrs().getCode() );
        list.add( new GeneralParameterValueIm( op ) );
        Envelope env = req.getDomainSubset().getSpatialSubset().getEnvelope();
        String s = StringTools.concat( 100, new Double( env.getMin().getX() ), ",", new Double( env.getMin().getY() ),
                                       ",", new Double( env.getMax().getX() ), ",", new Double( env.getMax().getY() ) );
        op = new OperationParameterIm( "BBOX", null, s );
        list.add( new GeneralParameterValueIm( op ) );
        op = new OperationParameterIm( "FORMAT", null, "GeoTiff" );
        list.add( new GeneralParameterValueIm( op ) );
        op = new OperationParameterIm( "Request", null, "GetCoverage" );
        list.add( new GeneralParameterValueIm( op ) );

        GeneralParameterValueIm[] gpvs = new GeneralParameterValueIm[list.size()];
        if ( serviceRes.getCoverage() != null ) {
            CoverageOffering co = getCoverageOffering( req );
            GridCoverageExchange gce = new GridCoverageExchange( co );
            GridCoverageWriter writer = gce.getWriter( os, format );
            writer.write( (GridCoverage) serviceRes.getCoverage(), list.toArray( gpvs ) );
        } else {
            s = Messages.getMessage( "WCSHandler.NULLCOVERAGE" );
            OGCWebServiceException owse = new OGCWebServiceException( getClass().getName(), s );
            sendException( response, owse );
        }

        os.close();

    }

    /**
     *
     * @param request
     * @return coverage offering created from request
     * @throws IOException
     * @throws SAXException
     * @throws InvalidCoverageDescriptionExcpetion
     * @throws ServiceException
     */
    private CoverageOffering getCoverageOffering( GetCoverage request )
                            throws IOException, SAXException, InvalidCoverageDescriptionExcpetion, ServiceException {

        ContentMetadata cm = ( (WCSCapabilities) getService().getCapabilities() ).getContentMetadata();
        CoverageOfferingBrief cob = cm.getCoverageOfferingBrief( request.getSourceCoverage() );
        URL url = cob.getConfiguration();
        CoverageDescription cd = CoverageDescription.createCoverageDescription( url );
        CoverageOffering co = cd.getCoverageOffering( request.getSourceCoverage() );

        return co;
    }

    /**
     * @return a service instance
     * @throws ServiceException
     */
    public WCService getService()
                            throws ServiceException {

        WCService service = null;
        try {
            service = WCServiceFactory.getService();
        } catch ( Exception e ) {
            LOG.logError( "ERROR: " + StringTools.stackTraceToString( e ), e );
            throw new ServiceException( e );
        }

        return service;
    }

}
