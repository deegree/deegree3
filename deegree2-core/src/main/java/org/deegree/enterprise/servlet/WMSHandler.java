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

import static javax.imageio.ImageIO.write;
import static org.deegree.framework.util.CharsetUtils.getSystemCharset;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.enterprise.ServiceException;
import org.deegree.enterprise.servlet.GetMapFilter.DummyRequest;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.MimeTypeMapper;
import org.deegree.framework.util.NetWorker;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.DOMPrinter;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.ogcwebservices.ExceptionReport;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.OGCWebServiceResponse;
import org.deegree.ogcwebservices.wms.InvalidFormatException;
import org.deegree.ogcwebservices.wms.WMService;
import org.deegree.ogcwebservices.wms.WMServiceFactory;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities_1_3_0;
import org.deegree.ogcwebservices.wms.configuration.WMSConfigurationType;
import org.deegree.ogcwebservices.wms.configuration.WMSDeegreeParams;
import org.deegree.ogcwebservices.wms.operation.DescribeLayerResult;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfo;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfoResult;
import org.deegree.ogcwebservices.wms.operation.GetLegendGraphic;
import org.deegree.ogcwebservices.wms.operation.GetLegendGraphicResult;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.ogcwebservices.wms.operation.GetMapResult;
import org.deegree.ogcwebservices.wms.operation.GetStylesResult;
import org.deegree.ogcwebservices.wms.operation.PutStylesResult;
import org.deegree.ogcwebservices.wms.operation.WMSGetCapabilitiesResult;
import org.deegree.owscommon.XMLFactory;
import org.deegree.owscommon_new.DomainType;
import org.deegree.owscommon_new.Operation;
import org.deegree.owscommon_new.OperationsMetadata;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <code>WMSHandler</code> is the handler class for WMS requests and their results.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version 2.0, $Revision$, $Date$
 * 
 * @since 2.0
 */
public class WMSHandler extends AbstractOWServiceHandler {

    private static ILogger LOG = LoggerFactory.getLogger( WMSHandler.class );

    private Color bgColor = Color.WHITE;

    private HttpServletResponse resp = null;

    private OGCWebServiceRequest request = null;

    private String exceptionFormat;

    private String format = null, version = null;

    private boolean transparent = false;

    private int height = 400;

    private int width = 600;

    private WMSConfigurationType configuration = null;

    /**
     *
     */
    public WMSHandler() {
        LOG.logDebug( "New WMSHandler instance created: " + this.getClass().getName() );
    }

    private String checkExceptionFormat( String exceptionFormat, List<String> availableExceptionFormats ) {
        boolean found = false;
        for ( String f : availableExceptionFormats ) {
            if ( f.equalsIgnoreCase( exceptionFormat ) ) {
                found = true;
            }
        }
        if ( !found ) {
            return availableExceptionFormats.get( 0 );
        }

        return exceptionFormat;
    }

