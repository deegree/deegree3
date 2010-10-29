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
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.services.controller.OGCFrontController;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class XMLConfigManager<T extends ManagedXMLConfig> {

    public static final String SUFFIX = ".xml";

    public static final String SUFFIX_IGNORE = ".ignore";

    private boolean needsReloading = true;

    protected Map<String, T> idToConfig = Collections.synchronizedMap( new TreeMap<String, T>() );

    protected XMLConfigManager() {
        scan();
    }

    public void scan() {
        idToConfig.clear();
        File wsDir = OGCFrontController.getServiceWorkspace().getLocation();
        File baseDir = new File( wsDir, getBaseDir() );
        baseDir.list( new FilenameFilter() {
            @Override
            public boolean accept( File dir, String name ) {
                if ( name.endsWith( SUFFIX_IGNORE ) ) {
                    String id = name.substring( 0, name.length() - SUFFIX_IGNORE.length() );
                    String ns = getNamespace( dir, name );
                    add( id, ns, true );
                } else if ( name.endsWith( SUFFIX ) ) {
                    String id = name.substring( 0, name.length() - SUFFIX.length() );
                    String ns = getNamespace( dir, name );
                    add( id, ns, false );
                }
                return false;
            }
        } );
        needsReloading = false;
    }

    private String getNamespace( File dir, String name ) {

        String ns = null;
        try {
            File file = new File( dir, name );
            FileInputStream is = new FileInputStream( file );
            XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( is );
            StAXParsingHelper.skipStartDocument( xmlStream );
            ns = xmlStream.getNamespaceURI();
            xmlStream.close();
            is.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return ns;
    }

    protected abstract void add( String id, String namespace, boolean ignore );

    public List<T> getConfigs() {
        return new ArrayList<T>( idToConfig.values() );
    }

    public void add( T config ) {
        if ( idToConfig.containsKey( config.getId() ) ) {
            throw new RuntimeException( "Connection '" + config.getId() + "' already exists." );
        }
        idToConfig.put( config.getId(), config );
        needsReloading = true;
    }

    public void remove( T config ) {
        idToConfig.remove( config.getId() );
        needsReloading = true;
    }

    public void switchState( T config ) {
        needsReloading = true;
    }

    public abstract String getBaseDir();

    public boolean needsReloading() {
        return needsReloading;
    }
}
