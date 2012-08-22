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

package org.deegree.portal.standard;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.RequestDispatcher;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.version.Version;
import org.deegree.portal.Constants;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.context.WebMapContextFactory;

/**
 * This is a <code>RequestDispatcher</code> which creates a event out of a GET or POST requests.
 * <P>
 *
 * Furthermore this class implements
 *
 * <HR>
 * <B>Design Patterns:</B>:<BR>
 *
 * The following Design Patterns are used:
 * <UL>
 * <LI> Proxy
 * </UL>
 *
 * @author <a href="mailto:friebe@gmx.net">Torsten Friebe</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: mays$
 *
 * @version $Revision$ $Date$
 */
public class PortalRequestDispatcher extends RequestDispatcher {

    private static final long serialVersionUID = -1932572065148294580L;

    private static final ILogger LOG = LoggerFactory.getLogger( PortalRequestDispatcher.class );

    protected ViewContext vc = null;

    /**
     * This method initializes the servlet.
     *
     * @param cfg
     *            the servlet configuration
     *
     * @throws ServletException
     *             an exception
     */
    @Override
    public void init( ServletConfig cfg )
                            throws ServletException {
        super.init( cfg );

        String clientContext = this.getInitParameter( "MapContext.configFile" );
        if ( !( new File( clientContext ).exists() ) ) {
            clientContext = getServletContext().getRealPath( clientContext );
        }
        try {
            File file = new File( clientContext );
            vc = WebMapContextFactory.createViewContext( file.toURL(), null, null );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        try {
            if ( this.getInitParameter( "UserRepository" ) != null ) {
                URL userRepository = new URL( this.getInitParameter( "UserRepository" ) );
                getServletContext().setAttribute( Constants.USERREPOSITORY, userRepository );
            }
        } catch ( MalformedURLException e1 ) {
            e1.printStackTrace();
        }

        LOG.logInfo( "Starting deegree version " + Version.getVersion() + " on server: "
                     + this.getServletContext().getServerInfo() + " / Java version: "
                     + System.getProperty( "java.version" ) );
        
    }

    /**
     *
     *
     * @param request
     * @param response
     *
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void service( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {
        Map<String, String[]> params = request.getParameterMap();
        Set<String> keys = params.keySet();
        for ( String key : keys ) {
            String[] array = params.get( key );
            for ( String k : array ) {
                LOG.logDebug( "found parameter for key: " + key + " param: " + k );
            }
        }
        String[] rpcCalls = request.getParameterValues( "rpc" );

        if ( rpcCalls != null ) {
            if ( rpcCalls.length > 1 ) {
                LOG.logDebug( "found multiple rpc parameters" );

            }
            for ( String tmp : rpcCalls ) {
                LOG.logDebug( "Found parameter: " + tmp );
            }
        }
        HttpSession session = request.getSession( true );
        session.setAttribute( Constants.DEFAULTMAPCONTEXT, vc );
        if ( session.getAttribute( Constants.CURRENTMAPCONTEXT ) == null ) {
            session.setAttribute( Constants.CURRENTMAPCONTEXT, vc );
        }
        super.service( request, response );
    }

}
