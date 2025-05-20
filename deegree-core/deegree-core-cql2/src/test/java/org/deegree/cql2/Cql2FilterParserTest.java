/*-
 * #%L
 * deegree-ogcapi-features - OGC API Features (OAF) implementation - Querying and modifying of geospatial data objects
 * %%
 * Copyright (C) 2019 - 2024 lat/lon GmbH, info@lat-lon.de, www.lat-lon.de
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.deegree.cql2;

import static java.util.Calendar.APRIL;
import static org.deegree.cql2.CQL2FilterParser.parseCql2Filter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.xml.namespace.QName;
import java.util.Calendar;
import java.util.Collections;
import java.util.Set;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.filter.Expression;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.spatial.Intersects;
import org.deegree.filter.temporal.After;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class Cql2FilterParserTest {

	private static final Set<QName> PROPS = Collections.singleton(new QName("http://deegree.or/ns", "testDate"));

	@Test
	public void test_parse_S_INTERSECTS_Point() throws UnknownCRSException {
		String intersects = "S_INTERSECTS(geometry,POINT(36.319836 32.288087))";
		Object visit = parseCql2Filter(intersects, crs(), PROPS);

		assertTrue(visit instanceof Intersects);

		Expression param1 = ((Intersects) visit).getParam1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals(((ValueReference) param1).getAsQName().getLocalPart(), "geometry");

		Geometry geometry = ((Intersects) visit).getGeometry();
		assertTrue(geometry instanceof Point);
		assertEquals(((Point) geometry).get(0), 36.319836, 0.0001);
		assertEquals(((Point) geometry).get(1), 32.288087, 0.0001);
		assertEquals(((Point) geometry).get(2), 0, 0.0001);
	}

	@Test
	public void test_parse_S_INTERSECTS_LineString() throws UnknownCRSException {
		String intersects = "S_INTERSECTS(geometry,LINESTRING(36.319836 32.288087,37.319836 33.288087,38.319836 34.288087))";
		Object visit = parseCql2Filter(intersects, crs(), PROPS);

		assertTrue(visit instanceof Intersects);

		Expression param1 = ((Intersects) visit).getParam1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals(((ValueReference) param1).getAsQName().getLocalPart(), "geometry");

		Geometry geometry = ((Intersects) visit).getGeometry();
		assertTrue(geometry instanceof LineString);
		assertEquals(((LineString) geometry).getControlPoints().size(), 3);
	}

	@Test
	public void test_parse_S_INTERSECTS_Polygon() throws UnknownCRSException {
		String intersects = "S_INTERSECTS(geometry,POLYGON((36.319836 32.288087,37.319836 33.288087,38.319836 34.288087,36.319836 32.288087)))";
		Object visit = parseCql2Filter(intersects, crs(), PROPS);

		assertTrue(visit instanceof Intersects);

		Expression param1 = ((Intersects) visit).getParam1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals(((ValueReference) param1).getAsQName().getLocalPart(), "geometry");

		Geometry geometry = ((Intersects) visit).getGeometry();
		assertTrue(geometry instanceof Polygon);
		assertEquals(((Polygon) geometry).getExteriorRing().getControlPoints().size(), 4);
		assertEquals(((Polygon) geometry).getInteriorRings().size(), 0);
	}

	@Test
	public void test_parse_S_INTERSECTS_MultiPoint() throws UnknownCRSException {
		String intersects = "S_INTERSECTS(geometry,MULTIPOINT((36.319836 32.288087),(37.319836 33.288087)))";
		Object visit = parseCql2Filter(intersects, crs(), PROPS);

		assertTrue(visit instanceof Intersects);

		Expression param1 = ((Intersects) visit).getParam1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals(((ValueReference) param1).getAsQName().getLocalPart(), "geometry");

		Geometry geometry = ((Intersects) visit).getGeometry();
		assertTrue(geometry instanceof MultiPoint);
		assertEquals(((MultiPoint) geometry).size(), 2);
		assertEquals(((MultiPoint) geometry).get(0).get(0), 36.319836, 0.0001);
		assertEquals(((MultiPoint) geometry).get(0).get(1), 32.288087, 0.0001);
		assertEquals(((MultiPoint) geometry).get(1).get(0), 37.319836, 0.0001);
		assertEquals(((MultiPoint) geometry).get(1).get(1), 33.288087, 0.0001);
	}

	@Test
	public void test_parse_S_INTERSECTS_MultiLineString() throws UnknownCRSException {
		String intersects = "S_INTERSECTS(geometry,MULTILINESTRING((36.319836 32.288087,37.319836 33.288087,38.319836 34.288087),(46.319836 32.288087,47.319836 33.288087,48.319836 34.288087)))";
		Object visit = parseCql2Filter(intersects, crs(), PROPS);

		assertTrue(visit instanceof Intersects);

		Expression param1 = ((Intersects) visit).getParam1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals(((ValueReference) param1).getAsQName().getLocalPart(), "geometry");

		Geometry geometry = ((Intersects) visit).getGeometry();
		assertTrue(geometry instanceof MultiLineString);
		assertEquals(((MultiLineString) geometry).size(), 2);
		assertEquals(((MultiLineString) geometry).get(0).getControlPoints().size(), 3);
		assertEquals(((MultiLineString) geometry).get(1).getControlPoints().size(), 3);
	}

	@Test
	public void test_parse_S_INTERSECTS_MultiPolygon() throws UnknownCRSException {
		String intersects = "S_INTERSECTS(geometry,MULTIPOLYGON(((36.319836 32.288087,37.319836 33.288087,38.319836 34.288087,36.319836 32.288087)),((46.319836 32.288087,47.319836 33.288087,48.319836 34.288087,46.319836 32.288087)))))";
		Object visit = parseCql2Filter(intersects, crs(), PROPS);

		assertTrue(visit instanceof Intersects);

		Expression param1 = ((Intersects) visit).getParam1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals(((ValueReference) param1).getAsQName().getLocalPart(), "geometry");

		Geometry geometry = ((Intersects) visit).getGeometry();
		assertTrue(geometry instanceof MultiPolygon);
		assertEquals(((MultiPolygon) geometry).size(), 2);
		assertEquals(((MultiPolygon) geometry).get(0).getExteriorRing().getControlPoints().size(), 4);
		assertEquals(((MultiPolygon) geometry).get(0).getInteriorRings().size(), 0);
		assertEquals(((MultiPolygon) geometry).get(1).getExteriorRing().getControlPoints().size(), 4);
		assertEquals(((MultiPolygon) geometry).get(1).getInteriorRings().size(), 0);
	}

	@Test
	public void test_parse_S_INTERSECTS_GeometryCollection() throws UnknownCRSException {
		String intersects = "S_INTERSECTS(geometry,GEOMETRYCOLLECTION(POINT(36.319836 32.288087),LINESTRING(36.319836 32.288087,37.319836 33.288087,38.319836 34.288087),POLYGON((36.319836 32.288087,37.319836 33.288087,38.319836 34.288087,36.319836 32.288087))))";
		Object visit = parseCql2Filter(intersects, crs(), PROPS);

		assertTrue(visit instanceof Intersects);

		Expression param1 = ((Intersects) visit).getParam1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals(((ValueReference) param1).getAsQName().getLocalPart(), "geometry");

		Geometry geometry = ((Intersects) visit).getGeometry();
		assertTrue(geometry instanceof MultiGeometry);
		assertEquals(((MultiGeometry) geometry).size(), 3);
		assertTrue(((MultiGeometry) geometry).get(0) instanceof Point);
		assertTrue(((MultiGeometry) geometry).get(1) instanceof LineString);
		assertTrue(((MultiGeometry) geometry).get(2) instanceof Polygon);
	}

	@Test
	public void test_parse_S_INTERSECTS_Bbox() throws UnknownCRSException {
		String intersects = "S_INTERSECTS(geometry,BBOX(36.319836,32.288087,37.319836,33.288087))";
		Object visit = parseCql2Filter(intersects, crs(), PROPS);

		assertTrue(visit instanceof Intersects);

		Expression param1 = ((Intersects) visit).getParam1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals(((ValueReference) param1).getAsQName().getLocalPart(), "geometry");

		Geometry geometry = ((Intersects) visit).getGeometry();
		assertTrue(geometry instanceof Envelope);
		assertEquals(((Envelope) geometry).getMin().get(0), 36.319836, 0.0001);
		assertEquals(((Envelope) geometry).getMin().get(1), 32.288087, 0.0001);
		assertEquals(((Envelope) geometry).getMax().get(0), 37.319836, 0.0001);
		assertEquals(((Envelope) geometry).getMax().get(1), 33.288087, 0.0001);
	}

	@Test
	public void test_parse_T_AFTER_date() throws UnknownCRSException {
		String after = "T_AFTER(testDate,DATE('2025-04-14'))";
		Object visit = parseCql2Filter(after, crs(), PROPS);

		assertTrue(visit instanceof After);

		Expression param1 = ((After) visit).getParameter1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals("testDate", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals(PROPS.stream().findFirst().get().getNamespaceURI(),
				((ValueReference) param1).getAsQName().getNamespaceURI());

		Expression date = ((After) visit).getParameter2();
		assertTrue(date instanceof Literal);
		TypedObjectNode primitiveValue = ((Literal<?>) date).getValue();
		assertTrue(primitiveValue instanceof PrimitiveValue);
		Object value = ((PrimitiveValue) primitiveValue).getValue();
		assertTrue(value instanceof Date);
		Calendar calendar = ((Date) value).getCalendar();
		assertEquals(2025, calendar.get(Calendar.YEAR));
		assertEquals(APRIL, calendar.get(Calendar.MONTH));
		assertEquals(14, calendar.get(Calendar.DAY_OF_MONTH));
	}

	@Test
	public void test_parse_T_AFTER_timestamp() throws UnknownCRSException {
		String after = "T_AFTER(testDate,TIMESTAMP('2025-04-14T08:59:30Z'))";
		Object visit = parseCql2Filter(after, crs(), PROPS);

		assertTrue(visit instanceof After);

		Expression param1 = ((After) visit).getParameter1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals("testDate", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals(PROPS.stream().findFirst().get().getNamespaceURI(),
				((ValueReference) param1).getAsQName().getNamespaceURI());

		Expression date = ((After) visit).getParameter2();
		assertTrue(date instanceof Literal);
		TypedObjectNode primitiveValue = ((Literal<?>) date).getValue();
		assertTrue(primitiveValue instanceof PrimitiveValue);
		Object value = ((PrimitiveValue) primitiveValue).getValue();
		assertTrue(value instanceof DateTime);
		Calendar calendar = ((DateTime) value).getCalendar();
		assertEquals(2025, calendar.get(Calendar.YEAR));
		assertEquals(APRIL, calendar.get(Calendar.MONTH));
		assertEquals(14, calendar.get(Calendar.DAY_OF_MONTH));
	}

	private static ICRS crs() throws UnknownCRSException {
		return CRSManager.lookup("urn:ogc:def:crs:OGC:1.3:CRS84");
	}

}
