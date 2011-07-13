//$HeadURL: svn+ssh://criador.lat-lon.de/srv/svn/deegree-intern/trunk/latlon-sqldialect-oracle/src/main/java/de/latlon/deegree/sqldialect/oracle/OracleGeometryConverter.java $
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
package org.deegree.sqldialect.oracle;

import static oracle.spatial.geometry.JGeometry.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import oracle.jdbc.OracleConnection;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.apache.commons.dbcp.DelegatingConnection;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.utils.GeometryParticleConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: 303 $, $Date: 2011-06-14 17:20:13 +0200 (Di, 14. Jun 2011) $
 */
public class OracleGeometryConverter implements GeometryParticleConverter {

    private static Logger LOG = LoggerFactory.getLogger( OracleGeometryConverter.class );

    private final String column;

    private final ICRS crs;

    private final String srid;

    /**
     * Creates a new {@link OracleGeometryConverter} instance.
     * 
     * @param column
     *            (unqualified) column that stores the geometry, must not be <code>null</code>
     * @param crs
     *            CRS of the stored geometries, can be <code>null</code>
     * @param srid
     *            Oracle spatial reference identifier, must not be <code>null</code>
     */
    public OracleGeometryConverter( String column, ICRS crs, String srid ) {
        this.column = column;
        this.crs = crs;
        this.srid = srid;
    }

    @Override
    public String getSelectSnippet( String tableAlias ) {
        if ( tableAlias != null ) {
            return tableAlias + "." + column;
        }
        return column;
    }

    @Override
    public String getSetSnippet( Geometry particle ) {
        return "?";
    }

    @Override
    public Geometry toParticle( ResultSet rs, int colIndex )
                            throws SQLException {
        Object sqlValue = rs.getObject( colIndex );
        if ( sqlValue == null ) {
            return null;
        }
        try {
            String srid = this.srid;
            srid = srid == null ? "0" : srid;
            return new JGeometryAdapter( crs, Integer.parseInt( srid ) ).toGeometry( JGeometry.load( (STRUCT) sqlValue ) );
        } catch ( Throwable t ) {
            t.printStackTrace();
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void setParticle( PreparedStatement stmt, Geometry particle, int paramIndex )
                            throws SQLException {
        try {
            OracleConnection oraConn = getOracleConnection( stmt.getConnection() );
            if ( particle == null ) {
                stmt.setNull( paramIndex, Types.STRUCT, "SDO_GEOMETRY" );
            } else {
                Geometry compatible = getCompatibleGeometry( particle );
                if ( compatible instanceof Envelope ) {
                    compatible = compatible.getConvexHull();
                }
                JGeometry jGeometry = null;
                String srid = this.srid;
                srid = srid == null ? "0" : srid;
                jGeometry = new JGeometryAdapter( crs, Integer.parseInt( srid ) ).toJGeometry( compatible );
                STRUCT struct = store( jGeometry, oraConn );
                stmt.setObject( paramIndex, struct );
            }
        } catch ( Throwable t ) {
            t.printStackTrace();
            throw new IllegalArgumentException();
        }
    }

    private OracleConnection getOracleConnection( Connection conn ) {
        OracleConnection oraconn = null;
        if ( conn instanceof DelegatingConnection ) {
            oraconn = (OracleConnection) ( (DelegatingConnection) conn ).getInnermostDelegate();
        }
        return oraconn;
    }

    private Geometry getCompatibleGeometry( Geometry literal )
                            throws SQLException {
        if ( crs == null ) {
            return literal;
        }

        Geometry transformedLiteral = literal;
        if ( literal != null ) {
            ICRS literalCRS = literal.getCoordinateSystem();
            if ( literalCRS != null && !( crs.equals( literalCRS ) ) ) {
                LOG.debug( "Need transformed literal geometry for evaluation: " + literalCRS.getAlias() + " -> "
                           + crs.getAlias() );
                try {
                    GeometryTransformer transformer = new GeometryTransformer( crs );
                    transformedLiteral = transformer.transform( literal );
                } catch ( Exception e ) {
                    throw new SQLException( e.getMessage() );
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