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
package org.deegree.record.persistence;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for creating {@link MetadataStore} instances from XML elements (JAXB objects) and for retrieving global
 * {@link MetadataStore} instances by id.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class MetadataStoreManager {

    private static final Logger LOG = LoggerFactory.getLogger( MetadataStoreManager.class );

    private static Map<String, MetadataStore> idToRs = Collections.synchronizedMap( new HashMap<String, MetadataStore>() );

    private MetadataStoreManager() {
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

        ISOMetadataStoreConfig config = null;
        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.metadata.persistence.iso19115.jaxb" );
            Unmarshaller u = jc.createUnmarshaller();
            config = (ISOMetadataStoreConfig) u.unmarshal( configURL );
        } catch ( JAXBException e ) {
            e.printStackTrace();
        }
        return (MetadataStore) new ISOMetadataStoreConfig();
    }

    private static void registerAndInit( MetadataStore rs, String id )
                            throws MetadataStoreException {

        rs.init();
        if ( id != null ) {
            if ( idToRs.containsKey( id ) ) {
                String msg = Messages.getMessage( "STORE_MANAGER_DUPLICATE_ID", id );
                throw new MetadataStoreException( msg );
            }
            LOG.info( "Registering global record store (" + rs + ") with id '" + id + "'." );
            idToRs.put( id, rs );

        }
    }

    /**
     * @param rsDir
     */
    public static void init( File rsDir ) {
        if ( !rsDir.exists() ) {
            LOG.info( "No 'datasources/record' directory -- skipping initialization of record stores." );
            return;
        }
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Setting up record stores." );
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
            LOG.info( "Setting up record store '" + rsId + "' from file '" + fileName + "'..." + "" );
            try {
                MetadataStore rs = create( rsConfigFile.toURI().toURL() );
                registerAndInit( rs, rsId );
            } catch ( Exception e ) {
                LOG.error( "Error initializing feature store: " + e.getMessage(), e );
            }
        }
        LOG.info( "" );
    }
}
