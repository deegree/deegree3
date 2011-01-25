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

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.deegree.commons.config.DeegreeWorkspace.getWorkspaceRoot;
import static org.deegree.commons.utils.io.Zip.unzip;
import static org.deegree.commons.utils.io.Zip.zip;
import static org.deegree.services.controller.OGCFrontController.getServiceWorkspace;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.services.controller.OGCFrontController;
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

    private void download( DeegreeWorkspace ws, HttpServletResponse resp )
                            throws IOException {
        File dir = ws.getLocation();
        if ( !dir.exists() ) {
            IOUtils.write( "No such workspace.\n", resp.getOutputStream() );
            return;
        }
        resp.setContentType( "application/x-download" );
        resp.setHeader( "Content-Disposition", "attachment; filename=" + dir.getName() + ".zip" );
        resp.setContentType( "application/zip" );
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream( resp.getOutputStream() );
            zip( dir, out, null );
        } finally {
            closeQuietly( out );
        }
    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
                            throws ServletException, IOException {
        String path = req.getPathInfo();
        if ( path == null || path.equals( "/" ) ) {
            StringBuilder data = new StringBuilder( "No action specified.\n\nAvailable actions:\n" );
            data.append( "GET /config/download           - download currently running workspace\n" );
            data.append( "GET /config/download/wsname    - download workspace with name <wsname>\n" );
            data.append( "GET /config/restart            - restart currently running workspace\n" );
            data.append( "GET /config/restart/wsname     - restart with workspace <wsname>\n" );
            data.append( "PUT /config/upload/wsname.zip  - upload workspace <wsname>\n" );
            IOUtils.write( data.toString(), resp.getOutputStream() );
            return;
        }
        // TODO error responses on IO exception
        if ( path.equalsIgnoreCase( "/download" ) || path.equalsIgnoreCase( "/download/" ) ) {
            try {
                download( getServiceWorkspace(), resp );
            } catch ( IOException e ) {
                IOUtils.write( "Error while downloading: " + e.getLocalizedMessage() + "\n", resp.getOutputStream() );
            }
            return;
        }
        if ( path.toLowerCase().startsWith( "/download/" ) ) {
            String name = path.substring( 10 );
            DeegreeWorkspace newWs = DeegreeWorkspace.getInstance( name );
            if ( newWs != null ) {
                try {
                    download( newWs, resp );
                } catch ( IOException e ) {
                    IOUtils.write( "Error while downloading: " + e.getLocalizedMessage() + "\n", resp.getOutputStream() );
                }
            }
        }
        if ( path.equalsIgnoreCase( "/restart" ) || path.equalsIgnoreCase( "/restart/" ) ) {
            try {
                OGCFrontController.getInstance().reload();
            } catch ( IOException e ) {
                IOUtils.write( "Error while reloading: " + e.getLocalizedMessage() + "\n", resp.getOutputStream() );
            } catch ( URISyntaxException e ) {
                IOUtils.write( "Error while reloading: " + e.getLocalizedMessage() + "\n", resp.getOutputStream() );
            }
            return;
        }
        if ( path.toLowerCase().startsWith( "/restart/" ) ) {
            String wsName = path.substring( 9 );
            try {
                OGCFrontController.getInstance().reload( wsName );
            } catch ( IOException e ) {
                IOUtils.write( "Error while reloading: " + e.getLocalizedMessage() + "\n", resp.getOutputStream() );
            } catch ( URISyntaxException e ) {
                IOUtils.write( "Error while reloading: " + e.getLocalizedMessage() + "\n", resp.getOutputStream() );
            }
            return;
        }
    }

    @Override
    protected void doPost( HttpServletRequest req, HttpServletResponse resp )
                            throws ServletException, IOException {
        super.doPut( req, resp );
    }

    @Override
    protected void doPut( HttpServletRequest req, HttpServletResponse resp )
                            throws ServletException, IOException {
        String path = req.getPathInfo();
        if ( path == null || path.equals( "/" ) ) {
            IOUtils.write( "No action specified.\n", resp.getOutputStream() );
            return;
        }
        if ( path.length() <= 8 ) {
            IOUtils.write( "No filename specified.\n", resp.getOutputStream() );
            return;
        }
        if ( path.startsWith( "/upload/" ) ) {
            String fileName = path.substring( 8 );
            if ( fileName.isEmpty() ) {
                IOUtils.write( "No file name given.\n", resp.getOutputStream() );
                return;
            }
            String wsName = fileName.substring( 0, fileName.length() - 4 );
            String dirName = fileName.endsWith( ".zip" ) ? wsName : fileName;
            File dir = new File( getWorkspaceRoot(), dirName );
            if ( dir.exists() ) {
                IOUtils.write( "Workspace " + wsName + " exists.\n", resp.getOutputStream() );
                return;
            }
            unzip( req.getInputStream(), dir );
        }
    }

}
