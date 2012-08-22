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
package org.deegree.tools.datastore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.FileUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.DBPoolException;
import org.deegree.io.dbaseapi.DBaseException;
import org.deegree.io.shpapi.HasNoDBaseFileException;
import org.deegree.io.shpapi.ShapeFile;
import org.xml.sax.SAXException;

/**
 * Example: java -classpath .;deegree.jar;$databasedriver.jar
 * org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables mytable,myothertable -user dev
 * -password dev -driver oracle.jdbc.OracleDriver -url jdbc:oracle:thin:@localhost:1521:devs -output
 * e:/temp/schema.xsd<br>
 * or for shapefile:<br>
 * java -classpath .;deegree.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -url
 * c:/data/myshape -driver SHAPE -output e:/temp/schema.xsd<br>
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DBSchemaToDatastoreConf {

    private static final ILogger LOG = LoggerFactory.getLogger( DBSchemaToDatastoreConf.class );

    private String[] tables;

    private String user;

    private String pw;

    private String driver;

    private String logon;

    private String backend;

    private String vendor;

    private String srs;

    private String defaultPKey;

    private String insert;

    private String update;

    private String delete;

    private String idGenerator;

    private String sequence;

    private Integer defaultSRID;

    private String omitFidAsProperty;

    /**
     * 
     * @param tables
     *            list of table names used for one featuretype
     * @param user
     *            database user
     * @param pw
     *            users password
     * @param driver
     *            database driver
     * @param logon
     *            database URL/logon
     * @param srs
     * @throws IOException
     */
    public DBSchemaToDatastoreConf( String[] tables, String user, String pw, String driver, String logon, String srs,
                                    String pkey, Integer srid, String insert, String update, String delete,
                                    String idGenerator, String sequence, String omitFidAsProperty ) throws IOException {
        this.driver = driver;
        this.logon = logon;
        this.pw = pw;
        this.user = user;
        this.tables = tables;
        if ( srs != null ) {
            this.srs = srs;
        } else {
            this.srs = readUserInput( DBSchemaToDatastoreConfSQLMSGAccess.getMessage( "ENTERSRS" ), false );
        }

        if ( driver.toUpperCase().indexOf( "ORACLE" ) > -1 ) {
            backend = "ORACLE";
            vendor = backend;
        } else if ( driver.toUpperCase().indexOf( "POSTGRES" ) > -1 ) {
            backend = "POSTGIS";
            vendor = backend;
        } else if ( driver.toUpperCase().contains( "SHAPE" ) ) {
            backend = "SHAPE";
            vendor = backend;
        } else {
            backend = "GENERICSQL";
            vendor = getVendor( driver );
        }
        this.defaultPKey = pkey;
        this.defaultSRID = srid;
        this.insert = insert;
        this.delete = delete;
        this.update = update;
        this.idGenerator = idGenerator;
        this.sequence = sequence;
        this.omitFidAsProperty = omitFidAsProperty;
    }

    private String getVendor( String driver ) {
        // find out which database is used
        String vendor = null;
        if ( driver.toUpperCase().contains( "POSTGRES" ) ) {
            backend = "POSTGRES";
        } else if ( driver.toUpperCase().contains( "SQLSERVER" ) ) {
            backend = "SQLSERVER";
        } else if ( driver.toUpperCase().contains( "INGRES" ) || driver.equals( "ca.edbc.jdbc.EdbcDriver" ) ) {
            backend = "INGRES";
        } else if ( driver.toUpperCase().contains( "HSQLDB" ) ) {
            backend = "HSQLDB";
        } else {
            backend = "SHAPE";
        }
        return vendor;
    }

    /**
     * creates a schema/datastore configuration for accessin database table through deegree WFS
     * 
     * @return a schema/datastore configuration for accessin database table through deegree WFS
     * @throws Exception
     */
    public String run()
                            throws Exception {
        StringBuffer sb = new StringBuffer( 5000 );

        if ( backend.equals( "SHAPE" ) ) {
            handleShape( sb );
        } else {
            handleDatabase( sb );
        }
        printFooter( sb );

        return sb.toString();
    }

    /**
     * creates a datastore configuration for a database backend
     * 
     * @param sb
     * @throws DBPoolException
     * @throws SQLException
     * @throws Exception
     * @throws UnknownTypeException
     * @throws IOException
     */
    private void handleDatabase( StringBuffer sb )
                            throws DBPoolException, SQLException, Exception, UnknownTypeException, IOException {
        printHeader( sb );

        for ( int k = 0; k < tables.length; k++ ) {
            LOG.logInfo( "Opening JDBC connection with driver: " + driver );
            LOG.logInfo( "Opening JDBC connection to database : " + logon );

            Connection con = DBConnectionPool.getInstance().acquireConnection( driver, logon, user, pw );
            Statement stmt = con.createStatement();
            // ensure that we do not get a filled resultset because we just
            // need the metainformation
            LOG.logDebug( "read table: ", tables[k] );
            ResultSet rs = stmt.executeQuery( "select * from " + tables[k] + " where 1 = 2" );

            ResultSetMetaData rsmd = rs.getMetaData();
            int cols = rsmd.getColumnCount();

            printComplexHeader( sb, tables[k] );
            for ( int i = 0; i < cols; i++ ) {
                if ( rsmd.getColumnType( i + 1 ) != 2004 ) {
                    int tp = rsmd.getColumnType( i + 1 );
                    String tpn = Types.getTypeNameForSQLTypeCode( tp );
                    LOG.logDebug( tables[k] + '.' + rsmd.getColumnName( i + 1 ) + ": " + tpn + " " + rsmd.getPrecision( i + 1 ) +  " " + rsmd.getScale( i + 1 ) ); 
                    // add property just if type != BLOB
                    if ( !"true".equals( omitFidAsProperty )
                         || !getPKeyName( tables[k] ).equalsIgnoreCase( rsmd.getColumnName( i + 1 ) ) ) {
                        printProperty( tables[k], rsmd.getColumnName( i + 1 ), rsmd.getColumnType( i + 1 ), tpn,
                                       rsmd.getPrecision( i + 1 ), rsmd.getScale( i + 1 ), sb );
                    }
                } else {
                    String msg = StringTools.concat( 200, "skiped: ", tables[k], '.', rsmd.getColumnName( i + 1 ),
                                                     ": ", rsmd.getColumnTypeName( i + 1 ) );
                    LOG.logDebug( msg );
                }
            }

            DBConnectionPool.getInstance().releaseConnection( con, driver, logon, user, pw );
            printComplexFooter( sb );
        }
    }

    /**
     * creates a datastore configuration for a shapefile backend
     * 
     * @param sb
     * @throws IOException
     * @throws Exception
     * @throws HasNoDBaseFileException
     * @throws DBaseException
     * @throws DBPoolException
     * @throws SQLException
     * @throws UnknownTypeException
     */
    private void handleShape( StringBuffer sb )
                            throws IOException, Exception, HasNoDBaseFileException, DBaseException, DBPoolException,
                            SQLException, UnknownTypeException {
        // TODO throw RE if tbl.len != 1

        printShapeHeader( sb, tables[0] );

        File f = new File( tables[0] );
        ShapeFile shp = new ShapeFile( f.getAbsolutePath() );

        printComplexHeader( sb, f.getName() );

        String[] dataTypes = shp.getDataTypes();

        printProperty( f.getName(), "GEOM", 2002, "GEOM", -9999, -9999, sb );

        String[] props = shp.getProperties();
        for ( int i = 0; i < props.length; i++ ) {
            int sqlCode = toSQLCode( dataTypes[i] ), precision, scale;
            printProperty( tables[0], props[i], sqlCode, Types.getTypeNameForSQLTypeCode( sqlCode ),
                           1, toScale( dataTypes[i] ), sb );
        }

        printComplexFooter( sb );

        shp.close();
    }

    /**
     * @return scale for a dBase numerical type
     * 
     * @param dbfType
     */
    private int toScale( String dbfType ) {
        int precision = 0;

        if ( dbfType.equalsIgnoreCase( "I" ) ) {
            precision = 1;
        } 

        return precision;
    }

    /**
     * @return the SQL type code for a dBase type char
     * 
     * @param dbfType
     */
    private int toSQLCode( String dbfType ) {

        int type = -9999;

        if ( dbfType.equalsIgnoreCase( "C" ) ) {
            type = Types.VARCHAR;
        } else if ( dbfType.equalsIgnoreCase( "F" ) || dbfType.equalsIgnoreCase( "N" ) 
        		|| dbfType.equalsIgnoreCase( "I" ) ) {
            type = Types.NUMERIC;
        } else if ( dbfType.equalsIgnoreCase( "D" ) || dbfType.equalsIgnoreCase( "M" ) ) {
            type = Types.DATE;
        } else if ( dbfType.equalsIgnoreCase( "L" ) ) {
            type = Types.BOOLEAN;
        } else if ( dbfType.equalsIgnoreCase( "B" ) ) {
            type = Types.BLOB;
        }

        if ( type == -9999 ) {
            throw new RuntimeException( "Type '" + dbfType + "' is not suported." );
        }

        return type;
    }

    /**
     * adds the header of the configuration/schema for a database datastore
     * 
     * @param sb
     */
    private void printHeader( StringBuffer sb ) {

        String s = DBSchemaToDatastoreConfSQLXSDAccess.getXSDFragment( "HEADER", backend, srs, driver, logon, user, pw );
        sb.append( s );

    }

    /**
     * adds the header of the configuration/schema for a shapefile datastore
     * 
     * @param sb
     * @param filename
     *            path to the shapefile
     */
    private void printShapeHeader( StringBuffer sb, String filename ) {

        String s = DBSchemaToDatastoreConfSQLXSDAccess.getXSDFragment( "SHAPEHEADER", filename, srs );
        sb.append( s );

    }

    /**
     * adds a header for a feature type to the schema
     * 
     * @param sb
     * @param table
     *            name of the table the feature type is assigned to
     * @throws Exception
     */
    private void printComplexHeader( StringBuffer sb, String table )
                            throws Exception {
        String idField = getPKeyName( table );
        String tp = "INTEGER";
        if ( backend.equals( "GENERICSQL" ) ) {
            tp = "VARCHAR";
        }
        String idg = "";
        if ( "DB_MAX".equalsIgnoreCase( idGenerator ) ) {
            idg = DBSchemaToDatastoreConfSQLXSDAccess.getXSDFragment( "DB_MAX_IDGENERATOR", table, idField );
        } else if ( "DB_SEQ".equalsIgnoreCase( idGenerator ) && sequence != null ) {
            idg = DBSchemaToDatastoreConfSQLXSDAccess.getXSDFragment( "DB_SEQ_IDGENERATOR", sequence );
        }
        String s = DBSchemaToDatastoreConfSQLXSDAccess.getXSDFragment( "COMPLEXHEADER", table, table, table, idField,
                                                                       tp, table, update, delete, insert, idg );
        sb.append( s );

    }

    /**
     * adds the footer of a feature type definition
     * 
     * @param sb
     */
    private void printComplexFooter( StringBuffer sb ) {
        sb.append( DBSchemaToDatastoreConfSQLXSDAccess.getXSDFragment( "COMPLEXFOOTER" ) );
    }

    /**
     * prints XSD footer
     * 
     * @param sb
     */
    private void printFooter( StringBuffer sb ) {
        sb.append( DBSchemaToDatastoreConfSQLXSDAccess.getXSDFragment( "FOOTER" ) );
    }

    /**
     * adds a property assigned to a database table field to the schema
     * 
     * @param tableName
     *            table name
     * @param name
     *            property name
     * @param type
     *            xsd type name
     * @param typeName
     *            SQL type name
     * @param precision
     *            number precision if type is a number
     * @param sb
     * @throws SQLException
     * @throws DBPoolException
     * @throws IOException
     */
    private void printProperty( String tableName, String name, int type, String typeName, int precision, int scale, StringBuffer sb )
                            throws DBPoolException, SQLException, IOException {

        String tp = Types.getXSDTypeForSQLType( type, precision, scale );
        if ( !tp.startsWith( "gml:" ) ) {
            tp = "xsd:" + tp;
        }

        if ( tp.equals( "gml:GeometryPropertyType" ) ) {
            int srid = getSRID( tableName, name );
            String s = DBSchemaToDatastoreConfSQLXSDAccess.getXSDFragment( "GEOMPROPERTY", name.toLowerCase(), tp,
                                                                           name, "" + srid );
            sb.append( s );
        } else {
            String s = DBSchemaToDatastoreConfSQLXSDAccess.getXSDFragment( "PROPERTY", name.toLowerCase(), tp, name,
                                                                           typeName.toUpperCase() );
            sb.append( s );
        }
    }

    private int getSRID( String tableName, String columnName )
                            throws SQLException, DBPoolException, IOException {

        if ( defaultSRID != null ) {
            return defaultSRID;
        }

        int srid = -1;
        String query = DBSchemaToDatastoreConfSQLSQLAccess.getSQLStatement( vendor + "_SRID", tableName.toUpperCase(),
                                                                            columnName.toUpperCase() );
        LOG.logInfo( query );
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        if ( query != null && query.indexOf( "not found$" ) < 0 ) {
            try {
                con = DBConnectionPool.getInstance().acquireConnection( driver, logon, user, pw );
                stmt = con.createStatement();
                rs = stmt.executeQuery( query );

                while ( rs.next() ) {
                    srid = rs.getInt( 1 );
                }

                if ( srid == 0 ) {
                    srid = -1;
                }

            } catch ( SQLException e ) {
                System.out.println( DBSchemaToDatastoreConfSQLMSGAccess.getMessage( "ERRORSRID" ) + e.getMessage() );
                System.out.println( DBSchemaToDatastoreConfSQLMSGAccess.getMessage( "ENTERFIELD" ) );
            } finally {
                rs.close();
                stmt.close();
                DBConnectionPool.getInstance().releaseConnection( con, driver, logon, user, pw );
            }
        } else {
            System.out.println( DBSchemaToDatastoreConfSQLMSGAccess.getMessage( "NOSRIDQUERY" ) );
        }
        if ( srid == -1 ) {
            String tmp = DBSchemaToDatastoreConfSQLMSGAccess.getMessage( "ENTERSRID" );
            srid = Integer.parseInt( readUserInput( tmp, false ) );
        }
        return srid;
    }

    /**
     * returns the name of the primary key of the passed table
     * 
     * @param table
     * @return the name of the primary key of the passed table
     * @throws DBPoolException
     * @throws SQLException
     * @throws IOException
     */
    private String getPKeyName( String table )
                            throws DBPoolException, SQLException, IOException {

        if ( defaultPKey != null ) {
            return defaultPKey;
        }

        String query = DBSchemaToDatastoreConfSQLSQLAccess.getSQLStatement( vendor + "_ID", table.toUpperCase() );
        LOG.logInfo( query );
        Object id = null;
        Statement stmt = null;
        ResultSet rs = null;
        if ( query != null && query.indexOf( "not found$" ) < 0 ) {
            Connection con = DBConnectionPool.getInstance().acquireConnection( driver, logon, user, pw );
            try {
                stmt = con.createStatement();
                rs = stmt.executeQuery( query );

                if ( rs.next() ) {
                    id = rs.getObject( 1 );
                }
            } catch ( Exception e ) {
                System.out.println( DBSchemaToDatastoreConfSQLMSGAccess.getMessage( "ERRORPK" ) + e.getMessage() );
                System.out.println( DBSchemaToDatastoreConfSQLMSGAccess.getMessage( "ENTERFIELD" ) );
            } finally {
                rs.close();
                stmt.close();
                DBConnectionPool.getInstance().releaseConnection( con, driver, logon, user, pw );
            }
        } else {
            System.out.println( DBSchemaToDatastoreConfSQLMSGAccess.getMessage( "NOPKQUERY" ) );
        }
        if ( id == null ) {
            id = readUserInput( DBSchemaToDatastoreConfSQLMSGAccess.getMessage( "ENTERPK" ), false );
        }
        return id.toString();
    }

    private static void validate( Map<String, String> map )
                            throws InvalidParameterException, IOException {
        if ( map.get( "-?" ) != null || map.get( "-h" ) != null || map.get( "-help" ) != null ) {
            printHelp();
            System.exit( 1 );
        }
        if ( map.get( "-tables" ) == null ) {
            String s = readUserInput( DBSchemaToDatastoreConfSQLMSGAccess.getMessage( "ENTERTABLES" ), false );
            map.put( "-tables", s );
        }

        if ( map.get( "-driver" ) == null ) {
            String s = readUserInput( DBSchemaToDatastoreConfSQLMSGAccess.getMessage( "ENTERDRIVER" ), false );
            map.put( "-driver", s );
        }

        if ( map.get( "-user" ) == null ) {
            if ( !"SHAPE".equals( map.get( "-driver" ) ) ) {
                String s = readUserInput( DBSchemaToDatastoreConfSQLMSGAccess.getMessage( "ENTERUSER" ), false );
                map.put( "-user", s );
            }
        }

        if ( map.get( "-password" ) == null ) {
            if ( !"SHAPE".equals( map.get( "-driver" ) ) ) {
                String s = readUserInput( DBSchemaToDatastoreConfSQLMSGAccess.getMessage( "ENTERPASSWORD" ), false );
                map.put( "-password", s );
            } else {
                map.put( "-password", " " );
            }
        }

        if ( map.get( "-url" ) == null && !"SHAPE".equalsIgnoreCase( (String) map.get( "-driver" ) ) ) {
            String s = readUserInput( DBSchemaToDatastoreConfSQLMSGAccess.getMessage( "ENTERURL" ), false );
            map.put( "-url", s );
        }
        if ( map.get( "-output" ) == null ) {
            String s = readUserInput( DBSchemaToDatastoreConfSQLMSGAccess.getMessage( "ENTEROUTPUT" ), false );
            map.put( "-output", s );
        }
    }

    private static void printHelp()
                            throws IOException {
        URL url = DBSchemaToDatastoreConf.class.getResource( "DBSchemaToDatastoreConfHelp.txt" );
        System.out.println( FileUtils.readTextFile( url ) );
    }

    /**
     * @param args
     * @throws IOException
     * @throws SAXException
     * @throws TransformerException
     * @throws Exception
     */
    public static void main( String[] args )
                            throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        for ( int i = 0; i < args.length; ) {
            String first = args[i++];
            if ( "?".equals( first ) || "-h".equals( first ) || "-help".equals( first ) ) {
                printHelp();
                System.exit( 0 );
            }
            map.put( first, args[i++] );
        }

        try {
            validate( map );
        } catch ( InvalidParameterException ipe ) {
            LOG.logError( ipe.getMessage() );
            printHelp();
            System.exit( 1 );
        }
        LOG.logDebug( "Resulting commandline arguments and their values {argument=value, ...}: " + map );
        String tmp = (String) map.get( "-tables" );
        String[] tables = StringTools.toArray( tmp, ",;|", true );
        String user = (String) map.get( "-user" );
        String pw = (String) map.get( "-password" );
        String driver = (String) map.get( "-driver" );
        String url = (String) map.get( "-url" );
        String output = (String) map.get( "-output" );
        String srs = (String) map.get( "-srs" );
        String insert = "false";
        if ( "true".equalsIgnoreCase( map.get( "-insert" ) ) ) {
            insert = "true";
        }
        String delete = "false";
        if ( "true".equalsIgnoreCase( map.get( "-delete" ) ) ) {
            delete = "true";
        }
        String update = "false";
        if ( "true".equalsIgnoreCase( map.get( "-update" ) ) ) {
            update = "true";
        }
        String idGenerator = (String) map.get( "-idGenerator" );
        String sequence = (String) map.get( "-sequence" );

        String omitFidAsProperty = "false";
        if ( "true".equalsIgnoreCase( map.get( "-omitFidAsProperty" ) ) ) {
            omitFidAsProperty = "true";
        }

        // hidden parameters to ease bulk processing, if provided, these values determine
        // primary key column name and srid for all tables
        String pkey = (String) map.get( "-pkey" );
        String sridString = (String) map.get( "-srid" );
        Integer srid = null;
        if ( sridString != null ) {
            srid = Integer.parseInt( sridString );
        }

        DBSchemaToDatastoreConf stc = new DBSchemaToDatastoreConf( tables, user, pw, driver, url, srs, pkey, srid,
                                                                   insert, update, delete, idGenerator, sequence,
                                                                   omitFidAsProperty );
        String conf = null;
        try {
            conf = stc.run();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            System.exit( 1 );
        }
        storeSchema( output, conf );
        System.exit( 0 );
    }

    /**
     * 
     * @param output
     * @param conf
     * @throws IOException
     * @throws SAXException
     * @throws TransformerException
     */
    private static void storeSchema( String output, String conf )
                            throws SAXException, IOException, TransformerException {
        if ( conf != null ) {
            XMLFragment xml = new XMLFragment();
            xml.load( new StringReader( conf ), XMLFragment.DEFAULT_URL );
            FileOutputStream fos = new FileOutputStream( output );
            xml.prettyPrint( fos );
            fos.close();
        }
    }

    /**
     * This function prints a message on the command line and asks the user for an input, returns
     * the text the User has typed, null otherwise
     * 
     * @param describtion
     *            The message to be displayed to the user asking for a certain text to type
     * @return the read text, or null if nothing was read
     * @throws IOException
     */
    private static String readUserInput( String describtion, boolean acceptNull )
                            throws IOException {

        String result = null;
        do {
            System.out.print( describtion );
            System.out.println( ':' );
            BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
            result = reader.readLine();
        } while ( !acceptNull && ( result == null || result.trim().length() == 0 ) );
        return result;

    }
}
