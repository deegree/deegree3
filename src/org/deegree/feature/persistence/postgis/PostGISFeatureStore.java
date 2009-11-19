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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.crs.CRS;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Features;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureCoder;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreGMLIdResolver;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.StoredFeatureTypeMetadata;
import org.deegree.feature.persistence.lock.DefaultLockManager;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.BBOX;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.wfs.getfeature.BBoxQuery;
import org.deegree.protocol.wfs.getfeature.FeatureIdQuery;
import org.deegree.protocol.wfs.getfeature.FilterQuery;
import org.deegree.protocol.wfs.getfeature.Query;
import org.postgis.PGgeometry;
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

    private final String jdbcConnId;

    private PostGISFeatureStoreTransaction activeTransaction;

    private Thread transactionHolder;

    private LockManager lockManager;

    /**
     * Creates a new {@link PostGISFeatureStore} for the given {@link ApplicationSchema}.
     * 
     * @param schema
     *            schema information, must not be <code>null</code>
     * @param jdbcConnId
     */
    public PostGISFeatureStore( ApplicationSchema schema, String jdbcConnId ) {
        this.schema = schema;
        this.jdbcConnId = jdbcConnId;
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
            setSearchPath( conn );
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
        // TODO use information from database
        return new GeometryFactory().createEnvelope( -180, -90, 180, 90, CRS.EPSG_4326 );
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
    public Object getObjectById( String id )
                            throws FeatureStoreException {

        Object geomOrFeature = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection( jdbcConnId );
            conn.setAutoCommit( false );
            stmt = conn.createStatement();
            rs = stmt.executeQuery( "SELECT binary_object FROM gml_objects WHERE gml_id='" + id + "'" );
            if ( rs.next() ) {
                LOG.debug( "Recreating object '" + id + "' from bytea." );
                geomOrFeature = FeatureCoder.decode( rs.getBinaryStream( 1 ), schema, new CRS( "EPSG:31466" ),
                                                       new FeatureStoreGMLIdResolver( this ) );
            }
        } catch ( Exception e ) {
            String msg = "Error performing query: " + e.getMessage();
            LOG.debug( msg, e );
            throw new FeatureStoreException( msg, e );
        } finally {
            closeSafely( conn, stmt, rs );
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
        lockManager = new DefaultLockManager( this, "LOCK_DB" );

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection( jdbcConnId );
            setSearchPath( conn );

            stmt = conn.createStatement();
            rs = stmt.executeQuery( "SELECT id,qname,tablename,wgs84bbox FROM feature_types" );
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
                CRS nativeCRS = new CRS( "EPSG:31466" );
                StoredFeatureTypeMetadata ftMd = new StoredFeatureTypeMetadata( ft, this, title, desc, nativeCRS );
                ftNameToMd.put( ftName, ftMd );
                ftNameToFtId.put( ftName, ftId );
            }
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new FeatureStoreException( e.getMessage(), e );
        } finally {
            closeSafely( conn, stmt, null );
        }
    }

    // TODO make this configurable in the JDBC configuration
    private void setSearchPath( Connection conn ) {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate( "SET search_path TO xplan2,public" );
            stmt.close();
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.debug( e.getMessage(), e );
                }
            }
        }
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
    public int performHitsQuery( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO
        return performQuery( query ).size();
    }

    @Override
    public FeatureCollection performQuery( Query query )
                            throws FeatureStoreException, FilterEvaluationException {

        if ( query.getTypeNames().length > 1 ) {
            String msg = "Queries that target more than one feature type (joins) are not supported yet.";
            throw new FeatureStoreException( msg );
        }

        QName ftName = query.getTypeNames()[0].getFeatureTypeName();
        if ( !ftNameToFtId.containsKey( ftName ) ) {
            String msg = "Feature type '" + ftName + "' is not served by this feature store.";
            throw new FeatureStoreException( msg );
        }
        short ftId = ftNameToFtId.get( ftName );

        FeatureCollection fc = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection( jdbcConnId );
            conn.setAutoCommit( false );
            stmt = conn.createStatement();
            rs = stmt.executeQuery( "SELECT gml_id,binary_object FROM gml_objects WHERE ft_type=" + ftId );

            // TODO lazy fetching (CloseableIterator etc)
            List<Feature> members = new LinkedList<Feature>();
            while ( rs.next() ) {
                String gml_id = rs.getString( 1 );
                LOG.debug( "Recreating object '" + gml_id + "' from blob." );
                Feature feature = FeatureCoder.decode( rs.getBinaryStream( 2 ), schema, new CRS( "EPSG:31466" ),
                                                       new FeatureStoreGMLIdResolver( this ) );
                members.add( feature );
            }
            fc = new GenericFeatureCollection( null, members );
        } catch ( Exception e ) {
            String msg = "Error performing query: " + e.getMessage();
            LOG.debug( msg, e );
            throw new FeatureStoreException( msg, e );
        } finally {
            closeSafely( conn, stmt, rs );
        }

        // extract / create filter from query
        if ( query instanceof FilterQuery ) {
            Filter filter = ( (FilterQuery) query ).getFilter();
            if ( filter != null ) {
                fc = fc.getMembers( filter );
            }
        } else if ( query instanceof BBoxQuery ) {
            Envelope bbox = ( (BBoxQuery) query ).getBBox();
            PropertyName geoProp = findGeoProp( schema.getFeatureType( ftName ) );
            Operator bboxOperator = new BBOX( geoProp, bbox );
            Filter filter = new OperatorFilter( bboxOperator );
            fc = fc.getMembers( filter );
        } else if ( query instanceof FeatureIdQuery ) {
            List<Feature> matches = new LinkedList<Feature>();
            for ( String fid : ( (FeatureIdQuery) query ).getFeatureIds() ) {
                Object object = getObjectById( fid );
                if ( object instanceof Feature ) {
                    matches.add( (Feature) object );
                }
            }
            fc = new GenericFeatureCollection( null, matches );
        }

        // sort features
        SortProperty[] sortCrit = query.getSortBy();
        if ( sortCrit != null ) {
            fc = Features.sortFc( fc, sortCrit );
        }
        return fc;
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

    @Override
    public FeatureCollection query( QName featureType, Filter filter, Envelope bbox, boolean withGeometries,
                                    boolean exact )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO Auto-generated method stub
        return null;
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
    }

    short getFtId( QName ftName ) {
        return ftNameToFtId.get( ftName );
    }
}
