/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.cs.persistence.deegree.d3;

import static org.deegree.cs.transformations.CRSDefines.DEGREE_EPSILON;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CoordinateTransformer;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.persistence.CRSStore;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class Epsg5650Test {

	@Test
	public void testEPSG5650_EastingHasPrefix_NorthingIsEqual() throws Exception {
		CRSStore defaultStore = new CRSManager().create(CRSManager.class.getResource("default.xml"));
		assertNotNull(defaultStore);
		assertTrue(defaultStore instanceof DeegreeCRSStore);
		DeegreeCRSStore dStore = (DeegreeCRSStore) defaultStore;

		ICRS epsg4326 = dStore.getCRSByCode(new CRSCodeType("epsg:4326"));
		ICRS epsg5650 = dStore.getCRSByCode(new CRSCodeType("epsg:5650"));
		ICRS epsg25833 = dStore.getCRSByCode(new CRSCodeType("epsg:25833"));

		List<Point3d> pointsToTransform = pointList(13.1396484375, 53.2177734375);

		CoordinateTransformer transformerToEpsg25833 = new CoordinateTransformer(epsg25833);
		Point3d resultIn25833 = transformerToEpsg25833.transform(epsg4326, pointsToTransform).get(0);

		CoordinateTransformer transformerToEpsg5650 = new CoordinateTransformer(epsg5650);
		Point3d resultIn5650 = transformerToEpsg5650.transform(epsg4326, pointsToTransform).get(0);

		assertThat(resultIn5650.y, isCloseTo(resultIn25833.y, DEGREE_EPSILON));
		assertThat(resultIn5650.x - 33000000, isCloseTo(resultIn25833.x, DEGREE_EPSILON));
	}

	private List<Point3d> pointList(double x, double y) {
		return Collections.singletonList(new Point3d(x, y, 0));
	}

	private Matcher<Double> isCloseTo(final double expected, final double epsilonD) {
		return new BaseMatcher<Double>() {

			@Override
			public boolean matches(Object item) {
				double actual = (Double) item;
				return Math.abs((actual - expected)) <= epsilonD;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Expected is the value ")
					.appendValue(expected)
					.appendText(" +/- ")
					.appendValue(epsilonD);
			}
		};
	}

}