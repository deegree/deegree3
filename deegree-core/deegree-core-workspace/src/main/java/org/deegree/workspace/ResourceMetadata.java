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

import java.util.Set;

/**
 * The resource metadata objects are the central objects, as they can be used to determine
 * dependency chains and are the originating object for creating resources.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public interface ResourceMetadata<T extends Resource> {

	/**
	 * @return the location, never <code>null</code>
	 */
	ResourceLocation<T> getLocation();

	/**
	 * Must be used to determine dependencies needed to build this resource. Must return a
	 * builder object that can be used to actually build a resource.
	 * @return the builder, never <code>null</code>
	 */
	ResourceBuilder<T> prepare();

	/**
	 * @return the identifier, never <code>null</code>
	 */
	ResourceIdentifier<T> getIdentifier();

	/**
	 * Initialization of the resource connected to this metadata will not be attempted
	 * unless all dependencies are available.
	 * @return a set of dependencies, may be empty but never <code>null</code>
	 */
	Set<ResourceIdentifier<? extends Resource>> getDependencies();

	/**
	 * Soft dependencies are dependencies that must be initialized before the resource
	 * connected to this metadata, but may also be missing.
	 * @return a set of dependencies, may be empty but never <code>null</code>
	 */
	Set<ResourceIdentifier<? extends Resource>> getSoftDependencies();

	/**
	 * @return the actual resource provider used by this resource, never <code>null</code>
	 */
	ResourceProvider<T> getProvider();

}
