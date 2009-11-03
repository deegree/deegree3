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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

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
        ImageIO.write( img, "tif", new File( "/tmp/out.tif" ) );

    }
}
