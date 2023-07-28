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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Can be used to cache resource builders while operating on a workspace. Usually used by
 * the workspace internally.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class PreparedResources {

	private Workspace workspace;

	private Map<ResourceMetadata<? extends Resource>, ResourceBuilder<? extends Resource>> map;

	public PreparedResources(Workspace workspace) {
		this.workspace = workspace;
		map = new HashMap<ResourceMetadata<?>, ResourceBuilder<?>>();
	}

	public <T extends Resource> ResourceBuilder<T> getBuilder(ResourceIdentifier<T> id) {
		ResourceBuilder<T> builder = (ResourceBuilder<T>) map
			.get(workspace.getResourceMetadata(id.getProvider(), id.getId()));
		if (builder == null) {
			builder = workspace.prepare(id);
		}
		return builder;
	}

	public <T extends Resource> void addBuilder(ResourceIdentifier<T> id, ResourceBuilder<T> builder) {
		map.put(workspace.getResourceMetadata(id.getProvider(), id.getId()), builder);
	}

	public Set<ResourceMetadata<? extends Resource>> getMetadata() {
		return map.keySet();
	}

	public boolean hasBuilder(ResourceMetadata<? extends Resource> md) {
		return map.containsKey(md);
	}

}
