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
package org.deegree.geometry.io;

import static org.deegree.geometry.io.WKTWriter.WKTFlag.USE_SQL_MM;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.segments.ArcString;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.junit.Test;

public class WKTWriterSqlMMTests {

	private static GeometryFactory fac = new GeometryFactory();

	private static WKTWriter wkt = new WKTWriter(Set.of(USE_SQL_MM), new DecimalCoordinateFormatter(0));

	@Test
	public void testPolygonFromArcString() throws IOException {
		List<Point> points = List.of(fac.createPoint(null, 0, 0, null), //
				fac.createPoint(null, 10, 0, null), //
				fac.createPoint(null, 0, 0, null));
		ArcString circle = fac.createArcString(fac.createPoints(points));
		Ring ring = fac.createRing(null, null, List.of(fac.createCurve(null, null, circle)));
		Geometry geom = fac.createPolygon(null, null, ring, null);

		StringWriter out = new StringWriter();
		wkt.writeGeometry(geom, out);
		assertThat(out.toString(), is("CURVEPOLYGON (CIRCULARSTRING (0 0,10 0,0 0))"));
	}

	@Test
	public void testCurveArcString() throws IOException {
		List<Point> points = List.of(fac.createPoint(null, 0, 0, null), //
				fac.createPoint(null, 10, 0, null), //
				fac.createPoint(null, 0, 0, null));
		ArcString circle = fac.createArcString(fac.createPoints(points));
		Curve curve = fac.createCurve(null, null, circle);

		StringWriter out = new StringWriter();
		wkt.writeCurveGeometry(curve, out);
		assertThat(out.toString(), is("CIRCULARSTRING (0 0,10 0,0 0)"));
	}

	@Test
	public void testCompoundCurve() throws IOException {
		List<Point> points = List.of(fac.createPoint(null, 0, 0, null), //
				fac.createPoint(null, 10, 0, null), //
				fac.createPoint(null, 0, 0, null));
		ArcString circle = fac.createArcString(fac.createPoints(points));
		LineStringSegment line = fac.createLineStringSegment(fac.createPoints(points));
		Curve curve = fac.createCurve(null, null, circle, line);

		StringWriter out = new StringWriter();
		wkt.writeCurveGeometry(curve, out);
		assertThat(out.toString(), is("COMPOUNDCURVE (CIRCULARSTRING (0 0,10 0,0 0),(0 0,10 0,0 0))"));
	}

}
