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

import org.deegree.style.styling.components.Fill;
import org.deegree.style.styling.components.Graphic;
import org.deegree.style.styling.components.UOM;
import org.deegree.style.utils.UomCalculator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.rendering.r2d.RenderHelper.renderMarkForFill;

/**
 * Responsible for applying fill stylings to a graphics 2d.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
class Java2DFillRenderer {

	private UomCalculator uomCalculator;

	private Graphics2D graphics;

	Java2DFillRenderer(UomCalculator uomCalculator, Graphics2D graphics) {
		this.uomCalculator = uomCalculator;
		this.graphics = graphics;
	}

	void applyGraphicFill(Graphic graphic, UOM uom) {
		Rectangle2D.Double graphicBounds = getGraphicBounds(graphic, 0, 0, uom);
		BufferedImage img;

		if (graphic.image == null) {
			int size = round(uomCalculator.considerUOM(graphic.size, uom));

			if (graphic.imageURL == null) {
				img = renderMarkForFill(graphic.mark, graphic.size < 0 ? 6 : size, uom, graphic.rotation,
						graphics != null ? graphics.getRenderingHints() : null);
				graphicBounds = getImageBounds(img, graphic, 0, 0, uom);
			}
			else {
				img = new BufferedImage(size, size, TYPE_INT_ARGB);
				Graphics2D g = img.createGraphics();
				Java2DRenderer renderer = new Java2DRenderer(g);
				img = renderer.rendererContext.svgRenderer.prepareSvg(graphicBounds, graphic);
				g.dispose();
			}
		}
		else {
			img = graphic.image;
		}
		graphics.setPaint(new TexturePaint(img, graphicBounds));
	}

	void applyFill(Fill fill, UOM uom) {
		if (fill == null) {
			graphics.setPaint(new Color(0, 0, 0, 0));
			return;
		}

		if (fill.graphic == null) {
			graphics.setPaint(fill.color);
		}
		else {
			applyGraphicFill(fill.graphic, uom);
		}
	}

	Rectangle2D.Double getImageBounds(BufferedImage image, Graphic graphic, double x, double y, UOM uom) {
		double width, height;
		width = image.getWidth();
		height = image.getHeight();
		double x0 = x - width * graphic.anchorPointX + uomCalculator.considerUOM(graphic.displacementX, uom);
		double y0 = y - height * graphic.anchorPointY + uomCalculator.considerUOM(graphic.displacementY, uom);

		return new Rectangle2D.Double(x0, y0, width, height);
	}

	Rectangle2D.Double getGraphicBounds(Graphic graphic, double x, double y, UOM uom) {
		double width, height;
		if (graphic.image != null) {
			double max = Math.max(graphic.image.getWidth(), graphic.image.getHeight());
			double fac = graphic.size / max;
			width = fac * graphic.image.getWidth();
			height = fac * graphic.image.getHeight();
		}
		else {
			width = graphic.size;
			height = graphic.size;
		}
		width = uomCalculator.considerUOM(width, uom);
		height = uomCalculator.considerUOM(height, uom);

		if (width < 0) {
			if (graphic.image == null) {
				width = 6;
				height = 6;
			}
			else {
				width = graphic.image.getWidth();
				height = graphic.image.getHeight();
			}
		}

		double x0 = x - width * graphic.anchorPointX + uomCalculator.considerUOM(graphic.displacementX, uom);
		double y0 = y - height * graphic.anchorPointY + uomCalculator.considerUOM(graphic.displacementY, uom);

		return new Rectangle2D.Double(x0, y0, width, height);
	}

}
