//$HeadURL$
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

package org.deegree.io.databaseloader;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.UUID;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.DBPoolException;
import org.deegree.io.JDBCConnection;
import org.deegree.io.datastore.sql.postgis.PGgeometryAdapter;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Surface;
import org.deegree.ogcwebservices.wms.configuration.DatabaseDataSource;
import org.postgis.PGboxbase;
import org.postgis.PGgeometry;
import org.postgresql.PGConnection;

/**
 * class for loading data as feature collection from a postgis database
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class PostgisDataLoader {

    private static final ILogger LOG = LoggerFactory.getLogger( PostgisDataLoader.class );

    private static final String GEOMETRY_DATATYPE_NAME = "geometry";

    private static final String BOX3D_DATATYPE_NAME = "box3d";

    private static final String PG_GEOMETRY_CLASS_NAME = "org.postgis.PGgeometry";

    private static final String PG_BOX3D_CLASS_NAME = "org.postgis.PGbox3d";

    private static Class<?> pgGeometryClass;

    private static Class<?> pgBox3dClass;

    private static URI namespace;
    static {
        try {
            namespace = new URI( "http://www.deegree.org/database" );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }
        try {
            pgGeometryClass = Class.forName( PG_GEOMETRY_CLASS_NAME );
        } catch ( ClassNotFoundException e ) {
            LOG.logError( "Cannot find class '" + PG_GEOMETRY_CLASS_NAME + "'.", e );
        }
        try {
            pgBox3dClass = Class.forName( PG_BOX3D_CLASS_NAME );
        } catch ( ClassNotFoundException e ) {
            LOG.logError( "Cannot find class '" + PG_BOX3D_CLASS_NAME + "'.", e );
        }
    }

    /**
     * @param datasource
     * @param envelope
     * @param sql
     * @return the fc
     * @throws Exception
     */
    public static FeatureCollection load( DatabaseDataSource datasource, Envelope envelope, String sql )
                            throws Exception {
        return load( datasource, envelope, sql, null );
    }

    /**
     * @param datasource
     * @param envelope
     * @param sql
     * @param extraClauses
     * @return the feature collection directly from the db
     * @throws Exception
     */
    public static FeatureCollection load( DatabaseDataSource datasource, Envelope envelope, String sql,
                                          String extraClauses )
                            throws Exception {
        if ( sql == null ) {
            sql = datasource.getSqlTemplate();
        }

        JDBCConnection jdbc = datasource.getJDBCConnection();
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet rs = null;
        FeatureCollection fc = FeatureFactory.createFeatureCollection( UUID.randomUUID().toString(), 10000 );
        try {
            CoordinateSystem crs = datasource.getNativeCRS();
            conn = acquireConnection( jdbc );
            stmt = createPreparedStatement( datasource, envelope, conn, crs, sql, extraClauses );

            rs = stmt.executeQuery();

            LOG.logDebug( "performing database query: " + sql );
            ResultSetMetaData rsmd = rs.getMetaData();
            FeatureType featureType = createFeatureType( datasource, datasource.getGeometryFieldName(), rsmd );
            int ccnt = rsmd.getColumnCount();
            int k = 0;

            // read each line from database and create a feature from it
            while ( rs.next() ) {
                FeatureProperty[] properties = new FeatureProperty[ccnt];
                for ( int i = 0; i < ccnt; i++ ) {
                    String name = rsmd.getColumnName( i + 1 );
                    Object value = rs.getObject( i + 1 );
                    // if column name equals geometry field name the value read from
                    // database must be converted into a deegree geometry
                    if ( name.equalsIgnoreCase( datasource.getGeometryFieldName() ) ) {
                        value = PGgeometryAdapter.wrap( (PGgeometry) value, crs );
                    }
                    properties[i] = FeatureFactory.createFeatureProperty( featureType.getPropertyName( i ), value );
                }
                // because feature IDs are not important in case of database datasource
                // it is just 'ID' as prefix plus a number of current row
                fc.add( FeatureFactory.createFeature( "ID" + k++, featureType, properties ) );
            }
            LOG.logDebug( k + " features loaded from database" );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw e;
        } finally {
            try {
                if ( rs != null ) {
                    rs.close();
                }
            } catch ( Exception e ) {
                // what to do here anyway
            }
            try {
                if ( stmt != null ) {
                    stmt.close();
                }
            } catch ( SQLException e ) {
                // what to do here anyway
            }
            releaseConnection( jdbc, conn );
        }
        return fc;
    }

    /**
     *
     * @param datasource
     * @param envelope
     * @return featurecollection loaded from a postgis database
     * @throws Exception
     */
    public static FeatureCollection load( DatabaseDataSource datasource, Envelope envelope )
                            throws Exception {
        return load( datasource, envelope, null );
    }

    private static PreparedStatement createPreparedStatement( DatabaseDataSource datasource, Envelope envelope,
                                                              Connection conn, CoordinateSystem crs, String sql,
                                                              String extraClauses )
                            throws GeometryException, SQLException {
        PreparedStatement stmt;

        String nativeCRS = getSRSCode( crs );
        String envCRS;
        if ( envelope.getCoordinateSystem() != null ) {
            envCRS = getSRSCode( envelope.getCoordinateSystem() );
        } else {
            envCRS = nativeCRS;
        }

        // use the bbox operator (&&) to filter using the spatial index
        PGboxbase box = PGgeometryAdapter.export( envelope );
        Surface surface = GeometryFactory.createSurface( envelope, envelope.getCoordinateSystem() );
        PGgeometry pggeom = PGgeometryAdapter.export( surface, Integer.parseInt( envCRS ) );
        StringBuffer query = new StringBuffer( 1000 );
        query.append( " (" );
        query.append( datasource.getGeometryFieldName() );
        query.append( " && transform(SetSRID( ?, " );
        query.append( envCRS );
        query.append( "), " );
        query.append( nativeCRS );
        query.append( ")) AND intersects(" );
        query.append( datasource.getGeometryFieldName() );
        query.append( ",transform(?, " );
        query.append( nativeCRS );
        query.append( "))" );

        if ( extraClauses != null ) {
            query.append( extraClauses );
        }

        if ( sql.indexOf( "$BBOX" ) == -1 ) {
            if ( sql.trim().toUpperCase().endsWith( " WHERE" ) ) {
                LOG.logDebug( "performed SQL: ", sql + query );
                stmt = conn.prepareStatement( sql + query );
            } else {
                LOG.logDebug( "performed SQL: ", sql + " AND " + query );
                stmt = conn.prepareStatement( sql + " AND " + query );
            }
        } else {
            if ( sql.substring( 0, sql.indexOf( "$BBOX" ) ).trim().toUpperCase().endsWith( " WHERE" ) ) {
                stmt = conn.prepareStatement( sql.replace( "$BBOX", query ) );
            } else {
                stmt = conn.prepareStatement( sql.replace( "$BBOX", " and " + query ) );
            }
            LOG.logDebug( "performed SQL: ", stmt );
        }
        stmt.setObject( 1, box, java.sql.Types.OTHER );
        stmt.setObject( 2, pggeom, java.sql.Types.OTHER );
        return stmt;
    }

    private static String getSRSCode( CoordinateSystem crs ) {
        return crs.getLocalName();
    }

    /**
     *
     * @param geometryFiedName
     * @param rsmd
     * @return {@link FeatureType} created from column names and types
     * @throws SQLException
     */
    private static FeatureType createFeatureType( DatabaseDataSource datasource, String geometryFiedName,
                                                  ResultSetMetaData rsmd )
                            throws SQLException {
        int ccnt = rsmd.getColumnCount();
        QualifiedName name = new QualifiedName( datasource.getName().getLocalName(), namespace );
        PropertyType[] properties = new PropertyType[ccnt];
        for ( int i = 0; i < ccnt; i++ ) {
            QualifiedName propName = new QualifiedName( rsmd.getColumnName( i + 1 ), namespace );
            LOG.logDebug( "propertyname: ", propName );
            int typeCode = getTypeCode( geometryFiedName, rsmd.getColumnName( i + 1 ), rsmd.getColumnType( i + 1 ) );
            properties[i] = FeatureFactory.createSimplePropertyType( propName, typeCode, true );
        }
        return FeatureFactory.createFeatureType( name, false, properties );
    }

    private static int getTypeCode( String geometryFiedName, String columnName, int columnType ) {
        if ( columnName.equalsIgnoreCase( geometryFiedName ) ) {
            return Types.GEOMETRY;
        }
        return columnType;
    }

    private static void releaseConnection( JDBCConnection jdbc, Connection conn ) {
        try {
            DBConnectionPool pool = DBConnectionPool.getInstance();
            pool.releaseConnection( conn, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        } catch ( DBPoolException e ) {
            // what to do here anyway
        }
    }

    private static Connection acquireConnection( JDBCConnection jdbc )
                            throws DBPoolException, SQLException {
        Connection conn;
        DBConnectionPool pool = DBConnectionPool.getInstance();
        conn = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        PGConnection pgConn = (PGConnection) conn;
        pgConn.addDataType( GEOMETRY_DATATYPE_NAME, pgGeometryClass );
        pgConn.addDataType( BOX3D_DATATYPE_NAME, pgBox3dClass );
        return conn;
    }

}
