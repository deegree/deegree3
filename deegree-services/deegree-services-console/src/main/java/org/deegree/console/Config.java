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

import javax.faces.context.FacesContext;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.ResourceManagerMetadata;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.xml.XMLAdapter;

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
    @Setter
    private String content;

    private final ConfigManager manager;

    public Config( File location, ResourceManagerMetadata md, ConfigManager manager, String prefix )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        this.location = location;
        this.manager = manager;
        this.id = ( prefix == null ? "" : ( prefix + "/" ) )
                  + location.getName().substring( 0, location.getName().indexOf( "." ) );
        active = location.getName().endsWith( ".xml" );
        InputStream in = null, in2 = null;
        try {
            in = new FileInputStream( location );
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader( in );
            while ( !reader.isStartElement() ) {
                reader.next();
            }
            String namespace = reader.getNamespaceURI();
            for ( ResourceProvider p : md.getResourceProviders() ) {
                if ( p.getConfigNamespace().equals( namespace ) ) {
                    schemaURL = p.getConfigSchema();
                    template = p.getConfigTemplates().values().iterator().next();
                    schemaAsText = schemaURL == null ? null : IOUtils.toString( in2 = schemaURL.openStream() );
                    return;
                }
            }
            throw new IOException( "No fitting provider found." );
        } finally {
            closeQuietly( in );
            closeQuietly( in2 );
        }
    }

    public Config( File location, ResourceManagerMetadata md, ConfigManager manager, URL schemaURL, String type )
                            throws IOException {
        this.location = location;
        this.manager = manager;
        this.id = location.getName().substring( 0, location.getName().indexOf( "." ) );
        active = true;
        InputStream in = null;
        try {
            for ( ResourceProvider p : md.getResourceProviders() ) {
                if ( p.getConfigNamespace().endsWith( type ) ) {
                    schemaURL = p.getConfigSchema();
                    template = p.getConfigTemplates().values().iterator().next();
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
    public Config( File location, URL schemaURL, URL template, ConfigManager manager ) throws IOException {
        this.location = location;
        this.manager = manager;
        active = true;
        InputStream in = null;
        try {
            this.schemaURL = schemaURL;
            this.template = template;
            schemaAsText = schemaURL == null ? null : IOUtils.toString( in = schemaURL.openStream() );
        } finally {
            closeQuietly( in );
        }
    }

    public void activate() {
        if ( !active ) {
            File target = new File( location.getParentFile(), id + ".xml" );
            location.renameTo( target );
            active = true;
        }
    }

    public void deactivate() {
        if ( active ) {
            File target = new File( location.getParentFile(), id + ".ignore" );
            location.renameTo( target );
            active = false;
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

    public String save()
                            throws XMLStreamException, IOException {
        XMLAdapter adapter = new XMLAdapter( new StringReader( content ), XMLAdapter.DEFAULT_URL );
        File location = getLocation();
        OutputStream os = new FileOutputStream( location );
        adapter.getRootElement().serialize( os );
        os.close();
        content = null;
        manager.setModified();
        return "/console/jsf/resources";
    }

    public String cancel() {
        content = null;
        return "/console/jsf/resources";
    }

    public int compareTo( Config o ) {
        return id.compareTo( o.id );
    }

}
