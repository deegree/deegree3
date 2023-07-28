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

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deegree.geometry.Envelope;
import org.slf4j.Logger;

/**
 * Default implementation of {@link TileDataSet}.
 * <p>
 * Selects tile matrices based on tile matrix metadata. Can be used in conjunction with
 * any implementation of {@link TileMatrixSet}.
 * </p>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class DefaultTileDataSet implements TileDataSet {

	private static final Logger LOG = getLogger(DefaultTileDataSet.class);

	private final Map<String, TileDataLevel> levels;

	private final TileMatrixSet metadata;

	private final String format;

	/**
	 * Creates a new {@link DefaultTileDataSet} instance.
	 * @param levels data levels, must not be <code>null</code> (ordering of resolutions
	 * is irrelevant)
	 * @param tileMatrixSet corresponding matrix set metadata, must not be
	 * <code>null</code>
	 * @param format native image format, must not be <code>null</code>
	 */
	public DefaultTileDataSet(List<TileDataLevel> levels, TileMatrixSet tileMatrixSet, String format) {
		List<TileDataLevel> sortedLevels = sortLevelsByResolutionFinestFirst(levels);
		this.levels = new LinkedHashMap<String, TileDataLevel>();
		for (TileDataLevel m : sortedLevels) {
			this.levels.put(m.getMetadata().getIdentifier(), m);
		}
		this.metadata = tileMatrixSet;
		this.format = format;
	}

	private List<TileDataLevel> sortLevelsByResolutionFinestFirst(List<TileDataLevel> levels) {
		List<TileDataLevel> sortedLevels = new ArrayList<TileDataLevel>(levels);
		Collections.sort(sortedLevels, new Comparator<TileDataLevel>() {
			@Override
			public int compare(TileDataLevel level1, TileDataLevel level2) {
				Double res1 = level1.getMetadata().getResolution();
				Double res2 = level2.getMetadata().getResolution();
				return res1.compareTo(res2);
			}
		});
		return sortedLevels;
	}

	@Override
	public Iterator<Tile> getTiles(Envelope envelope, double resolution) {
		// select correct matrix
		Iterator<TileDataLevel> iter = levels.values().iterator();
		TileDataLevel matrix = iter.next();
		TileDataLevel next = matrix;
		while (next.getMetadata().getResolution() <= resolution && iter.hasNext()) {
			matrix = next;
			next = iter.next();
		}
		if (next.getMetadata().getResolution() <= resolution) {
			matrix = next;
		}

		final long[] idxs = Tiles.getTileIndexRange(matrix, envelope);

		if (idxs == null) {
			return Collections.<Tile>emptyList().iterator();
		}

		final TileDataLevel fmatrix = matrix;

		LOG.info("Selected tile matrix {}, resolution {}, from {}x{} to {}x{}.",
				new Object[] { matrix.getMetadata().getIdentifier(), matrix.getMetadata().getResolution(), idxs[0],
						idxs[1], idxs[2], idxs[3] });

		// fetch tiles lazily
		return new Iterator<Tile>() {
			long x = idxs[0], y = idxs[1];

			@Override
			public boolean hasNext() {
				return x <= idxs[2];
			}

			@Override
			public Tile next() {
				Tile t = fmatrix.getTile(x, y);
				if (y == idxs[3]) {
					y = idxs[1];
					++x;
				}
				else {
					++y;
				}
				return t;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public List<TileDataLevel> getTileDataLevels() {
		return new ArrayList<TileDataLevel>(levels.values());
	}

	@Override
	public TileMatrixSet getTileMatrixSet() {
		return metadata;
	}

	@Override
	public TileDataLevel getTileDataLevel(String identifier) {
		return levels.get(identifier);
	}

	@Override
	public String getNativeImageFormat() {
		return format;
	}

}
