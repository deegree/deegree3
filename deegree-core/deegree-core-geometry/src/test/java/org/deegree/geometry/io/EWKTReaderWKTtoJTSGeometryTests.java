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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;

public class EWKTReaderWKTtoJTSGeometryTests {

	private Geometry readAsJTS(String wkt) throws ParseException {
		EWKTReader reader = new EWKTReader();
		org.deegree.geometry.Geometry g = reader.read(wkt);
		assertTrue(g instanceof AbstractDefaultGeometry);
		return ((AbstractDefaultGeometry) g).getJTSGeometry();
	}

	@Test
	public void verifyWKT() throws Exception {
		String WKT = "LINESTRING (60 380, 60 20, 200 400, 280 20, 360 400, 420 20, 500 400, 580 20, 620 400)";

		Geometry geometry = readAsJTS(WKT);
		assertNotNull(geometry);
		assertTrue(geometry instanceof LineString);
	}

	@Test
	public void multiPoint() throws Exception {
		String WKT = "MULTIPOINT (111 -47, 110 -46.5)";

		Geometry geometry = readAsJTS(WKT);
		assertNotNull(geometry);
		assertTrue(geometry instanceof MultiPoint);
		MultiPoint mp = (MultiPoint) geometry;
		assertEquals(2, mp.getNumGeometries());
		assertEquals(new Coordinate(111, -47), mp.getGeometryN(0).getCoordinate());
		assertEquals(new Coordinate(110, -46.5), mp.getGeometryN(1).getCoordinate());
	}

	@Test
	public void multiPointWithInnerParens() throws Exception {
		String WKT = "MULTIPOINT ((111 -47), (110 -46.5))";

		Geometry geometry = readAsJTS(WKT);
		assertNotNull(geometry);
		assertTrue(geometry instanceof MultiPoint);
		MultiPoint mp = (MultiPoint) geometry;
		assertEquals(2, mp.getNumGeometries());
		assertEquals(new Coordinate(111, -47), mp.getGeometryN(0).getCoordinate());
		assertEquals(new Coordinate(110, -46.5), mp.getGeometryN(1).getCoordinate());
	}

	/**
	 * Draw a circle between the start and end point; or each group of three their after.
	 * @throws Exception
	 */
	@Test
	public void circularString() throws Exception {
		String WKT = "CIRCULARSTRING(143.62025166838282 -30.037497356076827, 142.92857147299705 -32.75101196874403, 145.96132309891922 -34.985671061528784, 149.57565307617188 -33.41153335571289, 149.41972407584802 -29.824672680573517, 146.1209416055467 -30.19711586270431, 143.62025166838282 -30.037497356076827)";
		Geometry geometry = readAsJTS(WKT);
		assertNotNull("parsed circularstring ring", geometry);
		Coordinate[] array = geometry.getCoordinates();
		assertEquals("forms a ring", array[0], array[array.length - 1]);

		WKT = "CIRCULARSTRING(143.62025166838282 -30.037497356076827, 142.92857147299705 -32.75101196874403, 143.62025166838282 -30.037497356076827)";
		geometry = readAsJTS(WKT);
		assertNotNull("parsed perfect circle", geometry);
		assertEquals(100, geometry.getNumPoints());

		// this wont work, as CIRCULARSTRING EMPTY is read as null
		// WKT = "CIRCULARSTRING EMPTY";
		// geometry = readAsJTS( WKT );
		// assertNotNull( geometry );
		// assertTrue( geometry.isEmpty() );
	}

