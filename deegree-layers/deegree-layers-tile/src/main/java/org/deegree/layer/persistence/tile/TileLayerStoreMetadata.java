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

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.tile.jaxb.TileLayerType;
import org.deegree.layer.persistence.tile.jaxb.TileLayers;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.deegree.workspace.standard.DefaultResourceIdentifier;

/**
 * Resource metadata implementation for tile layer stores.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class TileLayerStoreMetadata extends AbstractResourceMetadata<LayerStore> {

	public TileLayerStoreMetadata(Workspace workspace, ResourceLocation<LayerStore> location,
			AbstractResourceProvider<LayerStore> provider) {
		super(workspace, location, provider);
	}

	@Override
	public ResourceBuilder<LayerStore> prepare() {
		try {
			TileLayers cfg = (TileLayers) unmarshall("org.deegree.layer.persistence.tile.jaxb", provider.getSchema(),
					location.getAsStream(), workspace);

			for (TileLayerType lay : cfg.getTileLayer()) {
				for (TileLayerType.TileDataSet tds : lay.getTileDataSet()) {
					dependencies
						.add(new DefaultResourceIdentifier<TileStore>(TileStoreProvider.class, tds.getTileStoreId()));
				}
			}
			return new TileLayerStoreBuilder(cfg, this, workspace);
		}
		catch (Exception e) {
			throw new ResourceInitException("Unable to create tile layer store.", e);
		}
	}

}
