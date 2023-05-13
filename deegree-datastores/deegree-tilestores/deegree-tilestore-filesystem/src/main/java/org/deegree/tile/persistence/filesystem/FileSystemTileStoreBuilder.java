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
package org.deegree.tile.persistence.filesystem;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.tile.DefaultTileDataSet;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.filesystem.jaxb.FileSystemTileStoreJAXB;
import org.deegree.tile.persistence.filesystem.layout.TileCacheDiskLayout;
import org.deegree.tile.tilematrixset.TileMatrixSetProvider;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * This class is responsible for building file system tile stores.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class FileSystemTileStoreBuilder implements ResourceBuilder<TileStore> {

	private static final Logger LOG = getLogger(FileSystemTileStoreBuilder.class);

	private FileSystemTileStoreJAXB config;

	private ResourceMetadata<TileStore> metadata;

	private Workspace workspace;

	public FileSystemTileStoreBuilder(FileSystemTileStoreJAXB config, ResourceMetadata<TileStore> metadata,
			Workspace workspace) {
		this.config = config;
		this.metadata = metadata;
		this.workspace = workspace;
	}

	@Override
	public TileStore build() {
		try {
			Map<String, TileDataSet> map = new HashMap<String, TileDataSet>();

			for (FileSystemTileStoreJAXB.TileDataSet tds : config.getTileDataSet()) {
				String id = tds.getIdentifier();
				String tmsId = tds.getTileMatrixSetId();
				org.deegree.tile.persistence.filesystem.jaxb.FileSystemTileStoreJAXB.TileDataSet.TileCacheDiskLayout lay = tds
					.getTileCacheDiskLayout();

				File baseDir = new File(lay.getLayerDirectory());
				if (!baseDir.isAbsolute()) {
					baseDir = metadata.getLocation().resolveToFile(lay.getLayerDirectory());
				}

				String baseStore = null;
				String baseDataSet = null;
				if (tds.getTileDataSetBase() != null) {
					baseStore = tds.getTileDataSetBase().getTileStoreId();
					baseDataSet = tds.getTileDataSetBase().getValue();
				}

				TileCacheDiskLayout layout = new TileCacheDiskLayout(baseDir, lay.getFileType());

				TileMatrixSet tms = workspace.getResource(TileMatrixSetProvider.class, tmsId);

				List<TileDataLevel> list = new ArrayList<TileDataLevel>(tms.getTileMatrices().size());

				for (TileMatrix tm : tms.getTileMatrices()) {
					list.add(new FileSystemTileDataLevel(tm, layout, baseStore, baseDataSet, workspace, metadata, id));
				}

				String format = "image/" + layout.getFileType();

				DefaultTileDataSet dataset = new DefaultTileDataSet(list, tms, format);
				layout.setTileMatrixSet(dataset);
				map.put(id, dataset);
			}

			return new FileSystemTileStore(map, metadata);
		}
		catch (Exception e) {
			String msg = "Unable to create FileSystemTileStore: " + e.getMessage();
			LOG.error(msg);
			throw new ResourceInitException(msg, e);
		}
	}

}
