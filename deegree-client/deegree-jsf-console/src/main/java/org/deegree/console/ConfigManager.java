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
package org.deegree.console;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static org.deegree.client.core.utils.ActionParams.getParam1;
import static org.deegree.commons.config.DeegreeWorkspace.getWorkspaceRoot;
import static org.deegree.services.controller.OGCFrontController.getServiceWorkspace;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.console.metadata.ResourceManagerMetadata;
import org.deegree.console.metadata.ResourceProviderMetadata;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.WebServicesConfiguration;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@ManagedBean
@SessionScoped
public class ConfigManager implements Serializable {

    private static final long serialVersionUID = -8669393203479413121L;

    private static final Logger LOG = getLogger( ConfigManager.class );

    private ResourceManagerMetadata currentResourceManager;

    private String newConfigType;

    private List<String> newConfigTypeTemplates;

    private String newConfigTypeTemplate;

    private String newConfigId;

    private Config proxyConfig;

    public ResourceManagerMetadata getCurrentResourceManager() {
        return currentResourceManager;
    }

    public void setCurrentResourceManager( ResourceManagerMetadata currentResourceManager ) {
        this.currentResourceManager = currentResourceManager;
    }

    public List<String> getNewConfigTypeTemplates() {
        return newConfigTypeTemplates;
    }

    public void setNewConfigTypeTemplates( List<String> newConfigTypeTemplates ) {
        this.newConfigTypeTemplates = newConfigTypeTemplates;
    }

    public String getNewConfigTypeTemplate() {
        return newConfigTypeTemplate;
    }

    public void setNewConfigTypeTemplate( String newConfigTypeTemplate ) {
        this.newConfigTypeTemplate = newConfigTypeTemplate;
    }

    public String getNewConfigId() {
        return newConfigId;
    }

    public void setNewConfigId( String newConfigId ) {
        this.newConfigId = newConfigId;
    }

    public Config getProxyConfig() {
        return proxyConfig;
    }

    public void setProxyConfig( Config proxyConfig ) {
        this.proxyConfig = proxyConfig;
    }

    public String getNewConfigType() {
        return newConfigType;
    }

    public ConfigManager() {
        File proxyLocation = new File( getWorkspaceRoot(), "proxy.xml" );
        URL example = ProxyUtils.class.getResource( "/META-INF/schemas/proxy/3.0.0/example.xml" );
        URL schema = ProxyUtils.class.getResource( "/META-INF/schemas/proxy/3.0.0/proxy.xsd" );
        proxyConfig = new Config( proxyLocation, schema, example, "/console/jsf/proxy" );
    }

    public List<ResourceManagerMetadata> getWebserviceManagers() {
        return getResourceManagers( "service" );
    }

    public List<Config> getServices() {
        List<Config> configs = new ArrayList<Config>();
        ResourceManager mgr = OGCFrontController.getServiceWorkspace().getSubsystemManager( WebServicesConfiguration.class );
        for ( ResourceState<?> state : mgr.getStates() ) {
            configs.add( new Config( state, this, mgr, null, true ) );
        }
        Collections.sort( configs );
        return configs;
    }

    public List<ResourceManagerMetadata> getDatastoreManagers() {
        return getResourceManagers( "datastore" );
    }

    public List<ResourceManagerMetadata> getMapManagers() {
        return getResourceManagers( "map" );
    }

    public List<ResourceManagerMetadata> getProcessManagers() {
        return getResourceManagers( "process" );
    }

    public List<ResourceManagerMetadata> getConnectionManagers() {
        return getResourceManagers( "connection" );
    }

    public List<ResourceManagerMetadata> getResourceManagers( String category ) {
        List<ResourceManagerMetadata> rmMetadata = new ArrayList<ResourceManagerMetadata>();
        if ( getServiceWorkspace() == null ) {
            return rmMetadata;
        }
        for ( ResourceManager mgr : getServiceWorkspace().getResourceManagers() ) {
            ResourceManagerMetadata md = ResourceManagerMetadata.getMetadata( mgr );
            if ( md != null && category.equals( md.getCategory() ) ) {
                rmMetadata.add( md );
            }
        }
        Collections.sort( rmMetadata );
        return rmMetadata;
    }

