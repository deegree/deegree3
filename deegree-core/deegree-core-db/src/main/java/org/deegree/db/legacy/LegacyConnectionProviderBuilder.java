/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.db.legacy;

import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.deegree.commons.utils.JDBCUtils;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.dialect.SqlDialectProvider;
import org.deegree.db.legacy.jaxb.JDBCConnection;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * Builds legacy connection providers.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class LegacyConnectionProviderBuilder implements ResourceBuilder<ConnectionProvider> {

	private static final Logger LOG = getLogger(LegacyConnectionProviderBuilder.class);

	private JDBCConnection config;

	private LegacyConnectionProviderMetadata metadata;

	private Workspace workspace;

	public LegacyConnectionProviderBuilder(JDBCConnection config, LegacyConnectionProviderMetadata metadata,
			Workspace workspace) {
		this.config = config;
		this.metadata = metadata;
		this.workspace = workspace;
	}

	@Override
	public ConnectionProvider build() {
		String url = config.getUrl();
		LegacyConnectionProvider cprov;
		cprov = new LegacyConnectionProvider(url, config.getUser(), config.getPassword(),
				config.isReadOnly() == null ? false : config.isReadOnly(), metadata);

		ServiceLoader<SqlDialectProvider> dialectLoader = ServiceLoader.load(SqlDialectProvider.class,
				workspace.getModuleClassLoader());
		Iterator<SqlDialectProvider> iter = dialectLoader.iterator();
		SQLDialect dialect = null;
		while (iter.hasNext()) {
			SqlDialectProvider prov = iter.next();
			Connection conn = null;
			try {
				conn = cprov.getConnection();
				if (prov.supportsConnection(conn)) {
					dialect = prov.createDialect(conn);
					break;
				}
			}
			finally {
				JDBCUtils.close(conn);
			}
		}
		cprov.setDialect(dialect);
		if (dialect == null) {
			LOG.warn("No SQL dialect for {} found, trying to continue.", url);
		}
		return cprov;
	}

}
