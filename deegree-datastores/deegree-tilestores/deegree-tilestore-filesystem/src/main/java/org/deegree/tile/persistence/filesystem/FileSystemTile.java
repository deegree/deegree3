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

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Envelope;
import org.deegree.tile.Tile;
import org.deegree.tile.TileIOException;

/**
 * A {@link Tile} that is backed by a {@link FileSystemTileStore}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
class FileSystemTile implements Tile {

	private final Envelope bbox;

	private final File file;

	/**
	 * Creates a new {@link FileSystemTile} instance.
	 * @param bbox envelope of the tile, must not be <code>null</code>
	 * @param file image file, must not be <code>null</code> and point to an existing
	 * image file
	 */
	FileSystemTile(Envelope bbox, File file) {
		this.bbox = bbox;
		this.file = file;
	}

	@Override
	public BufferedImage getAsImage() throws TileIOException {
		InputStream in = null;
		try {
			in = getAsStream();
			return ImageIO.read(in);
		}
		catch (IOException e) {
			throw new TileIOException("Error decoding tile from file '" + file + "'" + e.getMessage(), e);
		}
		finally {
			closeQuietly(in);
		}
	}

	@Override
	public InputStream getAsStream() throws TileIOException {
		try {
			return new FileInputStream(file);
		}
		catch (FileNotFoundException e) {
			throw new TileIOException("Tile file '" + file + "' does not exist.");
		}
	}

	@Override
	public Envelope getEnvelope() {
		return bbox;
	}

	@Override
	public FeatureCollection getFeatures(int i, int j, int limit) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Feature retrieval is not supported by the FileSystemTileStore.");
	}

}
