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
package org.deegree.ogcwebservices.wcs;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.deegree.enterprise.WebUtils;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.BootLogger;
import org.deegree.framework.util.MimeTypeMapper;
import org.deegree.framework.util.NetWorker;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.i18n.Messages;
import org.deegree.model.coverage.grid.ImageGridCoverage;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.OWSUtils;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageDescription;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageDescriptionDocument;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;
import org.deegree.ogcwebservices.wcs.describecoverage.DescribeCoverage;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSCapabilities;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSCapabilitiesDocument;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSGetCapabilities;
import org.deegree.ogcwebservices.wcs.getcoverage.GetCoverage;
import org.deegree.ogcwebservices.wcs.getcoverage.ResultCoverage;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;

/**
 * An instance of the class acts as a wrapper to a remote WMS.
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class RemoteWCService implements OGCWebService {

    private static ILogger LOG = LoggerFactory.getLogger( RemoteWCService.class );

    private static final String GETCAPABILITIES_NAME = "GetCapabilities";

    private static final String GETCOVERAGE_NAME = "GetCoverage";

    private static final String DESCRIBECOVERAGE_NAME = "DescribeCoverage";

    protected HashMap<String, URL> addresses = null;

    protected WCSCapabilities capabilities = null;

    private static Properties properties;
    static {
        if ( properties == null ) {
            try {
                properties = new Properties();
                InputStream is = RemoteWCService.class.getResourceAsStream( "remotewcservice.properties" );
                properties.load( is );
                is.close();
            } catch ( Exception e ) {
                BootLogger.logError( e.getMessage(), e );
            }
        }
    }

    /**
     * Creates a new instance of RemoteWMService
     *
     * @param capabilities
     */
    public RemoteWCService( WCSCapabilities capabilities ) {
        this.capabilities = capabilities;
        addresses = new HashMap<String, URL>();

        // get GetCapabilities operation address
        DCPType[] dcps = null;
        HTTP http = null;

        OperationsMetadata om = capabilities.getCapabilitiy().getOperations();

        Operation[] ops = om.getOperations();
        for ( Operation operation : ops ) {
            if ( operation.getName().equals( GETCAPABILITIES_NAME ) ) {
                dcps = operation.getDCPs();
                for ( DCPType dcp : dcps ) {
                    if ( dcp.getProtocol() instanceof HTTP ) {
                        http = (HTTP) dcp.getProtocol();
                    }
                }
            }
        }
        addresses.put( GETCAPABILITIES_NAME, http.getGetOnlineResources()[0] );

        // get GetCoverage operation address
        for ( Operation operation : ops ) {
            if ( operation.getName().equals( GETCOVERAGE_NAME ) ) {
                dcps = operation.getDCPs();
                for ( DCPType dcp : dcps ) {
                    if ( dcp.getProtocol() instanceof HTTP ) {
                        http = (HTTP) dcp.getProtocol();
                    }
                }
            }
        }
        addresses.put( GETCOVERAGE_NAME, http.getGetOnlineResources()[0] );

        // get DescribeCoverage operation address
        for ( Operation operation : ops ) {
            if ( operation.getName().equals( DESCRIBECOVERAGE_NAME ) ) {
                dcps = operation.getDCPs();
                for ( DCPType dcp : dcps ) {
                    if ( dcp.getProtocol() instanceof HTTP ) {
                        http = (HTTP) dcp.getProtocol();
                    }
                }
            }
        }
        addresses.put( DESCRIBECOVERAGE_NAME, http.getGetOnlineResources()[0] );

    }

    public OGCCapabilities getCapabilities() {
        return capabilities;
    }

    /**
     * the method performs the handling of the passed OGCWebServiceEvent directly and returns the
     * result to the calling class/method
     *
     * @param request
     *            request to perform
     *
     * @throws OGCWebServiceException
     */
    public Object doService( OGCWebServiceRequest request )
                            throws OGCWebServiceException {
        Object o = null;
        if ( request instanceof GetCoverage ) {
            o = handleGetCoverage( (GetCoverage) request );
        } else if ( request instanceof DescribeCoverage ) {
            o = handleDescribeCoverage( (DescribeCoverage) request );
        }

        return o;

    }

    // checks for excessive &
    private static String constructRequestURL( String params, String url ) {
        if ( url.endsWith( "?" ) && params.startsWith( "&" ) ) {
            return url + params.substring( 1 );
        }

        return url + params;
    }

    /**
     * performs a GetCoverage request against a remote service. The result contains the Coverage
     * decoded in the desired format as a byte array.
     *
     * @param request
     *            GetCoverage request
     * @return the requested coverage
     * @throws OGCWebServiceException
     *             if the url in the request is <code>null</code>
     */
    protected Object handleGetCoverage( GetCoverage request )
                            throws OGCWebServiceException {

        URL url = addresses.get( GETCOVERAGE_NAME );

        String us = constructRequestURL( request.getRequestParameter(), OWSUtils.validateHTTPGetBaseURL( url.toExternalForm() ) );

        LOG.logDebug( "remote wcs GetCoverage", us );

        Object result = null;
        try {
            HttpClient client = new HttpClient();
            WebUtils.enableProxyUsage( client, new URL( us ) );
            int timeout = 25000;
            if ( properties != null && properties.getProperty( "timeout" ) != null ) {
                timeout = Integer.parseInt( properties.getProperty( "timeout" ) );
            }
            LOG.logDebug( "timeout is:", timeout );
            client.getHttpConnectionManager().getParams().setSoTimeout( timeout );
            GetMethod get = new GetMethod( us );
            client.executeMethod( get );
            InputStream is = get.getResponseBodyAsStream();
            Header header = get.getResponseHeader( "Content-type" );

            String contentType = header.getValue();
            String[] tmp = StringTools.toArray( contentType, ";", true );
            contentType = tmp[0];

            if ( "application/vnd.ogc.se_xml".equals( contentType ) ) {
                String res = "Remote-WCS message: " + getInputStreamContent( is );
                throw new OGCWebServiceException( "RemoteWCS:handleGetCoverage", res );
            } else if ( MimeTypeMapper.isImageType( contentType ) && MimeTypeMapper.isKnownImageType( contentType ) ) {
                MemoryCacheSeekableStream mcss = new MemoryCacheSeekableStream( is );
                RenderedOp rop = JAI.create( "stream", mcss );
                BufferedImage bi = rop.getAsBufferedImage();
                mcss.close();
                DescribeCoverage dscC = new DescribeCoverage( UUID.randomUUID().toString(), capabilities.getVersion(),
                                                              new String[] { request.getSourceCoverage() } );
                CoverageDescription cd = handleDescribeCoverage( dscC );
                CoverageOffering co = cd.getCoverageOfferings()[0];
                result = new ImageGridCoverage( co, request.getDomainSubset().getSpatialSubset().getEnvelope(), bi );
            } else {
                // must be something else; e.g. GML or XYZ-text
                result = getInputStreamContent( is );
            }
        } catch ( HttpException e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "REMOTEWMS_GETMAP_GENERAL_ERROR", capabilities.getService().getLabel(),
                                              us );
            throw new OGCWebServiceException( "RemoteWCS:handleGetCoverage", msg );
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "REMOTEWMS_GETMAP_GENERAL_ERROR", capabilities.getService().getLabel(),
                                              us );
            throw new OGCWebServiceException( "RemoteWCS:handleGetCoverage", msg );
        }

        return new ResultCoverage( result, result.getClass(), request.getOutput().getFormat(), request );

    }

    /**
     *
     * @param request
     *            DescribeCoverage to perform
     * @return the response of the DescribeCoverage request.
     * @throws OGCWebServiceException
     *             if the request could not be excuted correctly.
     */
    protected CoverageDescription handleDescribeCoverage( DescribeCoverage request )
                            throws OGCWebServiceException {

        URL url = addresses.get( DESCRIBECOVERAGE_NAME );

        if ( url == null ) {
            String msg = Messages.getMessage( "REMOTEWMS_GFI_NOT_SUPPORTED", capabilities.getService().getLabel() );
            throw new OGCWebServiceException( msg );
        }

        String us = constructRequestURL( request.getRequestParameter(), OWSUtils.validateHTTPGetBaseURL( url.toExternalForm() ) );

        CoverageDescription result = null;
        try {
            LOG.logDebug( "DescribeCoverage: ", us );
            // get map from the remote service
            HttpClient client = new HttpClient();
            WebUtils.enableProxyUsage( client, new URL( us ) );
            int timeout = 25000;
            if ( properties != null && properties.getProperty( "timeout" ) != null ) {
                timeout = Integer.parseInt( properties.getProperty( "timeout" ) );
            }
            LOG.logDebug( "timeout is:", timeout );
            client.getHttpConnectionManager().getParams().setSoTimeout( timeout );
            GetMethod get = new GetMethod( us );
            client.executeMethod( get );
            InputStream is = get.getResponseBodyAsStream();
            Header header = get.getResponseHeader( "Content-type" );

            String contentType = header.getValue();
            String[] tmp = StringTools.toArray( contentType, ";", true );
            contentType = tmp[0];

            if ( "application/vnd.ogc.se_xml".equals( contentType ) ) {
                String res = "Remote-WCS message: " + getInputStreamContent( is );
                throw new OGCWebServiceException( "RemoteWCS:handleGetCoverage", res );
            } else {
                CoverageDescriptionDocument doc = new CoverageDescriptionDocument();
                doc.load( is, us );
                result = new CoverageDescription( doc );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "REMOTEWCS_GFI_GENERAL_ERROR", capabilities.getService().getLabel(), us );
            throw new OGCWebServiceException( "RemoteWCS:handleFeatureInfo", msg );
        }

        return result;
    }

    /**
     * reads the capabilities from the remote WMS by performing a GetCapabilities request against
     * it.
     *
     * @param request
     *            capabilities request
     * @return remote capabilities
     * @throws OGCWebServiceException
     *             if the request could not be executed correctly.
     */
    protected WCSCapabilities handleGetCapabilities( WCSGetCapabilities request )
                            throws OGCWebServiceException {

        URL url = addresses.get( GETCAPABILITIES_NAME );

        String us = constructRequestURL( request.getRequestParameter(), OWSUtils.validateHTTPGetBaseURL( url.toExternalForm() ) );

        WCSCapabilities result = null;

        try {
            URL ur = new URL( us );
            // get map from the remote service
            NetWorker nw = new NetWorker( ur );
            byte[] b = nw.getDataAsByteArr( 20000 );
            String contentType = nw.getContentType();

            if ( MimeTypeMapper.isKnownMimeType( contentType ) ) {
                // create a WMSCapabilitiesTEMP instance from the result
                StringReader reader = new StringReader( new String( b ) );
                WCSCapabilitiesDocument doc = new WCSCapabilitiesDocument();
                doc.load( reader, XMLFragment.DEFAULT_URL );
                result = (WCSCapabilities) doc.parseCapabilities();
            } else {
                String msg = Messages.getMessage( "REMOTEWMS_GETCAPS_INVALID_CONTENTTYPE", contentType, us );
                throw new OGCWebServiceException( "RemoteWCS:handleGetCapabilities", msg );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "REMOTEWCS_GETCAPS_GENERAL_ERROR", capabilities.getService().getLabel(),
                                              us );
            throw new OGCWebServiceException( "RemoteWCS:handleGetCapabilities", msg );
        }

        return result;
    }

    /**
     *
     *
     * @param is
     *
     * @return thr content as String
     *
     * @throws IOException
     */
    protected String getInputStreamContent( InputStream is )
                            throws IOException {
        StringBuffer sb = new StringBuffer( 1000 );
        int c = 0;

        while ( ( c = is.read() ) >= 0 ) {
            sb.append( (char) c );
        }

        is.close();
        return sb.toString();
    }

}
