/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

 e-mail: info@deegree.org
----------------------------------------------------------------------------*/
/*
 Copyright 2006 Jerry Huxtable

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.deegree.rendering.r2d.strokes;

import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;
import static org.deegree.commons.utils.math.MathUtils.isZero;
import static org.deegree.commons.utils.math.MathUtils.round;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import org.deegree.style.styling.components.Graphic;

/**
 * <code>ShapeStroke</code>
 *
 * @author Jerry Huxtable
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class ShapeStroke implements Stroke {

	private Shape shapes[];

	private double advance;

	private boolean repeat = true;

	private boolean rotate = true;

	private AffineTransform t = new AffineTransform();

	private double positionPercentage;

	private double initialGap;

	private static final float FLATNESS = 1;

	private class Counter {

		private int value = 0;

	}

	private interface StrokeResult {

		boolean append(float x, float y, double rotation);

	}

	/**
	 * @param shapes
	 * @param advance
	 * @param positionPercentage
	 * @param initialGap
	 */
	public ShapeStroke(Shape shapes, double advance, double positionPercentage, double initialGap) {
		this(new Shape[] { shapes }, advance, positionPercentage, initialGap);
	}

	/**
	 * @param shapes
	 * @param advance
	 * @param positionPercentage
	 * @param initialGap
	 */
	public ShapeStroke(Shape shapes[], double advance, double positionPercentage, double initialGap) {
		this(shapes, advance, positionPercentage, initialGap, 0.5, 0.5, 0, 0, false);
	}

	public ShapeStroke(Shape shapes, double advance, double positionPercentage, double initialGap, double anchorPointX,
			double anchorPointY, double displacementX, double displacementY, boolean rotate) {
		this(new Shape[] { shapes }, advance, positionPercentage, initialGap, anchorPointX, anchorPointY, displacementX,
				displacementY, rotate);
	}

	public ShapeStroke(Shape shapes[], double advance, double positionPercentage, double initialGap,
			double anchorPointX, double anchorPointY, double displacementX, double displacementY, boolean rotate) {
		this.advance = advance;
		this.shapes = new Shape[shapes.length];
		this.positionPercentage = positionPercentage;
		this.repeat = positionPercentage < 0;
		this.initialGap = initialGap;
		this.rotate = rotate;

		for (int i = 0; i < this.shapes.length; i++) {
			Rectangle2D bounds = shapes[i].getBounds2D();
			double translateX = bounds.getX() + bounds.getWidth() * anchorPointX + displacementX;
			double translateY = bounds.getY() + bounds.getHeight() * anchorPointY + displacementY;
			t.setToTranslation(-translateX, -translateY);
			this.shapes[i] = t.createTransformedShape(shapes[i]);
		}
	}

	@Override
	public Shape createStrokedShape(Shape shape) {
		GeneralPath result = new GeneralPath();
		PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), FLATNESS);

		// a little sub optimal to actually go through twice
		double totalLength = 0;
		if (positionPercentage >= 0) {
			totalLength = calculatePathLength(it);
			it = new FlatteningPathIterator(shape.getPathIterator(null), FLATNESS);
		}

		float next = 0;
		float minLength = (float) initialGap;
		if (positionPercentage >= 0) {
			minLength = (float) (totalLength * (positionPercentage / 100));
			next = minLength;
		}

		Counter currentShape = new Counter();
		final int length = shapes.length;

		createStrokedShape((x, y, angle) -> {
			t.setToTranslation(x, y);
			if (this.rotate) {
				t.rotate(angle);
			}

			result.append(t.createTransformedShape(shapes[currentShape.value]), false);
			currentShape.value++;
			if (currentShape.value >= length && repeat) {
				currentShape.value = 0;
				return false;
			}
			else {
				return true;
			}
		}, it, next, minLength);

		return result;
	}

	/**
	 * Draw a {@code Image} along a {@code Shape}
	 * @param shape Shape to render along
	 * @param graphics Graphics context
	 * @param img The image
	 * @param g Graphics for anchor point and rotation
	 * @param rect Rectangle describing the image
	 */
	public void renderStroke(Shape shape, Graphics2D graphics, Image img, Graphic g, Rectangle2D.Double rect) {
		PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), FLATNESS);

		// a little sub optimal to actually go through twice
		double totalLength = 0;
		if (positionPercentage >= 0) {
			totalLength = calculatePathLength(it);
			it = new FlatteningPathIterator(shape.getPathIterator(null), FLATNESS);
		}

		float next = 0;
		float minLength = (float) initialGap;
		if (positionPercentage >= 0) {
			minLength = (float) (totalLength * (positionPercentage / 100));
			next = minLength;
		}
		double graphicsRotation = toRadians(g.rotation);

		createStrokedShape((x, y, angle) -> {
			double rotation = this.rotate ? graphicsRotation + angle : graphicsRotation;
			AffineTransform t = graphics.getTransform();
			if (!isZero(rotation)) {
				int rotationPointX = round(x + rect.x + rect.getWidth() * g.anchorPointX);
				int rotationPointY = round(y + rect.y + rect.getHeight() * g.anchorPointY);
				graphics.rotate(rotation, rotationPointX, rotationPointY);
			}
			graphics.drawImage(img, round(x + rect.x), round(y + rect.y), round(rect.width), round(rect.height), null);
			graphics.setTransform(t);
			return !repeat;
		}, it, next, minLength);
	}

	private void createStrokedShape(StrokeResult result, PathIterator it, float next, float minLength) {
		int type = 0;
		float moveX = 0, moveY = 0;
		float lastX = 0, lastY = 0;
		float thisX = 0, thisY = 0;
		float points[] = new float[6];
		boolean stop = false;

		while (!stop && !it.isDone()) {
			type = it.currentSegment(points);
			switch (type) {
				case PathIterator.SEG_MOVETO:
					moveX = lastX = points[0];
					moveY = lastY = points[1];
					next = minLength;
					break;

				case PathIterator.SEG_CLOSE:
					points[0] = moveX;
					points[1] = moveY;
					// Fall into....

				case PathIterator.SEG_LINETO:
					thisX = points[0];
					thisY = points[1];
					float dx = thisX - lastX;
					float dy = thisY - lastY;
					float distance = (float) Math.sqrt(dx * dx + dy * dy);
					if (distance >= next) {
						float r = 1.0f / distance;
						double angle = Math.atan2(dy, dx);
						while (!stop && distance >= next) {
							float x = lastX + next * dx * r;
							float y = lastY + next * dy * r;
							stop = result.append(x, y, angle);
							next += advance;
						}
					}
					next -= distance;
					lastX = thisX;
					lastY = thisY;
					break;
			}
			it.next();
		}
	}

	private double calculatePathLength(PathIterator it) {
		double totalLength = 0;
		double lx = 0, ly = 0;
		while (!it.isDone()) {
			float[] ps = new float[6];
			int type = it.currentSegment(ps);
			switch (type) {
				case PathIterator.SEG_MOVETO:
					lx = ps[0];
					ly = ps[1];
					break;

				case PathIterator.SEG_CLOSE:
					break;

				case PathIterator.SEG_LINETO:
					totalLength += sqrt((lx - ps[0]) * (lx - ps[0]) + (ly - ps[1]) * (ly - ps[1]));
					lx = ps[0];
					ly = ps[1];
					break;
			}
			it.next();
		}
		return totalLength;
	}

}
