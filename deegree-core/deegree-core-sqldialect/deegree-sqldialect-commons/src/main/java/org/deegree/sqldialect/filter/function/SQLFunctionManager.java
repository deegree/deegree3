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
package org.deegree.sqldialect.filter.function;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.deegree.workspace.Destroyable;
import org.deegree.workspace.Initializable;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for accessing {@link SQLFunctionProvider} instances that are registered via
 * Java SPI.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class SQLFunctionManager implements Initializable, Destroyable {

	private static final Logger LOG = LoggerFactory.getLogger(SQLFunctionManager.class);

	private static Map<String, SQLFunctionProvider> nameToFunction;

	private static ServiceLoader<SQLFunctionProvider> functionLoader;

	/**
	 * Returns all available {@link SQLFunctionProvider}s. Multiple functions with the
	 * same case ignored name are not allowed.
	 * @return all available functions, keys: name, value: FunctionProvider
	 */
	public static synchronized Map<String, SQLFunctionProvider> getFunctionProviders() {
		if (nameToFunction == null) {
			nameToFunction = new HashMap<String, SQLFunctionProvider>();
			try {
				for (SQLFunctionProvider function : functionLoader) {
					LOG.debug("SQLFunction: " + function + ", name: " + function.getName());
					String name = function.getName().toLowerCase();
					if (nameToFunction.containsKey(name)) {
						LOG.error("Multiple SQLFunction instances for name: '" + name + "' on classpath -- omitting '"
								+ function.getClass().getName() + "'.");
						continue;
					}
					nameToFunction.put(name, function);
				}
			}
			catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return nameToFunction;
	}

	/**
	 * Returns the {@link SQLFunctionProvider} for the given name.
	 * @param name name of the function, must not be <code>null</code>
	 * @return custom function instance, or <code>null</code> if there is no function with
	 * this name
	 */
	public static SQLFunctionProvider getFunctionProvider(String name) {
		return getFunctionProviders().get(name.toLowerCase());
	}

	@Override
	public void destroy(Workspace workspace) {
		if (functionLoader == null) {
			return;
		}
		for (SQLFunctionProvider fp : functionLoader) {
			try {
				fp.destroy();
			}
			catch (Throwable t) {
				LOG.error("Destroying of SQLFunctionProvider " + fp.getName() + " failed: " + t.getMessage());
			}
		}
		functionLoader = null;
		if (nameToFunction != null) {
			nameToFunction.clear();
		}
		nameToFunction = null;
	}

	@Override
	public void init(Workspace ws) {
		functionLoader = ServiceLoader.load(SQLFunctionProvider.class, ws.getModuleClassLoader());
		for (SQLFunctionProvider fp : functionLoader) {
			try {
				fp.init(ws);
			}
			catch (Exception t) {
				LOG.error("Initialization of SQLFunctionProvider " + fp.getName() + " failed: " + t.getMessage());
			}
		}
	}

}
