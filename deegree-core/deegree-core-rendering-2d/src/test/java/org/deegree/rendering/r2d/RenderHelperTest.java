/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - IDgis bv -

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

 IDgis bv
 Boomkamp 16, 7461 AX Rijssen
 The Netherlands
 http://www.idgis.nl/

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

package org.deegree.rendering.r2d;

import java.awt.geom.AffineTransform;

import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <code>RenderHelperTest</code>
 *
 * @author <a href="mailto:reijer.copier@idgis.nl">Reijer Copier</a>
 */
public class RenderHelperTest {

	private GeometryFactory geometryFactory = new GeometryFactory();

	@Test
	public void testWGS84ShortIdTransform() throws Exception {

		final double[] min = new double[] { 4.835947, 52.442229 };
		final double[] max = new double[] { 4.871100, 52.453817 };

		final AffineTransform worldToScreen = new AffineTransform();
		final Envelope bbox = geometryFactory.createEnvelope(min, max, CRSManager.lookup("EPSG:4326"));
		RenderHelper.getWorldToScreenTransform(worldToScreen, bbox, 42, 47);

		final double[] transformedPoint = new double[2];

		worldToScreen.transform(min, 0, transformedPoint, 0, 1);
		assertEquals(0, transformedPoint[0], 0.1);
		assertEquals(47, transformedPoint[1], 0.1);

		worldToScreen.transform(max, 0, transformedPoint, 0, 1);
		assertEquals(42, transformedPoint[0], 0.1);
		assertEquals(0, transformedPoint[1], 0.1);
	}

	@Test
	public void testWGS84LongIdTransform() throws Exception {

		final double[] min = new double[] { 52.442229, 4.835947 };
		final double[] max = new double[] { 52.453817, 4.871100 };

		final AffineTransform worldToScreen = new AffineTransform();
		final Envelope bbox = geometryFactory.createEnvelope(min, max, CRSManager.lookup("urn:ogc:def:crs:EPSG::4326"));
		RenderHelper.getWorldToScreenTransform(worldToScreen, bbox, 42, 47);

		final double[] transformedPoint = new double[2];

		worldToScreen.transform(min, 0, transformedPoint, 0, 1);
		assertEquals(0, transformedPoint[0], 0.1);
		assertEquals(47, transformedPoint[1], 0.1);

		worldToScreen.transform(max, 0, transformedPoint, 0, 1);
		assertEquals(42, transformedPoint[0], 0.1);
		assertEquals(0, transformedPoint[1], 0.1);
	}

}
