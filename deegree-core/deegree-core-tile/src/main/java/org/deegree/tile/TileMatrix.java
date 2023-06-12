/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile;

import java.math.BigInteger;

import org.deegree.geometry.metadata.SpatialMetadata;

/**
 * Describes the structure of a {@link TileDataLevel}.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class TileMatrix {

	private final String identifier;

	private final SpatialMetadata spatialMetadata;

	private final BigInteger numTilesX, numTilesY;

	private final BigInteger tileSizeX, tileSizeY;

	private final double resolution, tileWidth, tileHeight;

	/**
	 * All fields must be set. The width/height of the tiles in world coordinates is
	 * calculated automatically.
	 * @param identifier to identify the tile matrix
	 * @param spatialMetadata the envelope and coordinate system, never null
	 * @param tileSizeX the width of a tile in pixels, must be positive and not
	 * <code>null</code>
	 * @param tileSizeY the height of a tile in pixels, must be positive and not
	 * <code>null</code>
	 * @param resolution the resolution of a pixel in world coordinates
	 * @param numTilesX the number of tiles in x direction, must be positive and not
	 * <code>null</code>
	 * @param numTilesY the number of tiles in y direction, must be positive and not
	 * <code>null</code>
	 */
	public TileMatrix(String identifier, SpatialMetadata spatialMetadata, BigInteger tileSizeX, BigInteger tileSizeY,
			double resolution, BigInteger numTilesX, BigInteger numTilesY) {
		this.identifier = identifier;
		this.spatialMetadata = spatialMetadata;
		this.tileSizeX = tileSizeX;
		this.tileSizeY = tileSizeY;
		this.resolution = resolution;
		this.numTilesX = numTilesX;
		this.numTilesY = numTilesY;
		this.tileWidth = tileSizeX.longValue() * resolution;
		this.tileHeight = tileSizeY.longValue() * resolution;
	}

	/**
	 * All fields must be set. The width/height of the tiles in world coordinates is
	 * calculated automatically.
	 * @param identifier to identify the tile matrix
	 * @param spatialMetadata the envelope and coordinate system, never null
	 * @param tileSizeX the width of a tile in pixels, must be positive
	 * @param tileSizeY the height of a tile in pixels, must be positive
	 * @param resolution the resolution of a pixel in world coordinates
	 * @param numTilesX the number of tiles in x direction, must be positive
	 * @param numTilesY the number of tiles in y direction, must be positive
	 */
	public TileMatrix(String identifier, SpatialMetadata spatialMetadata, long tileSizeX, long tileSizeY,
			double resolution, long numTilesX, long numTilesY) {
		this.identifier = identifier;
		this.spatialMetadata = spatialMetadata;
		this.tileSizeX = BigInteger.valueOf(tileSizeX);
		this.tileSizeY = BigInteger.valueOf(tileSizeY);
		this.resolution = resolution;
		this.numTilesX = BigInteger.valueOf(numTilesX);
		this.numTilesY = BigInteger.valueOf(numTilesY);
		this.tileWidth = tileSizeX * resolution;
		this.tileHeight = tileSizeY * resolution;
	}

	/**
	 * @return the envelope and crs, never null
	 */
	public SpatialMetadata getSpatialMetadata() {
		return spatialMetadata;
	}

	/**
	 * @return the width of a tile in pixels
	 */
	public long getTilePixelsX() {
		return tileSizeX.longValue();
	}

	/**
	 * @return the height of a tile in pixels
	 */
	public long getTilePixelsY() {
		return tileSizeY.longValue();
	}

	/**
	 * @return the resolution of a pixel in world coordinates
	 */
	public double getResolution() {
		return resolution;
	}

	/**
	 * @return the number of tiles in x direction
	 */
	public long getNumTilesX() {
		return numTilesX.longValue();
	}

	/**
	 * @return the number of tiles in y direction
	 */
	public long getNumTilesY() {
		return numTilesY.longValue();
	}

	/**
	 * @return the width of a tile in world coordinates (outer edges)
	 */
	public double getTileWidth() {
		return tileWidth;
	}

	/**
	 * @return the height of a tile in world coordinates (outer edges)
	 */
	public double getTileHeight() {
		return tileHeight;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

}
