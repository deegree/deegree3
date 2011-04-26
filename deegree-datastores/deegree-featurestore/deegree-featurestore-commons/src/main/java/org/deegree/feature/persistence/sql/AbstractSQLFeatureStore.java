//$HeadURL$
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
package org.deegree.feature.persistence.sql;

import static org.deegree.commons.utils.JDBCUtils.close;
import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.ResultSetIterator;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.primitive.SQLValueMangler;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.Feature;
import org.deegree.feature.Features;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreGMLIdResolver;
import org.deegree.feature.persistence.cache.FeatureStoreCache;
import org.deegree.feature.persistence.cache.SimpleFeatureStoreCache;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.CombinedResultSet;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.FilteredFeatureResultSet;
import org.deegree.feature.persistence.query.IteratorResultSet;
import org.deegree.feature.persistence.query.MemoryFeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.blob.BlobMapping;
import org.deegree.feature.persistence.sql.blob.FeatureBuilderBlob;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.id.IdAnalysis;
import org.deegree.feature.persistence.sql.rules.FeatureBuilderRelational;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.BBOX;
import org.deegree.filter.sql.AbstractWhereBuilder;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.Join;
import org.deegree.filter.sql.PropertyNameMapping;
import org.deegree.filter.sql.expression.SQLLiteral;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.gml.GMLObject;
import org.deegree.gml.GMLReferenceResolver;
import org.slf4j.Logger;

