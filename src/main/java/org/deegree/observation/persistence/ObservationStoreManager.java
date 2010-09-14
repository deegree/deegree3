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
package org.deegree.observation.persistence;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.feature.i18n.Messages;
import org.deegree.observation.persistence.contsql.jaxb.ContinuousObservationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class ObservationStoreManager {

    private static final Logger LOG = LoggerFactory.getLogger( ObservationStoreManager.class );

    private static ServiceLoader<ObservationStoreProvider> osProviderLoader = ServiceLoader.load( ObservationStoreProvider.class );

    private static Map<String, ObservationStoreProvider> osToProvider = null;

    private static Map<String, ObservationDatastore> idToOds = Collections.synchronizedMap( new HashMap<String, ObservationDatastore>() );

    /**
     * @param osDir
     */
    public static void init( File osDir ) {
        if ( !osDir.exists() ) {
            LOG.info( "No 'datasources/observation' directory -- skipping initialization of observation stores." );
            return;
        }
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Setting up observation stores." );
        LOG.info( "--------------------------------------------------------------------------------" );

        File[] osConfigFiles = osDir.listFiles( new FilenameFilter() {
            @Override
            public boolean accept( File dir, String name ) {
                return name.toLowerCase().endsWith( ".xml" );
            }
        } );
        for ( File osConfigFile : osConfigFiles ) {
            String fileName = osConfigFile.getName();
            // 4 is the length of ".xml"
            String odsId = fileName.substring( 0, fileName.length() - 4 );
            LOG.info( "Setting up observation store '" + odsId + "' from file '" + fileName + "'..." + "" );
            try {
                ObservationDatastore ods = create( osConfigFile.toURI().toURL() );
                register( odsId, ods );
            } catch ( Exception e ) {
                LOG.error( "Error creating feature store: " + e.getMessage(), e );
            }
        }
        LOG.info( "" );
    }

    /**
     * Returns the {@link ObservationDatastore} instance with the specified identifier.
     * 
     * @param id
     *            identifier of the observation store instance
     * @return the corresponding {@link ObservationDatastore} instance or null if no such instance has been created
     */
    public static ObservationDatastore get( String id ) {
        return idToOds.get( id );
    }

    /**
     * Returns all active {@link ObservationDatastore}s.
     * 
     * @return the {@link ObservationDatastore}s instance, may be empty but never <code>null</code>
     */
    public static Collection<ObservationDatastore> getAll() {
        return idToOds.values();
    }

    /**
     * Returns all available providers.
     */
    public static synchronized Map<String, ObservationStoreProvider> getProviders() {
        if ( osToProvider == null ) {
            osToProvider = new HashMap<String, ObservationStoreProvider>();
            try {
                for ( ObservationStoreProvider provider : osProviderLoader ) {
                    LOG.debug( "Observation store provider: " + provider + ", namespace: "
                               + provider.getConfigNamespace() );
                    if ( osToProvider.containsKey( provider.getConfigNamespace() ) ) {
                        LOG.error( "Multiple observation store providers for config namespace: '"
                                   + provider.getConfigNamespace() + "' on classpath -- omitting provider '"
                                   + provider.getClass().getName() + "'." );
                        continue;
                    }
                    osToProvider.put( provider.getConfigNamespace(), provider );
                }
            } catch ( Exception e ) {
                LOG.error( e.getMessage(), e );
            }

        }
        return osToProvider;
    }

    /**
     * @param odsId
     * @param ods
     * @throws ObservationDatastoreException
     */
    private static void register( String odsId, ObservationDatastore ods )
                            throws ObservationDatastoreException {
        if ( odsId != null ) {
            if ( idToOds.containsKey( odsId ) ) {
                String msg = Messages.getMessage( "STORE_MANAGER_DUPLICATE_ID", odsId );
                throw new ObservationDatastoreException( msg );
            }
            LOG.info( "Registering global observation store with id '" + odsId + "', type: '"
                      + ods.getClass().getName() + "'" );
            idToOds.put( odsId, ods );
        }
    }

    /**
     * @param url
     * @throws ObservationDatastoreException
     */
    private static synchronized ObservationDatastore create( URL url )
                            throws ObservationDatastoreException {
        String namespace = null;
        try {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( url.openStream() );
            StAXParsingHelper.nextElement( xmlReader );
            namespace = xmlReader.getNamespaceURI();
        } catch ( Exception e ) {
            String msg = "Error determining configuration namespace for file '" + url + "'";
            LOG.error( msg );
            throw new ObservationDatastoreException( msg );
        }
        LOG.debug( "Config namespace: '" + namespace + "'" );
        ObservationStoreProvider provider = getProviders().get( namespace );
        if ( provider == null ) {
            String msg = "No observation store provider for namespace '" + namespace + "' (file: '" + url
                         + "') registered. Skipping it.";
            LOG.error( msg );
            throw new ObservationDatastoreException( msg );
        }
        ObservationDatastore os = provider.getObservationStore( url );
        return os;
    }

    /**
     * @param contStore
     */
    private static DatastoreConfiguration getContStoreConfig( ContinuousObservationStore contStore ) {
        String jdbcId = contStore.getJDBCConnId();
        String tableName = contStore.getTable();

        DatastoreConfiguration dsConf = new DatastoreConfiguration( jdbcId, tableName );
        List<org.deegree.observation.persistence.contsql.jaxb.ColumnType> columns = contStore.getColumn();
        for ( org.deegree.observation.persistence.contsql.jaxb.ColumnType col : columns ) {
            dsConf.addToColumnMap( col.getType(), col.getName() );
        }

        List<org.deegree.observation.persistence.contsql.jaxb.OptionType> optionTypes = contStore.getOption();
        for ( org.deegree.observation.persistence.contsql.jaxb.OptionType opt : optionTypes ) {
            dsConf.addToGenOptionsMap( opt.getName(), opt.getValue() );
        }

        List<org.deegree.observation.persistence.contsql.jaxb.ContinuousObservationStore.Property> properties = contStore.getProperty();
        for ( org.deegree.observation.persistence.contsql.jaxb.ContinuousObservationStore.Property propType : properties ) {
            org.deegree.observation.model.Property prop = new org.deegree.observation.model.Property(
                                                                                                      propType.getHref(),
                                                                                                      propType.getColumn().getName() );
            for ( org.deegree.observation.persistence.contsql.jaxb.OptionType propOpType : propType.getOption() ) {
                prop.addToOption( propOpType.getName(), propOpType.getValue() );
            }
            dsConf.addToColumnMap( propType.getHref(), propType.getColumn().getName() );
            dsConf.addToProperties( prop );
        }
        return dsConf;
    }

    /**
     * @param datastoreId
     * @return a new store
     * @throws ObservationDatastoreException
     */
    public static ObservationDatastore getDatastoreById( String datastoreId )
                            throws ObservationDatastoreException {
        if ( idToOds.containsKey( datastoreId ) ) {
            return idToOds.get( datastoreId );
        }
        throw new ObservationDatastoreException( "The requested datastore id " + datastoreId
                                                 + " is not registered in the ObservationStoreManager" );
    }

    /**
     * @param datastoreId
     * @return true, if it does
     */
    public static boolean containsDatastore( String datastoreId ) {
        return idToOds.containsKey( datastoreId );
    }

}
