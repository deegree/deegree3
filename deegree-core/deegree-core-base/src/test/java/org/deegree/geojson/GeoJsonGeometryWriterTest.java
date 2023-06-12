package org.deegree.geojson;

import com.google.gson.stream.JsonWriter;
import com.jayway.jsonpath.matchers.JsonPathMatchers;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.segments.Arc;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GeoJsonGeometryWriterTest {

	private final GeometryFactory geometryFactory = new GeometryFactory();

	private static ICRS CRS_4326;

	private static ICRS CRS_25832;

	@BeforeClass
	public static void initCrs() throws UnknownCRSException {
		CRS_4326 = CRSManager.lookup("crs:84");
		CRS_25832 = CRSManager.lookup("EPSG:25832");
	}

	@Test
	public void testWriteGeometry_Point() throws Exception {
		Point point = geometryFactory.createPoint("pointId", 7.14, 50.68, CRS_4326);

		StringWriter json = new StringWriter();
		JsonWriter jsonWriter = instantiateJsonWriter(json);
		GeoJsonGeometryWriter geoJsonGeometryWriter = new GeoJsonGeometryWriter(jsonWriter, CRS_4326);

		geoJsonGeometryWriter.writeGeometry(point);

		String geometry = json.toString();
		assertThat(geometry, JsonPathMatchers.isJson());
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.type", is("Point")));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates.length()", is(2)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0]", is(point.get0())));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[1]", is(point.get1())));
	}

	@Test
	public void testWriteGeometry_PointInEPSG4326() throws Exception {
		ICRS epsg25832 = CRSManager.lookup("EPSG:25832");
		Point point = geometryFactory.createPoint("pointId", 368593.37, 5615891.00, epsg25832);

		StringWriter json = new StringWriter();
		JsonWriter jsonWriter = instantiateJsonWriter(json);
		GeoJsonGeometryWriter geoJsonGeometryWriter = new GeoJsonGeometryWriter(jsonWriter, CRS_4326);

		geoJsonGeometryWriter.writeGeometry(point);

		String geometry = json.toString();
		System.out.println(geometry);
		assertThat(geometry, JsonPathMatchers.isJson());
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.type", is("Point")));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates.length()", is(2)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0]", is(7.140000045737569)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[1]", is(50.67999998634426)));
	}

	@Test
	public void testWriteGeometry_Line() throws Exception {
		LineString lineString = createLineStringBonn();

		StringWriter json = new StringWriter();
		JsonWriter jsonWriter = instantiateJsonWriter(json);
		GeoJsonGeometryWriter geoJsonGeometryWriter = new GeoJsonGeometryWriter(jsonWriter, CRS_4326);

		geoJsonGeometryWriter.writeGeometry(lineString);

		String geometry = json.toString();
		assertThat(geometry, JsonPathMatchers.isJson());
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.type", is("LineString")));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates.length()", is(3)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].length()", is(2)));
		assertThat(geometry,
				JsonPathMatchers.hasJsonPath("$.coordinates[0].[0]", is(lineString.getStartPoint().get0())));
		assertThat(geometry,
				JsonPathMatchers.hasJsonPath("$.coordinates[0].[1]", is(lineString.getStartPoint().get1())));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[2].[0]", is(lineString.getEndPoint().get0())));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[2].[1]", is(lineString.getEndPoint().get1())));
	}

	@Test
	public void testWriteGeometry_Polygon() throws Exception {
		Polygon polygon = createPolygonBonn();

		StringWriter json = new StringWriter();
		JsonWriter jsonWriter = instantiateJsonWriter(json);
		GeoJsonGeometryWriter geoJsonGeometryWriter = new GeoJsonGeometryWriter(jsonWriter, CRS_4326);

		geoJsonGeometryWriter.writeGeometry(polygon);

		String geometry = json.toString();
		assertThat(geometry, JsonPathMatchers.isJson());
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.type", is("Polygon")));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates.length()", is(1)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].length()", is(4)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].[0].length()", is(2)));
		Points exteriorRing = polygon.getExteriorRingCoordinates();
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].[0].[0]", is(exteriorRing.get(0).get0())));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].[0].[1]", is(exteriorRing.get(0).get1())));
	}

	@Test
	public void testWriteGeometry_PolygonWithInteriorRing() throws Exception {
		Polygon polygon = createPolygonWithInteriorRing();

		StringWriter json = new StringWriter();
		JsonWriter jsonWriter = instantiateJsonWriter(json);
		GeoJsonGeometryWriter geoJsonGeometryWriter = new GeoJsonGeometryWriter(jsonWriter, CRS_4326);

		geoJsonGeometryWriter.writeGeometry(polygon);

		String geometry = json.toString();
		assertThat(geometry, JsonPathMatchers.isJson());
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.type", is("Polygon")));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates.length()", is(2)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].length()", is(4)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].[0].length()", is(2)));

		Points exteriorRing = polygon.getExteriorRingCoordinates();
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].[0].[0]", is(exteriorRing.get(0).get0())));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].[0].[1]", is(exteriorRing.get(0).get1())));

		Points interiorRing = polygon.getInteriorRingsCoordinates().get(0);
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[1].length()", is(4)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[1].[0].length()", is(2)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[1].[0].[0]", is(interiorRing.get(0).get0())));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[1].[0].[1]", is(interiorRing.get(0).get1())));

	}

	@Test
	public void testWriteGeometry_MultiPoints() throws Exception {
		Point point1 = geometryFactory.createPoint("pointId1", 7.14, 50.68, CRS_4326);
		Point point2 = geometryFactory.createPoint("pointId2", 7.24, 50.78, CRS_4326);

		MultiPoint multiPoint = geometryFactory.createMultiPoint("multiPointId", CRS_4326, asList(point1, point2));
		StringWriter json = new StringWriter();
		JsonWriter jsonWriter = instantiateJsonWriter(json);
		GeoJsonGeometryWriter geoJsonGeometryWriter = new GeoJsonGeometryWriter(jsonWriter, CRS_4326);

		geoJsonGeometryWriter.writeGeometry(multiPoint);

		String geometry = json.toString();
		assertThat(geometry, JsonPathMatchers.isJson());
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.type", is("MultiPoint")));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates.length()", is(2)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].length()", is(2)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].[0]", is(point1.get0())));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].[1]", is(point1.get1())));
	}

	@Test
	public void testWriteGeometry_MultiLineStrings() throws Exception {
		LineString lineString1 = createLineStringBonn();
		LineString lineString2 = createLineStringOldenburg();

		MultiLineString multiLineString = geometryFactory.createMultiLineString("multiPointId", CRS_4326,
				asList(lineString1, lineString2));
		StringWriter json = new StringWriter();
		JsonWriter jsonWriter = instantiateJsonWriter(json);
		GeoJsonGeometryWriter geoJsonGeometryWriter = new GeoJsonGeometryWriter(jsonWriter, CRS_4326);

		geoJsonGeometryWriter.writeGeometry(multiLineString);

		String geometry = json.toString();
		assertThat(geometry, JsonPathMatchers.isJson());
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.type", is("MultiLineString")));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates.length()", is(2)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].length()", is(3)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].[0].length()", is(2)));
		assertThat(geometry,
				JsonPathMatchers.hasJsonPath("$.coordinates[0].[0].[0]", is(lineString1.getStartPoint().get0())));
		assertThat(geometry,
				JsonPathMatchers.hasJsonPath("$.coordinates[0].[0].[1]", is(lineString1.getStartPoint().get1())));

		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[1].length()", is(4)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[1].[0]length()", is(2)));
		assertThat(geometry,
				JsonPathMatchers.hasJsonPath("$.coordinates[1].[0].[0]", is(lineString2.getStartPoint().get0())));
		assertThat(geometry,
				JsonPathMatchers.hasJsonPath("$.coordinates[1].[0].[1]", is(lineString2.getStartPoint().get1())));
	}

	@Test
	public void testWriteGeometry_MultiPolygons() throws Exception {
		Polygon polygon1 = createPolygonWithInteriorRing();
		Polygon polygon2 = createPolygonOldenburg();

		MultiPolygon multiPolygon = geometryFactory.createMultiPolygon("multiPointId", CRS_4326,
				asList(polygon1, polygon2));
		StringWriter json = new StringWriter();
		JsonWriter jsonWriter = instantiateJsonWriter(json);
		GeoJsonGeometryWriter geoJsonGeometryWriter = new GeoJsonGeometryWriter(jsonWriter, CRS_4326);

		geoJsonGeometryWriter.writeGeometry(multiPolygon);

		String geometry = json.toString();
		assertThat(geometry, JsonPathMatchers.isJson());
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.type", is("MultiPolygon")));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates.length()", is(2)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].length()", is(2)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].[0].length()", is(4)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].[0].[0].length()", is(2)));

		Points exteriorRing1 = polygon1.getExteriorRingCoordinates();
		assertThat(geometry,
				JsonPathMatchers.hasJsonPath("$.coordinates[0].[0].[0].[0]", is(exteriorRing1.get(0).get0())));
		assertThat(geometry,
				JsonPathMatchers.hasJsonPath("$.coordinates[0].[0].[0].[1]", is(exteriorRing1.get(0).get1())));

		Points interiorRing = polygon1.getInteriorRingsCoordinates().get(0);
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].[1].length()", is(4)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].[1].[0].length()", is(2)));
		assertThat(geometry,
				JsonPathMatchers.hasJsonPath("$.coordinates[0].[1].[0].[0]", is(interiorRing.get(0).get0())));
		assertThat(geometry,
				JsonPathMatchers.hasJsonPath("$.coordinates[0].[1].[0].[1]", is(interiorRing.get(0).get1())));

		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates.length()", is(2)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[1].[0].length()", is(5)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[1].[0].[0].length()", is(2)));

		Points exteriorRing2 = polygon2.getExteriorRingCoordinates();
		assertThat(geometry,
				JsonPathMatchers.hasJsonPath("$.coordinates[1].[0].[0].[0]", is(exteriorRing2.get(0).get0())));
		assertThat(geometry,
				JsonPathMatchers.hasJsonPath("$.coordinates[1].[0].[0].[1]", is(exteriorRing2.get(0).get1())));
	}

	@Test(expected = IOException.class)
	public void testWriteGeometry_UnsupportedGeometry() throws Exception {
		Point point1 = geometryFactory.createPoint("inPointId1", 7.2, 50.75, CRS_4326);
		Point point2 = geometryFactory.createPoint("inPointId2", 7.25, 50.8, CRS_4326);
		Point point3 = geometryFactory.createPoint("inPointId3", 7.25, 50.75, CRS_4326);

		Arc arc = geometryFactory.createArc(point1, point2, point3);
		Curve curve = geometryFactory.createCurve("curveId", CRS_4326, arc);

		StringWriter json = new StringWriter();
		JsonWriter jsonWriter = instantiateJsonWriter(json);
		GeoJsonGeometryWriter geoJsonGeometryWriter = new GeoJsonGeometryWriter(jsonWriter, CRS_4326);

		geoJsonGeometryWriter.writeGeometry(curve);
	}

	@Test
	public void testWriteGeometry_LineInEpsg25832() throws Exception {
		LineString lineString = createLineStringBonn();

		StringWriter json = new StringWriter();
		JsonWriter jsonWriter = instantiateJsonWriter(json);
		GeoJsonGeometryWriter geoJsonGeometryWriter = new GeoJsonGeometryWriter(jsonWriter, CRS_25832);

		geoJsonGeometryWriter.writeGeometry(lineString);

		GeometryTransformer geometryTransformer = new GeometryTransformer(CRS_25832);
		LineString lineStringIn25832 = geometryTransformer.transform(lineString);

		String geometry = json.toString();
		assertThat(geometry, JsonPathMatchers.isJson());
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.type", is("LineString")));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates.length()", is(3)));
		assertThat(geometry, JsonPathMatchers.hasJsonPath("$.coordinates[0].length()", is(2)));
		assertThat(geometry,
				JsonPathMatchers.hasJsonPath("$.coordinates[0].[0]", is(lineStringIn25832.getStartPoint().get0())));
		assertThat(geometry,
				JsonPathMatchers.hasJsonPath("$.coordinates[0].[1]", is(lineStringIn25832.getStartPoint().get1())));
		assertThat(geometry,
				JsonPathMatchers.hasJsonPath("$.coordinates[2].[0]", is(lineStringIn25832.getEndPoint().get0())));
		assertThat(geometry,
				JsonPathMatchers.hasJsonPath("$.coordinates[2].[1]", is(lineStringIn25832.getEndPoint().get1())));
	}

	private Polygon createPolygonWithInteriorRing() {
		Ring exteriorRing = geometryFactory.createLinearRing("ringId", CRS_4326, createPointsRingBonn());
		Point point1 = geometryFactory.createPoint("inPointId1", 7.2, 50.75, CRS_4326);
		Point point2 = geometryFactory.createPoint("inPointId2", 7.25, 50.8, CRS_4326);
		Point point3 = geometryFactory.createPoint("inPointId3", 7.25, 50.75, CRS_4326);

		Points interiorRingPoints = geometryFactory.createPoints(asList(point1, point2, point3, point1));
		Ring interiorRing = geometryFactory.createLinearRing("ringId", CRS_4326, interiorRingPoints);
		return geometryFactory.createPolygon("polygonId", CRS_4326, exteriorRing, asList(interiorRing));
	}

	private Polygon createPolygonBonn() {
		Points points = createPointsRingBonn();
		Ring exteriorRing = geometryFactory.createLinearRing("ringId", CRS_4326, points);
		return geometryFactory.createPolygon("polygonId", CRS_4326, exteriorRing, null);
	}

	private Polygon createPolygonOldenburg() {
		Points points = createPointsRingOldenburg();
		Ring exteriorRing = geometryFactory.createLinearRing("ringId", CRS_4326, points);
		return geometryFactory.createPolygon("polygonId", CRS_4326, exteriorRing, null);
	}

	private LineString createLineStringBonn() {
		Points points = geometryFactory.createPoints(createPointsBonn());
		return geometryFactory.createLineString("lineId", CRS_4326, points);
	}

	private LineString createLineStringOldenburg() {
		Points points = geometryFactory.createPoints(createPointsOldenburg());
		return geometryFactory.createLineString("lineId", CRS_4326, points);
	}

	private Points createPointsRingBonn() {
		List<Point> pointsBonn = createPointsBonn();
		List<Point> pointsRingBonn = new ArrayList<>(pointsBonn);
		pointsRingBonn.add(pointsBonn.get(0));
		return geometryFactory.createPoints(pointsRingBonn);
	}

	private Points createPointsRingOldenburg() {
		List<Point> pointsOldenburg = createPointsOldenburg();
		List<Point> pointsRingOldenburg = new ArrayList<>(pointsOldenburg);
		pointsRingOldenburg.add(pointsOldenburg.get(0));
		return geometryFactory.createPoints(pointsRingOldenburg);
	}

	private List<Point> createPointsBonn() {
		Point point1 = geometryFactory.createPoint("pointId1", 7.1, 50.7, CRS_4326);
		Point point2 = geometryFactory.createPoint("pointId2", 7.3, 50.7, CRS_4326);
		Point point3 = geometryFactory.createPoint("pointId3", 7.3, 50.9, CRS_4326);

		return asList(point1, point2, point3);
	}

	private List<Point> createPointsOldenburg() {
		Point point1 = geometryFactory.createPoint("pointId1", 8.19, 53.15, CRS_4326);
		Point point2 = geometryFactory.createPoint("pointId2", 8.20, 53.12, CRS_4326);
		Point point3 = geometryFactory.createPoint("pointId3", 8.27, 53.11, CRS_4326);
		Point point4 = geometryFactory.createPoint("pointId3", 8.25, 53.17, CRS_4326);

		return asList(point1, point2, point3, point4);
	}

	private JsonWriter instantiateJsonWriter(StringWriter json) throws IOException {
		JsonWriter jsonWriter = new JsonWriter(json);
		jsonWriter.setIndent("  ");
		return jsonWriter;
	}

}