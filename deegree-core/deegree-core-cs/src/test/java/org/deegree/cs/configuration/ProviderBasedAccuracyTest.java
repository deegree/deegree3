/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://CoordinateSystem.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.cs.configuration;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.CoordinateTransformer;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.Ellipsoid;
import org.deegree.cs.components.GeodeticDatum;
import org.deegree.cs.components.IAxis;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.CompoundCRS;
import org.deegree.cs.coordinatesystems.GeocentricCRS;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.coordinatesystems.ProjectedCRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.persistence.CRSStore;
import org.deegree.cs.transformations.TransformationFactory;
import org.deegree.cs.transformations.TransformationFactory.DSTransform;
import org.deegree.cs.transformations.helmert.Helmert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>TransformationTest</code> a junit test class for testing the accuracy of various
 * transformations.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
public class ProviderBasedAccuracyTest {

	private static Logger LOG = LoggerFactory.getLogger(ProviderBasedAccuracyTest.class);

	private final static double METER_EPSILON = 0.15;

	private final static double DEGREE_EPSILON = 0.0000015;

	private final static Point3d epsilon = new Point3d(METER_EPSILON, METER_EPSILON, 0.4);

	private final static Point3d epsilonDegree = new Point3d(DEGREE_EPSILON, DEGREE_EPSILON, 0.4);

	/**
	 * Used axis
	 */
	private final static Axis[] axis_geocentric = new Axis[] { new Axis(Unit.METRE, "X", Axis.AO_FRONT),
			new Axis(Unit.METRE, "Y", Axis.AO_EAST), new Axis(Unit.METRE, "Z", Axis.AO_NORTH) };

	private final static Axis heightAxis = new Axis(Unit.METRE, "z", Axis.AO_UP);

	/**
	 * Used ellipsoids
	 */
	private final static Ellipsoid ellipsoid_7004 = new Ellipsoid(6377397.155, Unit.METRE, 299.1528128,
			new CRSCodeType[] { new CRSCodeType("7004") });

	private final static Ellipsoid ellipsoid_7019 = new Ellipsoid(6378137.0, Unit.METRE, 298.257222101,
			new CRSCodeType[] { new CRSCodeType("7019") });

	/**
	 * Used to wgs
	 */

	private final static Helmert wgs_1188 = new Helmert(GeographicCRS.WGS84, GeographicCRS.WGS84,
			new CRSCodeType[] { new CRSCodeType("1188") });

	private final static Helmert wgs_1777 = new Helmert(598.1, 73.7, 418.2, 0.202, 0.045, -2.455, 6.7,
			GeographicCRS.WGS84, GeographicCRS.WGS84, new CRSCodeType[] { new CRSCodeType("1777") });

	/**
	 * Used datums
	 */

	private final static GeodeticDatum datum_6314 = new GeodeticDatum(ellipsoid_7004, wgs_1777,
			new CRSCodeType[] { new CRSCodeType("6314") });

	private final static GeodeticDatum datum_6171 = new GeodeticDatum(ellipsoid_7019, wgs_1188,
			new CRSCodeType[] { new CRSCodeType("6171") });

	/**
	 * Used geocentric crs's
	 */
	private final static GeocentricCRS geocentric_4964 = new GeocentricCRS(datum_6171, axis_geocentric,
			new CRSIdentifiable(new CRSCodeType[] { new CRSCodeType("4964") }));

	private final static GeocentricCRS geocentric_dummy = new GeocentricCRS(datum_6314, axis_geocentric,
			new CRSIdentifiable(new CRSCodeType[] { new CRSCodeType(

					"NO_REAL_GEOCENTRIC") }));

	/**
	 * Creates a {@link CoordinateTransformer} for the given coordinate system.
	 * @param targetCrs to which incoming coordinates will be transformed.
	 * @return the transformer which is able to transform coordinates to the given crs..
	 */
	private CoordinateTransformer getGeotransformer(ICRS targetCrs) {
		assertNotNull(targetCrs);
		return new CoordinateTransformer(targetCrs);
	}