    /**
     * performs the passed OGCWebServiceRequest by accessing service from the pool and passing the request to it
     */
    public void perform( OGCWebServiceRequest request, HttpServletResponse response )
                            throws ServiceException {
        this.request = request;
        resp = response;
        version = request.getVersion();

        try {

            OGCWebService service = WMServiceFactory.getService();
            configuration = (WMSConfigurationType) ( (WMService) service ).getCapabilities();

            List<String> availableExceptionFormats = new LinkedList<String>();

            // add 1.1.1 names if 1.3.0 capable
            for ( String f : configuration.getExceptions() ) {
                if ( f.equalsIgnoreCase( "XML" ) ) {
                    availableExceptionFormats.add( "application/vnd.ogc.se_xml" );
                }
                if ( f.equalsIgnoreCase( "INIMAGE" ) ) {
                    availableExceptionFormats.add( "application/vnd.ogc.se_inimage" );
                }
                if ( f.equalsIgnoreCase( "BLANK" ) ) {
                    availableExceptionFormats.add( "application/vnd.ogc.se_blank" );
                }

                availableExceptionFormats.add( f );
            }

            // EXCEPTION HANDLING NOTES:
            // currently, the exceptions are handled differently for each request type,
            // change the behaviour here
            if ( request instanceof GetMap ) {
                GetMap req = (GetMap) request;
                exceptionFormat = req.getExceptions();
                exceptionFormat = checkExceptionFormat( exceptionFormat, availableExceptionFormats );
                format = req.getFormat();
                bgColor = req.getBGColor();
                transparent = req.getTransparency();
                height = req.getHeight();
                width = req.getWidth();
            }

            if ( request instanceof GetLegendGraphic ) {
                GetLegendGraphic req = (GetLegendGraphic) request;
                exceptionFormat = req.getExceptions();
                exceptionFormat = checkExceptionFormat( exceptionFormat, availableExceptionFormats );
                format = req.getFormat();
                height = req.getHeight();
                width = req.getWidth();
            }

            if ( request instanceof GetFeatureInfo ) {
                GetFeatureInfo req = (GetFeatureInfo) request;
                exceptionFormat = req.getExceptions();
                exceptionFormat = checkExceptionFormat( exceptionFormat, availableExceptionFormats );
            }

            fixupExceptionFormat();

            if ( request instanceof DummyRequest ) {
                // the dummy request is used to prevent using the response object so the handler can be
                // used externally. One could also rewrite the handler...
            } else {

                Object o = service.doService( request );
                if ( request instanceof GetMap ) {
                    for ( String header : ( (GetMap) request ).warningHeaders ) {
                        response.setHeader( "Warning", header );
                    }
                }
                handleResponse( o );
            }

        } catch ( OGCWebServiceException e ) {
            LOG.logError( e.getMessage(), e );
            writeServiceExceptionReport( e );
        }
    }

    private void fixupExceptionFormat() {
        if ( exceptionFormat == null || exceptionFormat.equals( "" ) ) {
            if ( "1.1.1".equals( version ) ) {
                exceptionFormat = "application/vnd.ogc.se_xml";
            } else {
                exceptionFormat = "XML";
            }
        }

        // fixup the exception formats, 1.3.0 has it different
        // note that XML/....se_xml are not the same format!
        if ( "INIMAGE".equalsIgnoreCase( exceptionFormat ) ) {
            exceptionFormat = "application/vnd.ogc.se_inimage";
        }
        if ( "BLANK".equalsIgnoreCase( exceptionFormat ) ) {
            exceptionFormat = "application/vnd.ogc.se_blank";
        }

    }

    /**
     * 
     * 
     * @param result
     */
    private void handleResponse( Object result ) {
        // this method may need restructuring

        // handle exception case
        if ( result instanceof OGCWebServiceException ) {
            writeServiceExceptionReport( (OGCWebServiceException) result );
            return;
        }

        try {
            OGCWebServiceResponse response = (OGCWebServiceResponse) result;

            if ( response.getException() != null ) {
                // handle the case that an exception occurred during the
                // request performance
                writeServiceExceptionReport( response.getException() );
            } else {
                if ( response instanceof OGCWebServiceException ) {
                    writeServiceExceptionReport( (OGCWebServiceException) response );
                } else if ( response instanceof Exception ) {
                    sendException( resp, (Exception) response );
                } else if ( response instanceof WMSGetCapabilitiesResult ) {
                    handleGetCapabilitiesResponse( (WMSGetCapabilitiesResult) response );
                } else if ( response instanceof GetMapResult ) {
                    handleGetMapResponse( (GetMapResult) response );
                } else if ( response instanceof GetFeatureInfoResult ) {
                    handleFeatureInfoResponse( (GetFeatureInfoResult) response );
                } else if ( response instanceof GetStylesResult ) {
                    handleGetStylesResponse( (GetStylesResult) response );
                } else if ( response instanceof PutStylesResult ) {
                    handlePutStylesResponse( (PutStylesResult) response );
                } else if ( response instanceof DescribeLayerResult ) {
                    handleDescribeLayerResponse( (DescribeLayerResult) response );
                } else if ( response instanceof GetLegendGraphicResult ) {
                    handleGetLegendGraphicResponse( (GetLegendGraphicResult) response );
                }
            }
        } catch ( InvalidFormatException ife ) {
            LOG.logError( ife.getMessage(), ife );
            writeServiceExceptionReport( ife );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            writeServiceExceptionReport( new OGCWebServiceException( "WMS:write", e.getLocalizedMessage() ) );
        }
    }

