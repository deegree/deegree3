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
package org.deegree.services.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.services.controller.OGCFrontController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to service-related stored resources, e.g. XML schema files.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(debug = "logs resource requests")
public class ResourcesServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger( ResourcesServlet.class );

    private static final long serialVersionUID = -2072170206703402474L;

    /**
     * Returns the HTTP URL for retrieving the specified resource.
     * <p>
     * NOTE: This method will only return a correct result if the calling thread originated in the
     * {@link #doGet(HttpServletRequest, HttpServletResponse)} or
     * {@link #doPost(HttpServletRequest, HttpServletResponse)} of this class (or has been spawned as a child thread by
     * such a thread).
     * </p>
     * @param resourcePath
     * 
     * @return the HTTP URL (for GET requests)
     */
    public static String getHttpGetURL( String resourcePath ) {
        String url = OGCFrontController.getHttpGetURL();
        url = url.substring( 0, url.lastIndexOf( '/' ) );
        // TODO retrieve from config (web.xml)
        return url + "/resources/" + resourcePath;
    }

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {

        String resourcePath = request.getPathInfo();
        if ( !resourcePath.startsWith( "/" ) ) {
            throw new ServletException( "Requested resource path does not start with '/'." );
        }
        resourcePath = resourcePath.substring( 1 );

        LOG.debug( "Requested resource: " + resourcePath );
        File wsDir = OGCFrontController.getServiceWorkspace().getLocation();
        File resource = new File( wsDir, resourcePath );
        if ( !resource.exists() ) {
            throw new ServletException( "Resource " + resourcePath + " does not exist." );
        }
        if ( !resource.isFile() ) {
            throw new ServletException( "Resource " + resourcePath + " does not denote a file." );
        }
        sendResource( resource, response );
    }

    private void sendResource( File resource, HttpServletResponse response )
                            throws IOException {

        response.setContentLength( (int) resource.length() );
        String mimeType = determineMimeType( resource );
        response.setContentType( mimeType );

        byte[] buffer = new byte[4096];
        InputStream is = new FileInputStream( resource );
        OutputStream os = response.getOutputStream();
        int read = -1;
        while ( ( read = is.read( buffer ) ) > 0 ) {
            os.write( buffer, 0, read );
        }
        is.close();
        os.close();
    }

    private String determineMimeType( File resource ) {
        // TODO
        return "text/xml";
    }
}