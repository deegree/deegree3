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
package org.deegree.tile.persistence.geotiff;

import java.io.File;
import java.util.List;

import javax.imageio.ImageReader;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileMatrix;

/**
 * The <code>GeoTIFFTileMatrix</code> is a tile matrix handing out GeoTIFFTile tiles. It
 * uses an object pool shared among all tiles created by this matrix.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */

public class GeoTIFFTileDataLevel implements TileDataLevel {

	private final TileMatrix metadata;

	private final int imageIndex;

	private final GeometryFactory fac = new GeometryFactory();

	private GenericObjectPool<ImageReader> readerPool;

	private final int xoff, yoff, numx, numy;

	public GeoTIFFTileDataLevel(TileMatrix metadata, File file, int imageIndex, int xoff, int yoff, int numx, int numy,
			int maxActive) {
		this.metadata = metadata;
		this.imageIndex = imageIndex;
		ImageReaderFactory fac = new ImageReaderFactory(file);
		GenericObjectPoolConfig<ImageReader> poolConfig = new GenericObjectPoolConfig<>();
		poolConfig.setMaxTotal(maxActive);
		this.readerPool = new GenericObjectPool<ImageReader>(fac, poolConfig);
		this.xoff = xoff;
		this.yoff = yoff;
		this.numx = numx;
		this.numy = numy;
	}

	@Override
	public TileMatrix getMetadata() {
		return metadata;
	}

	@Override
	public GeoTIFFTile getTile(long x, long y) {
		if (metadata.getNumTilesX() <= x || metadata.getNumTilesY() <= y || x < 0 || y < 0) {
			return null;
		}
		// are requested tiles contained in tiff?
		if (x < xoff || y < yoff) {
			return null;
		}
		x -= xoff;
		y -= yoff;
		if (x >= numx || y >= numy) {
			return null;
		}

		double width = metadata.getTileWidth();
		double height = metadata.getTileHeight();
		Envelope env = metadata.getSpatialMetadata().getEnvelope();
		double minx = width * x + env.getMin().get0();
		double miny = env.getMax().get1() - height * y;
		Envelope envelope = fac.createEnvelope(minx, miny, minx + width, miny - height, env.getCoordinateSystem());
		return new GeoTIFFTile(readerPool, imageIndex, (int) x, (int) y, envelope, (int) metadata.getTilePixelsX(),
				(int) metadata.getTilePixelsY());
	}

	@Override
	public List<String> getStyles() {
		return null;
	}

}
