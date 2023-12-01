/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/

 (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

public class ShapeLoader implements WellKnownNameLoader {

	public static final String PREFIX = "shape://";

	private static Map<String, Shape> SHAPES = null;

	private static synchronized void lazyLoad() {
		if (SHAPES == null) {
			SHAPES = new HashMap<>();
			GeneralPath gp;
			AffineTransform inv = AffineTransform.getScaleInstance(1.0, -1.0);

			SHAPES.put("vertline", new Line2D.Double(0, -0.5, 0, 0.5));
			SHAPES.put("horline", new Line2D.Double(-0.5, 0, 0.5, 0));
			SHAPES.put("slash", new Line2D.Double(-0.5, 0.5, 0.5, -0.5));
			SHAPES.put("backslash", new Line2D.Double(-0.5, -0.5, 0.5, 0.5));

			gp = new GeneralPath();

			SHAPES.put("dot", BoundedShape.inv(new Ellipse2D.Double(-0.000001, -0.000001, 0.000001, 0.000001),
					new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0)));

			gp = new GeneralPath();
			gp.moveTo(-0.5f, 0);
			gp.lineTo(0.5f, 0);
			gp.moveTo(0, -0.5f);
			gp.lineTo(0, 0.5f);
			SHAPES.put("plus", inv.createTransformedShape(gp));

			gp = new GeneralPath();
			gp.moveTo(-0.5f, 0.5f);
			gp.lineTo(0.5f, -0.5f);
			gp.moveTo(-0.5f, -0.5f);
			gp.lineTo(0.5f, 0.5f);
			SHAPES.put("times", inv.createTransformedShape(gp));

			gp = new GeneralPath();
			gp.moveTo(-0.5f, 0.2f);
			gp.lineTo(0, 0);
			gp.lineTo(-0.5f, -0.2f);
			SHAPES.put("oarrow", BoundedShape.inv(gp, new Rectangle2D.Double(-0.5, 0.5, 1, 1)));

			gp = new GeneralPath();
			gp.moveTo(-0.5f, 0.2f);
			gp.lineTo(0, 0);
			gp.lineTo(-0.5f, -0.2f);
			gp.closePath();
			SHAPES.put("carrow", BoundedShape.inv(gp, new Rectangle2D.Double(-0.5, 0.5, 1, 1)));

			gp = new GeneralPath();
			gp.moveTo(-0.5f, 0.3f);
			gp.lineTo(0.5, 0);
			gp.lineTo(-0.5f, -0.3f);
			SHAPES.put("coarrow", BoundedShape.inv(gp, new Rectangle2D.Double(-0.32, 0.3, 0.6, 0.6)));

			gp = new GeneralPath();
			gp.moveTo(-0.5f, 0.3f);
			gp.lineTo(0.5, 0);
			gp.lineTo(-0.5f, -0.3f);
			gp.closePath();
			SHAPES.put("ccarrow", BoundedShape.inv(gp, new Rectangle2D.Double(-0.32, 0.3, 0.6, 0.6)));
		}
	}

	@Override
	public Shape parse(String wellKnownName, Function<String, URL> resolver) {
		if (wellKnownName == null || !wellKnownName.startsWith(PREFIX))
			return null;

		String wkn = wellKnownName.substring(PREFIX.length()).toLowerCase();

		lazyLoad();
		return SHAPES.get(wkn);
	}

	@Override
	public int order() {
		return 500;
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