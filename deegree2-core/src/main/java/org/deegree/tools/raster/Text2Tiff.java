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

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import org.deegree.datatypes.values.Interval;
import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.datatypes.values.Values;
import org.deegree.framework.util.ConvenienceFileFilter;
import org.deegree.framework.util.ImageUtils;
import org.deegree.io.quadtree.IndexException;
import org.deegree.io.quadtree.MemPointQuadtree;
import org.deegree.io.quadtree.Quadtree;
import org.deegree.model.coverage.grid.WorldFile;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.processing.raster.interpolation.DataTuple;
import org.deegree.processing.raster.interpolation.InterpolationException;
import org.deegree.processing.raster.interpolation.InverseDistanceToPower;

/**
 * This class converts geodata and special values from a simple text file format to a .tiff file
 * format. The values are written as 32 bit float values. The <code>main</code> method should be
 * used to utilise this class as a command line tool.
 *
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Text2Tiff {

    // parameters
    private int columnNumber = 0;

    private double resolution = 0;

    private String columnName = null;

    private List<String> inputFilenames = new ArrayList<String>();

    private String outputFilename = null;

    private boolean readHeader = false;

    private boolean oracle = false;

    private Envelope boundingBox = null;

    private boolean interpolate = false;

    private boolean use32Bits = false;

    // data
    private BufferedReader in;

    private Raster raster;

    private Quadtree quadtree;

    private BufferedImage image;

    private int imageWidth;

    private int imageHeight;

    private DataBuffer buffer;

    private float offset = 0;

    // interpolating options
    private double interpolatePower = 2;

    private int interpolateMinData = 5;

    private int interpolateMaxData = 20;

    private double interpolateNoValue = 0;

    private double interpolateRadiusX = 2;

    private double interpolateRadiusY = 2;

    private double interpolateRadiusIncreaseX = 0;

    private double interpolateRadiusIncreaseY = 0;

    private Values ignoreValues = null;

    /**
     * The only constructor, called usually by the main method with command line arguments.
     *
     * @param args
     */
    public Text2Tiff( String[] args ) {
        if ( args.length < 3 ) {
            printUsage( "Not enough arguments." );
        }

        parseArgs( args );

        // check for consistency
        if ( ( columnName != null ) && !readHeader ) {
            printUsage( "If a column name is given, I have to read the header!" );
        }

        if ( inputFilenames.size() == 0 ) {
            printUsage( "No input filename given." );
        }

        if ( outputFilename == null ) {
            printUsage( "No output filename given." );
        }

        if ( columnName == null && columnNumber == 0 ) {
            printUsage( "No column specified." );
        }

        if ( !readHeader ) {
            --columnNumber;
        }
    }

    private void parseArgs( String[] args ) {
        List<Interval> intervals = new ArrayList<Interval>();

        // parse options
        try {
            for ( int i = 0; i < ( args.length - 1 ); ++i ) {
                if ( args[i].equals( "--image-type" ) ) {
                    use32Bits = args[i + 1].equals( "32" );
                    ++i;
                } else if ( args[i].equals( "--offset" ) ) {
                    offset = Float.parseFloat( args[i + 1] );
                    ++i;
                } else if ( args[i].equals( "--image-width" ) ) {
                    imageWidth = Integer.parseInt( args[i + 1] );
                    ++i;
                } else if ( args[i].equals( "--image-height" ) ) {
                    imageHeight = Integer.parseInt( args[i + 1] );
                    ++i;
                } else if ( args[i].equals( "-c" ) || args[i].equals( "--column-number" ) ) {
                    columnNumber = Integer.parseInt( args[i + 1] );
                    ++i;
                } else if ( args[i].equals( "-h" ) || args[i].equals( "--no-read-header" ) ) {
                    readHeader = false;
                } else if ( args[i].equals( "-o" ) || args[i].equals( "--oracle" ) ) {
                    oracle = true;
                } else if ( args[i].equals( "+h" ) || args[i].equals( "--read-header" ) ) {
                    readHeader = true;
                } else if ( args[i].equals( "-cn" ) || args[i].equals( "--column-name" ) ) {
                    columnName = args[i + 1];
                    ++i;
                } else if ( args[i].equals( "-r" ) || args[i].equals( "--resolution" ) ) {
                    resolution = Double.parseDouble( args[i + 1] );
                    ++i;
                } else if ( args[i].equals( "-i" ) || args[i].equals( "--interpolate" ) ) {
                    interpolate = true;
                } else if ( args[i].equals( "--interpolate-power" ) ) {
                    interpolatePower = Double.parseDouble( args[i + 1] );
                    ++i;
                } else if ( args[i].equals( "--interpolate-min-data" ) ) {
                    interpolateMinData = Integer.parseInt( args[i + 1] );
                    ++i;
                } else if ( args[i].equals( "--interpolate-max-data" ) ) {
                    interpolateMaxData = Integer.parseInt( args[i + 1] );
                    ++i;
                } else if ( args[i].equals( "--interpolate-no-value" ) ) {
                    interpolateNoValue = Double.parseDouble( args[i + 1] );
                    ++i;
                } else if ( args[i].equals( "--interpolate-radius-x" ) ) {
                    interpolateRadiusX = Double.parseDouble( args[i + 1] );
                    ++i;
                } else if ( args[i].equals( "--interpolate-radius-y" ) ) {
                    interpolateRadiusY = Double.parseDouble( args[i + 1] );
                    ++i;
                } else if ( args[i].equals( "--interpolate-radius-increase-x" ) ) {
                    interpolateRadiusIncreaseX = Double.parseDouble( args[i + 1] );
                    ++i;
                } else if ( args[i].equals( "--interpolate-radius-increase-y" ) ) {
                    interpolateRadiusIncreaseY = Double.parseDouble( args[i + 1] );
                    ++i;
                } else if ( args[i].equals( "--interpolate-ignore-range" ) ) {
                    TypedLiteral min = new TypedLiteral( args[i + 1], null );
                    TypedLiteral max = new TypedLiteral( args[i + 2], null );
                    Interval interval = new Interval( min, max, null, null, null );
                    intervals.add( interval );
                    i += 2;
                } else if ( args[i].equals( "--help" ) ) {
                    printUsage( null );
                } else if ( args[i].equals( "-help" ) ) {
                    printUsage( null );
                } else {
                    File file = new File( args[i] );
                    if ( file.isDirectory() ) {
                        File[] files = file.listFiles( new ConvenienceFileFilter( false, "XYZ,TXT" ) );
                        for ( int j = 0; j < files.length; j++ ) {
                            inputFilenames.add( files[j].getAbsolutePath() );
                        }
                    } else {
                        inputFilenames.add( args[i] );
                    }
                }

            }
        } catch ( NumberFormatException nfe ) {
            printUsage( "Illegal argument, number expected." );
        }

        // get file names
        outputFilename = args[args.length - 1];
        if ( intervals.size() != 0 ) {
            ignoreValues = new Values( intervals.toArray( new Interval[intervals.size()] ), null, null );
        }
    }

    // reads the first line
    private void readHeader()
                            throws IOException {
        if ( !readHeader ) {
            return;
        }

        String s = in.readLine();

        columnNumber = 0;

        // get the right index for the column
        if ( columnName != null ) {
            StringTokenizer tok = new StringTokenizer( s );
            while ( tok.hasMoreTokens() ) {
                String t = tok.nextToken();
                if ( t.equals( columnName ) ) {
                    break;
                }
                ++columnNumber;
            }
        } else {
            --columnNumber;
        }
    }

    // reads all data into the array lists
    private ArrayList<DataTuple> readValues( String filename )
                            throws IOException, NumberFormatException {

        readHeader();

        File file = new File( filename );
        int size = (int) ( file.length() / 30 );

        ArrayList<DataTuple> values = new ArrayList<DataTuple>( size );
        BufferedReader in = new BufferedReader( new FileReader( filename ) );
        int counter = 0;
        while ( in.ready() ) {
            StringTokenizer tokenizer = new StringTokenizer( in.readLine() );
            int idx = 0;
            double x = 0;
            double y = 0;
            while ( tokenizer.hasMoreTokens() ) {
                if ( idx == 0 ) {
                    x = Double.parseDouble( tokenizer.nextToken() );
                } else if ( idx == 1 ) {
                    y = Double.parseDouble( tokenizer.nextToken() );
                } else if ( idx == columnNumber ) {
                    values.add( new DataTuple( x, y, Double.parseDouble( tokenizer.nextToken() ) ) );
                    break;
                } else
                    tokenizer.nextToken();
                ++idx;
            }
            if ( ++counter % 10000 == 0 ) {
                System.out.print( "Read " + counter / 1000 + " thousand lines.\r" );
            }
        }
        System.out.println();
        in.close();

        return values;
    }

    // calculate resolution and bbox
    private void preprocessFiles()
                            throws IOException {
        double minx = Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double maxx = Double.MIN_VALUE;
        double maxy = Double.MIN_VALUE;
        boolean calcResolution = ( resolution == 0 );
        if ( imageWidth != 0 && imageHeight != 0 ) {
            calcResolution = false;
        }

        if ( calcResolution ) {
            resolution = Double.MAX_VALUE;
        }

        for ( String filename : inputFilenames ) {
            System.out.println( "Reading file " + filename );
            ArrayList<DataTuple> values = readValues( filename );

            // Collections.sort( values );

            double[] ys = null;
            DataTuple prev = null;
            double cur;
            if ( calcResolution ) {
                ys = new double[values.size()];
                prev = values.get( 0 );
                cur = 0;
            }

            for ( int i = 0; i < values.size(); ++i ) {
                DataTuple tuple = values.get( i );

                if ( maxx < tuple.x ) {
                    maxx = tuple.x;
                }
                if ( maxy < tuple.y ) {
                    maxy = tuple.y;
                }
                if ( minx > tuple.x ) {
                    minx = tuple.x;
                }
                if ( miny > tuple.y ) {
                    miny = tuple.y;
                }

                if ( calcResolution ) {
                    cur = Math.abs( tuple.x - prev.x );
                    if ( ( cur != 0 ) && ( resolution > cur ) ) {
                        resolution = cur;
                    }
                    ys[i] = tuple.y;
                    prev = tuple;
                }
            }

            if ( calcResolution ) {
                Arrays.sort( ys );

                for ( int i = 0; i < ys.length - 1; ++i ) {
                    cur = Math.abs( ys[i] - ys[i + 1] );
                    if ( cur != 0 && cur < resolution ) {
                        resolution = cur;
                    }
                }
            }
        }

        System.out.println( "Covered area:" );
        System.out.println( minx + " - " + maxx );
        System.out.println( miny + " - " + maxy );

        boundingBox = GeometryFactory.createEnvelope( minx, miny, maxx, maxy, null );

        if ( !calcResolution && resolution == 0 ) {
            resolution = Math.abs( ( maxy - miny ) / ( imageHeight ) );
            double h = Math.abs( ( maxx - minx ) / ( imageWidth ) );
            if ( h < resolution ) {
                resolution = h;
            }
        }
        if ( imageWidth == 0 && imageHeight == 0 ) {
            imageWidth = (int) ( boundingBox.getWidth() / resolution ) + 1;
            imageHeight = (int) ( boundingBox.getHeight() / resolution ) + 1;
        }

        System.gc();

        System.out.println( "Resolution: " + resolution );

    }

    // creates the buffered image with the right size
    private void createImage() {

        ColorModel ccm;

        if ( use32Bits ) {
            image = new BufferedImage( imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB );
        } else {
            ccm = new ComponentColorModel( ColorSpace.getInstance( ColorSpace.CS_GRAY ), null, false, false,
                                           BufferedImage.OPAQUE, DataBuffer.TYPE_USHORT );
            WritableRaster wr = ccm.createCompatibleWritableRaster( imageWidth, imageHeight );

            image = new BufferedImage( ccm, wr, false, new Hashtable<Object, Object>() );
        }

        raster = image.getData();

        buffer = raster.getDataBuffer();

    }

    // calculates the index of the desired position (in regard to a DataBuffer of a Raster)
    private int calculatePosition( double x, double y ) {

        double tmp = ( x - boundingBox.getMin().getX() ) / resolution;

        double ypos = imageHeight - ( ( y - boundingBox.getMin().getY() ) / resolution ) - 1;
        return (int) Math.round( tmp + ( ypos * imageWidth ) );

    }

    // inserts all values into the image
    private void insertValue( double x, double y, double val ) {

        int pos = Math.abs( calculatePosition( x, y ) );
        if ( use32Bits ) {
            buffer.setElem( pos, Float.floatToIntBits( ( (float) val ) + offset ) );
        } else {
            buffer.setElem( pos, (int) Math.round( ( ( val + offset ) * 10 ) ) );
        }

    }

    // creates the worldfile, depends on minimum values (call after createImage)
    private void writeWorldfile()
                            throws IOException {
        if ( oracle ) {
            PrintStream out = new PrintStream( new FileOutputStream( outputFilename + ".tfw" ) );
            out.println( resolution );
            out.println( "0.0" );
            out.println( "0.0" );
            out.println( -resolution );
            if ( oracle ) {
                out.println( boundingBox.getMin().getX() - resolution / 2 );
            } else {
                out.println( boundingBox.getMin().getX() );
            }
            if ( oracle ) {
                out.println( boundingBox.getMax().getY() + resolution / 2 );
            } else {
                out.println( boundingBox.getMax().getY() );
            }
            out.println();
        } else {
            WorldFile wf = new WorldFile( resolution, -resolution, 0, 0, boundingBox );
            WorldFile.writeWorldFile( wf, outputFilename );
        }
    }

    private void buildQuadtree()
                            throws IOException, IndexException {
        for ( String filename : inputFilenames ) {
            readHeader();

            BufferedReader in = new BufferedReader( new FileReader( filename ) );
            int counter = 0;
            while ( in.ready() ) {
                StringTokenizer tokenizer = new StringTokenizer( in.readLine() );
                int idx = 0;
                double x = 0;
                double y = 0;
                while ( tokenizer.hasMoreTokens() ) {
                    if ( idx == 0 ) {
                        x = Double.parseDouble( tokenizer.nextToken() );
                    } else if ( idx == 1 ) {
                        y = Double.parseDouble( tokenizer.nextToken() );
                    } else if ( idx == columnNumber ) {
                        Point point = GeometryFactory.createPoint( x, y, null );
                        quadtree.insert( new DataTuple( x, y, Double.parseDouble( tokenizer.nextToken() ) ), point );
                        break;
                    } else
                        tokenizer.nextToken();
                    ++idx;
                }
                if ( ++counter % 10000 == 0 ) {
                    System.out.print( "Read " + counter / 1000 + " thousand lines.\r" );
                }
            }
            in.close();
            System.out.println();

        }

    }

    /**
     * This method executes all steps that are required to transform the text file into a tiff file.
     *
     */
    private void transform() {
        try {
            preprocessFiles();

            quadtree = new MemPointQuadtree( boundingBox );

            buildQuadtree();

            createImage();

            interpolate();

            image.setData( raster );

            System.out.println( "Writing output files..." );
            ImageUtils.saveImage( image, new File( outputFilename + ".tif" ), 1 );
            writeWorldfile();
            System.out.println( "Done." );
            // testOutput();
        } catch ( IOException ioe ) {
            System.out.println( "Could not read or write a file, reason:" );
            ioe.printStackTrace();
            System.exit( 0 );
        } catch ( NumberFormatException nfe ) {
            System.out.println( "A number could not be parsed correctly. Reason: " );
            nfe.printStackTrace();
            System.exit( 0 );
        } catch ( InterpolationException e ) {
            System.out.println( "Could not interpolate missing values. Reason: " );
            e.printStackTrace();
            System.exit( 0 );
        } catch ( IndexException e ) {
            System.out.println( "Could not build Quadtree. Reason: " );
            e.printStackTrace();
            System.exit( 0 );
        }
    }

    private void interpolate()
                            throws InterpolationException {

        InverseDistanceToPower interpolator = new InverseDistanceToPower( quadtree, ignoreValues, interpolateRadiusX,
                                                                          interpolateRadiusY, 0, interpolateMinData,
                                                                          interpolateMaxData, interpolateNoValue,
                                                                          interpolateRadiusIncreaseX,
                                                                          interpolateRadiusIncreaseY, interpolatePower );

        double minx = boundingBox.getMin().getX();
        double miny = boundingBox.getMin().getY();

        int count = imageWidth * imageHeight;

        int counter = 0;

        int interpolatedCounter = 0;

        for ( int xipos = 0; xipos < imageWidth; ++xipos ) {
            for ( int yipos = 0; yipos < imageHeight; ++yipos ) {
                double xpos = minx + resolution * xipos;
                double ypos = miny + resolution * yipos;

                Envelope env = GeometryFactory.createEnvelope( xpos - 0.01, ypos - 0.01, xpos + 0.01, ypos + 0.01, null );

                try {
                    List<?> list = quadtree.query( env );
                    double val = 0;
                    if ( list.size() == 0 ) {
                        if ( interpolate ) {
                            val = interpolator.calcInterpolatedValue( xpos, ypos, interpolateRadiusX,
                                                                      interpolateRadiusY );
                            ++interpolatedCounter;
                        }
                    } else {
                        val = ( (DataTuple) list.get( 0 ) ).value;
                    }

                    insertValue( xpos, ypos, val );

                } catch ( IndexException e ) {
                    throw new InterpolationException( "Could not interpolate.", e );
                }

                if ( ++counter % 1000 == 0 ) {
                    System.out.print( counter + "/" + count + "\r" );
                }
            }
        }

        System.out.println( counter + '/' + count + ", interpolated " + interpolatedCounter + " values" );
    }

    /**
     * Prints out an error message and general usage information of the tool.
     *
     * @param error
     *            an error message
     */
    public void printUsage( String error ) {
        if ( error != null ) {
            System.out.println( "Error: " + error );
            System.out.println();
        }
        System.out.println( "java Text2Tiff <options> <inputfile[s]> <outputfile>" );
        System.out.println( "Options:" );
        System.out.println();
        System.out.println( "    --help, -help:" );
        System.out.println( "              print this message" );
        System.out.println( "    --read-header, +h:" );
        System.out.println( "    --no-read-header, -h:" );
        System.out.println( "              Do/Do not read a header line in the input file. If enabled," );
        System.out.println( "              one can specify column names instead of column numbers as" );
        System.out.println( "              seen below. Default is no." );
        System.out.println( "    --column-number n, -c n:" );
        System.out.println( "              Use the column number n as input column. Must be specified" );
        System.out.println( "              if column name below is not given. Counting starts with one," );
        System.out.println( "              so '3' means actually the third column, not the fourth." );
        System.out.println( "    --column-name n, -cn n:" );
        System.out.println( "              Use the column named n as input column. Must be specified" );
        System.out.println( "              if no column number is given." );
        System.out.println( "    --oracle, -o:" );
        System.out.println( "              Write the worldfile as Oracle expects it, using the outer" );
        System.out.println( "              bounds of the bbox and not the point centers. Default is no." );
        System.out.println( "    --image-type n:" );
        System.out.println( "              n can be either 16 or 32. If n is 16, an image of type USHORT" );
        System.out.println( "              will be created, and the values will be stored as shorts," );
        System.out.println( "              multiplied by 10. If n is 32, the float values will be" );
        System.out.println( "              stored in an image of type integer, as can be seen in" );
        System.out.println( "              Java's Float.floatToIntBits() method. Default is 16." );
        System.out.println( "    --image-width n:" );
        System.out.println( "    --image-height n:" );
        System.out.println( "              If set, an image of this size will be created. If not set" );
        System.out.println( "              (default), the size will be determined by the resolution" );
        System.out.println( "              either determined automatically or set by hand." );
        System.out.println( "    --offset n:" );
        System.out.println( "              use this as offset value for the result tiff. If result tiff" );
        System.out.println( "              shall be a 16Bit tiff first offset will be added to value" );
        System.out.println( "              before it will be multiplyed by 10" );
        System.out.println( "    --resolution n, -r n:" );
        System.out.println( "              Set geo resolution to n. If omitted, the resolution will be" );
        System.out.println( "              set to the smallest value found in the input data." );
        System.out.println( "    --interpolate, i:" );
        System.out.println( "              Interpolate missing values. By default, no interpolation" );
        System.out.println( "              will be performed." );
        System.out.println( "    --interpolate-power n:" );
        System.out.println( "              Interpolate using n as power. Default is " + interpolatePower + "." );
        System.out.println( "    --interpolate-min-data n:" );
        System.out.println( "              Interpolate only in the presence of n values within the search" );
        System.out.println( "              radius. Default is " + interpolateMinData + "." );
        System.out.println( "    --interpolate-max-data n:" );
        System.out.println( "              Interpolate using a maximum of n values from within the search" );
        System.out.println( "              radius. If more values are found, the n nearest will be used." );
        System.out.println( "              Default is " + interpolateMaxData + "." );
        System.out.println( "    --interpolate-no-value n:" );
        System.out.println( "              The value to be used if insufficient data is in the search" );
        System.out.println( "              radius. See also the radius-increase options below. Default" );
        System.out.println( "              is " + interpolateNoValue + "." );
        System.out.println( "    --interpolate-radius-x n:" );
        System.out.println( "              Interpolate using a search radius of n in the x direction." );
        System.out.println( "              Default is " + interpolateRadiusX + "." );
        System.out.println( "    --interpolate-radius-y n:" );
        System.out.println( "              Interpolate using a search radius of n in the y direction." );
        System.out.println( "              Default is " + interpolateRadiusY + "." );
        System.out.println( "    --interpolate-radius-increase-x n:" );
        System.out.println( "              Automatically increase the x search radius by n if less than" );
        System.out.println( "              --i-min-data values are found. If specified and not 0, the" );
        System.out.println( "              value --i-no-value will be ignored. Default is 0." );
        System.out.println( "    --interpolate-radius-increase-y n:" );
        System.out.println( "              Automatically increase the y search radius by n if less than" );
        System.out.println( "              --i-min-data values are found. If specified and not 0, the" );
        System.out.println( "              value --i-no-value will be ignored. Default is 0." );
        System.out.println( "    --interpolate-ignore-range min max:" );
        System.out.println( "              Adds a new range of values to be ignored while interpolating." );
        System.out.println();
        System.out.println( ".tif/.tfw will be appended to the <outputfile> parameter." );
        System.out.println();

        if ( error == null ) {
            System.exit( 0 );
        } else {
            System.exit( 1 );
        }
    }

    /**
     * This method is used from the command line.
     *
     * @param args
     *            the command line arguments.
     */
    public static void main( String[] args ) {
        new Text2Tiff( args ).transform();
    }

}
