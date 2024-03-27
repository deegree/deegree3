//$HeadURL$
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

package org.deegree.tile.persistence.gpkg;

import org.deegree.commons.utils.Pair;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileMatrix;

import java.util.List;
import java.util.Map;

/**
 * A GpkgTileDataLevel
 *
 * @author <a href="mailto:migliavacca@lat-lon.de">Diego Migliavacca</a>
 * @since 3.5
 */

public class GpkgTileDataLevel implements TileDataLevel {

	private final TileMatrix tm;

	private Map<Pair<Long, Long>, byte[]> tileMap;

	public GpkgTileDataLevel(TileMatrix tm, Map<Pair<Long, Long>, byte[]> tileMap) {
		this.tm = tm;
		this.tileMap = tileMap;
	}

	@Override
	public TileMatrix getMetadata() {
		return tm;
	}

	@Override
	public Tile getTile(long x, long y) {
		Pair<Long, Long> k = new Pair<Long, Long>();
		k.setFirst(x);
		k.setSecond(y);
		byte[] byteArr = tileMap.get(k);
		return new GpkgTile(tm, byteArr);
	}

	@Override
	public List<String> getStyles() {
		// TODO Auto-generated method stub
		return null;
	}

}
