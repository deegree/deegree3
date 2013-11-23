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
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.ResourceStates.ResourceState;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.WorkspaceUtils;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.slf4j.Logger;

/**
 * JSF bean that wraps a {@link ResourceMetadata} and actions.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public class Config implements Comparable<Config> {

    private static final Logger LOG = getLogger( Config.class );

    protected String id;

    private URL schemaURL;

    private String schemaAsText;

    private URL template;

    private String resourceOutcome;

    private final ResourceMetadata<?> metadata;

    protected Workspace workspace;

    public Config( ResourceMetadata<?> metadata, ResourceManager<?> resourceManager, String resourceOutcome,
                   boolean autoActivate ) {
        workspace = OGCFrontController.getServiceWorkspace().getNewWorkspace();
        if ( metadata != null ) {
            this.id = metadata.getIdentifier().getId();
        }
        this.metadata = metadata;
        this.resourceOutcome = resourceOutcome;
        if ( metadata != null && metadata.getProvider() instanceof AbstractResourceProvider<?> ) {
            schemaURL = ( (AbstractResourceProvider<?>) metadata.getProvider() ).getSchema();
        }
        if ( schemaURL != null ) {
            try {
                schemaAsText = IOUtils.toString( schemaURL.openStream(), "UTF-8" );
            } catch ( IOException e ) {
                LOG.warn( "Schema not available: {}", schemaURL );
                LOG.trace( "Stack trace:", e );
            }
        }
    }

    public void activate() {
        try {
            workspace.getLocationHandler().activate( metadata.getLocation() );
            workspace.add( metadata.getLocation() );
            WorkspaceUtils.reinitializeChain( workspace, metadata.getIdentifier() );
        } catch ( Exception t ) {
            t.printStackTrace();
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to activate resource: " + t.getMessage(), null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return;
        }
    }

    public void deactivate() {
        try {
            workspace.destroy( metadata.getIdentifier() );
            workspace.getLocationHandler().deactivate( metadata.getLocation() );
            List<ResourceMetadata<?>> list = new ArrayList<ResourceMetadata<?>>();
            WorkspaceUtils.collectDependents( list, workspace.getDependencyGraph().getNode( metadata.getIdentifier() ) );
            for ( ResourceMetadata<?> md : list ) {
                workspace.getLocationHandler().deactivate( md.getLocation() );
            }
        } catch ( Throwable t ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to deactivate resource: " + t.getMessage(),
                                                null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return;
        }
    }

    public String edit()
                            throws IOException {
        StringBuilder sb = new StringBuilder( "/console/generic/xmleditor?faces-redirect=true" );
        sb.append( "&id=" ).append( id );
        sb.append( "&schemaUrl=" ).append( schemaURL.toString() );
        sb.append( "&resourceProviderClass=" ).append( metadata.getIdentifier().getProvider().getCanonicalName() );
        sb.append( "&nextView=" ).append( resourceOutcome );
        return sb.toString();
    }

    public void delete() {
        try {
            workspace.getLocationHandler().delete( metadata.getLocation() );
        } catch ( Throwable t ) {
            JsfTools.indicateException( "Deleting resource file", t );
            return;
        }
        try {
            workspace.destroy( metadata.getIdentifier() );
        } catch ( Throwable t ) {
            JsfTools.indicateException( "Destroying resource", t );
        }
    }

    public void showErrors() {
        String msg = "Initialization of resource '" + id + "' failed: ";
        for ( String error : workspace.getErrorHandler().getErrors( metadata.getIdentifier() ) ) {
            msg += error;
        }
        FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, null );
        FacesContext.getCurrentInstance().addMessage( null, fm );
    }

    @Override
    public int compareTo( Config o ) {
        return id.compareTo( o.id );
    }

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public String getSchemaAsText() {
        return schemaAsText;
    }

    public void setSchemaAsText( String schemaAsText ) {
        this.schemaAsText = schemaAsText;
    }

    public URL getTemplate() {
        return template;
    }

    public void setTemplate( URL template ) {
        this.template = template;
    }

    public String getResourceOutcome() {
        return resourceOutcome;
    }

    public void setResourceOutcome( String resourceOutcome ) {
        this.resourceOutcome = resourceOutcome;
    }

    public URL getSchemaURL() {
        return schemaURL;
    }

    public void setSchemaURL( URL schemaURL ) {
        this.schemaURL = schemaURL;
    }

    public String getState() {
        ResourceState state = workspace.getStates().getState( metadata.getIdentifier() );
        return state == null ? "Deactivated" : state.toString();
    }

}
