/*
 * PostGIS extension for PostgreSQL JDBC driver
 *
 * (C) 2004 Paul Ramsey, pramsey@refractions.net
 * (C) 2005 Markus Schaber, markus.schaber@logix-tt.com
 * (C) 2015 Phillip Ross, phillip.w.g.ross@gmail.com
 * (C) 2018-2023 Sebastian Baumhekel, sebastian.baumhekel@gmail.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <https://www.gnu.org/licenses/>.
 */

package org.deegree.geometry.io.ewkb;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.standard.points.PackedPoints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.IntFunction;

import static org.deegree.geometry.io.ewkb.Axis.XYM;
import static org.deegree.geometry.io.ewkb.Axis.XYZ;
import static org.deegree.geometry.io.ewkb.Axis.XYZM;

/**
 * A parser for reading geometries from a binary or hex string representation.
 *
 * @author Sebastian Baumhekel
 */
public final class BinaryParser {

	// prevent instantiating this class
	@Deprecated
	private BinaryParser() {
	}

	@SuppressWarnings("SpellCheckingInspection")
	public static final IntFunction<ICRS> SRID_TO_NONE = (srid) -> null;

	@SuppressWarnings("SpellCheckingInspection")
	public static final IntFunction<ICRS> SRID_TO_EPSG = (srid) -> srid >= 0 ? CRSManager.getCRSRef("EPSG:" + srid)
			: null;

	@SuppressWarnings("SpellCheckingInspection")
	public static final IntFunction<ICRS> SRID_TO_URN_OGC_DEF_CRS_EPSG = (srid) -> srid >= 0
			? CRSManager.getCRSRef("urn:ogc:def:crs:EPSG::" + srid) : null;

	private final static GeometryFactory _gf = new GeometryFactory();

	/**
	 * Parse a hex encoded geometry
	 * @param axis Requested number and arrangement of Axis
	 * @param sridToCrs Converter function to convert srid to ICRS
	 * @param value byte array containing the data to be parsed
	 * @param offset offset
	 * @return resulting geometry for the parsed data
	 * @throws IllegalArgumentException if a contained geometry is of the wrong type or
	 * the encoding type is unknown
	 */
	public static Geometry parse(Axis axis, IntFunction<ICRS> sridToCrs, byte[] value, int offset) {
		return parseGeometry(new BinaryValueGetter(axis != null ? axis : Axis.XY,
				sridToCrs != null ? sridToCrs : SRID_TO_NONE, value, offset));
	}

	/**
	 * Parse a hex encoded geometry
	 * @param axis Requested number and arrangement of Axis
	 * @param sridToCrs Converter function to convert srid to ICRS
	 * @param value String containing the data to be parsed
	 * @return resulting geometry for the parsed data
	 * @throws IllegalArgumentException if a contained geometry is of the wrong type or
	 * the encoding type is unknown
	 */
	public static Geometry parse(Axis axis, IntFunction<ICRS> sridToCrs, String value) {
		return parseGeometry(new StringValueGetter(axis != null ? axis : Axis.XY,
				sridToCrs != null ? sridToCrs : SRID_TO_NONE, value));
	}

