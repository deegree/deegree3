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
package org.deegree.services.config.servlet;

import static org.deegree.services.config.actions.Download.download;
import static org.deegree.services.config.actions.List.list;
import static org.deegree.services.config.actions.Restart.restart;
import static org.deegree.services.config.actions.Upload.upload;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ConfigServlet extends HttpServlet {

    private static final long serialVersionUID = -4412872621677620591L;

    private static final Logger LOG = getLogger( ConfigServlet.class );

    @Override
    public void init()
                            throws ServletException {
        LOG.info( "deegree 3 configuration servlet started." );
    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
                            throws ServletException, IOException {
        String path = req.getPathInfo();
        if ( path == null || path.equals( "/" ) ) {
            StringBuilder data = new StringBuilder( "No action specified.\n\nAvailable actions:\n" );
            data.append( "GET /config/download[/path]           - download currently running workspace or file in workspace\n" );
            data.append( "GET /config/download/wsname[/path]    - download workspace with name <wsname> or file in workspace\n" );
            data.append( "GET /config/restart                   - restart currently running workspace\n" );
            data.append( "GET /config/restart/wsname            - restart with workspace <wsname>\n" );
            data.append( "GET /config/list[/path]               - list currently running workspace or directory in workspace\n" );
            data.append( "GET /config/list/wsname[/path]        - list workspace with name <wsname> or directory in workspace\n" );
            data.append( "PUT /config/upload/wsname.zip         - upload workspace <wsname>\n" );
            data.append( "PUT /config/upload/path/file          - upload file into current workspace\n" );
            data.append( "PUT /config/upload/wsname/path/file   - upload file into workspace with name <wsname>\n" );
            IOUtils.write( data.toString(), resp.getOutputStream() );
            return;
        }

        if ( path.toLowerCase().startsWith( "/download" ) ) {
            download( path.substring( 9 ), resp );
        }

        if ( path.toLowerCase().startsWith( "/restart" ) ) {
            restart( path.substring( 8 ), resp );
        }

        if ( path.toLowerCase().startsWith( "/list" ) ) {
            list( path.substring( 5 ), resp );
        }
    }

    @Override
    protected void doPost( HttpServletRequest req, HttpServletResponse resp )
                            throws ServletException, IOException {
        doPut( req, resp );
    }

    @Override
    protected void doPut( HttpServletRequest req, HttpServletResponse resp )
                            throws ServletException, IOException {
        String path = req.getPathInfo();
        if ( path == null || path.equals( "/" ) ) {
            IOUtils.write( "No action specified.\n", resp.getOutputStream() );
            return;
        }
        if ( path.startsWith( "/upload" ) ) {
            upload( path.substring( 7 ), req, resp );
        }
    }

}
