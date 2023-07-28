/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.cs.transformations;

import static org.deegree.cs.transformations.CRSDefines.EPSILON_D;
import static org.deegree.cs.transformations.CRSDefines.EPSILON_M;

import javax.vecmath.Point3d;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * Test transformations from/to epsg:31466 <-> epsg:900913.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 *
 */
public class TransformationGoogleTest extends TransformationAccuracy {

	@Ignore
	@Test
	public void test31466To900913ForwardAndInverse() throws TransformationException, UnknownCRSException {
		ICRS srcCrs = CRSManager.lookup("epsg:31466");
		ICRS targetCrs = CRSManager.lookup("epsg:900913");

		Point3d source = new Point3d(2365253.9171053073, 5838184.758315763, Double.NaN);
		// p-gdal: 446156.299191267 6920307.29753714
		// a-gdal: 446156.299191267 6886312.61194982
		Point3d target = new Point3d(446156.27360980725, 6920316.401632084, Double.NaN);
		doForwardAndInverse(srcCrs, targetCrs, source, target, EPSILON_M, EPSILON_M);
	}

	@Test
	public void test31466To900913() throws TransformationException, UnknownCRSException {
		ICRS srcCrs = CRSManager.lookup("epsg:31466");
		ICRS targetCrs = CRSManager.lookup("epsg:900913");

		Point3d source = new Point3d(2365253.9171053073, 5838184.758315763, Double.NaN);
		// p-gdal: 446156.299191267 6920307.29753714
		// a-gdal: 446156.299191267 6886312.61194982
		Point3d target = new Point3d(446156.27360980725, 6920316.401632084, Double.NaN);
		doForward(srcCrs, targetCrs, source, target, EPSILON_M);
	}

	@Ignore
	@Test
	public void test900913To31466() throws TransformationException, UnknownCRSException {
		ICRS srcCrs = CRSManager.lookup("epsg:900913");
		ICRS targetCrs = CRSManager.lookup("epsg:31466");

		Point3d source = new Point3d(446156.27360980725, 6920316.401632084, Double.NaN);
		// p-gdal: 2365254.05418317 5838190.27790714
		// a-gdal: 2365824.88617144 5858806.32195126
		Point3d target = new Point3d(2365253.9171053073, 5838184.758315763, Double.NaN);
		doForward(srcCrs, targetCrs, source, target, EPSILON_M);
	}

	@Test
	public void test31466To4326() throws TransformationException, UnknownCRSException {
		ICRS srcCrs = CRSManager.lookup("epsg:31466");
		ICRS targetCrs = CRSManager.lookup("epsg:4326");

		Point3d source = new Point3d(2365253.9171053073, 5838184.758315763, Double.NaN);
		// gdal: 4.00789022669717 52.6597504782029
		// Point3d target = new Point3d( 4.00789022669717, 52.6597504782029, Double.NaN );
		Point3d target = new Point3d(4.00788999689501, 52.659800083701924, Double.NaN);

		doForwardAndInverse(srcCrs, targetCrs, source, target, EPSILON_D, EPSILON_M);
	}

	@Test
	public void test4326To900913() throws TransformationException, UnknownCRSException {
		ICRS srcCrs = CRSManager.lookup("epsg:4326");
		ICRS targetCrs = CRSManager.lookup("epsg:900913");

		Point3d source = new Point3d(4.00788999689501, 52.659800083701924, Double.NaN);
		Point3d target = new Point3d(446156.27360980725, 6920316.401632084, Double.NaN);

		doForwardAndInverse(srcCrs, targetCrs, source, target, EPSILON_D, EPSILON_M);
	}

	@Test
	public void test31466To4314() throws TransformationException, UnknownCRSException {
		ICRS srcCrs = CRSManager.lookup("epsg:31466");
		ICRS targetCrs = CRSManager.lookup("epsg:4314");

		Point3d source = new Point3d(2365253.9171053073, 5838184.758315763, Double.NaN);
		// gdal: 4.00817715936553 52.6612532424228
		Point3d target = new Point3d(4.00817715936553, 52.6612532424228, Double.NaN);
		doForwardAndInverse(srcCrs, targetCrs, source, target, EPSILON_D, EPSILON_M);
	}

	@Test
	public void test4314To4326() throws TransformationException, UnknownCRSException {
		ICRS srcCrs = CRSManager.lookup("epsg:4314");
		ICRS targetCrs = CRSManager.lookup("epsg:4326");

		Point3d source = new Point3d(4.00817715936553, 52.6612532424228, Double.NaN);
		// gdal: 4.00789022669718 52.659750478203
		Point3d target = new Point3d(4.0078899968950195, 52.659800083702, Double.NaN);
		doForwardAndInverse(srcCrs, targetCrs, source, target, EPSILON_D, EPSILON_D);
	}

	@Test
	public void test4326ToGoogleGeographic() throws TransformationException, UnknownCRSException {
		ICRS srcCrs = CRSManager.lookup("epsg:4326");
		ICRS targetCrs = CRSManager.lookup("google_maps_geographiccrs");

		Point3d source = new Point3d(4.0078899968950195, 52.659800083702, Double.NaN);
		Point3d target = new Point3d(4.0078899968950195, 52.659800083702, Double.NaN);
		doForwardAndInverse(srcCrs, targetCrs, source, target, EPSILON_D, EPSILON_D);
	}

	@Test
	public void testGoogleGeographicTo900913() throws TransformationException, UnknownCRSException {
		ICRS srcCrs = CRSManager.lookup("google_maps_geographiccrs");
		ICRS targetCrs = CRSManager.lookup("epsg:900913");

		Point3d source = new Point3d(4.0078899968950195, 52.659800083702, Double.NaN);
		Point3d target = new Point3d(446156.2736098083, 6920316.401632098, Double.NaN);
		doForwardAndInverse(srcCrs, targetCrs, source, target, EPSILON_M, EPSILON_D);
	}

}
