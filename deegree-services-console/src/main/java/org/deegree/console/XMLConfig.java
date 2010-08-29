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

import static org.deegree.console.XMLConfigManager.SUFFIX;
import static org.deegree.console.XMLConfigManager.SUFFIX_IGNORE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;

import javax.faces.context.FacesContext;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.services.controller.OGCFrontController;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
public class XMLConfig implements Serializable {

    private static final long serialVersionUID = 1161707801237264353L;

    private final XMLConfigManager manager;

    private final URL schema;

    private final URL template;

    private String id;

    private String content;

    private boolean active;

    private boolean modified;

    private boolean deactivated;

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active
     *            the active to set
     */
    public void setActive( boolean active ) {
        this.active = active;
    }

    /**
     * @return the modified
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * @param modified
     *            the modified to set
     */
    public void setModified( boolean modified ) {
        this.modified = modified;
    }

    /**
     * @return the deactivated
     */
    public boolean getDeactivated() {
        return deactivated;
    }

    /**
     * @param deactivated
     *            the deactivated to set
     */
    public void setDeactivated( boolean deactivated ) {
        if ( deactivated != this.deactivated ) {
            File file = getLocation();
            this.deactivated = deactivated;
            File newFile = getLocation();
            file.renameTo( newFile );
            System.out.println( file + " -> " + newFile );
        }
    }

    public void activate() {
        setDeactivated( false );
    }

    public void deactivate() {
        setDeactivated( true );
    }

    protected XMLConfig( String id, boolean active, boolean ignore, XMLConfigManager manager, URL schema, URL template ) {
        this.id = id;
        this.active = active;
        this.deactivated = ignore;
        this.manager = manager;
        this.schema = schema;
        this.template = template;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId( String id ) {
        this.id = id;
    }

    public File getLocation() {
        if ( id == null ) {
            throw new RuntimeException();
        }
        File wsDir = OGCFrontController.getServiceWorkspace().getLocation();
        File baseDir = new File( wsDir, manager.getBaseDir() );
        return new File( baseDir, id + ( deactivated ? SUFFIX_IGNORE : SUFFIX ) );
    }

    public String getContent() {
        XMLAdapter adapter = new XMLAdapter( getLocation() );
        return adapter.toString();
    }

    public void setContent( String content ) {
        System.out.println( "setContent()..." );
        this.content = content.trim();
    }

    public String getStatus() {
        if ( deactivated ) {
            return "DEACTIVATED";
        }
        if ( modified ) {
            return "MODIFIED";
        }
        if ( !active && !deactivated ) {
            return "ERROR (see logs)";
        }
        return "OK";
    }

    public void save()
                            throws XMLStreamException, IOException {
        XMLAdapter adapter = new XMLAdapter( new StringReader( content ), XMLAdapter.DEFAULT_URL );
        File location = getLocation();
        OutputStream os = new FileOutputStream( location );
        adapter.getRootElement().serialize( os );
        os.close();
        System.out.println( "Saved " + location );
        modified = true;
    }

    public void create()
                            throws IOException {

        File location = getLocation();
        OutputStream os = new FileOutputStream( location );

        InputStream is = template.openStream();
        byte[] buffer = new byte[1024];
        int read = -1;
        while ( ( read = is.read( buffer ) ) != -1 ) {
            os.write( buffer, 0, read );
        }
        os.close();
        System.out.println( "Wrote " + location );
        modified = true;
    }

    public void delete() {
        manager.remove( this );
        File location = getLocation();
        if ( location.exists() ) {
            location.delete();
        }
    }

    public String edit() {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put( "editConfig", this );
        return "console/generic/xmleditor.jsf";
    }

    @Override
    public String toString() {
        return "{id=" + id + ", location=" + getLocation() + ",schema=" + schema + ",template=" + template + "}";
    }
}
