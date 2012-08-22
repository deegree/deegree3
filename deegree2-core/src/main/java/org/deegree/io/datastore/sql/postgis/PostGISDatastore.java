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

package org.deegree.io.datastore.sql.postgis;

import static java.lang.Integer.parseInt;
import static org.deegree.model.crs.CRSFactory.create;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
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
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.datastore.sql.VirtualContentProvider;
import org.deegree.io.datastore.sql.wherebuilder.WhereBuilder;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.postgis.PGgeometry;
import org.postgresql.PGConnection;

/**
 * {@link Datastore} implementation for PostGIS/PostgreSQL databases.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISDatastore extends AbstractSQLDatastore {

    private static final ILogger LOG = LoggerFactory.getLogger( PostGISDatastore.class );

    private static final String GEOMETRY_DATATYPE_NAME = "geometry";

    private static final String BOX3D_DATATYPE_NAME = "box3d";

    private static final String PG_GEOMETRY_CLASS_NAME = "org.postgis.PGgeometry";

    private static final String PG_BOX3D_CLASS_NAME = "org.postgis.PGbox3d";

    private static Class<?> pgGeometryClass;

    private static Class<?> pgBox3dClass;

    private static final String SRS_CODE_PROP_FILE = "srs_codes_postgis.properties";

    private static Map<String, Integer> nativeSrsCodeMap = new HashMap<String, Integer>();

    private static boolean useNativeSrsCodeMap = true;

    static {
        try {
            pgGeometryClass = Class.forName( PG_GEOMETRY_CLASS_NAME );
        } catch ( ClassNotFoundException e ) {
            LOG.logError( "Cannot find class '" + PG_GEOMETRY_CLASS_NAME + "'.", e );
        }
        try {
            pgBox3dClass = Class.forName( PG_BOX3D_CLASS_NAME );
        } catch ( ClassNotFoundException e ) {
            LOG.logError( "Cannot find class '" + PG_BOX3D_CLASS_NAME + "'.", e );
        }
        try {
            initSRSCodeMap();
        } catch ( IOException e ) {
            String msg = "Cannot load native srs code file '" + SRS_CODE_PROP_FILE + "'.";
            LOG.logError( msg, e );
        }
    }

    /**
     * Returns a specific {@link WhereBuilder} implementation for PostGIS.
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
     * @return <code>WhereBuilder</code> implementation for PostGIS
     * @throws DatastoreException
     */
    @Override
    public PostGISWhereBuilder getWhereBuilder( MappedFeatureType[] rootFts, String[] aliases, Filter filter,
                                                SortProperty[] sortProperties, TableAliasGenerator aliasGenerator,
                                                VirtualContentProvider vcProvider )
                            throws DatastoreException {
        return new PostGISWhereBuilder( rootFts, aliases, filter, sortProperties, aliasGenerator, vcProvider );
    }

    /**
     * Converts a PostGIS specific geometry <code>Object</code> from the <code>ResultSet</code> to a deegree
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
        if ( value != null && value instanceof PGgeometry ) {
            try {
                LOG.logDebug( "Converting PostGIS geometry to deegree geometry ('" + targetCS.getIdentifier() + "')" );
                geometry = PGgeometryAdapter.wrap( (PGgeometry) value, targetCS );
            } catch ( Exception e ) {
                throw new SQLException( "Error converting PostGIS geometry to deegree geometry: " + e.getMessage() );
            }
        }
        return geometry;
    }

    /**
     * Converts a deegree <code>Geometry</code> to a PostGIS specific geometry object.
     * 
     * @param geometry
     * @param targetSRS
     * @param conn
     * @return corresponding PostGIS specific geometry object
     * @throws DatastoreException
     */
    @Override
    public PGgeometry convertDeegreeToDBGeometry( Geometry geometry, int targetSRS, Connection conn )
                            throws DatastoreException {
        PGgeometry pgGeometry;
        try {
            pgGeometry = PGgeometryAdapter.export( geometry, targetSRS );
        } catch ( GeometryException e ) {
            throw new DatastoreException( "Error converting deegree geometry to PostGIS geometry: " + e.getMessage(), e );
        }
        return pgGeometry;
    }

    @Override
    protected Connection acquireConnection()
                            throws DatastoreException {
        JDBCConnection jdbcConnection = ( (SQLDatastoreConfiguration) this.getConfiguration() ).getJDBCConnection();
        Connection conn = null;
        try {
            conn = pool.acquireConnection( jdbcConnection.getDriver(), jdbcConnection.getURL(),
                                           jdbcConnection.getUser(), jdbcConnection.getPassword() );
            PGConnection pgConn = (PGConnection) conn;
            pgConn.addDataType( GEOMETRY_DATATYPE_NAME, pgGeometryClass );
            pgConn.addDataType( BOX3D_DATATYPE_NAME, pgBox3dClass );
        } catch ( Exception e ) {
            String msg = "Cannot acquire database connection: " + e.getMessage();
            LOG.logInfo( msg );
            throw new DatastoreException( msg, e );
        }
        return conn;
    }

    /**
     * Returns the next value of the given SQL sequence.
     * 
     * @param conn
     *            JDBC connection to be used
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
                rs = stmt.executeQuery( "SELECT NEXTVAL('" + sequence + "')" );
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
        if ( query.getSrsName() == null || canTransformTo( query.getSrsName() ) ) {
            return query;
        }
        return super.transformQuery( query );
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

        SQLFunctionCall transformCall = new SQLFunctionCall( "transform($1,$2)", field.getType(), param1, param2 );
        return transformCall;
    }

    @Override
    public String buildSRSTransformCall( String geomIdentifier, int nativeSRSCode )
                            throws DatastoreException {
        String call = "transform(" + geomIdentifier + "," + nativeSRSCode + ")";
        return call;
    }

    @Override
    public int getNativeSRSCode( String srsName ) {
        if ( !useNativeSrsCodeMap ) {
            try {
                return parseInt( create( srsName ).getCRS().getIdentifier().split( ":" )[1] );
            } catch ( NumberFormatException e ) {
                LOG.logError( "Error while checking for srid code", e );
            } catch ( UnknownCRSException e ) {
                LOG.logError( "Error while checking for srid code", e );
            }
        }
        Integer nativeSRSCode = nativeSrsCodeMap.get( srsName );
        if ( nativeSRSCode == null ) {
            return SRS_UNDEFINED;
        }
        return nativeSRSCode;
    }

    private static void initSRSCodeMap()
                            throws IOException {
        InputStream is = PostGISDatastore.class.getResourceAsStream( SRS_CODE_PROP_FILE );
        if ( is != null ) {
            Properties props = new Properties();
            props.load( is );
            for ( Object key : props.keySet() ) {
                String nativeCodeStr = props.getProperty( (String) key ).trim();
                try {
                    int nativeCode = Integer.parseInt( nativeCodeStr );
                    nativeSrsCodeMap.put( (String) key, nativeCode );
                } catch ( NumberFormatException e ) {
                    String msg = Messages.getMessage( "DATASTORE_SRS_CODE_INVALID", SRS_CODE_PROP_FILE, nativeCodeStr,
                                                      key );
                    throw new IOException( msg );
                }
            }
        } else {
            LOG.logInfo( "Not using '" + SRS_CODE_PROP_FILE
                         + "' for customizing PostGIS transformations. Not found on classpath." );
            useNativeSrsCodeMap = false;
        }
    }
}
