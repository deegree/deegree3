//$HeadURL$  
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH
 and 
 grit GmbH   
 http://www.grit.de

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
package org.deegree.io.oraclegeoraster;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import oracle.spatial.georaster.GeoRasterException;
import oracle.spatial.georaster.JGeoRaster;
import oracle.spatial.georaster.JGeoRasterMeta;
import oracle.sql.STRUCT;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.graphics.transformation.WorldToScreenTransform;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.JDBCConnection;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.InvalidParameterValueException;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * @author <a href="mailto:lipski@grit.de">Eryk Lipski</a>
 * @author last edited by: $Author$
 * 
 *         Currently only tested on Oracle 10gR2.
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class GeoRasterReader {

    private static final ILogger LOG = LoggerFactory.getLogger( GeoRasterReader.class );

    /**
     * 
     * @param grDesc
     * @param envelope
     *            requested envelope
     * @param width
     * @param height
     * @return rendered image
     * @throws SQLException
     * @throws IOException
     * @throws GeoRasterException
     * @throws Exception
     */
    public static RenderedImage exportRaster( GeoRasterDescription grDesc, Envelope envelope, float width, float height )
                            throws SQLException, IOException, GeoRasterException, Exception {

        DBConnectionPool pool = DBConnectionPool.getInstance();
        JDBCConnection jdbc = grDesc.getJdbcConnection();
        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );

        RenderedImage ri = exportRaster( con, envelope, grDesc.getRdtTable(), grDesc.getTable(), grDesc.getColumn(),
                                         grDesc.getIdentification(), grDesc.getLevel(), width, height );

        pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );

        return ri;
    }

    /**
     * 
     * @param connection
     *            connection to Oracle database
     * @param envelope
     *            requested area
     * @param rasterRDT
     *            name of the RDT-table
     * @param rasterTable
     *            name of the table containing a geo raster column
     * @param geoRasterCol
     *            name of the geoRaster column
     * @param identification
     *            SQL where clause that identifies the raster of interest
     * @param level
     *            requested resolution level
     * @param width
     * @param height
     * @return rendered image
     * @throws SQLException
     * @throws IOException
     * @throws GeoRasterException
     * @throws Exception
     */
    public static RenderedImage exportRaster( Connection connection, Envelope envelope, String rasterRDT,
                                              String rasterTable, String geoRasterCol, String identification,
                                              int level, float width, float height )
                            throws Exception {
        RenderedImage img = null;
        try {

            int rasterID = readRasterID( connection, identification, rasterTable, geoRasterCol );

            STRUCT struct = readGeoRasterMetadata( connection, rasterRDT, rasterTable, geoRasterCol, rasterID );

            int major = connection.getMetaData().getDriverMajorVersion();
            int minor = connection.getMetaData().getDriverMinorVersion();

            JGeoRaster jGeoRaster = null;
            if ( major == 10 && minor == 1 ) {
                // synthax for Oracle 10g R1
                Class<?>[] clzz = new Class[] { STRUCT.class };
                Method method = JGeoRaster.class.getMethod( "load", clzz );
                jGeoRaster = (JGeoRaster) method.invoke( null, new Object[] { struct } );
                jGeoRaster = JGeoRaster.load( struct );
            } else if ( major == 10 && minor == 2 ) {
                // synthax for Oracle 10g R2
                Class<?>[] clzz = new Class[] { STRUCT.class, Connection.class, boolean.class };
                Method method = JGeoRaster.class.getMethod( "load", clzz );
                Object[] params = new Object[] { struct, connection, false };
                jGeoRaster = (JGeoRaster) method.invoke( null, params );
            } else {
                String s = StringTools.concat( 250, "Oracle driver ", major, ".", minor,
                                               " currently not supported for using Georaster functionality. (use 10.1 or 10.2)" );
                throw new InvalidParameterValueException( s );
            }
            jGeoRaster.setViewerUse( true );
            Properties props = jGeoRaster.getProperties();

            int maxWidth = Integer.parseInt( props.getProperty( "rasterInfo/dimensionSize_column" ) );
            int maxHeight = Integer.parseInt( props.getProperty( "rasterInfo/dimensionSize_row" ) );

            JGeoRasterMeta metaObj = jGeoRaster.getMetadataObject();

            // retieve meta-information how row/cells are referenced
            String metaTyp = "CC";
            String metaTxt = jGeoRaster.getMetadataString();
            if ( metaTxt != null
                 && metaTxt.indexOf( "<modelCoordinateLocation>UPPERLEFT</modelCoordinateLocation>" ) != -1 )
                metaTyp = "UL";

            double xMin = metaObj.getX( 0, 0 );
            double xMax = metaObj.getX( maxWidth - 1, maxHeight - 1 );
            double sc = Math.pow( 2, level );
            double yMin = metaObj.getY( 0, 0 );
            double yMax = metaObj.getY( maxWidth - 1, maxHeight - 1 );

            double xDiffPx = Math.abs( ( metaObj.getX( 1, 1 ) - xMin ) / 2 );
            double yDiffPx = Math.abs( ( metaObj.getY( 1, 1 ) - yMin ) / 2 );

            // LOG.logDebug(StringTools.concat(350, "georaster-extend ",
            // xMin, " ", yMin, " - ", xMax, " ", yMax, " typ: ", metaTyp ));

            /*
             * Difference between UL and CC handling
             * 
             * CC: Extend BBOX from middle-middel-pixel values to outer extend (default) (A -> B)
             * 
             * B-----* *-----B |\ | | /| | \ | . | / | | A | . | A | | | . | | | | | | *-----* *-----* ... ... *-----*
             * *-----* | | | | | | . | | | A | . | A | | / | . | \ | |/ | | \| B-----* *-----B
             * 
             * UL: Extend BBOX from upper-left-pixel values to outer extend (C=A=B; A->B)
             * 
             * C-----* A->->-B | | | | | | . | | | | . | | | | . | | | | | | *-----* *-----* ... ... A-----* A-----* | |
             * |\ | v | . | \ | | | . | \ | v | . | \ | | | | \| B-----A *-----B
             */
            if ( "UL".equals( metaTyp ) ) {
                // koordinatenreferenzierung oben-links
                yMax -= ( yDiffPx + yDiffPx );
                xMax += ( xDiffPx + xDiffPx );
            } else {
                // koordinatenreferenzierung center-center
                xMin -= xDiffPx;
                yMin += yDiffPx;
                xMax += xDiffPx;
                yMax -= yDiffPx;
            }

            WorldToScreenTransform wld2ora = new WorldToScreenTransform( xMin, yMin, xMax, yMax, 0, 0, maxWidth - 1,
                                                                         maxHeight - 1 );

            int xMinCell = (int) Math.round( wld2ora.getDestX( envelope.getMin().getX() ) / sc );
            int xMaxCell = (int) Math.round( wld2ora.getDestX( envelope.getMax().getX() ) / sc );
            int yMaxCell = (int) Math.round( wld2ora.getDestY( envelope.getMin().getY() ) / sc );
            int yMinCell = (int) Math.round( wld2ora.getDestY( envelope.getMax().getY() ) / sc );

            if ( LOG.isDebug() ) {
                LOG.logDebug( StringTools.concat( 400, "req-env: ", envelope.getMin().getX(), " ",
                                                  envelope.getMin().getY(), " - ", envelope.getMax().getX(), " ",
                                                  envelope.getMax().getY(), " lvl: ", level, " typ: ", metaTyp,
                                                  " row/cell-env: ", xMinCell, " ", yMinCell, " - ", xMaxCell, " ",
                                                  yMaxCell, " dx/y: ", xDiffPx, "/", yDiffPx ) );
            }

            // TODO: grit: testen und entfernen
            // if ( xMinCell < 0 )
            // xMinCell = 0;
            // if ( yMinCell < 0 )
            // yMinCell = 0;
            // if ( xMaxCell < 0 )
            // xMaxCell = 0;
            // if ( yMaxCell < 0 )
            // yMaxCell = 0;

            img = jGeoRaster.getRasterImage( connection, level, xMinCell, yMinCell, xMaxCell, yMaxCell );

            /*
             * rearange returned image in new image with size of request
             */
            if ( img != null ) {
                /*
                 * 1) calculate result bbox in nativ/georaster-level resulution (m/px from oracle georaster) 2) convert
                 * to outer bbox 3) calculate result resulution (m/px from result) 4) convert from outer bbox to bbox in
                 * result resulution (middle px) 5) calculate position on result-image (px-box) 6) draw (maybe streched)
                 * returned image inside the px-box
                 */

                // 1a) returned cell values
                int rcMinX = ( xMinCell < 0 ) ? 0 : xMinCell;
                int rcMinY = ( yMinCell < 0 ) ? 0 : yMinCell;
                int rcMaxX = rcMinX + ( img.getWidth() - 1 );
                int rcMaxY = rcMinY + ( img.getHeight() - 1 );

                // 1b) cell values to world
                double rwMinX = metaObj.getX( (int) ( rcMinX * sc ), (int) ( rcMinY * sc ) );
                double rwMinY = metaObj.getY( (int) ( rcMinX * sc ), (int) ( rcMinY * sc ) );
                double rwMaxX = metaObj.getX( (int) ( rcMaxX * sc ), (int) ( rcMaxY * sc ) );
                double rwMaxY = metaObj.getY( (int) ( rcMaxX * sc ), (int) ( rcMaxY * sc ) );

                // 2) convert to edges (see UL/CC handling above)
                if ( "UL".equals( metaTyp ) ) {
                    rwMaxY -= ( yDiffPx + yDiffPx );
                    rwMaxX += ( xDiffPx + xDiffPx );
                } else {
                    rwMinX -= xDiffPx;
                    rwMinY += yDiffPx;
                    rwMaxX += xDiffPx;
                    rwMaxY -= yDiffPx;
                }

                // 3) calculate result resulution (m/px)
                double resDiffX = envelope.getWidth() / (double) width / 2.0d;
                double resDiffY = envelope.getHeight() / (double) height / 2.0d;

                // 4) convert outer bbox to middle of pixel values
                rwMinX -= resDiffX;
                rwMinY += resDiffY;
                rwMaxX += resDiffX;
                rwMaxY -= resDiffY;

                GeoTransform wld2scr = new WorldToScreenTransform( envelope.getMin().getX(), envelope.getMin().getY(),
                                                                   envelope.getMax().getX(), envelope.getMax().getY(),
                                                                   0, 0, width - 1, height - 1 );

                // 5) calculate pixel position of returned fragment
                int scMinX = (int) Math.round( wld2scr.getDestX( rwMinX ) );
                int scMinY = (int) Math.round( wld2scr.getDestY( rwMinY ) );
                int scMaxX = (int) Math.round( wld2scr.getDestX( rwMaxX ) );
                int scMaxY = (int) Math.round( wld2scr.getDestY( rwMaxY ) );

                int scWidth = ( scMaxX - scMinX ) + 1;
                int scHeight = ( scMaxY - scMinY ) + 1;

                // 6) draw returned image into result image
                BufferedImage bimg = new BufferedImage( Math.round( width ), Math.round( height ),
                                                        BufferedImage.TYPE_INT_ARGB );
                Graphics2D bg = bimg.createGraphics();

                bg.drawImage( (Image) img, scMinX, scMinY, scWidth, scHeight, null );
                bg.dispose();
                img = bimg;
            }

        } catch ( SQLException e1 ) {
            String s = StringTools.concat( 1000, e1.getMessage(), " ", rasterTable, "; ", rasterRDT, "; ",
                                           geoRasterCol, "; ", identification, "; level: ", level );
            LOG.logError( s, e1 );

            throw new RuntimeException( s );
        } catch ( Exception e ) {
            LOG.logError( "error reading georaster", e );
            throw new RuntimeException( e );
        }
        return img;
    }

    /**
     * 
     * @param connection
     * @param rasterRDT
     * @param rasterTable
     * @param geoRasterCol
     * @param rasterID
     * @return
     * @throws SQLException
     */
    private static STRUCT readGeoRasterMetadata( Connection connection, String rasterRDT, String rasterTable,
                                                 String geoRasterCol, int rasterID )
                            throws SQLException {
        PreparedStatement ps = connection.prepareStatement( "select " + geoRasterCol + " from " + rasterTable
                                                            + " a where a." + geoRasterCol + ".rasterid = " + rasterID
                                                            + " and a." + geoRasterCol + ".rasterdatatable = '"
                                                            + rasterRDT.toUpperCase() + "'" );
        ResultSet resultset = ps.executeQuery();
        if ( !resultset.next() ) {
            throw new SQLException( "No GeoRaster object exists at rasterid = " + rasterID + ", RDT = " + rasterRDT );
        }

        STRUCT struct = (STRUCT) resultset.getObject( geoRasterCol.toUpperCase() );
        resultset.close();
        return struct;
    }

    /**
     * returns the rasterID of the requested GeoRaster
     * 
     * @param connection
     * @param identification
     * @param sql
     * @return
     * @throws SQLException
     * @throws GeoRasterException
     */
    private static int readRasterID( Connection connection, String identification, String rasterTable,
                                     String geoRasterCol )
                            throws SQLException, GeoRasterException {
        String sql = "SELECT  a." + geoRasterCol + ".rasterid FROM " + rasterTable + " a where " + identification;
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery( sql );
        if ( !rs.next() ) {
            throw new GeoRasterException( "Georaster with identification = " + identification + " not found!" );
        }
        int rasterID = rs.getInt( 1 );
        stmt.close();
        rs.close();
        return rasterID;
    }
}