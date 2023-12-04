/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.tools.coverage.gridifier.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deegree.geometry.Envelope;

/**
 * The <code></code> class TODO add class documentation here.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class MemoryRasterTileGridIndex {

	private static final double AVG_LOAD = 100;

	private final float domainMinX;

	private final float domainMinY;

	// private final float domainMaxX;

	// private final float domainMaxY;

	private final float domainWidth;

	private final float domainHeight;

	private int rows;

	private int columns;

	private List<TileFile>[] gridCells;

	// private float cellWidth;

	// private float cellHeight;

	@SuppressWarnings("unchecked")
	public MemoryRasterTileGridIndex(float minX, float minY, float maxX, float maxY, Collection<TileFile> tileFiles) {
		this.domainMinX = minX;
		this.domainMinY = minY;
		// this.domainMaxX = maxX;
		// this.domainMaxY = maxY;
		this.domainWidth = maxX - minX;
		this.domainHeight = maxY - minY;

		int n = (int) (Math.sqrt(tileFiles.size() / AVG_LOAD) + 0.5);
		if (n == 0) {
			n = 1;
		}
		System.out.println(
				tileFiles.size() + " tiles, -> using " + n + " rows and columns for an average load of " + AVG_LOAD);
		rows = n;
		columns = n;
		// cellWidth = domainWidth / columns;
		// cellHeight = domainHeight / columns;
		gridCells = new List[rows * columns];

		for (TileFile tileFile : tileFiles) {
			insertTile(tileFile);
		}

		int minLoad = Integer.MAX_VALUE;
		int maxLoad = Integer.MIN_VALUE;

		int i = 0;
		for (int rowId = 0; rowId < rows; rowId++) {
			for (int columnId = 0; columnId < columns; columnId++) {
				i++;
				List<TileFile> filesInCell = gridCells[getCellIdx(columnId, rowId)];
				if (filesInCell != null) {
					int number = filesInCell.size();
					if (number < minLoad) {
						minLoad = number;
					}
					if (number > maxLoad) {
						maxLoad = number;
					}
				}
			}
		}
		System.out.println("i: " + i + ", min load: " + minLoad + ", max load: " + maxLoad);
	}

	private void insertTile(TileFile tileFile) {

		int minColumnId = getColumnIdx((float) tileFile.getGeoEnvelope().getMin().get0());
		int minRowId = getRowIdx((float) tileFile.getGeoEnvelope().getMin().get1());
		int maxColumnId = getColumnIdx((float) tileFile.getGeoEnvelope().getMax().get0());
		int maxRowId = getRowIdx((float) tileFile.getGeoEnvelope().getMax().get1());

		for (int rowId = minRowId; rowId <= maxRowId; rowId++) {
			for (int columnId = minColumnId; columnId <= maxColumnId; columnId++) {
				List<TileFile> filesInCell = gridCells[getCellIdx(columnId, rowId)];
				if (filesInCell == null) {
					filesInCell = new ArrayList<TileFile>();
					gridCells[getCellIdx(columnId, rowId)] = filesInCell;
				}
				filesInCell.add(tileFile);
			}
		}
	}

	public Set<TileFile> getTiles(
			Envelope bbox /* float minX, float minY, float maxX, float maxY */) {

		Set<TileFile> tiles = new HashSet<TileFile>();
		org.deegree.geometry.primitive.Point min = bbox.getMin();
		org.deegree.geometry.primitive.Point max = bbox.getMax();
		int minColumnId = getColumnIdx((float) min.get0());
		int minRowId = getRowIdx((float) min.get1());
		int maxColumnId = getColumnIdx((float) max.get0());
		int maxRowId = getRowIdx((float) max.get1());

		for (int rowId = minRowId; rowId <= maxRowId; rowId++) {
			for (int columnId = minColumnId; columnId <= maxColumnId; columnId++) {
				List<TileFile> filesInCell = gridCells[getCellIdx(columnId, rowId)];
				if (filesInCell != null) {
					for (TileFile tileFile : filesInCell) {
						if (tileFile.intersects(bbox)) {
							tiles.add(tileFile);
						}
					}
				}
			}
		}

		return tiles;
	}

	private int getCellIdx(int columnIdx, int rowIdx) {
		return rowIdx * columns + columnIdx;
	}

	public int getColumnIdx(float x) {
		float dx = x - domainMinX;
		int columnIdx = (int) (columns * dx / domainWidth);
		if (columnIdx < 0) {
			columnIdx = 0;
		}
		if (columnIdx > columns - 1) {
			columnIdx = columns - 1;
		}
		return columnIdx;
	}

	public int getRowIdx(float y) {
		float dy = y - domainMinY;
		int rowIdx = (int) (rows * dy / domainHeight);
		if (rowIdx < 0) {
			rowIdx = 0;
		}
		if (rowIdx > rows - 1) {
			rowIdx = rows - 1;
		}
		return rowIdx;
	}

}
