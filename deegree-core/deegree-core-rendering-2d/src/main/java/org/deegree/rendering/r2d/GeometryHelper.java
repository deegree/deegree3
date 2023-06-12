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

import static org.deegree.commons.utils.math.MathUtils.isZero;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D.Double;
import java.util.Iterator;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.linearization.GeometryLinearizer;
import org.deegree.geometry.linearization.NumPointsCriterion;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Surface;
import org.slf4j.Logger;

/**
 * Used to transform, linearize, clip and fix geometry orientation for rendering.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class GeometryHelper {

	private static final Logger LOG = getLogger(GeometryHelper.class);

	private static final GeometryLinearizer linearizer = new GeometryLinearizer();

	private GeometryTransformer transformer;

	private AffineTransform worldToScreen;

	GeometryHelper(Envelope bbox, int width, AffineTransform worldToScreen) {
		this.worldToScreen = worldToScreen;
		try {
			if (bbox.getCoordinateSystem() != null && (!bbox.getCoordinateSystem().getAlias().equals("CRS:1"))) {
				transformer = new GeometryTransformer(bbox.getCoordinateSystem());
			}
		}
		catch (Throwable e) {
			LOG.debug("Stack trace:", e);
			LOG.warn(
					"Setting up the renderer yielded an exception when setting up internal transformer. This may lead to problems.");
		}
	}

	Double fromCurve(Curve curve, boolean close) {
		Double line = new Double();

		// TODO use error criterion
		ICRS crs = curve.getCoordinateSystem();
		curve = linearizer.linearize(curve, new NumPointsCriterion(100));
		curve.setCoordinateSystem(crs);
		Points points = curve.getControlPoints();
		Iterator<Point> iter = points.iterator();
		Point p = iter.next();
		double x = p.get0(), y = p.get1();
		line.moveTo(x, y);
		while (iter.hasNext()) {
			p = iter.next();
			if (iter.hasNext()) {
				line.lineTo(p.get0(), p.get1());
			}
			else {
				if (close && isZero(x - p.get0()) && isZero(y - p.get1())) {
					line.closePath();
				}
				else {
					line.lineTo(p.get0(), p.get1());
				}
			}
		}

		line.transform(worldToScreen);

		return line;
	}

	<T extends Geometry> T transform(T g) {
		if (g == null) {
			LOG.warn("Trying to transform null geometry.");
			return null;
		}
		if (g.getCoordinateSystem() == null) {
			LOG.warn("Geometry of type '{}' had null coordinate system.", g.getClass().getSimpleName());
			return g;
		}
		if (transformer != null) {
			ICRS crs = null;
			try {
				crs = ((Geometry) g).getCoordinateSystem();
				if (transformer.getTargetCRS().equals(crs)) {
					return g;
				}
				T g2 = transformer.transform(g);
				if (g2 == null) {
					LOG.warn("Geometry transformer returned null for geometry of type {}, crs was {}.",
							g.getClass().getSimpleName(), crs);
					return g;
				}
				return g2;
			}
			catch (IllegalArgumentException e) {
				T g2 = transformLinearized(g);

				if (g2 != null) {
					return g2;
				}

				LOG.debug("Stack trace:", e);
				LOG.warn("Could not transform geometry of type '{}' before rendering, "
						+ "this may lead to problems. CRS was {}.", g.getClass().getSimpleName(), crs);
			}
			catch (TransformationException e) {
				LOG.debug("Stack trace:", e);
				LOG.warn("Could not transform geometry of type '{}' before rendering, "
						+ "this may lead to problems. CRS was {}.", g.getClass().getSimpleName(), crs);
			}
			catch (UnknownCRSException e) {
				LOG.debug("Stack trace:", e);
				LOG.warn("Could not transform geometry of type '{}' before rendering, "
						+ "this may lead to problems. CRS was {}.", g.getClass().getSimpleName(), crs);
			}
		}
		return g;
	}

	private <T extends Geometry> T transformLinearized(T g) {
		if (g instanceof Surface) {
			@SuppressWarnings("unchecked")
			T g2 = (T) transform(linearizer.linearize((Surface) g, new NumPointsCriterion(100)));
			g2.setCoordinateSystem(g.getCoordinateSystem());
			return g2;
		}
		if (g instanceof Curve) {
			@SuppressWarnings("unchecked")
			T g2 = (T) transform(linearizer.linearize((Curve) g, new NumPointsCriterion(100)));
			g2.setCoordinateSystem(g.getCoordinateSystem());
			return g2;
		}
		return null;
	}

}
