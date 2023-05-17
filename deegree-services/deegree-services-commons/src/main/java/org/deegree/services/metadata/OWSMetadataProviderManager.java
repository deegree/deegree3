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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.deegree.commons.config.ResourceManager;
import org.deegree.services.metadata.provider.OWSMetadataProviderProvider;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.standard.DefaultResourceManager;
import org.deegree.workspace.standard.DefaultResourceManagerMetadata;

/**
 * {@link ResourceManager} for {@link OWSMetadataProvider}s.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class OWSMetadataProviderManager extends DefaultResourceManager<OWSMetadataProvider> {

	public OWSMetadataProviderManager() {
		super(new DefaultResourceManagerMetadata<OWSMetadataProvider>(OWSMetadataProviderProvider.class,
				"service metadata", "services"));
	}

	@Override
	protected void read(List<ResourceLocation<OWSMetadataProvider>> list) {
		list = new ArrayList<ResourceLocation<OWSMetadataProvider>>(list);
		ListIterator<ResourceLocation<OWSMetadataProvider>> iter = list.listIterator();
		while (iter.hasNext()) {
			ResourceLocation<OWSMetadataProvider> loc = iter.next();
			if (!loc.getIdentifier().getId().endsWith("_metadata")) {
				iter.remove();
			}
		}
		super.read(list);
	}

	@Override
	public ResourceMetadata<OWSMetadataProvider> add(ResourceLocation<OWSMetadataProvider> location) {
		// else new locations will be filtered out
		super.read(Collections.singletonList(location));
		return metadataMap.get(location.getIdentifier());
	}

}
