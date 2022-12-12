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
package org.deegree.sqldialect.gpkg;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKBReader;
import org.deegree.geometry.utils.GeometryParticleConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link GeometryParticleConverter} for GeoPackage databases.
 *
 * @author <a href="mailto:migliavacca@lat-lon.de">Diego Migliavacca</a>
 * @since 3.5
 */
public class GpkgGeometryConverter implements GeometryParticleConverter {

    private static Logger LOG = LoggerFactory.getLogger( GpkgGeometryConverter.class );

    private final String column;

    private final ICRS crs;

    private final String srid;

    /**
     * Creates a new {@link GpkgGeometryConverter} instance.
     *
     * @param column (unqualified) column that stores the geometry, must not be <code>null</code>
     * @param crs    CRS of the stored geometries, can be <code>null</code>
     * @param srid   PostGIS spatial reference identifier, must not be <code>null</code>
     */
    public GpkgGeometryConverter( String column, ICRS crs, String srid ) {
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
        return null;
    }

    @Override
    public Geometry toParticle( ResultSet rs, int colIndex )
                            throws SQLException {
        ByteBuffer bb = ByteBuffer.wrap( rs.getBytes( colIndex ) );
        if ( bb == null ) {
            return null;
        }
        try {
            int[] header = parseGpkgHeader( bb );
            int headerLength = header[0];
            int endian = header[1];
            return parseGpkgGeometry( bb, headerLength, endian );
        } catch ( Throwable t ) {
            throw new IllegalArgumentException( t.getMessage(), t );
        }
    }

    private int[] parseGpkgHeader( ByteBuffer pgb )
                            throws Exception {
        byte bytes = pgb.get( 3 );
        int endian = bytes & 0x01;
        int headerLength = getFlags( bytes );
        return new int[] { headerLength, endian };
    }

    private int getFlags( byte bytes )
                            throws Exception {
        int flags = ( bytes & 0x0E ) >> 1;
        return getHeaderLength( flags );
    }

    @SuppressWarnings("unchecked")
    private int getHeaderLength( int flag )
                            throws Exception {
        Map<Integer, Integer> eb = new HashMap();
        eb.put( 0, 8 );
        eb.put( 1, 40 );
        eb.put( 2, 56 );
        eb.put( 3, 56 );
        eb.put( 4, 72 );

        int envelopeLength = 0;
        try {
            envelopeLength = eb.get( flag );
        } catch ( Exception e ) {
            System.out.println( "Invalid envelope code value:" + flag );
        }
        return envelopeLength;
    }

    private Geometry parseGpkgGeometry( ByteBuffer byb, int headerLength, int endian )
                            throws Exception {
        WKBReader wkbReader = new WKBReader();
        if ( endian == 0 )
            byb.order( ByteOrder.BIG_ENDIAN );
        else {
            byb.order( ByteOrder.LITTLE_ENDIAN );
        }
        byte[] wkb = new byte[byb.capacity() - headerLength];
        byb.position( headerLength );
        byb.get( wkb, 0, wkb.length );
        Geometry gpkgGeom = wkbReader.read( wkb, crs );
        if ( gpkgGeom == null ) {
            throw new Exception( "Unable to parse the GeoPackage geometry" );
        }
        return gpkgGeom;
    }

    @Override
    public void setParticle( PreparedStatement stmt, Geometry particle, int paramIndex )
                            throws SQLException {
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
