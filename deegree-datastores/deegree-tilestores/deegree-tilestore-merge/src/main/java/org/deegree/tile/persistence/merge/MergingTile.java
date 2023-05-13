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
package org.deegree.tile.persistence.merge;

import static java.awt.Color.WHITE;
import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Envelope;
import org.deegree.tile.Tile;
import org.deegree.tile.TileIOException;

/**
 * {@link Tile} implementation used by {@link MergingTileStore}.
 *
 * @author <a href="mailto:Reijer.Copier@idgis.nl">Reijer Copier</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
class MergingTile implements Tile {

	private final List<Tile> tiles;

	MergingTile(final List<Tile> tiles) {
		this.tiles = tiles;
	}

	@Override
	public BufferedImage getAsImage() throws TileIOException {
		Iterator<Tile> itr = tiles.iterator();
		Tile firstTile = itr.next();
		BufferedImage img = firstTile.getAsImage();
		Graphics g = img.getGraphics();
		while (itr.hasNext()) {
			Tile nextTile = itr.next();
			BufferedImage nextImage = nextTile.getAsImage();
			if (nextImage.getColorModel().hasAlpha()) {
				g.drawImage(nextImage, 0, 0, null);
			}
			else {
				g.drawImage(makeColorTranslucent(nextImage, WHITE), 0, 0, null);
			}
		}
		return img;
	}

	private Image makeColorTranslucent(final BufferedImage image, final Color translucentColor) {
		final int transparentRgb = translucentColor.getRGB();
		final ImageFilter filter = new RGBImageFilter() {
			public final int filterRGB(final int x, final int y, final int rgb) {
				if (rgb == transparentRgb) {
					return Color.TRANSLUCENT;
				}
				return rgb;
			}
		};
		final ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}

	@Override
	public InputStream getAsStream() throws TileIOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			BufferedImage img = getAsImage();
			if (img.getTransparency() != BufferedImage.OPAQUE) {
				final int width = img.getWidth();
				final int height = img.getHeight();

				BufferedImage noTransparency = new BufferedImage(width, height, TYPE_3BYTE_BGR);
				Graphics g = noTransparency.getGraphics();
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, width, height);
				g.drawImage(img, 0, 0, null);
				img = noTransparency;
			}
			ImageIO.write(img, "jpeg", output);
		}
		catch (IOException e) {
			throw new TileIOException(e);
		}
		return new ByteArrayInputStream(output.toByteArray());
	}

	@Override
	public Envelope getEnvelope() {
		return tiles.get(0).getEnvelope();
	}

	@Override
	public FeatureCollection getFeatures(int i, int j, int limit) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("MergingTile does not support getFeatures");
	}

}
