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
package org.deegree.feature.persistence.sql;

import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Constructor;
import java.net.URL;
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

import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.jdbc.ResultSetIterator;
import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.CombinedReferenceResolver;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.GMLReferenceResolver;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.commons.tom.sql.SQLValueMangler;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.feature.Feature;
import org.deegree.feature.Features;
import org.deegree.feature.persistence.FeatureInspector;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreGMLIdResolver;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.cache.BBoxCache;
import org.deegree.feature.persistence.cache.FeatureStoreCache;
import org.deegree.feature.persistence.cache.SimpleFeatureStoreCache;
import org.deegree.feature.persistence.lock.DefaultLockManager;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.blob.BlobCodec;
import org.deegree.feature.persistence.sql.blob.BlobMapping;
import org.deegree.feature.persistence.sql.blob.FeatureBuilderBlob;
import org.deegree.feature.persistence.sql.config.AbstractMappedSchemaBuilder;
import org.deegree.feature.persistence.sql.converter.CustomParticleConverter;
import org.deegree.feature.persistence.sql.converter.FeatureParticleConverter;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.id.IdAnalysis;
import org.deegree.feature.persistence.sql.jaxb.CustomConverterJAXB;
import org.deegree.feature.persistence.sql.jaxb.CustomInspector;
import org.deegree.feature.persistence.sql.jaxb.SQLFeatureStoreJAXB;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.FeatureBuilderRelational;
import org.deegree.feature.persistence.sql.rules.FeatureMapping;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.feature.stream.CombinedFeatureInputStream;
import org.deegree.feature.stream.EmptyFeatureInputStream;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.stream.FilteredFeatureInputStream;
import org.deegree.feature.stream.IteratorFeatureInputStream;
import org.deegree.feature.stream.MemoryFeatureInputStream;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.BBOX;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.filter.AbstractWhereBuilder;
import org.deegree.sqldialect.filter.DBField;
import org.deegree.sqldialect.filter.Join;
import org.deegree.sqldialect.filter.MappingExpression;
import org.deegree.sqldialect.filter.PropertyNameMapper;
import org.deegree.sqldialect.filter.PropertyNameMapping;
import org.deegree.sqldialect.filter.TableAliasManager;
import org.deegree.sqldialect.filter.UnmappableException;
import org.deegree.sqldialect.filter.expression.SQLArgument;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * {@link FeatureStore} that is backed by a spatial SQL database.
 *
 * @see SQLDialect
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.2
 */
@LoggingNotes(info = "logs particle converter initialization", debug = "logs the SQL statements sent to the SQL server and startup/shutdown information")
public class SQLFeatureStore implements FeatureStore {

    private static final Logger LOG = getLogger( SQLFeatureStore.class );

    private static final int DEFAULT_FETCH_SIZE = 1000;

    private static final int DEFAULT_CACHE_SIZE = 10000;

    private final SQLFeatureStoreJAXB config;

    private final URL configURL;

    private final SQLDialect dialect;

    private final boolean allowInMemoryFiltering;

    private MappedAppSchema schema;

    private BlobMapping blobMapping;

    private final String jdbcConnId;

    private final Map<Mapping, ParticleConverter<?>> particleMappingToConverter = new HashMap<Mapping, ParticleConverter<?>>();

    private final FeatureStoreCache cache;

    private BBoxCache bboxCache;

    private GMLReferenceResolver resolver = new FeatureStoreGMLIdResolver( this );

    private Map<String, String> nsContext;

    private DefaultLockManager lockManager;

    private final int fetchSize;

    private final Boolean readAutoCommit;

    private final List<FeatureInspector> inspectors = new ArrayList<FeatureInspector>();

    private boolean nullEscalation;

    private final SqlFeatureStoreMetadata metadata;

    private final Workspace workspace;

    private ConnectionProvider connProvider;

    private final ThreadLocal<SQLFeatureStoreTransaction> transaction = new ThreadLocal<SQLFeatureStoreTransaction>();

    /**
     * Creates a new {@link SQLFeatureStore} for the given configuration.
     *
     * @param config
     *            jaxb configuration object
     * @param configURL
     *            configuration systemid
     */
    public SQLFeatureStore( SQLFeatureStoreJAXB config, URL configURL, SQLDialect dialect,
                            SqlFeatureStoreMetadata metadata, Workspace workspace ) {
        this.config = config;
        this.configURL = configURL;
        this.dialect = dialect;
        this.metadata = metadata;
        this.workspace = workspace;
        this.jdbcConnId = config.getJDBCConnId().getValue();
        this.allowInMemoryFiltering = config.getDisablePostFiltering() == null;
        fetchSize = config.getJDBCConnId().getFetchSize() != null ? config.getJDBCConnId().getFetchSize().intValue()
                                                                 : DEFAULT_FETCH_SIZE;
        LOG.debug( "Fetch size: " + fetchSize );
        readAutoCommit = config.getJDBCConnId().isReadAutoCommit() != null ? config.getJDBCConnId().isReadAutoCommit()
                                                                          : !dialect.requiresTransactionForCursorMode();
        LOG.debug( "Read auto commit: " + readAutoCommit );

        if ( config.getFeatureCache() != null ) {
            cache = new SimpleFeatureStoreCache( DEFAULT_CACHE_SIZE );
        } else {
            cache = null;
        }
    }

    /**
     * @return the currently active transaction., may be <code>null</code> if no transaction was acquired
     */
    public FeatureStoreTransaction getTransaction() {
        return transaction.get();
    }

    private void initConverters() {
        for ( FeatureType ft : schema.getFeatureTypes() ) {
            FeatureTypeMapping ftMapping = schema.getFtMapping( ft.getName() );
            if ( ftMapping != null ) {
                for ( Mapping particleMapping : ftMapping.getMappings() ) {
                    initConverter( particleMapping );
                }
            }
        }
    }

