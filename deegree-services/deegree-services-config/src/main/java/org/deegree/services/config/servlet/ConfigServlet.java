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

import static org.deegree.services.config.actions.Crs.checkCrs;
import static org.deegree.services.config.actions.Crs.getCodes;
import static org.deegree.services.config.actions.Crs.listCrs;
import static org.deegree.services.config.actions.Delete.delete;
import static org.deegree.services.config.actions.Download.download;
import static org.deegree.services.config.actions.Invalidate.invalidate;
import static org.deegree.services.config.actions.List.list;
import static org.deegree.services.config.actions.ListWorkspaces.listWorkspaces;
import static org.deegree.services.config.actions.Restart.restart;
import static org.deegree.services.config.actions.Update.update;
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
            data.append( "GET /config/download[/path]                                  - download currently running workspace or file in workspace\n" );
            data.append( "GET /config/download/wsname[/path]                           - download workspace with name <wsname> or file in workspace\n" );
            data.append( "GET /config/restart                                          - restart currently running workspace\n" );
            data.append( "GET /config/restart[/path]                                   - restarts all resources connected to the specified one\n" );
            data.append( "GET /config/restart/wsname                                   - restart with workspace <wsname>\n" );
            data.append( "GET /config/update                                           - rescan config files and update resources\n" );
            data.append( "GET /config/update/wsname                                    - update with workspace <wsname>, rescan config files and update resources\n" );
            data.append( "GET /config/listworkspaces                                   - list available workspace names\n" );
            data.append( "GET /config/list[/path]                                      - list currently running workspace or directory in workspace\n" );
            data.append( "GET /config/list/wsname[/path]                               - list workspace with name <wsname> or directory in workspace\n" );
            data.append( "GET /config/invalidate/datasources/tile/id/matrixset[?bbox=] - invalidate part or all of a tile store cache's tile matrix set\n" );
            data.append( "GET /config/crs/list                                         - list available CRS definitions\n" );
            data.append( "POST /config/crs/getcodes with wkt=<wkt>                     - retrieves a list of CRS codes corresponding to the WKT (POSTed KVP)\n" );
            data.append( "GET /config/crs/<code>                                       - checks if a CRS definition is available, returns true/false\n" );
            data.append( "PUT /config/upload/wsname.zip                                - upload workspace <wsname>\n" );
            data.append( "PUT /config/upload/path/file                                 - upload file into current workspace\n" );
            data.append( "PUT /config/upload/wsname/path/file                          - upload file into workspace with name <wsname>\n" );
            data.append( "DELETE /config/delete[/path]                                 - delete currently running workspace or file in workspace\n" );
            data.append( "DELETE /config/delete/wsname[/path]                          - delete workspace with name <wsname> or file in workspace\n" );
            data.append( "\nHTTP response codes used:\n" );
            data.append( "200 - ok\n" );
            data.append( "403 - if you tried something you shouldn't have\n" );
            data.append( "404 - if a file or directory needed to fulfill a request was not found\n" );
            data.append( "500 - if something serious went wrong on the server side\n" );
            IOUtils.write( data.toString(), resp.getOutputStream() );
            return;
        }

        try {
            dispatch( path, req, resp );
        } catch ( SecurityException e ) {
            resp.setStatus( 403 );
            IOUtils.write( "There were security concerns: " + e.getLocalizedMessage() + "\n", resp.getOutputStream() );
        } catch ( Throwable e ) {
            resp.setStatus( 500 );
            IOUtils.write( "Error while processing request: " + e.getLocalizedMessage() + "\n", resp.getOutputStream() );
        }
    }

    private void dispatch( String path, HttpServletRequest req, HttpServletResponse resp )
                            throws IOException, ServletException {
        if ( path.toLowerCase().startsWith( "/download" ) ) {
            download( path.substring( 9 ), resp );
        }

        if ( path.toLowerCase().startsWith( "/restart" ) ) {
            restart( path.substring( 8 ), resp );
        }

        if ( path.toLowerCase().startsWith( "/update" ) ) {
            update( path.substring( 7 ), resp );
        }

        if ( path.toLowerCase().startsWith( "/listworkspaces" ) ) {
            listWorkspaces( resp );
        } else if ( path.toLowerCase().startsWith( "/list" ) ) {
            list( path.substring( 5 ), resp );
        }

        if ( path.toLowerCase().startsWith( "/invalidate/datasources/tile/" ) ) {
            invalidate( path.substring( 29 ), req.getQueryString(), resp );
        }

        if ( path.toLowerCase().startsWith( "/delete" ) ) {
            delete( path.substring( 7 ), resp );
        }

        if ( path.toLowerCase().startsWith( "/crs/list" ) ) {
            listCrs( resp );
        } else if ( path.toLowerCase().startsWith( "/crs/getcodes" ) ) {
            getCodes( req, resp );
        } else if ( path.toLowerCase().startsWith( "/crs" ) ) {
            checkCrs( path.substring( 4 ), resp );
        }
    }

    @Override
    protected void doDelete( HttpServletRequest req, HttpServletResponse resp )
                            throws ServletException, IOException {
        doGet( req, resp );
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
        if ( path.startsWith( "/crs" ) ) {
            dispatch( path, req, resp );
        }
    }

}
