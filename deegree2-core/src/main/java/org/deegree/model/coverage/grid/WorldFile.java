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
package org.deegree.model.coverage.grid;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;

import com.sun.media.jai.codec.FileSeekableStream;

/**
 * class representation of a ESRI world file. A world file may defines bounding coordinates centered on the outter pixel
 * (e.g. ESRI software) or outside the bounding pixels (e.g.Oracle spatial). Reading a worldfile this must be considered
 * so the type of a worldfile must be passed. For this a <code>enum</code> named <code>TYPE</code> ist defined.
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WorldFile {

    private static ILogger LOG = LoggerFactory.getLogger( WorldFile.class );

    private double resx;

    private double resy;

    private double rotation1;

    private double rotation2;

    private Envelope envelope;

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
    public static WorldFile readWorldFile( String filename, TYPE type )
                            throws IOException {

        FileSeekableStream fss = new FileSeekableStream( filename );
        RenderedOp rop = JAI.create( "stream", fss );
        int iw = ( (Integer) rop.getProperty( "image_width" ) ).intValue();
        int ih = ( (Integer) rop.getProperty( "image_height" ) ).intValue();

        fss.close();

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
     * @return a class representation of a ESRI world file
     * @param is
     * @param type
     * @param width
     *            image width in pixel
     * @param height
     *            image height in pixel
     * @throws IOException
     * @throws NumberFormatException
     */
    public static WorldFile readWorldFile( InputStream is, TYPE type, int width, int height )
                            throws NumberFormatException, IOException {
        BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
        String s = null;
        int cnt = 0;
        double d1 = 0;
        double d2 = 0;
        double d3 = 0;
        double d4 = 0;
        double d7 = 0;
        double d8 = 0;
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
                d7 = Double.parseDouble( s.replace( ',', '.' ) );
                break;
            case 3:
                // rotation2
                d8 = Double.parseDouble( s.replace( ',', '.' ) );
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
        double resx = Math.abs( d1 );
        double resy = Math.abs( d2 );
        double ymax = d4;
        double ymin = d6;
        double xmax = d5;
        double xmin = d3;

        if ( type == TYPE.OUTER ) {
            LOG.logDebug( xmin + " " + ymin + " " + xmax + " " + ymax );
            xmin = xmin + resx / 2d;
            ymin = ymin - resy / 2d;
            xmax = xmin + resx * ( width - 1 );
            ymax = ymin + resy * ( height - 1 );
        }

        Envelope envelope = GeometryFactory.createEnvelope( xmin, ymin, xmax, ymax, null );

        return new WorldFile( resx, resy, d7, d8, envelope );
    }

    /**
     * @return a class representation of a ESRI world file
     * @param filename
     *            name of the image/raster file inclusing path and extension
     * @param type
     * @param width
     *            image width in pixel
     * @param height
     *            image height in pixel
     * @throws IOException
     */
    public static WorldFile readWorldFile( String filename, TYPE type, int width, int height )
                            throws IOException {
        // Gets the substring beginning at the specified beginIndex (0) - the beginning index,
        // inclusive - and extends to the character at index endIndex (position of '.') - the
        // ending index, exclusive.

        String fname = null;
        int pos = filename.lastIndexOf( "." );
        filename = filename.substring( 0, pos );

        // Look for corresponding world files.
        if ( ( new File( filename + ".tfw" ) ).exists() ) {
            fname = filename + ".tfw";
        } else if ( ( new File( filename + ".wld" ) ).exists() ) {
            fname = filename + ".wld";
        } else if ( ( new File( filename + ".jgw" ) ).exists() ) {
            fname = filename + ".jgw";
        } else if ( ( new File( filename + ".jpgw" ) ).exists() ) {
            fname = filename + ".jpgw";
        } else if ( ( new File( filename + ".gfw" ) ).exists() ) {
            fname = filename + ".gfw";
        } else if ( ( new File( filename + ".gifw" ) ).exists() ) {
            fname = filename + ".gifw";
        } else if ( ( new File( filename + ".pgw" ) ).exists() ) {
            fname = filename + ".pgw";
        } else if ( ( new File( filename + ".pngw" ) ).exists() ) {
            fname = filename + ".pngw";
        } else {
            throw new IOException( "Not a world file for: " + filename );
        }

        // Reads character files.
        // The constructors of this class (FileReader) assume that the default character
        // encoding and the default byte-buffer size are appropriate.
        // The BufferedReader reads text from a character-input stream, buffering characters
        // so as to provide for the efficient reading of characters.
        return readWorldFile( new FileInputStream( fname ), type, width, height );

    }

    /**
     * returns a class representation of a ESRI world file
     *
     * @param filename
     *            name of the image/raster file including path and extension
     * @param type
     *            world file type
     * @param image
     *            image/raster the world file belongs too
     * @return a class representation of a ESRI world file
     * @throws IOException
     */
    public static WorldFile readWorldFile( String filename, TYPE type, BufferedImage image )
                            throws IOException {

        return readWorldFile( filename, type, image.getWidth(), image.getHeight() );
    }

    /**
     * writes a WorldFile
     *
     * @param wf
     * @param fileBaseName
     * @throws IOException
     */
    public static void writeWorldFile( WorldFile wf, String fileBaseName )
                            throws IOException {

        Envelope env = wf.envelope;

        StringBuffer sb = new StringBuffer( 200 );

        sb.append( wf.resx ).append( "\n" ).append( 0.0 ).append( "\n" ).append( 0.0 );
        sb.append( "\n" ).append( ( -1 ) * wf.resy ).append( "\n" ).append( env.getMin().getX() );
        sb.append( "\n" ).append( env.getMax().getY() ).append( "\n" );

        File f = new File( fileBaseName + ".wld" );

        FileWriter fw = new FileWriter( f );
        PrintWriter pw = new PrintWriter( fw );

        pw.print( sb.toString() );

        pw.close();
        fw.close();
    }

    /**
     * writes a WorldFile
     *
     * @param os
     * @param wf
     * @throws IOException
     */
    public static void writeWorldFile( OutputStream os, WorldFile wf )
                            throws IOException {

        Envelope env = wf.envelope;

        StringBuffer sb = new StringBuffer( 200 );

        sb.append( wf.resx ).append( "\n" ).append( 0.0 ).append( "\n" ).append( 0.0 );
        sb.append( "\n" ).append( ( -1 ) * wf.resy ).append( "\n" ).append( env.getMin().getX() );
        sb.append( "\n" ).append( env.getMax().getY() ).append( "\n" );

        PrintWriter pw = new PrintWriter( os );
        pw.print( sb.toString() );
        pw.close();
    }

    /**
     * Create a new WorldFile with an envelope that spans from the center of the corner pixels.
     *
     * @param resx
     *            resolution x-direction
     * @param resy
     *            resolution y-direction (negative value)
     * @param rotation1
     *            first rotation parameter
     * @param rotation2
     *            second rotation parameter
     * @param envelope
     *            the envelope of the worldfile
     */
    public WorldFile( double resx, double resy, double rotation1, double rotation2, Envelope envelope ) {
        this.resx = resx;
        this.resy = resy;
        this.rotation1 = rotation1;
        this.rotation2 = rotation2;
        this.envelope = envelope;
    }

    /**
     * Create a new WorldFile with an envelope.
     *
     * @param resx
     *            resolution x-direction
     * @param resy
     *            resolution y-direction (negative value)
     * @param rotation1
     *            first rotation parameter
     * @param rotation2
     *            second rotation parameter
     * @param envelope
     *            the envelope of the worldfile
     * @param type
     *            whether the envelope spans from the center or from the outer bounds of the corner pixels
     */
    public WorldFile( double resx, double resy, double rotation1, double rotation2, Envelope envelope, TYPE type ) {
        this.resx = resx;
        this.resy = resy;
        this.rotation1 = rotation1;
        this.rotation2 = rotation2;
        if ( type == TYPE.CENTER ) {
            this.envelope = envelope;
        } else { // convert to internal TYPE.CENTER format
            this.envelope = GeometryFactory.createEnvelope( envelope.getMin().getX() + resx / 2,
                                                            envelope.getMin().getY() + resy / 2,
                                                            envelope.getMax().getX() - resx / 2,
                                                            envelope.getMax().getY() - resy / 2,
                                                            envelope.getCoordinateSystem() );
        }
    }

    /**
     * returns the envelope described by a word file. The envelope spans the center coordinates of the corner pixels.
     *
     * @return the envelope described by a word file
     */
    public Envelope getEnvelope() {
        return envelope;
    }

    /**
     * returns the envelope described by a word file
     *
     * @param envType
     *            whether the result envelope should span from the center or from the outer bounds of the corner pixels
     * @return the envelope described by a word file
     */
    public Envelope getEnvelope( TYPE envType ) {
        if ( envType == TYPE.CENTER ) {
            return envelope;
        }
        // convert from internal TYPE.CENTER format to TYPE.OUTER
        return GeometryFactory.createEnvelope( envelope.getMin().getX() - resx / 2,
                                               envelope.getMin().getY() - resy / 2,
                                               envelope.getMax().getX() + resx / 2,
                                               envelope.getMax().getY() + resy / 2, envelope.getCoordinateSystem() );
    }

    /**
     * returns the x-resolution described by a word file
     *
     * @return the x-resolution described by a word file
     */
    public double getResx() {
        return resx;
    }

    /**
     * returns the y-resolution described by a word file
     *
     * @return the y-resolution described by a word file
     */
    public double getResy() {
        return resy;
    }

    /**
     * returns the first rotation described by a word file
     *
     * @return the first rotation described by a word file
     */
    public double getRotation1() {
        return rotation1;
    }

    /**
     * returns the second rotation described by a word file
     *
     * @return the second rotation described by a word file
     */
    public double getRotation2() {
        return rotation2;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer( 200 );
        sb.append( "envelope: " ).append( envelope ).append( "\n" );
        sb.append( "resx: " ).append( resx ).append( "\n" );
        sb.append( "resy: " ).append( resy ).append( "\n" );
        sb.append( "rotation1: " ).append( rotation1 ).append( "\n" );
        sb.append( "rotation2: " ).append( rotation2 );
        return sb.toString();
    }

}
