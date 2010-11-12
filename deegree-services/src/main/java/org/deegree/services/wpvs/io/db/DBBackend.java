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

package org.deegree.services.wpvs.io.db;

import static org.deegree.services.wpvs.io.ModelBackend.Type.TREE;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.index.PositionableModel;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.BillBoard;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.DirectGeometryBuffer;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModel;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.BuildingRenderer;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.RenderableManager;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.TreeRenderer;
import org.deegree.rendering.r3d.opengl.rendering.model.prototype.RenderablePrototype;
import org.deegree.services.wpvs.io.BackendResult;
import org.deegree.services.wpvs.io.DataObjectInfo;
import org.deegree.services.wpvs.io.ModelBackend;
import org.deegree.services.wpvs.io.ModelBackendInfo;
import org.deegree.services.wpvs.io.serializer.BillBoardSerializer;
import org.deegree.services.wpvs.io.serializer.ObjectSerializer;
import org.deegree.services.wpvs.io.serializer.PrototypeSerializer;
import org.deegree.services.wpvs.io.serializer.WROSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>DBBackend</code> provides methods for connections to the wpvs model in a database.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * @param <G>
 *            the Geometry type used to create the envelope from.
 * 
 */
public abstract class DBBackend<G> extends ModelBackend<G> {

    /**
     * The <code>RelevantColumns</code> enum denotes the relevant column names of the tables containing renderable
     * objects.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author$
     * @version $Revision$, $Date$
     * 
     */
    private enum RelevantColumns {
        /**
         * The id of the renderable object
         */
        id( "id" ),
        /**
         * The uuid of the renderable object
         */
        uuid( "uuid" ),
        /**
         * A type definition of the renderable object
         */
        type( "model_type" ),
        /**
         * The bbox of the renderable object
         */
        envelope( "envelope" ),
        /**
         * The 2d bbox of the renderable object
         */
        footprint( "footprint" ),
        /**
         * The timestamp of last insertion
         */
        lastupdate( "lastupdate" ),
        /**
         * The actual renderable data.
         */
        data( "data" ),
        /**
         * Some name of the building
         */
        name( "name" ),
        /**
         * Some external reference.
         */
        externalRef( "externalref" );
        private String columnName;

        private RelevantColumns( String columnName ) {
            this.columnName = columnName;
        }

        /**
         * @return the columnname in the database.
         */
        public String getColumnName() {
            return columnName;
        }
    }

    /**
     * 
     * The <code>Tables</code> enum describe the tables for the db backend.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author$
     * @version $Revision$, $Date$
     * 
     */
    private enum Tables {
        /**
         * The building table name
         */
        BUILDINGS( "dobj_buildings" ),
        /**
         * The trees table name
         */
        TREES( "dobj_trees" ),
        /**
         * The prototypes table name
         */
        PROTOTYPES( "dobj_prototypes" ),
        /**
         * Information about the models
         */
        MODEL_INFO( "model_info" );

        private String tableName;

        Tables( String tableName ) {
            this.tableName = tableName;
        }

        /**
         * 
         * @return the table name of the given type.
         */
        public String getTableName() {
            return tableName;
        }

        /**
         * Get the tableName for an objecttype
         * 
         * @param objectType
         *            to get the name for
         * @return the table name.
         */
        public static String getTableName( Type objectType ) {
            switch ( objectType ) {
            case TREE:
                return Tables.TREES.getTableName();
            case PROTOTYPE:
                return Tables.PROTOTYPES.getTableName();
            default:
                return Tables.BUILDINGS.getTableName();
            }
        }
    }

    GeometryFactory geomFactory = new GeometryFactory();

    private static final String __TABLE__ = "_table_";

    /**
     * The prepared delete sql statement
     */
    private static final String DEL_SQL = "DELETE FROM " + __TABLE__ + " ";

    /**
     * The prepared insert sql statement
     */
    private static final String INS_SQL = "INSERT INTO " + __TABLE__ + " ( " + RelevantColumns.uuid.getColumnName()
                                          + "," + RelevantColumns.type.getColumnName() + ","
                                          + RelevantColumns.name.getColumnName() + ","
                                          + RelevantColumns.externalRef.getColumnName() + ","
                                          + RelevantColumns.envelope.getColumnName() + ", "
                                          + RelevantColumns.footprint.getColumnName() + ","
                                          + RelevantColumns.data.getColumnName() + ","
                                          + RelevantColumns.lastupdate.getColumnName()
                                          + ") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * The prepared update sql statement
     */
    private static final String UP_SQL = "UPDATE " + __TABLE__ + " SET " + RelevantColumns.envelope.getColumnName()
                                         + "=?, " + RelevantColumns.footprint.getColumnName() + "=?,"
                                         + RelevantColumns.data.getColumnName() + "=?,"
                                         + RelevantColumns.lastupdate.getColumnName() + "=?" + "WHERE "
                                         + RelevantColumns.uuid.getColumnName() + "=?";

