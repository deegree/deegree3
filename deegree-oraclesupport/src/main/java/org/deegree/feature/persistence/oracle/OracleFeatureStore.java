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

import static org.deegree.commons.tom.primitive.PrimitiveType.determinePrimitiveType;
import static org.deegree.commons.utils.JDBCUtils.close;
import static org.deegree.feature.types.property.ValueRepresentation.BOTH;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import oracle.jdbc.OracleConnection;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.apache.commons.dbcp.DelegatingConnection;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.ResultSetIterator;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.cs.CRS;
import org.deegree.feature.Feature;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.DefaultLockManager;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.mapping.FeatureTypeMapping;
import org.deegree.feature.persistence.oracle.jaxb.FeatureTypeDecl;
import org.deegree.feature.persistence.oracle.jaxb.OracleFeatureStoreConfig.MappingHints;
import org.deegree.feature.persistence.query.CombinedResultSet;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.IteratorResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.sql.UnmappableException;
import org.deegree.filter.sql.expression.SQLExpression;
import org.deegree.filter.sql.expression.SQLLiteral;
import org.deegree.filter.sql.oracle.OracleWhereBuilder;
import org.deegree.filter.sql.postgis.PostGISMapping;
import org.deegree.filter.sql.postgis.PropertyNameMapping;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.gml.GMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStore} implementation that uses an Oracle spatial database (TODO supported versions) as backend.
 * 
 * @see FeatureStore
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OracleFeatureStore implements FeatureStore {

    private static final Logger LOG = LoggerFactory.getLogger( OracleFeatureStore.class );

    private static final GeometryFactory geomFac = new GeometryFactory();

    private final String oracleSchema;

    private final GeometryTransformer geomTransformer;

    private final JGeometryAdapter jGeometryAdapter;

    private final CRS storageSRS;

    private final String connId;

    private ApplicationSchema schema;

    private final Map<QName, FeatureTypeMapping> ftToMapping = new HashMap<QName, FeatureTypeMapping>();

    private OracleFeatureStoreTransaction activeTransaction;

    private Connection taConn;

    private Thread transactionHolder;

    private PostGISMapping mapping = null;

    private LockManager lockManager;

    /**
     * @param schema
     * @param jdbcConnId
     * @param dbSchemaQualifier
     * @param storageSRS
     * @param mappingHints
     * @throws FeatureStoreException
     */
    OracleFeatureStore( ApplicationSchema schema, String jdbcConnId, String dbSchemaQualifier, CRS storageSRS,
                        MappingHints mappingHints, String oracleSchema ) throws FeatureStoreException {

        this.oracleSchema = oracleSchema;
        this.connId = jdbcConnId;
        this.storageSRS = storageSRS;
        if ( mappingHints != null ) {
            this.schema = buildSchema( jdbcConnId, mappingHints );
        } else {
            this.schema = schema;
        }
        // TODO make Oracle SRID configurable
        try {
            geomTransformer = new GeometryTransformer( storageSRS.getWrappedCRS() );
        } catch ( Exception e ) {
            throw new FeatureStoreException( e.getMessage(), e );
        }
        jGeometryAdapter = new JGeometryAdapter( storageSRS, -1 );

        mapping = new PostGISMapping() {

            @Override
            public byte[] getPostGISValue( Geometry literal, PropertyName propName )
                                    throws FilterEvaluationException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Object getPostGISValue( Literal<?> literal, PropertyName propName )
                                    throws FilterEvaluationException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public PropertyNameMapping getMapping( PropertyName propName )
                                    throws FilterEvaluationException, UnmappableException {
                LOG.debug( "Mapping " + propName.getPropertyName() + " to DB." );
                return new PropertyNameMapping( "X1", propName.getAsQName().getLocalPart() );
            }
        };

        // TODO
        lockManager = new DefaultLockManager( this, "LOCK_DB" );
    }

    private ApplicationSchema buildSchema( String jdbcConnId, MappingHints mappingHints )
                            throws FeatureStoreException {

        List<FeatureType> fts = new ArrayList<FeatureType>();
        try {
            Connection conn = ConnectionManager.getConnection( jdbcConnId );
            DatabaseMetaData md = conn.getMetaData();
            for ( FeatureTypeDecl ftDecl : mappingHints.getFeatureType() ) {
                LOG.info( "Creating mapping for feature type '" + ftDecl.getName() + "'." );
                fts.add( buildFeatureType( ftDecl, md ) );
                FeatureTypeMapping ftMapping = buildFeatureTypeMapping( ftDecl, md );
                ftToMapping.put( ftDecl.getName(), ftMapping );
            }
        } catch ( SQLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new ApplicationSchema( fts.toArray( new FeatureType[fts.size()] ), null );
    }

    private FeatureType buildFeatureType( FeatureTypeDecl ftDecl, DatabaseMetaData md )
                            throws SQLException, FeatureStoreException {

        String table = ftDecl.getTable();
        if ( table == null ) {
            table = ftDecl.getName().getLocalPart();
        }
        LOG.info( "table: " + table );

        QName ftName = ftDecl.getName();
        String prefix = ftName.getPrefix();
        String namespace = ftName.getNamespaceURI();

        String pkColumn = getPKColumn( md, table );

        ResultSet rs = md.getColumns( null, oracleSchema, table, null );
        List<PropertyType> propDecls = new ArrayList<PropertyType>();
        while ( rs.next() ) {
            String colName = rs.getString( 4 );
            if ( !colName.equalsIgnoreCase( pkColumn ) ) {
                QName ptName = new QName( namespace, colName, prefix );
                int sqlType = rs.getInt( 5 );
                PropertyType pt = buildPropertyType( ptName, sqlType );
                propDecls.add( pt );
            }
        }
        if ( propDecls.isEmpty() ) {
            String msg = "Configuration error: No columns for table/view '" + table + "' found (schema: '"
                         + oracleSchema + "').";
            throw new FeatureStoreException( msg );

        }
        return new GenericFeatureType( ftName, propDecls, false );
    }

    private FeatureTypeMapping buildFeatureTypeMapping( FeatureTypeDecl ftDecl, DatabaseMetaData md )
                            throws SQLException {

        String table = ftDecl.getTable();
        if ( table == null ) {
            table = ftDecl.getName().getLocalPart();
        }

        QName ftName = ftDecl.getName();
        String prefix = ftName.getPrefix();
        String namespace = ftName.getNamespaceURI();

        String pkColumn = getPKColumn( md, table );
        String[] gmlIdColumns = new String[] { pkColumn };

        ResultSet rs = md.getColumns( null, oracleSchema, table, null );

        Map<QName, String> propToColumn = new HashMap<QName, String>();

        while ( rs.next() ) {
            String colName = rs.getString( 4 );
            if ( !colName.equalsIgnoreCase( pkColumn ) ) {
                QName propName = new QName( namespace, colName, prefix );
                propToColumn.put( propName, colName );
            }
        }
        return new FeatureTypeMapping( ftName, table, gmlIdColumns, propToColumn );
    }

    private String getPKColumn( DatabaseMetaData md, String table )
                            throws SQLException {
        String pkColumn = null;
        ResultSet rs = md.getPrimaryKeys( null, oracleSchema, table );
        if ( !rs.next() ) {
            rs.close();
            // TODO
            return "gebiets_id";
        }
        pkColumn = rs.getString( 4 );
        if ( rs.next() ) {
            throw new SQLException( "More than one primary key column (composite primary key)." );
        }
        rs.close();
        return pkColumn;
    }

    private PropertyType buildPropertyType( QName ptName, int sqlType )
                            throws IllegalArgumentException {
        PropertyType pt = null;
        switch ( sqlType ) {
        case Types.OTHER: {
            pt = new GeometryPropertyType( ptName, 0, 1, GeometryType.GEOMETRY, CoordinateDimension.DIM_2, false, null,
                                           BOTH );
            break;
        }
        default: {
            PrimitiveType primType = determinePrimitiveType( sqlType );
            pt = new SimplePropertyType( ptName, 0, 1, primType, false, null );
            break;
        }
        }
        return pt;
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
            taConn = ConnectionManager.getConnection( connId );
            taConn.setAutoCommit( false );
            this.activeTransaction = new OracleFeatureStoreTransaction( this, taConn );
        } catch ( SQLException e ) {
            throw new FeatureStoreException( "Unable to acquire JDBC connection for transaction: " + e.getMessage(), e );
        }
        this.transactionHolder = Thread.currentThread();
        return this.activeTransaction;
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
    }

    @Override
    public Envelope getEnvelope( QName ftName ) {
        Envelope bbox = null;
        FeatureType ft = schema.getFeatureType( ftName );
        FeatureTypeMapping ftMapping = ftToMapping.get( ftName );
        if ( ftMapping != null ) {
            Connection conn = null;
            ResultSet rs = null;
            Statement stmt = null;
            try {
                conn = ConnectionManager.getConnection( connId );
                String table = ftMapping.getTable();
                GeometryPropertyType geomPt = ft.getDefaultGeometryPropertyDeclaration();
                if ( geomPt != null ) {
                    String geomColumn = ftMapping.getColumn( geomPt.getName() );
                    String sql = "SELECT SDO_AGGR_MBR(" + geomColumn + ") FROM " + table;
                    LOG.info( "Performing query: '" + sql + "'" );
                    stmt = conn.createStatement();
                    rs = stmt.executeQuery( sql );
                    if ( rs.next() ) {
                        STRUCT struct = (STRUCT) rs.getObject( 1 );
                        JGeometry jg = JGeometry.load( struct );
                        double[] mbr = jg.getMBR();
                        if ( mbr.length == 4 ) {
                            double[] min = new double[] { mbr[0], mbr[1] };
                            double[] max = new double[] { mbr[2], mbr[3] };
                            bbox = geomFac.createEnvelope( min, max, storageSRS );
                        } else if ( mbr.length == 6 ) {
                            double[] min = new double[] { mbr[0], mbr[1], mbr[2] };
                            double[] max = new double[] { mbr[3], mbr[4], mbr[5] };
                            bbox = geomFac.createEnvelope( min, max, storageSRS );
                        } else {
                            String msg = "Error while determining bbox for feature type '" + ftName
                                         + "': got an Oracle MBR with length " + mbr.length + ".";
                            LOG.error( msg );
                        }
                    }
                }
            } catch ( SQLException e ) {
                String msg = "SQLException occured while determining bbox for feature type '" + ftName + "': "
                             + e.getMessage();
                LOG.error( msg, e );
            } finally {
                JDBCUtils.close( rs, stmt, conn, LOG );
            }
        }
        return bbox;
    }

    @Override
    public LockManager getLockManager()
                            throws FeatureStoreException {
        return lockManager;
    }

    @Override
    public GMLObject getObjectById( String id )
                            throws FeatureStoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationSchema getSchema() {
        return schema;
    }

    @Override
    public CRS getStorageSRS() {
        return storageSRS;
    }

    @Override
    public void init()
                            throws FeatureStoreException {
        LOG.debug( "Initializing Oracle feature store." );
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public FeatureResultSet query( Query query )
                            throws FeatureStoreException, FilterEvaluationException {

        FeatureResultSet result = null;
        if ( query.getTypeNames().length == 0 ) {
            result = queryById( query );
        } else {
            result = queryByTypeNames( query );
        }
        return result;
    }

    private FeatureResultSet queryByTypeNames( Query query )
                            throws FeatureStoreException, FilterEvaluationException {

        if ( query.getTypeNames().length > 1 ) {
            String msg = "Join queries are currently not supported by the OracleFeatureStore.";
            throw new UnsupportedOperationException( msg );
        }
        QName ftName = query.getTypeNames()[0].getFeatureTypeName();
        FeatureType ft = schema.getFeatureType( ftName );
        if ( ft == null ) {
            String msg = "Feature type '" + ftName + "' is not served by this feature store.";
            throw new FeatureStoreException( msg );
        } else if ( ft.isAbstract() ) {
            String msg = "Requested feature type '" + ftName + "' is abstract and cannot be queried.";
            throw new FeatureStoreException( msg );
        }

        Filter filter = query.getFilter();
        if ( filter != null && !( filter instanceof OperatorFilter ) ) {
            throw new UnsupportedOperationException();
        }
        return queryByOperatorFilter( ft, (OperatorFilter) filter, query.getSortProperties() );
    }

    private FeatureResultSet queryByOperatorFilter( FeatureType ft, OperatorFilter filter, SortProperty[] sortCrits )
                            throws FeatureStoreException {

        FeatureTypeMapping ftMapping = ftToMapping.get( ft.getName() );
        FeatureResultSet result = null;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection( connId );
            OracleConnection oraConn = (OracleConnection) ( (DelegatingConnection) conn ).getInnermostDelegate();
            OracleWhereBuilder wb = new OracleWhereBuilder( mapping, filter, sortCrits, oraConn );
            SQLExpression where = wb.getWhereClause();
            SQLExpression orderBy = wb.getOrderBy();

            StringBuilder sql = new StringBuilder( "SELECT " );
            sql.append( ftMapping.getGMLIdColumns()[0] );
            for ( PropertyType pt : ft.getPropertyDeclarations() ) {
                sql.append( ',' );
                sql.append( ftMapping.getColumn( pt.getName() ) );
            }

            sql.append( " FROM " );
            sql.append( ftMapping.getTable() );
            sql.append( " X1" );

            if ( where != null ) {
                sql.append( " WHERE " );
                sql.append( where.getSQL() );
            }
            if ( orderBy != null ) {
                sql.append( " ORDER BY " );
                sql.append( orderBy.getSQL() );
            }

            LOG.info( "Preparing SELECT: " + sql );
            stmt = conn.prepareStatement( sql.toString() );

            int i = 1;
            if ( where != null ) {
                for ( SQLLiteral o : where.getLiterals() ) {
                    stmt.setObject( i++, o.getValue() );
                }
            }
            if ( orderBy != null ) {
                for ( SQLLiteral o : orderBy.getLiterals() ) {
                    stmt.setObject( i++, o.getValue() );
                }
            }

            rs = stmt.executeQuery();
            result = new IteratorResultSet( new FeatureResultSetIterator( rs, conn, stmt, ft, ftMapping ) );

        } catch ( Exception e ) {
            close( rs, stmt, conn, LOG );
            String msg = "Error performing query: " + e.getMessage();
            LOG.debug( msg, e );
            throw new FeatureStoreException( msg, e );
        }
        return result;
    }

    private FeatureResultSet queryById( Query query ) {

        IdFilter filter = (IdFilter) query.getFilter();
        throw new UnsupportedOperationException();
    }

    @Override
    public FeatureResultSet query( final Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        Iterator<FeatureResultSet> rsIter = new Iterator<FeatureResultSet>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < queries.length;
            }

            @Override
            public FeatureResultSet next() {
                if ( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                FeatureResultSet rs;
                try {
                    rs = query( queries[i++] );
                } catch ( Exception e ) {
                    throw new RuntimeException( e.getMessage(), e );
                }
                return rs;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return new CombinedResultSet( rsIter );
    }

    @Override
    public int queryHits( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        return query( query ).toCollection().size();
    }

    @Override
    public int queryHits( Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        return query( queries ).toCollection().size();
    }

    private Feature buildFeature( ResultSet rs, FeatureType ft, FeatureTypeMapping ftMapping )
                            throws SQLException {

        String fid = ft.getName().getLocalPart().toUpperCase() + "_" + rs.getString( 1 );
        List<Property> props = new ArrayList<Property>();
        int i = 2;
        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            if ( pt instanceof SimplePropertyType ) {
                String value = rs.getString( i );
                if ( value != null ) {
                    PrimitiveValue pv = new PrimitiveValue( value, ( (SimplePropertyType) pt ).getPrimitiveType() );
                    Property prop = new GenericProperty( pt, pv );
                    props.add( prop );
                }
            } else if ( pt instanceof GeometryPropertyType ) {
                STRUCT struct = (STRUCT) rs.getObject( i );
                if ( struct != null ) {
                    JGeometry jg = JGeometry.load( struct );
                    Geometry g = jGeometryAdapter.toGeometry( jg );
                    if ( g != null ) {
                        Property prop = new GenericProperty( pt, g );
                        props.add( prop );
                    }
                }
            } else {
                LOG.warn( "Skipping property '" + pt.getName() + "' -- type '" + pt.getClass()
                          + "' not handled in OracleFeatureStore." );
            }
            i++;
        }
        return ft.newFeature( fid, props, null );
    }

    FeatureType getFeatureType( QName ftName ) {
        return schema.getFeatureType( ftName );
    }

    FeatureTypeMapping getMapping( QName ftName ) {
        return ftToMapping.get( ftName );
    }

    JGeometryAdapter getJGeometryAdapter() {
        return jGeometryAdapter;
    }

    GeometryTransformer getGeometryTransformer() {
        return geomTransformer;
    }

    /**
     * Allows the {@link OracleFeatureStoreTransaction} to signal that it has been committed / rolled backed.
     * 
     * @param ta
     *            feature store transaction to be released (must be the active one)
     * @throws FeatureStoreException
     */
    void releaseTransaction( OracleFeatureStoreTransaction ta )
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
        try {
            taConn.close();
        } catch ( SQLException e ) {
            throw new FeatureStoreException( "Error closing connection after transaction: " + e.getMessage() );
        }
    }

    private class FeatureResultSetIterator extends ResultSetIterator<Feature> {

        private FeatureType ft;

        private FeatureTypeMapping ftMapping;

        public FeatureResultSetIterator( ResultSet rs, Connection conn, Statement stmt, FeatureType ft,
                                         FeatureTypeMapping ftMapping ) {
            super( rs, conn, stmt );
            this.ft = ft;
            this.ftMapping = ftMapping;
        }

        @SuppressWarnings("synthetic-access")
        @Override
        protected Feature createElement( ResultSet rs )
                                throws SQLException {
            return buildFeature( rs, ft, ftMapping );
        }
    }

    FeatureTypeMapping getMapping( String fid )
                            throws FeatureStoreException {
        int delimPos = fid.indexOf( '_' );
        if ( delimPos == -1 ) {
            String msg = "Cannot determine feature type for feature id '" + fid
                         + "' -- does not contain an underscore character.";
            throw new FeatureStoreException( msg );
        }
        String prefix = fid.substring( 0, delimPos  );
        for ( QName ftName : ftToMapping.keySet() ) {
            if ( ftName.getLocalPart().toUpperCase().equals( prefix ) ) {
                return ftToMapping.get( ftName );
            }
        }
        String msg = "No feature type for feature id '" + fid + "' found.";
        throw new FeatureStoreException( msg );
    }
}
