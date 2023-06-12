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

package org.deegree.geometry.utils;

import static java.awt.geom.PathIterator.SEG_CLOSE;
import static java.awt.geom.PathIterator.SEG_CUBICTO;
import static java.awt.geom.PathIterator.SEG_LINETO;
import static java.awt.geom.PathIterator.SEG_MOVETO;
import static java.awt.geom.PathIterator.SEG_QUADTO;
import static java.lang.Math.sqrt;

import java.awt.Shape;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deegree.cs.coordinatesystems.CRS;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.geometry.standard.multi.DefaultMultiGeometry;
import org.deegree.geometry.standard.multi.DefaultMultiLineString;
import org.deegree.geometry.standard.multi.DefaultMultiPoint;
import org.deegree.geometry.standard.multi.DefaultMultiPolygon;
import org.deegree.geometry.standard.points.JTSPoints;
import org.deegree.geometry.standard.points.PointsArray;
import org.deegree.geometry.standard.points.PointsList;
import org.deegree.geometry.standard.primitive.DefaultLineString;
import org.deegree.geometry.standard.primitive.DefaultLinearRing;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.geometry.standard.primitive.DefaultPolygon;
import org.locationtech.jts.geom.CoordinateSequence;

/**
 * <code>GeometryUtils</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class GeometryUtils {

	private static GeometryFactory fac = new GeometryFactory();

	private static PrecisionModel pm = PrecisionModel.DEFAULT_PRECISION_MODEL;

	/**
	 * @param env
	 * @return a polygon
	 */
	public static Polygon envelopeToPolygon(Envelope env) {
		GeometryFactory fac = new GeometryFactory();
		Point a = env.getMin();
		Point b = fac.createPoint(null, a.get0() + env.getSpan0(), a.get1(), env.getCoordinateSystem());
		Point c = env.getMax();
		Point d = fac.createPoint(null, a.get0(), a.get1() + env.getSpan1(), env.getCoordinateSystem());
		LinearRing ring = fac.createLinearRing(null, env.getCoordinateSystem(), new PointsArray(a, b, c, d, a));
		return fac.createPolygon(null, env.getCoordinateSystem(), ring, null);
	}

	// public static Geometry envelopeToGeometry( Envelope env ) {
	// Geometry geom = null;
	// if ( env != null ) {
	// double minX = envelope.getMin().get0();
	// double minY = envelope.getMin().get1();
	// double maxX = envelope.getMax().get0();
	// double maxY = envelope.getMax().get1();
	// if ( envelope.getMin().equals( envelope.getMax() ) ) {
	// Point point = new Point( envelope.getMin().get0(), envelope.getMin().get1() );
	// // TODO
	// point.setSrid( srid );
	// pgGeometry = new PGgeometry( point );
	// } else if ( minX == maxX || minY == maxY ) {
	// LineString line = new LineString( new Point[] { new Point( minX, minY ), new Point(
	// maxX, maxY ) } );
	// // TODO
	// line.setSrid( srid );
	// pgGeometry = new PGgeometry( line );
	// } else {
	// Point[] points = new Point[] { new Point( minX, minY ), new Point( maxX, minY ),
	// new Point( maxX, maxY ), new Point( minX, maxY ), new Point( minX, minY ) };
	// LinearRing outer = new LinearRing( points );
	// Polygon polygon = new Polygon( new LinearRing[] { outer } );
	// // TODO
	// polygon.setSrid( srid );
	// pgGeometry = new PGgeometry( polygon );
	// }
	// }
	// return pgGeometry;
	// }

	/**
	 * Moves the coordinates of a geometry.
	 * @param geom use only surfaces, line strings or points, and only with dim == 2
	 * @param offx
	 * @param offy
	 * @return the moved geometry
	 */
	public static Geometry move(Geometry geom, double offx, double offy) {
		GeometryFactory fac = new GeometryFactory();
		if (geom instanceof Point) {
			Point p = (Point) geom;
			return fac.createPoint(geom.getId(), new double[] { p.get0() + offx, p.get1() + offy },
					p.getCoordinateSystem());
		}
		if (geom instanceof Curve) {
			Curve c = (Curve) geom;
			LinkedList<Point> ps = new LinkedList<Point>();
			for (Point p : c.getAsLineString().getControlPoints()) {
				ps.add((Point) move(p, offx, offy));
			}
			return fac.createLineString(geom.getId(), c.getCoordinateSystem(), new PointsList(ps));
		}
		if (geom instanceof Polygon) {
			Surface s = (Surface) geom;
			LinkedList<SurfacePatch> movedPatches = new LinkedList<SurfacePatch>();
			for (SurfacePatch patch : s.getPatches()) {
				if (patch instanceof PolygonPatch) {
					Ring exterior = ((PolygonPatch) patch).getExteriorRing();
					LinearRing movedExteriorRing = null;
					if (exterior != null) {
						movedExteriorRing = fac.createLinearRing(exterior.getId(), exterior.getCoordinateSystem(),
								move(exterior.getAsLineString().getControlPoints(), offx, offy));
					}
					List<Ring> interiorRings = ((PolygonPatch) patch).getInteriorRings();
					List<Ring> movedInteriorRings = new ArrayList<Ring>(interiorRings.size());
					for (Ring interior : interiorRings) {
						movedInteriorRings.add(fac.createLinearRing(interior.getId(), interior.getCoordinateSystem(),
								move(interior.getAsLineString().getControlPoints(), offx, offy)));
					}
					movedPatches.add(fac.createPolygonPatch(movedExteriorRing, movedInteriorRings));
				}
				else {
					throw new UnsupportedOperationException("Cannot move non-planar surface patches.");
				}
			}
			return fac.createSurface(geom.getId(), movedPatches, geom.getCoordinateSystem());
		}
		return geom;
	}

	private static Points move(Points points, double offx, double offy) {
		List<Point> movedPoints = new ArrayList<Point>(points.size());
		GeometryFactory fac = new GeometryFactory();
		for (Point point : points) {
			double[] movedCoordinates = new double[] { point.get0() + offx, point.get1() + offy };
			movedPoints.add(fac.createPoint(point.getId(), movedCoordinates, point.getCoordinateSystem()));
		}
		return new PointsList(movedPoints);
	}

	/**
	 * @param shape
	 * @return a string representation of the shape
	 */
	public static String prettyPrintShape(Shape shape) {
		StringBuilder sb = new StringBuilder();
		PathIterator iter = shape.getPathIterator(null);
		double[] coords = new double[6];
		boolean closed = false;

		while (!iter.isDone()) {
			switch (iter.currentSegment(coords)) {
				case SEG_CLOSE:
					sb.append(", close]");
					closed = true;
					break;
				case SEG_CUBICTO:
					sb.append(", cubic to [");
					sb.append(coords[0] + ", ");
					sb.append(coords[1] + ", ");
					sb.append(coords[2] + ", ");
					sb.append(coords[3] + ", ");
					sb.append(coords[4] + ", ");
					sb.append(coords[5] + "]");
					break;
				case SEG_LINETO:
					sb.append(", line to [");
					sb.append(coords[0] + ", ");
					sb.append(coords[1] + "]");
					break;
				case SEG_MOVETO:
					sb.append("[move to [");
					sb.append(coords[0] + ", ");
					sb.append(coords[1] + "]");
					closed = false;
					break;
				case SEG_QUADTO:
					sb.append(", quadratic to [");
					sb.append(coords[0] + ", ");
					sb.append(coords[1] + ", ");
					sb.append(coords[2] + ", ");
					sb.append(coords[3] + "]");
					break;
			}
			iter.next();
		}

		if (!closed) {
			sb.append("]");
		}
		return sb.toString();
	}

	/**
	 * This method flattens the path with a flatness parameter of 1.
	 *
	 * @author Jerry Huxtable
	 * @param shape
	 * @return the path segment lengths
	 */
	public static LinkedList<Double> measurePathLengths(Shape shape) {
		PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 1);
		double points[] = new double[6];
		double moveX = 0, moveY = 0;
		double lastX = 0, lastY = 0;
		double thisX = 0, thisY = 0;
		int type = 0;
		LinkedList<Double> res = new LinkedList<Double>();

		while (!it.isDone()) {
			type = it.currentSegment(points);
			switch (type) {
				case PathIterator.SEG_MOVETO:
					moveX = lastX = points[0];
					moveY = lastY = points[1];
					break;

				case PathIterator.SEG_CLOSE:
					points[0] = moveX;
					points[1] = moveY;
					// Fall into....

				case PathIterator.SEG_LINETO:
					thisX = points[0];
					thisY = points[1];
					double dx = thisX - lastX;
					double dy = thisY - lastY;
					res.add(sqrt(dx * dx + dy * dy));
					lastX = thisX;
					lastY = thisY;
					break;
			}
			it.next();
		}

		return res;
	}

	/**
	 * Converts the given Envelope into an envelope with the given coordinate system.
	 * Basically this is a delegate call to the {@link GeometryTransformer}.
	 * @param sourceEnvelope to convert
	 * @param targetCRS
	 * @return the target Envelope
	 * @throws TransformationException
	 * @throws TransformationException if the transformation between the source and target
	 * crs cannot be created.
	 */
	public static Envelope createConvertedEnvelope(Envelope sourceEnvelope, ICRS targetCRS)
			throws TransformationException {
		Envelope result = sourceEnvelope;
		if (sourceEnvelope != null && sourceEnvelope.getCoordinateSystem() != null
				&& !sourceEnvelope.getCoordinateSystem().equals(targetCRS)) {
			try {
				result = new GeometryTransformer(targetCRS).transform(sourceEnvelope);
			}
			catch (IllegalArgumentException e) {
				throw new TransformationException(
						"Could not transform to given envelope because: " + e.getLocalizedMessage(), e);
			}
			catch (UnknownCRSException e) {
				throw new TransformationException(
						"Could not transform to given envelope because: " + e.getLocalizedMessage(), e);
			}
			catch (Exception e) {
				throw new TransformationException(
						"Could not transform to given envelope because: " + e.getLocalizedMessage(), e);
			}
		}
		return result;
	}

	/**
	 * Creates float array out of an envelope (Geometry).
	 * @param validDomain
	 * @return a float[] representation of the given envelope
	 */
	public static final float[] createEnvelope(Envelope validDomain) {
		if (validDomain == null) {
			return null;
		}
		int dim = validDomain.getCoordinateDimension();
		double[] env = validDomain.getMin().getAsArray();

		if (!(dim == 3 || dim == 2)) {
			throw new IllegalArgumentException("The envelope must be 2 or 3 dimensional.");
		}
		float[] envelope = new float[dim * 2];
		int index = 0;
		envelope[index++] = (float) env[0];
		envelope[index++] = (float) env[1];
		if (dim == 3) {
			envelope[index++] = (float) env[2];
		}
		env = validDomain.getMax().getAsArray();
		envelope[index++] = (float) env[0];
		envelope[index++] = (float) env[1];
		if (dim == 3) {
			envelope[index] = (float) env[2];
		}
		return envelope;
	}

	/**
	 * @param env
	 * @param crs
	 * @return reverse of the other createEnvelope method
	 */
	public static final Envelope createEnvelope(float[] env, CRS crs) {
		if (env.length == 4) {
			return fac.createEnvelope(env[0], env[1], env[2], env[3], crs);
		}
		if (env.length == 6) {
			return fac.createEnvelope(new double[] { env[0], env[1], env[2] }, new double[] { env[3], env[4], env[5] },
					crs);
		}
		throw new IllegalArgumentException("The envelope must be 2 or 3 dimensional.");
	}

	/**
	 * Helper methods for creating {@link AbstractDefaultGeometry} from JTS geometries
	 * that have been derived from this geometry by JTS spatial analysis methods.
	 * @param jtsGeom
	 * @param crs
	 * @return geometry with precision model and CoordinateSystem information that are
	 * identical to the ones of this geometry, or null if the given geometry is an empty
	 * collection
	 */
	@SuppressWarnings("unchecked")
	public static AbstractDefaultGeometry createFromJTS(org.locationtech.jts.geom.Geometry jtsGeom, ICRS crs) {
		AbstractDefaultGeometry geom = null;
		if (jtsGeom.isEmpty()) {
			return null;
		}
		if (jtsGeom instanceof org.locationtech.jts.geom.Point) {
			org.locationtech.jts.geom.Point jtsPoint = (org.locationtech.jts.geom.Point) jtsGeom;
			if (Double.isNaN(jtsPoint.getCoordinate().z)) {
				geom = new DefaultPoint(null, crs, pm, new double[] { jtsPoint.getX(), jtsPoint.getY() });
			}
			else {
				geom = new DefaultPoint(null, crs, pm,
						new double[] { jtsPoint.getX(), jtsPoint.getY(), jtsPoint.getCoordinate().z });
			}
		}
		else if (jtsGeom instanceof org.locationtech.jts.geom.LinearRing) {
			org.locationtech.jts.geom.LinearRing jtsLinearRing = (org.locationtech.jts.geom.LinearRing) jtsGeom;
			geom = new DefaultLinearRing(null, crs, pm, getAsPoints(jtsLinearRing.getCoordinateSequence(), crs));
		}
		else if (jtsGeom instanceof org.locationtech.jts.geom.LineString) {
			org.locationtech.jts.geom.LineString jtsLineString = (org.locationtech.jts.geom.LineString) jtsGeom;
			geom = new DefaultLineString(null, crs, pm, getAsPoints(jtsLineString.getCoordinateSequence(), crs));
		}
		else if (jtsGeom instanceof org.locationtech.jts.geom.Polygon) {
			org.locationtech.jts.geom.Polygon jtsPolygon = (org.locationtech.jts.geom.Polygon) jtsGeom;
			Points exteriorPoints = getAsPoints(jtsPolygon.getExteriorRing().getCoordinateSequence(), crs);
			LinearRing exteriorRing = new DefaultLinearRing(null, crs, pm, exteriorPoints);
			List<Ring> interiorRings = new ArrayList<Ring>(jtsPolygon.getNumInteriorRing());
			for (int i = 0; i < jtsPolygon.getNumInteriorRing(); i++) {
				Points interiorPoints = getAsPoints(jtsPolygon.getInteriorRingN(i).getCoordinateSequence(), crs);
				interiorRings.add(new DefaultLinearRing(null, crs, pm, interiorPoints));
			}
			geom = new DefaultPolygon(null, crs, pm, exteriorRing, interiorRings);
		}
		else if (jtsGeom instanceof org.locationtech.jts.geom.MultiPoint) {
			org.locationtech.jts.geom.MultiPoint jtsMultiPoint = (org.locationtech.jts.geom.MultiPoint) jtsGeom;
			if (jtsMultiPoint.getNumGeometries() > 0) {
				List<Point> members = new ArrayList<Point>(jtsMultiPoint.getNumGeometries());
				for (int i = 0; i < jtsMultiPoint.getNumGeometries(); i++) {
					members.add((Point) createFromJTS(jtsMultiPoint.getGeometryN(i), crs));
				}
				geom = new DefaultMultiPoint(null, crs, pm, members);
			}
		}
		else if (jtsGeom instanceof org.locationtech.jts.geom.MultiLineString) {
			org.locationtech.jts.geom.MultiLineString jtsMultiLineString = (org.locationtech.jts.geom.MultiLineString) jtsGeom;
			if (jtsMultiLineString.getNumGeometries() > 0) {
				List<LineString> members = new ArrayList<LineString>(jtsMultiLineString.getNumGeometries());
				for (int i = 0; i < jtsMultiLineString.getNumGeometries(); i++) {
					Curve curve = (Curve) createFromJTS(jtsMultiLineString.getGeometryN(i), crs);
					members.add(curve.getAsLineString());
				}
				geom = new DefaultMultiLineString(null, crs, pm, members);
			}
		}
		else if (jtsGeom instanceof org.locationtech.jts.geom.MultiPolygon) {
			org.locationtech.jts.geom.MultiPolygon jtsMultiPolygon = (org.locationtech.jts.geom.MultiPolygon) jtsGeom;
			if (jtsMultiPolygon.getNumGeometries() > 0) {
				List<Polygon> members = new ArrayList<Polygon>(jtsMultiPolygon.getNumGeometries());
				for (int i = 0; i < jtsMultiPolygon.getNumGeometries(); i++) {
					members.add((Polygon) createFromJTS(jtsMultiPolygon.getGeometryN(i), crs));
				}
				geom = new DefaultMultiPolygon(null, crs, pm, members);
			}
		}
		else if (jtsGeom instanceof org.locationtech.jts.geom.GeometryCollection) {
			org.locationtech.jts.geom.GeometryCollection jtsGeometryCollection = (org.locationtech.jts.geom.GeometryCollection) jtsGeom;
			if (jtsGeometryCollection.getNumGeometries() > 0) {
				List<Geometry> members = new ArrayList<Geometry>(jtsGeometryCollection.getNumGeometries());
				for (int i = 0; i < jtsGeometryCollection.getNumGeometries(); i++) {
					members.add(createFromJTS(jtsGeometryCollection.getGeometryN(i), crs));
				}
				geom = new DefaultMultiGeometry(null, crs, pm, members);
			}
		}
		else {
			throw new RuntimeException(
					"Internal error. Encountered unhandled JTS geometry type '" + jtsGeom.getClass().getName() + "'.");
		}
		return geom;
	}

	private static Points getAsPoints(CoordinateSequence seq, ICRS crs) {
		return new JTSPoints(crs, seq);
	}

}
