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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Envelope;
import org.deegree.tile.Tile;
import org.deegree.tile.TileIOException;

/**
 * A {@link Tile} that is read from a GeoTIFF/BigTIFF file, through ImageIO/imageio-ext.
 * Uses an object pool to cache readers (they take a long time to startup).
 * </p>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class GeoTIFFTile implements Tile {

	// private static final Logger LOG = getLogger( GeoTIFFTile.class );

	private final int imageIndex, x, y;

	private final Envelope envelope;

	private final int sizeX, sizeY;

	private final GenericObjectPool<ImageReader> readerPool;

	public GeoTIFFTile(GenericObjectPool<ImageReader> readerPool, int imageIndex, int x, int y, Envelope envelope,
			int sizeX, int sizeY) {
		this.readerPool = readerPool;
		this.imageIndex = imageIndex;
		this.x = x;
		this.y = y;
		this.envelope = envelope;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}

	@Override
	public BufferedImage getAsImage() throws TileIOException {
		ImageReader reader = null;
		try {
			reader = readerPool.borrowObject();
			BufferedImage img = reader.readTile(imageIndex, x, y);
			if (img.getWidth() != sizeX || img.getHeight() != sizeY) {
				Hashtable<Object, Object> table = new Hashtable<Object, Object>();
				String[] props = img.getPropertyNames();
				if (props != null) {
					for (String p : props) {
						table.put(p, img.getProperty(p));
					}
				}
				BufferedImage img2 = new BufferedImage(img.getColorModel(),
						img.getData().createCompatibleWritableRaster(sizeX, sizeY), img.isAlphaPremultiplied(), table);
				Graphics2D g = img2.createGraphics();
				g.drawImage(img, 0, 0, null);
				g.dispose();
				img = img2;
			}
			return img;
		}
		catch (Exception e) {
			throw new TileIOException("Error retrieving image: " + e.getMessage(), e);
		}
		finally {
			try {
				readerPool.returnObject(reader);
			}
			catch (Exception e) {
				// ignore closing error
			}
		}
	}

	@Override
	public InputStream getAsStream() throws TileIOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ImageIO.write(getAsImage(), "png", bos);
		}
		catch (IOException e) {
			throw new TileIOException("Error retrieving image: " + e.getMessage(), e);
		}
		return new ByteArrayInputStream(bos.toByteArray());
	}

	@Override
	public Envelope getEnvelope() {
		return envelope;
	}

	@Override
	public FeatureCollection getFeatures(int i, int j, int limit) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Feature retrieval is not supported by the GeoTIFFTileStore.");
	}

}
