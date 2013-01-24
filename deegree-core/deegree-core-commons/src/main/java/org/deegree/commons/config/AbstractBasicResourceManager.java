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

import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.deegree.commons.config.ResourceState.StateType.deactivated;
import static org.deegree.commons.config.ResourceState.StateType.init_error;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
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

    protected DeegreeWorkspace workspace;

    protected File dir;

    // keys: resource identifiers, values: resource states
    protected Map<String, ResourceState> idToState = Collections.synchronizedMap( new HashMap<String, ResourceState>() );

    protected abstract ResourceProvider getProvider( URL url );

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
                            throws IllegalArgumentException {

        LOG.debug( "Creating new resource with id " + id );
        ResourceState state = null;
        if ( idToState.containsKey( id ) ) {
            String msg = "Cannot create resource '" + id + "' (" + this.getClass().getSimpleName()
                         + "). Resource already exists.";
            throw new IllegalArgumentException( msg );
        }

        File file = new File( dir, id + ".ignore" );

        try {
            if ( !dir.exists() ) {
                if ( !dir.mkdirs() ) {
                    String msg = "Unable to create resource directory '" + dir + "'";
                    state = new ResourceState( id, file, null, init_error, null, new ResourceInitException( msg ) );
                }
            }
        } catch ( Throwable t ) {
            String msg = "Unable to access / create resource directory '" + dir + "': " + t.getMessage();
            state = new ResourceState( id, file, null, init_error, null, new ResourceInitException( msg, t ) );
        }

        OutputStream os = null;
        try {
            os = new FileOutputStream( file );
            IOUtils.copy( is, os );
            state = new ResourceState( id, file, getProvider( file.toURI().toURL() ), deactivated, null, null );
        } catch ( Throwable t ) {
            String msg = "Cannot create config file for resource '" + id + "' (" + this.getClass().getSimpleName()
                         + "): " + t.getMessage();
            state = new ResourceState( id, file, null, init_error, null, new ResourceInitException( msg, t ) );
        } finally {
            IOUtils.closeQuietly( is );
            IOUtils.closeQuietly( os );
        }

        idToState.put( id, state );
        return state;
    }

    @Override
    public ResourceState deleteResource( String id ) {
        ResourceState state = idToState.get( id );
        if ( state != null ) {
            state = deactivate( id );
            remove( id );

            if ( !deleteQuietly( state.getConfigLocation() ) ) {
                ResourceInitException e = new ResourceInitException( "Unable to delete file '"
                                                                     + state.getConfigLocation() + "'." );
                state = new ResourceState( id, state.getConfigLocation(), state.getProvider(), init_error,
                                           state.getResource(), e );
            }
        }
        return state;
    }
}