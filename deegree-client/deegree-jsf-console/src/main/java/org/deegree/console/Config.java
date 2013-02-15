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
import static org.apache.commons.io.FileUtils.copyURLToFile;
import static org.apache.commons.io.FileUtils.readFileToString;
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
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.Resource;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.config.ResourceState.StateType;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.console.webservices.WebServiceConfigManager;
import org.deegree.services.OWS;
import org.deegree.services.controller.WebServicesConfiguration;
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

    private static final URL METADATA_EXAMPLE_URL = WebServiceConfigManager.class.getResource( "/META-INF/schemas/services/metadata/3.2.0/example.xml" );

    private static final URL METADATA_SCHEMA_URL = WebServiceConfigManager.class.getResource( "/META-INF/schemas/services/metadata/3.2.0/metadata.xsd" );

    private File location;

    private String id;

    private String schemaAsText;

    private URL template;

    private String content;

    private ConfigManager manager;

    private String resourceOutcome;

    private URL schemaURL;

    private ResourceManager resourceManager;

    private ResourceState<?> state;

    private boolean requiresWSReload;

    private boolean autoActivate;

    public Config( File location, URL schemaURL, String resourceOutcome ) {
        this.location = location;
        this.schemaURL = schemaURL;
        this.resourceOutcome = resourceOutcome;
        this.requiresWSReload = true;
        if ( schemaURL != null ) {
            try {
                schemaAsText = IOUtils.toString( schemaURL.openStream(), "UTF-8" );
            } catch ( IOException e ) {
                LOG.warn( "Schema not available: {}", schemaURL );
                LOG.trace( "Stack trace:", e );
            }
        }
    }

    public Config( File location, URL schemaURL, URL template, String resourceOutcome ) {
        this.location = location;
        this.schemaURL = schemaURL;
        this.template = template;
        this.resourceOutcome = resourceOutcome;
        this.requiresWSReload = true;
        if ( schemaURL != null ) {
            try {
                schemaAsText = IOUtils.toString( schemaURL.openStream(), "UTF-8" );
            } catch ( IOException e ) {
                LOG.warn( "Schema not available: {}", schemaURL );
                LOG.trace( "Stack trace:", e );
            }
        }
    }

    public Config( ResourceState<?> state, ConfigManager manager,
                   org.deegree.commons.config.ResourceManager originalResourceManager, String resourceOutcome,
                   boolean autoActivate ) {
        this.state = state;
        this.id = state.getId();
        this.location = state.getConfigLocation();
        this.resourceManager = originalResourceManager;
        this.manager = manager;
        this.resourceOutcome = resourceOutcome;
        this.autoActivate = autoActivate;

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

    public String getCapabilitiesURL() {
        OWS ows = ( (WebServicesConfiguration) resourceManager ).get( id );
        String type = ows.getImplementationMetadata().getImplementedServiceName()[0];

        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        StringBuffer sb = req.getRequestURL();

        // HACK HACK HACK
        int index = sb.indexOf( "/console" );
        return sb.substring( 0, index ) + "/services/" + id + "?service=" + type + "&request=GetCapabilities";
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

    public String editMetadata()
                            throws IOException {
        File metadataLocation = new File( location.getParent(), id + "_metadata.xml" );
        Config metadataConfig = new Config( metadataLocation, METADATA_SCHEMA_URL, METADATA_EXAMPLE_URL,
                                            "/console/webservices/webservices" );
        return metadataConfig.edit();
    }

    public String edit()
                            throws IOException {
        if ( !location.exists() ) {
            copyURLToFile( template, location );
        }
        this.content = readFileToString( location, "UTF-8" );
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put( "editConfig", this );
        return "/console/generic/xmleditor?faces-redirect=true";
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
        ResourceState<?> state = manager.getCurrentResourceManager().getManager().getState( id );

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
            XMLAdapter adapter = new XMLAdapter( new StringReader( content ), XMLAdapter.DEFAULT_URL );
            File location = getLocation();
            OutputStream os = new FileOutputStream( location );
            adapter.getRootElement().serialize( os );
            os.close();
            content = null;
            if ( autoActivate && resourceManager != null ) {
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

        if ( requiresWSReload ) {
            ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
            WorkspaceBean ws = (WorkspaceBean) ctx.getApplicationMap().get( "workspace" );
            ws.setModified();
        }

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

    public ConfigManager getManager() {
        return manager;
    }

    public void setManager( ConfigManager manager ) {
        this.manager = manager;
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

    public boolean isRequiresWSReload() {
        return requiresWSReload;
    }

    public void setRequiresWSReload( boolean requiresWSReload ) {
        this.requiresWSReload = requiresWSReload;
    }

    public boolean isAutoActivate() {
        return autoActivate;
    }

    public void setAutoActivate( boolean autoActivate ) {
        this.autoActivate = autoActivate;
    }

    public void setState( ResourceState<?> state ) {
        this.state = state;
    }
}