/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.tile;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Iterator;

import junit.framework.TestCase;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.junit.Before;
import org.junit.Test;

/**
 * <code>TileMatrixSetTest</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class TileMatrixSetTest extends TestCase {

	private TileDataSet tms;

	private Envelope env;

	@Override
	@Before
	public void setUp() {
		GeometryFactory fac = new GeometryFactory();
		ICRS crs;
		try {
			crs = CRSManager.lookup("EPSG:4326");
			env = fac.createEnvelope(-10, -10, 10, 10, crs);
			SpatialMetadata smd = new SpatialMetadata(env, Collections.singletonList(crs));
			TileMatrix md = new TileMatrix("someid", smd, 256, 256, 1, 1, 1);
			TileDataLevel tm = mock(TileDataLevel.class);
			Tile t = mock(Tile.class);

			tm.getMetadata();
			tm.getTile(0, 0);
			when(tm.getMetadata()).thenReturn(md);
			when(tm.getTile(0, 0)).thenReturn(t);

			TileMatrixSet metadata = new TileMatrixSet("default", null, Collections.singletonList(md), smd, null);
			tms = new DefaultTileDataSet(Collections.singletonList(tm), metadata, "image/png");
		}
		catch (UnknownCRSException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Test method for
	 * {@link org.deegree.tile.TileDataSet#getTiles(org.deegree.geometry.Envelope, double)}.
	 */
	@Test
	public void testGetTiles() {
		Iterator<Tile> iter = tms.getTiles(env, 1);
		assertNotNull(iter.next());
	}

	/**
	 * Test method for {@link org.deegree.tile.TileDataSet#getTileDataLevels()}.
	 */
	@Test
	public void testGetTileMatrices() {
		assertNotNull(tms.getTileDataLevels());
		assertEquals(tms.getTileDataLevels().size(), 1);
	}

}
