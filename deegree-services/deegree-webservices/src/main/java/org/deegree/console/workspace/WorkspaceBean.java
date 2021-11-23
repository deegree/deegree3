/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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
package org.deegree.console.workspace;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.readLines;
import static org.deegree.client.core.utils.ActionParams.getParam1;
import static org.deegree.commons.utils.net.HttpUtils.STREAM;
import static org.deegree.console.JsfUtils.indicateException;
import static org.deegree.services.controller.OGCFrontController.getModulesInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.deegree.client.core.model.UploadedFile;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.workspace.standard.ModuleInfo;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.io.Zip;
import org.deegree.commons.utils.net.HttpUtils;
import org.deegree.console.client.RequestBean;
import org.deegree.services.controller.OGCFrontController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSF Bean for controlling various global aspects of the {@link DeegreeWorkspace}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
@ManagedBean(name = "workspace")
@ApplicationScoped
public class WorkspaceBean implements Serializable {

    private static Logger LOG = LoggerFactory.getLogger( WorkspaceBean.class );

    private static final long serialVersionUID = -2225303815897732019L;

    public static final String WS_MAIN_VIEW = "/console/workspace/workspace";

    public static final String WS_UPLOAD_VIEW = "/console/workspace/upload";

    // only used when no build (Maven) module version information is available
    private static final String DEFAULT_VERSION = "3.4-pre7";

    private static final String[] WS_LIST = { "deegree-workspace-csw", "deegree-workspace-inspire",
                                             "deegree-workspace-utah", "deegree-workspace-wps" };

    private final HashMap<String, String> workspaceLocations = new HashMap<String, String>();

    private String lastMessage = "Workspace initialized.";

    private String workspaceImportUrl;

    private String workspaceImportName;

    private UploadedFile upload;

    private boolean modified;

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage( String lastMessage ) {
        this.lastMessage = lastMessage;
    }

    public String getWorkspaceImportUrl() {
        return workspaceImportUrl;
    }

    public void setWorkspaceImportUrl( String workspaceImportUrl ) {
        this.workspaceImportUrl = workspaceImportUrl;
    }

    public String getWorkspaceImportName() {
        return workspaceImportName;
    }

    public void setWorkspaceImportName( String workspaceImportName ) {
        this.workspaceImportName = workspaceImportName;
    }

    public UploadedFile getUpload() {
        return upload;
    }