    private static final String OBJ_FROM_ID = "SELECT " + RelevantColumns.data.getColumnName() + ","
                                              + RelevantColumns.envelope.getColumnName() + " FROM " + __TABLE__
                                              + " WHERE " + RelevantColumns.uuid.getColumnName() + "=?";


    private static final String INFO = "SELECT * FROM " + Tables.MODEL_INFO.getTableName() + " WHERE "
                                       + RelevantColumns.type.getColumnName() + "=?";

    /**
     * The prepared update sql statement
     */
    private static final String UP_INFO = "UPDATE " + Tables.MODEL_INFO.getTableName()
                                          + " SET ordinates=?, texture_ordinates=?" + "WHERE "
                                          + RelevantColumns.type.getColumnName() + "=?";

    /**
     * 
     * The prepared 'test-an-id' sql statement
     */
    private static final String TEST_SQL = "SELECT count( " + RelevantColumns.uuid.getColumnName() + ") FROM "
                                           + __TABLE__ + " WHERE " + RelevantColumns.uuid.getColumnName() + "=?";

    private final static Logger LOG = LoggerFactory.getLogger( DBBackend.class );

    private final String connectionID;

    private Type dataType;

    /**
     * @param connectionID
     *            to be used to get a connection from the {@link ConnectionManager}
     * @param type 
     */
    DBBackend( String connectionID, Type type ) {
        this.connectionID = connectionID;
        this.dataType = type;
    }

    @Override
    public List<RenderablePrototype> loadProtoTypes( DirectGeometryBuffer geometryBuffer, CRS baseCRS ) {
        List<RenderablePrototype> result = new LinkedList<RenderablePrototype>();
        try {
            PrototypeSerializer serializer = getPrototypeSerializer();
            serializer.setGeometryBuffer( geometryBuffer );
            getRenderableObjects( Tables.PROTOTYPES.getTableName(), result, serializer );
            serializer.setGeometryBuffer( null );
        } catch ( SQLException e ) {
            LOG.error( "Could not get Prototypes because: " + e.getLocalizedMessage() );
        }
        return result;
    }

    @Override
    public void loadBuildings( BuildingRenderer bm, CRS baseCRS ) {
        try {
            WROSerializer serializer = getBuildingSerializer();
            serializer.setGeometryBuffer( bm.getGeometryBuffer() );
            getRenderableObjects( Tables.BUILDINGS.getTableName(), bm, serializer );
            serializer.setGeometryBuffer( null );
        } catch ( SQLException e ) {
            LOG.error( "Could not get Buildings because: " + e.getLocalizedMessage() );
        }
    }

    @Override
    public void loadTrees( TreeRenderer tm, CRS baseCRS ) {
        Connection connection;
        try {
            connection = getConnection();
        } catch ( SQLException e ) {
            LOG.error( "Could not get trees because: " + e.getLocalizedMessage() );
            return;
        }
        ResultSet rs = getResultSet( connection, Tables.TREES.getTableName() );
        BillBoardSerializer serializer = getTreeSerializer();
        if ( rs != null ) {
            try {
                while ( rs.next() ) {
                    byte[] buf = rs.getBytes( RelevantColumns.data.getColumnName() );
                    BillBoard tree = serializer.deserializeDataObject( buf );
                    if ( tree != null ) {
                        tm.add( tree );
                    } else {
                        LOG.error( "Could not deserialize Tree from database." );
                    }
                }
            } catch ( SQLException e ) {
                LOG.error(
                           "Error while getting the renderable objects from the result set: " + e.getLocalizedMessage(),
                           e );
            }
        }
        close( connection, rs );
    }

    @Override
    public Object getDeSerializedObjectForUUID( Type objectType, String uuid )
                            throws IOException {
        Connection connection = null;
        Object result = null;
        try {
            connection = getConnection();
            result = getDeSerializedObjectForUUID( connection, objectType, uuid );
            connection.close();
        } catch ( SQLException e ) {
            throw new IOException( "Could not acquire a connection because: " + e.getLocalizedMessage(), e );
        }
        return result;
    }

