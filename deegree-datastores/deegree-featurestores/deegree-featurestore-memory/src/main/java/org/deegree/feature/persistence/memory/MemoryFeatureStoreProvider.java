//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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
package org.deegree.feature.persistence.memory;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.deegree.protocol.wfs.transaction.action.IDGenMode.USE_EXISTING;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.memory.jaxb.GMLVersionType;
import org.deegree.feature.persistence.memory.jaxb.MemoryFeatureStoreConfig;
import org.deegree.feature.persistence.memory.jaxb.MemoryFeatureStoreConfig.GMLFeatureCollection;
import org.deegree.feature.persistence.memory.jaxb.MemoryFeatureStoreConfig.GMLSchema;
import org.deegree.feature.persistence.memory.jaxb.MemoryFeatureStoreConfig.NamespaceHint;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStoreProvider} for the {@link MemoryFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MemoryFeatureStoreProvider implements FeatureStoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger( MemoryFeatureStoreProvider.class );

    private static final String CONFIG_NS = "http://www.deegree.org/datasource/feature/memory";

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.feature.persistence.memory.jaxb";

    private static final URL CONFIG_SCHEMA = MemoryFeatureStoreProvider.class.getResource( "/META-INF/schemas/datasource/feature/memory/3.0.0/memory.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public String getConfigNamespace() {
        return CONFIG_NS;
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

    @Override
    public MemoryFeatureStore create( URL configURL )
                            throws ResourceInitException {

        MemoryFeatureStore fs = null;
        ICRS storageSRS = null;
        try {
            MemoryFeatureStoreConfig config = (MemoryFeatureStoreConfig) unmarshall( CONFIG_JAXB_PACKAGE,
                                                                                     CONFIG_SCHEMA, configURL,
                                                                                     workspace );

            AppSchema schema = null;
            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( configURL.toString() );
            try {
                String[] schemaURLs = new String[config.getGMLSchema().size()];
                int i = 0;
                GMLVersionType gmlVersionType = null;
                for ( GMLSchema jaxbSchemaURL : config.getGMLSchema() ) {
                    schemaURLs[i++] = resolver.resolve( jaxbSchemaURL.getValue().trim() ).toString();
                    // TODO what about different versions at the same time?
                    gmlVersionType = jaxbSchemaURL.getVersion();
                }

                GMLAppSchemaReader decoder = null;
                if ( schemaURLs.length == 1 && schemaURLs[0].startsWith( "file:" ) ) {
                    File file = new File( new URL( schemaURLs[0] ).toURI() );
                    decoder = new GMLAppSchemaReader( GMLVersion.valueOf( gmlVersionType.name() ),
                                                       getHintMap( config.getNamespaceHint() ), file );
                } else {
                    decoder = new GMLAppSchemaReader( GMLVersion.valueOf( gmlVersionType.name() ),
                                                       getHintMap( config.getNamespaceHint() ), schemaURLs );
                }
                schema = decoder.extractAppSchema();
                if ( config.getStorageCRS() != null ) {
                    storageSRS = CRSManager.lookup( config.getStorageCRS() );
                }
            } catch ( Exception e ) {
                String msg = Messages.getMessage( "STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage() );
                LOG.error( msg, e );
                throw new ResourceInitException( msg, e );
            }

            fs = new MemoryFeatureStore( schema, storageSRS, null );
            for ( GMLFeatureCollection datasetFile : config.getGMLFeatureCollection() ) {
                if ( datasetFile != null ) {
                    try {
                        GMLVersion version = GMLVersion.valueOf( datasetFile.getVersion().name() );
                        URL docURL = resolver.resolve( datasetFile.getValue().trim() );
                        GMLStreamReader gmlStream = GMLInputFactory.createGMLStreamReader( version, docURL );
                        gmlStream.setApplicationSchema( schema );
                        LOG.info( "Populating feature store with features from file '" + docURL + "'..." );
                        FeatureCollection fc = (FeatureCollection) gmlStream.readFeature();
                        gmlStream.getIdContext().resolveLocalRefs();

                        FeatureStoreTransaction ta = fs.acquireTransaction();
                        int fids = ta.performInsert( fc, USE_EXISTING ).size();
                        LOG.info( "Inserted " + fids + " features." );
                        ta.commit();
                    } catch ( Exception e ) {
                        String msg = Messages.getMessage( "STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage() );
                        LOG.error( msg, e );
                        throw new FeatureStoreException( msg, e );
                    }
                }
            }
        } catch ( JAXBException e ) {
            String msg = "Error in feature store configuration file '" + configURL + "': " + e.getMessage();
            LOG.error( msg );
            throw new ResourceInitException( msg, e );
        } catch ( FeatureStoreException e ) {
            String msg = "Error in feature store configuration file '" + configURL + "': " + e.getMessage();
            LOG.error( msg );
            throw new ResourceInitException( msg, e );
        }
        return fs;
    }

    private static Map<String, String> getHintMap( List<NamespaceHint> hints ) {
        Map<String, String> prefixToNs = new HashMap<String, String>();
        for ( NamespaceHint namespaceHint : hints ) {
            prefixToNs.put( namespaceHint.getPrefix(), namespaceHint.getNamespaceURI() );
        }
        return prefixToNs;
    }

    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { ProxyUtils.class };
    }
}
