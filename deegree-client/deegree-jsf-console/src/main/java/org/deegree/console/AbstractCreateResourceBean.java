/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.console;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.deegree.console.metadata.ResourceManagerMetadata;
import org.deegree.console.metadata.ResourceProviderMetadata;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.ResourceProvider;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.deegree.workspace.standard.DefaultResourceLocation;

/**
 * JSF backing bean for views of type "Create new XYZ resource".
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.3
 */
@ManagedBean
@ViewScoped
public abstract class AbstractCreateResourceBean {

    private String id;

    private String type;

    private String configTemplate;

    private List<String> configTemplates;

    private final transient ResourceManagerMetadata metadata;

    private Workspace workspace;

    public AbstractCreateResourceBean( Class<? extends ResourceManager<?>> resourceMgrClass ) {
        workspace = OGCFrontController.getServiceWorkspace().getNewWorkspace();
        ResourceManager<?> mgr = (ResourceManager<?>) workspace.getResourceManager( resourceMgrClass );
        metadata = ResourceManagerMetadata.getMetadata( mgr, workspace );
        if ( !getTypes().isEmpty() ) {
            type = getTypes().get( 0 );
            changeType( null );
        }
    }

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType( String type ) {
        this.type = type;
    }

    public void changeType( AjaxBehaviorEvent event ) {
        ResourceProvider<?> provider = metadata.getProvider( type );
        ResourceProviderMetadata md = ResourceProviderMetadata.getMetadata( provider );
        configTemplates = new ArrayList<String>( md.getExamples().keySet() );
    }

    public List<String> getTypes() {
        return metadata.getProviderNames();
    }

    public String getConfigTemplate() {
        return configTemplate;
    }

    public void setConfigTemplate( String configTemplate ) {
        this.configTemplate = configTemplate;
    }

    public List<String> getConfigTemplates() {
        return configTemplates;
    }

    public String create() {
        ResourceProvider<?> provider = metadata.getProvider( type );
        if ( provider == null ) {
            provider = metadata.getProviders().get( 0 );
        }
        ResourceProviderMetadata md = ResourceProviderMetadata.getMetadata( provider );
        URL templateURL = md.getExamples().get( configTemplate ).getContentLocation();
        try {
            DefaultResourceIdentifier<?> ident = new DefaultResourceIdentifier( provider.getClass(), id );
            ResourceLocation<?> loc = new DefaultResourceLocation( null, ident );
            workspace.add( loc );
            ResourceMetadata<?> rmd = workspace.getResourceMetadata( ident.getProvider(), ident.getId() );
            Config c = new Config( rmd, metadata.getManager(), getOutcome(), true );
            c.setTemplate( templateURL );
            return c.edit();
        } catch ( Throwable t ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to create config: " + t.getMessage(), null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        }
        return getOutcome();
    }

    protected abstract String getOutcome();

}
