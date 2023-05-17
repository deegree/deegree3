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

package org.deegree.cs.persistence.deegree.d3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.IAxis;
import org.deegree.cs.components.IDatum;
import org.deegree.cs.components.IEllipsoid;
import org.deegree.cs.components.IGeodeticDatum;
import org.deegree.cs.components.PrimeMeridian;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.coordinatesystems.IGeographicCRS;
import org.deegree.cs.coordinatesystems.IProjectedCRS;
import org.deegree.cs.exceptions.CRSStoreException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.persistence.CRSStore;
import org.deegree.cs.projections.IProjection;
import org.deegree.cs.projections.cylindric.ITransverseMercator;
import org.deegree.cs.refs.components.DatumRef;
import org.deegree.cs.refs.projections.ProjectionRef;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.helmert.Helmert;
import org.junit.Test;

/**
 * {@link DeegreeCRSStoreProviderTest} test the loading of the default configuration as
 * well as the loading of a projected crs from the default configuration.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
public class DeegreeCRSStoreProviderTest {

	/**
	 * Tries to load the default configuration, when no workspace is set!
	 * @throws ResourceInitException
	 */
	@Test
	public void testLoadingDefaultConfiguration() throws ResourceInitException {
		Collection<CRSStore> stores = CRSManager.getAll();
		assertNotNull(stores);
		assertTrue(stores.size() == 1);
		for (CRSStore store : stores) {
			assertTrue(store instanceof DeegreeCRSStore);
		}
	}

	/**
	 * Tries to create a crs by id.
	 * @throws CRSStoreException
	 */
	@Test
	public void testCRSByID() throws CRSStoreException {
		CRSStore defaultStore = new CRSManager().create(CRSManager.class.getResource("default.xml"));
		assertNotNull(defaultStore);
		assertTrue(defaultStore instanceof DeegreeCRSStore);
		DeegreeCRSStore dStore = (DeegreeCRSStore) defaultStore;
		// try loading the gaus krueger zone 2. (transverse mercator)
		ICRS testCRS = dStore.getCRSByCode(new CRSCodeType("epsg:31466"));
		testCRS_31466(testCRS, dStore);
		testCRS = dStore.getCRSByCode(new CRSCodeType("SOME_DUMMY_CODE"));
		assertTrue(testCRS == null);
		// test mercator reading
		testCRS = dStore.getCRSByCode(new CRSCodeType("epsg:3395"));
		assertTrue(testCRS != null);

		// stereographic alternative
		testCRS = dStore.getCRSByCode(new CRSCodeType("epsg:2172"));
		assertTrue(testCRS != null);

		// lambertAzimuthal
		testCRS = dStore.getCRSByCode(new CRSCodeType("epsg:2163"));
		assertTrue(testCRS != null);

		// lambert conformal conic
		testCRS = dStore.getCRSByCode(new CRSCodeType("epsg:2851"));
		assertTrue(testCRS != null);
	}

	@Test
	public void testAreaOfUse() throws CRSStoreException {
		CRSStore defaultStore = new CRSManager().create(CRSManager.class.getResource("default.xml"));
		assertNotNull(defaultStore);
		assertTrue(defaultStore instanceof DeegreeCRSStore);
		DeegreeCRSStore dStore = (DeegreeCRSStore) defaultStore;

		ICRS testCRS = dStore.getCRSByCode(new CRSCodeType("epsg:25832"));
		assertTrue(testCRS != null);
		double[] areaOfUseBBox = testCRS.getAreaOfUseBBox();

		assertTrue(11.9539362759596 > areaOfUseBBox[0] && 11.9539362759596 < areaOfUseBBox[2]);
		assertTrue(52.3111416255697 > areaOfUseBBox[1] && 52.3111416255697 < areaOfUseBBox[3]);
	}

	private void testCRS_31466(ICRS testCRS, DeegreeCRSStore provider) {
		assertNotNull(testCRS);
		assertTrue(testCRS instanceof IProjectedCRS);
		IProjectedCRS realCRS = (IProjectedCRS) testCRS;
		assertNotNull(realCRS.getProjection());
		IProjection projection = realCRS.getProjection();

		assertTrue(projection instanceof ProjectionRef);
		Object referencedObject = ((ProjectionRef) projection).getReferencedObject();
		assertTrue(referencedObject instanceof ITransverseMercator);

		// do stuff with projection
		ITransverseMercator proj = (ITransverseMercator) referencedObject;
		assertEquals(0.0, proj.getProjectionLatitude(), 1.0E-9);
		assertEquals(Math.toRadians(6.0), proj.getProjectionLongitude(), 1.0E-9);
		assertEquals(1.0, proj.getScale(), 1.0E-9);
		assertEquals(2500000.0, proj.getFalseEasting(), 1.0E-9);
		assertEquals(0.0, proj.getFalseNorthing(), 1.0E-9);
		assertTrue(proj.getHemisphere());

		// test the datum.
		IGeodeticDatum datum = realCRS.getGeodeticDatum();
		assertNotNull(datum);
		assertEquals("6314", datum.getCode().getCode());
		assertEquals(PrimeMeridian.GREENWICH, datum.getPrimeMeridian());

		// test the ellips
		IEllipsoid ellips = datum.getEllipsoid();
		assertNotNull(ellips);
		assertEquals("7004", ellips.getCode().getCode());
		assertEquals(Unit.METRE, ellips.getUnits());
		assertEquals(6377397.155, ellips.getSemiMajorAxis(), 1.0E-9);
		assertEquals(299.1528128, ellips.getInverseFlattening(), 1.0E-9);

		// test towgs84 params
		Helmert toWGS = datum.getWGS84Conversion();
		if (toWGS == null) {
			Transformation trans = provider.getDirectTransformation(realCRS.getGeographicCRS(), GeographicCRS.WGS84);
			assertNotNull(trans);
			assertTrue(trans instanceof Helmert);
			toWGS = (Helmert) trans;
		}
		assertNotNull(toWGS);
		assertTrue(toWGS.hasValues());
		assertEquals("1777", toWGS.getCode().getCode());
		assertEquals(598.1, toWGS.dx, 1.0E-9);
		assertEquals(73.7, toWGS.dy, 1.0E-9);
		assertEquals(418.2, toWGS.dz, 1.0E-9);
		assertEquals(0.202, toWGS.ex, 1.0E-9);
		assertEquals(0.045, toWGS.ey, 1.0E-9);
		assertEquals(-2.455, toWGS.ez, 1.0E-9);
		assertEquals(6.7, toWGS.ppm, 1.0E-9);

		// test the geographic
		IGeographicCRS geographic = realCRS.getGeographicCRS();
		assertNotNull(geographic);
		assertEquals("4314", geographic.getCode().getCode());
		IAxis[] ax = geographic.getAxis();
		assertEquals(2, ax.length);
		assertEquals(Axis.AO_EAST, ax[0].getOrientation());
		assertEquals(Unit.DEGREE, ax[0].getUnits());
		assertEquals(Axis.AO_NORTH, ax[1].getOrientation());
		assertEquals(Unit.DEGREE, ax[1].getUnits());
	}

	/**
	 * Test a cache
	 * @throws CRSStoreException
	 */
	public void testCache() throws CRSStoreException {
		CRSStore defaultStore = CRSManager.get("default");
		assertNotNull(defaultStore);
		assertTrue(defaultStore instanceof DeegreeCRSStore);
		DeegreeCRSStore dStore = (DeegreeCRSStore) defaultStore;

		ICRS testCRS = dStore.getCRSByCode(new CRSCodeType("epsg:31466"));
		testCRS_31466(testCRS, dStore);

		testCRS = dStore.getCRSByCode(new CRSCodeType("epsg:31466"));
		testCRS_31466(testCRS, dStore);
	}

	@Test
	public void testReference() throws UnknownCRSException {
		ICRS crsRef = CRSManager.lookup("epsg:4002");
		assertNotNull(crsRef);
		assertTrue(crsRef instanceof GeographicCRS);
		IDatum datum = ((GeographicCRS) crsRef).getDatum();
		assertTrue(datum instanceof DatumRef);
		assertTrue(!((DatumRef) datum).isResolved());
		// resolve the reference
		datum.getAreaOfUse();
		assertTrue(((DatumRef) datum).isResolved());
	}

}
