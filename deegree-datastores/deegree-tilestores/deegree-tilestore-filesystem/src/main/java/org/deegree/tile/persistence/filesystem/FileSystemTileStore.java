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
package org.deegree.tile.persistence.filesystem;

import java.util.Map;

import org.deegree.tile.TileDataSet;
import org.deegree.tile.persistence.GenericTileStore;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreTransaction;
import org.deegree.workspace.ResourceMetadata;

/**
 * Generic {@link org.deegree.tile.persistence.TileStore}.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class FileSystemTileStore extends GenericTileStore {

	/**
	 * Creates a new {@link FileSystemTileStore} instance.
	 * @param tileDataSets the tile data sets to serve, must not be <code>null</code>
	 * @param metadata resource metadata for this tile store, may not be <code>null</code>
	 * if managed by workspace
	 */
	public FileSystemTileStore(Map<String, TileDataSet> tileDataSets, ResourceMetadata<TileStore> metadata) {
		super(tileDataSets, metadata);
	}

	@Override
	public TileStoreTransaction acquireTransaction(String id) {
		return new FileSystemTileStoreTransaction(id, this);
	}

}
