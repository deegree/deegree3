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

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.deegree.geometry.Envelope;
import org.deegree.tile.DefaultTileDataSet;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreTransaction;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;
import org.slf4j.Logger;

/**
 * {@link TileStore} that wraps other {@link TileStore}s and merges their
 * {@link TileDataSet}s.
 *
 * @author <a href="mailto:Reijer.Copier@idgis.nl">Reijer Copier</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
class MergingTileStore implements TileStore {

	private static final Logger LOG = getLogger(MergingTileStore.class);

	private final static String FORMAT = "image/jpeg";

	private final ResourceMetadata<TileStore> metadata;

	private final TileMatrixSet tileMatrixSet;

	private final Map<String, Map<String, MergingTileDataLevel>> datasetIdToLevelIdToLevel = new TreeMap<String, Map<String, MergingTileDataLevel>>();

	private final Map<String, TileDataSet> datasetIdToDataset = new TreeMap<String, TileDataSet>();

	MergingTileStore(ResourceMetadata<TileStore> metadata, TileMatrixSet tileMatrixSet, List<TileStore> tileStores) {
		this.metadata = metadata;
		this.tileMatrixSet = tileMatrixSet;
		for (TileStore tileStore : tileStores) {
			for (String datasetId : tileStore.getTileDataSetIds()) {
				Map<String, MergingTileDataLevel> levelIdToLevel = datasetIdToLevelIdToLevel.get(datasetId);
				if (levelIdToLevel == null) {
					levelIdToLevel = new HashMap<String, MergingTileDataLevel>();
					datasetIdToLevelIdToLevel.put(datasetId, levelIdToLevel);
				}
				TileDataSet dataset = tileStore.getTileDataSet(datasetId);
				addLevels(levelIdToLevel, dataset);
			}
		}
		addDatasets(datasetIdToLevelIdToLevel);
	}

	private void addLevels(Map<String, MergingTileDataLevel> levelIdToLevel, TileDataSet dataset) {
		for (TileDataLevel level : dataset.getTileDataLevels()) {
			String levelId = level.getMetadata().getIdentifier();
			MergingTileDataLevel mergingLevel = levelIdToLevel.get(levelId);
			if (mergingLevel == null) {
				mergingLevel = new MergingTileDataLevel(level.getMetadata());
				levelIdToLevel.put(levelId, mergingLevel);
			}
			mergingLevel.addMergeLevel(level);
		}
	}

	private void addDatasets(Map<String, Map<String, MergingTileDataLevel>> datasetIdToLevelIdToLevel) {
		for (String datasetId : datasetIdToLevelIdToLevel.keySet()) {
			LOG.info("- Dataset: " + datasetId);
			Map<String, MergingTileDataLevel> levelIdToLevel = datasetIdToLevelIdToLevel.get(datasetId);
			addDataset(datasetId, levelIdToLevel);
		}
	}

	private void addDataset(String datasetId, Map<String, MergingTileDataLevel> LevelIdToLevel) {
		Map<String, MergingTileDataLevel> levelIdToLevel = datasetIdToLevelIdToLevel.get(datasetId);
		List<TileDataLevel> levels = new ArrayList<TileDataLevel>(levelIdToLevel.values());
		for (TileDataLevel level : levels) {
			List<TileDataLevel> mergeLevels = ((MergingTileDataLevel) level).getMergeLevels();
			LOG.info(" - Level: " + level.getMetadata().getIdentifier() + ", merge size: " + mergeLevels.size());
		}
		TileDataSet dataset = new DefaultTileDataSet(levels, tileMatrixSet, FORMAT);
		datasetIdToDataset.put(datasetId, dataset);
	}

	@Override
	public void init() {
	}

	@Override
	public void destroy() {

	}

	@Override
	public ResourceMetadata<? extends Resource> getMetadata() {
		return metadata;
	}

	@Override
	public TileStoreTransaction acquireTransaction(String id) {
		throw new UnsupportedOperationException("MergingTileStore does not support transactions.");
	}

	@Override
	public Tile getTile(String datasetId, String levelId, int x, int y) {
		return datasetIdToDataset.get(datasetId).getTileDataLevel(levelId).getTile(x, y);
	}

	@Override
	public TileDataSet getTileDataSet(String datasetId) {
		return datasetIdToDataset.get(datasetId);
	}

	@Override
	public Collection<String> getTileDataSetIds() {
		return datasetIdToDataset.keySet();
	}

	@Override
	public Iterator<Tile> getTiles(String datasetId, Envelope envelope, double resolution) {
		return getTileDataSet(datasetId).getTiles(envelope, resolution);
	}

}
