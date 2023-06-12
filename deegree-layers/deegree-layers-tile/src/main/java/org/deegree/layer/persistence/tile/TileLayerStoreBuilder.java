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
package org.deegree.layer.persistence.tile;

import java.util.HashMap;
import java.util.Map;

import org.deegree.layer.Layer;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.MultipleLayerStore;
import org.deegree.layer.persistence.tile.jaxb.TileLayerType;
import org.deegree.layer.persistence.tile.jaxb.TileLayers;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * This class is responsible for building tile layer stores.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class TileLayerStoreBuilder implements ResourceBuilder<LayerStore> {

	private TileLayers cfg;

	private ResourceMetadata<LayerStore> metadata;

	private Workspace workspace;

	public TileLayerStoreBuilder(TileLayers cfg, ResourceMetadata<LayerStore> metadata, Workspace workspace) {
		this.cfg = cfg;
		this.metadata = metadata;
		this.workspace = workspace;
	}

	@Override
	public LayerStore build() {
		Map<String, Layer> map = new HashMap<String, Layer>();
		TileLayerBuilder builder = new TileLayerBuilder(workspace);
		for (TileLayerType lay : cfg.getTileLayer()) {
			TileLayer l = builder.createLayer(lay);
			map.put(l.getMetadata().getName(), l);
		}
		return new MultipleLayerStore(map, metadata);
	}

}
