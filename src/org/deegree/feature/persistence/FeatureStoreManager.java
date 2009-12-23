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

import static org.deegree.commons.utils.CollectionUtils.map;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.commons.configuration.GMLVersionType;
import org.deegree.commons.datasource.configuration.DirectSQLDataSourceType;
import org.deegree.commons.datasource.configuration.FeatureStoreReferenceType;
import org.deegree.commons.datasource.configuration.FeatureStoreType;
import org.deegree.commons.datasource.configuration.MemoryFeatureStoreType;
import org.deegree.commons.datasource.configuration.PostGISFeatureStoreType;
import org.deegree.commons.datasource.configuration.ShapefileDataSourceType;
import org.deegree.commons.datasource.configuration.DirectSQLDataSourceType.LODStatement;
import org.deegree.commons.datasource.configuration.FeatureStoreType.NamespaceHint;
import org.deegree.commons.datasource.configuration.MemoryFeatureStoreType.GMLFeatureCollectionFileURL;
import org.deegree.commons.utils.CollectionUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.crs.CRS;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStoreTransaction.IDGenMode;
import org.deegree.feature.persistence.memory.MemoryFeatureStore;
import org.deegree.feature.persistence.postgis.PostGISFeatureStore;
import org.deegree.feature.persistence.shape.ShapeFeatureStore;
import org.deegree.feature.persistence.simplesql.SimpleSQLDatastore;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for creating {@link FeatureStore} instances from XML configuration documents or XML elements (JAXB
 * objects) and for retrieving global {@link FeatureStore} instances by id.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureStoreManager {

    private static final Logger LOG = LoggerFactory.getLogger( FeatureStoreManager.class );

    private static Map<String, FeatureStore> idToFs = Collections.synchronizedMap( new HashMap<String, FeatureStore>() );

    private static Mapper<Pair<Integer, String>, LODStatement> lodMapper = new Mapper<Pair<Integer, String>, LODStatement>() {
        public Pair<Integer, String> apply( LODStatement u ) {
            return new Pair<Integer, String>( u.getAboveScale(), u.getValue() );
        }
    };

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
     * Returns an initialized {@link FeatureStore} instance from the FeatureStore configuration document.
     * <p>
     * If the configuration specifies an identifier, the instance is also registered as global {@link FeatureStore}.
     * </p>
     * 
     * @param configURL
     *            URL of the configuration document, must not be <code>null</code>
     * @return corresponding {@link FeatureStore} instance, initialized and ready to be used
     * @throws FeatureStoreException
     *             if the creation fails, e.g. due to a configuration error
     */
    @SuppressWarnings("unchecked")
    public static synchronized FeatureStore create( URL configURL )
                            throws FeatureStoreException {

        FeatureStoreType config = null;
        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.commons.datasource.configuration" );
            Unmarshaller u = jc.createUnmarshaller();
            config = ( (JAXBElement<FeatureStoreType>) u.unmarshal( configURL ) ).getValue();
        } catch ( JAXBException e ) {
            e.printStackTrace();
        }
        return create( config, configURL.toString() );
    }

    /**
     * Returns an initialized {@link FeatureStore} instance from the given JAXB {@link FeatureStoreType} configuration
     * object.
     * <p>
     * If the configuration specifies an identifier, the instance is also registered as global {@link FeatureStore}.
     * </p>
     * 
     * @param jaxbConfig
     *            configuration object, must not be <code>null</code>
     * @param baseURL
     *            base url (used to resolve relative paths in the configuration)
     * @return corresponding {@link FeatureStore} instance, initialized and ready to be used
     * @throws FeatureStoreException
     *             if the creation fails, e.g. due to a configuration error
     */
    public static synchronized FeatureStore create( FeatureStoreType jaxbConfig, String baseURL )
                            throws FeatureStoreException {

        FeatureStore fs = null;
        if ( jaxbConfig instanceof FeatureStoreReferenceType ) {
            fs = create( (FeatureStoreReferenceType) jaxbConfig );
        } else if ( jaxbConfig instanceof ShapefileDataSourceType ) {
            fs = create( (ShapefileDataSourceType) jaxbConfig, baseURL );
        } else if ( jaxbConfig instanceof MemoryFeatureStoreType ) {
            fs = create( (MemoryFeatureStoreType) jaxbConfig, baseURL );
        } else if ( jaxbConfig instanceof PostGISFeatureStoreType ) {
            fs = create( (PostGISFeatureStoreType) jaxbConfig, baseURL );
        } else if ( jaxbConfig instanceof DirectSQLDataSourceType ) {
            fs = create( (DirectSQLDataSourceType) jaxbConfig );
        } else {
            String msg = Messages.getMessage( "STORE_MANAGER_UNHANDLED_CONFIGTYPE", jaxbConfig.getClass() );
            throw new FeatureStoreException( msg );
        }
        return fs;
    }

    /**
     * @param jaxbConfig
     * @return a corresponding feature store, initialized
     * @throws FeatureStoreException
     */
    public static synchronized FeatureStore create( DirectSQLDataSourceType jaxbConfig )
                            throws FeatureStoreException {
        String connId = jaxbConfig.getConnectionPoolId();
        String srs = jaxbConfig.getStorageSRS();
        String stmt = jaxbConfig.getSQLStatement();
        String name = jaxbConfig.getFeatureTypeName();
        String ns = jaxbConfig.getNamespace();
        String id = jaxbConfig.getDataSourceName();
        String bbox = jaxbConfig.getBBoxStatement();
        LinkedList<Pair<Integer, String>> lods = map( jaxbConfig.getLODStatement(), lodMapper );

        SimpleSQLDatastore fs = new SimpleSQLDatastore( connId, srs, stmt, name, ns, bbox, lods );
        registerAndInit( fs, id );
        return fs;
    }

    /**
     * Returns an initialized {@link FeatureStore} instance from the given JAXB {@link FeatureStoreReferenceType}
     * configuration object.
     * 
     * @param jaxbConfig
     *            configuration object, must not be <code>null</code>
     * @return corresponding {@link FeatureStore} instance, initialized and ready to be used
     * @throws FeatureStoreException
     *             if the creation fails, e.g. due to a configuration error
     */
    public static synchronized FeatureStore create( FeatureStoreReferenceType jaxbConfig )
                            throws FeatureStoreException {
        String storeId = jaxbConfig.getRefId();
        FeatureStore fs = idToFs.get( storeId );
        if ( fs == null ) {
            String msg = Messages.getMessage( "STORE_MANAGER_NO_SUCH_ID", storeId );
            throw new FeatureStoreException( msg );
        }
        return fs;
    }

    /**
     * Returns an initialized {@link ShapeFeatureStore} instance from the given JAXB {@link ShapefileDataSourceType}
     * configuration object.
     * <p>
     * If the configuration specifies an identifier, the instance is also registered as global {@link FeatureStore}.
     * </p>
     * 
     * @param jaxbConfig
     *            configuration object, must not be <code>null</code>
     * @param baseURL
     *            base url (used to resolve relative paths in the configuration)
     * @return corresponding {@link FeatureStore} instance, initialized and ready to be used
     * @throws FeatureStoreException
     *             if the creation fails, e.g. due to a configuration error
     */
    public static synchronized ShapeFeatureStore create( ShapefileDataSourceType jaxbConfig, String baseURL )
                            throws FeatureStoreException {

        String id = jaxbConfig.getDataSourceName();
        XMLAdapter resolver = new XMLAdapter();
        resolver.setSystemId( baseURL );
        String srs = jaxbConfig.getStorageSRS();
        if ( srs == null ) {
            srs = "EPSG:4326";
        } else {
            srs = srs.trim();
        }
        CRS crs = new CRS( srs );

        String shapeFileName = null;
        try {
            shapeFileName = resolver.resolve( jaxbConfig.getFile().trim() ).getFile();
        } catch ( MalformedURLException e ) {
            String msg = Messages.getMessage( "STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage() );
            LOG.error( msg, e );
            throw new FeatureStoreException( msg, e );
        }
        ShapeFeatureStore fs = new ShapeFeatureStore( shapeFileName, crs, null );

        registerAndInit( fs, id );
        return fs;
    }

    /**
     * Returns an initialized {@link MemoryFeatureStore} instance from the given JAXB {@link ShapefileDataSourceType}
     * configuration object.
     * <p>
     * If the configuration specifies an identifier, the instance is also registered as global {@link FeatureStore}.
     * </p>
     * 
     * @param jaxbConfig
     *            configuration object, must not be <code>null</code>
     * @param baseURL
     *            base url (used to resolve relative paths in the configuration)
     * @return corresponding {@link FeatureStore} instance, initialized and ready to be used
     * @throws FeatureStoreException
     *             if the creation fails, e.g. due to a configuration error
     */
    public static synchronized MemoryFeatureStore create( MemoryFeatureStoreType jaxbConfig, String baseURL )
                            throws FeatureStoreException {

        String id = jaxbConfig.getDataSourceName();
        XMLAdapter resolver = new XMLAdapter();
        resolver.setSystemId( baseURL );

        ApplicationSchema schema = null;
        try {
            String[] schemaURLs = new String[jaxbConfig.getGMLSchemaFileURL().size()];
            int i = 0;
            GMLVersionType gmlVersionType = null;
            for ( MemoryFeatureStoreType.GMLSchemaFileURL jaxbSchemaURL : jaxbConfig.getGMLSchemaFileURL() ) {
                schemaURLs[i++] = resolver.resolve( jaxbSchemaURL.getValue().trim() ).toString();
                // TODO what about different versions at the same time?
                gmlVersionType = jaxbSchemaURL.getGmlVersion();
            }
            ApplicationSchemaXSDDecoder decoder = new ApplicationSchemaXSDDecoder(
                                                                                   GMLVersion.valueOf( gmlVersionType.name() ),
                                                                                   getHintMap( jaxbConfig.getNamespaceHint() ),
                                                                                   schemaURLs );
            schema = decoder.extractFeatureTypeSchema();
        } catch ( Exception e ) {
            String msg = Messages.getMessage( "STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage() );
            LOG.error( msg, e );
            throw new FeatureStoreException( msg, e );
        }

        MemoryFeatureStore fs = new MemoryFeatureStore( schema );

        for ( GMLFeatureCollectionFileURL datasetFile : jaxbConfig.getGMLFeatureCollectionFileURL() ) {
            if ( datasetFile != null ) {
                try {
                    GMLVersion version = GMLVersion.valueOf( datasetFile.getGmlVersion().name() );
                    URL docURL = resolver.resolve( datasetFile.getValue().trim() );
                    GMLStreamReader gmlStream = GMLInputFactory.createGMLStreamReader( version, docURL );
                    gmlStream.setApplicationSchema( schema );
                    LOG.debug( "Populating feature store with features from file '" + docURL + "'..." );
                    FeatureCollection fc = (FeatureCollection) gmlStream.readFeature();
                    gmlStream.getIdContext().resolveLocalRefs();

                    FeatureStoreTransaction ta = fs.acquireTransaction();
                    List<String> fids = ta.performInsert( fc, IDGenMode.GENERATE_NEW );
                    LOG.debug( "Inserted " + fids.size() + " features." );
                    ta.commit();
                } catch ( Exception e ) {
                    String msg = Messages.getMessage( "STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage() );
                    LOG.error( msg, e );
                    throw new FeatureStoreException( msg, e );
                }
            }
        }

        registerAndInit( fs, id );
        return fs;
    }

    /**
     * Returns an initialized {@link PostGISFeatureStore} instance from the given JAXB {@link PostGISFeatureStoreType}
     * configuration object.
     * <p>
     * If the configuration specifies an identifier, the instance is also registered as global {@link FeatureStore}.
     * </p>
     * 
     * @param jaxbConfig
     *            configuration object, must not be <code>null</code>
     * @param baseURL
     *            base url (used to resolve relative paths in the configuration)
     * @return corresponding {@link FeatureStore} instance, initialized and ready to be used
     * @throws FeatureStoreException
     *             if the creation fails, e.g. due to a configuration error
     */
    public static synchronized PostGISFeatureStore create( PostGISFeatureStoreType jaxbConfig, String baseURL )
                            throws FeatureStoreException {

        String id = jaxbConfig.getDataSourceName();
        XMLAdapter resolver = new XMLAdapter();
        resolver.setSystemId( baseURL );

        ApplicationSchema schema = null;
        try {
            String[] schemaURLs = new String[jaxbConfig.getGMLSchemaFileURL().size()];
            int i = 0;
            GMLVersionType gmlVersionType = null;
            for ( PostGISFeatureStoreType.GMLSchemaFileURL jaxbSchemaURL : jaxbConfig.getGMLSchemaFileURL() ) {
                schemaURLs[i++] = resolver.resolve( jaxbSchemaURL.getValue().trim() ).toString();
                // TODO what about different versions at the same time?
                gmlVersionType = jaxbSchemaURL.getGmlVersion();
            }
            ApplicationSchemaXSDDecoder decoder = new ApplicationSchemaXSDDecoder(
                                                                                   GMLVersion.valueOf( gmlVersionType.name() ),
                                                                                   getHintMap( jaxbConfig.getNamespaceHint() ),
                                                                                   schemaURLs );
            schema = decoder.extractFeatureTypeSchema();
        } catch ( Exception e ) {
            String msg = Messages.getMessage( "STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage() );
            LOG.error( msg, e );
            throw new FeatureStoreException( msg, e );
        }

        CRS storageSRS = new CRS( jaxbConfig.getStorageSRS() );
        PostGISFeatureStore fs = new PostGISFeatureStore( schema, jaxbConfig.getJDBCConnId(),
                                                          jaxbConfig.getDBSchemaQualifier(), storageSRS );
        registerAndInit( fs, id );
        return fs;
    }

    private static Map<String, String> getHintMap( List<NamespaceHint> hints ) {
        Map<String, String> prefixToNs = new HashMap<String, String>();
        for ( NamespaceHint namespaceHint : hints ) {
            prefixToNs.put( namespaceHint.getPrefix(), namespaceHint.getNamespaceURI() );
        }
        return prefixToNs;
    }

    private static void registerAndInit( FeatureStore fs, String id )
                            throws FeatureStoreException {
        fs.init();
        if ( id != null ) {
            if ( idToFs.containsKey( id ) ) {
                String msg = Messages.getMessage( "STORE_MANAGER_DUPLICATE_ID", id );
                throw new FeatureStoreException( msg );
            }
            LOG.info( "Registering global feature store (" + fs + ") with id '" + id + "'." );
            idToFs.put( id, fs );
        }
    }
}
