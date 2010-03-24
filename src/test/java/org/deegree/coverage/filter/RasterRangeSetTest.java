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

package org.deegree.coverage.filter;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.deegree.coverage.filter.raster.RasterFilter;
import org.deegree.coverage.rangeset.AxisSubset;
import org.deegree.coverage.rangeset.Interval;
import org.deegree.coverage.rangeset.RangeSet;
import org.deegree.coverage.rangeset.RangeSetBuilder;
import org.deegree.coverage.rangeset.SingleValue;
import org.deegree.coverage.rangeset.ValueType;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.junit.Test;

/**
 * Tests filtering on rasters.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class RasterRangeSetTest {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( RasterRangeSetTest.class );

    private GeometryFactory geomFac = new GeometryFactory();

    private int width = 300;

    private int height = 200;

    Envelope env = geomFac.createEnvelope( 0, 0, 3000, 2000, null );

    RasterGeoReference rasterReference = new RasterGeoReference( OriginLocation.OUTER, 10, -10, 0, 0 );

    int[] colors = new int[] { 255, 230, 205, 180, 155, 130, 105, 80, 55, 30 };

    /**
     * @return
     * @throws IOException
     */
    private BufferedImage createBufferedImage() {
        BufferedImage im = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
        int heightStep = 20;
        int index = 0;
        int color = colors[index++];
        int c = color;
        c <<= 8;
        c |= color;
        c <<= 8;
        c |= color;
        // create a color ramp for each band.
        for ( int y = 0; y < height; ++y ) {
            if ( y != 0 && y % heightStep == 0 ) {
                color = colors[index++];
                c = color;
                c <<= 8;
                c |= color;
                c <<= 8;
                c |= color;
            }

            for ( int x = 0; x < width; ++x ) {
                im.setRGB( x, y, c );
            }
        }
        return im;
    }

    private synchronized SimpleRaster getRaster() {
        BufferedImage image = createBufferedImage();
        AbstractRaster raster = RasterFactory.createRasterFromImage( image, env, OriginLocation.OUTER );
        SimpleRaster createdRaster = raster.getAsSimpleRaster();
        RasterData rasterData = createdRaster.getRasterData();
        rasterData.setNoDataValue( new byte[] { (byte) 1, (byte) 2, (byte) 3 } );
        return createdRaster;
    }

    /**
     * tests if two of three band can be selected
     * 
     * @throws IOException
     */
    @Test
    public void threeBandTest()
                            throws IOException {

        SimpleRaster raster = getRaster();
        RangeSet bandRaster = RangeSetBuilder.createBandRangeSetFromRaster( "any", "kind", raster );

        RasterFilter filter = new RasterFilter( raster );

        List<AxisSubset> target = new ArrayList<AxisSubset>();
        AxisSubset red = new AxisSubset( "red", "yes", null, null );
        target.add( red );
        AxisSubset blue = new AxisSubset( "blue", "blue", null, null );
        target.add( blue );
        AxisSubset green = new AxisSubset( "green", "blue", null, null );
        target.add( green );
        RangeSet rs = new RangeSet( "target", "target", target, null );

        AbstractRaster subset = filter.apply( bandRaster, rs );
        byte[] nullPixel = subset.getAsSimpleRaster().getRasterData().getNullPixel( null );
        Assert.assertEquals( 3, nullPixel.length );
        int[] nullValues = new int[3];
        nullValues[0] = nullPixel[0] & 0xFF;
        nullValues[1] = nullPixel[1] & 0xFF;// green should be 0.
        nullValues[2] = nullPixel[2] & 0xFF;
        if ( LOG.isDebugEnabled() ) {
            RasterFactory.saveRasterToFile( subset, File.createTempFile( "red_blue_green", ".png" ) );
        }
        testDefaults( raster );
        BufferedImage newImage = RasterFactory.imageFromRaster( subset );
        testBandValues( newImage, -1, -1, new int[0], new int[0], new int[0], nullValues );
    }

    /**
     * tests if two of three band can be selected
     * 
     * @throws IOException
     */
    @Test
    public void twoBandTest()
                            throws IOException {

        SimpleRaster raster = getRaster();
        RangeSet bandRaster = RangeSetBuilder.createBandRangeSetFromRaster( "any", "kind", raster );

        RasterFilter filter = new RasterFilter( raster );

        List<AxisSubset> target = new ArrayList<AxisSubset>();
        AxisSubset red = new AxisSubset( "red", "yes", null, null );
        target.add( red );
        AxisSubset blue = new AxisSubset( "blue", "blue", null, null );
        target.add( blue );
        RangeSet rs = new RangeSet( "target", "target", target, null );

        AbstractRaster subset = filter.apply( bandRaster, rs );
        byte[] nullPixel = subset.getAsSimpleRaster().getRasterData().getNullPixel( null );
        Assert.assertEquals( 2, nullPixel.length );
        int[] nullValues = new int[3];
        nullValues[0] = nullPixel[0] & 0xFF;
        nullValues[1] = 0;// green should be 0.
        nullValues[2] = nullPixel[1] & 0xFF;
        if ( LOG.isDebugEnabled() ) {
            RasterFactory.saveRasterToFile( subset, File.createTempFile( "red_blue", ".png" ) );
        }
        testDefaults( raster );
        BufferedImage newImage = RasterFactory.imageFromRaster( subset );
        testBandValues( newImage, -1, -1, new int[0], null, new int[0], nullValues );
    }

    /**
     * tests if two of three band can be selected
     * 
     * @throws IOException
     */
    @Test
    public void oneBandTest()
                            throws IOException {
        SimpleRaster raster = getRaster();

        RangeSet bandRaster = RangeSetBuilder.createBandRangeSetFromRaster( "any", "kind", raster );

        RasterFilter filter = new RasterFilter( raster );

        List<AxisSubset> target = new ArrayList<AxisSubset>();
        AxisSubset green = new AxisSubset( "green", "yes", null, null );
        target.add( green );
        RangeSet rs = new RangeSet( "target", "target", target, null );

        AbstractRaster subset = filter.apply( bandRaster, rs );
        byte[] nullPixel = subset.getAsSimpleRaster().getRasterData().getNullPixel( null );
        Assert.assertEquals( 1, nullPixel.length );
        int nullValue = nullPixel[0] & 0xFF;
        if ( LOG.isDebugEnabled() ) {
            RasterFactory.saveRasterToFile( subset, File.createTempFile( "green_", ".png" ) );
        }
        testDefaults( raster );
        BufferedImage newImage = RasterFactory.imageFromRaster( subset );
        testOneBandValues( newImage, -1, -1, new int[0], nullValue );
    }

    /**
     * tests if two of three band can be selected
     * 
     * @throws IOException
     */
    @Test
    public void oneBandSingleValuesTest()
                            throws IOException {
        SimpleRaster raster = getRaster();

        RangeSet bandRaster = RangeSetBuilder.createBandRangeSetFromRaster( "any", "kind", raster );

        RasterFilter filter = new RasterFilter( raster );

        List<AxisSubset> target = new ArrayList<AxisSubset>();

        List<SingleValue<?>> sv = new ArrayList<SingleValue<?>>( 2 );
        sv.add( SingleValue.createFromValue( ValueType.Byte.name(), this.colors[2] ) );
        sv.add( SingleValue.createFromValue( ValueType.Byte.name(), this.colors[4] ) );
        AxisSubset green = new AxisSubset( "green", "yes", null, sv );
        target.add( green );

        RangeSet rs = new RangeSet( "target", "target", target, null );

        AbstractRaster subset = filter.apply( bandRaster, rs );
        byte[] nullPixel = subset.getAsSimpleRaster().getRasterData().getNullPixel( null );
        Assert.assertEquals( 1, nullPixel.length );
        int nullValue = nullPixel[0] & 0xFF;

        if ( LOG.isDebugEnabled() ) {
            RasterFactory.saveRasterToFile( subset, File.createTempFile( "green_singles_", ".png" ) );
        }
        testDefaults( raster );
        BufferedImage newImage = RasterFactory.imageFromRaster( subset );
        testOneBandValues( newImage, -1, -1, new int[] { colors[2], colors[4] }, nullValue );
    }

    /**
     * tests if two of three band can be selected
     * 
     * @throws IOException
     */
    @Test
    public void oneBandIntervalTest()
                            throws IOException {
        SimpleRaster raster = getRaster();

        RangeSet bandRaster = RangeSetBuilder.createBandRangeSetFromRaster( "any", "kind", raster );

        RasterFilter filter = new RasterFilter( raster );

        List<AxisSubset> target = new ArrayList<AxisSubset>();

        List<Interval<?, ?>> intervals = new ArrayList<Interval<?, ?>>( 2 );
        SingleValue<?> spacing = SingleValue.createFromString( ValueType.Integer.name(), "1" );
        Interval<?, ?> inter = Interval.createFromStrings( "Byte", Short.toString( (short) this.colors[4] ),
                                                           Short.toString( (short) this.colors[2] ),
                                                           org.deegree.coverage.rangeset.Interval.Closure.open, null,
                                                           false, spacing );
        intervals.add( inter );
        inter = Interval.createFromStrings( "Byte", Short.toString( (short) this.colors[8] ),
                                            Short.toString( (short) this.colors[6] ),
                                            org.deegree.coverage.rangeset.Interval.Closure.open, null, false, spacing );
        intervals.add( inter );
        AxisSubset red = new AxisSubset( "red", "yes", intervals, null );
        target.add( red );

        RangeSet rs = new RangeSet( "target", "target", target, null );

        AbstractRaster subset = filter.apply( bandRaster, rs );
        byte[] nullPixel = subset.getAsSimpleRaster().getRasterData().getNullPixel( null );
        Assert.assertEquals( 1, nullPixel.length );
        int nullValue = nullPixel[0] & 0xFF;
        if ( LOG.isDebugEnabled() ) {
            RasterFactory.saveRasterToFile( subset, File.createTempFile( "red_interval_", ".png" ) );
        }
        testDefaults( raster );
        BufferedImage newImage = RasterFactory.imageFromRaster( subset );
        testOneBandValues( newImage, -1, -1, new int[] { colors[2], colors[3], colors[4], colors[6], colors[7],
                                                        colors[8] }, nullValue );
    }

    /**
     * tests if two of three band can be selected
     * 
     * @throws IOException
     */
    @Test
    public void oneBandIntervalSinglesTest()
                            throws IOException {
        SimpleRaster raster = getRaster();

        RangeSet bandRaster = RangeSetBuilder.createBandRangeSetFromRaster( "any", "kind", raster );

        RasterFilter filter = new RasterFilter( raster );

        List<AxisSubset> target = new ArrayList<AxisSubset>();

        List<Interval<?, ?>> intervals = new ArrayList<Interval<?, ?>>( 2 );
        SingleValue<?> spacing = SingleValue.createFromString( ValueType.Integer.name(), "1" );
        Interval<?, ?> inter = Interval.createFromStrings( "Byte", Short.toString( (short) this.colors[3] ),
                                                           Short.toString( (short) this.colors[1] ),
                                                           org.deegree.coverage.rangeset.Interval.Closure.open, null,
                                                           false, spacing );
        intervals.add( inter );
        inter = Interval.createFromStrings( "Byte", Short.toString( (short) this.colors[7] ),
                                            Short.toString( (short) this.colors[6] ),
                                            org.deegree.coverage.rangeset.Interval.Closure.open, null, false, spacing );
        intervals.add( inter );

        List<SingleValue<?>> sv = new ArrayList<SingleValue<?>>( 2 );
        sv.add( SingleValue.createFromValue( ValueType.Byte.name(), this.colors[5] ) );
        sv.add( SingleValue.createFromValue( ValueType.Byte.name(), this.colors[9] ) );

        AxisSubset red = new AxisSubset( "blue", "yes", intervals, sv );
        target.add( red );

        RangeSet rs = new RangeSet( "target", "target", target, null );

        AbstractRaster subset = filter.apply( bandRaster, rs );
        byte[] nullPixel = subset.getAsSimpleRaster().getRasterData().getNullPixel( null );
        Assert.assertEquals( 1, nullPixel.length );
        int nullValue = nullPixel[0] & 0xFF;
        if ( LOG.isDebugEnabled() ) {
            RasterFactory.saveRasterToFile( subset, File.createTempFile( "blue_interval_single", ".png" ) );
        }
        testDefaults( raster );
        BufferedImage newImage = RasterFactory.imageFromRaster( subset );
        testOneBandValues( newImage, -1, -1, new int[] { colors[1], colors[2], colors[3], colors[5], colors[6],
                                                        colors[7], colors[9] }, nullValue );
    }

    /**
     * tests if two of three band can be selected
     * 
     * @throws IOException
     */
    @Test
    public void twoBandIntervalSinglesTest()
                            throws IOException {
        SimpleRaster raster = getRaster();

        RangeSet bandRaster = RangeSetBuilder.createBandRangeSetFromRaster( "any", "kind", raster );

        RasterFilter filter = new RasterFilter( raster );

        List<AxisSubset> target = new ArrayList<AxisSubset>();

        List<Interval<?, ?>> intervals = new ArrayList<Interval<?, ?>>( 2 );
        SingleValue<?> spacing = SingleValue.createFromString( ValueType.Integer.name(), "1" );
        Interval<?, ?> inter = Interval.createFromStrings( "Byte", Short.toString( (short) this.colors[2] ),
                                                           Short.toString( (short) this.colors[0] ),
                                                           org.deegree.coverage.rangeset.Interval.Closure.open, null,
                                                           false, spacing );
        intervals.add( inter );
        inter = Interval.createFromStrings( "Byte", Short.toString( (short) this.colors[7] ),
                                            Short.toString( (short) this.colors[6] ),
                                            org.deegree.coverage.rangeset.Interval.Closure.open, null, false, spacing );
        intervals.add( inter );

        // blue has intervals
        AxisSubset blue = new AxisSubset( "blue", "yes", intervals, null );
        target.add( blue );

        List<SingleValue<?>> sv = new ArrayList<SingleValue<?>>( 2 );
        sv.add( SingleValue.createFromValue( ValueType.Byte.name(), this.colors[5] ) );
        sv.add( SingleValue.createFromValue( ValueType.Byte.name(), this.colors[9] ) );

        AxisSubset green = new AxisSubset( "green", "yes", null, sv );
        target.add( green );

        RangeSet rs = new RangeSet( "target", "target", target, null );

        AbstractRaster subset = filter.apply( bandRaster, rs );
        byte[] nullPixel = subset.getAsSimpleRaster().getRasterData().getNullPixel( null );
        Assert.assertEquals( 2, nullPixel.length );
        int[] nullValues = new int[3];
        nullValues[0] = 0; // red should be 0.
        nullValues[1] = nullPixel[0] & 0xFF;
        nullValues[2] = nullPixel[1] & 0xFF;
        if ( LOG.isDebugEnabled() ) {
            RasterFactory.saveRasterToFile( subset, File.createTempFile( "green_blue_interval_single", ".png" ) );
        }
        testDefaults( raster );
        BufferedImage newImage = RasterFactory.imageFromRaster( subset );
        testBandValues( newImage, -1, -1, null, new int[] { colors[5], colors[9] }, new int[] { colors[0], colors[1],
                                                                                               colors[2], colors[6],
                                                                                               colors[7] }, nullValues );
    }

    /**
     * @param newImage
     * @param i
     * @param j
     * @param bs
     * @param object
     * @param bs2
     */
    private void testImage( BufferedImage newImage ) {
        Assert.assertNotNull( newImage );
        Assert.assertEquals( width, newImage.getWidth() );
        Assert.assertEquals( height, newImage.getHeight() );
    }

    private void testOneBandValues( BufferedImage newImage, int x, int y, int[] values, int nullValue ) {
        testImage( newImage );
        Assert.assertTrue( "Datatype of one band should be byte",
                           newImage.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE );
        boolean[] matchedValues = new boolean[values == null ? 0 : values.length];
        byte[] color = new byte[1];
        Raster r = newImage.getRaster();
        if ( x < 0 && y < 0 ) {
            // all pixels

            for ( int h = 0; h < height; ++h ) {
                for ( int w = 0; w < width; ++w ) {
                    r.getDataElements( w, h, color );
                    testValue( "one_band", color[0] & 0xFF, values, nullValue, matchedValues );
                }
            }
        } else {
            if ( x < 0 ) {
                // one line
                for ( int w = 0; w < width; ++w ) {
                    r.getDataElements( w, y, color );
                    testValue( "one_band", color[0] & 0xFF, values, nullValue, matchedValues );
                }
            } else if ( y < 0 ) {
                // one column
                for ( int h = 0; h < height; ++h ) {
                    r.getDataElements( x, h, color );
                    testValue( "one_band", color[0] & 0xFF, values, nullValue, matchedValues );
                }
            } else {
                // one value.
                r.getDataElements( x, y, color );
                testValue( "one_band", color[0] & 0xFF, values, nullValue, matchedValues );
            }
        }
        for ( int i = 0; i < matchedValues.length; ++i ) {
            Assert.assertTrue( "Expected value: " + i + "(" + values[i] + ") was missing.", matchedValues[i] );
        }
    }

    private void testBandValues( BufferedImage newImage, int x, int y, int[] redValues, int[] greenValues,
                                 int[] blueValues, int[] nullValues ) {
        testImage( newImage );
        Assert.assertTrue( "Datatype of one band should be int",
                           newImage.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT );
        boolean[] matchedRedValues = new boolean[redValues == null ? 0 : redValues.length];
        boolean[] matchedGreenValues = new boolean[greenValues == null ? 0 : greenValues.length];
        boolean[] matchedBlueValues = new boolean[blueValues == null ? 0 : blueValues.length];
        ColorModel cm = newImage.getColorModel();

        if ( x < 0 && y < 0 ) {
            // al pixels
            WritableRaster raster = newImage.getRaster();
            int[] color = new int[1];
            for ( int h = 0; h < height; ++h ) {
                for ( int w = 0; w < width; ++w ) {
                    raster.getDataElements( w, h, color );
                    testValue( "red", cm.getRed( color ), redValues, nullValues[0], matchedRedValues );
                    testValue( "green", cm.getGreen( color ), greenValues, nullValues[1], matchedGreenValues );
                    testValue( "blue", cm.getBlue( color ), blueValues, nullValues[2], matchedBlueValues );
                }
            }

        } else {
            if ( x < 0 ) {
                // one line
                for ( int w = 0; w < width; ++w ) {
                    int rgb = newImage.getRGB( w, y );
                    testValue( "red", cm.getRed( rgb ), redValues, nullValues[0], matchedRedValues );
                    testValue( "green", cm.getGreen( rgb ), greenValues, nullValues[1], matchedGreenValues );
                    testValue( "blue", cm.getBlue( rgb ), blueValues, nullValues[2], matchedBlueValues );
                }
            } else if ( y < 0 ) {
                // one column
                for ( int h = 0; h < height; ++h ) {
                    int rgb = newImage.getRGB( x, h );
                    testValue( "red", cm.getRed( rgb ), redValues, nullValues[0], matchedRedValues );
                    testValue( "green", cm.getGreen( rgb ), greenValues, nullValues[1], matchedGreenValues );
                    testValue( "blue", cm.getBlue( rgb ), blueValues, nullValues[2], matchedBlueValues );
                }
            } else {
                // one value.
                int rgb = newImage.getRGB( x, y );
                testValue( "red", cm.getRed( rgb ), redValues, nullValues[0], matchedRedValues );
                testValue( "green", cm.getGreen( rgb ), greenValues, nullValues[1], matchedGreenValues );
                testValue( "blue", cm.getBlue( rgb ), blueValues, nullValues[2], matchedBlueValues );
            }
        }

        for ( int i = 0; i < matchedRedValues.length; ++i ) {
            Assert.assertTrue( "Expected red value: " + i + "(" + redValues[i] + ") was missing.", matchedRedValues[i] );
        }
        for ( int i = 0; i < matchedGreenValues.length; ++i ) {
            Assert.assertTrue( "Expected green value: " + i + "(" + greenValues[i] + ") was missing.",
                               matchedGreenValues[i] );
        }
        for ( int i = 0; i < matchedBlueValues.length; ++i ) {
            Assert.assertTrue( "Expected blue value: " + i + "(" + blueValues[i] + ") was missing.",
                               matchedBlueValues[i] );
        }
    }

    /**
     * Test if the given color is one of the given values, or 0 or not 0.
     * 
     * @param value
     * @param values
     * @param matchedValues
     */
    private void testValue( String colorString, int value, int[] values, int nullValue, boolean[] matchedValues ) {
        boolean match = nullValue == value;
        if ( values != null ) {
            if ( !match ) {
                if ( values.length != 0 ) {
                    for ( int i = 0; i < values.length; ++i ) {
                        int val = values[i];
                        match = val == value;
                        if ( match ) {
                            matchedValues[i] |= match;
                            return;
                        }
                    }
                } else {
                    match = value > 0;
                    Assert.assertTrue( colorString + " value must be larger than 0 (supplied), was: " + value, match );
                }
            }
        } else {
            Assert.assertTrue( colorString + " value must not be supplied (" + nullValue + "), was:" + value, match );
            // testing for null value was successful.
        }
    }

    private void testDefaults( AbstractRaster raster ) {
        Assert.assertNotNull( raster );
        Assert.assertEquals( width, raster.getColumns() );
        Assert.assertEquals( height, raster.getRows() );
        Assert.assertEquals( env, raster.getEnvelope() );
        RasterGeoReference rasterReference = raster.getRasterReference();
        Assert.assertEquals( rasterReference, raster.getRasterReference() );
    }
}
