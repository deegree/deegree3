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

import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.segments.ArcString;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.primitive.segments.LineStringSegment;

import java.util.EnumSet;
import java.util.List;

import static org.deegree.geometry.primitive.segments.CurveSegment.CurveSegmentType.ARC_STRING;
import static org.deegree.geometry.primitive.segments.CurveSegment.CurveSegmentType.LINE_STRING_SEGMENT;

/**
 * A writer for building a binary or hex string representation of geometries.
 *
 * @author Sebastian Baumhekel
 */
public final class BinaryWriter {

	// prevent instantiating this class
	@Deprecated
	private BinaryWriter() {
	}

	/**
	 * Write a binary encoded geometry. The geometry you put in must be consistent,
	 * geom.checkConsistency() must return true. If not, the result may be invalid WKB.
	 * @param axis Requested number and arrangement of Axis
	 * @param geom the geometry to be written
	 * @param srid the srid to be set on geometry, values lower than zero are not set
	 * @return byte array containing the encoded geometry
	 */
	public static byte[] writeBinary(Axis axis, Geometry geom, int srid) {
		BinaryValueSetter bytes = new BinaryValueSetter(axis, srid);
		writeGeometry(geom, bytes, true);
		return bytes.getValue();
	}

	private static void writeEndianAndTypeWord(final int type, ValueSetter dest, boolean top) {
		int typeword = type;
		if (dest.hasZ()) {
			typeword |= 0x80000000;
		}
		if (dest.hasM()) {
			typeword |= 0x40000000;
		}
		if (dest.getSrid() >= 0 && top) {
			typeword |= 0x20000000;
		}

		// write endian flag, NDR (little endian)
		dest.setByte(Utils.LITTLE_ENDIAN);

		dest.setInt(typeword);

		if (dest.getSrid() >= 0 && top) {
			dest.setInt(dest.getSrid());
		}
	}

	/**
	 * Parse a geometry starting at offset.
	 * @param geom the geometry to write
	 * @param dest the value setting to be used for writing
	 */
	private static void writeGeometry(Geometry geom, ValueSetter dest, boolean top) {
		Geometry.GeometryType typ = geom.getGeometryType();
		if (typ == Geometry.GeometryType.PRIMITIVE_GEOMETRY) {
			writePrimitive((GeometricPrimitive) geom, dest, top);
		}
		else if (typ == Geometry.GeometryType.MULTI_GEOMETRY) {
			if (!top) {
				// encapsulated multi not supported
				throw new InvalidParameterValueException();
			}
			MultiGeometry<? extends Geometry> geometries = (MultiGeometry<? extends Geometry>) geom;
			if (geometries.getMultiGeometryType() == MultiGeometry.MultiGeometryType.MULTI_POINT) {
				writeEndianAndTypeWord(Types.MULTIPOINT_TYPE, dest, top);
			}
			else if (geometries.getMultiGeometryType() == MultiGeometry.MultiGeometryType.MULTI_LINE_STRING) {
				writeEndianAndTypeWord(Types.MULTILINESTRING_TYPE, dest, top);
			}
			else if (geometries.getMultiGeometryType() == MultiGeometry.MultiGeometryType.MULTI_POLYGON) {
				writeEndianAndTypeWord(Types.MULTIPOLYGON_TYPE, dest, top);
			}
			else if (geometries.getMultiGeometryType() == MultiGeometry.MultiGeometryType.MULTI_CURVE) {
				writeEndianAndTypeWord(Types.MULTICURVE_TYPE, dest, top);
			}
			else {
				writeEndianAndTypeWord(Types.GEOMETRY_COLLECTION_TYPE, dest, top);
			}
			dest.setInt(geometries.size());
			for (Geometry sub : geometries) {
				writeGeometry(sub, dest, false);
			}
		}
		else if (typ == Geometry.GeometryType.ENVELOPE) {
			// TODO should this be written as polygon?
			throw new InvalidParameterValueException();
		}
		else {
			// COMPOSITE_GEOMETRY: and others
			throw new InvalidParameterValueException();
		}
	}

	private static void writePrimitive(GeometricPrimitive geom, ValueSetter dest, boolean top) {
		GeometricPrimitive.PrimitiveType typ = geom.getPrimitiveType();
		if (typ == GeometricPrimitive.PrimitiveType.Curve) {
			writeCurve((Curve) geom, dest, top);
		}
		else if (typ == GeometricPrimitive.PrimitiveType.Point) {
			Point pnt = (Point) geom;
			writePoint(pnt, dest, top);
		}
		else if (typ == GeometricPrimitive.PrimitiveType.Surface) {
			writeSurface((Surface) geom, dest, top);
		}
		else {
			// unsupported
			throw new InvalidParameterValueException();
		}
	}