    /**
     * handles the response to a get capabilities request
     * 
     * @param response
     * @throws IOException
     * @throws TransformerException
     */
    private void handleGetCapabilitiesResponse( WMSGetCapabilitiesResult response )
                            throws IOException, TransformerException {
        WMSConfigurationType capa = response.getCapabilities();

        WMSDeegreeParams params = capa.getDeegreeParams();

        // version war follows

        boolean version130 = "1.3.0".equals( capa.calculateVersion( request.getVersion() ) );

        // version not set -> use highest supported version
        // use request's version otherwise

        boolean support111 = false;
        boolean support130 = false;
        for ( String version : params.getSupportedVersions() ) {
            if ( "1.1.1".equals( version ) )
                support111 = true;
            if ( "1.3.0".equals( version ) )
                support130 = true;
        }

        if ( ( !support130 ) && ( !support111 ) ) {
            support111 = true;
        }

        if ( version130 && support130 ) {
            resp.setContentType( "text/xml" );
        } else {
            resp.setContentType( "application/vnd.ogc.wms_xml" );
        }

        resp.setCharacterEncoding( getSystemCharset() );

        XMLFragment doc = null;

        if ( ( ( ( !version130 ) && support111 ) || ( !support130 ) ) && ( capa instanceof WMSCapabilities_1_3_0 ) ) {
            doc = org.deegree.ogcwebservices.wms.XMLFactory.exportAs_1_1_1( (WMSCapabilities_1_3_0) capa );
        } else {
            doc = org.deegree.ogcwebservices.wms.XMLFactory.export( (WMSCapabilities) capa );
        }

        if ( ( version130 && support130 ) || ( !support111 ) ) {
            doc.getRootElement().setAttribute( "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );
            doc.getRootElement().setAttribute(
                                               "xsi:schemaLocation",
                                               "http://www.opengis.net/wms "
                                                                       + "http://schemas.opengis.net/wms/1.3.0/capabilities_1_3_0.xsd"
                                                                       + " http://www.opengis.net/sld "
                                                                       + "http://schemas.opengis.net/sld/1.1.0/sld_capabilities.xsd" );

            doc.prettyPrint( resp.getWriter() );
        } else {
            String xml = new XMLFragment( doc.getRootElement() ).getAsString();
            xml = doc.getAsString();
            String dtd = NetWorker.url2String( configuration.getDeegreeParams().getDTDLocation() );
            StringBuffer sb = new StringBuffer();
            sb.append( "<!DOCTYPE WMT_MS_Capabilities SYSTEM " );
            sb.append( "'" + dtd + "' \n" );
            sb.append( "[\n<!ELEMENT VendorSpecificCapabilities EMPTY>\n]>" );

            int p = xml.indexOf( "?>" );
            if ( p > -1 ) {
                xml = xml.substring( p + 2, xml.length() );
            }

            xml = StringTools.concat( 50000, "<?xml version=\"1.0\" encoding=\"", CharsetUtils.getSystemCharset(),
                                      "\"?>", "\n", sb.toString(), xml );

            xml = StringTools.replace( xml, "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", "", false );

            try {
                PrintWriter pw = resp.getWriter();
                pw.print( xml );
                pw.close();
            } catch ( Exception e ) {
                LOG.logError( "-", e );
            }

        }
    }

    /**
     * handles the response to a get map request
     * 
     * @param response
     * @throws InvalidFormatException
     */
    public void handleGetMapResponse( GetMapResult response )
                            throws InvalidFormatException {
        // schmitz: added the toLowerCase to avoid errors
        String mime = MimeTypeMapper.toMimeType( ( (GetMap) request ).getFormat().toLowerCase() );

        if ( !MimeTypeMapper.isImageType( mime ) ) {
            throw new InvalidFormatException( mime + " is not a known image format" );
        }

        writeImage( response.getMap(), mime );
    }

    /**
     * handles the response to a get featureinfo request
     * 
     * @param response
     */
    private void handleFeatureInfoResponse( GetFeatureInfoResult response )
                            throws Exception {
        GetFeatureInfo req = (GetFeatureInfo) request;

        String s = req.getInfoFormat();

        // check if GML is actually the correct one
        // THIS IS A HACK
        if ( req.isInfoFormatDefault() ) {
            OperationsMetadata om = configuration.getOperationMetadata();
            Operation op = om.getOperation( new QualifiedName( "GetFeatureInfo" ) );
            DomainType dt = (DomainType) op.getParameter( new QualifiedName( "Format" ) );
            List<TypedLiteral> vals = dt.getValues();
            s = vals.get( 0 ).getValue();
        }

        String mime = MimeTypeMapper.toMimeType( s );
        resp.setContentType( mime + "; charset=" + CharsetUtils.getSystemCharset() );

        String fir = response.getFeatureInfo();

        String filter = FeatureInfoFilterDef.getString( s );

        if ( filter != null ) {
            handleFilteredFeatureInfoResponse( fir, filter );
        } else {
            OutputStreamWriter os = null;
            try {
                os = new OutputStreamWriter( resp.getOutputStream(), CharsetUtils.getSystemCharset() );
                os.write( fir );
            } catch ( Exception e ) {
                LOG.logError( "could not write to outputstream", e );
            } finally {
                if ( os != null ) {
                    os.close();
                }
            }
        }
    }

    /**
     * @param fir
     * @param filter
     * @throws MalformedURLException
     * @throws SAXException
     * @throws IOException
     * @throws URISyntaxException
     * @throws TransformerException
     */
    private void handleFilteredFeatureInfoResponse( String fir, String filter )
                            throws Exception {
        URL url = new URL( configuration.getBaseURL(), filter );
        LOG.logDebug( "used XSLT for transformation: ", url );
        LOG.logDebug( "GML document to transform", fir );
        XMLFragment xml = new XMLFragment( new StringReader( fir ) , XMLFragment.DEFAULT_URL );
       
        OutputStream os = null;
        try {
            os = resp.getOutputStream();
            XSLTDocument xslt = new XSLTDocument( url );
            xslt.transform( xml, os );
        } catch ( IOException e ) {
            LOG.logError( "could not write to outputstream", e );
        } finally {
            if ( os != null ) {
                os.close();
            }
        }
    }

    /**
     * handles the response to a get styles request
     * 
     * @param response
     */
    private void handleGetStylesResponse( GetStylesResult response ) {
        throw new RuntimeException( "method: handleGetStylesResponse not implemented yet" );
    }

    /**
     * handles the response to a put styles request
     * 
     * @param response
     */
    private void handlePutStylesResponse( PutStylesResult response ) {
        throw new RuntimeException( "method: handlePutStylesResponse not implemented yet" );
    }

    /**
     * handles the response to a describe layer request
     * 
     * @param response
     * @throws IOException
     * @throws TransformerException
     */
    private void handleDescribeLayerResponse( DescribeLayerResult response )
                            throws TransformerException, IOException {
        resp.setCharacterEncoding( "UTF-8" );
        resp.setContentType( "text/xml" );
        Writer out = resp.getWriter();
        response.getResult().prettyPrint( out );
        out.close();
    }

    /**
     * handles the response to a get legend graphic request
     * 
     * @param response
     */
    private void handleGetLegendGraphicResponse( GetLegendGraphicResult response )
                            throws Exception {
        String mime = MimeTypeMapper.toMimeType( ( (GetLegendGraphic) request ).getFormat() );

        if ( !MimeTypeMapper.isImageType( mime ) ) {
            throw new InvalidFormatException( mime + " is not a known image format" );
        }

        writeImage( response.getLegendGraphic(), mime );
    }

    private void writeServiceExceptionReport( OGCWebServiceException exception, OutputStream out ) {
        LOG.logInfo( "Sending exception in XML format." );
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            ExceptionReport report = new ExceptionReport( new OGCWebServiceException[] { exception } );
            String msg;
            if ( exceptionFormat.equals( "XML" ) ) {
                msg = XMLFactory.exportNS( report ).getAsPrettyString();
            } else {
                msg = XMLFactory.export( report ).getAsPrettyString();
            }

            LOG.logDebug( "Exception being sent: " + msg );
        }

        ExceptionReport report = new ExceptionReport( new OGCWebServiceException[] { exception } );
        try {
            XMLFragment doc;

            if ( exceptionFormat.equals( "XML" ) ) {
                resp.setContentType( "text/xml" );
                doc = XMLFactory.exportNS( report );
            } else {
                resp.setContentType( "application/vnd.ogc.se_xml" );
                doc = XMLFactory.export( report );
            }

            doc.write( out );
            out.close();
        } catch ( Exception ex ) {
            LOG.logError( "ERROR: " + ex.getMessage(), ex );
        }
    }

