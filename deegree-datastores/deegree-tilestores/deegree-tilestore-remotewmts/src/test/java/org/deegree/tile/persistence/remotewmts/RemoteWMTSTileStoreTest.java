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
package org.deegree.tile.persistence.remotewmts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.persistence.TileStore;
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
public class RemoteWMTSTileStoreTest {

	private Workspace ws;

	@Before
	public void setup() throws UnknownCRSException, IOException, URISyntaxException, ResourceInitException {
		URL wsUrl = RemoteWMTSTileStoreTest.class.getResource("workspace");
		ws = new DefaultWorkspace(new File(wsUrl.toURI()));
		ws.initAll();
	}

	@After
	public void tearDown() {
		ws.destroy();
	}

	@Test
	public void testTileDataSet() {
		TileStore store = ws.getResource(TileStoreProvider.class, "medford_buildings");
		assertNotNull(store);
		assertEquals(1, store.getTileDataSetIds().size());
		TileDataSet tileDataSet = store.getTileDataSet("medford:buildings");
		assertEquals("image/png", tileDataSet.getNativeImageFormat());
		assertEquals(19, tileDataSet.getTileDataLevels().size());
	}

}
