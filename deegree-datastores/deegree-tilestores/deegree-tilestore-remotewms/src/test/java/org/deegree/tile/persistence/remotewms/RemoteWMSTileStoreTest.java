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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.remotewms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.utils.MapUtils;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.persistence.GenericTileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Basic test cases for the {@link RemoteWMSTileStore}.
 * <p>
 * These tests only check the correct extraction of metadata and the generation of the
 * {@link TileDataSet}. Actual fetching of tile data is realized as integration tests
 * (module deegree-wmts-tests).
 * </p>
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class RemoteWMSTileStoreTest {

	private Workspace ws;

	@Before
	public void setup() throws UnknownCRSException, IOException, URISyntaxException, ResourceInitException {
		URL wsUrl = RemoteWMSTileStoreTest.class.getResource("workspace");
		ws = new DefaultWorkspace(new File(wsUrl.toURI()));
		ws.initAll();
	}

	@After
	public void tearDown() {
		ws.destroy();
	}

	@Test
	public void testGetMetdataEPSG26912() {
		GenericTileStore store = (GenericTileStore) ws.getResource(TileStoreProvider.class, "tiles26912");
		SpatialMetadata metadata = store.getTileDataSet("tiles26912").getTileMatrixSet().getSpatialMetadata();
		assertEquals(1, metadata.getCoordinateSystems().size());

		assertTrue(metadata.getCoordinateSystems().get(0).hasId("urn:opengis:def:crs:epsg::26912", false, true));
		assertEquals(228563.303, metadata.getEnvelope().getMin().get0(), 0.001);
		assertEquals(4103089.15, metadata.getEnvelope().getMin().get1(), 0.001);
		assertEquals(779065.703, metadata.getEnvelope().getMax().get0(), 0.001);
		assertEquals(4653591.55, metadata.getEnvelope().getMax().get1(), 0.001);
	}

	@Test
	public void testGetTileMatrixSetEPSG26912() {
		GenericTileStore store = (GenericTileStore) ws.getResource(TileStoreProvider.class, "tiles26912");
		TileDataSet dataSet = store.getTileDataSet("tiles26912");

		assertEquals(10, dataSet.getTileDataLevels().size());
		double scale = 1000.0;
		double resolution = MapUtils.DEFAULT_PIXEL_SIZE * scale;
		for (TileDataLevel matrix : dataSet.getTileDataLevels()) {
			TileMatrix md = matrix.getMetadata();
			assertEquals(resolution, md.getResolution(), 0.001);
			assertEquals(resolution * md.getTilePixelsX(), md.getTileWidth(), 0.001);
			assertEquals(resolution * md.getTilePixelsY(), md.getTileHeight(), 0.001);
			scale *= 2.0;
			resolution *= 2.0;
		}
	}

}
