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

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.deegree.db.ConnectionProvider;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceException;
import org.deegree.workspace.ResourceMetadata;
import org.slf4j.Logger;

/**
 * {@link ConnectionProvider} based on <code>javax.sql.DataSource</code>.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
class DataSourceConnectionProvider implements ConnectionProvider {

	private static final Logger LOG = getLogger(DataSourceConnectionProvider.class);

	private final DataSourceConnectionProviderMetadata resourceMetadata;

	private final DataSource ds;

	private final SQLDialect dialect;

	private final Method destroyMethod;

	/**
	 * Creates a new {@link DataSourceConnectionProvider} instance.
	 * @param resourceMetadata metadata, must not be <code>null</code>
	 * @param ds data source, must not be <code>null</code>
	 * @param dialect SQL dialect, can be <code>null</code>
	 * @param destroyMethod data source method for destroying the data source on shutdown,
	 * can be <code>null</code>
	 */
	DataSourceConnectionProvider(final DataSourceConnectionProviderMetadata resourceMetadata, final DataSource ds,
			final SQLDialect dialect, final Method destroyMethod) {
		this.resourceMetadata = resourceMetadata;
		this.ds = ds;
		this.dialect = dialect;
		this.destroyMethod = destroyMethod;
	}

	@Override
	public ResourceMetadata<? extends Resource> getMetadata() {
		return resourceMetadata;
	}

	@Override
	public void init() {
		// nothing to do
	}

	@Override
	public Connection getConnection() {
		try {
			return ds.getConnection();
		}
		catch (SQLException e) {
			String msg = "Unable to retrieve JDBC connection from DataSource: " + e.getLocalizedMessage();
			LOG.error(msg);
			throw new ResourceException(msg, e);
		}
	}

	@Override
	public void destroy() {
		if (destroyMethod != null) {
			LOG.info("Closing connection pool {}", resourceMetadata.getIdentifier());
			try {
				destroyMethod.invoke(ds);
			}
			catch (Exception e) {
				LOG.error("Error destroying DataSource instance: {}", e.getLocalizedMessage());
			}
		}
		else {
			LOG.warn("Unable to close connection pool {}. Check the DataSource configuration if the attribute "
					+ "'destroyMethod' is configured.", resourceMetadata.getIdentifier());
		}
	}

	@Override
	public SQLDialect getDialect() {
		return dialect;
	}

	@Override
	public void invalidate(Connection conn) {
		// nothing to do
	}

}
