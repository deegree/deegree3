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

import static org.deegree.commons.utils.JDBCUtils.close;
import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.feature.persistence.query.Query.QueryHint.HINT_LOOSE_BBOX;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.ResultSetIterator;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.primitive.SQLValueMangler;
import org.deegree.cs.CRS;
import org.deegree.feature.Feature;
import org.deegree.feature.Features;
import org.deegree.feature.persistence.BlobCodec;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreGMLIdResolver;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.cache.FeatureStoreCache;
import org.deegree.feature.persistence.cache.SimpleFeatureStoreCache;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.mapping.BBoxTableMapping;
import org.deegree.feature.persistence.mapping.BlobMapping;
import org.deegree.feature.persistence.mapping.DBField;
import org.deegree.feature.persistence.mapping.FeatureTypeMapping;
import org.deegree.feature.persistence.mapping.IdAnalysis;
import org.deegree.feature.persistence.mapping.Join;
import org.deegree.feature.persistence.mapping.JoinChain;
import org.deegree.feature.persistence.mapping.MappedApplicationSchema;
import org.deegree.feature.persistence.mapping.MappingExpression;
import org.deegree.feature.persistence.mapping.id.FIDMapping;
import org.deegree.feature.persistence.mapping.property.GeometryMapping;
import org.deegree.feature.persistence.mapping.property.Mapping;
import org.deegree.feature.persistence.query.CombinedResultSet;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.FilteredFeatureResultSet;
import org.deegree.feature.persistence.query.IteratorResultSet;
import org.deegree.feature.persistence.query.MemoryFeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.sql.PropertyNameMapping;
import org.deegree.filter.sql.expression.SQLLiteral;
import org.deegree.filter.sql.postgis.PostGISWhereBuilder;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.gml.GMLObject;
import org.deegree.gml.GMLReferenceResolver;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.PGboxbase;
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
public class PostGISFeatureStore implements SQLFeatureStore {

    static final Logger LOG = LoggerFactory.getLogger( PostGISFeatureStore.class );

    private final MappedApplicationSchema schema;

    private final BlobMapping blobMapping;

    private final LockManager lockManager;

    private final TransactionManager taManager;

    private final FeatureStoreGMLIdResolver resolver = new FeatureStoreGMLIdResolver( this );

    private final String jdbcConnId;

    // TODO make this configurable
    private FeatureStoreCache cache = new SimpleFeatureStoreCache( 10000 );

    // if true, use old-style for spatial predicates (intersects instead of ST_Intersecs)
    private boolean useLegacyPredicates;

    private Map<String, String> nsContext;

    /**
     * Creates a new {@link PostGISFeatureStore} for the given {@link ApplicationSchema}.
     * 
     * @param schema
     *            schema information, must not be <code>null</code>
     * @param jdbcConnId
     *            id of the deegree DB connection pool, must not be <code>null</code>
     */
    PostGISFeatureStore( MappedApplicationSchema schema, String jdbcConnId ) {
        this.schema = schema;
        this.blobMapping = schema.getBlobMapping();
        this.jdbcConnId = jdbcConnId;
        lockManager = null;
        taManager = new TransactionManager( this, jdbcConnId );
    }

    @Override
    public String getConnId() {
        return jdbcConnId;
    }

    /**
     * Returns the relational mapping for the given feature type name.
     * 
     * @param ftName
     *            name of the feature type
     * @return relational mapping for the feature type, may be <code>null</code> (no relational mapping)
     */
    FeatureTypeMapping getMapping( QName ftName ) {
        return schema.getMapping( ftName );
    }

    @Override
    public FeatureStoreTransaction acquireTransaction()
                            throws FeatureStoreException {
        return taManager.acquireTransaction();
    }

    @Override
    public void destroy() {
        LOG.debug( "destroy" );
    }

    @Override
    public Envelope getEnvelope( QName ftName )
                            throws FeatureStoreException {
        Envelope env = null;
        FeatureType ft = schema.getFeatureType( ftName );
        if ( ft != null ) {
            // TODO bbox caching
            // BBoxTableMapping bboxMapping = schema.getBBoxMapping();
            BlobMapping blobMapping = schema.getBlobMapping();
            if ( blobMapping != null ) {
                env = getEnvelope( ft.getName(), blobMapping );
            } else {
                env = getEnvelope( schema.getMapping( ft.getName() ) );
            }
        }
        return env;
    }

