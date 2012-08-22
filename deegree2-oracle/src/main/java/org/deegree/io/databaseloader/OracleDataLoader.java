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

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.DBPoolException;
import org.deegree.io.JDBCConnection;
import org.deegree.io.datastore.sql.oracle.JGeometryAdapter;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
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

/**
 * class for loading data as feature collection from a postgis database
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OracleDataLoader {

    private static final ILogger LOG = LoggerFactory.getLogger( OracleDataLoader.class );

    private static URI namespace;
    static {
        try {
            namespace = new URI( "http://www.deegree.org/database" );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }
    }

    /**
     * @param datasource
     * @param envelope
     * @return the feature collection directly from the db
     * @throws Exception
     */
    public static FeatureCollection load( DatabaseDataSource datasource, Envelope envelope )
                            throws Exception {
        return load( datasource, envelope, null );
    }

    /**
     * @param datasource
     * @param envelope
     * @param sql
     * @return the fc from the db
     * @throws Exception
     */
    public static FeatureCollection load( DatabaseDataSource datasource, Envelope envelope, String sql )
                            throws Exception {
        return load( datasource, envelope, sql, null );
    }

    /**
     * 
     * @param datasource
     * @param envelope
     * @param sql
     * @param extraClauses
     * @return featurecollection loaded from a postgis database
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
                        JGeometry jGeometry = JGeometry.load( (STRUCT) value );
                        value = JGeometryAdapter.wrap( jGeometry, crs );
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

    private static PreparedStatement createPreparedStatement( DatabaseDataSource datasource, Envelope envelope,
                                                              Connection conn, CoordinateSystem crs, String sql,
                                                              String extraClauses )
                            throws GeometryException, SQLException, IllegalArgumentException,
                            CRSTransformationException {
        PreparedStatement stmt;

        String nativeCRS = crs.getLocalName();
        String envCRS = nativeCRS;
        if ( envelope.getCoordinateSystem() != null ) {
            envCRS = envelope.getCoordinateSystem().getLocalName();
        }

        // use the bbox operator (&&) to filter using the spatial index
        if ( !( nativeCRS.equals( envCRS ) ) ) {
            GeoTransformer gt = new GeoTransformer( crs );
            envelope = gt.transform( envelope, envelope.getCoordinateSystem() );
        }
        Surface surface = GeometryFactory.createSurface( envelope, envelope.getCoordinateSystem() );
        JGeometry jgeom = JGeometryAdapter.export( surface, Integer.parseInt( nativeCRS ) );
        StringBuffer query = new StringBuffer( 1000 );
        query.append( " MDSYS.SDO_RELATE(" );
        query.append( datasource.getGeometryFieldName() );
        query.append( ',' );
        query.append( '?' );
        query.append( ",'MASK=ANYINTERACT QUERYTYPE=WINDOW')='TRUE'" );

        if ( extraClauses != null ) {
            query.append( extraClauses );
        }

        if ( sql.trim().toUpperCase().endsWith( " WHERE" ) ) {
            LOG.logDebug( "performed SQL: ", sql );
            stmt = conn.prepareStatement( sql + query );
        } else {
            LOG.logDebug( "performed SQL: ", sql + " AND " + query );
            stmt = conn.prepareStatement( sql + " AND " + query );
        }

        LOG.logDebug( "Converting JGeometry to STRUCT." );
        STRUCT struct = JGeometry.store( jgeom, conn );
        stmt.setObject( 1, struct, java.sql.Types.STRUCT );
        return stmt;
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
                            throws DBPoolException {
        DBConnectionPool pool = DBConnectionPool.getInstance();
        return pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
    }

}
