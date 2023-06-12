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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.deegree.geometry.Envelope;
import org.deegree.tile.DefaultTileDataSet;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.geotiff.jaxb.GeoTIFFTileStoreJAXB;
import org.deegree.tile.tilematrixset.TileMatrixSetProvider;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;

/**
 * Builds tile data sets from jaxb config beans.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class GeoTiffTileDataSetBuilder {

	private Workspace workspace;

	GeoTiffTileDataSetBuilder(Workspace workspace) {
		this.workspace = workspace;
	}

	TileDataSet buildTileDataSet(GeoTIFFTileStoreJAXB.TileDataSet cfg, ResourceLocation<TileStore> location,
			Envelope envelope) {
		String filename = cfg.getFile();
		String format = cfg.getImageFormat();
		String tmsId = cfg.getTileMatrixSetId();
		int maxActive = getMaxActive(cfg);

		File file = location.resolveToFile(filename);

		TileMatrixSet tms = workspace.getResource(TileMatrixSetProvider.class, tmsId);

		List<TileDataLevel> levels = new ArrayList<TileDataLevel>();
		double x = envelope.getMin().get0() - tms.getSpatialMetadata().getEnvelope().getMin().get0();
		double y = envelope.getMax().get1() - tms.getSpatialMetadata().getEnvelope().getMax().get1();

		int idx = 0;
		for (TileMatrix tm : tms.getTileMatrices()) {
			int xoff = (int) Math.round(x / tm.getTileWidth());
			int yoff = (int) Math.round(y / tm.getTileHeight());
			int numx = (int) Math.ceil(envelope.getSpan0() / tm.getTileWidth());
			int numy = (int) Math.ceil(envelope.getSpan1() / tm.getTileHeight());
			levels.add(new GeoTIFFTileDataLevel(tm, file, idx++, xoff, yoff, numx, numy, maxActive));
		}

		return new DefaultTileDataSet(levels, tms, format);
	}

	private int getMaxActive(GeoTIFFTileStoreJAXB.TileDataSet cfg) {
		if (cfg.getAccessConfig() != null && cfg.getAccessConfig().getMaxActive() != null)
			return cfg.getAccessConfig().getMaxActive().intValue();
		return 8;
	}

}
