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

package org.deegree.rendering.r2d;

import static java.lang.Math.abs;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.RasterDataFactory;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.style.styling.RasterChannelSelection;
import org.deegree.style.styling.RasterChannelSelection.ChannelSelectionMode;
import org.deegree.style.styling.RasterStyling;
import org.deegree.style.styling.RasterStyling.ContrastEnhancement;
import org.deegree.style.styling.Styling;
import org.deegree.style.utils.Raster2Feature;
import org.deegree.style.utils.RasterDataUtility;
import org.slf4j.Logger;

/**
 * <code>Java2DRasterRenderer</code>
 *
 * @author <a href="mailto:a.aiordachioaie@jacobs-university.de">Andrei Aiordachioaie</a>
 */
public class Java2DRasterRenderer implements RasterRenderer {

	private static final Logger LOG = getLogger(Java2DRasterRenderer.class);

	private Graphics2D graphics;

	private AffineTransform worldToScreen = new AffineTransform();

	private int width;

	private int height;

	private Envelope envelope;

	private double resx, resy;

	/**
	 * @param graphics
	 * @param width
	 * @param height
	 * @param bbox
	 */
	public Java2DRasterRenderer(Graphics2D graphics, int width, int height, Envelope bbox) {
		this.graphics = graphics;
		this.width = width;
		this.height = height;
		this.envelope = bbox;

		if (bbox != null) {
			Pair<Envelope, DoublePair> p = RenderHelper.getWorldToScreenTransform(worldToScreen, bbox, width, height);
			double scalex = p.second.first;
			double scaley = p.second.second;
			bbox = p.first;

			resx = abs(1 / scalex);
			resy = abs(1 / scaley);

			// we have to flip horizontally, so invert y scale and add the screen height
			worldToScreen.translate(-bbox.getMin().get0() * scalex, bbox.getMin().get1() * scaley + height);
			worldToScreen.scale(scalex, -scaley);

			LOG.debug("For coordinate transformations, scaling by x = {} and y = {}", scalex, -scaley);
			LOG.trace("Final transformation was {}", worldToScreen);
		}
		else {
			LOG.warn("No envelope given, proceeding with a scale of 1.");
		}
	}

	/**
	 * @param graphics
	 */
	public Java2DRasterRenderer(Graphics2D graphics) {
		this.graphics = graphics;
	}

	@Override
	public void render(RasterStyling styling, AbstractRaster raster) {
		LOG.debug("Rendering raster with style '{}'.", styling);
		if (raster == null) {
			LOG.warn("Trying to render null raster.");
			return;
		}
		if (styling == null) {
			LOG.debug("Raster style is null, rendering without style");
			render(raster);
			return;
		}

		handleStyling(styling, raster);
	}

