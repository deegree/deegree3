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
package org.deegree.metadata.persistence;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for creating {@link MetadataStore} providers and instances.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MetadataStoreManager {

    private static final Logger LOG = LoggerFactory.getLogger( MetadataStoreManager.class );

    private static ServiceLoader<MetadataStoreProvider> providerLoader = ServiceLoader.load( MetadataStoreProvider.class );

    private static Map<String, MetadataStoreProvider> nsToProvider = null;

    private static Map<String, MetadataStore> idToRs = Collections.synchronizedMap( new HashMap<String, MetadataStore>() );

    private MetadataStoreManager() {
    }

    /**
     * Returns all available {@link MetadataStoreManager} providers.
     * 
     * @return all available providers, keys: config namespace, value: provider instance
     */
    public static synchronized Map<String, MetadataStoreProvider> getProviders() {
        if ( nsToProvider == null ) {
            nsToProvider = new HashMap<String, MetadataStoreProvider>();
            try {
                for ( MetadataStoreProvider provider : providerLoader ) {
                    LOG.debug( "Metadata store provider: " + provider + ", namespace: " + provider.getConfigNamespace() );
                    if ( nsToProvider.containsKey( provider.getConfigNamespace() ) ) {
                        LOG.error( "Multiple metadata store providers for config namespace: '"
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
     * Returns the global {@link MetadataStore} instance with the specified identifier.
     * 
     * @param id
     *            identifier of the store
     * @return the corresponding {@link MetadataStore} instance or null if no such instance has been created
     */
    public static MetadataStore get( String id ) {
        return idToRs.get( id );
    }

    /**
     * Returns all {@link MetadataStore} instances.
     * 
     * @return the corresponding {@link MetadataStore} instances, may be empty, but never <code>null</code>
     */
    public static Map<String, MetadataStore> getAll() {
        return idToRs;
    }

    /**
     * Returns an initialized {@link MetadataStore} instance from the RecordStore configuration document.
     * 
     * @param configURL
     *            URL of the configuration document, must not be <code>null</code>
     * @return corresponding {@link FeatureStore} instance, initialized and ready to be used
     * @throws MetadataStoreException
     *             if the creation fails, e.g. due to a configuration error
     * 
     */
    public static synchronized MetadataStore create( URL configURL )
                            throws MetadataStoreException {
        String namespace = null;
        try {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( configURL.openStream() );
            StAXParsingHelper.nextElement( xmlReader );
            namespace = xmlReader.getNamespaceURI();
        } catch ( Exception e ) {
            String msg = "Error determining configuration namespace for file '" + configURL + "'";
            LOG.error( msg );
            throw new MetadataStoreException( msg );
        }
        LOG.debug( "Config namespace: '" + namespace + "'" );
        MetadataStoreProvider provider = getProviders().get( namespace );
        if ( provider == null ) {
            String msg = "No metadata store provider for namespace '" + namespace + "' (file: '" + configURL
                         + "') registered. Skipping it.";
            LOG.error( msg );
            throw new MetadataStoreException( msg );
        }
        return provider.getMetadataStore( configURL );
    }

    private static void registerAndInit( MetadataStore rs, String id )
                            throws MetadataStoreException {

        rs.init();
        if ( id != null ) {
            if ( idToRs.containsKey( id ) ) {
                String msg = Messages.getMessage( "STORE_MANAGER_DUPLICATE_ID", id );
                throw new MetadataStoreException( msg );
            }
            LOG.info( "Registering global metadata store (" + rs + ") with id '" + id + "'." );
            idToRs.put( id, rs );

        }
    }

    /**
     * @param rsDir
     */
    public static void init( File rsDir ) {
        if ( !rsDir.exists() ) {
            LOG.info( "No 'datasources/metadata' directory -- skipping initialization of metadata stores." );
            return;
        }
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Setting up metadata stores." );
        LOG.info( "--------------------------------------------------------------------------------" );
        File[] rsConfigFiles = rsDir.listFiles( new FilenameFilter() {
            @Override
            public boolean accept( File dir, String name ) {
                return name.toLowerCase().endsWith( ".xml" );
            }
        } );
        for ( File rsConfigFile : rsConfigFiles ) {
            String fileName = rsConfigFile.getName();
            // 4 is the length of ".xml"
            String rsId = fileName.substring( 0, fileName.length() - 4 );
            LOG.info( "Setting up metadata store '" + rsId + "' from file '" + fileName + "'..." + "" );
            try {
                MetadataStore rs = create( rsConfigFile.toURI().toURL() );
                registerAndInit( rs, rsId );
            } catch ( Exception e ) {
                LOG.error( "Error initializing metadata store: " + e.getMessage(), e );
            }
        }
        LOG.info( "" );
    }
}
