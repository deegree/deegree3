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
package org.deegree.tile.persistence;

import java.util.List;
import java.util.ListIterator;

import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.standard.DefaultResourceManager;
import org.deegree.workspace.standard.DefaultResourceManagerMetadata;

/**
 * Resource manager for tile stores.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class TileStoreManager extends DefaultResourceManager<TileStore> {

	public TileStoreManager() {
		super(new DefaultResourceManagerMetadata<TileStore>(TileStoreProvider.class, "tile stores",
				"datasources/tile/"));
	}

	@Override
	protected void read(List<ResourceLocation<TileStore>> list) {
		ListIterator<ResourceLocation<TileStore>> iter = list.listIterator();
		while (iter.hasNext()) {
			ResourceLocation<TileStore> loc = iter.next();
			if (loc.getIdentifier().getId().startsWith("tilematrixset")) {
				iter.remove();
			}
		}
		super.read(list);
	}

}
