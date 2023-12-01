/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/

 (C) 2016, Open Source Geospatial Foundation (OSGeo)
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
----------------------------------------------------------------------------*/
package org.deegree.style.styling.wkn;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.deegree.style.styling.mark.BoundedShape;
import org.deegree.style.styling.mark.WellKnownNameLoader;

public class QGisShapeLoader implements WellKnownNameLoader {

	public static final String PREFIX = "qgis://";

	private static Map<String, Shape> SHAPES = null;

	private static synchronized void lazyLoad() {
		if (SHAPES == null) {
			SHAPES = new HashMap<>();
			GeneralPath gp;
			AffineTransform at;

			// copy of shape://plus
			gp = new GeneralPath();
			gp.moveTo(-0.5f, 0);
			gp.lineTo(0.5f, 0);
			gp.moveTo(0, -0.5f);
			gp.lineTo(0, 0.5f);
			SHAPES.put("cross", gp);

			// copy of circle
			SHAPES.put("circle", new Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0));

			gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
			gp.moveTo(0f, 1f);
			gp.lineTo(0.866f, -0.8f);
			gp.lineTo(-0.866f, -0.8f);
			gp.lineTo(0f, 1f);
			at = new AffineTransform();
			at.translate(0, 0.10);
			at.scale(0.5, 0.5);
			SHAPES.put("triangle", BoundedShape.inv(at, gp, new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			// copy of triangle
			gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
			gp.moveTo(0f, 1f);
			gp.lineTo(0.866f, -0.5f);
			gp.lineTo(-0.866f, -0.5f);
			gp.lineTo(0f, 1f);
			at = new AffineTransform();
			at.translate(0, 0.25);
			at.scale(0.5, 0.5);
			SHAPES.put("equilateral_triangle", BoundedShape.inv(at, gp, new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
			gp.moveTo(0f, 0.5f);
			gp.lineTo(0.2f, 0.1f);
			gp.lineTo(0.5f, 0.1f);
			gp.lineTo(0.2f, -0.1f);
			gp.lineTo(0.5f, -0.5f);
			gp.lineTo(0f, -0.2f);
			gp.lineTo(-0.5f, -0.5f);
			gp.lineTo(-0.2f, -0.1f);
			gp.lineTo(-0.5f, 0.1f);
			gp.lineTo(-0.2f, 0.1f);
			gp.lineTo(0f, 0.5f);
			SHAPES.put("star", BoundedShape.inv(gp, new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			// copy of shape://times
			gp = new GeneralPath();
			gp.moveTo(-0.5f, 0.5f);
			gp.lineTo(0.5f, -0.5f);
			gp.moveTo(-0.5f, -0.5f);
			gp.lineTo(0.5f, 0.5f);
			SHAPES.put("cross2", gp);

			gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
			gp.moveTo(0.3f, -0.3f);
			gp.lineTo(0.5f, 0.0f);
			gp.lineTo(0.3f, 0.3f);
			gp.lineTo(0.3f, 0.1f);
			gp.lineTo(-0.5f, 0.1f);
			gp.lineTo(-0.5f, -0.1f);
			gp.lineTo(0.3f, -0.1f);
			gp.lineTo(0.3f, -0.3f);
			at = new AffineTransform();
			at.rotate(-Math.PI / 2.0);
			SHAPES.put("arrow", BoundedShape.inv(at, gp, new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			gp = new GeneralPath();
			gp.moveTo(0f, 1f);
			gp.lineTo(1f, 0f);
			gp.lineTo(0f, -1f);
			gp.lineTo(-1f, 0f);
			gp.lineTo(0f, 1f);
			at = new AffineTransform();
			at.scale(0.5, 0.5);
			SHAPES.put("diamond", BoundedShape.inv(at, gp, new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			gp = new GeneralPath();
			gp.moveTo(0f, 1f);
			gp.lineTo(0.9511f, 0.3090f);
			gp.lineTo(0.5878f, -0.8090f);
			gp.lineTo(-0.5878f, -0.8090f);
			gp.lineTo(-0.9511f, 0.3090f);
			gp.lineTo(0f, 1f);
			at = new AffineTransform();
			at.scale(0.5, 0.5);
			SHAPES.put("pentagon", BoundedShape.inv(at, gp, new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			// copy of square
			SHAPES.put("rectangle", new Rectangle2D.Double(-0.5, -0.5, 1.0, 1.0));

			// copy of star
			gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
			gp.moveTo(-0.309f, -0.5f);
			gp.lineTo(-0.25f, -0.156f);
			gp.lineTo(-0.5f, 0.088f);
			gp.lineTo(-0.154f, 0.138f);
			gp.lineTo(0.0f, 0.451f);
			gp.lineTo(0.154f, 0.138f);
			gp.lineTo(0.5f, 0.088f);
			gp.lineTo(0.25f, -0.156f);
			gp.lineTo(0.309f, -0.5f);
			gp.lineTo(0.0f, -0.338);
			gp.lineTo(-0.309f, -0.5f);
			SHAPES.put("regular_star", BoundedShape.inv(gp, new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			// copy of shape://vertline
			SHAPES.put("line", new Line2D.Double(0, -0.5, 0, 0.5));

			gp = new GeneralPath();
			gp.moveTo(-0.5f, 0.4f);
			gp.lineTo(0, 0);
			gp.lineTo(-0.5f, -0.4f);
			SHAPES.put("arrowhead", BoundedShape.inv(gp, new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			gp = new GeneralPath();
			gp.moveTo(-0.5f, 0.4f);
			gp.lineTo(0, 0);
			gp.lineTo(-0.5f, -0.4f);
			gp.closePath();
			SHAPES.put("filled_arrowhead", BoundedShape.inv(gp, new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
			gp.moveTo(-0.5f, -0.1f);
			gp.lineTo(-0.5f, -0.1f);
			gp.lineTo(-0.5f, 0.1f);
			gp.lineTo(-0.1f, 0.1f);
			gp.lineTo(-0.1f, 0.5f);
			gp.lineTo(0.1f, 0.5f);
			gp.lineTo(0.1f, 0.1f);
			gp.lineTo(0.5f, 0.1f);
			gp.lineTo(0.5f, -0.1f);
			gp.lineTo(0.1f, -0.1f);
			gp.lineTo(0.1f, -0.5f);
			gp.lineTo(-0.1f, -0.5f);
			gp.lineTo(-0.1f, -0.1f);
			gp.lineTo(-0.5f, -0.1f);
			SHAPES.put("crossfill", BoundedShape.inv(gp, new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
			gp.moveTo(-0.5f, -0.5f);
			gp.lineTo(0.5f, -0.5f);
			gp.lineTo(-0.5f, 0.5f);
			gp.lineTo(-0.5f, -0.5f);
			SHAPES.put("diagonalhalfsquare", BoundedShape.inv(gp, new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			SHAPES.put("halfsquare", BoundedShape.inv(new Rectangle2D.Double(-0.5, -0.5, 0.5, 1.0),
					new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			gp = new GeneralPath();
			gp.moveTo(-0.4330f, -0.25f);
			gp.lineTo(-0.4330f, 0.25f);
			gp.lineTo(0f, 0.5f);
			gp.lineTo(0.4330f, 0.25f);
			gp.lineTo(0.4330f, -0.25f);
			gp.lineTo(0f, -0.5f);
			gp.lineTo(-0.4330f, -0.25f);
			SHAPES.put("hexagon", BoundedShape.inv(gp, new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
			gp.moveTo(0f, -0.5f);
			gp.lineTo(0f, 0.5f);
			gp.lineTo(0.5f, -0.5f);
			gp.lineTo(0f, -0.5f);
			SHAPES.put("lefthalftriangle", BoundedShape.inv(gp, new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
			gp.moveTo(0f, -0.5f);
			gp.lineTo(0f, 0.5f);
			gp.lineTo(-0.5f, -0.5f);
			gp.lineTo(0f, -0.5f);
			SHAPES.put("righthalftriangle", BoundedShape.inv(gp, new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			SHAPES.put("quartercircle",
					BoundedShape.inv(new Arc2D.Double(new Rectangle2D.Double(-0.5, -0.5, 1.0, 1.0), 180, 90, Arc2D.PIE),
							new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			SHAPES.put("semicircle",
					BoundedShape.inv(
							new Arc2D.Double(new Rectangle2D.Double(-0.5, -0.5, 1.0, 1.0), 180, 180, Arc2D.PIE),
							new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			SHAPES.put("thirdcircle",
					BoundedShape.inv(
							new Arc2D.Double(new Rectangle2D.Double(-0.5, -0.5, 1.0, 1.0), 150, 120, Arc2D.PIE),
							new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			SHAPES.put("quartersquare", BoundedShape.inv(new Rectangle2D.Double(-0.5, 0.0, 0.5, 0.5),
					new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

		}
	}

	@Override
	public Shape parse(String wellKnownName, Function<String, URL> resolver) {
		if (wellKnownName == null || !wellKnownName.startsWith(PREFIX))
			return null;

		String wkn = wellKnownName.substring(PREFIX.length()).toLowerCase();
		// debug//SHAPES = null;
		lazyLoad();

		return SHAPES.get(wkn);
	}

	@Override
	public int order() {
		return 1500;
	}

	public static void main(String[] args) {
		lazyLoad();
		String list = SHAPES.entrySet()
			.stream()//
			.map(Map.Entry::getKey) //
			.map(str -> PREFIX + str) //
			.collect(Collectors.joining(","));
		System.out.println(list);
	}

}