	/**
	 * Creates an epsilon string with following layout axis.getName: origPoint -
	 * resultPoint = epsilon Unit.getName().
	 * @param sourceCoordinate on the given axis
	 * @param targetCoordinate on the given axis
	 * @param allowedEpsilon defined by test.
	 * @param axis of the coordinates
	 * @return a String representation.
	 */
	private String createEpsilonString(boolean failure, double sourceCoordinate, double targetCoordinate,
			double allowedEpsilon, IAxis axis) {
		double epsilon = sourceCoordinate - targetCoordinate;
		StringBuilder sb = new StringBuilder(400);
		sb.append(axis.getName()).append(" (result - orig = error [allowedError]): ");
		sb.append(sourceCoordinate).append(" - ").append(targetCoordinate);
		sb.append(" = ").append(epsilon).append(axis.getUnits());
		sb.append(" [").append(allowedEpsilon).append(axis.getUnits()).append("]");
		if (failure) {
			sb.append(" [FAILURE]");
		}
		return sb.toString();
	}

	/**
	 * Transforms the given coordinates in the sourceCRS to the given targetCRS and checks
	 * if they lie within the given epsilon range to the reference point. If successful
	 * the transformed will be logged.
	 * @param sourcePoint to transform
	 * @param targetPoint to which the result shall be checked.
	 * @param epsilons for each axis
	 * @param sourceCRS of the origPoint
	 * @param targetCRS of the targetPoint.
	 * @return the string containing the success string.
	 * @throws TransformationException
	 * @throws AssertionError if one of the axis of the transformed point do not lie
	 * within the given epsilon range.
	 */
	private String doAccuracyTest(Point3d sourcePoint, Point3d targetPoint, Point3d epsilons, ICRS sourceCRS,
			ICRS targetCRS) throws TransformationException {
		assertNotNull(sourceCRS);
		assertNotNull(targetCRS);
		assertNotNull(sourcePoint);
		assertNotNull(targetPoint);
		assertNotNull(epsilons);

		CoordinateTransformer transformer = getGeotransformer(targetCRS);

		List<Point3d> tmp = new ArrayList<Point3d>(1);
		tmp.add(new Point3d(sourcePoint));
		Point3d result = transformer.transform(sourceCRS, tmp).get(0);
		assertNotNull(result);
		boolean xFail = Math.abs(result.x - targetPoint.x) > epsilons.x;
		String xString = createEpsilonString(xFail, result.x, targetPoint.x, epsilons.x, targetCRS.getAxis()[0]);
		boolean yFail = Math.abs(result.y - targetPoint.y) > epsilons.y;
		String yString = createEpsilonString(yFail, result.y, targetPoint.y, epsilons.y, targetCRS.getAxis()[1]);

		// Z-Axis if available.
		boolean zFail = false;
		String zString = "";
		if (targetCRS.getDimension() == 3) {
			zFail = Math.abs(result.z - targetPoint.z) > epsilons.z;
			zString = createEpsilonString(zFail, result.z, targetPoint.z, epsilons.z, targetCRS.getAxis()[2]);
		}
		StringBuilder sb = new StringBuilder();
		if (xFail || yFail || zFail) {
			sb.append("[FAILED] ");
		}
		else {
			sb.append("[SUCCESS] ");
		}
		sb.append("Transformation (").append(sourceCRS.getCode().toString());
		sb.append(" -> ").append(targetCRS.getCode().toString()).append(")\n");
		sb.append(xString);
		sb.append("\n").append(yString);
		if (targetCRS.getDimension() == 3) {
			sb.append("\n").append(zString);
		}
		if (xFail || yFail || zFail) {
			throw new AssertionError(sb.toString());
		}
		return sb.toString();
	}

	/**
	 * Do an forward and inverse accuracy test.
	 * @param sourceCRS
	 * @param targetCRS
	 * @param source
	 * @param target
	 * @param forwardEpsilon
	 * @param inverseEpsilon
	 * @throws TransformationException
	 */
	private void doForwardAndInverse(ICRS sourceCRS, ICRS targetCRS, Point3d source, Point3d target,
			Point3d forwardEpsilon, Point3d inverseEpsilon) throws TransformationException {
		StringBuilder output = new StringBuilder();
		output.append("Transforming forward/inverse -> crs with id: '");
		output.append(sourceCRS.getCode().toString());
		output.append("' and crs with id: '");
		output.append(targetCRS.getCode().toString());
		output.append("'.\n");

		// forward transform.
		boolean forwardSuccess = true;
		try {
			output.append("Forward transformation: ");
			output.append(doAccuracyTest(source, target, forwardEpsilon, sourceCRS, targetCRS));
		}
		catch (AssertionError ae) {
			output.append(ae.getLocalizedMessage());
			forwardSuccess = false;
		}

		// inverse transform.
		boolean inverseSuccess = true;
		try {
			output.append("\nInverse transformation: ");
			output.append(doAccuracyTest(target, source, inverseEpsilon, targetCRS, sourceCRS));
		}
		catch (AssertionError ae) {
			output.append(ae.getLocalizedMessage());
			inverseSuccess = false;
		}
		if (!forwardSuccess || !inverseSuccess) {
			LOG.info(output.toString());
		}

		assertEquals(true, forwardSuccess);
		assertEquals(true, inverseSuccess);

	}

