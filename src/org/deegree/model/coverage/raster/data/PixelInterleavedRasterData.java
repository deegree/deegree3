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
package org.deegree.model.coverage.raster.data;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * This class implements a pixel-interleaved, ByteBuffer-based RasterData.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class PixelInterleavedRasterData extends ByteBufferRasterData {

    /**
     * Creates a new PixelInterleavedRasterData with given size, number of bands and data type
     * 
     * @param width
     *            width of new raster
     * @param height
     *            height of new raster
     * @param bands
     *            number of bands
     * @param dataType
     *            DataType of raster samples
     */
    public PixelInterleavedRasterData( int width, int height, int bands, DataType dataType ) {
        super( width, height, bands, dataType );
    }

    protected PixelInterleavedRasterData( int x0, int y0, int subWidth, int subHeight, int width, int height,
                                          int bands, DataType dataType, ByteBuffer data ) {
        super( x0, y0, subWidth, subHeight, width, height, bands, dataType, data );
    }

    @Override
    public PixelInterleavedRasterData createCompatibleRasterData( int width, int height, int bands ) {
        return new PixelInterleavedRasterData( width, height, bands, this.dataType );
    }

    @Override
    public final int getBandStride() {
        return 1 * dataType.getSize();
    }

    @Override
    public final int getLineStride() {
        return width * getPixelStride();
    }

    @Override
    public final int getPixelStride() {
        return bands * dataType.getSize();
    }

    @Override
    public final InterleaveType getInterleaveType() {
        return InterleaveType.PIXEL;
    }

    @Override
    public byte[][] getBytes( int x, int y, int width, int height, int band ) {
        checkBounds( x, y, width, height );

        byte[][] result = new byte[height][width];
        for ( int i = 0; i < height; i++ ) {
            for ( int j = 0; j < width; j++ ) {
                result[i][j] = data.get( calculatePos( x + j, y + i, band ) );
            }
        }
        return result;
    }

    @Override
    public byte[][] getBytes( int x, int y, int width, int height ) {
        checkBounds( x, y, width, height );

        byte[][] result = new byte[height][width * bands];

        for ( int i = 0; i < height; i++ ) {
            data.position( calculatePos( x, y + i ) );
            data.get( result[i], 0, result[i].length );
        }

        return result;
    }

    @Override
    public float[][] getFloats( int x, int y, int width, int height, int band ) {
        checkBounds( x, y, width, height );

        FloatBuffer bufView = data.asFloatBuffer();

        float[][] result = new float[height][width];
        for ( int i = 0; i < height; i++ ) {
            for ( int j = 0; j < width; j++ ) {
                result[i][j] = bufView.get( calculateViewPos( x + j, y + i, band ) );
            }
        }
        return result;
    }

    @Override
    public float[][] getFloats( int x, int y, int width, int height ) {
        checkBounds( x, y, width, height );

        FloatBuffer bufView = data.asFloatBuffer();

        float[][] result = new float[height][width * bands];

        for ( int i = 0; i < height; i++ ) {
            bufView.position( calculateViewPos( x, y + i ) );
            bufView.get( result[i], 0, result[i].length );
        }

        return result;
    }

    @Override
    public byte[][] getBytes( int x, int y, int width, int height, InterleaveType interleaving ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public float[][] getFloats( int x, int y, int width, int height, InterleaveType interleaving ) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#setBytes(int, int, int, int, int, byte[][])
     */
    public void setBytes( int x, int y, int width, int height, int band, byte[][] source ) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#setFloats(int, int, int, int, int, float[][])
     */
    public void setFloats( int x, int y, int width, int height, int band, float[][] source ) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.ByteBufferRasterData#getSubset(int, int, int, int)
     */
    public PixelInterleavedRasterData getSubset( int x0, int y0, int width, int height ) {
        int w = Math.min( width, subWidth - x0 );
        int h = Math.min( height, subHeight - y0 );
        checkBounds( x0, y0, w, h );
        PixelInterleavedRasterData result = createCompatibleRasterData( w, h, bands );
        PixelInterleavedRasterData subset = new PixelInterleavedRasterData( this.x0 + x0, this.y0 + y0, w, h,
                                                                            this.width, this.height, this.bands,
                                                                            this.dataType, this.data );
        result.setSubset( 0, 0, subset );

        return result;
    }

    public void setSubset( int x0, int y0, RasterData srcRaster ) {
        // System.out.format( "%d %d, %d %d\n", x0, y0, srcRaster.getWidth(), srcRaster.getHeight() );
        // checkBounds(x0, y0, srcRaster.getWidth(), srcRaster.getHeight());

        // copy data direct if interleaving type is identical _and_ both are not
        // single-band subsets
        boolean set = false;

        if ( ( srcRaster instanceof PixelInterleavedRasterData ) && !singleBand ) {
            PixelInterleavedRasterData raster = (PixelInterleavedRasterData) srcRaster;
            int width = Math.min( raster.getWidth(), subWidth - x0 );
            int height = Math.min( raster.getHeight(), subHeight - y0 );
            if ( width == 0 || height == 0 ) {
                return;
            }
            if ( !raster.singleBand ) {
                ByteBuffer srcData = raster.getByteBuffer();
                byte[] tmp = new byte[width * getPixelStride()];
                for ( int i = 0; i < height; i++ ) {
                    // order of position and get calls is significant, if bytebuffer is identical
                    srcData.position( raster.calculatePos( 0, i ) );
                    srcData.get( tmp );
                    data.position( calculatePos( x0, y0 + i ) );
                    data.put( tmp );
                }
                set = true;
            }
        }
        if ( !set ) { // else use generic setSubset method
            super.setSubset( x0, y0, srcRaster );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getSubset(int, int)
     */
    public RasterData getSubset( int outWidth, int outHeight ) {
        PixelInterleavedRasterData result = createCompatibleRasterData( outWidth, outHeight, bands );
        double xStep = (double) width / outWidth;
        double yStep = (double) height / outHeight;

        byte[] tmp = new byte[getPixelStride()];

        double xSrc = 0;
        double ySrc = 0;

        ByteBuffer srcBuf = this.getByteBuffer();
        ByteBuffer destBuf = result.getByteBuffer();
        destBuf.rewind();

        for ( int yDest = 0; yDest < outHeight; yDest++ ) {
            xSrc = 0;
            for ( int xDest = 0; xDest < outWidth; xDest++ ) {
                srcBuf.position( calculatePos( (int) xSrc, (int) ySrc ) );
                srcBuf.get( tmp );
                destBuf.put( tmp );
                xSrc += xStep;
            }
            ySrc += yStep;
        }

        return result;
    }
}
