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
package org.deegree.layer.persistence.feature;

import static org.slf4j.LoggerFactory.getLogger;

import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.feature.jaxb.FeatureLayers;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * This class is responsible for building feature layer stores.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class FeatureLayerStoreBuilder implements ResourceBuilder<LayerStore> {

	private static final Logger LOG = getLogger(FeatureLayerStoreBuilder.class);

	private FeatureLayers config;

	private ResourceMetadata<LayerStore> metadata;

	private Workspace workspace;

	public FeatureLayerStoreBuilder(FeatureLayers config, ResourceMetadata<LayerStore> metadata, Workspace workspace) {
		this.config = config;
		this.metadata = metadata;
		this.workspace = workspace;
	}

	@Override
	public LayerStore build() {
		try {
			if (config.getAutoLayers() != null) {
				AutoFeatureLayerBuilder builder = new AutoFeatureLayerBuilder(workspace, metadata);
				return builder.createInAutoMode(config.getAutoLayers());
			}

			LOG.debug("Creating configured feature layers only.");

			String id = config.getFeatureStoreId();
			FeatureStore store = workspace.getResource(FeatureStoreProvider.class, id);
			if (store == null) {
				throw new ResourceInitException(
						"Feature layer config was invalid, feature store with id " + id + " is not available.");
			}

			ManualFeatureLayerBuilder builder = new ManualFeatureLayerBuilder(config, metadata, store, workspace);
			return builder.buildFeatureLayers();
		}
		catch (Exception e) {
			throw new ResourceInitException("Could not parse layer configuration file: " + e.getLocalizedMessage(), e);
		}
	}

}
