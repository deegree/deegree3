//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/
package org.deegree.model.coverage.raster.implementation.io;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.deegree.model.coverage.raster.RasterEnvelope;

import com.sun.media.jai.codec.FileSeekableStream;

/**
 * Class representation of a ESRI world file. A world file may defines bounding coordinates centered on the outter pixel
 * (e.g. ESRI software) or outside the bounding pixels (e.g.Oracle spatial). Reading a worldfile this must be considered
 * so the type of a worldfile must be passed. For this a <code>enum</code> named <code>TYPE</code> ist defined.
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version Modified from Revision: 7587 $ Date: 2007-06-19 11:29:12 +0200 (Tue, 19 Jun 2007) $
 * @version $Revision$, $Date$
 */
public class WorldFileReader {

    private static Log log = LogFactory.getLog( WorldFileReader.class );

    /**
     * <code>TYPE</code> enumerates the world file types.
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public enum TYPE {

        /**
         * Coordinates denote pixel centers.
         */
        CENTER,

        /**
         * Coordinates denote outer edges.
         */
        OUTER

    }

    /**
     * @return a class represention of a ESRI world file
     * @param filename
     *            name of the image/raster file inclusing path and extension
     * @param type
     * @throws IOException
     */
    public static RasterEnvelope readWorldFile( String filename, TYPE type )
                            throws IOException {

        FileSeekableStream fss = new FileSeekableStream( filename );
        RenderedOp rop = JAI.create( "stream", fss );
        int iw = ( (Integer) rop.getProperty( "image_width" ) ).intValue();
        int ih = ( (Integer) rop.getProperty( "image_height" ) ).intValue();

        return readWorldFile( filename, type, iw, ih );

    }

    /**
     * @param name
     * @return true, if the name ends with .tfw, .wld, .jgw, .gfw, .gifw, .pgw or .pngw.
     */
    public static boolean hasWorldfileSuffix( String name ) {
        String lname = name.toLowerCase();
        return lname.endsWith( ".tfw" ) || lname.endsWith( ".wld" ) || lname.endsWith( ".jgw" )
               || lname.endsWith( ".gfw" ) || lname.endsWith( ".gifw" ) || lname.endsWith( ".pgw" )
               || lname.endsWith( ".pngw" );
    }

    /**
     * @return a class represention of a ESRI world file
     * @param filename
     *            name of the image/raster file inclusing path and extension
     * @param type
     * @param width
     *            image width in pixel
     * @param height
     *            image height in pixel
     * @throws IOException
     */
    public static RasterEnvelope readWorldFile( String filename, TYPE type, int width, int height )
                            throws IOException {
        // Gets the substring beginning at the specified beginIndex (0) - the beginning index,
        // inclusive - and extends to the character at index endIndex (position of '.') - the
        // ending index, exclusive.

        int pos = filename.lastIndexOf( "." );
        String basename = filename.substring( 0, pos + 1 );

        // Look for corresponding worldfiles.
        String[] worldfileExtensions = new String[] { "wld", "tfw", "tifw", "jgw", "jpgw", "gfw", "gifw", "pgw", "pngw" };
        String fname = null;
        for ( String ext : worldfileExtensions ) {
            if ( new File( basename + ext ).exists() ) {
                fname = basename + ext;
                break;
            }
        }
        if ( fname == null ) {
            throw new IOException( "No world file for: " + filename );
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "read worldfile for " + filename );
        }

        // Reads character files.
        // The constructors of this class (FileReader) assume that the default character
        // encoding and the default byte-buffer size are appropriate.
        // The BufferedReader reads text from a character-input stream, buffering characters
        // so as to provide for the efficient reading of characters.
        BufferedReader br = new BufferedReader( new FileReader( fname ) );
        String s = null;
        int cnt = 0;
        double d1 = 0;
        double d2 = 0;
        double d3 = 0;
        double d4 = 0;
//        double d7 = 0;
//        double d8 = 0;
        while ( ( s = br.readLine() ) != null ) {
            cnt++;
            s = s.trim();
            switch ( cnt ) {
            case 1:
                // spatial resolution x direction
                d1 = Double.parseDouble( s.replace( ',', '.' ) );
                break;
            case 2:
                // rotation1
//                d7 = Double.parseDouble( s.replace( ',', '.' ) );
                break;
            case 3:
                // rotation2
//                d8 = Double.parseDouble( s.replace( ',', '.' ) );
                break;
            case 4:
                // spatial resolution y direction
                d2 = Double.parseDouble( s.replace( ',', '.' ) );
                break;
            case 5:
                // minimum x coordinate
                d3 = Double.parseDouble( s.replace( ',', '.' ) );
                break;
            case 6:
                // maximum y coordinate
                d4 = Double.parseDouble( s.replace( ',', '.' ) );
                break;
            }
        }
        br.close();

        double d5 = d3 + ( ( width - 1 ) * d1 );
        double d6 = d4 + ( ( height - 1 ) * d2 );
        double resx = d1;
        double resy = d2;
        double ymax = d4;
        double ymin = d6;
        double xmax = d5;
        double xmin = d3;

        if ( type == TYPE.OUTER ) { // changes untested
            log.debug( xmin + " " + ymin + " " + xmax + " " + ymax );
            xmin = xmin + resx / 2d;
            ymin = ymin - resy / 2d;
            xmax = xmin + resx * ( width - 1 );
            ymax = ymin + resy * ( height - 1 );
        }

        // Envelope envelope = GeometryFactoryCreator.getInstance().getGeometryFactory().
        // createEnvelope( new double[] { xmin, ymin },
        // new double[] { xmax, ymax },
        // 0.001, null );

        return new RasterEnvelope( d3, d4, resx, resy );
    }

    /**
     * returns a class represention of a ESRI world file
     * 
     * @param filename
     *            name of the image/raster file inclusing path and extension
     * @param type
     *            world file type
     * @param image
     *            image/raster the worldfile belongs too
     * @return a class represention of a ESRI world file
     * @throws IOException
     */
    public static RasterEnvelope readWorldFile( String filename, TYPE type, BufferedImage image )
                            throws IOException {

        return readWorldFile( filename, type, image.getWidth(), image.getHeight() );
    }

    /**
     * writes a WorldFile
     * 
     * @param renv
     * @param filename
     * @throws IOException
     */
    public static void writeWorldFile( RasterEnvelope renv, String filename )
                            throws IOException {

        StringBuffer sb = new StringBuffer( 200 );

        sb.append( renv.getXRes() ).append( "\n" ).append( 0.0 ).append( "\n" );
        sb.append( 0.0 ).append( "\n" ).append( renv.getYRes() ).append( "\n" );
        sb.append( renv.getX0( RasterEnvelope.Type.CENTER ) ).append( "\n" );
        sb.append( renv.getY0( RasterEnvelope.Type.CENTER ) ).append( "\n" );

        int pos = filename.lastIndexOf( "." );
        filename = filename.substring( 0, pos );

        File f = new File( filename + ".wld" );

        FileWriter fw = new FileWriter( f );
        PrintWriter pw = new PrintWriter( fw );

        pw.print( sb.toString() );

        pw.close();
        fw.close();
    }

}
