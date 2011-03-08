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
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.io.IOUtils;
import org.deegree.client.util.FacesUtil;
import org.deegree.commons.config.Resource;
import org.deegree.commons.config.ResourceManagerMetadata;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.services.OWSProvider;

/**
 * 
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
    private URL schemaURL;

    @Getter
    private String schemaAsText;

    @Getter
    private URL template;

    @Getter
    private boolean active;

    @Getter
    private boolean activated;

    @Getter
    @Setter
    private String content;

    private final ConfigManager manager;

    private final String resourceOutcome;

    @Getter
    private String capabilitiesURL;

    public Config( File location, ResourceManagerMetadata<? extends Resource> md, ConfigManager manager, String prefix,
                   String resourceOutcome ) throws XMLStreamException, FactoryConfigurationError, IOException {
        this.location = location;
        this.manager = manager;
        this.resourceOutcome = resourceOutcome;
        this.id = ( prefix == null ? "" : ( prefix + "/" ) )
                  + location.getName().substring( 0, location.getName().indexOf( "." ) );
        active = location.getName().endsWith( ".xml" );
        activated = active;
        InputStream in = null, in2 = null;
        try {
            in = new FileInputStream( location );
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader( in );
            while ( !reader.isStartElement() ) {
                reader.next();
            }
            String namespace = reader.getNamespaceURI();
            for ( ResourceProvider p : md.getResourceProviders() ) {
                ResourceProviderMetadata ctm = ResourceProviderMetadata.getMetadata( p );
                if ( p.getConfigNamespace().equals( namespace ) ) {
                    schemaURL = p.getConfigSchema();
                    template = ctm.getExamples().values().iterator().next().getContentLocation();
                    schemaAsText = schemaURL == null ? null : IOUtils.toString( in2 = schemaURL.openStream() );
                    if ( p instanceof OWSProvider<?> ) {
                        capabilitiesURL = FacesUtil.getServerURL()
                                          + "services?request=GetCapabilities&service="
                                          + ( (OWSProvider<?>) p ).getImplementationMetadata().getImplementedServiceName();
                    }
                    return;
                }
            }
            throw new IOException( "No fitting provider found." );
        } finally {
            closeQuietly( in );
            closeQuietly( in2 );
        }
    }

    public Config( File location, ResourceManagerMetadata<? extends Resource> md, ConfigManager manager, URL schemaURL,
                   String type, String resourceOutcome ) throws IOException {
        this.location = location;
        this.manager = manager;
        this.resourceOutcome = resourceOutcome;
        this.id = location.getName().substring( 0, location.getName().indexOf( "." ) );
        active = true;
        activated = false;
        InputStream in = null;
        try {
            for ( ResourceProvider p : md.getResourceProviders() ) {
                if ( p.getConfigNamespace().endsWith( type ) ) {
                    schemaURL = p.getConfigSchema();
                    ResourceProviderMetadata ctm = ResourceProviderMetadata.getMetadata( p );
                    template = ctm.getExamples().values().iterator().next().getContentLocation();
                    schemaAsText = schemaURL == null ? null : IOUtils.toString( in = schemaURL.openStream() );
                    return;
                }
            }
            throw new IOException( "No fitting provider found." );
        } finally {
            closeQuietly( in );
        }
    }

    /**
     * for single file configs
     */
    public Config( File location, URL schemaURL, URL template, ConfigManager manager, String resourceOutcome )
                            throws IOException {
        this.location = location;
        this.manager = manager;
        this.resourceOutcome = resourceOutcome;
        active = true;
        activated = true;
        InputStream in = null;
        try {
            this.schemaURL = schemaURL;
            this.template = template;
            schemaAsText = schemaURL == null ? null : IOUtils.toString( in = schemaURL.openStream() );
        } finally {
            closeQuietly( in );
        }
    }

    public String getState() {
        ResourceState state = manager.getCurrentResourceManager().originalResourceManager.getState( id );
        if ( state != null ) {
            return state.getType().name();
        }
        // TODO remove this after implementing getState for all resource managers
        if ( isActivated() ) {
            return "init_error";
        }
        return "deactivated";
    }

    public void activate() {
        if ( !activated ) {
            File target = new File( location.getParentFile(), id + ".xml" );
            location.renameTo( target );
            activated = true;
            manager.setModified();
        }
    }

    public void deactivate() {
        if ( activated ) {
            File target = new File( location.getParentFile(), id + ".ignore" );
            location.renameTo( target );
            activated = false;
            manager.setModified();
        }
    }

    public String edit()
                            throws IOException {
        if ( !location.exists() && template != null ) {
            copyURLToFile( template, location );
        }
        this.content = readFileToString( location, "UTF-8" );
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put( "editConfig", this );
        return "/console/generic/xmleditor?faces-redirect=true";
    }

    public void delete() {
        if ( location.exists() ) {
            location.delete();
            manager.update();
        }
    }

    public void showErrors() {
        String msg = "Initialization failed (see application server logs for more details).";
        ResourceState state = manager.getCurrentResourceManager().originalResourceManager.getState( id );
        if ( state.getLastException() != null ) {
            msg += "" + state.getLastException().getMessage();
        }
        FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, null );
        FacesContext.getCurrentInstance().addMessage( null, fm );
    }

    public String save()
                            throws XMLStreamException, IOException {
        XMLAdapter adapter = new XMLAdapter( new StringReader( content ), XMLAdapter.DEFAULT_URL );
        File location = getLocation();
        OutputStream os = new FileOutputStream( location );
        adapter.getRootElement().serialize( os );
        os.close();
        content = null;
        manager.setModified();
        return resourceOutcome;
    }

    public int compareTo( Config o ) {
        return id.compareTo( o.id );
    }
}