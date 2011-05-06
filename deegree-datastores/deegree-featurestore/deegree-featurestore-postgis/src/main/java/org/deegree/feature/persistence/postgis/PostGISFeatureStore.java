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

import static org.deegree.commons.utils.JDBCUtils.close;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.namespace.QName;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreGMLIdResolver;
import org.deegree.feature.persistence.sql.AbstractSQLFeatureStore;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.MappedApplicationSchema;
import org.deegree.feature.persistence.sql.blob.BlobCodec;
import org.deegree.feature.persistence.sql.blob.BlobMapping;
import org.deegree.feature.persistence.sql.config.MappedSchemaBuilderGML;
import org.deegree.feature.persistence.sql.id.IdAnalysis;
import org.deegree.feature.persistence.sql.jaxb.SQLFeatureStoreJAXB;
import org.deegree.feature.persistence.sql.rules.FeatureBuilderRelational;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.Mappings;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.sql.AbstractWhereBuilder;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.MappingExpression;
import org.deegree.filter.sql.PropertyNameMapper;
import org.deegree.filter.sql.PropertyNameMapping;
import org.deegree.filter.sql.TableAliasManager;
import org.deegree.filter.sql.UnmappableException;
import org.deegree.filter.sql.postgis.PostGISWhereBuilder;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKBReader;
import org.deegree.geometry.io.WKBWriter;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.geometry.utils.GeometryUtils;
import org.deegree.gml.GMLObject;
import org.postgis.PGboxbase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.io.ParseException;

