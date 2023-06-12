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
import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.CAP_SQUARE;
import static java.awt.BasicStroke.JOIN_BEVEL;
import static java.awt.BasicStroke.JOIN_MITER;
import static java.awt.BasicStroke.JOIN_ROUND;
import static org.deegree.commons.utils.TunableParameter.get;
import static org.deegree.commons.utils.math.MathUtils.isZero;
import static org.deegree.style.utils.ShapeHelper.getShapeFromMark;
import static org.deegree.style.utils.ShapeHelper.getShapeFromSvg;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import org.deegree.rendering.r2d.strokes.OffsetStroke;
import org.deegree.rendering.r2d.strokes.ShapeStroke;
import org.deegree.style.styling.components.PerpendicularOffsetType;
import org.deegree.style.styling.components.Stroke;
import org.deegree.style.styling.components.UOM;
import org.deegree.style.utils.UomCalculator;
import org.slf4j.Logger;

/**
 * Responsible to render stroke styles.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class Java2DStrokeRenderer {

	private static final Logger LOG = getLogger(Java2DStrokeRenderer.class);

	private static final boolean SVG_AS_MARK = get("deegree.rendering.graphicstroke.svg-as-mark", false);

	private Graphics2D graphics;

	private UomCalculator uomCalculator;

	private Java2DFillRenderer fillRenderer;

	private RendererContext rendererContext;

	Java2DStrokeRenderer(Graphics2D graphics, UomCalculator uomCalculator, Java2DFillRenderer fillRenderer,
			RendererContext rendererContext) {
		this.graphics = graphics;
		this.uomCalculator = uomCalculator;
		this.fillRenderer = fillRenderer;
		this.rendererContext = rendererContext;
	}

	void applyStroke(Stroke stroke, UOM uom, Shape object, double perpendicularOffset, PerpendicularOffsetType type) {
		if (stroke == null || isZero(stroke.width)) {
			graphics.setPaint(new Color(0, 0, 0, 0));
			return;
		}
		if (stroke.fill == null) {
			graphics.setPaint(stroke.color);
		}
		else {
			fillRenderer.applyGraphicFill(stroke.fill, uom);
		}
		if (stroke.stroke != null) {
			if (applyGraphicStroke(stroke, uom, object, perpendicularOffset, type)) {
				return;
			}
		}
		else {
			applyNormalStroke(stroke, uom, object, perpendicularOffset, type);
		}

		graphics.draw(object);
	}

	private boolean applyGraphicStroke(Stroke stroke, UOM uom, Shape object, double perpendicularOffset,
			PerpendicularOffsetType type) {
		double strokeSizeUOM = stroke.stroke.size <= 0 ? 6 : uomCalculator.considerUOM(stroke.stroke.size, uom);
		double poff = uomCalculator.considerUOM(perpendicularOffset, uom);
		Shape transed = object;
		if (!isZero(poff)) {
			transed = new OffsetStroke(poff, null, type).createStrokedShape(transed);
		}

		Rectangle2D.Double rect = fillRenderer.getGraphicBounds(stroke.stroke, 0, 0, uom);
		BufferedImage img = null;
		Shape[] shapes;
		if (stroke.stroke.image != null) {
			shapes = new Shape[0];
			img = stroke.stroke.image;
		}
		else if (stroke.stroke.imageURL != null && SVG_AS_MARK) {
			// Render SVG like mark
			Shape shape = getShapeFromSvg(stroke.stroke.imageURL, uomCalculator.considerUOM(stroke.stroke.size, uom),
					stroke.stroke.rotation);
			shapes = new Shape[] { shape };
		}
		else if (stroke.stroke.imageURL != null) {
			// render SVG like image
			img = rendererContext.svgRenderer.prepareSvg(rect, stroke.stroke);
			if (img == null) {
				// fallback to regular rendering if no image can be produced
				return false;
			}
			shapes = new Shape[0];
		}
		else if (stroke.stroke.mark != null) {
			Shape shape = getShapeFromMark(stroke.stroke.mark, strokeSizeUOM, stroke.stroke.rotation);
			shapes = new Shape[] { shape };
		}
		else {
			LOG.warn("Only images, SVGs and Mark are currently supported as GraphicStroke.");
			return true;
		}

		ShapeStroke s = new ShapeStroke(shapes, uomCalculator.considerUOM(stroke.strokeGap, uom) + strokeSizeUOM,
				stroke.positionPercentage, uomCalculator.considerUOM(stroke.strokeInitialGap, uom),
				stroke.stroke.anchorPointX, stroke.stroke.anchorPointY,
				uomCalculator.considerUOM(stroke.stroke.displacementX, uom),
				uomCalculator.considerUOM(stroke.stroke.displacementY, uom), stroke.positionRotation);

		if (img != null) {
			s.renderStroke(transed, graphics, img, stroke.stroke, rect);
			return true;
		}
		else if (stroke.stroke.image == null && stroke.stroke.imageURL != null) {
			graphics.setStroke(s);
			graphics.draw(transed);
			return true;
		}
		else if (stroke.stroke.mark != null) {
			transed = s.createStrokedShape(transed);
			if (stroke.stroke.mark.fill != null && !stroke.stroke.mark.fill.isInvisible()) {
				fillRenderer.applyFill(stroke.stroke.mark.fill, uom);
				graphics.fill(transed);
			}
			if (stroke.stroke.mark.stroke != null && !stroke.stroke.mark.stroke.isInvisible()) {
				applyStroke(stroke.stroke.mark.stroke, uom, transed, 0, null);
				graphics.draw(transed);
			}
			return true;
		}
		return false;
	}

	private void applyNormalStroke(Stroke stroke, UOM uom, Shape object, double perpendicularOffset,
			PerpendicularOffsetType type) {
		int linecap = getLinecap(stroke);
		float miterLimit = get("deegree.rendering.stroke.miterlimit", 10f);
		int linejoin = getLinejoin(stroke);
		float dashoffset = (float) uomCalculator.considerUOM(stroke.dashoffset, uom);
		float[] dasharray = stroke.dasharray == null ? null : new float[stroke.dasharray.length];
		if (dasharray != null) {
			for (int i = 0; i < stroke.dasharray.length; ++i) {
				dasharray[i] = (float) uomCalculator.considerUOM(stroke.dasharray[i], uom);
			}
		}

		BasicStroke bs = new BasicStroke((float) uomCalculator.considerUOM(stroke.width, uom), linecap, linejoin,
				miterLimit, dasharray, dashoffset);
		double poff = uomCalculator.considerUOM(perpendicularOffset, uom);
		if (!isZero(poff)) {
			graphics.setStroke(new OffsetStroke(poff, bs, type));
		}
		else {
			graphics.setStroke(bs);
		}
	}

	private int getLinecap(Stroke stroke) {
		int linecap = CAP_SQUARE;
		if (stroke.linecap != null) {
			switch (stroke.linecap) {
				case BUTT:
					linecap = CAP_BUTT;
					break;
				case ROUND:
					linecap = CAP_ROUND;
					break;
				case SQUARE:
					linecap = CAP_SQUARE;
					break;
			}
		}
		return linecap;
	}

	private int getLinejoin(Stroke stroke) {
		int linejoin = JOIN_MITER;
		if (stroke.linejoin != null) {
			switch (stroke.linejoin) {
				case BEVEL:
					linejoin = JOIN_BEVEL;
					break;
				case MITRE:
					linejoin = JOIN_MITER;
					break;
				case ROUND:
					linejoin = JOIN_ROUND;
					break;
			}
		}
		return linejoin;
	}

}
