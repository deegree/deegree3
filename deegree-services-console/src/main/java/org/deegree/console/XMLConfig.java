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

/**
 * Represents an XML configuration file.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
public class XMLConfig implements Serializable {

    private final File location;

    private final URL schema;

    private final URL template;

    private String content;

    private boolean active;

    private boolean modified;

    private boolean deactivated;

    protected XMLConfig( boolean active, boolean ignore, File location, URL schema, URL template ) {
        this.active = active;
        this.deactivated = ignore;
        this.location = location;
        this.schema = schema;
        this.template = template;
    }

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
            modified = true;
        }
    }

    public void activate() {
        setDeactivated( false );
    }

    public void deactivate() {
        setDeactivated( true );
    }

    public File getLocation() {
        return location;
    }

    public String getContent() {
        XMLAdapter adapter = new XMLAdapter( getLocation() );
        return adapter.toString();
    }

    public String getSchema() {
        String xml = null;
        if ( schema != null ) {
            XMLAdapter adapter = new XMLAdapter( schema );
            xml = adapter.toString();
        }
        return xml;
    }

    public void setContent( String content ) {
        this.content = content.trim();
    }

    public String getStatus() {
        if ( deactivated ) {
            return "INACTIVE";
        }
        if ( modified ) {
            return "MODIFIED";
        }
        if ( !active && !deactivated ) {
            return "ERROR (see logs)";
        }
        return "ACTIVE";
    }

    public String save()
                            throws XMLStreamException, IOException {
        XMLAdapter adapter = new XMLAdapter( new StringReader( content ), XMLAdapter.DEFAULT_URL );
        File location = getLocation();
        OutputStream os = new FileOutputStream( location );
        adapter.getRootElement().serialize( os );
        os.close();
        modified = true;
        return "/console";
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
        File location = getLocation();
        if ( location.exists() ) {
            location.delete();
        }
    }

    public String edit() {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put( "editConfig", this );
        return "console/generic/xmleditor";
    }

    @Override
    public String toString() {
        return "{location=" + getLocation() + ",schema=" + schema + ",template=" + template + "}";
    }
}
