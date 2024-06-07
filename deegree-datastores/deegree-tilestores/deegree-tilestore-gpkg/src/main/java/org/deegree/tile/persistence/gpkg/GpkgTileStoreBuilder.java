/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.gpkg;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.db.legacy.LegacyConnectionProvider;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.GenericTileStore;
import org.deegree.tile.persistence.TileStore;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * This class is responsible for building GeoPackage tile stores.
 *
 * @author <a href="mailto:migliavacca@lat-lon.de">Diego Migliavacca</a>
 * @since 3.5
 */
public class GpkgTileStoreBuilder implements ResourceBuilder<TileStore> {

	private org.deegree.tile.persistence.gpkg.jaxb.GpkgTileStoreJAXB cfg;

	private ResourceMetadata<TileStore> metadata;

	private String table;

	private Connection conn = null;

	private List<TileMatrix> matrices;

	private final GeometryFactory fac = new GeometryFactory();

	public GpkgTileStoreBuilder(org.deegree.tile.persistence.gpkg.jaxb.GpkgTileStoreJAXB cfg,
			ResourceMetadata<TileStore> metadata) {
		this.cfg = cfg;
		this.metadata = metadata;
	}

	@Override
	public TileStore build() {
		try {
			table = cfg.getTileDataSet().getTileMapping().getTable();
			String id;
			LegacyConnectionProvider connProvider = new LegacyConnectionProvider(
					"jdbc:sqlite:/" + cfg.getTileDataSet().getFile(), "", "", false, null);
			conn = connProvider.getConnection();
			TileMatrix tm;
			matrices = new ArrayList<TileMatrix>();
			try {
				Statement stmt = conn.createStatement();
				String query = "select * from gpkg_tile_matrix where table_name = '" + table + "'";
				ResultSet rs = stmt.executeQuery(query);
				if (rs == null) {
					throw new ResourceInitException(
							"No information could be read from gpkg_tile_matrix table. Please add the table to the GeoPackage.");
				}
				SpatialMetadata sm = getTileMatrixSet().getSpatialMetadata();
				while (rs.next()) {
					id = rs.getString(2);
					long numx = rs.getLong(3);
					long numy = rs.getLong(4);
					long tileWidth = rs.getLong(5);
					long tsx = rs.getLong(7);
					long tsy = rs.getLong(8);
					double res = (double) (tsx / tileWidth);
					tm = new TileMatrix(id, sm, tsx, tsy, res, numx, numy);
					matrices.add(tm);
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			GpkgTileDataSetBuilder builder = new GpkgTileDataSetBuilder(cfg, getTileMatrixSet());
			Map<String, TileDataSet> map = builder.extractTileDataSets();
			return new GenericTileStore(map, metadata);
		}
		catch (Exception e) {
			throw new ResourceInitException("Error when parsing configuration: " + e.getLocalizedMessage(), e);
		}
	}

	public TileMatrixSet getTileMatrixSet() {
		String id = null;
		SpatialMetadata spatialMetadata = null;
		try {
			Statement stmt = conn.createStatement();
			String query = "select * from gpkg_tile_matrix_set where table_name = '" + table + "'";
			ResultSet rs = stmt.executeQuery(query);
			if (rs == null) {
				throw new ResourceInitException(
						"No information could be read from gpkg_tile_matrix_set table. Please add the table to the GeoPackage.");
			}
			id = rs.getString(1);
			ICRS srs = CRSManager.lookup("EPSG:" + rs.getString(2));
			if (srs == null) {
				throw new ResourceInitException(
						"No SRS information could be read from GeoPackage. Please add one to the GeoPackage.");
			}
			double minx = rs.getDouble(3);
			double miny = rs.getDouble(4);
			double maxx = rs.getDouble(5);
			double maxy = rs.getDouble(6);
			Envelope env = fac.createEnvelope(minx, miny, maxx, maxy, srs);
			if (env == null) {
				throw new ResourceInitException(
						"No envelope information could be read from GeoPackage. Please add one to the GeoPackage.");
			}
			spatialMetadata = new SpatialMetadata(env, singletonList(env.getCoordinateSystem()));
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		catch (UnknownCRSException e) {
			e.printStackTrace();
		}
		catch (ResourceInitException e) {
			e.printStackTrace();
		}
		return new TileMatrixSet(id, null, matrices, spatialMetadata, null);
	}

}
