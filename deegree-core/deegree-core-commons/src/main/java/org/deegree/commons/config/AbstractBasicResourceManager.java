//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

import static org.deegree.commons.config.ResourceState.StateType.deactivated;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides some basic functionality for implementing {@link ResourceManager}s.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractBasicResourceManager implements ResourceManager {

    private static Logger LOG = LoggerFactory.getLogger( AbstractBasicResourceManager.class );

    protected Map<String, ResourceState> idToState = new HashMap<String, ResourceState>();

    protected DeegreeWorkspace workspace;

    protected File dir;

    // TODO this should happen in the constructor to ensure that it is always invoked!!!
    protected void init( DeegreeWorkspace workspace, File resourceDir ) {
        this.workspace = workspace;
        this.dir = resourceDir;
    }

    protected abstract ResourceProvider getProvider( File file );

    protected abstract void remove( String id );

    @Override
    public ResourceState[] getStates() {
        return idToState.values().toArray( new ResourceState[idToState.size()] );
    }

    @Override
    public ResourceState getState( String id ) {
        return idToState.get( id );
    }

    @Override
    public ResourceState createResource( String id, InputStream is )
                            throws ResourceInitException {

        LOG.info( "Creating new resource with id " + id );
        if ( idToState.containsKey( id ) ) {
            String msg = "Cannot create resource '" + id + "' (" + this.getClass().getSimpleName()
                         + "). Resource already exists.";
            throw new ResourceInitException( msg );
        }
        if ( !dir.exists() ) {
            try {
                if ( !dir.mkdirs() ) {
                    String msg = "Unable to create resource directory '" + dir + "'";
                    throw new ResourceInitException( msg );
                }
            } catch ( Throwable t ) {
                String msg = "Unable to create resource directory '" + dir + "': " + t.getMessage();
                throw new ResourceInitException( msg );
            }
        }
        File file = new File( dir, id + ".ignore" );
        OutputStream os = null;
        try {
            os = new FileOutputStream( file );
            IOUtils.copy( is, os );
        } catch ( IOException e ) {
            String msg = "Cannot create config file for resource '" + id + "' (" + this.getClass().getSimpleName()
                         + "): " + e.getMessage();
            throw new ResourceInitException( msg );
        } finally {
            IOUtils.closeQuietly( is );
            IOUtils.closeQuietly( os );
        }

        ResourceState state = new ResourceState( id, file, getProvider( file ), deactivated, null );
        idToState.put( id, state );
        return state;
    }

    @Override
    public void deleteResource( String id ) {
        ResourceState state = idToState.get( id );
        if ( state != null ) {
            remove( id );
            try {
                deactivate( id );
            } catch ( ResourceInitException e ) {
                // TODO
                e.printStackTrace();
            }
            state.getConfigLocation().delete();
        }
    }
}