	@Test
	public void compoundCurve() throws Exception {
		String WKT = "COMPOUNDCURVE((153.72942375 -27.21757040, 152.29285719 -29.23940482, 154.74034096 -30.51635287),CIRCULARSTRING(154.74034096 -30.51635287, 154.74034096 -30.51635287, 152.39926953 -32.16574411, 155.11278414 -34.08116619, 151.86720784 -35.62414508))";

		Geometry geometry = readAsJTS(WKT);
		assertNotNull(geometry);

		WKT = "COMPOUNDCURVE((153.72942375 -27.21757040, 152.29285719 -29.23940482, 154.74034096 -30.51635287))";
		geometry = readAsJTS(WKT);
		assertNotNull(geometry);

		WKT = "COMPOUNDCURVE(CIRCULARSTRING(154.74034096 -30.51635287, 154.74034096 -30.51635287, 152.39926953 -32.16574411, 155.11278414 -34.08116619, 151.86720784 -35.62414508))";
		geometry = readAsJTS(WKT);
		assertNotNull(geometry);

		// this wont work, as COMPOUNDCURVE EMPTY is read as null
		// WKT = "COMPOUNDCURVE EMPTY";
		// geometry = readAsJTS( WKT );
		// assertNotNull( geometry );
		// assertTrue( geometry.isEmpty() );
	}

	@Test
	public void curvePolygon() throws Exception {

		// perfect circle!

		String WKT;
		Polygon polygon;
		Geometry geometry;

		WKT = "CURVEPOLYGON(CIRCULARSTRING(143.62025166838282 -30.037497356076827, 142.92857147299705 -32.75101196874403, 143.62025166838282 -30.037497356076827))";
		geometry = readAsJTS(WKT);
		assertNotNull("read curvepolygon", geometry);
		assertTrue(geometry instanceof Polygon);
		polygon = (Polygon) geometry;
		// TODO//assertTrue(polygon.getExteriorRing() instanceof CircularRing);
		assertTrue("ring", polygon.getExteriorRing().isClosed());
		assertEquals("segmented ring", 100, polygon.getExteriorRing().getNumPoints());
		assertEquals("no holes", 0, polygon.getNumInteriorRing());

		WKT = "CURVEPOLYGON((144.84399355252685 -31.26123924022086, 144.20551952601693 -32.27215644886158, 145.55230712890625 -33.49203872680664, 147.97080993652344 -32.03618621826172, 146.38697244992585 -31.47406391572417, 144.84399355252685 -31.26123924022086))";
		polygon = (Polygon) readAsJTS(WKT);
		assertTrue("ring", polygon.getExteriorRing().isClosed());
		assertEquals("no holes", 0, polygon.getNumInteriorRing());

	}

	@Test
	public void testParseMulticurve() throws Exception {

		String WKT;
		MultiLineString ml;
		// this wont work, as MULTICURVE EMPTY is read as null
		// WKT = "MULTICURVE EMPTY";
		// ml = (MultiLineString) readAsJTS( WKT );
		// assertTrue( ml.isEmpty() );

		WKT = "MULTICURVE((0 0, 5 5),CIRCULARSTRING(4 0, 4 4, 8 4))";
		ml = (MultiLineString) readAsJTS(WKT);
		assertEquals(2, ml.getNumGeometries());
		assertTrue(ml.getGeometryN(0).getClass() == LineString.class);

		WKT = "MULTICURVE((100 100, 120 120), COMPOUNDCURVE(CIRCULARSTRING(0 0, 2 0, 2 1, 2 3, 4 3),(4 3, 4 5, 1 4, 0 0)))";
		ml = (MultiLineString) readAsJTS(WKT);
		// this is 3 instead of 2 as it get linearized
		assertEquals(3, ml.getNumGeometries());
		assertTrue(ml.getGeometryN(0).getClass() == LineString.class);
	}

	@Test
	public void testCaseInsensitive() throws Exception {

		assertNotNull(readAsJTS("POINT(1 2)"));
		assertNotNull(readAsJTS("Point(1 2)"));

		assertNotNull(readAsJTS("LINESTRING(0 2, 2 0, 8 6)"));
		assertNotNull(readAsJTS("LineString(0 2, 2 0, 8 6)"));
	}

}
