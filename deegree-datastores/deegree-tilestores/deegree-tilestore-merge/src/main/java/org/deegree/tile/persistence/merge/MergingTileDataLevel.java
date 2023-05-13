/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.merge;

import java.util.ArrayList;
import java.util.List;

import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileMatrix;

/**
 * {@link TileDataLevel} implementation used by {@link MergingTileStore}.
 *
 * @author <a href="mailto:Reijer.Copier@idgis.nl">Reijer Copier</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
class MergingTileDataLevel implements TileDataLevel {

	private final TileMatrix tileMatrix;

	private final List<TileDataLevel> mergeLevels = new ArrayList<TileDataLevel>();

	MergingTileDataLevel(TileMatrix tileMatrix) {
		this.tileMatrix = tileMatrix;
	}

	void addMergeLevel(TileDataLevel mergeLevel) {
		mergeLevels.add(mergeLevel);
	}

	List<TileDataLevel> getMergeLevels() {
		return mergeLevels;
	}

	@Override
	public TileMatrix getMetadata() {
		return tileMatrix;
	}

	@Override
	public Tile getTile(long x, long y) {
		List<Tile> tiles = new ArrayList<Tile>(mergeLevels.size());
		for (TileDataLevel tileDataLevel : mergeLevels) {
			Tile tile = tileDataLevel.getTile(x, y);
			if (tile != null) {
				tiles.add(tile);
			}
		}
		if (tiles.isEmpty()) {
			return null;
		}
		if (tiles.size() == 1) {
			return tiles.get(0);
		}
		return new MergingTile(tiles);
	}

	@Override
	public List<String> getStyles() {
		return null;
	}

}
