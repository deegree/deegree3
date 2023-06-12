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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deegree.commons.utils.JDBCUtils;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.tools.coverage.gridifier.RasterLevel;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;

public class PostGISRasterTileIndex implements MultiLevelRasterTileIndex {

	private GeometryFactory geomFac = new GeometryFactory();

	private RasterLevel[] levels;

	private String tileTableName;

	private String levelTableName;

	private String jdbcUrl;

	private OriginLocation location;

	public PostGISRasterTileIndex(String jdbcUrl, String tileTableName, String levelTableName, OriginLocation location)
			throws SQLException {

		this.jdbcUrl = jdbcUrl;
		this.tileTableName = tileTableName;
		this.levelTableName = levelTableName;
		this.location = location;

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(jdbcUrl);
			levels = fetchScaleLevels(conn);
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

	public Set<TileFile> getTiles(Envelope env, double metersPerPixel) {

		RasterLevel level = getLevelForScale(metersPerPixel);
		Connection conn;
		try {
			conn = getDBConnection();
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}

		Set<TileFile> tiles = new HashSet<TileFile>();
		try {
			Statement stmt = conn.createStatement();
			String box3dArgument = "'BOX3D(" + env.getMin().get0() + " " + env.getMin().get1() + ","
					+ env.getMax().get0() + " " + env.getMax().get1() + ")'::box3d";
			String sql = "SELECT id,level,dir,file,bbox FROM " + tileTableName + " WHERE level=" + level.getLevel()
					+ " AND bbox && " + box3dArgument + " AND intersects(bbox," + box3dArgument + ") ";
			System.out.println(sql);
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				int id = rs.getInt(1);
				int levelNo = rs.getInt(2);
				String dir = rs.getString(3);
				String fileName = rs.getString(4);

				// get the bounding box (a bit of a hack)
				PGgeometry geom = (PGgeometry) rs.getObject(5);
				Polygon bboxPolygon = (Polygon) geom.getGeometry();
				Point min = bboxPolygon.getFirstPoint();
				Point max = bboxPolygon.getPoint(bboxPolygon.numPoints() - 3);
				float dbMinX = (float) min.x;
				float dbMinY = (float) min.y;
				float dbMaxX = (float) max.x;
				float dbMaxY = (float) max.y;

				if (level.getLevel() < 9) {
					tiles.add(new TileFile(id, levelNo, geomFac.createEnvelope(dbMinX, dbMinY, dbMaxX, dbMaxY, null),
							dir, fileName, (float) level.getMaxScale(), (float) -level.getMaxScale(), location));
				}
				else {
					tiles.add(new TileFile(id, levelNo, geomFac.createEnvelope(dbMinX, dbMinY, dbMaxX, dbMaxY, null),
							dir, fileName, 51.2f, -51.2f, location));
				}
			}
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			releaseConnection(conn);
		}

		return tiles;
	}

	private RasterLevel getLevelForScale(double maxMetersPerPixel) {
		System.out.println("Max meters per pixels:  " + maxMetersPerPixel);
		RasterLevel level = null;
		for (int i = levels.length - 1; i >= 0; i--) {
			if (maxMetersPerPixel > levels[i].getMinScale()) {
				level = levels[i];
				break;
			}
		}
		System.out.println("Determined Level, max meters per pixels:  " + (level == null ? -1 : level.getMaxScale())
				+ ", level: " + level);
		return level;
	}

	private RasterLevel[] fetchScaleLevels(Connection conn) throws SQLException {

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
			return scaleLevels.toArray(new RasterLevel[scaleLevels.size()]);
		}
		finally {
			JDBCUtils.close(rs);
			JDBCUtils.close(stmt);
		}
	}

	private Connection getDBConnection() throws SQLException {
		return DriverManager.getConnection(jdbcUrl);
	}

	private void releaseConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public RasterLevel[] getRasterLevels() {
		return levels;
	}

}
