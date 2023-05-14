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

import java.util.Iterator;
import java.util.List;

import org.deegree.geometry.Envelope;

/**
 * A collection of {@link TileDataLevel}s that adhere to the structure defined by a
 * {@link TileMatrixSet}.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public interface TileDataSet {

	/**
	 * Constructs an iterator of tiles for a resolution and envelope.
	 * @param envelope all tiles intersecting with this envelope will be returned, must
	 * not be <code>null</code>
	 * @param resolution selects the tile matrix, the smallest tile matrix with a
	 * sufficient resolution will be used
	 * @return an iterator of tiles, never <code>null</code>
	 */
	Iterator<Tile> getTiles(Envelope envelope, double resolution);

	/**
	 * Returns the tile matrices of this matrix set.
	 * @return the list of tile matrices this matrix set contains, never <code>null</code>
	 */
	List<TileDataLevel> getTileDataLevels();

	/**
	 * Returns the metadata about this matrix set.
	 * @return never null.
	 */
	TileMatrixSet getTileMatrixSet();

	/**
	 * Returns a single tile matrix identified by the identifier.
	 * @param identifier
	 * @return null, if no such matrix
	 */
	TileDataLevel getTileDataLevel(String identifier);

	/**
	 * Returns the mime type of the native image format.
	 * @return mime type, never <code>null</code>
	 */
	String getNativeImageFormat();

}
