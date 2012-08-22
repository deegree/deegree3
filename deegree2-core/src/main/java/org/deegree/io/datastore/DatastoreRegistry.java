//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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
package org.deegree.io.datastore;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.deegree.i18n.Messages;

/**
 * Responsible for the lookup of {@link Datastore} instances by their configuration information.
 * <p>
 * This is necessary to ensure that application schemas which use the same backend and identical
 * configuration information are served by the same {@link Datastore} instance.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class DatastoreRegistry {

    private static final String BUNDLE_NAME = "org.deegree.io.datastore.datastores";

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

    private static Map<DatastoreConfiguration, Datastore> dsMap = new HashMap<DatastoreConfiguration, Datastore>();

    /**
     * Returns the {@link Class} of a {@link Datastore} identified by the passed name. The mapping
     * between common names and Datastore classes is stored in the file
     * <code>org.deegree.io.datastore.datastores.datastore.properties</code>.
     *
     * @param commonName
     * @return Datastore class
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    public static Class<Datastore> getDatastoreClass( String commonName )
                            throws IllegalArgumentException {
        Class<Datastore> datastoreClass = null;
        String className = null;
        try {
            className = RESOURCE_BUNDLE.getString( commonName );
            datastoreClass = (Class<Datastore>) Class.forName( className );
        } catch ( MissingResourceException e ) {
            String msg = Messages.getMessage( "DATASTORE_REGISTRY_BACKEND_UNKNOWN", commonName, BUNDLE_NAME );
            throw new IllegalArgumentException( msg );
        } catch ( ClassNotFoundException e ) {
            throw new IllegalArgumentException( "Could not instantiate datastore class '" + className + ": "
                                                + e.getMessage() );
        }
        return datastoreClass;
    }

    /**
     * Returns the {@link Datastore} instance that serves the given {@link DatastoreConfiguration}.
     *
     * @param config
     * @return Datastore instance or null, if no Datastore is registered for this configuration
     */
    public static Datastore getDatastore( DatastoreConfiguration config ) {
        return dsMap.get( config );
    }

    /**
     * Registers a new {@link Datastore} instance.
     *
     * @param ds
     * @throws DatastoreException
     *             if Datastore for configuration is already registered
     */
    public static synchronized void registerDatastore( Datastore ds )
                            throws DatastoreException {
        if ( dsMap.get( ds.getConfiguration() ) != null ) {
            String msg = Messages.getMessage( "DATASTORE_REGISTRY_ALREADY_REGISTERED", ds.getConfiguration() );
            throw new DatastoreException( msg );
        }
        dsMap.put( ds.getConfiguration(), ds );
    }

    /**
     * Returns the {@link Datastore} instance that serves the given {@link DatastoreConfiguration}
     * from the registry.
     *
     * @param config
     * @throws DatastoreException
     *             if no Datastore for configuration is registered
     */
    public static synchronized void deregisterDatastore( DatastoreConfiguration config )
                            throws DatastoreException {
        if ( dsMap.get( config ) != null ) {
            String msg = Messages.getMessage( "DATASTORE_REGISTRY_NOT_REGISTERED", config );
            throw new DatastoreException( msg );
        }
    }
}
