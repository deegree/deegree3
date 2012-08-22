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
package org.deegree.io.quadtree;

import java.io.IOException;
import java.io.StringReader;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.DBPoolException;
import org.deegree.io.JDBCConnection;
import org.deegree.io.dbaseapi.DBaseException;
import org.deegree.io.shpapi.HasNoDBaseFileException;
import org.deegree.io.shpapi.ShapeFile;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;

/**
 * Access control to a quadtree for managing spatial indizes stored in a usual database.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 * @param <T>
 *            the type of the quadtree. If unsure use the determineQuattreType() method to determine
 *            the type. Be carefull though, if you use a wrong generic here (e.g. not Integer or
 *            String) while supplying another types.Type to the constructor there is no way to check
 *            find the correct instance.
 */
public class DBQuadtreeManager<T> {

    private static final ILogger LOG = LoggerFactory.getLogger( DBQuadtreeManager.class );

    protected JDBCConnection jdbc = null;

    protected String table = null;

    protected String column = null;

    protected String owner = null;

    protected String indexName = null;

    protected int maxDepth = 6;

    private DBQuadtree<T> qt = null;

    protected Envelope envelope = null;

    protected String backend = null;

    private int TYPE;

    private static HashMap<String, String> quadTreeVersionInfo = new HashMap<String, String>();

    /**
     * @param jdbc
     *            database connection info
     * @param owner
     *            owner of the table (optional, database user will be used if set to null )
     * @param indexName
     *            this name will be used to create the table that stores the nodes of a specific quadtree
     * @param table
     *            name of table the index shall be created for
     * @param column
     *            name of column the index shall be created for
     * @param maxDepth
     *            max depth of the generated quadtree (default = 6 if a value &lt; 2 will be passed)
     * @param type
     *            the type of the id of the quadtree, valid are Types.INTEGER and Types.VARCHAR. if unsure use
     *            Intger.MIN_VALUE and call {@link #determineQuattreeType()}, to get an instance of the Type.
     */
    public DBQuadtreeManager( JDBCConnection jdbc, String owner, String indexName, String table, String column,
                              int maxDepth, int type ) {
        TYPE = type;
        if ( TYPE != Types.INTEGER && TYPE != Types.VARCHAR ) {
            TYPE = Integer.MIN_VALUE;
        }
        if ( jdbc == null ) {
            throw new InvalidParameterException( "The JDBCConnection reference parameter 'jdbc' may not be null." );
        }
        this.jdbc = jdbc;
        if ( table == null || "".equals( table.trim() ) ) {
            throw new InvalidParameterException( "The 'table' parameter may not be null or emtpy." );
        }
        this.table = table.trim();

        if ( indexName == null || "".equals( indexName.trim() ) || "idx_".equalsIgnoreCase( indexName.trim() ) ) {
            throw new InvalidParameterException(
                                                 "The 'indexName' parameter may not be null or emtpy or solumnly exist of idx_." );
        }
        this.indexName = indexName.trim();

        if ( column == null || "".equals( column.trim() ) ) {
            throw new InvalidParameterException( "The 'column' parameter may not be null or emtpy." );
        }
        this.column = column.trim();

        this.owner = owner;
        if ( owner == null ) {
            String user = jdbc.getUser();
            if ( user == null || "".equals( user.trim() ) ) {
                this.owner = "";
            } else {
                this.owner = user;
            }
        }
        if ( maxDepth > 1 ) {
            this.maxDepth = maxDepth;
        } else {
            this.maxDepth = 6;
        }

        String driver = jdbc.getDriver();
        if ( driver == null || "".equals( driver.trim() ) ) {
            throw new InvalidParameterException( "The JDBCConnection.driver may not be null or emtpy." );
        }
        // find out which database is used
        if ( driver.toUpperCase().contains( "POSTGRES" ) ) {
            backend = "POSTGRES";
        } else if ( driver.toUpperCase().contains( "SQLSERVER" ) ) {
            backend = "SQLSERVER";
        } else if ( driver.toUpperCase().contains( "INGRES" ) || driver.equals( "ca.edbc.jdbc.EdbcDriver" ) ) {
            backend = "INGRES";
        } else if ( driver.toUpperCase().contains( "HSQLDB" ) ) {
            backend = "HSQLDB";
        } else {
            backend = "GENERICSQL";
        }

        try {
            if ( !hasIndexTable() ) {
                LOG.logDebug( "It seems no indextable with name: '" + indexName
                              + "' exists in the database backend, creating one." );
                createIndexTable( indexName, "VARCHAR(50)" );
            }
        } catch ( IndexException e ) {
            LOG.logWarning( "Could not create index (does it already exist?): " + e.getMessage() );
            LOG.logDebug( "Stack trace: " + e.getMessage(), e );
        }
    }

    /**
     * 
     * @param driver
     *            database connection driver
     * @param logon
     *            database connection logon
     * @param user
     *            database user
     * @param password
     *            database user's password
     * @param encoding
     *            character encoding to be used (if possible)
     * @param indexName
     *            this name will be used to create the table that stores the nodes of a specific quadtree
     * @param table
     *            name of table the index shall be created for
     * @param column
     *            name of column the index shall be created for
     * @param owner
     *            owner of the table (optional, database user will be used if set to null )
     * @param maxDepth
     *            max depth of the generated quadtree (default = 6 if a value &lt; 2 will be passed)
     * @param type
     *            the type of the id of the quadtree, valid are Types.INTEGER and Types.VARCHAR. if unsure use
     *            Intger.MIN_VALUE and call {@link #determineQuattreeType()}, to get an instance of the Type.
     */
    public DBQuadtreeManager( String driver, String logon, String user, String password, String encoding,
                              String indexName, String table, String column, String owner, int maxDepth, int type ) {
        this( new JDBCConnection( driver, logon, user, password, null, encoding, null ), owner, indexName, table,
              column, maxDepth, type );
    }

