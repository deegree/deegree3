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
package org.deegree.tile.tilematrixset.gdal;

import static org.deegree.commons.utils.MapUtils.DEFAULT_PIXEL_SIZE;
import static org.deegree.tile.persistence.gdal.GdalUtils.getEnvelopeAndCrs;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.tilematrixset.gdal.jaxb.GdalTileMatrixSetConfig;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.slf4j.Logger;

/**
 * {@link ResourceBuilder} for {@link TileMatrixSet} instances based on overview and
 * tiling information reported by <a href="http://www.gdal.org">GDAL</a>.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.5
 */
class GdalTileMatrixSetBuilder implements ResourceBuilder<TileMatrixSet> {

	private static final Logger LOG = getLogger(GdalTileMatrixSetBuilder.class);

	private final GdalTileMatrixSetConfig cfg;

	private final ResourceMetadata<TileMatrixSet> metadata;

	/**
	 * Creates a new {@link GdalTileMatrixSetBuilder} instance.
	 * @param cfg JAXB configuration, must not be <code>null</code>
	 * @param metadata resource metadata, must not be <code>null</code>
	 */
	GdalTileMatrixSetBuilder(GdalTileMatrixSetConfig cfg, ResourceMetadata<TileMatrixSet> metadata) {
		this.cfg = cfg;
		this.metadata = metadata;
	}

	@Override
	public TileMatrixSet build() {
		File file = metadata.getLocation().resolveToFile(cfg.getFile());
		if (!file.exists()) {
			throw new ResourceInitException("File " + file + " does not exist.");
		}
		Dataset gdalDataset = gdal.OpenShared(file.toString());
		try {
			SpatialMetadata envelopeAndCrs = getEnvelopeAndCrs(gdalDataset, cfg.getStorageCRS());
			Band band = gdalDataset.GetRasterBand(1);
			Band overview = band;
			int overviewCount = band.GetOverviewCount();
			List<TileMatrix> matrices = new ArrayList<TileMatrix>();
			for (int i = 0; i <= overviewCount; i++) {
				if (i != 0) {
					overview = band.GetOverview(i - 1);
				}
				TileMatrix matrix = buildTileMatrix(envelopeAndCrs, overview);
				matrices.add(matrix);
				LOG.debug("Level {} has {}x{} tiles of {}x{} pixels, resolution is {}",
						new Object[] { i, matrix.getNumTilesX(), matrix.getNumTilesY(), matrix.getTilePixelsX(),
								matrix.getTilePixelsY(), matrix.getResolution() });
			}
			return new TileMatrixSet(file.getName().substring(0, file.getName().length() - 4), null, matrices,
					envelopeAndCrs, metadata);
		}
		catch (UnknownCRSException e) {
			throw new ResourceInitException("Could not create tile matrix set. Reason: " + e.getLocalizedMessage(), e);
		}
		finally {
			gdalDataset.delete();
		}
	}

	private TileMatrix buildTileMatrix(SpatialMetadata envelopeAndCrs, Band overview) {
		int tw = overview.GetBlockXSize();
		int th = overview.GetBlockYSize();
		int width = overview.getXSize();
		int height = overview.getYSize();
		int numx = (int) Math.ceil((double) width / (double) tw);
		int numy = (int) Math.ceil((double) height / (double) th);
		Envelope env = envelopeAndCrs.getEnvelope();
		double res = Math.max(env.getSpan0() / width, env.getSpan1() / height);
		String id = Double.toString(res / DEFAULT_PIXEL_SIZE);
		return new TileMatrix(id, envelopeAndCrs, BigInteger.valueOf(tw), BigInteger.valueOf(th), res,
				BigInteger.valueOf(numx), BigInteger.valueOf(numy));

	}

}
