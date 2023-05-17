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
package org.deegree.tile.persistence.gdal;

import static org.gdal.gdalconst.gdalconstConstants.GA_ReadOnly;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.gdal.jaxb.GdalTileStoreJaxb;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.slf4j.Logger;

/**
 * Builds a tile data set map from jaxb config.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class GdalTileDataSetMapBuilder {

	private static final Logger LOG = getLogger(GdalTileDataSetMapBuilder.class);

	private final GdalTileStoreJaxb cfg;

	private final GdalTileDataSetBuilder builder;

	private ResourceLocation<TileStore> location;

	GdalTileDataSetMapBuilder(Workspace workspace, ResourceLocation<TileStore> location, GdalTileStoreJaxb cfg) {
		this.location = location;
		this.cfg = cfg;
		builder = new GdalTileDataSetBuilder(workspace);
	}

	Map<String, TileDataSet> buildTileDataSetMap() throws IOException, UnknownCRSException {

		Map<String, TileDataSet> map = new HashMap<String, TileDataSet>();
		for (GdalTileStoreJaxb.TileDataSet tds : cfg.getTileDataSet()) {
			String id = tds.getIdentifier();
			if (id == null) {
				id = new File(tds.getFile()).getName();
			}
			File file = location.resolveToFile(tds.getFile());
			if (!file.exists()) {
				LOG.warn("File {} does not exist, skipping.", file);
				continue;
			}
			Dataset gdalDataset = gdal.OpenShared(file.toString());
			try {
				// TODO
				Envelope env = GdalUtils.getEnvelopeAndCrs(gdalDataset, "EPSG:28992").getEnvelope();
				if (env == null) {
					throw new ResourceInitException("No envelope information could be read via GDAL.");
				}
				LOG.debug("Envelope from GDAL was {}.", env);
				map.put(id, builder.buildTileDataSet(tds, location, env));
			}
			finally {
				gdalDataset.delete();
			}
		}
		return map;
	}

}
