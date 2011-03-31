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
package org.deegree.feature.persistence.postgis;

import static org.deegree.commons.jdbc.ConnectionManager.Type.PostgreSQL;

import java.net.URL;
import java.util.HashMap;
import java.util.ServiceLoader;

import javax.xml.bind.JAXBException;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.postgis.jaxb.PostGISFeatureStoreConfig;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.SQLFeatureStoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStoreProvider} for the {@link PostGISFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISFeatureStoreProvider implements FeatureStoreProvider, SQLFeatureStoreProvider<PostGISFeatureStore> {

    private static final Logger LOG = LoggerFactory.getLogger( PostGISFeatureStoreProvider.class );

    private static final String CONFIG_NS = "http://www.deegree.org/datasource/feature/postgis";

    static final String CONFIG_JAXB_PACKAGE = "org.deegree.feature.persistence.postgis.jaxb";

    static final URL CONFIG_SCHEMA = PostGISFeatureStoreProvider.class.getResource( "/META-INF/schemas/datasource/feature/sql/3.1.0/sql.xsd" );

    private DeegreeWorkspace workspace;

    private HashMap<Type, SQLFeatureStoreProvider<? extends SQLFeatureStore>> providers = new HashMap<Type, SQLFeatureStoreProvider<? extends SQLFeatureStore>>();

    @Override
    public String getConfigNamespace() {
        return CONFIG_NS;
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

    @Override
    public SQLFeatureStore create( URL configURL )
                            throws ResourceInitException {

        try {
            PostGISFeatureStoreConfig cfg = (PostGISFeatureStoreConfig) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE,
                                                                                              CONFIG_SCHEMA, configURL,
                                                                                              workspace );
            ConnectionManager mgr = workspace.getSubsystemManager( ConnectionManager.class );
            Type connType = mgr.getType( cfg.getJDBCConnId() );
            SQLFeatureStoreProvider<? extends SQLFeatureStore> provider = providers.get( connType );
            if ( provider != null ) {
                return provider.create( cfg, configURL, workspace );
            }
            throw new ResourceInitException( "No provider found for " + connType.name() + " SQL feature stores." );
        } catch ( JAXBException e ) {
            LOG.trace( "Stack trace: ", e );
            throw new ResourceInitException( "Error when parsing configuration: " + e.getLocalizedMessage(), e );
        }

    }

    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
        for ( SQLFeatureStoreProvider<? extends SQLFeatureStore> p : ServiceLoader.load(
                                                                                         SQLFeatureStoreProvider.class,
                                                                                         workspace.getModuleClassLoader() ) ) {
            providers.put( p.getSupportedType(), p );
        }
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { ConnectionManager.class };
    }

    public Type getSupportedType() {
        return PostgreSQL;
    }

    public PostGISFeatureStore create( PostGISFeatureStoreConfig config, URL configURL, DeegreeWorkspace workspace ) {
        return new PostGISFeatureStore( config, configURL, workspace );
    }
}