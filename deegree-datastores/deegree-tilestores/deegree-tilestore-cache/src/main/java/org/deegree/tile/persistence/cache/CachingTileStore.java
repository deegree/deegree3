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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.cache;

import org.deegree.geometry.Envelope;
import org.deegree.tile.DefaultTileDataSet;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.Tiles;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreTransaction;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * {@link TileStore} that acts as a caching proxy to another {@link TileStore}.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class CachingTileStore implements TileStore {

	private final TileStore tileStore;

	private final CacheManager cacheManager;

	private final Cache<String, byte[]> cache;

	private Map<String, TileDataSet> tileMatrixSets;

	private final ResourceMetadata<TileStore> metadata;

	public CachingTileStore(TileStore tileStore, String cacheName, URL cacheConfiguration,
			ResourceMetadata<TileStore> metadata) {
		this.tileStore = tileStore;
		this.metadata = metadata;
		Configuration xmlConfig = new XmlConfiguration(cacheConfiguration);
		this.cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
		this.cacheManager.init();
		this.cache = this.cacheManager.getCache(cacheName, String.class, byte[].class);
	}

	@Override
	public void init() {
		Collection<String> ids = tileStore.getTileDataSetIds();
		tileMatrixSets = new HashMap<>();
		for (String id : ids) {
			TileDataSet cachedDataset = tileStore.getTileDataSet(id);
			List<TileDataLevel> list = new ArrayList<>();
			for (TileDataLevel tm : cachedDataset.getTileDataLevels()) {
				list.add(new CachingTileMatrix(tm, cache));
			}
			TileDataSet cachingDataset = new DefaultTileDataSet(list, cachedDataset.getTileMatrixSet(),
					cachedDataset.getNativeImageFormat());
			this.tileMatrixSets.put(id, cachingDataset);
		}
	}

	@Override
	public Collection<String> getTileDataSetIds() {
		return tileMatrixSets.keySet();
	}

	@Override
	public void destroy() {
		cacheManager.close();
	}

	@Override
	public TileDataSet getTileDataSet(String id) {
		return tileMatrixSets.get(id);
	}

	@Override
	public Iterator<Tile> getTiles(String id, Envelope envelope, double resolution) {
		return tileMatrixSets.get(id).getTiles(envelope, resolution);
	}

	@Override
	public Tile getTile(String tileMatrixSet, String tileMatrix, int x, int y) {
		TileDataLevel tm = tileMatrixSets.get(tileMatrixSet).getTileDataLevel(tileMatrix);
		if (tm == null) {
			return null;
		}
		return tm.getTile(x, y);
	}

	/**
	 * Removes matching objects from cache.
	 * @param tileMatrixSet the id of the tile matrix set
	 * @param envelope may be null, in which case all objects will be removed from the
	 * cache
	 */
	public long invalidateCache(String tileMatrixSet, Envelope envelope) {
		if (envelope == null) {
			long count = StreamSupport.stream(cache.spliterator(), false).count();
			cache.clear();
			return count;
		}
		long cnt = 0;
		for (TileDataLevel tm : tileMatrixSets.get(tileMatrixSet).getTileDataLevels()) {
			long[] ts = Tiles.getTileIndexRange(tm, envelope);
			if (ts != null) {
				String id = tm.getMetadata().getIdentifier();
				for (long x = ts[0]; x <= ts[2]; ++x) {
					for (long y = ts[1]; y <= ts[3]; ++y) {
						String key = id + "_" + x + "_" + y;
						if (cache.containsKey(key)) {
							cache.remove(key);
							++cnt;
						}
					}
				}
			}
		}
		return cnt;
	}

	@Override
	public TileStoreTransaction acquireTransaction(String id) {
		throw new UnsupportedOperationException("CachingTileStore does not support transactions.");
	}

	@Override
	public ResourceMetadata<? extends Resource> getMetadata() {
		return metadata;
	}

}
