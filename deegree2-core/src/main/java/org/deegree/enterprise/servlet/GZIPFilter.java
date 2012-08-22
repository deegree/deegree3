//$HeadURL$
/*----------------------------------------------------------------------------
 This file originated as work from Jayson Falkner.

 Copyright 2003 Jayson Falkner (jayson@jspinsider.com)
 This code is from "Servlets and JavaServer pages; the J2EE Web Tier",
 http://www.jspbook.com. You may freely use the code both commercially
 and non-commercially. If you like the code, please pick up a copy of
 the book and help support the authors, development of more free code,
 and the JSP/Servlet/J2EE community.
 ----------------------------------------------------------------------------*/
package org.deegree.enterprise.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 * ServletFilter that enables compression of response content, if the client supports GZIP compression
 * (accept-encoding=gzip ...).
 * 
 * @author <a href="mailto:jayson@jspinsider.com">Jayson Falkner</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GZIPFilter implements Filter {

    private static final ILogger LOG = LoggerFactory.getLogger( GZIPFilter.class );

    /**
     * @param req
     * @param res
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    public void doFilter( ServletRequest req, ServletResponse res, FilterChain chain )
                            throws IOException, ServletException {
        if ( req instanceof HttpServletRequest ) {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;
            String ae = request.getHeader( "accept-encoding" );
            if ( ae != null && ae.indexOf( "gzip" ) != -1 ) {
                LOG.logDebug( "GZIP supported, compressing." );
                GZIPResponseWrapper wrappedResponse = new GZIPResponseWrapper( response );
                chain.doFilter( req, wrappedResponse );
                wrappedResponse.finishResponse();
                return;
            }
            chain.doFilter( req, res );
        }
    }

    /**
     * @param filterConfig
     */
    public void init( FilterConfig filterConfig ) {
        // noop
    }

    /**
     *
     */
    public void destroy() {
        // noop
    }
}