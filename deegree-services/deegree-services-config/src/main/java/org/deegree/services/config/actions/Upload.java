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
package org.deegree.services.config.actions;

import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.deegree.commons.config.DeegreeWorkspace.getWorkspaceRoot;
import static org.deegree.commons.config.DeegreeWorkspace.isWorkspace;
import static org.deegree.commons.utils.io.Zip.unzip;
import static org.deegree.services.config.actions.Utils.getWorkspaceAndPath;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.Pair;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Upload {

    public static void upload( String path, HttpServletRequest req, HttpServletResponse resp )
                            throws IOException {

        Pair<DeegreeWorkspace, String> p = getWorkspaceAndPath( path );

        resp.setContentType( "text/plain" );

        if ( p.second == null ) {
            IOUtils.write( "No file name given.\n", resp.getOutputStream() );
            return;
        }

        boolean isZip = p.second.endsWith( ".zip" ) || req.getContentType() != null
                        && req.getContentType().equals( "application/zip" );

        ServletInputStream in = null;
        try {
            in = req.getInputStream();
            if ( isZip ) {
                // unzip a workspace
                String wsName = p.second.substring( 0, p.second.length() - 4 );
                String dirName = p.second.endsWith( ".zip" ) ? wsName : p.second;
                File workspaceRoot = new File ( getWorkspaceRoot() );
                File dir = new File( workspaceRoot, dirName );
                if ( !FilenameUtils.directoryContains( workspaceRoot.getCanonicalPath(), dir.getCanonicalPath() ) ) {
                    IOUtils.write( "Workspace " + wsName + " invalid.\n", resp.getOutputStream() );
                    return;
                } else if ( isWorkspace( dirName ) ) {
                    IOUtils.write( "Workspace " + wsName + " exists.\n", resp.getOutputStream() );
                    return;
                }
                unzip( in, dir );
                IOUtils.write( "Workspace " + wsName + " uploaded.\n", resp.getOutputStream() );
            } else {
                File workspaceDir = p.first.getLocation();
                File dest = new File( workspaceDir, p.second );
                if ( !FilenameUtils.directoryContains( workspaceDir.getCanonicalPath(), dest.getCanonicalPath() ) ) {
                    IOUtils.write( "Unable to upload file: " + p.second + ".\n", resp.getOutputStream() );
                    return;
                }
                if ( !dest.getParentFile().exists() && !dest.getParentFile().mkdirs() ) {
                    IOUtils.write( "Unable to create parent directory for upload.\n", resp.getOutputStream() );
                    return;
                }
                copyInputStreamToFile( in, dest );
                IOUtils.write( dest.getName() + " uploaded.\n", resp.getOutputStream() );
            }
        } finally {
            closeQuietly( in );
        }
    }

}
