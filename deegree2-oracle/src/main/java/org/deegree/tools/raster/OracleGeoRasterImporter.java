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

package org.deegree.tools.raster;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

import org.deegree.framework.util.FileUtils;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.oraclegeoraster.GeoRasterWriter;

/**
 * Utitliy program to import a georeferenced image (worldfile must exist) into Oracle 10g GeoRaster
 * Database
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class OracleGeoRasterImporter {

    /**
     * @param args
     * @throws Exception
     */
    public static void main( String[] args )
                            throws Exception {

        Properties map = new Properties();
        for ( int i = 0; i < args.length; i += 2 ) {
            if ( args[i].equals( "-h" ) || args[i].equals( "-?" ) ) {
                printHelp();
                System.exit( 0 );
            }
            map.put( args[i], args[i + 1] );
        }

        if ( !validate( map ) ) {
            System.exit( 1 );
        }

        DBConnectionPool pool = DBConnectionPool.getInstance();

        Connection con = pool.acquireConnection( map.getProperty( "-driver" ), map.getProperty( "-url" ),
                                                 map.getProperty( "-user" ), map.getProperty( "-password" ) );

        try {
            GeoRasterWriter.importRaster( con, map.getProperty( "-imageFileName" ),
                                          map.getProperty( "-worldFileName" ),
                                          map.getProperty( "-rdtName" ).toUpperCase(),
                                          map.getProperty( "-imageTableName" ).toUpperCase(),
                                          map.getProperty( "-georColName" ).toUpperCase() );
        } catch ( Exception e ) {
            throw e;
        } finally {
            con.close();
            System.exit( 0 );
        }

    }

    /**
     * prints help text read from OracleGeoRasterImporterHelp.txt
     *
     * @throws IOException
     */
    private static void printHelp()
                            throws IOException {
        InputStream is = OracleGeoRasterImporter.class.getResourceAsStream( "OracleGeoRasterImporterHelp.txt" );
        String s = FileUtils.readTextFile( is ).toString();
        System.out.println( s );
    }

    /**
     * validates that all required parameters has been set
     *
     * @param map
     * @return true if valid
     */
    private static boolean validate( Properties map ) {
        if ( map.getProperty( "-driver" ) == null ) {
            System.out.println( "-driver must be defined" );
            return false;
        }
        if ( map.getProperty( "-url" ) == null ) {
            System.out.println( "-url must be defined" );
            return false;
        }
        if ( map.getProperty( "-user" ) == null ) {
            map.put( "-user", "" );
        }
        if ( map.getProperty( "-password" ) == null ) {
            map.put( "-password", "" );
        }
        if ( map.getProperty( "-imageFileName" ) == null ) {
            System.out.println( "-imageFileName must be defined" );
            return false;
        }
        if ( map.getProperty( "-worldFileName" ) == null ) {
            System.out.println( "-worldFileName must be defined" );
            return false;
        }
        if ( map.getProperty( "-rdtName" ) == null ) {
            System.out.println( "-rdtName must be defined" );
            return false;
        }
        if ( map.getProperty( "-imageTableName" ) == null ) {
            System.out.println( "-imageTableName must be defined" );
            return false;
        }
        if ( map.getProperty( "-georColName" ) == null ) {
            System.out.println( "-georColName must be defined" );
            return false;
        }
        return true;
    }

}
