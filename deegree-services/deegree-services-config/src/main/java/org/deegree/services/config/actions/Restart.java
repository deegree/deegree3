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

import static org.deegree.services.config.actions.Utils.getWorkspaceAndPath;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.Pair;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.WorkspaceUtils;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Restart {

    public static void restart( String path, HttpServletResponse resp )
                            throws IOException, ServletException {
        Pair<DeegreeWorkspace, String> p = getWorkspaceAndPath( path );

        resp.setContentType( "text/plain" );

        try {
            if ( p.second != null ) {
                Workspace ws = p.first.getNewWorkspace();
                List<ResourceIdentifier<?>> ids = WorkspaceUtils.getPossibleIdentifiers( ws, p.second );
                for ( ResourceIdentifier<?> id : ids ) {
                    WorkspaceUtils.reinitializeChain( ws, id );
                }
                return;
            }

            OGCFrontController fc = OGCFrontController.getInstance();
            fc.setActiveWorkspaceName( p.first.getName() );
            fc.reload();
        } catch ( Exception e ) {
            IOUtils.write( "Error while reloading: " + e.getLocalizedMessage() + "\n", resp.getOutputStream() );
            return;
        }

        IOUtils.write( "Restart complete.", resp.getOutputStream() );
    }

}
