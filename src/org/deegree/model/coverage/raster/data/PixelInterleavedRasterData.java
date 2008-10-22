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
    
    private PixelInterleavedRasterData( int width, int height, int bands, DataType dataType, boolean init ) {
        super( width, height, bands, dataType, init );
    }

    @Override
    public PixelInterleavedRasterData createCompatibleRasterData( int width, int height, int bands ) {
        return new PixelInterleavedRasterData( width, height, bands, this.dataType, true );
    }

    @Override
    protected ByteBufferRasterData createCompatibleEmptyRasterData() {
        return new PixelInterleavedRasterData( width, height, bands, this.dataType, false );
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
    public byte[] getPixel( int x, int y, byte[] result ) {
        if ( result == null ) {
            result = new byte[getBands() * bands];
        }
        if ( 0 > x || x >= width || 0 > y || y >= height  ) {
            System.arraycopy( nodata, 0, result, 0, result.length );
            return result;
        }
        data.position( calculatePos( x, y ) );
        data.get( result );
        return result;
    }

    @Override
    public void setPixel( int x, int y, byte[] result ) {
        data.position( calculatePos( x, y ) );
        data.put( result );
    }

    @Override
    public void setSubset( int x0, int y0, int width, int height, RasterData srcRaster, int xOffset, int yOffset ) {
        // clamp to maximum possible size
        int subWidth = min( this.width - x0, width, srcRaster.getWidth() - xOffset );
        int subHeight = min( this.height - y0, height, srcRaster.getHeight() - yOffset );
        
        if ( subHeight <= 0 || subWidth <= 0 ) {
            return;
        }
        
        // copy data direct if interleaving type is identical
        boolean set = false;

        if ( srcRaster instanceof PixelInterleavedRasterData ) {
            PixelInterleavedRasterData raster = (PixelInterleavedRasterData) srcRaster;
            ByteBuffer srcData = raster.getByteBuffer();
            byte[] tmp = new byte[subWidth * getPixelStride()];
            for ( int i = 0; i < subHeight; i++ ) {
                // order of .position and .get calls is significant, if bytebuffer is identical
                srcData.position( raster.calculatePos( xOffset, i + yOffset ) );
                srcData.get( tmp );
                data.position( calculatePos( x0, y0 + i ) );
                data.put( tmp );
            }
            set = true;
        }
        if ( !set ) { // else use generic setSubset method
            super.setSubset( x0, y0, width, height, srcRaster, xOffset, yOffset );
        }
    }

    // scaling code, to be moved in an external package
    // public RasterData getSubset( int outWidth, int outHeight ) {
    // PixelInterleavedRasterData result = createCompatibleRasterData( outWidth, outHeight, bands );
    // double xStep = (double) width / outWidth;
    // double yStep = (double) height / outHeight;
    //
    // byte[] tmp = new byte[getPixelStride()];
    //
    // double xSrc = 0;
    // double ySrc = 0;
    //
    // ByteBuffer srcBuf = this.getByteBuffer();
    // ByteBuffer destBuf = result.getByteBuffer();
    // destBuf.rewind();
    //
    // for ( int yDest = 0; yDest < outHeight; yDest++ ) {
    // xSrc = 0;
    // for ( int xDest = 0; xDest < outWidth; xDest++ ) {
    // srcBuf.position( calculatePos( (int) xSrc, (int) ySrc ) );
    // srcBuf.get( tmp );
    // destBuf.put( tmp );
    // xSrc += xStep;
    // }
    // ySrc += yStep;
    // }
    //
    // return result;
    // }

}