/**
 * Provides common base functionality for {@link FeatureStore} implementations that use {@link MappedApplicationSchema}
 * as mapping configuration and use JDBC for connecting to the backend.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractSQLFeatureStore implements SQLFeatureStore {

    private static final Logger LOG = getLogger( AbstractSQLFeatureStore.class );

    private MappedApplicationSchema schema;

    protected BlobMapping blobMapping;

    private String jdbcConnId;

    // TODO make this configurable
    private final FeatureStoreCache cache = new SimpleFeatureStoreCache( 10000 );

    private final FeatureStoreGMLIdResolver resolver = new FeatureStoreGMLIdResolver( this );

    // cache for feature type bounding boxes
    private final Map<FeatureType, Envelope> ftToBBox = Collections.synchronizedMap( new HashMap<FeatureType, Envelope>() );

    private Map<String, String> nsContext;

    protected void init( MappedApplicationSchema schema, String jdbcConnId ) {
        this.schema = schema;
        this.blobMapping = schema.getBlobMapping();
        this.jdbcConnId = jdbcConnId;
    }

    @Override
    public MappedApplicationSchema getSchema() {
        return schema;
    }

    @Override
    public String getConnId() {
        return jdbcConnId;
    }

    /**
     * Returns the relational mapping for the given feature type name.
     * 
     * @param ftName
     *            name of the feature type, must not be <code>null</code>
     * @return relational mapping for the feature type, may be <code>null</code> (no relational mapping)
     */
    public FeatureTypeMapping getMapping( QName ftName ) {
        return schema.getFtMapping( ftName );
    }

    @Override
    public Envelope getEnvelope( QName ftName )
                            throws FeatureStoreException {
        Envelope env = null;
        FeatureType ft = schema.getFeatureType( ftName );
        if ( ft != null ) {
            if ( !ftToBBox.containsKey( ft ) ) {
                // TODO what should be favored for hybrid mappings?
                if ( blobMapping != null ) {
                    env = getEnvelope( ftName, blobMapping );
                } else {
                    FeatureTypeMapping ftMapping = schema.getFtMapping( ft.getName() );
                    if ( ftMapping == null ) {
                        String msg = "Unable to determine envelope for feature type '" + ftName
                                     + "': neither feature type nor BLOB mapping defined.";
                        throw new FeatureStoreException( msg );
                    }
                    env = getEnvelope( ftMapping );
                }
                ftToBBox.put( ft, env );
            } else {
                env = ftToBBox.get( ft );
            }
        }
        return env;
    }

    public void clearEnvelopeCache() {
        ftToBBox.clear();
    }

    protected abstract Envelope getEnvelope( QName ftName, BlobMapping blobMapping )
                            throws FeatureStoreException;

    protected abstract Envelope getEnvelope( FeatureTypeMapping ftMapping )
                            throws FeatureStoreException;

    @Override
    public GMLObject getObjectById( String id )
                            throws FeatureStoreException {

        GMLObject geomOrFeature = getCache().get( id );
        if ( geomOrFeature == null ) {
            if ( getSchema().getBlobMapping() != null ) {
                geomOrFeature = getObjectByIdBlob( id, getSchema().getBlobMapping() );
            } else {
                geomOrFeature = getObjectByIdRelational( id );
            }
        }
        return geomOrFeature;
    }

    protected abstract GMLObject getObjectByIdBlob( String id, BlobMapping blobMapping )
                            throws FeatureStoreException;

    protected abstract GMLObject getObjectByIdRelational( String id )
                            throws FeatureStoreException;

    @Override
    public LockManager getLockManager()
                            throws FeatureStoreException {
        // TODO
        return null;
    }

    /**
     * Returns the {@link FeatureStoreCache}.
     * 
     * @return feature store cache, never <code>null</code>
     */
    public FeatureStoreCache getCache() {
        return cache;
    }

    /**
     * Returns a resolver instance for retrieving objects that are stored in this feature store.
     * 
     * @return resolver, never <code>null</code>
     */
    public GMLReferenceResolver getResolver() {
        return resolver;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void destroy() {
        // nothing to do
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

    public Map<String, String> getNamespaceContext() {
        if ( nsContext == null ) {
            nsContext = new HashMap<String, String>( getSchema().getNamespaceBindings() );
            nsContext.put( "xlink", XLNNS );
            nsContext.put( "xsi", XSINS );
            nsContext.put( "ogc", OGCNS );
        }
        return nsContext;
    }

    /**
     * Returns a transformed version of the given {@link Geometry} in the specified CRS.
     * 
     * @param literal
     * @param crs
     * @return transformed version of the geometry, never <code>null</code>
     * @throws FilterEvaluationException
     */
    public Geometry getCompatibleGeometry( Geometry literal, ICRS crs )
                            throws FilterEvaluationException {
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
                    throw new FilterEvaluationException( e.getMessage() );
                }
            }
        }
        return transformedLiteral;
    }

    public abstract SQLValueMapper getSQLValueMapper();

    protected short getFtId( QName ftName ) {
        return getSchema().getFtId( ftName );
    }

    @Override
    public FeatureResultSet query( Query query )
                            throws FeatureStoreException, FilterEvaluationException {

        if ( query.getTypeNames() == null || query.getTypeNames().length > 1 ) {
            String msg = "Join queries between multiple feature types are not supported yet.";
            throw new UnsupportedOperationException( msg );
        }

        FeatureResultSet result = null;
        Filter filter = query.getFilter();

        if ( query.getTypeNames().length == 1 && ( filter == null || filter instanceof OperatorFilter ) ) {
            QName ftName = query.getTypeNames()[0].getFeatureTypeName();
            FeatureType ft = getSchema().getFeatureType( ftName );
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

    @Override
    public FeatureResultSet query( final Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {

        // check for most common case: multiple featuretypes, same bbox (WMS), no filter
        boolean wmsStyleQuery = false;
        Envelope env = (Envelope) queries[0].getPrefilterBBox();
        if ( getSchema().getBlobMapping() != null && queries[0].getFilter() == null
             && queries[0].getSortProperties().length == 0 ) {
            wmsStyleQuery = true;
            for ( int i = 1; i < queries.length; i++ ) {
                Envelope queryBBox = (Envelope) queries[i].getPrefilterBBox();
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
            conn = ConnectionManager.getConnection( getConnId() );

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

        LinkedHashMap<QName, List<IdAnalysis>> ftNameToIdAnalysis = new LinkedHashMap<QName, List<IdAnalysis>>();
        try {
            for ( String fid : filter.getMatchingIds() ) {
                IdAnalysis analysis = getSchema().analyzeId( fid );
                FeatureType ft = analysis.getFeatureType();
                List<IdAnalysis> idKernels = ftNameToIdAnalysis.get( ft.getName() );
                if ( idKernels == null ) {
                    idKernels = new ArrayList<IdAnalysis>();
                    ftNameToIdAnalysis.put( ft.getName(), idKernels );
                }
                idKernels.add( analysis );
            }
        } catch ( IllegalArgumentException e ) {
            throw new FeatureStoreException( e.getMessage(), e );
        }

        if ( ftNameToIdAnalysis.size() != 1 ) {
            throw new FeatureStoreException(
                                             "Currently, only relational id queries are supported that target single feature types." );
        }

        QName ftName = ftNameToIdAnalysis.keySet().iterator().next();
        FeatureType ft = getSchema().getFeatureType( ftName );
        FeatureTypeMapping ftMapping = getSchema().getFtMapping( ftName );
        FIDMapping fidMapping = ftMapping.getFidMapping();
        List<IdAnalysis> idKernels = ftNameToIdAnalysis.get( ftName );

        FeatureResultSet result = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            long begin = System.currentTimeMillis();

            FeatureBuilder builder = new FeatureBuilderRelational( this, ft, ftMapping, conn );
            List<String> columns = builder.getInitialSelectColumns();
            StringBuilder sql = new StringBuilder( "SELECT " );
            sql.append( columns.get( 0 ) );
            for ( int i = 1; i < columns.size(); i++ ) {
                sql.append( ',' );
                sql.append( columns.get( i ) );
            }
            sql.append( " FROM " );
            sql.append( ftMapping.getFtTable() );
            sql.append( " WHERE " );
            boolean first = true;
            for ( IdAnalysis idKernel : idKernels ) {
                if ( !first ) {
                    sql.append( " OR " );
                }
                sql.append( "(" );
                boolean firstCol = true;
                for ( Pair<String, PrimitiveType> fidColumn : fidMapping.getColumns() ) {
                    if ( !firstCol ) {
                        sql.append( " AND " );
                    }
                    sql.append( fidColumn.first );
                    sql.append( "=?" );
                    firstCol = false;
                }
                sql.append( ")" );
                first = false;
            }
            LOG.debug( "SQL: {}", sql );

            conn = ConnectionManager.getConnection( getConnId() );
            stmt = conn.prepareStatement( sql.toString() );
            LOG.debug( "Preparing SELECT took {} [ms] ", System.currentTimeMillis() - begin );

            int i = 1;
            for ( IdAnalysis idKernel : idKernels ) {
                for ( Object o : idKernel.getIdKernels() ) {
                    // TODO
                    PrimitiveValue value = new PrimitiveValue( o, PrimitiveType.STRING );
                    Object sqlValue = SQLValueMangler.internalToSQL( value );
                    stmt.setObject( i++, sqlValue );
                }
            }

            begin = System.currentTimeMillis();
            rs = stmt.executeQuery();
            LOG.debug( "Executing SELECT took {} [ms] ", System.currentTimeMillis() - begin );
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

        AbstractWhereBuilder wb = null;
        Connection conn = null;
        FeatureResultSet result = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionManager.getConnection( getConnId() );
            // TODO where to put this?
            conn.setAutoCommit( false );

            FeatureType ft = getSchema().getFeatureType( ftName );
            FeatureTypeMapping ftMapping = getMapping( ftName );

            FeatureBuilder builder = null;
            if ( getSchema().getBlobMapping() != null ) {
                builder = new FeatureBuilderBlob( this, getSchema().getBlobMapping() );
            } else {
                builder = new FeatureBuilderRelational( this, ft, ftMapping, conn );
            }
            List<String> columns = builder.getInitialSelectColumns();

            wb = getWhereBuilder( ft, filter, query.getSortProperties(), conn );
            LOG.debug( "WHERE clause: " + wb.getWhere() );
            LOG.debug( "ORDER BY clause: " + wb.getOrderBy() );

            BlobMapping blobMapping = getSchema().getBlobMapping();
            String ftTableAlias = wb.getAliasManager().getRootTableAlias();
            String blobTableAlias = wb.getAliasManager().generateNew();
            String tableAlias = ftTableAlias;
            if ( blobMapping != null ) {
                tableAlias = blobTableAlias;
            }

            StringBuilder sql = new StringBuilder( "SELECT " );
            sql.append( tableAlias );
            sql.append( '.' );
            sql.append( columns.get( 0 ) );
            for ( int i = 1; i < columns.size(); i++ ) {
                sql.append( ',' );
                // TODO
                if ( !columns.get( i ).contains( "(" ) ) {
                    sql.append( tableAlias );
                    sql.append( '.' );
                }
                sql.append( columns.get( i ) );
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
                stmt.setShort( i++, getSchema().getFtId( ftName ) );
                if ( query.getPrefilterBBox() != null ) {
                    OperatorFilter bboxFilter = new OperatorFilter( new BBOX( query.getPrefilterBBox() ) );
                    AbstractWhereBuilder blobWb = getWhereBuilderBlob( bboxFilter, conn );
                    if ( blobWb.getWhere() != null ) {
                        for ( SQLLiteral o : blobWb.getWhere().getLiterals() ) {
                            stmt.setObject( i++, o.getValue() );
                        }
                    }
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

            // TODO make this configurable?
            stmt.setFetchSize( 1 );
            rs = stmt.executeQuery();
            LOG.debug( "Executing SELECT took {} [ms] ", System.currentTimeMillis() - begin );

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
        AbstractWhereBuilder blobWb = null;
        try {
            if ( looseBBox != null ) {
                OperatorFilter bboxFilter = new OperatorFilter( new BBOX( looseBBox ) );
                blobWb = getWhereBuilderBlob( bboxFilter, conn );
            }

            conn = ConnectionManager.getConnection( getConnId() );
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
            if ( blobWb != null && blobWb.getWhere() != null ) {
                for ( SQLLiteral o : blobWb.getWhere().getLiterals() ) {
                    stmt.setObject( firstFtArg++, o.getValue() );
                }
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

    /**
     * Returns a {@link AbstractWhereBuilder} suitable for the SQL backend.
     * 
     * @param ft
     *            feature type, must not be <code>null</code> and must be mapped
     * @param filter
     *            filter to use for generating the WHERE clause, can be <code>null</code>
     * @param sortCrit
     *            criteria to use generating the ORDER BY clause, can be <code>null</code>
     * @return where builder, never <code>null</code>
     * @throws FilterEvaluationException
     */
    protected abstract AbstractWhereBuilder getWhereBuilder( FeatureType ft, OperatorFilter filter,
                                                             SortProperty[] sortCrit, Connection conn )
                            throws FilterEvaluationException;

    protected abstract AbstractWhereBuilder getWhereBuilderBlob( OperatorFilter filter, Connection conn )
                            throws FilterEvaluationException;

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