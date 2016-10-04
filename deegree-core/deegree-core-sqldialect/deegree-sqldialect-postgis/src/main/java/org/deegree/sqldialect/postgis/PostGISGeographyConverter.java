package org.deegree.sqldialect.postgis;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.io.WKBWriter;
import org.deegree.geometry.utils.GeometryParticleConverter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link GeometryParticleConverter} for PostGIS databases. Legacy predicates are not used!
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class PostGISGeographyConverter extends AbstractPostGISConverter {

    /**
     * Creates a new {@link PostGISGeographyConverter} instance.
     *
     * @param column (unqualified) column that stores the geometry, must not be <code>null</code>
     * @param crs    CRS of the stored geometries, can be <code>null</code>
     * @param srid   PostGIS spatial reference identifier, must not be <code>null</code>
     */
    public PostGISGeographyConverter( String column, ICRS crs, String srid ) {
        super( column, crs, srid );
    }

    @Override
    public String getSrid() {
        return srid;
    }

    @Override
    public ICRS getCrs() {
        return crs;
    }

    @Override
    public String getSelectSnippet( String tableAlias ) {
        throw new UnsupportedOperationException( "Select of geography columns is currently not supported" );
    }

    @Override
    public Geometry toParticle( ResultSet rs, int colIndex )
                            throws SQLException {
        throw new UnsupportedOperationException( "Select of geography columns is currently not supported" );
    }

    @Override
    public String getSetSnippet( Geometry particle ) {
        StringBuilder sb = new StringBuilder();
        sb.append( "ST_SetSRID(ST_GeogFromWKB(?)," );
        sb.append( srid == null ? "-1" : srid );
        sb.append( ")" );
        return sb.toString();
    }

    @Override
    public void setParticle( PreparedStatement stmt, Geometry particle, int paramIndex )
                            throws SQLException {
        byte[] wkb = null;
        if ( particle != null ) {
            try {
                Geometry compatible = getCompatibleGeometry( particle );
                wkb = WKBWriter.write( compatible );
            } catch ( Throwable t ) {
                throw new IllegalArgumentException( t.getMessage(), t );
            }
        }
        stmt.setBytes( paramIndex, wkb );
    }

}