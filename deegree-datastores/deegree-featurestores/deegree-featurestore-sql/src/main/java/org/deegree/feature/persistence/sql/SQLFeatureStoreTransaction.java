//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.feature.persistence.sql;

import static org.deegree.feature.Features.findFeaturesAndGeometries;
import static org.deegree.feature.i18n.Messages.getMessage;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.deegree.protocol.wfs.transaction.action.IDGenMode.USE_EXISTING;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.commons.tom.sql.SQLValueMangler;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.BBoxTracker;
import org.deegree.feature.persistence.FeatureInspector;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.Lock;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.blob.BlobCodec;
import org.deegree.feature.persistence.sql.blob.BlobMapping;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.id.IdAnalysis;
import org.deegree.feature.persistence.sql.insert.FeatureRow;
import org.deegree.feature.persistence.sql.insert.InsertRowManager;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.FeatureMapping;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.feature.persistence.transaction.FeatureUpdater;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.ResourceId;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometries;
import org.deegree.geometry.Geometry;
import org.deegree.protocol.wfs.transaction.action.IDGenMode;
import org.deegree.protocol.wfs.transaction.action.ParsedPropertyReplacement;
import org.deegree.protocol.wfs.transaction.action.UpdateAction;
import org.deegree.sqldialect.filter.DBField;
import org.deegree.sqldialect.filter.MappingExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStoreTransaction} implementation for {@link SQLFeatureStore}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class SQLFeatureStoreTransaction implements FeatureStoreTransaction {

    private static final Logger LOG = LoggerFactory.getLogger( SQLFeatureStoreTransaction.class );

    private final SQLFeatureStore fs;

    private final MappedAppSchema schema;

    private final BlobMapping blobMapping;

    private final Connection conn;

    private final List<FeatureInspector> inspectors;

    private final BBoxTracker bboxTracker;

    // TODO
    private ParticleConverter<Geometry> blobGeomConverter;

    /**
     * Creates a new {@link SQLFeatureStoreTransaction} instance.
     *
     * @param store
     *            corresponding feature store instance, must not be <code>null</code>
     * @param conn
     *            JDBC connection associated with the transaction, must not be <code>null</code> and have
     *            <code>autocommit</code> set to <code>false</code>
     * @param schema
     *            application schema with mapping information, must not be <code>null</code>
     * @param inspectors
     *            feature inspectors, must not be <code>null</code>
     */
    SQLFeatureStoreTransaction( SQLFeatureStore store, Connection conn, MappedAppSchema schema,
                                List<FeatureInspector> inspectors ) {
        this.fs = store;
        this.conn = conn;
        this.schema = schema;
        this.inspectors = inspectors;
        blobMapping = schema.getBlobMapping();
        if ( blobMapping != null ) {
            DBField bboxColumn = new DBField( blobMapping.getBBoxColumn() );
            GeometryStorageParams geometryParams = new GeometryStorageParams( blobMapping.getCRS(), null, DIM_2 );
            GeometryMapping blobGeomMapping = new GeometryMapping( null, true, bboxColumn, GeometryType.GEOMETRY,
                                                                   geometryParams, null );
            blobGeomConverter = fs.getGeometryConverter( blobGeomMapping );
        }
        this.bboxTracker = new BBoxTracker();
    }

    @Override
    public void commit()
                            throws FeatureStoreException {

        LOG.debug( "Committing transaction." );
        try {
            conn.commit();
            updateBBoxCache();
        } catch ( Throwable t ) {
            LOG.debug( t.getMessage(), t );
            throw new FeatureStoreException( "Unable to commit SQL transaction: " + t.getMessage() );
        } finally {
            fs.closeAndDetachTransactionConnection();
        }
    }

    private void updateBBoxCache()
                            throws FeatureStoreException {

        // beware of concurrent transactions
        synchronized ( fs ) {

            Set<QName> recalcFTs = bboxTracker.getRecalcFeatureTypes();
            Map<QName, Envelope> ftNamesToIncreaseBBoxes = bboxTracker.getIncreaseBBoxes();

            // handle bbox increases
            for ( Entry<QName, Envelope> ftNameToIncreaseBBox : ftNamesToIncreaseBBoxes.entrySet() ) {
                QName ftName = ftNameToIncreaseBBox.getKey();
                Envelope bbox = null;
                if ( fs.getBBoxCache().contains( ftName ) ) {
                    bbox = ftNameToIncreaseBBox.getValue();
                }
                if ( bbox != null ) {
                    Envelope oldBbox = fs.getBBoxCache().get( ftName );
                    if ( oldBbox != null ) {
                        bbox = oldBbox.merge( bbox );
                    }
                    fs.getBBoxCache().set( ftName, bbox );
                }
            }

            // TODO configuration switch for bbox recalculation strategy
            if ( !recalcFTs.isEmpty() ) {
                LOG.debug( "Full recalculation of feature type envelopes required. Delete 'bbox_cache.properties' if you need minimal envelopes." );
            }

            try {
                fs.getBBoxCache().persist();
            } catch ( Throwable t ) {
                LOG.error( "Unable to persist bbox cache: " + t.getMessage() );
            }
        }
    }

    @Override
    public void rollback()
                            throws FeatureStoreException {
        LOG.debug( "Performing rollback of transaction." );
        try {
            conn.rollback();
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new FeatureStoreException( "Unable to rollback SQL transaction: " + e.getMessage() );
        } finally {
            fs.closeAndDetachTransactionConnection();
        }
    }

    @Override
    public FeatureStore getStore() {
        return fs;
    }

    /**
     * Returns the underlying JDBC connection. Can be used for performing other operations in the same transaction
     * context.
     *
     * @return the underlying JDBC connection, never <code>null</code>
     */
    public Connection getConnection() {
        return conn;
    }

    @Override
    public int performDelete( QName ftName, OperatorFilter filter, Lock lock )
                            throws FeatureStoreException {
        // TODO implement this more efficiently
        return performDelete( getIdFilter( ftName, filter ), lock );
    }

    @Override
    public int performDelete( IdFilter filter, Lock lock )
                            throws FeatureStoreException {
        int deleted = 0;
        if ( blobMapping != null ) {
            deleted = performDeleteBlob( filter, lock );
        } else {
            deleted = performDeleteRelational( filter, lock );
        }

        // TODO improve this
        for ( FeatureType ft : schema.getFeatureTypes( null, false, false ) ) {
            bboxTracker.delete( ft.getName() );
        }

        return deleted;
    }

    private int performDeleteBlob( IdFilter filter, Lock lock )
                            throws FeatureStoreException {
        checkIfFeaturesAreNotLocked( filter, lock );
        
        int deleted = 0;
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement( "DELETE FROM " + blobMapping.getTable() + " WHERE "
                                          + blobMapping.getGMLIdColumn() + "=?" );
            for ( ResourceId id : filter.getSelectedIds() ) {
                stmt.setString( 1, id.getRid() );
                stmt.addBatch();
                if ( fs.getCache() != null ) {
                    fs.getCache().remove( id.getRid() );
                }
            }
            int[] deletes = stmt.executeBatch();
            for ( int noDeleted : deletes ) {
                deleted += noDeleted;
            }
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new FeatureStoreException( e.getMessage(), e );
        } finally {
            JDBCUtils.close( stmt );
        }
        LOG.debug( "Deleted " + deleted + " features." );
        return deleted;
    }

    private int performDeleteRelational( IdFilter filter, Lock lock )
                            throws FeatureStoreException {
        checkIfFeaturesAreNotLocked( filter, lock );
        
        int deleted = 0;
        for ( ResourceId id : filter.getSelectedIds() ) {
            LOG.debug( "Analyzing id: " + id.getRid() );
            IdAnalysis analysis = null;
            try {
                analysis = schema.analyzeId( id.getRid() );
                LOG.debug( "Analysis: " + analysis );
                if ( !schema.getKeyDependencies().getDeleteCascadingByDB() ) {
                    LOG.debug( "Deleting joined rows manually." );
                    deleteJoinedRows( analysis );
                } else {
                    LOG.debug( "Depending on database to delete joined rows automatically." );
                }
                deleted += deleteFeatureRow( analysis );
            } catch ( IllegalArgumentException e ) {
                throw new FeatureStoreException( "Unable to determine feature type for id '" + id + "'." );
            }
        }
        return deleted;
    }

    private int deleteFeatureRow( IdAnalysis analysis )
                            throws FeatureStoreException {
        int deleted = 0;
        FeatureTypeMapping ftMapping = schema.getFtMapping( analysis.getFeatureType().getName() );
        FIDMapping fidMapping = ftMapping.getFidMapping();
        PreparedStatement stmt = null;
        try {
            StringBuilder sql = new StringBuilder( "DELETE FROM " + ftMapping.getFtTable() + " WHERE " );
            sql.append( fidMapping.getColumns().get( 0 ).first );
            sql.append( "=?" );
            for ( int i = 1; i < fidMapping.getColumns().size(); i++ ) {
                sql.append( " AND " );
                sql.append( fidMapping.getColumns().get( i ) );
                sql.append( "=?" );
            }
            stmt = conn.prepareStatement( sql.toString() );

            int i = 1;
            for ( String fidKernel : analysis.getIdKernels() ) {
                PrimitiveType pt = new PrimitiveType( fidMapping.getColumns().get( i - 1 ).second );
                PrimitiveValue value = new PrimitiveValue( fidKernel, pt );
                Object sqlValue = SQLValueMangler.internalToSQL( value );
                stmt.setObject( i++, sqlValue );
            }
            LOG.debug( "Executing: " + stmt );
            deleted += stmt.executeUpdate();
        } catch ( Throwable e ) {
            LOG.error( e.getMessage(), e );
            throw new FeatureStoreException( e.getMessage(), e );
        } finally {
            JDBCUtils.close( stmt );
        }
        return deleted;
    }

    /**
     * Deletes the joined rows for the specified feature id.
     * <p>
     * Deletes all joined rows and transitive join rows, but stops at joins to subfeature tables.
     * </p>
     *
     * @param fid
     *            feature id, must not be <code>null</code>
     * @throws FeatureStoreException
     */
    private void deleteJoinedRows( IdAnalysis fid )
                            throws FeatureStoreException {

        Map<SQLIdentifier, Object> keyColsToValues = new HashMap<SQLIdentifier, Object>();

        FeatureTypeMapping ftMapping = schema.getFtMapping( fid.getFeatureType().getName() );

        // add values for feature id columns
        int i = 0;
        for ( Pair<SQLIdentifier, BaseType> fidColumns : ftMapping.getFidMapping().getColumns() ) {
            PrimitiveType pt = new PrimitiveType( fidColumns.second );
            PrimitiveValue value = new PrimitiveValue( fid.getIdKernels()[i], pt );
            Object sqlValue = SQLValueMangler.internalToSQL( value );
            keyColsToValues.put( fidColumns.first, sqlValue );
            i++;
        }

        // traverse mapping particles
        for ( Mapping particle : ftMapping.getMappings() ) {
            deleteJoinedRows( particle, keyColsToValues );
        }
    }

    private void deleteJoinedRows( Mapping particle, Map<SQLIdentifier, Object> keyColToValue )
                            throws FeatureStoreException {

        // TODO: After FeatureTypeJoin is introduced, rework this case (may allow joins)
        if ( particle instanceof FeatureMapping ) {
            return;
        }

        // determine and delete joined rows
        if ( particle.getJoinedTable() != null && !particle.getJoinedTable().isEmpty() ) {
            TableJoin tableJoin = particle.getJoinedTable().get( 0 );

            PreparedStatement stmt = null;
            try {
                StringBuilder sql = new StringBuilder( "SELECT " );
                boolean first = true;
                for ( SQLIdentifier selectColumn : tableJoin.getToColumns() ) {
                    if ( !first ) {
                        sql.append( ',' );
                    } else {
                        first = false;
                    }
                    sql.append( "X2." );
                    sql.append( selectColumn );
                }
                sql.append( " FROM " );
                sql.append( tableJoin.getFromTable() );
                sql.append( " X1," );
                sql.append( tableJoin.getToTable() );
                sql.append( " X2" );
                sql.append( " WHERE" );

                first = true;
                int i = 0;
                for ( SQLIdentifier fromColumn : tableJoin.getFromColumns() ) {
                    SQLIdentifier toColumn = tableJoin.getToColumns().get( i++ );
                    if ( !first ) {
                        sql.append( ',' );
                    } else {
                        first = false;
                    }
                    sql.append( " X1." );
                    sql.append( fromColumn );
                    sql.append( "=" );
                    sql.append( "X2." );
                    sql.append( toColumn );
                    first = false;
                }

                for ( Entry<SQLIdentifier, Object> joinKey : keyColToValue.entrySet() ) {
                    sql.append( " AND X1." );
                    sql.append( joinKey.getKey() );
                    sql.append( "=?" );
                    first = false;
                }

                stmt = conn.prepareStatement( sql.toString() );

                i = 1;
                for ( Entry<SQLIdentifier, Object> joinKey : keyColToValue.entrySet() ) {
                    stmt.setObject( i++, joinKey.getValue() );
                }
                LOG.debug( "Executing SELECT (following join): " + stmt );
                ResultSet rs = stmt.executeQuery();
                while ( rs.next() ) {
                    Map<SQLIdentifier, Object> joinKeyToValue = new HashMap<SQLIdentifier, Object>();
                    i = 1;
                    for ( SQLIdentifier toColumn : tableJoin.getToColumns() ) {
                        joinKeyToValue.put( toColumn, rs.getObject( i++ ) );
                    }
                    deleteJoinedRows( particle, tableJoin, joinKeyToValue );
                }
            } catch ( SQLException e ) {
                LOG.error( e.getMessage(), e );
                throw new FeatureStoreException( e.getMessage(), e );
            } finally {
                JDBCUtils.close( stmt );
            }
        } else {
            // process compound particle structure
            if ( particle instanceof CompoundMapping ) {
                CompoundMapping cm = (CompoundMapping) particle;
                for ( Mapping child : cm.getParticles() ) {
                    deleteJoinedRows( child, keyColToValue );
                }
            }
        }
    }

    private void deleteJoinedRows( Mapping particle, TableJoin tableJoin, Map<SQLIdentifier, Object> joinKeyColToValue )
                            throws FeatureStoreException {

        TableName joinTable = tableJoin.getToTable();

        if ( particle instanceof CompoundMapping ) {
            CompoundMapping cm = (CompoundMapping) particle;
            for ( Mapping child : cm.getParticles() ) {
                deleteJoinedRows( child, joinKeyColToValue );
            }
        }

        // DELETE join rows
        PreparedStatement stmt = null;
        try {
            StringBuilder sql = new StringBuilder( "DELETE FROM " + joinTable + " WHERE" );

            boolean first = true;
            for ( Entry<SQLIdentifier, Object> joinKey : joinKeyColToValue.entrySet() ) {
                if ( !first ) {
                    sql.append( " AND" );
                }
                sql.append( ' ' );
                sql.append( joinKey.getKey() );
                sql.append( "=?" );
                first = false;
            }

            stmt = conn.prepareStatement( sql.toString() );

            int i = 1;
            for ( Entry<SQLIdentifier, Object> joinKey : joinKeyColToValue.entrySet() ) {
                stmt.setObject( i++, joinKey.getValue() );
            }
            LOG.debug( "Executing DELETE (joined rows): " + stmt );
            stmt.executeUpdate();
        } catch ( SQLException e ) {
            LOG.error( e.getMessage(), e );
            throw new FeatureStoreException( e.getMessage(), e );
        } finally {
            JDBCUtils.close( stmt );
        }
    }

    @Override
    public List<String> performInsert( FeatureCollection fc, IDGenMode mode )
                            throws FeatureStoreException {

        LOG.debug( "performInsert()" );

        Set<Geometry> geometries = new LinkedHashSet<Geometry>();
        Set<Feature> features = new LinkedHashSet<Feature>();
        Set<String> fids = new LinkedHashSet<String>();
        Set<String> gids = new LinkedHashSet<String>();
        for ( Feature member : fc ) {
            findFeaturesAndGeometries( member, geometries, features, fids, gids );
        }

        LOG.debug( features.size() + " features / " + geometries.size() + " geometries" );

        for ( FeatureInspector inspector : inspectors ) {
            for ( Feature f : features ) {
                // TODO cope with inspectors that return a different instance
                inspector.inspect( f, this );
            }
        }

        long begin = System.currentTimeMillis();

        String fid = null;
        try {
            PreparedStatement blobInsertStmt = null;
            if ( blobMapping != null ) {
                switch ( mode ) {
                case GENERATE_NEW: {
                    // TODO don't change incoming features / geometries
                    for ( Feature feature : features ) {
                        String newFid = "FEATURE_" + generateNewId();
                        String oldFid = feature.getId();
                        if ( oldFid != null ) {
                            fids.remove( oldFid );
                        }
                        fids.add( newFid );
                        feature.setId( newFid );
                    }
                    for ( Geometry geometry : geometries ) {
                        String newGid = "GEOMETRY_" + generateNewId();
                        String oldGid = geometry.getId();
                        if ( oldGid != null ) {
                            gids.remove( oldGid );
                        }
                        gids.add( newGid );
                        geometry.setId( newGid );
                    }
                    break;
                }
                case REPLACE_DUPLICATE: {
                    throw new FeatureStoreException( "REPLACE_DUPLICATE is not available yet." );
                }
                case USE_EXISTING: {
                    // TODO don't change incoming features / geometries
                    for ( Feature feature : features ) {
                        if ( feature.getId() == null ) {
                            String newFid = "FEATURE_" + generateNewId();
                            feature.setId( newFid );
                            fids.add( newFid );
                        }
                    }

                    for ( Geometry geometry : geometries ) {
                        if ( geometry.getId() == null ) {
                            String newGid = "GEOMETRY_" + generateNewId();
                            geometry.setId( newGid );
                            gids.add( newGid );
                        }
                    }
                    break;
                }
                }
                StringBuilder sql = new StringBuilder( "INSERT INTO " );
                sql.append( blobMapping.getTable() );
                sql.append( " (" );
                sql.append( blobMapping.getGMLIdColumn() );
                sql.append( "," );
                sql.append( blobMapping.getTypeColumn() );
                sql.append( "," );
                sql.append( blobMapping.getDataColumn() );
                sql.append( "," );
                sql.append( blobMapping.getBBoxColumn() );
                sql.append( ") VALUES(?,?,?," );
                sql.append( blobGeomConverter.getSetSnippet( null ) );
                sql.append( ")" );
                LOG.debug( "Inserting: {}", sql );
                blobInsertStmt = conn.prepareStatement( sql.toString() );
                for ( Feature feature : features ) {
                    fid = feature.getId();
                    if ( blobInsertStmt != null ) {
                        insertFeatureBlob( blobInsertStmt, feature );
                    }
                    FeatureTypeMapping ftMapping = fs.getMapping( feature.getName() );
                    if ( ftMapping != null ) {
                        throw new UnsupportedOperationException();
                    }
                    ICRS storageSrs = blobMapping.getCRS();
                    bboxTracker.insert( feature, storageSrs );
                }
                if ( blobInsertStmt != null ) {
                    blobInsertStmt.close();
                }
            } else {
                // pure relational mode
                List<FeatureRow> idAssignments = new ArrayList<FeatureRow>();
                InsertRowManager insertManager = new InsertRowManager( fs, conn, mode );
                for ( Feature feature : features ) {
                    FeatureTypeMapping ftMapping = fs.getMapping( feature.getName() );
                    if ( ftMapping == null ) {
                        continue;
//                        throw new FeatureStoreException( "Cannot insert feature of type '" + feature.getName()
//                                                         + "'. No mapping defined and BLOB mode is off." );
                    }
                    idAssignments.add( insertManager.insertFeature( feature, ftMapping ) );
                    Pair<TableName, GeometryMapping> mapping = ftMapping.getDefaultGeometryMapping();
                    if ( mapping != null ) {
                        ICRS storageSrs = mapping.second.getCRS();
                        bboxTracker.insert( feature, storageSrs );
                    }
                }
                if ( insertManager.getDelayedRows() != 0 ) {
                    String msg = "After insertion, " + insertManager.getDelayedRows()
                                 + " delayed rows left uninserted. Probably a cyclic key constraint blocks insertion.";
                    throw new RuntimeException( msg );
                }
                // TODO why is this necessary?
                fids.clear();
                for ( FeatureRow assignment : idAssignments ) {
                    fids.add( assignment.getNewId() );
                }
            }
        } catch ( Throwable t ) {
            String msg = "Error inserting feature: " + t.getMessage();
            LOG.error( msg );
            LOG.trace( "Stack trace:", t );
            throw new FeatureStoreException( msg, t );
        }

        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Insertion of " + features.size() + " features: " + elapsed + " [ms]" );
        return new ArrayList<String>( fids );
    }

    private String generateNewId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Inserts the given feature into BLOB table and returns the generated primary key.
     *
     * @param stmt
     * @param feature
     * @return primary key of the feature
     * @throws SQLException
     * @throws FeatureStoreException
     */
    private int insertFeatureBlob( PreparedStatement stmt, Feature feature )
                            throws SQLException, FeatureStoreException {
        LOG.debug( "Inserting feature with id '" + feature.getId() + "' (BLOB)" );
        if ( fs.getSchema().getFeatureType( feature.getName() ) == null ) {
            throw new FeatureStoreException( "Cannot insert feature '" + feature.getName()
                                             + "': feature type is not served by this feature store." );
        }
        ICRS crs = blobMapping.getCRS();
        stmt.setString( 1, feature.getId() );
        stmt.setShort( 2, fs.getFtId( feature.getName() ) );
        byte[] bytes = encodeFeatureBlob( feature, crs );
        stmt.setBytes( 3, bytes );
        LOG.debug( "Feature blob size: " + bytes.length );
        Geometry bboxGeom = getFeatureEnvelopeAsGeometry( feature );
        blobGeomConverter.setParticle( stmt, bboxGeom, 4 );
        stmt.execute();
        int internalId = -1;
        // ResultSet rs = null;
        // try {
        // // TODO only supported for PostgreSQL >= 8.2
        // rs = stmt.getGeneratedKeys();
        // rs.next();
        // internalId = rs.getInt( 1 );
        // } finally {
        // if ( rs != null ) {
        // rs.close();
        // }
        // }
        return internalId;
    }

    @Override
    public List<String> performUpdate( QName ftName, List<ParsedPropertyReplacement> replacementProps, Filter filter,
                                       Lock lock )
                            throws FeatureStoreException {
        LOG.debug( "Updating feature type '" + ftName + "', filter: " + filter + ", replacement properties: "
                   + replacementProps.size() );
        List<String> updatedFids = null;
        if ( blobMapping != null ) {
            updatedFids = performUpdateBlob( ftName, replacementProps, filter, lock );
        } else {
            updatedFids = performUpdateRelational( ftName, replacementProps, filter );
        }
        bboxTracker.update( ftName );
        return updatedFids;
    }

    private List<String> performUpdateBlob( final QName ftName, final List<ParsedPropertyReplacement> replacementProps,
                                            final Filter filter, final Lock lock )
                            throws FeatureStoreException {
        final List<String> updatedFids = new ArrayList<String>();
        final Query query = new Query( ftName, filter, -1, -1, -1 );
        final StringBuilder sql = new StringBuilder( "UPDATE " );
        sql.append( blobMapping.getTable() );
        sql.append( " SET " );
        sql.append( blobMapping.getDataColumn() );
        sql.append( "=?," );
        sql.append( blobMapping.getBBoxColumn() );
        sql.append( "=? WHERE " );
        sql.append( blobMapping.getGMLIdColumn() );
        sql.append( "=?" );
        PreparedStatement blobUpdateStmt = null;
        FeatureInputStream features = null;
        try {
            LOG.debug( "Preparing update stmt: {}", sql );
            blobUpdateStmt = conn.prepareStatement( sql.toString() );
            features = fs.query( query );
            for ( final Feature feature : features ) {
                new FeatureUpdater().update( feature, replacementProps );
                updateFeatureBlob( blobUpdateStmt, feature );
                updatedFids.add( feature.getId() );
            }
        } catch ( final Exception e ) {
            final String msg = "Error while performing Update (BLOB): " + e.getMessage();
            LOG.trace( msg, e );
            throw new FeatureStoreException( msg );
        } finally {
            if ( features != null ) {
                features.close();
            }
        }
        return updatedFids;
    }

    private void updateFeatureBlob( final PreparedStatement stmt, final Feature feature )
                            throws SQLException {
        LOG.debug( "Updating feature with id '" + feature.getId() + "' (BLOB)" );
        final ICRS crs = blobMapping.getCRS();
        final byte[] bytes = encodeFeatureBlob( feature, crs );
        stmt.setBytes( 1, bytes );
        LOG.debug( "Feature blob size: " + bytes.length );
        final Geometry bboxGeom = getFeatureEnvelopeAsGeometry( feature );
        blobGeomConverter.setParticle( stmt, bboxGeom, 2 );
        stmt.setString( 3, feature.getId() );
        stmt.execute();
    }

    private Geometry getFeatureEnvelopeAsGeometry( final Feature feature ) {
        Geometry bboxGeom = null;
        try {
            Envelope bbox = feature.getEnvelope();
            if ( bbox != null ) {
                bboxGeom = Geometries.getAsGeometry( bbox );
            }
        } catch ( Exception e ) {
            LOG.warn( "Unable to determine bbox of feature with id '" + feature.getId() + "': " + e.getMessage() );
        }
        return bboxGeom;
    }

    private byte[] encodeFeatureBlob( final Feature feature, ICRS crs )
                            throws FactoryConfigurationError, SQLException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            final BlobCodec codec = fs.getSchema().getBlobMapping().getCodec();
            codec.encode( feature, fs.getNamespaceContext(), bos, crs );
        } catch ( Exception e ) {
            String msg = "Error encoding feature for BLOB: " + e.getMessage();
            LOG.error( msg );
            LOG.trace( "Stack trace:", e );
            throw new SQLException( msg, e );
        }
        byte[] bytes = bos.toByteArray();
        return bytes;
    }

    private List<String> performUpdateRelational( QName ftName, List<ParsedPropertyReplacement> replacementProps,
                                                  Filter filter )
                            throws FeatureStoreException {
        IdFilter idFilter = null;
        try {
            if ( filter instanceof IdFilter ) {
                idFilter = (IdFilter) filter;
            } else {
                idFilter = getIdFilter( ftName, (OperatorFilter) filter );
            }
        } catch ( Exception e ) {
            LOG.debug( e.getMessage(), e );
        }
        List<String> updated = null;
        if ( blobMapping != null ) {
            throw new FeatureStoreException( "Updates in SQLFeatureStore (BLOB mode) are currently not implemented." );
        } else {
            try {
                updated = performUpdateRelational( ftName, replacementProps, idFilter );
                if ( fs.getCache() != null ) {
                    for ( ResourceId id : idFilter.getSelectedIds() ) {
                        fs.getCache().remove( id.getRid() );
                    }
                }
            } catch ( Exception e ) {
                LOG.debug( e.getMessage(), e );
                throw new FeatureStoreException( e.getMessage(), e );
            }
        }
        return updated;
    }

    private List<String> performUpdateRelational( QName ftName, List<ParsedPropertyReplacement> replacementProps,
                                                  IdFilter filter )
                            throws FeatureStoreException, FilterEvaluationException {

        FeatureTypeMapping ftMapping = schema.getFtMapping( ftName );
        FIDMapping fidMapping = ftMapping.getFidMapping();

        int updated = 0;
        PreparedStatement stmt = null;
        try {
            String sql = createRelationalUpdateStatement( ftMapping, fidMapping, replacementProps,
                                                          filter.getSelectedIds() );

            if ( sql != null ) {
                LOG.debug( "Update: " + sql );
                stmt = conn.prepareStatement( sql.toString() );
                setRelationalUpdateValues( replacementProps, ftMapping, stmt, filter, fidMapping );
                int[] updates = stmt.executeBatch();
                for ( int noUpdated : updates ) {
                    updated += noUpdated;
                }
            }
        } catch ( SQLException e ) {
            JDBCUtils.log( e, LOG );
            throw new FeatureStoreException( JDBCUtils.getMessage( e ), e );
        } finally {
            JDBCUtils.close( stmt );
        }
        LOG.debug( "Updated {} features.", updated );
        return new ArrayList<String>( filter.getMatchingIds() );
    }

    private void setRelationalUpdateValues( List<ParsedPropertyReplacement> replacementProps,
                                            FeatureTypeMapping ftMapping, PreparedStatement stmt, IdFilter filter,
                                            FIDMapping fidMapping )
                            throws SQLException {
        int i = 1;

        for ( ParsedPropertyReplacement replacement : replacementProps ) {
            Property replacementProp = replacement.getNewValue();
            QName propName = replacementProp.getType().getName();
            Mapping mapping = ftMapping.getMapping( propName );
            if ( mapping != null ) {
                if ( mapping.getJoinedTable() != null && !mapping.getJoinedTable().isEmpty() ) {
                    continue;
                }

                Object value = replacementProp.getValue();
                if ( value != null ) {
                    ParticleConverter<TypedObjectNode> converter = (ParticleConverter<TypedObjectNode>) fs.getConverter( mapping );
                    if ( mapping instanceof PrimitiveMapping ) {
                        MappingExpression me = ( (PrimitiveMapping) mapping ).getMapping();
                        if ( !( me instanceof DBField ) ) {
                            continue;
                        }
                        converter.setParticle( stmt, (PrimitiveValue) value, i++ );
                    } else if ( mapping instanceof GeometryMapping ) {
                        MappingExpression me = ( (GeometryMapping) mapping ).getMapping();
                        if ( !( me instanceof DBField ) ) {
                            continue;
                        }
                        converter.setParticle( stmt, (Geometry) value, i++ );
                    }
                } else {
                    stmt.setObject( i++, null );
                }
            }
        }

        for ( String id : filter.getMatchingIds() ) {
            IdAnalysis analysis = schema.analyzeId( id );
            int j = i;
            for ( String fidKernel : analysis.getIdKernels() ) {
                PrimitiveValue value = new PrimitiveValue( fidKernel, new PrimitiveType( fidMapping.getColumnType() ) );
                Object sqlValue = SQLValueMangler.internalToSQL( value );
                stmt.setObject( j++, sqlValue );
            }
            stmt.addBatch();
        }
    }

    private String createRelationalUpdateStatement( FeatureTypeMapping ftMapping, FIDMapping fidMapping,
                                                    List<ParsedPropertyReplacement> replacementProps,
                                                    List<ResourceId> list )
                            throws FilterEvaluationException, FeatureStoreException, SQLException {
        StringBuffer sql = new StringBuffer( "UPDATE " );
        sql.append( ftMapping.getFtTable() );
        sql.append( " SET " );
        boolean first = true;
        for ( ParsedPropertyReplacement replacement : replacementProps ) {
            Property replacementProp = replacement.getNewValue();
            QName propName = replacementProp.getType().getName();
            Mapping mapping = ftMapping.getMapping( propName );
            if ( mapping != null ) {
                if ( mapping.getJoinedTable() != null && !mapping.getJoinedTable().isEmpty() ) {
                    addRelationallyMappedMultiProperty( replacement, mapping, ftMapping, list );
                    continue;
                }
                String column = null;
                ParticleConverter<TypedObjectNode> converter = (ParticleConverter<TypedObjectNode>) fs.getConverter( mapping );
                if ( mapping instanceof PrimitiveMapping ) {
                    MappingExpression me = ( (PrimitiveMapping) mapping ).getMapping();
                    if ( !( me instanceof DBField ) ) {
                        continue;
                    }
                    column = ( (DBField) me ).getColumn();
                    if ( !first ) {
                        sql.append( "," );
                    } else {
                        first = false;
                    }
                    sql.append( column );
                    sql.append( "=" );

                    // TODO communicate value for non-prepared statement converters
                    sql.append( converter.getSetSnippet( null ) );
                } else if ( mapping instanceof GeometryMapping ) {
                    MappingExpression me = ( (GeometryMapping) mapping ).getMapping();
                    if ( !( me instanceof DBField ) ) {
                        continue;
                    }
                    column = ( (DBField) me ).getColumn();
                    if ( !first ) {
                        sql.append( "," );
                    } else {
                        first = false;
                    }
                    sql.append( column );
                    sql.append( "=" );
                    // TODO communicate value for non-prepared statement converters
                    sql.append( converter.getSetSnippet( null ) );
                } else {
                    LOG.warn( "Updating of " + mapping.getClass() + " is currently not implemented. Omitting." );
                    continue;
                }
            } else {
                LOG.warn( "No mapping for update property '" + propName + "'. Omitting." );
            }
        }

        // only property changes in multi properties?
        if ( first ) {
            return null;
        }

        sql.append( " WHERE " );
        sql.append( fidMapping.getColumns().get( 0 ).first );
        sql.append( "=?" );
        for ( int i = 1; i < fidMapping.getColumns().size(); i++ ) {
            sql.append( " AND " );
            sql.append( fidMapping.getColumns().get( i ) );
            sql.append( "=?" );
        }
        return sql.toString();
    }

    private void addRelationallyMappedMultiProperty( ParsedPropertyReplacement replacement, Mapping mapping,
                                                     FeatureTypeMapping ftMapping, List<ResourceId> list )
                            throws FilterEvaluationException, FeatureStoreException, SQLException {
        UpdateAction action = replacement.getUpdateAction();
        if ( action == null ) {
            action = UpdateAction.INSERT_AFTER;
        }
        switch ( action ) {
        case INSERT_BEFORE:
        case REMOVE:
        case REPLACE:
            LOG.warn( "Updating of multi properties is currently only supported for 'insertAfter' update action. Omitting." );
            break;
        case INSERT_AFTER:
            break;
        default:
            break;
        }
        InsertRowManager mgr = new InsertRowManager( fs, conn, null );
        List<Property> props = Collections.singletonList( replacement.getNewValue() );
        for ( ResourceId id : list ) {
            IdAnalysis analysis = schema.analyzeId( id.getRid() );
            FeatureType featureType = schema.getFeatureType( ftMapping.getFeatureType() );
            Feature f = featureType.newFeature( id.getRid(), props, null );
            mgr.updateFeature( f, ftMapping, analysis.getIdKernels(), mapping, replacement );
        }
    }

    private IdFilter getIdFilter( QName ftName, OperatorFilter filter )
                            throws FeatureStoreException {
        Set<String> ids = new HashSet<String>();
        Query query = new Query( ftName, filter, -1, -1, -1 );
        FeatureInputStream rs = null;
        try {
            rs = fs.query( query );
            for ( Feature feature : rs ) {
                ids.add( feature.getId() );
            }
        } catch ( FilterEvaluationException e ) {
            throw new FeatureStoreException( e );
        } finally {
            if ( rs != null ) {
                rs.close();
            }
        }
        return new IdFilter( ids );
    }

    private void checkIfFeaturesAreNotLocked( IdFilter filter, Lock lock )
                            throws FeatureStoreException {
        String lockId = lock != null ? lock.getId() : null;
    
        // check if all features can be deleted
        for ( ResourceId id : filter.getSelectedIds() ) {
            if ( !fs.getLockManager().isFeatureModifiable( id.getRid(), lockId ) ) {
                if ( lockId == null ) {
                    throw new MissingParameterException( getMessage( "TA_DELETE_LOCKED_NO_LOCK_ID", id.getRid() ),
                                                         "lockId" );
                }
                throw new InvalidParameterValueException( getMessage( "TA_DELETE_LOCKED_WRONG_LOCK_ID", id.getRid() ),
                                                          "lockId" );
            }
        }
    }

    @Override
    public String performReplace( final Feature replacement, final Filter filter, final Lock lock,
                                  final IDGenMode idGenMode )
                            throws FeatureStoreException {
        if ( filter instanceof IdFilter ) {
            performDelete( (IdFilter) filter, lock );
        } else {
            performDelete( replacement.getName(), (OperatorFilter) filter, lock );
        }
        final GenericFeatureCollection col = new GenericFeatureCollection();
        col.add( replacement );
        final List<String> ids = performInsert( col, USE_EXISTING );
        if ( ids.isEmpty() || ids.size() > 1 ) {
            throw new FeatureStoreException( "Unable to determine new feature id." );
        }
        return ids.get( 0 );
    }

}