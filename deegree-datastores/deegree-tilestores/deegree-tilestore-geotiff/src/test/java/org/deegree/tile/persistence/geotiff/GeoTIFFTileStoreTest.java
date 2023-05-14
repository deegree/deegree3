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
package org.deegree.tile.persistence.geotiff;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.utils.Pair;
import org.deegree.geometry.Envelope;
import org.deegree.tile.Tile;
import org.deegree.tile.TileIOException;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.persistence.TileStore;
import org.junit.Test;

/**
 * <code>GeoTIFFTileStoreTest</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */

public class GeoTIFFTileStoreTest {

	@Test
	public void testGetTiles() throws ResourceInitException, IOException, TileIOException {
		// File file = new File( "/stick/merged.tif" );
		// assumeTrue( file.exists() );
		// TileStore ts = new GeoTIFFTileStore( Collections.singletonList( new Pair<File,
		// String>( file, null ) ) );
		// ts.init( null );
		// Envelope envelope = ts.getMetadata( "merged" ).getEnvelope();
		// TileDataSet set = ts.getTileDataSet( "merged" );
		// double res = 0;
		// for ( TileDataLevel tm : set.getTileDataLevels() ) {
		// res = Math.max( tm.getMetadata().getResolution(), res );
		// }
		//
		// Iterator<Tile> i = ts.getTiles( "merged", envelope, res );
		// int cnt = 0;
		// long t1 = System.currentTimeMillis();
		// while ( i.hasNext() ) {
		// ++cnt;
		// BufferedImage img = i.next().getAsImage();
		// assertNotNull( "A tile resulted in null image.", img );
		// }
		// double secs = ( System.currentTimeMillis() - t1 ) / 1000d;
		// System.out.println( "Took " + secs + " seconds to fetch " + cnt + " tile
		// images." );
	}

}
