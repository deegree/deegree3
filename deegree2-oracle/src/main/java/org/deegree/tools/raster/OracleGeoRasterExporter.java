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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

import org.deegree.framework.util.FileUtils;
import org.deegree.framework.util.ImageUtils;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.oraclegeoraster.GeoRasterReader;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;

/**
 * Utitliy program to export an image from Oracle 10g GeoRaster Database
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OracleGeoRasterExporter {

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
        validate( map );

        DBConnectionPool pool = DBConnectionPool.getInstance();

        Connection con = pool.acquireConnection( map.getProperty( "-driver" ), map.getProperty( "-url" ),
                                                 map.getProperty( "-user" ), map.getProperty( "-password" ) );

        try {
            Envelope env = GeometryFactory.createEnvelope( map.getProperty( "-envelope" ), null );
            int level = Integer.parseInt( map.getProperty( "-level" ) );
            float width = 1000;
            if ( map.getProperty( "-width" ) != null ) {
                width = Float.parseFloat( map.getProperty( "-width" ) );
            }
            float height = 1000;
            if ( map.getProperty( "-height" ) != null ) {
                height = Float.parseFloat( map.getProperty( "-height" ) );
            }

            BufferedImage bi = (BufferedImage) GeoRasterReader.exportRaster(
                                                                             con,
                                                                             env,
                                                                             map.getProperty( "-rdtName" ).toUpperCase(),
                                                                             map.getProperty( "-imageTableName" ).toUpperCase(),
                                                                             map.getProperty( "-georColName" ).toUpperCase(),
                                                                             map.getProperty( "-identification" ),
                                                                             level, width, height );
            ImageUtils.saveImage( bi, map.getProperty( "-outFile" ), 1 );
        } catch ( Exception e ) {
            throw e;
        } finally {
            con.close();
            System.exit( 0 );
        }

    }

    /**
     * prints help text read from OracleGeoRasterExporterHelp.txt
     * 
     * @throws IOException
     */
    private static void printHelp()
                            throws IOException {
        InputStream is = OracleGeoRasterExporter.class.getResourceAsStream( "OracleGeoRasterExporterHelp.txt" );
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
        if ( map.getProperty( "-envelope" ) == null ) {
            System.out.println( "-envelope must be defined" );
            return false;
        }
        if ( map.getProperty( "-level" ) == null ) {
            System.out.println( "-level must be defined" );
            return false;
        }
        if ( map.getProperty( "-outFile" ) == null ) {
            System.out.println( "-outFile must be defined" );
            return false;
        }
        if ( map.getProperty( "-identification" ) == null ) {
            System.out.println( "-identification must be defined" );
            return false;
        }

        return true;

    }

}
