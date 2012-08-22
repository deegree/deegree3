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

import static org.deegree.framework.xml.XMLTools.appendElement;
import static org.deegree.framework.xml.XMLTools.getElement;
import static org.deegree.framework.xml.XMLTools.getElements;
import static org.deegree.ogcbase.CommonNamespaces.OWSNS;
import static org.deegree.ogcbase.CommonNamespaces.W3SOAP_1_1_PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.W3SOAP_ENVELOPE_1_1;
import static org.deegree.ogcbase.CommonNamespaces.getNamespaceContext;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.WebappResourceResolver;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLException;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The <code>SOAP_1_1_FacadeServletFilter</code> class is able to handle an incoming SOAP requests.
 * <p>
 * It is also able to handle multipart messages, by using the {@link RequestMultiPartHandler}.
 * </p>
 * <p>
 * Following filter-parameters are supported:
 * <ol>
 * <li>multipart.handler -- should denote a sub class of RequestMultipartHandler, which can be used to handle
 * multiparts</li>
 * <li>error.namespace -- the default namespace of error messages, default to: http://www.opengis.net/ows </li>
 * <li>wsdl.location -- the location of a wsdl file which will be sent to a requesting client (GET->wsdl)</li>
 * <li>soap.mustUnderstand -- A comma separated list of namespace bound strings a soap service must understand e.g
 * {http://some.namespace.org/}:CoolElement,{http://other.namespace.org/}:HotElement </li>
 * <li>only.except.soap -- if 'true' the service will reject all incoming request which are not soap encoded except for
 * the get -wsdl request</li>
 * </ol>
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */

public class SOAP_1_1_FacadeServletFilter implements Filter {

    private static ILogger LOG = LoggerFactory.getLogger( SOAP_1_1_FacadeServletFilter.class );

    private static NamespaceContext nsContext = getNamespaceContext();

    private URI defaultErrorNamespace = OWSNS;

    private RequestMultiPartHandler multiPartHandler = null;

    private XMLFragment wsdlDescription = null;

    private List<String> soapUnderstanding = new ArrayList<String>();

    private boolean onlyExceptSoap;

    @SuppressWarnings("unchecked")
    // for instantiation of the multipart handler
    public void init( FilterConfig config )
                            throws ServletException {
        String multiPartString = config.getInitParameter( "multipart.handler" );
        if ( multiPartString != null && !"".equals( multiPartString.trim() ) ) {
            // try to instantiate the multipart handler.
            try {
                Class<?> c = Class.forName( multiPartString );
                Constructor<RequestMultiPartHandler> con = (Constructor<RequestMultiPartHandler>) c.getConstructor();
                // call constructor and instantiate a new MultipartHandler
                multiPartHandler = con.newInstance();
                LOG.logDebug( "Successfully Instantiated class: " + multiPartString );
            } catch ( ClassNotFoundException e ) {
                LOG.logError( e.getMessage(), e );
            } catch ( SecurityException e ) {
                LOG.logError( e.getMessage(), e );
            } catch ( NoSuchMethodException e ) {
                LOG.logError( "An empty constructor must be specified for the class: " + multiPartString
                              + ". The error message was: " + e.getMessage(), e );
            } catch ( IllegalArgumentException e ) {
                LOG.logError( "An empty constructor must be specified for the class: " + multiPartString
                              + ". The error message was: " + e.getMessage(), e );

            } catch ( InstantiationException e ) {
                LOG.logError( "Could not instantiate the configured multipart handler (" + multiPartString
                              + ") because: " + e.getMessage(), e );
            } catch ( IllegalAccessException e ) {
                LOG.logError( "Could not acquire access to the configured multipart handler (" + multiPartString
                              + ") because: " + e.getMessage(), e );
            } catch ( InvocationTargetException e ) {
                LOG.logError( "Could not invoce the configured multipart handler (" + multiPartString + ") because: "
                              + e.getMessage(), e );
            }
        }
        String errorNamespace = config.getInitParameter( "error.namespace" );
        if ( errorNamespace != null && !"".equals( errorNamespace.trim() ) ) {
            try {
                defaultErrorNamespace = new URI( errorNamespace );
            } catch ( URISyntaxException e ) {
                LOG.logError( "Configured 'error.namespace' parameter is not a valid URI, setting to "
                              + OWSNS.toASCIIString() + ". Error message was: " + e.getMessage(), e );
                defaultErrorNamespace = OWSNS;
            }
        }
        String wsdlLocation = config.getInitParameter( "wsdl.location" );
        if ( wsdlLocation != null && !"".equals( wsdlLocation.trim() ) ) {
            try {
                URL wsdlFile = WebappResourceResolver.resolveFileLocation( wsdlLocation, config.getServletContext(),
                                                                           LOG );
                wsdlDescription = new XMLFragment( wsdlFile );
            } catch ( MalformedURLException e ) {
                LOG.logError( "Could not load wsdl description document ('wsdl.location' parameter) because: "
                              + e.getMessage(), e );
            } catch ( IOException e ) {
                LOG.logError( "Could not load wsdl description document ('wsdl.location' parameter) because: "
                              + e.getMessage(), e );
            } catch ( SAXException e ) {
                LOG.logError( "Could not load wsdl description document ('wsdl.location' parameter) because: "
                              + e.getMessage(), e );
            }
        }
        String tmp = config.getInitParameter( "soap.mustUnderstand" );
        if ( tmp != null && !"".equals( tmp ) ) {
            LOG.logDebug( "The mustunderstand list contains following values: " + tmp );
            String[] names = tmp.trim().split( "," );
            if ( names != null ) {
                soapUnderstanding = Arrays.asList( names );
            }
        }

        tmp = config.getInitParameter( "only.except.soap" );
        if ( tmp != null && !"".equals( tmp ) ) {
            tmp = tmp.trim().toLowerCase();
            onlyExceptSoap = "true".equals( tmp ) || "1".equals( tmp ) || "yes".equals( tmp ) || "on".equals( tmp );
        }

        LOG.logInfo( "SOAP 1.1 Servlet Filter successfully initialized "
                     + ( ( multiPartHandler == null ) ? "without" : "with" ) + " multipart support" );
    }

    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
                            throws IOException, ServletException {
        ServletRequestWrapper requestWrapper = null;
        if ( request instanceof ServletRequestWrapper ) {
            LOG.logDebug( "the incoming request is actually an org.deegree.enterprise.servlet.RequestWrapper, so not creating new instance." );
            requestWrapper = (ServletRequestWrapper) request;
        } else {
            requestWrapper = new ServletRequestWrapper( (HttpServletRequest) request );
        }
        if ( requestWrapper.getMethod().equalsIgnoreCase( "GET" ) ) {
            // check for the wsdl parameter, if it is given, return the wsdl (Web Services
            // Description Language
            // (WSDL) 1.1) for this service.
            Map<String, String[]> params = requestWrapper.getParameterMap();
            if ( params != null ) {
                if ( params.containsKey( "wsdl" ) ) {
                    if ( params.keySet().size() > 1 ) {
                        sendException(
                                       response,
                                       new OGCWebServiceException(
                                                                   "If the wsdl keyword is supplied, no other parameters are allowed.",
                                                                   ExceptionCode.INVALIDPARAMETERVALUE ), false );
                    } else {
                        try {
                            sendWSDL( response );
                        } catch ( OGCWebServiceException e ) {
                            LOG.logError( e.getMessage(), e );
                            sendException( response, e, false );
                        }
                    }
                    return;
                }
            }
            if ( !onlyExceptSoap ) {
                chain.doFilter( requestWrapper, response );
            } else {
                sendException(
                               response,
                               new OGCWebServiceException(
                                                           "This service only excepts soap version 1.1 encoded requests.",
                                                           ExceptionCode.INVALID_FORMAT ), false );
            }
        } else {
            response.setCharacterEncoding( CharsetUtils.getSystemCharset() );

            BufferedReader reader = new BufferedReader( new InputStreamReader( requestWrapper.getInputStream() ) );
            String firstLine = reader.readLine();
            LOG.logDebug( "first line of request: " + firstLine );
            if ( firstLine == null ) {
                LOG.logInfo( "No request characters found, not handling request" );
                // chain.doFilter( requestWrapper, response );
                sendException( response,
                               new OGCWebServiceException( "No request characters found, not handling request",
                                                           ExceptionCode.INVALIDPARAMETERVALUE ), false );
                return;
            }
            if ( LOG.isDebug() ) {
                LOG.logDebug( "OUTPUTING as Strings" );
                LOG.logDebug( firstLine );
                while ( reader.ready() ) {
                    LOG.logDebug( reader.readLine() );
                }
            }

            LOG.logDebug( "Contentype of the request: " + requestWrapper.getContentType() );

            // These values will be set according to the request properties
            boolean usingMultiparts = ( requestWrapper.getContentType() != null )
                                      && ( requestWrapper.getContentType().contains( "multipart/form-data" ) )
                                      && multiPartHandler != null;
            XMLFragment resultingRequest = new XMLFragment();
            XMLFragment[] mimeParts = null;
            if ( usingMultiparts ) {
                // because we have some multiparts, we will insert them into the request body
                try {
                    mimeParts = multiPartHandler.handleMultiparts( requestWrapper );
                    if ( mimeParts.length > 0 ) {
                        resultingRequest = mimeParts[0];
                    }
                } catch ( OGCWebServiceException e ) {
                    LOG.logError( e.getMessage(), e );
                    sendException( response, e, false );
                    return;
                }
                if ( resultingRequest == null ) {
                    LOG.logDebug( "could not generate an xml-dom representation out of the multiparts, returning an error message" );
                    sendException(
                                   response,
                                   new OGCWebServiceException(
                                                               "Could not generate an XML-DOM-representation out of the multiparts",
                                                               ExceptionCode.INVALID_FORMAT ), false );
                    return;
                }
            } else {// not a mime-multipart
                try {
                    resultingRequest.load( requestWrapper.getInputStream(), XMLFragment.DEFAULT_URL );
                } catch ( XMLException e ) {
                    LOG.logError( e.getMessage(), e );
                    sendException( response,
                                   new OGCWebServiceException( "An error occurred while parsing request: "
                                                               + e.getMessage(), ExceptionCode.INVALID_FORMAT ), false );
                    return;
                } catch ( SAXException e ) {
                    LOG.logError( e.getMessage(), e );
                    sendException( response,
                                   new OGCWebServiceException( "An error occurred while parsing request: "
                                                               + e.getMessage(), ExceptionCode.INVALID_FORMAT ), false );
                    return;
                }
            }
            if ( resultingRequest.getRootElement() == null ) {
                sendException(
                               response,
                               new OGCWebServiceException(
                                                           "Could not validate your request, please check your parameters.",
                                                           ExceptionCode.INVALID_FORMAT ), false );
                return;

            }

            String s = resultingRequest.getRootElement().getNamespaceURI();
            LOG.logDebug( "Namespace of root element: " + s );

            // checking if the root elements node name equals the root name of a SOAP message
            // document. If so the SOAP
            // body must be accessed to be forwarded to the the filter/servlet
            boolean usingSoap = s.equals( W3SOAP_ENVELOPE_1_1.toASCIIString() );
            if ( usingSoap ) {
                try {
                    resultingRequest = handleSOAPRequest( resultingRequest );
                } catch ( XMLParsingException e ) {
                    LOG.logError( e.getMessage(), e );
                    sendException( response,
                                   new OGCWebServiceException( e.getMessage(), ExceptionCode.INVALID_FORMAT ),
                                   usingSoap );
                    return;
                } catch ( OGCWebServiceException e ) {
                    LOG.logError( e.getMessage(), e );
                    sendException( response, e, usingSoap );
                    return;
                }
            } else if ( onlyExceptSoap ) {
                sendException(
                               response,
                               new OGCWebServiceException(
                                                           "This service only excepts soap version 1.1 encoded requests.",
                                                           ExceptionCode.INVALID_FORMAT ), false );
                return;
            }

            if ( usingMultiparts ) {
                // append the multiparts to the root request which is stripped of any soap envelope.
                Document doc = resultingRequest.getRootElement().getOwnerDocument();
                for ( XMLFragment multipart : mimeParts ) {
                    if ( multipart != null ) {
                        Element rootElement = multipart.getRootElement();
                        if ( rootElement != null ) {
                            String nameID = rootElement.getAttribute( "originalNameID" );
                            if ( nameID != null && !"".equals( nameID ) ) {
                                Element parent = multiPartHandler.getElementForId( resultingRequest, nameID );
                                if ( parent == null ) {
                                    LOG.logError( "No element was given to append the multipart node with id: "
                                                  + nameID );
                                    sendException( response,
                                                   new OGCWebServiceException(
                                                                               "An error occurred while processing multipart with id: "
                                                                                                       + nameID,
                                                                               ExceptionCode.INTERNAL_SERVER_ERROR ),
                                                   usingSoap );
                                    return;
                                }
                                Element imported = (Element) doc.importNode( multipart.getRootElement(), true );
                                parent.appendChild( imported );
                            } else {
                                LOG.logError( "No nameID found in the originalNameID attribute, this is strange!!" );
                            }
                        } else {
                            LOG.logError( "One of the mime multiparts does not contain a root element, this is strange!!" );
                        }
                    } else {
                        LOG.logError( "One of the mime multiparts is null, this is strange!!" );
                    }
                }
            }

            // the original request has been changed, set the request accordingly. Deegree will be
            // able to handle it.
            if ( usingSoap || usingMultiparts ) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream( 50000 );
                String encoding = requestWrapper.getCharacterEncoding();
                if ( encoding == null ) {
                    encoding = CharsetUtils.getSystemCharset();
                }
                OutputStreamWriter osw = new OutputStreamWriter( bos, encoding );
                resultingRequest.write( osw );
                requestWrapper.setInputStreamAsByteArray( bos.toByteArray() );
            }
            ServletResponseWrapper responseWrapper = new ServletResponseWrapper( (HttpServletResponse) response );
            chain.doFilter( requestWrapper, responseWrapper );
            if ( usingMultiparts ) {
                // send response using multiparts.
                LOG.logInfo( "Sending multiparted response is not supported yet" );
            }
            if ( usingSoap ) {
                try {
                    createSoapResponse( responseWrapper );
                } catch ( OGCWebServiceException e ) {
                    LOG.logError( e.getMessage(), e );
                    sendException( responseWrapper, e, usingSoap );
                    return;
                }
            }
            OutputStream os = responseWrapper.getOutputStream();
            String encoding = requestWrapper.getCharacterEncoding();
            LOG.logDebug( "The request uses following character encoding: " + encoding );
            if ( !CharsetUtils.getSystemCharset().equals( encoding ) ) {
                LOG.logDebug( "The request uses following character encoding: " + encoding
                              + " setting to CharsetUtils.getSystemCharsset: " + CharsetUtils.getSystemCharset() );
                encoding = CharsetUtils.getSystemCharset();
            }
            String responseString = ( (ServletResponseWrapper.ProxyServletOutputStream) os ).toString( encoding );
            os.close();
            if ( LOG.isDebug() ) {
                LOG.logDebug( "Responding with: " + responseString );
            }
            PrintWriter writer = response.getWriter();
            writer.write( responseString );
            writer.flush();
            writer.close();
        }

    }

    /**
     * @param responseWrapper
     * @throws OGCWebServiceException
     *             representing the exception which was found inside the response
     */
    private void createSoapResponse( ServletResponseWrapper responseWrapper )
                            throws OGCWebServiceException {
        String contentType = responseWrapper.getContentType();
        LOG.logDebug( "Creating soap response with content type: " + contentType );
        if ( contentType != null ) {
            if ( contentType.contains( "xml" ) || contentType.contains( "gml" ) ) {
                try {
                    OutputStream os = responseWrapper.getOutputStream();
                    String responseString = ( (ServletResponseWrapper.ProxyServletOutputStream) os ).toString( CharsetUtils.getSystemCharset() );
                    XMLFragment responseTree = new XMLFragment( new StringReader( responseString ),
                                                                XMLFragment.DEFAULT_URL );
                    LOG.logDebug( "The original response was (which will be wrapped): "
                                  + responseTree.getAsPrettyString() );
                    Element root = responseTree.getRootElement();
                    if ( root.getLocalName().toLowerCase().contains( "exception" ) ) {
                        responseWrapper.reset();
                        throw new OGCWebServiceException( responseTree.getAsPrettyString(), ExceptionCode.SOAP_SERVER );
                    }
                    responseWrapper.setContentType( "text/xml" );
                    Document doc = XMLTools.create();
                    Element responseRoot = doc.createElementNS( W3SOAP_ENVELOPE_1_1.toASCIIString(), W3SOAP_1_1_PREFIX
                                                                                                     + ":Envelope" );
                    Element body = appendElement( responseRoot, W3SOAP_ENVELOPE_1_1, W3SOAP_1_1_PREFIX + ":Body" );
                    Node result = doc.importNode( root, true );
                    body.appendChild( result );
                    responseTree = new XMLFragment( responseRoot );
                    if ( LOG.isDebug() ) {
                        LOG.logDebug( "The soap-response will be: " + responseTree.getAsPrettyString() );
                    }
                    responseWrapper.reset();
                    os = responseWrapper.getOutputStream();
                    responseTree.prettyPrint( os );
                } catch ( IOException e ) {
                    LOG.logError( e.getMessage(), e );
                    throw new OGCWebServiceException(
                                                      "Following error occurred while creating a soap envelope of the service response: "
                                                                              + e.getMessage(),
                                                      ExceptionCode.SOAP_SERVER );
                } catch ( SAXException e ) {
                    LOG.logError( e.getMessage(), e );
                    throw new OGCWebServiceException(
                                                      "Following error occurred while creating a soap envelope of the service response: "
                                                                              + e.getMessage(),
                                                      ExceptionCode.SOAP_SERVER );
                } catch ( TransformerException e ) {
                    LOG.logError( e.getMessage(), e );
                    throw new OGCWebServiceException(
                                                      "Following error occurred while creating a soap envelope of the service response: "
                                                                              + e.getMessage(),
                                                      ExceptionCode.SOAP_SERVER );
                }
            } else {
                LOG.logInfo( "Response did not contain a known xml contentype, therefore it cannot be embedded in a soap envelope" );
            }
        } else {
            LOG.logInfo( "Response did not contain a known xml contentype, therefore it cannot be embedded in a soap envelope" );
        }
        // responseWrapper.reset();
    }

    /**
     * @param response
     *            to write to.
     * @throws IOException
     *             if a given exception could not be written to the stream
     * @throws OGCWebServiceException
     *             if no wsdl file was given.
     */
    private void sendWSDL( ServletResponse response )
                            throws IOException, OGCWebServiceException {
        if ( wsdlDescription == null ) {
            throw new OGCWebServiceException( "No wsdl description document available.",
                                              ExceptionCode.INTERNAL_SERVER_ERROR );
        }
        response.setCharacterEncoding( CharsetUtils.getSystemCharset() );
        response.setContentType( "application/xml" );
        PrintWriter writer = response.getWriter();
        writer.write( wsdlDescription.getAsPrettyString() );
        writer.flush();
        writer.close();

    }

    /**
     * Handles a SOAP 1.1. envelope request. The given xml-dom tree will be traversed and the content of the body will
     * be returned.
     *
     * @param xmlReq
     *            the xml-dom representation of the original request, it should be a soap-envelope bound to the
     *            namespace: http://schemas.xmlsoap.org/soap/envelope/
     * @return the contents of the soap-body never <code>null</code>
     * @throws XMLParsingException
     *             if the body could not be parsed
     * @throws OGCWebServiceException
     *             if one of the header elements was not configured to be understood.
     * @throws IllegalArgumentException
     *             if the xmlReq is <code>null</code>
     */
    protected XMLFragment handleSOAPRequest( XMLFragment xmlReq )
                            throws XMLParsingException, OGCWebServiceException, IllegalArgumentException {
        if ( xmlReq == null ) {
            throw new IllegalArgumentException( "The xmlReq element may not be null" );
        }
        LOG.logDebug( "Handling SOAP request" );
        // check header elements for mustUnderstand attributes.
        if ( LOG.isDebug() ) {
            LOG.logDebug( xmlReq.getAsPrettyString() );
        }
        Element rootElement = xmlReq.getRootElement();
        if ( rootElement == null ) {
            throw new OGCWebServiceException(
                                              "The request does not contain a root node, hence the request cannot be handled." );
        }
        checkMustUnderstandAttributes( getElement( xmlReq.getRootElement(), W3SOAP_1_1_PREFIX + ":Header", nsContext ) );
        Element elem = XMLTools.getRequiredElement( xmlReq.getRootElement(), W3SOAP_1_1_PREFIX + ":Body", nsContext );
        // use first child element
        elem = XMLTools.getElement( elem, "*[1]", nsContext );
        // extract SOAPBody
        Document doc = XMLTools.create();
        Element root = (Element) doc.importNode( elem, true );
        XMLFragment result = new XMLFragment( root );

        if ( LOG.isDebug() ) {
            LOG.logDebug( "Extracted request", result.getAsPrettyString() );
        }

        return result;
    }

    /**
     * Check all direct children of the given headerElement for the mustUndertand attribute. If one is set to '1' the
     * {namespace}:localName will be checked against the configured names. If such an Element is not configured as
     * understandable an SOAP-Fault will be sent, as described in the soap 1.1 specification.
     *
     * @param headerElement
     *            which top-level child-nodes will be checked for mustUnderstand attributes. If <code>null</code>
     *            nothing will be done.
     * @throws OGCWebServiceException
     *             if one of the found children was not configured to be understood.
     * @throws XMLParsingException
     *             if an error occurs while retrieving the child elements of the headerelement
     */
    protected void checkMustUnderstandAttributes( Element headerElement )
                            throws OGCWebServiceException, XMLParsingException {
        if ( headerElement != null ) {
            List<Element> children = getElements( headerElement, "*", nsContext );
            for ( Element child : children ) {
                if ( child != null ) {
                    String mustUnderstand = child.getAttributeNS( W3SOAP_ENVELOPE_1_1.toASCIIString(), "mustUnderstand" );
                    if ( mustUnderstand != null && !"".equals( mustUnderstand.trim() ) ) {
                        if ( "1".equals( mustUnderstand ) ) {
                            StringBuilder sb = new StringBuilder( 200 );
                            String namespace = child.getNamespaceURI();
                            if ( namespace != null ) {
                                sb.append( "{" ).append( namespace ).append( "}:" );
                            }
                            sb.append( child.getLocalName() );
                            if ( !soapUnderstanding.contains( sb.toString().trim() ) ) {
                                throw new OGCWebServiceException( "The element: " + sb.toString()
                                                                  + " is not understood by this SOAP-Server.",
                                                                  ExceptionCode.SOAP_MUST_UNDERSTAND );
                            }

                        }
                    }

                }
            }
        }
    }

    public void destroy() {
        // implements nottin.
    }

    /**
     * Sends the passed <tt>OGCWebServiceException</tt> to the calling client and flushes/closes the writer.
     *
     * @param response
     *            to write the exception message to.
     * @param e
     *            the exception to 'send' e.g. write to the stream.
     * @param usingSoap
     *            true if the exception should be wrapped inside a soap body.
     * @throws IOException
     *             if an error occurred while getting the writer of the response.
     */
    protected void sendException( ServletResponse response, OGCWebServiceException e, boolean usingSoap )
                            throws IOException {
        if ( LOG.isDebug() ) {
            Thread.dumpStack();
        }
        if ( response instanceof ServletResponseWrapper ) {
            ( (ServletResponseWrapper) response ).reset();
        }
        Document doc = XMLTools.create();
        XMLFragment errorResponse = null;
        ExceptionCode code = e.getCode();
        String exceptionCode = ExceptionCode.NOAPPLICABLECODE.value;
        if ( code != null && code.value != null ) {
            exceptionCode = code.value;
        }

        if ( usingSoap ) {
            // the specification says following content-type
            response.setContentType( "text/xml" );
            Element root = doc.createElementNS( W3SOAP_ENVELOPE_1_1.toASCIIString(), W3SOAP_1_1_PREFIX + ":Envelope" );
            Element body = appendElement( root, W3SOAP_ENVELOPE_1_1, W3SOAP_1_1_PREFIX + ":Body" );
            Element fault = appendElement( body, W3SOAP_ENVELOPE_1_1, W3SOAP_1_1_PREFIX + ":Fault" );
            if ( !( "VersionMismatch".equalsIgnoreCase( exceptionCode )
                    || "MustUnderStand".equalsIgnoreCase( exceptionCode ) || "Client".equalsIgnoreCase( exceptionCode ) || "Server".equalsIgnoreCase( exceptionCode ) ) ) {
                exceptionCode = "Server." + exceptionCode;
            } else {
                // if a soap error occurred set the internal server error 500.
                ( (HttpServletResponse) response ).setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            }
            appendElement( fault, W3SOAP_ENVELOPE_1_1, W3SOAP_1_1_PREFIX + ":faultcode", exceptionCode );
            String message = e.getMessage();
            if ( message != null && !"".equals( message.trim() ) ) {
                // this is definitely an xml file, please put it in the detailed section.
                if ( message.startsWith( "<?xml" ) ) {
                    Element detail = appendElement( fault, W3SOAP_ENVELOPE_1_1, W3SOAP_1_1_PREFIX + ":detail" );
                    try {
                        XMLFragment errorMessage = new XMLFragment( new StringReader( message ),
                                                                    XMLFragment.DEFAULT_URL );
                        Node imported = doc.importNode( errorMessage.getRootElement(), true );
                        detail.appendChild( imported );
                    } catch ( SAXException e1 ) {
                        LOG.logError( e1.getMessage(), e1 );
                        sendException(
                                       response,
                                       new OGCWebServiceException(
                                                                   "The server responded with an error message, but unable to create a valid soap response from it.",
                                                                   ExceptionCode.SOAP_SERVER ), false );
                        return;
                    }
                } else {
                    appendElement( fault, W3SOAP_ENVELOPE_1_1, W3SOAP_1_1_PREFIX + ":faultstring", message );
                }
            }
            errorResponse = new XMLFragment( root );
        } else {
            LOG.logInfo( "Sending OGCWebServiceException to client with message: ." + e.getMessage() );
            response.setContentType( "application/xml" );
            errorResponse = new XMLFragment( doc.createElementNS( CommonNamespaces.OWSNS.toASCIIString(),
                                                                  "ows:ExceptionReport" ) );
            Element errorMessage = XMLTools.appendElement( errorResponse.getRootElement(), defaultErrorNamespace,
                                                           exceptionCode );
            XMLTools.setNodeValue( errorMessage, e.getMessage() );
        }
        PrintWriter writer = response.getWriter();
        writer.write( errorResponse.getAsPrettyString() );
        writer.flush();
        writer.close();
    }
}
