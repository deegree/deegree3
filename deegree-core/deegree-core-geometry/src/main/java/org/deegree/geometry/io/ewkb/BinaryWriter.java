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
import io.github.sebasbaumh.postgis.PostGisUtil;

/**
 * A writer for building a binary or hex string representation of geometries.
 * @author Sebastian Baumhekel
 */
public final class BinaryWriter
{
	// prevent instantiating this class
	@Deprecated
	private BinaryWriter()
	{
	}

	/**
	 * Write a binary encoded geometry. The geometry you put in must be consistent, geom.checkConsistency() must return
	 * true. If not, the result may be invalid WKB.
	 * @see Geometry#checkConsistency() the consistency checker
	 * @param geom the geometry to be written
	 * @return byte arrray containing the encoded geometry
	 */
	public static byte[] writeBinary(Geometry geom)
	{
		BinaryValueSetter bytes = new BinaryValueSetter();
		writeGeometry(geom, bytes);
		return bytes.getValue();
	}

	/**
	 * Parse a geometry starting at offset.
	 * @param geom the geometry to write
	 * @param dest the value setting to be used for writing
	 */
	private static void writeGeometry(Geometry geom, ValueSetter dest)
	{
		// write endian flag, NDR (little endian)
		dest.setByte(PostGisUtil.LITTLE_ENDIAN);

		// write typeword
		int typeword = geom.getType();
		if (geom.is3d())
		{
			typeword |= 0x80000000;
		}
		if (geom.hasMeasure())
		{
			typeword |= 0x40000000;
		}
		if (geom.getSrid() != Geometry.UNKNOWN_SRID)
		{
			typeword |= 0x20000000;
		}
		dest.setInt(typeword);

		if (geom.getSrid() != Geometry.UNKNOWN_SRID)
		{
			dest.setInt(geom.getSrid());
		}

		switch (geom.getType())
		{
			case Point.TYPE:
				writePoint((Point) geom, dest);
				break;
			case LineString.TYPE:
				writePoints((LineString) geom, dest);
				break;
			case CircularString.TYPE:
				writePoints((CircularString) geom, dest);
				break;
			case CompoundCurve.TYPE:
				writeMultiGeometry(((CompoundCurve) geom).getGeometries(), dest);
				break;
			case Polygon.TYPE:
				writePolygon((Polygon) geom, dest);
				break;
			case CurvePolygon.TYPE:
				writePolygon((CurvePolygon) geom, dest);
				break;
			case MultiPoint.TYPE:
				writeMultiGeometry(((MultiPoint) geom).getGeometries(), dest);
				break;
			case MultiLineString.TYPE:
				writeMultiGeometry(((MultiLineString) geom).getGeometries(), dest);
				break;
			case MultiCurve.TYPE:
				writeMultiGeometry(((MultiCurve) geom).getGeometries(), dest);
				break;
			case MultiPolygon.TYPE:
				writeMultiGeometry(((MultiPolygon) geom).getGeometries(), dest);
				break;
			case MultiSurface.TYPE:
				writeMultiGeometry(((MultiSurface) geom).getGeometries(), dest);
				break;
			case GeometryCollection.TYPE:
				writeMultiGeometry(((GeometryCollection) geom).getGeometries(), dest);
				break;
			default:
				throw new IllegalArgumentException("Unknown Geometry Type: " + geom.getType());
		}
	}

	/**
	 * Write a hex encoded geometry. The geometry you put in must be consistent, geom.checkConsistency() must return
	 * true. If not, the result may be invalid WKB.
	 * @see Geometry#checkConsistency() the consistency checker
	 * @param geom the geometry to be written
	 * @return String containing the hex encoded geometry
	 */
	public static String writeHexed(Geometry geom)
	{
		StringValueSetter bytes = new StringValueSetter();
		writeGeometry(geom, bytes);
		return bytes.getValue();
	}

	/**
	 * Writes multiple geometries preceded by their count.
	 * @param geoms geometries
	 * @param dest writer
	 */
	private static <T extends Geometry> void writeMultiGeometry(Collection<T> geoms, ValueSetter dest)
	{
		dest.setInt(geoms.size());
		for (Geometry geom : geoms)
		{
			writeGeometry(geom, dest);
		}
	}

	/**
	 * Writes a "slim" Point (without endiannes, srid ant type, only the ordinates and measure. Used by writeGeometry as
	 * ell as writePointArray.
	 * @param geom geometry
	 * @param dest writer
	 */
	private static void writePoint(Point geom, ValueSetter dest)
	{
		dest.setDouble(geom.getX());
		dest.setDouble(geom.getY());
		// write z coordinate?
		if (geom.is3d())
		{
			dest.setDouble(geom.getZ());
		}
		// write measure?
		if (geom.hasMeasure())
		{
			dest.setDouble(geom.getM());
		}
	}

	/**
	 * Write an Array of "slim" Points (without endianness, srid and type, part of LinearRing and Linestring, but not
	 * MultiPoint!
	 * @param geom geometry
	 * @param dest writer
	 */
	private static void writePoints(LineString geom, ValueSetter dest)
	{
		// number of points
		dest.setInt(geom.getNumberOfCoordinates());
		for (Point p : geom)
		{
			writePoint(p, dest);
		}
	}

	/**
	 * Writes a {@link Polygon}.
	 * @param geom {@link Polygon}
	 * @param dest writer
	 */
	private static <T extends Curve> void writePolygon(PolygonBase<T> geom, ValueSetter dest)
	{
		// collect all rings (outer ring+inner rings)
		ArrayList<T> rings = new ArrayList<T>(geom.getNumberOfRings() + 1);
		rings.add(geom.getOuterRing());
		for (T ring : geom.getRings())
		{
			rings.add(ring);
		}
		// write number of rings
		dest.setInt(rings.size());
		// then all rings
		for (T ring : rings)
		{
			// polygon linear rings are just written as a plain set of points
			if (ring instanceof LinearRing lr)
			{
				writePoints(lr, dest);
			}
			else
			{
				// curve polygons can have different geometries
				writeGeometry(ring, dest);
			}
		}
	}

}