	private CRSStore getProvider() {
		Collection<CRSStore> stores = CRSManager.getAll();
		assertNotNull(stores);
		assertTrue(stores.size() > 0);
		for (CRSStore store : stores) {
			return store;
		}
		return null;
	}

	private ICRS getCRS(String id) {
		CRSStore provider = getProvider();
		ICRS crs = provider.getCRSByCode(new CRSCodeType(id));
		assertNotNull(crs);
		assertTrue(crs.hasId(id, false, true));
		return crs;
	}

	/**
	 * Test the forward/inverse transformation from a compound_projected crs (EPSG:28992)
	 * to another compound_projected crs (EPSG:25832)
	 * @throws TransformationException
	 */
	@Test
	public void testCompoundToCompound() throws TransformationException {
		ICRS crs = getCRS("epsg:28992");
		assertTrue(crs instanceof ProjectedCRS);
		ProjectedCRS p_28992 = (ProjectedCRS) crs;
		// Source crs espg:28992
		CompoundCRS sourceCRS = new CompoundCRS(heightAxis, p_28992, 20,
				new CRSIdentifiable(new CRSCodeType[] { new CRSCodeType("epsg:28992_compound") }));
		crs = getCRS("epsg:25832");
		assertTrue(crs instanceof ProjectedCRS);
		ProjectedCRS p_25832 = (ProjectedCRS) crs;

		// Target crs espg:25832
		CompoundCRS targetCRS = new CompoundCRS(heightAxis, p_25832, 20,
				new CRSIdentifiable(new CRSCodeType[] { new CRSCodeType("epsg:25832_compound") }));

		// reference created with coord tool from http://CoordinateSystem.rdnap.nl/
		// (NL/Amsterdam/dam)
		Point3d sourcePoint = new Point3d(121397.572, 487325.817, 6.029);
		Point3d targetPoint = new Point3d(220513.823, 5810438.891, 49);
		doForwardAndInverse(sourceCRS, targetCRS, sourcePoint, targetPoint, epsilon, epsilon);
	}

	/**
	 * Test the transformation from a compound_projected crs (EPSG:28992_compound) to a
	 * geographic crs (EPSG:4258) coordinate system .
	 * @throws TransformationException
	 */
	@Test
	public void testCompoundToGeographic() throws TransformationException {
		ICRS crs = getCRS("epsg:28992");
		assertTrue(crs instanceof ProjectedCRS);
		ProjectedCRS p_28992 = (ProjectedCRS) crs;
		// Source crs espg:28992
		CompoundCRS sourceCRS = new CompoundCRS(heightAxis, p_28992, 20,
				new CRSIdentifiable(new CRSCodeType[] { new CRSCodeType("epsg:28992_compound") }));

		crs = getCRS("epsg:4258");
		assertTrue(crs instanceof GeographicCRS);
		// Target crs espg:4258
		GeographicCRS targetCRS = (GeographicCRS) crs;

		// reference created with coord tool from http://CoordinateSystem.rdnap.nl/
		// denoting (NL/Groningen/lichtboei)
		Point3d sourcePoint = new Point3d(236694.856, 583952.500, 1.307);
		Point3d targetPoint = new Point3d(6.610765, 53.235916, 42);

		doForwardAndInverse(sourceCRS, targetCRS, sourcePoint, targetPoint, epsilonDegree,
				new Point3d(METER_EPSILON, 0.17, 0.6));
	}

	/**
	 * Test the forward/inverse transformation from a compound_projected crs (EPSG:31467)
	 * to a geocentric crs (EPSG:4964)
	 * @throws TransformationException
	 */
	@Test
	public void testCompoundToGeocentric() throws TransformationException {

		ICRS crs = getCRS("epsg:31467");
		assertTrue(crs instanceof ProjectedCRS);
		ProjectedCRS p_31467 = (ProjectedCRS) crs;
		// source crs epsg:31467
		CompoundCRS sourceCRS = new CompoundCRS(heightAxis, p_31467, 20,
				new CRSIdentifiable(new CRSCodeType[] { new CRSCodeType("epsg:31467_compound") }));

		// Target crs EPSG:4964, is not in the database
		GeocentricCRS targetCRS = geocentric_4964;

		// do the testing
		Point3d sourcePoint = new Point3d(3532465.57, 5301523.49, 817);
		Point3d targetPoint = new Point3d(4230602.192492622, 702858.4858986374, 4706428.360722791);

		doForwardAndInverse(sourceCRS, targetCRS, sourcePoint, targetPoint, epsilon, epsilon);
	}

