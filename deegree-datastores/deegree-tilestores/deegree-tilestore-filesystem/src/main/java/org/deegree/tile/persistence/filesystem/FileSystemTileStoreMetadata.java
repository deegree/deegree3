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

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.slf4j.LoggerFactory.getLogger;

import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.tile.persistence.filesystem.jaxb.FileSystemTileStoreJAXB;
import org.deegree.tile.persistence.filesystem.jaxb.FileSystemTileStoreJAXB.TileDataSet.TileDataSetBase;
import org.deegree.tile.tilematrixset.TileMatrixSetProvider;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.slf4j.Logger;

/**
 * Resource metadata implementation for file system tile stores.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class FileSystemTileStoreMetadata extends AbstractResourceMetadata<TileStore> {

	private static final Logger LOG = getLogger(FileSystemTileStoreMetadata.class);

	private static final String JAXB_PACKAGE = "org.deegree.tile.persistence.filesystem.jaxb";

	public FileSystemTileStoreMetadata(Workspace workspace, ResourceLocation<TileStore> location,
			AbstractResourceProvider<TileStore> provider) {
		super(workspace, location, provider);
	}

	@Override
	public ResourceBuilder<TileStore> prepare() {
		try {

			FileSystemTileStoreJAXB config = (FileSystemTileStoreJAXB) unmarshall(JAXB_PACKAGE, provider.getSchema(),
					location.getAsStream(), workspace);

			for (FileSystemTileStoreJAXB.TileDataSet tds : config.getTileDataSet()) {
				String tmsId = tds.getTileMatrixSetId();
				dependencies.add(new DefaultResourceIdentifier<TileMatrixSet>(TileMatrixSetProvider.class, tmsId));
				TileDataSetBase base = tds.getTileDataSetBase();
				if (base != null) {
					dependencies
						.add(new DefaultResourceIdentifier<TileStore>(TileStoreProvider.class, base.getTileStoreId()));
				}
			}

			return new FileSystemTileStoreBuilder(config, this, workspace);
		}
		catch (Exception e) {
			String msg = "Unable to prepare FileSystemTileStore: " + e.getMessage();
			LOG.error(msg);
			throw new ResourceInitException(msg, e);
		}
	}

}
