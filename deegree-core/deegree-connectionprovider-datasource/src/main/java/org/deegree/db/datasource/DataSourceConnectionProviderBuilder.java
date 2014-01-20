/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.db.datasource;

import static org.deegree.commons.utils.JDBCUtils.close;
import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.sql.DataSource;

import org.deegree.db.ConnectionProvider;
import org.deegree.db.datasource.jaxb.DataSourceConnectionProvider;
import org.deegree.db.dialect.SqlDialectProvider;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * {@link ResourceBuilder} for the {@link DataSourceConnectionProvider}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
class DataSourceConnectionProviderBuilder implements ResourceBuilder<ConnectionProvider> {

    private static final Logger LOG = getLogger( DataSourceConnectionProviderBuilder.class );

    private final DataSourceConnectionProvider config;

    private final DataSourceConnectionProviderMetadata metadata;

    private final Workspace workspace;

    DataSourceConnectionProviderBuilder( final DataSourceConnectionProvider config,
                                         final DataSourceConnectionProviderMetadata metadata, final Workspace workspace ) {
        this.config = config;
        this.metadata = metadata;
        this.workspace = workspace;
    }

    @Override
    public ConnectionProvider build() {
        final DataSource ds = createOrRetrieveDataSourceInstance();
        Connection conn = null;
        try {
            conn = ds.getConnection();
        } catch ( SQLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        SQLDialect dialect = null;
        try {
            dialect = lookupSqlDialect( conn );
        } finally {
            close( conn );
        }
        return new org.deegree.db.datasource.DataSourceConnectionProvider( metadata, ds, dialect );
    }

    DataSource createOrRetrieveDataSourceInstance() {
        return null;
    }

    SQLDialect lookupSqlDialect( Connection conn ) {
        ServiceLoader<SqlDialectProvider> dialectLoader = ServiceLoader.load( SqlDialectProvider.class,
                                                                              workspace.getModuleClassLoader() );
        Iterator<SqlDialectProvider> iter = dialectLoader.iterator();
        SQLDialect dialect = null;
        while ( iter.hasNext() ) {
            SqlDialectProvider prov = iter.next();
            if ( prov.supportsConnection( conn ) ) {
                dialect = prov.createDialect( conn );
                break;
            }
        }
        if ( dialect == null ) {
            LOG.warn( "No SQL dialect for connection found, trying to continue." );
        }
        return dialect;
    }
}
