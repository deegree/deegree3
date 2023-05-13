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
package org.deegree.style.styling.wkn.shape;

import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.toDegrees;

import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;

import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.segments.ArcString;
import org.deegree.geometry.primitive.segments.Circle;
import org.deegree.geometry.primitive.segments.CubicSpline;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.primitive.segments.LineStringSegment;

public class ShapeConverterArc extends AbstractShapeConverter {

	private void toShape(GeneralPath path, Points points) {
		toShape(path, points, false);
	}

	private void toShape(GeneralPath path, Points points, boolean close) {
		Iterator<Point> iter = points.iterator();
		Point p;
		while (iter.hasNext()) {
			p = iter.next();
			toShape(path, p);
		}
	}

	private void toShape(GeneralPath path, Point p) {
		Point2D pnt = path.getCurrentPoint();
		if (pnt == null) {
			path.moveTo(p.get0(), p.get1());
		}
		else {
			path.lineTo(p.get0(), p.get1());
		}
	}

	@Override
	protected void toShape(GeneralPath path, Curve geometry) {
		switch (geometry.getCurveType()) {
			case LineString: {
				// both LineString and LinearRing are handled by this case
				toShape(path, ((LineString) geometry).getControlPoints());
				break;
			}
			default: {
				if (geometry instanceof Ring) {
					Ring ring = (Ring) geometry;
					List<Curve> curves = ring.getMembers();
					for (Curve member : curves) {
						toShape(path, member);
					}
				}
				else {
					List<CurveSegment> segments = geometry.getCurveSegments();
					for (CurveSegment member : segments) {
						toShape(path, member);
					}
				}
				break;
			}
		}
	}

	private void toShape(GeneralPath path, CurveSegment segment) {
		switch (segment.getSegmentType()) {
			case LINE_STRING_SEGMENT: {
				toShape(path, ((LineStringSegment) segment).getControlPoints());
				break;
			}
			case CIRCLE: {
				toShape(path, (Circle) segment);
				break;
			}
			case ARC:
			case ARC_STRING: {
				toShape(path, (ArcString) segment);
				break;
			}
			case CUBIC_SPLINE: {
				toShape(path, (CubicSpline) segment);
				break;
			}
			case GEODESIC_STRING:
			case ARC_BY_BULGE:
			case ARC_BY_CENTER_POINT: // should be possible
			case ARC_STRING_BY_BULGE:
			case BEZIER:
			case BSPLINE:
			case CIRCLE_BY_CENTER_POINT:
			case CLOTHOID:
			case GEODESIC:
			case OFFSET_CURVE: {
				String msg = "Handling of curve segment type '" + segment.getSegmentType().name()
						+ "' is not implemented yet.";
				throw new IllegalArgumentException(msg);
			}
		}
	}

	private void toShape(GeneralPath path, CubicSpline segment) {
		// TODO needs to be tested
		CubicCurve2D.Double cubic;
		cubic = new CubicCurve2D.Double(segment.getStartPoint().get0(), segment.getStartPoint().get1(),
				segment.getVectorAtStart().get0(), segment.getVectorAtStart().get1(), segment.getVectorAtEnd().get0(),
				segment.getVectorAtEnd().get1(), segment.getEndPoint().get0(), segment.getEndPoint().get1());
		path.append(cubic, true);
	}

	private static Point2D.Double getCircleCenter(Point a, Point b, Point c) {
		double ax = a.get0();
		double ay = a.get1();
		double bx = b.get0();
		double by = b.get1();
		double cx = c.get0();
		double cy = c.get1();

		double A = bx - ax;
		double B = by - ay;
		double C = cx - ax;
		double D = cy - ay;

		double E = A * (ax + bx) + B * (ay + by);
		double F = C * (ax + cx) + D * (ay + cy);

		double G = 2 * (A * (cy - by) - B * (cx - bx));
		if (G == 0.0)
			return null; // a, b, c must be collinear

		double px = (D * E - B * F) / G;
		double py = (A * F - C * E) / G;
		return new Point2D.Double(px, py);
	}

	private double postiveAngle(double angle) {
		while (angle < 0.0d) {
			angle += 360;
		}
		return angle;
	}

	private double getNearestAnglePhase(double limit, double source, int dir) {
		double value = source;
		if (dir > 0) {
			while (value < limit) {
				value += 360.0;
			}
		}
		else if (dir < 0) {
			while (value > limit) {
				value -= 360.0;
			}
		}
		return value;
	}

	private void toShape(GeneralPath path, Circle segment) {
		// TODO needs to be tested
		Point2D.Double center;
		Point beg = segment.getPoint1();
		Point mid = segment.getPoint2();
		Point end = segment.getPoint3();
		center = getCircleCenter(beg, mid, end);
		if (center == null) {
			toShape(path, beg);
			toShape(path, mid);
			toShape(path, end);
		}
		else {
			double r = center.distance(beg.get0(), beg.get1());
			double minx = center.x - r;
			double miny = center.y - r;

			Ellipse2D.Double ellips = new Ellipse2D.Double(minx, miny, 2 * r, 2 * r);
			path.append(ellips, false);
		}
	}

	private void toShape(GeneralPath path, ArcString segment) {
		Points points = segment.getControlPoints();
		Point2D.Double center;
		for (int i = 0, j = points.size() - 2; i < j; i += 2) {
			Point beg = points.get(i);
			Point mid = points.get(i + 1);
			Point end = points.get(i + 2);
			center = getCircleCenter(beg, mid, end);
			if (center == null) {
				toShape(path, beg);
				toShape(path, mid);
				toShape(path, end);
			}
			else {
				double r = center.distance(beg.get0(), beg.get1());
				double minx = center.x - r;
				double miny = center.y - r;

				double begAngle = postiveAngle(toDegrees(-atan2(beg.get1() - center.y, beg.get0() - center.x)));
				double midAngle = postiveAngle(toDegrees(-atan2(mid.get1() - center.y, mid.get0() - center.x)));
				double endAngle = postiveAngle(toDegrees(-atan2(end.get1() - center.y, end.get0() - center.x)));

				double midDecreasing = getNearestAnglePhase(begAngle, midAngle, -1);
				double midIncreasing = getNearestAnglePhase(begAngle, midAngle, 1);
				double endDecreasing = getNearestAnglePhase(midDecreasing, endAngle, -1);
				double endIncreasing = getNearestAnglePhase(midIncreasing, endAngle, 1);

				double extent = 0;
				if (abs(endDecreasing - begAngle) < abs(endIncreasing - begAngle)) {
					extent = endDecreasing - begAngle;
				}
				else {
					extent = endIncreasing - begAngle;
				}
				Arc2D.Double arc2d = new Arc2D.Double(minx, miny, 2 * r, 2 * r, begAngle, extent, Arc2D.OPEN);
				path.append(arc2d, true);
			}
		}
	}

}
