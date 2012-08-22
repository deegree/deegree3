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

package org.deegree.io.datastore.sql.oracle;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.CLOB;
import oracle.sql.STRUCT;
import oracle.sql.TIMESTAMP;

import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.FileUtils;
import org.deegree.framework.util.TimeTools;
import org.deegree.i18n.Messages;
import org.deegree.io.JDBCConnection;
import org.deegree.io.datastore.Datastore;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGeometryPropertyType;
import org.deegree.io.datastore.schema.TableRelation;
import org.deegree.io.datastore.schema.content.ConstantContent;
import org.deegree.io.datastore.schema.content.FieldContent;
import org.deegree.io.datastore.schema.content.FunctionParam;
import org.deegree.io.datastore.schema.content.MappingGeometryField;
import org.deegree.io.datastore.schema.content.SQLFunctionCall;
import org.deegree.io.datastore.sql.AbstractSQLDatastore;
import org.deegree.io.datastore.sql.SQLDatastoreConfiguration;
import org.deegree.io.datastore.sql.StatementBuffer;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.datastore.sql.VirtualContentProvider;
import org.deegree.io.datastore.sql.StatementBuffer.StatementArgument;
import org.deegree.io.datastore.sql.wherebuilder.WhereBuilder;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.wfs.operation.Query;