    /**
     * initializes a QuadtreeManager to access an alread existing Quadtree
     * 
     * @param jdbc
     *            database connection info
     * @param table
     *            name of table the index shall be created for
     * @param column
     *            name of column the index shall be created for
     * @param owner
     *            owner of the table (optional, database user will be used if set to null )
     * @param type
     *            the type of the id of the quadtree, valid are Types.INTEGER and Types.VARCHAR. if unsure use
     *            Intger.MIN_VALUE and call {@link #determineQuattreeType()}, to get an instance of the Type.
     */
    public DBQuadtreeManager( JDBCConnection jdbc, String table, String column, String owner, int type ) {
        this( jdbc, owner, "idx_" + table, table, column, 6, type );
    }

    /**
     * initializes a QuadtreeManager to access an alread existing Quadtree
     * 
     * @param driver
     *            database connection driver
     * @param logon
     *            database connection logon
     * @param user
     *            database user
     * @param password
     *            database user's password
     * @param encoding
     *            character encoding to be used (if possible)
     * @param table
     *            name of table the index shall be created for
     * @param column
     *            name of column the index shall be created for
     * @param owner
     *            owner of the table (optional, database user will be used if set to null )
     * @param type
     *            the type of the id of the quadtree, valid are Types.INTEGER and Types.VARCHAR. if unsure use
     *            Intger.MIN_VALUE and call {@link #determineQuattreeType()}, to get an instance of the Type.
     */
    public DBQuadtreeManager( String driver, String logon, String user, String password, String encoding, String table,
                              String column, String owner, int type ) {
        this( new JDBCConnection( driver, logon, user, password, null, encoding, null ), owner, "idx_" + table, table,
              column, 6, type );
    }

