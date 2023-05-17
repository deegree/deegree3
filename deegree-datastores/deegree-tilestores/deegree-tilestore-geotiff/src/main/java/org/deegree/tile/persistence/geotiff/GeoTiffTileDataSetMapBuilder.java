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
package org.deegree.tile.persistence.geotiff;

import static javax.imageio.ImageIO.createImageInputStream;
import static javax.imageio.ImageIO.getImageReadersBySuffix;
import static org.deegree.tile.persistence.geotiff.GeoTiffUtils.getEnvelope;
import static org.slf4j.LoggerFactory.getLogger;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.deegree.geometry.Envelope;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.geotiff.jaxb.GeoTIFFTileStoreJAXB;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * Builds a tile data set map from jaxb config.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class GeoTiffTileDataSetMapBuilder {

	private static final Logger LOG = getLogger(GeoTiffTileDataSetMapBuilder.class);

	private GeoTIFFTileStoreJAXB cfg;

	private GeoTiffTileDataSetBuilder builder;

	private ResourceLocation<TileStore> location;

	GeoTiffTileDataSetMapBuilder(Workspace workspace, ResourceLocation<TileStore> location, GeoTIFFTileStoreJAXB cfg) {
		this.location = location;
		this.cfg = cfg;
		builder = new GeoTiffTileDataSetBuilder(workspace);
	}

	Map<String, TileDataSet> buildTileDataSetMap() throws IOException {
		Iterator<ImageReader> readers = getImageReadersBySuffix("tiff");
		ImageReader reader = null;
		while (readers.hasNext() && !(reader instanceof TIFFImageReader)) {
			reader = readers.next();
		}

		if (reader == null) {
			throw new ResourceInitException("No TIFF reader was found for imageio.");
		}

		Map<String, TileDataSet> map = new HashMap<String, TileDataSet>();
		for (GeoTIFFTileStoreJAXB.TileDataSet tds : cfg.getTileDataSet()) {
			String id = tds.getIdentifier();
			if (id == null) {
				id = new File(tds.getFile()).getName();
			}

			File file = location.resolveToFile(tds.getFile());

			if (!file.exists()) {
				LOG.warn("The file {} does not exist, skipping.", file);
				continue;
			}

			ImageInputStream iis = createImageInputStream(file);
			reader.setInput(iis, false, true);
			IIOMetadata md = reader.getImageMetadata(0);
			Envelope envelope = getEnvelope(md, reader.getWidth(0), reader.getHeight(0), null);

			if (envelope == null) {
				throw new ResourceInitException(
						"No envelope information could be read from GeoTIFF. " + "Please add one to the GeoTIFF.");
			}

			LOG.debug("Envelope from GeoTIFF was {}.", envelope);

			map.put(id, builder.buildTileDataSet(tds, location, envelope));
		}
		return map;
	}

}
