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
package org.deegree.security.owsproxy;

import static java.lang.System.getProperty;
import static org.deegree.framework.util.CharsetUtils.getSystemCharset;
import static org.deegree.i18n.Messages.getMessage;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deegree.enterprise.servlet.ServletRequestWrapper;
import org.deegree.enterprise.servlet.ServletResponseWrapper;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.trigger.TriggerProvider;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.MimeTypeMapper;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCRequestFactory;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.csw.discovery.GetRecords;
import org.deegree.ogcwebservices.wcs.getcoverage.GetCoverage;
import org.deegree.ogcwebservices.wfs.XMLFactory;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.transaction.Transaction;
import org.deegree.ogcwebservices.wms.operation.GetLegendGraphic;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.security.AbstractAuthentication;
import org.deegree.security.AuthenticationDocument;
import org.deegree.security.Authentications;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.SecurityConfigurationException;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.WrongCredentialsException;
import org.deegree.security.drm.model.RightType;
import org.deegree.security.drm.model.Role;
import org.deegree.security.drm.model.Service;
import org.deegree.security.drm.model.User;
import org.deegree.security.owsrequestvalidator.OWSValidator;
import org.deegree.security.owsrequestvalidator.Policy;
import org.deegree.security.owsrequestvalidator.PolicyDocument;
import org.xml.sax.SAXException;

