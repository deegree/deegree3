/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.tools.coverage.gridifier.index;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.TiledRaster;
import org.deegree.coverage.raster.container.TileContainer;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterBuilder;
import org.deegree.geometry.Envelope;
import org.deegree.tools.coverage.gridifier.RasterLevel;

/**
 * The <code>MultiResolutionTileGrid</code> class uses a MultiResolutionRaster from the
 * Raster api.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
public class MultiResolutionTileGrid implements MultiLevelRasterTileIndex {

	private final MultiResolutionRaster mrr;

	private final RasterLevel[] rasterLevels;

	/**
	 * @param topLevelResolutionDir
	 * @param recursive
	 * @param options containing information about the loaded raster.
	 *
	 */
	public MultiResolutionTileGrid(java.io.File topLevelResolutionDir, boolean recursive, RasterIOOptions options) {
		if (!topLevelResolutionDir.isDirectory()) {
			throw new IllegalArgumentException(
					"Specified dir: " + topLevelResolutionDir.getAbsolutePath() + " is not a directory.");
		}
		mrr = new RasterBuilder().buildMultiResolutionRaster(topLevelResolutionDir, recursive, options);
		rasterLevels = new RasterLevel[mrr.getResolutions().size()];
		double currentMin = 0;
		List<Double> resolutions = mrr.getResolutions();
		for (int i = 0; i < rasterLevels.length; ++i) {
			rasterLevels[i] = new RasterLevel(i, i, currentMin, resolutions.get(i));
			currentMin = resolutions.get(i);
		}

	}

	@Override
	public RasterLevel[] getRasterLevels() {
		return rasterLevels;
	}

	@Override
	public Set<TileFile> getTiles(Envelope env, double metersPerPixel) {
		TiledRaster raster = (TiledRaster) mrr.getRaster(metersPerPixel);
		TileContainer container = raster.getTileContainer();
		List<AbstractRaster> tiles = container.getTiles(env);
		Set<TileFile> result = new HashSet<TileFile>();
		int level = -1;

		for (Double res : mrr.getResolutions()) {
			if (metersPerPixel <= res) {
				level++;
			}
			else {
				break;
			}
		}
		// make sure it will get to level 0
		level = Math.max(0, level);

		for (AbstractRaster r : tiles) {
			if (r != null) {
				int id = r.hashCode();
				result.add(new MyTile(id, level, r.getAsSimpleRaster()));
			}
		}
		return result;
	}

	private static class MyTile extends TileFile {

		private SimpleRaster raster;

		/**
		 * @param id
		 * @param level
		 * @param raster
		 */
		public MyTile(int id, int level, SimpleRaster raster) {
			super(id, level, raster.getEnvelope(), null, null, (float) raster.getRasterReference().getResolutionX(),
					(float) raster.getRasterReference().getResolutionY(),
					raster.getRasterReference().getOriginLocation());
			this.raster = raster;
		}

		@Override
		public SimpleRaster loadRaster(String fileName) {
			return raster;
		}

	}

}
