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
package org.deegree.services.controller;

import static java.io.File.createTempFile;
import static java.util.Collections.emptyList;
import static org.deegree.commons.ows.exception.OWSException.NOT_FOUND;
import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.commons.tom.ows.Version.parseVersion;
import static org.reflections.util.ClasspathHelper.forClassLoader;
import static org.reflections.util.ClasspathHelper.forWebInfLib;
import static org.slf4j.LoggerFactory.getLogger;

import java.beans.Introspector;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.spi.IIORegistry;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;
import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.concurrent.Executor;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.DeegreeAALogoUtils;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.commons.xml.stax.XMLInputFactoryUtils;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.feature.stream.ThreadedFeatureInputStream;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.OwsManager;
import org.deegree.services.authentication.SecurityException;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.security.SecurityConfiguration;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.controller.utils.LoggingHttpRequestWrapper;
import org.deegree.services.controller.watchdog.RequestWatchdog;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType.RequestTimeoutMilliseconds;
import org.deegree.services.ows.OWS110ExceptionReportSerializer;
import org.deegree.services.resources.ResourcesServlet;
import org.deegree.workspace.standard.ModuleInfo;
import org.slf4j.Logger;

/**
 * Servlet that acts as OWS-HTTP communication end point and dispatcher to the {@link OWS} instances configured in the
 * active {@link DeegreeWorkspace}.
 * <p>
 * Calls to {@link #doGet(HttpServletRequest, HttpServletResponse)} and
 * {@link #doPost(HttpServletRequest, HttpServletResponse)} are processed as follows:
 * <nl>
 * <li>The DCP-type of the incoming request is determined. This must be one of the following:
 * <ul>
 * <li>KVP</li>
 * <li>XML</li>
 * <li>SOAP (OGC style, the XML request is the child element of the SOAP body)</li>
 * </ul>
 * </li>
 * <li>The responsible {@link OWS} instance is determined and one of the following methods is called:
 * <ul>
 * <li>{@link OWS#doKVP(Map, HttpServletRequest, HttpResponseBuffer, List)}</li>
 * <li>{@link OWS#doXML(XMLStreamReader, HttpServletRequest, HttpResponseBuffer, List)}</li>
 * <li>
 * {@link OWS#doSOAP(SOAPEnvelope, HttpServletRequest, HttpResponseBuffer, List, SOAPFactory)}</li>
 * </ul>
 * </li>
 * </nl>
 * </p>
 * 
 * @see OWS
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(debug = "logs the server startup, incoming requests and timing info, also enables enhanced request logging in $HOME/.deegree")
public class OGCFrontController extends HttpServlet {

    private static final Logger LOG = getLogger( OGCFrontController.class );

    private static final long serialVersionUID = -1379869403008798932L;

    /** used to decode (already URL-decoded) query strings */
    private static final String DEFAULT_ENCODING = "UTF-8";

    private static final String defaultTMPDir = System.getProperty( "java.io.tmpdir" );

    // file name that stores active workspaces (per webapp)
    private static final String ACTIVE_WS_CONFIG_FILE = "webapps.properties";

    private static OGCFrontController instance;

    // make fields transient, serialized servlets are a bad idea IMHO
    private transient DeegreeServiceControllerType mainConfig;

    private transient String hardcodedServicesUrl;

    private transient String hardcodedResourcesUrl;

    private transient final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<RequestContext>();

    private transient RequestWatchdog requestWatchdog;

    private transient SecurityConfiguration securityConfiguration;

    private transient OwsManager serviceConfiguration;

    private transient DeegreeWorkspace workspace;

    private transient String ctxPath;

    private transient Collection<ModuleInfo> modulesInfo;

    private transient String version;

    /**
     * Returns the only instance of this class.
     * 
     * @return the only instance of this class, never <code>null</code>
     * @throws RuntimeException
     *             if {@link #init()} has not been called
     */
    public static synchronized OGCFrontController getInstance() {
        if ( instance == null ) {
            throw new RuntimeException( "OGCFrontController has not been initialized yet." );
        }
        return instance;
    }

    /**
     * Returns the {@link RequestContext} associated with the calling thread.
     * <p>
     * NOTE: This method will only return a correct result if the calling thread originated in the
     * {@link #doGet(HttpServletRequest, HttpServletResponse)} or
     * {@link #doPost(HttpServletRequest, HttpServletResponse)} of this class (or has been spawned as a child thread by
     * such a thread).
     * </p>
     * 
     * @return the {@link RequestContext} associated with the calling thread
     */
    public static RequestContext getContext() {
        RequestContext context = instance.CONTEXT.get();
        LOG.debug( "Retrieving RequestContext for current thread " + Thread.currentThread() + ": " + context );
        return context;
    }

    /**
     * @return the service workspace
     */
    public static DeegreeWorkspace getServiceWorkspace() {
        return getInstance().workspace;
    }

    /**
     * @return the service configuration
     */
    public static OwsManager getServiceConfiguration() {
        return getInstance().serviceConfiguration;
    }

    /**
     * Returns the HTTP URL for accessing the {@link OGCFrontController}/{@link OWS} (using POST requests).
     * <p>
     * NOTE: This includes OGC service instance path info (service identifier), if available. This method will only
     * return a correct result if the calling thread originated in the
     * {@link #doGet(HttpServletRequest, HttpServletResponse)} or
     * {@link #doPost(HttpServletRequest, HttpServletResponse)} of this class (or has been spawned as a child thread by
     * such a thread).
     * </p>
     * 
     * @return URL, never <code>null</code> (without trailing slash or question mark)
     */
    public static String getHttpPostURL() {
        return getHttpURL();
    }

    /**
     * Returns the HTTP URL for accessing the {@link OGCFrontController}/{@link OWS} (using GET requests).
     * <p>
     * NOTE: This includes OGC service instance path info (service identifier), if available. This method will only
     * return a correct result if the calling thread originated in the
     * {@link #doGet(HttpServletRequest, HttpServletResponse)} or
     * {@link #doPost(HttpServletRequest, HttpServletResponse)} of this class (or has been spawned as a child thread by
     * such a thread).
     * </p>
     * 
     * @return URL (for GET requests), never <code>null</code> (with trailing question mark)
     */
    public static String getHttpGetURL() {
        return getHttpURL() + "?";
    }

    /**
     * Returns the HTTP URL for accessing the {@link ResourcesServlet}.
     * <p>
     * NOTE: This method will only return a correct result if the calling thread originated in the
     * {@link #doGet(HttpServletRequest, HttpServletResponse)} or
     * {@link #doPost(HttpServletRequest, HttpServletResponse)} of this class (or has been spawned as a child thread by
     * such a thread).
     * </p>
     * 
     * @return URL, never <code>null</code> (without trailing slash or question mark)
     */
    public static String getResourcesUrl() {
        return getContext().getResourcesUrl();
    }

    /**
     * Returns the {@link ModuleInfo}s for the deegree modules accessible by the webapp classloader.
     * 
     * @return module infos, never <code>null</code>, but can be empty
     */
    public static Collection<ModuleInfo> getModulesInfo() {
        return getInstance().modulesInfo;
    }

    private static void addHeaders( HttpServletResponse response ) {
        // add cache control headers
        response.addHeader( "Cache-Control", "no-cache, no-store" );
        // add deegree header
        response.addHeader( "deegree-version", getInstance().version );
    }

    /**
     * Handles HTTP GET requests.
     * <p>
     * An HTTP GET request implies that input parameters are specified as key-value pairs. However, at least one OGC
     * service specification allows the sending of XML requests via GET (see WCS 1.0.0 specification, section 6.3.3). In
     * this case, the query string contains no <code>key=value</code> pairs, but the (URL encoded) xml. The encoding
     * ensures that no <code>=</code> char (parameter/value delimiters) occur in the string.
     * </p>
     */
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {
        request = createHttpRequest( request );
        HttpResponseBuffer responseBuffer = new HttpResponseBuffer( response, request );

        try {
            long entryTime = System.currentTimeMillis();

            logHeaders( request );
            addHeaders( responseBuffer );
            responseBuffer = handleCompression( responseBuffer );

            String queryString = request.getQueryString();
            try {
                LOG.debug( "doGet(), query string: '" + queryString + "'" );

                if ( queryString == null ) {
                    OWSException ex = new OWSException( "The request did not contain any parameters.",
                                                        "MissingParameterValue" );
                    OWS ows = null;
                    try {
                        ows = determineOWSByPathQuirk( request );
                    } catch ( OWSException e ) {
                        sendException( null, e, responseBuffer, null );
                        return;
                    }
                    sendException( ows, ex, responseBuffer, null );
                    return;
                }

                // handle as XML, if the request starts with '<'
                boolean isXML = queryString.startsWith( "<" );
                List<FileItem> multiParts = checkAndRetrieveMultiparts( request );
                if ( isXML ) {
                    XMLStreamReader xmlStream = null;
                    String dummySystemId = "HTTP Get request from " + request.getRemoteAddr() + ":"
                                           + request.getRemotePort();
                    if ( multiParts != null && multiParts.size() > 0 ) {
                        InputStream is = multiParts.get( 0 ).getInputStream();
                        xmlStream = XMLInputFactoryUtils.newSafeInstance().createXMLStreamReader( dummySystemId, is );
                    } else {
                        // decode query string
                        String decodedString = URLDecoder.decode( queryString, DEFAULT_ENCODING );
                        StringReader reader = new StringReader( decodedString );
                        xmlStream = XMLInputFactoryUtils.newSafeInstance().createXMLStreamReader( dummySystemId, reader );
                    }
                    if ( isSOAPRequest( xmlStream ) ) {
                        dispatchSOAPRequest( xmlStream, request, responseBuffer, multiParts );
                    } else {
                        dispatchXMLRequest( xmlStream, request, responseBuffer, multiParts );
                    }
                } else {
                    // for GET requests, there is no standard way for defining the used encoding
                    Map<String, String> normalizedKVPParams = KVPUtils.getNormalizedKVPMap( request.getQueryString(),
                                                                                            DEFAULT_ENCODING );
                    LOG.debug( "parameter map: " + normalizedKVPParams );
                    dispatchKVPRequest( normalizedKVPParams, request, responseBuffer, multiParts, entryTime );
                }
            } catch ( XMLProcessingException e ) {
                // the message might be more meaningful
                OWSException ex = new OWSException( "The request did not contain KVP parameters and no parseable XML.",
                                                    "MissingParameterValue", "request" );
                sendException( null, ex, responseBuffer, null );
                return;
            } catch ( Throwable e ) {
                e.printStackTrace();
                LOG.debug( "Handling HTTP-GET request took: " + ( System.currentTimeMillis() - entryTime )
                           + " ms before sending exception." );
                LOG.debug( e.getMessage(), e );
                OWSException ex = new OWSException( e.getLocalizedMessage(), e, "InvalidRequest" );
                sendException( null, ex, responseBuffer, null );
                return;
            }
            LOG.debug( "Handling HTTP-GET request with status 'success' took: "
                       + ( System.currentTimeMillis() - entryTime ) + " ms." );
        } finally {
            getInstance().CONTEXT.remove();
            responseBuffer.flushBuffer();
            if ( mainConfig.isValidateResponses() != null && mainConfig.isValidateResponses() ) {
                validateResponse( responseBuffer );
            }
        }
    }

    private void logHeaders( HttpServletRequest request ) {
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "HTTP headers:" );
            Enumeration<String> headerEnum = request.getHeaderNames();
            while ( headerEnum.hasMoreElements() ) {
                String headerName = headerEnum.nextElement();
                LOG.debug( "- " + headerName + "='" + request.getHeader( headerName ) + "'" );
            }
        }
    }

    /**
     * Handles HTTP POST requests.
     * <p>
     * An HTTP POST request specifies parameters in the request body. OGC service specifications use three different
     * ways to encode the parameters:
     * <ul>
     * <li><b>KVP</b>: Parameters are given as <code>key=value</code> pairs which are separated using the &amp;
     * character. This is equivalent to standard HTTP GET requests, except that the parameters are not part of the query
     * string, but the POST body. In this case, the <code>content-type</code> field in the header must be
     * <code>application/x-www-form-urlencoded</code>.</li>
     * <li><b>XML</b>: The POST body contains an XML document. In this case, the <code>content-type</code> field in the
     * header has to be <code>text/xml</code>, but the implementation does not rely on this in order to be more tolerant
     * to clients.</li>
     * <li><b>SOAP</b>: TODO</li>
     * <li><b>Multipart</b>: TODO</li>
     * </ul>
     */
    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {
        request = createHttpRequest( request );
        HttpResponseBuffer responseBuffer = new HttpResponseBuffer( response, request );

        try {
            logHeaders( request );
            addHeaders( responseBuffer );
            responseBuffer = handleCompression( responseBuffer );

            LOG.debug( "doPost(), contentType: '" + request.getContentType() + "'" );

            long entryTime = System.currentTimeMillis();
            try {
                // check if content-type implies that it's a KVP request
                String contentType = request.getContentType();
                boolean isKVP = false;
                if ( contentType != null ) {
                    isKVP = request.getContentType().startsWith( "application/x-www-form-urlencoded" );
                }
                List<FileItem> multiParts = checkAndRetrieveMultiparts( request );
                InputStream is = request.getInputStream();

                if ( isKVP ) {
                    String queryString = readPostBodyAsString( is );
                    LOG.debug( "Treating POST input stream as KVP parameters. Raw input: '" + queryString + "'." );
                    Map<String, String> normalizedKVPParams = null;
                    String encoding = request.getCharacterEncoding();
                    if ( encoding == null ) {
                        LOG.debug( "Request has no further encoding information. Defaulting to '" + DEFAULT_ENCODING
                                   + "'." );
                        normalizedKVPParams = KVPUtils.getNormalizedKVPMap( queryString, DEFAULT_ENCODING );
                    } else {
                        LOG.debug( "Client encoding information :" + encoding );
                        normalizedKVPParams = KVPUtils.getNormalizedKVPMap( queryString, encoding );
                    }
                    dispatchKVPRequest( normalizedKVPParams, request, responseBuffer, multiParts, entryTime );
                } else {
                    // if( handle multiparts, get first body from multipart (?)
                    // body->requestDoc

                    InputStream requestInputStream = null;
                    if ( multiParts != null && multiParts.size() > 0 ) {
                        for ( int i = 0; i < multiParts.size() && requestInputStream == null; ++i ) {
                            FileItem item = multiParts.get( i );
                            if ( item != null ) {
                                LOG.debug( "Using multipart item: " + i + " with contenttype: " + item.getContentType()
                                           + " as the request." );
                                requestInputStream = item.getInputStream();
                            }
                        }
                    } else {
                        requestInputStream = is;
                    }
                    if ( requestInputStream == null ) {
                        String msg = "Could not create a valid inputstream from request "
                                     + ( ( multiParts != null && multiParts.size() > 0 ) ? "without" : "with" )
                                     + " multiparts.";
                        LOG.error( msg );
                        throw new IOException( msg );
                    }

                    String dummySystemId = "HTTP Post request from " + request.getRemoteAddr() + ":"
                                           + request.getRemotePort();
                    XMLStreamReader xmlStream = XMLInputFactoryUtils.newSafeInstance().createXMLStreamReader( dummySystemId,
                                                                                                              requestInputStream );
                    // skip to start tag of root element
                    XMLStreamUtils.nextElement( xmlStream );
                    if ( isSOAPRequest( xmlStream ) ) {
                        dispatchSOAPRequest( xmlStream, request, responseBuffer, multiParts );
                    } else {
                        dispatchXMLRequest( xmlStream, request, responseBuffer, multiParts );
                    }
                }
            } catch ( Throwable e ) {
                LOG.debug( "Handling HTTP-POST request took: " + ( System.currentTimeMillis() - entryTime )
                           + " ms before sending exception." );
                LOG.debug( e.getMessage(), e );
                OWSException ex = new OWSException( e.getLocalizedMessage(), "InvalidRequest" );
                OWS ows = null;
                try {
                    ows = determineOWSByPath( request );
                } catch ( OWSException e2 ) {
                    sendException( ows, e2, responseBuffer, null );
                    return;
                }
                sendException( ows, ex, responseBuffer, null );
            }
            LOG.debug( "Handling HTTP-POST request with status 'success' took: "
                       + ( System.currentTimeMillis() - entryTime ) + " ms." );
        } finally {
            instance.CONTEXT.remove();
            responseBuffer.flushBuffer();
            if ( mainConfig.isValidateResponses() != null && mainConfig.isValidateResponses() ) {
                validateResponse( responseBuffer );
            }
        }
    }

    private HttpServletRequest createHttpRequest( HttpServletRequest request ) {
        OwsGlobalConfigLoader loader = workspace.getNewWorkspace().getInitializable( OwsGlobalConfigLoader.class );
        if ( loader.getRequestLogger() != null ) {
            return createLoggingResponseWrapper( request );
        }
        return request;
    }

    private HttpServletRequest createLoggingResponseWrapper( HttpServletRequest request ) {
        OwsGlobalConfigLoader loader = workspace.getNewWorkspace().getInitializable( OwsGlobalConfigLoader.class );

        Boolean onlySuccessfulConfig = mainConfig.getRequestLogging().isOnlySuccessful();
        if ( onlySuccessfulConfig != null ) {
            LOG.warn( "The option OnlySuccessful of RequestLogging is ignored. All requests are logged." );
        }
        return new LoggingHttpRequestWrapper(  request, mainConfig.getRequestLogging().getOutputDirectory(), loader.getRequestLogger() );
    }

    private OWS determineOWSByPath( HttpServletRequest request )
                            throws OWSException {
        OWS ows = null;
        String pathInfo = request.getPathInfo();
        if ( pathInfo != null ) {
            // remove start "/"
            String serviceId = pathInfo.substring( 1 );
            ows = workspace.getNewWorkspace().getResource( OWSProvider.class, serviceId );
            if ( ows == null && serviceConfiguration.isSingleServiceConfigured() ) {
                ows = serviceConfiguration.getSingleConfiguredService();
            }
            if ( ows == null ) {
                String msg = "No service with identifier '" + serviceId + "' available.";
                throw new OWSException( msg, NOT_FOUND );
            }
        }
        return ows;
    }

    private OWS determineOWSByPathQuirk( HttpServletRequest request )
                            throws OWSException {
        OWS ows = null;
        String pathInfo = request.getPathInfo();
        if ( pathInfo != null ) {
            // remove start "/"
            String serviceId;
            // nice hack to work around the most stupid WFS 1.1.0 CITE tests
            // I'm sure there are a bazillion clients around that send out broken URLs, then validate the exception
            // responses, see the error of their ways and then send a proper request...
            if ( pathInfo.indexOf( "#" ) != -1 ) {
                serviceId = pathInfo.substring( 1, pathInfo.indexOf( "#" ) );
            } else if ( pathInfo.indexOf( "=" ) != -1 ) {
                serviceId = pathInfo.substring( 1, pathInfo.indexOf( "=" ) );
            } else {
                serviceId = pathInfo.substring( 1 );
            }

            ows = workspace.getNewWorkspace().getResource( OWSProvider.class, serviceId );
            if ( ows == null && serviceConfiguration.isSingleServiceConfigured() ) {
                ows = serviceConfiguration.getSingleConfiguredService();
            }
            if ( ows == null ) {
                String msg = "No service with identifier '" + serviceId + "' available.";
                throw new OWSException( msg, NOT_FOUND );
            }
        }
        return ows;
    }

    private static HttpResponseBuffer handleCompression( HttpResponseBuffer response ) {
        // TODO check if we should enable this in any case (XML, images, ...)
        // String encoding = request.getHeader( "Accept-Encoding" );
        // boolean supportsGzip = encoding != null && encoding.toLowerCase().contains( "gzip" );
        // if ( supportsGzip ) {
        // LOG.debug( "Using GZIP-compression" );
        // response = new GZipHttpServletResponse( response );
        // }
        return response;
    }

    private static String readPostBodyAsString( InputStream is )
                            throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedInputStream bis = new BufferedInputStream( is );
        byte[] readBuffer = new byte[1024];
        int numBytes = -1;
        while ( ( numBytes = bis.read( readBuffer ) ) != -1 ) {
            bos.write( readBuffer, 0, numBytes );
        }
        return bos.toString().trim();
    }

    /**
     * Checks if the given request is a multi part request. If so it will return the multiparts as a list of
     * {@link FileItem} else <code>null</code> will be returned.
     * 
     * @param request
     *            to check
     * @return a list of multiparts or <code>null</code> if it was not a multipart request.
     * @throws FileUploadException
     *             if there are problems reading/parsing the request or storing files.
     */
    private List<FileItem> checkAndRetrieveMultiparts( HttpServletRequest request )
                            throws FileUploadException {
        List<FileItem> result = null;
        if ( ServletFileUpload.isMultipartContent( request ) ) {
            // Create a factory for disk-based file items
            FileItemFactory factory = new DiskFileItemFactory();
            LOG.debug( "The incoming request is a multipart request." );
            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload( factory );

            // Parse the request
            result = upload.parseRequest( request );
            LOG.debug( "The multipart request contains: " + result.size() + " items." );
            OwsGlobalConfigLoader loader = workspace.getNewWorkspace().getInitializable( OwsGlobalConfigLoader.class );
            if ( loader.getRequestLogger() != null ) { // TODO, this is not actually something of the
                // request logger, what is
                // actually logged here?
                for ( FileItem item : result ) {
                    LOG.debug( item.toString() );
                }
            }
        }
        return result;
    }

    /**
     * Dispatches a KVP request to the responsible {@link OWS}. Both GET and POST are handled by this method.
     * <p>
     * The responsible {@link OWS} is identified according to this strategy:
     * <nl>
     * <li>If a <code>SERVICE</code> attribute is present, it is used to determine the controller.</li>
     * <li>If no <code>SERVICE</code> attribute is present, the value of the <code>REQUEST</code> attribute is taken to
     * determine the controller.</li>
     * </nl>
     * </p>
     * 
     * @param normalizedKVPParams
     * @param requestWrapper
     * @param response
     * @param multiParts
     * @throws ServletException
     * @throws IOException
     */
    private void dispatchKVPRequest( Map<String, String> normalizedKVPParams, HttpServletRequest requestWrapper,
                                     HttpResponseBuffer response, List<FileItem> multiParts, long entryTime )
                            throws ServletException, IOException {

        OWS ows = null;
        try {
            ows = determineOWSByPath( requestWrapper );
        } catch ( OWSException e ) {
            sendException( null, e, response, null );
            return;
        }

        CredentialsProvider credentialsProvider = securityConfiguration == null ? null
                                                                               : securityConfiguration.getCredentialsProvider();

        // extract (deegree specific) security information and bind to current thread
        try {
            // TODO handle multiple authentication methods
            Credentials cred = null;
            if ( credentialsProvider != null ) {
                cred = credentialsProvider.doKVP( normalizedKVPParams, requestWrapper, response );
            }
            LOG.debug( "credentials: " + cred );
            bindContextToThread( requestWrapper, cred );

            String service = normalizedKVPParams.get( "SERVICE" );
            String request = normalizedKVPParams.get( "REQUEST" );

            if ( ows == null ) {
                // first try service parameter, SERVICE-parameter is mandatory for each service and request (except WMS
                // 1.0.0)

                if ( request != null && request.equalsIgnoreCase( "getlogo" ) ) {
                    response.setContentType( "text/plain" );
                    DeegreeAALogoUtils.print( response.getWriter() );
                    return;
                }

                if ( service != null ) {
                    List<OWS> services = serviceConfiguration.getByServiceType( service );
                    if ( services != null && !services.isEmpty() ) {
                        if ( services.size() > 1 ) {
                            String msg = "Cannot dispatch request for service '" + service
                                         + "' -- currently, multiple services of this type "
                                         + "are active. Please add the service id to the URL to "
                                         + "choose one of the services.";
                            throw new ServletException( msg );
                        }
                        ows = services.get( 0 );
                    }
                } else {
                    // dispatch according to REQUEST-parameter
                    if ( request != null ) {
                        List<OWS> services = serviceConfiguration.getByRequestName( request );
                        if ( services != null && !services.isEmpty() ) {
                            if ( services.size() > 1 ) {
                                String msg = "Cannot dispatch KVP request of type '" + request
                                             + "' -- currently, multiple services for this request type "
                                             + "are active. Please add the service id to the URL to "
                                             + "choose one of the services.";
                                throw new ServletException( msg );
                            }
                            ows = services.get( 0 );
                        }
                    }
                }
            }

            if ( service != null && serviceConfiguration.getByServiceType( service ) == null ) {
                OWSException ex = new OWSException( "No service for service type '" + service
                                                    + "' is configured / active.", "InvalidParameterValue", "service" );
                sendException( ows, ex, response, null );
                return;
            }

            if ( request != null && !request.equalsIgnoreCase( "GetCapabilities" )
                 && serviceConfiguration.getByRequestName( request ) == null ) {
                OWSException ex = new OWSException( "No service for request type '" + request
                                                    + "' is configured / active.", "InvalidParameterValue", "request" );
                sendException( ows, ex, response, null );
                return;
            }

            if ( ows == null ) {
                OWSException ex = new OWSException( "No service for service type '" + service + "' and request type '"
                                                    + request + "' is configured / active.", "MissingParameterValue",
                                                    "service" );
                sendException( null, ex, response, null );
                return;
            }

            // Seems not all services test their incoming requests completely, so the check is done here.
            // Since for WMS the service parameter is not always mandatory, here's the exception.
            // Once all services properly check their requests (WFS and SOS have this problem), this workaround can be
            // removed.
            if ( service == null
                 && !( ( (OWSProvider) ows.getMetadata().getProvider() ).getImplementationMetadata().getImplementedServiceName()[0].equalsIgnoreCase( "WMS" ) ) ) {
                OWSException ex = new OWSException( "The 'SERVICE' parameter is missing.", "MissingParameterValue",
                                                    "service" );
                sendException( ows, ex, response, null );
                return;
            }

            if ( request == null ) {
                OWSException ex = new OWSException( "The 'REQUEST' parameter is absent.", "MissingParameterValue",
                                                    "request" );
                sendException( ows, ex, response, null );
                return;
            }

            LOG.debug( "Dispatching request to OWS class: " + ows.getClass().getName() );
            long dispatchTime = FrontControllerStats.requestDispatched();
            try {
                watchTimeout( ows, request );
                ows.doKVP( normalizedKVPParams, requestWrapper, response, multiParts );
            } finally {
                FrontControllerStats.requestFinished( dispatchTime );
                unwatchTimeout();
            }
        } catch ( SecurityException e ) {
            if ( credentialsProvider != null ) {
                LOG.debug( "A security exception was thrown, let the credential provider handle the job." );
                credentialsProvider.handleException( response, e );
            } else {
                LOG.debug( "A security exception was thrown ( " + e.getLocalizedMessage()
                           + " but no credentials provider was configured, sending generic ogc exception." );
                sendException( ows, new OWSException( e.getLocalizedMessage(), NO_APPLICABLE_CODE ), response, null );
            }
        }
    }

    private static void validateResponse( HttpResponseBuffer responseWrapper ) {
        responseWrapper.validate();
    }

    /**
     * Dispatches an XML request to the responsible {@link OWS}. Both GET and POST are handled by this method.
     * <p>
     * The responsible {@link OWS} is identified by the namespace of the root element.
     * 
     * @param xmlStream
     *            provides access to the XML request, cursor points to the START_ELEMENT event of the root element
     * @param requestWrapper
     * @param response
     * @param multiParts
     * @throws ServletException
     * @throws IOException
     */
    private void dispatchXMLRequest( XMLStreamReader xmlStream, HttpServletRequest requestWrapper,
                                     HttpResponseBuffer response, List<FileItem> multiParts )
                            throws ServletException, IOException {

        OWS ows = null;
        try {
            ows = determineOWSByPath( requestWrapper );
        } catch ( OWSException e ) {
            sendException( null, e, response, null );
            return;
        }

        CredentialsProvider credentialsProvider = securityConfiguration == null ? null
                                                                               : securityConfiguration.getCredentialsProvider();

        try {
            // TODO handle multiple authentication methods
            Credentials cred = null;
            if ( credentialsProvider != null ) {
                cred = credentialsProvider.doXML( xmlStream, requestWrapper, response );
            }
            LOG.debug( "credentials: " + cred );
            bindContextToThread( requestWrapper, cred );

            // AbstractOGCServiceController subController = null;
            // extract (deegree specific) security information and bind to current thread
            // String user = xmlStream.getAttributeValue( XMLConstants.NULL_NS_URI, "user" );
            // String password = xmlStream.getAttributeValue( XMLConstants.NULL_NS_URI, "password" );
            // String sessionId = xmlStream.getAttributeValue( XMLConstants.NULL_NS_URI, "sessionId" );

            String ns = xmlStream.getNamespaceURI();
            if ( ows == null ) {
                List<OWS> services = serviceConfiguration.getByRequestNS( ns );
                if ( services == null || services.isEmpty() ) {
                    String msg = "Cannot dispatch XML request with namespace '" + ns
                                 + "' -- currently, no service for this request namespace is configured / active.";
                    throw new ServletException( msg );
                }
                if ( services.size() > 1 ) {
                    String msg = "Cannot dispatch XML request with namespace '" + ns
                                 + "' -- currently, multiple services for this namespace "
                                 + "are active. Please add the service id to the URL to "
                                 + "choose one of the services.";
                    throw new ServletException( msg );
                }
                ows = services.get( 0 );
            }
            if ( ows != null ) {
                LOG.debug( "Dispatching request to OWS: " + ows.getClass().getName() );
                long dispatchTime = FrontControllerStats.requestDispatched();
                try {
                    watchTimeout( ows, xmlStream.getLocalName() );
                    ows.doXML( xmlStream, requestWrapper, response, multiParts );
                } finally {
                    FrontControllerStats.requestFinished( dispatchTime );
                    unwatchTimeout();
                }
            }
        } catch ( SecurityException e ) {
            if ( credentialsProvider != null ) {
                LOG.debug( "A security exception was thrown, let the credential provider handle the job." );
                credentialsProvider.handleException( response, e );
            } else {
                LOG.debug( "A security exception was thrown ( " + e.getLocalizedMessage()
                           + " but no credentials provider was configured, sending generic ogc exception." );
                sendException( ows, new OWSException( e.getLocalizedMessage(), NO_APPLICABLE_CODE ),
                               response, null );
            }
        }
    }

    /**
     * Dispatches a SOAP request to the responsible {@link OWS}. Both GET and POST are handled by this method.
     * <p>
     * The responsible {@link OWS} is identified by the namespace of the first child of the SOAP body element.
     * 
     * @param xmlStream
     *            provides access to the SOAP request, cursor points to the START_ELEMENT event of the root element
     * @param requestWrapper
     * @param response
     * @param multiParts
     * @throws ServletException
     * @throws IOException
     */
    private void dispatchSOAPRequest( XMLStreamReader xmlStream, HttpServletRequest requestWrapper,
                                      HttpResponseBuffer response, List<FileItem> multiParts )
                            throws ServletException, IOException {

        OWS ows = null;
        try {
            ows = determineOWSByPath( requestWrapper );
        } catch ( OWSException e ) {
            sendException( null, e, response, null );
            return;
        }

        // TODO integrate authentication handling (CredentialsProvider)
        LOG.debug( "Handling SOAP request." );
        XMLAdapter requestDoc = new XMLAdapter( xmlStream );
        OMElement root = requestDoc.getRootElement();
        SOAPFactory factory = null;
        String ns = root.getNamespace().getNamespaceURI();
        if ( "http://schemas.xmlsoap.org/soap/envelope/".equals( ns ) ) {
            factory = new SOAP11Factory();
        } else {
            factory = new SOAP12Factory();
        }

        StAXSOAPModelBuilder soap = new StAXSOAPModelBuilder( root.getXMLStreamReaderWithoutCaching(), factory,
                                                              factory.getSoapVersionURI() );

        SOAPEnvelope env = soap.getSOAPEnvelope();

        CredentialsProvider credentialsProvider = securityConfiguration == null ? null
                                                                               : securityConfiguration.getCredentialsProvider();

        try {
            // TODO handle multiple authentication methods
            Credentials creds = null;
            if ( credentialsProvider != null ) {
                creds = credentialsProvider.doSOAP( env, requestWrapper );
            }
            LOG.debug( "credentials: " + creds );
            bindContextToThread( requestWrapper, creds );

            // extract (deegree specific) security information and bind to current thread
            // String user = null;
            // String password = null;
            // String sessionId = null;
            // SOAPHeader header = envelope.getHeader();
            // if ( header != null ) {
            // OMElement userElement = header.getFirstChildWithName( new QName( "http://www.deegree.org/security",
            // "user" ) );
            // if ( userElement != null ) {
            // user = userElement.getText();
            // }
            // OMElement passwordElement = header.getFirstChildWithName( new QName( "http://www.deegree.org/security",
            // "password" ) );
            // if ( passwordElement != null ) {
            // password = passwordElement.getText();
            // }
            // OMElement sessionIdElement = header.getFirstChildWithName( new QName( "http://www.deegree.org/security",
            // "sessionId" ) );
            // if ( sessionIdElement != null ) {
            // sessionId = sessionIdElement.getText();
            // }
            // }

            if ( ows == null ) {
                String requestNs = env.getSOAPBodyFirstElementNS().getNamespaceURI();
                List<OWS> services = serviceConfiguration.getByRequestNS( requestNs );
                if ( services == null || services.isEmpty() ) {
                    String msg = "Cannot dispatch SOAP request with body namespace '" + requestNs
                                 + "' -- currently, no service for this namespace '" + requestNs
                                 + "' is configured / active.";
                    throw new ServletException( msg );
                }
                if ( services.size() > 1 ) {
                    String msg = "Cannot dispatch SOAP request with body namespace '" + requestNs
                                 + "' -- currently, multiple services for this namespace '" + requestNs
                                 + "' are active. Please add the service id to the URL to "
                                 + "choose one of the services.";
                    throw new ServletException( msg );
                }
                ows = services.get( 0 );
            }

            LOG.debug( "Dispatching request to OWS class: " + ows.getClass().getName() );
            long dispatchTime = FrontControllerStats.requestDispatched();
            try {
                watchTimeout( ows, env.getSOAPBodyFirstElementLocalName() );
                ows.doSOAP( env, requestWrapper, response, multiParts, factory );
            } finally {
                FrontControllerStats.requestFinished( dispatchTime );
            }
        } catch ( SecurityException e ) {
            if ( credentialsProvider != null ) {
                LOG.debug( "A security exception was thrown, let the credential provider handle the job." );
                credentialsProvider.handleException( response, e );
            } else {
                LOG.debug( "A security exception was thrown ( " + e.getLocalizedMessage()
                           + " but no credentials provider was configured, sending generic ogc exception." );
                sendException( ows, new OWSException( e.getLocalizedMessage(), NO_APPLICABLE_CODE ), response, null );
            }
        }
    }

    private static boolean isSOAPRequest( XMLStreamReader xmlStream ) {
        String ns = xmlStream.getNamespaceURI();
        String localName = xmlStream.getLocalName();
        return ( "http://schemas.xmlsoap.org/soap/envelope/".equals( ns ) || "http://www.w3.org/2003/05/soap-envelope".equals( ns ) )
               && "Envelope".equals( localName );
    }

    @Override
    public void init( ServletConfig config )
                            throws ServletException {

        instance = this;

        try {
            super.init( config );
            ctxPath = config.getServletContext().getContextPath();
            LOG.info( "--------------------------------------------------------------------------------" );
            DeegreeAALogoUtils.logInfo( LOG );
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "deegree modules" );
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "" );
            try {
                modulesInfo = extractModulesInfo( config.getServletContext() );
            } catch ( Throwable t ) {
                LOG.error( "Unable to extract deegree module information: " + t.getMessage() );
                modulesInfo = emptyList();
            }
            for ( ModuleInfo moduleInfo : modulesInfo ) {
                LOG.info( "- " + moduleInfo.toString() );
                if ( "deegree-services-commons".equals( moduleInfo.getArtifactId() ) ) {
                    version = moduleInfo.getVersion();
                }
            }
            if ( version == null ) {
                version = "unknown";
            }
            LOG.info( "" );
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "System info" );
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "" );
            LOG.info( "- java version       " + System.getProperty( "java.version" ) + " ("
                      + System.getProperty( "java.vendor" ) + ")" );
            LOG.info( "- operating system   " + System.getProperty( "os.name" ) + " ("
                      + System.getProperty( "os.version" ) + ", " + System.getProperty( "os.arch" ) + ")" );
            LOG.info( "- container          " + config.getServletContext().getServerInfo() );
            LOG.info( "- webapp path        " + ctxPath );
            LOG.info( "- default encoding   " + DEFAULT_ENCODING );
            LOG.info( "- system encoding    " + Charset.defaultCharset().displayName() );
            LOG.info( "- temp directory     " + defaultTMPDir );
            LOG.info( "- XMLOutputFactory   " + XMLOutputFactory.newInstance().getClass().getCanonicalName() );
            LOG.info( "- XMLInputFactory    " + XMLInputFactory.newInstance().getClass().getCanonicalName() );
            LOG.info( "" );

            initWorkspace();
            DeegreeWorkspaceUpdater.INSTANCE.init( workspace );

        } catch ( NoClassDefFoundError e ) {
            LOG.error( "Initialization failed!" );
            LOG.error( "You probably forgot to add a required .jar to the WEB-INF/lib directory." );
            LOG.error( "The resource that could not be found was '{}'.", e.getMessage() );
            LOG.debug( "Stack trace:", e );

            throw new ServletException( e );
        } catch ( Exception e ) {
            LOG.error( "Initialization failed!" );
            LOG.error( "An unexpected error was caught, stack trace:", e );

            throw new ServletException( e );
        } finally {
            CONTEXT.remove();
        }
    }

    private Collection<ModuleInfo> extractModulesInfo( ServletContext servletContext )
                            throws IOException, URISyntaxException {

        if ( servletContext.getServerInfo() != null && servletContext.getServerInfo().contains( "WebLogic" ) ) {
            LOG.debug( "Running on weblogic. Not extracting module info from classpath, but from WEB-INF/lib." );
            return ModuleInfo.extractModulesInfo( forWebInfLib( servletContext ) );
        }
        return ModuleInfo.extractModulesInfo( forClassLoader() );
    }

    private void initWorkspace()
                            throws IOException, URISyntaxException, ResourceInitException {

        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Initializing workspace" );
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "" );
        LOG.info( "- deegree workspace root  " + DeegreeWorkspace.getWorkspaceRoot() );
        File wsRoot = new File( DeegreeWorkspace.getWorkspaceRoot() );
        if ( !wsRoot.isDirectory() && !wsRoot.mkdirs() ) {
            LOG.warn( "*** The workspace root is not a directory and could not be created. ***" );
            LOG.warn( "*** This will lead to problems when you'll try to download workspaces. ***" );
        }
        if ( wsRoot.isDirectory() && !wsRoot.canWrite() ) {
            LOG.warn( "*** The workspace root is not writable. ***" );
            LOG.warn( "*** This will lead to problems when you'll try to download workspaces. ***" );
        }

        workspace = getActiveWorkspace();
        workspace.initAll();
        serviceConfiguration = workspace.getNewWorkspace().getResourceManager( OwsManager.class );
        OwsGlobalConfigLoader loader = workspace.getNewWorkspace().getInitializable( OwsGlobalConfigLoader.class );
        mainConfig = loader.getMainConfig();
        if ( mainConfig != null ) {
            initHardcodedUrls( mainConfig );
        }
        if ( mainConfig != null && !mainConfig.getRequestTimeoutMilliseconds().isEmpty() ) {
            LOG.info( "Initializing request watchdog." );
            initRequestWatchdog( mainConfig.getRequestTimeoutMilliseconds() );
        } else {
            LOG.info( "Not initializing request watchdog. No request time-outs configured." );
        }
        LOG.info( "" );
    }

    private void initHardcodedUrls( DeegreeServiceControllerType mainConfig ) {
        if ( mainConfig.getReportedUrls() != null ) {
            hardcodedServicesUrl = mainConfig.getReportedUrls().getServices();
            hardcodedResourcesUrl = mainConfig.getReportedUrls().getResources();
            hardcodedServicesUrl = removeTrailingSlashOrQuestionMark( hardcodedServicesUrl );
            hardcodedResourcesUrl = removeTrailingSlashOrQuestionMark( hardcodedResourcesUrl );
        } else if ( mainConfig.getDCP() != null ) {
            if ( mainConfig.getDCP().getHTTPGet() != null ) {
                hardcodedServicesUrl = mainConfig.getDCP().getHTTPGet();
            } else if ( mainConfig.getDCP().getHTTPPost() != null ) {
                hardcodedServicesUrl = mainConfig.getDCP().getHTTPPost();
            } else if ( mainConfig.getDCP().getSOAP() != null ) {
                hardcodedServicesUrl = mainConfig.getDCP().getSOAP();
            }
            if ( hardcodedServicesUrl != null ) {
                hardcodedServicesUrl = removeTrailingSlashOrQuestionMark( hardcodedServicesUrl );
                hardcodedResourcesUrl = hardcodedServicesUrl + "/../resources";
            }
        }
    }

    private void initRequestWatchdog( final List<RequestTimeoutMilliseconds> timeoutConfigs ) {
        requestWatchdog = new RequestWatchdog( timeoutConfigs );
        requestWatchdog.init();
    }

    private void destroyWorkspace() {
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Destroying workspace" );
        LOG.info( "--------------------------------------------------------------------------------" );
        workspace.destroyAll();
        if ( requestWatchdog != null ) {
            requestWatchdog.destroy();
        }
        LOG.info( "" );
    }

    /**
     * Re-initializes the whole workspace, effectively reloading the whole configuration.
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ServletException
     */
    public void reload()
                            throws IOException, URISyntaxException, ServletException {
        destroyWorkspace();
        try {
            initWorkspace();
            DeegreeWorkspaceUpdater.INSTANCE.notifyWorkspaceChange( workspace );
        } catch ( ResourceInitException e ) {
            throw new ServletException( e.getLocalizedMessage(), e.getCause() );
        }
    }

    /**
     * Checks for deleted, modified or added resource configs and updates the workspace resources
     * accordingly.
     *
     * @throws IOException
     * @throws URISyntaxException
     * @throws ServletException
     */
    public void update()
                            throws IOException, URISyntaxException, ServletException {
        if ( DeegreeWorkspaceUpdater.INSTANCE.isWorkspaceChange(getActiveWorkspace()) ) {
            // do complete reload
            destroyWorkspace();
            try {
                initWorkspace();
            } catch ( ResourceInitException e ) {
                throw new ServletException( e.getLocalizedMessage(), e.getCause() );
            }
            DeegreeWorkspaceUpdater.INSTANCE.notifyWorkspaceChange( workspace );
        } else {
            // no complete reload - update only
            DeegreeWorkspaceUpdater.INSTANCE.updateWorkspace( workspace );
        }
    }

    /**
     * Determines the active {@link DeegreeWorkspace} for this webapp.
     * <p>
     * The following steps are performed to find it (in order). Strategy stops on first match:
     * <nl>
     * <li>Analyse file $DEEGREE_WORKSPACE_ROOT/webapps.properties (and lookup entry for this webapp context name)</li>
     * <li>Match workspace by webapp context name</li>
     * <li>Read WEB-INF/workspace_name (in webapp directory)</li>
     * </nl>
     * If this doesn't lead to an existing workspace directory, the following additional steps are performed:
     * <nl>
     * <li>Check for WEB-INF/workspace</li>
     * <li>Check for WEB-INF/conf</li>
     * </nl>
     * </p>
     * 
     * @return workspace (uninitialized), never <code>null</code>
     * @throws IOException
     * @throws URISyntaxException
     */
    private DeegreeWorkspace getActiveWorkspace()
                            throws IOException, URISyntaxException {

        String wsName = null;
        File wsRoot = new File( DeegreeWorkspace.getWorkspaceRoot() );
        if ( wsRoot.isDirectory() ) {
            File activeWsConfigFile = new File( wsRoot, ACTIVE_WS_CONFIG_FILE );

            Properties props = loadWebappToWsMappings( activeWsConfigFile );
            wsName = props.getProperty( ctxPath );

            if ( wsName != null ) {
                LOG.info( "Active workspace determined by webapp-to-workspace mapping file: " + activeWsConfigFile );
            } else {
                LOG.debug( "No webapp-to-workspace mappings file. Trying alternative methods." );
                if ( !ctxPath.isEmpty() ) {
                    String webappName = ctxPath;
                    if ( webappName.startsWith( "/" ) ) {
                        webappName = webappName.substring( 1 );
                    }
                    if ( !webappName.isEmpty() ) {
                        File file = new File( wsRoot, webappName );
                        LOG.debug( "Matching by webapp name ('" + file + "'). Checking for workspace directory '"
                                   + file + "'" );
                        if ( file.exists() ) {
                            wsName = webappName;
                            LOG.info( "Active workspace determined by matching webapp name (" + webappName
                                      + ") with available workspaces." );
                        }
                    }
                }
            }
        } else {
            String msg = "Workspace root directory ('" + wsRoot + "') does not exist or does not denote a directory.";
            LOG.info( msg );
        }

        File fallbackDir = null;
        if ( wsName == null ) {
            wsName = getActiveWorkspaceName();
            if ( wsName != null && new File( wsRoot, wsName ).exists() ) {
                LOG.info( "Active workspace determined by matching workspace name from WEB-INF/workspace_name ("
                          + wsName + ") with available workspaces." );
            } else {
                LOG.info( "Active workspace in webapp." );
                fallbackDir = new File( resolveFileLocation( "WEB-INF/workspace", getServletContext() ).toURI() );
                if ( !fallbackDir.exists() ) {
                    LOG.debug( "Trying legacy-style workspace directory (WEB-INF/conf)" );
                    fallbackDir = new File( resolveFileLocation( "WEB-INF/conf", getServletContext() ).toURI() );
                } else {
                    LOG.debug( "Using new-style workspace directory (WEB-INF/workspace)" );
                }
            }
        }

        DeegreeWorkspace ws = DeegreeWorkspace.getInstance( wsName, fallbackDir );
        LOG.info( "Using workspace '{}' at '{}'", ws.getName(), ws.getLocation() );
        return ws;
    }

    private String getActiveWorkspaceName()
                            throws URISyntaxException, IOException {
        String wsName = null;
        File wsNameFile = new File( resolveFileLocation( "WEB-INF/workspace_name", getServletContext() ).toURI() );
        if ( wsNameFile.exists() ) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader( new FileReader( wsNameFile ) );
                wsName = reader.readLine();
                if ( wsName != null ) {
                    wsName = wsName.trim();
                }
            } finally {
                IOUtils.closeQuietly( reader );
            }
            LOG.info( "Using workspace name {} (defined in WEB-INF/workspace_name)", wsName, wsNameFile );
        } else {
            LOG.info( "Using default workspace (WEB-INF/workspace_name does not exist)" );
        }
        return wsName;
    }

    /**
     * Associates the specified workspace with the current webapp.
     * <p>
     * Setting is saved in properties file <code>webapps.properties</code> in the deegree workspace root folder.
     * </p>
     * 
     * TODO file locking to prevent race conditions when this method gets called simultaneously in different webapps
     * 
     * @param wsName
     *            name of the workspace, must not be <code>null</code>
     */
    public void setActiveWorkspaceName( String wsName )
                            throws IOException {

        File wsRoot = new File( DeegreeWorkspace.getWorkspaceRoot() );
        if ( !wsRoot.exists() || !wsRoot.isDirectory() ) {
            String msg = "Workspace root directory ('" + wsRoot + "') does not exist or does not denote a directory.";
            LOG.error( msg );
            throw new IOException( msg );
        }

        File activeWsConfigFile = new File( wsRoot, ACTIVE_WS_CONFIG_FILE );
        Properties props = loadWebappToWsMappings( activeWsConfigFile );
        String oldWsName = props.getProperty( ctxPath );
        if ( oldWsName == null || !oldWsName.equals( wsName ) ) {
            props.put( ctxPath, wsName );
            writeWebappToWsMappings( props, activeWsConfigFile );
        }
    }

    private static void writeWebappToWsMappings( Properties props, File file )
                            throws IOException {

        if ( file.exists() && !file.canWrite() ) {
            String msg = "Webapp-to-workspace mappings file ('" + file + "') is not a writable file.";
            LOG.error( msg );
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream( file );
            props.store( fos, null );
        } catch ( IOException e ) {
            String msg = "Error writing webapp-to-workspace mappings to file '" + file + "': " + e.getMessage();
            LOG.error( msg );
            throw new IOException( msg, e );
        } finally {
            IOUtils.closeQuietly( fos );
        }
        LOG.info( "Successfully saved webapp-to-workspace mapping in file '" + file + "'." );
    }

    private static Properties loadWebappToWsMappings( File file )
                            throws IOException {

        Properties props = new Properties();
        if ( file.exists() ) {
            LOG.info( "Loading webapp-to-workspace mappings from file '" + file + "'" );
            FileInputStream fis = null;
            try {
                fis = new FileInputStream( file );
                props = new Properties();
                props.load( fis );
            } catch ( IOException e ) {
                String msg = "Error reading webapp-to-workspace mappings from file '" + file + "': " + e.getMessage();
                LOG.error( msg );
                throw new IOException( msg, e );
            } finally {
                IOUtils.closeQuietly( fis );
            }
        }
        return props;
    }

    @Override
    public void destroy() {
        super.destroy();
        destroyWorkspace();
        if ( mainConfig.isPreventClassloaderLeaks() == null || mainConfig.isPreventClassloaderLeaks() ) {
            plugClassLoaderLeaks();
        }
    }

    /**
     * Apply workarounds for classloader leaks, see eg. <a
     * href="http://java.jiderhamn.se/2012/02/26/classloader-leaks-v-common-mistakes-and-known-offenders/">this blog
     * post</a>.
     */
    private void plugClassLoaderLeaks() {
        // if the feature store manager does this, it breaks
        try {
            ThreadedFeatureInputStream.shutdown();
        } catch ( Throwable e ) {
            // just eat it
        }
        Executor.getInstance().shutdown();

        LogFactory.releaseAll();

        // image io
        Iterator<Class<?>> i = IIORegistry.getDefaultInstance().getCategories();
        while ( i.hasNext() ) {
            Class<?> c = i.next();
            Iterator<?> k = IIORegistry.getDefaultInstance().getServiceProviders( c, false );
            while ( k.hasNext() ) {
                Object o = k.next();
                if ( o.getClass().getClassLoader() == getClass().getClassLoader() ) {
                    IIORegistry.getDefaultInstance().deregisterServiceProvider( o );
                    LOG.debug( "Deregistering " + o );
                    k = IIORegistry.getDefaultInstance().getServiceProviders( c, false );
                }
            }
        }

        // JSF
        Introspector.flushCaches();

        // Batik
        try {
            Class<?> cls = Class.forName( "org.apache.batik.util.CleanerThread" );
            if ( cls != null ) {
                Field field = cls.getDeclaredField( "thread" );
                field.setAccessible( true );
                Object obj = field.get( null );
                if ( obj != null ) {
                    // interrupt is ignored by the thread
                    ( (Thread) obj ).stop();
                }
            }
        } catch ( Exception ex ) {
            LOG.warn( "Problem when trying to fix batik class loader leak." );
        }
    }

    /**
     * 'Heuristical' method to retrieve the {@link URL} for a file referenced from an init-param of a webapp config file
     * which may be:
     * <ul>
     * <li>a (absolute) <code>URL</code></li>
     * <li>a file location</li>
     * <li>a (relative) URL which in turn is resolved using <code>ServletContext.getRealPath</code></li>
     * </ul>
     * 
     * @param location
     * @param context
     * @return the full (and whitespace-escaped) URL
     * @throws MalformedURLException
     */
    public static URL resolveFileLocation( String location, ServletContext context )
                            throws MalformedURLException {
        URL serviceConfigurationURL = null;

        LOG.debug( "Resolving configuration file location: '" + location + "'..." );
        try {
            // construction of URI performs whitespace escaping
            serviceConfigurationURL = new URI( location ).toURL();
        } catch ( Exception e ) {
            LOG.debug( "No valid (absolute) URL. Trying context.getRealPath() now." );
            String realPath = context.getRealPath( location );
            if ( realPath == null ) {
                LOG.debug( "No 'real path' available. Trying to parse as a file location now." );
                serviceConfigurationURL = new File( location ).toURI().toURL();
            } else {
                try {
                    // realPath may either be a URL or a File
                    serviceConfigurationURL = new URI( realPath ).toURL();
                } catch ( Exception e2 ) {
                    LOG.debug( "'Real path' cannot be parsed as URL. Trying to parse as a file location now." );
                    // construction of URI performs whitespace escaping
                    serviceConfigurationURL = new File( realPath ).toURI().toURL();
                    LOG.debug( "configuration URL: " + serviceConfigurationURL );
                }
            }
        }
        return serviceConfigurationURL;
    }

    private void bindContextToThread( HttpServletRequest request, Credentials credentials ) {
        RequestContext context = new RequestContext( request, credentials, hardcodedServicesUrl, hardcodedResourcesUrl );
        CONTEXT.set( context );
        LOG.debug( "Initialized RequestContext for Thread " + Thread.currentThread() + "=" + context );
    }

    private String removeTrailingSlashOrQuestionMark( String url ) {
        if ( url.endsWith( "?" ) || url.endsWith( "/" ) ) {
            return url.substring( 0, url.length() - 1 );
        }
        return url;
    }

    /**
     * Sends an exception report to the client.
     * <p>
     * NOTE: Usually, exception reports are generated by the specific service controller. This method is only used when
     * the request is so broken that it cannot be dispatched.
     * </p>
     * 
     * @param ows
     *            if not null, it will be used to determine the responsible controller for exception serializing
     * @param e
     *            exception to be serialized
     * @param res
     *            response object
     * @throws ServletException
     */
    private void sendException( OWS ows, OWSException e, HttpResponseBuffer res, Version requestVersion )
                            throws ServletException {
        String userAgent = null;
        if ( OGCFrontController.getContext() != null ) {
            userAgent = OGCFrontController.getContext().getUserAgent();
        }

        if ( ows == null ) {
            Collection<List<OWS>> values = serviceConfiguration.getAll().values();
            if ( values.size() > 0 && !values.iterator().next().isEmpty() ) {
                ows = values.iterator().next().get( 0 );
            }
        }
        if ( ows != null ) {
            // use exception serializer / mime type from first registered controller (fair chance that this will be
            // correct)
            XMLExceptionSerializer serializer = ows.getExceptionSerializer( requestVersion );
            ( (AbstractOWS) ows ).sendException( null, serializer, e, res );
        } else {
            // use the most common serializer (OWS 1.1.0)
            XMLExceptionSerializer serializer = null;
            if ( requestVersion == null ) {
                serializer = new OWS110ExceptionReportSerializer( parseVersion( "1.1.0" ) );
            } else {
                serializer = new OWS110ExceptionReportSerializer( requestVersion );
            }

            if ( !res.isCommitted() ) {
                try {
                    res.reset();
                } catch ( IllegalStateException e2 ) {
                    // rb: the illegal state exception occurred.
                    throw new ServletException( e2 );
                }
                try {
                    serializer.serializeException( res, e );
                } catch ( Exception e2 ) {
                    LOG.error( "An error occurred while trying to send an exception: " + e2.getLocalizedMessage(), e );
                    throw new ServletException( e2 );
                }
            }
        }

        if ( userAgent != null && userAgent.toLowerCase().contains( "mozilla" ) ) {
            res.setContentType( "application/xml" );
        }
    }

    private void watchTimeout( final OWS ows, final String requestName ) {
        if ( requestWatchdog != null ) {
            final String serviceId = ows.getMetadata().getIdentifier().getId();
            requestWatchdog.watchCurrentThread( serviceId, requestName );
        }
    }

    private void unwatchTimeout() {
        if ( requestWatchdog != null ) {
            requestWatchdog.unwatchCurrentThread();
        }
    }

    private static String getHttpURL() {
        RequestContext context = getContext();
        String xForwardedHost = context.getXForwardedHost();
        if ( xForwardedHost != null && xForwardedHost != "" ) {
            String contextServiceUrl = context.getServiceUrl();
            try {
                URL serviceUrl = new URL( contextServiceUrl );
                return buildUrlFromForwardedHeader( context, serviceUrl );
            } catch ( MalformedURLException e ) {
                LOG.warn( "Could not parse service URL as URL: " + contextServiceUrl );
            }
        }
        return context.getServiceUrl();
    }

    private static String buildUrlFromForwardedHeader( RequestContext context, URL serviceUrl )
                            throws MalformedURLException {
        String xForwardedPort = context.getXForwardedPort();
        String xForwardedHost = context.getXForwardedHost();
        String xForwardedProto = context.getXForwardedProto();

        String protocol = parseProtocol( xForwardedProto, serviceUrl );
        String port = parsePort( xForwardedPort, serviceUrl );
        String path = serviceUrl.getPath();

        StringBuffer urlBuilder = new StringBuffer();
        urlBuilder.append( protocol ).append( "://" ).append( xForwardedHost );
        if ( port != null )
            urlBuilder.append( ":" ).append( port );
        if ( path != null && !"".equals( path ) )
            urlBuilder.append( path );
        return urlBuilder.toString();
    }

    private static String parseProtocol( String xForwardedProto, URL serviceUrl ) {
        if ( xForwardedProto != null && !"".equals( xForwardedProto ) )
            return xForwardedProto;
        else
            return serviceUrl.getProtocol();
    }

    private static String parsePort( String xForwardedPort, URL serviceUrl ) {
        if ( xForwardedPort != null && !"".equals( xForwardedPort ) )
            return xForwardedPort;
        else if ( serviceUrl.getPort() > -1 )
            return Integer.toString( serviceUrl.getPort() );
        return null;
    }

}