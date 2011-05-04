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

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.readLines;
import static org.deegree.client.core.utils.ActionParams.getParam1;
import static org.deegree.commons.utils.net.HttpUtils.STREAM;
import static org.deegree.commons.utils.net.HttpUtils.get;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.io.FileUtils;
import org.deegree.client.core.model.UploadedFile;
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

    public static final String WS_MAIN_VIEW = "/console/workspace/workspace";

    public static final String WS_UPLOAD_VIEW = "/console/workspace/upload";

    private static final String WS_DOWNLOAD_BASE_URL = "http://download.deegree.org/deegree3/workspaces/workspaces-";

    // only used when no module version information is available
    private static final String DEFAULT_VERSION = "3.1-pre5-SNAPSHOT";

    @Getter
    private String lastMessage = "Workspace initialized.";

    @Getter
    @Setter
    private String workspaceImportUrl;

    @Getter
    @Setter
    private String workspaceImportName;

    @Getter
    @Setter
    private UploadedFile upload;

    private boolean modified;

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
        return DeegreeWorkspace.listWorkspaces();
    }

    public void startWorkspace()
                            throws Exception {

        String wsName = (String) getParam1();
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        File file = new File( ctx.getRealPath( "WEB-INF/workspace_name" ) );
        writeStringToFile( file, wsName );
        try {
            OGCFrontController.getInstance().reload( wsName );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        lastMessage = "Workspace has been started.";
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
            OGCFrontController.getServiceWorkspace().initClassloader();
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

    public void downloadWorkspace() {
        String wsName = (String) getParam1();
        InputStream in = null;
        try {
            in = get( STREAM, getDownloadBaseUrl(), null );
            for ( String s : readLines( in ) ) {
                String[] ss = s.split( " ", 2 );
                if ( ss[1].equals( wsName ) ) {
                    importWorkspace( ss[0] );
                }
            }
        } catch ( Throwable t ) {
            lastMessage = "Workspace could not be loaded: " + t.getLocalizedMessage();
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

    public String uploadWorkspace() {
        LOG.info( "Uploaded workspace file: '" + upload.getFileName() + "'" );
        workspaceImportName = upload.getFileName();
        if ( workspaceImportName.endsWith( ".deegree-workspace" ) ) {
            workspaceImportName = workspaceImportName.substring( 0,
                                                                 workspaceImportName.length()
                                                                                         - ".deegree-workspace".length() );
        }
        return WS_UPLOAD_VIEW;
    }

    public String unzipWorkspace() {
        InputStream in = null;
        try {
            File wsRoot = new File( getWorkspaceRoot() );
            in = new FileInputStream( new File( upload.getAbsolutePath() ) );
            File target = new File( wsRoot, workspaceImportName );
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

    public List<String> getRemoteWorkspaces() {
        InputStream in = null;
        try {
            in = get( STREAM, getDownloadBaseUrl(), null );
            List<String> list = readLines( in );
            List<String> res = new ArrayList<String>( list.size() );
            for ( String s : list ) {
                String[] tokens = s.split( " " );
                if ( tokens.length > 2 ) {
                    res.add( s.split( " ", 2 )[1] );
                }
            }
            return res;
        } catch ( Throwable t ) {
            t.printStackTrace();
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to retrieve remote workspaces: "
                                                                + t.getMessage(), null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        } finally {
            closeQuietly( in );
        }
        return Collections.emptyList();
    }

    private String getDownloadBaseUrl() {
        return WS_DOWNLOAD_BASE_URL + getVersion();
    }

    private String getVersion() {
        String version = DEFAULT_VERSION;
        List<DeegreeModuleInfo> modules = DeegreeModuleInfo.getRegisteredModules();
        if ( !modules.isEmpty() ) {
            if ( !( "${project.version}" ).equals( modules.get( 0 ).getVersion().getVersionNumber() ) ) {
                version = modules.get( 0 ).getVersion().getVersionNumber();
            } else {
                LOG.warn( "No valid version information for modules available. Defaulting to " + DEFAULT_VERSION );
            }
        } else {
            LOG.warn( "No valid version information for modules available. Defaulting to " + DEFAULT_VERSION );
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