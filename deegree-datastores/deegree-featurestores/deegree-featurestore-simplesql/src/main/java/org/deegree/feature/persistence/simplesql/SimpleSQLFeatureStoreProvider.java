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
package org.deegree.feature.persistence.simplesql;

import static org.deegree.commons.utils.CollectionUtils.map;

import java.net.URL;
import java.util.LinkedList;

import javax.xml.bind.JAXBException;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.simplesql.jaxb.SimpleSQLFeatureStoreConfig;
import org.deegree.feature.persistence.simplesql.jaxb.SimpleSQLFeatureStoreConfig.LODStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStoreProvider} for the {@link SimpleSQLFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SimpleSQLFeatureStoreProvider implements FeatureStoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger( SimpleSQLFeatureStoreProvider.class );

    private static final String CONFIG_NS = "http://www.deegree.org/datasource/feature/simplesql";

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.feature.persistence.simplesql.jaxb";

    private static final URL CONFIG_SCHEMA = SimpleSQLFeatureStoreProvider.class.getResource( "/META-INF/schemas/datasource/feature/simplesql/3.0.1/simplesql.xsd" );

    private DeegreeWorkspace workspace;

    private static Mapper<Pair<Integer, String>, LODStatement> lodMapper = new Mapper<Pair<Integer, String>, LODStatement>() {
        public Pair<Integer, String> apply( LODStatement u ) {
            return new Pair<Integer, String>( u.getAboveScale(), u.getValue() );
        }
    };

    @Override
    public String getConfigNamespace() {
        return CONFIG_NS;
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

    @Override
    public SimpleSQLFeatureStore create( URL configURL )
                            throws ResourceInitException {

        SimpleSQLFeatureStore fs = null;
        try {
            SimpleSQLFeatureStoreConfig config;
            config = (SimpleSQLFeatureStoreConfig) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA, configURL,
                                                                         workspace );
            String connId = config.getConnectionPoolId();
            if ( connId == null ) {
                connId = config.getJDBCConnId();
            }
            String srs = config.getStorageCRS();
            String stmt = config.getSQLStatement();
            String name = config.getFeatureTypeName();
            String ns = config.getFeatureTypeNamespace();
            String prefix = config.getFeatureTypePrefix();
            String bbox = config.getBBoxStatement();
            LinkedList<Pair<Integer, String>> lods = map( config.getLODStatement(), lodMapper );

            fs = new SimpleSQLFeatureStore( connId, srs, stmt, name, ns, prefix, bbox, lods, null );
        } catch ( JAXBException e ) {
            String msg = "Error in feature store configuration file '" + configURL + "': " + e.getMessage();
            LOG.error( msg );
            throw new ResourceInitException( msg, e );
        }
        return fs;
    }

    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { ProxyUtils.class, ConnectionManager.class };
    }
}
