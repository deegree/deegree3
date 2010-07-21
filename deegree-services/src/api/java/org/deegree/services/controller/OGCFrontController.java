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
import static java.lang.Class.forName;

import java.beans.Introspector;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
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
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.DeegreeAALogoUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.commons.utils.io.LoggingInputStream;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.version.DeegreeModuleInfo;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.coverage.persistence.CoverageBuilderManager;
import org.deegree.cs.configuration.CRSConfiguration;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.observation.persistence.ObservationStoreManager;
import org.deegree.record.persistence.RecordStoreManager;
import org.deegree.rendering.r3d.multiresolution.persistence.BatchedMTStoreManager;
import org.deegree.rendering.r3d.persistence.RenderableStoreManager;
import org.deegree.services.authentication.SecurityException;
import org.deegree.services.controller.exception.ControllerException;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.ows.OWSException110XMLAdapter;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.controller.utils.LoggingHttpResponseWrapper;
import org.deegree.services.controller.utils.StandardRequestLogger;
import org.deegree.services.i18n.Messages;
import org.deegree.services.jaxb.main.AllowedServices;
import org.deegree.services.jaxb.main.AuthenticationMethodType;
import org.deegree.services.jaxb.main.ConfiguredServicesType;
import org.deegree.services.jaxb.main.DeegreeServiceControllerType;
import org.deegree.services.jaxb.main.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.main.FrontControllerOptionsType;
import org.deegree.services.jaxb.main.ServiceType;
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

    private static final Version SUPPORTED_CONFIG_VERSION = Version.parseVersion( "0.6.0" );

    /** used to decode (already URL-decoded) query strings */
    private static final String DEFAULT_ENCODING = "UTF-8";

    private static DeegreeServicesMetadataType serviceConfig;

    private static DeegreeServiceControllerType mainConfig;

    private static final String DEFAULT_CONFIG_PATH = "WEB-INF/conf";

    private static final String DEFAULT_SERVICE_METADATA_PATH = DEFAULT_CONFIG_PATH + "/services/metadata.xml";

    private static final String DEFAULT_SERVICE_MAIN_PATH = DEFAULT_CONFIG_PATH + "/services/main.xml";

    // maps service names (e.g. 'WMS', 'WFS', ...) to responsible subcontrollers
    private static final Map<AllowedServices, AbstractOGCServiceController> serviceNameToController = new HashMap<AllowedServices, AbstractOGCServiceController>();

    // maps service namespaces (e.g. 'http://www.opengis.net/wms', 'http://www.opengis.net/wfs', ...) to the
    // responsible subcontrollers
    private static final Map<String, AbstractOGCServiceController> serviceNSToController = new HashMap<String, AbstractOGCServiceController>();

    // maps request names (e.g. 'GetMap', 'DescribeFeatureType') to the responsible subcontrollers
    private static final Map<String, AbstractOGCServiceController> requestNameToController = new HashMap<String, AbstractOGCServiceController>();

    private static final String defaultTMPDir = System.getProperty( "java.io.tmpdir" );

    private static final InheritableThreadLocal<RequestContext> CONTEXT = new InheritableThreadLocal<RequestContext>();

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
        RequestContext context = CONTEXT.get();
        LOG.debug( "Retrieving RequestContext for current thread " + Thread.currentThread() + ": " + context );
        return context;
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
        if ( mainConfig.getDCP() != null && mainConfig.getDCP().getHTTPPost() != null ) {
            url = mainConfig.getDCP().getHTTPPost();
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
        if ( mainConfig.getDCP() != null && mainConfig.getDCP().getHTTPGet() != null ) {
            url = mainConfig.getDCP().getHTTPGet();
        } else {
            url = getContext().getRequestedBaseURL() + "?";
        }
        return url;
    }

    private RequestLogger requestLogger;

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
                Map<String, String> normalizedKVPParams = getNormalizedKVPMap( request.getQueryString(),
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
     * @param queryString
     * @param encoding
     * @return a map with the query string's kvps parsed (uppercase keys)
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String> getNormalizedKVPMap( String queryString, String encoding )
                            throws UnsupportedEncodingException {

        Map<String, List<String>> keyToValueList = new HashMap<String, List<String>>();

        for ( String pair : queryString.split( "&" ) ) {
            // ignore empty key-values (prevents NPEs later)
            if ( pair.length() == 0 || !pair.contains( "=" ) ) {
                continue;
            }
            // NOTE: there may be more than one '=' character in pair, so the first one is taken as delimiter
            String[] parts = pair.split( "=", 2 );
            String key = parts[0];
            String value = null;
            if ( parts.length == 2 ) {
                value = parts[1];
            } else {
                if ( parts[0].endsWith( "=" ) ) {
                    value = "";
                }
            }
            List<String> values = keyToValueList.get( key );
            if ( values == null ) {
                values = new ArrayList<String>();
            }
            values.add( value );
            keyToValueList.put( key, values );
        }

        Map<String, String[]> keyToValueArray = new HashMap<String, String[]>();
        for ( String key : keyToValueList.keySet() ) {
            List<String> valueList = keyToValueList.get( key );
            String[] valueArray = new String[valueList.size()];
            valueList.toArray( valueArray );
            keyToValueArray.put( key, valueArray );
        }

        Map<String, String> kvpParamsUC = new HashMap<String, String>();
        for ( String key : keyToValueArray.keySet() ) {
            String[] values = keyToValueArray.get( key );
            if ( values != null && values.length > 0 ) {
                String decodedValue = URLDecoder.decode( values[0], encoding );
                kvpParamsUC.put( key.toUpperCase(), decodedValue );
            }
        }
        return kvpParamsUC;
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
                if ( !isKVP && requestLogger != null ) {
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
                    response = logging = new LoggingHttpResponseWrapper( response, file, onlySuccessful, entryTime,
                                                                         null, requestLogger ); // TODO obtain/set credentials somewhere
                }
            }

            if ( isKVP ) {
                String queryString = readPostBodyAsString( is );
                LOG.debug( "Treating POST input stream as KVP parameters. Raw input: '" + queryString + "'." );
                Map<String, String> normalizedKVPParams = null;
                String encoding = request.getCharacterEncoding();
                if ( encoding == null ) {
                    LOG.debug( "Request has no further encoding information. Defaulting to '" + DEFAULT_ENCODING + "'." );
                    normalizedKVPParams = getNormalizedKVPMap( queryString, DEFAULT_ENCODING );
                } else {
                    LOG.debug( "Client encoding information :" + encoding );
                    normalizedKVPParams = getNormalizedKVPMap( queryString, encoding );
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

    private static RequestLogger instantiateRequestLogger(
                                                           org.deegree.services.jaxb.main.FrontControllerOptionsType.RequestLogging.RequestLogger conf ) {
        if ( conf != null ) {
            String cls = conf.getClazz();
            try {
                Object o = conf.getConfiguration();
                if ( o == null ) {
                    return (RequestLogger) forName( cls ).newInstance();
                }
                return (RequestLogger) forName( cls ).getDeclaredConstructor( Object.class ).newInstance( o );
            } catch ( ClassNotFoundException e ) {
                LOG.info( "The request logger class '{}' could not be found on the classpath.", cls );
                LOG.trace( "Stack trace:", e );
            } catch ( ClassCastException e ) {
                LOG.info( "The request logger class '{}' does not implement the RequestLogger interface.", cls );
                LOG.trace( "Stack trace:", e );
            } catch ( InstantiationException e ) {
                LOG.info( "The request logger class '{}' could not be instantiated"
                          + " (needs a default constructor without arguments if no configuration is given).", cls );
                LOG.trace( "Stack trace:", e );
            } catch ( IllegalAccessException e ) {
                LOG.info( "The request logger class '{}' could not be instantiated"
                          + " (default constructor needs to be accessible if no configuration is given).", cls );
                LOG.trace( "Stack trace:", e );
            } catch ( IllegalArgumentException e ) {
                LOG.info( "The request logger class '{}' could not be instantiated"
                          + " (constructor needs to take an object argument if configuration is given).", cls );
                LOG.trace( "Stack trace:", e );
            } catch ( java.lang.SecurityException e ) {
                LOG.info( "The request logger class '{}' could not be instantiated"
                          + " (JVM does have insufficient rights to instantiate the class).", cls );
                LOG.trace( "Stack trace:", e );
            } catch ( InvocationTargetException e ) {
                LOG.info( "The request logger class '{}' could not be instantiated"
                          + " (constructor call threw an exception).", cls );
                LOG.trace( "Stack trace:", e );
            } catch ( NoSuchMethodException e ) {
                LOG.info( "The request logger class '{}' could not be instantiated"
                          + " (constructor needs to take an object argument if configuration is given).", cls );
                LOG.trace( "Stack trace:", e );
            }
        }
        return new StandardRequestLogger();
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
            if ( requestLogger != null ) { // TODO, this is not actually something of the request logger, what is
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

        // extract (deegree specific) security information and bind to current thread
        CredentialsProvider credProv = null;

        try {
            // TODO handle multiple authentication methods
            AuthenticationMethodType authType = mainConfig.getAuthenticationMethod();

            Credentials cred = null;
            if ( authType != null ) {
                LOG.debug( "Configured authtype: " + authType );
                if ( authType.getSOAPAuthentication() != null ) {
                    authType.setSOAPAuthentication( null );
                    credProv = CredentialsProviderManager.create( authType );
                    LOG.debug( "credProv1: " + credProv );
                    if ( credProv != null ) {
                        cred = credProv.doKVP( normalizedKVPParams, requestWrapper, response );
                    }
                    authType.setSOAPAuthentication( "" );

                } else {
                    credProv = CredentialsProviderManager.create( authType );
                    LOG.debug( "credProv2: " + credProv );
                    if ( credProv != null ) {
                        cred = credProv.doKVP( normalizedKVPParams, requestWrapper, response );
                    }
                }
            }
            LOG.debug( "credentials: " + cred );
            bindContextToThread( requestWrapper, cred );

            if ( requestLogger != null ) {
                Boolean conf = opts.getRequestLogging().isOnlySuccessful();
                boolean onlySuccessful = conf != null && conf;
                response = logging = new LoggingHttpResponseWrapper( response, requestWrapper.getQueryString(),
                                                                     onlySuccessful, entryTime, cred, requestLogger );
            }

            AbstractOGCServiceController subController = null;
            // first try service parameter, SERVICE-parameter is mandatory for each service and request (except WMS
            // 1.0.0)
            String service = normalizedKVPParams.get( "SERVICE" );
            String request = normalizedKVPParams.get( "REQUEST" );
            if ( service != null ) {

                try {
                    subController = determineResponsibleControllerByServiceName( service );
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
                    subController = determineResponsibleControllerByRequestName( request );
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
            if ( credProv != null ) {
                LOG.debug( "A security exception was thrown, let the credential provider handle the job." );
                credProv.handleException( response, e );
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

        // TODO integrate authentication handling (CredentialsProvider)

        CredentialsProvider credProv = null;

        try {
            // TODO handle multiple authentication methods
            AuthenticationMethodType authType = mainConfig.getAuthenticationMethod();

            Credentials cred = null;
            if ( authType != null ) {
                LOG.debug( "Configured authtype: " + authType );

                if ( authType.getSOAPAuthentication() != null ) {
                    authType.setSOAPAuthentication( null );
                    credProv = CredentialsProviderManager.create( authType );
                    LOG.debug( "credProv: " + credProv );
                    if ( credProv != null ) {
                        cred = credProv.doXML( xmlStream, requestWrapper, response );
                    }
                    authType.setSOAPAuthentication( "" );
                }

            }
            LOG.debug( "credentials: " + cred );
            bindContextToThread( requestWrapper, cred );

            // AbstractOGCServiceController subController = null;
            // extract (deegree specific) security information and bind to current thread
            // String user = xmlStream.getAttributeValue( XMLConstants.NULL_NS_URI, "user" );
            // String password = xmlStream.getAttributeValue( XMLConstants.NULL_NS_URI, "password" );
            // String sessionId = xmlStream.getAttributeValue( XMLConstants.NULL_NS_URI, "sessionId" );

            String ns = xmlStream.getNamespaceURI();
            AbstractOGCServiceController subcontroller = determineResponsibleControllerByNS( ns );
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
            if ( credProv != null ) {
                LOG.debug( "A security exception was thrown, let the credential provider handle the job." );
                credProv.handleException( response, e );
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

        CredentialsProvider credProv = null;

        try {
            // TODO handle multiple authentication methods
            AuthenticationMethodType authType = mainConfig.getAuthenticationMethod();
            Credentials cred = null;
            if ( authType != null ) {
                LOG.debug( "Configured authtype: " + authType );

                if ( authType.getHttpBasicAuthentication() != null ) {
                    // workaround...like a wrapper
                    authType.setHttpBasicAuthentication( null );
                    authType.setDeegreeAuthentication( null );
                    authType.setHttpDigestAuthentication( null );

                    credProv = CredentialsProviderManager.create( authType );
                    LOG.debug( "credProv: " + credProv );
                    cred = credProv.doSOAP( envelope, requestWrapper );

                    authType.setHttpBasicAuthentication( "" );
                }

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

            AbstractOGCServiceController subcontroller = determineResponsibleControllerByNS( envelope.getSOAPBodyFirstElementNS().getNamespaceURI() );
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
            if ( credProv != null ) {
                LOG.debug( "A security exception was thrown, let the credential provider handle the job." );
                credProv.handleException( response, e );
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

    /**
     * Determines the {@link AbstractOGCServiceController} that is responsible for handling requests to a certain
     * service type, e.g. WMS, WFS.
     * 
     * @param serviceType
     *            service type code, e.g. "WMS" or "WFS"
     * @return responsible <code>SecuredSubController</code> or null, if no responsible controller was found
     */
    private AbstractOGCServiceController determineResponsibleControllerByServiceName( String serviceType ) {
        AllowedServices service = AllowedServices.fromValue( serviceType );
        if ( service == null ) {
            return null;
        }
        return serviceNameToController.get( service );
    }

    /**
     * Determines the {@link AbstractOGCServiceController} that is responsible for handling requests with a certain
     * name, e.g. GetMap, GetFeature.
     * 
     * @param requestName
     *            request name, e.g. "GetMap" or "GetFeature"
     * @return responsible <code>SecuredSubController</code> or null, if no responsible controller was found
     */
    private AbstractOGCServiceController determineResponsibleControllerByRequestName( String requestName ) {
        return requestNameToController.get( requestName );
    }

    /**
     * Determines the {@link AbstractOGCServiceController} that is responsible for handling requests to a certain
     * service type, e.g. WMS, WFS.
     * 
     * @param ns
     *            service type code, e.g. "WMS" or "WFS"
     * @return responsible <code>SecuredSubController</code> or null, if no responsible controller was found
     */
    private AbstractOGCServiceController determineResponsibleControllerByNS( String ns ) {
        return serviceNSToController.get( ns );
    }

    /**
     * Return all active service controllers.
     * 
     * @return the instance of the requested service used by OGCFrontController, or null if the service is not
     *         registered.
     */
    public static Map<String, AbstractOGCServiceController> getServiceControllers() {
        Map<String, AbstractOGCServiceController> nameToController = new HashMap<String, AbstractOGCServiceController>();
        for ( AllowedServices serviceName : serviceNameToController.keySet() ) {
            nameToController.put( serviceName.value(), serviceNameToController.get( serviceName ) );
        }
        return nameToController;
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
        AbstractOGCServiceController result = null;
        for ( AbstractOGCServiceController it : serviceNSToController.values() ) {
            if ( c == it.getClass() ) {
                result = it;
                break;
            }
        }
        return result;
    }

    @Override
    public void init( ServletConfig config )
                            throws ServletException {
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

            // LOG.info( "--------------------------------------------------------------------------------" );
            // LOG.info( "Setting up temporary file storage." );
            // LOG.info( "--------------------------------------------------------------------------------" );
            // TempFileManager.init( config.getServletContext().getContextPath() );
            // LOG.info( "" );

            initProxyConfig();
            initJDBCConnections();
            initFeatureStores();
            initObservationStores();
            initCoverages();
            initRecordStores();
            initRenderableStores();
            initBatchedMTStores();
            initWebServices();
            initRequestLogger();

        } catch ( NoClassDefFoundError e ) {
            LOG.error( "Initialization failed!" );
            LOG.error( "You probably forgot to add a required .jar to the WEB-INF/lib directory." );
            LOG.error( "The resource that could not be found was '{}'.", e.getMessage() );
            LOG.debug( "Stack trace:", e );
        } catch ( Exception e ) {
            LOG.error( "Initialization failed!" );
            LOG.error( "An unexpected error was caught:", e );
        }
    }

    private void initProxyConfig() {

        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Proxy configuration." );
        LOG.info( "--------------------------------------------------------------------------------" );

        File proxyConfigFile = null;
        try {
            proxyConfigFile = new File(
                                        resolveFileLocation( DEFAULT_CONFIG_PATH + "/proxy.xml", getServletContext() ).toURI() );
        } catch ( MalformedURLException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( URISyntaxException e ) {
            LOG.error( e.getMessage(), e );
        }

        if ( proxyConfigFile != null && proxyConfigFile.exists() ) {
            try {
                ProxyUtils.setupProxyParameters( proxyConfigFile );
            } catch ( IllegalArgumentException e ) {
                LOG.warn( "Unable to apply proxy configuration from file: " + proxyConfigFile + ": " + e.getMessage() );
            }
        } else {
            LOG.info( "No 'proxy.xml' file -- skipping set up of proxy configuration." );
        }
        ProxyUtils.logProxyConfiguration( LOG );
        LOG.info( "" );
    }

    private void initWebServices()
                            throws ServletException {
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Starting webservices." );
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "" );

        URL resolvedMetadataURL = null;
        try {
            resolvedMetadataURL = resolveFileLocation( DEFAULT_SERVICE_METADATA_PATH, getServletContext() );
        } catch ( MalformedURLException e ) {
            throw new ServletException( "Resolving of parameter 'ServicesConfiguration' failed: "
                                        + e.getLocalizedMessage(), e );
        }
        if ( resolvedMetadataURL == null ) {
            throw new ServletException( "Resolving service configuration url failed!" );
        }
        URL resolvedMainURL = null;
        try {
            resolvedMainURL = resolveFileLocation( DEFAULT_SERVICE_MAIN_PATH, getServletContext() );
        } catch ( MalformedURLException e ) {
            throw new ServletException( "Resolving of main.xml failed: " + e.getLocalizedMessage(), e );
        }
        if ( resolvedMainURL == null ) {
            throw new ServletException( "Resolving service configuration url for main.xml failed!" );
        }

        unmarshallConfiguration( resolvedMetadataURL, resolvedMainURL );

        Version configVersion = Version.parseVersion( serviceConfig.getConfigVersion() );
        if ( !configVersion.equals( SUPPORTED_CONFIG_VERSION ) ) {
            String msg = "The service metadata file '" + resolvedMetadataURL + " uses configuration format version "
                         + serviceConfig.getConfigVersion() + ", but this deegree version only supports version "
                         + SUPPORTED_CONFIG_VERSION + ". Information on resolving this issue can be found at "
                         + "'http://wiki.deegree.org/deegreeWiki/deegree3/ConfigurationVersions'. ";
            LOG.debug( "********************************************************************************" );
            LOG.error( msg );
            LOG.debug( "********************************************************************************" );
            throw new ServletException( msg );
        }

        ConfiguredServicesType servicesConfigured = mainConfig.getConfiguredServices();
        List<ServiceType> services = null;
        if ( servicesConfigured != null ) {
            services = servicesConfigured.getService();
            if ( services != null && services.size() > 0 ) {
                LOG.info( "The file: " + resolvedMetadataURL );
                LOG.info( "Provided following services:" );
                for ( ServiceType s : services ) {
                    URL configLocation = null;
                    try {
                        configLocation = new URL( resolvedMetadataURL, s.getConfigurationLocation() );
                    } catch ( MalformedURLException e ) {
                        LOG.error( e.getMessage(), e );
                        return;
                    }
                    s.setConfigurationLocation( configLocation.toExternalForm() );

                    LOG.info( " - " + s.getServiceName() );
                }
                LOG.info( "ATTENTION - Skipping the loading of all services in conf/ which are not listed above." );
            }
        }
        if ( services == null || services.size() == 0 ) {
            LOG.info( "No service elements were supplied in the file: '" + resolvedMainURL
                      + "' -- trying to use the default loading mechanism." );
            try {
                services = loadServicesFromDefaultLocation();
            } catch ( MalformedURLException e ) {
                throw new ServletException( "Error loading service configurations: " + e.getMessage() );
            }
        }
        if ( services.size() == 0 ) {
            throw new ServletException(
                                        "No deegree web services could be loaded (manually or automatically) please take a look at your configuration file: "
                                                                + resolvedMainURL
                                                                + " and or your WEB-INF/conf directory." );
        }

        for ( ServiceType configuredService : services ) {
            AbstractOGCServiceController serviceController = instantiateServiceController( configuredService );
            if ( serviceController != null ) {
                registerSubController( configuredService, serviceController );
            }
        }
        LOG.info( "" );
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Webservices started." );
        LOG.info( "--------------------------------------------------------------------------------" );
    }

    private void initRequestLogger() {
        FrontControllerOptionsType opts = mainConfig.getFrontControllerOptions();
        if ( opts != null && opts.getRequestLogging() != null ) {
            org.deegree.services.jaxb.main.FrontControllerOptionsType.RequestLogging.RequestLogger logger = opts.getRequestLogging().getRequestLogger();
            requestLogger = instantiateRequestLogger( logger );
        }
    }

    private void initFeatureStores() {

        File fsDir = null;
        try {
            fsDir = new File(
                              resolveFileLocation( DEFAULT_CONFIG_PATH + "/datasources/feature", getServletContext() ).toURI() );
        } catch ( MalformedURLException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( URISyntaxException e ) {
            LOG.error( e.getMessage(), e );
        }
        if ( fsDir != null && fsDir.exists() ) {
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "Setting up feature stores." );
            LOG.info( "--------------------------------------------------------------------------------" );
            FeatureStoreManager.init( fsDir );
            LOG.info( "" );
        } else {
            LOG.debug( "No 'datasources/feature' directory -- skipping initialization of feature stores." );
        }

    }

    private void initObservationStores() {
        File osDir = null;
        try {
            osDir = new File( resolveFileLocation( DEFAULT_CONFIG_PATH + "/datasources/observation",
                                                   getServletContext() ).toURI() );
        } catch ( MalformedURLException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( URISyntaxException e ) {
            LOG.error( e.getMessage(), e );
        }
        if ( osDir != null && osDir.exists() ) {
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "Setting up observation stores." );
            LOG.info( "--------------------------------------------------------------------------------" );
            ObservationStoreManager.init( osDir );
            LOG.info( "" );
        } else {
            LOG.debug( "No 'datasources/observation' directory -- skipping initialization of observation stores." );
        }
    }

    private void initCoverages() {
        File coverageDir = null;
        try {
            coverageDir = new File( resolveFileLocation( DEFAULT_CONFIG_PATH + "/datasources/coverage",
                                                         getServletContext() ).toURI() );
        } catch ( MalformedURLException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( URISyntaxException e ) {
            LOG.error( e.getMessage(), e );
        }
        if ( coverageDir != null && coverageDir.exists() ) {
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "Setting up coverages." );
            LOG.info( "--------------------------------------------------------------------------------" );
            CoverageBuilderManager.init( coverageDir );
            LOG.info( "" );
        } else {
            LOG.debug( "No 'datasources/coverage' directory -- skipping initialization of coverages." );
        }
    }

    private void initRenderableStores() {
        File renderableDir = null;
        try {
            renderableDir = new File( resolveFileLocation( DEFAULT_CONFIG_PATH + "/datasources/renderable",
                                                           getServletContext() ).toURI() );
        } catch ( MalformedURLException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( URISyntaxException e ) {
            LOG.error( e.getMessage(), e );
        }
        if ( renderableDir != null && renderableDir.exists() ) {
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "Setting up renderable stores." );
            LOG.info( "--------------------------------------------------------------------------------" );
            RenderableStoreManager.init( renderableDir );
            LOG.info( "" );
        } else {
            LOG.debug( "No 'datasources/renderable' directory -- skipping initialization of renderable stores." );
        }
    }

    private void initBatchedMTStores() {
        File batchedMTDir = null;
        try {
            batchedMTDir = new File( resolveFileLocation( DEFAULT_CONFIG_PATH + "/datasources/batchedmt",
                                                          getServletContext() ).toURI() );
        } catch ( MalformedURLException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( URISyntaxException e ) {
            LOG.error( e.getMessage(), e );
        }
        if ( batchedMTDir != null && batchedMTDir.exists() ) {
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "Setting up BatchedMT stores." );
            LOG.info( "--------------------------------------------------------------------------------" );
            BatchedMTStoreManager.init( batchedMTDir );
            LOG.info( "" );
        } else {
            LOG.debug( "No 'datasources/batchedmt' directory -- skipping initialization of BatchedMT stores." );
        }
    }

    private void initRecordStores() {
        File rsDir = null;
        try {
            rsDir = new File(
                              resolveFileLocation( DEFAULT_CONFIG_PATH + "/datasources/record", getServletContext() ).toURI() );
        } catch ( MalformedURLException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( URISyntaxException e ) {
            LOG.error( e.getMessage(), e );
        }
        if ( rsDir != null && rsDir.exists() ) {
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "Setting up record stores." );
            LOG.info( "--------------------------------------------------------------------------------" );
            RecordStoreManager.init( rsDir );
            LOG.info( "" );
        } else {
            LOG.debug( "No 'datasources/record' directory -- skipping initialization of record stores." );
        }
    }

    private void initJDBCConnections() {
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Setting up JDBC connection pools." );
        LOG.info( "--------------------------------------------------------------------------------" );

        File jdbcDir = null;
        try {
            jdbcDir = new File( resolveFileLocation( DEFAULT_CONFIG_PATH + "/jdbc", getServletContext() ).toURI() );
        } catch ( MalformedURLException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( URISyntaxException e ) {
            LOG.error( e.getMessage(), e );
        }
        if ( jdbcDir != null && jdbcDir.exists() ) {
            ConnectionManager.init( jdbcDir );
        } else {
            LOG.info( "No 'jdbc' directory -- skipping initialization of JDBC connection pools." );
        }
        LOG.info( "" );
    }

    @Override
    public void destroy() {
        super.destroy();
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Shutting down deegree in context '" + getServletContext().getServletContextName() + "'..." );
        for ( AllowedServices serviceName : serviceNameToController.keySet() ) {
            AbstractOGCServiceController subcontroller = serviceNameToController.get( serviceName );
            LOG.info( "Shutting down '" + serviceName + "'." );
            try {
                subcontroller.destroy();
            } catch ( Exception e ) {
                String msg = "Error destroying subcontroller '" + subcontroller.getClass().getName() + "': "
                             + e.getMessage();
                LOG.error( msg, e );
            }
        }
        LOG.info( "deegree OGC webservices shut down." );
        ConnectionManager.destroy();
        LOG.info( "--------------------------------------------------------------------------------" );
        plugClassLoaderLeaks();
    }

    private void registerSubController( ServiceType configuredService, AbstractOGCServiceController serviceController ) {

        // associate service name (abbreviation) with controller instance
        LOG.debug( "Service name '" + configuredService.getServiceName() + "' -> '"
                   + serviceController.getClass().getSimpleName() + "'" );
        serviceNameToController.put( configuredService.getServiceName(), serviceController );

        // associate request types with controller instance
        for ( String request : serviceController.getHandledRequests() ) {
            // skip GetCapabilities requests
            if ( !( "GetCapabilities".equals( request ) ) ) {
                LOG.debug( "Request type '" + request + "' -> '" + serviceController.getClass().getSimpleName() + "'" );
                requestNameToController.put( request, serviceController );
            }
        }

        // associate namespaces with controller instance
        for ( String ns : serviceController.getHandledNamespaces() ) {
            LOG.debug( "Namespace '" + ns + "' -> '" + serviceController.getClass().getSimpleName() + "'" );
            serviceNSToController.put( ns, serviceController );
        }
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
     * Creates an instance of a sub controller which is valid for the given configured Service, by applying following
     * conventions:
     * <ul>
     * <li>The sub controller must extend {@link AbstractOGCServiceController}</li>
     * <li>The package of the controller is the package of this class.[SERVICE_ABBREV_lower_case]</li>
     * <li>The name of the controller must be [SERVICE_NAME_ABBREV]Controller</li>
     * <li>The controller must have a constructor with a String parameter</li>
     * </ul>
     * If all above conditions are met, the instantiated controller will be returned else <code>null</code>
     * 
     * @param configuredService
     * @return the instantiated secured sub controller or <code>null</code> if an error occurred.
     */
    @SuppressWarnings("unchecked")
    private AbstractOGCServiceController instantiateServiceController( ServiceType configuredService ) {
        AbstractOGCServiceController subController = null;
        if ( configuredService == null ) {
            return subController;
        }

        final String serviceName = configuredService.getServiceName().name();
        final String packageName = OGCFrontController.class.getPackage().getName();

        // something like org.deegree.services.controller.wfs.WFSController
        String controller = packageName + "." + serviceName.toLowerCase() + "." + serviceName + "Controller";

        LOG.info( "" );
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Starting " + serviceName + "." );
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Configuration file: '" + configuredService.getConfigurationLocation() + "'" );
        if ( configuredService.getControllerClass() != null ) {
            LOG.info( "Using custom controller class '{}'.", configuredService.getControllerClass() );
            controller = configuredService.getControllerClass();
        }
        try {
            long time = System.currentTimeMillis();
            Class<AbstractOGCServiceController> subControllerClass = (Class<AbstractOGCServiceController>) Class.forName(
                                                                                                                          controller,
                                                                                                                          false,
                                                                                                                          OGCFrontController.class.getClassLoader() );
            subController = subControllerClass.newInstance();
            XMLAdapter controllerConf = new XMLAdapter( new URL( configuredService.getConfigurationLocation() ) );
            subController.init( controllerConf, serviceConfig, mainConfig );
            LOG.info( "" );
            // round to exactly two decimals, I think their should be a java method for this though
            double startupTime = Math.round( ( ( System.currentTimeMillis() - time ) * 0.1 ) ) * 0.01;
            LOG.info( serviceName + " startup successful (took: " + startupTime + " seconds)" );
        } catch ( Exception e ) {
            LOG.error( "Initializing " + serviceName + " failed: " + e.getMessage(), e );
            LOG.info( "" );
            LOG.info( serviceName + " startup failed." );
            subController = null;
        }
        return subController;
    }

    /**
     * Iterates over all directories in the conf/ directory and returns the service/configuration mappings as a list.
     * This default service loading mechanism implies the following directory structure:
     * <ul>
     * <li>conf/</li>
     * <li>conf/[SERVICE_NAME]/ (upper-case abbreviation of a deegree web service, please take a look at
     * {@link AllowedServices})</li>
     * <li>conf/[SERVICE_NAME]/[SERVICE_NAME]_configuration.xml</li>
     * </ul>
     * If all conditions are met the service type is added to resulting list. If none of the underlying directories meet
     * above criteria, an empty list will be returned.
     * 
     * @return the list of services found in the conf directory. Or an empty list if the above conditions are not met
     *         for any directory in the conf directory.
     * @throws MalformedURLException
     */
    private List<ServiceType> loadServicesFromDefaultLocation()
                            throws MalformedURLException {
        File serviceConfigDir = null;
        try {
            serviceConfigDir = new File(
                                         resolveFileLocation( DEFAULT_CONFIG_PATH + "/services", getServletContext() ).toURI() );
        } catch ( MalformedURLException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( URISyntaxException e ) {
            LOG.error( e.getMessage(), e );
        }
        List<ServiceType> loadedServices = new ArrayList<ServiceType>();
        if ( serviceConfigDir == null || !serviceConfigDir.isDirectory() ) {
            LOG.error( "Could not read from the default service configuration directory (" + DEFAULT_CONFIG_PATH
                       + ") because it is not a directory." );
            return loadedServices;

        }
        LOG.info( "Using default directory: " + serviceConfigDir.getAbsolutePath()
                  + " to scan for webservice configurations." );
        File[] files = serviceConfigDir.listFiles();
        if ( files == null || files.length == 0 ) {
            LOG.error( "No files found in default configuration directory, hence no services to load." );
            return loadedServices;
        }
        for ( File f : files ) {
            if ( !f.isDirectory() ) {
                String fileName = f.getName();
                if ( fileName != null && !"".equals( fileName.trim() ) ) {
                    String serviceName = fileName.trim().toUpperCase();
                    // to avoid the ugly warning we can afford this extra s(hack)
                    if ( serviceName.equals( ".SVN" ) || !serviceName.endsWith( ".XML" )
                         || serviceName.equals( "METADATA.XML" ) || serviceName.equals( "MAIN.XML" ) ) {
                        continue;
                    }
                    serviceName = serviceName.substring( 0, fileName.length() - 4 );

                    AllowedServices as;
                    try {
                        as = AllowedServices.fromValue( serviceName );
                    } catch ( IllegalArgumentException ex ) {
                        LOG.warn( "File '" + fileName + "' in the configuration directory "
                                  + "is not a valid deegree webservice, so skipping it." );
                        continue;
                    }
                    LOG.debug( "Trying to create a frontcontroller for service: " + fileName
                               + " found in the configuration directory." );
                    ServiceType configuredService = new ServiceType();
                    configuredService.setConfigurationLocation( f.toURI().toURL().toString() );
                    configuredService.setServiceName( as );
                    loadedServices.add( configuredService );
                }
            }

        }

        return loadedServices;
    }

    /**
     * Unmarshalls the configuration file with a little help from jaxb.
     * 
     * @param resolvedConfigURL
     *            pointing to the configuration file.
     * @throws ServletException
     */
    private synchronized void unmarshallConfiguration( URL resolvedConfigURL, URL resolvedMainURL )
                            throws ServletException {
        if ( serviceConfig == null ) {
            try {
                String contextName = "org.deegree.services.jaxb.main";
                JAXBContext jc = JAXBContext.newInstance( contextName );
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                serviceConfig = (DeegreeServicesMetadataType) ( (JAXBElement<?>) unmarshaller.unmarshal( resolvedConfigURL ) ).getValue();
                try {
                    mainConfig = (DeegreeServiceControllerType) ( (JAXBElement<?>) unmarshaller.unmarshal( resolvedMainURL ) ).getValue();
                } catch ( JAXBException e ) {
                    mainConfig = new DeegreeServiceControllerType();
                    LOG.info( "main.xml could not be loaded. Proceeding with defaults." );
                    LOG.debug( "Error was: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            } catch ( JAXBException e ) {
                String msg = "Could not unmarshall frontcontroller configuration: " + e.getMessage();
                LOG.error( msg, e );
                throw new ServletException( msg, e );
            }
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
    private URL resolveFileLocation( String location, ServletContext context )
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
                    LOG.debug( "serviceConfigurationURL: " + serviceConfigurationURL );
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

        Collection<AbstractOGCServiceController> values = serviceNameToController.values();
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
