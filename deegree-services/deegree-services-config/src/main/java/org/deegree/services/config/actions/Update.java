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
 * @author <a href="mailto:markus@beefcafe.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Update {

    public static void update( String path, HttpServletResponse resp )
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
            fc.update();
        } catch ( Exception e ) {
            IOUtils.write( "Error while updating: " + e.getLocalizedMessage() + "\n", resp.getOutputStream() );
            return;
        }

        IOUtils.write( "Update complete.", resp.getOutputStream() );
    }

}
