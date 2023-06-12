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

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.tile.tilematrixset.TileMatrixSetProvider;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.DefaultResourceIdentifier;

/**
 * {@link ResourceMetadata} for {@link MergingTileStore}.
 *
 * @author <a href="mailto:Reijer.Copier@idgis.nl">Reijer Copier</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
class MergingTileStoreMetadata extends AbstractResourceMetadata<TileStore> {

	private static final String JAXB_NAMESPACE = "org.deegree.tile.persistence.merge.jaxb";

	/**
	 * Creates a new {@link MergingTileStoreMetadata} instance.
	 * @param ws workspace, must not be <code>null</code>
	 * @param location resource location, must not be <code>null</code>
	 * @param provider provider, must not be <code>null</code>
	 */
	MergingTileStoreMetadata(Workspace ws, ResourceLocation<TileStore> location, MergingTileStoreProvider provider) {
		super(ws, location, provider);
	}

	@Override
	public ResourceBuilder<TileStore> prepare() {
		try {
			org.deegree.tile.persistence.merge.jaxb.MergingTileStore cfg;
			cfg = (org.deegree.tile.persistence.merge.jaxb.MergingTileStore) unmarshall(JAXB_NAMESPACE,
					provider.getSchema(), location.getAsStream(), workspace);
			dependencies.add(new DefaultResourceIdentifier<TileMatrixSet>(TileMatrixSetProvider.class,
					cfg.getTileMatrixSetId()));
			for (String tileStoreId : cfg.getTileStoreId()) {
				dependencies.add(new DefaultResourceIdentifier<TileStore>(TileStoreProvider.class, tileStoreId));
			}
			return new MergingTileStoreBuilder(cfg, this, workspace);
		}
		catch (Exception e) {
			throw new ResourceInitException("Could not prepare MergingTileStore: " + e.getLocalizedMessage(), e);
		}
	}

}
