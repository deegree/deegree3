/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.rendering.r2d.context;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.KEY_STROKE_CONTROL;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_OFF;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
import static java.awt.RenderingHints.VALUE_RENDER_DEFAULT;
import static java.awt.RenderingHints.VALUE_RENDER_QUALITY;
import static java.awt.RenderingHints.VALUE_RENDER_SPEED;
import static java.awt.RenderingHints.VALUE_STROKE_DEFAULT;
import static java.awt.RenderingHints.VALUE_STROKE_NORMALIZE;
import static java.awt.RenderingHints.VALUE_STROKE_PURE;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;

import java.awt.Graphics2D;

import org.deegree.rendering.r2d.context.MapOptions.Antialias;
import org.deegree.rendering.r2d.context.MapOptions.Interpolation;
import org.deegree.rendering.r2d.context.MapOptions.Quality;

/**
 * Utility methods to apply map options to a graphics 2d object.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class Java2DHelper {

	public static void applyHints(final String layerName, final Graphics2D g, final MapOptionsMaps options,
			final MapOptions defaults) {
		applyQuality(layerName, g, options, defaults);
		applyInterpolation(layerName, g, options, defaults);
		applyAntialiasing(layerName, g, options, defaults);
	}

	private static void applyQuality(final String layerName, final Graphics2D g, final MapOptionsMaps options,
			final MapOptions defaults) {
		Quality q = options.getQuality(layerName);
		if (q == null) {
			q = defaults.getQuality();
		}
		switch (q) {
			case HIGH:
				g.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
				g.setRenderingHint(KEY_STROKE_CONTROL, VALUE_STROKE_PURE);
				break;
			case LOW:
				g.setRenderingHint(KEY_RENDERING, VALUE_RENDER_SPEED);
				g.setRenderingHint(KEY_STROKE_CONTROL, VALUE_STROKE_NORMALIZE);
				break;
			case NORMAL:
				g.setRenderingHint(KEY_RENDERING, VALUE_RENDER_DEFAULT);
				g.setRenderingHint(KEY_STROKE_CONTROL, VALUE_STROKE_DEFAULT);
				break;
		}
	}

	private static void applyInterpolation(final String layerName, final Graphics2D g, final MapOptionsMaps options,
			final MapOptions defaults) {
		Interpolation i = options.getInterpolation(layerName);
		if (i == null) {
			i = defaults.getInterpolation();
		}
		switch (i) {
			case BICUBIC:
				g.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
				break;
			case BILINEAR:
				g.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
				break;
			case NEARESTNEIGHBOR:
			case NEARESTNEIGHBOUR:
				g.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				break;
		}
	}

	private static void applyAntialiasing(final String layerName, final Graphics2D g, final MapOptionsMaps options,
			final MapOptions defaults) {
		Antialias a = options.getAntialias(layerName);
		if (a == null) {
			a = defaults.getAntialias();
		}
		switch (a) {
			case IMAGE:
				g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
				g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_OFF);
				break;
			case TEXT:
				g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);
				g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
				break;
			case BOTH:
				g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
				g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
				break;
			case NONE:
				g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);
				g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_OFF);
				break;
		}
	}

}
