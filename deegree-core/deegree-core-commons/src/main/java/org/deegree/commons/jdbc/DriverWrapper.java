/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.commons.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Workaround class to fix inability of DriverManager to accept classes not loaded with
 * system class loader...
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class DriverWrapper implements Driver {

	private Driver d;

	public DriverWrapper(Driver d) {
		this.d = d;
	}

	@Override
	public boolean acceptsURL(String url) throws SQLException {
		return d.acceptsURL(url);
	}

	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		return d.connect(url, info);
	}

	@Override
	public int getMajorVersion() {
		return d.getMajorVersion();
	}

	@Override
	public int getMinorVersion() {
		return d.getMinorVersion();
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		return d.getPropertyInfo(url, info);
	}

	@Override
	public boolean jdbcCompliant() {
		return d.jdbcCompliant();
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		Logger logger = null;
		try {
			Method m = d.getClass().getDeclaredMethod("getParentLogger", new Class[0]);
			logger = (Logger) m.invoke(d, new Object[0]);
		}
		catch (NoSuchMethodException e) {
			throw new SQLFeatureNotSupportedException(e.getMessage(), e);
		}
		catch (SecurityException e) {
			throw new SQLFeatureNotSupportedException(e.getMessage(), e);
		}
		catch (IllegalAccessException e) {
			throw new SQLFeatureNotSupportedException(e.getMessage(), e);
		}
		catch (IllegalArgumentException e) {
			throw new SQLFeatureNotSupportedException(e.getMessage(), e);
		}
		catch (InvocationTargetException e) {
			throw new SQLFeatureNotSupportedException(e.getMessage(), e);
		}
		return logger;
	}

}