	/**
	 * Test the forward/inverse transformation from a compound_geographic crs (EPSG:4326)
	 * to a projected crs (EPSG:31467)
	 * @throws TransformationException
	 */
	@Test
	public void testCompoundToProjected() throws TransformationException {

		// Source WGS:84_compound
		CompoundCRS sourceCRS = new CompoundCRS(heightAxis, GeographicCRS.WGS84, 20, new CRSIdentifiable(
				new CRSCodeType[] { new CRSCodeType(GeographicCRS.WGS84.getCode().getOriginal() + "_compound") }));

		ICRS crs = getCRS("epsg:31467");
		assertTrue(crs instanceof ProjectedCRS);
		// Target EPSG:31467
		ProjectedCRS targetCRS = (ProjectedCRS) crs;

		// kind regards to vodafone for supplying reference points.
		Point3d sourcePoint = new Point3d(9.432778, 47.851111, 870.6);
		Point3d targetPoint = new Point3d(3532465.57, 5301523.49, 817);

		doForwardAndInverse(sourceCRS, targetCRS, sourcePoint, targetPoint, epsilon, epsilonDegree);
	}

	/**
	 * Test the forward/inverse transformation from a projected crs (EPSG:28992) to
	 * another projected crs (EPSG:25832)
	 * @throws TransformationException
	 */
	@Test
	public void testProjectedToProjected() throws TransformationException {
		ICRS crs = getCRS("epsg:28992");
		assertTrue(crs instanceof ProjectedCRS);
		// Source crs espg:28992
		ProjectedCRS sourceCRS = (ProjectedCRS) crs;

		crs = getCRS("epsg:25832");
		assertTrue(crs instanceof ProjectedCRS);
		// Target crs espg:25832
		ProjectedCRS targetCRS = (ProjectedCRS) crs;

		// reference created with coord tool from http://CoordinateSystem.rdnap.nl/
		// (NL/hoensbroek)
		Point3d sourcePoint = new Point3d(191968.31999475454, 326455.285005203, Double.NaN);
		Point3d targetPoint = new Point3d(283065.845, 5646206.125, Double.NaN);

		doForwardAndInverse(sourceCRS, targetCRS, sourcePoint, targetPoint, epsilon, epsilon);
	}

	/**
	 * Test the forward/inverse transformation from a projected crs (EPSG:31467) to a
	 * geographic crs (EPSG:4258)
	 * @throws TransformationException
	 */
	@Test
	public void testProjectedToGeographic() throws TransformationException {
		ICRS crs = getCRS("epsg:31467");
		assertTrue(crs instanceof ProjectedCRS);
		// Source crs espg:31467
		ProjectedCRS sourceCRS = (ProjectedCRS) crs;

		crs = getCRS("epsg:4258");
		assertTrue(crs instanceof GeographicCRS);
		// Target crs espg:4258
		GeographicCRS targetCRS = (GeographicCRS) crs;

		// with kind regards to vodafone for supplying reference points
		Point3d sourcePoint = new Point3d(3532465.57, 5301523.49, Double.NaN);
		Point3d targetPoint = new Point3d(9.432778, 47.851111, Double.NaN);

		doForwardAndInverse(sourceCRS, targetCRS, sourcePoint, targetPoint, epsilonDegree, epsilon);
	}

	/**
	 * Test the forward/inverse transformation from a projected crs (EPSG:28992) to a
	 * geocentric crs (EPSG:4964)
	 * @throws TransformationException
	 */
	@Test
	public void testProjectedToGeocentric() throws TransformationException {
		ICRS crs = getCRS("epsg:28992");
		assertTrue(crs instanceof ProjectedCRS);
		// Source crs espg:28992
		ProjectedCRS sourceCRS = (ProjectedCRS) crs;

		// Target crs EPSG:4964
		GeocentricCRS targetCRS = geocentric_4964;

		// do the testing created reference points with deegree (not a fine test!!)
		Point3d sourcePoint = new Point3d(191968.31999475454, 326455.285005203, Double.NaN);
		Point3d targetPoint = new Point3d(4006964.9993508584, 414997.8479008863, 4928439.8089122595);

		doForwardAndInverse(sourceCRS, targetCRS, sourcePoint, targetPoint, epsilon, epsilon);
	}

