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

package org.deegree.feature.persistence.postgis;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.primitive.SQLValueMangler;
import org.deegree.cs.CRS;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureCoder;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.Lock;
import org.deegree.feature.persistence.postgis.jaxbconfig.GeometryPropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.PropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.SimplePropertyMappingType;
import org.deegree.feature.property.Property;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.io.WKBWriter;
import org.deegree.gml.feature.FeatureReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.io.ParseException;

/**
 * {@link FeatureStoreTransaction} implementation used by the {@link PostGISFeatureStore}.
 * 
 * @see PostGISFeatureStore
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISFeatureStoreTransaction implements FeatureStoreTransaction {

    private static final Logger LOG = LoggerFactory.getLogger( PostGISFeatureStoreTransaction.class );

    private final PostGISFeatureStore store;

    private final Connection conn;

    private static GeometryTransformer ftBBoxTransformer;

    static {
        try {
            ftBBoxTransformer = new GeometryTransformer( CRS.EPSG_4326.getWrappedCRS() );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link PostGISFeatureStoreTransaction} instance.
     * 
     * NOTE: This method is only supposed to be invoked by the {@link PostGISFeatureStore}.
     * 
     * @param store
     *            invoking feature store instance, never <code>null</code>
     * @param conn
     *            JDBC connection associated with the transaction, never <code>null</code> and has
     *            <code>autocommit</code> set to <code>false</code>
     */
    PostGISFeatureStoreTransaction( PostGISFeatureStore store, Connection conn ) {
        this.store = store;
        this.conn = conn;
    }

    @Override
    public void commit()
                            throws FeatureStoreException {
        LOG.debug( "Committing transaction." );
        try {
            persistEnvelopes();
            conn.commit();
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            LOG.debug( e.getMessage(), e.getNextException() );
            throw new FeatureStoreException( "Unable to commit SQL transaction: " + e.getMessage() );
        } finally {
            store.releaseTransaction( this );
        }
    }

    public void prepareCommit()
                            throws FeatureStoreException {
        LOG.debug( "Preparing commit of transaction." );
        try {
            persistEnvelopes();
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            LOG.debug( e.getMessage(), e.getNextException() );
            throw new FeatureStoreException( "Unable to prepare commit of SQL transaction: " + e.getMessage() );
        }
    }

    @Override
    public FeatureStore getStore() {
        return store;
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int performDelete( IdFilter filter, Lock lock )
                            throws FeatureStoreException {

        LOG.debug( "performDelete()" );

        int deleted = 0;
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement( "DELETE FROM " + store.qualifyTableName( "gml_objects" ) + " WHERE gml_id=?" );
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
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    e.printStackTrace();
                }
            }
        }
        LOG.debug( "Deleted " + deleted + " features." );
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

        LOG.debug( features.size() + " features / " + geometries.size() + " geometries" );

        long begin = System.currentTimeMillis();

        String fid = null;
        try {
            String sql = "INSERT INTO " + store.qualifyTableName( "gml_objects" )
                         + " (gml_id,gml_description,ft_type,binary_object,gml_bounded_by) VALUES(?,?,?,?,?)";
            PreparedStatement gmlObjectsInsertStmt = conn.prepareStatement( sql );
            for ( Feature feature : features ) {
                fid = feature.getId();
                int internalId = insertFeature( gmlObjectsInsertStmt, feature );
                FeatureTypeMapping mapping = store.getMapping( feature.getName() );
                if ( mapping != null ) {
                    insertFeatureRelational( internalId, feature, mapping );
                }
            }
            // stmt.executeBatch();
            gmlObjectsInsertStmt.close();
        } catch ( SQLException e ) {
            String msg = "Error inserting feature '" + fid + "':" + e.getMessage();
            LOG.debug( msg, e );
            throw new FeatureStoreException( msg, e );
        }

        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Insertion of " + features.size() + " features: " + elapsed + " [ms]" );
        return new ArrayList<String>( fids );
    }

    private String generateNewId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Inserts the given feature into table <code>gml_objects</code> and returns the primary key.
     * 
     * @param stmt
     * @param feature
     * @return primary key of the feature (column <code>id</code>)
     * @throws SQLException
     * @throws FeatureStoreException
     */
    private int insertFeature( PreparedStatement stmt, Feature feature )
                            throws SQLException, FeatureStoreException {

        if ( store.getSchema().getFeatureType( feature.getName() ) == null ) {
            throw new FeatureStoreException( "Cannot insert feature '" + feature.getName()
                                             + "': feature is not served by this feature store." );
        }
        CRS storageCRS = store.getStorageSRS();

        stmt.setString( 1, feature.getId() );
        stmt.setString( 2, "TODO: gml_description" );
        stmt.setShort( 3, store.getFtId( feature.getName() ) );

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            FeatureCoder.encode( feature, bos, storageCRS );
        } catch ( Exception e ) {
            String msg = "Error encoding feature for BLOB: " + e.getMessage();
            LOG.error( msg, e.getMessage() );
            throw new SQLException( msg, e );
        }
        stmt.setBytes( 4, bos.toByteArray() );
        // System.out.println( "Wrote feature blob (" + bos.toByteArray().length + " bytes)" );
        Envelope bbox = feature.getEnvelope();
        if ( bbox != null ) {
            try {
                GeometryTransformer bboxTransformer = new GeometryTransformer( storageCRS.getWrappedCRS() );
                bbox = (Envelope) bboxTransformer.transform( bbox );
            } catch ( Exception e ) {
                throw new SQLException( e.getMessage(), e );
            }
        }
        stmt.setObject( 5, store.toPGPolygon( bbox, -1 ) );
        // stmt.addBatch();
        stmt.execute();

        Envelope env = feature.getEnvelope();
        if ( env != null ) {
            try {
                env = (Envelope) ftBBoxTransformer.transform( env );
                Envelope ftEnv = store.ftNameToBBox.get( feature.getName() );
                if ( ftEnv != null ) {
                    ftEnv = ftEnv.merge( env );
                } else {
                    ftEnv = env;
                }
                store.setEnvelope( feature.getType(), ftEnv );
            } catch ( Exception e ) {
                throw new SQLException( e.getMessage(), e );
            }
        }

        int internalId = -1;
        PreparedStatement idSelect = null;
        ResultSet rs = null;
        try {
            idSelect = conn.prepareStatement( "SELECT id FROM " + store.qualifyTableName( "gml_objects" )
                                              + " WHERE gml_id=?" );
            idSelect.setString( 1, feature.getId() );
            rs = idSelect.executeQuery();
            rs.next();
            internalId = rs.getInt( 1 );
            rs.close();
            idSelect.close();
        } finally {
            if ( idSelect != null ) {
                idSelect.close();
            }
            if ( rs != null ) {
                rs.close();
            }
        }
        return internalId;
    }

    private void insertFeatureRelational( int internalId, Feature feature, FeatureTypeMapping ftMapping )
                            throws SQLException {

        LinkedHashMap<String, Object> columnsToValues;
        try {
            columnsToValues = getInsertColumns( feature, ftMapping );
        } catch ( FilterEvaluationException e ) {
            throw new SQLException( e.getMessage(), e );
        }
        String tableName = ftMapping.getFeatureTypeHints().getDBTable();

        // build SQL string
        StringBuilder sql = new StringBuilder( "INSERT INTO " + store.qualifyTableName( tableName ) + "(id" );
        for ( String column : columnsToValues.keySet() ) {
            sql.append( ',' );
            sql.append( column );
        }
        sql.append( ") VALUES(?" );
        for ( Entry<String, Object> entry : columnsToValues.entrySet() ) {
            if ( entry.getValue() instanceof Geometry ) {
                sql.append( ",GeomFromWKB(?," );
                // TODO
                sql.append( "-1)" );
            } else {
                sql.append( ",?" );
            }
        }
        sql.append( ")" );

        PreparedStatement stmt = conn.prepareStatement( sql.toString() );
        stmt.setInt( 1, internalId );
        int columnId = 2;
        for ( Entry<String, Object> entry : columnsToValues.entrySet() ) {
            Object pgValue = null;
            if ( entry.getValue() == null ) {
                // nothing
            } else if ( entry.getValue() instanceof Geometry ) {
                try {
                    pgValue = WKBWriter.write( (Geometry) entry.getValue() );
                } catch ( ParseException e ) {
                    throw new SQLException( e.getMessage(), e );
                }
            } else {
                pgValue = SQLValueMangler.internalToSQL( entry.getValue() );
            }
            stmt.setObject( columnId++, pgValue );
        }
        stmt.executeUpdate();
        stmt.close();
    }

    private LinkedHashMap<String, Object> getInsertColumns( Feature feature, FeatureTypeMapping ftMapping )
                            throws FilterEvaluationException {
        LinkedHashMap<String, Object> columnsToValues = new LinkedHashMap<String, Object>();

        for ( Property prop : feature.getProperties() ) {
            PropertyMappingType propMapping = ftMapping.getPropertyHints( prop.getName() );
            String dbColumn = null;
            if ( propMapping != null ) {
                if ( propMapping instanceof SimplePropertyMappingType ) {
                    SimplePropertyMappingType simplePropMapping = (SimplePropertyMappingType) propMapping;
                    dbColumn = simplePropMapping.getDBColumn().getName();
                } else if ( propMapping instanceof GeometryPropertyMappingType ) {
                    GeometryPropertyMappingType geoPropMapping = (GeometryPropertyMappingType) propMapping;
                    dbColumn = geoPropMapping.getGeometryDBColumn().getName();
                } else {
                    String msg = "Relational mapping of " + propMapping.getClass().getName()
                                 + " is not implemented yet.";
                    throw new UnsupportedOperationException( msg );
                }

                Object value = prop.getValue();

                if ( value instanceof Geometry ) {
                    value = store.getCompatibleGeometry( ( (Geometry) value ), store.storageSRS );
                }
                LOG.debug( "Property '" + prop.getName() + "', colum: " + dbColumn );

                columnsToValues.put( dbColumn, value );
            }
        }
        return columnsToValues;
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
                    if ( !( propertyValue instanceof FeatureReference ) && !features.contains( propertyValue ) ) {
                        findFeaturesAndGeometries( (Feature) propertyValue, geometries, features, fids, gids );
                    } else if ( ( (FeatureReference) propertyValue ).isLocal()
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

    private void persistEnvelopes()
                            throws SQLException {

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement( "UPDATE " + store.qualifyTableName( "feature_types" )
                                          + " SET wgs84bbox=? WHERE id=?" );
            for ( Map.Entry<QName, Envelope> ftBbox : store.ftNameToBBox.entrySet() ) {
                stmt.setObject( 1, store.toPGPolygon( ftBbox.getValue(), 4326 ) );
                stmt.setShort( 2, store.getFtId( ftBbox.getKey() ) );
                stmt.executeUpdate();
            }
        } finally {
            if ( stmt != null ) {
                stmt.close();
            }
        }
    }

    @Override
    public int performUpdate( QName ftName, List<Property> replacementProps, Filter filter, Lock lock )
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        return 0;
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
            store.releaseTransaction( this );
        }
    }

    public void prepareRollbackAndRelease()
                            throws FeatureStoreException {
        LOG.debug( "Preparing rollback of transaction." );
        store.releaseTransaction( this );
    }
}