    /**
     * loads the metadata of a Index from the TAB_DEEGREE_IDX table
     * 
     * @return FK to the index
     * @throws IndexException
     */
    protected int loadIndexMetadata()
                            throws IndexException {
        int fk_indexTree = -1;
        Connection con = null;
        DBConnectionPool pool = null;
        try {
            pool = DBConnectionPool.getInstance();
            con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );

            StringBuilder sb = new StringBuilder( 200 );
            sb.append( "Select INDEX_NAME, FK_INDEXTREE from TAB_DEEGREE_IDX where " );
            sb.append( "column_name = '" ).append( column ).append( "' AND " );
            sb.append( "table_name = '" ).append( table ).append( "' AND " );
            sb.append( "owner = '" ).append( owner ).append( "'" );

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery( sb.toString() );

            if ( rs.next() ) {
                indexName = rs.getString( "INDEX_NAME" );
                fk_indexTree = rs.getInt( "FK_INDEXTREE" );
            } else {
                throw new IndexException(
                                          "Could not read the structure of the quadtree tables from database (did you run the base/scripts/index/quadtree.hsql script, which create the meta-info tables?)." );
            }
            rs.close();
            stmt.close();
        } catch ( SQLException e ) {
            throw new IndexException(
                                      "Could not load quadtree definition from database (did you run the base/scripts/index/quadtree.hsql script, which create the meta-info tables?). The error message was: "
                                                              + e.getMessage() );
        } catch ( DBPoolException e ) {
            throw new IndexException( "Could not acquire a database connection. The error message was: "
                                      + e.getMessage() );
        } finally {
            try {
                if ( pool != null && con != null ) {
                    pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                }
            } catch ( Exception e1 ) {
                LOG.logError( "Could not release the jdbc connection because: " + e1.getMessage() );
            }
        }
        LOG.logDebug( "It seems an indextable with name: '" + indexName + "' allready exists in the database backend." );
        return fk_indexTree;
    }

    /**
     * returns the current Quadtree
     * 
     * @return the current Quadtree
     * @throws IndexException
     */
    public DBQuadtree<T> getQuadtree()
                            throws IndexException {
        if ( qt == null ) {
            qt = loadQuadtree();
        }
        return qt;
    }

    /**
     * loads an already existing quadtree
     * 
     * @return the Quadtree structure read from the database
     * @throws IndexException
     */
    private DBQuadtree<T> loadQuadtree()
                            throws IndexException {
        int fk_index = loadIndexMetadata();

        String version = getQTVersion( table );
        return new DBQuadtree<T>( fk_index, indexName, jdbc, version );
    }

    /**
     * @return an instance of the type of the feature id's stored in the db. Possible instances are
     *         <code>String<code>, <code>Integer</code> or <code>null</code> if the type could not be determined.
     * @throws IndexException
     *             if the type information could not be retrieved either because no connection was acquired or an error
     *             occurred while executing the select statement.
     */
    public Object determineQuattreeType()
                            throws IndexException {

        if ( TYPE == Integer.MIN_VALUE ) {
            StringBuilder sb = new StringBuilder( 1000 );
            sb.append( "SELECT FK_ITEM from " ).append( indexName ).append( "_ITEM " );
            Connection con = null;
            DBConnectionPool pool = null;

            try {
                pool = DBConnectionPool.getInstance();
                con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );

                PreparedStatement stmt = con.prepareStatement( sb.toString() );
                ResultSet rs = stmt.executeQuery();
                ResultSetMetaData metaData = rs.getMetaData();

                if ( metaData != null ) {
                    TYPE = metaData.getColumnType( 1 );
                    LOG.logDebug( "Found type: " + TYPE );
                }
                rs.close();
                stmt.close();

            } catch ( SQLException e ) {
                throw new IndexException( "Could not get Type information because: " + e.getMessage(), e );
            } catch ( DBPoolException e ) {
                throw new IndexException(
                                          "Could not acquire a connection to the database to retrieve column information because: "
                                                                  + e.getMessage(), e );
            } finally {
                try {
                    if ( pool != null && con != null ) {
                        pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(),
                                                jdbc.getPassword() );
                    }
                } catch ( DBPoolException e ) {
                    LOG.logError( "Could not release JDBC connection because: " + e.getMessage() );
                }
            }
        }
        Object result = null;
        switch ( TYPE ) {
        case Types.VARCHAR:
            result = "";
            break;
        case Types.INTEGER:
            result = new Integer( 1 );
            break;
        default:
            TYPE = Integer.MAX_VALUE;
        }
        return result;
    }

    /**
     * @param table
     *            to open a quadtree for.
     * @return the version of the quadtree used.
     */
    /**
     * @param table
     */
    private String getQTVersion( String table ) {
        String version = "1.0.0";
        if ( quadTreeVersionInfo.containsKey( table ) && quadTreeVersionInfo.get( table ) != null ) {
            LOG.logDebug( "Retrieved the quatdree version info for table: " + table + " from cache." );
            version = quadTreeVersionInfo.get( table );
        } else {
            Connection con = null;
            DBConnectionPool pool = null;
            Statement stmt = null;
            ResultSet rs = null;
            pool = DBConnectionPool.getInstance();
            StringBuilder sb = new StringBuilder( 400 );
            sb.append( "SELECT fk_indextree FROM tab_deegree_idx WHERE " );
            sb.append( "column_name = 'geometry' AND " );
            sb.append( "table_name = '" ).append( table.toLowerCase() ).append( "'" );

            LOG.logDebug( "Get Index Metadata sql statement:\n", sb );
            try {
                con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                stmt = con.createStatement();
                rs = stmt.executeQuery( sb.toString() );
                String tableID = null;
                if ( rs.next() ) {
                    tableID = rs.getString( 1 );
                }
                if ( tableID != null ) {
                    sb = new StringBuilder( 400 );
                    sb.append( "SELECT * FROM tab_quadtree WHERE " );
                    sb.append( "fk_root = '" ).append( tableID.trim() ).append( "'" );
                    rs.close();
                    stmt.close();
                    stmt = con.createStatement();
                    rs = stmt.executeQuery( sb.toString() );
                    if ( rs.next() ) {
                        boolean hasVersion = false;
                        ResultSetMetaData md = rs.getMetaData();
                        int numberOfColumns = md.getColumnCount();
                        System.out.println( "Columnecount: " + numberOfColumns );
                        for ( int i = 1; i <= numberOfColumns && !hasVersion; i++ ) {
                            String tmp = md.getColumnName( i );
                            LOG.logDebug( "Found columnname: " + tmp );
                            if ( tmp != null ) {
                                if ( "version".equalsIgnoreCase( tmp.trim() ) ) {
                                    hasVersion = true;
                                    version = rs.getString( i );
                                    LOG.logDebug( "Found a version column, setting version to: " + rs.getString( i ) );
                                }
                            }
                        }
                        if ( !hasVersion ) {
                            try {
                                LOG.logInfo( "Found no Version Column in the TAB_QUADTREE table, assuming version 1.0.0, and adding the version column." );
                                rs.close();
                                stmt.close();
                                stmt = con.createStatement();
                                rs = stmt.executeQuery( "ALTER TABLE TAB_QUADTREE ADD version VARCHAR(15)" );
                                rs.close();
                                stmt.close();
                            } catch ( SQLException e ) {
                                rs.close();
                                stmt.close();
                                LOG.logError(
                                              "An error occurred while trying to insert a new 'version' column in the database: "
                                                                      + e.getMessage(), e );
                            }
                        }
                    }
                } else {
                    LOG.logError( "Could not find the foreign key (fk_root) of the table: '" + table
                                  + "'. Is your database set up correct?" );
                }
            } catch ( SQLException e ) {
                LOG.logError(
                              "An error occurred while determening version of quadtree, therefore setting version to '1.0.0'. Errormessage: "
                                                      + e.getMessage(), e );
            } catch ( DBPoolException e ) {
                LOG.logError(
                              "An error occurred while acquiring connection to the database to determine version of quadtree, therefore setting version to '1.0.0'. Errormessage: "
                                                      + e.getMessage(), e );
            } finally {
                quadTreeVersionInfo.put( table, version );
                try {
                    if ( rs != null ) {
                        rs.close();
                    }
                    if ( stmt != null ) {
                        stmt.close();
                    }
                    pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                } catch ( SQLException e ) {
                    LOG.logError( "Could not close ResultSet or Statement because: " + e.getMessage() );
                } catch ( DBPoolException e ) {
                    LOG.logError( "Could not release connection because: " + e.getMessage() );
                }
            }
        }
        return version;
    }

    /**
     * stores one feature into the defined table
     * 
     * @param feature
     *            the feature to insert into the 'table'
     * @param id
     *            of the feature to store in the database, currently String and Integer are supported. If it is neither,
     *            the Object is saved as an object, which may result in inconsitencies.
     * @param jdbc
     *            the connection to the database.
     * @throws IndexException
     *             if the feature can not be inserted or a connection error occurrs.
     */
    protected void storeFeature( Feature feature, T id, JDBCConnection jdbc )
                            throws IndexException {

        Connection con = null;
        DBConnectionPool pool = null;

        FeatureType ft = feature.getFeatureType();
        PropertyType[] ftp = ft.getProperties();
        try {
            pool = DBConnectionPool.getInstance();
            con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );

            StringBuilder sb = new StringBuilder( 100 );
            sb.append( "INSERT INTO " ).append( table ).append( '(' );
            sb.append( "FEATURE_ID," );
            for ( int i = 0; i < ftp.length; i++ ) {
                if ( ftp[i].getType() == Types.GEOMETRY ) {
                    sb.append( column ).append( ' ' );
                } else {
                    sb.append( ftp[i].getName().getLocalName() );
                }
                if ( i < ftp.length - 1 ) {
                    sb.append( ", " );
                }
            }
            sb.append( ") VALUES (?," );
            for ( int i = 0; i < ftp.length; i++ ) {
                sb.append( '?' );
                if ( i < ftp.length - 1 ) {
                    sb.append( ", " );
                }
            }
            sb.append( ')' );

            PreparedStatement stmt = con.prepareStatement( sb.toString() );
            if ( id instanceof String ) {
                LOG.logDebug( "Setting to id '" + id + "'an instance of String" );
                stmt.setString( 1, (String) id );
            } else if ( id instanceof Integer ) {
                LOG.logDebug( "Setting to id '" + id + "'an instance of integer" );
                stmt.setInt( 1, ( (Integer) id ).intValue() );
            } else {
                LOG.logWarning( "The type of id is uncertain (neiter String nor Integer), adding it as an 'object' to the database." );
                stmt.setObject( 1, id );
            }

            for ( int i = 0; i < ftp.length; i++ ) {
                Object o = null;
                if ( feature.getProperties( ftp[i].getName() ) != null ) {
                    if ( feature.getProperties( ftp[i].getName() ).length > 0 ) {
                        o = feature.getProperties( ftp[i].getName() )[0].getValue();
                    }
                }
                if ( o == null ) {
                    stmt.setNull( i + 2, ftp[i].getType() );
                } else {
                    switch ( ftp[i].getType() ) {
                    case Types.CHAR:
                    case Types.VARCHAR:
                        stmt.setString( i + 2, o.toString() );
                        break;
                    case Types.SMALLINT:
                    case Types.TINYINT:
                    case Types.INTEGER:
                    case Types.BIGINT:
                        stmt.setInt( i + 2, (int) Double.parseDouble( o.toString() ) );
                        break;
                    case Types.DOUBLE:
                    case Types.FLOAT:
                    case Types.DECIMAL:
                    case Types.NUMERIC:
                        stmt.setFloat( i + 2, Float.parseFloat( o.toString() ) );
                        break;
                    case Types.DATE:
                    case Types.TIME:
                    case Types.TIMESTAMP:
                        stmt.setDate( i + 2, (Date) o );
                        break;
                    case Types.GEOMETRY: {
                        StringBuffer gs = GMLGeometryAdapter.export( (Geometry) o );
                        String s = StringTools.replace( gs.toString(), ">",
                                                        " xmlns:gml=\"http://www.opengis.net/gml\">", false );
                        if ( backend.equals( "POSTGRES" ) || backend.equals( "HSQLDB" ) ) {
                            LOG.logDebug( "Adding geometry: " + s );
                            stmt.setString( i + 2, s );
                        } else if ( backend.equals( "INGRES" ) ) {
                            stmt.setObject( i + 2, new StringReader( s ) );
                        } else {
                            stmt.setObject( i + 2, s.getBytes() );
                        }
                        break;
                    }
                    default: {
                        LOG.logWarning( "unsupported type: " + ftp[i].getType() );
                    }
                    }
                }
            }
            LOG.logDebug( "SQL statement for insert feature: " + sb );
            if ( !stmt.execute() ) {
                LOG.logError( "The insertion of the feature resulted in " + stmt.getUpdateCount() + " updates." );
            }

            stmt.close();
        } catch ( SQLException e ) {
            String msg = "Could not insert feature with id='" + id + "' into the database because: " + e.getMessage();
            LOG.logError( msg, e );
            throw new IndexException( msg, e );
        } catch ( DBPoolException e ) {
            String msg = "Could not acquire a connection to the database to insert the feature with id: " + id;
            LOG.logError( msg, e );
            throw new IndexException( msg, e );
        } catch ( GeometryException e ) {
            String msg = "Could not insert feature with id='" + id + "' into the database because: " + e.getMessage();
            LOG.logError( msg, e );
            throw new IndexException( msg, e );
        } finally {
            try {
                if ( pool != null && con != null ) {
                    pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                }
            } catch ( DBPoolException e ) {
                LOG.logError( "Could not release JDBC connection because: " + e.getMessage() );
            }
        }
    }

    /**
     * initializes the root node of the quadtree
     * 
     * @param fileName
     * @throws IndexException
     * @throws IOException
     * 
     */
    protected void initRootNode( String fileName )
                            throws IndexException, IOException {
        LOG.logDebug( "Trying to read shapefile from file: " + fileName );
        ShapeFile sf = new ShapeFile( fileName );
        if ( envelope == null ) {
            envelope = sf.getFileMBR();
        }
        envelope = envelope.getBuffer( envelope.getWidth() / 20 );
        LOG.logInfo( "Bounding box of the root feature: " + envelope );
        sf.close();
        // DBQuadtree<T> qtTmp = loadQuadtree();
        Connection con = null;
        DBConnectionPool pool = null;
        try {
            pool = DBConnectionPool.getInstance();
            con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );

            StringBuilder sb = new StringBuilder( 100 );
            sb.append( "INSERT INTO " ).append( indexName );
            sb.append( " ( ID, MINX, MINY, MAXX , MAXY ) " );
            sb.append( "VALUES ( ?, ?, ?, ?, ? ) " );
            PreparedStatement stmt = con.prepareStatement( sb.toString() );
            stmt.setString( 1, "1" );
            stmt.setFloat( 2, (float) envelope.getMin().getX() );
            stmt.setFloat( 3, (float) envelope.getMin().getY() );
            stmt.setFloat( 4, (float) envelope.getMax().getX() );
            stmt.setFloat( 5, (float) envelope.getMax().getY() );
            stmt.execute();
            stmt.close();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new IndexException( "could not create root node definition at database", e );
        } finally {
            try {
                if ( pool != null && con != null ) {
                    pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                }
            } catch ( Exception e1 ) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * before importing a shape a user may set an envelope for the quadtree to bee created that is different from the
     * one of the shape by calling this method. Notice: calling this method does not have any effect when calling
     * 
     * @see #appendShape(String) method.
     * @param envelope
     */
    public void setRootEnvelope( Envelope envelope ) {
        this.envelope = envelope;
    }

    /**
     * initializes a new Quadtree by adding a row into table TAB_QUADTREE and into TAB_QTNODE (-> root node)
     * 
     * @param fileName
     * 
     * @return the id of the inserted node
     * @throws IndexException
     * @throws IOException
     *             if the shape file could not be read.
     */
    protected int initQuadtree( String fileName )
                            throws IndexException, IOException {

        initRootNode( fileName );
        Connection con = null;
        DBConnectionPool pool = null;
        int id = -1;
        try {
            pool = DBConnectionPool.getInstance();
            con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );

            // first check if the version column exists;
            StringBuilder versionCheck = new StringBuilder( "Select * from TAB_QUADTREE;" );
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery( versionCheck.toString() );
            boolean hasVersion = false;
            try {
                ResultSetMetaData md = rs.getMetaData();
                int numberOfColumns = md.getColumnCount();

                for ( int i = 1; i <= numberOfColumns && !hasVersion; i++ ) {
                    String tmp = md.getColumnName( i );
                    if ( tmp != null ) {
                        if ( "version".equalsIgnoreCase( tmp.trim() ) ) {
                            hasVersion = true;
                        }
                    }
                }
                if ( !hasVersion ) {
                    LOG.logInfo( "Found no Version Column in the TAB_QUADTREE table, assuming version 2.0.0, and adding the version column." );
                    rs.close();
                    stmt.close();
                    stmt = con.createStatement();
                    rs = stmt.executeQuery( "ALTER TABLE TAB_QUADTREE ADD version VARCHAR(15)" );
                    rs.close();
                    stmt.close();
                }
            } catch ( SQLException e ) {
                LOG.logError( "An error occurred while trying to determine if the database supports versioning: "
                              + e.getMessage() );
            }

            StringBuilder sb = new StringBuilder( 100 );
            sb.append( "INSERT INTO TAB_QUADTREE (" );
            if ( backend.equals( "INGRES" ) || backend.equals( "HSQLDB" ) ) {
                sb.append( "ID, " );
            }
            sb.append( "FK_ROOT, DEPTH, VERSION ) VALUES ( " );
            if ( backend.equals( "INGRES" ) || backend.equals( "HSQLDB" ) ) {
                stmt = con.createStatement();
                rs = stmt.executeQuery( "SELECT MAX(ID) FROM TAB_QUADTREE" );
                rs.next();
                int myid = rs.getInt( 1 ) + 1;
                sb.append( myid + ", " );
            }
            sb.append( " '1', ?, '2.0.0' ) " );

            PreparedStatement pstmt = con.prepareStatement( sb.toString() );
            pstmt.setInt( 1, maxDepth );
            pstmt.execute();
            pstmt.close();
            stmt = con.createStatement();
            rs = stmt.executeQuery( "select max(ID) from TAB_QUADTREE" );
            rs.next();
            id = rs.getInt( 1 );
            if ( id < 0 ) {
                throw new IndexException( "could not read ID of quadtree from database." );
            }
        } catch ( SQLException e ) {
            throw new IndexException(
                                      "Could not load quadtree definition from database (did you run the base/scripts/index/quadtree.hsql script, which create the meta-info tables?). The error message was: "
                                                              + e.getMessage() );
        } catch ( DBPoolException e ) {
            throw new IndexException(
                                      "Could not acquire a connection to the database to initiate the quattree index structure because: "
                                                              + e.getMessage() );
        } finally {
            try {
                if ( pool != null && con != null ) {
                    pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                }
            } catch ( DBPoolException e ) {
                LOG.logError( "Could not release JDBC connection because: " + e.getMessage() );
            }
        }
        return id;
    }

    /**
     * Inserts a row into the quadtree meta data structure 'TAB_DEEGREE_IDX', containing information on the table,
     * geometry, indexname, owner and the foreign_key to the index table.
     * 
     * @param fk_indexTree
     * @throws IndexException
     */
    public void insertIndexMetadata( int fk_indexTree )
                            throws IndexException {

        Connection con = null;
        DBConnectionPool pool = null;
        try {
            pool = DBConnectionPool.getInstance();
            con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );

            StringBuilder sb = new StringBuilder( 100 );
            sb.append( "INSERT INTO TAB_DEEGREE_IDX ( " );
            if ( backend.equals( "INGRES" ) || backend.equals( "HSQLDB" ) ) {
                sb.append( "ID, " );
            }
            sb.append( "column_name, table_name, " );
            sb.append( "owner, INDEX_NAME, FK_indexTree ) " );
            sb.append( "VALUES ( " );
            if ( backend.equals( "INGRES" ) || backend.equals( "HSQLDB" ) ) {
                Statement stm = con.createStatement();
                ResultSet rs = stm.executeQuery( "SELECT MAX(ID) FROM TAB_QUADTREE" );
                rs.next();
                int myid = rs.getInt( 1 ) + 1;
                sb.append( myid + ", " );
            }
            sb.append( "?, ?, ?, ?, ? ) " );
            PreparedStatement stmt = con.prepareStatement( sb.toString() );
            stmt.setString( 1, column );
            stmt.setString( 2, table );
            stmt.setString( 3, owner );
            stmt.setString( 4, indexName );
            stmt.setInt( 5, fk_indexTree );

            stmt.execute();
            stmt.close();
        } catch ( SQLException e ) {
            throw new IndexException(
                                      "Could not insert a new row into the quadtree index metadata table (did you run the base/scripts/index/quadtree.hsql script, which creates the meta-info tables?). The error message was: "
                                                              + e.getMessage() );
        } catch ( DBPoolException e ) {
            throw new IndexException(
                                      "Could not acquire a connection to the database to store the quattree index metadata structure because: "
                                                              + e.getMessage() );
        } finally {
            try {
                if ( pool != null && con != null ) {
                    pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                }
            } catch ( DBPoolException e ) {
                LOG.logError( "Could not release JDBC connection because: " + e.getMessage() );
            }
        }
    }

    /**
     * creates table the shape data shall be stored
     * 
     * @param fileName
     * @param idType
     *            the type of the feature_id column, for example VARCHAR(50) or NUMBER.
     * @throws IndexException
     * @throws IOException
     */
    protected void createDataTable( String fileName, String idType )
                            throws IndexException, IOException {
        ShapeFile sf = new ShapeFile( fileName );
        FeatureType ft = null;
        try {
            ft = sf.getFeatureByRecNo( 1 ).getFeatureType();
        } catch ( HasNoDBaseFileException e ) {
            throw new IndexException( e );
        } catch ( DBaseException e ) {
            throw new IndexException( e );
        }
        sf.close();
        StringBuilder sb = new StringBuilder( 1000 );
        sb.append( "CREATE TABLE " ).append( table ).append( '(' );

        sb.append( "FEATURE_ID " ).append( idType ).append( "," );
        PropertyType[] ftp = ft.getProperties();
        for ( int i = 0; i < ftp.length; i++ ) {
            if ( ftp[i].getType() == Types.GEOMETRY ) {
                sb.append( column ).append( ' ' );
            } else {
                sb.append( ftp[i].getName().getLocalName() ).append( ' ' );
            }
            sb.append( getDatabaseType( ftp[i].getType() ) );
            if ( i < ftp.length - 1 ) {
                sb.append( ", " );
            }
        }
        sb.append( ')' );

        Connection con = null;
        DBConnectionPool pool = null;
        try {
            pool = DBConnectionPool.getInstance();
            con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );

            Statement stmt = con.createStatement();
            LOG.logDebug( sb.toString() );
            stmt.execute( sb.toString() );
            stmt.close();
        } catch ( SQLException e ) {
            throw new IndexException( "Could not create a DataTable: '" + table
                                      + "' (which will hold the features from the shapefile: '" + fileName
                                      + "'). The error message was: " + e.getMessage(), e );
        } catch ( DBPoolException e ) {
            throw new IndexException( "Could not acquire a connection to the database to create a DataTable because: "
                                      + e.getMessage(), e );
        } finally {
            try {
                if ( pool != null && con != null ) {
                    pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                }
            } catch ( DBPoolException e ) {
                LOG.logError( "Could not release JDBC connection because: " + e.getMessage() );
            }
        }
    }

    /**
     * returns the type name for a generic type code as used by SQLServer
     * 
     * @param dataTypeCode
     * @return the type name for a generic type code as used by SQLServer
     */
    String getDatabaseType( int dataTypeCode ) {
        String type = null;

        switch ( dataTypeCode ) {
        case Types.CHAR:
        case Types.VARCHAR:
            type = DBQuadtreeDataTypes.getString( backend + ".string" );
            break;
        case Types.SMALLINT:
        case Types.TINYINT:
        case Types.INTEGER:
        case Types.BIGINT:
            type = DBQuadtreeDataTypes.getString( backend + ".integer" );
            break;
        case Types.DOUBLE:
        case Types.FLOAT:
        case Types.DECIMAL:
        case Types.NUMERIC:
            type = DBQuadtreeDataTypes.getString( backend + ".float" );
            break;
        case Types.DATE:
        case Types.TIME:
        case Types.TIMESTAMP:
            type = DBQuadtreeDataTypes.getString( backend + ".datetime" );
            break;
        case Types.GEOMETRY:
            type = DBQuadtreeDataTypes.getString( backend + ".geometry" );
            break;
        default:
            throw new InvalidParameterException( "Unknown data type code: " + dataTypeCode );
        }

        return type;
    }

    /**
     * imports a shape into the database and builds a quadtree on it
     * 
     * @param fileName
     *            of the shapefile.
     * @throws IOException
     *             if the shapefile could not be opened.
     * @throws IndexException
     *             if an error occurred while talking to the jdbc database.
     * @throws DBaseException
     *             if the connection to the shapefile could not be opened.
     * @throws HasNoDBaseFileException
     *             if the feature could not be read from shape file's database file.
     */
    public void importShape( String fileName )
                            throws IOException, IndexException, HasNoDBaseFileException, DBaseException {
        if ( TYPE == Integer.MIN_VALUE ) {
            LOG.logInfo( "You supplied an unknown type to the DBQuadtreeManager, therefore assuming you meant the Types.VARCHAR type" );
            TYPE = Types.VARCHAR;
        }
        StringBuilder typeName = new StringBuilder( 64 );

        typeName.append( getDatabaseType( TYPE ) );

        createDataTable( fileName, typeName.toString() );

        int qtid = initQuadtree( fileName );

        insertIndexMetadata( qtid );

        qt = new DBQuadtree<T>( qtid, indexName, jdbc );

        ShapeFile sf = new ShapeFile( fileName );

        double step = 100.0 / sf.getRecordNum();
        double counter = 0;
        Envelope sfEnv = sf.getFileMBR();

        LOG.logDebug( "The shape file read " + sf.getRecordNum() + " number of records" );
        for ( int i = 0; i < sf.getRecordNum(); i++ ) {
            Feature feat = sf.getFeatureByRecNo( i + 1 );
            if ( counter < step * i ) {
                if ( step < 1 ) {
                    counter += 10;
                } else {
                    counter += step;
                }
                System.out.println( counter + "%" );
            }
            if ( i % 200 == 0 ) {
                System.gc();
            }
            Envelope env = feat.getDefaultGeometryPropertyValue().getEnvelope();
            LOG.logDebug( i + " --- " + env );
            if ( env == null ) {
                // must be a point geometry
                Point point = (Point) feat.getDefaultGeometryPropertyValue();
                double w = sfEnv.getWidth() / 1000;
                double h = sfEnv.getHeight() / 1000;
                env = GeometryFactory.createEnvelope( point.getX() - w / 2d, point.getY() - h / 2d, point.getX() + w
                                                                                                    / 2d, point.getY()
                                                                                                          + h / 2d,
                                                      null );
            }
            // map to the requested featuretype id's type
            T id = getMappedID( i );
            LOG.logDebug( "Inserting item : " + i );
            qt.insert( id, env );
            storeFeature( feat, id, jdbc );
        }

        if ( "HSQLDB".equals( backend ) ) {
            LOG.logInfo( "Because you are using an hsql database, the current thread will wait '10' seconds, this gives the inmemory database time to flush it's tables" );
            try {
                Thread.sleep( 10000 );
            } catch ( InterruptedException e ) {
                LOG.logError(
                              "Exception occurred while waitig for the db-manager to flush it's memory tables. Message: "
                                                      + e.getMessage(), e );
            }
        }
        sf.close();
        LOG.logInfo( "finished!" );
    }

    @SuppressWarnings("unchecked")
    private T getMappedID( int i ) {
        if ( TYPE == Types.VARCHAR ) {
            return (T) UUID.randomUUID().toString();
        } else if ( TYPE == Types.INTEGER ) {
            return (T) new Integer( i );
        }
        return null;

    }

    /**
     * appends the features of a shape to an existing datatable and inserts references into the assigned quadtree table.
     * <p>
     * you have to consider that the quadtree is just valid for a defined area. if the features to append exceeds this
     * area the quadtree has to be rebuilded.
     * </p>
     * 
     * @param fileName
     * @throws IOException
     *             if the shape file cannot be read.
     * @throws IndexException
     *             if the quatree could not be read.
     */
    public void appendShape( String fileName )
                            throws IOException, IndexException {

        ShapeFile sf = new ShapeFile( fileName );

        int b = sf.getRecordNum() / 100;
        if ( b == 0 )
            b = 1;
        int k = 0;
        qt = getQuadtree();
        Envelope sfEnv = sf.getFileMBR();
        int cnt = getMaxIdValue();

        for ( int i = 0; i < sf.getRecordNum(); i++ ) {
            Feature feat = null;
            try {
                feat = sf.getFeatureByRecNo( i + 1 );
            } catch ( HasNoDBaseFileException e ) {
                throw new IndexException( e );
            } catch ( DBaseException e ) {
                throw new IndexException( e );
            }
            if ( i % b == 0 ) {
                System.out.println( k + "%" );
                k++;
            }
            if ( i % 200 == 0 ) {
                System.gc();
            }
            Envelope env = feat.getDefaultGeometryPropertyValue().getEnvelope();
            if ( env == null ) {
                // must be a point geometry
                Point point = (Point) feat.getDefaultGeometryPropertyValue();
                double w = sfEnv.getWidth() / 1000;
                double h = sfEnv.getHeight() / 1000;
                env = GeometryFactory.createEnvelope( point.getX() - w / 2d, point.getY() - h / 2d, point.getX() + w
                                                                                                    / 2d, point.getY()
                                                                                                          + h / 2d,
                                                      null );
            }
            // map to the requested featuretype id's type
            T id = getMappedID( cnt + i + 1 );
            qt.insert( id, env );
            storeFeature( feat, id, jdbc );
        }
        LOG.logInfo( " finished!" );
        sf.close();
    }

    /**
     * returns the maximum ID of the data table
     * 
     * @return the maximum ID of the data table
     * @throws IndexException
     */
    private int getMaxIdValue()
                            throws IndexException {
        if ( TYPE != Types.INTEGER ) {
            return 0;
        }
        String sql = "SELECT MAX( FEATURE_ID ) FROM " + table;

        Connection con = null;
        DBConnectionPool pool = null;
        Statement stmt = null;
        int maxId = 0;
        try {
            pool = DBConnectionPool.getInstance();
            con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );

            stmt = con.createStatement();
            LOG.logDebug( sql );
            ResultSet rs = stmt.executeQuery( sql );
            if ( rs.next() ) {
                maxId = rs.getInt( 1 );
            }
        } catch ( SQLException e ) {
            throw new IndexException(
                                      "Error while executing the sql statement while finding the max( Faeture_Id ) from table: "
                                                              + table, e );
        } catch ( DBPoolException e ) {
            throw new IndexException( "Could not acquire a jdbc connection to read the max( Faeture_Id ) from table: "
                                      + table, e );
        } finally {
            try {
                if ( pool != null && con != null ) {
                    pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                }
            } catch ( DBPoolException e ) {
                LOG.logError( "Could not release JDBC connection because: " + e.getMessage() );
            }
        }

        return maxId;
    }

    /**
     * Creates actually two tables, an indextable, which will hold the actual quadtree and an index_item table which is
     * a join-table between the dbNodes and the feature_ids.
     * 
     * @param indexTable
     *            name of the index table.
     * @param idType
     *            the type of the feature_id column, for example VARCHAR(50) or NUMBER.
     * @throws IndexException
     *             if the table could not be created.
     */
    protected void createIndexTable( String indexTable, String idType )
                            throws IndexException {
        StringBuilder sb = new StringBuilder( 2000 );
        String qtDataType = getDatabaseType( Types.VARCHAR );
        sb.append( "CREATE TABLE " ).append( indexTable ).append( " ( " );
        sb.append( "ID " ).append( qtDataType ).append( " NOT NULL," );
        sb.append( "minx float NOT NULL," );
        sb.append( "miny float NOT NULL," );
        sb.append( "maxx float NOT NULL," );
        sb.append( "maxy float NOT NULL," );
        sb.append( "FK_SUBNODE1 " ).append( qtDataType );
        sb.append( ", FK_SUBNODE2 " ).append( qtDataType );
        sb.append( ", FK_SUBNODE3 " ).append( qtDataType );
        sb.append( ", FK_SUBNODE4 " ).append( qtDataType ).append( ")" );

        StringBuilder sb2 = new StringBuilder( 1000 );
        sb2.append( "CREATE TABLE " ).append( indexName ).append( "_ITEM ( " );
        sb2.append( "FK_QTNODE " ).append( qtDataType ).append( " NOT NULL," );
        sb2.append( "FK_ITEM " ).append( idType ).append( " NOT NULL )" );

        Connection con = null;
        DBConnectionPool pool = null;
        try {
            pool = DBConnectionPool.getInstance();
            con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );

            Statement stmt = con.createStatement();
            stmt.execute( sb.toString() );
            stmt.close();

            stmt = con.createStatement();
            stmt.execute( sb2.toString() );
            stmt.close();
        } catch ( SQLException e ) {
            throw new IndexException( "Could not create the indextable: '" + indexTable
                                      + "' and/or the index_item table: '" + indexTable
                                      + "_ITEM'. The error message was: " + e.getMessage(), e );
        } catch ( DBPoolException e ) {
            throw new IndexException(
                                      "Could not acquire a connection to the database to store create the necessary tables: "
                                                              + e.getMessage(), e );
        } finally {
            try {
                if ( pool != null && con != null ) {
                    pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                }
            } catch ( DBPoolException e ) {
                LOG.logError( "Could not release JDBC connection because: " + e.getMessage() );
            }
        }
    }

    /**
     * Executes a simple select from indextable, and returns true if no SQL exception occurred.
     * 
     * @return true if a select * from indextable resulted in no exceptions, false otherwise.
     */
    private boolean hasIndexTable() {
        DBConnectionPool pool = DBConnectionPool.getInstance();
        Connection con = null;
        try {
            con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
            // what a great way to check for an existing table...
            Statement stmt = con.createStatement();
            stmt.execute( "SELECT * from " + indexName );
            stmt.close();
            return true;
        } catch ( DBPoolException e ) {
            LOG.logError( "Could not aqcuire connection to the database backend because: " + e.getMessage(), e );
            return false;
        } catch ( SQLException e ) {
            LOG.logDebug( "Stack trace: ", e );
            return false;
        } finally {
            try {
                if ( con != null ) {
                    pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                }
            } catch ( Exception e1 ) {
                LOG.logError( "Could not release the jdbc connection because: " + e1.getMessage() );
            }
        }
    }

    /**
     * Releases the db connection.
     */
    public void release() {
        qt.releaseConnection();
    }

}
