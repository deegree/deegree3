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

import java.io.File;
import java.net.URL;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.deegree.console.featurestore.FeatureStoreConfigManager;
import org.deegree.console.jdbc.ConnectionConfigManager;
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

    private static URL MAIN_SCHEMA_URL = ConnectionConfigManager.class.getResource( "/META-INF/schemas/webservices/0.6.0/services.xsd" );

    private static URL METADATA_TEMPLATE = ConnectionConfigManager.class.getResource( "metadata_template.xml" );

    private static URL METADATA_SCHEMA_URL = ConnectionConfigManager.class.getResource( "/META-INF/schemas/webservices/0.6.0/services.xsd" );

    private final XMLConfig serviceMainConfig;

    private final XMLConfig serviceMetadataConfig;

    private final ServiceConfigManager serviceManager;

    private final StyleConfigManager styleManager;

    private final FeatureStoreConfigManager fsManager;

    private final ConnectionConfigManager connManager;

    private String lastMessage = "Workspace initialized.";

    public ConfigManager() {
        File serviceMainConfigFile = new File( OGCFrontController.getServiceWorkspace().getLocation(),
                                               "services/main.xml" );
        serviceMainConfig = new XMLConfig( true, false, serviceMainConfigFile, MAIN_SCHEMA_URL, MAIN_TEMPLATE );
        File serviceMetadataConfigFile = new File( OGCFrontController.getServiceWorkspace().getLocation(),
                                                   "services/metadata.xml" );
        serviceMetadataConfig = new XMLConfig( true, false, serviceMetadataConfigFile, METADATA_SCHEMA_URL,
                                               METADATA_TEMPLATE );
        this.serviceManager = new ServiceConfigManager();
        this.styleManager = new StyleConfigManager();
        this.fsManager = new FeatureStoreConfigManager();
        this.connManager = new ConnectionConfigManager();
    }

    public boolean getPendingChanges() {
        if ( serviceMetadataConfig.isModified() ) {
            lastMessage = "Workspace has been changed.";
            return true;
        }
        if ( serviceMainConfig.isModified() ) {
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
     * @return the connManager
     */
    public ConnectionConfigManager getConnManager() {
        return connManager;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public Object applyChanges() {
        try {
            OGCFrontController.getInstance().reload();
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        serviceMainConfig.setModified( false );
        serviceMetadataConfig.setModified( false );

        serviceManager.scan();
        styleManager.scan();
        fsManager.scan();
        connManager.scan();
        lastMessage = "Workspace changes have been applied.";
        return "";
    }
}
