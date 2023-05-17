/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.sqldialect.postgis;

import static org.deegree.commons.utils.JDBCUtils.close;
import static org.deegree.commons.utils.JDBCUtils.determinePostGISVersion;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.deegree.db.dialect.SqlDialectProvider;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.workspace.ResourceInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SqlDialectProvider} for PostGIS-enabled PostgreSQL databases.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class PostGISDialectProvider implements SqlDialectProvider {

	private static Logger LOG = LoggerFactory.getLogger(PostGISDialectProvider.class);

	@Override
	public boolean supportsConnection(final Connection connection) {
		String url = null;
		try {
			url = connection.getMetaData().getURL();
		}
		catch (Exception e) {
			LOG.debug("Could not determine metadata/url of connection: {}", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
			return false;
		}
		return url.startsWith("jdbc:postgresql:");
	}

	@Override
	public SQLDialect createDialect(final Connection conn) {
		Statement stmt = null;
		ResultSet rs = null;
		String version = null;
		try {
			version = determinePostGISVersion(conn, LOG);
		}
		catch (Exception e) {
			LOG.trace(e.getMessage(), e);
			throw new ResourceInitException(e.getMessage(), e);
		}
		finally {
			close(rs, stmt, null, LOG);
		}
		return new PostGISDialect(version);
	}

}
