//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.console;

import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.readLines;
import static org.deegree.commons.config.DeegreeWorkspace.getWorkspaceRoot;
import static org.deegree.commons.utils.net.HttpUtils.STREAM;
import static org.deegree.commons.utils.net.HttpUtils.get;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.io.FileUtils;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.io.Zip;
import org.deegree.commons.version.DeegreeModuleInfo;
import org.deegree.console.util.RequestBean;
import org.deegree.services.controller.OGCFrontController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSF Bean for controlling various global aspects of the {@link DeegreeWorkspace}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
@ManagedBean(name = "workspace")
@ApplicationScoped
public class WorkspaceBean implements Serializable {

    private static Logger LOG = LoggerFactory.getLogger( WorkspaceBean.class );

    private static final long serialVersionUID = -2225303815897732019L;

    @Getter
    private String lastMessage = "Workspace initialized.";

    @Getter
    @Setter
    private String workspaceImportUrl;

    @Getter
    @Setter
    private String workspaceImportName;

    private boolean modified;

    public String getWorkspaceRoot (){
        return DeegreeWorkspace.getWorkspaceRoot();
    }
    
    public boolean getOtherAvailable () {
        return getWorkspaceList().size() > 1;
    }
    
    /**
     * Returns the currently active {@link DeegreeWorkspace}.
     * 
     * @return the currently active workspace, never <code>null</code>
     */
    public DeegreeWorkspace getActiveWorkspace() {
        return OGCFrontController.getServiceWorkspace();
    }

    public List<String> getWorkspaceList() {
        return DeegreeWorkspace.listWorkspaces();
    }

    public void startWorkspace( ActionEvent evt )
                            throws Exception {
        if ( evt.getSource() instanceof HtmlCommandButton ) {
            String ws = ( (HtmlCommandButton) evt.getSource() ).getLabel();
            ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
            File file = new File( ctx.getRealPath( "WEB-INF/workspace_name" ) );
            writeStringToFile( file, ws );
            try {
                OGCFrontController.getInstance().reload( ws );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            lastMessage = "Workspace has been started.";
        }
    }

    public void deleteWorkspace( ActionEvent evt )
                            throws IOException {
        if ( evt.getSource() instanceof HtmlCommandButton ) {
            String ws = ( (HtmlCommandButton) evt.getSource() ).getLabel();
            DeegreeWorkspace dw = DeegreeWorkspace.getInstance( ws );
            if ( dw.getLocation().isDirectory() ) {
                FileUtils.deleteDirectory( dw.getLocation() );
                lastMessage = "Workspace has been deleted.";
            }
        }
    }

    public String applyChanges() {
        try {
            OGCFrontController.getInstance().reload();
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        modified = false;

        lastMessage = "Workspace changes have been applied.";
        FacesContext ctx = FacesContext.getCurrentInstance();
        RequestBean bean = (RequestBean) ctx.getExternalContext().getSessionMap().get( "requestBean" );
        if ( bean != null ) {
            bean.init();
        }
        return ctx.getViewRoot().getViewId();
    }

    public void downloadWorkspace( ActionEvent evt ) {
        InputStream in = null;
        try {
            if ( evt.getSource() instanceof HtmlCommandButton ) {
                String ws = ( (HtmlCommandButton) evt.getSource() ).getLabel();

                // deal with missing version information (e.g. when running in Eclipse)
                String version = DeegreeModuleInfo.getRegisteredModules().get( 0 ).getVersion().getVersionNumber();
                if ( !version.startsWith( "3" ) ) {
                    LOG.warn( "No valid version information for module available. Defaulting to 3.1" );
                    version = "3.1-SNAPSHOT";
                }
                in = get( STREAM, "http://download.deegree.org/deegree3/workspaces/workspaces-" + version, null );

                for ( String s : readLines( in ) ) {
                    String[] ss = s.split( " ", 2 );
                    if ( ss[1].equals( ws ) ) {
                        importWorkspace( ss[0] );
                    }
                }
            }
        } catch ( IOException e ) {
            lastMessage = "Workspace could not be loaded: " + e.getLocalizedMessage();
        } finally {
            closeQuietly( in );
        }
    }

    private void importWorkspace( String location ) {
        InputStream in = null;
        try {
            URL url = new URL( location );
            File root = new File( getWorkspaceRoot() );
            in = get( STREAM, location, null );
            String name = workspaceImportName;
            if ( name == null || name.isEmpty() ) {
                name = new File( url.getPath() ).getName();
                name = name.substring( 0, name.lastIndexOf( "." ) );
            }
            File target = new File( root, name );
            if ( target.exists() ) {
                lastMessage = "Workspace already exists!";
            } else {
                Zip.unzip( in, target );
                lastMessage = "Workspace has been imported.";
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            LOG.trace( "Stack trace: ", e );
            lastMessage = "Workspace could not be imported: " + e.getLocalizedMessage();
        } finally {
            closeQuietly( in );
        }
    }

    public void importWorkspace() {
        importWorkspace( workspaceImportUrl );
    }

    public List<String> getRemoteWorkspaces()
                            throws IOException {
        InputStream in = null;
        try {
            String version = DeegreeModuleInfo.getRegisteredModules().get( 0 ).getVersion().getVersionNumber();
            if ( version.equals( "${project.version}" ) ) {
                // workaround for Eclipse
                LOG.warn( "No valid version information for module available. Defaulting to 3.1" );
                version = "3.1-SNAPSHOT";
            }
            in = get( STREAM, "http://download.deegree.org/deegree3/workspaces/workspaces-" + version, null );
            List<String> list = readLines( in );
            List<String> res = new ArrayList<String>( list.size() );

            for ( String s : list ) {
                res.add( s.split( " ", 2 )[1] );
            }

            return res;
        } finally {
            closeQuietly( in );
        }
    }

    public void setModified() {
        this.modified = true;
    }

    public boolean getPendingChanges() {
        if ( modified ) {
            lastMessage = "Workspace has been changed.";
        }
        return modified;
    }
}