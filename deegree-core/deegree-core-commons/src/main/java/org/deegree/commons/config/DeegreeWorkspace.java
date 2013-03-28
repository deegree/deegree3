//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-e by:
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
package org.deegree.commons.config;

import static java.io.File.separator;
import static java.util.Arrays.asList;
import static org.deegree.commons.utils.CollectionUtils.removeDuplicates;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.deegree.commons.modules.ModuleInfo;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.slf4j.Logger;

/**
 * Encapsulates a directory for deegree configuration files (a deegree workspace) and provides access to the configured
 * deegree resources.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DeegreeWorkspace {

    private static final Logger LOG = getLogger( DeegreeWorkspace.class );

    private static final String DEFAULT_WORKSPACE = "default";

    // environment variable for controlling the workspace directory
    private static final String VAR_WORKSPACE = "DEEGREE_WORKSPACE";

    // environment variable for controlling the root directory of the workspaces
    private static final String VAR_WORKSPACE_ROOT = "DEEGREE_WORKSPACE_ROOT";

    private static final Map<String, DeegreeWorkspace> nameToWs = new HashMap<String, DeegreeWorkspace>();

    private static final Map<File, DeegreeWorkspace> wsRootDirToWs = new HashMap<File, DeegreeWorkspace>();

    static {
        new DeegreeWorkspace( DEFAULT_WORKSPACE );

        // Getting Rid Of Derby.Log, TODO find a better place
        System.setProperty( "derby.stream.error.field", "org.deegree.commons.utils.io.Utils.DEV_NULL" );
    }

    private final String name;

    private final File dir;

    private List<ResourceManager> managers = new ArrayList<ResourceManager>();

    private Map<Class<? extends ResourceManager>, ResourceManager> managerMap;

    private ClassLoader moduleClassLoader;

    private Collection<ModuleInfo> wsModules = new TreeSet<ModuleInfo>();

    private DefaultWorkspace workspace;

    /**
     * @return a list of the currently loaded resource managers, never null
     */
    public List<ResourceManager> getResourceManagers() {
        return new ArrayList<ResourceManager>( managers );
    }

    /**
     * Returns the {@link ModuleInfo} for all deegree modules in the workspace.
     * 
     * @return
     * @throws IOException
     */
    public Collection<ModuleInfo> getModulesInfo()
                            throws IOException {
        if ( !( moduleClassLoader instanceof URLClassLoader ) ) {
            return null;
        }
        Set<URL> urls = new HashSet<URL>();
        for ( URL url : ( (URLClassLoader) moduleClassLoader ).getURLs() ) {
            urls.add( url );
        }
        return ModuleInfo.extractModulesInfo( urls );
    }

    /**
     * Call this if modules directory content has changed.
     */
    private void initClassloader() {
        // setup classloader
        File modules = new File( dir, "modules" );
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

    /**
     * Initializes the managers available on the classpath, but does not actually read the configurations.
     * 
     * @throws ResourceInitException
     */
    public void initManagers() {
        initClassloader();

        // setup managers
        Iterator<ResourceManager> iter = ServiceLoader.load( ResourceManager.class, getModuleClassLoader() ).iterator();

        Map<ResourceManager, List<Class<? extends ResourceManager>>> map = new HashMap<ResourceManager, List<Class<? extends ResourceManager>>>();
        managerMap = new HashMap<Class<? extends ResourceManager>, ResourceManager>();

        // first, collect all manager instances
        while ( iter.hasNext() ) {
            try {
                List<Class<? extends ResourceManager>> list = new LinkedList<Class<? extends ResourceManager>>();
                ResourceManager manager = iter.next();
                if ( manager instanceof ExtendedResourceManager ) {
                    // this makes sure resource provider dependencies are available below
                    ( (ExtendedResourceManager<?>) manager ).initMetadata( this );
                }
                map.put( manager, list );
                managerMap.put( manager.getClass(), manager );
            } catch ( ServiceConfigurationError e ) {
                LOG.warn( "A resource manager was not available. Error was {}", e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            }
        }

        // second, check for transitive dependencies
        for ( ResourceManager m : map.keySet() ) {
            List<Class<? extends ResourceManager>> list = map.get( m );
            searchDeps( list, m );
            removeDuplicates( list );
        }

        // third, check for transitive dependencies from resource providers, if applicable
        for ( ResourceManager m : map.keySet() ) {
            List<Class<? extends ResourceManager>> list = map.get( m );
            if ( m.getMetadata() == null ) {
                continue;
            }
            for ( Object o : m.getMetadata().getResourceProviders() ) {
                if ( o instanceof ExtendedResourceProvider<?> ) {
                    searchDeps( list, (ExtendedResourceProvider<?>) o );
                    removeDuplicates( list );
                }
            }
        }

        // fourth, order dependencies using fixed point method
        LinkedList<ResourceManager> order = new LinkedList<ResourceManager>( map.keySet() );
        boolean changed = true;
        outer: while ( changed ) {
            changed = false;
            for ( ResourceManager m : order ) {
                for ( Class<? extends ResourceManager> c : map.get( m ) ) {
                    if ( order.indexOf( managerMap.get( c ) ) > order.indexOf( m ) ) {
                        order.remove( managerMap.get( c ) );
                        order.add( order.indexOf( m ), managerMap.get( c ) );
                        changed = true;
                        continue outer;
                    }
                }
            }
        }

        managers.addAll( order );
    }

    private DeegreeWorkspace( String workspaceName, File dir ) throws IOException {
        this.dir = new File( dir.getCanonicalPath() );
        this.name = workspaceName + "(external)";
        wsRootDirToWs.put( this.dir, this );
        nameToWs.put( name, this );
        register();
        LOG.debug( "Created workspace '{}' at '{}'.", this.name, this.dir );
    }

    private DeegreeWorkspace( String workspaceName ) {
        String workspaceDir = System.getProperty( VAR_WORKSPACE );
        if ( workspaceDir == null || workspaceDir.isEmpty() ) {
            String workspaceRoot = getWorkspaceRoot();
            workspaceDir = separator + workspaceRoot + separator + workspaceName;
        }
        dir = new File( workspaceDir );
        name = workspaceName;
        register();
        LOG.debug( "Created workspace '{}' at '{}'.", this.name, this.dir );
    }

    private void register() {

        wsRootDirToWs.put( this.dir, this );
        nameToWs.put( name, this );
        this.workspace = new DefaultWorkspace( dir );
    }

    /**
     * @param name
     * @return true, if the directory $workspace_root/$name exists
     */
    public static boolean isWorkspace( String name ) {
        return new File( getWorkspaceRoot(), name ).isDirectory();
    }

    /**
     * Returns the default workspace.
     * 
     * @return the default workspace, never <code>null</code>
     */
    public static DeegreeWorkspace getInstance() {
        return nameToWs.get( DEFAULT_WORKSPACE );
    }

    /**
     * Returns the workspace with the given name.
     * 
     * @param workspaceName
     *            name of the workspace, can be <code>null</code> (implies default workspace)
     * @return the workspace instance (directory must not necessarily exist), never <code>null</code>
     */
    public static synchronized DeegreeWorkspace getInstance( String workspaceName ) {
        if ( workspaceName == null ) {
            workspaceName = DEFAULT_WORKSPACE;
        }
        DeegreeWorkspace ws = nameToWs.get( workspaceName );
        if ( ws != null ) {
            return ws;
        }
        return new DeegreeWorkspace( workspaceName );
    }

    /**
     * Returns the workspace with the given name (or the workspace for the given directory if the former does not
     * exist).
     * 
     * @param workspaceName
     *            name of the workspace, can be <code>null</code> (implies default workspace)
     * @param fallbackDir
     *            directory to use as workspace if the named workspace does not exist
     * @return the workspace instance (directory must not necessarily exist), never <code>null</code>
     * @throws IOException
     */
    public static synchronized DeegreeWorkspace getInstance( String workspaceName, File fallbackDir )
                            throws IOException {
        DeegreeWorkspace ws = getInstance( workspaceName );
        if ( ( !ws.getLocation().exists() || workspaceName == null ) && fallbackDir != null ) {
            ws = wsRootDirToWs.get( fallbackDir.getCanonicalFile() );
            if ( ws == null ) {
                if ( workspaceName == null ) {
                    workspaceName = DEFAULT_WORKSPACE;
                }
                ws = new DeegreeWorkspace( workspaceName, fallbackDir );
            }
        }
        return ws;
    }

    /**
     * Returns the name of the workspace.
     * 
     * @return the name of the workspace, never <code>null</code>
     */
    public String getName() {
        return name;
    }

    /**
     * @return the root directory of the workspace (must not necessarily exist)), never <code>null</code>
     */
    public File getLocation() {
        return dir;
    }

    /**
     * @param c
     * @return null, if no such manager was loaded
     */
    public <T extends ResourceManager> T getSubsystemManager( Class<T> c ) {
        return (T) managerMap.get( c );
    }

    private void searchDeps( List<Class<? extends ResourceManager>> list, ResourceManager m ) {
        list.addAll( asList( m.getDependencies() ) );
        for ( Class<? extends ResourceManager> c : m.getDependencies() ) {
            ResourceManager mgr = managerMap.get( c );
            if ( mgr == null ) {
                LOG.info( "No resource manager found for {}, skipping. This may lead to problems.", c.getSimpleName() );
            } else {
                searchDeps( list, mgr );
            }
        }
    }

    private void searchDeps( List<Class<? extends ResourceManager>> list, ExtendedResourceProvider<?> p ) {
        list.addAll( asList( p.getDependencies() ) );
        for ( Class<? extends ResourceManager> c : p.getDependencies() ) {
            ResourceManager mgr = managerMap.get( c );
            if ( mgr == null ) {
                LOG.warn( "No resource manager found for {}, skipping. This may lead to problems.", c.getSimpleName() );
            } else {
                searchDeps( list, mgr );
            }
        }
    }

    /**
     * Initializes all managed configurations.
     * 
     * @throws ResourceInitException
     */
    public synchronized void initAll()
                            throws ResourceInitException {
        workspace.init();
        ImageIO.scanForPlugins();
        initManagers();
        for ( ResourceManager m : managers ) {
            m.startup( this );
        }
    }

    /**
     * Unloads all resources associated with this context, as well as ALL STATIC ones.
     */
    public synchronized void destroyAll() {
        for ( ResourceManager m : managers ) {
            m.shutdown();
        }
        managers.clear();
        managerMap.clear();
        workspace.destroy();
    }

    public ClassLoader getModuleClassLoader() {
        return moduleClassLoader;
    }

    /**
     * @return the root directory for workspaces
     */
    public static String getWorkspaceRoot() {
        String workspaceRoot = System.getProperty( VAR_WORKSPACE_ROOT );
        if ( workspaceRoot == null || workspaceRoot.isEmpty() ) {
            workspaceRoot = System.getenv( VAR_WORKSPACE_ROOT );
        }
        if ( workspaceRoot == null || workspaceRoot.isEmpty() ) {
            workspaceRoot = System.getProperty( "user.home" ) + separator + ".deegree";
        }
        return workspaceRoot;
    }

    /**
     * @return a list of available workspaces
     */
    public static List<String> listWorkspaces() {
        File root = new File( getWorkspaceRoot() );
        List<String> workspaces = new ArrayList<String>();
        if ( root.isDirectory() ) {
            File[] list = root.listFiles();
            if ( list != null ) {
                for ( File f : list ) {
                    if ( !f.getName().equalsIgnoreCase( ".svn" ) && f.isDirectory() ) {
                        workspaces.add( f.getName() );
                    }
                }
            }
        }
        return workspaces;
    }

    public static void unregisterWorkspace( String name ) {
        DeegreeWorkspace ws = nameToWs.get( name );
        if ( ws != null ) {
            nameToWs.remove( name );
            try {
                wsRootDirToWs.remove( ws.getLocation().getCanonicalFile() );
            } catch ( IOException e ) {
                LOG.warn( "Could not properly unregister workspace {}.", name );
                LOG.trace( "Stack trace: ", e );
            }
        }
    }

    public Workspace getNewWorkspace() {
        return workspace;
    }

    /**
     * @param url
     * @param pattern
     *            some regex part like datasources.feature
     * @return the resource id
     */
    public String determineId( URL url, String pattern ) {
        Pattern p = Pattern.compile( ".*" + pattern + ".(.*)[.]xml" );
        Matcher m = p.matcher( url.toString() );
        m.find();
        return m.group( 1 );
    }

}