    private void initConverter( Mapping particleMapping ) {
        if ( particleMapping.getConverter() != null ) {
            CustomParticleConverter<TypedObjectNode> converter = instantiateConverter( particleMapping.getConverter() );
            converter.init( particleMapping, this );
            particleMappingToConverter.put( particleMapping, converter );
        } else if ( particleMapping instanceof PrimitiveMapping ) {
            PrimitiveMapping pm = (PrimitiveMapping) particleMapping;
            ParticleConverter<?> converter = dialect.getPrimitiveConverter( pm.getMapping().toString(), pm.getType() );
            particleMappingToConverter.put( particleMapping, converter );
        } else if ( particleMapping instanceof GeometryMapping ) {
            GeometryMapping gm = (GeometryMapping) particleMapping;
            ParticleConverter<?> converter = getGeometryConverter( gm );
            particleMappingToConverter.put( particleMapping, converter );
        } else if ( particleMapping instanceof FeatureMapping ) {
            FeatureMapping fm = (FeatureMapping) particleMapping;
            SQLIdentifier fkColumn = null;
            if ( fm.getJoinedTable() != null && !fm.getJoinedTable().isEmpty() ) {
                // TODO more complex joins
                fkColumn = fm.getJoinedTable().get( fm.getJoinedTable().size() - 1 ).getFromColumns().get( 0 );
            }
            SQLIdentifier hrefColumn = null;
            if ( fm.getHrefMapping() != null ) {
                hrefColumn = new SQLIdentifier( fm.getHrefMapping().toString() );
            }
            FeatureType valueFt = null;
            if ( fm.getValueFtName() != null ) {
                valueFt = schema.getFeatureType( fm.getValueFtName() );
            }
            ParticleConverter<?> converter = new FeatureParticleConverter( fkColumn, hrefColumn, getResolver(),
                                                                           valueFt, schema );
            particleMappingToConverter.put( particleMapping, converter );
        } else if ( particleMapping instanceof CompoundMapping ) {
            CompoundMapping cm = (CompoundMapping) particleMapping;
            for ( Mapping childMapping : cm.getParticles() ) {
                initConverter( childMapping );
            }
        } else {
            LOG.warn( "Unhandled particle mapping type {}", particleMapping );
        }
    }

    ParticleConverter<Geometry> getGeometryConverter( GeometryMapping geomMapping ) {
        String column = geomMapping.getMapping().toString();
        ICRS crs = geomMapping.getCRS();
        String srid = geomMapping.getSrid();
        boolean is2d = geomMapping.getDim() == CoordinateDimension.DIM_2;
        return dialect.getGeometryConverter( column, crs, srid, is2d );
    }

    @SuppressWarnings("unchecked")
    private CustomParticleConverter<TypedObjectNode> instantiateConverter( CustomConverterJAXB config ) {
        String className = config.getClazz();
        LOG.info( "Instantiating configured custom particle converter (class=" + className + ")" );
        try {
            return (CustomParticleConverter<TypedObjectNode>) workspace.getModuleClassLoader().loadClass( className ).newInstance();
        } catch ( Throwable t ) {
            String msg = "Unable to instantiate custom particle converter (class=" + className + "). "
                         + " Maybe directory 'modules' in your workspace is missing the JAR with the "
                         + " referenced converter class?! " + t.getMessage();
            LOG.error( msg, t );
            throw new IllegalArgumentException( msg );
        }
    }

    @Override
    public MappedAppSchema getSchema() {
        return schema;
    }

    @Override
    public boolean isMapped( QName ftName ) {
        if ( schema.getFtMapping( ftName ) != null ) {
            return true;
        }
        if ( schema.getBBoxMapping() != null ) {
            return true;
        }
        return false;
    }

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

    /**
     * Returns a {@link ParticleConverter} for the given {@link Mapping} instance from the served
     * {@link MappedAppSchema}.
     *
     * @param mapping
     *            particle mapping, must not be <code>null</code>
     * @return particle converter, never <code>null</code>
     */
    public ParticleConverter<?> getConverter( Mapping mapping ) {
        return particleMappingToConverter.get( mapping );
    }

    @Override
    public Envelope getEnvelope( QName ftName )
                            throws FeatureStoreException {
        if ( !bboxCache.contains( ftName ) ) {
            calcEnvelope( ftName );
        }
        return bboxCache.get( ftName );
    }

    @Override
    public Envelope calcEnvelope( QName ftName )
                            throws FeatureStoreException {

        Envelope env = null;
        Connection conn = null;
        try {
            conn = getConnection();
            env = calcEnvelope( ftName, conn );
        } catch ( SQLException e ) {
            throw new FeatureStoreException( e.getMessage() );
        } finally {
            release( null, null, conn );
        }
        return env;
    }

    Envelope calcEnvelope( QName ftName, Connection conn )
                            throws FeatureStoreException {
        Envelope env = null;
        FeatureType ft = schema.getFeatureType( ftName );
        if ( ft != null ) {
            // TODO what should be favored for hybrid mappings?
            if ( blobMapping != null ) {
                env = calcEnvelope( ftName, blobMapping, conn );
            } else if ( schema.getFtMapping( ft.getName() ) != null ) {
                FeatureTypeMapping ftMapping = schema.getFtMapping( ft.getName() );
                env = calcEnvelope( ftMapping, conn );
            }
        }
        bboxCache.set( ftName, env );
        return env;
    }

