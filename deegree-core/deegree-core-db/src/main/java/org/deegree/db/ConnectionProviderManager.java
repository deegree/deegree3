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
package org.deegree.db;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultResourceManager;
import org.deegree.workspace.standard.DefaultResourceManagerMetadata;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource manager for connection providers.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * 
 * @version $Revision: $, $Date: $
 */
public class ConnectionProviderManager extends DefaultResourceManager<ConnectionProvider> {

    private static final Logger LOG = LoggerFactory.getLogger( ConnectionProviderManager.class );

    private Workspace workspace;

    public ConnectionProviderManager() {
        super( new DefaultResourceManagerMetadata<ConnectionProvider>( ConnectionProviderProvider.class,
                                                                       "database connections", "jdbc" ) );
    }

    @Override
    public void startup( Workspace workspace ) {
        this.workspace = workspace;

        // Check for legacy JDBC drivers and warn if some are found in modules directory
        ClassLoader modulesClassloader = buildModulesOnlyClassloader();

        if ( modulesClassloader != null ) {
            for ( Driver d : ServiceLoader.load( Driver.class, modulesClassloader ) ) {
                final String clsName = d.getClass().getName();
                String clsFile;
                try {
                    clsFile = modulesClassloader.getResource( clsName.replace( '.', '/' ) + ".class" ).toString();
                } catch ( Exception ign ) {
                    clsFile = "";
                }
                int jarpos = clsFile.indexOf( ".jar" );
                if ( jarpos != -1 ) {
                    clsFile = clsFile.substring( 0, jarpos + 4 );
                }

                LOG.warn( "The JDBC driver {} has been found in the modules directory.", clsName );
                LOG.warn( "This method of loading JDBC drivers is not supported in deegree any more." );
                LOG.warn( "Please check the webservices handbook for more infomation." );
                if ( clsFile.length() > 0 ) {
                    LOG.warn( "The jdbc driver has been found at {}", clsFile );
                }
            }
        }
        super.startup( workspace );
    }

    @Override
    public void shutdown() {
        // nothing to do
    }

    /**
     * Create a ClassLoader which does not include the systems classloader
     * 
     * @see org.deegree.workspace.standard.DefaultWorkspace#initClassloader()
     * @return a classloader or null if no jars / classes are available in the workspace
     */
    private ClassLoader buildModulesOnlyClassloader() {
        if ( !( workspace instanceof DefaultWorkspace ) )
            return null;

        File directory = ( (DefaultWorkspace) workspace ).getLocation();
        if ( directory == null || !directory.isDirectory() )
            return null;

        File modules = new File( directory, "modules" );
        File classes = new File( modules, "classes" );
        if ( !modules.isDirectory() )
            return null;

        File[] fs = modules.listFiles();
        if ( fs != null && fs.length > 0 ) {
            List<URL> urls = new ArrayList<URL>( fs.length );
            if ( classes.isDirectory() ) {
                try {
                    urls.add( classes.toURI().toURL() );
                } catch ( MalformedURLException e ) {
                    LOG.warn( "Could not add modules/classes/ to classpath: {}", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            for ( int i = 0; i < fs.length; ++i ) {
                if ( fs[i].isFile() ) {
                    try {
                        URL url = fs[i].toURI().toURL();
                        if ( url.getFile().endsWith( ".jar" ) ) {
                            urls.add( url );
                        }
                    } catch ( Exception e ) {
                        LOG.warn( "Module {} could not be loaded: {}", fs[i].getName(), e.getLocalizedMessage() );
                        LOG.trace( "Stack trace:", e );
                    }
                }
            }
            return new URLClassLoader( urls.toArray( new URL[urls.size()] ) );
        }

        return null;
    }

}