    private Envelope getEnvelope( FeatureTypeMapping ftMapping )
                            throws FeatureStoreException {

        LOG.trace( "Determining BBOX for feature type '{}' (relational mode)", ftMapping.getFeatureType() );

        String column = null;
        FeatureType ft = schema.getFeatureType( ftMapping.getFeatureType() );
        GeometryPropertyType pt = ft.getDefaultGeometryPropertyDeclaration();
        if ( pt == null ) {
            return null;
        }
        GeometryMapping propMapping = (GeometryMapping) ftMapping.getMapping( pt.getName() );
        MappingExpression me = propMapping.getMapping();
        if ( me == null || !( me instanceof DBField ) ) {
            String msg = "Cannot determine BBOX for feature type '" + ft.getName() + "' (relational mode).";
            LOG.warn( msg );
            return null;
        }
        column = ( (DBField) me ).getColumn();

        Envelope env = null;
        StringBuilder sql = new StringBuilder( "SELECT " );
        if ( useLegacyPredicates ) {
            sql.append( "extent" );
        } else {
            sql.append( "ST_Extent" );
        }
        sql.append( "(" );
        sql.append( column );
        sql.append( ")::BOX2D FROM " );
        sql.append( ftMapping.getFtTable() );

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection( jdbcConnId );
            stmt = conn.createStatement();
            rs = stmt.executeQuery( sql.toString() );
            rs.next();
            PGboxbase pgBox = (PGboxbase) rs.getObject( 1 );
            if ( pgBox != null ) {
                CRS crs = propMapping.getCRS();
                org.deegree.geometry.primitive.Point min = getPoint( pgBox.getLLB(), crs );
                org.deegree.geometry.primitive.Point max = getPoint( pgBox.getURT(), crs );
                env = new DefaultEnvelope( null, crs, null, min, max );
            }
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new FeatureStoreException( e.getMessage(), e );
        } finally {
            close( rs, stmt, conn, LOG );
        }
        return env;
    }

    private Envelope getEnvelope( QName ftName, BBoxTableMapping bboxMapping )
                            throws FeatureStoreException {

        LOG.trace( "Determining BBOX for feature type '{}' (BBOX table mode)", ftName );

        Envelope env = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection( jdbcConnId );
            stmt = conn.createStatement();
            StringBuilder sql = new StringBuilder( "SELECT Box2D(" );
            sql.append( bboxMapping.getBBoxColumn() );
            sql.append( ") FROM " );
            sql.append( bboxMapping.getTable() );
            sql.append( " WHERE " );
            sql.append( bboxMapping.getFTNameColumn() );
            sql.append( "='" );
            sql.append( ftName.toString() );
            sql.append( "'" );
            rs = stmt.executeQuery( sql.toString() );
            if ( rs.next() ) {
                PGboxbase pgBox = (PGboxbase) rs.getObject( 1 );
                if ( pgBox != null ) {
                    CRS crs = bboxMapping.getCRS();
                    org.deegree.geometry.primitive.Point min = getPoint( pgBox.getLLB(), crs );
                    org.deegree.geometry.primitive.Point max = getPoint( pgBox.getURT(), crs );
                    env = new DefaultEnvelope( null, bboxMapping.getCRS(), null, min, max );
                }
            }
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new FeatureStoreException( e.getMessage(), e );
        } finally {
            close( rs, stmt, conn, LOG );
        }
        return env;
    }

    private Envelope getEnvelope( QName ftName, BlobMapping blobMapping )
                            throws FeatureStoreException {

        LOG.debug( "Determining BBOX for feature type '{}' (BLOB mode)", ftName );

        int ftId = getFtId( ftName );
        String column = blobMapping.getBBoxColumn();

        Envelope env = null;
        StringBuilder sql = new StringBuilder( "SELECT " );
        if ( useLegacyPredicates ) {
            sql.append( "extent" );
        } else {
            sql.append( "ST_Extent" );
        }
        sql.append( "(" );
        sql.append( column );
        sql.append( ")::BOX2D FROM " );
        sql.append( blobMapping.getTable() );
        sql.append( " WHERE " );
        sql.append( blobMapping.getTypeColumn() );
        sql.append( "=" );
        sql.append( ftId );

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection( jdbcConnId );
            stmt = conn.createStatement();
            rs = stmt.executeQuery( sql.toString() );
            rs.next();
            PGboxbase pgBox = (PGboxbase) rs.getObject( 1 );
            if ( pgBox != null ) {
                CRS crs = blobMapping.getCRS();
                org.deegree.geometry.primitive.Point min = getPoint( pgBox.getLLB(), crs );
                org.deegree.geometry.primitive.Point max = getPoint( pgBox.getURT(), crs );
                env = new DefaultEnvelope( null, blobMapping.getCRS(), null, min, max );
            }
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new FeatureStoreException( e.getMessage(), e );
        } finally {
            close( rs, stmt, conn, LOG );
        }
        return env;
    }

    private org.deegree.geometry.primitive.Point getPoint( org.postgis.Point p, CRS crs ) {
        double[] coords = new double[p.getDimension()];
        coords[0] = p.getX();
        coords[1] = p.getY();
        if ( p.getDimension() > 2 ) {
            coords[2] = p.getZ();
        }
        return new DefaultPoint( null, crs, null, coords );
    }

    @Override
    public LockManager getLockManager()
                            throws FeatureStoreException {
        return lockManager;
    }

    @Override
    public GMLObject getObjectById( String id )
                            throws FeatureStoreException {

        GMLObject geomOrFeature = cache.get( id );

        if ( geomOrFeature == null ) {
            if ( schema.getBlobMapping() != null ) {
                geomOrFeature = getObjectByIdBlob( id, schema.getBlobMapping() );
            } else {
                geomOrFeature = getObjectByIdRelational( id );
            }
        }
        return geomOrFeature;
    }

    private GMLObject getObjectByIdRelational( String id )
                            throws FeatureStoreException {

        GMLObject result = null;

        IdAnalysis idAnalysis = schema.analyzeId( id );
        if ( !idAnalysis.isFid() ) {
            String msg = "Fetching of geometries by id (relational mode) is not implemented yet.";
            throw new UnsupportedOperationException( msg );
        }

        FeatureType ft = idAnalysis.getFeatureType();
        FeatureTypeMapping mapping = schema.getMapping( ft.getName() );

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuilder sql = new StringBuilder( "SELECT " );
            sql.append( mapping.getFidMapping().getColumn() );
            for ( PropertyType pt : ft.getPropertyDeclarations() ) {
                // append every (mapped) property to SELECT list
                // TODO columns in related tables with 1:1 relation
                Mapping mapping2 = mapping.getMapping( pt.getName() );
                MappingExpression column = mapping2 == null ? null : mapping2.getMapping();
                if ( column != null ) {
                    sql.append( ',' );
                    if ( column instanceof JoinChain ) {
                        JoinChain jc = (JoinChain) column;
                        sql.append( jc.getFields().get( 0 ) );
                    } else {
                        if ( pt instanceof SimplePropertyType ) {
                            sql.append( column );
                        } else if ( pt instanceof GeometryPropertyType ) {
                            if ( useLegacyPredicates ) {
                                sql.append( "AsBinary(" );
                            } else {
                                sql.append( "ST_AsBinary(" );
                            }
                            sql.append( column );
                            sql.append( ')' );
                        } else if ( pt instanceof FeaturePropertyType ) {
                            sql.append( column );
                        } else {
                            LOG.warn( "Skipping property '" + pt.getName() + "' -- type '" + pt.getClass()
                                      + "' not handled in PostGISFeatureStore#getObjectByIdRelational()." );
                        }
                    }
                }
            }
            sql.append( " FROM " );
            sql.append( mapping.getFtTable() );
            sql.append( " WHERE " );
            sql.append( mapping.getFidMapping().getColumn() );
            sql.append( "=?" );

            LOG.debug( "Preparing SELECT: " + sql );

            conn = ConnectionManager.getConnection( jdbcConnId );
            stmt = conn.prepareStatement( sql.toString() );

            // TODO proper SQL type handling
            stmt.setInt( 1, Integer.parseInt( idAnalysis.getIdKernel() ) );

            rs = stmt.executeQuery();
            if ( rs.next() ) {
                result = new FeatureBuilderRelational( this, ft, mapping, conn ).buildFeature( rs );
            }
        } catch ( Exception e ) {
            String msg = "Error retrieving object by id (relational mode): " + e.getMessage();
            LOG.error( msg, e );
            throw new FeatureStoreException( msg, e );
        } finally {
            close( rs, stmt, conn, LOG );
        }
        return result;
    }

    private GMLObject getObjectByIdBlob( String id, BlobMapping blobMapping )
                            throws FeatureStoreException {

        GMLObject geomOrFeature = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuilder sql = new StringBuilder( "SELECT " );
            sql.append( blobMapping.getDataColumn() );
            sql.append( " FROM " );
            sql.append( blobMapping.getTable() );
            sql.append( " WHERE " );
            sql.append( blobMapping.getGMLIdColumn() );
            sql.append( "=?" );

            conn = ConnectionManager.getConnection( jdbcConnId );
            stmt = conn.prepareStatement( sql.toString() );
            stmt.setString( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                LOG.debug( "Recreating object '" + id + "' from bytea." );
                BlobCodec codec = blobMapping.getCodec();
                geomOrFeature = codec.decode( rs.getBinaryStream( 1 ), getNamespaceContext(), schema,
                                              blobMapping.getCRS(), new FeatureStoreGMLIdResolver( this ) );
                cache.add( geomOrFeature );
            }
        } catch ( Exception e ) {
            String msg = "Error retrieving object by id (BLOB mode): " + e.getMessage();
            LOG.debug( msg, e );
            throw new FeatureStoreException( msg, e );
        } finally {
            close( rs, stmt, conn, LOG );
        }
        return geomOrFeature;
    }

    @Override
    public MappedApplicationSchema getSchema() {
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
            String version = determinePostGISVersion( conn );
            if ( version.startsWith( "0." ) || version.startsWith( "1.0" ) || version.startsWith( "1.1" )
                 || version.startsWith( "1.2" ) ) {
                LOG.debug( "PostGIS version is " + version + " -- using legacy (pre-SQL-MM) predicates." );
                useLegacyPredicates = true;
            } else {
                LOG.debug( "PostGIS version is " + version + " -- using modern (SQL-MM) predicates." );
            }
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new FeatureStoreException( e.getMessage(), e );
        } finally {
            close( rs, stmt, conn, LOG );
        }
    }

    private String determinePostGISVersion( Connection conn ) {
        String version = "1.0";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery( "SELECT postgis_version()" );
            rs.next();
            String postGISVersion = rs.getString( 1 );
            version = postGISVersion.split( " " )[0];
            LOG.debug( "PostGIS version: {}", version );
        } catch ( Exception e ) {
            LOG.warn( "Could not determine PostGIS version: {} -- defaulting to 1.0.0", e.getMessage() );
        }
        return version;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public FeatureResultSet query( Query query )
                            throws FeatureStoreException, FilterEvaluationException {

        if ( query.getTypeNames() == null || query.getTypeNames().length > 1 ) {
            String msg = "Join queries between multiple feature types are currently not supported by the PostGISFeatureStore.";
            throw new UnsupportedOperationException( msg );
        }

        FeatureResultSet result = null;
        Filter filter = query.getFilter();

        if ( query.getTypeNames().length == 1 && ( filter == null || filter instanceof OperatorFilter ) ) {
            QName ftName = query.getTypeNames()[0].getFeatureTypeName();
            FeatureType ft = schema.getFeatureType( ftName );
            if ( ft == null ) {
                String msg = "Feature type '" + ftName + "' is not served by this feature store.";
                throw new FeatureStoreException( msg );
            }
            result = queryByOperatorFilter( query, ftName, (OperatorFilter) filter );
        } else {
            // must be an id filter based query
            if ( query.getFilter() == null || !( query.getFilter() instanceof IdFilter ) ) {
                String msg = "Invalid query. If no type names are specified, it must contain an IdFilter.";
                throw new FilterEvaluationException( msg );
            }
            result = queryByIdFilter( (IdFilter) filter, query.getSortProperties() );
        }
        return result;
    }

    private FeatureResultSet queryByIdFilter( IdFilter filter, SortProperty[] sortCrit )
                            throws FeatureStoreException {
        if ( blobMapping != null ) {
            return queryByIdFilterBlob( filter, sortCrit );
        }
        return queryByIdFilterRelational( filter, sortCrit );
    }

    private FeatureResultSet queryByIdFilterBlob( IdFilter filter, SortProperty[] sortCrit )
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
            rs = stmt.executeQuery( "SELECT gml_id,binary_object FROM " + blobMapping.getTable()
                                    + " A, temp_ids B WHERE A.gml_id=b.fid" );

            FeatureBuilder builder = new FeatureBuilderBlob( this, blobMapping );
            result = new IteratorResultSet( new PostGISResultSetIterator( builder, rs, conn, stmt ) );
        } catch ( Exception e ) {
            close( rs, stmt, conn, LOG );
            String msg = "Error performing id query: " + e.getMessage();
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

        // sort features
        if ( sortCrit.length > 0 ) {
            result = new MemoryFeatureResultSet( Features.sortFc( result.toCollection(), sortCrit ) );
        }
        return result;
    }

    private FeatureResultSet queryByIdFilterRelational( IdFilter filter, SortProperty[] sortCrit )
                            throws FeatureStoreException {

        LinkedHashMap<QName, List<String>> ftNameToIdKernels = new LinkedHashMap<QName, List<String>>();
        try {
            for ( String fid : filter.getMatchingIds() ) {
                IdAnalysis analysis = schema.analyzeId( fid );
                FeatureType ft = analysis.getFeatureType();
                String idKernel = analysis.getIdKernel();
                List<String> idKernels = ftNameToIdKernels.get( ft.getName() );
                if ( idKernels == null ) {
                    idKernels = new ArrayList<String>();
                    ftNameToIdKernels.put( ft.getName(), idKernels );
                }
                idKernels.add( idKernel );
            }
        } catch ( IllegalArgumentException e ) {
            throw new FeatureStoreException( e.getMessage(), e );
        }

        if ( ftNameToIdKernels.size() != 1 ) {
            throw new FeatureStoreException(
                                             "Currently, only relational id queries are supported that target single feature types." );
        }

        QName ftName = ftNameToIdKernels.keySet().iterator().next();
        FeatureType ft = schema.getFeatureType( ftName );
        FeatureTypeMapping ftMapping = schema.getMapping( ftName );
        FIDMapping fidMapping = ftMapping.getFidMapping();
        List<String> idKernels = ftNameToIdKernels.get( ftName );

        StringBuilder sql = new StringBuilder( "SELECT " );
        sql.append( fidMapping.getColumn() );
        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            // append every (mapped) property to SELECT list
            // TODO columns in related tables
            Mapping mapping = ftMapping.getMapping( pt.getName() );
            MappingExpression column = mapping == null ? null : mapping.getMapping();
            if ( column != null ) {
                sql.append( ',' );
                if ( column instanceof JoinChain ) {
                    JoinChain jc = (JoinChain) column;
                    sql.append( jc.getFields().get( 0 ) );
                } else {
                    if ( pt instanceof SimplePropertyType ) {
                        sql.append( column );
                    } else if ( pt instanceof GeometryPropertyType ) {
                        if ( useLegacyPredicates ) {
                            sql.append( "AsBinary(" );
                        } else {
                            sql.append( "ST_AsBinary(" );
                        }
                        sql.append( column );
                        sql.append( ')' );
                    } else if ( pt instanceof FeaturePropertyType ) {
                        sql.append( column );
                    } else {
                        LOG.warn( "Skipping property '" + pt.getName() + "' -- type '" + pt.getClass()
                                  + "' not handled in PostGISFeatureStore." );
                    }
                }
            }
        }

        sql.append( " FROM " );
        sql.append( ftMapping.getFtTable() );
        sql.append( " WHERE " );
        sql.append( fidMapping.getColumn() );
        sql.append( " IN (?" );
        for ( int i = 1; i < idKernels.size(); i++ ) {
            sql.append( ",?" );
        }
        sql.append( ")" );

        LOG.debug( "SQL: {}", sql );

        FeatureResultSet result = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            long begin = System.currentTimeMillis();
            conn = ConnectionManager.getConnection( jdbcConnId );
            stmt = conn.prepareStatement( sql.toString() );
            LOG.debug( "Preparing SELECT took {} [ms] ", System.currentTimeMillis() - begin );

            int i = 1;
            for ( String idKernel : idKernels ) {
                PrimitiveValue value = new PrimitiveValue( idKernel, fidMapping.getColumnType() );
                Object sqlValue = SQLValueMangler.internalToSQL( value );
                stmt.setObject( i++, sqlValue );
            }

            begin = System.currentTimeMillis();
            rs = stmt.executeQuery();
            LOG.debug( "Executing SELECT took {} [ms] ", System.currentTimeMillis() - begin );
            FeatureBuilder builder = new FeatureBuilderRelational( this, ft, ftMapping, conn );
            result = new IteratorResultSet( new PostGISResultSetIterator( builder, rs, conn, stmt ) );
        } catch ( Exception e ) {
            close( rs, stmt, conn, LOG );
            String msg = "Error performing query by id filter (relational mode): " + e.getMessage();
            LOG.error( msg, e );
            throw new FeatureStoreException( msg, e );
        }
        return result;
    }

    /**
     * @param conn
     * @param query
     * @param ftName
     * @param filter
     * @return
     * @throws FeatureStoreException
     */
    FeatureResultSet queryByOperatorFilter( Query query, QName ftName, OperatorFilter filter )
                            throws FeatureStoreException {

        LOG.debug( "Performing query by operator filter" );

        PostGISWhereBuilder wb = null;
        Connection conn = null;
        FeatureResultSet result = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            FeatureType ft = schema.getFeatureType( ftName );
            FeatureTypeMapping ftMapping = getMapping( ftName );

            conn = ConnectionManager.getConnection( jdbcConnId );

            PostGISFeatureMapping pgMapping = new PostGISFeatureMapping( schema, ft, ftMapping, this );
            wb = new PostGISWhereBuilder( pgMapping, filter, query.getSortProperties(), useLegacyPredicates );
            LOG.debug( "WHERE clause: " + wb.getWhere() );
            LOG.debug( "ORDER BY clause: " + wb.getOrderBy() );

            BlobMapping blobMapping = schema.getBlobMapping();
            String ftTableAlias = wb.getAliasManager().getRootTableAlias();
            String blobTableAlias = wb.getAliasManager().generateNew();

            StringBuilder sql = new StringBuilder( "SELECT " );
            if ( blobMapping != null ) {
                sql.append( blobTableAlias );
                sql.append( '.' );
                sql.append( schema.getBlobMapping().getGMLIdColumn() );
                sql.append( ',' );
                sql.append( blobTableAlias );
                sql.append( '.' );
                sql.append( schema.getBlobMapping().getDataColumn() );
            } else {
                sql.append( ftTableAlias );
                sql.append( '.' );
                sql.append( ftMapping.getFidMapping().getColumn() );
                for ( PropertyType pt : ft.getPropertyDeclarations() ) {
                    // append every (mapped) property to SELECT list
                    // TODO columns in related tables
                    Mapping mapping = ftMapping.getMapping( pt.getName() );
                    MappingExpression column = mapping == null ? null : mapping.getMapping();
                    if ( column != null ) {
                        sql.append( ',' );
                        if ( column instanceof JoinChain ) {
                            JoinChain jc = (JoinChain) column;
                            sql.append( ftTableAlias );
                            sql.append( '.' );
                            sql.append( jc.getFields().get( 0 ) );
                        } else {
                            if ( pt instanceof SimplePropertyType ) {
                                sql.append( ftTableAlias );
                                sql.append( '.' );
                                sql.append( column );
                            } else if ( pt instanceof GeometryPropertyType ) {
                                if ( useLegacyPredicates ) {
                                    sql.append( "AsBinary(" );
                                } else {
                                    sql.append( "ST_AsBinary(" );
                                }
                                sql.append( ftTableAlias );
                                sql.append( '.' );
                                sql.append( column );
                                sql.append( ')' );
                            } else if ( pt instanceof FeaturePropertyType ) {
                                sql.append( ftTableAlias );
                                sql.append( '.' );
                                sql.append( column );
                            } else {
                                LOG.warn( "Skipping property '" + pt.getName() + "' -- type '" + pt.getClass()
                                          + "' not handled in PostGISFeatureStore." );
                            }
                        }
                    }
                }
            }

            sql.append( " FROM " );
            if ( blobMapping == null ) {
                // pure relational query
                sql.append( ftMapping.getFtTable() );
                sql.append( " AS " );
                sql.append( ftTableAlias );
            } else if ( wb.getWhere() == null && wb.getOrderBy() == null ) {
                // pure BLOB query
                sql.append( blobMapping.getTable() );
                sql.append( " AS " );
                sql.append( blobTableAlias );
            } else {
                // hybrid query
                sql.append( blobMapping.getTable() );
                sql.append( " AS " );
                sql.append( blobTableAlias );
                sql.append( " LEFT OUTER JOIN " );
                sql.append( ftMapping.getFtTable() );
                sql.append( " AS " );
                sql.append( ftTableAlias );
                sql.append( " ON " );
                sql.append( blobTableAlias );
                sql.append( "." );
                sql.append( blobMapping.getInternalIdColumn() );
                sql.append( "=" );
                sql.append( ftTableAlias );
                sql.append( "." );
                sql.append( ftMapping.getFidMapping().getColumn() );
            }

            for ( PropertyNameMapping mappedPropName : wb.getMappedPropertyNames() ) {
                String currentAlias = ftTableAlias;
                for ( Join join : mappedPropName.getJoins() ) {
                    DBField from = join.getFrom();
                    DBField to = join.getTo();
                    sql.append( " LEFT OUTER JOIN " );
                    sql.append( to.getTable() );
                    sql.append( " AS " );
                    sql.append( to.getAlias() );
                    sql.append( " ON " );
                    sql.append( currentAlias );
                    sql.append( "." );
                    sql.append( from.getColumn() );
                    sql.append( "=" );
                    currentAlias = to.getAlias();
                    sql.append( currentAlias );
                    sql.append( "." );
                    sql.append( to.getColumn() );
                }
            }

            if ( blobMapping != null ) {
                sql.append( " WHERE " );
                sql.append( blobTableAlias );
                sql.append( "." );
                sql.append( blobMapping.getTypeColumn() );
                sql.append( "=?" );
                if ( query.getPrefilterBBox() != null ) {
                    sql.append( " AND " );
                    sql.append( blobTableAlias );
                    sql.append( "." );
                    sql.append( blobMapping.getBBoxColumn() );
                    sql.append( " && ?" );
                }
            }

            if ( wb.getWhere() != null ) {
                if ( blobMapping != null ) {
                    sql.append( " AND " );
                } else {
                    sql.append( " WHERE " );
                }
                sql.append( wb.getWhere().getSQL() );
            }
            if ( wb.getOrderBy() != null ) {
                sql.append( " ORDER BY " );
                sql.append( wb.getOrderBy().getSQL() );
            }

            LOG.debug( "SQL: {}", sql );
            long begin = System.currentTimeMillis();
            stmt = conn.prepareStatement( sql.toString() );
            LOG.debug( "Preparing SELECT took {} [ms] ", System.currentTimeMillis() - begin );

            int i = 1;
            if ( blobMapping != null ) {
                stmt.setShort( i++, schema.getFtId( ftName ) );
                if ( query.getPrefilterBBox() != null ) {
                    Envelope env = (Envelope) getCompatibleGeometry( query.getPrefilterBBox(), blobMapping.getCRS() );
                    stmt.setObject( i++, toPGPolygon( env, -1 ) );
                }
            }
            if ( wb.getWhere() != null ) {
                for ( SQLLiteral o : wb.getWhere().getLiterals() ) {
                    stmt.setObject( i++, o.getValue() );
                }
            }
            if ( wb.getOrderBy() != null ) {
                for ( SQLLiteral o : wb.getOrderBy().getLiterals() ) {
                    stmt.setObject( i++, o.getValue() );
                }
            }

            begin = System.currentTimeMillis();
            rs = stmt.executeQuery();
            LOG.debug( "Executing SELECT took {} [ms] ", System.currentTimeMillis() - begin );

            FeatureBuilder builder = null;
            if ( blobMapping != null ) {
                builder = new FeatureBuilderBlob( this, blobMapping );
            } else {
                builder = new FeatureBuilderRelational( this, ft, ftMapping, conn );
            }
            result = new IteratorResultSet( new PostGISResultSetIterator( builder, rs, conn, stmt ) );
        } catch ( Exception e ) {
            close( rs, stmt, conn, LOG );
            String msg = "Error performing query by operator filter: " + e.getMessage();
            LOG.error( msg, e );
            throw new FeatureStoreException( msg, e );
        }

        if ( wb.getPostFilter() != null ) {
            LOG.debug( "Applying in-memory post-filtering." );
            result = new FilteredFeatureResultSet( result, wb.getPostFilter() );
        }
        if ( wb.getPostSortCriteria() != null ) {
            LOG.debug( "Applying in-memory post-sorting." );
            result = new MemoryFeatureResultSet( Features.sortFc( result.toCollection(), wb.getPostSortCriteria() ) );
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
            StringBuffer sql = new StringBuffer( "SELECT gml_id,binary_object FROM " + blobMapping.getTable()
                                                 + " WHERE " );
            if ( looseBBox != null ) {
                sql.append( "gml_bounded_by && ? AND " );
            }
            sql.append( "ft_type IN(?" );
            for ( int i = 1; i < ftId.length; i++ ) {
                sql.append( ",?" );
            }
            sql.append( ") ORDER BY position('['||ft_type||']' IN ?)" );
            stmt = conn.prepareStatement( sql.toString() );
            int firstFtArg = 1;
            if ( looseBBox != null ) {
                stmt.setObject( 1,
                                toPGPolygon( (Envelope) getCompatibleGeometry( looseBBox, blobMapping.getCRS() ), -1 ) );
                firstFtArg++;
            }
            StringBuffer orderString = new StringBuffer();
            for ( int i = 0; i < ftId.length; i++ ) {
                stmt.setShort( i + firstFtArg, ftId[i] );
                orderString.append( "[" );
                orderString.append( "" + ftId[i] );
                orderString.append( "]" );
            }
            stmt.setString( ftId.length + firstFtArg, orderString.toString() );
            LOG.debug( "Query {}", stmt );

            rs = stmt.executeQuery();
            FeatureBuilder builder = new FeatureBuilderBlob( this, blobMapping );
            result = new IteratorResultSet( new PostGISResultSetIterator( builder, rs, conn, stmt ) );
        } catch ( Exception e ) {
            close( rs, stmt, conn, LOG );
            String msg = "Error performing query: " + e.getMessage();
            LOG.debug( msg, e );
            throw new FeatureStoreException( msg, e );
        }
        return result;
    }

    // private FeatureResultSet queryMultipleFts2( Query[] queries, Envelope looseBBox )
    // throws FeatureStoreException {
    //
    // FeatureResultSet result = null;
    //
    // short[] ftId = new short[queries.length];
    // for ( int i = 0; i < ftId.length; i++ ) {
    // Query query = queries[i];
    // if ( query.getTypeNames() == null || query.getTypeNames().length > 1 ) {
    // String msg = "Join queries between multiple feature types are currently not supported.";
    // throw new UnsupportedOperationException( msg );
    // }
    // ftId[i] = getFtId( query.getTypeNames()[0].getFeatureTypeName() );
    // }
    //
    // Connection conn = null;
    // PreparedStatement stmt = null;
    // ResultSet rs = null;
    // try {
    // conn = ConnectionManager.getConnection( jdbcConnId );
    // StringBuffer sql = new StringBuffer();
    //
    // if ( queries.length == 1 ) {
    // sql.append( "SELECT gml_id,binary_object FROM " + qualifyTableName( "gml_objects" ) + " WHERE " );
    // if ( looseBBox != null ) {
    // sql.append( "gml_bounded_by && ? AND " );
    // }
    // sql.append( "ft_type=?" );
    // } else {
    // sql.append( "(SELECT gml_id,binary_object FROM " + qualifyTableName( "gml_objects" ) + " WHERE " );
    // if ( looseBBox != null ) {
    // sql.append( "gml_bounded_by && ? AND " );
    // }
    // sql.append( "ft_type=?)" );
    // for ( int i = 1; i < queries.length; i++ ) {
    // sql.append( "UNION (SELECT gml_id,binary_object FROM " + qualifyTableName( "gml_objects" )
    // + " WHERE " );
    // if ( looseBBox != null ) {
    // sql.append( "gml_bounded_by && ? AND " );
    // }
    // sql.append( "ft_type=?)" );
    // }
    // }
    // stmt = conn.prepareStatement( sql.toString() );
    // int i = 1;
    // for ( Query query : queries ) {
    // if ( looseBBox != null ) {
    // stmt.setObject( i++, toPGPolygon( (Envelope) getCompatibleGeometry( looseBBox, storageSRS ), -1 ) );
    // }
    // stmt.setShort( i++, getFtId( query.getTypeNames()[0].getFeatureTypeName() ) );
    // StringBuffer orderString = new StringBuffer();
    // stmt.setString( ftId.length + 2, orderString.toString() );
    // }
    // LOG.info( "Query {}", stmt );
    // rs = stmt.executeQuery();
    // result = new IteratorResultSet( new FeatureResultSetIterator( rs, conn, stmt,
    // new FeatureStoreGMLIdResolver( this ) ) );
    // } catch ( Exception e ) {
    // closeSafely( conn, stmt, rs );
    // String msg = "Error performing query: " + e.getMessage();
    // LOG.debug( msg, e );
    // throw new FeatureStoreException( msg, e );
    // }
    // return result;
    // }

    @Override
    public FeatureResultSet query( final Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {

        // check for most common case: multiple featuretypes, same bbox (WMS), no filter
        boolean wmsStyleQuery = false;
        Envelope env = (Envelope) queries[0].getHint( HINT_LOOSE_BBOX );
        if ( schema.getBlobMapping() != null && queries[0].getFilter() == null
             && queries[0].getSortProperties().length == 0 ) {
            wmsStyleQuery = true;
            for ( int i = 1; i < queries.length; i++ ) {
                Envelope queryBBox = (Envelope) queries[i].getHint( HINT_LOOSE_BBOX );
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
                    LOG.debug( e.getMessage(), e );
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

    @Override
    public String[] getDDL() {
        return new PostGISDDLCreator( schema ).getDDL();
    }

    /**
     * Returns a transformed version of the given {@link Geometry} in the specified CRS.
     * 
     * @param literal
     * @param crs
     * @return transformed version of the geometry, never <code>null</code>
     * @throws FilterEvaluationException
     */
    Geometry getCompatibleGeometry( Geometry literal, CRS crs )
                            throws FilterEvaluationException {

        Geometry transformedLiteral = literal;
        if ( literal != null ) {
            CRS literalCRS = literal.getCoordinateSystem();
            if ( literalCRS != null && !( crs.equals( literalCRS ) ) ) {
                LOG.debug( "Need transformed literal geometry for evaluation: " + literalCRS.getName() + " -> "
                           + crs.getName() );
                try {
                    GeometryTransformer transformer = new GeometryTransformer( crs.getWrappedCRS() );
                    transformedLiteral = transformer.transform( literal );
                } catch ( Exception e ) {
                    throw new FilterEvaluationException( e.getMessage() );
                }
            }
        }
        return transformedLiteral;
    }

    short getFtId( QName ftName ) {
        return schema.getFtId( ftName );
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

    FeatureStoreCache getCache() {
        return cache;
    }

    GMLReferenceResolver getResolver() {
        return resolver;
    }

    String getWKBParamTemplate( String srid ) {
        StringBuilder sb = new StringBuilder();
        if ( useLegacyPredicates ) {
            sb.append( "SetSRID(GeomFromWKB(?)," );
        } else {
            sb.append( "SetSRID(ST_GeomFromWKB(?)," );
        }
        sb.append( srid );
        sb.append( ")" );
        return sb.toString();
    }

    Map<String, String> getNamespaceContext() {
        if ( nsContext == null ) {
            nsContext = new HashMap<String, String>(schema.getNamespaceBindings());
            nsContext.put ("xlink", XLNNS);
            nsContext.put ("xsi", XSINS);
            nsContext.put ("ogc", OGCNS);
        }
        return nsContext;
    }

    private class PostGISResultSetIterator extends ResultSetIterator<Feature> {

        private final FeatureBuilder builder;

        public PostGISResultSetIterator( FeatureBuilder builder, ResultSet rs, Connection conn, Statement stmt ) {
            super( rs, conn, stmt );
            this.builder = builder;
        }

        @Override
        protected Feature createElement( ResultSet rs )
                                throws SQLException {
            return builder.buildFeature( rs );
        }
    }
}