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

import static org.slf4j.LoggerFactory.getLogger;

import java.nio.ByteBuffer;
import java.util.SortedMap;
import java.util.TreeMap;

import org.deegree.coverage.raster.data.DataView;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterReader;
import org.slf4j.Logger;

/**
 * This abstract class implements the RasterData interface for ByteBuffer based raster.
 * 
 * <p>
 * It is based on java.nio.ByteBuffer and implements common get- and set-operations on the data. The different
 * InterleaveTypes are implemented by additional subclasses.
 * 
 * <p>
 * get- and set-operations are implemented naive and access all data sample-wise. For efficiency subclasses should
 * overwrite methods that access more than one sample and leverage the knowledge of the internal storage format
 * (interleaving).
 * 
 * <p>
 * TODO: Only implements access to byte and float data at the moment. Copy float methods for other data types and change
 * 'Float/float' to short, int, long or double. These types are supported by ByteBuffer and the according methods only
 * differ in the name of the type (eg. getFloat, getInt, getDouble,...). Opposed to the methods for bytes, which lack
 * the type in the method names (eg. only get()).
 * 
 * <p>
 * Also this implementation is able to store a sub-view on another {@link ByteBufferRasterData}, resp. ByteBuffer. With
 * this feature you are able to create subsets without copying the data. Though the current deegree SimpleRaster
 * implementation makes no use of it.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public abstract class ByteBufferRasterData implements RasterData {

    private static final Logger LOG = getLogger( ByteBufferRasterData.class );

    // /**
    // * The view of this data
    // */
    // protected DataView view;

    /**
     * the width of the raster
     */
    protected int rasterWidth;

    /**
     * the height of the raster
     */
    protected int rasterHeight;

    /**
     * the raster data info of the original data, not the view.
     */
    protected RasterDataInfo dataInfo;

    /**
     * Buffer access holds a reference to the reader so not all data should be in memory
     */
    protected BufferAccess dataAccess;

    /** information on this raster data */
    public String info;

    /**
     * ==================== Abstract method declarations ====================
     */

    /**
     * Implementation should create a view of this raster data.
     * 
     * @param view
     *            the new view on this data
     * 
     * @return a view or new raster data object, backed by a {@link java.nio.ByteBuffer}
     */
    protected abstract ByteBufferRasterData createCompatibleRasterData( DataView view );

    /**
     * @return ByteBufferRasterData with unset data
     */
    protected abstract ByteBufferRasterData createCompatibleEmptyRasterData();

    /**
     * Retruns the byte offset to the next pixel.
     * 
     * @return byte offset to next pixel
     */
    public abstract int getPixelStride();

    /**
     * Returns the byte offset to the next sample of the same pixel.
     * 
     * @return byte offset to sample in the next band (same pixel)
     */
    public abstract int getBandStride();

    /**
     * Returns the byte offset to the next row (same column, same sample)
     * 
     * @return byte offset to next row (same column, same sample)
     */
    public abstract int getLineStride();

    /**
     * ==================== Implementation ====================
     */

    /**
     * Fills the entire buffer with no data values. Note this operation is only possible on writable buffers.
     */
    public void fillWithNoData() {
        dataAccess.fillWithNoData();
    }

    /**
     * Creates a new ByteBufferRasterData instance.
     * 
     * @param view
     *            the raster rectangle defining the sample domain of this raster data.
     * @param rasterWidth
     *            width of the raster data
     * @param rasterHeight
     *            height of the raster data
     * @param reader
     *            to be used to read the data from a location.
     * @param originalDataInfo
     *            containing information about this raster data object
     * @param init
     *            true if the ByteBuffer should be initialized
     */
    protected ByteBufferRasterData( DataView view, int rasterWidth, int rasterHeight, RasterReader reader,
                                    RasterDataInfo originalDataInfo, boolean init ) {
        // this.view = view;
        this.rasterWidth = rasterWidth;
        this.rasterHeight = rasterHeight;
        this.dataInfo = originalDataInfo;
        this.dataAccess = new BufferAccess( reader, rasterWidth, rasterHeight, view, originalDataInfo,
                                            getPixelStride(), getLineStride(), getBandStride() );
        if ( init ) {
            initByteBuffer();
        }
    }

    /**
     * @return the view on the data
     */
    public DataView getView() {
        return dataAccess.getView();
    }

    /**
     * Creates a new ByteBufferRasterData instance.
     * 
     * @param view
     *            the raster rectangle defining the sample domain of this raster data.
     * @param rasterWidth
     *            width of the raster data
     * @param rasterHeight
     *            height of the raster data
     * @param reader
     *            to be used to read the data.
     * @param originalDataInfo
     *            containing information about this raster data object
     */
    protected ByteBufferRasterData( DataView view, int rasterWidth, int rasterHeight, RasterReader reader,
                                    RasterDataInfo originalDataInfo ) {
        this( view, rasterWidth, rasterHeight, reader, originalDataInfo, true );
    }

    /**
     * Initialize the internal ByteBuffer
     */
    protected void initByteBuffer() {
        dataAccess.prepareBuffer();
    }

    /**
     * Use the given bytebuffer as a data source. This method should be handled with much care.
     * 
     * @param buf
     *            to set.
     * @param dataRect
     *            defining the width, height and position of the data.
     */
    public void setByteBuffer( ByteBuffer buf, DataView dataRect ) {
        dataAccess.setByteBuffer( buf, dataRect );
    }

    /**
     * Returns the needed size of the ByteBuffer in bytes.
     * 
     * @return size of the buffer
     */
    public final int getRequiredBufferSize() {
        // data.capacity() can not be used if the ByteBuffer was not yet instantiated.
        // return rasterWidth * rasterHeight * dataInfo.bands * dataInfo.dataSize;
        return dataAccess.requiredBufferSize();
    }

    public ByteBufferRasterData createCompatibleRasterData( int width, int height ) {
        // use the offset from the view and the new width and height (shouldn't they be checked for size?)
        return createCompatibleRasterData( new DataView( getView().x, getView().y, width, height, dataInfo ) );
    }

    public ByteBufferRasterData createCompatibleRasterData( RasterRect env ) {
        // create a new view using the given rectangle, the info will be the same.
        return createCompatibleRasterData( new DataView( env, dataInfo ) );
    }

    @Override
    public RasterData createCompatibleRasterData( RasterRect sampleDomain, BandType[] bands ) {
        // create a view taken using the given rectangle and the given bands
        return createCompatibleRasterData( new DataView( sampleDomain, createRasterDataInfo( bands ), dataInfo ) );
    }

    public ByteBufferRasterData createCompatibleRasterData( BandType[] bands ) {
        // create a new view from the given bands
        return createCompatibleRasterData( new DataView( getView(), createRasterDataInfo( bands ), dataInfo ) );
    }

    public ByteBufferRasterData createCompatibleRasterData() {
        // just use the view on the data
        return createCompatibleRasterData( getView() );
    }

    @Override
    public RasterData asReadOnly() {
        ByteBufferRasterData copy = createCompatibleEmptyRasterData();
        copy.info = info;
        // this instantiation ensures a readonly buffer.
        copy.setByteBuffer( this.getByteBuffer().asReadOnlyBuffer(), null );
        return copy;
    }

    public DataType getDataType() {
        return getView().dataInfo.dataType;
    }

    public int getBands() {
        return getView().dataInfo.bands;
    }

    public RasterDataInfo getDataInfo() {
        return getView().dataInfo;
    }

    /**
     * @return the original datainfo object.
     */
    public RasterDataInfo getOriginalDataInfo() {
        return dataInfo;
    }

    /**
     * @return the underlying raster height.
     */
    public int getOriginalHeight() {
        return rasterHeight;
    }

    /**
     * @return the domain of validity of the byte buffer, which need not be the view on the data.
     */
    public RasterRect getBytebufferDomain() {
        return dataAccess.getBytebufferDomain();
    }

    /**
     * @return the underlying raster width.
     */
    public int getOriginalWidth() {
        return rasterWidth;
    }

    public byte[] getNullPixel( byte[] result ) {
        return getView().dataInfo.getNoDataPixel( result );
    }

    public void setNoDataValue( byte[] values ) {
        getView().dataInfo.setNoDataPixel( values );
    }

    public final int getColumns() {
        return getView().width;
    }

    public final int getRows() {
        return getView().height;
    }

    /**
     * @return The internal ByteBuffer.
     */
    public ByteBuffer getByteBuffer() {
        return dataAccess.getByteBuffer();
    }

    /**
     * Checks whether a given rect is inside the raster, throws an exception if outside.
     * 
     * @param x
     *            The x position of the rect.
     * @param y
     *            The y position of the rect.
     * @param width
     *            The width of the rect.
     * @param height
     *            The height of the rect.
     * @throws IndexOutOfBoundsException
     *             if the given rect is outside the raster
     */
    protected final void checkBoundsEx( int x, int y, int width, int height )
                            throws IndexOutOfBoundsException {
        if ( !checkBounds( x, y, width, height ) ) {
            throw new IndexOutOfBoundsException( "request out of bounds" );
        }
    }

    /**
     * Checks whether a given rect is inside the raster.
     * 
     * @param x
     *            The x position of the rect.
     * @param y
     *            The y position of the rect.
     * @param width
     *            The width of the rect.
     * @param height
     *            The height of the rect.
     * @return <code>true</code> if the given rect is inside the raster, else <code>false</code>
     */
    protected final boolean checkBounds( int x, int y, int width, int height ) {
        if ( ( ( getView().x + x + width ) > this.getOriginalWidth() )
             || ( ( getView().y + y + height ) > this.getOriginalHeight() ) || ( getView().x + x < 0 )
             || ( getView().y + y < 0 ) ) {
            return false;
        }
        return true;
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
        return dataAccess.calculatePos( x, y );
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
        return dataAccess.calculatePos( x, y, bandOfView );
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
        return dataAccess.calculateViewPos( x, y );
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
        return dataAccess.calculateViewPos( x, y, band );
    }

    public byte[] getBytes( int x, int y, int width, int height, int band, byte[] result ) {
        checkBoundsEx( x, y, width, height );
        if ( result == null || result.length < ( width * height ) ) {
            result = new byte[width * height];
        }
        for ( int h = 0; h < height; h++ ) {
            for ( int w = 0; w < width; w++ ) {
                int pos = calculatePos( x + w, y + h, band );
                int rOffset = ( 2 * h ) + w;
                if ( pos == -1 ) {// the position is outside the databuffer.
                    result[rOffset] = getView().dataInfo.noDataPixel[getView().getBandOffset( band )];
                } else {
                    result[rOffset] = getByteBuffer().get( pos );
                }
            }
        }
        return result;
    }

    public byte[] getBytePixel( int x, int y, byte[] result ) {
        if ( result == null || result.length < getView().dataInfo.bands ) {
            result = new byte[getView().dataInfo.bands];
        }
        for ( int band = 0; band < getView().dataInfo.bands; band++ ) {
            result[band] = getByteSample( x, y, band );
        }
        return result;
    }

    public byte getByteSample( int x, int y, int band ) {
        int pos = calculatePos( x, y, band );
        if ( pos == -1 ) {// the position is outside the databuffer.
            return getView().dataInfo.noDataPixel[getView().getBandOffset( band )];
        }
        return getByteBuffer().get( pos );
    }

    public double[] getDoubles( int x, int y, int width, int height, int band, double[] result ) {
        checkBoundsEx( x, y, width, height );
        if ( result == null || result.length < ( width * height ) ) {
            result = new double[width * height];
        }
        for ( int h = 0; h < height; h++ ) {
            for ( int w = 0; w < width; w++ ) {
                int pos = calculatePos( x + w, y + h, band );
                int rOffset = ( 2 * h ) + w;
                if ( pos == -1 ) {// the position is outside the databuffer.
                    System.arraycopy( getView().dataInfo.noDataPixel, getView().getBandOffset( band )
                                                                      * getView().dataInfo.dataSize, result, rOffset,
                                      getView().dataInfo.dataSize );
                } else {
                    result[rOffset] = getByteBuffer().getDouble( pos );
                }
            }
        }
        return result;
    }

    public double[] getDoublePixel( int x, int y, double[] result ) {
        if ( result == null || result.length < getView().dataInfo.bands ) {
            result = new double[getView().dataInfo.bands];
        }
        for ( int band = 0; band < getView().dataInfo.bands; band++ ) {
            result[band] = getDoubleSample( x, y, band );
        }
        return result;
    }

    public double getDoubleSample( int x, int y, int band ) {
        int pos = calculatePos( x, y, band );
        if ( pos == -1 ) {// the position is outside the databuffer.
            ByteBuffer wrap = ByteBuffer.wrap( getView().dataInfo.noDataPixel );
            return wrap.getDouble( getView().getBandOffset( band ) * getView().dataInfo.dataSize );
        }
        return getByteBuffer().getDouble( pos );
    }

    public float[] getFloats( int x, int y, int width, int height, int band, float[] result ) {
        checkBoundsEx( x, y, width, height );
        if ( result == null || result.length < ( width * height ) ) {
            result = new float[width * height];
        }
        for ( int h = 0; h < height; h++ ) {
            for ( int w = 0; w < width; w++ ) {
                int pos = calculatePos( x + w, y + h, band );
                int rOffset = ( 2 * h ) + w;
                if ( pos == -1 ) {// the position is outside the databuffer.
                    System.arraycopy( getView().dataInfo.noDataPixel, getView().getBandOffset( band )
                                                                      * getView().dataInfo.dataSize, result, rOffset,
                                      getView().dataInfo.dataSize );
                } else {
                    result[rOffset] = getByteBuffer().getFloat( pos );
                }
                // result[( 2 * h ) + w] = data.getFloat( calculatePos( x + w, y + h, band ) );
            }
        }
        return result;
    }

    public float[] getFloatPixel( int x, int y, float[] result ) {
        if ( result == null || result.length < getView().dataInfo.bands ) {
            result = new float[getView().dataInfo.bands];
        }
        for ( int band = 0; band < getView().dataInfo.bands; band++ ) {
            result[band] = getFloatSample( x, y, band );
        }
        return result;
    }

    public float getFloatSample( int x, int y, int band ) {
        int pos = calculatePos( x, y, band );
        if ( pos == -1 ) {// the position is outside the databuffer.
            ByteBuffer wrap = ByteBuffer.wrap( getView().dataInfo.noDataPixel );
            return wrap.getFloat( getView().getBandOffset( band ) * getView().dataInfo.dataSize );
        }
        return getByteBuffer().getFloat( pos );
        // return data.getFloat( calculatePos( x, y, band ) );
    }

    public int[] getInts( int x, int y, int width, int height, int band, int[] result ) {
        checkBoundsEx( x, y, width, height );
        if ( result == null || result.length < ( width * height ) ) {
            result = new int[width * height];
        }
        for ( int h = 0; h < height; h++ ) {
            for ( int w = 0; w < width; w++ ) {
                int pos = calculatePos( x + w, y + h, band );
                int rOffset = ( 2 * h ) + w;
                if ( pos == -1 ) {// the position is outside the databuffer.
                    System.arraycopy( getView().dataInfo.noDataPixel, getView().getBandOffset( band )
                                                                      * getView().dataInfo.dataSize, result, rOffset,
                                      getView().dataInfo.dataSize );
                } else {
                    result[rOffset] = getByteBuffer().getInt( pos );
                }
                // result[( 2 * h ) + w] = data.getInt( calculatePos( x + w, y + h, band ) );
            }
        }
        return result;
    }

    public int[] getIntPixel( int x, int y, int[] result ) {
        if ( result == null || result.length < getView().dataInfo.bands ) {
            result = new int[getView().dataInfo.bands];
        }
        for ( int band = 0; band < getView().dataInfo.bands; band++ ) {
            result[band] = getIntSample( x, y, band );
        }
        return result;
    }

    public int getIntSample( int x, int y, int band ) {
        int pos = calculatePos( x, y, band );
        if ( pos == -1 ) {// the position is outside the databuffer.
            ByteBuffer wrap = ByteBuffer.wrap( getView().dataInfo.noDataPixel );
            return wrap.getInt( getView().getBandOffset( band ) * getView().dataInfo.dataSize );
        }
        return getByteBuffer().getInt( pos );
        // return data.getInt( calculatePos( x, y, band ) );
    }

    public byte[] getPixel( int x, int y, byte[] result ) {
        // operates on the getView().
        int numBands = getView().dataInfo.bands;
        // datasize should be equal between original data and the view
        int sampleSize = getView().dataInfo.dataSize;
        if ( result == null || result.length < ( numBands * sampleSize ) ) {
            result = new byte[numBands * sampleSize];
        }

        // null pixel
        if ( 0 > x || x >= getOriginalWidth() || 0 > y || y >= getOriginalHeight() ) {
            System.arraycopy( getView().dataInfo.noDataPixel, 0, result, 0, result.length );
            return result;
        }

        // copy per band on the getView().
        for ( int b = 0; b < numBands; b++ ) {
            int pos = calculatePos( x, y, b );
            if ( pos == -1 ) {// the position is outside the databuffer.
                System.arraycopy( getView().dataInfo.noDataPixel, b * sampleSize, result, b * sampleSize, sampleSize );
            } else {
                ByteBuffer buf = getByteBuffer();
                buf.position( pos );
                buf.get( result, b * sampleSize, sampleSize );
            }
        }

        return result;
    }

    public byte[] getSample( int x, int y, int band, byte[] result ) {
        if ( result == null || result.length < getView().dataInfo.dataSize ) {
            result = new byte[getView().dataInfo.dataSize];
        }
        int pos = calculatePos( x, y, band );
        if ( pos == -1 ) {// the position is outside the databuffer.
            System.arraycopy( getView().dataInfo.noDataPixel, getView().getBandOffset( band ), result, 0,
                              getView().dataInfo.dataSize );
        } else {
            ByteBuffer buf = getByteBuffer();
            buf.position( pos );
            buf.get( result, 0, getView().dataInfo.dataSize );
        }
        return result;
    }

    public short[] getShorts( int x, int y, int width, int height, int band, short[] result ) {
        checkBoundsEx( x, y, width, height );
        if ( result == null || result.length < ( width * height ) ) {
            result = new short[width * height];
        }
        for ( int h = 0; h < height; h++ ) {
            for ( int w = 0; w < width; w++ ) {
                int pos = calculatePos( x + w, y + h, band );
                int rOffset = ( 2 * h ) + w;
                if ( pos == -1 ) {// the position is outside the databuffer.
                    System.arraycopy( getView().dataInfo.noDataPixel, getView().getBandOffset( band )
                                                                      * getView().dataInfo.dataSize, result, rOffset,
                                      getView().dataInfo.dataSize );
                } else {
                    result[rOffset] = getByteBuffer().getShort( pos );
                }
                // result[( 2 * h ) + w] = data.getShort( calculatePos( x + w, y + h, band ) );
            }
        }
        return result;
    }

    public short[] getShortPixel( int x, int y, short[] result ) {
        if ( result == null || result.length < getView().dataInfo.bands ) {
            result = new short[getView().dataInfo.bands];
        }
        for ( int band = 0; band < getView().dataInfo.bands; band++ ) {
            result[band] = getShortSample( x, y, band );
        }
        return result;
    }

    public short getShortSample( int x, int y, int band ) {
        int pos = calculatePos( x, y, band );
        if ( pos == -1 ) {// the position is outside the databuffer.
            ByteBuffer wrap = ByteBuffer.wrap( getView().dataInfo.noDataPixel );
            return wrap.getShort( getView().getBandOffset( band ) * getView().dataInfo.dataSize );
        }
        if ( LOG.isDebugEnabled() ) {
            short result = 0;
            try {
                result = getByteBuffer().getShort( pos );
            } catch ( Exception e ) {
                LOG.debug( Thread.currentThread().getName() + "->(x,y)|band->pos: " + x + "," + y + "|" + band + "->"
                           + pos + "\n-view: " + getView() + "\n-rdi: " + getView().dataInfo + "\n-buffer:"
                           + getByteBuffer() );
            }
            return result;
        }
        return getByteBuffer().getShort( pos );

        // return data.getShort( calculatePos( x, y, band ) );
    }

    public ByteBufferRasterData getSubset( RasterRect sampleDomain ) {
        // return this.getSubset( env.x, env.y, env.width, env.height );
        // return createCompatibleRasterData( sampleDomain );
        ByteBufferRasterData result = createCompatibleRasterData( new DataView( getView().x + sampleDomain.x,
                                                                                getView().y + sampleDomain.y,
                                                                                sampleDomain.width,
                                                                                sampleDomain.height, dataInfo ) );
        if ( dataAccess.hasDataBuffer() && dataAccess.getReader() == null ) {
            // the data was loaded, but no reader was available, we need a copy of the data.
            result.dataAccess.setByteBuffer( getByteBuffer().asReadOnlyBuffer(), dataAccess.getBytebufferDomain() );
            // result.dataAccess.setByteBuffer( getByteBuffer().asReadOnlyBuffer(), view );
            // result.data = this.data.asReadOnlyBuffer();
        }
        // result.dataAccess.setByteBuffer( getByteBuffer().asReadOnlyBuffer(), view );
        // result.data = this.data.asReadOnlyBuffer();
        return result;

    }

    public ByteBufferRasterData getSubset( RasterRect sampleDomain, BandType[] bands ) {
        // get the minimal value of width and height, and allow only for positive values.
        int newOrigx = min( getView().x + sampleDomain.x, getOriginalWidth() );
        int newOrigy = min( getView().y + sampleDomain.y, getOriginalHeight() );
        // int newOrigx = getView().x + sampleDomain.x;
        // int newOrigy = getView().y + sampleDomain.y;
        int newWidth = sampleDomain.width;
        int newHeight = sampleDomain.height;

        // if ( ( newOrigx + newWidth ) > rasterWidth ) {
        // newWidth = rasterWidth - newOrigx;
        // }
        // if ( ( newOrigy + newHeight ) > rasterHeight ) {
        // newHeight = rasterHeight - newOrigy;
        // }

        // ByteBufferRasterData result = createCompatibleRasterData( new DataView( getView().x + sampleDomain.x,
        // getView().y + sampleDomain.y,
        // sampleDomain.width,
        // sampleDomain.height,
        // createRasterDataInfo( bands ), dataInfo ) );

        ByteBufferRasterData result = createCompatibleRasterData( new DataView( newOrigx, newOrigy, newWidth,
                                                                                newHeight,
                                                                                createRasterDataInfo( bands ), dataInfo ) );
        result.info = info;
        // result.data = this.data.asReadOnlyBuffer();
        // result.dataAccess.setByteBuffer( getByteBuffer().asReadOnlyBuffer(), view );
        if ( dataAccess.hasDataBuffer() && dataAccess.getReader() == null ) {
            // the data was loaded, but no reader was available, we need a copy of the data.
            result.dataAccess.setByteBuffer( getByteBuffer().asReadOnlyBuffer(), dataAccess.getBytebufferDomain() );
        }

        /** old comments */
        // RASTERRECT viewRect = new RasterRect( ( getView().x + rasterRect.x ), ( getView().y + rasterRect.y ),
        // rasterRect.width,
        // rasterRect.height );
        // ByteBufferRasterData result = (ByteBufferRasterData) createCompatibleWritableRasterData( viewRect, bands );
        // // result.data = this.data.asReadOnlyBuffer();
        // result.setSubset( 0, 0, rasterRect.width, rasterRect.height, 0, this, bands, getView().x + rasterRect.x,
        // getView().y + rasterRect.y );
        return result;
    }

    public void setBytes( int x, int y, int width, int height, int band, byte[] values ) {
        if ( values != null && values.length >= ( width * height ) ) {
            ByteBuffer buf = getByteBuffer();
            for ( int h = 0; h < height; h++ ) {
                for ( int w = 0; w < width; w++ ) {
                    buf.put( calculatePos( x + w, y + h, band ), values[( 2 * h ) + w] );
                }
            }
        }
    }

    public void setBytePixel( int x, int y, byte[] pixel ) {
        ByteBuffer buf = getByteBuffer();
        for ( int band = 0; band < getView().dataInfo.bands; band++ ) {
            buf.put( calculatePos( x, y, band ), pixel[band] );
        }
    }

    public void setByteSample( int x, int y, int band, byte value ) {
        getByteBuffer().put( calculatePos( x, y, band ), value );
    }

    public void setDoubles( int x, int y, int width, int height, int band, double[] values ) {
        if ( values != null && values.length >= ( width * height ) ) {
            ByteBuffer buf = getByteBuffer();
            for ( int h = 0; h < height; h++ ) {
                for ( int w = 0; w < width; w++ ) {
                    buf.putDouble( calculatePos( x + w, y + h, band ), values[( 2 * h ) + w] );
                }
            }
        }
    }

    public void dispose() {
        this.dataAccess.dispose();
    }

    public void setDoublePixel( int x, int y, double[] pixel ) {
        ByteBuffer buf = getByteBuffer();
        for ( int band = 0; band < getView().dataInfo.bands; band++ ) {
            buf.putDouble( calculatePos( x, y, band ), pixel[band] );
        }
    }

    public void setDoubleSample( int x, int y, int band, double value ) {
        getByteBuffer().putDouble( calculatePos( x, y, band ), value );
    }

    public void setFloats( int x, int y, int width, int height, int band, float[] values ) {
        if ( values != null && values.length >= ( width * height ) ) {
            ByteBuffer buf = getByteBuffer();
            for ( int h = 0; h < height; h++ ) {
                for ( int w = 0; w < width; w++ ) {
                    buf.putFloat( calculatePos( x + w, y + h, band ), values[( 2 * h ) + w] );
                }
            }
        }
    }

    public void setFloatPixel( int x, int y, float[] pixel ) {
        ByteBuffer buf = getByteBuffer();
        for ( int band = 0; band < getView().dataInfo.bands; band++ ) {
            buf.putFloat( calculatePos( x, y, band ), pixel[band] );
        }
    }

    public void setFloatSample( int x, int y, int band, float value ) {
        getByteBuffer().putFloat( calculatePos( x, y, band ), value );
    }

    public void setPixel( int x, int y, byte[] result ) {
        // operates on the view
        if ( result != null && result.length == ( getView().dataInfo.dataSize * getView().dataInfo.bands ) ) {
            int sampleSize = getView().dataInfo.dataSize;
            ByteBuffer buf = getByteBuffer();
            for ( int b = 0; b < getView().dataInfo.bands; b++ ) {
                buf.position( calculatePos( x, y, b ) );
                buf.put( result, b * sampleSize, sampleSize );
            }
        }
    }

    public void setInts( int x, int y, int width, int height, int band, int[] values ) {
        if ( values != null && values.length >= ( width * height ) ) {
            ByteBuffer buf = getByteBuffer();
            for ( int h = 0; h < height; h++ ) {
                for ( int w = 0; w < width; w++ ) {
                    buf.putInt( calculatePos( x + w, y + h, band ), values[( 2 * h ) + w] );
                }
            }
        }
    }

    public void setIntPixel( int x, int y, int[] pixel ) {
        ByteBuffer buf = getByteBuffer();
        for ( int band = 0; band < getView().dataInfo.bands; band++ ) {
            buf.putInt( calculatePos( x, y, band ), pixel[band] );
        }
    }

    public void setIntSample( int x, int y, int band, int value ) {
        getByteBuffer().putInt( calculatePos( x, y, band ), value );
    }

    public void setShorts( int x, int y, int width, int height, int band, short[] values ) {
        if ( values != null && values.length >= ( width * height ) ) {
            ByteBuffer buf = getByteBuffer();
            for ( int h = 0; h < height; h++ ) {
                for ( int w = 0; w < width; w++ ) {
                    buf.putShort( calculatePos( x + w, y + h, band ), values[( 2 * h ) + w] );
                }
            }
        }
    }

    public void setShortPixel( int x, int y, short[] pixel ) {
        ByteBuffer buf = getByteBuffer();
        for ( int band = 0; band < getView().dataInfo.bands; band++ ) {
            buf.putShort( calculatePos( x, y, band ), pixel[band] );
        }
    }

    public void setShortSample( int x, int y, int band, short value ) {
        getByteBuffer().putShort( calculatePos( x, y, band ), value );
    }

    public void setSample( int x, int y, int band, byte[] value ) {
        if ( value == null || value.length < getView().dataInfo.dataSize ) {
            return;
        }
        ByteBuffer buf = getByteBuffer();
        buf.position( calculatePos( x, y, band ) );
        buf.put( value, 0, getView().dataInfo.dataSize );
    }

    public void setSubset( int x0, int y0, int width, int height, RasterData sourceRaster ) {
        setSubset( x0, y0, width, height, sourceRaster, 0, 0 );
    }

    public void setSubset( int x0, int y0, int width, int height, int dstBand, RasterData sourceRaster, int srcBand ) {
        setSubset( x0, y0, width, height, dstBand, sourceRaster, srcBand, 0, 0 );
    }

    public void setSubset( int dstX, int dstY, int width, int height, RasterData srcRaster, int srcX, int srcY ) {

        // clamp to maximum possible size
        // int subWidth = min( this.rasterWidth - dstX, width, srcRaster.getWidth() );
        // int subHeight = min( this.rasterHeight - dstY, height, srcRaster.getHeight() );

        int subWidth = clampSize( getColumns(), dstX, srcRaster.getColumns(), srcX, width );
        int subHeight = clampSize( getRows(), dstY, srcRaster.getRows(), srcY, height );

        if ( subHeight <= 0 || subWidth <= 0 ) {
            return;
        }

        byte[] tmp = new byte[dataInfo.dataSize];
        for ( int y = 0; y < subHeight; ++y ) {
            for ( int x = 0; x < subWidth; ++x ) {
                for ( int band = 0; band < this.getView().dataInfo.bands; band++ ) {
                    srcRaster.getSample( x + srcX, y + srcY, band, tmp );
                    setSample( dstX + x, dstY + y, band, tmp );
                }
            }
        }
    }

    public void setSubset( int dstX, int dstY, int width, int height, int dstBand, RasterData srcRaster, int srcBand,
                           int srcX, int srcY ) {
        // clamp to maximum possible size
        // int subWidth = min( this.rasterWidth - dstX, width, srcRaster.getWidth() );
        // int subHeight = min( this.rasterHeight - dstY, height, srcRaster.getHeight() );
        int subWidth = clampSize( getColumns(), dstX, srcRaster.getColumns(), srcX, width );
        int subHeight = clampSize( getRows(), dstY, srcRaster.getRows(), srcY, height );

        if ( subHeight <= 0 || subWidth <= 0 ) {
            return;
        }

        byte[] tmp = new byte[dataInfo.dataSize];
        for ( int y = 0; y < subHeight; y++ ) {
            for ( int x = 0; x < subWidth; x++ ) {
                srcRaster.getSample( x + srcX, y + srcY, srcBand, tmp );
                setSample( dstX + x, dstY + y, dstBand, tmp );
            }
        }
    }

    /**
     * Clamp to the minimal size of the given values.
     * 
     * @param dstSize
     *            e.g. getWidth() or getHeight() of the destination raster.
     * @param dstOrdinate
     *            e.g srcX, srcY, the location to put the data in destination raster.
     * @param srcSize
     *            e.g. getWidth() or getHeight() of the source raster. *
     * @param srcOrdinate
     *            e.g dstX, dstY, the location to read the data from the source raster.
     * @param copySize
     *            e.g. width/height of rectangle to put data in this raster.
     * @return the minimal value
     */
    protected static final int clampSize( int dstSize, int dstOrdinate, int srcSize, int srcOrdinate, int copySize ) {
        return min( dstSize - dstOrdinate, srcSize - srcOrdinate, copySize );
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append( "RasterData: type " + dataInfo.dataType + ", " );
        result.append( "size " + getOriginalWidth() + "x" + getOriginalHeight() );
        result.append( ", interleaving " + dataInfo.interleaveType );

        return result.toString();
    }

    /**
     * Returns the smallest value of all <code>int</code>s.
     * 
     * @param sizes
     * @return the smallest value
     */
    protected static final int min( int... sizes ) {
        int result = Math.min( sizes[0], sizes[1] );
        int i = 2;
        while ( i < sizes.length ) {
            result = Math.min( result, sizes[i] );
            i++;
        }
        return result;
    }

    /**
     * Create the raster data info object for the given bands, if empty or
     * <code>null<code> the current dataInfo will be used.
     * 
     * @param bands
     * @return a new raster data info object with for the given bands.
     */
    protected RasterDataInfo createRasterDataInfo( BandType[] bands ) {
        if ( bands == null || bands.length == 0 ) {
            return dataInfo;
        }
        byte[] noPD = dataInfo.getNoDataPixel( bands );

        // sort the bands according to their first definition.
        SortedMap<Integer, BandType> nb = new TreeMap<Integer, BandType>();
        for ( int i = 0; i < dataInfo.bands; ++i ) {
            for ( int j = 0; j < bands.length; ++j ) {
                if ( bands[j] == dataInfo.bandInfo[i] ) {
                    nb.put( i, bands[j] );
                }
            }
        }
        int index = 0;
        BandType[] newBands = new BandType[bands.length];
        for ( int i : nb.keySet() ) {
            newBands[index++] = nb.get( i );
        }
        return new RasterDataInfo( noPD, newBands, dataInfo.dataType, dataInfo.interleaveType );
    }

    /**
     * @return the Reader which supplies this buffer with data, may be <code>null</code>
     */
    public RasterReader getReader() {
        return this.dataAccess.getReader();
    }
}