	/**
	 * Parse multiple geometries into a {@link Collection}. The number of geometries is
	 * read upfront from the {@link ValueGetter}.
	 * @param clazz {@link Class} of the geometries
	 * @param data {@link ValueGetter}
	 * @return {@link Collection} of geometries
	 * @throws IllegalArgumentException if a contained geometry is of the wrong type
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Geometry> List<T> parseGeometries(Class<T> clazz, ValueGetter data) {
		// get number of geometries to parse
		int count = data.getInt();
		ArrayList<T> l = new ArrayList<>(count);
		// parse geometries
		for (int i = 0; i < count; i++) {
			Geometry geom = parseGeometry(data);
			// check if the geometry is of the correct type
			if (clazz.isInstance(geom)) {
				l.add((T) geom);
			}
			else {
				throw new IllegalArgumentException(
						"expected: " + clazz.getCanonicalName() + " got: " + geom.getClass().getCanonicalName());
			}
		}
		return l;
	}

	/**
	 * Parse a geometry starting at offset.
	 * @param data ValueGetter with the data to be parsed
	 * @return the parsed geometry
	 * @throws IllegalArgumentException for unknown geometry types
	 */
	private static Geometry parseGeometry(ValueGetter data) {
		// read endian flag
		data.readEncoding();
		// and get the type
		int typeword = data.getInt();
		int geometryType = typeword & 0x1FFFFFFF; // cut off high flag bits

		boolean haveZ = (typeword & 0x80000000) != 0;
		boolean haveM = (typeword & 0x40000000) != 0;
		boolean haveS = (typeword & 0x20000000) != 0;

		int srid = -1;
		if (haveS) {
			srid = data.getInt();
			if (srid < 0) {
				srid = -1;
			}
		}
		// parse geometry according to type
		Geometry result;
		switch (geometryType) {
			case Types.POINT_TYPE:
				result = parsePoint(data, haveZ, haveM);
				break;
			case Types.LINESTRING_TYPE:
				result = _gf.createLineString(null, null, parsePoints(data, haveZ, haveM));
				break;
			case Types.CIRCULAR_STRING_TYPE:
				result = _gf.createCurve(null, null, _gf.createArcString(parsePoints(data, haveZ, haveM)));
				break;
			case Types.COMPOUND_CURVE_TYPE:
				result = parseCompoundCurve(data);
				break;
			case Types.POLYGON_TYPE:
				result = parsePolygon(data, haveZ, haveM);
				break;
			case Types.TIN_TYPE: // Triangles as Polygon
			case Types.CURVE_POLYGON_TYPE:
				List<Ring> rings = parseRings(data);
				result = _gf.createPolygon(null, null, rings.remove(0), rings);
				break;
			case Types.MULTIPOINT_TYPE:
				result = _gf.createMultiPoint(null, null, parseGeometries(Point.class, data));
				break;
			case Types.MULTILINESTRING_TYPE:
				result = _gf.createMultiLineString(null, null, parseGeometries(LineString.class, data));
				break;
			case Types.MULTICURVE_TYPE:
				result = _gf.createMultiCurve(null, null, parseGeometries(Curve.class, data));
				break;
			case Types.MULTIPOLYGON_TYPE:
				result = _gf.createMultiPolygon(null, null, parseGeometries(Polygon.class, data));
				break;
			case Types.MULTISURFACE_TYPE:
			case Types.GEOMETRY_COLLECTION_TYPE:
				result = _gf.createMultiGeometry(null, null, parseGeometries(Geometry.class, data));
				break;
			default:
				throw new IllegalArgumentException("Unknown Geometry Type: " + geometryType);
		}
		if (srid >= 0) {
			ICRS crs = data.getSridToCrs().apply(srid);
			if (crs != null) {
				result.setCoordinateSystem(crs);
			}
		}
		return result;
	}

	/**
	 * Parse a single point.
	 * @param data {@link ValueGetter}
	 * @param haveZ parse z value?
	 * @param haveM parse measure value?
	 * @return {@link Point}
	 */
	private static Point parsePoint(ValueGetter data, boolean haveZ, boolean haveM) {
		double x = data.getDouble();
		double y = data.getDouble();
		double z = haveZ ? data.getDouble() : data.getNoValue();
		double m = haveM ? data.getDouble() : data.getNoValue();
		// parse z?
		if (haveZ) {
			z = data.getDouble();
		}
		// parse measure?
		if (haveM) {
			m = data.getDouble();
		}

		return switch (data.getAxis()) {
			case XY -> _gf.createPoint(null, x, y, null);
			case XYZ -> _gf.createPoint(null, x, y, z, null);
			case XYM -> _gf.createPoint(null, x, y, m, null);
			case XYZM -> _gf.createPoint(null, new double[] { x, y, z, m }, null);
		};
	}

