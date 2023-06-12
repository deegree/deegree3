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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.deegree.geometry.Envelope;
import org.deegree.style.utils.UomCalculator;

/**
 * <code>RendererContext</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class RendererContext {

	GeometryHelper geomHelper;

	GeometryClipper clipper;

	public UomCalculator uomCalculator;

	Java2DFillRenderer fillRenderer;

	Java2DStrokeRenderer strokeRenderer;

	SvgRenderer svgRenderer;

	PolygonRenderer polygonRenderer;

	CurveRenderer curveRenderer;

	Graphics2D graphics;

	PointRenderer pointRenderer;

	RendererContext(double pixelSize, double res, Graphics2D graphics, Java2DRenderer renderer, Envelope bbox,
			int width, AffineTransform worldToScreen) {
		this.graphics = graphics;
		if (bbox != null) {
			geomHelper = new GeometryHelper(bbox, width, worldToScreen);
			clipper = new GeometryClipper(bbox, width);
		}
		uomCalculator = new UomCalculator(pixelSize, res);
		fillRenderer = new Java2DFillRenderer(uomCalculator, graphics);
		strokeRenderer = new Java2DStrokeRenderer(graphics, uomCalculator, fillRenderer, this);
		svgRenderer = new SvgRenderer();
		polygonRenderer = new PolygonRenderer(geomHelper, fillRenderer, strokeRenderer, graphics, renderer);
		curveRenderer = new CurveRenderer(renderer);
		pointRenderer = new PointRenderer(renderer.worldToScreen, this);
	}

}
