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
package org.deegree.workspace;

import java.util.Collection;
import java.util.List;

/**
 * The resource managers are responsible for finding resources of a specific type.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public interface ResourceManager<T extends Resource> {

	/**
	 * Called during initialization to make the manager find its resources.
	 */
	void find();

	/**
	 * @return the manager's metadata, never <code>null</code>
	 */
	ResourceManagerMetadata<T> getMetadata();

	/**
	 * @return the resource metadata objects, never <code>null</code> after #find has been
	 * called
	 */
	Collection<ResourceMetadata<T>> getResourceMetadata();

	/**
	 * Called when workspace is searching for and preparing all resource managers for
	 * work.
	 * @param workspace never <code>null</code>
	 */
	void startup(Workspace workspace);

	/**
	 * Called when workspace is going down. Can be used to do preparatory work needed for
	 * all resources of this type.
	 */
	void shutdown();

	/**
	 * Adds a single new resource.
	 * @param location never <code>null</code>
	 * @return metadata for the new resource, never <code>null</code>
	 */
	ResourceMetadata<T> add(ResourceLocation<T> location);

	/**
	 * Removes a single resource.
	 * @param md resource metadata, must not be <code>null</code>
	 */
	void remove(ResourceMetadata<?> md);

	/**
	 * Returns a list of available resource providers after find has been called.
	 * @return never <code>null</code>
	 */
	List<ResourceProvider<T>> getProviders();

}
