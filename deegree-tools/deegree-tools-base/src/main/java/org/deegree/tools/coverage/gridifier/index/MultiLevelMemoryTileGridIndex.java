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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deegree.commons.utils.JDBCUtils;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.tools.coverage.gridifier.RasterLevel;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;

public class MultiLevelMemoryTileGridIndex implements MultiLevelRasterTileIndex {

	private final static GeometryFactory geomFac = new GeometryFactory();

	// private final int avgLoad;

	// quality levels, ordered descending (best quality first)
	private List<RasterLevel> levels = new ArrayList<RasterLevel>();

	private final float areaMinX, areaMinY, areaMaxX, areaMaxY;

	// key: quality level, value: corresponding quad tree
	private Map<RasterLevel, MemoryRasterTileGridIndex> levelToGrid = new HashMap<RasterLevel, MemoryRasterTileGridIndex>();

	private final String jdbcUrl;

	private String tileTableName;

	private String levelTableName;

	private OriginLocation location;

	public MultiLevelMemoryTileGridIndex(String jdbcUrl, String tileTableName, String levelTableName, float MIN_X,
			float MIN_Y, float MAX_X, float MAX_Y, OriginLocation location) throws SQLException {

		this.jdbcUrl = jdbcUrl;
		this.tileTableName = tileTableName;
		this.levelTableName = levelTableName;
		// this.avgLoad = avgLoad;
		this.areaMinX = MIN_X;
		this.areaMinY = MIN_Y;
		this.areaMaxX = MAX_X;
		this.areaMaxY = MAX_Y;
		this.location = location;

		Connection conn = null;
		try {
			conn = getDBConnection();
			levels = fetchScaleLevels(conn);
			for (RasterLevel level : levels) {
				levelToGrid.put(level, buildGridIndex(conn, level));
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public RasterLevel[] getRasterLevels() {
		return levels.toArray(new RasterLevel[levels.size()]);
	}

	public Set<TileFile> getTiles(Envelope bbox, double metersPerPixel) {

		RasterLevel level = getLevelForScale(metersPerPixel);
		MemoryRasterTileGridIndex index = levelToGrid.get(level);
		return index.getTiles(bbox);
	}

	private RasterLevel getLevelForScale(double maxMetersPerPixel) {
		RasterLevel level = null;
		for (int i = levels.size() - 1; i >= 0; i--) {
			if (maxMetersPerPixel > levels.get(i).getMinScale()) {
				level = levels.get(i);
				break;
			}
		}
		return level;
	}

	private MemoryRasterTileGridIndex buildGridIndex(Connection conn, RasterLevel level) throws SQLException {

		System.out.println("Building memory grid index for level " + level + "...");

		List<TileFile> tiles = new ArrayList<TileFile>();
		String box3dArgument = "'BOX3D(" + areaMinX + " " + areaMinY + "," + areaMaxX + " " + areaMaxY + ")'::box3d";

		String sql = "SELECT id,level,dir,file,bbox FROM " + tileTableName + " WHERE level=" + level.getLevel()
				+ " AND bbox && " + box3dArgument + " AND intersects(bbox," + box3dArgument + ")";
		System.out.println(sql);
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			int num = 0;
			while (rs.next()) {
				num++;
				int id = rs.getInt(1);
				int levelNo = rs.getInt(2);
				String dir = rs.getString(3);
				String fileName = rs.getString(4);

				// get the bounding box (a bit of a hack)
				PGgeometry geom = (PGgeometry) rs.getObject(5);
				Polygon bboxPolygon = (Polygon) geom.getGeometry();
				Point min = bboxPolygon.getFirstPoint();
				Point max = bboxPolygon.getPoint(bboxPolygon.numPoints() - 3);
				float minX = (float) min.x;
				float minY = (float) min.y;
				float maxX = (float) max.x;
				float maxY = (float) max.y;

				if (level.getLevel() < 9) {
					tiles.add(new TileFile(id, levelNo, geomFac.createEnvelope(minX, minY, maxX, maxY, null), dir,
							fileName, (float) level.getMaxScale(), (float) -level.getMaxScale(), location));
				}
				else {
					tiles.add(new TileFile(id, levelNo, geomFac.createEnvelope(minX, minY, maxX, maxY, null), dir,
							fileName, 51.2f, -51.2f, location));
				}
			}
			System.out.println("Total: " + tiles.size() + " tiles.");
			return new MemoryRasterTileGridIndex(areaMinX, areaMinY, areaMaxX, areaMaxY, tiles);
		}
		finally {
			JDBCUtils.close(rs);
			JDBCUtils.close(stmt);
		}
	}

	private List<RasterLevel> fetchScaleLevels(Connection conn) throws SQLException {

		List<RasterLevel> scaleLevels = new ArrayList<RasterLevel>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt
				.executeQuery("SELECT id, level, minscale, maxscale FROM " + levelTableName + " ORDER BY minscale");

			while (rs.next()) {
				int id = rs.getInt(1);
				int level = rs.getInt(2);
				double minScale = rs.getDouble(3);
				double maxScale = rs.getDouble(4);
				RasterLevel o = new RasterLevel(id, level, minScale, maxScale);
				scaleLevels.add(o);
			}
			return scaleLevels;
		}
		finally {
			JDBCUtils.close(rs);
			JDBCUtils.close(stmt);
		}
	}

	private Connection getDBConnection() throws SQLException {
		return DriverManager.getConnection(jdbcUrl);
	}

}
