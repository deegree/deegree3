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
package org.deegree.feature.persistence.oracle;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.commons.configuration.GMLVersionType;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.CRS;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.oracle.jaxb.OracleFeatureStoreConfig;
import org.deegree.feature.persistence.oracle.jaxb.OracleFeatureStoreConfig.GMLSchemaFileURL;
import org.deegree.feature.persistence.oracle.jaxb.OracleFeatureStoreConfig.NamespaceHint;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStoreProvider} for the {@link OracleFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OracleFeatureStoreProvider implements FeatureStoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger( OracleFeatureStoreProvider.class );
    
    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/datasource/feature/oracle";
    }

    @Override
    public FeatureStore getFeatureStore( URL configURL )
                            throws FeatureStoreException {

        FeatureStore fs = null;
        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.feature.persistence.oracle.jaxb" );
            Unmarshaller u = jc.createUnmarshaller();
            OracleFeatureStoreConfig config = (OracleFeatureStoreConfig) u.unmarshal( configURL );

            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( configURL.toString() );

            ApplicationSchema schema = null;

            try {
                String[] schemaURLs = new String[config.getGMLSchemaFileURL().size()];
                int i = 0;
                GMLVersionType gmlVersionType = null;
                for ( GMLSchemaFileURL jaxbSchemaURL : config.getGMLSchemaFileURL() ) {
                    schemaURLs[i++] = resolver.resolve( jaxbSchemaURL.getValue().trim() ).toString();
                    // TODO what about different versions at the same time?
                    gmlVersionType = GMLVersionType.GML_32;
                }
                ApplicationSchemaXSDDecoder decoder = new ApplicationSchemaXSDDecoder(
                                                                                       GMLVersion.valueOf( gmlVersionType.name() ),
                                                                                       getHintMap( config.getNamespaceHint() ),
                                                                                       schemaURLs );
                schema = decoder.extractFeatureTypeSchema();

            } catch ( Exception e ) {
                String msg = Messages.getMessage( "STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage() );
                LOG.error( msg, e );
                throw new FeatureStoreException( msg, e );
            }

            CRS storageSRS = new CRS( config.getStorageSRS() );
            fs = new OracleFeatureStore( schema, config.getJDBCConnId(), config.getDBSchemaQualifier(), storageSRS);
        } catch ( JAXBException e ) {
            String msg = "Error in feature store configuration file '" + configURL + "': " + e.getMessage();
            LOG.error( msg );
            throw new FeatureStoreException( msg, e );
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
}
