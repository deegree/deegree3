package org.deegree.cs.transformations;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.junit.Test;

import javax.vecmath.Point3d;

/**
 * Test transformations to and from EPSG 3857.
 */
public class Epsg3857TransformationTest extends TransformationAccuracy {

	public final static Point3d EPSILON_WGS84 = new Point3d(0.0001, 0.0001, 0.1);

	public final static Point3d EPSILON_METER = new Point3d(10, 10, 0.1);

	private static final Point3d P2_3857 = new Point3d(1233190.824196, 6352029.317383, Double.NaN);

	private static final Point3d P2_31468 = new Point3d(4433251.725131215, 5479941.645054788, Double.NaN);

	private static final Point3d P2_4326 = new Point3d(11.0779417, 49.4527128, Double.NaN);

	private static final Point3d P2_4314 = new Point3d(11.0792762, 49.4537829, Double.NaN);

	@Test
	public void testFromEpsg3857to4326() throws UnknownCRSException, TransformationException {
		double xw = 1160534.71688755;
		double yw = 6058437.91872294;
		Point3d world = new Point3d(xw, yw, Double.NaN);
		ICRS worldCRS = CRSManager.lookup("epsg:3857");

		double epsg4326lon = 10.4252607393141;
		double epsg4326lat = 47.7081494644095;
		Point3d epsg4326Point = new Point3d(epsg4326lon, epsg4326lat, Double.NaN);
		ICRS epsg4326 = CRSManager.lookup("epsg:4326");

		doForward(worldCRS, epsg4326, world, epsg4326Point, EPSILON_WGS84);
	}

	@Test
	public void testFromEpsg3857to31468() throws UnknownCRSException, TransformationException {
		// https://epsg.io/transform#s_srs=3857&t_srs=31468&x=1233190.8241960&y=6352029.3173830
		ICRS sourceCRS = CRSManager.lookup("epsg:3857");
		ICRS targetCRS = CRSManager.lookup("epsg:31468");

		/*
		 * Original transformation chain:
		 *
		 * 1. inverse ProjectionTransform "mercator"; from ProjectedCRS epsg:3857 to
		 * GeographicCRS {uri=#google_maps_geographiccrs, resolved=true}
		 *
		 * 2. GeocentricTransform; from GeographicCRS {uri=#google_maps_geographiccrs,
		 * resolved=true} to GeocentricCRS epsg:4978 Geocentric WGS84
		 *
		 * 3. MatrixTransform
		 *
		 * 0.9999932999031813, -1.1902095911843137E-5, -2.1817635076876219E-7,
		 * -598.0950242462742 1.1902096339150821E-5, 0.9999932999022699,
		 * 9.793144776425789E-7, -73.70703439593228 2.1815303876065866E-7,
		 * -9.793196709117108E-7, 0.9999933000438833, -418.1972563798247 0.0, 0.0, 0.0,
		 * 1.0
		 *
		 * 4. inverse GeocentricTransform; from GeocentricCRS epsg:4314 to GeographicCRS
		 * {uri=#urn:opengis:def:crs:epsg::4314, resolved=true}
		 *
		 * 5. ProjectionTransform "transverseMercator"; from GeographicCRS
		 * {uri=#urn:opengis:def:crs:epsg::4314, resolved=true} to ProjectedCRS epsg:31468
		 *
		 * As a workaround for the wrong results now the transformation happens via WGS
		 * 84.
		 */
		doForward(sourceCRS, targetCRS, P2_3857, P2_31468, EPSILON_METER);
	}

	@Test
	public void testFromEpsg3857to4314() throws UnknownCRSException, TransformationException {
		// https://epsg.io/transform#s_srs=3857&t_srs=4314&x=1233190.8241960&y=6352029.3173830
		ICRS sourceCRS = CRSManager.lookup("epsg:3857");
		ICRS targetCRS = CRSManager.lookup("epsg:4314");

		doForward(sourceCRS, targetCRS, P2_3857, P2_4314, EPSILON_WGS84);
	}

	@Test
	public void testFromEpsg3857via4326to31468() throws UnknownCRSException, TransformationException {
		ICRS sourceCRS = CRSManager.lookup("epsg:3857");
		ICRS step1CRS = CRSManager.lookup("epsg:4326");
		ICRS targetCRS = CRSManager.lookup("epsg:31468");

		/*
		 * Transformation chain:
		 *
		 * 1. inverse ProjectionTransform "mercator"; from ProjectedCRS epsg:3857 to
		 * GeographicCRS {uri=#google_maps_geographiccrs, resolved=true}
		 *
		 * 2. MatrixTransform; epsg:4326 mentioned as source and target CRS (do these
		 * fields not have any meaning for the MatrixTransform?)
		 *
		 * 57.29577951308232, 0.0, 0.0 0.0, 57.29577951308232, 0.0 0.0, 0.0, 1.0
		 */
		doForward(sourceCRS, step1CRS, P2_3857, P2_4326, EPSILON_WGS84);

		/*
		 * Transformation chain:
		 *
		 * 1. MatrixTransform; epsg:4326
		 *
		 * 0.017453292519943295, 0.0, 0.0 0.0, 0.017453292519943295, 0.0 0.0, 0.0, 1.0
		 *
		 * 2. GeocentricTransform; from GeographicCRS epsg:4326 WGS 84 to GeocentricCRS
		 * epsg:4326 WGS 84_geocentric
		 *
		 * 3. inverse Helmert "DHDN to WGS 84"; from GeographicCRS epsg:4326 to
		 * GeographicCRS epsg:4314
		 *
		 * 4. inverse GeocentricTransform; from GeocentricCRS epsg:4314 to GeographicCRS
		 * {uri=#urn:opengis:def:crs:epsg::4314, resolved=true}
		 *
		 * 5. ProjectionTransform "transverseMercator"; from GeographicCRS
		 * {uri=#urn:opengis:def:crs:epsg::4314, resolved=true} to ProjectedCRS epsg:31468
		 */
		doForward(step1CRS, targetCRS, P2_4326, P2_31468, EPSILON_METER);
	}

	@Test
	public void testFromEpsg31468to3857() throws UnknownCRSException, TransformationException {
		// https://epsg.io/transform#s_srs=31468&t_srs=3857&x=4433251.7251312&y=5479941.6450548
		ICRS sourceCRS = CRSManager.lookup("epsg:31468");
		ICRS targetCRS = CRSManager.lookup("epsg:3857");

		doForward(sourceCRS, targetCRS, P2_31468, P2_3857, EPSILON_METER);
	}

}
