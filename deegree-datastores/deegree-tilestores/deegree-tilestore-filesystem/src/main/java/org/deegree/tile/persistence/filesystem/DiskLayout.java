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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.filesystem;

import java.io.File;

import org.deegree.tile.TileDataSet;

/**
 * Implementations define how the {@link FileSystemTileStore} maps between the
 * {@link org.deegree.tile.TileDataLevel} instances in a {@link TileDataSet} and image
 * files on the file system.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public interface DiskLayout {

	/**
	 * Returns the image file for the specified {@link org.deegree.tile.TileDataLevel} and
	 * tile indexes.
	 * @param matrixId identifier of the matrix in the matrix set, must not be
	 * <code>null</code>
	 * @param x column index of the tile (starting at 0)
	 * @param y row index of the tile (starting at 0)
	 * @return tile file or <code>null</code> if the tile matrix does not exist (or
	 * indexes are out of range)
	 */
	File resolve(String matrixId, long x, long y);

	/**
	 * Returns the suffix of the tile files (without '.').
	 * @return suffix of the tile files, never <code>null</code>
	 */
	String getFileType();

	/**
	 * Assigns the given {@link TileDataSet}.
	 * @param set tile matrix to assign, must not be <code>null</code>
	 */
	void setTileMatrixSet(TileDataSet set);

}
