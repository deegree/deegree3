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

/**
 * Uniquely identifies a resource in the workspace. Needs to be comparable to other
 * identifiers.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public interface ResourceIdentifier<T extends Resource> extends Comparable<ResourceIdentifier<T>> {

	/**
	 * @return the 'local' part of the id, typically the base name of the configuration
	 * file. Never <code>null</code>
	 */
	String getId();

	/**
	 * This must be the base provider class for the type of resource, not the actual
	 * implementation.
	 * @return the provider class qualifying the resource, never <code>null</code>
	 */
	Class<? extends ResourceProvider<T>> getProvider();

}
