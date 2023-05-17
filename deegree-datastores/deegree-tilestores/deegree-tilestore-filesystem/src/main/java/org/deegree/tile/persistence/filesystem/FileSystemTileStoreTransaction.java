/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.deegree.tile.Tile;
import org.deegree.tile.TileIOException;
import org.deegree.tile.persistence.AbstractTileStoreTransaction;

/**
 * {@link org.deegree.tile.persistence.TileStoreTransaction} for the
 * {@link FileSystemTileStore}.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
class FileSystemTileStoreTransaction extends AbstractTileStoreTransaction {

	// private static final Logger LOG = getLogger( FileSystemTileStoreTransaction.class
	// );

	/**
	 * Creates a new {@link org.deegree.tile.persistence.TileStoreTransaction}.
	 * @param store tile store, must not be <code>null</code>
	 */
	FileSystemTileStoreTransaction(String id, FileSystemTileStore store) {
		super(store, id);
	}

	@Override
	public void put(String matrixId, Tile tile, long x, long y) throws TileIOException {
		DiskLayout layout = ((FileSystemTileDataLevel) this.store.getTileDataSet(this.tileMatrixSet)
			.getTileDataLevel(matrixId)).getLayout();
		FileOutputStream fos = null;
		try {
			File file = layout.resolve(matrixId, x, y);
			synchronized (store) {
				if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
					throw new TileIOException("Unable to create parent directories for " + file);
				}
			}
			fos = new FileOutputStream(file);
			ImageIO.write(tile.getAsImage(), layout.getFileType(), fos);
		}
		catch (IOException e) {
			throw new TileIOException("Error retrieving image: " + e.getMessage(), e);
		}
		finally {
			IOUtils.closeQuietly(fos);
		}
	}

	@Override
	public void delete(String matrixId, long x, long y) throws TileIOException {
		DiskLayout layout = ((FileSystemTileDataLevel) this.store.getTileDataSet(this.tileMatrixSet)
			.getTileDataLevel(matrixId)).getLayout();
		File file = layout.resolve(matrixId, x, y);
		if (file.exists()) {
			if (!file.delete()) {
				throw new TileIOException("Unable to delete tile file " + file);
			}
		}
	}

}
