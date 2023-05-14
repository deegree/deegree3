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
package org.deegree.db;

import java.sql.Driver;
import java.util.ServiceLoader;

import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultResourceManager;
import org.deegree.workspace.standard.DefaultResourceManagerMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource manager for connection providers.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public class ConnectionProviderManager extends DefaultResourceManager<ConnectionProvider> {

	private static final Logger LOG = LoggerFactory.getLogger(ConnectionProviderManager.class);

	public ConnectionProviderManager() {
		super(new DefaultResourceManagerMetadata<ConnectionProvider>(ConnectionProviderProvider.class,
				"database connections", "jdbc"));
	}

	@Override
	public void startup(Workspace workspace) {
		// Check for legacy JDBC drivers and warn if some are found in modules directory
		ClassLoader moduleClassLoader = workspace.getModuleClassLoader();
		for (Driver d : ServiceLoader.load(Driver.class, moduleClassLoader)) {
			warnIfDriversAreRegisteredInModulesClassLoader(moduleClassLoader, d);
		}
		super.startup(workspace);
	}

	@Override
	public void shutdown() {
		// nothing to do
	}

	private void warnIfDriversAreRegisteredInModulesClassLoader(ClassLoader moduleClassLoader, Driver d) {
		final String clsName = d.getClass().getName();
		String clsFile = createClsFile(moduleClassLoader, clsName);

		LOG.warn("The JDBC driver {} has been found in the modules directory.", clsName);
		LOG.warn("This method of loading JDBC drivers is not supported in deegree any more.");
		LOG.warn("Please check the webservices handbook for more infomation.");
		if (clsFile != null) {
			LOG.warn("The jdbc driver has been found at {}", clsFile);
		}
	}

	private String createClsFile(ClassLoader moduleClassLoader, final String clsName) {
		String clsFile;
		try {
			clsFile = moduleClassLoader.getResource(clsName.replace('.', '/') + ".class").toString();
		}
		catch (Exception ign) {
			return null;
		}
		if (clsFile == null || clsFile.length() == 0)
			return null;
		int jarpos = clsFile.indexOf(".jar");
		if (jarpos != -1) {
			clsFile = clsFile.substring(0, jarpos + 4);
		}
		return clsFile;
	}

}
