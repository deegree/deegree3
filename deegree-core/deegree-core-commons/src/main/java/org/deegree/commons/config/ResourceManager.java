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

import java.io.InputStream;

import org.deegree.commons.config.ResourceState.StateType;

/**
 * Responsible for managing and creating a specific type of {@link Resource}s from
 * configuration documents.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public interface ResourceManager {

	/**
	 * Is called upon workspace startup.
	 */
	public void startup(DeegreeWorkspace workspace) throws ResourceInitException;

	/**
	 * Is called upon workspace shutdown.
	 */
	public void shutdown();

	/**
	 * @return an empty array if there are no dependencies
	 */
	public Class<? extends ResourceManager>[] getDependencies();

	/**
	 * @return a metadata object, may be null
	 */
	public ResourceManagerMetadata getMetadata();

	/**
	 * Returns the state of all resources.
	 * @return the states, never <code>null</code>
	 */
	public ResourceState<?>[] getStates();

	/**
	 * Returns the state of the resource.
	 * @param id resource identifier, must not be <code>null</code>
	 * @return the state or <code>null</code> (if the specified resource does not exist)
	 */
	public ResourceState<?> getState(String id);

	/**
	 * Activates the resource with the given identifier.
	 * @param id resource identifier, must not be <code>null</code>
	 * @return resource state after activation (may be unsuccessful), but never
	 * <code>null</code>
	 */
	public ResourceState<?> activate(String id);

	/**
	 * Deactivates the resource with the given identifier.
	 * @param id resource identifier, must not be <code>null</code>
	 * @return resource state after deactivation (may be unsuccessful), but never
	 * <code>null</code>
	 */
	public ResourceState<?> deactivate(String id);

	/**
	 * Creates a new {@link Resource} (which is initially in state
	 * {@link StateType#deactivated}).
	 * @param id resource identifier, must not be <code>null</code>
	 * @param config provides the configuration file content, must not be
	 * <code>null</code>
	 * @return resource state after creation ({@link StateType#deactivated}), never
	 * <code>null</code>
	 * @throws IllegalArgumentException if a resource with the specified identifier
	 * already exists
	 */
	public ResourceState<?> createResource(String id, InputStream config) throws IllegalArgumentException;

	/**
	 * Removes the specified resource and deletes the corresponding configuration file.
	 * @param id resource identifier, must not be <code>null</code>
	 * @return resource state after deletion, usually <code>null</code> (if not, deletion
	 * failed)
	 */
	public ResourceState<?> deleteResource(String id);

}