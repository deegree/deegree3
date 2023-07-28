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
import java.util.List;

import org.deegree.commons.gdal.GdalDataset;
import org.deegree.commons.gdal.GdalSettings;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileMatrix;

/**
 * {@link TileDataLevel} backed by an {@link GdalDataset}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
class GdalTileDataLevel implements TileDataLevel {

	private final TileMatrix metadata;

	private final File file;

	private final int xMin;

	private final int yMin;

	private final int yMax;

	private final int xMax;

	private final String imageFormat;

	private final GdalSettings gdalSettings;

	/**
	 * Creates a new {@link GdalTileDataLevel} instance.
	 * @param matrix tile matrix, must not be <code>null</code>
	 * @param file GDAL raster file, must not be <code>null</code>
	 * @param xMin
	 * @param yMin
	 * @param gdalSettings
	 * @throws Exception
	 */
	GdalTileDataLevel(TileMatrix matrix, File file, int xMin, int yMin, int xMax, int yMax, String imageFormat,
			GdalSettings gdalSettings) throws Exception {
		this.metadata = matrix;
		this.file = file;
		this.xMin = xMin;
		this.yMin = yMin;
		this.xMax = xMax;
		this.yMax = yMax;
		this.imageFormat = imageFormat;
		this.gdalSettings = gdalSettings;
	}

	@Override
	public TileMatrix getMetadata() {
		return metadata;
	}

	@Override
	public Tile getTile(long x, long y) {
		if (!isWithinLimits(x, y)) {
			return null;
		}
		double tileWidth = metadata.getTileWidth();
		double tileHeight = metadata.getTileHeight();
		Envelope matrixEnvelope = metadata.getSpatialMetadata().getEnvelope();
		double minX = tileWidth * x + matrixEnvelope.getMin().get0();
		double maxX = minX + tileWidth;
		double maxY = matrixEnvelope.getMax().get1() - tileHeight * y;
		double minY = maxY - tileHeight;
		ICRS crs = metadata.getSpatialMetadata().getEnvelope().getCoordinateSystem();
		Point min = new DefaultPoint(null, crs, null, new double[] { minX, minY });
		Point max = new DefaultPoint(null, crs, null, new double[] { maxX, maxY });
		Envelope tileEnvelope = new DefaultEnvelope(null, crs, null, min, max);
		return new GdalTile(file, tileEnvelope, (int) metadata.getTilePixelsX(), (int) metadata.getTilePixelsY(),
				imageFormat, gdalSettings.getDatasetPool());
	}

	private boolean isWithinLimits(long x, long y) {
		return x >= xMin && x <= xMax && y >= yMin && y <= yMax;
	}

	@Override
	public List<String> getStyles() {
		return null;
	}

}
