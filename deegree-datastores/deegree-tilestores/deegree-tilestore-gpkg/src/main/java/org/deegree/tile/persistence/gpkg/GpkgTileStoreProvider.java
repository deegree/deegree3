//$HeadURL$
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
package org.deegree.tile.persistence.gpkg;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.GenericTileStore;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

/**
 * The GpkgTileStoreProvider provides a TileSet out of a GeoPackage database.
 *
 * @author <a href="mailto:migliavacca@lat-lon.de">Diego Migliavacca</a>
 * @author last edited by: $Author: dmigliavacca $
 */

public class GpkgTileStoreProvider implements TileStoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger( GpkgTileStoreProvider.class );

    private static final URL SCHEMA = GpkgTileStoreProvider.class.getResource( "/META-INF/schemas/datasource/tile/gpkg/3.2.0/geopackage.xsd" );

    private org.deegree.tile.persistence.gpkg.jaxb.GpkgTileStoreJAXB cfg;

    private String table;

    private DeegreeWorkspace workspace;

    private Connection conn = null;

    private List<TileMatrix> matrices;

    private final GeometryFactory fac = new GeometryFactory();

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @Override
    public URL getConfigSchema() {
        return SCHEMA;
    }

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/datasource/tile/gpkg";
    }

    @Override
    public TileStore create( URL configUrl )
                            throws ResourceInitException {
        try {
            cfg = (org.deegree.tile.persistence.gpkg.jaxb.GpkgTileStoreJAXB) unmarshall( "org.deegree.tile.persistence.gpkg.jaxb",
                                                                                         SCHEMA, configUrl, workspace );

            table = cfg.getTileDataSet().getTileMapping().getTable();
            String id;
            ConnectionManager mgr = workspace.getSubsystemManager( ConnectionManager.class );
            conn = mgr.get( cfg.getTileDataSet().getJDBCConnId() );
            Type connType = mgr.getType( cfg.getTileDataSet().getJDBCConnId() );
            if ( connType == null ) {
                throw new ResourceInitException( "No JDBC connection with id '" + cfg.getTileDataSet().getJDBCConnId() + "' defined." );
            }
            LOG.debug( "Connection type is {}.", connType );
            TileMatrix tm;
            matrices = new ArrayList<TileMatrix>();
            try {
                Statement stmt = conn.createStatement();
                String query = "select * from gpkg_tile_matrix where table_name = '" + table + "'";
                ResultSet rs = stmt.executeQuery( query );
                if ( rs == null ) {
                    throw new ResourceInitException(
                                            "No information could be read from gpkg_tile_matrix table. Please add the table to the GeoPackage." );
                }
                SpatialMetadata sm = getTileMatrixSet().getSpatialMetadata();
                while ( rs.next() ) {
                    id = rs.getString( 2 );
                    long numx = rs.getLong( 3 );
                    long numy = rs.getLong( 4 );
                    long tileWidth = rs.getLong( 5 );
                    long tsx = rs.getLong( 7 );
                    long tsy = rs.getLong( 8 );
                    double res = (double) ( tsx / tileWidth );
                    tm = new TileMatrix( id, sm, tsx, tsy, res, numx, numy );
                    matrices.add( tm );
                }
            } catch ( SQLException e ) {
                e.printStackTrace();
            }
            GpkgTileDataSetBuilder builder = new GpkgTileDataSetBuilder( cfg, workspace, getTileMatrixSet() );
            Map<String, TileDataSet> map = builder.extractTileDataSets();
            return new GenericTileStore( map );
        } catch ( JAXBException e ) {
            LOG.info( "Stack trace: ", e );
            throw new ResourceInitException( "Error when parsing configuration: " + e.getLocalizedMessage(), e );
        }
    }

    public TileMatrixSet getTileMatrixSet() {
        String id = null;
        SpatialMetadata spatialMetadata = null;
        try {
            Statement stmt = conn.createStatement();
            String query = "select * from gpkg_tile_matrix_set where table_name = '" + table + "'";
            ResultSet rs = stmt.executeQuery( query );
            if ( rs == null ) {
                throw new ResourceInitException(
                                        "No information could be read from gpkg_tile_matrix_set table. Please add the table to the GeoPackage." );
            }
            id = rs.getString( 1 );
            ICRS srs = CRSManager.lookup( "EPSG:" + rs.getString( 2 ) );
            if ( srs == null ) {
                throw new ResourceInitException(
                                        "No SRS information could be read from GeoPackage. Please add one to the GeoPackage." );
            }
            double minx = rs.getDouble( 3 );
            double miny = rs.getDouble( 4 );
            double maxx = rs.getDouble( 5 );
            double maxy = rs.getDouble( 6 );
            Envelope env = fac.createEnvelope( minx, miny, maxx, maxy, srs );
            if ( env == null ) {
                throw new ResourceInitException(
                                        "No envelope information could be read from GeoPackage. Please add one to the GeoPackage." );
            }
            spatialMetadata = new SpatialMetadata( env, singletonList( env.getCoordinateSystem() ) );
        } catch ( SQLException e ) {
            e.printStackTrace();
        } catch ( UnknownCRSException e ) {
            e.printStackTrace();
        } catch ( ResourceInitException e ) {
            e.printStackTrace();
        }
        return new TileMatrixSet( id, null, matrices, spatialMetadata );
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { };
    }

    @Override
    public List<File> getTileStoreDependencies( File config ) {
        return Collections.<File>emptyList();
    }

}
