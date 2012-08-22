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
package org.deegree.ogcwebservices.wms;

import static org.deegree.enterprise.WebUtils.enableProxyUsage;
import static org.deegree.framework.util.MapUtils.DEFAULT_PIXEL_SIZE;
import static org.deegree.framework.util.MapUtils.calcScale;
import static org.deegree.model.spatialschema.GeometryFactory.createEnvelope;
import static org.deegree.ogcwebservices.OWSUtils.validateHTTPGetBaseURL;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.BootLogger;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.MimeTypeMapper;
import org.deegree.framework.util.NetWorker;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.LayerBoundingBox;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocument;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocumentFactory;
import org.deegree.ogcwebservices.wms.operation.DescribeLayer;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfo;
import org.deegree.ogcwebservices.wms.operation.GetLegendGraphic;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.ogcwebservices.wms.operation.GetStyles;
import org.deegree.ogcwebservices.wms.operation.PutStyles;
import org.deegree.ogcwebservices.wms.operation.WMSGetCapabilities;
import org.deegree.ogcwebservices.wms.operation.WMSProtocolFactory;
import org.deegree.owscommon_new.DCP;
import org.deegree.owscommon_new.HTTP;
import org.deegree.owscommon_new.Operation;
import org.deegree.owscommon_new.OperationsMetadata;
import org.xml.sax.SAXException;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;

