//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.services.controller.security;

import static java.io.File.createTempFile;
import static java.lang.System.currentTimeMillis;
import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.DTD;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.utils.kvp.KVPUtils.toQueryString;
import static org.deegree.commons.utils.net.HttpUtils.STREAM;
import static org.deegree.commons.utils.net.HttpUtils.post;
import static org.deegree.commons.utils.net.HttpUtils.retrieve;
import static org.deegree.services.controller.OGCFrontController.resolveFileLocation;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.io.LoggingInputStream;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.services.controller.Credentials;
import org.deegree.services.controller.CredentialsProvider;
import org.deegree.services.controller.RequestLogger;
import org.deegree.services.controller.WebServicesConfiguration;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SecureProxy extends HttpServlet {

    static final Logger LOG = getLogger( SecureProxy.class );

    private static final long serialVersionUID = 6154340524804958669L;

    String proxiedUrl;

    private CredentialsProvider credentialsProvider;

    XMLInputFactory inFac = XMLInputFactory.newInstance();

    XMLOutputFactory outFac = XMLOutputFactory.newInstance();

    private WebServicesConfiguration serviceConfig;

    private SecurityConfiguration securityConfiguration;

    private DeegreeWorkspace workspace;

    private RequestLogger requestLogger;

    @Override
    public void init( ServletConfig config )
                            throws ServletException {
        super.init( config );

        try {
            File fallbackDir = new File( resolveFileLocation( "WEB-INF/conf", getServletContext() ).toURI() );
            workspace = DeegreeWorkspace.getInstance( null, fallbackDir );
            LOG.info( "Using workspace '{}' at '{}'", workspace.getName(), workspace.getLocation() );
        } catch ( MalformedURLException e ) {
            String msg = "Secure Proxy was NOT started, since the configuration could not be loaded.";
            LOG.error( msg );
            throw new ServletException( msg );
        } catch ( URISyntaxException e ) {
            String msg = "Secure Proxy was NOT started, since the configuration could not be loaded.";
            LOG.error( msg );
            throw new ServletException( msg );
        } catch ( IOException e ) {
            String msg = "Secure Proxy was NOT started, since the configuration could not be loaded.";
            LOG.error( msg );
            throw new ServletException( msg );
        }

        @SuppressWarnings("unchecked")
        Enumeration<String> e = config.getInitParameterNames();
        while ( e.hasMoreElements() ) {
            String param = e.nextElement();
            if ( param.equalsIgnoreCase( "proxied_url" ) ) {
                proxiedUrl = config.getInitParameter( param );
            }
        }
        if ( proxiedUrl == null ) {
            String msg = "You need to define the 'proxied_url' init parameter in the web.xml.";
            LOG.info( "Secure Proxy was NOT started:" );
            LOG.info( msg );
            throw new ServletException( msg );
        }
        // working around unwanted deegree 2 ant artefacts... an URL normalization would be nice
        proxiedUrl = proxiedUrl.replace( ":80", "" );

        workspace.initAll();

        securityConfiguration = new SecurityConfiguration( workspace );
        securityConfiguration.init();
        credentialsProvider = securityConfiguration.getCredentialsProvider();
        if ( credentialsProvider == null ) {
            String msg = "You need to provide an WEB-INF/conf/services/security/security.xml which defines at least one credentials provider.";
            LOG.info( "Secure Proxy was NOT started:" );
            LOG.info( msg );
            throw new ServletException( msg );
        }

        serviceConfig = new WebServicesConfiguration( workspace );
        serviceConfig.init();
        requestLogger = serviceConfig.getRequestLogger();

        LOG.info( "deegree 3 secure proxy initialized." );
        LOG.info( "Secured service is '{}'", proxiedUrl );
    }

    @Override
    protected void doPost( final HttpServletRequest request, final HttpServletResponse response ) {
        long startTime = currentTimeMillis();

        try {
            File tmpFile = null;
            if ( serviceConfig.getRequestLogger() != null ) {
                String dir = serviceConfig.getMainConfiguration().getFrontControllerOptions().getRequestLogging().getOutputDirectory();
                if ( dir == null ) {
                    tmpFile = createTempFile( "request", ".body" );
                } else {
                    File directory = new File( dir );
                    if ( !directory.exists() ) {
                        directory.mkdirs();
                    }
                    tmpFile = createTempFile( "request", ".body", directory );
                }
            }

            InputStream tmp = request.getInputStream();
            if ( tmpFile != null ) {
                tmp = new LoggingInputStream( tmp, new FileOutputStream( tmpFile ) );
            }

            final XMLStreamReader reader = inFac.createXMLStreamReader( tmp, request.getCharacterEncoding() );
            reader.next();
            Credentials creds = credentialsProvider.doXML( reader, request, response );
            boolean loggedIn = securityConfiguration.checkCredentials( creds );
            boolean serviceRights = securityConfiguration.verifyAddress( creds, proxiedUrl );
            final String requestURL = request.getRequestURL().toString();
            if ( loggedIn && serviceRights ) {
                final PipedOutputStream pout = new PipedOutputStream();
                final PipedInputStream pin = new PipedInputStream( pout );

                Thread writerThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            XMLStreamWriter writer = outFac.createXMLStreamWriter( pout );
                            copyXML( reader, writer, request.getRequestURL().toString() );
                            pout.close();
                        } catch ( IOException e ) {
                            LOG.debug( "IO-error occurred while proxying: '{}'", e.getLocalizedMessage() );
                            LOG.trace( "Stack trace:", e );
                        } catch ( XMLStreamException e ) {
                            LOG.debug( "IO-error occurred while proxying: '{}'", e.getLocalizedMessage() );
                            LOG.trace( "Stack trace:", e );
                        }
                    }
                };
                writerThread.start();

                Map<String, String> headers = new HashMap<String, String>();
                @SuppressWarnings("unchecked")
                Enumeration<String> iter = request.getHeaderNames();
                while ( iter.hasMoreElements() ) {
                    String next = iter.nextElement();
                    // by re-exporting the XML and omitting user/password the length may change...
                    // not removing it will cause the post call below to hang indefinitely!
                    if ( next.equalsIgnoreCase( "content-length" ) ) {
                        continue;
                    }
                    headers.put( next, request.getHeader( next ) );
                }

                InputStream in = post( STREAM, proxiedUrl, pin, headers );
                pin.close();
                OutputStream out = response.getOutputStream();
                XMLStreamReader responseReader = inFac.createXMLStreamReader( in );
                responseReader.next();
                boolean successful = copyXML( responseReader, outFac.createXMLStreamWriter( out ), requestURL )
                                     || !serviceConfig.logOnlySuccessful();
                if ( requestLogger != null && successful ) {
                    requestLogger.logXML( proxiedUrl + "?" + request.getRequestURL(), tmpFile, startTime,
                                          System.currentTimeMillis(), creds );
                } else {
                    if ( tmpFile != null ) {
                        tmpFile.delete();
                    }
                }
            } else {
                writeUnauthorized( response, loggedIn );
            }
        } catch ( UnsupportedEncodingException e ) {
            LOG.trace( "Stack trace:", e );
        } catch ( IOException e ) {
            LOG.debug( "IO-error occurred while proxying: '{}'", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( XMLStreamException e ) {
            LOG.debug( "Error while writing 'not authorized' response: '{}'", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
    }

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) {
        long startTime = currentTimeMillis();
        try {
            Map<String, String> normalizedKVPParams = KVPUtils.getNormalizedKVPMap( request.getQueryString(), null );
            Credentials creds = credentialsProvider.doKVP( normalizedKVPParams, request, response );
            boolean loggedIn = securityConfiguration.checkCredentials( creds );
            boolean serviceRights = securityConfiguration.verifyAddress( creds, proxiedUrl );
            if ( loggedIn && serviceRights ) {
                normalizedKVPParams.remove( "USER" );
                normalizedKVPParams.remove( "PASSWORD" );
                InputStream in = retrieve( STREAM, proxiedUrl, normalizedKVPParams );
                OutputStream out = response.getOutputStream();
                boolean successful = false;
                String req = normalizedKVPParams.get( "REQUEST" );
                if ( req.equalsIgnoreCase( "GetCapabilities" ) || req.equalsIgnoreCase( "GetFeature" )
                     || req.equalsIgnoreCase( "DescribeFeatureType" ) ) {
                    XMLStreamReader reader = inFac.createXMLStreamReader( in );
                    reader.next();
                    successful = copyXML( reader, outFac.createXMLStreamWriter( out ),
                                          request.getRequestURL().toString() );
                } else {
                    // TODO determine from content type if it was successful, for WFS this should not be a problem
                    copy( in, out );
                }
                successful = successful || !serviceConfig.logOnlySuccessful();
                if ( requestLogger != null && successful ) {
                    requestLogger.logKVP( proxiedUrl + "?" + request.getRequestURL(),
                                          toQueryString( normalizedKVPParams ), startTime, System.currentTimeMillis(),
                                          creds );
                }
            } else {
                writeUnauthorized( response, loggedIn );
            }
        } catch ( UnsupportedEncodingException e ) {
            LOG.trace( "Stack trace:", e );
        } catch ( IOException e ) {
            LOG.debug( "IO-error occurred while proxying: '{}'", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( XMLStreamException e ) {
            LOG.debug( "Error while writing 'not authorized' response: '{}'", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
    }

    boolean copyXML( XMLStreamReader reader, XMLStreamWriter writer, String serverUrl )
                            throws XMLStreamException {

        boolean wasSuccessful = false;

        int openElements = 0;
        boolean firstRun = true;
        while ( firstRun || openElements > 0 ) {
            firstRun = false;
            int eventType = reader.getEventType();

            switch ( eventType ) {
            case COMMENT:
                writer.writeComment( reader.getText() );
                if ( openElements == 0 ) {
                    firstRun = true;
                    reader.next();
                }
                break;
            case CDATA: {
                writer.writeCData( reader.getText() );
                break;
            }
            case DTD:
                writer.writeDTD( reader.getText() );
                firstRun = true;
                reader.next();
                break;
            case CHARACTERS: {
                writer.writeCharacters( reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength() );
                break;
            }
            case END_ELEMENT: {
                writer.writeEndElement();
                openElements--;
                break;
            }
            case START_ELEMENT: {
                String elementName = reader.getLocalName();
                if ( openElements == 0 ) {
                    wasSuccessful = elementName.indexOf( "Exception" ) == -1;
                }
                if ( reader.getNamespaceURI() == null || reader.getPrefix() == null ) {
                    writer.writeStartElement( elementName );
                } else {
                    writer.writeStartElement( reader.getPrefix(), elementName, reader.getNamespaceURI() );
                }
                // copy all namespace bindings
                for ( int i = 0; i < reader.getNamespaceCount(); i++ ) {
                    String nsPrefix = reader.getNamespacePrefix( i );
                    String nsURI = reader.getNamespaceURI( i );
                    writer.writeNamespace( nsPrefix, nsURI );
                }

                // copy all attributes
                for ( int i = 0; i < reader.getAttributeCount(); i++ ) {
                    String localName = reader.getAttributeLocalName( i );
                    String nsPrefix = reader.getAttributePrefix( i );
                    String value = reader.getAttributeValue( i );
                    String nsURI = reader.getAttributeNamespace( i );
                    if ( openElements == 0 && ( localName.equals( "user" ) || localName.equals( "password" ) ) ) {
                        continue;
                    }

                    if ( nsURI == null ) {
                        writer.writeAttribute( localName, value );
                    } else {
                        if ( nsURI.equals( "http://www.w3.org/1999/xlink" ) ) {
                            String link = value.replace( ":80", "" ); // again, normalization would be nice
                            if ( link.startsWith( proxiedUrl ) ) {
                                link = link.replace( proxiedUrl, serverUrl );
                                // next two to work around buggy servers with broken endpoints such as a misconfigured
                                // XtraServer or deegree 2
                            } else if ( elementName.equals( "Get" ) && link.endsWith( "?" ) ) {
                                link = serverUrl + "?";
                            } else if ( elementName.equals( "Post" ) ) {
                                link = serverUrl;
                            }
                            writer.writeAttribute( nsPrefix, nsURI, localName, link );
                        } else {
                            writer.writeAttribute( nsPrefix, nsURI, localName, value );
                        }
                    }
                }

                openElements++;
                break;
            }
            default: {
                break;
            }
            }
            if ( openElements > 0 ) {
                reader.next();
            }
        }

        reader.close();
        writer.close();

        return wasSuccessful;
    }

    void copy( InputStream in, OutputStream out )
                            throws IOException {
        try {
            byte[] buf = new byte[65536];
            int read;
            while ( ( read = in.read( buf ) ) != -1 ) {
                out.write( buf, 0, read );
            }
        } finally {
            try {
                in.close();
            } catch ( IOException e ) {
                LOG.trace( "Stack trace:", e );
            }
            try {
                out.close();
            } catch ( IOException e ) {
                LOG.trace( "Stack trace:", e );
            }
        }
    }

    private void writeUnauthorized( HttpServletResponse response, boolean loggedIn )
                            throws XMLStreamException, IOException {
        XMLStreamWriter out = outFac.createXMLStreamWriter( response.getOutputStream() );
        out.writeStartDocument();
        out.writeStartElement( "SecureProxyResponse" );
        out.writeStartElement( "Reason" );
        if ( !loggedIn ) {
            out.writeCharacters( "Username/Password could not be verified." );
        } else {
            out.writeCharacters( "User has no right to access the secured service." );
        }
        out.writeEndElement();
        out.writeEndElement();
        out.close();
    }

    @Override
    public void destroy() {
        workspace.destroyAll();
    }

}
