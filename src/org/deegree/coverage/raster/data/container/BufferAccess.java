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

package org.deegree.coverage.raster.data.container;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.deegree.coverage.raster.data.DataView;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterReader;
import org.slf4j.Logger;

/**
 * The <code>BufferAccess</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class BufferAccess {
    private static final Logger LOG = getLogger( BufferAccess.class );

    private DataView view;

    /**
     * the raster data info of the original data, not the view.
     */
    protected RasterDataInfo dataInfo;

    /**
     * The raster data itself, will always be the size of the rasterWidthxRasterHeight
     */
    private ByteBuffer data;

    private RasterReader reader;

    private int lineStride;

    private int pixelStride;

    private int bandStride;

    private RasterRect maxViewData;

    private int maxDataWidth;

    private int maxDataHeight;

    // /** Intersection of the view with the values from the byte buffer. */
    // private RasterRect dataRect;

    public BufferAccess( RasterReader rasterReader, int maxDataWidth, int maxDataHeight, DataView view,
                         RasterDataInfo dataInfo, int pixelStride, int lineStride, int bandStride ) {
        this.reader = rasterReader;
        this.pixelStride = pixelStride;
        this.bandStride = bandStride;
        this.view = view;
        this.dataInfo = dataInfo;
        this.maxDataWidth = maxDataWidth;
        this.maxDataHeight = maxDataHeight;

        RasterRect origData = new RasterRect( 0, 0, maxDataWidth, maxDataHeight );
        maxViewData = RasterRect.intersection( origData, view );
        if ( maxViewData == null ) {
            maxViewData = new RasterRect( 0, 0, 0, 0 );
        }
        this.lineStride = maxViewData.width * pixelStride;

        // the byte buffer intersection is the whole view
        // dataRect = new RasterRect( 0, 0, view.width, view.height );
    }

    /**
     * Calculates the position of a pixel in the ByteBuffer.
     * 
     * This method calculates the position of a pixel and returns the offset to this pixel in bytes. Use this method for
     * direct access to ByteBuffers.
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @return byte offset to the pixel with the specified coordinate or -1 if outside of the bytebuffer.
     */
    public final int calculatePos( int x, int y ) {
        // check for negative views.
        int yPos = ( view.y - maxViewData.y ) + y;
        int xPos = ( view.x - maxViewData.x ) + x;
        int dataPos = ( xPos * pixelStride ) + ( yPos * lineStride );
        if ( yPos < 0 || xPos < 0 || yPos >= ( maxViewData.height ) || xPos >= ( maxViewData.width )
             || dataPos > requiredBufferSize() ) {
            dataPos = -1;
        }
        return dataPos;
    }

    /**
     * Calculates the position of a sample in the ByteBuffer.
     * 
     * This method calculates the position of a pixel and returns the offset to this pixel in bytes. Use this method for
     * direct access to ByteBuffers.
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @param bandOfView
     *            band index of the sample
     * @return byte offset to the sample with the specified coordinate or -1 if outside of the bytebuffer.
     */
    public final int calculatePos( int x, int y, int bandOfView ) {
        // check for negative views.
        int yPos = ( view.y - maxViewData.y ) + y;
        int xPos = ( view.x - maxViewData.x ) + x;
        int dataPos = ( xPos * pixelStride ) + ( yPos * lineStride ) + ( view.getBandOffset( bandOfView ) * bandStride );
        if ( yPos < 0 || xPos < 0 || yPos >= ( maxViewData.height ) || xPos >= ( maxViewData.width )
             || dataPos > requiredBufferSize() || dataPos < 0 ) {
            dataPos = -1;
        }
        return dataPos;
    }

    /**
     * Calculates the position of a pixel in a view (FloatBuffer, etc.) of the ByteBuffer.
     * 
     * This method considers different sample sizes (eg. byte, float) and returns the position in sample strides (not
     * byte strides). Use this method to get proper positions for ByteBuffer views like FloatBuffer, ShortBuffer, etc..
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @return offset to the pixel with the specified coordinates or -1 if outside of the bytebuffer.
     */
    public final int calculateViewPos( int x, int y ) {
        int pos = calculatePos( x, y ) / dataInfo.dataSize;
        return pos < 0 ? -1 : pos;
    }

    /**
     * Calculates the position of a sample in a view (FloatBuffer, etc.) of the ByteBuffer.
     * 
     * This method considers different sample sizes (eg. byte, float) and returns the position in sample stides (not
     * byte strides). Use this method to get proper positions for ByteBuffer-views like FloatBuffer, ShortBuffer, etc..
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @param band
     *            band index of the sample
     * @return offset to the sample with the specified coordinates
     */
    public final int calculateViewPos( int x, int y, int band ) {
        int pos = calculatePos( x, y, band ) / dataInfo.dataSize;
        return pos < 0 ? -1 : pos;
    }

    /**
     * Prepares the byte buffer for reading / writing thus instantiates it with values (no) data;
     */
    public void prepareBuffer() {
        if ( data == null ) {
            data = ByteBuffer.allocate( requiredBufferSize() );
            boolean noData = false;
            if ( reader != null ) {
                try {
                    BufferResult dataResult = reader.read( maxViewData, data );
                    data = dataResult.getResult();
                    data.rewind();
                    RasterRect rect = dataResult.getRect();
                    // lineStride = pixelStride * rect.width;
                } catch ( IOException e ) {
                    LOG.debug( "No data available: " + e.getLocalizedMessage(), e );
                    // the data is no longer available, lets just fill it with no data values
                    noData = true;
                }
            }
            if ( noData ) {
                fillWithNoData();
            }
        }
    }

    /**
     * Fills the entire buffer with no data values. Note this operation is only possible on writable buffers.
     */
    public void fillWithNoData() {
        if ( data == null ) {
            data = ByteBuffer.allocate( requiredBufferSize() );
        }
        if ( !data.isReadOnly() ) {
            int pos = 0;
            int cap = data.capacity();
            byte[] noData = dataInfo.getNoDataPixel( new byte[0] );
            data.position( 0 );
            while ( pos != cap ) {
                data.put( noData );
                pos = data.position();
            }
        }
    }

    /**
     * Returns the needed size of the ByteBuffer in bytes.
     * 
     * @return size of the buffer
     */
    public final int requiredBufferSize() {
        // data.capacity() can not be used if the ByteBuffer was not yet instantiated.
        return maxViewData.width * maxViewData.height * dataInfo.bands * dataInfo.dataSize;
    }

    /**
     * @return The internal ByteBuffer.
     */
    public ByteBuffer getByteBuffer() {
        if ( data == null ) {
            prepareBuffer();
        }
        return data;
    }

    /**
     * @param newData
     *            to use.
     * @param original
     */
    public void setByteBuffer( ByteBuffer newData, DataView original ) {
        if ( newData != null ) {
            if ( original != null ) {
                // current buffer access is the original.
                // dataRect = original;

            }
            if ( newData.capacity() < requiredBufferSize() ) {
                LOG.error( "The given byteBuffer does not contain enough space for the current view." );
                return;
            }
        }
        this.data = newData;
    }

    /**
     * @return the reader which supplies this buffer with data, may be <code>null</code> if the buffer is not backed by
     *         data (a new memory based raster for example).
     */
    public RasterReader getReader() {
        return reader;
    }

    /**
     * @return
     */
    public int getMaxDataHeight() {
        return this.maxDataHeight;
    }

    /**
     * @return
     */
    public int getMaxDataWidth() {
        return maxDataWidth;
    }
}
