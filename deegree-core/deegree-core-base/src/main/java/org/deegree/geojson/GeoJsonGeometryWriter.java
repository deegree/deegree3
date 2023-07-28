package org.deegree.geojson;

import java.io.IOException;
import java.util.List;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Surface;

import com.google.gson.stream.JsonWriter;

/**
 * Stream-based writer for GeoJSON {@link Geometry}s.
 * <p>
 * Instances of this class are not thread-safe.
 * </p>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GeoJsonGeometryWriter {

	private final ICRS geoJsonCrs;

	private final JsonWriter jsonWriter;

	/**
	 * Instantiates a new {@link GeoJsonGeometryWriter}
	 * @param jsonWriter used to write the GeoJSON geometries, never <code>null</code>
	 * @param crs the target crs of the geometries, may be <code>null</code>, then
	 * "EPSG:4326" will be used
	 * @throws UnknownCRSException if "crs:84" is not known as CRS (should never happen)
	 */
	public GeoJsonGeometryWriter(JsonWriter jsonWriter, ICRS crs) throws UnknownCRSException {
		this.jsonWriter = jsonWriter;
		this.geoJsonCrs = ensureCrs(crs);
	}

	/**
	 * Writes the passed geometry as GeoJSON.
	 * @param geometry geometry to export, never <code>null</code>
	 * @throws IOException if GeoJSON could no be written
	 * @throws TransformationException if a geometry to export cannot be transformed to
	 * CRS:84
	 * @throws UnknownCRSException if the CRS of the geometry is not supported
	 */
	public void writeGeometry(Geometry geometry) throws IOException, TransformationException, UnknownCRSException {
		Geometry geometryToExport = transformGeometryIfRequired(geometry);
		exportGeometry(geometryToExport);
	}

	private Geometry transformGeometryIfRequired(Geometry geometry)
			throws UnknownCRSException, TransformationException {
		if (geoJsonCrs.equals(geometry.getCoordinateSystem())) {
			return geometry;
		}
		GeometryTransformer geometryTransformer = new GeometryTransformer(geoJsonCrs);
		return geometryTransformer.transform(geometry);
	}

	private void exportGeometry(Geometry geometryToExport) throws IOException {
		jsonWriter.beginObject();
		switch (geometryToExport.getGeometryType()) {
			case MULTI_GEOMETRY:
				exportMultiGeometry((MultiGeometry<? extends Geometry>) geometryToExport);
				break;
			case PRIMITIVE_GEOMETRY:
				GeometricPrimitive.PrimitiveType primitiveType = ((GeometricPrimitive) geometryToExport)
					.getPrimitiveType();
				switch (primitiveType) {
					case Curve:
						exportCurve((Curve) geometryToExport);
						break;
					case Point:
						exportPoint((Point) geometryToExport);
						break;
					case Surface:
						exportSurface((Surface) geometryToExport);
						break;
					default:
						throw new IOException("Could not export primitive geometry " + primitiveType + " as GeoJSON");
				}
				break;
			default:
				throw new IOException(
						"Could not export geometry " + geometryToExport.getGeometryType() + " as GeoJSON");
		}
		jsonWriter.endObject();
	}

	private void exportCurve(Curve curve) throws IOException {
		switch (curve.getCurveType()) {
			case LineString:
				exportLineString((LineString) curve);
				break;
			default:
				throw new IOException("Could not export curve " + curve.getCurveType() + " as GeoJSON");
		}
	}

	private void exportSurface(Surface surface) throws IOException {
		switch (surface.getSurfaceType()) {
			case Polygon:
				exportPolygon((Polygon) surface);
				break;
			default:
				throw new IOException("Could not export surface " + surface.getSurfaceType() + " as GeoJSON");
		}
	}

	private void exportMultiGeometry(MultiGeometry<? extends Geometry> multiGeometry) throws IOException {
		switch (multiGeometry.getMultiGeometryType()) {
			case MULTI_POINT:
				exportMultiPoint((MultiPoint) multiGeometry);
				break;
			case MULTI_LINE_STRING:
				exportMultiLineString((MultiLineString) multiGeometry);
				break;
			case MULTI_POLYGON:
				exportMultiPolygon((MultiPolygon) multiGeometry);
				break;
			case MULTI_SURFACE:
				exportMultiSurface((MultiSurface) multiGeometry);
				break;
			default:
				throw new IOException(
						"Could not export multi geometry " + multiGeometry.getMultiGeometryType() + " as GeoJSON");
		}
	}

	private void exportMultiPoint(MultiPoint multiPoint) throws IOException {
		jsonWriter.name("type").value("MultiPoint");
		jsonWriter.name("coordinates");
		jsonWriter.beginArray();
		for (Point point : multiPoint) {
			exportPointArray(point);
		}
		jsonWriter.endArray();
	}

	private void exportMultiLineString(MultiLineString multiLineString) throws IOException {
		jsonWriter.name("type").value("MultiLineString");
		jsonWriter.name("coordinates");
		jsonWriter.beginArray();
		for (LineString ls : multiLineString) {
			exportPoints(ls.getControlPoints());
		}
		jsonWriter.endArray();
	}

	private void exportMultiPolygon(MultiPolygon multiPolygon) throws IOException {
		jsonWriter.name("type").value("MultiPolygon");
		jsonWriter.name("coordinates");
		jsonWriter.beginArray();
		for (Polygon polygon : multiPolygon) {
			exportPolygonRings(polygon);
		}
		jsonWriter.endArray();
	}

	private void exportMultiSurface(MultiSurface<Surface> multiSurface) throws IOException {
		if (!containsOnlyPolygons(multiSurface))
			throw new IOException("Could not export multi surface with other geometries than polygons as GeoJSON");

		jsonWriter.name("type").value("MultiPolygon");
		jsonWriter.name("coordinates");
		jsonWriter.beginArray();
		for (Surface surface : multiSurface) {
			exportPolygonRings((Polygon) surface);
		}
		jsonWriter.endArray();

	}

	private void exportPoint(Point point) throws IOException {
		jsonWriter.name("type").value("Point");
		jsonWriter.name("coordinates");
		exportPointArray(point);
	}

	private void exportLineString(LineString lineString) throws IOException {
		jsonWriter.name("type").value("LineString");
		jsonWriter.name("coordinates");
		exportPoints(lineString.getControlPoints());
	}

	private void exportPolygon(Polygon polygon) throws IOException {
		jsonWriter.name("type").value("Polygon");
		jsonWriter.name("coordinates");
		exportPolygonRings(polygon);
	}

	private void exportPolygonRings(Polygon polygon) throws IOException {
		jsonWriter.beginArray();
		Ring exteriorRing = polygon.getExteriorRing();
		exportPoints(exteriorRing.getControlPoints());

		List<Ring> interiorRings = polygon.getInteriorRings();
		for (Ring interiorRing : interiorRings) {
			exportPoints(interiorRing.getControlPoints());
		}
		jsonWriter.endArray();
	}

	private void exportPoints(Points points) throws IOException {
		jsonWriter.beginArray();
		for (Point point : points) {
			exportPointArray(point);
		}
		jsonWriter.endArray();
	}

	private void exportPointArray(Point point) throws IOException {
		jsonWriter.beginArray();
		jsonWriter.value(point.get0());
		jsonWriter.value(point.get1());
		if (!Double.isNaN(point.get2()))
			jsonWriter.value(point.get2());
		jsonWriter.endArray();
	}

	private boolean containsOnlyPolygons(MultiSurface<Surface> multiSurface) {
		for (Surface surface : multiSurface) {
			if (!Surface.SurfaceType.Polygon.equals(surface.getSurfaceType()))
				return false;
		}
		return true;
	}

	private ICRS ensureCrs(ICRS crs) throws UnknownCRSException {
		if (crs != null)
			return crs;
		return CRSManager.lookup("crs:84");
	}

}
