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
package org.deegree.feature.persistence;

import org.deegree.feature.persistence.cache.BBoxCache;
import org.deegree.feature.persistence.cache.BBoxPropertiesCache;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultResourceManager;
import org.deegree.workspace.standard.DefaultResourceManagerMetadata;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for finding feature store resources.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class FeatureStoreManager extends DefaultResourceManager<FeatureStore> {

	private static Logger LOG = LoggerFactory.getLogger(FeatureStoreManager.class);

	private static final String BBOX_CACHE_FILE = "bbox_cache.properties";

	private static final String BBOX_CACHE_FEATURESTOE_FILE = "bbox_cache_%s.properties";

	private BBoxPropertiesCache bboxCache;

	private final Map<String, BBoxPropertiesCache> customBboxCaches = new HashMap<>();

	private Workspace workspace;

	public FeatureStoreManager() {
		super(new DefaultResourceManagerMetadata<>(FeatureStoreProvider.class, "feature stores",
				"datasources/feature"));
	}

	@Override
	public void startup(Workspace workspace) {
		this.workspace = workspace;
		super.startup(workspace);
	}

	/**
	 * Returns the bbox_cache.properties file (which is created if not existing). As there
	 * may be feature store specific bbox_cache_FEATURESTOE_ID.properties file the method
	 * getBBoxCache( String featureStoreId ) should be used.
	 */
	public BBoxCache getBBoxCache() {
		return getOrCreateBBoxCache();
	}

	/**
	 * Returns the feature store specific bbox_cache_FEATURESTOE_ID.properties if
	 * existing, if not the bbox_cache.properties file is returned (which is created if
	 * not existing).
	 * @param featureStoreId
	 * @return
	 */
	public BBoxCache getBBoxCache(String featureStoreId) {
		if (customBboxCaches.containsValue(featureStoreId)) {
			return customBboxCaches.get(featureStoreId);
		}
		BBoxPropertiesCache customBBoxCache = getCustomBBoxCache(featureStoreId);
		if (customBBoxCache != null) {
			customBboxCaches.put(featureStoreId, customBBoxCache);
			return customBBoxCache;
		}
		return getOrCreateBBoxCache();
	}

	private BBoxPropertiesCache getOrCreateBBoxCache() {
		try {
			if (bboxCache == null) {
				File dir = new File(((DefaultWorkspace) workspace).getLocation(), getMetadata().getWorkspacePath());
				File propsFile = new File(dir, BBOX_CACHE_FILE);
				bboxCache = new BBoxPropertiesCache(propsFile);
			}
		}
		catch (IOException e) {
			LOG.error("Unable to initialize envelope cache {}: {}", BBOX_CACHE_FILE, e.getMessage());
			LOG.trace(e.getMessage(), e);
		}
		return bboxCache;
	}

	private BBoxPropertiesCache getCustomBBoxCache(String featureStoreId) {
		try {
			if (workspace instanceof DefaultWorkspace) {
				File dir = new File(((DefaultWorkspace) workspace).getLocation(), getMetadata().getWorkspacePath());
				File propsFile = new File(dir, String.format(BBOX_CACHE_FEATURESTOE_FILE, featureStoreId));
				if (propsFile.exists()) {
					return new BBoxPropertiesCache(propsFile);
				}
			}
		}
		catch (IOException e) {
			LOG.error("Unable to initialize envelope cache for feature store with id {}: {}", featureStoreId,
					e.getMessage());
			LOG.trace(e.getMessage(), e);
		}
		return null;
	}

}
