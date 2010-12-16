//$HeadURL:svn+ssh://otonnhofer@svn.wald.intevation.org/deegree/deegree3/model/trunk/src/org/deegree/model/coverage/raster/implementation/io/WorldFileReader.java $
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
package org.deegree.coverage.raster.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.cs.CRS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representation of a ESRI world file. A world file may defines bounding coordinates centered on the outer pixel
 * (e.g. ESRI software) or outside the bounding pixels (e.g.Oracle spatial). Reading a worldfile this must be considered
 * so the type of a worldfile must be passed. For this a <code>enum</code> named <code>TYPE</code> ist defined.
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author:otonnhofer $
 * 
 * @version Modified from Revision: 7587 $ Date: 2007-06-19 11:29:12 +0200 (Tue, 19 Jun 2007) $
 * @version $Revision:10872 $, $Date:2008-04-01 15:41:48 +0200 (Tue, 01 Apr 2008) $
 */
public class WorldFileAccess {

    private static Logger log = LoggerFactory.getLogger( WorldFileAccess.class );

    private static final String[] WORLD_FILE_EXT = new String[] { "wld", "tfw", "tifw", "jgw", "jpgw", "gfw", "gifw",
                                                                 "pgw", "pngw" };

    /**
     * Returns the world file for given file.
     * 
     * @param rasterFile
     *            the raster file
     * @return the world file or null if not found.
     */
    private static File getWorldFile( File rasterFile ) {
        String basename = FileUtils.getBasename( rasterFile );
        // Look for corresponding worldfiles.
        String wldName = "";
        for ( String ext : WORLD_FILE_EXT ) {
            String tmp = basename + "." + ext;
            if ( new File( tmp ).exists() ) {
                wldName = tmp;
                break;
            }
        }
        return new File( wldName );
    }

    /**
     * @param stream
     *            the stream pointing to a world file.
     * @param options
     *            set for this stream.
     * @return a RasterReference
     * @throws IOException
     */
    public static RasterGeoReference readWorldFile( InputStream stream, RasterIOOptions options )
                            throws IOException {

        if ( stream == null ) {
            throw new IOException( "Stream is null, no world file found." );
        }

        BufferedReader br = new BufferedReader( new InputStreamReader( stream ) );

        return readRasterReference( br, "from stream", options );

    }

    /**
     * @param reader
     *            to a world file.
     * @param options
     *            set for this stream.
     * @return a RasterReference
     * @throws IOException
     */
    public static RasterGeoReference readWorldFile( Reader reader, RasterIOOptions options )
                            throws IOException {

        if ( reader == null ) {
            throw new IOException( "Reader is null, no world file found." );
        }

        BufferedReader br = ( reader instanceof BufferedReader ) ? (BufferedReader) reader
                                                                : new BufferedReader( reader );

        return readRasterReference( br, "from reader", options );

    }

    /**
     * @param br
     * @return
     * @throws IOException
     */
    private static RasterGeoReference readRasterReference( final BufferedReader br, final String filePath,
                                                           final RasterIOOptions options )
                            throws IOException {
        double[] values = new double[6];

        int i = 0;
        try {
            for ( ; i < 6; i++ ) {
                String line = br.readLine();
                if ( line == null ) {
                    throw new IOException( i + ") failure, current line could not be read from world file"
                                           + ( ( filePath != null ) ? " (" + filePath + ")" : "" ) + "." );
                }
                line = line.trim();
                double val = Double.parseDouble( line.replace( ',', '.' ) );
                values[i] = val;
            }
        } catch ( NumberFormatException e ) {
            throw new IOException( i + ") invalid line in world file"
                                   + ( ( filePath != null ) ? " (" + filePath + ")" : "" ) + "." );
        }

        // br.close();

        double resx = values[0];
        double resy = values[3];
        double xmin = values[4];
        double ymax = values[5];
        // double xmax = xmin + ( ( width - 1 ) * resx );
        // double ymin = ymax + ( ( height - 1 ) * resy );
        // if ( type == RasterReference.Type.OUTER ) {
        // xmin = xmin + resx / 2.0;
        // // ymin = ymin - resy / 2.0;
        // // xmax = xmax - resx / 2.0;
        // ymax = ymax + resy / 2.0;
        // }
        OriginLocation location = OriginLocation.CENTER;
        CRS crs = null;
        if ( options != null ) {
            location = options.getRasterOriginLocation();
            crs = options.getCRS();
        }
        return new RasterGeoReference( location, resx, resy, values[2], values[1], xmin, ymax, crs );
    }

    /**
     * @param filename
     *            the image/raster file (including path and file extension)
     * @param options
     *            set for this file.
     * @return a RasterReference
     * @throws IOException
     */
    public static RasterGeoReference readWorldFile( File filename, RasterIOOptions options )
                            throws IOException {

        File worldFile = getWorldFile( filename );
        if ( !worldFile.exists() ) {
            throw new IOException( "No world file for: " + filename );
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "read worldfile for " + filename );
        }

        BufferedReader br = new BufferedReader( new FileReader( worldFile ) );
        RasterGeoReference result = readRasterReference( br, worldFile.getAbsolutePath(), options );
        br.close();
        return result;
    }

    /**
     * writes a RasterReference into a world file (with .wld extension).
     * 
     * @param renv
     *            the envelope
     * @param file
     *            the raster file
     * @throws IOException
     */
    public static void writeWorldFile( RasterGeoReference renv, File file )
                            throws IOException {
        writeWorldFile( renv, file, "wld" );
    }

    /**
     * writes a RasterReference into a world file.
     * 
     * @param renv
     *            the envelope
     * @param file
     *            the raster file
     * @param extension
     *            the file extension for the world file (eg. 'wld', 'tfw', etc)
     * @throws IOException
     */
    public static void writeWorldFile( RasterGeoReference renv, File file, String extension )
                            throws IOException {
        File f = new File( FileUtils.getBasename( file ) + "." + extension );
        FileWriter fw = new FileWriter( f );
        PrintWriter pw = new PrintWriter( fw );
        writeWorldFile( renv, pw );
        pw.close();
        fw.close();
    }

    /**
     * writes a RasterReference into a world file.
     * 
     * @param renv
     *            the envelope
     * @param writer
     *            to write the worldfile to.
     * @throws IOException
     */
    public static void writeWorldFile( RasterGeoReference renv, PrintWriter writer )
                            throws IOException {

        writer.println( renv.getResolutionX() );
        writer.println( renv.getRotationY() );
        writer.println( renv.getRotationX() );
        writer.println( renv.getResolutionY() );
        // worldfiles are centered.
        double[] orig = renv.getOrigin( OriginLocation.CENTER );
        writer.println( orig[0] );
        writer.println( orig[1] );
    }
}
