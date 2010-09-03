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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
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

    private static Mapper<Pair<Integer, String>, LODStatement> lodMapper = new Mapper<Pair<Integer, String>, LODStatement>() {
        public Pair<Integer, String> apply( LODStatement u ) {
            return new Pair<Integer, String>( u.getAboveScale(), u.getValue() );
        }
    };

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/datasource/feature/simplesql";
    }

    @Override
    public URL getConfigSchema() {
        return SimpleSQLFeatureStoreProvider.class.getResource( "/META-INF/schemas/datasource/0.5.0/feature/simpleqsl.xsd" );
    }

    @Override
    public URL getConfigTemplate() {
        return SimpleSQLFeatureStoreProvider.class.getResource( "/META-INF/schemas/datasource/0.5.0/feature/example_simpleqsl.xsd" );
    }

    @Override
    public FeatureStore getFeatureStore( URL configURL )
                            throws FeatureStoreException {

        SimpleSQLFeatureStore fs = null;
        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.feature.persistence.simplesql.jaxb" );
            Unmarshaller u = jc.createUnmarshaller();
            SimpleSQLFeatureStoreConfig config = (SimpleSQLFeatureStoreConfig) u.unmarshal( configURL );

            String connId = config.getConnectionPoolId();
            String srs = config.getStorageSRS();
            String stmt = config.getSQLStatement();
            String name = config.getFeatureTypeName();
            String ns = config.getNamespace();
            String bbox = config.getBBoxStatement();
            LinkedList<Pair<Integer, String>> lods = map( config.getLODStatement(), lodMapper );

            fs = new SimpleSQLFeatureStore( connId, srs, stmt, name, ns, bbox, lods );
        } catch ( JAXBException e ) {
            String msg = "Error in feature store configuration file '" + configURL + "': " + e.getMessage();
            LOG.error( msg );
            throw new FeatureStoreException( msg, e );
        }
        return fs;
    }
}
