/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.tile.persistence.filesystem;

import static org.deegree.tile.Tiles.calcTileEnvelope;

import java.io.File;
import java.util.List;

import org.deegree.geometry.Envelope;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.tile.persistence.TileStoreTransaction;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * {@link TileDataLevel} implementation for the {@link FileSystemTileStore}.
 *
 * @see DiskLayout
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
class FileSystemTileDataLevel implements TileDataLevel {

	private final TileMatrix metadata;

	private final DiskLayout layout;

	private String baseStoreId;

	private String baseDataSetId;

	private Workspace workspace;

	private ResourceMetadata<TileStore> tsMetadata;

	private String myId;

	/**
	 * Creates a new {@link FileSystemTileDataLevel} instance.
	 * @param metadata
	 * @param layout
	 */
	FileSystemTileDataLevel(TileMatrix metadata, DiskLayout layout, String baseStoreId, String baseDataSetId,
			Workspace workspace, ResourceMetadata<TileStore> tsMetadata, String myId) {
		this.metadata = metadata;
		this.layout = layout;
		this.baseStoreId = baseStoreId;
		this.baseDataSetId = baseDataSetId;
		this.workspace = workspace;
		this.tsMetadata = tsMetadata;
		this.myId = myId;
	}

	@Override
	public TileMatrix getMetadata() {
		return metadata;
	}

	private void checkBase(long x, long y, File file) {
		if (baseStoreId != null && !file.exists()) {
			file.getParentFile().mkdirs();
			TileStore store = workspace.getResource(TileStoreProvider.class, baseStoreId);
			TileDataSet set = store.getTileDataSet(baseDataSetId);
			TileDataLevel lev = set.getTileDataLevel(metadata.getIdentifier());
			Tile tile = lev.getTile(x, y);
			TileStoreTransaction ta = workspace.getResource(TileStoreProvider.class, tsMetadata.getIdentifier().getId())
				.acquireTransaction(myId);
			ta.put(metadata.getIdentifier(), tile, x, y);
		}
	}

	@Override
	public Tile getTile(long x, long y) {
		if (metadata.getNumTilesX() <= x || metadata.getNumTilesY() <= y || x < 0 || y < 0) {
			return null;
		}
		Envelope bbox = calcTileEnvelope(metadata, x, y);
		File file = layout.resolve(metadata.getIdentifier(), x, y);
		checkBase(x, y, file);
		return new FileSystemTile(bbox, file);
	}

	public DiskLayout getLayout() {
		return layout;
	}

	@Override
	public List<String> getStyles() {
		return null;
	}

}
