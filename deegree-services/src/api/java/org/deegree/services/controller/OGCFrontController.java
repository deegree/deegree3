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

import java.beans.Introspector;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.spi.IIORegistry;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
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
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.DeegreeAALogoUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.io.LoggingInputStream;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.version.DeegreeModuleInfo;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.cs.configuration.CRSConfiguration;
import org.deegree.services.authentication.SecurityException;
import org.deegree.services.controller.exception.ControllerException;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.ows.OWSException110XMLAdapter;
import org.deegree.services.controller.security.SecurityConfiguration;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.controller.utils.LoggingHttpResponseWrapper;
import org.deegree.services.i18n.Messages;
import org.deegree.services.jaxb.main.DeegreeServiceControllerType;
import org.deegree.services.jaxb.main.FrontControllerOptionsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Acts as the single communication entry point and dispatcher to all deegree OGC web services (WMS, WFS, WCS, CSW, WPS,
 * SOS...).
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
 * <li>The responsible {@link AbstractOGCServiceController} instance is determined and one of the following methods is
 * called:
 * <ul>
 * <li>{@link AbstractOGCServiceController#doKVP(Map, HttpServletRequest, HttpResponseBuffer, List)}</li>
 * <li>{@link AbstractOGCServiceController#doXML(XMLStreamReader, HttpServletRequest, HttpResponseBuffer, List)}</li>
 * <li>
 * {@link AbstractOGCServiceController#doSOAP(SOAPEnvelope, HttpServletRequest, HttpResponseBuffer, List, SOAPFactory)}</li>
 * </ul>
 * </li>
 * </nl>
 * </p>
 * 
 * @see AbstractOGCServiceController
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(debug = "logs the server startup, incoming requests and timing info, also enables enhanced request logging in $HOME/.deegree")
public class OGCFrontController extends HttpServlet {

    private static Logger LOG = LoggerFactory.getLogger( OGCFrontController.class );

    private static final long serialVersionUID = -1379869403008798932L;

    /** used to decode (already URL-decoded) query strings */
    private static final String DEFAULT_ENCODING = "UTF-8";

    private static final String defaultTMPDir = System.getProperty( "java.io.tmpdir" );

    private static OGCFrontController instance;

    private DeegreeServiceControllerType mainConfig;

    private final InheritableThreadLocal<RequestContext> CONTEXT = new InheritableThreadLocal<RequestContext>();

    private SecurityConfiguration securityConfiguration;

    private WebServicesConfiguration serviceConfiguration;

    private DeegreeWorkspace workspace;

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
        return instance.workspace;
    }

    /**
     * @return the service configuration
     */
    public static WebServicesConfiguration getServiceConfiguration() {
        return instance.serviceConfiguration;
    }

    /**
     * Return all active service controllers.
     * 
     * @return the instance of the requested service used by OGCFrontController, or null if the service is not
     *         registered.
     */
    public static Map<String, AbstractOGCServiceController> getServiceControllers() {
        return instance.serviceConfiguration.getServiceControllers();
    }

    /**
     * Returns the service controller instance based on the class of the service controller.
     * 
     * @param c
     *            class of the requested service controller, e.g. <code>WPSController.getClass()</code>
     * @return the instance of the requested service used by OGCFrontController, or null if no such service controller
     *         is active
     */
    public static AbstractOGCServiceController getServiceController( Class<? extends AbstractOGCServiceController> c ) {
        return instance.serviceConfiguration.getServiceController( c );
    }

    /**
     * Returns the HTTP URL for communicating with the OGCFrontController over the web (for POST requests).
     * <p>
     * NOTE: This method will only return a correct result if the calling thread originated in the
     * {@link #doGet(HttpServletRequest, HttpServletResponse)} or
     * {@link #doPost(HttpServletRequest, HttpServletResponse)} of this class (or has been spawned as a child thread by
     * such a thread).
     * </p>
     * 
     * @return the HTTP URL (for POST requests)
     */
    public static String getHttpPostURL() {
        String url = null;
        if ( instance.mainConfig.getDCP() != null && instance.mainConfig.getDCP().getHTTPPost() != null ) {
            url = instance.mainConfig.getDCP().getHTTPPost();
        } else {
            url = getContext().getRequestedBaseURL();
        }
        return url;
    }

    /**
     * Returns the HTTP URL for communicating with the OGCFrontController over the web (for GET requests).
     * <p>
     * NOTE: This method will only return a correct result if the calling thread originated in the
     * {@link #doGet(HttpServletRequest, HttpServletResponse)} or
     * {@link #doPost(HttpServletRequest, HttpServletResponse)} of this class (or has been spawned as a child thread by
     * such a thread).
     * </p>
     * 
     * @return the HTTP URL (for GET requests)
     */
    public static String getHttpGetURL() {
        String url = null;
        if ( instance.mainConfig.getDCP() != null && instance.mainConfig.getDCP().getHTTPGet() != null ) {
            url = instance.mainConfig.getDCP().getHTTPGet();
        } else {
            url = getContext().getRequestedBaseURL() + "?";
        }
        return url;
    }

    /**
     * Handles HTTP GET requests.
     * <p>
     * An HTTP GET request implies that input parameters are specified as key-value pairs. However, at least one OGC
     * service specification allows the sending of XML requests via GET (see WCS 1.0.0 specification, section 6.3.3). In
     * this case, the query string contains no <code>key=value</code> pairs, but the (URL encoded) xml. The encoding
     * ensures that no <code>=</code> char (parameter/value delimiters) occur in the string.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {
        long entryTime = System.currentTimeMillis();

        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "HTTP headers:" );
            Enumeration<String> headerEnum = request.getHeaderNames();
            while ( headerEnum.hasMoreElements() ) {
                String headerName = headerEnum.nextElement();
                LOG.debug( "- " + headerName + "='" + request.getHeader( headerName ) + "'" );
            }
        }

        String queryString = request.getQueryString();
        try {
            LOG.debug( "doGet(), query string: '" + queryString + "'" );

            if ( queryString == null ) {
                OWSException ex = new OWSException( "The request did not contain any parameters.",
                                                    "MissingParameterValue" );
                sendException( ex, response, null );
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
                    xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( dummySystemId, is );
                } else {
                    // decode query string
                    String decodedString = URLDecoder.decode( queryString, DEFAULT_ENCODING );
                    StringReader reader = new StringReader( decodedString );
                    xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( dummySystemId, reader );
                }
                if ( isSOAPRequest( xmlStream ) ) {
                    dispatchSOAPRequest( xmlStream, request, response, multiParts );
                } else {
                    dispatchXMLRequest( xmlStream, request, response, multiParts );
                }
            } else {
                // for GET requests, there is no standard way for defining the used encoding
                Map<String, String> normalizedKVPParams = KVPUtils.getNormalizedKVPMap( request.getQueryString(),
                                                                                        DEFAULT_ENCODING );
                LOG.debug( "parameter map: " + normalizedKVPParams );
                dispatchKVPRequest( normalizedKVPParams, request, response, multiParts, entryTime );
            }
        } catch ( XMLProcessingException e ) {
            // the message might be more meaningful
            OWSException ex = new OWSException( "The request did not contain KVP parameters and no parseable XML.",
                                                "MissingParameterValue", "request" );
            sendException( ex, response, null );
            return;
        } catch ( Throwable e ) {
            LOG.debug( "Handling HTTP-GET request took: " + ( System.currentTimeMillis() - entryTime )
                       + " ms before sending exception." );
            LOG.debug( e.getMessage(), e );
            OWSException ex = new OWSException( e.getLocalizedMessage(), e, "InvalidRequest" );
            sendException( ex, response, null );
            return;
        }
        LOG.debug( "Handling HTTP-GET request with status 'success' took: " + ( System.currentTimeMillis() - entryTime )
                   + " ms." );
    }

    /**
     * Handles HTTP POST requests.
     * <p>
     * An HTTP POST request specifies parameters in the request body. OGC service specifications use three different
     * ways to encode the parameters:
     * <ul>
     * <li><b>KVP</b>: Parameters are given as <code>key=value</code> pairs which are separated using the &amp;
     * character. This is equivalent to standard HTTP GET requests, except that the parameters are not encoded in the
     * query string, but in the POST body. In this case, the <code>content-type</code> field in the header must be
     * <code>application/x-www-form-urlencoded</code>.</li>
     * <li><b>XML</b>: The POST body contains an XML document. In this case, the <code>content-type</code> field in the
     * header has to be <code>text/xml</code>, but the implementation does not rely on this in order to be more tolerant
     * to clients.</li>
     * <li><b>SOAP</b>: TODO</li>
     * <li><b>Multipart</b>: TODO</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {

        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "HTTP headers:" );
            Enumeration<String> headerEnum = request.getHeaderNames();
            while ( headerEnum.hasMoreElements() ) {
                String headerName = headerEnum.nextElement();
                LOG.debug( "- " + headerName + "='" + request.getHeader( headerName ) + "'" );
            }
        }

        LOG.debug( "doPost(), contentType: '" + request.getContentType() + "'" );

        LoggingHttpResponseWrapper logging = null;
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
            if ( multiParts == null ) {
                // TODO log multiparts requests
                FrontControllerOptionsType opts = mainConfig.getFrontControllerOptions();
                if ( !isKVP && serviceConfiguration.getRequestLogger() != null ) {
                    String dir = opts.getRequestLogging().getOutputDirectory();
                    File file;
                    if ( dir == null ) {
                        file = createTempFile( "request", ".body" );
                    } else {
                        File directory = new File( dir );
                        if ( !directory.exists() ) {
                            directory.mkdirs();
                        }
                        file = createTempFile( "request", ".body", directory );
                    }
                    is = new LoggingInputStream( is, new FileOutputStream( file ) );
                    Boolean conf = opts.getRequestLogging().isOnlySuccessful();
                    boolean onlySuccessful = conf != null && conf;
                    response = logging = new LoggingHttpResponseWrapper( request.getRequestURL().toString(), response,
                                                                         file, onlySuccessful, entryTime, null,
                                                                         serviceConfiguration.getRequestLogger() );
                    // TODO obtain/set credentials somewhere
                }
            }

            if ( isKVP ) {
                String queryString = readPostBodyAsString( is );
                LOG.debug( "Treating POST input stream as KVP parameters. Raw input: '" + queryString + "'." );
                Map<String, String> normalizedKVPParams = null;
                String encoding = request.getCharacterEncoding();
                if ( encoding == null ) {
                    LOG.debug( "Request has no further encoding information. Defaulting to '" + DEFAULT_ENCODING + "'." );
                    normalizedKVPParams = KVPUtils.getNormalizedKVPMap( queryString, DEFAULT_ENCODING );
                } else {
                    LOG.debug( "Client encoding information :" + encoding );
                    normalizedKVPParams = KVPUtils.getNormalizedKVPMap( queryString, encoding );
                }
                dispatchKVPRequest( normalizedKVPParams, request, response, multiParts, entryTime );
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
                XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( dummySystemId,
                                                                                                 requestInputStream );
                // skip to start tag of root element
                StAXParsingHelper.nextElement( xmlStream );
                if ( isSOAPRequest( xmlStream ) ) {
                    dispatchSOAPRequest( xmlStream, request, response, multiParts );
                } else {
                    dispatchXMLRequest( xmlStream, request, response, multiParts );
                }
                if ( logging != null ) {
                    logging.finalizeLogging();
                }
            }
        } catch ( Throwable e ) {
            LOG.debug( "Handling HTTP-POST request took: " + ( System.currentTimeMillis() - entryTime )
                       + " ms before sending exception." );
            LOG.debug( e.getMessage(), e );
            OWSException ex = new OWSException( e.getLocalizedMessage(), "InvalidRequest" );
            sendException( ex, response, null );
        }
        LOG.debug( "Handling HTTP-POST request with status 'success' took: "
                   + ( System.currentTimeMillis() - entryTime ) + " ms." );
    }

    private String readPostBodyAsString( InputStream is )
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
    @SuppressWarnings("unchecked")
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
            if ( serviceConfiguration.getRequestLogger() != null ) { // TODO, this is not actually something of the
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
     * Dispatches a KVP request to the responsible {@link AbstractOGCServiceController}. Both GET and POST are handled
     * by this method.
     * <p>
     * The responsible {@link AbstractOGCServiceController} is identified according to this strategy:
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
                                     HttpServletResponse response, List<FileItem> multiParts, long entryTime )
                            throws ServletException, IOException {

        FrontControllerOptionsType opts = mainConfig.getFrontControllerOptions();
        LoggingHttpResponseWrapper logging = null;

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

            if ( serviceConfiguration.getRequestLogger() != null ) {
                Boolean conf = opts.getRequestLogging().isOnlySuccessful();
                boolean onlySuccessful = conf != null && conf;
                response = logging = new LoggingHttpResponseWrapper( response, requestWrapper.getQueryString(),
                                                                     onlySuccessful, entryTime, cred,
                                                                     serviceConfiguration.getRequestLogger() );
            }

            AbstractOGCServiceController subController = null;
            // first try service parameter, SERVICE-parameter is mandatory for each service and request (except WMS
            // 1.0.0)
            String service = normalizedKVPParams.get( "SERVICE" );
            String request = normalizedKVPParams.get( "REQUEST" );
            if ( service != null ) {

                try {
                    subController = serviceConfiguration.determineResponsibleControllerByServiceName( service );
                } catch ( IllegalArgumentException e ) {
                    // I know that the SOS tests test for the appropriate service exception here, so sending a OWS
                    // commons
                    // 1.1 one should be fine
                    OWSException ex = new OWSException( Messages.get( "CONTROLLER_INVALID_SERVICE", service ),
                                                        "InvalidParameterValue", "service" );
                    sendException( ex, response, null );
                    return;
                }
            } else {
                // dispatch according to REQUEST-parameter
                if ( request != null ) {
                    subController = serviceConfiguration.determineResponsibleControllerByRequestName( request );
                }
            }

            if ( subController != null ) {
                LOG.debug( "Dispatching request to subcontroller class: " + subController.getClass().getName() );
                HttpResponseBuffer responseWrapper = new HttpResponseBuffer( response );
                long dispatchTime = FrontControllerStats.requestDispatched();
                try {

                    subController.doKVP( normalizedKVPParams, requestWrapper, responseWrapper, multiParts );

                } finally {
                    FrontControllerStats.requestFinished( dispatchTime );
                }
                if ( opts != null && opts.isValidateResponses() != null && opts.isValidateResponses() ) {
                    validateResponse( responseWrapper );
                }
                responseWrapper.flushBuffer();
                if ( logging != null ) {
                    logging.finalizeLogging();
                }
            } else {
                String msg = null;
                if ( service == null && request == null ) {
                    msg = "Neither 'SERVICE' nor 'REQUEST' parameter is present. Cannot determine responsible subcontroller.";
                } else {
                    msg = "Unable to determine the subcontroller for request type '" + request + "' and service type '"
                          + service + "'.";
                }
                OWSException ex = new OWSException( msg, "MissingParameterValue", "service" );
                sendException( ex, response, null );
            }
        } catch ( SecurityException e ) {
            if ( credentialsProvider != null ) {
                LOG.debug( "A security exception was thrown, let the credential provider handle the job." );
                credentialsProvider.handleException( response, e );
            } else {
                LOG.debug( "A security exception was thrown ( " + e.getLocalizedMessage()
                           + " but no credentials provider was configured, sending generic ogc exception." );
                sendException( new OWSException( e.getLocalizedMessage(), ControllerException.NO_APPLICABLE_CODE ),
                               response, null );
            }
        }
    }

    private void validateResponse( HttpResponseBuffer responseWrapper ) {
        responseWrapper.validate();
    }

    /**
     * Dispatches an XML request to the responsible {@link AbstractOGCServiceController}. Both GET and POST are handled
     * by this method.
     * <p>
     * The responsible {@link AbstractOGCServiceController} is identified by the namespace of the root element.
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
                                     HttpServletResponse response, List<FileItem> multiParts )
                            throws ServletException, IOException {

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
            AbstractOGCServiceController subcontroller = serviceConfiguration.determineResponsibleControllerByNS( ns );
            if ( subcontroller != null ) {
                LOG.debug( "Dispatching request to subcontroller class: " + subcontroller.getClass().getName() );
                HttpResponseBuffer responseWrapper = new HttpResponseBuffer( response );
                long dispatchTime = FrontControllerStats.requestDispatched();
                try {
                    subcontroller.doXML( xmlStream, requestWrapper, responseWrapper, multiParts );
                } finally {
                    FrontControllerStats.requestFinished( dispatchTime );
                }
                if ( mainConfig.getFrontControllerOptions() != null
                     && mainConfig.getFrontControllerOptions().isValidateResponses() != null
                     && mainConfig.getFrontControllerOptions().isValidateResponses() ) {
                    validateResponse( responseWrapper );
                }
                responseWrapper.flushBuffer();
            } else {
                String msg = "No subcontroller for request namespace '" + ns + "' available.";
                throw new ServletException( msg );
            }
        } catch ( SecurityException e ) {
            if ( credentialsProvider != null ) {
                LOG.debug( "A security exception was thrown, let the credential provider handle the job." );
                credentialsProvider.handleException( response, e );
            } else {
                LOG.debug( "A security exception was thrown ( " + e.getLocalizedMessage()
                           + " but no credentials provider was configured, sending generic ogc exception." );
                sendException( new OWSException( e.getLocalizedMessage(), ControllerException.NO_APPLICABLE_CODE ),
                               response, null );
            }
        }

    }

    /**
     * Dispatches a SOAP request to the responsible {@link AbstractOGCServiceController}. Both GET and POST are handled
     * by this method.
     * <p>
     * The responsible {@link AbstractOGCServiceController} is identified by the namespace of the first child of the
     * SOAP body element.
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
                                      HttpServletResponse response, List<FileItem> multiParts )
                            throws ServletException, IOException {

        // TODO integrate authentication handling (CredentialsProvider)
        LOG.debug( "Handling soap request." );
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

        SOAPEnvelope envelope = soap.getSOAPEnvelope();

        CredentialsProvider credentialsProvider = securityConfiguration == null ? null
                                                                               : securityConfiguration.getCredentialsProvider();

        try {
            // TODO handle multiple authentication methods
            Credentials cred = null;
            if ( credentialsProvider != null ) {
                cred = credentialsProvider.doSOAP( envelope, requestWrapper );
            }
            LOG.debug( "credentials: " + cred );
            bindContextToThread( requestWrapper, cred );

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

            AbstractOGCServiceController subcontroller = serviceConfiguration.determineResponsibleControllerByNS( envelope.getSOAPBodyFirstElementNS().getNamespaceURI() );
            if ( subcontroller != null ) {
                LOG.debug( "Dispatching request to subcontroller class: " + subcontroller.getClass().getName() );
                HttpResponseBuffer responseWrapper = new HttpResponseBuffer( response );
                long dispatchTime = FrontControllerStats.requestDispatched();
                try {
                    subcontroller.doSOAP( envelope, requestWrapper, responseWrapper, multiParts, factory );
                } finally {
                    FrontControllerStats.requestFinished( dispatchTime );
                }
                if ( mainConfig.getFrontControllerOptions() != null
                     && mainConfig.getFrontControllerOptions().isValidateResponses() != null
                     && mainConfig.getFrontControllerOptions().isValidateResponses() ) {
                    validateResponse( responseWrapper );
                }
                responseWrapper.flushBuffer();
            } else {
                String msg = "No subcontroller for request namespace '"
                             + envelope.getSOAPBodyFirstElementNS().getNamespaceURI() + "' available.";
                throw new ServletException( msg );
            }
        } catch ( SecurityException e ) {
            if ( credentialsProvider != null ) {
                LOG.debug( "A security exception was thrown, let the credential provider handle the job." );
                credentialsProvider.handleException( response, e );
            } else {
                LOG.debug( "A security exception was thrown ( " + e.getLocalizedMessage()
                           + " but no credentials provider was configured, sending generic ogc exception." );
                sendException( new OWSException( e.getLocalizedMessage(), ControllerException.NO_APPLICABLE_CODE ),
                               response, null );
            }
        }
    }

    private boolean isSOAPRequest( XMLStreamReader xmlStream ) {
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
            LOG.info( "--------------------------------------------------------------------------------" );
            DeegreeAALogoUtils.logInfo( LOG );
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "deegree modules" );
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "" );
            for ( DeegreeModuleInfo moduleInfo : DeegreeModuleInfo.getRegisteredModules() ) {
                LOG.info( " - " + moduleInfo.toString() );
            }
            LOG.info( "" );
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "System info" );
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "" );
            LOG.info( "- java version      : " + System.getProperty( "java.version" ) + " ("
                      + System.getProperty( "java.vendor" ) + ")" );
            LOG.info( "- operating system  : " + System.getProperty( "os.name" ) + " ("
                      + System.getProperty( "os.version" ) + ", " + System.getProperty( "os.arch" ) + ")" );
            LOG.info( "- default encoding  : " + DEFAULT_ENCODING );
            LOG.info( "- temp directory    : " + defaultTMPDir );
            LOG.info( "" );

            initWorkspace();
            initServices();

        } catch ( NoClassDefFoundError e ) {
            LOG.error( "Initialization failed!" );
            LOG.error( "You probably forgot to add a required .jar to the WEB-INF/lib directory." );
            LOG.error( "The resource that could not be found was '{}'.", e.getMessage() );
            LOG.debug( "Stack trace:", e );
        } catch ( Exception e ) {
            LOG.error( "Initialization failed!" );
            LOG.trace( "An unexpected error was caught, stack trace:", e );
        }
    }

    private void initWorkspace()
                            throws IOException, URISyntaxException {
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Initializing workspace" );
        LOG.info( "--------------------------------------------------------------------------------" );
        workspace = getWorkspace();
        workspace.initAll();
        LOG.info( "" );
    }

    private void initServices()
                            throws ServletException {
        serviceConfiguration = new WebServicesConfiguration( workspace );
        serviceConfiguration.init();
        // TODO somehow eliminate the need for this stupid static field
        mainConfig = serviceConfiguration.getMainConfiguration();
        securityConfiguration = new SecurityConfiguration( workspace );
    }

    private void destroyWorkspace() {
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Destroying workspace" );
        LOG.info( "--------------------------------------------------------------------------------" );
        workspace.destroyAll();
        LOG.info( "" );
    }

    private void destroyServices() {
        serviceConfiguration.destroy();
        mainConfig = null;
        securityConfiguration = null;
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
        destroyServices();
        destroyWorkspace();
        initWorkspace();
        initServices();
    }

    private DeegreeWorkspace getWorkspace()
                            throws IOException, URISyntaxException {
        String wsName = getWorkspaceName();
        File fallbackDir = new File( resolveFileLocation( "WEB-INF/workspace", getServletContext() ).toURI() );
        if ( !fallbackDir.exists() ) {
            LOG.debug( "Trying old-style workspace directory (WEB-INF/conf)" );
            fallbackDir = new File( resolveFileLocation( "WEB-INF/conf", getServletContext() ).toURI() );
        } else {
            LOG.debug( "Using old-style workspace directory (WEB-INF/workspace)" );
        }
        DeegreeWorkspace ws = DeegreeWorkspace.getInstance( wsName, fallbackDir );
        LOG.info( "Using workspace '{}' at '{}'", ws.getName(), ws.getLocation() );
        return ws;
    }

    private String getWorkspaceName()
                            throws URISyntaxException, IOException {
        String wsName = "default";
        File wsNameFile = new File( resolveFileLocation( "WEB-INF/workspace_name", getServletContext() ).toURI() );
        if ( wsNameFile.exists() ) {
            BufferedReader reader = new BufferedReader( new FileReader( wsNameFile ) );
            wsName = reader.readLine().trim();
            LOG.info( "Using workspace name {} (defined in WEB-INF/workspace_name)", wsName, wsNameFile );
        } else {
            LOG.info( "Using default workspace (WEB-INF/workspace_name does not exist)", wsNameFile );
        }
        return wsName;
    }

    @Override
    public void destroy() {
        super.destroy();
        serviceConfiguration.destroy();
        plugClassLoaderLeaks();
    }

    /**
     * Apply workarounds for classloader leaks, see <a
     * href="https://wiki.deegree.org/deegreeWiki/ClassLoaderLeaks">ClassLoaderLeaks in deegree wiki</a>.
     */
    private void plugClassLoaderLeaks() {

        // deregister all JDBC drivers loaded by webapp classloader
        Enumeration<Driver> e = DriverManager.getDrivers();
        while ( e.hasMoreElements() ) {
            Driver driver = e.nextElement();
            try {
                if ( driver.getClass().getClassLoader() == getClass().getClassLoader() )
                    DriverManager.deregisterDriver( driver );
            } catch ( SQLException e1 ) {
                LOG.error( "Cannot unload driver: " + driver );
            }
        }

        LogFactory.releaseAll();
        LogManager.shutdown();

        // SLF4JLogFactory.releaseAll(); // should be the same as the LogFactory.releaseAll call
        // IIORegistry.getDefaultInstance().deregisterAll(); // breaks ImageIO
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

        Introspector.flushCaches();

        // just clear the configurations for now, it does not hurt
        CRSConfiguration.DEFINED_CONFIGURATIONS.clear();
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
        RequestContext context = new RequestContext( request, credentials );
        CONTEXT.set( context );
        LOG.debug( "Initialized RequestContext for Thread " + Thread.currentThread() + "=" + context );
    }

    /**
     * Sends an exception report to the client.
     * <p>
     * NOTE: Usually, exception reports are generated by the specific service controller. This method is only used when
     * the request is so broken that it cannot be dispatched.
     * </p>
     * 
     * @param e
     *            exception to be serialized
     * @param res
     *            response object
     * @throws ServletException
     */
    private void sendException( OWSException e, HttpServletResponse res, Version requestVersion )
                            throws ServletException {

        Collection<AbstractOGCServiceController> values = serviceConfiguration.getServiceControllers().values();
        if ( values.size() > 0 ) {
            // use exception serializer / mime type from first registered controller (fair chance that this will be
            // correct)
            AbstractOGCServiceController first = values.iterator().next();
            Pair<XMLExceptionSerializer<OWSException>, String> serializerAndMime = first.getExceptionSerializer( requestVersion );
            AbstractOGCServiceController.sendException( serializerAndMime.second, "UTF-8", null, 200,
                                                        serializerAndMime.first, e, res );
        } else {
            // use the most common serializer (OWS 1.1.0)
            AbstractOGCServiceController.sendException( "text/xml", "UTF-8", null, 200,
                                                        new OWSException110XMLAdapter(), e, res );
        }
    }

}
