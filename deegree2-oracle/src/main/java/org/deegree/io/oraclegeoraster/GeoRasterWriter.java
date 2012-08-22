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

package org.deegree.io.oraclegeoraster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import oracle.jdbc.OraclePreparedStatement;
import oracle.spatial.georaster.GeoRasterAdapter;
import oracle.sql.STRUCT;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;

/**
 * This class offers one public static method for importing an image read from a file into Oracle
 * GeoRaster Database. To work correctly following assumptions are made:
 * <ul>
 * <li>a worldfile is assigend to the image file
 * <li>DLLTrigger has been set for RDT-Table
 * <li>no mosaicing and pyramid calculations will be perform by this class
 * </ul>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GeoRasterWriter {

    private static final ILogger LOG = LoggerFactory.getLogger( GeoRasterWriter.class );

    /**
     *
     * @param connection
     *            database connection
     * @param imageFile
     *            absolut path to the image file to import
     * @param worldFilename
     *            name of the images worldfile
     * @param rdtName
     *            name of the RDT table
     * @param imageTableName
     *            name of the tabvle to store the image
     * @param georColName
     *            name of the GeoRaster column in imageTableName
     * @throws Exception
     */
    public static void importRaster( Connection connection, String imageFile, String worldFilename, String rdtName,
                                     String imageTableName, String georColName )
                            throws Exception {

        try {

            connection.setAutoCommit( false );

            Statement stat = connection.createStatement();
            String sql = null;
            ResultSet resSet = null;

            sql = "select max(id) from " + imageTableName;

            resSet = stat.executeQuery( sql );
            if ( !resSet.next() ) {
                throw new SQLException( "Error initializing ID" );
            }
            int newRasterID = resSet.getInt( 1 );
            newRasterID++;
            sql = StringTools.concat( 500, "insert into ", imageTableName, "( ID, ", georColName, ") ", "values ( ",
                                      newRasterID, ", MDSYS.sdo_geor.init( '", rdtName, "' ) )" );
            LOG.logInfo( sql );
            stat.execute( sql );

            // RASTERPROPERTY
            sql = "SELECT  a." + georColName + ".rasterid FROM " + imageTableName + " a where id = " + newRasterID;
            resSet = stat.executeQuery( sql );
            LOG.logInfo( sql );
            if ( !resSet.next() ) {
                throw new SQLException( "Error initializing rasterID" );
            }
            int rasterID = resSet.getInt( 1 );

            resSet.close();
            stat.close();

            String s7 = StringTools.concat( 500, "SELECT ", georColName, " FROM ", imageTableName, " a where a.",
                                            georColName, ".rasterid = ? and a.", georColName, ".rasterdatatable = ?" );
            LOG.logInfo( s7 );

            OraclePreparedStatement oraclepreparedstatement = (OraclePreparedStatement) connection.prepareStatement( s7 );

            oraclepreparedstatement.setInt( 1, rasterID );
            oraclepreparedstatement.setString( 2, rdtName );
            ResultSet resultset = null;

            resultset = oraclepreparedstatement.executeQuery();

            if ( !resultset.next() ) {
                throw new SQLException( "No georaster object exists at rasterid = " + rasterID + ", RDT = " + rdtName );
            }

            STRUCT struct = (STRUCT) resultset.getObject( georColName.toUpperCase() );
            oracle.sql.Datum adatum[] = struct.getOracleAttributes();
            oraclepreparedstatement.close();

            if ( adatum[0] != null || adatum[1] != null || adatum[4] != null ) {

                String s9 = "delete from " + rdtName + " where rasterid = " + rasterID;
                CallableStatement callablestatement = connection.prepareCall( s9 );
                LOG.logInfo( s9 );
                callablestatement.execute();
                String s10 = StringTools.concat( 1000, "declare\ngeor SDO_GEORASTER;\nbegin\nselect ", georColName,
                                                 " into geor from ", imageTableName, " a where a.", georColName,
                                                 ".rasterid = ", rasterID, " and a.", georColName,
                                                 ".rasterdatatable = '", rdtName, "' for update;\n",
                                                 "geor := sdo_geor.init('", rdtName, "', ", rasterID, ");\n",
                                                 "update ", imageTableName, " a set ", georColName, " = geor where a.",
                                                 georColName, ".rasterid = ", rasterID, " and a.", georColName,
                                                 ".rasterdatatable = '", rdtName, "';commit;end;" );
                CallableStatement callablestatement1 = connection.prepareCall( s10 );
                LOG.logInfo( s10 );
                callablestatement1.execute();
            }

            File f = null;
            try {
                // FIXME this is not so fine: creatign 2 files, one of which is empty
                // not so bad, because they get deleted when Importer ends; fix if time
                // availab
                f = File.createTempFile( "temp_wld", "tfw" );
                f.deleteOnExit();
                saveWorldFile( f.getAbsolutePath(), createParsFromWorldfile( new FileReader( worldFilename ) ), true );

            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
            }

            STRUCT struct1 = GeoRasterAdapter.loadFromFile( "", imageFile, "WORLDFILE", f.getAbsolutePath() + ".tfw",
                                                            "blocking=true", struct, connection );

            if ( struct1 != null ) {
                // Raster_relief_imp
                String s8 = StringTools.concat( 500, "UPDATE ", imageTableName, " a SET a.", georColName,
                                                " = ? WHERE a.", georColName, ".rasterid = ? and a.", georColName,
                                                ".rasterdatatable = ?" );
                LOG.logInfo( s8 );
                OraclePreparedStatement oraclepreparedstatement1 = (OraclePreparedStatement) connection.prepareStatement( s8 );
                oraclepreparedstatement1.setObject( 1, struct1 );
                oraclepreparedstatement1.setInt( 2, rasterID );
                oraclepreparedstatement1.setString( 3, rdtName );
                oraclepreparedstatement1.execute();
                oraclepreparedstatement1.close();
            } else {
                throw new SQLException( "\nThe georaster object is not loaded correctly!!" );
            }
            if ( resultset != null ) {
                resultset.close();
            }

            connection.commit();
            LOG.logInfo( "commited" );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new RuntimeException( e );
        }
    }

    public static double[] createParsFromWorldfile( Reader reader )
                            throws Exception {
        double[] pars = null;
        DecimalFormat decimalformat = (DecimalFormat) NumberFormat.getInstance( Locale.ENGLISH );
        BufferedReader bufferedreader = null;
        try {
            bufferedreader = new BufferedReader( reader );
            double d = decimalformat.parse( bufferedreader.readLine().trim() ).doubleValue();
            double d1 = decimalformat.parse( bufferedreader.readLine().trim() ).doubleValue();
            double d2 = decimalformat.parse( bufferedreader.readLine().trim() ).doubleValue();
            double d3 = decimalformat.parse( bufferedreader.readLine().trim() ).doubleValue();
            double d4 = decimalformat.parse( bufferedreader.readLine().trim() ).doubleValue();
            double d5 = decimalformat.parse( bufferedreader.readLine().trim() ).doubleValue();

            decimalformat = (DecimalFormat) DecimalFormat.getInstance( Locale.GERMAN );
            decimalformat.setDecimalSeparatorAlwaysShown( true );
            decimalformat.setGroupingUsed( false );
            decimalformat.setMinimumFractionDigits( 1 );
            pars = new double[] { d, d1, d2, d3, d4, d5 };

        } catch ( Exception e ) {
            e.printStackTrace();
        }
        bufferedreader.close();
        return pars;
    }

    private static void saveWorldFile( String filename, double[] wldPars, boolean reformat )
                            throws IOException {

        StringBuffer sb = new StringBuffer();

        String[] _wldPars = new String[wldPars.length];

        DecimalFormat decimalformat = null;
        if ( reformat ) { // reformat numbers to german locale
            decimalformat = (DecimalFormat) NumberFormat.getInstance( Locale.GERMAN );
        } else {
            decimalformat = (DecimalFormat) NumberFormat.getInstance( Locale.ENGLISH );
        }
        decimalformat.setDecimalSeparatorAlwaysShown( true );
        decimalformat.setGroupingUsed( false );
        decimalformat.setMinimumFractionDigits( 1 );
        for ( int i = 0; i < _wldPars.length; i++ ) {
            _wldPars[i] = decimalformat.format( wldPars[i] );

        }

        sb.append( _wldPars[0] ).append( "\n" ).append( _wldPars[1] ).append( "\n" );
        sb.append( _wldPars[2] ).append( "\n" ).append( _wldPars[3] ).append( "\n" );
        sb.append( _wldPars[4] ).append( "\n" ).append( _wldPars[5] ).append( "\n" );

        File f = new File( filename + ".tfw" );
        if ( reformat ) {// reformat also menas: only need this file temporarily
            f.deleteOnExit();
        }
        FileWriter fw = new FileWriter( f );
        PrintWriter pw = new PrintWriter( fw );

        pw.print( sb.toString() );

        pw.close();
        fw.close();
    }

}
