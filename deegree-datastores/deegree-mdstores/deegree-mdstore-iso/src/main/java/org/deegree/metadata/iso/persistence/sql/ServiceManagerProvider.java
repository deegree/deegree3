/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.metadata.iso.persistence.sql;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.protocol.csw.MetadataStoreException;

/**
 * Provides access to the sql service manager.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class ServiceManagerProvider {

	private ServiceManager serviceManager;

	private static ServiceManagerProvider instance;

	private ServiceManagerProvider() throws MetadataStoreException {
		Iterator<ServiceManager> iter = ServiceLoader.load(ServiceManager.class).iterator();
		while (iter.hasNext()) {
			if (serviceManager != null) {
				String msg = "It is not allowed to specify more than one service manager. Please check "
						+ ServiceManager.class.getName();
				throw new MetadataStoreException(msg);
			}
			serviceManager = iter.next();
		}
		if (serviceManager == null) {
			serviceManager = new DefaultServiceManager();
		}
	}

	/**
	 * @return the {@link ServiceManager}, never <code>null</code>
	 */
	public ServiceManager getServiceManager() {
		return serviceManager;
	}

	/**
	 * @return the one and only instance of the {@link ServiceManagerProvider}, never
	 * <code>null</code>
	 * @throws ResourceInitException if more than one {@link ServiceManager} was found
	 */
	public synchronized static ServiceManagerProvider getInstance() throws MetadataStoreException {
		if (instance == null) {
			instance = new ServiceManagerProvider();
		}
		return instance;
	}

}