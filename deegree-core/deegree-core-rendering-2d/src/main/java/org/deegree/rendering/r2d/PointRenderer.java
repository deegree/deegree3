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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.rendering.r2d;

import static java.lang.Math.toRadians;
import static org.deegree.commons.utils.math.MathUtils.isZero;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.rendering.r2d.RenderHelper.renderMark;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.deegree.style.styling.PointStyling;
import org.deegree.style.styling.components.Graphic;

/**
 * <code>PointRenderer</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class PointRenderer {

	private AffineTransform worldToScreen;

	private RendererContext rendererContext;

	PointRenderer(AffineTransform worldToScreen, RendererContext rendererContext) {
		this.worldToScreen = worldToScreen;
		this.rendererContext = rendererContext;
	}

	void render(PointStyling styling, double x, double y) {
		Point2D.Double p = (Point2D.Double) worldToScreen.transform(new Point2D.Double(x, y), null);
		x = p.x;
		y = p.y;

		Graphic g = styling.graphic;
		Rectangle2D.Double rect = rendererContext.fillRenderer.getGraphicBounds(g, x, y, styling.uom);

		if (g.image == null && g.imageURL == null) {
			renderMark(g.mark, g.size < 0 ? 6 : round(rendererContext.uomCalculator.considerUOM(g.size, styling.uom)),
					styling.uom, rendererContext, rect.getMinX(), rect.getMinY(), g.rotation);
			return;
		}

		BufferedImage img = g.image;

		// try if it's an svg
		if (img == null && g.imageURL != null) {
			img = rendererContext.svgRenderer.prepareSvg(rect, g);
		}

		if (img != null) {
			// TODO: fix rotation if anchor point is not 0.5,0.5 - see
			// org.deegree.rendering.r2d.Java2DRendererTest.testPointStyling()
			AffineTransform t = rendererContext.graphics.getTransform();
			if (!isZero(g.rotation)) {
				int rotationPointX = round(rect.x + rect.getWidth() * g.anchorPointX);
				int rotationPointY = round(rect.y + rect.getHeight() * g.anchorPointY);
				rendererContext.graphics.rotate(toRadians(g.rotation), rotationPointX, rotationPointY);
			}
			rendererContext.graphics.drawImage(img, round(rect.x), round(rect.y), round(rect.width), round(rect.height),
					null);
			rendererContext.graphics.setTransform(t);
		}
	}

}
