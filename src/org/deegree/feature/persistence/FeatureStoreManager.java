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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.datasource.configuration.FeatureStoreReferenceType;
import org.deegree.commons.datasource.configuration.FeatureStoreType;
import org.deegree.commons.datasource.configuration.MemoryFeatureStoreType;
import org.deegree.commons.datasource.configuration.ShapefileDataSourceType;
import org.deegree.commons.gml.GMLIdContext;
import org.deegree.commons.gml.GMLVersion;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.CRS;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.gml.GMLFeatureDecoder;
import org.deegree.feature.gml.schema.ApplicationSchemaXSDDecoder;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.gml.MemoryFeatureStore;
import org.deegree.feature.persistence.shape.ShapeFeatureStore;
import org.deegree.feature.types.ApplicationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for creating {@link FeatureStore} instances from XML elements (JAXB objects) and for retrieving global
 * {@link FeatureStore} instances by id.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureStoreManager {

    private static final Logger LOG = LoggerFactory.getLogger( FeatureStoreManager.class );

    private static Map<String, FeatureStore> idToFs = Collections.synchronizedMap( new HashMap<String, FeatureStore>() );

    /**
     * Returns the global {@link FeatureStore} instance with the specified identifier.
     * 
     * @param id
     *            identifier of the store
     * @return the corresponding {@link FeatureStore} instance or null if no such instance has been created
     */
    public static FeatureStore get( String id ) {
        return idToFs.get( id );
    }

    /**
     * Creates a {@link FeatureStore} instance from the given configuration object.
     * <p>
     * If the configuration specifies an identifier, the instance is also registered as global {@link FeatureStore}.
     * </p>
     * 
     * @param config
     *            configuration object
     * @param baseURL
     *            base url (used to resolve relative paths in the configuration)
     * @return corresponding {@link FeatureStore} instance
     * @throws FeatureStoreException
     *             if the creation fails, e.g. due to a configuration error
     */
    public static synchronized FeatureStore create( FeatureStoreType config, String baseURL )
                            throws FeatureStoreException {

        // special case FeatureStoreReferenceType (just a lookup of existing feature store types)
        if ( config instanceof FeatureStoreReferenceType ) {
            String storeId = ( (FeatureStoreReferenceType) config ).getRefId();
            FeatureStore fs = idToFs.get( storeId );
            if ( fs == null ) {
                String msg = Messages.getMessage( "STORE_MANAGER_NO_SUCH_ID", storeId );
                throw new FeatureStoreException( msg );
            }
        }

        // any other FeatureStoreType requires the creation of a new FeatureStore instance
        FeatureStore fs = null;
        String id = config.getDataSourceName();

        if ( config instanceof ShapefileDataSourceType ) {
            ShapefileDataSourceType shapeDsConfig = (ShapefileDataSourceType) config;
            CRS crs = new CRS( shapeDsConfig.getCoordinateSystem().trim() );
            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( baseURL );
            String shapeFileName = null;
            try {
                shapeFileName = resolver.resolve( shapeDsConfig.getFile().trim() ).getFile();
            } catch ( MalformedURLException e ) {
                String msg = Messages.getMessage( "STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage() );
                LOG.error( msg, e );
                throw new FeatureStoreException( msg, e );
            }
            fs = new ShapeFeatureStore( shapeFileName, crs, null );
        } else if ( config instanceof MemoryFeatureStoreType ) {
            MemoryFeatureStoreType memoryDsConfig = (MemoryFeatureStoreType) config;
            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( baseURL );

            ApplicationSchema schema = null;
            try {
                URL schemaURL = resolver.resolve( memoryDsConfig.getGMLSchemaFileURL().trim() );
                ApplicationSchemaXSDDecoder decoder = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31,
                                                                                       schemaURL.toString() );
                schema = decoder.extractFeatureTypeSchema();
            } catch ( Exception e ) {
                String msg = Messages.getMessage( "STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage() );
                LOG.error( msg, e );
                throw new FeatureStoreException( msg, e );
            }

            fs = new MemoryFeatureStore( schema );
            fs.init();

            String datasetFile = memoryDsConfig.getGMLFeatureCollectionFileURL();
            if ( datasetFile != null ) {
                try {
                    GMLIdContext idContext = new GMLIdContext();
                    GMLFeatureDecoder decoder = new GMLFeatureDecoder( schema, idContext );
                    URL docURL = resolver.resolve( datasetFile.trim() );
                    XMLStreamReaderWrapper xmlStream = new XMLStreamReaderWrapper( docURL );
                    xmlStream.nextTag();
                    LOG.info( "Populating feature store with features from file '" + docURL + "'..." );
                    FeatureCollection fc = (FeatureCollection) decoder.parseFeature( xmlStream, null );
                    idContext.resolveXLinks( schema );
                    
                    FeatureStoreTransaction ta = fs.acquireTransaction();
                    List<String> fids = ta.performInsert( fc, IDGenMode.GENERATE_NEW );
                    LOG.info( "Inserted " + fids.size() + " features." );
                    ta.commit();
                } catch ( Exception e ) {
                    String msg = Messages.getMessage( "STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage() );
                    LOG.error( msg, e );
                    throw new FeatureStoreException( msg, e );
                }
            }
        } else {
            String msg = Messages.getMessage( "STORE_MANAGER_UNHANDLED_CONFIGTYPE", config.getClass() );
            throw new FeatureStoreException( msg );
        }

        if ( id != null ) {
            if ( idToFs.containsKey( id ) ) {
                String msg = Messages.getMessage( "STORE_MANAGER_DUPLICATE_ID", id );
                throw new FeatureStoreException( msg );
            }
            LOG.info( "Registering global feature store (" + fs + ") with id '" + id + "'." );
        } else {
            fs.init();
            idToFs.put( id, fs );
        }
        return fs;
    }
}
