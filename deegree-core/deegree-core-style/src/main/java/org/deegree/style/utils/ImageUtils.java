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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.style.utils;

import static java.awt.image.BufferedImage.TYPE_BYTE_INDEXED;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.awt.image.DataBuffer.TYPE_BYTE;
import static java.awt.image.Raster.createBandedRaster;
import static javax.media.jai.operator.ColorQuantizerDescriptor.MEDIANCUT;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.BandSelectDescriptor;
import javax.media.jai.operator.ColorQuantizerDescriptor;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class ImageUtils {

	private static int getType(boolean transparent, String format) {
		if (!isTransparentAndTransparencySupported(format, transparent)) {
			return TYPE_INT_RGB;
		}
		return transparent ? TYPE_INT_ARGB : TYPE_INT_RGB;
	}

	/**
	 * @return an empty image conforming to the request parameters
	 */
	public static BufferedImage prepareImage(String format, int width, int height, boolean transparent, Color bgColor) {
		if (format.equals("image/png; mode=8bit") || format.equals("image/png; subtype=8bit")
				|| format.equals("image/gif")) {
			ColorModel cm = PlanarImage.getDefaultColorModel(TYPE_BYTE, 4);
			return new BufferedImage(cm, createBandedRaster(TYPE_BYTE, width, height, 4, null), false, null);
		}

		BufferedImage img = new BufferedImage(width, height, getType(transparent, format));
		if (!isTransparentAndTransparencySupported(format, transparent)) {
			Graphics2D g = img.createGraphics();
			g.setBackground(bgColor);
			g.clearRect(0, 0, width, height);
			g.dispose();
		}

		return img;
	}

	private static boolean isTransparentAndTransparencySupported(String format, boolean transparent) {
		if (format.equals("image/x-ms-bmp") || format.equals("image/jpeg")) {
			return false;
		}
		return transparent;
	}

	/**
	 * @param img
	 * @return a new 8bit image, quantized
	 */
	public static final BufferedImage postprocessPng8bit(final BufferedImage img) {
		RenderedOp torgb = BandSelectDescriptor.create(img, new int[] { 0, 1, 2 }, null);

		torgb = ColorQuantizerDescriptor.create(torgb, MEDIANCUT, 254, null, null, null, null, null);

		WritableRaster data = torgb.getAsBufferedImage().getRaster();

		IndexColorModel model = (IndexColorModel) torgb.getColorModel();
		byte[] reds = new byte[256];
		byte[] greens = new byte[256];
		byte[] blues = new byte[256];
		byte[] alphas = new byte[256];
		model.getReds(reds);
		model.getGreens(greens);
		model.getBlues(blues);
		// note that this COULD BE OPTIMIZED to SUPPORT EG HALF TRANSPARENT PIXELS for
		// PNG-8!
		// It's not true that PNG-8 does not support this! Try setting the value to eg.
		// 128 here and see what
		// you'll get...
		for (int i = 0; i < 254; ++i) {
			alphas[i] = -1;
		}
		alphas[255] = 0;
		IndexColorModel newModel = new IndexColorModel(8, 256, reds, greens, blues, alphas);

		// yeah, double memory, but it was the only way I could find (I could be blind...)
		BufferedImage res = new BufferedImage(torgb.getWidth(), torgb.getHeight(), TYPE_BYTE_INDEXED, newModel);
		res.setData(data);

		// do it the hard way as the OR operation would destroy the channels
		for (int y = 0; y < img.getHeight(); ++y) {
			for (int x = 0; x < img.getWidth(); ++x) {
				if (img.getRGB(x, y) == 0) {
					res.setRGB(x, y, 0);
				}
			}
		}

		return res;
	}

}
