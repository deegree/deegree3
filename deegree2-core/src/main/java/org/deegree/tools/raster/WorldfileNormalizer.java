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

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.coverage.grid.WorldFile;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.processing.raster.converter.Image2RawData;
import org.deegree.processing.raster.converter.RawData2Image;

/**
 * <code>WorldfileNormalizer</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WorldfileNormalizer {

    private static ILogger LOG = LoggerFactory.getLogger( WorldfileNormalizer.class );

    private Config config;

    private double resx, resy, rotx, roty, minx, miny;

    // int xmin = Integer.MAX_VALUE;
    //
    // int ymin = Integer.MAX_VALUE;
    //
    // int xmax = Integer.MIN_VALUE;
    //
    // int ymax = Integer.MIN_VALUE;

    private int width, height;

    private AffineTransform transform = new AffineTransform();

    /**
     * @param config
     */
    public WorldfileNormalizer( Config config ) {
        this.config = config;
    }

    // x -> y -> interpolation -> wert
    private float getValue( Image2RawData data, double x, double y ) {
        // transform = new AffineTransform(resx, roty, minx + x*resx, rotx, resy, miny + y*resy);

        // System.out.println( transform );
        // try {
        // transform = transform.createInverse();
        // } catch ( NoninvertibleTransformException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // int srcy = (int)( ( resx * ( y - miny ) - rotx * ( x - minx ) ) / denominator );
        // int srcx = (int)( ( x - roty * srcy - minx ) / resx );
        // int srcy = (int)( ( ( y ) - rotx * ( x ) ) / denominator );
        // int srcx = (int)( ( x - roty * srcy ) );

        // double tmpx = (int) ( x * cos1 - y * sin1 );
        // double tmpy = (int) ( x * sin1 + y * cos1 );
        double[] dest = new double[2];
        transform.transform( new double[] { x, y }, 0, dest, 0, 1 );
        int srcx = (int) ( dest[0] );
        int srcy = (int) ( dest[1] );
        if ( srcx < width && srcx >= 0 && srcy >= 0 && srcy < height ) {
            return data.get( srcx, srcy );
        }

        // System.out.println( srcx + "/" + srcy );
        return 0;
    }

    // private int getTargetWidth() {
    // double hyp = width / Math.cos( rotx );
    // hyp *= hyp;
    // double res = Math.sqrt( hyp - width * width );
    // return (int) ( width + res );
    // }
    //
    // private int getTargetHeight() {
    // double hyp = height / Math.cos( rotx );
    // hyp *= hyp;
    // double res = Math.sqrt( hyp - height * height );
    // return (int) ( height + res );
    // }

    private void normalize( String file )
                            throws IOException {
        // avoid trying to read world files as images
        if ( WorldFile.hasWorldfileSuffix( file ) ) {
            return;
        }
        BufferedImage src = null;
        try {
            LOG.logInfo( "Reading " + file );
            src = ImageIO.read( new File( file ) ); // ImageUtils.loadImage( file );
            // src = ImageUtils.loadImage( file );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            LOG.logInfo( "Ignoring " + file );
            // ignore faulty images/files that are no images
            return;
        }

        LOG.logInfo( "Read " + file );

        WorldFile wf = WorldFile.readWorldFile( file, config.type, src );

        Envelope env = wf.getEnvelope();

        file = file.substring( 0, file.length() - 4 );
        resx = wf.getResx();
        resy = -wf.getResy();
        rotx = wf.getRotation1();
        roty = wf.getRotation2();
        minx = env.getMin().getX();
        miny = env.getMax().getY();
        double maxx = Double.MIN_VALUE, maxy = Double.MIN_VALUE;
        minx = Double.MIN_VALUE;
        miny = Double.MIN_VALUE;
        width = Math.abs( (int) ( wf.getEnvelope().getWidth() / resx ) ) + 1;
        height = Math.abs( (int) ( wf.getEnvelope().getHeight() / resy ) ) + 1;

        transform = new AffineTransform( resx, rotx, roty, resy, minx, miny );
        try {

            double[] d = new double[2];
            for ( int x = 0; x < width; ++x )
                for ( int y = 0; y < height; ++y ) {

                    transform.transform( new double[] { x, y }, 0, d, 0, 1 );
                    if ( d[0] > maxx )
                        maxx = d[0];

                    if ( d[1] > maxy )
                        maxy = d[1];

                    if ( d[0] < minx )
                        minx = d[0];

                    if ( d[1] < miny )
                        miny = d[1];
                }

            transform = transform.createInverse();
        } catch ( NoninvertibleTransformException e ) {
            LOG.logError( "Worldfile was filled with invalid parameters.", e );
        }

        int twidth = (int) ( ( maxx - minx ) / resx );
        int theight = (int) -( ( maxy - miny ) / resy );

        LOG.logInfo( "Target image size is " + twidth + "x" + theight );

        LOG.logInfo( "Image size is " + width + "x" + height );
        Image2RawData srcData = new Image2RawData( src );

        ColorModel model = src.getColorModel();
        SampleModel sampleModel = src.getSampleModel();

        float[][] destData = new float[theight][twidth];
        for ( int i = 0; i < theight; ++i ) {
            destData[i] = new float[twidth];
        }

        LOG.logInfo( "Transforming image." );
        for ( int x = 0; x < twidth; ++x ) {
            if ( x % 1000 == 0 ) {
                System.out.print( "\r" + x );
            }
            for ( int y = 0; y < theight; ++y ) {
                destData[destData.length - y - 1][x] = getValue( srcData, x * resx + minx, miny - y * resy );
            }
        }

        System.out.println( "\r                  " );
        LOG.logInfo( "Finished transforming image." );
        srcData = null;
        src = null;
        System.gc();
        System.gc();
        // LOG.logInfo( "Target image size is " + twidth + "x" + theight );

        LOG.logInfo( "Creating target image..." );
        BufferedImage dest = RawData2Image.rawData2Image( destData, true, model, sampleModel );
        destData = null;
        System.gc();
        System.gc();

        LOG.logInfo( "Writing target image..." );
        ImageIO.write( dest, "jpeg", new File( file + "_converted.jpg" ) );
        LOG.logInfo( "Finished writing image." );

        double minx = env.getMin().getX();
        double miny = env.getMin().getY();
        Envelope env2 = GeometryFactory.createEnvelope( minx, miny, maxx, maxy, env.getCoordinateSystem() );

        WorldFile outWF = new WorldFile( resx, resy, 0, 0, env2 );
        WorldFile.writeWorldFile( outWF, file + "_converted" );
    }

    private void normalize() {
        while ( config.files.size() > 0 ) {
            String file = config.files.poll();
            try {
                normalize( file );
            } catch ( IOException e ) {
                LOG.logWarning( "No world file named '" + file + "' could be found/read." );
                e.printStackTrace();
            }
        }
    }

    /**
     * @param args
     */
    public static void main( String[] args ) {
        if ( args.length == 0 ) {
            printUsage( null );
        }

        WorldfileNormalizer wfn = new WorldfileNormalizer( new Config( args ) );
        wfn.normalize();
    }

    /**
     * Prints usage information and exits.
     *
     * @param msg
     */
    public static void printUsage( String msg ) {
        if ( msg != null ) {
            System.out.println( msg );
            System.out.println();
        }

        System.out.println( "Usage:" );
        System.out.println( "java -cp ... " + WorldfileNormalizer.class.getCanonicalName() + " <options> <files>" );
        System.out.println();
        System.out.println( "      --help, -h:" );
        System.out.println( "           Print out this message and exit." );
        System.out.println( "      --directory, -d <dir>:" );
        System.out.println( "           Adds all worldfiles in a directory to the list of files." );
        System.out.println( "      --type, -t <type>:" );
        System.out.println( "           Set the INPUT world file type, either to 'center' or to 'outer'." );
        System.out.println( "           Note that the output world file type will always be 'center'." );
        System.out.println( "           For details on the world file types see the documentation" );
        System.out.println( "           of the RasterTreeBuilder." );
        System.out.println( "      --interpolation, -i <method>:" );
        System.out.println( "           Set the interpolation method. Can be one of 'bicubic', 'bilinear' or" );
        System.out.println( "           'nearest'. Default is nearest neighbor." );

        System.exit( 0 );
    }

    private static class Config {

        /**
         * List of files to convert.
         */
        public LinkedList<String> files = new LinkedList<String>();

        /**
         * The type of the world files.
         */
        public WorldFile.TYPE type = WorldFile.TYPE.CENTER;

        /**
         * Interpolation method, default is 'nearest'.
         */
        public String interpolation = "nearest";

        /**
         * Parses the commandline arguments. If -h or --help is contained in the array, the
         * application will exit.
         *
         * @param args
         *            cmdline arguments
         */
        public Config( String[] args ) {
            int i = 0;

            while ( i < args.length ) {
                if ( args[i].equals( "-h" ) || args[i].equals( "--help" ) ) {
                    printUsage( null );
                } else if ( args[i].equals( "--type" ) || args[i].equals( "-t" ) ) {
                    if ( args[i + 1].equalsIgnoreCase( "outer" ) ) {
                        type = WorldFile.TYPE.OUTER;
                    }
                    if ( args[i + 1].equalsIgnoreCase( "center" ) ) {
                        type = WorldFile.TYPE.CENTER;
                    }
                    i += 2;
                } else if ( args[i].equals( "--interpolation" ) || args[i].equals( "-i" ) ) {
                    String m = args[i + 1].toLowerCase();
                    if ( m.equals( "nearest" ) || m.equals( "bilinear" ) || m.equals( "biqubic" ) ) {
                        interpolation = m;
                    } else {
                        printUsage( "Unknown interpolation method: '" + m + "'" );
                    }
                    i += 2;
                } else if ( args[i].equals( "-d" ) || args[i].equals( "--directory" ) ) {
                    File dir = new File( args[i + 1] );
                    File[] fs = dir.listFiles();
                    for ( File f : fs ) {
                        String s = f.getAbsolutePath();
                        if ( s.toLowerCase().endsWith( "jpg" ) || s.toLowerCase().endsWith( "jpeg" )
                             || s.toLowerCase().endsWith( "gif" ) || s.toLowerCase().endsWith( "png" )
                             || s.toLowerCase().endsWith( "tif" ) || s.toLowerCase().endsWith( "tiff" )
                             || s.toLowerCase().endsWith( "bmp" ) ) {
                            files.add( s );
                        }
                    }
                    i += 2;
                } else {
                    files.add( args[i] );
                    ++i;
                }
            }
        }

    }

}
