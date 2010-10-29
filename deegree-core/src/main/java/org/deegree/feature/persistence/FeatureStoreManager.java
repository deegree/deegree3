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
package org.deegree.feature.persistence;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.feature.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for creating and retrieving {@link FeatureStore} providers and instances.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureStoreManager {

    private static final Logger LOG = LoggerFactory.getLogger( FeatureStoreManager.class );

    private static ServiceLoader<FeatureStoreProvider> fsProviderLoader = ServiceLoader.load( FeatureStoreProvider.class );

    private static Map<String, FeatureStoreProvider> nsToProvider = null;

    private static Map<String, FeatureStore> idToFs = Collections.synchronizedMap( new HashMap<String, FeatureStore>() );

    public static Collection<String> getFeatureStoreIds () {
        return idToFs.keySet();
    }
    
    /**
     * Returns all available {@link FeatureStore} providers.
     * 
     * @return all available providers, keys: config namespace, value: provider instance
     */
    public static synchronized Map<String, FeatureStoreProvider> getProviders() {
        if ( nsToProvider == null ) {
            nsToProvider = new HashMap<String, FeatureStoreProvider>();
            try {
                for ( FeatureStoreProvider provider : fsProviderLoader ) {
                    LOG.debug( "Feature store provider: " + provider + ", namespace: " + provider.getConfigNamespace() );
                    if ( nsToProvider.containsKey( provider.getConfigNamespace() ) ) {
                        LOG.error( "Multiple feature store providers for config namespace: '"
                                   + provider.getConfigNamespace() + "' on classpath -- omitting provider '"
                                   + provider.getClass().getName() + "'." );
                        continue;
                    }
                    nsToProvider.put( provider.getConfigNamespace(), provider );
                }
            } catch ( Exception e ) {
                LOG.error( e.getMessage(), e );
            }

        }
        return nsToProvider;
    }

    /**
     * Initializes the {@link FeatureStoreManager} by loading all feature store configurations from the given directory.
     * 
     * @param fsDir
     */
    public static void init( File fsDir ) {
        if ( !fsDir.exists() ) {
            LOG.info( "No 'datasources/feature' directory -- skipping initialization of feature stores." );
            return;
        }
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Setting up feature stores." );
        LOG.info( "--------------------------------------------------------------------------------" );

        File[] fsConfigFiles = fsDir.listFiles( new FilenameFilter() {
            @Override
            public boolean accept( File dir, String name ) {
                return name.toLowerCase().endsWith( ".xml" );
            }
        } );
        for ( File fsConfigFile : fsConfigFiles ) {
            String fileName = fsConfigFile.getName();
            // 4 is the length of ".xml"
            String fsId = fileName.substring( 0, fileName.length() - 4 );
            LOG.info( "Setting up feature store '" + fsId + "' from file '" + fileName + "'..." + "" );
            try {
                FeatureStore fs = create( fsConfigFile.toURI().toURL() );
                registerAndInit( fs, fsId );
            } catch ( Exception e ) {
                LOG.error( "Error creating feature store: " + e.getMessage(), e );
            }
        }
        LOG.info( "" );
    }

    /**
     * Returns the {@link FeatureStore} instance with the specified identifier.
     * 
     * @param id
     *            identifier of the feature store instance
     * @return the corresponding {@link FeatureStore} instance or null if no such instance has been created
     */
    public static FeatureStore get( String id ) {
        return idToFs.get( id );
    }

    /**
     * Returns all active {@link FeatureStore}s.
     * 
     * @return the {@link FeatureStore}s instance, may be empty but never <code>null</code>
     */
    public static Collection<FeatureStore> getAll() {
        return idToFs.values();
    }

    /**
     * Returns an uninitialized {@link FeatureStore} instance that's created from the specified FeatureStore
     * configuration document.
     * 
     * @param configURL
     *            URL of the configuration document, must not be <code>null</code>
     * @return corresponding {@link FeatureStore} instance, not yet initialized, never <code>null</code>
     * @throws FeatureStoreException
     *             if the creation fails, e.g. due to a configuration error
     */
    public static synchronized FeatureStore create( URL configURL )
                            throws FeatureStoreException {

        String namespace = null;
        try {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( configURL.openStream() );
            StAXParsingHelper.nextElement( xmlReader );
            namespace = xmlReader.getNamespaceURI();
        } catch ( Exception e ) {
            String msg = "Error determining configuration namespace for file '" + configURL + "'";
            LOG.error( msg );
            throw new FeatureStoreException( msg );
        }
        LOG.debug( "Config namespace: '" + namespace + "'" );
        FeatureStoreProvider provider = getProviders().get( namespace );
        if ( provider == null ) {
            String msg = "No feature store provider for namespace '" + namespace + "' (file: '" + configURL
                         + "') registered. Skipping it.";
            LOG.error( msg );
            throw new FeatureStoreException( msg );
        }
        FeatureStore fs = provider.getFeatureStore( configURL );
        return fs;
    }

    /**
     * 
     * @param fs
     * @param id
     * @throws FeatureStoreException
     */
    public static void registerAndInit( FeatureStore fs, String id )
                            throws FeatureStoreException {
        if ( id != null ) {
            if ( idToFs.containsKey( id ) ) {
                String msg = Messages.getMessage( "STORE_MANAGER_DUPLICATE_ID", id );
                throw new FeatureStoreException( msg );
            }
            LOG.info( "Registering global feature store with id '" + id + "', type: '" + fs.getClass().getName() + "'" );
            idToFs.put( id, fs );            
            fs.init();
        }
    }
    
    /**
     * 
     */
    public static void destroy () {
        for ( FeatureStore fs : idToFs.values() ) {
            fs.destroy();
        }
        idToFs.clear();
    }
}
