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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import oracle.jdbc.OracleConnection;
import oracle.spatial.geometry.JGeometry;

import org.apache.commons.dbcp.DelegatingConnection;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.Lock;
import org.deegree.feature.persistence.mapping.FeatureTypeMapping;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.FeatureType;
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
        throw new UnsupportedOperationException( "Deletes based on non-id filters are currently not implemented "
                                                 + "for this feature store." );
    }

    @Override
    public int performDelete( IdFilter filter, Lock lock )
                            throws FeatureStoreException {

        if ( filter.getMatchingIds().isEmpty() ) {
            return 0;
        }

        FeatureTypeMapping ftMapping = null;
        for ( String fid : filter.getMatchingIds() ) {
            if ( ftMapping != null && fs.getMapping( fid ) != ftMapping ) {
                String msg = "Trying to delete multiple feature types at once. This is currently not supported";
                throw new UnsupportedOperationException( msg );
            }
            ftMapping = fs.getMapping( fid );
        }

        int deleted = -1;
        PreparedStatement stmt = null;
        try {
            StringBuilder sql = new StringBuilder( "DELETE FROM " );
            sql.append( ftMapping.getFtTable() );
            sql.append( " WHERE " );
            sql.append( ftMapping.getFidColumn() );
            sql.append( " IN(?" );
            for ( int i = 1; i < filter.getMatchingIds().size(); i++ ) {
                sql.append( ",?" );
            }
            sql.append( ")" );

            LOG.debug( "Deleting: " + sql );
            stmt = conn.prepareStatement( sql.toString() );

            int i = 1;
            for ( String fid : filter.getMatchingIds() ) {
                int fidColumnValue = getFidColumnValue( fid, ftMapping );
                stmt.setInt( i++, fidColumnValue );
            }
            deleted = stmt.executeUpdate();

        } catch ( SQLException e ) {
            String msg = "Error deleting features using id filter: " + e.getMessage();
            throw new FeatureStoreException( msg, e );
        } finally {
            if ( stmt != null ) {
                JDBCUtils.close( stmt );
            }
        }
        return deleted;
    }

    @Override
    public List<String> performInsert( FeatureCollection fc, IDGenMode mode )
                            throws FeatureStoreException {

        List<String> fids = new ArrayList<String>( fc.size() );
        for ( Feature f : fc ) {
            fids.add( insertFeature( f, mode ) );
        }
        return fids;
    }

    private String insertFeature( Feature f, IDGenMode mode )
                            throws FeatureStoreException {

        FeatureType ft = fs.getFeatureType( f.getName() );
        if ( ft == null ) {
            String msg = "Unable to insert feature with type '" + f.getName()
                         + "': feature type is not served by this feature store.";
            throw new FeatureStoreException( msg );
        }

        FeatureTypeMapping ftMapping = fs.getMapping( f.getName() );
        String fid = determineNewFid( mode, f.getId(), ftMapping );

        StringBuilder sql = new StringBuilder( "INSERT INTO " );
        sql.append( ftMapping.getFtTable() );
        sql.append( " (" );
        sql.append( ftMapping.getFidColumn() );
        StringBuffer qMarks = new StringBuffer( "?" );
        for ( Property prop : f.getProperties() ) {
            if ( prop.getValue() != null ) {
                qMarks.append( ",?" );
                sql.append( "," );
                // TODO handle non-trivial mapping
                sql.append( ftMapping.getMapping( prop.getName() ) );
            }
        }
        sql.append( ") VALUES (" );
        sql.append( qMarks );
        sql.append( ")" );

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement( sql.toString() );
            int fidColumnValue = getFidColumnValue( fid, ftMapping );
            stmt.setInt( 1, fidColumnValue );
            int i = 2;
            for ( Property prop : f.getProperties() ) {
                if ( prop.getValue() != null ) {
                    Object oracleValue = getSQLValue(
                                                      (OracleConnection) ( (DelegatingConnection) conn ).getInnermostDelegate(),
                                                      f.getName(), prop );
                    stmt.setObject( i++, oracleValue );
                }
            }
            LOG.debug( "Executing: " + sql );
            stmt.execute();
        } catch ( SQLException e ) {
            throw new FeatureStoreException( "SQLException while inserting feature: " + e.getMessage(), e );
        } finally {
            JDBCUtils.close( stmt );
        }
        return fid;
    }

    private int getFidColumnValue( String fid, FeatureTypeMapping ftMapping )
                            throws FeatureStoreException {
        String prefix = ftMapping.getFeatureType().getLocalPart().toUpperCase() + "_";
        if ( !fid.startsWith( prefix ) ) {
            throw new FeatureStoreException( "Internal error. Cannot map fid '" + fid + "' to integer column." );
        }
        String substring = fid.substring( prefix.length() );
        int fidColumnValue = -1;
        try {
            fidColumnValue = Integer.parseInt( substring );
        } catch ( NumberFormatException e ) {
            throw new FeatureStoreException( "Internal error. Cannot map fid '" + fid + "' to integer column." );
        }
        return fidColumnValue;
    }

    private String determineNewFid( IDGenMode mode, String presentFid, FeatureTypeMapping ftMapping )
                            throws FeatureStoreException {

        String fid = null;
        switch ( mode ) {
        case GENERATE_NEW: {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = conn.prepareStatement( "SELECT MAX(" + ftMapping.getFidColumn() + ") + 1 FROM "
                                              + ftMapping.getFtTable() );
                rs = stmt.executeQuery();
                rs.next();
                String prefix = ftMapping.getFeatureType().getLocalPart().toUpperCase();
                fid = prefix + "_" + rs.getInt( 1 );
            } catch ( SQLException e ) {
                String msg = "SQLException during generation of new feature id: " + e.getMessage();
                LOG.error( msg, e );
                throw new FeatureStoreException( msg, e );
            } finally {
                JDBCUtils.close( rs, stmt, null, LOG );
            }
            break;
        }
        case REPLACE_DUPLICATE: {
            throw new FeatureStoreException( "Id generation mode REPLACE_DUPLICATE is not available yet." );
        }
        case USE_EXISTING: {
            String prefix = ftMapping.getFeatureType().getLocalPart().toUpperCase() + "_";
            if ( presentFid == null || !presentFid.startsWith( prefix ) ) {
                throw new FeatureStoreException( "Id generation mode is USE_EXISTING, but present id (" + presentFid
                                                 + ") does not start with canonical prefix '" + prefix + "'." );
            }
            fid = presentFid;
            break;
        }
        }
        return fid;
    }

    private Object getSQLValue( OracleConnection conn, QName ft, Property prop )
                            throws FeatureStoreException {
        Object value = null;
        PropertyType pt = prop.getType();
        if ( pt instanceof SimplePropertyType ) {
            SimplePropertyType spt = (SimplePropertyType) pt;
            String s = ( (PrimitiveValue) prop.getValue() ).getAsText();
            value = s;
            if ( spt.getPrimitiveType() == PrimitiveType.DECIMAL ) {
                // TODO Provide correct type information / remove this OpenJUMP hack
                if ( s.endsWith( ".0" ) ) {
                    value = s.substring( 0, s.length() - 2 );
                }
            }
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
            JGeometryAdapter jGeometryAdapter = fs.getJGeometryAdapter( ft, prop.getName() );
            JGeometry jg = jGeometryAdapter.toJGeometry( g );
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

        if ( filter == null ) {
            return 0;
        }
        if ( filter instanceof IdFilter ) {
            return performUpdate( (IdFilter) filter, replacementProps, lock );
        } else if ( filter instanceof OperatorFilter ) {
            throw new UnsupportedOperationException( "Updates based on non-id filters are currently not implemented "
                                                     + "for this feature store." );
        }
        return 0;
    }

    private int performUpdate( IdFilter filter, List<Property> replacementProps, Lock lock )
                            throws FeatureStoreException {

        if ( filter.getMatchingIds().isEmpty() ) {
            return 0;
        }

        FeatureTypeMapping ftMapping = null;
        for ( String fid : filter.getMatchingIds() ) {
            if ( ftMapping != null && fs.getMapping( fid ) != ftMapping ) {
                String msg = "Trying to update multiple feature types at once. This is currently not supported";
                throw new UnsupportedOperationException( msg );
            }
            ftMapping = fs.getMapping( fid );
        }

        int updated = -1;

        PreparedStatement stmt = null;
        try {
            OracleConnection oraConn = (OracleConnection) ( (DelegatingConnection) conn ).getInnermostDelegate();

            StringBuilder sql = new StringBuilder( "UPDATE " );
            sql.append( ftMapping.getFtTable() );
            sql.append( " SET " );
            boolean first = true;
            for ( Property replacementProp : replacementProps ) {
                if ( !first ) {
                    sql.append( ',' );
                }
                first = false;
                //  TODO handle non-trivial mapping
                String column = "" + ftMapping.getMapping( replacementProp.getType().getName() );
                if ( column == null ) {
                    String msg = "Cannot update property '" + replacementProp.getName() + "' not mapped to a column!?";
                    throw new FeatureStoreException( msg );
                }
                sql.append( column );
                sql.append( "=?" );
            }
            sql.append( " WHERE " );
            sql.append( ftMapping.getFidColumn() );
            sql.append( " IN(?" );
            for ( int i = 1; i < filter.getMatchingIds().size(); i++ ) {
                sql.append( ",?" );
            }
            sql.append( ")" );

            LOG.debug( "Updating: " + sql );
            stmt = conn.prepareStatement( sql.toString() );

            int i = 1;
            for ( Property property : replacementProps ) {
                Object sqlValue = getSQLValue( oraConn, ftMapping.getFeatureType(), property );
                stmt.setObject( i++, sqlValue );
            }
            for ( String fid : filter.getMatchingIds() ) {
                int delimPos = fid.indexOf( '_' );
                fid = fid.substring( delimPos + 1 );
                stmt.setString( i++, fid );
            }
            updated = stmt.executeUpdate();
        } catch ( SQLException e ) {
            String msg = "Error updating features using id filter: " + e.getMessage();
            throw new FeatureStoreException( msg, e );
        } finally {
            if ( stmt != null ) {
                JDBCUtils.close( stmt );
            }
        }
        return updated;
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
}
