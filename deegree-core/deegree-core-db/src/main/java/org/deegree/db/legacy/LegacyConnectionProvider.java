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

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.apache.commons.dbcp2.PoolableConnection;
import org.deegree.commons.jdbc.ConnectionPool;
import org.deegree.db.ConnectionProvider;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceException;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;

/**
 * Implementation that uses the old connection pooling mechanism.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class LegacyConnectionProvider implements ConnectionProvider {

	private LegacyConnectionProviderMetadata metadata;

	private ConnectionPool pool;

	private SQLDialect dialect;

	public LegacyConnectionProvider(String url, String user, String password, boolean readOnly,
			LegacyConnectionProviderMetadata metadata) {
		this.metadata = metadata;
		// hardcoded as until 3.2
		int poolMinSize = 5;
		int poolMaxSize = 25;

		if (metadata != null) {
			pool = new ConnectionPool(metadata.getIdentifier().getId(), url, user, password, readOnly, poolMinSize,
					poolMaxSize);
		}
		else {
			pool = new ConnectionPool("<unspecified>", url, user, password, readOnly, poolMinSize, poolMaxSize);
		}
	}

	public void setDialect(SQLDialect dialect) {
		this.dialect = dialect;
	}

	@Override
	public ResourceMetadata<? extends Resource> getMetadata() {
		return metadata;
	}

	@Override
	public void init() {
		try {
			getConnection().close();
		}
		catch (SQLException e) {
			throw new ResourceInitException(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public Connection getConnection() {
		try {
			return pool.getConnection();
		}
		catch (SQLException e) {
			throw new ResourceException(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void destroy() {
		try {
			pool.destroy();
		}
		catch (Exception e) {
			throw new ResourceException(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public SQLDialect getDialect() {
		return dialect;
	}

	@Override
	public void invalidate(Connection conn) {
		try {
			pool.invalidate((PoolableConnection) conn);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
