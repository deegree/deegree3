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
import static org.deegree.commons.jdbc.ConnectionManager.getConnection;
import static org.deegree.feature.persistence.query.Query.QueryHint.HINT_LOOSE_BBOX;
import static org.deegree.feature.persistence.query.Query.QueryHint.HINT_NO_GEOMETRIES;
import static org.deegree.feature.persistence.query.Query.QueryHint.HINT_SCALE;
import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.ResultSetIterator;
import org.deegree.commons.jdbc.Util;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.GenericFeature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.CombinedResultSet;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.FilteredFeatureResultSet;
import org.deegree.feature.persistence.query.IteratorResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.Property;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
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
@LoggingNotes(info = "logs problems when connecting to the DB/getting data from the DB", debug = "logs the SQL statements sent to the SQL server", trace = "logs stack traces")
public class SimpleSQLDatastore implements FeatureStore {

    static final Logger LOG = getLogger( SimpleSQLDatastore.class );

    boolean available = false;

    String connId;

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
            LOG.error( "The invalid crs '{}' was specified for the simple SQL data store.", crs );
            LOG.trace( "Stack trace:", e );
        } catch ( UnknownCRSException e ) {
            LOG.error( "The invalid crs '{}' was specified for the simple SQL data store.", crs );
            LOG.trace( "Stack trace:", e );
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
        // nothing to do
    }

    public Envelope getEnvelope( QName ftName ) {
        ResultSet set = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = getConnection( connId );
            stmt = conn.prepareStatement( bbox );
            LOG.debug( "Getting bbox with query '{}'.", stmt );
            stmt.execute();
            set = stmt.getResultSet();
            if ( set.next() ) {
                Geometry g = WKTReader.read( set.getString( "bbox" ) );
                g.setCoordinateSystem( crs );
                return g.getEnvelope();
            }
        } catch ( SQLException e ) {
            LOG.info( "BBox could not be read: '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
            available = false;
            return null;
        } catch ( ParseException e ) {
            LOG.info( "BBox could not be read: '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
            available = false;
            return null;
        } finally {
            if ( set != null ) {
                try {
                    set.close();
                } catch ( SQLException e ) {
                    LOG.info( "A DB error occurred: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.info( "A DB error occurred: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( conn != null ) {
                try {
                    conn.close();
                } catch ( SQLException e ) {
                    LOG.info( "A DB error occurred: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
        return null;
    }

    public LockManager getLockManager()
                            throws FeatureStoreException {
        throw new FeatureStoreException( "Transactions are not implemented for the simple SQL datastore." );
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
        featureType = Util.determineFeatureType( featureName, namespace, connId, lods.values().iterator().next() );
        if ( featureType == null ) {
            available = false;
        } else {
            schema = new ApplicationSchema( new FeatureType[] { featureType }, null );
            available = true;
        }
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
        PreparedStatement stmt = null;
        Connection conn = null;
        FeatureResultSet set = null;
        try {

            LinkedList<FeatureResultSet> list = new LinkedList<FeatureResultSet>();

            for ( final Query q : queries ) {

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

                conn = getConnection( connId );
                stmt = conn.prepareStatement( sql );
                try {
                    bbox = (Envelope) transformer.transform( bbox );
                } catch ( UnknownCRSException e ) {
                    LOG.info( "Bounding box could not be transformed: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                } catch ( TransformationException e ) {
                    LOG.info( "Bounding box could not be transformed: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
                stmt.setString( 1, WKTWriter.write( bbox ) );
                LOG.debug( "Statement to fetch features was '{}'.", stmt );
                stmt.execute();

                set = new IteratorResultSet( new ResultSetIterator<Feature>( stmt.getResultSet(), conn, stmt ) {

                    @Override
                    protected Feature createElement( ResultSet rs )
                                            throws SQLException {
                        LinkedList<Property> props = new LinkedList<Property>();
                        for ( PropertyType pt : featureType.getPropertyDeclarations() ) {
                            if ( pt instanceof GeometryPropertyType ) {
                                if ( q.getHint( HINT_NO_GEOMETRIES ) != TRUE ) {
                                    byte[] bs = rs.getBytes( pt.getName().getLocalPart() );
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
                                            LOG.info( "WKB from the DB could not be parsed: '{}'.",
                                                      e.getLocalizedMessage() );
                                            LOG.trace( "Stack trace:", e );
                                        }
                                    }
                                }
                            } else {
                                Object obj = rs.getObject( pt.getName().getLocalPart() );
                                if ( obj != null ) {
                                    if ( obj instanceof Integer ) {
                                        props.add( new SimpleProperty( (SimplePropertyType) pt, "" + obj,
                                                                       PrimitiveType.INTEGER ) );
                                    } else if ( obj instanceof Double ) {
                                        props.add( new SimpleProperty( (SimplePropertyType) pt, "" + obj,
                                                                       PrimitiveType.DOUBLE ) );
                                    } else {
                                        props.add( new SimpleProperty( (SimplePropertyType) pt, "" + obj,
                                                                       PrimitiveType.STRING ) );
                                    }
                                }
                            }
                        }
                        return new GenericFeature( featureType, null, props, null );
                    }
                } );

                if ( q.getFilter() != null ) {
                    set = new FilteredFeatureResultSet( set, q.getFilter() );
                }

                list.add( set );
            }

            return new CombinedResultSet( list.iterator() );
        } catch ( SQLException e ) {
            LOG.info( "Data store could not be accessed: '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
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

    @Override
    public CRS getStorageSRS() {
        return crs;
    }
}
