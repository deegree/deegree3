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

package io.github.sebasbaumh.postgis.binary;

import java.util.ArrayList;
import java.util.Collection;

import io.github.sebasbaumh.postgis.CircularString;
import io.github.sebasbaumh.postgis.CompoundCurve;
import io.github.sebasbaumh.postgis.Curve;
import io.github.sebasbaumh.postgis.CurvePolygon;
import io.github.sebasbaumh.postgis.Geometry;
import io.github.sebasbaumh.postgis.GeometryCollection;
import io.github.sebasbaumh.postgis.LineString;
import io.github.sebasbaumh.postgis.LinearRing;
import io.github.sebasbaumh.postgis.MultiCurve;
import io.github.sebasbaumh.postgis.MultiLineString;
import io.github.sebasbaumh.postgis.MultiPoint;
import io.github.sebasbaumh.postgis.MultiPolygon;
import io.github.sebasbaumh.postgis.MultiSurface;
import io.github.sebasbaumh.postgis.Point;
import io.github.sebasbaumh.postgis.Polygon;
import io.github.sebasbaumh.postgis.PolygonBase;

/**
 * A parser for reading geometries from a binary or hex string representation.
 * @author Sebastian Baumhekel
 */
public final class BinaryParser
{

	// prevent instantiating this class
	@Deprecated
	private BinaryParser()
	{
	}

	/**
	 * Parse a hex encoded geometry
	 * @param value byte array containing the data to be parsed
	 * @param offset offset
	 * @return resulting geometry for the parsed data
	 * @throws IllegalArgumentException if a contained geometry is of the wrong type or the encoding type is unknown
	 */
	public static Geometry parse(byte[] value, int offset)
	{
		return parseGeometry(new BinaryValueGetter(value, offset));
	}

	/**
	 * Parse a hex encoded geometry
	 * @param value String containing the data to be parsed
	 * @return resulting geometry for the parsed data
	 * @throws IllegalArgumentException if a contained geometry is of the wrong type or the encoding type is unknown
	 */
	public static Geometry parse(String value)
	{
		return parseGeometry(new StringValueGetter(value));
	}

	/**
	 * Parse multiple geometries into a {@link Collection}. The number of geometries is read upfront from the
	 * {@link ValueGetter}.
	 * @param clazz {@link Class} of the geometries
	 * @param data {@link ValueGetter}
	 * @return {@link Collection} of geometries
	 * @throws IllegalArgumentException if a contained geometry is of the wrong type
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Geometry> Collection<T> parseGeometries(Class<T> clazz, ValueGetter data)
	{
		// get number of geometries to parse
		int count = data.getInt();
		ArrayList<T> l = new ArrayList<T>(count);
		// parse geometries
		for (int i = 0; i < count; i++)
		{
			Geometry geom = parseGeometry(data);
			// check if the geometry is of the correct type
			if (clazz.isInstance(geom))
			{
				l.add((T) geom);
			}
			else
			{
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
	private static Geometry parseGeometry(ValueGetter data)
	{
		// read endian flag
		data.readEncoding();
		// and get the type
		int typeword = data.getInt();
		int geometryType = typeword & 0x1FFFFFFF; // cut off high flag bits

		boolean haveZ = (typeword & 0x80000000) != 0;
		boolean haveM = (typeword & 0x40000000) != 0;
		boolean haveS = (typeword & 0x20000000) != 0;

		int srid = Geometry.UNKNOWN_SRID;
		if (haveS)
		{
			// ensure valid SRID
			srid = data.getInt();
			if (srid < 0)
			{
				srid = Geometry.UNKNOWN_SRID;
			}
		}
		// parse geometry according to type
		Geometry result;
		switch (geometryType)
		{
			case Point.TYPE:
				result = parsePoint(data, haveZ, haveM);
				break;
			case LineString.TYPE:
				result = new LineString(parsePoints(data, haveZ, haveM));
				break;
			case CircularString.TYPE:
				result = new CircularString(parsePoints(data, haveZ, haveM));
				break;
			case CompoundCurve.TYPE:
				result = new CompoundCurve(parseGeometries(LineString.class, data));
				break;
			case Polygon.TYPE:
				result = parsePolygon(data, haveZ, haveM);
				break;
			case CurvePolygon.TYPE:
				result = new CurvePolygon(parseGeometries(Curve.class, data));
				break;
			case MultiPoint.TYPE:
				result = new MultiPoint(parseGeometries(Point.class, data));
				break;
			case MultiLineString.TYPE:
				result = new MultiLineString(parseGeometries(LineString.class, data));
				break;
			case MultiCurve.TYPE:
				result = new MultiCurve(parseGeometries(Curve.class, data));
				break;
			case MultiPolygon.TYPE:
				result = new MultiPolygon(parseGeometries(Polygon.class, data));
				break;
			case MultiSurface.TYPE:
				result = new MultiSurface(parseGeometries(PolygonBase.class, data));
				break;
			case GeometryCollection.TYPE:
				result = new GeometryCollection(parseGeometries(Geometry.class, data));
				break;
			default:
				throw new IllegalArgumentException("Unknown Geometry Type: " + geometryType);
		}
		// set SRID and return the geometry
		result.setSrid(srid);
		return result;
	}

	/**
	 * Parse a single point.
	 * @param data {@link ValueGetter}
	 * @param haveZ parse z value?
	 * @param haveM parse measure value?
	 * @return {@link Point}
	 */
	private static Point parsePoint(ValueGetter data, boolean haveZ, boolean haveM)
	{
		double x = data.getDouble();
		double y = data.getDouble();
		Point result;
		// parse z?
		if (haveZ)
		{
			result = new Point(x, y, data.getDouble());
		}
		else
		{
			result = new Point(x, y);
		}
		// parse measure?
		if (haveM)
		{
			result.setM(data.getDouble());
		}
		return result;
	}

	/**
	 * Parse an Array of "slim" {@link Point}s (without endianness and type, part of {@link LinearRing} and
	 * {@link LineString}, but not {@link MultiPoint}!
	 * @param data {@link ValueGetter}
	 * @param haveZ parse z value?
	 * @param haveM parse measure value?
	 * @return {@link Collection} of {@link Point}s
	 */
	private static Collection<Point> parsePoints(ValueGetter data, boolean haveZ, boolean haveM)
	{
		int count = data.getInt();
		ArrayList<Point> l = new ArrayList<Point>(count);
		for (int i = 0; i < count; i++)
		{
			l.add(parsePoint(data, haveZ, haveM));
		}
		return l;
	}

	/**
	 * Parse a {@link Polygon}.
	 * @param data {@link ValueGetter}
	 * @param haveZ parse z value?
	 * @param haveM parse measure value?
	 * @return {@link Polygon}
	 */
	private static Polygon parsePolygon(ValueGetter data, boolean haveZ, boolean haveM)
	{
		int count = data.getInt();
		ArrayList<LinearRing> rings = new ArrayList<LinearRing>(count);
		for (int i = 0; i < count; i++)
		{
			rings.add(new LinearRing(parsePoints(data, haveZ, haveM)));
		}
		return new Polygon(rings);
	}
}
