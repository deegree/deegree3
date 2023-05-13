package org.deegree.geometry.standard.points;

import static org.junit.Assert.assertThat;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import org.locationtech.jts.geom.Envelope;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class PackedPointsTest {

	@Test
	public void testExpandEnvelope_Dimension2() {
		ICRS crs = CRSManager.getCRSRef("EPSG:4326");
		double[] coordinates = new double[] { 3, 3, 4, 4, 5, 5 };

		PackedPoints points = new PackedPoints(crs, coordinates, 2);
		Envelope envelope = new Envelope(0, 10, 0, 10);
		Envelope expandedEnvelope = points.expandEnvelope(envelope);

		assertThat(expandedEnvelope, CoreMatchers.<Envelope>is(envelope));
	}

	@Test
	public void testExpandEnvelope_Dimension3() {
		ICRS crs = CRSManager.getCRSRef("EPSG:4326");
		double[] coordinates = new double[] { 3, 3, 0, 4, 4, 0, 5, 5, 0 };

		PackedPoints points = new PackedPoints(crs, coordinates, 3);
		Envelope envelope = new Envelope(0, 10, 0, 10);
		Envelope expandedEnvelope = points.expandEnvelope(envelope);

		assertThat(expandedEnvelope, CoreMatchers.<Envelope>is(envelope));
	}

}
