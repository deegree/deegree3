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
package org.deegree.db.dialect;

import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.deegree.sqldialect.SQLDialect;
import org.slf4j.Logger;

/**
 * Static utility methods for common {@link SQLDialect} tasks.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
public class SqlDialects {

	private static final Logger LOG = getLogger(SqlDialects.class);

	/**
	 * Looks up the matching {@link SQLDialect} for the given {@link Connection}.
	 * @param conn database connection, must not be <code>null</code>
	 * @param classLoader class loader to use, must not be <code>null</code>
	 * @return matching SQL dialect, can be <code>null</code> (no such dialect)
	 */
	public static SQLDialect lookupSqlDialect(Connection conn, ClassLoader classLoader) {
		ServiceLoader<SqlDialectProvider> dialectLoader = ServiceLoader.load(SqlDialectProvider.class, classLoader);
		Iterator<SqlDialectProvider> iter = dialectLoader.iterator();
		SQLDialect dialect = null;
		while (iter.hasNext()) {
			SqlDialectProvider prov = iter.next();
			if (prov.supportsConnection(conn)) {
				dialect = prov.createDialect(conn);
				break;
			}
		}
		if (dialect == null) {
			LOG.warn("No SQL dialect for connection found, trying to continue.");
		}
		return dialect;
	}

}
