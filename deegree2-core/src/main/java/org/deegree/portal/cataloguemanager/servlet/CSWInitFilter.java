//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.portal.cataloguemanager.servlet;

import java.io.FileInputStream;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CSWInitFilter implements Filter {

    private static ILogger LOG = LoggerFactory.getLogger( CSWInitFilter.class );

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init( FilterConfig config )
                            throws ServletException {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     * javax.servlet.FilterChain)
     */
    public void doFilter( ServletRequest req, ServletResponse res, FilterChain chain )
                            throws IOException, ServletException {
        ServletContext sc = ( (HttpServletRequest) req ).getSession().getServletContext();

        String userName = null;
        if ( ( (HttpServletRequest) req ).getUserPrincipal() != null ) {
            userName = ( (HttpServletRequest) req ).getUserPrincipal().getName();
        }

        String s = sc.getRealPath( "WEB-INF/conf/setup/catalogueManager_config.properties" );
        Properties p = new Properties();
        p.load( new FileInputStream( s ) );
        String tmp = ( (HttpServletRequest) req ).getRequestURI();
        if ( tmp.indexOf( "cswsetup.jsp" ) > -1 && !( "cmAdmin".equals( userName ) ) ) {
            req.setAttribute( "javax.servlet.jsp.jspException",
                              new ServletException( "user: " + userName + " is not allowed to setup catalogueManager" ) );
            req.getRequestDispatcher( "error.jsp" ).forward( req, res );
        } else {
            if ( tmp.indexOf( "oracle" ) > -1 && tmp.endsWith( ".html" ) ) {
                chain.doFilter( req, res );
            } else if ( !new Boolean( p.getProperty( "configured" ) ) ) {
                req.getRequestDispatcher( "cswsetup.jsp" ).forward( req, res );
            } else {
                if ( tmp.indexOf( "md_editor.jsp" ) > -1 && !new Boolean( p.getProperty( "editor" ) ) ) {
                    req.getRequestDispatcher( "error.jsp" ).forward( req, res );
                    LOG.logInfo( "editor is not configured to be available" );
                } else if ( tmp.indexOf( "md_editor.jsp" ) > -1 && !( "cmEditor".equals( userName ) ) ) {
                    req.setAttribute( "javax.servlet.jsp.jspException",
                                      new ServletException( "user: " + userName + " is not allowed to edit metadata" ) );
                    req.getRequestDispatcher( "error.jsp" ).forward( req, res );
                } else if ( tmp.indexOf( "md_search." ) > -1 && !new Boolean( p.getProperty( "searchClient" ) ) ) {
                    req.getRequestDispatcher( "error.jsp" ).forward( req, res );
                    LOG.logInfo( "search client is not configured to be available" );
                } else {
                    chain.doFilter( req, res );
                }
            }
        }

    }
}
