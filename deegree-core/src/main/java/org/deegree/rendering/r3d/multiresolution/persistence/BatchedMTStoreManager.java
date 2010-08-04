//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.rendering.r3d.multiresolution.persistence;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.utils.FileUtils;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class BatchedMTStoreManager {

    private static final Logger LOG = LoggerFactory.getLogger( BatchedMTStoreManager.class );

    private static ServiceLoader<BatchedMTStoreProvider> providerLoader = ServiceLoader.load( BatchedMTStoreProvider.class );

    private static Map<String, BatchedMTStoreProvider> nsToProvider = new ConcurrentHashMap<String, BatchedMTStoreProvider>();

    private static Map<String, BatchedMTStore> idToStore = Collections.synchronizedMap( new HashMap<String, BatchedMTStore>() );

    static {
        try {
            for ( BatchedMTStoreProvider builder : providerLoader ) {
                if ( builder != null ) {
                    LOG.debug( "Service loader found BatchedMTStoreProvider: " + builder + ", namespace: "
                               + builder.getConfigNamespace() );
                    if ( nsToProvider.containsKey( builder.getConfigNamespace() ) ) {
                        LOG.error( "Multiple BatchedMTStoreProviders for config namespace: '"
                                   + builder.getConfigNamespace() + "' on classpath -- omitting provider '"
                                   + builder.getClass().getName() + "'." );
                        continue;
                    }
                    nsToProvider.put( builder.getConfigNamespace(), builder );
                }
            }
        } catch ( Exception e ) {
            LOG.error( e.getMessage(), e );
        }
    }

    /**
     * Initializes the {@link BatchedMTStoreManager} by loading all {@link BatchedMTStore} configurations from the given
     * directory.
     * 
     * @param configLocation
     *            containing BatchedMT manager configurations
     */
    public static void init( File configLocation ) {
        if ( !configLocation.exists() ) {
            LOG.info( "No 'datasources/batchedmt' directory -- skipping initialization of batchedmt stores." );
            return;
        }
        File[] rsConfigFiles = null;
        if ( configLocation.isFile() ) {
            String ext = FileUtils.getFileExtension( configLocation );
            if ( "xml".equalsIgnoreCase( ext ) ) {
                rsConfigFiles = new File[] { configLocation };
            }
        } else {
            rsConfigFiles = configLocation.listFiles( new FilenameFilter() {
                @Override
                public boolean accept( File dir, String name ) {
                    return name.toLowerCase().endsWith( ".xml" );
                }
            } );
        }
        if ( rsConfigFiles == null ) {
            LOG.warn( "Did not find any BatchedMTStore configuration files in directory: "
                      + configLocation.getAbsolutePath() + " no global BatchedMT manager will be available." );
            return;
        }
        for ( File rsConfigFile : rsConfigFiles ) {
            if ( rsConfigFile != null ) {
                String storeId = FileUtils.getFilename( rsConfigFile );
                LOG.info( "Setting up BatchedMTStore '" + storeId + "' from file '" + rsConfigFile.getName() + "'..."
                          + "" );
                if ( idToStore.containsKey( storeId ) ) {
                    String msg = "Duplicate definition of BatchedMTStore with id '" + storeId + "'.";
                    LOG.warn( msg );
                } else {
                    try {
                        BatchedMTStore rs = create( rsConfigFile.toURI().toURL() );
                        if ( rs != null ) {
                            LOG.info( "Registering global BatchedMTStore with id '" + storeId + "', type: '"
                                      + rs.getClass().getCanonicalName() + "'" );
                            idToStore.put( storeId, rs );
                        } else {
                            LOG.info( "BatchedMTStore with id '" + storeId + "', could not be loaded (null)." );
                        }
                    } catch ( Exception e ) {
                        LOG.error( "Error initializing BatchedMTStore: " + e.getMessage(), e );
                    }
                }
            }
        }
    }

    /**
     * Returns the {@link BatchedMTStore} instance with the specified identifier.
     * 
     * @param id
     *            identifier of the BatchedMT store instance
     * @return the corresponding {@link BatchedMTStore} instance or null if no such instance has been created
     */
    public static BatchedMTStore get( String id ) {
        return idToStore.get( id );
    }

    /**
     * Returns all active {@link BatchedMTStore}s.
     * 
     * @return the {@link BatchedMTStore}s instance, may be empty but never <code>null</code>
     */
    public static Collection<BatchedMTStore> getAll() {
        return idToStore.values();
    }

    /**
     * Returns an uninitialized {@link BatchedMTStore} instance created from the given configuration document.
     * 
     * @param configURL
     *            URL of the configuration document, must not be <code>null</code>
     * @return corresponding {@link BatchedMTStore} instance, initialized and ready to be used
     * @throws IllegalArgumentException
     *             if the creation fails, e.g. due to a configuration error
     */
    private static synchronized BatchedMTStore create( URL configURL )
                            throws IllegalArgumentException {

        String namespace = null;
        try {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( configURL.openStream() );
            StAXParsingHelper.nextElement( xmlReader );
            namespace = xmlReader.getNamespaceURI();
        } catch ( Exception e ) {
            String msg = "Error determining configuration namespace for file '" + configURL + "'";
            LOG.error( msg );
            throw new IllegalArgumentException( msg );
        }
        LOG.debug( "Config namespace: '" + namespace + "'" );
        BatchedMTStoreProvider builder = nsToProvider.get( namespace );
        if ( builder == null ) {
            String msg = "No BatchedMTStore provider for namespace '" + namespace + "' (file: '" + configURL
                         + "') registered. Skipping it.";
            LOG.error( msg );
            throw new IllegalArgumentException( msg );
        }
        return builder.build( configURL );
    }
}
