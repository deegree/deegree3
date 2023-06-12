/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.workspace;

import java.util.List;

/**
 * Describes the operations that a workspace needs to be able to do on resource locations.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public interface LocationHandler {

	/**
	 * Adds a single extra resource. Can be used after workspace startup to include the
	 * new resource in the initialization process.
	 * @param location may not be <code>null</code>
	 */
	void addExtraResource(ResourceLocation<? extends Resource> location);

	/**
	 * Can be used to obtain a list of resource locations for a specific resource manager.
	 * @param metadata may not be <code>null</code>
	 * @return the locations, never <code>null</code>
	 */
	<T extends Resource> List<ResourceLocation<T>> findResourceLocations(ResourceManagerMetadata<T> metadata);

	/**
	 * Can be used to permanently store/update a resource location.
	 * @param location may not be <code>null</code>
	 * @return a new updated resource location, <code>null</code>, if the location could
	 * not be persisted
	 */
	<T extends Resource> ResourceLocation<T> persist(ResourceLocation<T> location);

	/**
	 * Can be used to permanently delete a resource location.
	 * @param location may not be <code>null</code>
	 */
	<T extends Resource> void delete(ResourceLocation<T> location);

	/**
	 * Activates a resource location so the workspace can find/initialize it.
	 * @param location may not be <code>null</code>
	 */
	<T extends Resource> void activate(ResourceLocation<T> location);

	/**
	 * Deactivates a resource location so the workspace ignores it.
	 * @param location may not be <code>null</code>
	 */
	<T extends Resource> void deactivate(ResourceLocation<T> location);

}