    public void refresh() {
        if ( currentResourceManager != null ) {
            for ( ResourceManager mgr : getServiceWorkspace().getResourceManagers() ) {
                ResourceManagerMetadata md = ResourceManagerMetadata.getMetadata( mgr );
                if ( md != null && md.getName().equals( currentResourceManager.getName() ) ) {
                    currentResourceManager = md;
                }
            }
        }
        File proxyLocation = new File( getServiceWorkspace().getLocation(), "proxy.xml" );
        URL example = ProxyUtils.class.getResource( "/META-INF/schemas/proxy/3.0.0/example.xml" );
        URL schema = ProxyUtils.class.getResource( "/META-INF/schemas/proxy/3.0.0/proxy.xsd" );
        proxyConfig = new Config( proxyLocation, schema, example, "/console/jsf/proxy" );
    }

    public String getStartView() {
        ResourceManagerMetadata param1 = (ResourceManagerMetadata) getParam1();
        this.currentResourceManager = param1;
        setNewConfigType( param1.getProviderNames().iterator().next() );
        return param1.getStartView();
    }

    public List<Config> getAvailableResources() {
        List<Config> configs = new ArrayList<Config>();
        if ( currentResourceManager == null || currentResourceManager.getManager() == null ) {
            FacesContext.getCurrentInstance().validationFailed();
            return new LinkedList<Config>();
        }
        for ( ResourceState state : currentResourceManager.getManager().getStates() ) {
            configs.add( new Config( state, this, currentResourceManager.getManager(),
                                     currentResourceManager.getStartView(), true ) );
        }
        Collections.sort( configs );
        return configs;
    }

    public void setNewConfigType( String newConfigType ) {
        this.newConfigType = newConfigType;
        ResourceProvider provider = currentResourceManager.getProvider( newConfigType );
        if ( provider == null ) {
            provider = currentResourceManager.getProviders().get( 0 );
        }
        ResourceProviderMetadata md = ResourceProviderMetadata.getMetadata( provider );
        newConfigTypeTemplates = new LinkedList<String>( md.getExamples().keySet() );
    }

    public String startWizard() {

        String nextView = "/console/jsf/wizard";

        ResourceProvider provider = currentResourceManager.getProvider( newConfigType );
        if ( provider == null ) {
            provider = currentResourceManager.getProviders().get( 0 );
        }

        ResourceProviderMetadata md = ResourceProviderMetadata.getMetadata( provider );
        nextView = md.getConfigWizardView();

        Map<String, Object> map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        map.put( "newConfigId", newConfigId );
        map.put( "configManager", this );
        map.put( "resourceManagerMetadata", currentResourceManager );
        if ( "/console/jsf/wizard".equals( nextView ) ) {
            if ( md.getExamples().size() == 1 ) {
                setNewConfigTypeTemplate( md.getExamples().keySet().iterator().next() );
                return createConfig();
            }
        }
        return nextView;
    }

    public String createConfig() {

        ResourceManager manager = currentResourceManager.getManager();
        ResourceProvider provider = currentResourceManager.getProvider( newConfigType );
        if ( provider == null ) {
            provider = currentResourceManager.getProviders().get( 0 );
        }
        ResourceProviderMetadata md = ResourceProviderMetadata.getMetadata( provider );

        // lookup template
        URL templateURL = null;
        if ( newConfigTypeTemplate != null ) {
            templateURL = md.getExamples().get( newConfigTypeTemplate ).getContentLocation();
            LOG.info( "Found template URL: " + templateURL );
        } else {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "No template for config.", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return null;
        }

        // let the resource manager do the dirty work
        ResourceState rs = null;
        try {
            rs = manager.createResource( newConfigId, templateURL.openStream() );
            Config c = new Config( rs, this, currentResourceManager.getManager(),
                                   currentResourceManager.getStartView(), true );
            return c.edit();
        } catch ( Throwable t ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to create config: " + t.getMessage(), null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return null;
        }
    }
}