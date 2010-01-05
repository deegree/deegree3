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
package org.deegree.feature.persistence.simplesql;

import static java.lang.Boolean.TRUE;
import static java.sql.Types.BINARY;
import static java.sql.Types.BIT;
import static java.sql.Types.CHAR;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.INTEGER;
import static java.sql.Types.NUMERIC;
import static java.sql.Types.OTHER;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.VARCHAR;
import static org.deegree.commons.jdbc.ConnectionManager.getConnection;
import static org.deegree.feature.persistence.query.Query.QueryHint.HINT_LOOSE_BBOX;
import static org.deegree.feature.persistence.query.Query.QueryHint.HINT_NO_GEOMETRIES;
import static org.deegree.feature.persistence.query.Query.QueryHint.HINT_SCALE;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2_OR_3;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.GEOMETRY;
import static org.deegree.feature.types.property.PrimitiveType.BOOLEAN;
import static org.deegree.feature.types.property.PrimitiveType.DECIMAL;
import static org.deegree.feature.types.property.PrimitiveType.STRING;
import static org.slf4j.LoggerFactory.getLogger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.CloseableIterator;
import org.deegree.commons.utils.Pair;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.GenericFeature;
import org.deegree.feature.GenericProperty;
import org.deegree.feature.Property;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.StoredFeatureTypeMetadata;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.CombinedResultSet;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.FilteredFeatureResultSet;
import org.deegree.feature.persistence.query.IteratorResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PrimitiveType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.io.WKBReader;
import org.deegree.geometry.io.WKTReader;
import org.deegree.geometry.io.WKTWriter;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.gml.GMLObject;
import org.slf4j.Logger;

import com.vividsolutions.jts.io.ParseException;

