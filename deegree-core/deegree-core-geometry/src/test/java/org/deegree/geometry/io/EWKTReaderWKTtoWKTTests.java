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

import java.io.StringWriter;
import java.util.EnumSet;
import java.util.LinkedList;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKTWriter.WKTFlag;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class EWKTReaderWKTtoWKTTests {

	@Parameter(0)
	public int decimalPlaces;

	@Parameter(1)
	public String wkt;

	EWKTReader reader = new EWKTReader();

	private static Object[] of(Object... lst) {
		return lst;
	}

	@Parameters(name = "{index}: WKT={1}")
	public static Iterable<Object[]> data() {
		// This cannot be tested because it is not distinguishable from a normal polygon
		// CURVEPOLYGON ((144.84399355252685 -31.26123924022086,144.20551952601693
		// -32.27215644886158,145.55230712890625
		// -33.49203872680664,147.97080993652344 -32.03618621826172,146.38697244992585
		// -31.47406391572417,144.84399355252685 -31.26123924022086))
		//
		// Multipoint with inner brackets cannot be distinguished by their syntax, since
		// the brackets are not mapped in
		// the data model.
		// MULTIPOINT ((111.0 -47),(110.0 -46.5))

		LinkedList<Object[]> tests = new LinkedList<>();
		tests.add(of(0, "LINESTRING (60 380,60 20,200 400,280 20,360 400,420 20,500 400,580 20,620 400)"));
		tests.add(of(1, "MULTIPOINT (111.0 -47.0,110.0 -46.5)"));
		tests.add(of(9,
				"CIRCULARSTRING (220268.439465645 150415.359530563,220227.333322076 150505.561285879,220227.353105332 150406.434743975)"));
		tests.add(of(14,
				"CIRCULARSTRING (143.62025166838282 -30.03749735607682,142.92857147299705 -32.75101196874403,145.96132309891922 -34.98567106152878,149.57565307617188 -33.41153335571289,149.41972407584802 -29.82467268057351,146.12094160554670 -30.19711586270431,143.62025166838282 -30.03749735607682)"));
		tests.add(of(14,
				"CIRCULARSTRING (143.62025166838282 -30.03749735607682,142.92857147299705 -32.75101196874403,143.62025166838282 -30.03749735607682)"));
		tests.add(of(8,
				"COMPOUNDCURVE ((153.72942375 -27.21757040,152.29285719 -29.23940482,154.74034096 -30.51635287),CIRCULARSTRING (154.74034096 -30.51635287,154.74034096 -30.51635287,152.39926953 -32.16574411,155.11278414 -34.08116619,151.86720784 -35.62414508))"));
		tests.add(of(8,
				"COMPOUNDCURVE ((153.72942375 -27.21757040,152.29285719 -29.23940482,154.74034096 -30.51635287))"));
		tests.add(of(8,
				"COMPOUNDCURVE (CIRCULARSTRING (154.74034096 -30.51635287,154.74034096 -30.51635287,152.39926953 -32.16574411,155.11278414 -34.08116619,151.86720784 -35.62414508))"));
		tests.add(of(14,
				"CURVEPOLYGON (CIRCULARSTRING (143.62025166838282 -30.03749735607682,142.92857147299705 -32.75101196874403,143.62025166838282 -30.03749735607682))"));
		tests.add(of(14, "CURVEPOLYGON ("
				+ "CIRCULARSTRING (143.62025166838282 -30.03749735607682,142.92857147299705 -32.75101196874403,145.96132309891922 -34.98567106152878,149.57565307617188 -33.41153335571289,149.41972407584802 -29.82467268057351,146.12094160554670 -30.19711586270431,143.62025166838282 -30.03749735607682),"
				+ "(144.84399355252685 -31.26123924022086,144.20551952601693 -32.27215644886158,145.55230712890625 -33.49203872680664,147.97080993652344 -32.03618621826172,146.38697244992585 -31.47406391572417,144.84399355252685 -31.26123924022086))"));
		tests.add(of(1,
				"CURVEPOLYGON (COMPOUNDCURVE (CIRCULARSTRING (0.0 0.0,2.0 0.0,2.0 1.0,2.0 3.0,4.0 3.0),(4.0 3.0,4.0 5.0,1.0 4.0,0.0 0.0)),CIRCULARSTRING (1.7 1.0,1.4 0.4,1.6 0.4,1.6 0.5,1.7 1.0))"));
		tests.add(of(0, "MULTICURVE ((0 0,5 5),CIRCULARSTRING (4 0,4 4,8 4))"));
		tests.add(of(0,
				"MULTICURVE ((100 100,120 120),COMPOUNDCURVE (CIRCULARSTRING (0 0,2 0,2 1,2 3,4 3),(4 3,4 5,1 4,0 0)))"));
		tests.add(of(0, "POINT (1 2)"));
		tests.add(of(0, "LINESTRING (0 2,2 0,8 6)"));
		tests.add(of(0,
				"MULTISURFACE (((0 0,1 0,1 4,0 0)),CURVEPOLYGON (COMPOUNDCURVE (CIRCULARSTRING (0 2,7 5,2 10),(2 10,0 2)),COMPOUNDCURVE (CIRCULARSTRING (3 9,6 5,3 2),(3 2,3 9))))"));
		tests.add(of(0,
				"MULTISURFACE (CURVEPOLYGON (COMPOUNDCURVE (CIRCULARSTRING (0 2,7 5,2 10),(2 10,0 2)),COMPOUNDCURVE (CIRCULARSTRING (3 9,6 5,3 2),(3 2,3 9))),((0 0,1 0,1 4,0 0)))"));
		return tests;
	}

	@Test
	public void verifyWKT() throws Exception {
		Geometry geom = reader.read(wkt);
		assertNotNull(geom);
		StringWriter sw = new StringWriter();
		WKTWriter writer = new WKTWriter(EnumSet.of(WKTFlag.USE_SQL_MM), new DecimalCoordinateFormatter(decimalPlaces));
		writer.writeGeometry(geom, sw);
		String outWKT = sw.toString();
		assertNotNull(outWKT);
		assertEquals(wkt, outWKT);
	}

}
