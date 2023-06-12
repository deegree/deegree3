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
package org.deegree.tile.persistence.gdal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.gdal.GdalSettings;
import org.deegree.geometry.Envelope;
import org.deegree.tile.DefaultTileDataSet;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.gdal.jaxb.GdalTileStoreJaxb;
import org.deegree.tile.tilematrixset.TileMatrixSetProvider;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;

/**
 * Builds tile data sets from jaxb config beans.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
class GdalTileDataSetBuilder {

	private final Workspace workspace;

	GdalTileDataSetBuilder(Workspace workspace) {
		this.workspace = workspace;
	}

	TileDataSet buildTileDataSet(GdalTileStoreJaxb.TileDataSet cfg, ResourceLocation<TileStore> location,
			Envelope gdalEnvelope) throws IOException {
		String filename = cfg.getFile();
		String imageFormat = cfg.getImageFormat();
		String tmsId = cfg.getTileMatrixSetId();
		File file = location.resolveToFile(filename);
		TileMatrixSet tms = workspace.getResource(TileMatrixSetProvider.class, tmsId);
		GdalSettings gdalSettings = workspace.getInitializable(GdalSettings.class);
		gdalSettings.getDatasetPool().addDataset(file, tms.getSpatialMetadata().getCoordinateSystems().get(0));
		List<TileDataLevel> levels = new ArrayList<TileDataLevel>();
		double datasetMinX = gdalEnvelope.getMin().get0();
		double matrixMinX = tms.getSpatialMetadata().getEnvelope().getMin().get0();
		double datasetToMatrixOffsetX = datasetMinX - matrixMinX;
		double datasetMaxY = gdalEnvelope.getMax().get1();
		double matrixMaxY = tms.getSpatialMetadata().getEnvelope().getMax().get1();
		double datasetToMatrixOffsetY = matrixMaxY - datasetMaxY;
		for (TileMatrix tm : tms.getTileMatrices()) {
			int xMin = (int) Math.floor(datasetToMatrixOffsetX / tm.getTileWidth());
			int yMin = (int) Math.floor(datasetToMatrixOffsetY / tm.getTileHeight());
			int xMax = (int) Math.floor((datasetToMatrixOffsetX + gdalEnvelope.getSpan0()) / tm.getTileWidth());
			int yMax = (int) Math.floor((datasetToMatrixOffsetY + gdalEnvelope.getSpan1()) / tm.getTileHeight());
			try {
				levels.add(new GdalTileDataLevel(tm, file.getCanonicalFile(), xMin, yMin, xMax, yMax, imageFormat,
						gdalSettings));
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new DefaultTileDataSet(levels, tms, imageFormat);
	}

}
