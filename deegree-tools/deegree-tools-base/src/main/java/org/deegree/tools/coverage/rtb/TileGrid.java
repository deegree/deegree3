/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

 e-mail: info@deegree.org
----------------------------------------------------------------------------*/
package org.deegree.tools.coverage.rtb;

import static java.lang.Math.pow;

import java.util.ArrayList;
import java.util.List;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;

/**
 * This class represents a grid of tiles.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 *
 */
public class TileGrid {

	private static GeometryFactory geomFactory = new GeometryFactory();

	private double x0, y0, width, height;

	private double precision;

	private int xTiles, yTiles;

	private ICRS crs;

	private TileGrid(double x0, double y0, double width, double height, int xTiles, int yTiles, ICRS crs,
			double precision) {
		this.x0 = x0;
		this.y0 = y0;
		this.width = width;
		this.height = height;
		this.xTiles = xTiles;
		this.yTiles = yTiles;
		this.crs = crs;
		this.precision = precision;
	}

	private TileGrid() {
		//
	}

	/**
	 * Create a tile grid based on the output envelope, tile size and target pixel
	 * resolution.
	 * @param dstEnv the envelope of the target raster (in target SRS)
	 * @param tileSize the size of each tile (in pixel)
	 * @param resolution the pixel resolution (units/pixel)
	 * @return a new TileGrid
	 */
	static TileGrid createTileGrid(Envelope dstEnv, int tileSize, float resolution) {
		// TODO
		// double precision = dstEnv.getPrecision();
		double precision = 0.0;

		float tileDimension = tileSize * resolution;

		int xTiles = (int) Math.ceil(dstEnv.getSpan0() / tileDimension);
		int yTiles = (int) Math.ceil(dstEnv.getSpan1() / tileDimension);

		double x0 = dstEnv.getMin().get0();
		double y0 = dstEnv.getMin().get1();

		ICRS crs = dstEnv.getCoordinateSystem();

		return new TileGrid(x0, y0, tileDimension, tileDimension, xTiles, yTiles, crs, precision);
	}

	/**
	 * Create a list with all tiles in this grid.
	 * @return a list of all tiles, stored in row-order
	 */
	public List<Tile> createTileEnvelopes() {
		List<Tile> result = new ArrayList<Tile>(xTiles * yTiles);

		System.out.println("reticulating splines...");
		for (int i = 0; i < yTiles; i++) {
			for (int j = 0; j < xTiles; j++) {
				double x = x0 + j * width;
				double y = y0 + i * height;
				double x2 = x + width;
				double y2 = y + height;

				// TODO
				Envelope subset = geomFactory.createEnvelope(new double[] { x, y }, new double[] { x2, y2 }, crs);
				result.add(new Tile(j, i, subset));
			}
		}
		return result;
	}

	/**
	 * Calculate the optimal tile size, so that the last tile in a raster tree consist of
	 * exactly one tile (no borders).
	 * @param srcWidth the width of the source raster (in pixel)
	 * @param srcHeight the height of the source raster (in pixel)
	 * @param dstEnv the envelope of the target raster (in target SRS)
	 * @param maxTileSize the maximum size of each tile (in pixel)
	 * @return the optimal tile size
	 */
	public static int calculateOptimalTileSize(int srcWidth, int srcHeight, Envelope dstEnv, int maxTileSize) {
		// calculate the new size, consider the aspect ratio to get square pixels
		double deltaX = dstEnv.getSpan0();
		double deltaY = dstEnv.getSpan1();
		double diagSize = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		// pixelSize for calculation of the new image size
		double pixelSize = diagSize / Math.sqrt(Math.pow(srcWidth, 2) + Math.pow(srcHeight, 2));
		int dstHeight;
		int dstWidth;

		int levels = 0;
		do {
			levels += 1;
			dstHeight = (int) (deltaY / (pixelSize * pow(2, levels)) + 0.5);
			dstWidth = (int) (deltaX / (pixelSize * pow(2, levels)) + 0.5);
		}
		while (Math.max(dstHeight, dstWidth) > maxTileSize);

		return Math.max(dstHeight, dstWidth);
	}

	/**
	 * Calculate the resolution for the first raster level.
	 * @param srcWidth the width of the source raster (in pixel)
	 * @param srcHeight the height of the source raster (in pixel)
	 * @param dstEnv the envelope of the target raster (in target SRS)
	 * @param tileSize the size of each tile (in pixel)
	 * @return the resolution in unit/pixel
	 */
	public static double calculateBaseResolution(int srcWidth, int srcHeight, Envelope dstEnv, int tileSize) {
		int xTiles = (int) Math.ceil(srcWidth / (double) tileSize);
		int yTiles = (int) Math.ceil(srcHeight / (double) tileSize);
		double xRes = dstEnv.getSpan0() / srcWidth;
		double yRes = dstEnv.getSpan1() / srcHeight;

		double deltaX = xTiles * tileSize * xRes;
		double deltaY = yTiles * tileSize * yRes;

		double diagSize = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

		return diagSize / Math.sqrt(Math.pow(xTiles * tileSize, 2) + Math.pow(yTiles * tileSize, 2));
	}

	/**
	 * Simple container class for a tile coordinate and envelope.
	 */
	public static class Tile {

		/**
		 * the x tile coordinate
		 */
		public final int x;

		/**
		 * the y tile coordinate
		 */
		public final int y;

		/**
		 * the envelope of the tile
		 */
		public final Envelope envelope;

		/**
		 * @param x
		 * @param y
		 * @param env
		 */
		public Tile(int x, int y, Envelope env) {
			this.x = x;
			this.y = y;
			this.envelope = env;
		}

	}

}
