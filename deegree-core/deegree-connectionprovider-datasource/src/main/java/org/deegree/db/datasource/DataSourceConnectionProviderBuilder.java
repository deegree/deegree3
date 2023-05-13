/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.db.datasource;

import static org.deegree.commons.utils.JDBCUtils.close;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Method;
import java.sql.Connection;

import javax.sql.DataSource;

import org.deegree.db.ConnectionProvider;
import org.deegree.db.dialect.SqlDialectProvider;
import org.deegree.db.dialect.SqlDialects;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceException;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * {@link ResourceBuilder} for the {@link DataSourceConnectionProvider}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * @since 3.4
 */
class DataSourceConnectionProviderBuilder implements ResourceBuilder<ConnectionProvider> {

	private static final Logger LOG = getLogger(DataSourceConnectionProviderBuilder.class);

	private final org.deegree.db.datasource.jaxb.DataSourceConnectionProvider config;

	private final DataSourceConnectionProviderMetadata metadata;

	private final Workspace workspace;

	DataSourceConnectionProviderBuilder(final org.deegree.db.datasource.jaxb.DataSourceConnectionProvider config,
			final DataSourceConnectionProviderMetadata metadata, final Workspace workspace) {
		this.config = config;
		this.metadata = metadata;
		this.workspace = workspace;
	}

	@Override
	public ConnectionProvider build() {
		final DataSource ds = initializeDataSourceInstance();
		final Method destroyMethod = getDestroyMethod(ds, config.getDataSource().getDestroyMethod());
		final Connection conn = checkConnectivity(ds);
		final SQLDialect dialect;
		if (config.getDialectProvider() != null) {
			String dialectProviderCls = config.getDialectProvider().getJavaClass();
			try {
				Class<?> clazz = workspace.getModuleClassLoader().loadClass(dialectProviderCls);
				SqlDialectProvider prov = clazz.asSubclass(SqlDialectProvider.class).newInstance();
				dialect = prov.createDialect(conn);
			}
			catch (Exception ex) {
				final String msg = "Configured SQL dialect provider '" + dialectProviderCls + "' failed to initialize: "
						+ ex.getLocalizedMessage();
				throw new ResourceException(msg, ex);
			}
		}
		else {
			dialect = SqlDialects.lookupSqlDialect(conn, workspace.getModuleClassLoader());
		}
		close(conn);
		return new DataSourceConnectionProvider(metadata, ds, dialect, destroyMethod);
	}

	private DataSource initializeDataSourceInstance() {
		try {
			final DataSourceInitializer initializer = new DataSourceInitializer(workspace.getModuleClassLoader());
			return initializer.getConfiguredDataSource(config);
		}
		catch (Exception e) {
			String msg = getMessageOrCauseMessage(e);
			LOG.error(msg, e);
			throw new ResourceException(msg, e);
		}
	}

	private String getMessageOrCauseMessage(final Exception e) {
		if (e.getLocalizedMessage() != null) {
			return e.getLocalizedMessage();
		}
		if (e.getCause() != null) {
			return e.getCause().getLocalizedMessage();
		}
		return null;
	}

	private Connection checkConnectivity(DataSource ds) {
		try {
			return ds.getConnection();
		}
		catch (Exception e) {
			String msg = "Error connecting to database: " + e.getLocalizedMessage();
			throw new ResourceException(msg, e);
		}
	}

	Method getDestroyMethod(final DataSource ds, final String methodName) {
		if (methodName != null) {
			try {
				return ds.getClass().getMethod(methodName);
			}
			catch (Exception e) {
				String msg = "Cannot find specified destroy method '" + methodName + "' for class '"
						+ ds.getClass().getCanonicalName() + "'";
				LOG.error(msg);
			}
		}
		return null;
	}

}
