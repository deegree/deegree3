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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.io.FileUtils;
import org.deegree.workspace.ErrorHandler;
import org.deegree.workspace.PreparedResources;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.ResourceManagerMetadata;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.ResourceProvider;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.graph.ResourceGraph;
import org.deegree.workspace.graph.ResourceNode;
import org.slf4j.Logger;

/**
 * Directory based workspace implementation.
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

    private Map<Class<? extends ResourceProvider<? extends Resource>>, ResourceManager<? extends Resource>> resourceManagers;

    private Map<ResourceIdentifier<? extends Resource>, ResourceMetadata<? extends Resource>> resourceMetadata;

    private Map<ResourceIdentifier<? extends Resource>, Resource> resources;

    private Map<Class<? extends ResourceProvider<? extends Resource>>, List<ResourceLocation<? extends Resource>>> extraResources = new HashMap<Class<? extends ResourceProvider<? extends Resource>>, List<ResourceLocation<? extends Resource>>>();

    private ResourceGraph graph;

    private ErrorHandler errors = new ErrorHandler();

    public DefaultWorkspace( File directory ) {
        this.directory = directory;
    }

    @Override
    public void initAll() {
        startup();
        scan();
        PreparedResources prepared = prepare();

        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Building and initializing resources." );
        LOG.info( "--------------------------------------------------------------------------------" );

        // probably better to implement an insert bulk operation on the graph
        for ( ResourceMetadata<? extends Resource> md : prepared.getMetadata() ) {
            graph.insertNode( md );
        }

        for ( ResourceMetadata<? extends Resource> md : graph.toSortedList() ) {
            LOG.info( "Building resource {}.", md.getIdentifier() );
            try {
                Resource res = prepared.getBuilder( md.getIdentifier() ).build();
                if ( res == null ) {
                    errors.registerError( md.getIdentifier(), "Unable to prepare." );
                    LOG.error( "Unable to build resource {}.", md.getIdentifier() );
                    continue;
                }
                LOG.info( "Initializing resource {}.", md.getIdentifier() );
                res.init();
                resources.put( res.getMetadata().getIdentifier(), res );
            } catch ( Exception ex ) {
                ex.printStackTrace();
                String msg = "Unable to build resource " + md.getIdentifier() + ": " + ex.getLocalizedMessage();
                errors.registerError( md.getIdentifier(), msg );
                LOG.error( msg );
                LOG.trace( "Stack trace:", ex );
            }
        }
    }

    @Override
    public void destroy() {
        List<ResourceMetadata<? extends Resource>> list = graph.toSortedList();
        Collections.reverse( list );
        for ( ResourceMetadata<? extends Resource> md : list ) {
            Resource res = resources.get( md.getIdentifier() );
            try {
                LOG.info( "Shutting down {}.", md.getIdentifier() );
                res.destroy();
            } catch ( Exception e ) {
                LOG.warn( "Unable to destroy resource {}: {}", res.getMetadata().getIdentifier(),
                          e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            }
        }

        for ( ResourceManager<? extends Resource> mgr : resourceManagers.values() ) {
            mgr.shutdown();
        }

        moduleClassLoader = null;
        resourceMetadata = null;
        resources = null;
        resourceManagers = null;
        wsModules = null;
        extraResources.clear();
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
                        } catch ( Exception e ) {
                            LOG.warn( "Module {} could not be loaded: {}", fs[i].getName(), e.getLocalizedMessage() );
                            LOG.trace( "Stack trace:", e );
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

        if ( extraResources.get( metadata.getProviderClass() ) != null ) {
            list.addAll( (Collection) extraResources.get( metadata.getProviderClass() ) );
        }

        File dir = new File( directory, metadata.getWorkspacePath() );
        if ( !dir.isDirectory() ) {
            return list;
        }
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

    @Override
    public <T extends Resource> ResourceMetadata<T> getResourceMetadata( Class<? extends ResourceProvider<T>> providerClass,
                                                                         String id ) {
        return (ResourceMetadata<T>) resourceMetadata.get( new DefaultResourceIdentifier<T>( providerClass, id ) );
    }

    @Override
    public <T extends Resource> T getResource( Class<? extends ResourceProvider<T>> providerClass, String id ) {
        return (T) resources.get( new DefaultResourceIdentifier( providerClass, id ) );
    }

    /**
     * @return the directory this workspace is based on, never <code>null</code>
     */
    public File getLocation() {
        return directory;
    }

    @Override
    public <T extends ResourceManager<? extends Resource>> T getResourceManager( Class<T> managerClass ) {
        for ( ResourceManager<?> mgr : resourceManagers.values() ) {
            if ( mgr.getClass().equals( managerClass ) ) {
                return (T) mgr;
            }
        }
        return null;
    }

    @Override
    public void addExtraResource( ResourceLocation<? extends Resource> location ) {
        List<ResourceLocation<? extends Resource>> list = extraResources.get( location.getIdentifier().getProvider() );
        if ( list == null ) {
            list = new ArrayList<ResourceLocation<? extends Resource>>();
            extraResources.put( location.getIdentifier().getProvider(), list );
        }
        list.add( location );
    }

    @Override
    public void startup() {
        wsModules = new ArrayList<ModuleInfo>();
        resourceManagers = new HashMap<Class<? extends ResourceProvider<? extends Resource>>, ResourceManager<? extends Resource>>();
        resourceMetadata = new HashMap<ResourceIdentifier<? extends Resource>, ResourceMetadata<? extends Resource>>();
        resources = new HashMap<ResourceIdentifier<? extends Resource>, Resource>();
        graph = new ResourceGraph();
        initClassloader();

        // setup managers
        Iterator<ResourceManager> iter = ServiceLoader.load( ResourceManager.class, moduleClassLoader ).iterator();
        while ( iter.hasNext() ) {
            ResourceManager<?> mgr = iter.next();
            LOG.info( "Found resource manager {}.", mgr.getClass().getSimpleName() );
            resourceManagers.put( mgr.getMetadata().getProviderClass(), mgr );
            LOG.info( "Starting up resource manager {}.", mgr.getClass().getSimpleName() );
            // try/catch?
            mgr.startup( this );
        }
    }

    @Override
    public <T extends Resource> T init( ResourceIdentifier<T> id, PreparedResources prepared ) {
        if ( prepared == null ) {
            prepared = new PreparedResources( this );
        }
        LOG.info( "Collecting, building and initializing dependencies for {}.", id );
        List<ResourceMetadata<? extends Resource>> mdList = new ArrayList<ResourceMetadata<? extends Resource>>();
        ResourceMetadata<? extends Resource> md = resourceMetadata.get( id );
        mdList.add( md );
        for ( ResourceIdentifier<? extends Resource> did : md.getRelatedResources() ) {
            mdList.add( resourceMetadata.get( did ) );
        }
        ResourceGraph g = new ResourceGraph( mdList );
        mdList = g.toSortedList();

        for ( ResourceMetadata<? extends Resource> metadata : mdList ) {
            if ( resources.get( metadata.getIdentifier() ) != null ) {
                LOG.info( "Resource {} already available.", metadata.getIdentifier() );
                continue;
            }
            ResourceBuilder<? extends Resource> builder = prepared.getBuilder( metadata.getIdentifier() );
            LOG.info( "Building resource {}.", metadata.getIdentifier() );
            try {
                Resource res = builder.build();
                if ( res == null ) {
                    errors.registerError( metadata.getIdentifier(), "Unable to build resource." );
                    LOG.error( "Unable to build resource {}.", metadata.getIdentifier() );
                    throw new ResourceInitException( "Unable to build resource " + metadata.getIdentifier() + "." );
                }
                LOG.info( "Initializing resource {}.", metadata.getIdentifier() );
                res.init();
                resources.put( res.getMetadata().getIdentifier(), res );
            } catch ( Exception ex ) {
                String msg = "Unable to build resource " + metadata.getIdentifier() + ": " + ex.getLocalizedMessage();
                errors.registerError( metadata.getIdentifier(), msg );
                LOG.error( msg );
                LOG.trace( "Stack trace:", ex );
                throw new ResourceInitException( "Unable to build resource " + metadata.getIdentifier() + ": "
                                                 + ex.getLocalizedMessage(), ex );
            }
        }
        return getResource( id.getProvider(), id.getId() );
    }

    @Override
    public PreparedResources prepare() {
        scan();
        PreparedResources prepared = new PreparedResources( this );

        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Preparing resources." );
        LOG.info( "--------------------------------------------------------------------------------" );
        for ( ResourceMetadata<? extends Resource> md : resourceMetadata.values() ) {
            LOG.info( "Preparing resource {}.", md.getIdentifier() );
            try {
                ResourceBuilder<? extends Resource> builder = md.prepare();
                if ( builder == null ) {
                    LOG.error( "Could not prepare resource {}.", md.getIdentifier() );
                    continue;
                }
                prepared.addBuilder( (ResourceIdentifier) md.getIdentifier(), builder );
            } catch ( Exception e ) {
                String msg = "Error preparing resource " + md.getIdentifier() + ": " + e.getLocalizedMessage();
                errors.registerError( md.getIdentifier(), msg );
                LOG.error( msg );
                LOG.trace( "Stack trace:", e );
            }
        }

        return prepared;
    }

    private void scan() {
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Scanning resources." );
        LOG.info( "--------------------------------------------------------------------------------" );

        for ( ResourceManager<? extends Resource> mgr : resourceManagers.values() ) {
            mgr.find( this );
            Collection<? extends ResourceMetadata<? extends Resource>> mds = mgr.getResourceMetadata();
            for ( ResourceMetadata<? extends Resource> md : mds ) {
                resourceMetadata.put( md.getIdentifier(), md );
            }
        }
    }

    @Override
    public <T extends Resource> ResourceBuilder<T> prepare( ResourceIdentifier<T> id ) {
        LOG.info( "Preparing {}", id );
        ResourceMetadata<T> md = (ResourceMetadata) resourceMetadata.get( id );
        ResourceBuilder<T> builder = md.prepare();
        return builder;
    }

    @Override
    public <T extends Resource> List<ResourceIdentifier<T>> getResourcesOfType( Class<? extends ResourceProvider<T>> providerClass ) {
        List<ResourceIdentifier<T>> list = new ArrayList<ResourceIdentifier<T>>();
        for ( ResourceIdentifier<?> id : resources.keySet() ) {
            if ( id.getProvider().equals( providerClass ) ) {
                list.add( (ResourceIdentifier) id );
            }
        }
        return list;
    }

    @Override
    public ResourceGraph getDependencyGraph() {
        return graph;
    }

    @Override
    public <T extends Resource> void add( ResourceLocation<T> location ) {
        LOG.info( "Scanning {}", location.getIdentifier() );
        ResourceManager<T> mgr = (ResourceManager) resourceManagers.get( location.getIdentifier().getProvider() );
        ResourceMetadata<T> md = mgr.add( location, this );
        resourceMetadata.put( md.getIdentifier(), md );
    }

    @Override
    public <T extends Resource> void destroy( ResourceIdentifier<T> id ) {
        ResourceNode<T> node = graph.getNode( id );
        for ( ResourceNode<? extends Resource> n : node.getDependents() ) {
            destroy( n.getMetadata().getIdentifier() );
        }
        T res = (T) resources.get( node.getMetadata().getIdentifier() );
        LOG.info( "Shutting down {}.", node.getMetadata().getIdentifier() );
        res.destroy();
        resources.remove( node.getMetadata().getIdentifier() );
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errors;
    }

}
