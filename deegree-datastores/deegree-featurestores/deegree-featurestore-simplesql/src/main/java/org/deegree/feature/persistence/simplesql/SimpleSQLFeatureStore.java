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

import static java.lang.System.currentTimeMillis;
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

import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.jdbc.ResultSetIterator;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.CRSUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.db.ConnectionProvider;
import org.deegree.feature.Feature;
import org.deegree.feature.GenericFeature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.stream.CombinedFeatureInputStream;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.stream.FilteredFeatureInputStream;
import org.deegree.feature.stream.IteratorFeatureInputStream;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericAppSchema;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.io.WKBReader;
import org.deegree.geometry.io.WKTReader;
import org.deegree.geometry.io.WKTWriter;
import org.deegree.sqldialect.postgis.PostGISDialect;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;
import org.slf4j.Logger;

import org.locationtech.jts.io.ParseException;

/**
 * {@link FeatureStore} implementation that is backed by an SQL database and configured by providing an SQL statement /
 * an SQL connection.
 * 
 * @see FeatureStore
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(info = "logs problems when connecting to the DB/getting data from the DB", debug = "logs the SQL statements sent to the SQL server", trace = "logs stack traces")
public class SimpleSQLFeatureStore implements FeatureStore {

    static final Logger LOG = getLogger( SimpleSQLFeatureStore.class );

    private final QName ftName;

    private boolean available = false;

    ICRS crs;

    private AppSchema schema;

    private GeometryFactory fac = new GeometryFactory();

    GenericFeatureType featureType;

    private String bbox;

    private GeometryTransformer transformer;

    private TreeMap<Integer, String> lods;

    private Pair<Long, Envelope> cachedEnvelope = new Pair<Long, Envelope>();

    static int currentid = 0;

    private ResourceMetadata<FeatureStore> metadata;

    private ConnectionProvider connProvider;

    private boolean strict;

    /**
     * @param connId
     * @param crs
     * @param sql
     * @param ftLocalName
     * @param ftNamespace
     * @param ftPrefix
     * @param bbox
     * @param lods
     * @param metadata
     */
    public SimpleSQLFeatureStore( ConnectionProvider connProvider, String crs, String sql, String ftLocalName,
                                  String ftNamespace, String ftPrefix, String bbox, List<Pair<Integer, String>> lods,
                                  ResourceMetadata<FeatureStore> metadata ) {
        this.connProvider = connProvider;
        this.metadata = metadata;

        sql = sql.trim();
        if ( sql.endsWith( ";" ) ) {
            sql = sql.substring( 0, sql.length() - 1 );
        }
        this.bbox = bbox;

        // TODO allow null namespaces / empty prefix
        // NOTE: verify that the WFS code for dealing with that (e.g. repairing unqualified names) works with that first
        ftLocalName = ( ftLocalName != null && !ftLocalName.isEmpty() ) ? ftLocalName : "Feature";
        ftNamespace = ( ftNamespace != null && !ftNamespace.isEmpty() ) ? ftNamespace : "http://www.deegree.org/app";
        ftPrefix = ( ftPrefix != null && !ftPrefix.isEmpty() ) ? ftPrefix : "app";
        this.ftName = new QName( ftNamespace, ftLocalName, ftPrefix );

        try {
            this.crs = CRSManager.lookup( crs );
            transformer = new GeometryTransformer( this.crs );
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

    @Override
    public Envelope getEnvelope( QName ftName )
                            throws FeatureStoreException {
        return calcEnvelope( ftName );
    }

    public Envelope calcEnvelope( QName ftName ) {
        synchronized ( cachedEnvelope ) {
            long current = currentTimeMillis();
            if ( cachedEnvelope.first != null && ( current - cachedEnvelope.first ) < 1000 ) {
                return cachedEnvelope.second;
            }
            ResultSet set = null;
            PreparedStatement stmt = null;
            Connection conn = null;
            try {
                conn = connProvider.getConnection();
                stmt = conn.prepareStatement( bbox );
                LOG.debug( "Getting bbox with query '{}'.", stmt );
                stmt.execute();
                set = stmt.getResultSet();
                if ( set.next() ) {
                    String bboxString = set.getString( "bbox" );
                    if ( bboxString == null ) {
                        LOG.info( "Could not determine envelope of database table, using world bbox instead." );
                        return fac.createEnvelope( -180, -90, 180, 90, CRSUtils.EPSG_4326 );
                    }
                    Geometry g = new WKTReader( crs ).read( bboxString );
                    cachedEnvelope.first = current;
                    cachedEnvelope.second = g.getEnvelope();
                    return cachedEnvelope.second;
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
                JDBCUtils.close( set, stmt, conn, LOG );
            }
            return null;
        }
    }

    public LockManager getLockManager()
                            throws FeatureStoreException {
        throw new FeatureStoreException( "Transactions are not implemented for the simple SQL datastore." );
    }

    public GMLObject getObjectById( String id )
                            throws FeatureStoreException {
        throw new FeatureStoreException( "Getting objects by id is not implemented for the simple SQL datastore." );
    }

    public AppSchema getSchema() {
        return schema;
    }

    @Override
    public boolean isMapped( QName ftName ) {
        return schema.getFeatureType( ftName ) != null;
    }

    /**
     * @return the feature type (it can have only one)
     */
    public GenericFeatureType getFeatureType() {
        return featureType;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    public FeatureInputStream query( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        return query( new Query[] { query } );
    }

    public FeatureInputStream query( final Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        PreparedStatement stmt = null;
        Connection conn = null;
        FeatureInputStream set = null;
        try {

            LinkedList<FeatureInputStream> list = new LinkedList<FeatureInputStream>();

            for ( final Query q : queries ) {

                Envelope bbox = q.getPrefilterBBoxEnvelope();
                if ( bbox == null ) {
                    bbox = calcEnvelope( ftName );
                }

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

                conn = connProvider.getConnection();

                if ( q.getMaxFeatures() > 0 && connProvider.getDialect() instanceof PostGISDialect ) {
                    sql += " limit " + q.getMaxFeatures();
                }

                stmt = conn.prepareStatement( sql );
                try {
                    bbox = transformer.transform( bbox );
                } catch ( UnknownCRSException e ) {
                    LOG.info( "Bounding box could not be transformed: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                } catch ( TransformationException e ) {
                    LOG.info( "Bounding box could not be transformed: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
                int parameterCount = stmt.getParameterMetaData().getParameterCount();
                if ( parameterCount == 0 ) {
                    LOG.info( "No parameter for the bbox was found, requesting without bbox!" );
                } else if ( parameterCount > 1 ) {
                    LOG.warn( "Too many parameters specified ({}), cannot go further!", parameterCount );
                    return null;
                }
                stmt.setString( 1, WKTWriter.write( bbox ) );
                LOG.debug( "Statement to fetch features was '{}'.",
                           connProvider.getClass().getSimpleName().equals( "OracleDialect" ) ? sql : stmt );
                stmt.execute();

                set = new IteratorFeatureInputStream(
                                                      new ResultSetIterator<Feature>( stmt.getResultSet(), conn, stmt ) {

                                                          @Override
                                                          protected Feature createElement( ResultSet rs )
                                                                                  throws SQLException {

                                                              LinkedList<Property> props = new LinkedList<Property>();
                                                              for ( PropertyType pt : featureType.getPropertyDeclarations() ) {
                                                                  if ( pt instanceof GeometryPropertyType ) {
                                                                      byte[] bs = rs.getBytes( pt.getName().getLocalPart() );
                                                                      if ( bs != null ) {
                                                                          try {
                                                                              Geometry geom = WKBReader.read( bs, crs );
                                                                              props.add( new GenericProperty( pt, geom ) );
                                                                          } catch ( ParseException e ) {
                                                                              LOG.info( "WKB from the DB could not be parsed: '{}'.",
                                                                                        e.getLocalizedMessage() );
                                                                              LOG.info( "For PostGIS users: you have to select the geometry field 'asbinary(geometry)'." );
                                                                              LOG.trace( "Stack trace:", e );
                                                                          }
                                                                      }
                                                                  } else {
                                                                      Object obj = rs.getObject( pt.getName().getLocalPart() );
                                                                      if ( obj != null ) {
                                                                          SimplePropertyType spt = (SimplePropertyType) pt;
                                                                          props.add( new SimpleProperty( spt, "" + obj ) );
                                                                      }
                                                                  }
                                                              }
                                                              return new GenericFeature( featureType,
                                                                                         "ID_" + ( ++currentid ),
                                                                                         props, null );
                                                          }
                                                      } );

                if ( q.getFilter() != null ) {
                    set = new FilteredFeatureInputStream( set, q.getFilter() );
                }

                list.add( set );
            }

            return new CombinedFeatureInputStream( list.iterator() );
        } catch ( SQLException e ) {
            LOG.info( "Data store could not be accessed: '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
            available = false;
            throw new FeatureStoreException( "Data store could not be accessed." );
        }
    }

    public int queryHits( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO SELECT COUNT
        return query( query ).count();
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

    /**
     * Returns the CRS of the geometry column.
     * 
     * @return the CRS of the geometry column, never <code>null</code>
     */
    public ICRS getStorageCRS() {
        return crs;
    }

    @Override
    public ResourceMetadata<? extends Resource> getMetadata() {
        return metadata;
    }

    @Override
    public void init() {
        featureType = DbFeatureUtils.determineFeatureType( ftName, connProvider, lods.values().iterator().next() );
        if ( featureType == null ) {
            available = false;
        } else {
            schema = new GenericAppSchema( new FeatureType[] { featureType }, null, null, null, null, null );
            available = true;
        }
    }

    @Override
    public void destroy() {
        // unused
    }

}
