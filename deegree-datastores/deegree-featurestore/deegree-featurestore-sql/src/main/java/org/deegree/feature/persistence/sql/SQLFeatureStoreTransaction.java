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

import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.deegree.gml.GMLVersion.GML_32;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.primitive.SQLValueMangler;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.Lock;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.blob.BlobCodec;
import org.deegree.feature.persistence.sql.blob.BlobMapping;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.id.IdAnalysis;
import org.deegree.feature.persistence.sql.insert.InsertRowNode;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.FeatureMapping;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.feature.xpath.FeatureXPathEvaluator;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.MappingExpression;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometries;
import org.deegree.geometry.Geometry;
import org.deegree.gml.feature.FeatureReference;
import org.deegree.protocol.wfs.transaction.IDGenMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStoreTransaction} implementation for {@link SQLFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SQLFeatureStoreTransaction implements FeatureStoreTransaction {

    private static final Logger LOG = LoggerFactory.getLogger( SQLFeatureStoreTransaction.class );

    private final SQLFeatureStore fs;

    private final MappedApplicationSchema schema;

    private final BlobMapping blobMapping;

    private final TransactionManager taManager;

    private final Connection conn;

    // TODO
    private ParticleConverter<Geometry> blobGeomConverter;

    /**
     * Creates a new {@link SQLFeatureStoreTransaction} instance.
     * 
     * @param store
     *            invoking feature store instance, never <code>null</code>
     * @param taManager
     * @param conn
     *            JDBC connection associated with the transaction, never <code>null</code> and has
     *            <code>autocommit</code> set to <code>false</code>
     * @param schema
     */
    SQLFeatureStoreTransaction( SQLFeatureStore store, TransactionManager taManager, Connection conn,
                                MappedApplicationSchema schema ) {
        this.fs = store;
        this.taManager = taManager;
        this.conn = conn;
        this.schema = schema;
        blobMapping = schema.getBlobMapping();
        if ( blobMapping != null ) {
            DBField bboxColumn = new DBField( blobMapping.getBBoxColumn() );
            GeometryStorageParams geometryParams = new GeometryStorageParams( blobMapping.getCRS(), null, DIM_2 );
            GeometryMapping blobGeomMapping = new GeometryMapping( null, true, bboxColumn, GeometryType.GEOMETRY,
                                                                   geometryParams, null );
            blobGeomConverter = fs.getGeometryConverter( blobGeomMapping );
        }
    }

    @Override
    public void commit()
                            throws FeatureStoreException {

        LOG.debug( "Committing transaction." );
        try {
            conn.commit();
            fs.clearEnvelopeCache();
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            LOG.debug( e.getMessage(), e.getNextException() );
            throw new FeatureStoreException( "Unable to commit SQL transaction: " + e.getMessage() );
        } finally {
            taManager.releaseTransaction( this );
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
        return deleted;
    }

    private int performDeleteBlob( IdFilter filter, Lock lock )
                            throws FeatureStoreException {

        // TODO implement this more efficiently (using IN / temporary tables)
        int deleted = 0;
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement( "DELETE FROM " + blobMapping.getTable() + " WHERE "
                                          + blobMapping.getGMLIdColumn() + "=?" );
            for ( String id : filter.getMatchingIds() ) {
                stmt.setString( 1, id );
                stmt.addBatch();
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
        int deleted = 0;
        for ( String id : filter.getMatchingIds() ) {
            LOG.debug( "Analyzing id: " + id );
            IdAnalysis analysis = null;
            try {
                analysis = schema.analyzeId( id );
                LOG.debug( "Analysis: " + analysis );
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
                        PrimitiveValue value = new PrimitiveValue( fidKernel,
                                                                   new PrimitiveType( fidMapping.getColumnType() ) );
                        Object sqlValue = SQLValueMangler.internalToSQL( value );
                        stmt.setObject( i++, sqlValue );
                    }
                    LOG.debug( "Executing: " + stmt );
                    deleted += stmt.executeUpdate();
                } catch ( SQLException e ) {
                    LOG.debug( e.getMessage(), e );
                    throw new FeatureStoreException( e.getMessage(), e );
                } finally {
                    JDBCUtils.close( stmt );
                }
            } catch ( IllegalArgumentException e ) {
                throw new FeatureStoreException( "Unable to determine feature type for id '" + id + "'." );
            }
        }
        return deleted;
    }

    @Override
    public List<String> performInsert( FeatureCollection fc, IDGenMode mode )
                            throws FeatureStoreException {

        LOG.debug( "performInsert()" );

        Set<Geometry> geometries = new LinkedHashSet<Geometry>();
        Set<Feature> features = new LinkedHashSet<Feature>();
        Set<String> fids = new LinkedHashSet<String>();
        Set<String> gids = new LinkedHashSet<String>();
        findFeaturesAndGeometries( fc, geometries, features, fids, gids );

        LOG.debug( features.size() + " features / " + geometries.size() + " geometries" );

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
                sql.append( blobGeomConverter.getSetSnippet() );
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
                }
                if ( blobInsertStmt != null ) {
                    blobInsertStmt.close();
                }
            } else {
                // pure relational mode
                for ( Feature feature : features ) {
                    fid = feature.getId();
                    FeatureTypeMapping ftMapping = fs.getMapping( feature.getName() );
                    if ( ftMapping == null ) {
                        throw new FeatureStoreException( "Cannot insert feature of type '" + feature.getName()
                                                         + "'. No mapping defined and BLOB mode is off." );
                    }
                    fids.add( insertFeatureRelational( feature, ftMapping, mode ) );
                }
            }
        } catch ( Throwable t ) {
            String msg = "Error inserting feature '" + fid + "':" + t.getMessage();
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

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            BlobCodec codec = fs.getSchema().getBlobMapping().getCodec();
            codec.encode( feature, fs.getNamespaceContext(), bos, crs );
        } catch ( Exception e ) {
            String msg = "Error encoding feature for BLOB: " + e.getMessage();
            LOG.error( msg );
            LOG.trace( "Stack trace:", e );
            throw new SQLException( msg, e );
        }
        byte[] bytes = bos.toByteArray();
        stmt.setBytes( 3, bytes );
        LOG.debug( "Feature blob size: " + bytes.length );
        Geometry bboxGeom = null;
        Envelope bbox = feature.getEnvelope();
        if ( bbox != null ) {
            bboxGeom = Geometries.getAsGeometry( bbox );
        }
        blobGeomConverter.setParticle( stmt, bboxGeom, 4 );
        // stmt.addBatch();
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

    private String insertFeatureRelational( Feature feature, FeatureTypeMapping ftMapping, IDGenMode mode )
                            throws SQLException, FeatureStoreException, FilterEvaluationException {

        InsertRowNode node = new InsertRowNode( ftMapping.getFtTable(), null );

        String fid = feature.getId();
        FIDMapping fidMapping = ftMapping.getFidMapping();
        if ( mode == IDGenMode.USE_EXISTING ) {
            node.getRow().addPreparedArgument( fidMapping.getColumn(), fid );
        } else {
            throw new UnsupportedOperationException();
        }

        for ( Mapping particleMapping : ftMapping.getMappings() ) {
            buildInsertRows( feature, particleMapping, node );
        }
        LOG.debug( "Built row {}", node );
        node.performInsert( conn );
        return fid;
    }

    private void buildInsertRows( TypedObjectNode particle, Mapping mapping, InsertRowNode node )
                            throws FilterEvaluationException, FeatureStoreException {

        List<TableJoin> jc = mapping.getJoinedTable();
        if ( jc != null ) {
            if ( jc.size() != 1 ) {
                throw new FeatureStoreException( "Handling of joins with " + jc.size() + " steps is not implemented." );
            }
        }

        FeatureXPathEvaluator evaluator = new FeatureXPathEvaluator( GML_32 );
        TypedObjectNode[] values = evaluator.eval( particle, mapping.getPath() );
        int childIdx = 1;
        for ( TypedObjectNode value : values ) {
            InsertRowNode insertNode = node;
            if ( jc != null ) {
                insertNode = new InsertRowNode( jc.get( 0 ).getToTable(), jc.get( 0 ) );
                node.getRelatedRows().add( insertNode );
            }
            if ( mapping instanceof PrimitiveMapping ) {
                MappingExpression me = ( (PrimitiveMapping) mapping ).getMapping();
                if ( !( me instanceof DBField ) ) {
                    LOG.debug( "Skipping primitive mapping. Not mapped to database column." );
                } else {
                    PrimitiveValue primitiveValue = getPrimitiveValue( value );
                    String column = ( (DBField) me ).getColumn();
                    Object sqlValue = SQLValueMangler.internalToSQL( primitiveValue.getValue() );
                    insertNode.getRow().addPreparedArgument( column, sqlValue );
                }
            } else if ( mapping instanceof GeometryMapping ) {
                MappingExpression me = ( (GeometryMapping) mapping ).getMapping();
                if ( !( me instanceof DBField ) ) {
                    LOG.debug( "Skipping geometry mapping. Not mapped to database column." );
                } else {
                    Geometry geom = (Geometry) getPropValue( value );
                    ParticleConverter<Geometry> converter = (ParticleConverter<Geometry>) fs.getConverter( mapping );
                    String column = ( (DBField) me ).getColumn();
                    insertNode.getRow().addPreparedArgument( column, geom, converter );
                }
            } else if ( mapping instanceof FeatureMapping ) {
                String fid = null;
                String href = null;
                Feature feature = (Feature) getPropValue( value );
                if ( feature instanceof FeatureReference ) {
                    if ( ( (FeatureReference) feature ).isLocal() ) {
                        fid = feature.getId();
                    } else {
                        href = ( (FeatureReference) feature ).getURI();
                    }
                } else if ( feature != null ) {
                    fid = feature.getId();
                }

                if ( fid != null ) {
                    MappingExpression me = ( (FeatureMapping) mapping ).getMapping();
                    if ( !( me instanceof DBField ) ) {
                        LOG.debug( "Skipping feature mapping (fk). Not mapped to database column." );
                    } else {
                        String column = ( (DBField) me ).getColumn();
                        Object sqlValue = SQLValueMangler.internalToSQL( fid );
                        insertNode.getRow().addPreparedArgument( column, sqlValue );
                    }
                }
                if ( href != null ) {
                    MappingExpression me = ( (FeatureMapping) mapping ).getMapping();
                    if ( !( me instanceof DBField ) ) {
                        LOG.debug( "Skipping feature mapping (href). Not mapped to database column." );
                    } else {
                        String column = ( (DBField) me ).getColumn();
                        Object sqlValue = SQLValueMangler.internalToSQL( href );
                        insertNode.getRow().addPreparedArgument( column, sqlValue );
                    }
                }
            } else if ( mapping instanceof CompoundMapping ) {
                for ( Mapping child : ( (CompoundMapping) mapping ).getParticles() ) {
                    buildInsertRows( value, child, insertNode );
                }
            } else {
                LOG.warn( "Unhandled mapping type '" + mapping.getClass() + "'." );
            }

            if ( jc != null ) {
                // add index column value
                for ( String col : jc.get( 0 ).getOrderColumns() ) {
                    if ( insertNode.getRow().get( col ) == null ) {
                        // TODO do this properly
                        insertNode.getRow().addLiteralValue( col, "" + childIdx++ );
                    }
                }
            }
        }
    }

    private PrimitiveValue getPrimitiveValue( TypedObjectNode value ) {
        if ( value instanceof Property ) {
            value = ( (Property) value ).getValue();
        }
        if ( value instanceof GenericXMLElement ) {
            value = ( (GenericXMLElement) value ).getValue();
        }
        return (PrimitiveValue) value;
    }

    private TypedObjectNode getPropValue( TypedObjectNode prop ) {
        if ( prop instanceof Property ) {
            return ( (Property) prop ).getValue();
        }
        return prop;
    }

    private void findFeaturesAndGeometries( Feature feature, Set<Geometry> geometries, Set<Feature> features,
                                            Set<String> fids, Set<String> gids ) {

        if ( feature instanceof FeatureCollection ) {
            // try to keep document order
            for ( Feature member : (FeatureCollection) feature ) {
                if ( !( member instanceof FeatureReference ) ) {
                    features.add( member );
                }
            }
            for ( Feature member : (FeatureCollection) feature ) {
                findFeaturesAndGeometries( member, geometries, features, fids, gids );
            }
        } else {
            if ( feature.getId() == null || !( fids.contains( feature.getId() ) ) ) {
                features.add( feature );
                if ( feature.getId() != null ) {
                    fids.add( feature.getId() );
                }
            }
            for ( Property property : feature.getProperties() ) {
                Object propertyValue = property.getValue();
                if ( propertyValue instanceof Feature ) {
                    if ( !( propertyValue instanceof FeatureReference ) ) {
                        if ( !features.contains( propertyValue ) ) {
                            findFeaturesAndGeometries( (Feature) propertyValue, geometries, features, fids, gids );
                        }
                    } else if ( ( (FeatureReference) propertyValue ).isResolved()
                                && !( features.contains( ( (FeatureReference) propertyValue ).getReferencedObject() ) ) ) {
                        findFeaturesAndGeometries( ( (FeatureReference) propertyValue ).getReferencedObject(),
                                                   geometries, features, fids, gids );
                    }
                } else if ( propertyValue instanceof Geometry ) {
                    findGeometries( (Geometry) propertyValue, geometries, gids );
                }
            }
        }
    }

    private void findGeometries( Geometry geometry, Set<Geometry> geometries, Set<String> gids ) {
        if ( geometry.getId() == null || !( gids.contains( geometry.getId() ) ) ) {
            geometries.add( geometry );
            if ( geometry.getId() != null ) {
                gids.add( geometry.getId() );
            }
        }
    }

    @Override
    public int performUpdate( QName ftName, List<Property> replacementProps, Filter filter, Lock lock )
                            throws FeatureStoreException {
        LOG.debug( "Updating feature type '" + ftName + "', filter: " + filter + ", replacement properties: "
                   + replacementProps.size() );
        // TODO implement update more efficiently
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
        return performUpdate( ftName, replacementProps, idFilter );
    }

    private int performUpdate( QName ftName, List<Property> replacementProps, IdFilter filter )
                            throws FeatureStoreException {
        int updated = 0;
        if ( blobMapping != null ) {
            throw new FeatureStoreException( "Updates in SQLFeatureStore (BLOB mode) are currently not implemented." );
        } else {
            try {
                updated = performUpdateRelational( ftName, replacementProps, filter );
                for ( String id : filter.getMatchingIds() ) {
                    fs.getCache().remove( id );
                }
            } catch ( Exception e ) {
                LOG.debug( e.getMessage(), e );
                throw new FeatureStoreException( e.getMessage(), e );
            }
        }
        return updated;
    }

    private int performUpdateRelational( QName ftName, List<Property> replacementProps, IdFilter filter )
                            throws FeatureStoreException {

        FeatureTypeMapping ftMapping = schema.getFtMapping( ftName );
        FIDMapping fidMapping = ftMapping.getFidMapping();

        StringBuffer sql = new StringBuffer( "UPDATE " );
        sql.append( ftMapping.getFtTable() );
        sql.append( " SET " );
        boolean first = true;
        for ( Property replacementProp : replacementProps ) {
            QName propName = replacementProp.getType().getName();
            Mapping mapping = ftMapping.getMapping( propName );
            if ( mapping != null ) {
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

                    sql.append( converter.getSetSnippet() );
                } else if ( mapping instanceof GeometryMapping ) {
                    MappingExpression me = ( (GeometryMapping) mapping ).getMapping();
                    if ( !( me instanceof DBField ) ) {
                        continue;
                    }
                    if ( !first ) {
                        sql.append( "," );
                    } else {
                        first = false;
                    }
                    sql.append( column );
                    sql.append( "=" );
                    sql.append( converter.getSetSnippet() );
                } else {
                    LOG.warn( "Updating of " + mapping.getClass() + " is currently not implemented. Omitting." );
                    continue;
                }
            } else {
                LOG.warn( "No mapping for update property '" + propName + "'. Omitting." );
            }
        }
        sql.append( " WHERE " );
        sql.append( fidMapping.getColumns().get( 0 ).first );
        sql.append( "=?" );
        for ( int i = 1; i < fidMapping.getColumns().size(); i++ ) {
            sql.append( " AND " );
            sql.append( fidMapping.getColumns().get( i ) );
            sql.append( "=?" );
        }

        LOG.debug( "Update: " + sql );

        int updated = 0;
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement( sql.toString() );
            int i = 1;

            for ( Property replacementProp : replacementProps ) {
                QName propName = replacementProp.getType().getName();
                Mapping mapping = ftMapping.getMapping( propName );
                if ( mapping != null ) {
                    ParticleConverter<TypedObjectNode> converter = (ParticleConverter<TypedObjectNode>) fs.getConverter( mapping );
                    if ( mapping instanceof PrimitiveMapping ) {
                        MappingExpression me = ( (GeometryMapping) mapping ).getMapping();
                        if ( !( me instanceof DBField ) ) {
                            continue;
                        }
                        PrimitiveValue value = (PrimitiveValue) replacementProp.getValue();
                        converter.setParticle( stmt, value, i++ );
                    } else if ( mapping instanceof GeometryMapping ) {
                        MappingExpression me = ( (GeometryMapping) mapping ).getMapping();
                        if ( !( me instanceof DBField ) ) {
                            continue;
                        }
                        Geometry value = (Geometry) replacementProp.getValue();
                        converter.setParticle( stmt, value, i++ );
                    }
                }
            }

            for ( String id : filter.getMatchingIds() ) {
                IdAnalysis analysis = schema.analyzeId( id );
                int j = i;
                for ( String fidKernel : analysis.getIdKernels() ) {
                    PrimitiveValue value = new PrimitiveValue( fidKernel,
                                                               new PrimitiveType( fidMapping.getColumnType() ) );
                    Object sqlValue = SQLValueMangler.internalToSQL( value );
                    stmt.setObject( j++, sqlValue );
                }
                stmt.addBatch();
            }
            int[] updates = stmt.executeBatch();
            for ( int noUpdated : updates ) {
                updated += noUpdated;
            }
        } catch ( SQLException e ) {
            JDBCUtils.log( e, LOG );
            throw new FeatureStoreException( JDBCUtils.getMessage( e ), e );
        } finally {
            JDBCUtils.close( stmt );
        }
        LOG.debug( "Updated" + updated + " features." );
        return updated;
    }

    private IdFilter getIdFilter( QName ftName, OperatorFilter filter )
                            throws FeatureStoreException {
        Set<String> ids = new HashSet<String>();
        Query query = new Query( ftName, filter, -1, -1, -1 );
        FeatureResultSet rs = null;
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

    private String getAutoIncrementFID( FIDMapping fidMapping, Statement stmt )
                            throws SQLException {
        // TODO check for PostgreSQL >= 8.2 first
        String fid = null;
        ResultSet rs = null;
        try {
            rs = stmt.getGeneratedKeys();
            rs.next();
            Object idKernel = rs.getObject( fidMapping.getColumn() );
            fid = fidMapping.getPrefix() + idKernel;
        } finally {
            if ( rs != null ) {
                rs.close();
            }
        }
        return fid;
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
            taManager.releaseTransaction( this );
        }
    }

}