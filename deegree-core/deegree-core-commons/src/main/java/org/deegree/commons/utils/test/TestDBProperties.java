/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.commons.utils.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides convenient access to test database configurations defined in
 * {@link TestProperties}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class TestDBProperties {

	private static Logger LOG = LoggerFactory.getLogger(TestDBProperties.class);

	private static final String TESTDB_PROPERTY_PREFIX = "testdb.";

	private final String id;

	private final String adminUrl;

	private final String adminUser;

	private final String adminPass;

	private final String dbName;

	private final String url;

	private final String user;

	private final String pass;

	private TestDBProperties(String id, Properties props) throws IllegalArgumentException {
		this.id = id;
		String prefix = TESTDB_PROPERTY_PREFIX + id + ".";
		this.adminUrl = props.getProperty(prefix + "adminurl");
		this.adminUser = props.getProperty(prefix + "adminuser");
		this.adminPass = props.getProperty(prefix + "adminpass");
		this.dbName = props.getProperty(prefix + "name");
		this.url = props.getProperty(prefix + "url");
		this.user = props.getProperty(prefix + "user");
		this.pass = props.getProperty(prefix + "pass");
	}

	public String getId() {
		return id;
	}

	public String getAdminUrl() {
		return adminUrl;
	}

	public String getAdminUser() {
		return adminUser;
	}

	public String getAdminPass() {
		return adminPass;
	}

	public String getDbName() {
		return dbName;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}

	public static List<TestDBProperties> getAll() throws IllegalArgumentException, IOException {

		List<TestDBProperties> settings = new ArrayList<TestDBProperties>();

		Properties props = TestProperties.getProperties();
		Set<String> ids = new HashSet<String>();

		for (Object key : props.keySet()) {
			String propName = (String) key;
			if (propName.startsWith(TESTDB_PROPERTY_PREFIX)) {
				String s = propName.substring(TESTDB_PROPERTY_PREFIX.length());
				int pos = s.indexOf('.');
				if (pos != -1) {
					String id = s.substring(0, pos);
					if (!ids.contains(id)) {
						LOG.info("Found test DB config '{}'", id);
						settings.add(new TestDBProperties(id, props));
						ids.add(id);
					}
				}
				else {
					LOG.error("Skipping test db configuration property {}. Unexpected format.", propName);
				}
			}
		}
		return settings;
	}

}
