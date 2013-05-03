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
package org.deegree.console;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static org.apache.commons.io.FileUtils.copyURLToFile;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.deegree.commons.xml.XMLAdapter.DEFAULT_URL;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.Resource;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.config.ResourceState.StateType;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.console.workspace.WorkspaceBean;
import org.slf4j.Logger;

/**
 * Wraps information on a {@link Resource} and its configuration file.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Config implements Comparable<Config> {

    private static final Logger LOG = getLogger( Config.class );

    protected File location;

    protected String id;

    private URL schemaURL;

    private String content;

    private String schemaAsText;

    private URL template;

    private String resourceOutcome;

    protected ResourceManager resourceManager;

    private ResourceState<?> state;

    public Config( File location, URL schemaURL, URL template, String resourceOutcome ) {
        this.location = location;
        this.schemaURL = schemaURL;
        this.template = template;
        this.resourceOutcome = resourceOutcome;
        if ( schemaURL != null ) {
            try {
                schemaAsText = IOUtils.toString( schemaURL.openStream(), "UTF-8" );
            } catch ( IOException e ) {
                LOG.warn( "Schema not available: {}", schemaURL );
                LOG.trace( "Stack trace:", e );
            }
        }
    }

    public Config( ResourceState<?> state, ResourceManager resourceManager, String resourceOutcome, boolean autoActivate ) {
        this.state = state;
        this.id = state.getId();
        this.location = state.getConfigLocation();
        this.resourceManager = resourceManager;
        this.resourceOutcome = resourceOutcome;
        ResourceProvider provider = state.getProvider();
        if ( provider != null && provider.getConfigSchema() != null ) {
            schemaURL = provider.getConfigSchema();
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

    public String getState() {
        ResourceState<?> stateType = resourceManager.getState( id );
        if ( stateType == null ) {
            return "unknown";
        }
        return stateType.getType().name();
    }

    public void activate() {
        try {
            resourceManager.activate( id );
        } catch ( Throwable t ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to activate resource: " + t.getMessage(), null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return;
        }
        state = resourceManager.getState( id );
        WorkspaceBean ws = (WorkspaceBean) FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get( "workspace" );
        ws.setModified();
        if ( state.getLastException() != null ) {
            String msg = state.getLastException().getMessage();
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        }
    }

    public void deactivate() {
        try {
            resourceManager.deactivate( id );
        } catch ( Throwable t ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to deactivate resource: " + t.getMessage(),
                                                null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return;
        }
        state = resourceManager.getState( id );
        WorkspaceBean ws = (WorkspaceBean) FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get( "workspace" );
        ws.setModified();
        if ( state.getLastException() != null ) {
            String msg = state.getLastException().getMessage();
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        }
    }

    public String edit()
                            throws IOException {
        if ( !location.exists() ) {
            copyURLToFile( template, location );
        }
        this.content = readFileToString( location, "UTF-8" );
        StringBuilder sb = new StringBuilder( "/console/generic/xmleditor?faces-redirect=true" );
        sb.append( "&id=" ).append( id );
        sb.append( "&fileName=" ).append( location );
        sb.append( "&schemaUrl=" ).append( schemaURL.toString() );
        sb.append( "&resourceManagerClass=" ).append( getResourceManagerClass() );
        sb.append( "&nextView=" ).append( resourceOutcome );
        return sb.toString();
    }

    public void delete() {
        try {
            resourceManager.deleteResource( id );
        } catch ( Throwable t ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to deactivate resource: " + t.getMessage(),
                                                null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        }
    }

    public void showErrors() {
        ResourceState<?> state = resourceManager.getState( id );

        String msg = "Initialization of resource with id '" + id + "' failed";

        if ( state.getLastException() != null ) {
            msg += ": " + state.getLastException().getMessage();
        } else {
            msg += ".";
        }
        msg += " (The application server log may contain additional information.)";
        FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, null );
        FacesContext.getCurrentInstance().addMessage( null, fm );
    }

    public String save() {

        try {
            XMLAdapter adapter = new XMLAdapter( new StringReader( content ), DEFAULT_URL );
            File location = getLocation();
            OutputStream os = new FileOutputStream( location );
            adapter.getRootElement().serialize( os );
            os.close();
            content = null;
            if ( resourceManager != null ) {
                if ( state.getType() == StateType.deactivated ) {
                    resourceManager.activate( id );
                } else {
                    resourceManager.deactivate( id );
                    resourceManager.activate( id );
                }
            }
        } catch ( Throwable t ) {
            if ( resourceManager != null ) {
                state = resourceManager.getState( id );
            }
            String msg = "Error adapting changes: " + t.getMessage();
            if ( state.getLastException() != null ) {
                msg = state.getLastException().getMessage();
            }
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return resourceOutcome;
        }
        if ( resourceManager != null ) {
            state = resourceManager.getState( id );
        }
        if ( state != null && state.getLastException() != null ) {
            String msg = state.getLastException().getMessage();
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        }

        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        WorkspaceBean ws = (WorkspaceBean) ctx.getApplicationMap().get( "workspace" );
        ws.setModified();

        return resourceOutcome;
    }

    public int compareTo( Config o ) {
        return id.compareTo( o.id );
    }

    public File getLocation() {
        return location;
    }

    public void setLocation( File location ) {
        this.location = location;
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

    public String getContent() {
        return content;
    }

    public void setContent( String content ) {
        this.content = content;
    }

    public String getResourceManagerClass() {
        if ( resourceManager == null ) {
            return "";
        }
        return resourceManager.getClass().getCanonicalName();
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

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public void setResourceManager( ResourceManager resourceManager ) {
        this.resourceManager = resourceManager;
    }

    public void setState( ResourceState<?> state ) {
        this.state = state;
    }
}
