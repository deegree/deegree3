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

import static org.deegree.workspace.ResourceStates.ResourceState.Built;
import static org.deegree.workspace.ResourceStates.ResourceState.Deactivated;
import static org.deegree.workspace.ResourceStates.ResourceState.Error;
import static org.deegree.workspace.ResourceStates.ResourceState.Initialized;
import static org.deegree.workspace.ResourceStates.ResourceState.Prepared;
import static org.deegree.workspace.ResourceStates.ResourceState.Scanned;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.MalformedURLException;
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

import org.deegree.workspace.Destroyable;
import org.deegree.workspace.ErrorHandler;
import org.deegree.workspace.Initializable;
import org.deegree.workspace.LocationHandler;
import org.deegree.workspace.PreparedResources;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.ResourceProvider;
import org.deegree.workspace.ResourceStates;
import org.deegree.workspace.ResourceStates.ResourceState;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.WorkspaceUtils;
import org.deegree.workspace.graph.ResourceGraph;
import org.deegree.workspace.graph.ResourceNode;
import org.slf4j.Logger;

/**
 * Directory based workspace implementation.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public class DefaultWorkspace implements Workspace {

    private static final Logger LOG = getLogger( DefaultWorkspace.class );

    private final File directory;

    private ClassLoader moduleClassLoader;

    private List<ModuleInfo> wsModules;

    private Map<Class<? extends ResourceProvider<? extends Resource>>, ResourceManager<? extends Resource>> resourceManagers;

    private Map<ResourceIdentifier<? extends Resource>, ResourceMetadata<? extends Resource>> resourceMetadata;

    private Map<ResourceIdentifier<? extends Resource>, Resource> resources;

    private final Map<Class<? extends Initializable>, Initializable> initializables = new HashMap<Class<? extends Initializable>, Initializable>();

    private ResourceGraph graph;

    private final ErrorHandler errors = new ErrorHandler();

    private LocationHandler locationHandler;

    private ResourceStates states;

    private boolean startedUp = false;

    public DefaultWorkspace( File directory ) {
        this.directory = directory;
    }

    @Override
    public void initAll() {
        startup();
        errors.clear();
        scan();
        PreparedResources prepared = prepare();

        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Building and initializing resources." );
        LOG.info( "--------------------------------------------------------------------------------" );

        // probably better to implement an insert bulk operation on the graph
        for ( ResourceMetadata<? extends Resource> md : prepared.getMetadata() ) {
            graph.insertNode( md );
        }

        outer: for ( ResourceMetadata<? extends Resource> md : graph.toSortedList() ) {
            if ( states.getState( md.getIdentifier() ) == Deactivated ) {
                LOG.warn( "Not building resource {} (deactivated).", md.getIdentifier() );
                continue;
            }
            LOG.info( "Building resource {}.", md.getIdentifier() );
            for ( ResourceIdentifier<? extends Resource> dep : md.getDependencies() ) {
                if ( states.getState( dep ) != Initialized ) {
                    states.setState( md.getIdentifier(), Error );
                    String msg = "Dependent resource " + dep + " failed to initialize.";
                    LOG.error( "Unable to build resource {}: " + msg, md.getIdentifier() );
                    errors.registerError( md.getIdentifier(), msg );
                    continue outer;
                }
            }
            try {
                Resource res = prepared.getBuilder( md.getIdentifier() ).build();
                if ( res == null ) {
                    errors.registerError( md.getIdentifier(), "Unable to prepare." );
                    states.setState( md.getIdentifier(), Error );
                    LOG.error( "Unable to build resource {}.", md.getIdentifier() );
                    continue;
                }
                states.setState( md.getIdentifier(), Built );
                LOG.info( "Initializing resource {}.", md.getIdentifier() );
                res.init();
                states.setState( md.getIdentifier(), Initialized );
                resources.put( res.getMetadata().getIdentifier(), res );
            } catch ( Exception ex ) {
                states.setState( md.getIdentifier(), Error );
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
                if ( res != null ) {
                    LOG.info( "Shutting down {}.", md.getIdentifier() );
                    res.destroy();
                }
            } catch ( Exception e ) {
                LOG.warn( "Unable to destroy resource {}: {}", md.getIdentifier(), e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            }
        }

        for ( ResourceManager<? extends Resource> mgr : resourceManagers.values() ) {
            mgr.shutdown();
        }

        Iterator<Destroyable> it = ServiceLoader.load( Destroyable.class, moduleClassLoader ).iterator();
        while ( it.hasNext() ) {
            Destroyable init = it.next();
            try {
                init.destroy( this );
            } catch ( Exception e ) {
                LOG.error( "Could not destroy {}: {}", init.getClass().getSimpleName(), e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            }
        }

        moduleClassLoader = null;
        resourceMetadata = null;
        resources = null;
        resourceManagers = null;
        wsModules = null;
        initializables.clear();
        states = null;
        locationHandler = null;
        startedUp = false;
        errors.clear();
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
                            if ( url.getFile().endsWith( ".jar" ) ) {
                                urls.add( url );
                                ModuleInfo moduleInfo = ModuleInfo.extractModuleInfo( url );
                                if ( moduleInfo != null ) {
                                    LOG.info( " - " + moduleInfo );
                                    wsModules.add( moduleInfo );
                                } else {
                                    LOG.info( " - " + fs[i] + " (non-deegree)" );
                                }
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
    public List<ResourceManager<? extends Resource>> getResourceManagers() {
        return new ArrayList<ResourceManager<?>>( resourceManagers.values() );
    }

    @Override
    public void startup() {
        if ( startedUp ) {
            return;
        }
        wsModules = new ArrayList<ModuleInfo>();
        resourceManagers = new HashMap<Class<? extends ResourceProvider<? extends Resource>>, ResourceManager<? extends Resource>>();
        resourceMetadata = new HashMap<ResourceIdentifier<? extends Resource>, ResourceMetadata<? extends Resource>>();
        resources = new HashMap<ResourceIdentifier<? extends Resource>, Resource>();
        initializables.clear();
        graph = new ResourceGraph();
        states = new ResourceStates();
        locationHandler = new DefaultLocationHandler( directory, resourceManagers, states );
        errors.clear();
        initClassloader();

        Iterator<Initializable> it = ServiceLoader.load( Initializable.class, moduleClassLoader ).iterator();
        while ( it.hasNext() ) {
            Initializable init = it.next();
            try {
                init.init( this );
                initializables.put( init.getClass(), init );
            } catch ( Exception e ) {
                LOG.error( "Could not initialize {}: {}", init.getClass().getSimpleName(), e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            }
        }

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
        startedUp = true;
    }

    @Override
    public <T extends Resource> T init( ResourceIdentifier<T> id, PreparedResources prepared ) {
        if ( states.getState( id ) == Deactivated ) {
            return null;
        }
        if ( prepared == null ) {
            prepared = new PreparedResources( this );
        }
        LOG.info( "Collecting, building and initializing dependencies for {}.", id );
        List<ResourceMetadata<? extends Resource>> mdList = new ArrayList<ResourceMetadata<? extends Resource>>();
        ResourceMetadata<? extends Resource> md = resourceMetadata.get( id );
        mdList.add( md );
        graph.insertNode( md );
        List<ResourceMetadata<? extends Resource>> dependencies = new ArrayList<ResourceMetadata<?>>();
        WorkspaceUtils.collectDependencies( dependencies, graph.getNode( id ) );
        mdList.addAll( dependencies );

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
                    states.setState( metadata.getIdentifier(), Error );
                    errors.registerError( metadata.getIdentifier(), "Unable to build resource." );
                    LOG.error( "Unable to build resource {}.", metadata.getIdentifier() );
                    throw new ResourceInitException( "Unable to build resource " + metadata.getIdentifier() + "." );
                }
                states.setState( metadata.getIdentifier(), Built );
                LOG.info( "Initializing resource {}.", metadata.getIdentifier() );
                res.init();
                states.setState( metadata.getIdentifier(), Initialized );
                resources.put( res.getMetadata().getIdentifier(), res );
            } catch ( Exception ex ) {
                states.setState( metadata.getIdentifier(), Error );
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
        outer: for ( ResourceMetadata<? extends Resource> md : resourceMetadata.values() ) {
            ResourceState state = states.getState( md.getIdentifier() );
            if ( state == null ) {
                continue outer;
            }

            for ( ResourceIdentifier<? extends Resource> id : md.getDependencies() ) {
                state = states.getState( id );
                if ( state == null || state == Scanned ) {
                    continue outer;
                }
            }
            LOG.info( "Preparing resource {}.", md.getIdentifier() );
            try {
                ResourceBuilder<? extends Resource> builder = md.prepare();
                if ( builder == null ) {
                    LOG.error( "Could not prepare resource {}.", md.getIdentifier() );
                    if ( states.getState( md.getIdentifier() ) != Deactivated ) {
                        states.setState( md.getIdentifier(), Error );
                    }
                    continue;
                }
                graph.insertNode( md );
                if ( states.getState( md.getIdentifier() ) != Deactivated ) {
                    states.setState( md.getIdentifier(), Prepared );
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
            mgr.find();
            Collection<? extends ResourceMetadata<? extends Resource>> mds = mgr.getResourceMetadata();
            for ( ResourceMetadata<? extends Resource> md : mds ) {
                resourceMetadata.put( md.getIdentifier(), md );
                if ( states.getState( md.getIdentifier() ) != Deactivated ) {
                    states.setState( md.getIdentifier(), Scanned );
                }
            }
        }
    }

    @Override
    public <T extends Resource> ResourceBuilder<T> prepare( ResourceIdentifier<T> id ) {
        if ( states.getState( id ) == Deactivated ) {
            return null;
        }
        LOG.info( "Preparing {}", id );
        ResourceMetadata<T> md = (ResourceMetadata) resourceMetadata.get( id );
        ResourceBuilder<T> builder = md.prepare();
        if ( builder == null ) {
            states.setState( id, Error );
        } else {
            graph.insertNode( md );
            states.setState( id, Prepared );
        }
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
        ResourceMetadata<T> md = mgr.add( location );
        resourceMetadata.put( md.getIdentifier(), md );
    }

    @Override
    public <T extends Resource> void destroy( ResourceIdentifier<T> id ) {
        ResourceNode<T> node = graph.getNode( id );
        if ( node == null ) {
            return;
        }
        for ( ResourceNode<? extends Resource> n : node.getDependents() ) {
            destroy( n.getMetadata().getIdentifier() );
        }
        T res = (T) resources.get( id );
        if ( res != null ) {
            LOG.info( "Shutting down {}.", id );
            res.destroy();
        }
        states.remove( id );
        removeMetadataFromResourceManager( id );
        resources.remove( id );
        errors.clear( id );
    }

    @Override
    public <T extends Resource> void destroyAndShutdownDependents( ResourceIdentifier<T> id ) {
        ResourceNode<T> node = graph.getNode( id );
        if ( node == null ) {
            return;
        }
        for ( ResourceNode<? extends Resource> n : node.getDependents() ) {
            shutdownResourceAndDependents( n.getMetadata().getIdentifier() );
        }
        T res = (T) resources.get( id );
        if ( res != null ) {
            LOG.info( "Shutting down {}.", id );
            res.destroy();
        }
        states.remove( id );
        removeMetadataFromResourceManager( id );
        resources.remove( id );
        graph.removeNode( id );
        errors.clear( id );
    }

    private <T extends Resource> void shutdownResourceAndDependents( ResourceIdentifier<T> id ) {
        ResourceNode<T> node = graph.getNode( id );
        if ( node == null ) {
            return;
        }
        for ( ResourceNode<? extends Resource> n : node.getDependents() ) {
            shutdownResourceAndDependents( n.getMetadata().getIdentifier() );
        }
        T res = (T) resources.get( id );
        if ( res != null ) {
            LOG.info( "Shutting down {}.", id );
            res.destroy();
        }
        resources.remove( id );
        states.setState( id, ResourceState.Error );
        errors.clear( id );
        errors.registerError( id, "Required dependency unavailable." );
    }

    private void removeMetadataFromResourceManager( ResourceIdentifier<?> id ) {
        for ( ResourceManager<?> mgr : getResourceManagers() ) {
            for ( ResourceMetadata<?> md : mgr.getResourceMetadata() ) {
                if ( md.getIdentifier() == id ) {
                    mgr.remove (md);
                    return;
                }
            }
        }
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errors;
    }

    @Override
    public ResourceStates getStates() {
        return states;
    }

    @Override
    public <T extends Initializable> T getInitializable( Class<T> className ) {
        return (T) initializables.get( className );
    }

    @Override
    public LocationHandler getLocationHandler() {
        return locationHandler;
    }

}
