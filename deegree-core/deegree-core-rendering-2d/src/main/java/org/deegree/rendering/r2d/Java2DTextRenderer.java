/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.rendering.r2d;

import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.JOIN_ROUND;
import static java.awt.Font.BOLD;
import static java.awt.Font.ITALIC;
import static java.awt.Font.PLAIN;
import static java.awt.geom.AffineTransform.getTranslateInstance;
import static java.lang.Math.toRadians;
import static org.deegree.commons.utils.math.MathUtils.isZero;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D.Double;
import java.awt.geom.Point2D;
import java.util.Collection;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.rendering.r2d.strokes.HaloStroke;
import org.deegree.rendering.r2d.strokes.OffsetStroke;
import org.deegree.rendering.r2d.strokes.TextStroke;
import org.deegree.style.styling.TextStyling;
import org.deegree.style.styling.components.Font.Style;
import org.slf4j.Logger;

/**
 * <code>Java2DTextRenderer</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class Java2DTextRenderer implements TextRenderer {

	private static final Logger LOG = getLogger(Java2DTextRenderer.class);

	private Java2DRenderer renderer;

	// private Java2DLabelRenderer labelRenderer;

	/**
	 * @param renderer
	 */
	public Java2DTextRenderer(Java2DRenderer renderer) {
		this.renderer = renderer;
		// this.labelRenderer = new Java2DLabelRenderer( renderer );
	}

	@Override
	public void render(TextStyling styling, String text, Collection<Geometry> geoms) {
		for (Geometry g : geoms) {
			render(styling, text, g);
		}
	}

	@Override
	public void render(final TextStyling styling, final String text, final Geometry geom) {
		if (geom == null) {
			LOG.debug("Trying to render null geometry.");
			return;
		}
		if (text == null || text.length() == 0) {
			LOG.debug("Trying to render null or zero length text.");
			return;
		}
		final Geometry renderGeometry = renderer.transformToWorldCrsAndClip(geom);
		if (renderGeometry == null) {
			return;
		}
		final Font font = convertFont(styling);
		handleGeometryTypes(styling, text, font, renderGeometry);
	}

	private void handleGeometryTypes(TextStyling styling, String text, Font font, Geometry geom) {
		if (geom instanceof Point) {
			render(styling, font, text, (Point) geom);
		}
		else if (geom instanceof Surface && styling.linePlacement != null) {
			render(styling, font, text, (Surface) geom);
		}
		else if (geom instanceof Curve && styling.linePlacement != null) {
			render(styling, font, text, (Curve) geom);
		}
		else if (geom instanceof GeometricPrimitive) {
			render(styling, font, text, geom.getCentroid());
		}
		else if (geom instanceof MultiPoint) {
			handleMultiGeometry(styling, text, font, (MultiPoint) geom);
		}
		else if (geom instanceof MultiCurve<?> && styling.linePlacement != null) {
			handleMultiGeometry(styling, text, font, (MultiCurve<?>) geom);
		}
		else if (geom instanceof MultiLineString && styling.linePlacement != null) {
			handleMultiGeometry(styling, text, font, (MultiLineString) geom);
		}
		else if (geom instanceof MultiGeometry<?>) {
			for (Geometry g : (MultiGeometry<?>) geom) {
				render(styling, text, g);
			}
		}
		else {
			LOG.warn("Trying to use unsupported geometry type '{}' for text rendering.",
					geom.getClass().getSimpleName());
		}
	}

	private <T extends Geometry> void handleMultiGeometry(TextStyling styling, String text, Font font,
			MultiGeometry<T> geom) {
		for (T g : geom) {
			handleGeometryTypes(styling, text, font, g);
		}
	}

	void render(TextStyling styling, Font font, String text, Surface surface) {
		for (SurfacePatch patch : surface.getPatches()) {
			if (patch instanceof PolygonPatch) {
				PolygonPatch polygonPatch = (PolygonPatch) patch;
				for (Curve curve : polygonPatch.getBoundaryRings()) {
					render(styling, font, text, curve);
				}
			}
			else {
				throw new IllegalArgumentException("Cannot render non-planar surfaces.");
			}
		}
	}

	void render(TextStyling styling, Font font, String text, Point p) {
		Point2D.Double pt = (Point2D.Double) renderer.worldToScreen.transform(new Point2D.Double(p.get0(), p.get1()),
				null);

		double x = pt.x + renderer.rendererContext.uomCalculator.considerUOM(styling.displacementX, styling.uom);
		double y = pt.y - renderer.rendererContext.uomCalculator.considerUOM(styling.displacementY, styling.uom);
		renderer.graphics.setFont(font);
		AffineTransform transform = renderer.graphics.getTransform();
		renderer.graphics.rotate(toRadians(styling.rotation), x, y);
		TextLayout layout;
		synchronized (FontRenderContext.class) {
			// apparently getting the font render context is not threadsafe (despite
			// having different graphics here)
			// so do this globally synchronized to fix:
			// http://tracker.deegree.org/deegree-core/ticket/200
			FontRenderContext frc = renderer.graphics.getFontRenderContext();
			layout = new TextLayout(text, font, frc);
		}
		double width = layout.getBounds().getWidth();
		double height = layout.getBounds().getHeight();
		double px = x - styling.anchorPointX * width;
		double py = y + styling.anchorPointY * height;

		if (styling.halo != null) {
			renderer.rendererContext.fillRenderer.applyFill(styling.halo.fill, styling.uom);

			int haloSize = round(
					2 * renderer.rendererContext.uomCalculator.considerUOM(styling.halo.radius, styling.uom));
			if (haloSize < 0) {
				// render box styled halo (deegree2 like)
				int wi = Math.abs(haloSize);
				// prevent useless halo of sub-pixel-size
				if (wi < 1) {
					wi = 1;
				}

				int w = (int) (width + 0.5d);
				int h = (int) (height + 0.5d);
				int bx = (int) px;
				int by = (int) py;

				renderer.graphics.fillRect(bx - wi, by - h - wi, w + wi + wi, h + wi + wi);
			}
			else {
				// prevent useless halo of sub-pixel-size
				if (haloSize < 1) {
					haloSize = 1;
				}

				BasicStroke stroke = new BasicStroke(haloSize, CAP_BUTT, JOIN_ROUND);
				renderer.graphics.setStroke(stroke);
				renderer.graphics.draw(layout.getOutline(getTranslateInstance(px, py)));
			}
		}

		renderer.graphics.setStroke(new BasicStroke());

		renderer.rendererContext.fillRenderer.applyFill(styling.fill, styling.uom);
		layout.draw(renderer.graphics, (float) px, (float) py);

		renderer.graphics.setTransform(transform);
	}

	void render(TextStyling styling, Font font, String text, Curve c) {
		java.awt.Stroke stroke = new TextStroke(text, font, styling.linePlacement);
		if (isZero(((TextStroke) stroke).getLineHeight())) {
			return;
		}
		stroke = applyOffset(styling, stroke);
		Double line = renderer.rendererContext.geomHelper.fromCurve(c, false);
		if (styling.halo != null) {
			Stroke haloStroke = new HaloStroke(text, font, styling.linePlacement, styling.halo, styling.uom,
					renderer.rendererContext.uomCalculator);
			haloStroke = applyOffset(styling, haloStroke);
			renderer.rendererContext.fillRenderer.applyFill(styling.halo.fill, styling.uom);
			renderer.graphics.setStroke(haloStroke);
			renderer.graphics.draw(line);
		}

		renderer.rendererContext.fillRenderer.applyFill(styling.fill, styling.uom);
		renderer.graphics.setStroke(stroke);
		renderer.graphics.draw(line);
	}

	protected Font convertFont(TextStyling styling) {
		AffineTransform shear = null;

		int style = styling.font.bold ? BOLD : PLAIN;
		switch (styling.font.fontStyle) {
			case ITALIC:
				style += ITALIC;
				break;
			case NORMAL:
				style += PLAIN; // yes, it's zero, but the code looks nicer this way
				break;
			case OBLIQUE:
				// Shear the font horizontally to achieve obliqueness
				shear = new AffineTransform();
				shear.shear(-0.2, 0);
				break;
		}

		// use the first matching name, or Dialog, if none was found
		int size = round(renderer.rendererContext.uomCalculator.considerUOM(styling.font.fontSize, styling.uom));
		Font font = new Font("", style, size);
		for (String name : styling.font.fontFamily) {
			font = new Font(name, style, size);
			if (!font.getFamily().equalsIgnoreCase("dialog")) {
				break;
			}
		}

		if (styling.font.fontStyle == Style.OBLIQUE && shear != null)
			font = font.deriveFont(shear);
		return font;
	}

	private java.awt.Stroke applyOffset(TextStyling styling, java.awt.Stroke stroke) {
		if (!isZero(styling.linePlacement.perpendicularOffset)) {
			stroke = new OffsetStroke(styling.linePlacement.perpendicularOffset, stroke,
					styling.linePlacement.perpendicularOffsetType);
		}
		return stroke;
	}

}
