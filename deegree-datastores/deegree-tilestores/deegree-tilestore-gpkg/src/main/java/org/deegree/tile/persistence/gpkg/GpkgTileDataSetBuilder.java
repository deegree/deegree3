//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.gpkg;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.utils.Pair;
import org.deegree.db.legacy.LegacyConnectionProvider;
import org.deegree.tile.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Builds tile data sets from jaxb config beans.
 *
 * @author <a href="mailto:migliavacca@lat-lon.de">Diego Migliavacca</a>
 * @since 3.5
 */
class GpkgTileDataSetBuilder {

	private final org.deegree.tile.persistence.gpkg.jaxb.GpkgTileStoreJAXB cfg;

	private String format;

	private String identifier;

	private TileMatrixSet tms;

	GpkgTileDataSetBuilder(org.deegree.tile.persistence.gpkg.jaxb.GpkgTileStoreJAXB cfg, TileMatrixSet tms) {
		this.cfg = cfg;
		this.tms = tms;
		this.format = cfg.getTileDataSet().getImageFormat();
		this.identifier = cfg.getTileDataSet().getIdentifier();
	}

	Map<String, TileDataSet> extractTileDataSets() throws ResourceInitException {
		Map<String, TileDataSet> tileDataSet = new HashMap<String, TileDataSet>();
		tileDataSet.put(identifier, buildTileDataSet());
		return tileDataSet;
	}

	public TileDataSet buildTileDataSet() throws ResourceInitException {
		List<TileDataLevel> list = new ArrayList<TileDataLevel>();
		for (TileMatrix tm : tms.getTileMatrices()) {
			String idTm = tm.getIdentifier();
			Map<Pair<Long, Long>, byte[]> ts = getTileData(idTm);
			TileDataLevel tdl = new GpkgTileDataLevel(tm, ts);
			list.add(tdl);
		}
		// if ( format == null ) {
		// format = "image/jpg";
		// }
		return new DefaultTileDataSet(list, tms, format);
	}

	public Map<Pair<Long, Long>, byte[]> getTileData(String id) {
		Map<Pair<Long, Long>, byte[]> mapTile = new LinkedHashMap<Pair<Long, Long>, byte[]>();
		try {
			LegacyConnectionProvider connProvider = new LegacyConnectionProvider(
					"jdbc:sqlite:/" + cfg.getTileDataSet().getFile(), "", "", false, null);
			Connection conn = connProvider.getConnection();
			Statement stmt = conn.createStatement();
			String table = cfg.getTileDataSet().getTileMapping().getTable();
			String query = "select * from " + table + " where zoom_level = " + id;
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				long row = rs.getLong(4);
				long column = rs.getLong(3);
				byte[] imgBytes = rs.getBytes(5);
				Pair<Long, Long> keyTile = new Pair<Long, Long>();
				keyTile.setFirst(row);
				keyTile.setSecond(column);
				mapTile.put(keyTile, imgBytes);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return mapTile;
	}

}