/**
 * <code>SimpleSQLDatastore</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SimpleSQLDatastore implements FeatureStore {

    static final Logger LOG = getLogger( SimpleSQLDatastore.class );

    boolean available = false;

    private String connId;

    Connection conn;

    private StoredFeatureTypeMetadata metadata;

    CRS crs;

    private ApplicationSchema schema;

    GeometryFactory fac = new GeometryFactory();

    private String featureName;

    private String namespace;

    GenericFeatureType featureType;

    private String bbox;

    GeometryTransformer transformer;

    TreeMap<Integer, String> lods;

    /**
     * @param connId
     * @param crs
     * @param sql
     * @param featureName
     * @param namespace
     * @param bbox
     * @param lods
     */
    public SimpleSQLDatastore( String connId, String crs, String sql, String featureName, String namespace,
                               String bbox, List<Pair<Integer, String>> lods ) {
        this.connId = connId;
        this.crs = new CRS( crs );
        sql = sql.trim();
        if ( sql.endsWith( ";" ) ) {
            sql = sql.substring( 0, sql.length() - 1 );
        }
        this.bbox = bbox;
        this.featureName = featureName == null ? "feature" : featureName;
        this.namespace = namespace;
        try {
            transformer = new GeometryTransformer( this.crs.getWrappedCRS() );
        } catch ( IllegalArgumentException e ) {
            LOG.error( "Stack trace:", e );
        } catch ( UnknownCRSException e ) {
            LOG.error( "The invalid crs '{}' was specified for the simple SQL data store.", crs );
            LOG.debug( "Stack trace:", e );
        }
        this.lods = new TreeMap<Integer, String>();
        this.lods.put( -1, sql );
        for ( Pair<Integer, String> p : lods ) {
            this.lods.put( p.first, p.second );
        }
    }

    public FeatureStoreTransaction acquireTransaction()
                            throws FeatureStoreException {
        throw new FeatureStoreException( "Transactions are not implemented for the simple SQL datastore." );
    }

    public void destroy() {
        try {
            if ( conn != null ) {
                conn.close();
            }
        } catch ( SQLException e ) {
            LOG.warn( "Connection could not be closed: '{}'.", e.getLocalizedMessage() );
            LOG.debug( "Stack trace:", e );
        }
    }

    public Envelope getEnvelope( QName ftName ) {
        ResultSet set = null;
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement( bbox );
            stmt.execute();
            set = stmt.getResultSet();
            if ( set.next() ) {
                Geometry g = WKTReader.read( set.getString( "bbox" ) );
                g.setCoordinateSystem( crs );
                return g.getEnvelope();
            }
        } catch ( SQLException e ) {
            LOG.warn( "BBox could not be read: '{}'.", e.getLocalizedMessage() );
            LOG.debug( "Stack trace:", e );
            available = false;
            return null;
        } catch ( ParseException e ) {
            LOG.warn( "BBox could not be read: '{}'.", e.getLocalizedMessage() );
            LOG.debug( "Stack trace:", e );
            available = false;
            return null;
        } finally {
            if ( set != null ) {
                try {
                    set.close();
                } catch ( SQLException e ) {
                    LOG.warn( "A DB error occurred: '{}'.", e.getLocalizedMessage() );
                    LOG.debug( "Stack trace:", e );
                }
            }
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.warn( "A DB error occurred: '{}'.", e.getLocalizedMessage() );
                    LOG.debug( "Stack trace:", e );
                }
            }
        }
        return null;
    }

    public LockManager getLockManager()
                            throws FeatureStoreException {
        throw new FeatureStoreException( "Transactions are not implemented for the simple SQL datastore." );
    }

    public StoredFeatureTypeMetadata getMetadata( QName ftName ) {
        return metadata;
    }

    public GMLObject getObjectById( String id )
                            throws FeatureStoreException {
        throw new FeatureStoreException( "Getting objects by id is not implemented for the simple SQL datastore." );
    }

    public ApplicationSchema getSchema() {
        return schema;
    }

    public void init()
                            throws FeatureStoreException {
        ResultSet set = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection( connId );
            stmt = conn.prepareStatement( lods.values().iterator().next() + " limit 0" );
            stmt.setString( 1, WKTWriter.write( fac.createEnvelope( 0, 0, 1, 1, null ) ) );
            stmt.execute();
            set = stmt.getResultSet();
            ResultSetMetaData md = set.getMetaData();
            LinkedList<PropertyType<?>> ps = new LinkedList<PropertyType<?>>();
            for ( int i = 1; i <= md.getColumnCount(); ++i ) {
                String name = md.getColumnLabel( i );

                PropertyType<?> pt;
                int colType = md.getColumnType( i );
                switch ( colType ) {
                case VARCHAR:
                case CHAR:
                    pt = new SimplePropertyType<String>( new QName( namespace, name ), 0, 1, STRING, false, null );
                    break;
                case INTEGER:
                case SMALLINT:
                    pt = new SimplePropertyType<String>( new QName( namespace, name ), 0, 1, PrimitiveType.INTEGER,
                                                         false, null );
                    break;
                case BIT:
                    pt = new SimplePropertyType<String>( new QName( namespace, name ), 0, 1, BOOLEAN, false, null );
                    break;
                case NUMERIC:
                case DOUBLE:
                    pt = new SimplePropertyType<String>( new QName( namespace, name ), 0, 1, DECIMAL, false, null );
                    break;
                case OTHER:
                case BINARY:
                    pt = new GeometryPropertyType( new QName( namespace, name ), 0, 1, GEOMETRY, DIM_2_OR_3, false,
                                                   null, null );
                    break;
                default:
                    LOG.error( "Unsupported data type '{}'.", colType );
                    continue;
                }

                ps.add( pt );
            }

            featureType = new GenericFeatureType( new QName( namespace, featureName ), (List) ps, false );

            metadata = new StoredFeatureTypeMetadata( featureType, this, null, null, crs );
            schema = new ApplicationSchema( new FeatureType[] { featureType }, null );
        } catch ( SQLException e ) {
            LOG.warn( "Data store could not be initialized: '{}'.", e.getLocalizedMessage() );
            LOG.debug( "Stack trace:", e );
            available = false;
            return;
        } finally {
            if ( set != null ) {
                try {
                    set.close();
                } catch ( SQLException e ) {
                    LOG.warn( "A DB error occurred: '{}'.", e.getLocalizedMessage() );
                    LOG.debug( "Stack trace:", e );
                }
            }
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.warn( "A DB error occurred: '{}'.", e.getLocalizedMessage() );
                    LOG.debug( "Stack trace:", e );
                }
            }
        }

        available = true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    public FeatureResultSet query( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        return query( new Query[] { query } );
    }

    public FeatureResultSet query( final Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        try {
            LinkedList<FeatureResultSet> list = new LinkedList<FeatureResultSet>();

            for ( final Query q : queries ) {
                FeatureResultSet set = new IteratorResultSet( new CloseableIterator<Feature>() {
                    ResultSet set = null;

                    PreparedStatement stmt = null;

                    {
                        Envelope bbox = (Envelope) q.getHint( HINT_LOOSE_BBOX );
                        Object scaleHint = q.getHint( HINT_SCALE );
                        int scale = -1;
                        if ( scaleHint != null ) {
                            scale = (Integer) scaleHint;
                        }
                        String sql = null;
                        for ( Integer i : lods.keySet() ) {
                            if ( i <= scale ) {
                                LOG.debug( "Considering use of LOD with scale {}.", i );
                                sql = lods.get( i );
                            }
                        }

                        stmt = conn.prepareStatement( sql );
                        try {
                            bbox = (Envelope) transformer.transform( bbox );
                        } catch ( UnknownCRSException e ) {
                            LOG.warn( "Bounding box could not be transformed: '{}'.", e.getLocalizedMessage() );
                            LOG.debug( "Stack trace:", e );
                        } catch ( TransformationException e ) {
                            LOG.warn( "Bounding box could not be transformed: '{}'.", e.getLocalizedMessage() );
                            LOG.debug( "Stack trace:", e );
                        }
                        stmt.setString( 1, WKTWriter.write( bbox ) );
                        LOG.debug( "Statement to fetch features was '{}'.", stmt );
                        stmt.execute();
                        set = stmt.getResultSet();
                    }

                    public void close() {
                        if ( set != null ) {
                            try {
                                set.close();
                            } catch ( SQLException e ) {
                                LOG.warn( "A DB error occurred: '{}'.", e.getLocalizedMessage() );
                                LOG.debug( "Stack trace:", e );
                            }
                        }
                        if ( stmt != null ) {
                            try {
                                stmt.close();
                            } catch ( SQLException e ) {
                                LOG.warn( "A DB error occurred: '{}'.", e.getLocalizedMessage() );
                                LOG.debug( "Stack trace:", e );
                            }
                        }
                    }

                    public Collection<Feature> getAsCollectionAndClose( Collection<Feature> collection ) {
                        while ( hasNext() ) {
                            collection.add( next() );
                        }
                        return collection;
                    }

                    public List<Feature> getAsListAndClose() {
                        return (List<Feature>) getAsCollectionAndClose( new LinkedList<Feature>() );
                    }

                    public boolean hasNext() {
                        try {
                            return !set.isLast();
                        } catch ( SQLException e ) {
                            LOG.warn( "Data store could not be accessed: '{}'.", e.getLocalizedMessage() );
                            LOG.debug( "Stack trace:", e );
                            available = false;
                        }
                        return false;
                    }

                    public Feature next() {
                        if ( !hasNext() ) {
                            return null;
                        }
                        try {
                            if ( set.next() ) {
                                LinkedList<Property<?>> props = new LinkedList<Property<?>>();
                                for ( PropertyType<?> pt : featureType.getPropertyDeclarations() ) {
                                    if ( pt instanceof GeometryPropertyType ) {
                                        if ( q.getHint( HINT_NO_GEOMETRIES ) != TRUE ) {
                                            byte[] bs = set.getBytes( pt.getName().getLocalPart() );
                                            if ( bs != null ) {
                                                try {
                                                    Geometry geom = WKBReader.read( bs );
                                                    geom.setCoordinateSystem( crs );
                                                    if ( geom instanceof MultiGeometry<?> ) {
                                                        for ( Geometry g : (MultiGeometry<?>) geom ) {
                                                            g.setCoordinateSystem( crs );
                                                        }
                                                    }
                                                    props.add( new GenericProperty( pt, geom ) );
                                                } catch ( ParseException e ) {
                                                    LOG.warn( "WKB from the DB could not be parsed: '{}'.",
                                                              e.getLocalizedMessage() );
                                                    LOG.debug( "Stack trace:", e );
                                                }
                                            }
                                        }
                                    } else {
                                        Object obj = set.getObject( pt.getName().getLocalPart() );
                                        if ( obj != null ) {
                                            if ( obj instanceof Integer ) {
                                                BigInteger theInt = new BigInteger( ( (Integer) obj ).toString() );
                                                props.add( new GenericProperty( pt, theInt ) );
                                            } else if ( obj instanceof Double ) {
                                                BigDecimal dec = new BigDecimal( (Double) obj );
                                                props.add( new GenericProperty( pt, dec ) );
                                            } else {
                                                props.add( new GenericProperty( pt, obj ) );
                                            }
                                        }
                                    }
                                }
                                return new GenericFeature( featureType, null, (List) props, null );
                            }
                        } catch ( SQLException e ) {
                            LOG.warn( "Data store could not be accessed: '{}'.", e.getLocalizedMessage() );
                            LOG.debug( "Stack trace:", e );
                            available = false;
                        }
                        return null;
                    }

                    public void remove() {
                        try {
                            set.next();
                        } catch ( SQLException e ) {
                            LOG.warn( "Data store could not be accessed: '{}'.", e.getLocalizedMessage() );
                            LOG.debug( "Stack trace:", e );
                            available = false;
                        }
                    }
                } );

                if ( q.getFilter() != null ) {
                    set = new FilteredFeatureResultSet( set, q.getFilter() );
                }

                list.add( set );
            }

            return new CombinedResultSet( list.iterator() );
        } catch ( SQLException e ) {
            LOG.warn( "Data store could not be accessed: '{}'.", e.getLocalizedMessage() );
            LOG.debug( "Stack trace:", e );
            available = false;
            throw new FeatureStoreException( "Data store could not be accessed." );
        }
    }

    public int queryHits( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO
        return query( query ).toCollection().size();
    }

    public int queryHits( Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO
        return query( queries ).toCollection().size();
    }

}
