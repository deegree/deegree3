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
package org.deegree.commons.config;

import java.io.File;

/**
 * Encapsulates information on the state of a managed {@link Resource} (and provides
 * access to it).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class ResourceState<T extends Resource> {

	/**
	 * Represents the lifecycle phases of a {@link Resource}.
	 *
	 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
	 */
	public static enum StateType {

		/** Resource is deactivated */
		deactivated,
		/** Resource has been created, but not initialized yet */
		created,
		/** Resource has been successfully initialized */
		init_ok,
		/** Error occured during initialization */
		init_error,
		/** Resource has been destroyed */
		destroyed

	}

	private final StateType type;

	private final ResourceInitException lastException;

	private final String id;

	private final File configLocation;

	private final T resource;

	private final ResourceProvider provider;

	/**
	 * Creates a new {@link ResourceState} instance.
	 * @param id resource identifier, never <code>null</code>
	 * @param configLocation config file location, can be <code>null</code>
	 * @param type state type, must not be <code>null</code>
	 * @param resource actual resource object, can be <code>null</code>
	 * @param lastException exception that occurred during the last state change of the
	 * resource, can be <code>null</code> (no exception)
	 */
	public ResourceState(String id, File configLocation, ResourceProvider provider, StateType type, T resource,
			ResourceInitException lastException) {
		this.id = id;
		this.configLocation = configLocation;
		this.provider = provider;
		this.type = type;
		this.resource = resource;
		this.lastException = lastException;
	}

	/**
	 * Returns an identifier that uniquely identifies the resource among all resources
	 * that are managed by the corresponding {@link ResourceManager}.
	 * @return identifier of the resource, never <code>null</code>
	 */
	public String getId() {
		return id;
	}

	public File getConfigLocation() {
		return configLocation;
	}

	/**
	 * Returns the {@link ResourceProvider} responsible for this resource.
	 * @return resource provider, can be <code>null</code> (no suitable resource provider
	 * available)
	 */
	public ResourceProvider getProvider() {
		return provider;
	}

	/**
	 * Returns the state type.
	 * @return the state type, never <code>null</code>
	 */
	public StateType getType() {
		return type;
	}

	/**
	 * Returns the actual resource.
	 * @return resource, can be <code>null</code>
	 */
	public T getResource() {
		return resource;
	}

	/**
	 * Returns the exception that occurred during the last state change of the resource.
	 * @return the last exception, can be <code>null</code>
	 */
	public ResourceInitException getLastException() {
		return lastException;
	}

	@Override
	public String toString() {
		return "ResourceState: " + id + " (" + type + ")";
	}

}