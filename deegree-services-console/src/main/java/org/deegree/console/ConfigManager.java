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

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.console.featurestore.FeatureStoreConfigManager;
import org.deegree.console.jdbc.ConnectionConfigManager;
import org.deegree.console.metadatastore.MetadataStoreConfigManager;
import org.deegree.console.observationstore.ObservationStoreConfigManager;
import org.deegree.console.services.ServiceConfigManager;
import org.deegree.console.styles.StyleConfigManager;
import org.deegree.services.controller.OGCFrontController;

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

    private static URL MAIN_TEMPLATE = ConnectionConfigManager.class.getResource( "main_template.xml" );

    private static URL MAIN_SCHEMA_URL = ConnectionConfigManager.class.getResource( "/META-INF/schemas/controller/3.0.0/controller.xsd" );

    private static URL METADATA_TEMPLATE = ConnectionConfigManager.class.getResource( "metadata_template.xml" );

    private static URL PROXY_SCHEMA_URL = ConnectionConfigManager.class.getResource( "/META-INF/schemas/proxy/3.0.0/proxy.xsd" );

    private static URL PROXY_TEMPLATE = ConnectionConfigManager.class.getResource( "/META-INF/schemas/proxy/3.0.0/example.xml" );

    private static URL METADATA_SCHEMA_URL = ConnectionConfigManager.class.getResource( "/META-INF/schemas/metadata/3.0.0/metadata.xsd" );

    private final XMLConfig serviceMainConfig;

    private final XMLConfig serviceMetadataConfig;

    private final ServiceConfigManager serviceManager;

    private final StyleConfigManager styleManager;

    private final FeatureStoreConfigManager fsManager;

    private final ObservationStoreConfigManager osManager;

    private final MetadataStoreConfigManager msManager;

    private final ConnectionConfigManager connManager;

    private final XMLConfig proxyConfig;

    private String lastMessage = "Workspace initialized.";

    public ConfigManager() {
        File serviceMainConfigFile = new File( OGCFrontController.getServiceWorkspace().getLocation(),
                                               "services/main.xml" );
        serviceMainConfig = new XMLConfig( true, false, serviceMainConfigFile, MAIN_SCHEMA_URL, MAIN_TEMPLATE, false,
                                           null );
        File serviceMetadataConfigFile = new File( OGCFrontController.getServiceWorkspace().getLocation(),
                                                   "services/metadata.xml" );
        serviceMetadataConfig = new XMLConfig( true, false, serviceMetadataConfigFile, METADATA_SCHEMA_URL,
                                               METADATA_TEMPLATE, false, null );
        this.serviceManager = new ServiceConfigManager();
        this.styleManager = new StyleConfigManager();
        this.fsManager = new FeatureStoreConfigManager();
        this.osManager = new ObservationStoreConfigManager();
        this.connManager = new ConnectionConfigManager();
        this.msManager = new MetadataStoreConfigManager();
        File proxyFile = new File( OGCFrontController.getServiceWorkspace().getLocation(), "proxy.xml" );
        this.proxyConfig = new XMLConfig( true, false, proxyFile, PROXY_SCHEMA_URL, PROXY_TEMPLATE, true,
                                          "/console/jsf/proxy.xhtml" );
    }

    public boolean getPendingChanges() {
        if ( serviceMetadataConfig.isModified() || serviceMainConfig.isModified() || proxyConfig.isModified() ) {
            lastMessage = "Workspace has been changed.";
            return true;
        }
        if ( serviceManager.needsReloading() ) {
            lastMessage = "Workspace has been changed.";
            return true;
        }
        if ( styleManager.needsReloading() ) {
            lastMessage = "Workspace has been changed.";
            return true;
        }
        if ( fsManager.needsReloading() ) {
            lastMessage = "Workspace has been changed.";
            return true;
        }
        if ( osManager.needsReloading() ) {
            lastMessage = "Workspace has been changed.";
            return true;
        }
        if ( msManager.needsReloading() ) {
            lastMessage = "Workspace has been changed.";
            return true;
        }
        if ( connManager.needsReloading() ) {
            lastMessage = "Workspace has been changed.";
            return true;
        }
        return false;
    }

    public static ConfigManager getApplicationInstance() {
        return (ConfigManager) FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get(
                                                                                                               "configManager" );
    }

    public XMLConfig getServiceMainConfig() {
        return serviceMainConfig;
    }

    public XMLConfig getServiceMetadataConfig() {
        return serviceMetadataConfig;
    }

    public XMLConfig getProxyConfig() {
        return proxyConfig;
    }

    /**
     * @return the serviceManager
     */
    public ServiceConfigManager getServiceManager() {
        return serviceManager;
    }

    /**
     * @return the styleManager
     */
    public StyleConfigManager getStyleManager() {
        return styleManager;
    }

    /**
     * @return the fsManager
     */
    public FeatureStoreConfigManager getFsManager() {
        return fsManager;
    }

    /**
     * @return the osManager
     */
    public ObservationStoreConfigManager getOsManager() {
        return osManager;
    }

    public MetadataStoreConfigManager getMsManager() {
        return msManager;
    }

    /**
     * @return the connManager
     */
    public ConnectionConfigManager getConnManager() {
        return connManager;
    }

    public String getLastMessage() {
        return lastMessage;
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
            applyChanges();
        }
    }

    public String applyChanges() {
        try {
            OGCFrontController.getInstance().reload();
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        serviceMainConfig.setModified( false );
        serviceMetadataConfig.setModified( false );
        proxyConfig.setModified( false );

        serviceManager.scan();
        styleManager.scan();
        fsManager.scan();
        osManager.scan();
        msManager.scan();
        connManager.scan();
        lastMessage = "Workspace changes have been applied.";
        return FacesContext.getCurrentInstance().getViewRoot().getViewId();
    }
}
