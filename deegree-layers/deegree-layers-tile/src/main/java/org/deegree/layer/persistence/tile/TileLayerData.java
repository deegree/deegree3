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
package org.deegree.layer.persistence.tile;

import java.util.Iterator;

import org.deegree.feature.FeatureCollection;
import org.deegree.layer.LayerData;
import org.deegree.rendering.r2d.TileRenderer;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.tile.Tile;

/**
 * <code>TileLayerData</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */

public class TileLayerData implements LayerData {

	private final Iterator<Tile> tiles;

	public TileLayerData(Iterator<Tile> tiles) {
		this.tiles = tiles;
	}

	@Override
	public void render(RenderContext context) {
		TileRenderer renderer = context.getTileRenderer();
		while (tiles.hasNext()) {
			renderer.render(tiles.next());
		}
	}

	@Override
	public FeatureCollection info() {
		return null;
	}

}