    private Envelope calcEnvelope( FeatureTypeMapping ftMapping, Connection conn )
                            throws FeatureStoreException {

        LOG.trace( "Determining BBOX for feature type '{}' (relational mode)", ftMapping.getFeatureType() );

        String column = null;
        Pair<TableName, GeometryMapping> propMapping = ftMapping.getDefaultGeometryMapping();
        if ( propMapping == null ) {
            return null;
        }
        MappingExpression me = propMapping.second.getMapping();
        if ( me == null || !( me instanceof DBField ) ) {
            String msg = "Cannot determine BBOX for feature type '" + ftMapping.getFeatureType()
                         + "' (relational mode).";
            LOG.warn( msg );
            return null;
        }
        column = ( (DBField) me ).getColumn();

        Envelope env = null;
        StringBuilder sql = new StringBuilder( "SELECT " );
        sql.append( dialect.getBBoxAggregateSnippet( column ) );
        sql.append( " FROM " );
        sql.append( propMapping.first );

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            LOG.debug( "Executing envelope SELECT: " + sql );
            rs = stmt.executeQuery( sql.toString() );
            rs.next();
            ICRS crs = propMapping.second.getCRS();
            env = dialect.getBBoxAggregateValue( rs, 1, crs );
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new FeatureStoreException( e.getMessage(), e );
        } finally {
            release( rs, stmt, null );
        }
        return env;
    }

    private Envelope calcEnvelope( QName ftName, BlobMapping blobMapping, Connection conn )
                            throws FeatureStoreException {

        LOG.debug( "Determining BBOX for feature type '{}' (BLOB mode)", ftName );

        int ftId = getFtId( ftName );
        String column = blobMapping.getBBoxColumn();

        Envelope env = null;
        StringBuilder sql = new StringBuilder( "SELECT " );
        sql.append( dialect.getBBoxAggregateSnippet( column ) );
        sql.append( " FROM " );
        sql.append( blobMapping.getTable() );
        sql.append( " WHERE " );
        sql.append( blobMapping.getTypeColumn() );
        sql.append( "=" );
        sql.append( ftId );

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery( sql.toString() );
            rs.next();
            ICRS crs = blobMapping.getCRS();
            env = dialect.getBBoxAggregateValue( rs, 1, crs );
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new FeatureStoreException( e.getMessage(), e );
        } finally {
            release( rs, stmt, null );
        }
        return env;
    }

    BBoxCache getBBoxCache() {
        return bboxCache;
    }

    @Override
    public GMLObject getObjectById( String id )
                            throws FeatureStoreException {

        GMLObject geomOrFeature = null;
        if ( getCache() != null ) {
            geomOrFeature = getCache().get( id );
        }
        if ( geomOrFeature == null ) {
            if ( getSchema().getBlobMapping() != null ) {
                geomOrFeature = getObjectByIdBlob( id, getSchema().getBlobMapping() );
            } else {
                geomOrFeature = getObjectByIdRelational( id );
            }
        }
        return geomOrFeature;
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

            conn = getConnection();
            stmt = conn.prepareStatement( sql.toString() );
            stmt.setFetchSize( fetchSize );
            stmt.setString( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                LOG.debug( "Recreating object '" + id + "' from bytea." );
                BlobCodec codec = blobMapping.getCodec();
                geomOrFeature = codec.decode( rs.getBinaryStream( 1 ), getNamespaceContext(), getSchema(),
                                              blobMapping.getCRS(), resolver );
                if ( getCache() != null ) {
                    getCache().add( geomOrFeature );
                }
            }
        } catch ( Exception e ) {
            String msg = "Error retrieving object by id (BLOB mode): " + e.getMessage();
            LOG.debug( msg, e );
            throw new FeatureStoreException( msg, e );
        } finally {
            release( rs, stmt, conn );
        }
        return geomOrFeature;
    }

    private GMLObject getObjectByIdRelational( String id )
                            throws FeatureStoreException {

        GMLObject result = null;

        IdAnalysis idAnalysis = getSchema().analyzeId( id );
        if ( !idAnalysis.isFid() ) {
            String msg = "Fetching of geometries by id (relational mode) is not implemented yet.";
            throw new UnsupportedOperationException( msg );
        }

        FeatureInputStream rs = queryByIdFilterRelational( null, new IdFilter( id ), null );
        try {
            Iterator<Feature> iter = rs.iterator();
            if ( iter.hasNext() ) {
                result = iter.next();
            }
        } finally {
            rs.close();
        }
        return result;
    }

    @Override
    public LockManager getLockManager()
                            throws FeatureStoreException {
        return lockManager;
    }

    @Override
    public FeatureStoreTransaction acquireTransaction()
                            throws FeatureStoreException {
        SQLFeatureStoreTransaction ta = null;
        try {
            final Connection conn = getConnection();
            conn.setAutoCommit( false );
            ta = new SQLFeatureStoreTransaction( this, conn, getSchema(), inspectors );
            transaction.set( ta );
        } catch ( SQLException e ) {
            throw new FeatureStoreException( "Unable to acquire JDBC connection for transaction: " + e.getMessage(), e );
        }
        return ta;
    }

    void closeAndDetachTransactionConnection()
                            throws FeatureStoreException {
        try {
            transaction.get().getConnection().close();
        } catch ( final SQLException e ) {
            LOG.error( "Error closing connection/removing it from the pool: " + e.getMessage() );
        } finally {
            transaction.remove();
        }
    }

    /**
     * Returns the {@link FeatureStoreCache}.
     *
     * @return feature store cache, can be <code>null</code> (no cache configured)
     */
    public FeatureStoreCache getCache() {
        return cache;
    }

    /**
     * Returns a resolver instance for resolving references to objects that are stored in this feature store.
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

        if ( query.getTypeNames() == null || query.getTypeNames().length > 1 ) {
            String msg = "Join queries between multiple feature types are not supported by the SQLFeatureStore implementation (yet).";
            throw new UnsupportedOperationException( msg );
        }

        Filter filter = query.getFilter();

        int hits = 0;
        if ( query.getTypeNames().length == 1 && ( filter == null || filter instanceof OperatorFilter ) ) {
            QName ftName = query.getTypeNames()[0].getFeatureTypeName();
            FeatureType ft = getSchema().getFeatureType( ftName );
            if ( ft == null ) {
                String msg = "Feature type '" + ftName + "' is not served by this feature store.";
                throw new FeatureStoreException( msg );
            }
            hits = queryHitsByOperatorFilter( query, ftName, (OperatorFilter) filter );
        } else {
            // must be an id filter based query
            if ( query.getFilter() == null || !( query.getFilter() instanceof IdFilter ) ) {
                String msg = "Invalid query. If no type names are specified, it must contain an IdFilter.";
                throw new FilterEvaluationException( msg );
            }
            // should be no problem iterating over the features (id queries usually request only a few ids)
            hits = queryByIdFilter( query.getTypeNames(), (IdFilter) filter, query.getSortProperties() ).count();
        }
        return hits;
    }

    private int queryHitsByOperatorFilter( Query query, QName ftName, OperatorFilter filter )
                            throws FeatureStoreException {

        LOG.debug( "Performing hits query by operator filter" );

        if ( getSchema().getBlobMapping() != null ) {
            return queryHitsByOperatorFilterBlob( query, ftName, filter );
        }

        FeatureType ft = getSchema().getFeatureType( ftName );
        FeatureTypeMapping ftMapping = getMapping( ftName );
        if ( ftMapping == null ) {
            String msg = "Cannot perform query on feature type '" + ftName + "'. Feature type is not mapped.";
            throw new FeatureStoreException( msg );
        }

        int hits = 0;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            AbstractWhereBuilder wb = getWhereBuilder( ft, filter, query.getSortProperties(), conn, query.isHandleStrict() );

            if ( wb.getPostFilter() != null ) {
                LOG.debug( "Filter not fully mappable to WHERE clause. Need to iterate over all features to determine count." );
                hits = queryByOperatorFilter( query, ftName, filter ).count();
            } else {
                StringBuilder sql = new StringBuilder( "SELECT " );
                if ( wb.getWhere() == null ) {
                    sql.append( "COUNT(*) FROM " );
                    sql.append( ftMapping.getFtTable() );
                } else {
                    sql.append( "COUNT(*) FROM (SELECT DISTINCT " );

                    String ftTableAlias = wb.getAliasManager().getRootTableAlias();

                    FIDMapping fidMapping = ftMapping.getFidMapping();
                    List<Pair<SQLIdentifier, BaseType>> fidCols = fidMapping.getColumns();
                    boolean first = true;
                    for ( Pair<SQLIdentifier, BaseType> fidCol : fidCols ) {
                        if ( !first ) {
                            sql.append( "," );
                        } else {
                            first = false;
                        }
                        sql.append( ftTableAlias ).append( '.' ).append( fidCol.first );
                    }

                    sql.append( " FROM " );

                    // pure relational query
                    sql.append( ftMapping.getFtTable() );
                    sql.append( ' ' );
                    sql.append( ftTableAlias );

                    for ( PropertyNameMapping mappedPropName : wb.getMappedPropertyNames() ) {
                        for ( Join join : mappedPropName.getJoins() ) {
                            sql.append( " LEFT OUTER JOIN " );
                            sql.append( join.getToTable() );
                            sql.append( ' ' );
                            sql.append( join.getToTableAlias() );
                            sql.append( " ON " );
                            sql.append( join.getSQLJoinCondition() );
                        }
                    }

                    LOG.debug( "WHERE clause: " + wb.getWhere() );
                    if ( wb.getWhere() != null ) {
                        sql.append( " WHERE " );
                        sql.append( wb.getWhere().getSQL() );
                    }
                    sql.append( ") featureids" );
                }
                LOG.debug( "SQL: {}", sql );
                long begin = System.currentTimeMillis();
                stmt = conn.prepareStatement( sql.toString() );
                LOG.debug( "Preparing SELECT took {} [ms] ", System.currentTimeMillis() - begin );

                int i = 1;
                if ( wb.getWhere() != null ) {
                    for ( SQLArgument o : wb.getWhere().getArguments() ) {
                        o.setArgument( stmt, i++ );
                    }
                }

                begin = System.currentTimeMillis();
                rs = stmt.executeQuery();
                LOG.debug( "Executing SELECT took {} [ms] ", System.currentTimeMillis() - begin );
                rs.next();
                hits = rs.getInt( 1 );
            }
        } catch( InvalidParameterValueException e ){
            throw e;
        } catch ( Exception e ) {
            String msg = "Error performing hits query by operator filter: " + e.getMessage();
            LOG.error( msg, e );
            throw new FeatureStoreException( msg, e );
        } finally {
            release( rs, stmt, conn );
        }

        return hits;
    }

    private int queryHitsByOperatorFilterBlob( Query query, QName ftName, OperatorFilter filter )
                            throws FeatureStoreException {

        LOG.debug( "Performing blob query (hits) by operator filter" );

        AbstractWhereBuilder wb = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        int hits = 0;
        try {
            conn = getConnection();

            BlobMapping blobMapping = getSchema().getBlobMapping();

            if ( query.getPrefilterBBox() != null ) {
                OperatorFilter bboxFilter = new OperatorFilter( query.getPrefilterBBox() );
                wb = getWhereBuilderBlob( bboxFilter, conn );
                LOG.debug( "WHERE clause: " + wb.getWhere() );
            }
            String alias = wb != null ? wb.getAliasManager().getRootTableAlias() : "X1";

            StringBuilder sql = new StringBuilder( "SELECT COUNT(*) FROM " );
            sql.append( blobMapping.getTable() );
            sql.append( ' ' );
            sql.append( alias );
            sql.append( " WHERE " );
            sql.append( alias );
            sql.append( "." );
            sql.append( blobMapping.getTypeColumn() );
            sql.append( "=?" );
            if ( wb != null ) {
                sql.append( " AND " );
                sql.append( wb.getWhere().getSQL() );
            }
            LOG.debug( "SQL: {}", sql );
            long begin = System.currentTimeMillis();
            stmt = conn.prepareStatement( sql.toString() );
            LOG.debug( "Preparing SELECT took {} [ms] ", System.currentTimeMillis() - begin );

            int i = 1;
            stmt.setShort( i++, getSchema().getFtId( ftName ) );
            if ( wb != null ) {
                for ( SQLArgument o : wb.getWhere().getArguments() ) {
                    o.setArgument( stmt, i++ );
                }
            }

            begin = System.currentTimeMillis();
            rs = stmt.executeQuery();
            stmt.setFetchSize( fetchSize );
            LOG.debug( "Executing SELECT took {} [ms] ", System.currentTimeMillis() - begin );
            rs.next();
            hits = rs.getInt( 1 );
        } catch ( Exception e ) {
            String msg = "Error performing query by operator filter: " + e.getMessage();
            LOG.error( msg, e );
            throw new FeatureStoreException( msg, e );
        } finally {
            release( rs, stmt, conn );
        }

        return hits;
    }

    @Override
    public int[] queryHits( Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        int[] hits = new int[queries.length];
        for ( int i = 0; i < queries.length; i++ ) {
            hits[i] = queryHits( queries[i] );
        }
        return hits;
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
    public static Geometry getCompatibleGeometry( Geometry literal, ICRS crs )
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

    short getFtId( QName ftName ) {
        return getSchema().getFtId( ftName );
    }

    @Override
    public FeatureInputStream query( Query query )
                            throws FeatureStoreException, FilterEvaluationException {

        if ( query.getTypeNames() == null || query.getTypeNames().length > 1 ) {
            String msg = "Join queries between multiple feature types are not by SQLFeatureStore (yet).";
            throw new UnsupportedOperationException( msg );
        }

        FeatureInputStream result = null;
        Filter filter = query.getFilter();

        if ( query.getTypeNames().length == 1 && ( filter == null || filter instanceof OperatorFilter ) ) {
            QName ftName = query.getTypeNames()[0].getFeatureTypeName();
            FeatureType ft = getSchema().getFeatureType( ftName );
            if ( ft == null ) {
                String msg = "Feature store is not configured to serve feature type '" + ftName + "'.";
                throw new FeatureStoreException( msg );
            }
            result = queryByOperatorFilter( query, ftName, (OperatorFilter) filter );
        } else {
            // must be an id filter based query
            if ( query.getFilter() == null || !( query.getFilter() instanceof IdFilter ) ) {
                String msg = "Invalid query. If no type names are specified, it must contain an IdFilter.";
                throw new FilterEvaluationException( msg );
            }
            result = queryByIdFilter( query.getTypeNames(), (IdFilter) filter, query.getSortProperties() );
        }
        return result;
    }

    @Override
    public FeatureInputStream query( final Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {

        // check for common case: multiple featuretypes, same bbox (WMS), no other filter constraints
        boolean wmsStyleQuery = false;
        Envelope env = queries[0].getPrefilterBBoxEnvelope();
        if ( getSchema().getBlobMapping() != null && queries[0].getFilter() == null
             && queries[0].getSortProperties().length == 0 ) {
            wmsStyleQuery = true;
            for ( int i = 1; i < queries.length; i++ ) {
                Envelope queryBBox = queries[i].getPrefilterBBoxEnvelope();
                if ( queryBBox != env && queries[i].getFilter() != null && queries[i].getSortProperties() != null ) {
                    wmsStyleQuery = false;
                    break;
                }
            }
        }

        if ( wmsStyleQuery ) {
            return queryMultipleFts( queries, env );
        }

        Iterator<FeatureInputStream> rsIter = new Iterator<FeatureInputStream>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < queries.length;
            }

            @Override
            public FeatureInputStream next() {
                if ( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                FeatureInputStream rs;
                try {
                    rs = query( queries[i++] );
                } catch ( InvalidParameterValueException e ){
                    throw e;
                } catch ( Throwable e ) {
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
        return new CombinedFeatureInputStream( rsIter );
    }

    private FeatureInputStream queryByIdFilter( TypeName[] typeNames, IdFilter filter, SortProperty[] sortCrit )
                            throws FeatureStoreException {
        if ( blobMapping != null ) {
            return queryByIdFilterBlob( typeNames, filter, sortCrit );
        }
        return queryByIdFilterRelational( typeNames, filter, sortCrit );
    }

    private FeatureInputStream queryByIdFilterBlob( TypeName[] typeNames,
                                                    IdFilter filter, SortProperty[] sortCrit )
                            throws FeatureStoreException {

        FeatureInputStream result = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();

            StringBuilder sb = new StringBuilder( filter.getMatchingIds().size() * 2 );
            sb.append( "?" );
            for ( int i = 1; i < filter.getMatchingIds().size(); ++i ) {
                sb.append( ",?" );
            }
            long begin = System.currentTimeMillis();
            stmt = conn.prepareStatement( "SELECT gml_id,binary_object FROM " + blobMapping.getTable()
                                          + " A WHERE A.gml_id in (" + sb + ")" );
            LOG.debug( "Preparing SELECT took {} [ms] ", System.currentTimeMillis() - begin );
            stmt.setFetchSize( fetchSize );
            int idx = 0;
            for ( String id : filter.getMatchingIds() ) {
                stmt.setString( ++idx, id );
            }
            begin = System.currentTimeMillis();
            rs = stmt.executeQuery();
            LOG.debug( "Executing SELECT took {} [ms] ", System.currentTimeMillis() - begin );
            FeatureBuilder builder = new FeatureBuilderBlob( this, blobMapping, typeNames );
            result = new IteratorFeatureInputStream( new FeatureResultSetIterator( builder, rs, conn, stmt ) );
        } catch ( Exception e ) {
            release( rs, stmt, conn );
            String msg = "Error performing id query: " + e.getMessage();
            LOG.debug( msg, e );
            throw new FeatureStoreException( msg, e );
        }

        // sort features
        if ( sortCrit.length > 0 ) {
            result = new MemoryFeatureInputStream( Features.sortFc( result.toCollection(), sortCrit ) );
        }
        return result;
    }

    private FeatureInputStream queryByIdFilterRelational( TypeName[] typeNames, IdFilter filter, SortProperty[] sortCrit )
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
            LOG.warn( "No features are returned, as an error occurred during mapping of feature name to id: "
                      + e.getMessage() );
            LOG.trace( e.getMessage(), e );
            return new EmptyFeatureInputStream();
        }

        if ( ftNameToIdAnalysis.size() != 1 ) {
            throw new FeatureStoreException(
                                             "Currently, only relational id queries are supported that target single feature types." );
        }

        QName ftName = ftNameToIdAnalysis.keySet().iterator().next();
        FeatureType ft = getSchema().getFeatureType( ftName );
        checkIfFeatureTypIsRequested( typeNames, ft );
        FeatureTypeMapping ftMapping = getSchema().getFtMapping( ftName );
        FIDMapping fidMapping = ftMapping.getFidMapping();
        List<IdAnalysis> idKernels = ftNameToIdAnalysis.get( ftName );

        FeatureInputStream result = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            long begin = System.currentTimeMillis();
            conn = getConnection();

            String tableAlias = "X1";
            FeatureBuilder builder = new FeatureBuilderRelational( this, ft, ftMapping, conn, tableAlias,
                                                                   nullEscalation );
            List<String> columns = builder.getInitialSelectList();
            StringBuilder sql = new StringBuilder( "SELECT " );
            sql.append( columns.get( 0 ) );
            for ( int i = 1; i < columns.size(); i++ ) {
                sql.append( ',' );
                sql.append( columns.get( i ) );
            }
            sql.append( " FROM " );
            sql.append( ftMapping.getFtTable() );
            sql.append( ' ' );
            sql.append( tableAlias );
            sql.append( " WHERE " );
            boolean first = true;
            for ( IdAnalysis idKernel : idKernels ) {
                if ( !first ) {
                    sql.append( " OR " );
                }
                sql.append( "(" );
                boolean firstCol = true;
                for ( Pair<SQLIdentifier, BaseType> fidColumn : fidMapping.getColumns() ) {
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

            stmt = conn.prepareStatement( sql.toString() );
            stmt.setFetchSize( fetchSize );
            LOG.debug( "Preparing SELECT took {} [ms] ", System.currentTimeMillis() - begin );

            int i = 1;
            for ( IdAnalysis idKernel : idKernels ) {
                int j = 0;
                for ( Object o : idKernel.getIdKernels() ) {
                    PrimitiveType pt = new PrimitiveType( fidMapping.getColumns().get( j++ ).getSecond() );
                    PrimitiveValue value = new PrimitiveValue( o, pt );
                    Object sqlValue = SQLValueMangler.internalToSQL( value );
                    stmt.setObject( i++, sqlValue );
                }
            }

            begin = System.currentTimeMillis();
            rs = stmt.executeQuery();
            LOG.debug( "Executing SELECT took {} [ms] ", System.currentTimeMillis() - begin );
            result = new IteratorFeatureInputStream( new FeatureResultSetIterator( builder, rs, conn, stmt ) );
        } catch ( Exception e ) {
            release( rs, stmt, conn );
            String msg = "Error performing query by id filter (relational mode): " + e.getMessage();
            LOG.error( msg, e );
            throw new FeatureStoreException( msg, e );
        }
        return result;
    }

    protected Connection getConnection()
                            throws SQLException {
        if ( isTransactionActive() ) {
            return transaction.get().getConnection();
        }
        final Connection conn = connProvider.getConnection();
        conn.setAutoCommit( readAutoCommit );
        return conn;
    }

    private void release( final ResultSet rs, final Statement stmt, final Connection conn ) {
        if ( isTransactionActive() ) {
            JDBCUtils.close( rs, stmt, null, LOG );
        } else {
            JDBCUtils.close( rs, stmt, conn, LOG );
        }
    }

    private boolean isTransactionActive() {
        return transaction.get() != null;
    }

    private FeatureInputStream queryByOperatorFilterBlob( Query query, QName ftName, OperatorFilter filter )
                            throws FeatureStoreException {

        LOG.debug( "Performing blob query by operator filter" );

        AbstractWhereBuilder wb = null;
        Connection conn = null;
        FeatureInputStream result = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();

            FeatureTypeMapping ftMapping = getMapping( ftName );
            BlobMapping blobMapping = getSchema().getBlobMapping();
            FeatureBuilder builder = new FeatureBuilderBlob( this, blobMapping );
            List<String> columns = builder.getInitialSelectList();
            if ( query.getPrefilterBBox() != null ) {
                OperatorFilter bboxFilter = new OperatorFilter( query.getPrefilterBBox() );
                wb = getWhereBuilderBlob( bboxFilter, conn );
                LOG.debug( "WHERE clause: " + wb.getWhere() );
            }
            String alias = wb != null ? wb.getAliasManager().getRootTableAlias() : "X1";

            StringBuilder sql = new StringBuilder( "SELECT " );
            sql.append( columns.get( 0 ) );
            for ( int i = 1; i < columns.size(); i++ ) {
                sql.append( ',' );
                sql.append( columns.get( i ) );
            }
            sql.append( " FROM " );
            if ( ftMapping == null ) {
                // pure BLOB query
                sql.append( blobMapping.getTable() );
                sql.append( ' ' );
                sql.append( alias );
                // } else {
                // hybrid query
                // sql.append( blobMapping.getTable() );
                // sql.append( ' ' );
                // sql.append( alias );
                // sql.append( " LEFT OUTER JOIN " );
                // sql.append( ftMapping.getFtTable() );
                // sql.append( ' ' );
                // sql.append( alias );
                // sql.append( " ON " );
                // sql.append( alias );
                // sql.append( "." );
                // sql.append( blobMapping.getInternalIdColumn() );
                // sql.append( "=" );
                // sql.append( alias );
                // sql.append( "." );
                // sql.append( ftMapping.getFidMapping().getColumn() );
            }

            if ( wb != null ) {
                for ( PropertyNameMapping mappedPropName : wb.getMappedPropertyNames() ) {
                    for ( Join join : mappedPropName.getJoins() ) {
                        sql.append( " LEFT OUTER JOIN " );
                        sql.append( join.getToTable() );
                        sql.append( ' ' );
                        sql.append( join.getToTableAlias() );
                        sql.append( " ON " );
                        sql.append( join.getSQLJoinCondition() );
                    }
                }
            }
            sql.append( " WHERE " );
            sql.append( alias );
            sql.append( "." );
            sql.append( blobMapping.getTypeColumn() );
            sql.append( "=?" );
            if ( wb != null ) {
                sql.append( " AND " );
                sql.append( wb.getWhere().getSQL() );
            }

            // if ( wb != null && wb.getWhere() != null ) {
            // if ( blobMapping != null ) {
            // sql.append( " AND " );
            // } else {
            // sql.append( " WHERE " );
            // }
            // sql.append( wb.getWhere().getSQL() );
            // }
            // if ( wb != null && wb.getOrderBy() != null ) {
            // sql.append( " ORDER BY " );
            // sql.append( wb.getOrderBy().getSQL() );
            // }

            LOG.debug( "SQL: {}", sql );
            long begin = System.currentTimeMillis();
            stmt = conn.prepareStatement( sql.toString() );
            LOG.debug( "Preparing SELECT took {} [ms] ", System.currentTimeMillis() - begin );

            int i = 1;
            // if ( blobMapping != null ) {
            stmt.setShort( i++, getSchema().getFtId( ftName ) );
            if ( wb != null ) {
                for ( SQLArgument o : wb.getWhere().getArguments() ) {
                    o.setArgument( stmt, i++ );
                }
            }
            // }
            // if ( wb != null && wb.getWhere() != null ) {
            // for ( SQLArgument o : wb.getWhere().getArguments() ) {
            // o.setArgument( stmt, i++ );
            // }
            // }
            // if ( wb != null && wb.getOrderBy() != null ) {
            // for ( SQLArgument o : wb.getOrderBy().getArguments() ) {
            // o.setArgument( stmt, i++ );
            // }
            // }

            begin = System.currentTimeMillis();
            stmt.setFetchSize( fetchSize );
            rs = stmt.executeQuery();
            LOG.debug( "Executing SELECT took {} [ms] ", System.currentTimeMillis() - begin );

            result = new IteratorFeatureInputStream( new FeatureResultSetIterator( builder, rs, conn, stmt ) );
        } catch ( Exception e ) {
            release( rs, stmt, conn );
            String msg = "Error performing query by operator filter: " + e.getMessage();
            LOG.error( msg, e );
            throw new FeatureStoreException( msg, e );
        }

        if ( filter != null ) {
            LOG.debug( "Applying in-memory post-filtering." );
            result = new FilteredFeatureInputStream( result, filter );
        }

        if ( query.getSortProperties().length > 0 ) {
            LOG.debug( "Applying in-memory post-sorting." );
            result = new MemoryFeatureInputStream( Features.sortFc( result.toCollection(), query.getSortProperties() ) );
        }
        return result;
    }

    private FeatureInputStream queryByOperatorFilter( Query query, QName ftName, OperatorFilter filter )
                            throws FeatureStoreException {

        LOG.debug( "Performing query by operator filter" );

        if ( getSchema().getBlobMapping() != null ) {
            return queryByOperatorFilterBlob( query, ftName, filter );
        }

        AbstractWhereBuilder wb = null;
        Connection conn = null;
        FeatureInputStream result = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        FeatureType ft = getSchema().getFeatureType( ftName );
        FeatureTypeMapping ftMapping = getMapping( ftName );
        if ( ftMapping == null ) {
            String msg = "Cannot perform query on feature type '" + ftName + "'. Feature type is not mapped.";
            throw new FeatureStoreException( msg );
        }

        try {
            conn = getConnection();

            wb = getWhereBuilder( ft, filter, query.getSortProperties(), conn, query.isHandleStrict() );
            String ftTableAlias = wb.getAliasManager().getRootTableAlias();
            LOG.debug( "WHERE clause: " + wb.getWhere() );
            LOG.debug( "ORDER BY clause: " + wb.getOrderBy() );

            FeatureBuilder builder = new FeatureBuilderRelational( this, ft, ftMapping, conn, ftTableAlias,
                                                                   nullEscalation );
            List<String> columns = builder.getInitialSelectList();

            BlobMapping blobMapping = getSchema().getBlobMapping();

            StringBuilder sql = new StringBuilder( "SELECT " );
            sql.append( columns.get( 0 ) );
            for ( int i = 1; i < columns.size(); i++ ) {
                sql.append( ',' );
                sql.append( columns.get( i ) );
            }
            sql.append( " FROM " );

            // pure relational query
            sql.append( ftMapping.getFtTable() );
            sql.append( ' ' );
            sql.append( ftTableAlias );

            for ( PropertyNameMapping mappedPropName : wb.getMappedPropertyNames() ) {
                for ( Join join : mappedPropName.getJoins() ) {
                    sql.append( " LEFT OUTER JOIN " );
                    sql.append( join.getToTable() );
                    sql.append( ' ' );
                    sql.append( join.getToTableAlias() );
                    sql.append( " ON " );
                    sql.append( join.getSQLJoinCondition() );
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
            if ( wb.getWhere() != null ) {
                for ( SQLArgument o : wb.getWhere().getArguments() ) {
                    o.setArgument( stmt, i++ );
                }
            }
            if ( wb.getOrderBy() != null ) {
                for ( SQLArgument o : wb.getOrderBy().getArguments() ) {
                    o.setArgument( stmt, i++ );
                }
            }

            begin = System.currentTimeMillis();
            stmt.setFetchSize( fetchSize );
            rs = stmt.executeQuery();
            LOG.debug( "Executing SELECT took {} [ms] ", System.currentTimeMillis() - begin );

            result = new IteratorFeatureInputStream( new FeatureResultSetIterator( builder, rs, conn, stmt ) );
        } catch ( InvalidParameterValueException e ) {
            release( rs, stmt, conn );
            String msg = "Error performing query by operator filter: " + e.getMessage();
            LOG.error( msg, e );
            throw e;
        } catch ( Exception e ) {
            release( rs, stmt, conn );
            String msg = "Error performing query by operator filter: " + e.getMessage();
            LOG.error( msg, e );
            throw new FeatureStoreException( msg, e );
        }

        if ( wb.getPostFilter() != null ) {
            LOG.debug( "Applying in-memory post-filtering." );
            result = new FilteredFeatureInputStream( result, wb.getPostFilter() );
        }
        if ( wb.getPostSortCriteria() != null ) {
            LOG.debug( "Applying in-memory post-sorting." );
            result = new MemoryFeatureInputStream( Features.sortFc( result.toCollection(), wb.getPostSortCriteria() ) );
        }
        return result;
    }

    private FeatureInputStream queryMultipleFts( Query[] queries, Envelope looseBBox )
                            throws FeatureStoreException {
        FeatureInputStream result = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        AbstractWhereBuilder blobWb = null;
        try {
            if ( looseBBox != null ) {
                final OperatorFilter bboxFilter = new OperatorFilter( new BBOX( looseBBox ) );
                blobWb = getWhereBuilderBlob( bboxFilter, conn );
            }
            conn = getConnection();
            final short[] ftId = getQueriedFeatureTypeIds( queries );
            final StringBuilder sql = new StringBuilder();
            for ( int i = 0; i < ftId.length; i++ ) {
                if ( i > 0 ) {
                    sql.append( " UNION " );
                }
                sql.append( "SELECT gml_id,binary_object" );
                if ( ftId.length > 1 ) {
                    sql.append( "," );
                    sql.append( i );
                    sql.append( " AS QUERY_POS" );
                }
                sql.append( " FROM " );
                sql.append( blobMapping.getTable() );
                sql.append( " WHERE ft_type=?" );
                if ( looseBBox != null ) {
                    sql.append( " AND gml_bounded_by && ?" );
                }
            }
            if ( ftId.length > 1 ) {
                sql.append( " ORDER BY QUERY_POS" );
            }
            stmt = conn.prepareStatement( sql.toString() );
            stmt.setFetchSize( fetchSize );
            int argIdx = 1;
            for ( final short ftId2 : ftId ) {
                stmt.setShort( argIdx++, ftId2 );
                if ( blobWb != null && blobWb.getWhere() != null ) {
                    for ( SQLArgument o : blobWb.getWhere().getArguments() ) {
                        o.setArgument( stmt, argIdx++ );
                    }
                }
            }
            LOG.debug( "Query: {}", sql );
            LOG.debug( "Prepared: {}", stmt );
            rs = stmt.executeQuery();
            final FeatureBuilder builder = new FeatureBuilderBlob( this, blobMapping );
            result = new IteratorFeatureInputStream( new FeatureResultSetIterator( builder, rs, conn, stmt ) );
        } catch ( Exception e ) {
            release( rs, stmt, conn );
            String msg = "Error performing query: " + e.getMessage();
            LOG.debug( msg );
            LOG.trace( "Stack trace:", e );
            throw new FeatureStoreException( msg, e );
        }
        return result;
    }

    private short[] getQueriedFeatureTypeIds( Query[] queries ) {
        short[] ftId = new short[queries.length];
        for ( int i = 0; i < ftId.length; i++ ) {
            Query query = queries[i];
            if ( query.getTypeNames() == null || query.getTypeNames().length > 1 ) {
                String msg = "Join queries between multiple feature types are currently not supported.";
                throw new UnsupportedOperationException( msg );
            }
            ftId[i] = getFtId( query.getTypeNames()[0].getFeatureTypeName() );
        }
        return ftId;
    }

    private AbstractWhereBuilder getWhereBuilder( FeatureType ft, OperatorFilter filter, SortProperty[] sortCrit,
                                                  Connection conn, boolean handleStrict )
                            throws FilterEvaluationException, UnmappableException {
        PropertyNameMapper mapper = new SQLPropertyNameMapper( this, getMapping( ft.getName() ), handleStrict );
        return dialect.getWhereBuilder( mapper, filter, sortCrit, allowInMemoryFiltering );
    }

    private AbstractWhereBuilder getWhereBuilderBlob( OperatorFilter filter, Connection conn )
                            throws FilterEvaluationException, UnmappableException {
        final String undefinedSrid = dialect.getUndefinedSrid();
        PropertyNameMapper mapper = new PropertyNameMapper() {
            @Override
            public PropertyNameMapping getMapping( ValueReference propName, TableAliasManager aliasManager )
                                    throws FilterEvaluationException, UnmappableException {
                GeometryStorageParams geometryParams = new GeometryStorageParams( blobMapping.getCRS(), undefinedSrid,
                                                                                  CoordinateDimension.DIM_2 );
                GeometryMapping bboxMapping = new GeometryMapping( null, false,
                                                                   new DBField( blobMapping.getBBoxColumn() ),
                                                                   GeometryType.GEOMETRY, geometryParams, null );
                return new PropertyNameMapping( getGeometryConverter( bboxMapping ), null, blobMapping.getBBoxColumn(),
                                                aliasManager.getRootTableAlias() );
            }

            @Override
            public PropertyNameMapping getSpatialMapping( ValueReference propName, TableAliasManager aliasManager )
                                    throws FilterEvaluationException, UnmappableException {
                return getMapping( propName, aliasManager );
            }
        };
        return dialect.getWhereBuilder( mapper, filter, null, allowInMemoryFiltering );
    }

    public SQLDialect getDialect() {
        return dialect;
    }

    private class FeatureResultSetIterator extends ResultSetIterator<Feature> {

        private final FeatureBuilder builder;

        private final ResultSet rs;

        private final Connection conn;

        private final Statement stmt;

        public FeatureResultSetIterator( FeatureBuilder builder, ResultSet rs, Connection conn, Statement stmt ) {
            super( rs, conn, stmt );
            this.builder = builder;
            this.rs = rs;
            this.conn = conn;
            this.stmt = stmt;
        }

        @Override
        public void close() {
            release( rs, stmt, conn );
        }

        @Override
        protected Feature createElement( ResultSet rs )
                                throws SQLException {
            return builder.buildFeature( rs );
        }
    }

    @Override
    public ResourceMetadata<? extends Resource> getMetadata() {
        return metadata;
    }

    @Override
    public void init() {
        connProvider = workspace.getResource( ConnectionProviderProvider.class, getConnId() );
        LOG.debug( "init" );

        List<String> resolverClasses = config.getCustomReferenceResolver();
        List<GMLReferenceResolver> resolvers = new ArrayList<GMLReferenceResolver>();
        for ( String resolver : resolverClasses ) {
            try {
                Class<GMLReferenceResolver> clzz = (Class<GMLReferenceResolver>) Class.forName( resolver );
                Constructor<GMLReferenceResolver> cons = clzz.getConstructor( FeatureStore.class );
                GMLReferenceResolver res = cons.newInstance( this );
                resolvers.add( res );
                LOG.info( "Added custom reference resolver {}.", clzz.getSimpleName() );
            } catch ( ClassNotFoundException e ) {
                LOG.warn( "Custom resolver class {} could not be found on the classpath.", resolver );
                LOG.trace( "Stack trace:", e );
            } catch ( NoSuchMethodException e ) {
                LOG.warn( "Custom resolver class {} needs a constructor with a FeatureStore parameter.", resolver );
                LOG.trace( "Stack trace:", e );
            } catch ( SecurityException e ) {
                LOG.warn( "Insufficient rights to instantiate custom resolver class {}.", resolver );
                LOG.trace( "Stack trace:", e );
            } catch ( Throwable e ) {
                LOG.warn( "Could not instantiate custom resolver class {}.", resolver );
                LOG.trace( "Stack trace:", e );
            }
        }
        if ( !resolvers.isEmpty() ) {
            resolvers.add( resolver );
            this.resolver = new CombinedReferenceResolver( resolvers );
        }

        MappedAppSchema schema;
        try {
            schema = AbstractMappedSchemaBuilder.build( configURL.toString(), config, dialect, this.workspace );
        } catch ( Exception t ) {
            throw new ResourceInitException( t.getMessage(), t );
        }

        // lockManager = new DefaultLockManager( this, "LOCK_DB" );

        this.schema = schema;
        this.blobMapping = schema.getBlobMapping();
        initConverters();
        try {
            // however TODO it properly on the DB
            ConnectionProvider conn = this.workspace.getResource( ConnectionProviderProvider.class, "LOCK_DB" );
            lockManager = new DefaultLockManager( this, conn );
        } catch ( Throwable e ) {
            LOG.warn( "Lock manager initialization failed, locking will not be available." );
            LOG.trace( "Stack trace:", e );
        }

        // TODO make this configurable
        FeatureStoreManager fsMgr = this.workspace.getResourceManager( FeatureStoreManager.class );
        if ( fsMgr != null ) {
            this.bboxCache = fsMgr.getBBoxCache();
        } else {
            LOG.warn( "Unmanaged feature store." );
        }

        if ( config.getInspectors() != null ) {
            for ( CustomInspector inspectorConfig : config.getInspectors().getCustomInspector() ) {
                String className = inspectorConfig.getClazz();
                LOG.info( "Adding custom feature inspector '" + className + "' to inspector chain." );
                try {
                    @SuppressWarnings("unchecked")
                    Class<FeatureInspector> inspectorClass = (Class<FeatureInspector>) workspace.getModuleClassLoader().loadClass( className );
                    inspectors.add( inspectorClass.newInstance() );
                } catch ( Exception e ) {
                    String msg = "Unable to instantiate custom feature inspector '" + className + "': "
                                 + e.getMessage();
                    throw new ResourceInitException( msg );
                }
            }
        }

        if ( config.isNullEscalation() == null ) {
            nullEscalation = false;
        } else {
            nullEscalation = config.isNullEscalation();
        }
    }

    public void checkIfFeatureTypIsRequested( TypeName[] typeNames, FeatureType ft ) {
        if ( typeNames != null && typeNames.length > 0 ) {
            boolean isFeatureTypeRequested = false;
            for ( TypeName typeName : typeNames ) {
                if ( typeName.getFeatureTypeName().equals( ft.getName() ) )
                    isFeatureTypeRequested = true;
            }
            if ( !isFeatureTypeRequested )
                throw new InvalidParameterValueException( "Requested feature does not match the requested feature type.",
                                                          "RESOURCEID" );
        }
    }

}