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
import static org.apache.commons.io.FileUtils.readFileToString;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.xml.stream.XMLStreamException;

import lombok.Getter;
import lombok.Setter;

import org.deegree.commons.config.Resource;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.config.ResourceState.StateType;
import org.deegree.commons.xml.XMLAdapter;

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

    @Getter
    private File location;

    @Getter
    private String id;

    @Getter
    private String schemaAsText;

    @Getter
    private URL template;

    @Getter
    @Setter
    private String content;

    private final ConfigManager manager;

    private final String resourceOutcome;

    @Getter
    private String capabilitiesURL;

    private ResourceManager resourceManager;

    private ResourceState state;

    public Config( ResourceState state, ConfigManager manager,
                   org.deegree.commons.config.ResourceManager originalResourceManager, String resourceOutcome ) {
        this.state = state;
        this.id = state.getId();
        this.location = state.getConfigLocation();
        this.resourceManager = originalResourceManager;
        this.manager = manager;
        this.resourceOutcome = resourceOutcome;
    }

    public URL getSchemaURL() {
        ResourceProvider provider = state.getProvider();
        if ( provider.getConfigSchema() != null ) {
            return provider.getConfigSchema();
        }
        return null;
    }

    public String getState() {
        ResourceState stateType = resourceManager.getState( id );
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
        if ( state.getLastException() != null ) {
            String msg = state.getLastException().getMessage();
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        }
    }

    public String edit()
                            throws IOException {
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
        String msg = "Initialization failed (see application server logs for more details).";
        ResourceState state = manager.getCurrentResourceManager().getManager().getState( id );
        if ( state.getLastException() != null ) {
            msg += "" + state.getLastException().getMessage();
        }
        FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, null );
        FacesContext.getCurrentInstance().addMessage( null, fm );
    }

    public String save()
                            throws XMLStreamException, IOException {

        try {
            XMLAdapter adapter = new XMLAdapter( new StringReader( content ), XMLAdapter.DEFAULT_URL );
            File location = getLocation();
            OutputStream os = new FileOutputStream( location );
            adapter.getRootElement().serialize( os );
            os.close();
            content = null;
            if ( state.getType() == StateType.deactivated ) {
                resourceManager.activate( id );
            } else {
                resourceManager.deactivate( id );
                resourceManager.activate( id );
            }
        } catch ( Throwable t ) {
            state = resourceManager.getState( id );
            String msg = "Error adapting changes.";
            if ( state.getLastException() != null ) {
                msg = state.getLastException().getMessage();
            }
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return resourceOutcome;
        }
        state = resourceManager.getState( id );
        if ( state.getLastException() != null ) {
            String msg = state.getLastException().getMessage();
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        }
        return resourceOutcome;
    }

    public int compareTo( Config o ) {
        return id.compareTo( o.id );
    }
}