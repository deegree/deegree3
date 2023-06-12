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
package org.deegree.rendering.r2d.legends;

import static org.deegree.style.styling.components.UOM.Metre;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import org.deegree.commons.utils.Pair;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r2d.TextRenderer;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.style.styling.TextStyling;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class Legends {

	private static final Logger LOG = getLogger(Legends.class);

	private static final GeometryFactory geofac = new GeometryFactory();

	private LegendOptions opts;

	private LegendBuilder builder;

	/**
	 * New legend renderer with default legend options
	 */
	public Legends() {
		opts = new LegendOptions();
		builder = new LegendBuilder(opts);
	}

	/**
	 * @param opts
	 */
	public Legends(LegendOptions opts) {
		this.opts = opts;
		builder = new LegendBuilder(opts);
	}

	public LegendOptions getLegendOptions() {
		return opts;
	}

	public static void paintLegendText(int origin, LegendOptions opts, String text, TextRenderer textRenderer) {
		TextStyling textStyling = new TextStyling();
		textStyling.font = new org.deegree.style.styling.components.Font();
		textStyling.font.fontFamily.add(0, "Arial");
		textStyling.font.fontSize = opts.textSize;
		textStyling.anchorPointX = 0;
		textStyling.anchorPointY = 0.5;
		textStyling.uom = Metre;

		if (text != null && text.length() > 0) {
			textRenderer.render(textStyling, text, geofac.createPoint(null, opts.baseWidth + opts.spacing * 2,
					origin - opts.baseHeight / 2 - opts.spacing, CRSManager.getCRSRef("CRS:1")));
		}
	}

	public List<LegendItem> prepareLegend(Style style, Graphics2D g, int width, int height) {
		return builder.prepareLegend(style, g, width, height);
	}

	/**
	 * @param style
	 * @param width
	 * @param height
	 * @param g
	 */
	public void paintLegend(Style style, int width, int height, Graphics2D g) {
		URL url = style.getLegendURL();
		File file = style.getLegendFile();
		if (url == null && file != null) {
			try {
				url = file.toURI().toURL();
			}
			catch (MalformedURLException e) {
				// nothing to do
			}
		}
		if (url != null) {
			try {
				BufferedImage legend = ImageIO.read(url);
				g.drawImage(legend, 0, 0, width, height, null);
				g.dispose();
				return;
			}
			catch (IOException e) {
				LOG.warn("Legend file {} could not be read, using dynamic legend: {}", file, e.getLocalizedMessage());
				LOG.trace("Stack trace:", e);
			}
		}
		List<LegendItem> items = prepareLegend(style, g, width, height);
		int rowHeight = 2 * opts.spacing + opts.baseHeight;
		int pos = getLegendSize(style).second;

		for (LegendItem item : items) {
			item.paint(pos, opts);
			pos -= rowHeight * item.getHeight();
		}

		g.dispose();
	}

	public Pair<Integer, Integer> getLegendSize(Style style) {
		return builder.getLegendSize(style);
	}

}
