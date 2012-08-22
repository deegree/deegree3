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

package org.deegree.portal.standard.wms.control;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.deegree.portal.context.ViewContext;
import org.deegree.portal.context.WebMapContextFactory;
import org.deegree.portal.standard.wms.Constants;

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
 * @author last edited by: $Author: mays$
 *
 * @version $Revision$ $Date$
 */
public class MapRequestDispatcher extends org.deegree.enterprise.control.RequestDispatcher {

    private static final long serialVersionUID = -9110528190805632210L;

    private ViewContext vc = null;

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

        String controllerFile = getInitParameter( "Handler.configFile" );
        if ( !( new File( controllerFile ).exists() ) ) {
            controllerFile = getServletContext().getRealPath( controllerFile );
        }
        String clientContext = this.getInitParameter( "MapContext.configFile" );
        if ( !( new File( clientContext ).exists() ) ) {
            clientContext = getServletContext().getRealPath( clientContext );
            try {
                File file = new File( clientContext );
                vc = WebMapContextFactory.createViewContext( file.toURL(), null, null );
                appHandler = new MapApplicationHandler( controllerFile, vc );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
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

        HttpSession session = request.getSession( true );
        session.setAttribute( Constants.DEFAULTMAPCONTEXT, vc );
        super.service( request, response );
    }

}
