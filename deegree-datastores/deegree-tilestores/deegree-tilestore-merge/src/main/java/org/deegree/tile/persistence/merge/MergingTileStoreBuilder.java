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
package org.deegree.tile.persistence.merge;

import java.util.ArrayList;
import java.util.List;

import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.tile.tilematrixset.TileMatrixSetProvider;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * {@link ResourceBuilder} for {@link MergingTileStore}.
 *
 * @author <a href="mailto:Reijer.Copier@idgis.nl">Reijer Copier</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
class MergingTileStoreBuilder implements ResourceBuilder<TileStore> {

	private final org.deegree.tile.persistence.merge.jaxb.MergingTileStore cfg;

	private final ResourceMetadata<TileStore> metadata;

	private final Workspace workspace;

	MergingTileStoreBuilder(org.deegree.tile.persistence.merge.jaxb.MergingTileStore cfg,
			ResourceMetadata<TileStore> metadata, Workspace workspace) {
		this.cfg = cfg;
		this.metadata = metadata;
		this.workspace = workspace;
	}

	@Override
	public TileStore build() throws ResourceInitException {
		List<TileStore> tileStores = new ArrayList<TileStore>();
		TileMatrixSet tileMatrix = workspace.getResource(TileMatrixSetProvider.class, cfg.getTileMatrixSetId());
		for (String tileStoreId : cfg.getTileStoreId()) {
			TileStore tileStore = workspace.getResource(TileStoreProvider.class, tileStoreId);
			if (tileStore == null) {
				throw new ResourceInitException(
						"Cannot build MergingTileStore: No tile store with id '" + tileStoreId + "' in workspace.");
			}
			tileStores.add(tileStore);
		}
		return new MergingTileStore(metadata, tileMatrix, tileStores);
	}

}