	/**
	 * Test the forward/inverse transformation from a geographic crs (EPSG:4314) to
	 * another geographic crs (EPSG:4258)
	 * @throws TransformationException
	 */
	@Test
	public void testGeographicToGeographic() throws TransformationException {
		ICRS crs = getCRS("epsg:4314");
		assertTrue(crs instanceof GeographicCRS);
		// source crs epsg:4314
		GeographicCRS sourceCRS = (GeographicCRS) crs;

		crs = getCRS("epsg:4258");
		assertTrue(crs instanceof GeographicCRS);
		// target crs epsg:4258
		GeographicCRS targetCRS = (GeographicCRS) crs;

		// with kind regards to vodafone for supplying reference points.
		Point3d sourcePoint = new Point3d(8.83319047, 54.90017335, Double.NaN);
		Point3d targetPoint = new Point3d(8.83213115, 54.89846442, Double.NaN);

		// do the testing
		doForwardAndInverse(sourceCRS, targetCRS, sourcePoint, targetPoint, epsilonDegree, epsilonDegree);
	}

	/**
	 * Test the forward/inverse transformation from a geographic crs (EPSG:4314) to
	 * another geographic crs (EPSG:4258)
	 * @throws TransformationException
	 */
	@Test
	public void testGeographicToGeographicNTv2() throws TransformationException {

		TransformationFactory fac = CRSManager.getTransformationFactory(null);
		fac.setPreferredTransformation(DSTransform.NTv2);

		ICRS crs = getCRS("epsg:4314");
		assertTrue(crs instanceof GeographicCRS);
		// source crs epsg:4314
		GeographicCRS sourceCRS = (GeographicCRS) crs;

		crs = getCRS("epsg:4258");
		assertTrue(crs instanceof GeographicCRS);
		// target crs epsg:4258
		GeographicCRS targetCRS = (GeographicCRS) crs;

		// coordinates from
		// http://crs.bkg.bund.de/crseu/crs/descrtrans/BeTA/BETA2007testdaten.csv
		Point3d sourcePoint = new Point3d(8.5, 54.716666666667, Double.NaN);
		Point3d targetPoint = new Point3d(8.499027339833, 54.714992333813, Double.NaN);

		// do the testing
		doForwardAndInverse(sourceCRS, targetCRS, sourcePoint, targetPoint, epsilonDegree, epsilonDegree);
		fac.setPreferredTransformation(DSTransform.HELMERT);
	}

	/**
	 * Test the forward/inverse transformation from a geographic crs (EPSG:4314) to a
	 * geocentric crs (EPSG:4964)
	 * @throws TransformationException
	 * @throws TransformationException
	 */
	@Test
	public void testGeographicToGeocentric() throws TransformationException {
		ICRS crs = getCRS("epsg:4314");
		assertTrue(crs instanceof GeographicCRS);
		// source crs epsg:4314
		GeographicCRS sourceCRS = (GeographicCRS) crs;

		// target crs epsg:4964
		GeocentricCRS targetCRS = geocentric_4964;

		// created with deegree not a fine reference
		Point3d sourcePoint = new Point3d(8.83319047, 54.90017335, Double.NaN);
		Point3d targetPoint = new Point3d(3632280.522352362, 564392.6943947134, 5194921.3092999635);
		// do the testing
		doForwardAndInverse(sourceCRS, targetCRS, sourcePoint, targetPoint, epsilon, epsilonDegree);
	}

	/**
	 * Test the forward/inverse transformation from a geocentric (dummy based on bessel)
	 * to another geocentric crs (EPSG:4964 based on etrs89)
	 * @throws TransformationException
	 */
	@Test
	public void testGeocentricToGeocentric() throws TransformationException {
		// source crs is a dummy based on the epsg:4314 == bessel datum.
		GeocentricCRS sourceCRS = geocentric_dummy;

		// target crs epsg:4964 etrs89 based
		GeocentricCRS targetCRS = geocentric_4964;

		// created with deegree not a fine reference
		Point3d sourcePoint = new Point3d(3631650.239831989, 564363.5250884632, 5194468.545970947);
		Point3d targetPoint = new Point3d(3632280.522352362, 564392.6943947134, 5194921.3092999635);

		// do the testing
		doForwardAndInverse(sourceCRS, targetCRS, sourcePoint, targetPoint, epsilon, epsilonDegree);
	}

}
