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

package org.deegree.coverage.raster;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.gc;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.RegistryElementDescriptor;
import javax.media.jai.RegistryMode;
import javax.media.jai.RenderedOp;

import junit.framework.Assert;

import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.InterleaveType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.data.nio.PixelInterleavedRasterData;
import org.deegree.coverage.raster.geom.RasterRect;
import org.junit.Test;

/**
 * The <code>RasterFactory</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class RasterFactory {

    private final static float[] TEST_HEIGHTS = new float[] { -10.912f, -5.8f, 0.001f, 5.5f, 10.1f, 15.3f };

    /**
     * Test the creation of a float buffered image.
     * 
     * @throws IOException
     */
    @Test
    public void testFloatRaster()
                            throws IOException {
        int width = 20;
        int height = 20;
        RasterDataInfo rdi = new RasterDataInfo( new BandType[] { BandType.BAND_0 }, DataType.FLOAT,
                                                 InterleaveType.PIXEL );
        PixelInterleavedRasterData pird = new PixelInterleavedRasterData( new RasterRect( 0, 0, width, height ), width,
                                                                          height, rdi );

        for ( int y = 0; y < height; y++ ) {
            for ( int x = 0; x < width; x++ ) {
                pird.setFloatSample( x, y, 0, TEST_HEIGHTS[(int) ( Math.random() * TEST_HEIGHTS.length )] );
            }
        }

        BufferedImage img = org.deegree.coverage.raster.utils.RasterFactory.rasterDataToImage( pird );
        Assert.assertEquals( width, img.getWidth() );
        Assert.assertEquals( height, img.getHeight() );

        byte[] result = new byte[DataType.FLOAT.getSize()];
        ByteBuffer converter = ByteBuffer.wrap( result );
        for ( int by = 0; by < height; ++by ) {
            for ( int bx = 0; bx < width; ++bx ) {
                int val = img.getRGB( bx, by );
                converter.putInt( 0, val );
                float realVal = converter.getFloat( 0 );
                float compareVal = pird.getFloatSample( bx, by, 0 );
                Assert.assertEquals( compareVal, realVal );
            }
        }
        // ImageIO.write( img, "tif", new File( "/tmp/out.tif" ) );
    }

    /**
     * Test the creation of a float buffered image.
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    public synchronized void testTiledImage()
                            throws IOException, InterruptedException {

        long t = System.currentTimeMillis();

        RenderedOp tiff = getJAIImage( "test_tiled_image_lzw.tif" );
        RenderedOp tiffNone = getJAIImage( "test_tiled_image.tif" );

        long ret = currentTimeMillis();
        saveSubset( tiff, "jai_tif_1", 0, 0, 250, 260 );
        System.out.println( "subset 1: " + ( currentTimeMillis() - ret ) + "millis" );
        ret = currentTimeMillis();
        saveSubset( tiff, "jai_tif_2", 4998, 4900, 10500, 13080 );
        System.out.println( "subset 2: " + ( currentTimeMillis() - ret ) + "millis" );

        gc();
        gc();
        gc();
        gc();
        gc();
        gc();
        ret = currentTimeMillis();
        saveSubset( tiffNone, "jai_tif_none_1", 0, 0, 250, 260 );
        System.out.println( "subset none 1: " + ( currentTimeMillis() - ret ) + "millis" );
        ret = currentTimeMillis();
        saveSubset( tiffNone, "jai_tif_none_2", 4998, 4900, 10500, 13080 );
        System.out.println( "subset none 2: " + ( currentTimeMillis() - ret ) + "millis" );

        System.out.println( "jai total: " + ( currentTimeMillis() - t ) + "millis" );
        t = System.currentTimeMillis();
    }

    private void saveSubset( RenderedOp image, String name, int x, int y, int w, int h )
                            throws IOException {
        System.out.println( "getting subset: " + name );
        WritableRaster jpgRaster = image.getColorModel().createCompatibleWritableRaster( w, h ).createWritableTranslatedChild(
                                                                                                                               x,
                                                                                                                               y );
        image.copyExtendedData( jpgRaster, BorderExtender.createInstance( BorderExtender.BORDER_ZERO ) );
        System.out.println( "wrote subset: " + name );
    }

    private RenderedOp getJAIImage( String file )
                            throws IOException {
        File f = new File( file );
        ImageInputStream iis = ImageIO.createImageInputStream( f );
        ParameterBlockJAI pbj = new ParameterBlockJAI( "ImageRead" );
        pbj.setParameter( "Input", iis );

        RenderedOp result = JAI.create( "ImageRead", pbj, null );

        int width = result.getWidth();
        int height = result.getHeight();
        int tileWidth = width;
        int tileHeight = height;
        double scaleWidth = width / 500.d;
        double scaleHeight = height / 500.d;
        if ( scaleWidth > 1 ) {
            tileWidth = (int) Math.floor( width / scaleWidth );
        }

        if ( scaleWidth > 1 ) {
            tileHeight = (int) Math.floor( height / scaleHeight );
        }
        ImageLayout layout = new ImageLayout();
        layout.setTileWidth( tileWidth );
        layout.setTileHeight( tileHeight );

        result.setRenderingHint( JAI.KEY_IMAGE_LAYOUT, layout );

        return result;
    }

    private void outputJAIParams() {
        JAI jai = JAI.getDefaultInstance();
        OperationRegistry reg = jai.getOperationRegistry();
        RenderingHints renderingHints = jai.getRenderingHints();
        System.out.println( renderingHints.values() );

        String[] modeNames = RegistryMode.getModeNames();
        for ( String modeName : modeNames ) {
            System.out.println( "**** " + modeName + " ****" );
            String[] descriptorNames = reg.getDescriptorNames( modeName );
            for ( String dn : descriptorNames ) {
                System.out.println( "- " + dn );
                RegistryElementDescriptor descriptor = reg.getDescriptor( modeName, dn );
                String[] supportedModes = descriptor.getSupportedModes();
                for ( String sm : supportedModes ) {
                    System.out.println( " - " + sm );
                    ParameterListDescriptor parameterListDescriptor = descriptor.getParameterListDescriptor( sm );
                    if ( parameterListDescriptor != null ) {
                        String[] parameterNames = parameterListDescriptor.getParamNames();
                        if ( parameterNames != null ) {
                            for ( String parameterName : parameterNames ) {
                                System.out.println( "  - " + parameterName );
                            }
                        }
                    } else {
                        System.out.println( "  - no parameters" );
                    }
                }
            }
        }

    }
}
