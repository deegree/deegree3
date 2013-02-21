//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.workspace.standard;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.commons.io.FileUtils;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.ResourceManagerMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class DefaultWorkspace implements Workspace {

    private static final Logger LOG = getLogger( DefaultWorkspace.class );

    private File directory;

    private ClassLoader moduleClassLoader;

    private List<ModuleInfo> wsModules;

    public DefaultWorkspace( File directory ) {
        this.directory = directory;
    }

    @Override
    public void init() {
        initClassloader();

        // setup managers
        Iterator<ResourceManager> iter = ServiceLoader.load( ResourceManager.class, moduleClassLoader ).iterator();
        while ( iter.hasNext() ) {
            iter.next().init( this );
        }
    }

    @Override
    public void destroy() {
        moduleClassLoader = null;
    }

    private void initClassloader() {
        // setup classloader
        File modules = new File( directory, "modules" );
        File classes = new File( modules, "classes/" );
        moduleClassLoader = Thread.currentThread().getContextClassLoader();
        if ( modules.exists() ) {
            File[] fs = modules.listFiles();
            if ( fs != null && fs.length > 0 ) {
                LOG.info( "--------------------------------------------------------------------------------" );
                LOG.info( "deegree modules (additional)" );
                LOG.info( "--------------------------------------------------------------------------------" );
                List<URL> urls = new ArrayList<URL>( fs.length );
                if ( classes.isDirectory() ) {
                    LOG.info( "Added modules/classes/." );
                    try {
                        urls.add( classes.toURI().toURL() );
                    } catch ( MalformedURLException e ) {
                        LOG.warn( "Could not add modules/classes/ to classpath." );
                    }
                }
                for ( int i = 0; i < fs.length; ++i ) {
                    if ( fs[i].isFile() ) {
                        try {
                            URL url = fs[i].toURI().toURL();
                            urls.add( url );
                            ModuleInfo moduleInfo = ModuleInfo.extractModuleInfo( url );
                            if ( moduleInfo != null ) {
                                LOG.info( " - " + moduleInfo );
                                wsModules.add( moduleInfo );
                            } else {
                                LOG.info( " - " + fs[i] + " (non-deegree)" );
                            }
                        } catch ( Throwable e ) {
                            LOG.warn( "Module {} could not be loaded: {}", fs[i].getName(), e.getLocalizedMessage() );
                        }
                    }
                }
                moduleClassLoader = new URLClassLoader( urls.toArray( new URL[urls.size()] ), moduleClassLoader );
            } else {
                LOG.info( "Not loading additional modules." );
            }
        } else {
            LOG.info( "Not loading additional modules." );
        }
    }

    @Override
    public ClassLoader getModuleClassLoader() {
        return moduleClassLoader;
    }

    @Override
    public <T extends Resource> List<ResourceLocation<T>> findResourceLocations( ResourceManagerMetadata<T> metadata ) {
        List<ResourceLocation<T>> list = new ArrayList<ResourceLocation<T>>();
        File dir = new File( directory, metadata.getWorkspacePath() );
        URI base = dir.getAbsoluteFile().toURI();
        for ( File f : FileUtils.listFiles( dir, new String[] { "xml" }, true ) ) {
            URI uri = f.getAbsoluteFile().toURI();
            uri = base.relativize( uri );
            String p = uri.getPath();
            p = p.substring( 0, p.length() - 4 );
            list.add( new DefaultResourceLocation<T>( f, new DefaultResourceIdentifier<T>( metadata.getProviderClass(),
                                                                                           p ) ) );
        }
        return list;
    }

}
