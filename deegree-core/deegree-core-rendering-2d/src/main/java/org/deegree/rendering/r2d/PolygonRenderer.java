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

import static java.awt.geom.Path2D.WIND_EVEN_ODD;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D.Double;
import java.util.LinkedList;

import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.style.styling.LineStyling;
import org.deegree.style.styling.PointStyling;
import org.deegree.style.styling.PolygonStyling;

/**
 * <code>PolygonRenderer</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class PolygonRenderer {

	private GeometryHelper geomHelper;

	private Java2DFillRenderer fillRenderer;

	private Java2DStrokeRenderer strokeRenderer;

	private Graphics2D graphics;

	private Java2DRenderer renderer;

	PolygonRenderer(GeometryHelper geomHelper, Java2DFillRenderer fillRenderer, Java2DStrokeRenderer strokeRenderer,
			Graphics2D graphics, Java2DRenderer renderer) {
		this.geomHelper = geomHelper;
		this.fillRenderer = fillRenderer;
		this.strokeRenderer = strokeRenderer;
		this.graphics = graphics;
		this.renderer = renderer;
	}

	void render(PolygonStyling styling, Surface surface) {
		for (SurfacePatch patch : surface.getPatches()) {
			if (patch instanceof PolygonPatch) {
				LinkedList<Double> lines = new LinkedList<Double>();
				PolygonPatch polygonPatch = (PolygonPatch) patch;

				// just appending the holes appears to work, the Java2D rendering
				// mechanism can determine that they lie
				// inside and thus no substraction etc. is needed. This speeds up things
				// SIGNIFICANTLY
				GeneralPath polygon = new GeneralPath(WIND_EVEN_ODD);
				for (Curve curve : polygonPatch.getBoundaryRings()) {
					Double d = geomHelper.fromCurve(curve, true);
					lines.add(d);
					polygon.append(d, false);
				}

				fillRenderer.applyFill(styling.fill, styling.uom);
				graphics.fill(polygon);
				for (Double d : lines) {
					strokeRenderer.applyStroke(styling.stroke, styling.uom, d, styling.perpendicularOffset,
							styling.perpendicularOffsetType);
				}
			}
			else {
				throw new IllegalArgumentException("Cannot render non-planar surfaces.");
			}
		}
	}

	void render(PointStyling styling, Surface surface) {
		for (SurfacePatch patch : surface.getPatches()) {
			if (patch instanceof PolygonPatch) {
				PolygonPatch polygonPatch = (PolygonPatch) patch;
				for (Curve curve : polygonPatch.getBoundaryRings()) {
					curve.setCoordinateSystem(surface.getCoordinateSystem());
					renderer.render(styling, curve);
				}
			}
			else {
				throw new IllegalArgumentException("Cannot render non-planar surfaces.");
			}
		}
	}

	void render(LineStyling styling, Surface surface) {
		for (SurfacePatch patch : surface.getPatches()) {
			if (patch instanceof PolygonPatch) {
				PolygonPatch polygonPatch = (PolygonPatch) patch;
				for (Curve curve : polygonPatch.getBoundaryRings()) {
					if (curve.getCoordinateSystem() == null) {
						curve.setCoordinateSystem(surface.getCoordinateSystem());
					}
					renderer.render(styling, curve);
				}
			}
			else {
				throw new IllegalArgumentException("Cannot render non-planar surfaces.");
			}
		}
	}

}
