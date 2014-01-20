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

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.deegree.db.ConnectionProvider;
import org.deegree.db.datasource.jaxb.DataSourceConnectionProvider;
import org.deegree.db.dialect.SqlDialects;
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
        final DataSource ds = initializeDataSourceInstance();
        final Method destroyMethod = getDestroyMethod( ds, config.getDataSource().getDestroyMethod() );
        final SQLDialect dialect = lookupSqlDialect( ds );
        return new org.deegree.db.datasource.DataSourceConnectionProvider( metadata, ds, dialect, destroyMethod );
    }

    private DataSource initializeDataSourceInstance() {
        final DataSourceInitializer initializer = new DataSourceInitializer( workspace.getModuleClassLoader() );
        return initializer.getConfiguredDataSource( config );
    }

    Method getDestroyMethod( final DataSource ds, final String methodName ) {
        if ( methodName != null ) {
            try {
                return ds.getClass().getMethod( methodName );
            } catch ( Exception e ) {
                String msg = "Cannot find specified destroy method '" + methodName + "' for class '"
                             + ds.getClass().getCanonicalName() + "'";
                LOG.error( msg );
            }
        }
        return null;
    }

    private SQLDialect lookupSqlDialect( final DataSource ds ) {
        SQLDialect dialect = null;
        Connection conn = null;
        try {
            conn = ds.getConnection();
            dialect = SqlDialects.lookupSqlDialect( conn, workspace.getModuleClassLoader() );
        } catch ( SQLException e ) {
            String msg = "Error connecting to DB: " + e.getLocalizedMessage();
            LOG.error( msg );
            return null;
        } finally {
            if ( conn != null ) {
                close( conn );
            }
        }
        return dialect;
    }

}