    @Override
    public List<Object> getDeSerializedObjectsForSQL( Type objectType, String sqlWhere )
                            throws IOException {

        String tableName = Tables.getTableName( objectType );
        Connection connection = null;
        try {
            connection = getConnection();
        } catch ( SQLException e ) {
            throw new IOException( "Could not acquire a connection because: " + e.getLocalizedMessage(), e );
        }

        String retrieveIDS = "SELECT " + RelevantColumns.uuid.getColumnName() + " FROM " + tableName + " " + sqlWhere;
        List<Object> result = new LinkedList<Object>();
        try {
            PreparedStatement psUP = connection.prepareStatement( retrieveIDS );
            ResultSet rs = psUP.executeQuery();
            while ( rs.next() ) {
                String uuid = rs.getString( 1 );
                Object obj = getDeSerializedObjectForUUID( connection, objectType, uuid );
                if ( obj != null ) {
                    result.add( obj );
                }
            }
        } catch ( SQLException e ) {
            throw new IOException( "Error while retrieving objects from the database because: "
                                   + e.getLocalizedMessage(), e );
        }

        try {
            connection.close();
        } catch ( SQLException e ) {
            LOG.warn( "Could not close connection, let the manager deal with it: " + e.getLocalizedMessage() );
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object getDeSerializedObjectForUUID( Connection connection, Type objectType, String uuid )
                            throws IOException {
        Object result = null;
        try {
            PreparedStatement ps = connection.prepareStatement( mapTypeToTable( OBJ_FROM_ID, objectType ) );
            ps.setString( 1, uuid );
            ResultSet rs = ps.executeQuery();
            ObjectSerializer<?> serializer = super.getSerializerForType( objectType );
            if ( rs.next() ) {
                byte[] b = rs.getBytes( RelevantColumns.data.getColumnName() );
                if ( b != null ) {
                    result = serializer.deserializeDataObject( b );
                    if ( result != null ) {
                        if ( result instanceof WorldRenderableObject ) {
                            WorldRenderableObject wro = (WorldRenderableObject) result;
                            wro.setId( uuid );
                            wro.setBbox( createEnvelope( ( (G) rs.getObject( RelevantColumns.envelope.getColumnName() ) ) ) );
                        }
                    }
                }
            }
            ps.close();
        } catch ( SQLException e ) {
            throw new IOException( e );
        }
        return result;
    }

    @Override
    public BackendResult delete( String uuid, Type objectType, int qualityLevel, String sqlWhere )
                            throws IOException {
        try {
            if ( sqlWhere == null || "".equals( sqlWhere ) || !sqlWhere.toUpperCase().startsWith( "WHERE" ) ) {
                throw new IllegalArgumentException( "The sql statement must start with a 'where' and may not be null. " );
            }
            if ( objectType != TREE ) {
                if ( qualityLevel != -1 ) {
                    return updateWRO( objectType, qualityLevel, sqlWhere );
                }
            }
            return deleteObjectsFromDB( objectType, sqlWhere );
        } catch ( SQLException e ) {
            LOG.error( "Could not delete id: " + uuid + " because: " + e.getLocalizedMessage(), e );
            throw new IOException( e );
        }
    }

    /**
     * @param objectType
     * @param qualityLevel
     * @param sqlWhere
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private BackendResult updateWRO( Type objectType, int qualityLevel, String sqlWhere )
                            throws SQLException, IOException {
        String tableName = Tables.getTableName( objectType );
        BackendResult result = new BackendResult();
        Connection connection = getConnection();
        ModelBackendInfo info = getBackendInfo( connection, objectType );

        String updateSQL = mapTypeToTable( UP_SQL, objectType );
        String rowDelete = "DELETE FROM  " + tableName + " where " + RelevantColumns.uuid.getColumnName() + "=?";
        PreparedStatement rowDelStatement = connection.prepareStatement( rowDelete );

        String retrieveIDS = "SELECT " + RelevantColumns.uuid.getColumnName() + " FROM " + tableName + " " + sqlWhere;
        PreparedStatement psUP = connection.prepareStatement( retrieveIDS );
        ResultSet rs = psUP.executeQuery();
        WROSerializer serializer = (WROSerializer) getSerializerForType( objectType );
        while ( rs.next() ) {
            String uuid = rs.getString( 1 );
            WorldRenderableObject obj = (WorldRenderableObject) getDeSerializedObjectForUUID( connection, objectType,
                                                                                              uuid );
            if ( obj != null ) {
                RenderableQualityModel[] rqms = obj.getQualityLevels();
                if ( rqms != null ) {
                    if ( qualityLevel < rqms.length ) {
                        RenderableQualityModel rqm = rqms[qualityLevel];
                        if ( rqm != null ) {
                            int ordinates = rqm.getOrdinateCount();
                            int textureOrdinates = rqm.getTextureOrdinateCount();
                            info.addOrdinates( -ordinates );
                            info.addTextureOrdinates( -textureOrdinates );
                            obj.setQualityLevel( qualityLevel, null );
                            boolean deleteRow = true;
                            for ( int i = 0; i < obj.getNumberOfQualityLevels() && deleteRow; ++i ) {
                                deleteRow = ( obj.getQualityLevel( i ) == null );
                            }
                            if ( deleteRow ) {
                                rowDelStatement.setString( 1, uuid );
                                rowDelStatement.execute();
                                rowDelStatement.clearParameters();
                                result.deleteCount++;
                            } else {
                                DataObjectInfo<WorldRenderableObject> doi = new DataObjectInfo<WorldRenderableObject>(
                                                                                                                       uuid,
                                                                                                                       null,
                                                                                                                       null,
                                                                                                                       null,
                                                                                                                       obj.getBbox(),
                                                                                                                       obj );

                                doi.setSerializedData( serializer.serializeObject( doi ) );
                                doUpdate( doi, connection, updateSQL );
                                result.updateCount++;
                            }

                        }
                    }
                }
            } else {
                // the data was null, but a reference still exist, just delete the row.
                rowDelStatement.setString( 1, uuid );
                rowDelStatement.execute();
                rowDelStatement.clearParameters();
                result.deleteCount++;
            }
        }
        updateBackendInfo( connection, info, objectType );
        connection.close();
        return result;
    }

    /**
     * @param objectType
     * @param sqlWhere
     * @throws SQLException
     * @throws IOException
     */
    private BackendResult deleteObjectsFromDB( Type objectType, String sqlWhere )
                            throws SQLException, IOException {
        BackendResult result = new BackendResult();
        String deleteSQL = mapTypeToTable( DEL_SQL, objectType );
        deleteSQL += sqlWhere;
        Connection connection = getConnection();
        ModelBackendInfo info = getBackendInfo( connection, objectType );
        if ( objectType != TREE ) {
            String retrieveIDS = "SELECT " + RelevantColumns.uuid.getColumnName() + " FROM " + __TABLE__ + " "
                                 + sqlWhere;
            retrieveIDS = mapTypeToTable( retrieveIDS, objectType );
            PreparedStatement psUP = connection.prepareStatement( retrieveIDS );
            ResultSet rs = psUP.executeQuery();
            while ( rs.next() ) {
                String uuid = rs.getString( 1 );
                Object obj = getDeSerializedObjectForUUID( connection, objectType, uuid );
                updateInfoFile( obj, info, true );
            }
        }
        PreparedStatement ps = connection.prepareStatement( deleteSQL );
        ps.execute();
        int r = ps.getUpdateCount();
        if ( r == 0 ) {
            LOG.warn( "Could not determine the number of deleted objects, does your sqlStatement delete objects from the database: "
                      + deleteSQL );
        }
        result.deleteCount = r;
        ps.close();
        if ( objectType != TREE ) {
            updateBackendInfo( connection, info, objectType );
        }
        connection.close();
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends PositionableModel> BackendResult insert( List<DataObjectInfo<P>> model, Type objectType )
                            throws IOException {
        BackendResult result = new BackendResult();
        if ( model != null && !model.isEmpty() ) {
            String insertSQL = mapTypeToTable( INS_SQL, objectType );
            String updateSQL = mapTypeToTable( UP_SQL, objectType );
            String testSQL = mapTypeToTable( TEST_SQL, objectType );
            Connection connection;
            try {
                connection = getConnection();
            } catch ( SQLException e ) {
                throw new IOException( "Transaction failed because no connection could be established: "
                                       + e.getLocalizedMessage(), e );
            }
            ObjectSerializer<P> serializer = (ObjectSerializer<P>) getSerializerForType( objectType );
            ModelBackendInfo info = null;

            try {
                info = getBackendInfo( connection, objectType );
            } catch ( SQLException e ) {
                throw new IOException( "Transaction failed because modelbackendinfo could be retrieved: "
                                       + e.getLocalizedMessage(), e );
            }
            for ( DataObjectInfo<P> dm : model ) {
                if ( dm != null ) {
                    try {
                        if ( shouldUpdate( connection, dm.getUuid(), testSQL ) ) {
                            executeUpdate( dm, connection, updateSQL, info );
                            result.updateCount++;
                        } else {
                            dm.setSerializedData( serializer.serializeObject( dm ) );
                            doInsert( dm, connection, insertSQL );
                            updateInfoFile( dm.getData(), info, false );
                            result.insertCount++;
                        }
                    } catch ( SQLException e ) {
                        LOG.warn( "Failed to insert object with uuid: " + dm.getUuid() + " because: "
                                  + e.getLocalizedMessage() );
                    }
                }
            }
            try {
                updateBackendInfo( connection, info, objectType );
            } catch ( SQLException e ) {
                LOG.warn( "Could not update modelbackend info: " + e.getLocalizedMessage() );
            }
            try {
                connection.close();
            } catch ( SQLException e ) {
                LOG.warn( "Could not close connection, let the manager deal with it: " + e.getLocalizedMessage() );
            }
        }
        return result;
    }

    private void updateInfoFile( Object obj, ModelBackendInfo info, boolean delete ) {
        if ( obj != null ) {
            if ( obj instanceof WorldRenderableObject ) {
                int sub = delete ? -1 : 1;
                int o = sub * ( (WorldRenderableObject) obj ).getOrdinateCount();
                info.addOrdinates( o );
                o = sub * ( (WorldRenderableObject) obj ).getTextureOrdinateCount();
                info.addTextureOrdinates( o );
            }
        }
    }

    /**
     * @param connection
     * @param info
     * @param objectType
     * @throws SQLException
     */
    private void updateBackendInfo( Connection connection, ModelBackendInfo info, Type objectType )
                            throws SQLException {
        PreparedStatement ps = connection.prepareStatement( UP_INFO );
        ps.setInt( 1, info.getOrdinateCount() );
        ps.setInt( 2, info.getTextureOrdinateCount() );
        ps.setString( 3, objectType.getModelTypeName() );
        ps.execute();
        ps.close();
    }

    /**
     * @param wo
     * @param connection
     * @throws SQLException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private <P extends PositionableModel> void executeUpdate( DataObjectInfo<P> dm, Connection connection,
                                                              String updateSQL, ModelBackendInfo info )
                            throws SQLException, IOException {
        P newObject = dm.getData();
        if ( newObject instanceof WorldRenderableObject ) {
            updateWorldRenderableObject( (DataObjectInfo<WorldRenderableObject>) dm, connection, updateSQL, info );
        } else if ( newObject instanceof BillBoard ) {
            updateBillBoard( (DataObjectInfo<BillBoard>) dm, connection, updateSQL );
        } else {
            LOG.error( "The object: " + newObject + " is of unknown type, could not update. " );
        }
    }

    /**
     * @param dm
     * @param connection
     * @param updateSQL
     * @throws SQLException
     */
    private void updateBillBoard( DataObjectInfo<BillBoard> dm, Connection connection, String updateSQL )
                            throws SQLException {
        dm.setSerializedData( getTreeSerializer().serializeObject( dm ) );
        doUpdate( dm, connection, updateSQL );
    }

    /**
     * @param dm
     * @param connection
     * @param updateSQL
     * @throws SQLException
     * @throws IOException
     */
    private void updateWorldRenderableObject( DataObjectInfo<WorldRenderableObject> dm, Connection connection,
                                              String updateSQL, ModelBackendInfo info )
                            throws SQLException, IOException {
        WorldRenderableObject oldWRO = (WorldRenderableObject) getDeSerializedObjectForUUID( connection, Type.BUILDING,
                                                                                             dm.getUuid() );

        if ( oldWRO == null ) {
            LOG.error( "The id: " + dm.getUuid() + " is present in the database but no data was found, this is wrong." );
        } else {
            updateInfoFile( oldWRO, info, true );
            WorldRenderableObject wo = dm.getData();
            boolean mergeEnvelopes = false;
            for ( int i = 0; i < wo.getNumberOfQualityLevels(); ++i ) {
                RenderableQualityModel rqm = wo.getQualityLevel( i );
                if ( rqm != null ) {
                    oldWRO.setQualityLevel( i, rqm );
                } else {
                    // rqm is null, lets test if the other wro has quality levels, if not, we should not merge the
                    // envelopes, but use the new envelope instead.
                    if ( !mergeEnvelopes ) {
                        mergeEnvelopes = oldWRO.getQualityLevel( i ) != null;
                    }
                }

            }
            Envelope nEnv = wo.getBbox();
            Envelope oldEnv = oldWRO.getBbox();

            Envelope resultEnv = mergeEnvelopes ? oldEnv.merge( nEnv ) : nEnv;

            dm.setEnvelope( resultEnv );
            dm.setData( oldWRO );
            dm.setSerializedData( getBuildingSerializer().serializeObject( dm ) );
            doUpdate( dm, connection, updateSQL );
            updateInfoFile( oldWRO, info, false );
        }

    }

    private String mapTypeToTable( String sqlStatement, Type objectType ) {
        String table = Tables.getTableName( objectType );
        return replaceTableName( sqlStatement, table );
    }

    /**
     * Execute an insert statement, by setting given values.
     * 
     * @param connection
     * @param uuid
     * @param type
     * @param name
     * @param externalRef
     * @param envelope
     * @param data
     * @throws SQLException
     */
    private <P extends PositionableModel> void doInsert( DataObjectInfo<P> dataModel, Connection connection,
                                                         String insertSQL )
                            throws SQLException {

        PreparedStatement ps = connection.prepareStatement( insertSQL );
        ps.setString( 1, dataModel.getUuid() );
        ps.setString( 2, dataModel.getType() );
        ps.setString( 3, dataModel.getName() );
        ps.setString( 4, dataModel.getExternalRef() );
        setEnvelopeAndFootPrintType( ps, dataModel.getEnvelope(), true );
        ps.setBytes( 7, dataModel.getSerializedData() );
        // ps.setBinaryStream( 7, new BufferedInputStream( new ByteArrayInputStream( data ) ) );
        ps.setTimestamp( 8, new Timestamp( System.currentTimeMillis() ) );
        ps.execute();
        ps.close();
    }

    /**
     * Execute an update statement, by setting given values.
     * 
     * @param connection
     * @param uuid
     * @param envelope
     * @param data
     * @throws SQLException
     */
    private <P extends PositionableModel> void doUpdate( DataObjectInfo<P> dataModel, Connection connection,
                                                         String updateSQL )
                            throws SQLException {
        PreparedStatement ps = connection.prepareStatement( updateSQL );
        setEnvelopeAndFootPrintType( ps, dataModel.getEnvelope(), false );
        ps.setBytes( 3, dataModel.getSerializedData() );
        ps.setTimestamp( 4, new Timestamp( System.currentTimeMillis() ) );
        ps.setString( 5, dataModel.getUuid() );
        ps.execute();
        ps.close();
    }

    /**
     * Tests if the given uuid is present in the db to which the {@link Connection} is attached.
     * 
     * @param connection
     *            to create the statement for.
     * 
     * @param uuid
     *            the uuid to test for
     * @param testSQL2
     * 
     * @return true if the given uuid is in the database to which the connection is attached.
     * @throws SQLException
     */
    private boolean shouldUpdate( Connection connection, String uuid, String testSQL )
                            throws SQLException {

        PreparedStatement testPS = connection.prepareStatement( testSQL );
        testPS.setString( 1, uuid );

        ResultSet rs = testPS.executeQuery();
        boolean result = false;
        if ( rs.next() ) {
            int count = rs.getInt( 1 );
            result = ( count > 0 );
            if ( count == 1 ) {
                LOG.info( "id: " + uuid + " is already present in the db, creating update statement. " );
            }
            if ( count > 1 ) {
                LOG.warn( "id: " + uuid + " has multiple presents in the db, it is inconsistent. " );
            }
        }
        rs.close();
        testPS.close();

        return result;
    }

    /**
     * Use the {@link RelevantColumns} to get the column names of the tables.
     * 
     * @return
     */
    private String getRelevantColumnNames() {
        return RelevantColumns.uuid.getColumnName() + "," + RelevantColumns.lastupdate.getColumnName() + ","
               + RelevantColumns.envelope.getColumnName() + "," + RelevantColumns.data.getColumnName() + ","
               + RelevantColumns.type.getColumnName() + "," + RelevantColumns.name.getColumnName() + ","
               + RelevantColumns.externalRef.getColumnName();
    }

    /**
     * @param ps
     * @param env
     *            to create the appropriate backend envelopes for.
     * @param insert
     *            true to signal an insert statement, false an update
     * @throws SQLException
     */
    private void setEnvelopeAndFootPrintType( PreparedStatement ps, Envelope env, boolean insert )
                            throws SQLException {
        Object obj = createBackendEnvelope( env, 3 );
        int index = insert ? 5 : 1;
        ps.setObject( index, obj );
        obj = createBackendEnvelope( env, 2 );
        ps.setObject( index + 1, obj );
    }

    /**
     * replace the __table_name__ value from the given sql statement with the table name.
     * 
     * @param sql
     * @param tableName
     * @return
     */
    private String replaceTableName( String sql, String tableName ) {
        return sql.replace( __TABLE__, tableName );
    }

    /**
     * Retrieve a result set containing the columns defined in {@link #getRelevantColumnNames()} from the given table
     * name.
     * 
     * @param tableName
     *            to retrieve the resultset from
     * @return the resultset or <code>null</code> if an error occurred.
     */
    private ResultSet getResultSet( Connection connection, String tableName ) {
        ResultSet rs = null;
        try {
            rs = getResultSet( connection, tableName, getRelevantColumnNames() );
        } catch ( SQLException e ) {
            LOG.error( "Error while getting the renderable objects: " + e.getLocalizedMessage(), e );
        }
        return rs;
    }

    /**
     * Calls close on the given result set.
     * 
     * @param rs
     *            to be closed, may be <code>null</code>
     */
    private void close( Connection connection, ResultSet rs ) {
        if ( connection != null ) {
            try {
                connection.close();
            } catch ( SQLException e ) {
                // just do not close it.
                LOG.warn( "Could not close the result set, waiting for automatic closure." );
            }
        }
        if ( rs != null ) {
            try {
                rs.close();
            } catch ( SQLException e ) {
                // just do not close it.
                LOG.warn( "Could not close the result set, waiting for automatic closure." );
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends WorldRenderableObject> void getRenderableObjects( String tableName, Collection<T> result,
                                                                         ObjectSerializer<?> serializer )
                            throws SQLException {
        Connection connection = getConnection();

        // Envelope dsEnv = getDatasetEnvelope( connection, tableName, RelevantColumns.envelope.name() );
        // if ( dsEnv != null ) {
        // LOG.debug( "Boundingbox of dataset: " + dsEnv );
        // if ( result instanceof BuildingRenderer ) {
        // BuildingRenderer bm = ( (BuildingRenderer) result );
        // if ( bm.getValidDomain() == null || bm.getValidDomain().getSpan0() == ModelDatasetWrapper.DEFAULT_SPAN ) {
        // bm.setValidDomain( dsEnv );
        // }
        //
        // }
        // }

        // ResultSet rs = getResultSet( connection, tableName );
        PreparedStatement ps = connection.prepareStatement( "Select count( " + RelevantColumns.id.getColumnName()
                                                            + ") FROM " + tableName );
        ResultSet rs1 = ps.executeQuery();
        int total = 0;
        if ( rs1.next() ) {
            total = rs1.getInt( 1 );
        }
        rs1.close();

        LOG.info( "Getting " + total + " world renderable objects (buildings/prototyes)." );
        ps = connection.prepareStatement( "Select " + getRelevantColumnNames() + " FROM " + tableName );
        // ps = connection.prepareStatement( "Select " + getRelevantColumnNames() + " FROM " + tableName
        // + " where uuid='PostTower'" );

        ResultSet rs = ps.executeQuery();
        if ( rs != null ) {
            try {
                int loaded = 0;
                int percentage = total / 10;

                while ( rs.next() ) {
                    byte[] buf = rs.getBytes( RelevantColumns.data.getColumnName() );
                    T wro = (T) serializer.deserializeDataObject( buf );
                    if ( wro != null ) {
                        Envelope env = createEnvelope( (G) rs.getObject( RelevantColumns.envelope.getColumnName() ) );
                        wro.setBbox( env );
                        wro.setExternalReference( rs.getString( RelevantColumns.externalRef.getColumnName() ) );
                        wro.setType( rs.getString( RelevantColumns.type.getColumnName() ) );
                        wro.setName( rs.getString( RelevantColumns.name.getColumnName() ) );
                        wro.setId( rs.getString( RelevantColumns.uuid.getColumnName() ) );
                        wro.setTime( rs.getTimestamp( RelevantColumns.lastupdate.getColumnName() ).toString() );
                        result.add( wro );
                    } else {
                        LOG.error( "Could not deserialize WorldRenderableObject from database because no data was found for uuid: "
                                   + rs.getString( RelevantColumns.uuid.getColumnName() ) );
                    }
                    if ( percentage != 0 && ( ( ++loaded ) % percentage == 0 ) ) {
                        LOG.info( "Loaded " + loaded + " of " + total + " objects from the database." );
                    }
                }
            } catch ( SQLException e ) {
                LOG.error(
                           "Error while getting the renderable objects from the result set: " + e.getLocalizedMessage(),
                           e );
            }
            rs.close();
        }
        connection.close();

    }

    /**
     * @return a connection to the database
     * @throws SQLException
     */
    public Connection getConnection()
                            throws SQLException {
        Connection connection = ConnectionManager.getConnection( connectionID );
        connection.setAutoCommit( true );
        return connection;
    }

    /**
     * @param tableName
     * @param columns
     * @throws SQLException
     */
    private ResultSet getResultSet( Connection connection, String tableName, String columns )
                            throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery( "SELECT " + columns + " FROM " + tableName );
        return rs;

    }

    @Override
    public ModelBackendInfo getBackendInfo( org.deegree.services.wpvs.io.ModelBackend.Type type ) {
        ModelBackendInfo result = new ModelBackendInfo();
        try {
            Connection connection = getConnection();
            result = getBackendInfo( connection, type );
            connection.close();
        } catch ( SQLException e ) {
            LOG.debug( "Error getting backendinfo: " + e.getLocalizedMessage(), e );
            LOG.error( "Unable to retrieve modelbackendinfo, this is wrong. Error was: " + e.getLocalizedMessage() );
        }
        return result;
    }

    /**
     * @param connection
     * @param objectType
     * @return
     * @throws SQLException
     */
    private ModelBackendInfo getBackendInfo( Connection connection, Type objectType )
                            throws SQLException {
        ModelBackendInfo result = new ModelBackendInfo();
        PreparedStatement ps = connection.prepareStatement( INFO );
        Envelope env = null;
        switch ( objectType ) {
        case BUILDING:
        case STAGE:
            ps.setString( 1, Type.BUILDING.getModelTypeName() );
            env = getDatasetEnvelope( connection, Tables.BUILDINGS.getTableName(),
                                      RelevantColumns.envelope.getColumnName() );
            break;
        case PROTOTYPE:
            ps.setString( 1, Type.PROTOTYPE.getModelTypeName() );
            env = getDatasetEnvelope( connection, Tables.PROTOTYPES.getTableName(),
                                      RelevantColumns.envelope.getColumnName() );
            break;
        case TREE:
            ps.setString( 1, Type.TREE.getModelTypeName() );
            env = getDatasetEnvelope( connection, Tables.TREES.getTableName(), RelevantColumns.envelope.getColumnName() );
        }
        ResultSet rs = ps.executeQuery();
        if ( rs.next() ) {
            result.addOrdinates( rs.getInt( 2 ) );
            result.addTextureOrdinates( rs.getInt( 3 ) );
            result.setDatasetEnvelope( env );
        } else {
            // create the type in the db;
            LOG.info( "No row for objectType: " + objectType.getModelTypeName() + " creating one." );
            connection.prepareStatement(
                                         "INSERT INTO " + Tables.MODEL_INFO.getTableName() + " VALUES ( '"
                                                                 + objectType.getModelTypeName() + "', 0, 0 )" ).execute();
        }

        rs.close();
        ps.close();
        return result;
    }

    @Override
    public void flush() {
        // nothing
    }
    
    @Override
    public void loadEntities( RenderableManager<?> renderer, CRS baseCRS ) {
        if ( dataType == Type.TREE ) {
            loadTrees( (TreeRenderer) renderer, baseCRS );
        } else {
            loadBuildings( (BuildingRenderer) renderer, baseCRS );
        }
    }

    @Override
    public boolean isBillboard() {
        return dataType == Type.TREE;
    }

    /**
     * Should return the envelope in WPVS scene coordinates, not in world coordinates.
     * 
     * @param con
     * @param tableName
     * @param geomColumn
     * @return the envelope of the given dataset.
     * @throws SQLException
     */
    protected abstract Envelope getDatasetEnvelope( Connection con, String tableName, String geomColumn )
                            throws SQLException;

}
