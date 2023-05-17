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
package org.deegree.rendering.r2d.context;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_OFF;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
import static java.awt.RenderingHints.VALUE_RENDER_DEFAULT;
import static java.awt.RenderingHints.VALUE_RENDER_QUALITY;
import static java.awt.RenderingHints.VALUE_RENDER_SPEED;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
import static javax.imageio.ImageIO.write;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.deegree.style.utils.ColorQuantizer;
import org.deegree.style.utils.ImageUtils;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class ImageRenderContext extends Java2DRenderContext {

	private final BufferedImage image;

	private final String format;

	private final RenderingInfo info;

	private ImageRenderContext(RenderingInfo info, BufferedImage image, Graphics2D graphics,
			OutputStream outputStream) {
		super(info, graphics, outputStream);

		this.image = image;
		this.format = info.getFormat();
		this.info = info;
	}

	public static RenderContext createInstance(RenderingInfo info, BufferedImage image, OutputStream outputStream) {
		return new ImageRenderContext(info, image, image.createGraphics(), outputStream);
	}

	public static RenderContext createInstance(RenderingInfo info, OutputStream outputStream) {
		BufferedImage image = ImageUtils.prepareImage(info.getFormat(), info.getWidth(), info.getHeight(),
				info.getTransparent(), info.getBgColor());

		return createInstance(info, image, outputStream);
	}

	@Override
	public boolean close() throws IOException {
		try {
			graphics.dispose();

			if (outputStream != null) {
				BufferedImage image = this.image;
				String format = this.format.substring(this.format.indexOf("/") + 1);
				if (format.equals("x-ms-bmp")) {
					format = "bmp";
				}
				if (format.equals("png; subtype=8bit") || format.equals("png; mode=8bit")) {
					image = ColorQuantizer.quantizeImage(image, 256, false, false);
					format = "png";
				}

				if (info.getSerializer() != null) {
					info.getSerializer().serialize(info, image, outputStream);
				}
				else {
					return write(image, format, outputStream);
				}
			}
		}
		finally {
			closeQuietly(outputStream);
		}
		return false;
	}

	@Override
	public void applyOptions(MapOptions options) {
		applyQuality(options);
		applyInterpolation(options);
		applyAntialias(options);
	}

	private void applyAntialias(MapOptions options) {
		switch (options.getAntialias()) {
			case IMAGE:
				graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
				graphics.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_OFF);
				break;
			case TEXT:
				graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);
				graphics.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
				break;
			case BOTH:
				graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
				graphics.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
				break;
			case NONE:
				graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);
				graphics.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_OFF);
				break;
		}
	}

	private void applyInterpolation(MapOptions options) {
		switch (options.getInterpolation()) {
			case BICUBIC:
				graphics.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
				break;
			case BILINEAR:
				graphics.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
				break;
			case NEARESTNEIGHBOR:
			case NEARESTNEIGHBOUR:
				graphics.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				break;
		}
	}

	private void applyQuality(MapOptions options) {
		switch (options.getQuality()) {
			case HIGH:
				graphics.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
				break;
			case LOW:
				graphics.setRenderingHint(KEY_RENDERING, VALUE_RENDER_SPEED);
				break;
			case NORMAL:
				graphics.setRenderingHint(KEY_RENDERING, VALUE_RENDER_DEFAULT);
				break;
		}
	}

}
