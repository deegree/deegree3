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
package org.deegree.tile.persistence;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.deegree.geometry.Envelope;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.workspace.ResourceMetadata;

/**
 * Generic implementation of {@link TileStore}.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class GenericTileStore implements TileStore {

	private final Map<String, TileDataSet> tileDataSets;

	private ResourceMetadata<TileStore> metadata;

	/**
	 * Creates a new {@link GenericTileStore} instance.
	 * @param tileDataSets the tile data sets to serve, must not be <code>null</code>
	 * @param metadata resource metadata, must not be <code>null</code>
	 */
	public GenericTileStore(Map<String, TileDataSet> tileDataSets, ResourceMetadata<TileStore> metadata) {
		this.tileDataSets = tileDataSets;
		this.metadata = metadata;
	}

	@Override
	public Collection<String> getTileDataSetIds() {
		return tileDataSets.keySet();
	}

	@Override
	public void init() {
		// nothing to init
	}

	@Override
	public void destroy() {
		// nothing to destroy
	}

	@Override
	public Iterator<Tile> getTiles(String id, Envelope envelope, double resolution) {
		return tileDataSets.get(id).getTiles(envelope, resolution);
	}

	@Override
	public TileDataSet getTileDataSet(String id) {
		return tileDataSets.get(id);
	}

	@Override
	public Tile getTile(String tmsId, String tileMatrix, int x, int y) {
		TileDataLevel tm = tileDataSets.get(tmsId).getTileDataLevel(tileMatrix);
		if (tm == null) {
			return null;
		}
		return tm.getTile(x, y);
	}

	@Override
	public TileStoreTransaction acquireTransaction(String id) {
		throw new UnsupportedOperationException("Transactions are not supported by this tile store.");
	}

	@Override
	public ResourceMetadata<TileStore> getMetadata() {
		return metadata;
	}

}
