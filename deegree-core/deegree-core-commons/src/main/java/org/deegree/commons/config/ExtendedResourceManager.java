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
package org.deegree.commons.config;

import java.net.URL;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public interface ExtendedResourceManager<T extends Resource> extends ResourceManager {

	/**
	 * Initializes the metadata (can be used to scan for resource providers, important so
	 * transitive dependencies work).
	 * @param workspace
	 */
	void initMetadata(DeegreeWorkspace workspace);

	/**
	 * @return a metadata object for use in GUIs, may be null
	 */
	ResourceManagerMetadata<T> getMetadata();

	/**
	 * Is used to obtain a resource instance from a configuration url and register it. The
	 * creation is usually delegated to an appropriate {@link ResourceProvider}.
	 * @param id the desired id of the new resource
	 * @param configUrl the configuration url of the new resource
	 * @return the new resource instance
	 * @throws ResourceInitException if an error occurred while creating the resource
	 */
	T create(String id, URL configUrl) throws ResourceInitException;

	/**
	 * Is used to obtain a resource instance from an id.
	 * @param id
	 * @return null, if no such resource has been registered
	 */
	T get(String id);

}
