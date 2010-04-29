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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.commons.datasource.configuration.ISORecordStoreType;
import org.deegree.commons.datasource.configuration.RecordStoreType;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.record.persistence.genericrecordstore.ISORecordStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for creating {@link RecordStore} instances from XML elements (JAXB objects) and for retrieving global
 * {@link RecordStore} instances by id.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class RecordStoreManager {

    private static final Logger LOG = LoggerFactory.getLogger( RecordStoreManager.class );

    private static Map<String, RecordStore> idToRs = Collections.synchronizedMap( new HashMap<String, RecordStore>() );

    /**
     * Returns the global {@link RecordStore} instance with the specified identifier.
     * 
     * @param id
     *            identifier of the store
     * @return the corresponding {@link RecordStore} instance or null if no such instance has been created
     */
    public static RecordStore get( String id ) {
        return idToRs.get( id );

    }

    /**
     * Returns an initialized {@link FeatureStore} instance from the FeatureStore configuration document.
     * <p>
     * If the configuration specifies an identifier, the instance is also registered as global {@link FeatureStore}.
     * </p>
     * 
     * @param configURL
     *            URL of the configuration document, must not be <code>null</code>
     * @return corresponding {@link FeatureStore} instance, initialized and ready to be used
     * @throws RecordStoreException
     *             if the creation fails, e.g. due to a configuration error
     * 
     */
    @SuppressWarnings("unchecked")
    public static synchronized RecordStore create( URL configURL )
                            throws RecordStoreException {

        RecordStoreType config = null;
        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.commons.datasource.configuration" );
            Unmarshaller u = jc.createUnmarshaller();
            config = ( (JAXBElement<RecordStoreType>) u.unmarshal( configURL ) ).getValue();
        } catch ( JAXBException e ) {
            e.printStackTrace();
        }
        return create( config, configURL.toString() );
    }

    /**
     * Creates a {@link RecordStore} instance from the given configuration object.
     * <p>
     * If the configuration specifies an identifier, the instance is also registered as global {@link RecordStore}.
     * </p>
     * 
     * @param config
     *            configuration object
     * 
     * @return corresponding {@link RecordStore} instance
     * @throws RecordStoreException
     *             if the creation fails, e.g. due to a configuration error
     */
    public static synchronized RecordStore create( RecordStoreType jaxbConfig, String baseURL )
                            throws RecordStoreException {
        RecordStore rs = null;
        XMLAdapter resolver = new XMLAdapter();
        resolver.setSystemId( baseURL );

        String id = jaxbConfig.getDataSourceName();

        if ( jaxbConfig instanceof ISORecordStoreType ) {
            rs = new ISORecordStore( ( (ISORecordStoreType) jaxbConfig ).getConnId() );

        } else {
            String msg = Messages.getMessage( "STORE_MANAGER_UNHANDLED_CONFIGTYPE", jaxbConfig.getClass() );
            throw new RecordStoreException( msg );
        }

        registerAndInit( rs, id );
        return rs;
    }

    private static void registerAndInit( RecordStore rs, String id )
                            throws RecordStoreException {

        rs.init();
        if ( id != null ) {
            if ( idToRs.containsKey( id ) ) {
                String msg = Messages.getMessage( "STORE_MANAGER_DUPLICATE_ID", id );
                throw new RecordStoreException( msg );
            }
            LOG.info( "Registering global record store (" + rs + ") with id '" + id + "'." );
            idToRs.put( id, rs );

        }
    }

    /**
     * @param rsDir
     */
    public static void init( File rsDir ) {
        File[] rsConfigFiles = rsDir.listFiles( new FilenameFilter() {
            @Override
            public boolean accept( File dir, String name ) {
                // TODO Auto-generated method stub
                return name.toLowerCase().endsWith( ".xml" );
            }
        } );
        for ( File rsConfigFile : rsConfigFiles ) {
            String fileName = rsConfigFile.getName();
            // 4 is the length of ".xml"
            String rsId = fileName.substring( 0, fileName.length() - 4 );
            LOG.info( "Setting up record store '" + rsId + "' from file '" + fileName + "'..." + "" );
            try {
                RecordStore rs = create( rsConfigFile.toURI().toURL() );
                idToRs.put( rsId, rs );
            } catch ( Exception e ) {
                LOG.error( "Error initializing feature store: " + e.getMessage(), e );
            }
        }
    }
}
