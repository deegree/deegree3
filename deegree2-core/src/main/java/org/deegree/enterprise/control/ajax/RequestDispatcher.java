//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de

 
 ---------------------------------------------------------------------------*/

package org.deegree.enterprise.control.ajax;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deegree.enterprise.servlet.ServletRequestWrapper;

/**
 * This is a <code>RequestDispatcher</code> which creates an event out of a GET or POST request.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$ $Date$
 */
public class RequestDispatcher extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static String CONFIGURATION = "Handler.configFile";
  
    protected transient ApplicationHandler appHandler = null;

    /**
     * This method initializes the servlet.
     * 
     * @param cfg
     *            the servlet configuration
     * @throws ServletException
     *             an exception
     */
    @Override
    public void init( ServletConfig cfg )
                            throws ServletException {
        super.init( cfg );

        try {
            String url = null;

            String s = getInitParameter( CONFIGURATION );
            if ( new File( s ).isAbsolute() ) {
                url = s;
            } else {
                url = getServletContext().getRealPath( s );
            }

            this.appHandler = new ApplicationHandler( url );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void service( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {    
        
        ResponseHandler responseHandler = new ResponseHandler( response );

        // deliver event to application handler
        deliverEvent( request, responseHandler );

    }

    /**
     * 
     * @param request
     * @param responseHandler
     * @throws ServletException
     */
    protected void deliverEvent( HttpServletRequest request, ResponseHandler responseHandler ) throws ServletException {
        if ( appHandler == null ) {
            try {
                String url = null;

                String s = getInitParameter( CONFIGURATION );
                if ( new File( s ).isAbsolute() ) {
                    url = s;
                } else {
                    url = getServletContext().getRealPath( s );
                }
System.out.println(url);
                this.appHandler = new ApplicationHandler( url );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
        this.appHandler.actionPerformed( getServletContext(), new ServletRequestWrapper( request ), responseHandler );
    }
}