/**
 * An OWSProxyPolicyFilter can be registered as a ServletFilter to a web context. It offers a facade that looks like a
 * OWS but additionaly enables validating incoming requests and outgoing responses against rules defined in a policy
 * document and/or a deegree user and right management system.
 * 
 * @see org.deegree.security.drm.SecurityRegistry
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ConfigurableOWSProxyServletFilter implements Filter {

    private static TriggerProvider TP = TriggerProvider.create( ConfigurableOWSProxyServletFilter.class );

    private static final ILogger LOG = LoggerFactory.getLogger( ConfigurableOWSProxyServletFilter.class );

    private FilterConfig config;

    private OWSProxyPolicyFilter pFilter;

    private Authentications authentications;

    private SecurityConfig secConfig;

    private String altRequestPage;

    private String altResponsePage;

    private boolean imageExpected = false;

    private String proxiedUrl;

    /**
     * initialize the filter with parameters from the deployment descriptor
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init( FilterConfig config )
                            throws ServletException {
        this.config = config;

        Properties validators = new Properties();
        try {
            InputStream is = ConfigurableOWSProxyServletFilter.class.getResourceAsStream( "validators.properties" );
            validators.load( is );
            is.close();
        } catch ( Exception e ) {
            throw new ServletException( e );
        }

        pFilter = new OWSProxyPolicyFilter();
        String proxyURL = "http://127.0.0.1/owsproxy/proxy";
        if ( config.getInitParameter( "PROXYURL" ) != null ) {
            proxyURL = config.getInitParameter( "PROXYURL" );
        }

        // may be null
        proxiedUrl = config.getInitParameter( "PROXIED_URL" );
        if ( proxiedUrl == null ) {
            LOG.logInfo( "NOT using service prefixes for layers and feature types." );
        } else {
            LOG.logInfo( "Using service prefix '" + proxiedUrl + "' for layers and feature types." );
        }

        Enumeration<?> iterator = config.getInitParameterNames();
        while ( iterator.hasMoreElements() ) {
            String paramName = (String) iterator.nextElement();
            String paramValue = config.getInitParameter( paramName );
            if ( paramName.endsWith( "POLICY" ) ) {
                paramValue = config.getServletContext().getRealPath( paramValue );
                File file = new File( paramValue );
                URL fileURL = null;
                try {
                    fileURL = file.toURI().toURL();
                } catch ( MalformedURLException e ) {
                    LOG.logError( "Couldn't create an url from the configured POLICY parameter: " + paramValue
                                  + " because: " + e.getMessage() );
                    throw new ServletException( e );
                }
                if ( fileURL != null ) {
                    LOG.logDebug( "OWSProxyFilter: reading configuration file from : " + fileURL.toExternalForm() );
                    initValidator( proxyURL, paramName, fileURL, validators );
                }
            }

        }
        // } catch ( Exception e ) {
        // LOG.logError( e.getMessage(), e );
        // throw new ServletException( e );
        // }
        LOG.logInfo( "OWSProxyServlet intitialized successfully" );
        LOG.logInfo( "-DCHARSET setting: " + getSystemCharset() );
        LOG.logInfo( "-Dfile.encoding setting: " + getProperty( "file.encoding" ) );
        altRequestPage = config.getInitParameter( "ALTREQUESTPAGE" );
        altResponsePage = config.getInitParameter( "ALTRESPONSEPAGE" );

        if ( altRequestPage == null ) {
            LOG.logWarning( "You did not configure the ALTREQUESTPAGE parameter." );
            LOG.logWarning( "The servlet filter will not be fully functional." );
        }
        if ( altResponsePage == null ) {
            LOG.logWarning( "You did not configure the ALTRESPONSEPAGE parameter." );
            LOG.logWarning( "The servlet filter will not be fully functional." );
        }

        try {
            initAuthentications( config.getInitParameter( "AuthenticationSettings" ) );
        } catch ( Exception e ) {
            LOG.logDebug( "Error while initializing", e );
            throw new ServletException( e );
        }
    }

    /**
     * 
     * @param configFile
     * @throws IOException
     * @throws SAXException
     * @throws XMLParsingException
     */
    private void initAuthentications( String configFile )
                            throws IOException, SAXException, XMLParsingException {
        URL url = null;
        if ( configFile == null ) {
            // TODO what to do here?
        } else {
            File file = new File( configFile );
            if ( !file.isAbsolute() ) {
                String s = this.config.getServletContext().getRealPath( configFile );
                url = new File( s ).toURI().toURL();
            } else {
                url = file.toURI().toURL();
            }
        }
        AuthenticationDocument ad = new AuthenticationDocument( url );
        authentications = ad.createAuthentications();

    }

    /**
     * 
     * @param proxyURL
     * @param paramName
     * @param paramValue
     * @param validators
     * @throws ServletException
     */
    private void initValidator( String proxyURL, String paramName, URL paramValue, Properties validators )
                            throws ServletException {
        try {
            PolicyDocument doc = new PolicyDocument( paramValue );
            Policy policy = doc.getPolicy();
            if ( secConfig == null && policy.getSecurityConfig() != null ) {
                // use security configuration of the first policy that defined one.
                // this is possible because just one security configuration can be
                // used within a deegree/VM instance
                secConfig = policy.getSecurityConfig();
            }

            LOG.logDebug( "Whitelisted parameters: " + policy.getWhitelist() );

            if ( secConfig != null ) {
                secConfig.setProxiedUrl( proxiedUrl );
            }

            int pos = paramName.indexOf( ':' );
            String service = paramName.substring( 0, pos );

            // describes the signature of the required constructor
            Class<?>[] cl = new Class<?>[2];
            cl[0] = Policy.class;
            cl[1] = String.class;

            // set parameter to submit to the constructor
            Object[] o = new Object[2];
            o[0] = policy;
            o[1] = proxyURL;

            Class<?> clzz = Class.forName( validators.getProperty( service ) );
            Constructor<?> con = clzz.getConstructor( cl );

            pFilter.addValidator( service, (OWSValidator) con.newInstance( o ) );
        } catch ( SecurityConfigurationException e ) {
            LOG.logError( "Couldn't create a policy document from given value: " + paramValue + ", because : "
                                                  + e.getMessage(), e );
            throw new ServletException( e );
        } catch ( XMLParsingException e ) {
            LOG.logError( "Couldn't create a policy from given value: " + paramValue + ", because : " + e.getMessage(),
                          e );
            throw new ServletException( e );
        } catch ( ClassNotFoundException e ) {
            LOG.logError( "The classloader couldn't find an appropriate class  for the configured service, because"
                                                  + e.getMessage(), e );
            throw new ServletException( e );
        } catch ( NoSuchMethodException e ) {
            LOG.logError( "The classloader couldn't find a constructor for the configured service, because"
                                                  + e.getMessage(), e );
            throw new ServletException( e );
        } catch ( InstantiationException e ) {
            LOG.logError( "The classloader couldn't instantiate the configured service, because" + e.getMessage(), e );
            throw new ServletException( e );
        } catch ( IllegalAccessException e ) {
            LOG.logError( "The classloader couldn't instantiate the configured service, because" + e.getMessage(), e );
            throw new ServletException( e );
        } catch ( InvocationTargetException e ) {
            LOG.logError( "The classloader couldn't instantiate the configured service, because" + e.getMessage(), e );
            throw new ServletException( e );
        }
    }

    /**
     * free resources allocated by the filter
     * 
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        config = null;
    }

    /**
     * perform filter
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     *      javax.servlet.FilterChain)
     */
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
                            throws IOException, ServletException {

        Object[] o = TP.doPreTrigger( this, request, response, chain );
        request = (ServletRequest) o[0];
        response = (ServletResponse) o[1];
        chain = (FilterChain) o[2];

        // encapsulate the servlet request into a wrapper object to ensure
        // the availability of the InputStream
        ServletRequestWrapper requestWrapper = null;

        if ( request instanceof ServletRequestWrapper ) {
            LOG.logDebug( "OWSProxySerlvetFilter: the incoming request is actually an org.deegree.enterprise.servlet.RequestWrapper, so not creating new instance." );
            requestWrapper = (ServletRequestWrapper) request;
        } else {
            requestWrapper = new ServletRequestWrapper( (HttpServletRequest) request );
        }

        LOG.logDebug( "ConfigurableOWSProxyServletFilter: GetContentype(): " + requestWrapper.getContentType() );
        OGCWebServiceRequest owsReq = null;
        try {
            owsReq = OGCRequestFactory.create( requestWrapper );
        } catch ( OGCWebServiceException e ) {
            LOG.logError( "OWSProxyServletFilter: Couln't create an OGCWebserviceRequest because: " + e.getMessage(), e );
            throw new ServletException( e.getMessage() );
        }
        imageExpected = isImageRequested( owsReq );
        // extract user from the request
        User user = null;
        try {
            user = getUser( requestWrapper, owsReq );
        } catch ( Exception e1 ) {
            handleResponseMissingAutorization( (HttpServletRequest) request, (HttpServletResponse) response, owsReq,
                                               e1.getMessage() );
            return;
        }
        requestWrapper = new SecureRequestWrapper( requestWrapper );
        try {
            pFilter.validateGeneralConditions( (HttpServletRequest) request, requestWrapper.getContentLength(), user );
            pFilter.validate( owsReq, user );
            ( (SecureRequestWrapper) requestWrapper ).setRequest( owsReq );
            if ( proxiedUrl != null ) {
                ( (SecureRequestWrapper) requestWrapper ).setUser( user );
            }
        } catch ( InvalidParameterValueException e ) {
            LOG.logError( e.getMessage(), e );
            handleRequestMissingAutorization( (HttpServletRequest) request, (HttpServletResponse) response, owsReq,
                                              e.getMessage() );
            return;
        } catch ( UnauthorizedException e ) {
            LOG.logError( e.getMessage(), e );
            handleRequestMissingAutorization( (HttpServletRequest) request, (HttpServletResponse) response, owsReq,
                                              e.getMessage() );
            return;
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            request.setAttribute( "MESSAGE", e.getMessage() );
            ServletContext sc = config.getServletContext();
            sc.getRequestDispatcher( altResponsePage ).forward( request, response );
            return;
        }
        export( requestWrapper, owsReq );

        // encapsulate the servlet response into a wrapper object to ensure
        // the availability of the OutputStream
        ServletResponseWrapper resWrap = new ServletResponseWrapper( (HttpServletResponse) response );
        logHttpRequest( requestWrapper );
        // forward request to the next filter or servlet
        chain.doFilter( requestWrapper, resWrap );
        // get result from performing the request
        OutputStream os = resWrap.getOutputStream();
        byte[] b = ( (ServletResponseWrapper.ProxyServletOutputStream) os ).toByteArray();

        if ( !imageExpected ) {
            // fixup encoding mess: convert byte array into system character set for processing
            String str = new String( b, resWrap.getCharacterEncoding() );
            b = str.getBytes( getSystemCharset() );
            LOG.logDebug( "Internal response was", str );
        } else {
            LOG.logDebug( "Expecting image." );
        }
        try {
            // validate the result of a request performing
            String mime = resWrap.getContentType();
            LOG.logDebug( "mime type raw: " + mime );
            if ( mime != null ) {
                mime = StringTools.toArray( mime, ";", false )[0];
            } else {
                if ( imageExpected ) {
                    mime = "image/jpeg";
                } else {
                    mime = "text/xml";
                }
            }
            LOG.logDebug( "mime type", mime );
            b = pFilter.validate( owsReq, b, mime, user );
        } catch ( InvalidParameterValueException ee ) {
            LOG.logError( ee.getMessage(), ee );
            handleResponseMissingAutorization( (HttpServletRequest) request, (HttpServletResponse) response, owsReq,
                                               ee.getMessage() );
            return;
        } catch ( UnauthorizedException e ) {
            LOG.logError( e.getMessage(), e );
            handleResponseMissingAutorization( (HttpServletRequest) request, (HttpServletResponse) response, owsReq,
                                               e.getMessage() );
            return;
        }

        // fix up encoding mess: convert encoding of byte array into response character set for sending
        if ( !imageExpected ) {
            if ( resWrap.getCharacterEncoding() != null ) {
                String str = new String( b, getSystemCharset() );
                b = str.getBytes( resWrap.getCharacterEncoding() );
            }
        }
        response.setContentType( resWrap.getContentType() );
        response.setCharacterEncoding( resWrap.getCharacterEncoding() );
        // write result back to the client
        os = response.getOutputStream();
        os.write( b );
        os.close();

        TP.doPostTrigger( this, b );
    }

    /**
     * exports the changed request to a XML document/string that will substitute the original request contained in the
     * passed {@link ServletRequestWrapper}
     * 
     * @param requestWrapper
     * @param owsReq
     */
    private void export( ServletRequestWrapper requestWrapper, OGCWebServiceRequest owsReq ) {
        try {
            XMLFragment doc = null;
            if ( owsReq instanceof GetFeature ) {
                doc = XMLFactory.export( (GetFeature) owsReq );
            } else if ( owsReq instanceof Transaction ) {
                doc = XMLFactory.export( (Transaction) owsReq );
            } else if ( owsReq instanceof GetRecords ) {
                doc = org.deegree.ogcwebservices.csw.discovery.XMLFactory.export( (GetRecords) owsReq );
            } else if ( owsReq instanceof org.deegree.ogcwebservices.csw.manager.Transaction ) {
                doc = org.deegree.ogcwebservices.csw.manager.XMLFactory.export( (org.deegree.ogcwebservices.csw.manager.Transaction) owsReq );
            }
            if ( doc != null ) {
                requestWrapper.setInputStreamAsByteArray( doc.getAsString().getBytes() );
            }
        } catch ( Exception e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * logs a requests parameters and meta informations
     * 
     * @param reqWrap
     */
    private void logHttpRequest( ServletRequestWrapper reqWrap ) {
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "getRemoteAddr " + reqWrap.getRemoteAddr() );
            LOG.logDebug( "getPort " + reqWrap.getServerPort() );
            LOG.logDebug( "getMethod " + reqWrap.getMethod() );
            LOG.logDebug( "getQueryString " + reqWrap.getQueryString() );
            LOG.logDebug( "getPathInfo " + reqWrap.getPathInfo() );
            LOG.logDebug( "getRequestURI " + reqWrap.getRequestURI() );
            LOG.logDebug( "getServerName " + reqWrap.getServerName() );
            LOG.logDebug( "getServerPort " + reqWrap.getServerPort() );
            LOG.logDebug( "getServletPath " + reqWrap.getServletPath() );
        }
    }

    /**
     * go to alternative page if authorization to perform the desired request ist missing
     * 
     * @param request
     * @param response
     * @param owsReq
     * @param message
     * @throws IOException
     * @throws ServletException
     */
    private void handleRequestMissingAutorization( HttpServletRequest request, HttpServletResponse response,
                                                   OGCWebServiceRequest owsReq, String message )
                            throws IOException, ServletException {
        if ( message == null ) {
            message = "missing authorization";
        }
        if ( imageExpected ) {
            int width = 500;
            int height = 500;
            if ( owsReq != null && owsReq instanceof GetMap ) {
                width = ( (GetMap) owsReq ).getWidth();
                height = ( (GetMap) owsReq ).getHeight();
            } else if ( owsReq != null && owsReq instanceof GetCoverage ) {
                Envelope env = (Envelope) ( (GetCoverage) owsReq ).getDomainSubset().getSpatialSubset().getGrid();
                width = (int) env.getWidth();
                height = (int) env.getHeight();
            }
            response.setContentType( "image/jpeg" );
            OutputStream os = response.getOutputStream();
            BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
            Graphics g = bi.getGraphics();
            g.setColor( Color.WHITE );
            g.fillRect( 0, 0, width, height );
            g.setColor( Color.BLACK );
            g.setFont( new Font( "DIALOG", Font.PLAIN, 14 ) );
            g.drawString( Messages.getString( "MISSINGAUTHORIZATION" ), 5, 60 );
            String[] lines = StringTools.toArray( message, ":|", false );
            int y = 100;
            for ( int i = 0; i < lines.length; i++ ) {
                g.drawString( lines[i], 5, y );
                y = y + 30;
            }
            g.dispose();
            try {
                ImageUtils.saveImage( bi, os, "jpeg", 0.95f );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            os.close();
        } else {
            request.setAttribute( "MESSAGE", message );
            ServletContext sc = config.getServletContext();
            sc.getRequestDispatcher( altRequestPage ).forward( request, response );
        }
    }

    /**
     * go to alternative page if authorization to deliver the result to a request is missing
     * 
     * @param request
     * @param response
     * @param owsReq
     * @param message
     * @throws IOException
     * @throws ServletException
     */
    private void handleResponseMissingAutorization( HttpServletRequest request, HttpServletResponse response,
                                                    OGCWebServiceRequest owsReq, String message )
                            throws IOException, ServletException {

        if ( imageExpected ) {
            int width = 500;
            int height = 500;
            if ( owsReq != null && owsReq instanceof GetMap ) {
                width = ( (GetMap) owsReq ).getWidth();
                height = ( (GetMap) owsReq ).getHeight();
            } else if ( owsReq != null && owsReq instanceof GetCoverage ) {
                Envelope env = (Envelope) ( (GetCoverage) owsReq ).getDomainSubset().getSpatialSubset().getGrid();
                width = (int) env.getWidth();
                height = (int) env.getHeight();
            }
            response.setContentType( "image/jpeg" );
            OutputStream os = response.getOutputStream();
            BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
            Graphics g = bi.getGraphics();
            g.setColor( Color.WHITE );
            g.fillRect( 0, 0, width, height );
            g.setColor( Color.BLACK );
            g.setFont( new Font( "DIALOG", Font.PLAIN, 14 ) );
            String[] lines = StringTools.toArray( message, ":|", false );
            int y = 100;
            for ( int i = 0; i < lines.length; i++ ) {
                g.drawString( lines[i], 5, y );
                y = y + 30;
            }
            g.dispose();
            try {
                ImageUtils.saveImage( bi, os, "jpeg", 0.95f );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
            }
            os.write( message.getBytes() );
            os.close();
        } else {
            request.setAttribute( "MESSAGE", message );
            ServletContext sc = config.getServletContext();
            sc.getRequestDispatcher( altResponsePage ).forward( request, response );
        }
    }

    /**
     * returns the user from the incoming request.
     * 
     * @param request
     * @return the user from the incoming request.
     * @throws WrongCredentialsException
     */
    private User getUser( HttpServletRequest request, OGCWebServiceRequest owsReq )
                            throws WrongCredentialsException {

        String sessionId = owsReq.getVendorSpecificParameter( "SESSIONID" );
        String user = owsReq.getVendorSpecificParameter( "USER" );
        String password = owsReq.getVendorSpecificParameter( "PASSWORD" );
        Map<String, String> params = new HashMap<String, String>();
        // known Authentication classes requires following parameters. Depending on the
        // concrete implementation not all parameters are used for authentication
        params.put( "SESSIONID", sessionId );
        params.put( "USER", user );
        params.put( "PASSWORD", password );
        params.put( "USERPRINCIPAL", request.getUserPrincipal().getName() );
        params.put( "IPADDRESS", request.getRemoteHost() );

        User usr = null;

        List<AbstractAuthentication> authList = authentications.getAuthenticationsAsOrderedList();
        // the available authentication implementations and their order are defined in a
        // configuration file. Depending on their order it will be tried to authenticate
        // the current user against DRM. As soon as a authentication method succeeds the
        // authenticated user will be returned. If no authentication method succeeds an
        // exception will be thrown.
        // So it can be configured which authentication methods in which order shall be used.
        StringBuffer sb = new StringBuffer( 1000 );
        if ( authList.size() == 0 ) {
            LOG.logInfo( "no authentication method defined, return null as user" );
            return null;
        }
        sb.append( "following authentication methods have been performed: " );
        for ( AbstractAuthentication authentication : authList ) {
            try {
                LOG.logDebug( "authenticate using: " + authentication.getAuthenticationName() );
                usr = authentication.authenticate( params, request );
                if ( usr != null ) {
                    return usr;
                }
                sb.append( "cannot get user with authentication method: " );
                sb.append( authentication.getAuthenticationName() ).append( " | " );
            } catch ( WrongCredentialsException e ) {
                LOG.logInfo( "user cannot be authenticated with: " + authentication.getAuthenticationName() );
                LOG.logInfo( "reason: " + e.getMessage() );
                sb.append( "authentication method " ).append( authentication.getAuthenticationName() );
                sb.append( ": " ).append( e.getMessage() ).append( " | " );
            }
        }

        // no authentication method succeeded (user is still null)
        String msg = getMessage( "OWSPROXY_UNAUTHORIZED_USER", sb );
        throw new WrongCredentialsException( msg );

    }

    private boolean isImageRequested( OGCWebServiceRequest request ) {
        boolean imageReq = false;

        if ( request instanceof GetMap ) {
            imageReq = ( (GetMap) request ).getExceptions().indexOf( "image" ) > -1
                       || ( (GetMap) request ).getFormat().indexOf( "image" ) > -1;
        } else if ( request instanceof GetLegendGraphic ) {
            imageReq = ( (GetLegendGraphic) request ).getExceptions().indexOf( "image" ) > -1
                       || ( (GetLegendGraphic) request ).getFormat().indexOf( "image" ) > -1;
        } else if ( request instanceof GetCoverage ) {
            String format = ( (GetCoverage) request ).getOutput().getFormat().getCode();
            imageReq = MimeTypeMapper.isKnownImageType( "image/" + format );
        }

        LOG.logDebug( "authorization problems expected to be returned as image: ", imageReq );

        return imageReq;
    }

    public class SecureRequestWrapper extends ServletRequestWrapper {
        private OGCWebServiceRequest req;

        private User user;

        public SecureRequestWrapper( HttpServletRequest request ) {
            super( request );
        }

        public void setRequest( OGCWebServiceRequest owsReq ) {
            this.req = owsReq;
        }

        public void setUser( User user ) {
            this.user = user;
        }

        @Override
        public String getQueryString() {
            StringBuilder sb = new StringBuilder();
            for ( Entry<String, String[]> e : getParameterMap().entrySet() ) {
                sb.append( e.getKey() );
                sb.append( "=" );
                for ( String s : e.getValue() ) {
                    try {
                        sb.append( URLEncoder.encode( s, "UTF-8" ) );
                    } catch ( UnsupportedEncodingException e1 ) {
                        // UTF8 is known
                    }
                }
                sb.append( "&" );
            }
            sb.deleteCharAt( sb.length() - 1 );
            return sb.toString();
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            if ( req == null ) {
                return null;
            }
            Map<String, String[]> map = new HashMap<String, String[]>();
            try {
                super.reinitParameterMap();
                try {
                    boolean sldAllowed = true;
                    if ( user != null ) {
                        SecurityAccessManager sam = SecurityAccessManager.getInstance();
                        SecurityAccess access = sam.acquireAccess( user );
                        Service service = access.getServiceByAddress( proxiedUrl );
                        RightType right = access.getRightByName( "SLD" );
                        sldAllowed = false;
                        for ( Role role : user.getRoles( access ) ) {
                            if ( access.hasServiceRight( service, role, right ) ) {
                                sldAllowed = true;
                                break;
                            }
                        }
                    }

                    Map<String, String[]> oldMap = super.getParameterMap();
                    for ( Entry<String, String[]> e : oldMap.entrySet() ) {
                        String name = e.getKey().toUpperCase();
                        if ( !sldAllowed && name.startsWith( "SLD" ) ) {
                            continue;
                        }
                        map.put( name, e.getValue() );
                    }
                    Map<String, String> newVsps = new HashMap<String, String>( req.getVendorSpecificParameters() );
                    List<String> whitelist = pFilter.getValidator( req.getServiceName() ).getPolicy().getWhitelist();
                    for ( String s : newVsps.keySet() ) {
                        if ( !whitelist.contains( s.toUpperCase() ) && !s.equals( "REQUEST" ) ) {
                            map.remove( s );
                        }
                    }
                } catch ( GeneralSecurityException e ) {
                    LOG.logDebug( "Security exception, proxying empty parameter map." );
                }
            } catch ( Throwable e ) {
                e.printStackTrace();

            }

            return map;
        }
    }

}