	/**
	 * Parse an Array of "slim" {@link Point}s
	 * @param data {@link ValueGetter}
	 * @param haveZ parse z value?
	 * @param haveM parse measure value?
	 * @return {@link Collection} of {@link Point}s
	 */
	private static PackedPoints parsePoints(ValueGetter data, boolean haveZ, boolean haveM) {
		int count = data.getInt();
		double[] points = new double[data.getAxis().size(count)];
		int pos = 0;
		for (int i = 0; i < count; i++) {
			points[pos++] = data.getDouble();
			points[pos++] = data.getDouble();
			if (data.getAxis() == XYZ || data.getAxis() == XYZM) {
				points[pos++] = haveZ ? data.getDouble() : data.getNoValue();
			}
			if (data.getAxis() == XYM || data.getAxis() == XYZM) {
				points[pos++] = haveM ? data.getDouble() : data.getNoValue();
			}
		}
		return new PackedPoints(null, points, data.getAxis().size(1));
	}

	private static Curve parseCompoundCurve(ValueGetter data) {
		int count = data.getInt();
		CurveSegment[] segments = new CurveSegment[count];
		for (int i = 0; i < count; i++) {
			// read endian flag
			data.readEncoding();
			// and get the type
			int typeWord = data.getInt();
			int geometryType = typeWord & 0x1FFFFFFF; // cut off high flag bits

			boolean haveZ = (typeWord & 0x80000000) != 0;
			boolean haveM = (typeWord & 0x40000000) != 0;
			switch (geometryType) {
				case Types.LINESTRING_TYPE:
					segments[i] = _gf.createLineStringSegment(parsePoints(data, haveZ, haveM));
					break;
				case Types.CIRCULAR_STRING_TYPE:
					segments[i] = _gf.createArcString(parsePoints(data, haveZ, haveM));
					break;
				default:
					throw new IllegalArgumentException("Unknown Geometry Type: " + geometryType);
			}
		}
		return _gf.createCurve(null, null, segments);
	}

	private static List<Ring> parseRings(ValueGetter data) {
		int count = data.getInt();
		List<Ring> segments = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			// read endian flag
			data.readEncoding();
			// and get the type
			int typeWord = data.getInt();
			int geometryType = typeWord & 0x1FFFFFFF; // cut off high flag bits

			boolean haveZ = (typeWord & 0x80000000) != 0;
			boolean haveM = (typeWord & 0x40000000) != 0;
			switch (geometryType) {
				case Types.LINESTRING_TYPE:
					segments.add(_gf.createLinearRing(null, null, parsePoints(data, haveZ, haveM)));
					break;
				case Types.CIRCULAR_STRING_TYPE:
					segments.add(_gf.createRing(null, null, List
						.of(_gf.createCurve(null, null, _gf.createArcString(parsePoints(data, haveZ, haveM))))));
					break;
				case Types.COMPOUND_CURVE_TYPE:
					segments.add(_gf.createRing(null, null, List.of(parseCompoundCurve(data))));
					break;
				case Types.POLYGON_TYPE:
				case Types.TRIANGLE_TYPE: // like a polygon
					Polygon poly = parsePolygon(data, haveZ, haveM);
					segments.add(poly.getExteriorRing());
					segments.addAll(poly.getInteriorRings());
					break;
				default:
					throw new IllegalArgumentException("Unknown Geometry Type: " + geometryType);
			}
		}
		return segments;
	}

	/**
	 * Parse a {@link Polygon}.
	 * @param data {@link ValueGetter}
	 * @param haveZ parse z value?
	 * @param haveM parse measure value?
	 * @return {@link Polygon}
	 */
	private static Polygon parsePolygon(ValueGetter data, boolean haveZ, boolean haveM) {
		int count = data.getInt();
		List<Ring> rings = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			rings.add(_gf.createLinearRing(null, null, parsePoints(data, haveZ, haveM)));
		}
		return _gf.createPolygon(null, null, rings.remove(0), rings);
	}

}