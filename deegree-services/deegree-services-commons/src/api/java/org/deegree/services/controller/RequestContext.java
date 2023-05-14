/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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

import static org.slf4j.LoggerFactory.getLogger;

import javax.servlet.http.HttpServletRequest;

import org.deegree.services.resources.ResourcesServlet;
import org.slf4j.Logger;

/**
 * Encapsulates security and URL information that are associated with a currently processed {@link OGCFrontController}
 * request.
 * 
 * @see OGCFrontController#getServletContext()
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * 
 */
public class RequestContext {

    private static final Logger LOG = getLogger( RequestContext.class );

    private final String hardcodedServicesUrl;

    private final String hardcodedResourcesUrl;

    private final String pathInfo;

    private final Credentials credentials;

    private final String webappBaseUrl;

    private final String requestedEndpointUrl;

    private final String userAgent;

    private final String xForwardedPort;

    private final String xForwardedHost;

    private final String xForwardedProto;

    /**
     * @param request
     *            request for which the context will be created, must not be <code>null</code>
     * @param credentials
     *            credentials associated with the request, can be <code>null</code>
     * @param hardcodedServicesUrl
     *            URL to be used for accessing the OGCFrontController, can be <code>null</code> (derive from request)
     * @param hardcodedResourcesUrl
     *            URL to be used for accessing the ResourcesServlet, can be <code>null</code> (derive from request)
     */
    RequestContext( HttpServletRequest request, Credentials credentials, String hardcodedServicesUrl,
                    String hardcodedResourcesUrl ) {
        this.hardcodedServicesUrl = hardcodedServicesUrl;
        this.hardcodedResourcesUrl = hardcodedResourcesUrl;
        this.credentials = credentials;
        pathInfo = request.getPathInfo();
        requestedEndpointUrl = request.getRequestURL().toString();
        webappBaseUrl = deriveWebappBaseUrl( requestedEndpointUrl, request );
        userAgent = request.getHeader( "user-agent" );
        xForwardedPort = request.getHeader( "X-Forwarded-Port" );
        xForwardedHost = request.getHeader( "X-Forwarded-Host" );
        xForwardedProto = request.getHeader( "X-Forwarded-Proto" );
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Request URL: " + requestedEndpointUrl );
            LOG.debug( "Webapp base URL (derived from request): " + webappBaseUrl );
            LOG.debug( "Hardcoded services URL: " + hardcodedServicesUrl );
            LOG.debug( "Hardcoded resources URL: " + hardcodedResourcesUrl );
        }
    }

    private String deriveWebappBaseUrl( String requestedEndpointUrl, HttpServletRequest request ) {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Servlet path: " + servletPath );
            LOG.debug( "Path Info: " + pathInfo );
        }
        int webappBaseUrlLength = requestedEndpointUrl.length() - servletPath.length();
        if ( pathInfo != null ) {
            webappBaseUrlLength -= pathInfo.length();
        }
        return requestedEndpointUrl.substring( 0, webappBaseUrlLength );
    }

    /**
     * Returns the URL for contacting the {@link OGCFrontController} instance via HTTP (including OGC service instance
     * path info, if available).
     * 
     * @return the URL (without trailing slash or question mark), never <code>null</code>
     */
    public String getServiceUrl() {
        if ( hardcodedServicesUrl != null ) {
            return getServiceUrlWithPathInfo( hardcodedServicesUrl );
        }
        return requestedEndpointUrl;
    }

    private String getServiceUrlWithPathInfo( String hardcodedUrl ) {
        String url = hardcodedUrl.trim();
        if ( url.endsWith( "/" ) || url.endsWith( "?" ) ) {
            url = url.substring( 0, url.length() - 1 );
        }
        if ( pathInfo != null ) {
            url += pathInfo;
        }
        return url;
    }

    /**
     * Returns the URL for contacting the {@link ResourcesServlet} instance via HTTP.
     * 
     * @return the URL (without trailing slash or question mark), never <code>null</code>
     */
    public String getResourcesUrl() {
        if ( hardcodedResourcesUrl != null ) {
            return hardcodedResourcesUrl;
        }
        return webappBaseUrl + "/resources";
    }

    /**
     * @return the credentials, can be <code>null</code>
     */
    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * @return the request's 'User-Agent' header, can be <code>null</code>
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * @return the request's 'X-Forwarded-Proto' header, can be <code>null</code>
     */
    public String getXForwardedProto() {
        return xForwardedProto;
    }

    /**
     * @return the request's 'X-Forwarded-Host' header, can be <code>null</code>
     */
    public String getXForwardedHost() {
        return xForwardedHost;
    }

    /**
     * @return the request's 'X-Forwarded-Port' header, can be <code>null</code>
     */
    public String getXForwardedPort() {
        return xForwardedPort;
    }

    @Override
    public String toString() {
        return "{credentials=" + credentials + ",requestURL=" + requestedEndpointUrl + "}";
    }
}
