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
package org.deegree.io.datastore.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.TimeTools;
import org.deegree.i18n.Messages;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.JDBCConnection;
import org.deegree.io.datastore.AnnotationDocument;
import org.deegree.io.datastore.Datastore;
import org.deegree.io.datastore.DatastoreConfiguration;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.DatastoreTransaction;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.TransactionException;
import org.deegree.io.datastore.idgenerator.IdGenerationException;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGeometryPropertyType;
import org.deegree.io.datastore.schema.content.SQLFunctionCall;
import org.deegree.io.datastore.sql.StatementBuffer.StatementArgument;
import org.deegree.io.datastore.sql.transaction.SQLTransaction;
import org.deegree.io.datastore.sql.wherebuilder.WhereBuilder;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.GMLFeatureDocument;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.wfs.operation.Lock;
import org.deegree.ogcwebservices.wfs.operation.Query;

/**
 * This abstract class implements the common functionality of {@link Datastore} implementations that use SQL databases
 * as backend.
 * 
 * @see QueryHandler
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractSQLDatastore extends Datastore {

    private static final ILogger LOG = LoggerFactory.getLogger( AbstractSQLDatastore.class );

    /** Database specific SRS code for an unspecified SRS. */
    protected static final int SRS_UNDEFINED = -1;

    /** Pool of database connections. */
    protected DBConnectionPool pool;

    private DatastoreTransaction activeTransaction;

    private Thread transactionHolder;

    @Override
    public AnnotationDocument getAnnotationParser() {
        return new SQLAnnotationDocument( this.getClass() );
    }

    @Override
    public void configure( DatastoreConfiguration datastoreConfiguration )
                            throws DatastoreException {
        super.configure( datastoreConfiguration );
        this.pool = DBConnectionPool.getInstance();
    }

    @Override
    public void close()
                            throws DatastoreException {
        LOG.logInfo( "close() does not do nothing for AbstractSQLDatastore because no resources must be released" );
    }

    /**
     * Overwrite this to return a database specific (spatial capable) {@link WhereBuilder} implementation.
     * 
     * @param rootFts
     *            involved (requested) feature types
     * @param aliases
     *            aliases for the feature types, may be null
     * @param filter
     *            filter that restricts the matched features
     * @param sortProperties
     *            sort criteria for the result, may be null or empty
     * @param aliasGenerator
     *            used to generate unique table aliases
     * @param vcProvider
     * @return <code>WhereBuilder</code> implementation suitable for this datastore
     * @throws DatastoreException
     */
    public WhereBuilder getWhereBuilder( MappedFeatureType[] rootFts, String[] aliases, Filter filter,
                                         SortProperty[] sortProperties, TableAliasGenerator aliasGenerator,
                                         VirtualContentProvider vcProvider )
                            throws DatastoreException {
        return new WhereBuilder( rootFts, aliases, filter, sortProperties, aliasGenerator, vcProvider );
    }

    @Override
    public FeatureCollection performQuery( Query query, MappedFeatureType[] rootFts )
                            throws DatastoreException, UnknownCRSException {
        Connection conn = acquireConnection();
        FeatureCollection result;
        try {
            result = performQuery( query, rootFts, conn );
        } finally {
            releaseConnection( conn );
        }
        return result;
    }

    @Override
    public FeatureCollection performQuery( Query query, MappedFeatureType[] rootFts, DatastoreTransaction context )
                            throws DatastoreException, UnknownCRSException {
        return performQuery( query, rootFts, ( (SQLTransaction) context ).getConnection() );
    }

    /**
     * Performs a {@link Query} against the datastore.
     * <p>
     * Note that this method is responsible for the coordinate system tranformation of the input {@link Query} and the
     * output {@link FeatureCollection}.
     * 
     * @param query
     *            query to be performed
     * @param rootFts
     *            the root feature types that are queried, more than one type means that the types are joined
     * @param conn
     *            JDBC connection to use
     * @return requested feature instances
     * @throws DatastoreException
     * @throws UnknownCRSException
     */
    protected FeatureCollection performQuery( Query query, MappedFeatureType[] rootFts, Connection conn )
                            throws DatastoreException, UnknownCRSException {

        Query transformedQuery = transformQuery( query );

        FeatureCollection result = null;
        try {
            QueryHandler queryHandler = new QueryHandler( this, new TableAliasGenerator(), conn, rootFts, query );
            result = queryHandler.performQuery();
        } catch ( SQLException e ) {
            String msg = "SQL error while performing query: " + e.getMessage();
            LOG.logError( msg, e );
            throw new DatastoreException( msg, e );
        }

        // transform result to queried srs (only if necessary)
        String targetSrs = transformedQuery.getSrsName();
        if ( targetSrs != null && !this.canTransformTo( targetSrs ) ) {
            result = transformResult( result, targetSrs );
        }
        return result;
    }

    /**
     * Acquires transactional access to the datastore. There's only one active transaction per datastore instance
     * allowed at a time.
     * 
     * @return transaction object that allows to perform transaction operations on the datastore
     * @throws DatastoreException
     */
    @Override
    public DatastoreTransaction acquireTransaction()
                            throws DatastoreException {

        while ( this.activeTransaction != null ) {
            Thread holder = this.transactionHolder;
            // check if transaction holder variable has (just) been cleared or if the other thread
            // has been killed (avoid deadlocks)
            if ( holder == null || !holder.isAlive() ) {
                this.activeTransaction = null;
                this.transactionHolder = null;
                break;
            }
        }

        this.activeTransaction = createTransaction();
        this.transactionHolder = Thread.currentThread();
        return this.activeTransaction;
    }

    /**
     * Creates a new {@link SQLTransaction} that provides transactional access.
     * 
     * @return new {@link SQLTransaction} instance
     * @throws DatastoreException
     */
    protected SQLTransaction createTransaction()
                            throws DatastoreException {
        return new SQLTransaction( this, new TableAliasGenerator(), acquireConnection() );
    }

    /**
     * Returns the transaction to the datastore. This makes the transaction available to other clients again (via
     * <code>acquireTransaction</code>). Underlying resources (such as JDBCConnections are freed).
     * <p>
     * The transaction should be terminated, i.e. commit() or rollback() must have been called before.
     * 
     * @param ta
     *            the DatastoreTransaction to be returned
     * @throws DatastoreException
     */
    @Override
    public void releaseTransaction( DatastoreTransaction ta )
                            throws DatastoreException {
        if ( ta.getDatastore() != this ) {
            String msg = Messages.getMessage( "DATASTORE_TA_NOT_OWNER" );
            throw new TransactionException( msg );
        }
        if ( ta != this.activeTransaction ) {
            String msg = Messages.getMessage( "DATASTORE_TA_NOT_ACTIVE" );
            throw new TransactionException( msg );
        }
        releaseConnection( ( (SQLTransaction) ta ).getConnection() );

        this.activeTransaction = null;
        this.transactionHolder = null;
    }

    @Override
    public Set<FeatureId> determineFidsToLock( List<Lock> requestParts )
                            throws DatastoreException {

        Set<FeatureId> lockedFids = null;
        Connection conn = acquireConnection();
        LockHandler handler = new LockHandler( this, new TableAliasGenerator(), conn, requestParts );
        try {
            lockedFids = handler.determineFidsToLock();
        } finally {
            releaseConnection( conn );
        }
        return lockedFids;
    }

    /**
     * Converts a database specific geometry <code>Object</code> from the <code>ResultSet</code> to a deegree
     * <code>Geometry</code>.
     * 
     * @param value
     * @param targetSRS
     * @param conn
     * @return corresponding deegree geometry
     * @throws SQLException
     */
    public abstract Geometry convertDBToDeegreeGeometry( Object value, CoordinateSystem targetSRS, Connection conn )
                            throws SQLException;

    /**
     * Converts a deegree <code>Geometry</code> to a database specific geometry <code>Object</code>.
     * 
     * @param geometry
     * @param nativeSRSCode
     * @param conn
     * @return corresponding database specific geometry object
     * @throws DatastoreException
     */
    public abstract Object convertDeegreeToDBGeometry( Geometry geometry, int nativeSRSCode, Connection conn )
                            throws DatastoreException;

    /**
     * Returns the database connection requested for.
     * 
     * @return Connection
     * @throws DatastoreException
     */
    protected Connection acquireConnection()
                            throws DatastoreException {
        JDBCConnection jdbcConnection = ( (SQLDatastoreConfiguration) this.getConfiguration() ).getJDBCConnection();
        Connection conn = null;
        try {
            conn = pool.acquireConnection( jdbcConnection.getDriver(), jdbcConnection.getURL(),
                                           jdbcConnection.getUser(), jdbcConnection.getPassword() );
        } catch ( Exception e ) {
            String msg = "Cannot acquire database connection: " + e.getMessage();
            LOG.logError( msg, e );
            throw new DatastoreException( msg, e );
        }
        return conn;
    }

    /**
     * Releases the connection.
     * 
     * @param conn
     *            connection to be released.
     * @throws DatastoreException
     */
    public void releaseConnection( Connection conn )
                            throws DatastoreException {
        LOG.logDebug( "Releasing JDBCConnection." );
        JDBCConnection jdbcConnection = ( (SQLDatastoreConfiguration) this.getConfiguration() ).getJDBCConnection();
        try {
            pool.releaseConnection( conn, jdbcConnection.getDriver(), jdbcConnection.getURL(),
                                    jdbcConnection.getUser(), jdbcConnection.getPassword() );
        } catch ( Exception e ) {
            String msg = "Cannot release database connection: " + e.getMessage();
            LOG.logError( msg, e );
            throw new DatastoreException( msg, e );
        }
    }

    /**
     * Converts the <code>StatementBuffer</code> into a <code>PreparedStatement</code>, which is initialized and ready
     * to be performed.
     * 
     * @param conn
     *            connection to be used to create the <code>PreparedStatement</code>
     * @param statementBuffer
     * @return the <code>PreparedStatment</code>, ready to be performed
     * @throws SQLException
     *             if a JDBC related error occurs
     * @throws DatastoreException
     */
    public PreparedStatement prepareStatement( Connection conn, StatementBuffer statementBuffer )
                            throws SQLException, DatastoreException {

        LOG.logDebug( "Preparing statement: " + statementBuffer.getQueryString() );
        PreparedStatement preparedStatement = conn.prepareStatement( statementBuffer.getQueryString() );

        Iterator<StatementArgument> argumentIter = statementBuffer.getArgumentsIterator();
        int i = 1;
        while ( argumentIter.hasNext() ) {
            StatementArgument argument = argumentIter.next();
            int targetSqlType = argument.getTypeCode();
            Object obj = argument.getArgument();
            Object sqlObject = obj != null ? convertToDBType( obj, targetSqlType ) : null;

            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                try {
                    String typeName = Types.getTypeNameForSQLTypeCode( targetSqlType );
                    LOG.logDebug( "Setting argument " + i + ": type=" + typeName + ", value class="
                                  + ( sqlObject == null ? "none (null object)" : sqlObject.getClass() ) );
                    if ( sqlObject instanceof String ) {
                        String s = (String) sqlObject;
                        if ( s.length() <= 100 ) {
                            LOG.logDebug( "Value: '" + s + "'" );
                        } else {
                            LOG.logDebug( "Value: '" + s.substring( 0, 100 ) + "...'" );
                        }
                    }
                    if ( sqlObject instanceof Number ) {
                        LOG.logDebug( "Value: '" + sqlObject + "'" );
                    }
                } catch ( UnknownTypeException e ) {
                    LOG.logError( e.getMessage(), e );
                    throw new SQLException( e.getMessage() );
                }
            }
            if ( sqlObject == null ) {
                preparedStatement.setNull( i, targetSqlType );
            } else {
                preparedStatement.setObject( i, sqlObject, targetSqlType );
            }
            i++;
        }
        return preparedStatement;
    }

    /**
     * Converts the given object into an object that is suitable for a table column of the specified SQL type.
     * <p>
     * The return value is used in a java.sql.PreparedStatement#setObject() call.
     * <p>
     * Please note that this implementation is subject to change. There are missing type cases, and it is preferable to
     * use the original string representation of the input object (except for geometries).
     * <p>
     * NOTE: Almost identical functionality exists in {@link GMLFeatureDocument}. This is subject to change.
     * 
     * @see java.sql.PreparedStatement#setObject(int, Object, int)
     * @param o
     * @param sqlTypeCode
     * @return an object that is suitable for a table column of the specified SQL type
     * @throws DatastoreException
     */
    private Object convertToDBType( Object o, int sqlTypeCode )
                            throws DatastoreException {

        Object sqlType = null;

        switch ( sqlTypeCode ) {
        case Types.VARCHAR: {
            sqlType = o.toString();
            break;
        }
        case Types.INTEGER:
        case Types.SMALLINT: {
            try {
                sqlType = new Integer( o.toString().trim() );
            } catch ( NumberFormatException e ) {
                throw new DatastoreException( "'" + o + "' does not denote a valid Integer value." );
            }
            break;
        }
        case Types.NUMERIC:
        case Types.REAL:
        case Types.DOUBLE: {
            try {
                sqlType = new Double( o.toString() );
            } catch ( NumberFormatException e ) {
                throw new DatastoreException( "'" + o + "' does not denote a valid Double value." );
            }
            break;
        }
        case Types.DECIMAL:
        case Types.FLOAT: {
            try {
                sqlType = new Float( o.toString() );
            } catch ( NumberFormatException e ) {
                throw new DatastoreException( "'" + o + "' does not denote a valid Double value." );
            }
            break;
        }
        case Types.BOOLEAN: {
            sqlType = new Boolean( o.toString() );
            break;
        }
        case Types.DATE: {
            if ( o instanceof Date ) {
                sqlType = new java.sql.Date( ( (Date) o ).getTime() );
            } else {
                String s = o.toString();
                int idx = s.indexOf( " " ); // Datestring like "2005-04-21 00:00:00"
                if ( -1 != idx )
                    s = s.substring( 0, idx );
                sqlType = new java.sql.Date( TimeTools.createCalendar( s ).getTimeInMillis() );
            }
            break;
        }
        case Types.TIME: {
            if ( o instanceof Date ) {
                sqlType = new java.sql.Time( ( (Date) o ).getTime() );
            } else {
                sqlType = new java.sql.Time( TimeTools.createCalendar( o.toString() ).getTimeInMillis() );
            }
            break;
        }
        case Types.TIMESTAMP: {
            if ( o instanceof Date ) {
                sqlType = new Timestamp( ( (Date) o ).getTime() );
            } else {
                sqlType = new java.sql.Timestamp( TimeTools.createCalendar( o.toString() ).getTimeInMillis() );
            }
            break;
        }
        default: {
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                String sqlTypeName = "" + sqlTypeCode;
                try {
                    sqlTypeName = Types.getTypeNameForSQLTypeCode( sqlTypeCode );
                } catch ( UnknownTypeException e ) {
                    LOG.logError( e.getMessage(), e );
                }
                LOG.logDebug( "No type conversion for sql type '" + sqlTypeName
                              + "' defined. Passing argument of type '" + o.getClass().getName() + "'." );
            }
            sqlType = o;
        }
        }
        return sqlType;
    }

    /**
     * Converts the given object from a <code>java.sql.ResultSet</code> column to the common type to be used as a
     * feature property.
     * 
     * @param rsObject
     * @param sqlTypeCode
     * @return an object that is suitable for a table column of the specified SQL type
     * @throws DatastoreException
     */
    @SuppressWarnings("unused")
    public Object convertFromDBType( Object rsObject, int sqlTypeCode )
                            throws DatastoreException {
        return rsObject;
    }

    /**
     * Overwrite this to enable the datastore to fetch the next value of a SQL sequence.
     * 
     * @param conn
     *            JDBC connection to be used.
     * @param sequence
     *            name of the SQL sequence.
     * @return next value of the given SQL sequence
     * @throws DatastoreException
     *             if the value could not be retrieved
     */
    public Object getSequenceNextVal( Connection conn, String sequence )
                            throws DatastoreException {
        String msg = Messages.getMessage( "DATASTORE_SEQ_NOT_SUPPORTED", this.getClass().getName() );
        throw new DatastoreException( msg );
    }

    /**
     * Overwrite this to enable the datastore to fetch the current value (plus an offset) of a SQL sequence.
     * 
     * @param conn
     *            JDBC connection to be used.
     * @param sequence
     *            name of the SQL sequence
     * @param offset
     *            offset added to the sequence value
     * @return next value of the given SQL sequence
     * @throws DatastoreException
     *             if the value could not be retrieved
     */
    public Object getSequenceCurrValPlusOffset( Connection conn, String sequence, int offset )
                            throws DatastoreException {
        String msg = Messages.getMessage( "DATASTORE_SEQ_NOT_SUPPORTED", this.getClass().getName() );
        throw new DatastoreException( msg );
    }

    /**
     * Returns the maximum (integer) value stored in a certain table column.
     * 
     * @param conn
     *            JDBC connection to be used
     * @param tableName
     *            name of the table
     * @param columnName
     *            name of the column
     * @return the maximum value
     * @throws IdGenerationException
     *             if the value could not be retrieved
     */
    public int getMaxValue( Connection conn, String tableName, String columnName )
                            throws IdGenerationException {

        int max = 0;
        Statement stmt = null;
        ResultSet rs = null;

        LOG.logDebug( "Retrieving max value in " + tableName + "." + columnName + "..." );

        try {
            try {
                stmt = conn.createStatement();
                rs = stmt.executeQuery( "SELECT MAX(" + columnName + ") FROM " + tableName );
                if ( rs.next() ) {
                    Object columnMax = rs.getObject( 1 );
                    if ( columnMax != null ) {
                        if ( columnMax instanceof Integer ) {
                            max = ( (Integer) columnMax ).intValue();
                        } else {
                            max = Integer.parseInt( columnMax.toString() );
                        }
                    }
                }
            } finally {
                try {
                    if ( rs != null ) {
                        rs.close();
                    }
                } finally {
                    if ( stmt != null ) {
                        stmt.close();
                    }
                }
            }
        } catch ( SQLException e ) {
            String msg = "Could not retrieve max value for table column '" + tableName + "." + columnName + "': "
                         + e.getMessage();
            LOG.logError( msg, e );
            throw new IdGenerationException( msg, e );
        } catch ( NumberFormatException e ) {
            String msg = "Could not convert selected value to integer: " + e.getMessage();
            LOG.logError( msg, e );
            throw new IdGenerationException( msg, e );
        }
        LOG.logDebug( "max value: " + max );
        return max;
    }

    /**
     * Returns an {@link SQLFunctionCall} that refers to the given {@link MappedGeometryPropertyType} in the specified
     * target SRS using a database specific SQL function.
     * 
     * @param geoProperty
     *            geometry property
     * @param targetSRS
     *            target spatial reference system (usually "EPSG:XYZ")
     * @return an {@link SQLFunctionCall} that refers to the geometry in the specified srs
     * @throws DatastoreException
     */
    public SQLFunctionCall buildSRSTransformCall( @SuppressWarnings("unused") MappedGeometryPropertyType geoProperty,
                                                  @SuppressWarnings("unused") String targetSRS )
                            throws DatastoreException {
        String msg = Messages.getMessage( "DATASTORE_SQL_NATIVE_CT_UNSUPPORTED", this.getClass().getName() );
        throw new DatastoreException( msg );
    }

    /**
     * Builds an SQL fragment that converts the given geometry to the specified SRS.
     * 
     * @param geomIdentifier
     * @param nativeSRSCode
     * @return an SQL fragment that converts the given geometry to the specified SRS
     * @throws DatastoreException
     */
    public String buildSRSTransformCall( @SuppressWarnings("unused") String geomIdentifier,
                                         @SuppressWarnings("unused") int nativeSRSCode )
                            throws DatastoreException {
        String msg = Messages.getMessage( "DATASTORE_SQL_NATIVE_CT_UNSUPPORTED", this.getClass().getName() );
        throw new DatastoreException( msg );
    }

    /**
     * Returns the database specific code for the given SRS name.
     * 
     * @param srsName
     *            spatial reference system name (usually "EPSG:XYZ")
     * @return the database specific SRS code, or -1 if no corresponding native code is known
     * @throws DatastoreException
     */
    public int getNativeSRSCode( @SuppressWarnings("unused") String srsName )
                            throws DatastoreException {
        String msg = Messages.getMessage( "DATASTORE_SQL_NATIVE_CT_UNSUPPORTED", this.getClass().getName() );
        throw new DatastoreException( msg );
    }

    /**
     * Checks whether the (native) coordinate transformation of the specified geometry property to the given SRS is
     * possible (and necessary), i.e.
     * <ul>
     * <li>the internal srs of the property is specified (and not -1)
     * <li>or the requested SRS is null or equal to the property's srs
     * </ul>
     * If this is not the case, a {@link DatastoreException} is thrown to indicate the problem.
     * 
     * @param pt
     * @param queriedSrs
     * @return the srs to transform to, or null, if transformation is unnecessary
     * @throws DatastoreException
     */
    String checkTransformation( MappedGeometryPropertyType pt, String queriedSrs )
                            throws DatastoreException {

        String targetSrs = null;
        int internalSrs = pt.getMappingField().getSRS();
        String propertySrs = pt.getCS().getIdentifier();

        if ( queriedSrs != null && !propertySrs.equals( queriedSrs ) ) {
            if ( internalSrs == SRS_UNDEFINED ) {
                String msg = Messages.getMessage( "DATASTORE_SRS_NOT_SPECIFIED", pt.getName(), queriedSrs, propertySrs );
                throw new DatastoreException( msg );
            }
            targetSrs = queriedSrs;
        }
        return targetSrs;
    }

    public void appendGeometryColumnGet( StatementBuffer query, String tableAlias, String column ) {
        query.append( tableAlias );
        query.append( '.' );
        query.append( column );
    }
}