/**
 * {@link AbstractSQLFeatureStore} implementation that uses a PostGIS/PostgreSQL database as backend.
 * 
 * TODO always ensure that autocommit is false and fetch size is set for (possibly) large SELECTS
 * 
 * @see AbstractSQLFeatureStore
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISFeatureStore extends AbstractSQLFeatureStore {

    static final Logger LOG = LoggerFactory.getLogger( PostGISFeatureStore.class );

    // if true, use old-style for spatial predicates (e.g "intersects" instead of "ST_Intersects")
    boolean useLegacyPredicates;

    private SQLFeatureStoreJAXB config;

    private final URL configURL;

    /**
     * Creates a new {@link PostGISFeatureStore} for the given {@link ApplicationSchema}.
     * 
     * @param config
     *            jaxb configuration object
     * @param configURL
     *            configuration systemid
     * @param workspace
     *            deegree workspace, must not be <code>null</code>
     */
    protected PostGISFeatureStore( SQLFeatureStoreJAXB config, URL configURL, DeegreeWorkspace workspace ) {
        this.config = config;
        this.configURL = configURL;
    }

    @Override
    public void init( DeegreeWorkspace workspace )
                            throws ResourceInitException {

        LOG.debug( "init" );

        MappedApplicationSchema schema;
        try {
            schema = MappedSchemaBuilderGML.build( configURL.toString(), config );
        } catch ( Throwable t ) {
            LOG.error( t.getMessage(), t );
            throw new ResourceInitException( t.getMessage(), t );
        }
        String jdbcConnId = config.getJDBCConnId();
        init( schema, jdbcConnId );

        // lockManager = new DefaultLockManager( this, "LOCK_DB" );

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection( getConnId() );
            useLegacyPredicates = JDBCUtils.useLegayPostGISPredicates( conn, LOG );
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new ResourceInitException( e.getMessage(), e );
        } finally {
            close( rs, stmt, conn, LOG );
        }
    }

    protected Envelope getEnvelope( FeatureTypeMapping ftMapping )
                            throws FeatureStoreException {

        LOG.trace( "Determining BBOX for feature type '{}' (relational mode)", ftMapping.getFeatureType() );

        String column = null;
        FeatureType ft = getSchema().getFeatureType( ftMapping.getFeatureType() );
        GeometryPropertyType pt = ft.getDefaultGeometryPropertyDeclaration();
        if ( pt == null ) {
            return null;
        }
        Mapping propMapping = ftMapping.getMapping( pt.getName() );
        GeometryMapping mapping = Mappings.getGeometryMapping( propMapping );
        if ( mapping == null ) {
            return null;
        }
        MappingExpression me = mapping.getMapping();
        if ( me == null || !( me instanceof DBField ) ) {
            String msg = "Cannot determine BBOX for feature type '" + ft.getName() + "' (relational mode).";
            LOG.warn( msg );
            return null;
        }
        column = ( (DBField) me ).getColumn();

        Envelope env = null;
        StringBuilder sql = new StringBuilder( "SELECT " );
        if ( useLegacyPredicates ) {
            sql.append( "extent" );
        } else {
            sql.append( "ST_Extent" );
        }
        sql.append( "(" );
        sql.append( column );
        sql.append( ")::BOX2D FROM " );
        sql.append( ftMapping.getFtTable() );

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection( getConnId() );
            stmt = conn.createStatement();
            rs = stmt.executeQuery( sql.toString() );
            rs.next();
            PGboxbase pgBox = (PGboxbase) rs.getObject( 1 );
            if ( pgBox != null ) {
                ICRS crs = mapping.getCRS();
                org.deegree.geometry.primitive.Point min = buildPoint( pgBox.getLLB(), crs );
                org.deegree.geometry.primitive.Point max = buildPoint( pgBox.getURT(), crs );
                env = new DefaultEnvelope( null, crs, null, min, max );
            }
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new FeatureStoreException( e.getMessage(), e );
        } finally {
            close( rs, stmt, conn, LOG );
        }
        return env;
    }

    protected Envelope getEnvelope( QName ftName, BlobMapping blobMapping )
                            throws FeatureStoreException {

        LOG.debug( "Determining BBOX for feature type '{}' (BLOB mode)", ftName );

        int ftId = getFtId( ftName );
        String column = blobMapping.getBBoxColumn();

        Envelope env = null;
        StringBuilder sql = new StringBuilder( "SELECT " );
        if ( useLegacyPredicates ) {
            sql.append( "extent" );
        } else {
            sql.append( "ST_Extent" );
        }
        sql.append( "(" );
        sql.append( column );
        sql.append( ")::BOX2D FROM " );
        sql.append( blobMapping.getTable() );
        sql.append( " WHERE " );
        sql.append( blobMapping.getTypeColumn() );
        sql.append( "=" );
        sql.append( ftId );

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection( getConnId() );
            stmt = conn.createStatement();
            rs = stmt.executeQuery( sql.toString() );
            rs.next();
            PGboxbase pgBox = (PGboxbase) rs.getObject( 1 );
            if ( pgBox != null ) {
                ICRS crs = blobMapping.getCRS();
                org.deegree.geometry.primitive.Point min = buildPoint( pgBox.getLLB(), crs );
                org.deegree.geometry.primitive.Point max = buildPoint( pgBox.getURT(), crs );
                env = new DefaultEnvelope( null, blobMapping.getCRS(), null, min, max );
            }
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new FeatureStoreException( e.getMessage(), e );
        } finally {
            close( rs, stmt, conn, LOG );
        }
        return env;
    }

    private org.deegree.geometry.primitive.Point buildPoint( org.postgis.Point p, ICRS crs ) {
        double[] coords = new double[p.getDimension()];
        coords[0] = p.getX();
        coords[1] = p.getY();
        if ( p.getDimension() > 2 ) {
            coords[2] = p.getZ();
        }
        return new DefaultPoint( null, crs, null, coords );
    }

    @Override
    protected GMLObject getObjectByIdRelational( String id )
                            throws FeatureStoreException {

        GMLObject result = null;

        IdAnalysis idAnalysis = getSchema().analyzeId( id );
        if ( !idAnalysis.isFid() ) {
            String msg = "Fetching of geometries by id (relational mode) is not implemented yet.";
            throw new UnsupportedOperationException( msg );
        }

        FeatureType ft = idAnalysis.getFeatureType();
        FeatureTypeMapping mapping = getSchema().getFtMapping( ft.getName() );

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuilder sql = new StringBuilder( "SELECT " );
            sql.append( mapping.getFidMapping().getColumn() );
            for ( PropertyType pt : ft.getPropertyDeclarations() ) {
                // append every (mapped) property to SELECT list
                // TODO columns in related tables with 1:1 relation
                Mapping mapping2 = mapping.getMapping( pt.getName() );
                MappingExpression column = mapping2 == null ? null : Mappings.getMappingExpression( mapping2 );
                if ( column != null ) {
                    sql.append( ',' );
                    if ( pt instanceof SimplePropertyType ) {
                        sql.append( column );
                    } else if ( pt instanceof GeometryPropertyType ) {
                        if ( useLegacyPredicates ) {
                            sql.append( "AsBinary(" );
                        } else {
                            sql.append( "ST_AsBinary(" );
                        }
                        sql.append( column );
                        sql.append( ')' );
                    } else if ( pt instanceof FeaturePropertyType ) {
                        sql.append( column );
                    } else {
                        LOG.warn( "Skipping property '" + pt.getName() + "' -- type '" + pt.getClass()
                                  + "' not handled in PostGISFeatureStore#getObjectByIdRelational()." );
                    }
                }
            }
            sql.append( " FROM " );
            sql.append( mapping.getFtTable() );
            sql.append( " WHERE " );
            sql.append( mapping.getFidMapping().getColumns().get( 0 ).first );
            sql.append( "=?" );
            for ( int i = 1; i < mapping.getFidMapping().getColumns().size(); i++ ) {
                sql.append( " AND " );
                sql.append( mapping.getFidMapping().getColumns().get( i ).first );
                sql.append( "=?" );
            }

            LOG.debug( "Preparing SELECT: " + sql );

            conn = ConnectionManager.getConnection( getConnId() );
            stmt = conn.prepareStatement( sql.toString() );

            // TODO proper SQL type handling
            for ( int i = 0; i < idAnalysis.getIdKernels().length; i++ ) {
                stmt.setInt( 1, Integer.parseInt( idAnalysis.getIdKernels()[i] ) );
            }

            rs = stmt.executeQuery();
            if ( rs.next() ) {
                result = new FeatureBuilderRelational( this, ft, mapping, conn ).buildFeature( rs );
            }
        } catch ( Exception e ) {
            String msg = "Error retrieving object by id (relational mode): " + e.getMessage();
            LOG.error( msg, e );
            throw new FeatureStoreException( msg, e );
        } finally {
            close( rs, stmt, conn, LOG );
        }
        return result;
    }

    @Override
    protected GMLObject getObjectByIdBlob( String id, BlobMapping blobMapping )
                            throws FeatureStoreException {

        GMLObject geomOrFeature = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuilder sql = new StringBuilder( "SELECT " );
            sql.append( blobMapping.getDataColumn() );
            sql.append( " FROM " );
            sql.append( blobMapping.getTable() );
            sql.append( " WHERE " );
            sql.append( blobMapping.getGMLIdColumn() );
            sql.append( "=?" );

            conn = ConnectionManager.getConnection( getConnId() );
            stmt = conn.prepareStatement( sql.toString() );
            stmt.setString( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                LOG.debug( "Recreating object '" + id + "' from bytea." );
                BlobCodec codec = blobMapping.getCodec();
                geomOrFeature = codec.decode( rs.getBinaryStream( 1 ), getNamespaceContext(), getSchema(),
                                              blobMapping.getCRS(), new FeatureStoreGMLIdResolver( this ) );
                getCache().add( geomOrFeature );
            }
        } catch ( Exception e ) {
            String msg = "Error retrieving object by id (BLOB mode): " + e.getMessage();
            LOG.debug( msg, e );
            throw new FeatureStoreException( msg, e );
        } finally {
            close( rs, stmt, conn, LOG );
        }
        return geomOrFeature;
    }

    @Override
    public String[] getDDL() {
        return new PostGISDDLCreator( getSchema() ).getDDL();
    }

    @Override
    public ParticleConverter<Geometry> getGeometryConverter( final GeometryMapping gm ) {

        final String column = ( (DBField) gm.getMapping() ).getColumn();

        return new ParticleConverter<Geometry>() {
            @Override
            public String getSelectSnippet( String tableAlias ) {
                if ( tableAlias != null ) {
                    return "ST_AsEWKB(" + tableAlias + "." + column + ")";
                }
                return "ST_AsEWKB(" + column + ")";
            }

            @Override
            public String getSetSnippet() {
                StringBuilder sb = new StringBuilder();
                if ( useLegacyPredicates ) {
                    sb.append( "SetSRID(GeomFromWKB(?)," );
                } else {
                    sb.append( "SetSRID(ST_GeomFromWKB(?)," );
                }
                sb.append( gm.getSrid() == null ? "-1" : gm.getSrid() );
                sb.append( ")" );
                return sb.toString();
            }

            @Override
            public Geometry toParticle( Object sqlValue ) {
                if ( sqlValue == null ) {
                    return null;
                }
                try {
                    return WKBReader.read( (byte[]) sqlValue, gm.getCRS() );
                } catch ( Throwable t ) {
                    throw new IllegalArgumentException( t.getMessage(), t );
                }
            }

            @Override
            public Object toSQLArgument( Geometry particle, Connection conn ) {
                if ( particle == null ) {
                    return null;
                }
                try {
                    Geometry compatible = getCompatibleGeometry( particle, gm.getCRS() );
                    return WKBWriter.write( compatible );
                } catch ( Throwable t ) {
                    throw new IllegalArgumentException(t.getMessage(), t);
                }
            }
        };
    }

    @Override
    protected AbstractWhereBuilder getWhereBuilder( FeatureType ft, OperatorFilter filter, SortProperty[] sortCrit,
                                                    Connection conn )
                            throws FilterEvaluationException {
        return new PostGISWhereBuilder( new PostGISFeatureMapping( getSchema(), ft, getMapping( ft.getName() ), this ),
                                        filter, sortCrit, useLegacyPredicates );
    }

    @Override
    protected AbstractWhereBuilder getWhereBuilderBlob( OperatorFilter filter, Connection conn )
                            throws FilterEvaluationException {
        PropertyNameMapper pgMapping = new PropertyNameMapper() {
            @Override
            public byte[] getSQLValue( Geometry literal, PropertyName propName )
                                    throws FilterEvaluationException {

                Envelope env = (Envelope) getCompatibleGeometry( literal, blobMapping.getCRS() );
                org.deegree.geometry.primitive.Polygon polygon = GeometryUtils.envelopeToPolygon( env );
                byte[] wkb = null;
                try {
                    wkb = WKBWriter.write( polygon );
                } catch ( ParseException e ) {
                    throw new FilterEvaluationException( e.getMessage() );
                }
                return wkb;
            }

            @Override
            public Object getSQLValue( Literal<?> literal, PropertyName propName )
                                    throws FilterEvaluationException {
                throw new UnsupportedOperationException();
            }

            @Override
            public PropertyNameMapping getMapping( PropertyName propName, TableAliasManager aliasManager )
                                    throws FilterEvaluationException, UnmappableException {
                return new PropertyNameMapping( aliasManager.getRootTableAlias(), blobMapping.getBBoxColumn(),
                                                blobMapping.getCRS(), "-1" );
            }
        };
        return new PostGISWhereBuilder( pgMapping, filter, null, useLegacyPredicates );
    }
}