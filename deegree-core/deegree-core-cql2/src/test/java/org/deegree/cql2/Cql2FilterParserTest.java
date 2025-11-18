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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import javax.xml.namespace.QName;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.filter.Expression;
import org.deegree.filter.Operator;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.Not;
import org.deegree.filter.logical.Or;
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

	public static final String NS_URL = "http://deegree.or/ns";

	private static final Set<QName> PROPS = Set.of(new QName(NS_URL, "geometry"), new QName(NS_URL, "testDate"),
			new QName(NS_URL, "test"), new QName(NS_URL, "test1"), new QName(NS_URL, "test2"));

	private static final List<FilterProperty> FILTERPROPS = List.of(
			new FilterProperty(new QName("testDate"), FilterPropertyType.DATE),
			new FilterProperty(new QName("testString"), FilterPropertyType.STRING),
			new FilterProperty(new QName("geometry"), FilterPropertyType.GEOMETRY));

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
	public void parse_s_intersects_point() throws Exception {
		String intersects = "S_INTERSECTS(geometry,POINT(36.319836 32.288087))";
		Object visit = parseCql2Filter(intersects, crs(), FILTERPROPS);

		assertTrue(visit instanceof Intersects);

		Expression param1 = ((Intersects) visit).getParam1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals("geometry", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals("", ((ValueReference) param1).getAsQName().getNamespaceURI());

		Geometry geometry = ((Intersects) visit).getGeometry();
		assertTrue(geometry instanceof Point);
		assertEquals(36.319836, ((Point) geometry).get(0), 0.0001);
		assertEquals(32.288087, ((Point) geometry).get(1), 0.0001);
		assertEquals(0, ((Point) geometry).get(2), 0.0001);
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
	public void parse_s_intersects_line_string_fp() throws Exception {
		String intersects = "S_INTERSECTS(geometry,LINESTRING(36.319836 32.288087,37.319836 33.288087,38.319836 34.288087))";
		Object visit = parseCql2Filter(intersects, crs(), FILTERPROPS);

		assertTrue(visit instanceof Intersects);

		Expression param1 = ((Intersects) visit).getParam1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals("geometry", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals("", ((ValueReference) param1).getAsQName().getNamespaceURI());

		Geometry geometry = ((Intersects) visit).getGeometry();
		assertTrue(geometry instanceof LineString);
		assertEquals(3, ((LineString) geometry).getControlPoints().size());
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
	public void parse_s_intersects_polygon() throws Exception {
		String intersects = "S_INTERSECTS(geometry,POLYGON((36.319836 32.288087,37.319836 33.288087,38.319836 34.288087,36.319836 32.288087)))";
		Object visit = parseCql2Filter(intersects, crs(), FILTERPROPS);

		assertTrue(visit instanceof Intersects);

		Expression param1 = ((Intersects) visit).getParam1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals("geometry", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals("", ((ValueReference) param1).getAsQName().getNamespaceURI());

		Geometry geometry = ((Intersects) visit).getGeometry();
		assertTrue(geometry instanceof Polygon);
		assertEquals(4, ((Polygon) geometry).getExteriorRing().getControlPoints().size());
		assertEquals(0, ((Polygon) geometry).getInteriorRings().size());
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
	public void parse_s_intersects_multi_point() throws Exception {
		String intersects = "S_INTERSECTS(geometry,MULTIPOINT((36.319836 32.288087),(37.319836 33.288087)))";
		Object visit = parseCql2Filter(intersects, crs(), FILTERPROPS);

		assertTrue(visit instanceof Intersects);

		Expression param1 = ((Intersects) visit).getParam1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals("geometry", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals("", ((ValueReference) param1).getAsQName().getNamespaceURI());

		Geometry geometry = ((Intersects) visit).getGeometry();
		assertTrue(geometry instanceof MultiPoint);
		assertEquals(2, ((MultiPoint) geometry).size());
		assertEquals(36.319836, ((MultiPoint) geometry).get(0).get(0), 0.0001);
		assertEquals(32.288087, ((MultiPoint) geometry).get(0).get(1), 0.0001);
		assertEquals(37.319836, ((MultiPoint) geometry).get(1).get(0), 0.0001);
		assertEquals(33.288087, ((MultiPoint) geometry).get(1).get(1), 0.0001);
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
	public void parse_s_intersects_multi_line_string() throws Exception {
		String intersects = "S_INTERSECTS(geometry,MULTILINESTRING((36.319836 32.288087,37.319836 33.288087,38.319836 34.288087),(46.319836 32.288087,47.319836 33.288087,48.319836 34.288087)))";
		Object visit = parseCql2Filter(intersects, crs(), FILTERPROPS);

		assertTrue(visit instanceof Intersects);

		Expression param1 = ((Intersects) visit).getParam1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals("geometry", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals("", ((ValueReference) param1).getAsQName().getNamespaceURI());

		Geometry geometry = ((Intersects) visit).getGeometry();
		assertTrue(geometry instanceof MultiLineString);
		assertEquals(2, ((MultiLineString) geometry).size());
		assertEquals(3, ((MultiLineString) geometry).get(0).getControlPoints().size());
		assertEquals(3, ((MultiLineString) geometry).get(1).getControlPoints().size());
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
	public void parse_s_intersects_multi_polygon() throws Exception {
		String intersects = "S_INTERSECTS(geometry,MULTIPOLYGON(((36.319836 32.288087,37.319836 33.288087,38.319836 34.288087,36.319836 32.288087)),((46.319836 32.288087,47.319836 33.288087,48.319836 34.288087,46.319836 32.288087)))))";
		Object visit = parseCql2Filter(intersects, crs(), FILTERPROPS);

		assertTrue(visit instanceof Intersects);

		Expression param1 = ((Intersects) visit).getParam1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals("geometry", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals("", ((ValueReference) param1).getAsQName().getNamespaceURI());

		Geometry geometry = ((Intersects) visit).getGeometry();
		assertTrue(geometry instanceof MultiPolygon);
		assertEquals(2, ((MultiPolygon) geometry).size());
		assertEquals(4, ((MultiPolygon) geometry).get(0).getExteriorRing().getControlPoints().size());
		assertEquals(0, ((MultiPolygon) geometry).get(0).getInteriorRings().size());
		assertEquals(4, ((MultiPolygon) geometry).get(1).getExteriorRing().getControlPoints().size());
		assertEquals(0, ((MultiPolygon) geometry).get(1).getInteriorRings().size());
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
	public void parse_s_intersects_geometry_collection() throws Exception {
		String intersects = "S_INTERSECTS(geometry,GEOMETRYCOLLECTION(POINT(36.319836 32.288087),LINESTRING(36.319836 32.288087,37.319836 33.288087,38.319836 34.288087),POLYGON((36.319836 32.288087,37.319836 33.288087,38.319836 34.288087,36.319836 32.288087))))";
		Object visit = parseCql2Filter(intersects, crs(), FILTERPROPS);

		assertTrue(visit instanceof Intersects);

		Expression param1 = ((Intersects) visit).getParam1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals("geometry", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals("", ((ValueReference) param1).getAsQName().getNamespaceURI());

		Geometry geometry = ((Intersects) visit).getGeometry();
		assertTrue(geometry instanceof MultiGeometry);
		assertEquals(3, ((MultiGeometry) geometry).size());
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
	public void parse_s_intersects_bbox() throws Exception {
		String intersects = "S_INTERSECTS(geometry,BBOX(36.319836,32.288087,37.319836,33.288087))";
		Object visit = parseCql2Filter(intersects, crs(), FILTERPROPS);

		assertTrue(visit instanceof Intersects);

		Expression param1 = ((Intersects) visit).getParam1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals("geometry", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals("", ((ValueReference) param1).getAsQName().getNamespaceURI());

		Geometry geometry = ((Intersects) visit).getGeometry();
		assertTrue(geometry instanceof Envelope);
		assertEquals(36.319836, ((Envelope) geometry).getMin().get(0), 0.0001);
		assertEquals(32.288087, ((Envelope) geometry).getMin().get(1), 0.0001);
		assertEquals(37.319836, ((Envelope) geometry).getMax().get(0), 0.0001);
		assertEquals(33.288087, ((Envelope) geometry).getMax().get(1), 0.0001);
	}

	@Test
	public void parse_s_intersects_non_geom_property() {
		String intersects = "S_INTERSECTS(testString,BBOX(36.319836,32.288087,37.319836,33.288087))";
		assertThrows(IllegalArgumentException.class, () -> parseCql2Filter(intersects, crs(), PROPS));
	}

	@Test
	public void test_parse_T_AFTER_date() throws UnknownCRSException {
		String after = "T_AFTER(testDate,DATE('2025-04-14'))";
		Object visit = parseCql2Filter(after, crs(), PROPS);

		assertTrue(visit instanceof After);

		Expression param1 = ((After) visit).getParameter1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals("testDate", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals(NS_URL, ((ValueReference) param1).getAsQName().getNamespaceURI());

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
	public void parse_t_after_date() throws Exception {
		String after = "T_AFTER(testDate,DATE('2025-04-14'))";
		Object visit = parseCql2Filter(after, crs(), FILTERPROPS);

		assertTrue(visit instanceof After);

		Expression param1 = ((After) visit).getParameter1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals("testDate", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals("", ((ValueReference) param1).getAsQName().getNamespaceURI());

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
		assertEquals(NS_URL, ((ValueReference) param1).getAsQName().getNamespaceURI());

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

	@Test
	public void parse_t_after_timestamp() throws Exception {
		String after = "T_AFTER(testDate,TIMESTAMP('2025-04-14T08:59:30Z'))";
		Object visit = parseCql2Filter(after, crs(), FILTERPROPS);

		assertTrue(visit instanceof After);

		Expression param1 = ((After) visit).getParameter1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals("testDate", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals("", ((ValueReference) param1).getAsQName().getNamespaceURI());

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

	@Test
	public void parse_t_after_non_date_property() throws Exception {
		String after = "T_AFTER(testString,TIMESTAMP('2025-04-14T08:59:30Z'))";
		assertThrows(IllegalArgumentException.class, () -> parseCql2Filter(after, crs(), PROPS));
	}

	@Test
	public void test_parse_comparison() throws UnknownCRSException {
		String comp = "test='VALUE'";
		Object visit = parseCql2Filter(comp, crs(), PROPS);

		assertTrue(visit instanceof PropertyIsEqualTo);
		PropertyIsEqualTo propertyIsEqualTo = (PropertyIsEqualTo) visit;
		assertFalse(propertyIsEqualTo.isMatchCase());

		Expression param1 = propertyIsEqualTo.getParameter1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals("test", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals(NS_URL, ((ValueReference) param1).getAsQName().getNamespaceURI());

		Expression param2 = propertyIsEqualTo.getParameter2();
		assertTrue(param2 instanceof Literal);
		TypedObjectNode primitiveValue = ((Literal<?>) param2).getValue();
		assertTrue(primitiveValue instanceof PrimitiveValue);
		Object value = ((PrimitiveValue) primitiveValue).getValue();
		assertTrue(value instanceof String);
		assertEquals("VALUE", value);
	}

	@Test
	public void test_parse_comparison_int() throws UnknownCRSException {
		String comp = "test=10";
		Object visit = parseCql2Filter(comp, crs(), PROPS);

		assertTrue(visit instanceof PropertyIsEqualTo);
		PropertyIsEqualTo propertyIsEqualTo = (PropertyIsEqualTo) visit;
		assertFalse(propertyIsEqualTo.isMatchCase());

		Expression param1 = propertyIsEqualTo.getParameter1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals("test", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals(NS_URL, ((ValueReference) param1).getAsQName().getNamespaceURI());

		Expression param2 = propertyIsEqualTo.getParameter2();
		assertTrue(param2 instanceof Literal);
		TypedObjectNode primitiveValue = ((Literal<?>) param2).getValue();
		assertTrue(primitiveValue instanceof PrimitiveValue);
		assertEquals(BaseType.INTEGER, ((PrimitiveValue) primitiveValue).getType().getBaseType());
		Object value = ((PrimitiveValue) primitiveValue).getValue();
		assertTrue(value instanceof Integer);
		assertEquals(10, value);
	}

	@Test
	public void test_parse_comparison_double() throws UnknownCRSException {
		String comp = "test=10.9";
		Object visit = parseCql2Filter(comp, crs(), PROPS);

		assertTrue(visit instanceof PropertyIsEqualTo);
		PropertyIsEqualTo propertyIsEqualTo = (PropertyIsEqualTo) visit;
		assertFalse(propertyIsEqualTo.isMatchCase());

		Expression param1 = propertyIsEqualTo.getParameter1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals("test", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals(NS_URL, ((ValueReference) param1).getAsQName().getNamespaceURI());

		Expression param2 = propertyIsEqualTo.getParameter2();
		assertTrue(param2 instanceof Literal);
		TypedObjectNode primitiveValue = ((Literal<?>) param2).getValue();
		assertTrue(primitiveValue instanceof PrimitiveValue);
		assertEquals(BaseType.DOUBLE, ((PrimitiveValue) primitiveValue).getType().getBaseType());
		Object value = ((PrimitiveValue) primitiveValue).getValue();
		assertTrue(value instanceof Double);
		assertEquals(10.9, value);
	}

	@Test
	public void test_parse_comparison_CASEI() throws UnknownCRSException {
		String comp = "test=CASEI('VALUE')";
		Object visit = parseCql2Filter(comp, crs(), PROPS);

		assertTrue(visit instanceof PropertyIsEqualTo);
		PropertyIsEqualTo propertyIsEqualTo = (PropertyIsEqualTo) visit;
		assertTrue(propertyIsEqualTo.isMatchCase());

		Expression param1 = propertyIsEqualTo.getParameter1();
		assertTrue(param1 instanceof ValueReference);
		assertEquals("test", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals(NS_URL, ((ValueReference) param1).getAsQName().getNamespaceURI());

		Expression param2 = propertyIsEqualTo.getParameter2();
		assertTrue(param2 instanceof Literal);
		TypedObjectNode primitiveValue = ((Literal<?>) param2).getValue();
		assertTrue(primitiveValue instanceof PrimitiveValue);
		Object value = ((PrimitiveValue) primitiveValue).getValue();
		assertTrue(value instanceof String);
		assertEquals("VALUE", value);
	}

	@Test
	public void test_parse_LIKE() throws UnknownCRSException {
		String comp = "test LIKE 'V_L%'";
		Object visit = parseCql2Filter(comp, crs(), PROPS);

		assertTrue(visit instanceof PropertyIsLike);

		PropertyIsLike propertyIsLike = (PropertyIsLike) visit;
		assertEquals("_", propertyIsLike.getSingleChar());
		assertEquals("%", propertyIsLike.getWildCard());
		assertEquals("\\", propertyIsLike.getEscapeChar());
		assertFalse(propertyIsLike.isMatchCase());

		Expression param1 = propertyIsLike.getParams()[0];
		assertTrue(param1 instanceof ValueReference);
		assertEquals("test", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals(NS_URL, ((ValueReference) param1).getAsQName().getNamespaceURI());

		Expression param2 = propertyIsLike.getParams()[1];
		assertTrue(param2 instanceof Literal);
		TypedObjectNode primitiveValue = ((Literal<?>) param2).getValue();
		assertTrue(primitiveValue instanceof PrimitiveValue);
		Object value = ((PrimitiveValue) primitiveValue).getValue();
		assertTrue(value instanceof String);
		assertEquals("V_L%", value);
	}

	@Test
	public void test_parse_LIKE_CASEI() throws UnknownCRSException {
		String comp = "test LIKE CASEI('V_L%')";
		Object visit = parseCql2Filter(comp, crs(), PROPS);

		assertTrue(visit instanceof PropertyIsLike);

		PropertyIsLike propertyIsLike = (PropertyIsLike) visit;
		assertEquals("_", propertyIsLike.getSingleChar());
		assertEquals("%", propertyIsLike.getWildCard());
		assertEquals("\\", propertyIsLike.getEscapeChar());
		assertTrue(propertyIsLike.isMatchCase());

		Expression param1 = propertyIsLike.getParams()[0];
		assertTrue(param1 instanceof ValueReference);
		assertEquals("test", ((ValueReference) param1).getAsQName().getLocalPart());
		assertEquals(NS_URL, ((ValueReference) param1).getAsQName().getNamespaceURI());

		Expression param2 = propertyIsLike.getParams()[1];
		assertTrue(param2 instanceof Literal);
		TypedObjectNode primitiveValue = ((Literal<?>) param2).getValue();
		assertTrue(primitiveValue instanceof PrimitiveValue);
		Object value = ((PrimitiveValue) primitiveValue).getValue();
		assertTrue(value instanceof String);
		assertEquals("V_L%", value);
	}

	@Test
	public void test_parse_AND() throws UnknownCRSException {
		String comp = "test1='VALUE1' AND test2 LIKE 'V_L%'";
		Object visit = parseCql2Filter(comp, crs(), PROPS);

		assertTrue(visit instanceof And);
		assertEquals(2, ((And) visit).getSize());

		Operator and1 = ((And) visit).getParameter(0);
		Operator and2 = ((And) visit).getParameter(1);

		assertTrue(and1 instanceof PropertyIsEqualTo);

		Expression param1_1 = ((PropertyIsEqualTo) and1).getParameter1();
		assertTrue(param1_1 instanceof ValueReference);
		assertEquals("test1", ((ValueReference) param1_1).getAsQName().getLocalPart());
		assertEquals(NS_URL, ((ValueReference) param1_1).getAsQName().getNamespaceURI());

		Expression param1_2 = ((PropertyIsEqualTo) and1).getParameter2();
		assertTrue(param1_2 instanceof Literal);
		TypedObjectNode primitiveValue1 = ((Literal<?>) param1_2).getValue();
		assertTrue(primitiveValue1 instanceof PrimitiveValue);
		Object value1 = ((PrimitiveValue) primitiveValue1).getValue();
		assertTrue(value1 instanceof String);
		assertEquals("VALUE1", value1);

		assertTrue(and2 instanceof PropertyIsLike);
		Expression param2_1 = ((PropertyIsLike) and2).getParams()[0];
		assertTrue(param2_1 instanceof ValueReference);
		assertEquals("test2", ((ValueReference) param2_1).getAsQName().getLocalPart());
		assertEquals(NS_URL, ((ValueReference) param2_1).getAsQName().getNamespaceURI());

		Expression param2_2 = ((PropertyIsLike) and2).getParams()[1];
		assertTrue(param2_2 instanceof Literal);
		TypedObjectNode primitiveValue = ((Literal<?>) param2_2).getValue();
		assertTrue(primitiveValue instanceof PrimitiveValue);
		Object value = ((PrimitiveValue) primitiveValue).getValue();
		assertTrue(value instanceof String);
		assertEquals("V_L%", value);
	}

	@Test(expected = ParseCancellationException.class)
	public void test_parse_inList_noValues() throws UnknownCRSException {
		String comp = "test1 IN()";
		parseCql2Filter(comp, crs(), PROPS);
	}

	@Test
	public void test_parse_inList_oneValue() throws UnknownCRSException {
		String comp = "test1 IN('VALUE1')";
		Object visit = parseCql2Filter(comp, crs(), PROPS);

		assertTrue(visit instanceof PropertyIsEqualTo);

		Expression param1_1 = ((PropertyIsEqualTo) visit).getParameter1();
		assertTrue(param1_1 instanceof ValueReference);
		assertEquals("test1", ((ValueReference) param1_1).getAsQName().getLocalPart());
		assertEquals(NS_URL, ((ValueReference) param1_1).getAsQName().getNamespaceURI());

		Expression param1_2 = ((PropertyIsEqualTo) visit).getParameter2();
		assertTrue(param1_2 instanceof Literal);
		TypedObjectNode primitiveValue1 = ((Literal<?>) param1_2).getValue();
		assertTrue(primitiveValue1 instanceof PrimitiveValue);
		Object value1 = ((PrimitiveValue) primitiveValue1).getValue();
		assertTrue(value1 instanceof String);
		assertEquals("VALUE1", value1);
	}

	@Test
	public void test_parse_inList_twoValues() throws UnknownCRSException {
		String comp = "test1 IN('VALUE1','VALUE2')";
		Object visit = parseCql2Filter(comp, crs(), PROPS);

		assertTrue(visit instanceof Or);
		assertEquals(2, ((Or) visit).getSize());

		Operator or1 = ((Or) visit).getParameter(0);
		Operator or2 = ((Or) visit).getParameter(1);

		assertTrue(or1 instanceof PropertyIsEqualTo);

		Expression param1_1 = ((PropertyIsEqualTo) or1).getParameter1();
		assertTrue(param1_1 instanceof ValueReference);
		assertEquals("test1", ((ValueReference) param1_1).getAsQName().getLocalPart());
		assertEquals(NS_URL, ((ValueReference) param1_1).getAsQName().getNamespaceURI());

		Expression param1_2 = ((PropertyIsEqualTo) or1).getParameter2();
		assertTrue(param1_2 instanceof Literal);
		TypedObjectNode primitiveValue1 = ((Literal<?>) param1_2).getValue();
		assertTrue(primitiveValue1 instanceof PrimitiveValue);
		Object value1 = ((PrimitiveValue) primitiveValue1).getValue();
		assertTrue(value1 instanceof String);
		assertEquals("VALUE1", value1);

		assertTrue(or2 instanceof PropertyIsEqualTo);
		Expression param2_1 = ((PropertyIsEqualTo) or2).getParams()[0];
		assertTrue(param2_1 instanceof ValueReference);
		assertEquals("test1", ((ValueReference) param2_1).getAsQName().getLocalPart());
		assertEquals(NS_URL, ((ValueReference) param2_1).getAsQName().getNamespaceURI());

		Expression param2_2 = ((PropertyIsEqualTo) or2).getParams()[1];
		assertTrue(param2_2 instanceof Literal);
		TypedObjectNode primitiveValue = ((Literal<?>) param2_2).getValue();
		assertTrue(primitiveValue instanceof PrimitiveValue);
		Object value = ((PrimitiveValue) primitiveValue).getValue();
		assertTrue(value instanceof String);
		assertEquals("VALUE2", value);
	}

	@Test
	public void test_parse_notInList_oneValue() throws UnknownCRSException {
		String comp = "test1 NOT IN('VALUE1')";
		Object visit = parseCql2Filter(comp, crs(), PROPS);

		assertTrue(visit instanceof Not);
		assertEquals(1, ((Not) visit).getParams().length);
		Operator notParam = ((Not) visit).getParams()[0];

		assertTrue(notParam instanceof PropertyIsEqualTo);

		Expression param1_1 = ((PropertyIsEqualTo) notParam).getParameter1();
		assertTrue(param1_1 instanceof ValueReference);
		assertEquals("test1", ((ValueReference) param1_1).getAsQName().getLocalPart());
		assertEquals(NS_URL, ((ValueReference) param1_1).getAsQName().getNamespaceURI());

		Expression param1_2 = ((PropertyIsEqualTo) notParam).getParameter2();
		assertTrue(param1_2 instanceof Literal);
		TypedObjectNode primitiveValue1 = ((Literal<?>) param1_2).getValue();
		assertTrue(primitiveValue1 instanceof PrimitiveValue);
		Object value1 = ((PrimitiveValue) primitiveValue1).getValue();
		assertTrue(value1 instanceof String);
		assertEquals("VALUE1", value1);
	}

	@Test
	public void test_parse_notInList_twoValues() throws UnknownCRSException {
		String comp = "test1 NOT IN('VALUE1','VALUE2')";
		Object visit = parseCql2Filter(comp, crs(), PROPS);

		assertTrue(visit instanceof Not);
		assertEquals(1, ((Not) visit).getParams().length);
		Operator or = ((Not) visit).getParams()[0];

		assertTrue(or instanceof Or);
		assertEquals(2, ((Or) or).getSize());

		Operator or1 = ((Or) or).getParameter(0);
		Operator or2 = ((Or) or).getParameter(1);

		assertTrue(or1 instanceof PropertyIsEqualTo);

		Expression param1_1 = ((PropertyIsEqualTo) or1).getParameter1();
		assertTrue(param1_1 instanceof ValueReference);
		assertEquals("test1", ((ValueReference) param1_1).getAsQName().getLocalPart());
		assertEquals(NS_URL, ((ValueReference) param1_1).getAsQName().getNamespaceURI());

		Expression param1_2 = ((PropertyIsEqualTo) or1).getParameter2();
		assertTrue(param1_2 instanceof Literal);
		TypedObjectNode primitiveValue1 = ((Literal<?>) param1_2).getValue();
		assertTrue(primitiveValue1 instanceof PrimitiveValue);
		Object value1 = ((PrimitiveValue) primitiveValue1).getValue();
		assertTrue(value1 instanceof String);
		assertEquals("VALUE1", value1);

		assertTrue(or2 instanceof PropertyIsEqualTo);
		Expression param2_1 = ((PropertyIsEqualTo) or2).getParams()[0];
		assertTrue(param2_1 instanceof ValueReference);
		assertEquals("test1", ((ValueReference) param2_1).getAsQName().getLocalPart());
		assertEquals(NS_URL, ((ValueReference) param2_1).getAsQName().getNamespaceURI());

		Expression param2_2 = ((PropertyIsEqualTo) or2).getParams()[1];
		assertTrue(param2_2 instanceof Literal);
		TypedObjectNode primitiveValue = ((Literal<?>) param2_2).getValue();
		assertTrue(primitiveValue instanceof PrimitiveValue);
		Object value = ((PrimitiveValue) primitiveValue).getValue();
		assertTrue(value instanceof String);
		assertEquals("VALUE2", value);
	}

	private static ICRS crs() throws UnknownCRSException {
		return CRSManager.lookup("urn:ogc:def:crs:OGC:1.3:CRS84");
	}

}
