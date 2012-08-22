//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2006 by: M.O.S.S. Computer Grafik Systeme GmbH
 Hohenbrunner Weg 13
 D-82024 Taufkirchen
 http://www.moss.de/

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 ---------------------------------------------------------------------------*/
package org.deegree.io.datastore.sde;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.JDBCConnection;
import org.deegree.io.datastore.AnnotationDocument;
import org.deegree.io.datastore.Datastore;
import org.deegree.io.datastore.DatastoreConfiguration;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.DatastoreTransaction;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGMLSchema;
import org.deegree.io.datastore.sql.SQLAnnotationDocument;
import org.deegree.io.datastore.sql.SQLDatastoreConfiguration;
import org.deegree.io.datastore.sql.StatementBuffer;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.sdeapi.SDEAdapter;
import org.deegree.io.sdeapi.SDEConnection;
import org.deegree.io.sdeapi.SDEConnectionPool;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcwebservices.wfs.operation.Query;

import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeShape;

/**
 * Datastore implementation for an ESRI SDE database.
 * 
 * @author <a href="mailto:cpollmann@moss.de">Christoph Pollmann</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SDEDatastore extends Datastore {

    private static final ILogger LOG = LoggerFactory.getLogger( SDEDatastore.class );

    private SDEConnectionPool pool = null;

    @Override
    public AnnotationDocument getAnnotationParser() {
        return new SQLAnnotationDocument( this.getClass() );
    }
   
    @Override
    public void configure( DatastoreConfiguration config )
                            throws DatastoreException {
        super.configure( config );
        this.pool = SDEConnectionPool.getInstance();
    }

    @Override
    public DatastoreConfiguration getConfiguration() {
        return super.getConfiguration();
    }

    @Override
    public void bindSchema( MappedGMLSchema schema )
                            throws DatastoreException {
        super.bindSchema( schema );
    }

    @Override
    public MappedGMLSchema[] getSchemas() {
        return super.getSchemas();
    }

    @Override
    public MappedFeatureType getFeatureType( QualifiedName ftName ) {
        return super.getFeatureType( ftName );
    }

    @Override
    public void close()
                            throws DatastoreException {
        pool = null;
    }

    @Override
    public FeatureCollection performQuery( final Query query, final MappedFeatureType[] rootFts )
                            throws DatastoreException {

        FeatureCollection result = null;
        SDEConnection conn = acquireConnection();
        SDEQueryHandler queryHandler = new SDEQueryHandler( this, new TableAliasGenerator(), conn, rootFts, query );
        result = queryHandler.performQuery();
        releaseConnection( conn );
        return result;
    }

    @Override
    public DatastoreTransaction acquireTransaction()
                            throws DatastoreException {
        DatastoreTransaction transactionHandler = new SDETransaction( this, new TableAliasGenerator(),
                                                                      acquireConnection() );
        return transactionHandler;
    }

    /**
     * Returns a specific <code>WhereBuilder</code> implementation for SDE.
     * 
     * @param rootFts
     * @param aliases
     * @param filter
     * @param aliasGenerator
     * @return a specific <code>WhereBuilder</code> implementation for SDE.
     * @throws DatastoreException
     */
    public SDEWhereBuilder getWhereBuilder( MappedFeatureType[] rootFts, String[] aliases, Filter filter,
                                            TableAliasGenerator aliasGenerator )
                            throws DatastoreException {
        SDEWhereBuilder wb = new SDEWhereBuilder( rootFts, aliases, filter, aliasGenerator );
        return wb;
    }

    /**
     * Converts a database specific geometry <code>Object</code> from the <code>ResultSet</code> to a deegree
     * <code>Geometry</code>.
     * 
     * @param value
     * @param coordinateSystem 
     * @return corresponding deegree geometry
     * @throws DatastoreException
     */
    public Geometry convertDBToDegreeGeometry( Object value, CoordinateSystem coordinateSystem )
                            throws DatastoreException {

        Geometry geometry = null;
        if ( value != null ) {
            try {           	
                geometry = SDEAdapter.wrap( (SeShape) value, coordinateSystem );
            } catch ( Exception e ) {
                throw new DatastoreException( "Error converting SeShape to Geometry: " + e.getMessage() );
            }
        }
        return geometry;
    }

    /**
     * Converts a deegree <code>Geometry</code> to a database specific geometry <code>Object</code>.
     * 
     * @param geometry
     * @return corresponding database specific geometry object
     * @throws DatastoreException
     */
    public Object convertDegreeToDBGeometry( Geometry geometry )
                            throws DatastoreException {
        Object value = null;
        if ( geometry != null ) {
            try {
                // TODO: SRS handling
                SeCoordinateReference coordRef = new SeCoordinateReference();
                value = SDEAdapter.export( geometry, coordRef );
            } catch ( Exception e ) {
                throw new DatastoreException( "Error converting Geometry to SeShape: " + e.getMessage(), e );
            }
        }
        return value;
    }

    /**
     * Returns the database connection requested for.
     * 
     * @return Connection
     * @throws DatastoreException
     */
    protected SDEConnection acquireConnection()
                            throws DatastoreException {
        JDBCConnection jdbcConnection = ( (SQLDatastoreConfiguration) this.getConfiguration() ).getJDBCConnection();
        SDEConnection conn = null;
        try {
            String url = jdbcConnection.getURL();
            String[] tmp = url.split( ":" );
            int instance = 5151;
            if ( 2 == tmp.length ) {
                url = tmp[0];
                instance = Integer.parseInt( tmp[1] );
            }
            conn = pool.acquireConnection( url, instance, jdbcConnection.getSDEDatabase(),
                                           jdbcConnection.getSDEVersion(), jdbcConnection.getUser(),
                                           jdbcConnection.getPassword() );
        } catch ( Exception e ) {
            String msg = "Cannot acquire database connection: " + e.getMessage();
            LOG.logInfo( msg );
            throw new DatastoreException( msg, e );
        }
        return conn;
    }

    /**
     * Releases the connection.
     * 
     * @param conn
     *            Connection to be released.
     * @throws DatastoreException
     */
    protected void releaseConnection( SDEConnection conn )
                            throws DatastoreException {
        JDBCConnection jdbcConnection = ( (SQLDatastoreConfiguration) this.getConfiguration() ).getJDBCConnection();
        try {
            String url = jdbcConnection.getURL();
            String[] tmp = url.split( ":" );
            int instance = 5151;
            if ( 2 == tmp.length ) {
                url = tmp[0];
                instance = Integer.parseInt( tmp[1] );
            }
            pool.releaseConnection( conn, url, instance, jdbcConnection.getSDEDatabase(),
                                    jdbcConnection.getSDEVersion(), jdbcConnection.getUser() );
        } catch ( Exception e ) {
            String msg = "Cannot release database connection: " + e.getMessage();
            LOG.logInfo( msg );
            throw new DatastoreException( msg, e );
        }
    }

    /**
     * Converts the <code>StatementBuffer</code> into a <code>PreparedStatement</code>, which is initialized and
     * ready to be performed.
     * 
     * @param conn
     *            connection to be used to create the <code>PreparedStatement</code>
     * @param statementBuffer
     * @return the <code>PreparedStatment</code>, ready to be performed
     */
    public SeQuery prepareStatement( SDEConnection conn, StatementBuffer statementBuffer ) {
        LOG.logDebug( "Preparing statement: " + statementBuffer.getQueryString() );

        SeQuery query = null;
        try {
            query = new SeQuery( conn.getConnection() );
            query.prepareSql( statementBuffer.getQueryString() );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        // TODO
        return query;
    }

    @Override
    public FeatureCollection performQuery( Query query, MappedFeatureType[] rootFts, DatastoreTransaction context )
                            throws DatastoreException {
        throw new DatastoreException( "method invocation for sde not applicable" );
    }

    @Override
    public void releaseTransaction( DatastoreTransaction ta )
                            throws DatastoreException {
        releaseConnection( ( (SDETransaction) ta ).getConnection() );
    }
}