	private void handleStyling(RasterStyling styling, AbstractRaster raster) {
		BufferedImage img = null;

		if (styling.channelSelection != null) {
			// Compute channel selection indexes on current raster
			styling.channelSelection.evaluate(raster.getRasterDataInfo().bandInfo);
		}

		// TODO maybe reorder this a bit
		if (styling.shaded != null) {
			raster = performHillShading(raster, styling);
		}

		if (styling.channelSelection != null) {
			raster = evaluateChannelSelections(styling.channelSelection, raster);
		}

		if (styling.contrastEnhancement != null) {
			raster = performContrastEnhancement(raster, styling.contrastEnhancement);
		}

		if (styling.opacity != 1) {
			LOG.trace("Using opacity: {}", styling.opacity);
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) styling.opacity));
		}

		img = handleFunctions(styling, raster);

		LOG.trace("Rendering raster...");
		if (img != null) {
			render(img, raster.getEnvelope());
		}
		else {
			render(raster);
		}
		LOG.trace("Done rendering raster.");

		handleOutline(styling, raster);
	}

	private BufferedImage handleFunctions(RasterStyling styling, AbstractRaster raster) {
		BufferedImage img = null;
		if (styling.categorize != null || styling.interpolate != null) {
			LOG.trace("Creating raster ColorMap...");
			if (styling.categorize != null) {
				img = styling.categorize.evaluateRaster(raster, styling);
			}
			else if (styling.interpolate != null) {
				img = styling.interpolate.evaluateRaster(raster, styling);
			}
		}
		return img;
	}

	private void handleOutline(RasterStyling styling, AbstractRaster raster) {
		// TODO cleanup outline stuff
		if (styling.imageOutline != null) {
			LOG.trace("Rendering image outline...");
			Geometry geom = Raster2Feature.createPolygonGeometry(raster);
			Java2DRenderer vectorRenderer = new Java2DRenderer(graphics);
			@SuppressWarnings("unchecked")
			Pair<Styling, LinkedList<Geometry>> pair = (Pair) styling.imageOutline.evaluate(null, null);
			Styling ls = pair.first;
			vectorRenderer.render(ls, geom);
			LOG.trace("Done rendering image outline.");
		}
	}

	/**
	 * Performs contrast enhancement on all bands of a raster and returns the modified
	 * raster.
	 * @param raster initial raster
	 * @param contrastEnhancement
	 * @return the enhanced raster
	 */
	private AbstractRaster performContrastEnhancement(AbstractRaster raster, ContrastEnhancement contrastEnhancement) {
		if (contrastEnhancement == null)
			return raster;

		LOG.trace("Enhancing contrast for overall raster...");
		RasterData data = raster.getAsSimpleRaster().getRasterData(), newData = data;
		RasterDataUtility rasutil = new RasterDataUtility(raster);
		rasutil.setContrastEnhancement(contrastEnhancement);
		rasutil.precomputeContrastEnhancements(-1, contrastEnhancement);
		for (int band = 0; band < data.getBands(); band++)
			newData = setEnhancedChannelData(newData, rasutil, band, band, contrastEnhancement);

		return new SimpleRaster(newData, raster.getEnvelope(), raster.getRasterReference(), null);
	}

	/**
	 * Create a new raster according to the specified channel selections (after performing
	 * needed contrast enhancements).
	 * @param channels
	 * @param raster
	 */
	private AbstractRaster evaluateChannelSelections(RasterChannelSelection channels, AbstractRaster raster) {
		if (channels.getMode() == ChannelSelectionMode.NONE)
			return raster;
		LOG.trace("Evaluating channel selections ...");

		SimpleRaster simpleRaster = raster.getAsSimpleRaster();
		RasterData data = simpleRaster.getRasterData();
		int cols = data.getColumns(), rows = data.getRows();
		int[] idx = channels.evaluate(simpleRaster.getBandTypes());
		int redIndex = idx[0], greenIndex = idx[1];
		int blueIndex = idx[2], grayIndex = idx[3];
		RasterDataUtility rasutil = new RasterDataUtility(raster, channels);
		RasterData newData = simpleRaster.getRasterData();
		BandType[] bandTypes = null;
		if (channels.getMode() == ChannelSelectionMode.RGB && data.getBands() > 1) {
			bandTypes = new BandType[] { BandType.RED, BandType.GREEN, BandType.BLUE };
			newData = RasterDataFactory.createRasterData(cols, rows, bandTypes, DataType.BYTE,
					data.getDataInfo().interleaveType, false);

			rasutil.precomputeContrastEnhancements(redIndex, channels.channelContrastEnhancements.get("red"));
			newData = setEnhancedChannelData(newData, rasutil, redIndex, 0,
					channels.channelContrastEnhancements.get("red"));
			rasutil.precomputeContrastEnhancements(greenIndex, channels.channelContrastEnhancements.get("green"));
			newData = setEnhancedChannelData(newData, rasutil, greenIndex, 1,
					channels.channelContrastEnhancements.get("green"));
			rasutil.precomputeContrastEnhancements(blueIndex, channels.channelContrastEnhancements.get("blue"));
			newData = setEnhancedChannelData(newData, rasutil, blueIndex, 2,
					channels.channelContrastEnhancements.get("blue"));

		}
		if (channels.getMode() == ChannelSelectionMode.GRAY) {
			bandTypes = new BandType[] { BandType.BAND_0 };
			newData = RasterDataFactory.createRasterData(cols, rows, bandTypes, DataType.BYTE,
					data.getDataInfo().interleaveType, false);

			newData = setEnhancedChannelData(newData, rasutil, grayIndex, 0,
					channels.channelContrastEnhancements.get("gray"));

		}
		return new SimpleRaster(newData, raster.getEnvelope(), raster.getRasterReference(), null);
	}

	/**
	 * Perform contrast enhancement on one channel and copy the result to a RasterData
	 * object.
	 * @param newData RasterData output container
	 * @param rasutil channel data source
	 * @param inIndex input channel index
	 * @param outIndex output channel index
	 * @param enhancement ContrastEnhancement to perform
	 * @return modified RasterData container
	 */
	private RasterData setEnhancedChannelData(RasterData newData, RasterDataUtility rasutil, int inIndex, int outIndex,
			ContrastEnhancement enhancement) {
		int i = 0, j = 0, val = 0, cols = newData.getColumns(), rows = newData.getRows();

		rasutil.setContrastEnhancement(enhancement);
		if (enhancement != null) {
			LOG.trace("Using gamma {} for channel '{}'...", enhancement.gamma, inIndex);
		}

		for (i = 0; i < cols; i++)
			for (j = 0; j < rows; j++) {
				val = (int) rasutil.getEnhanced(i, j, inIndex);
				newData.setByteSample(i, j, outIndex, int2byte(val));
			}

		return newData;
	}

	private static byte int2byte(final int val) {
		return (val < 128 ? (byte) val : (byte) (val + 2 * Byte.MIN_VALUE));
	}

	/**
	 * Perform the hill-shading algorithm on a DEM raster. Based on algorithm presented at
	 * http://edndoc.esri.com/arcobjects/9.2/net/shared/geoprocessing/spatial_analyst_tools/how_hillshade_works.htm
	 * @param raster Input raster, containing a DEM, with R rows and C columns
	 * @param style
	 * @return a gray-scale raster (with bytes), with R-2 rows and C-2 columns
	 */
	public AbstractRaster performHillShading(AbstractRaster raster, RasterStyling style) {
		LOG.debug("Performing Hill-Shading '{}'.", style.shaded);

		int cols = raster.getColumns(), rows = raster.getRows();
		RasterDataUtility data = new RasterDataUtility(raster, style.channelSelection);
		RasterData shadeData = RasterDataFactory.createRasterData(cols - 2, rows - 2, DataType.BYTE, false);
		RasterGeoReference ref = raster.getRasterReference();
		double resx = cols * ref.getResolutionX() / (cols - 2);
		double resy = rows * ref.getResolutionY() / (rows - 2);
		ref = new RasterGeoReference(ref.getOriginLocation(), resx, resy, ref.getOrigin()[0], ref.getOrigin()[1]);
		SimpleRaster hillShade = new SimpleRaster(shadeData, raster.getEnvelope(), ref, null);

		final double zenith_rad = Math.toRadians(90 - style.shaded.alt);
		final double azimuth_rad = Math.toRadians(90 - style.shaded.azimuthAngle);
		final double sinZenith = Math.sin(zenith_rad);
		final double cosZenith = Math.cos(zenith_rad);
		double slope_rad;
		double aspect_rad = 0;
		byte shade = 0;
		double dx, dy;
		float m[][] = new float[3][3];

		for (int row = 1; row < rows - 1; row++) {
			for (int col = 1; col < cols - 1; col++) {
				m[0][0] = data.get(col - 1, row - 1);
				m[0][1] = data.get(col, row - 1);
				m[0][2] = data.get(col + 1, row - 1);
				m[1][0] = data.get(col - 1, row);
				m[1][1] = data.get(col, row);
				m[1][2] = data.get(col + 1, row);
				m[2][0] = data.get(col - 1, row + 1);
				m[2][1] = data.get(col, row + 1);
				m[2][2] = data.get(col + 1, row + 1);

				dx = ((m[0][2] + 2 * m[1][2] + m[2][2]) - (m[0][0] + 2 * m[1][0] + m[2][0])) / 8;
				dy = ((m[2][0] + 2 * m[2][1] + m[2][2]) - (m[0][0] + 2 * m[0][1] + m[0][2])) / 8;
				slope_rad = Math.atan(style.shaded.reliefFactor * Math.sqrt(dx * dx + dy * dy));
				if (dx != 0) {
					aspect_rad = Math.atan2(dy, -dx);
					if (aspect_rad < 0)
						aspect_rad += Math.PI * 2;
				}
				if (dx == 0) {
					if (dy > 0)
						aspect_rad = Math.PI / 2;
					else if (dy < 0)
						aspect_rad = 2 * Math.PI - Math.PI / 2;
					else
						aspect_rad = 0;
				}

				long val = Math.round(255.0 * ((cosZenith * Math.cos(slope_rad))
						+ (sinZenith * Math.sin(slope_rad) * Math.cos(azimuth_rad - aspect_rad))));
				if (val < 0)
					val = 0;
				shade = (byte) val;

				shadeData.setByteSample(col - 1, row - 1, 0, shade);
			}
		}

		return hillShade;
	}

	private void render(final AbstractRaster raster) {
		render(RasterFactory.imageFromRaster(raster), raster.getEnvelope());
	}

	private void render(final BufferedImage img, final Envelope box) {
		if (envelope != null && box != null) {
			int minx = 0, miny = 0, maxx = width, maxy = height;
			minx = round((box.getMin().get0() - envelope.getMin().get0()) / resx);
			miny = round((box.getMin().get1() - envelope.getMin().get1()) / resy);
			maxx = width - round((envelope.getMax().get0() - box.getMax().get0()) / resx);
			maxy = height - round((envelope.getMax().get1() - box.getMax().get1()) / resy);
			graphics.drawImage(img, minx, miny, maxx - minx, maxy - miny, null);
		}
		else {
			graphics.drawImage(img, worldToScreen, null);
		}
	}

}
