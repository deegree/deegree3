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

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static org.deegree.cs.CRSUtils.EPSG_4326;
import static org.deegree.cs.components.Axis.AO_EAST;
import static org.deegree.cs.coordinatesystems.GeographicCRS.WGS84;
import static org.deegree.style.utils.ShapeHelper.getShapeFromMark;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.MapUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.style.styling.components.Mark;
import org.deegree.style.styling.components.UOM;
import org.deegree.style.utils.ShapeHelper;
import org.slf4j.Logger;

/**
 * <code>RenderHelper</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public class RenderHelper {

	private static final Logger LOG = getLogger(RenderHelper.class);

	/**
	 * @param mark
	 * @param size
	 * @param uom
	 * @param context
	 * @param x
	 * @param y
	 * @param rotation
	 */
	public static void renderMark(Mark mark, int size, UOM uom, RendererContext context, double x, double y,
			double rotation) {
		if (size == 0) {
			LOG.debug("Not rendering a symbol because the size is zero.");
			return;
		}
		if (mark.fill == null && mark.stroke == null) {
			LOG.debug("Not rendering a symbol because no fill/stroke is available/configured.");
			return;
		}

		Shape shape = getShapeFromMark(mark, size - 1, rotation, true, x, y);

		if (mark.fill != null) {
			context.fillRenderer.applyFill(mark.fill, uom);
			context.graphics.fill(shape);
		}
		if (mark.stroke != null) {
			context.strokeRenderer.applyStroke(mark.stroke, uom, shape, 0, null);
		}
	}

	/**
	 * Render <code>Mark</code> to be usable for texture filling of areas
	 * @param mark the mark to render
	 * @param size size
	 * @param uom unit of measure
	 * @param rotation angle measured in degrees
	 * @param hints rendering hints, if available
	 * @return rendered image
	 */
	public static BufferedImage renderMarkForFill(Mark mark, int size, UOM uom, double rotation, RenderingHints hints) {
		if (size == 0) {
			LOG.debug("Not rendering a symbol because the size is zero.");
			return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		}
		if (mark.fill == null && mark.stroke == null) {
			LOG.debug("Not rendering a symbol because no fill/stroke is available/configured.");
			return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		}

		Shape shape = ShapeHelper.getShapeFromMarkForFill(mark, size - 1, rotation);
		Rectangle2D box = shape.getBounds2D();

		// TRICKY fall back to size if width or height is less than 1 (ex. a line)
		int w = (int) Math.round(box.getWidth() < 1 ? size : box.getWidth());
		int h = (int) Math.round(box.getHeight() < 1 ? size : box.getHeight());

		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		if (hints != null) {
			// reuse current rendering hints (ex. anti-aliasing)
			g.setRenderingHints(hints);
		}
		Java2DRenderer renderer = new Java2DRenderer(g);

		GeneralPath union = new GeneralPath(shape);
		double[] pos = { -w, -h, 0, -h, +w, -h, -w, 0, w, 0, -w, h, 0, h, w, h };
		for (int i = 0, j = pos.length - 2; i < j; i += 2) {
			AffineTransform at = AffineTransform.getTranslateInstance(pos[i], pos[i + 1]);
			union.append(shape.getPathIterator(at), false);
		}

		if (mark.fill != null) {
			renderer.rendererContext.fillRenderer.applyFill(mark.fill, uom);
			g.fill(union);
		}
		if (mark.stroke != null) {
			renderer.rendererContext.strokeRenderer.applyStroke(mark.stroke, uom, union, 0, null);
		}

		g.dispose();
		return img;
	}

	/**
	 * @param mapWidth
	 * @param mapHeight
	 * @param bbox
	 * @param crs
	 * @return the WMS 1.1.1 scale (size of the diagonal pixel)
	 */
	public static double calcScaleWMS111(int mapWidth, int mapHeight, Envelope bbox, ICRS crs) {
		if (mapWidth == 0 || mapHeight == 0) {
			return 0;
		}
		double scale = 0;

		if (crs == null) {
			throw new RuntimeException("Invalid null crs.");
		}

		if (isMetric(crs)) {
			/*
			 * this method to calculate a maps scale as defined in OGC WMS and SLD
			 * specification is not required for maps having a projected reference system.
			 * Direct calculation of scale avoids uncertainties
			 */
			double dx = bbox.getSpan0() / mapWidth;
			double dy = bbox.getSpan1() / mapHeight;
			scale = sqrt(dx * dx + dy * dy);
		}
		else {

			if (!crs.equals(WGS84)) {
				// transform the bounding box of the request to EPSG:4326
				GeometryTransformer trans = new GeometryTransformer(WGS84);
				try {
					bbox = trans.transform(bbox, crs);
				}
				catch (IllegalArgumentException e) {
					LOG.error("Unknown error", e);
				}
				catch (TransformationException e) {
					LOG.error("Unknown error", e);
				}
			}
			double dx = bbox.getSpan0() / mapWidth;
			double dy = bbox.getSpan1() / mapHeight;
			double minx = bbox.getMin().get0() + dx * (mapWidth / 2d - 1);
			double miny = bbox.getMin().get1() + dy * (mapHeight / 2d - 1);
			double maxx = bbox.getMin().get0() + dx * (mapWidth / 2d);
			double maxy = bbox.getMin().get1() + dy * (mapHeight / 2d);

			double distance = MapUtils.calcDistance(minx, miny, maxx, maxy);

			scale = distance / MapUtils.SQRT2;

		}

		return scale;
	}

	/**
	 * @param mapWidth
	 * @param mapHeight
	 * @param bbox
	 * @param crs
	 * @return the WMS 1.3.0 scale (horizontal size of the pixel, pixel size == 0.28mm)
	 */
	public static double calcScaleWMS130(int mapWidth, int mapHeight, Envelope bbox, ICRS crs, double pixelSize) {
		if (mapWidth == 0 || mapHeight == 0) {
			return 0;
		}

		double scale = 0;

		if (crs == null) {
			throw new IllegalArgumentException("Null crs when trying to calculate scale.");
		}

		if (isMetric(crs)) {
			/*
			 * this method to calculate a maps scale as defined in OGC WMS and SLD
			 * specification is not required for maps having a projected reference system.
			 * Direct calculation of scale avoids uncertainties
			 */
			double dx = bbox.getSpan0() / mapWidth;
			scale = dx / pixelSize;
		}
		else {

			if (!crs.equals(WGS84)) {
				// transform the bounding box of the request to EPSG:4326
				GeometryTransformer trans = new GeometryTransformer(WGS84);
				try {
					bbox = trans.transform(bbox, crs);
				}
				catch (IllegalArgumentException e) {
					LOG.error("Unknown error", e);
				}
				catch (TransformationException e) {
					LOG.error("Unknown error", e);
				}
			}
			double dx = bbox.getSpan0() / mapWidth;
			double dy = bbox.getSpan1() / mapHeight;

			double minx = bbox.getMin().get0() + dx * (mapWidth / 2d - 1);
			double miny = bbox.getMin().get1() + dy * (mapHeight / 2d - 1);
			double maxx = bbox.getMin().get0() + dx * (mapWidth / 2d);
			double maxy = bbox.getMin().get1() + dy * (mapHeight / 2d - 1);

			double distance = MapUtils.calcDistance(minx, miny, maxx, maxy);

			scale = distance / MapUtils.SQRT2 / pixelSize;

		}

		return scale;
	}

	/**
	 * @param env
	 * @param width
	 * @param height
	 * @return max(resx, resy)
	 */
	public static double calcResolution(Envelope env, int width, int height) {
		return max(env.getSpan0() / width, env.getSpan1() / height);
	}

	public static Pair<Envelope, DoublePair> getWorldToScreenTransform(AffineTransform worldToScreen, Envelope bbox,
			int width, int height) {

		// we have to flip horizontally, so invert y-axis and move y-axis with screen
		// height
		worldToScreen.scale(1, -1);
		worldToScreen.translate(0, -height);

		// calculate scalex, scaley and swap axis if necessary
		final double scalex, scaley;
		final ICRS crs = bbox.getCoordinateSystem();
		if (isXyOrdered(crs)) {
			scalex = width / bbox.getSpan0();
			scaley = height / bbox.getSpan1();
		}
		else {
			worldToScreen.scale(-1, 1);
			worldToScreen.rotate(Math.PI / 2);
			scalex = height / bbox.getSpan0();
			scaley = width / bbox.getSpan1();
		}

		worldToScreen.scale(scalex, scaley);
		worldToScreen.translate(-bbox.getMin().get0(), -bbox.getMin().get1());

		return new Pair<Envelope, DoublePair>(bbox, new DoublePair(scalex, scaley));
	}

	static double calculateResolution(final Envelope bbox, int width) {
		double res;
		try {
			ICRS crs = bbox.getCoordinateSystem();
			if (crs == null || (isMetric(crs) && isXyOrdered(crs))) {
				res = bbox.getSpan0() / width; // use x for resolution
			}
			else {
				// heuristics more or less copied from d2, TODO is use the proper UTM
				// conversion
				Envelope box = new GeometryTransformer(EPSG_4326).transform(bbox);
				double minx = box.getMin().get0(), miny = box.getMin().get1();
				double maxx = minx + box.getSpan0();
				double r = 6378.137;
				double rad = PI / 180d;
				double cose = sin(rad * minx) * sin(rad * maxx) + cos(rad * minx) * cos(rad * maxx);
				double dist = r * acos(cose) * cos(rad * miny);
				res = abs(dist * 1000 / width);
			}
		}
		catch (ReferenceResolvingException e) {
			LOG.warn("Could not determine CRS of bbox, assuming it's in meter...");
			LOG.debug("Stack trace:", e);
			res = bbox.getSpan0() / width; // use x for resolution
		}
		catch (UnknownCRSException e) {
			LOG.warn("Could not determine CRS of bbox, assuming it's in meter...");
			LOG.debug("Stack trace:", e);
			res = bbox.getSpan0() / width; // use x for resolution
		}
		catch (Throwable e) {
			LOG.warn("Could not transform bbox, assuming it's in meter...");
			LOG.debug("Stack trace:", e);
			res = bbox.getSpan0() / width; // use x for resolution
		}
		return res;
	}

	private static boolean isXyOrdered(final ICRS crs) {
		return crs == null || crs.getAlias().equals("CRS:1") || crs.getAxis()[0].getOrientation() == AO_EAST;
	}

	private static boolean isMetric(ICRS crs) {
		return "m".equalsIgnoreCase(crs.getAxis()[0].getUnits().toString());
	}

}
