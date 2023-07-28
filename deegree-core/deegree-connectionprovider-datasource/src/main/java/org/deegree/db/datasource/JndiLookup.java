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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;

/**
 * Helper class for using {@link DataSourceConnectionProvider} with JNDI: Provides a
 * static factory method for retrieving <code>javax.sql.DataSource</code> objects via
 * JNDI.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
public class JndiLookup {

	private static final Logger LOG = getLogger(JndiLookup.class);

	/**
	 * Retrieves the specified JNDI <code>javax.sql.DataSource</code>.
	 * @param jndiName name of the JNDI resource, must not be <code>null</code>
	 * @return DataSource object, never <code>null</code>
	 * @throws NamingException if the lookup of the specified resource failed
	 * @throws ClassCastException if the designated resource is not a
	 * <code>javax.sql.DataSource</code>
	 */
	public static final DataSource lookup(final String jndiName) throws NamingException, ClassCastException {
		LOG.debug("Looking up JNDI DataSource '" + jndiName + "'");
		Object object = null;
		try {
			final InitialContext initialContext = new InitialContext();
			object = initialContext.lookup(jndiName);
		}
		catch (NamingException e) {
			String msg = "Error retrieving JNDI DataSource '" + jndiName + "': " + e.getLocalizedMessage();
			LOG.error(msg, e);
			throw e;
		}
		if (!(object instanceof DataSource)) {
			final String msg = "Error retrieving JNDI DataSource '" + jndiName
					+ "': JNDI object is not a javax.sql.DataSource.";
			LOG.error(msg);
			throw new ClassCastException(msg);
		}
		return (DataSource) object;
	}

}
