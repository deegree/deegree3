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
package org.deegree.coverage.raster.data.nio;

import java.nio.ByteBuffer;

import org.deegree.coverage.raster.data.BandType;
import org.deegree.coverage.raster.data.DataType;
import org.deegree.coverage.raster.data.InterleaveType;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.geom.RasterRect;

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
     * @param sampleDomain
     *            the raster rectangle defining the sample domain of this raster data.
     * @param rasterWidth
     *            width of the underlying raster data.
     * @param rasterHeight
     *            height of the underlying raster data.
     * @param bands
     *            number of bands
     * @param dataType
     *            DataType of raster samples
     */
    public PixelInterleavedRasterData( RasterRect sampleDomain, int rasterWidth, int rasterHeight, BandType[] bands,
                                       DataType dataType ) {
        super( sampleDomain, rasterWidth, rasterHeight, bands, dataType );
    }

    /**
     * 
     * @param sampleDomain
     *            the raster rectangle defining the sample domain of this raster data.
     * @param rasterWidth
     *            width of the underlying raster data.
     * @param rasterHeight
     *            height of the underlying raster data.
     * @param bands
     *            number of bands
     * @param dataType
     *            DataType of raster samples
     * @param init
     *            true if a new buffer should be initialized
     */
    protected PixelInterleavedRasterData( RasterRect sampleDomain, int rasterWidth, int rasterHeight, BandType[] bands,
                                          DataType dataType, boolean init ) {
        super( sampleDomain, rasterWidth, rasterHeight, bands, dataType, init );
    }

    @Override
    public PixelInterleavedRasterData createCompatibleRasterData( RasterRect sampleDomain, BandType[] bands ) {
        return new PixelInterleavedRasterData( sampleDomain, rasterWidth, rasterHeight, bands, this.dataType, false );
    }

    @Override
    public RasterData createCompatibleWritableRasterData( RasterRect sampleDomain, BandType[] bands ) {
        return new PixelInterleavedRasterData( sampleDomain, sampleDomain.width, sampleDomain.height, bands,
                                               this.dataType, true );
    }

    @Override
    protected ByteBufferRasterData createCompatibleEmptyRasterData() {
        return new PixelInterleavedRasterData( view, rasterWidth, rasterHeight, bandsTypes, this.dataType, false );
    }

    @Override
    public final int getBandStride() {
        return 1 * dataType.getSize();
    }

    @Override
    public final int getLineStride() {
        return rasterWidth * getPixelStride();
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
        if ( 0 > x || x >= rasterWidth || 0 > y || y >= rasterHeight ) {
            System.arraycopy( nodata, 0, result, 0, result.length );
            return result;
        }
        data.position( calculatePos( x, y ) );
        data.get( result );
        return result;
    }

    @Override
    public void setPixel( int x, int y, byte[] result ) {
        if ( data == null ) {
            initByteBuffer();
        }
        data.position( calculatePos( x, y ) );
        data.put( result );
    }

    @Override
    public void setSubset( int x0, int y0, int width, int height, RasterData srcRaster, int xOffset, int yOffset ) {
        if ( data == null ) {
            initByteBuffer();
        }
        // clamp to maximum possible size
        // int wx0 = this.rasterWidth - x0;
        // int hy0 = this.rasterHeight - y0;
        // the actual width and height of this raster
        int wx0 = this.getWidth() - x0;
        int hy0 = this.getHeight() - y0;

        // the width and height of the raster from which the data will be copied
        int srcw = srcRaster.getWidth() - xOffset;
        int srch = srcRaster.getHeight() - yOffset;

        int subWidth = min( wx0, width, srcw );
        int subHeight = min( hy0, height, srch );

        if ( subHeight <= 0 || subWidth <= 0 ) {
            return;
        }

        // copy data direct if interleaving type is identical
        if ( srcRaster instanceof PixelInterleavedRasterData ) {
            PixelInterleavedRasterData raster = (PixelInterleavedRasterData) srcRaster;
            ByteBuffer srcData = raster.getByteBuffer().asReadOnlyBuffer();
            // byte[] tmp = new byte[subWidth * getPixelStride()];
            int length = subWidth * getPixelStride();
            for ( int i = 0; i < subHeight; i++ ) {
                // order of .position and .get calls is significant, if bytebuffer is identical
                int pos = raster.calculatePos( xOffset, i + yOffset );
                srcData.limit( pos + length );
                srcData.position( pos );
                // srcData.get( tmp );
                pos = calculatePos( x0, y0 + i );
                data.position( pos );
                data.put( srcData.slice() );
                // data.put( tmp );
            }
        } else {
            // else use generic setSubset method
            super.setSubset( x0, y0, width, height, srcRaster, xOffset, yOffset );
        }
    }
}
