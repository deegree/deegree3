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
import java.io.IOException;
import java.net.URL;

import javax.xml.stream.XMLStreamException;

import org.deegree.services.controller.OGCFrontController;

/**
 * {@link XMLConfig} that is managed by an {@link XMLConfigManager}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
public class ManagedXMLConfig extends XMLConfig {

    private static final long serialVersionUID = 1161707801237264353L;

    private String id;

    private final XMLConfigManager manager;

    protected ManagedXMLConfig( String id, boolean active, boolean ignore, XMLConfigManager manager, URL schema,
                                URL template ) {
        super( active, ignore, null, schema, template );
        this.id = id;
        this.manager = manager;
        reloadContent();
    }

    /**
     * @param deactivated
     *            the deactivated to set
     */
    public void setDeactivated( boolean deactivated ) {
        if ( deactivated != getDeactivated() ) {
            super.setDeactivated( deactivated );
            manager.switchState( this );
        }
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
        return new File( baseDir, id + ( getDeactivated() ? SUFFIX_IGNORE : SUFFIX ) );
    }

    public String save()
                            throws XMLStreamException, IOException {
        super.save();
        manager.switchState( this );
        return "/console?faces-redirect=true";
    }

    public void delete() {
        super.delete();
        manager.remove( this );
    }
}
