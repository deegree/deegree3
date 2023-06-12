/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/

 (C) 2002-2011, Open Source Geospatial Foundation (OSGeo)
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

import static java.util.Collections.emptyMap;
import static org.deegree.commons.utils.kvp.KVPUtils.getDefaultDouble;
import static org.deegree.commons.utils.kvp.KVPUtils.getNormalizedKVPMap;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.deegree.style.styling.mark.BoundedShape;
import org.deegree.style.styling.mark.WellKnownNameLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtShapeLoader implements WellKnownNameLoader {

	private static final Logger LOG = LoggerFactory.getLogger(ExtShapeLoader.class);

	public static final String PREFIX = "extshape://";

	private static Map<String, Shape> SHAPES = null;

	private static synchronized void lazyLoad() {
		if (SHAPES == null) {
			SHAPES = new HashMap<>();
			GeneralPath gp;
			AffineTransform inv = AffineTransform.getScaleInstance(1.0, -1.0);

			gp = new GeneralPath();
			gp.moveTo(-0.145f, 0.000f);
			gp.lineTo(0.000f, 0.175f);
			gp.lineTo(0.105f, 0.000f);
			gp.closePath();
			SHAPES.put("triangle", BoundedShape.inv(gp, new Rectangle2D.Double(-0.25, 0.25, 0.5, 0.5)));

			gp = new GeneralPath();
			gp.moveTo(-0.125f, 0.000f);
			gp.curveTo(-0.125f, 0.000f, 0.000f, 0.250f, 0.125f, 0.000f);
			gp.closePath();
			SHAPES.put("emicircle", BoundedShape.inv(gp, new Rectangle2D.Double(-0.25, 0.25, 0.5, 0.5)));

			gp = new GeneralPath();
			gp.moveTo(-0.395f, 0.000f);
			gp.lineTo(-0.250f, -0.175f);
			gp.lineTo(-0.145f, 0.000f);
			gp.moveTo(0.125f, 0.000f);
			gp.curveTo(0.125f, 0.000f, 0.250f, 0.250f, 0.375f, 0.000f);
			gp.closePath();
			SHAPES.put("triangleemicircle", BoundedShape.inv(gp, new Rectangle2D.Double(-0.25, 0.25, 0.5, 0.5)));

			gp = new GeneralPath();
			gp.moveTo(0f, 1f);
			gp.lineTo(0.5, 0f);
			gp.lineTo(0.1f, 0f);
			gp.lineTo(0.1f, -1f);
			gp.lineTo(-0.1f, -1f);
			gp.lineTo(-0.1f, 0f);
			gp.lineTo(-0.5f, 0f);
			gp.closePath();
			SHAPES.put("narrow", inv.createTransformedShape(gp));

			AffineTransform at = AffineTransform.getQuadrantRotateInstance(2);
			at.scale(1.0, -1.0);
			gp = new GeneralPath();
			gp.moveTo(0f, 1f);
			gp.lineTo(0.5, 0f);
			gp.lineTo(0.1f, 0f);
			gp.lineTo(0.1f, -1.0f);
			gp.lineTo(-0.1f, -1.0f);
			gp.lineTo(-0.1f, 0f);
			gp.lineTo(-0.5f, 0f);
			gp.closePath();
			SHAPES.put("sarrow", at.createTransformedShape(gp));

			SHAPES.put("arrow", buildDynamicArrow(2, 0.2f, 0.5f));
		}
	}

	@Override
	public Shape parse(String wellKnownName, Function<String, URL> resolver) {
		if (wellKnownName == null || !wellKnownName.startsWith(PREFIX))
			return null;

		String wkn = wellKnownName.substring(PREFIX.length()).toLowerCase();
		Shape s;

		if (wkn.startsWith("arrow?")) {
			Map<String, String> kvp;
			try {
				kvp = getNormalizedKVPMap(wkn.substring(6), null);
			}
			catch (UnsupportedEncodingException e) {
				LOG.warn("Cloud not parse arrow? WellKnownName {}: {}", wkn, e.getMessage());
				LOG.trace("Exception", e);
				kvp = emptyMap();
			}
			float heightRatio = rangeValue(kvp, "HR", 1.0f, 0.0f, 1000.0f);
			float thicknessRatio = rangeValue(kvp, "T", 0.2f, 0.0f, 1.0f);
			float baseRatio = rangeValue(kvp, "AB", 0.5f, 0.0f, 1.0f);

			s = buildDynamicArrow(heightRatio, thicknessRatio, baseRatio);
		}
		else {
			// debug//SHAPES=null;
			lazyLoad();
			s = SHAPES.get(wkn);
		}

		return s;
	}

	private float rangeValue(Map<String, String> kvp, String key, float defaultValue, float min, float max) {
		float val = (float) getDefaultDouble(kvp, key, defaultValue);
		if (val < min || val > max)
			return defaultValue;
		return val;
	}

	private static Shape buildDynamicArrow(float height, float thickness, float arrowBase) {
		GeneralPath gp = new GeneralPath();
		// start from the point of the arrow
		gp.moveTo(0f, height / 2);
		// the right base of the arrow
		float arrowBaseHeight = height / 2 - height * (1 - arrowBase);
		gp.lineTo(0.5, arrowBaseHeight);
		// back to the center
		float t2 = thickness / 2;
		if (t2 < 0.5) {
			gp.lineTo(t2, arrowBaseHeight);
		}
		// down to the base
		gp.lineTo(t2, -height / 2);
		if (t2 > 0) {
			// go the the other side of the base
			gp.lineTo(-t2, -height / 2);
		}
		// back up to the arrow base
		if (t2 < 0.5) {
			gp.lineTo(-t2, arrowBaseHeight);
		}
		gp.lineTo(-0.5f, arrowBaseHeight);
		// and finish
		gp.closePath();

		return AffineTransform.getScaleInstance(1.0, -1.0).createTransformedShape(gp);
	}

	@Override
	public int order() {
		return 5000;
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