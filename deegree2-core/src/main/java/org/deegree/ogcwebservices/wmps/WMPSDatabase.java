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

package org.deegree.ogcwebservices.wmps;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.io.DBConnectionPool;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.ogcwebservices.wmps.configuration.CacheDatabase;
import org.deegree.ogcwebservices.wmps.operation.PrintMap;
import org.deegree.ogcwebservices.wmps.operation.TextArea;
import org.deegree.ogcwebservices.wms.operation.GetMap.Layer;

/**
 * Provides database functionalities for the wmps.
 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh</a>
 *
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class WMPSDatabase {

    private static final String WMPS_REQUEST_STORAGE_TABLE = "WMPS_REQUESTS";

    private static ILogger LOG = LoggerFactory.getLogger( WMPSDatabase.class );

    private CacheDatabase cacheDatabase;

    private DBConnectionPool pool;

    /**
     * Creates a new WMPSDatabase instance.
     *
     * @param cacheDatabase
     * @throws Exception
     */
    public WMPSDatabase( CacheDatabase cacheDatabase ) throws Exception {

        this.cacheDatabase = cacheDatabase;
        this.pool = DBConnectionPool.getInstance();
    }

    /**
     * Creates a table, if no table exists. Used only for the HSQLDb
     *
     * @param connection
     * @throws SQLException
     * @throws PrintMapServiceException
     */
    private void createTable( Connection connection )
                            throws SQLException {
        /*
         * PrintMap table structure
         * id,processed,timestamp,version,layers,srs,boundingbox,center,scaledenominator,
         * transparent,bgcolor,title,copyright,legend,scaleBar,note,template,emailaddress,
         * textAreas,vendor
         */
        StringBuffer sqlCreateQuery = new StringBuffer( 500 );
        sqlCreateQuery.append( "CREATE TABLE " ).append( WMPS_REQUEST_STORAGE_TABLE ).append( " ( " );
        sqlCreateQuery.append( "id VARCHAR(50), " ).append( "processed VARCHAR(10), " );
        sqlCreateQuery.append( "timestamp BIGINT, " ).append( "version VARCHAR(10), " );
        sqlCreateQuery.append( "layers BINARY, " ).append( "srs VARCHAR(150), " );
        sqlCreateQuery.append( "boundingbox VARCHAR(100), " ).append( "center VARCHAR(50), " );
        sqlCreateQuery.append( "scaledenominator INTEGER, " ).append( "transparent BOOLEAN, " );
        sqlCreateQuery.append( "bgcolor VARCHAR(10), " ).append( "title VARCHAR(300), " );
        sqlCreateQuery.append( "copyright VARCHAR(150), " ).append( "legend BOOLEAN, " );
        sqlCreateQuery.append( "scaleBar BOOLEAN, " ).append( "note VARCHAR(800), " );
        sqlCreateQuery.append( "template VARCHAR(30), " ).append( "emailaddress VARCHAR(150), " );
        sqlCreateQuery.append( "textAreas BINARY, " ).append( "vendor BINARY, " );
        sqlCreateQuery.append( "PRIMARY KEY(id,timestamp) );" );

        String sqlTableCreation = sqlCreateQuery.toString();

        try {
            Statement statement = connection.createStatement();
            statement.execute( sqlTableCreation );
            statement.close();
        } catch ( SQLException e ) {
            if ( !e.getMessage().startsWith( "Table already" ) ) {
                LOG.logError( e.getMessage(), e );
                throw new SQLException( "Unable to create a table for the sql command '" + sqlTableCreation + "'."
                                        + e.getMessage() );
            }
        }

    }

    /**
     * Inserts data into the table. Each incomming request is stored in the db.
     *
     * @param connection
     * @param request
     * @throws IOException
     * @throws PrintMapServiceException
     * @throws IOException
     */
    public void insertData( Connection connection, PrintMap request )
                            throws PrintMapServiceException, IOException {

        /*
         * PrintMap table structure
         * id,processed,timestamp,version,layers,srs,boundingbox,center,scaledenominator,
         * transparent,bgcolor,title,copyright,legend,scaleBar,note,template,emailaddress,
         * textAreas,vendor
         */
        try {
            // hack to support DPI
            String id = request.getId();
            String version = request.getVersion();
            Layer[] layers = request.getLayers();
            String srs = request.getSRS();
            Envelope bbox = request.getBBOX();
            Point center = request.getCenter();
            int scaleDenominator = request.getScaleDenominator();
            boolean transparent = request.getTransparent();
            Color bgColor = request.getBGColor();
            String title = request.getTitle();
            String copyright = request.getCopyright();
            boolean legend = request.getLegend();
            boolean scaleBar = request.getScaleBar();
            String note = request.getNote();
            String template = request.getTemplate() + "&" + request.getDpi();
            String emailAddress = request.getEmailAddress();
            TextArea[] textAreas = request.getTextAreas();

            Map<String, String> vendorSpecificParams = request.getVendorSpecificParameters();
            if ( vendorSpecificParams == null ) {
                vendorSpecificParams = new HashMap<String, String>();
            }
            long timestamp = request.getTimestamp().getTime();
            String processed = "FALSE";

            String sql = StringTools.concat( 200, "INSERT INTO ", WMPS_REQUEST_STORAGE_TABLE,
                                             " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" );
            PreparedStatement statement = connection.prepareStatement( sql );

            /*
             * PrintMap table structure
             * id,processed,timestamp,version,layers,srs,boundingbox,center,scaledenominator,
             * transparent,bgcolor,title,copyright,legend,scaleBar,note,template,emailaddress,
             * textAreas,vendor
             */
            statement.setString( 1, id );
            statement.setString( 2, processed );
            statement.setLong( 3, timestamp );
            statement.setString( 4, version );
            statement.setBytes( 5, serialize( layers ) );
            statement.setString( 6, srs );
            if ( bbox != null ) {
                String bboxString = StringTools.concat( 200, bbox.getMin().getX(), ',', bbox.getMin().getY(), ',',
                                                        bbox.getMax().getX(), ',', bbox.getMax().getY(), ',',
                                                        bbox.getCoordinateSystem().getPrefixedName() );
                statement.setString( 7, bboxString );
            } else {
                statement.setNull( 7, Types.VARCHAR );
            }
            if ( center != null ) {
                String centerString = StringTools.concat( 200, center.getX(), ',', center.getY(), ',',
                                                          center.getCoordinateSystem().getPrefixedName() );
                statement.setString( 8, centerString );
            } else {
                statement.setNull( 8, Types.VARCHAR );
            }
            statement.setInt( 9, scaleDenominator );
            statement.setBoolean( 10, transparent );
            if ( bgColor != null ) {
                String color = convertColorToHexString( bgColor );
                statement.setString( 11, color );
            }
            statement.setString( 12, title );
            statement.setString( 13, copyright );
            statement.setBoolean( 14, legend );
            statement.setBoolean( 15, scaleBar );
            statement.setString( 16, note );
            statement.setString( 17, template );
            statement.setString( 18, emailAddress );
            statement.setBytes( 19, serialize( textAreas ) );

            if ( vendorSpecificParams != null ) {
                statement.setBytes( 20, serialize( vendorSpecificParams ) );
            }

            statement.execute();
            connection.commit();
            statement.close();

        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            throw new PrintMapServiceException( "Error inserting data into the '" + WMPS_REQUEST_STORAGE_TABLE
                                                + "' table. " + e.getMessage() );
        }

    }

    /**
     * Creates a valid db connection with properties read from the configuration file.
     *
     * @return Connection
     * @throws Exception
     */
    public Connection acquireConnection()
                            throws Exception {

        String driver = this.cacheDatabase.getDriver();
        String url = this.cacheDatabase.getUrl();
        if ( this.pool == null ) {
            this.pool = DBConnectionPool.getInstance();
        }
        Connection conn = this.pool.acquireConnection( driver, url, this.cacheDatabase.getUser(),
                                                       this.cacheDatabase.getPassword() );

        try {
            if ( driver.equals( "org.hsqldb.jdbcDriver" ) ) {
                createTable( conn );
            }
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            throw new Exception( "Unable to build a valid connection to the 'hsqldb' "
                                 + "database for the connection string '" + url + "'. " + e.getMessage() );
        }

        return conn;
    }

    /**
     * Releases the current database connection.
     *
     * @param connection
     * @throws SQLException
     */
    protected void releaseConnection( Connection connection )
                            throws SQLException {

        try {
            if ( this.pool != null ) {
                this.pool.releaseConnection( connection, this.cacheDatabase.getDriver(), this.cacheDatabase.getUrl(),
                                             this.cacheDatabase.getUser(), this.cacheDatabase.getPassword() );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new SQLException( "Error releasing the open connection. " + e.getMessage() );
        }

    }

    /**
     * Select the PrintMap request that has been in the databank for the longest time. i.e the first
     * in queue to be processed.
     *
     * @param connection
     * @return PrintMap
     * @throws PrintMapServiceException
     */
    public PrintMap selectPrintMapRequest( Connection connection )
                            throws PrintMapServiceException {

        String sql = StringTools.concat( 200, "SELECT MAX( timestamp ) FROM ", WMPS_REQUEST_STORAGE_TABLE,
                                         " WHERE processed = 'FALSE' " );
        String selectionSQL = StringTools.concat( 200, "SELECT id, timestamp FROM ", WMPS_REQUEST_STORAGE_TABLE,
                                                  " WHERE timestamp = (", sql, ");" );
        String firstInQueue = null;
        long timeStamp = -1;
        try {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery( selectionSQL );
            while ( results.next() ) {
                firstInQueue = results.getString( "id" );
                timeStamp = results.getLong( 2 );
            }
            results.close();
            statement.close();
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            throw new PrintMapServiceException( "Error retrieving data from the 'WMPSPrintMap' table for the "
                                                + "selectionSQL statement '" + selectionSQL + "'. " + e.getMessage() );
        }

        return getPrintMapRequest( connection, firstInQueue, timeStamp );

    }

    /**
     * Retrieve the PrintMap request from the DB for the id and convert the byte array back to a
     * PrintMap request instance.
     *
     * @param connection
     * @param firstInQueue
     * @param timestamp
     * @return PrintMapRequest
     * @throws PrintMapServiceException
     */
    @SuppressWarnings("unchecked")
    private PrintMap getPrintMapRequest( Connection connection, String firstInQueue, long timestamp )
                            throws PrintMapServiceException {

        PrintMap request = null;
        if ( firstInQueue == null ) {
            return request;
        }

        /*
         * PrintMap table structure
         * id,version,layers,srs,boundingBox,center,scaleDenominator,transparent,bgColor,title,copyright,
         * legend,scaleBar,note,template,emailaddress,textAreas
         */
        String selectRequest = StringTools.concat( 400, "SELECT id, version, layers, srs, boundingbox, center,",
                                                   "scaledenominator, transparent, bgcolor, title, copyright,",
                                                   "legend, scalebar, note, template, emailaddress, ",
                                                   "textAreas, vendor FROM ", WMPS_REQUEST_STORAGE_TABLE,
                                                   " WHERE id='", firstInQueue, "' ", "AND timestamp=", timestamp );

        try {
            Statement statement = connection.createStatement();

            ResultSet results = statement.executeQuery( selectRequest );

            while ( results.next() ) {
                String id = results.getString( 1 );                
                String version = results.getString( 2 );
                byte[] b = results.getBytes( 3 );
                Layer[] layers = null;
                if ( b != null ) {
                    Object object = deserialize( b );
                    if ( object != null ) {
                        layers = (Layer[]) object;
                    }
                }
                String srs = results.getString( 4 );
                String bboxString = results.getString( 5 );
                Envelope bbox = null;
                if ( bboxString != null ) {
                    String[] bboxArray = StringTools.toArray( bboxString, ",", false );
                    if ( bboxArray.length == 5 ) {
                        double minX = Double.valueOf( bboxArray[0] ).doubleValue();
                        double minY = Double.valueOf( bboxArray[1] ).doubleValue();
                        double maxX = Double.valueOf( bboxArray[2] ).doubleValue();
                        double maxY = Double.valueOf( bboxArray[3] ).doubleValue();
                        CoordinateSystem crs;
                        try {
                            crs = CRSFactory.create( bboxArray[4] );
                        } catch ( UnknownCRSException e ) {
                            throw new PrintMapServiceException( e.getMessage() );
                        }
                        bbox = GeometryFactory.createEnvelope( minX, minY, maxX, maxY, crs );
                    }
                }
                String centerString = results.getString( 6 );
                Point center = null;
                if ( centerString != null ) {
                    String[] centerArray = StringTools.toArray( centerString, ",", false );
                    if ( centerArray.length == 3 ) {
                        double x = Double.valueOf( centerArray[0] ).doubleValue();
                        double y = Double.valueOf( centerArray[1] ).doubleValue();
                        try {
                            CoordinateSystem crs = CRSFactory.create( centerArray[2] );
                            center = GeometryFactory.createPoint( x, y, crs );
                        } catch ( UnknownCRSException e ) {
                            throw new PrintMapServiceException( e.getMessage() );
                        }
                    }
                }
                /*
                 * "scaledenominator, transparent, bgcolor, title, copyright,legend, scalebar, note,
                 * template, emailaddress, textAreas, vendorspecificparams
                 */
                int scaleDenominator = results.getInt( 7 );
                boolean transparent = results.getBoolean( 8 );
                String bgColorString = results.getString( 9 );
                Color bgColor = null;
                if ( bgColorString != null ) {
                    bgColor = convertStringToColor( bgColorString );
                }
                String title = results.getString( 10 );
                String copyright = results.getString( 11 );
                boolean legend = results.getBoolean( 12 );
                boolean scaleBar = results.getBoolean( 13 );
                String note = results.getString( 14 );
                String template = results.getString( 15 );
                // hack to support DPI
                String[] tmp = StringTools.toArray( template, "&", false );
                template = tmp[0];
                int dpi = -1;
                if ( tmp.length == 2 ) {
                    dpi = Integer.parseInt( tmp[1] );
                }
                String emailAddress = results.getString( 16 );
                b = results.getBytes( 17 );
                TextArea[] textAreas = null;
                if ( b != null ) {
                    Object object = deserialize( b );
                    if ( object != null ) {
                        textAreas = (TextArea[]) object;
                    }
                }
                b = results.getBytes( 18 );
                Map<String, String> vendorSpecificParameters = (Map<String, String>) deserialize( b );

                request = PrintMap.create( id, version, layers, srs, bbox, center, scaleDenominator, transparent,
                                           bgColor, title, copyright, legend, scaleBar, note, template, emailAddress,
                                           new Timestamp( timestamp ), textAreas, dpi, vendorSpecificParameters );
            }
            statement.close();

        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            throw new PrintMapServiceException( "Error executing the sql statement '" + selectRequest + "'. "
                                                + e.getMessage() );
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            throw new PrintMapServiceException( "Error deserializing the result set. " + e.getMessage() );
        } catch ( ClassNotFoundException e ) {
            LOG.logError( e.getMessage(), e );
            throw new PrintMapServiceException( "Error deserializing the result set. " + e.getMessage() );
        }

        return request;
    }

    /**
     * Updating the processed field in the db to signify that the PrintMap request has been
     * successfully carried out.
     *
     * @param connection
     * @param id
     * @param timeStamp
     * @param state
     * @throws SQLException
     */
    public void updateDB( Connection connection, String id, Timestamp timeStamp, String state )
                            throws SQLException {

        String updateSQL = StringTools.concat( 200, "UPDATE ", WMPS_REQUEST_STORAGE_TABLE, " SET processed='", state,
                                               "' WHERE id='", id, "' AND timestamp=", timeStamp.getTime() );

        try {

            Statement statement = connection.createStatement();
            int i = statement.executeUpdate( updateSQL );
            if ( i == 0 ) {
                // TODO is this not an error?
            } else if ( i == -1 ) {
                String s = StringTools.concat( 200, "Error executing the update statement. Could not update row in ",
                                               "the DB for id='", id, "and timestamp='", timeStamp, '.' );
                throw new SQLException( s );
            }
            connection.commit();
            statement.close();

        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            String s = StringTools.concat( 200, "Error executing the update statement. Could not update row ",
                                           "in the DB for id='", id, "and timestamp='", timeStamp, ". ", e.getMessage() );
            throw new SQLException( s );
        }

    }

    /**
     * Convert the object to a byte array.
     *
     * @param object
     * @return byte[]
     * @throws IOException
     */
    private synchronized byte[] serialize( Object object )
                            throws IOException {

        byte[] b = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream( 10000 );
        try {
            ObjectOutputStream oos = new ObjectOutputStream( bos );
            oos.writeObject( object );
            oos.close();
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            throw new IOException( "Error converting the current object to an array of bytes. " + e.getMessage() );
        }
        b = bos.toByteArray();
        bos.close();

        return b;

    }

    /**
     * Reserialize the byte array to a PrintMap instance.
     *
     * @param b
     * @return Object
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private synchronized Object deserialize( byte[] b )
                            throws IOException, ClassNotFoundException {

        Object object = null;
        try {
            ByteArrayInputStream bai = new ByteArrayInputStream( b );
            ObjectInputStream in = new ObjectInputStream( bai );
            object = in.readObject();
            in.close();
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            throw new IOException( "Error opening ObjectInputStream to reserialize the byte "
                                   + "array back to the original instance. " + e.getMessage() );
        } catch ( ClassNotFoundException e ) {
            LOG.logError( e.getMessage(), e );
            throw new ClassNotFoundException( "Error recasting the ObjectInputStream "
                                              + "retrieved Object to the original instance. "
                                              + "The wrong data may have been stored in the DB "
                                              + "or the DB instance is inconsistent. " + e.getMessage() );
        }

        return object;
    }

    /**
     * Convert a "#FFFFFF" hex string to a Color. If the color specification is bad, an attempt will
     * be made to fix it up.
     *
     * @param value
     * @return Color
     */
    private Color hexToColor( String value ) {

        if ( value.startsWith( "#" ) ) {
            String digits = value.substring( 1, Math.min( value.length(), 7 ) );
            String hstr = "0x" + digits;
            return Color.decode( hstr );
        }
        return null;

    }

    /**
     * Convert a color string "RED" or "#NNNNNN" to a Color. Note: This will only convert the
     * HTML3.2 colors strings or string of length 7 otherwise, it will return Color.white.
     *
     * @param str
     * @return Color
     */
    private Color convertStringToColor( String str ) {

        if ( str != null ) {
            if ( str.charAt( 0 ) == '#' ) {
                return hexToColor( str );
            } else if ( str.equalsIgnoreCase( "Black" ) ) {
                return hexToColor( "#000000" );
            } else if ( str.equalsIgnoreCase( "Silver" ) ) {
                return hexToColor( "#C0C0C0" );
            } else if ( str.equalsIgnoreCase( "Gray" ) ) {
                return hexToColor( "#808080" );
            } else if ( str.equalsIgnoreCase( "White" ) ) {
                return hexToColor( "#FFFFFF" );
            } else if ( str.equalsIgnoreCase( "Maroon" ) ) {
                return hexToColor( "#800000" );
            } else if ( str.equalsIgnoreCase( "Red" ) ) {
                return hexToColor( "#FF0000" );
            } else if ( str.equalsIgnoreCase( "Purple" ) ) {
                return hexToColor( "#800080" );
            } else if ( str.equalsIgnoreCase( "Fuchsia" ) ) {
                return hexToColor( "#FF00FF" );
            } else if ( str.equalsIgnoreCase( "Green" ) ) {
                return hexToColor( "#008000" );
            } else if ( str.equalsIgnoreCase( "Lime" ) ) {
                return hexToColor( "#00FF00" );
            } else if ( str.equalsIgnoreCase( "Olive" ) ) {
                return hexToColor( "#808000" );
            } else if ( str.equalsIgnoreCase( "Yellow" ) ) {
                return hexToColor( "#FFFF00" );
            } else if ( str.equalsIgnoreCase( "Navy" ) ) {
                return hexToColor( "#000080" );
            } else if ( str.equalsIgnoreCase( "Blue" ) ) {
                return hexToColor( "#0000FF" );
            } else if ( str.equalsIgnoreCase( "Teal" ) ) {
                return hexToColor( "#008080" );
            } else if ( str.equalsIgnoreCase( "Aqua" ) ) {
                return hexToColor( "#00FFFF" );
            }
        }
        return null;
    }

    /**
     * convert a color to its hex string.
     *
     * @param c
     * @return String
     */
    private String convertColorToHexString( Color c ) {
        String str = Integer.toHexString( c.getRGB() & 0xFFFFFF );
        return ( "#" + "000000".substring( str.length() ) + str.toUpperCase() );
    }

}
