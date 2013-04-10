//$HeadURL$
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
package org.deegree.console.webservices;

import static org.deegree.commons.config.DeegreeWorkspace.getWorkspaceRoot;
import static org.deegree.services.controller.OGCFrontController.getServiceWorkspace;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.console.Config;
import org.deegree.console.ResourceManagerMetadata;
import org.deegree.console.ResourceProviderMetadata;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.WebServicesConfiguration;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@company.com">Your Name</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
@ManagedBean
@ViewScoped
public class ServicesBean {

    private static final long serialVersionUID = -8669333203479413121L;

    private final ResourceManager resourceManager;

    private final ResourceManagerMetadata metadata;
    
    private String newConfigType;

    private List<String> newConfigTypeTemplates;

    private String newConfigTypeTemplate;

    private String newConfigId;

    private Config proxyConfig;

    public ServicesBean() {
        File proxyLocation = new File( getWorkspaceRoot(), "proxy.xml" );
        URL example = ProxyUtils.class.getResource( "/META-INF/schemas/proxy/3.0.0/example.xml" );
        URL schema = ProxyUtils.class.getResource( "/META-INF/schemas/proxy/3.0.0/proxy.xsd" );
        proxyConfig = new Config( proxyLocation, schema, example, "/console/jsf/proxy" );
        resourceManager = OGCFrontController.getServiceWorkspace().getSubsystemManager( WebServicesConfiguration.class );
        metadata = ResourceManagerMetadata.getMetadata( resourceManager );
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

    public ResourceManagerMetadata getMetadata () {
        return metadata;
    }
    
    public List<ResourceManagerMetadata> getWebserviceManagers() {
        return getResourceManagers( "service" );
    }
    
    public List<Config> getServices() {
        List<Config> configs = new ArrayList<Config>();
        for ( ResourceState<?> state : resourceManager.getStates() ) {
            configs.add( new Config( state, null, resourceManager, null, true ) );
        }
        Collections.sort( configs );
        return configs;
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

    public void setNewConfigType( String newConfigType ) {
        this.newConfigType = newConfigType;
        ResourceProvider provider = metadata.getProvider( newConfigType );
        if ( provider == null ) {
            provider = metadata.getProviders().get( 0 );
        }
        ResourceProviderMetadata md = ResourceProviderMetadata.getMetadata( provider );
        newConfigTypeTemplates = new LinkedList<String>( md.getExamples().keySet() );
    }
}
