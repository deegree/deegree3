/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2022 by:
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.style.styling.mark;

import static java.util.Objects.requireNonNull;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Extension of {@link Shape} which allows to specify the bounds explicitly
 */
public class BoundedShape implements Shape {

	private Shape shape;

	private Rectangle2D bounds = null;

	public BoundedShape(Shape shape) {
		requireNonNull(shape, "Shape can not be null");
		this.shape = shape;
	}

	/**
	 * Create Bounded Shape
	 * @param shp Source Shape
	 * @param bounds Explicit Bounds of Shape
	 * @return Bounded Shape
	 */
	public static BoundedShape of(Shape shp, Rectangle2D bounds) {
		BoundedShape s = new BoundedShape(shp);
		s.setBounds(bounds);
		return s;
	}

	/**
	 * Create Bounded Shape
	 * @param at AffineTransform which is altered by a y-scale of -1
	 * @param shp Source Shape
	 * @param bounds Explicit Bounds of Shape
	 * @return Bounded Shape
	 */
	public static BoundedShape of(AffineTransform at, Shape shp, Rectangle2D bounds) {
		BoundedShape s = new BoundedShape(at.createTransformedShape(shp));
		s.setBounds(bounds);
		return s;
	}

	/**
	 * Inverted bounded Shape
	 *
	 * Y-Axis is scaled with -1
	 * @param shp Source Shape
	 * @param bounds Explicit Bounds of Shape
	 * @return Bounded Shape
	 */
	public static BoundedShape inv(Shape shp, Rectangle2D bounds) {
		AffineTransform at = AffineTransform.getScaleInstance(1.0, -1.0);
		BoundedShape s = new BoundedShape(at.createTransformedShape(shp));
		s.setBounds(new Rectangle2D.Double(bounds.getX(), bounds.getY() * -1, bounds.getWidth(), bounds.getHeight()));

		return s;
	}

	/**
	 * Inverted bounded Shape
	 *
	 * Y-Axis is scaled with -1
	 * @param at AffineTransform which is altered by a y-scale of -1
	 * @param shp Source Shape
	 * @param bounds Explicit Bounds of Shape
	 * @return Bounded Shape
	 */
	public static BoundedShape inv(AffineTransform at, Shape shp, Rectangle2D bounds) {
		at.scale(1.0, -1.0);
		BoundedShape s = new BoundedShape(at.createTransformedShape(shp));
		s.setBounds(new Rectangle2D.Double(bounds.getX(), bounds.getY() * -1, bounds.getWidth(), bounds.getHeight()));

		return s;
	}

	/**
	 * Transformed Bounded Shape
	 *
	 * This transforms the shape and also his bounds
	 *
	 * <p>
	 * <b>NOTE: The transformation creates new bounds, which could create an undesired
	 * effect when rotated.</b>
	 * </p>
	 * @return The transformed bounded shape
	 */
	public BoundedShape transform(AffineTransform at) {
		if (this.bounds == null) {
			return of(at, this.shape, null);
		}
		Shape bnds = at.createTransformedShape(this.bounds);
		return of(at, this.shape, bnds.getBounds());
	}

	public void setBounds(Rectangle2D bounds) {
		this.bounds = bounds;
	}

	public boolean contains(double x, double y, double w, double h) {
		return shape.contains(x, y, w, h);
	}

	public boolean contains(double x, double y) {
		return shape.contains(x, y);
	}

	public boolean contains(Point2D p) {
		return shape.contains((Point2D) p);
	}

	public boolean contains(Rectangle2D r) {
		return shape.contains((Rectangle2D) r);
	}

	public Rectangle getBounds() {
		if (bounds != null)
			return new Rectangle((int) bounds.getMinX(), (int) bounds.getMinY(), (int) bounds.getWidth(),
					(int) bounds.getHeight());
		return shape.getBounds();
	}

	public Rectangle2D getBounds2D() {
		if (bounds != null)
			return bounds;
		return shape.getBounds2D();
	}

	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return shape.getPathIterator(at, flatness);
	}

	public PathIterator getPathIterator(AffineTransform at) {
		return shape.getPathIterator(at);
	}

	public boolean intersects(double x, double y, double w, double h) {
		return shape.intersects(x, y, w, h);
	}

	public boolean intersects(Rectangle2D r) {
		return shape.intersects(r);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BoundedShape) {
			BoundedShape other = (BoundedShape) obj;
			boolean result = shape.equals(other.shape);
			if (bounds == null)
				return result & (other.bounds == null);
			return result & bounds.equals(other.bounds);
		}
		else if (obj instanceof Shape) {
			if (bounds == null)
				return shape.equals(obj);
			return false;
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		int hascode = shape.hashCode();
		if (bounds != null)
			hascode += hascode * 37 + bounds.hashCode();
		return hascode;
	}

}