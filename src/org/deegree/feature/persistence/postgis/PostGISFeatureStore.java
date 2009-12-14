//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.feature.persistence.postgis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.ResultSetIterator;
import org.deegree.crs.CRS;
import org.deegree.feature.Feature;
import org.deegree.feature.Features;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureCoder;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreGMLIdResolver;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.StoredFeatureTypeMetadata;
import org.deegree.feature.persistence.cache.FeatureStoreCache;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.CachedFeatureResultSet;
import org.deegree.feature.persistence.query.CombinedResultSet;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.FilteredFeatureResultSet;
import org.deegree.feature.persistence.query.IteratorResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.query.Query.QueryHint;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.gml.GMLObject;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStore} implementation that uses a PostGIS/PostgreSQL database as backend.
 * 
 * @see FeatureStore
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISFeatureStore implements FeatureStore {

    private static final Logger LOG = LoggerFactory.getLogger( PostGISFeatureStore.class );

    private final ApplicationSchema schema;

    private final Map<QName, StoredFeatureTypeMetadata> ftNameToMd = new HashMap<QName, StoredFeatureTypeMetadata>();

    private final Map<QName, Short> ftNameToFtId = new HashMap<QName, Short>();

    final Map<QName, Envelope> ftNameToBBox = new HashMap<QName, Envelope>();

    private Envelope defaultEnvelope;

    private final String jdbcConnId;

    private final String dbSchema;

    private final CRS storageSRS;

    private PostGISFeatureStoreTransaction activeTransaction;

    private Thread transactionHolder;

    private LockManager lockManager;

    // TODO make this configurable
    private FeatureStoreCache cache = new FeatureStoreCache( 10000 );

    /**
     * Creates a new {@link PostGISFeatureStore} for the given {@link ApplicationSchema}.
     * 
     * @param schema
     *            schema information, must not be <code>null</code>
     * @param jdbcConnId
     *            id of the deegree DB connection pool, must not be <code>null</code>
     * @param dbSchema
     *            name of the database schema, can be <code>null</code> (-> public schema)
     * @param storageSRS
     *            srs used for stored geometries, must not be <code>null</code>
     */
    public PostGISFeatureStore( ApplicationSchema schema, String jdbcConnId, String dbSchema, CRS storageSRS ) {
        this.schema = schema;
        this.jdbcConnId = jdbcConnId;
        this.dbSchema = dbSchema;
        this.storageSRS = storageSRS;
        defaultEnvelope = new GeometryFactory().createEnvelope( -180, -90, 180, 90, CRS.EPSG_4326 );
    }

    @Override
    public FeatureStoreTransaction acquireTransaction()
                            throws FeatureStoreException {

        while ( this.activeTransaction != null ) {
            Thread holder = this.transactionHolder;
            // check if transaction holder variable has (just) been cleared or if the other thread
            // has been killed (avoid deadlocks)
            if ( holder == null || !holder.isAlive() ) {
                this.activeTransaction = null;
                this.transactionHolder = null;
                break;
            }

            try {
                // wait until the transaction holder wakes us, but not longer than 5000
                // milliseconds (as the transaction holder may very rarely get killed without
                // signalling us)
                wait( 5000 );
            } catch ( InterruptedException e ) {
                // nothing to do
            }
        }

        try {
            Connection conn = ConnectionManager.getConnection( jdbcConnId );
            conn.setAutoCommit( false );
            this.activeTransaction = new PostGISFeatureStoreTransaction( this, conn );
        } catch ( SQLException e ) {
            throw new FeatureStoreException( "Unable to acquire JDBC connection for transaction: " + e.getMessage(), e );
        }
        this.transactionHolder = Thread.currentThread();
        return this.activeTransaction;
    }

    @Override
    public void destroy() {
        LOG.debug( "destroy" );
    }

    @Override
    public Envelope getEnvelope( QName ftName ) {
        Envelope env = ftNameToBBox.get( ftName );
        if ( env == null ) {
            env = defaultEnvelope;
        }
        return env;
    }

    /**
     * Sets the envelope for the given feature type.
     * 
     * @param ft
     *            feature type, must not be <code>null</code>
     * @param ftEnv
     *            envelope, must not be <code>null</code> and use EPSG:4326
     */
    void setEnvelope( FeatureType ft, Envelope ftEnv ) {
        ftNameToBBox.put( ft.getName(), ftEnv );
    }

    @Override
    public LockManager getLockManager()
                            throws FeatureStoreException {
        return lockManager;
    }

    @Override
    public StoredFeatureTypeMetadata getMetadata( QName ftName ) {
        return ftNameToMd.get( ftName );
    }

    @Override
    public GMLObject getObjectById( String id )
                            throws FeatureStoreException {

        GMLObject geomOrFeature = cache.get( id );

        if ( geomOrFeature == null ) {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                conn = ConnectionManager.getConnection( jdbcConnId );
                conn.setAutoCommit( false );
                stmt = conn.createStatement();
                rs = stmt.executeQuery( "SELECT binary_object FROM " + qualifyTableName( "gml_objects" )
                                        + " WHERE gml_id='" + id + "'" );
                if ( rs.next() ) {
                    LOG.debug( "Recreating object '" + id + "' from bytea." );
                    geomOrFeature = FeatureCoder.decode( rs.getBinaryStream( 1 ), schema, storageSRS,
                                                         new FeatureStoreGMLIdResolver( this ) );
                    cache.add( geomOrFeature );
                }
            } catch ( Exception e ) {
                String msg = "Error performing query: " + e.getMessage();
                LOG.debug( msg, e );
                throw new FeatureStoreException( msg, e );
            } finally {
                closeSafely( conn, stmt, rs );
            }
        }
        return geomOrFeature;
    }

    @Override
    public ApplicationSchema getSchema() {
        return schema;
    }

    @Override
    public void init()
                            throws FeatureStoreException {
        LOG.debug( "init" );
        // lockManager = new DefaultLockManager( this, "LOCK_DB" );

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection( jdbcConnId );
            stmt = conn.createStatement();
            rs = stmt.executeQuery( "SELECT id,qname,tablename,wgs84bbox FROM " + qualifyTableName( "feature_types" ) );
            while ( rs.next() ) {
                short ftId = rs.getShort( 1 );
                QName ftName = QName.valueOf( rs.getString( 2 ) );
                String tableName = rs.getString( 3 ).trim();
                PGgeometry pgGeom = (PGgeometry) rs.getObject( 4 );
                LOG.debug( "{" + ftId + "," + ftName + "," + tableName + "," + pgGeom + "}" );

                // create feature type metadata (TODO BBOX, configurability)
                FeatureType ft = schema.getFeatureType( ftName );
                if ( ft == null ) {
                    String msg = "Configuration inconsistency. Feature type '" + ftName
                                 + "' is not defined in the application schemas of the feature store.";
                    throw new FeatureStoreException( msg );
                }
                if ( ft.isAbstract() ) {
                    String msg = "Configuration inconsistency. Feature type '" + ftName
                                 + "' is abstract according to the application schemas of the feature store.";
                    throw new FeatureStoreException( msg );
                }
                String title = ftName.toString() + " served by PostGISFeatureStore";
                String desc = ftName.toString() + " served by PostGISFeatureStore";
                StoredFeatureTypeMetadata ftMd = new StoredFeatureTypeMetadata( ft, this, title, desc, storageSRS );
                ftNameToMd.put( ftName, ftMd );
                ftNameToFtId.put( ftName, ftId );

                if ( pgGeom != null ) {
                    double[] min = new double[] { 180.0, 90.0 };
                    double[] max = new double[] { -180.0, -90.0 };
                    if ( pgGeom.getGeoType() == org.postgis.Geometry.POINT ) {
                        Point point = (Point) pgGeom.getGeometry();
                        min[0] = point.x;
                        min[1] = point.y;
                        max[0] = point.x;
                        max[1] = point.y;
                    } else if ( pgGeom.getGeoType() == org.postgis.Geometry.LINESTRING ) {
                        LineString line = (LineString) pgGeom.getGeometry();
                        min[0] = line.getFirstPoint().x;
                        min[1] = line.getFirstPoint().y;
                        max[0] = line.getLastPoint().x;
                        max[1] = line.getLastPoint().y;
                    } else if ( pgGeom.getGeoType() == org.postgis.Geometry.POLYGON ) {
                        Polygon polygon = (Polygon) pgGeom.getGeometry();
                        for ( int i = 0; i < polygon.numPoints(); i++ ) {
                            Point point = polygon.getPoint( i );
                            if ( min[0] > point.x ) {
                                min[0] = point.x;
                            }
                            if ( min[1] > point.y ) {
                                min[1] = point.y;
                            }
                            if ( max[0] < point.x ) {
                                max[0] = point.x;
                            }
                            if ( max[1] < point.y ) {
                                max[1] = point.y;
                            }
                        }
                    } else {
                        throw new RuntimeException();
                    }
                    Envelope env = new GeometryFactory().createEnvelope( min, max, CRS.EPSG_4326 );
                    ftNameToBBox.put( ftName, env );
                }
            }
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new FeatureStoreException( e.getMessage(), e );
        } finally {
            closeSafely( conn, stmt, null );
        }
    }

    /**
     * Returns the given table name qualified with the db schema.
     * 
     * @param tableName
     *            name of the table to be qualified
     * @return dbSchema + "." + tableName, or tableName if dbSchema is <code>null</code>
     */
    String qualifyTableName( String tableName ) {
        if ( dbSchema == null ) {
            return tableName;
        }
        return dbSchema + "." + tableName;
    }

    private void closeSafely( Connection conn, Statement stmt, ResultSet rs ) {
        if ( rs != null ) {
            try {
                rs.close();
            } catch ( SQLException e ) {
                LOG.warn( e.getMessage(), e );
            }
        }
        if ( stmt != null ) {
            try {
                stmt.close();
            } catch ( SQLException e ) {
                LOG.warn( e.getMessage(), e );
            }
        }
        if ( conn != null ) {
            try {
                conn.close();
            } catch ( SQLException e ) {
                LOG.warn( e.getMessage(), e );
            }
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public FeatureResultSet query( Query query )
                            throws FeatureStoreException, FilterEvaluationException {

        if ( query.getTypeNames() == null || query.getTypeNames().length > 1 ) {
            String msg = "Join queries between multiple feature types are currently not supported.";
            throw new UnsupportedOperationException( msg );
        }

        FeatureResultSet rs = null;
        Filter filter = query.getFilter();
        if ( query.getTypeNames().length == 1 && ( filter == null || filter instanceof OperatorFilter ) ) {
            QName ftName = query.getTypeNames()[0].getFeatureTypeName();
            FeatureType ft = schema.getFeatureType( ftName );
            if ( ft == null ) {
                String msg = "Feature type '" + ftName + "' is not served by this feature store.";
                throw new FeatureStoreException( msg );
            }
            rs = queryByOperatorFilter( ftName, (OperatorFilter) filter,
                                        (Envelope) query.getHint( QueryHint.HINT_LOOSE_BBOX ) );
            if ( filter != null ) {
                rs = new FilteredFeatureResultSet( rs, filter );
            }
        } else {
            // must be an id filter based query
            if ( query.getFilter() == null || !( query.getFilter() instanceof IdFilter ) ) {
                String msg = "Invalid query. If no type names are specified, it must contain an IdFilter.";
                throw new FilterEvaluationException( msg );
            }
            rs = queryByIdFilter( (IdFilter) filter );
        }

        // sort features
        SortProperty[] sortCrit = query.getSortProperties();
        if ( sortCrit != null ) {
            rs = new CachedFeatureResultSet( Features.sortFc( rs.toCollection(), sortCrit ) );
        }
        return rs;
    }

    private FeatureResultSet queryByIdFilter( IdFilter filter )
                            throws FeatureStoreException {

        FeatureResultSet result = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection( jdbcConnId );

            // create temp table with ids
            stmt = conn.createStatement();
            stmt.executeUpdate( "CREATE TEMP TABLE temp_ids (fid TEXT)" );
            stmt.close();

            // fill temp table
            PreparedStatement insertFid = conn.prepareStatement( "INSERT INTO temp_ids (fid) VALUES (?)" );
            for ( String fid : filter.getMatchingIds() ) {
                insertFid.setString( 1, fid );
                insertFid.addBatch();
            }
            insertFid.executeBatch();

            stmt = conn.createStatement();
            rs = stmt.executeQuery( "SELECT gml_id,binary_object FROM " + qualifyTableName( "gml_objects" )
                                    + " A, temp_ids B WHERE A.gml_id=b.fid" );

            result = new IteratorResultSet( new FeatureResultSetIterator( rs, conn, stmt,
                                                                          new FeatureStoreGMLIdResolver( this ) ) );
        } catch ( Exception e ) {
            closeSafely( conn, stmt, rs );
            String msg = "Error performing query: " + e.getMessage();
            LOG.debug( msg, e );
            throw new FeatureStoreException( msg, e );
        } finally {
            if ( conn != null ) {
                try {
                    // drop temp table
                    stmt = conn.createStatement();
                    stmt.executeUpdate( "DROP TABLE temp_ids " );
                    stmt.close();
                } catch ( SQLException e ) {
                    String msg = "Error dropping temp table.";
                    LOG.debug( msg, e );
                }
            }
        }
        return result;
    }

    private FeatureResultSet queryByOperatorFilter( QName ftName, OperatorFilter filter, Envelope looseBBox )
                            throws FeatureStoreException {

        FeatureResultSet result = null;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection( jdbcConnId );
            stmt = conn.prepareStatement( "SELECT gml_id,binary_object FROM " + qualifyTableName( "gml_objects" )
                                          + " WHERE ft_type=? AND gml_bounded_by && ?" );
            stmt.setShort( 1, ftNameToFtId.get( ftName ) );
            stmt.setObject( 2, toPGPolygon( (Envelope) getCompatibleGeometry( looseBBox, storageSRS ), -1 ) );
            rs = stmt.executeQuery();
            result = new IteratorResultSet( new FeatureResultSetIterator( rs, conn, stmt,
                                                                          new FeatureStoreGMLIdResolver( this ) ) );
        } catch ( Exception e ) {
            closeSafely( conn, stmt, rs );
            String msg = "Error performing query: " + e.getMessage();
            LOG.debug( msg, e );
            throw new FeatureStoreException( msg, e );
        }
        return result;
    }

    private FeatureResultSet queryMultipleFts( Query[] queries, Envelope looseBBox )
                            throws FeatureStoreException {

        FeatureResultSet result = null;

        short[] ftId = new short[queries.length];
        for ( int i = 0; i < ftId.length; i++ ) {
            Query query = queries[i];
            if ( query.getTypeNames() == null || query.getTypeNames().length > 1 ) {
                String msg = "Join queries between multiple feature types are currently not supported.";
                throw new UnsupportedOperationException( msg );
            }
            ftId[i] = getFtId( query.getTypeNames()[0].getFeatureTypeName() );
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection( jdbcConnId );
            StringBuffer sql = new StringBuffer( "SELECT gml_id,binary_object FROM " + qualifyTableName( "gml_objects" )
                                                 + " WHERE gml_bounded_by && ? AND ft_type IN(?" );
            for ( int i = 1; i < ftId.length; i++ ) {
                sql.append( ",?" );
            }
            sql.append( ") ORDER BY position('['||ft_type||']' IN ?)" );
            stmt = conn.prepareStatement( sql.toString() );
            stmt.setObject( 1, toPGPolygon( (Envelope) getCompatibleGeometry( looseBBox, storageSRS ), -1 ) );
            StringBuffer orderString = new StringBuffer();
            for ( int i = 0; i < ftId.length; i++ ) {
                stmt.setShort( i + 2, ftId[i] );
                orderString.append( "[" );
                orderString.append( "" + ftId[i] );
                orderString.append( "]" );
            }
            stmt.setString( ftId.length + 2, orderString.toString() );
            LOG.debug( "Query {}", stmt );

            rs = stmt.executeQuery();
            result = new IteratorResultSet( new FeatureResultSetIterator( rs, conn, stmt,
                                                                          new FeatureStoreGMLIdResolver( this ) ) );
        } catch ( Exception e ) {
            closeSafely( conn, stmt, rs );
            String msg = "Error performing query: " + e.getMessage();
            LOG.debug( msg, e );
            throw new FeatureStoreException( msg, e );
        }
        return result;
    }

    private FeatureResultSet queryMultipleFts2( Query[] queries, Envelope looseBBox )
                            throws FeatureStoreException {

        FeatureResultSet result = null;

        short[] ftId = new short[queries.length];
        for ( int i = 0; i < ftId.length; i++ ) {
            Query query = queries[i];
            if ( query.getTypeNames() == null || query.getTypeNames().length > 1 ) {
                String msg = "Join queries between multiple feature types are currently not supported.";
                throw new UnsupportedOperationException( msg );
            }
            ftId[i] = getFtId( query.getTypeNames()[0].getFeatureTypeName() );
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection( jdbcConnId );
            StringBuffer sql = new StringBuffer();

            if (queries.length == 1) {
                sql.append( "SELECT gml_id,binary_object FROM " + qualifyTableName( "gml_objects" )
                            + " WHERE gml_bounded_by && ? AND ft_type=?" );                
            } else {
                sql.append( "(SELECT gml_id,binary_object FROM " + qualifyTableName( "gml_objects" )
                            + " WHERE gml_bounded_by && ? AND ft_type=?)" );
                for ( int i = 1; i < queries.length; i++ ) {
                    sql.append( "UNION (SELECT gml_id,binary_object FROM " + qualifyTableName( "gml_objects" )
                                + " WHERE gml_bounded_by && ? AND ft_type=?)" );                    
                }                
            }
            stmt = conn.prepareStatement( sql.toString() );
            int i = 1;
            for ( Query query : queries ) {
                stmt.setObject( i++, toPGPolygon( (Envelope) getCompatibleGeometry( looseBBox, storageSRS ), -1 ) );
                stmt.setShort( i++, getFtId( query.getTypeNames()[0].getFeatureTypeName() ) );
                StringBuffer orderString = new StringBuffer();
                stmt.setString( ftId.length + 2, orderString.toString() );
            }
            LOG.info( "Query {}", stmt );
            rs = stmt.executeQuery();
            result = new IteratorResultSet( new FeatureResultSetIterator( rs, conn, stmt,
                                                                          new FeatureStoreGMLIdResolver( this ) ) );
        } catch ( Exception e ) {
            closeSafely( conn, stmt, rs );
            String msg = "Error performing query: " + e.getMessage();
            LOG.debug( msg, e );
            throw new FeatureStoreException( msg, e );
        }
        return result;
    }

    @Override
    public FeatureResultSet query( final Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {

        // check for most common case: multiple featuretypes, same bbox (WMS), no filter
        boolean wmsStyleQuery = false;
        Envelope env = (Envelope) queries[0].getHint( QueryHint.HINT_LOOSE_BBOX );
        if ( queries[0].getFilter() == null && queries[0].getSortProperties() == null ) {
            wmsStyleQuery = true;
            for ( int i = 1; i < queries.length; i++ ) {
                Envelope queryBBox = (Envelope) queries[i].getHint( QueryHint.HINT_LOOSE_BBOX );
                if ( queryBBox != env && queries[i].getFilter() != null && queries[i].getSortProperties() != null ) {
                    wmsStyleQuery = false;
                    break;
                }
            }
        }

        if ( wmsStyleQuery ) {
            return queryMultipleFts( queries, env );
        }

        Iterator<FeatureResultSet> rsIter = new Iterator<FeatureResultSet>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < queries.length;
            }

            @Override
            public FeatureResultSet next() {
                if ( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                FeatureResultSet rs;
                try {
                    rs = query( queries[i++] );
                } catch ( Exception e ) {
                    throw new RuntimeException( e.getMessage(), e );
                }
                return rs;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return new CombinedResultSet( rsIter );
    }

    @Override
    public int queryHits( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO
        return query( query ).toCollection().size();
    }

    @Override
    public int queryHits( final Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO
        return query( queries ).toCollection().size();
    }

    private PropertyName findGeoProp( FeatureType ft )
                            throws FilterEvaluationException {

        PropertyName propName = null;

        // TODO what about geometry properties on subfeature levels
        for ( PropertyType<?> pt : ft.getPropertyDeclarations() ) {
            if ( pt instanceof GeometryPropertyType ) {
                propName = new PropertyName( pt.getName() );
                break;
            }
        }

        if ( propName == null ) {
            String msg = "Cannot perform BBox query: requested feature type ('" + ft.getName()
                         + "') does not have a geometry property.";
            throw new FilterEvaluationException( msg );
        }
        return propName;
    }

    private Geometry getCompatibleGeometry( Geometry literal, CRS crs )
                            throws FilterEvaluationException {

        Geometry transformedLiteral = literal;
        CRS literalCRS = literal.getCoordinateSystem();
        if ( literal != null && literalCRS != null && !( crs.equals( literalCRS ) ) ) {
            LOG.debug( "Need transformed literal geometry for evaluation: " + literalCRS.getName() + " -> "
                       + crs.getName() );
            try {
                GeometryTransformer transformer = new GeometryTransformer( crs.getWrappedCRS() );
                transformedLiteral = transformer.transform( literal );
            } catch ( Exception e ) {
                throw new FilterEvaluationException( e.getMessage() );
            }
        }
        return transformedLiteral;
    }

    /**
     * Returns the transaction to the datastore. This makes the transaction available to other clients again (via
     * {@link #acquireTransaction()}.
     * <p>
     * The transaction should be terminated, i.e. commit() or rollback() must have been called before.
     * 
     * @param ta
     *            the PostGISFeatureStoreTransaction to be returned
     * @throws FeatureStoreException
     */
    void releaseTransaction( PostGISFeatureStoreTransaction ta )
                            throws FeatureStoreException {
        if ( ta.getStore() != this ) {
            String msg = Messages.getMessage( "TA_NOT_OWNER" );
            throw new FeatureStoreException( msg );
        }
        if ( ta != this.activeTransaction ) {
            String msg = Messages.getMessage( "TA_NOT_ACTIVE" );
            throw new FeatureStoreException( msg );
        }
        this.activeTransaction = null;
        this.transactionHolder = null;
        // notifyAll();
        try {
            ta.getConnection().close();
        } catch ( SQLException e ) {
            throw new FeatureStoreException( "Error closing connection: " + e.getMessage() );
        }
    }

    short getFtId( QName ftName ) {
        return ftNameToFtId.get( ftName );
    }

    PGgeometry toPGPolygon( Envelope envelope, int srid ) {
        PGgeometry pgGeometry = null;
        if ( envelope != null ) {
            double minX = envelope.getMin().get0();
            double minY = envelope.getMin().get1();
            double maxX = envelope.getMax().get0();
            double maxY = envelope.getMax().get1();
            if ( envelope.getMin().equals( envelope.getMax() ) ) {
                Point point = new Point( envelope.getMin().get0(), envelope.getMin().get1() );
                // TODO
                point.setSrid( srid );
                pgGeometry = new PGgeometry( point );
            } else if ( minX == maxX || minY == maxY ) {
                LineString line = new LineString( new Point[] { new Point( minX, minY ), new Point( maxX, maxY ) } );
                // TODO
                line.setSrid( srid );
                pgGeometry = new PGgeometry( line );
            } else {
                Point[] points = new Point[] { new Point( minX, minY ), new Point( maxX, minY ),
                                              new Point( maxX, maxY ), new Point( minX, maxY ), new Point( minX, minY ) };
                LinearRing outer = new LinearRing( points );
                Polygon polygon = new Polygon( new LinearRing[] { outer } );
                // TODO
                polygon.setSrid( srid );
                pgGeometry = new PGgeometry( polygon );
            }
        }
        return pgGeometry;
    }

    private class FeatureResultSetIterator extends ResultSetIterator<Feature> {

        private final FeatureStoreGMLIdResolver resolver;

        public FeatureResultSetIterator( ResultSet rs, Connection conn, Statement stmt,
                                         FeatureStoreGMLIdResolver resolver ) {
            super( rs, conn, stmt );
            this.resolver = resolver;
        }

        @SuppressWarnings("synthetic-access")
        @Override
        protected Feature createElement( ResultSet rs )
                                throws SQLException {
            Feature feature = null;
            try {
                String gml_id = rs.getString( 1 );
                feature = (Feature) cache.get( gml_id );
                if ( feature == null ) {
                    LOG.debug( "Cache miss. Recreating object '" + gml_id + "' from blob." );
                    feature = FeatureCoder.decode( rs.getBinaryStream( 2 ), schema, new CRS( "EPSG:31466" ), resolver );
                    cache.add( feature );
                } else {
                    LOG.debug( "Cache hit." );
                }
            } catch ( Exception e ) {
                String msg = "Cannot recreate feature from result set: " + e.getMessage();
                throw new SQLException( msg, e );
            }
            return feature;
        }
    }
}
