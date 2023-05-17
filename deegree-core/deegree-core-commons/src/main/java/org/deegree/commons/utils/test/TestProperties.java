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
package org.deegree.commons.utils.test;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates access to the global test configuration file
 * <code>${user.home}/.deegree-test.properties</code>.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class TestProperties {

	private static Logger LOG = LoggerFactory.getLogger(TestProperties.class);

	private static final Properties props = new Properties();

	private static final String DEEGREE_TEST_PROPERTIES = ".deegree-test.properties";

	static {
		String userHome = System.getProperty("user.home");
		File file = new File(userHome, DEEGREE_TEST_PROPERTIES);
		if (file.exists()) {
			LOG.info("Reading test properties from file {}.", file);
			try {
				props.load(new FileReader(file));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			LOG.info("File {} does not exist. Some tests may be skipped.", file);
		}
	}

	/**
	 * Returns the properties from <code>${user.home}/.deegree-test.properties</code>.
	 * @return the properties, can be empty, but never <code>null</code>
	 */
	public static Properties getProperties() {
		return props;
	}

	public static String getProperty(String key) {
		return props.getProperty(key);
	}

}
