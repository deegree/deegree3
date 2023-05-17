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
package org.deegree.tile.persistence.filesystem.layout;

import static java.io.File.separatorChar;

import java.io.File;
import java.text.DecimalFormat;

import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.persistence.filesystem.DiskLayout;

/**
 * {@link DiskLayout} implementation for the disk cache used by
 * <a href="http://tilecache.org/">TileCache</a>.
 * <p>
 * TileCache uses a hierarchy of 7 directories to organize tiles.<br/>
 * <br/>
 * Structure: <code>zz/xxx/xxx/xxx/yyy/yyy/yyy.format</code><br/>
 * <br/>
 * Example: <code>layername/01/018/782/353/786/347/862.filetype</code>
 * </p>
 * <ul>
 * <li>1st directory: <i>layername</i></li>
 * <li>2nd directory: <i>zoomlevel</i> (using 2 digits eg. 01, counting starts with
 * 0)</li>
 * <li>3rd-5th directory: <i>column number (x)</i>, split into thousands: x = 018782353
 * results in 018/782/353</li>
 * <li>6rd-7th directory + filename: <b>inverted</b> <i>row number (y)</i>, split into
 * thousands: y = 786347862 results in 786/347/862</li>
 * <li>filename suffix: <i>filetype</i></li>
 * </ul>
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class TileCacheDiskLayout implements DiskLayout {

	private final File layerDir;

	private final String fileType;

	private TileDataSet set;

	/**
	 * Creates a new {@link TileCacheDiskLayout} instance.
	 * @param baseDir layer directory, must not be <code>null</code>
	 * @param fileType suffix of the tile files (without '.'), must not be
	 * <code>null</code>
	 */
	public TileCacheDiskLayout(File baseDir, String fileType) {
		this.layerDir = baseDir;
		this.fileType = fileType;
	}

	@Override
	public void setTileMatrixSet(TileDataSet set) {
		this.set = set;
	}

	@Override
	public File resolve(String matrixId, long x, long y) {

		TileDataLevel tileMatrix = set.getTileDataLevel(matrixId);
		if (tileMatrix == null) {
			return null;
		}
		if (tileMatrix.getMetadata().getNumTilesX() <= x || tileMatrix.getMetadata().getNumTilesY() <= y || x < 0
				|| y < 0) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		String levelDirectory = getLevelDirectory(tileMatrix);
		String columnFileNamePart = getColumnFileNamePart(x);
		String rowFileNamePart = getRowFileNamePart(y, tileMatrix);

		sb.append(levelDirectory);
		sb.append(separatorChar);
		sb.append(columnFileNamePart);
		sb.append(separatorChar);
		sb.append(rowFileNamePart);

		return new File(layerDir, sb.toString());
	}

	private String getLevelDirectory(TileDataLevel tileMatrix) {
		DecimalFormat formatter = new DecimalFormat("00");
		int tileMatrixIndex = set.getTileDataLevels().indexOf(tileMatrix);
		return formatter.format(tileMatrixIndex);
	}

	private String getColumnFileNamePart(long x) {
		StringBuilder sb = new StringBuilder();
		DecimalFormat formatter = new DecimalFormat("000");
		sb.append(formatter.format(x / 1000000));
		sb.append(separatorChar);
		sb.append(formatter.format(x / 1000 % 1000));
		sb.append(separatorChar);
		sb.append(formatter.format(x % 1000));
		sb.append(separatorChar);
		return sb.toString();
	}

	private String getRowFileNamePart(long y, TileDataLevel tileMatrix) {
		long tileCacheY = getTileCacheYIndex(tileMatrix, y);
		StringBuilder sb = new StringBuilder();
		DecimalFormat formatter = new DecimalFormat("000");
		sb.append(formatter.format(tileCacheY / 1000000));
		sb.append(separatorChar);
		sb.append(formatter.format(tileCacheY / 1000 % 1000));
		sb.append(separatorChar);
		sb.append(formatter.format(tileCacheY % 1000));
		sb.append('.');
		sb.append(fileType);
		return sb.toString();
	}

	private long getTileCacheYIndex(TileDataLevel tileMatrix, long y) {
		// TileCache's y-axis is inverted
		return tileMatrix.getMetadata().getNumTilesY() - 1 - y;
	}

	@Override
	public String getFileType() {
		return fileType;
	}

}
