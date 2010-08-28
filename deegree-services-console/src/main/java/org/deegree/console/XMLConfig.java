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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLAdapter;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
@ManagedBean
@RequestScoped
public abstract class XMLConfig implements Serializable {

    private static final long serialVersionUID = 1161707801237264353L;

    private final File baseDir;

    private final URL schema;

    private final URL template;

    private String id;

    private String content;

    protected XMLConfig( File baseDir, URL schema, URL template ) {
        this.baseDir = baseDir;
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
        return new File( baseDir, id + ".xml" );
    }

    public String getContent() {
        XMLAdapter adapter = new XMLAdapter( getLocation() );
        return adapter.toString();
    }

    public void setContent( String content ) {
        System.out.println( "setContent()..." );
        this.content = content.trim();
    }

    public void save()
                            throws XMLStreamException, IOException {
        XMLAdapter adapter = new XMLAdapter( new StringReader( content ), XMLAdapter.DEFAULT_URL );
        File location = getLocation();
        OutputStream os = new FileOutputStream( location );
        adapter.getRootElement().serialize( os );
        os.close();
        System.out.println( "Saved " + location );
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
    }

    public void delete() {
        File location = getLocation();
        if ( location.exists() ) {
            location.delete();
        }
    }

    public String edit() {
        EditorConfig.current = this;
        return "console/generic/xmleditor.jsf";
    }

    @Override
    public String toString() {
        return "{id=" + id + ", baseDir=" + baseDir + ",schema=" + schema + ",template=" + template + "}";
    }
}
