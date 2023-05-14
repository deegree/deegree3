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
package org.deegree.layer.persistence.feature;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.LinkedHashMap;
import java.util.Map;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.types.FeatureType;
import org.deegree.layer.Layer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.MultipleLayerStore;
import org.deegree.layer.persistence.feature.jaxb.FeatureLayers.AutoLayers;
import org.deegree.style.persistence.StyleStore;
import org.deegree.style.persistence.StyleStoreProvider;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * Responsible for building feature layers from jaxb configs in auto mode.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class AutoFeatureLayerBuilder {

	private static final Logger LOG = getLogger(AutoFeatureLayerBuilder.class);

	private Workspace workspace;

	private ResourceMetadata<LayerStore> metadata;

	AutoFeatureLayerBuilder(Workspace workspace, ResourceMetadata<LayerStore> metadata) {
		this.workspace = workspace;
		this.metadata = metadata;
	}

	MultipleLayerStore createInAutoMode(AutoLayers auto) throws ResourceInitException {
		LOG.debug("Creating feature layers for all feature types automatically.");

		Map<String, Layer> map = new LinkedHashMap<String, Layer>();
		String id = auto.getFeatureStoreId();
		FeatureStore store = workspace.getResource(FeatureStoreProvider.class, id);
		if (store == null) {
			throw new ResourceInitException(
					"Feature layer config was invalid, feature store with id " + id + " is not available.");
		}
		id = auto.getStyleStoreId();
		StyleStore sstore = null;
		if (id != null) {
			sstore = workspace.getResource(StyleStoreProvider.class, id);
		}
		if (id != null && sstore == null) {
			throw new ResourceInitException(
					"Feature layer config was invalid, style store with id " + id + " is not available.");
		}

		for (FeatureType ft : store.getSchema().getFeatureTypes()) {
			addLayer(store, ft, sstore, map);
		}

		return new MultipleLayerStore(map, metadata);
	}

	private void addLayer(FeatureStore store, FeatureType ft, StyleStore sstore, Map<String, Layer> map) {
		String name = ft.getName().getLocalPart();
		LOG.debug("Adding layer {}.", name);
		LayerMetadata md = LayerMetadataBuilder.buildMetadataForAutoMode(store, ft, name);
		Map<String, Style> styles = new LinkedHashMap<String, Style>();
		if (sstore != null && sstore.getAll(name) != null) {
			for (Style s : sstore.getAll(name)) {
				LOG.debug("Adding style with name {}.", s.getName());
				styles.put(s.getName(), s);
				if (!styles.containsKey("default")) {
					styles.put("default", s);
				}
			}
		}
		if (!styles.containsKey("default")) {
			LOG.debug("No styles found, using gray default style.");
			styles.put("default", new Style());
		}
		md.setStyles(styles);
		Layer l = new FeatureLayer(md, store, ft.getName(), null, null, null);
		map.put(name, l);
	}

}
