/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.filesystem.layout;

import static java.util.Collections.singletonList;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.math.BigInteger;
import java.util.Collections;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.SimpleGeometryFactory;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrix;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Basic tests for {@link TileCacheDiskLayout}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class TileCacheDiskLayoutTest {

	private TileCacheDiskLayout layout;

	@Before
	public void setup() {
		SimpleGeometryFactory fac = new SimpleGeometryFactory();
		ICRS crs = CRSManager.getCRSRef("EPSG:31466");
		double[] min = new double[] { 0.0, 0.0 };
		double[] max = new double[] { 1000.0, 1000.0 };
		Envelope env = fac.createEnvelope(min, max, crs);
		SpatialMetadata spatialMetadata = new SpatialMetadata(env, singletonList(crs));
		TileMatrix md = new TileMatrix("00", spatialMetadata, BigInteger.valueOf(128), BigInteger.valueOf(128), 0.001,
				BigInteger.valueOf(1000000000), BigInteger.valueOf(1000000000));
		TileDataLevel mockedMatrix = Mockito.mock(TileDataLevel.class);
		when(mockedMatrix.getMetadata()).thenReturn(md);
		TileDataSet mockedMatrixSet = Mockito.mock(TileDataSet.class);
		when(mockedMatrixSet.getTileDataLevel("00")).thenReturn(mockedMatrix);
		when(mockedMatrixSet.getTileDataLevels()).thenReturn(Collections.singletonList(mockedMatrix));

		layout = new TileCacheDiskLayout(new File("default"), "png");
		layout.setTileMatrixSet(mockedMatrixSet);
	}

	@Test
	public void testResolveLargeIndexes() {
		File file = layout.resolve("00", 18782353, 786347862);
		String path = file.getPath();
		String replaced = path.replace(File.separatorChar, '/');
		assertEquals("default/00/018/782/353/213/652/137.png", replaced);
	}

	@Test
	public void testResolveTinyIndexes() {
		File file = layout.resolve("00", 31, 41);
		String path = file.getPath();
		String replaced = path.replace(File.separatorChar, '/');
		assertEquals("default/00/000/000/031/999/999/958.png", replaced);
	}

}
