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

package org.deegree.tile.persistence;

import org.deegree.geometry.Envelope;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileIOException;
import org.deegree.tile.Tiles;

/**
 * Provides common base functionality for implementations of {@link TileStoreTransaction}.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public abstract class AbstractTileStoreTransaction implements TileStoreTransaction {

	protected final TileStore store;

	protected final String tileMatrixSet;

	/**
	 * Creates a new {@link AbstractTileStoreTransaction} instance.
	 * @param store associated tile store, must not be <code>null</code>
	 * @param tileMatrixSet the id of the tile matrix set, must not be <code>null</code>
	 */
	protected AbstractTileStoreTransaction(TileStore store, String tileMatrixSet) {
		this.store = store;
		this.tileMatrixSet = tileMatrixSet;
	}

	@Override
	public void delete(String tileMatrixId, Envelope env) throws TileIOException {
		if (tileMatrixId == null) {
			for (TileDataLevel matrix : store.getTileDataSet(tileMatrixSet).getTileDataLevels()) {
				delete(matrix, env);
			}
		}
		else {
			TileDataLevel matrix = store.getTileDataSet(tileMatrixSet).getTileDataLevel(tileMatrixId);
			delete(matrix, env);
		}
	}

	private void delete(TileDataLevel matrix, Envelope env) throws TileIOException {
		long[] tileIndexRange = Tiles.getTileIndexRange(matrix, env);
		long minX = tileIndexRange[0];
		long minY = tileIndexRange[1];
		long maxX = tileIndexRange[2];
		long maxY = tileIndexRange[3];
		for (long x = minX; x <= maxX; x++) {
			for (long y = minY; y <= maxY; y++) {
				delete(matrix.getMetadata().getIdentifier(), x, y);
			}
		}
	}

}
