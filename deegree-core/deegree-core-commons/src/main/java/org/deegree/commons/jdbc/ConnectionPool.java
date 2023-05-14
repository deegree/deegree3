/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.commons.jdbc;

import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;

/**
 * Simple implementation of a JDBC connection pool based on the Apache Commons Pool and
 * DBCP projects.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class ConnectionPool {

	private static final Logger LOG = getLogger(ConnectionPool.class);

	private final String id;

	private final PoolingDataSource ds;

	private final GenericObjectPool<PoolableConnection> pool;

	/**
	 * Creates a new {@link ConnectionPool} instance.
	 * @param id
	 * @param connectURI
	 * @param user
	 * @param password
	 * @param readOnly
	 * @param minIdle
	 * @param maxActive
	 */
	public ConnectionPool(String id, String connectURI, String user, String password, boolean readOnly, int minIdle,
			int maxActive) {

		this.id = id;
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, user, password);
		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
		pool = new GenericObjectPool<>(poolableConnectionFactory);
		pool.setMinIdle(minIdle);
		pool.setMaxTotal(maxActive);
		pool.setTestOnBorrow(true);

		poolableConnectionFactory.setPool(pool);
		ds = new PoolingDataSource(pool);

		// needed, so users can retrieve the underlying connection from pooled
		// connections, e.g. to access the
		// LargeObjectManager from a PGConnection
		ds.setAccessToUnderlyingConnectionAllowed(true);
	}

	/**
	 * Returns a {@link Connection} from the pool.
	 * @return a connection from the pool
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		LOG.debug("For connection id '{}': active connections: {}, idle connections: {}",
				new Object[] { id, pool.getNumActive(), pool.getNumIdle() });
		return ds.getConnection();
	}

	/**
	 * @throws Exception
	 */
	public void destroy() throws Exception {
		pool.close();
		ds.close();
	}

	public void invalidate(PoolableConnection conn) throws Exception {
		conn.getDelegate().close();
		conn.reallyClose();
		pool.invalidateObject(conn);
	}

}
