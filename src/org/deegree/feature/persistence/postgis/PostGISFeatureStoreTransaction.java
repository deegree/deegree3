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

import static org.postgresql.largeobject.LargeObjectManager.READWRITE;
import static org.postgresql.largeobject.LargeObjectManager.WRITE;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.dbcp.DelegatingConnection;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Property;
import org.deegree.feature.gml.FeatureReference;
import org.deegree.feature.persistence.FeatureCoder;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.Lock;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.postgis.LinearRing;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
class PostGISFeatureStoreTransaction implements FeatureStoreTransaction {

    private static final Logger LOG = LoggerFactory.getLogger( PostGISFeatureStoreTransaction.class );

    private final PostGISFeatureStore store;

    private final Connection conn;

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
            conn.commit();
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new FeatureStoreException( "Unable to commit SQL transaction: " + e.getMessage() );
        } finally {
            store.releaseTransaction( this );
        }
    }

    @Override
    public FeatureStore getStore() {
        return store;
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<String> performInsert( FeatureCollection fc, IDGenMode mode )
                            throws FeatureStoreException {

        LOG.debug( "performInsert()" );

        Set<Geometry> geometries = new HashSet<Geometry>();
        Set<Feature> features = new HashSet<Feature>();
        Set<String> fids = new LinkedHashSet<String>();
        Set<String> gids = new LinkedHashSet<String>();
        findFeaturesAndGeometries( fc, geometries, features, fids, gids );

        LOG.debug( features.size() + " features / " + geometries.size() + " geometries" );

        long begin = System.currentTimeMillis();
        for ( Feature feature : features ) {
            try {
                insertFeature( feature );
            } catch ( SQLException e ) {
                LOG.debug( e.getMessage(), e );
                throw new FeatureStoreException( e.getMessage(), e );
            }
        }
        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Insertion of " + features.size() + " features: " + elapsed + " [ms]" );
        return new ArrayList<String>( fids );
    }

    private void insertFeature( Feature feature )
                            throws SQLException {

        long t1 = System.currentTimeMillis();

        PreparedStatement stmt = conn.prepareStatement( "INSERT INTO gml_objects (gml_id,gml_description,ft_type,binary_object2,gml_bounded_by) VALUES(?,?,?,?,?)" );
        stmt.setString( 1, feature.getId() );
        stmt.setString( 2, "TODO: gml_description" );
        stmt.setShort( 3, store.getFtId( feature.getName() ) );

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            FeatureCoder.encode( feature, bos, LOG );
        } catch ( Exception e ) {
            String msg = "Error encoding feature for BLOB: " + e.getMessage();
            LOG.error( msg, e.getMessage() );
            throw new SQLException( msg, e );
        }
        stmt.setBytes( 4, bos.toByteArray() );

        stmt.setObject( 5, toPGPolygon( feature.getEnvelope() ) );
        stmt.executeUpdate();
        stmt.close();
        long t2 = System.currentTimeMillis();
        LOG.debug( "Insert: " + ( t2 - t1 ) + " [ms]" );
    }

    private PGgeometry toPGPolygon( Envelope envelope ) {
        PGgeometry pgGeometry = null;
        if ( envelope != null ) {
            if ( !envelope.getMin().equals( envelope.getMax() ) ) {
                double minX = envelope.getMin().get0();
                double minY = envelope.getMin().get1();
                double maxX = envelope.getMax().get0();
                double maxY = envelope.getMax().get1();
                Point[] points = new Point[] { new Point( minX, minY ), new Point( maxX, minY ),
                                              new Point( maxX, maxY ), new Point( minX, maxY ), new Point( minX, minY ) };
                LinearRing outer = new LinearRing( points );
                Polygon polygon = new Polygon( new LinearRing[] { outer } );
                // TODO
                polygon.setSrid( -1 );
                pgGeometry = new PGgeometry( polygon );
            } else {
                Point point = new Point( envelope.getMin().get0(), envelope.getMin().get1() );
                // TODO
                point.setSrid( -1 );
                pgGeometry = new PGgeometry( point );
            }
        }
        return pgGeometry;
    }

    private void findFeaturesAndGeometries( Feature feature, Set<Geometry> geometries, Set<Feature> features,
                                            Set<String> fids, Set<String> gids ) {

        if ( !features.contains( feature ) ) {
            if ( feature instanceof FeatureCollection ) {
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
                for ( Property<?> property : feature.getProperties() ) {
                    Object propertyValue = property.getValue();
                    if ( propertyValue instanceof Feature ) {
                        if ( !( propertyValue instanceof FeatureReference )
                             || ( (FeatureReference) propertyValue ).isLocal() ) {
                            findFeaturesAndGeometries( (Feature) propertyValue, geometries, features, fids, gids );
                        }
                    } else if ( propertyValue instanceof Geometry ) {
                        findGeometries( (Geometry) propertyValue, geometries, gids );
                    }
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
    public int performUpdate( QName ftName, List<Property<?>> replacementProps, Filter filter, Lock lock )
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
}
