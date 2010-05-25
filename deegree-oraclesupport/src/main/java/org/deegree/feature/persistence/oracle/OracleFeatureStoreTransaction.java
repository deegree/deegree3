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
package org.deegree.feature.persistence.oracle;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.xml.namespace.QName;

import oracle.jdbc.OracleConnection;
import oracle.spatial.geometry.JGeometry;

import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.Lock;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.geometry.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStoreTransaction} implementation used by the {@link OracleFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class OracleFeatureStoreTransaction implements FeatureStoreTransaction {

    private static final Logger LOG = LoggerFactory.getLogger( OracleFeatureStoreTransaction.class );

    private OracleFeatureStore fs;

    private Connection conn;

    /**
     * Creates a new {@link OracleFeatureStoreTransaction}. Should only be called by the {@link OracleFeatureStore}.
     * 
     * @param fs
     *            feature store, must not be <code>null</code>
     * @param conn
     *            JDBC connection, must not be <code>null</code>
     */
    OracleFeatureStoreTransaction( OracleFeatureStore fs, Connection conn ) {
        this.fs = fs;
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
            LOG.debug( e.getMessage(), e.getNextException() );
            throw new FeatureStoreException( "Unable to commit SQL transaction: " + e.getMessage() );
        } finally {
            fs.releaseTransaction( this );
        }
    }

    @Override
    public FeatureStore getStore() {
        return fs;
    }

    @Override
    public int performDelete( QName ftName, OperatorFilter filter, Lock lock )
                            throws FeatureStoreException {
        throw new FeatureStoreException ("The delete operation is not available for the feature store / configuration.");
    }

    @Override
    public int performDelete( IdFilter filter, Lock lock )
                            throws FeatureStoreException {

        throw new FeatureStoreException ("The delete operation is not available for the feature store / configuration.");
//        
//        if ( filter.getMatchingIds().isEmpty() ) {
//            return 0;
//        }
//        int deleted = -1;
//
//        PreparedStatement stmt = null;
//        try {
//            LOG.warn( "Deleting from table GGD (needs configurability)" );
//            StringBuilder sql = new StringBuilder( "DELETE FROM GGD WHERE GMLID IN(?" );
//            for ( int i = 1; i < filter.getMatchingIds().size(); i++ ) {
//                sql.append( ",?" );
//            }
//            sql.append( ")" );
//            stmt = conn.prepareStatement( sql.toString() );
//            int i = 1;
//            for ( String fid : filter.getMatchingIds() ) {
//                // TODO
//                fid = fid.substring( 4 );
//                stmt.setString( i++, fid );
//            }
//            deleted = stmt.executeUpdate();
//        } catch ( SQLException e ) {
//            String msg = "Error deleting features using id filter: " + e.getMessage();
//            throw new FeatureStoreException( msg, e );
//        } finally {
//            if ( stmt != null ) {
//                JDBCUtils.close( stmt );
//            }
//        }
//        return deleted;
    }

    @Override
    public List<String> performInsert( FeatureCollection fc, IDGenMode mode )
                            throws FeatureStoreException {

        throw new FeatureStoreException ("The insert operation is not available for the feature store / configuration.");
//        
//        if ( mode == IDGenMode.REPLACE_DUPLICATE ) {
//            throw new FeatureStoreException( "REPLACE_DUPLICATE is not available yet." );
//        }
//
//        List<String> fids = new ArrayList<String>( fc.size() );
//        for ( Feature f : fc ) {
//            String fid = f.getId();
//            if ( mode == IDGenMode.GENERATE_NEW ) {
//                fid = generateNewId();
//            } else if ( fid == null ) {
//                String msg = "Unable to perform insert. Id generation mode is USE_EXISTING, but feature collection contains features without ids.";
//                throw new FeatureStoreException( msg );
//            }
//            fids.add( fid );
//
//            FeatureType ft = fs.getFeatureType( f.getName() );
//            if ( ft == null ) {
//                String msg = "Unable to insert feature with type '" + f.getName()
//                             + "': feature type is not served by this feature store.";
//                throw new FeatureStoreException( msg );
//            }
//            FeatureTypeMapping ftMapping = fs.getMapping( f.getName() );
//
//            StringBuffer sql = new StringBuffer( "INSERT INTO " );
//            sql.append( ftMapping.getTable() );
//            // TODO
//            sql.append( " (GMLID" );
//            StringBuffer qMarks = new StringBuffer( "?" );
//            for ( Property prop : f.getProperties() ) {
//                if ( prop.getValue() != null ) {
//                    qMarks.append( ",?" );
//                    sql.append( "," );
//                    sql.append( ftMapping.getColumn( prop.getName() ) );
//                }
//            }
//            sql.append( ") VALUES (" );
//            sql.append( qMarks );
//            sql.append( ")" );
//            try {
//                PreparedStatement stmt = conn.prepareStatement( sql.toString() );
//                stmt.setString( 1, fid );
//                int i = 2;
//                for ( Property prop : f.getProperties() ) {
//                    if ( prop.getValue() != null ) {
//                        Object oracleValue = getSQLValue(
//                                                          (OracleConnection) ( (DelegatingConnection) conn ).getInnermostDelegate(),
//                                                          prop );
//                        stmt.setObject( i++, oracleValue );
//                    }
//                }
//                stmt.execute();
//            } catch ( SQLException e ) {
//                throw new FeatureStoreException( "SQLException while inserting feature: " + e.getMessage(), e );
//            }
//            LOG.info( "Executing: " + sql );
//        }
//        return fids;
    }

    private Object getSQLValue( OracleConnection conn, Property prop )
                            throws FeatureStoreException {
        Object value = null;
        PropertyType pt = prop.getType();
        if ( pt instanceof SimplePropertyType ) {
            SimplePropertyType spt = (SimplePropertyType) pt;
            value = ( (PrimitiveValue) prop.getValue() ).getAsText();
        } else if ( pt instanceof GeometryPropertyType ) {
            Geometry g = (Geometry) prop.getValue();
            if ( g.getCoordinateSystem() != null && !g.getCoordinateSystem().equals( fs.getStorageSRS() ) ) {
                try {
                    g = fs.getGeometryTransformer().transform( g );
                } catch ( Exception e ) {
                    String msg = "Error during transformation to storage SRS: " + e.getMessage();
                    throw new FeatureStoreException( msg, e );
                }
            }
            JGeometry jg = fs.getJGeometryAdapter().toJGeometry( g );
            try {
                value = JGeometry.store( conn, jg );
            } catch ( Exception e ) {
                String msg = "Error converting geometry to Oracle geometry: " + e.getMessage();
                throw new FeatureStoreException( msg );
            }
        } else {
            String msg = "SQL mapping for properties of type '" + prop.getType()
                         + "' is not implemented for OracleFeatureStore yet.";
            throw new FeatureStoreException( msg );
        }
        return value;
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
            fs.releaseTransaction( this );
        }
    }

    private String generateNewId() {
        return UUID.randomUUID().toString();
    }
}
