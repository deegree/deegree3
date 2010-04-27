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

import org.deegree.coverage.raster.data.DataView;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterReader;

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
     * Creates a new PixelInterleavedRasterData with given size, number of bands and data type, backed with no data.
     * 
     * @param sampleDomain
     *            the raster rectangle defining the sample domain of this raster data.
     * @param rasterWidth
     *            width of the underlying raster data.
     * @param rasterHeight
     *            height of the underlying raster data.
     * @param dataInfo
     *            containing information about the underlying raster.
     */
    public PixelInterleavedRasterData( RasterRect sampleDomain, int rasterWidth, int rasterHeight,
                                       RasterDataInfo dataInfo ) {
        this( sampleDomain, rasterWidth, rasterHeight, null, dataInfo );
    }

    /**
     * Creates a new PixelInterleavedRasterData with given size, number of bands and data type
     * 
     * @param sampleDomain
     *            the raster rectangle defining the sample domain of this raster data.
     * @param rasterWidth
     *            width of the underlying raster data.
     * @param rasterHeight
     *            height of the underlying raster data.
     * @param reader
     *            to be used for reading the data, may be <code>null<code>
     * @param dataInfo
     *            containing information about the underlying raster.
     */
    public PixelInterleavedRasterData( RasterRect sampleDomain, int rasterWidth, int rasterHeight, RasterReader reader,
                                       RasterDataInfo dataInfo ) {
        this( new DataView( sampleDomain, dataInfo ), rasterWidth, rasterHeight, reader, dataInfo, false );
    }

    /**
     * 
     * @param view
     *            the raster rectangle defining the sample domain of this raster data as well as the data info of the
     *            view.
     * @param rasterWidth
     *            width of the underlying raster data.
     * @param rasterHeight
     *            height of the underlying raster data.
     * @param reader
     *            to be used for reading the data, may be <code>null<code>
     * @param dataInfo
     *            containing information about the underlying raster.
     * @param init
     *            true if a new buffer should be initialized
     */
    protected PixelInterleavedRasterData( DataView view, int rasterWidth, int rasterHeight, RasterReader reader,
                                          RasterDataInfo dataInfo, boolean init ) {
        super( view, rasterWidth, rasterHeight, reader, dataInfo, init );
    }

    @Override
    public PixelInterleavedRasterData createCompatibleRasterData( DataView view ) {
        return new PixelInterleavedRasterData( view, getOriginalWidth(), getOriginalHeight(), dataAccess.getReader(),
                                               dataInfo, false );
    }

    @Override
    public RasterData createCompatibleWritableRasterData( RasterRect sampleDomain, BandType[] bands ) {
        // a new raster will be created, the old information should be discarded.
        RasterDataInfo newRasterInfo = createRasterDataInfo( bands );
        return new PixelInterleavedRasterData( new DataView( sampleDomain, newRasterInfo ), sampleDomain.width,
                                               sampleDomain.height, null, newRasterInfo, true );
    }

    @Override
    protected ByteBufferRasterData createCompatibleEmptyRasterData() {
        return new PixelInterleavedRasterData( getView(), getOriginalWidth(), getOriginalHeight(),
                                               dataAccess.getReader(), this.dataInfo, false );
    }

    @Override
    public final int getBandStride() {
        return dataInfo.dataSize;
    }

    @Override
    public final int getLineStride() {
        return getOriginalWidth() * getPixelStride();
    }

    @Override
    public final int getPixelStride() {
        return dataInfo.bands * dataInfo.dataSize;
    }

    @Override
    public byte[] getPixel( int x, int y, byte[] result ) {
        if ( getView().dataInfo.bands != dataInfo.bands ) {
            return super.getPixel( x, y, result );
        }
        if ( result == null ) {
            result = new byte[dataInfo.noDataPixel.length];
        }
        int pos = calculatePos( x, y );
        if ( pos == -1 ) {
            System.arraycopy( dataInfo.noDataPixel, 0, result, 0, result.length );
        } else {
            ByteBuffer data = getByteBuffer();
            data.position( pos );
            data.get( result, 0, dataInfo.noDataPixel.length );
        }

        return result;
    }

    @Override
    public void setPixel( int x, int y, byte[] result ) {
        if ( getView().dataInfo.bands != dataInfo.bands ) {
            // Is this a view on less bands?
            super.setPixel( x, y, result );
            return;
        }
        ByteBuffer data = getByteBuffer();
        data.position( calculatePos( x, y ) );
        data.put( result, 0, dataInfo.noDataPixel.length );
    }

    @Override
    public void setSubset( int dstX, int dstY, int width, int height, RasterData srcRaster, int srcX, int srcY ) {

        // // // the actual width and height of this raster
        // int wx0 = this.getWidth() - dstX;
        // int hy0 = this.getHeight() - dstY;
        // //
        // // // the width and height of the raster from which the data will be copied
        // int srcw = srcRaster.getWidth()/* - srcX */;
        // int srch = srcRaster.getHeight()/* - srcY */;
        // //
        // // // clamp to maximum possible size
        // int subWidth = min( wx0, width, srcw );
        // int subHeight = min( hy0, height, srch );

        // copy data direct if interleaving type is identical

        if ( srcRaster instanceof PixelInterleavedRasterData && getView().dataInfo.bands == dataInfo.bands ) {
            // calculate if the getWidth || getWidth methods would exceed the actual rasterWidth
            PixelInterleavedRasterData raster = (PixelInterleavedRasterData) srcRaster;

            int srcRasterPosx = raster.getView().x + srcX;
            int srcRasterPosy = raster.getView().y + srcY;
            int possibleSrcWidth = raster.getWidth();
            int possibleSrcHeight = raster.getHeight();

            if ( srcRasterPosx < 0 ) {
                // origin is negative, so add them to the width(subtract them).
                possibleSrcWidth += srcRasterPosx;
                srcRasterPosx = 0;
            } else if ( srcRasterPosx >= raster.getOriginalWidth() ) {
                // no copy possible
                possibleSrcWidth = 0;
                srcRasterPosx = raster.getOriginalWidth();
            }

            if ( srcRasterPosy < 0 ) {
                // origin is negative, so add them to the height (subtract them).
                possibleSrcHeight += srcRasterPosy;
                // adjust for snapping to the raster
                srcRasterPosy = 0;
            } else if ( srcRasterPosy >= raster.getOriginalHeight() ) {
                // no copy possible
                possibleSrcHeight = 0;
                srcRasterPosy = raster.getOriginalHeight();
            }

            // if the number of pixels from the source raster x position to requested width > raster width, snap the new
            // width to the maximum raster width.
            if ( ( srcRasterPosx + possibleSrcWidth ) >= raster.getOriginalWidth() ) {
                possibleSrcWidth = raster.getOriginalWidth() - srcRasterPosx;
            }
            if ( ( srcRasterPosy + possibleSrcHeight ) >= raster.getOriginalHeight() ) {
                possibleSrcHeight = raster.getOriginalHeight() - srcRasterPosy;
            }
            // reset to the view, for the calculation of the position
            srcRasterPosx -= raster.getView().x;
            srcRasterPosy -= raster.getView().y;
            // int srcRasterPosx = raster.getView().x;
            // int srcRasterPosy = raster.getView().y;

            // find the smallest denominator of all values.
            // int subWidth = clampSize( getWidth(), dstX, possibleSrcWidth,
            // 0/* the possible srcWidth has been determined */, width );
            // int subHeight = clampSize( getHeight(), dstY, possibleSrcHeight, 0/*
            // * the possible srcWidth has been
            // * determined
            // */, height );
            int subWidth = clampSize( getWidth(), dstX, raster.dataAccess.getDataRectangle().width, 0/*
                                                                                                      * the possible
                                                                                                      * srcWidth has
                                                                                                      * been determined
                                                                                                      */, width );
            int subHeight = clampSize( getHeight(), dstY, raster.dataAccess.getDataRectangle().height, 0/*
                                                                                                         * the possible
                                                                                                         * srcWidth has
                                                                                                         * been
                                                                                                         * determined
                                                                                                         */, height );

            if ( subHeight <= 0 || subWidth <= 0 ) {
                return;
            }

            ByteBuffer srcData = raster.getByteBuffer().asReadOnlyBuffer();
            // byte[] tmp = new byte[subWidth * getPixelStride()];
            int length = subWidth * getPixelStride();
            ByteBuffer data = getByteBuffer();
            for ( int i = 0; i < subHeight; i++ ) {
                int pos = raster.calculatePos( srcRasterPosx, i + srcRasterPosy );
                // order of .position and .get calls is significant, if bytebuffer is identical
                srcData.limit( pos + length );
                srcData.position( pos );
                // srcData.get( tmp );
                pos = calculatePos( dstX, dstY + i );
                data.position( pos );
                data.put( srcData.slice() );
                // data.put( tmp );
            }
        } else {
            // else use generic setSubset method
            super.setSubset( dstX, dstY, width, height, srcRaster, srcX, srcY );
        }
    }
}