/**
 * An instance of the class acts as a wrapper to a remote WMS.
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class RemoteWMService implements OGCWebService {

    private static ILogger LOG = LoggerFactory.getLogger( RemoteWMService.class );

    private static final String GETCAPABILITIES_NAME = "GetCapabilities";

    private static final String CAPABILITIES_NAME = "Capabilities";

    private static final String GETMAP_NAME = "GetMap";

    private static final String MAP_NAME = "Map";

    private static final String GETFEATUREINFO_NAME = "GetFeatureInfo";

    private static final String FEATUREINFO_NAME = "FeatureInfo";

    private static final String DESCRIBELAYER_NAME = "DescribeLayer";

    private static final String GETLEGENDGRAPHIC_NAME = "GetLegendGraphic";

    private static final String GETSTYLES_NAME = "GetStyles";

    private static final String PUTSTYLES_NAME = "PutStyles";

    // private static final String UNKNOWN_NAME = "Unknown";

    protected HashMap<String, URL> addresses = null;

    protected WMSCapabilities capabilities = null;

    private static Properties properties;
    static {
        if ( properties == null ) {
            try {
                properties = new Properties();
                InputStream is = RemoteWMService.class.getResourceAsStream( "remotewmservice.properties" );
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
    public RemoteWMService( WMSCapabilities capabilities ) {
        this.capabilities = capabilities;
        addresses = new HashMap<String, URL>();

        // get GetCapabilities operation address
        List<DCP> dcps = null;
        HTTP http = null;

        OperationsMetadata om = capabilities.getOperationMetadata();

        if ( capabilities.getVersion().equals( "1.0.0" ) ) {
            dcps = om.getOperation( new QualifiedName( CAPABILITIES_NAME ) ).getDCP();
            for ( DCP dcp : dcps )
                if ( dcp instanceof HTTP )
                    http = (HTTP) dcp;
            if ( http != null ) {
                addresses.put( CAPABILITIES_NAME, http.getLinks().get( 0 ).getLinkage().getHref() );
            }
        } else {
            dcps = om.getOperation( new QualifiedName( GETCAPABILITIES_NAME ) ).getDCP();
            for ( DCP dcp : dcps )
                if ( dcp instanceof HTTP )
                    http = (HTTP) dcp;
            if ( http != null ) {
                addresses.put( GETCAPABILITIES_NAME, http.getLinks().get( 0 ).getLinkage().getHref() );
            }
        }

        // get GetMap operation address
        if ( capabilities.getVersion().equals( "1.0.0" ) ) {
            dcps = om.getOperation( new QualifiedName( MAP_NAME ) ).getDCP();
            for ( DCP dcp : dcps )
                if ( dcp instanceof HTTP )
                    http = (HTTP) dcp;
            if ( http != null ) {
                addresses.put( MAP_NAME, http.getLinks().get( 0 ).getLinkage().getHref() );
            }
        } else {
            dcps = om.getOperation( new QualifiedName( GETMAP_NAME ) ).getDCP();
            for ( DCP dcp : dcps )
                if ( dcp instanceof HTTP )
                    http = (HTTP) dcp;
            if ( http != null ) {
                addresses.put( GETMAP_NAME, http.getLinks().get( 0 ).getLinkage().getHref() );
            }
        }

        // get GetFeatureInfo operation address
        if ( capabilities.getVersion().equals( "1.0.0" ) ) {
            Operation operation = om.getOperation( new QualifiedName( FEATUREINFO_NAME ) );

            if ( operation != null ) {
                dcps = operation.getDCP();
                for ( DCP dcp : dcps )
                    if ( dcp instanceof HTTP )
                        http = (HTTP) dcp;
                if ( http != null ) {
                    addresses.put( FEATUREINFO_NAME, http.getLinks().get( 0 ).getLinkage().getHref() );
                }
            }
        } else {
            Operation operation = om.getOperation( new QualifiedName( GETFEATUREINFO_NAME ) );

            if ( operation != null ) {
                dcps = operation.getDCP();
                for ( DCP dcp : dcps )
                    if ( dcp instanceof HTTP )
                        http = (HTTP) dcp;
                if ( http != null ) {
                    addresses.put( GETFEATUREINFO_NAME, http.getLinks().get( 0 ).getLinkage().getHref() );
                }
            }
        }

        // get GetLegendGraphic operation address
        Operation operation = om.getOperation( new QualifiedName( GETLEGENDGRAPHIC_NAME ) );

        if ( operation != null ) {
            dcps = operation.getDCP();
            for ( DCP dcp : dcps )
                if ( dcp instanceof HTTP )
                    http = (HTTP) dcp;
            if ( http != null ) {
                addresses.put( GETLEGENDGRAPHIC_NAME, http.getLinks().get( 0 ).getLinkage().getHref() );
            }
        }

        // get GetStyles operation address
        operation = om.getOperation( new QualifiedName( GETSTYLES_NAME ) );

        if ( operation != null ) {
            dcps = operation.getDCP();
            for ( DCP dcp : dcps )
                if ( dcp instanceof HTTP )
                    http = (HTTP) dcp;
            if ( http != null ) {
                addresses.put( GETSTYLES_NAME, http.getLinks().get( 0 ).getLinkage().getHref() );
            }
        }

        // get PutStyles operation address
        operation = om.getOperation( new QualifiedName( PUTSTYLES_NAME ) );

        if ( operation != null ) {
            dcps = operation.getDCP();
            for ( DCP dcp : dcps )
                if ( dcp instanceof HTTP )
                    http = (HTTP) dcp;
            if ( http != null ) {
                addresses.put( PUTSTYLES_NAME, http.getLinks().get( 0 ).getLinkage().getHref() );
            }
        }

        // get DescribeLayer operation address
        operation = om.getOperation( new QualifiedName( DESCRIBELAYER_NAME ) );

        if ( operation != null ) {
            dcps = operation.getDCP();
            for ( DCP dcp : dcps )
                if ( dcp instanceof HTTP )
                    http = (HTTP) dcp;
            if ( http != null ) {
                addresses.put( DESCRIBELAYER_NAME, http.getLinks().get( 0 ).getLinkage().getHref() );
            }
        }

    }

    public OGCCapabilities getCapabilities() {
        return capabilities;
    }

    private HashSet<CoordinateSystem> getSupportedCoordinateSystems( GetMap getMap )
                            throws UnknownCRSException {
        HashSet<CoordinateSystem> crs = new HashSet<CoordinateSystem>();
        List<Layer> layers = new LinkedList<Layer>();
        for ( GetMap.Layer l : getMap.getLayers() ) {
            if ( l == null ) {
                continue; // unclear when/why this can happen
            }
            Layer lay = capabilities.getLayer( l.getName() );
            while ( lay.getParent() != null ) {
                layers.add( lay );
                lay = lay.getParent();
            }
        }
        for ( Layer l : layers ) {
            for ( LayerBoundingBox bbox : l.getBoundingBoxes() ) {
                if ( bbox.getCoordinateSystem() != null ) {
                    crs.add( bbox.getCoordinateSystem() );
                }
            }
            for ( String srs : l.getSrs() ) {
                try {
                    crs.add( CRSFactory.create( srs ) );
                } catch ( Exception e ) {
                    LOG.logWarning( "CRS: " + crs + " is not known and will be ignored" );
                }
            }
        }
        return crs;
    }

    /**
     * the method performs the handling of the passed OGCWebServiceEvent directly and returns the result to the calling
     * class/method
     * 
     * @param request
     *            request (WMS, WCS, WFS, WCAS, WCTS, WTS, Gazetter) to perform
     * 
     * @throws OGCWebServiceException
     */
    public Object doService( OGCWebServiceRequest request )
                            throws OGCWebServiceException {
        Object o = null;
        if ( request instanceof GetMap ) {
            o = handleGetMap( (GetMap) request );
            o = WMSProtocolFactory.createGetMapResponse( request, null, o );
        } else if ( request instanceof GetFeatureInfo ) {
            o = handleFeatureInfo( (GetFeatureInfo) request );
            o = WMSProtocolFactory.createGetFeatureInfoResponse( request, null, (String) o );
        } else if ( request instanceof GetLegendGraphic ) {
            o = handleGetLegendGraphic( (GetLegendGraphic) request );
            o = WMSProtocolFactory.createGetLegendGraphicResponse( request, o );
        }
        /*
         * else if ( request instanceof WMSGetCapabilities) { handleGetCapabilities( (WMSGetCapabilities)request, client
         * ); } else if ( request instanceof GetStyles ) { handleGetStyles( (GetStyles)request, client ); } else if (
         * request instanceof PutStyles ) { handlePutStyles( (PutStyles)request, client ); } else if ( request
         * instanceof DescribeLayer ) { handleDescribeLayer( (DescribeLayer)request, client ); } else if ( request
         * instanceof GetLegendGraphic ) { handleGetLegendGraphic( (GetLegendGraphic)request, client ); }
         */

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
     * performs a GetMap request against the remote service. The result contains the map decoded in the desired format
     * as a byte array.
     * 
     * @param request
     *            GetMap request
     * @return the requested map-image
     * @throws OGCWebServiceException
     *             if the url in the request is <code>null</code>
     */
    protected Object handleGetMap( GetMap request )
                            throws OGCWebServiceException {

        URL url = null;

        if ( request.getVersion().equals( "1.0.0" ) ) {
            url = addresses.get( MAP_NAME );
        } else {
            url = addresses.get( GETMAP_NAME );
        }

        try {
            Envelope requestBBOX = request.getBoundingBox();
            HashSet<CoordinateSystem> crss = getSupportedCoordinateSystems( request );
            CoordinateSystem requestCRS = CRSFactory.create( request.getSrs() );
            requestBBOX = createEnvelope( requestBBOX.getMin(), requestBBOX.getMax(), requestCRS );
            if ( !crss.contains( requestCRS ) ) {
                Iterator<CoordinateSystem> iterator = crss.iterator();
                CoordinateSystem dataCRS = iterator.hasNext() ? iterator.next() : null;
                if ( dataCRS != null ) {
                    GeoTransformer transformer = new GeoTransformer( dataCRS );
                    GeoTransformer transformBack = new GeoTransformer( requestCRS );
                    Envelope dataBBOX = transformer.transform( requestBBOX, requestCRS, true );

                    int origWidth = request.getWidth();
                    int origHeight = request.getHeight();

                    double scale = calcScale( origWidth, origHeight, requestBBOX, requestCRS, DEFAULT_PIXEL_SIZE );
                    double newScale = calcScale( origWidth, origHeight, dataBBOX, dataCRS, DEFAULT_PIXEL_SIZE );
                    double ratio = scale / newScale;
                    if ( ratio < 1 ) {
                        ratio = newScale / scale;
                    }

                    LOG.logDebug( "Requesting transformed bounding box " + dataBBOX + " in srs "
                                  + dataCRS.getIdentifier() );
                    request.setBoundingBox( dataBBOX );
                    request.setSrs( dataCRS.getIdentifier() );
                    request.setWidth( (int) ( origWidth * ratio ) );
                    request.setHeight( (int) ( origHeight * ratio ) );
                    Object o = handleGetMap( request );
                    if ( o instanceof BufferedImage ) {
                        return transformBack.transform( (BufferedImage) o, dataBBOX, requestBBOX, origWidth,
                                                        origHeight, 16, 3, null );
                    }

                    return o;
                }
            }
        } catch ( UnknownCRSException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
        } catch ( CRSTransformationException e ) {
            LOG.logError( "An error occurred while transforming bounding boxes (this should not happen)", e );
        }

        String us = constructRequestURL( request.getRequestParameter(), validateHTTPGetBaseURL( url.toExternalForm() ) );

        LOG.logDebug( "remote wms getmap", us );

        if ( capabilities.getVersion().compareTo( "1.0.0" ) <= 0 ) {
            us = StringTools.replace( us, "TRANSPARENCY", "TRANSPARENT", false );
            us = StringTools.replace( us, "GetMap", "map", false );
            us = StringTools.replace( us, "image/", "", false );
        }

        Object result = null;
        try {
            HttpClient client = new HttpClient();
            enableProxyUsage( client, new URL( us ) );
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
            for ( int i = 0; i < tmp.length; i++ ) {
                if ( tmp[i].indexOf( "image" ) > -1 ) {
                    contentType = tmp[i];
                    break;
                }
                contentType = tmp[0];
            }

            if ( MimeTypeMapper.isImageType( contentType ) && MimeTypeMapper.isKnownImageType( contentType ) ) {
                MemoryCacheSeekableStream mcss = new MemoryCacheSeekableStream( is );
                RenderedOp rop = JAI.create( "stream", mcss );
                result = rop.getAsBufferedImage();
                mcss.close();
            } else {
                // extract remote (error) message if the response
                // contains a known mime type
                String res = "";
                if ( MimeTypeMapper.isKnownMimeType( contentType ) ) {
                    res = "Remote-WMS message: " + getInputStreamContent( is );
                } else {
                    res = Messages.getMessage( "REMOTEWMS_GETMAP_INVALID_RESULT", contentType, us );
                }
                throw new OGCWebServiceException( "RemoteWMS:handleGetMap", res );
            }
        } catch ( HttpException e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "REMOTEWMS_GETMAP_GENERAL_ERROR",
                                              capabilities.getServiceIdentification().getTitle(), us );
            throw new OGCWebServiceException( "RemoteWMS:handleGetMap", msg );
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "REMOTEWMS_GETMAP_GENERAL_ERROR",
                                              capabilities.getServiceIdentification().getTitle(), us );
            throw new OGCWebServiceException( "RemoteWMS:handleGetMap", msg );
        }
        // catch ( Exception e ) {
        // LOG.logError( e.getMessage(), e );
        // String msg = Messages.getMessage( "REMOTEWMS_GETMAP_GENERAL_ERROR",
        // capabilities.getServiceIdentification().getTitle(), us );
        // throw new OGCWebServiceException( "RemoteWMS:handleGetMap", msg );
        // }

        return result;
    }

    /**
     * reads feature infos from the remote WMS by performing a FeatureInfo request against it. As long the result of a
     * FeatureInfo request is generic (for usual it is som HTML) it isn't easy to combine the result with that of other
     * WMS's
     * 
     * @param request
     *            feature info request
     * @return the response of the GetFeatureInfo request.
     * @throws OGCWebServiceException
     *             if the request could not be excuted correctly.
     */
    protected Object handleFeatureInfo( GetFeatureInfo request )
                            throws OGCWebServiceException {

        URL url = null;

        if ( request.getVersion().equals( "1.0.0" ) ) {
            url = addresses.get( FEATUREINFO_NAME );
        } else {
            url = addresses.get( GETFEATUREINFO_NAME );
        }

        if ( url == null ) {
            String msg = Messages.getMessage( "REMOTEWMS_GFI_NOT_SUPPORTED",
                                              capabilities.getServiceIdentification().getTitle() );
            throw new OGCWebServiceException( msg );
        }

        try {
            GetMap gm = request.getGetMapRequestCopy();
            Envelope requestBBOX = gm.getBoundingBox();
            HashSet<CoordinateSystem> crss = getSupportedCoordinateSystems( gm );
            CoordinateSystem requestCRS = CRSFactory.create( gm.getSrs() );
            if ( !crss.contains( requestCRS ) ) {
                CoordinateSystem dataCRS = crss.iterator().next();
                if ( dataCRS != null ) {
                    GeoTransformer transformer = new GeoTransformer( dataCRS );
                    GeoTransformer transformBack = new GeoTransformer( requestCRS );
                    Envelope dataBBOX = transformer.transform( requestBBOX, requestCRS, true );

                    int origWidth = gm.getWidth();
                    int origHeight = gm.getHeight();

                    double scale = calcScale( origWidth, origHeight, requestBBOX, requestCRS, DEFAULT_PIXEL_SIZE );
                    double newScale = calcScale( origWidth, origHeight, dataBBOX, dataCRS, DEFAULT_PIXEL_SIZE );
                    double ratio = scale / newScale;

                    LOG.logDebug( "Requesting transformed bounding box " + dataBBOX + " in srs "
                                  + dataCRS.getIdentifier() );
                    gm.setBoundingBox( dataBBOX );
                    gm.setSrs( dataCRS.getIdentifier() );
                    gm.setWidth( (int) ( origWidth * ratio ) );
                    gm.setHeight( (int) ( origHeight * ratio ) );

                    Object o = handleFeatureInfo( request );
                    if ( o instanceof BufferedImage ) {
                        return transformBack.transform( (BufferedImage) o, dataBBOX, requestBBOX, origWidth,
                                                        origHeight, 16, 3, null );
                    }

                    return o;
                }
            }
        } catch ( UnknownCRSException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
        } catch ( CRSTransformationException e ) {
            LOG.logError( "An error occurred while transforming bounding boxes (this should not happen)", e );
        }

        String us = constructRequestURL( request.getRequestParameter(), validateHTTPGetBaseURL( url.toExternalForm() ) );

        String result = null;
        try {
            LOG.logDebug( "GetFeatureInfo: ", us );
            URL ur = new URL( us );
            // get map from the remote service
            NetWorker nw = new NetWorker( ur );
            byte[] b = nw.getDataAsByteArr( 20000 );
            String contentType = nw.getContentType();

            // extract content charset if available; otherwise use configured system charset
            String charset = null;
            LOG.logDebug( "content type: ", contentType );
            if ( contentType != null ) {
                String[] tmp = StringTools.toArray( contentType, ";", false );
                if ( tmp.length == 2 ) {
                    charset = tmp[1].substring( tmp[1].indexOf( '=' ) + 1, tmp[1].length() );
                } else {
                    charset = CharsetUtils.getSystemCharset();
                }
            } else {
                charset = CharsetUtils.getSystemCharset();
            }

            // commented out checks, we're trying to fix broken GFI responses here, after all
            // if ( contentType != null && contentType.toLowerCase().startsWith( "application/vnd.ogc.gml" ) ) {
            result = new String( b, charset );
            // } else {
            // throw new OGCWebServiceException( "RemoteWMS:handleFeatureInfo" );
            // }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "REMOTEWMS_GFI_GENERAL_ERROR",
                                              capabilities.getServiceIdentification().getTitle(), us );
            throw new OGCWebServiceException( "RemoteWMS:handleFeatureInfo", msg );
        }

        return result;
    }

    /**
     * reads the capabilities from the remote WMS by performing a GetCapabilities request against it.
     * 
     * @param request
     *            capabilities request
     * @return remote capabilities
     * @throws OGCWebServiceException
     *             if the request could not be executed correctly.
     */
    protected WMSCapabilities handleGetCapabilities( WMSGetCapabilities request )
                            throws OGCWebServiceException {

        URL url = null;

        if ( request.getVersion().equals( "1.0.0" ) ) {
            url = addresses.get( CAPABILITIES_NAME );
        } else {
            url = addresses.get( GETCAPABILITIES_NAME );
        }

        String us = constructRequestURL( request.getRequestParameter(), validateHTTPGetBaseURL( url.toExternalForm() ) );

        WMSCapabilities result = null;

        try {
            URL ur = new URL( us );
            // get map from the remote service
            NetWorker nw = new NetWorker( ur );
            byte[] b = nw.getDataAsByteArr( 20000 );
            String contentType = nw.getContentType();

            if ( MimeTypeMapper.isKnownMimeType( contentType ) ) {
                // create a WMSCapabilitiesTEMP instance from the result
                StringReader reader = new StringReader( new String( b ) );
                WMSCapabilitiesDocument doc = new WMSCapabilitiesDocument();
                doc.load( reader, XMLFragment.DEFAULT_URL );
                doc = WMSCapabilitiesDocumentFactory.getWMSCapabilitiesDocument( doc.getRootElement() );
                result = (WMSCapabilities) doc.parseCapabilities();
            } else {
                String msg = Messages.getMessage( "REMOTEWMS_GETCAPS_INVALID_CONTENTTYPE", contentType, us );
                throw new OGCWebServiceException( "RemoteWMS:handleGetCapabilities", msg );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "REMOTEWMS_GETCAPS_GENERAL_ERROR",
                                              capabilities.getServiceIdentification().getTitle(), us );
            throw new OGCWebServiceException( "RemoteWMS:handleGetCapabilities", msg );
        }

        return result;
    }

    /**
     * 
     * 
     * @param request
     *            get styles request (WMS 1.1.1 - SLD)
     * @return <code>null</code>
     * @throws OGCWebServiceException
     *             if the url in the request is <code>null</code>
     */
    protected Object handleGetStyles( GetStyles request )
                            throws OGCWebServiceException {

        URL url = addresses.get( GETSTYLES_NAME );

        if ( url == null ) {
            throw new OGCWebServiceException( "GetStyles is not supported by the RemoteWMS: "
                                              + capabilities.getServiceIdentification().getTitle() );
        }

        constructRequestURL( request.getRequestParameter(), validateHTTPGetBaseURL( url.toExternalForm() ) );

        // FIXME
        // TODO
        return null;
    }

    /**
     * 
     * 
     * @param request
     *            put styles request (WMS 1.1.1 - SLD)
     * @return <code>null</code>
     * @throws OGCWebServiceException
     *             if the url in the request is <code>null</code>
     */
    protected Object handlePutStyles( PutStyles request )
                            throws OGCWebServiceException {

        URL url = addresses.get( PUTSTYLES_NAME );

        if ( url == null ) {
            throw new OGCWebServiceException( "PUTSTYLES is not supported by the RemoteWMS: "
                                              + capabilities.getServiceIdentification().getTitle() );
        }

        constructRequestURL( request.getRequestParameter(), validateHTTPGetBaseURL( url.toExternalForm() ) );

        // FIXME
        // TODO

        return null;
    }

    /**
     * 
     * 
     * @param request
     *            describe layer request (WMS 1.1.1 - SLD)
     * @return <code>null</code>
     * @throws OGCWebServiceException
     *             if the url in the request is <code>null</code>
     */
    protected Object handleDescribeLayer( DescribeLayer request )
                            throws OGCWebServiceException {

        URL url = addresses.get( DESCRIBELAYER_NAME );

        if ( url == null ) {
            throw new OGCWebServiceException( "DESCRIBELAYER is not supported by the RemoteWMS: "
                                              + capabilities.getServiceIdentification().getTitle() );
        }

        constructRequestURL( request.getRequestParameter(), validateHTTPGetBaseURL( url.toExternalForm() ) );

        // FIXME
        // TODO

        return null;
    }

    /**
     * 
     * 
     * @param request
     *            describe layer request (WMS 1.1.1 - SLD)
     * @return <code>null</code>
     * @throws OGCWebServiceException
     *             if the url in the request is <code>null</code>
     */
    protected Object handleGetLegendGraphic( GetLegendGraphic request )
                            throws OGCWebServiceException {

        URL url = addresses.get( GETLEGENDGRAPHIC_NAME );

        if ( url == null ) {
            throw new OGCWebServiceException( "GETLEGENDGRAPHIC is not supported by the RemoteWMS: "
                                              + capabilities.getServiceIdentification().getTitle() );
        }

        String address = constructRequestURL( request.getRequestParameter(),
                                              validateHTTPGetBaseURL( url.toExternalForm() ) );

        try {
            URL theURL = new URL( address );
            LOG.logDebug( "Getting legend from remote WMS, URL: ", theURL );
            BufferedImage img = ImageIO.read( theURL );
            if ( img == null ) {
                XMLFragment doc = new XMLFragment( theURL );
                LOG.logDebug( "Got error message: ", doc.getAsPrettyString() );
                return new IOException( "Service exception recieved" );
            }
            return img;
        } catch ( MalformedURLException e ) {
            return e;
        } catch ( IOException e ) {
            return e;
        } catch ( SAXException e ) {
            return e;
        }
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
