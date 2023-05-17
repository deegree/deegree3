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
package org.deegree.workspace.standard;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.ResourceProvider;
import org.deegree.workspace.Workspace;

/**
 * Abstract resource provider implementation for schema based providers.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public abstract class AbstractResourceProvider<T extends Resource> implements ResourceProvider<T> {

	@Override
	public ResourceMetadata<T> read(Workspace workspace, ResourceLocation<T> location) {
		if (!getNamespace().equals(location.getNamespace())) {
			return null;
		}
		return createFromLocation(workspace, location);
	}

	public abstract ResourceMetadata<T> createFromLocation(Workspace workspace, ResourceLocation<T> location);

	public abstract URL getSchema();

	@Override
	public List<ResourceMetadata<T>> getAdditionalResources(Workspace workspace) {
		return new ArrayList<ResourceMetadata<T>>();
	}

}
