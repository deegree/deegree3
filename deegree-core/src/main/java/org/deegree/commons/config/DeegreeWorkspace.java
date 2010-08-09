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
import java.util.HashMap;

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
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DeegreeWorkspace {

    private static final Logger LOG = getLogger( DeegreeWorkspace.class );

    private static final HashMap<String, DeegreeWorkspace> WORKSPACES = new HashMap<String, DeegreeWorkspace>();

    static {
        WORKSPACES.put( "default", new DeegreeWorkspace( "default" ) );
    }

    /**
     * @return the workspace named 'default'.
     */
    public static DeegreeWorkspace getDefaultInstance() {
        return WORKSPACES.get( "default" );
    }

    /**
     * @param workspaceName
     * @return the workspace instance, or null, if the target directory does not exist
     */
    public static synchronized DeegreeWorkspace getInstance( String workspaceName ) {
        DeegreeWorkspace space = WORKSPACES.get( workspaceName );
        if ( space != null ) {
            return space;
        }
        space = new DeegreeWorkspace( workspaceName );
        // for now, ignore workspaces that do not exist
        if ( !space.getLocation().exists() ) {
            return null;
        }
        WORKSPACES.put( workspaceName, space );
        return space;
    }

    /**
     * @param dir
     * @return the workspace instance specified in the DEEGREE_WORKSPACE environment variable, or the one found in the
     *         specified directory, if the workspace from DEEGREE_WORKSPACE does not exist.
     */
    public static synchronized DeegreeWorkspace getInstance( File dir ) {
        String ws = System.getenv( "DEEGREE_WORKSPACE" );
        if ( ws == null ) {
            return new DeegreeWorkspace( dir );
        }
        DeegreeWorkspace workspace;
        LOG.info( "DEEGREE_WORKSPACE = '{}'", ws );
        workspace = DeegreeWorkspace.getInstance( ws );
        if ( workspace == null ) {
            LOG.info( "'{}' does not exist, using default directory.", ws );
            workspace = new DeegreeWorkspace( dir );
        }
        return workspace;
    }

    private File dir;

    private CoverageBuilderManager coverageBuilderManager;

    private DeegreeWorkspace( String workspaceName ) {
        dir = new File( System.getProperty( "user.home" ) + separator + ".deegree" + separator + workspaceName );
    }

    /**
     * @param dir
     */
    public DeegreeWorkspace( File dir ) {
        this.dir = dir;
        LOG.info( "Using workspace at '{}'.", dir );
    }

    /**
     * @return the directory corresponding to this workspace
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
        ProxyUtils.setupProxyParameters( new File( dir, "proxy.xml" ) );
        ConnectionManager.init( new File( dir, "jdbc" ) );
        ObservationStoreManager.init( new File( dir, "datasources" + separator + "observation" ) );
        FeatureStoreManager.init( new File( dir, "datasources" + separator + "feature" ) );
        getCoverageBuilderManager().init();
        RecordStoreManager.init( new File( dir, "datasources" + separator + "record" ) );
        RenderableStoreManager.init( new File( dir, "datasources" + separator + "renderable" ) );
        BatchedMTStoreManager.init( new File( dir, "datasources" + separator + "batchedmt" ) );
    }

    /**
     * Unloads all resources associated with this context, as well as ALL STATIC ones.
     */
    public void destroyAll() {
        ConnectionManager.destroy();
        // the rest seems not to need this
        // TODO: set dir to null?
    }

}
