package org.deegree.sqldialect.postgis;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.utils.GeometryParticleConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * {@link AbstractPostGISConverter} for PostGIS databases.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public abstract class AbstractPostGISConverter implements GeometryParticleConverter {

    private static Logger LOG = LoggerFactory.getLogger( AbstractPostGISConverter.class );

    protected final String column;

    protected final ICRS crs;

    protected final String srid;

    /**
     * Creates a new {@link PostGISGeometryConverter} instance.
     *
     * @param column (unqualified) column that stores the geometry, must not be <code>null</code>
     * @param crs    CRS of the stored geometries, can be <code>null</code>
     * @param srid   PostGIS spatial reference identifier, must not be <code>null</code>
     */
    public AbstractPostGISConverter( String column, ICRS crs, String srid ) {
        this.column = column;
        this.crs = crs;
        this.srid = srid;
    }

    protected Geometry getCompatibleGeometry( Geometry literal )
                            throws SQLException {
        if ( crs == null ) {
            return literal;
        }

        Geometry transformedLiteral = literal;
        if ( literal != null ) {
            ICRS literalCRS = literal.getCoordinateSystem();
            if ( literalCRS != null && !( crs.equals( literalCRS ) ) ) {
                LOG.debug( "Need transformed literal geometry for evaluation: " + literalCRS.getAlias() +
                           " -> "
                           + crs.getAlias() );
                try {
                    GeometryTransformer transformer = new GeometryTransformer( crs );
                    transformedLiteral = transformer.transform( literal );
                } catch ( Exception e ) {
                    throw new SQLException( e.getMessage(), e );
                }
            }
        }
        return transformedLiteral;
    }

    @Override
    public String getSrid() {
        return srid;
    }

    @Override
    public ICRS getCrs() {
        return crs;
    }

}