    /**
     * @param eFormat
     * @param format
     * @param version
     * @param response
     */
    public void determineExceptionFormat( String eFormat, String format, String version, HttpServletResponse response ) {
        exceptionFormat = eFormat;
        this.format = format;
        this.version = version;
        resp = response;

        fixupExceptionFormat();
    }

    /**
     * writes an service exception report into the <tt>OutputStream</tt> back to the client. the method considers the
     * format an exception shall be returned to the client as defined in the request.
     * 
     * @param exception
     *            the exception object containing the code and message
     */
    public void writeServiceExceptionReport( OGCWebServiceException exception ) {
        String code = "none";
        if ( exception.getCode() != null ) {
            code = exception.getCode().value;
        }
        String message = exception.getMessage();

        LOG.logInfo( "sending exception in format " + exceptionFormat );
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            ExceptionReport report = new ExceptionReport( new OGCWebServiceException[] { exception } );
            String msg;
            if ( exceptionFormat.equals( "XML" ) ) {
                msg = XMLFactory.exportNS( report ).getAsPrettyString();
            } else {
                msg = XMLFactory.export( report ).getAsPrettyString();
            }

            LOG.logDebug( "Exception being sent: " + msg );
        }

        if ( exceptionFormat.equals( "application/vnd.ogc.se_inimage" ) ) {
            BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
            Graphics g = bi.getGraphics();

            if ( !transparent ) {
                g.setColor( bgColor );
                g.fillRect( 0, 0, bi.getWidth(), bi.getHeight() );
            }

            g.setColor( Color.BLUE );
            g.drawString( code, 5, 20 );
            int pos1 = message.indexOf( ':' );
            g.drawString( message.substring( 0, pos1 + 1 ), 5, 50 );
            g.drawString( message.substring( pos1 + 1, message.length() ), 5, 80 );
            String mime = MimeTypeMapper.toMimeType( format );
            writeImage( bi, mime );
        } else if ( exceptionFormat.equals( "application/vnd.ogc.se_blank" ) ) {
            BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
            Graphics g = bi.getGraphics();

            if ( !transparent ) {
                g.setColor( bgColor );
                g.fillRect( 0, 0, bi.getWidth(), bi.getHeight() );
            }

            g.dispose();
            String mime = MimeTypeMapper.toMimeType( format );
            writeImage( bi, mime );
        } else {
            LOG.logInfo( "Sending OGCWebServiceException to client." );
            ExceptionReport report = new ExceptionReport( new OGCWebServiceException[] { exception } );
            try {
                XMLFragment doc;

                if ( exceptionFormat.equals( "XML" ) ) {
                    resp.setContentType( "text/xml" );
                    doc = XMLFactory.exportNS( report );
                } else {
                    resp.setContentType( "application/vnd.ogc.se_xml" );
                    doc = XMLFactory.export( report );
                }

                OutputStream os = resp.getOutputStream();
                doc.write( os );
                os.close();
            } catch ( Exception ex ) {
                LOG.logError( "ERROR: " + ex.getMessage(), ex );
            }
        }
    }

    /**
     * writes the passed image to the response output stream.
     * 
     * @param output
     * @param mime
     */
    private void writeImage( Object output, String mime ) {
        OutputStream os = null;
        try {
            resp.setContentType( mime );
            if ( mime.equalsIgnoreCase( "image/gif" ) ) {
                BufferedImage img = (BufferedImage) output;
                ByteArrayOutputStream out = new ByteArrayOutputStream( img.getWidth() * img.getHeight() * 4 );
                try {
                    ImageUtils.encodeGif( out, img );
                } catch ( IOException e ) {
                    LOG.logWarning( "ACME failed to write GIF image, trying ImageIO." );
                    LOG.logDebug( "Stack trace", e );
                    // use imageio, it can transform the colors correctly starting from Java 1.6
                    if ( !write( img, "gif", out ) ) {
                        os = resp.getOutputStream();
                        writeServiceExceptionReport( new OGCWebServiceException( e.getLocalizedMessage() ), os );
                        os.close();
                        return;
                    }
                }

                resp.setContentType( mime );
                os = resp.getOutputStream();

                out.close();
                byte[] bs = out.toByteArray();
                out = null;

                os.write( bs );
            } else if ( mime.equalsIgnoreCase( "image/jpg" ) || mime.equalsIgnoreCase( "image/jpeg" ) ) {
                os = resp.getOutputStream();
                ImageUtils.saveImage( (BufferedImage) output, os, "jpeg",
                                      configuration.getDeegreeParams().getMapQuality() );
            } else if ( mime.equalsIgnoreCase( "image/png" ) || mime.equalsIgnoreCase( "image/png; mode=24bit" ) ) {
                os = resp.getOutputStream();
                ImageUtils.saveImage( (BufferedImage) output, os, "png", 1 );
            } else if ( mime.equalsIgnoreCase( "image/png; mode=8bit" ) ) {
                os = resp.getOutputStream();
                ImageIO.write( (BufferedImage) output, "png", os );
                // ImageUtils.saveImage( (BufferedImage) output, os, "png", 1 ); // ImageUtils produces double size
                // images...
            } else if ( mime.equalsIgnoreCase( "image/tif" ) || mime.equalsIgnoreCase( "image/tiff" ) ) {
                os = resp.getOutputStream();
                ImageUtils.saveImage( (BufferedImage) output, os, "tif", 1 );
            } else if ( mime.equalsIgnoreCase( "image/bmp" ) ) {
                os = resp.getOutputStream();
                ImageUtils.saveImage( (BufferedImage) output, os, "bmp", 1 );
            } else if ( mime.equalsIgnoreCase( "image/svg+xml" ) ) {
                os = resp.getOutputStream();
                resp.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
                PrintWriter pw = new PrintWriter( os );
                DOMPrinter.printNode( pw, (Node) output );
                pw.close();
            } else {
                resp.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
                os = resp.getOutputStream();
                OGCWebServiceException exce = new OGCWebServiceException( "WMS:writeImage",
                                                                          "unsupported image format: " + mime );
                writeServiceExceptionReport( exce, os );
            }

            os.close();
        } catch ( Exception e ) {
            LOG.logError( resp.isCommitted() ? "Response is already committed!" : "Response is not committed yet." );
            LOG.logError( "Error while writing image: ", e );
            writeServiceExceptionReport( new OGCWebServiceException( e.getLocalizedMessage() ), os );
        }
    }

    /**
     * It's a workaround to make the 'API' more usable.
     * 
     * @param request
     */
    public void setRequest( OGCWebServiceRequest request ) {
        this.request = request;
    }

}
