/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright by:
 (C) 2018 - 2022, grit GmbH
 (C) 2014 - 2016, Open Source Geospatial Foundation (OSGeo)

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
  e-mail: info@deegree.org
 website: http://www.deegree.org/

 ----------------------------------------------------------------------------*/
package org.deegree.geometry.io;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Curve.CurveType;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring.RingType;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.Surface.SurfaceType;
import org.deegree.geometry.primitive.segments.ArcString;
import org.deegree.geometry.primitive.segments.CurveSegment.CurveSegmentType;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.locationtech.jts.io.ParseException;

public class EWKTReaderWKTtoGeometryTests {

	private Geometry read(String wkt) throws ParseException {
		EWKTReader reader = new EWKTReader();
		return reader.read(wkt);
	}

	/**
	 * Draw a circle between the start and end point; or each group of three their after.
	 * @throws Exception
	 */
	@Test
	public void circularString() throws Exception {
		String WKT = "CIRCULARSTRING(220268.439465645 150415.359530563, 220227.333322076 150505.561285879, 220227.353105332 150406.434743975)";

		Geometry geometry = read(WKT);
		assertNotNull("parsed circularstring", geometry);

		assertTrue(geometry instanceof Curve);
		Curve curve = (Curve) geometry;
		assertEquals("one segment curve", 1, curve.getCurveSegments().size());
		assertEquals("arc string segment", CurveSegmentType.ARC_STRING,
				curve.getCurveSegments().get(0).getSegmentType());
		ArcString segment = (ArcString) curve.getCurveSegments().get(0);
		assertEquals("not segmentized ", 3, segment.getControlPoints().size());
	}