	private static void writeSurface(Surface geom, ValueSetter dest, boolean top) {
		boolean isSimple = true;
		EnumSet<CurveSegment.CurveSegmentType> types = EnumSet.noneOf(CurveSegment.CurveSegmentType.class);
		if (geom.getPatches().size() > 1) {
			// no multi polygon/surface at this point
			throw new InvalidParameterValueException();
		}
		for (SurfacePatch sp : geom.getPatches()) {
			if (sp.getSurfacePatchType() != SurfacePatch.SurfacePatchType.POLYGON_PATCH)
				throw new InvalidParameterValueException();

			PolygonPatch pp = (PolygonPatch) sp;
			for (Ring r : pp.getBoundaryRings()) {
				if (r.getCurveSegments().size() > 1) {
					// compund
					isSimple = false;
					break;
				}
				checksCompoundLineSegment(types, r.getCurveSegments());
			}
		}
		PolygonPatch pp = (PolygonPatch) geom.getPatches().get(0);
		List<Ring> interiorRings = pp.getInteriorRings();
		if (isSimple && types.size() == 1 && types.contains(LINE_STRING_SEGMENT)) {
			writeEndianAndTypeWord(Types.POLYGON_TYPE, dest, top);
			dest.setInt(1 + interiorRings.size());
			writePoints(pp.getExteriorRing().getControlPoints(), dest);
			for (Ring ring : interiorRings) {
				writePoints(ring.getControlPoints(), dest);
			}
			return;
		}
		writeEndianAndTypeWord(Types.CURVE_POLYGON_TYPE, dest, top);
		dest.setInt(1 + interiorRings.size());
		writeCurve(pp.getExteriorRing(), dest, false);
		for (Ring ring : interiorRings) {
			writeCurve(ring, dest, false);
		}
	}

	private static EnumSet<CurveSegment.CurveSegmentType> checksCompoundLineSegment(List<CurveSegment> segments) {
		EnumSet<CurveSegment.CurveSegmentType> types = EnumSet.noneOf(CurveSegment.CurveSegmentType.class);
		checksCompoundLineSegment(types, segments);
		return types;
	}

	private static EnumSet<CurveSegment.CurveSegmentType> checksCompoundLineSegment(
			EnumSet<CurveSegment.CurveSegmentType> types, List<CurveSegment> segments) {
		for (CurveSegment segment : segments) {
			switch (segment.getSegmentType()) {
				case CIRCLE: // Circle extends ArcString
				case ARC: // Arc extends ArcString
				case ARC_STRING:
					types.add(ARC_STRING);
					break;
				case LINE_STRING_SEGMENT:
					types.add(LINE_STRING_SEGMENT);
					break;
				default:
					throw new InvalidParameterValueException();
			}
		}
		return types;
	}

	private static void writePoint(Point pnt, ValueSetter dest, boolean top) {
		writeEndianAndTypeWord(Types.POINT_TYPE, dest, top);
		dest.setDouble(pnt.get0());
		dest.setDouble(pnt.get1());
		switch (dest.getAxis()) {
			case XYZ:
			case XYM:
				dest.setDouble(pnt.get(2));
				break;
			case XYZM:
				dest.setDouble(pnt.get(2));
				dest.setDouble(pnt.get(3));
		}
	}

	private static void writePoints(Points points, ValueSetter dest) {
		dest.setInt(points.size());
		Axis dim = dest.getAxis();
		for (Point pnt : points) {
			dest.setDouble(pnt.get0());
			dest.setDouble(pnt.get1());
			switch (dim) {
				case XYZ:
				case XYM:
					dest.setDouble(pnt.get(2));
					break;
				case XYZM:
					dest.setDouble(pnt.get(2));
					dest.setDouble(pnt.get(3));
			}
		}
	}

	private static void writeCurve(Curve geom, ValueSetter dest, boolean top) {
		List<CurveSegment> segments = geom.getCurveSegments();
		EnumSet<CurveSegment.CurveSegmentType> types = checksCompoundLineSegment(segments);

		Points controlPoints = null;
		if (types.size() == 1 && segments.size() == 1 && types.contains(LINE_STRING_SEGMENT)) {
			writeEndianAndTypeWord(Types.LINESTRING_TYPE, dest, top);
			controlPoints = ((LineStringSegment) segments.get(0)).getControlPoints();
		}
		else if (types.size() == 1 && segments.size() == 1 && types.contains(ARC_STRING)) {
			writeEndianAndTypeWord(Types.CIRCULAR_STRING_TYPE, dest, top);
			controlPoints = ((ArcString) segments.get(0)).getControlPoints();
		}
		else {
			// COMPOUND
			writeEndianAndTypeWord(Types.COMPOUND_CURVE_TYPE, dest, top);
		}
		if (controlPoints != null) {
			writePoints(controlPoints, dest);
		}
		else {
			dest.setInt(segments.size());
			for (CurveSegment segment : segments) {
				if (segment.getSegmentType() == LINE_STRING_SEGMENT) {
					writeEndianAndTypeWord(Types.LINESTRING_TYPE, dest, false);
					controlPoints = ((LineStringSegment) segment).getControlPoints();
					writePoints(controlPoints, dest);
				}
				else {
					// ARC
					writeEndianAndTypeWord(Types.CIRCULAR_STRING_TYPE, dest, false);
					controlPoints = ((ArcString) segment).getControlPoints();
					writePoints(controlPoints, dest);
				}
			}
		}
	}

}