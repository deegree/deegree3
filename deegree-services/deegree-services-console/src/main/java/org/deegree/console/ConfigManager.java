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
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.io.FileUtils;
import org.deegree.client.generic.RequestBean;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceManagerMetadata;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.utils.io.Zip;
import org.deegree.commons.version.DeegreeModuleInfo;
import org.deegree.services.controller.OGCFrontController;
import org.h2.util.IOUtils;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
@ManagedBean
@ApplicationScoped
public class ConfigManager {

    private static final Logger LOG = getLogger( ConfigManager.class );

    @Getter
    private List<ResourceManagerMetadata> resourceManagers;

    private HashMap<String, ResourceManagerMetadata> resourceManagerMap;

    @Getter
    private ResourceManagerMetadata currentResourceManager;

    @Getter
    private List<Config> availableResources;

    @Getter
    private List<String> providers;

    @Getter
    @Setter
    private String newConfigType;

    @Getter
    @Setter
    private String newConfigId;

    @Getter
    private String lastMessage = "Workspace initialized.";

    @Getter
    @Setter
    private String workspaceImportUrl;

    @Getter
    @Setter
    private String workspaceImportName;

    private String workspaceName;

    private boolean modified;

    public ConfigManager() {
        resourceManagers = new LinkedList<ResourceManagerMetadata>();
        resourceManagerMap = new HashMap<String, ResourceManagerMetadata>();
        ServiceLoader<ResourceManager> loaded = ServiceLoader.load( ResourceManager.class );
        for ( ResourceManager mgr : loaded ) {
            ResourceManagerMetadata md = mgr.getMetadata();
            if ( md != null ) {
                resourceManagers.add( md );
                resourceManagerMap.put( md.getName(), md );
            }
        }
    }

    public void resourceManagerChanged( ActionEvent evt ) {
        currentResourceManager = resourceManagerMap.get( ( (HtmlCommandLink) evt.getSource() ).getValue().toString() );
        update();
    }

    public void update() {
        availableResources = new LinkedList<Config>();
        if ( currentResourceManager != null ) {
            providers = new LinkedList<String>();
            for ( ResourceProvider p : currentResourceManager.getResourceProviders() ) {
                providers.add( p.getConfigNamespace().substring( p.getConfigNamespace().lastIndexOf( "/" ) + 1 ) );
            }
            File dir = new File( OGCFrontController.getServiceWorkspace().getLocation(),
                                 currentResourceManager.getPath() );
            if ( dir.isDirectory() ) {
                File[] fs = dir.listFiles();
                if ( fs != null ) {
                    for ( File f : fs ) {
                        if ( !f.isDirectory() ) {
                            try {
                                Config c = new Config( f, currentResourceManager, this );
                                availableResources.add( c );
                            } catch ( XMLStreamException e ) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch ( FactoryConfigurationError e ) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch ( IOException e ) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            Collections.sort( availableResources );
        }
    }

    public String createConfig() {
        File dir = new File( OGCFrontController.getServiceWorkspace().getLocation(), currentResourceManager.getPath() );
        if ( !dir.exists() && !dir.mkdirs() ) {
            // TODO error
            return "resources";
        }
        File conf = new File( dir, newConfigId + ".xml" );
        if ( !conf.getParentFile().isDirectory() && !conf.getParentFile().mkdirs() ) {
            // TODO error
        }
        boolean template = false;
        URL schemaURL = null;
        for ( ResourceProvider p : currentResourceManager.getResourceProviders() ) {
            if ( p.getConfigNamespace().endsWith( newConfigType ) ) {
                if ( p.getConfigTemplates() != null ) {
                    schemaURL = p.getConfigSchema();
                    template = true;
                    try {
                        IOUtils.copyAndClose( p.getConfigTemplates().values().iterator().next().openStream(), new FileOutputStream( conf ) );
                    } catch ( FileNotFoundException e ) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch ( IOException e ) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        try {
            Config c;
            if ( template ) {
                c = new Config( conf, currentResourceManager, this );
            } else {
                c = new Config( conf, currentResourceManager, this, schemaURL, newConfigType );
            }
            availableResources.add( c );
            Collections.sort( availableResources );
            return c.edit();
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FactoryConfigurationError e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "resources";
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

    public static ConfigManager getApplicationInstance() {
        return (ConfigManager) FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get(
                                                                                                               "configManager" );
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
            this.workspaceName = ws;
            applyChanges();
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
            OGCFrontController.getInstance().reload( workspaceName );
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
                    version = "3.1";
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

}
