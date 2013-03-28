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
package org.deegree.feature.persistence.sql;

import java.net.URL;

import javax.xml.bind.JAXBException;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.sql.jaxb.SQLFeatureStoreJAXB;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.SQLDialectManager;
import org.deegree.sqldialect.SQLDialectProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStoreProvider} for {@link SQLFeatureStore} implementations.
 * <p>
 * This {@link FeatureStoreProvider} needs registered {@link SQLDialectProvider} implementations in order to actually
 * create {@link SQLFeatureStore} instances.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SQLFeatureStoreProvider implements FeatureStoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger( SQLFeatureStoreProvider.class );

    private static final String CONFIG_NS = "http://www.deegree.org/datasource/feature/sql";

    static final String CONFIG_JAXB_PACKAGE = "org.deegree.feature.persistence.sql.jaxb";

    static final URL CONFIG_SCHEMA = SQLFeatureStoreProvider.class.getResource( "/META-INF/schemas/datasource/feature/sql/3.2.0/sql.xsd" );

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
    public SQLFeatureStore create( URL configURL )
                            throws ResourceInitException {

        try {
            SQLFeatureStoreJAXB cfg = (SQLFeatureStoreJAXB) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA,
                                                                                  configURL, workspace );
            ConnectionManager mgr = workspace.getSubsystemManager( ConnectionManager.class );
            Type connType = mgr.getType( cfg.getJDBCConnId().getValue() );
            if ( connType == null ) {
                throw new ResourceInitException( "No JDBC connection with id '" + cfg.getJDBCConnId().getValue() + "' defined." );
            }
            LOG.debug( "Connection type is {}.", connType );

            SQLDialectManager dialectMgr = workspace.getSubsystemManager( SQLDialectManager.class );
            if ( dialectMgr == null ) {
                 throw new ResourceInitException( "SQLDialectManager not found in workspace / classpath." );
            }
            SQLDialect dialect = dialectMgr.create( cfg.getJDBCConnId().getValue() );
            return new SQLFeatureStore( cfg, configURL, dialect, null );
        } catch ( JAXBException e ) {
            LOG.info( "Stack trace: ", e );
            throw new ResourceInitException( "Error when parsing configuration: " + e.getLocalizedMessage(), e );
        }
    }

    /**
     * Returns the SQL statements for creating the database tables.
     * 
     * @return the SQL statements for creating the database tables, never <code>null</code>
     */
    public String[] getDDL() {
        return null;
    }

    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { ConnectionManager.class, SQLDialectManager.class };
    }
}