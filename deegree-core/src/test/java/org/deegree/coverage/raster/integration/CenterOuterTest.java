//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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

package org.deegree.coverage.raster.integration;

import static junit.framework.Assert.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.deegree.commons.utils.PixelCounter;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.junit.Before;
import org.slf4j.Logger;

/**
 * The <code>CenterOuterTest</code> defines raster API integration tests on a tiled raster and a sole simple raster. For
 * each retrieval envelope a centered and outer representation of the raster file(s) is tested. The values tested should
 * not be altered, they are visually tested.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public abstract class CenterOuterTest implements CompareValues {
    private static final Logger LOG = getLogger( CenterOuterTest.class );

    /** geometry factory to use */
    protected static final GeometryFactory geomFac = new GeometryFactory();

    // se to true to get the compare values.
    private static final boolean outputRasterAsArrays = false;

    /** To verify the coordinates */
    protected static final Envelope rasterEnvelope = geomFac.createEnvelope( 1000, 2000, 1030, 2030, null );

    /**
     * kind of a constructor will be called as initialization.
     * 
     * @throws IOException
     * @throws URISyntaxException
     * @throws NumberFormatException
     */
    protected abstract void buildRasters()
                            throws IOException, NumberFormatException, URISyntaxException;

    /**
     * Init the two rasters
     */
    @Before
    public void init() {
        try {
            buildRasters();
        } catch ( Exception e ) {
            Assert.fail( e.getLocalizedMessage() );
        }
    }

    /**
     * @param prefix
     * @param raster
     */
    protected void writeDebugFile( String prefix, SimpleRaster raster ) {
        assertNotNull( raster );
        // always test reading operations on the new raster
        BufferedImage image = RasterFactory.imageFromRaster( raster );
        if ( outputRasterAsArrays ) {
            BigInteger[] pixels = PixelCounter.countPixels( image );
            StringBuilder sb = new StringBuilder( "private final static BigInteger[] FP_" );
            sb.append( prefix.toUpperCase() );
            sb.append( " = new BigInteger[] { " );
            int i = 0;
            for ( BigInteger bi : pixels ) {
                sb.append( "BigInteger.valueOf( " );
                sb.append( bi );
                sb.append( "l )" );
                if ( ++i < pixels.length ) {
                    sb.append( ", " );
                }
            }
            sb.append( "};" );
            System.out.println( sb.toString() );

            if ( LOG.isDebugEnabled() ) {
                try {
                    File f = File.createTempFile( prefix, ".png" );
                    RasterFactory.saveRasterToFile( raster, f );
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
                outputRaster( raster, prefix );
            }
        }
    }

    /**
     * output the values of the given raster.
     * 
     * @param raster
     * @param name
     */
    protected void outputRaster( SimpleRaster raster, String name ) {
        StringBuilder sb = new StringBuilder( "/** values from the test: " );
        sb.append( name ).append( "*/\n" );
        sb.append( "public static final int[][] " );
        sb.append( name.toUpperCase() ).append( "RESULT = new int[][]{\n" );
        RasterData rasterData = raster.getRasterData();
        byte[] result = new byte[rasterData.getBands()];
        for ( int y = 0; y < raster.getRows(); ++y ) {
            for ( int x = 0; x < raster.getColumns(); ++x ) {
                rasterData.getPixel( x, y, result );
                sb.append( "/* Pixel (x,y): " ).append( x ).append( "," ).append( y ).append( "*/ " );
                sb.append( "new int[]{" );
                sb.append( result[0] & 0xFF ).append( "," );
                sb.append( result[1] & 0xFF ).append( "," );
                sb.append( result[2] & 0xFF ).append( "}" );
                if ( x + 1 < raster.getColumns() ) {
                    sb.append( ",\n" );
                }
            }
            if ( y + 1 < raster.getRows() ) {
                sb.append( ",\n" );
            }
        }
        sb.append( "};" );
        LOG.debug( sb.toString() );

        try {
            FileWriter fw = new FileWriter( "/tmp/" + name + ".txt" );
            fw.write( sb.toString() );
            fw.close();
        } catch ( IOException e ) {
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "(Stack) Exception occurred: " + e.getLocalizedMessage(), e );
            } else {
                LOG.error( "Exception occurred: " + e.getLocalizedMessage() );
            }
        }
        // System.out.println( sb.toString() );
    }

    /**
     * @param reference
     * @param simpleRaster
     */
    protected void testValues( int[][] reference, SimpleRaster simpleRaster ) {
        int y = 0;
        int x = 0;
        RasterData data = simpleRaster.getRasterData();
        byte[] result = new byte[data.getBands()];
        int[] actualVal = new int[result.length];
        int width = simpleRaster.getColumns();
        for ( int i = 0; i < reference.length; ++i ) {
            int[] refVal = reference[i];
            data.getPixel( x++, y, result );
            actualVal[0] = result[0] & 0xFF;
            actualVal[1] = result[1] & 0xFF;
            actualVal[2] = result[2] & 0xFF;
            Assert.assertEquals( "Wrong color for pixel (" + ( x - 1 ) + "," + y + ") band 0: ", refVal[0],
                                 actualVal[0] );
            Assert.assertEquals( "Wrong color for pixel (" + ( x - 1 ) + "," + y + ") band 1: ", refVal[1],
                                 actualVal[1] );
            Assert.assertEquals( "Wrong color for pixel (" + ( x - 1 ) + "," + y + ") band 2: ", refVal[2],
                                 actualVal[2] );
            if ( ( i + 1 ) % width == 0 ) {
                y++;
                x = 0;
            }
        }
    }

    /**
     * @param footprint
     * @param raster
     */
    public void testValues( BigInteger[] footprint, SimpleRaster raster ) {
        BufferedImage image = RasterFactory.imageFromRaster( raster );
        double similarty = PixelCounter.similarityLevel( image, footprint );
        Assert.assertEquals( 1.0, similarty, 0.0001 );

    }
}
