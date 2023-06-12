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
package org.deegree.rendering.r2d;

import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.JOIN_ROUND;
import static java.awt.geom.AffineTransform.getTranslateInstance;
import static java.lang.Math.toRadians;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Surface;
import org.deegree.style.styling.TextStyling;
import org.slf4j.Logger;

/**
 * Responsible for creating and rendering of labels. Based on Java2DTextRenderer
 *
 * @author Florian Bingel
 */
public class Java2DLabelRenderer implements LabelRenderer {

	static final Logger LOG = getLogger(Java2DLabelRenderer.class);

	private Java2DRenderer renderer;

	private RendererContext context;

	private Java2DTextRenderer textRenderer;

	private ArrayList<Label> labelList;

	public Java2DLabelRenderer(Java2DRenderer renderer, Java2DTextRenderer textRenderer) {
		this.renderer = renderer;
		this.context = renderer.rendererContext;
		this.textRenderer = textRenderer;
		labelList = new ArrayList<Label>();
	}

	@Override
	public void createLabel(TextStyling styling, String text, Collection<Geometry> geoms) {
		for (Geometry g : geoms) {
			createLabel(styling, text, g);
		}
	}

	@Override
	public void createLabel(TextStyling styling, String text, Geometry geom) {
		if (geom == null) {
			LOG.debug("Trying to render null geometry.");
		}
		if (text == null || text.length() == 0) {
			LOG.debug("Trying to render null or zero length text.");
		}
		geom = renderer.rendererContext.geomHelper.transform(geom);
		geom = renderer.rendererContext.clipper.clipGeometry(geom);
		Font font = textRenderer.convertFont(styling);
		handleGeometryTypes(styling, text, font, geom);
	}

	@Override
	public List<Label> getLabels() {
		return labelList;
	}

	private void handleGeometryTypes(TextStyling styling, String text, Font font, Geometry geom) {
		if (geom == null) {
			LOG.warn("null geometry cannot be handled.");
			return;
		}
		if (geom instanceof Point) {
			labelList.add(createLabel(styling, font, text, (Point) geom));
		}
		else if (geom instanceof Surface && styling.linePlacement != null) {
			textRenderer.render(styling, font, text, (Surface) geom);
		}
		else if (geom instanceof Curve && styling.linePlacement != null) {
			textRenderer.render(styling, font, text, (Curve) geom);
		}
		else if (geom instanceof Polygon && styling.auto) {
			handlePolygonWithAutoPlacement(styling, font, text, (Polygon) geom);
		}
		else if (geom instanceof GeometricPrimitive) {
			labelList.add(createLabel(styling, font, text, geom.getCentroid()));
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
			handleMultiGeometry(styling, text, font, (MultiGeometry<?>) geom);
		}
		else {
			LOG.warn("Trying to use unsupported geometry type '{}' for text rendering.",
					geom.getClass().getSimpleName());
		}
	}

	private <T extends Geometry> ArrayList<Label> handleMultiGeometry(TextStyling styling, String text, Font font,
			MultiGeometry<T> geom) {
		ArrayList<Label> list = new ArrayList<Label>();
		for (T g : geom) {
			handleGeometryTypes(styling, text, font, g);
		}
		return list;
	}

	@Override
	public Label createLabel(TextStyling styling, Font font, String text, Point p) {

		TextLayout layout;
		synchronized (FontRenderContext.class) {
			// apparently getting the font render context is not threadsafe (despite
			// having different graphics here)
			// so do this globally synchronized to fix:
			// http://tracker.deegree.org/deegree-core/ticket/200
			FontRenderContext frc = renderer.graphics.getFontRenderContext();
			layout = new TextLayout(text, font, frc);
		}

		Point2D.Double origin = (Point2D.Double) renderer.worldToScreen
			.transform(new Point2D.Double(p.get0(), p.get1()), null);
		return new Label(layout, styling, font, text, origin, context);
	}

	@Override
	public void render() {
		for (Label l : labelList) {
			render(l);
		}
		labelList = null;
	}

	@Override
	public void render(List<Label> pLabels) {
		for (Label l : pLabels) {
			render(l);
		}
	}

	@Override
	public void render(Label pLabel) {

		renderer.graphics.setFont(pLabel.getFont());
		AffineTransform transform = renderer.graphics.getTransform();
		renderer.graphics.rotate(toRadians(pLabel.getStyling().rotation), pLabel.getOrigin().x, pLabel.getOrigin().y);

		if (pLabel.getStyling().halo != null) {
			context.fillRenderer.applyFill(pLabel.getStyling().halo.fill, pLabel.getStyling().uom);

			int haloSize = round(
					2 * context.uomCalculator.considerUOM(pLabel.getStyling().halo.radius, pLabel.getStyling().uom));

			if (haloSize < 0) {
				// render box styled halo (deegree2 like)
				int wi = Math.abs(haloSize);
				// prevent useless halo of sub-pixel-size
				if (wi < 1) {
					wi = 1;
				}

				int w = (int) (pLabel.getLayout().getBounds().getWidth() + Math.abs(pLabel.getDrawPosition().x % 1)
						+ 0.5d);
				int h = (int) (pLabel.getLayout().getBounds().getHeight() + Math.abs(pLabel.getDrawPosition().y % 1)
						+ 0.5d);
				int bx = (int) pLabel.getDrawPosition().x;
				int by = (int) pLabel.getDrawPosition().y;

				renderer.graphics.fillRect(bx - wi, by - h - wi, w + wi + wi, h + wi + wi);
			}
			else {
				// prevent useless halo of sub-pixel-size
				if (haloSize < 1) {
					haloSize = 1;
				}

				BasicStroke stroke = new BasicStroke(haloSize, CAP_BUTT, JOIN_ROUND);
				renderer.graphics.setStroke(stroke);
				renderer.graphics.draw(pLabel.getLayout()
					.getOutline(getTranslateInstance(pLabel.getDrawPosition().x, pLabel.getDrawPosition().y)));
			}
		}

		// LOG.debug("LabelRender w:" + pLabel.getLayout().getBounds().getWidth() + " h:
		// "+pLabel.getLayout().getBounds().getHeight()+" x: "+pLabel.getDrawPosition().x
		// + " y: "+pLabel.getDrawPosition().y);
		renderer.graphics.setStroke(new BasicStroke());

		context.fillRenderer.applyFill(pLabel.getStyling().fill, pLabel.getStyling().uom);
		pLabel.getLayout()
			.draw(renderer.graphics, (float) pLabel.getDrawPosition().x, (float) pLabel.getDrawPosition().y);

		renderer.graphics.setTransform(transform);
	}

	private void handlePolygonWithAutoPlacement(TextStyling styling, Font font, String text, Polygon geom) {
		Geometry transformedGeom = renderer.rendererContext.geomHelper.transform(geom);
		MultiPoint points = renderer.rendererContext.clipper.calculateInteriorPoints(transformedGeom);
		if (geom == null)
			return;
		handleMultiGeometry(styling, text, font, (MultiGeometry) points);
	}

}