    public void setUpload( UploadedFile upload ) {
        this.upload = upload;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified( boolean modified ) {
        this.modified = modified;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public static String getWsMainView() {
        return WS_MAIN_VIEW;
    }

    public static String getWsUploadView() {
        return WS_UPLOAD_VIEW;
    }

    public HashMap<String, String> getWorkspaceLocations() {
        return workspaceLocations;
    }

    public String getWorkspaceRoot() {
        return DeegreeWorkspace.getWorkspaceRoot();
    }

    public boolean getOtherAvailable() {
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
        List<String> list = DeegreeWorkspace.listWorkspaces();
        Collections.sort( list );
        return list;
    }

    public void startWorkspace() {
        String wsName = (String) getParam1();
        try {
            OGCFrontController fc = OGCFrontController.getInstance();
            fc.setActiveWorkspaceName( wsName );
            fc.reload();
            lastMessage = "Workspace has been started.";
        } catch ( Throwable t ) {
            indicateException( "Workspace startup", t );
        }
    }

    public void deleteWorkspace()
                            throws IOException {
        String wsName = (String) getParam1();
        DeegreeWorkspace dw = DeegreeWorkspace.getInstance( wsName );
        if ( dw.getLocation().isDirectory() ) {
            FileUtils.deleteDirectory( dw.getLocation() );
            lastMessage = "Workspace has been deleted.";
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
        return "/index?faces-redirect=true";
    }

    public void downloadWorkspace() {
        String wsName = (String) getParam1();
        String location = workspaceLocations.get( wsName );
        InputStream in = null;
        try {
            setWorkspaceImportName( wsName );
            importWorkspace( location );
        } catch ( Throwable t ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to download workspace: " + t.getMessage(), null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        } finally {
            closeQuietly( in );
        }
    }

    private void importWorkspace( String location ) {
        InputStream in = null;
        try {
            URL url = new URL( location );
            Pair<InputStream, HttpResponse> p = HttpUtils.getFullResponse( STREAM, location, null, null, null, 10 );
            File root = new File( getWorkspaceRoot() );
            in = p.getFirst();
            if ( p.second.getStatusLine().getStatusCode() != 200 ) {
                throw new Exception( "Download of '" + location + "' failed. Server responded with HTTP status code "
                                     + p.second.getStatusLine().getStatusCode() );
            }
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
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to import workspace: " + e.getMessage(), null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        } finally {
            closeQuietly( in );
        }
    }

    public String uploadWorkspace() {
        if ( upload == null || upload.getFileItem() == null ) {
            FacesMessage fm = new FacesMessage( SEVERITY_INFO, "Please select a workspace file first.", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return null;
        }
        LOG.info( "Uploaded workspace file: '" + upload.getFileName() + "'" );
        workspaceImportName = upload.getFileName();
        if ( workspaceImportName.endsWith( ".deegree-workspace" ) ) {
            workspaceImportName = workspaceImportName.substring( 0,
                                                                 workspaceImportName.length()
                                                                                         - ".deegree-workspace".length() );
        }
        if ( workspaceImportName.endsWith( ".zip" ) ) {
            workspaceImportName = workspaceImportName.substring( 0, workspaceImportName.length() - ".zip".length() );
        }

        return WS_UPLOAD_VIEW;
    }

    public String unzipWorkspace() {
        InputStream in = null;
        try {
            File wsRoot = new File( getWorkspaceRoot() );
            in = new FileInputStream( new File( upload.getAbsolutePath() ) );
            File target = new File( wsRoot, workspaceImportName );
            
            if ( !FileUtils.directoryContains( wsRoot, target ) ) {
                throw new Exception( "Invalid workspace name: '" + workspaceImportName + "'." );
            }
            
            if ( target.exists() ) {
                throw new Exception( "Workspace '" + workspaceImportName + "' already exists." );
            } else {
                Zip.unzip( in, target );
            }
        } catch ( Throwable t ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Workspace could not be imported: " + t.getMessage(),
                                                null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return null;
        } finally {
            closeQuietly( in );
        }
        FacesMessage fm = new FacesMessage( SEVERITY_INFO,
                                            "Workspace '" + workspaceImportName + "' added succesfully.", null );
        FacesContext.getCurrentInstance().addMessage( null, fm );
        return WS_MAIN_VIEW;
    }

    public void importWorkspace() {
        importWorkspace( workspaceImportUrl );
    }

    public List<String> downloadWorkspaceList( String url ) {
        InputStream in = null;
        try {
            Pair<InputStream, HttpResponse> p = HttpUtils.getFullResponse( STREAM, url, null, null, null, 10 );
            LOG.debug( "Retrieving list of remote workspaces from {} ", url );
            in = p.getFirst();
            if ( p.second.getStatusLine().getStatusCode() != 200 ) {
                LOG.warn( "Could not get workspace list: Server responded with HTTP status code {}.",
                          p.second.getStatusLine().getStatusCode() );
                return new ArrayList<String>();
            }
            List<String> list = readLines( in );
            List<String> res = new ArrayList<String>( list.size() );
            for ( String s : list ) {
                if ( !s.trim().isEmpty() ) {
                    String[] tokens = s.split( " ", 2 );
                    if ( tokens.length != 2 ) {
                        LOG.warn( "Invalid workspace metadata line: '" + s + "'" );
                    }
                    res.add( tokens[1] );
                    workspaceLocations.put( tokens[1], tokens[0] );
                }
            }
            return res;
        } catch ( Throwable t ) {
            LOG.warn( "Could not get workspace list: {}.", t.getMessage() );
            return new ArrayList<String>();
        } finally {
            closeQuietly( in );
        }
    }

    public List<String> getRemoteWorkspaces() {
        workspaceLocations.clear();
        List<String> list = new ArrayList<String>();
        for ( String wsArtifactName : WS_LIST ) {
            addWorkspaceLocation( wsArtifactName, list );
        }
        return list;
    }

    private void addWorkspaceLocation( String wsArtifactName, List<String> list ) {
        String url = "https://repo.deegree.org/service/rest/v1/search/assets/download?"
                + "repository=releases"
                + "&maven.groupId=org.deegree"
                + "&maven.artifactId=" + wsArtifactName
                + "&sort=version"
                + "&maven.extension=zip";
        workspaceLocations.put( wsArtifactName, url );
        list.add( wsArtifactName );
    }

    private String getVersion() {
        String version = null;
        Collection<ModuleInfo> modules = getModulesInfo();
        for ( ModuleInfo module : modules ) {
            if ( module.getArtifactId().equals( "deegree-core-commons" ) ) {
                version = module.getVersion();
                break;
            }
        }
        if ( version == null ) {
            LOG.warn( "No valid version information from Maven deegree modules available. Defaulting to "
                      + DEFAULT_VERSION );
            version = DEFAULT_VERSION;
        }
        return version;
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
