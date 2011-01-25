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

import static org.deegree.commons.config.DeegreeWorkspace.getWorkspaceRoot;
import static org.deegree.commons.utils.io.Zip.unzip;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

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
        if ( path.isEmpty() ) {
            IOUtils.write( "No file name given.\n", resp.getOutputStream() );
            return;
        }
        String fileName = path.substring( 1 );
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
