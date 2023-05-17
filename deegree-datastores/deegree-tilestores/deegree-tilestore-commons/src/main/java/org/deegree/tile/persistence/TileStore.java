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
package org.deegree.tile.persistence;

import java.util.Collection;
import java.util.Iterator;

import org.deegree.workspace.Resource;
import org.deegree.geometry.Envelope;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataSet;

/**
 * {@link Resource} that provides access to stored {@link TileDataSet}s and {@link Tile}s.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public interface TileStore extends Resource {

	/**
	 * Returns the specified {@link TileDataSet}.
	 * @param tileDataSet the id of the tile data set, must not be <code>null</code>
	 * @return the tile data set, or <code>null</code>, if no tile data set with the
	 * specified identifier exists
	 */
	TileDataSet getTileDataSet(String tileDataSet);

	/**
	 * Returns the identifiers of all {@link TileDataSet}s served by this tile store.
	 * @return the identifiers, can be empty, but never <code>null</code>
	 */
	Collection<String> getTileDataSetIds();

	/**
	 * Creates tile stream according to the parameters.
	 * @param tileDataSet the id of the tile data set, must not be <code>null</code>
	 * @param envelope the extent of tiles needed, must not be <code>null</code>
	 * @param resolution the desired minimum resolution of tiles, must be positive
	 * @return an iterator of tiles for the given envelope and resolution, never
	 * <code>null</code>
	 */
	Iterator<Tile> getTiles(String tileDataSet, Envelope envelope, double resolution);

	/**
	 * Queries a single tile from the specified tile data set.
	 * @param tileDataSet id of the tile data set, must not be <code>null</code>
	 * @param tileDataLevel id of the tile data level, must not be <code>null</code>
	 * @param x column index, starting at zero
	 * @param y row index, starting at zero
	 * @return the specified tile, or <code>null</code>, if no such tile exists
	 */
	Tile getTile(String tileDataSet, String tileDataLevel, int x, int y);

	/**
	 * Acquires transactional access to the tile store.
	 * @param tileDataSet the id of the tile data set to be modified, must not be
	 * <code>null</code>
	 * @return transaction object that allows to perform transactions operations on the
	 * store, never <code>null</code>
	 */
	TileStoreTransaction acquireTransaction(String tileDataSet);

}