/**
 * {@link Datastore} implementation for Oracle Spatial database systems. Supports Oracle Spatial for Oracle 10g.
 * 
 * TODO Which Oracle spatial versions are supported exactly?
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OracleDatastore extends AbstractSQLDatastore {

    private static final ILogger LOG = LoggerFactory.getLogger( OracleDatastore.class );

    private static final String SRS_CODE_PROP_FILE = "srs_codes_oracle.properties";

    private static Map<String, Integer> nativeSrsCodeMap = new HashMap<String, Integer>();

    // used for identifying the last active connection (if changed, Reijer's JGeometry store workaround patch is
    // applied)
    private static Connection lastStoreConnection;

    static {
        try {
            initSRSCodeMap();
        } catch ( IOException e ) {
            String msg = "Cannot load native srs code file '" + SRS_CODE_PROP_FILE + "'.";
            LOG.logError( msg, e );
        }
    }

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
            Properties props = new Properties();
            props.put( "user", jdbcConnection.getUser() );
            props.put( "password", jdbcConnection.getPassword() );
            props.put( "SetBigStringTryClob", "true" );
            conn = pool.acquireConnection( jdbcConnection.getDriver(), jdbcConnection.getURL(), props );
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
            Properties props = new Properties();
            props.put( "user", jdbcConnection.getUser() );
            props.put( "password", jdbcConnection.getPassword() );
            props.put( "SetBigStringTryClob", "true" );
            pool.releaseConnection( conn, jdbcConnection.getDriver(), jdbcConnection.getURL(), props );
        } catch ( Exception e ) {
            String msg = "Cannot release database connection: " + e.getMessage();
            LOG.logError( msg, e );
            throw new DatastoreException( msg, e );
        }
    }

    /**
     * @param code
     *            an EPSG code
     * @return the oracle code as stored in srs_codes_oracle.properties
     */
    public static int getOracleSRIDCode( String code ) {
        Integer res = nativeSrsCodeMap.get( code );
        if ( res != null ) {
            return res.intValue();
        }

        // only in Oracle 10, but what else to do?
        return Integer.parseInt( code.split( ":" )[1] );
    }

    /**
     * @param srid
     * @return an EPSG code or "-1", if none was found
     */
    public static String fromOracleSRIDCode( int srid ) {
        for ( String k : nativeSrsCodeMap.keySet() ) {
            if ( nativeSrsCodeMap.get( k ).intValue() == srid ) {
                return k;
            }
        }

        return "-1";
    }

    /**
     * Returns a specific {@link WhereBuilder} implementation for Oracle Spatial.
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
     * @return <code>WhereBuilder</code> implementation for Oracle Spatial
     * @throws DatastoreException
     */
    @Override
    public WhereBuilder getWhereBuilder( MappedFeatureType[] rootFts, String[] aliases, Filter filter,
                                         SortProperty[] sortProperties, TableAliasGenerator aliasGenerator,
                                         VirtualContentProvider vcProvider )
                            throws DatastoreException {
        return new OracleSpatialWhereBuilder( rootFts, aliases, filter, sortProperties, aliasGenerator, vcProvider );
    }

    /**
     * Converts an Oracle specific geometry <code>Object</code> from the <code>ResultSet</code> to a deegree
     * <code>Geometry</code>.
     * 
     * @param value
     * @param targetCS
     * @param conn
     * @return corresponding deegree geometry
     * @throws SQLException
     */
    @Override
    public Geometry convertDBToDeegreeGeometry( Object value, CoordinateSystem targetCS, Connection conn )
                            throws SQLException {
        Geometry geometry = null;
        if ( value != null ) {
            LOG.logDebug( "Converting STRUCT to JGeometry." );
            JGeometry jGeometry = JGeometry.load( (STRUCT) value );
            try {
                LOG.logDebug( "Converting JGeometry to deegree geometry ('" + targetCS + "')" );
                geometry = JGeometryAdapter.wrap( jGeometry, targetCS );
            } catch ( Exception e ) {
                LOG.logError( "Error while converting STRUCT to Geometry: ", e );
                throw new SQLException( "Error converting STRUCT to Geometry: " + e.getMessage() );
            }
        }
        return geometry;
    }

    /**
     * Converts a deegree <code>Geometry</code> to an Oracle specific geometry object.
     * 
     * @param geometry
     * @param nativeSRSCode
     * @param conn
     * @return corresponding Oracle specific geometry object
     * @throws DatastoreException
     */
    @Override
    public STRUCT convertDeegreeToDBGeometry( Geometry geometry, int nativeSRSCode, Connection conn )
                            throws DatastoreException {

        JGeometry jGeometry = null;
        LOG.logDebug( "Converting deegree geometry to JGeometry." );
        try {
            jGeometry = JGeometryAdapter.export( geometry, nativeSRSCode );
        } catch ( GeometryException e ) {
            throw new DatastoreException( "Error converting deegree geometry to JGeometry: " + e.getMessage(), e );
        }

        LOG.logDebug( "Converting JGeometry to STRUCT." );
        STRUCT struct = null;
        try {
            struct = storeGeometryWithMultiConnHack( jGeometry, conn );
        } catch ( SQLException e ) {
            throw new DatastoreException( "Error converting JGeometry to STRUCT: " + e.getMessage(), e );
        }
        return struct;
    }

    /**
     * Workaround for a known Oracle JDBC driver problem.
     * <p>
     * JGeometry#store() isn't working when invoked successively using different connections. This method applies a
     * workaround (based on undocumented behaviour of the Oracle driver) to solve this problem.
     * http://forums.oracle.com/forums/thread.jspa?messageID=1273670
     * </p>
     * 
     * @param geometry
     *            geometry to be stored
     * @param connection
     *            jdbc connection
     * @return a {@link STRUCT} to be used as query parameter
     * @throws SQLException
     */
    private STRUCT storeGeometryWithMultiConnHack( JGeometry geometry, Connection connection )
                            throws SQLException {
        synchronized ( JGeometry.class ) {
            if ( lastStoreConnection != null && lastStoreConnection != connection ) {
                LOG.logDebug( "JGeometry#store(...) workaround (lastStoreConnection != connection)" );
                try {
                    Field geomDesc = JGeometry.class.getDeclaredField( "geomDesc" );
                    geomDesc.setAccessible( true );
                    geomDesc.set( null, null );
                } catch ( Exception e ) {
                    LOG.logWarning( "Exception caught applying JGeometr#store(...) workaround: " + e.getMessage(), e );
                }
            }
            lastStoreConnection = connection;
            return JGeometry.store( geometry, connection );
        }
    }

    /**
     * Converts the given object from a <code>java.sql.ResultSet</code> column to the common type to be used as a
     * feature property.
     * <p>
     * NOTE: String- and boolean-valued results have a special conversion handling:
     * <ul>
     * <li><code>Strings:</code> because we encountered difficulties when inserting empty strings "" into String-type
     * columns with NOT NULL constraints (for example in VARCHAR2 fields), "$EMPTY_STRING$" is used to mark them.</li>
     * <li><code>Boolean:<code>because Oracle has no special boolean type, it is assumed that a CHAR(1) column is used
     * instead (with values 'Y'=true and 'N'=false)</li>
     * </ul>
     * 
     * @param rsObject
     * @param sqlTypeCode
     * @return an object that is suitable for a table column of the specified SQL type
     * @throws DatastoreException
     */
    @Override
    public Object convertFromDBType( Object rsObject, int sqlTypeCode )
                            throws DatastoreException {
        Object propertyValue = rsObject;
        try {
            if ( rsObject instanceof TIMESTAMP ) {
                propertyValue = ( (TIMESTAMP) rsObject ).timestampValue();
            } else if ( rsObject instanceof String ) {
                if ( rsObject.equals( "$EMPTY_STRING$" ) ) {
                    propertyValue = "";
                }
                if ( sqlTypeCode == Types.BOOLEAN ) {
                    String val = rsObject.toString();

                    if ( val.length() == 1 && val.charAt( 0 ) == 'Y' ) {
                        propertyValue = Boolean.TRUE;
                    }
                    if ( val.length() == 1 && val.charAt( 0 ) == 'N' ) {
                        propertyValue = Boolean.FALSE;
                    }
                }
            } else if ( rsObject instanceof oracle.sql.CLOB ) {
                try {
                    propertyValue = FileUtils.readTextFile( ( (CLOB) rsObject ).getCharacterStream() ).toString();
                } catch ( IOException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch ( SQLException e ) {
            throw new DatastoreException( e.getMessage(), e );
        }
        return propertyValue;
    }

    /**
     * Returns the next value of the given SQL sequence.
     * 
     * @param conn
     *            JDBC connection to be used.
     * @param sequence
     *            name of the SQL sequence
     * @return next value of the given SQL sequence
     * @throws DatastoreException
     *             if the value could not be retrieved
     */
    @Override
    public Object getSequenceNextVal( Connection conn, String sequence )
                            throws DatastoreException {

        Object nextVal = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            try {
                stmt = conn.createStatement();
                rs = stmt.executeQuery( "SELECT " + sequence + ".nextval FROM dual" );
                if ( rs.next() ) {
                    nextVal = rs.getObject( 1 );
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
            String msg = "Could not retrieve value for sequence '" + sequence + "': " + e.getMessage();
            throw new DatastoreException( msg, e );
        }
        return nextVal;
    }

    /**
     * Returns the current value (plus an offset) of the given SQL sequence.
     * 
     * @param conn
     *            JDBC connection to be used.
     * @param sequence
     *            name of the SQL sequence
     * @param offset
     *            offset added to the sequence value
     * @return current value (plus offset) of the given SQL sequence
     * @throws DatastoreException
     *             if the value could not be retrieved
     */
    @Override
    public Object getSequenceCurrValPlusOffset( Connection conn, String sequence, int offset )
                            throws DatastoreException {

        Object nextVal = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            try {
                stmt = conn.createStatement();
                rs = stmt.executeQuery( "SELECT " + sequence + ".currval + " + offset + " FROM dual" );
                if ( rs.next() ) {
                    nextVal = rs.getObject( 1 );
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
            String msg = "Could not retrieve value for sequence '" + sequence + "': " + e.getMessage();
            throw new DatastoreException( msg, e );
        }
        return nextVal;
    }

    /**
     * Converts the {@link StatementBuffer} into a {@link PreparedStatement}, which is initialized and ready to be
     * performed.
     * 
     * TODO remove this method (use super class method instead), change handling of JGeometry NOTE: String- and
     * boolean-valued results have a special conversion handling:
     * <ul>
     * <li><code>Strings:</code> because we encountered difficulties when inserting empty strings "" into String-type
     * columns with NOT NULL constraints (for example in VARCHAR2 fields), "$EMPTY_STRING$" is used to mark them.</li>
     * <li><code>Boolean:<code>because Oracle has no special boolean type, it is assumed that a CHAR(1) column is used
     * instead (with values 'Y'=true and 'N'=false)</li>
     * </ul>
     * 
     * @param conn
     *            connection to be used to create the <code>PreparedStatement</code>
     * @param statementBuffer
     * @return the <code>PreparedStatment</code>, ready to be performed
     * @throws SQLException
     *             if a JDBC related error occurs
     */
    @Override
    public PreparedStatement prepareStatement( Connection conn, StatementBuffer statementBuffer )
                            throws SQLException {
        LOG.logDebug( "Preparing statement: " + statementBuffer.getQueryString() );

        PreparedStatement preparedStatement = conn.prepareStatement( statementBuffer.getQueryString() );

        Iterator<StatementArgument> it = statementBuffer.getArgumentsIterator();
        int i = 1;
        while ( it.hasNext() ) {
            StatementArgument argument = it.next();
            Object parameter = argument.getArgument();
            int targetSqlType = argument.getTypeCode();
            if ( parameter != null ) {
                if ( targetSqlType == Types.DATE ) {
                    if ( parameter instanceof String ) {
                        parameter = TimeTools.createCalendar( (String) parameter ).getTime();
                    }
                    parameter = new java.sql.Date( ( (Date) parameter ).getTime() );
                } else if ( targetSqlType == Types.TIMESTAMP ) {
                    if ( parameter instanceof String ) {
                        parameter = TimeTools.createCalendar( (String) parameter ).getTime();
                    }
                    parameter = new java.sql.Timestamp( ( (Date) parameter ).getTime() );
                } else if ( parameter != null && parameter instanceof JGeometry ) {
                    parameter = storeGeometryWithMultiConnHack( (JGeometry) parameter, conn );
                } else if ( targetSqlType == Types.INTEGER || targetSqlType == Types.SMALLINT
                            || targetSqlType == Types.TINYINT ) {
                    parameter = Integer.parseInt( parameter.toString() );
                } else if ( targetSqlType == Types.DECIMAL || targetSqlType == Types.DOUBLE
                            || targetSqlType == Types.REAL || targetSqlType == Types.FLOAT ) {
                    parameter = Double.parseDouble( parameter.toString() );
                } else if ( targetSqlType == Types.NUMERIC ) {
                    try {
                        parameter = Integer.parseInt( parameter.toString() );
                    } catch ( Exception e ) {
                        parameter = Double.parseDouble( parameter.toString() );
                    }
                } else if ( targetSqlType == Types.BOOLEAN ) {
                    // Oracle does not have a BOOLEAN datatype
                    // default maping to column of type CHAR(1)
                    // http://thinkoracle.blogspot.com/2005/07/oracle-boolean.html
                    targetSqlType = Types.CHAR;
                    if ( Boolean.parseBoolean( parameter.toString() ) ) {
                        parameter = "Y";
                    } else {
                        parameter = "N";
                    }
                } else if ( parameter instanceof String ) {
                    // Using the empty string ("") for NOT NULL columns fails
                    // (at least using PreparedStatements)
                    // TODO implement a proper solution
                    if ( ( (String) parameter ).length() == 0 ) {
                        parameter = "$EMPTY_STRING$";
                    }
                }
                if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                    try {
                        String typeName = Types.getTypeNameForSQLTypeCode( targetSqlType );
                        LOG.logDebug( "Setting argument " + i + ": type=" + typeName + ", value class="
                                      + parameter.getClass() );
                        if ( parameter instanceof String || parameter instanceof Number
                             || parameter instanceof java.sql.Date ) {
                            LOG.logDebug( "Value: '" + parameter + "'" );
                        }
                    } catch ( UnknownTypeException e ) {
                        throw new SQLException( e.getMessage() );
                    }
                }
                preparedStatement.setObject( i, parameter, targetSqlType );
            } else {
                setNullValue( preparedStatement, i, targetSqlType );
            }
            i++;
        }
        return preparedStatement;
    }

    /**
     * Transforms the incoming {@link Query} so that the {@link CoordinateSystem} of all spatial arguments (BBOX, etc.)
     * in the {@link Filter} match the SRS of the targeted {@link MappingGeometryField}s.
     * <p>
     * NOTE: If this transformation can be performed by the backend (e.g. by Oracle Spatial), this method should be
     * overwritten to return the original input {@link Query}.
     * 
     * @param query
     *            query to be transformed
     * @return query with spatial arguments transformed to target SRS
     */
    @Override
    protected Query transformQuery( Query query ) {
        return query;
    }

    /**
     * Transforms the {@link FeatureCollection} so that the geometries of all contained geometry properties use the
     * requested SRS.
     * 
     * @param fc
     *            feature collection to be transformed
     * @param targetSRS
     *            requested SRS
     * @return transformed FeatureCollection
     */
    @Override
    protected FeatureCollection transformResult( FeatureCollection fc, String targetSRS ) {
        return fc;
    }

    /**
     * Returns whether the datastore is capable of performing a native coordinate transformation (using an SQL function
     * call for example) into the given SRS.
     * 
     * @param targetSRS
     *            target spatial reference system (usually "EPSG:XYZ")
     * @return true, if the datastore can perform the coordinate transformation, false otherwise
     */
    @Override
    protected boolean canTransformTo( String targetSRS ) {
        return getNativeSRSCode( targetSRS ) != SRS_UNDEFINED;
    }

    /**
     * Returns an {@link SQLFunctionCall} that refers to the given {@link MappingGeometryField} in the specified target
     * SRS using a database specific SQL function.
     * 
     * @param geoProperty
     *            geometry property
     * @param targetSRS
     *            target spatial reference system (usually "EPSG:XYZ")
     * @return an {@link SQLFunctionCall} that refers to the geometry in the specified srs
     * @throws DatastoreException
     */
    @Override
    public SQLFunctionCall buildSRSTransformCall( MappedGeometryPropertyType geoProperty, String targetSRS )
                            throws DatastoreException {

        int nativeSRSCode = getNativeSRSCode( targetSRS );
        if ( nativeSRSCode == SRS_UNDEFINED ) {
            String msg = Messages.getMessage( "DATASTORE_SQL_NATIVE_CT_UNKNOWN_SRS", this.getClass().getName(),
                                              targetSRS );
            throw new DatastoreException( msg );
        }

        MappingGeometryField field = geoProperty.getMappingField();
        FunctionParam param1 = new FieldContent( field, new TableRelation[0] );
        FunctionParam param2 = new ConstantContent( "" + nativeSRSCode );

        SQLFunctionCall transformCall = new SQLFunctionCall( "SDO_CS.TRANSFORM($1,$2)", field.getType(), param1, param2 );
        return transformCall;
    }

    @Override
    public String buildSRSTransformCall( String geomIdentifier, int nativeSRSCode )
                            throws DatastoreException {
        String call = "SDO_CS.TRANSFORM(" + geomIdentifier + "," + nativeSRSCode + ")";
        return call;
    }

    @Override
    public int getNativeSRSCode( String srsName ) {
        Integer nativeSRSCode = nativeSrsCodeMap.get( srsName );
        if ( nativeSRSCode == null ) {
            return SRS_UNDEFINED;
        }
        return nativeSRSCode;
    }

    private void setNullValue( PreparedStatement preparedStatement, int i, int targetSqlType )
                            throws SQLException {
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            try {
                String typeName = Types.getTypeNameForSQLTypeCode( targetSqlType );
                LOG.logDebug( "Setting argument " + i + ": type=" + typeName );
                LOG.logDebug( "Value: null" );
            } catch ( UnknownTypeException e ) {
                throw new SQLException( e.getMessage() );
            }
        }
        preparedStatement.setNull( i, targetSqlType );
    }

    private static void initSRSCodeMap()
                            throws IOException {
        InputStream is = OracleDatastore.class.getResourceAsStream( SRS_CODE_PROP_FILE );
        Properties props = new Properties();
        props.load( is );
        for ( Object key : props.keySet() ) {
            String nativeCodeStr = props.getProperty( (String) key ).trim();
            try {
                int nativeCode = Integer.parseInt( nativeCodeStr );
                nativeSrsCodeMap.put( (String) key, nativeCode );
            } catch ( NumberFormatException e ) {
                String msg = Messages.getMessage( "DATASTORE_SRS_CODE_INVALID", SRS_CODE_PROP_FILE, nativeCodeStr, key );
                throw new IOException( msg );
            }
        }
    }
}
