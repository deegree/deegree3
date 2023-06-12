/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.coverage.raster.data.container;

import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.container.RasterDataContainerFactory.LoadingPolicy;
import org.deegree.coverage.raster.io.RasterDataReader;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * This class implements a cached RasterDataContainer.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 */
public class CachedRasterDataContainer implements RasterDataContainer, RasterDataContainerProvider {

	private final static Logger LOG = LoggerFactory.getLogger(CachedRasterDataContainer.class);

	private RasterDataReader reader;

	private String identifier;

	private static Cache<String, RasterData> cache;

	private final static String CACHENAME = "CachedRasterDataContainer";

	private static final StatisticsRetrieval statsRetrievalService = new StatisticsRetrieval();

	static {
		try {
			// TODO: make cachename configurable
			// see ehcache.xml for CachedRasterDataContainer configuration
			CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.using(statsRetrievalService)
				.build();
			cacheManager.init();
			cache = cacheManager.createCache(CACHENAME,
					CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, RasterData.class,
							ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(1, MemoryUnit.GB)));
		}
		catch (Throwable e) {
			LOG.error(e.getLocalizedMessage(), e);
		}

	}

	/**
	 * Creates an empty RasterDataContainer that loads the data on first access.
	 */
	public CachedRasterDataContainer() {
		// empty constructor
	}

	/**
	 * Creates a RasterDataContainer that loads the data on first access.
	 * @param reader RasterReader for the raster source
	 */
	public CachedRasterDataContainer(RasterDataReader reader) {
		setRasterDataReader(reader);
	}

	public synchronized void setRasterDataReader(RasterDataReader reader) {
		// reader.close();
		this.reader = reader;
		this.identifier = UUID.randomUUID().toString();
	}

	@Override
	public synchronized RasterData getRasterData() {
		// synchronized to prevent multiple reader.read()-calls when
		if (LOG.isDebugEnabled()) {
			LOG.debug("accessing: " + this);
		}
		if (!cache.containsKey(identifier)) {
			RasterData raster = reader.read();
			cache.put(identifier, raster);
			if (LOG.isDebugEnabled()) {
				long occupiedByteSize = statsRetrievalService.getStatisticsService()
					.getCacheStatistics(CACHENAME)
					.getTierStatistics()
					.get("OffHeap")
					.getOccupiedByteSize();
				LOG.debug("cache miss: " + this + " #mem: " + occupiedByteSize);
			}
			return raster;
		}
		else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("cache hit: " + this);
			}
			return cache.get(identifier);
		}
	}

	@Override
	public RasterData getReadOnlyRasterData() {
		return getRasterData().asReadOnly();
	}

	public RasterDataContainer getRasterDataContainer(LoadingPolicy type) {
		if (type == LoadingPolicy.CACHED && cache != null) {
			// the service loader caches provider instances, so return a new instance
			return new CachedRasterDataContainer();
		}
		return null;
	}

}