	@Test
	public void curvePolygon() throws Exception {
		String WKT = "CURVEPOLYGON("
				+ "CIRCULARSTRING(143.62025166838282 -30.037497356076827, 142.92857147299705 -32.75101196874403, 145.96132309891922 -34.985671061528784, 149.57565307617188 -33.41153335571289, 149.41972407584802 -29.824672680573517, 146.1209416055467 -30.19711586270431, 143.62025166838282 -30.037497356076827),"
				+ "(144.84399355252685 -31.26123924022086, 144.20551952601693 -32.27215644886158, 145.55230712890625 -33.49203872680664, 147.97080993652344 -32.03618621826172, 146.38697244992585 -31.47406391572417, 144.84399355252685 -31.26123924022086))";

		Polygon polygon = (Polygon) read(WKT);
		assertTrue("ring", polygon.getExteriorRing().isClosed());
		assertEquals("arc string segment", CurveSegmentType.ARC_STRING,
				polygon.getExteriorRing().getCurveSegments().get(0).getSegmentType());
		assertEquals("one holes", 1, polygon.getInteriorRings().size());
		assertEquals("arc string segment", CurveSegmentType.LINE_STRING_SEGMENT,
				polygon.getInteriorRings().get(0).getCurveSegments().get(0).getSegmentType());

		WKT = "CURVEPOLYGON(COMPOUNDCURVE(CIRCULARSTRING(0 0,2 0, 2 1, 2 3, 4 3),(4 3, 4 5, 1 4, 0 0)), CIRCULARSTRING(1.7 1, 1.4 0.4, 1.6 0.4, 1.6 0.5, 1.7 1) )";
		polygon = (Polygon) read(WKT);
		assertTrue("ring", polygon.getExteriorRing().isClosed());
		assertEquals(RingType.Ring, polygon.getExteriorRing().getRingType());
		assertEquals(1, polygon.getExteriorRing().getMembers().size());
		assertEquals(CurveType.CompositeCurve, polygon.getExteriorRing().getMembers().get(0).getCurveType());

		assertEquals("one holes", 1, polygon.getInteriorRings().size());
		assertEquals("arc string segment", CurveSegmentType.ARC_STRING,
				polygon.getInteriorRings().get(0).getCurveSegments().get(0).getSegmentType());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testParseMulticurve() throws Exception {

		String WKT = "MULTICURVE((0 0, 5 5),CIRCULARSTRING(4 0, 4 4, 8 4))";
		MultiCurve<Curve> ml = (MultiCurve<Curve>) read(WKT);
		assertEquals(2, ml.size());
		assertEquals(CurveType.LineString, ml.get(0).getCurveType());
		assertEquals(CurveType.Curve, ml.get(1).getCurveType());
		assertEquals("arc string segment", CurveSegmentType.ARC_STRING,
				ml.get(1).getCurveSegments().get(0).getSegmentType());

		WKT = "MULTICURVE((100 100, 120 120), COMPOUNDCURVE(CIRCULARSTRING(0 0, 2 0, 2 1, 2 3, 4 3),(4 3, 4 5, 1 4, 0 0)))";
		ml = (MultiCurve<Curve>) read(WKT);
		assertEquals(2, ml.size());
		assertEquals(CurveType.LineString, ml.get(0).getCurveType());
		assertEquals(CurveType.CompositeCurve, ml.get(1).getCurveType());
	}

	@Test
	public void testMultiSurfaceStraightPolygon() throws Exception {
		String wkt = "MULTISURFACE (((0 0, 1 0, 1 4, 0 0)), CURVEPOLYGON (COMPOUNDCURVE (CIRCULARSTRING (0 2, 7 5, 2 10), (2 10, 0 2)), COMPOUNDCURVE (CIRCULARSTRING (3 9, 6 5, 3 2), (3 2, 3 9))))";
		Geometry geometry = read(wkt);
		@SuppressWarnings("unchecked")
		MultiSurface<Surface> ms = (MultiSurface<Surface>) geometry;

		assertEquals(2, ms.size());
		assertEquals(SurfaceType.Polygon, ms.get(0).getSurfaceType());
		assertEquals(SurfaceType.Polygon, ms.get(1).getSurfaceType());
	}

	@Test
	public void testMultiSurfaceStraightPolygon2() throws Exception {
		String wkt = "MULTISURFACE (CURVEPOLYGON (COMPOUNDCURVE (CIRCULARSTRING (0 2, 7 5, 2 10), (2 10, 0 2)), COMPOUNDCURVE (CIRCULARSTRING (3 9, 6 5, 3 2), (3 2, 3 9))), ((0 0, 1 0, 1 4, 0 0)))";
		Geometry geometry = read(wkt);

		@SuppressWarnings("unchecked")
		MultiSurface<Surface> ms = (MultiSurface<Surface>) geometry;

		assertEquals(2, ms.size());
		assertEquals(SurfaceType.Polygon, ms.get(0).getSurfaceType());
		assertEquals(SurfaceType.Polygon, ms.get(1).getSurfaceType());
	}

	@Test
	public void testMultiSurfaceEmpty() throws Exception {
		String wkt = "MULTISURFACE (EMPTY, CURVEPOLYGON (COMPOUNDCURVE (CIRCULARSTRING (0 2, 7 5, 2 10), (2 10, 0 2)), COMPOUNDCURVE (CIRCULARSTRING (3 9, 6 5, 3 2), (3 2, 3 9))), ((0 0, 1 0, 1 4, 0 0)))";
		Geometry geometry = read(wkt);

		@SuppressWarnings("unchecked")
		MultiSurface<Surface> ms = (MultiSurface<Surface>) geometry;

		assertEquals(3, ms.size());
		assertEquals(null, ms.get(0));
		assertEquals(SurfaceType.Polygon, ms.get(1).getSurfaceType());
		assertEquals(SurfaceType.Polygon, ms.get(2).getSurfaceType());
	}

	@Test
	public void testEmptyPoint() throws Exception {
		// TRICKY deegree does not have Geometry.isEmpty() so, read should return null
		String wkt = "POINT EMPTY";
		Geometry geom = read(wkt);
		assertNull(geom);
		wkt = "MULTIPOINT EMPTY";
		geom = read(wkt);
		assertNotNull(geom);
		assertThat(((MultiGeometry) geom).size(), is(0));
		wkt = "MULTIPOINT (EMPTY)";
		geom = read(wkt);
		assertNotNull(geom);
		assertThat(((MultiGeometry) geom).size(), is(1));
	}

	@Test
	public void testEmptyLineString() throws Exception {

		String wkt = "LINESTRING EMPTY";
		Geometry geom = read(wkt);
		assertNull(geom);
		wkt = "MULTILINESTRING EMPTY";
		geom = read(wkt);
		assertNotNull(geom);
		assertThat(((MultiGeometry) geom).size(), is(0));
		wkt = "MULTILINESTRING (EMPTY)";
		geom = read(wkt);
		assertNotNull(geom);
		assertThat(((MultiGeometry) geom).size(), is(1));
	}

	@Test
	public void testEmptyPolygon() throws Exception {
		// TRICKY deegree does not have Geometry.isEmpty() so, read should return null
		String wkt = "POLYGON EMPTY";
		Geometry geom = read(wkt);
		assertNull(geom);
		wkt = "MULTIPOLYGON EMPTY";
		geom = read(wkt);
		assertNotNull(geom);
		assertThat(((MultiGeometry) geom).size(), is(0));
		wkt = "MULTIPOLYGON (EMPTY)";
		geom = read(wkt);
		assertNotNull(geom);
		assertThat(((MultiGeometry) geom).size(), is(1));
	}

}