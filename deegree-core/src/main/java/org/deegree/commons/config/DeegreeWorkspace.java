//$HeadURL$
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
package org.deegree.commons.config;

import static java.io.File.separator;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.coverage.persistence.CoverageBuilderManager;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.observation.persistence.ObservationStoreManager;
import org.deegree.record.persistence.RecordStoreManager;
import org.deegree.rendering.r3d.multiresolution.persistence.BatchedMTStoreManager;
import org.deegree.rendering.r3d.persistence.RenderableStoreManager;
import org.slf4j.Logger;

/**
 * Encapsulates a directory for deegree configuration files (a deegree workspace) and provides access to the configured
 * deegree resources.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:scneider@lat-lon.de">Markus Schneider</a>
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

    private CoverageBuilderManager coverageBuilderManager;

    private Actions currentAction = Actions.NotInited;

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
            String workspaceRoot = System.getProperty( VAR_WORKSPACE_ROOT );
            if ( workspaceRoot == null || workspaceRoot.isEmpty() ) {
                workspaceRoot = System.getProperty( "user.home" ) + separator + ".deegree";
            }
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
        if ( !ws.getLocation().exists() ) {
            ws = wsRootDirToWs.get( fallbackDir.getCanonicalPath() );
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
     * @return the coverage builder manager
     */
    public synchronized CoverageBuilderManager getCoverageBuilderManager() {
        if ( coverageBuilderManager == null ) {
            coverageBuilderManager = new CoverageBuilderManager( new File( dir, "datasources" + separator + "coverage" ) );
        }
        return coverageBuilderManager;
    }

    /**
     * Initializes all managed configurations.
     */
    public synchronized void initAll() {
        currentAction = Actions.Proxy;
        ProxyUtils.setupProxyParameters( new File( dir, "proxy.xml" ) );
        currentAction = Actions.ConnectionManager;
        ConnectionManager.init( new File( dir, "jdbc" ) );
        currentAction = Actions.ObservationManager;
        ObservationStoreManager.init( new File( dir, "datasources" + separator + "observation" ) );
        currentAction = Actions.FeatureManager;
        FeatureStoreManager.init( new File( dir, "datasources" + separator + "feature" ) );
        currentAction = Actions.CoverageManager;
        getCoverageBuilderManager().init();
        currentAction = Actions.RecordManager;
        RecordStoreManager.init( new File( dir, "datasources" + separator + "record" ) );
        currentAction = Actions.RenderableManager;
        RenderableStoreManager.init( new File( dir, "datasources" + separator + "renderable" ) );
        currentAction = Actions.BatchedMTManager;
        BatchedMTStoreManager.init( new File( dir, "datasources" + separator + "batchedmt" ) );
        currentAction = Actions.Inited;
    }

    /**
     * @return one of the actions defined in the enum, depending of the state of the workspace.
     */
    public Actions getCurrentAction() {
        return currentAction;
    }

    /**
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static enum Actions {
        /***/
        NotInited, /***/
        Proxy, /***/
        ConnectionManager, /***/
        ObservationManager, /***/
        FeatureManager, /***/
        CoverageManager, /***/
        RecordManager, /***/
        RenderableManager, /***/
        BatchedMTManager, /***/
        Inited
    }

    /**
     * Unloads all resources associated with this context, as well as ALL STATIC ones.
     */
    public synchronized void destroyAll() {
        getCoverageBuilderManager().destroy();
        FeatureStoreManager.destroy();
        ConnectionManager.destroy();
    }
}
